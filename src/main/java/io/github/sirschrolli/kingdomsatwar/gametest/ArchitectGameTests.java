package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.architect.BuildRules;
import io.github.sirschrolli.kingdomsatwar.architect.PlanInfo;
import io.github.sirschrolli.kingdomsatwar.architect.PlanLibrary;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownStorage;
import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.WallSignBlock;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Spieltests des Dragon Architect: Regeln (was übernommen wird, was kostbar ist), Vermessen (Bodenlinie, Vorderseite,
 * Drehung, Betten, Eingang, Speichern), Keller und der Nachbau durch den Baumeister – abschnittsweise, mit einem
 * kostbaren Block, auf den er wartet, bis er im Lager liegt.
 */
final class ArchitectGameTests {
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_RULES =
            ModGameTests.TEST_FUNCTIONS.register("architect_rules", () -> ArchitectGameTests::rules);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_SCAN =
            ModGameTests.TEST_FUNCTIONS.register("architect_scan", () -> ArchitectGameTests::scan);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_BASEMENT =
            ModGameTests.TEST_FUNCTIONS.register("architect_basement", () -> ArchitectGameTests::basement);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_BUILD =
            ModGameTests.TEST_FUNCTIONS.register("architect_build", () -> ArchitectGameTests::build);

    private ArchitectGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event) {
        own(event, ARCHITECT_RULES, "architect_rules", 100);
        own(event, ARCHITECT_SCAN, "architect_scan", 200);
        own(event, ARCHITECT_BASEMENT, "architect_basement", 200);
        own(event, ARCHITECT_BUILD, "architect_build", 9000);
    }

    private static void own(RegisterGameTestsEvent event, DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test,
            String name, int maxTicks) {
        Holder<TestEnvironmentDefinition<?>> env = event.registerEnvironment(KingdomsAtWar.id(name));
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(env, Level.OVERWORLD, TownGameTests.TOWN, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Regeln
    // =====================================================================================

    private static void rules(GameTestHelper helper) {
        helper.assertValueEqual(BuildRules.kind(Blocks.OAK_PLANKS.defaultBlockState()), BuildRules.Kind.MATERIAL, "Bretter");
        helper.assertValueEqual(BuildRules.kind(Blocks.STONE_BRICKS.defaultBlockState()), BuildRules.Kind.MATERIAL, "Steinziegel");
        helper.assertValueEqual(BuildRules.kind(Blocks.DIRT.defaultBlockState()), BuildRules.Kind.FREE, "Erde");
        helper.assertValueEqual(BuildRules.kind(Blocks.WATER.defaultBlockState()), BuildRules.Kind.FREE, "stehendes Wasser");
        helper.assertValueEqual(BuildRules.kind(Blocks.LAVA.defaultBlockState()), BuildRules.Kind.SKIP, "Lava");
        helper.assertValueEqual(BuildRules.kind(Blocks.DIAMOND_BLOCK.defaultBlockState()), BuildRules.Kind.PRECIOUS, "Diamantblock");
        helper.assertValueEqual(BuildRules.kind(Blocks.GOLD_BLOCK.defaultBlockState()), BuildRules.Kind.PRECIOUS, "Goldblock");
        helper.assertValueEqual(BuildRules.kind(Blocks.IRON_ORE.defaultBlockState()), BuildRules.Kind.PRECIOUS, "Eisenerz");
        helper.assertValueEqual(BuildRules.kind(Blocks.OBSIDIAN.defaultBlockState()), BuildRules.Kind.PRECIOUS, "Obsidian");
        helper.assertValueEqual(BuildRules.kind(Blocks.ANVIL.defaultBlockState()), BuildRules.Kind.PRECIOUS, "Amboss");
        helper.assertValueEqual(BuildRules.kind(Blocks.BEACON.defaultBlockState()), BuildRules.Kind.PRECIOUS, "Leuchtfeuer");
        helper.assertValueEqual(BuildRules.kind(Blocks.HAY_BLOCK.defaultBlockState()) == BuildRules.Kind.PRECIOUS, false, "Heuballen kostbar");
        helper.assertValueEqual(BuildRules.kind(Blocks.SPAWNER.defaultBlockState()), BuildRules.Kind.SKIP, "Spawner");
        helper.assertValueEqual(BuildRules.kind(Blocks.BEDROCK.defaultBlockState()), BuildRules.Kind.SKIP, "Grundgestein");
        helper.assertValueEqual(BuildRules.kind(Blocks.COMMAND_BLOCK.defaultBlockState()), BuildRules.Kind.SKIP, "Befehlsblock");
        helper.assertValueEqual(BuildRules.kind(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.TOWN_BANNER.get().defaultBlockState()),
                BuildRules.Kind.SKIP, "Stadtbanner");
        helper.assertValueEqual(BuildRules.precious(Blocks.WATER_CAULDRON.defaultBlockState(), true), Items.CAULDRON, "Wasserkessel");
        // Bereinigen
        BlockState wheat = BuildRules.normalize(Blocks.WHEAT.defaultBlockState().setValue(CropBlock.AGE, 7));
        helper.assertValueEqual(wheat.getValue(CropBlock.AGE), 0, "Reifer Weizen wird frisch gesät");
        helper.assertTrue(BuildRules.normalize(Blocks.OAK_LEAVES.defaultBlockState()).getValue(BlockStateProperties.PERSISTENT), "Laub dauerhaft");
        helper.assertValueEqual(BuildRules.normalize(Blocks.LAVA_CAULDRON.defaultBlockState()), Blocks.CAULDRON.defaultBlockState(), "Lavakessel");
        helper.assertTrue(!BuildRules.normalize(Blocks.PISTON.defaultBlockState().setValue(BlockStateProperties.EXTENDED, true))
                .getValue(BlockStateProperties.EXTENDED), "Kolben eingefahren");
        // Befehle aus Schildern
        CompoundTag sign = new CompoundTag();
        CompoundTag message = new CompoundTag();
        message.putString("text", "Klick");
        CompoundTag click = new CompoundTag();
        click.putString("action", "run_command");
        click.putString("command", "op Bob");
        message.put("click_event", click);
        net.minecraft.nbt.ListTag messages = new net.minecraft.nbt.ListTag();
        messages.add(message);
        CompoundTag front = new CompoundTag();
        front.put("messages", messages);
        sign.put("front_text", front);
        BuildRules.harmless(sign);
        helper.assertTrue(!sign.toString().contains("click_event") && sign.toString().contains("Klick"), "Befehl im Schild: " + sign);
        helper.succeed();
    }

    // =====================================================================================
    // Vermessen
    // =====================================================================================

    /**
     * Baut ein kleines Haus (Steinboden, Bruchsteinwände, Tür nach Osten, Bett, Goldblock, Spawner, Truhe mit Inhalt,
     * Schild mit Text) auf einer Lage Gras und gibt die beiden Ecken des Bereichs zurück (mit der Graslage).
     */
    static BlockPos[] house(GameTestHelper helper, int x0, int z0) {
        ServerLevel level = helper.getLevel();
        int g = TownGameTests.ground(helper);
        BlockPos a = helper.absolutePos(new BlockPos(x0, g - 1, z0));
        BlockPos b = helper.absolutePos(new BlockPos(x0 + 6, g + 4, z0 + 5));
        for (BlockPos p : BlockPos.betweenClosed(a, b)) {
            int dy = p.getY() - a.getY();
            int dx = p.getX() - a.getX();
            int dz = p.getZ() - a.getZ();
            BlockState state;
            boolean wall = dx == 0 || dx == 6 || dz == 0 || dz == 5;
            if (dy == 0) {
                state = Blocks.GRASS_BLOCK.defaultBlockState();
            } else if (dy == 1) {
                state = Blocks.STONE_BRICKS.defaultBlockState();
            } else if (dy <= 4) {
                state = wall ? Blocks.COBBLESTONE.defaultBlockState() : Blocks.AIR.defaultBlockState();
            } else {
                state = Blocks.OAK_PLANKS.defaultBlockState();
            }
            level.setBlock(p, state, Block.UPDATE_CLIENTS);
        }
        BlockPos door = a.offset(6, 2, 2);
        level.setBlock(door, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST), Block.UPDATE_CLIENTS);
        level.setBlock(door.above(), Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.WEST)
                .setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
        level.setBlock(a.offset(0, 3, 2), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_ALL);
        BlockPos foot = a.offset(2, 2, 3);
        level.setBlock(foot, Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH), Block.UPDATE_CLIENTS);
        level.setBlock(foot.north(), Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH)
                .setValue(BedBlock.PART, BedPart.HEAD), Block.UPDATE_CLIENTS);
        level.setBlock(a.offset(1, 2, 4), Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        level.setBlock(a.offset(4, 2, 4), Blocks.SPAWNER.defaultBlockState(), Block.UPDATE_CLIENTS);
        BlockPos chest = a.offset(4, 2, 1);
        level.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(chest) instanceof Container c) {
            c.setItem(0, new ItemStack(Items.DIAMOND, 5));
        }
        BlockPos sign = a.offset(3, 3, 1);
        level.setBlock(sign, Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
        if (level.getBlockEntity(sign) instanceof SignBlockEntity be) {
            be.updateText(text -> text.asMutable().setLine(0, Component.literal("Zum Drachen")).asImmutable(),
                    net.minecraft.world.level.block.entity.SignTextSlot.FRONT);
        }
        return new BlockPos[] {a, b};
    }

    private static void scan(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TownGameTests.clearArea(helper);
        BlockPos[] box = house(helper, 6, 4);
        int ground = PlanScan.autoGround(level, box[0], box[1]);
        helper.assertValueEqual(ground, 1, "Bodenlinie (eine Lage Gras)");
        Direction door = PlanScan.doorSide(level, box[0], box[1], ground, null);
        helper.assertValueEqual(door, Direction.EAST, "Seite der Haustür");
        PlanScan scan = PlanScan.scan(level, box[0], box[1], ground, door, level.registryAccess());
        // gedreht: die Tür (Osten) liegt jetzt vorn (−z); die Graslage ist abgeschnitten
        helper.assertValueEqual(scan.sizeX, 6, "Breite nach dem Drehen");
        helper.assertValueEqual(scan.sizeZ, 7, "Tiefe nach dem Drehen");
        helper.assertValueEqual(scan.sizeY, 5, "Höhe ohne Gelände");
        helper.assertValueEqual(scan.ground, 0, "Lagen unter der Bodenlinie");
        BlockPos d = scan.door();
        helper.assertTrue(d != null && d.getZ() == 0, "Haupttür vorn: " + d);
        helper.assertValueEqual(scan.entrance().getZ(), -(scan.sizeZ - 1) / 2 - 1, "Eingang vor der Vorderseite");
        helper.assertValueEqual(scan.beds().size(), 1, "Betten");
        PlanScan.Costs costs = scan.costs();
        helper.assertValueEqual(costs.precious().getOrDefault(Items.GOLD_BLOCK, 0), 1, "Goldblock kostbar");
        helper.assertValueEqual(scan.skipped().getOrDefault(Blocks.SPAWNER, 0), 1, "Spawner nicht übernommen");
        helper.assertTrue(costs.units()[io.github.sirschrolli.kingdomsatwar.economy.town.Material.STONE.ordinal()] > 40, "Stein-Einheiten");
        // speichern und laden: dieselben Blöcke
        PlanScan copy = PlanScan.load(scan.save());
        for (int y = 0; y < scan.sizeY; y++) {
            for (int z = 0; z < scan.sizeZ; z++) {
                for (int x = 0; x < scan.sizeX; x++) {
                    helper.assertValueEqual(copy.get(x, y, z), scan.get(x, y, z), "Geladen anders bei " + x + "," + y + "," + z);
                }
            }
        }
        // nachbauen (sofort) ein Stück weiter: Tür, Gold, Schild, leere Truhe, kein Spawner
        Blueprint plan = copy.toBlueprint();
        int g = TownGameTests.ground(helper);
        BlockPos origin = helper.absolutePos(new BlockPos(30, g, 20));
        plan.placeAll(level, origin, Rotation.NONE);
        helper.assertTrue(plan.complete(level, origin, Rotation.NONE), "Nachbau unvollständig: " + mismatches(helper, plan, origin, Rotation.NONE));
        int doors = 0;
        int gold = 0;
        int spawners = 0;
        int chests = 0;
        String signText = "";
        net.minecraft.world.level.levelgen.structure.BoundingBox bounds = plan.worldBounds(origin, Rotation.NONE);
        for (BlockPos p : BlockPos.betweenClosed(bounds.minX(), bounds.minY(), bounds.minZ(), bounds.maxX(), bounds.maxY(), bounds.maxZ())) {
            BlockState s = level.getBlockState(p);
            if (s.getBlock() instanceof DoorBlock && s.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER) {
                doors++;
                helper.assertValueEqual(s.getValue(DoorBlock.FACING), Direction.SOUTH, "Tür nach dem Drehen");
            }
            gold += s.is(Blocks.GOLD_BLOCK) ? 1 : 0;
            spawners += s.is(Blocks.SPAWNER) ? 1 : 0;
            if (level.getBlockEntity(p) instanceof Container c) {
                chests++;
                helper.assertTrue(c.isEmpty(), "Truhe im Nachbau nicht leer");
            }
            if (level.getBlockEntity(p) instanceof SignBlockEntity be) {
                signText = be.getText(net.minecraft.world.level.block.entity.SignTextSlot.FRONT).getMessages(false).getFirst().getString();
            }
        }
        helper.assertValueEqual(doors, 1, "Türen im Nachbau");
        helper.assertValueEqual(gold, 1, "Goldblöcke im Nachbau");
        helper.assertValueEqual(spawners, 0, "Spawner im Nachbau");
        helper.assertValueEqual(chests, 1, "Truhen im Nachbau");
        helper.assertValueEqual(signText, "Zum Drachen", "Schild im Nachbau");
        // Bilder: Grundriss so breit wie der Plan, Ansicht so hoch
        PlanScan.Picture top = scan.top(40);
        PlanScan.Picture face = scan.front(40);
        helper.assertTrue(top.width() == 6 && top.height() == 7 && face.height() == 5, "Bildgrößen " + top + " " + face);
        helper.succeed();
    }

    /** Ein Keller unter der Bodenlinie wird ausgehoben, das Gelände daneben bleibt, wie es ist. */
    private static void basement(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TownGameTests.clearArea(helper);
        int g = TownGameTests.ground(helper);
        // drei Lagen Erde, darin ein Keller (3x3, zwei Lagen hoch), darüber ein Boden aus Brettern mit Steinmauer
        BlockPos a = helper.absolutePos(new BlockPos(6, g - 3, 6));
        BlockPos b = helper.absolutePos(new BlockPos(12, g + 2, 12));
        for (BlockPos p : BlockPos.betweenClosed(a, b)) {
            int dy = p.getY() - a.getY();
            int dx = p.getX() - a.getX();
            int dz = p.getZ() - a.getZ();
            BlockState state;
            if (dy < 3) {
                boolean cellar = dx >= 2 && dx <= 4 && dz >= 2 && dz <= 4 && dy >= 1;
                state = cellar ? Blocks.AIR.defaultBlockState() : Blocks.DIRT.defaultBlockState();
            } else if (dy == 3) {
                state = Blocks.OAK_PLANKS.defaultBlockState();
            } else {
                state = dx == 0 || dx == 6 || dz == 0 || dz == 6 ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.AIR.defaultBlockState();
            }
            level.setBlock(p, state, Block.UPDATE_CLIENTS);
        }
        // eine Leiter in den Keller (kein Gelände: kommt mit)
        level.setBlock(a.offset(2, 2, 3), Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING,
                Direction.EAST), Block.UPDATE_CLIENTS);
        helper.assertValueEqual(PlanScan.autoGround(level, a, b), 3, "Bodenlinie über drei Lagen Erde");
        PlanScan scan = PlanScan.scan(level, a, b, 3, Direction.NORTH, null);
        helper.assertValueEqual(scan.ground, 2, "Gelände unter dem Keller abgeschnitten");
        Blueprint plan = scan.toBlueprint();
        // anderswo: auf festem Boden aus Stein nachbauen
        BlockPos origin = helper.absolutePos(new BlockPos(30, g, 30));
        for (BlockPos p : BlockPos.betweenClosed(origin.offset(-5, -4, -5), origin.offset(5, -1, 5))) {
            level.setBlock(p, Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        plan.placeAll(level, origin, Rotation.NONE);
        // der Keller ist frei, die Erde drumherum nicht gebaut (dort bleibt Stein)
        BlockPos cellar = origin.offset(0, -1, 0);
        helper.assertTrue(level.getBlockState(cellar).isAir() && level.getBlockState(cellar.below()).isAir(), "Keller ausgehoben");
        helper.assertTrue(level.getBlockState(origin.offset(-3, -2, -3)).is(Blocks.STONE), "Gelände unter dem Fundament verändert: "
                + level.getBlockState(origin.offset(-3, -2, -3)));
        helper.assertTrue(level.getBlockState(origin.offset(-5, -1, -5)).is(Blocks.STONE), "Gelände neben dem Bau verändert");
        helper.assertTrue(level.getBlockState(origin).is(Blocks.OAK_PLANKS) || level.getBlockState(origin).isAir(), "Boden über dem Keller");
        helper.assertTrue(level.getBlockState(origin.offset(-1, -1, 0)).is(Blocks.LADDER), "Leiter im Keller: "
                + level.getBlockState(origin.offset(-1, -1, 0)));
        helper.succeed();
    }

    /** Was am Nachbau fehlt (die ersten Abweichungen). */
    static String mismatches(GameTestHelper helper, Blueprint plan, BlockPos origin, Rotation rotation) {
        StringBuilder out = new StringBuilder();
        int n = 0;
        for (Blueprint.Entry e : plan.blocks()) {
            BlockPos at = Blueprint.world(origin, rotation, e.pos());
            BlockState want = Blueprint.world(e.state(), rotation);
            BlockState have = helper.getLevel().getBlockState(at);
            if (have.getBlock() != want.getBlock() && !(want.getBlock() instanceof CropBlock)) {
                if (n++ < 6) {
                    out.append(helper.relativePos(at)).append(" soll ").append(want).append(" ist ").append(have).append("; ");
                }
            }
        }
        return n + " Abweichungen: " + out;
    }

    // =====================================================================================
    // Nachbau durch den Baumeister
    // =====================================================================================

    /**
     * Ein vermessenes Haus wird als eigener Bauplan gespeichert, bezahlt und abschnittsweise gebaut. Der Goldblock fehlt
     * im Lager: der Baumeister wartet auf ihn; liegt er in der Truhe, baut er fertig. Das fertige Haus zählt als Gebäude
     * der Siedlung, sein Bett als Bett.
     */
    private static void build(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        TownGameTests.clearTowns(server);
        TownGameTests.clearArea(helper);
        PlanLibrary.clearCache();
        helper.setTime(1000);
        ServerPlayer owner = TownGameTests.player(helper, new BlockPos(24, 1, 40));
        Town town = TownGameTests.found(helper, owner, new BlockPos(24, 1, 34));
        BlockPos[] box = house(helper, 4, 2);
        int ground = PlanScan.autoGround(level, box[0], box[1]);
        Direction front = PlanScan.doorSide(level, box[0], box[1], ground, null);
        PlanScan scan = PlanScan.scan(level, box[0], box[1], ground, front, level.registryAccess());
        PlanInfo info;
        try {
            info = PlanLibrary.of(server).add(server, scan, "Testhaus", owner.getUUID(), "Tester");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        helper.assertTrue(PlanLibrary.of(server).byAuthor(owner.getUUID()).contains(info), "Plan im Verzeichnis");
        // der Gegenstand trägt den Plan
        ItemStack paper = BlueprintItem.of(info);
        helper.assertValueEqual(BlueprintItem.type(paper), BuildingType.CUSTOM, "Bauplan-Gegenstand");
        int g = TownGameTests.ground(helper);
        BlockPos origin = helper.absolutePos(new BlockPos(24, g, 16));
        helper.assertValueEqual(TownManager.placeCustom(owner, info, origin, Rotation.NONE), TownManager.PlaceResult.NOT_ENOUGH, "Ohne Geld");
        Coins.give(owner, 1000);
        int before = Coins.total(owner);
        helper.assertValueEqual(TownManager.placeCustom(owner, info, origin, Rotation.NONE), TownManager.PlaceResult.PLACED, "Baustelle");
        helper.assertValueEqual(Coins.total(owner), before - Reputation.discounted(info.price(), Reputation.rank(owner)), "Preis");
        Town.Project project = town.projects().getFirst();
        helper.assertTrue(project.custom != null && project.custom.equals(info.id()), "Baustelle nach eigenem Plan");
        project.fast = true;
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.HIRED, "Baumeister");
        // Baustoffe ins Lager, aber ohne den Goldblock
        BlockPos chest = helper.absolutePos(new BlockPos(28, g, 34));
        TownStorage.supply(level, chest, info.costs().unitArray());
        boolean[] waited = {false};
        boolean[] supplied = {false};
        helper.onEachTick(() -> {
            if (!town.projects().isEmpty() && town.projects().getFirst().waitingItem == Items.GOLD_BLOCK && !supplied[0]) {
                waited[0] = true;
                if (helper.getTick() % 40 == 0) {
                    supplied[0] = true;
                    TownStorage.store(level, town, new ItemStack(Items.GOLD_BLOCK), chest);
                }
            }
        });
        helper.succeedWhen(() -> {
            if (!town.projects().isEmpty()) {
                Town.Project open = town.projects().getFirst();
                helper.assertTrue(false, "Noch im Bau: " + open.progress + " von " + open.steps().size() + " Schritten"
                        + (open.waitingItem != null ? " (wartet auf " + open.waitingItem + ")" : "")
                        + (open.waiting != null ? " (wartet auf " + open.waiting + ")" : ""));
            }
            helper.assertTrue(waited[0], "Der Baumeister hat nie auf den Goldblock gewartet");
            helper.assertValueEqual(town.count(BuildingType.CUSTOM), 1, "Fertige eigene Gebäude");
            Town.Building building = town.buildings().getLast();
            helper.assertTrue(building.custom().isPresent() && building.custom().get().equals(info.id()), "Gebäude kennt seinen Plan");
            helper.assertTrue(building.plan().complete(level, origin, Rotation.NONE), "Haus unvollständig: "
                    + mismatches(helper, building.plan(), origin, Rotation.NONE));
            helper.assertValueEqual(town.beds().size(), 1, "Bett im eigenen Haus zählt");
            KingdomsAtWar.LOGGER.info("[Test] Eigenes Haus abschnittsweise gebaut nach {} Ticks ({} Schritte)", helper.getTick(),
                    building.plan().blocks().size());
            TownGameTests.clearTowns(server);
            TownGameTests.leave(helper, owner);
        });
    }
}
