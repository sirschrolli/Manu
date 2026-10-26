package io.github.sirschrolli.kingdomsatwar.entity.boss.nature;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossPart;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.CommonAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementAttacks;
import io.github.sirschrolli.kingdomsatwar.entity.boss.attack.ElementFx;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NatureLairDesign;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Verdantis, die Hüterin des Hains – der Naturdrachen-Boss. Schläft unter einem überwucherten Hügel in der Mitte
 * ihrer Lichtung; beim Erwachen reißen die Wurzelranken, der Hügel bricht auf, und der Hain erblüht.
 *
 * <p>Angriffe: Biss, Prankenhieb, Schwanzhieb (Samenkolben), Flügelschlag, Brüllen, Sporenodem (vergiftet), Sporenodem
 * im Sturzflug, Dornensalve, Wurzelfessel, Rankensäulen, Giftsporen, Dornensaat (aus der Luft), Rankendiener und die
 * Heilung aus den Lebensblüten (ab Phase 2). Langsam, schwer und zäh; fliegt nur selten.
 */
public class VerdantisEntity extends DragonBossEntity {
    /** In diesem Moment des Erwachens bricht der Hügel auf. */
    public static final int BURST_TICK = 92;

    public VerdantisEntity(EntityType<? extends VerdantisEntity> type, Level level) {
        super(type, level, DragonType.NATURE);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return createBossAttributes(980.0, 14.0, 0.27);
    }

    @Override
    protected int groundDuration() {
        return 560 + this.random.nextInt(320);
    }

    @Override
    protected int airDuration() {
        return 170 + this.random.nextInt(90);
    }

    @Override
    protected double cruiseHeight() {
        return 15.0;
    }

    // =====================================================================================
    // Trefferzonen (Geweih, kurzer Hals, Schwanz mit Samenkolben)
    // =====================================================================================

    @Override
    protected BossPart[] createParts() {
        return new BossPart[] {
                new BossPart(this, "head", 3.2f, 3.0f, 1.5f),
                new BossPart(this, "antlers", 6.0f, 3.2f, 0.6f),
                new BossPart(this, "neck", 2.8f, 2.8f, 1.1f),
                new BossPart(this, "tail1", 2.5f, 2.3f, 0.7f),
                new BossPart(this, "tail2", 2.1f, 1.9f, 0.7f),
                new BossPart(this, "tail3", 1.8f, 1.6f, 0.7f),
                new BossPart(this, "tail4", 2.3f, 2.1f, 0.7f),
                new BossPart(this, "wing_l", 5.2f, 1.2f, 0.85f),
                new BossPart(this, "wing_r", 5.2f, 1.2f, 0.85f),
        };
    }

    private Vec3 neckBase() {
        return this.bodyPoint(0, 4.9 * this.collapse(), 2.8);
    }

    private Vec3 neckDir() {
        Vec3 body = Vec3.directionFromRotation(0, this.yBodyRot);
        Vec3 head = Vec3.directionFromRotation(0, this.yHeadRot);
        Vec3 d = body.scale(0.4).add(head.scale(0.6));
        return d.lengthSqr() < 1.0e-4 ? body : d.normalize();
    }

    private double collapse() {
        if (this.getBossState() == State.DYING) {
            return 1.0 - 0.5 * Mth.clamp((this.deathTime - 30) / 50.0, 0.0, 1.0);
        }
        return 1.0;
    }

    @Override
    protected void layoutParts() {
        double reach = this.attackHeadReach();
        Vec3 base = this.neckBase();
        Vec3 dir = this.neckDir();
        double c = this.collapse();
        BossPart[] p = this.parts;
        setAt(p[0], base.add(dir.scale(4.3 + reach)).add(0, 1.4 * c, 0));
        setAt(p[1], base.add(dir.scale(3.6 + reach)).add(0, 4.4 * c, 0));
        setAt(p[2], base.add(dir.scale(1.5 + reach * 0.4)).add(0, 0.8 * c, 0));
        this.placePart(p[3], 0, 4.0 * c, -4.6);
        this.placePart(p[4], 0, 3.2 * c, -7.1);
        this.placePart(p[5], 0, 2.5 * c, -9.5);
        this.placePart(p[6], 0, 1.9 * c, -12.0);
        boolean spread = this.isFlying();
        double wx = spread ? 5.8 : 2.8;
        double wy = spread ? 6.0 : 5.8;
        this.placePart(p[7], wx, wy * c, spread ? 0.8 : 0.0);
        this.placePart(p[8], -wx, wy * c, spread ? 0.8 : 0.0);
    }

    private static void setAt(BossPart part, Vec3 pos) {
        part.setOldPosAndRot();
        part.setPos(pos.x, pos.y - part.getBbHeight() / 2.0, pos.z);
    }

    @Override
    public Vec3 mouthPosition() {
        return this.neckBase().add(this.neckDir().scale(6.2 + this.attackHeadReach())).add(0, 1.0 * this.collapse(), 0);
    }

    @Override
    protected void snortParticles() {
        super.snortParticles();
        Vec3 mouth = this.mouthPosition();
        for (int i = 0; i < 4; i++) {
            this.level().addParticle(ParticleTypes.SPORE_BLOSSOM_AIR, mouth.x, mouth.y + 0.3, mouth.z,
                    (this.random.nextDouble() - 0.5) * 0.1, 0.03, (this.random.nextDouble() - 0.5) * 0.1);
        }
    }

    // =====================================================================================
    // Angriffe
    // =====================================================================================

    @Override
    protected void registerAttacks(List<BossAttack> list) {
        ElementFx spores = new ElementFx(ModParticles.BOSS_SPORE.get(), ParticleTypes.SPORE_BLOSSOM_AIR,
                ModSounds.DRAGON_BREATH.get(DragonType.NATURE).get(), NatureAttacks::spores);
        list.add(new CommonAttacks.Bite(18, 12.0));
        list.add(new CommonAttacks.Claw(15));
        list.add(new CommonAttacks.TailSweep(13));
        list.add(new CommonAttacks.WingBuffet(5));
        list.add(new CommonAttacks.Roar(20));
        list.add(new CommonAttacks.TakeOff());
        list.add(new CommonAttacks.Land());
        list.add(new ElementAttacks.Breath(spores, 4, 20));
        list.add(new ElementAttacks.AirBreath(spores, 5));
        list.add(new ElementAttacks.Volley(BossProjectile.Kind.THORN, 6, 7));
        list.add(new NatureAttacks.RootGrasp(8));
        list.add(new NatureAttacks.VinePillars(10));
        list.add(new NatureAttacks.SporeCloud());
        list.add(new NatureAttacks.SeedRain(8));
        list.add(new NatureAttacks.VineServants());
        list.add(new NatureAttacks.BlossomHeal());
    }

    // =====================================================================================
    // Erwachen: Der überwucherte Hügel bricht auf
    // =====================================================================================

    @Override
    public int awakeningTicks() {
        return 190;
    }

    @Override
    protected void tickAwakening(ServerLevel level, int tick) {
        BlockPos home = this.getHome() != null ? this.getHome() : this.blockPosition();
        Vec3 c = Vec3.atBottomCenterOf(home);
        double depth;
        if (tick < BURST_TICK) {
            depth = 5.5 - 2.5 * Mth.clamp((tick - 30) / 60.0, 0.0, 1.0);
        } else {
            double u = Mth.clamp((tick - BURST_TICK) / 18.0, 0.0, 1.0);
            depth = 3.0 * (1.0 - (1.0 - Math.pow(1.0 - u, 3.0)));
        }
        this.setPos(c.x, c.y - depth, c.z);
        this.setDeltaMovement(Vec3.ZERO);
        if (tick < BURST_TICK) {
            this.stirMound(level, home, tick);
        } else if (tick == BURST_TICK) {
            this.burstMound(level, home);
        } else if (tick < 140) {
            // schüttelt Erde und Laub ab
            if (tick % 2 == 0) {
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), this.getX(),
                        this.getY() + 3.5, this.getZ(), 12, 3.0, 1.5, 3.0, 0.1);
                level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, this.getX(), this.getY() + 5, this.getZ(), 6, 4.0, 2.0, 4.0, 0.02);
            }
            if (tick == 118) {
                NatureAttacks.regrowBlossoms(level, home);
                this.bloom(level, home);
            }
        } else {
            if (tick == 142) {
                BossFx.sound(level, this.position(), ModSounds.DRAGON_ROAR.get(), 8.0f, 0.55f);
                BossFx.sound(level, this.position(), SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.55f);
                BossFx.sound(level, this.position(), SoundEvents.SPORE_BLOSSOM_BREAK, 4.0f, 0.5f);
                this.shake(level, 90, 0.85f, 30);
                BossFx.burst(level, ModParticles.BOSS_SPORE.get(), this.position().add(0, 4, 0), 80, 6.0, 0.1);
            }
            if (tick > 142) {
                // Sporen und Blütenblätter treiben über die Lichtung
                for (int i = 0; i < 5; i++) {
                    double a = this.random.nextDouble() * Mth.TWO_PI;
                    double r = this.random.nextDouble() * 28;
                    level.sendParticles(i % 2 == 0 ? ParticleTypes.SPORE_BLOSSOM_AIR : ParticleTypes.CHERRY_LEAVES,
                            c.x + Math.cos(a) * r, c.y + 10 + this.random.nextDouble() * 8, c.z + Math.sin(a) * r, 2, 1, 1, 1, 0.0);
                }
            }
        }
    }

    /** Der Hügel regt sich: Risse, Erdbrocken, Sporen; die Wurzelranken reißen eine nach der anderen. */
    private void stirMound(ServerLevel level, BlockPos home, int tick) {
        Vec3 c = Vec3.atBottomCenterOf(home);
        if (tick % 3 == 0) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = this.random.nextDouble() * NatureLairDesign.MOUND_RADIUS;
            BlockPos crack = BlockPos.containing(c.x + Math.cos(a) * r, c.y + NatureLairDesign.MOUND_HEIGHT * (1 - r / 10.0), c.z + Math.sin(a) * r);
            for (int dy = 2; dy >= -3; dy--) {
                BlockPos p = crack.above(dy);
                if (!level.getBlockState(p).isAir()) {
                    level.destroyBlockProgress(this.getId() * 64 + (tick / 3) % 40, p, Math.min(9, tick / 10));
                    level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(p)), p.getX() + 0.5, p.getY() + 1.0,
                            p.getZ() + 0.5, 6, 0.4, 0.2, 0.4, 0.05);
                    break;
                }
            }
            BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, c.add(0, NatureLairDesign.MOUND_HEIGHT, 0), 4, 3.0, 0.02);
        }
        if (tick % 10 == 0) {
            BossFx.sound(level, c, SoundEvents.ROOTED_DIRT_BREAK, 3.0f, 0.4f + tick * 0.004f);
        }
        if (tick == 30) {
            BossFx.sound(level, c, ModSounds.DRAGON_AMBIENT.get(), 5.0f, 0.45f);
        }
        if (tick >= 56 && tick % 6 == 2) {
            // eine Wurzelranke reißt
            for (BlockPos pos : BlockPos.betweenClosed(home.offset(-10, -1, -10), home.offset(10, (int) NatureLairDesign.MOUND_HEIGHT + 2, 10))) {
                if (level.getBlockState(pos).is(ModBlocks.GNARLED_ROOTS.get()) && this.random.nextInt(3) == 0) {
                    level.destroyBlock(pos, false);
                    BossFx.sound(level, Vec3.atCenterOf(pos), SoundEvents.MANGROVE_ROOTS_BREAK, 2.0f, 0.6f);
                    break;
                }
            }
        }
    }

    /** Der Hügel bricht auf: Erde, Moos und Blumen fliegen in alle Richtungen. */
    private void burstMound(ServerLevel level, BlockPos home) {
        Vec3 c = Vec3.atBottomCenterOf(home);
        int thrown = 0;
        double radius = NatureLairDesign.MOUND_RADIUS + 1.0;
        int reach = (int) Math.ceil(radius);
        int top = (int) NatureLairDesign.MOUND_HEIGHT + 2;
        for (int y = top; y >= 0; y--) {
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
                    if (state.isCollisionShapeFullBlock(level, pos) && thrown < 34 && this.random.nextInt(5) == 0) {
                        FallingBlockEntity chunk = FallingBlockEntity.fall(level, pos, state);
                        chunk.dropItem = false;
                        Vec3 out = new Vec3(dx, 0, dz);
                        out = out.lengthSqr() < 0.01 ? new Vec3(1, 0, 0) : out.normalize();
                        double speed = 0.45 + this.random.nextDouble() * 0.5;
                        chunk.setDeltaMovement(out.x * speed, 0.55 + this.random.nextDouble() * 0.5, out.z * speed);
                        chunk.needsSync = true;
                        thrown++;
                    } else {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        if (this.random.nextInt(3) == 0) {
                            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                                    pos.getZ() + 0.5, 8, 0.4, 0.4, 0.4, 0.2);
                        }
                    }
                }
            }
        }
        // der Boden darunter wird wieder zu Moos und Gras
        for (int dx = -reach; dx <= reach; dx++) {
            for (int dz = -reach; dz <= reach; dz++) {
                if (dx * dx + dz * dz > radius * radius) {
                    continue;
                }
                BlockPos floor = home.offset(dx, -1, dz);
                BlockState below = level.getBlockState(floor);
                if (below.isAir()) {
                    level.setBlock(floor, Blocks.ROOTED_DIRT.defaultBlockState(), Block.UPDATE_ALL);
                } else if (below.is(Blocks.DIRT) || below.is(Blocks.ROOTED_DIRT)) {
                    level.setBlock(floor, this.random.nextBoolean() ? Blocks.MOSS_BLOCK.defaultBlockState() : Blocks.ROOTED_DIRT.defaultBlockState(),
                            Block.UPDATE_ALL);
                }
            }
        }
        BossFx.sound(level, c, SoundEvents.GENERIC_EXPLODE.value(), 6.0f, 0.5f);
        BossFx.sound(level, c, SoundEvents.ROOTED_DIRT_BREAK, 6.0f, 0.4f);
        BossFx.sound(level, c, SoundEvents.MANGROVE_ROOTS_BREAK, 5.0f, 0.4f);
        BossFx.burst(level, ParticleTypes.EXPLOSION, c.add(0, 2, 0), 6, 4.0, 0.0);
        BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, c.add(0, 3, 0), 120, 7.0, 0.1);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ROOTED_DIRT.defaultBlockState()), c.x, c.y + 2, c.z, 120, 5.0,
                2.0, 5.0, 0.25);
        this.shake(level, 90, 1.0f, 25);
        for (var e : this.enemiesAround(c, 12, 6)) {
            DragonBossEntity.pushAway(e, c, 1.5, 0.7);
        }
    }

    /** Der Hain erblüht: Blumen sprießen im Kreis um das Nest. */
    private void bloom(ServerLevel level, BlockPos home) {
        BlockState[] flowers = {ModBlocks.GROVE_BELL.get().defaultBlockState(), Blocks.ALLIUM.defaultBlockState(),
                Blocks.AZURE_BLUET.defaultBlockState(), Blocks.OXEYE_DAISY.defaultBlockState(), Blocks.CORNFLOWER.defaultBlockState(),
                Blocks.LILY_OF_THE_VALLEY.defaultBlockState()};
        for (int i = 0; i < 70; i++) {
            double a = this.random.nextDouble() * Mth.TWO_PI;
            double r = 10 + this.random.nextDouble() * 16;
            int x = home.getX() + (int) Math.round(Math.cos(a) * r);
            int z = home.getZ() + (int) Math.round(Math.sin(a) * r);
            for (int dy = 3; dy >= -3; dy--) {
                BlockPos p = new BlockPos(x, home.getY() + dy, z);
                BlockState ground = level.getBlockState(p.below());
                BlockState here = level.getBlockState(p);
                if ((here.isAir() || here.is(Blocks.SHORT_GRASS)) && (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.MOSS_BLOCK))) {
                    level.setBlock(p, flowers[this.random.nextInt(flowers.length)], Block.UPDATE_ALL);
                    level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x + 0.5, p.getY() + 0.5, z + 0.5, 3, 0.3, 0.3, 0.3, 0.0);
                    break;
                }
            }
        }
        BossFx.sound(level, Vec3.atCenterOf(home), SoundEvents.AMETHYST_BLOCK_CHIME, 4.0f, 0.8f);
    }

    // =====================================================================================
    // Tod: Der Hain nimmt sie zurück
    // =====================================================================================

    @Override
    protected void tickDeathEffects(ServerLevel level, int tick) {
        Vec3 body = this.position().add(0, 3.0 * this.collapse(), 0);
        if (tick == 1) {
            BossFx.sound(level, body, ModSounds.DRAGON_DEATH.get(), 8.0f, 0.5f);
            BossFx.sound(level, body, SoundEvents.ENDER_DRAGON_GROWL, 5.0f, 0.5f);
        }
        if (tick < 60) {
            BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, body, 8, 3.5, 0.04);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, ModBlocks.ANCIENT_LEAVES.get().defaultBlockState()), body.x,
                    body.y + 2, body.z, 6, 3.0, 1.5, 3.0, 0.08);
            if (tick % 12 == 0) {
                BossFx.sound(level, body, SoundEvents.AZALEA_LEAVES_BREAK, 3.0f, 0.5f);
            }
            if (tick == 30) {
                BossFx.sound(level, body, ModSounds.DRAGON_HURT.get(), 6.0f, 0.5f);
            }
        } else if (tick < 140) {
            BossFx.burst(level, ParticleTypes.COMPOSTER, body.add(0, 1.5, 0), 4, 3.5, 0.02);
            BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, body.add(0, 1, 0), 4, 4.5, 0.02);
            if (tick % 4 == 0) {
                // Moos und Blumen breiten sich um die Gefallene aus
                double a = this.random.nextDouble() * Mth.TWO_PI;
                double r = 2 + this.random.nextDouble() * 7;
                BlockPos p = BlockPos.containing(this.getX() + Math.cos(a) * r, this.getY(), this.getZ() + Math.sin(a) * r);
                for (int dy = 2; dy >= -2; dy--) {
                    BlockPos q = p.above(dy);
                    BlockState ground = level.getBlockState(q.below());
                    if (level.getBlockState(q).isAir() && (ground.is(Blocks.GRASS_BLOCK) || ground.is(Blocks.DIRT) || ground.is(Blocks.ROOTED_DIRT)
                            || ground.is(Blocks.MOSS_BLOCK))) {
                        level.setBlock(q.below(), Blocks.MOSS_BLOCK.defaultBlockState(), Block.UPDATE_ALL);
                        level.setBlock(q, this.random.nextInt(3) == 0 ? ModBlocks.GROVE_BELL.get().defaultBlockState()
                                : Blocks.MOSS_CARPET.defaultBlockState(), Block.UPDATE_ALL);
                        break;
                    }
                }
            }
        } else {
            BossFx.burst(level, ParticleTypes.HAPPY_VILLAGER, body, 6, 4.0, 0.02);
            if (tick == 160) {
                BossFx.burst(level, ParticleTypes.SPORE_BLOSSOM_AIR, body, 200, 6.0, 0.2);
                BossFx.burst(level, ParticleTypes.END_ROD, body, 60, 3.0, 0.2);
                BossFx.sound(level, body, SoundEvents.TOTEM_USE, 3.0f, 0.8f);
                BossFx.sound(level, body, SoundEvents.AMETHYST_BLOCK_RESONATE, 4.0f, 0.6f);
            }
        }
    }
}
