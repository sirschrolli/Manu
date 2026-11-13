package io.github.sirschrolli.kingdomsatwar.army;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

/**
 * Kampferfahrung der Soldaten: Wer kämpft, lernt. Ein Soldat sammelt Kampferfahrung für jeden Schaden, den er einem
 * Gegner zufügt, und für jeden Sieg – je stärker der Gegner, desto mehr, für Bosse besonders viel; Kameraden in der Nähe
 * lernen ein Viertel davon mit. Mit der Erfahrung steigt er Stufe um Stufe auf (bis {@value #MAX_LEVEL}); jede Stufe gibt
 * ihm etwas mehr Leben, Schaden und Rüstung.
 *
 * <p>Die Ränge am Feldherrentisch setzen eine Stufe voraus – Veteran ab Stufe 3, Elite ab 6, Meister ab 10, Legende ab
 * 15 – und kosten wie bisher Sold und Ausrüstung. Ein Meister erholt sich außerhalb des Kampfes doppelt so schnell; eine
 * Legende kommt einmal in jedem Kampf (dann zwei Minuten nicht wieder) mit einem zweiten Atem zurück, wenn es eng wird,
 * und an ihrer Seite lernen die Kameraden schneller.
 *
 * <p>Die Erfahrung der Spieler (Minecraft-Erfahrungspunkte) bleibt davon ganz unberührt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Veterancy {
    /** Die höchste Stufe. */
    public static final int MAX_LEVEL = 20;
    /** Mindeststufe für jeden Rang (Index = Rang): Rekrut, Veteran, Elite, Meister, Legende. */
    private static final int[] RANK_LEVEL = {1, 3, 6, 10, 15};
    /** Kampferfahrung je Schadenspunkt am Gegner. */
    public static final float PER_DAMAGE = 0.5f;
    /** Kampferfahrung für einen Sieg: die Hälfte der Lebenspunkte des Gegners, mindestens zwei, höchstens 400. */
    public static final float PER_HEALTH = 0.5f;
    public static final float MAX_KILL = 400.0f;
    /** Bosse geben anderthalbmal so viel. */
    public static final float BOSS_BONUS = 1.5f;
    /** So viel lernen die Kameraden in der Nähe von einem Sieg mit – und so weit ist "in der Nähe". */
    public static final float SHARE = 0.25f;
    public static final double SHARE_RANGE = 12.0;
    /** An der Seite einer Legende lernt man ein Viertel schneller. */
    public static final float LEGEND_BONUS = 1.25f;
    public static final double LEGEND_RANGE = 12.0;
    /** Stufenwerte: Leben (Anteil der Grundlebenspunkte), Schaden und Rüstung je Stufe über der ersten. */
    public static final double HEALTH_PER_LEVEL = 0.03;
    public static final double DAMAGE_PER_LEVEL = 0.15;
    public static final double ARMOR_PER_LEVEL = 0.1;
    /** Zweiter Atem der Legende: unter einem Viertel der Lebenspunkte zurück auf die Hälfte, dann zwei Minuten Ruhe. */
    public static final int SECOND_WIND_COOLDOWN = 20 * 120;

    private Veterancy() {}

    // ------------------------------------------------------------------------------------------------ Stufen

    /**
     * Wie viel Kampferfahrung man für diese Stufe insgesamt braucht (Stufe 1: keine). Seit 0.61 liegen die Stufen weiter
     * auseinander (Stufe 20: rund 5.600 statt 2.300); die Erfahrung je Treffer und Sieg ist gleich geblieben.
     */
    public static int total(int level) {
        if (level <= 1) {
            return 0;
        }
        int clamped = Math.min(level, MAX_LEVEL);
        return (int) Math.round(18.0 * Math.pow(clamped - 1, 1.95) / 5.0) * 5;
    }

    /** Die alte Kurve (bis 0.60). */
    static int oldTotal(int level) {
        if (level <= 1) {
            return 0;
        }
        int clamped = Math.min(level, MAX_LEVEL);
        return (int) Math.round(10.0 * Math.pow(clamped - 1, 1.85) / 5.0) * 5;
    }

    /** Rechnet Kampferfahrung der alten Kurve auf die neue um: gleiche Stufe, gleicher Anteil des Wegs zur nächsten. */
    public static int migrate(int oldExperience) {
        int level = 1;
        while (level < MAX_LEVEL && oldExperience >= oldTotal(level + 1)) {
            level++;
        }
        if (level >= MAX_LEVEL) {
            return total(MAX_LEVEL);
        }
        int from = oldTotal(level);
        float progress = Mth.clamp((oldExperience - from) / (float) (oldTotal(level + 1) - from), 0.0f, 1.0f);
        return total(level) + (int) Math.floor(progress * (total(level + 1) - total(level)));
    }

    /** Die Stufe zu so viel Kampferfahrung. */
    public static int levelFor(int experience) {
        int level = 1;
        while (level < MAX_LEVEL && experience >= total(level + 1)) {
            level++;
        }
        return level;
    }

    /** Ab welcher Stufe ein Soldat diesen Rang bekommen kann. */
    public static int minLevel(int rank) {
        return RANK_LEVEL[Mth.clamp(rank, 0, RANK_LEVEL.length - 1)];
    }

    /** Wie weit er auf dem Weg zur nächsten Stufe ist (0 bis 1; auf der höchsten Stufe 1). */
    public static float progress(int experience) {
        int level = levelFor(experience);
        if (level >= MAX_LEVEL) {
            return 1.0f;
        }
        int from = total(level);
        return Mth.clamp((experience - from) / (float) (total(level + 1) - from), 0.0f, 1.0f);
    }

    // ------------------------------------------------------------------------------------------------ Erfahrung im Kampf

    /** Kampferfahrung für einen Sieg über {@code victim}. */
    public static float killExperience(LivingEntity victim) {
        float experience = Math.max(2.0f, victim.getMaxHealth() * PER_HEALTH);
        if (victim instanceof ElementalBoss || victim.typeHolder().is(Tags.EntityTypes.BOSSES)) {
            experience *= BOSS_BONUS;
        }
        return Math.min(MAX_KILL, experience);
    }

    /**
     * Lohnt dieser Kampf Erfahrung? Monster, Banditen und Krieger der Reiche, Soldaten fremder Herren, wer ihn oder seine
     * Freunde angreift, und ein Spieler, gegen den er gerade kämpft – aber nie Freunde, Haustiere oder Dorfbewohner.
     */
    public static boolean worthy(SoldierEntity soldier, LivingEntity target) {
        if (target == soldier || soldier.isAlliedTo(target)) {
            return false;
        }
        if (target instanceof Enemy) {
            return true;
        }
        if (target instanceof SoldierEntity other) {
            return !other.isAlliedTo(soldier);
        }
        if (target instanceof Mob mob && mob.getTarget() != null && (mob.getTarget() == soldier || soldier.isAlliedTo(mob.getTarget()))) {
            return true;
        }
        return target instanceof Player && soldier.getTarget() == target;
    }

    /** Der Soldat hinter einem Schaden: er selbst oder der Schütze seines Pfeils, Bolzens, Speers. */
    private static @Nullable SoldierEntity soldierOf(DamageSource source) {
        return source.getEntity() instanceof SoldierEntity soldier ? soldier : null;
    }

    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity target = event.getEntity();
        SoldierEntity soldier = soldierOf(event.getSource());
        if (soldier == null || !(target.level() instanceof ServerLevel) || event.getHealthDamage() <= 0.0f || !worthy(soldier, target)) {
            return;
        }
        award(soldier, Math.min(event.getHealthDamage(), target.getMaxHealth()) * PER_DAMAGE);
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        SoldierEntity soldier = soldierOf(event.getSource());
        if (soldier == null || !(victim.level() instanceof ServerLevel level) || !worthy(soldier, victim)) {
            return;
        }
        float experience = killExperience(victim);
        award(soldier, experience);
        for (SoldierEntity mate : level.getEntitiesOfClass(SoldierEntity.class, soldier.getBoundingBox().inflate(SHARE_RANGE),
                mate -> mate != soldier && mate.isAlive() && mate.isAlliedTo(soldier))) {
            award(mate, experience * SHARE);
        }
    }

    /** Gibt einem Soldaten Kampferfahrung – an der Seite einer Legende ein Viertel mehr. */
    public static void award(SoldierEntity soldier, float experience) {
        if (experience <= 0.0f || !(soldier.level() instanceof ServerLevel level)) {
            return;
        }
        if (soldier.getRank() < SoldierEntity.MAX_RANK && !level.getEntitiesOfClass(SoldierEntity.class,
                soldier.getBoundingBox().inflate(LEGEND_RANGE), legend -> legend != soldier && legend.isAlive()
                        && legend.getRank() == SoldierEntity.MAX_RANK && legend.isAlliedTo(soldier)).isEmpty()) {
            experience *= LEGEND_BONUS;
        }
        soldier.gainCombatExperience(experience);
    }

    // ------------------------------------------------------------------------------------------------ Aufstieg

    /**
     * Ein Soldat ist aufgestiegen: bunte Funken, ein heller Klang, sein Herr liest über der Leiste davon – und wenn er
     * jetzt den nächsten Rang erreichen kann, sagt es ihm eine Nachricht.
     */
    public static void celebrate(ServerLevel level, SoldierEntity soldier, int from, int to) {
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, soldier.getX(), soldier.getY() + 1.0, soldier.getZ(), 26, 0.35, 0.6, 0.35, 0.25);
        level.sendParticles(new DustParticleOptions(0xF2C14E, 1.0f), soldier.getX(), soldier.getY() + 2.25, soldier.getZ(), 10, 0.25, 0.08, 0.25,
                0.0);
        level.playSound(null, soldier.getX(), soldier.getY(), soldier.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 0.55f, 1.25f);
        if (!(soldier.getOwner() instanceof ServerPlayer owner)) {
            return;
        }
        owner.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier.level_up", soldier.getDisplayName(), to)
                .withStyle(ChatFormatting.GOLD));
        int rank = soldier.getRank();
        if (rank < SoldierEntity.MAX_RANK) {
            int needed = minLevel(rank + 1);
            if (from < needed && to >= needed) {
                owner.sendSystemMessage(Component.translatable("message.kingdomsatwar.soldier.rank_ready", soldier.getDisplayName(),
                        Component.translatable("soldier.kingdomsatwar.rank." + (rank + 1))).withStyle(ChatFormatting.YELLOW));
            }
        }
    }

    /**
     * Zweiter Atem einer Legende: Sinkt sie unter ein Viertel ihrer Lebenspunkte, rafft sie sich mit einem goldenen
     * Leuchten auf die Hälfte auf – höchstens alle zwei Minuten.
     *
     * @return ob sie sich aufgerafft hat
     */
    public static boolean secondWind(ServerLevel level, SoldierEntity legend, long lastWind) {
        if (legend.getRank() < SoldierEntity.MAX_RANK || !legend.isAlive() || legend.getHealth() >= legend.getMaxHealth() * 0.25f
                || lastWind >= 0 && legend.tickCount - lastWind < SECOND_WIND_COOLDOWN) {
            return false;
        }
        legend.setHealth(legend.getMaxHealth() * 0.5f);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, legend.getX(), legend.getY() + 1.0, legend.getZ(), 40, 0.4, 0.7, 0.4, 0.4);
        level.playSound(null, legend.getX(), legend.getY(), legend.getZ(), SoundEvents.TOTEM_USE, SoundSource.NEUTRAL, 0.5f, 1.15f);
        legend.speak("attack");
        if (legend.getOwner() instanceof ServerPlayer owner) {
            owner.sendOverlayMessage(Component.translatable("message.kingdomsatwar.soldier.second_wind", legend.getDisplayName())
                    .withStyle(ChatFormatting.GOLD));
        }
        return true;
    }

    /** Die Kameraden in Reichweite einer Legende (für Anzeigen und Tests). */
    public static List<SoldierEntity> inspired(ServerLevel level, SoldierEntity legend) {
        return level.getEntitiesOfClass(SoldierEntity.class, legend.getBoundingBox().inflate(LEGEND_RANGE),
                mate -> mate != legend && mate.isAlive() && mate.isAlliedTo(legend));
    }
}
