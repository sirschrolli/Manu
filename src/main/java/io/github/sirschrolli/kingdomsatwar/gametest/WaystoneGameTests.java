package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.waystone.WaystoneCompass;
import io.github.sirschrolli.kingdomsatwar.waystone.Waypoints;
import io.netty.buffer.Unpooled;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;

/**
 * Tests zu Wegkompass und Wegsteinen (seit 1.12): Der Kompass von Minecraft lässt sich nicht mehr herstellen, der Wegkompass
 * schon (und ersetzt ihn in den Rezepten); er merkt sich bis zu acht Wegsteine, wählt eins als Ziel (die Nadel bekommt es
 * über den {@code lodestone_tracker}), vergisst sie wieder und merkt, wenn einer nicht mehr steht; ein Wegstein heißt nach
 * seinem Biom oder nach einem Namensschild, behält einen gegebenen Namen beim Abbauen und leuchtet.
 */
final class WaystoneGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RECIPES =
            ModGameTests.TEST_FUNCTIONS.register("waystone_recipes", () -> WaystoneGameTests::recipes);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> COMPASS =
            ModGameTests.TEST_FUNCTIONS.register("waystone_compass", () -> WaystoneGameTests::compass);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STONE =
            ModGameTests.TEST_FUNCTIONS.register("waystone_block", () -> WaystoneGameTests::stone);

    private WaystoneGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(RECIPES, COMPASS, STONE)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Rezepte
    // =====================================================================================

    /** Kein Kompass mehr aus Eisen; der Wegkompass, der Wegstein, die Karte aus Papier und der Bergungskompass gehen. */
    private static void recipes(GameTestHelper helper) {
        var manager = helper.getLevel().getServer().getRecipeManager();
        helper.assertTrue(manager.byKey(ResourceKey.create(Registries.RECIPE, Identifier.withDefaultNamespace("compass"))).isEmpty(),
                "Der Kompass von Minecraft lässt sich noch herstellen (Quellen: " + helper.getLevel().getServer().getResourceManager()
                        .getResourceStack(Identifier.withDefaultNamespace("recipe/compass.json")).stream().map(r -> r.sourcePackId()).toList() + ")");
        for (String name : List.of("waystone_compass", "waystone", "map_from_paper", "recovery_compass_from_waystone_compass")) {
            helper.assertTrue(manager.byKey(ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(name))).isPresent(), "Rezept fehlt: " + name);
        }
        ItemStack e = ItemStack.EMPTY;
        ItemStack iron = new ItemStack(Items.IRON_INGOT);
        ItemStack copper = new ItemStack(Items.COPPER_INGOT);
        ItemStack redstone = new ItemStack(Items.REDSTONE);
        ItemStack nugget = new ItemStack(Items.GOLD_NUGGET);
        crafts(helper, List.of(e, iron, e, iron, redstone, iron, e, iron, e), Items.AIR);
        crafts(helper, List.of(e, nugget, e, copper, redstone, copper, e, copper, e), ModItems.WAYSTONE_COMPASS.get());
        ItemStack bricks = new ItemStack(Items.STONE_BRICKS);
        ItemStack aetherit = new ItemStack(ModItems.AETHERIT.get());
        crafts(helper, List.of(e, aetherit, e, bricks, copper, bricks, bricks, bricks, bricks), ModBlocks.WAYSTONE.get().asItem());
        ItemStack paper = new ItemStack(Items.PAPER);
        crafts(helper, List.of(paper, paper, paper, paper, paper, paper, paper, paper, paper), Items.MAP);
        ItemStack compass = new ItemStack(ModItems.WAYSTONE_COMPASS.get());
        ItemStack echo = new ItemStack(Items.ECHO_SHARD);
        crafts(helper, List.of(echo, echo, echo, echo, compass, echo, echo, echo, echo), Items.RECOVERY_COMPASS);
        // der Drachenkompass nimmt den Wegkompass (und weiter einen alten Kompass aus einer Truhe)
        ItemStack draconium = new ItemStack(ModItems.DRACONIUM.get());
        ItemStack gem = new ItemStack(ModItems.GEMSTONE.get());
        crafts(helper, List.of(e, draconium, e, gem, compass, gem, e, aetherit, e), ModItems.DRAGON_COMPASS.get());
        crafts(helper, List.of(e, draconium, e, gem, new ItemStack(Items.COMPASS), gem, e, aetherit, e), ModItems.DRAGON_COMPASS.get());
        // Texte
        JsonObject german = lang(helper, "de_de");
        JsonObject english = lang(helper, "en_us");
        List<String> keys = new ArrayList<>(List.of("item.kingdomsatwar.waystone_compass", "block.kingdomsatwar.waystone",
                "block.kingdomsatwar.waystone.named", "lore.kingdomsatwar.waystone.1", "lore.kingdomsatwar.waystone.2",
                "item.kingdomsatwar.waystone_compass.count", "item.kingdomsatwar.waystone_compass.target", "item.kingdomsatwar.waystone_compass.spawn",
                "item.kingdomsatwar.waystone_compass.hint"));
        for (String m : List.of("saved", "full", "target", "spawn", "forgotten", "gone", "renamed", "info")) {
            keys.add("message.kingdomsatwar.waystone." + m);
        }
        for (String g : List.of("title", "count", "north", "east", "south", "west", "free", "free_hint", "here", "distance", "distance_short",
                "dimension.overworld", "dimension.nether", "dimension.end", "dimension.other", "click_select", "click_clear", "click_forget",
                "forget_confirm", "clear", "clear_hint", "no_target", "to_spawn", "spinning", "hint1", "hint2")) {
            keys.add("gui.kingdomsatwar.waystone_compass." + g);
        }
        for (String d : List.of("north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest")) {
            keys.add("direction.kingdomsatwar.short." + d);
            keys.add("direction.kingdomsatwar." + d);
        }
        for (String key : keys) {
            helper.assertTrue(german.has(key) && english.has(key), "Text fehlt: " + key);
        }
        helper.assertTrue(german.get("item.kingdomsatwar.waystone_compass").getAsString().equals("Wegkompass"), "Deutscher Name des Wegkompasses");
        helper.succeed();
    }

    private static void crafts(GameTestHelper helper, List<ItemStack> grid, Item expected) {
        CraftingInput input = CraftingInput.of(3, 3, grid);
        ItemStack result = helper.getLevel().getServer().getRecipeManager().getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel())
                .map(holder -> holder.value().assemble(input)).orElse(ItemStack.EMPTY);
        String what = grid.stream().map(stack -> stack.getItem().toString()).toList().toString();
        helper.assertTrue(expected == Items.AIR ? result.isEmpty() : result.is(expected), "Werktisch " + what + " ergab " + result + " statt " + expected);
    }

    private static JsonObject lang(GameTestHelper helper, String name) {
        try (InputStream in = WaystoneGameTests.class.getResourceAsStream("/assets/" + KingdomsAtWar.MODID + "/lang/" + name + ".json")) {
            helper.assertTrue(in != null, "Sprachdatei fehlt: " + name);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException ex) {
            throw new IllegalStateException(ex);
        }
    }

    // =====================================================================================
    // Der Kompass
    // =====================================================================================

    private static WaystoneBlockEntity place(GameTestHelper helper, BlockPos relative, String name) {
        BlockPos pos = helper.absolutePos(relative);
        helper.getLevel().setBlockAndUpdate(pos, ModBlocks.WAYSTONE.get().defaultBlockState());
        WaystoneBlockEntity stone = (WaystoneBlockEntity) helper.getLevel().getBlockEntity(pos);
        helper.assertTrue(stone != null, "Kein Wegstein bei " + pos);
        stone.setName(Component.literal(name));
        return stone;
    }

    private static GlobalPos at(GameTestHelper helper, WaystoneBlockEntity stone) {
        return GlobalPos.of(helper.getLevel().dimension(), stone.getBlockPos());
    }

    /** Der Kompass zeigt auf {@code expected} (null: kein Ziel, also kein {@code lodestone_tracker} – die Nadel zeigt zum Spawn). */
    private static void aims(GameTestHelper helper, ItemStack stack, @Nullable GlobalPos expected, String when) {
        LodestoneTracker tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        Waypoints.Waypoint target = WaystoneCompass.of(stack).target();
        if (expected == null) {
            helper.assertTrue(tracker == null && target == null, when + ": Der Kompass hat noch ein Ziel: " + tracker + " / " + target);
        } else {
            helper.assertTrue(tracker != null && tracker.target().isPresent() && tracker.target().get().equals(expected) && !tracker.tracked(),
                    when + ": Die Nadel zeigt nicht auf " + expected + " sondern " + tracker);
            helper.assertTrue(target != null && target.pos().equals(expected), when + ": Falsches Ziel " + target);
        }
    }

    /**
     * Merken (bis zu acht, ein bekannter Wegstein wird nur Ziel und lernt seinen neuen Namen), wählen (noch einmal: kein Ziel),
     * vergessen (das Ziel rückt mit), zurück zum Spawn, ein verschwundener Wegstein wird beim Wählen vergessen; die Daten
     * kommen heil durch Speichern und Netz.
     */
    private static void compass(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        try {
            ItemStack stack = new ItemStack(ModItems.WAYSTONE_COMPASS.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, stack);
            List<WaystoneBlockEntity> stones = new ArrayList<>();
            for (int i = 0; i < 9; i++) {
                stones.add(place(helper, new BlockPos(1 + i % 5, 1, 2 + i / 5 * 2), "Stein " + i));
            }
            aims(helper, stack, null, "Neu");
            WaystoneCompass.remember(player, stack, level, stones.get(0).getBlockPos(), stones.get(0));
            Waypoints waypoints = WaystoneCompass.of(stack);
            helper.assertTrue(waypoints.entries().size() == 1 && waypoints.selected() == 0, "Erster Wegstein: " + waypoints);
            aims(helper, stack, at(helper, stones.get(0)), "Erster Wegstein");
            // schon bekannt: kein zweiter Eintrag, aber der neue Name
            stones.get(0).setName(Component.literal("Heimat"));
            WaystoneCompass.remember(player, stack, level, stones.get(0).getBlockPos(), stones.get(0));
            waypoints = WaystoneCompass.of(stack);
            helper.assertTrue(waypoints.entries().size() == 1 && waypoints.entries().getFirst().name().getString().equals("Heimat"),
                    "Bekannter Wegstein: " + waypoints);
            for (int i = 1; i < 9; i++) {
                WaystoneCompass.remember(player, stack, level, stones.get(i).getBlockPos(), stones.get(i));
            }
            waypoints = WaystoneCompass.of(stack);
            helper.assertTrue(waypoints.entries().size() == Waypoints.MAX && waypoints.full(), "Mehr als acht oder weniger: " + waypoints.entries().size());
            helper.assertTrue(waypoints.indexOf(at(helper, stones.get(8))) < 0, "Der neunte Wegstein passt noch hinein");
            aims(helper, stack, at(helper, stones.get(7)), "Der zuletzt gemerkte");
            // wählen und abwählen
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, 2);
            aims(helper, stack, at(helper, stones.get(2)), "Gewählt");
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, 2);
            aims(helper, stack, null, "Abgewählt");
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, 99);
            aims(helper, stack, null, "Unsinnige Wahl");
            // vergessen: das Ziel rückt mit
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, 5);
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.REMOVE, 1);
            waypoints = WaystoneCompass.of(stack);
            helper.assertTrue(waypoints.entries().size() == 7 && waypoints.selected() == 4, "Nach dem Vergessen: " + waypoints.selected());
            aims(helper, stack, at(helper, stones.get(5)), "Ziel nach dem Vergessen");
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.REMOVE, 4);
            aims(helper, stack, null, "Ziel vergessen");
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, 0);
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.CLEAR, 0);
            aims(helper, stack, null, "Zurück zum Spawn");
            // ein Wegstein, der nicht mehr steht, wird beim Wählen vergessen
            int index = WaystoneCompass.of(stack).indexOf(at(helper, stones.get(3)));
            helper.assertTrue(index >= 0, "Stein 3 fehlt");
            level.setBlockAndUpdate(stones.get(3).getBlockPos(), Blocks.AIR.defaultBlockState());
            int before = WaystoneCompass.of(stack).entries().size();
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, index);
            waypoints = WaystoneCompass.of(stack);
            helper.assertTrue(waypoints.entries().size() == before - 1 && waypoints.indexOf(at(helper, stones.get(3))) < 0,
                    "Der verschwundene Wegstein wurde nicht vergessen");
            aims(helper, stack, null, "Verschwundenes Ziel");
            // die andere Hand; etwas anderes in der Hand tut nichts
            player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
            player.setItemInHand(InteractionHand.OFF_HAND, stack);
            WaystoneCompass.act(player, InteractionHand.MAIN_HAND, WaystoneCompass.SELECT, 0);
            aims(helper, stack, null, "Stock in der Hand");
            WaystoneCompass.act(player, InteractionHand.OFF_HAND, WaystoneCompass.SELECT, 0);
            aims(helper, stack, waypoints.entries().getFirst().pos(), "Andere Hand");
            // Speichern und Netz
            Waypoints saved = WaystoneCompass.of(stack);
            var ops = level.registryAccess().createSerializationContext(JsonOps.INSTANCE);
            Waypoints loaded = Waypoints.CODEC.encodeStart(ops, saved).flatMap(json -> Waypoints.CODEC.parse(ops, json)).getOrThrow();
            helper.assertTrue(loaded.equals(saved), "Gespeichert und geladen: " + loaded + " statt " + saved);
            RegistryFriendlyByteBuf buffer = new RegistryFriendlyByteBuf(Unpooled.buffer(), level.registryAccess());
            Waypoints.STREAM_CODEC.encode(buffer, saved);
            Waypoints received = Waypoints.STREAM_CODEC.decode(buffer);
            helper.assertTrue(received.equals(saved) && buffer.readableBytes() == 0, "Über das Netz: " + received);
            helper.assertTrue(stack.has(ModDataComponents.WAYPOINTS.get()), "Der Kompass trägt seine Wegsteine nicht");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // =====================================================================================
    // Der Wegstein
    // =====================================================================================

    /**
     * Ungenannt heißt er nach seinem Biom (und vergisst den Namen beim Abbauen), ein Namensschild tauft ihn (der Name bleibt
     * am Gegenstand), mit dem Kompass angeklickt merkt der Kompass ihn sich; er leuchtet und widersteht Explosionen.
     */
    private static void stone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        try {
            BlockPos pos = helper.absolutePos(new BlockPos(3, 1, 3));
            Block block = ModBlocks.WAYSTONE.get();
            BlockState state = block.defaultBlockState();
            level.setBlockAndUpdate(pos, state);
            block.setPlacedBy(level, pos, state, player, new ItemStack(block));
            WaystoneBlockEntity stone = (WaystoneBlockEntity) level.getBlockEntity(pos);
            helper.assertTrue(stone != null && WaystoneBlockEntity.isAutoName(stone.getCustomName()), "Kein Name nach dem Biom: "
                    + (stone == null ? null : stone.getCustomName()));
            List<ItemStack> drops = Block.getDrops(state, level, pos, stone);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().is(block.asItem()) && !drops.getFirst().has(DataComponents.CUSTOM_NAME),
                    "Abgebaut behält er den Namen nach dem Biom: " + drops);
            // ein Namensschild tauft ihn
            ItemStack tag = new ItemStack(Items.NAME_TAG);
            tag.set(DataComponents.CUSTOM_NAME, Component.literal("Rabenfels"));
            player.setItemInHand(InteractionHand.MAIN_HAND, tag);
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            state.useItemOn(tag, level, player, InteractionHand.MAIN_HAND, hit);
            helper.assertTrue(stone.getName().getString().equals("Rabenfels") && tag.isEmpty(), "Das Namensschild tauft ihn nicht: " + stone.getName());
            drops = Block.getDrops(state, level, pos, stone);
            helper.assertTrue(drops.size() == 1 && drops.getFirst().has(DataComponents.CUSTOM_NAME)
                    && drops.getFirst().get(DataComponents.CUSTOM_NAME).getString().equals("Rabenfels"), "Der Name bleibt nicht am Gegenstand: " + drops);
            // mit dem Kompass angeklickt
            ItemStack compass = new ItemStack(ModItems.WAYSTONE_COMPASS.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, compass);
            state.useItemOn(compass, level, player, InteractionHand.MAIN_HAND, hit);
            Waypoints waypoints = WaystoneCompass.of(compass);
            helper.assertTrue(waypoints.entries().size() == 1 && waypoints.entries().getFirst().name().getString().equals("Rabenfels"),
                    "Der Kompass merkt sich den Wegstein nicht: " + waypoints);
            aims(helper, compass, GlobalPos.of(level.dimension(), pos), "Angeklickt");
            // Licht und Härte
            helper.assertTrue(state.getLightEmission() == 6, "Der Wegstein leuchtet nicht: " + state.getLightEmission());
            helper.assertTrue(block.getExplosionResistance() >= 1200.0f, "Der Wegstein widersteht keiner Explosion");
            helper.assertTrue(state.requiresCorrectToolForDrops(), "Der Wegstein braucht keine Spitzhacke");
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }
}
