package io.github.sirschrolli.kingdomsatwar.mixin;

import io.github.sirschrolli.kingdomsatwar.rules.HardWorld;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.feature.AbstractOreFeature;
import net.minecraft.world.level.levelgen.feature.OreFeature;
import net.minecraft.world.level.levelgen.feature.ScatteredOreFeature;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Seltenere Erze (seit 1.3, siehe {@link HardWorld#skipOre}): Nur jede zweite Erzader entsteht – bei den eigenen Erzen wie
 * bei denen von Minecraft, auch beim antiken Schutt.
 */
@Mixin({OreFeature.class, ScatteredOreFeature.class})
public abstract class OreFeatureMixin {
    @Inject(method = "place", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$rarerOres(WorldGenLevel level, ChunkGenerator chunkGenerator, RandomSource random, BlockPos origin,
            CallbackInfoReturnable<Boolean> cir) {
        if (HardWorld.skipOre((AbstractOreFeature) (Object) this, random)) {
            cir.setReturnValue(false);
        }
    }
}
