package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Garrison;
import io.github.sirschrolli.kingdomsatwar.army.Provisions;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Coronation;
import io.github.sirschrolli.kingdomsatwar.economy.town.Material;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownFood;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownStorage;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownTier;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownView;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.SeatEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.network.OpenTownPayload;
import io.github.sirschrolli.kingdomsatwar.network.OpenTradePayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.world.SealState;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der eigenen Siedlung: Banner setzen (gründen, versetzen, zu nah), Bürger anheuern (Lohn, Betten, Werkstätten),
 * Baupläne (alle Gebäude in allen Drehungen), der Baumeister baut eine Baustelle Block für Block, Handel mit Grenze und
 * Echtzeit-Pause, Speichern der Bürger und die Krönung.
 *
 * <p>Jeder Test mit einer Siedlung läuft in einer eigenen Testumgebung (also in einem eigenen Durchgang) und räumt vorher
 * alle Siedlungen weg – so kommen sich die Siedlungen der Tests nicht in die Quere (Mindestabstand).
 */
final class TownGameTests {
    static final Identifier TOWN = KingdomsAtWar.id("test_town");
    /** Mitte der Testfläche (48 × 48, Steinboden). */
    private static final BlockPos CENTER = new BlockPos(24, 1, 24);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_FOUND =
            ModGameTests.TEST_FUNCTIONS.register("town_found", () -> TownGameTests::townFound);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_BLUEPRINTS =
            ModGameTests.TEST_FUNCTIONS.register("town_blueprints", () -> TownGameTests::townBlueprints);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_BUILDER =
            ModGameTests.TEST_FUNCTIONS.register("town_builder", () -> TownGameTests::townBuilder);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_BUILDER_TALL =
            ModGameTests.TEST_FUNCTIONS.register("town_builder_tall", () -> TownGameTests::townBuilderTall);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_STORAGE =
            ModGameTests.TEST_FUNCTIONS.register("town_storage", () -> TownGameTests::townStorage);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_EVENING =
            ModGameTests.TEST_FUNCTIONS.register("town_evening", () -> TownGameTests::townEvening);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_TRADE =
            ModGameTests.TEST_FUNCTIONS.register("town_trade", () -> TownGameTests::townTrade);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_CROWN =
            ModGameTests.TEST_FUNCTIONS.register("town_crown", () -> TownGameTests::townCrown);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_FOOD =
            ModGameTests.TEST_FUNCTIONS.register("town_food", () -> TownGameTests::townFood);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_GARRISON =
            ModGameTests.TEST_FUNCTIONS.register("town_garrison", () -> TownGameTests::townGarrison);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_LUMBERJACK =
            ModGameTests.TEST_FUNCTIONS.register("town_lumberjack", () -> TownGameTests::townLumberjack);

    private TownGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(TOWN_BLUEPRINTS.getId(), new FunctionGameTestInstance(TOWN_BLUEPRINTS.key(),
                new TestData<>(environment, Level.OVERWORLD, TOWN, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        // jede Siedlung in einem eigenen Durchgang
        own(event, TOWN_FOUND, "town_found", 200);
        own(event, TOWN_BUILDER, "town_builder", 4000);
        own(event, TOWN_BUILDER_TALL, "town_builder_tall", 12000);
        own(event, TOWN_STORAGE, "town_storage", 200);
        own(event, TOWN_EVENING, "town_evening", 1200);
        own(event, TOWN_TRADE, "town_trade", 4000);
        own(event, TOWN_CROWN, "town_crown", 600);
        own(event, TOWN_FOOD, "town_food", 200);
        own(event, TOWN_GARRISON, "town_garrison", 300);
        own(event, TOWN_LUMBERJACK, "town_lumberjack", 4000);
    }

    private static void own(RegisterGameTestsEvent event, DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test,
            String name, int maxTicks) {
        Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(KingdomsAtWar.id(name));
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(env, Level.OVERWORLD, TOWN, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Ein Spieler im Überlebensmodus (bezahlt also mit Münzen), an einer Stelle der Testfläche. */
    @SuppressWarnings("removal")
    static ServerPlayer player(GameTestHelper helper, BlockPos relative) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(relative));
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    static void leave(GameTestHelper helper, ServerPlayer... players) {
        for (ServerPlayer p : players) {
            helper.getLevel().getServer().getPlayerList().remove(p);
        }
    }

    static void clearTowns(MinecraftServer server) {
        for (Town town : Towns.of(server).all()) {
            TownManager.disband(server, town);
        }
    }

    /** Setzt das Banner (beide Hälften) als Block. */
    private static void bannerBlocks(ServerLevel level, BlockPos pos) {
        BlockState lower = ModBlocks.TOWN_BANNER.get().defaultBlockState();
        level.setBlock(pos, lower, Block.UPDATE_ALL);
        level.setBlock(pos.above(), lower.setValue(TownBannerBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_ALL);
    }

    static Town found(GameTestHelper helper, ServerPlayer owner, BlockPos relative) {
        ServerLevel level = helper.getLevel();
        BlockPos pos = helper.absolutePos(relative);
        bannerBlocks(level, pos);
        TownManager.FoundResult result = TownManager.placeBanner(owner, level, pos);
        helper.assertValueEqual(result, TownManager.FoundResult.FOUNDED, "Banner setzen");
        Town town = Towns.of(level.getServer()).byOwner(owner.getUUID());
        helper.assertTrue(town != null, "Siedlung nicht angelegt");
        return town;
    }

    /** Ursprung eines Plans, sodass sein Grundriss mit der Mitte bei (x, z) liegt. */
    private static BlockPos originFor(GameTestHelper helper, BuildingType type, int x, int z) {
        return originFor(helper, type.blueprint(), x, z);
    }

    private static BlockPos originFor(GameTestHelper helper, Blueprint plan, int x, int z) {
        BoundingBox b = plan.bounds();
        return helper.absolutePos(new BlockPos(x - (b.minX() + b.maxX()) / 2, ground(helper), z - (b.minZ() + b.maxZ()) / 2));
    }

    /**
     * Räumt die Testfläche: über dem Boden alles weg, darunter (Fundamente, Schächte, Brunnen) wieder der Boden aus der
     * Testfläche – damit jeder Entwurf auf frischem Boden steht.
     */
    static void clearArea(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        int ground = ground(helper);
        BlockState floor = helper.getBlockState(new BlockPos(1, ground - 1, 1));
        BlockState air = Blocks.AIR.defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 48; x++) {
            for (int z = 0; z < 48; z++) {
                for (int y = ground - 14; y <= ground + 30; y++) {
                    BlockPos abs = helper.absolutePos(new BlockPos(x, y, z));
                    pos.set(abs);
                    BlockState want = y < ground ? floor : air;
                    if (!level.getBlockState(pos).equals(want)) {
                        level.setBlock(pos, want, Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE | Block.UPDATE_SUPPRESS_DROPS);
                    }
                }
            }
        }
    }

    /**
     * Die erste Lage über dem Boden der Testfläche – dorthin setzt auch der Bauplan im Spiel den Ursprung (Rechtsklick auf
     * den Boden). Gemessen in einer Ecke, wo nie gebaut wird.
     */
    static int ground(GameTestHelper helper) {
        int y = 2;
        while (y > -4 && helper.getBlockState(new BlockPos(1, y - 1, 1)).isAir()) {
            y--;
        }
        return y;
    }

    private static CitizenEntity citizen(GameTestHelper helper, Town.Resident r) {
        CitizenEntity c = TownManager.citizen(helper.getLevel(), r.entity());
        helper.assertTrue(c != null, "Bürger " + r.name() + " nicht in der Welt");
        return c;
    }

    // =====================================================================================
    // Gründen, Anheuern, Betten und Werkstätten
    // =====================================================================================

    private static void townFound(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 20));
        ServerPlayer other = player(helper, new BlockPos(30, 1, 20));
        Town town = found(helper, owner, CENTER);
        BlockPos banner = helper.absolutePos(CENTER);
        helper.assertTrue(town.hasBanner() && town.banner().equals(banner), "Banner der Siedlung falsch");
        helper.assertTrue(!town.name().isBlank(), "Siedlung ohne Namen");
        helper.assertValueEqual(town.tier(), TownTier.CAMP, "Stufe am Anfang");
        helper.assertValueEqual(town.capacity(), Town.CAMP_BEDS, "Betten im Lager am Banner");

        // ein zweites Banner geht nicht, eine fremde Siedlung so nah auch nicht
        helper.assertValueEqual(TownManager.placeBanner(owner, level, banner.offset(6, 0, 0)), TownManager.FoundResult.HAS_BANNER,
                "Zweites Banner");
        helper.assertValueEqual(TownManager.placeBanner(other, level, banner.offset(12, 0, 0)), TownManager.FoundResult.TOO_CLOSE,
                "Fremde Siedlung zu nah");
        helper.assertTrue(Towns.of(server).byOwner(other.getUUID()) == null, "Fremde Siedlung trotzdem gegründet");

        // Banner abbauen (die obere Hälfte): die untere fällt mit, das Banner fällt als Gegenstand heraus
        level.destroyBlock(banner.above(), true);
        helper.assertTrue(level.getBlockState(banner).isAir(), "Untere Hälfte des Banners steht noch");
        helper.assertTrue(!town.hasBanner(), "Siedlung hat nach dem Abbau noch ein Banner");
        int drops = 0;
        for (var item : helper.getEntities(EntityTypes.ITEM)) {
            if (item.getItem().is(ModBlocks.TOWN_BANNER.get().asItem())) {
                drops += item.getItem().getCount();
                item.discard();
            }
        }
        helper.assertValueEqual(drops, 1, "Abgebautes Banner als Gegenstand");
        // wieder aufstellen: die Siedlung bleibt dieselbe
        bannerBlocks(level, banner);
        helper.assertValueEqual(TownManager.placeBanner(owner, level, banner), TownManager.FoundResult.MOVED, "Banner wieder aufstellen");
        helper.assertTrue(Towns.of(server).byOwner(owner.getUUID()) == town && town.hasBanner(), "Siedlung nach dem Aufstellen");

        // Anheuern: ohne Geld nicht, mit Geld ein Baumeister (Lohn 40 Bronze)
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.NOT_ENOUGH, "Ohne Geld anheuern");
        Coins.give(owner, 1000);
        helper.assertValueEqual(TownManager.hire(other, town, Profession.BUILDER), TownManager.HireResult.NOT_OWNER, "Fremder heuert an");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.HIRED, "Baumeister anheuern");
        helper.assertValueEqual(Coins.total(owner), 1000 - Profession.BUILDER.price(), "Lohn des Baumeisters");
        helper.assertValueEqual(Reputation.points(owner), Reputation.HIRE, "Ansehen fürs Anheuern");
        Town.Resident builder = town.residents().getFirst();
        CitizenEntity builderEntity = citizen(helper, builder);
        helper.assertValueEqual(builderEntity.getProfession(), Profession.BUILDER, "Beruf des Bürgers");
        helper.assertTrue(builderEntity.hasCustomName() && builderEntity.getCustomName().getString().equals(builder.name()), "Name des Bürgers");
        helper.assertTrue(builderEntity.distanceToSqr(Vec3.atCenterOf(banner)) < 6 * 6, "Bürger erscheint nicht am Banner");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.NO_WORKPLACE,
                "Im Lager gibt es nur einen Baumeister");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.FARMER), TownManager.HireResult.NO_WORKPLACE, "Bauer ohne Feld");

        // Feld und Haus: Arbeitsplatz für den Bauern, zwei Betten mehr
        BlockPos field = originFor(helper, BuildingType.FIELD, 10, 10);
        BlockPos house = originFor(helper, BuildingType.HOUSE, 38, 10);
        TownManager.buildInstantly(level, town, BuildingType.FIELD, field, Rotation.NONE);
        TownManager.buildInstantly(level, town, BuildingType.HOUSE, house, Rotation.NONE);
        helper.assertValueEqual(town.capacity(), Town.CAMP_BEDS + 2, "Betten mit Haus");
        helper.assertTrue(town.residents().getFirst().bed().isPresent(), "Baumeister bekommt kein Bett im Haus");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.FARMER), TownManager.HireResult.HIRED, "Bauer anheuern");
        Town.Resident farmer = town.residents().get(1);
        helper.assertTrue(farmer.work().isPresent() && farmer.work().get().equals(field), "Bauer arbeitet nicht auf dem Feld");
        helper.assertTrue(farmer.bed().isPresent(), "Bauer bekommt kein Bett");
        helper.assertTrue(field.equals(citizen(helper, farmer).getWork()), "Bürger kennt sein Feld nicht");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.FARMER), TownManager.HireResult.NO_WORKPLACE, "Zweiter Bauer");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.LUMBERJACK), TownManager.HireResult.NO_WORKPLACE, "Holzfäller ohne Hütte");
        // das Fenster: Abbild der Siedlung übers Netz
        TownView view = TownView.of(owner, town);
        helper.assertValueEqual(view.residents().size(), 2, "Bewohner im Fenster");
        helper.assertTrue(view.isOwner() && view.hires().get(Profession.FARMER.ordinal()).blocked() == 1, "Fenster: Bauer nicht gesperrt");
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), level.registryAccess());
        OpenTownPayload payload = new OpenTownPayload(banner, view, Optional.of(Component.literal("x")), true);
        OpenTownPayload.STREAM_CODEC.encode(buf, payload);
        OpenTownPayload copy = OpenTownPayload.STREAM_CODEC.decode(buf);
        helper.assertTrue(copy.view().equals(view) && copy.banner().equals(banner), "Stadtfenster übers Netz verändert");

        // entlassen: der Bauer geht, sein Bett wird frei
        TownManager.dismiss(owner, town, farmer.entity());
        helper.assertTrue(town.resident(farmer.entity()) == null, "Entlassener Bürger wohnt noch hier");
        helper.assertTrue(TownManager.citizen(level, farmer.entity()) == null, "Entlassener Bürger steht noch da");
        helper.assertValueEqual(town.residents().size(), 1, "Bewohner nach dem Entlassen");
        // stirbt ein Bürger, wird er ausgetragen
        builderEntity.kill(level);
        helper.assertTrue(town.residents().isEmpty(), "Gestorbener Bürger wohnt noch hier");
        clearTowns(server);
        leave(helper, owner, other);
        helper.succeed();
    }

    // =====================================================================================
    // Baupläne
    // =====================================================================================

    /**
     * Jeder Entwurf jedes Gebäudes lässt sich vollständig setzen; Betten, Truhen, Eingang und Bauablauf stimmen, alles ist
     * begehbar und verbunden; Wohn- und Bürgerhäuser auch in allen vier Drehungen.
     */
    private static void townBlueprints(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // alle Entwürfe prüfen und die Fehler sammeln, statt beim ersten aufzuhören
        List<String> problems = new java.util.ArrayList<>();
        for (BuildingType type : BuildingType.standard()) {
          for (int design = 0; design < type.designs(); design++) {
            Blueprint plan = type.blueprint(design);
            String what = type.id() + (type.designs() > 1 ? " (Entwurf " + design + ")" : "");
            BoundingBox b = plan.bounds();
            clearArea(helper);
            try {
            helper.assertTrue(!plan.blocks().isEmpty(), what + ": leerer Bauplan");
            helper.assertTrue(b.getXSpan() <= 46 && b.getZSpan() <= 46 && b.maxY() < 28, what + ": zu groß für die Testfläche " + b);
            helper.assertValueEqual(plan.beds().size(), type.beds(), what + ": Betten");
            if (type == BuildingType.FIELD || type == BuildingType.LUMBER_HUT || type == BuildingType.MINE || type == BuildingType.STOREHOUSE
                    || type == BuildingType.MARKET || type == BuildingType.SMITHY) {
                helper.assertTrue(plan.chest() != null, what + ": Truhe für die Arbeit fehlt");
            }
            if (type.jobs() > 0) {
                helper.assertTrue(plan.workSpot() != null, what + ": kein Arbeitsplatz");
            }
            helper.assertTrue(b.inflatedBy(1).isInside(plan.entrance()), what + ": Eingang außerhalb");
            BlockPos origin = originFor(helper, plan, 24, 24);
            helper.assertTrue(!plan.steps(origin, Rotation.NONE).isEmpty(), what + ": kein Bauablauf");
            plan.placeAll(level, origin, Rotation.NONE);
            helper.assertTrue(plan.complete(level, origin, Rotation.NONE), what + ": nach dem Bauen unvollständig");
            assertConnected(helper, plan, origin, Rotation.NONE, what);
            assertWalkable(helper, plan, origin, Rotation.NONE, what);
            assertStanding(helper, plan, origin, Rotation.NONE, what);
            for (BlockPos bed : plan.beds()) {
                helper.assertTrue(level.getBlockState(Blueprint.world(origin, Rotation.NONE, bed)).getBlock()
                        instanceof net.minecraft.world.level.block.BedBlock, what + ": Bett fehlt bei " + bed);
            }
            if (plan.chest() != null) {
                helper.assertTrue(level.getBlockEntity(Blueprint.world(origin, Rotation.NONE, plan.chest()))
                        instanceof net.minecraft.world.Container, what + ": Truhe ist kein Behälter");
            }
            } catch (RuntimeException e) {
                problems.add(e.getMessage());
                KingdomsAtWar.LOGGER.error("[Baupläne] {}", e.getMessage());
            }
          }
        }
        // Drehungen: alle Wohn- und Bürgerhäuser in allen vier Richtungen
        for (BuildingType type : new BuildingType[] {BuildingType.HOUSE, BuildingType.TOWNHOUSE}) {
            for (int design = 0; design < type.designs(); design++) {
                Blueprint plan = type.blueprint(design);
                for (Rotation rotation : Rotation.values()) {
                    clearArea(helper);
                    BlockPos origin = originFor(helper, plan, 24, 24);
                    String what = type.id() + " (Entwurf " + design + ") gedreht " + rotation;
                    try {
                        plan.placeAll(level, origin, rotation);
                        helper.assertTrue(plan.complete(level, origin, rotation), what + " unvollständig");
                        assertConnected(helper, plan, origin, rotation, what);
                        assertWalkable(helper, plan, origin, rotation, what);
                    } catch (RuntimeException e) {
                        problems.add(e.getMessage());
                        KingdomsAtWar.LOGGER.error("[Baupläne] {}", e.getMessage());
                    }
                }
            }
        }
        clearArea(helper);
        helper.assertTrue(problems.isEmpty(), problems.size() + " Fehler in den Bauplänen: " + String.join(" ¦ ", problems));
        helper.succeed();
    }

    /**
     * Was im Plan steht, bleibt auch stehen: Pflanzen, Laternen, Leitern, Ketten, Kronleuchter, Banner und Glocken haben
     * nach dem Bauen ihren Halt (sonst wären sie gleich wieder abgefallen).
     */
    static void assertStanding(GameTestHelper helper, Blueprint plan, BlockPos origin, Rotation rotation, String what) {
        ServerLevel level = helper.getLevel();
        for (Blueprint.Entry e : plan.blocks()) {
            BlockPos pos = Blueprint.world(origin, rotation, e.pos());
            BlockState state = level.getBlockState(pos);
            // Feldfrüchte: nur der Ackerboden zählt (ihr Lichtbedarf hängt davon ab, wie weit die Beleuchtung nachgerechnet
            // ist – unter Last hinkt sie im Testserver hinterher)
            boolean standing = state.getBlock() instanceof net.minecraft.world.level.block.CropBlock
                    ? level.getBlockState(pos.below()).getBlock() instanceof net.minecraft.world.level.block.FarmlandBlock
                    : state.canSurvive(level, pos);
            if (!state.isAir() && !standing) {
                helper.fail(what + ": " + state.getBlock().getName().getString() + " bei " + helper.relativePos(pos) + " hat keinen Halt");
            }
        }
    }

    /**
     * Begehbar wie für einen Spieler, der nicht springt: vom Eingang aus erreicht man jeden Raum, jedes Bett, die Truhe und den
     * Arbeitsplatz (Treppen enden auf Höhe des Bodens darüber, mit Kopffreiheit).
     */
    static void assertWalkable(GameTestHelper helper, Blueprint plan, BlockPos origin, Rotation rotation, String what) {
        ServerLevel level = helper.getLevel();
        WalkCheck walk = new WalkCheck(level, plan.worldBounds(origin, rotation));
        BlockPos door = Blueprint.world(origin, rotation, plan.entrance());
        walk.walkFrom(door.getX(), door.getZ(), door.getY());
        if (Boolean.getBoolean("kingdomsatwar.debugWalk")) {
            StringBuilder line = new StringBuilder("[Walk] " + what + " Eingang:");
            for (int d = 0; d <= 6; d++) {
                BlockPos p = door.relative(rotation.rotate(Direction.SOUTH), d);
                line.append(" | ").append(d).append(": ").append(walk.column(p.getX(), p.getZ()));
            }
            KingdomsAtWar.LOGGER.info(line.toString());
        }
        for (BlockPos bed : plan.beds()) {
            // ein Bett erreicht man am Kopf- oder am Fußende
            BlockPos head = Blueprint.world(origin, rotation, bed);
            BlockState state = level.getBlockState(head);
            BlockPos foot = state.getBlock() instanceof net.minecraft.world.level.block.BedBlock
                    ? head.relative(state.getValue(net.minecraft.world.level.block.BedBlock.FACING).getOpposite()) : head;
            if (!(walk.reachesBlock(head) || walk.reachesBlock(foot)) && Boolean.getBoolean("kingdomsatwar.debugWalk")) {
                BoundingBox box = plan.worldBounds(origin, rotation);
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    StringBuilder line = new StringBuilder("[Walk] z=" + (z - origin.getZ()) + ":");
                    for (int x = box.minX(); x <= box.maxX(); x++) {
                        line.append(" | ").append(x - origin.getX()).append(":").append(walk.column(x, z).replaceAll("^[^:]*:", ""));
                    }
                    KingdomsAtWar.LOGGER.info(line.toString());
                }
            }
            helper.assertTrue(walk.reachesBlock(head) || walk.reachesBlock(foot), what + ": Bett bei " + helper.relativePos(head)
                    + " nicht zu Fuß erreichbar (" + walk.explain() + ")");
        }
        if (plan.chest() != null) {
            BlockPos pos = Blueprint.world(origin, rotation, plan.chest());
            helper.assertTrue(walk.reachesBlock(pos), what + ": Truhe bei " + helper.relativePos(pos) + " nicht zu Fuß erreichbar");
        }
        if (plan.workSpot() != null && plan.workSpot().getY() >= 0) {
            BlockPos pos = Blueprint.world(origin, rotation, plan.workSpot());
            helper.assertTrue(walk.reachesSpot(pos) || walk.reachesBlock(pos), what + ": Arbeitsplatz bei " + helper.relativePos(pos)
                    + " nicht zu Fuß erreichbar");
        }
        BlockPos room = walk.unreachedRoom(4, origin.getY());
        helper.assertTrue(room == null, what + ": Raum bei " + (room == null ? "" : helper.relativePos(room))
                + " nicht zu Fuß erreichbar (Treppe zu kurz, keine Kopffreiheit oder kein Zugang; " + walk.explain() + ")");
    }

    /**
     * Scheiben, Gitter, Zäune, Mauern und Treppen sind mit ihren Nachbarn verbunden, wie sie es beim Setzen von Hand wären
     * (keine einzelnen Scheiben-Pfosten im Fenster, Dachecken als Ecken), und keine Fensterscheibe steht frei.
     */
    static void assertConnected(GameTestHelper helper, Blueprint plan, BlockPos origin, Rotation rotation, String what) {
        ServerLevel level = helper.getLevel();
        for (Blueprint.Entry e : plan.blocks()) {
            BlockPos pos = Blueprint.world(origin, rotation, e.pos());
            BlockState state = level.getBlockState(pos);
            Block block = state.getBlock();
            if (block instanceof net.minecraft.world.level.block.CrossCollisionBlock || block instanceof net.minecraft.world.level.block.StairBlock
                    || block instanceof net.minecraft.world.level.block.WallBlock) {
                BlockState settled = Block.updateFromNeighbourShapes(state, level, pos);
                helper.assertTrue(settled.equals(state), what + ": " + block.getName().getString() + " bei " + helper.relativePos(pos)
                        + " nicht mit den Nachbarn verbunden (" + state + " statt " + settled + ")");
            }
            if (block instanceof net.minecraft.world.level.block.IronBarsBlock) {
                boolean any = false;
                for (var property : java.util.List.of(net.minecraft.world.level.block.CrossCollisionBlock.NORTH,
                        net.minecraft.world.level.block.CrossCollisionBlock.EAST, net.minecraft.world.level.block.CrossCollisionBlock.SOUTH,
                        net.minecraft.world.level.block.CrossCollisionBlock.WEST)) {
                    any |= state.getValue(property);
                }
                helper.assertTrue(any, what + ": Fensterscheibe ohne Halt bei " + helper.relativePos(pos));
            }
        }
    }

    // =====================================================================================
    // Der Baumeister baut
    // =====================================================================================

    /** Eine Baustelle (Feld) wird bezahlt und eingerichtet; der Baumeister läuft hin und baut sie Block für Block fertig. */
    private static void townBuilder(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        helper.setTime(1000);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 22));
        Town town = found(helper, owner, new BlockPos(24, 1, 34));
        Coins.give(owner, 500);
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.HIRED, "Baumeister anheuern");
        BlockPos origin = originFor(helper, BuildingType.FIELD, 24, 12);
        // falsche Plätze: zu weit weg, auf einer anderen Baustelle, Gebäude zu groß für die Stufe
        helper.assertValueEqual(TownManager.placeProject(owner, BuildingType.FIELD, origin.offset(80, 0, 0), Rotation.NONE),
                TownManager.PlaceResult.OUTSIDE, "Baustelle außerhalb");
        helper.assertValueEqual(TownManager.placeProject(owner, BuildingType.MARKET, origin, Rotation.NONE),
                TownManager.PlaceResult.TIER, "Markt im Lager");
        int before = Coins.total(owner);
        helper.assertValueEqual(TownManager.placeProject(owner, BuildingType.FIELD, origin, Rotation.NONE),
                TownManager.PlaceResult.PLACED, "Baustelle einrichten");
        helper.assertValueEqual(Coins.total(owner), before - BuildingType.FIELD.price(), "Preis des Felds");
        helper.assertValueEqual(TownManager.placeProject(owner, BuildingType.HOUSE, origin, Rotation.NONE),
                TownManager.PlaceResult.OVERLAP, "Zweite Baustelle am selben Platz");
        helper.assertValueEqual(town.projects().size(), 1, "Baustellen");
        Town.Project project = town.projects().getFirst();
        int total = project.steps().size();
        int[] maxProgress = {0};
        // erst gibt es kein Lager: der Baumeister wartet auf Holz (für den Zaun); dann kommt eine Truhe mit Baustoffen ans Banner
        boolean[] waited = {false};
        int[] stuckAt = {-1};
        helper.onEachTick(() -> {
            if (!town.projects().isEmpty()) {
                Town.Project open = town.projects().getFirst();
                maxProgress[0] = Math.max(maxProgress[0], open.progress);
                if (!waited[0] && open.waiting != null) {
                    if (stuckAt[0] < 0) {
                        stuckAt[0] = open.progress;
                    } else if (helper.getTick() % 100 == 0) {
                        helper.assertValueEqual(open.progress, stuckAt[0], "Baustelle ohne Baustoffe weitergebaut");
                        if (helper.getTick() > 600) {
                            waited[0] = true;
                            TownStorage.supply(level, helper.absolutePos(new BlockPos(27, 1, 34)), BuildingType.FIELD.costs());
                        }
                    }
                }
            }
        });
        helper.succeedWhen(() -> {
            if (!town.projects().isEmpty()) {
                Town.Project open = town.projects().getFirst();
                String detail = "";
                if (open.progress < open.steps().size()) {
                    var step = open.steps().get(open.progress);
                    detail = " – Schritt " + step.state() + " bei " + helper.relativePos(step.pos()) + " (dort: "
                            + level.getBlockState(step.pos()) + ")";
                }
                CitizenEntity builder = TownManager.citizen(level, town.residents().getFirst().entity());
                detail += builder == null ? " – kein Baumeister" : " – Baumeister bei " + helper.relativePos(builder.blockPosition())
                        + (builder.isWorking() ? " (arbeitet)" : "");
                helper.assertTrue(false, "Baustelle noch offen: " + open.progress + " von " + total + " Schritten" + detail);
            }
            helper.assertTrue(waited[0], "Die Baustelle hat nie auf Baustoffe gewartet");
            helper.assertValueEqual(town.count(BuildingType.FIELD), 1, "Fertige Felder");
            helper.assertTrue(BuildingType.FIELD.blueprint().complete(level, origin, Rotation.NONE), "Feld unvollständig gebaut");
            helper.assertTrue(Reputation.points(owner) >= Reputation.HIRE + Reputation.BUILDING, "Ansehen fürs fertige Gebäude");
            KingdomsAtWar.LOGGER.info("[Test] Der Baumeister hat das Feld nach {} Ticks fertig gebaut ({} Bauschritte)", helper.getTick(), total);
            clearTowns(server);
            leave(helper, owner);
        });
    }

    /**
     * Ein zweistöckiges Bürgerhaus: Der Baumeister baut es ganz fertig, und zwar aus der Nähe (fast jeder Block in
     * Reichweite vom Stehplatz, auch oben am Dach – dafür stellt er sich Gerüste auf). Danach steht kein Gerüst mehr.
     */
    private static void townBuilderTall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        helper.setTime(1000);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 40));
        Town town = found(helper, owner, new BlockPos(24, 1, 42));
        Coins.give(owner, 500);
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.HIRED, "Baumeister anheuern");
        BlockPos origin = originFor(helper, BuildingType.TOWNHOUSE, 24, 18);
        int[] costs = BuildingType.TOWNHOUSE.costs();
        TownStorage.supply(level, helper.absolutePos(new BlockPos(20, 1, 42)), costs);
        int[] before = TownStorage.counts(level, town);
        for (Material m : Material.values()) {
            helper.assertTrue(before[m.ordinal()] >= costs[m.ordinal()], "Lager zu klein für " + m.id());
        }
        Town.Project project = TownManager.startProject(level, town, BuildingType.TOWNHOUSE, origin, Rotation.NONE);
        List<Blueprint.Step> steps = project.steps();
        int[] last = {0};
        int[] near = {0};
        int[] placed = {0};
        double[] worst = {0};
        String[] worstAt = {""};
        boolean[] scaffold = {false};
        helper.onEachTick(() -> {
            if (town.projects().isEmpty()) {
                return;
            }
            Town.Project open = town.projects().getFirst();
            CitizenEntity builder = TownManager.citizen(level, town.residents().getFirst().entity());
            if (builder != null && open.progress == last[0] + 1 && open.progress > 0) {
                Blueprint.Step step = steps.get(open.progress - 1);
                if (!step.state().isAir() && !step.foundation()) {
                    double dx = builder.getX() - (step.pos().getX() + 0.5);
                    double dz = builder.getZ() - (step.pos().getZ() + 0.5);
                    double h = Math.sqrt(dx * dx + dz * dz);
                    double dy = step.pos().getY() + 0.5 - builder.getY();
                    placed[0]++;
                    if (h <= 5.0 && dy <= 5.5 && dy >= -4.0) {
                        near[0]++;
                    }
                    double far = Math.max(h, Math.abs(dy));
                    if (far > worst[0]) {
                        worst[0] = far;
                        worstAt[0] = step.state().getBlock() + " bei " + helper.relativePos(step.pos()) + ", Baumeister bei "
                                + helper.relativePos(builder.blockPosition());
                    }
                }
            }
            last[0] = open.progress;
            if (!scaffold[0] && builder != null && level.getBlockState(builder.blockPosition().below()).is(ModBlocks.BUILDER_SCAFFOLD.get())) {
                scaffold[0] = true;
            }
        });
        helper.succeedWhen(() -> {
            if (!town.projects().isEmpty()) {
                Town.Project open = town.projects().getFirst();
                CitizenEntity builder = TownManager.citizen(level, town.residents().getFirst().entity());
                String detail = open.progress < steps.size() ? " – Schritt " + steps.get(open.progress).state() + " bei "
                        + helper.relativePos(steps.get(open.progress).pos()) : " – Gerüste abbauen";
                detail += builder == null ? " – kein Baumeister" : " – Baumeister bei " + helper.relativePos(builder.blockPosition());
                helper.assertTrue(false, "Bürgerhaus noch offen: " + open.progress + " von " + steps.size() + detail);
            }
            helper.assertValueEqual(town.count(BuildingType.TOWNHOUSE), 1, "Fertige Bürgerhäuser");
            helper.assertTrue(BuildingType.TOWNHOUSE.blueprint().complete(level, origin, Rotation.NONE), "Bürgerhaus unvollständig gebaut");
            assertConnected(helper, BuildingType.TOWNHOUSE.blueprint(), origin, Rotation.NONE, "Bürgerhaus vom Baumeister");
            var box = project.bounds().inflatedBy(6, 0, 6);
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY() - 1, box.minZ(), box.maxX(), box.maxY() + 4, box.maxZ())) {
                helper.assertTrue(!level.getBlockState(p).is(ModBlocks.BUILDER_SCAFFOLD.get()), "Gerüst stehen geblieben bei " + helper.relativePos(p));
            }
            helper.assertTrue(scaffold[0], "Baumeister stand nie auf einem Gerüst");
            // die Baustoffe kamen aus dem Lager (höchstens ein paar Stämme mehr, als gebraucht wurden)
            int[] after = TownStorage.counts(level, town);
            for (Material m : Material.values()) {
                int used = before[m.ordinal()] - after[m.ordinal()];
                helper.assertTrue(used >= costs[m.ordinal()] * 0.6 && used <= costs[m.ordinal()] + 72, "Verbrauch an " + m.id() + ": "
                        + used + " statt etwa " + costs[m.ordinal()]);
            }
            helper.assertTrue(placed[0] > 100 && near[0] >= placed[0] * 0.95, "Baumeister zu weit weg: nur " + near[0] + " von "
                    + placed[0] + " Blöcken aus der Nähe gesetzt (am weitesten " + String.format("%.1f", worst[0]) + ": " + worstAt[0] + ")");
            KingdomsAtWar.LOGGER.info("[Test] Bürgerhaus nach {} Ticks fertig ({} Bauschritte, {} von {} Blöcken aus der Nähe, am weitesten {}: {})",
                    helper.getTick(), steps.size(), near[0], placed[0], String.format("%.1f", worst[0]), worstAt[0]);
            clearTowns(server);
            leave(helper, owner);
        });
    }

    // =====================================================================================
    // Lager und Baustoffe
    // =====================================================================================

    /**
     * Das Lager: Truhen bis vier Blöcke um das Banner und in Lagerhäusern zählen, weiter weg nicht; Einheiten je Baustoff
     * (Stamm vier, Brett eins, Stahl zwei), Nehmen (erst Bretter, dann Stämme) und Einlagern; jeder Bau außer dem Lagerhaus
     * kostet Holz und Stein.
     */
    private static void townStorage(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 30));
        Town town = found(helper, owner, CENTER);
        helper.assertTrue(!TownStorage.exists(level, town), "Lager ohne Truhe");
        // eine Truhe zu weit weg zählt nicht, eine nah am Banner schon
        BlockPos far = helper.absolutePos(CENTER.offset(6, 0, 0));
        BlockPos near = helper.absolutePos(CENTER.offset(3, 0, 1));
        level.setBlock(far, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(!TownStorage.exists(level, town), "Truhe sechs Blöcke weg zählt zum Lager");
        level.setBlock(near, Blocks.BARREL.defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(TownStorage.exists(level, town), "Fass am Banner zählt nicht zum Lager");
        net.minecraft.world.Container barrel = (net.minecraft.world.Container) level.getBlockEntity(near);
        barrel.setItem(0, new ItemStack(Items.OAK_LOG, 3));
        barrel.setItem(1, new ItemStack(Items.SPRUCE_PLANKS, 5));
        barrel.setItem(2, new ItemStack(Items.COBBLESTONE, 10));
        barrel.setItem(3, new ItemStack(Items.SAND, 2));
        barrel.setItem(4, new ItemStack(Items.IRON_INGOT, 1));
        barrel.setItem(5, new ItemStack(ModItems.STEEL_INGOT.get(), 1));
        barrel.setItem(6, new ItemStack(Items.WHEAT, 20));
        ((net.minecraft.world.Container) level.getBlockEntity(far)).setItem(0, new ItemStack(Items.OAK_LOG, 64));
        int[] counts = TownStorage.counts(level, town);
        helper.assertValueEqual(counts[Material.WOOD.ordinal()], 17, "Holz im Lager");
        helper.assertValueEqual(counts[Material.STONE.ordinal()], 12, "Stein im Lager");
        helper.assertValueEqual(counts[Material.METAL.ordinal()], 3, "Metall im Lager");
        // sechs Einheiten Holz: fünf Bretter, dann ein Stamm (vier) – zusammen neun
        helper.assertValueEqual(TownStorage.take(level, town, Material.WOOD, 6), 9, "Genommenes Holz");
        helper.assertValueEqual(TownStorage.counts(level, town)[Material.WOOD.ordinal()], 8, "Holz danach");
        helper.assertValueEqual(TownStorage.take(level, town, Material.METAL, 10), 3, "Nur so viel Metall, wie da ist");
        // ein Lagerhaus: seine Truhen und Fässer gehören dazu, Eingelagertes kommt in den nächsten Behälter
        BlockPos storehouse = originFor(helper, BuildingType.STOREHOUSE, 24, 10);
        TownManager.buildInstantly(level, town, BuildingType.STOREHOUSE, storehouse, Rotation.NONE);
        int containers = TownStorage.containers(level, town).size();
        helper.assertTrue(containers >= 10, "Behälter im Lagerhaus: " + containers);
        ItemStack rest = TownStorage.store(level, town, new ItemStack(Items.COBBLESTONE, 64), storehouse);
        helper.assertTrue(rest.isEmpty(), "Einlagern ging nicht");
        helper.assertValueEqual(TownStorage.counts(level, town)[Material.STONE.ordinal()], 12 + 64, "Stein nach dem Einlagern");
        BlockPos nearest = TownStorage.nearest(level, town, storehouse);
        helper.assertTrue(nearest != null && BuildingType.STOREHOUSE.blueprint().worldBounds(storehouse, Rotation.NONE).isInside(nearest),
                "Nächster Behälter liegt nicht im Lagerhaus");
        // Baukosten
        for (BuildingType type : BuildingType.standard()) {
            int[] c = type.costs();
            if (type == BuildingType.STOREHOUSE) {
                helper.assertTrue(c[0] == 0 && c[1] == 0 && c[2] == 0, "Das Lagerhaus kostet Baustoffe");
            } else {
                helper.assertTrue(c[Material.WOOD.ordinal()] > 0 && c[Material.STONE.ordinal()] > 0 || type == BuildingType.FIELD,
                        type.id() + " kostet keine Baustoffe");
            }
            KingdomsAtWar.LOGGER.info("[Test] Baukosten {}: {} Holz, {} Stein, {} Metall", type.id(), c[0], c[1], c[2]);
        }
        helper.assertTrue(BuildingType.FIELD.costs()[Material.WOOD.ordinal()] > 0, "Der Zaun ums Feld kostet nichts");
        // das Fenster zeigt das Lager
        TownView view = TownView.of(owner, town);
        helper.assertTrue(view.storage() && view.stock(Material.STONE) == 76, "Fenster: Lager falsch");
        // die Baukosten im Fenster: das Meiste und das Wenigste, was einer der Entwürfe braucht
        TownView.BuildView house = view.builds().get(BuildingType.HOUSE.ordinal());
        helper.assertValueEqual(house.cost(Material.WOOD), BuildingType.HOUSE.maxCosts()[0], "Fenster: höchste Baukosten");
        helper.assertValueEqual(house.minCost(Material.WOOD), BuildingType.HOUSE.minCosts()[0], "Fenster: niedrigste Baukosten");
        helper.assertTrue(house.minCost(Material.STONE) < house.cost(Material.STONE), "Fenster: die Entwürfe kosten alle gleich viel Stein");
        // jeder Entwurf kostet etwas anderes (sie unterscheiden sich wirklich)
        for (BuildingType type : BuildingType.standard()) {
            for (int a = 0; a < type.designs(); a++) {
                for (int b2 = a + 1; b2 < type.designs(); b2++) {
                    helper.assertTrue(!java.util.Arrays.equals(type.blueprint(a).costs(), type.blueprint(b2).costs()),
                            type.id() + ": Entwurf " + a + " und " + b2 + " sind gleich");
                }
            }
        }
        clearTowns(server);
        leave(helper, owner);
        helper.succeed();
    }

    /**
     * Abends bringt der Holzfäller alles ins Lager: was in der Truhe seiner Hütte liegt und was er bei sich trägt. Ohne Lager
     * bleibt es in der Hütte.
     */
    private static void townEvening(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        helper.setTime(1000);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 30));
        Town town = found(helper, owner, CENTER);
        Coins.give(owner, 500);
        BlockPos hut = originFor(helper, BuildingType.LUMBER_HUT, 24, 12);
        TownManager.buildInstantly(level, town, BuildingType.LUMBER_HUT, hut, Rotation.NONE);
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.LUMBERJACK), TownManager.HireResult.HIRED, "Holzfäller anheuern");
        CitizenEntity lumberjack = citizen(helper, town.residents().getFirst());
        BlockPos hutChest = Blueprint.world(hut, Rotation.NONE, BuildingType.LUMBER_HUT.blueprint().chest());
        ((net.minecraft.world.Container) level.getBlockEntity(hutChest)).setItem(0, new ItemStack(Items.SPRUCE_LOG, 10));
        lumberjack.carried().addItem(new ItemStack(Items.SPRUCE_LOG, 5));
        BlockPos store = helper.absolutePos(CENTER.offset(2, 0, 2));
        level.setBlock(store, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        helper.setTime(10650);
        helper.succeedWhen(() -> {
            int stored = TownStorage.counts(level, town)[Material.WOOD.ordinal()] / 4;
            helper.assertValueEqual(stored, 15, "Stämme im Lager");
            helper.assertTrue(((net.minecraft.world.Container) level.getBlockEntity(hutChest)).isEmpty(), "Truhe der Hütte nicht geleert");
            helper.assertTrue(lumberjack.carried().isEmpty(), "Holzfäller trägt noch etwas");
            // wieder Tag: die Tests danach rechnen nicht mit dem Abend
            helper.setTime(1000);
            clearTowns(server);
            leave(helper, owner);
        });
    }

    /**
     * Der Holzfäller bei der Arbeit: Er findet einen ausgewachsenen Baum im Baumgarten hinter seiner Hütte und einen
     * draußen auf der Wiese, fällt beide ganz (Stamm und Äste), pflanzt an ihrem Fuß wieder einen Setzling und trägt das
     * Holz bei sich oder hat es schon in die Truhe gebracht.
     */
    private static void townLumberjack(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        helper.setTime(1000);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 40));
        Town town = found(helper, owner, new BlockPos(24, 1, 42));
        Coins.give(owner, 500);
        BlockPos hut = originFor(helper, BuildingType.LUMBER_HUT, 24, 14);
        TownManager.buildInstantly(level, town, BuildingType.LUMBER_HUT, hut, Rotation.NONE);
        var oak = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.FEATURE)
                .getOrThrow(net.minecraft.data.worldgen.features.TreeFeatures.OAK).value();
        // ein Baum im Baumgarten (an Stelle des mittleren Setzlings), einer draußen rechts neben der Hütte
        BlockPos grove = hut.offset(0, 0, 8);
        BlockPos meadow = hut.offset(13, 0, 2);
        List<BlockPos> trees = new java.util.ArrayList<>();
        for (BlockPos at : List.of(grove, meadow)) {
            level.setBlock(at, Blocks.AIR.defaultBlockState(), 2);
            level.setBlock(at.below(), Blocks.GRASS_BLOCK.defaultBlockState(), 2);
            boolean placed = false;
            for (int attempt = 0; attempt < 6 && !placed; attempt++) {
                placed = oak.place(level, level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(11L + attempt), at);
            }
            helper.assertTrue(placed && level.getBlockState(at).is(net.minecraft.tags.BlockTags.LOGS), "kein Baum bei " + helper.relativePos(at));
            trees.add(at);
        }
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.LUMBERJACK), TownManager.HireResult.HIRED, "Holzfäller anheuern");
        CitizenEntity lumberjack = citizen(helper, town.residents().getFirst());
        BlockPos hutChest = Blueprint.world(hut, Rotation.NONE, BuildingType.LUMBER_HUT.blueprint().chest());
        helper.succeedWhen(() -> {
            for (BlockPos at : trees) {
                // beim Baumgarten nicht bis an die Rückwand der Hütte zählen (deren Balken sind auch Holz)
                int back = at.equals(grove) ? -3 : -5;
                int left = 0;
                for (BlockPos p : BlockPos.betweenClosed(at.offset(-5, 0, back), at.offset(5, 12, 5))) {
                    if (level.getBlockState(p).is(net.minecraft.tags.BlockTags.LOGS)) {
                        left++;
                    }
                }
                helper.assertValueEqual(left, 0, "Holz übrig am Baum bei " + helper.relativePos(at));
                helper.assertTrue(level.getBlockState(at).is(net.minecraft.tags.BlockTags.SAPLINGS), "kein neuer Setzling bei " + helper.relativePos(at));
            }
            int logs = 0;
            for (int i = 0; i < lumberjack.carried().getContainerSize(); i++) {
                if (lumberjack.carried().getItem(i).is(net.minecraft.tags.ItemTags.LOGS)) {
                    logs += lumberjack.carried().getItem(i).getCount();
                }
            }
            var chest = (net.minecraft.world.Container) level.getBlockEntity(hutChest);
            for (int i = 0; i < chest.getContainerSize(); i++) {
                if (chest.getItem(i).is(net.minecraft.tags.ItemTags.LOGS)) {
                    logs += chest.getItem(i).getCount();
                }
            }
            helper.assertTrue(logs >= 8, "zu wenig Holz geschlagen: " + logs);
            clearTowns(server);
            leave(helper, owner);
        });
    }

    // =====================================================================================
    // Handel
    // =====================================================================================

    /** Kaufen und verkaufen beim Händler; nach der Handelsgrenze schließt der Laden in Echtzeit, danach öffnet er wieder. */
    private static void townTrade(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 30));
        Town town = found(helper, owner, CENTER);
        // Markt (2 Händler) braucht ein Dorf: für den Test sofort gebaut
        BlockPos market = originFor(helper, BuildingType.MARKET, 24, 10);
        TownManager.buildInstantly(level, town, BuildingType.MARKET, market, Rotation.NONE);
        Coins.give(owner, 1000);
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.MERCHANT), TownManager.HireResult.HIRED, "Händler anheuern");
        CitizenEntity merchant = citizen(helper, town.residents().getFirst());
        owner.snapTo(merchant.getX() + 1.5, merchant.getY(), merchant.getZ(), 0.0f, 0.0f);

        List<Trading.Offer> offers = Trading.offers(owner, merchant);
        int glass = -1;
        int wheat = -1;
        int torch = -1;
        for (int i = 0; i < offers.size(); i++) {
            Trading.Offer o = offers.get(i);
            if (o.get().is(Items.GLASS)) {
                glass = i;
            } else if (o.give().is(Items.WHEAT)) {
                wheat = i;
            } else if (o.get().is(Items.TORCH)) {
                torch = i;
            }
        }
        helper.assertTrue(glass >= 0 && wheat >= 0 && torch >= 0, "Angebote des Händlers fehlen");
        // kaufen
        int coins = Coins.total(owner);
        helper.assertValueEqual(Trading.trade(owner, merchant.getId(), glass), Trading.Result.DONE, "Glas kaufen");
        helper.assertValueEqual(owner.getInventory().countItem(Items.GLASS), offers.get(glass).get().getCount(), "Gekauftes Glas");
        helper.assertValueEqual(Coins.total(owner), coins - offers.get(glass).giveCoins(), "Preis des Glases");
        // verkaufen: ohne Ware nicht, mit Ware gegen Münzen
        helper.assertValueEqual(Trading.trade(owner, merchant.getId(), wheat), Trading.Result.MISSING_ITEMS, "Verkaufen ohne Ware");
        owner.getInventory().add(new ItemStack(Items.WHEAT, offers.get(wheat).give().getCount()));
        coins = Coins.total(owner);
        helper.assertValueEqual(Trading.trade(owner, merchant.getId(), wheat), Trading.Result.DONE, "Weizen verkaufen");
        helper.assertValueEqual(owner.getInventory().countItem(Items.WHEAT), 0, "Weizen nach dem Verkauf");
        helper.assertValueEqual(Coins.total(owner), coins + offers.get(wheat).getCoins(), "Erlös für den Weizen");
        // bis zur Grenze handeln: dann schließt der Laden
        int limit = Trading.limit();
        for (int i = 2; i < limit; i++) {
            helper.assertValueEqual(Trading.trade(owner, merchant.getId(), torch), Trading.Result.DONE, "Handel Nr. " + (i + 1));
        }
        helper.assertTrue(merchant.isClosed(), "Laden nach " + limit + " Handeln nicht geschlossen");
        long remaining = merchant.closedUntil() - System.currentTimeMillis();
        helper.assertTrue(Math.abs(remaining - Trading.pauseMillis()) < 5_000, "Pause des Ladens: " + remaining + " ms");
        helper.assertValueEqual(Trading.trade(owner, merchant.getId(), torch), Trading.Result.CLOSED, "Handel im geschlossenen Laden");
        // das Handelsfenster übers Netz
        RegistryFriendlyByteBuf buf = new RegistryFriendlyByteBuf(io.netty.buffer.Unpooled.buffer(), level.registryAccess());
        OpenTradePayload payload = new OpenTradePayload(merchant.getId(), merchant.skinTexture(), merchant.professionKey(), merchant.traderName(),
                Trading.offers(owner, merchant), 0, remaining, 0, false, limit, 180);
        OpenTradePayload.STREAM_CODEC.encode(buf, payload);
        OpenTradePayload copy = OpenTradePayload.STREAM_CODEC.decode(buf);
        helper.assertValueEqual(copy.offers().size(), payload.offers().size(), "Angebote übers Netz");
        helper.assertTrue(ItemStack.matches(copy.offers().get(glass).get(), payload.offers().get(glass).get()), "Angebot übers Netz verändert");

        // Speichern und Laden: Beruf, Siedlung, Werkstatt und Pause bleiben
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        merchant.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        CitizenEntity loaded = ModEntities.CITIZEN.get().create(level, EntitySpawnReason.LOAD);
        helper.assertTrue(loaded != null, "Bürger lässt sich nicht erzeugen");
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        helper.assertValueEqual(loaded.getProfession(), Profession.MERCHANT, "Beruf nach dem Laden");
        helper.assertTrue(town.id().equals(loaded.getTownId()) && market.equals(loaded.getWork()), "Siedlung/Werkstatt nach dem Laden");
        helper.assertTrue(loaded.isClosed() && loaded.closedUntil() == merchant.closedUntil(), "Pause nach dem Laden");
        loaded.discard();

        // Echtzeit: eine kurze Pause läuft ab, dann ist der Laden wieder offen und die Zählung beginnt von vorn
        CitizenEntity second = TownManager.spawnCitizen(level, town, Profession.MERCHANT);
        helper.assertTrue(second != null, "Zweiter Händler");
        second.recordTrade(1, 60);
        helper.assertTrue(second.isClosed(), "Kurze Pause beginnt nicht");
        long start = System.currentTimeMillis();
        helper.succeedWhen(() -> {
            helper.assertTrue(System.currentTimeMillis() - start >= 100, "Pause läuft noch");
            helper.assertTrue(!second.isClosed() && second.tradesUsed() == 0, "Laden nach der Pause noch zu");
            clearTowns(server);
            leave(helper, owner);
        });
    }

    // =====================================================================================
    // Krönung
    // =====================================================================================

    /**
     * Die Krönung: erst wenn das Siegel gebrochen ist, die Siedlung eine Stadt ist, das Ansehen legendär, ein Thronsaal
     * steht und der Spieler die Königskrone trägt, beginnt sie auf dem Thron – danach ist bei den eigenen Bürgern alles
     * umsonst.
     */
    private static void townCrown(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        SealState seal = SealState.of(server.overworld());
        boolean sealBefore = seal.isBroken();
        ServerPlayer owner = player(helper, new BlockPos(40, 1, 44));
        Town town = found(helper, owner, new BlockPos(44, 1, 44));
        seal.setBroken(false);
        seal.save(server.overworld());
        helper.assertValueEqual(Coronation.requirements(owner, town), 0, "Am Anfang ist nichts erfüllt");
        // Stadt: 22 Bewohner
        for (int i = 0; i < TownTier.CITY.residents(); i++) {
            helper.assertTrue(TownManager.spawnCitizen(level, town, Profession.BUILDER) != null, "Bürger Nr. " + (i + 1));
        }
        helper.assertValueEqual(town.tier(), TownTier.CITY, "Stufe mit 22 Bewohnern");
        owner.setData(ModAttachments.REPUTATION, Reputation.Rank.LEGENDARY.points());
        BlockPos hall = originFor(helper, BuildingType.THRONE_HALL, 22, 18);
        TownManager.buildInstantly(level, town, BuildingType.THRONE_HALL, hall, Rotation.NONE);
        owner.setItemSlot(EquipmentSlot.HEAD, new ItemStack(ModItems.GEMSTONE_CROWN.get()));
        int bits = Coronation.requirements(owner, town);
        helper.assertValueEqual(bits, Coronation.ALL & ~TownView.CROWN_SEAL, "Alles außer dem Siegel");
        // der Thron: ohne gebrochenes Siegel keine Krönung
        BlockPos throne = Blueprint.world(hall, Rotation.NONE, BuildingType.THRONE_HALL.blueprint().marks().getFirst());
        helper.assertTrue(level.getBlockState(throne).is(ModBlocks.THRONE.get()), "Thron steht nicht an seinem Platz");
        BlockState throneState = level.getBlockState(throne);
        helper.assertTrue(SeatEntity.sit(level, throne, ((io.github.sirschrolli.kingdomsatwar.block.SeatBlock) ModBlocks.THRONE.get()).seatHeight(),
                throneState.getValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING), owner), "Auf den Thron setzen");
        helper.assertTrue(owner.getVehicle() instanceof SeatEntity, "Spieler sitzt nicht");
        Coronation.onSit(owner, throne);
        seal.setBroken(true);
        seal.save(server.overworld());
        helper.assertValueEqual(Coronation.requirements(owner, town), Coronation.ALL, "Alle Voraussetzungen");
        // jetzt beginnt die Zeremonie
        Coronation.onSit(owner, throne);
        int price = TownManager.hirePrice(town, Profession.SMITH, owner);
        helper.assertTrue(price > 0, "Vor der Krönung kostet Anheuern etwas");
        helper.succeedWhen(() -> {
            helper.assertTrue(town.crowned(), "Noch nicht gekrönt");
            helper.assertValueEqual(TownManager.hirePrice(town, Profession.SMITH, owner), 0, "Anheuern nach der Krönung");
            helper.assertValueEqual(TownManager.buildPrice(town, BuildingType.TOWNHOUSE, owner), 0, "Bauen nach der Krönung");
            helper.assertTrue((Coronation.requirements(owner, town) & TownView.CROWN_DONE) != 0, "Krönung nicht vermerkt");
            seal.setBroken(sealBefore);
            seal.save(server.overworld());
            owner.stopRiding();
            clearTowns(server);
            leave(helper, owner);
        });
    }

    // =====================================================================================
    // Essen, Garnison und Schlachtzug
    // =====================================================================================

    /**
     * Essen im Stadtlager: Es zählt der Nährwert (Verdorbenes und Goldenes nicht). Ein neuer Bürger kommt satt an; an
     * einem neuen Tag isst er eine Mahlzeit – das Einfache zuerst. Ist nicht genug da, hungert er (die Übersicht zeigt es);
     * bringt man Essen, isst er beim nächsten Nachschauen.
     */
    private static void townFood(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 30));
        Town town = found(helper, owner, CENTER);
        BlockPos chestPos = helper.absolutePos(CENTER.offset(3, 0, 1));
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        net.minecraft.world.Container chest = (net.minecraft.world.Container) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.BREAD, 1));             // 5
        chest.setItem(1, new ItemStack(Items.CARROT, 2));            // 2 × 3
        chest.setItem(2, new ItemStack(Items.ROTTEN_FLESH, 8));      // verdorben: zählt nicht
        chest.setItem(3, new ItemStack(Items.GOLDEN_CARROT, 4));     // zu kostbar
        chest.setItem(5, new ItemStack(Items.WHEAT, 3));             // 3 × 2: das backen die Bürger selbst
        helper.assertValueEqual(TownFood.pantry(level, town), 17, "Nährwert im Lager");
        CitizenEntity citizen = TownManager.spawnCitizen(level, town, Profession.FARMER);
        helper.assertTrue(citizen != null, "Bürger nicht erschienen");
        long day = Provisions.day(level);
        citizen.mealTick(level);
        helper.assertTrue(!citizen.isHungry() && citizen.fedDay() == day, "neuer Bürger kommt nicht satt an");
        helper.assertValueEqual(TownFood.pantry(level, town), 17, "neuer Bürger hat schon gegessen");
        // ein neuer Tag: er isst – das Einfache zuerst (den Weizen), Möhren und Brot bleiben
        citizen.setFedDay(day - 1);
        citizen.mealTick(level);
        helper.assertTrue(!citizen.isHungry() && citizen.fedDay() == day, "Bürger hat nicht gegessen");
        helper.assertTrue(chest.getItem(5).isEmpty() && chest.getItem(1).getCount() == 2, "nicht den Weizen zuerst gegessen: "
                + chest.getItem(5) + ", " + chest.getItem(1));
        helper.assertValueEqual(TownFood.pantry(level, town), 11, "Nährwert nach der ersten Mahlzeit");
        // noch ein Tag: die Möhren, das Brot bleibt
        citizen.setFedDay(day - 1);
        citizen.mealTick(level);
        helper.assertTrue(chest.getItem(1).isEmpty() && chest.getItem(0).getCount() == 1, "nicht das Einfache zuerst gegessen: "
                + chest.getItem(0) + ", " + chest.getItem(1) + ", Lager " + TownFood.pantry(level, town));
        helper.assertValueEqual(TownFood.pantry(level, town), 5, "Nährwert nach der Mahlzeit");
        // noch ein Tag: zu wenig im Lager – er hungert, und die Übersicht zeigt es
        citizen.setFedDay(day - 1);
        citizen.mealTick(level);
        helper.assertTrue(citizen.isHungry(), "Bürger hungert nicht ohne Essen");
        helper.assertValueEqual(TownFood.pantry(level, town), 5, "hungriger Bürger hat trotzdem genommen");
        TownView view = TownView.of(owner, town);
        helper.assertValueEqual(view.residents().getFirst().state(), 4, "Übersicht zeigt den Hunger nicht");
        helper.assertValueEqual(view.foodDays(), 0, "Essen für Tage ohne Mahlzeit");
        // Essen gebracht: beim nächsten Nachschauen isst er
        chest.setItem(4, new ItemStack(Items.COOKED_BEEF, 3));       // 3 × 8
        helper.assertValueEqual(TownView.of(owner, town).foodDays(), 4, "Tage mit Brot und Braten");
        citizen.mealTick(level);
        helper.assertTrue(!citizen.isHungry() && citizen.fedDay() == day, "Bürger isst nicht, obwohl wieder Essen da ist");
        leave(helper, owner);
        helper.succeed();
    }

    /**
     * Garnison und Schlachtzug: Ein angeworbener Soldat zieht in die Siedlung und geht vor dem Tor der Kaserne Streife; er
     * zählt zur Armee und isst in der Garnison aus dem Stadtlager. Am Feldherrentisch bekommt er Proviant – erst aus dem
     * Lager, dann aus dem Gepäck –, zieht mit auf den Schlachtzug (lebt dort von seinem Proviant) und kehrt am Tisch oder
     * mit dem Kommandostab heim.
     */
    private static void townGarrison(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        clearTowns(server);
        ServerPlayer owner = player(helper, new BlockPos(24, 1, 30));
        Town town = found(helper, owner, CENTER);
        BlockPos barracks = originFor(helper, BuildingType.BARRACKS, 24, 10);
        TownManager.buildInstantly(level, town, BuildingType.BARRACKS, barracks, Rotation.NONE);
        BlockPos table = helper.absolutePos(CENTER.offset(-3, 0, 3));
        level.setBlock(table, ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState(), Block.UPDATE_ALL);
        BlockPos chestPos = helper.absolutePos(CENTER.offset(3, 0, 1));
        level.setBlock(chestPos, Blocks.CHEST.defaultBlockState(), Block.UPDATE_ALL);
        net.minecraft.world.Container chest = (net.minecraft.world.Container) level.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.BREAD, 4));             // 4 × 5
        int pantry = TownFood.pantry(level, town);
        helper.assertValueEqual(pantry, 20, "Nährwert im Lager");
        // angeworben: er zieht in die Garnison und geht vor der Kaserne Streife
        SoldierEntity soldier = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(30, 1, 30));
        soldier.recruitBy(owner);
        helper.assertTrue(soldier.isOwnedBy(owner) && !soldier.isOnCampaign(), "Soldat zieht nicht in die Garnison");
        helper.assertValueEqual(soldier.getMode(), SoldierEntity.MODE_PATROL, "Modus in der Garnison");
        BlockPos gate = Blueprint.world(barracks, Rotation.NONE, BuildingType.BARRACKS.blueprint().entrance());
        BlockPos post = soldier.getPost();
        helper.assertTrue(post != null && Math.abs(post.getX() - gate.getX()) <= 2 && Math.abs(post.getZ() - gate.getZ()) <= 2
                && Math.abs(post.getY() - gate.getY()) <= 2, "Posten nicht vor der Kaserne: " + post + ", Tor " + gate);
        helper.assertTrue(level.getBlockState(post).getCollisionShape(level, post).isEmpty()
                && level.getBlockState(post.below()).isFaceSturdy(level, post.below(), Direction.UP), "Posten nicht auf festem Boden: " + post);
        helper.assertValueEqual(Recruitment.armySize(level, owner.getUUID()), 1, "Armee nach dem Anwerben");
        helper.assertValueEqual(soldier.getRations(), Provisions.START_RATIONS, "Proviant beim Anwerben");
        // in der Garnison isst er aus dem Stadtlager – sein Proviant bleibt
        long day = Provisions.day(level);
        soldier.setFedDay(day - 1);
        soldier.rationTick(level);
        helper.assertTrue(!soldier.isHungry(), "Soldat in der Garnison hat nicht gegessen");
        helper.assertValueEqual(soldier.getRations(), Provisions.START_RATIONS, "in der Garnison vom Proviant gegessen");
        helper.assertValueEqual(TownFood.pantry(level, town), 10, "Mahlzeit nicht aus dem Stadtlager");
        // Proviant am Feldherrentisch: erst aus dem Lager, dann aus dem Gepäck (Verdorbenes nimmt keiner)
        owner.getInventory().clearContent();
        owner.getInventory().add(new ItemStack(Items.APPLE, 5));      // 5 × 4
        owner.getInventory().add(new ItemStack(Items.ROTTEN_FLESH, 9));
        helper.assertTrue(Provisions.supply(owner, table) == Recruitment.Result.SUPPLIED, "Proviant zuteilen klappt nicht");
        helper.assertValueEqual(soldier.getRations(), Provisions.MAX_RATIONS, "Proviant nach dem Zuteilen");
        helper.assertValueEqual(TownFood.pantry(level, town), 0, "das Lager zuerst");
        helper.assertValueEqual(owner.getInventory().countItem(Items.APPLE), 1, "Äpfel aus dem Gepäck");
        helper.assertValueEqual(owner.getInventory().countItem(Items.ROTTEN_FLESH), 9, "Verdorbenes zugeteilt");
        helper.assertTrue(Provisions.supply(owner, table) == Recruitment.Result.NOTHING_NEEDED, "voller Proviant");
        // Lager leer: in der Garnison isst er nun von seinem Proviant
        soldier.setFedDay(day - 1);
        soldier.rationTick(level);
        helper.assertTrue(!soldier.isHungry() && soldier.getRations() == Provisions.MAX_RATIONS - 1, "leeres Lager: nicht vom Proviant gegessen");
        // auf zum Schlachtzug: er folgt dem Spieler und lebt von seinem Proviant, auch wenn das Lager wieder voll ist
        helper.assertTrue(Garrison.march(owner, table) == Recruitment.Result.MARCHED, "Schlachtzug klappt nicht");
        helper.assertTrue(soldier.isOnCampaign() && soldier.getMode() == SoldierEntity.MODE_FOLLOW, "Soldat zieht nicht mit");
        helper.assertTrue(Garrison.march(owner, table) == Recruitment.Result.NO_SOLDIERS, "alle sind schon unterwegs");
        chest.setItem(0, new ItemStack(Items.BREAD, 10));
        soldier.setFedDay(day - 1);
        soldier.rationTick(level);
        helper.assertValueEqual(soldier.getRations(), Provisions.MAX_RATIONS - 2, "auf dem Schlachtzug nicht vom Proviant gegessen");
        helper.assertValueEqual(TownFood.pantry(level, town), 50, "auf dem Schlachtzug aus dem Stadtlager gegessen");
        // Heimkehr am Tisch: wieder Streife vor der Kaserne
        helper.assertTrue(Garrison.returnHome(owner, table) == Recruitment.Result.RETURNED, "Heimkehr klappt nicht");
        helper.assertTrue(!soldier.isOnCampaign() && soldier.getMode() == SoldierEntity.MODE_PATROL && post.equals(soldier.getPost()),
                "Soldat kehrt nicht auf seinen Posten heim: " + soldier.getPost());
        helper.assertTrue(Garrison.returnHome(owner, table) == Recruitment.Result.NO_SOLDIERS, "niemand ist mehr unterwegs");
        // mit dem Kommandostab (Schleichen und in den Himmel zeigen) heim
        soldier.setCampaign(true);
        soldier.setMode(SoldierEntity.MODE_FOLLOW, null);
        helper.assertValueEqual(Garrison.sendHome(owner), 1, "Kommandostab schickt niemanden heim");
        helper.assertTrue(!soldier.isOnCampaign() && soldier.getMode() == SoldierEntity.MODE_PATROL, "Kommandostab: nicht heimgekehrt");
        // gefallen: aus der Stammrolle
        soldier.discard();
        helper.assertValueEqual(Recruitment.armySize(level, owner.getUUID()), 0, "Armee nach dem Ende des Soldaten");
        leave(helper, owner);
        helper.succeed();
    }
}
