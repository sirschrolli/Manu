package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Schrolli der Schöpfer. Er schwebt und wiegt sich, die sechs Schwingen atmen in Wellen (oben, Mitte, unten versetzt), der
 * Heiligenschein dreht sich langsam, Gewand und Haar wehen. Für jede Kunst nimmt er seine Haltung ein (weich ein- und
 * ausgeblendet): das Zepter emporgereckt, beide Arme zum Himmel, das Zepter auf sein Ziel gerichtet, den Heiligenschein
 * oder die Weltkugel geworfen, Schwingen gespreizt und geschlagen, die Arme weit ausgebreitet, kniend, wenn er taumelt.
 * In der Wahren Gestalt drehen sich die Räder mit den Augen um ihn, hinter dem Heiligenschein steht der Strahlenkranz,
 * und seine Krone ist größer.
 *
 * <p>Am Boden (seit 0.76) wachsen aus dem Saum Beine in goldenen Beinschienen, das Zepter wird zur Klinge aus Licht (die
 * Weltkugel vergeht), und er steht in Kampfhaltung: das linke Bein vor, die Klinge schräg nach vorn gerichtet, die
 * Schwingen halb angelegt. Er schreitet (die Sohlen bleiben am Boden, das Gewand öffnet sich mit den Schritten), holt aus
 * und schlägt durch – schräge Schnitte, waagrechte Hiebe, der Spaltschlag mit beiden Händen, der Stoß im Sturmlauf, der
 * Wirbel, der Sprung und das Knien nach dem Himmelssturz, die Schwingen als Schild vor sich, der Gegenschlag; in der
 * Wahren Gestalt reckt er die Weltenklinge empor, die zu gewaltiger Länge wächst.
 */
public class SchrolliModel extends EntityModel<SchrolliRenderState> {
    private static final float D = Anim.DEG;
    private static final Attack[] ATTACKS = Attack.values();
    private static final String[] WINGS = {"upper", "mid", "lower"};

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart hairBack;
    private final ModelPart hairTip;
    private final ModelPart crown;
    private final ModelPart halo;
    private final ModelPart haloInner;
    private final ModelPart corona;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart foreL;
    private final ModelPart foreR;
    private final ModelPart scepter;
    private final ModelPart scepterHead;
    private final ModelPart orb;
    private final ModelPart robe;
    private final ModelPart[] robePanels = new ModelPart[4];
    private final ModelPart[] robeLows = new ModelPart[4];
    /** [Lage][Glied] links und rechts. */
    private final ModelPart[][] wingsL = new ModelPart[3][3];
    private final ModelPart[][] wingsR = new ModelPart[3][3];
    private final ModelPart rings;
    private final ModelPart ringA;
    private final ModelPart ringB;
    // am Boden (seit 0.76)
    private final ModelPart blade;
    private final ModelPart bladeEdge;
    private final ModelPart legL;
    private final ModelPart legR;
    private final ModelPart shinL;
    private final ModelPart shinR;
    private final ModelPart footL;
    private final ModelPart footR;

    public SchrolliModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.hairBack = this.head.getChild("hair_back");
        this.hairTip = this.hairBack.getChild("hair_tip");
        this.crown = this.head.getChild("crown");
        this.halo = this.head.getChild("halo");
        this.haloInner = this.halo.getChild("halo_inner");
        this.corona = this.halo.getChild("true_corona");
        this.armL = this.body.getChild("left_arm");
        this.armR = this.body.getChild("right_arm");
        this.foreL = this.armL.getChild("left_forearm");
        this.foreR = this.armR.getChild("right_forearm");
        this.scepter = this.foreR.getChild("scepter");
        this.scepterHead = this.scepter.getChild("scepter_head");
        this.orb = this.foreL.getChild("orb");
        this.robe = this.body.getChild("robe");
        String[] panels = {"robe_front", "robe_back", "robe_left", "robe_right"};
        for (int i = 0; i < 4; i++) {
            this.robePanels[i] = this.robe.getChild(panels[i]);
            this.robeLows[i] = this.robePanels[i].getChild(panels[i] + "_low");
        }
        for (int w = 0; w < 3; w++) {
            for (String side : new String[] {"left", "right"}) {
                ModelPart a = this.body.getChild("wing_" + WINGS[w] + "_" + side);
                ModelPart b = a.getChild("wing_" + WINGS[w] + "_" + side + "_mid");
                ModelPart c = b.getChild("wing_" + WINGS[w] + "_" + side + "_tip");
                ModelPart[] target = side.equals("left") ? this.wingsL[w] : this.wingsR[w];
                target[0] = a;
                target[1] = b;
                target[2] = c;
            }
        }
        this.rings = this.body.getChild("true_rings");
        this.ringA = this.rings.getChild("true_ring_a");
        this.ringB = this.rings.getChild("true_ring_b");
        this.blade = this.foreR.getChild("blade");
        this.bladeEdge = this.blade.getChild("blade_edge");
        this.legL = root.getChild("left_leg");
        this.legR = root.getChild("right_leg");
        this.shinL = this.legL.getChild("left_shin");
        this.shinR = this.legR.getChild("right_shin");
        this.footL = this.shinL.getChild("left_foot");
        this.footR = this.shinR.getChild("right_foot");
    }

    /** Ist die Klinge gerade zu sehen (am Boden)? */
    public boolean bladeVisible() {
        return this.blade.visible;
    }

    /** Führt {@code poseStack} vom Ursprung des Modells bis in die Klinge (Knochen blade_edge; ihre Spitze liegt bei y -31). */
    public void toBlade(com.mojang.blaze3d.vertex.PoseStack poseStack) {
        this.root().translateAndRotate(poseStack);
        this.body.translateAndRotate(poseStack);
        this.armR.translateAndRotate(poseStack);
        this.foreR.translateAndRotate(poseStack);
        this.blade.translateAndRotate(poseStack);
        this.bladeEdge.translateAndRotate(poseStack);
    }

    /** Dreht einen Teil (gewichtet) auf eine feste Haltung in Grad – statt sie hinzuzufügen. */
    private static void toward(ModelPart part, float w, float x, float y, float z) {
        if (w <= 0.0f) {
            return;
        }
        w = Math.min(w, 1.0f);
        part.xRot += (x * D - part.xRot) * w;
        part.yRot += (y * D - part.yRot) * w;
        part.zRot += (z * D - part.zRot) * w;
    }

    /** Der rechte Arm samt Klinge: Oberarm (x, y, z), Unterarm (x), Klinge (x) – in Grad, gewichtet. */
    private void sword(float w, float ax, float ay, float az, float fx, float bx) {
        toward(this.armR, w, ax, ay, az);
        toward(this.foreR, w, fx, 0.0f, 0.0f);
        toward(this.blade, w, bx, 0.0f, -10.0f);
    }

    /** Der linke Arm: Oberarm (x, y, z), Unterarm (x). */
    private void off(float w, float ax, float ay, float az, float fx) {
        toward(this.armL, w, ax, ay, az);
        toward(this.foreL, w, fx, 0.0f, 0.0f);
    }

    /** Die Beine: Oberschenkel und Schienbein links, rechts (die Sohlen bleiben waagrecht). */
    private void legs(float w, float lx, float lsx, float rx, float rsx) {
        toward(this.legL, w, lx, 0.0f, -5.0f);
        toward(this.shinL, w, lsx, 0.0f, 0.0f);
        toward(this.footL, w, -(lx + lsx), 0.0f, 0.0f);
        toward(this.legR, w, rx, 0.0f, 5.0f);
        toward(this.shinR, w, rsx, 0.0f, 0.0f);
        toward(this.footR, w, -(rx + rsx), 0.0f, 0.0f);
    }

    /** Geduckt (0 Kampfhaltung – 1 tief in den Knien), das linke Bein vorn. */
    private void crouch(float w, float c) {
        this.legs(w, -16.0f - 46.0f * c, 14.0f + 62.0f * c, 14.0f + 10.0f * c, 16.0f + 58.0f * c);
    }

    /**
     * Setzt ein Bein auf den Boden (y 24): verschiebt das Hüftgelenk um so viel, wie Oberschenkel, Schienbein und Sohle in
     * ihrer Haltung kürzer sind als ausgestreckt. Gibt die Verschiebung zurück.
     */
    private static float plant(ModelPart leg, ModelPart shin, ModelPart foot) {
        float a = leg.xRot;
        float b = a + shin.xRot;
        float c = b + foot.xRot;
        float splay = Mth.cos(leg.zRot);
        float sole = 2.8f * Mth.cos(c) + (c > 0.0f ? 5.7f * Mth.sin(c) : -1.9f * Mth.sin(c));
        float reach = (11.0f * Mth.cos(a) + 9.6f * Mth.cos(b)) * splay + sole;
        float drop = 23.4f - reach;
        leg.y += drop;
        return drop;
    }

    private static void add(ModelPart part, float w, float x, float y, float z) {
        if (w != 0.0f) {
            part.xRot += x * D * w;
            part.yRot += y * D * w;
            part.zRot += z * D * w;
        }
    }

    /** Hebt (negativ) oder senkt (positiv) eine Schwinge – links und rechts spiegelbildlich. */
    private void wing(int w, int joint, float weight, float degrees) {
        if (weight != 0.0f) {
            this.wingsL[w][joint].zRot += degrees * D * weight;
            this.wingsR[w][joint].zRot -= degrees * D * weight;
        }
    }

    /** Biegt ein Glied einer Schwinge um die Hochachse: positiv nach vorn und einwärts (wie ein Schild um ihn). */
    private void fold(int w, int joint, float weight, float degrees) {
        if (weight != 0.0f) {
            this.wingsL[w][joint].yRot += degrees * D * weight;
            this.wingsR[w][joint].yRot -= degrees * D * weight;
        }
    }

    /** Schwenkt eine Schwinge um die Hochachse: positiv nach vorn, negativ nach hinten. */
    private void sweep(int w, float weight, float degrees) {
        if (weight != 0.0f) {
            this.wingsL[w][0].yRot += degrees * D * weight;
            this.wingsR[w][0].yRot -= degrees * D * weight;
        }
    }

    @Override
    public void setupAnim(SchrolliRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float a = state.attackTime;
        Attack attack = state.attack >= 0 && state.attack < ATTACKS.length ? ATTACKS[state.attack] : Attack.NONE;
        float death = Mth.clamp(state.deathTime / SchrolliEntity.DEATH_TICKS, 0.0f, 1.0f);
        boolean truly = state.trueForm;

        // ---------------------------------------------------------- die Teile der Wahren Gestalt
        this.rings.visible = truly;
        this.corona.visible = truly;
        this.halo.visible = (state.flags & SchrolliEntity.FLAG_HALO) == 0;
        this.orb.visible = (state.flags & SchrolliEntity.FLAG_ORB) == 0;
        if (truly) {
            float k = 1.45f;
            this.crown.xScale = k;
            this.crown.yScale = k;
            this.crown.zScale = k;
            this.ringA.yRot += t * 0.035f;
            this.ringA.xRot += Mth.sin(t * 0.013f) * 0.35f;
            this.ringB.yRot -= t * 0.026f;
            this.ringB.zRot += Mth.sin(t * 0.011f + 1.0f) * 0.3f;
            this.rings.xRot += Mth.sin(t * 0.017f) * 0.12f;
            this.corona.zRot -= t * 0.018f;
            // im Vergehen lösen sich die Räder und sinken fort
            if (death > 0.0f) {
                this.ringA.y += death * 24.0f;
                this.ringB.y += death * 16.0f;
                this.ringA.xRot += death * 1.4f;
                this.ringB.zRot += death * 1.1f;
            }
        }
        this.halo.zRot += t * 0.012f;
        this.haloInner.zRot -= t * 0.03f;
        this.scepterHead.yRot += t * 0.05f;

        // ---------------------------------------------------------- Schweben, Atmen, Wehen
        float g = Mth.clamp(state.ground, 0.0f, 1.0f);
        float calm = 1.0f - death * 0.7f;
        this.body.y += Mth.sin(t * 0.06f) * 1.1f * calm * (1.0f - g);
        this.body.xRot += Mth.sin(t * 0.045f) * 0.03f;
        float f = state.wing;
        float[] lag = {0.0f, 0.55f, 1.1f};
        for (int w = 0; w < 3; w++) {
            float amp = (w == 1 ? 9.0f : 7.0f) * calm * (1.0f - 0.55f * g);
            for (int j = 0; j < 3; j++) {
                float s = Mth.sin(f - lag[w] - j * 0.5f) * amp * (j == 0 ? 1.0f : 0.6f);
                this.wing(w, j, 1.0f, s);
            }
            this.sweep(w, 1.0f, Mth.cos(f - lag[w]) * 3.0f * calm);
        }
        for (int i = 0; i < 4; i++) {
            float sway = Mth.sin(t * 0.07f + i * 1.3f);
            this.robePanels[i].xRot += (i < 2 ? sway * 3.0f : 0.0f) * D;
            this.robePanels[i].zRot += (i >= 2 ? sway * 3.0f : 0.0f) * D;
            this.robeLows[i].xRot += (i < 2 ? Mth.sin(t * 0.07f + i * 1.3f - 0.8f) * 6.0f : 0.0f) * D;
            this.robeLows[i].zRot += (i >= 2 ? Mth.sin(t * 0.07f + i * 1.3f - 0.8f) * 6.0f : 0.0f) * D;
        }
        this.hairBack.xRot += (Mth.sin(t * 0.08f) * 4.0f + 4.0f) * D;
        this.hairTip.xRot += Mth.sin(t * 0.08f - 0.7f) * 7.0f * D;
        this.armR.zRot += (Mth.sin(t * 0.05f) * 2.0f) * D;
        this.armL.zRot -= (Mth.sin(t * 0.05f + 0.8f) * 2.0f) * D;
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * D * 0.8f;
        this.head.xRot += Mth.clamp(state.xRot, -35.0f, 35.0f) * D * 0.6f;

        // ---------------------------------------------------------- Haltungen der Künste (Hüllkurven 0..1)
        float raise = 0.0f;
        float bothUp = 0.0f;
        float point = 0.0f;
        float haloWind = 0.0f;
        float haloThrow = 0.0f;
        float orbWind = 0.0f;
        float orbThrow = 0.0f;
        float smiteUp = 0.0f;
        float slam = 0.0f;
        float spread = 0.0f;
        float wingsUp = 0.0f;
        float beat = 0.0f;
        float kneel = 0.0f;
        float sing = 0.0f;
        float sweepArm = 0.0f;
        float stab = 0.0f;
        float dive = 0.0f;
        float glare = (state.flags & SchrolliEntity.FLAG_GLARE) != 0 ? 1.0f : 0.0f;
        switch (attack) {
            case LANCES -> raise = Anim.bump(a, 8.0f, 50.0f, 66.0f);
            case HALO -> {
                haloWind = Anim.bump(a, 10.0f, 14.0f, 17.0f);
                haloThrow = Anim.bump(a - 15.0f, 3.0f, 8.0f, 22.0f);
            }
            case PILLARS -> {
                raise = Anim.bump(a, 6.0f, 72.0f, 92.0f);
                stab = Anim.bump(a - 30.0f, 3.0f, 5.0f, 12.0f) + Anim.bump(a - 64.0f, 3.0f, 5.0f, 12.0f);
            }
            case STARFALL, APOCALYPSE -> bothUp = Anim.bump(a, 12.0f, attack.duration - 16.0f, attack.duration - 2.0f);
            case SMITE -> {
                smiteUp = Anim.bump(a, 8.0f, 30.0f, 34.0f);
                slam = Anim.bump(a - 32.0f, 2.0f, 8.0f, 26.0f);
            }
            case SUNBEAM, CONSTELLATION -> point = Anim.bump(a, 14.0f, attack.duration - 16.0f, attack.duration - 2.0f);
            case ORB -> {
                orbWind = Anim.bump(a, 10.0f, 16.0f, 19.0f);
                orbThrow = Anim.bump(a - 17.0f, 2.0f, 8.0f, 22.0f);
            }
            case FEATHERS -> {
                wingsUp = Anim.bump(a, 12.0f, 60.0f, 76.0f);
                spread = 0.4f * wingsUp;
                beat = Anim.bump(a - 17.0f, 2.0f, 3.0f, 8.0f) + Anim.bump(a - 31.0f, 2.0f, 3.0f, 8.0f) + Anim.bump(a - 45.0f, 2.0f, 3.0f, 8.0f);
            }
            case JUDGMENT -> {
                point = Anim.bump(a, 8.0f, 64.0f, 70.0f);
                raise = Anim.bump(a - 64.0f, 3.0f, 14.0f, 30.0f);
            }
            case SERAPHS, TWIN_HALOS -> spread = Anim.bump(a, 10.0f, attack.duration - 16.0f, attack.duration - 2.0f);
            case BLINK -> sweepArm = Anim.bump(a - 16.0f, 2.0f, 6.0f, 16.0f);
            case GALE -> {
                wingsUp = Anim.bump(a, 24.0f, 28.0f, 31.0f);
                beat = Anim.bump(a - 29.0f, 2.0f, 6.0f, 22.0f);
            }
            case SUNWHEEL -> spread = Anim.bump(a, 20.0f, 150.0f, 168.0f);
            case CHOIR -> sing = Anim.bump(a, 10.0f, 92.0f, 106.0f);
            case WORD -> {
                bothUp = Anim.bump(a, 24.0f, 54.0f, 58.0f);
                spread = Anim.bump(a - 54.0f, 2.0f, 10.0f, 28.0f);
            }
            case TIMESTOP -> raise = Anim.bump(a, 10.0f, 110.0f, 128.0f);
            case PLANETS -> {
                bothUp = Anim.bump(a, 8.0f, 40.0f, 46.0f);
                orbThrow = Anim.bump(a - 45.0f, 2.0f, 6.0f, 14.0f) + Anim.bump(a - 65.0f, 2.0f, 6.0f, 14.0f) + Anim.bump(a - 85.0f, 2.0f, 6.0f, 14.0f);
            }
            case SUPERNOVA -> {
                bothUp = Anim.bump(a, 16.0f, 138.0f, 146.0f);
                spread = Anim.bump(a - 138.0f, 3.0f, 12.0f, 36.0f);
                wingsUp = bothUp;
            }
            case GENESIS -> {
                point = Anim.bump(a, 20.0f, 106.0f, 112.0f);
                spread = Anim.bump(a - 108.0f, 2.0f, 12.0f, 46.0f);
            }
            case EYE_BEAMS -> sing = 0.0f;
            case COMET -> {
                dive = Anim.bump(a, 6.0f, 66.0f, 70.0f);
                kneel = Anim.bump(a - 68.0f, 2.0f, 10.0f, 34.0f);
                smiteUp = Anim.bump(a - 36.0f, 4.0f, 30.0f, 36.0f);
            }
            case WORLD_SPLIT -> {
                raise = Anim.bump(a, 10.0f, 22.0f, 26.0f) + Anim.bump(a - 44.0f, 6.0f, 56.0f, 60.0f);
                stab = Anim.bump(a - 22.0f, 2.0f, 6.0f, 16.0f) + Anim.bump(a - 56.0f, 2.0f, 6.0f, 16.0f);
            }
            case RECREATION -> {
                bothUp = Anim.bump(a, 16.0f, 150.0f, 168.0f);
                sing = bothUp;
            }
            case STUNNED -> kneel = Anim.bump(a, 8.0f, 66.0f, 80.0f);
            case AWAKEN -> {
                spread = Anim.bump(a, 10.0f, 80.0f, 100.0f);
                wingsUp = spread;
                raise = Anim.bump(a - 88.0f, 6.0f, 46.0f, 62.0f);
            }
            case ASCEND -> {
                bothUp = Anim.bump(a, 20.0f, 64.0f, 72.0f);
                spread = Anim.bump(a - SchrolliEntity.ASCEND_REVEAL, 10.0f, 80.0f, 118.0f);
                wingsUp = Math.max(bothUp, spread);
            }
            default -> {
            }
        }
        raise = Math.min(1.0f, raise);
        stab = Math.min(1.0f, stab);
        beat = Math.min(1.0f, beat);
        orbThrow = Math.min(1.0f, orbThrow);
        spread = Math.min(1.0f, spread);

        // Zepter empor: der Arm gestreckt nach oben, der Stern hoch über ihm
        add(this.armR, raise, -125, 0, 14);
        add(this.foreR, raise, 20, 0, 0);
        add(this.scepter, raise, 105, 0, 0);
        add(this.armR, stab, 40, 0, 0);
        add(this.body, stab, 6, 0, 0);
        // beide Arme zum Himmel
        add(this.armR, bothUp, -140, 0, 20);
        add(this.foreR, bothUp, 20, 0, 0);
        add(this.scepter, bothUp, 120, 0, 0);
        add(this.armL, bothUp, -145, 0, -14);
        add(this.foreL, bothUp, 50, 0, 0);
        add(this.orb, bothUp, 95, 0, 0);
        add(this.head, bothUp, -20, 0, 0);
        add(this.body, bothUp, -6, 0, 0);
        // das Zepter auf das Ziel gerichtet
        add(this.armR, point, -64, 0, 6);
        add(this.foreR, point, 24, 0, 0);
        add(this.scepter, point, -40, 0, 0);
        add(this.armL, point, -30, 0, -10);
        // Heiligenschein: ausholen hinter den Kopf, werfen
        add(this.armR, haloWind, -150, 0, -20);
        add(this.foreR, haloWind, -60, 0, 0);
        add(this.armR, haloThrow, -80, 0, 0);
        add(this.foreR, haloThrow, 20, 0, 0);
        add(this.body, haloThrow, 8, -12, 0);
        // Weltkugel (und Welten): ausholen, werfen
        add(this.armL, orbWind, -140, 0, 10);
        add(this.foreL, orbWind, -40, 0, 0);
        add(this.armL, orbThrow, -62, 0, 0);
        add(this.foreL, orbThrow, 40, 0, 0);
        add(this.body, orbThrow, 8, 12, 0);
        // Zepterschlag: beide Hände über dem Kopf, dann hinab
        add(this.armR, smiteUp, -150, 0, 10);
        add(this.armL, smiteUp, -150, 0, -10);
        add(this.scepter, smiteUp, 100, 0, 0);
        add(this.body, smiteUp, -8, 0, 0);
        add(this.armR, slam, -30, 0, 0);
        add(this.foreR, slam, -20, 0, 0);
        add(this.scepter, slam, -60, 0, 0);
        add(this.armL, slam, -40, 0, -6);
        add(this.body, slam, 22, 0, 0);
        add(this.head, slam, -12, 0, 0);
        // weit ausgebreitete Arme
        add(this.armR, spread, 26, 0, 80);
        add(this.armL, spread, 18, 0, -82);
        add(this.foreR, spread, 24, 0, 0);
        add(this.foreL, spread, 40, 0, 0);
        add(this.head, spread, -10, 0, 0);
        // singen: den Kopf erhoben, die Arme leicht geöffnet
        add(this.head, sing, -22, 0, 0);
        add(this.armR, sing, 0, 0, 22);
        add(this.armL, sing, 0, 0, -22);
        // Lichtsprung: der Hieb fegt waagrecht
        add(this.armR, sweepArm, -70, 70.0f * (1.0f - 2.0f * Mth.clamp((a - 17.0f) / 4.0f, 0.0f, 1.0f)), 0);
        add(this.body, sweepArm, 0, -18.0f * (1.0f - 2.0f * Mth.clamp((a - 17.0f) / 4.0f, 0.0f, 1.0f)), 0);
        // Kometensturz: die Schwingen angelegt
        add(this.body, dive, 0, 0, 0);
        // gleißende Augen: den Blick gesenkt nach vorn
        add(this.head, glare, 10, 0, 0);
        // knien (Taumeln, Einschlag)
        add(this.body, kneel, 28, 0, 0);
        add(this.head, kneel, 18, 0, 0);
        add(this.armR, kneel, 30, 0, 10);
        add(this.armL, kneel, 30, 0, -10);

        // ---------------------------------------------------------- am Boden: Beine, Klinge, Kampfhaltung, Hiebe
        float shield = 0.0f;
        float whirlWings = 0.0f;
        boolean airborne = this.ground(state, attack, a, g, t);
        if (attack == Attack.GUARD) {
            shield = Anim.bump(a, 6.0f, 66.0f, 70.0f);
            beat = Math.max(beat, Anim.bump(a - 67.0f, 2.0f, 4.0f, 9.0f));
        }
        if (attack == Attack.WHIRL) {
            whirlWings = Anim.bump(a - 10.0f, 6.0f, 36.0f, 46.0f);
        }
        if (attack == Attack.PLUNGE) {
            beat = Math.max(beat, Anim.bump(a - 9.0f, 2.0f, 4.0f, 12.0f));
            dive = Math.max(dive, Anim.bump(a - 38.0f, 2.0f, 8.0f, 10.0f));
            wingsUp = Math.max(wingsUp, Anim.bump(a - 18.0f, 6.0f, 20.0f, 24.0f));
        }
        if (attack == Attack.DESCEND) {
            dive = Math.max(dive, Anim.bump(a - 7.0f, 4.0f, 16.0f, 19.0f));
            spread = Math.max(spread, Anim.bump(a - SchrolliEntity.DESCEND_LAND, 3.0f, 8.0f, 22.0f));
            wingsUp = Math.max(wingsUp, Anim.bump(a, 4.0f, 6.0f, 9.0f));
        }
        if (attack == Attack.RISE) {
            wingsUp = Math.max(wingsUp, Anim.bump(a, 6.0f, 10.0f, 13.0f));
            beat = Math.max(beat, Anim.bump(a - SchrolliEntity.RISE_LIFT, 2.0f, 4.0f, 14.0f));
        }
        if (attack == Attack.DASH) {
            dive = Math.max(dive, 0.6f * (Anim.bump(a - 19.0f, 2.0f, 12.0f, 18.0f) + Anim.bump(a - 41.0f, 2.0f, 12.0f, 18.0f)));
        }
        spread = Math.min(1.0f, spread);
        beat = Math.min(1.0f, beat);
        dive = Math.min(1.0f, dive);

        // die Schwingen: gespreizt, geschlagen, angelegt
        for (int w = 0; w < 3; w++) {
            // am Boden halb angelegt und zurückgenommen
            this.wing(w, 0, g, w == 2 ? 12.0f : 6.0f);
            this.sweep(w, g, -14.0f);
            // Schwingenwehr: die Schwingen schwenken nach vorn und biegen sich wie ein Schild um ihn
            this.sweep(w, shield, w == 1 ? 78.0f : 70.0f);
            this.fold(w, 1, shield, 58.0f);
            this.fold(w, 2, shield, 38.0f);
            this.wing(w, 0, shield, w == 0 ? 22.0f : w == 1 ? 4.0f : -18.0f);
            // Klingenwirbel: weit und flach ausgebreitet
            this.wing(w, 0, whirlWings, w == 0 ? 30.0f : w == 2 ? -26.0f : 0.0f);
            this.sweep(w, whirlWings, -10.0f);
            this.wing(w, 0, wingsUp, w == 2 ? -18.0f : -30.0f);
            this.wing(w, 1, wingsUp, -12.0f);
            this.sweep(w, wingsUp, 18.0f);
            this.wing(w, 0, beat, w == 0 ? 46.0f : 38.0f);
            this.wing(w, 1, beat, 18.0f);
            this.sweep(w, beat, -26.0f);
            this.wing(w, 0, spread, -10.0f);
            this.sweep(w, spread, 10.0f);
            this.wing(w, 0, dive, w == 0 ? 50.0f : 30.0f);
            this.sweep(w, dive, 40.0f);
            this.wing(w, 0, kneel, 28.0f);
            this.wing(w, 1, kneel, 14.0f);
            this.wing(w, 0, death, 32.0f);
            this.wing(w, 1, death, 20.0f);
        }

        // ---------------------------------------------------------- Beine und Klinge formen sich aus Licht
        boolean legs = g > 0.01f;
        this.legL.visible = legs;
        this.legR.visible = legs;
        this.blade.visible = legs;
        float form = Mth.clamp(g * 1.25f, 0.0f, 1.0f);
        this.legL.yScale = form;
        this.legR.yScale = form;
        this.blade.xScale = form;
        this.blade.yScale = form;
        this.blade.zScale = form;
        float melt = 1.0f - Mth.clamp(g * 1.6f, 0.0f, 1.0f);
        this.scepter.visible = melt > 0.01f;
        this.scepter.xScale = melt;
        this.scepter.yScale = melt;
        this.scepter.zScale = melt;
        this.orb.visible = this.orb.visible && melt > 0.01f;
        this.orb.xScale = melt;
        this.orb.yScale = melt;
        this.orb.zScale = melt;
        // Weltenklinge: die Klinge wächst zu gewaltiger Länge
        if (attack == Attack.WORLD_BLADE) {
            float grow = Anim.bump(a - 6.0f, 24.0f, 104.0f, 124.0f);
            this.bladeEdge.yScale = 1.0f + 3.0f * grow;
            this.bladeEdge.xScale = 1.0f + 0.9f * grow;
            this.bladeEdge.zScale = 1.0f + 0.9f * grow;
        }
        // die Sohlen auf den Boden (nicht im Sprung, nicht im Sturz, nicht solange die Beine noch wachsen)
        if (g >= 0.999f && !airborne) {
            float dl = plant(this.legL, this.shinL, this.footL);
            float dr = plant(this.legR, this.shinR, this.footR);
            this.body.y += (dl + dr) * 0.5f;
        }
        // mit den Schritten öffnet sich das Gewand, beim Ducken breitet es sich aus
        if (legs) {
            float fwd = Math.max(0.0f, -Math.min(this.legL.xRot, this.legR.xRot));
            float back = Math.max(0.0f, Math.max(this.legL.xRot, this.legR.xRot));
            this.robePanels[0].xRot -= fwd * 0.8f * g;
            this.robePanels[1].xRot += back * 0.8f * g;
            float low = 1.0f - 0.22f * g;
            for (ModelPart lowPart : this.robeLows) {
                lowPart.yScale = low;
            }
        }

        // ---------------------------------------------------------- der Aufstieg und das Vergehen
        if (attack == Attack.ASCEND && a < SchrolliEntity.ASCEND_SHATTER) {
            float shake = a / SchrolliEntity.ASCEND_SHATTER;
            this.body.xRot += Mth.sin(t * 2.3f) * 0.05f * shake;
            this.body.zRot += Mth.sin(t * 2.9f) * 0.05f * shake;
            add(this.head, shake, -28, 0, 0);
        }
        add(this.body, death, -14, 0, 0);
        add(this.head, death, -26, 0, 0);
        add(this.armR, death, 10, 0, 34);
        add(this.armL, death, 10, 0, -34);
    }

    /**
     * Haltung am Boden: Kampfhaltung, Schritte und die Hiebe der Klinge. Gibt zurück, ob er gerade in der Luft ist (Sprung
     * und Sturz des Himmelssturzes, die Landung bis zum Aufsetzen) – dann stehen die Sohlen nicht auf dem Boden.
     */
    private boolean ground(SchrolliRenderState state, Attack attack, float a, float g, float t) {
        if (g <= 0.0f) {
            return false;
        }
        // ---------------------------------------------------------- die Kampfhaltung: die Klinge tief an seiner Rechten, die Spitze
        // schräg hinab und zurück (das Gesicht bleibt frei), die Linke halb erhoben
        this.sword(g, -12.0f, 0.0f, 30.0f, -40.0f, -75.0f);
        this.off(g, -40.0f, -10.0f, 14.0f, -48.0f);
        toward(this.body, g, 6.0f, 10.0f, 0.0f);
        this.crouch(g, 0.0f);
        // ---------------------------------------------------------- Schritte (ein Riese: lange, langsame Schritte)
        float stride = Math.min(1.0f, state.walkAnimationSpeed * 1.6f) * g;
        boolean posed = attack == Attack.DASH || attack == Attack.PLUNGE || attack == Attack.WORLD_BLADE || attack == Attack.GUARD
                || attack == Attack.DESCEND || attack == Attack.RISE;
        if (stride > 0.01f && !posed) {
            float phase = state.walkAnimationPos * 0.42f;
            float swing = Mth.sin(phase) * 24.0f * stride;
            this.legL.xRot -= swing * D;
            this.legR.xRot += swing * D;
            this.shinL.xRot += Math.max(0.0f, Mth.sin(phase + 1.3f)) * 30.0f * stride * D;
            this.shinR.xRot += Math.max(0.0f, Mth.sin(phase + 1.3f + Mth.PI)) * 30.0f * stride * D;
            this.footL.xRot += swing * 0.4f * D;
            this.footR.xRot -= swing * 0.4f * D;
            this.body.zRot += Mth.sin(phase) * 2.0f * stride * D;
            this.armL.xRot += swing * 0.5f * D;
        }
        boolean truly = state.trueForm;
        boolean airborne = false;
        switch (attack) {
            case DESCEND -> {
                // bis zum Aufsetzen wachsen die Beine noch; dann tief in den Knien, die Klinge seitwärts
                float land = Anim.bump(a - SchrolliEntity.DESCEND_LAND, 1.5f, 6.0f, 22.0f);
                airborne = a < SchrolliEntity.DESCEND_LAND;
                this.crouch(land, 0.85f);
                this.sword(land, -22.0f, 0.0f, 48.0f, -20.0f, 100.0f);
                this.off(land, -30.0f, 0.0f, -55.0f, -20.0f);
                toward(this.body, land, 24.0f, 0.0f, 0.0f);
                toward(this.head, land, -16.0f, 0.0f, 0.0f);
            }
            case RISE -> {
                float dip = Anim.bump(a, 6.0f, 10.0f, 14.0f);
                this.crouch(dip, 0.7f);
                toward(this.body, dip, 16.0f, 0.0f, 0.0f);
            }
            case STRIKE -> {
                int[] hits = truly ? new int[] {16, 30, 44, 62} : new int[] {18, 36, 60};
                for (int i = 0; i < hits.length; i++) {
                    float h = hits[i];
                    boolean chop = i == hits.length - 1;
                    if (chop) {
                        float up = Anim.bump(a - (h - 14.0f), 8.0f, 13.0f, 14.0f);
                        float down = Anim.bump(a - (h - 1.0f), 1.5f, 9.0f, 20.0f);
                        // beide Hände über dem Kopf …
                        this.sword(up, -172.0f, 0.0f, 8.0f, -22.0f, 164.0f);
                        this.off(up, -168.0f, 0.0f, -12.0f, -26.0f);
                        toward(this.body, up, -10.0f, 0.0f, 0.0f);
                        toward(this.head, up, -12.0f, 0.0f, 0.0f);
                        this.crouch(up, 0.15f);
                        // … und mit aller Kraft hinab: die Spitze fährt vor ihm in den Boden
                        this.sword(down, -62.0f, 0.0f, -6.0f, -16.0f, 195.0f);
                        this.off(down, -64.0f, 0.0f, 16.0f, -16.0f);
                        toward(this.body, down, 26.0f, 0.0f, 0.0f);
                        toward(this.head, down, -14.0f, 0.0f, 0.0f);
                        this.crouch(down, 0.6f);
                        continue;
                    }
                    float wind = Anim.bump(a - (h - 9.0f), 6.0f, 8.0f, 9.0f);
                    float cut = Anim.bump(a - (h - 1.0f), 1.5f, 4.0f, 12.0f);
                    boolean fromRight = i % 2 == 0;
                    if (fromRight) {
                        // hoch über der rechten Schulter – schräg hinab nach links
                        this.sword(wind, -165.0f, 30.0f, 25.0f, -30.0f, 150.0f);
                        toward(this.body, wind, -4.0f, 28.0f, 0.0f);
                        this.off(wind, -60.0f, 0.0f, 20.0f, -40.0f);
                        this.sword(cut, -55.0f, -45.0f, -15.0f, -10.0f, 185.0f);
                        toward(this.body, cut, 14.0f, -24.0f, 0.0f);
                        this.off(cut, -20.0f, 0.0f, -30.0f, -30.0f);
                    } else {
                        // tief links – schräg hinauf nach rechts
                        this.sword(wind, -55.0f, -45.0f, -15.0f, -10.0f, 185.0f);
                        toward(this.body, wind, 14.0f, -24.0f, 0.0f);
                        this.sword(cut, -150.0f, 50.0f, 35.0f, -20.0f, 160.0f);
                        toward(this.body, cut, -4.0f, 26.0f, 0.0f);
                        this.off(cut, -40.0f, 0.0f, -10.0f, -40.0f);
                    }
                    this.crouch(Math.max(wind, cut), 0.25f);
                }
            }
            case SLASHES -> {
                int[] times = truly ? new int[] {16, 30, 44, 58} : new int[] {18, 34, 50};
                for (int i = 0; i < times.length; i++) {
                    float h = times[i];
                    float wind = Anim.bump(a - (h - 7.0f), 5.0f, 6.0f, 7.0f);
                    float cut = Anim.bump(a - (h - 1.0f), 1.5f, 3.0f, 10.0f);
                    float side = i % 2 == 0 ? 1.0f : -1.0f;
                    // waagrecht: ausholen zur einen Seite, durchziehen zur anderen – tief, dicht über dem Boden
                    this.sword(wind, -52.0f, 72.0f * side, 10.0f * side, -10.0f, 178.0f);
                    toward(this.body, wind, 8.0f, 30.0f * side, 0.0f);
                    this.sword(cut, -52.0f, -78.0f * side, -10.0f * side, -10.0f, 178.0f);
                    toward(this.body, cut, 12.0f, -30.0f * side, 0.0f);
                    this.off(Math.max(wind, cut), -30.0f, 0.0f, -40.0f * side, -30.0f);
                    this.crouch(Math.max(wind, cut), 0.4f);
                }
            }
            case DASH -> {
                for (int k = 0; k < (truly ? 2 : 1); k++) {
                    float go = k == 0 ? 20.0f : 42.0f;
                    float aim = Anim.bump(a - (go - 18.0f), 8.0f, 17.0f, 18.0f);
                    float run = Anim.bump(a - (go - 1.0f), 1.5f, 12.0f, 20.0f);
                    // geduckt, die Klinge zurückgenommen, die Linke weist auf das Ziel …
                    this.sword(aim, 20.0f, 0.0f, 26.0f, -30.0f, -80.0f);
                    this.off(aim, -70.0f, 0.0f, 10.0f, -20.0f);
                    toward(this.body, aim, 18.0f, 22.0f, 0.0f);
                    this.crouch(aim, 0.7f);
                    // … dann der Stoß: die Klinge voran, ein weiter Ausfallschritt
                    this.sword(run, -88.0f, 0.0f, 0.0f, 0.0f, 178.0f);
                    this.off(run, 12.0f, 0.0f, -24.0f, -20.0f);
                    toward(this.body, run, 26.0f, -8.0f, 0.0f);
                    this.legs(run, -48.0f, 30.0f, 34.0f, 8.0f);
                }
            }
            case WHIRL -> {
                float wind = Anim.bump(a, 8.0f, 15.0f, 17.0f);
                float spin = Anim.bump(a - 15.0f, 2.0f, 30.0f, 40.0f);
                this.sword(wind, -80.0f, 80.0f, 0.0f, -5.0f, 175.0f);
                toward(this.body, wind, 2.0f, 34.0f, 0.0f);
                this.off(wind, -70.0f, -20.0f, 30.0f, -30.0f);
                this.crouch(wind, 0.5f);
                // der Wirbel: die Klinge weit vorn rechts, waagrecht
                this.sword(spin, -84.0f, 58.0f, 0.0f, -4.0f, 178.0f);
                this.off(spin, -20.0f, 0.0f, -70.0f, -10.0f);
                toward(this.body, spin, 8.0f, 10.0f, 0.0f);
                this.crouch(spin, 0.35f);
            }
            case PLUNGE -> {
                float dip = Anim.bump(a, 6.0f, 9.0f, 11.0f);
                float hold = Anim.bump(a - 10.0f, 6.0f, 36.0f, 37.0f);
                float kneel = Anim.bump(a - SchrolliEntity.PLUNGE_IMPACT, 1.0f, 28.0f, 40.0f);
                airborne = a > 10.0f && a < SchrolliEntity.PLUNGE_IMPACT;
                this.crouch(dip, 0.8f);
                this.sword(dip, -40.0f, 0.0f, 20.0f, -40.0f, 120.0f);
                // im Sprung und über dem Ziel: beide Hände am Griff, die Spitze zeigt hinab; die Knie angezogen
                this.sword(hold, -150.0f, 0.0f, -6.0f, -40.0f, 10.0f);
                this.off(hold, -150.0f, 0.0f, 10.0f, -40.0f);
                toward(this.body, hold, 6.0f, 0.0f, 0.0f);
                this.legs(hold * (a < 38.0f ? 1.0f : 0.0f), -60.0f, 90.0f, -20.0f, 70.0f);
                // im Sturz die Beine gestreckt, die Klinge voran
                float fall = Anim.bump(a - 38.0f, 2.0f, 8.0f, 9.0f);
                this.sword(fall, -60.0f, 0.0f, -10.0f, -40.0f, 280.0f);
                this.off(fall, -60.0f, 0.0f, 14.0f, -40.0f);
                this.legs(fall, -10.0f, 10.0f, 10.0f, 10.0f);
                // Einschlag: tief gekniet, die Klinge steckt vor ihm im Boden
                this.crouch(kneel, 1.0f);
                this.sword(kneel, -48.0f, 0.0f, -8.0f, -30.0f, 258.0f);
                this.off(kneel, -48.0f, 0.0f, 16.0f, -30.0f);
                toward(this.body, kneel, 30.0f, 0.0f, 0.0f);
                toward(this.head, kneel, -20.0f, 0.0f, 0.0f);
            }
            case GUARD -> {
                float guard = Anim.bump(a, 6.0f, 66.0f, 72.0f);
                // die Klinge senkrecht vor der Brust, beide Hände am Griff, breiter Stand
                this.sword(guard, -55.0f, -10.0f, -15.0f, -50.0f, 105.0f);
                this.off(guard, -52.0f, 0.0f, 26.0f, -50.0f);
                toward(this.body, guard, 4.0f, 0.0f, 0.0f);
                this.legs(guard, -22.0f, 26.0f, 22.0f, 22.0f);
            }
            case COUNTER -> {
                float wind = Anim.bump(a, 3.0f, 6.0f, 8.0f);
                float cut = Anim.bump(a - 7.0f, 1.5f, 6.0f, 16.0f);
                this.sword(wind, -80.0f, -80.0f, -10.0f, -20.0f, 190.0f);
                toward(this.body, wind, 6.0f, -30.0f, 0.0f);
                this.sword(cut, -80.0f, 85.0f, 15.0f, -10.0f, 185.0f);
                toward(this.body, cut, 12.0f, 32.0f, 0.0f);
                this.off(Math.max(wind, cut), -30.0f, 0.0f, -40.0f, -30.0f);
                this.crouch(Math.max(wind, cut), 0.45f);
            }
            case WORLD_BLADE -> {
                float raise = Anim.bump(a - 2.0f, 10.0f, 40.0f, 44.0f);
                float sweep = Anim.bump(a - 42.0f, 2.0f, 20.0f, 24.0f);
                float lift = Anim.bump(a - 62.0f, 10.0f, 22.0f, 24.0f);
                float chop = Anim.bump(a - 85.0f, 1.5f, 18.0f, 40.0f);
                // die Klinge zum Himmel
                this.sword(raise, -176.0f, 0.0f, 10.0f, -6.0f, 182.0f);
                this.off(raise, -172.0f, 0.0f, -12.0f, -10.0f);
                toward(this.body, raise, -8.0f, 0.0f, 0.0f);
                toward(this.head, raise, -22.0f, 0.0f, 0.0f);
                // der Rundschlag dicht über dem Boden
                this.sword(sweep, -62.0f, 60.0f, -5.0f, 0.0f, 167.0f);
                this.off(sweep, -20.0f, 0.0f, -60.0f, -10.0f);
                toward(this.body, sweep, 10.0f, 16.0f, 0.0f);
                this.crouch(sweep, 0.5f);
                // noch einmal empor – und der Spaltschlag
                this.sword(lift, -172.0f, 0.0f, 8.0f, -15.0f, 160.0f);
                this.off(lift, -168.0f, 0.0f, -12.0f, -20.0f);
                toward(this.body, lift, -8.0f, 0.0f, 0.0f);
                this.sword(chop, -60.0f, 0.0f, -6.0f, -10.0f, 175.0f);
                this.off(chop, -62.0f, 0.0f, 16.0f, -12.0f);
                toward(this.body, chop, 28.0f, 0.0f, 0.0f);
                this.crouch(chop, 0.7f);
            }
            default -> {
            }
        }
        return airborne;
    }
}
