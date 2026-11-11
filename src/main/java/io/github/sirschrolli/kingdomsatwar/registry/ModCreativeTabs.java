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
                        // Baupläne aller Gebäude (sonst gibt es sie im Stadtfenster)
                        for (io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType type
                                : io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType.standard()) {
                            for (int design = 0; design < type.designs(); design++) {
                                output.accept(io.github.sirschrolli.kingdomsatwar.item.BlueprintItem.of(type, design));
                            }
                        }
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

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> SHADOW_WORLD = TABS.register("shadow_world",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.shadow_world"))
                    .icon(() -> ShadowBlocks.NIGHT_CRYSTAL.toStack())
                    .withTabsBefore(NATURE.key())
                    .displayItems((parameters, output) -> accept(output, ModBlocks.SHADOW_TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_VOID = TABS.register("the_void",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.the_void"))
                    .icon(() -> VoidBlocks.VOID_CRYSTAL.toStack())
                    .withTabsBefore(SHADOW_WORLD.key())
                    .displayItems((parameters, output) -> accept(output, VoidBlocks.TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_ABYSS = TABS.register("the_abyss",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.the_abyss"))
                    .icon(() -> AbyssBlocks.WORLD_SEAM.toStack())
                    .withTabsBefore(THE_VOID.key())
                    .displayItems((parameters, output) -> accept(output, AbyssBlocks.TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_SEA = TABS.register("the_sea",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.the_sea"))
                    .icon(() -> SeaBlocks.DEEP_SEA_LANTERN.toStack())
                    .withTabsBefore(THE_ABYSS.key())
                    .displayItems((parameters, output) -> accept(output, SeaBlocks.TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> THE_FAE_REALM = TABS.register("the_fae_realm",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.the_fae_realm"))
                    .icon(() -> FaeBlocks.FAE_DUST.toStack())
                    .withTabsBefore(THE_SEA.key())
                    .displayItems((parameters, output) -> accept(output, FaeBlocks.TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> LEGENDS = TABS.register("legends",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.legends"))
                    .icon(() -> ForgeBlocks.GLUT_SPEAR.toStack())
                    .withTabsBefore(THE_FAE_REALM.key())
                    .displayItems((parameters, output) -> accept(output, ForgeBlocks.TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATION = TABS.register("creation",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.creation"))
                    .icon(() -> CreatorBlocks.CREATOR_ALTAR.toStack())
                    .withTabsBefore(LEGENDS.key())
                    .displayItems((parameters, output) -> accept(output, CreatorBlocks.TAB))
                    .build());

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> FISHING = TABS.register("fishing",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.kingdomsatwar.fishing"))
                    .icon(() -> new net.minecraft.world.item.ItemStack(io.github.sirschrolli.kingdomsatwar.fishing.FishSpecies.GOLDFISH.item()))
                    .withTabsBefore(CREATION.key())
                    .displayItems((parameters, output) -> accept(output, FishingItems.TAB))
                    .build());

    private ModCreativeTabs() {}

    private static void accept(CreativeModeTab.Output output, List<Supplier<? extends ItemLike>> entries) {
        for (Supplier<? extends ItemLike> entry : entries) {
            output.accept(entry.get());
        }
    }
}
