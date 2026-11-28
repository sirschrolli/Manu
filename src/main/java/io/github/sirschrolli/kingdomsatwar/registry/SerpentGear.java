package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlock;
import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.serpent.SerpentFangItem;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.Weapon;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Der Schlangentempel und seine Hüterin (seit 1.10): der Schlangenaltar mitten in der Schlangengrube (er ruft Quetzara, die
 * Gefiederte Schlange, und ein Goldblock auf dem leeren Altar ruft sie zurück), der Schlangenzahn (ihre Beute: ein giftiger
 * Dolch mit dem Schlangenstoß), die Quetzalfedern (bessern ihn aus) und ihr Spawn-Ei – im Kreativ-Tab „Legenden“. Quetzara
 * selbst steht in {@link ModEntities}.
 */
public final class SerpentGear {
    private SerpentGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Schlangenaltar: unzerstörbar; auf ihm das goldene Schlangenbild, um ihn schläft Quetzara. */
    public static final DeferredBlock<SerpentAltarBlock> SERPENT_ALTAR = ModBlocks.blockWithItem("serpent_altar", SerpentAltarBlock::new,
            p -> p.mapColor(MapColor.COLOR_GREEN).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.STONE).noOcclusion()
                    .pushReaction(PushReaction.IMMOVEABLE).isValidSpawn((state, level, pos, type) -> false)
                    .lightLevel(state -> switch (state.getValue(SerpentAltarBlock.PHASE)) {
                        case SLEEPING -> 5;
                        case AWAKE -> 11;
                        case FALLEN -> 0;
                    }),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("serpent_altar", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<SerpentAltarBlockEntity>> SERPENT_ALTAR_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("serpent_altar", () -> new BlockEntityType<>(SerpentAltarBlockEntity::new, SERPENT_ALTAR.get()));

    /**
     * Der Schlangenzahn: ein Dolch aus einem Giftzahn Quetzaras – schnell, jeder Treffer vergiftet (wer schon vergiftet ist,
     * den trifft er härter); mit der rechten Maustaste der Schlangenstoß, ein Satz nach vorn mit einem Biss.
     */
    public static final DeferredItem<SerpentFangItem> SERPENT_FANG = ModItems.ITEMS.registerItem("serpent_fang", SerpentFangItem::new,
            () -> new Item.Properties().stacksTo(1).durability(1300).rarity(Rarity.EPIC).enchantable(15)
                    .repairable(ModTags.Items.REPAIRS_SERPENT_FANG)
                    .component(DataComponents.WEAPON, new Weapon(1))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, SerpentFangItem.DAMAGE - 1.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, SerpentFangItem.SPEED - 4.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("serpent_fang", 3)));

    /** Eine Quetzalfeder: smaragdgrün mit goldenem Kiel, schillernd wie ein Kolibri; bessert den Schlangenzahn aus. */
    public static final DeferredItem<Item> QUETZAL_FEATHER = ModItems.ITEMS.registerSimpleItem("quetzal_feather",
            p -> p.rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("quetzal_feather", 1)));

    public static final DeferredItem<SpawnEggItem> QUETZARA_SPAWN_EGG = ModItems.ITEMS.registerItem("quetzara_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.QUETZARA.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(SERPENT_FANG);
        ModItems.RESOURCE_TAB.add(QUETZAL_FEATHER);
        ForgeBlocks.TAB.add(SERPENT_FANG);
        ForgeBlocks.TAB.add(QUETZAL_FEATHER);
        ForgeBlocks.TAB.add(QUETZARA_SPAWN_EGG);
    }
}
