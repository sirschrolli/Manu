package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageLocator;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jspecify.annotations.Nullable;

/** Karten des Kartografen: eine Schatzkarte mit Kreuz am Ziel und dem Gelände ringsum. */
final class Maps {
    private Maps() {}

    static ItemStack explorerMap(ServerLevel overworld, BlockPos target, String nameKey) {
        ItemStack map = MapItem.create(overworld, target.getX(), target.getZ(), (byte) 2, true, true);
        MapItem.renderBiomePreviewMap(overworld, map);
        MapItemSavedData.addTargetDecoration(map, target, "+", MapDecorationTypes.TARGET_X);
        map.set(DataComponents.ITEM_NAME, Component.translatable(nameKey));
        return map;
    }

    /** Das nächste Dorf – für einen Dorfbewohner das nächste andere als sein eigenes. */
    static @Nullable BlockPos nearestOtherVillage(ServerLevel overworld, LivingEntity trader) {
        BlockPos center = trader instanceof VillagerEntity villager ? villager.getVillageCenter() : null;
        VillageLocator.Found found = VillageLocator.nearest(overworld, trader.blockPosition(),
                center == null ? null : VillageLocator.startChunk(center), 3);
        return found == null ? null : found.center();
    }

    /** Der nächste Magierturm (gleich welchen Elements). */
    static @Nullable BlockPos nearestMageTower(ServerLevel overworld, BlockPos from) {
        var registry = overworld.registryAccess().lookupOrThrow(Registries.STRUCTURE);
        List<Holder<Structure>> towers = new ArrayList<>();
        for (var type : io.github.sirschrolli.kingdomsatwar.dragon.DragonType.values()) {
            registry.get(ModStructures.mageTower(type)).ifPresent(towers::add);
        }
        if (towers.isEmpty()) {
            return null;
        }
        var found = overworld.getChunkSource().getGenerator().findNearestMapStructure(overworld, HolderSet.direct(towers), from, 12, false);
        return found == null ? null : found.getFirst();
    }
}
