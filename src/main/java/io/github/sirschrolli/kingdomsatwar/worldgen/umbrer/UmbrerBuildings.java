package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock;
import io.github.sirschrolli.kingdomsatwar.entity.umbrer.UmbrerProfession;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.AbstractFurnaceBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.ComposterBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FarmlandBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.LecternBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.StonecutterBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;

/**
 * Die Gebäude der Umbrer, jedes in eigenen Koordinaten gebaut (siehe {@link VillageBuilder}): Fachwerk aus dunklen
 * Schattenholz-Balken mit violetten Bohlen auf einem Sockel aus Schattenstein, Dächer aus Schattenstein-Schindeln,
 * Fenster aus hellem Glas, Mondlaternen – und überall die Nachtkristalle, die die Umbrer so lieben.
 */
final class UmbrerBuildings {
    private UmbrerBuildings() {}

    // =====================================================================================
    // Baustoffe
    // =====================================================================================

    private static BlockState bricks() {
        return ShadowBlocks.SHADOW_STONE_BRICKS.base().get().defaultBlockState();
    }

    /** Ziegel, hier und da gesprungen. */
    private static BlockState bricks(VillageBuilder b, int x, int y, int z) {
        return b.chance(x, y, z, 0.12f) ? ShadowBlocks.CRACKED_SHADOW_STONE_BRICKS.get().defaultBlockState() : bricks();
    }

    private static Block brickStairs() {
        return ShadowBlocks.SHADOW_STONE_BRICKS.stairs().get();
    }

    private static BlockState brickWall() {
        return ShadowBlocks.SHADOW_STONE_BRICKS.wall() != null ? ShadowBlocks.SHADOW_STONE_BRICKS.wall().get().defaultBlockState() : bricks();
    }

    private static BlockState polished() {
        return ShadowBlocks.POLISHED_SHADOW_STONE.base().get().defaultBlockState();
    }

    private static Block polishedStairs() {
        return ShadowBlocks.POLISHED_SHADOW_STONE.stairs().get();
    }

    private static BlockState polishedSlab(SlabType type) {
        return VillageBuilder.slab(ShadowBlocks.POLISHED_SHADOW_STONE.slab().get(), type);
    }

    private static BlockState chiseled() {
        return ShadowBlocks.CHISELED_SHADOW_STONE.get().defaultBlockState();
    }

    /** Säule aus Tiefschattenstein. */
    private static BlockState pillar() {
        return ShadowBlocks.DEEP_SHADOW_STONE.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y);
    }

    private static BlockState planks() {
        return ShadowBlocks.SHADOW_PLANKS.base().get().defaultBlockState();
    }

    private static Block plankStairs() {
        return ShadowBlocks.SHADOW_PLANKS.stairs().get();
    }

    private static BlockState plankSlab(SlabType type) {
        return VillageBuilder.slab(ShadowBlocks.SHADOW_PLANKS.slab().get(), type);
    }

    private static BlockState log(Direction.Axis axis) {
        return ShadowBlocks.SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState stripped(Direction.Axis axis) {
        return ShadowBlocks.STRIPPED_SHADOW_LOG.get().defaultBlockState().setValue(RotatedPillarBlock.AXIS, axis);
    }

    private static BlockState fence() {
        return ShadowBlocks.SHADOW_FENCE.get().defaultBlockState();
    }

    /** Fensterglas: hell und kühl wie Mondlicht. */
    private static BlockState pane() {
        return Blocks.STAINED_GLASS_PANE.lightBlue().defaultBlockState();
    }

    private static BlockState crystal() {
        return ShadowBlocks.NIGHT_CRYSTAL.get().defaultBlockState();
    }

    private static BlockState cluster() {
        return ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState();
    }

    private static Block lantern() {
        return ShadowBlocks.MOON_LANTERN.get();
    }

    private static BlockState soil() {
        return ShadowBlocks.SHADOW_SOIL.get().defaultBlockState();
    }

    private static BlockState grass() {
        return ShadowBlocks.SHADOW_GRASS.get().defaultBlockState();
    }

    private static BlockState path() {
        return ShadowBlocks.SHADOW_PATH.get().defaultBlockState();
    }

    private static BlockState carpet() {
        return Blocks.CARPET.purple().defaultBlockState();
    }

    private static BlockState candles(int count) {
        return Blocks.DYED_CANDLE.purple().defaultBlockState().setValue(CandleBlock.CANDLES, count).setValue(CandleBlock.LIT, true);
    }

    private static BlockState chest(Direction facing) {
        return Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing);
    }

    private static BlockState barrel(Direction facing) {
        return Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing);
    }

    // =====================================================================================
    // Gemeinsame Teile
    // =====================================================================================

    /**
     * Fachwerkwände um das Rechteck (x0..x1, z0..z1) von y = 1 bis 4: unten ein Sockel aus Schattenstein, darüber
     * violette Bohlen, oben ein liegender Balken, an den Ecken stehende Stämme.
     */
    private static void timberWalls(VillageBuilder b, int x0, int z0, int x1, int z1) {
        for (int y = 1; y <= 4; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean rimX = x == x0 || x == x1;
                    boolean rimZ = z == z0 || z == z1;
                    if (!rimX && !rimZ) {
                        continue;
                    }
                    BlockState state;
                    if (rimX && rimZ) {
                        state = log(Direction.Axis.Y);
                    } else if (y == 1) {
                        state = bricks(b, x, y, z);
                    } else if (y == 4) {
                        state = log(rimZ ? Direction.Axis.X : Direction.Axis.Z);
                    } else {
                        state = planks();
                    }
                    b.set(x, y, z, state);
                }
            }
        }
    }

    /** Boden: am Rand Schattenstein, innen Bohlen. */
    private static void plankFloor(VillageBuilder b, int x0, int z0, int x1, int z1) {
        b.fill(x0, 0, z0, x1, 0, z1, bricks());
        b.fill(x0 + 1, 0, z0 + 1, x1 - 1, 0, z1 - 1, planks());
    }

    /** Ein Fenster aus zwei Scheiben übereinander. */
    private static void window(VillageBuilder b, int x, int y, int z) {
        b.set(x, y, z, pane());
        b.set(x, y + 1, z, pane());
    }

    /**
     * Der Weg von der Straße zur Tür (x0..x1, vor der Vorderseite bei z = −1): ein Pfad, oder eine Stufe hinauf bzw.
     * hinab, je nachdem, wie der Fußboden zur Straße liegt.
     */
    private static void entrance(VillageBuilder b, UmbrerVillagePiece piece, int x0, int x1) {
        for (int x = x0; x <= x1; x++) {
            switch (piece.step()) {
                case 1 -> {
                    b.set(x, -1, -1, bricks());
                    b.set(x, 0, -1, VillageBuilder.stairs(brickStairs(), Direction.SOUTH, Half.BOTTOM));
                    b.air(x, 1, -1, x, 3, -1);
                }
                case -1 -> {
                    b.set(x, 0, -1, bricks());
                    b.set(x, 1, -1, VillageBuilder.stairs(brickStairs(), Direction.NORTH, Half.BOTTOM));
                    b.air(x, 2, -1, x, 3, -1);
                }
                default -> {
                    b.set(x, -1, -1, soil());
                    b.set(x, 0, -1, path());
                    b.air(x, 1, -1, x, 3, -1);
                }
            }
        }
    }

    /** Glimmerranken, die von (x, top, z) aus {@code length} Blöcke herabhängen. */
    private static void vines(VillageBuilder b, int x, int top, int z, int length) {
        for (int i = 0; i < length; i++) {
            if (!b.get(x, top - i, z).isAir()) {
                break;
            }
            boolean tip = i == length - 1 || !b.get(x, top - i - 1, z).isAir();
            b.set(x, top - i, z, tip ? ShadowBlocks.GLIMMER_VINES.get().defaultBlockState() : ShadowBlocks.GLIMMER_VINES_PLANT.get().defaultBlockState());
        }
    }

    // =====================================================================================
    // Wohnhaus (7 × 7)
    // =====================================================================================

    static void house(VillageBuilder b, UmbrerVillagePiece piece) {
        UmbrerProfession resident = UmbrerProfession.randomTrader(b.random);
        boolean bedLeft = b.random.nextBoolean();
        b.foundation(0, 0, 6, 6, bricks(), bricks(), 12);
        plankFloor(b, 0, 0, 6, 6);
        timberWalls(b, 0, 0, 6, 6);
        b.door(3, 1, 0, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.set(3, 4, 0, chiseled());
        window(b, 1, 2, 0);
        window(b, 5, 2, 0);
        window(b, 0, 2, 3);
        window(b, 6, 2, 3);
        window(b, 3, 2, 6);
        // Dach aus Schattenstein-Schindeln, First aus poliertem Stein, Rundfenster im Giebel
        b.gableAlongZ(0, 6, 0, 6, 4, brickStairs(), polished(), polishedSlab(SlabType.BOTTOM), planks());
        b.set(3, 6, 0, pane());
        b.set(3, 6, 6, pane());
        b.hangingLantern(3, 6, -1, lantern());
        // Einrichtung
        int bx = bedLeft ? 1 : 5;
        int sx = bedLeft ? 5 : 1;
        b.bed(bx, 1, 4, Blocks.BED.purple(), Direction.SOUTH);
        b.chest(sx, 1, 5, chest(Direction.NORTH), ModLootTables.UMBRER_HOUSE);
        b.set(sx, 1, 4, barrel(Direction.UP));
        b.set(sx, 2, 4, ShadowBlocks.POTTED_SHADOW_SAPLING.get().defaultBlockState());
        b.set(sx, 1, 2, plankSlab(SlabType.TOP));
        b.set(sx + (bedLeft ? -1 : 1), 1, 2, VillageBuilder.stairs(plankStairs(), bedLeft ? Direction.WEST : Direction.EAST, Half.BOTTOM));
        b.set(sx, 2, 2, candles(2));
        b.fill(2, 1, 3, 4, 1, 4, carpet());
        b.hangingLantern(3, 6, 3, lantern());
        entrance(b, piece, 3, 3);
        piece.spawn(b, 3, 1, 2, resident);
    }

    // =====================================================================================
    // Langhaus (11 × 7) – hier wohnt meist ein Dämmerbauer
    // =====================================================================================

    static void longhouse(VillageBuilder b, UmbrerVillagePiece piece) {
        UmbrerProfession second = UmbrerProfession.randomTrader(b.random);
        b.foundation(0, 0, 10, 6, bricks(), bricks(), 12);
        plankFloor(b, 0, 0, 10, 6);
        timberWalls(b, 0, 0, 10, 6);
        // Pfosten neben der Tür und in der Mitte der Rückwand
        b.fill(4, 1, 0, 4, 4, 0, log(Direction.Axis.Y));
        b.fill(6, 1, 0, 6, 4, 0, log(Direction.Axis.Y));
        b.fill(5, 1, 6, 5, 4, 6, log(Direction.Axis.Y));
        b.door(5, 1, 0, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.set(5, 3, 0, planks());
        b.set(5, 4, 0, chiseled());
        window(b, 2, 2, 0);
        window(b, 8, 2, 0);
        window(b, 2, 2, 6);
        window(b, 8, 2, 6);
        window(b, 0, 2, 3);
        window(b, 10, 2, 3);
        b.gableAlongX(0, 10, 0, 6, 4, brickStairs(), polished(), polishedSlab(SlabType.BOTTOM), planks());
        b.set(0, 6, 3, pane());
        b.set(10, 6, 3, pane());
        b.hangingLantern(3, 3, -1, lantern());
        b.hangingLantern(7, 3, -1, lantern());
        // Einrichtung: zwei Betten, ein langer Tisch mit Stühlen, Vorräte
        b.bed(1, 1, 4, Blocks.BED.purple(), Direction.SOUTH);
        b.bed(3, 1, 4, Blocks.BED.purple(), Direction.SOUTH);
        for (int x = 6; x <= 8; x++) {
            b.set(x, 1, 3, plankSlab(SlabType.TOP));
        }
        b.set(7, 2, 3, candles(3));
        b.set(6, 1, 2, VillageBuilder.stairs(plankStairs(), Direction.NORTH, Half.BOTTOM));
        b.set(8, 1, 2, VillageBuilder.stairs(plankStairs(), Direction.NORTH, Half.BOTTOM));
        b.set(7, 1, 4, VillageBuilder.stairs(plankStairs(), Direction.SOUTH, Half.BOTTOM));
        b.chest(9, 1, 5, chest(Direction.NORTH), ModLootTables.UMBRER_HOUSE);
        b.set(9, 1, 1, barrel(Direction.UP));
        b.set(9, 2, 1, barrel(Direction.UP));
        b.set(9, 1, 2, Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 3));
        b.set(1, 1, 1, Blocks.SMOKER.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.EAST));
        b.set(1, 1, 2, barrel(Direction.UP));
        b.hangingLantern(3, 6, 3, lantern());
        b.hangingLantern(7, 6, 3, lantern());
        entrance(b, piece, 5, 5);
        piece.spawn(b, 4, 1, 2, UmbrerProfession.FARMER);
        piece.spawn(b, 6, 1, 1, second);
    }

    // =====================================================================================
    // Mondschmiede (9 × 9) – offene Werkstatt aus Stein mit Esse und blauem Seelenfeuer
    // =====================================================================================

    static void smithy(VillageBuilder b, UmbrerVillagePiece piece) {
        b.foundation(0, 0, 8, 8, bricks(), bricks(), 13);
        b.fill(0, 0, 0, 8, 0, 8, polished());
        b.ring(0, 0, 8, 8, 0, bricks());
        for (int y = 1; y <= 4; y++) {
            for (int x = 0; x <= 8; x++) {
                for (int z = 0; z <= 8; z++) {
                    if (x != 0 && x != 8 && z != 0 && z != 8) {
                        continue;
                    }
                    boolean corner = (x == 0 || x == 8) && (z == 0 || z == 8);
                    b.set(x, y, z, corner ? pillar() : bricks(b, x, y, z));
                }
            }
        }
        // breite, offene Front mit Sturz und dem Zeichen der Umbrer
        b.air(2, 1, 0, 6, 3, 0);
        b.fill(1, 1, 0, 1, 3, 0, pillar());
        b.fill(7, 1, 0, 7, 3, 0, pillar());
        b.fill(1, 4, 0, 7, 4, 0, polished());
        b.set(4, 4, 0, chiseled());
        // vergitterte Fenster
        for (int side : new int[] {0, 8}) {
            b.set(side, 2, 3, Blocks.IRON_BARS.defaultBlockState());
            b.set(side, 3, 3, Blocks.IRON_BARS.defaultBlockState());
            b.set(side, 2, 5, Blocks.IRON_BARS.defaultBlockState());
            b.set(side, 3, 5, Blocks.IRON_BARS.defaultBlockState());
        }
        int top = b.hipRoof(-1, -1, 9, 9, 4, brickStairs(), polished());
        // Esse: erhöhte Feuerstelle mit Seelenfeuer hinter einem Gitter, darüber eine Haube und der Schornstein
        b.set(1, 1, 7, Blocks.SOUL_SOIL);
        b.set(2, 1, 7, Blocks.SOUL_SOIL);
        b.set(1, 2, 7, Blocks.SOUL_FIRE);
        b.set(2, 2, 7, Blocks.SOUL_FIRE);
        b.set(1, 1, 6, bricks());
        b.set(2, 1, 6, bricks());
        b.set(1, 2, 6, Blocks.IRON_BARS.defaultBlockState());
        b.set(2, 2, 6, Blocks.IRON_BARS.defaultBlockState());
        b.fill(3, 1, 7, 3, 3, 7, bricks());
        b.set(3, 1, 6, VillageBuilder.stairs(brickStairs(), Direction.WEST, Half.BOTTOM));
        b.set(1, 3, 6, VillageBuilder.stairs(brickStairs(), Direction.SOUTH, Half.TOP));
        b.set(2, 3, 6, VillageBuilder.stairs(brickStairs(), Direction.SOUTH, Half.TOP));
        b.fill(1, 3, 7, 2, top + 1, 7, bricks());
        BlockState fire = Blocks.SOUL_CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true);
        b.set(1, top + 2, 7, fire);
        b.set(2, top + 2, 7, fire);
        // Werkstatt
        b.set(4, 1, 7, Blocks.BLAST_FURNACE.defaultBlockState().setValue(AbstractFurnaceBlock.FACING, Direction.NORTH)
                .setValue(AbstractFurnaceBlock.LIT, true));
        b.set(5, 1, 7, Blocks.SMITHING_TABLE);
        b.chest(7, 1, 7, chest(Direction.NORTH), ModLootTables.UMBRER_SMITHY);
        b.set(6, 1, 7, barrel(Direction.UP));
        b.set(4, 1, 4, Blocks.ANVIL.defaultBlockState().setValue(AnvilBlock.FACING, Direction.EAST));
        b.set(7, 1, 4, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, Direction.NORTH));
        b.set(7, 1, 1, barrel(Direction.UP));
        b.set(1, 1, 1, barrel(Direction.UP));
        b.set(1, 2, 1, ShadowBlocks.MOONSILVER_BLOCK.get());
        b.set(7, 2, 1, lantern().defaultBlockState());
        // Kette mit Laterne unter der Dachspitze
        b.fill(4, 6, 4, 4, top - 1, 4, Blocks.IRON_CHAIN.defaultBlockState());
        b.hangingLantern(4, 5, 4, lantern());
        entrance(b, piece, 2, 6);
        piece.spawn(b, 4, 1, 2, UmbrerProfession.MOONSMITH);
    }

    // =====================================================================================
    // Kräuterhütte (7 × 11) – Hütte vorne, Kräutergarten dahinter
    // =====================================================================================

    static void herbHut(VillageBuilder b, UmbrerVillagePiece piece) {
        b.foundation(0, 0, 6, 6, bricks(), bricks(), 11);
        b.foundation(0, 7, 6, 10, soil(), soil(), 6);
        plankFloor(b, 0, 0, 6, 6);
        timberWalls(b, 0, 0, 6, 6);
        b.door(3, 1, 0, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.door(3, 1, 6, ShadowBlocks.SHADOW_DOOR.get(), Direction.NORTH);
        b.set(3, 4, 0, chiseled());
        window(b, 1, 2, 0);
        window(b, 5, 2, 0);
        window(b, 0, 2, 3);
        window(b, 6, 2, 3);
        // Dach aus Schattenholz, auf dem First wächst Glimmmoos, von der Traufe hängen Glimmerranken
        b.gableAlongZ(0, 6, 0, 6, 4, plankStairs(), stripped(Direction.Axis.Z), ShadowBlocks.GLIMMER_MOSS.get().defaultBlockState(), planks());
        b.set(3, 6, 0, pane());
        b.set(3, 6, 6, pane());
        for (int z = -1; z <= 7; z++) {
            if (b.chance(-1, 3, z, 0.4f)) {
                vines(b, -1, 3, z, 1 + (int) (b.noise(-1, 2, z) * 2.0f));
            }
            if (b.chance(7, 3, z, 0.4f)) {
                vines(b, 7, 3, z, 1 + (int) (b.noise(7, 2, z) * 2.0f));
            }
        }
        // Einrichtung: Kessel, Braustand, Bett, Truhe, Kräuter unter dem Dach
        b.set(1, 1, 1, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        b.set(1, 1, 2, Blocks.BREWING_STAND);
        b.set(1, 1, 5, barrel(Direction.UP));
        b.set(1, 2, 5, ShadowBlocks.POTTED_SHADOW_SAPLING.get().defaultBlockState());
        b.set(1, 1, 4, plankSlab(SlabType.TOP));
        b.set(1, 2, 4, candles(1));
        b.bed(5, 1, 3, Blocks.BED.purple(), Direction.SOUTH);
        b.chest(5, 1, 5, chest(Direction.NORTH), ModLootTables.UMBRER_HERBALIST);
        b.set(5, 1, 1, Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 5));
        vines(b, 1, 5, 3, 2);
        vines(b, 5, 5, 2, 2);
        vines(b, 1, 5, 1, 1);
        b.hangingLantern(3, 6, 3, lantern());
        // Kräutergarten: Seelenbeeren, Geisterkappen und Glimmmoos, mittendurch ein Pfad, rundum ein Zaun
        for (int z = 7; z <= 10; z++) {
            b.set(0, 1, z, fence());
            b.set(6, 1, z, fence());
        }
        for (int x = 1; x <= 5; x++) {
            b.set(x, 1, 10, fence());
        }
        b.set(0, 2, 10, lantern().defaultBlockState());
        b.set(6, 2, 10, lantern().defaultBlockState());
        for (int x = 1; x <= 5; x++) {
            for (int z = 7; z <= 9; z++) {
                b.air(x, 1, z, x, 4, z);
                if (x == 3) {
                    b.set(x, 0, z, path());
                } else if (x <= 2) {
                    b.set(x, 0, z, grass());
                    int age = b.chance(x, 1, z, 0.6f) ? 3 : 2;
                    b.set(x, 1, z, ShadowBlocks.SOUL_BERRY_BUSH.get().defaultBlockState().setValue(SoulBerryBushBlock.AGE, age));
                } else if ((x + z) % 2 == 0) {
                    b.set(x, 0, z, ShadowBlocks.GLIMMER_MOSS.get());
                } else {
                    b.set(x, 0, z, grass());
                    b.set(x, 1, z, ShadowBlocks.GHOSTCAP.get());
                }
            }
        }
        entrance(b, piece, 3, 3);
        piece.spawn(b, 3, 1, 2, UmbrerProfession.HERBALIST);
    }

    // =====================================================================================
    // Kristallwerkstatt (7 × 9) – Fenster aus Nachtkristall, ein Kamm aus Kristallen auf dem First
    // =====================================================================================

    static void crystalWorkshop(VillageBuilder b, UmbrerVillagePiece piece) {
        b.foundation(0, 0, 6, 8, bricks(), bricks(), 12);
        b.fill(0, 0, 0, 6, 0, 8, polished());
        b.ring(0, 0, 6, 8, 0, bricks());
        for (int y = 1; y <= 4; y++) {
            for (int x = 0; x <= 6; x++) {
                for (int z = 0; z <= 8; z++) {
                    boolean rimX = x == 0 || x == 6;
                    boolean rimZ = z == 0 || z == 8;
                    if (!rimX && !rimZ) {
                        continue;
                    }
                    BlockState state = rimX && rimZ ? pillar() : (y == 1 || y == 4) ? bricks(b, x, y, z) : polished();
                    b.set(x, y, z, state);
                }
            }
        }
        b.door(3, 1, 0, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.set(3, 3, 0, chiseled());
        for (int y = 2; y <= 3; y++) {
            b.set(1, y, 0, crystal());
            b.set(5, y, 0, crystal());
            b.set(0, y, 2, crystal());
            b.set(0, y, 6, crystal());
            b.set(6, y, 2, crystal());
            b.set(6, y, 6, crystal());
            b.set(3, y, 8, crystal());
        }
        // First aus Nachtkristall mit einem Kamm aus Kristalldrusen
        b.gableAlongZ(0, 6, 0, 8, 4, polishedStairs(), crystal(), cluster(), polished());
        b.set(3, 6, 0, crystal());
        b.set(3, 6, 8, crystal());
        // Einrichtung: Steinsäge, Werkbank mit Drusen, Kristallbeet im Boden
        b.bed(1, 1, 6, Blocks.BED.purple(), Direction.SOUTH);
        b.chest(5, 1, 7, chest(Direction.NORTH), ModLootTables.UMBRER_WORKSHOP);
        b.set(5, 1, 5, Blocks.STONECUTTER.defaultBlockState().setValue(StonecutterBlock.FACING, Direction.WEST));
        b.set(5, 1, 2, polishedSlab(SlabType.TOP));
        b.set(5, 1, 3, polishedSlab(SlabType.TOP));
        b.set(5, 2, 2, cluster());
        b.set(5, 2, 3, candles(2));
        b.set(1, 1, 1, barrel(Direction.UP));
        b.set(1, 1, 2, barrel(Direction.UP));
        b.set(1, 2, 1, cluster());
        b.set(3, 0, 6, crystal());
        b.set(3, 1, 6, cluster());
        b.set(2, 1, 7, cluster());
        b.set(4, 1, 7, cluster());
        b.hangingLantern(3, 6, 4, lantern());
        entrance(b, piece, 3, 3);
        piece.spawn(b, 3, 1, 3, UmbrerProfession.CRYSTAL_CUTTER);
    }

    // =====================================================================================
    // Seherturm (9 × 9, achteckig, drei Stockwerke und eine offene Warte unter dem Zeltdach)
    // =====================================================================================

    private static boolean octagonWall(int x, int z) {
        if (z == 0 || z == 8) {
            return x >= 2 && x <= 6;
        }
        if (z == 1 || z == 7) {
            return x == 1 || x == 7;
        }
        return x == 0 || x == 8;
    }

    private static boolean octagonInside(int x, int z) {
        if (z == 0 || z == 8) {
            return false;
        }
        if (z == 1 || z == 7) {
            return x >= 2 && x <= 6;
        }
        return x >= 1 && x <= 7;
    }

    private static boolean octagonDiagonal(int x, int z) {
        return (z == 1 || z == 7) && (x == 1 || x == 7);
    }

    static void seerTower(VillageBuilder b, UmbrerVillagePiece piece) {
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                if (octagonWall(x, z) || octagonInside(x, z)) {
                    b.column(x, z, bricks(), 8);
                }
            }
        }
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                boolean wall = octagonWall(x, z);
                boolean inside = octagonInside(x, z);
                if (!wall && !inside) {
                    continue;
                }
                for (int y = 0; y <= 18; y++) {
                    boolean floor = y == 0 || y == 6 || y == 12 || y == 18;
                    BlockState state;
                    if (wall) {
                        if (octagonDiagonal(x, z) && y > 0) {
                            state = pillar();
                        } else if (floor || y == 17) {
                            state = polished();
                        } else {
                            state = bricks(b, x, y, z);
                        }
                    } else if (floor) {
                        state = y == 0 ? polished() : y == 18 ? bricks() : planks();
                    } else {
                        state = Blocks.AIR.defaultBlockState();
                    }
                    b.set(x, y, z, state);
                }
                // Warte: Brüstung zwischen den Säulen, die bis unter das Dach reichen
                if (wall) {
                    if (octagonDiagonal(x, z)) {
                        b.fill(x, 19, z, x, 22, z, pillar());
                    } else {
                        b.set(x, 19, z, brickWall());
                        b.air(x, 20, z, x, 21, z);
                    }
                } else {
                    b.air(x, 19, z, x, 21, z);
                }
            }
        }
        b.door(4, 1, 0, ShadowBlocks.SHADOW_DOOR.get(), Direction.SOUTH);
        b.set(4, 3, 0, chiseled());
        for (int y : new int[] {3, 8, 14}) {
            window(b, 0, y, 4);
            window(b, 8, y, 4);
            if (y > 3) {
                window(b, 4, y, 0);
            }
        }
        b.ladder(4, 1, 18, 7, Direction.NORTH);
        // Erdgeschoss: Lesepult, Bücher, Kerzen, die Truhe des Sehers
        b.set(4, 1, 2, Blocks.LECTERN.defaultBlockState().setValue(LecternBlock.FACING, Direction.NORTH));
        b.fill(1, 1, 2, 1, 2, 3, Blocks.BOOKSHELF);
        b.fill(7, 1, 5, 7, 2, 6, Blocks.BOOKSHELF);
        b.set(1, 3, 2, candles(3));
        b.set(7, 3, 6, candles(2));
        b.chest(7, 1, 2, chest(Direction.WEST), ModLootTables.UMBRER_SEER);
        b.fill(3, 1, 4, 5, 1, 5, carpet());
        b.hangingLantern(4, 5, 4, lantern());
        // erster Stock: Schlafkammer mit dem Spiegelbecken
        b.bed(2, 7, 5, Blocks.BED.purple(), Direction.SOUTH);
        b.fill(1, 7, 2, 1, 8, 3, Blocks.BOOKSHELF);
        b.set(6, 7, 5, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 3));
        b.set(6, 7, 2, barrel(Direction.UP));
        b.set(6, 8, 2, crystal());
        b.fill(3, 7, 3, 5, 7, 4, carpet());
        b.hangingLantern(4, 11, 4, lantern());
        // zweiter Stock: der Zaubertisch zwischen acht Regalen
        b.set(4, 13, 4, Blocks.ENCHANTING_TABLE);
        for (int z = 3; z <= 5; z++) {
            b.set(2, 13, z, Blocks.BOOKSHELF);
            b.set(6, 13, z, Blocks.BOOKSHELF);
        }
        b.set(3, 13, 2, Blocks.BOOKSHELF);
        b.set(5, 13, 2, Blocks.BOOKSHELF);
        b.set(2, 14, 4, candles(3));
        b.set(6, 14, 4, candles(3));
        b.hangingLantern(4, 17, 4, lantern());
        // Warte: Mondlaternen auf der Brüstung, in der Mitte ein Nachtkristall auf einem Sockel
        b.set(4, 20, 0, lantern().defaultBlockState());
        b.set(4, 20, 8, lantern().defaultBlockState());
        b.set(0, 20, 4, lantern().defaultBlockState());
        b.set(8, 20, 4, lantern().defaultBlockState());
        b.set(4, 19, 4, chiseled());
        b.set(4, 20, 4, crystal());
        b.set(4, 21, 4, cluster());
        int top = b.hipRoof(0, 0, 8, 8, 22, brickStairs(), polished());
        b.set(4, top + 1, 4, crystal());
        b.set(4, top + 2, 4, cluster());
        entrance(b, piece, 4, 4);
        piece.spawn(b, 3, 1, 5, UmbrerProfession.SEER);
    }

    // =====================================================================================
    // Wachturm (5 × 5) an den Wegenden
    // =====================================================================================

    static void watchtower(VillageBuilder b, UmbrerVillagePiece piece) {
        b.foundation(0, 0, 4, 4, bricks(), bricks(), 10);
        b.fill(0, 0, 0, 4, 0, 4, polished());
        for (int y = 1; y <= 8; y++) {
            for (int x = 0; x <= 4; x++) {
                for (int z = 0; z <= 4; z++) {
                    boolean rimX = x == 0 || x == 4;
                    boolean rimZ = z == 0 || z == 4;
                    if (rimX && rimZ) {
                        b.set(x, y, z, pillar());
                    } else if (rimX || rimZ) {
                        b.set(x, y, z, y == 8 ? polished() : bricks(b, x, y, z));
                    } else {
                        b.set(x, y, z, y == 8 ? planks() : Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
        // offener Durchgang (Wächter öffnen keine Türen), Schießscharten, Leiter nach oben
        b.air(2, 1, 0, 2, 2, 0);
        b.set(2, 3, 0, chiseled());
        window(b, 0, 4, 2);
        window(b, 4, 4, 2);
        b.ladder(2, 1, 8, 3, Direction.NORTH);
        // Plattform mit Brüstung, Eckpfosten aus Schattenholz, Zeltdach mit Kristallspitze
        for (int x = 0; x <= 4; x++) {
            for (int z = 0; z <= 4; z++) {
                boolean rimX = x == 0 || x == 4;
                boolean rimZ = z == 0 || z == 4;
                if (rimX && rimZ) {
                    b.fill(x, 9, z, x, 11, z, log(Direction.Axis.Y));
                } else if (rimX || rimZ) {
                    b.set(x, 9, z, brickWall());
                    b.air(x, 10, z, x, 11, z);
                } else {
                    b.air(x, 9, z, x, 11, z);
                }
            }
        }
        b.set(2, 10, 0, lantern().defaultBlockState());
        b.set(2, 10, 4, lantern().defaultBlockState());
        b.set(0, 10, 2, lantern().defaultBlockState());
        b.set(4, 10, 2, lantern().defaultBlockState());
        b.fill(0, 12, 0, 4, 12, 4, planks());
        int top = b.hipRoof(-1, -1, 5, 5, 12, plankStairs(), polished());
        b.set(2, top + 1, 2, cluster());
        b.hangingLantern(2, 11, 2, lantern());
        b.hangingLantern(2, 7, 1, lantern());
        entrance(b, piece, 2, 2);
        piece.spawn(b, 2, 1, 1, UmbrerProfession.GUARD);
    }

    // =====================================================================================
    // Dämmerkornfeld (9 × 9)
    // =====================================================================================

    static void farm(VillageBuilder b, UmbrerVillagePiece piece) {
        boolean berries = b.random.nextFloat() < 0.3f;
        boolean farmer = b.random.nextBoolean();
        b.foundation(0, 0, 8, 8, soil(), soil(), 5);
        for (int x = 0; x <= 8; x++) {
            for (int z = 0; z <= 8; z++) {
                boolean rimX = x == 0 || x == 8;
                boolean rimZ = z == 0 || z == 8;
                if (rimX || rimZ) {
                    b.set(x, 0, z, log(rimZ ? Direction.Axis.X : Direction.Axis.Z));
                } else if (x == 4) {
                    b.set(x, -1, z, soil());
                    b.set(x, 0, z, Blocks.WATER);
                } else if (berries && x > 4) {
                    b.set(x, 0, z, grass());
                    int age = b.chance(x, 1, z, 0.5f) ? 3 : 2;
                    b.set(x, 1, z, ShadowBlocks.SOUL_BERRY_BUSH.get().defaultBlockState().setValue(SoulBerryBushBlock.AGE, age));
                } else {
                    b.set(x, 0, z, ShadowBlocks.SHADOW_FARMLAND.get().defaultBlockState().setValue(FarmlandBlock.MOISTURE, 7));
                    int age = Math.min(7, 3 + (int) (b.noise(x, 1, z) * 6.0f));
                    b.set(x, 1, z, ShadowBlocks.DUSKGRAIN.get().defaultBlockState().setValue(CropBlock.AGE, age));
                }
            }
        }
        // Laternen an zwei Ecken, ein Komposter
        for (int[] c : new int[][] {{0, 0}, {8, 8}}) {
            b.set(c[0], 1, c[1], fence());
            b.set(c[0], 2, c[1], fence());
            b.set(c[0], 3, c[1], lantern().defaultBlockState());
        }
        b.set(8, 1, 0, Blocks.COMPOSTER.defaultBlockState().setValue(ComposterBlock.LEVEL, 2));
        entrance(b, piece, 4, 4);
        if (farmer) {
            piece.spawn(b, 0, 1, 4, UmbrerProfession.FARMER);
        }
    }

    // =====================================================================================
    // Kristallgarten (7 × 7)
    // =====================================================================================

    static void crystalGarden(VillageBuilder b, UmbrerVillagePiece piece) {
        b.foundation(0, 0, 6, 6, polished(), soil(), 8);
        for (int x = 0; x <= 6; x++) {
            for (int z = 0; z <= 6; z++) {
                boolean rim = x == 0 || x == 6 || z == 0 || z == 6;
                if (rim) {
                    b.set(x, 0, z, polished());
                    boolean corner = (x == 0 || x == 6) && (z == 0 || z == 6);
                    if (corner) {
                        b.set(x, 1, z, bricks());
                        b.set(x, 2, z, lantern().defaultBlockState());
                    } else if (!(x == 3 && z == 0)) {
                        b.set(x, 1, z, brickWall());
                    }
                } else if (x == 3 || z == 3) {
                    b.set(x, 0, z, polished());
                } else if (b.chance(x, 0, z, 0.45f)) {
                    b.set(x, 0, z, ShadowBlocks.GLIMMER_MOSS.get());
                } else {
                    b.set(x, 0, z, grass());
                    float n = b.noise(x, 1, z);
                    if (n < 0.3f) {
                        b.set(x, 1, z, ShadowBlocks.GHOSTCAP.get());
                    } else if (n < 0.55f) {
                        b.set(x, 1, z, ShadowBlocks.SHADOW_SPROUTS.get());
                    }
                }
            }
        }
        // mittendrin eine Säule aus Nachtkristall, ringsum kleinere Drusen
        b.set(3, 0, 3, chiseled());
        b.fill(3, 1, 3, 3, 3, 3, crystal());
        b.set(3, 4, 3, cluster());
        b.set(2, 1, 2, crystal());
        b.set(2, 2, 2, cluster());
        b.set(4, 1, 4, crystal());
        b.set(4, 2, 4, cluster());
        b.set(4, 1, 2, cluster());
        b.set(2, 1, 4, cluster());
        // Bänke zum Betrachten
        b.set(1, 1, 3, VillageBuilder.stairs(polishedStairs(), Direction.WEST, Half.BOTTOM));
        b.set(5, 1, 3, VillageBuilder.stairs(polishedStairs(), Direction.EAST, Half.BOTTOM));
        b.set(3, 1, 5, VillageBuilder.stairs(polishedStairs(), Direction.SOUTH, Half.BOTTOM));
        entrance(b, piece, 3, 3);
    }
}
