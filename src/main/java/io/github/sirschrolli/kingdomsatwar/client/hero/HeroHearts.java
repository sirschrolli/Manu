package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModClientConfig;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Hud;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.GuiLayer;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

/**
 * Heldenherzen (seit 0.77): Ein Held kann mit Herzkristallen, Heldenkraft und Lebenskraft über siebzig Herzen haben – Minecraft
 * stapelt sie zu einer Wand aus sieben, acht Herzreihen. Ab {@value #COMPACT_FROM} Herzen zeigt die Leiste deshalb eine
 * einzige Reihe in <b>Schichten</b>: Die ersten zehn Herzen sind rot wie gewohnt, jede weitere Schicht von zehn hat ihre
 * Farbe (Orange, Rosa, Violett, Blau, Türkis, Smaragd, Silber), die oberste gefüllte liegt über der darunter, und links
 * daneben steht, wie viele Herzen es gerade sind. Die Absorption liegt als goldene Reihe darüber. Gift, Verdorren und Frost
 * färben die Herzen wie gewohnt, beim Treffer blinken sie (das verlorene Leben leuchtet kurz hell nach), mit Regeneration
 * läuft die Welle über die Reihe, bei wenig Leben zittern sie. Abschaltbar in den Einstellungen ("Herzen in Schichten").
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class HeroHearts {
    /** Ab so vielen Herzen (Herzrahmen) zeigt die Leiste Schichten statt Reihen. */
    public static final int COMPACT_FROM = 21;
    /** Die Farben der Schichten ab der zweiten (die erste ist das gewohnte Rot); danach beginnen sie von vorn. */
    private static final int[] TIERS = {0xFFFFA040, 0xFFFF5CAA, 0xFFB46CFF, 0xFF5A90FF, 0xFF3FE0F0, 0xFF46E07E, 0xFFF4F4FF};
    private static final int RED = 0xFFFF5A5A;
    private static final int GOLDEN = 0xFFF2C14E;
    private static final Identifier FULL = KingdomsAtWar.id("hero/heart/full");
    private static final Identifier HALF = KingdomsAtWar.id("hero/heart/half");
    private static final Identifier SHINE = KingdomsAtWar.id("hero/heart/shine");

    private static final RandomSource RANDOM = RandomSource.create();
    private static int lastHealth;
    private static int displayHealth;
    private static long lastHealthTime;
    private static long blinkUntil;

    private HeroHearts() {}

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.wrapLayer(VanillaGuiLayers.PLAYER_HEALTH, vanilla -> (GuiLayer) (g, delta) -> {
            if (!render(g, delta)) {
                vanilla.render(g, delta);
            }
        });
    }

    /** Die Farbe einer Schicht (0: das gewohnte Rot). */
    public static int tierColor(int tier) {
        return tier <= 0 ? RED : TIERS[(tier - 1) % TIERS.length];
    }

    /** Ob die Leiste dieses Spielers in Schichten gezeigt wird. */
    public static boolean compact(Player player) {
        return ModClientConfig.HERO_HEARTS.get() && Mth.ceil(player.getAttributeValue(Attributes.MAX_HEALTH) / 2.0) >= COMPACT_FROM;
    }

    /** Zeichnet die Leiste in Schichten; {@code false}: Minecraft zeichnet sie wie gewohnt. */
    private static boolean render(GuiGraphicsExtractor g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (!(mc.getCameraEntity() instanceof Player player) || !compact(player)) {
            return false;
        }
        Hud hud = mc.gui.hud;
        if (hud.isHidden() || mc.gameMode == null || !mc.gameMode.canHurtPlayer()) {
            return true;
        }
        int ticks = hud.getGuiTicks();
        int current = Mth.ceil(player.getHealth());
        long now = Util.getMillis();
        // wie bei Minecraft: nach einem Treffer blinkt die Leiste, das verlorene Leben leuchtet noch eine Sekunde nach
        if (current < lastHealth && player.damageCooldownTime > 0) {
            lastHealthTime = now;
            blinkUntil = ticks + 20L;
        } else if (current > lastHealth && player.damageCooldownTime > 0) {
            lastHealthTime = now;
            blinkUntil = ticks + 10L;
        }
        if (now - lastHealthTime > 1000L || blinkUntil - ticks > 20L) {
            displayHealth = current;
            lastHealthTime = now;
            blinkUntil = Math.min(blinkUntil, ticks + 20L);
        }
        lastHealth = current;
        int old = displayHealth;
        boolean blink = blinkUntil > ticks && (blinkUntil - ticks) / 3L % 2L == 1L;
        RANDOM.setSeed(ticks * 312871L);

        int xLeft = g.guiWidth() / 2 - 91;
        int yBase = g.guiHeight() - hud.leftHeight;
        int absorption = Mth.ceil(player.getAbsorptionAmount());
        hud.leftHeight += absorption > 0 ? 20 : 10;
        Hud.HeartType type = ClientHooks.firePlayerHeartTypeEvent(player, player.hasEffect(MobEffects.POISON) ? Hud.HeartType.POISIONED
                : player.hasEffect(MobEffects.WITHER) ? Hud.HeartType.WITHERED : player.isFullyFrozen() ? Hud.HeartType.FROZEN : Hud.HeartType.NORMAL);
        boolean hardcore = player.level().getLevelData().isHardcore();
        boolean shaking = current + absorption <= 4;
        // mit Regeneration läuft eine Welle über die Reihe (zehn Herzen, dann eine kurze Pause)
        int wave = player.hasEffect(MobEffects.REGENERATION) ? ticks % 15 : -1;

        for (int i = 0; i < 10; i++) {
            int x = xLeft + i * 8;
            int y = yBase + (shaking ? RANDOM.nextInt(2) : 0) - (i == wave ? 2 : 0);
            g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.HeartType.CONTAINER.getSprite(hardcore, false, blink), x, y, 9, 9);
            if (blink && old > current) {
                layer(g, type, hardcore, old, i, x, y, true);
            }
            layer(g, type, hardcore, current, i, x, y, false);
        }
        // links daneben: wie viele Herzen es gerade sind, in der Farbe der obersten Schicht
        String count = HeroGui.decimal(current / 2.0);
        int top = current > 0 ? (current - 1) / 20 : 0;
        g.text(mc.font, count, xLeft - 3 - mc.font.width(count), yBase + 1, tierColor(top), true);

        // die Absorption als goldene Reihe darüber
        if (absorption > 0) {
            int y = yBase - 10;
            int hearts = Mth.ceil(absorption / 2.0);
            Hud.HeartType gold = type == Hud.HeartType.WITHERED ? type : Hud.HeartType.ABSORBING;
            for (int i = 0; i < Math.min(10, hearts); i++) {
                int x = xLeft + i * 8;
                int yy = y + (shaking ? RANDOM.nextInt(2) : 0);
                g.blitSprite(RenderPipelines.GUI_TEXTURED, Hud.HeartType.CONTAINER.getSprite(hardcore, false, false), x, yy, 9, 9);
                g.blitSprite(RenderPipelines.GUI_TEXTURED, gold.getSprite(hardcore, absorption - i * 2 == 1, false), x, yy, 9, 9);
            }
            if (hearts > 10) {
                String more = "+" + HeroGui.decimal(absorption / 2.0);
                g.text(mc.font, more, xLeft - 3 - mc.font.width(more), y + 1, GOLDEN, true);
            }
        }
        return true;
    }

    /**
     * Ein Platz der Reihe bei {@code halves} halben Herzen: das Herz der obersten Schicht (ganz oder halb) über dem vollen
     * Herzen der Schicht darunter.
     */
    private static void layer(GuiGraphicsExtractor g, Hud.HeartType type, boolean hardcore, int halves, int slot, int x, int y, boolean ghost) {
        if (halves <= 0) {
            return;
        }
        int top = (halves - 1) / 20;
        int here = halves - top * 20 - slot * 2;
        if (here < 2 && top > 0) {
            heart(g, type, hardcore, top - 1, false, x, y, ghost);
        }
        if (here >= 1) {
            heart(g, type, hardcore, top, here == 1, x, y, ghost);
        }
    }

    private static void heart(GuiGraphicsExtractor g, Hud.HeartType type, boolean hardcore, int tier, boolean half, int x, int y, boolean ghost) {
        if (tier == 0 || type != Hud.HeartType.NORMAL) {
            g.blitSprite(RenderPipelines.GUI_TEXTURED, type.getSprite(hardcore, half, ghost), x, y, 9, 9);
            return;
        }
        int color = tierColor(tier);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, half ? HALF : FULL, x, y, 9, 9, ghost ? ARGB.srgbLerp(0.65f, color, 0xFFFFFFFF) : color);
        g.blitSprite(RenderPipelines.GUI_TEXTURED, SHINE, x, y, 9, 9);
    }
}
