package io.github.sirschrolli.kingdomsatwar.loot;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.neoforged.neoforge.common.loot.IGlobalLootModifier;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

/** Die eigenen Arten globaler Loot-Modifier. */
public final class ModLootModifiers {
    public static final DeferredRegister<MapCodec<? extends IGlobalLootModifier>> SERIALIZERS =
            DeferredRegister.create(NeoForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, KingdomsAtWar.MODID);

    /** Karge Truhen (siehe {@link LeanLootModifier}). */
    public static final DeferredHolder<MapCodec<? extends IGlobalLootModifier>, MapCodec<LeanLootModifier>> LEAN =
            SERIALIZERS.register("lean_chests", () -> LeanLootModifier.CODEC);

    private ModLootModifiers() {}
}
