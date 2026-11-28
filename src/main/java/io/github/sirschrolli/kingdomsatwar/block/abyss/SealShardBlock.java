package io.github.sirschrolli.kingdomsatwar.block.abyss;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jspecify.annotations.Nullable;

/**
 * Kronensplitter: einer der sieben Splitter der Königskrone, die den Weltenfresser versiegeln. Aus der Druse auf dem Sockel
 * steigt ein großer Kristall, der langsam kreisend darüber schwebt; eine Kette aus Licht führt von ihm zum Siegelstern
 * über dem Schlund (gezeichnet von {@code client.renderer.abyss.SealShardRenderer}). Unzerstörbar.
 */
public class SealShardBlock extends AmethystClusterBlock implements EntityBlock {
    /** Farbe des Kronenlichts (Violett). */
    public static final int COLOR = 0xB27CFF;

    public SealShardBlock(Properties properties) {
        super(9.0f, 3.0f, properties);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SealShardBlockEntity(pos, state);
    }

    /** Funken steigen vom Kristall auf, violetter Staub sinkt zur Druse. */
    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        if (random.nextInt(2) == 0) {
            level.addParticle(ParticleTypes.END_ROD, pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 0.8, pos.getY() + 1.6 + random.nextDouble(),
                    pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 0.8, 0.0, 0.015, 0.0);
        }
        level.addParticle(new DustParticleOptions(COLOR, 0.9f), pos.getX() + 0.5 + (random.nextDouble() - 0.5) * 1.2, pos.getY() + 1.2
                + random.nextDouble() * 1.5, pos.getZ() + 0.5 + (random.nextDouble() - 0.5) * 1.2, 0.0, -0.01, 0.0);
    }
}
