package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Die Baustoffe eines Dorfs: Sockelstein, Holz für Böden und Möbel, dunkles Holz für Pfosten und Balken, zwei
 * Dachdeckungen (die zweite für einzelne Häuser, damit nicht alle Dächer gleich aussehen), Fensterläden, Fenster und
 * Laternen.
 */
public final class VillagePalette {
    public final VillageStyle style;
    public final int variant;
    /** Sockel, Fundament, Brunnen und Treppenstufen draußen. */
    public final BuildingSet stone;
    /** Eingestreuter zweiter Stein (alt, bemoost). */
    public final BuildingSet stoneAlt;
    /** Böden, Möbel, Treppen im Haus, Zäune. */
    public final VillageStyle.WoodSet wood;
    /** Pfosten, Balken, Türen und alles Dunkle. */
    public final VillageStyle.WoodSet trim;
    public final BuildingSet roof;
    public final BuildingSet roofAlt;
    public final Block shutter;
    public final Block pane;
    public final Block lantern;

    VillagePalette(VillageStyle style, int variant, BuildingSet stone, BuildingSet stoneAlt, VillageStyle.WoodSet wood,
            VillageStyle.WoodSet trim, BuildingSet roof, BuildingSet roofAlt, Block shutter, Block pane, Block lantern) {
        this.style = style;
        this.variant = variant;
        this.stone = stone;
        this.stoneAlt = stoneAlt;
        this.wood = wood;
        this.trim = trim;
        this.roof = roof;
        this.roofAlt = roofAlt;
        this.shutter = shutter;
        this.pane = pane;
        this.lantern = lantern;
    }

    public BlockState stone() {
        return this.stone.base().get().defaultBlockState();
    }

    public BlockState stoneAlt() {
        return this.stoneAlt.base().get().defaultBlockState();
    }

    public Block stoneStairs() {
        return this.stone.stairs().get();
    }

    public Block stoneSlab() {
        return this.stone.slab().get();
    }

    public BlockState stoneWall() {
        return this.stone.wall() != null ? this.stone.wall().get().defaultBlockState() : Blocks.COBBLESTONE_WALL.defaultBlockState();
    }

    public BlockState planks() {
        return this.wood.planks().defaultBlockState();
    }

    public BlockState trimPlanks() {
        return this.trim.planks().defaultBlockState();
    }

    /** Stamm (Pfosten bzw. Balken) entlang einer Achse. */
    public BlockState post(Direction.Axis axis) {
        return this.trim.log().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    /** Geschälter Stamm (helle Balken, Fensterstürze). */
    public BlockState beam(Direction.Axis axis) {
        return this.trim.strippedLog().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    public BlockState fence() {
        return this.trim.fence().defaultBlockState();
    }

    public Block door() {
        return this.trim.door();
    }

    public BlockState roofBlock(BuildingSet roof) {
        return roof.base().get().defaultBlockState();
    }
}
