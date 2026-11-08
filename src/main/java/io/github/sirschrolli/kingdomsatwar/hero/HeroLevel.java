package io.github.sirschrolli.kingdomsatwar.hero;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.function.Supplier;
import net.minecraft.ChatFormatting;
import net.minecraft.advancements.DisplayInfo;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.AdvancementEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Das eigene Levelsystem des Helden – ganz getrennt von der Erfahrung von Minecraft (Erfahrungskugeln, Stufen und
 * Verzauberungen bleiben, wie sie sind).
 *
 * <p>Der Held sammelt <b>Heldenerfahrung</b>: für besiegte Monster und Bosse (auch ein Viertel dessen, was seine
 * Soldaten und gezähmten Drachen besiegen), für Erz, das er abbaut, für jeden Fortschritt (Aufgabe, Ziel, Herausforderung),
 * für jedes Biom, das er zum ersten Mal betritt, und für jede eroberte Burg. Damit steigt er bis zur
 * <b>Heldenstufe {@value #MAX_LEVEL}</b> auf und bekommt mit jeder Stufe einen <b>Talentpunkt</b>, den er im
 * Charakterfenster (Taste K) auf sechs Talente verteilt – je Talent höchstens {@value #MAX_TALENT} Punkte, also lässt sich
 * nicht alles meistern. Neu verteilen kostet eine Silbermünze je verteiltem Punkt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HeroLevel {
    /** Die höchste Heldenstufe. */
    public static final int MAX_LEVEL = 50;
    /** So viele Punkte fasst ein Talent. */
    public static final int MAX_TALENT = 10;
    /** Neu verteilen: eine Silbermünze (9 Bronze) je verteiltem Punkt. */
    public static final int RESET_PER_POINT = 9;
    /** Heldenerfahrung für einen Sieg: die Hälfte der Lebenspunkte, Bosse doppelt, höchstens so viel. */
    public static final float KILL_SHARE = 0.5f;
    public static final int MAX_KILL = 500;
    /** So viel bekommt der Held, wenn seine Soldaten oder gezähmten Wesen siegen. */
    public static final float FOLLOWER_SHARE = 0.25f;
    /** Für ein neues Biom und für eine eroberte Burg. */
    public static final int NEW_BIOME = 20;
    public static final int CONQUEST = 500;

    /** Die sechs Talente: was ein Punkt bringt und welches Bild sie im Charakterfenster haben. */
    public enum Talent implements StringRepresentable {
        /** +0,5 Schaden je Punkt. */
        STRENGTH(() -> Items.IRON_SWORD),
        /** +1 Lebenspunkt je Punkt. */
        VITALITY(() -> Items.GOLDEN_APPLE),
        /** +2 % Lauftempo je Punkt. */
        AGILITY(() -> Items.FEATHER),
        /** +0,5 Rüstung je Punkt. */
        TOUGHNESS(() -> Items.IRON_CHESTPLATE),
        /** +10 Mana und +5 % Erholung je Punkt. */
        ARCANA(() -> io.github.sirschrolli.kingdomsatwar.registry.ModItems.MANA_CRYSTAL.get()),
        /** +0,25 Glück je Punkt (bessere Beute in Truhen). */
        FORTUNE(() -> Items.RABBIT_FOOT);

        private final Supplier<Item> icon;

        Talent(Supplier<Item> icon) {
            this.icon = icon;
        }

        public ItemStack icon() {
            return new ItemStack(this.icon.get());
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "hero.kingdomsatwar.talent." + this.getSerializedName();
        }

        Identifier modifierId() {
            return KingdomsAtWar.id("hero_" + this.getSerializedName());
        }
    }

    /**
     * Was der Held gesammelt hat: Heldenerfahrung, die Punkte in jedem Talent (in der Reihenfolge von {@link Talent}) und
     * wie viele Biome er schon entdeckt hat.
     */
    public record Hero(int experience, List<Integer> talents, int discoveries) {
        public static final Hero NONE = new Hero(0, List.of(0, 0, 0, 0, 0, 0), 0);
        public static final Codec<Hero> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("experience", 0).forGetter(Hero::experience),
                Codec.INT.listOf().optionalFieldOf("talents", List.of()).forGetter(Hero::talents),
                Codec.INT.optionalFieldOf("discoveries", 0).forGetter(Hero::discoveries)).apply(i, Hero::new));
        public static final StreamCodec<ByteBuf, Hero> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Hero::experience,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list(Talent.values().length)), Hero::talents,
                ByteBufCodecs.VAR_INT, Hero::discoveries,
                Hero::new);

        public Hero {
            List<Integer> fixed = new ArrayList<>(talents);
            while (fixed.size() < Talent.values().length) {
                fixed.add(0);
            }
            talents = List.copyOf(fixed.subList(0, Talent.values().length).stream().map(p -> Mth.clamp(p, 0, MAX_TALENT)).toList());
            experience = Math.max(0, experience);
        }

        public int points(Talent talent) {
            return this.talents.get(talent.ordinal());
        }

        public int level() {
            return levelFor(this.experience);
        }

        /** Verteilte Punkte. */
        public int spent() {
            return this.talents.stream().mapToInt(Integer::intValue).sum();
        }

        /** Noch zu verteilende Talentpunkte. */
        public int unspent() {
            return Math.max(0, this.level() - 1 - this.spent());
        }

        Hero with(Talent talent, int points) {
            List<Integer> list = new ArrayList<>(this.talents);
            list.set(talent.ordinal(), points);
            return new Hero(this.experience, list, this.discoveries);
        }
    }

    private HeroLevel() {}

    // ------------------------------------------------------------------------------------------------ Stufen

    /** Wie viel Heldenerfahrung man für diese Heldenstufe insgesamt braucht (Stufe 1: keine). */
    public static int total(int level) {
        if (level <= 1) {
            return 0;
        }
        return (int) Math.round(50.0 * Math.pow(Math.min(level, MAX_LEVEL) - 1, 1.7) / 10.0) * 10;
    }

    public static int levelFor(int experience) {
        int level = 1;
        while (level < MAX_LEVEL && experience >= total(level + 1)) {
            level++;
        }
        return level;
    }

    /** Wie weit der Weg zur nächsten Stufe gegangen ist (0 bis 1; auf der höchsten Stufe 1). */
    public static float progress(int experience) {
        int level = levelFor(experience);
        if (level >= MAX_LEVEL) {
            return 1.0f;
        }
        int from = total(level);
        return Mth.clamp((experience - from) / (float) (total(level + 1) - from), 0.0f, 1.0f);
    }

    public static Hero of(Player player) {
        return player.getData(ModAttachments.HERO);
    }

    // ------------------------------------------------------------------------------------------------ Erfahrung

    /**
     * Gibt dem Helden Heldenerfahrung (bis zur höchsten Stufe). Steigt er auf, gibt es Klang, Funken und einen Titel –
     * und den Talentpunkt dazu.
     */
    public static void award(ServerPlayer player, int amount) {
        if (amount <= 0 || player.isSpectator()) {
            return;
        }
        Hero hero = of(player);
        int cap = total(MAX_LEVEL);
        if (hero.experience() >= cap) {
            return;
        }
        int before = hero.level();
        Hero next = new Hero(Math.min(cap, hero.experience() + amount), hero.talents(), hero.discoveries());
        player.setData(ModAttachments.HERO, next);
        if (next.level() > before) {
            celebrate(player, next.level());
        }
    }

    private static void celebrate(ServerPlayer player, int level) {
        ServerLevel world = player.level();
        world.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.0, player.getZ(), 30, 0.5, 0.8, 0.5, 0.3);
        world.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.55f,
                1.15f);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(8, 50, 16));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("hero.kingdomsatwar.level_up.title", level)
                .withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("hero.kingdomsatwar.level_up.subtitle",
                Component.keybind("key.kingdomsatwar.character")).withStyle(ChatFormatting.YELLOW)));
    }

    /** Heldenerfahrung für einen Sieg über {@code victim}. */
    public static int killExperience(LivingEntity victim) {
        float experience = Math.max(1.0f, victim.getMaxHealth() * KILL_SHARE);
        if (victim instanceof ElementalBoss || victim.typeHolder().is(Tags.EntityTypes.BOSSES)) {
            experience *= 2.0f;
        }
        return Math.min(MAX_KILL, Math.round(experience));
    }

    /**
     * Heldenerfahrung für ein Erz: Diamant, Smaragd, Antiker Schutt und Draconium viel, Edelstein, Aetherit und Wyvernit
     * etwas weniger, Kohle und Karbon wenig, Eisen und Kupfer zwei, andere Erze drei.
     */
    public static int oreExperience(BlockState state) {
        if (!state.is(Tags.Blocks.ORES)) {
            return 0;
        }
        if (state.is(Tags.BlockItems.ORES_DIAMOND.block()) || state.is(Tags.BlockItems.ORES_EMERALD.block())
                || state.is(Tags.BlockItems.ORES_NETHERITE_SCRAP.block())
                || state.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.ORES_DRACONIUM)) {
            return 8;
        }
        if (state.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.ORES_GEMSTONE)
                || state.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.ORES_AETHERIT)
                || state.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.ORES_WYVERNIT)) {
            return 5;
        }
        if (state.is(Tags.BlockItems.ORES_COAL.block()) || state.is(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Blocks.ORES_CARBON)) {
            return 1;
        }
        if (state.is(Tags.BlockItems.ORES_IRON.block()) || state.is(Tags.BlockItems.ORES_COPPER.block())) {
            return 2;
        }
        return 3;
    }

    /** Heldenerfahrung für einen Fortschritt: Aufgabe 25, Ziel 60, Herausforderung 150 (Rezepte und Unsichtbares nichts). */
    public static int advancementExperience(DisplayInfo display) {
        return switch (display.type()) {
            case TASK -> 25;
            case GOAL -> 60;
            case CHALLENGE -> 150;
        };
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel) || !(victim instanceof Enemy || victim instanceof ElementalBoss)) {
            return;
        }
        if (event.getSource().getEntity() instanceof ServerPlayer player) {
            award(player, killExperience(victim));
        } else if (event.getSource().getEntity() instanceof OwnableEntity follower && follower.getOwner() instanceof ServerPlayer owner) {
            award(owner, Math.round(killExperience(victim) * FOLLOWER_SHARE));
        }
    }

    @SubscribeEvent
    static void onBreak(BreakBlockEvent event) {
        if (!(event.getPlayer() instanceof ServerPlayer player) || player.hasInfiniteMaterials()) {
            return;
        }
        int experience = oreExperience(event.getState());
        if (experience <= 0) {
            return;
        }
        ItemStack tool = player.getMainHandItem();
        // mit Behutsamkeit gibt es nichts: sonst ließe sich dasselbe Erz immer wieder setzen und abbauen
        Holder<net.minecraft.world.item.enchantment.Enchantment> silk = player.level().registryAccess()
                .lookupOrThrow(Registries.ENCHANTMENT).getOrThrow(Enchantments.SILK_TOUCH);
        if (EnchantmentHelper.getItemEnchantmentLevel(silk, tool) > 0 || !player.hasCorrectToolForDrops(event.getState())) {
            return;
        }
        award(player, experience);
    }

    @SubscribeEvent
    static void onAdvancement(AdvancementEvent.AdvancementEarnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            event.getAdvancement().value().display().ifPresent(display -> award(player, advancementExperience(display)));
        }
    }

    /** Alle zwei Sekunden: Ist der Held in einem Biom, das er noch nie betreten hat? */
    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || player.tickCount % 40 != 0 || player.isSpectator()) {
            return;
        }
        String biome = player.level().getBiome(player.blockPosition()).unwrapKey().map(key -> key.identifier().toString()).orElse("");
        if (biome.isEmpty()) {
            return;
        }
        List<String> seen = player.getData(ModAttachments.HERO_BIOMES);
        if (seen.contains(biome)) {
            return;
        }
        List<String> next = new ArrayList<>(seen);
        next.add(biome);
        player.setData(ModAttachments.HERO_BIOMES, List.copyOf(next));
        Hero hero = of(player);
        player.setData(ModAttachments.HERO, new Hero(hero.experience(), hero.talents(), next.size()));
        if (seen.isEmpty()) {
            return; // das Biom, in dem er erwacht, ist keine Entdeckung
        }
        award(player, NEW_BIOME);
        player.sendOverlayMessage(Component.translatable("hero.kingdomsatwar.discovered",
                Component.translatable(Util.biomeKey(biome))).withStyle(ChatFormatting.AQUA));
    }

    // ------------------------------------------------------------------------------------------------ Talente

    /** Einen Talentpunkt in {@code talent} stecken (wenn noch einer frei ist und das Talent nicht voll). */
    public static boolean spend(ServerPlayer player, Talent talent) {
        Hero hero = of(player);
        if (hero.unspent() <= 0 || hero.points(talent) >= MAX_TALENT) {
            return false;
        }
        player.setData(ModAttachments.HERO, hero.with(talent, hero.points(talent) + 1));
        apply(player);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.6f,
                1.3f);
        return true;
    }

    /** Was das Neuverteilen kostet (in Bronze). */
    public static int resetCost(Hero hero) {
        return hero.spent() * RESET_PER_POINT;
    }

    /** Alle Punkte zurück – gegen eine Silbermünze je Punkt. */
    public static boolean reset(ServerPlayer player) {
        Hero hero = of(player);
        int cost = resetCost(hero);
        if (hero.spent() == 0 || !player.hasInfiniteMaterials() && !Coins.canAfford(player, cost)) {
            return false;
        }
        if (!player.hasInfiniteMaterials()) {
            Coins.pay(player, cost);
        }
        player.setData(ModAttachments.HERO, new Hero(hero.experience(), List.of(), hero.discoveries()));
        apply(player);
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.9f);
        return true;
    }

    /** Setzt alles auf einmal (Befehle, Tests) und wendet die Talente an. */
    public static void set(Player player, Hero hero) {
        player.setData(ModAttachments.HERO, hero);
        apply(player);
    }

    /** Mana mehr durch Zauberkunst. */
    public static float bonusMana(Player player) {
        return of(player).points(Talent.ARCANA) * 10.0f;
    }

    /** Erholung des Manas: je Punkt Zauberkunst 5 % schneller. */
    public static float manaRegenFactor(Player player) {
        return 1.0f + of(player).points(Talent.ARCANA) * 0.05f;
    }

    /** Die Talente als dauerhafte Steigerungen der Werte des Spielers. */
    public static void apply(Player player) {
        Hero hero = of(player);
        modifier(player, Attributes.ATTACK_DAMAGE, Talent.STRENGTH, 0.5 * hero.points(Talent.STRENGTH), AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MAX_HEALTH, Talent.VITALITY, 1.0 * hero.points(Talent.VITALITY), AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MOVEMENT_SPEED, Talent.AGILITY, 0.02 * hero.points(Talent.AGILITY), AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.ARMOR, Talent.TOUGHNESS, 0.5 * hero.points(Talent.TOUGHNESS), AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.LUCK, Talent.FORTUNE, 0.25 * hero.points(Talent.FORTUNE), AttributeModifier.Operation.ADD_VALUE);
        if (player.getHealth() > player.getMaxHealth()) {
            player.setHealth(player.getMaxHealth());
        }
    }

    private static void modifier(Player player, Holder<Attribute> attribute, Talent talent, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) {
            return;
        }
        if (amount > 0.0) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(talent.modifierId(), amount, operation));
        } else {
            instance.removeModifier(talent.modifierId());
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        apply(event.getEntity());
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        apply(event.getEntity());
        if (!event.isEndConquered()) {
            event.getEntity().setHealth(event.getEntity().getMaxHealth());
        }
    }

    /** Für Anzeigen: welche Biome der Held schon kennt (nur auf dem Server). */
    public static Set<String> biomes(Player player) {
        return new HashSet<>(player.getData(ModAttachments.HERO_BIOMES));
    }

    /** Hilfen für Texte. */
    static final class Util {
        private Util() {}

        /** Der Übersetzungsschlüssel eines Bioms aus seiner Kennung ("minecraft:plains" → "biome.minecraft.plains"). */
        static String biomeKey(String id) {
            int colon = id.indexOf(':');
            return colon < 0 ? "biome.minecraft." + id : "biome." + id.substring(0, colon) + "." + id.substring(colon + 1).replace('/', '.');
        }
    }
}
