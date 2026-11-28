package io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Die Sternenweberin: Sie läuft im Gang der Spinnen (je vier Beine zugleich, über Kreuz), der Hinterleib atmet, die Taster
 * tasten, die Kieferklauen zucken. Hängt sie am Faden, zieht sie die Beine an (die Drehung kopfüber besorgt der Renderer).
 * Jeder Angriff hat seine Gebärde: sich aufbäumen und die Vorderbeine heben (Fangfaden, Netzwurf, Zorn), mit den Vorderbeinen
 * Sterne in die Luft zeichnen (Sternbild), auf das Netz trommeln (Sternschnuppen), mit den Vorderbeinen umwickeln (Umgarnen),
 * den Hinterleib senken und pulsieren lassen (Brut), zuschnappen (Giftbiss), die Beine flach ausstrecken (Spindel). Im Tod
 * rollt sie die Beine ein. Der Sternspinnling trägt dasselbe Modell, klein.
 *
 * <p>Alle Haltungen werden zur Ruhehaltung des Modells addiert. Die Beine hängen an der Wurzel (damit die Füße stehen, wenn
 * der Leib sich hebt); ihre Hüften folgen dem Leib, wenn er sich aufbäumt.
 */
public class StarWeaverModel extends EntityModel<StarWeaverRenderState> {
    /** Die Hüften: links 1–4 (Index 0–3), rechts 1–4 (Index 4–7). */
    private final ModelPart[] hips = new ModelPart[8];
    private final ModelPart[] femurs = new ModelPart[8];
    private final ModelPart[] tibias = new ModelPart[8];
    private final ModelPart[] tarsi = new ModelPart[8];
    private final float[] side = new float[8];
    /** Gang: zwei Gruppen von je vier Beinen (links 1 und 3 mit rechts 2 und 4, und umgekehrt). */
    private static final float[] PHASE = {0.0f, Mth.PI, 0.0f, Mth.PI, Mth.PI, 0.0f, Mth.PI, 0.0f};
    private final ModelPart body;
    private final ModelPart abdomen;
    private final ModelPart spinnerets;
    private final ModelPart[] crystals = new ModelPart[3];
    private final ModelPart fangL;
    private final ModelPart fangR;
    private final ModelPart palpL;
    private final ModelPart palpR;
    private final ModelPart palpLTip;
    private final ModelPart palpRTip;

    public StarWeaverModel(ModelPart root) {
        super(root);
        for (int i = 0; i < 8; i++) {
            String name = "leg_" + (i < 4 ? "l" : "r") + (i % 4 + 1);
            this.hips[i] = root.getChild(name);
            this.femurs[i] = this.hips[i].getChild(name + "_femur");
            this.tibias[i] = this.femurs[i].getChild(name + "_tibia");
            this.tarsi[i] = this.tibias[i].getChild(name + "_tarsus");
            this.side[i] = i < 4 ? 1.0f : -1.0f;
        }
        this.body = root.getChild("body");
        this.abdomen = this.body.getChild("abdomen");
        this.spinnerets = this.abdomen.getChild("spinnerets");
        for (int i = 0; i < 3; i++) {
            this.crystals[i] = this.abdomen.getChild("crystal_" + i);
        }
        ModelPart fangs = this.body.getChild("fangs");
        this.fangL = fangs.getChild("fang_l");
        this.fangR = fangs.getChild("fang_r");
        this.palpL = this.body.getChild("palp_l");
        this.palpR = this.body.getChild("palp_r");
        this.palpLTip = this.palpL.getChild("palp_l_tip");
        this.palpRTip = this.palpR.getChild("palp_r_tip");
    }

    private static StarWeaverEntity.Attack attack(StarWeaverRenderState state) {
        return state.spiderling ? StarWeaverEntity.Attack.NONE
                : StarWeaverEntity.Attack.values()[Mth.clamp(state.attack, 0, StarWeaverEntity.Attack.values().length - 1)];
    }

    @Override
    public void setupAnim(StarWeaverRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        StarWeaverEntity.Attack attack = attack(state);
        float a = state.attackTime;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float pos = state.walkAnimationPos;
        float hang = state.hang;

        // ------------------------------------------------------------ Atem, Taster, Kieferklauen, Blick
        float breath = Mth.sin(t * 0.05f);
        this.abdomen.xRot += breath * 0.025f;
        this.abdomen.xScale = this.abdomen.zScale = 1.0f + breath * 0.015f;
        this.abdomen.yScale = 1.0f - breath * 0.01f;
        this.palpL.xRot += Mth.sin(t * 0.13f) * 0.12f;
        this.palpR.xRot += Mth.sin(t * 0.13f + 1.7f) * 0.12f;
        this.palpLTip.xRot += Mth.sin(t * 0.17f + 0.4f) * 0.15f;
        this.palpRTip.xRot += Mth.sin(t * 0.17f + 2.2f) * 0.15f;
        float twitch = Mth.sin(t * 0.07f) * 0.05f;
        this.fangL.zRot += twitch;
        this.fangR.zRot -= twitch;
        this.body.yRot += Mth.clamp(state.yRot, -30.0f, 30.0f) * Anim.DEG * 0.25f * (1.0f - hang);
        this.spinnerets.xRot += Mth.sin(t * 0.09f) * 0.06f;

        // ------------------------------------------------------------ Gang: je vier Beine zugleich, über Kreuz
        float cycle = pos * 0.9f;
        for (int i = 0; i < 8; i++) {
            float sx = this.side[i];
            float ph = cycle + PHASE[i] + (i % 4) * 0.25f;
            float swing = Mth.sin(ph);
            float lift = Math.max(0.0f, Mth.cos(ph));
            this.hips[i].yRot += swing * 0.3f * speed * sx;
            this.femurs[i].zRot += -lift * 0.4f * speed * sx;
            this.tibias[i].zRot += lift * 0.18f * speed * sx;
            // in Ruhe zucken die Beine kaum merklich
            this.hips[i].yRot += Mth.sin(t * 0.04f + i * 1.3f) * 0.02f * sx;
        }
        this.body.y -= Mth.abs(Mth.sin(cycle * 2.0f)) * 0.35f * speed;
        this.abdomen.yRot += Mth.sin(cycle) * 0.06f * speed;

        // ------------------------------------------------------------ am Faden: die Beine angezogen
        if (hang > 0.001f) {
            for (int i = 0; i < 8; i++) {
                float sx = this.side[i];
                boolean front = i % 4 < 2;
                this.hips[i].yRot += (front ? 0.22f : -0.22f) * hang * sx + Mth.sin(t * 0.05f + i) * 0.04f * hang;
                this.femurs[i].zRot += -0.45f * hang * sx;
                this.tibias[i].zRot += 0.55f * hang * sx;
                this.tarsi[i].zRot += 0.35f * hang * sx;
            }
            this.palpL.xRot += 0.4f * hang;
            this.palpR.xRot += 0.4f * hang;
        }

        // ------------------------------------------------------------ Gebärden der Angriffe
        float rear = 0.0f;
        switch (attack) {
            case AWAKEN -> rear = this.awaken(a, t);
            case BITE -> rear = this.bite(a);
            case THREAD -> rear = this.reach(a, 6.0f, 40.0f, 0.38f, t);
            case WEB -> rear = this.spit(a);
            case CONSTELLATION -> rear = this.draw(a, t);
            case STARFALL -> rear = this.drum(a, t);
            case COCOON -> rear = this.wrap(a, t);
            case BROOD -> this.brood(a, t);
            case DROP -> this.drop(a);
            case SPINDLE -> this.spindle(a);
            case FURY -> rear = this.fury(a, t);
            default -> { }
        }
        if (state.enraged && attack == StarWeaverEntity.Attack.NONE) {
            // im Zorn: unruhig, die Vorderbeine zucken
            this.femurs[0].zRot += -Math.max(0.0f, Mth.sin(t * 0.5f)) * 0.12f;
            this.femurs[4].zRot += Math.max(0.0f, Mth.sin(t * 0.5f + 1.5f)) * 0.12f;
        }
        this.rearUp(rear);

        // ------------------------------------------------------------ Tod: sie rollt die Beine ein
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 30.0f, 0.0f, 1.0f);
            float e = d * d * (3.0f - 2.0f * d);
            for (int i = 0; i < 8; i++) {
                float sx = this.side[i];
                this.femurs[i].zRot += -0.9f * e * sx;
                this.tibias[i].zRot += 1.3f * e * sx;
                this.tarsi[i].zRot += 0.9f * e * sx;
                this.hips[i].y += 2.5f * e;
            }
            this.body.y += 2.5f * e;
            this.abdomen.xRot += -0.2f * e;
            this.palpL.xRot += 0.6f * e;
            this.palpR.xRot += 0.6f * e;
        }
    }

    /**
     * Bäumt den Vorderleib um {@code rear} (Bogenmaß, positiv = Kopf hoch) auf: Der Leib kippt um seine Mitte, die Hüften der
     * Beine gehen mit (sie hängen an der Wurzel).
     */
    private void rearUp(float rear) {
        if (Math.abs(rear) < 1.0e-4f) {
            return;
        }
        float angle = -rear;
        this.body.xRot += angle;
        float sin = Mth.sin(angle);
        float cos = Mth.cos(angle);
        for (int i = 0; i < 8; i++) {
            float z = this.hips[i].getInitialPose().z();
            this.hips[i].y += -z * sin;
            this.hips[i].z += z * (cos - 1.0f);
        }
    }

    /** Hebt ein Vorderbein (Index 0 links, 4 rechts) um {@code k} hoch und streckt es nach vorn. */
    private void raise(int i, float k) {
        float sx = this.side[i];
        this.femurs[i].zRot += -0.85f * k * sx;
        this.tibias[i].zRot += -0.55f * k * sx;
        this.hips[i].yRot += 0.25f * k * sx;
    }

    private void fangsOpen(float k) {
        this.fangL.zRot += -0.45f * k;
        this.fangR.zRot += 0.45f * k;
        this.fangL.xRot += -0.2f * k;
        this.fangR.xRot += -0.2f * k;
    }

    /** Erwachen: vom Faden fallend die Beine gespreizt, beim Aufprall geduckt, dann aufgebäumt mit gehobenen Vorderbeinen. */
    private float awaken(float a, float t) {
        float fall = Anim.bump(a - 8.0f, 4.0f, 12.0f, 18.0f);
        for (int i = 0; i < 8; i++) {
            this.femurs[i].zRot += 0.25f * fall * this.side[i];
            this.tibias[i].zRot += -0.4f * fall * this.side[i];
        }
        float crouch = Anim.bump(a - 18.0f, 3.0f, 6.0f, 14.0f);
        this.body.y += crouch * 2.0f;
        float up = Anim.bump(a - 26.0f, 8.0f, 24.0f, 36.0f);
        this.raise(0, up);
        this.raise(4, up);
        this.raise(1, up * 0.5f);
        this.raise(5, up * 0.5f);
        this.fangsOpen(up);
        this.abdomen.xRot += 0.2f * up;
        this.femurs[0].zRot += Mth.sin(t * 0.8f) * 0.08f * up;
        this.femurs[4].zRot -= Mth.sin(t * 0.8f + 1.0f) * 0.08f * up;
        return 0.45f * up;
    }

    /** Giftbiss: kurz zurück, dann vor und zuschnappen. */
    private float bite(float a) {
        float wind = Anim.bump(a, 5.0f, 7.0f, 10.0f);
        float strike = Anim.bump(a - 8.0f, 3.0f, 5.0f, 12.0f);
        this.fangsOpen(wind - strike * 0.6f);
        this.raise(0, wind * 0.6f);
        this.raise(4, wind * 0.6f);
        return 0.22f * wind - 0.18f * strike;
    }

    /** Fangfaden (und ähnliche): aufbäumen, die Vorderbeine heben, die Klauen offen – und halten. */
    private float reach(float a, float in, float end, float height, float t) {
        float k = Anim.bump(a, in, end - 8.0f, end);
        this.raise(0, k);
        this.raise(4, k);
        this.fangsOpen(k * 0.8f);
        this.palpL.xRot += -0.4f * k + Mth.sin(t * 0.6f) * 0.1f * k;
        this.palpR.xRot += -0.4f * k + Mth.sin(t * 0.6f + 1.0f) * 0.1f * k;
        return height * k;
    }

    /** Netzwurf: aufbäumen, dann den Kopf vorstoßen (spucken). */
    private float spit(float a) {
        float up = Anim.bump(a, 6.0f, 9.0f, 14.0f);
        float jerk = Anim.bump(a - 10.0f, 2.0f, 4.0f, 12.0f);
        this.raise(0, up * 0.7f);
        this.raise(4, up * 0.7f);
        this.fangsOpen(up);
        return 0.35f * up - 0.15f * jerk;
    }

    /** Sternbild: aufgebäumt zeichnet sie mit den Vorderbeinen Kreise in die Luft, die Kristalle auf dem Rücken heben sich. */
    private float draw(float a, float t) {
        float k = Anim.bump(a, 8.0f, 58.0f, 70.0f);
        this.raise(0, k);
        this.raise(4, k);
        this.femurs[0].zRot += Mth.sin(a * 0.35f) * 0.25f * k;
        this.hips[0].yRot += Mth.cos(a * 0.35f) * 0.2f * k;
        this.femurs[4].zRot += -Mth.sin(a * 0.35f + 1.6f) * 0.25f * k;
        this.hips[4].yRot += -Mth.cos(a * 0.35f + 1.6f) * 0.2f * k;
        this.abdomen.xRot += 0.18f * k;
        for (ModelPart c : this.crystals) {
            c.xScale = c.yScale = c.zScale = 1.0f + 0.12f * k * (0.5f + 0.5f * Mth.sin(t * 0.4f));
        }
        return 0.3f * k;
    }

    /** Sternschnuppen: sie trommelt mit den Vorderbeinen auf ihr Netz (abwechselnd, schnell). */
    private float drum(float a, float t) {
        float k = Anim.bump(a - 2.0f, 4.0f, 44.0f, 52.0f);
        float beatL = Math.max(0.0f, Mth.sin(a * 0.8f));
        float beatR = Math.max(0.0f, Mth.sin(a * 0.8f + Mth.PI));
        this.femurs[0].zRot += -0.6f * beatL * k;
        this.tibias[0].zRot += -0.3f * beatL * k;
        this.femurs[4].zRot += 0.6f * beatR * k;
        this.tibias[4].zRot += 0.3f * beatR * k;
        this.abdomen.xRot += 0.1f * k + Mth.sin(t * 0.8f) * 0.03f * k;
        return 0.12f * k;
    }

    /** Umgarnen: die Vorderbeine greifen vor und wickeln abwechselnd, der Hinterleib schwingt hin und her. */
    private float wrap(float a, float t) {
        float k = Anim.bump(a - 4.0f, 8.0f, 40.0f, 50.0f);
        for (int i : new int[] {0, 1, 4, 5}) {
            float sx = this.side[i];
            float w = Mth.sin(a * 0.55f + i * 1.3f);
            this.femurs[i].zRot += (-0.5f - 0.3f * w) * k * sx;
            this.tibias[i].zRot += (-0.3f + 0.35f * w) * k * sx;
            this.hips[i].yRot += 0.35f * k * sx;
        }
        this.abdomen.yRot += Mth.sin(a * 0.5f) * 0.25f * k;
        this.abdomen.xRot += -0.12f * k;
        this.fangsOpen(0.5f * k);
        return 0.15f * k;
    }

    /** Brut: der Hinterleib senkt sich und pulsiert. */
    private void brood(float a, float t) {
        float k = Anim.bump(a - 2.0f, 8.0f, 30.0f, 38.0f);
        float pulse = 0.06f * Mth.sin(a * 0.9f) * k;
        this.abdomen.xRot += -0.35f * k;
        this.abdomen.xScale *= 1.0f + pulse;
        this.abdomen.yScale *= 1.0f + pulse;
        this.abdomen.zScale *= 1.0f + pulse;
        this.body.y += 0.8f * k;
    }

    /** Abseilen: hängend die Beine angezogen (siehe oben), im Fallen gespreizt, beim Aufprall geduckt. */
    private void drop(float a) {
        float fall = Anim.bump(a - StarWeaverEntity.DROP_FALL, 2.0f, 8.0f, 14.0f);
        for (int i = 0; i < 8; i++) {
            this.femurs[i].zRot += 0.3f * fall * this.side[i];
            this.tibias[i].zRot += -0.45f * fall * this.side[i];
        }
        float crouch = Anim.bump(a - StarWeaverEntity.DROP_FALL - 8.0f, 3.0f, 6.0f, 16.0f);
        this.body.y += crouch * 2.2f;
        this.fangsOpen(fall);
    }

    /** Die Spindel: die Beine flach ausgestreckt (sie dreht sich – das macht das Wesen). */
    private void spindle(float a) {
        float k = Anim.bump(a, 10.0f, 140.0f, 150.0f);
        for (int i = 0; i < 8; i++) {
            float sx = this.side[i];
            this.femurs[i].zRot += 0.3f * k * sx;
            this.tibias[i].zRot += -0.55f * k * sx;
            this.tarsi[i].zRot += -0.2f * k * sx;
        }
        this.body.y += 0.6f * k;
    }

    /** Zorn: hoch aufgebäumt, alle Beine gespreizt, die Klauen weit offen, der Hinterleib erhoben. */
    private float fury(float a, float t) {
        float k = Anim.bump(a, 6.0f, 26.0f, 36.0f);
        this.raise(0, k * 1.1f);
        this.raise(4, k * 1.1f);
        this.raise(1, k * 0.6f);
        this.raise(5, k * 0.6f);
        for (int i = 2; i < 8; i++) {
            if (i == 4 || i == 5) {
                continue;
            }
            this.hips[i].yRot += Mth.sin(t * 0.9f + i) * 0.06f * k;
        }
        this.fangsOpen(k * 1.2f);
        this.abdomen.xRot += 0.3f * k;
        return 0.55f * k;
    }
}
