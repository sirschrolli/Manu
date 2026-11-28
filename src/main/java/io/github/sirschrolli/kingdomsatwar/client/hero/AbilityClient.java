package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.ModKeyMappings;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbilities;
import io.github.sirschrolli.kingdomsatwar.hero.HeroAbility;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.network.HeroAbilityPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

/**
 * Die Fähigkeitstaste (seit 1.11): kurz tippen setzt die gewählte Fähigkeit ein, gehalten (eine Drittelsekunde) öffnet das
 * Fähigkeitenrad. Dazu die Anzeige an der Schnellleiste.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class AbilityClient {
    /** So lange (Ticks) muss die Taste gehalten werden, damit das Rad aufgeht. */
    static final int HOLD = 6;

    private static int held;
    private static boolean opened;

    private AbilityClient() {}

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.HOTBAR, AbilityHud.LAYER, AbilityHud::render);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        int clicks = 0;
        while (ModKeyMappings.ABILITY.consumeClick()) {
            clicks++;
        }
        AbilityHud.tick();
        Player player = minecraft.player;
        if (player == null) {
            held = 0;
            opened = false;
            return;
        }
        boolean down = ModKeyMappings.ABILITY.isDown();
        if (down) {
            if (minecraft.gui.screen() == null) {
                held++;
                if (held >= HOLD && !opened && HeroAbility.count(HeroLevel.of(player).level()) > 0) {
                    opened = true;
                    minecraft.gui.setScreen(new AbilityWheelScreen());
                }
            }
        } else {
            if (!opened && (held > 0 || clicks > 0) && minecraft.gui.screen() == null) {
                use(player);
            }
            held = 0;
            opened = false;
        }
    }

    /** Setzt die gewählte Fähigkeit ein (der Server prüft Stufe und Abklingzeit). */
    static void use(Player player) {
        HeroAbility ability = HeroAbilities.state(player).selectedAbility();
        if (!ability.unlocked(HeroLevel.of(player).level())) {
            HeroAbility first = HeroAbility.values()[0];
            player.sendOverlayMessage(Component.translatable("hero.kingdomsatwar.ability.none", first.level()).withStyle(ChatFormatting.GRAY));
            return;
        }
        if (HeroAbilities.cooldown(player, ability) > 0L) {
            AbilityHud.shake();
        }
        ClientPacketDistributor.sendToServer(new HeroAbilityPayload(HeroAbilityPayload.USE, ability.ordinal()));
    }

    /** Legt eine Fähigkeit auf die Taste (sofort angezeigt; der Server bestätigt). */
    public static void select(Player player, HeroAbility ability) {
        HeroAbilities.State state = HeroAbilities.state(player);
        if (state.selectedAbility() != ability) {
            player.setData(ModAttachments.HERO_ABILITIES, new HeroAbilities.State(ability.ordinal(), state.readyAt(), state.introduced()));
            ClientPacketDistributor.sendToServer(new HeroAbilityPayload(HeroAbilityPayload.SELECT, ability.ordinal()));
            AbilityHud.announce(ability);
        }
    }
}
