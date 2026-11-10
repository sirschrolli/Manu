package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.abyss.AbyssRiftBlock;
import io.github.sirschrolli.kingdomsatwar.block.abyss.GravityCrystalBlock;
import io.github.sirschrolli.kingdomsatwar.block.abyss.GravityCrystalBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.abyss.WorldSeamBlock;
import io.github.sirschrolli.kingdomsatwar.item.RiftKeyItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Blöcke des Abgrunds – des Chaos hinter der Leere, in dem die Bruchstücke aller Welten durcheinandertreiben:
 * <ul>
 *     <li><b>Chaosgestein</b> – ein Trümmergestein, in dem Splitter aller Welten zusammengebacken sind: Netherrack, Endstein,
 *     Sternenmarmor, Schattenstein, Kristall (mit Ziegeln, poliert, rissig und gemeißelt);</li>
 *     <li>die <b>Weltennaht</b> – wo zwei Welten aneinanderstoßen, klafft eine glühende Fuge;</li>
 *     <li><b>Schwebekristall</b> und <b>Hebekristall</b> – Splitter, um die die Schwerkraft verrückt spielt: in ihrer Nähe
 *     schwebt man federleicht bzw. wird sanft nach oben getragen;</li>
 *     <li>der <b>Abgrundriss</b> – das Tor zwischen der Leere und dem Abgrund – und der <b>Rissschlüssel</b>, der es öffnet.</li>
 * </ul>
 */
public final class AbyssBlocks {
    private AbyssBlocks() {}

    /** Inhalt des Kreativ-Tabs „Der Abgrund“ (in Reihenfolge). */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    /** Lädt die Klasse, damit sich alle Blöcke anmelden, bevor die Register gefüllt werden. */
    public static void init() {}

    public static List<Supplier<? extends ItemLike>> tab() {
        return java.util.Collections.unmodifiableList(TAB);
    }

    // =====================================================================================
    // Gestein
    // =====================================================================================

    /** Chaosgestein: dunkles Trümmergestein voller Splitter fremder Welten. */
    public static final BuildingSet CHAOS_STONE = ModBlocks.stoneSet("chaos_stone", "chaos_stone", MapColor.COLOR_BLACK,
            SoundType.DEEPSLATE, 2.0f, TAB);
    public static final BuildingSet CHAOS_STONE_BRICKS = ModBlocks.stoneSet("chaos_stone_bricks", "chaos_stone_brick",
            MapColor.COLOR_BLACK, SoundType.DEEPSLATE_BRICKS, 2.2f, TAB);
    public static final BuildingSet POLISHED_CHAOS_STONE = ModBlocks.stoneSet("polished_chaos_stone", "polished_chaos_stone",
            MapColor.COLOR_BLACK, SoundType.POLISHED_DEEPSLATE, 2.2f, TAB);
    public static final DeferredBlock<Block> CRACKED_CHAOS_STONE_BRICKS = stone("cracked_chaos_stone_bricks", SoundType.DEEPSLATE_BRICKS, 0);
    /** Gemeißeltes Chaosgestein: ein Auge, durch das ein Riss läuft – es glimmt schwach. */
    public static final DeferredBlock<Block> CHISELED_CHAOS_STONE = stone("chiseled_chaos_stone", SoundType.POLISHED_DEEPSLATE, 5);

    /** Weltennaht: wo zwei Welten aneinanderstoßen, glüht eine Fuge aus rotem und weißem Licht. */
    public static final DeferredBlock<WorldSeamBlock> WORLD_SEAM = ModBlocks.blockWithItem("world_seam", WorldSeamBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(2.4f, 8.0f)
                    .sound(SoundType.AMETHYST).lightLevel(s -> 13).emissiveRendering(state -> true),
            p -> p.rarity(Rarity.UNCOMMON), TAB);

    // =====================================================================================
    // Die Kristalle der verdrehten Schwerkraft
    // =====================================================================================

    /** Schwebekristall: in seiner Nähe wiegt man fast nichts – man springt weit und sinkt sanft. */
    public static final DeferredBlock<GravityCrystalBlock> FLOAT_CRYSTAL = ModBlocks.blockWithItem("float_crystal",
            p -> new GravityCrystalBlock(GravityCrystalBlock.Kind.FLOAT, p), AbyssBlocks::crystal, p -> p.rarity(Rarity.UNCOMMON), TAB);
    /** Hebekristall: in seiner Nähe kehrt sich die Schwerkraft um – man wird sanft nach oben getragen. */
    public static final DeferredBlock<GravityCrystalBlock> LIFT_CRYSTAL = ModBlocks.blockWithItem("lift_crystal",
            p -> new GravityCrystalBlock(GravityCrystalBlock.Kind.LIFT, p), AbyssBlocks::crystal, p -> p.rarity(Rarity.UNCOMMON), TAB);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GravityCrystalBlockEntity>> GRAVITY_CRYSTAL_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("gravity_crystal",
                    () -> new BlockEntityType<>(GravityCrystalBlockEntity::new, FLOAT_CRYSTAL.get(), LIFT_CRYSTAL.get()));

    private static BlockBehaviour.Properties crystal(BlockBehaviour.Properties p) {
        return p.mapColor(MapColor.COLOR_LIGHT_BLUE).noOcclusion().strength(1.6f).sound(SoundType.AMETHYST_CLUSTER).lightLevel(s -> 10)
                .requiresCorrectToolForDrops().pushReaction(PushReaction.POPPED).isValidSpawn((state, level, pos, type) -> false)
                .isSuffocating((state, level, pos) -> false).isViewBlocking((state, level, pos, box) -> false)
                .isRedstoneConductor((state, level, pos) -> false);
    }

    // =====================================================================================
    // Der Riss in den Abgrund
    // =====================================================================================

    /** Der Abgrundriss: ein Spalt, hinter dem es rot und schwarz wogt (ohne Gegenstand). */
    public static final DeferredBlock<AbyssRiftBlock> ABYSS_RIFT = ModBlocks.BLOCKS.registerBlock("abyss_rift", AbyssRiftBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).noCollision().strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 12)
                    .sound(SoundType.AMETHYST).pushReaction(PushReaction.IMMOVEABLE));

    /** Himmelsriss: wo die Welt am Himmel gesprungen ist – weißglühend, man fliegt hindurch und verbrennt (ohne Gegenstand). */
    public static final DeferredBlock<io.github.sirschrolli.kingdomsatwar.block.abyss.SkyRiftBlock> SKY_RIFT = ModBlocks.BLOCKS.registerBlock(
            "sky_rift", io.github.sirschrolli.kingdomsatwar.block.abyss.SkyRiftBlock::new,
            p -> p.mapColor(MapColor.COLOR_RED).noCollision().noOcclusion().strength(-1.0f, 3_600_000.0f).noLootTable().lightLevel(s -> 15)
                    .emissiveRendering(state -> true).sound(SoundType.AMETHYST).pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false).isViewBlocking((state, level, pos, box) -> false)
                    .isSuffocating((state, level, pos) -> false).isRedstoneConductor((state, level, pos) -> false));

    // =====================================================================================
    // Gegenstände
    // =====================================================================================

    /**
     * Rissschlüssel: aus der zerbrochenen Krone des Königs geschmiedet – in der Leere reißt er einen Riss in den Abgrund auf
     * (und schließt ihn wieder).
     */
    public static final DeferredItem<RiftKeyItem> RIFT_KEY = item("rift_key", RiftKeyItem::new,
            p -> p.stacksTo(1).rarity(Rarity.EPIC).fireResistant()
                    .component(net.minecraft.core.component.DataComponents.LORE, ModItems.lore("rift_key", 2)));

    private static <T extends Item> DeferredItem<T> item(String name, java.util.function.Function<Item.Properties, T> factory,
            UnaryOperator<Item.Properties> properties) {
        DeferredItem<T> item = ModItems.ITEMS.registerItem(name, factory, properties);
        TAB.add(item);
        return item;
    }

    private static DeferredBlock<Block> stone(String name, SoundType sound, int light) {
        return ModBlocks.blockWithItem(name, Block::new,
                p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                        .strength(2.2f, 6.0f).sound(sound).lightLevel(s -> light),
                UnaryOperator.identity(), TAB);
    }
}
