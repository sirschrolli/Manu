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
 * Chronit (seit 1.1): Im Abgrund steht die Zeit still – und wo zwei Welten aneinanderstoßen, an den glühenden Weltennähten,
 * gerinnt sie zu Kristall. Chronit ist bernsteinfarben, in seinem Innern hängen erstarrte Funken wie Staub in einem
 * Sonnenstrahl. Daraus die Rüstung der Zeitwächter, die ihren Träger einmal vor dem Tod zurückspult (siehe
 * {@link io.github.sirschrolli.kingdomsatwar.item.abyss.TimewardenArmor}).
 */
public final class AbyssGear {
    private AbyssGear() {}

    /** Lädt die Klasse (Registrierung der Blöcke und Gegenstände). */
    public static void init() {}

    // =====================================================================================
    // Chronit
    // =====================================================================================

    /** Chronit-Erz: Chaosgestein mit bernsteinfarbenen Kristallen, in denen Funken stehen (ab einer Diamantspitzhacke). */
    public static final DeferredBlock<DropExperienceBlock> CHRONIT_ORE = ModBlocks.blockWithItem("chronit_ore",
            p -> new DropExperienceBlock(UniformInt.of(4, 8), p),
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(4.5f, 9.0f).sound(SoundType.DEEPSLATE).lightLevel(s -> 5),
            UnaryOperator.identity(), AbyssBlocks.TAB);
    /** Chronit: ein Kristall aus geronnener Zeit, bernsteinfarben, mit erstarrten Funken im Innern. */
    public static final DeferredItem<Item> CHRONIT = add(ModItems.ITEMS.registerSimpleItem("chronit",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("chronit", 1))));
    /** Chronitblock: ein geschliffenes Zifferblatt aus Chronit, über das langsam ein Lichtzeiger wandert. */
    public static final DeferredBlock<Block> CHRONIT_BLOCK = ModBlocks.blockWithItem("chronit_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.BELL).requiresCorrectToolForDrops()
                    .strength(5.5f, 9.0f).sound(SoundType.AMETHYST).lightLevel(s -> 6),
            UnaryOperator.identity(), AbyssBlocks.TAB);

    // =====================================================================================
    // Die Rüstung der Zeitwächter
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> TIMEWARDEN_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("timewarden"));
    /** Chronit in Uhrbronze: Schutz und Härte wie Netherit. */
    public static final ArmorMaterial TIMEWARDEN_ARMOR = ModEquipment.material(37, 3, 6, 8, 3, 11, 15, SoundEvents.ARMOR_EQUIP_NETHERITE,
            3.0f, 0.1f, ModTags.Items.REPAIRS_TIMEWARDEN_ARMOR, TIMEWARDEN_ASSET);

    /** Der Helm mit dem Zeigerkamm und den Zahnrädern an den Schläfen. */
    public static final DeferredItem<Item> TIMEWARDEN_HELMET = armor("timewarden_helmet", ArmorType.HELMET);
    /** Der Harnisch mit dem Zifferblatt auf der Brust und dem Pendel im Rücken. */
    public static final DeferredItem<Item> TIMEWARDEN_CHESTPLATE = armor("timewarden_chestplate", ArmorType.CHESTPLATE);
    /** Die Beinschienen mit dem Gewicht eines Pendels am Gürtel. */
    public static final DeferredItem<Item> TIMEWARDEN_LEGGINGS = armor("timewarden_leggings", ArmorType.LEGGINGS);
    /** Die Stiefel mit den kleinen Zahnrädern an den Knöcheln. */
    public static final DeferredItem<Item> TIMEWARDEN_BOOTS = armor("timewarden_boots", ArmorType.BOOTS);

    /** Die Rüstung der Zeitwächter: Helm, Harnisch, Beinschienen, Stiefel. */
    public static List<DeferredItem<Item>> timewardenSet() {
        return List.of(TIMEWARDEN_HELMET, TIMEWARDEN_CHESTPLATE, TIMEWARDEN_LEGGINGS, TIMEWARDEN_BOOTS);
    }

    private static DeferredItem<Item> armor(String name, ArmorType type) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(TIMEWARDEN_ARMOR, type).fireResistant()
                .rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("timewarden_armor", 3)));
        ModItems.EQUIPMENT_TAB.add(item);
        AbyssBlocks.TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        AbyssBlocks.TAB.add(item);
        return item;
    }
}
