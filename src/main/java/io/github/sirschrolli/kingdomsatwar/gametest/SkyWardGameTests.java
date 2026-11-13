package io.github.sirschrolli.kingdomsatwar.gametest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.FlightBan;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyBoltEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWard;
import io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import io.netty.channel.embedded.EmbeddedChannel;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForgeMod;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Prüft die Himmelswacht der Bosse (seit 0.82): Jeder Boss hat sein Thema; der Flugbann schaltet das Gleiten ab, dämpft
 * den Sturz, lässt sich nicht wegtrinken und ist mit guten Schwingen kürzer. Ein Boss nimmt einen Flieger nach zwei
 * Sekunden ins Visier, lädt sein Siegel und holt ihn mit dem Bannpfeil herunter; wer rechtzeitig landet, dem geschieht
 * nichts – das Siegel erlischt, und ein Pfeil, der schon unterwegs ist, verliert ihn. An Blöcken zerschellt der Pfeil;
 * Annis Pfeil stellt den Flieger auf den Boden zurück.
 */
final class SkyWardGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RULES =
            ModGameTests.TEST_FUNCTIONS.register("sky_ward_rules", () -> SkyWardGameTests::rules);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOLT =
            ModGameTests.TEST_FUNCTIONS.register("sky_ward_bolt", () -> SkyWardGameTests::bolt);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LANDING =
            ModGameTests.TEST_FUNCTIONS.register("sky_ward_landing", () -> SkyWardGameTests::landing);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> RIDERS =
            ModGameTests.TEST_FUNCTIONS.register("sky_ward_riders", () -> SkyWardGameTests::riders);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INTERCEPT =
            ModGameTests.TEST_FUNCTIONS.register("sky_ward_intercept", () -> SkyWardGameTests::intercept);

    private SkyWardGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(RULES.getId(), new FunctionGameTestInstance(RULES.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(BOLT.getId(), new FunctionGameTestInstance(BOLT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(LANDING.getId(), new FunctionGameTestInstance(LANDING.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        // die Flieger gleiten bis 16 Blöcke vor den Platz und 30 hoch: Abstand, damit kein Nachbartest den Pfeil aufhält
        event.registerTest(RIDERS.getId(), new FunctionGameTestInstance(RIDERS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 300, 0, true, Rotation.NONE, false, 1, 1, true, 20)));
        event.registerTest(INTERCEPT.getId(), new FunctionGameTestInstance(INTERCEPT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 340, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static JsonObject lang(GameTestHelper helper, String name) {
        try (InputStream in = SkyWardGameTests.class.getResourceAsStream("/assets/" + KingdomsAtWar.MODID + "/lang/" + name + ".json")) {
            helper.assertTrue(in != null, "Sprachdatei fehlt: " + name);
            return JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean exists(String path) {
        return SkyWardGameTests.class.getResource(path) != null;
    }

    /** Ein Spieler im Überlebensmodus, verwundbar, der fallen kann (die Testwelt läuft im Kreativmodus). */
    private static ServerPlayer flyer(GameTestHelper helper, String name) {
        keepTicking(helper, -32, 48, -40, 48);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, name, new EmbeddedChannel[1], 0);
        player.connection.markClientLoaded();
        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        player.getAbilities().mayfly = false;
        player.getAbilities().flying = false;
        return player;
    }

    /**
     * Hält die Chunks rund um den Test am Laufen (Koordinaten des Tests): Flieger und Pfeile verlassen den kleinen Test-Bereich
     * weit, und draußen blieben Wesen sonst stehen – ein Pfeil hielt dann mitten im Flug an der Grenze zum nächsten Chunk an.
     * Die Chunks gibt der Testlauf am Ende des Durchgangs wieder frei.
     */
    private static void keepTicking(GameTestHelper helper, int minX, int maxX, int minZ, int maxZ) {
        ServerLevel level = helper.getLevel();
        BlockPos a = helper.absolutePos(new BlockPos(minX, 0, minZ));
        BlockPos b = helper.absolutePos(new BlockPos(maxX, 0, maxZ));
        for (int cx = Math.min(a.getX(), b.getX()) >> 4; cx <= Math.max(a.getX(), b.getX()) >> 4; cx++) {
            for (int cz = Math.min(a.getZ(), b.getZ()) >> 4; cz <= Math.max(a.getZ(), b.getZ()) >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
            }
        }
    }

    /**
     * Lässt den Spieler an dieser Stelle (in Koordinaten des Tests) mit diesen Schwingen gleiten. Er meldet sich dort auch
     * an (wie es sonst seine Verbindung jeden Tick tut): nur um Spieler herum laufen die Wesen weiter – sonst bliebe ein
     * Pfeil, der den Test-Bereich verlässt, an der Grenze zum nächsten Chunk stehen.
     */
    private static void glide(GameTestHelper helper, ServerPlayer player, Vec3 at, ItemStack wings) {
        player.setItemSlot(EquipmentSlot.CHEST, wings);
        Vec3 pos = helper.absoluteVec(at);
        player.snapTo(pos.x, pos.y, pos.z, 0.0f, 0.0f);
        player.level().getChunkSource().move(player);
        player.setDeltaMovement(Vec3.ZERO);
        player.startFallFlying();
    }

    /** Ein Feuermagier ohne eigenen Willen in der Mitte des Tests – er wacht nur über {@code player}. */
    private static Mob warden(GameTestHelper helper, ServerPlayer player, Vec3 at) {
        ServerLevel level = helper.getLevel();
        Mob boss = ModEntities.MAGES.get(DragonType.FIRE).get().create(level, EntitySpawnReason.COMMAND);
        helper.assertTrue(boss != null, "Der Feuermagier lässt sich nicht erschaffen");
        Vec3 pos = helper.absoluteVec(at);
        boss.snapTo(pos.x, pos.y, pos.z, 0.0f, 0.0f);
        boss.setNoAi(true);
        boss.setPersistenceRequired();
        level.addFreshEntity(boss);
        SkyWard.forceOnly(boss, player);
        return boss;
    }

    private static void cleanup(ServerLevel level, ServerPlayer player, Mob... bosses) {
        for (Mob boss : bosses) {
            SkyWard.force(boss, false);
            boss.discard();
        }
        level.getServer().getPlayerList().remove(player);
    }

    // =====================================================================================
    // Regeln: Themen, Flugbann, Schwingen, Texte und Dateien
    // =====================================================================================

    private static void rules(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // jeder Boss hat sein Thema
        Map<EntityType<?>, SkyTheme> expected = new LinkedHashMap<>();
        expected.put(ModEntities.PYRRHON.get(), SkyTheme.FIRE);
        expected.put(ModEntities.GLACIARA.get(), SkyTheme.ICE);
        expected.put(ModEntities.VOLTARION.get(), SkyTheme.LIGHTNING);
        expected.put(ModEntities.VERDANTIS.get(), SkyTheme.NATURE);
        expected.put(ModEntities.TERRAGON.get(), SkyTheme.EARTH);
        expected.put(ModEntities.THALASSA.get(), SkyTheme.WATER);
        expected.put(ModEntities.NYXAR.get(), SkyTheme.SHADOW);
        for (DragonType element : DragonType.values()) {
            expected.put(ModEntities.MAGES.get(element).get(), SkyTheme.of(element));
        }
        expected.put(ModEntities.REEF_COLOSSUS.get(), SkyTheme.REEF);
        expected.put(ModEntities.MOTH_QUEEN.get(), SkyTheme.MOTH);
        expected.put(ModEntities.STONE_KEEPER.get(), SkyTheme.KEEPER);
        expected.put(ModEntities.SEA_WITCH.get(), SkyTheme.REEF);
        expected.put(ModEntities.SOUL_SHEPHERD.get(), SkyTheme.ASH);
        expected.put(ModEntities.STAR_WEAVER.get(), SkyTheme.STAR);
        expected.put(ModEntities.HOUR_WARDEN.get(), SkyTheme.TIME);
        expected.put(ModEntities.WITHER.get(), SkyTheme.ASH);
        expected.put(ModEntities.MISS_MYSTIK.get(), SkyTheme.MYSTIK);
        expected.put(ModEntities.GLUTSCHMIED.get(), SkyTheme.FORGE);
        expected.put(ModEntities.IGNARA.get(), SkyTheme.PRIEST);
        expected.put(ModEntities.PHASMOR.get(), SkyTheme.PHASE);
        expected.put(ModEntities.SORDIN.get(), SkyTheme.ECHO);
        expected.put(ModEntities.ROBBER_KNIGHT.get(), SkyTheme.RAVEN);
        expected.put(ModEntities.ASTERIA.get(), SkyTheme.STAR);
        expected.put(net.minecraft.world.entity.EntityTypes.ENDER_DRAGON, SkyTheme.STAR);
        expected.put(ModEntities.ANNI_TROLL.get(), SkyTheme.GLITCH);
        expected.put(ModEntities.SHATTERED_KING.get(), SkyTheme.VOID);
        expected.put(ModEntities.WORLD_EATER.get(), SkyTheme.MAW);
        expected.put(ModEntities.SCHROLLI.get(), SkyTheme.CREATOR);
        java.util.EnumSet<SkyTheme> used = java.util.EnumSet.noneOf(SkyTheme.class);
        for (Map.Entry<EntityType<?>, SkyTheme> entry : expected.entrySet()) {
            Entity boss = entry.getKey().create(level, EntitySpawnReason.COMMAND);
            helper.assertTrue(boss != null, "Boss lässt sich nicht erschaffen: " + entry.getKey());
            SkyTheme theme = SkyWard.themeOf(boss);
            helper.assertTrue(theme == entry.getValue(), "Falsche Himmelswacht bei " + entry.getKey() + ": " + theme);
            if (boss instanceof SkyWarden warden) {
                helper.assertFalse(warden.guardsSky(), "Ein Boss ohne Gegner wacht über den Himmel: " + entry.getKey());
            }
            used.add(theme);
            boss.discard();
        }
        helper.assertTrue(used.size() == SkyTheme.values().length, "Nicht jedes Thema hat einen Boss: " + used);
        helper.assertTrue(SkyWard.themeOf(net.minecraft.world.entity.EntityTypes.ZOMBIE.create(level, EntitySpawnReason.COMMAND)) == null, "Ein Zombie wacht über den Himmel");
        // die Stufen: Schaden 8/10/12, Bann 8/10/12 Sekunden, schneller und wendiger
        for (SkyTheme theme : SkyTheme.values()) {
            helper.assertTrue(theme.tier >= 1 && theme.tier <= 3 && theme.particle() != null, "Thema unvollständig: " + theme);
            helper.assertTrue(Math.abs(theme.damage() - (6.0f + 2.0f * theme.tier)) < 0.01f && theme.banTicks() == 120 + 40 * theme.tier,
                    "Falsche Werte: " + theme);
            helper.assertTrue(theme.speed() > 1.6 && theme.speed() < 2.4 && theme.turn() > 0.09 && theme.turn() < 0.14, "Falscher Flug: " + theme);
        }
        helper.assertTrue(SkyTheme.CREATOR.speed() > SkyTheme.FIRE.speed() && SkyTheme.CREATOR.turn() > SkyTheme.FIRE.turn(),
                "Der Pfeil des Schöpfers ist nicht schneller und wendiger");
        // die Wende: höchstens so scharf wie erlaubt, nie ohne Richtung
        Vec3 turned = SkyBoltEntity.turnToward(new Vec3(1, 0, 0), new Vec3(0, 0, 1), Math.toRadians(10.0));
        helper.assertTrue(Math.abs(turned.length() - 1.0) < 1.0e-6 && Math.abs(Math.toDegrees(Math.acos(turned.dot(new Vec3(1, 0, 0)))) - 10.0) < 0.01,
                "Der Pfeil wendet falsch: " + turned);
        helper.assertTrue(SkyBoltEntity.turnToward(new Vec3(1, 0, 0), new Vec3(0.99, 0.141, 0).normalize(), 0.3).y > 0.13, "Kleine Wenden gelingen nicht ganz");
        Vec3 back = SkyBoltEntity.turnToward(new Vec3(1, 0, 0), new Vec3(-1, 0, 0), 0.1);
        helper.assertTrue(Math.abs(back.length() - 1.0) < 1.0e-6 && back.x < 1.0 && back.x > 0.9, "Die Kehrtwende entgleist: " + back);

        // der Flugbann
        ServerPlayer player = flyer(helper, "test-flight-ban");
        try {
            AttributeInstance gliding = player.getAttribute(NeoForgeMod.GLIDING_FLIGHT);
            helper.assertTrue(gliding != null, "Dem Spieler fehlt das Merkmal Gleitflug");
            Identifier testWings = KingdomsAtWar.id("test_wings");
            gliding.addTransientModifier(new AttributeModifier(testWings, 1.0, AttributeModifier.Operation.ADD_VALUE));
            helper.assertTrue(player.getAttributeValue(NeoForgeMod.GLIDING_FLIGHT) > 0.0, "Mit Schwingen gleitet der Spieler nicht");
            glide(helper, player, new Vec3(4.5, 8.0, 4.5), new ItemStack(Items.ELYTRA));
            int ticks = FlightBan.apply(player, 200, null);
            helper.assertTrue(ticks == 200, "Falsche Dauer des Flugbanns: " + ticks);
            MobEffectInstance ban = player.getEffect(ModEffects.FLIGHT_BAN);
            helper.assertTrue(ban != null && ban.getDuration() == 200 && FlightBan.banned(player), "Der Flugbann wirkt nicht");
            helper.assertFalse(player.isFallFlying(), "Der Gebannte gleitet weiter");
            helper.assertTrue(player.getAttributeValue(NeoForgeMod.GLIDING_FLIGHT) <= 0.0, "Der Flugbann schaltet das Gleiten nicht ab");
            // Milch hilft nicht
            player.removeAllEffects();
            helper.assertTrue(FlightBan.banned(player), "Milch hebt den Flugbann auf");
            // der Sturz tut um 60 % weniger weh
            player.setHealth(player.getMaxHealth());
            player.damageCooldownTime = 0;
            player.causeFallDamage(15.0, 1.0f, level.damageSources().fall());
            float banned = player.getMaxHealth() - player.getHealth();
            FlightBan.lift(player);
            helper.assertFalse(FlightBan.banned(player), "Der Flugbann lässt sich nicht aufheben");
            helper.assertTrue(player.getAttributeValue(NeoForgeMod.GLIDING_FLIGHT) > 0.0, "Nach dem Bann tragen die Schwingen nicht wieder");
            player.setHealth(player.getMaxHealth());
            player.damageCooldownTime = 0;
            player.causeFallDamage(15.0, 1.0f, level.damageSources().fall());
            float free = player.getMaxHealth() - player.getHealth();
            helper.assertTrue(free > 6.0f && banned > 0.5f && banned < free * 0.5f && banned > free * 0.3f,
                    "Der Sturz im Flugbann ist nicht um 60 % gedämpft: " + banned + " statt " + free);
            player.setHealth(player.getMaxHealth());
            gliding.removeModifier(testWings);
            // gute Schwingen verkürzen den Bann
            helper.assertTrue(FlightBan.factor(player) == 1.0f, "Gewöhnliche Elytren verkürzen den Bann");
            Object[][] wings = {{FaeBlocks.FAE_ELYTRA.get(), 170}, {StarBlocks.STAR_WINGS.get(), 140}, {AnniBlocks.GLITCH_WINGS.get(), 120},
                    {CreatorBlocks.CREATOR_WINGS.get(), 80}};
            for (Object[] pair : wings) {
                player.setItemSlot(EquipmentSlot.CHEST, new ItemStack((net.minecraft.world.item.Item) pair[0]));
                int shortened = FlightBan.apply(player, 200, null);
                helper.assertTrue(shortened == (int) pair[1], "Falsche Dauer mit " + pair[0] + ": " + shortened);
                FlightBan.lift(player);
            }
        } finally {
            level.getServer().getPlayerList().remove(player);
        }

        // Texte, Bilder, Klänge, Schadensart
        for (String name : new String[] {"de_de", "en_us"}) {
            JsonObject lang = lang(helper, name);
            for (String key : new String[] {"effect.kingdomsatwar.flight_ban", "entity.kingdomsatwar.sky_bolt", "message.kingdomsatwar.sky_ward.marked",
                    "message.kingdomsatwar.sky_ward.banned", "message.kingdomsatwar.sky_ward.released", "message.kingdomsatwar.sky_ward.no_fly",
                    "death.attack.kingdomsatwar.sky_bolt", "death.attack.kingdomsatwar.sky_bolt.player", "death.attack.kingdomsatwar.sky_bolt.item",
                    "hud.kingdomsatwar.sky_ward.locked", "hud.kingdomsatwar.sky_ward.land", "hud.kingdomsatwar.sky_ward.incoming",
                    "hud.kingdomsatwar.sky_ward.distance", "lore.kingdomsatwar.fae_elytra.5", "lore.kingdomsatwar.star_wings.5",
                    "lore.kingdomsatwar.glitch_wings.4", "lore.kingdomsatwar.creator_wings.6"}) {
                helper.assertTrue(lang.has(key), "Text fehlt (" + name + "): " + key);
            }
            for (String sound : new String[] {"mark", "charge", "launch", "hit", "fizzle", "ping", "release"}) {
                helper.assertTrue(lang.has("subtitles.kingdomsatwar.boss.sky_ward." + sound), "Untertitel fehlt (" + name + "): " + sound);
            }
        }
        String assets = "/assets/" + KingdomsAtWar.MODID + "/";
        for (String texture : new String[] {"entity/sky_ward/sigil", "entity/sky_ward/bolt", "entity/sky_ward/line", "mob_effect/flight_ban",
                "gui/sky_ward/arrow"}) {
            helper.assertTrue(exists(assets + "textures/" + texture + ".png"), "Bild fehlt: " + texture);
        }
        try (InputStream in = SkyWardGameTests.class.getResourceAsStream(assets + "sounds.json")) {
            JsonObject sounds = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject();
            for (String sound : new String[] {"mark", "charge", "launch", "hit", "fizzle", "ping", "release"}) {
                helper.assertTrue(sounds.has("boss.sky_ward." + sound), "Klangereignis fehlt: " + sound);
                helper.assertTrue(exists(assets + "sounds/sky/" + sound + "1.ogg"), "Klangdatei fehlt: " + sound);
            }
        } catch (java.io.IOException e) {
            throw new IllegalStateException(e);
        }
        helper.assertTrue(exists("/data/" + KingdomsAtWar.MODID + "/damage_type/sky_bolt.json"), "Die Schadensart des Bannpfeils fehlt");
        helper.succeed();
    }

    // =====================================================================================
    // Der Boss holt einen Flieger herunter
    // =====================================================================================

    private static void bolt(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = flyer(helper, "test-sky-flyer");
        Mob boss = warden(helper, player, new Vec3(4.5, 1.0, 4.5));
        glide(helper, player, new Vec3(4.5, 26.0, 4.5), new ItemStack(Items.ELYTRA));
        player.setHealth(player.getMaxHealth());
        // ein kurzer Flug bleibt folgenlos
        helper.runAfterDelay(20, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(SkyWard.bolt(boss) == null, "Der Boss lädt sein Siegel schon nach einer Sekunde");
            helper.assertTrue(SkyWard.airTime(boss, player) >= 15, "Die Flugzeit zählt nicht: " + SkyWard.airTime(boss, player));
        }));
        // nach zwei Sekunden: im Visier, das Siegel lädt
        helper.runAfterDelay(56, () -> guard(helper, level, player, boss, () -> {
            SkyBoltEntity bolt = SkyWard.bolt(boss);
            helper.assertTrue(bolt != null && bolt.state() == SkyBoltEntity.CHARGE, "Der Boss nimmt den Flieger nicht ins Visier");
            helper.assertTrue(bolt.theme() == SkyTheme.FIRE && bolt.targetEntity() == player && bolt.ownerEntity() == boss
                    && bolt.chargeTicks() == SkyWard.CHARGE, "Das Siegel gehört nicht zu Boss und Flieger");
            helper.assertTrue(bolt.distanceTo(boss) < 6.0 && bolt.getY() > boss.getBoundingBox().maxY, "Das Siegel schwebt nicht über dem Boss");
            helper.assertFalse(FlightBan.banned(player), "Flugbann schon beim Laden");
        }));
        // der Pfeil hat getroffen
        helper.runAfterDelay(112, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(FlightBan.banned(player), "Der Bannpfeil hat den Flieger nicht getroffen");
            helper.assertFalse(player.isFallFlying(), "Der Getroffene gleitet weiter");
            MobEffectInstance ban = player.getEffect(ModEffects.FLIGHT_BAN);
            helper.assertTrue(ban != null && ban.getDuration() == SkyTheme.FIRE.banTicks(), "Falsche Dauer des Flugbanns: " + ban);
            helper.assertTrue(player.getHealth() < player.getMaxHealth(), "Der Bannpfeil tut nicht weh");
            helper.assertTrue(player.getRemainingFireTicks() > 0, "Der Pfeil des Feuermagiers entzündet nicht");
            helper.assertTrue(SkyWard.bolt(boss) == null, "Der Pfeil ist nach dem Treffer noch da");
            // solange der Flieger gebannt ist, lädt der Boss kein neues Siegel
            player.startFallFlying();
        }));
        helper.runAfterDelay(260, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(SkyWard.bolt(boss) == null && SkyWard.airTime(boss, player) == 0, "Der Boss zielt auf einen Gebannten");
            cleanup(level, player, boss);
            helper.succeed();
        }));
    }

    /** Führt {@code checks} aus und räumt auf, wenn sie scheitern (sonst bliebe der Spieler in der Welt). */
    private static void guard(GameTestHelper helper, ServerLevel level, ServerPlayer player, Mob boss, Runnable checks) {
        guard(helper, level, player, boss, () -> { }, checks);
    }

    private static void guard(GameTestHelper helper, ServerLevel level, ServerPlayer player, Mob boss, Runnable alsoOnFailure, Runnable checks) {
        try {
            checks.run();
        } catch (RuntimeException | Error e) {
            alsoOnFailure.run();
            cleanup(level, player, boss);
            throw e;
        }
    }

    // =====================================================================================
    // Wer landet, dem geschieht nichts
    // =====================================================================================

    private static void landing(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = flyer(helper, "test-sky-lander");
        Mob boss = warden(helper, player, new Vec3(4.5, 1.0, 4.5));
        glide(helper, player, new Vec3(4.5, 26.0, 4.5), new ItemStack(Items.ELYTRA));
        SkyBoltEntity[] seen = new SkyBoltEntity[2];
        // das Siegel lädt – der Flieger landet
        helper.runAfterDelay(56, () -> guard(helper, level, player, boss, () -> {
            seen[0] = SkyWard.bolt(boss);
            helper.assertTrue(seen[0] != null && seen[0].state() == SkyBoltEntity.CHARGE, "Kein Siegel");
            player.stopFallFlying();
        }));
        // das Siegel ist erloschen
        helper.runAfterDelay(76, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(seen[0].isRemoved() && seen[0].outcome() == SkyBoltEntity.Outcome.CANCELLED,
                    "Das Siegel erlischt nicht, obwohl der Flieger gelandet ist: " + seen[0].outcome());
            helper.assertFalse(FlightBan.banned(player), "Flugbann trotz Landung");
            // er steigt wieder auf – diesmal weit oben
            glide(helper, player, new Vec3(4.5, 76.0, 4.5), new ItemStack(Items.ELYTRA));
        }));
        // nach der kurzen Pause lädt der Boss ein neues Siegel
        helper.runAfterDelay(136, () -> guard(helper, level, player, boss, () -> {
            seen[1] = SkyWard.bolt(boss);
            helper.assertTrue(seen[1] != null && seen[1] != seen[0] && seen[1].state() == SkyBoltEntity.CHARGE,
                    "Nach der Landung lädt der Boss kein neues Siegel");
        }));
        // der Pfeil ist unterwegs – der Flieger landet
        helper.runAfterDelay(160, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(!seen[1].isRemoved() && seen[1].state() == SkyBoltEntity.HOMING, "Der Pfeil ist nicht unterwegs: " + seen[1].state());
            helper.assertTrue(seen[1].getY() > boss.getBoundingBox().maxY + 4.0, "Der Pfeil steigt nicht");
            player.stopFallFlying();
        }));
        helper.runAfterDelay(174, () -> guard(helper, level, player, boss, () -> helper.assertTrue(seen[1].isRemoved() || seen[1].state() == SkyBoltEntity.FREE,
                "Der Pfeil verfolgt einen Gelandeten")));
        helper.runAfterDelay(200, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(seen[1].isRemoved() && seen[1].outcome() == SkyBoltEntity.Outcome.LOST, "Der Pfeil verpufft nicht: " + seen[1].outcome());
            helper.assertFalse(FlightBan.banned(player), "Flugbann, obwohl der Flieger gelandet war");
            cleanup(level, player, boss);
            helper.succeed();
        }));
    }

    // =====================================================================================
    // Der Pfeil hält vor: wer geradeaus fliegt, wird beim ersten Anflug getroffen
    // =====================================================================================

    private static void intercept(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        // der Treffpunkt, gerechnet: ein Ziel 30 Blöcke voraus, das mit 1 Block je Tick quer fliegt, Geschoss mit 2 Blöcken je Tick
        double t = SkyBoltEntity.interceptTime(new Vec3(30.0, 0.0, 0.0), new Vec3(0.0, 0.0, 1.0), 2.0);
        helper.assertTrue(Math.abs(t - 30.0 / Math.sqrt(3.0)) < 1.0e-6, "Falscher Treffpunkt: " + t);
        helper.assertTrue(Math.abs(SkyBoltEntity.interceptTime(new Vec3(20.0, 0.0, 0.0), Vec3.ZERO, 2.0) - 10.0) < 1.0e-6, "Ein ruhendes Ziel");
        helper.assertTrue(SkyBoltEntity.interceptTime(new Vec3(20.0, 0.0, 0.0), new Vec3(3.0, 0.0, 0.0), 2.0) == 30.0, "Ein Ziel, das davonzieht");
        ServerPlayer player = flyer(helper, "test-sky-straight");
        Mob boss = ModEntities.MAGES.get(DragonType.FIRE).get().create(level, EntitySpawnReason.COMMAND);
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 1.0, 4.5));
        boss.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        boss.setNoAi(true);
        boss.setPersistenceRequired();
        level.addFreshEntity(boss);
        // die Anflüge führen weit hinaus: dort muss die Welt geladen sein, sonst bliebe der Pfeil in der Luft stehen
        int cx = net.minecraft.core.SectionPos.blockToSectionCoord(at.x);
        int cz = net.minecraft.core.SectionPos.blockToSectionCoord(at.z);
        Runnable release = () -> {
            for (int x = cx - 2; x <= cx + 2; x++) {
                for (int z = cz - 2; z <= cz + 3; z++) {
                    level.setChunkForced(x, z, false);
                }
            }
        };
        for (int x = cx - 2; x <= cx + 2; x++) {
            for (int z = cz - 2; z <= cz + 3; z++) {
                level.setChunkForced(x, z, true);
            }
        }
        // drei Anflüge: über den Boss hinweg auf ihn zu, quer an ihm vorbei, von ihm fort
        Vec3[][] runs = {
                {new Vec3(4.5, 26.0, -26.0), new Vec3(0.0, -0.12, 1.1)},
                {new Vec3(-22.0, 22.0, 18.0), new Vec3(1.2, -0.1, 0.0)},
                {new Vec3(4.5, 24.0, 8.0), new Vec3(0.25, -0.05, 0.9)}};
        SkyBoltEntity[] bolt = new SkyBoltEntity[1];
        Vec3[] course = new Vec3[2];
        int[] launched = new int[1];
        // der Flieger zieht jeden Tick ein Stück weiter (der Testspieler bewegt sich nicht von selbst)
        helper.onEachTick(() -> {
            if (course[0] != null && player.isFallFlying()) {
                course[0] = course[0].add(course[1]);
                player.snapTo(course[0].x, course[0].y, course[0].z, 0.0f, 0.0f);
            }
        });
        for (int i = 0; i < runs.length; i++) {
            Vec3[] run = runs[i];
            int number = i + 1;
            helper.runAfterDelay(30 + i * 80L, () -> guard(helper, level, player, boss, release, () -> {
                FlightBan.lift(player);
                player.clearFire();
                course[0] = helper.absoluteVec(run[0]);
                course[1] = run[1];
                glide(helper, player, run[0], new ItemStack(Items.ELYTRA));
                bolt[0] = SkyBoltEntity.charge(level, boss, player, SkyTheme.FIRE, 4);
                launched[0] = (int) helper.getTick();
            }));
            helper.runAfterDelay(30 + i * 80L + 50, () -> guard(helper, level, player, boss, release, () -> {
                helper.assertTrue(bolt[0].isRemoved() && bolt[0].outcome() == SkyBoltEntity.Outcome.HIT && FlightBan.banned(player),
                        "Anflug " + number + ": Der Pfeil verfehlt einen, der geradeaus fliegt (" + bolt[0].outcome() + ")");
                course[0] = null;
            }));
        }
        helper.runAfterDelay(30 + runs.length * 80L, () -> {
            release.run();
            cleanup(level, player, boss);
            helper.succeed();
        });
    }

    // =====================================================================================
    // Blöcke halten den Pfeil auf; Annis Rückruck; der Ruck zum Boss
    // =====================================================================================

    private static void riders(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer player = flyer(helper, "test-sky-riders");
        // ein Boss ohne eigene Wacht: die Pfeile löst der Test selbst
        Mob boss = ModEntities.MAGES.get(DragonType.FIRE).get().create(level, EntitySpawnReason.COMMAND);
        Vec3 at = helper.absoluteVec(new Vec3(4.5, 1.0, 1.5));
        boss.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        boss.setNoAi(true);
        boss.setPersistenceRequired();
        level.addFreshEntity(boss);
        // ein Dach aus Stein zwischen Siegel und Flieger
        for (int x = 2; x <= 6; x++) {
            for (int z = 3; z <= 6; z++) {
                helper.setBlock(new BlockPos(x, 4, z), Blocks.STONE);
            }
        }
        glide(helper, player, new Vec3(4.5, 1.6, 7.5), new ItemStack(Items.ELYTRA));
        SkyBoltEntity[] seen = new SkyBoltEntity[3];
        seen[0] = SkyBoltEntity.charge(level, boss, player, SkyTheme.EARTH, 2);
        helper.runAfterDelay(24, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(seen[0].isRemoved() && seen[0].outcome() == SkyBoltEntity.Outcome.BLOCKED,
                    "Der Pfeil geht durch Stein: " + seen[0].outcome());
            helper.assertFalse(FlightBan.banned(player), "Flugbann durch die Decke");
            // Anni: der Flieger steht wieder auf dem Boden – und mit den Schwingen des Schöpfers ist der Bann kurz
            glide(helper, player, new Vec3(4.5, 30.0, 1.5), new ItemStack(CreatorBlocks.CREATOR_WINGS.get()));
            seen[1] = SkyBoltEntity.charge(level, boss, player, SkyTheme.GLITCH, 2);
        }));
        helper.runAfterDelay(70, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(seen[1].isRemoved() && seen[1].outcome() == SkyBoltEntity.Outcome.HIT, "Annis Pfeil trifft nicht: " + seen[1].outcome());
            MobEffectInstance ban = player.getEffect(ModEffects.FLIGHT_BAN);
            helper.assertTrue(ban != null && ban.getDuration() == Math.round(SkyTheme.GLITCH.banTicks() * 0.4f), "Falsche Dauer mit den Schwingen des Schöpfers: " + ban);
            // der Boden des Tests liegt unter seiner untersten Schicht
            double floor = helper.absoluteVec(Vec3.ZERO).y;
            helper.assertTrue(Math.abs(player.getY() - floor) < 0.01, "Annis Pfeil stellt den Flieger nicht auf den Boden: " + (player.getY() - floor));
            helper.assertFalse(player.isFallFlying(), "Nach Annis Pfeil gleitet der Flieger weiter");
            // er flog genau über dem Boss – und landet nicht in ihm, sondern ein paar Schritte davor
            double away = Math.hypot(player.getX() - boss.getX(), player.getZ() - boss.getZ());
            helper.assertTrue(away > 5.5 && away < 8.0, "Annis Pfeil stellt den Flieger in den Boss: " + away);
            FlightBan.lift(player);
            // der Stern: ein Ruck zum Boss hin, der Getroffene leuchtet
            glide(helper, player, new Vec3(4.5, 30.0, 16.5), new ItemStack(Items.ELYTRA));
            player.removeEffect(MobEffects.GLOWING);
            seen[2] = SkyBoltEntity.charge(level, boss, player, SkyTheme.STAR, 2);
        }));
        helper.runAfterDelay(120, () -> guard(helper, level, player, boss, () -> {
            helper.assertTrue(seen[2].isRemoved() && seen[2].outcome() == SkyBoltEntity.Outcome.HIT, "Der Sternenpfeil trifft nicht: " + seen[2].outcome()
                    + " (Zustand " + seen[2].state() + ", Pfeil " + helper.relativeVec(seen[2].position()) + ", Flieger "
                    + helper.relativeVec(player.position()) + (player.isFallFlying() ? " gleitet" : " gleitet nicht") + ")");
            helper.assertTrue(FlightBan.banned(player) && player.hasEffect(MobEffects.GLOWING), "Der Sternenpfeil bannt nicht oder lässt nicht leuchten");
            Vec3 pull = player.getDeltaMovement();
            helper.assertTrue(pull.z < -0.2 && pull.y < 0.0, "Kein Ruck zum Boss hin: " + pull);
            FlightBan.lift(player);
            cleanup(level, player, boss);
            helper.succeed();
        }));
    }
}
