package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.UmbrerVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuilding;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Bauprüfung aller Gebäude (siehe {@link BuildAudit}): jedes Dorfgebäude in jeder Bauweise, Spielart und Drehung, jeder
 * Entwurf der eigenen Siedlung in jeder Drehung und jedes Gebäude der Umbrer in jeder Drehung. Gemeldet wird alles auf
 * einmal, mit der Stelle in Gebäude-Koordinaten.
 */
final class AuditGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    private static final List<DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>>> TESTS = new ArrayList<>();

    static {
        for (VillageStyle style : VillageStyle.values()) {
            TESTS.add(ModGameTests.TEST_FUNCTIONS.register("audit_village_" + style.id(), () -> helper -> villages(helper, style)));
        }
        for (Rotation rotation : Rotation.values()) {
            TESTS.add(ModGameTests.TEST_FUNCTIONS.register("audit_town_" + rotation.ordinal(), () -> helper -> town(helper, rotation)));
        }
        TESTS.add(ModGameTests.TEST_FUNCTIONS.register("audit_umbrer", () -> AuditGameTests::umbrer));
    }

    private AuditGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : TESTS) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, TOWN, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================

    /** Jedes Dorfgebäude einer Bauweise in allen vier Drehungen (je Drehung eine andere Spielart und ein anderer Zufall). */
    private static void villages(GameTestHelper helper, VillageStyle style) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        for (VillageBuilding kind : VillageBuilding.values()) {
            for (Rotation rotation : Rotation.values()) {
                TownGameTests.clearArea(helper);
                int floor = style == VillageStyle.STILT ? 2 : 0;
                BlockPos origin = centered(helper, new BlockPos(24, floor, 24), kind.width, kind.depth, rotation);
                long seed = 31L * kind.ordinal() + style.ordinal() + 1000L * rotation.ordinal();
                KingdomVillagePiece piece = KingdomVillagePiece.single(style, rotation.ordinal() % 3, kind, origin, rotation, seed);
                BoundingBox box = piece.getBoundingBox();
                BoundingBox all = new BoundingBox(box.minX(), level.getMinY(), box.minZ(), box.maxX(), level.getMaxY(), box.maxZ());
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(7), all,
                        ChunkPos.containing(origin), origin);
                String name = style.id() + "/" + kind.id() + " " + rotation.name().toLowerCase();
                BoundingBox scan = new BoundingBox(box.minX(), origin.getY() - 2, box.minZ(), box.maxX(), origin.getY() + kind.height + 2, box.maxZ());
                for (String p : BuildAudit.check(level, scan, local(origin, rotation))) {
                    problems.add(name + ": " + p);
                }
            }
        }
        TownGameTests.clearArea(helper);
        report(helper, "Dorf " + style.id(), problems);
    }

    /** Jeder Entwurf jedes Gebäudes der eigenen Siedlung in einer Drehung. */
    private static void town(GameTestHelper helper, Rotation rotation) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        for (BuildingType type : BuildingType.standard()) {
            for (int design = 0; design < type.designs(); design++) {
                Blueprint plan = type.blueprint(design);
                TownGameTests.clearArea(helper);
                BlockPos origin = TownGameTests.originFor(helper, plan, 24, 24);
                plan.placeAll(level, origin, rotation);
                Map<BlockPos, BlockPos> local = new HashMap<>();
                for (Blueprint.Entry e : plan.blocks()) {
                    local.put(Blueprint.world(origin, rotation, e.pos()), e.pos());
                }
                Function<BlockPos, String> where = pos -> {
                    BlockPos l = local.get(pos);
                    return l != null ? "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")" : "Welt " + helper.relativePos(pos);
                };
                String name = type.id() + (type.designs() > 1 ? " Entwurf " + design : "") + " " + rotation.name().toLowerCase();
                for (String p : BuildAudit.check(level, plan.worldBounds(origin, rotation), where)) {
                    problems.add(name + ": " + p);
                }
            }
        }
        TownGameTests.clearArea(helper);
        report(helper, "Siedlung " + rotation.name().toLowerCase(), problems);
    }

    /** Jedes Gebäude der Umbrer in allen vier Drehungen. */
    private static void umbrer(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        for (UmbrerVillagePiece.Kind kind : UmbrerVillagePiece.Kind.values()) {
            for (Rotation rotation : Rotation.values()) {
                TownGameTests.clearArea(helper);
                BlockPos origin = centered(helper, new BlockPos(24, 1, 24), kind.width(), kind.depth(), rotation);
                UmbrerVillagePiece piece = UmbrerVillagePiece.single(kind, origin, rotation, 1234L + 77L * rotation.ordinal());
                BoundingBox box = piece.getBoundingBox();
                BoundingBox all = new BoundingBox(box.minX(), level.getMinY(), box.minZ(), box.maxX(), level.getMaxY(), box.maxZ());
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(7), all,
                        ChunkPos.containing(origin), origin);
                String name = "umbrer/" + kind.name().toLowerCase() + " " + rotation.name().toLowerCase();
                BoundingBox scan = new BoundingBox(box.minX(), origin.getY() - 2, box.minZ(), box.maxX(), origin.getY() + kind.height() + 2, box.maxZ());
                for (String p : BuildAudit.check(level, scan, local(origin, rotation))) {
                    problems.add(name + ": " + p);
                }
            }
        }
        TownGameTests.clearArea(helper);
        report(helper, "Umbrer", problems);
    }

    // =====================================================================================

    /** Ursprung (vorne links) so, dass das gedrehte Gebäude mitten auf der Testfläche steht. */
    private static BlockPos centered(GameTestHelper helper, BlockPos center, int width, int depth, Rotation rotation) {
        BlockPos half = new BlockPos((width - 1) / 2, 0, (depth - 1) / 2).rotate(rotation);
        return helper.absolutePos(center.subtract(half));
    }

    /** Weltposition → Gebäude-Koordinaten (x nach rechts, y ab Fußboden, z nach hinten). */
    private static Function<BlockPos, String> local(BlockPos origin, Rotation rotation) {
        Rotation back = switch (rotation) {
            case CLOCKWISE_90 -> Rotation.COUNTERCLOCKWISE_90;
            case COUNTERCLOCKWISE_90 -> Rotation.CLOCKWISE_90;
            default -> rotation;
        };
        return pos -> {
            BlockPos l = pos.subtract(origin).rotate(back);
            return "(" + l.getX() + ", " + l.getY() + ", " + l.getZ() + ")";
        };
    }

    private static void report(GameTestHelper helper, String what, List<String> problems) {
        for (String p : problems) {
            KingdomsAtWar.LOGGER.warn("[Bauprüfung] {}", p);
        }
        helper.assertTrue(problems.isEmpty(), what + ": " + problems.size() + " Baufehler, z. B. "
                + String.join(" ¦ ", problems.subList(0, Math.min(6, problems.size()))));
        helper.succeed();
    }
}
