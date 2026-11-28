package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.abyss.GravityField;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Ein Kristall der verdrehten Schwerkraft – eine Druse, die an Boden, Wand oder Decke wachsen kann. Um den
 * <b>Schwebekristall</b> wiegt man fast nichts (Kugel mit sieben Blöcken Halbmesser: weite Sprünge, sanftes Sinken, kein
 * Fallschaden); über dem <b>Hebekristall</b> kehrt sich die Schwerkraft um – eine Säule, in der man langsam nach oben
 * getragen wird. Die Wirkung selbst: {@link GravityField}.
 */
public class GravityCrystalBlock extends AmethystClusterBlock implements EntityBlock {
    public enum Kind {
        FLOAT(0x9AD4FF),
        LIFT(0xFF7AA0);

        public final int color;

        Kind(int color) {
            this.color = color;
        }
    }

    private final Kind kind;

    public GravityCrystalBlock(Kind kind, Properties properties) {
        super(7.0f, 3.0f, properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new GravityCrystalBlockEntity(pos, state);
    }

    @Override
    public <T extends BlockEntity> @Nullable BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide() || type != AbyssBlocks.GRAVITY_CRYSTAL_ENTITY.get()) {
            return null;
        }
        return (lvl, pos, st, be) -> GravityField.tickCrystal((ServerLevel) lvl, pos, this.kind);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        DustParticleOptions dust = new DustParticleOptions(this.kind.color, 0.8f);
        if (this.kind == Kind.FLOAT) {
            // ein schimmernder Hauch ringsum, der langsam steigt und sinkt
            for (int i = 0; i < 2; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = 1.0 + random.nextDouble() * GravityField.FLOAT_RADIUS * 0.8;
                double y = (random.nextDouble() - 0.5) * GravityField.FLOAT_RADIUS;
                level.addParticle(dust, pos.getX() + 0.5 + Math.cos(a) * r, pos.getY() + 0.5 + y, pos.getZ() + 0.5 + Math.sin(a) * r, 0.0,
                        0.004, 0.0);
            }
            if (random.nextInt(3) == 0) {
                level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + (random.nextDouble() - 0.5), pos.getY() + 0.8,
                        pos.getZ() + 0.5 + (random.nextDouble() - 0.5), 0.0, 0.01, 0.0);
            }
        } else {
            // die Säule: Funken steigen auf
            for (int i = 0; i < 3; i++) {
                double a = random.nextDouble() * Math.PI * 2.0;
                double r = random.nextDouble() * GravityField.LIFT_RADIUS;
                double y = random.nextDouble() * GravityField.LIFT_HEIGHT;
                level.addParticle(dust, pos.getX() + 0.5 + Math.cos(a) * r, pos.getY() + 0.5 + y, pos.getZ() + 0.5 + Math.sin(a) * r, 0.0,
                        0.12, 0.0);
            }
            if (random.nextInt(2) == 0) {
                level.addParticle(ParticleTypes.REVERSE_PORTAL, pos.getX() + 0.5, pos.getY() + 0.6, pos.getZ() + 0.5,
                        (random.nextDouble() - 0.5) * 0.1, 0.25, (random.nextDouble() - 0.5) * 0.1);
            }
        }
    }
}
