package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerGoals;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuilding;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Dörfer: jedes Gebäude in jeder Bauweise steht, hat seine Bewohner mit Bett (Kopfende, mit Platz zum
 * Aufstehen) und Arbeitsplatz; Bewohner finden abends ins Bett (auch im Obergeschoss) und tagsüber an die Arbeit;
 * Handel gegen Münzen mit allen Berufen; Wachen schützen die Bewohner; Speichern und Laden; die alten Dörfer und
 * Plünderer-Außenposten entstehen nicht mehr.
 */
final class VillageGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGE_BUILDINGS =
            ModGameTests.TEST_FUNCTIONS.register("village_buildings", () -> VillageGameTests::buildings);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGER_SLEEPS =
            ModGameTests.TEST_FUNCTIONS.register("villager_sleeps", () -> VillageGameTests::sleeps);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGER_WORKS =
            ModGameTests.TEST_FUNCTIONS.register("villager_works", () -> VillageGameTests::works);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGER_TRADES =
            ModGameTests.TEST_FUNCTIONS.register("villager_trades", () -> VillageGameTests::trades);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGE_GUARDS =
            ModGameTests.TEST_FUNCTIONS.register("village_guards", () -> VillageGameTests::guards);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGER_SAVE =
            ModGameTests.TEST_FUNCTIONS.register("villager_save", () -> VillageGameTests::save);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VANILLA_VILLAGES_OFF =
            ModGameTests.TEST_FUNCTIONS.register("vanilla_villages_off", () -> VillageGameTests::vanillaOff);

    private VillageGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, VILLAGE_BUILDINGS, 200);
        add(event, environment, VILLAGER_SLEEPS, 1600);
        add(event, environment, VILLAGER_WORKS, 1200);
        add(event, environment, VILLAGER_TRADES, 200);
        add(event, environment, VILLAGE_GUARDS, 200);
        add(event, environment, VILLAGER_SAVE, 100);
        add(event, environment, VANILLA_VILLAGES_OFF, 100);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, TOWN, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Baut ein Gebäude (alle Chunks seines Bereichs) mit dem Fußboden bei {@code local}; gibt das Bauteil zurück. */
    static KingdomVillagePiece build(GameTestHelper helper, VillageStyle style, VillageBuilding kind, BlockPos local, Rotation rotation) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(local);
        KingdomVillagePiece piece = KingdomVillagePiece.single(style, 0, kind, origin, rotation, 31L * kind.ordinal() + style.ordinal());
        BoundingBox box = piece.getBoundingBox();
        var generator = level.getChunkSource().getGenerator();
        // in einem Zug: in der fertigen Testwelt lösen gesetzte Blöcke Nachbar-Updates aus, die ein Bett an einer Chunkgrenze
        // auseinanderreißen würden, solange seine andere Hälfte noch fehlt (bei der Weltentstehung gibt es diese Updates nicht)
        BoundingBox all = new BoundingBox(box.minX(), level.getMinY(), box.minZ(), box.maxX(), level.getMaxY(), box.maxZ());
        piece.postProcess(level, level.structureManager(), generator, RandomSource.create(7), all, ChunkPos.containing(origin), origin);
        return piece;
    }

    /** Räumt die Testfläche (ab Höhe 1) und alle Wesen darauf. */
    static void clear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos a = helper.absolutePos(new BlockPos(0, 1, 0));
        BlockPos b = helper.absolutePos(new BlockPos(47, 29, 47));
        for (BlockPos pos : BlockPos.betweenClosed(a, b)) {
            if (!level.getBlockState(pos).isAir()) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
            }
        }
        for (Entity e : level.getEntities((Entity) null, AABB.encapsulatingFullBlocks(a.below(), b), e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    static List<VillagerEntity> villagers(GameTestHelper helper) {
        BlockPos a = helper.absolutePos(new BlockPos(-8, -4, -8));
        BlockPos b = helper.absolutePos(new BlockPos(56, 34, 56));
        return helper.getLevel().getEntitiesOfClass(VillagerEntity.class, AABB.encapsulatingFullBlocks(a, b));
    }

    // =====================================================================================
    // Gebäude
    // =====================================================================================

    /**
     * Jedes Gebäude in jeder Bauweise: Es entsteht ohne Fehler, seine Bewohner erscheinen, jedes Bett ist ein Kopfende mit
     * Platz zum Aufstehen, jeder Arbeitsplatz ist begehbar.
     */
    private static void buildings(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        int residents = 0;
        for (VillageStyle style : VillageStyle.values()) {
            for (VillageBuilding kind : VillageBuilding.values()) {
                if (kind == VillageBuilding.MARKET_STALL) {
                    continue;
                }
                clear(helper);
                int floor = style == VillageStyle.STILT ? 2 : 0;
                if (floor > 0) {
                    // der Steg vor dem Pfahlbau (wie im Dorf auf Höhe des Fußbodens)
                    for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(24 - kind.width / 2 - 3, floor, 9)),
                            helper.absolutePos(new BlockPos(24 + kind.width / 2 + 3, floor, 13)))) {
                        level.setBlock(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                    }
                }
                KingdomVillagePiece piece = build(helper, style, kind, new BlockPos(24 - kind.width / 2, floor, 14), Rotation.NONE);
                String name = style.id() + "/" + kind.id();
                int before = problems.size();
                List<VillagerEntity> here = villagers(helper);
                boolean expectsResidents = kind != VillageBuilding.CHAPEL && kind != VillageBuilding.WATCHTOWER;
                if (expectsResidents && here.isEmpty()) {
                    problems.add(name + ": keine Bewohner");
                }
                residents += here.size();
                // von der Straße vor dem Haus aus muss jeder Bewohner sein Bett und seinen Arbeitsplatz erreichen
                BlockPos street = helper.absolutePos(new BlockPos(24, floor + 1, 11));
                for (VillagerEntity v : here) {
                    String who = name + " (" + v.getProfession().id() + ")";
                    // ins Bett geht es vom Arbeitsplatz aus (oder von der Straße, wer keinen hat)
                    BlockPos from = v.getWork() != null ? v.getWork() : street;
                    v.teleportTo(from.getX() + 0.5, from.getY(), from.getZ() + 0.5);
                    v.setOnGround(true);
                    BlockPos bed = v.getBed();
                    if (bed != null) {
                        BlockState state = level.getBlockState(bed);
                        if (!(state.getBlock() instanceof AbstractBedBlock)) {
                            problems.add(who + ": Bett fehlt");
                        } else if (state.getValue(BedBlock.PART) != BedPart.HEAD) {
                            problems.add(who + ": Bett ist das Fußende");
                        } else {
                            var standUp = AbstractBedBlock.findStandUpPosition(v.getType(), level, bed, state.getValue(BedBlock.FACING), 0.0f);
                            if (standUp.isEmpty()) {
                                problems.add(who + ": kein Platz zum Aufstehen am Bett");
                            } else if (!reachable(v, BlockPos.containing(standUp.get()))) {
                                problems.add(who + ": kein Weg zum Bett " + helper.relativePos(bed));
                            }
                            if (!insideWalls(helper, bed)) {
                                problems.add(who + ": Bett steckt in der Wand");
                            }
                        }
                    }
                    BlockPos work = v.getWork();
                    v.teleportTo(street.getX() + 0.5, street.getY(), street.getZ() + 0.5);
                    v.setOnGround(true);
                    if (work != null) {
                        // ein Teppich stört nicht, ein Stuhl, eine Bank oder ein Block schon
                        if (level.getBlockState(work).getCollisionShape(level, work).max(net.minecraft.core.Direction.Axis.Y) > 0.2
                                || !level.getBlockState(work.above()).getCollisionShape(level, work.above()).isEmpty()) {
                            problems.add(who + ": Arbeitsplatz ist verbaut (" + level.getBlockState(work) + ")");
                        } else if (!reachable(v, work)) {
                            problems.add(who + ": kein Weg zum Arbeitsplatz " + helper.relativePos(work));
                        }
                    }
                    // wer von weit her kommt, geht über den Platz vor der Haustür: der muss von der Straße aus erreichbar sein,
                    // und von dort geht es hinein an den Arbeitsplatz (und von dem aus, wie oben geprüft, ins Bett)
                    BlockPos door = v.getFrontDoor();
                    if (door == null) {
                        problems.add(who + ": keine Haustür");
                        continue;
                    }
                    if (!reachable(v, door)) {
                        problems.add(who + ": Platz vor der Haustür " + helper.relativePos(door) + " nicht erreichbar");
                        continue;
                    }
                    v.teleportTo(door.getX() + 0.5, door.getY(), door.getZ() + 0.5);
                    v.setOnGround(true);
                    if (work != null && !reachable(v, work)) {
                        problems.add(who + ": von der Haustür " + helper.relativePos(door) + " kein Weg zum Arbeitsplatz");
                    }
                }
                if (problems.size() > before && style == VillageStyle.TIMBER) {
                    dump(helper, piece, kind);
                }
            }
        }
        clear(helper);
        KingdomsAtWar.LOGGER.info("[Test] Dörfer: {} Bewohner in allen Gebäuden aller Bauweisen", residents);
        helper.assertTrue(problems.isEmpty(), "Gebäude: " + String.join("; ", problems));
        helper.succeed();
    }

    /** Nur zur Fehlersuche: die Geschosse eines Gebäudes als Buchstaben ins Protokoll (von vorn = unten). */
    private static void dump(GameTestHelper helper, KingdomVillagePiece piece, VillageBuilding kind) {
        ServerLevel level = helper.getLevel();
        StringBuilder out = new StringBuilder("[Test] Grundriss " + kind.id() + "\n");
        for (int y = 1; y <= 8; y++) {
            out.append("y=").append(y).append('\n');
            for (int z = kind.depth; z >= -3; z--) {
                for (int x = -1; x <= kind.width; x++) {
                    BlockPos pos = piece.origin().offset(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock()).getPath();
                    char c = state.isAir() ? '.' : id.contains("bed") ? 'B' : id.contains("door") ? 'D' : id.contains("stairs") ? 'S'
                            : id.contains("fence") ? 'f' : id.contains("carpet") ? '_' : id.contains("chair") || id.contains("bench") ? 'h'
                            : id.contains("table") ? 't' : id.contains("slab") ? 's' : id.contains("trapdoor") || id.contains("shutters") ? 'L'
                            : id.contains("ladder") ? 'H' : state.getCollisionShape(level, pos).isEmpty() ? ',' : '#';
                    out.append(c);
                }
                out.append('\n');
            }
        }
        KingdomsAtWar.LOGGER.info(out.toString());
    }

    /** Nur zur Fehlersuche: was der Bewohner gerade tut und wohin sein Weg führt. */
    private static String describePath(GameTestHelper helper, VillagerEntity v) {
        var path = v.getNavigation().getPath();
        BlockPos bed = v.getBed();
        String fresh = "";
        if (bed != null && helper.getLevel().getBlockState(bed).getBlock() instanceof AbstractBedBlock) {
            var standUp = AbstractBedBlock.findStandUpPosition(v.getType(), helper.getLevel(), bed,
                    helper.getLevel().getBlockState(bed).getValue(BedBlock.FACING), 0.0f);
            if (standUp.isPresent()) {
                var p = v.getNavigation().createPath(BlockPos.containing(standUp.get()), 1);
                fresh = ", neuer Weg " + (p == null ? "keiner" : p.getNodeCount() + "/" + p.canReach() + " bis " + helper.relativePos(p.getEndNode().asBlockPos()));
            }
        }
        return " [Ziele " + v.runningGoals() + ", Weg " + (path == null ? "keiner" : path.getNodeCount() + "/" + path.canReach() + " bis "
                + helper.relativePos(path.getEndNode().asBlockPos())) + fresh + ", Haustür " + (v.getFrontDoor() == null ? "-" : helper.relativePos(v.getFrontDoor()))
                + ", am Boden " + v.onGround() + "]";
    }

    /** Findet der Bewohner (von seinem Platz aus) einen Weg bis an {@code target} heran? */
    private static boolean reachable(VillagerEntity v, BlockPos target) {
        net.minecraft.world.level.pathfinder.Path path = v.getNavigation().createPath(target, 1);
        return path != null && path.canReach();
    }

    /** Hat das Bett ringsum (seitlich) wenigstens einen freien Nachbarn – steckt es also nicht in einer Wand? */
    private static boolean insideWalls(GameTestHelper helper, BlockPos bed) {
        ServerLevel level = helper.getLevel();
        BlockState state = level.getBlockState(bed);
        BlockPos foot = bed.relative(state.getValue(BedBlock.FACING).getOpposite());
        // das Kopfende darf an eine Wand stoßen, aber nicht von ihr umschlossen sein: links oder rechts muss Luft sein
        for (BlockPos part : List.of(bed, foot)) {
            for (net.minecraft.core.Direction side : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                BlockPos n = part.relative(side);
                if (!n.equals(bed) && !n.equals(foot) && level.getBlockState(n).getCollisionShape(level, n).isEmpty()) {
                    return true;
                }
            }
        }
        return false;
    }

    // =====================================================================================
    // Tagesablauf
    // =====================================================================================

    /** Abends finden alle Bewohner der Wohnhäuser (auch im Obergeschoss) und der Schmiede ohne Abkürzung ins Bett. */
    private static void sleeps(GameTestHelper helper) {
        clear(helper);
        VillagerGoals.allowShortcuts = false;
        build(helper, VillageStyle.TIMBER, VillageBuilding.HOUSE, new BlockPos(2, 0, 4), Rotation.NONE);
        build(helper, VillageStyle.TIMBER, VillageBuilding.HOUSE_LARGE, new BlockPos(14, 0, 4), Rotation.NONE);
        build(helper, VillageStyle.TIMBER, VillageBuilding.SMITHY, new BlockPos(30, 0, 4), Rotation.NONE);
        build(helper, VillageStyle.NORDIC, VillageBuilding.TAVERN, new BlockPos(4, 0, 26), Rotation.NONE);
        build(helper, VillageStyle.CLAY, VillageBuilding.HOUSE_SMALL, new BlockPos(24, 0, 28), Rotation.NONE);
        List<VillagerEntity> sleepers = villagers(helper).stream().filter(v -> v.getBed() != null).toList();
        helper.assertTrue(sleepers.size() >= 8, "zu wenige Bewohner mit Bett: " + sleepers.size());
        sleepers.forEach(v -> v.setTimeOverride(13000));
        helper.succeedWhen(() -> {
            // die Einzelheiten (mit einer Wegsuche) erst ganz am Ende – sie sollen den Bewohnern nicht dazwischenfunken
            boolean last = helper.getTick() >= 1590;
            List<String> awake = sleepers.stream().filter(v -> !v.isSleeping())
                    .map(v -> v.getProfession().id() + " bei " + helper.relativePos(v.blockPosition()) + " (Bett " + helper.relativePos(v.getBed()) + ")"
                            + (last ? describePath(helper, v) : ""))
                    .toList();
            if (!awake.isEmpty()) {
                helper.fail("noch wach: " + String.join(", ", awake));
            }
            VillagerGoals.allowShortcuts = true;
        });
    }

    /** Tagsüber gehen Schmiede und Bäcker an ihren Arbeitsplatz und arbeiten (mit Werkzeug in der Hand). */
    private static void works(GameTestHelper helper) {
        clear(helper);
        build(helper, VillageStyle.TIMBER, VillageBuilding.SMITHY, new BlockPos(4, 0, 6), Rotation.NONE);
        build(helper, VillageStyle.CLAY, VillageBuilding.BAKERY, new BlockPos(28, 0, 6), Rotation.NONE);
        List<VillagerEntity> workers = villagers(helper).stream().filter(v -> v.getWork() != null && v.canTrade()).toList();
        helper.assertTrue(workers.size() >= 3, "zu wenige Handwerker: " + workers.size());
        workers.forEach(v -> {
            v.setTimeOverride(3000);
            // erst einmal auf die Straße davor (sie sollen den Weg finden; außerhalb des Testbereichs stünden sie in einem
            // Chunk, der nicht mitläuft)
            BlockPos away = helper.absolutePos(new BlockPos(helper.relativePos(v.getWork()).getX(), 0, 1));
            v.teleportTo(away.getX() + 0.5, away.getY() + 1, away.getZ() + 0.5);
        });
        helper.succeedWhen(() -> {
            for (VillagerEntity v : workers) {
                double d = v.position().distanceTo(Vec3.atBottomCenterOf(v.getWork()));
                if (d > 1.7 || !v.isWorking()) {
                    var path = v.getNavigation().getPath();
                    helper.fail(v.getProfession().id() + " nicht bei der Arbeit (Abstand " + String.format("%.1f", d) + ", bei "
                            + helper.relativePos(v.blockPosition()) + ", Arbeitsplatz " + helper.relativePos(v.getWork()) + ", Ziele " + v.runningGoals()
                            + ", Weg " + (path == null ? "keiner" : path.getNodeCount() + " Schritte, erreicht " + path.canReach() + ", Ende "
                                    + helper.relativePos(path.getEndNode().asBlockPos())) + ", am Boden " + v.onGround() + ")");
                }
                if (v.getMainHandItem().isEmpty()) {
                    helper.fail(v.getProfession().id() + " ohne Werkzeug");
                }
            }
        });
    }

    // =====================================================================================
    // Handel
    // =====================================================================================

    /** Jeder Beruf (außer Tagelöhnern) hat feste Angebote; Handel gegen Münzen; die Pause und neue Ware danach. */
    @SuppressWarnings("removal")
    private static void trades(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        clear(helper);
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        BlockPos spot = helper.absolutePos(new BlockPos(24, 1, 24));
        player.snapTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 0.0f, 0.0f);
        for (VillageProfession profession : VillageProfession.values()) {
            VillagerEntity v = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.COMMAND);
            helper.assertTrue(v != null, "Dorfbewohner lässt sich nicht erzeugen");
            v.setup(profession, 0, "Test", spot, "Testdorf", null, null, 12345L + profession.ordinal());
            v.snapTo(spot.getX() + 1.5, spot.getY(), spot.getZ() + 0.5, 0.0f, 0.0f);
            v.setNoAi(true);
            level.addFreshEntity(v);
            List<Trading.Offer> offers = v.baseOffers();
            if (!profession.trader()) {
                helper.assertTrue(offers.isEmpty(), "Tagelöhner handeln nicht");
                v.discard();
                continue;
            }
            helper.assertTrue(offers.size() >= 8, profession.id() + ": nur " + offers.size() + " Angebote");
            for (Trading.Offer o : offers) {
                helper.assertTrue(o.giveCoins() > 0 || o.getCoins() > 0, profession.id() + ": Angebot ohne Preis");
                helper.assertTrue(!o.get().isEmpty() || !o.give().isEmpty(), profession.id() + ": leeres Angebot");
            }
            // dieselbe Liste bei jedem Abruf (sonst stimmt die Nummer beim Handeln nicht)
            List<Trading.Offer> again = v.baseOffers();
            for (int i = 0; i < offers.size(); i++) {
                helper.assertTrue(ItemStack.matches(offers.get(i).get(), again.get(i).get()) && offers.get(i).giveCoins() == again.get(i).giveCoins(),
                        profession.id() + ": Angebote ändern sich zwischen zwei Abrufen");
            }
            // das erste Kaufangebot ohne Sonderware wirklich handeln
            int index = -1;
            for (int i = 0; i < offers.size(); i++) {
                if (offers.get(i).giveCoins() > 0 && offers.get(i).special().isEmpty() && offers.get(i).give().isEmpty()) {
                    index = i;
                    break;
                }
            }
            helper.assertTrue(index >= 0, profession.id() + ": nichts zu kaufen");
            Trading.Offer offer = Trading.offers(player, v).get(index);
            player.getInventory().clearContent();
            Coins.give(player, offer.giveCoins() + 7);
            helper.assertValueEqual(Trading.trade(player, v.getId(), index), Trading.Result.DONE, profession.id() + ": Kauf");
            helper.assertTrue(player.getInventory().countItem(offer.get().getItem()) >= offer.get().getCount(), profession.id() + ": Ware fehlt");
            helper.assertValueEqual(Coins.total(player), 7, profession.id() + ": Wechselgeld");
            v.discard();
        }
        // Pause und neue Ware: der Schreiber bietet nach der Pause andere Bücher an
        VillagerEntity scribe = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.COMMAND);
        scribe.setup(VillageProfession.SCRIBE, 0, "Test", spot, "Testdorf", null, null, 99L);
        List<String> before = scribe.baseOffers().stream().map(o -> o.get().getComponents().toString()).toList();
        boolean changed = false;
        for (int round = 0; round < 6 && !changed; round++) {
            scribe.recordTrade(1, 0L);
            helper.assertTrue(!scribe.isClosed(), "Laden nach der Pause nicht wieder offen");
            List<String> after = scribe.baseOffers().stream().map(o -> o.get().getComponents().toString()).toList();
            changed = !after.equals(before);
        }
        helper.assertTrue(changed, "nach der Pause gibt es keine neue Ware");
        player.getInventory().clearContent();
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }

    // =====================================================================================
    // Wachen
    // =====================================================================================

    /** Greift jemand einen Bewohner an, gehen die Wachen auf ihn los; die Pfeile der Wachen treffen keine Bewohner. */
    private static void guards(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        clear(helper);
        BlockPos post = helper.absolutePos(new BlockPos(20, 1, 24));
        SoldierEntity guard = ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get().create(level, EntitySpawnReason.COMMAND);
        guard.snapTo(post.getX() + 0.5, post.getY(), post.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(guard);
        guard.guardVillage("Testdorf", post);
        helper.assertTrue(guard.isVillageGuard(), "keine Dorfwache");
        VillagerEntity villager = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.COMMAND);
        villager.setup(VillageProfession.BAKER, 0, "Agnes", post, "Testdorf", null, null, 1L);
        villager.snapTo(post.getX() + 3.5, post.getY(), post.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(villager);
        Zombie zombie = EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND);
        zombie.snapTo(post.getX() + 6.5, post.getY(), post.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(zombie);
        float health = villager.getHealth();
        helper.assertTrue(!villager.hurtServer(level, level.damageSources().mobAttack(guard), 5.0f) && villager.getHealth() == health,
                "die eigene Wache verletzt einen Bewohner");
        helper.assertTrue(villager.hurtServer(level, level.damageSources().mobAttack(zombie), 1.0f), "Angriff des Zombies kommt nicht an");
        helper.assertTrue(guard.getTarget() == zombie, "die Wache verteidigt den Bewohner nicht");
        helper.succeed();
    }

    // =====================================================================================
    // Speichern
    // =====================================================================================

    private static void save(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(24, 1, 24));
        VillagerEntity v = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.COMMAND);
        v.setup(VillageProfession.CARTOGRAPHER, 2, "Clara", center, "Lindenau", center.offset(3, 0, 1), center.offset(-2, 0, 4), 4242L);
        v.recordTrade(1, 3_600_000L);
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        v.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        VillagerEntity loaded = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.LOAD);
        loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        helper.assertValueEqual(loaded.getProfession(), VillageProfession.CARTOGRAPHER, "Beruf");
        helper.assertValueEqual(loaded.getVariant(), 2, "Gesicht");
        helper.assertTrue(loaded.isFeminine(), "die Kartografin ist eine Frau");
        helper.assertValueEqual(loaded.getVillageName(), "Lindenau", "Dorf");
        helper.assertValueEqual(loaded.getVillageCenter(), center, "Dorfmitte");
        helper.assertValueEqual(loaded.getBed(), center.offset(3, 0, 1), "Bett");
        helper.assertValueEqual(loaded.getWork(), center.offset(-2, 0, 4), "Arbeitsplatz");
        helper.assertTrue(loaded.isClosed() && loaded.closedUntil() == v.closedUntil(), "Pause des Ladens");
        helper.assertTrue(ItemStack.matches(loaded.baseOffers().getFirst().get(), v.baseOffers().getFirst().get()), "Angebote nach dem Laden");
        helper.assertValueEqual(loaded.getCustomName().getString(), "Clara", "Name");
        helper.succeed();
    }

    // =====================================================================================
    // Alte Dörfer
    // =====================================================================================

    /** Die Biom-Listen der alten Dörfer und Außenposten sind leer; die eigenen Dörfer haben ihr Land. */
    private static void vanillaOff(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().lookupOrThrow(Registries.BIOME);
        for (var tag : List.of(BiomeTags.HAS_VILLAGE_PLAINS, BiomeTags.HAS_VILLAGE_DESERT, BiomeTags.HAS_VILLAGE_SAVANNA, BiomeTags.HAS_VILLAGE_SNOWY,
                BiomeTags.HAS_VILLAGE_TAIGA, BiomeTags.HAS_PILLAGER_OUTPOST)) {
            helper.assertTrue(biomes.get(tag).map(set -> set.size() == 0).orElse(true), "alte Bauwerke noch erlaubt: " + tag.location());
        }
        var structures = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        for (var key : List.of(BuiltinStructures.VILLAGE_PLAINS, BuiltinStructures.VILLAGE_DESERT, BuiltinStructures.VILLAGE_SAVANNA,
                BuiltinStructures.VILLAGE_SNOWY, BuiltinStructures.VILLAGE_TAIGA, BuiltinStructures.PILLAGER_OUTPOST)) {
            structures.get(key).ifPresent(s -> helper.assertTrue(s.value().biomes().size() == 0, "altes Bauwerk hat noch Land: " + key.identifier()));
        }
        for (VillageStyle style : VillageStyle.values()) {
            helper.assertTrue(biomes.get(ModTags.Biomes.HAS_VILLAGE.get(style)).map(set -> set.size() > 0).orElse(false),
                    "kein Land für Dörfer der Bauweise " + style.id());
        }
        helper.succeed();
    }
}
