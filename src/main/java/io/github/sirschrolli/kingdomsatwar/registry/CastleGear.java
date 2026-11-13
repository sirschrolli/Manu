package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlock;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.castle.MorningStarItem;
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
 * Die Raubritterburg und ihr Herr (seit 1.8): das Rabenbanner, unter dem Kuno von Rabenfels, der Raubritter, Hof hält, der
 * Morgenstern (seine Beute) und sein Spawn-Ei – im Kreativ-Tab „Legenden“. Der Ritter selbst steht in {@link ModEntities}.
 */
public final class CastleGear {
    private CastleGear() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Das Rabenbanner: unzerstörbar; es weht, solange der Raubritter lebt. */
    public static final DeferredBlock<RavenBannerBlock> RAVEN_BANNER = ModBlocks.blockWithItem("raven_banner", RavenBannerBlock::new,
            p -> p.mapColor(MapColor.COLOR_BLACK).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.WOOD).noOcclusion()
                    .pushReaction(PushReaction.IMMOVEABLE).isValidSpawn((state, level, pos, type) -> false),
            ip -> ip.rarity(Rarity.EPIC).component(DataComponents.LORE, ModItems.lore("raven_banner", 2)), ForgeBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<RavenBannerBlockEntity>> RAVEN_BANNER_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("raven_banner", () -> new BlockEntityType<>(RavenBannerBlockEntity::new, RAVEN_BANNER.get()));

    /**
     * Der Morgenstern: sehr schwer, ein Schildbrecher (wie eine Axt); Rechtsklick: der Schmetterschlag (ein Fächer, in dem der
     * Boden aufreißt).
     */
    public static final DeferredItem<MorningStarItem> MORNING_STAR = ModItems.ITEMS.registerItem("morning_star", MorningStarItem::new,
            () -> new Item.Properties().stacksTo(1).durability(1800).rarity(Rarity.EPIC).enchantable(15)
                    .repairable(ModTags.Items.REPAIRS_MORNING_STAR)
                    .component(DataComponents.WEAPON, new Weapon(1, 5.0f))
                    .attributes(ItemAttributeModifiers.builder()
                            .add(Attributes.ATTACK_DAMAGE, new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 9.0,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .add(Attributes.ATTACK_SPEED, new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -3.1,
                                    AttributeModifier.Operation.ADD_VALUE), EquipmentSlotGroup.MAINHAND)
                            .build())
                    .component(DataComponents.LORE, ModItems.lore("morning_star", 3)));
    public static final DeferredItem<SpawnEggItem> ROBBER_KNIGHT_SPAWN_EGG = ModItems.ITEMS.registerItem("robber_knight_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.ROBBER_KNIGHT.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(MORNING_STAR);
        ForgeBlocks.TAB.add(MORNING_STAR);
        ForgeBlocks.TAB.add(ROBBER_KNIGHT_SPAWN_EGG);
    }
}
