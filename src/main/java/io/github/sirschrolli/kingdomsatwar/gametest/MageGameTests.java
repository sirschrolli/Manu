package io.github.sirschrolli.kingdomsatwar.gametest;

import com.mojang.authlib.GameProfile;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageImageEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MagicMissile;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerBuilder;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPlacement;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.network.protocol.game.ClientboundLevelParticlesPacket;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.component.WrittenBookContent;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.LecternBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/** Tests der Erzmagier und ihrer Türme: Bau aller sechs Türme (begehbar, mit Truhen, Buch und Magier) und der Kampf. */
final class MageGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MAGE_TOWERS =
            ModGameTests.TEST_FUNCTIONS.register("mage_towers", () -> MageGameTests::mageTowers);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> MAGE_BOSS =
            ModGameTests.TEST_FUNCTIONS.register("mage_boss", () -> MageGameTests::mageBoss);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CLEAR_SIGHT =
            ModGameTests.TEST_FUNCTIONS.register("boss_clear_sight", () -> MageGameTests::clearSight);

    private MageGameTests() {}

    static void init() {
    }

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        event.registerTest(MAGE_TOWERS.getId(), new FunctionGameTestInstance(MAGE_TOWERS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 600, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(MAGE_BOSS.getId(), new FunctionGameTestInstance(MAGE_BOSS.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 1800, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        event.registerTest(CLEAR_SIGHT.getId(), new FunctionGameTestInstance(CLEAR_SIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 100, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Türme
    // =====================================================================================

    /**
     * Alle sechs Türme: registriert und in der gemeinsamen Verteilung; gebaut (Chunk für Chunk wie in der Welt, jeder in
     * einer anderen Drehung) mit Tür, sechs Stufen je Lauf mit Kopffreiheit, Treppenlöchern und – geprüft mit einer Suche
     * über die Böden – begehbaren Geschossen von der Tür bis zum Sanktum, in denen jede Truhe, das Lesepult, der
     * Magietisch und der Zaubertisch erreichbar sind. Im Sanktum meditiert genau ein Erzmagier des Elements.
     */
    private static void mageTowers(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var registries = level.registryAccess();
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.MAGE_TOWERS);
        helper.assertTrue(set.isPresent(), "Gruppe der Magiertürme fehlt");
        helper.assertTrue(set.get().value().placement() instanceof MageTowerPlacement, "Magiertürme ohne eigene Verteilung");
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        List<MageTowerPiece> pieces = new java.util.ArrayList<>();
        for (DragonType element : DragonType.values()) {
            helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE).get(ModStructures.mageTower(element)).isPresent(),
                    "Turm fehlt: " + element.id());
            helper.assertTrue(set.get().value().structures().stream().anyMatch(e -> e.structure().is(ModStructures.mageTower(element))),
                    "Turm nicht in der Verteilung: " + element.id());
            int x = base.getX() + 4000 + element.ordinal() * 160;
            int z = base.getZ() + 400;
            MageTowerPiece piece = build(level, element, new BlockPos(x, groundY(level, x, z), z),
                    Rotation.values()[element.ordinal() % 4]);
            forceChunks(level, piece, true);
            pieces.add(piece);
        }
        // geprüft wird, wenn die Chunks samt ihrer Wesen bereitstehen (wie nach dem Laden in der echten Welt)
        helper.startSequence()
                .thenIdle(10)
                .thenExecute(() -> {
                    for (MageTowerPiece piece : pieces) {
                        checkTower(helper, level, piece);
                    }
                    pieces.forEach(piece -> forceChunks(level, piece, false));
                })
                .thenSucceed();
    }

    private static void checkTower(GameTestHelper helper, ServerLevel level, MageTowerPiece piece) {
        String name = piece.element().id() + " (" + piece.rotation() + ")";
        BlockState door = level.getBlockState(piece.world(0, 2, -6));
        helper.assertTrue(door.getBlock() instanceof DoorBlock && door.getValue(DoorBlock.HALF) == DoubleBlockHalf.LOWER, name + ": keine Tür");
        int[] floors = MageTowerBuilder.FLOOR;
        // Treppenläufe
        for (MageTowerBuilder.Flight f : MageTowerBuilder.FLIGHTS) {
            helper.assertTrue(passable(level, piece.world(f.x(0), f.floor() + 1, f.z(0))) && passable(level, piece.world(f.x(0), f.floor() + 2,
                    f.z(0))), name + ": Zugang zur Treppe versperrt bei " + f.x(0) + "," + f.z(0));
            for (int i = 1; i <= 6; i++) {
                BlockPos step = piece.world(f.x(i), f.floor() + i, f.z(i));
                BlockState state = level.getBlockState(step);
                helper.assertTrue(state.getBlock() instanceof StairBlock && state.getValue(StairBlock.HALF) == Half.BOTTOM
                        && state.getValue(StairBlock.FACING) == piece.rotation().rotate(f.dir()), name + ": Stufe " + i + " falsch: " + state);
                helper.assertTrue(passable(level, step.above()) && passable(level, step.above(2)), name + ": keine Kopffreiheit über Stufe " + i
                        + " (Geschoss " + f.floor() + ")");
            }
            helper.assertTrue(level.getBlockState(piece.world(f.x(4), f.floor() + 6, f.z(4))).isAir()
                    && level.getBlockState(piece.world(f.x(5), f.floor() + 6, f.z(5))).isAir(), name + ": Treppenloch zu");
        }
        // Begehbarkeit der Geschosse: vom Eingang (bzw. vom Ende der Treppe) bis zur nächsten Treppe und zu allem Wichtigen
        int[][] starts = {{0, -5}, {3, 3}, {3, -3}, {-3, -3}, {-3, 3}};
        int[] levels = {floors[0], floors[1], floors[2], floors[3], MageTowerBuilder.SANCTUM};
        for (int k = 0; k < 5; k++) {
            double radius = k == 4 ? MageTowerBuilder.R_HALL : MageTowerBuilder.R_IN;
            Set<Long> reach = reachable(level, piece, levels[k], radius, starts[k][0], starts[k][1]);
            helper.assertTrue(!reach.isEmpty(), name + ": Start im Geschoss " + k + " nicht begehbar");
            if (k < 4) {
                MageTowerBuilder.Flight f = MageTowerBuilder.FLIGHTS[k];
                helper.assertTrue(reach.contains(key(f.x(0), f.z(0))), name + ": Treppe im Geschoss " + k + " nicht erreichbar");
            } else {
                helper.assertTrue(near(reach, 0, 0), name + ": Mitte des Sanktums nicht erreichbar");
            }
        }
        // Truhen mit Beute – und erreichbar
        List<int[]> chests = MageTowerBuilder.chestSpots();
        List<ResourceKey<LootTable>> loot = List.of(ModLootTables.MAGE_TOWER_HALL, ModLootTables.MAGE_TOWER_LIBRARY,
                ModLootTables.MAGE_TOWER_LABORATORY, ModLootTables.MAGE_TOWER_QUARTERS, ModLootTables.MAGE_TOWER_SANCTUM);
        for (int k = 0; k < chests.size(); k++) {
            int[] c = chests.get(k);
            BlockPos pos = piece.world(c[0], c[1], c[2]);
            helper.assertTrue(level.getBlockEntity(pos) instanceof ChestBlockEntity chest && loot.get(k).equals(chest.getLootTable()),
                    name + ": keine Truhe mit Beute " + loot.get(k).identifier() + " bei " + c[0] + "," + c[1] + "," + c[2]);
            Set<Long> reach = reachable(level, piece, levels[k], k == 4 ? MageTowerBuilder.R_HALL : MageTowerBuilder.R_IN, starts[k][0], starts[k][1]);
            helper.assertTrue(near(reach, c[0], c[2]), name + ": Truhe " + k + " nicht erreichbar");
        }
        // Bibliothek: Lesepult mit den Aufzeichnungen des Magiers
        BlockPos lectern = piece.world(-4, floors[1] + 1, 0);
        helper.assertTrue(level.getBlockEntity(lectern) instanceof LecternBlockEntity be && be.hasBook()
                && be.getBook().get(DataComponents.WRITTEN_BOOK_CONTENT) instanceof WrittenBookContent book && book.pages().size() == 4
                && be.getBook().has(DataComponents.CUSTOM_NAME), name + ": Lesepult ohne Buch");
        helper.assertTrue(near(reachable(level, piece, floors[1], MageTowerBuilder.R_IN, 3, 3), -4, 0), name + ": Lesepult nicht erreichbar");
        // Laboratorium mit Magietisch, Kammer mit Zaubertisch
        helper.assertTrue(level.getBlockState(piece.world(0, floors[2] + 1, 0)).is(ModBlocks.MAGIC_TABLE.get()), name + ": kein Magietisch");
        helper.assertTrue(near(reachable(level, piece, floors[2], MageTowerBuilder.R_IN, 3, -3), 0, 0), name + ": Magietisch nicht erreichbar");
        helper.assertTrue(level.getBlockState(piece.world(0, floors[3] + 1, 2)).is(Blocks.ENCHANTING_TABLE), name + ": kein Zaubertisch");
        helper.assertTrue(near(reachable(level, piece, floors[3], MageTowerBuilder.R_IN, -3, -3), 0, 2), name + ": Zaubertisch nicht erreichbar");
        // Dach mit Spitze
        helper.assertTrue(!level.getBlockState(piece.world(0, MageTowerBuilder.PEAK, 0)).isAir()
                && !level.getBlockState(piece.world(0, MageTowerBuilder.PEAK + 1, 0)).isAir(), name + ": Dachspitze fehlt");
        // der Erzmagier meditiert in der Mitte des Sanktums
        List<MageBossEntity> mages = level.getEntitiesOfClass(MageBossEntity.class, new AABB(piece.home()).inflate(3.0));
        helper.assertTrue(mages.size() == 1, name + ": " + mages.size() + " Erzmagier im Sanktum");
        MageBossEntity mage = mages.getFirst();
        helper.assertTrue(mage.element() == piece.element() && mage.dormant() && piece.home().equals(mage.home()),
                name + ": Magier falsch (Element " + mage.element() + ", ruhend " + mage.dormant() + ", Platz " + mage.home() + ")");
    }

    // =====================================================================================
    // Kampf
    // =====================================================================================

    /**
     * Der Erzmagier im Turm: Er erwacht nicht, wenn jemand im Geschoss darunter steht, wohl aber, sobald ein Spieler das
     * Sanktum betritt. Gegen sein Element ist er gefeit; sein Runenschild fängt vier Schläge ab und zerbricht; seine
     * Geschosse lassen sich zurückschlagen; ein Spiegelbild zerplatzt beim ersten Treffer. Unter zwei Dritteln beginnt die
     * zweite Phase (beim Wechsel unverwundbar). Flieht der Spieler ins Geschoss darunter, ist er kein Ziel mehr – der Magier
     * kehrt geheilt an seinen Platz zurück. Erneut geweckt und besiegt, lässt er am Ende der Todesanimation seine
     * Zaubertafel fallen.
     */
    private static void mageBoss(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        int x = base.getX() + 4000;
        int z = base.getZ() - 500;
        MageTowerPiece piece = build(level, DragonType.FIRE, new BlockPos(x, groundY(level, x, z), z), Rotation.CLOCKWISE_90);
        forceChunks(level, piece, true);
        List<MageBossEntity> mages = level.getEntitiesOfClass(MageBossEntity.class, new AABB(piece.home()).inflate(3.0));
        helper.assertTrue(mages.size() == 1, "kein Erzmagier im Turm");
        MageBossEntity mage = mages.getFirst();
        ServerPlayer player = survivalPlayer(helper);
        player.getAbilities().invulnerable = true;
        int sanctum = MageTowerBuilder.SANCTUM + 1;
        BlockPos arena = piece.world(-3, sanctum, 3);
        float[] health = new float[1];
        teleport(player, piece.world(2, MageTowerBuilder.FLOOR[3] + 1, 2));
        // Laufen viele Tests gleichzeitig, die alle weit entfernte Chunks erzeugen, kommt der Testserver mit dem
        // Freigeben ferner Chunks für Wesen bisweilen nicht nach. Dann treibt der Test den Magier selbst an – genau so,
        // wie es der Server täte (im Spiel und in der Vorführung läuft das ohnehin von selbst).
        helper.onEachTick(() -> {
            if (!mage.isRemoved() && !level.isPositionEntityTicking(mage.blockPosition())) {
                mage.setOldPosAndRot();
                mage.tickCount++;
                mage.tick();
            }
        });
        helper.startSequence()
                .thenIdle(40)
                .thenExecute(() -> helper.assertTrue(mage.state() == MageBossEntity.State.DORMANT, "Magier erwacht von unten"))
                .thenExecute(() -> teleport(player, arena))
                .thenWaitUntil(() -> helper.assertTrue(mage.state() != MageBossEntity.State.DORMANT, "Magier erwacht nicht im Sanktum (entfernt "
                        + mage.isRemoved() + ", Ticks " + mage.tickCount + ", Abstand " + String.format("%.1f", mage.distanceTo(player))
                        + ", Höhe " + String.format("%.1f", player.getY() - mage.getY()) + ", Sicht " + mage.hasLineOfSight(player)
                        + ", Feind " + mage.isBossEnemy(player) + ", Spieler " + level.players().contains(player)
                        + ", tickend " + level.isPositionEntityTicking(mage.blockPosition())
                        + ", erzwungen " + level.getForceLoadedChunks().contains(mage.chunkPosition().pack())
                        + ", Chunk " + chunkStatus(level, mage) + ")"))
                .thenWaitUntil(() -> helper.assertTrue(mage.state() == MageBossEntity.State.FIGHT, "Kampf beginnt nicht"))
                .thenExecute(() -> {
                    health[0] = mage.getHealth();
                    mage.hurtServer(level, level.damageSources().inFire(), 20.0f);
                    mage.hurtServer(level, level.damageSources().lava(), 20.0f);
                    helper.assertTrue(mage.getHealth() == health[0], "Feuermagier nimmt Feuerschaden");
                    mage.setInvulnerableTime(0);
                    mage.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
                    helper.assertTrue(mage.getHealth() < health[0], "Treffer des Spielers wirkt nicht");
                    // Runenschild: vier Schläge, kein Schaden, dann zerbricht er
                    mage.devShield();
                    float shielded = mage.getHealth();
                    for (int i = 0; i < 4; i++) {
                        mage.setInvulnerableTime(0);
                        mage.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
                    }
                    helper.assertTrue(mage.getHealth() == shielded && !mage.shielded(), "Schild hält nicht oder bricht nicht");
                    // Geschoss zurückschlagen
                    Vec3 from = mage.staffTip();
                    MagicMissile missile = MagicMissile.shoot(level, mage, player, from, player.position().subtract(from), 0.55, 4.5f);
                    helper.assertTrue(missile.hurtServer(level, level.damageSources().playerAttack(player), 1.0f) && missile.getOwner() == player,
                            "Geschoss lässt sich nicht zurückschlagen");
                    missile.discard();
                    // Spiegelbild
                    MageImageEntity image = MageImageEntity.spawn(level, mage, mage.position().add(2.0, 0.0, 0.0));
                    helper.assertTrue(image != null, "kein Spiegelbild");
                    image.hurtServer(level, level.damageSources().playerAttack(player), 1.0f);
                    helper.assertTrue(image.isRemoved(), "Spiegelbild übersteht einen Treffer");
                    mage.setHealth(mage.getMaxHealth() * 0.6f);
                })
                .thenWaitUntil(() -> helper.assertTrue(mage.phase() == 2, "keine zweite Phase"))
                .thenExecute(() -> {
                    float hp = mage.getHealth();
                    mage.setInvulnerableTime(0);
                    mage.hurtServer(level, level.damageSources().playerAttack(player), 10.0f);
                    helper.assertTrue(mage.getHealth() == hp, "im Phasenwechsel verwundbar");
                })
                .thenIdle(MageBossEntity.TRANSITION_TICKS + 5)
                .thenExecute(() -> {
                    teleport(player, piece.world(2, MageTowerBuilder.FLOOR[2] + 1, 2));
                    helper.assertTrue(!mage.inArena(player) && mage.arenaEnemies().isEmpty(), "Spieler im Laboratorium gilt als im Saal");
                })
                .thenWaitUntil(() -> helper.assertTrue(mage.state() == MageBossEntity.State.DORMANT && mage.getHealth() == mage.getMaxHealth(),
                        "Magier kehrt nicht geheilt an seinen Platz zurück"))
                .thenExecute(() -> {
                    helper.assertTrue(mage.position().distanceTo(Vec3.atBottomCenterOf(piece.home())) < 2.0, "Magier nicht an seinem Platz");
                    teleport(player, arena);
                    mage.beginAwakening(level);
                })
                .thenWaitUntil(() -> helper.assertTrue(mage.state() == MageBossEntity.State.FIGHT, "Kampf beginnt nicht erneut"))
                .thenExecute(() -> mage.setHealth(1.0f))
                .thenIdle(MageBossEntity.TRANSITION_TICKS + 5)
                .thenExecute(() -> {
                    mage.setInvulnerableTime(0);
                    mage.hurtServer(level, level.damageSources().playerAttack(player), 50.0f);
                    helper.assertTrue(mage.state() == MageBossEntity.State.DYING, "Magier stirbt nicht");
                })
                .thenWaitUntil(() -> helper.assertTrue(mage.isRemoved(), "Todesanimation endet nicht"))
                .thenExecute(() -> {
                    List<ItemEntity> drops = level.getEntitiesOfClass(ItemEntity.class, new AABB(piece.home()).inflate(6.0));
                    helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(ModItems.SPELL_TABLETS.get(Spell.FIRE).get())),
                            "keine Feuer-Zaubertafel als Beute");
                    level.getServer().getPlayerList().remove(player);
                    forceChunks(level, piece, false);
                })
                .thenSucceed();
    }

    // =====================================================================================
    // Freie Sicht im Kampf
    // =====================================================================================

    /**
     * Einschläge (Geschosse der Magier und Drachen, Zauber am Gegner) werfen ihre Teilchen nicht direkt vor die Augen
     * dessen, der mitten in der Wolke steht – sonst sähe der Getroffene nur noch bildschirmfüllende bunte Klötze. Er
     * bekommt den Rest der Wolke (kein Teilchen näher als {@link BossFx#CLEAR_SIGHT} vor den Augen), ein Zuschauer in
     * einigem Abstand die ganze Wolke wie gewohnt in einem Paket. Auch die Spur eines Geschosses endet vor dem Gesicht.
     */
    private static void clearSight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        EmbeddedChannel[] channels = new EmbeddedChannel[2];
        ServerPlayer victim = survivalPlayer(helper, "test-victim", channels, 0);
        ServerPlayer watcher = survivalPlayer(helper, "test-watcher", channels, 1);
        BlockPos at = helper.absolutePos(new BlockPos(1, 1, 1));
        teleport(victim, at);
        teleport(watcher, at.offset(9, 0, 0));
        drain(channels[0]);
        drain(channels[1]);
        Vec3 eye = victim.getEyePosition();
        Vec3 hit = eye.add(0.3, -0.7, 0.0);               // ein Geschoss trifft die Brust
        BossFx.impact(level, ParticleTypes.FLAME, hit, 60, 0.8, 0.8, 0.8, 0.05);
        List<ClientboundLevelParticlesPacket> near = particles(channels[0]);
        List<ClientboundLevelParticlesPacket> far = particles(channels[1]);
        helper.assertTrue(!near.isEmpty() && near.size() < 60, "Der Getroffene sieht " + near.size() + " von 60 Teilchen");
        for (ClientboundLevelParticlesPacket packet : near) {
            Vec3 pos = new Vec3(packet.x(), packet.y(), packet.z());
            double d = eye.distanceTo(pos);
            helper.assertTrue(packet.count() == 0 && d >= BossFx.CLEAR_SIGHT - 1.0e-6,
                    "Teilchen " + String.format(java.util.Locale.ROOT, "%.2f", d) + " Blöcke vor den Augen des Getroffenen");
            Vec3 velocity = new Vec3(packet.xDist(), packet.yDist(), packet.zDist());
            helper.assertTrue(velocity.dot(pos.subtract(eye)) >= -1.0e-6, "Ein Teilchen fliegt dem Getroffenen ins Gesicht");
        }
        helper.assertTrue(far.size() == 1 && far.getFirst().count() == 60 && far.getFirst().xDist() == 0.8f,
                "Der Zuschauer sieht die Wolke nicht wie gewohnt (" + far.size() + " Pakete)");
        helper.assertTrue(BossFx.nearEyes(level, eye.add(0.4, -0.3, 0.2)) && !BossFx.nearEyes(level, eye.add(0.0, 3.0, 0.0)),
                "Die Spur eines Geschosses endet nicht vor dem Gesicht");
        level.getServer().getPlayerList().remove(victim);
        level.getServer().getPlayerList().remove(watcher);
        helper.succeed();
    }

    private static void drain(EmbeddedChannel channel) {
        while (channel.readOutbound() != null) {
            // alles, was beim Anmelden und Versetzen geschickt wurde, verwerfen
        }
    }

    private static List<ClientboundLevelParticlesPacket> particles(EmbeddedChannel channel) {
        List<ClientboundLevelParticlesPacket> found = new java.util.ArrayList<>();
        for (Object message; (message = channel.readOutbound()) != null; ) {
            if (message instanceof ClientboundLevelParticlesPacket packet) {
                found.add(packet);
            }
        }
        return found;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Baut den Turm Chunk für Chunk wie in der echten Weltgenerierung. */
    static MageTowerPiece build(ServerLevel level, DragonType element, BlockPos origin, Rotation rotation) {
        MageTowerPiece piece = new MageTowerPiece(element, origin, rotation, 7_000L + element.ordinal() * 31L);
        BoundingBox box = piece.getBoundingBox();
        var generator = level.getChunkSource().getGenerator();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                BoundingBox chunkBox = new BoundingBox(cx << 4, level.getMinY(), cz << 4, (cx << 4) + 15, level.getMaxY(), (cz << 4) + 15);
                piece.postProcess(level, level.structureManager(), generator, RandomSource.create(1), chunkBox, new ChunkPos(cx, cz), origin);
            }
        }
        return piece;
    }

    /** Höhe der Terrasse wie bei der echten Platzwahl: eine Lage über dem Gelände. */
    static int groundY(ServerLevel level, int x, int z) {
        return level.getChunkSource().getGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level,
                level.getChunkSource().randomState());
    }

    private static void forceChunks(ServerLevel level, MageTowerPiece piece, boolean force) {
        BoundingBox box = piece.getBoundingBox();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                level.setChunkForced(cx, cz, force);
            }
        }
    }

    /** Ein Spieler im Überlebensmodus (die Testhilfe kennt nur Kreativspieler – die greift der Magier nicht an). */
    static ServerPlayer survivalPlayer(GameTestHelper helper) {
        return survivalPlayer(helper, "test-mage-hunter", new EmbeddedChannel[1], 0);
    }

    /** Wie oben; die Verbindung landet in {@code channels[index]} (dort lässt sich lesen, was der Server ihm schickt). */
    static ServerPlayer survivalPlayer(GameTestHelper helper, String name, EmbeddedChannel[] channels, int index) {
        ServerLevel level = helper.getLevel();
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.randomUUID(), name), false);
        ServerPlayer player = new ServerPlayer(level.getServer(), level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override
            public GameType gameMode() {
                return GameType.SURVIVAL;
            }
        };
        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        channels[index] = new EmbeddedChannel(connection);
        level.getServer().getPlayerList().placeNewPlayer(connection, player, cookie);
        return player;
    }

    private static String chunkStatus(ServerLevel level, MageBossEntity mage) {
        long key = mage.chunkPosition().pack();
        var holder = level.getChunkSource().chunkMap.getVisibleChunkIfPresent(key);
        var distances = level.getChunkSource().chunkMap.getDistanceManager();
        var tickets = level.getDataStorage().computeIfAbsent(net.minecraft.world.level.TicketStorage.TYPE);
        return (holder == null ? "keiner" : holder.getFullStatus() + "/" + holder.getTicketLevel()) + ", Simulation "
                + distances.getChunkLevel(key, true) + ", Laden " + distances.getChunkLevel(key, false) + ", Tickets "
                + tickets.getTicketDebugString(key, true) + " | " + tickets.getTicketDebugString(key, false);
    }

    /** Versetzt den Spieler und meldet ihn an der neuen Stelle an (wie es sonst seine Verbindung jeden Tick tut). */
    private static void teleport(ServerPlayer player, BlockPos pos) {
        player.teleportTo(player.level(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, Set.of(), 0.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    private static boolean passable(ServerLevel level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return shape.isEmpty() || shape.max(Direction.Axis.Y) <= 0.1875;     // Teppiche sind kein Hindernis
    }

    private static boolean standable(ServerLevel level, BlockPos pos) {
        VoxelShape shape = level.getBlockState(pos).getCollisionShape(level, pos);
        return !shape.isEmpty() && shape.max(Direction.Axis.Y) >= 0.5;
    }

    private static long key(int x, int z) {
        return BlockPos.asLong(x, 0, z);
    }

    /** Alle Felder eines Geschosses, die man von (sx, sz) aus zu Fuß erreicht (fester Boden, zwei Blöcke Luft). */
    private static Set<Long> reachable(ServerLevel level, MageTowerPiece piece, int floorY, double radius, int sx, int sz) {
        Set<Long> seen = new HashSet<>();
        if (!walkable(level, piece, floorY, sx, sz)) {
            return seen;
        }
        ArrayDeque<int[]> queue = new ArrayDeque<>();
        queue.add(new int[] {sx, sz});
        seen.add(key(sx, sz));
        while (!queue.isEmpty()) {
            int[] c = queue.poll();
            for (Direction dir : Direction.Plane.HORIZONTAL) {
                int nx = c[0] + dir.getStepX();
                int nz = c[1] + dir.getStepZ();
                if (MageTowerBuilder.dist(nx, nz) > radius || seen.contains(key(nx, nz)) || !walkable(level, piece, floorY, nx, nz)) {
                    continue;
                }
                seen.add(key(nx, nz));
                queue.add(new int[] {nx, nz});
            }
        }
        return seen;
    }

    private static boolean walkable(ServerLevel level, MageTowerPiece piece, int floorY, int x, int z) {
        return standable(level, piece.world(x, floorY, z)) && passable(level, piece.world(x, floorY + 1, z))
                && passable(level, piece.world(x, floorY + 2, z));
    }

    /** Ist ein Nachbarfeld von (x, z) erreichbar? */
    private static boolean near(Set<Long> reach, int x, int z) {
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (reach.contains(key(x + dir.getStepX(), z + dir.getStepZ()))) {
                return true;
            }
        }
        return false;
    }
}
