package io.github.sirschrolli.kingdomsatwar.client;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.network.DragonBreathPayload;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

/** Tastenbelegung der Mod (änderbar unter Optionen → Steuerung → Tastenbelegung). */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class ModKeyMappings {
    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(KingdomsAtWar.id("dragons"));
    /** Drachenodem beim Reiten (Standard: R). */
    public static final KeyMapping DRAGON_BREATH = new KeyMapping("key.kingdomsatwar.dragon_breath", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_R, CATEGORY);
    public static final KeyMapping.Category MAGIC = new KeyMapping.Category(KingdomsAtWar.id("magic"));
    /** Zauberkreis: gedrückt halten, Zauber mit der Maus wählen, loslassen (Standard: V). */
    public static final KeyMapping SPELL_WHEEL = new KeyMapping("key.kingdomsatwar.spell_wheel", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_V, MAGIC);

    public static final KeyMapping.Category HERO = new KeyMapping.Category(KingdomsAtWar.id("hero"));
    /** Der Weg zur Legende (Heldenstufe als Pfad), von dort Talente und Werte (Standard: K). */
    public static final KeyMapping CHARACTER = new KeyMapping("key.kingdomsatwar.character", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_K, HERO);
    /** Streicheln, die Belohnung der Legende: das Wesen vor einem (Standard: G; gedrückt halten streichelt weiter). */
    public static final KeyMapping PAT = new KeyMapping("key.kingdomsatwar.pat", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_G, HERO);
    /** Die Chronik des Siegels aufschlagen – seit 0.79 ohne Buch im Gepäck (Standard: J). */
    public static final KeyMapping CHRONICLE = new KeyMapping("key.kingdomsatwar.chronicle", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_J, HERO);

    /** Das Fischbuch aufschlagen (Standard: H). */
    public static final KeyMapping FISH_BOOK = new KeyMapping("key.kingdomsatwar.fish_book", KeyConflictContext.IN_GAME,
            InputConstants.Type.KEYBOARD, InputConstants.KEY_H, HERO);

    private ModKeyMappings() {}

    @SubscribeEvent
    static void register(RegisterKeyMappingsEvent event) {
        event.registerCategory(CATEGORY);
        event.register(DRAGON_BREATH);
        event.registerCategory(MAGIC);
        event.register(SPELL_WHEEL);
        event.registerCategory(HERO);
        event.register(CHARACTER);
        event.register(PAT);
        event.register(CHRONICLE);
        event.register(FISH_BOOK);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        while (CHARACTER.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null) {
                minecraft.gui.setScreen(new io.github.sirschrolli.kingdomsatwar.client.hero.HeroPathScreen());
            }
        }
        while (CHRONICLE.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null) {
                ClientPacketDistributor.sendToServer(io.github.sirschrolli.kingdomsatwar.network.RequestChroniclePayload.INSTANCE);
            }
        }
        while (FISH_BOOK.consumeClick()) {
            if (minecraft.player != null && minecraft.gui.screen() == null) {
                ClientPacketDistributor.sendToServer(io.github.sirschrolli.kingdomsatwar.network.RequestFishBookPayload.INSTANCE);
            }
        }
        while (DRAGON_BREATH.consumeClick()) {
            if (minecraft.player != null && minecraft.player.getVehicle() instanceof DragonEntity dragon
                    && dragon.getControllingPassenger() == minecraft.player) {
                ClientPacketDistributor.sendToServer(DragonBreathPayload.INSTANCE);
            }
        }
    }
}
