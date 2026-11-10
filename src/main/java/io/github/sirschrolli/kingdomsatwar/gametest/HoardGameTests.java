package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.abyss.WorldSavior;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardLocator;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.block.abyss.HoardSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.abyss.WorldEaterTrophyBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldChunkEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterPart;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.abyss.WorldSpearItem;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThronePlacement;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zum Schlund der Welten (0.73): das Bauwerk und sein fester Ort, das Siegel und das Erwachen, die Angriffe des
 * Weltenfressers, Treffer an seinen Gliedern, sein Tod mit Beute und Titel, der Weltenspeer und die Trophäe.
 */
final class HoardGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD_EATER_HOARD =
            ModGameTests.TEST_FUNCTIONS.register("world_eater_hoard", () -> HoardGameTests::hoard);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD_EATER_FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("world_eater_fight", () -> HoardGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD_SPEAR_WORLDS =
            ModGameTests.TEST_FUNCTIONS.register("world_spear_worlds", () -> HoardGameTests::spear);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD_EATER_TROPHY =
            ModGameTests.TEST_FUNCTIONS.register("world_eater_trophy", () -> HoardGameTests::trophy);

    private HoardGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(WORLD_EATER_HOARD, WORLD_SPEAR_WORLDS, WORLD_EATER_TROPHY)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        // der Kampf dauert: Erwachen, Biss, Weltenspucke, Treffer an den Gliedern, Zerfall
        event.registerTest(WORLD_EATER_FIGHT.getId(), new FunctionGameTestInstance(WORLD_EATER_FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 1600, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    private static void floor(GameTestHelper helper, int size) {
        for (int x = 0; x < size; x++) {
            for (int z = 0; z < size; z++) {
                helper.setBlock(x, 0, z, AbyssBlocks.POLISHED_CHAOS_STONE.base().get());
            }
        }
    }

    /** Wartet höchstens {@code maxTicks}, bis {@code done} gilt, und macht dann weiter – sonst schlägt der Test fehl. */
    private static void await(GameTestHelper helper, BooleanSupplier done, int maxTicks, String message, Runnable then) {
        await(helper, done, maxTicks, () -> message, then);
    }

    private static void await(GameTestHelper helper, BooleanSupplier done, int maxTicks, java.util.function.Supplier<String> message,
            Runnable then) {
        if (done.getAsBoolean()) {
            then.run();
        } else if (maxTicks <= 0) {
            helper.fail(message.get());
        } else {
            helper.runAfterDelay(1, () -> await(helper, done, maxTicks - 1, message, then));
        }
    }

    /**
     * Der Schlund der Welten: als Bauwerk des Abgrunds angemeldet (über allen Biomen des Abgrunds), genau einer je Welt im
     * Ring 500–900 Blöcke um die Mitte, an festem Ort; die sieben Kronensplitter stehen zwischen den Speichen auf dem Ring,
     * das Siegel liegt unter dem Ring; die Oberwelt hat keinen.
     */
    private static void hoard(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var registries = server.registryAccess();
        helper.assertTrue(registries.lookupOrThrow(Registries.STRUCTURE).get(ModStructures.WORLD_EATER_HOARD_STRUCTURE).isPresent(),
                "Das Bauwerk Schlund der Welten fehlt");
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.WORLD_EATER_HOARD_SET);
        helper.assertTrue(set.isPresent() && set.get().value().placement() instanceof ThronePlacement, "Die Platzierung des Schlunds fehlt");
        var placement = (ThronePlacement) set.get().value().placement();
        for (long seed : new long[] {1L, 42L, -77L, 20_261_110L, "kingdoms at war".hashCode()}) {
            var chunk = ThroneLocator.chunk(seed, placement);
            double distance = Math.sqrt(Math.pow(chunk.getMiddleBlockX(), 2) + Math.pow(chunk.getMiddleBlockZ(), 2));
            helper.assertTrue(distance > 480 && distance < 930, "Der Schlund liegt nicht im Ring: " + distance);
            helper.assertTrue(chunk.equals(ThroneLocator.chunk(seed, placement)), "Der Ort des Schlunds ist nicht fest");
        }
        var biomes = registries.lookupOrThrow(Registries.BIOME);
        for (ResourceKey<Biome> biome : AbyssBiomes.ALL) {
            helper.assertTrue(biomes.getOrThrow(biome).is(ModTags.Biomes.HAS_WORLD_EATER_HOARD),
                    "Der Schlund kann nicht über " + biome.identifier() + " schweben");
        }
        // die Kronensplitter: sieben, alle auf dem Ring (zwischen Schlund und Rand), einer über dem Boden, alle verschieden
        BlockPos home = new BlockPos(1000, HoardPiece.FLOOR + 1, -2000);
        Set<BlockPos> shards = new HashSet<>();
        for (int k = 0; k < HoardPiece.SHARDS; k++) {
            BlockPos p = HoardPiece.shardPosition(home, k);
            double r = Math.sqrt(Math.pow(p.getX() - home.getX(), 2) + Math.pow(p.getZ() - home.getZ(), 2));
            helper.assertTrue(r > HoardPiece.R_IN && r < HoardPiece.R_OUT, "Kronensplitter " + k + " steht nicht auf dem Ring: " + r);
            helper.assertTrue(p.getY() == home.getY() + 1, "Kronensplitter " + k + " in falscher Höhe");
            shards.add(p);
        }
        helper.assertTrue(shards.size() == HoardPiece.SHARDS, "Kronensplitter fallen zusammen");
        BlockPos seal = HoardPiece.sealPosition(home);
        double sealR = Math.abs(seal.getZ() - home.getZ());
        helper.assertTrue(seal.getY() < home.getY() - 1 && sealR > HoardPiece.R_IN && sealR < HoardPiece.R_OUT,
                "Das Siegel liegt nicht unter dem Ring");
        ServerLevel abyss = server.getLevel(AbyssGate.THE_ABYSS);
        if (abyss != null) {
            BlockPos center = HoardLocator.center(abyss);
            helper.assertTrue(center != null && center.getY() == HoardPiece.FLOOR + 1, "Kein Schlund der Welten im Abgrund");
        }
        helper.assertTrue(HoardLocator.find(helper.getLevel()) == null, "Auch die Oberwelt hat einen Schlund der Welten");
        // wer den Ring betritt, weckt ihn – wer weit draußen ist oder tief darunter, nicht
        helper.assertTrue(HoardSealBlockEntity.wakes(home, home.getX() + 18.5, home.getY(), home.getZ() + 0.5), "Der Ring weckt ihn nicht");
        helper.assertFalse(HoardSealBlockEntity.wakes(home, home.getX() + 60.5, home.getY(), home.getZ() + 0.5), "Er erwacht auch von weit draußen");
        helper.assertFalse(HoardSealBlockEntity.wakes(home, home.getX() + 18.5, home.getY() - 30, home.getZ() + 0.5), "Er erwacht auch von tief unten");
        helper.succeed();
    }

    /**
     * Der Weltenfresser: das Siegel weckt ihn (beim Erwachen unverwundbar), danach treffen Biss und Weltenspucke; Treffer an
     * den gepanzerten Gliedern zählen weniger als am Kopf; besiegt zerfällt er Glied um Glied, lässt auf dem Ring
     * Weltenspeer und Trophäe zurück, das Siegel bleibt still, und wer dabei war, wird Weltenretter.
     */
    /** Ein eigenes Feld fern der Prüfflächen (der Weltenfresser kreist 40 Blöcke weit): Chunks erzwungen, am Ende geräumt. */
    private record Field(ServerLevel level, BlockPos center, List<long[]> chunks) {
        void close() {
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, new AABB(this.center).inflate(90, 70, 90),
                    e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(this.center.offset(-5, -2, -5), this.center.offset(5, -1, 5))) {
                this.level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }

        /** Führt {@code action} aus; schlägt sie fehl, wird das Feld vorher geräumt. */
        Runnable guard(Runnable action) {
            return () -> {
                try {
                    action.run();
                } catch (RuntimeException e) {
                    this.close();
                    throw e;
                }
            };
        }
    }

    private static Field field(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos center = new BlockPos(base.getX() + 23_000, 150, base.getZ() - 16_000);
        List<long[]> chunks = new java.util.ArrayList<>();
        int reach = 56;
        for (int cx = (center.getX() - reach) >> 4; cx <= (center.getX() + reach) >> 4; cx++) {
            for (int cz = (center.getZ() - reach) >> 4; cz <= (center.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-5, -1, -5), center.offset(5, -1, 5))) {
            level.setBlock(p, AbyssBlocks.POLISHED_CHAOS_STONE.base().get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        return new Field(level, center, chunks);
    }

    private static void fight(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Field field = field(helper);
        BlockPos center = field.center();
        BlockPos sealPos = center.offset(0, -2, 4);
        level.setBlock(sealPos, AbyssBlocks.HOARD_SEAL.get().defaultBlockState(), Block.UPDATE_ALL);
        field.guard(() -> {
            if (!(level.getBlockEntity(sealPos) instanceof HoardSealBlockEntity seal)) {
                throw new IllegalStateException("Das Siegel des Horts hat keine Blockentität");
            }
            seal.setup(center);
            helper.assertTrue(seal.phase() == HoardSealBlockEntity.Phase.DORMANT, "Der Schlund ruht nicht");
            WorldEaterEntity eater = seal.awaken(level);
            helper.assertTrue(eater != null, "Das Siegel weckt den Weltenfresser nicht");
            helper.assertTrue(level.getEntity(eater.getUUID()) == eater, "Der Weltenfresser ist nicht in der Welt");
            helper.assertTrue(eater.isAwakening(), "Der Weltenfresser erwacht nicht");
            helper.assertTrue(seal.phase() == HoardSealBlockEntity.Phase.AWAKE, "Der Schlund merkt das Erwachen nicht");
            helper.assertTrue(eater.eaterParts().length == WorldEaterEntity.SEGMENTS + 1, "Falsche Zahl an Gliedern");
            float before = eater.getHealth();
            eater.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(eater.getHealth() >= before, "Beim Erwachen ist der Weltenfresser verwundbar");
            var knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            knight.snapTo(center.getX() + 0.5, center.getY(), center.getZ() + 2.5, 180.0f, 0.0f);
            knight.setNoAi(true);
            level.addFreshEntity(knight);
            ServerPlayer witness = helper.makeMockServerPlayerInLevel();
            witness.teleportTo(level, center.getX() + 0.5, center.getY(), center.getZ() - 3.5, java.util.Set.of(), 0.0f, 0.0f, true);
            witness.setPermanentlyInvulnerable(true);
            eater.debugHold(knight);
            await(helper, field, () -> !eater.isAwakening(), WorldEaterEntity.AWAKEN_TICKS + 120, () -> "Der Weltenfresser erhebt sich nicht (bei "
                    + eater.position() + ", Phase " + eater.phaseName() + ", Takte " + eater.tickCount + ")", () -> {
                helper.assertTrue(eater.headCenter().y > center.getY(), "Er steigt nicht über den Schlund empor");
                // der Biss
                float health = knight.getHealth();
                eater.forceAttack(level, "bite", knight);
                helper.runAfterDelay(WorldEaterEntity.BITE_STRIKE + 12, field.guard(() -> {
                    helper.assertTrue(knight.getHealth() < health, "Der Biss trifft nicht");
                    knight.setHealth(knight.getMaxHealth());
                    knight.setDeltaMovement(Vec3.ZERO);
                    knight.teleportTo(center.getX() + 0.5, center.getY(), center.getZ() + 2.5);
                    // die Weltenspucke
                    eater.forceAttack(level, "spit", knight);
                    helper.runAfterDelay(26, field.guard(() -> {
                        helper.assertFalse(level.getEntitiesOfClass(WorldChunkEntity.class, eater.getBoundingBox().inflate(80)).isEmpty(),
                                "Er speit keine Weltbrocken");
                        float spitBefore = knight.getHealth();
                        await(helper, field, () -> knight.getHealth() < spitBefore, 80, () -> "Die Weltbrocken treffen nicht", () ->
                                helper.runAfterDelay(WorldEaterEntity.SPIT_TICKS, field.guard(() -> parts(helper, level, field, eater, seal, witness))));
                    }));
                }));
            });
        }).run();
    }

    /** Wie {@link #await}, räumt bei einem Fehlschlag das Feld. */
    private static void await(GameTestHelper helper, Field field, BooleanSupplier done, int maxTicks, java.util.function.Supplier<String> message,
            Runnable then) {
        if (done.getAsBoolean()) {
            field.guard(then).run();
        } else if (maxTicks <= 0) {
            field.close();
            helper.fail(message.get());
        } else {
            helper.runAfterDelay(1, () -> await(helper, field, done, maxTicks - 1, message, then));
        }
    }

    /** Treffer an Glied und Kopf, dann sein Tod. */
    private static void parts(GameTestHelper helper, ServerLevel level, Field field, WorldEaterEntity eater, HoardSealBlockEntity seal,
            ServerPlayer witness) {
        WorldEaterPart part = eater.eaterParts()[3];
        float h0 = eater.getHealth();
        part.hurtServer(level, level.damageSources().playerAttack(witness), 40.0f);
        float bodyHit = h0 - eater.getHealth();
        helper.assertTrue(bodyHit > 0.0f, "Treffer an den Gliedern zählen nicht");
        eater.damageCooldownTime = 0;
        float h1 = eater.getHealth();
        eater.hurtServer(level, level.damageSources().playerAttack(witness), 40.0f);
        float headHit = h1 - eater.getHealth();
        helper.assertTrue(headHit > bodyHit, "Der Kopf ist nicht verwundbarer als der Leib (" + headHit + " / " + bodyHit + ")");
        // sein Tod
        eater.setHealth(1.0f);
        eater.damageCooldownTime = 0;
        eater.hurtServer(level, level.damageSources().playerAttack(witness), 50.0f);
        helper.assertTrue(eater.isDeadOrDying(), "Der Weltenfresser fällt nicht");
        helper.runAfterDelay(WorldEaterEntity.DEATH_TICKS + 8, field.guard(() -> {
            helper.assertTrue(eater.isRemoved(), "Er zerfällt nicht");
            helper.assertTrue(seal.phase() == HoardSealBlockEntity.Phase.DEFEATED, "Der Schlund merkt den Sieg nicht");
            AABB around = new AABB(seal.center()).inflate(48);
            List<ItemEntity> loot = level.getEntitiesOfClass(ItemEntity.class, around);
            helper.assertTrue(loot.stream().anyMatch(e -> e.getItem().is(AbyssBlocks.WORLD_SPEAR.get())), "Kein Weltenspeer in der Beute");
            helper.assertTrue(loot.stream().anyMatch(e -> e.getItem().is(AbyssBlocks.WORLD_EATER_TROPHY.get().asItem())), "Keine Trophäe in der Beute");
            helper.assertTrue(WorldSavior.is(witness), "Wer dabei war, wird nicht Weltenretter");
            String shown = WorldSavior.prefixed(witness.getName()).getString();
            helper.assertTrue(shown.length() > witness.getName().getString().length(), "Der Titel steht nicht vor dem Namen: " + shown);
            field.close();
            helper.succeed();
        }));
    }

    /**
     * Der Weltenspeer: ein Speer wie die von Minecraft (verzauberbar, mit Weltennähten zu reparieren); jeder Treffer trägt
     * reihum eine Welt in sich (Glut, Frost, End, Schatten), jeder vierte ruft einen Weltbrocken herab, der das Ziel trifft
     * und den Träger verschont.
     */
    private static void spear(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 9);
        ItemStack spear = new ItemStack(AbyssBlocks.WORLD_SPEAR.get());
        helper.assertTrue(spear.is(ItemTags.SPEARS), "Der Weltenspeer ist kein Speer");
        helper.assertTrue(spear.getRarity() == Rarity.EPIC, "Der Weltenspeer ist nicht episch");
        helper.assertTrue(spear.isValidRepairItem(new ItemStack(AbyssBlocks.WORLD_SEAM.get())), "Weltennähte reparieren ihn nicht");
        helper.assertTrue(spear.getMaxDamage() > 3000, "Der Weltenspeer hält zu wenig aus");
        ServerPlayer player = helper.makeMockServerPlayerInLevel();
        player.teleportTo(level, helper.absolutePos(new BlockPos(1, 1, 1)).getX() + 0.5, helper.absolutePos(new BlockPos(1, 1, 1)).getY(),
                helper.absolutePos(new BlockPos(1, 1, 1)).getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        var spearItem = (WorldSpearItem) AbyssBlocks.WORLD_SPEAR.get();
        helper.assertTrue(spearItem.getAttackDamageBonus(player, 12.0f, level.damageSources().playerAttack(player)) == 0.0f,
                "In der Oberwelt trifft er härter");
        // ein Ritter der Mod (Monster von Minecraft werden beim Erscheinen durch die der Mod ersetzt)
        var target = helper.spawnWithNoFreeWill(ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), new BlockPos(6, 1, 6));
        // vier Treffer: Glut, Frost, End, Schatten – der vierte ruft den Brocken
        spearItem.postHurtEnemy(spear, target, player);
        helper.assertTrue(target.isOnFire(), "Die Glut setzt nicht in Brand");
        spearItem.postHurtEnemy(spear, target, player);
        helper.assertTrue(target.hasEffect(MobEffects.SLOWNESS), "Der Frost lähmt nicht");
        spearItem.postHurtEnemy(spear, target, player);
        helper.assertTrue(target.hasEffect(MobEffects.LEVITATION), "Das End hebt nicht empor");
        helper.assertTrue(level.getEntitiesOfClass(WorldChunkEntity.class, new AABB(target.blockPosition()).inflate(20)).isEmpty(),
                "Schon vor dem vierten Treffer stürzt ein Brocken");
        spearItem.postHurtEnemy(spear, target, player);
        helper.assertTrue(target.hasEffect(MobEffects.BLINDNESS), "Der Schatten blendet nicht");
        helper.assertFalse(level.getEntitiesOfClass(WorldChunkEntity.class, new AABB(target.blockPosition()).inflate(20)).isEmpty(),
                "Der vierte Treffer ruft keinen Weltbrocken");
        target.removeEffect(MobEffects.LEVITATION);
        target.setDeltaMovement(Vec3.ZERO);
        float before = target.getHealth();
        float playerBefore = player.getHealth();
        WorldChunkEntity chunk = level.getEntitiesOfClass(WorldChunkEntity.class, new AABB(target.blockPosition()).inflate(20)).get(0);
        Vec3 start = chunk.position();
        await(helper, () -> target.getHealth() < before, 60, () -> "Der Weltbrocken trifft das Ziel nicht (Start " + start + ", Ziel "
                + target.position() + ", Brocken " + (chunk.isRemoved() ? "zerschellt bei " : "noch bei ") + chunk.position() + ", Takte "
                + chunk.tickCount + "; Ziel: Takte " + target.tickCount + ", Leben " + target.getHealth() + "/" + before + ", unverwundbar "
                + target.isInvulnerable() + ", brennt " + target.getRemainingFireTicks() + ", lebt " + target.isAlive() + ")", () -> {
            helper.assertTrue(player.getHealth() >= playerBefore, "Der Weltbrocken trifft den Träger");
            helper.succeed();
        });
    }

    /**
     * Die Trophäe: ein Block mit Blockentität (für den Kopf), der sich selbst fallen lässt, leuchtet und dem zugewandt
     * steht, der ihn aufstellt.
     */
    private static void trophy(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        floor(helper, 5);
        BlockPos pos = helper.absolutePos(new BlockPos(2, 1, 2));
        level.setBlock(pos, AbyssBlocks.WORLD_EATER_TROPHY.get().defaultBlockState(), Block.UPDATE_ALL);
        helper.assertTrue(level.getBlockEntity(pos) instanceof WorldEaterTrophyBlockEntity, "Die Trophäe hat keine Blockentität");
        helper.assertTrue(level.getBlockState(pos).getLightEmission(level, pos) > 0, "Die Trophäe leuchtet nicht");
        List<ItemStack> drops = Block.getDrops(level.getBlockState(pos), level, pos, level.getBlockEntity(pos));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(AbyssBlocks.WORLD_EATER_TROPHY.get().asItem())), "Die Trophäe lässt sich nicht abbauen");
        helper.assertTrue(new ItemStack(AbyssBlocks.WORLD_EATER_TROPHY.get()).getRarity() == Rarity.EPIC, "Die Trophäe ist nicht episch");
        helper.succeed();
    }

    /** Nur für Aufräumarbeiten in anderen Tests: alles, was der Weltenfresser zurücklässt. */
    static void clear(ServerLevel level, AABB box) {
        level.getEntitiesOfClass(Entity.class, box, e -> e instanceof WorldEaterEntity || e instanceof WorldChunkEntity).forEach(Entity::discard);
    }
}
