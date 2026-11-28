package io.github.sirschrolli.kingdomsatwar.gametest;

import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.command.DimensionCommand;
import io.github.sirschrolli.kingdomsatwar.command.KawLocate;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu {@code /kaw} (seit 1.1.2): die Befehle sind nach Art geordnet ({@code dim} nur Dimensionen, {@code boss} nur
 * Bosse, {@code structure} nur Bauwerke), jedes Bauwerk der Mod lässt sich finden, man landet sicher, und per Klick reist
 * nur, wer die Befehle nutzen darf.
 */
final class KawCommandGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TREE =
            ModGameTests.TEST_FUNCTIONS.register("kaw_tree", () -> KawCommandGameTests::tree);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COVERAGE =
            ModGameTests.TEST_FUNCTIONS.register("kaw_coverage", () -> KawCommandGameTests::coverage);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LANDING =
            ModGameTests.TEST_FUNCTIONS.register("kaw_landing", () -> KawCommandGameTests::landing);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CLICK =
            ModGameTests.TEST_FUNCTIONS.register("kaw_click", () -> KawCommandGameTests::click);

    private KawCommandGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(TREE, COVERAGE, LANDING, CLICK)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static Set<String> literals(GameTestHelper helper, CommandNode<CommandSourceStack> node, String what) {
        Set<String> names = new HashSet<>();
        for (CommandNode<CommandSourceStack> child : node.getChildren()) {
            helper.assertTrue(child instanceof LiteralCommandNode, what + ": nicht nur Namen, auch " + child.getUsageText());
            names.add(child.getName());
        }
        return names;
    }

    /** Der Befehlsbaum: {@code dim}, {@code boss} und {@code structure} zeigen nur Namen ihrer Art; die alten Einträge sind fort. */
    private static void tree(GameTestHelper helper) {
        var root = helper.getLevel().getServer().getCommands().getDispatcher().getRoot().getChild("kaw");
        helper.assertTrue(root != null, "/kaw fehlt");
        for (String name : List.of("intro", "army", "dim", "boss", "structure", "story", "seal", "realms", "raid", "hero")) {
            helper.assertTrue(root.getChild(name) != null, "/kaw " + name + " fehlt");
        }
        for (String old : List.of("citadel", "throne", "atoll", "kraken", "mondhain", "zauberbuehne", "hexengrotte", "seelenpferch",
                "sternennetz", "uhrturm", "mondsteinbruch", "fehlerzone", "lair")) {
            helper.assertTrue(root.getChild(old) == null, "Alter Eintrag /kaw " + old + " ist noch da");
        }
        // Dimensionen: nur die Namen (keine Kampfdimensionen, keine IDs)
        Set<String> dims = literals(helper, root.getChild("dim"), "/kaw dim");
        helper.assertValueEqual(dims, new HashSet<>(DimensionCommand.names()), "Namen unter /kaw dim");
        helper.assertTrue(dims.containsAll(List.of("oberwelt", "nether", "end", "schattenwelt", "leere", "abgrund", "urmeer", "feenreich")),
                "Dimensionen fehlen: " + dims);
        // Bauwerke: genau die Orte, jeder mit tp
        var structure = root.getChild("structure");
        Set<String> sites = literals(helper, structure, "/kaw structure");
        Set<String> expectedSites = new HashSet<>();
        KawPlaces.sites().forEach(site -> expectedSites.add(site.id()));
        helper.assertValueEqual(sites, expectedSites, "Namen unter /kaw structure");
        helper.assertTrue(sites.containsAll(List.of("uhrturm", "sternennetz", "zitadelle", "atoll", "mondhain", "aschenesse", "sternwarte",
                "krakenschlund", "fachwerkdorf", "vulkankrater")), "Bauwerke fehlen: " + sites);
        for (String site : sites) {
            helper.assertTrue(structure.getChild(site).getChild("tp") != null, "/kaw structure " + site + " tp fehlt");
        }
        // Bosse: genau die Bosse, je mit tp; wecken und zurücksetzen, wo es geht
        var boss = root.getChild("boss");
        Set<String> bosses = literals(helper, boss, "/kaw boss");
        helper.assertValueEqual(bosses.size(), KawPlaces.bosses().size(), "Anzahl der Bosse");
        for (KawPlaces.Boss b : KawPlaces.bosses()) {
            var node = boss.getChild(b.id());
            helper.assertTrue(node != null && node.getChild("tp") != null, "/kaw boss " + b.id() + " tp fehlt");
            helper.assertValueEqual(node.getChild("awaken") != null, b.awaken() != null, "awaken bei " + b.id());
            helper.assertValueEqual(node.getChild("reset") != null, b.reset() != null, "reset bei " + b.id());
            helper.assertTrue(sites.contains(b.site().id()), "Hort von " + b.id() + " ist kein Bauwerk");
        }
        for (String id : List.of("stundenwaechter", "sternenweberin", "seelenhirte", "meerhexe", "kraken", "karkinos", "nyxar", "pyrrhon",
                "koenig", "weltenfresser", "malleus", "asteria", "anni", "missmystik", "steinhueter")) {
            helper.assertTrue(boss.getChild(id) != null && boss.getChild(id).getChild("awaken") != null
                    && boss.getChild(id).getChild("reset") != null, "/kaw boss " + id + " awaken/reset fehlt");
        }
        helper.assertTrue(boss.getChild("noctua").getChild("awaken") != null, "Noctua lässt sich nicht wecken");
        helper.assertTrue(boss.getChild("pyrrhon").getChild("summon") != null && boss.getChild("nyxar").getChild("summon") != null,
                "Pyrrhon und Nyxar lassen sich nicht mehr herbeirufen");
        helper.assertTrue(boss.getChild("ignatius") != null && boss.getChild("ignatius").getChild("awaken") == null, "Ignatius falsch");
        helper.succeed();
    }

    /** Jedes Bauwerk der Mod gehört zu einem Ort (und jeder Ort zu einem Bauwerk, das es gibt). */
    private static void coverage(GameTestHelper helper) {
        var structures = helper.getLevel().registryAccess().lookupOrThrow(Registries.STRUCTURE);
        Set<ResourceKey<Structure>> covered = new HashSet<>(KawPlaces.coveredStructures());
        for (var key : structures.registryKeySet()) {
            if (KingdomsAtWar.MODID.equals(key.identifier().getNamespace())) {
                helper.assertTrue(covered.contains(key), "Bauwerk ohne /kaw structure: " + key.identifier());
            }
        }
        for (ResourceKey<Structure> key : covered) {
            helper.assertTrue(structures.get(key).isPresent(), "Ort zu einem Bauwerk, das es nicht gibt: " + key.identifier());
        }
        helper.assertTrue(covered.size() >= 57, "Zu wenige Bauwerke: " + covered.size());
        helper.succeed();
    }

    /** Sicherer Stand: fester Boden, zwei Blöcke Luft; nie in oder auf Lava, nicht im Pulverschnee, Wasser nur auf Wunsch. */
    private static void landing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // acht Säulen in der Arena (9 × 9): Boden aus Stein auf Höhe 2, darüber frei
        int[][] spots = {{1, 1}, {3, 1}, {5, 1}, {7, 1}, {1, 4}, {3, 4}, {5, 4}, {7, 4}};
        BlockPos[] floor = new BlockPos[spots.length];
        for (int i = 0; i < spots.length; i++) {
            floor[i] = helper.absolutePos(new BlockPos(spots[i][0], 2, spots[i][1]));
            for (int dy = 1; dy <= 3; dy++) {
                level.setBlockAndUpdate(floor[i].above(dy), Blocks.AIR.defaultBlockState());
            }
            level.setBlockAndUpdate(floor[i], Blocks.STONE.defaultBlockState());
        }
        helper.assertTrue(KawPlaces.standable(level, floor[0].above(), false), "Auf Stein mit Luft darüber kein Stand");
        level.setBlockAndUpdate(floor[1], Blocks.LAVA.defaultBlockState());
        helper.assertFalse(KawPlaces.standable(level, floor[1].above(), true), "Auf Lava gelandet");
        level.setBlockAndUpdate(floor[2].above(), Blocks.LAVA.defaultBlockState());
        helper.assertFalse(KawPlaces.standable(level, floor[2].above(), true), "In Lava gelandet");
        level.setBlockAndUpdate(floor[3].above(2), Blocks.STONE.defaultBlockState());
        helper.assertFalse(KawPlaces.standable(level, floor[3].above(), true), "Mit dem Kopf in der Decke gelandet");
        level.setBlockAndUpdate(floor[4].above(), Blocks.WATER.defaultBlockState());
        helper.assertFalse(KawPlaces.standable(level, floor[4].above(), false), "Trocken gesucht, im Wasser gelandet");
        helper.assertTrue(KawPlaces.standable(level, floor[4].above(), true), "Im Wasser kein Stand, obwohl erlaubt");
        level.setBlockAndUpdate(floor[5].above(), Blocks.POWDER_SNOW.defaultBlockState());
        helper.assertFalse(KawPlaces.standable(level, floor[5].above(), true), "Im Pulverschnee gelandet");
        level.setBlockAndUpdate(floor[6], Blocks.MAGMA_BLOCK.defaultBlockState());
        helper.assertFalse(KawPlaces.standable(level, floor[6].above(), true), "Auf Magma gelandet");
        // eine Säule mit Dach: der höchste Stand ist auf dem Dach, nicht darunter
        BlockPos roof = floor[7].above(3);
        level.setBlockAndUpdate(roof, Blocks.GLASS.defaultBlockState());
        BlockPos found = KawPlaces.column(level, roof.getX(), roof.getZ(), null, false);
        helper.assertValueEqual(found, roof.above(), "Stand in der Säule mit Dach");
        helper.succeed();
    }

    /** Per Klick reist nur, wer die Befehle nutzen darf – sonst bleibt man, wo man ist. */
    private static void click(GameTestHelper helper) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.snapTo(helper.absoluteVec(new Vec3(2.5, 2.0, 2.5)));
        Vec3 before = player.position();
        CompoundTag tag = new CompoundTag();
        tag.putString("site", "fehlerzone");
        tag.putString("dim", Level.OVERWORLD.identifier().toString());
        tag.putInt("x", 0);
        tag.putInt("z", 0);
        helper.assertFalse(KawLocate.click(player, tag), "Ohne Rechte gereist");
        helper.assertTrue(player.position().distanceTo(before) < 0.01, "Ohne Rechte versetzt");
        // ein Ort, den es nicht gibt, oder eine falsche Dimension: auch mit Rechten keine Reise
        tag.putString("site", "gibt_es_nicht");
        helper.assertFalse(KawLocate.click(player, tag), "Zu einem unbekannten Ort gereist");
        helper.assertTrue(KawPlaces.site("uhrturm") != null && KawPlaces.site("uhrturm").dimension() != Level.OVERWORLD,
                "Der Uhrturm liegt nicht im Abgrund");
        // die Koordinaten im Chat: in eckigen Klammern (Höhe „~“, wenn sie nicht feststeht), mit der Klick-Aktion zum Reisen
        Component coords = KawLocate.coordinates(helper.getLevel(), KawPlaces.ERROR_ZONE,
                new KawPlaces.Spot(new BlockPos(120, 70, -340), false, null));
        helper.assertValueEqual(coords.getString(), "[120, ~, -340]", "Koordinaten im Chat");
        helper.assertTrue(coords.getStyle().getClickEvent() instanceof ClickEvent.Custom custom && custom.id().equals(KawLocate.TRAVEL)
                && custom.payload().orElse(null) instanceof CompoundTag payload && payload.getStringOr("site", "").equals("fehlerzone")
                && payload.getStringOr("dim", "").equals("minecraft:overworld") && payload.getIntOr("x", 0) == 120
                && payload.getIntOr("z", 0) == -340, "Die Koordinaten führen nicht zur Fehlerzone: " + coords.getStyle().getClickEvent());
        Component floating = KawLocate.coordinates(helper.getLevel(), KawPlaces.ERROR_ZONE, new KawPlaces.Spot(new BlockPos(5, 140, 6), true, null));
        helper.assertValueEqual(floating.getString(), "[5, 140, 6]", "Koordinaten mit Höhe");
        helper.succeed();
    }
}
