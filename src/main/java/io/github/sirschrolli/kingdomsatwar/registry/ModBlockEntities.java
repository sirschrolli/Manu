package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Blockentitäten der Mod. */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, KingdomsAtWar.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DragonLairCoreBlockEntity>> DRAGON_LAIR_CORE =
            BLOCK_ENTITIES.register("dragon_lair_core", () -> new BlockEntityType<>(DragonLairCoreBlockEntity::new, ModBlocks.DRAGON_LAIR_CORE.get()));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity>>
            SHADOW_THRONE_CORE = BLOCK_ENTITIES.register("shadow_throne_core",
                    () -> new BlockEntityType<>(io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity::new,
                            ModBlocks.SHADOW_THRONE_CORE.get()));

    /** Magietisch: hält Stab, Zaubertafel und Aetherit. */
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<io.github.sirschrolli.kingdomsatwar.block.entity.MagicTableBlockEntity>>
            MAGIC_TABLE = BLOCK_ENTITIES.register("magic_table",
                    () -> new BlockEntityType<>(io.github.sirschrolli.kingdomsatwar.block.entity.MagicTableBlockEntity::new,
                            ModBlocks.MAGIC_TABLE.get()));

    private ModBlockEntities() {}
}
