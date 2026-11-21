package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlock;
import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.nether.GlutspeerItem;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Die Legenden: die großen Gegner jenseits der Drachen und was man ihnen abringt – im Kreativ-Tab „Legenden“.
 * <ul>
 *     <li><b>Malleus, der Glutschmied</b> (0.64): der Glutamboss auf dem Dach der Aschenesse, der Glutspeer und die
 *     Glutklumpen.</li>
 * </ul>
 */
public final class ForgeBlocks {
    private ForgeBlocks() {}

    /** Inhalt des Kreativ-Tabs „Legenden“ (in Reihenfolge). */
    static final List<Supplier<? extends ItemLike>> TAB = new ArrayList<>();

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    public static List<Supplier<? extends ItemLike>> tab() {
        return java.util.Collections.unmodifiableList(TAB);
    }

    // =====================================================================================
    // Malleus, der Glutschmied
    // =====================================================================================

    /** Der Glutamboss: weckt Malleus, merkt sich seinen Sieg und ruft ihn (mit einem Netheritbarren) zurück. Unzerstörbar. */
    public static final DeferredBlock<GlutAnvilBlock> GLUT_ANVIL = ModBlocks.blockWithItem("glut_anvil", GlutAnvilBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.ANVIL)
                    .lightLevel(s -> s.getValue(GlutAnvilBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("glut_anvil", 2)), TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<GlutAnvilBlockEntity>> GLUT_ANVIL_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("glut_anvil", () -> new BlockEntityType<>(GlutAnvilBlockEntity::new, GLUT_ANVIL.get()));

    /** Werkstoff des Glutspeers: so hart wie Netherit, ein wenig schärfer; mit Netheritschrott zu reparieren. */
    public static final ToolMaterial GLUT_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2431, 9.0f, 4.5f, 15,
            ModTags.Items.REPAIRS_GLUT_SPEAR);
    /** Der Glutspeer: Meisterstück des Glutschmieds (Stoß und Ansturm wie ein Speer, stärker im Nether, entzündet). */
    public static final DeferredItem<GlutspeerItem> GLUT_SPEAR = add(ModItems.ITEMS.registerItem("glut_spear", GlutspeerItem::new,
            () -> new Item.Properties().spear(GLUT_MATERIAL, 1.15f, 1.3f, 0.4f, 2.5f, 8.5f, 5.5f, 5.1f, 8.5f, 4.6f).rarity(Rarity.EPIC)
                    .fireResistant().component(DataComponents.LORE, ModItems.lore("glut_spear", 3))));
    /** Glutklumpen: geschmolzenes Metall aus der Esse des Glutschmieds; brennt im Ofen so lange wie eine Lohenrute. */
    public static final DeferredItem<Item> MOLTEN_GLOB = add(ModItems.ITEMS.registerSimpleItem("molten_glob",
            p -> p.fireResistant().rarity(Rarity.UNCOMMON)
                    .cookingFuel(net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders.COOKING_TIME_BLAZE_ROD)
                    .component(DataComponents.LORE, ModItems.lore("molten_glob", 1))));

    // ---------------------------------------------------------------- Spawn-Eier
    public static final DeferredItem<SpawnEggItem> GLUTSCHMIED_SPAWN_EGG = egg("glutschmied", () -> ModEntities.GLUTSCHMIED.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(GLUTSCHMIED_SPAWN_EGG);
    }

    /** Schlichte Gegenstände (flaches Modell). */
    public static List<DeferredItem<? extends Item>> flatItems() {
        return List.of(MOLTEN_GLOB);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        TAB.add(item);
        return item;
    }
}
