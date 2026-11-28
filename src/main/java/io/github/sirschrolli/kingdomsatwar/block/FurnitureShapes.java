package io.github.sirschrolli.kingdomsatwar.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * Umrisse der Sitzmöbel, jeweils mit der Vorderseite nach Norden (Lehne im Süden). Sie folgen den Modellen aus
 * {@code datagen/FurnitureModels}.
 */
public final class FurnitureShapes {
    /** Stuhl: vier Beine, Sitzfläche auf 9 Pixel Höhe, Lehne. */
    public static final VoxelShape CHAIR = Shapes.or(
            Block.box(2, 7, 2, 14, 9, 14),
            Block.box(3, 0, 3, 5, 7, 5), Block.box(11, 0, 3, 13, 7, 5),
            Block.box(3, 0, 11, 5, 7, 13), Block.box(11, 0, 11, 13, 7, 13),
            Block.box(3, 9, 11, 13, 16, 13));
    public static final double CHAIR_SEAT = 9;

    /** Bank: über die volle Breite, damit mehrere nebeneinander eine lange Bank ergeben. */
    public static final VoxelShape BENCH = Shapes.or(
            Block.box(0, 7, 3, 16, 9, 13),
            Block.box(1, 0, 4, 3, 7, 12), Block.box(13, 0, 4, 15, 7, 12),
            Block.box(0, 9, 11, 16, 16, 13));
    public static final double BENCH_SEAT = 9;

    /** Thron: Sockel, Sitzkissen, Armlehnen und eine hohe Lehne mit goldener Krone. */
    public static final VoxelShape THRONE = Shapes.or(
            Block.box(0, 0, 0, 16, 7, 16),
            Block.box(2, 7, 1.5, 14, 8.5, 13),
            Block.box(0, 7, 0.5, 2, 13, 13), Block.box(14, 7, 0.5, 16, 13, 13),
            Block.box(0, 7, 13, 16, 27, 16));
    public static final double THRONE_SEAT = 8.5;

    private FurnitureShapes() {}
}
