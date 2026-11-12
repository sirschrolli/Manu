package io.github.sirschrolli.kingdomsatwar.gametest;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.voidrealm.StarwalkerArmor;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.feature.StardustCraterFeature;
import java.util.List;
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
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.event.RegisterGameTestsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Tests zu 0.99 in der Leere: Himmelseisen (Erz, Beute, Adern im Leerenstein, Kern der gefallenen Sterne), die Rüstung der
 * Sternwandler (Schutz, Schwerkraft, Sternenschweben, Sternenanker), das Sternennetz (wie es gebaut ist und dass es in der Leere
 * entsteht), die Sternenweberin (Andacht, Erwachen, alle Angriffe, Zorn, Tod, Rückruf) und der Sternenbogen.
 */
final class StarWeaverGameTests {
    private static final Identifier ARENA = KingdomsAtWar.id("test_arena");

    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> SKY_IRON =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_sky_iron", () -> StarWeaverGameTests::skyIron);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> ARMOR =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_armor", () -> StarWeaverGameTests::armor);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WEB =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_web", () -> StarWeaverGameTests::web);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> WORLD =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_world", () -> StarWeaverGameTests::world);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> FIGHT =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_fight", () -> StarWeaverGameTests::fight);
    static final DeferredHolder<Consumer<GameTestHelper>, Consumer<GameTestHelper>> BOW =
            ModGameTests.TEST_FUNCTIONS.register("star_weaver_bow", () -> StarWeaverGameTests::bow);

    private StarWeaverGameTests() {}

    /** Lädt die Klasse (meldet die Testfunktionen an). */
    static void init() {}

    static void register(RegisterGameTestsEvent event, Holder<TestEnvironmentDefinition<?>> environment) {
        for (var test : List.of(SKY_IRON, ARMOR, WEB, WORLD, BOW)) {
            event.registerTest(test.getId(), new FunctionGameTestInstance(test.key(),
                    new TestData<>(environment, Level.OVERWORLD, ARENA, 400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
        }
        event.registerTest(FIGHT.getId(), new FunctionGameTestInstance(FIGHT.key(),
                new TestData<>(environment, Level.OVERWORLD, ARENA, 2400, 0, true, Rotation.NONE, false, 1, 1, true, 0)));
    }

    // =====================================================================================
    // Himmelseisen
    // =====================================================================================

    /** Das Erz gibt rohes Himmelseisen, wächst in allen Biomen der Leere als Ader im Leerenstein und im Kern gefallener Sterne. */
    private static void skyIron(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos center = helper.absolutePos(new BlockPos(7, 6, 7));
        List<ItemStack> drops = Block.getDrops(VoidGear.SKY_IRON_ORE.get().defaultBlockState(), level, center, null, null,
                new ItemStack(Items.NETHERITE_PICKAXE));
        helper.assertTrue(drops.stream().anyMatch(s -> s.is(VoidGear.RAW_SKY_IRON.get())), "Das Erz gibt kein rohes Himmelseisen: " + drops);
        helper.assertTrue(VoidGear.SKY_IRON_ORE.get().defaultBlockState().getLightEmission() > 0, "Das Erz schimmert nicht");

        // in jedem Biom der Leere
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME);
        var placedKey = ResourceKey.create(Registries.PLACED_FEATURE, KingdomsAtWar.id("void/ore_sky_iron"));
        for (var key : VoidBiomes.ALL) {
            boolean found = false;
            for (var step : biomes.getOrThrow(key).value().getGenerationSettings().features()) {
                for (var placed : step) {
                    found |= placed.is(placedKey);
                }
            }
            helper.assertTrue(found, "Kein Himmelseisen im Biom " + key.identifier());
        }

        // eine Ader im Leerenstein
        var feature = level.registryAccess().lookupOrThrow(Registries.FEATURE)
                .getOrThrow(ResourceKey.create(Registries.FEATURE, KingdomsAtWar.id("void/ore_sky_iron"))).value();
        var generator = level.getChunkSource().getGenerator();
        Block stone = VoidBlocks.VOID_STONE.base().get();
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-3, -3, -3), center.offset(3, 3, 3))) {
            level.setBlock(p, stone.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        int grown = 0;
        for (int seed = 0; seed < 6 && grown == 0; seed++) {
            feature.place(level, generator, RandomSource.create(seed), center);
            grown = count(level, center, 3, VoidGear.SKY_IRON_ORE.get());
        }
        helper.assertTrue(grown > 0, "Keine Himmelseisenader im Leerenstein");
        clear(level, center, 3);

        // ein Sternenkrater: im gefallenen Stern steckt Himmelseisen
        BlockPos floor = helper.absolutePos(new BlockPos(7, 2, 7));
        for (BlockPos p : BlockPos.betweenClosed(floor.offset(-7, -3, -7), floor.offset(7, 0, 7))) {
            level.setBlock(p, (p.getY() == floor.getY() ? VoidBlocks.STARDUST.get() : stone).defaultBlockState(), Block.UPDATE_CLIENTS);
        }
        int core = 0;
        for (int seed = 0; seed < 8 && core == 0; seed++) {
            new StardustCraterFeature().place(level, generator, RandomSource.create(seed), floor.above());
            core = count(level, floor, 7, VoidGear.SKY_IRON_ORE.get());
        }
        helper.assertTrue(core > 0, "Im gefallenen Stern steckt kein Himmelseisen");
        clear(level, floor, 7);
        helper.succeed();
    }

    private static int count(ServerLevel level, BlockPos center, int r, Block block) {
        int n = 0;
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            n += level.getBlockState(p).is(block) ? 1 : 0;
        }
        return n;
    }

    private static void clear(ServerLevel level, BlockPos center, int r) {
        for (BlockPos p : BlockPos.betweenClosed(center.offset(-r, -r, -r), center.offset(r, r, r))) {
            level.setBlock(p, Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // =====================================================================================
    // Die Rüstung der Sternwandler
    // =====================================================================================

    /** Schutz, leichter mit jedem Teil, weiche Landung; Sternenschweben (nur mit vollem Satz) und Sternenanker mit Abklingzeit. */
    private static void armor(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        var set = VoidGear.starwalkerSet();
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        double[] armor = new double[1];
        double[] fall = new double[1];
        for (int i = 0; i < set.size(); i++) {
            var modifiers = new ItemStack(set.get(i).get()).getOrDefault(net.minecraft.core.component.DataComponents.ATTRIBUTE_MODIFIERS,
                    net.minecraft.world.item.component.ItemAttributeModifiers.EMPTY);
            modifiers.forEach(slots[i], (attribute, modifier) -> {
                if (attribute.is(Attributes.ARMOR)) {
                    armor[0] += modifier.amount();
                } else if (attribute.is(Attributes.SAFE_FALL_DISTANCE)) {
                    fall[0] += modifier.amount();
                }
            });
        }
        helper.assertTrue(armor[0] >= 20.0, "Die Rüstung der Sternwandler schützt zu wenig: " + armor[0]);
        helper.assertTrue(fall[0] >= 3.0, "Die Stiefel fangen keine Stürze ab: " + fall[0]);

        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-starwalker", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        double gravity = player.getAttributeValue(Attributes.GRAVITY);
        player.setItemSlot(EquipmentSlot.CHEST, new ItemStack(set.get(1).get()));
        helper.assertTrue(StarwalkerArmor.pieces(player) == 1 && !StarwalkerArmor.fullSet(player), "Ein Teil zählt nicht richtig");
        helper.assertFalse(StarwalkerArmor.anchor(player, new Vec3(0.0, 100.0, 0.0)), "Der Anker trägt schon mit einem Teil");
        for (int i = 0; i < set.size(); i++) {
            player.setItemSlot(slots[i], new ItemStack(set.get(i).get()));
        }
        // die Attribute der Ausrüstung setzt das Spiel erst im nächsten Tick – hier von Hand wie das Spiel
        for (int i = 0; i < set.size(); i++) {
            new ItemStack(set.get(i).get()).forEachModifier(slots[i], (attribute, modifier) -> {
                var instance = player.getAttribute(attribute);
                if (instance != null && !instance.hasModifier(modifier.id())) {
                    instance.addTransientModifier(modifier);
                }
            });
        }
        double light = player.getAttributeValue(Attributes.GRAVITY);
        helper.assertTrue(StarwalkerArmor.fullSet(player), "Der volle Satz zählt nicht");
        helper.assertTrue(light < gravity * 0.85 && light > gravity * 0.7, "Die Rüstung macht nicht leichter: " + gravity + " → " + light);

        // Sternenschweben: ein kurzer, unsichtbarer Sinkflug, der beim Loslassen endet (ein Trank bleibt)
        helper.assertTrue(StarwalkerArmor.glide(player, true), "Kein Sternenschweben");
        MobEffectInstance glide = player.getEffect(MobEffects.SLOW_FALLING);
        helper.assertTrue(glide != null && glide.isAmbient() && !glide.isVisible(), "Der Sinkflug fehlt: " + glide);
        helper.assertFalse(StarwalkerArmor.glide(player, false), "Schweben ohne Taste");
        helper.assertTrue(player.getEffect(MobEffects.SLOW_FALLING) == null, "Der Sinkflug endet nicht");
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 1200));
        StarwalkerArmor.glide(player, false);
        helper.assertTrue(player.getEffect(MobEffects.SLOW_FALLING) != null, "Die Rüstung nimmt einen Trank des sanften Falls weg");
        player.removeEffect(MobEffects.SLOW_FALLING);

        // Sternenanker: zurück auf festen Boden, dann eine Weile nicht
        BlockPos ground = helper.absolutePos(new BlockPos(3, 1, 3));
        Vec3 safe = Vec3.atBottomCenterOf(ground.above());
        player.teleportTo(level, safe.x + 20.0, level.getMinY() - 8.0, safe.z, java.util.Set.of(), 0.0f, 0.0f, true);
        player.setDeltaMovement(0.0, -2.0, 0.0);
        player.fallDistance = 40.0;
        helper.assertTrue(StarwalkerArmor.anchorDepth(level) > player.getY(), "Unter der Welt greift der Anker nicht");
        helper.assertTrue(StarwalkerArmor.anchor(player, safe), "Der Anker trägt nicht zurück");
        helper.assertTrue(player.position().distanceTo(safe) < 0.5, "Nicht auf dem festen Boden: " + player.position());
        helper.assertTrue(player.fallDistance == 0.0 && player.getDeltaMovement().lengthSqr() < 1.0E-4, "Der Sturz geht weiter");
        helper.assertFalse(StarwalkerArmor.anchorReady(player), "Keine Abklingzeit");
        helper.assertFalse(StarwalkerArmor.anchor(player, safe), "Der Anker trägt gleich noch einmal");
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }

    // =====================================================================================
    // Ein eigenes Feld weit draußen (für das Sternennetz)
    // =====================================================================================

    private record Field(ServerLevel level, BlockPos origin, List<long[]> chunks) {
        void close() {
            net.minecraft.world.phys.AABB area = new net.minecraft.world.phys.AABB(this.origin).inflate(48, 40, 48);
            for (net.minecraft.world.entity.Entity e : this.level.getEntitiesOfClass(net.minecraft.world.entity.Entity.class, area,
                    e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
                e.discard();
            }
            var box = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.makeBox(this.origin);
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
        BlockPos origin = new BlockPos(base.getX() + dx, 170, base.getZ() + dz);
        List<long[]> chunks = new java.util.ArrayList<>();
        int reach = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.REACH + 4;
        for (int cx = (origin.getX() - reach) >> 4; cx <= (origin.getX() + reach) >> 4; cx++) {
            for (int cz = (origin.getZ() - reach) >> 4; cz <= (origin.getZ() + reach) >> 4; cz++) {
                level.getChunk(cx, cz);
                level.setChunkForced(cx, cz, true);
                chunks.add(new long[] {cx, cz});
            }
        }
        return new Field(level, origin, chunks);
    }

    // =====================================================================================
    // Das Sternennetz
    // =====================================================================================

    /**
     * Ein Sternennetz im Nichts: die Netzplatte aus Sternenseide (Speichen und Ringe) und Sternengewebe trägt, in der Mitte das
     * Gelege (unzerstörbar, leuchtet, kennt den Platz der Weberin), darüber Platz für die Weberin an ihrem Faden; Ankerfelsen,
     * Taue, Kokons, Sternenspinnweben und eine Truhe.
     */
    private static void web(GameTestHelper helper) {
        Field field = field(helper, 33_000, 29_000);
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        try {
            io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.build(level,
                    io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.makeBox(o), o, 4242L, RandomSource.create(7), null);
            BlockPos nestPos = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.nest(o);
            var nest = level.getBlockState(nestPos);
            helper.assertTrue(nest.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.WEAVER_NEST.get()), "Kein Gelege in der Mitte: " + nest);
            helper.assertTrue(nest.getDestroySpeed(level, nestPos) < 0, "Das Gelege lässt sich abbauen");
            helper.assertTrue(nest.getLightEmission(level, nestPos) > 0, "Das Gelege leuchtet nicht");
            helper.assertTrue(level.getBlockEntity(nestPos) instanceof io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity be
                    && be.home() != null, "Das Gelege kennt den Platz der Weberin nicht");
            // über dem Gelege ist Platz für die Weberin an ihrem Faden
            for (int dy = 1; dy <= (int) io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.HANG + 2; dy++) {
                helper.assertTrue(level.getBlockState(nestPos.above(dy)).getCollisionShape(level, nestPos.above(dy)).isEmpty(),
                        "Über dem Gelege ist es verbaut (+" + dy + ")");
            }
            int silk = 0;
            int weave = 0;
            int webs = 0;
            int cocoons = 0;
            int chests = 0;
            int stone = 0;
            var box = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.makeBox(o);
            for (BlockPos p : BlockPos.betweenClosed(box.minX(), box.minY(), box.minZ(), box.maxX(), box.maxY(), box.maxZ())) {
                var st = level.getBlockState(p);
                if (st.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_SILK.get())) {
                    silk++;
                } else if (st.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEAVE.get())) {
                    weave++;
                } else if (st.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEB.get())) {
                    webs++;
                } else if (st.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_COCOON.get())) {
                    cocoons++;
                } else if (st.is(Blocks.CHEST)) {
                    chests++;
                } else if (st.is(VoidBlocks.VOID_STONE.base().get())) {
                    stone++;
                }
            }
            KingdomsAtWar.LOGGER.info("Sternennetz: {} Seide, {} Gewebe, {} Spinnweben, {} Kokons, {} Truhen, {} Leerenstein", silk, weave, webs,
                    cocoons, chests, stone);
            helper.assertTrue(silk > 200, "Zu wenig Sternenseide: " + silk);
            helper.assertTrue(weave > 200, "Zu wenig Sternengewebe: " + weave);
            helper.assertTrue(webs >= 4, "Zu wenig Sternenspinnweben: " + webs);
            helper.assertTrue(cocoons >= 8, "Zu wenig Kokons: " + cocoons);
            helper.assertTrue(chests == 1, "Nicht eine Truhe: " + chests);
            helper.assertTrue(stone > 200, "Keine Ankerfelsen: " + stone);
            // die Netzplatte trägt rings um das Gelege
            for (net.minecraft.core.Direction d : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                for (int r = 2; r <= 12; r += 5) {
                    BlockPos p = nestPos.relative(d, r);
                    helper.assertTrue(!level.getBlockState(p).getCollisionShape(level, p).isEmpty()
                            || !level.getBlockState(p.below()).getCollisionShape(level, p.below()).isEmpty(),
                            "Die Netzplatte trägt nicht (" + d + ", " + r + ")");
                }
            }
            // Sternenseide und Gewebe fallen als sie selbst, Spinnweben nur mit Schere (sonst Faden), gespuckte nie
            BlockPos at = helper.absolutePos(new BlockPos(2, 2, 2));
            helper.assertTrue(Block.getDrops(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_SILK.get().defaultBlockState(), level, at, null)
                    .stream().anyMatch(i -> i.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_SILK.get().asItem())), "Sternenseide fällt nicht");
            var web = io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEB.get().defaultBlockState();
            helper.assertTrue(Block.getDrops(web, level, at, null, null, new ItemStack(Items.SHEARS)).stream()
                    .anyMatch(i -> i.is(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_WEB.get().asItem())), "Mit der Schere fällt keine Spinnwebe");
            helper.assertTrue(Block.getDrops(web.setValue(io.github.sirschrolli.kingdomsatwar.block.voidrealm.StarWebBlock.TEMPORARY, true), level, at,
                    null, null, new ItemStack(Items.SHEARS)).isEmpty(), "Gespuckte Spinnweben lassen etwas fallen");
            var cocoonDrops = Block.getDrops(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_COCOON.get().defaultBlockState(), level, at, null);
            helper.assertTrue(!cocoonDrops.isEmpty(), "Ein Kokon gibt nichts frei");
        } finally {
            field.close();
        }
        helper.succeed();
    }

    /**
     * In der Leere entstehen Sternennetze – in den leeren Weiten (Sternennichts): Über die Kandidaten der Verteilung findet sich in
     * einem weiten Gebiet mindestens ein freier Raum. Ohne die Welt selbst mit einem eigenen Erzeuger der Leere.
     */
    private static void world(GameTestHelper helper) {
        var server = helper.getLevel().getServer();
        var registries = server.registryAccess();
        Holder<net.minecraft.world.level.levelgen.NoiseGeneratorSettings> settings = registries.lookupOrThrow(Registries.NOISE_SETTINGS)
                .getOrThrow(io.github.sirschrolli.kingdomsatwar.voidrealm.VoidTerrain.SETTINGS);
        var generator = new net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator(
                net.minecraft.world.level.biome.MultiNoiseBiomeSource.createFromList(VoidBiomes.parameters(registries.lookupOrThrow(Registries.BIOME))),
                settings);
        long seed = 20_261_112L;
        var random = net.minecraft.world.level.levelgen.RandomState.create(registries.lookupOrThrow(Registries.NOISE), seed, settings.value());
        var height = net.minecraft.world.level.LevelHeightAccessor.create(-64, 384);
        var biomes = generator.getBiomeSource().createUncachedResolver(random);
        var set = registries.lookupOrThrow(Registries.STRUCTURE_SET).getOrThrow(io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures.STAR_WEBS);
        helper.assertTrue(set.value().placement() instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement,
                "Die Sternennetze sind nicht gleichmäßig verteilt");
        var placement = (net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement) set.value().placement();
        var expanse = registries.lookupOrThrow(Registries.BIOME).getOrThrow(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Biomes.HAS_STAR_WEB);
        int cells = 0;
        int sites = 0;
        BlockPos first = null;
        int span = 7;
        for (int i = -span; i < span; i++) {
            for (int j = -span; j < span; j++) {
                net.minecraft.world.level.ChunkPos c = placement.getPotentialStructureChunk(seed, i * placement.spacing(), j * placement.spacing());
                cells++;
                BlockPos site = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebStructure.find(generator, height, random, biomes,
                        expanse::contains, c, seed + i * 31L + j);
                if (site != null) {
                    sites++;
                    if (first == null) {
                        first = site;
                    }
                }
            }
        }
        KingdomsAtWar.LOGGER.info("Sternennetze: {} Felder, {} passend (erstes bei {})", cells, sites, first);
        helper.assertTrue(sites * 40 >= cells, "Zu selten ein freier Raum für ein Sternennetz: " + sites + " von " + cells + " Feldern");
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
     * Auf dem Netz: Die Weberin hängt in Andacht über dem Gelege (ein Schlag weckt sie, unverwundbar); sie erwacht, stürzt auf
     * ihr Netz (auch dabei unverwundbar). Dann ihre Angriffe gegen einen Ritter: Giftbiss, Fangfaden, Netzwurf, Sternbild,
     * Sternschnuppen, Umgarnen, Brut, Abseilen; unter halber Kraft ihr Zorn und die Spindel. Besiegt lässt sie den Sternenbogen
     * fallen, das Gelege erlischt; ein Himmelseisenbarren ruft sie zurück; zurückgesetzt hängt beim nächsten Besuch wieder eine.
     */
    private static void fight(GameTestHelper helper) {
        Field field = field(helper, -31_000, 27_000);
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.build(level,
                io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.makeBox(o), o, 99L, RandomSource.create(3), null);
        BlockPos nestPos = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.nest(o);
        if (!(level.getBlockEntity(nestPos) instanceof io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity nest)) {
            field.close();
            helper.fail("Das Gelege hat keine Blockentität");
            return;
        }
        var weaver = nest.spawn(level, false);
        if (weaver == null) {
            field.close();
            helper.fail("Keine Weberin über dem Gelege");
            return;
        }
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-weaver-visitor", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        player.teleportTo(level, o.getX() + 0.5, o.getY() + 1.0, o.getZ() + 30.5, java.util.Set.of(), 180.0f, 0.0f, true);
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(weaver.isDormant(), "Die Weberin hängt nicht in Andacht: " + weaver.currentAttack());
            helper.assertTrue(Math.abs(weaver.getY() - (nest.home().getY() + io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.HANG)) < 0.6,
                    "Die Weberin hängt nicht über dem Gelege: " + weaver.position());
            helper.assertTrue(nest.owns(weaver), "Das Gelege kennt seine Weberin nicht");
            float hp = weaver.getHealth();
            weaver.hurtServer(level, level.damageSources().playerAttack(player), 40.0f);
            helper.assertTrue(weaver.getHealth() >= hp, "In Andacht ist sie verwundbar");
            helper.assertTrue(weaver.currentAttack() == io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.AWAKEN,
                    "Ein Schlag weckt sie nicht: " + weaver.currentAttack());
            weaver.hurtServer(level, level.damageSources().generic(), 40.0f);
            helper.assertTrue(weaver.getHealth() >= hp, "Beim Erwachen ist sie verwundbar");
            level.getServer().getPlayerList().remove(player);
            helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.AWAKEN.duration + 6, () -> {
                helper.assertTrue(weaver.currentAttack() != io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.AWAKEN,
                        "Das Erwachen endet nicht");
                helper.assertTrue(weaver.getY() < nest.home().getY() + 2.5, "Sie stürzt nicht auf ihr Netz: " + weaver.position());
                attacks(helper, field, nest, weaver);
            });
        });
    }

    private static void attacks(GameTestHelper helper, Field field, io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity nest,
            io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity weaver) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        var knight = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SOLDIERS.get(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.KNIGHT)
                .get().create(level, net.minecraft.world.entity.EntitySpawnReason.EVENT);
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
        Vec3 spot = Vec3.atBottomCenterOf(nest.home()).add(0.0, 0.0, -5.0);
        knight.snapTo(spot.x + 4.0, spot.y, spot.z, 0.0f, 0.0f);
        level.addFreshEntity(knight);
        float[] hp = {0.0f};
        double[] gap = {0.0};
        // vor jedem Angriff: der Ritter {@code ahead} Blöcke östlich der Weberin (beide auf dem Netz), geheilt, sie blickt ihn an
        Consumer<Double> place = ahead -> {
            knight.removeAllEffects();
            knight.setHealth(knight.getMaxHealth());
            knight.setInvulnerableTime(0);
            knight.hurtTime = 0;
            knight.teleportTo(spot.x + ahead, spot.y, spot.z);
            knight.setDeltaMovement(Vec3.ZERO);
            weaver.snapTo(spot.x, spot.y, spot.z, -90.0f, 0.0f);
            weaver.setYBodyRot(-90.0f);
            weaver.setYHeadRot(-90.0f);
            weaver.setDeltaMovement(Vec3.ZERO);
            weaver.setTarget(knight);
            hp[0] = knight.getHealth();
            gap[0] = knight.distanceTo(weaver);
        };
        var around = new net.minecraft.world.phys.AABB(o).inflate(40, 30, 40);
        int[] brood = {0};
        List<Step> steps = List.of(
                new Step(20, () -> {
                    place.accept(3.2);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.BITE);
                }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Der Giftbiss trifft den Ritter nicht");
                    helper.assertTrue(knight.hasEffect(MobEffects.POISON), "Der Giftbiss vergiftet den Ritter nicht");
                }),
                new Step(26, () -> {
                    place.accept(12.0);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.THREAD);
                }, () -> helper.assertTrue(knight.distanceTo(weaver) < gap[0] - 3.0, "Der Fangfaden zieht den Ritter nicht heran: "
                        + knight.distanceTo(weaver))),
                new Step(20, () -> { }, () -> { }),
                new Step(18, () -> {
                    place.accept(9.0);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.WEB);
                }, () -> helper.assertTrue(!level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWebShotEntity.class, around)
                        .isEmpty() || knight.hasEffect(MobEffects.SLOWNESS), "Kein Netzballen")),
                new Step(30, () -> { }, () -> { }),
                new Step(14, () -> {
                    place.accept(8.0);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.CONSTELLATION);
                }, () -> helper.assertTrue(weaver.constellation().size() >= 6, "Kein Sternbild um den Ritter: " + weaver.constellation().size())),
                new Step(64, () -> { }, () -> { }),
                new Step(40, () -> {
                    place.accept(8.0);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.STARFALL);
                }, () -> { }),
                new Step(40, () -> { }, () -> helper.assertTrue(knight.getHealth() < hp[0], "Keine Sternschnuppe trifft den Ritter")),
                new Step(18, () -> {
                    place.accept(3.5);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.COCOON);
                }, () -> helper.assertTrue(weaver.isRooted(knight), "Sie spinnt den Ritter nicht ein")),
                new Step(60, () -> { }, () -> helper.assertFalse(weaver.isRooted(knight), "Die Fäden reißen nicht")),
                new Step(36, () -> {
                    place.accept(6.0);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.BROOD);
                }, () -> brood[0] = level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarSpiderlingEntity.class, around).size()),
                new Step(10, () -> { }, () -> helper.assertTrue(brood[0] > 0, "Keine Brut")),
                new Step(2, () -> {
                    place.accept(7.0);
                    weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.DROP);
                }, () -> { }),
                new Step(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.DROP_FALL + 20, () -> { }, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Das Abseilen trifft den Ritter nicht");
                }));
        run(helper, steps, 0, () -> fury(helper, field, nest, weaver, knight, place));
    }

    /** Der Zorn unter halber Kraft, danach die Spindel (die Fäden treffen den Ritter, wenn er nicht springt). */
    private static void fury(GameTestHelper helper, Field field, io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity nest,
            io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity weaver, net.minecraft.world.entity.LivingEntity knight,
            Consumer<Double> place) {
        weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.NONE);
        weaver.setHealth(weaver.getMaxHealth() * 0.45f);
        place.accept(8.0);
        float[] hp = {knight.getHealth()};
        helper.runAfterDelay(4, () -> {
            helper.assertTrue(weaver.currentAttack() == io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.FURY,
                    "Unter halber Kraft rast sie nicht: " + weaver.currentAttack() + " (" + weaver.getHealth() + "/" + weaver.getMaxHealth()
                            + ", Zorn " + weaver.isEnraged() + ")");
            helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.FURY.duration + 2, () -> {
                helper.assertTrue(weaver.isEnraged(), "Nach dem Kreischen ist sie nicht im Zorn");
                helper.assertTrue(weaver.currentAttack() == io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.SPINDLE,
                        "Nach dem Zorn kommt nicht die Spindel: " + weaver.currentAttack());
                knight.teleportTo(weaver.getX() + 8.0, weaver.getY(), weaver.getZ());
                knight.setHealth(knight.getMaxHealth());
                hp[0] = knight.getHealth();
                helper.runAfterDelay(80, () -> {
                    helper.assertTrue(knight.getHealth() < hp[0], "Die Spindel trifft den Ritter nicht");
                    knight.discard();
                    death(helper, field, nest, weaver);
                });
            });
        });
    }

    /** Tod: der Sternenbogen, das Gelege erlischt; ein Himmelseisenbarren ruft sie zurück; zurückgesetzt hängt wieder eine. */
    private static void death(GameTestHelper helper, Field field, io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity nest,
            io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity weaver) {
        ServerLevel level = field.level();
        BlockPos o = field.origin();
        BlockPos nestPos = io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece.nest(o);
        var around = new net.minecraft.world.phys.AABB(o).inflate(40, 30, 40);
        weaver.forceAttack(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.NONE);
        BossLootGameTests.firstVictory(helper, weaver, Float.MAX_VALUE);
        helper.assertTrue(weaver.isDeadOrDying(), "Die Weberin stirbt nicht");
        helper.runAfterDelay(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.DEATH_TICKS + 6, () -> {
            helper.assertTrue(weaver.isRemoved(), "Die Weberin zerfällt nicht");
            boolean crystal = !level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, around,
                    e -> e.getItem().is(io.github.sirschrolli.kingdomsatwar.registry.ModItems.HEART_CRYSTAL.get())).isEmpty();
            helper.assertTrue(crystal, "Sie lässt keinen Herzkristall fallen");
            helper.assertTrue(level.getBlockState(nestPos).getValue(io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlock.PHASE)
                    == io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlock.Phase.DEFEATED, "Das Gelege erlischt nicht");
            helper.assertTrue(nest.recall(level, null), "Ein Himmelseisenbarren ruft sie nicht zurück");
            var next = nest.weaver(level);
            helper.assertTrue(next != null && next.currentAttack() == io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity.Attack.AWAKEN,
                    "Die gerufene Weberin erwacht nicht");
            nest.reset(level);
            helper.assertTrue(nest.weaver(level) == null, "Zurückgesetzt bleibt die Weberin");
            helper.assertTrue(level.getBlockState(nestPos).getValue(io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlock.PHASE)
                    == io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlock.Phase.SLEEPING, "Zurückgesetzt ruht das Gelege nicht wieder");
            field.close();
            helper.succeed();
        });
    }

    // =====================================================================================
    // Der Sternenbogen
    // =====================================================================================

    /**
     * Der Sternenbogen ist ein Bogen (verzauberbar, mit Himmelseisen zu reparieren); seine Pfeile fliegen schwerelos und schneller;
     * im Schleichen voll gespannt wird es ein Netzpfeil (danach ruht der Bogen), der sein Ziel einspinnt.
     */
    private static void bow(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ItemStack bow = new ItemStack(io.github.sirschrolli.kingdomsatwar.registry.StarWeaver.STAR_BOW.get());
        helper.assertTrue(bow.getItem() instanceof net.minecraft.world.item.BowItem, "Der Sternenbogen ist kein Bogen");
        helper.assertTrue(bow.is(net.minecraft.tags.ItemTags.BOW_ENCHANTABLE) && bow.is(net.neoforged.neoforge.common.Tags.Items.TOOLS_BOW),
                "Der Sternenbogen ist nicht als Bogen gemerkt");
        helper.assertTrue(bow.isValidRepairItem(new ItemStack(VoidGear.SKY_IRON_INGOT.get())), "Himmelseisen repariert den Bogen nicht");
        ServerPlayer player = MageGameTests.survivalPlayer(helper, "test-star-bow", new io.netty.channel.embedded.EmbeddedChannel[1], 0);
        BlockPos stand = helper.absolutePos(new BlockPos(4, 2, 2));
        player.teleportTo(level, stand.getX() + 0.5, stand.getY(), stand.getZ() + 0.5, java.util.Set.of(), 0.0f, 0.0f, true);
        player.getInventory().add(new ItemStack(Items.ARROW, 16));
        player.setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, bow);
        // voll gespannt, aufrecht: ein schwereloser Sternenpfeil
        bow.releaseUsing(level, player, bow.getUseDuration(player) - 30);
        var arrows = level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarArrowEntity.class, player.getBoundingBox().inflate(4.0));
        helper.assertTrue(arrows.size() == 1, "Kein Sternenpfeil: " + arrows.size());
        var arrow = arrows.getFirst();
        helper.assertFalse(arrow.isWeb(), "Ohne Schleichen schon ein Netzpfeil");
        helper.assertTrue(arrow.isNoGravity() || arrow.getDeltaMovement().length() > 3.0, "Der Pfeil ist nicht schnell: " + arrow.getDeltaMovement());
        double vy = arrow.getDeltaMovement().y;
        arrow.discard();
        // im Schleichen: ein Netzpfeil, der Bogen ruht danach
        player.setShiftKeyDown(true);
        bow.releaseUsing(level, player, bow.getUseDuration(player) - 30);
        var webs = level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarArrowEntity.class, player.getBoundingBox().inflate(4.0));
        helper.assertTrue(webs.size() == 1 && webs.getFirst().isWeb(), "Im Schleichen kein Netzpfeil");
        helper.assertTrue(player.getCooldowns().isOnCooldown(bow), "Nach dem Netzpfeil ruht der Bogen nicht");
        webs.getFirst().discard();
        helper.assertTrue(Math.abs(vy) < 0.2, "Der Pfeil fliegt nicht waagrecht: " + vy);
        level.getServer().getPlayerList().remove(player);
        helper.succeed();
    }
}
