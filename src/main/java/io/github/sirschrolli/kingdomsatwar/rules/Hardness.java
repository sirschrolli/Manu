package io.github.sirschrolli.kingdomsatwar.rules;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.gamerules.GameRules;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingHealEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Härte von Kingdoms at War – fest eingebaut, nicht abschaltbar:
 * <ul>
 *     <li>Die Schwierigkeit steht immer auf <i>Schwer</i> und ist gesperrt.</li>
 *     <li>Die Debug-Anzeige (F3) zeigt keine Koordinaten (verkürzte Debug-Infos, auch per Spielregel nicht abschaltbar).</li>
 *     <li>Gegner haben doppelt so viel Leben, treffen 50 % härter und wittern Beute doppelt so weit (höchstens
 *     {@value #MAX_FOLLOW_RANGE} Blöcke; Bosse: 25 % mehr Leben, 15 % härter; bis 1.2: 50 % und 30 %).</li>
 *     <li>Die natürliche Heilung ist halbiert.</li>
 *     <li>Wer stirbt, ist nach der Wiederkehr zwei Minuten lang geschwächt.</li>
 * </ul>
 * Auf dem GameTest-Server gelten nur die Koordinatensperre und die einzeln aufgerufenen Regeln (die Tests prüfen die
 * Spielmechanik mit ihren Grundwerten).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Hardness {
    /** Mehr Leben der Gegner (Anteil, seit 1.3; bis 1.2: 0,5). */
    public static final double ENEMY_HEALTH = 1.0;
    /** So viel härter treffen Gegner (Anteil, seit 1.3; bis 1.2: 0,3). */
    public static final double ENEMY_DAMAGE = 0.5;
    public static final double BOSS_HEALTH = 0.25;
    public static final double BOSS_DAMAGE = 0.15;
    /** So viel weiter wittern Gegner ihre Beute (Anteil, seit 1.3: doppelt so weit; bis 1.2: 0,25) … */
    public static final double ENEMY_FOLLOW_RANGE = 1.0;
    /** … aber nie weiter als so viele Blöcke durch die Härte (wer von Natur aus weiter wittert, behält das). */
    public static final double MAX_FOLLOW_RANGE = 48.0;
    public static final float NATURAL_REGENERATION = 0.5f;
    public static final int RESPAWN_WEAKNESS_TICKS = 2400;
    /** Um so viel sind die Schläge nach dem Tod schwächer (wie Schwäche I). */
    public static final double DEATH_WEAKNESS_DAMAGE = -4.0;
    static final Identifier DEATH_WEAKNESS = KingdomsAtWar.id("death_weakness");

    static final Identifier HEALTH = KingdomsAtWar.id("hardness_health");
    static final Identifier FOLLOW_RANGE = KingdomsAtWar.id("hardness_follow_range");

    /** Der Spieler, dessen Tick gerade läuft (nur im Server-Thread gesetzt): Heilung darin ist natürliche Heilung. */
    private static @Nullable Player ticking;

    private Hardness() {}

    /** Gilt die volle Härte auf diesem Server? (Nicht auf dem GameTest-Server.) */
    public static boolean active(@Nullable MinecraftServer server) {
        return server != null && !(server instanceof GameTestServer);
    }

    // =====================================================================================
    // Schwierigkeit und Koordinaten
    // =====================================================================================

    @SubscribeEvent
    static void onServerStarted(ServerStartedEvent event) {
        enforce(event.getServer());
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        ticking = null;
        if (event.getServer().getTickCount() % 40 == 0) {
            enforce(event.getServer());
        }
    }

    /** Schwierigkeit „Schwer“ (gesperrt) und die verkürzte Debug-Anzeige – auch wenn jemand sie umgestellt hat. */
    public static void enforce(MinecraftServer server) {
        GameRules rules = server.getGameRules();
        if (!rules.get(GameRules.REDUCED_DEBUG_INFO)) {
            rules.set(GameRules.REDUCED_DEBUG_INFO, true, server);
        }
        if (!active(server)) {
            return;
        }
        if (server.getWorldData().getDifficulty() != Difficulty.HARD) {
            server.setDifficulty(Difficulty.HARD, true);
        }
        if (!server.getWorldData().isDifficultyLocked()) {
            server.setDifficultyLocked(true);
        }
    }

    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            recover(player);
            // die Spielregel schickt das Signal beim Betreten schon – sicher ist sicher
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundEntityEventPacket(player,
                    net.minecraft.world.entity.EntityEvent.REDUCED_DEBUG_INFO));
        }
    }

    // =====================================================================================
    // Gegner
    // =====================================================================================

    /** Ein Gegner (feindlich und niemandes Begleiter)? */
    public static boolean isEnemy(@Nullable Entity entity) {
        return entity instanceof Mob mob && mob instanceof Enemy && !(mob instanceof OwnableEntity ownable && ownable.getOwnerReference() != null);
    }

    /** Ein Boss: Drachen und Erzmagier, der Aschenkönig und andere Bosse, der Herrscher eines Reichs. */
    public static boolean isBoss(Entity entity) {
        return entity instanceof ElementalBoss || entity.is(Tags.EntityTypes.BOSSES)
                || entity instanceof RealmWarriorEntity warrior && warrior.role() == WarriorRole.LORD;
    }

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel level && active(level.getServer()) && isEnemy(event.getEntity())) {
            strengthen((Mob) event.getEntity());
        }
    }

    /**
     * Mehr Leben und größere Witterung (einmalig: die Verstärkung bleibt gespeichert). Gibt zurück, ob sie neu ist. Wer noch
     * die Verstärkung einer älteren Fassung trägt, bekommt die neue – mit demselben Anteil an Leben wie vorher.
     */
    public static boolean strengthen(Mob mob) {
        boolean boss = isBoss(mob);
        float before = mob.getMaxHealth();
        float share = mob.getHealth() / Math.max(1.0f, before);
        boolean fresh = addModifier(mob, Attributes.MAX_HEALTH, HEALTH, boss ? BOSS_HEALTH : ENEMY_HEALTH);
        addModifier(mob, Attributes.FOLLOW_RANGE, FOLLOW_RANGE, followRangeBonus(mob));
        if (fresh) {
            mob.setHealth(share >= 0.999f ? mob.getMaxHealth() : Math.max(1.0f, share * mob.getMaxHealth()));
        }
        return fresh;
    }

    /** Die größere Witterung: doppelt so weit, aber durch die Härte nie über {@value #MAX_FOLLOW_RANGE} Blöcke. */
    static double followRangeBonus(Mob mob) {
        AttributeInstance range = mob.getAttribute(Attributes.FOLLOW_RANGE);
        double base = range == null ? 0.0 : range.getBaseValue();
        if (base <= 0.0 || base >= MAX_FOLLOW_RANGE) {
            return 0.0;
        }
        return Math.min(ENEMY_FOLLOW_RANGE, MAX_FOLLOW_RANGE / base - 1.0);
    }

    /** Setzt die Verstärkung (oder ersetzt die einer älteren Fassung mit anderem Wert). Gibt zurück, ob sich etwas änderte. */
    private static boolean addModifier(Mob mob, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null) {
            return false;
        }
        AttributeModifier present = instance.getModifier(id);
        if (present != null && Math.abs(present.amount() - amount) < 1.0e-6) {
            return false;
        }
        if (present == null && amount == 0.0) {
            return false;
        }
        instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
        return true;
    }

    /** Gegner treffen härter – auch mit Pfeilen, Zaubern und Explosionen (alles, was sie verursachen). */
    @SubscribeEvent(priority = EventPriority.HIGH)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel level) || !active(level.getServer())) {
            return;
        }
        event.setAmount(scaleDamage(event.getSource().getEntity(), victim, event.getAmount()));
    }

    /** Der Schaden, den ein Gegner diesem Opfer zufügt (unverändert, wenn der Angreifer kein Gegner ist). */
    public static float scaleDamage(@Nullable Entity attacker, LivingEntity victim, float amount) {
        if (!isEnemy(attacker) || victim instanceof Enemy) {
            return amount;
        }
        return (float) (amount * (1.0 + (isBoss(attacker) ? BOSS_DAMAGE : ENEMY_DAMAGE)));
    }

    // =====================================================================================
    // Heilung und Tod
    // =====================================================================================

    // Der Server sättigt den Spieler gleich nach seinem Tick (ServerPlayer.doTick: erst tick(), dann die Nahrung): Was
    // zwischen dem Ende seines Ticks und dem nächsten Spieler-Tick heilt, ist die natürliche Heilung.

    @SubscribeEvent
    static void onPlayerTickPre(PlayerTickEvent.Pre event) {
        if (!event.getEntity().level().isClientSide()) {
            ticking = null;
        }
    }

    @SubscribeEvent
    static void onPlayerTickPost(PlayerTickEvent.Post event) {
        if (!event.getEntity().level().isClientSide()) {
            ticking = event.getEntity();
            if (event.getEntity() instanceof ServerPlayer player && player.tickCount % 20 == 0) {
                recover(player);
            }
        }
    }

    @SubscribeEvent
    static void onServerTickStart(ServerTickEvent.Pre event) {
        ticking = null;
    }

    /** Natürliche Heilung (durch Sättigung, gleich nach dem Tick des Spielers, ohne Regeneration) ist halbiert. */
    @SubscribeEvent
    static void onHeal(LivingHealEvent event) {
        if (event.getEntity() instanceof Player player && player == ticking && player.level() instanceof ServerLevel level
                && active(level.getServer())) {
            event.setAmount(naturalHeal(player, event.getAmount()));
        }
    }

    /** Wie viel natürliche Heilung ankommt: kleine Heilschritte ohne Regenerationseffekt werden halbiert. */
    public static float naturalHeal(Player player, float amount) {
        if (amount > 1.0f || player.hasEffect(MobEffects.REGENERATION)) {
            return amount;
        }
        return amount * NATURAL_REGENERATION;
    }

    @SubscribeEvent
    static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.isEndConquered() || !(event.getEntity() instanceof ServerPlayer player) || !active(player.level().getServer())) {
            return;
        }
        weaken(player);
    }

    /**
     * Nach dem Tod: zwei Minuten Schwäche – nicht als Statuseffekt (kein Symbol, keine Partikel, Milch hilft nicht), sondern
     * einfach so: Die Schläge sind um {@value #DEATH_WEAKNESS_DAMAGE} schwächer, so viel wie bei Schwäche I. Die Zeit läuft
     * in der Spielzeit weiter (auch über Ab- und Anmelden hinweg); danach ist der Abzug wieder weg.
     */
    public static void weaken(ServerPlayer player) {
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        if (attack != null) {
            attack.addOrReplacePermanentModifier(new AttributeModifier(DEATH_WEAKNESS, DEATH_WEAKNESS_DAMAGE, AttributeModifier.Operation.ADD_VALUE));
        }
        player.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.WEAKENED_UNTIL,
                player.level().getGameTime() + RESPAWN_WEAKNESS_TICKS);
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.hardness.weakened").withStyle(ChatFormatting.DARK_RED));
    }

    /** Ist der Spieler (nach seinem Tod) noch geschwächt? */
    public static boolean weakened(Player player) {
        AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
        return attack != null && attack.getModifier(DEATH_WEAKNESS) != null;
    }

    /** Ist die Zeit um, verschwindet der Abzug wieder (geprüft jede Sekunde und beim Anmelden). */
    public static void recover(ServerPlayer player) {
        if (!weakened(player)) {
            return;
        }
        long until = player.getData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.WEAKENED_UNTIL);
        long now = player.level().getGameTime();
        // abgelaufen – oder die Spielzeit lief zurück (Welt neu erzeugt): dann auch weg
        if (now >= until || until - now > RESPAWN_WEAKNESS_TICKS) {
            AttributeInstance attack = player.getAttribute(Attributes.ATTACK_DAMAGE);
            if (attack != null) {
                attack.removeModifier(DEATH_WEAKNESS);
            }
            player.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.WEAKENED_UNTIL, 0L);
        }
    }
}
