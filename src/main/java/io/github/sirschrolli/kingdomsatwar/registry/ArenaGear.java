package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlock;
import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.arena.GladiatorNetBlock;
import io.github.sirschrolli.kingdomsatwar.item.arena.GladiatorNetItem;
import io.github.sirschrolli.kingdomsatwar.item.arena.StormTridentItem;
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
 * Die Arena der Prüfungen und ihr Meister (seit 1.9): das Arenasiegel unter dem Spawner der Böen (es zählt die bezwungenen
 * Käfige und ruft Ventor, den Gladiator der Winde), das Netz, das ihn gefangen hält, wen es trifft, der Sturmdreizack und das
 * Wurfnetz (seine Beute) und sein Spawn-Ei – im Kreativ-Tab „Legenden“. Ventor selbst steht in {@link ModEntities}.
 */
public final class ArenaGear {
    private ArenaGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Das Arenasiegel: unzerstörbar; es zählt die Käfige, die bezwungen sind, und ruft den Meister der Arena. */
    public static final DeferredBlock<ArenaSealBlock> ARENA_SEAL = ModBlocks.blockWithItem("arena_seal", ArenaSealBlock::new,
            p -> p.mapColor(MapColor.SAND).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.STONE)
                    .pushReaction(PushReaction.IMMOVEABLE).lightLevel(state -> 6).isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("arena_seal", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ArenaSealBlockEntity>> ARENA_SEAL_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("arena_seal", () -> new BlockEntityType<>(ArenaSealBlockEntity::new, ARENA_SEAL.get()));

    /** Ein Wurfnetz, das sich um jemanden gelegt hat: Wer darin steckt, kommt kaum vom Fleck; es löst sich bald von selbst. */
    public static final DeferredBlock<GladiatorNetBlock> GLADIATOR_NET_BLOCK = ModBlocks.BLOCKS.registerBlock("gladiator_net",
            GladiatorNetBlock::new, () -> net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().mapColor(MapColor.WOOL)
                    .noCollision().strength(0.3f).sound(SoundType.WOOL).noLootTable().noOcclusion().pushReaction(PushReaction.POPPED)
                    .replaceable().isValidSpawn((state, level, pos, type) -> false));

    /**
     * Der Sturmdreizack: im Nahkampf wie ein schwerer Dreizack; gehalten und losgelassen fliegt er davon, entfesselt beim
     * Aufschlag eine Böe und kehrt von selbst in die Hand zurück.
     */
    public static final DeferredItem<StormTridentItem> STORM_TRIDENT = ModItems.ITEMS.registerItem("storm_trident", StormTridentItem::new,
            () -> new Item.Properties().stacksTo(1).durability(1400).rarity(Rarity.EPIC).enchantable(15)
                    .repairable(ModTags.Items.REPAIRS_STORM_TRIDENT)
                    .component(DataComponents.WEAPON, new Weapon(1))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 9.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.9,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("storm_trident", 3)));

    /** Das Wurfnetz eines Gladiators: geworfen fängt es ein Wesen ein (es kommt eine Weile kaum vom Fleck). */
    public static final DeferredItem<GladiatorNetItem> GLADIATOR_NET = ModItems.ITEMS.registerItem("gladiator_net", GladiatorNetItem::new,
            () -> new Item.Properties().stacksTo(16).rarity(Rarity.RARE).component(DataComponents.LORE, ModItems.lore("gladiator_net", 2)));

    public static final DeferredItem<SpawnEggItem> VENTOR_SPAWN_EGG = ModItems.ITEMS.registerItem("ventor_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.VENTOR.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(STORM_TRIDENT);
        ModItems.EQUIPMENT_TAB.add(GLADIATOR_NET);
        ForgeBlocks.TAB.add(STORM_TRIDENT);
        ForgeBlocks.TAB.add(GLADIATOR_NET);
        ForgeBlocks.TAB.add(VENTOR_SPAWN_EGG);
    }
}
