package io.github.sirschrolli.kingdomsatwar.client.intro;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Erzähl-Intro „Das Siegel“ beim ersten Betreten einer Welt: eine Bilderfolge in der eigenen Welt
 * ({@link IntroFlight}) – die Dunkelheit steigt auf, sechs Drachen schmieden das Siegel, werden wild und ziehen davon,
 * ein Heer salutiert, unter der Erde pocht es –, schwarze Kinobalken, der Erzähler spricht elf Zeilen (Text erscheint
 * dazu), Musik läuft, am Ende die Titelkarte. Der Spieler selbst ist währenddessen nicht zu sehen. Leertaste, Enter
 * oder Esc überspringen.
 *
 * <p>Die Drachen, die Soldaten, die Lichteffekte und die Kamera gibt es nur auf dem eigenen Bildschirm (nicht auf dem
 * Server).
 */
public class IntroScreen extends Screen {
    private static final Identifier TITLE = KingdomsAtWar.id("textures/gui/intro_title.png");
    private static final int TITLE_W = 520;
    private static final int TITLE_H = 140;
    private static final Identifier DRAGON_WARFARE = KingdomsAtWar.id("textures/gui/intro_dragon_warfare.png");
    private static final int DW_W = 565;
    private static final int DW_H = 103;
    private static final int TEXT_COLOR = 0xF3E7C8;

    private final IntroScript script;
    private final List<IntroScript.Line> lines;
    private final Music music;
    private final boolean[] spoken;
    private final List<SoundInstance> voices = new ArrayList<>();
    private long startNanos = -1;
    private int ticks;
    private @Nullable Marker camera;
    private @Nullable IntroFlight flight;
    private float lastYaw;
    private float lastPitch;
    private boolean hudWasHidden;
    private boolean cleanedUp;

    public IntroScreen(IntroScript script, String language) {
        super(Component.translatable("intro.kingdomsatwar.title"));
        this.script = script;
        this.lines = script.linesFor(language);
        this.spoken = new boolean[this.lines.size()];
        this.music = new Music(Holder.direct(SoundEvent.createVariableRangeEvent(script.music())), 0, 0, true);
    }

    /** Sekunden seit dem Start der Musik (0, solange sie noch nicht läuft). */
    public double elapsed() {
        return this.startNanos < 0 ? 0.0 : (System.nanoTime() - this.startNanos) / 1.0e9;
    }

    @Override
    protected void init() {
        // init() läuft bei jeder Größenänderung des Fensters erneut – die Kamera nur einmal einrichten
        if (this.camera != null || this.minecraft.player == null || this.minecraft.level == null) {
            return;
        }
        LocalPlayer player = this.minecraft.player;
        this.flight = new IntroFlight(this.minecraft.level, player.position(), player.getYRot(), this.script, this.lines, this::elapsed,
                (sound, volume, pitch) -> this.play(sound.location(), SoundSource.AMBIENT, volume, pitch));
        this.flight.tick(0.0);
        this.lastYaw = player.getYRot();
        this.camera = new Marker(EntityTypes.MARKER, this.minecraft.level);
        this.updateCamera(0.0, true);
        this.minecraft.setCameraEntity(this.camera);
        this.hudWasHidden = this.minecraft.gui.hud.isHidden();
        if (!this.hudWasHidden) {
            this.minecraft.gui.hud.toggle();
        }
    }

    @Override
    public void tick() {
        this.ticks++;
        if (this.startNanos < 0 && (this.minecraft.getMusicManager().isPlayingMusic(this.music) || this.ticks > 40)) {
            this.startNanos = System.nanoTime();
        }
        this.minecraft.gui.toastManager().hideNowPlayingToast();
        double t = this.elapsed();
        if (this.startNanos >= 0) {
            for (int i = 0; i < this.lines.size(); i++) {
                if (!this.spoken[i] && t >= this.lines.get(i).start()) {
                    this.spoken[i] = true;
                    this.voices.add(this.play(this.lines.get(i).sound(), SoundSource.VOICE, 1.0f, 1.0f));
                }
            }
        }
        if (this.flight != null) {
            this.flight.tick(t);
        }
        this.updateCamera(t, false);
        if (t > this.script.length() + 0.3) {
            this.onClose();
        }
    }

    private SoundInstance play(Identifier sound, SoundSource source, float volume, float pitch) {
        SoundInstance instance = new SimpleSoundInstance(sound, source, volume, pitch, SoundInstance.createUnseededRandom(), false, 0,
                SoundInstance.Attenuation.NONE, 0.0, 0.0, 0.0, true);
        this.minecraft.getSoundManager().play(instance);
        return instance;
    }

    // =====================================================================================
    // Kamerafahrt
    // =====================================================================================

    private void updateCamera(double t, boolean snap) {
        if (this.camera == null || this.flight == null) {
            return;
        }
        IntroFlight.Shot shot = this.flight.camera(t);
        snap |= shot.cut();
        Vec3 wanted = shot.position();
        Vec3 target = shot.target();
        double dx = target.x - wanted.x;
        double dy = target.y - wanted.y;
        double dz = target.z - wanted.z;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        yaw = this.lastYaw + Mth.wrapDegrees(yaw - this.lastYaw);   // stetig, kein Sprung bei ±180°
        if (!snap) {
            // ruhige Kameraführung: Drehungen leicht nachziehen
            yaw = this.lastYaw + (yaw - this.lastYaw) * 0.35f;
            pitch = this.lastPitch + (pitch - this.lastPitch) * 0.35f;
        }
        this.lastYaw = yaw;
        this.lastPitch = pitch;
        if (snap) {
            this.camera.snapTo(wanted.x, wanted.y, wanted.z, yaw, pitch);
        } else {
            this.camera.setOldPosAndRot();
            this.camera.setPos(wanted.x, wanted.y, wanted.z);
            this.camera.setYRot(yaw);
            this.camera.setXRot(pitch);
        }
    }

    // =====================================================================================
    // Zeichnen
    // =====================================================================================

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        // kein Menühintergrund: die Welt bleibt sichtbar
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        double t = this.elapsed();
        int w = this.width;
        int h = this.height;
        if (this.flight != null) {
            this.drawMood(graphics, t, w, h);
        }
        double title = smooth((t - this.script.titleCardStart()) / 1.6);
        if (title > 0.0) {
            graphics.fill(0, 0, w, h, ARGB.black((float) (0.55 * title)));
        }
        int bar = (int) Math.round(h * 0.12 * (this.startNanos < 0 ? 1.0 : 0.35 + 0.65 * smooth(t / 1.5)));
        graphics.fill(0, 0, w, bar, 0xFF000000);
        graphics.fill(0, h - bar, w, h, 0xFF000000);
        this.drawLine(graphics, t, w, h, bar);
        if (title > 0.0) {
            this.drawTitle(graphics, t, w, h, (float) title);
        }
        double black = this.startNanos < 0 ? 1.0
                : Math.max(1.0 - smooth(t / 2.5), smooth((t - (this.script.length() - 1.8)) / 1.6));
        if (black > 0.003) {
            graphics.fill(0, 0, w, h, ARGB.black((float) black));
        }
        Component skip = Component.translatable("intro.kingdomsatwar.skip");
        graphics.text(this.font, skip, w - this.font.width(skip) - 8, h - Math.max(12, bar / 2 + 4), 0xFF8C8577, false);
    }

    /**
     * Stimmung über dem Bild: die Dunkelheit (violett-schwarz, von unten nach oben dichter), der Blitz beim Schmieden
     * des Siegels, das Abblenden bei harten Schnitten und das violette Pochen am Rand zum Schluss.
     */
    private void drawMood(GuiGraphicsExtractor graphics, double t, int w, int h) {
        IntroFlight flight = this.flight;
        double dark = flight.darkness(t);
        if (dark > 0.005) {
            graphics.fillGradient(0, 0, w, h, ARGB.color((int) (dark * 120), 0x0A, 0x04, 0x12), ARGB.color((int) (dark * 205), 0x16, 0x06, 0x22));
            int edge = Math.max(8, w / 6);
            int shade = ARGB.color((int) (dark * 170), 0x05, 0x02, 0x0A);
            graphics.fillGradient(0, 0, w, h / 5, shade, ARGB.color(0, 0x05, 0x02, 0x0A));
            this.sideShade(graphics, w, h, edge, shade);
        }
        double pulse = flight.pulse(t);
        if (pulse > 0.01) {
            int edge = Math.max(10, w / 5);
            int shade = ARGB.color((int) (pulse * 150), 0x3A, 0x0C, 0x55);
            graphics.fillGradient(0, h - h / 3, w, h, ARGB.color(0, 0x3A, 0x0C, 0x55), shade);
            this.sideShade(graphics, w, h, edge, shade);
        }
        double flash = flight.screenFlash(t);
        if (flash > 0.005) {
            graphics.fill(0, 0, w, h, ARGB.color((int) (flash * 235), 0xFF, 0xF8, 0xFF));
        }
        double dip = flight.dip(t);
        if (dip > 0.005) {
            graphics.fill(0, 0, w, h, ARGB.black((float) dip));
        }
    }

    /** Seitliche Abdunkelung (links und rechts je ein Verlauf zur Mitte). */
    private void sideShade(GuiGraphicsExtractor graphics, int w, int h, int edge, int shade) {
        int clear = ARGB.color(0, ARGB.red(shade), ARGB.green(shade), ARGB.blue(shade));
        graphics.pose().pushMatrix();
        graphics.pose().translate(edge, 0.0f);
        graphics.pose().rotate((float) (Math.PI / 2.0));
        graphics.fillGradient(0, 0, h, edge, clear, shade);
        graphics.pose().popMatrix();
        graphics.pose().pushMatrix();
        graphics.pose().translate(w - edge, h);
        graphics.pose().rotate((float) (-Math.PI / 2.0));
        graphics.fillGradient(0, 0, h, edge, clear, shade);
        graphics.pose().popMatrix();
    }

    /** Aktuelle Erzählzeile mittig über dem unteren Balken, weich ein- und ausgeblendet. */
    private void drawLine(GuiGraphicsExtractor graphics, double t, int w, int h, int bar) {
        IntroScript.Line current = null;
        double fade = 0.0;
        for (int i = 0; i < this.lines.size(); i++) {
            IntroScript.Line line = this.lines.get(i);
            double next = i + 1 < this.lines.size() ? this.lines.get(i + 1).start() : this.script.titleCardStart();
            double hideAt = Math.min(line.end() + 1.2, next - 0.25);
            if (t >= line.start() - 0.15 && t < hideAt) {
                current = line;
                fade = Math.min(smooth((t - (line.start() - 0.15)) / 0.45), smooth((hideAt - t) / 0.5));
            }
        }
        if (current == null || fade < 0.03) {
            return;
        }
        float scale = Mth.clamp(w / 380.0f, 1.0f, 1.6f);
        List<FormattedCharSequence> rows = this.font.split(Component.translatable(current.key()), (int) (w * 0.72 / scale));
        int lineHeight = 11;
        float top = h - bar - 12 - rows.size() * lineHeight * scale;
        int widest = 0;
        for (FormattedCharSequence row : rows) {
            widest = Math.max(widest, this.font.width(row));
        }
        int half = (int) (widest * scale / 2) + 12;
        graphics.fill(w / 2 - half, (int) top - 6, w / 2 + half, (int) (top + rows.size() * lineHeight * scale) + 4,
                ARGB.black((float) (0.38 * fade)));
        graphics.pose().pushMatrix();
        graphics.pose().translate(w / 2.0f, top);
        graphics.pose().scale(scale, scale);
        int color = ARGB.color((float) fade, TEXT_COLOR);
        for (int i = 0; i < rows.size(); i++) {
            graphics.centeredText(this.font, rows.get(i), 0, i * lineHeight, color);
        }
        graphics.pose().popMatrix();
    }

    /** Titelkarte: Kingdoms-at-War-Banner, darunter der Dragon-Warfare-Schriftzug. */
    private void drawTitle(GuiGraphicsExtractor graphics, double t, int w, int h, float alpha) {
        int tw = (int) Math.min(w * 0.62, TITLE_W * (h * 0.32 / TITLE_H));
        int th = tw * TITLE_H / TITLE_W;
        int x = (w - tw) / 2;
        int y = (int) (h * 0.2);
        graphics.blit(RenderPipelines.GUI_TEXTURED, TITLE, x, y, 0.0f, 0.0f, tw, th, TITLE_W, TITLE_H, TITLE_W, TITLE_H, ARGB.white(alpha));
        float second = (float) smooth((t - this.script.titleCardStart() - 0.9) / 1.4);
        if (second > 0.0f) {
            int dw = (int) (tw * 0.8);
            int dh = dw * DW_H / DW_W;
            graphics.blit(RenderPipelines.GUI_TEXTURED, DRAGON_WARFARE, (w - dw) / 2, y + th + 4, 0.0f, 0.0f, dw, dh, DW_W, DW_H, DW_W, DW_H,
                    ARGB.white(second));
        }
    }

    private static double smooth(double v) {
        double c = Mth.clamp(v, 0.0, 1.0);
        return c * c * (3.0 - 2.0 * c);
    }

    // =====================================================================================
    // Bedienung und Aufräumen
    // =====================================================================================

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (event.key() == InputConstants.KEY_SPACE || event.key() == InputConstants.KEY_RETURN) {
            this.onClose();
            return true;
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public Music getBackgroundMusic() {
        return this.music;
    }

    @Override
    public void removed() {
        this.cleanUp();
    }

    private void cleanUp() {
        if (this.cleanedUp) {
            return;
        }
        this.cleanedUp = true;
        for (SoundInstance voice : this.voices) {
            this.minecraft.getSoundManager().stop(voice);
        }
        this.minecraft.getMusicManager().stopPlaying(this.music);
        if (this.flight != null) {
            this.flight.removeAll();
        }
        if (this.minecraft.player != null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }
        if (!this.hudWasHidden && this.minecraft.gui.hud.isHidden()) {
            this.minecraft.gui.hud.toggle();
        }
    }
}
