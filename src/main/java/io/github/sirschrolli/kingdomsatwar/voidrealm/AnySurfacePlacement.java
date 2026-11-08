package io.github.sirschrolli.kingdomsatwar.voidrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.placement.PlacementContext;
import net.minecraft.world.level.levelgen.placement.PlacementModifier;

/**
 * Platzierung in der Leere, wo Bruchstücke übereinander treiben: Statt nur auf die oberste Fläche einer Spalte setzt sie
 * das Feature auf eine zufällige Oberseite ({@code ceiling = false}: fester Block mit Luft darüber) oder unter eine zufällige
 * Unterseite ({@code ceiling = true}: Luft unter festem Block) – so bekommen auch die tieferen Bruchstücke ihre Bäume und
 * ihre hängenden Ranken.
 */
public record AnySurfacePlacement(boolean ceiling) implements PlacementModifier {
    public static final MapCodec<AnySurfacePlacement> CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
            Codec.BOOL.optionalFieldOf("ceiling", false).forGetter(AnySurfacePlacement::ceiling)
    ).apply(i, AnySurfacePlacement::new));

    public static AnySurfacePlacement floor() {
        return new AnySurfacePlacement(false);
    }

    public static AnySurfacePlacement ceilingSide() {
        return new AnySurfacePlacement(true);
    }

    @Override
    public void modify(PlacementContext context, RandomSource random, BlockPos origin, Consumer<BlockPos> output) {
        int top = context.getHeight(Heightmap.Types.MOTION_BLOCKING, origin.getX(), origin.getZ());
        int bottom = context.getMinY() + 2;
        List<BlockPos> found = new ArrayList<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(origin.getX(), top, origin.getZ());
        BlockState above = context.getBlockState(pos);
        for (int y = top - 1; y >= bottom; y--) {
            pos.setY(y);
            BlockState here = context.getBlockState(pos);
            boolean solidHere = !here.isAir() && !here.canBeReplaced();
            boolean solidAbove = !above.isAir() && !above.canBeReplaced();
            if (!this.ceiling && solidHere && !solidAbove) {
                found.add(new BlockPos(origin.getX(), y + 1, origin.getZ()));
            } else if (this.ceiling && !solidHere && solidAbove) {
                found.add(new BlockPos(origin.getX(), y, origin.getZ()));
            }
            above = here;
        }
        if (!found.isEmpty()) {
            output.accept(found.get(random.nextInt(found.size())));
        }
    }

    @Override
    public MapCodec<AnySurfacePlacement> codec() {
        return CODEC;
    }
}
