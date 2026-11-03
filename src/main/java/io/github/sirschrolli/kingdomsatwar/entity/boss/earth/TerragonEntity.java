package io.github.sirschrolli.kingdomsatwar.entity.boss.earth;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossPart;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.CommonAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementFx;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Terragon, der Bergbrecher – der Erddrachen-Boss. Flügellos und gepanzert; schläft tief unter dem Felshügel in der
 * Mitte seiner Schlucht und bricht beim Erwachen unter einem Erdbeben daraus hervor.
 *
 * <p>Statt zu fliegen gräbt er sich ein und taucht unter seinen Gegnern wieder auf. Angriffe: Biss, Prankenhieb,
 * Keulenschwung, Brüllen, Staubodem (blendet), Felsbrocken-Wurf, Erdbeben-Stampfer mit Steinschlag, Stalagmitenreihe,
 * Eingraben und Auftauchen, Kristallpanzer (ab Phase 2) und Erdwelle (Phase 3). Sehr viel Leben, langsam.
 */
public class TerragonEntity extends DragonBossEntity {
    /** In diesem Moment des Erwachens bricht der Fels auf. */
    public static final int BURST_TICK = 84;
    private boolean burrowing;
    private int armorTicks;

    public TerragonEntity(EntityType<? extends TerragonEntity> type, Level level) {
        super(type, level, DragonType.EARTH);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(1150.0, 18.0, 0.24);
    }

    @Override
    public boolean canFly() {
        return false;
    }

    public void setBurrowing(boolean burrowing) {
        this.burrowing = burrowing;
    }

    /** Kristallpanzer für {@code ticks} Ticks: fast aller Schaden prallt ab. */
    public void armorUp(int ticks) {
        this.armorTicks = ticks;
        this.setEmpowered(true);
    }

    @Override
    protected float modifyIncomingDamage(DamageSource source, float damage) {
        if (this.burrowing) {
            return 0.0f;
        }
        if (this.armorTicks > 0) {
            if (this.level() instanceof ServerLevel level && this.random.nextInt(3) == 0) {
                BossFx.sound(level, this.position(), SoundEvents.AMETHYST_BLOCK_HIT, 2.0f, 0.6f);
            }
            return damage * 0.3f;
        }
        return damage;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!(this.level() instanceof ServerLevel level) || this.armorTicks <= 0) {
            return;
        }
        this.armorTicks--;
        Vec3 back = this.bodyPoint(0, 5.0, 0);
        if (this.tickCount % 3 == 0) {
            BossFx.burst(level, ParticleTypes.WAX_ON, back, 2, 3.5, 0.02);
        }
        if (this.armorTicks == 0 || this.isDeadOrDying()) {
            this.armorTicks = 0;
            this.setEmpowered(false);
            this.shatterArmor(level);
        }
    }

    /** Der Kristallpanzer zerspringt: Splitter fliegen in alle Richtungen. */
    private void shatterArmor(ServerLevel level) {
        Vec3 c = this.bodyPoint(0, 3.5, 0);
        BossFx.sound(level, c, SoundEvents.AMETHYST_CLUSTER_BREAK, 5.0f, 0.5f);
        BossFx.sound(level, c, SoundEvents.GLASS_BREAK, 4.0f, 0.5f);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.AMBER_CRYSTAL.get().defaultBlockState()), c.x, c.y, c.z,
                120, 4.0, 2.0, 4.0, 0.5);
        BossFx.burst(level, ParticleTypes.WAX_ON, c, 60, 5.0, 0.3);
        for (LivingEntity e : this.enemiesAround(c, 9, 6)) {
            this.hurtTarget(level, e, 10.0f);
            DragonBossEntity.pushAway(e, c, 1.2, 0.4);
        }
    }

    // =====================================================================================
    // Trefferzonen (Kopf, Hals, Schwanz mit Keule)
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 3.6f, 3.2f, 1.5f),
                new BossPart(this, "neck", 3.2f, 3.0f, 1.0f),
                new BossPart(this, "tail1", 2.8f, 2.4f, 0.7f),
                new BossPart(this, "tail2", 2.4f, 2.0f, 0.7f),
                new BossPart(this, "tail3", 2.0f, 1.8f, 0.7f),
                new BossPart(this, "club", 3.0f, 2.8f, 0.6f),
        };
    }

    private double collapse() {
        if (this.getBossState() == State.DYING) {
            return 1.0 - 0.45 * Mth.clamp((this.deathTime - 30) / 50.0, 0.0, 1.0);
        }
        return 1.0;
    }

    @Override
    protected void layoutParts() {
        double reach = this.attackHeadReach();
        double c = this.collapse();
        Vec3 dir = this.headDir();
        Vec3 base = this.bodyPoint(0, 4.8 * c, 3.0);
        BossPart[] p = this.parts;
        setAt(p[0], base.add(dir.scale(4.6 + reach)).add(0, 0.4 * c, 0));
        setAt(p[1], base.add(dir.scale(1.6 + reach * 0.4)).add(0, 0.3 * c, 0));
        this.placePart(p[2], 0, 4.1 * c, -4.6);
        this.placePart(p[3], 0, 3.6 * c, -7.0);
        this.placePart(p[4], 0, 3.2 * c, -9.3);
        this.placePart(p[5], 0, 2.8 * c, -12.4);
    }

    private Vec3 headDir() {
        Vec3 body = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 head = Vec3.directionFromRotation(0, this.yHeadRot);
        Vec3 d = body.scale(0.5).add(head.scale(0.5));
        return d.lengthSqr() < 1.0e-4 ? body : d.normalize();
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    @Override
    public Vec3 mouthPosition() {
        return this.bodyPoint(0, 4.8 * this.collapse(), 3.0).add(this.headDir().scale(6.3 + this.attackHeadReach())).add(0, -0.2, 0);
    }

    @Override
    protected void snortParticles() {
        super.snortParticles();
        Vec3 mouth = this.mouthPosition();
        this.level().addParticle(ModParticles.BOSS_DUST.get(), mouth.x, mouth.y, mouth.z, 0, 0.02, 0);
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected String signatureAttack(int phase) {
        return phase >= 3 ? "stalagmite_row" : "quake";
    }

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx dust = new ElementFx(ModParticles.BOSS_DUST.get(),
                new BlockParticleOption(ParticleTypes.BLOCK, Blocks.SAND.defaultBlockState()),
                ModSounds.DRAGON_BREATH.get(DragonType.EARTH).get(), EarthAttacks::dust);
        list.add(new CommonAttacks.Bite(22, 12.0));
        list.add(new CommonAttacks.Claw(17));
        list.add(new CommonAttacks.TailSweep(18));
        list.add(new CommonAttacks.Roar(22));
        list.add(new ElementAttacks.Breath(dust, 4, 18));
        list.add(new EarthAttacks.BoulderThrow(3, 12));
        list.add(new EarthAttacks.Quake(12));
        list.add(new EarthAttacks.StalagmiteRow(11));
        list.add(new EarthAttacks.Burrow(14));
        list.add(new EarthAttacks.CrystalArmor());
        list.add(new ElementAttacks.NovaRing(dust, 12, 22));
    }

    // =====================================================================================
    // Erwachen: Erdbeben, der Fels bricht auf
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 180;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        Vec3 c = Vec3.atBottomCenterOf(home);
        double depth;
        if (tick < BURST_TICK) {
            depth = 9.0 - 3.0 * Mth.clamp((tick - 20) / 60.0, 0.0, 1.0);
        } else {
            double u = Mth.clamp((tick - BURST_TICK) / 16.0, 0.0, 1.0);
            depth = 6.0 * Math.pow(1.0 - u, 3.0);
        }
        this.setPos(c.x, c.y - depth, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (tick < BURST_TICK) {
            this.quake(level, home, tick);
        } else if (tick == BURST_TICK) {
            this.burst(level, home);
        } else if (tick < 135) {
            if (tick % 2 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), this.getX(),
                        this.getY() + 4.0, this.getZ(), 10, 3.0, 1.5, 3.0, 0.1);
                BossFx.burst(level, ModParticles.BOSS_DUST.get(), this.position().add(0, 3, 0), 3, 4.0, 0.02);
            }
        } else {
            if (tick == 138) {
                BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.4f);
                BossFx.sound(level, this.position(), SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.4f);
                BossFx.sound(level, this.position(), SoundEvents.RAVAGER_ROAR, 5.0f, 0.5f);
                this.shake(level, 90, 1.0f, 35);
                BossFx.burst(level, ModParticles.BOSS_DUST.get(), this.position().add(0, 3, 0), 60, 6.0, 0.08);
            }
            if (tick > 138 && tick % 2 == 0) {
                // Steine bröckeln von den Schluchtwänden
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = 18 + this.random.nextDouble() * 14;
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), c.x + Math.cos(a) * r,
                        c.y + 10 + this.random.nextDouble() * 8, c.z + Math.sin(a) * r, 12, 0.6, 0.6, 0.6, 0.05);
            }
        }
    }

    /** Erdbeben: Risse laufen über den Boden, Staub rieselt, der Fels über dem Schlafplatz bebt. */
    private void quake(ServerLevel level, BlockPos home, int tick) {
        Vec3 c = Vec3.atBottomCenterOf(home);
        if (tick % 2 == 0) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = this.random.nextDouble() * (6 + tick * 0.25);
            BlockPos p = BlockPos.containing(c.x + Math.cos(a) * r, c.y + 3, c.z + Math.sin(a) * r);
            for (int dy = 0; dy >= -6; dy--) {
                BlockPos q = p.above(dy);
                if (!level.getBlockState(q).isAir()) {
                    level.destroyBlockProgress(this.getId() * 64 + (tick / 2) % 48, q, Math.min(9, tick / 8));
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(q)), q.getX() + 0.5, q.getY() + 1.1,
                            q.getZ() + 0.5, 5, 0.4, 0.1, 0.4, 0.08);
                    break;
                }
            }
        }
        if (tick % 4 == 0) {
            for (int i = 0; i < 4; i++) {
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = this.random.nextDouble() * 30;
                level.sendParticles(ModParticles.BOSS_DUST.get(), c.x + Math.cos(a) * r, c.y + 6 + this.random.nextDouble() * 10,
                        c.z + Math.sin(a) * r, 1, 0.5, 0.5, 0.5, 0.0);
            }
        }
        if (tick % 10 == 0) {
            BossFx.sound(level, c, SoundEvents.GRAVEL_BREAK, 3.0f, 0.3f);
            BossFx.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), 1.5f, 0.3f);
        }
        if (tick == 30) {
            BossFx.sound(level, c, ModSounds.DRAGON_AMBIENT.get(), 5.0f, 0.35f);
        }
    }

    /** Der Fels bricht auf: Brocken fliegen in alle Richtungen, Staub steigt auf. */
    private void burst(ServerLevel level, BlockPos home) {
        Vec3 c = Vec3.atBottomCenterOf(home);
        int thrown = 0;
        double radius = 6.5;
        int reach = (int) Math.ceil(radius);
        for (int y = 9; y >= -1; y--) {   // bis über die Bernsteinkrone des Hügels
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    if (dx * dx + dz * dz > radius * radius) {
                        continue;
                    }
                    BlockPos pos = home.offset(dx, y, dz);
                    BlockState state = level.getBlockState(pos);
                    if (state.isAir() || state.getDestroySpeed(level, pos) < 0 || level.getBlockEntity(pos) != null) {
                        continue;
                    }
                    if (y == -1 && dx * dx + dz * dz > 9) {
                        continue;   // nur ein Krater unter der Mitte
                    }
                    if (state.isCollisionShapeFullBlock(level, pos) && thrown < 30 && this.random.nextInt(4) == 0) {
                        FallingBlockEntity chunk = FallingBlockEntity.fall(level, pos, state);
                        chunk.dropItem = false;
                        if (this.random.nextInt(3) != 0) {
                            chunk.disableDrop();
                        }
                        Vec3 out = new Vec3(dx, 0, dz);
                        out = out.lengthSqr() < 0.01 ? new Vec3(1, 0, 0) : out.normalize();
                        double speed = 0.5 + this.random.nextDouble() * 0.5;
                        chunk.setDeltaMovement(out.x * speed, 0.7 + this.random.nextDouble() * 0.5, out.z * speed);
                        chunk.needsSync = true;
                        thrown++;
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        if (this.random.nextInt(3) == 0) {
                            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                                    pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.25);
                        }
                    }
                }
            }
        }
        BossFx.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), 6.0f, 0.4f);
        BossFx.sound(level, c, SoundEvents.STONE_BREAK, 6.0f, 0.3f);
        BossFx.sound(level, c, SoundEvents.ANCIENT_DEBRIS_BREAK, 5.0f, 0.4f);
        BossFx.burst(level, ParticleTypes.EXPLOSION, c.add(0, 2, 0), 8, 4.0, 0.0);
        BossFx.burst(level, ModParticles.BOSS_DUST.get(), c.add(0, 3, 0), 90, 6.0, 0.15);
        this.shake(level, 90, 1.2f, 30);
        for (var e : this.enemiesAround(c, 12, 6)) {
            DragonBossEntity.pushAway(e, c, 1.6, 0.8);
        }
    }

    // =====================================================================================
    // Tod: Er erstarrt zu Stein und wird selbst zum Berg
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 3.0 * this.collapse(), 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.4f);
            BossFx.sound(level, body, SoundEvents.RAVAGER_DEATH, 5.0f, 0.4f);
        }
        if (tick < 60) {
            BossFx.burst(level, ModParticles.BOSS_DUST.get(), body, 4, 3.0, 0.03);
            if (tick % 10 == 0) {
                Vec3 p = body.add((this.random.nextDouble() - 0.5) * 8, this.random.nextDouble() * 3, (this.random.nextDouble() - 0.5) * 8);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), p.x, p.y, p.z, 20, 0.6, 0.6,
                        0.6, 0.2);
                BossFx.sound(level, p, SoundEvents.STONE_BREAK, 2.5f, 0.5f);
            }
            if (tick == 30) {
                BossFx.sound(level, body, ModSounds.DRAGON_HURT.get(), 6.0f, 0.4f);
            }
        } else if (tick < 140) {
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), body.x, body.y, body.z, 8, 3.5,
                    1.5, 3.5, 0.05);
            if (tick % 8 == 0) {
                BossFx.sound(level, body, SoundEvents.DRIPSTONE_BLOCK_BREAK, 2.0f, 0.5f);
            }
        } else if (tick == 160) {
            BossFx.burst(level, ParticleTypes.EXPLOSION_EMITTER, body, 1, 0, 0);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.AMBER_CRYSTAL.get().defaultBlockState()), body.x, body.y,
                    body.z, 150, 4.0, 2.0, 4.0, 0.4);
            BossFx.sound(level, body, SoundEvents.GENERIC_EXPLODE.value(), 6.0f, 0.5f);
            BossFx.sound(level, body, SoundEvents.TOTEM_USE, 3.0f, 0.5f);
            this.stoneMound(level);
        }
    }

    /** Wo er fiel, bleibt ein kleiner Felshügel mit Bernsteinkristallen. */
    private void stoneMound(ServerLevel level) {
        BlockPos base = this.blockPosition();
        for (int dx = -4; dx <= 4; dx++) {
            for (int dz = -4; dz <= 4; dz++) {
                double r = Math.sqrt(dx * dx + dz * dz);
                if (r > 4.2) {
                    continue;
                }
                int h = (int) Math.round(2.5 * (1.0 - r / 4.5) + this.random.nextDouble() * 0.8);
                for (int y = 0; y < h; y++) {
                    BlockPos p = base.offset(dx, y, dz);
                    if (level.getBlockState(p).isAir() || level.getBlockState(p).canBeReplaced()) {
                        BlockState s = this.random.nextInt(5) == 0 ? ModBlocks.RIFT_STONE.get().defaultBlockState()
                                : this.random.nextBoolean() ? ModBlocks.SHALE.base().get().defaultBlockState() : Blocks.COBBLESTONE.defaultBlockState();
                        level.setBlock(p, s, Block.UPDATE_ALL);
                    }
                }
                BlockPos top = base.offset(dx, h, dz);
                if (h > 0 && this.random.nextInt(4) == 0 && level.getBlockState(top).isAir()) {
                    level.setBlock(top, ModBlocks.AMBER_CLUSTER.get().defaultBlockState(), Block.UPDATE_ALL);
                }
            }
        }
    }
}
