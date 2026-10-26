package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import net.minecraft.world.level.saveddata.maps.MapDecorationTypes;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

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
}
