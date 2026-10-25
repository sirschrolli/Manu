package io.github.sirschrolli.kingdomsatwar.client.intro;

import com.mojang.blaze3d.platform.InputConstants;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.ARGB;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Das Erzähl-Intro beim ersten Betreten einer Welt: Eine Kamera kreist langsam steigend um den Spieler,
 * schwarze Kinobalken, der Erzähler spricht neun Zeilen (Text erscheint dazu), Musik läuft, im
 * Drachen-Teil fliegen drei Drachen über die Landschaft, am Ende die Titelkarte. Leertaste, Enter
 * oder Esc überspringen.
 *
 * <p>Die Drachen und die Kamera gibt es nur auf dem eigenen Bildschirm (nicht auf dem Server).
 */
public class IntroScreen extends Screen {
    private static final Identifier TITLE = KingdomsAtWar.id("textures/gui/intro_title.png");
    private static final int TITLE_W = 520;
    private static final int TITLE_H = 140;
    private static final Identifier DRAGON_WARFARE = KingdomsAtWar.id("textures/gui/intro_dragon_warfare.png");
    private static final int DW_W = 565;
    private static final int DW_H = 103;
    private static final int TEXT_COLOR = 0xF3E7C8;
    /** Eigene, negative Kennungen für die Drachen auf dem Bildschirm, damit sie keinem echten Wesen in die Quere kommen. */
    private static final int FIRST_DRAGON_ID = -2_100_000_000;

    private final IntroScript script;
    private final List<IntroScript.Line> lines;
    private final Music music;
    private final boolean[] spoken;
    private final List<SoundInstance> voices = new ArrayList<>();
    private final List<DragonEntity> dragons = new ArrayList<>();
    private long startNanos = -1;
    private int ticks;
    private @Nullable Marker camera;
    private Vec3 center = Vec3.ZERO;
    private double startAngle;
    private float lastYaw;
    private double cameraLift;
    private boolean hudWasHidden;
    private boolean dragonsRoared;
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
        this.center = player.position();
        double yaw = Math.toRadians(player.getYRot());
        // Kamera beginnt hinter dem Spieler (Blickrichtung des Spielers = (-sin, cos))
        this.startAngle = Math.atan2(-Math.cos(yaw), Math.sin(yaw));
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
        this.updateCamera(t, false);
        this.updateDragons(t);
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
        ClientLevel level = this.minecraft.level;
        if (this.camera == null || level == null) {
            return;
        }
        double ease = smooth(t / this.script.length());
        // im Drachen-Teil hebt sich der Blick zum Himmel und folgt dem großen Feuerdrachen
        double dragonsStart = this.script.section("dragons", 20.0);
        double dragonsEnd = this.script.section("wonder", 36.667);
        double lookUp = smooth((t - dragonsStart) / 3.0) * (1.0 - smooth((t - dragonsEnd) / 3.0));
        double radius = 4.5 + 11.5 * ease - 2.0 * lookUp;
        double height = 1.6 + 15.0 * ease - 3.0 * lookUp;
        double angle = this.startAngle + Math.toRadians(t * 3.0);
        Vec3 head = this.center.add(0.0, 1.4 + 2.5 * ease, 0.0);
        Vec3 target = head;
        if (lookUp > 0.0 && !this.dragons.isEmpty()) {
            Vec3 dragon = this.dragonPosition(0, t, dragonsStart - 1.0, dragonsEnd + 3.0).add(0.0, 2.0, 0.0);
            target = head.lerp(dragon, lookUp * 0.85);
        }
        Vec3 wanted = new Vec3(this.center.x + Math.cos(angle) * radius, this.center.y + height, this.center.z + Math.sin(angle) * radius);
        // Nicht in Bäumen oder Bergen stecken: so weit anheben, bis die Kamera frei ist (weich nachgeführt) ...
        BlockPos.MutableBlockPos pos = BlockPos.containing(wanted).mutable();
        int lift = 0;
        while (lift < 24 && !level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
            pos.move(0, 1, 0);
            lift++;
        }
        this.cameraLift = snap ? lift : this.cameraLift + (lift - this.cameraLift) * 0.25;
        wanted = wanted.add(0.0, this.cameraLift, 0.0);
        // ... und wenn Blätter oder Felsen zwischen Kamera und Spieler liegen, näher heranrücken
        BlockHitResult hit = level.clip(new ClipContext(head, wanted, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, this.camera));
        if (hit.getType() == HitResult.Type.BLOCK) {
            Vec3 toCamera = wanted.subtract(head);
            double free = Math.max(1.5, hit.getLocation().distanceTo(head) - 0.4);
            wanted = head.add(toCamera.normalize().scale(Math.min(free, toCamera.length())));
        }
        double dx = target.x - wanted.x;
        double dy = target.y - wanted.y;
        double dz = target.z - wanted.z;
        float yaw = (float) Math.toDegrees(Math.atan2(-dx, dz));
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)));
        yaw = this.lastYaw + Mth.wrapDegrees(yaw - this.lastYaw);   // stetig, kein Sprung bei ±180°
        this.lastYaw = yaw;
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
    // Drachen am Himmel
    // =====================================================================================

    private void updateDragons(double t) {
        ClientLevel level = this.minecraft.level;
        if (level == null) {
            return;
        }
        double start = this.script.section("dragons", 20.0) - 1.0;
        double end = this.script.section("wonder", 36.667) + 3.0;
        if (this.dragons.isEmpty() && t >= start && t < end - 4.0) {
            this.spawnDragons(level, start);
        }
        if (this.dragons.isEmpty()) {
            return;
        }
        if (!this.dragonsRoared && t >= start + 1.2) {
            this.dragonsRoared = true;
            this.play(ModSounds.DRAGON_ROAR.get().location(), SoundSource.NEUTRAL, 0.9f, 0.85f);
        }
        for (int i = 0; i < this.dragons.size(); i++) {
            Vec3 position = this.dragonPosition(i, t, start, end);
            Vec3 ahead = this.dragonPosition(i, t + 0.25, start, end);
            float yaw = (float) Math.toDegrees(Math.atan2(-(ahead.x - position.x), ahead.z - position.z));
            DragonEntity dragon = this.dragons.get(i);
            dragon.moveOrInterpolateTo(position, yaw, 0.0f);
            dragon.setYBodyRot(yaw);
            dragon.setYHeadRot(yaw);
        }
        if (t >= end) {
            this.removeDragons();
        }
    }

    /** Kreisbahn um den Spieler: anfliegen (4 s), kreisen, zum Schluss steigend davonfliegen. */
    private Vec3 dragonPosition(int i, double t, double start, double end) {
        double approach = 1.0 - smooth((t - start) / 4.0);
        double leave = smooth((t - (end - 4.0)) / 4.0);
        double radius = 24.0 + i * 7.0 + approach * 70.0 + leave * 90.0;
        double height = 13.0 + i * 5.0 + Math.sin(t * 0.6 + i) * 1.5 + leave * 25.0;
        double direction = i % 2 == 0 ? 1.0 : -1.0;
        double angle = this.startAngle + i * 2.1 + direction * (t - start) * 0.32;
        return new Vec3(this.center.x + Math.cos(angle) * radius, this.center.y + height, this.center.z + Math.sin(angle) * radius);
    }

    private void spawnDragons(ClientLevel level, double start) {
        DragonType[] types = {DragonType.FIRE, DragonType.ICE, DragonType.LIGHTNING};
        for (int i = 0; i < types.length; i++) {
            DragonEntity dragon = ModEntities.DRAGONS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
            if (dragon == null) {
                continue;
            }
            dragon.setId(FIRST_DRAGON_ID - i);
            dragon.applyStage(i == 0 ? DragonStage.ELDER : DragonStage.ADULT, false);
            dragon.setFlying(true);
            dragon.setSilent(true);
            Vec3 position = this.dragonPosition(i, start, start, start + 100.0);
            dragon.snapTo(position.x, position.y, position.z, 0.0f, 0.0f);
            level.addEntity(dragon);
            this.dragons.add(dragon);
        }
    }

    private void removeDragons() {
        ClientLevel level = this.minecraft.level;
        for (DragonEntity dragon : this.dragons) {
            if (level != null) {
                level.removeEntity(dragon.getId(), Entity.RemovalReason.DISCARDED);
            }
        }
        this.dragons.clear();
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
        this.removeDragons();
        if (this.minecraft.player != null) {
            this.minecraft.setCameraEntity(this.minecraft.player);
        }
        if (!this.hudWasHidden && this.minecraft.gui.hud.isHidden()) {
            this.minecraft.gui.hud.toggle();
        }
    }
}
