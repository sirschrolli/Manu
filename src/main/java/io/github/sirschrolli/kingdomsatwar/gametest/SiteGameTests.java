package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.StructureTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.RandomizableContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EndPortalFrameBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.BuiltinStructures;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der eigenen Bauwerke, die die Rolle der Vanilla-Bauwerke übernehmen: Im Sternensturz ergeben die zwölf Rahmen
 * mit Enderaugen das Portal, und der Wendelsteg führt begehbar hinab bis in die Kernkammer; die Wasserstollen haben
 * Rinnen voll Wasser, eine Zisterne mit Becken und Säulen, Treppen und Truhen; in den Gebeinen eines Seedrachen liegen
 * drei Truhen (im gesunkenen geflutet); unter dem Altar des Gezeitenschreins liegt die Truhe mit dem Herz des Meeres;
 * Enderaugen, Schatzkarten und Delfine finden die eigenen Bauwerke, die alten entstehen nicht mehr.
 */
final class SiteGameTests {
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FALLEN_STAR_PORTAL =
            ModGameTests.TEST_FUNCTIONS.register("site_fallen_star_portal", () -> SiteGameTests::fallenStarPortal);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FALLEN_STAR_WALKWAY =
            ModGameTests.TEST_FUNCTIONS.register("site_fallen_star_walkway", () -> SiteGameTests::fallenStarWalkway);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> AQUEDUCT =
            ModGameTests.TEST_FUNCTIONS.register("site_aqueduct", () -> SiteGameTests::aqueduct);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SEA_DRAGON_AND_SHRINE =
            ModGameTests.TEST_FUNCTIONS.register("site_sea_dragon_shrine", () -> SiteGameTests::seaDragonAndShrine);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CARAVANSERAI =
            ModGameTests.TEST_FUNCTIONS.register("site_caravanserai", () -> SiteGameTests::caravanserai);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SERPENT_TEMPLE =
            ModGameTests.TEST_FUNCTIONS.register("site_serpent_temple", () -> SiteGameTests::serpentTemple);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WITCH_TREE =
            ModGameTests.TEST_FUNCTIONS.register("site_witch_tree", () -> SiteGameTests::witchTree);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FROST_GROTTO =
            ModGameTests.TEST_FUNCTIONS.register("site_frost_grotto", () -> SiteGameTests::frostGrotto);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BANDIT_CAMP =
            ModGameTests.TEST_FUNCTIONS.register("site_bandit_camp", () -> SiteGameTests::banditCamp);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHARCOAL_CAMP =
            ModGameTests.TEST_FUNCTIONS.register("site_charcoal_camp", () -> SiteGameTests::charcoalCamp);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> EMBER_GATE =
            ModGameTests.TEST_FUNCTIONS.register("site_ember_gate", () -> SiteGameTests::emberGate);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VANILLA_SITES_OFF =
            ModGameTests.TEST_FUNCTIONS.register("site_vanilla_off", () -> SiteGameTests::vanillaOff);

    private SiteGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, FALLEN_STAR_PORTAL, 100);
        add(event, environment, FALLEN_STAR_WALKWAY, 100);
        add(event, environment, AQUEDUCT, 100);
        add(event, environment, SEA_DRAGON_AND_SHRINE, 100);
        add(event, environment, CARAVANSERAI, 100);
        add(event, environment, SERPENT_TEMPLE, 200);
        add(event, environment, WITCH_TREE, 100);
        add(event, environment, FROST_GROTTO, 100);
        add(event, environment, BANDIT_CAMP, 100);
        add(event, environment, CHARCOAL_CAMP, 100);
        add(event, environment, EMBER_GATE, 100);
        add(event, environment, VANILLA_SITES_OFF, 100);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, TOWN, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Der Bereich des Tests (relativ 0..47, 0..29, 0..47) in Weltkoordinaten. */
    private static BoundingBox area(GameTestHelper helper) {
        return BoundingBox.fromCorners(helper.absolutePos(new BlockPos(0, 1, 0)), helper.absolutePos(new BlockPos(47, 29, 47)));
    }

    /** Baut ein Bauteil in einem Zug, aber nur innerhalb des Testbereichs (die Nachbartests bleiben unberührt). */
    private static void build(GameTestHelper helper, SitePiece piece) {
        ServerLevel level = helper.getLevel();
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(5), area(helper),
                ChunkPos.containing(piece.origin()), piece.origin());
    }

    private static void fill(GameTestHelper helper, BlockPos a, BlockPos b, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(a), helper.absolutePos(b))) {
            helper.getLevel().setBlock(pos, state, 2 | 16);
        }
    }

    private static void clear(GameTestHelper helper) {
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, 29, 47), Blocks.AIR.defaultBlockState());
        for (Entity e : helper.getLevel().getEntities((Entity) null, AABB.encapsulatingFullBlocks(helper.absolutePos(new BlockPos(-2, -2, -2)),
                helper.absolutePos(new BlockPos(50, 32, 50))), e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    private static int lootChests(GameTestHelper helper, BlockPos a, BlockPos b, Predicate<BlockState> extra) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(a, b)) {
            BlockEntity be = helper.getLevel().getBlockEntity(pos);
            if (be instanceof RandomizableContainer container && container.getLootTable() != null && extra.test(helper.getLevel().getBlockState(pos))) {
                n++;
            }
        }
        return n;
    }

    private static int count(GameTestHelper helper, BlockPos a, BlockPos b, Predicate<BlockState> test) {
        int n = 0;
        for (BlockPos pos : BlockPos.betweenClosed(a, b)) {
            if (test.test(helper.getLevel().getBlockState(pos))) {
                n++;
            }
        }
        return n;
    }

    private static VillagerEntity walker(GameTestHelper helper, BlockPos feet) {
        VillagerEntity v = ModEntities.VILLAGER.get().create(helper.getLevel(), EntitySpawnReason.COMMAND);
        helper.assertTrue(v != null, "kein Bewohner");
        v.setup(VillageProfession.PEASANT, 0, "Test", null, "", null, null, 1L);
        // geprüft wird, ob der Weg begehbar ist, nicht wie weit ein Bewohner plant: lange Wege zulassen
        v.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.FOLLOW_RANGE).setBaseValue(96.0);
        v.snapTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, 0.0f, 0.0f);
        v.setOnGround(true);
        helper.getLevel().addFreshEntity(v);
        return v;
    }

    // =====================================================================================
    // Sternensturz
    // =====================================================================================

    /** Die Mitte des Musters: Land auf Höhe 24 (relativ), die Kernkammer 18 tiefer. */
    private static final BlockPos STAR = new BlockPos(24, 24, 24);
    private static final int STAR_FLOOR = -18;

    private static BlockPos buildStar(GameTestHelper helper) {
        clear(helper);
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, STAR.getY(), 47), Blocks.STONE.defaultBlockState());
        fill(helper, new BlockPos(0, STAR.getY(), 0), new BlockPos(47, STAR.getY(), 47), Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos center = helper.absolutePos(STAR);
        build(helper, SiteSamples.fallenStar(center, false, STAR_FLOOR, 0.0, 1, helper.getLevel().getSeaLevel()));
        return center;
    }

    /**
     * Die zwölf Rahmen stehen um weinenden Obsidian, jeder zur Mitte gewandt; mit Enderaugen in allen zwölf erkennt das
     * Spiel die Portalform. Die Kammer ist dicht (keine Flüssigkeit darin), im Krater und unten liegt je eine Truhe.
     */
    private static void fallenStarPortal(GameTestHelper helper) {
        BlockPos center = buildStar(helper);
        int floor = center.getY() + STAR_FLOOR;
        BlockPos lo = center.offset(-3, STAR_FLOOR + 1, -3);
        BlockPos hi = center.offset(3, STAR_FLOOR + 3, 3);
        int frames = count(helper, lo, hi, s -> s.is(Blocks.END_PORTAL_FRAME));
        helper.assertTrue(frames == 12, "Portalrahmen: " + frames);
        BlockPos any = null;
        for (BlockPos pos : BlockPos.betweenClosed(lo, hi)) {
            BlockState state = helper.getLevel().getBlockState(pos);
            if (state.is(Blocks.END_PORTAL_FRAME)) {
                helper.getLevel().setBlock(pos, state.setValue(EndPortalFrameBlock.HAS_EYE, true), 2);
                any = pos.immutable();
            }
        }
        helper.assertTrue(any != null && EndPortalFrameBlock.getOrCreatePortalShape().find(helper.getLevel(), any) != null,
                "die zwölf Rahmen ergeben keine Portalform");
        // die Kammer: frei über dem Boden, ohne Flüssigkeit
        int air = 0;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-11, STAR_FLOOR + 1, -11), center.offset(11, STAR_FLOOR + 6, 11))) {
            BlockState s = helper.getLevel().getBlockState(pos);
            helper.assertTrue(s.getFluidState().isEmpty(), "Flüssigkeit in der Kammer bei " + helper.relativePos(pos));
            if (s.isAir()) {
                air++;
            }
        }
        helper.assertTrue(air > 900, "Kernkammer zu klein: " + air);
        int chests = lootChests(helper, center.offset(-27, STAR_FLOOR - 1, -27), center.offset(27, 6, 27), s -> true);
        helper.assertTrue(chests == 2, "Truhen mit Beute: " + chests);
        helper.assertTrue(floor > helper.absolutePos(BlockPos.ZERO).getY(), "Kammer unter dem Testbereich");
        helper.succeed();
    }

    /** Ein Bewohner findet vom oberen Ende des Wendelstegs einen Weg bis hinab auf den Boden der Kernkammer. */
    private static void fallenStarWalkway(GameTestHelper helper) {
        BlockPos center = buildStar(helper);
        // der Steg beginnt im Osten (Winkel 0) am Rand des Schachts
        BlockPos top = null;
        for (int y = 0; y >= -9 && top == null; y--) {
            BlockPos p = center.offset(5, y, 0);
            if (helper.getLevel().getBlockState(p).isAir() && helper.getLevel().getBlockState(p.above()).isAir()
                    && !helper.getLevel().getBlockState(p.below()).isAir()) {
                top = p;
            }
        }
        helper.assertTrue(top != null, "kein Anfang des Stegs gefunden");
        VillagerEntity v = walker(helper, top);
        BlockPos target = center.offset(8, STAR_FLOOR + 1, 1);
        var path = v.getNavigation().createPath(target, 1);
        helper.assertTrue(path != null && path.canReach(), "kein Weg den Steg hinab (von " + helper.relativePos(top) + ")"
                + (path == null ? "" : ", endet bei " + helper.relativePos(path.getEndNode().asBlockPos())));
        int steps = count(helper, center.offset(-7, STAR_FLOOR, -7), center.offset(7, 0, 7),
                s -> s.is(Blocks.SPRUCE_SLAB) || s.is(Blocks.SPRUCE_PLANKS));
        helper.assertTrue(steps >= 20, "Steg zu kurz: " + steps);
        v.discard();
        helper.succeed();
    }

    // =====================================================================================
    // Wasserstollen
    // =====================================================================================

    /**
     * Brunnenkammer, Kanal, Zisterne, Treppe und ein zweiter Kanal im Fels: die Rinne führt Wasser, das Becken der
     * Zisterne ist voll und leuchtet, Säulen tragen die Bögen, die Treppe führt hinab, Truhen haben Beute, das Brunnenhaus
     * steht oben auf der Erde; ein Bewohner kommt von der Brunnenkammer bis in die Zisterne.
     */
    private static void aqueduct(GameTestHelper helper) {
        clear(helper);
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, 21, 47), Blocks.STONE.defaultBlockState());
        fill(helper, new BlockPos(0, 22, 0), new BlockPos(47, 22, 47), Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos start = helper.absolutePos(new BlockPos(12, 10, 7));
        List<SitePiece> pieces = SiteSamples.aqueduct(start, 12);
        for (SitePiece piece : pieces) {
            build(helper, piece);
        }
        List<String> problems = new ArrayList<>();
        BlockPos lo = helper.absolutePos(new BlockPos(0, 1, 0));
        BlockPos hi = helper.absolutePos(new BlockPos(47, 29, 47));
        // Rinne im ersten Kanal
        int channel = 0;
        for (int z = 7; z <= 18; z++) {
            if (helper.getLevel().getFluidState(start.offset(0, 0, z)).isSource()) {
                channel++;
            }
        }
        if (channel < 10) {
            problems.add("Rinne: " + channel + " Wasser");
        }
        BlockPos cistern = pieces.get(2).origin();
        int pool = count(helper, cistern.offset(-4, -2, -4), cistern.offset(4, 0, 4), s -> s.is(Blocks.WATER));
        if (pool < 200) {
            problems.add("Becken: " + pool + " Wasser");
        }
        int lights = count(helper, cistern.offset(-4, -3, -4), cistern.offset(4, -3, 4), s -> s.is(Blocks.SEA_LANTERN));
        if (lights < 4) {
            problems.add("Meereslaternen: " + lights);
        }
        int columns = count(helper, cistern.offset(-5, 3, -5), cistern.offset(5, 3, 5), s -> s.is(Blocks.STONE_BRICKS));
        if (columns < 8) {
            problems.add("Säulen: " + columns);
        }
        int stairs = count(helper, lo, hi, s -> s.is(Blocks.STONE_BRICK_STAIRS) && s.getValue(BlockStateProperties.HALF)
                == net.minecraft.world.level.block.state.properties.Half.BOTTOM);
        if (stairs < 10) {
            problems.add("Treppenstufen: " + stairs);
        }
        int chests = lootChests(helper, lo, hi, s -> true);
        if (chests < 3) {
            problems.add("Truhen mit Beute: " + chests);
        }
        int wet = lootChests(helper, lo, hi, s -> s.hasProperty(BlockStateProperties.WATERLOGGED) && s.getValue(BlockStateProperties.WATERLOGGED));
        if (wet != 1) {
            problems.add("Truhe auf dem Grund der Zisterne: " + wet);
        }
        int house = count(helper, start.offset(-3, 12, -3), start.offset(3, 18, 3), s -> s.is(Blocks.SPRUCE_STAIRS) || s.is(Blocks.SPRUCE_LOG));
        if (house < 10) {
            problems.add("Brunnenhaus: " + house);
        }
        int ladder = count(helper, start.offset(0, 1, -1), start.offset(0, 12, -1), s -> s.is(Blocks.LADDER));
        if (ladder < 11) {
            problems.add("Leiter im Brunnenschacht: " + ladder);
        }
        VillagerEntity v = walker(helper, start.offset(3, 1, 3));
        var path = v.getNavigation().createPath(cistern.offset(6, 1, -6), 1);
        if (path == null || !path.canReach()) {
            problems.add("kein Weg in die Zisterne" + (path == null ? "" : " (endet bei " + helper.relativePos(path.getEndNode().asBlockPos()) + ")"));
        }
        v.discard();
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    // =====================================================================================
    // Seedrache und Gezeitenschrein
    // =====================================================================================

    /**
     * Ein gesunkener Seedrache (unter dem Meeresspiegel: Truhen geflutet), ein gestrandeter (trocken) und ein
     * Gezeitenschrein (die Truhe in der versiegelten Kammer unter dem Altar).
     */
    private static void seaDragonAndShrine(GameTestHelper helper) {
        clear(helper);
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, 3, 47), Blocks.SAND.defaultBlockState());
        List<String> problems = new ArrayList<>();
        BlockPos sunken = helper.absolutePos(new BlockPos(12, 4, 2));
        build(helper, SiteSamples.seaDragon(sunken, Rotation.NONE, true, sunken.getY() + 16));
        BlockPos beached = helper.absolutePos(new BlockPos(36, 4, 2));
        build(helper, SiteSamples.seaDragon(beached, Rotation.NONE, false, beached.getY() - 4));
        for (BlockPos origin : new BlockPos[] {sunken, beached}) {
            boolean wet = origin == sunken;
            String name = wet ? "gesunkener Drache" : "gestrandeter Drache";
            BlockPos a = origin.offset(-10, -3, -11);
            BlockPos b = origin.offset(10, 12, 42);
            int bones = count(helper, a, b, s -> s.is(Blocks.BONE_BLOCK));
            if (bones < 120) {
                problems.add(name + ": " + bones + " Knochen");
            }
            int chests = lootChests(helper, a, b, s -> true);
            if (chests != 3) {
                problems.add(name + ": " + chests + " Truhen");
            }
            int flooded = lootChests(helper, a, b, s -> s.getValue(BlockStateProperties.WATERLOGGED));
            if (flooded != (wet ? 3 : 0)) {
                problems.add(name + ": " + flooded + " geflutete Truhen");
            }
            int eyes = count(helper, origin.offset(-3, 4, -2), origin.offset(3, 4, -2), s -> s.is(Blocks.SEA_LANTERN));
            if (eyes != 2) {
                problems.add(name + ": " + eyes + " Augen");
            }
        }
        BlockPos shrine = helper.absolutePos(new BlockPos(24, 6, 40));
        fill(helper, new BlockPos(16, 1, 32), new BlockPos(32, 6, 47), Blocks.SAND.defaultBlockState());
        build(helper, SiteSamples.tideShrine(shrine));
        BlockEntity chest = helper.getLevel().getBlockEntity(shrine.offset(0, -3, 0));
        if (!(chest instanceof RandomizableContainer c) || c.getLootTable() == null) {
            problems.add("Schrein: keine Truhe mit Beute unter dem Altar");
        }
        if (!helper.getLevel().getBlockState(shrine).is(Blocks.SEA_LANTERN) || !helper.getLevel().getBlockState(shrine.above()).is(Blocks.WATER)) {
            problems.add("Schrein: kein leuchtendes Becken auf dem Altar");
        }
        if (!helper.getLevel().getBlockState(shrine.below()).isSolidRender()) {
            problems.add("Schrein: Kammer nicht versiegelt");
        }
        int pillars = count(helper, shrine.offset(-4, 1, -4), shrine.offset(4, 1, 4), s -> s.is(Blocks.DARK_PRISMARINE));
        if (pillars != 8) {
            problems.add("Schrein: " + pillars + " Säulen");
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    // =====================================================================================
    // Karawanserei und Schlangentempel
    // =====================================================================================

    /**
     * Die Karawanserei auf Sand: Tor offen, Brunnenschacht mit Leiter bis in den Keller, dort zwei Schatztruhen und die
     * Falle (Druckplatte über neun Blöcken Sprengstoff), im Hof verdächtiger Sand mit Scherben-Beute, eine Truhe in den
     * Kammern.
     */
    private static void caravanserai(GameTestHelper helper) {
        clear(helper);
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, 11, 47), Blocks.SAND.defaultBlockState());
        BlockPos c = helper.absolutePos(new BlockPos(24, 12, 24));
        build(helper, SiteSamples.caravanserai(c));
        List<String> problems = new ArrayList<>();
        for (int x = -1; x <= 1; x++) {
            for (int y = 1; y <= 3; y++) {
                if (!helper.getLevel().getBlockState(c.offset(x, y, 11)).isAir()) {
                    problems.add("Tor zu bei " + x + "/" + y);
                }
            }
        }
        int treasure = lootChests(helper, c.offset(-4, -7, 2), c.offset(4, -1, 8), s -> true);
        if (treasure != 2) {
            problems.add("Schatzkeller: " + treasure + " Truhen");
        }
        int all = lootChests(helper, c.offset(-12, -9, -12), c.offset(12, 10, 12), s -> true);
        if (all != 3) {
            problems.add("Truhen insgesamt: " + all);
        }
        int tnt = count(helper, c.offset(-1, -8, 4), c.offset(1, -8, 6), s -> s.is(Blocks.TNT));
        if (tnt != 9 || !helper.getLevel().getBlockState(c.offset(0, -6, 5)).is(Blocks.STONE_PRESSURE_PLATE)) {
            problems.add("Falle: " + tnt + " Sprengstoff, Platte " + helper.getLevel().getBlockState(c.offset(0, -6, 5)));
        }
        int ladder = count(helper, c.offset(0, -6, -1), c.offset(0, 1, -1), s -> s.is(Blocks.LADDER));
        if (ladder < 8) {
            problems.add("Leiter im Brunnen: " + ladder);
        }
        int brush = 0;
        for (BlockPos pos : BlockPos.betweenClosed(c.offset(-5, -2, -5), c.offset(-1, 0, -2))) {
            if (helper.getLevel().getBlockState(pos).is(Blocks.SUSPICIOUS_SAND)
                    && helper.getLevel().getBlockEntity(pos) instanceof net.minecraft.world.level.block.entity.BrushableBlockEntity be
                    && be.saveWithoutMetadata(helper.getLevel().registryAccess()).contains("LootTable")) {
                brush++;
            }
        }
        if (brush < 3) {
            problems.add("verdächtiger Sand mit Beute: " + brush);
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /**
     * Der Schlangentempel: drei Truhen (zwei am Altar, eine versteckt), vier Pfeilwerfer mit Beute am Stolperdraht im
     * Schlund – und die Falle löst aus, wenn jemand über den Draht geht (es fliegen Pfeile).
     */
    private static void serpentTemple(GameTestHelper helper) {
        clear(helper);
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, 13, 47), Blocks.DIRT.defaultBlockState());
        fill(helper, new BlockPos(0, 14, 0), new BlockPos(47, 14, 47), Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos o = helper.absolutePos(new BlockPos(24, 14, 6));
        build(helper, SiteSamples.serpentTemple(o));
        List<String> problems = new ArrayList<>();
        BlockPos lo = o.offset(-9, -10, -3);
        BlockPos hi = o.offset(9, 12, 29);
        int chests = lootChests(helper, lo, hi, s -> s.is(Blocks.CHEST));
        if (chests != 3) {
            problems.add("Truhen: " + chests);
        }
        int dispensers = lootChests(helper, lo, hi, s -> s.is(Blocks.DISPENSER));
        if (dispensers != 4) {
            problems.add("Pfeilwerfer: " + dispensers);
        }
        BlockState wire = helper.getLevel().getBlockState(o.offset(0, 1, 7));
        if (!wire.is(Blocks.TRIPWIRE) || !wire.getValue(net.minecraft.world.level.block.TripWireBlock.ATTACHED)) {
            problems.add("Stolperdraht: " + wire);
        }
        int eyes = count(helper, lo, hi, s -> s.is(Blocks.SEA_LANTERN));
        if (eyes != 8) {
            problems.add("Augen: " + eyes + " Lichter");
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        // über den Draht gehen: die Falle muss auslösen
        VillagerEntity v = walker(helper, o.offset(0, 1, 7));
        helper.runAfterDelay(30, () -> {
            var arrows = helper.getLevel().getEntitiesOfClass(net.minecraft.world.entity.projectile.arrow.AbstractArrow.class,
                    AABB.encapsulatingFullBlocks(o.offset(-3, 0, 4), o.offset(3, 5, 10)));
            v.discard();
            helper.assertTrue(!arrows.isEmpty(), "die Falle hat nicht ausgelöst");
            arrows.forEach(Entity::discard);
            helper.succeed();
        });
    }

    // =====================================================================================
    // Hexenbaum, Frostgrotte, Räuberlager, Köhlerei, Glutpforte
    // =====================================================================================

    private static BlockPos ground(GameTestHelper helper, int top, BlockState surface) {
        clear(helper);
        fill(helper, new BlockPos(0, 1, 0), new BlockPos(47, top - 1, 47), Blocks.DIRT.defaultBlockState());
        fill(helper, new BlockPos(0, top, 0), new BlockPos(47, top, 47), surface);
        return helper.absolutePos(new BlockPos(24, top, 24));
    }

    /** Der Hexenbaum: hohler Stamm mit Kessel, Braustand, Truhe; Leiter bis in die Krone; Hexen und Katze tauchen hier auf. */
    private static void witchTree(GameTestHelper helper) {
        BlockPos g = ground(helper, 6, Blocks.MUD.defaultBlockState());
        BlockPos o = g.above();
        build(helper, SiteSamples.witchTree(o));
        List<String> problems = new ArrayList<>();
        if (!helper.getLevel().getBlockState(o).is(Blocks.CAULDRON) || !helper.getLevel().getBlockState(o.offset(-2, 0, 0)).is(Blocks.BREWING_STAND)) {
            problems.add("Kessel/Braustand fehlen");
        }
        int chests = lootChests(helper, o.offset(-4, -1, -4), o.offset(4, 3, 4), s -> true);
        if (chests != 1) {
            problems.add("Truhen: " + chests);
        }
        int ladder = count(helper, o.offset(0, 0, -2), o.offset(0, 16, -2), s -> s.is(Blocks.LADDER));
        if (ladder < 16) {
            problems.add("Leiter: " + ladder);
        }
        var spawns = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(SiteKind.WITCH_TREE))
                .value().spawnOverrides();
        if (!spawns.containsKey(net.minecraft.world.entity.MobCategory.MONSTER) || !spawns.containsKey(net.minecraft.world.entity.MobCategory.CREATURE)) {
            problems.add("keine Hexen/Katze am Hexenbaum");
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** Die Frostgrotte: Stube mit Feuer und Truhe, Falltür unter dem Fell, Leiter hinab in den Keller mit dem Schatz. */
    private static void frostGrotto(GameTestHelper helper) {
        BlockPos o = ground(helper, 8, Blocks.SNOW_BLOCK.defaultBlockState());
        build(helper, SiteSamples.frostGrotto(o));
        List<String> problems = new ArrayList<>();
        int chests = lootChests(helper, o.offset(-8, -7, -8), o.offset(8, 5, 8), s -> true);
        if (chests != 2) {
            problems.add("Truhen: " + chests);
        }
        if (!helper.getLevel().getBlockState(o.offset(-3, 0, -2)).is(Blocks.SPRUCE_TRAPDOOR)) {
            problems.add("Falltür fehlt");
        }
        int ladder = count(helper, o.offset(-3, -4, -2), o.offset(-3, -1, -2), s -> s.is(Blocks.LADDER));
        if (ladder != 4) {
            problems.add("Kellerleiter: " + ladder);
        }
        if (!helper.getLevel().getBlockState(o.offset(0, 1, 0)).is(Blocks.CAMPFIRE)) {
            problems.add("kein Feuer");
        }
        int room = count(helper, o.offset(-4, 1, -3), o.offset(4, 3, 3), BlockState::isAir);
        if (room < 60) {
            problems.add("Stube zu klein: " + room);
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** Das Räuberlager: Palisade mit Tor, Käfig mit einem Gefangenen, Wachgerüst mit Leiter, eine Truhe mit Beute. */
    private static void banditCamp(GameTestHelper helper) {
        BlockPos g = ground(helper, 6, Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos o = g.above();
        build(helper, SiteSamples.banditCamp(o));
        List<String> problems = new ArrayList<>();
        int chests = lootChests(helper, o.offset(-12, -1, -12), o.offset(12, 8, 12), s -> true);
        if (chests != 1) {
            problems.add("Truhen: " + chests);
        }
        int logs = count(helper, o.offset(-12, 0, -12), o.offset(12, 0, 12), s -> s.is(Blocks.SPRUCE_LOG));
        if (logs < 40) {
            problems.add("Palisade: " + logs);
        }
        var captives = helper.getLevel().getEntitiesOfClass(VillagerEntity.class, AABB.encapsulatingFullBlocks(o.offset(-3, 0, -9), o.offset(-1, 2, -7)));
        if (captives.size() != 1) {
            problems.add("Gefangene: " + captives.size());
        }
        captives.forEach(Entity::discard);
        int ladder = count(helper, o.offset(-6, 0, -3), o.offset(-6, 5, -3), s -> s.is(Blocks.LADDER));
        if (ladder != 6) {
            problems.add("Leiter am Wachgerüst: " + ladder);
        }
        var spawns = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(SiteKind.BANDIT_CAMP))
                .value().spawnOverrides();
        if (!spawns.containsKey(net.minecraft.world.entity.MobCategory.MONSTER)) {
            problems.add("keine Wegelagerer im Lager");
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** Die Köhlerei: Meiler mit Rauchfeuer, Holzstapel, Unterstand mit Truhe, darunter das Versteck. */
    private static void charcoalCamp(GameTestHelper helper) {
        BlockPos g = ground(helper, 6, Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos o = g.above();
        build(helper, SiteSamples.charcoalCamp(o, 1));
        List<String> problems = new ArrayList<>();
        int chests = lootChests(helper, o.offset(-10, -4, -10), o.offset(10, 6, 10), s -> true);
        if (chests != 2) {
            problems.add("Truhen: " + chests);
        }
        BlockState fire = helper.getLevel().getBlockState(o.offset(-3, 3, -2));
        if (!fire.is(Blocks.CAMPFIRE) || !fire.getValue(net.minecraft.world.level.block.CampfireBlock.SIGNAL_FIRE)) {
            problems.add("Meilerfeuer: " + fire);
        }
        int logs = count(helper, o.offset(-10, 0, -10), o.offset(10, 3, 10), s -> s.is(Blocks.SPRUCE_LOG));
        if (logs < 20) {
            problems.add("Holz: " + logs);
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        helper.succeed();
    }

    /** Die Glutpforte: im Rahmen fehlen ein bis drei Steine; mit Obsidian ergänzt lässt sich das Tor entzünden. */
    private static void emberGate(GameTestHelper helper) {
        BlockPos g = ground(helper, 6, Blocks.GRASS_BLOCK.defaultBlockState());
        BlockPos o = g.above();
        build(helper, SiteSamples.emberGate(o));
        List<String> problems = new ArrayList<>();
        int[][] frame = {{0, 0}, {1, 0}, {-1, 1}, {-1, 2}, {-1, 3}, {2, 1}, {2, 2}, {2, 3}, {0, 4}, {1, 4}};
        int obsidian = 0;
        for (int[] f : frame) {
            if (helper.getLevel().getBlockState(o.offset(f[0], f[1], -2)).is(Blocks.OBSIDIAN)) {
                obsidian++;
            }
        }
        if (obsidian < 7 || obsidian > 9) {
            problems.add("Obsidian im Rahmen: " + obsidian);
        }
        int chests = lootChests(helper, o.offset(-8, -2, -8), o.offset(8, 6, 8), s -> true);
        if (chests != 1) {
            problems.add("Truhen: " + chests);
        }
        helper.assertTrue(problems.isEmpty(), String.join("; ", problems));
        // ergänzen und entzünden
        for (int[] f : frame) {
            helper.getLevel().setBlock(o.offset(f[0], f[1], -2), Blocks.OBSIDIAN.defaultBlockState(), 3);
        }
        var shape = net.minecraft.world.level.portal.PortalShape.findEmptyPortalShape(helper.getLevel(), o.offset(0, 1, -2), Direction.Axis.X);
        helper.assertTrue(shape.isPresent(), "das ergänzte Tor lässt sich nicht entzünden");
        shape.get().createPortalBlocks(helper.getLevel());
        helper.assertTrue(helper.getLevel().getBlockState(o.offset(0, 1, -2)).is(Blocks.NETHER_PORTAL), "kein Portal");
        helper.succeed();
    }

    // =====================================================================================
    // Tags
    // =====================================================================================

    /** Enderaugen, Schatzkarten, Delfine und Minenkarten finden die eigenen Bauwerke; die alten entstehen nicht mehr. */
    private static void vanillaOff(GameTestHelper helper) {
        var access = helper.getLevel().registryAccess();
        var biomes = access.lookupOrThrow(Registries.BIOME);
        for (var tag : List.of(BiomeTags.HAS_STRONGHOLD, BiomeTags.HAS_MINESHAFT, BiomeTags.HAS_MINESHAFT_MESA, BiomeTags.HAS_BURIED_TREASURE,
                BiomeTags.HAS_SHIPWRECK, BiomeTags.HAS_SHIPWRECK_BEACHED, BiomeTags.HAS_DESERT_PYRAMID, BiomeTags.HAS_JUNGLE_TEMPLE,
                BiomeTags.HAS_SWAMP_HUT, BiomeTags.HAS_IGLOO, BiomeTags.HAS_PILLAGER_OUTPOST, BiomeTags.HAS_RUINED_PORTAL_STANDARD,
                BiomeTags.HAS_RUINED_PORTAL_DESERT, BiomeTags.HAS_ABANDONED_CAMP_FOREST, BiomeTags.HAS_ABANDONED_CAMP_TAIGA)) {
            helper.assertTrue(biomes.get(tag).map(set -> set.size() == 0).orElse(true), "altes Bauwerk noch erlaubt: " + tag.location());
        }
        var structures = access.lookupOrThrow(Registries.STRUCTURE);
        for (var key : List.of(BuiltinStructures.STRONGHOLD, BuiltinStructures.MINESHAFT, BuiltinStructures.MINESHAFT_MESA,
                BuiltinStructures.BURIED_TREASURE, BuiltinStructures.SHIPWRECK, BuiltinStructures.SHIPWRECK_BEACHED)) {
            structures.get(key).ifPresent(s -> helper.assertTrue(s.value().biomes().size() == 0, "altes Bauwerk hat noch Land: " + key.identifier()));
        }
        for (SiteKind kind : SiteKind.values()) {
            helper.assertTrue(biomes.get(ModTags.Biomes.HAS_SITE.get(kind)).map(set -> set.size() > 0).orElse(false),
                    "kein Land für " + kind.id());
        }
        record Expect(TagKey<Structure> tag, SiteKind kind) {}
        for (Expect e : List.of(new Expect(StructureTags.EYE_OF_ENDER_LOCATED, SiteKind.FALLEN_STAR),
                new Expect(StructureTags.ON_TREASURE_MAPS, SiteKind.TIDE_SHRINE), new Expect(StructureTags.MINESHAFT, SiteKind.AQUEDUCT),
                new Expect(StructureTags.ON_MINESHAFT_MAPS, SiteKind.AQUEDUCT), new Expect(StructureTags.SHIPWRECK, SiteKind.SEA_DRAGON))) {
            var set = structures.get(e.tag()).orElseThrow();
            helper.assertTrue(set.size() == 1 && set.get(0).is(ModStructures.site(e.kind())), e.tag().location() + " zeigt nicht auf "
                    + e.kind().id());
        }
        helper.succeed();
    }
}
