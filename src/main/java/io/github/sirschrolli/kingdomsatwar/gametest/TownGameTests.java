package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.TownBannerBlock;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Coronation;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownTier;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownView;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.SeatEntity;
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
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");
    /** Mitte der Testfläche (48 × 48, Steinboden). */
    private static final BlockPos CENTER = new BlockPos(24, 1, 24);

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_FOUND =
            ModGameTests.TEST_FUNCTIONS.register("town_found", () -> TownGameTests::townFound);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_BLUEPRINTS =
            ModGameTests.TEST_FUNCTIONS.register("town_blueprints", () -> TownGameTests::townBlueprints);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_BUILDER =
            ModGameTests.TEST_FUNCTIONS.register("town_builder", () -> TownGameTests::townBuilder);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_TRADE =
            ModGameTests.TEST_FUNCTIONS.register("town_trade", () -> TownGameTests::townTrade);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWN_CROWN =
            ModGameTests.TEST_FUNCTIONS.register("town_crown", () -> TownGameTests::townCrown);

    private TownGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(TOWN_BLUEPRINTS.getId(), new FunctionGameTestInstance(TOWN_BLUEPRINTS.key(),
                new TestData<>(environment, Level.OVERWORLD, TOWN, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        // jede Siedlung in einem eigenen Durchgang
        own(event, TOWN_FOUND, "town_found", 200);
        own(event, TOWN_BUILDER, "town_builder", 4000);
        own(event, TOWN_TRADE, "town_trade", 4000);
        own(event, TOWN_CROWN, "town_crown", 600);
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
    private static ServerPlayer player(GameTestHelper helper, BlockPos relative) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        Vec3 at = helper.absoluteVec(Vec3.atBottomCenterOf(relative));
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer... players) {
        for (ServerPlayer p : players) {
            helper.getLevel().getServer().getPlayerList().remove(p);
        }
    }

    private static void clearTowns(MinecraftServer server) {
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

    private static Town found(GameTestHelper helper, ServerPlayer owner, BlockPos relative) {
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
        BoundingBox b = type.blueprint().bounds();
        return helper.absolutePos(new BlockPos(x - (b.minX() + b.maxX()) / 2, 1, z - (b.minZ() + b.maxZ()) / 2));
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

    /** Jedes Gebäude lässt sich vollständig setzen; Betten, Truhen, Eingang und Bauablauf stimmen; die Drehungen auch. */
    private static void townBlueprints(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (BuildingType type : BuildingType.values()) {
            Blueprint plan = type.blueprint();
            BoundingBox b = plan.bounds();
            helper.assertTrue(!plan.blocks().isEmpty(), type.id() + ": leerer Bauplan");
            helper.assertTrue(b.getXSpan() <= 46 && b.getZSpan() <= 46 && b.maxY() < 28, type.id() + ": zu groß für die Testfläche " + b);
            helper.assertValueEqual(plan.beds().size(), type.beds(), type.id() + ": Betten");
            if (type == BuildingType.FIELD || type == BuildingType.LUMBER_HUT || type == BuildingType.MINE) {
                helper.assertTrue(plan.chest() != null, type.id() + ": Truhe für die Arbeit fehlt");
            }
            helper.assertTrue(b.inflatedBy(1).isInside(plan.entrance()), type.id() + ": Eingang außerhalb");
            BlockPos origin = originFor(helper, type, 24, 24);
            helper.assertTrue(!plan.steps(origin, Rotation.NONE).isEmpty(), type.id() + ": kein Bauablauf");
            plan.placeAll(level, origin, Rotation.NONE);
            helper.assertTrue(plan.complete(level, origin, Rotation.NONE), type.id() + ": nach dem Bauen unvollständig");
            for (BlockPos bed : plan.beds()) {
                helper.assertTrue(level.getBlockState(Blueprint.world(origin, Rotation.NONE, bed)).getBlock()
                        instanceof net.minecraft.world.level.block.BedBlock, type.id() + ": Bett fehlt bei " + bed);
            }
            if (plan.chest() != null) {
                helper.assertTrue(level.getBlockEntity(Blueprint.world(origin, Rotation.NONE, plan.chest()))
                        instanceof net.minecraft.world.Container, type.id() + ": Truhe ist kein Behälter");
            }
        }
        // Drehungen: das Haus in allen vier Richtungen
        for (Rotation rotation : Rotation.values()) {
            BlockPos origin = originFor(helper, BuildingType.HOUSE, 24, 24);
            BuildingType.HOUSE.blueprint().placeAll(level, origin, rotation);
            helper.assertTrue(BuildingType.HOUSE.blueprint().complete(level, origin, rotation), "Haus gedreht " + rotation + " unvollständig");
        }
        helper.succeed();
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
        helper.onEachTick(() -> {
            if (!town.projects().isEmpty()) {
                maxProgress[0] = Math.max(maxProgress[0], town.projects().getFirst().progress);
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
            helper.assertValueEqual(town.count(BuildingType.FIELD), 1, "Fertige Felder");
            helper.assertTrue(BuildingType.FIELD.blueprint().complete(level, origin, Rotation.NONE), "Feld unvollständig gebaut");
            helper.assertTrue(Reputation.points(owner) >= Reputation.HIRE + Reputation.BUILDING, "Ansehen fürs fertige Gebäude");
            KingdomsAtWar.LOGGER.info("[Test] Der Baumeister hat das Feld nach {} Ticks fertig gebaut ({} Bauschritte)", helper.getTick(), total);
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
}
