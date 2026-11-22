package io.github.sirschrolli.kingdomsatwar.gametest;

import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.fishing.FishLog;
import io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbilities;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.TradePayload;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import java.lang.reflect.Constructor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.RecordComponent;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * Mehrspieler (eigener Server, LAN, eine mit Essential geöffnete Welt): Was übers Netz geht, kommt unverändert an. Im
 * Einzelspieler werden Nachrichten nie verpackt – Spiel und Server teilen sich den Speicher –, ein Fehler in einem Codec
 * fiele dort nicht auf. Hier wird alles mit Beispielwerten verpackt, ausgepackt und wieder verpackt: Nichts darf übrig
 * bleiben, und beide Male müssen genau dieselben Bytes entstehen (so fallen auch vertauschte Felder auf – jede Zahl und
 * jeder Text der Beispiele ist anders).
 */
final class NetworkGameTests {
    private static final Logger LOGGER = LogUtils.getLogger();
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PAYLOADS =
            ModGameTests.TEST_FUNCTIONS.register("network_payloads", () -> NetworkGameTests::payloads);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ITEMS =
            ModGameTests.TEST_FUNCTIONS.register("network_items", () -> NetworkGameTests::items);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACHMENTS =
            ModGameTests.TEST_FUNCTIONS.register("network_attachments", () -> NetworkGameTests::attachments);

    /** Nachrichten, deren Kennung nicht dem Namen der Klasse folgt. */
    private static final Map<String, String> CLASS_NAMES = Map.of("heal_camp", "HealPayload", "provisions", "ProvisionPayload");

    private NetworkGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(PAYLOADS, ITEMS, ATTACHMENTS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    /** Jede angemeldete Nachricht – in beide Richtungen – mit Beispielwerten in allen Feldern. */
    private static void payloads(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        List<ModNetwork.Registered> all = ModNetwork.registered();
        helper.assertTrue(all.size() >= 43, "Zu wenige Nachrichten angemeldet: " + all.size());
        List<String> problems = new ArrayList<>();
        for (ModNetwork.Registered message : all) {
            String id = message.type().id().getPath();
            Object sample;
            try {
                Class<?> type = Class.forName(ModNetwork.class.getPackageName() + "." + CLASS_NAMES.getOrDefault(id, camel(id) + "Payload"));
                if (type.getField("TYPE").get(null) != message.type()) {
                    problems.add(id + ": Klasse " + type.getSimpleName() + " gehört zu einer anderen Nachricht");
                    continue;
                }
                sample = new Samples().make(type);
            } catch (ReflectiveOperationException | RuntimeException e) {
                problems.add(id + ": kein Beispiel (" + e + ")");
                continue;
            }
            String problem = roundTrip(registries, message.codec(), sample);
            if (problem != null) {
                problems.add(id + (message.toServer() ? " (an den Server)" : " (an das Spiel)") + ": " + problem);
            }
        }
        helper.assertTrue(problems.isEmpty(), "Nachrichten kommen nicht heil an: " + String.join(" | ", problems));
        // Selbstprüfung: Ein Codec, der zwei Felder vertauscht, und einer, der ein Feld nicht liest, müssen auffallen
        StreamCodec<RegistryFriendlyByteBuf, TradePayload> swapped = StreamCodec.composite(ByteBufCodecs.VAR_INT, TradePayload::entity,
                ByteBufCodecs.VAR_INT, TradePayload::offer, (first, second) -> new TradePayload(second, first));
        StreamCodec<RegistryFriendlyByteBuf, TradePayload> lazy = StreamCodec.of((buf, payload) -> {
            buf.writeVarInt(payload.entity());
            buf.writeVarInt(payload.offer());
        }, buf -> new TradePayload(buf.readVarInt(), 0));
        Object probe = new Samples().make(TradePayload.class);
        helper.assertTrue(roundTrip(registries, swapped, probe) != null && roundTrip(registries, lazy, probe) != null,
                "Selbstprüfung: vertauschte oder ungelesene Felder fallen nicht auf");
        LOGGER.info("[Netz] {} Nachrichten verpackt, ausgepackt und wieder verpackt", all.size());
        helper.succeed();
    }

    /**
     * Jeder Gegenstand der Mod, wie er frisch entsteht, und jeder aus den Reitern der Mod (manche tragen dort schon Daten):
     * Ihre Datenkomponenten gehen beim Füllen des Inventars übers Netz.
     */
    private static void items(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        List<ItemStack> stacks = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            if (BuiltInRegistries.ITEM.getKey(item).getNamespace().equals(KingdomsAtWar.MODID)) {
                stacks.add(new ItemStack(item));
            }
        }
        int plain = stacks.size();
        CreativeModeTabs.tryRebuildTabContents(FeatureFlags.DEFAULT_FLAGS, true, registries);
        for (CreativeModeTab tab : BuiltInRegistries.CREATIVE_MODE_TAB) {
            Identifier id = BuiltInRegistries.CREATIVE_MODE_TAB.getKey(tab);
            if (id != null && id.getNamespace().equals(KingdomsAtWar.MODID)) {
                stacks.addAll(tab.getDisplayItems());
            }
        }
        helper.assertTrue(plain > 300, "Zu wenige Gegenstände der Mod gefunden: " + plain);
        helper.assertTrue(stacks.size() > plain, "Die Reiter der Mod sind leer");
        List<String> problems = new ArrayList<>();
        for (ItemStack stack : stacks) {
            String problem = roundTrip(registries, ItemStack.OPTIONAL_STREAM_CODEC, stack);
            if (problem != null && problems.size() < 20) {
                problems.add(BuiltInRegistries.ITEM.getKey(stack.getItem()) + ": " + problem);
            }
        }
        helper.assertTrue(problems.isEmpty(), "Gegenstände kommen nicht heil an: " + String.join(" | ", problems));
        LOGGER.info("[Netz] {} Gegenstände geprüft ({} aus den Reitern)", stacks.size(), stacks.size() - plain);
        helper.succeed();
    }

    /** Was das Spiel des Spielers von seinen Daten zugeschickt bekommt: Held, Fähigkeiten, Kristalle (leer und gefüllt). */
    private static void attachments(GameTestHelper helper) {
        RegistryAccess registries = helper.getLevel().registryAccess();
        List<String> problems = new ArrayList<>();
        check(problems, "Held (leer)", registries, HeroLevel.Hero.STREAM_CODEC, HeroLevel.Hero.NONE);
        check(problems, "Held", registries, HeroLevel.Hero.STREAM_CODEC, new Samples().make(HeroLevel.Hero.class));
        check(problems, "Fähigkeiten (leer)", registries, HeroAbilities.State.STREAM_CODEC, HeroAbilities.State.NONE);
        check(problems, "Fähigkeiten", registries, HeroAbilities.State.STREAM_CODEC, new Samples().make(HeroAbilities.State.class));
        check(problems, "Kristalle (leer)", registries, Crystals.Count.STREAM_CODEC, Crystals.Count.NONE);
        check(problems, "Kristalle", registries, Crystals.Count.STREAM_CODEC, new Samples().make(Crystals.Count.class));
        check(problems, "Fangbuch (leer)", registries, FishLog.STREAM_CODEC, new FishLog());
        check(problems, "Fangbuch", registries, FishLog.STREAM_CODEC, new Samples().make(FishLog.class));
        helper.assertTrue(problems.isEmpty(), "Daten des Spielers kommen nicht heil an: " + String.join(" | ", problems));
        helper.succeed();
    }

    private static void check(List<String> problems, String what, RegistryAccess registries, StreamCodec<? super RegistryFriendlyByteBuf, ?> codec,
            Object value) {
        String problem = roundTrip(registries, codec, value);
        if (problem != null) {
            problems.add(what + ": " + problem);
        }
    }

    /** Verpackt, packt aus und verpackt wieder – {@code null}, wenn alles stimmt, sonst was nicht stimmt. */
    @SuppressWarnings("unchecked")
    static @Nullable String roundTrip(RegistryAccess registries, StreamCodec<? super RegistryFriendlyByteBuf, ?> rawCodec, Object value) {
        StreamCodec<RegistryFriendlyByteBuf, Object> codec = (StreamCodec<RegistryFriendlyByteBuf, Object>) rawCodec;
        RegistryFriendlyByteBuf first = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        RegistryFriendlyByteBuf second = new RegistryFriendlyByteBuf(Unpooled.buffer(), registries);
        try {
            codec.encode(first, value);
            byte[] bytes = ByteBufUtil.getBytes(first);
            Object copy = codec.decode(first);
            if (first.readableBytes() != 0) {
                return first.readableBytes() + " von " + bytes.length + " Bytes werden nicht gelesen";
            }
            codec.encode(second, copy);
            if (!Arrays.equals(bytes, ByteBufUtil.getBytes(second))) {
                return "kommt verändert an: " + shorten(value) + " → " + shorten(copy);
            }
            return null;
        } catch (RuntimeException e) {
            return e.toString();
        } finally {
            first.release();
            second.release();
        }
    }

    private static String shorten(Object value) {
        String text = String.valueOf(value);
        return text.length() > 240 ? text.substring(0, 240) + "…" : text;
    }

    private static String camel(String id) {
        StringBuilder name = new StringBuilder();
        for (String part : id.split("_")) {
            if (!part.isEmpty()) {
                name.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
            }
        }
        return name.toString();
    }

    /** Beispielwerte: jede Zahl, jeder Text anders (mit Umlauten), Listen mit zwei Einträgen, Optionen gefüllt. */
    static final class Samples {
        private int next = 1;

        Object make(Type type) {
            return this.make(type, 0);
        }

        private Object make(Type type, int depth) {
            if (depth > 8) {
                throw new IllegalArgumentException("zu tief verschachtelt");
            }
            if (type instanceof WildcardType wildcard) {
                return this.make(wildcard.getUpperBounds()[0], depth);
            }
            if (type instanceof ParameterizedType parameterized) {
                Class<?> raw = (Class<?>) parameterized.getRawType();
                Type[] args = parameterized.getActualTypeArguments();
                if (List.class.isAssignableFrom(raw)) {
                    return List.of(this.make(args[0], depth + 1), this.make(args[0], depth + 1));
                }
                if (raw == Optional.class) {
                    return Optional.of(this.make(args[0], depth + 1));
                }
                if (Set.class.isAssignableFrom(raw)) {
                    Set<Object> set = new LinkedHashSet<>();
                    set.add(this.make(args[0], depth + 1));
                    return set;
                }
                if (Map.class.isAssignableFrom(raw)) {
                    Map<Object, Object> map = new LinkedHashMap<>();
                    map.put(this.make(args[0], depth + 1), this.make(args[1], depth + 1));
                    return map;
                }
                return this.make(raw, depth);
            }
            Class<?> c = (Class<?>) type;
            int n = this.next++;
            if (c == int.class || c == Integer.class) {
                return n;
            }
            if (c == long.class || c == Long.class) {
                return n * 1_000_003L;
            }
            if (c == short.class || c == Short.class) {
                return (short) n;
            }
            if (c == byte.class || c == Byte.class) {
                return (byte) n;
            }
            if (c == boolean.class || c == Boolean.class) {
                return n % 2 == 1;
            }
            if (c == float.class || c == Float.class) {
                return n + 0.5f;
            }
            if (c == double.class || c == Double.class) {
                return n + 0.25;
            }
            if (c == String.class) {
                return "Prüfstück " + n + " – Größe";
            }
            if (c == byte[].class) {
                return new byte[] {(byte) n, 7, 0, -3};
            }
            if (c == int[].class) {
                return new int[] {n, 7};
            }
            if (c == UUID.class) {
                return new UUID(n, 0x5EEDL);
            }
            if (c == BlockPos.class) {
                return new BlockPos(n, 60 + n, -n);
            }
            if (c == Vec3.class) {
                return new Vec3(n + 0.5, 64.0, -n);
            }
            if (c.isEnum()) {
                Object[] values = c.getEnumConstants();
                return values[n % values.length];
            }
            if (c == Component.class || c == MutableComponent.class) {
                return Component.literal("Name " + n);
            }
            if (c == ItemStack.class) {
                return new ItemStack(Items.GOLDEN_APPLE, 1 + n % 16);
            }
            if (c == Item.class) {
                return Items.EMERALD;
            }
            if (c == FishLog.class) {
                FishLog log = new FishLog();
                List<FishSpecies> species = FishSpecies.values();
                log.record(species.get(n % species.size()), 20 + n);
                log.record(species.get((n + 1) % species.size()), 31 + n);
                log.addTreasure();
                return log;
            }
            if (c.isRecord()) {
                RecordComponent[] parts = c.getRecordComponents();
                Object[] values = new Object[parts.length];
                Class<?>[] types = new Class<?>[parts.length];
                for (int i = 0; i < parts.length; i++) {
                    values[i] = this.make(parts[i].getGenericType(), depth + 1);
                    types[i] = parts[i].getType();
                }
                try {
                    Constructor<?> constructor = c.getDeclaredConstructor(types);
                    constructor.setAccessible(true);
                    return constructor.newInstance(values);
                } catch (ReflectiveOperationException e) {
                    throw new IllegalArgumentException(c.getSimpleName() + ": " + (e.getCause() != null ? e.getCause() : e));
                }
            }
            throw new IllegalArgumentException("kein Beispiel für " + c.getName());
        }
    }
}
