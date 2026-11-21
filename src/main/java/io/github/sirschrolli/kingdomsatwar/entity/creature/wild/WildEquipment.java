package io.github.sirschrolli.kingdomsatwar.entity.creature.wild;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.item.equipment.Equippable;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.ModifyDefaultComponentsEvent;

/**
 * Ausrüstung, die Vanilla nur bestimmten Vanilla-Typen erlaubt (Wolfspanzer nur dem Wolf, Teppiche nur Lama und
 * Händlerlama), passt auch den eigenen Gegenstücken: Wo ein Vanilla-Tier in der Liste steht, kommt das eigene dazu.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class WildEquipment {
    private WildEquipment() {}

    private static Map<EntityType<?>, Supplier<? extends EntityType<?>>> counterparts() {
        return Map.of(
                EntityTypes.WOLF, ModEntities.WOLF,
                EntityTypes.LLAMA, ModEntities.LLAMA,
                EntityTypes.TRADER_LLAMA, ModEntities.TRADER_LLAMA,
                EntityTypes.CAMEL, ModEntities.CAMEL);
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    static void onDefaultComponents(ModifyDefaultComponentsEvent event) {
        event.modifyMatching((item, components) -> needsCounterpart(components.get(DataComponents.EQUIPPABLE)), (builder, context, item) -> {
            Equippable equippable = builder.get(DataComponents.EQUIPPABLE);
            if (!needsCounterpart(equippable)) {
                return;
            }
            List<Holder<EntityType<?>>> allowed = new ArrayList<>(equippable.allowedEntities().orElseThrow().stream().toList());
            for (Map.Entry<EntityType<?>, Supplier<? extends EntityType<?>>> entry : counterparts().entrySet()) {
                Holder<EntityType<?>> vanilla = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entry.getKey());
                Holder<EntityType<?>> own = BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(entry.getValue().get());
                if (allowed.contains(vanilla) && !allowed.contains(own)) {
                    allowed.add(own);
                }
            }
            builder.set(DataComponents.EQUIPPABLE, new Equippable(equippable.slot(), equippable.equipSound(), equippable.assetId(),
                    equippable.cameraOverlay(), Optional.of(HolderSet.direct(allowed)), equippable.dispensable(), equippable.swappable(),
                    equippable.damageOnHurt(), equippable.equipOnInteract(), equippable.canBeSheared(), equippable.shearingSound()));
        });
    }

    private static boolean needsCounterpart(Equippable equippable) {
        if (equippable == null || equippable.allowedEntities().isEmpty()) {
            return false;
        }
        HolderSet<EntityType<?>> allowed = equippable.allowedEntities().get();
        if (allowed.unwrapKey().isPresent()) {
            return false;                                       // Tags bekommen die eigenen Tiere über die Tag-Dateien
        }
        return counterparts().keySet().stream().anyMatch(type -> allowed.contains(BuiltInRegistries.ENTITY_TYPE.wrapAsHolder(type)));
    }
}
