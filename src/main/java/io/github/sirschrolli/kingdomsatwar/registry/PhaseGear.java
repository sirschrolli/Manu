package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.equipment.ArmorMaterial;
import net.minecraft.world.item.equipment.ArmorType;
import net.minecraft.world.item.equipment.EquipmentAsset;
import net.minecraft.world.item.equipment.EquipmentAssets;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DropExperienceBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Phasit (seit 1.4): Auf den äußeren Inseln des Ends, wo die Leerwandler seit Äonen zwischen den Orten hin und her springen,
 * ist der Endstein von ihren Sprüngen durchsetzt. Wo sie ihn am häufigsten kreuzten, ist er zu Kristall geronnen –
 * türkis wie ihr Blick, und er flackert, als wäre er nicht ganz da. Daraus die Rüstung der Leerwandler, die ihren Träger
 * einem Hieb ausweichen lässt (siehe {@link io.github.sirschrolli.kingdomsatwar.item.end.VoidwalkerArmor}).
 */
public final class PhaseGear {
    private PhaseGear() {}

    /** Lädt die Klasse (Registrierung der Blöcke und Gegenstände). */
    public static void init() {}

    // =====================================================================================
    // Phasit
    // =====================================================================================

    /**
     * Phasiterz: Endstein mit Büscheln aus Phasit, die im Dunkeln schimmern (ab einer Diamantspitzhacke). Die Büschel leuchten
     * nur für sich (eigene Leuchtebene); der Block selbst gibt kein Licht, sonst stäche er hell und flach beleuchtet aus dem
     * Endstein heraus.
     */
    public static final DeferredBlock<DropExperienceBlock> PHASIT_ORE = ModBlocks.blockWithItem("phasit_ore",
            p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
            p -> p.mapColor(MapColor.SAND).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(3.5f, 9.0f).sound(SoundType.STONE),
            UnaryOperator.identity(), ModBlocks.RESOURCE_TAB);
    /** Phasit: ein türkiser Kristall, in dem ein violetter Funke wandert; er flackert, als wäre er nicht ganz da. */
    public static final DeferredItem<Item> PHASIT = add(ModItems.ITEMS.registerSimpleItem("phasit",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("phasit", 1))));
    /** Phasitblock: geschliffene Phasitprismen in Purpur gefasst; das Licht darin springt von Stein zu Stein. */
    public static final DeferredBlock<Block> PHASIT_BLOCK = ModBlocks.blockWithItem("phasit_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_CYAN).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops()
                    .strength(5.0f, 9.0f).sound(SoundType.AMETHYST).lightLevel(s -> 8),
            UnaryOperator.identity(), ModBlocks.RESOURCE_TAB);

    // =====================================================================================
    // Die Rüstung der Leerwandler
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> VOIDWALKER_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("voidwalker"));
    /** Phasit in schwarzem Leerenstahl: Schutz wie Diamant, etwas leichter zu verzaubern. */
    public static final ArmorMaterial VOIDWALKER_ARMOR = ModEquipment.material(33, 3, 6, 8, 3, 11, 16, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0f, 0.0f, ModTags.Items.REPAIRS_VOIDWALKER_ARMOR, VOIDWALKER_ASSET);

    /** Die Kapuze mit den leuchtenden Augenschlitzen und der Krone aus Phasitsplittern. */
    public static final DeferredItem<Item> VOIDWALKER_HELMET = armor("voidwalker_helmet", ArmorType.HELMET);
    /** Der lange Mantel mit dem Phasit auf der Brust und den schwebenden Splittern an den Schultern. */
    public static final DeferredItem<Item> VOIDWALKER_CHESTPLATE = armor("voidwalker_chestplate", ArmorType.CHESTPLATE);
    /** Die Beinlinge mit den Phasitknoten an den Knien. */
    public static final DeferredItem<Item> VOIDWALKER_LEGGINGS = armor("voidwalker_leggings", ArmorType.LEGGINGS);
    /** Die Stiefel mit den schwebenden Sohlen. */
    public static final DeferredItem<Item> VOIDWALKER_BOOTS = armor("voidwalker_boots", ArmorType.BOOTS);

    /** Die Rüstung der Leerwandler: Kapuze, Mantel, Beinlinge, Stiefel. */
    public static List<DeferredItem<Item>> voidwalkerSet() {
        return List.of(VOIDWALKER_HELMET, VOIDWALKER_CHESTPLATE, VOIDWALKER_LEGGINGS, VOIDWALKER_BOOTS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(VOIDWALKER_ARMOR, type)
                .rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("voidwalker_armor", 3)));
        ModItems.EQUIPMENT_TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        ModItems.RESOURCE_TAB.add(item);
        return item;
    }
}
