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
 *     <li>Gegner haben 50 % mehr Leben, treffen 30 % härter und wittern Beute auf größere Entfernung (Bosse: 25 % und 15 %).</li>
 *     <li>Die natürliche Heilung ist halbiert.</li>
 *     <li>Wer stirbt, ist nach der Wiederkehr zwei Minuten lang geschwächt.</li>
 * </ul>
 * Auf dem GameTest-Server gelten nur die Koordinatensperre und die einzeln aufgerufenen Regeln (die Tests prüfen die
 * Spielmechanik mit ihren Grundwerten).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Hardness {
    public static final double ENEMY_HEALTH = 0.5;
    public static final double ENEMY_DAMAGE = 0.3;
    public static final double BOSS_HEALTH = 0.25;
    public static final double BOSS_DAMAGE = 0.15;
    public static final double ENEMY_FOLLOW_RANGE = 0.25;
    public static final float NATURAL_REGENERATION = 0.5f;
    public static final int RESPAWN_WEAKNESS_TICKS = 2400;

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

    /** Mehr Leben und größere Witterung (einmalig: die Verstärkung bleibt gespeichert). Gibt zurück, ob sie neu ist. */
    public static boolean strengthen(Mob mob) {
        boolean boss = isBoss(mob);
        float before = mob.getMaxHealth();
        boolean fresh = addModifier(mob, Attributes.MAX_HEALTH, HEALTH, boss ? BOSS_HEALTH : ENEMY_HEALTH);
        addModifier(mob, Attributes.FOLLOW_RANGE, FOLLOW_RANGE, ENEMY_FOLLOW_RANGE);
        if (fresh && mob.getHealth() >= before - 0.01f) {
            mob.setHealth(mob.getMaxHealth());
        }
        return fresh;
    }

    private static boolean addModifier(Mob mob, Holder<Attribute> attribute, Identifier id, double amount) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance == null || instance.hasModifier(id)) {
            return false;
        }
        instance.addPermanentModifier(new AttributeModifier(id, amount, AttributeModifier.Operation.ADD_MULTIPLIED_BASE));
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

    /** Nach dem Tod: zwei Minuten Schwäche. */
    public static void weaken(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, RESPAWN_WEAKNESS_TICKS, 0));
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.hardness.weakened").withStyle(ChatFormatting.DARK_RED));
    }
}
