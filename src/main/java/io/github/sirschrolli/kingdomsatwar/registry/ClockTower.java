package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlock;
import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.abyss.MinuteHandItem;
import java.util.function.UnaryOperator;
import net.minecraft.core.component.DataComponents;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.ToolMaterial;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

/**
 * Der Uhrturm und sein Stundenwächter (seit 1.1): Uhrbronze (mit Treppe, Stufe und Mauer), Uhrwerk, Zifferblatt, das
 * Uhrwerkherz in der Mitte des großen Zifferblatts, über dem der Stundenwächter erstarrt steht, der Minutenzeiger (seine
 * Beute) und sein Spawn-Ei. Der Wächter selbst, seine Zahnräder und das Pendel stehen in {@link ModEntities}.
 */
public final class ClockTower {
    private ClockTower() {}

    /** Lädt die Klasse, damit sich alles anmeldet, bevor die Register gefüllt werden. */
    public static void init() {}

    /** Uhrbronze: polierte, warm glänzende Bronze mit Nieten – Ränder, Zeiger, Zahnräder und die Glocke des Uhrturms. */
    public static final BuildingSet CLOCK_BRONZE = ModBlocks.buildingSet("clock_bronze", "clock_bronze",
            p -> p.mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BELL).requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f).sound(SoundType.COPPER),
            true, BuildingSet.Tool.PICKAXE, AbyssBlocks.TAB);
    /** Uhrwerk: Bronzeplatten, hinter denen Zahnräder ineinandergreifen und sich langsam drehen. */
    public static final DeferredBlock<Block> CLOCKWORK = ModBlocks.blockWithItem("clockwork", Block::new,
            p -> p.mapColor(MapColor.TERRACOTTA_ORANGE).instrument(NoteBlockInstrument.BELL).requiresCorrectToolForDrops()
                    .strength(4.0f, 8.0f).sound(SoundType.COPPER_GRATE),
            UnaryOperator.identity(), AbyssBlocks.TAB);
    /** Zifferblatt: elfenbeinfarbenes Email auf Stein, fein graviert – der Boden des großen Zifferblatts. */
    public static final DeferredBlock<Block> CLOCK_FACE = ModBlocks.blockWithItem("clock_face", Block::new,
            p -> p.mapColor(MapColor.QUARTZ).instrument(NoteBlockInstrument.CHIME).requiresCorrectToolForDrops()
                    .strength(2.5f, 6.0f).sound(SoundType.CALCITE),
            UnaryOperator.identity(), AbyssBlocks.TAB);
    /** Das Uhrwerkherz: unzerstörbar; es tickt, solange der Stundenwächter lebt. */
    public static final DeferredBlock<ClockHeartBlock> CLOCK_HEART = ModBlocks.blockWithItem("clock_heart", ClockHeartBlock::new,
            p -> p.mapColor(MapColor.TERRACOTTA_ORANGE).strength(-1.0f, 3_600_000.0f).noLootTable().sound(SoundType.COPPER)
                    .lightLevel(s -> s.getValue(ClockHeartBlock.PHASE).light).pushReaction(PushReaction.IMMOVEABLE)
                    .isValidSpawn((state, level, pos, type) -> false),
            p -> p.rarity(Rarity.EPIC), AbyssBlocks.TAB);
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ClockHeartBlockEntity>> CLOCK_HEART_ENTITY =
            ModBlockEntities.BLOCK_ENTITIES.register("clock_heart", () -> new BlockEntityType<>(ClockHeartBlockEntity::new, CLOCK_HEART.get()));

    /** Chronit in Uhrbronze: zäher als Netherit, Härte wie die Diamantspitzhacke. */
    public static final ToolMaterial MINUTE_HAND_MATERIAL = new ToolMaterial(BlockTags.INCORRECT_FOR_NETHERITE_TOOL, 2400, 9.0f, 5.0f, 18,
            ModTags.Items.REPAIRS_MINUTE_HAND);
    /** Der Minutenzeiger: die Beute des Stundenwächters (jeder zwölfte Treffer schlägt die Stunde, Rechtsklick: Zeitsprung). */
    public static final DeferredItem<MinuteHandItem> MINUTE_HAND = ModItems.ITEMS.registerItem("minute_hand", MinuteHandItem::new,
            () -> new Item.Properties().sword(MINUTE_HAND_MATERIAL, 4.0f, -2.6f).fireResistant().rarity(Rarity.EPIC)
                    .component(DataComponents.LORE, ModItems.lore("minute_hand", 3)));
    public static final DeferredItem<SpawnEggItem> HOUR_WARDEN_SPAWN_EGG = ModItems.ITEMS.registerItem("hour_warden_spawn_egg",
            SpawnEggItem::new, p -> p.spawnEgg(ModEntities.HOUR_WARDEN.get()));

    static {
        ModItems.EQUIPMENT_TAB.add(MINUTE_HAND);
        AbyssBlocks.TAB.add(MINUTE_HAND);
        AbyssBlocks.TAB.add(HOUR_WARDEN_SPAWN_EGG);
    }
}
