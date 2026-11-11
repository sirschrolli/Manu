package io.github.sirschrolli.kingdomsatwar.relic;

import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.consume_effects.ConsumeEffect;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;

/**
 * Was ein Totem der Mod tut, wenn es den Tod aufhält (als „Wirkung beim Verbrauch“ am Gegenstand, so wie bei Minecraft die
 * Zustände des Totems der Unsterblichkeit): mehr Herzen, eigene Zustände und die Kraft seines Elements. Minecraft hat das
 * Wesen in diesem Augenblick schon auf ein halbes Herz gesetzt und alle Zustände genommen.
 */
public record TotemReviveEffect(TotemKind kind) implements ConsumeEffect {
    public static final MapCodec<TotemReviveEffect> CODEC = TotemKind.CODEC.fieldOf("totem").xmap(TotemReviveEffect::new, TotemReviveEffect::kind);
    public static final StreamCodec<RegistryFriendlyByteBuf, TotemReviveEffect> STREAM_CODEC =
            NeoForgeStreamCodecs.<RegistryFriendlyByteBuf, TotemKind>enumCodec(TotemKind.class).map(TotemReviveEffect::new, TotemReviveEffect::kind);

    @Override
    public ConsumeEffect.Type<TotemReviveEffect> getType() {
        return io.github.sirschrolli.kingdomsatwar.registry.RelicItems.TOTEM_REVIVE.get();
    }

    @Override
    public boolean apply(Level level, ItemStack stack, LivingEntity entity) {
        return Totems.revive(level, stack, entity, this.kind);
    }
}
