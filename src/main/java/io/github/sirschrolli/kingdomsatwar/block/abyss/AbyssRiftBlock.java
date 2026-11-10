package io.github.sirschrolli.kingdomsatwar.block.abyss;

import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.VoidRiftBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.portal.TeleportTransition;
import org.jspecify.annotations.Nullable;

/**
 * Der Abgrundriss: ein Spalt in der Leere, hinter dem es rot und schwarz wogt. Wer hineintritt, stürzt in den Abgrund
 * (siehe {@link AbyssGate}). Funken und Splitter wirbeln heraus, und manchmal knirscht es, als bräche dahinter eine Welt.
 */
public class AbyssRiftBlock extends VoidRiftBlock {
    public AbyssRiftBlock(Properties properties) {
        super(properties);
    }

    @Override
    public @Nullable TeleportTransition getPortalDestination(ServerLevel currentLevel, Entity entity, BlockPos portalEntryPos) {
        return AbyssGate.destination(currentLevel, entity, portalEntryPos);
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        boolean x = state.getValue(AXIS) == Direction.Axis.X;
        for (int i = 0; i < 2; i++) {
            double px = pos.getX() + (x ? random.nextDouble() : 0.5 + (random.nextDouble() - 0.5) * 0.3);
            double pz = pos.getZ() + (x ? 0.5 + (random.nextDouble() - 0.5) * 0.3 : random.nextDouble());
            double py = pos.getY() + random.nextDouble();
            double out = (random.nextDouble() - 0.5) * 0.14;
            level.addParticle(random.nextInt(3) == 0 ? ParticleTypes.FLAME : ParticleTypes.SMOKE, px, py, pz, x ? 0.0 : out, 0.02,
                    x ? out : 0.0);
        }
        if (random.nextInt(5) == 0) {
            level.addParticle(ParticleTypes.LAVA, pos.getX() + random.nextDouble(), pos.getY() + random.nextDouble(),
                    pos.getZ() + random.nextDouble(), 0.0, 0.0, 0.0);
        }
        if (random.nextInt(70) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.35f,
                    0.3f + random.nextFloat() * 0.1f, false);
        }
        if (random.nextInt(160) == 0) {
            level.playLocalSound(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, SoundEvents.DEEPSLATE_BREAK, SoundSource.BLOCKS, 0.6f,
                    0.4f + random.nextFloat() * 0.2f, false);
        }
    }
}
