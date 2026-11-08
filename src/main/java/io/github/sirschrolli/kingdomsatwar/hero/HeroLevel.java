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
 * <b>Heldenstufe {@value #MAX_LEVEL}</b> auf – ein weiter Weg: Stufe {@value #MAX_LEVEL} braucht 392.040 Heldenerfahrung.
 * Jede Stufe bringt einen <b>Talentpunkt</b>, jeder neue <b>Rang</b> (Knappe, Recke, Ritter, Held, Kriegsherr, Legende)
 * {@value #RANK_BONUS} Punkte dazu. Die Punkte verteilt er im Charakterfenster (Taste K) auf sechs Talente – je Talent
 * höchstens {@value #MAX_TALENT} Punkte, also lässt sich nicht alles meistern. Neu verteilen kostet eine Silbermünze je
 * verteiltem Punkt. Wer Legende wird, darf streicheln ({@link HeroPat}).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HeroLevel {
    /** Die höchste Heldenstufe. */
    public static final int MAX_LEVEL = 100;
    /** So viele Punkte fasst ein Talent. */
    public static final int MAX_TALENT = 20;
    /** Talentpunkte, die jeder neue Rang zusätzlich bringt. */
    public static final int RANK_BONUS = 2;
    /** Heldenerfahrung für Stufe L: 40 · (L − 1)², für Stufe 100 also 392.040. */
    public static final int CURVE = 40;
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

    /** Die sechs Talente: was ein Punkt bringt, welches Bild und welche Farbe sie im Charakterfenster haben. */
    public enum Talent implements StringRepresentable {
        /** +0,3 Schaden je Punkt (20 Punkte: +6). */
        STRENGTH(() -> Items.IRON_SWORD, 0.3, 0xFFE0584A),
        /** +1 Lebenspunkt je Punkt (20 Punkte: zehn Herzen). */
        VITALITY(() -> Items.GOLDEN_APPLE, 1.0, 0xFFF06A8A),
        /** +1,25 % Lauftempo je Punkt (20 Punkte: +25 %). */
        AGILITY(() -> Items.FEATHER, 1.25, 0xFF8FE07A),
        /** +0,4 Rüstung je Punkt (20 Punkte: +8). */
        TOUGHNESS(() -> Items.IRON_CHESTPLATE, 0.4, 0xFFB8C4D6),
        /** +8 Mana und +4 % Erholung je Punkt (20 Punkte: +160 Mana, +80 %). */
        ARCANA(() -> io.github.sirschrolli.kingdomsatwar.registry.ModItems.MANA_CRYSTAL.get(), 8.0, 0xFF6FA8FF),
        /** +0,2 Glück je Punkt (bessere Beute in Truhen; 20 Punkte: +4). */
        FORTUNE(() -> Items.RABBIT_FOOT, 0.2, 0xFFF2C14E);

        private final Supplier<Item> icon;
        private final double perPoint;
        private final int color;

        Talent(Supplier<Item> icon, double perPoint, int color) {
            this.icon = icon;
            this.perPoint = perPoint;
            this.color = color;
        }

        public ItemStack icon() {
            return new ItemStack(this.icon.get());
        }

        /** Was ein Punkt bringt (Schaden, Leben, Prozent Tempo, Rüstung, Mana, Glück). */
        public double perPoint() {
            return this.perPoint;
        }

        /** Was {@code points} Punkte zusammen bringen. */
        public double value(int points) {
            return this.perPoint * points;
        }

        /** Farbe im Charakterfenster (ARGB). */
        public int color() {
            return this.color;
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
     * Die Ränge des Helden: Wer eine dieser Stufen erreicht, steigt im Rang auf, bekommt {@value #RANK_BONUS} Talentpunkte
     * dazu und ein neues Abzeichen. Die Legende (Stufe 100) darf außerdem streicheln.
     */
    public enum Rank implements StringRepresentable {
        SQUIRE(1, 0xFFD08C52),
        WARRIOR(20, 0xFFD9E1EA),
        KNIGHT(40, 0xFFF2C14E),
        HERO(60, 0xFF52D98F),
        WARLORD(80, 0xFF6EE2F5),
        LEGEND(MAX_LEVEL, 0xFFFF9A3C);

        private final int minLevel;
        private final int color;

        Rank(int minLevel, int color) {
            this.minLevel = minLevel;
            this.color = color;
        }

        /** Ab dieser Heldenstufe. */
        public int minLevel() {
            return this.minLevel;
        }

        /** Die Farbe des Rangs (ARGB). */
        public int color() {
            return this.color;
        }

        /** Der nächste Rang (null für die Legende). */
        public @org.jspecify.annotations.Nullable Rank next() {
            return this.ordinal() + 1 < values().length ? values()[this.ordinal() + 1] : null;
        }

        public static Rank of(int level) {
            Rank rank = SQUIRE;
            for (Rank r : values()) {
                if (level >= r.minLevel) {
                    rank = r;
                }
            }
            return rank;
        }

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        public String translationKey() {
            return "hero.kingdomsatwar.rank." + this.getSerializedName();
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

        /** Sein Rang. */
        public Rank rank() {
            return Rank.of(this.level());
        }

        /** Alle Talentpunkte, die seine Stufe und seine Ränge hergeben. */
        public int earned() {
            return pointsFor(this.level());
        }

        /** Noch zu verteilende Talentpunkte. */
        public int unspent() {
            return Math.max(0, this.earned() - this.spent());
        }

        Hero with(Talent talent, int points) {
            List<Integer> list = new ArrayList<>(this.talents);
            list.set(talent.ordinal(), points);
            return new Hero(this.experience, list, this.discoveries);
        }
    }

    private HeroLevel() {}

    // ------------------------------------------------------------------------------------------------ Stufen

    /** Wie viel Heldenerfahrung man für diese Heldenstufe insgesamt braucht (Stufe 1: keine; Stufe 100: 392.040). */
    public static int total(int level) {
        if (level <= 1) {
            return 0;
        }
        int steps = Math.min(level, MAX_LEVEL) - 1;
        return CURVE * steps * steps;
    }

    /** Talentpunkte auf dieser Stufe: einer je Stufe über der ersten und {@value #RANK_BONUS} für jeden erreichten Rang. */
    public static int pointsFor(int level) {
        return Math.max(0, Math.min(level, MAX_LEVEL) - 1) + RANK_BONUS * Rank.of(level).ordinal();
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
            celebrate(player, before, next.level());
        }
    }

    /**
     * Der Aufstieg: Auf dem Bildschirm des Helden läuft die Aufstiegs-Szene (Abzeichen, Strahlen, Rangtitel); in der Welt
     * sehen alle einen Lichtring, der vom Helden aus über den Boden läuft, eine Spirale in der Farbe seines Rangs, die an ihm
     * aufsteigt, und Funken – bei einem neuen Rang dazu ein Feuerwerk, eine Fanfare und eine Nachricht im Chat.
     */
    private static void celebrate(ServerPlayer player, int from, int level) {
        ServerLevel world = player.level();
        Rank rank = Rank.of(level);
        boolean rankUp = Rank.of(from) != rank;
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        for (int i = 0; i < 36; i++) {
            double a = i * Mth.TWO_PI / 36.0;
            world.sendParticles(ParticleTypes.END_ROD, x + Math.cos(a) * 0.5, y + 0.15, z + Math.sin(a) * 0.5, 0, Math.cos(a), 0.015, Math.sin(a),
                    0.2);
        }
        int rgb = rank.color() & 0xFFFFFF;
        net.minecraft.core.particles.DustColorTransitionOptions dust = new net.minecraft.core.particles.DustColorTransitionOptions(rgb, 0xFFF6DC,
                1.0f);
        // die Spirale sehen nur die anderen (dem Helden selbst flöge sie dicht vor den Augen vorbei)
        for (ServerPlayer viewer : world.players()) {
            if (viewer == player || viewer.distanceToSqr(player) > 48.0 * 48.0) {
                continue;
            }
            for (int i = 0; i < 48; i++) {
                double t = i / 48.0;
                double a = t * Mth.TWO_PI * 3.0;
                double r = 1.05 - t * 0.45;
                for (int k = 0; k < 2; k++) {
                    double b = a + k * Math.PI;
                    world.sendParticles(viewer, dust, false, false, x + Math.cos(b) * r, y + 0.1 + t * 2.7, z + Math.sin(b) * r, 1, 0.0, 0.0, 0.0,
                            0.0);
                }
            }
        }
        // Totem-Funken, Zauberschrift und Feuerwerk sehen nur die anderen – dem Helden selbst flögen sie mitten durchs Bild,
        // er hat ja die Aufstiegs-Szene
        for (ServerPlayer viewer : world.players()) {
            if (viewer == player || viewer.distanceToSqr(player) > 48.0 * 48.0) {
                continue;
            }
            world.sendParticles(viewer, ParticleTypes.TOTEM_OF_UNDYING, false, false, x, y + 1.3, z, rankUp ? 70 : 26, 0.35, 0.6, 0.35,
                    rankUp ? 0.5 : 0.28);
            world.sendParticles(viewer, ParticleTypes.ENCHANT, false, false, x, y + 1.0, z, 40, 0.6, 0.8, 0.6, 1.2);
            if (rankUp) {
                world.sendParticles(viewer, ParticleTypes.FIREWORK, false, false, x, y + 2.6, z, 60, 0.15, 0.15, 0.15, 0.22);
            }
        }
        world.playSound(null, x, y, z, SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.8f);
        world.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.0f, 1.2f);
        if (rankUp) {
            world.playSound(null, x, y, z, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.7f, 1.0f);
            world.playSound(null, x, y, z, SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.8f, 1.1f);
            player.sendSystemMessage(Component.translatable("hero.kingdomsatwar.rank_up", Component.translatable(rank.translationKey())
                    .withColor(rank.color() & 0xFFFFFF).withStyle(ChatFormatting.BOLD), RANK_BONUS).withStyle(ChatFormatting.GOLD));
            if (rank == Rank.LEGEND) {
                player.sendSystemMessage(Component.translatable("hero.kingdomsatwar.reward.pat", Component.keybind("key.kingdomsatwar.pat"))
                        .withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        }
        io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player,
                new io.github.sirschrolli.kingdomsatwar.network.HeroLevelUpPayload(from, level));
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

    /**
     * Setzt die Heldenerfahrung (Befehle): Steigt der Held dabei auf, läuft die Aufstiegs-Szene; sinkt er, bekommt er zu viel
     * verteilte Punkte zurück.
     */
    public static void setExperience(ServerPlayer player, int experience) {
        Hero hero = of(player);
        int before = hero.level();
        Hero next = new Hero(Mth.clamp(experience, 0, total(MAX_LEVEL)), hero.talents(), hero.discoveries());
        player.setData(ModAttachments.HERO, next);
        if (next.level() > before) {
            celebrate(player, before, next.level());
        }
        settle(player);
        apply(player);
    }

    // ------------------------------------------------------------------------------------------------ Befehle

    /**
     * {@code /kaw hero} (nur mit Cheats bzw. für Operatoren): {@code /kaw hero} zeigt Stufe, Rang und Erfahrung;
     * {@code xp add <Menge> [Spieler]} gibt Heldenerfahrung (mit Aufstiegs-Szene), {@code xp set <Menge> [Spieler]} setzt
     * sie, {@code level <Stufe> [Spieler]} springt auf eine Heldenstufe, {@code reset [Spieler]} fängt von vorn an.
     */
    public static com.mojang.brigadier.builder.LiteralArgumentBuilder<net.minecraft.commands.CommandSourceStack> command() {
        var amount = com.mojang.brigadier.arguments.IntegerArgumentType.integer(0, total(MAX_LEVEL));
        var levels = com.mojang.brigadier.arguments.IntegerArgumentType.integer(1, MAX_LEVEL);
        return net.minecraft.commands.Commands.literal("hero")
                .requires(net.minecraft.commands.Commands.hasPermission(net.minecraft.commands.Commands.LEVEL_GAMEMASTERS))
                .executes(c -> status(c.getSource(), List.of(c.getSource().getPlayerOrException())))
                .then(net.minecraft.commands.Commands.literal("xp")
                        .then(net.minecraft.commands.Commands.literal("add")
                                .then(net.minecraft.commands.Commands.argument("amount", amount)
                                        .executes(c -> change(c.getSource(), List.of(c.getSource().getPlayerOrException()), Change.ADD,
                                                com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "amount")))
                                        .then(net.minecraft.commands.Commands.argument("targets", net.minecraft.commands.arguments.EntityArgument.players())
                                                .executes(c -> change(c.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(c, "targets"),
                                                        Change.ADD, com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "amount"))))))
                        .then(net.minecraft.commands.Commands.literal("set")
                                .then(net.minecraft.commands.Commands.argument("amount", amount)
                                        .executes(c -> change(c.getSource(), List.of(c.getSource().getPlayerOrException()), Change.SET,
                                                com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "amount")))
                                        .then(net.minecraft.commands.Commands.argument("targets", net.minecraft.commands.arguments.EntityArgument.players())
                                                .executes(c -> change(c.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(c, "targets"),
                                                        Change.SET, com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "amount")))))))
                .then(net.minecraft.commands.Commands.literal("level")
                        .then(net.minecraft.commands.Commands.argument("level", levels)
                                .executes(c -> change(c.getSource(), List.of(c.getSource().getPlayerOrException()), Change.LEVEL,
                                        com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "level")))
                                .then(net.minecraft.commands.Commands.argument("targets", net.minecraft.commands.arguments.EntityArgument.players())
                                        .executes(c -> change(c.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(c, "targets"),
                                                Change.LEVEL, com.mojang.brigadier.arguments.IntegerArgumentType.getInteger(c, "level"))))))
                .then(net.minecraft.commands.Commands.literal("reset")
                        .executes(c -> change(c.getSource(), List.of(c.getSource().getPlayerOrException()), Change.RESET, 0))
                        .then(net.minecraft.commands.Commands.argument("targets", net.minecraft.commands.arguments.EntityArgument.players())
                                .executes(c -> change(c.getSource(), net.minecraft.commands.arguments.EntityArgument.getPlayers(c, "targets"),
                                        Change.RESET, 0))));
    }

    private enum Change { ADD, SET, LEVEL, RESET }

    private static int change(net.minecraft.commands.CommandSourceStack source, java.util.Collection<ServerPlayer> players, Change change, int value) {
        for (ServerPlayer player : players) {
            Hero hero = of(player);
            switch (change) {
                case ADD -> setExperience(player, (int) Math.min((long) total(MAX_LEVEL), (long) hero.experience() + value));
                case SET -> setExperience(player, value);
                case LEVEL -> setExperience(player, total(value));
                case RESET -> {
                    player.setData(ModAttachments.HERO, new Hero(0, List.of(), hero.discoveries()));
                    apply(player);
                }
            }
        }
        return status(source, players);
    }

    private static int status(net.minecraft.commands.CommandSourceStack source, java.util.Collection<ServerPlayer> players) {
        for (ServerPlayer player : players) {
            Hero hero = of(player);
            Rank rank = hero.rank();
            source.sendSuccess(() -> Component.translatable("commands.kingdomsatwar.hero.status", player.getDisplayName(), hero.level(),
                    Component.translatable(rank.translationKey()).withColor(rank.color() & 0xFFFFFF), String.format(Locale.GERMANY, "%,d", hero.experience()),
                    String.format(Locale.GERMANY, "%,d", hero.level() >= MAX_LEVEL ? total(MAX_LEVEL) : total(hero.level() + 1)), hero.unspent()), true);
        }
        return players.size();
    }

    /** Setzt alles auf einmal (Befehle, Tests) und wendet die Talente an. */
    public static void set(Player player, Hero hero) {
        player.setData(ModAttachments.HERO, hero);
        apply(player);
    }

    /** Erholung des Manas: je Punkt Zauberkunst 4 % schneller. */
    public static final float REGEN_PER_ARCANA = 0.04f;

    /** Mana mehr durch Zauberkunst. */
    public static float bonusMana(Player player) {
        return (float) Talent.ARCANA.value(of(player).points(Talent.ARCANA));
    }

    /** Erholung des Manas: je Punkt Zauberkunst 4 % schneller. */
    public static float manaRegenFactor(Player player) {
        return 1.0f + of(player).points(Talent.ARCANA) * REGEN_PER_ARCANA;
    }

    /** Die Talente als dauerhafte Steigerungen der Werte des Spielers. */
    public static void apply(Player player) {
        Hero hero = of(player);
        modifier(player, Attributes.ATTACK_DAMAGE, Talent.STRENGTH, Talent.STRENGTH.value(hero.points(Talent.STRENGTH)),
                AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MAX_HEALTH, Talent.VITALITY, Talent.VITALITY.value(hero.points(Talent.VITALITY)), AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.MOVEMENT_SPEED, Talent.AGILITY, Talent.AGILITY.value(hero.points(Talent.AGILITY)) / 100.0,
                AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
        modifier(player, Attributes.ARMOR, Talent.TOUGHNESS, Talent.TOUGHNESS.value(hero.points(Talent.TOUGHNESS)), AttributeModifier.Operation.ADD_VALUE);
        modifier(player, Attributes.LUCK, Talent.FORTUNE, Talent.FORTUNE.value(hero.points(Talent.FORTUNE)), AttributeModifier.Operation.ADD_VALUE);
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
        if (event.getEntity() instanceof ServerPlayer player) {
            settle(player);
        }
        apply(event.getEntity());
    }

    /**
     * Stimmen verteilte Punkte und Stufe nicht mehr zusammen (ältere Spielstände: früher reichte weniger Erfahrung für eine
     * Stufe, und ein Talent fasste weniger Punkte), bekommt der Held seine Punkte kostenlos zurück und verteilt neu.
     */
    public static boolean settle(ServerPlayer player) {
        Hero hero = of(player);
        if (hero.spent() <= hero.earned()) {
            return false;
        }
        player.setData(ModAttachments.HERO, new Hero(hero.experience(), List.of(), hero.discoveries()));
        player.sendSystemMessage(Component.translatable("hero.kingdomsatwar.settled", hero.level()).withStyle(ChatFormatting.GOLD));
        return true;
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
