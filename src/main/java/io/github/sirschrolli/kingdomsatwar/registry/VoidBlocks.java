package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.CrystalFoliageBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.StarVinesBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.StarVinesPlantBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.StardustBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.VoidBloomBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.VoidMossBlock;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.VoidRiftBlock;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HalfTransparentBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Blöcke der Leere – der Welt, die das Herz der Schatten aufreißt:
 * <ul>
 *     <li><b>Leerenstein</b> – nachtschwarzer Fels mit winzigen, eingeschlossenen Sternen, aus dem die treibenden
 *     Bruchstücke bestehen (mit Ziegeln, poliert, gemeißelt, rissig);</li>
 *     <li><b>Sternenmarmor</b> – heller, violett geäderter Marmor mit Goldadern, aus dem die Alten ihre Bauten errichteten
 *     (die schwerelosen Ruinen);</li>
 *     <li><b>Leerenmoos</b>, <b>Sternenstaub</b>, <b>Leerenkristall</b> und seine Drusen, <b>Kristalllaub</b>,
 *     <b>Leerenblüten</b> und <b>Sternenranken</b>;</li>
 *     <li>der <b>Leerenriss</b> selbst – das Tor zwischen den Welten.</li>
 * </ul>
 */
public final class VoidBlocks {
    private VoidBlocks() {}

    /** Inhalt des Kreativ-Tabs „Die Leere“ (in Reihenfolge). */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    /** Lädt die Klasse, damit sich alle Blöcke anmelden, bevor die Register gefüllt werden. */
    public static void init() {}

    public static List<Supplier<? extends ItemLike>> tab() {
        return java.util.Collections.unmodifiableList(TAB);
    }

    // =====================================================================================
    // Gestein
    // =====================================================================================

    /** Leerenstein: nachtschwarzer, bläulicher Fels mit winzigen, eingeschlossenen Sternen. */
    public static final BuildingSet VOID_STONE = ModBlocks.stoneSet("void_stone", "void_stone", MapColor.COLOR_BLACK,
            SoundType.DEEPSLATE, 1.8f, TAB);
    public static final BuildingSet VOID_STONE_BRICKS = ModBlocks.stoneSet("void_stone_bricks", "void_stone_brick",
            MapColor.COLOR_BLACK, SoundType.DEEPSLATE_BRICKS, 2.0f, TAB);
    public static final BuildingSet POLISHED_VOID_STONE = ModBlocks.stoneSet("polished_void_stone", "polished_void_stone",
            MapColor.COLOR_BLACK, SoundType.POLISHED_DEEPSLATE, 2.0f, TAB);
    public static final DeferredBlock<Block> CRACKED_VOID_STONE_BRICKS = stone("cracked_void_stone_bricks", SoundType.DEEPSLATE_BRICKS);
    /** Gemeißelter Leerenstein: das Zeichen der Alten – ein Auge über einem Riss. */
    public static final DeferredBlock<Block> CHISELED_VOID_STONE = stone("chiseled_void_stone", SoundType.POLISHED_DEEPSLATE);

    /** Sternenmarmor: heller, violett geäderter Marmor mit feinen Goldadern – der Baustoff der Alten. */
    public static final BuildingSet ASTRAL_MARBLE = ModBlocks.stoneSet("astral_marble", "astral_marble", MapColor.QUARTZ,
            SoundType.CALCITE, 1.6f, TAB);
    public static final BuildingSet ASTRAL_MARBLE_BRICKS = ModBlocks.stoneSet("astral_marble_bricks", "astral_marble_brick",
            MapColor.QUARTZ, SoundType.CALCITE, 1.8f, TAB);
    public static final DeferredBlock<RotatedPillarBlock> ASTRAL_PILLAR = ModBlocks.blockWithItem("astral_pillar", RotatedPillarBlock::new,
            p -> p.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8f, 6.0f)
                    .sound(SoundType.CALCITE),
            UnaryOperator.identity(), TAB);
    /** Gemeißelter Sternenmarmor: ein goldener Stern in einem Kreis. */
    public static final DeferredBlock<Block> CHISELED_ASTRAL_MARBLE = ModBlocks.blockWithItem("chiseled_astral_marble", Block::new,
            p -> p.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(1.8f, 6.0f)
                    .sound(SoundType.CALCITE).lightLevel(s -> 4),
            UnaryOperator.identity(), TAB);

    // =====================================================================================
    // Boden, Kristalle und Pflanzen
    // =====================================================================================

    /** Leerenmoos: samtiges Moos von der Farbe der Nacht auf dem Leerenstein. */
    public static final DeferredBlock<VoidMossBlock> VOID_MOSS = ModBlocks.blockWithItem("void_moss", VoidMossBlock::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).strength(1.4f).requiresCorrectToolForDrops().sound(SoundType.MOSS),
            UnaryOperator.identity(), TAB);
    /** Sternenstaub: fahlblauer Staub voller winziger Sterne; er rieselt nicht und leuchtet schwach. */
    public static final DeferredBlock<StardustBlock> STARDUST = ModBlocks.blockWithItem("stardust", StardustBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.SNARE).strength(0.6f).sound(SoundType.SAND)
                    .lightLevel(s -> 4),
            UnaryOperator.identity(), TAB);
    /** Leerenkristall: durchscheinender, von innen leuchtender Kristall – Stämme der Kristallbäume, Splitterklippen. */
    public static final DeferredBlock<HalfTransparentBlock> VOID_CRYSTAL = ModBlocks.blockWithItem("void_crystal", HalfTransparentBlock::new,
            p -> p.mapColor(MapColor.COLOR_MAGENTA).strength(1.5f).sound(SoundType.AMETHYST).noOcclusion().lightLevel(s -> 11)
                    .isValidSpawn((state, level, pos, type) -> false).isRedstoneConductor((state, level, pos) -> false)
                    .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false),
            UnaryOperator.identity(), TAB);
    /** Leerenkristall-Druse: spitze Kristalle, die in Violett und Türkis glimmen. */
    public static final DeferredBlock<CrystalClusterBlock> VOID_CRYSTAL_CLUSTER = ModBlocks.blockWithItem("void_crystal_cluster",
            p -> new CrystalClusterBlock(0xB98CFF, p),
            p -> p.mapColor(MapColor.COLOR_MAGENTA).noCollision().noOcclusion().strength(0.5f).sound(SoundType.AMETHYST_CLUSTER)
                    .lightLevel(s -> 8).offsetType(BlockBehaviour.OffsetType.XZ).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Kristalllaub: Büschel feiner, leuchtender Splitter an den Kristallbäumen; welkt nicht. */
    public static final DeferredBlock<CrystalFoliageBlock> CRYSTAL_FOLIAGE = ModBlocks.blockWithItem("crystal_foliage",
            CrystalFoliageBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).strength(0.3f).sound(SoundType.AMETHYST_CLUSTER).noOcclusion().lightLevel(s -> 9)
                    .isValidSpawn((state, level, pos, type) -> false).isSuffocating((state, level, pos) -> false)
                    .isViewBlocking((state, level, pos, box) -> false).isRedstoneConductor((state, level, pos) -> false),
            UnaryOperator.identity(), TAB);
    /** Leerenblüte: ein gläserner Kelch, aus dem Sternenfunken steigen. */
    public static final DeferredBlock<VoidBloomBlock> VOID_BLOOM = ModBlocks.blockWithItem("void_bloom", VoidBloomBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.GRASS)
                    .offsetType(BlockBehaviour.OffsetType.XZ).lightLevel(s -> 7).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    /** Sternenranke: lange, leuchtende Strähnen unter den Bruchstücken; die Spitze trägt einen kleinen Stern. */
    public static final DeferredBlock<StarVinesBlock> STAR_VINES = ModBlocks.blockWithItem("star_vines", StarVinesBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).randomTicks().noCollision().instabreak().sound(SoundType.WEEPING_VINES)
                    .lightLevel(s -> 10).pushReaction(PushReaction.POPPED),
            UnaryOperator.identity(), TAB);
    public static final DeferredBlock<StarVinesPlantBlock> STAR_VINES_PLANT = ModBlocks.BLOCKS.registerBlock("star_vines_plant",
            StarVinesPlantBlock::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).noCollision().instabreak().sound(SoundType.WEEPING_VINES).lightLevel(s -> 5)
                    .pushReaction(PushReaction.POPPED));

    // =====================================================================================
    // Der Riss
    // =====================================================================================

    /** Der Leerenriss: ein Spalt in der Welt, hinter dem die Sterne der Leere treiben (ohne Gegenstand). */
    public static final DeferredBlock<VoidRiftBlock> VOID_RIFT = ModBlocks.BLOCKS.registerBlock("void_rift", VoidRiftBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).noCollision().strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 12)
                    .sound(SoundType.AMETHYST).pushReaction(PushReaction.IMMOVEABLE));

    // =====================================================================================
    // Gegenstände
    // =====================================================================================

    /** Leerensplitter: aus den Drusen und Kristallen der Leere – vier ergeben einen Leerenkristall. */
    public static final DeferredItem<Item> VOID_SHARD = item("void_shard", p -> p.rarity(Rarity.UNCOMMON));

    // ---------------------------------------------------------------- Spawn-Eier der Wesen der Leere
    public static final DeferredItem<SpawnEggItem> VOID_MAW_SPAWN_EGG = egg("void_maw", () -> ModEntities.VOID_MAW.get());
    public static final DeferredItem<SpawnEggItem> SHARD_WARDEN_SPAWN_EGG = egg("shard_warden", () -> ModEntities.SHARD_WARDEN.get());
    public static final DeferredItem<SpawnEggItem> DISTORTLING_SPAWN_EGG = egg("distortling", () -> ModEntities.DISTORTLING.get());
    public static final DeferredItem<SpawnEggItem> STAR_RAY_SPAWN_EGG = egg("star_ray", () -> ModEntities.STAR_RAY.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(VOID_MAW_SPAWN_EGG, SHARD_WARDEN_SPAWN_EGG, DISTORTLING_SPAWN_EGG, STAR_RAY_SPAWN_EGG);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        DeferredItem<SpawnEggItem> item = ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new,
                p -> p.spawnEgg(type.get()));
        TAB.add(item);
        return item;
    }

    private static DeferredBlock<Block> stone(String name, SoundType sound) {
        return ModBlocks.blockWithItem(name, Block::new,
                p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                        .strength(2.0f, 6.0f).sound(sound),
                UnaryOperator.identity(), TAB);
    }

    private static DeferredItem<Item> item(String name, UnaryOperator<Item.Properties> properties) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, properties);
        TAB.add(item);
        return item;
    }
}
