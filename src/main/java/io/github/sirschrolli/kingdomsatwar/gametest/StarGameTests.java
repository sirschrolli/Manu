package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlock;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.end.StarWings;
import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.CometLanceEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.StarfallEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.end.StarLanceItem;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
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
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
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
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu Asteria, der Sternenreiterin (0.65): der Sternstein (Herabstoßen, Supernova unter halber Kraft, Sieg, Rückruf
 * mit einem Endkristall), ihre Angriffe, die Sternenlanze, die Sternenschwingen und der Sternstein in der Sternwarte.
 * Alles, wobei sie fliegt, spielt auf eigenen Feldern weit draußen (sie fliegt weit, ihre Angriffe reichen weit; so
 * bleiben die Nachbartests unberührt).
 */
final class StarGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> STONE =
            ModGameTests.TEST_FUNCTIONS.register("star_stone", () -> StarGameTests::stone);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ATTACKS =
            ModGameTests.TEST_FUNCTIONS.register("star_attacks", () -> StarGameTests::attacks);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> LANCE =
            ModGameTests.TEST_FUNCTIONS.register("star_lance", () -> StarGameTests::lance);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WATCH =
            ModGameTests.TEST_FUNCTIONS.register("star_watch", () -> StarGameTests::watch);

    private StarGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(LANCE, WATCH)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        for (var test : List.of(STONE, ATTACKS)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 1200, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
    }

    private static void floor(GameTestHelper helper) {
        for (int x = 0; x < 9; x++) {
            for (int z = 0; z < 9; z++) {
                helper.setBlock(x, 0, z, Blocks.END_STONE_BRICKS);
            }
        }
    }

    /**
     * Ein eigenes Feld weit draußen (Asteria fliegt weit; dort stört sie keinen Nachbartest), die Chunks ringsum werden
     * geladen gehalten, damit sie auch fern der Mitte weiterfliegt: eine Plattform aus Endsteinziegeln mit der Mitte bei
     * {@code origin}.
     */
    private record Field(ServerLevel level, BlockPos origin, List<long[]> chunks) {
        void close() {
            AABB area = new AABB(this.origin).inflate(48, 48, 48);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(this.origin.offset(-6, -1, -6), this.origin.offset(6, 2, 6))) {
                this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    private static Field field(GameTestHelper helper, int slot) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos origin = new BlockPos(base.getX() - 15000 - slot * 400, 150, base.getZ() - 15000);
        List<long[]> chunks = new java.util.ArrayList<>();
        int reach = 44;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        for (int x = -5; x <= 5; x++) {
            for (int z = -5; z <= 5; z++) {
                level.setBlock(origin.offset(x, -1, z), Blocks.END_STONE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        return new Field(level, origin, chunks);
    }

    /** Versetzt den Spieler und meldet ihn an der neuen Stelle an (wie es sonst seine Verbindung jeden Tick tut). */
    private static void move(ServerPlayer player, double x, double y, double z) {
        player.teleportTo(player.level(), x, y, z, java.util.Set.of(), 180.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }

    // =====================================================================================
    // Der Sternstein
    // =====================================================================================

    /**
     * Der Sternstein: unzerstörbar; tritt ein Spieler heran, stößt Asteria hoch vom Himmel herab (unverwundbar, bis sie die
     * Lanze hebt). Danach ist sie verwundbar, unter halber Kraft beginnt die Supernova, und besiegt lässt sie Sternenlanze
     * und Sternenschwingen fallen – der Stein erlischt. Ein Endkristall entzündet ihn neu und ruft sie zurück.
     */
    private static void stone(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 0);
        helper.assertTrue(ModEntities.ASTERIA.get().getCategory() == MobCategory.MONSTER, "Asteria: falsche Gruppe");
        BlockPos pos = field.origin();
        level.setBlock(pos, StarBlocks.STARSTONE.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockState(pos).getDestroySpeed(level, pos) < 0, "Der Sternstein lässt sich abbauen");
        helper.assertTrue(level.getBlockState(pos).getLightEmission(level, pos) == 15, "Der Sternstein leuchtet nicht");
        if (!(level.getBlockEntity(pos) instanceof StarstoneBlockEntity stone)) {
            helper.fail("Der Sternstein hat keine Blockentität");
            return;
        }
        helper.assertTrue(stone.phase() == StarstoneBlockEntity.Phase.DORMANT, "Der Sternstein ruht nicht");
        AABB area = new AABB(pos).inflate(20, 45, 20);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-star-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        move(player, pos.getX() + 0.5, pos.getY(), pos.getZ() + 4.5);
        helper.runAfterDelay(15, () -> {
            var riders = level.getEntitiesOfClass(AsteriaEntity.class, area, e -> pos.equals(e.home()));
            helper.assertTrue(riders.size() == 1, "Asteria stößt nicht herab: " + riders.size() + " (Stein " + stone.phase() + ")");
            AsteriaEntity asteria = riders.getFirst();
            level.getServer().getPlayerList().remove(player);
            helper.assertTrue(asteria.currentAttack() == AsteriaEntity.Attack.AWAKEN, "Sie stößt nicht herab: " + asteria.currentAttack());
            helper.assertTrue(asteria.getY() > pos.getY() + 15, "Sie erscheint nicht hoch am Himmel: " + (asteria.getY() - pos.getY()));
            helper.assertTrue(stone.owns(asteria) && pos.equals(asteria.home()), "Der Stein kennt seine Reiterin nicht");
            helper.assertTrue(level.getBlockState(pos).getValue(StarstoneBlock.PHASE) == StarstoneBlock.Phase.AWAKE, "Der Stein merkt es nicht");
            float hp = asteria.getHealth();
            asteria.hurtServer(level, level.damageSources().generic(), 50.0f);
            helper.assertTrue(asteria.getHealth() >= hp, "Beim Herabstoßen ist sie verwundbar");
            helper.runAfterDelay(AsteriaEntity.AWAKEN_TICKS, () -> {
                helper.assertTrue(asteria.currentAttack() != AsteriaEntity.Attack.AWAKEN, "Sie kommt nicht an: " + asteria.currentAttack());
                helper.assertTrue(asteria.getY() < pos.getY() + 16, "Sie bleibt hoch am Himmel: " + (asteria.getY() - pos.getY()));
                asteria.setInvulnerableTime(0);
                asteria.hurtServer(level, level.damageSources().generic(), 20.0f);
                helper.assertTrue(asteria.getHealth() < asteria.getMaxHealth(), "Angekommen ist sie unverwundbar");
                asteria.setHealth(asteria.getMaxHealth() * 0.45f);
                helper.runAfterDelay(3, () -> {
                    helper.assertTrue(asteria.currentAttack() == AsteriaEntity.Attack.SUPERNOVA,
                            "Unter halber Kraft beginnt keine Supernova: " + asteria.currentAttack());
                    asteria.setInvulnerableTime(0);
                    asteria.setHealth(1.0f);
                    BossLootGameTests.firstVictory(helper, asteria, 50.0f);
                    helper.assertTrue(asteria.isDeadOrDying(), "Asteria fällt nicht");
                    helper.assertTrue(stone.phase() == StarstoneBlockEntity.Phase.DEFEATED, "Der Stein merkt den Sieg nicht");
                    helper.assertTrue(level.getBlockState(pos).getValue(StarstoneBlock.PHASE) == StarstoneBlock.Phase.DIM, "Der Stein erlischt nicht");
                    helper.runAfterDelay(AsteriaEntity.DEATH_TICKS + 5, () -> {
                        helper.assertTrue(asteria.isRemoved(), "Sie vergeht nicht");
                        var drops = level.getEntitiesOfClass(ItemEntity.class, area, e -> true);
                        helper.assertTrue(drops.stream().anyMatch(e -> e.getItem().is(io.github.sirschrolli.kingdomsatwar.registry.ModItems.HEART_CRYSTAL.get())), "Sie lässt keinen Herzkristall fallen");
                        drops.forEach(Entity::discard);
                        // ein Endkristall entzündet den Stein neu
                        helper.assertTrue(stone.rekindle(level, null), "Der Endkristall entzündet den Stein nicht");
                        var again = level.getEntitiesOfClass(AsteriaEntity.class, area, e -> e.isAlive() && pos.equals(e.home()));
                        helper.assertTrue(again.size() == 1 && again.getFirst().currentAttack() == AsteriaEntity.Attack.AWAKEN,
                                "Sie kehrt nicht zurück");
                        helper.assertTrue(level.getBlockState(pos).getValue(StarstoneBlock.PHASE) == StarstoneBlock.Phase.AWAKE, "Der Stein leuchtet nicht");
                        helper.assertFalse(stone.rekindle(level, null), "Der Stein lässt sich zweimal entzünden");
                        stone.reset(level);
                        helper.assertTrue(level.getEntitiesOfClass(AsteriaEntity.class, area, e -> e.isAlive() && pos.equals(e.home())).isEmpty(),
                                "Nach dem Zurücksetzen bleibt sie");
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
                        field.close();
                        helper.succeed();
                    });
                });
            });
        });
    }

    // =====================================================================================
    // Die Angriffe
    // =====================================================================================

    /**
     * Ihre Angriffe gegen einen Ritter (hoch über dem Testfeld, schwebend): die Tjost reitet ihn nieder, der Sternenregen
     * trifft, das Sternbild trifft und lässt ihn schweben, der Komet trifft und lässt ihn leuchten, der Schwingenschlag
     * wirft ihn fort; das Schwarze Loch zerreibt ihn am Kern und trifft ihn mit der Lichtwelle, die Mondsicheln treffen.
     */
    private static void attacks(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        Field field = field(helper, 1);
        BlockPos o = field.origin();
        LivingEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        AsteriaEntity asteria = ModEntities.ASTERIA.get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
        if (knight == null || asteria == null) {
            helper.fail("Ritter oder Asteria lassen sich nicht erschaffen");
            return;
        }
        knight.snapTo(o.getX() + 0.5, o.getY(), o.getZ() + 0.5, 0.0f, 0.0f);
        ((net.minecraft.world.entity.Mob) knight).setNoAi(true);
        knight.setNoGravity(true);
        level.addFreshEntity(knight);
        asteria.snapTo(o.getX() + 0.5, o.getY() + 5.0, o.getZ() - 3.5, 0.0f, 0.0f);
        level.addFreshEntity(asteria);
        Vec3 spot = knight.position();
        AABB area = new AABB(o).inflate(40, 20, 40);
        float[] hp = {knight.getHealth()};
        asteria.setTarget(knight);
        asteria.forceAttack(AsteriaEntity.Attack.TJOST);
        helper.runAfterDelay(AsteriaEntity.TJOST_END + 4, () -> {
            helper.assertTrue(knight.getHealth() < hp[0], "Die Tjost trifft nicht");
            reset(knight, spot, hp);
            asteria.setTarget(knight);
            asteria.forceAttack(AsteriaEntity.Attack.STARFALL);
            helper.runAfterDelay(30, () -> {
                helper.assertFalse(level.getEntitiesOfClass(StarfallEntity.class, area, e -> e.getOwner() == asteria).isEmpty(), "Es fallen keine Sterne");
                helper.runAfterDelay(32, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Sternenregen trifft nicht");
                    reset(knight, spot, hp);
                    asteria.setTarget(knight);
                    asteria.forceAttack(AsteriaEntity.Attack.CONSTELLATION);
                    helper.runAfterDelay(49, () -> {
                        helper.assertTrue(knight.getHealth() < hp[0], "Das Sternbild trifft nicht");
                        helper.assertTrue(knight.hasEffect(MobEffects.LEVITATION), "Das Sternbild lässt ihn nicht schweben");
                        reset(knight, spot, hp);
                        asteria.setTarget(knight);
                        asteria.forceAttack(AsteriaEntity.Attack.COMET);
                        helper.runAfterDelay(AsteriaEntity.COMET_THROW + 2, () -> {
                            helper.assertFalse(level.getEntitiesOfClass(CometLanceEntity.class, area, e -> e.getOwner() == asteria).isEmpty(),
                                    "Sie wirft keinen Kometen");
                            helper.assertFalse(asteria.holdsLance(AsteriaEntity.COMET_THROW + 2), "Die Lanze bleibt in ihrer Hand");
                            helper.runAfterDelay(16, () -> {
                                helper.assertTrue(knight.getHealth() < hp[0], "Der Komet trifft nicht");
                                helper.assertTrue(knight.hasEffect(MobEffects.GLOWING), "Der Komet lässt ihn nicht leuchten");
                                reset(knight, spot, hp);
                                asteria.teleportTo(spot.x, spot.y + 4.0, spot.z - 3.0);
                                asteria.setDeltaMovement(Vec3.ZERO);
                                asteria.setTarget(knight);
                                asteria.forceAttack(AsteriaEntity.Attack.WINGBEAT);
                                helper.runAfterDelay(24, () -> {
                                    helper.assertTrue(knight.getHealth() < hp[0], "Der Schwingenschlag trifft nicht");
                                    helper.assertTrue(knight.position().distanceTo(spot) > 0.5 || knight.getDeltaMovement().horizontalDistance() > 0.05,
                                            "Der Schwingenschlag wirft ihn nicht fort");
                                    blackHoleAndCrescents(helper, level, asteria, knight, spot, hp, area, () -> {
                                        field.close();
                                        helper.succeed();
                                    });
                                });
                            });
                        });
                    });
                });
            });
        });
    }

    /** Die Künste von 0.68: das Schwarze Loch (Kern, Wirbel, Lichtwelle beim Zusammensturz) und die Mondsicheln. */
    private static void blackHoleAndCrescents(GameTestHelper helper, ServerLevel level, AsteriaEntity asteria, LivingEntity knight, Vec3 spot,
            float[] hp, AABB area, Runnable then) {
        reset(knight, spot, hp);
        asteria.teleportTo(spot.x, spot.y + 6.0, spot.z - 8.0);
        asteria.setDeltaMovement(Vec3.ZERO);
        asteria.setTarget(knight);
        asteria.forceAttack(AsteriaEntity.Attack.BLACK_HOLE);
        helper.runAfterDelay(AsteriaEntity.HOLE_OPEN + 4, () -> {
            helper.assertTrue(asteria.currentAttack() == AsteriaEntity.Attack.BLACK_HOLE, "Das Schwarze Loch bricht ab: " + asteria.currentAttack());
            helper.assertFalse(level.getEntitiesOfClass(SpellFxEntity.class, area, e -> e.kind() == SpellFxEntity.Kind.VORTEX).isEmpty(),
                    "Es öffnet sich kein Wirbel");
            helper.assertTrue(knight.getHealth() < hp[0], "Der Kern zerreibt ihn nicht");
            helper.assertTrue(knight.hasEffect(MobEffects.SLOWNESS), "Der Kern hält ihn nicht fest");
            // vier Blöcke neben dem Kern: dort zerreibt er nicht mehr, aber die Lichtwelle reicht so weit
            reset(knight, spot.add(4.0, 0.0, 0.0), hp);
            helper.runAfterDelay(AsteriaEntity.HOLE_COLLAPSE - AsteriaEntity.HOLE_OPEN - 8, () -> {
                helper.assertTrue(knight.getHealth() >= hp[0], "Der Kern trifft auch neben ihm");
                helper.runAfterDelay(12, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Die Lichtwelle trifft nicht, wenn das Loch zusammenstürzt");
                    helper.assertTrue(level.getEntitiesOfClass(SpellFxEntity.class, area, e -> e.kind() == SpellFxEntity.Kind.VORTEX).isEmpty(),
                            "Der Wirbel bleibt nach dem Zusammensturz");
                    reset(knight, spot, hp);
                    asteria.teleportTo(spot.x, spot.y + 5.0, spot.z - 9.0);
                    asteria.setDeltaMovement(Vec3.ZERO);
                    asteria.setTarget(knight);
                    asteria.forceAttack(AsteriaEntity.Attack.CRESCENT);
                    helper.runAfterDelay(AsteriaEntity.CRESCENT_FIRST + 2, () -> {
                        helper.assertFalse(level.getEntitiesOfClass(SpellFxEntity.class, area, e -> e.kind() == SpellFxEntity.Kind.CRESCENT).isEmpty(),
                                "Sie sendet keine Mondsichel");
                        helper.runAfterDelay(16, () -> {
                            helper.assertTrue(knight.getHealth() < hp[0], "Die Mondsichel trifft nicht");
                            then.run();
                        });
                    });
                });
            });
        });
    }

    private static void reset(LivingEntity knight, Vec3 spot, float[] hp) {
        knight.removeAllEffects();
        knight.setHealth(knight.getMaxHealth());
        knight.setInvulnerableTime(0);
        knight.teleportTo(spot.x, spot.y, spot.z);
        knight.setDeltaMovement(Vec3.ZERO);
        hp[0] = knight.getHealth();
    }

    // =====================================================================================
    // Sternenlanze und Sternenschwingen
    // =====================================================================================

    /**
     * Die Sternenlanze: ein Speer (verzauberbar wie die Speere von Minecraft), feuerfest, mit Sternsplittern zu reparieren;
     * unter dem Nachthimmel (oder im End) trifft sie härter; Getroffene leuchten, und jeder dritte Treffer ruft einen kleinen
     * Stern herab. Die Sternenschwingen gleiten, sind mit Phantomhaut und Sternsplittern zu reparieren, und der Sternenschub
     * treibt nach vorn (kostet Haltbarkeit und hat eine Abklingzeit).
     */
    private static void lance(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper);
        ItemStack stack = new ItemStack(StarBlocks.STAR_LANCE.get());
        helper.assertTrue(stack.has(DataComponents.KINETIC_WEAPON), "Die Sternenlanze ist kein Speer");
        helper.assertTrue(stack.is(ItemTags.SPEARS), "Die Sternenlanze ist nicht als Speer verzauberbar");
        helper.assertTrue(stack.has(DataComponents.DAMAGE_RESISTANT), "Die Sternenlanze verbrennt");
        helper.assertTrue(stack.isValidRepairItem(new ItemStack(StarBlocks.STAR_SHARD.get())), "Sternsplitter reparieren sie nicht");
        helper.assertTrue(stack.getMaxDamage() > new ItemStack(io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks.GLUT_SPEAR.get()).getMaxDamage(),
                "Die Sternenlanze hält nicht länger als der Glutspeer");
        StarLanceItem lance = StarBlocks.STAR_LANCE.get();
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.setGameMode(GameType.SURVIVAL);
        LivingEntity target = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), new BlockPos(4, 1, 4));
        var source = level.damageSources().playerAttack(player);
        boolean night = level.isDarkOutside() && level.canSeeSky(player.blockPosition().above());
        helper.assertTrue(StarLanceItem.starlit(player) == night, "Sternenkraft ohne Nachthimmel (oder ohne sie in der Nacht)");
        helper.assertTrue((lance.getAttackDamageBonus(target, 9.0f, source) > 2.9f) == night, "Die Sternenkraft stimmt nicht");
        AABB area = new AABB(helper.absolutePos(new BlockPos(4, 1, 4))).inflate(3, 16, 3);
        for (int i = 0; i < 2; i++) {
            lance.postHurtEnemy(stack, target, player);
        }
        helper.assertTrue(target.hasEffect(MobEffects.GLOWING), "Getroffene leuchten nicht");
        helper.assertTrue(level.getEntitiesOfClass(StarfallEntity.class, area).isEmpty(), "Schon nach zwei Treffern fällt ein Stern");
        lance.postHurtEnemy(stack, target, player);
        var stars = level.getEntitiesOfClass(StarfallEntity.class, area);
        helper.assertTrue(stars.size() == 1 && stars.getFirst().isSmall(), "Der dritte Treffer ruft keinen kleinen Stern: " + stars.size());
        stars.forEach(Entity::discard);
        // die Sternenschwingen
        ItemStack wings = new ItemStack(StarBlocks.STAR_WINGS.get());
        helper.assertTrue(wings.has(DataComponents.GLIDER), "Die Sternenschwingen gleiten nicht");
        helper.assertTrue(wings.isValidRepairItem(new ItemStack(Items.PHANTOM_MEMBRANE)) && wings.isValidRepairItem(new ItemStack(StarBlocks.STAR_SHARD.get())),
                "Phantomhaut und Sternsplitter reparieren die Schwingen nicht");
        player.setItemSlot(EquipmentSlot.CHEST, wings);
        helper.assertTrue(StarWings.wearing(player), "Die Schwingen werden nicht erkannt");
        Vec3 look = new Vec3(0.0, 0.0, 1.0);
        Vec3 boosted = StarWings.boostVelocity(new Vec3(0.0, -0.2, 0.6), look);
        helper.assertTrue(boosted.z > 1.5 && boosted.length() <= 2.81, "Der Sternenschub treibt nicht nach vorn: " + boosted);
        player.startFallFlying();
        StarWings.boost(player);
        ItemStack worn = player.getItemBySlot(EquipmentSlot.CHEST);
        helper.assertTrue(worn.getDamageValue() == 1, "Der Sternenschub kostet keine Haltbarkeit: " + worn.getDamageValue());
        helper.assertTrue(player.getCooldowns().isOnCooldown(worn), "Der Sternenschub hat keine Abklingzeit");
        StarWings.boost(player);
        helper.assertTrue(worn.getDamageValue() == 1, "Der Sternenschub zündet trotz Abklingzeit");
        level.getServer().getPlayerList().remove(player);
        target.discard();
        helper.succeed();
    }

    // =====================================================================================
    // Die Sternwarte
    // =====================================================================================

    /**
     * Die Sternwarte: in der Mitte des Sternenhofs steht der Sternstein (ruhend, mit Blockentität) auf seinem Pfeiler, ringsum
     * die Leuchtstäbe. Gebaut wird nur eine schmale Säule, hoch über dem Test (so bleiben die Nachbartests unberührt).
     */
    private static void watch(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = helper.absolutePos(new BlockPos(4, 120, 0));
        var piece = io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.starwatch(origin, 1L);
        BlockPos stone = io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.starwatchStone(piece);
        var box = net.minecraft.world.level.levelgen.structure.BoundingBox.fromCorners(stone.offset(-3, -2, -3), stone.offset(3, 2, 3));
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(5), box,
                net.minecraft.world.level.ChunkPos.containing(origin), origin);
        var state = level.getBlockState(stone);
        helper.assertTrue(state.is(StarBlocks.STARSTONE.get()), "Kein Sternstein im Sternenhof: " + state);
        helper.assertTrue(state.getValue(StarstoneBlock.PHASE) == StarstoneBlock.Phase.DORMANT, "Der Sternstein ruht nicht");
        helper.assertTrue(level.getBlockEntity(stone) instanceof StarstoneBlockEntity, "Der Sternstein hat keine Blockentität");
        helper.assertFalse(level.getBlockState(stone.below()).isAir(), "Der Sternstein steht nicht auf seinem Pfeiler");
        helper.assertTrue(level.getBlockState(stone.above()).isAir(), "Über dem Sternstein ist es verbaut");
        // (der Stein riefe sonst Asteria, sobald ein Spieler eines anderen Tests in die Nähe käme)
        for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        helper.succeed();
    }
}
