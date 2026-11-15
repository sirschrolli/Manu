package io.github.sirschrolli.kingdomsatwar.network;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * Spiel an Server: Der Spieler steckt einen Punkt in das Talent {@code talent} (Nummer in {@link HeroLevel.Talent}) – oder
 * verteilt mit {@code talent = -1} alle Talentpunkte (Kampf), mit {@code -2} alle Handwerkspunkte neu. Der Server prüft alles
 * noch einmal.
 */
public record HeroTalentPayload(int talent) implements CustomPacketPayload {
    public static final int RESET = -1;
    /** Die Handwerkspunkte neu verteilen (seit 1.11). */
    public static final int RESET_CRAFT = -2;
    public static final CustomPacketPayload.Type<HeroTalentPayload> TYPE = new CustomPacketPayload.Type<>(KingdomsAtWar.id("hero_talent"));
    public static final StreamCodec<RegistryFriendlyByteBuf, HeroTalentPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, HeroTalentPayload::talent, HeroTalentPayload::new);

    @Override
    public CustomPacketPayload.Type<HeroTalentPayload> type() {
        return TYPE;
    }

    static void handle(HeroTalentPayload payload, IPayloadContext context) {
        if (!(context.player() instanceof ServerPlayer player)) {
            return;
        }
        HeroLevel.Talent[] talents = HeroLevel.Talent.values();
        if (payload.talent() == RESET) {
            HeroLevel.reset(player, HeroLevel.Pot.COMBAT);
        } else if (payload.talent() == RESET_CRAFT) {
            HeroLevel.reset(player, HeroLevel.Pot.CRAFT);
        } else if (payload.talent() >= 0 && payload.talent() < talents.length) {
            HeroLevel.spend(player, talents[payload.talent()]);
        }
    }
}
