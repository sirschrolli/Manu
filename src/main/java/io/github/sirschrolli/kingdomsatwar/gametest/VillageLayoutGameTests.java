package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuilding;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageLayout;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Dorfanlagen: Jeder Grundriss (Haufendorf, Straßendorf, Angerdorf, Rundling) lässt sich in jeder passenden
 * Bauweise planen – mit Gasthaus, Schmiede und Söldnerhaus, ohne dass sich Gebäude, Wege und Platz überschneiden, jede
 * Haustür an einem Weg; der Zufall wählt verschiedene Grundrisse; Anger und Dorfwiese entstehen mit Teich, Brunnen bzw.
 * Linde.
 */
final class VillageLayoutGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGE_LAYOUTS =
            ModGameTests.TEST_FUNCTIONS.register("village_layouts", () -> VillageLayoutGameTests::layouts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGE_GREENS =
            ModGameTests.TEST_FUNCTIONS.register("village_greens", () -> VillageLayoutGameTests::greens);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UMBRER_LAYOUTS =
            ModGameTests.TEST_FUNCTIONS.register("umbrer_layouts", () -> VillageLayoutGameTests::umbrerLayouts);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> UMBRER_PLAZAS =
            ModGameTests.TEST_FUNCTIONS.register("umbrer_plazas", () -> VillageLayoutGameTests::umbrerPlazas);

    private VillageLayoutGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(VILLAGE_LAYOUTS, VILLAGE_GREENS, UMBRER_LAYOUTS, UMBRER_PLAZAS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Planung
    // =====================================================================================

    private static void layouts(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        int villages = 0;
        int buildings = 0;
        for (VillageStyle style : VillageStyle.values()) {
            for (VillageLayout layout : VillageLayout.values()) {
                if (!layout.suits(style)) {
                    continue;
                }
                for (long seed = 1; seed <= 6; seed++) {
                    String name = style.id() + "/" + layout.id() + "#" + seed;
                    List<KingdomVillagePiece> pieces = KingdomVillageStructure.planFlat(style, layout, 0, 0, 64, seed * 7919L);
                    if (pieces.isEmpty()) {
                        problems.add(name + ": kein Dorf");
                        continue;
                    }
                    villages++;
                    KingdomVillagePiece square = pieces.getFirst();
                    if (square.kind() != KingdomVillagePiece.Kind.SQUARE || square.layout() != layout) {
                        problems.add(name + ": Grundriss " + square.layout().id());
                    }
                    buildings += check(name, pieces, layout, problems);
                }
            }
        }
        // ohne Vorgabe wählt der Zufall verschiedene Grundrisse
        for (VillageStyle style : VillageStyle.values()) {
            Set<VillageLayout> seen = EnumSet.noneOf(VillageLayout.class);
            for (long seed = 1; seed <= 40; seed++) {
                List<KingdomVillagePiece> pieces = KingdomVillageStructure.planFlat(style, null, 0, 0, 64, seed * 104729L);
                if (!pieces.isEmpty()) {
                    seen.add(pieces.getFirst().layout());
                }
            }
            int expected = style == VillageStyle.STILT ? 2 : 4;
            if (seen.size() < expected) {
                problems.add(style.id() + ": nur die Grundrisse " + seen);
            }
            if (style == VillageStyle.STILT && (seen.contains(VillageLayout.ANGERDORF) || seen.contains(VillageLayout.RUNDLING))) {
                problems.add("Pfahldorf mit Dorfwiese");
            }
        }
        KingdomsAtWar.LOGGER.info("[Test] Dorfanlagen: {} Dörfer mit {} Gebäuden geplant", villages, buildings);
        helper.assertTrue(problems.isEmpty(), "Dorfanlagen: " + String.join("; ", problems.subList(0, Math.min(12, problems.size()))));
        helper.succeed();
    }

    /** Prüft ein geplantes Dorf; gibt die Zahl der Gebäude zurück. */
    private static int check(String name, List<KingdomVillagePiece> pieces, VillageLayout layout, List<String> problems) {
        Map<VillageBuilding, Integer> kinds = new EnumMap<>(VillageBuilding.class);
        List<BoundingBox> lots = new ArrayList<>();
        List<KingdomVillagePiece> roads = new ArrayList<>();
        for (KingdomVillagePiece piece : pieces) {
            if (piece.kind() == KingdomVillagePiece.Kind.ROAD) {
                roads.add(piece);
            }
        }
        for (KingdomVillagePiece piece : pieces) {
            VillageBuilding kind = piece.building();
            if (kind == null) {
                continue;
            }
            kinds.merge(kind, 1, Integer::sum);
            BoundingBox lot = footprint(piece.origin(), piece.rotation(), kind.width, kind.depth);
            for (BoundingBox other : lots) {
                if (other.intersects(lot)) {
                    problems.add(name + ": " + kind.id() + " überschneidet ein anderes Gebäude");
                }
            }
            lots.add(lot);
            for (KingdomVillagePiece road : roads) {
                if (roadArea(road).intersects(lot)) {
                    problems.add(name + ": " + kind.id() + " steht auf einem Weg");
                    break;
                }
            }
            if (plaza(pieces.getFirst()).intersects(lot)) {
                problems.add(name + ": " + kind.id() + " steht auf dem Platz");
            }
            // vor der Haustür (Mitte der Vorderseite, ein Block davor) liegt ein Weg – außer bei Feldern hinter den Häusern
            BlockPos door = piece.origin().offset(new BlockPos(kind.width / 2, 0, -1).rotate(piece.rotation()));
            boolean onRoad = false;
            for (KingdomVillagePiece road : roads) {
                if (roadArea(road).inflatedBy(2, 0, 2).isInside(door.getX(), 0, door.getZ())) {
                    onRoad = true;
                    break;
                }
            }
            boolean behindOrEnd = kind == VillageBuilding.FIELD || kind == VillageBuilding.WATCHTOWER || kind == VillageBuilding.WINDMILL
                    || kind == VillageBuilding.BARN || kind == VillageBuilding.SHEEPFOLD;
            if (!onRoad && !behindOrEnd) {
                problems.add(name + ": " + kind.id() + " ohne Weg vor der Tür");
            }
        }
        int total = kinds.values().stream().mapToInt(Integer::intValue).sum();
        if (total < 12) {
            problems.add(name + ": nur " + total + " Gebäude");
        }
        for (VillageBuilding core : List.of(VillageBuilding.TAVERN, VillageBuilding.SMITHY, VillageBuilding.MERCENARY_HALL)) {
            if (!kinds.containsKey(core)) {
                problems.add(name + ": ohne " + core.id());
            }
        }
        if (layout == VillageLayout.ANGERDORF && !kinds.containsKey(VillageBuilding.CHAPEL)) {
            problems.add(name + ": Angerdorf ohne Kapelle");
        }
        if (layout == VillageLayout.STRASSENDORF) {
            long arms = roads.stream().filter(r -> r.skip() > 0 && r.origin().equals(pieces.getFirst().origin())).count();
            if (arms < 2) {
                problems.add(name + ": Straßendorf ohne durchgehende Straße");
            }
        }
        return total;
    }

    private static BoundingBox footprint(BlockPos origin, Rotation rotation, int width, int depth) {
        BlockPos far = origin.offset(new BlockPos(width - 1, 0, depth - 1).rotate(rotation));
        return new BoundingBox(Math.min(origin.getX(), far.getX()), 0, Math.min(origin.getZ(), far.getZ()), Math.max(origin.getX(), far.getX()), 0,
                Math.max(origin.getZ(), far.getZ()));
    }

    /** Die Fläche eines Wegs (ab dem ersten gebauten Schritt, drei Blöcke breit). */
    private static BoundingBox roadArea(KingdomVillagePiece road) {
        Direction dir = road.direction();
        int length = road.heights().length - 1;
        int x0 = road.origin().getX() + dir.getStepX() * road.skip();
        int z0 = road.origin().getZ() + dir.getStepZ() * road.skip();
        int x1 = road.origin().getX() + dir.getStepX() * length;
        int z1 = road.origin().getZ() + dir.getStepZ() * length;
        int px = dir.getStepX() == 0 ? 1 : 0;
        int pz = dir.getStepZ() == 0 ? 1 : 0;
        return new BoundingBox(Math.min(x0, x1) - px, 0, Math.min(z0, z1) - pz, Math.max(x0, x1) + px, 0, Math.max(z0, z1) + pz);
    }

    /** Platz bzw. Dorfwiese des Dorfs. */
    private static BoundingBox plaza(KingdomVillagePiece square) {
        BlockPos c = square.origin();
        int r = square.radius();
        if (square.layout() == VillageLayout.ANGERDORF) {
            int hl = square.heights()[0];
            boolean alongX = square.direction().getAxis() == Direction.Axis.X;
            int ax = alongX ? hl : r;
            int az = alongX ? r : hl;
            return new BoundingBox(c.getX() - ax, 0, c.getZ() - az, c.getX() + ax, 0, c.getZ() + az);
        }
        return new BoundingBox(c.getX() - r, 0, c.getZ() - r, c.getX() + r, 0, c.getZ() + r);
    }

    // =====================================================================================
    // Anger und Dorfwiese
    // =====================================================================================

    /**
     * Baut den Anger (Fachwerk, längs Nord-Süd) und die Dorfwiese (Fachwerk, nordisch) mitten in die Testfläche: Rasen,
     * Teich mit Wasser, Brunnen bzw. Linde und Maibaum stehen.
     */
    private static void greens(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        record Case(VillageStyle style, VillageLayout layout) {}
        for (Case c : List.of(new Case(VillageStyle.TIMBER, VillageLayout.ANGERDORF), new Case(VillageStyle.CLAY, VillageLayout.ANGERDORF),
                new Case(VillageStyle.TIMBER, VillageLayout.RUNDLING), new Case(VillageStyle.NORDIC, VillageLayout.RUNDLING))) {
            VillageGameTests.clear(helper);
            BlockPos center = helper.absolutePos(new BlockPos(24, 0, 24));
            KingdomVillagePiece square = null;
            for (long seed = 1; seed < 40 && square == null; seed++) {
                List<KingdomVillagePiece> pieces = KingdomVillageStructure.planFlat(c.style(), c.layout(), center.getX(), center.getZ(),
                        center.getY(), seed);
                // der Anger muss in die Testfläche passen (längs höchstens 21 Blöcke um die Mitte)
                if (!pieces.isEmpty() && pieces.getFirst().layout() == c.layout()
                        && (c.layout() != VillageLayout.ANGERDORF || pieces.getFirst().heights()[0] <= 17)) {
                    square = pieces.getFirst();
                }
            }
            String name = c.style().id() + "/" + c.layout().id();
            if (square == null) {
                problems.add(name + ": nicht geplant");
                continue;
            }
            BoundingBox box = square.getBoundingBox();
            square.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(3),
                    new BoundingBox(box.minX(), level.getMinY(), box.minZ(), box.maxX(), level.getMaxY(), box.maxZ()), ChunkPos.containing(center),
                    center);
            int grass = 0;
            int water = 0;
            int logs = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.minX(), center.getY(), box.minZ(), box.maxX(), center.getY() + 12, box.maxZ())) {
                var state = level.getBlockState(pos);
                if (state.is(Blocks.GRASS_BLOCK) || state.is(Blocks.SAND)) {
                    grass++;
                } else if (state.is(Blocks.WATER)) {
                    water++;
                } else if (state.is(net.minecraft.tags.BlockTags.LOGS)) {
                    logs++;
                }
            }
            if (grass < 60) {
                problems.add(name + ": nur " + grass + " Rasen");
            }
            if (c.layout() == VillageLayout.ANGERDORF && water < 12) {
                problems.add(name + ": kein Teich (" + water + " Wasser)");
            }
            if (c.style() != VillageStyle.NORDIC && logs < 4) {
                problems.add(name + ": keine Bäume (" + logs + " Stämme)");
            }
        }
        VillageGameTests.clear(helper);
        helper.assertTrue(problems.isEmpty(), "Dorfwiesen: " + String.join("; ", problems));
        helper.succeed();
    }

    // =====================================================================================
    // Dörfer der Umbrer
    // =====================================================================================

    /**
     * Jeder Grundriss der Umbrer (Stern, Gasse, Dreiweg, Ring) lässt sich planen: mit Schmiede, Kräuterhütte und
     * Kristallwerkstatt, ohne Überschneidungen, jede Tür an einem Weg; der Zufall wählt verschiedene Grundrisse und
     * Mittelstücke.
     */
    private static void umbrerLayouts(GameTestHelper helper) {
        List<String> problems = new ArrayList<>();
        for (var layout : io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.Layout.values()) {
            for (long seed = 1; seed <= 5; seed++) {
                String name = layout.name().toLowerCase(java.util.Locale.ROOT) + "#" + seed;
                var pieces = io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.planFlat(0, 0, 70, seed * 6151L, layout, -1);
                if (pieces.isEmpty()) {
                    problems.add(name + ": kein Dorf");
                    continue;
                }
                String signature = umbrerSignature(pieces);
                String expected = switch (layout) {
                    case STAR -> "4/0";
                    case LANE -> "2/0";
                    case FORK -> "3/0";
                    case RING -> "2/4";
                };
                if (!signature.equals(expected)) {
                    problems.add(name + ": Wege " + signature + " statt " + expected);
                }
                umbrerCheck(name, pieces, problems);
            }
        }
        Set<String> layouts = new java.util.HashSet<>();
        Set<Integer> centerpieces = new java.util.HashSet<>();
        for (long seed = 1; seed <= 40; seed++) {
            var pieces = io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.planFlat(0, 0, 70, seed * 7907L, null, -1);
            if (!pieces.isEmpty()) {
                layouts.add(umbrerSignature(pieces));
                centerpieces.add(pieces.getFirst().centerpiece());
            }
        }
        if (layouts.size() < 4) {
            problems.add("nur die Grundrisse " + layouts);
        }
        if (centerpieces.size() < 4) {
            problems.add("nur die Mittelstücke " + centerpieces);
        }
        helper.assertTrue(problems.isEmpty(), "Umbrer-Dörfer: " + String.join("; ", problems.subList(0, Math.min(12, problems.size()))));
        helper.succeed();
    }

    /** "Wege vom Platz / Stücke des Rings". */
    private static String umbrerSignature(List<io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece> pieces) {
        BlockPos center = pieces.getFirst().origin();
        int arms = 0;
        int ring = 0;
        for (var piece : pieces) {
            if (piece.kind() == io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece.Kind.ROAD) {
                if (piece.origin().equals(center)) {
                    arms++;
                } else {
                    ring++;
                }
            }
        }
        return arms + "/" + ring;
    }

    private static void umbrerCheck(String name, List<io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece> pieces,
            List<String> problems) {
        java.util.Map<String, Integer> kinds = new java.util.TreeMap<>();
        List<BoundingBox> lots = new ArrayList<>();
        List<BoundingBox> roads = new ArrayList<>();
        for (var piece : pieces) {
            if (piece.kind() == io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece.Kind.ROAD) {
                Direction dir = piece.roadDirection();
                int x0 = piece.origin().getX() + dir.getStepX() * piece.roadSkip();
                int z0 = piece.origin().getZ() + dir.getStepZ() * piece.roadSkip();
                int x1 = piece.origin().getX() + dir.getStepX() * piece.roadLength();
                int z1 = piece.origin().getZ() + dir.getStepZ() * piece.roadLength();
                int px = dir.getStepX() == 0 ? 1 : 0;
                int pz = dir.getStepZ() == 0 ? 1 : 0;
                roads.add(new BoundingBox(Math.min(x0, x1) - px, 0, Math.min(z0, z1) - pz, Math.max(x0, x1) + px, 0, Math.max(z0, z1) + pz));
            }
        }
        for (var piece : pieces) {
            var kind = piece.kind();
            if (kind == io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece.Kind.ROAD
                    || kind == io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece.Kind.PLAZA) {
                continue;
            }
            kinds.merge(kind.name(), 1, Integer::sum);
            BoundingBox lot = footprint(piece.origin(), piece.rotation(), kind.width(), kind.depth());
            for (BoundingBox other : lots) {
                if (other.intersects(lot)) {
                    problems.add(name + ": " + kind + " überschneidet ein anderes Gebäude");
                }
            }
            lots.add(lot);
            for (BoundingBox road : roads) {
                if (road.intersects(lot)) {
                    problems.add(name + ": " + kind + " steht auf einem Weg");
                    break;
                }
            }
            BlockPos door = piece.origin().offset(new BlockPos(kind.width() / 2, 0, -1).rotate(piece.rotation()));
            boolean onRoad = roads.stream().anyMatch(r -> r.inflatedBy(2, 0, 2).isInside(door.getX(), 0, door.getZ()));
            if (!onRoad) {
                problems.add(name + ": " + kind + " ohne Weg vor der Tür");
            }
        }
        int total = kinds.values().stream().mapToInt(Integer::intValue).sum();
        if (total < 8) {
            problems.add(name + ": nur " + total + " Gebäude");
        }
        for (String core : List.of("SMITHY", "HERB_HUT", "CRYSTAL_WORKSHOP")) {
            if (!kinds.containsKey(core)) {
                problems.add(name + ": ohne " + core);
            }
        }
    }

    /** Alle vier Mittelstücke der Umbrer-Plätze entstehen: Mondbrunnen, Kristallhain, Schattenbaum, Steinkreis. */
    private static void umbrerPlazas(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        BlockPos center = helper.absolutePos(new BlockPos(24, 0, 24));
        for (int centerpiece = 0; centerpiece < 4; centerpiece++) {
            VillageGameTests.clear(helper);
            var pieces = io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillageStructure.planFlat(center.getX(), center.getZ(),
                    center.getY(), 99L + centerpiece, null, centerpiece);
            if (pieces.isEmpty()) {
                problems.add(centerpiece + ": nicht geplant");
                continue;
            }
            var plaza = pieces.getFirst();
            BoundingBox box = plaza.getBoundingBox();
            plaza.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(5),
                    new BoundingBox(box.minX(), level.getMinY(), box.minZ(), box.maxX(), level.getMaxY(), box.maxZ()), ChunkPos.containing(center),
                    center);
            int water = 0;
            int crystal = 0;
            int logs = 0;
            int stones = 0;
            for (BlockPos pos : BlockPos.betweenClosed(box.minX(), center.getY() - 2, box.minZ(), box.maxX(), center.getY() + 11, box.maxZ())) {
                var state = level.getBlockState(pos);
                if (state.is(Blocks.WATER)) {
                    water++;
                } else if (state.is(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.NIGHT_CRYSTAL.get())) {
                    crystal++;
                } else if (state.is(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_LOG.get())) {
                    logs++;
                } else if (state.is(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.DEEP_SHADOW_STONE.get())) {
                    stones++;
                }
            }
            boolean ok = switch (centerpiece) {
                case 0 -> water >= 9;
                case 1 -> crystal >= 8;
                case 2 -> logs >= 10;
                default -> stones >= 16;
            };
            if (!ok) {
                problems.add(centerpiece + ": Wasser " + water + ", Kristall " + crystal + ", Stamm " + logs + ", Stein " + stones);
            }
            level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new net.minecraft.world.phys.AABB(box.minX(), box.minY(), box.minZ(),
                    box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)).forEach(net.minecraft.world.entity.Entity::discard);
        }
        VillageGameTests.clear(helper);
        helper.assertTrue(problems.isEmpty(), "Umbrer-Plätze: " + String.join("; ", problems));
        helper.succeed();
    }
}
