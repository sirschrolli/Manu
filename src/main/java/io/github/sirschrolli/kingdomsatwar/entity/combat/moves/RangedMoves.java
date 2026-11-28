package io.github.sirschrolli.kingdomsatwar.entity.combat.moves;

import io.github.sirschrolli.kingdomsatwar.entity.combat.Family;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Fight;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Move;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutschleim;
import io.github.sirschrolli.kingdomsatwar.entity.monster.MonsterShot;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.monster.Enderman;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.hurtingprojectile.LargeFireball;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.BowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Die Fernkampf-Angriffe der Monster (und das Ausweichen, Huschen und Entrücken). */
public final class RangedMoves {
    /** Grundschaden eines Pfeils (wie bei Vanilla). */
    static final double ARROW_DAMAGE = 2.0;

    private RangedMoves() {}

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    static boolean crossbow(Mob mob) {
        return mob.getMainHandItem().getItem() instanceof CrossbowItem || mob.getOffhandItem().getItem() instanceof CrossbowItem;
    }

    static boolean bow(Mob mob) {
        return mob.getMainHandItem().getItem() instanceof BowItem || mob.getOffhandItem().getItem() instanceof BowItem;
    }

    static InteractionHand hand(Mob mob) {
        return mob.getMainHandItem().getItem() instanceof BowItem || mob.getMainHandItem().getItem() instanceof CrossbowItem
                ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
    }

    /** Spannt den Bogen (oder lädt die Armbrust) – die Haltung dazu. */
    static void draw(Mob mob, boolean on) {
        if (on) {
            mob.setAggressive(true);
            if (bow(mob) || crossbow(mob)) {
                mob.startUsingItem(hand(mob));
            }
            if (mob instanceof CrossbowAttackMob loader) {
                loader.setChargingCrossbow(true);
            }
        } else {
            mob.stopUsingItem();
            if (mob instanceof CrossbowAttackMob loader) {
                loader.setChargingCrossbow(false);
            }
        }
    }

    /**
     * Ein Pfeil (oder Bolzen), wie ihn das Monster schießt – um {@code yawOffset} Grad zur Seite gedreht, {@code speed}
     * mal so schnell und {@code damage} mal so stark. Gerippe schießen ihre eigenen Pfeile (Frost, Gift …).
     */
    static List<AbstractArrow> arrows(Fight f, LivingEntity target, float yawOffset, float speed, float damage) {
        Mob mob = f.mob;
        ServerLevel level = f.level();
        List<AbstractArrow> fired = new ArrayList<>();
        if (mob instanceof RangedAttackMob ranged && bow(mob)) {
            ranged.performRangedAttack(target, 1.0f);
            for (AbstractArrow arrow : level.getEntitiesOfClass(AbstractArrow.class, mob.getBoundingBox().inflate(3.0),
                    a -> a.getOwner() == mob && a.tickCount == 0 && !f.hit.contains(a.getId()))) {
                f.hit.add(arrow.getId());
                fired.add(arrow);
            }
        } else {
            ItemStack weapon = mob.getMainHandItem();
            AbstractArrow arrow = ProjectileUtil.getMobArrow(mob, new ItemStack(Items.ARROW), 1.0f, weapon);
            Vec3 eye = mob.getEyePosition().subtract(0.0, 0.1, 0.0);
            arrow.setPos(eye.x, eye.y, eye.z);
            double dx = target.getX() - eye.x;
            double dz = target.getZ() - eye.z;
            double dy = target.getY(0.33) - eye.y + Math.sqrt(dx * dx + dz * dz) * 0.2;
            arrow.shoot(dx, dy, dz, 1.6f, 2.0f);
            level.addFreshEntity(arrow);
            f.sound(SoundEvents.CROSSBOW_SHOOT, 1.0f, 1.0f / (f.random().nextFloat() * 0.4f + 0.8f));
            f.hit.add(arrow.getId());
            fired.add(arrow);
        }
        for (AbstractArrow arrow : fired) {
            Vec3 v = arrow.getDeltaMovement().yRot((float) Math.toRadians(-yawOffset)).scale(speed);
            arrow.setDeltaMovement(v);
            if (damage != 1.0f) {
                arrow.setBaseDamage(ARROW_DAMAGE * damage);
            }
        }
        return fired;
    }

    static Vec3 mouth(Mob mob) {
        return mob.getEyePosition().add(Vec3.directionFromRotation(0.0f, mob.getYHeadRot()).scale(mob.getBbWidth() * 0.5 + 0.2));
    }

    /** Wurf im Bogen: Geschwindigkeit, um in etwa {@code ticks} Ticks anzukommen (Schwerkraft der Monstergeschosse). */
    static Vec3 lob(Vec3 from, Vec3 to, int ticks) {
        return MonsterShot.lob(from, to, ticks, 0.04);
    }

    /** Stellt ein Monster an einen neuen Ort (sucht festen Boden darunter). */
    static boolean teleport(Mob mob, Vec3 at) {
        for (int i = 0; i < 6; i++) {
            double x = at.x + (i == 0 ? 0.0 : (mob.getRandom().nextDouble() - 0.5) * 3.0);
            double z = at.z + (i == 0 ? 0.0 : (mob.getRandom().nextDouble() - 0.5) * 3.0);
            if (mob.randomTeleport(x, at.y + 2.0, z, false, (BlockState state) -> false)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================

    /** Salve: spannt länger als sonst – dann drei Pfeile (Bolzen, Feuerkugeln) auf einmal im Fächer. */
    public static final class Volley extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            double min = f.family == Family.QUALLE ? 8.0 : 4.5;
            double max = f.family == Family.QUALLE ? 44.0 : 24.0;
            return dist >= min && dist <= max && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return f.family == Family.QUALLE ? 22 : 18;
        }

        @Override
        public void begin(Fight f) {
            draw(f.mob, true);
            if (f.mob instanceof Ghast ghast) {
                ghast.setCharging(true);
                f.sound(SoundEvents.GHAST_WARN, 3.0f, 0.8f);
            } else if (f.family == Family.GLUTGEIST) {
                f.sound(SoundEvents.BLAZE_BURN, 1.5f, 0.7f);
            } else if (crossbow(f.mob)) {
                f.sound(SoundEvents.CROSSBOW_LOADING_MIDDLE, 1.0f, 0.8f);
            }
        }

        @Override
        public void warn(Fight f, int t) {
            Mob mob = f.mob;
            if (f.family == Family.GLUTGEIST) {
                double a = t * 0.7;
                Vec3 c = mob.position().add(0.0, mob.getBbHeight() * 0.5, 0.0);
                f.level().sendParticles(ParticleTypes.FLAME, c.x + Math.cos(a) * 0.9, c.y, c.z + Math.sin(a) * 0.9, 1, 0.0, 0.05, 0.0, 0.01);
                return;
            }
            if (f.family == Family.QUALLE) {
                if (t % 2 == 0) {
                    f.particles(ParticleTypes.LARGE_SMOKE, mouth(mob), 2, 0.4, 0.02);
                }
                return;
            }
            if (t % 3 == 0) {
                f.particles(ParticleTypes.CRIT, mouth(mob), 2, 0.12, 0.05);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            ServerLevel level = f.level();
            if (f.family == Family.GLUTGEIST) {
                Vec3 from = new Vec3(mob.getX(), mob.getY(0.5) + 0.5, mob.getZ());
                Vec3 dir = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(from).normalize();
                for (float off : new float[] {-16.0f, -8.0f, 0.0f, 8.0f, 16.0f}) {
                    Vec3 d = dir.yRot((float) Math.toRadians(off));
                    SmallFireball ball = new SmallFireball(level, mob, d);
                    ball.setPos(from.x, from.y, from.z);
                    level.addFreshEntity(ball);
                }
                f.sound(SoundEvents.BLAZE_SHOOT, 1.4f, 0.8f);
                return false;
            }
            if (mob instanceof Ghast ghast) {
                Vec3 view = ghast.getViewVector(1.0f);
                Vec3 from = new Vec3(ghast.getX() + view.x * 4.0, ghast.getY(0.5) + 0.5, ghast.getZ() + view.z * 4.0);
                Vec3 dir = target.position().add(0.0, target.getBbHeight() * 0.5, 0.0).subtract(from).normalize();
                for (float off : new float[] {-14.0f, 0.0f, 14.0f}) {
                    LargeFireball ball = new LargeFireball(level, ghast, dir.yRot((float) Math.toRadians(off)), 1);
                    ball.setPos(from.x, from.y, from.z);
                    level.addFreshEntity(ball);
                }
                f.sound(SoundEvents.GHAST_SHOOT, 3.0f, 0.9f);
                ghast.setCharging(false);
                return false;
            }
            for (float off : new float[] {-10.0f, 0.0f, 10.0f}) {
                arrows(f, target, off, 1.0f, 1.0f);
            }
            draw(mob, false);
            return false;
        }

        @Override
        public void end(Fight f) {
            draw(f.mob, false);
            if (f.mob instanceof Ghast ghast) {
                ghast.setCharging(false);
            }
        }

        @Override
        public int recover(Fight f) {
            return 10;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 140 + random.nextInt(90);
        }
    }

    /**
     * Zielschuss: zielt lange – eine Linie aus Licht zeigt, wohin. Kurz vor dem Schuss erstarrt die Linie und färbt sich
     * rot: Wer dann noch darauf steht, wird von einem schnellen, harten Schuss getroffen.
     */
    public static final class Aimed extends Move {
        public static final int WINDUP = 34;
        /** So lange vor dem Schuss steht das Ziel fest (Ticks). */
        public static final int LOCK = 9;

        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 7.0 && dist <= 30.0 && MeleeMoves.sees(f, f.target);
        }

        @Override
        public float weight(Fight f, double dist) {
            return dist > 12.0 ? 1.8f : 1.0f;
        }

        @Override
        public int windup(Fight f) {
            return WINDUP;
        }

        @Override
        public boolean tracks(Fight f, int t) {
            return t < WINDUP - LOCK;
        }

        @Override
        public void begin(Fight f) {
            draw(f.mob, true);
            f.b = aimPoint(f.target);
        }

        static Vec3 aimPoint(LivingEntity target) {
            return target.position().add(0.0, target.getBbHeight() * 0.55, 0.0);
        }

        @Override
        public void warn(Fight f, int t) {
            boolean locked = t >= WINDUP - LOCK;
            if (!locked && f.target != null) {
                f.b = aimPoint(f.target);
            }
            if (t == WINDUP - LOCK) {
                f.sound(SoundEvents.CROSSBOW_LOADING_END, 1.0f, 1.4f);
            }
            if (t % 2 == 0 || locked) {
                Vec3 from = mouth(f.mob);
                Vec3 to = f.b;
                ParticleOptions dust = new DustParticleOptions(locked ? 0xFF2A1A : f.family.color(), locked ? 0.9f : 0.55f);
                double len = from.distanceTo(to);
                Vec3 dir = to.subtract(from).normalize();
                for (double s = 0.6; s < len; s += locked ? 0.45 : 0.8) {
                    Vec3 p = from.add(dir.scale(s));
                    f.level().sendParticles(dust, p.x, p.y, p.z, 1, 0.0, 0.0, 0.0, 0.0);
                }
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            Mob mob = f.mob;
            for (AbstractArrow arrow : arrows(f, target, 0.0f, 1.0f, 1.9f)) {
                Vec3 from = arrow.position();
                Vec3 d = f.b.subtract(from);
                double flat = Math.sqrt(d.x * d.x + d.z * d.z);
                arrow.shoot(d.x, d.y + flat * 0.045, d.z, 3.0f, 0.0f);
                arrow.setCritArrow(true);
            }
            f.sound(SoundEvents.ARROW_SHOOT, 1.2f, 0.6f);
            draw(mob, false);
            return false;
        }

        @Override
        public void end(Fight f) {
            draw(f.mob, false);
        }

        @Override
        public int recover(Fight f) {
            return 12;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 200 + random.nextInt(100);
        }
    }

    /** Rückzugssprung: kommt man zu nahe, springt es mit einem Satz nach hinten (Gerippe schießen noch im Landen). */
    public static final class Backstep extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 4.5 && f.mob.onGround();
        }

        @Override
        public float weight(Fight f, double dist) {
            return dist < 3.0 ? 2.0f : 1.0f;
        }

        @Override
        public int windup(Fight f) {
            return 4;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            if (t == 0) {
                Vec3 away = mob.position().subtract(f.target.position()).multiply(1.0, 0.0, 1.0);
                if (away.lengthSqr() < 1.0e-4) {
                    away = f.forward().scale(-1.0);
                }
                away = away.normalize().yRot((f.random().nextFloat() - 0.5f) * 0.8f);
                mob.setDeltaMovement(away.x * 0.78, 0.44, away.z * 0.78);
                mob.needsSync = true;
                MeleeMoves.groundDust(f, mob.position(), 10, 0.3, 0.1);
                f.sound(SoundEvents.GOAT_LONG_JUMP, 0.6f, 1.5f);
                return true;
            }
            if (t > 3 && mob.onGround()) {
                if (f.family == Family.GEBEINE && f.target != null && f.target.isAlive() && mob.distanceTo(f.target) < 20.0
                        && MeleeMoves.sees(f, f.target)) {
                    // schießt noch im Landen
                    f.face(true);
                    arrows(f, f.target, 0.0f, 1.0f, 1.0f);
                }
                return false;
            }
            return t < 22;
        }

        @Override
        public int recover(Fight f) {
            return 4;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 100 + random.nextInt(80);
        }
    }

    /** Speien: reckt den Kopf zurück – dann fliegt ein Netz (Schleim, Glut) auf das Ziel. */
    public static final class Spit extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 2.5 && dist <= 14.0 && MeleeMoves.sees(f, f.target) && Family.big(f.mob);
        }

        @Override
        public float weight(Fight f, double dist) {
            return dist > 5.0 ? 1.6f : 0.8f;
        }

        @Override
        public int windup(Fight f) {
            return 12;
        }

        static MonsterShot.Kind kind(Fight f) {
            if (f.family == Family.SPINNE) {
                return MonsterShot.Kind.WEB;
            }
            return f.mob instanceof Glutschleim ? MonsterShot.Kind.MAGMA : MonsterShot.Kind.SLIME;
        }

        @Override
        public void begin(Fight f) {
            f.sound(f.family == Family.SPINNE ? SoundEvents.SPIDER_AMBIENT : SoundEvents.SLIME_SQUISH_SMALL, 1.0f, 0.7f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 2 == 0) {
                ParticleOptions p = switch (kind(f)) {
                    case WEB -> new DustParticleOptions(0xF0F0F0, 0.7f);
                    case MAGMA -> ParticleTypes.FLAME;
                    default -> ParticleTypes.ITEM_SLIME;
                };
                f.particles(p, mouth(f.mob), 2, 0.1, 0.01);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            Vec3 from = mouth(f.mob);
            Vec3 to = target.position().add(0.0, target.getBbHeight() * 0.4, 0.0).add(target.getDeltaMovement().scale(4.0));
            int ticks = Mth.clamp((int) (from.distanceTo(to) * 1.1) + 6, 8, 24);
            MonsterShot.Kind kind = kind(f);
            float damage = switch (kind) {
                case WEB -> 2.0f;
                case MAGMA -> 4.0f;
                default -> 3.0f;
            };
            MonsterShot.shoot(f.level(), f.mob, kind, from, lob(from, to, ticks), damage);
            f.sound(SoundEvents.LLAMA_SPIT, 1.0f, kind == MonsterShot.Kind.WEB ? 0.7f : 0.5f);
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 8;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 140 + random.nextInt(90);
        }
    }

    /** Wurf: holt weit aus – und schleudert einen Brocken (Sand, Schädel, Leerenbrocken) im hohen Bogen. */
    public static final class Throw extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 4.0 && dist <= 16.0 && MeleeMoves.sees(f, f.target);
        }

        @Override
        public float weight(Fight f, double dist) {
            return dist > 6.0 ? 1.8f : 0.6f;
        }

        @Override
        public int windup(Fight f) {
            return f.family == Family.HOEHLENTROLL ? 18 : 14;
        }

        static MonsterShot.Kind kind(Fight f) {
            Mob mob = f.mob;
            if (mob instanceof Mummy) {
                return MonsterShot.Kind.SAND;
            }
            if (mob instanceof DrownedCorpse) {
                return MonsterShot.Kind.MUD;
            }
            return switch (f.family) {
                case BRANDGEBEIN -> MonsterShot.Kind.SKULL;
                case LEERWANDLER -> MonsterShot.Kind.VOID;
                default -> MonsterShot.Kind.ROCK;
            };
        }

        @Override
        public void begin(Fight f) {
            f.mob.playAmbientSound();
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 3 == 0) {
                Mob mob = f.mob;
                Vec3 hand = mob.position().add(f.forward().scale(-0.3)).add(0.0, mob.getBbHeight() * 0.95, 0.0);
                ParticleOptions p = switch (kind(f)) {
                    case SAND -> new DustParticleOptions(0xD8C38A, 1.0f);
                    case SKULL -> ParticleTypes.SMOKE;
                    case VOID -> ParticleTypes.PORTAL;
                    case MUD -> new DustParticleOptions(0x4A3A2A, 1.0f);
                    default -> new DustParticleOptions(0x8A8A80, 1.0f);
                };
                f.particles(p, hand, 3, 0.15, 0.02);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            Mob mob = f.mob;
            Vec3 from = mob.position().add(f.forward().scale(0.4)).add(0.0, mob.getBbHeight() * 0.9, 0.0);
            Vec3 to = target.position().add(0.0, 0.4, 0.0).add(target.getDeltaMovement().scale(5.0));
            int ticks = Mth.clamp((int) (from.distanceTo(to) * 1.3) + 8, 10, 30);
            MonsterShot.Kind kind = kind(f);
            float damage = switch (f.family) {
                case HOEHLENTROLL -> 7.0f;
                case LEERWANDLER -> 5.0f;
                default -> kind == MonsterShot.Kind.SAND || kind == MonsterShot.Kind.MUD ? 3.0f : 4.0f;
            };
            if (kind == MonsterShot.Kind.VOID && mob instanceof Enderman enderman && enderman.getCarriedBlock() != null) {
                ItemStack carried = new ItemStack(enderman.getCarriedBlock().getBlock());
                enderman.setCarriedBlock(null);
                MonsterShot.shoot(f.level(), mob, kind, from, lob(from, to, ticks), damage, carried.isEmpty() ? new ItemStack(Items.END_STONE) : carried);
            } else {
                MonsterShot.shoot(f.level(), mob, kind, from, lob(from, to, ticks), damage);
            }
            f.sound(SoundEvents.WITCH_THROW, 1.0f, 0.6f);
            return false;
        }

        @Override
        public int recover(Fight f) {
            return 10;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 160 + random.nextInt(90);
        }
    }

    /** Trankhagel (Hexe): drei Tränke rasch hintereinander. */
    public static final class Barrage extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist >= 3.0 && dist <= 14.0 && MeleeMoves.sees(f, f.target);
        }

        @Override
        public int windup(Fight f) {
            return 14;
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.WITCH_CELEBRATE, 1.0f, 1.2f);
        }

        @Override
        public void warn(Fight f, int t) {
            if (t % 2 == 0) {
                f.particles(ParticleTypes.WITCH, f.mob.position().add(0.0, f.mob.getBbHeight() + 0.2, 0.0), 3, 0.3, 0.05);
            }
        }

        @Override
        public boolean act(Fight f, int t) {
            if (t % 6 == 0 && f.target != null && f.target.isAlive() && f.mob instanceof RangedAttackMob ranged) {
                f.face(true);
                ranged.performRangedAttack(f.target, 1.0f);
            }
            return t < 12;
        }

        @Override
        public int recover(Fight f) {
            return 10;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 180 + random.nextInt(90);
        }
    }

    /** Huschen: blitzschnell zur Seite – und aus einer anderen Richtung wieder heran. */
    public static final class Skitter extends Move {
        @Override
        public boolean ready(Fight f, double dist) {
            return dist <= 6.0 && f.mob.onGround();
        }

        @Override
        public int windup(Fight f) {
            return 3;
        }

        @Override
        public void begin(Fight f) {
            f.d = f.random().nextBoolean() ? 1.0 : -1.0;
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            Vec3 to = target.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0).normalize();
            Vec3 side = new Vec3(-to.z * f.d, 0.0, to.x * f.d);
            if (t == 0) {
                mob.setDeltaMovement(side.x * 0.8 - to.x * 0.2, 0.25, side.z * 0.8 - to.z * 0.2);
                mob.needsSync = true;
                f.sound(SoundEvents.SPIDER_STEP, 1.0f, 1.7f);
                MeleeMoves.groundDust(f, mob.position(), 6, 0.2, 0.1);
            } else if (t == 7) {
                Vec3 in = to.scale(0.7).add(side.scale(-0.3));
                mob.setDeltaMovement(in.x, 0.2, in.z);
                mob.needsSync = true;
                f.sound(SoundEvents.SPIDER_STEP, 1.0f, 1.9f);
            }
            if (t > 7 && mob.distanceTo(target) <= 2.0 && f.hit.isEmpty() && f.strike(target, 0.9f, 0.3, 2.0f)) {
                f.hit.add(target.getId());
            }
            return t < 13;
        }

        @Override
        public int recover(Fight f) {
            return 3;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 100 + random.nextInt(80);
        }
    }

    /**
     * Entrücken: verschwindet in Rauch (Funken, Schatten) und taucht ein Stück entfernt wieder auf – Hexen und Zauberer
     * fliehen so aus der Nähe, Schattenwesen stehen danach neben ihrem Opfer.
     */
    public static final class Blink extends Move {
        static boolean flanker(Fight f) {
            return f.family == Family.SCHATTENPIRSCHER || f.family == Family.ZERRLING;
        }

        @Override
        public boolean ready(Fight f, double dist) {
            return flanker(f) ? dist >= 5.0 && dist <= 14.0 : dist <= 5.5;
        }

        @Override
        public float weight(Fight f, double dist) {
            return flanker(f) ? 1.2f : 2.0f;
        }

        @Override
        public int windup(Fight f) {
            return 9;
        }

        static ParticleOptions smoke(Fight f) {
            return switch (f.family) {
                case HEXE -> ParticleTypes.WITCH;
                case ZERRLING -> ParticleTypes.REVERSE_PORTAL;
                case SCHATTENPIRSCHER -> ParticleTypes.SQUID_INK;
                default -> ParticleTypes.CLOUD;
            };
        }

        @Override
        public void begin(Fight f) {
            f.sound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 0.8f, flanker(f) ? 0.6f : 1.2f);
        }

        @Override
        public void warn(Fight f, int t) {
            Mob mob = f.mob;
            f.level().sendParticles(smoke(f), mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 3, mob.getBbWidth() * 0.5,
                    mob.getBbHeight() * 0.4, mob.getBbWidth() * 0.5, 0.02);
        }

        @Override
        public boolean act(Fight f, int t) {
            Mob mob = f.mob;
            LivingEntity target = f.target;
            if (target == null) {
                return false;
            }
            if (t == 0) {
                Vec3 at;
                Vec3 to = target.position().subtract(mob.position()).multiply(1.0, 0.0, 1.0).normalize();
                if (flanker(f)) {
                    Vec3 side = new Vec3(-to.z, 0.0, to.x).scale(f.random().nextBoolean() ? 1.0 : -1.0);
                    at = target.position().add(side.scale(2.0)).add(to.scale(0.8));
                } else {
                    Vec3 away = to.scale(-1.0).yRot((f.random().nextFloat() - 0.5f) * 1.4f);
                    at = mob.position().add(away.scale(6.0 + f.random().nextDouble() * 3.0));
                }
                Vec3 old = mob.position();
                f.level().sendParticles(ParticleTypes.LARGE_SMOKE, old.x, old.y + mob.getBbHeight() * 0.5, old.z, 16, 0.3, 0.5, 0.3, 0.02);
                f.level().sendParticles(smoke(f), old.x, old.y + mob.getBbHeight() * 0.5, old.z, 20, 0.3, 0.6, 0.3, 0.05);
                if (!teleport(mob, at)) {
                    return false;
                }
                f.level().sendParticles(smoke(f), mob.getX(), mob.getY() + mob.getBbHeight() * 0.5, mob.getZ(), 20, 0.3, 0.6, 0.3, 0.05);
                f.sound(SoundEvents.ILLUSIONER_MIRROR_MOVE, 1.0f, flanker(f) ? 0.7f : 1.3f);
                f.yaw = Fight.yawTo(mob.position(), target.position());
                mob.setYRot(f.yaw);
                mob.setYBodyRot(f.yaw);
                mob.setYHeadRot(f.yaw);
                return flanker(f);
            }
            // Schattenwesen schlagen gleich zu
            if (t == 4 && mob.distanceTo(target) <= 3.0) {
                f.strike(target, 1.1f, 0.5, 3.0f);
            }
            return t < 5;
        }

        @Override
        public int recover(Fight f) {
            return 6;
        }

        @Override
        public int cooldown(RandomSource random) {
            return 170 + random.nextInt(100);
        }
    }
}
