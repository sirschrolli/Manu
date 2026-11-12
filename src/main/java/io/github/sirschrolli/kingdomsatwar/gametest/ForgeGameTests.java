package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlock;
import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Brandgebein;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutklumpenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.nether.GlutspeerItem;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Malleus, dem Glutschmied (0.64): der Glutamboss (schmiedender Schmied, Erwachen, Sieg, Rückruf mit Netherit),
 * seine Angriffe und der Glutspeer.
 */
final class ForgeGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ANVIL =
            ModGameTests.TEST_FUNCTIONS.register("forge_anvil", () -> ForgeGameTests::anvil);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("forge_attacks", () -> ForgeGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SPEAR =
            ModGameTests.TEST_FUNCTIONS.register("forge_spear", () -> ForgeGameTests::spear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ROOF =
            ModGameTests.TEST_FUNCTIONS.register("forge_roof", () -> ForgeGameTests::roof);

    private ForgeGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SPEAR, ROOF)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(ANVIL, ATTACKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1000, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.POLISHED_BLACKSTONE_BRICKS);
            }
        }
    }

    private static void cleanup(ServerLevel level, AABB area) {
        for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, area, e -> !(e instanceof ServerPlayer))) {
            e.discard();
        }
    }

    // =====================================================================================
    // Der Glutamboss
    // =====================================================================================

    /**
     * Der Glutamboss: unzerstörbar; kommt jemand in die Nähe, kniet Malleus schon am Amboss und schmiedet (unverwundbar);
     * tritt jemand näher heran, erwacht er und nimmt den Speer vom Amboss. Danach ist er verwundbar (doch nicht durch
     * Feuer), wird unter halber Kraft weißglühend, und besiegt lässt er den Glutspeer fallen – der Amboss erkaltet. Ein
     * Netheritbarren facht ihn neu an und ruft den Schmied zurück.
     */
    private static void anvil(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        helper.assertTrue(ModEntities.GLUTSCHMIED.get().getCategory() == MobCategory.MONSTER, "Malleus: falsche Gruppe");
        BlockPos pos = helper.absolutePos(new BlockPos(4, 1, 1));
        level.setBlock(pos, ForgeBlocks.GLUT_ANVIL.get().defaultBlockState().setValue(GlutAnvilBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Glutamboss lässt sich abbauen");
        if (!(level.getBlockEntity(pos) instanceof GlutAnvilBlockEntity forge)) {
            helper.fail("Der Glutamboss hat keine Blockentität");
            return;
        }
        helper.assertTrue(forge.phase() == GlutAnvilBlockEntity.Phase.DORMANT, "Der Amboss ruht nicht");
        AABB area = new AABB(pos).inflate(5, 4, 5);
        // ein Spieler im Überlebensmodus (die Testhilfe kennt nur Kreativspieler – die übersieht der Amboss)
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-forge-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 9.0);
        helper.runAfterDelay(25, () -> {
            var smiths = level.getEntitiesOfClass(GlutschmiedEntity.class, area);
            helper.assertTrue(smiths.size() == 1, "Kein (oder mehr als ein) Schmied am Amboss: " + smiths.size() + " (Spieler "
                    + helper.relativePos(player.blockPosition()) + ", " + player.gameMode() + ", in der Welt: " + level.players().contains(player)
                    + ", Schwierigkeit " + level.getDifficulty() + ", Amboss " + forge.phase() + ")");
            GlutschmiedEntity smith = smiths.getFirst();
            helper.assertTrue(smith.currentAttack() == GlutschmiedEntity.Attack.FORGE, "Er schmiedet nicht: " + smith.currentAttack());
            helper.assertTrue(forge.owns(smith), "Der Amboss kennt seinen Schmied nicht");
            helper.assertTrue(smith.blockPosition().equals(pos.relative(Direction.SOUTH, 2)), "Er kniet nicht vor dem Amboss: "
                    + helper.relativePos(smith.blockPosition()));
            float hp = smith.getHealth();
            smith.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(smith.getHealth() >= hp && smith.currentAttack() == GlutschmiedEntity.Attack.FORGE,
                    "Beim Schmieden ist er verwundbar");
            // jemand tritt heran: er erwacht
            move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 5.0);
            helper.runAfterDelay(12, () -> {
                helper.assertTrue(smith.currentAttack() == GlutschmiedEntity.Attack.AWAKEN, "Er erwacht nicht: " + smith.currentAttack());
                helper.assertTrue(forge.phase() == GlutAnvilBlockEntity.Phase.AWAKE, "Der Amboss merkt das Erwachen nicht");
                level.getServer().getPlayerList().remove(player);
                helper.runAfterDelay(GlutschmiedEntity.TAKE_SPEAR + 2, () -> {
                    helper.assertTrue(level.getBlockState(pos).getValue(GlutAnvilBlock.PHASE) == GlutAnvilBlock.Phase.AWAKE,
                            "Der Speer liegt noch auf dem Amboss");
                    helper.runAfterDelay(GlutschmiedEntity.AWAKEN_TICKS - GlutschmiedEntity.TAKE_SPEAR + 4, () -> fight(helper, level, pos, forge,
                            smith, area));
                });
            });
        });
    }

    /** Versetzt den Spieler und meldet ihn an der neuen Stelle an (wie es sonst seine Verbindung jeden Tick tut). */
    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    private static void fight(GameTestHelper helper, ServerLevel level, BlockPos pos, GlutAnvilBlockEntity forge, GlutschmiedEntity smith,
            AABB area) {
        helper.assertTrue(smith.currentAttack() != GlutschmiedEntity.Attack.AWAKEN && smith.currentAttack() != GlutschmiedEntity.Attack.FORGE,
                "Er erwacht nicht ganz: " + smith.currentAttack());
        smith.setInvulnerableTime(0);
        float hp = smith.getHealth();
        smith.hurtServer(level, level.damageSources().onFire(), 20.0f);
        helper.assertTrue(smith.getHealth() >= hp, "Feuer schadet ihm");
        smith.setInvulnerableTime(0);
        smith.hurtServer(level, level.damageSources().generic(), 20.0f);
        helper.assertTrue(smith.getHealth() < hp, "Wach ist er unverwundbar");
        smith.setHealth(smith.getMaxHealth() * 0.45f);
        helper.runAfterDelay(24, () -> {
            helper.assertTrue(smith.isStoked(), "Unter halber Kraft wird er nicht weißglühend");
            smith.setInvulnerableTime(0);
            smith.setHealth(1.0f);
            BossLootGameTests.firstVictory(helper, smith, 50.0f);
            helper.assertTrue(smith.isDeadOrDying(), "Malleus fällt nicht");
            helper.assertTrue(forge.phase() == GlutAnvilBlockEntity.Phase.DEFEATED, "Der Amboss merkt den Sieg nicht");
            helper.assertTrue(level.getBlockState(pos).getValue(GlutAnvilBlock.PHASE) == GlutAnvilBlock.Phase.COLD, "Der Amboss erkaltet nicht");
            helper.runAfterDelay(GlutschmiedEntity.DEATH_TICKS + 5, () -> {
                helper.assertTrue(smith.isRemoved(), "Er zerfällt nicht");
                helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, area, e -> e.getItem().is(ForgeBlocks.MOLTEN_GLOB.get())).isEmpty(),
                        "Er lässt keine Glutklumpen fallen");
                // ein Netheritbarren facht die Esse neu an
                helper.assertTrue(forge.rekindle(level, null), "Netherit facht die Esse nicht an");
                var again = level.getEntitiesOfClass(GlutschmiedEntity.class, area, LivingEntity::isAlive);
                helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == GlutschmiedEntity.Attack.AWAKEN,
                        "Der Schmied kehrt nicht zurück");
                helper.assertTrue(level.getBlockState(pos).getValue(GlutAnvilBlock.PHASE) == GlutAnvilBlock.Phase.DORMANT,
                        "Der Speer liegt nicht wieder auf dem Amboss");
                helper.assertFalse(forge.rekindle(level, null), "Die Esse lässt sich zweimal anfachen");
                cleanup(level, area);
                helper.succeed();
            });
        });
    }

    /**
     * Das Dach der Aschenesse: vor dem Schlot steht der Glutamboss (mit dem Speer, Blick nach vorn), davor ist Platz für den
     * Schmied, darunter das Dach; daneben Lavakessel und Schleifstein. Gebaut wird nur eine schmale Säule des Turms, hoch
     * über dem Test (so bleiben die Nachbartests unberührt).
     */
    private static void roof(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 110, -3));
        var tower = io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.ashForge(origin);
        BlockPos anvil = io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.ashForgeAnvil(tower);
        var box = net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(new BlockPos(anvil.getX() - 4, origin.getY() + 1,
                anvil.getZ() - 1), new BlockPos(anvil.getX() + 4, anvil.getY() + 4, anvil.getZ() + 4));
        tower.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(5), box,
                net.minecraft.world.level.ChunkPos.containing(origin), origin);
        var state = level.getBlockState(anvil);
        helper.assertTrue(state.is(ForgeBlocks.GLUT_ANVIL.get()), "Kein Glutamboss auf dem Dach: " + state);
        helper.assertTrue(state.getValue(GlutAnvilBlock.PHASE) == GlutAnvilBlock.Phase.DORMANT, "Auf dem Amboss liegt kein Speer");
        helper.assertTrue(state.getValue(GlutAnvilBlock.FACING) == Direction.SOUTH, "Der Amboss blickt nicht nach vorn: " + state);
        helper.assertTrue(level.getBlockEntity(anvil) instanceof GlutAnvilBlockEntity, "Der Amboss hat keine Blockentität");
        BlockPos place = anvil.relative(Direction.SOUTH, 2);
        for (BlockPos p : BlockPos.betweenClosed(place.offset(-1, 0, -1), place.offset(1, 3, 1))) {
            helper.assertTrue(level.getBlockState(p).isAir(), "Der Platz des Schmieds ist verbaut: " + level.getBlockState(p));
        }
        helper.assertTrue(level.getBlockState(place.below()).isSolidRender(), "Unter dem Schmied ist kein Dach");
        helper.assertTrue(level.getBlockState(anvil.east(3)).is(Blocks.LAVA_CAULDRON), "Kein Lavakessel neben dem Amboss");
        // (der Amboss weckte sonst einen Schmied, sobald ein Spieler eines anderen Tests in die Nähe käme)
        for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.succeed();
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /**
     * Seine Angriffe gegen einen Ritter: der Speerstoß trifft und setzt in Brand, die Schockwelle des Hammerschlags trifft,
     * der Funkenflug trifft, der Glutwurf schleudert Glutklumpen, die zerplatzen, und der Ruf holt zwei Brandgebeine.
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        GlutschmiedEntity smith = helper.spawn(ModEntities.GLUTSCHMIED.get(), new BlockPos(4, 1, 1));
        LivingEntity knight = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(4, 1, 4));
        AABB area = smith.getBoundingBox().inflate(5.5, 3, 5.5);
        float[] hp = {knight.getHealth()};
        smith.setTarget(knight);
        smith.forceAttack(GlutschmiedEntity.Attack.THRUST);
        helper.runAfterDelay(12, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Der Speerstoß trifft nicht");
            helper.assertTrue(knight.isOnFire(), "Der Speerstoß setzt nicht in Brand");
            reset(knight, hp);
            smith.setTarget(knight);
            smith.forceAttack(GlutschmiedEntity.Attack.SLAM);
            helper.runAfterDelay(24, () -> {
                helper.assertTrue(knight.getHealth() < hp[0], "Die Schockwelle trifft nicht");
                reset(knight, hp);
                smith.setTarget(knight);
                smith.forceAttack(GlutschmiedEntity.Attack.SPARKS);
                helper.runAfterDelay(22, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Funkenflug trifft nicht");
                    reset(knight, hp);
                    smith.setTarget(knight);
                    smith.forceAttack(GlutschmiedEntity.Attack.GLOBS);
                    helper.runAfterDelay(14, () -> {
                        helper.assertFalse(level.getEntitiesOfClass(GlutklumpenEntity.class, area).isEmpty(), "Er schleudert keine Glutklumpen");
                        helper.runAfterDelay(34, () -> {
                            helper.assertTrue(knight.getHealth() < hp[0], "Die Glutklumpen treffen nicht");
                            helper.assertTrue(level.getEntitiesOfClass(GlutklumpenEntity.class, area).isEmpty(), "Glutklumpen bleiben liegen");
                            reset(knight, hp);
                            smith.setTarget(knight);
                            smith.forceAttack(GlutschmiedEntity.Attack.CALL);
                            helper.runAfterDelay(18, () -> {
                                var apprentices = level.getEntitiesOfClass(Brandgebein.class, area);
                                helper.assertTrue(apprentices.size() == 2, "Der Ruf holt keine zwei Brandgebeine: " + apprentices.size());
                                apprentices.forEach(net.minecraft.world.entity.Entity::discard);
                                chainAndMolten(helper, level, smith, knight, hp, () -> {
                                    cleanup(level, area);
                                    helper.succeed();
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /**
     * Glutkette (sie erwischt den Ritter – er glüht und wird gebremst –, der Hammer trifft) und Schmelzfluss (die Rinnen aus
     * Glut erreichen ihn). Der Ritter hat keinen eigenen Willen und bleibt stehen (heranziehen lässt er sich so nicht).
     */
    private static void chainAndMolten(GameTestHelper helper, ServerLevel level, GlutschmiedEntity smith, LivingEntity knight, float[] hp,
            Runnable then) {
        reset(knight, hp);
        smith.setTarget(knight);
        smith.forceAttack(GlutschmiedEntity.Attack.CHAIN);
        helper.runAfterDelay(GlutschmiedEntity.CHAIN_THROW + 3, () -> {
            helper.assertTrue(knight.isOnFire() && knight.hasEffect(net.minecraft.world.effect.MobEffects.SLOWNESS), "Die Glutkette erwischt den Ritter nicht");
        });
        helper.runAfterDelay(GlutschmiedEntity.CHAIN_SMASH + 1, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Der Hammer nach der Glutkette trifft nicht");
            reset(knight, hp);
            smith.setTarget(knight);
            smith.forceAttack(GlutschmiedEntity.Attack.MOLTEN);
            helper.runAfterDelay(GlutschmiedEntity.MOLTEN_HIT + 12, () -> {
                helper.assertTrue(knight.getHealth() < hp[0], "Der Schmelzfluss trifft nicht");
                helper.assertTrue(knight.isOnFire(), "Der Schmelzfluss setzt nicht in Brand");
                then.run();
            });
        });
    }

    private static void reset(LivingEntity knight, float[] hp) {
        knight.clearFire();
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        hp[0] = knight.getHealth();
    }

    // =====================================================================================
    // Der Glutspeer
    // =====================================================================================

    /**
     * Der Glutspeer: ein Speer (Stoß und Ansturm, verzauberbar wie die Speere von Minecraft – ebenso der Gezeiten-Speer),
     * feuerfest, mit Netheritschrott zu reparieren; brennend trifft er härter; jeder Treffer setzt in Brand, und brennend
     * lodert ein Flammenring um das Ziel auf. Der Glutklumpen ist Brennstoff.
     */
    private static void spear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ItemStack stack = new ItemStack(ForgeBlocks.GLUT_SPEAR.get());
        helper.assertTrue(stack.has(DataComponents.KINETIC_WEAPON), "Der Glutspeer ist kein Speer");
        helper.assertTrue(stack.is(ItemTags.SPEARS) && new ItemStack(SeaBlocks.TIDE_SPEAR.get()).is(ItemTags.SPEARS),
                "Die Speere der Bosse sind nicht als Speere verzauberbar");
        helper.assertTrue(stack.has(DataComponents.DAMAGE_RESISTANT), "Der Glutspeer verbrennt");
        helper.assertTrue(stack.isValidRepairItem(new ItemStack(Items.NETHERITE_SCRAP)), "Netheritschrott repariert ihn nicht");
        helper.assertTrue(new ItemStack(ForgeBlocks.MOLTEN_GLOB.get()).has(DataComponents.COOKING_FUEL), "Der Glutklumpen brennt nicht im Ofen");
        GlutspeerItem spear = ForgeBlocks.GLUT_SPEAR.get();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        LivingEntity target = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(4, 1, 4));
        LivingEntity other = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(5, 1, 5));
        var source = level.damageSources().playerAttack(player);
        helper.assertTrue(spear.getAttackDamageBonus(target, 9.0f, source) == 0.0f, "Ohne Glut trifft er härter");
        player.igniteForSeconds(5.0f);
        helper.assertTrue(spear.getAttackDamageBonus(target, 9.0f, source) > 2.9f, "Brennend trifft er nicht härter");
        spear.postHurtEnemy(stack, target, player);
        helper.assertTrue(target.isOnFire(), "Der Treffer setzt nicht in Brand");
        helper.assertTrue(other.isOnFire(), "Kein Flammenring um das Ziel");
        level.getServer().getPlayerList().remove(player);
        // nur die eigenen Wesen wegräumen (ein weiter Bereich träfe die Nachbartests)
        target.discard();
        other.discard();
        helper.succeed();
    }
}
