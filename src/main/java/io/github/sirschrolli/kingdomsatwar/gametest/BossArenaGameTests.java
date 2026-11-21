package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaIsland;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenas;
import io.github.sirschrolli.kingdomsatwar.arena.BossFights;
import io.github.sirschrolli.kingdomsatwar.arena.BossLair;
import io.github.sirschrolli.kingdomsatwar.arena.MageArena;
import io.github.sirschrolli.kingdomsatwar.block.abyss.HoardSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePiece;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
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
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.SignBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu den Kampfdimensionen der großen Bosse: die sieben Welten und ihre Regeln, das herausgerissene Stück Welt (ohne
 * Beute, Portale und Wesen, die Kopie steht still), jeder Hort zieht seine Gegner in sein Abbild und erwacht dort, nach
 * der Niederlage ruht der Hort wieder; ein ganzer Kampf mit Sieg (Beute und Spieler zurück, der Hort besiegt) und das
 * Zurücksetzen eines Bosses, dessen letzter Gegner gefallen ist.
 */
final class BossArenaGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLDS =
            ModGameTests.TEST_FUNCTIONS.register("boss_arena_worlds", () -> BossArenaGameTests::worlds);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ISLAND =
            ModGameTests.TEST_FUNCTIONS.register("boss_arena_island", () -> BossArenaGameTests::island);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LAIRS =
            ModGameTests.TEST_FUNCTIONS.register("boss_arena_lairs", () -> BossArenaGameTests::lairs);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> VICTORY =
            ModGameTests.TEST_FUNCTIONS.register("boss_arena_victory", () -> BossArenaGameTests::victory);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LAST_STAND =
            ModGameTests.TEST_FUNCTIONS.register("boss_arena_last_stand", () -> BossArenaGameTests::lastStand);

    private BossArenaGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(WORLDS, ISLAND, LAST_STAND)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(LAIRS, VICTORY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 2400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Ein Feld fern der Prüfflächen: Chunks erzwungen, am Ende geräumt. */
    private record Field(ServerLevel level, List<long[]> chunks, List<AABB> areas, List<ServerPlayer> players) {
        static Field of(ServerLevel level) {
            return new Field(level, new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }

        void hold(BlockPos center, int reach) {
            for (long[] c : BossArenas.chunks(center, reach)) {
                this.level.getChunk((int) c[0], (int) c[1]);
                this.level.setChunkForced((int) c[0], (int) c[1], true);
                this.chunks.add(c);
            }
            this.areas.add(new AABB(center).inflate(reach + 8, 220, reach + 8));
        }

        /** Sind die Chunks samt ihren Wesen geladen (erst dann sieht man, wer dort erscheint)? */
        boolean loaded() {
            for (long[] c : this.chunks) {
                if (!this.level.areEntitiesLoaded(net.minecraft.world.level.ChunkPos.pack((int) c[0], (int) c[1]))) {
                    return false;
                }
            }
            return true;
        }

        void close() {
            for (AABB area : this.areas) {
                for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                    e.discard();
                }
            }
            // nur die eigenen Kämpfe (andere Tests laufen zugleich)
            BossFights fights = BossFights.of(this.level.getServer());
            for (BossFights.Fight fight : fights.all()) {
                if (this.areas.stream().anyMatch(a -> a.contains(Vec3.atCenterOf(fight.center())) || a.contains(Vec3.atCenterOf(fight.lair())))) {
                    fights.remove(fight);
                }
            }
            fights.save(this.level.getServer());
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
            for (ServerPlayer player : this.players) {
                this.level.getServer().getPlayerList().remove(player);
            }
        }
    }

    private static void floor(ServerLevel level, BlockPos center, int radius, BlockState state) {
        for (int x = -radius; x <= radius; x++) {
            for (int z = -radius; z <= radius; z++) {
                level.setBlock(center.offset(x, 0, z), state, Block.UPDATE_CLIENTS);
            }
        }
    }

    // =====================================================================================
    // Die sieben Welten und ihre Regeln
    // =====================================================================================

    /**
     * Sieben Kampfdimensionen, jede mit eigenem Typ (feste Zeit, hoch genug für ihren Hort) und eigenem Biom; jede bildet
     * eine Welt nach (Waffen, die dort stärker sind, sind es auch hier); in allen gilt das Bauverbot – außer im Kreativmodus.
     */
    private static void worlds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.getServer().registryAccess();
        var types = registries.lookupOrThrow(Registries.DIMENSION_TYPE);
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        List<String> ids = new ArrayList<>();
        for (BossArenaKind kind : BossArenaKind.values()) {
            helper.assertFalse(ids.contains(kind.id()), "Kennung doppelt: " + kind.id());
            ids.add(kind.id());
            helper.assertTrue(BossArenaKind.of(kind.level()) == kind && BossArenaKind.isArena(kind.level()), "Welt nicht erkannt: " + kind.id());
            helper.assertTrue(biomes.get(kind.biome()).isPresent(), "Biom fehlt: " + kind.id());
            var type = types.get(kind.type());
            helper.assertTrue(type.isPresent(), "Dimensionstyp fehlt: " + kind.id());
            helper.assertTrue(type.get().value().hasFixedTime(), "Die Zeit steht nicht still: " + kind.id());
            helper.assertTrue(BossArenaKind.counts(level, Level.OVERWORLD) && !BossArenaKind.isArena(level), "Die Oberwelt gilt als Kampfdimension");
        }
        // hoch genug für die neu gebauten Horte
        var maw = types.getOrThrow(BossArenaKind.WORLD_EATER.type()).value();
        helper.assertTrue(maw.minY() <= HoardPiece.FLOOR - 80 && maw.minY() + maw.height() > HoardPiece.FLOOR + 90, "Der Weltenschlund ist zu niedrig");
        var throne = types.getOrThrow(BossArenaKind.KING.type()).value();
        helper.assertTrue(throne.minY() <= ThronePiece.FLOOR - 40 && throne.minY() + throne.height() > ThronePiece.FLOOR + 40,
                "Die Thronwelt ist zu niedrig");
        helper.assertTrue(BossArenaKind.ASTERIA.home() == Level.END && BossArenaKind.MALLEUS.home() == Level.NETHER, "Falsche Heimatwelten");
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        try {
            for (BossArenaKind kind : BossArenaKind.values()) {
                helper.assertTrue(MageArena.forbidden(player, kind.level()), "In " + kind.id() + " darf man bauen");
            }
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
        helper.succeed();
    }

    // =====================================================================================
    // Das herausgerissene Stück Welt
    // =====================================================================================

    /**
     * Ein Stück Welt wird zur Insel: der Boden kommt mit (außerhalb der Form nichts), die Truhe kommt leer, das Schild
     * behält seine Schrift, ein Netherportal bleibt zurück, und die Kopie steht still – Sand fällt nicht, Lava fließt nicht.
     */
    private static void island(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos from = new BlockPos(base.getX() + 14_000, 150, base.getZ() + 9_000);
        BlockPos to = new BlockPos(base.getX() + 14_000, 150, base.getZ() + 9_400);
        Field field = Field.of(level);
        field.hold(from, 24);
        field.hold(to, 24);
        try {
            floor(level, from, 5, Blocks.STONE_BRICKS.defaultBlockState());
            level.setBlock(from.offset(9, 0, 0), Blocks.GOLD_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);      // außerhalb der Form
            level.setBlock(from.offset(1, 1, 1), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(from.offset(1, 1, 1)) instanceof Container chest) {
                chest.setItem(0, new ItemStack(Items.DIAMOND, 5));
            }
            level.setBlock(from.offset(-1, 1, 1), Blocks.OAK_SIGN.defaultBlockState(), Block.UPDATE_CLIENTS);
            if (level.getBlockEntity(from.offset(-1, 1, 1)) instanceof SignBlockEntity sign) {
                sign.setText(net.minecraft.world.level.block.entity.SignText.EMPTY.withColor(net.minecraft.world.item.DyeColor.RED).withGlowingText(true),
                        net.minecraft.world.level.block.entity.SignTextSlot.FRONT);
            }
            level.setBlock(from.offset(0, 3, -2), Blocks.SAND.defaultBlockState(), Block.UPDATE_CLIENTS);           // schwebt
            level.setBlock(from.offset(4, 1, 0), Blocks.LAVA.defaultBlockState(), Block.UPDATE_CLIENTS);            // am Rand
            level.setBlock(from.offset(-2, 1, -2), Blocks.NETHER_PORTAL.defaultBlockState(), Block.UPDATE_CLIENTS | Block.UPDATE_KNOWN_SHAPE);
            int placed = ArenaIsland.copy(level, from, level, to, new ArenaIsland.Shape(7, 3, 6, false, 5));
            helper.assertTrue(placed >= 100, "Zu wenig kopiert: " + placed);
            helper.assertTrue(level.getBlockState(to).is(Blocks.STONE_BRICKS) && level.getBlockState(to.offset(3, 0, -3)).is(Blocks.STONE_BRICKS),
                    "Der Boden kam nicht mit");
            helper.assertTrue(level.getBlockState(to.offset(9, 0, 0)).isAir(), "Auch was außerhalb der Form lag, kam mit");
            helper.assertTrue(level.getBlockState(to.offset(1, 1, 1)).is(Blocks.CHEST) && level.getBlockEntity(to.offset(1, 1, 1)) instanceof Container c
                    && c.isEmpty(), "Die Truhe kam nicht leer mit");
            helper.assertTrue(level.getBlockEntity(to.offset(-1, 1, 1)) instanceof SignBlockEntity sign
                    && sign.getText(net.minecraft.world.level.block.entity.SignTextSlot.FRONT).getColor() == net.minecraft.world.item.DyeColor.RED
                    && sign.getText(net.minecraft.world.level.block.entity.SignTextSlot.FRONT).hasGlowingText(), "Das Schild verlor seine Schrift");
            helper.assertTrue(level.getBlockState(to.offset(-2, 1, -2)).isAir(), "Das Portal kam mit");
            helper.runAfterDelay(30, () -> {
                try {
                    helper.assertTrue(level.getBlockState(to.offset(0, 3, -2)).is(Blocks.SAND), "Der Sand fiel herab");
                    int lava = 0;
                    for (BlockPos p : BlockPos.betweenClosed(to.offset(-8, -4, -8), to.offset(8, 3, 8))) {
                        if (level.getFluidState(p).is(net.minecraft.tags.FluidTags.LAVA)) {
                            lava++;
                        }
                    }
                    helper.assertTrue(lava == 1, "Die Lava floss aus: " + lava);
                    ArenaIsland.clear(level, to, 8, 4, 7);
                    helper.assertTrue(level.getBlockState(to).isAir() && level.getBlockState(to.offset(1, 1, 1)).isAir(), "Die Insel blieb stehen");
                    helper.succeed();
                } finally {
                    ArenaIsland.clear(level, from, 10, 2, 5);
                    ArenaIsland.clear(level, to, 10, 4, 7);
                    field.close();
                }
            });
        } catch (RuntimeException e) {
            field.close();
            throw e;
        }
    }

    // =====================================================================================
    // Jeder Hort zieht in sein Abbild – und ruht nach der Niederlage wieder
    // =====================================================================================

    /** Baut den Hort einer Art (nur, was der Kampf braucht) um {@code at}; gibt die Lage seiner Blockentität zurück. */
    private static BlockPos lair(ServerLevel level, BossArenaKind kind, BlockPos at) {
        switch (kind) {
            case MYSTIK -> {
                floor(level, at.below(), 12, Blocks.SPRUCE_PLANKS.defaultBlockState());
                level.setBlock(at, MystikBlocks.STAGE_HAT.get().defaultBlockState(), Block.UPDATE_ALL);
                return at;
            }
            case MALLEUS -> {
                floor(level, at.below(), 12, Blocks.NETHER_BRICKS.defaultBlockState());
                level.setBlock(at, ForgeBlocks.GLUT_ANVIL.get().defaultBlockState(), Block.UPDATE_ALL);
                return at;
            }
            case ASTERIA -> {
                floor(level, at.below(2), 13, Blocks.END_STONE_BRICKS.defaultBlockState());
                level.setBlock(at.below(), Blocks.PURPUR_PILLAR.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(at, StarBlocks.STARSTONE.get().defaultBlockState(), Block.UPDATE_ALL);
                return at;
            }
            case ANNI -> {
                floor(level, at.below(2), 13, Blocks.GRASS_BLOCK.defaultBlockState());
                level.setBlock(at.below(), Blocks.CRYING_OBSIDIAN.defaultBlockState(), Block.UPDATE_ALL);
                level.setBlock(at, AnniBlocks.ERROR_CORE.get().defaultBlockState(), Block.UPDATE_ALL);
                return at;
            }
            case KING -> {
                // das Siegel unter dem Thron; der Platz selbst (Höhe des Bodens fest) entsteht nur im Abbild
                BlockPos center = new BlockPos(at.getX(), ThronePiece.FLOOR + 1, at.getZ());
                floor(level, center.below(), 8, Blocks.POLISHED_BLACKSTONE.defaultBlockState());
                BlockPos seal = center.offset(0, -4, ThronePiece.THRONE_Z);
                level.setBlock(seal, VoidBlocks.THRONE_SEAL.get().defaultBlockState(), Block.UPDATE_ALL);
                if (level.getBlockEntity(seal) instanceof ThroneSealBlockEntity be) {
                    be.setup(center);
                }
                return seal;
            }
            case WORLD_EATER -> {
                BlockPos center = new BlockPos(at.getX(), HoardPiece.FLOOR + 1, at.getZ());
                floor(level, center.offset(0, -1, 18), 6, AbyssBlocks.POLISHED_CHAOS_STONE.base().get().defaultBlockState());
                BlockPos seal = HoardPiece.sealPosition(center);
                level.setBlock(seal, AbyssBlocks.HOARD_SEAL.get().defaultBlockState(), Block.UPDATE_ALL);
                if (level.getBlockEntity(seal) instanceof HoardSealBlockEntity be) {
                    be.setup(center);
                }
                return seal;
            }
            case CREATOR -> {
                floor(level, at.below(), 10, Blocks.SMOOTH_QUARTZ.defaultBlockState());
                level.setBlock(at, CreatorBlocks.CREATOR_ALTAR.get().defaultBlockState(), Block.UPDATE_ALL);
                return at;
            }
        }
        return at;
    }

    /**
     * Alle sieben Horte: Wer am Hort steht, landet im Abbild (in der Arena, nahe der Mitte), dort erwacht der Boss sofort
     * (gebunden an das Abbild), das Original wartet; steht niemand mehr in der Arena, ist der Kampf verloren – der Boss
     * vergeht, der Hort ruht wieder und ist bereit für einen neuen, unversehrten Boss.
     */
    private static void lairs(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        Field field = Field.of(level);
        BossArenaKind[] kinds = BossArenaKind.values();
        for (int i = 0; i < kinds.length; i++) {
            field.hold(new BlockPos(base.getX() + 20_000 + i * 600, 120, base.getZ() - 12_000), 40);
            // die Schöpfung ist riesig: für die Probe genügt ihre Mitte (den Rest lädt der Bau selbst)
            field.hold(new BlockPos(base.getX() + 20_000 + i * 600, 120, base.getZ() - 14_000),
                    kinds[i] == BossArenaKind.CREATOR ? 48 : kinds[i].reach());
        }
        // erst die Chunks samt ihren Wesen laden (unter Last dauert das ein paar Ticks)
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(field.loaded(), "Die Chunks der Felder laden nicht"))
                .thenExecute(() -> lairsBody(helper, field, base))
                .thenSucceed();
    }

    private static void lairsBody(GameTestHelper helper, Field field, BlockPos base) {
        ServerLevel level = helper.getLevel();
        List<String> problems = new ArrayList<>();
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        field.players().add(player);
        BossFights fights = BossFights.of(level.getServer());
        try {
            int i = 0;
            for (BossArenaKind kind : BossArenaKind.values()) {
                BlockPos at = new BlockPos(base.getX() + 20_000 + i * 600, 120, base.getZ() - 12_000);
                BlockPos center = new BlockPos(base.getX() + 20_000 + i * 600, 120, base.getZ() - 14_000);
                i++;
                BlockPos lairPos = lair(level, kind, at);
                if (!(level.getBlockEntity(lairPos) instanceof BossLair lair)) {
                    problems.add(kind.id() + ": kein Hort");
                    continue;
                }
                BlockPos focus = lair.arenaFocus();
                // die Schöpfung steht immer gleich hoch (über dem Boden der Testwelt)
                BlockPos arenaCenter = new BlockPos(center.getX(), kind.arenaY(focus.getY()), center.getZ());
                Vec3 start = new Vec3(focus.getX() + 3.5, focus.getY() + (kind == BossArenaKind.ASTERIA || kind == BossArenaKind.ANNI ? -1 : 0),
                        focus.getZ() + 0.5);
                if (kind == BossArenaKind.WORLD_EATER) {
                    start = new Vec3(focus.getX() + 0.5, focus.getY(), focus.getZ() + 18.5);
                }
                player.teleportTo(level, start.x, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
                BossFights.Fight fight = BossArenas.pull(level, lairPos, level, 900 + i, arenaCenter);
                if (fight == null) {
                    problems.add(kind.id() + ": niemand wird hineingezogen");
                    continue;
                }
                if (!BossArenas.inArena(player, level, fight) || player.position().distanceTo(Vec3.atCenterOf(arenaCenter)) > kind.reach()) {
                    problems.add(kind.id() + ": Spieler nicht in der Arena (" + player.position() + ")");
                }
                if (!(level.getBlockEntity(fight.trigger()) instanceof BossLair replica) || !replica.isArenaReplica()) {
                    problems.add(kind.id() + ": kein Abbild des Horts bei " + fight.trigger());
                } else {
                    LivingEntity boss = replica.arenaBoss(level);
                    if (boss == null) {
                        problems.add(kind.id() + ": der Boss erwacht nicht im Abbild");
                    } else if (boss.position().distanceTo(Vec3.atCenterOf(arenaCenter)) > kind.reach() + 40) {
                        problems.add(kind.id() + ": der Boss erwacht fern der Arena (" + boss.position() + ")");
                    }
                    if (lair.arenaBoss(level) != null) {
                        problems.add(kind.id() + ": am Original ist auch ein Boss");
                    }
                }
                if (!BossArenas.busy(level, lairPos)) {
                    problems.add(kind.id() + ": das Original weiß nichts vom Kampf");
                }
                if (kind == BossArenaKind.WORLD_EATER && !level.getBlockState(HoardPiece.shardPosition(arenaCenter, 0)).is(AbyssBlocks.SEAL_SHARD.get())) {
                    problems.add(kind.id() + ": der Schlund wurde nicht gebaut");
                }
                // niemand mehr in der Arena: verloren
                player.teleportTo(level, start.x, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
                BossArenas.tick(level.getServer(), level, fights, fight);
                if (fights.byLair(level.dimension(), lairPos) != null) {
                    problems.add(kind.id() + ": der verlorene Kampf endet nicht");
                }
                if (lair.arenaDefeated() || BossArenas.busy(level, lairPos)) {
                    problems.add(kind.id() + ": der Hort ruht nach der Niederlage nicht");
                }
                if (level.getBlockEntity(fight.trigger()) instanceof BossLair replica && replica.arenaBoss(level) != null) {
                    problems.add(kind.id() + ": der Boss blieb nach der Niederlage");
                }
            }
        } finally {
            field.close();
        }
        helper.assertTrue(problems.isEmpty(), "Kampfdimensionen: " + problems);
    }

    // =====================================================================================
    // Ein ganzer Kampf mit Sieg
    // =====================================================================================

    /**
     * Asteria zieht den Spieler ins Sternenzelt (ein Nachzügler folgt); sie fällt: Der Sieg wird vermerkt, nach kurzer Zeit
     * kehren alle an ihren Platz zurück, die Beute (und ihre Erfahrung) liegt am Sternstein, und der Stein ist erloschen.
     */
    private static void victory(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos at = new BlockPos(base.getX() - 17_000, 130, base.getZ() + 21_000);
        BlockPos center = new BlockPos(base.getX() - 17_000, 130, base.getZ() + 23_000);
        Field field = Field.of(level);
        field.hold(at, 40);
        field.hold(center, BossArenaKind.ASTERIA.reach());
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(field.loaded(), "Die Chunks der Felder laden nicht"))
                .thenExecute(() -> victoryBody(helper, field, at, center));
    }

    private static void victoryBody(GameTestHelper helper, Field field, BlockPos at, BlockPos center) {
        ServerLevel level = helper.getLevel();
        BlockPos stonePos = lair(level, BossArenaKind.ASTERIA, at);
        ServerPlayer player = MageGameTests.survivalPlayer(helper);
        ServerPlayer late = MageGameTests.survivalPlayer(helper, "test-latecomer", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        field.players().add(player);
        field.players().add(late);
        Vec3 start = new Vec3(at.getX() + 4.5, at.getY() - 1, at.getZ() + 0.5);
        Vec3 lateStart = new Vec3(at.getX() - 30.5, at.getY() - 1, at.getZ() + 0.5);
        player.teleportTo(level, start.x, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
        late.teleportTo(level, lateStart.x, lateStart.y, lateStart.z, java.util.Set.of(), 0.0f, 0.0f, true);
        BossFights fights = BossFights.of(level.getServer());
        try {
            if (!(level.getBlockEntity(stonePos) instanceof StarstoneBlockEntity stone)) {
                throw new IllegalStateException("Der Sternstein hat keine Blockentität");
            }
            BossFights.Fight fight = BossArenas.pull(level, stonePos, level, 950, center);
            helper.assertTrue(fight != null, "Asteria zieht niemanden ins Sternenzelt");
            helper.assertTrue(fight.fighter(late.getUUID()) == null, "Wer fern war, wurde mitgezogen");
            // ein Nachzügler tritt an den Stein und folgt
            late.teleportTo(level, start.x - 1.0, start.y, start.z, java.util.Set.of(), 0.0f, 0.0f, true);
            helper.assertTrue(BossArenas.join(level, stonePos, late) && BossArenas.inArena(late, level, fight), "Der Nachzügler folgt nicht");
            helper.assertTrue(stone.phase() == StarstoneBlockEntity.Phase.AWAKE, "Der Stein wartet nicht, während drüben gekämpft wird");
            if (!(level.getBlockEntity(fight.trigger()) instanceof StarstoneBlockEntity replica) || !(replica.arenaBoss(level) instanceof AsteriaEntity asteria)) {
                throw new IllegalStateException("Asteria erwacht nicht im Sternenzelt");
            }
            // sie fällt durch die Hand des Spielers (sonst gäbe sie keine Erfahrung)
            asteria.setLastHurtByPlayer(player, 100);
            asteria.kill(level);
            ItemEntity marker = new ItemEntity(level, center.getX() + 2.5, center.getY() + 1.0, center.getZ() + 0.5, new ItemStack(Items.NETHER_STAR));
            level.addFreshEntity(marker);
            drive(helper, field, fights, fight, AsteriaEntity.DEATH_TICKS + BossArenas.RETURN_DELAY + 60, () -> {
                helper.assertTrue(fights.byLair(level.dimension(), stonePos) == null, "Der Kampf endet nach dem Sieg nicht");
                helper.assertTrue(player.position().distanceTo(start) < 1.0, "Der Spieler ist nicht zurück: " + player.position());
                helper.assertTrue(late.position().distanceTo(start.add(-1.0, 0.0, 0.0)) < 1.0, "Der Nachzügler ist nicht zurück: " + late.position());
                helper.assertTrue(stone.phase() == StarstoneBlockEntity.Phase.DEFEATED, "Der Sternstein erlischt nicht");
                AABB loot = new AABB(stone.arenaLootSpot()).inflate(4.0);
                helper.assertTrue(level.getEntitiesOfClass(ItemEntity.class, loot, e -> e.getItem().is(Items.NETHER_STAR)).size() == 1,
                        "Die Beute liegt nicht am Sternstein");
                helper.assertFalse(level.getEntitiesOfClass(ExperienceOrb.class, loot).isEmpty(), "Ihre Erfahrung liegt nicht am Sternstein");
                helper.assertTrue(level.getEntitiesOfClass(AsteriaEntity.class, new AABB(center).inflate(80)).isEmpty(), "Asteria blieb in der Arena");
                field.close();
                helper.succeed();
            });
        } catch (RuntimeException e) {
            field.close();
            throw e;
        }
    }

    /** Treibt den Kampf Tick für Tick voran (auf dem Testserver gibt es die Kampfdimension nicht), bis er endet. */
    private static void drive(GameTestHelper helper, Field field, BossFights fights, BossFights.Fight fight, int maxTicks, Runnable then) {
        ServerLevel level = field.level();
        if (!fights.all().contains(fight)) {
            try {
                then.run();
            } catch (RuntimeException e) {
                field.close();
                throw e;
            }
            return;
        }
        if (maxTicks <= 0) {
            field.close();
            helper.fail("Der Kampf endet nicht (gewonnen: " + fight.won() + ")");
            return;
        }
        BossArenas.tick(level.getServer(), level, fights, fight);
        helper.runAfterDelay(1, () -> drive(helper, field, fights, fight, maxTicks - 1, then));
    }

    // =====================================================================================
    // Der letzte Gegner fällt
    // =====================================================================================

    /**
     * Wird (ausnahmsweise) am Hort selbst gekämpft und fällt der letzte Gegner, vergeht der Boss, und der Hort ruht wieder:
     * Der nächste Boss erwacht unversehrt. Steht noch ein zweiter Spieler am Hort, kämpft der Boss weiter.
     */
    private static void lastStand(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos at = new BlockPos(base.getX() + 31_000, 140, base.getZ() + 5_000);
        Field field = Field.of(level);
        field.hold(at, 40);
        helper.startSequence()
                .thenWaitUntil(() -> helper.assertTrue(field.loaded(), "Die Chunks des Felds laden nicht"))
                .thenExecute(() -> lastStandBody(helper, field, at))
                .thenSucceed();
    }

    private static void lastStandBody(GameTestHelper helper, Field field, BlockPos at) {
        ServerLevel level = helper.getLevel();
        BlockPos stonePos = lair(level, BossArenaKind.ASTERIA, at);
        ServerPlayer first = MageGameTests.survivalPlayer(helper);
        ServerPlayer second = MageGameTests.survivalPlayer(helper, "test-second-hunter", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        field.players().add(first);
        field.players().add(second);
        first.connection.markClientLoaded();    // sonst sind sie unverwundbar wie Spieler, die gerade erst die Welt laden
        second.connection.markClientLoaded();
        try {
            if (!(level.getBlockEntity(stonePos) instanceof StarstoneBlockEntity stone)) {
                throw new IllegalStateException("Der Sternstein hat keine Blockentität");
            }
            first.teleportTo(level, at.getX() + 4.5, at.getY() - 1, at.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            second.teleportTo(level, at.getX() - 4.5, at.getY() - 1, at.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
            AsteriaEntity asteria = stone.summon(level);
            helper.assertTrue(asteria != null, "Asteria erwacht nicht am Stein");
            // der erste fällt – der zweite steht noch: sie kämpft weiter
            first.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
            helper.assertTrue(asteria.isAlive() && !asteria.isRemoved() && stone.phase() == StarstoneBlockEntity.Phase.AWAKE,
                    "Sie verschwand, obwohl noch jemand am Stein stand");
            // der zweite fällt: niemand mehr – sie vergeht, der Stein ruht
            second.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
            helper.assertTrue(asteria.isRemoved(), "Sie bleibt, obwohl niemand mehr gegen sie steht");
            helper.assertTrue(stone.phase() == StarstoneBlockEntity.Phase.DORMANT, "Der Stein ruht nicht wieder");
            AsteriaEntity fresh = stone.summon(level);
            helper.assertTrue(fresh != null && fresh != asteria && fresh.getHealth() >= fresh.getMaxHealth(), "Die nächste Asteria ist nicht unversehrt");
        } finally {
            field.close();
        }
    }
}
