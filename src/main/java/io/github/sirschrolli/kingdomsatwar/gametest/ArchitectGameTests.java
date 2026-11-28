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
import io.github.sirschrolli.kingdomsatwar.economy.town.SiteSections;
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
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_FORMATS =
            ModGameTests.TEST_FUNCTIONS.register("architect_formats", () -> ArchitectGameTests::formats);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_SAMPLES =
            ModGameTests.TEST_FUNCTIONS.register("architect_samples", () -> ArchitectGameTests::samples);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_STORE =
            ModGameTests.TEST_FUNCTIONS.register("architect_store", () -> ArchitectGameTests::store);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_SECTIONS =
            ModGameTests.TEST_FUNCTIONS.register("architect_sections", () -> ArchitectGameTests::sections);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_GREAT_SITE =
            ModGameTests.TEST_FUNCTIONS.register("architect_great_site", () -> ArchitectGameTests::greatSite);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARCHITECT_ROYAL =
            ModGameTests.TEST_FUNCTIONS.register("architect_royal", () -> ArchitectGameTests::royal);

    private ArchitectGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event) {
        own(event, ARCHITECT_RULES, "architect_rules", 100);
        own(event, ARCHITECT_SCAN, "architect_scan", 200);
        own(event, ARCHITECT_BASEMENT, "architect_basement", 200);
        own(event, ARCHITECT_BUILD, "architect_build", 9000);
        own(event, ARCHITECT_FORMATS, "architect_formats", 200);
        own(event, ARCHITECT_SAMPLES, "architect_samples", 200);
        own(event, ARCHITECT_STORE, "architect_store", 200);
        own(event, ARCHITECT_SECTIONS, "architect_sections", 200);
        own(event, ARCHITECT_GREAT_SITE, "architect_great_site", 12000);
        own(event, ARCHITECT_ROYAL, "architect_royal", 400);
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
    // Schematiken: .litematic, .schem, .nbt
    // =====================================================================================

    /**
     * Ein vermessenes Haus geht als .litematic, .schem und .nbt hinaus und kommt genau so wieder herein (Blöcke, Drehung,
     * Schildtext); dazu eine Strukturdatei, die das Spiel selbst mit dem Strukturblock-Code geschrieben hat.
     */
    private static void formats(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        TownGameTests.clearArea(helper);
        BlockPos[] box = house(helper, 6, 4);
        // unter der Bodenlinie: ein Fundament unter einer Wand und ein Hohlraum unter dem Boden (wird ausgehoben)
        for (int dx = 0; dx <= 6; dx++) {
            level.setBlock(box[0].offset(dx, 0, 0), Blocks.COBBLESTONE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        level.setBlock(box[0].offset(3, 0, 3), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        int ground = PlanScan.autoGround(level, box[0], box[1]);
        Direction door = PlanScan.doorSide(level, box[0], box[1], ground, null);
        PlanScan scan = PlanScan.scan(level, box[0], box[1], ground, door, level.registryAccess());
        helper.assertValueEqual(scan.ground, 1, "Bodenlinie über dem Fundament");
        for (io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format format
                : io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format.values()) {
            CompoundTag tag = switch (format) {
                case LITEMATIC -> io.github.sirschrolli.kingdomsatwar.architect.io.SchematicWriter.litematic(scan, "Testhaus", "Tester");
                case SCHEM -> io.github.sirschrolli.kingdomsatwar.architect.io.SchematicWriter.sponge(scan, "Testhaus", "Tester");
                case NBT -> io.github.sirschrolli.kingdomsatwar.architect.io.SchematicWriter.structure(scan, "Tester");
            };
            try {
                // wie auf der Platte: gepackt schreiben und wieder lesen
                java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
                net.minecraft.nbt.NbtIo.writeCompressed(tag, bytes);
                CompoundTag back = net.minecraft.nbt.NbtIo.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()),
                        net.minecraft.nbt.NbtAccounter.unlimitedHeap());
                var result = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.read(back, format, level.registryAccess());
                helper.assertValueEqual(result.unknown(), 0, format + ": unbekannte Blöcke");
                helper.assertValueEqual(result.ground(), scan.ground, format + ": Bodenlinie in der Datei");
                PlanScan again = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(result.volume(), result.ground(),
                        level.registryAccess());
                helper.assertTrue(again.sizeX == scan.sizeX && again.sizeY == scan.sizeY && again.sizeZ == scan.sizeZ && again.ground == scan.ground,
                        format + ": Größe " + again.sizeX + "x" + again.sizeY + "x" + again.sizeZ + ", Boden " + again.ground);
                // genau gleich: Unberührtes bleibt unberührt, Ausgehobenes wird ausgehoben, alles andere gebaut
                for (int y = 0; y < scan.sizeY; y++) {
                    for (int z = 0; z < scan.sizeZ; z++) {
                        for (int x = 0; x < scan.sizeX; x++) {
                            BlockState want = scan.get(x, y, z);
                            BlockState have = again.get(x, y, z);
                            boolean same = want == null ? have == null : want.equals(have);
                            helper.assertTrue(same, format + ": bei " + x + "," + y + "," + z + " soll " + want + " ist " + have);
                        }
                    }
                }
                helper.assertTrue(signText(again).equals("Zum Drachen"), format + ": Schild " + signText(again));
                // ohne die Angabe des Architekten (fremde Werkzeuge verlieren sie) findet er die Bodenlinie selbst
                if (format != io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format.NBT) {
                    CompoundTag stripped = back.copy();
                    CompoundTag holder = format == io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format.LITEMATIC
                            ? stripped.getCompoundOrEmpty("Metadata") : stripped.getCompoundOrEmpty("Schematic").getCompoundOrEmpty("Metadata");
                    holder.remove("KingdomsAtWar");
                    var plain = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.read(stripped, format, level.registryAccess());
                    helper.assertValueEqual(plain.ground(), -1, format + ": Angabe entfernt");
                    PlanScan guessed = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(plain.volume(), level.registryAccess());
                    helper.assertTrue(guessed.ground == scan.ground && guessed.sizeY == scan.sizeY,
                            format + ": Bodenlinie ohne Angabe " + guessed.ground + ", Höhe " + guessed.sizeY);
                }
            } catch (java.io.IOException e) {
                throw new IllegalStateException(format + ": " + e.getMessage(), e);
            }
        }
        // eine Strukturdatei aus dem Code des Strukturblocks
        net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate template =
                new net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate();
        template.fillFromWorld(level, box[0], box[1].subtract(box[0]).offset(1, 1, 1), false, java.util.List.of());
        CompoundTag vanilla = template.save(new CompoundTag());
        try {
            var result = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.read(vanilla,
                    io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format.NBT, level.registryAccess());
            PlanScan fromStructure = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(result.volume(), level.registryAccess());
            helper.assertTrue(fromStructure.sizeX == scan.sizeX && fromStructure.sizeY == scan.sizeY && fromStructure.sizeZ == scan.sizeZ
                    && fromStructure.ground == scan.ground, "Strukturdatei: Größe/Bodenlinie anders");
            helper.assertValueEqual(fromStructure.costs().precious().getOrDefault(Items.GOLD_BLOCK, 0), 1, "Strukturdatei: Goldblock");
            helper.assertValueEqual(signText(fromStructure), "Zum Drachen", "Strukturdatei: Schild");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        helper.succeed();
    }

    /** Der Text des ersten Schilds in einem Plan. */
    private static String signText(PlanScan scan) {
        for (int y = 0; y < scan.sizeY; y++) {
            for (int z = 0; z < scan.sizeZ; z++) {
                for (int x = 0; x < scan.sizeX; x++) {
                    CompoundTag tag = scan.blockEntity(x, y, z);
                    if (tag != null && tag.contains("front_text")) {
                        return tag.toString().contains("Zum Drachen") ? "Zum Drachen" : tag.toString();
                    }
                }
            }
        }
        return "";
    }

    /**
     * Echte Dateien anderer Werkzeuge (nur bei der Entwicklung: Ordner in {@code -Dkingdomsatwar.schematicSamples}):
     * jede lässt sich lesen und vermessen, ohne unbekannte Blöcke.
     */
    private static void samples(GameTestHelper helper) {
        String dir = System.getProperty("kingdomsatwar.schematicSamples", "");
        if (dir.isEmpty()) {
            helper.succeed();
            return;
        }
        ServerLevel level = helper.getLevel();
        java.util.List<String> report = new java.util.ArrayList<>();
        try (var walk = java.nio.file.Files.walk(java.nio.file.Path.of(dir), 3)) {
            for (java.nio.file.Path file : (Iterable<java.nio.file.Path>) walk::iterator) {
                if (io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format.of(file.getFileName().toString()) == null
                        || !java.nio.file.Files.isRegularFile(file)) {
                    continue;
                }
                var result = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.read(file, level.registryAccess());
                String line = file.getFileName() + ": " + result.volume().sizeX + "x" + result.volume().sizeY + "x" + result.volume().sizeZ
                        + ", " + result.volume().blocks() + " Blöcke, unbekannt " + result.unknown();
                try {
                    PlanScan plan = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(result.volume(), level.registryAccess());
                    line += " → Plan " + plan.sizeX + "x" + plan.sizeY + "x" + plan.sizeZ + " (Boden " + plan.ground + "), "
                            + plan.costs().blocks() + " Blöcke" + (plan.skipped().isEmpty() ? "" : ", weggelassen " + plan.skipped());
                } catch (IllegalArgumentException e) {
                    line += " → nichts zu bauen";
                }
                report.add(line);
                KingdomsAtWar.LOGGER.info("[Test] Schematik {}", line);
                helper.assertValueEqual(result.unknown(), 0, file.getFileName() + ": unbekannte Blöcke");
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        helper.assertTrue(!report.isEmpty(), "Keine Beispieldateien in " + dir);
        helper.succeed();
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

    // =====================================================================================
    // Große Bauten (Teil 3)
    // =====================================================================================

    /**
     * Eine kleine Burg als Volumen (44 × 10 × 40 mit einer Lage Gelände darunter): Ringmauer mit Tor und Zinnen auf einem
     * Fundament, vier Türme mit Bretterboden, Tür und Leiter, ein Bergfried mit Dach, Fenstern, Laternen, Bett und Schild,
     * Fackeln an der Mauer, ein Becken mit Wasser, ein Kiesweg und ein kleiner Keller unter dem Bergfried.
     */
    static io.github.sirschrolli.kingdomsatwar.architect.io.Volume testCastle(ServerLevel level) {
        int sx = 44;
        int sy = 10;
        int sz = 40;
        var v = new io.github.sirschrolli.kingdomsatwar.architect.io.Volume(sx, sy, sz);
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int z = 0; z < sz; z++) {
            for (int x = 0; x < sx; x++) {
                boolean edge = x == 0 || x == sx - 1 || z == 0 || z == sz - 1;
                if (edge) {
                    v.set(x, 0, z, Blocks.STONE_BRICKS.defaultBlockState()); // Fundament unter der Mauer
                    for (int y = 1; y <= 5; y++) {
                        if (!(z == 0 && x >= 20 && x <= 23 && y <= 3)) {
                            v.set(x, y, z, Blocks.COBBLESTONE.defaultBlockState());
                        }
                    }
                    if ((x + z) % 2 == 0) {
                        v.set(x, 6, z, Blocks.COBBLESTONE.defaultBlockState());
                    }
                }
            }
        }
        // Türme
        int[][] corners = {{0, 0}, {sx - 5, 0}, {0, sz - 5}, {sx - 5, sz - 5}};
        for (int[] c : corners) {
            int tx = c[0];
            int tz = c[1];
            boolean west = tx < sx / 2;
            for (int y = 1; y <= 8; y++) {
                for (int dz = 0; dz < 5; dz++) {
                    for (int dx = 0; dx < 5; dx++) {
                        boolean ring = dx == 0 || dx == 4 || dz == 0 || dz == 4;
                        v.set(tx + dx, y, tz + dz, ring ? Blocks.STONE_BRICKS.defaultBlockState() : y == 1 ? Blocks.OAK_PLANKS.defaultBlockState() : air);
                    }
                }
            }
            Direction out = west ? Direction.EAST : Direction.WEST;
            int doorX = west ? tx + 4 : tx;
            v.set(doorX, 2, tz + 2, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, out));
            v.set(doorX, 3, tz + 2, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, out).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
            int ladderX = west ? tx + 1 : tx + 3;
            for (int y = 2; y <= 7; y++) {
                v.set(ladderX, y, tz + 2, Blocks.LADDER.defaultBlockState().setValue(net.minecraft.world.level.block.LadderBlock.FACING, out));
            }
        }
        // Fackeln an der Innenseite der Mauer
        for (int x = 8; x <= sx - 9; x += 6) {
            if (x < 19 || x > 24) {
                v.set(x, 4, 1, Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.SOUTH));
            }
            v.set(x, 4, sz - 2, Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.NORTH));
        }
        for (int z = 8; z <= sz - 9; z += 6) {
            v.set(1, 4, z, Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.EAST));
            v.set(sx - 2, 4, z, Blocks.WALL_TORCH.defaultBlockState().setValue(net.minecraft.world.level.block.WallTorchBlock.FACING, Direction.WEST));
        }
        // Bergfried mit Keller
        for (int z = 18; z <= 21; z++) {
            for (int x = 19; x <= 24; x++) {
                v.set(x, 0, z, air);
            }
        }
        for (int y = 1; y <= 8; y++) {
            for (int z = 15; z <= 24; z++) {
                for (int x = 17; x <= 26; x++) {
                    boolean ring = x == 17 || x == 26 || z == 15 || z == 24;
                    BlockState state = y == 8 ? Blocks.OAK_PLANKS.defaultBlockState() : ring ? Blocks.STONE_BRICKS.defaultBlockState()
                            : y == 1 ? Blocks.OAK_PLANKS.defaultBlockState() : air;
                    v.set(x, y, z, state);
                }
            }
        }
        v.set(21, 2, 15, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH));
        v.set(21, 3, 15, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.FACING, Direction.NORTH).setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER));
        for (int k = 0; k < 2; k++) {
            v.set(23 + k, 4, 15, Blocks.GLASS_PANE.defaultBlockState());
            v.set(21 + k, 4, 24, Blocks.GLASS_PANE.defaultBlockState());
            v.set(17, 4, 19 + k, Blocks.GLASS_PANE.defaultBlockState());
            v.set(26, 4, 19 + k, Blocks.GLASS_PANE.defaultBlockState());
        }
        v.set(19, 7, 17, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        v.set(24, 7, 22, Blocks.LANTERN.defaultBlockState().setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true));
        v.set(19, 2, 22, Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH));
        v.set(19, 2, 21, Blocks.BED.red().defaultBlockState().setValue(BedBlock.FACING, Direction.NORTH).setValue(BedBlock.PART, BedPart.HEAD));
        BlockPos signPos = new BlockPos(24, 4, 14);
        BlockState sign = Blocks.OAK_WALL_SIGN.defaultBlockState().setValue(WallSignBlock.FACING, Direction.NORTH);
        v.set(signPos.getX(), signPos.getY(), signPos.getZ(), sign);
        SignBlockEntity be = new SignBlockEntity(signPos, sign);
        be.setLevel(level); // nur, damit das Schild seinen Text annimmt (es steht nirgends)
        be.updateText(text -> text.asMutable().setLine(0, Component.literal("Testburg")).asImmutable(),
                net.minecraft.world.level.block.entity.SignTextSlot.FRONT);
        v.setBlockEntity(signPos, be);
        // Kiesweg vom Tor zum Bergfried, Becken mit Wasser
        for (int z = 1; z <= 14; z++) {
            for (int x = 20; x <= 23; x++) {
                v.set(x, 1, z, Blocks.GRAVEL.defaultBlockState());
            }
        }
        for (int z = 28; z <= 32; z++) {
            for (int x = 30; x <= 34; x++) {
                boolean rim = x == 30 || x == 34 || z == 28 || z == 32;
                v.set(x, 1, z, rim ? Blocks.STONE_BRICKS.defaultBlockState() : Blocks.WATER.defaultBlockState());
            }
        }
        return v;
    }

    /** Hindernisse auf dem Bauplatz (Steinsäulen, Laub), die der Baumeister wegräumen muss; gibt ihre Stellen zurück. */
    private static java.util.List<BlockPos> obstacles(GameTestHelper helper, int g) {
        java.util.List<BlockPos> list = new java.util.ArrayList<>();
        int[][] at = {{8, 10}, {30, 12}, {12, 34}, {34, 36}};
        for (int[] a : at) {
            for (int y = 0; y < 4; y++) {
                BlockPos p = helper.absolutePos(new BlockPos(a[0], g + y, a[1]));
                helper.getLevel().setBlock(p, (y == 3 ? Blocks.OAK_LEAVES : Blocks.STONE).defaultBlockState(), Block.UPDATE_CLIENTS);
                list.add(p);
            }
        }
        return list;
    }

    private static PlanInfo addPlan(MinecraftServer server, PlanScan scan, String name, java.util.UUID author) {
        try {
            return PlanLibrary.of(server).add(server, scan, name, author, "Tester");
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Die eingebaute Königsburg: 300 × 300 Blöcke, vier Lagen unter der Bodenlinie (der Graben), nichts, was der Architekt
     * nicht übernimmt, keine kostbaren Blöcke; Scheiben und Mauern mit ihren Nachbarn verbunden; immer gleich entworfen;
     * als .litematic hinaus und wieder herein genau dieselbe Burg; in der Liste zum Einlesen ganz oben. Mit
     * {@code -Dkingdomsatwar.castleExport=<Ordner>} legt der Test sie dort als .litematic, .schem und .nbt ab.
     */
    private static void royal(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        long t0 = System.nanoTime();
        var castle = io.github.sirschrolli.kingdomsatwar.architect.castle.CastleGenerator.royalCastle();
        var v = castle.volume();
        long ms = (System.nanoTime() - t0) / 1_000_000;
        helper.assertTrue(v.sizeX == 300 && v.sizeZ == 300 && castle.ground() == 4,
                "Königsburg: " + v.sizeX + "x" + v.sizeY + "x" + v.sizeZ + ", Boden " + castle.ground());
        long t1 = System.nanoTime();
        PlanScan scan = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(v, castle.ground(), level.registryAccess());
        long scanMs = (System.nanoTime() - t1) / 1_000_000;
        helper.assertTrue(scan.sizeX == 300 && scan.sizeZ == 300 && scan.ground == 4 && scan.sizeY <= PlanScan.MAX_HEIGHT,
                "Plan: " + scan.sizeX + "x" + scan.sizeY + "x" + scan.sizeZ + ", Boden " + scan.ground);
        helper.assertTrue(scan.skipped().isEmpty(), "Nicht übernommen: " + scan.skipped());
        PlanScan.Costs costs = scan.costs();
        // nur der Thron im Palas muss im Lager liegen, alles andere bauen die Baumeister aus Holz, Stein und Metall
        helper.assertTrue(costs.precious().size() == 1 && costs.precious().getOrDefault(
                io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.THRONE.get().asItem(), 0) == 1, "Kostbare Blöcke: " + costs.precious());
        helper.assertTrue(costs.blocks() > 150_000, "Zu wenige Blöcke: " + costs.blocks());
        // verbunden wie in der Welt: Scheiben und Mauern
        int panes = 0;
        int panesJoined = 0;
        int walls = 0;
        int wallsJoined = 0;
        java.util.Map<net.minecraft.world.level.block.Block, Integer> kinds = new java.util.HashMap<>();
        for (int y = 0; y < scan.sizeY; y++) {
            for (int z = 0; z < scan.sizeZ; z++) {
                for (int x = 0; x < scan.sizeX; x++) {
                    BlockState s = scan.get(x, y, z);
                    if (s == null || s.isAir()) {
                        continue;
                    }
                    kinds.merge(s.getBlock(), 1, Integer::sum);
                    if (s.getBlock() instanceof net.minecraft.world.level.block.IronBarsBlock) {
                        panes++;
                        if (s.getValue(net.minecraft.world.level.block.CrossCollisionBlock.NORTH) || s.getValue(net.minecraft.world.level.block.CrossCollisionBlock.SOUTH)
                                || s.getValue(net.minecraft.world.level.block.CrossCollisionBlock.EAST)
                                || s.getValue(net.minecraft.world.level.block.CrossCollisionBlock.WEST)) {
                            panesJoined++;
                        }
                    } else if (s.getBlock() instanceof net.minecraft.world.level.block.WallBlock) {
                        walls++;
                        if (s.getValue(net.minecraft.world.level.block.WallBlock.NORTH) != net.minecraft.world.level.block.state.properties.WallSide.NONE
                                || s.getValue(net.minecraft.world.level.block.WallBlock.EAST) != net.minecraft.world.level.block.state.properties.WallSide.NONE) {
                            wallsJoined++;
                        }
                    }
                }
            }
        }
        helper.assertTrue(panes > 100 && panesJoined * 10 >= panes * 9, "Scheiben verbunden: " + panesJoined + " von " + panes);
        helper.assertTrue(walls > 1000 && wallsJoined * 10 >= walls * 8, "Mauern verbunden: " + wallsJoined + " von " + walls);
        for (net.minecraft.world.level.block.Block b : new net.minecraft.world.level.block.Block[] {Blocks.IRON_CHAIN, Blocks.PACKED_MUD, Blocks.WATER,
                Blocks.LECTERN, Blocks.DEEPSLATE_TILE_STAIRS}) {
            helper.assertTrue(kinds.getOrDefault(b, 0) > 0, "Fehlt in der Königsburg: " + b);
        }
        // immer gleich entworfen
        PlanScan twice = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(
                io.github.sirschrolli.kingdomsatwar.architect.castle.CastleGenerator.royalCastle().volume(), castle.ground(), level.registryAccess());
        helper.assertTrue(sameCells(scan, twice), "Zweimal entworfen: verschieden");
        // als .litematic hinaus und wieder herein
        try {
            CompoundTag tag = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicWriter.litematic(scan, "Königsburg", "Dragon Architect");
            java.io.ByteArrayOutputStream bytes = new java.io.ByteArrayOutputStream();
            net.minecraft.nbt.NbtIo.writeCompressed(tag, bytes);
            CompoundTag back = net.minecraft.nbt.NbtIo.readCompressed(new java.io.ByteArrayInputStream(bytes.toByteArray()),
                    net.minecraft.nbt.NbtAccounter.unlimitedHeap());
            var result = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.read(back,
                    io.github.sirschrolli.kingdomsatwar.architect.io.SchematicReader.Format.LITEMATIC, level.registryAccess());
            helper.assertValueEqual(result.unknown(), 0, "Unbekannte Blöcke in der .litematic");
            PlanScan again = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(result.volume(), result.ground(),
                    level.registryAccess());
            helper.assertTrue(sameCells(scan, again), "Königsburg aus der .litematic: anders");
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("Königsburg: {} ms entworfen, {} ms vermessen, {} Blöcke, {} Lagen, .litematic {} KB",
                    ms, scanMs, costs.blocks(), scan.sizeY, bytes.size() / 1024);
            String export = System.getProperty("kingdomsatwar.castleExport");
            if (export != null && !export.isBlank()) {
                io.github.sirschrolli.kingdomsatwar.architect.io.SchematicWriter.writeAll(scan, "Königsburg", "Dragon Architect",
                        java.nio.file.Path.of(export));
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Königsburg: " + e.getMessage(), e);
        }
        String map = System.getProperty("kingdomsatwar.castleMap");
        if (map != null && !map.isBlank()) {
            writeMap(scan, java.nio.file.Path.of(map));
        }
        // ganz oben in der Liste zum Einlesen
        var list = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.list(level.getServer());
        helper.assertTrue(!list.isEmpty() && list.getFirst().id().equals(io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.ROYAL_CASTLE),
                "Die Königsburg steht nicht oben in der Liste");
        helper.succeed();
    }

    /** Für die Gestaltung: eine Karte des Plans von oben (Farbe des obersten Blocks, nach Höhe schattiert) als PPM-Bild. */
    private static void writeMap(PlanScan scan, java.nio.file.Path file) {
        byte[] rgb = new byte[scan.sizeX * scan.sizeZ * 3];
        for (int z = 0; z < scan.sizeZ; z++) {
            for (int x = 0; x < scan.sizeX; x++) {
                int color = 0x6A8F3A;
                int height = scan.ground - 1;
                for (int y = scan.sizeY - 1; y >= 0; y--) {
                    BlockState s = scan.get(x, y, z);
                    if (s != null && !s.isAir()) {
                        var mc = s.getMapColor(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
                        if (mc != net.minecraft.world.level.material.MapColor.NONE) {
                            color = mc.col;
                            height = y;
                            break;
                        }
                    }
                }
                double shade = 0.55 + 0.45 * Math.min(1.0, Math.max(0.0, (height - scan.ground + 4) / 40.0));
                int i = (z * scan.sizeX + x) * 3;
                rgb[i] = (byte) Math.min(255, (int) (((color >> 16) & 255) * shade));
                rgb[i + 1] = (byte) Math.min(255, (int) (((color >> 8) & 255) * shade));
                rgb[i + 2] = (byte) Math.min(255, (int) ((color & 255) * shade));
            }
        }
        try (java.io.OutputStream out = java.nio.file.Files.newOutputStream(file)) {
            out.write(("P6\n" + scan.sizeX + " " + scan.sizeZ + "\n255\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII));
            out.write(rgb);
        } catch (java.io.IOException e) {
            throw new IllegalStateException("Karte: " + e.getMessage(), e);
        }
    }

    private static boolean sameCells(PlanScan a, PlanScan b) {
        if (a.sizeX != b.sizeX || a.sizeY != b.sizeY || a.sizeZ != b.sizeZ || a.ground != b.ground) {
            return false;
        }
        for (int y = 0; y < a.sizeY; y++) {
            for (int z = 0; z < a.sizeZ; z++) {
                for (int x = 0; x < a.sizeX; x++) {
                    if (!java.util.Objects.equals(a.get(x, y, z), b.get(x, y, z))) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /**
     * Ein großer Plan (200 × 6 × 180, eine Ringmauer mit Türmen): Lage für Lage vermessen wie auf einmal, gepackt gespeichert
     * und wieder geladen (auch in der alten Fassung 1), der Grundriss zeigt die schmale Mauer trotz Verkleinerung. Dazu die
     * Regeln für den Bauplatz einer großen Burg: sie muss das Gebiet der Siedlung berühren und darf das Banner nicht
     * überbauen.
     */
    private static void store(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        TownGameTests.clearTowns(server);
        PlanLibrary.clearCache();
        int sx = 200;
        int sz = 180;
        var v = new io.github.sirschrolli.kingdomsatwar.architect.io.Volume(sx, 6, sz);
        for (int z = 0; z < sz; z++) {
            for (int x = 0; x < sx; x++) {
                boolean edge = x == 0 || x == sx - 1 || z == 0 || z == sz - 1;
                boolean tower = (x < 6 || x >= sx - 6) && (z < 6 || z >= sz - 6);
                if (edge || tower) {
                    for (int y = 1; y <= (tower ? 5 : 4); y++) {
                        v.set(x, y, z, (tower ? Blocks.STONE_BRICKS : Blocks.COBBLESTONE).defaultBlockState());
                    }
                }
            }
        }
        PlanScan scan = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(v, 1, level.registryAccess());
        // ohne Fundament bleibt von der Lage Gelände nichts übrig: die Bodenlinie liegt dann ganz unten
        helper.assertTrue(scan.sizeX == sx && scan.sizeY == 5 && scan.sizeZ == sz && scan.ground == 0,
                "Großer Plan: " + scan.sizeX + "x" + scan.sizeY + "x" + scan.sizeZ + ", Boden " + scan.ground);
        // Lage für Lage
        PlanScan.Scanner scanner = new PlanScan.Scanner(v, BlockPos.ZERO, new BlockPos(sx - 1, 5, sz - 1), 1, Direction.NORTH, level.registryAccess());
        int steps = 0;
        while (!scanner.step(5000)) {
            steps++;
        }
        helper.assertTrue(steps >= 3, "Lage für Lage: " + steps + " Schritte");
        helper.assertTrue(sameCells(scanner.finish(), scan), "Lage für Lage vermessen ergibt einen anderen Plan");
        // gepackt speichern und laden
        CompoundTag tag = scan.save();
        helper.assertValueEqual(tag.getIntOr("Version", 0), 2, "Dateifassung");
        helper.assertTrue(sameCells(PlanScan.load(tag), scan), "Gepackt gespeichert und geladen: anders");
        // die alte Fassung 1 (eine Zahl je Stelle)
        net.minecraft.nbt.ListTag palette = tag.getListOrEmpty("Palette");
        java.util.Map<BlockState, Integer> index = new java.util.HashMap<>();
        for (int i = 1; i < palette.size(); i++) {
            index.put(net.minecraft.nbt.NbtUtils.readBlockState(net.minecraft.core.registries.BuiltInRegistries.BLOCK, palette.getCompoundOrEmpty(i)), i);
        }
        int[] ints = new int[scan.sizeX * scan.sizeY * scan.sizeZ];
        for (int y = 0; y < scan.sizeY; y++) {
            for (int z = 0; z < scan.sizeZ; z++) {
                for (int x = 0; x < scan.sizeX; x++) {
                    BlockState st = scan.get(x, y, z);
                    ints[(y * scan.sizeZ + z) * scan.sizeX + x] = st == null ? 0 : index.get(st);
                }
            }
        }
        CompoundTag old = tag.copy();
        old.putInt("Version", 1);
        old.remove("Bits");
        old.putIntArray("Blocks", ints);
        helper.assertTrue(sameCells(PlanScan.load(old), scan), "Fassung 1 lässt sich nicht mehr lesen");
        // der Grundriss zeigt die Mauer auch verkleinert (jede Ecke und jeder Rand)
        PlanScan.Picture top = scan.top(PlanInfo.PICTURE_SIZE);
        helper.assertTrue(top.width() <= PlanInfo.PICTURE_SIZE && top.height() <= PlanInfo.PICTURE_SIZE, "Grundriss zu groß");
        for (int px = 0; px < top.width(); px++) {
            helper.assertTrue(top.argb(px, 0) != 0 && top.argb(px, top.height() - 1) != 0, "Mauer im Grundriss fehlt bei " + px);
        }
        // Bauplatz einer großen Burg
        ServerPlayer owner = TownGameTests.player(helper, new BlockPos(24, 1, 30));
        Town town = TownGameTests.found(helper, owner, new BlockPos(24, 1, 24));
        PlanInfo info = addPlan(server, scan, "Große Mauer", owner.getUUID());
        helper.assertTrue(SiteSections.large(info), "Großer Plan wird in Abschnitten gebaut");
        BlockPos banner = town.banner();
        BlockPos far = new BlockPos(banner.getX(), banner.getY(), banner.getZ() - 400);
        helper.assertValueEqual(TownManager.check(level, town, info, far, Rotation.NONE), TownManager.PlaceResult.OUTSIDE, "Weit weg");
        BlockPos over = new BlockPos(banner.getX(), banner.getY(), banner.getZ());
        helper.assertValueEqual(TownManager.check(level, town, info, over, Rotation.NONE), TownManager.PlaceResult.OVERLAP, "Über dem Banner");
        BlockPos next = new BlockPos(banner.getX(), banner.getY(), banner.getZ() - 90 - town.radius() + 8);
        TownManager.PlaceResult beside = TownManager.check(level, town, info, next, Rotation.NONE);
        helper.assertTrue(beside != TownManager.PlaceResult.OUTSIDE && beside != TownManager.PlaceResult.OVERLAP
                && beside != TownManager.PlaceResult.FOREIGN, "Neben der Siedlung: " + beside);
        TownGameTests.clearTowns(server);
        TownGameTests.leave(helper, owner);
        helper.succeed();
    }

    /**
     * Die Bauabschnitte einer Großbaustelle (die Testburg, um 90° gedreht): jeder Schritt liegt in seinem Abschnitt, zusammen
     * sind es genau die Schritte des ganzen Plans; Rohbau aller Abschnitte, dann alle Einzelheiten ergeben die Burg – mit
     * Hindernissen, die weggeräumt werden, Fackeln über die Grenzen der Abschnitte hinweg und dem Schild am Bergfried.
     */
    private static void sections(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        TownGameTests.clearTowns(server);
        TownGameTests.clearArea(helper);
        PlanLibrary.clearCache();
        int g = TownGameTests.ground(helper);
        PlanScan scan = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(testCastle(level), 1, level.registryAccess());
        helper.assertValueEqual(scan.ground, 1, "Bodenlinie der Testburg");
        PlanInfo info = addPlan(server, scan, "Testburg", java.util.UUID.randomUUID());
        helper.assertTrue(SiteSections.large(info), "Testburg wird in Abschnitten gebaut");
        java.util.List<BlockPos> obstacles = obstacles(helper, g);
        BlockPos origin = helper.absolutePos(new BlockPos(22, g, 23));
        Rotation rotation = Rotation.CLOCKWISE_90;
        Town.Project project = new Town.Project(BuildingType.CUSTOM, origin, rotation, 0, 0, info.id());
        helper.assertTrue(project.sectioned(), "Baustelle in Abschnitten");
        SiteSections site = project.site();
        helper.assertTrue(site != null && site.count() >= 6, "Abschnitte: " + (site == null ? 0 : site.count()));
        java.util.Map<BlockPos, BlockState> union = new java.util.HashMap<>();
        java.util.List<java.util.List<Blueprint.Step>> frames = new java.util.ArrayList<>();
        java.util.List<java.util.List<Blueprint.Step>> details = new java.util.ArrayList<>();
        int duplicates = 0;
        for (int i = 0; i < site.count(); i++) {
            var bounds = site.bounds(i);
            java.util.List<Blueprint.Step> frame = site.generate(level, i, 0);
            java.util.List<Blueprint.Step> detail = site.generate(level, i, 1);
            boolean built = false;
            for (Blueprint.Step step : frame) {
                helper.assertTrue(bounds.isInside(step.pos()), "Schritt " + helper.relativePos(step.pos()) + " außerhalb von Abschnitt " + i);
                // Rohbau: erst räumen, dann bauen (nie wieder räumen)
                if (step.state().isAir() && step.pos().getY() >= origin.getY()) {
                    helper.assertTrue(!built, "Abschnitt " + i + ": Räumen nach dem Bauen");
                } else {
                    built = true;
                }
            }
            for (Blueprint.Step step : detail) {
                helper.assertTrue(bounds.isInside(step.pos()), "Einzelheit außerhalb von Abschnitt " + i);
            }
            for (Blueprint.Step step : concat(frame, detail)) {
                if (!step.state().isAir() || step.pos().getY() < origin.getY()) {
                    if (union.put(step.pos(), step.state()) != null) {
                        duplicates++;
                    }
                }
            }
            frames.add(frame);
            details.add(detail);
        }
        helper.assertValueEqual(duplicates, 0, "Doppelte Schritte");
        Blueprint whole = scan.toBlueprint();
        java.util.Map<BlockPos, BlockState> classic = new java.util.HashMap<>();
        for (Blueprint.Step step : whole.steps(origin, rotation)) {
            if (!step.state().isAir() || step.pos().getY() < origin.getY()) {
                classic.put(step.pos(), step.state());
            }
        }
        helper.assertValueEqual(union.size(), classic.size(), "Schritte in Abschnitten und im ganzen Plan");
        for (java.util.Map.Entry<BlockPos, BlockState> e : classic.entrySet()) {
            helper.assertTrue(e.getValue().equals(union.get(e.getKey())), "Schritt bei " + helper.relativePos(e.getKey()) + ": soll " + e.getValue()
                    + " ist " + union.get(e.getKey()));
        }
        // bauen: erst überall der Rohbau, dann die Einzelheiten
        for (java.util.List<Blueprint.Step> frame : frames) {
            for (Blueprint.Step step : frame) {
                Blueprint.apply(level, step);
            }
        }
        for (java.util.List<Blueprint.Step> detail : details) {
            for (Blueprint.Step step : detail) {
                Blueprint.apply(level, step);
            }
        }
        helper.assertTrue(whole.complete(level, origin, rotation), "Burg unvollständig: " + mismatches(helper, whole, origin, rotation));
        for (BlockPos p : obstacles) {
            BlockState have = level.getBlockState(p);
            helper.assertTrue(have.isAir() || whole.blocks().stream().anyMatch(e -> Blueprint.world(origin, rotation, e.pos()).equals(p)),
                    "Hindernis nicht weggeräumt bei " + helper.relativePos(p) + ": " + have);
        }
        helper.assertTrue(level.getBlockState(Blueprint.world(origin, rotation, scan.local(30 + 2, 1, 28 + 2))).is(Blocks.WATER), "Wasser im Becken");
        boolean signed = false;
        for (BlockPos p : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, g, 0)), helper.absolutePos(new BlockPos(47, g + 6, 47)))) {
            if (level.getBlockEntity(p) instanceof SignBlockEntity sign) {
                signed |= sign.getText(net.minecraft.world.level.block.entity.SignTextSlot.FRONT).getMessages(false).getFirst().getString().equals("Testburg");
            }
        }
        helper.assertTrue(signed, "Schild am Bergfried ohne Text");
        helper.succeed();
    }

    private static <T> java.util.List<T> concat(java.util.List<T> a, java.util.List<T> b) {
        java.util.List<T> out = new java.util.ArrayList<>(a);
        out.addAll(b);
        return out;
    }

    /**
     * Eine Großbaustelle mit drei Baumeistern: jeder nimmt sich einen eigenen Abschnitt (zeitweise arbeiten mehrere
     * zugleich), am Ende steht die ganze Testburg, die Gerüste sind weg, alle Abschnitte fertig, und die Burg zählt als
     * Gebäude der Siedlung (mit ihrem Bett).
     */
    private static void greatSite(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        MinecraftServer server = level.getServer();
        TownGameTests.clearTowns(server);
        TownGameTests.clearArea(helper);
        PlanLibrary.clearCache();
        helper.setTime(1000);
        int g = TownGameTests.ground(helper);
        ServerPlayer owner = TownGameTests.player(helper, new BlockPos(45, 1, 12));
        Town town = TownGameTests.found(helper, owner, new BlockPos(45, 1, 4));
        PlanScan scan = io.github.sirschrolli.kingdomsatwar.architect.io.SchematicFiles.toScan(testCastle(level), 1, level.registryAccess());
        PlanInfo info = addPlan(server, scan, "Testburg", owner.getUUID());
        obstacles(helper, g);
        BlockPos origin = helper.absolutePos(new BlockPos(22, g, 23));
        Rotation rotation = Rotation.CLOCKWISE_90;
        Town.Project project = TownManager.startCustom(level, town, info, origin, rotation);
        helper.assertTrue(project.fast, "Große Baustellen gleich abschnittsweise");
        Coins.give(owner, 3000);
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.HIRED, "Baumeister");
        helper.assertValueEqual(TownManager.hire(owner, town, Profession.BUILDER), TownManager.HireResult.HIRED,
                "Zweiter Baumeister (für die Großbaustelle)");
        helper.assertTrue(TownManager.spawnCitizen(level, town, Profession.BUILDER) != null, "Dritter Baumeister");
        BlockPos chest = helper.absolutePos(new BlockPos(46, g, 7));
        int[] units = info.costs().unitArray();
        for (int i = 0; i < units.length; i++) {
            units[i] = units[i] * 2 + 64;
        }
        TownStorage.supply(level, chest, units);
        int[] most = {0};
        java.util.Set<Integer> seen = new java.util.HashSet<>();
        helper.onEachTick(() -> {
            SiteSections site = project.site();
            if (site != null && !town.projects().isEmpty()) {
                byte[] map = site.map(level.getGameTime());
                int busy = 0;
                for (int i = 0; i < map.length; i++) {
                    if (map[i] == 3) {
                        busy++;
                        seen.add(i);
                    }
                }
                most[0] = Math.max(most[0], busy);
                if (Boolean.getBoolean("kingdomsatwar.debugBuilder") && helper.getTick() % 200 == 0) {
                    StringBuilder line = new StringBuilder();
                    for (byte b : map) {
                        line.append(b);
                    }
                    KingdomsAtWar.LOGGER.info("[Test] Großbaustelle t={} Karte {} ({} Baumeister, {} %, wartet {})", helper.getTick(), line,
                            busy, Math.round(project.fraction() * 100), project.waiting);
                }
            }
        });
        helper.succeedWhen(() -> {
            if (!town.projects().isEmpty()) {
                SiteSections site = project.site();
                helper.assertTrue(false, "Noch im Bau: " + (site == null ? "?" : site.done() + " von " + site.count() + " Abschnitten")
                        + (project.waiting != null ? " (wartet auf " + project.waiting + ")" : ""));
            }
            helper.assertTrue(most[0] >= 2, "Nie zwei Baumeister zugleich an verschiedenen Abschnitten (höchstens " + most[0] + ")");
            helper.assertValueEqual(town.count(BuildingType.CUSTOM), 1, "Fertige Burg");
            Blueprint whole = scan.toBlueprint();
            helper.assertTrue(whole.complete(level, origin, rotation), "Burg unvollständig: " + mismatches(helper, whole, origin, rotation));
            int scaffolds = 0;
            for (BlockPos p : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(0, g, 0)), helper.absolutePos(new BlockPos(47, g + 16, 47)))) {
                if (level.getBlockState(p).is(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.BUILDER_SCAFFOLD.get())) {
                    scaffolds++;
                }
            }
            helper.assertValueEqual(scaffolds, 0, "Gerüste stehen noch");
            helper.assertValueEqual(town.beds().size(), 1, "Bett in der Burg zählt");
            KingdomsAtWar.LOGGER.info("[Test] Großbaustelle mit drei Baumeistern fertig nach {} Ticks ({} Abschnitte, höchstens {} zugleich)",
                    helper.getTick(), seen.size(), most[0]);
            TownGameTests.clearTowns(server);
            TownGameTests.leave(helper, owner);
        });
    }
}
