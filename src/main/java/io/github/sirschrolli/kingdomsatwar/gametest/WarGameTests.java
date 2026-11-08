package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastle;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePiece;
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
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum Krieg der Königreiche: Die Stammburg steht vollständig (jeder Posten frei und begehbar, Truhen und Banner
 * des Reichs), ihre Besatzung steht auf den Posten, Krieger greifen nur Eindringlinge an, und der Fall des Herrschers
 * wird vermerkt (wer ihn erschlägt, liegt mit dem Reich im Krieg).
 */
final class WarGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CASTLE =
            ModGameTests.TEST_FUNCTIONS.register("realm_castle", () -> WarGameTests::castle);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> INTRUDERS =
            ModGameTests.TEST_FUNCTIONS.register("realm_intruders", () -> WarGameTests::intruders);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LORD_FALLS =
            ModGameTests.TEST_FUNCTIONS.register("realm_lord_falls", () -> WarGameTests::lordFalls);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CONQUEST =
            ModGameTests.TEST_FUNCTIONS.register("realm_conquest", () -> WarGameTests::conquest);

    private WarGameTests() {}

    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CASTLE, INTRUDERS, LORD_FALLS, CONQUEST)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, test == CASTLE || test == CONQUEST ? 1200 : 200, 0, true, Rotation.NONE,
                            false, 1, 1, true, 0)));
        }
    }

    /**
     * Eine Burg (gedreht) weit draußen bauen: jeder Posten hat festen Boden und zwei Blöcke Luft, alle Posten sind besetzt,
     * es gibt Schatztruhe, Waffenkammer und Vorräte und die Banner tragen das Wappen des Reichs.
     */
    /** Eine Stammburg weit draußen bauen (Chunks laden, dann Chunk für Chunk errichten wie bei der Weltgenerierung). */
    private static RealmCastlePiece buildCastle(GameTestHelper helper, Realm realm, Rotation rotation, int dx, int dz) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos center = new BlockPos(base.getX() + dx, level.getSeaLevel() + 20, base.getZ() + dz);
        RealmCastlePiece piece = new RealmCastlePiece(realm, center, rotation, 4242L);
        BoundingBox box = piece.getBoundingBox();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                BoundingBox chunkBox = new BoundingBox(cx << 4, level.getMinY(), cz << 4, (cx << 4) + 15, level.getMaxY(), (cz << 4) + 15);
                piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), RandomSource.create(1), chunkBox,
                        new ChunkPos(cx, cz), center);
            }
        }
        BuildAudit.log(level, box, center, "Stammburg " + realm + " " + rotation);
        return piece;
    }

    private static void castle(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RealmCastlePiece piece = buildCastle(helper, Realm.DORNFELS, Rotation.CLOCKWISE_90, 3000, 3000);
        BoundingBox box = piece.getBoundingBox();
        List<String> problems = new ArrayList<>();
        for (RealmCastle.Post post : RealmCastle.posts()) {
            BlockPos feet = piece.world(new BlockPos(post.x(), post.y(), post.z()));
            BlockState below = level.getBlockState(feet.below());
            if (level.getBlockState(feet).getCollisionShape(level, feet).max(Direction.Axis.Y) > 0.2
                    || !level.getBlockState(feet.above()).getCollisionShape(level, feet.above()).isEmpty()) {
                problems.add(post.role() + " bei " + post.x() + "/" + post.y() + "/" + post.z() + " steckt in " + level.getBlockState(feet));
            } else if (below.getCollisionShape(level, feet.below()).isEmpty() && !below.is(net.minecraft.world.level.block.Blocks.LADDER)) {
                problems.add(post.role() + " bei " + post.x() + "/" + post.y() + "/" + post.z() + " schwebt über " + below);
            }
        }
        AABB area = AABB.of(box);
        List<RealmWarriorEntity> warriors = level.getEntitiesOfClass(RealmWarriorEntity.class, area);
        if (warriors.size() != RealmCastle.posts().size()) {
            problems.add(warriors.size() + " Krieger statt " + RealmCastle.posts().size());
        }
        if (warriors.stream().noneMatch(w -> w.role() == WarriorRole.LORD)) {
            problems.add("kein Herrscher");
        }
        if (warriors.stream().anyMatch(w -> w.realm() != Realm.DORNFELS)) {
            problems.add("Krieger eines fremden Reichs");
        }
        int chests = 0;
        int banners = 0;
        int painted = 0;
        for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            var entity = level.getBlockEntity(pos);
            if (entity instanceof ChestBlockEntity) {
                chests++;
            } else if (entity instanceof BannerBlockEntity banner) {
                banners++;
                if (!banner.getPatterns().layers().isEmpty()) {
                    painted++;
                }
            }
        }
        // Schatztruhe, Waffenkammer, Küche und Vorratshaus
        if (chests != 4) {
            problems.add(chests + " Truhen");
        }
        if (banners < 20 || painted != banners) {
            problems.add(banners + " Banner, davon " + painted + " mit Wappen");
        }
        // Thron und Reichsbanner hinter ihm (eine Wand dahinter)
        BlockPos standard = piece.world(RealmCastle.STANDARD.above());
        if (!(level.getBlockEntity(standard) instanceof BannerBlockEntity)) {
            problems.add("kein Reichsbanner bei " + standard);
        }
        for (RealmWarriorEntity warrior : warriors) {
            warrior.discard();
        }
        helper.assertTrue(problems.isEmpty(), "Stammburg: " + problems);
        helper.succeed();
    }

    /** Krieger greifen an, wer in die Burg eindringt – wer draußen bleibt, wird in Ruhe gelassen. */
    private static void intruders(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos home = helper.absolutePos(new BlockPos(4, 1, 4));
        RealmWarriorEntity guard = ModEntities.REALM_WARRIORS.get(WarriorRole.FOOTMAN).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(guard != null, "kein Fußknecht");
        guard.setRealm(Realm.SILBERAU);
        guard.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.snapTo(home.getX() + 3.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        // Burg hier: der Spieler ist drinnen
        guard.setPost(home, home);
        boolean inside = guard.isIntruder(player);
        // Burg weit weg: der Spieler ist draußen
        guard.setPost(home, home.relative(Direction.EAST, 200));
        boolean outside = guard.isIntruder(player);
        RealmWarriorEntity other = ModEntities.REALM_WARRIORS.get(WarriorRole.ARCHER).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        other.setRealm(Realm.SILBERAU);
        boolean allied = guard.isAlliedTo(other);
        other.setRealm(Realm.RABENSTEIN);
        boolean foreign = guard.isAlliedTo(other);
        player.discard();
        guard.discard();
        other.discard();
        helper.assertTrue(inside, "Eindringling in der Burg wird nicht angegriffen");
        helper.assertFalse(outside, "Spieler außerhalb der Burg wird angegriffen");
        helper.assertTrue(allied && !foreign, "Verbündete: gleiches Reich " + allied + ", fremdes Reich " + foreign);
        helper.succeed();
    }

    /** Fällt der Herrscher, merkt sich die Welt das – und wer ihn erschlug, liegt mit dem Reich im Krieg. */
    private static void lordFalls(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        RealmState state = RealmState.of(level);
        state.reset(Realm.RABENSTEIN);
        state.save(level);
        BlockPos home = helper.absolutePos(new BlockPos(4, 1, 4));
        RealmWarriorEntity lord = ModEntities.REALM_WARRIORS.get(WarriorRole.LORD).get().create(level, EntitySpawnReason.MOB_SUMMONED);
        helper.assertTrue(lord != null, "kein Herrscher");
        lord.setRealm(Realm.RABENSTEIN);
        lord.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, 0.0f, 0.0f);
        level.addFreshEntity(lord);
        Player player = helper.makeMockPlayer(GameType.SURVIVAL);
        lord.hurtServer(level, level.damageSources().playerAttack(player), 10000.0f);
        boolean dead = lord.isDeadOrDying();
        boolean fallen = RealmState.of(level).isLordFallen(Realm.RABENSTEIN);
        boolean war = RealmState.of(level).atWar(Realm.RABENSTEIN, player.getUUID());
        player.discard();
        lord.discard();
        state = RealmState.of(level);
        state.reset(Realm.RABENSTEIN);
        state.save(level);
        helper.assertTrue(dead, "Herrscher lebt noch");
        helper.assertTrue(fallen, "Fall des Herrschers nicht vermerkt");
        helper.assertTrue(war, "kein Krieg mit dem Spieler, der den Herrscher erschlug");
        helper.succeed();
    }

    /**
     * Nachschub, Eroberung und Tribut: Gefallene Krieger rücken nach (aber nur so viele, wie gefallen sind); solange der
     * Herrscher lebt oder Wachen am Reichsbanner stehen, lässt sich die Burg nicht erobern (das Banner bleibt hängen, die
     * Wachen leuchten auf); ein spurlos verschwundener Herrscher gilt als gefallen. Gegriffen wird das Reichsbanner auch über
     * sein herabhängendes Tuch und die Wand dahinter; danach tragen alle Banner das Wappen des Spielers (eine abgerissene
     * Bahn hängt wieder), er hat das alte Reichsbanner als Trophäe, die Krieger sind abgezogen, und nach drei Tagen liegt
     * dreifacher Tribut in der Schatztruhe.
     */
    private static void conquest(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Realm realm = Realm.EISENMARK;
        RealmState state = RealmState.of(level);
        state.reset(realm);
        state.save(level);
        RealmCastlePiece piece = buildCastle(helper, realm, Rotation.COUNTERCLOCKWISE_90, -3200, 3200);
        AABB area = AABB.of(piece.getBoundingBox());
        List<String> problems = new ArrayList<>();
        // drei Krieger fallen (ohne den Herrscher), eine Weile später rücken genau drei nach
        List<RealmWarriorEntity> garrison = level.getEntitiesOfClass(RealmWarriorEntity.class, area, w -> w.role() != WarriorRole.LORD);
        for (int i = 0; i < 3 && i < garrison.size(); i++) {
            garrison.get(i).discard();
            state.warriorFallen(realm);
        }
        state.setLastReinforcement(realm, level.getGameTime() - 10 * io.github.sirschrolli.kingdomsatwar.war.RealmWar.REINFORCEMENT);
        int added = io.github.sirschrolli.kingdomsatwar.war.RealmWar.reinforce(level, piece, pos -> true);
        int now = level.getEntitiesOfClass(RealmWarriorEntity.class, area, RealmWarriorEntity::isAlive).size();
        if (added != 3 || state.fallen(realm) != 0 || now != RealmCastle.posts().size()) {
            problems.add("Nachschub: " + added + " nachgerückt, " + state.fallen(realm) + " fehlen noch, " + now + " Krieger");
        }
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        try {
            player.setGameMode(GameType.SURVIVAL);
            BlockPos standard = piece.world(RealmCastle.STANDARD);
            player.snapTo(standard.getX() + 0.5, standard.getY() - 3, standard.getZ() + 3.5, 0.0f, 0.0f);
            // wer auf das Tuch zielt, trifft den Block darunter oder die Wand dahinter – auch das greift nach dem Banner
            boolean cloth = io.github.sirschrolli.kingdomsatwar.war.RealmWar.claims(level, piece, piece.world(RealmCastle.STANDARD), false);
            boolean wall = io.github.sirschrolli.kingdomsatwar.war.RealmWar.claims(level, piece, piece.world(RealmCastle.STANDARD.offset(1, 1, -1)), false);
            boolean throne = io.github.sirschrolli.kingdomsatwar.war.RealmWar.claims(level, piece, piece.world(RealmCastle.THRONE), false);
            boolean tear = io.github.sirschrolli.kingdomsatwar.war.RealmWar.claims(level, piece, piece.world(RealmCastle.STANDARD.above()), true);
            if (!cloth || !wall || throne || !tear) {
                problems.add("Griff nach dem Reichsbanner: Tuch " + cloth + ", Wand " + wall + ", Thron " + throne + ", Abreißen " + tear);
            }
            if (io.github.sirschrolli.kingdomsatwar.war.RealmWar.obstacle(level, player.getUUID(), piece) == null) {
                problems.add("Eroberung möglich, obwohl der Herrscher lebt");
            }
            // der Herrscher verschwindet, ohne zu fallen (etwa auf friedlicher Schwierigkeit): Dann gilt er als gefallen
            for (RealmWarriorEntity lord : level.getEntitiesOfClass(RealmWarriorEntity.class, area, w -> w.role() == WarriorRole.LORD)) {
                lord.discard();
            }
            if (io.github.sirschrolli.kingdomsatwar.war.RealmWar.obstacle(level, player.getUUID(), piece) == null) {
                problems.add("Eroberung möglich, obwohl Wachen am Reichsbanner stehen");
            }
            if (!RealmState.of(level).isLordFallen(realm)) {
                problems.add("verschwundener Herrscher gilt nicht als gefallen");
            }
            // wer das bewachte Banner abreißen will, bekommt es nicht – die Wachen leuchten auf
            boolean caught = io.github.sirschrolli.kingdomsatwar.war.RealmWar.seize(level, player, piece, piece.world(RealmCastle.STANDARD.above()), true);
            List<RealmWarriorEntity> guards = io.github.sirschrolli.kingdomsatwar.war.RealmWar.defenders(level, piece);
            boolean glowing = !guards.isEmpty() && guards.stream().allMatch(w -> w.hasEffect(net.minecraft.world.effect.MobEffects.GLOWING));
            if (!caught || !glowing || RealmState.of(level).isConquered(realm)) {
                problems.add("Griff nach dem bewachten Banner: abgefangen " + caught + ", " + guards.size() + " Wachen leuchten " + glowing);
            }
            for (RealmWarriorEntity warrior : level.getEntitiesOfClass(RealmWarriorEntity.class, area)) {
                warrior.discard();
            }
            // eine Bahn ist abgerissen – der Eroberer hängt sie neu auf
            BlockPos torn = piece.world(RealmCastle.STANDARD.offset(1, 1, 0));
            level.setBlock(torn, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            boolean standardFound = io.github.sirschrolli.kingdomsatwar.war.RealmWar.isStandard(piece, piece.world(RealmCastle.STANDARD.above()));
            boolean seized = io.github.sirschrolli.kingdomsatwar.war.RealmWar.seize(level, player, piece, piece.world(RealmCastle.STANDARD), false);
            boolean conquered = player.getUUID().equals(RealmState.of(level).conqueror(realm));
            if (!standardFound || !seized || !conquered) {
                problems.add("Eroberung: Reichsbanner erkannt " + standardFound + ", gegriffen " + seized + ", erobert " + conquered);
            }
            if (!(level.getBlockState(torn).getBlock() instanceof net.minecraft.world.level.block.WallBannerBlock)) {
                problems.add("abgerissene Bahn des Reichsbanners hängt nicht wieder: " + level.getBlockState(torn));
            }
            if (!player.getInventory().hasAnyMatching(stack -> stack.is(net.minecraft.tags.ItemTags.BANNERS))) {
                problems.add("kein Reichsbanner als Trophäe im Inventar");
            }
            if (io.github.sirschrolli.kingdomsatwar.war.RealmWar.claims(level, piece, piece.world(RealmCastle.STANDARD), false)) {
                problems.add("nach der Eroberung greift ein Klick auf die Wand noch nach dem Banner");
            }
            // alle Banner im Wappen des Spielers
            int banners = 0;
            int ours = 0;
            BoundingBox box = piece.getBoundingBox();
            for (BlockPos pos : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
                    banners++;
                    if (banner.getBaseColor() == net.minecraft.world.item.DyeColor.BLUE && banner.getPatterns().layers().size() == 2) {
                        ours++;
                    }
                }
            }
            if (banners < 20 || ours != banners) {
                problems.add(banners + " Banner, davon " + ours + " im Wappen des Spielers");
            }
            // Tribut: drei Tage
            state = RealmState.of(level);
            state.setLastTribute(realm, level.getGameTime() - 3 * 24000L - 10);
            int days = io.github.sirschrolli.kingdomsatwar.war.RealmWar.payTribute(level, piece);
            int coins = 0;
            if (level.getBlockEntity(piece.world(RealmCastle.TREASURY)) instanceof ChestBlockEntity chest) {
                for (int i = 0; i < chest.getContainerSize(); i++) {
                    if (chest.getItem(i).is(io.github.sirschrolli.kingdomsatwar.registry.ModItems.BRONZE_COIN.get())) {
                        coins += chest.getItem(i).getCount();
                    }
                }
            } else {
                problems.add("keine Schatztruhe bei " + piece.world(RealmCastle.TREASURY));
            }
            if (days != 3 || coins < 24) {
                problems.add("Tribut: " + days + " Tage, " + coins + " Bronzemünzen");
            }
            if (io.github.sirschrolli.kingdomsatwar.war.RealmWar.payTribute(level, piece) != 0) {
                problems.add("Tribut doppelt gezahlt");
            }
        } finally {
            level.getServer().getPlayerList().remove(player);
            for (RealmWarriorEntity warrior : level.getEntitiesOfClass(RealmWarriorEntity.class, area)) {
                warrior.discard();
            }
            state = RealmState.of(level);
            state.reset(realm);
            state.save(level);
        }
        helper.assertTrue(problems.isEmpty(), "Eroberung: " + problems);
        helper.succeed();
    }
}
