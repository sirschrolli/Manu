package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.AshLayerBlock;
import io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock;
import io.github.sirschrolli.kingdomsatwar.block.GlowcapBlock;
import io.github.sirschrolli.kingdomsatwar.block.ShadowGrassBlock;
import io.github.sirschrolli.kingdomsatwar.block.ShadowPlantBlock;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.util.ColorRGBA;
import net.minecraft.world.level.block.Block;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ColoredFallingBlock;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.TintedParticleLeavesBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;

/**
 * Die Blöcke der Schattenwelt: Schattenstein in vielen Formen, der tiefere Tiefschattenstein, Schattenerde und
 * Schattengras (seine Farbe kommt aus dem Biom), fahler Schattensand, Aschschnee auf den Gipfeln, das Holz der
 * Schattenbäume mit violettem Laub, leuchtende Nachtkristalle, Glimmmoos und die Pflanzen des Zwielichts.
 */
public final class ShadowBlocks {
    private ShadowBlocks() {}

    /** Lädt die Klasse, damit sich alle Blöcke anmelden, bevor die Register gefüllt werden. */
    public static void init() {}

    private static final List<java.util.function.Supplier<? extends net.minecraft.world.level.ItemLike>> TAB = ModBlocks.SHADOW_TAB;

    // =====================================================================================
    // Gestein
    // =====================================================================================

    /** Schattenstein: dunkler, violettgrauer Fels mit fahlen Schlieren – das Grundgestein der Schattenwelt. */
    public static final BuildingSet SHADOW_STONE = ModBlocks.stoneSet("shadow_stone", "shadow_stone", MapColor.TERRACOTTA_GRAY,
            SoundType.DEEPSLATE, 1.6f, TAB);
    /** Ziegel aus Schattenstein. */
    public static final BuildingSet SHADOW_STONE_BRICKS = ModBlocks.stoneSet("shadow_stone_bricks", "shadow_stone_brick",
            MapColor.TERRACOTTA_GRAY, SoundType.DEEPSLATE_BRICKS, 2.0f, TAB);
    /** Polierter Schattenstein. */
    public static final BuildingSet POLISHED_SHADOW_STONE = ModBlocks.stoneSet("polished_shadow_stone", "polished_shadow_stone",
            MapColor.TERRACOTTA_GRAY, SoundType.POLISHED_DEEPSLATE, 2.0f, TAB);
    public static final DeferredBlock<Block> CRACKED_SHADOW_STONE_BRICKS = stone("cracked_shadow_stone_bricks", SoundType.DEEPSLATE_BRICKS);
    /** Gemeißelter Schattenstein: eine Mondsichel über einem wachenden Auge – das Zeichen der Umbrer. */
    public static final DeferredBlock<Block> CHISELED_SHADOW_STONE = stone("chiseled_shadow_stone", SoundType.POLISHED_DEEPSLATE);
    /** Tiefschattenstein: fast schwarzes, geschichtetes Gestein tief unter der Oberfläche. */
    public static final DeferredBlock<RotatedPillarBlock> DEEP_SHADOW_STONE = ModBlocks.blockWithItem("deep_shadow_stone",
            RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(3.0f, 6.0f).sound(SoundType.DEEPSLATE),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Böden
    // =====================================================================================

    /** Schattenerde: dunkle, violettbraune Erde. */
    public static final DeferredBlock<Block> SHADOW_SOIL = ModBlocks.blockWithItem("shadow_soil", Block::new,
            p -> p.mapColor(MapColor.TERRACOTTA_BLACK).strength(0.5f).sound(SoundType.ROOTED_DIRT),
            UnaryOperator.identity(), TAB);
    /** Schattengras: wächst auch im Zwielicht; seine Farbe hängt vom Biom ab. */
    public static final DeferredBlock<ShadowGrassBlock> SHADOW_GRASS = ModBlocks.blockWithItem("shadow_grass", ShadowGrassBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).randomTicks().strength(0.6f).sound(SoundType.GRASS),
            UnaryOperator.identity(), TAB);
    /** Schattensand: fahler, feiner Sand der Bleichen Öde und der Küsten. */
    public static final DeferredBlock<ColoredFallingBlock> SHADOW_SAND = ModBlocks.blockWithItem("shadow_sand",
            p -> new ColoredFallingBlock(new ColorRGBA(0xFF9A94A6), p),
            p -> p.mapColor(MapColor.TERRACOTTA_WHITE).instrument(NoteBlockInstrument.SNARE).strength(0.5f).sound(SoundType.SAND),
            UnaryOperator.identity(), TAB);
    /** Aschschnee: grauer Schnee voller Asche, der auf den Schattengipfeln liegt (Block). */
    public static final DeferredBlock<Block> ASH_SNOW_BLOCK = ModBlocks.blockWithItem("ash_snow_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).strength(0.2f).sound(SoundType.SNOW),
            UnaryOperator.identity(), TAB);
    /** Aschschnee in bis zu acht Lagen – er schmilzt nicht. */
    public static final DeferredBlock<AshLayerBlock> ASH_SNOW = ModBlocks.blockWithItem("ash_snow", AshLayerBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).replaceable().forceSolidOff().strength(0.1f).sound(SoundType.SNOW)
                    .isViewBlocking((state, level, pos, box) -> state.getValue(SnowLayerBlock.LAYERS) >= 8)
                    .pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Schattenholz
    // =====================================================================================

    public static final DeferredBlock<RotatedPillarBlock> SHADOW_LOG = log("shadow_log", MapColor.COLOR_BLACK);
    public static final DeferredBlock<RotatedPillarBlock> SHADOW_WOOD = log("shadow_wood", MapColor.COLOR_BLACK);
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_SHADOW_LOG = log("stripped_shadow_log", MapColor.COLOR_PURPLE);
    public static final DeferredBlock<RotatedPillarBlock> STRIPPED_SHADOW_WOOD = log("stripped_shadow_wood", MapColor.COLOR_PURPLE);
    /** Schattenholzbretter (auch als Treppe und Stufe). */
    public static final BuildingSet SHADOW_PLANKS = ModBlocks.buildingSet("shadow_planks", "shadow_plank",
            p -> p.mapColor(MapColor.COLOR_PURPLE).instrument(NoteBlockInstrument.BASS).strength(2.0f, 3.0f).sound(SoundType.WOOD)
                    .ignitedByLava(),
            false, BuildingSet.Tool.AXE, TAB);
    /** Schattenlaub: violett, im Wald dunkler, auf den schwebenden Inseln heller (Farbe aus dem Biom). */
    public static final DeferredBlock<TintedParticleLeavesBlock> SHADOW_LEAVES = ModBlocks.blockWithItem("shadow_leaves",
            p -> new TintedParticleLeavesBlock(0.01f, p),
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(0.2f).randomTicks().sound(SoundType.AZALEA_LEAVES).noOcclusion()
                    .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos, box) -> false).ignitedByLava().pushReaction(PushReaction.POPPED)
                    .isRedstoneConductor((state, level, pos) -> false),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Licht und Pflanzen
    // =====================================================================================

    /** Nachtkristall: durchscheinend, leuchtet in kaltem Blauviolett. */
    public static final DeferredBlock<HalfTransparentBlock> NIGHT_CRYSTAL = ModBlocks.blockWithItem("night_crystal",
            HalfTransparentBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLUE).strength(1.5f).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 12)
                    .isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false),
            UnaryOperator.identity(), TAB);
    /** Nachtkristall-Druse: ein Grüppchen spitzer Kristalle. */
    public static final DeferredBlock<CrystalClusterBlock> NIGHT_CRYSTAL_CLUSTER = ModBlocks.blockWithItem("night_crystal_cluster",
            p -> new CrystalClusterBlock(0x8A7AFF, p),
            p -> p.mapColor(MapColor.COLOR_BLUE).noCollision().noOcclusion().strength(0.5f).sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 8).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Glimmmoos: weiches Moos, dessen Spitzen silbrig glimmen. */
    public static final DeferredBlock<Block> GLIMMER_MOSS = ModBlocks.blockWithItem("glimmer_moss", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.1f).sound(SoundType.MOSS).lightLevel(state -> 7),
            UnaryOperator.identity(), TAB);
    /** Geisterpilze: fahle Pilze, die wie kleine Laternen leuchten. */
    public static final DeferredBlock<GlowcapBlock> GHOSTCAP = ModBlocks.blockWithItem("ghostcap", GlowcapBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_WHITE).noCollision().instabreak().sound(SoundType.FUNGUS).lightLevel(state -> 9)
                    .offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Schattenhalme: niedriges, fedriges Gras des Zwielichts (Farbe aus dem Biom). */
    public static final DeferredBlock<ShadowPlantBlock> SHADOW_SPROUTS = ModBlocks.blockWithItem("shadow_sprouts", ShadowPlantBlock::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).replaceable().noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Bleichgras: dürre, fahle Halme der Bleichen Öde. */
    public static final DeferredBlock<ShadowPlantBlock> PALE_GRASS = ModBlocks.blockWithItem("pale_grass", ShadowPlantBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_WHITE).replaceable().noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XYZ).ignitedByLava().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);

    /** Seelenbeerenstrauch (ohne eigenen Gegenstand – man pflanzt ihn mit den Beeren). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock> SOUL_BERRY_BUSH =
            ModBlocks.BLOCKS.registerBlock("soul_berry_bush", io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock::new,
                    p -> p.mapColor(MapColor.COLOR_CYAN).randomTicks().noCollision().strength(0.2f).sound(SoundType.SWEET_BERRY_BUSH)
                            .lightLevel(io.github.sirschrolli.kingdomsatwar.block.SoulBerryBushBlock::light).pushReaction(PushReaction.POPPED));
    /** Glimmerranken: hängen von Laub und Höhlendecken, die Spitzen glimmen. */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.GlimmerVinesBlock> GLIMMER_VINES = ModBlocks.blockWithItem(
            "glimmer_vines", io.github.sirschrolli.kingdomsatwar.block.GlimmerVinesBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).randomTicks().noCollision().instabreak().sound(SoundType.WEEPING_VINES)
                    .lightLevel(state -> 9).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.GlimmerVinesPlantBlock> GLIMMER_VINES_PLANT =
            ModBlocks.BLOCKS.registerBlock("glimmer_vines_plant", io.github.sirschrolli.kingdomsatwar.block.GlimmerVinesPlantBlock::new,
                    p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.WEEPING_VINES)
                            .lightLevel(state -> 4).pushReaction(PushReaction.POPPED));

    // =====================================================================================
    // Erze
    // =====================================================================================

    /** Mondsilbererz: fahle Silberflocken im Schattenstein (häufig, ab einer Steinspitzhacke). */
    public static final DeferredBlock<DropExperienceBlock> MOONSILVER_ORE = ore("moonsilver_ore", false, 1, 3, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEP_MOONSILVER_ORE = ore("deep_moonsilver_ore", true, 1, 3, 0);
    /** Nachtstahlerz: dunkle Klumpen mit violettem Glanz (ab einer Eisenspitzhacke). */
    public static final DeferredBlock<DropExperienceBlock> NIGHTSTEEL_ORE = ore("nightsteel_ore", false, 2, 5, 0);
    public static final DeferredBlock<DropExperienceBlock> DEEP_NIGHTSTEEL_ORE = ore("deep_nightsteel_ore", true, 2, 5, 0);
    /** Seelenkristallerz: nur tief unten, die Kristalle leuchten türkis. */
    public static final DeferredBlock<DropExperienceBlock> SOUL_CRYSTAL_ORE = ore("soul_crystal_ore", true, 4, 8, 5);
    /** Abyssiterz: ganz unten im Abgrund, sehr hart und explosionsfest – es glüht magenta durch die Risse. */
    public static final DeferredBlock<Block> ABYSSITE_ORE = ModBlocks.blockWithItem("abyssite_ore", Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(30.0f, 1200.0f).sound(SoundType.ANCIENT_DEBRIS).lightLevel(state -> 3),
            ip -> ip.fireResistant(), TAB);

    // =====================================================================================
    // Speicherblöcke
    // =====================================================================================

    public static final DeferredBlock<Block> MOONSILVER_BLOCK = ModBlocks.blockWithItem("moonsilver_block", Block::new,
            p -> p.mapColor(MapColor.METAL).instrument(NoteBlockInstrument.BELL).requiresCorrectToolForDrops().strength(5.0f, 6.0f)
                    .sound(SoundType.METAL),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<Block> NIGHTSTEEL_BLOCK = ModBlocks.blockWithItem("nightsteel_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_BLUE).instrument(NoteBlockInstrument.IRON_XYLOPHONE).requiresCorrectToolForDrops()
                    .strength(25.0f, 1200.0f).sound(SoundType.NETHERITE_BLOCK),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<Block> SOUL_CRYSTAL_BLOCK = ModBlocks.blockWithItem("soul_crystal_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).requiresCorrectToolForDrops().strength(5.0f, 6.0f).sound(SoundType.AMETHYST)
                    .lightLevel(state -> 10),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<Block> ABYSSITE_BLOCK = ModBlocks.blockWithItem("abyssite_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).requiresCorrectToolForDrops().strength(50.0f, 1200.0f)
                    .sound(SoundType.NETHERITE_BLOCK).lightLevel(state -> 4),
            ip -> ip.fireResistant(), TAB);

    // =====================================================================================
    // Licht, Setzling
    // =====================================================================================

    /** Mondlaterne: Mondsilber und Nachtkristall – kaltes, weißblaues Licht. */
    public static final DeferredBlock<LanternBlock> MOON_LANTERN = ModBlocks.blockWithItem("moon_lantern", LanternBlock::new,
            p -> p.mapColor(MapColor.METAL).forceSolidOn().strength(3.5f).sound(SoundType.LANTERN).lightLevel(state -> 15)
                    .noOcclusion().pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Schattensetzling: wächst zu einem Schattenbaum, vier im Quadrat zu einem Schattenriesen. */
    public static final DeferredBlock<SaplingBlock> SHADOW_SAPLING = ModBlocks.blockWithItem("shadow_sapling",
            p -> new SaplingBlock(io.github.sirschrolli.kingdomsatwar.shadow.ShadowFeatures.SHADOW_TREE, p),
            p -> p.mapColor(MapColor.COLOR_PURPLE).noCollision().randomTicks().instabreak().sound(SoundType.GRASS)
                    .pushReaction(PushReaction.POPPED),
            ip -> ip.compostable(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COMPOSTABLE_LOW), TAB);
    public static final DeferredBlock<FlowerPotBlock> POTTED_SHADOW_SAPLING = ModBlocks.BLOCKS.registerBlock("potted_shadow_sapling",
            p -> new FlowerPotBlock(() -> (FlowerPotBlock) Blocks.FLOWER_POT, SHADOW_SAPLING, p),
            p -> p.instabreak().noOcclusion().pushReaction(PushReaction.POPPED));

    // =====================================================================================
    // Hilfsmethoden
    // =====================================================================================

    private static DeferredBlock<DropExperienceBlock> ore(String name, boolean deep, int minXp, int maxXp, int light) {
        return ModBlocks.blockWithItem(name, p -> new DropExperienceBlock(UniformInt.of(minXp, maxXp), p),
                p -> p.mapColor(deep ? MapColor.DEEPSLATE : MapColor.TERRACOTTA_GRAY).instrument(NoteBlockInstrument.BASEDRUM)
                        .requiresCorrectToolForDrops().strength(deep ? 4.5f : 3.0f, 3.0f)
                        .sound(deep ? SoundType.DEEPSLATE : SoundType.STONE).lightLevel(state -> light),
                UnaryOperator.identity(), TAB);
    }

    private static DeferredBlock<Block> stone(String name, SoundType sound) {
        return ModBlocks.blockWithItem(name, Block::new,
                p -> p.mapColor(MapColor.TERRACOTTA_GRAY).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                        .strength(2.0f, 6.0f).sound(sound),
                UnaryOperator.identity(), TAB);
    }

    private static DeferredBlock<RotatedPillarBlock> log(String name, MapColor color) {
        return ModBlocks.blockWithItem(name, RotatedPillarBlock::new,
                p -> p.mapColor(color).instrument(NoteBlockInstrument.BASS).strength(2.0f).sound(SoundType.WOOD).ignitedByLava(),
                ip -> ip.cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_WOOD_BLOCKS), TAB);
    }
}
