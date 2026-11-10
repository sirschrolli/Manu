package io.github.sirschrolli.kingdomsatwar.client.creator;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import java.util.List;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.Holder;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.event.SelectMusicEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import org.jspecify.annotations.Nullable;

/**
 * Was Schrolli der Schöpfer auf dem Bildschirm bewirkt:
 * <ul>
 *     <li>steht die Zeit still, wird alles grau und kalt;</li>
 *     <li>„Es werde Licht!“ – wer hinsieht, dem wird es gleißend weiß vor Augen; der Aufstieg in die Wahre Gestalt, die
 *     Supernova, der Urknall und sein Vergehen blenden alle kurz;</li>
 *     <li>die Musik: in seiner Nähe die Kampfmusik des Schöpfers, in der Wahren Gestalt ihre entfesselte Fassung (im Hof
 *     sonst die stille Musik der Schöpfung).</li>
 * </ul>
 * Außerdem merkt sich diese Klasse, wie weit der Himmel der Schöpfung „aufgerissen“ ist (für den {@link CreationSkyRenderer}).
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class CreatorClient {
    public static final Identifier LAYER = KingdomsAtWar.id("creator_overlay");
    private static final Music BATTLE = music("music.creation_battle");
    private static final Music TRUE = music("music.creation_true");

    private static @Nullable SchrolliEntity near;
    private static int lastAttack = -1;
    private static float lastTime = -1.0f;
    private static float grey;
    private static float greyO;
    private static float flash;
    private static float flashO;
    private static int flashColor = 0xFFFFFF;
    /** Wie weit die Wahre Gestalt den Himmel verwandelt hat (0 – 1), und wie stark er gerade aufflammt. */
    private static float reveal;
    private static float revealO;
    private static float flare;

    private CreatorClient() {}

    private static Music music(String name) {
        Holder<SoundEvent> sound = Holder.direct(SoundEvent.createVariableRangeEvent(KingdomsAtWar.id(name)));
        return new Music(sound, 20, 60, true);
    }

    /** Der nächste Schöpfer in der Nähe (wird jeden Tick neu gesucht). */
    public static @Nullable SchrolliEntity nearest() {
        return near;
    }

    public static float reveal(float partialTicks) {
        return Mth.lerp(partialTicks, revealO, reveal);
    }

    public static float flare() {
        return flare;
    }

    @SubscribeEvent
    static void registerLayers(RegisterGuiLayersEvent event) {
        event.registerAbove(VanillaGuiLayers.CAMERA_OVERLAYS, LAYER, CreatorClient::render);
    }

    @SubscribeEvent
    static void onClientTick(ClientTickEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        LocalPlayer player = minecraft.player;
        ClientLevel level = minecraft.level;
        greyO = grey;
        flashO = flash;
        revealO = reveal;
        flash = Math.max(0.0f, flash - 0.035f);
        flare = Math.max(0.0f, flare - 0.02f);
        if (player == null || level == null) {
            near = null;
            grey = 0.0f;
            return;
        }
        SchrolliEntity boss = null;
        double best = Double.MAX_VALUE;
        List<SchrolliEntity> found = level.getEntitiesOfClass(SchrolliEntity.class, new AABB(player.blockPosition()).inflate(220.0));
        for (SchrolliEntity s : found) {
            double d = s.distanceToSqr(player);
            if (d < best) {
                best = d;
                boss = s;
            }
        }
        near = boss;
        boolean frozen = false;
        float wantReveal = 0.0f;
        if (boss != null) {
            Attack attack = boss.currentAttack();
            float time = boss.attackTime(0.0f);
            int ordinal = attack.ordinal();
            boolean same = ordinal == lastAttack;
            float from = same ? lastTime : -1.0f;
            frozen = attack == Attack.TIMESTOP && time >= SchrolliEntity.TIME_FREEZE && time < SchrolliEntity.TIME_RESUME;
            if (attack == Attack.WORD && crossed(from, time, SchrolliEntity.WORD_FLASH)) {
                Vec3 to = boss.position().add(0.0, boss.getBbHeight() * 0.9, 0.0).subtract(player.getEyePosition()).normalize();
                boolean sees = player.getViewVector(1.0f).dot(to) > 0.45;
                flash(sees ? 1.0f : 0.25f, 0xFFFFFF);
            }
            if (attack == Attack.ASCEND && crossed(from, time, SchrolliEntity.ASCEND_SHATTER)) {
                flash(0.8f, 0xFFF6E0);
            }
            if (attack == Attack.ASCEND && crossed(from, time, SchrolliEntity.ASCEND_REVEAL)) {
                flash(1.0f, 0xE8D0FF);
                flare = 1.0f;
            }
            if (attack == Attack.SUPERNOVA && crossed(from, time, SchrolliEntity.NOVA_BLAST)) {
                flash(0.95f, 0xFFE8B0);
                flare = 1.0f;
            }
            if (attack == Attack.GENESIS && crossed(from, time, SchrolliEntity.GENESIS_BANG)) {
                flash(0.6f, 0xD8B0FF);
                flare = 0.8f;
            }
            if (attack == Attack.APOCALYPSE) {
                flare = Math.max(flare, 0.45f + Mth.sin(time * 0.3f) * 0.15f);
            }
            if (boss.deathTime == SchrolliEntity.DEATH_TICKS - 16) {
                flash(1.0f, 0xFFFFFF);
                flare = 1.0f;
            }
            lastAttack = ordinal;
            lastTime = time;
            wantReveal = boss.isTrueForm() ? 1.0f : attack == Attack.ASCEND ? boss.ascension(0.0f) * 0.8f : 0.0f;
            if (boss.deathTime > 0) {
                wantReveal *= Math.max(0.0f, 1.0f - boss.deathTime / (float) SchrolliEntity.DEATH_TICKS);
            }
        } else {
            lastAttack = -1;
            lastTime = -1.0f;
        }
        grey = frozen ? Math.min(1.0f, grey + 0.2f) : Math.max(0.0f, grey - 0.12f);
        reveal += (wantReveal - reveal) * 0.03f;
    }

    private static boolean crossed(float from, float to, float at) {
        return from < at && to >= at;
    }

    private static void flash(float strength, int color) {
        if (strength >= flash) {
            flash = strength;
            flashColor = color;
        }
    }

    static void render(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        float pt = delta.getGameTimeDeltaPartialTick(false);
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        float g = Mth.lerp(pt, greyO, grey);
        if (g > 0.0f) {
            // die stillstehende Zeit: alles grau und kalt, an den Rändern dunkler
            graphics.fill(0, 0, width, height, ARGB.color(Math.round(g * 105.0f), 0x9AA4BC));
            int edge = Math.max(8, width / 10);
            for (int i = 0; i < 6; i++) {
                int a = Math.round(g * (60 - i * 10));
                int e = edge * (6 - i) / 6;
                graphics.fill(0, 0, e, height, ARGB.color(a, 0x283040));
                graphics.fill(width - e, 0, width, height, ARGB.color(a, 0x283040));
            }
        }
        float f = Mth.lerp(pt, flashO, flash);
        if (f > 0.0f) {
            graphics.fill(0, 0, width, height, ARGB.color(Math.round(Mth.clamp(f * f, 0.0f, 1.0f) * 235.0f), flashColor));
        }
    }

    /**
     * Die Musik: in der Nähe des Schöpfers seine Kampfmusik (in der Wahren Gestalt die entfesselte Fassung); im Hof
     * ohne ihn bleibt die stille Musik der Schöpfung (aus der Welt selbst).
     */
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onSelectMusic(SelectMusicEvent event) {
        SchrolliEntity boss = near;
        Minecraft minecraft = Minecraft.getInstance();
        if (boss == null || !boss.isAlive() || minecraft.player == null || boss.distanceToSqr(minecraft.player) > 160.0 * 160.0) {
            return;
        }
        boolean truly = boss.isTrueForm() || boss.currentAttack() == Attack.ASCEND && boss.attackTime(0.0f) > SchrolliEntity.ASCEND_REVEAL;
        event.overrideMusic(truly ? TRUE : BATTLE);
    }

    /** Ist der Spieler in der Schöpfung? */
    public static boolean inCreation(ClientLevel level) {
        return level.dimension() == BossArenaKind.CREATOR.level();
    }
}
