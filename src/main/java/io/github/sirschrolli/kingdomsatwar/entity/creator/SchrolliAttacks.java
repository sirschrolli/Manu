package io.github.sirschrolli.kingdomsatwar.entity.creator;

import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Künste Schrollis des Schöpfers – jede kündigt sich an (Haltung, Klang, leuchtende Warnkreise oder dünne Linien),
 * bevor sie trifft. Zeitpunkte in Ticks seit Beginn der Kunst.
 */
final class SchrolliAttacks {
    /** Die Zeit steht still: hier erstarren alle, hier läuft sie weiter. */
    static final int TIME_FREEZE = SchrolliEntity.TIME_FREEZE;
    static final int TIME_RESUME = SchrolliEntity.TIME_RESUME;
    /** So weit reichen die Strahlen des Sonnenrads. */
    static final double WHEEL_REACH = 36.0;

    private SchrolliAttacks() {}

    static void tick(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target) {
        int t = boss.attackTick;
        switch (boss.attack) {
            case LANCES -> lances(boss, level, target, t);
            case HALO -> halo(boss, level, target, t);
            case PILLARS -> pillars(boss, level, target, t);
            case STARFALL -> starfall(boss, level, target, t);
            case SMITE -> smite(boss, level, target, t);
            case SUNBEAM -> sunbeam(boss, level, target, t);
            case ORB -> orb(boss, level, target, t);
            case FEATHERS -> feathers(boss, level, target, t);
            case JUDGMENT -> judgment(boss, level, target, t);
            case SERAPHS -> seraphs(boss, level, target, t);
            case BLINK -> blink(boss, level, target, t);
            case GALE -> gale(boss, level, target, t);
            case SUNWHEEL -> sunwheel(boss, level, t);
            case CHOIR -> choir(boss, level, target, t);
            case WORD -> word(boss, level, target, t);
            case TIMESTOP -> timestop(boss, level, target, t);
            case PLANETS -> planets(boss, level, target, t);
            case SUPERNOVA -> supernova(boss, level, t);
            case GENESIS -> genesis(boss, level, t);
            case CONSTELLATION -> constellation(boss, level, target, t);
            case TWIN_HALOS -> twinHalos(boss, level, target, t);
            case APOCALYPSE -> apocalypse(boss, level, t);
            case EYE_BEAMS -> eyeBeams(boss, level, target, t);
            case COMET -> comet(boss, level, target, t);
            case WORLD_SPLIT -> worldSplit(boss, level, target, t);
            case RECREATION -> recreation(boss, level, t);
            case DESCEND -> descend(boss, level, target, t);
            case RISE -> rise(boss, level, target, t);
            case STRIKE -> strike(boss, level, target, t);
            case SLASHES -> slashes(boss, level, target, t);
            case DASH -> dash(boss, level, target, t);
            case WHIRL -> whirl(boss, level, target, t);
            case PLUNGE -> plunge(boss, level, target, t);
            case GUARD -> guard(boss, level, target, t);
            case COUNTER -> counter(boss, level, target, t);
            case WORLD_BLADE -> worldBlade(boss, level, target, t);
            default -> {
            }
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void keepNear(SchrolliEntity boss, @Nullable LivingEntity target, double radius, double height) {
        if (target != null) {
            boss.hover(target, radius, height);
            boss.face(target.position(), 10.0f);
        } else {
            boss.steer(boss.center().add(0.0, height + 4.0, 0.0), 0.05, 0.4);
        }
    }

    /** Wohin ein Wesen in {@code ticks} Ticks läuft (auf dem Boden). */
    private static Vec3 lead(SchrolliEntity boss, LivingEntity e, double ticks) {
        Vec3 v = e.getDeltaMovement();
        return boss.ground(e.position().add(v.x * ticks, 0.0, v.z * ticks));
    }

    /** Ein Warnkreis, aus dem nach {@code fuse} Ticks eine Säule aus Licht bricht. */
    static void warnPillar(SchrolliEntity boss, ServerLevel level, Vec3 at, float radius, int fuse, float damage, int color, float height) {
        DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, at.add(0.0, 0.06, 0.0), color, fuse, radius)
                .onExpire(fx -> pillar(boss, level, at, radius, height, damage, color)).spawn();
    }

    /** Eine Säule aus Licht: trifft alles in ihr und schleudert es hoch. */
    static void pillar(SchrolliEntity boss, ServerLevel level, Vec3 at, float radius, float height, float damage, int color) {
        DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, at, color, 18, radius).param(height).spawn();
        DivineFx.flash(level, at.add(0.0, 1.0, 0.0), color);
        level.sendParticles(ParticleTypes.END_ROD, at.x, at.y + 1.0, at.z, 18, radius * 0.4, 2.0, radius * 0.4, 0.2);
        DivineFx.sparks(level, at.add(0.0, 0.5, 0.0), 10, radius * 0.4, 0.1);
        // viele Säulen zugleich: nur eine erklingt (sonst wird es ein Getöse)
        if (boss.pillarSoundTick != level.getGameTime()) {
            boss.pillarSoundTick = level.getGameTime();
            DivineFx.sound(level, at, ModSounds.SCHROLLI_PILLAR.get(), 2.4f, 0.85f + level.getRandom().nextFloat() * 0.3f);
        }
        AABB box = new AABB(at.x - radius, at.y - 1.0, at.z - radius, at.x + radius, at.y + height, at.z + radius);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, boss::canStrike)) {
            if (DivineFx.flat(e.position(), at) <= radius + 0.3 && boss.strike(level, e, damage, true)) {
                e.push(0.0, 0.9, 0.0);
                e.needsSync = true;
            }
        }
    }

    /** Ein Ring rollt über den Boden (niedrig: springen; hoch: ducken). Jeden trifft er höchstens einmal. */
    static void ringHits(SchrolliEntity boss, ServerLevel level, SpellFxEntity fx, double groundY, float damage, boolean high, Set<UUID> hit) {
        float r = fx.ringRadius(0.0f);
        Vec3 c = fx.position();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r + 2.0, 6.0, r + 2.0), boss::canStrike)) {
            double d = DivineFx.flat(e.position(), c);
            if (Math.abs(d - r) > 0.9 || hit.contains(e.getUUID())) {
                continue;
            }
            if (!high && e.getY() > groundY + SpellFxEntity.SHOCKWAVE_HEIGHT) {
                continue;                       // drübergesprungen
            }
            if (high && (e.getBoundingBox().maxY < groundY + 1.62 || e.getY() > groundY + 2.4)) {
                continue;                       // drunter weggeduckt
            }
            hit.add(e.getUUID());
            if (boss.strike(level, e, damage, true)) {
                SchrolliEntity.knock(e, c, 0.7, 0.35);
            }
        }
    }

    /** Wer gerade einen Strahl berührt (Abstand des Körpers zur Strecke), mit Abklingzeit je Wesen. */
    private static void beamHits(SchrolliEntity boss, ServerLevel level, Vec3 a, Vec3 b, double width, float damage, int cooldown, boolean burn) {
        AABB box = new AABB(a, b).inflate(width + 1.5);
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, box, boss::canStrike)) {
            if (boss.hitCooldown.containsKey(e.getUUID())) {
                continue;
            }
            double d = Math.min(DivineFx.distanceToSegment(e.position().add(0.0, 0.3, 0.0), a, b),
                    Math.min(DivineFx.distanceToSegment(e.position().add(0.0, e.getBbHeight() * 0.5, 0.0), a, b),
                            DivineFx.distanceToSegment(e.position().add(0.0, e.getBbHeight() - 0.2, 0.0), a, b)));
            if (d <= width) {
                boss.hitCooldown.put(e.getUUID(), cooldown);
                if (boss.strike(level, e, damage, true) && burn) {
                    e.igniteForSeconds(3.0f);
                }
            }
        }
    }

    private static List<LivingEntity> sample(SchrolliEntity boss, ServerLevel level, int max) {
        List<LivingEntity> all = new ArrayList<>(boss.enemies(level));
        java.util.Collections.shuffle(all, new java.util.Random(boss.getRandom().nextLong()));
        return all.size() > max ? all.subList(0, max) : all;
    }

    // =====================================================================================
    // Die Künste des Schöpfers
    // =====================================================================================

    /** Lichtspeere: hinter ihm erscheinen im Bogen sieben (in der Wahren Gestalt elf) Speere und fliegen einer nach dem anderen. */
    private static void lances(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 12.0, 8.0);
        int count = boss.isTrueForm() ? 11 : 7;
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 3.0f, 1.3f);
        }
        int i = t - 2;
        if (target != null && i >= 0 && i < count) {
            double a = Math.toRadians(-78.0 + 156.0 * i / (count - 1));
            Vec3 at = boss.local(Math.sin(a) * 4.4, 6.0 + Math.cos(a) * 1.8, -1.4);
            DivineShotEntity.spear(level, boss, at, target, 20 + i * 3, 14.0f * boss.power(), boss.isTrueForm() ? 2.4 : 2.0);
            DivineFx.sparks(level, at, 6, 0.2, 0.05);
            boss.sound(level, ModSounds.SCHROLLI_SPEAR.get(), 1.4f, 0.8f + i * 0.07f);
        }
    }

    /** Heiligenschein-Klinge: er wirft seinen Heiligenschein; er saust zum Ziel und kehrt zurück (in der Wahren Gestalt zwei). */
    private static void halo(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 13.0, 8.0);
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 2.5f, 1.5f);
        }
        if (t == 16 && target != null) {
            boss.setFlag(SchrolliEntity.FLAG_HALO, true);
            Vec3 from = boss.local(0.0, 5.6, -0.7);
            int discs = boss.isTrueForm() ? 2 : 1;
            for (int k = 0; k < discs; k++) {
                double spread = discs == 1 ? 0.0 : k == 0 ? -24.0 : 24.0;
                Vec3 to = target.position().add(0.0, 1.0, 0.0).subtract(from);
                double yaw = Math.toDegrees(Math.atan2(to.z, to.x)) + spread;
                double flat = Math.sqrt(to.x * to.x + to.z * to.z);
                Vec3 dir = new Vec3(Math.cos(Math.toRadians(yaw)) * flat, to.y, Math.sin(Math.toRadians(yaw)) * flat).normalize();
                double dist = Math.min(42.0, to.length() + 9.0);
                DivineShotEntity.halo(level, boss, from, from.add(dir.scale(dist)), 1.25, 14.0f * boss.power());
            }
            boss.sound(level, ModSounds.SCHROLLI_HALO.get(), 3.0f, 1.0f);
        }
    }

    /** Säulen des Lichts: Warnkreise unter allen Gegnern und um sie herum – dann brechen Säulen aus Licht hervor. Zwei, drei Wellen. */
    private static void pillars(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 14.0, 10.0);
        boolean wave = t == 6 || t == 40 || boss.isTrueForm() && t == 70;
        if (!wave) {
            return;
        }
        boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 2.5f, 0.9f);
        float radius = boss.isTrueForm() ? 3.2f : 2.6f;
        int placed = 0;
        for (LivingEntity e : sample(boss, level, 6)) {
            warnPillar(boss, level, lead(boss, e, 8), radius, 28, 16.0f, DivineFx.HOLY, 26.0f);
            for (int k = 0; k < 2; k++) {
                double a = boss.getRandom().nextDouble() * Math.PI * 2.0;
                double r = 3.5 + boss.getRandom().nextDouble() * 3.5;
                warnPillar(boss, level, boss.ground(e.position().add(Math.cos(a) * r, 0.0, Math.sin(a) * r)), radius, 28, 16.0f, DivineFx.GOLD, 26.0f);
            }
            placed++;
        }
        if (placed == 0 && target != null) {
            warnPillar(boss, level, boss.ground(target.position()), radius, 28, 16.0f, DivineFx.HOLY, 26.0f);
        }
    }

    /** Sternenregen: Arme empor – Sterne stürzen vom Himmel auf leuchtende Kreise, über den ganzen Hof. */
    private static void starfall(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t <= 14) {
            keepNear(boss, target, 14.0, 10.0);
        } else {
            boss.setDeltaMovement(boss.getDeltaMovement().scale(0.8));
        }
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 3.0f, 1.1f);
        }
        if (t >= 20 && t <= 94 && t % (boss.isTrueForm() ? 2 : 3) == 0) {
            List<LivingEntity> enemies = boss.enemies(level);
            Vec3 mark;
            if (!enemies.isEmpty() && boss.getRandom().nextFloat() < 0.6f) {
                LivingEntity e = enemies.get(boss.getRandom().nextInt(enemies.size()));
                Vec3 lead = e.getDeltaMovement().scale(14.0);
                mark = boss.ground(e.position().add(lead.x + (boss.getRandom().nextDouble() - 0.5) * 6.0, 0.0,
                        lead.z + (boss.getRandom().nextDouble() - 0.5) * 6.0));
            } else {
                double a = boss.getRandom().nextDouble() * Math.PI * 2.0;
                double r = Math.sqrt(boss.getRandom().nextDouble()) * 34.0;
                mark = boss.ground(boss.center().add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
            }
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, mark.add(0.0, 0.06, 0.0), DivineFx.GOLD, 26, 2.4f).spawn();
            DivineShotEntity.star(level, boss, mark, 30.0, 1.15, 12.0f * boss.power(), 2.6);
        }
    }

    /** Zepterschlag: er stürzt auf sein Ziel herab und schlägt das Zepter in den Boden – ein Ring rollt los (springen!). */
    private static void smite(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t < 10) {
            boss.steer(boss.position().add(0.0, 3.0, 0.0), 0.08, 0.5);
            if (target != null) {
                boss.face(target.position(), 15.0f);
            }
            if (t == 1) {
                boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 2.5f, 0.8f);
            }
            return;
        }
        if (t == 10) {
            boss.mark = target != null ? lead(boss, target, 8) : boss.ground(boss.position());
            boss.aim = boss.position();
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.mark.add(0.0, 0.06, 0.0), DivineFx.AMBER, 24, 4.5f).spawn();
        }
        if (t > 10 && t <= 34) {
            float k = (t - 10) / 24.0f;
            Vec3 p = boss.aim.lerp(boss.mark.add(0.0, 0.2, 0.0), k * k);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            boss.face(boss.mark, 20.0f);
            if (t % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 3.0, p.z, 3, 0.6, 1.0, 0.6, 0.05);
            }
        }
        if (t == 34) {
            Vec3 m = boss.mark;
            boss.sound(level, ModSounds.SCHROLLI_SMITE.get(), 5.0f, 0.9f);
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 4.0f, 0.8f);
            DivineFx.flash(level, m.add(0.0, 1.0, 0.0), DivineFx.WHITE);
            DivineFx.sparks(level, m.add(0.0, 0.4, 0.0), 60, 1.6, 0.25);
            level.sendParticles(ParticleTypes.EXPLOSION, m.x, m.y + 0.5, m.z, 3, 1.2, 0.2, 1.2, 0.0);
            for (LivingEntity e : boss.enemiesNear(level, m.add(0.0, 1.0, 0.0), 4.8)) {
                if (boss.strike(level, e, 22.0f, false)) {
                    SchrolliEntity.knock(e, m, 1.4, 0.6);
                }
            }
            Set<UUID> hit = new HashSet<>();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, m.add(0.0, 0.1, 0.0), DivineFx.GOLD, 34, 2.0f).param(0.55f)
                    .onTick(fx -> ringHits(boss, level, fx, m.y, 10.0f, false, hit)).spawn();
            for (int k = 0; k < 4; k++) {
                Vec3 end = m.add(DivineFx.dir(boss.getYRot() + 45.0 + k * 90.0).scale(9.0));
                DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, m.add(0.0, 0.05, 0.0), DivineFx.GOLD, 40, 0.9f).param(0.0f).end(end).spawn();
            }
        }
        if (t > 40) {
            boss.steer(boss.mark.add(0.0, 9.0, 0.0), 0.06, 0.5);
        }
    }

    /** Strahl der Schöpfung: aus dem Zepter fegt ein Strahl aus Licht über den Boden (in der Wahren Gestalt zwei, gegenläufig). */
    private static void sunbeam(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (target != null) {
            boss.hover(target, 14.0, 10.0);
        }
        boss.setDeltaMovement(boss.getDeltaMovement().scale(0.6));
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 3.0f, 0.7f);
            boss.aim = target != null ? target.position() : boss.center();
            boss.sweepDir = boss.getRandom().nextBoolean() ? 1.0 : -1.0;
            boss.sweepBase = DivineFx.yaw(boss.position(), boss.aim);
            boss.sweep = boss.sweepBase - 55.0 * boss.sweepDir;
        }
        if (t < 30) {
            boss.face(boss.aim, 10.0f);
            if (t % 2 == 0) {
                DivineFx.gather(level, boss.scepterTip(), 3.0, 3);
            }
            return;
        }
        double base = DivineFx.flat(boss.position(), boss.aim);
        double reach = Mth.clamp(base, 8.0, 34.0);
        Vec3 g = boss.ground(boss.position());
        if (t == 30) {
            boss.sound(level, ModSounds.SCHROLLI_BEAM.get(), 4.0f, 1.0f);
            boss.beams.add(DivineFx.fx(level, SpellFxEntity.Kind.BEAM, boss.scepterTip(), DivineFx.HOLY, 84, 1.3f).end(g).spawn());
            if (boss.isTrueForm()) {
                boss.beams.add(DivineFx.fx(level, SpellFxEntity.Kind.BEAM, boss.orbHand(), DivineFx.COSMIC, 84, 1.1f).end(g).spawn());
            }
        }
        if (t >= 30 && t < 112) {
            boss.sweep += boss.sweepDir * 110.0 / 82.0;
            for (int k = 0; k < boss.beams.size(); k++) {
                SpellFxEntity fx = boss.beams.get(k);
                // der zweite Strahl läuft spiegelbildlich (gegenläufig) über das Ziel
                double angle = k == 0 ? boss.sweep : 2.0 * boss.sweepBase - boss.sweep;
                Vec3 from = k == 0 ? boss.scepterTip() : boss.orbHand();
                Vec3 point = boss.ground(g.add(DivineFx.dir(angle).scale(reach)));
                fx.setPos(from.x, from.y, from.z);
                fx.moveEnd(point.add(0.0, 0.1, 0.0));
                if (t % 4 == 0) {
                    beamHits(boss, level, from, point, 1.6, 7.0f, 4, true);
                }
                if (t % 2 == 0) {
                    level.sendParticles(ParticleTypes.SMALL_FLAME, point.x, point.y + 0.1, point.z, 3, 0.5, 0.05, 0.5, 0.02);
                    level.sendParticles(ParticleTypes.END_ROD, point.x, point.y + 0.3, point.z, 2, 0.4, 0.3, 0.4, 0.06);
                }
            }
            boss.turnTo((float) boss.sweep, 30.0f);
            if ((t - 30) % 20 == 0) {
                boss.sound(level, ModSounds.SCHROLLI_BEAM.get(), 3.0f, 1.0f);
            }
        }
        if (t == 112) {
            boss.clearArtState();
        }
    }

    /** Die Weltkugel: er schleudert sie; wo sie anhält, wird sie zum Sog – und zerbirst in Licht. */
    private static void orb(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 12.0, 8.0);
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 2.5f, 1.0f);
        }
        if (t == 18 && target != null) {
            boss.setFlag(SchrolliEntity.FLAG_ORB, true);
            Vec3 mark = lead(boss, target, 12).add(0.0, 1.8, 0.0);
            DivineShotEntity.orb(level, boss, boss.orbHand(), mark, 20.0f * boss.power());
            boss.sound(level, ModSounds.SCHROLLI_ORB.get(), 3.0f, 1.2f);
        }
    }

    /** Federsturm: die Schwingen weit – drei Wellen goldener Federn fächern auf die Gegner zu. */
    private static void feathers(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 12.0, 7.0);
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 3.0f, 0.8f);
        }
        boolean wave = t == 18 || t == 32 || t == 46 || boss.isTrueForm() && t == 60;
        if (!wave || target == null) {
            return;
        }
        int count = boss.isTrueForm() ? 20 : 14;
        Vec3 aimAt = target.position().add(0.0, 1.0, 0.0);
        for (int k = 0; k < count; k++) {
            double side = (k % 2 == 0 ? 1.0 : -1.0) * (1.6 + boss.getRandom().nextDouble() * 4.0);
            Vec3 from = boss.local(side, 4.4 + boss.getRandom().nextDouble() * 1.6, -0.4);
            Vec3 to = aimAt.subtract(from);
            double yaw = Math.atan2(to.z, to.x) + Math.toRadians(-50.0 + 100.0 * k / (count - 1) + (boss.getRandom().nextDouble() - 0.5) * 6.0);
            double flat = Math.sqrt(to.x * to.x + to.z * to.z);
            double pitch = to.y / Math.max(1.0, flat) + (boss.getRandom().nextDouble() - 0.5) * 0.12;
            Vec3 dir = new Vec3(Math.cos(yaw), pitch, Math.sin(yaw)).normalize();
            DivineShotEntity.feather(level, boss, from, dir, 1.3, 6.0f * boss.power());
        }
        boss.sound(level, ModSounds.SCHROLLI_FEATHERS.get(), 3.0f, 0.9f + boss.getRandom().nextFloat() * 0.2f);
    }

    /**
     * Göttliches Urteil: ein Kreis aus Licht heftet sich an ein Ziel; wenn er sich gefüllt hat, fährt das Urteil herab –
     * sein Schaden wird auf alle verteilt, die im Kreis stehen (allein trägt man ihn ganz).
     */
    private static void judgment(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 14.0, 10.0);
        if (t == 8) {
            if (target == null) {
                boss.endArt();
                return;
            }
            boss.marked = target.getUUID();
            boss.markFx = DivineFx.fx(level, SpellFxEntity.Kind.CAST_CIRCLE, target.position(), DivineFx.GOLD, 60, 3.2f).param(0.0f).anchor(target)
                    .spawn();
            boss.sound(level, ModSounds.SCHROLLI_JUDGMENT.get(), 4.0f, 0.7f);
            Component name = target.getDisplayName();
            for (ServerPlayer player : boss.challengers(level)) {
                player.sendOverlayMessage(player == target
                        ? Component.translatable("entity.kingdomsatwar.schrolli.judged_you").withColor(0xFFD24A)
                        : Component.translatable("entity.kingdomsatwar.schrolli.judged", name).withColor(0xFFD24A));
            }
        }
        if (t > 8 && t < 64 && boss.markFx != null) {
            boss.markFx.setParam((t - 8) / 56.0f);
            if (t % 3 == 0 && level.getEntity(boss.marked) instanceof LivingEntity m) {
                DivineFx.ring(level, DivineFx.GOLD_DUST, m.position().add(0.0, m.getBbHeight() + 0.6, 0.0), 0.6, 8);
            }
        }
        if (t == 64) {
            LivingEntity m = boss.marked != null && level.getEntity(boss.marked) instanceof LivingEntity e ? e : null;
            boss.mark = m != null ? m.position() : boss.mark;
            if (boss.markFx != null) {
                boss.markFx.discard();
                boss.markFx = null;
            }
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.mark.add(0.0, 0.06, 0.0), DivineFx.WHITE, 7, 3.4f).spawn();
        }
        if (t == 70) {
            Vec3 m = boss.mark;
            List<LivingEntity> inside = new ArrayList<>();
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(m, m).inflate(3.6, 6.0, 3.6), boss::canStrike)) {
                if (DivineFx.flat(e.position(), m) <= 3.5) {
                    inside.add(e);
                }
            }
            float share = 36.0f / Math.max(1, inside.size());
            for (LivingEntity e : inside) {
                boss.strike(level, e, share, true);
                e.push(0.0, 0.6, 0.0);
                e.needsSync = true;
            }
            DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, m, DivineFx.WHITE, 24, 3.4f).param(60.0f).spawn();
            DivineFx.flash(level, m.add(0.0, 1.5, 0.0), DivineFx.WHITE);
            DivineFx.sparks(level, m.add(0.0, 1.0, 0.0), 80, 2.0, 0.3);
            DivineFx.sound(level, m, ModSounds.SCHROLLI_JUDGMENT.get(), 5.0f, 1.0f);
            DivineFx.sound(level, m, ModSounds.SCHROLLI_PILLAR.get(), 5.0f, 0.6f);
        }
    }

    /** Seraphim: aus Licht formen sich Seraphlinge, die ihm beistehen. */
    private static void seraphs(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 13.0, 9.0);
        int count = Math.min(boss.isTrueForm() ? 5 : 3, 8 - boss.minions.size());
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHOIR.get(), 3.0f, 1.2f);
            boss.points.clear();
            for (int k = 0; k < count; k++) {
                double a = Math.toRadians(boss.getYRot() + 90.0 + k * 360.0 / Math.max(1, count));
                boss.points.add(boss.position().add(Math.cos(a) * 5.5, 2.0 + k % 2, Math.sin(a) * 5.5));
            }
        }
        if (t < 30 && t % 2 == 0) {
            for (Vec3 p : boss.points) {
                DivineFx.gather(level, p, 2.0, 2);
            }
        }
        if (t == 30) {
            for (Vec3 p : boss.points) {
                SeraphlingEntity s = SeraphlingEntity.summon(level, boss, p);
                if (s != null) {
                    boss.minions.add(s.getUUID());
                }
                DivineFx.flash(level, p, DivineFx.HOLY);
            }
            boss.points.clear();
        }
    }

    /** Lichtsprung: er zerfällt in Licht, erscheint hinter seinem Ziel und fegt mit dem Zepter. */
    private static void blink(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t < 4 && target != null) {
            boss.face(target.position(), 20.0f);
        }
        if (t == 4) {
            boss.setFlag(SchrolliEntity.FLAG_HIDDEN, true);
            DivineFx.flash(level, boss.position().add(0.0, 3.0, 0.0), DivineFx.WHITE);
            DivineFx.sparks(level, boss.position().add(0.0, 3.0, 0.0), 60, 1.5, 0.2);
            boss.sound(level, ModSounds.SCHROLLI_BLINK.get(), 3.0f, 1.0f);
        }
        if (t > 4 && t < 16) {
            boss.setDeltaMovement(Vec3.ZERO);
        }
        if (t == 16) {
            Vec3 behind;
            if (target != null) {
                Vec3 look = target.getLookAngle().multiply(1.0, 0.0, 1.0);
                look = look.lengthSqr() < 1.0e-4 ? new Vec3(0.0, 0.0, 1.0) : look.normalize();
                behind = boss.ground(target.position().subtract(look.scale(3.8))).add(0.0, 0.3, 0.0);
            } else {
                behind = boss.position();
            }
            boss.teleportTo(behind.x, behind.y, behind.z);
            boss.setFlag(SchrolliEntity.FLAG_HIDDEN, false);
            if (target != null) {
                boss.turnTo((float) DivineFx.yaw(behind, target.position()), 360.0f);
            }
            DivineFx.flash(level, behind.add(0.0, 3.0, 0.0), DivineFx.WHITE);
            boss.sound(level, ModSounds.SCHROLLI_BLINK.get(), 3.0f, 1.4f);
        }
        if (t == 20) {
            Vec3 at = boss.position();
            Vec3 fwd = boss.forward();
            DivineFx.fx(level, SpellFxEntity.Kind.CRESCENT, at.add(0.0, 0.1, 0.0), DivineFx.HOLY, 12, 8.0f).param(0.8f).facing(boss.getYRot()).spawn();
            for (LivingEntity e : boss.enemiesNear(level, at.add(0.0, 1.0, 0.0), 6.0)) {
                Vec3 to = e.position().subtract(at).multiply(1.0, 0.0, 1.0);
                if (to.lengthSqr() < 1.0e-3 || to.normalize().dot(fwd) > 0.15) {
                    if (boss.strike(level, e, 18.0f, false)) {
                        SchrolliEntity.knock(e, at, 1.3, 0.45);
                    }
                }
            }
            boss.sound(level, ModSounds.SCHROLLI_SMITE.get(), 3.0f, 1.4f);
        }
        if (t > 24) {
            boss.steer(boss.position().add(0.0, 6.0, 0.0), 0.06, 0.45);
        }
    }

    /** Schwingensturm: alle sechs Schwingen schlagen zugleich – ein Sturm wirft alles fort (Vorsicht am Rand!). */
    private static void gale(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 9.0, 5.0);
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 3.0f, 0.6f);
        }
        if (t < 30 && t % 3 == 0) {
            DivineFx.gather(level, boss.position().add(0.0, 4.0, 0.0), 7.0, 4);
        }
        if (t == 30) {
            Vec3 c = boss.position();
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 6.0f, 0.5f);
            boss.sound(level, ModSounds.SCHROLLI_BLAST.get(), 4.0f, 0.5f);
            for (LivingEntity e : boss.enemiesNear(level, c, 22.0)) {
                double d = e.position().distanceTo(c);
                double f = 1.0 - d / 22.0;
                boss.strike(level, e, 6.0f, true);
                SchrolliEntity.knock(e, c, 0.6 + 2.0 * f, 0.55);
            }
            Vec3 g = boss.ground(c);
            for (int k = 1; k <= 4; k++) {
                DivineFx.ring(level, ParticleTypes.CLOUD, g.add(0.0, 0.4 * k, 0.0), 3.0 * k, 12 * k);
            }
            level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.DIVINE_FEATHER.get(), c.x, c.y + 4.0, c.z, 60, 4.0, 2.0, 4.0, 0.3);
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, 0.1, 0.0), DivineFx.WHITE, 20, 2.0f).param(1.0f).spawn();
        }
    }

    /** Sonnenrad: über der Mitte drehen sich sechs (acht) Strahlen dicht über dem Boden – springen oder ausweichen. */
    private static void sunwheel(SchrolliEntity boss, ServerLevel level, int t) {
        Vec3 hub = boss.ground(boss.center());
        int spokes = boss.isTrueForm() ? 8 : 6;
        boss.steer(hub.add(0.0, 10.0, 0.0), 0.06, 0.5);
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 4.0f, 0.6f);
            boss.sweep = boss.getRandom().nextDouble() * 360.0;
            boss.sweepDir = boss.getRandom().nextBoolean() ? 1.0 : -1.0;
        }
        if (t == 12) {
            for (int k = 0; k < spokes; k++) {
                Vec3 end = hub.add(DivineFx.dir(boss.sweep + k * 360.0 / spokes).scale(WHEEL_REACH)).add(0.0, 0.6, 0.0);
                DivineFx.fx(level, SpellFxEntity.Kind.BEAM, hub.add(0.0, 0.6, 0.0), DivineFx.GOLD, 18, 0.15f).end(end).spawn();
            }
        }
        if (t == 30) {
            boss.sound(level, ModSounds.SCHROLLI_BEAM.get(), 5.0f, 0.8f);
            for (int k = 0; k < spokes; k++) {
                Vec3 end = hub.add(DivineFx.dir(boss.sweep + k * 360.0 / spokes).scale(WHEEL_REACH)).add(0.0, 0.6, 0.0);
                boss.beams.add(DivineFx.fx(level, SpellFxEntity.Kind.BEAM, hub.add(0.0, 0.6, 0.0), k % 2 == 0 ? DivineFx.HOLY : DivineFx.GOLD, 124, 0.8f)
                        .end(end).spawn());
            }
        }
        if (t > 30 && t < 152) {
            if (boss.isTrueForm() && t == 91) {
                boss.sweepDir = -boss.sweepDir;
            }
            boss.sweep += boss.sweepDir * (boss.isTrueForm() ? 1.7 : 1.2);
            for (int k = 0; k < boss.beams.size(); k++) {
                Vec3 end = hub.add(DivineFx.dir(boss.sweep + k * 360.0 / spokes).scale(WHEEL_REACH)).add(0.0, 0.6, 0.0);
                boss.beams.get(k).moveEnd(end);
                Vec3 a = hub.add(0.0, 0.6, 0.0);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(hub, hub).inflate(WHEEL_REACH + 1.0, 3.0, WHEEL_REACH + 1.0),
                        boss::canStrike)) {
                    if (e.getY() > hub.y + 1.0 || boss.hitCooldown.containsKey(e.getUUID())) {
                        continue;                       // drübergesprungen
                    }
                    if (DivineFx.distanceToSegment(new Vec3(e.getX(), a.y, e.getZ()), a, end) < 0.9) {
                        boss.hitCooldown.put(e.getUUID(), 10);
                        if (boss.strike(level, e, 9.0f, true)) {
                            e.push(0.0, 0.6, 0.0);
                            e.needsSync = true;
                        }
                    }
                }
            }
            boss.turnTo((float) boss.sweep, 360.0f);
            if ((t - 30) % 30 == 0) {
                boss.sound(level, ModSounds.SCHROLLI_BEAM.get(), 3.0f, 0.8f);
            }
        }
        if (t == 152) {
            boss.clearArtState();
        }
    }

    /** Engelschor: Ringe aus Klang rollen von ihm aus – goldene dicht über dem Boden (springen!), türkise in Kopfhöhe (ducken!). */
    private static void choir(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 10.0, 6.0);
        int[] times = boss.isTrueForm() ? new int[] {20, 34, 48, 62, 76, 90} : new int[] {20, 38, 56, 74};
        for (int i = 0; i < times.length; i++) {
            if (t == times[i]) {
                boolean high = i % 2 == 1;
                Vec3 g = boss.ground(boss.position());
                Set<UUID> hit = new HashSet<>();
                DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, high ? 1.55 : 0.1, 0.0), high ? DivineFx.CYAN : DivineFx.GOLD, 52, 2.0f)
                        .param(0.6f).onTick(fx -> ringHits(boss, level, fx, g.y, 12.0f, high, hit)).spawn();
                boss.sound(level, ModSounds.SCHROLLI_CHOIR.get(), 4.0f, high ? 1.35f : 0.8f);
            }
        }
    }

    /**
     * „Es werde Licht!“: er sammelt alles Licht in sich – wer im Augenblick des Blitzes zu ihm hinsieht, ist geblendet und
     * verbrannt. Wegsehen schützt.
     */
    private static void word(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        boss.steer(boss.center().add(0.0, 12.0, 0.0), 0.05, 0.45);
        if (target != null) {
            boss.face(target.position(), 8.0f);
        }
        if (t == 1) {
            boss.setFlag(SchrolliEntity.FLAG_GLARE, true);
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 4.0f, 0.5f);
        }
        if (t < 56 && t % 2 == 0) {
            DivineFx.gather(level, boss.position().add(0.0, 4.0, 0.0), 12.0, 8);
        }
        if (t == 26) {
            boss.say(level, "light");
            boss.announce(level, Component.translatable("entity.kingdomsatwar.schrolli.line.light").withColor(0xFFF2C4).withStyle(ChatFormatting.BOLD),
                    Component.translatable("entity.kingdomsatwar.schrolli.look_away").withColor(0xFFD24A), 30);
        }
        if (t == SchrolliEntity.WORD_FLASH) {
            Vec3 eyes = boss.eyes();
            for (int k = 0; k < 4; k++) {
                DivineFx.flash(level, eyes, DivineFx.WHITE);
            }
            level.sendParticles(ParticleTypes.END_ROD, eyes.x, eyes.y, eyes.z, 300, 3.0, 3.0, 3.0, 0.8);
            boss.sound(level, ModSounds.SCHROLLI_WORD.get(), 6.0f, 1.0f);
            for (LivingEntity e : boss.enemies(level)) {
                if (e instanceof Player p) {
                    Vec3 to = eyes.subtract(p.getEyePosition()).normalize();
                    boolean sees = p.getViewVector(1.0f).dot(to) > 0.45 && p.hasLineOfSight(boss);
                    if (sees) {
                        boss.strike(level, p, 14.0f, true);
                        p.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 0), boss);
                        p.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), boss);
                        if (p instanceof ServerPlayer sp) {
                            sp.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.schrolli.blinded").withColor(0xFFF2C4));
                        }
                    } else if (p instanceof ServerPlayer sp) {
                        sp.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.schrolli.looked_away").withColor(0xA8E8A8));
                    }
                } else {
                    boss.strike(level, e, 8.0f, true);
                    e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 60, 0), boss);
                }
            }
        }
        if (t == 62) {
            boss.setFlag(SchrolliEntity.FLAG_GLARE, false);
        }
    }

    /**
     * Die Zeit steht still: alle Gegner erstarren; er schreitet durch die stillstehende Zeit und stellt Speere um jeden –
     * wenn die Zeit weiterläuft, fliegen sie (in schneller Folge: weglaufen!).
     */
    private static void timestop(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 12.0, 8.0);
        if (t == 1) {
            boss.say(level, "time");
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 3.0f, 0.5f);
        }
        if (t == TIME_FREEZE) {
            boss.sound(level, ModSounds.SCHROLLI_TIMESTOP.get(), 6.0f, 1.0f);
            boss.ids.clear();
            for (LivingEntity e : boss.enemies(level)) {
                TimeFreeze.freeze(e, TIME_RESUME - TIME_FREEZE);
                boss.ids.add(e.getId());
            }
            boss.tell(level, Component.translatable("entity.kingdomsatwar.schrolli.time_stands").withColor(0xC8C8D8));
        }
        if (t > TIME_FREEZE && t < TIME_RESUME) {
            if ((t - TIME_FREEZE) % 10 == 0) {
                boss.sound(level, ModSounds.SCHROLLI_TICK.get(), 3.0f, 1.0f);
            }
            int step = t - TIME_FREEZE - 6;
            int per = boss.isTrueForm() ? 8 : 6;
            if (step >= 0 && step % 4 == 0 && step / 4 < per) {
                int k = step / 4;
                int spawned = 0;
                for (int id : boss.ids) {
                    if (spawned++ > 6) {
                        break;
                    }
                    if (level.getEntity(id) instanceof LivingEntity e && e.isAlive()) {
                        double a = Math.toRadians(k * 360.0 / per + e.getId() * 37.0);
                        Vec3 at = e.position().add(Math.cos(a) * 5.5, 1.6 + (k % 3) * 0.8, Math.sin(a) * 5.5);
                        DivineShotEntity.spear(level, boss, at, e, TIME_RESUME + 6 + k * 3 - t, 10.0f * boss.power(), 1.9);
                        DivineFx.sparks(level, at, 5, 0.2, 0.03);
                    }
                }
                boss.sound(level, ModSounds.SCHROLLI_SPEAR.get(), 1.5f, 0.6f + k * 0.08f);
            }
        }
        if (t == TIME_RESUME) {
            for (int id : boss.ids) {
                if (level.getEntity(id) instanceof LivingEntity e) {
                    TimeFreeze.unfreeze(e);
                }
            }
            boss.sound(level, ModSounds.SCHROLLI_TIME_RESUME.get(), 6.0f, 1.0f);
            boss.tell(level, Component.translatable("entity.kingdomsatwar.schrolli.time_runs").withColor(0xFFD24A));
        }
    }

    // =====================================================================================
    // Die Künste der Wahren Gestalt
    // =====================================================================================

    /** Weltenwurf: drei Welten entstehen über ihm – Glut, Eis, Erde oder Leere – und er schleudert sie auf seine Gegner. */
    private static void planets(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 14.0, 10.0);
        if (t == 6 || t == 18 || t == 30) {
            int k = (t - 6) / 12;
            int[] variants = {DivineShotEntity.PLANET_LAVA, DivineShotEntity.PLANET_ICE, DivineShotEntity.PLANET_EARTH, DivineShotEntity.PLANET_VOID};
            int variant = variants[(k + boss.getRandom().nextInt(4)) % 4];
            boss.held.add(DivineShotEntity.planet(level, boss, variant, k));
            boss.sound(level, ModSounds.SCHROLLI_PLANET.get(), 3.0f, 0.8f + k * 0.15f);
        }
        if (t == 46 || t == 66 || t == 86) {
            int k = (t - 46) / 20;
            List<LivingEntity> enemies = boss.enemies(level);
            LivingEntity aimAt = enemies.isEmpty() ? target : enemies.get(k % enemies.size());
            if (k < boss.held.size() && aimAt != null) {
                DivineShotEntity planet = boss.held.get(k);
                if (!planet.isRemoved()) {
                    planet.hurl(lead(boss, aimAt, 16));
                    boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 3.0f, 0.7f);
                }
            }
        }
        if (t == 100) {
            boss.held.clear();
        }
    }

    /**
     * Supernova: er steigt über die Mitte, eine Sonne wächst über ihm – wer nicht hinter einer Säule Schutz sucht oder weit
     * genug flieht, verbrennt im Licht der ersten Sonne.
     */
    private static void supernova(SchrolliEntity boss, ServerLevel level, int t) {
        Vec3 c = boss.center();
        Vec3 top = c.add(0.0, 22.0, 0.0);
        boss.steer(top, 0.06, 0.6);
        boss.turnTo(boss.getYRot() + 2.0f, 360.0f);
        if (t == 1) {
            boss.say(level, "sun");
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 5.0f, 0.5f);
            boss.announce(level, Component.translatable("entity.kingdomsatwar.schrolli.nova").withColor(0xFFB040).withStyle(ChatFormatting.BOLD),
                    Component.translatable("entity.kingdomsatwar.schrolli.nova.hint").withColor(0xFFF2C4), 60);
        }
        if (t == 10) {
            boss.sun = DivineShotEntity.sun(level, boss, boss.local(0.0, 9.0, 0.0), 0.6f);
        }
        if (t > 10 && t < 140 && boss.sun != null) {
            float k = (t - 10) / 130.0f;
            boss.sun.setVisualScale(0.6f + 5.4f * k * k);
            Vec3 at = boss.local(0.0, 9.0 + 3.0 * k, 0.0);
            boss.sun.setPos(at.x, at.y, at.z);
            if (t % 2 == 0) {
                DivineFx.gather(level, at, 16.0, 10);
            }
            if (t % 20 == 0) {
                DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.ground(c).add(0.0, 0.06, 0.0), DivineFx.AMBER, 20, 40.0f).spawn();
                boss.sound(level, ModSounds.SCHROLLI_TICK.get(), 3.0f, 0.6f + k);
            }
            if (t == 76) {
                boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 5.0f, 0.9f);
            }
        }
        if (t == SchrolliEntity.NOVA_BLAST) {
            Vec3 sunPos = boss.sun != null ? boss.sun.position() : boss.local(0.0, 12.0, 0.0);
            boss.sound(level, ModSounds.SCHROLLI_SUPERNOVA.get(), 8.0f, 1.0f);
            for (int k = 0; k < 6; k++) {
                DivineFx.flash(level, sunPos.add((boss.getRandom().nextDouble() - 0.5) * 6.0, (boss.getRandom().nextDouble() - 0.5) * 6.0,
                        (boss.getRandom().nextDouble() - 0.5) * 6.0), k % 2 == 0 ? DivineFx.WHITE : DivineFx.AMBER);
            }
            DivineFx.sparks(level, sunPos, 500, 8.0, 0.8);
            level.sendParticles(ParticleTypes.END_ROD, sunPos.x, sunPos.y, sunPos.z, 400, 6.0, 6.0, 6.0, 1.2);
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, boss.ground(c).add(0.0, 0.1, 0.0), DivineFx.WHITE, 60, 4.0f).param(1.8f).spawn();
            for (LivingEntity e : boss.enemies(level)) {
                Vec3 eye = e.getEyePosition();
                boolean covered = level.clip(new ClipContext(sunPos, eye, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, boss)).getType()
                        == HitResult.Type.BLOCK;
                if (covered) {
                    if (e instanceof ServerPlayer sp) {
                        sp.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.schrolli.nova.covered").withColor(0xA8E8A8));
                    }
                    continue;
                }
                double d = eye.distanceTo(sunPos);
                double f = d < 40.0 ? 1.0 : Math.max(0.0, 1.0 - (d - 40.0) / 70.0);
                if (f > 0.0 && boss.strike(level, e, (float) (60.0 * f), true)) {
                    e.igniteForSeconds(5.0f);
                    SchrolliEntity.knock(e, sunPos, 1.5 * f, 0.5 * f);
                }
            }
            if (boss.sun != null) {
                boss.sun.discard();
                boss.sun = null;
            }
        }
        if (t > 140) {
            boss.steer(c.add(0.0, 10.0, 0.0), 0.05, 0.4);
        }
    }

    /** Urknall: ein Wirbel in der Mitte zieht alles an – dann zerbirst alles in einem neuen Anfang. */
    private static void genesis(SchrolliEntity boss, ServerLevel level, int t) {
        Vec3 c = boss.center();
        Vec3 g = boss.ground(c);
        Vec3 hub = g.add(0.0, 2.0, 0.0);
        boss.steer(c.add(0.0, 12.0, 0.0), 0.06, 0.6);
        if (t == 1) {
            boss.say(level, "beginning");
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 5.0f, 0.4f);
        }
        if (t == 24) {
            DivineFx.fx(level, SpellFxEntity.Kind.VORTEX, hub, DivineFx.COSMIC, 92, 6.0f).spawn();
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, g.add(0.0, 0.06, 0.0), DivineFx.EMBER, 86, 14.0f).spawn();
            boss.sound(level, ModSounds.SCHROLLI_ORB.get(), 6.0f, 0.5f);
        }
        if (t >= 30 && t < 110) {
            float k = (t - 30) / 80.0f;
            double pull = 0.025 + 0.085 * k;
            for (LivingEntity e : boss.enemies(level)) {
                Vec3 to = hub.subtract(e.position().add(0.0, e.getBbHeight() * 0.5, 0.0));
                double d = to.length();
                if (d > 56.0 || d < 0.6) {
                    continue;
                }
                Vec3 dir = to.scale(1.0 / d);
                double f = pull * (0.6 + 0.4 * (1.0 - d / 56.0));
                e.push(dir.x * f, dir.y * f * 0.3, dir.z * f);
                e.needsSync = true;
            }
            for (Projectile shot : level.getEntitiesOfClass(Projectile.class, new AABB(hub, hub).inflate(40.0), p -> !(p instanceof DivineShotEntity))) {
                Vec3 to = hub.subtract(shot.position());
                double d = to.length();
                if (d < 1.0) {
                    shot.discard();
                    continue;
                }
                shot.setDeltaMovement(shot.getDeltaMovement().scale(0.95).add(to.scale(0.12 / d)));
                shot.needsSync = true;
            }
            if (t % 2 == 0) {
                DivineFx.gather(level, hub, 18.0, 12);
            }
            if (t % 20 == 0) {
                boss.sound(level, ModSounds.SCHROLLI_ORB.get(), 4.0f, 0.5f + k * 0.5f);
            }
        }
        if (t == SchrolliEntity.GENESIS_BANG) {
            boss.sound(level, ModSounds.SCHROLLI_SUPERNOVA.get(), 7.0f, 1.3f);
            for (int k = 0; k < 4; k++) {
                DivineFx.flash(level, hub.add(0.0, k, 0.0), k % 2 == 0 ? DivineFx.COSMIC : DivineFx.WHITE);
            }
            DivineFx.sparks(level, hub, 300, 6.0, 0.7);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, hub.x, hub.y, hub.z, 300, 4.0, 2.0, 4.0, 1.0);
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, 0.1, 0.0), DivineFx.COSMIC, 36, 2.0f).param(1.2f).spawn();
            for (LivingEntity e : boss.enemies(level)) {
                double d = DivineFx.flat(e.position(), hub);
                if (d < 24.0) {
                    double f = 1.0 - d / 24.0;
                    if (boss.strike(level, e, (float) (34.0 * (0.35 + 0.65 * f)), true)) {
                        SchrolliEntity.knock(e, hub, 0.5 + 2.4 * f, 0.6);
                    }
                }
            }
        }
    }

    /** Sternbild: Sterne erscheinen um das Ziel, dünne Linien zeigen, wo – dann verbinden sie Strahlen aus Licht. */
    private static void constellation(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 15.0, 11.0);
        if (t == 8) {
            boss.points.clear();
            Vec3 base = target != null ? target.position() : boss.center();
            for (int k = 0; k < 9; k++) {
                double a = boss.getRandom().nextDouble() * Math.PI * 2.0;
                double r = 2.0 + boss.getRandom().nextDouble() * 12.0;
                Vec3 g = boss.ground(base.add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
                Vec3 p = g.add(0.0, 0.7 + boss.getRandom().nextDouble() * 1.9, 0.0);
                boss.points.add(p);
                DivineShotEntity.fixedStar(level, boss, p, 104);
            }
            boss.sound(level, ModSounds.SCHROLLI_STAR.get(), 3.0f, 1.2f);
        }
        if (t == 40 || t == 56) {
            boolean warn = t == 40;
            List<Vec3> p = boss.points;
            List<Vec3[]> segments = new ArrayList<>();
            for (int k = 0; k + 1 < p.size(); k++) {
                segments.add(new Vec3[] {p.get(k), p.get(k + 1)});
            }
            if (p.size() >= 9) {
                segments.add(new Vec3[] {p.get(0), p.get(4)});
                segments.add(new Vec3[] {p.get(2), p.get(7)});
                segments.add(new Vec3[] {p.get(3), p.get(8)});
            }
            for (int k = 0; k < segments.size(); k++) {
                Vec3[] s = segments.get(k);
                SpellFxEntity fx = DivineFx.fx(level, SpellFxEntity.Kind.BEAM, s[0], k % 2 == 0 ? DivineFx.COSMIC : DivineFx.CYAN, warn ? 16 : 54,
                        warn ? 0.12f : 0.75f).end(s[1]).spawn();
                if (!warn) {
                    boss.beams.add(fx);
                }
            }
            boss.sound(level, warn ? ModSounds.SCHROLLI_CHARGE.get() : ModSounds.SCHROLLI_BEAM.get(), 3.0f, warn ? 1.4f : 1.2f);
        }
        if (t > 56 && t < 110) {
            for (SpellFxEntity fx : boss.beams) {
                if (!fx.isRemoved()) {
                    beamHits(boss, level, fx.position(), fx.endPoint(), 0.75, 14.0f, 12, false);
                }
            }
        }
        if (t == 110) {
            for (Vec3 p : boss.points) {
                DivineFx.flash(level, p, DivineFx.CYAN);
                DivineFx.sparks(level, p, 12, 0.4, 0.1);
            }
            boss.clearArtState();
        }
    }

    /** Doppelklinge: zwei Heiligenscheine kreisen in einer Spirale um ihn – weit hinaus und wieder zurück. */
    private static void twinHalos(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t == 1) {
            boss.mark = boss.ground(target != null ? target.position().lerp(boss.position(), 0.5) : boss.position());
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 3.0f, 1.6f);
        }
        boss.steer(boss.mark.add(0.0, 9.0, 0.0), 0.06, 0.6);
        if (target != null) {
            boss.face(target.position(), 10.0f);
        }
        if (t == 16) {
            boss.setFlag(SchrolliEntity.FLAG_HALO, true);
            for (int k = 0; k < 2; k++) {
                DivineShotEntity.orbitHalo(level, boss, boss.mark.add(0.0, 1.1, 0.0), k * 180.0f, 16.0f * boss.power(), 94);
            }
            boss.sound(level, ModSounds.SCHROLLI_HALO.get(), 4.0f, 0.8f);
        }
    }

    /** Das Jüngste Gericht: hoch über dem Hof ruft er einen Regen aus Säulen des Lichts herab – überall. */
    private static void apocalypse(SchrolliEntity boss, ServerLevel level, int t) {
        Vec3 c = boss.center();
        boss.steer(c.add(0.0, 24.0, 0.0), 0.06, 0.6);
        boss.turnTo(boss.getYRot() + 1.5f, 360.0f);
        if (t == 1) {
            boss.say(level, "last_judgment");
            boss.announce(level, Component.translatable("entity.kingdomsatwar.schrolli.apocalypse").withColor(0xFF6A3A).withStyle(ChatFormatting.BOLD),
                    null, 50);
            boss.sound(level, ModSounds.SCHROLLI_JUDGMENT.get(), 6.0f, 0.5f);
        }
        if (t >= 30 && t <= 190 && t % 3 == 0) {
            Vec3 spot;
            List<LivingEntity> enemies = boss.enemies(level);
            if (!enemies.isEmpty() && boss.getRandom().nextFloat() < 0.4f) {
                LivingEntity e = enemies.get(boss.getRandom().nextInt(enemies.size()));
                spot = boss.ground(e.position().add((boss.getRandom().nextDouble() - 0.5) * 6.0, 0.0, (boss.getRandom().nextDouble() - 0.5) * 6.0));
            } else {
                double a = boss.getRandom().nextDouble() * Math.PI * 2.0;
                double r = Math.sqrt(boss.getRandom().nextDouble()) * 42.0;
                spot = boss.ground(c.add(Math.cos(a) * r, 0.0, Math.sin(a) * r));
            }
            warnPillar(boss, level, spot, 2.6f, 24, 18.0f, t % 2 == 0 ? DivineFx.GOLD : DivineFx.COSMIC, 40.0f);
        }
    }

    /** Augenstrahlen: aus seinen Augen fahren zwei Strahlen über den Boden, gegenläufig, mal nah, mal fern. */
    private static void eyeBeams(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (target != null) {
            boss.hover(target, 15.0, 11.0);
        }
        boss.setDeltaMovement(boss.getDeltaMovement().scale(0.6));
        if (t == 1) {
            boss.setFlag(SchrolliEntity.FLAG_GLARE, true);
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 4.0f, 1.0f);
            boss.aim = target != null ? target.position() : boss.center();
            boss.sweep = DivineFx.yaw(boss.position(), boss.aim);
        }
        if (t < 30) {
            boss.face(boss.aim, 10.0f);
            return;
        }
        Vec3 g = boss.ground(boss.position());
        if (t == 30) {
            boss.sound(level, ModSounds.SCHROLLI_BEAM.get(), 5.0f, 1.3f);
            boss.beams.add(DivineFx.fx(level, SpellFxEntity.Kind.BEAM, boss.eyes(), DivineFx.COSMIC, 90, 1.0f).end(g).spawn());
            boss.beams.add(DivineFx.fx(level, SpellFxEntity.Kind.BEAM, boss.eyes(), DivineFx.CYAN, 90, 1.0f).end(g).spawn());
        }
        if (t >= 30 && t < 118 && boss.beams.size() >= 2) {
            float k = (t - 30) / 88.0f;
            double reach = 10.0 + 14.0 * (0.5 + 0.5 * Math.sin(t * 0.09));
            double[] angles = {boss.sweep - 70.0 + 140.0 * k, boss.sweep + 70.0 - 140.0 * k};
            Vec3 eyes = boss.eyes();
            for (int i = 0; i < 2; i++) {
                Vec3 point = boss.ground(g.add(DivineFx.dir(angles[i]).scale(reach)));
                SpellFxEntity fx = boss.beams.get(i);
                fx.setPos(eyes.x, eyes.y, eyes.z);
                fx.moveEnd(point.add(0.0, 0.1, 0.0));
                if (t % 4 == 0) {
                    beamHits(boss, level, eyes, point, 1.5, 9.0f, 4, true);
                }
                if (t % 2 == 0) {
                    level.sendParticles(ParticleTypes.REVERSE_PORTAL, point.x, point.y + 0.2, point.z, 4, 0.4, 0.1, 0.4, 0.05);
                }
            }
            boss.face(boss.aim, 4.0f);
        }
        if (t == 118) {
            boss.setFlag(SchrolliEntity.FLAG_GLARE, false);
            boss.clearArtState();
        }
    }

    /** Kometensturz: er schießt in den Himmel – ein Kreis am Boden – und stürzt wie ein Komet darauf herab. */
    private static void comet(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t < 36) {
            boss.setDeltaMovement(0.0, Math.min(1.6, 0.3 + t * 0.06), 0.0);
            if (t == 1) {
                boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 5.0f, 0.6f);
            }
            if (t % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, boss.getX(), boss.getY(), boss.getZ(), 6, 0.8, 0.4, 0.8, 0.05);
            }
            return;
        }
        if (t == 36) {
            boss.mark = target != null ? lead(boss, target, 14) : boss.ground(boss.center());
            boss.aim = boss.mark.add(0.0, 48.0, 0.0);
            boss.teleportTo(boss.aim.x, boss.aim.y, boss.aim.z);
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.mark.add(0.0, 0.06, 0.0), DivineFx.EMBER, 34, 6.5f).spawn();
            if (target instanceof ServerPlayer sp) {
                sp.sendOverlayMessage(Component.translatable("entity.kingdomsatwar.schrolli.comet").withColor(0xFF8A40));
            }
        }
        if (t > 36 && t <= 70) {
            float k = (t - 36) / 34.0f;
            Vec3 p = boss.aim.lerp(boss.mark.add(0.0, 0.3, 0.0), k * k);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            level.sendParticles(ParticleTypes.FLAME, p.x, p.y + 3.0, p.z, 6, 0.8, 1.5, 0.8, 0.05);
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 3.0, p.z, 4, 0.8, 1.5, 0.8, 0.05);
            if (t == 50) {
                boss.sound(level, ModSounds.SCHROLLI_STAR.get(), 5.0f, 0.5f);
            }
        }
        if (t == 70) {
            Vec3 m = boss.mark;
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 7.0f, 0.6f);
            boss.sound(level, ModSounds.SCHROLLI_BLAST.get(), 5.0f, 0.7f);
            DivineFx.flash(level, m.add(0.0, 1.0, 0.0), DivineFx.AMBER);
            DivineFx.flash(level, m.add(0.0, 2.0, 0.0), DivineFx.WHITE);
            level.sendParticles(ParticleTypes.EXPLOSION, m.x, m.y + 0.5, m.z, 6, 2.0, 0.4, 2.0, 0.0);
            level.sendParticles(ParticleTypes.LAVA, m.x, m.y + 0.5, m.z, 30, 2.0, 0.4, 2.0, 0.0);
            DivineFx.sparks(level, m.add(0.0, 0.5, 0.0), 150, 3.0, 0.4);
            for (LivingEntity e : boss.enemiesNear(level, m.add(0.0, 1.0, 0.0), 7.5)) {
                double d = DivineFx.flat(e.position(), m);
                boolean hit = d < 3.2 ? boss.strike(level, e, 34.0f, false) : boss.strike(level, e, (float) (20.0 * (1.0 - d / 7.5)), true);
                if (hit) {
                    SchrolliEntity.knock(e, m, 2.0 * (1.0 - d / 8.0) + 0.3, 0.7);
                }
            }
            Set<UUID> hit = new HashSet<>();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, m.add(0.0, 0.1, 0.0), DivineFx.EMBER, 40, 3.0f).param(0.7f)
                    .onTick(fx -> ringHits(boss, level, fx, m.y, 12.0f, false, hit)).spawn();
            for (int k = 0; k < 6; k++) {
                Vec3 end = m.add(DivineFx.dir(k * 60.0 + 15.0).scale(11.0));
                DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, m.add(0.0, 0.05, 0.0), DivineFx.EMBER, 50, 1.1f).param(0.0f).end(end).spawn();
            }
        }
        if (t > 76) {
            boss.steer(boss.mark.add(0.0, 10.0, 0.0), 0.06, 0.6);
        }
    }

    /** Weltenriss: sechs Risse laufen strahlenförmig von ihm aus über den Boden – dann bricht Licht aus ihnen. Zweimal. */
    private static void worldSplit(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        keepNear(boss, target, 9.0, 7.0);
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 3.0f, 0.7f);
        }
        if (t == 24 || t == 58) {
            Vec3 g = boss.ground(boss.position());
            double base = target != null ? DivineFx.yaw(g, target.position()) : boss.getYRot();
            if (t == 58) {
                base += 30.0;
            }
            boss.points.clear();
            for (int k = 0; k < 6; k++) {
                Vec3 end = boss.ground(g.add(DivineFx.dir(base + k * 60.0).scale(28.0)));
                DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, g.add(0.0, 0.05, 0.0), k % 2 == 0 ? DivineFx.COSMIC : DivineFx.HOLY, 44, 1.3f)
                        .param(14.0f).end(end).spawn();
                boss.points.add(g);
                boss.points.add(end);
            }
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 3.0f, 1.2f);
        }
        if ((t == 38 || t == 72) && boss.points.size() >= 2) {
            for (int k = 0; k + 1 < boss.points.size(); k += 2) {
                Vec3 a = boss.points.get(k);
                Vec3 b = boss.points.get(k + 1);
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(a, b).inflate(2.0, 3.0, 2.0), boss::canStrike)) {
                    if (e.getY() < a.y + 1.5 && DivineFx.distanceToSegment(new Vec3(e.getX(), a.y, e.getZ()), a, b) < 1.4
                            && boss.strike(level, e, 16.0f, true)) {
                        e.push(0.0, 1.0, 0.0);
                        e.needsSync = true;
                    }
                }
                Vec3 d = b.subtract(a);
                double len = d.length();
                for (double s = 2.0; s < len; s += 2.5) {
                    Vec3 p = a.add(d.scale(s / len));
                    level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 0.5, p.z, 4, 0.2, 1.4, 0.2, 0.15);
                }
            }
            boss.sound(level, ModSounds.SCHROLLI_PILLAR.get(), 4.0f, 1.1f);
        }
    }

    /**
     * Neuschöpfung: er schwebt über der Mitte und erschafft sich neu – seine Leiste füllt sich wieder. Wer ihm genug
     * Schaden zufügt, unterbricht ihn: Er stürzt und taumelt (und nimmt dann mehr Schaden).
     */
    private static void recreation(SchrolliEntity boss, ServerLevel level, int t) {
        Vec3 c = boss.center();
        boss.steer(c.add(0.0, 6.0, 0.0), 0.06, 0.5);
        int players = Math.max(1, boss.challengers(level).size());
        float threshold = SchrolliEntity.DESIGNED_HEALTH * 0.035f * players;
        if (t == 1) {
            boss.say(level, "anew");
            boss.setFlag(SchrolliEntity.FLAG_CHANNEL, true);
            boss.channelDamage = 0.0f;
            boss.markFx = DivineFx.fx(level, SpellFxEntity.Kind.CAST_CIRCLE, boss.ground(c), DivineFx.COSMIC, 150, 7.0f).param(0.0f).spawn();
            boss.sound(level, ModSounds.SCHROLLI_HEAL.get(), 5.0f, 0.8f);
        }
        if (boss.channelDamage >= threshold) {
            boss.setFlag(SchrolliEntity.FLAG_CHANNEL, false);
            boss.begin(Attack.STUNNED);
            return;
        }
        if (t >= 20 && t <= 150) {
            if (boss.markFx != null) {
                boss.markFx.setParam((t - 20) / 130.0f);
            }
            if (t % 10 == 0) {
                boss.heal(boss.getMaxHealth() * 0.012f);
                boss.sound(level, ModSounds.SCHROLLI_HEAL.get(), 2.5f, 1.0f + (t - 20) / 260.0f);
                int percent = Math.round(boss.channelDamage / threshold * 100.0f);
                boss.tell(level, Component.translatable("entity.kingdomsatwar.schrolli.recreation", Math.min(99, percent)).withColor(0xD8A8FF));
            }
            if (t % 2 == 0) {
                DivineFx.gather(level, boss.position().add(0.0, 4.0, 0.0), 10.0, 8);
            }
        }
        if (t == 152) {
            boss.setFlag(SchrolliEntity.FLAG_CHANNEL, false);
            DivineFx.flash(level, boss.position().add(0.0, 4.0, 0.0), DivineFx.COSMIC);
        }
    }

    // =====================================================================================
    // Am Boden: die Klinge aus Licht (seit 0.76)
    // =====================================================================================

    /** Klingenhieb: die Hiebe (Ticks); der letzte ist der Spaltschlag. */
    static final int[] STRIKE_HITS = {18, 36, 60};
    static final int[] STRIKE_HITS_TRUE = {16, 30, 44, 62};
    /** Lichtsicheln: wann jede Sichel losgeht. */
    static final int[] SLASH_TIMES = {18, 34, 50};
    static final int[] SLASH_TIMES_TRUE = {16, 30, 44, 58};
    /** Sturmlauf: Beginn und Ende des Laufs (Wahre Gestalt: ein zweiter). */
    static final int DASH_GO = 20;
    static final int DASH_END = 31;
    static final int DASH_GO_2 = 42;
    static final int DASH_END_2 = 52;
    /** Klingenwirbel: von hier bis hier wirbelt er. */
    static final int WHIRL_FROM = 16;
    static final int WHIRL_TO = 46;
    /** Himmelssturz: Absprung, höchster Punkt, Sturz. */
    static final int PLUNGE_LEAP = 10;
    static final int PLUNGE_APEX = 30;
    static final int PLUNGE_FALL = 40;
    /** Gegenschlag: der Hieb. */
    static final int COUNTER_HIT = 8;
    /** Weltenklinge: die Klinge wächst, der Rundschlag, der Spaltschlag, der Riss bricht auf. */
    static final int WORLD_GROW = 6;
    static final int WORLD_SWEEP = 44;
    static final int WORLD_SWEEP_END = 62;
    static final int WORLD_WARN = 70;
    static final int WORLD_CHOP = 86;
    static final int WORLD_ERUPT = 98;
    /** So weit reicht die Weltenklinge im Rundschlag, so lang ist ihr Spaltschlag. */
    static final double WORLD_REACH = 22.0;
    static final double WORLD_CHOP_LENGTH = 34.0;

    /** Reichweite der Klinge (waagrecht von seiner Mitte). */
    static double reach(SchrolliEntity boss) {
        return 7.0 * boss.getScale();
    }

    /** Waagrechte Richtung zum Ziel. */
    private static Vec3 toward(SchrolliEntity boss, Vec3 at) {
        Vec3 to = at.subtract(boss.position()).multiply(1.0, 0.0, 1.0);
        return to.lengthSqr() < 1.0e-4 ? boss.forward() : to.normalize();
    }

    /** Nicht über den Rand des Hofs hinaus (die Säulen stehen auf 35). */
    private static Vec3 inCourt(SchrolliEntity boss, Vec3 at) {
        Vec3 c = boss.center();
        double limit = io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.isArena(boss.level()) ? 33.0 : 58.0;
        Vec3 off = at.subtract(c);
        double flat = Math.sqrt(off.x * off.x + off.z * off.z);
        if (flat <= limit) {
            return at;
        }
        return boss.ground(new Vec3(c.x + off.x / flat * limit, at.y, c.z + off.z / flat * limit));
    }

    /**
     * Ein Hieb der Klinge: trifft jeden vor ihm ({@code halfAngle} Grad um {@code yaw}), bis {@code reach} weit, vom Boden bis
     * über seinen Kopf. Leichte Hiebe sind körperlich (ein Schild fängt sie ab), goldene ({@code divine}) gehen durch alles.
     */
    static int cut(SchrolliEntity boss, ServerLevel level, double yaw, double reach, double halfAngle, float damage, boolean divine,
            double knockH, double knockUp) {
        Vec3 c = boss.position();
        Vec3 fwd = DivineFx.dir(yaw);
        double cos = Math.cos(Math.toRadians(halfAngle));
        int hits = 0;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(reach + 2.0, 9.0, reach + 2.0), boss::canStrike)) {
            Vec3 to = e.position().subtract(c).multiply(1.0, 0.0, 1.0);
            double d = to.length();
            double dy = e.getY() - c.y;
            if (d > reach + e.getBbWidth() * 0.5 || dy < -2.5 || dy > boss.getBbHeight() + 1.0) {
                continue;
            }
            if (d > 1.6 * boss.getScale() && to.scale(1.0 / d).dot(fwd) < cos) {
                continue;
            }
            if (boss.strike(level, e, damage, divine)) {
                SchrolliEntity.knock(e, c, knockH, knockUp);
                hits++;
            }
            level.sendParticles(ParticleTypes.CRIT, e.getX(), e.getY(0.6), e.getZ(), 8, 0.3, 0.4, 0.3, 0.3);
        }
        return hits;
    }

    /** Die Spur eines Hiebs: ein Bogen aus Licht um ihn (von {@code from} bis {@code to} Grad um {@code yaw}, in der Höhe von–bis). */
    static void slashArc(SchrolliEntity boss, ServerLevel level, double yaw, double from, double to, double radius, double hFrom, double hTo) {
        boolean truly = boss.isTrueForm();
        ParticleOptions dust = truly ? DivineFx.VIOLET_DUST : DivineFx.GOLD_DUST;
        int steps = Math.max(6, (int) (Math.abs(to - from) / 6.0));
        for (int i = 0; i <= steps; i++) {
            double k = i / (double) steps;
            double a = yaw + Mth.lerp(k, from, to);
            double h = Mth.lerp(k, hFrom, hTo);
            Vec3 p = boss.position().add(DivineFx.dir(a).scale(radius * (0.85 + 0.15 * Math.sin(k * Math.PI)))).add(0.0, h, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.05, 0.05, 0.05, 0.01);
            level.sendParticles(dust, p.x, p.y, p.z, 2, 0.18, 0.18, 0.18, 0.0);
            if (truly && i % 2 == 0) {
                level.sendParticles(ParticleTypes.REVERSE_PORTAL, p.x, p.y, p.z, 2, 0.1, 0.1, 0.1, 0.05);
            }
            if (i % 3 == 1) {
                level.sendParticles(ParticleTypes.SWEEP_ATTACK, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
            }
        }
    }

    /** Staub vom Boden unter {@code at} (ein Ring). */
    private static void dust(SchrolliEntity boss, ServerLevel level, Vec3 at, double radius, int count) {
        BlockState floor = level.getBlockState(net.minecraft.core.BlockPos.containing(at.x, at.y - 0.5, at.z));
        if (floor.isAir()) {
            return;
        }
        BlockParticleOption particle = new BlockParticleOption(ParticleTypes.BLOCK, floor);
        for (int i = 0; i < count; i++) {
            double a = Math.PI * 2.0 * i / count;
            level.sendParticles(particle, at.x + Math.cos(a) * radius, at.y + 0.2, at.z + Math.sin(a) * radius, 3, 0.3, 0.1, 0.3, 0.15);
        }
        level.sendParticles(ParticleTypes.DUST_PLUME, at.x, at.y + 0.2, at.z, 12, radius * 0.4, 0.1, radius * 0.4, 0.05);
    }

    /** Wer gerade vor der Schneide einer gleitenden Lichtsichel steht (und nicht darüber springt) – jeden nur einmal. */
    private static void crescentHits(SchrolliEntity boss, ServerLevel level, SpellFxEntity fx, Set<UUID> hit, float damage) {
        Vec3 f = fx.forward();
        Vec3 side = new Vec3(-f.z, 0.0, f.x);
        double half = fx.size() * 0.5;
        Vec3 p = fx.position();
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(p, p).inflate(half + 1.5, 2.5, half + 1.5), boss::canStrike)) {
            if (hit.contains(e.getUUID())) {
                continue;
            }
            Vec3 rel = e.position().subtract(p);
            double across = rel.dot(side);
            if (Math.abs(across) > half + e.getBbWidth() * 0.5) {
                continue;
            }
            double x = Mth.clamp(across / half, -1.0, 1.0);
            double along = rel.dot(f) - SpellFxEntity.CRESCENT_BULGE * (1.0 - x * x);
            double up = e.getY() - p.y;
            if (along < -1.2 || along > 0.8 || up < -1.0 || up > 1.1) {
                continue;                               // dahinter, davor – oder drübergesprungen
            }
            hit.add(e.getUUID());
            if (boss.strike(level, e, damage, true)) {
                e.push(f.x * 0.6, 0.35, f.z * 0.6);
                e.needsSync = true;
            }
            level.sendParticles(ParticleTypes.END_ROD, e.getX(), e.getY(0.5), e.getZ(), 10, 0.3, 0.4, 0.3, 0.06);
        }
    }

    // ------------------------------------------------------------------ Landung und Aufschwung

    /** Landung: ein Warnkreis, der Sturz, der Einschlag (ein Ring rollt – springen!); Beine und Klinge formen sich aus Licht. */
    private static void descend(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        int land = SchrolliEntity.DESCEND_LAND;
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 3.0f, 0.8f);
            Vec3 spot = boss.ground(boss.position());
            if (target != null) {
                Vec3 dir = toward(boss, target.position());
                spot = boss.ground(lead(boss, target, 10).subtract(dir.scale(4.0 * boss.getScale())));
            }
            boss.mark = inCourt(boss, spot);
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.mark.add(0.0, 0.06, 0.0), DivineFx.AMBER, land, 4.5f).spawn();
        }
        if (t < 8) {
            boss.steer(boss.position().add(0.0, 2.5, 0.0), 0.08, 0.4);
            boss.face(boss.mark, 12.0f);
            return;
        }
        if (t == 8) {
            boss.aim = boss.position();
        }
        if (t <= land) {
            float k = (t - 8) / (float) (land - 8);
            Vec3 p = boss.aim.lerp(boss.mark, k * k);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            boss.face(boss.mark, 20.0f);
            if (t % 2 == 0) {
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 3.0, p.z, 4, 0.8, 1.2, 0.8, 0.05);
            }
        }
        if (t == land) {
            boss.setGrounded(true);
            Vec3 m = boss.mark;
            boss.sound(level, ModSounds.SCHROLLI_LAND.get(), 5.0f, 1.0f);
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 3.0f, 0.8f);
            DivineFx.flash(level, m.add(0.0, 1.0, 0.0), DivineFx.WHITE);
            DivineFx.sparks(level, m.add(0.0, 0.5, 0.0), 50, 2.0, 0.2);
            dust(boss, level, m, 3.0, 16);
            for (LivingEntity e : boss.enemiesNear(level, m.add(0.0, 1.0, 0.0), 4.5)) {
                if (boss.strike(level, e, 14.0f, true)) {
                    SchrolliEntity.knock(e, m, 1.1, 0.7);
                }
            }
            Set<UUID> hit = new HashSet<>();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, m.add(0.0, 0.1, 0.0), DivineFx.GOLD, 30, 2.5f).param(0.6f)
                    .onTick(fx -> ringHits(boss, level, fx, m.y, 8.0f, false, hit)).spawn();
            for (int k = 0; k < 5; k++) {
                Vec3 end = m.add(DivineFx.dir(boss.getYRot() + 36.0 + k * 72.0).scale(6.0));
                DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, m.add(0.0, 0.05, 0.0), DivineFx.GOLD, 34, 0.7f).param(0.0f).end(end).spawn();
            }
            boss.sayOnce(level, "hand");
        }
        if (t == land + 3) {
            // das Zepter wird zur Klinge
            boss.sound(level, ModSounds.SCHROLLI_BLADE.get(), 3.0f, 1.0f);
            Vec3 hand = boss.local(0.95, 3.6, 1.2);
            DivineFx.flash(level, hand, DivineFx.WHITE);
            DivineFx.sparks(level, hand, 30, 0.6, 0.15);
        }
        if (t > land) {
            boss.walk(Vec3.ZERO);
            if (target != null) {
                boss.face(target.position(), 8.0f);
            }
        }
    }

    /** Aufschwung: die Klinge wird wieder zum Zepter, ein Schwingenschlag wirft alles zurück, und er hebt ab. */
    private static void rise(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        int lift = SchrolliEntity.RISE_LIFT;
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_BLADE.get(), 2.5f, 0.75f);
        }
        if (t < lift) {
            boss.walk(Vec3.ZERO);
            if (target != null) {
                boss.face(target.position(), 10.0f);
            }
            if (t % 2 == 0) {
                DivineFx.gather(level, boss.position().add(0.0, 1.0, 0.0), 5.0, 3);
            }
            return;
        }
        if (t == lift) {
            boss.setGrounded(false);
            Vec3 c = boss.position();
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 4.0f, 0.7f);
            for (LivingEntity e : boss.enemiesNear(level, c.add(0.0, 1.5, 0.0), 8.0)) {
                boss.strike(level, e, 4.0f, true);
                SchrolliEntity.knock(e, c, 1.4, 0.5);
            }
            for (int k = 1; k <= 3; k++) {
                DivineFx.ring(level, ParticleTypes.CLOUD, c.add(0.0, 0.3 * k, 0.0), 2.5 * k, 10 * k);
            }
            dust(boss, level, c, 2.5, 12);
            level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.DIVINE_FEATHER.get(), c.x, c.y + 3.0, c.z, 30, 3.0, 1.5, 3.0, 0.2);
            boss.setDeltaMovement(0.0, 0.9, 0.0);
            return;
        }
        if (target != null) {
            boss.hover(target, 11.0, 8.0);
            boss.face(target.position(), 10.0f);
        } else {
            boss.steer(boss.center().add(0.0, 12.0, 0.0), 0.06, 0.5);
        }
    }

    // ------------------------------------------------------------------ die Künste der Klinge

    /**
     * Klingenhieb: er rückt nach und schlägt zu – schräge Schnitte (ein Schild fängt sie ab), zuletzt ein Spaltschlag: eine
     * goldene Linie zeigt ihn an, er reißt den Boden vor ihm auf (ausweichen!).
     */
    private static void strike(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        int[] hits = boss.isTrueForm() ? STRIKE_HITS_TRUE : STRIKE_HITS;
        int last = hits[hits.length - 1];
        double reach = reach(boss);
        boolean chopping = t >= last - 14 && t <= last;
        if (target != null && t <= last && !chopping) {
            double d = DivineFx.flat(boss.position(), target.position());
            boolean lunge = false;
            for (int h : hits) {
                lunge |= t >= h - 8 && t < h;
            }
            boss.walk(toward(boss, target.position()).scale(d > reach * 0.55 ? (lunge ? 0.45 : 0.18) : 0.0));
            boss.face(target.position(), lunge ? 16.0f : 9.0f);
        } else {
            boss.walk(Vec3.ZERO);
        }
        for (int i = 0; i < hits.length; i++) {
            boolean chop = i == hits.length - 1;
            if (t == hits[i] - (chop ? 14 : 7)) {
                boss.sound(level, ModSounds.SCHROLLI_SWING.get(), 2.0f, chop ? 0.8f : 1.0f);
                if (chop) {
                    Vec3 a = boss.ground(boss.position());
                    Vec3 b = boss.ground(boss.position().add(boss.forward().scale(reach + 12.0)));
                    DivineFx.fx(level, SpellFxEntity.Kind.BEAM, a.add(0.0, 0.15, 0.0), DivineFx.AMBER, 14, 0.14f).end(b.add(0.0, 0.15, 0.0)).spawn();
                }
            }
            if (t != hits[i]) {
                continue;
            }
            double yaw = boss.getYRot();
            if (!chop) {
                cut(boss, level, yaw, reach, 75.0, 16.0f, false, 0.9, 0.35);
                boolean fromRight = i % 2 == 0;
                slashArc(boss, level, yaw, fromRight ? 75.0 : -75.0, fromRight ? -75.0 : 75.0, reach * 0.75, fromRight ? 5.5 : 1.5,
                        fromRight ? 1.5 : 5.0);
                boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 2.5f, 1.0f + i * 0.04f);
                continue;
            }
            // der Spaltschlag: schmal, schwer, golden – und der Boden reißt vor ihm auf
            cut(boss, level, yaw, reach + 1.0, 26.0, 22.0f, true, 1.2, 0.6);
            Vec3 tip = boss.ground(boss.position().add(boss.forward().scale(reach * 0.9)));
            Vec3 end = boss.ground(boss.position().add(boss.forward().scale(reach + 12.0)));
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(tip, end).inflate(2.5, 3.0, 2.5), boss::canStrike)) {
                if (DivineFx.distanceToSegment(new Vec3(e.getX(), tip.y, e.getZ()), tip, end) < 1.5 && e.getY() - tip.y < 1.2
                        && boss.strike(level, e, 14.0f, true)) {
                    e.push(0.0, 0.7, 0.0);
                    e.needsSync = true;
                }
            }
            DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, tip.add(0.0, 0.05, 0.0), boss.isTrueForm() ? DivineFx.COSMIC : DivineFx.GOLD, 40, 1.2f)
                    .param(0.0f).end(end).spawn();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, tip.add(0.0, 0.1, 0.0), DivineFx.GOLD, 14, 1.0f).param(0.5f).spawn();
            DivineFx.flash(level, tip.add(0.0, 0.6, 0.0), DivineFx.WHITE);
            DivineFx.sparks(level, tip.add(0.0, 0.4, 0.0), 40, 1.2, 0.25);
            dust(boss, level, tip, 1.5, 10);
            slashArc(boss, level, yaw, 0.0, 0.0, reach * 0.6, 7.0, 0.5);
            boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 3.0f, 0.85f);
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 3.0f, 0.9f);
        }
    }

    /** Lichtsicheln: aus der Klinge lösen sich Sicheln aus Licht und gleiten über den Boden auf ihr Ziel zu – springen! */
    private static void slashes(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        boolean truly = boss.isTrueForm();
        int[] times = truly ? SLASH_TIMES_TRUE : SLASH_TIMES;
        if (target != null) {
            double d = DivineFx.flat(boss.position(), target.position());
            boss.walk(toward(boss, target.position()).scale(d < 7.0 ? -0.16 : 0.0));
            boss.face(target.position(), 12.0f);
        } else {
            boss.walk(Vec3.ZERO);
        }
        for (int i = 0; i < times.length; i++) {
            if (t == times[i] - 6) {
                boss.sound(level, ModSounds.SCHROLLI_SWING.get(), 1.8f, 1.0f);
            }
            if (t != times[i]) {
                continue;
            }
            Vec3 aim = target != null ? lead(boss, target, 6) : boss.position().add(boss.forward().scale(10.0));
            float yaw = (float) DivineFx.yaw(boss.position(), aim);
            Vec3 from = boss.ground(boss.position().add(DivineFx.dir(yaw).scale(2.0 * boss.getScale())));
            int count = truly ? 3 : 1;
            for (int k = 0; k < count; k++) {
                float spread = (k - (count - 1) / 2.0f) * 22.0f;
                Set<UUID> hit = new HashSet<>();
                DivineFx.fx(level, SpellFxEntity.Kind.CRESCENT, from.add(0.0, 0.1, 0.0), truly ? DivineFx.COSMIC : DivineFx.HOLY, 34, truly ? 9.0f : 8.0f)
                        .param(0.85f).facing(yaw + spread).onTick(fx -> crescentHits(boss, level, fx, hit, 14.0f)).spawn();
            }
            boolean fromRight = i % 2 == 0;
            slashArc(boss, level, boss.getYRot(), fromRight ? 80.0 : -80.0, fromRight ? -80.0 : 80.0, reach(boss) * 0.7, 2.0, 0.8);
            boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 2.5f, 0.95f);
        }
    }

    /** Sturmlauf: geduckt; eine goldene Linie zeigt den Weg – dann stürmt er hindurch, die Klinge voran; hinter ihm bricht die Spur auf. */
    private static void dash(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        boolean truly = boss.isTrueForm();
        boolean second = truly && t >= DASH_GO_2 - 8;
        int go = second ? DASH_GO_2 : DASH_GO;
        int end = second ? DASH_END_2 : DASH_END;
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 2.5f, 0.8f);
        }
        if (t == go - 8) {
            Vec3 start = boss.position();
            Vec3 dir = boss.forward();
            double length = 18.0;
            if (target != null) {
                dir = toward(boss, lead(boss, target, 6));
                length = Mth.clamp(DivineFx.flat(start, target.position()) + 7.0, 12.0, 26.0);
                boss.turnTo((float) Math.toDegrees(Math.atan2(dir.z, dir.x)) - 90.0f, 360.0f);
            }
            boss.aim = start;
            boss.mark = inCourt(boss, boss.ground(start.add(dir.scale(length))));
            boss.struck.clear();
            Vec3 a = boss.ground(start);
            DivineFx.fx(level, SpellFxEntity.Kind.BEAM, a.add(0.0, 0.15, 0.0), DivineFx.GOLD, 8 + end - go, 0.16f).end(boss.mark.add(0.0, 0.15, 0.0)).spawn();
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.mark.add(0.0, 0.06, 0.0), DivineFx.AMBER, 8, 2.5f).spawn();
        }
        boolean dashing = t >= go && t <= end;
        if (!dashing) {
            boss.walk(Vec3.ZERO);
            boolean aiming = t < DASH_GO - 8 || truly && t > DASH_END && t < DASH_GO_2 - 8;
            if (target != null && aiming) {
                boss.face(lead(boss, target, 6), 14.0f);
            }
        }
        if (t == go) {
            boss.sound(level, ModSounds.SCHROLLI_DASH.get(), 4.0f, 1.0f);
            DivineFx.flash(level, boss.position().add(0.0, 2.0, 0.0), DivineFx.WHITE);
            dust(boss, level, boss.position(), 2.0, 10);
        }
        if (dashing) {
            float k = (t - go) / (float) (end - go);
            Vec3 prev = boss.position();
            Vec3 p = boss.aim.lerp(boss.mark, k);
            p = new Vec3(p.x, boss.ground(p).y, p.z);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            Vec3 a = prev.add(0.0, 1.0, 0.0);
            Vec3 b = p.add(0.0, 1.0, 0.0);
            double width = 2.6 * boss.getScale();
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(a, b).inflate(width + 1.0, 4.0, width + 1.0), boss::canStrike)) {
                if (boss.struck.contains(e.getUUID()) || DivineFx.distanceToSegment(e.position().add(0.0, 0.5, 0.0), a, b) > width) {
                    continue;
                }
                boss.struck.add(e.getUUID());
                if (boss.strike(level, e, 18.0f, false)) {
                    Vec3 dir = b.subtract(a);
                    Vec3 side = new Vec3(-dir.z, 0.0, dir.x);
                    double sign = Math.signum(e.position().subtract(a).dot(side));
                    Vec3 push = side.lengthSqr() < 1.0e-6 ? Vec3.ZERO : side.normalize().scale(sign == 0 ? 1.0 : sign);
                    e.push(push.x * 1.3, 0.55, push.z * 1.3);
                    e.needsSync = true;
                }
            }
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 2.0, p.z, 6, 0.6, 1.4, 0.6, 0.03);
            level.sendParticles(truly ? DivineFx.VIOLET_DUST : DivineFx.GOLD_DUST, p.x, p.y + 0.3, p.z, 8, 0.8, 0.2, 0.8, 0.0);
        }
        if (t == end) {
            boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 3.0f, 0.9f);
            Vec3 a = boss.ground(boss.aim);
            Vec3 b = boss.ground(boss.mark);
            DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, a.add(0.0, 0.05, 0.0), truly ? DivineFx.COSMIC : DivineFx.GOLD, 34, 1.0f).param(8.0f).end(b).spawn();
            double len = DivineFx.flat(a, b);
            for (double d = 3.0; d < len; d += 3.5) {
                Vec3 p = boss.ground(a.lerp(b, d / len));
                warnPillar(boss, level, p, 1.6f, 14, 12.0f, truly ? DivineFx.COSMIC : DivineFx.HOLY, 12.0f);
            }
        }
    }

    /** Klingenwirbel: die Klinge seitwärts gestreckt, wirbelt er um sich und rückt dabei vor; zuletzt rollt ein Ring (springen!). */
    private static void whirl(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        double r = reach(boss) * 1.05;
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 2.0f, 0.9f);
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.ground(boss.position()).add(0.0, 0.06, 0.0), DivineFx.AMBER, WHIRL_FROM + 2, (float) r)
                    .spawn();
        }
        if (t < WHIRL_FROM) {
            boss.walk(Vec3.ZERO);
            if (target != null) {
                boss.face(target.position(), 10.0f);
            }
            if (t % 3 == 0) {
                DivineFx.gather(level, boss.local(1.6, 3.0, 0.6), 3.0, 3);
            }
            return;
        }
        if (t < WHIRL_TO) {
            boss.turnTo(boss.getYRot() + (boss.isTrueForm() ? 36.0f : 30.0f), 360.0f);
            if (target != null) {
                double d = DivineFx.flat(boss.position(), target.position());
                boss.walk(toward(boss, target.position()).scale(d > 3.0 ? 0.24 : 0.0));
            } else {
                boss.walk(Vec3.ZERO);
            }
            if ((t - WHIRL_FROM) % 5 == 0) {
                Vec3 c = boss.position();
                for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(r + 1.0, 7.0, r + 1.0), boss::canStrike)) {
                    double dy = e.getY() - c.y;
                    if (DivineFx.flat(e.position(), c) > r + e.getBbWidth() * 0.5 || dy < -2.0 || dy > boss.getBbHeight()
                            || boss.hitCooldown.containsKey(e.getUUID())) {
                        continue;
                    }
                    boss.hitCooldown.put(e.getUUID(), 9);
                    if (boss.strike(level, e, 10.0f, false)) {
                        SchrolliEntity.knock(e, c, 0.9, 0.3);
                    }
                }
                DivineFx.ring(level, ParticleTypes.END_ROD, c.add(0.0, 2.6 * boss.getScale(), 0.0), r * 0.8, 22);
                DivineFx.ring(level, boss.isTrueForm() ? DivineFx.VIOLET_DUST : DivineFx.GOLD_DUST, c.add(0.0, 2.4 * boss.getScale(), 0.0), r * 0.9, 28);
                DivineFx.ring(level, ParticleTypes.SWEEP_ATTACK, c.add(0.0, 2.5 * boss.getScale(), 0.0), r * 0.75, 6);
            }
            if ((t - WHIRL_FROM) % 12 == 0) {
                boss.sound(level, ModSounds.SCHROLLI_WHIRL.get(), 2.5f, 1.0f);
            }
            return;
        }
        if (t == WHIRL_TO) {
            Vec3 g = boss.ground(boss.position());
            Set<UUID> hit = new HashSet<>();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, g.add(0.0, 0.1, 0.0), boss.isTrueForm() ? DivineFx.COSMIC : DivineFx.GOLD, 30, 2.0f)
                    .param(0.6f).onTick(fx -> ringHits(boss, level, fx, g.y, 10.0f, false, hit)).spawn();
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 3.0f, 0.9f);
            DivineFx.flash(level, g.add(0.0, 1.0, 0.0), DivineFx.WHITE);
            dust(boss, level, g, 2.5, 14);
        }
        boss.walk(Vec3.ZERO);
        if (target != null) {
            boss.face(target.position(), 6.0f);
        }
    }

    /**
     * Himmelssturz: mit einem Schwingenschlag springt er hoch hinauf, hängt einen Augenblick über seinem Ziel – ein
     * Warnkreis zeigt, wo er einschlägt – und stürzt mit der Klinge voran hinab. Danach steckt die Klinge im Boden.
     */
    private static void plunge(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        int impact = SchrolliEntity.PLUNGE_IMPACT;
        boolean truly = boss.isTrueForm();
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 2.5f, 0.9f);
        }
        if (t < PLUNGE_LEAP) {
            boss.walk(Vec3.ZERO);
            if (target != null) {
                boss.face(target.position(), 12.0f);
            }
            return;
        }
        if (t == PLUNGE_LEAP) {
            Vec3 start = boss.position();
            Vec3 over = target != null ? target.position() : start.add(boss.forward().scale(8.0));
            boss.aim = start;
            boss.points.clear();
            boss.points.add(new Vec3(Mth.lerp(0.6, start.x, over.x), start.y + 14.0, Mth.lerp(0.6, start.z, over.z)));
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 4.0f, 0.7f);
            dust(boss, level, start, 2.5, 14);
            DivineFx.ring(level, ParticleTypes.CLOUD, start.add(0.0, 0.3, 0.0), 3.0, 16);
        }
        Vec3 apex = boss.points.isEmpty() ? boss.position() : boss.points.getFirst();
        if (t > PLUNGE_LEAP && t <= PLUNGE_APEX) {
            float k = (t - PLUNGE_LEAP) / (float) (PLUNGE_APEX - PLUNGE_LEAP);
            float ease = 1.0f - (1.0f - k) * (1.0f - k);
            Vec3 p = boss.aim.lerp(apex, ease);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            if (target != null) {
                boss.face(target.position(), 10.0f);
            }
        }
        if (t == PLUNGE_APEX - 6) {
            boss.mark = inCourt(boss, target != null ? lead(boss, target, 14) : boss.ground(apex));
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.mark.add(0.0, 0.06, 0.0), DivineFx.AMBER, impact - t, truly ? 6.5f : 5.5f).spawn();
        }
        if (t > PLUNGE_APEX && t < PLUNGE_FALL) {
            Vec3 p = apex.add(0.0, Math.sin((t - PLUNGE_APEX) * 0.4) * 0.3, 0.0);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            boss.face(boss.mark, 14.0f);
            if (t % 2 == 0) {
                DivineFx.gather(level, boss.local(0.0, 7.0, 0.6), 4.0, 4);
            }
        }
        if (t >= PLUNGE_FALL && t <= impact) {
            float k = (t - PLUNGE_FALL) / (float) (impact - PLUNGE_FALL);
            Vec3 p = apex.lerp(boss.mark, k * k);
            boss.setPos(p.x, p.y, p.z);
            boss.setDeltaMovement(Vec3.ZERO);
            level.sendParticles(ParticleTypes.END_ROD, p.x, p.y + 4.0, p.z, 6, 0.6, 2.0, 0.6, 0.05);
        }
        if (t == impact) {
            Vec3 m = boss.mark;
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 5.0f, 0.7f);
            boss.sound(level, ModSounds.SCHROLLI_LAND.get(), 4.0f, 0.85f);
            boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 3.0f, 0.8f);
            DivineFx.flash(level, m.add(0.0, 1.0, 0.0), DivineFx.WHITE);
            DivineFx.flash(level, m.add(0.0, 1.5, 0.0), truly ? DivineFx.COSMIC : DivineFx.GOLD);
            DivineFx.sparks(level, m.add(0.0, 0.5, 0.0), 80, 2.0, 0.3);
            level.sendParticles(ParticleTypes.EXPLOSION, m.x, m.y + 0.5, m.z, 3, 1.4, 0.2, 1.4, 0.0);
            dust(boss, level, m, 3.5, 20);
            double radius = truly ? 6.5 : 5.5;
            for (LivingEntity e : boss.enemiesNear(level, m.add(0.0, 1.0, 0.0), radius)) {
                if (boss.strike(level, e, 26.0f, true)) {
                    SchrolliEntity.knock(e, m, 1.2, 0.9);
                }
            }
            Set<UUID> hit = new HashSet<>();
            DivineFx.fx(level, SpellFxEntity.Kind.SHOCKWAVE, m.add(0.0, 0.1, 0.0), truly ? DivineFx.COSMIC : DivineFx.GOLD, 40, 2.5f).param(0.6f)
                    .onTick(fx -> ringHits(boss, level, fx, m.y, 10.0f, false, hit)).spawn();
            int cracks = truly ? 8 : 6;
            for (int k = 0; k < cracks; k++) {
                Vec3 end = m.add(DivineFx.dir(boss.getYRot() + k * 360.0 / cracks).scale(9.0));
                DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, m.add(0.0, 0.05, 0.0), truly ? DivineFx.COSMIC : DivineFx.GOLD, 50, 1.0f).param(0.0f)
                        .end(end).spawn();
            }
            int pillars = truly ? 10 : 6;
            for (int k = 0; k < pillars; k++) {
                Vec3 at = boss.ground(m.add(DivineFx.dir(k * 360.0 / pillars + 15.0).scale(8.5)));
                warnPillar(boss, level, at, 1.8f, 16, 14.0f, truly ? DivineFx.COSMIC : DivineFx.HOLY, 14.0f);
            }
            boss.tell(level, Component.translatable("entity.kingdomsatwar.schrolli.stuck").withColor(0xFFD24A));
        }
        if (t > impact) {
            boss.walk(Vec3.ZERO);
            if (t > SchrolliEntity.PLUNGE_STUCK && target != null) {
                boss.face(target.position(), 6.0f);
            }
            if (t < SchrolliEntity.PLUNGE_STUCK && t % 4 == 0) {
                Vec3 blade = boss.local(0.3, 0.4, 2.4);
                level.sendParticles(ParticleTypes.END_ROD, blade.x, blade.y, blade.z, 2, 0.2, 0.3, 0.2, 0.04);
            }
        }
    }

    /**
     * Schwingenwehr: die vorderen Schwingen schließen sich wie ein Schild vor ihm. Was von vorn kommt, pariert er (und
     * schlägt zurück – der Gegenschlag); von hinten ist er ungeschützt. Kommt niemand, stößt er die Schwingen zuletzt nach vorn.
     */
    private static void guard(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_GUARD.get(), 3.0f, 1.0f);
            boss.tell(level, Component.translatable("entity.kingdomsatwar.schrolli.guard").withColor(0xFFE89A));
        }
        boss.walk(Vec3.ZERO);
        if (target != null) {
            boss.face(target.position(), 2.5f);
        }
        if (t % 4 == 0 && t < Attack.GUARD.duration - 8) {
            double s = boss.getScale();
            for (int k = -3; k <= 3; k++) {
                Vec3 p = boss.position().add(DivineFx.dir(boss.getYRot() + k * 16.0).scale(2.8 * s)).add(0.0, (1.2 + Math.abs(k) * 0.5) * s, 0.0);
                level.sendParticles(ParticleTypes.END_ROD, p.x, p.y, p.z, 1, 0.1, 0.4, 0.1, 0.0);
            }
        }
        if (t == Attack.GUARD.duration - 8) {
            boss.sound(level, ModSounds.SCHROLLI_WINGS.get(), 3.5f, 0.8f);
            cut(boss, level, boss.getYRot(), reach(boss) * 0.9, 70.0, 8.0f, true, 1.6, 0.4);
            Vec3 f = boss.position().add(boss.forward().scale(3.0 * boss.getScale())).add(0.0, 2.0, 0.0);
            level.sendParticles(ParticleTypes.CLOUD, f.x, f.y, f.z, 30, 1.5, 1.0, 1.5, 0.15);
            level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.DIVINE_FEATHER.get(), f.x, f.y + 1.0, f.z, 16, 2.0, 1.0, 2.0, 0.2);
        }
    }

    /** Gegenschlag: nach der Parade – blitzschnell zum Angreifer und ein weiter Hieb (golden: kein Schild hilft). */
    private static void counter(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        LivingEntity foe = boss.marked != null && level.getEntity(boss.marked) instanceof LivingEntity e && e.isAlive() ? e : target;
        if (foe != null && t <= COUNTER_HIT) {
            double d = DivineFx.flat(boss.position(), foe.position());
            boss.walk(toward(boss, foe.position()).scale(d > reach(boss) * 0.6 ? 0.6 : 0.0));
            boss.face(foe.position(), t == 1 ? 360.0f : 30.0f);
        } else {
            boss.walk(Vec3.ZERO);
        }
        if (t == COUNTER_HIT - 3) {
            boss.sound(level, ModSounds.SCHROLLI_SWING.get(), 2.5f, 1.0f);
        }
        if (t == COUNTER_HIT) {
            double yaw = boss.getYRot();
            cut(boss, level, yaw, reach(boss) + 0.5, 110.0, 20.0f, true, 1.6, 0.5);
            slashArc(boss, level, yaw, -100.0, 100.0, reach(boss) * 0.75, 3.0, 3.5);
            boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 3.0f, 0.9f);
            DivineFx.flash(level, boss.local(0.0, 3.0, 2.0), DivineFx.WHITE);
        }
    }

    /**
     * Weltenklinge (Wahre Gestalt): er reckt die Klinge zum Himmel, und sie wächst zu einer Klinge aus Weltall. Ein Rundschlag
     * fegt dicht über den Boden um ihn (springen!), dann spaltet ein Hieb den Hof der Länge nach – eine Linie zeigt, wo
     * (ausweichen!) –, und aus dem Riss bricht Licht.
     */
    private static void worldBlade(SchrolliEntity boss, ServerLevel level, @Nullable LivingEntity target, int t) {
        if (t == 1) {
            boss.sound(level, ModSounds.SCHROLLI_CHARGE.get(), 4.0f, 0.6f);
            boss.sayOnce(level, "world_blade");
        }
        if (t == WORLD_GROW) {
            boss.sound(level, ModSounds.SCHROLLI_BLADE.get(), 4.0f, 0.7f);
        }
        if (t == 16) {
            DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, boss.ground(boss.position()).add(0.0, 0.06, 0.0), DivineFx.AMBER, WORLD_SWEEP - 16,
                    (float) WORLD_REACH).spawn();
        }
        if (t < WORLD_SWEEP) {
            boss.walk(Vec3.ZERO);
            if (target != null) {
                boss.face(target.position(), 6.0f);
            }
            if (t > WORLD_GROW && t % 2 == 0) {
                DivineFx.gather(level, boss.local(0.6, 14.0, 0.8), 5.0, 4);
            }
            return;
        }
        Vec3 c = boss.position();
        if (t == WORLD_SWEEP) {
            boss.sound(level, ModSounds.SCHROLLI_BEAM.get(), 3.0f, 0.7f);
            boss.struck.clear();
        }
        if (t < WORLD_SWEEP_END) {
            boss.turnTo(boss.getYRot() + 20.0f, 360.0f);
            boss.walk(Vec3.ZERO);
            double yaw = boss.getYRot();
            Vec3 fwd = DivineFx.dir(yaw);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(c, c).inflate(WORLD_REACH + 1.0, 4.0, WORLD_REACH + 1.0),
                    boss::canStrike)) {
                Vec3 to = e.position().subtract(c).multiply(1.0, 0.0, 1.0);
                double d = to.length();
                if (d > WORLD_REACH || boss.struck.contains(e.getUUID()) || e.getY() > c.y + 1.0) {
                    continue;                           // zu weit – oder drübergesprungen
                }
                if (d > 2.0 && to.scale(1.0 / d).dot(fwd) < Math.cos(Math.toRadians(26.0))) {
                    continue;
                }
                boss.struck.add(e.getUUID());
                if (boss.strike(level, e, 20.0f, true)) {
                    SchrolliEntity.knock(e, c, 1.0, 0.5);
                }
            }
            for (double d = 3.0; d <= WORLD_REACH; d += 1.5) {
                Vec3 p = c.add(fwd.scale(d)).add(0.0, 1.0, 0.0);
                level.sendParticles(d % 3.0 < 1.5 ? ParticleTypes.END_ROD : DivineFx.VIOLET_DUST, p.x, p.y, p.z, 1, 0.1, 0.1, 0.1, 0.0);
            }
            if ((t - WORLD_SWEEP) % 6 == 0) {
                boss.sound(level, ModSounds.SCHROLLI_WHIRL.get(), 3.0f, 0.8f);
            }
            return;
        }
        boss.walk(Vec3.ZERO);
        if (t < WORLD_WARN && target != null) {
            boss.face(target.position(), 10.0f);
        }
        if (t == WORLD_WARN) {
            Vec3 a = boss.ground(c);
            Vec3 b = boss.ground(c.add(boss.forward().scale(WORLD_CHOP_LENGTH)));
            boss.aim = a;
            boss.mark = b;
            DivineFx.fx(level, SpellFxEntity.Kind.BEAM, a.add(0.0, 0.15, 0.0), DivineFx.AMBER, WORLD_CHOP - WORLD_WARN, 0.18f).end(b.add(0.0, 0.15, 0.0)).spawn();
            boss.sound(level, ModSounds.SCHROLLI_SWING.get(), 3.0f, 0.7f);
        }
        if (t == WORLD_CHOP) {
            Vec3 a = boss.aim;
            Vec3 b = boss.mark;
            boss.sound(level, ModSounds.SCHROLLI_SLASH.get(), 4.0f, 0.7f);
            boss.sound(level, ModSounds.SCHROLLI_IMPACT.get(), 5.0f, 0.6f);
            for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, new AABB(a, b).inflate(3.0, 4.0, 3.0), boss::canStrike)) {
                if (DivineFx.distanceToSegment(new Vec3(e.getX(), a.y, e.getZ()), a, b) < 1.9 && e.getY() - a.y < 3.0
                        && boss.strike(level, e, 24.0f, true)) {
                    Vec3 dir = b.subtract(a);
                    Vec3 side = new Vec3(-dir.z, 0.0, dir.x).normalize();
                    double sign = Math.signum(e.position().subtract(a).dot(side));
                    e.push(side.x * (sign == 0 ? 1 : sign) * 1.2, 0.6, side.z * (sign == 0 ? 1 : sign) * 1.2);
                    e.needsSync = true;
                }
            }
            DivineFx.fx(level, SpellFxEntity.Kind.FISSURE, a.add(0.0, 0.05, 0.0), DivineFx.COSMIC, 60, 1.6f).param(WORLD_ERUPT - WORLD_CHOP).end(b).spawn();
            Vec3 hit = a.add(b.subtract(a).normalize().scale(reach(boss)));
            DivineFx.flash(level, hit.add(0.0, 1.0, 0.0), DivineFx.WHITE);
            DivineFx.flash(level, hit.add(0.0, 1.5, 0.0), DivineFx.COSMIC);
            DivineFx.sparks(level, hit.add(0.0, 0.5, 0.0), 60, 1.6, 0.3);
            dust(boss, level, hit, 2.5, 16);
        }
        if (t == WORLD_ERUPT) {
            Vec3 a = boss.aim;
            Vec3 b = boss.mark;
            double len = DivineFx.flat(a, b);
            for (double d = 4.0; d <= len; d += 3.0) {
                pillar(boss, level, boss.ground(a.lerp(b, d / len)), 1.8f, 16.0f, 14.0f, d % 6.0 < 3.0 ? DivineFx.COSMIC : DivineFx.WHITE);
            }
        }
    }
}
