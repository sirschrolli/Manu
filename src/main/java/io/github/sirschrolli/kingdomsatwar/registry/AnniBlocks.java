package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlock;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.glitch.BannhammerItem;
import java.util.List;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Unit;
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
import net.minecraft.world.item.component.Weapon;
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
 * Anni der Troll (0.67): der Fehlerkern im Herzen der Fehlerzone, die Fehlerblöcke und das Bildrauschen, der Bannhammer,
 * die Fehlerflügel, die Fehlersplitter und ihr Spawn-Ei – im Kreativ-Tab „Legenden“ (nach MissMystik).
 */
public final class AnniBlocks {
    private AnniBlocks() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Der Fehlerkern: lädt Anni, merkt sich ihren Absturz und startet (mit einem Redstoneblock) neu. Unzerstörbar. */
    public static final DeferredBlock<ErrorCoreBlock> ERROR_CORE = ModBlocks.blockWithItem("error_core", ErrorCoreBlock::new,
            p -> p.mapColor(MapColor.COLOR_MAGENTA).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.AMETHYST)
                    .lightLevel(s -> s.getValue(ErrorCoreBlock.PHASE).light).noOcclusion().pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("error_core", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ErrorCoreBlockEntity>> ERROR_CORE_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("error_core", () -> new BlockEntityType<>(ErrorCoreBlockEntity::new, ERROR_CORE.get()));

    /** Fehlerblock: fehlende Textur – Magenta und Schwarz im Schachbrett, schwach leuchtend. */
    public static final DeferredBlock<Block> MISSING_BLOCK = ModBlocks.blockWithItem("missing_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_MAGENTA).strength(1.5f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .lightLevel(s -> 6),
            UnaryOperator.identity(), ForgeBlocks.TAB);
    /** Bildrauschen: ein Block aus flimmerndem Schnee wie bei einem Fernseher ohne Sender (bewegt). */
    public static final DeferredBlock<Block> STATIC_BLOCK = ModBlocks.blockWithItem("static_block", Block::new,
            p -> p.mapColor(MapColor.COLOR_LIGHT_GRAY).strength(1.5f, 6.0f).requiresCorrectToolForDrops().sound(SoundType.AMETHYST)
                    .lightLevel(s -> 4),
            UnaryOperator.identity(), ForgeBlocks.TAB);

    /** Fehlersplitter: ein Splitter kaputter Welt – Beute Annis, repariert Bannhammer und Fehlerflügel. */
    public static final DeferredItem<Item> GLITCH_SHARD = add(ModItems.ITEMS.registerSimpleItem("glitch_shard",
            p -> p.rarity(Rarity.UNCOMMON).component(DataComponents.LORE, ModItems.lore("glitch_shard", 1))));
    /**
     * Der Bannhammer: Annis Hammer – ein Steinklotz mit Eisenbändern an einem Baumstamm, dessen vordere Hälfte fehlt.
     * Schwer und langsam; Treffer können das Ziel zurückrucken lassen, jeder fünfte bannt alles ringsum.
     */
    public static final DeferredItem<BannhammerItem> BANNHAMMER = add(ModItems.ITEMS.registerItem("bannhammer", BannhammerItem::new,
            () -> new Item.Properties().stacksTo(1).durability(2600).rarity(Rarity.EPIC).fireResistant().enchantable(15)
                    .repairable(ModTags.Items.REPAIRS_GLITCH_GEAR)
                    .component(DataComponents.WEAPON, new Weapon(1, 5.0f))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 13.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.2,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_KNOCKBACK, new AttributeModifier(KingdomsAtWar.id("bannhammer_knockback"), 1.2,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("bannhammer", 3))));
    /**
     * Die Fehlerflügel: Flügel aus fehlender Textur mit Löchern und Farbsäumen. Gleiten wie Elytren – dazu im Gleitflug
     * (Springen) das Blinzeln: ein Sprung acht Blöcke nach vorn, ohne den Raum dazwischen (alle zwei Sekunden).
     */
    public static final DeferredItem<Item> GLITCH_WINGS = add(ModItems.ITEMS.registerSimpleItem("glitch_wings",
            p -> p.durability(600).rarity(Rarity.EPIC).fireResistant()
                    .component(DataComponents.GLIDER, Unit.INSTANCE)
                    .component(DataComponents.EQUIPPABLE, Equippable.builder(EquipmentSlot.CHEST)
                            .setEquipSound(SoundEvents.ARMOR_EQUIP_ELYTRA).setAsset(ModEquipment.GLITCH_WINGS_ASSET).setDamageOnHurt(false).build())
                    .repairable(ModTags.Items.REPAIRS_GLITCH_GEAR)
                    .component(DataComponents.LORE, ModItems.lore("glitch_wings", 3))));

    // ---------------------------------------------------------------- Spawn-Ei
    public static final DeferredItem<SpawnEggItem> ANNI_TROLL_SPAWN_EGG = egg("anni_troll", () -> ModEntities.ANNI_TROLL.get());

    public static List<DeferredItem<SpawnEggItem>> spawnEggs() {
        return List.of(ANNI_TROLL_SPAWN_EGG);
    }

    /** Die Gegenstände mit flachem Bild (alles außer dem Bannhammer, der in der Hand ein 3D-Modell ist). */
    public static List<DeferredItem<Item>> flatItems() {
        return List.of(GLITCH_SHARD);
    }

    private static DeferredItem<SpawnEggItem> egg(String name, Supplier<EntityType<? extends Mob>> type) {
        return add(ModItems.ITEMS.registerItem(name + "_spawn_egg", SpawnEggItem::new, p -> p.spawnEgg(type.get())));
    }

    private static <T extends Item> DeferredItem<T> add(DeferredItem<T> item) {
        ForgeBlocks.TAB.add(item);
        return item;
    }
}
