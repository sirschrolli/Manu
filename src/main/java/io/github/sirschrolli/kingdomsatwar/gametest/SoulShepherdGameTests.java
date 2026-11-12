package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlock;
import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulShepherdEntity;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulWispEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.shadow.ShepherdCrookItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowGear;
import io.github.sirschrolli.kingdomsatwar.registry.SoulShepherd;
import io.github.sirschrolli.kingdomsatwar.worldgen.shepherd.SoulPenPiece;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum Seelenhirten und zum Seelenpferch (0.98): wie ein Pferch gebaut ist und dass er in der Schattenwelt entsteht, die
 * Glocke und die Andacht des Hirten, sein Erwachen, alle Angriffe, sein Zorn, Tod, verstummte Glocke und Rückruf mit einem
 * Seelenkristall; dazu der Hirtenstab.
 */
final class SoulShepherdGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> PEN =
            ModGameTests.TEST_FUNCTIONS.register("soul_shepherd_pen", () -> SoulShepherdGameTests::pen);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD =
            ModGameTests.TEST_FUNCTIONS.register("soul_shepherd_world", () -> SoulShepherdGameTests::world);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("soul_shepherd_fight", () -> SoulShepherdGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CROOK =
            ModGameTests.TEST_FUNCTIONS.register("soul_shepherd_crook", () -> SoulShepherdGameTests::crook);

    private SoulShepherdGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(PEN, WORLD, CROOK)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 2000, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Ein eigenes Feld weit draußen
    // =====================================================================================

    private record Field(ServerLevel level, BlockPos origin, List<long[]> chunks) {
        void close() {
            AABB area = new AABB(this.origin).inflate(48, 30, 48);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
                e.discard();
            }
            BoundingBox box = SoulPenPiece.makeBox(this.origin);
            for (BlockPos p : BlockPos.betweenClosed(box.minX() - 1, box.minY() - 1, box.minZ() - 1, box.maxX() + 1, box.maxY() + 1, box.maxZ() + 1)) {
                if (!this.level.getBlockState(p).isAir()) {
                    this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int dx, int dz) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() + dx, 150, base.getZ() + dz);
        List<long[]> chunks = new ArrayList<>();
        int reach = 40;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        return new Field(level, origin, chunks);
    }

    /** Ein Ritter, der nichts tut (keine Ziele, keine Absichten) – aber anders als ohne KI lässt er sich stoßen und ziehen. */
    private static void idle(Mob mob) {
        mob.goalSelector.removeAllGoals(goal -> true);
        mob.targetSelector.removeAllGoals(goal -> true);
        mob.setPersistenceRequired();
    }

    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    // =====================================================================================
    // Der Pferch
    // =====================================================================================

    /**
     * Ein Pferch auf leerem Grund: aufgeschüttet, ein Zaun mit Pfosten, Laternen und Schädeln, vier offene Tore, in der Mitte
     * das Podest mit Dämmerquarz und der Glockenstuhl mit der Seelenglocke (leuchtet, unzerstörbar, kennt den Platz des
     * Hirten), vier Feuerschalen, Stege mit Stelzen, die Hütte mit Tür und einer Truhe.
     */
    private static void pen(GameTestHelper helper) {
        Field field = field(helper, 31_000, -27_000);
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        try {
            SoulPenPiece.build(level, SoulPenPiece.makeBox(o), o, 4242L, o.getY() - 20, RandomSource.create(7), null);
            BlockPos bell = SoulPenPiece.bell(o);
            BlockState b = level.getBlockState(bell);
            helper.assertTrue(b.is(SoulShepherd.SOUL_BELL.get()), "Keine Seelenglocke im Glockenstuhl: " + b);
            helper.assertTrue(b.getDestroySpeed(level, bell) < 0, "Die Seelenglocke lässt sich abbauen");
            helper.assertTrue(b.getLightEmission(level, bell) > 0, "Die Seelenglocke leuchtet nicht");
            helper.assertTrue(level.getBlockEntity(bell) instanceof SoulBellBlockEntity be && be.home().equals(SoulPenPiece.home(o)),
                    "Die Glocke kennt den Platz des Hirten nicht");
            helper.assertTrue(level.getBlockState(bell.above()).is(ShadowBlocks.SHADOW_LOG.get()), "Kein Balken über der Glocke");
            // unter der Glocke und vor ihr ist Platz für den Hirten (gut drei Blöcke hoch)
            for (int dy = 0; dy < 4; dy++) {
                helper.assertTrue(level.getBlockState(SoulPenPiece.home(o).above(dy)).isAir(), "Der Platz des Hirten ist verbaut (+" + dy + ")");
            }
            int fences = 0;
            int lanterns = 0;
            int skulls = 0;
            int chests = 0;
            int fires = 0;
            int quartz = 0;
            int quartzBlocks = 0;
            int doors = 0;
            int logs = 0;
            BoundingBox box = SoulPenPiece.makeBox(o);
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                BlockState s = level.getBlockState(p);
                if (s.is(ShadowBlocks.SHADOW_FENCE.get())) {
                    fences++;
                } else if (s.is(Blocks.SOUL_LANTERN)) {
                    lanterns++;
                } else if (s.is(Blocks.SKELETON_SKULL)) {
                    skulls++;
                } else if (s.is(Blocks.CHEST)) {
                    chests++;
                } else if (s.is(Blocks.SOUL_FIRE)) {
                    fires++;
                } else if (s.is(ShadowGear.DUSK_QUARTZ_ORE.get())) {
                    quartz++;
                } else if (s.is(ShadowGear.DUSK_QUARTZ_BLOCK.get())) {
                    quartzBlocks++;
                } else if (s.is(ShadowBlocks.SHADOW_DOOR.get())) {
                    doors++;
                } else if (s.is(ShadowBlocks.SHADOW_LOG.get())) {
                    logs++;
                }
            }
            helper.assertTrue(fences > 60, "Zu wenig Zaun: " + fences);
            helper.assertTrue(lanterns >= 16, "Zu wenig Laternen: " + lanterns);
            helper.assertTrue(skulls >= 4, "Keine Schafsschädel auf den Pfosten: " + skulls);
            helper.assertTrue(chests == 1, "Nicht eine Truhe: " + chests);
            helper.assertTrue(fires == 4, "Nicht vier Feuerschalen: " + fires);
            helper.assertTrue(quartz == 4, "Nicht viermal Dämmerquarz am Podest: " + quartz);
            helper.assertTrue(quartzBlocks == 1, "Kein Dämmerquarz in der Hütte: " + quartzBlocks);
            helper.assertTrue(doors == 2, "Die Hütte hat keine Tür: " + doors);
            helper.assertTrue(logs > 40, "Zu wenig Schattenholz (Pfosten, Glockenstuhl, Stelzen): " + logs);
            // vier offene Tore, der Boden des Pferchs trägt
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos gate = o.relative(d, SoulPenPiece.PEN);
                helper.assertTrue(level.getBlockState(gate).isAir() && level.getBlockState(gate.above()).isAir(), "Das Tor im " + d + " ist zu");
                helper.assertTrue(!level.getBlockState(gate.below()).isAir(), "Vor dem Tor im " + d + " fehlt der Steg");
                helper.assertTrue(!level.getBlockState(o.relative(d, 7).below()).isAir(), "Im Pferch fehlt der Boden (" + d + ")");
            }
            // der Steg nach Süden reicht bis zur Plattform
            helper.assertTrue(!level.getBlockState(o.relative(Direction.SOUTH, SoulPenPiece.PATH_END).below()).isAir(), "Der Steg ist zu kurz");
        } finally {
            field.close();
        }
        helper.succeed();
    }

    /**
     * In der Schattenwelt entstehen Seelenpferche – im Seelensumpf: Über die Kandidaten der Verteilung (wie beim Erzeugen der
     * Welt) findet sich in einem weiten Gebiet mindestens ein passender Platz. Ohne die Welt selbst (der Testserver lädt nur die
     * Grunddimensionen) mit einem eigenen Erzeuger der Schattenwelt.
     */
    private static void world(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var registries = server.registryAccess();
        Holder<net.minecraft.world.level.levelgen.NoiseGeneratorSettings> settings = registries.lookupOrThrow(
                net.minecraft.core.registries.Registries.NOISE_SETTINGS).getOrThrow(io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain.SETTINGS);
        var generator = new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(
                net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromList(io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes.parameters(
                        registries.lookupOrThrow(net.minecraft.core.registries.Registries.BIOME))), settings);
        long seed = 20_261_026L;
        var random = net.minecraft.world.level.levelgen.RandomState.create(registries.lookupOrThrow(net.minecraft.core.registries.Registries.NOISE),
                seed, settings.value());
        var height = net.minecraft.world.level.LevelHeightAccessor.create(-64, 384);
        var biomes = generator.getBiomeSource().createUncachedResolver(random);
        var set = registries.lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET)
                .getOrThrow(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.SOUL_PENS);
        helper.assertTrue(set.value().placement() instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement,
                "Die Seelenpferche sind nicht gleichmäßig verteilt");
        var placement = (net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement) set.value().placement();
        var marsh = registries.lookupOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getOrThrow(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Biomes.HAS_SOUL_PEN);
        int cells = 0;
        int inMarsh = 0;
        int sites = 0;
        BlockPos first = null;
        int span = 7;
        for (int i = -span; i < span; i++) {
            for (int j = -span; j < span; j++) {
                net.minecraft.world.level.ChunkPos c = placement.getPotentialStructureChunk(seed, i * placement.spacing(), j * placement.spacing());
                int x = c.getMiddleBlockX();
                int z = c.getMiddleBlockZ();
                cells++;
                int ground = generator.getBaseHeight(x, z, net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG, height, random);
                if (marsh.contains(biomes.getNoiseBiome(x >> 2, Math.max(ground, generator.getSeaLevel() + 1) >> 2, z >> 2))) {
                    inMarsh++;
                }
                BlockPos site = io.github.sirschrolli.kingdomsatwar.worldgen.shepherd.SoulPenStructure.find(generator, height, random, biomes,
                        marsh::contains, c, seed + i * 31L + j);
                if (site != null) {
                    sites++;
                    if (first == null) {
                        first = site;
                    }
                }
            }
        }
        KingdomsAtWar.LOGGER.info("Seelenpferche: {} Felder, {} im Seelensumpf, {} passend (erster bei {})", cells, inMarsh, sites, first);
        helper.assertTrue(inMarsh > 0, "Kein Feld der Verteilung liegt im Seelensumpf");
        helper.assertTrue(sites * 40 >= cells, "Zu selten ein passender Platz für einen Seelenpferch: " + sites + " von " + cells + " Feldern");
        helper.succeed();
    }

    // =====================================================================================
    // Der Kampf
    // =====================================================================================

    private record Step(int delay, Runnable start, Runnable check) {}

    private static void run(GameTestHelper helper, List<Step> steps, int index, Runnable done) {
        if (index >= steps.size()) {
            done.run();
            return;
        }
        Step step = steps.get(index);
        step.start().run();
        helper.runAfterDelay(step.delay(), () -> {
            step.check().run();
            run(helper, steps, index + 1, done);
        });
    }

    /**
     * Im Pferch: Kommt ein Spieler in die Nähe, steht der Hirte in Andacht vor seiner Glocke (unverwundbar, ein Schlag weckt
     * ihn); er erwacht (auch dabei unverwundbar). Dann alle Angriffe gegen einen Ritter: Hakenzug, Laternenschwung,
     * Irrlichter, Glocke, Nebelgang, Pferch, Hirtenstoß; unter halber Kraft sein Zorn, im Zorn der Seelensturm. Besiegt lässt er
     * den Hirtenstab fallen, die Glocke verstummt; ein Seelenkristall ruft ihn zurück; zurückgesetzt steht beim nächsten Besuch
     * wieder einer in Andacht.
     */
    private static void fight(GameTestHelper helper) {
        helper.assertTrue(ModEntities.SOUL_SHEPHERD.get().getCategory() == MobCategory.MONSTER, "Seelenhirte: falsche Gruppe");
        Field field = field(helper, -29_000, -25_000);
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        SoulPenPiece.build(level, SoulPenPiece.makeBox(o), o, 99L, o.getY() - 20, RandomSource.create(3), null);
        BlockPos bellPos = SoulPenPiece.bell(o);
        if (!(level.getBlockEntity(bellPos) instanceof SoulBellBlockEntity bell)) {
            helper.fail("Die Seelenglocke hat keine Blockentität");
            field.close();
            return;
        }
        BlockPos home = bell.home();
        AABB area = new AABB(o).inflate(40, 24, 40);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-shepherd-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos outside = o.relative(SoulPenPiece.GATE, SoulPenPiece.PATH_END);
        move(player, outside.getX() + 0.5, outside.getY(), outside.getZ() + 0.5);
        helper.runAfterDelay(72, () -> {
            List<SoulShepherdEntity> found = level.getEntitiesOfClass(SoulShepherdEntity.class, area, e -> bellPos.equals(e.lair()));
            if (found.size() != 1) {
                field.close();
                helper.fail("Kein Hirte vor der Glocke: " + found.size());
                return;
            }
            SoulShepherdEntity shepherd = found.getFirst();
            helper.assertTrue(shepherd.isDormant(), "Der Hirte steht nicht in Andacht: " + shepherd.currentAttack());
            helper.assertTrue(shepherd.position().distanceTo(Vec3.atBottomCenterOf(home)) < 1.5, "Der Hirte steht nicht vor der Glocke: "
                    + shepherd.position());
            helper.assertTrue(Math.abs(net.minecraft.util.Mth.wrapDegrees(shepherd.getYRot() - SoulPenPiece.GATE.toYRot())) < 5.0f,
                    "Der Hirte blickt nicht zum Tor: " + shepherd.getYRot());
            helper.assertTrue(bell.owns(shepherd), "Die Glocke kennt ihren Hirten nicht");
            helper.assertTrue(level.getBlockState(bellPos).getValue(SoulBellBlock.PHASE) == SoulBellBlock.Phase.SLEEPING,
                    "Die Glocke ruht nicht");
            float hp = shepherd.getHealth();
            shepherd.hurtServer(level, level.damageSources().playerAttack(player), 40.0f);
            helper.assertTrue(shepherd.getHealth() >= hp, "In Andacht ist er verwundbar");
            helper.assertTrue(shepherd.currentAttack() == SoulShepherdEntity.Attack.AWAKEN, "Ein Schlag weckt ihn nicht: " + shepherd.currentAttack());
            shepherd.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(shepherd.getHealth() >= hp, "Beim Erwachen ist er verwundbar");
            level.getServer().getPlayerList().remove(player);
            helper.runAfterDelay(SoulShepherdEntity.Attack.AWAKEN.duration + 4, () -> {
                helper.assertTrue(shepherd.currentAttack() != SoulShepherdEntity.Attack.AWAKEN, "Das Erwachen endet nicht");
                helper.assertTrue(level.getBlockState(bellPos).getValue(SoulBellBlock.PHASE) == SoulBellBlock.Phase.AWAKE,
                        "Die Glocke merkt das Erwachen nicht");
                attacks(helper, field, bell, shepherd);
            });
        });
    }

    private static void attacks(GameTestHelper helper, Field field, SoulBellBlockEntity bell, SoulShepherdEntity shepherd) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            field.close();
            helper.fail("Kein Ritter");
            return;
        }
        idle((Mob) knight);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        knight.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        knight.setHealth(1000.0f);
        // mitten im Pferch, ein Stück vor dem Glockenstuhl (nach Süden)
        Vec3 spot = new Vec3(o.getX() + 0.5, o.getY(), o.getZ() - 1.5);
        knight.snapTo(spot.x + 5.0, spot.y, spot.z + 4.0, 0.0f, 0.0f);
        level.addFreshEntity(knight);
        float[] hp = {0.0f};
        double[] gap = {0.0};
        // vor jedem Angriff: der Ritter {@code ahead} Blöcke östlich des Hirten (beide auf dem Boden), geheilt, der Hirte blickt ihn an
        Consumer<Double> place = ahead -> {
            knight.removeAllEffects();
            knight.clearFire();
            knight.setHealth(knight.getMaxHealth());
            knight.setInvulnerableTime(0);
            knight.hurtTime = 0;
            knight.teleportTo(spot.x + ahead, spot.y, spot.z + 4.0);
            knight.setDeltaMovement(Vec3.ZERO);
            shepherd.snapTo(spot.x, spot.y, spot.z + 4.0, -90.0f, 0.0f);
            shepherd.setYBodyRot(-90.0f);
            shepherd.setYHeadRot(-90.0f);
            shepherd.setDeltaMovement(Vec3.ZERO);
            shepherd.setTarget(knight);
            hp[0] = knight.getHealth();
            gap[0] = knight.distanceTo(shepherd);
        };
        AABB around = new AABB(o).inflate(40, 24, 40);
        int[] wisps = {0};
        Vec3[] pen = {null};
        List<Step> steps = List.of(
                new Step(18, () -> {
                    place.accept(9.0);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.HOOK);
                }, () -> {
                    helper.assertTrue(knight.distanceTo(shepherd) < gap[0] - 2.0, "Der Haken zieht den Ritter nicht heran: " + knight.distanceTo(shepherd));
                    helper.assertTrue(knight.hasEffect(MobEffects.SLOWNESS), "Der Haken verlangsamt den Ritter nicht");
                }),
                new Step(30, () -> { }, () -> { }),
                new Step(22, () -> {
                    place.accept(3.0);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.LANTERN);
                }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Laternenschwung trifft den Ritter nicht");
                    helper.assertTrue(knight.getRemainingFireTicks() > 0, "Der Laternenschwung setzt den Ritter nicht in Brand");
                }),
                new Step(30, () -> {
                    place.accept(9.0);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.WISPS);
                }, () -> wisps[0] = level.getEntitiesOfClass(SoulWispEntity.class, around).size()),
                new Step(40, () -> { }, () -> {
                    helper.assertTrue(wisps[0] > 0, "Keine Irrlichter");
                    helper.assertTrue(knight.getHealth() < hp[0], "Die Irrlichter treffen den Ritter nicht");
                    helper.assertTrue(knight.hasEffect(MobEffects.WITHER), "Die Irrlichter lassen den Ritter nicht verdorren");
                }),
                new Step(24, () -> {
                    place.accept(5.0);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.BELL);
                }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Ring der Glocke trifft den Ritter nicht");
                    helper.assertTrue(knight.hasEffect(MobEffects.SLOWNESS) && knight.hasEffect(MobEffects.WEAKNESS),
                            "Der Ring der Glocke schwächt den Ritter nicht");
                }),
                new Step(30, () -> {
                    place.accept(6.0);
                    knight.setYRot(90.0f);
                    knight.setYHeadRot(90.0f);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.FOG);
                }, () -> {
                    helper.assertTrue(knight.hasEffect(MobEffects.BLINDNESS), "Aus dem Nebel trifft er den Ritter nicht");
                    helper.assertFalse(shepherd.isInvisible(), "Er bleibt unsichtbar");
                }),
                new Step(26, () -> {
                    place.accept(7.0);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.PEN);
                }, () -> {
                    pen[0] = shepherd.penCenter();
                    helper.assertTrue(pen[0] != null, "Kein Feuerkreis um den Ritter");
                    // den Ritter in die Flammen stellen
                    knight.teleportTo(pen[0].x + SoulShepherdEntity.PEN_RADIUS, pen[0].y, pen[0].z);
                    knight.setDeltaMovement(Vec3.ZERO);
                    hp[0] = knight.getHealth();
                }),
                new Step(10, () -> { }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Feuerkreis brennt den Ritter nicht");
                    helper.assertTrue(knight.getDeltaMovement().x < 0.0 || knight.getX() < pen[0].x + SoulShepherdEntity.PEN_RADIUS - 0.05,
                            "Der Feuerkreis stößt den Ritter nicht zurück");
                }),
                new Step(40, () -> { }, () -> { }),
                new Step(22, () -> {
                    place.accept(2.6);
                    shepherd.forceAttack(SoulShepherdEntity.Attack.CRASH);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Hirtenstoß trifft den Ritter nicht")));
        run(helper, steps, 0, () -> furyAndStorm(helper, field, bell, shepherd, knight, place));
    }

    /** Der Zorn unter halber Kraft und im Zorn der Seelensturm (er zieht den Ritter zu sich). */
    private static void furyAndStorm(GameTestHelper helper, Field field, SoulBellBlockEntity bell, SoulShepherdEntity shepherd, LivingEntity knight,
            Consumer<Double> place) {
        shepherd.setHealth(SoulShepherdEntity.MAX_HEALTH * 0.45f);
        shepherd.forceAttack(SoulShepherdEntity.Attack.NONE);
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(shepherd.currentAttack() == SoulShepherdEntity.Attack.FURY, "Unter halber Kraft läutet er nicht im Zorn: "
                    + shepherd.currentAttack());
            helper.runAfterDelay(SoulShepherdEntity.Attack.FURY.duration, () -> {
                helper.assertTrue(shepherd.isEnraged(), "Nach dem Läuten ist er nicht im Zorn");
                place.accept(6.5);
                double before = knight.distanceTo(shepherd);
                shepherd.forceAttack(SoulShepherdEntity.Attack.STORM);
                helper.runAfterDelay(40, () -> {
                    helper.assertTrue(knight.distanceTo(shepherd) < before - 1.0, "Der Seelensturm zieht den Ritter nicht heran: "
                            + knight.distanceTo(shepherd));
                    knight.discard();
                    death(helper, field, bell, shepherd);
                });
            });
        });
    }

    /** Tod: der Hirtenstab, die Glocke verstummt; ein Seelenkristall ruft ihn zurück; zurückgesetzt in Andacht. */
    private static void death(GameTestHelper helper, Field field, SoulBellBlockEntity bell, SoulShepherdEntity shepherd) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        BlockPos bellPos = SoulPenPiece.bell(o);
        AABB around = new AABB(o).inflate(40, 24, 40);
        shepherd.forceAttack(SoulShepherdEntity.Attack.NONE);
        shepherd.hurtServer(level, level.damageSources().genericKill(), Float.MAX_VALUE);
        helper.assertTrue(shepherd.isDeadOrDying(), "Der Hirte stirbt nicht");
        helper.runAfterDelay(SoulShepherdEntity.DEATH_TICKS + 6, () -> {
            helper.assertTrue(shepherd.isRemoved(), "Der Hirte zerfällt nicht");
            boolean crook = !level.getEntitiesOfClass(ItemEntity.class, around, e -> e.getItem().is(SoulShepherd.CROOK.get())).isEmpty();
            helper.assertTrue(crook, "Er lässt den Hirtenstab nicht fallen");
            helper.assertTrue(level.getBlockState(bellPos).getValue(SoulBellBlock.PHASE) == SoulBellBlock.Phase.DEFEATED,
                    "Die Glocke verstummt nicht");
            helper.assertTrue(bell.recall(level, null), "Ein Seelenkristall ruft ihn nicht zurück");
            SoulShepherdEntity next = bell.shepherd(level);
            helper.assertTrue(next != null && next.currentAttack() == SoulShepherdEntity.Attack.AWAKEN, "Der gerufene Hirte erwacht nicht");
            bell.reset(level);
            helper.assertTrue(bell.shepherd(level) == null, "Zurückgesetzt bleibt der Hirte");
            helper.assertTrue(level.getBlockState(bellPos).getValue(SoulBellBlock.PHASE) == SoulBellBlock.Phase.SLEEPING,
                    "Zurückgesetzt ruht die Glocke nicht wieder");
            field.close();
            helper.succeed();
        });
    }

    // =====================================================================================
    // Der Hirtenstab
    // =====================================================================================

    /** Der Hirtenstab hakt einen anvisierten Ritter und reißt ihn heran (Schaden, Langsamkeit); danach ruht er. */
    private static void crook(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 16; z++) {
                helper.setBlock(x, 0, z, Blocks.MUD_BRICKS);
                for (int y = 1; y < 5; y++) {
                    helper.setBlock(x, y, z, Blocks.AIR);
                }
            }
        }
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            helper.fail("Kein Ritter");
            return;
        }
        // ohne Rüstung (sonst steht er zu fest)
        for (net.minecraft.world.entity.EquipmentSlot slot : net.minecraft.world.entity.EquipmentSlot.values()) {
            knight.setItemSlot(slot, ItemStack.EMPTY);
        }
        BlockPos kp = helper.absolutePos(new BlockPos(4, 1, 12));
        knight.snapTo(kp.getX() + 0.5, kp.getY(), kp.getZ() + 0.5, 0.0f, 0.0f);
        idle((Mob) knight);
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
        knight.getAttribute(Attributes.KNOCKBACK_RESISTANCE).removeModifiers();
        knight.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        knight.setHealth(200.0f);
        level.addFreshEntity(knight);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-shepherd-crook", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos stand = helper.absolutePos(new BlockPos(4, 1, 2));
        player.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        ItemStack crook = new ItemStack(SoulShepherd.CROOK.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, crook);
        helper.runAfterDelay(2, () -> {
            // auf die Mitte des Ritters zielen
            Vec3 aim = knight.getBoundingBox().getCenter().subtract(player.getEyePosition());
            player.setYRot(0.0f);
            player.setXRot((float) Math.toDegrees(Math.atan2(-aim.y, Math.hypot(aim.x, aim.z))));
            double before = knight.distanceTo(player);
            crook.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getCooldowns().isOnCooldown(crook), "Der Stab ruht danach nicht");
            helper.assertTrue(knight.getHealth() < 200.0f, "Der Haken trifft den Ritter nicht");
            helper.assertTrue(knight.hasEffect(MobEffects.SLOWNESS), "Der Haken verlangsamt den Ritter nicht");
            helper.assertTrue(ShepherdCrookItem.RANGE >= 12.0, "Die Kette ist zu kurz");
            helper.runAfterDelay(12, () -> {
                helper.assertTrue(knight.distanceTo(player) < before - 3.0, "Der Haken reißt den Ritter nicht heran: " + knight.distanceTo(player));
                knight.discard();
                level.getServer().getPlayerList().remove(player);
                helper.succeed();
            });
        });
    }
}
