package io.github.sirschrolli.kingdomsatwar.client.boss;

import com.mojang.renderpearl.api.pipeline.BlendFunction;
import com.mojang.renderpearl.api.pipeline.ColorTargetState;
import com.mojang.renderpearl.api.pipeline.RenderPipeline;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.LongSupplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.LerpingBossEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.Util;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterRenderPipelinesEvent;
import org.jspecify.annotations.Nullable;

/**
 * Eigene Bossleisten der Bosse: je Boss ein verzierter Rahmen (Flammen, Eiskristalle, ein Korallenriff, die Arme des Kraken
 * …), links ein Medaillon mit seinem Bildnis, über der Leiste je Kampfphase ein Edelstein – genau dort, wo der Boss die
 * nächste Phase beginnt. Die Füllung strömt in seinen Farben, erlittener Schaden bleibt als helle Spur kurz stehen und
 * schmilzt dann nach. Der Name steht auf einem Band mit Borte und Schwalbenschwänzen.
 *
 * <p>Seit 0.94 lebt die Leiste: sie senkt sich beim Erscheinen herab, das Namensband entrollt sich und die Füllung lädt sich
 * auf (die Edelsteine leuchten auf, sobald sie erreicht sind). Treffer lassen den Rahmen beben und das Bildnis rot
 * aufblitzen, Heilung schimmert. Ein Glanz zieht über den Rahmen, um die Leiste schweben Funken, Flocken, Blasen oder
 * Störpixel – je Phase mehr. Erreicht der Boss eine Phase, zerspringt ihr Edelstein in Splitter, der Rahmen blitzt auf; in
 * der letzten Phase strömt und pulsiert die Füllung schneller, der Rahmen glüht, das Bildnis färbt sich vor Wut rot.
 *
 * <p>Erkannt werden die Leisten am Übersetzungsschlüssel {@code boss.kingdomsatwar.<stil>} (mit Namensschild
 * {@code boss.kingdomsatwar.<stil>.named}); der Aufbau der Texturen steht in tools/bossbar_textures.py.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class BossBarRenderer {
    public static final int FRAME_W = 240;
    public static final int FRAME_H = 44;
    public static final int BAR_X = 48;
    public static final int BAR_Y = 18;
    public static final int BAR_W = 182;
    public static final int BAR_H = 9;
    private static final int EMB_X = 21;
    private static final int EMB_Y = 22;
    private static final int TEX = 256;
    /** Abstand von der Zeile des Namens bis zum Rahmen. */
    private static final int NAME_SPACE = 13;
    /** So viel Platz nimmt eine Leiste ein (Name, Rahmen, Abstand zur nächsten). */
    public static final int HEIGHT = NAME_SPACE + FRAME_H + 4;
    // Namensband: Mittelstück (kachelbar), Enden mit Schwalbenschwanz, Abstand zum Text, so weit über dem Text beginnt es
    private static final int BAND_H = 15;
    private static final int BAND_TAIL = 18;
    private static final int BAND_CAP = 16;
    private static final int BAND_TILE = 16;
    private static final int BAND_PAD = 4;
    private static final int BAND_RISE = 4;
    private static final int MAX_MOTES = 64;
    /** Licht obenauf: hellt auf, statt zu verdecken (Glanz, Aufblitzen, Funken). */
    public static final RenderPipeline GLOW = RenderPipeline.builder(RenderPipelines.GUI_TEXTURED_SNIPPET)
            .withLocation(KingdomsAtWar.id("pipeline/boss_bar_glow"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build();
    /** Wie {@link #GLOW}, aber für einfarbige Flächen (Funken, Splitter). */
    public static final RenderPipeline GLOW_FILL = RenderPipeline.builder(RenderPipelines.GUI_SNIPPET)
            .withLocation(KingdomsAtWar.id("pipeline/boss_bar_glow_fill"))
            .withColorTargetState(new ColorTargetState(BlendFunction.LIGHTNING))
            .build();

    /** Wie Teilchen um eine Leiste schweben. */
    enum Drift {
        /** Glut: steigt flackernd auf. */
        EMBER,
        /** Schneeflocken: sinken pendelnd. */
        SNOW,
        /** Funken: kurz und schnell, in alle Richtungen. */
        SPARK,
        /** Funken einer Esse: fliegen im Bogen und fallen. */
        ARC,
        /** Blätter und Pollen: treiben pendelnd herab. */
        LEAF,
        /** Staub und Steinchen: rieseln unter der Leiste herab. */
        DUST,
        /** Luftblasen: steigen wackelnd auf. */
        BUBBLE,
        /** Rauch und Seelen: steigen langsam auf und vergehen. */
        SMOKE,
        /** Sterne: funkeln an Ort und Stelle (als Kreuz). */
        TWINKLE,
        /** Glitzer: treibt langsam und flackert. */
        GLITTER,
        /** Konfetti: fällt und dreht sich. */
        CONFETTI,
        /** Störpixel: Streifen, die kurz aufflackern. */
        GLITCH,
        /** Tinte: Tropfen lösen sich unter der Leiste. */
        DRIP,
        /** Kristallsplitter: treiben aufwärts. */
        SHARD,
        /** Splitter eines zersprungenen Edelsteins (fallen). */
        BREAK
    }

    /** Teilchen einer Leiste: wie sie ziehen, wie viele je Sekunde (in der ersten Phase), in welchen Farben. */
    private record Motes(Drift drift, float rate, int... colors) {}

    /** Aussehen einer Leiste: Textur, Farbe des Namens, Kampfphasen (Anteil des Lebens), Teilchen. */
    private record Style(Identifier texture, int color, float[] phases, Motes... motes) {}

    private static final Map<String, Style> STYLES = new HashMap<>();
    private static final Map<UUID, Bar> BARS = new HashMap<>();
    private static final String PREFIX = "boss.kingdomsatwar.";
    private static final String NAMED = ".named";
    /** Uhr der Leisten (Millisekunden); in Vorführungen auch verlangsamt. */
    private static LongSupplier clock = Util::getMillis;
    private static float devGlint = -1.0f;
    private static long lastCleanup;

    static {
        float[] dragon = {0.6f, 0.3f};
        float[] mage = {0.66f, 0.33f};
        for (DragonType type : DragonType.values()) {
            Motes[] motes = elementMotes(type);
            STYLES.put(type.id(), style(type.id(), type.color(), dragon, motes));
            // die Erzmagier: Rahmen ihres Elements, im Medaillon ihr Bildnis
            STYLES.put("mage_" + type.id(), style("mage_" + type.id(), type.color(), mage, motes));
        }
        // Nyxar, der Schattendrache
        STYLES.put("shadow", style("shadow", 0xB46CFF, dragon, new Motes(Drift.SMOKE, 4.0f, 0xA660FF, 0x6420B0, 0xF2E4FF),
                new Motes(Drift.TWINKLE, 1.0f, 0xF0E0FF)));
        // der Zerbrochene König (Boss der Leere): Sternenmarmor, Goldsaum, Sternennebel; Zorn bei der Hälfte, unter einem
        // Viertel die neun Kronensplitter
        STYLES.put("void", style("void", 0xD8C4FF, new float[] {0.5f, 0.25f}, new Motes(Drift.TWINKLE, 3.5f, 0xF4EEFF, 0x9A6AE8),
                new Motes(Drift.SHARD, 1.5f, 0xD2CAE6, 0x9A6AE8)));
        // Karkinos, der Riffkoloss: Muschelkalk mit Goldsaum, Korallenriff, Licht einer Lagune
        STYLES.put("reef", style("reef", 0x7AE8FF, new float[] {0.66f, 0.4f}, new Motes(Drift.BUBBLE, 4.0f, 0xE4FFF6, 0x38D4C4)));
        // der Kraken: Bronze mit Patina, seine Arme klammern sich an den Rahmen, die Füllung seine Tinte
        STYLES.put("kraken", style("kraken", 0xFF7684, new float[] {0.67f, 0.34f}, new Motes(Drift.BUBBLE, 3.5f, 0xD6FFF8, 0x36DCD2),
                new Motes(Drift.DRIP, 1.2f, 0x4A1A3E, 0x1C0818)));
        // der Steinhüter (Feenreich, seit 0.96): Quader aus Feenstein mit leuchtenden Fugen, Lavendelmoos und Mondsteinkristalle,
        // Leuchtranken; die Füllung milchiger Mondstein mit wanderndem Schimmer; unter halber Kraft legt er den Kristallpanzer an
        STYLES.put("keeper", style("keeper", 0xA8D4FF, new float[] {0.5f}, new Motes(Drift.DUST, 3.0f, 0xBFB8CF, 0x938AA6, 0x645C76),
                new Motes(Drift.GLITTER, 1.5f, 0xFFB0E4, 0x9AF2FF, 0xD4ECFF)));
        // die Meerhexe (Urmeer, seit 0.97): geschwärzte Bronze mit schwarzer Koralle, Tang und Perlen, Füllung wie ihr Trank
        STYLES.put("sea_witch", style("sea_witch", 0x7AF0C8, new float[] {0.5f}, new Motes(Drift.BUBBLE, 3.5f, 0xDCFFF2, 0x5AE8B8),
                new Motes(Drift.GLITTER, 1.2f, 0xFF7AC8, 0x7AFFC8, 0xDCFFF2)));
        // der Seelenhirte (Schattenwelt, seit 0.98): geschwärztes Eisen und Bein, Widderhörner, Talismane an Ketten, der Haken
        // seines Stabs mit der Laterne; die Füllung züngelndes Seelenfeuer, aus dem Seelen aufsteigen
        STYLES.put("soul_shepherd", style("soul_shepherd", 0x7AE8DC, new float[] {0.5f},
                new Motes(Drift.SMOKE, 3.0f, 0x8EF0E4, 0x2AC8BC, 0xE0FFFA), new Motes(Drift.EMBER, 1.5f, 0xB8FFF2, 0x4FE0D0)));
        // die Sternenweberin (Leere, seit 0.99): nachtblaues Chitin mit Silbersaum, vier Spinnenbeine hinter dem Medaillon,
        // Kokons und Sterne an Fäden; die Füllung ihr Netz vor dem Nachthimmel
        STYLES.put("star_weaver", style("star_weaver", 0xB8D4FF, new float[] {0.5f},
                new Motes(Drift.TWINKLE, 4.0f, 0xFFFFFF, 0x9AD8FF, 0xFFF4C4), new Motes(Drift.GLITTER, 1.2f, 0xD6E4FF, 0x8048BC)));
        // der Stundenwächter (Abgrund, seit 1.1): Uhrbronze mit Messingsaum, ein Zahnkranz um das Medaillon, Zahnräder über der
        // Leiste, Uhrgewichte darunter, als Spitze ein gebläuter Minutenzeiger; die Füllung glühendes Bernstein mit Minutenbahn
        STYLES.put("hour_warden", style("hour_warden", 0xFFC86A, new float[] {0.5f},
                new Motes(Drift.GLITTER, 3.5f, 0xFFF0C4, 0xFFB347, 0xDEB44A), new Motes(Drift.DUST, 1.2f, 0xC8964A, 0x8C561E)));
        // Noctua, die Mottenkönigin (Feenreich): Mondsilber mit Mottenflügeln, Füllung aus schimmerndem Flügelstaub
        STYLES.put("moth", style("moth", 0xFFB8E8, new float[] {0.5f}, new Motes(Drift.GLITTER, 4.5f, 0xFFF0FA, 0xFF8AD0, 0x9EE8C8)));
        // Malleus, der Glutschmied: geschwärztes Eisen mit Nieten, glühende Nähte, Hammer und Speer gekreuzt
        STYLES.put("forge", style("forge", 0xFF8A30, new float[] {0.5f}, new Motes(Drift.ARC, 4.0f, 0xFFF2B8, 0xFF8A24, 0xA8300A),
                new Motes(Drift.EMBER, 2.5f, 0xFFD27A, 0xFF7A1A, 0x8A2406)));
        // Ignara, die Flammenpriesterin (seit 1.2): Tempelgold mit Feueropalen, lodernde Flammenzungen, darunter ihre Flammenpeitsche;
        // Glut steigt auf, dazwischen blitzt es grün und blau wie in einem Opal; unter halber Kraft steigt sie als Phönix auf
        STYLES.put("ignara", style("ignara", 0xFF9A3A, new float[] {0.5f}, new Motes(Drift.EMBER, 4.5f, 0xFFE9A0, 0xFF8A24, 0xC8300E),
                new Motes(Drift.GLITTER, 1.0f, 0x5AF0A0, 0x62C8FF, 0xFFE08A)));
        // Asteria, die Sternenreiterin: Silber mit Goldsaum, Sterne und Nachthimmel, ein Stern im Medaillon
        STYLES.put("star", style("star", 0x9AD8FF, new float[] {0.5f}, new Motes(Drift.TWINKLE, 4.5f, 0xFFFFFF, 0x9AD8FF, 0xE8C25A)));
        // MissMystik, die Feen-Zauberkünstlerin: violetter Samt mit Goldsaum, Zauberkarten, Harlekin-Rauten
        STYLES.put("mystic", style("mystic", 0xE88AFF, new float[] {0.6f, 0.3f},
                new Motes(Drift.CONFETTI, 4.0f, 0xFF7AC8, 0x4AE0E8, 0xFFD86A, 0xFBF7FF), new Motes(Drift.TWINKLE, 1.0f, 0xFFFFFF)));
        // Anni der Troll: dunkles Metall mit Farbsäumen, der Bannhammer mit fehlender Textur, ein kaputtes Bild als Füllung
        STYLES.put("anni", style("anni", 0xF800F8, new float[] {0.5f}, new Motes(Drift.GLITCH, 7.0f, 0xF800F8, 0x22D8E8, 0x40FF70, 0xFFD400)));
        // Anni 2.0 (aus der Sicherung wiederhergestellt): derselbe Rahmen, aber alles in Alarmrot
        STYLES.put("anni2", style("anni2", 0xFF2040, new float[] {0.5f}, new Motes(Drift.GLITCH, 8.0f, 0xFF2040, 0xFF8A30, 0xFFD0D4, 0x40FF70)));
        // Schrolli der Schöpfer: Elfenbein und Gold, Schwingen hinter dem Medaillon, ein Kranz aus Strahlen, strömendes Licht;
        // in seiner ersten Gestalt wird er bei 70 % und 40 % schneller
        STYLES.put("schrolli", style("schrolli", 0xFFD24A, new float[] {0.7f, 0.4f}, new Motes(Drift.EMBER, 3.5f, 0xFFFFFF, 0xFFF2B0, 0xFFD24A),
                new Motes(Drift.TWINKLE, 1.5f, 0xFFFFFF)));
        // seine Wahre Gestalt: violettes Metall, Schwingen wie ein Sternenhimmel, eine Füllung aus Nebeln; Nova, Apokalypse und
        // Neuschöpfung
        STYLES.put("schrolli_true", style("schrolli_true", 0xD8A8FF, new float[] {0.6f, 0.45f, 0.25f},
                new Motes(Drift.TWINKLE, 4.0f, 0xFFFFFF, 0xC870F0, 0xA0F0FF)));
        // Schrolli der Allmächtige (dritte Gestalt): Weißgold mit Glutsaum, brennende Schwingen, eine Sonnenkrone, die Füllung das
        // Plasma einer Sonne; zweimal das Sonnengericht
        STYLES.put("schrolli_almighty", style("schrolli_almighty", 0xFFAA3C, new float[] {0.66f, 0.25f},
                new Motes(Drift.EMBER, 6.0f, 0xFFFBEA, 0xFFC040, 0xF8600E), new Motes(Drift.SPARK, 1.5f, 0xFFF2B0)));
        // der Weltenfresser: Chaosgestein mit glühenden Fugen, Stücke verschlungener Welten, sein Gebiss; die Füllung die Glut
        // seines Schlunds; Ruf der Welten, Zorn, Ruf der Welten
        STYLES.put("world_eater", style("world_eater", 0xFF8A3A, new float[] {0.66f, 0.5f, 0.33f},
                new Motes(Drift.EMBER, 5.0f, 0xFFE0A0, 0xFF7418, 0xA01C08), new Motes(Drift.SHARD, 1.5f, 0xF0E6FF, 0xA47CF0)));
    }

    private static Style style(String key, int color, float[] phases, Motes... motes) {
        return new Style(KingdomsAtWar.id("textures/gui/boss_bar/" + key + ".png"), color, phases, motes);
    }

    private static Motes[] elementMotes(DragonType type) {
        return switch (type) {
            case FIRE -> new Motes[] {new Motes(Drift.EMBER, 5.0f, 0xFFE9A0, 0xFF9A2A, 0xD8360A)};
            case ICE -> new Motes[] {new Motes(Drift.SNOW, 4.0f, 0xEAFCFF, 0xB8E8FF), new Motes(Drift.TWINKLE, 1.5f, 0xFFFFFF, 0x7AD0F8)};
            case LIGHTNING -> new Motes[] {new Motes(Drift.SPARK, 4.5f, 0xFFFBE0, 0xFFD84A), new Motes(Drift.TWINKLE, 1.5f, 0xFFF6B0)};
            case NATURE -> new Motes[] {new Motes(Drift.LEAF, 3.0f, 0x6CD03A, 0x2E8A1E, 0xA8E05A), new Motes(Drift.GLITTER, 1.5f, 0xD8FFA0)};
            case EARTH -> new Motes[] {new Motes(Drift.DUST, 4.0f, 0xE8A440, 0xB0661A, 0x6A4A2E)};
            case WATER -> new Motes[] {new Motes(Drift.BUBBLE, 4.0f, 0xD8FAFF, 0x3CC4EC)};
        };
    }

    /** Ein Teilchen (Lage relativ zur linken oberen Ecke des Rahmens, in Pixeln der Anzeige). */
    private static final class Mote {
        Drift drift;
        float x;
        float y;
        float vx;
        float vy;
        float age;
        float life;
        float seed;
        int color;
        int color2;
        int width = 1;
    }

    /** Was eine Leiste über die Zeit weiß: Erscheinen, Schadensspur, Treffer, Heilung, Phase, Teilchen. */
    private static final class Bar {
        final String style;
        final long born;
        final boolean intro;
        final RandomSource random;
        final float glintOffset;
        final List<Mote> motes = new ArrayList<>();
        final float[] spawn;
        long seen;
        long time;
        float last = -1.0f;
        float trail = -1.0f;
        float trailLast;
        long holdUntil;
        long hitTime = -100000L;
        float hitAmp;
        long flashTime = -100000L;
        float flashLo;
        float flashHi;
        long healTime = -100000L;
        float healLo;
        int phase = -1;
        long phaseTime = -100000L;
        float phaseMark;
        int lit;

        Bar(String style, long now, boolean intro, UUID id, int kinds) {
            this.style = style;
            this.born = now;
            this.intro = intro;
            this.random = RandomSource.create(id.getLeastSignificantBits() ^ now);
            this.glintOffset = (id.getMostSignificantBits() & 0xFFFF) / 65535.0f;
            this.spawn = new float[kinds];
            this.time = now;
        }
    }

    private BossBarRenderer() {}

    @SubscribeEvent
    static void registerPipelines(RegisterRenderPipelinesEvent event) {
        event.registerPipeline(GLOW);
        event.registerPipeline(GLOW_FILL);
    }

    @SubscribeEvent
    static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        LerpingBossEvent boss = event.getBossEvent();
        if (draw(event.getGuiGraphics(), boss.getId(), boss.getName(), boss.getProgress(), event.getGuiGraphics().guiWidth() / 2,
                event.getY())) {
            event.setCanceled(true);
            event.setIncrement(HEIGHT);
        }
    }

    /** Für Vorführungen: eine andere Uhr für die Bewegungen der Leisten (null: wieder die echte). */
    public static void devClock(@Nullable LongSupplier devClock) {
        clock = devClock == null ? Util::getMillis : devClock;
    }

    /** Für Vorführungen: den Glanz an dieser Stelle festhalten (0 bis 1; negativ: wieder von selbst). */
    public static void devGlint(float k) {
        devGlint = k;
    }

    /** Für Vorführungen: alle Leisten vergessen (sie erscheinen dann neu). */
    public static void devReset() {
        BARS.clear();
    }

    /**
     * Zeichnet die Leiste mit diesem Namen (falls es eine eigene gibt): mittig über {@code centerX}, den Namen in der Zeile
     * {@code top}, den Rahmen darunter. Liefert false, wenn der Name keine eigene Leiste hat.
     */
    public static boolean draw(GuiGraphicsExtractor g, UUID id, Component name, float rawProgress, int centerX, int top) {
        String key = styleKey(name);
        Style style = key == null ? null : STYLES.get(key);
        if (style == null) {
            return false;
        }
        long now = clock.getAsLong();
        Bar bar = BARS.get(id);
        if (bar == null || !bar.style.equals(key)) {
            bar = new Bar(key, now, true, id, style.motes().length);
            BARS.put(id, bar);
        }
        if (now - lastCleanup > 2000L) {
            lastCleanup = now;
            BARS.values().removeIf(b -> now - b.seen > 6000L && b.seen != 0L);
        }
        bar.seen = now;
        float dt = Mth.clamp((now - bar.time) / 1000.0f, 0.0f, 0.1f);
        bar.time = now;
        float progress = Mth.clamp(rawProgress, 0.0f, 1.0f);
        float[] marks = style.phases();

        // Erscheinen: herabsenken, Band entrollen, Füllung aufladen
        float t = bar.intro ? (now - bar.born) : 100000.0f;
        float appear = ease(Mth.clamp(t / 380.0f, 0.0f, 1.0f));
        float unfurl = ease(Mth.clamp((t - 120.0f) / 450.0f, 0.0f, 1.0f));
        float textAlpha = Mth.clamp((t - 380.0f) / 260.0f, 0.0f, 1.0f);
        float charge = smooth(Mth.clamp((t - 320.0f) / 1150.0f, 0.0f, 1.0f));
        boolean charging = charge < 1.0f;
        float shown = progress * charge;

        // Treffer, Heilung, Phasen
        if (!charging && bar.last >= 0.0f) {
            float drop = bar.last - progress;
            if (drop > 0.0008f) {
                float amp = Mth.clamp(0.7f + drop * 40.0f, 0.7f, 2.4f);
                boolean fresh = now - bar.hitTime > 200L;
                bar.hitAmp = fresh ? amp : Math.max(bar.hitAmp * (1.0f - (now - bar.hitTime) / 200.0f), amp);
                bar.hitTime = now;
                bar.flashHi = now - bar.flashTime < 160L ? Math.max(bar.flashHi, bar.last) : bar.last;
                bar.flashLo = progress;
                bar.flashTime = now;
            } else if (drop < -0.0008f) {
                bar.healLo = now - bar.healTime < 600L ? Math.min(bar.healLo, bar.last) : bar.last;
                bar.healTime = now;
                healSparkles(bar, style, bar.last, progress);
            }
        }
        bar.last = progress;
        int phase = 0;
        for (float f : marks) {
            if (progress <= f) {
                phase++;
            }
        }
        if (charging) {
            bar.phase = phase;
            for (int i = 0; i < marks.length; i++) {
                boolean on = shown > marks[i];
                if (on && (bar.lit & (1 << i)) == 0) {
                    bar.lit |= 1 << i;
                    burst(bar, style, BAR_X + Math.round(BAR_W * marks[i]), BAR_Y - 6, 7, 26.0f, false);
                }
            }
        } else {
            if (bar.phase >= 0 && phase > bar.phase) {
                // die Edelsteine, die gerade zersprungen sind (die Phasen stehen absteigend: 0,6 vor 0,3)
                for (int i = bar.phase; i < phase; i++) {
                    shatter(bar, style, BAR_X + Math.round(BAR_W * marks[i]));
                }
                bar.phaseMark = marks[phase - 1];
                bar.phaseTime = now;
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.AMETHYST_CLUSTER_BREAK, 0.8f, 0.7f));
            }
            bar.phase = phase;
        }
        boolean last = marks.length > 0 && phase == marks.length;
        float trail = trail(bar, charging ? shown : progress, now, dt);

        // Bewegung des Ganzen: Beben bei Treffern, Herabsenken beim Erscheinen
        // Zeit in Sekunden innerhalb der Stunde (als float genau genug für Wellen und Flackern)
        float secs = (now % 3_600_000L) / 1000.0f;
        float shakeX = 0.0f;
        float shakeY = 0.0f;
        float hitK = (now - bar.hitTime) / 200.0f;
        if (hitK < 1.0f) {
            float a = bar.hitAmp * (1.0f - hitK) * (1.0f - hitK);
            shakeX = Math.round(a * Mth.sin(secs * 130.0f));
            shakeY = Math.round(a * 0.6f * Mth.cos(secs * 171.0f));
        }
        float slide = Math.round((1.0f - appear) * -10.0f);
        int x = centerX - BAR_W / 2 - BAR_X;
        int y = top + NAME_SPACE;
        int bx = x + BAR_X;
        int by = y + BAR_Y;
        Identifier tex = style.texture();
        float pulse = 0.5f + 0.5f * Mth.sin(secs * 5.5f);
        float flash = Math.max(0.0f, 1.0f - (now - bar.phaseTime) / 700.0f);
        Minecraft mc = Minecraft.getInstance();

        // Namensband (entrollt sich von der Mitte aus)
        int nameW = mc.font.width(name);
        int nx = centerX - nameW / 2;
        g.pose().pushMatrix();
        g.pose().translate(0.0f, slide);
        band(g, tex, centerX, nx - BAND_PAD, nx + nameW + BAND_PAD, top - BAND_RISE, unfurl, appear, flash);
        g.pose().popMatrix();

        g.pose().pushMatrix();
        g.pose().translate(shakeX, shakeY + slide);
        int frameColor = ARGB.white(appear);

        // Leiste: leer, Schadensspur, strömende Füllung
        g.blit(RenderPipelines.GUI_TEXTURED, tex, bx, by, 0.0f, 58.0f, BAR_W, BAR_H, TEX, TEX, frameColor);
        int fill = Mth.lerpDiscrete(shown, 0, BAR_W);
        int trailFill = Mth.lerpDiscrete(trail, 0, BAR_W);
        if (trailFill > fill) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, bx + fill, by, fill, 68.0f, trailFill - fill, BAR_H, TEX, TEX, frameColor);
        }
        float flashK = (now - bar.flashTime) / 170.0f;
        if (flashK < 1.0f) {
            int lo = Mth.lerpDiscrete(bar.flashLo, 0, BAR_W);
            int hi = Math.max(lo + 1, Mth.lerpDiscrete(bar.flashHi, 0, BAR_W));
            g.fill(bx + lo, by + 1, bx + hi, by + BAR_H - 1, ARGB.white(0.85f * (1.0f - flashK)));
        }
        if (fill > 0) {
            long speed = last ? 20L : 45L;
            int offset = (int) ((now / speed) % TEX);
            fillStrip(g, RenderPipelines.GUI_TEXTURED, tex, bx, by, fill, offset, frameColor);
            if (last) {
                // letzte Phase: die Füllung pulsiert hell
                fillStrip(g, GLOW, tex, bx, by, fill, offset, ARGB.white((0.12f + 0.22f * pulse) * appear));
            }
            float healK = (now - bar.healTime) / 650.0f;
            if (healK < 1.0f) {
                // Heilung schimmert hell über dem geheilten Stück
                int lo = Mth.clamp(Mth.lerpDiscrete(bar.healLo, 0, BAR_W), 0, fill - 1);
                g.fill(GLOW_FILL, bx + lo, by + 1, bx + fill, by + BAR_H - 1, ARGB.white(0.55f * (1.0f - healK)));
            }
            int shine = (int) ((now / 12L) % (BAR_W + 60)) - 30;
            if (shine > -16 && shine < fill) {
                int from = Math.max(0, shine);
                int to = Math.min(fill, shine + 16);
                if (to > from) {
                    g.blit(RenderPipelines.GUI_TEXTURED, tex, bx + from, by, 184.0f + (from - shine), 58.0f, to - from, BAR_H,
                            TEX, TEX, ARGB.white(0.6f * appear));
                }
            }
            if (fill < BAR_W) {
                // die Kante der Füllung glimmt (beim Aufladen und in der letzten Phase heller)
                float edge = charging ? 0.9f : last ? 0.45f + 0.4f * flickerf(secs * 11.0f + 3.0f) : 0.35f + 0.15f * flickerf(secs * 11.0f + 5.0f);
                g.fill(GLOW_FILL, bx + fill - 1, by + 1, bx + fill, by + BAR_H - 1, ARGB.white(edge * appear));
                g.fill(GLOW_FILL, bx + fill - 2, by + 2, bx + fill - 1, by + BAR_H - 2, ARGB.white(0.35f * edge * appear));
            }
        }
        // Kerben bei den Phasen
        for (float f : marks) {
            int mx = bx + Math.round(BAR_W * f);
            g.fill(mx, by + 1, mx + 1, by + BAR_H - 1, ARGB.color(Math.round(150 * appear), 0, 0, 0));
            g.fill(mx + 1, by + 1, mx + 2, by + BAR_H - 1, ARGB.color(Math.round(45 * appear), 255, 255, 255));
        }

        // Aura: in der letzten Phase pulsierend, davor schwach; nach einem Phasenwechsel hell
        float aura = last ? 0.55f + 0.45f * pulse : phase > 0 ? 0.1f + 0.08f * phase / Math.max(1, marks.length) + 0.08f * pulse : 0.0f;
        aura = Math.max(aura, flash);
        if (aura > 0.02f) {
            g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0f, 80.0f, FRAME_W, FRAME_H, TEX, TEX, ARGB.white(aura * appear));
        }

        // Rahmen, Glanz, Aufblitzen beim Phasenwechsel
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x, y, 0.0f, 0.0f, FRAME_W, FRAME_H, TEX, TEX, frameColor);
        glint(g, tex, x, y, now, bar.glintOffset, appear);
        if (flash > 0.01f) {
            g.blit(GLOW, tex, x, y, 0.0f, 0.0f, FRAME_W, FRAME_H, TEX, TEX, ARGB.white(0.9f * flash * flash));
        }

        // Bildnis (doppelte Auflösung): bei Treffern rot aufblitzend, in der letzten Phase rot pulsierend
        int tint = frameColor;
        if (last) {
            int gb = Math.round(255 - 110 * pulse);
            tint = ARGB.color(Math.round(255 * appear), 255, gb, gb);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, x + EMB_X - 16, y + EMB_Y - 16, 0.0f, 128.0f, 32, 32, 64, 64, TEX, TEX, tint);
        if (hitK < 0.9f) {
            float hurt = 1.0f - hitK / 0.9f;
            g.blit(GLOW, tex, x + EMB_X - 16, y + EMB_Y - 16, 0.0f, 128.0f, 32, 32, 64, 64, TEX, TEX,
                    ARGB.color(Math.round(150 * hurt * appear), 255, 60, 40));
        }

        // Edelsteine: leuchtend, solange die Phase noch nicht erreicht ist; der nächste pulsiert, wenn es knapp wird
        for (int i = 0; i < marks.length; i++) {
            float f = marks[i];
            int gx = bx + Math.round(BAR_W * f) - 4;
            int gy = by - 10;
            boolean on = charging ? (bar.lit & (1 << i)) != 0 : progress > f;
            g.blit(RenderPipelines.GUI_TEXTURED, tex, gx, gy, on ? 204.0f : 214.0f, 58.0f, 9, 9, TEX, TEX, frameColor);
            if (on && !charging) {
                float near = (progress - f) / 0.08f;
                if (near < 1.0f) {
                    g.blit(GLOW, tex, gx, gy, 204.0f, 58.0f, 9, 9, TEX, TEX, ARGB.white((1.0f - near) * (0.3f + 0.4f * pulse)));
                }
                long tw = (now + (long) (i * 1370 + bar.glintOffset * 3000)) % 3200L;
                if (tw < 220L) {
                    float k = 1.0f - Math.abs(tw - 110.0f) / 110.0f;
                    int sx = gx + 3;
                    int sy = gy + 2;
                    g.fill(GLOW_FILL, sx, sy - 1, sx + 1, sy + 2, ARGB.white(0.9f * k));
                    g.fill(GLOW_FILL, sx - 1, sy, sx + 2, sy + 1, ARGB.white(0.9f * k));
                }
            }
            if (!on && now - bar.phaseTime < 160L && bar.phaseMark == f) {
                float k = 1.0f - (now - bar.phaseTime) / 160.0f;
                g.fill(gx, gy + 1, gx + 9, gy + 8, ARGB.white(0.85f * k));
                g.fill(gx + 1, gy, gx + 8, gy + 9, ARGB.white(0.85f * k));
            }
        }

        // Teilchen: Funken, Flocken, Blasen … (je Phase mehr), dazu die Splitter
        if (appear >= 1.0f) {
            float mult = last ? 2.6f : 1.0f + 0.6f * phase;
            Motes[] kinds = style.motes();
            for (int i = 0; i < kinds.length; i++) {
                bar.spawn[i] += kinds[i].rate() * mult * dt;
                while (bar.spawn[i] >= 1.0f) {
                    bar.spawn[i] -= 1.0f;
                    if (bar.motes.size() < MAX_MOTES) {
                        bar.motes.add(spawn(bar.random, kinds[i]));
                    }
                }
            }
        }
        motes(g, bar, x, y, dt, appear);
        g.pose().popMatrix();

        // der Name (beim Phasenwechsel kurz hell)
        if (textAlpha > 0.02f) {
            int color = ARGB.srgbLerp(flash * 0.8f, ARGB.opaque(style.color()), 0xFFFFFFFF);
            g.pose().pushMatrix();
            g.pose().translate(0.0f, slide);
            g.text(mc.font, name, nx, top, ARGB.color(Math.round(255 * textAlpha), color), true);
            g.pose().popMatrix();
        }
        return true;
    }

    /** Die Füllung ab {@code offset} der kachelbaren Textur (sie strömt, indem der Versatz wandert). */
    private static void fillStrip(GuiGraphicsExtractor g, RenderPipeline pipeline, Identifier tex, int bx, int by, int fill, int offset,
                                  int color) {
        int first = Math.min(fill, TEX - offset);
        g.blit(pipeline, tex, bx, by, offset, 48.0f, first, BAR_H, TEX, TEX, color);
        if (first < fill) {
            g.blit(pipeline, tex, bx + first, by, 0.0f, 48.0f, fill - first, BAR_H, TEX, TEX, color);
        }
    }

    /** Namensband: Mittelstück zwischen x0 und x1 (gekachelt), an beiden Enden der Schwalbenschwanz. */
    private static void band(GuiGraphicsExtractor g, Identifier tex, int centerX, int x0, int x1, int y, float unfurl, float alpha,
                             float flash) {
        if (unfurl <= 0.0f || alpha <= 0.0f) {
            return;
        }
        int half = Math.round((x1 - x0) / 2.0f * unfurl);
        int a = centerX - half;
        int b = centerX + half;
        int color = ARGB.white(alpha);
        g.blit(RenderPipelines.GUI_TEXTURED, tex, a - BAND_CAP, y, 80.0f, 128.0f, BAND_CAP, BAND_TAIL, TEX, TEX, color);
        for (int tx = a; tx < b; tx += BAND_TILE) {
            int w = Math.min(BAND_TILE, b - tx);
            g.blit(RenderPipelines.GUI_TEXTURED, tex, tx, y, 64.0f, 128.0f, w, BAND_H, TEX, TEX, color);
        }
        g.blit(RenderPipelines.GUI_TEXTURED, tex, b, y, 96.0f, 128.0f, BAND_CAP, BAND_TAIL, TEX, TEX, color);
        if (flash > 0.01f) {
            int glow = ARGB.white(0.7f * flash * flash);
            g.blit(GLOW, tex, a - BAND_CAP, y, 80.0f, 128.0f, BAND_CAP, BAND_TAIL, TEX, TEX, glow);
            for (int tx = a; tx < b; tx += BAND_TILE) {
                int w = Math.min(BAND_TILE, b - tx);
                g.blit(GLOW, tex, tx, y, 64.0f, 128.0f, w, BAND_H, TEX, TEX, glow);
            }
            g.blit(GLOW, tex, b, y, 96.0f, 128.0f, BAND_CAP, BAND_TAIL, TEX, TEX, glow);
        }
    }

    /**
     * Ein schräger Glanz, der alle paar Sekunden über Rahmen und Medaillon zieht: Spalte für Spalte wird das Bild des Rahmens
     * noch einmal aufhellend darübergelegt – ein weicher Streif, in der Mitte eine helle Linie.
     */
    private static void glint(GuiGraphicsExtractor g, Identifier tex, int x, int y, long now, float offset, float alpha) {
        long period = 5200L;
        float k = devGlint >= 0.0f ? devGlint : ((now + (long) (offset * period)) % period) / 1400.0f;
        if (k >= 1.0f) {
            return;
        }
        float center = -40.0f + (FRAME_W + 80.0f) * k;
        float strength = alpha * (1.0f - Math.abs(2.0f * k - 1.0f) * 0.35f);
        for (int col = 0; col < FRAME_W; col++) {
            float d = col - center;
            if (d < -40.0f || d > 40.0f) {
                continue;
            }
            // die Linie läuft schräg: oben weiter rechts als unten
            float mid = FRAME_H / 2.0f - d / 0.7f;
            glintBand(g, tex, x, y, col, mid, 8.0f, 0.42f * strength);
            glintBand(g, tex, x, y, col, mid, 2.5f, 0.65f * strength);
        }
    }

    private static void glintBand(GuiGraphicsExtractor g, Identifier tex, int x, int y, int col, float mid, float half, float strength) {
        int y0 = Math.max(0, Math.round(mid - half));
        int y1 = Math.min(FRAME_H, Math.round(mid + half));
        if (y1 > y0) {
            g.blit(GLOW, tex, x + col, y + y0, col, y0, 1, y1 - y0, TEX, TEX, ARGB.white(strength));
        }
    }

    /** Spur des Schadens fortschreiben und ihren aktuellen Stand liefern. */
    private static float trail(Bar bar, float progress, long now, float dt) {
        if (bar.trail < 0.0f) {
            bar.trail = progress;
            bar.trailLast = progress;
        }
        if (progress < bar.trailLast - 0.0005f && now > bar.holdUntil + 300L) {
            bar.holdUntil = now + 550L;                 // frischer Treffer: Spur kurz stehen lassen (nicht endlos verlängern)
        }
        bar.trailLast = progress;
        if (progress >= bar.trail) {
            bar.trail = progress;                       // Heilung: sofort mitgehen
        } else if (now > bar.holdUntil) {
            bar.trail = Math.max(progress, bar.trail - dt * (0.35f + (bar.trail - progress) * 2.0f));
        }
        return bar.trail;
    }

    // ------------------------------------------------------------------ Teilchen

    private static Mote spawn(RandomSource r, Motes kind) {
        Mote m = new Mote();
        m.drift = kind.drift();
        int[] colors = kind.colors();
        m.color = colors[r.nextInt(colors.length)];
        m.color2 = colors[colors.length - 1];
        m.seed = r.nextFloat() * 100.0f;
        float along = BAR_X + r.nextFloat() * BAR_W;
        switch (m.drift) {
            case EMBER, SHARD -> {
                m.x = along;
                m.y = BAR_Y - 2.0f + r.nextFloat() * (BAR_H + 4.0f);
                m.vx = (r.nextFloat() - 0.5f) * 5.0f;
                m.vy = -(7.0f + r.nextFloat() * 10.0f) * (m.drift == Drift.SHARD ? 0.6f : 1.0f);
                m.life = 0.8f + r.nextFloat() * 0.9f;
                m.color = colors[0];
            }
            case SNOW, LEAF, CONFETTI -> {
                m.x = along;
                m.y = 1.0f + r.nextFloat() * 8.0f;
                m.vx = (r.nextFloat() - 0.5f) * 3.0f;
                m.vy = m.drift == Drift.CONFETTI ? 9.0f + r.nextFloat() * 6.0f : 4.5f + r.nextFloat() * 4.0f;
                m.life = 2.2f + r.nextFloat() * 1.2f;
            }
            case SPARK -> {
                m.x = along;
                m.y = BAR_Y + r.nextFloat() * BAR_H;
                double a = r.nextFloat() * Math.PI * 2.0;
                float v = 22.0f + r.nextFloat() * 26.0f;
                m.vx = (float) Math.cos(a) * v;
                m.vy = (float) Math.sin(a) * v * 0.6f;
                m.life = 0.14f + r.nextFloat() * 0.22f;
            }
            case ARC -> {
                m.x = along;
                m.y = BAR_Y - 1.0f;
                m.vx = (r.nextFloat() - 0.5f) * 28.0f;
                m.vy = -(16.0f + r.nextFloat() * 18.0f);
                m.life = 0.5f + r.nextFloat() * 0.4f;
            }
            case DUST, DRIP -> {
                m.x = along;
                m.y = BAR_Y + BAR_H + 3.0f;
                m.vx = m.drift == Drift.DUST ? (r.nextFloat() - 0.5f) * 3.0f : 0.0f;
                m.vy = m.drift == Drift.DUST ? 5.0f + r.nextFloat() * 6.0f : 0.5f;
                m.life = m.drift == Drift.DUST ? 0.8f + r.nextFloat() * 0.5f : 1.0f + r.nextFloat() * 0.4f;
            }
            case BUBBLE -> {
                m.x = along;
                m.y = BAR_Y + BAR_H + 2.0f + r.nextFloat() * 10.0f;
                m.vy = -(6.0f + r.nextFloat() * 7.0f);
                m.life = 1.4f + r.nextFloat() * 1.2f;
            }
            case SMOKE -> {
                m.x = along;
                m.y = BAR_Y + r.nextFloat() * BAR_H;
                m.vx = (r.nextFloat() - 0.5f) * 3.0f;
                m.vy = -(3.5f + r.nextFloat() * 4.0f);
                m.life = 1.6f + r.nextFloat() * 1.2f;
            }
            case TWINKLE, GLITTER -> {
                m.x = 34.0f + r.nextFloat() * (FRAME_W - 36.0f);
                m.y = 1.0f + r.nextFloat() * (FRAME_H - 2.0f);
                if (m.drift == Drift.GLITTER) {
                    m.vx = (r.nextFloat() - 0.5f) * 4.0f;
                    m.vy = -(1.0f + r.nextFloat() * 3.0f);
                }
                m.life = 0.6f + r.nextFloat() * 0.6f;
            }
            case GLITCH -> {
                m.x = 40.0f + r.nextFloat() * (FRAME_W - 44.0f);
                m.y = 4.0f + r.nextFloat() * (FRAME_H - 8.0f);
                m.width = 1 + r.nextInt(5);
                m.life = 0.06f + r.nextFloat() * 0.2f;
            }
            case BREAK -> m.life = 1.0f;
        }
        return m;
    }

    /** Ein Ring aus Funken (beim Aufleuchten eines Edelsteins) oder – mit {@code fall} – ein Splitterregen. */
    private static void burst(Bar bar, Style style, float cx, float cy, int count, float speed, boolean fall) {
        RandomSource r = bar.random;
        for (int i = 0; i < count && bar.motes.size() < MAX_MOTES + 40; i++) {
            Mote m = new Mote();
            m.drift = fall ? Drift.BREAK : Drift.SPARK;
            double a = (i + r.nextFloat() * 0.6f) / count * Math.PI * 2.0;
            float v = speed * (0.6f + r.nextFloat() * 0.6f);
            m.x = cx;
            m.y = cy;
            m.vx = (float) Math.cos(a) * v;
            m.vy = (float) Math.sin(a) * v - (fall ? 22.0f : 0.0f);
            m.life = fall ? 0.8f + r.nextFloat() * 0.5f : 0.3f + r.nextFloat() * 0.2f;
            m.color = fall || i % 3 == 0 ? 0xFFFFFF : style.color();
            m.color2 = style.color();
            m.seed = r.nextFloat() * 100.0f;
            bar.motes.add(m);
        }
    }

    /** Der Edelstein einer erreichten Phase zerspringt: Splitter fallen, ein Funkenring breitet sich aus. */
    private static void shatter(Bar bar, Style style, int gemX) {
        burst(bar, style, gemX + 0.5f, BAR_Y - 6.0f, 16, 40.0f, true);
        burst(bar, style, gemX + 0.5f, BAR_Y - 6.0f, 16, 44.0f, false);
    }

    /** Bei Heilung steigen über dem geheilten Stück ein paar helle Funken auf. */
    private static void healSparkles(Bar bar, Style style, float from, float to) {
        RandomSource r = bar.random;
        int n = Mth.clamp(Math.round((to - from) * 60.0f), 1, 5);
        for (int i = 0; i < n && bar.motes.size() < MAX_MOTES + 24; i++) {
            Mote m = new Mote();
            m.drift = Drift.GLITTER;
            m.x = BAR_X + BAR_W * Mth.lerp(r.nextFloat(), from, to);
            m.y = BAR_Y + r.nextFloat() * BAR_H;
            m.vx = (r.nextFloat() - 0.5f) * 3.0f;
            m.vy = -(5.0f + r.nextFloat() * 5.0f);
            m.life = 0.6f + r.nextFloat() * 0.4f;
            m.color = 0xFFFFFF;
            m.color2 = style.color();
            bar.motes.add(m);
        }
    }

    /** Teilchen bewegen und zeichnen (Leuchtendes aufhellend, Staub, Blätter und Tinte deckend). */
    private static void motes(GuiGraphicsExtractor g, Bar bar, int x, int y, float dt, float alpha) {
        for (Iterator<Mote> it = bar.motes.iterator(); it.hasNext(); ) {
            Mote m = it.next();
            m.age += dt;
            if (m.age >= m.life) {
                it.remove();
                continue;
            }
            float k = m.age / m.life;
            float sway = Mth.sin(m.age * 3.2f + m.seed);
            switch (m.drift) {
                case EMBER, SHARD -> m.vx += sway * 6.0f * dt;
                case SNOW, LEAF -> m.vx = sway * 5.0f;
                case CONFETTI -> m.vx = sway * 7.0f;
                case BUBBLE -> m.vx = Mth.sin(m.age * 6.0f + m.seed) * 3.5f;
                case ARC -> m.vy += 70.0f * dt;
                case BREAK -> m.vy += 120.0f * dt;
                case DRIP -> m.vy += 26.0f * dt;
                case SPARK -> {
                    m.vx *= 1.0f - 4.0f * dt;
                    m.vy *= 1.0f - 4.0f * dt;
                }
                default -> {
                }
            }
            m.x += m.vx * dt;
            m.y += m.vy * dt;
            float fade = Math.min(1.0f, Math.min(k * 6.0f, (1.0f - k) * 3.0f)) * alpha;
            if (fade <= 0.02f) {
                continue;
            }
            float px = x + m.x;
            float py = y + m.y;
            g.pose().pushMatrix();
            g.pose().translate(px, py);
            switch (m.drift) {
                case EMBER -> {
                    int col = ARGB.srgbLerp(k, ARGB.opaque(m.color), ARGB.opaque(m.color2));
                    g.fill(0, 0, 1, 1, ARGB.color(Math.round(255 * fade * (0.7f + 0.3f * flickerf(m.age * 9.0f + m.seed))), col));
                }
                case SHARD -> g.fill(0, 0, 1, 1, ARGB.color(Math.round(235 * fade), k < 0.5f ? m.color : m.color2));
                case SNOW -> g.fill(0, 0, 1, 1, ARGB.color(Math.round(235 * fade), m.color));
                case LEAF -> {
                    boolean wide = Mth.sin(m.age * 4.0f + m.seed) > 0.0f;
                    g.fill(0, 0, wide ? 2 : 1, wide ? 1 : 2, ARGB.color(Math.round(240 * fade), m.color));
                }
                case CONFETTI -> {
                    boolean wide = ((int) (m.age * 7.0f + m.seed)) % 2 == 0;
                    g.fill(0, 0, wide ? 2 : 1, wide ? 1 : 2, ARGB.color(Math.round(255 * fade), m.color));
                }
                case DUST -> g.fill(0, 0, 1, 1, ARGB.color(Math.round(225 * fade), m.color));
                case DRIP -> g.fill(0, 0, 1, k < 0.3f ? 1 : 2, ARGB.color(Math.round(235 * fade), m.color));
                case SPARK -> g.fill(0, 0, 1, 1, ARGB.color(Math.round(255 * fade), m.color));
                case BREAK -> {
                    // Splitter des Edelsteins: hell, dann in seiner Farbe; die größeren zwei Pixel breit oder hoch
                    int col = k > 0.35f ? m.color2 : m.color;
                    int a = Math.round(255 * fade);
                    int w = m.seed > 55.0f ? 2 : 1;
                    int h = m.seed > 80.0f || m.seed < 20.0f ? 2 : 1;
                    g.fill(0, 0, w, h, ARGB.color(a, ARGB.scaleRGB(col, 0.55f)));
                    g.fill(0, 0, 1, 1, ARGB.color(a, col));
                }
                case ARC -> {
                    int col = ARGB.srgbLerp(k, ARGB.opaque(m.color), ARGB.opaque(m.color2));
                    g.fill(0, 0, 1, 1, ARGB.color(Math.round(255 * fade), col));
                }
                case BUBBLE -> {
                    int a = Math.round(210 * fade);
                    if (m.seed > 70.0f) {
                        // große Blase: ein Ring mit Lichtpunkt
                        g.fill(0, -1, 1, 0, ARGB.color(a, m.color2));
                        g.fill(-1, 0, 0, 1, ARGB.color(a, m.color2));
                        g.fill(1, 0, 2, 1, ARGB.color(a, m.color2));
                        g.fill(0, 1, 1, 2, ARGB.color(a, m.color2));
                        g.fill(-1, -1, 0, 0, ARGB.color(a, m.color));
                    } else {
                        g.fill(0, 0, 1, 1, ARGB.color(a, m.color));
                    }
                }
                case SMOKE -> {
                    int size = k < 0.4f ? 1 : 2;
                    g.fill(0, 0, size, size, ARGB.color(Math.round(170 * fade * (1.0f - 0.6f * k)), m.color));
                }
                case TWINKLE -> {
                    float tw = Mth.sin(k * Mth.PI);
                    int a = Math.round(255 * tw * fade);
                    g.fill(0, 0, 1, 1, ARGB.color(a, m.color));
                    if (tw > 0.6f) {
                        int arm = ARGB.color(Math.round(a * 0.6f), m.color);
                        g.fill(-1, 0, 0, 1, arm);
                        g.fill(1, 0, 2, 1, arm);
                        g.fill(0, -1, 1, 0, arm);
                        g.fill(0, 1, 1, 2, arm);
                    }
                }
                case GLITTER -> g.fill(0, 0, 1, 1,
                        ARGB.color(Math.round(255 * fade * (0.45f + 0.55f * flickerf(m.age * 14.0f + m.seed))), m.color));
                case GLITCH -> g.fill(0, 0, m.width, 1, ARGB.color(Math.round(235 * alpha), m.color));
            }
            g.pose().popMatrix();
        }
    }

    // ------------------------------------------------------------------ Hilfen

    /** Schlüssel der Leiste aus ihrem Namen ({@code boss.kingdomsatwar.fire}, mit Namensschild {@code ….fire.named}). */
    private static @Nullable String styleKey(Component name) {
        if (name.getContents() instanceof TranslatableContents contents && contents.getKey().startsWith(PREFIX)) {
            String key = contents.getKey().substring(PREFIX.length());
            return key.endsWith(NAMED) ? key.substring(0, key.length() - NAMED.length()) : key;
        }
        return null;
    }

    /** Hat dieser Name eine eigene Leiste? */
    public static boolean hasStyle(Component name) {
        String key = styleKey(name);
        return key != null && STYLES.containsKey(key);
    }

    /** Die Kampfphasen der Leiste {@code boss.kingdomsatwar.<key>} (Anteil des Lebens), leer ohne eigene Leiste. */
    public static float[] phases(String key) {
        Style style = STYLES.get(key);
        return style == null ? new float[0] : style.phases().clone();
    }

    private static float ease(float t) {
        float u = 1.0f - t;
        return 1.0f - u * u * u;
    }

    private static float smooth(float t) {
        return t < 0.5f ? 4.0f * t * t * t : 1.0f - (float) Math.pow(-2.0f * t + 2.0f, 3.0) / 2.0f;
    }

    private static float flickerf(float t) {
        return 0.5f + 0.25f * Mth.sin(t) + 0.25f * Mth.sin(t * 2.7f + 1.3f);
    }
}
