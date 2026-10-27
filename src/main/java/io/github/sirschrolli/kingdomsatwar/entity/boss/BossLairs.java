package io.github.sirschrolli.kingdomsatwar.entity.boss;

import io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Verbindung zwischen Boss und Nest: Nach dem Sieg erscheint das Ei auf dem Nest-Altar, beim Einschlafen wartet
 * das Nest auf den nächsten Besucher.
 */
public final class BossLairs {
    private BossLairs() {}

    /** Der Boss ist besiegt: Das reine Drachenei erscheint (mit Lichtstrahl) am Nest. */
    public static void onBossDefeated(ServerLevel level, DragonBossEntity boss, BlockPos home) {
        BlockPos egg = findEggSpot(level, home, boss.element());
        BlockState state = ModBlocks.DRAGON_EGGS.get(boss.element()).get().defaultBlockState();
        level.setBlock(egg, state, Block.UPDATE_ALL);
        for (int i = 0; i < 40; i++) {
            level.sendParticles(ParticleTypes.END_ROD, egg.getX() + 0.5, egg.getY() + 0.5 + i * 0.6, egg.getZ() + 0.5, 3,
                    0.15, 0.2, 0.15, 0.01);
        }
        level.sendParticles(boss.element().particle(), egg.getX() + 0.5, egg.getY() + 1.0, egg.getZ() + 0.5, 60, 1.2, 1.0, 1.2, 0.05);
        level.playSound(null, egg, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 2.0f, 0.7f);
        level.playSound(null, egg, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 2.0f, 0.6f);
        spawnShard(level, egg, boss.element());
        if (boss.getLairCore() != null && level.getBlockEntity(boss.getLairCore()) instanceof DragonLairCoreBlockEntity core) {
            core.onBossDefeated(level);
        } else {
            io.github.sirschrolli.kingdomsatwar.world.LairRecords.update(level, boss.element(), home,
                    io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.DEFEATED);
        }
        io.github.sirschrolli.kingdomsatwar.story.Story.onDragonDefeated(level, boss.element());
    }

    /**
     * Der Siegelsplitter schwebt über dem Ei: Er fällt nicht, verbrennt nicht, verschwindet nicht und kann nicht
     * zerstört werden – bis ihn jemand aufhebt.
     */
    public static net.minecraft.world.entity.item.ItemEntity spawnShard(ServerLevel level, BlockPos egg, DragonType element) {
        net.minecraft.world.item.ItemStack stack = new net.minecraft.world.item.ItemStack(
                io.github.sirschrolli.kingdomsatwar.registry.ModItems.SEAL_SHARDS.get(element).get());
        net.minecraft.world.entity.item.ItemEntity shard = new net.minecraft.world.entity.item.ItemEntity(level,
                egg.getX() + 0.5, egg.getY() + 1.6, egg.getZ() + 0.5, stack, 0.0, 0.0, 0.0);
        shard.setNoGravity(true);
        shard.setUnlimitedLifetime();
        shard.setPermanentlyInvulnerable(true);
        shard.setGlowingTag(true);
        shard.setPickUpDelay(40);
        level.addFreshEntity(shard);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, shard.getX(), shard.getY(), shard.getZ(), 30, 0.3, 0.3, 0.3, 0.05);
        level.playSound(null, egg, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 2.0f, 0.5f);
        return shard;
    }

    /** Der Boss schläft wieder (keine Gegner mehr im Nest). */
    public static void onBossSlumber(ServerLevel level, DragonBossEntity boss) {
        if (boss.getLairCore() != null && level.getBlockEntity(boss.getLairCore()) instanceof DragonLairCoreBlockEntity core) {
            core.onBossSlumber(level);
        } else if (boss.getLairCore() != null
                && level.getBlockEntity(boss.getLairCore()) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity throne) {
            throne.onBossSlumber(level);
        }
    }

    private static BlockPos findEggSpot(ServerLevel level, BlockPos home, DragonType element) {
        BlockPos.MutableBlockPos pos = home.mutable();
        for (int i = 0; i < 32 && !level.getBlockState(pos).isAir(); i++) {
            pos.move(0, 1, 0);           // aus festem Boden oder Flüssigkeit heraus
        }
        for (int i = 0; i < 32 && level.getBlockState(pos.below()).isAir() && pos.getY() > level.getMinY(); i++) {
            pos.move(0, -1, 0);          // durch Luft hinab bis auf den Boden
        }
        if (!level.getFluidState(pos.below()).isEmpty()) {
            // über einem Lavasee oder im Wasser: ein kleiner Sockel trägt das Ei
            level.setBlock(pos.below(), pedestal(element), Block.UPDATE_ALL);
        }
        return pos.immutable();
    }

    private static BlockState pedestal(DragonType element) {
        return switch (element) {
            case FIRE -> Blocks.POLISHED_BLACKSTONE.defaultBlockState();
            case ICE -> Blocks.PACKED_ICE.defaultBlockState();
            case LIGHTNING -> Blocks.COPPER_BLOCK.weathering().unaffected().defaultBlockState();
            case NATURE -> Blocks.MOSS_BLOCK.defaultBlockState();
            case EARTH -> Blocks.POLISHED_DEEPSLATE.defaultBlockState();
            case WATER -> Blocks.PRISMARINE_BRICKS.defaultBlockState();
        };
    }
}
