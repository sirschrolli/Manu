package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.fae.MysticDeckItem;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.equipment.Equippable;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * MissMystik, die Feen-Zauberkünstlerin (0.66): der große Zauberzylinder auf der Zauberbühne, die Samtvorhänge der Bühne,
 * das Kartendeck der Mystik, der Zylinder der Mystik und ihr Spawn-Ei – im Kreativ-Tab „Legenden“ (nach Asteria); die
 * Vorhänge stehen im Tab des Feenreichs.
 */
public final class MystikBlocks {
    private MystikBlocks() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der große Zauberzylinder: darin wartet MissMystik; leer ruft Feenstaub sie zurück. Unzerstörbar. */
    public static final DeferredBlock<StageHatBlock> STAGE_HAT = ModBlocks.blockWithItem("stage_hat", StageHatBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.WOOL)
                    .lightLevel(s -> s.getValue(StageHatBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("stage_hat", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<StageHatBlockEntity>> STAGE_HAT_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("stage_hat", () -> new BlockEntityType<>(StageHatBlockEntity::new, STAGE_HAT.get()));

    /** Samtvorhang: schwerer, tiefvioletter Samt in Falten (die Vorhänge der Zauberbühne). */
    public static final DeferredBlock<Block> VELVET_CURTAIN = ModBlocks.blockWithItem("velvet_curtain", Block::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(0.8f).sound(SoundType.WOOL).ignitedByLava(),
            UnaryOperator.identity(), FaeBlocks.TAB);
    /** Samtvorhang mit goldener Fransenborte (der untere Saum). */
    public static final DeferredBlock<Block> VELVET_CURTAIN_FRINGE = ModBlocks.blockWithItem("velvet_curtain_fringe", Block::new,
            p -> p.mapColor(MapColor.COLOR_PURPLE).strength(0.8f).sound(SoundType.WOOL).ignitedByLava(),
            UnaryOperator.identity(), FaeBlocks.TAB);

    /** Das Kartendeck der Mystik: wirft Fächer aus Zauberkarten (jeder fünfte Wurf: ein Ass im Ärmel). */
    public static final DeferredItem<MysticDeckItem> MYSTIC_DECK = add(ModItems.ITEMS.registerItem("mystic_deck", MysticDeckItem::new,
            () -> new Item.Properties().stacksTo(1).durability(750).rarity(Rarity.EPIC).enchantable(15).repairable(ModTags.Items.REPAIRS_MYSTIC_GEAR)
                    .component(DataComponents.LORE, ModItems.lore("mystic_deck", 3))));
    /**
     * Der Zylinder der Mystik: MissMystiks Hut, auf dem Kopf zu tragen (als echter Zylinder). Schützt wie ein guter Helm,
     * bringt Glück – und sinkt das Leben seines Trägers unter ein knappes Drittel, verschwindet er in einer Rauchwolke
     * (Verschwindetrick, siehe {@link io.github.sirschrolli.kingdomsatwar.item.fae.MysticTopHatEffects}).
     */
    public static final DeferredItem<Item> MYSTIC_TOP_HAT = add(ModItems.ITEMS.registerSimpleItem("mystic_top_hat",
            p -> p.stacksTo(1).durability(520).rarity(Rarity.EPIC).enchantable(15).repairable(ModTags.Items.REPAIRS_MYSTIC_GEAR)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.HEAD).setEquipSound(SoundEvents.ARMOR_EQUIP_LEATHER)
                            .build())
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ARMOR, new AttributeModifier(KingdomsAtWar.id("mystic_top_hat_armor"), 3.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                            .add(Attributes.ARMOR_TOUGHNESS, new AttributeModifier(KingdomsAtWar.id("mystic_top_hat_toughness"), 2.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                            .add(Attributes.LUCK, new AttributeModifier(KingdomsAtWar.id("mystic_top_hat_luck"), 1.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.HEAD)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("mystic_top_hat", 3))));

    // ---------------------------------------------------------------- Spawn-Ei
    public static final DeferredItem<SpawnEggItem> MISS_MYSTIK_SPAWN_EGG = egg("miss_mystik", () -> ModEntities.MISS_MYSTIK.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(MISS_MYSTIK_SPAWN_EGG);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        ForgeBlocks.TAB.add(item);
        return item;
    }
}
