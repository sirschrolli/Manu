package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.LavaCrustBlock;
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
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Feueropal (seit 1.1.6): Wo im Nether die Hitze das Gestein über Äonen gebrannt hat, ist Kieselgel zu Opal erstarrt – rot
 * und gold wie die Lava, die es umschließt, und doch voller grüner und blauer Blitze. Man findet ihn in kleinen Knollen im
 * Netherrack und im Schwarzstein. Daraus die Rüstung der Glutwandler, die durch das Lavameer schreiten (siehe
 * {@link io.github.sirschrolli.kingdomsatwar.item.nether.EmberwalkerArmor}).
 */
public final class FlameGear {
    private FlameGear() {}

    /** Lädt die Klasse (Registrierung der Blöcke und Gegenstände). */
    public static void init() {}

    // =====================================================================================
    // Feueropal
    // =====================================================================================

    /** Feueropal-Erz: Netherrack mit Knollen aus Feueropal, die schwach glühen (ab einer Diamantspitzhacke). */
    public static final DeferredBlock<DropExperienceBlock> FIRE_OPAL_ORE = ModBlocks.blockWithItem("fire_opal_ore",
            p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
            p -> p.mapColor(MapColor.NETHER).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(3.0f, 3.0f).sound(SoundType.NETHER_ORE).lightLevel(s -> 4),
            UnaryOperator.identity(), ModBlocks.RESOURCE_TAB);
    /** Feueropal-Erz im Schwarzstein (in den Basaltdeltas und um die Hortfesten). */
    public static final DeferredBlock<DropExperienceBlock> BLACKSTONE_FIRE_OPAL_ORE = ModBlocks.blockWithItem("blackstone_fire_opal_ore",
            p -> new DropExperienceBlock(UniformInt.of(3, 7), p),
            p -> p.mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops()
                    .strength(3.5f, 6.0f).sound(SoundType.GILDED_BLACKSTONE).lightLevel(s -> 4),
            UnaryOperator.identity(), ModBlocks.RESOURCE_TAB);
    /** Feueropal: rot und gold wie Lava, darin das Farbenspiel des Opals; verbrennt nicht. */
    public static final DeferredItem<Item> FIRE_OPAL = add(ModItems.ITEMS.registerSimpleItem("fire_opal",
            p -> p.rarity(Rarity.UNCOMMON).fireResistant().component(DataComponents.LORE, ModItems.lore("fire_opal", 1))));
    /** Feueropalblock: geschliffene Feueropale in Schwarzgold gefasst; das Feuer darin wogt und leuchtet. */
    public static final DeferredBlock<Block> FIRE_OPAL_BLOCK = ModBlocks.blockWithItem("fire_opal_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_ORANGE).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops()
                    .strength(5.0f, 6.0f).sound(SoundType.AMETHYST).lightLevel(s -> 9),
            Item.Properties::fireResistant, ModBlocks.RESOURCE_TAB);

    // =====================================================================================
    // Die Rüstung der Glutwandler
    // =====================================================================================

    public static final ResourceKey<EquipmentAsset> EMBERWALKER_ASSET = ResourceKey.create(EquipmentAssets.ROOT_ID, KingdomsAtWar.id("emberwalker"));
    /** Feueropal in geschwärztem Stahl: Schutz wie Diamant, etwas härter; verbrennt nicht. */
    public static final ArmorMaterial EMBERWALKER_ARMOR = ModEquipment.material(35, 3, 6, 8, 3, 11, 13, SoundEvents.ARMOR_EQUIP_NETHERITE,
            2.5f, 0.05f, ModTags.Items.REPAIRS_EMBERWALKER_ARMOR, EMBERWALKER_ASSET);

    /** Der Helm mit dem Flammenkamm und dem glühenden Sehschlitz: klare Sicht in der Lava. */
    public static final DeferredItem<Item> EMBERWALKER_HELMET = armor("emberwalker_helmet", ArmorType.HELMET);
    /** Der Harnisch mit dem großen Feueropal auf der Brust. */
    public static final DeferredItem<Item> EMBERWALKER_CHESTPLATE = armor("emberwalker_chestplate", ArmorType.CHESTPLATE);
    /** Die Beinschienen mit der Opalschnalle und den Kniesteinen. */
    public static final DeferredItem<Item> EMBERWALKER_LEGGINGS = armor("emberwalker_leggings", ArmorType.LEGGINGS);
    /** Die Stiefel mit den Glutsohlen: Wo sie Lava betreten, erstarrt sie zu einer Kruste. */
    public static final DeferredItem<Item> EMBERWALKER_BOOTS = armor("emberwalker_boots", ArmorType.BOOTS);

    /** Die Rüstung der Glutwandler: Helm, Harnisch, Beinschienen, Stiefel. */
    public static List<DeferredItem<Item>> emberwalkerSet() {
        return List.of(EMBERWALKER_HELMET, EMBERWALKER_CHESTPLATE, EMBERWALKER_LEGGINGS, EMBERWALKER_BOOTS);
    }

    /**
     * Glutkruste: Wo die Stiefel der Glutwandler Lava betreten, erstarrt sie zu einer dünnen Kruste, die trägt; nach ein paar
     * Sekunden bricht sie auf und schmilzt wieder (so lange jemand mit den Stiefeln darauf steht, hält sie). Kein Gegenstand.
     */
    public static final DeferredBlock<LavaCrustBlock> LAVA_CRUST = ModBlocks.BLOCKS.registerBlock("lava_crust", LavaCrustBlock::new,
            () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).strength(0.5f)
                    .sound(SoundType.BASALT).randomTicks().noLootTable().pushReaction(PushReaction.IMMOVEABLE)
                    .lightLevel(s -> 3 + 3 * s.getValue(LavaCrustBlock.AGE))
                    .isValidSpawn((state, level, pos, type) -> false));

    private static DeferredItem<Item> armor(String name, ArmorType type) {
        DeferredItem<Item> item = ModItems.ITEMS.registerSimpleItem(name, p -> p.humanoidArmor(EMBERWALKER_ARMOR, type).fireResistant()
                .rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("emberwalker_armor", 3)));
        ModItems.EQUIPMENT_TAB.add(item);
        return item;
    }

    private static DeferredItem<Item> add(DeferredItem<Item> item) {
        ModItems.RESOURCE_TAB.add(item);
        return item;
    }
}
