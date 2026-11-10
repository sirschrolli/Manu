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

    /** Schwenkt eine Schwinge nach vorn (negativ) oder hinten (positiv). */
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
        float calm = 1.0f - death * 0.7f;
        this.body.y += Mth.sin(t * 0.06f) * 1.1f * calm;
        this.body.xRot += Mth.sin(t * 0.045f) * 0.03f;
        float f = state.wing;
        float[] lag = {0.0f, 0.55f, 1.1f};
        for (int w = 0; w < 3; w++) {
            float amp = (w == 1 ? 9.0f : 7.0f) * calm;
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

        // die Schwingen: gespreizt, geschlagen, angelegt
        for (int w = 0; w < 3; w++) {
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
}
