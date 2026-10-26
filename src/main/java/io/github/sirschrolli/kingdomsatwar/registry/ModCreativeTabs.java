package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Die Kreativ-Tabs der Mod.
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, KingdomsAtWar.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> BUILDING = TABS.register("building",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.building"))
                    .icon(() -> ModBlocks.RED_ROOF_TILES.stairs().toStack())
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .displayItems((parameters, output) -> accept(output, ModBlocks.BUILDING_TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RESOURCES = TABS.register("resources",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.resources"))
                    .icon(() -> ModItems.DRACONIUM.toStack())
                    .withTabsBefore(BUILDING.key())
                    .displayItems((parameters, output) -> {
                        accept(output, ModItems.RESOURCE_TAB);
                        accept(output, ModBlocks.RESOURCE_TAB);
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> DRAGONS = TABS.register("dragons",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.dragons"))
                    .icon(() -> ModBlocks.DRAGON_EGGS.get(DragonType.FIRE).toStack())
                    .withTabsBefore(RESOURCES.key())
                    .displayItems((parameters, output) -> {
                        accept(output, ModBlocks.DRAGON_TAB);
                        accept(output, ModItems.DRAGON_TAB);
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FARMING = TABS.register("farming",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.farming"))
                    .icon(() -> ModItems.TOMATO.toStack())
                    .withTabsBefore(DRAGONS.key())
                    .displayItems((parameters, output) -> {
                        accept(output, ModItems.FARMING_TAB);
                        accept(output, ModBlocks.FARMING_TAB);
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> KINGDOM = TABS.register("kingdom",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.kingdom"))
                    .icon(() -> ModItems.COMMAND_BATON.toStack())
                    .withTabsBefore(FARMING.key())
                    .displayItems((parameters, output) -> {
                        accept(output, ModBlocks.KINGDOM_TAB);
                        accept(output, ModItems.KINGDOM_TAB);
                    })
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EQUIPMENT = TABS.register("equipment",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.equipment"))
                    .icon(() -> ModItems.DRACONIUM_SWORD.toStack())
                    .withTabsBefore(KINGDOM.key())
                    .displayItems((parameters, output) -> accept(output, ModItems.EQUIPMENT_TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> NATURE = TABS.register("nature",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.nature"))
                    .icon(() -> ModBlocks.GLOWING_VOLCANIC_ROCK.toStack())
                    .withTabsBefore(EQUIPMENT.key())
                    .displayItems((parameters, output) -> accept(output, ModBlocks.NATURE_TAB))
                    .build());

    private ModCreativeTabs() {}

    private static void accept(CreativeModeTab.Output output, List<Supplier<? extends ItemLike>> entries) {
        for (Supplier<? extends ItemLike> entry : entries) {
            output.accept(entry.get());
        }
    }
}
