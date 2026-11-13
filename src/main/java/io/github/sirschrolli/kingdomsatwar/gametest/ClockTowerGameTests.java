package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssBiomes;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssTerrain;
import io.github.sirschrolli.kingdomsatwar.abyss.feature.SeamVeinFeature;
import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlock;
import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlockEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.ClockGearEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.ClockPendulumEntity;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.HourWardenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossHealth;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.abyss.MinuteHandItem;
import io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssGear;
import io.github.sirschrolli.kingdomsatwar.registry.ClockTower;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerClearing;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerStructure;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.FunctionGameTestInstance;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestData;
import net.minecraft.gametest.framework.TestEnvironmentDefinition;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MultiNoiseBiomeSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests der Erweiterung des Abgrunds (seit 1.1): Chronit (Erz, Drops, an den Weltennähten), die Rüstung der Zeitwächter
 * (Schutz, Die Zeit stockt, Zweite Chance mit Totem und Abklingzeit), der Uhrturm (Bauwerk und Verteilung im Abgrund), der Kampf
 * gegen den Stundenwächter und sein Minutenzeiger.
 */
final class ClockTowerGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> CHRONIT =
            ModGameTests.TEST_FUNCTIONS.register("clock_chronit", () -> ClockTowerGameTests::chronit);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("clock_armor", () -> ClockTowerGameTests::armor);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> TOWER =
            ModGameTests.TEST_FUNCTIONS.register("clock_tower", () -> ClockTowerGameTests::tower);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD =
            ModGameTests.TEST_FUNCTIONS.register("clock_world", () -> ClockTowerGameTests::world);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("clock_fight", () -> ClockTowerGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEAPON =
            ModGameTests.TEST_FUNCTIONS.register("clock_weapon", () -> ClockTowerGameTests::weapon);

    private ClockTowerGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(CHRONIT, ARMOR, TOWER, WORLD, WEAPON)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 2400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Chronit
    // =====================================================================================

    /**
     * Das Erz gibt Chronit (ab einer Diamantspitzhacke) und schimmert; Chronit wächst in jedem Biom des Abgrunds unter und neben
     * den Weltennähten; Rezepte für Schmelzen, Block und Rüstung gibt es.
     */
    private static void chronit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockState ore = AbyssGear.CHRONIT_ORE.get().defaultBlockState();
        BlockPos center = helper.absolutePos(new BlockPos(7, 4, 7));
        List<ItemStack> drops = Block.getDrops(ore, level, center, null, null, new ItemStack(Items.DIAMOND_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(AbyssGear.CHRONIT.get())), "Das Erz gibt kein Chronit: " + drops);
        helper.assertTrue(ore.getLightEmission() > 0 && AbyssGear.CHRONIT_BLOCK.get().defaultBlockState().getLightEmission() > 0,
                "Chronit schimmert nicht");
        helper.assertTrue(ore.is(BlockTags.NEEDS_DIAMOND_TOOL) && ore.is(BlockTags.MINEABLE_WITH_PICKAXE), "Das Erz braucht keine Diamantspitzhacke");
        helper.assertTrue(new ItemStack(Items.DIAMOND_PICKAXE).isCorrectToolForDrops(ore), "Die Diamantspitzhacke baut es nicht ab");
        helper.assertFalse(new ItemStack(Items.IRON_PICKAXE).isCorrectToolForDrops(ore), "Schon die Eisenspitzhacke baut es ab");
        helper.assertTrue(ore.is(ModTags.Blocks.ORES_CHRONIT) && new ItemStack(AbyssGear.CHRONIT.get()).is(ModTags.Items.GEMS_CHRONIT),
                "Die Tags des Chronits fehlen");

        // in jedem Biom des Abgrunds gibt es Weltennähte (und damit Chronit)
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        var seams = ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("abyss/world_seams"));
        for (var key : AbyssBiomes.ALL) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(seams);
                }
            }
            helper.assertTrue(found, "Keine Weltennähte im Biom " + key.identifier());
        }

        // eine Weltennaht über einem Feld aus Chaosgestein: darunter und daneben gerinnt Chronit
        Block stone = AbyssBlocks.CHAOS_STONE.base().get();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 0, 7))) {
            level.setBlock(p, stone.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        var generator = level.getChunkSource().getGenerator();
        int seam = 0;
        int chronit = 0;
        for (int seed = 0; seed < 24 && chronit < 2; seed++) {
            new SeamVeinFeature().place(level, generator, RandomSource.create(seed), center.above());
            seam = count(level, center, AbyssBlocks.WORLD_SEAM.get());
            chronit = count(level, center, AbyssGear.CHRONIT_ORE.get());
        }
        helper.assertTrue(seam > 0, "Keine Weltennaht auf dem Chaosgestein (Höhe " + center.getY() + ")");
        helper.assertTrue(chronit >= 2, "An der Weltennaht wächst kaum Chronit: " + chronit + " bei " + seam + " Nahtblöcken");
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 1, 7))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }

        // Rezepte: Schmelzen, Hochofen, Block und zurück, die vier Teile der Rüstung
        var recipes = level.getServer().getRecipeManager().recipeMap();
        for (String id : List.of("chronit_from_smelting_chronit_ore", "chronit_from_blasting_chronit_ore", "chronit_block",
                "chronit_from_chronit_block", "timewarden_helmet", "timewarden_chestplate", "timewarden_leggings", "timewarden_boots")) {
            helper.assertTrue(recipes.byKey(ResourceKey.create(Registries.RECIPE, KingdomsAtWar.id(id))) != null, "Rezept fehlt: " + id);
        }
        helper.succeed();
    }

    private static int count(ServerLevel level, BlockPos center, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-7, -2, -7), center.offset(7, 1, 7))) {
            n += level.getBlockState(p).is(block) ? 1 : 0;
        }
        return n;
    }

    // =====================================================================================
    // Die Rüstung der Zeitwächter
    // =====================================================================================

    /**
     * Schutz wie Netherit, feuerfest, mit Chronit auszubessern; wer den Träger trifft, stockt manchmal; mit dem vollen Satz spult
     * ein tödlicher Treffer drei Sekunden zurück (Ort, Herzen, schädliche Wirkungen fort), danach steht die Uhr still. Ein Totem
     * kommt zuerst, ohne den vollen Satz hilft nichts.
     */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = AbyssGear.timewardenSet();
        double[] armor = new double[1];
        double[] toughness = new double[1];
        for (int i = 0; i < set.size(); i++) {
            ItemStack stack = new ItemStack(set.get(i).get());
            stack.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(SLOTS[i], (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                } else if (attribute.is(Attributes.ARMOR_TOUGHNESS)) {
                    toughness[0] += modifier.amount();
                }
            });
            helper.assertTrue(stack.has(DataComponents.DAMAGE_RESISTANT), "Verbrennt in Lava: " + stack);
            helper.assertTrue(stack.isValidRepairItem(new ItemStack(AbyssGear.CHRONIT.get())), "Nicht mit Chronit auszubessern: " + stack);
        }
        helper.assertTrue(armor[0] >= 20.0 && toughness[0] >= 12.0, "Schutz wie Netherit fehlt: " + armor[0] + " / " + toughness[0]);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-timewarden", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        try {
            vulnerable(player);
            player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.get(1).get()));
            helper.assertTrue(TimewardenArmor.pieces(player) == 1 && !TimewardenArmor.fullSet(player), "Ein Teil zählt nicht richtig");
            for (int i = 0; i < set.size(); i++) {
                player.setItemSlot(SLOTS[i], new ItemStack(set.get(i).get()));
            }
            helper.assertTrue(TimewardenArmor.fullSet(player), "Der volle Satz zählt nicht");

            // Die Zeit stockt: wer ihn trifft, wird manchmal langsamer (bei vier Teilen in etwa jedem dritten Treffer)
            Zombie zombie = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
            helper.assertTrue(zombie != null, "Kein Zombie");
            zombie.snapTo(helper.absolutePos(new BlockPos(10, 1, 10)), 0.0f, 0.0f);
            zombie.setNoAi(true);
            level.addFreshEntity(zombie);
            TimewardenArmor.stall(level, zombie, player);
            MobEffectInstance slow = zombie.getEffect(MobEffects.SLOWNESS);
            helper.assertTrue(slow != null && slow.getAmplifier() >= 2 && zombie.hasEffect(MobEffects.MINING_FATIGUE), "Die Zeit stockt nicht: " + slow);
            zombie.removeAllEffects();
            int hits = 0;
            while (hits < 60 && !zombie.hasEffect(MobEffects.SLOWNESS)) {
                player.setHealth(player.getMaxHealth());
                ready(player);
                player.hurtServer(level, level.damageSources().mobAttack(zombie), 2.0f);
                hits++;
            }
            helper.assertTrue(zombie.hasEffect(MobEffects.SLOWNESS), "In 60 Treffern stockte die Zeit kein einziges Mal");
            zombie.discard();

            // Zweite Chance: der Weg der letzten drei Sekunden – erst hier (A, gesund), dann dort (B, angeschlagen)
            TimewardenArmor.resetCooldown(player);
            helper.assertTrue(TimewardenArmor.rewindReady(player), "Die Uhr tickt nicht");
            BlockPos a = helper.absolutePos(new BlockPos(3, 1, 3));
            BlockPos b = helper.absolutePos(new BlockPos(11, 1, 9));
            moveTo(player, a);
            player.setHealth(18.0f);
            for (int i = 0; i < 20; i++) {
                TimewardenArmor.remember(player);
            }
            moveTo(player, b);
            player.setHealth(3.0f);
            for (int i = 0; i < 30; i++) {
                TimewardenArmor.remember(player);
            }
            TimewardenArmor.Moment oldest = TimewardenArmor.oldest(player);
            helper.assertTrue(oldest != null && oldest.pos().distanceTo(Vec3.atBottomCenterOf(a)) < 0.6, "Der Weg wird nicht gemerkt: " + oldest);
            player.addEffect(new MobEffectInstance(MobEffects.POISON, 200, 1));
            player.addEffect(new MobEffectInstance(MobEffects.SPEED, 200));
            player.igniteForSeconds(5.0f);
            ready(player);
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isAlive() && !player.isDeadOrDying(), "Die Zeit spult nicht zurück: " + player.getHealth());
            helper.assertTrue(player.position().distanceTo(Vec3.atBottomCenterOf(a)) < 0.6, "Nicht zurück an der alten Stelle: " + player.position());
            helper.assertTrue(Math.abs(player.getHealth() - 18.0f) < 0.01f, "Nicht die Herzen von damals: " + player.getHealth());
            helper.assertFalse(player.hasEffect(MobEffects.POISON) || player.isOnFire(), "Gift oder Feuer bleiben");
            helper.assertTrue(player.hasEffect(MobEffects.SPEED), "Auch die guten Wirkungen sind fort");
            helper.assertTrue(player.isInvulnerable(), "Nach dem Zurückspulen nicht kurz unverwundbar");
            helper.assertFalse(TimewardenArmor.rewindReady(player), "Die Uhr steht danach nicht still");
            helper.assertTrue(TimewardenArmor.oldest(player) == null, "Der alte Weg bleibt gemerkt");

            // war man noch nicht lange genug unterwegs, bleibt man stehen – mit mindestens vier Herzen
            TimewardenArmor.resetCooldown(player);
            moveTo(player, b);
            player.setHealth(2.0f);
            TimewardenArmor.remember(player);
            ready(player);
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isAlive() && player.getHealth() >= TimewardenArmor.MIN_HEALTH - 0.01f,
                    "Weniger als vier Herzen nach dem Zurückspulen: " + player.getHealth());

            // ein Totem kommt zuerst (die Uhr bleibt bereit)
            TimewardenArmor.resetCooldown(player);
            player.setHealth(player.getMaxHealth());
            ready(player);
            player.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.TOTEM_OF_UNDYING));
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isAlive() && player.getOffhandItem().isEmpty(), "Das Totem wirkt nicht zuerst: " + player.getOffhandItem());
            helper.assertTrue(TimewardenArmor.rewindReady(player), "Nach dem Totem steht die Uhr still");

            // ohne den vollen Satz hilft nichts
            player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
            player.removeAllEffects();
            player.setAbsorptionAmount(0.0f);
            player.setHealth(player.getMaxHealth());
            ready(player);
            player.hurtServer(level, level.damageSources().generic(), 10_000.0f);
            helper.assertTrue(player.isDeadOrDying(), "Ohne Helm spult die Zeit trotzdem zurück");
            helper.succeed();
        } finally {
            level.getServer().getPlayerList().remove(player);
        }
    }

    // =====================================================================================
    // Ein eigenes Feld weit draußen (für den Uhrturm)
    // =====================================================================================

    private record Field(ServerLevel level, BlockPos center, List<long[]> chunks) {
        void close() {
            BoundingBox box = ClockTowerPiece.makeBox(this.center);
            for (Entity e : this.level.getEntitiesOfClass(Entity.class, AABB.of(box).inflate(8.0), e -> !(e instanceof Player))) {
                e.discard();
            }
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                if (!this.level.getBlockState(p).isAir()) {
                    this.level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            for (long[] c : this.chunks) {
                this.level.setChunkForced((int) c[0], (int) c[1], false);
            }
        }
    }

    /** Ein Feld für einen Uhrturm: Mitte des Zifferblatts {@code dx}/{@code dz} vom Test entfernt auf Höhe {@code y}, alle Chunks geladen. */
    private static Field field(GameTestHelper helper, int dx, int dz, int y) {
        ServerLevel level = helper.getLevel();
        BlockPos base = helper.absolutePos(BlockPos.ZERO);
        BlockPos center = new BlockPos(base.getX() + dx, y, base.getZ() + dz);
        BoundingBox box = ClockTowerPiece.makeBox(center);
        List<long[]> chunks = new ArrayList<>();
        for (int cx = (box.minX() - 2) >> 4; cx <= (box.maxX() + 2) >> 4; cx++) {
            for (int cz = (box.minZ() - 2) >> 4; cz <= (box.maxZ() + 2) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        return new Field(level, center, chunks);
    }

    private static boolean free(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).getCollisionShape(level, pos).isEmpty();
    }

    // =====================================================================================
    // Der Uhrturm
    // =====================================================================================

    /**
     * Ein Uhrturm im Nichts: der Raum ringsum freigeräumt (weit draußen bleibt alles), das große Zifferblatt ohne Löcher und darüber
     * frei (Platz für den Kampf), in der Nabe das Uhrwerkherz (unzerstörbar, leuchtet, kennt den Platz des Wächters), Brüstung,
     * Brücke und Tür zum Turm, im Turm eine Treppe ohne Lücke bis in die Glockenstube, das Pendel im Kern, die Glocke unter dem
     * Dach, Zifferblätter außen, zwei Truhen, schwebende Zahnräder und der Kegel darunter.
     */
    private static void tower(GameTestHelper helper) {
        Field field = field(helper, 27_000, 35_000, 150);
        ServerLevel level = field.level();
        BlockPos c = field.center();
        try {
            BlockState rock = AbyssBlocks.CHAOS_STONE.base().get().defaultBlockState();
            BlockPos high = c.offset(30, 40, 0);
            BlockPos low = c.offset(0, -20, 30);
            BlockPos beyond = c.offset(47, 0, 0);
            for (BlockPos p : List.of(high, low, beyond)) {
                level.setBlock(p, rock, Block.UPDATE_CLIENTS);
            }
            ClockTowerPiece.build(level, ClockTowerPiece.makeBox(c), c, 4711L, RandomSource.create(11), null);
            helper.assertTrue(level.getBlockState(high).isAir() && level.getBlockState(low).isAir(), "Der Raum um den Uhrturm ist nicht freigeräumt");
            helper.assertTrue(level.getBlockState(beyond).is(rock.getBlock()), "Der Uhrturm räumt zu weit frei");

            // das Uhrwerkherz in der Nabe
            BlockPos heartPos = ClockTowerPiece.heart(c);
            BlockState heart = level.getBlockState(heartPos);
            helper.assertTrue(heart.is(ClockTower.CLOCK_HEART.get()), "Kein Uhrwerkherz in der Nabe: " + heart);
            helper.assertTrue(heart.getDestroySpeed(level, heartPos) < 0, "Das Uhrwerkherz lässt sich abbauen");
            helper.assertTrue(heart.getLightEmission() > 0, "Das Uhrwerkherz leuchtet nicht");
            helper.assertTrue(level.getBlockEntity(heartPos) instanceof ClockHeartBlockEntity be && be.home().equals(c)
                    && be.phase() == ClockHeartBlock.Phase.SLEEPING, "Das Uhrwerkherz kennt den Platz des Wächters nicht");

            // das Zifferblatt: ohne Löcher, Email, Chronit an den Hauptstunden, darüber frei
            int holes = 0;
            int face = 0;
            int chronit = 0;
            int blocked = 0;
            int walls = 0;
            for (int dx = -17; dx <= 17; dx++) {
                for (int dz = -17; dz <= 17; dz++) {
                    double r = Math.hypot(dx, dz);
                    if (r > ClockTowerPiece.DIAL + 0.95) {
                        continue;
                    }
                    if (r > ClockTowerPiece.DIAL) {
                        walls += level.getBlockState(c.offset(dx, 0, dz)).is(AbyssBlocks.CHAOS_STONE_BRICKS.wall().get()) ? 1 : 0;
                        continue;
                    }
                    BlockPos floor = c.offset(dx, -1, dz);
                    BlockState st = level.getBlockState(floor);
                    holes += free(level, floor) ? 1 : 0;
                    face += st.is(ClockTower.CLOCK_FACE.get()) ? 1 : 0;
                    chronit += st.is(AbyssGear.CHRONIT_BLOCK.get()) ? 1 : 0;
                    if (r <= ClockTowerPiece.DIAL - 0.5) {
                        for (int y = 0; y <= 6; y++) {
                            blocked += free(level, c.offset(dx, y, dz)) ? 0 : 1;
                        }
                    }
                }
            }
            KingdomsAtWar.LOGGER.info("Uhrturm: Zifferblatt {} Email, {} Chronit, {} Löcher, {} verbaut, Brüstung {}", face, chronit, holes, blocked, walls);
            helper.assertTrue(holes == 0, "Löcher im Zifferblatt: " + holes);
            helper.assertTrue(face > 300, "Zu wenig Email im Zifferblatt: " + face);
            helper.assertTrue(chronit >= 8, "Die Hauptstunden aus Chronit fehlen: " + chronit);
            helper.assertTrue(blocked == 0, "Über dem Zifferblatt ist es verbaut: " + blocked);
            helper.assertTrue(walls > 20, "Kaum eine Brüstung: " + walls);
            helper.assertFalse(free(level, c.below(10)), "Unter dem Zifferblatt trägt kein Kegel");

            // über die Brücke durch die Tür in den Turm
            BlockPos tower = ClockTowerPiece.tower(c);
            for (int z = c.getZ() - 1; z >= tower.getZ() + ClockTowerPiece.TOWER_HALF - 1; z--) {
                BlockPos floor = new BlockPos(c.getX(), c.getY() - 1, z);
                BlockPos feet = floor.above();
                helper.assertFalse(free(level, floor), "Kein Boden auf dem Weg zum Turm (z " + (z - c.getZ()) + ")");
                helper.assertTrue(level.getBlockState(feet).getCollisionShape(level, feet).max(Direction.Axis.Y) <= 0.5 && free(level, feet.above()),
                        "Der Weg zum Turm ist verbaut (z " + (z - c.getZ()) + ")");
            }
            int rails = 0;
            for (int z = tower.getZ() + ClockTowerPiece.TOWER_HALF + 1; z <= c.getZ() - 16; z++) {
                for (int side : new int[] {-3, 3}) {
                    rails += level.getBlockState(new BlockPos(c.getX() + side, c.getY(), z)).is(ClockTower.CLOCK_BRONZE.wall().get()) ? 1 : 0;
                }
            }
            helper.assertTrue(rails >= 8, "Die Brücke hat kein Geländer: " + rails);

            // die Treppe: auf jeder Höhe bis zur Glockenstube eine Stufe mit Platz darüber
            Block stairs = AbyssBlocks.CHAOS_STONE_BRICKS.stairs().get();
            for (int k = 0; k <= ClockTowerPiece.BELL_ROOM; k++) {
                boolean step = false;
                for (int dx = -4; dx <= 4 && !step; dx++) {
                    for (int dz = -4; dz <= 4 && !step; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) < 3) {
                            continue;
                        }
                        BlockPos p = tower.offset(dx, k, dz);
                        step = level.getBlockState(p).is(stairs) && free(level, p.above()) && free(level, p.above(2));
                    }
                }
                helper.assertTrue(step, "Die Treppe im Turm reißt ab (Höhe +" + k + ")");
            }
            // das Pendel im Kern, die Glocke unter dem Dach, die offenen Bögen der Glockenstube, Zifferblätter außen
            helper.assertTrue(level.getBlockState(tower.above(4)).is(AbyssGear.CHRONIT_BLOCK.get())
                    && level.getBlockState(tower.above(10)).is(Blocks.COPPER_CHAIN.waxed().unaffected()), "Kein Pendel im Kern des Turms");
            int top = ClockTowerPiece.ROOF - 1;
            helper.assertTrue(level.getBlockState(tower.above(top - 1)).is(ClockTower.CLOCK_BRONZE.base().get())
                    && level.getBlockState(tower.above(top - 4)).is(AbyssGear.CHRONIT_BLOCK.get()), "Keine Glocke in der Glockenstube");
            for (Direction d : Direction.Plane.HORIZONTAL) {
                BlockPos arch = tower.relative(d, ClockTowerPiece.TOWER_HALF).above(ClockTowerPiece.BELL_ROOM + 2);
                helper.assertTrue(level.getBlockState(arch).isAir(), "Die Glockenstube ist nach " + d + " nicht offen");
                int enamel = 0;
                BlockPos dial = tower.relative(d, ClockTowerPiece.TOWER_HALF + 1).above(20);
                for (BlockPos p : BlockPos.betweenClosed(dial.offset(-4, -4, -4), dial.offset(4, 4, 4))) {
                    enamel += level.getBlockState(p).is(ClockTower.CLOCK_FACE.get()) ? 1 : 0;
                }
                helper.assertTrue(enamel > 20, "Kein Zifferblatt am Turm nach " + d + ": " + enamel);
            }
            // zwei Truhen (Uhrwerkkammer, Glockenstube) auf festem Boden, zu öffnen
            for (BlockPos chestPos : List.of(ClockTowerPiece.gearRoomChest(c), ClockTowerPiece.bellRoomChest(c))) {
                helper.assertTrue(level.getBlockEntity(chestPos) instanceof ChestBlockEntity chest && ModLootTables.CLOCK_TOWER_CHEST.equals(chest.getLootTable()),
                        "Keine Truhe des Uhrturms bei " + chestPos.subtract(c));
                helper.assertFalse(free(level, chestPos.below()), "Die Truhe schwebt: " + chestPos.subtract(c));
                helper.assertTrue(free(level, chestPos.above()), "Die Truhe lässt sich nicht öffnen: " + chestPos.subtract(c));
            }
            // die schwebenden Zahnräder (fern von Zifferblatt und Turm)
            int gears = 0;
            BoundingBox box = ClockTowerPiece.makeBox(c);
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), c.getY() - 30, box.minZ(), box.maxX(), c.getY() + 30, box.maxZ())) {
                int dx = p.getX() - c.getX();
                int dz = p.getZ() - c.getZ();
                if (Math.hypot(dx, dz) < 18.0 || (Math.abs(dx) <= 10 && Math.abs(p.getZ() - tower.getZ()) <= 10)) {
                    continue;
                }
                BlockState st = level.getBlockState(p);
                gears += st.is(ClockTower.CLOCK_BRONZE.base().get()) || st.is(ClockTower.CLOCKWORK.get()) ? 1 : 0;
            }
            KingdomsAtWar.LOGGER.info("Uhrturm: {} Blöcke schwebender Zahnräder", gears);
            helper.assertTrue(gears > 120, "Kaum schwebende Zahnräder: " + gears);
            helper.succeed();
        } finally {
            field.close();
        }
    }

    // =====================================================================================
    // Die Uhrtürme im Abgrund
    // =====================================================================================

    /**
     * Der Uhrturm ist angemeldet und steht in jedem Biom des Abgrunds (nicht anderswo), gleichmäßig verteilt (fern des Schlunds der
     * Welten); in einem weiten Gebiet passt (fast) jeder Kandidat, auf verschiedenen Höhen. Mit einem eigenen Erzeuger des Abgrunds,
     * wenn es den Abgrund nicht gibt.
     */
    private static void world(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var registries = server.registryAccess();
        var structure = registries.lookupOrThrow(Registries.STRUCTURE).get(ModStructures.CLOCK_TOWER_STRUCTURE);
        helper.assertTrue(structure.isPresent(), "Der Uhrturm ist nicht angemeldet");
        var tagged = registries.lookupOrThrow(Registries.BIOME).getOrThrow(ModTags.Biomes.HAS_CLOCK_TOWER);
        for (ResourceKey<Biome> key : AbyssBiomes.ALL) {
            helper.assertTrue(structure.get().value().biomes().stream().anyMatch(h -> h.is(key)), "Kein Uhrturm im Biom " + key.identifier());
        }
        helper.assertFalse(structure.get().value().biomes().stream().anyMatch(h -> h.is(Biomes.PLAINS)), "Uhrtürme auch in der Oberwelt");
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(ModStructures.CLOCK_TOWERS);
        helper.assertTrue(set.value().placement() instanceof RandomSpreadStructurePlacement, "Die Uhrtürme sind nicht gleichmäßig verteilt");
        var placement = (RandomSpreadStructurePlacement) set.value().placement();
        helper.assertTrue(placement.spacing() >= 20 && placement.separation() >= 8, "Die Uhrtürme stehen zu dicht: " + placement.spacing());

        // Splitter, Nähte, Kristalle und Pflanzen des Abgrunds halten Abstand (sonst wüchsen die der Nachbarchunks ins Auge hinein)
        var placed = registries.lookupOrThrow(Registries.PLACED_FEATURE);
        for (String name : List.of("shards_lands", "shards_ember", "shards_star", "shards_shadow", "shards_seam", "shards_great", "world_seams",
                "float_crystals", "lift_crystals", "oaks", "shadow_trees", "chorus", "crimson_fungi")) {
            var feature = placed.getOrThrow(ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("abyss/" + name)));
            helper.assertTrue(feature.value().placement().stream().anyMatch(m -> m instanceof ClockTowerClearing.Filter),
                    "Wächst in das Auge des Uhrturms: " + name);
        }

        Holder<NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS).getOrThrow(AbyssTerrain.SETTINGS);
        ServerLevel abyss = server.getLevel(AbyssGate.THE_ABYSS);
        ChunkGenerator generator;
        RandomState random;
        long seed;
        if (abyss != null) {
            generator = abyss.getChunkSource().getGenerator();
            random = abyss.getChunkSource().randomState();
            seed = abyss.getSeed();
            helper.assertFalse(abyss.getChunkSource().getGeneratorState().getPlacementsForStructure(structure.get()).isEmpty(),
                    "Im Abgrund entstehen keine Uhrtürme");
        } else {
            generator = new NoiseBasedChunkGenerator(MultiNoiseBiomeSource.createFromList(AbyssBiomes.parameters(registries.lookupOrThrow(Registries.BIOME))),
                    settings);
            seed = 20_261_112L;
            random = RandomState.create(registries.lookupOrThrow(Registries.NOISE), seed, settings.value());
        }
        BiomeResolver biomes = generator.getBiomeSource().createUncachedResolver(random);
        int cells = 0;
        int sites = 0;
        java.util.Set<Integer> heights = new java.util.TreeSet<>();
        for (int i = -6; i < 6; i++) {
            for (int j = -6; j < 6; j++) {
                ChunkPos chunk = placement.getPotentialStructureChunk(seed, i * placement.spacing(), j * placement.spacing());
                cells++;
                BlockPos site = ClockTowerStructure.site(biomes, tagged::contains, chunk, RandomSource.create(seed + i * 31L + j).nextLong());
                if (site != null) {
                    sites++;
                    heights.add(site.getY());
                    helper.assertTrue(site.getX() == chunk.getMiddleBlockX() && site.getZ() == chunk.getMiddleBlockZ(), "Der Uhrturm steht nicht in der Mitte seines Chunks");
                }
            }
        }
        KingdomsAtWar.LOGGER.info("Uhrtürme: {} Felder, {} passend, Höhen {}", cells, sites, heights);
        helper.assertTrue(sites * 10 >= cells * 9, "Zu selten ein Platz für einen Uhrturm: " + sites + " von " + cells);
        helper.assertTrue(heights.size() >= 3, "Die Uhrtürme stehen immer auf derselben Höhe: " + heights);
        for (int y : heights) {
            helper.assertTrue(java.util.Arrays.stream(ClockTowerStructure.HEIGHTS).anyMatch(h -> h == y), "Unerwartete Höhe: " + y);
        }
        Holder<Biome> plains = registries.lookupOrThrow(Registries.BIOME).getOrThrow(Biomes.PLAINS);
        helper.assertTrue(ClockTowerStructure.site((x, y, z) -> plains, tagged::contains, new ChunkPos(3, 4), 7L) == null,
                "Ein Uhrturm in den Ebenen");
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
     * Auf dem Zifferblatt: Der Wächter steht erstarrt über dem Herzen (ein Schlag weckt ihn, unverwundbar), er erwacht (auch dabei
     * unverwundbar). Dann seine Angriffe gegen einen Ritter: Zeigerhieb, Stundenschlag, Zahnradwurf, Zeitstopp (wer ihn ansieht,
     * erstarrt – wer wegschaut, nicht; mit dem vollen Satz der Zeitwächter nur kurz), Pendelschwung, Sanduhr, Zeitsprung, Rückspulen
     * (ungestört heilt er, hart getroffen taumelt er und nimmt mehr Schaden); fortgelockt springt er zurück; unter halber Kraft die
     * Zwölfte Stunde. Besiegt lässt er den Minutenzeiger fallen, das Herz bleibt stehen; Chronit ruft einen neuen; zurückgesetzt
     * steht beim nächsten Besuch wieder einer erstarrt da, und wer ihm zu nahe kommt, weckt ihn.
     */
    private static void fight(GameTestHelper helper) {
        Field field = field(helper, -35_000, 31_000, 140);
        ServerLevel level = field.level();
        BlockPos c = field.center();
        ClockTowerPiece.build(level, ClockTowerPiece.makeBox(c), c, 77L, RandomSource.create(5), null);
        BlockPos heartPos = ClockTowerPiece.heart(c);
        if (!(level.getBlockEntity(heartPos) instanceof ClockHeartBlockEntity heart)) {
            field.close();
            helper.fail("Das Uhrwerkherz hat keine Blockentität");
            return;
        }
        HourWardenEntity warden = heart.spawn(level, false);
        if (warden == null) {
            field.close();
            helper.fail("Kein Wächter über dem Herzen");
            return;
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-clock-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        vulnerable(player);
        moveTo(player, c.offset(0, 0, -19));
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(warden.isDormant(), "Der Wächter steht nicht erstarrt: " + warden.currentAttack());
            helper.assertTrue(warden.position().distanceTo(Vec3.atBottomCenterOf(c)) < 0.6, "Er steht nicht über dem Herzen: " + warden.position());
            helper.assertTrue(heart.owns(warden) && c.equals(warden.home()) && heartPos.equals(warden.heart()), "Das Herz kennt seinen Wächter nicht");
            float hp = warden.getHealth();
            warden.hurtServer(level, level.damageSources().playerAttack(player), 40.0f);
            helper.assertTrue(warden.getHealth() >= hp, "Erstarrt ist er verwundbar");
            helper.assertTrue(warden.currentAttack() == HourWardenEntity.Attack.AWAKEN, "Ein Schlag weckt ihn nicht: " + warden.currentAttack());
            warden.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(warden.getHealth() >= hp, "Beim Erwachen ist er verwundbar");
            level.getServer().getPlayerList().remove(player);
            helper.runAfterDelay(HourWardenEntity.Attack.AWAKEN.duration + 6, () -> {
                helper.assertTrue(warden.currentAttack() != HourWardenEntity.Attack.AWAKEN, "Das Erwachen endet nicht");
                helper.assertTrue(level.getBlockState(heartPos).getValue(ClockHeartBlock.PHASE) == ClockHeartBlock.Phase.AWAKE,
                        "Das Herz wacht nicht mit ihm");
                attacks(helper, field, heart, warden);
            });
        });
    }

    private static void attacks(GameTestHelper helper, Field field, ClockHeartBlockEntity heart, HourWardenEntity warden) {
        ServerLevel level = field.level();
        BlockPos c = field.center();
        var knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
        if (knight == null) {
            field.close();
            helper.fail("Kein Ritter");
            return;
        }
        knight.goalSelector.removeAllGoals(goal -> true);
        knight.targetSelector.removeAllGoals(goal -> true);
        knight.setPersistenceRequired();
        knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
        knight.getAttribute(Attributes.KNOCKBACK_RESISTANCE).setBaseValue(0.0);
        knight.setHealth(1000.0f);
        Vec3 spot = Vec3.atBottomCenterOf(c).add(-5.0, 0.0, 0.0);
        knight.snapTo(spot.x + 4.0, spot.y, spot.z, 0.0f, 0.0f);
        level.addFreshEntity(knight);
        AABB around = AABB.of(ClockTowerPiece.makeBox(c));
        float[] hp = {0.0f};
        float[] health = {0.0f};
        Vec3[] held = {Vec3.ZERO};
        // vor jedem Angriff: Zahnräder und Pendel fort, der Ritter {@code ahead} Blöcke östlich des Wächters, geheilt, er blickt ihn an
        Consumer<Double> place = ahead -> {
            level.getEntitiesOfClass(ClockGearEntity.class, around).forEach(Entity::discard);
            level.getEntitiesOfClass(ClockPendulumEntity.class, around).forEach(Entity::discard);
            knight.removeAllEffects();
            knight.setHealth(knight.getMaxHealth());
            knight.damageCooldownTime = 0;
            knight.setInvulnerableTime(0);
            knight.hurtTime = 0;
            knight.resetFallDistance();
            knight.setTarget(null);
            knight.teleportTo(spot.x + ahead, spot.y, spot.z);
            knight.setYRot(0.0f);
            knight.setYHeadRot(0.0f);
            knight.setYBodyRot(0.0f);
            knight.setDeltaMovement(Vec3.ZERO);
            warden.snapTo(spot.x, spot.y, spot.z, -90.0f, 0.0f);
            warden.setYBodyRot(-90.0f);
            warden.setYHeadRot(-90.0f);
            warden.setDeltaMovement(Vec3.ZERO);
            warden.setTarget(knight);
            hp[0] = knight.getHealth();
        };
        ServerPlayer[] players = new ServerPlayer[2];
        List<Step> steps = List.of(
                // Zeigerhieb und Stundenschlag
                new Step(16, () -> {
                    place.accept(3.0);
                    warden.forceAttack(HourWardenEntity.Attack.SWEEP);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Zeigerhieb trifft den Ritter nicht")),
                new Step(24, () -> {
                    place.accept(3.0);
                    warden.forceAttack(HourWardenEntity.Attack.STRIKE);
                }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Der Stundenschlag trifft den Ritter nicht")),
                // Zahnradwurf
                new Step(18, () -> {
                    place.accept(8.0);
                    warden.forceAttack(HourWardenEntity.Attack.GEARS);
                }, () -> helper.assertTrue(!level.getEntitiesOfClass(ClockGearEntity.class, around, g -> g.owner() == warden).isEmpty(),
                        "Er wirft keine Zahnräder")),
                new Step(30, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Kein Zahnrad trifft den Ritter")),
                // Zeitstopp: der Ritter, wer ihn ansieht, und wer wegschaut
                new Step(HourWardenEntity.STOP_AT + 2, () -> {
                    place.accept(6.0);
                    players[0] = MageGameTests.survivalPlayer(helper, "test-clock-watcher", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
                    players[1] = MageGameTests.survivalPlayer(helper, "test-clock-away", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
                    for (ServerPlayer p : players) {
                        vulnerable(p);
                    }
                    players[0].teleportTo(level, spot.x, spot.y, spot.z + 8.0, java.util.Set.of(), 180.0f, -8.0f, true);
                    players[1].teleportTo(level, spot.x, spot.y, spot.z - 8.0, java.util.Set.of(), 180.0f, 0.0f, true);
                    for (ServerPlayer p : players) {
                        level.getChunkSource().move(p);
                    }
                    warden.forceAttack(HourWardenEntity.Attack.TIME_STOP);
                }, () -> {
                    helper.assertTrue(warden.isFrozen(knight), "Der Ritter erstarrt nicht");
                    helper.assertTrue(warden.isFrozen(players[0]), "Wer ihn ansieht, erstarrt nicht");
                    helper.assertFalse(warden.isFrozen(players[1]), "Wer wegschaut, erstarrt trotzdem");
                    MobEffectInstance slow = players[0].getEffect(MobEffects.SLOWNESS);
                    helper.assertTrue(slow != null && slow.getAmplifier() >= 9, "Erstarrt ist man nicht langsam: " + slow);
                    held[0] = players[0].position();
                    players[0].teleportTo(level, held[0].x + 3.0, held[0].y, held[0].z, java.util.Set.of(), 180.0f, 0.0f, true);
                }),
                new Step(2, () -> { }, () -> helper.assertTrue(players[0].position().distanceTo(held[0]) < 0.3,
                        "Erstarrt kann man fortgehen: " + players[0].position().distanceTo(held[0]))),
                new Step(18, () -> { }, () -> {
                    helper.assertTrue(warden.distanceTo(knight) < 4.0, "Er tritt nicht hinter den Erstarrten: " + warden.distanceTo(knight));
                    helper.assertTrue(knight.getHealth() < hp[0], "Er schlägt den Erstarrten nicht");
                    // mit dem vollen Satz der Zeitwächter erstarrt man nur kurz
                    var set = AbyssGear.timewardenSet();
                    for (int i = 0; i < set.size(); i++) {
                        players[1].setItemSlot(SLOTS[i], new ItemStack(set.get(i).get()));
                    }
                    warden.freezeEntity(level, players[1]);
                    MobEffectInstance slow = players[1].getEffect(MobEffects.SLOWNESS);
                    helper.assertTrue(slow != null && slow.getDuration() <= HourWardenEntity.FREEZE_TICKS_WARDEN,
                            "Die Rüstung der Zeitwächter verkürzt den Zeitstopp nicht: " + slow);
                    for (ServerPlayer p : players) {
                        level.getServer().getPlayerList().remove(p);
                    }
                }),
                new Step(36, () -> { }, () -> helper.assertFalse(warden.isFrozen(knight), "Die Zeit läuft nicht weiter")),
                // Pendelschwung
                new Step(12, () -> {
                    place.accept(7.0);
                    warden.forceAttack(HourWardenEntity.Attack.PENDULUM);
                }, () -> {
                    var pendulums = level.getEntitiesOfClass(ClockPendulumEntity.class, around, p -> p.owner() == warden);
                    helper.assertTrue(pendulums.size() == 1, "Kein Pendel: " + pendulums.size());
                    ClockPendulumEntity pendulum = pendulums.getFirst();
                    helper.assertTrue(Math.hypot(pendulum.getX() - knight.getX(), pendulum.getZ() - knight.getZ()) < 1.0 && pendulum.getY() > knight.getY() + 10.0,
                            "Das Pendel hängt nicht über dem Ritter: " + pendulum.position().subtract(knight.position()));
                }),
                new Step(56, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Das Pendel trifft den Ritter nicht")),
                // Sanduhr: um den Ritter (der ihn angreift) rinnt Sand, er wird langsam, dann birst sie
                new Step(20, () -> {
                    place.accept(5.0);
                    knight.setTarget(warden);
                    warden.forceAttack(HourWardenEntity.Attack.HOURGLASS);
                }, () -> {
                    var glasses = warden.hourglasses();
                    helper.assertTrue(glasses.size() >= 2, "Zu wenig Sanduhren: " + glasses.size());
                    helper.assertTrue(glasses.stream().anyMatch(g -> Math.hypot(g.center().x - knight.getX(), g.center().z - knight.getZ()) < 1.0),
                            "Keine Sanduhr um den Ritter");
                }),
                new Step(20, () -> { }, () -> {
                    helper.assertTrue(knight.hasEffect(MobEffects.SLOWNESS), "In der Sanduhr wird der Ritter nicht langsam");
                    knight.setTarget(null);
                    warden.setTarget(null);
                    hp[0] = knight.getHealth();
                }),
                new Step(80, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Die Sanduhr birst nicht")),
                // Zeitsprung: hinter den Ritter, dann ein Hieb
                new Step(8, () -> {
                    place.accept(6.0);
                    warden.forceAttack(HourWardenEntity.Attack.BLINK);
                }, () -> helper.assertTrue(warden.distanceTo(knight) < 3.6, "Der Zeitsprung trägt ihn nicht hinter den Ritter: " + warden.distanceTo(knight))),
                new Step(10, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Nach dem Zeitsprung schlägt er nicht zu")),
                // Rückspulen: ungestört heilt er …
                new Step(76, () -> {
                    place.accept(12.0);
                    warden.setHealth(warden.getMaxHealth() * 0.6f);
                    health[0] = warden.getHealth();
                    warden.forceAttack(HourWardenEntity.Attack.REWIND);
                }, () -> {
                    helper.assertTrue(warden.getHealth() > health[0] + warden.getMaxHealth() * 0.1f,
                            "Das Rückspulen heilt ihn nicht: " + health[0] + " -> " + warden.getHealth());
                    helper.assertTrue(warden.rewinds() == 1, "Das Rückspulen zählt nicht: " + warden.rewinds());
                }),
                // … hart getroffen taumelt er und nimmt mehr Schaden
                new Step(14, () -> {
                    place.accept(12.0);
                    warden.setHealth(warden.getMaxHealth() * 0.64f);
                    warden.forceAttack(HourWardenEntity.Attack.REWIND);
                }, () -> {
                    // Treffer zählen bei ihm weniger (seine Lebenskraft übersteigt die Grenze von Minecraft)
                    warden.damageCooldownTime = 0;
                    warden.hurtServer(level, level.damageSources().magic(),
                            warden.getMaxHealth() * HourWardenEntity.REWIND_BREAK * 1.6f / BossHealth.scale(warden));
                    helper.assertTrue(warden.currentAttack() == HourWardenEntity.Attack.STAGGER, "Getroffen spult er weiter zurück: " + warden.currentAttack());
                }),
                new Step(2, () -> { }, () -> {
                    warden.damageCooldownTime = 0;
                    float before = warden.getHealth();
                    warden.hurtServer(level, level.damageSources().magic(), 40.0f);
                    float taken = (before - warden.getHealth()) / BossHealth.scale(warden);
                    helper.assertTrue(Math.abs(taken - 50.0f) < 0.5f, "Taumelnd nimmt er nicht mehr Schaden: " + taken);
                }),
                // fortgelockt springt er zurück auf die Nabe
                new Step(14, () -> {
                    place.accept(6.0);
                    warden.teleportTo(c.getX() + 26.5, c.getY() + 1.0, c.getZ() + 0.5);
                }, () -> helper.assertTrue(warden.position().distanceTo(Vec3.atBottomCenterOf(c)) < 2.0,
                        "Fortgelockt kehrt er nicht auf das Zifferblatt zurück: " + warden.position().subtract(Vec3.atBottomCenterOf(c)))),
                // unter halber Kraft: die Zwölfte Stunde, dann die Glockenschläge
                new Step(4, () -> {
                    place.accept(6.0);
                    warden.forceAttack(HourWardenEntity.Attack.NONE);
                    warden.setHealth(warden.getMaxHealth() * 0.45f);
                }, () -> helper.assertTrue(warden.currentAttack() == HourWardenEntity.Attack.FURY,
                        "Unter halber Kraft schlägt nicht die Zwölfte Stunde: " + warden.currentAttack() + " (" + warden.getHealth() + "/"
                                + warden.getMaxHealth() + ")")),
                new Step(HourWardenEntity.Attack.FURY.duration + 2, () -> { }, () -> {
                    helper.assertTrue(warden.isEnraged(), "Nach der Zwölften Stunde ist er nicht im Zorn");
                    helper.assertTrue(warden.currentAttack() == HourWardenEntity.Attack.TOLLS, "Die Glocke schlägt nicht: " + warden.currentAttack());
                    knight.setHealth(knight.getMaxHealth());
                    hp[0] = knight.getHealth();
                }),
                new Step(50, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Kein Glockenschlag trifft den Ritter")));
        run(helper, steps, 0, () -> {
            knight.discard();
            death(helper, field, heart, warden);
        });
    }

    /**
     * Tod: ein Herzkristall und Chronit (der Minutenzeiger nur selten), das Herz bleibt stehen (und erlischt); Chronit ruft einen neuen Wächter;
     * zurückgesetzt steht beim nächsten Besuch wieder einer erstarrt da – wer ihm zu nahe kommt, weckt ihn.
     */
    private static void death(GameTestHelper helper, Field field, ClockHeartBlockEntity heart, HourWardenEntity warden) {
        ServerLevel level = field.level();
        BlockPos c = field.center();
        BlockPos heartPos = ClockTowerPiece.heart(c);
        AABB around = AABB.of(ClockTowerPiece.makeBox(c));
        warden.forceAttack(HourWardenEntity.Attack.NONE);
        BossLootGameTests.firstVictory(helper, warden, Float.MAX_VALUE);
        helper.assertTrue(warden.isDeadOrDying(), "Der Wächter stirbt nicht");
        helper.runAfterDelay(HourWardenEntity.DEATH_TICKS + 6, () -> {
            helper.assertTrue(warden.isRemoved(), "Der Wächter zerspringt nicht");
            // (seit 1.2 fällt der Herzkristall nur selten – das prüft die Beute-Statistik)
            for (var drop : List.of(AbyssGear.CHRONIT.get())) {
                helper.assertFalse(level.getEntitiesOfClass(ItemEntity.class, around, e -> e.getItem().is(drop)).isEmpty(), "Er lässt nicht fallen: " + drop);
            }
            BlockState stopped = level.getBlockState(heartPos);
            helper.assertTrue(stopped.getValue(ClockHeartBlock.PHASE) == ClockHeartBlock.Phase.DEFEATED, "Das Herz bleibt nicht stehen");
            helper.assertTrue(stopped.getLightEmission() == 0, "Das stillstehende Herz leuchtet noch");
            helper.assertTrue(heart.recall(level, null), "Chronit ruft keinen neuen Wächter");
            HourWardenEntity next = heart.warden(level);
            helper.assertTrue(next != null && next.currentAttack() == HourWardenEntity.Attack.AWAKEN, "Der gerufene Wächter erwacht nicht");
            heart.reset(level);
            helper.assertTrue(heart.warden(level) == null, "Zurückgesetzt bleibt der Wächter");
            helper.assertTrue(level.getBlockState(heartPos).getValue(ClockHeartBlock.PHASE) == ClockHeartBlock.Phase.SLEEPING, "Zurückgesetzt ruht das Herz nicht");
            // beim nächsten Besuch: ein neuer Wächter, erstarrt – wer näher kommt, weckt ihn
            ServerPlayer visitor = MageGameTests.survivalPlayer(helper, "test-clock-return", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
            vulnerable(visitor);
            moveTo(visitor, c.offset(0, 0, -19));
            helper.runAfterDelay(80, () -> {
                HourWardenEntity again = heart.warden(level);
                helper.assertTrue(again != null && again.isDormant(), "Beim nächsten Besuch steht kein Wächter erstarrt da: " + again);
                moveTo(visitor, c.offset(0, 0, -9));
                helper.runAfterDelay(14, () -> {
                    helper.assertTrue(again.currentAttack() == HourWardenEntity.Attack.AWAKEN, "Wer ihm nahe kommt, weckt ihn nicht: " + again.currentAttack());
                    level.getServer().getPlayerList().remove(visitor);
                    field.close();
                    helper.succeed();
                });
            });
        });
    }

    // =====================================================================================
    // Der Minutenzeiger
    // =====================================================================================

    /**
     * Der Minutenzeiger ist ein Schwert (stärker als Netherit, verbrennt nicht, mit Chronit auszubessern); jeder zwölfte Treffer
     * schlägt die Stunde (Schaden und Stocken ringsum, nicht für den Spieler, nicht zu weit); Rechtsklick: Zeitsprung bis zu acht
     * Blöcke, vor einer Wand hält er an, danach ruht der Zeiger.
     */
    private static void weapon(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack hand = new ItemStack(ClockTower.MINUTE_HAND.get());
        helper.assertTrue(hand.is(ItemTags.SWORDS), "Der Minutenzeiger ist kein Schwert");
        helper.assertTrue(hand.has(DataComponents.DAMAGE_RESISTANT), "Der Minutenzeiger verbrennt in Lava");
        helper.assertTrue(hand.isValidRepairItem(new ItemStack(AbyssGear.CHRONIT.get())), "Chronit bessert den Minutenzeiger nicht aus");
        double[] damage = {1.0};
        hand.getOrDefault(DataComponents.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.EMPTY).forEach(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            if (attribute.is(Attributes.ATTACK_DAMAGE)) {
                damage[0] += modifier.amount();
            }
        });
        helper.assertTrue(damage[0] > 8.0, "Der Minutenzeiger schlägt nicht stärker als ein Netheritschwert: " + damage[0]);
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-minute-hand", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        List<Zombie> zombies = new ArrayList<>();
        try {
            vulnerable(player);
            player.setItemInHand(InteractionHand.MAIN_HAND, hand);
            moveTo(player, helper.absolutePos(new BlockPos(1, 1, 1)));
            for (BlockPos at : List.of(new BlockPos(4, 1, 4), new BlockPos(6, 1, 4), new BlockPos(8, 1, 8))) {
                Zombie zombie = ModEntities.ZOMBIE.get().create(level, EntitySpawnReason.EVENT);
                helper.assertTrue(zombie != null, "Kein Zombie");
                zombie.snapTo(Vec3.atBottomCenterOf(helper.absolutePos(at)), 0.0f, 0.0f);
                zombie.setNoAi(true);
                level.addFreshEntity(zombie);
                zombies.add(zombie);
            }
            Zombie target = zombies.get(0);
            Zombie bystander = zombies.get(1);
            Zombie far = zombies.get(2);
            MinuteHandItem item = (MinuteHandItem) hand.getItem();
            for (int i = 1; i < MinuteHandItem.MINUTES; i++) {
                item.hurtEnemy(hand, target, player);
                helper.assertTrue(MinuteHandItem.minutes(player) == i, "Der Zeiger rückt nicht vor: " + MinuteHandItem.minutes(player) + " statt " + i);
            }
            helper.assertTrue(bystander.getHealth() >= bystander.getMaxHealth(), "Die Stunde schlägt zu früh");
            float health = player.getHealth();
            item.hurtEnemy(hand, target, player);
            helper.assertTrue(MinuteHandItem.minutes(player) == 0, "Nach der vollen Stunde beginnt der Zeiger nicht von vorn");
            helper.assertTrue(bystander.getHealth() < bystander.getMaxHealth() && bystander.hasEffect(MobEffects.SLOWNESS),
                    "Die volle Stunde trifft die Umstehenden nicht");
            helper.assertTrue(far.getHealth() >= far.getMaxHealth(), "Die volle Stunde reicht zu weit");
            helper.assertTrue(player.getHealth() >= health && !player.hasEffect(MobEffects.SLOWNESS), "Die volle Stunde trifft den Spieler selbst");

            // Zeitsprung: acht Blöcke nach Osten, danach ruht der Zeiger
            zombies.forEach(Entity::discard);
            BlockPos start = helper.absolutePos(new BlockPos(0, 1, 4));
            player.teleportTo(level, start.getX() + 0.5, start.getY(), start.getZ() + 0.5, java.util.Set.of(), -90.0f, 0.0f, true);
            double x0 = player.getX();
            hand.use(level, player, InteractionHand.MAIN_HAND);
            helper.assertTrue(player.getX() - x0 > 6.0 && Math.abs(player.getZ() - (start.getZ() + 0.5)) < 0.1,
                    "Der Zeitsprung trägt nicht weit genug: " + (player.getX() - x0));
            helper.assertTrue(player.getCooldowns().isOnCooldown(hand), "Nach dem Zeitsprung ruht der Zeiger nicht");
            helper.assertTrue(hand.getDamageValue() == 1, "Der Zeitsprung nutzt den Zeiger nicht ab: " + hand.getDamageValue());
            // vor einer Wand hält er an
            for (int y = 1; y <= 3; y++) {
                for (int z = 3; z <= 5; z++) {
                    level.setBlock(helper.absolutePos(new BlockPos(5, y, z)), Blocks.STONE.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            player.teleportTo(level, start.getX() + 0.5, start.getY(), start.getZ() + 0.5, java.util.Set.of(), -90.0f, 0.0f, true);
            hand.use(level, player, InteractionHand.MAIN_HAND);
            double wall = helper.absolutePos(new BlockPos(5, 1, 4)).getX();
            helper.assertTrue(player.getX() - x0 > 2.0 && player.getX() < wall - 0.29, "Der Zeitsprung hält nicht vor der Wand: " + (player.getX() - wall));
            helper.succeed();
        } finally {
            zombies.forEach(Entity::discard);
            level.getServer().getPlayerList().remove(player);
        }
    }

    /** Ein Testspieler, der Schaden nimmt (im Spiel angekommen, nicht unverwundbar, keine Kreativkräfte). */
    private static void vulnerable(ServerPlayer player) {
        player.connection.markClientLoaded();
        player.getAbilities().invulnerable = false;
        player.getAbilities().instabuild = false;
        player.setData(io.github.sirschrolli.kingdomsatwar.registry.ModAttachments.HERO_IMMORTAL, -1L);
        ready(player);
    }

    /** Keine Schonzeit nach einem Treffer. */
    private static void ready(ServerPlayer player) {
        player.damageCooldownTime = 0;
        player.setInvulnerableTime(0);
    }

    private static void moveTo(ServerPlayer player, BlockPos pos) {
        player.teleportTo(player.level(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        player.level().getChunkSource().move(player);
    }
}
