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
 * Hallkristall (seit 1.6): Tief unten, im Reich des Sculk, wo die Stille Kathedrale steht, hallt der Gesang ihrer Orgel bis
 * heute im Gestein nach. Wo er am längsten nachklang, ist der Tiefenschiefer zu Kristall geronnen – blass wie Eis, mit
 * Ringen darin wie Wellen auf einem Teich. Daraus die Rüstung der Lauscher, deren Träger der Tiefenhüter nicht hört (siehe
 * {@link io.github.sirschrolli.kingdomsatwar.item.deep.ListenerArmor}).
 */
public final class EchoGear {
    private EchoGear() {}

    /** Lädt die Klasse (Registrierung der Blöcke und Gegenstände). */
    public static void init() {}

    // =====================================================================================
    // Hallkristall
    // =====================================================================================

    /**
     * Hallkristallerz: Tiefenschiefer mit blassen Kristallen, nur in der Tiefen Dunkelheit (ab einer Diamantspitzhacke). Die
     * Kristalle schimmern für sich (eigene Leuchtebene); der Block selbst gibt kein Licht.
     */
    public static final DeferredBlock<DropExperienceBlock> ECHO_CRYSTAL_ORE = ModBlocks.blockWithItem("echo_crystal_ore",
            p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
            p -> p.mapColor(MapColor.DEEPSLATE).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(4.5f, 9.0f).sound(SoundType.DEEPSLATE),
            UnaryOperator.identity(), ModBlocks.RESOURCE_TAB);
    /** Hallkristall: blass wie Eis, mit Ringen darin wie Wellen; hält man ihn ans Ohr, hört man fernen Gesang. */
    public static final DeferredItem<Item> ECHO_CRYSTAL = add(ModItems.ITEMS.registerSimpleItem("echo_crystal",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("echo_crystal", 1))));
    /** Hallkristallblock: geschliffene Hallkristalle in einem Rahmen aus Tiefenschiefer; Wellen laufen durch ihn hindurch. */
    public static final DeferredBlock<Block> ECHO_CRYSTAL_BLOCK = ModBlocks.blockWithItem("echo_crystal_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_BLUE).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops()
                    .strength(5.0f, 9.0f).sound(SoundType.AMETHYST).lightLevel(s -> 6),
            UnaryOperator.identity(), ModBlocks.RESOURCE_TAB);

    // =====================================================================================
    // Die Rüstung der Lauscher
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> LISTENER_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("listener"));
    /** Hallkristall auf Platten aus Tiefenschiefer: Schutz wie Diamant, etwas leichter zu verzaubern. */
    public static final ArmorMaterial LISTENER_ARMOR = ModEquipment.material(33, 3, 6, 8, 3, 11, 16, SoundEvents.ARMOR_EQUIP_DIAMOND,
            2.0f, 0.0f, ModTags.Items.REPAIRS_LISTENER_ARMOR, LISTENER_ASSET);

    /** Der Helm mit den Fühlern aus Sculk, die jeder Erschütterung nachlauschen. */
    public static final DeferredItem<Item> LISTENER_HELMET = armor("listener_helmet", ArmorType.HELMET);
    /** Der Harnisch mit dem pochenden Sculkherz auf der Brust. */
    public static final DeferredItem<Item> LISTENER_CHESTPLATE = armor("listener_chestplate", ArmorType.CHESTPLATE);
    /** Die Beinschienen mit Hallkristall an den Knien. */
    public static final DeferredItem<Item> LISTENER_LEGGINGS = armor("listener_leggings", ArmorType.LEGGINGS);
    /** Die Stiefel mit Sohlen aus Wolle, die jeden Schritt verschlucken. */
    public static final DeferredItem<Item> LISTENER_BOOTS = armor("listener_boots", ArmorType.BOOTS);

    /** Die Rüstung der Lauscher: Helm, Harnisch, Beinschienen, Stiefel. */
    public static List<DeferredItem<Item>> listenerSet() {
        return List.of(LISTENER_HELMET, LISTENER_CHESTPLATE, LISTENER_LEGGINGS, LISTENER_BOOTS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(LISTENER_ARMOR, type)
                .rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("listener_armor", 4)));
        ModItems.EQUIPMENT_TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        ModItems.RESOURCE_TAB.add(item);
        return item;
    }
}
