package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.Warhorse;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.siege.SiegePoses;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.GameTestSequence;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der neuen Truppen (K3b): Ausrüstung und Werte jeder Art, die Ausbildungswege am Feldherrentisch (mit Wahl der
 * Art und ihren Preisen), der Lanzenreiter mit Schlachtross und Lanzenangriff, der Armbrustschütze, die Nahkampf-Eigenheiten
 * (Speer gegen Reiter, Streitkolben gegen die Nebenleute, Herausforderung des Schildträgers) und der Belagerungsknecht
 * (bessert aus, lädt schneller nach).
 */
final class TroopGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> KIT =
            ModGameTests.TEST_FUNCTIONS.register("troops_kit", () -> TroopGameTests::kit);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TRAINING =
            ModGameTests.TEST_FUNCTIONS.register("troops_training", () -> TroopGameTests::training);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LANCER =
            ModGameTests.TEST_FUNCTIONS.register("troops_lancer", () -> TroopGameTests::lancer);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CROSSBOW =
            ModGameTests.TEST_FUNCTIONS.register("troops_crossbow", () -> TroopGameTests::crossbow);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MELEE =
            ModGameTests.TEST_FUNCTIONS.register("troops_melee", () -> TroopGameTests::melee);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ENGINEER =
            ModGameTests.TEST_FUNCTIONS.register("troops_engineer", () -> TroopGameTests::engineer);

    private TroopGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, KIT, 60);
        add(event, environment, TRAINING, 200);
        add(event, environment, LANCER, 1500);
        add(event, environment, CROSSBOW, 1200);
        add(event, environment, MELEE, 100);
        add(event, environment, ENGINEER, 600);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static ServerPlayer player(GameTestHelper helper, Vec3 at) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.connection.markClientLoaded();
        player.getAbilities().instabuild = false;
        player.getAbilities().invulnerable = true;
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    private static void leave(GameTestHelper helper, ServerPlayer player) {
        helper.getLevel().getServer().getPlayerList().remove(player);
    }

    /** Ein Soldat der Art {@code type} an {@code at}, ausgerüstet und benannt; gehört {@code owner} (wenn angegeben). */
    private static SoldierEntity soldier(ServerLevel level, SoldierType type, Vec3 at, float yaw, ServerPlayer owner) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        soldier.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        soldier.setYHeadRot(yaw);
        soldier.setYBodyRot(yaw);
        soldier.finalizeSpawn(level, level.getCurrentDifficultyAt(soldier.blockPosition()), EntitySpawnReason.COMMAND, null);
        level.addFreshEntity(soldier);
        if (owner != null) {
            soldier.setOwner(owner);
            soldier.setMode(SoldierEntity.MODE_HOLD, soldier.blockPosition());
        }
        return soldier;
    }

    /** Eine Mumie ohne eigenen Willen (verbrennt nicht in der Sonne) mit {@code health} Leben. */
    private static Mob dummy(ServerLevel level, Vec3 at, double health) {
        var mob = ModEntities.HUSK.get().create(level, EntitySpawnReason.COMMAND);
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        mob.setNoAi(true);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        mob.setHealth((float) health);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        return mob;
    }

    /** Ein Feld weit draußen (wie der Schießplatz der Belagerungstests): Chunks dauerhaft geladen, Steinboden, Luft darüber. */
    private record Field(ServerLevel level, BlockPos origin, int half, List<long[]> chunks) {
        Vec3 at(double dx, double dy, double dz) {
            return new Vec3(this.origin.getX() + dx, this.origin.getY() + dy, this.origin.getZ() + dz);
        }

        boolean loaded() {
            for (long[] c : this.chunks) {
                if (!this.level.areEntitiesLoaded(ChunkPos.pack((int) c[0], (int) c[1]))) {
                    return false;
                }
            }
            return true;
        }

        void close() {
            AABB area = new AABB(this.origin).inflate(this.half + 8, 20, this.half + 8);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                e.discard();
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int slot, int half) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + 15000 + slot * 320, 150, base.getZ() + 15000);
        List<long[]> chunks = new ArrayList<>();
        for (int cx = (origin.getX() - half - 4) >> 4; cx <= (origin.getX() + half + 4) >> 4; cx++) {
            for (int cz = (origin.getZ() - half - 4) >> 4; cz <= (origin.getZ() + half + 4) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState air = Blocks.AIR.defaultBlockState();
        for (int x = -half; x <= half; x++) {
            for (int z = -half; z <= half; z++) {
                level.setBlock(origin.offset(x, -1, z), stone, Block.UPDATE_CLIENTS);
                for (int y = 0; y < 8; y++) {
                    level.setBlock(origin.offset(x, y, z), air, Block.UPDATE_CLIENTS);
                }
            }
        }
        return new Field(level, origin, half, chunks);
    }

    /**
     * Ablauf auf dem Feld: erst warten, bis seine Chunks samt Wesen geladen sind, dann aufbauen. Der Testserver tickt so
     * schnell er kann – solange die Chunks fehlen, wartet jeder Tick ein wenig, damit das Laden (in anderen Threads)
     * hinterherkommt.
     */
    private static GameTestSequence prepared(GameTestHelper helper, Field f, Runnable setup) {
        return helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(ModGameTests.awaitLoading(f.loaded()), "Chunks des Felds laden nicht"))
                .thenExecute(setup)
                .thenIdle(2);
    }

    // =====================================================================================
    // Ausrüstung und Werte
    // =====================================================================================

    /** Jede neue Art erscheint mit ihrer Waffe (und Schild), ihren Werten, einem Namen und einem gültigen Gesicht. */
    private static void kit(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        Object[][] kits = {
                {SoldierType.SPEARMAN, Items.IRON_SPEAR, null},
                {SoldierType.CROSSBOWMAN, Items.CROSSBOW, null},
                {SoldierType.SHIELDBEARER, Items.IRON_SWORD, Items.SHIELD},
                {SoldierType.MAN_AT_ARMS, Items.MACE, null},
                {SoldierType.LANCER, Items.IRON_SPEAR, null},
                {SoldierType.ENGINEER, Items.IRON_AXE, null}};
        int i = 0;
        for (Object[] kit : kits) {
            SoldierType type = (SoldierType) kit[0];
            SoldierEntity s = soldier(level, type, helper.absoluteVec(new Vec3(1.5 + i, 1, 4.5)), 0.0f, null);
            i++;
            helper.assertTrue(s.getMainHandItem().is((net.minecraft.world.item.Item) kit[1]), type.id() + " trägt " + s.getMainHandItem());
            if (kit[2] != null) {
                helper.assertTrue(s.getOffhandItem().is((net.minecraft.world.item.Item) kit[2]), type.id() + " ohne Schild");
            }
            helper.assertTrue(Math.abs(s.getAttributeValue(Attributes.MAX_HEALTH) - type.health()) < 0.01, type.id() + ": Leben");
            helper.assertTrue(Math.abs(s.getAttributeBaseValue(Attributes.ARMOR) - type.armor()) < 0.01, type.id() + ": Rüstung");
            helper.assertTrue(!s.getPersonalName().isEmpty(), type.id() + " ohne Namen");
            helper.assertTrue(s.getVariant() >= 0 && s.getVariant() < type.variants(), type.id() + ": Gesicht");
            helper.assertTrue(s.hirePrice() % 9 == 0 && s.hirePrice() > type.coins(), type.id() + ": Sold");
            s.discard();
        }
        helper.assertTrue(SoldierType.CROSSBOWMAN.ranged() && SoldierType.CROSSBOWMAN.crossbow(), "Armbrustschütze schießt nicht");
        helper.assertTrue(SoldierType.SHIELDBEARER.shielded() && SoldierType.KNIGHT.shielded(), "Schildträger ohne Schild");
        helper.assertTrue(SoldierType.LANCER.mounted() && SoldierType.LANCER.spear() && SoldierType.SPEARMAN.spear(), "Speere/Reiter");
        helper.succeed();
    }

    // =====================================================================================
    // Ausbildung
    // =====================================================================================

    /**
     * Am Feldherrentisch: ein Schwertkämpfer wird Speerträger (27 Bronze und ein Eisenspeer – Name und Rang bleiben), ein
     * Bogenschütze Armbrustschütze; ein Ritter wird ohne Sattel kein Lanzenreiter, mit Sattel und Stahl schon – und sitzt
     * bald auf seinem Schlachtross. Was nicht auf dem Ausbildungsweg liegt, geht nicht.
     */
    private static void training(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos tableLocal = new BlockPos(4, 1, 4);
        helper.setBlock(tableLocal, ModBlocks.RECRUITMENT_TABLE.get());
        BlockPos table = helper.absolutePos(tableLocal);
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(4.5, 1, 1.5)));
        SoldierEntity[] lancer = {null};
        helper.startSequence().thenExecute(() -> {
            helper.assertValueEqual(Training.promotions(SoldierType.SWORDSMAN), List.of(SoldierType.KNIGHT, SoldierType.SPEARMAN,
                    SoldierType.SHIELDBEARER, SoldierType.ENGINEER), "Wege des Schwertkämpfers");
            helper.assertValueEqual(Training.promotions(SoldierType.KNIGHT), List.of(SoldierType.CAPTAIN, SoldierType.LANCER,
                    SoldierType.MAN_AT_ARMS), "Wege des Ritters");
            helper.assertValueEqual(Training.promotions(SoldierType.ARCHER), List.of(SoldierType.CROSSBOWMAN), "Weg des Bogenschützen");
            for (SoldierType type : SoldierType.values()) {
                for (SoldierType next : Training.promotions(type)) {
                    Training.Cost cost = Training.promoteCost(type, next);
                    helper.assertTrue(cost != null && cost.coins() % 9 == 0 && !cost.items().isEmpty(), type.id() + " → " + next.id() + ": Preis");
                }
            }
            // Schwertkämpfer → Speerträger
            SoldierEntity swordsman = soldier(level, SoldierType.SWORDSMAN, helper.absoluteVec(new Vec3(2.5, 1, 4.5)), 0.0f, player);
            swordsman.setRank(1);
            String name = swordsman.getPersonalName();
            helper.assertTrue(Training.train(player, table, swordsman.getId(), Training.Action.PROMOTE, SoldierType.CAPTAIN)
                    == Recruitment.Result.MAX_RANK, "Schwertkämpfer darf nicht gleich Hauptmann werden");
            helper.assertTrue(Training.train(player, table, swordsman.getId(), Training.Action.PROMOTE, SoldierType.SPEARMAN)
                    == Recruitment.Result.NOT_ENOUGH, "Speerträger ohne Speer und Münzen muss scheitern");
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 5));
            player.getInventory().add(new ItemStack(Items.IRON_SPEAR));
            int coins = Coins.total(player);
            helper.assertTrue(Training.train(player, table, swordsman.getId(), Training.Action.PROMOTE, SoldierType.SPEARMAN)
                    == Recruitment.Result.PROMOTED, "Ausbildung zum Speerträger klappt nicht");
            helper.assertValueEqual(Coins.total(player), coins - 27, "Münzen nach der Ausbildung zum Speerträger");
            helper.assertValueEqual(player.getInventory().countItem(Items.IRON_SPEAR), 0, "der Speer des Spielers ist noch da");
            SoldierEntity spearman = level.getEntitiesOfClass(SoldierEntity.class, new AABB(table).inflate(10),
                    s -> s.getSoldierType() == SoldierType.SPEARMAN).getFirst();
            helper.assertValueEqual(spearman.getPersonalName(), name, "Name des Speerträgers");
            helper.assertValueEqual(spearman.getRank(), 1, "Rang des Speerträgers");
            helper.assertTrue(spearman.isOwnedBy(player) && spearman.getMainHandItem().is(Items.IRON_SPEAR), "Speerträger ohne Herrn oder Speer");
            // Bogenschütze → Armbrustschütze
            SoldierEntity archer = soldier(level, SoldierType.ARCHER, helper.absoluteVec(new Vec3(6.5, 1, 4.5)), 0.0f, player);
            player.getInventory().add(new ItemStack(Items.CROSSBOW));
            helper.assertTrue(Training.train(player, table, archer.getId(), Training.Action.PROMOTE, SoldierType.CROSSBOWMAN)
                    == Recruitment.Result.PROMOTED, "Ausbildung zum Armbrustschützen klappt nicht");
            // Ritter → Lanzenreiter: erst ohne Sattel, dann mit
            SoldierEntity knight = soldier(level, SoldierType.KNIGHT, helper.absoluteVec(new Vec3(4.5, 1, 6.5)), 0.0f, player);
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 9));
            player.getInventory().add(new ItemStack(ModItems.STEEL_INGOT.get(), 2));
            helper.assertTrue(Training.train(player, table, knight.getId(), Training.Action.PROMOTE, SoldierType.LANCER)
                    == Recruitment.Result.NOT_ENOUGH, "Lanzenreiter ohne Sattel muss scheitern");
            player.getInventory().add(new ItemStack(Items.SADDLE));
            helper.assertTrue(Training.train(player, table, knight.getId(), Training.Action.PROMOTE, SoldierType.LANCER)
                    == Recruitment.Result.PROMOTED, "Ausbildung zum Lanzenreiter klappt nicht");
            lancer[0] = level.getEntitiesOfClass(SoldierEntity.class, new AABB(table).inflate(10),
                    s -> s.getSoldierType() == SoldierType.LANCER).getFirst();
        }).thenWaitUntil(() -> helper.assertTrue(lancer[0].getVehicle() instanceof Warhorse, "der Lanzenreiter sitzt nicht auf"))
                .thenExecute(() -> {
                    Warhorse horse = (Warhorse) lancer[0].getVehicle();
                    helper.assertTrue(horse.isTamed() && horse.isSaddled(), "Schlachtross nicht gezähmt oder ohne Sattel");
                    helper.assertTrue(horse.getItemBySlot(EquipmentSlot.BODY).is(Items.IRON_HORSE_ARMOR), "Schlachtross ohne Rossharnisch");
                    helper.assertTrue(horse.getOwnerReference() != null && horse.getOwnerReference().getUUID().equals(player.getUUID()),
                            "das Schlachtross gehört nicht dem Spieler");
                    helper.assertTrue(lancer[0].isAlliedTo(horse) && horse.isAlliedTo(lancer[0]), "Reiter und Ross sind keine Verbündeten");
                    for (SoldierEntity s : level.getEntitiesOfClass(SoldierEntity.class, new AABB(table).inflate(12))) {
                        s.discard();
                    }
                    horse.discard();
                    leave(helper, player);
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Lanzenreiter
    // =====================================================================================

    /**
     * Der Lanzenreiter sitzt auf seinem Schlachtross auf, reitet den Lanzenangriff gegen einen Gegner und trifft ihn hart;
     * er bleibt dabei im Sattel. Fällt das Ross, steht er zu Fuß – der Feldscher berechnet ihm ein neues.
     */
    private static void lancer(GameTestHelper helper) {
        Field f = field(helper, 0, 22);
        ServerLevel level = f.level();
        ServerPlayer player = player(helper, f.at(-12.5, 0.0, 12.5));
        SoldierEntity[] lancer = {null};
        Mob[] foe = {null};
        boolean[] lowered = {false};
        prepared(helper, f, () -> lancer[0] = soldier(level, SoldierType.LANCER, f.at(0.5, 0.0, 10.5), 180.0f, player))
                .thenWaitUntil(() -> helper.assertTrue(lancer[0].getVehicle() instanceof Warhorse, "der Lanzenreiter sitzt nicht auf"))
                .thenExecute(() -> {
                    foe[0] = dummy(level, f.at(0.5, 0.0, -8.5), 200.0);
                    lancer[0].setTarget(foe[0]);
                })
                .thenWaitUntil(() -> {
                    lowered[0] |= lancer[0].isUsingItem();
                    helper.assertTrue(foe[0].getHealth() < 200.0f - 6.0f, "der Lanzenangriff trifft nicht (Leben "
                            + foe[0].getHealth() + ", Abstand " + String.format(java.util.Locale.ROOT, "%.1f", lancer[0].distanceTo(foe[0]))
                            + ", im Sattel " + (lancer[0].getVehicle() instanceof Warhorse) + ", Lanze gesenkt " + lancer[0].isUsingItem() + ")");
                })
                .thenExecute(() -> {
                    helper.assertTrue(lowered[0], "der Lanzenreiter senkt die Lanze nie");
                    helper.assertTrue(lancer[0].getVehicle() instanceof Warhorse, "der Lanzenreiter fällt beim Angriff vom Ross");
                    Warhorse horse = (Warhorse) lancer[0].getVehicle();
                    foe[0].discard();
                    lancer[0].setTarget(null);
                    horse.kill(level);
                })
                .thenWaitUntil(() -> helper.assertTrue(!lancer[0].isPassenger(), "der Reiter sitzt noch auf dem toten Ross"))
                .thenExecute(() -> {
                    helper.assertTrue(lancer[0].needsSteed(), "ohne Ross braucht der Lanzenreiter ein neues");
                    helper.assertTrue(Training.healCost(lancer[0]) >= Training.STEED_COST, "der Feldscher berechnet kein neues Ross");
                    lancer[0].provideSteed();
                })
                .thenWaitUntil(() -> helper.assertTrue(lancer[0].getVehicle() instanceof Warhorse, "kein neues Ross aus den Ställen"))
                .thenExecute(() -> {
                    leave(helper, player);
                    f.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Armbrustschütze
    // =====================================================================================

    /** Der Armbrustschütze spannt, legt an und trifft einen Gegner auf 14 Blöcke; danach ist die Armbrust wieder leer. */
    private static void crossbow(GameTestHelper helper) {
        Field f = field(helper, 1, 12);
        ServerLevel level = f.level();
        ServerPlayer player = player(helper, f.at(-8.5, 0.0, 8.5));
        SoldierEntity[] shooter = {null};
        Mob[] foe = {null};
        boolean[] charged = {false};
        prepared(helper, f, () -> {
            shooter[0] = soldier(level, SoldierType.CROSSBOWMAN, f.at(0.5, 0.0, 7.5), 180.0f, player);
            foe[0] = dummy(level, f.at(0.5, 0.0, -6.5), 100.0);
            shooter[0].setTarget(foe[0]);
        })
                .thenWaitUntil(() -> {
                    charged[0] |= CrossbowItem.isCharged(shooter[0].getMainHandItem());
                    helper.assertTrue(foe[0].getHealth() < 100.0f, "der Bolzen trifft nicht (gespannt: " + charged[0] + ", spannt: "
                            + shooter[0].isUsingItem() + ")");
                })
                .thenExecute(() -> {
                    helper.assertTrue(charged[0], "die Armbrust war nie gespannt");
                    helper.assertTrue(100.0f - foe[0].getHealth() >= 5.0f, "der Bolzen trifft zu schwach: " + (100.0f - foe[0].getHealth()));
                    leave(helper, player);
                    f.close();
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Nahkampf
    // =====================================================================================

    /**
     * Speerträger treffen Reiter härter als Fußvolk; der Streitkolben des Waffenknechts trifft auch den Gegner neben dem
     * Ziel, aber nie einen Kameraden; der Schildträger zieht einen Gegner, der einen Kameraden angreift, auf sich.
     */
    private static void melee(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(8.5, 1, 8.5)));
        try {
            // Speer gegen Reiter
            SoldierEntity spearman = soldier(level, SoldierType.SPEARMAN, helper.absoluteVec(new Vec3(1.5, 1, 1.5)), 0.0f, player);
            spearman.setNoAi(true);
            Mob walker = dummy(level, helper.absoluteVec(new Vec3(1.5, 1, 3.0)), 100.0);
            Mob rider = dummy(level, helper.absoluteVec(new Vec3(3.5, 1, 3.0)), 100.0);
            var horse = ModEntities.FARM_HORSE.get().create(level, EntitySpawnReason.COMMAND);
            horse.snapTo(rider.getX(), rider.getY(), rider.getZ(), 0.0f, 0.0f);
            horse.setNoAi(true);
            level.addFreshEntity(horse);
            rider.startRiding(horse, true, true);
            helper.assertTrue(rider.isPassenger(), "die Mumie sitzt nicht auf dem Pferd");
            spearman.doHurtTarget(level, walker);
            spearman.doHurtTarget(level, rider);
            float plain = 100.0f - walker.getHealth();
            float bonus = 100.0f - rider.getHealth();
            helper.assertTrue(plain > 0.0f && bonus > plain * 1.4f, "Speer gegen Reiter: " + bonus + " statt mehr als " + plain + " × 1,4");
            // Streitkolben: Nebenmann getroffen, Kamerad nicht
            SoldierEntity heavy = soldier(level, SoldierType.MAN_AT_ARMS, helper.absoluteVec(new Vec3(6.5, 1, 1.5)), 0.0f, player);
            heavy.setNoAi(true);
            Mob first = dummy(level, helper.absoluteVec(new Vec3(6.5, 1, 3.0)), 100.0);
            Mob beside = dummy(level, helper.absoluteVec(new Vec3(7.6, 1, 3.2)), 100.0);
            SoldierEntity comrade = soldier(level, SoldierType.SWORDSMAN, helper.absoluteVec(new Vec3(5.4, 1, 3.2)), 0.0f, player);
            comrade.setNoAi(true);
            float comradeHealth = comrade.getHealth();
            helper.assertTrue(heavy.doHurtTarget(level, first), "der Waffenknecht trifft nicht");
            helper.assertTrue(beside.getHealth() < 100.0f, "der Streitkolben trifft den Nebenmann nicht");
            helper.assertTrue(comrade.getHealth() >= comradeHealth, "der Streitkolben trifft einen Kameraden");
            // Schildträger: Herausforderung
            SoldierEntity bearer = soldier(level, SoldierType.SHIELDBEARER, helper.absoluteVec(new Vec3(4.5, 1, 6.5)), 180.0f, player);
            SoldierEntity ward = soldier(level, SoldierType.ARCHER, helper.absoluteVec(new Vec3(2.5, 1, 7.5)), 0.0f, player);
            ward.setNoAi(true);
            Mob foe = dummy(level, helper.absoluteVec(new Vec3(4.5, 1, 4.5)), 100.0);
            foe.setTarget(ward);
            bearer.tauntNow(level);
            helper.assertTrue(foe.getTarget() == bearer, "der Schildträger zieht den Gegner nicht auf sich");
            for (Entity e : List.of(spearman, walker, rider, horse, heavy, first, beside, comrade, bearer, ward, foe)) {
                e.discard();
            }
            helper.succeed();
        } finally {
            leave(helper, player);
        }
    }

    // =====================================================================================
    // Belagerungsknecht
    // =====================================================================================

    /**
     * Ein Belagerungsknecht bessert ein beschädigtes eigenes Katapult in der Nähe aus; bedient er eines, lädt es um die Hälfte
     * schneller nach als mit einem Schwertkämpfer.
     */
    private static void engineer(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        ServerPlayer player = player(helper, helper.absoluteVec(new Vec3(4.5, 1, 4.5)));
        SiegeWeaponEntity[] w = {null, null, null};
        SoldierEntity[] crew = {null, null};
        int since = SiegePoses.releaseTick(SiegeType.CATAPULT) + 10;
        helper.startSequence().thenExecute(() -> {
            // vorn das beschädigte Katapult mit dem Knecht daneben, hinten zwei Katapulte mit Mannschaft
            w[0] = weapon(level, helper.absoluteVec(new Vec3(2.0, 1, 2.0)), player);
            w[0].setHealthFraction(0.4f);
            SoldierEntity mender = soldier(level, SoldierType.ENGINEER, helper.absoluteVec(new Vec3(7.0, 1, 1.5)), 90.0f, player);
            mender.setMode(SoldierEntity.MODE_HOLD, mender.blockPosition());
            w[1] = weapon(level, helper.absoluteVec(new Vec3(2.0, 1, 7.0)), player);
            w[2] = weapon(level, helper.absoluteVec(new Vec3(7.0, 1, 7.0)), player);
            crew[0] = soldier(level, SoldierType.ENGINEER, helper.absoluteVec(new Vec3(2.0, 1, 5.0)), 180.0f, player);
            crew[1] = soldier(level, SoldierType.SWORDSMAN, helper.absoluteVec(new Vec3(7.0, 1, 5.0)), 180.0f, player);
            crew[0].startRiding(w[1], true, true);
            crew[1].startRiding(w[2], true, true);
        })
                .thenIdle(2)
                .thenExecute(() -> {
                    helper.assertTrue(crew[0].getVehicle() == w[1] && crew[1].getVehicle() == w[2], "Mannschaft steigt nicht auf");
                    w[1].devPose(since);
                    w[2].devPose(since);
                })
                .thenIdle(40)
                .thenExecute(() -> {
                    float fast = w[1].sinceFired(0.0f) - since;
                    float slow = w[2].sinceFired(0.0f) - since;
                    helper.assertTrue(fast >= slow * 1.35f && slow >= 38.0f, "Nachladen mit Belagerungsknecht: " + fast + " gegen " + slow);
                })
                .thenWaitUntil(() -> helper.assertTrue(w[0].healthFraction() > 0.45f, "der Belagerungsknecht bessert nicht aus ("
                        + Math.round(w[0].healthFraction() * 100) + " %)"))
                .thenExecute(() -> {
                    for (SiegeWeaponEntity weapon : w) {
                        weapon.ejectPassengers();
                        weapon.discard();
                    }
                    for (SoldierEntity s : level.getEntitiesOfClass(SoldierEntity.class, new AABB(helper.absolutePos(BlockPos.ZERO)).inflate(12))) {
                        s.discard();
                    }
                    leave(helper, player);
                })
                .thenSucceed();
    }

    private static SiegeWeaponEntity weapon(ServerLevel level, Vec3 at, ServerPlayer owner) {
        SiegeWeaponEntity weapon = ModEntities.siegeWeapon(SiegeType.CATAPULT).create(level, EntitySpawnReason.COMMAND);
        weapon.snapTo(at.x, at.y, at.z, 180.0f, 0.0f);
        weapon.setYHeadRot(180.0f);
        weapon.setOwner(owner.getUUID());
        level.addFreshEntity(weapon);
        return weapon;
    }
}
