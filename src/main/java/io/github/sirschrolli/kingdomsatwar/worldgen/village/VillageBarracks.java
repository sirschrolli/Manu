package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.at;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.barrel;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.chest;
import static io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuildings.facing;

import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CarvedPumpkinBlock;
import net.minecraft.world.level.block.GrindstoneBlock;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.util.ProblemReporter;

/**
 * Das Söldnerhaus (13 × 11): vorn ein umzäunter Übungshof mit Toreinfahrt, Strohzielen, einer Übungspuppe und der
 * Söldnertafel, an der die Söldner auf Arbeit warten; dahinter das Haus mit Schankraum (lange Tafel, Kamin, Vorräte)
 * und oben der Schlafsaal mit Kartentisch. In jeder Bauweise des Dorfs: Fachwerk, nordisch, Lehm und Pfahlbau.
 */
final class VillageBarracks {
    private VillageBarracks() {}

    /** Die Plätze im Hof, an denen die Söldner warten (neben dem Weg, vor der Tafel – deren Leseplatz bleibt frei). */
    static final List<BlockPos> SPOTS = List.of(at(8, 1, 1), at(9, 1, 1), at(10, 1, 1), at(8, 1, 2), at(10, 1, 2));

    static void mercenaryHall(Arch a, KingdomVillagePiece piece) {
        VillageBuilder b = a.b;
        VillageStyle style = a.style;
        a.foundation(0, 0, 12, 10, 17);
        a.floor(0, 4, 12, 10);
        yard(a);
        // vor dem Tor: der Weg zur Straße (zuerst, damit der Eingang des Hauses dort liegt)
        VillageBuildings.entrance(a, piece, 5, 7);

        // ---------------------------------------------------------------- Haus
        a.walls(0, 4, 12, 10, 1, 4);
        a.walls(0, 4, 12, 10, 5, 8, true);
        int top = VillageBuildings.roof(a, 0, 4, 12, 10, 8, false);
        b.air(1, 1, 5, 11, 4, 9);
        b.air(1, 6, 5, 11, 7, 9);
        VillageBuildings.upperFloor(a, 1, 5, 11, 9, 5);
        VillageBuildings.staircase(a, 11, 9, 5);
        for (int z = 6; z <= 9; z++) {
            b.set(10, 6, z, a.p.trim.fence().defaultBlockState());
        }
        a.door(6, 4, Direction.SOUTH, ModBlocks.CASTLE_DOOR.get());
        // vorn unten keine Fenster: davor stehen die Ziele und warten die Söldner (Läden und Blumenkästen wären im Weg)
        a.window(3, 6, 4, Direction.NORTH, 2);
        a.window(9, 6, 4, Direction.NORTH, 2);
        a.window(0, 2, 7, Direction.WEST, 2);
        a.window(0, 6, 8, Direction.WEST, 2);
        a.window(12, 6, 7, Direction.EAST, 2);
        a.window(12, 2, 6, Direction.EAST, 2);
        a.window(3, 2, 10, Direction.SOUTH, 2);
        a.window(8, 2, 10, Direction.SOUTH, 2);
        a.window(6, 6, 10, Direction.SOUTH, 2);
        // Banner der Söldner beiderseits der Tür
        Block banner = banner(style);
        b.set(4, 3, 3, banner.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
        b.set(8, 3, 3, banner.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));

        // ---------------------------------------------------------------- Schankraum
        // Kamin an der linken Wand
        BlockState hearth = style == VillageStyle.NORDIC ? a.p.stone() : Blocks.BRICKS.defaultBlockState();
        b.set(1, 1, 7, Blocks.CAMPFIRE.defaultBlockState().setValue(CampfireBlock.LIT, true));
        b.set(1, 1, 6, hearth);
        b.set(1, 1, 8, hearth);
        b.fill(1, 2, 6, 1, 2, 8, hearth);
        a.chimney(1, 7, 3, top);
        // lange Tafel mit Bänken, ein Festmahl und Krüge
        for (int x = 4; x <= 8; x++) {
            b.set(x, 1, 7, a.table());
            b.set(x, 1, 6, a.bench(Direction.SOUTH));
            b.set(x, 1, 8, a.bench(Direction.NORTH));
        }
        b.set(6, 2, 7, ModBlocks.FEAST.get().defaultBlockState());
        b.set(4, 2, 7, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.CANDLES, 3)
                .setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(8, 2, 7, a.flower(8, 2, 7));
        // Vorräte und Waffenpflege an der Rückwand, ein Pult mit dem Soldbuch
        chest(a, 2, 1, 9, Direction.NORTH, ModLootTables.VILLAGE_WATCHTOWER);
        b.set(3, 1, 9, barrel(Direction.UP));
        b.set(2, 1, 5, barrel(Direction.SOUTH));
        b.set(8, 1, 9, Blocks.GRINDSTONE.defaultBlockState().setValue(GrindstoneBlock.FACE, AttachFace.FLOOR)
                .setValue(GrindstoneBlock.FACING, Direction.EAST));
        b.set(9, 1, 9, facing(Blocks.LECTERN, Direction.NORTH));
        b.set(5, 3, 9, banner.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
        b.set(7, 3, 9, banner.defaultBlockState().setValue(WallBannerBlock.FACING, Direction.NORTH));
        a.hangingLantern(6, 4, 7);
        a.hangingLantern(9, 4, 6);
        a.hangingLantern(3, 4, 6);

        // ---------------------------------------------------------------- Schlafsaal
        for (int x : new int[] {2, 4, 6, 8}) {
            b.bed(x, 6, 8, a.bed(false), Direction.SOUTH);
        }
        b.set(3, 6, 9, barrel(Direction.UP));
        b.set(3, 7, 9, a.p.lantern.defaultBlockState());
        chest(a, 5, 6, 9, Direction.NORTH, ModLootTables.VILLAGE_HOUSE);
        b.set(7, 6, 9, barrel(Direction.UP));
        b.set(7, 7, 9, a.p.lantern.defaultBlockState());
        // vorn der Kartentisch mit Kerze, Pfeilmacherbank und Rüstzeug
        b.set(3, 6, 5, Blocks.CARTOGRAPHY_TABLE.defaultBlockState());
        b.set(3, 7, 5, Blocks.CANDLE.defaultBlockState().setValue(net.minecraft.world.level.block.CandleBlock.LIT, true));
        b.set(2, 6, 5, a.chair(Direction.EAST));
        b.set(6, 6, 5, Blocks.FLETCHING_TABLE.defaultBlockState());
        b.set(7, 6, 5, Blocks.SMITHING_TABLE.defaultBlockState());
        b.set(7, 7, 5, a.p.lantern.defaultBlockState());
        for (int x = 2; x <= 9; x++) {
            b.set(x, 6, 7, a.carpet().defaultBlockState());
        }

        // ---------------------------------------------------------------- Söldnertafel
        // an der Hauswand rechts der Tür, mit den Anschlägen zur Straße: schon durch das Tor zu sehen
        piece.mercenaryBoard(b, 9, 1, 3, Direction.NORTH, SPOTS);
    }

    /** Der Hof vor dem Haus: Boden, Umfriedung mit Tor, Ziele, Übungspuppe, Bank und Laternenpfahl. */
    private static void yard(Arch a) {
        VillageBuilder b = a.b;
        VillageStyle style = a.style;
        if (style == VillageStyle.STILT) {
            a.floor(0, 0, 12, 3);
        }
        for (int x = 0; x <= 12; x++) {
            for (int z = 0; z <= 3; z++) {
                boolean path = x >= 5 && x <= 7;
                if (style != VillageStyle.STILT) {
                    b.set(x, 0, z, ground(a, x, z, path));
                }
                boolean rim = (x == 0 || x == 12 || z == 0) && !(z == 0 && x >= 4 && x <= 8);
                if (rim) {
                    b.set(x, 1, z, enclosure(a));
                }
            }
        }
        // Toreinfahrt: zwei Pfosten, ein Balken darüber, Laternen obenauf und ein Schild in der Mitte
        for (int x : new int[] {4, 8}) {
            for (int y = 1; y <= 3; y++) {
                b.set(x, y, 0, a.p.post(Direction.Axis.Y));
            }
            a.standingLantern(x, 5, 0);
        }
        for (int x = 4; x <= 8; x++) {
            b.set(x, 4, 0, a.p.beam(Direction.Axis.X));
        }
        BlockPos sign = b.world(6, 3, 0);
        b.set(6, 3, 0, a.p.wood.hangingSign().defaultBlockState());
        if (b.inside(sign) && b.level.getBlockEntity(sign) instanceof SignBlockEntity entity) {
            // beim Erzeugen der Welt hat das Schild noch keine Welt: die Schrift wird wie beim Laden gesetzt (setText würde
            // die Welt benachrichtigen wollen)
            SignText text = SignText.EMPTY.asMutable().setLine(1, Component.translatable("sign.kingdomsatwar.mercenaries")).asImmutable();
            TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, b.level.registryAccess());
            out.store("front_text", SignText.CODEC, text);
            out.store("back_text", SignText.CODEC, text);
            entity.loadCustomOnly(TagValueInput.create(ProblemReporter.DISCARDING, b.level.registryAccess(), out.buildResult()));
            entity.setChanged();
        }
        // links: Strohziele an der Umfriedung und eine Übungspuppe mit Kürbiskopf
        b.set(1, 1, 1, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(1, 2, 1, Blocks.TARGET.defaultBlockState());
        b.set(1, 1, 2, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(1, 1, 3, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(1, 2, 3, Blocks.TARGET.defaultBlockState());
        b.set(3, 1, 2, a.p.trim.fence().defaultBlockState());
        b.set(3, 2, 2, Blocks.HAY_BLOCK.defaultBlockState());
        b.set(3, 3, 2, Blocks.CARVED_PUMPKIN.defaultBlockState().setValue(CarvedPumpkinBlock.FACING, Direction.EAST));
        // rechts: Laternenpfahl, Bank und Fass neben der Tafel
        b.set(11, 1, 1, a.p.post(Direction.Axis.Y));
        b.set(11, 2, 1, a.p.post(Direction.Axis.Y));
        a.standingLantern(11, 3, 1);
        b.set(11, 1, 2, a.bench(Direction.WEST));
        b.set(11, 1, 3, barrel(Direction.UP));
    }

    /** Boden des Hofs: festgetretene Erde mit Kies (im Lehmdorf Sand), der Weg zum Haus gepflastert bzw. ausgetreten. */
    private static BlockState ground(Arch a, int x, int z, boolean path) {
        float n = a.b.noise(x, 0, z);
        return switch (a.style) {
            case CLAY -> path ? Blocks.SMOOTH_SANDSTONE.defaultBlockState()
                    : n < 0.35f ? Blocks.SAND.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            case NORDIC -> path ? (n < 0.5f ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.MOSSY_COBBLESTONE.defaultBlockState())
                    : n < 0.4f ? Blocks.GRAVEL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
            default -> path ? Blocks.DIRT_PATH.defaultBlockState()
                    : n < 0.3f ? Blocks.GRAVEL.defaultBlockState() : Blocks.COARSE_DIRT.defaultBlockState();
        };
    }

    private static BlockState enclosure(Arch a) {
        return switch (a.style) {
            case CLAY, NORDIC -> a.p.stoneWall();
            default -> a.p.fence();
        };
    }

    private static Block banner(VillageStyle style) {
        return switch (style) {
            case TIMBER -> Blocks.WALL_BANNER.red();
            case NORDIC -> Blocks.WALL_BANNER.blue();
            case CLAY -> Blocks.WALL_BANNER.orange();
            case STILT -> Blocks.WALL_BANNER.green();
        };
    }
}
