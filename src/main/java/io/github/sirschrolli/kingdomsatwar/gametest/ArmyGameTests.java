package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.army.Mercenaries;
import io.github.sirschrolli.kingdomsatwar.army.Recruitment;
import io.github.sirschrolli.kingdomsatwar.army.Training;
import io.github.sirschrolli.kingdomsatwar.block.entity.MercenaryBoardBlockEntity;
import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuilding;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Armee: Ausbilden am Feldherrentisch (Ränge mit ihren Werten, Beförderung zur nächsten Art mit Name, Rang
 * und Herrn, Speichern), die Söldnertafel (füllt auf, heuert an, prüft Geld und Armeegröße) und das Söldnerhaus der
 * Dörfer in jeder Bauweise.
 */
final class ArmyGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final Identifier TOWN = KingdomsAtWar.id("test_town");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SOLDIER_TRAINING =
            ModGameTests.TEST_FUNCTIONS.register("soldier_training", () -> ArmyGameTests::training);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MERCENARY_BOARD =
            ModGameTests.TEST_FUNCTIONS.register("mercenary_board", () -> ArmyGameTests::board);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VILLAGE_MERCENARY_HALL =
            ModGameTests.TEST_FUNCTIONS.register("village_mercenary_hall", () -> ArmyGameTests::hall);

    private ArmyGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        add(event, environment, SOLDIER_TRAINING, ARENA, 200);
        add(event, environment, MERCENARY_BOARD, ARENA, 200);
        add(event, environment, VILLAGE_MERCENARY_HALL, TOWN, 300);
    }

    private static void add(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment,
            DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> test, Identifier structure, int maxTicks) {
        event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                new TestData<>(environment, Level.OVERWORLD, structure, maxTicks, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.STONE);
            }
        }
    }

    private static ServerPlayer player(GameTestHelper helper, Vec3 local) {
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.getAbilities().instabuild = false;
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        player.getInventory().clearContent();
        Vec3 at = helper.absoluteVec(local);
        player.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        return player;
    }

    // =====================================================================================
    // Ausbilden
    // =====================================================================================

    /**
     * Ein Schwertkämpfer wird am Tisch Veteran (27 Bronze und 3 Eisen), dann Elite (45 Bronze und 2 Stahl) und dann
     * Ritter (81 Bronze und 2 Stahl): Er behält Herrn, Namen und Rang, seine Werte wachsen mit.
     */
    private static void training(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos tableLocal = new BlockPos(4, 1, 4);
        helper.setBlock(tableLocal, ModBlocks.RECRUITMENT_TABLE.get());
        BlockPos table = helper.absolutePos(tableLocal);
        ServerPlayer player = player(helper, new Vec3(4.5, 1, 1.5));
        try {
            SoldierEntity soldier = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(2, 1, 4));
            soldier.finalizeSpawn(level, level.getCurrentDifficultyAt(soldier.blockPosition()), EntitySpawnReason.COMMAND, null);
            soldier.setOwner(player);
            String name = soldier.getPersonalName();
            helper.assertTrue(!name.isEmpty(), "Soldat ohne eigenen Namen");
            helper.assertValueEqual(soldier.getRank(), 0, "Rang am Anfang");
            double health = soldier.getAttributeValue(Attributes.MAX_HEALTH);
            double armor = soldier.getAttributeValue(Attributes.ARMOR);
            // ohne Geld geht nichts
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.NOT_ENOUGH,
                    "Befördern ohne Münzen muss scheitern");
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 2));
            player.getInventory().add(new ItemStack(Items.IRON_INGOT, 3));
            player.getInventory().add(new ItemStack(ModItems.STEEL_INGOT.get(), 4));
            // Veteran
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.TRAINED,
                    "Befördern zum Veteranen klappt nicht");
            helper.assertValueEqual(soldier.getRank(), 1, "Rang nach dem ersten Befördern");
            helper.assertValueEqual(Coins.total(player), 162 - 27, "Münzen nach dem ersten Befördern");
            helper.assertValueEqual(player.getInventory().countItem(Items.IRON_INGOT), 0, "Eisen nach dem ersten Befördern");
            helper.assertTrue(Math.abs(soldier.getAttributeValue(Attributes.MAX_HEALTH) - health * 1.25) < 0.01, "Leben des Veteranen");
            helper.assertTrue(Math.abs(soldier.getAttributeValue(Attributes.ARMOR) - (armor + 2.0)) < 0.01, "Rüstung des Veteranen");
            helper.assertTrue(soldier.getCustomName() != null && soldier.getCustomName().getString().contains("★"), "Name zeigt den Rang nicht");
            // Elite
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.TRAINED,
                    "Befördern zur Elite klappt nicht");
            helper.assertValueEqual(soldier.getRank(), 2, "Rang nach dem zweiten Befördern");
            helper.assertValueEqual(Coins.total(player), 162 - 27 - 45, "Münzen nach dem zweiten Befördern");
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.RANK) == Recruitment.Result.MAX_RANK,
                    "über Elite hinaus darf es nicht gehen");
            // zum Ritter
            helper.assertTrue(Training.train(player, table, soldier.getId(), Training.Action.PROMOTE) == Recruitment.Result.PROMOTED,
                    "Ausbildung zum Ritter klappt nicht");
            helper.assertTrue(!soldier.isAlive(), "der Schwertkämpfer ist noch da");
            helper.assertValueEqual(Coins.total(player), 162 - 27 - 45 - 81, "Münzen nach der Ausbildung");
            helper.assertValueEqual(player.getInventory().countItem(ModItems.STEEL_INGOT.get()), 0, "Stahl nach der Ausbildung");
            List<SoldierEntity> knights = level.getEntitiesOfClass(SoldierEntity.class, new AABB(table).inflate(10),
                    s -> s.getSoldierType() == SoldierType.KNIGHT);
            helper.assertValueEqual(knights.size(), 1, "Ritter nach der Ausbildung");
            SoldierEntity knight = knights.getFirst();
            helper.assertTrue(knight.isOwnedBy(player), "der Ritter gehört nicht dem Spieler");
            helper.assertValueEqual(knight.getPersonalName(), name, "Name des Ritters");
            helper.assertValueEqual(knight.getRank(), 2, "Rang des Ritters");
            helper.assertTrue(Math.abs(knight.getAttributeValue(Attributes.MAX_HEALTH) - SoldierType.KNIGHT.health() * 1.5) < 0.01,
                    "Leben des Elite-Ritters");
            helper.assertTrue(knight.getOffhandItem().is(Items.SHIELD), "der Ritter hat keinen Schild");
            // Hauptmann braucht einen Edelstein
            helper.assertTrue(Training.train(player, table, knight.getId(), Training.Action.PROMOTE) == Recruitment.Result.NOT_ENOUGH,
                    "Hauptmann ohne Edelstein muss scheitern");
            // fremde Soldaten bildet man nicht aus
            SoldierEntity stranger = helper.spawn(ModEntities.SOLDIERS.get(SoldierType.ARCHER).get(), new BlockPos(6, 1, 4));
            helper.assertTrue(Training.train(player, table, stranger.getId(), Training.Action.RANK) == Recruitment.Result.NOT_HERE,
                    "fremder Soldat darf nicht ausgebildet werden");
            // Rang und Name überstehen Speichern und Laden
            TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
            knight.saveWithoutId(out);
            CompoundTag tag = out.buildResult();
            SoldierEntity loaded = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.LOAD);
            loaded.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
            helper.assertValueEqual(loaded.getRank(), 2, "Rang nach dem Laden");
            helper.assertValueEqual(loaded.getPersonalName(), name, "Name nach dem Laden");
            helper.assertTrue(Math.abs(loaded.getAttributeValue(Attributes.MAX_HEALTH) - SoldierType.KNIGHT.health() * 1.5) < 0.01,
                    "Leben nach dem Laden");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    // =====================================================================================
    // Söldnertafel
    // =====================================================================================

    /**
     * Die Tafel ruft drei Söldner herbei; einer lässt sich mit Münzen anheuern (ohne genug Münzen oder aus der Ferne
     * nicht), danach folgt er dem Spieler und gehört nicht mehr zum Haus; am nächsten Morgen ist wieder voll.
     */
    private static void board(GameTestHelper helper) {
        floor(helper);
        ServerLevel level = helper.getLevel();
        BlockPos boardLocal = new BlockPos(4, 1, 6);
        helper.setBlock(boardLocal, ModBlocks.MERCENARY_BOARD.get().defaultBlockState().setValue(HorizontalDirectionalBlock.FACING, Direction.NORTH));
        BlockPos boardPos = helper.absolutePos(boardLocal);
        MercenaryBoardBlockEntity board = helper.getBlockEntity(boardLocal, MercenaryBoardBlockEntity.class);
        int came = board.refill(level, false);
        helper.assertValueEqual(came, Mercenaries.MAX_AT_HALL, "Söldner an einer neuen Tafel");
        List<SoldierEntity> waiting = Mercenaries.at(level, boardPos);
        helper.assertValueEqual(waiting.size(), Mercenaries.MAX_AT_HALL, "wartende Söldner");
        for (SoldierEntity s : waiting) {
            helper.assertTrue(!s.hasOwner() && boardPos.equals(s.getHall()), "Söldner gehört nicht zur Tafel");
            helper.assertTrue(s.getMode() == SoldierEntity.MODE_HOLD, "Söldner wartet nicht an seinem Platz");
            helper.assertTrue(s.isPersistenceRequired(), "Söldner würde verschwinden");
            helper.assertTrue(s.hirePrice() % 9 == 0 && s.hirePrice() >= s.getSoldierType().coins(), "Sold ist kein ganzes Silber");
        }
        helper.assertValueEqual(board.refill(level, false), 0, "eine volle Tafel ruft niemanden mehr");
        SoldierEntity mercenary = waiting.getFirst();
        int price = mercenary.hirePrice();
        ServerPlayer player = player(helper, new Vec3(4.5, 1, 3.5));
        try {
            helper.assertTrue(Mercenaries.hire(player, boardPos, mercenary.getId()) == Recruitment.Result.NOT_ENOUGH,
                    "Anheuern ohne Münzen muss scheitern");
            player.getInventory().add(new ItemStack(ModItems.GOLD_COIN.get(), 8));
            Vec3 far = helper.absoluteVec(new Vec3(4.5, 1, 30.5));
            player.snapTo(far.x, far.y, far.z, 0.0f, 0.0f);
            helper.assertTrue(Mercenaries.hire(player, boardPos, mercenary.getId()) == Recruitment.Result.TOO_FAR,
                    "Anheuern aus der Ferne muss scheitern");
            Vec3 near = helper.absoluteVec(new Vec3(4.5, 1, 3.5));
            player.snapTo(near.x, near.y, near.z, 0.0f, 0.0f);
            helper.assertTrue(Mercenaries.hire(player, boardPos, mercenary.getId()) == Recruitment.Result.HIRED, "Anheuern klappt nicht");
            helper.assertTrue(mercenary.isOwnedBy(player), "der Söldner gehört nicht dem Spieler");
            helper.assertTrue(!mercenary.isMercenary() && mercenary.getMode() == SoldierEntity.MODE_FOLLOW, "der Söldner folgt nicht");
            helper.assertValueEqual(Coins.total(player), 8 * 81 - price, "Münzen nach dem Anheuern");
            helper.assertValueEqual(Mercenaries.at(level, boardPos).size(), Mercenaries.MAX_AT_HALL - 1, "Söldner nach dem Anheuern");
            helper.assertTrue(Mercenaries.hire(player, boardPos, mercenary.getId()) == Recruitment.Result.NOT_HERE,
                    "derselbe Söldner lässt sich nicht zweimal anheuern");
            helper.assertValueEqual(board.refill(level, true), 1, "am nächsten Morgen kommt ein neuer");
            helper.succeed();
        } finally {
            player.discard();
        }
    }

    // =====================================================================================
    // Söldnerhaus
    // =====================================================================================

    /**
     * Das Söldnerhaus in jeder Bauweise (eins auch gedreht): Es hat seine Tafel mit fünf Warteplätzen, die frei stehen;
     * die Tafel ist von der Straße aus zu erreichen, und sie ruft drei Söldner herbei.
     */
    private static void hall(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        VillageBuilding kind = VillageBuilding.MERCENARY_HALL;
        List<String> problems = new ArrayList<>();
        int built = 0;
        for (VillageStyle style : VillageStyle.values()) {
            for (Rotation rotation : style == VillageStyle.TIMBER ? new Rotation[] {Rotation.NONE, Rotation.CLOCKWISE_90}
                    : new Rotation[] {Rotation.NONE}) {
                VillageGameTests.clear(helper);
                int floor = style == VillageStyle.STILT ? 2 : 0;
                if (floor > 0) {
                    for (BlockPos pos : BlockPos.betweenClosed(helper.absolutePos(new BlockPos(14, floor, 4)),
                            helper.absolutePos(new BlockPos(34, floor, 13)))) {
                        level.setBlock(pos, Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                    }
                }
                BlockPos origin = rotation == Rotation.NONE ? new BlockPos(24 - kind.width / 2, floor, 14) : new BlockPos(30, floor, 14);
                KingdomVillagePiece piece = VillageGameTests.build(helper, style, kind, origin, rotation);
                String name = style.id() + (rotation == Rotation.NONE ? "" : "/gedreht");
                BoundingBox box = piece.getBoundingBox();
                BlockPos boardPos = null;
                for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                    if (level.getBlockState(pos).is(ModBlocks.MERCENARY_BOARD.get())) {
                        boardPos = pos.immutable();
                        break;
                    }
                }
                if (boardPos == null) {
                    problems.add(name + ": keine Söldnertafel");
                    continue;
                }
                built++;
                BlockState state = level.getBlockState(boardPos);
                Direction front = state.getValue(HorizontalDirectionalBlock.FACING);
                Direction expected = rotation.rotate(Direction.NORTH);
                if (front != expected) {
                    problems.add(name + ": Tafel schaut nach " + front + " statt " + expected);
                }
                if (!(level.getBlockEntity(boardPos) instanceof MercenaryBoardBlockEntity board)) {
                    problems.add(name + ": Tafel ohne Daten");
                    continue;
                }
                if (board.spots().size() != 5) {
                    problems.add(name + ": " + board.spots().size() + " statt 5 Warteplätze");
                }
                if (board.village().isEmpty()) {
                    problems.add(name + ": Tafel kennt ihr Dorf nicht");
                }
                for (BlockPos spot : board.spots()) {
                    boolean floorOk = level.getBlockState(spot.below()).isFaceSturdy(level, spot.below(), Direction.UP);
                    boolean free = level.getBlockState(spot).getCollisionShape(level, spot).isEmpty()
                            && level.getBlockState(spot.above()).getCollisionShape(level, spot.above()).isEmpty();
                    if (!floorOk || !free) {
                        problems.add(name + ": Warteplatz " + helper.relativePos(spot) + " ist verbaut");
                    }
                    if (spot.distSqr(boardPos) > 16) {
                        problems.add(name + ": Warteplatz " + helper.relativePos(spot) + " zu weit von der Tafel");
                    }
                }
                int came = board.refill(level, false);
                if (came != Mercenaries.MAX_AT_HALL) {
                    problems.add(name + ": " + came + " statt " + Mercenaries.MAX_AT_HALL + " Söldner");
                }
                // von der Straße vor dem Tor zur Tafel (ein Söldner läuft den Weg, den der Spieler nimmt)
                List<SoldierEntity> waiting = Mercenaries.at(level, boardPos);
                if (!waiting.isEmpty()) {
                    SoldierEntity walker = waiting.getFirst();
                    BlockPos street = helper.absolutePos(rotation == Rotation.NONE ? new BlockPos(24, floor + 1, 11) : new BlockPos(32, floor + 1, 20));
                    BlockPos reading = boardPos.relative(front);
                    walker.teleportTo(street.getX() + 0.5, street.getY(), street.getZ() + 0.5);
                    walker.setOnGround(true);
                    var path = walker.getNavigation().createPath(reading, 0);
                    if (path == null || !path.canReach()) {
                        problems.add(name + ": Tafel von der Straße aus nicht erreichbar");
                    }
                }
            }
        }
        VillageGameTests.clear(helper);
        helper.assertTrue(built == 5, "nur " + built + " Söldnerhäuser gebaut");
        helper.assertTrue(problems.isEmpty(), "Söldnerhaus: " + String.join("; ", problems));
        helper.succeed();
    }
}
