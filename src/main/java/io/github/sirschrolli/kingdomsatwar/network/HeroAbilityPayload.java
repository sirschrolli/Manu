package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbilities;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbility;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Spiel an Server (seit 1.11): Der Held setzt die Fähigkeit {@code ability} ein ({@link #USE}) oder legt sie auf die
 * Fähigkeitstaste ({@link #SELECT}). Der Server prüft Stufe und Abklingzeit.
 */
public record HeroAbilityPayload(int action, int ability) implements CustomPacketPayload {
    public static final int USE = 0;
    public static final int SELECT = 1;
    public static final CustomPacketPayload.Type<HeroAbilityPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hero_ability"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeroAbilityPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HeroAbilityPayload::action, ByteBufCodecs.VAR_INT, HeroAbilityPayload::ability, HeroAbilityPayload::new);

    @Override
    public CustomPacketPayload.Type<HeroAbilityPayload> type() {
        return TYPE;
    }

    static void handle(HeroAbilityPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player) || payload.ability() < 0 || payload.ability() >= HeroAbility.values().length) {
            return;
        }
        HeroAbility ability = HeroAbility.values()[payload.ability()];
        if (payload.action() == SELECT) {
            HeroAbilities.select(player, ability);
        } else if (payload.action() == USE) {
            HeroAbilities.use(player, ability);
        }
    }
}
