package io.github.sirschrolli.kingdomsatwar.client.renderer.fae;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * MissMystik, die Feen-Zauberkünstlerin. Sie schwebt und wiegt sich sanft, die Feenflügel schwirren (die unteren etwas
 * nach), Pferdeschwanz, Frackschöße und Feder wehen; in Ruhe stützt sie die linke Hand in die Hüfte und hält den
 * Zauberstab vor sich. Für jeden Trick nimmt sie ihre Haltung ein (weich ein- und ausgeblendet):
 * <ul>
 *     <li>Auftritt: die Arme zum „Tada!“ emporgeworfen, dann eine tiefe Verbeugung;</li>
 *     <li>Kartenfächer: den Fächer neben dem Kopf aufgefächert, dann mit Schwung nach vorn geschleudert;</li>
 *     <li>Lichttauben: mit dem Stab einen Kreis ziehen, dann „Tada!“;</li>
 *     <li>Verschwindetrick: an den Hut tippen – nach dem Wiederauftauchen ein Stich mit dem Stab;</li>
 *     <li>Spiegelkabinett: mit ausgebreiteten Armen drehen, dann verbeugen;</li>
 *     <li>Hut-Trick: den Zylinder greifen und werfen (ohne Hut, bis er zurück ist);</li>
 *     <li>Schwerterkiste: den Stab auf das Ziel gerichtet, die linke Hand dirigiert – dann ein Hieb;</li>
 *     <li>Zieh eine Karte: die Karte präsentieren, dann „Voilà“ mit ausgebreiteten Armen;</li>
 *     <li>Rampenlicht und Finale: beide Arme empor, die Flügel weit, am Ende ein Knall mit zurückgeworfenem Kopf;</li>
 *     <li>Abgang und Tod: eine tiefe Verbeugung.</li>
 * </ul>
 */
public class MissMystikModel extends EntityModel<MissMystikRenderState> {
    private static final float D = Anim.DEG;
    private static final MissMystikEntity.Attack[] ATTACKS = MissMystikEntity.Attack.values();

    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart hat;
    private final ModelPart feather;
    private final ModelPart ponytail;
    private final ModelPart ponytailTip;
    private final ModelPart armR;
    private final ModelPart foreR;
    private final ModelPart wand;
    private final ModelPart armL;
    private final ModelPart foreL;
    private final ModelPart cards;
    private final ModelPart coattails;
    private final ModelPart skirt;
    private final ModelPart legR;
    private final ModelPart shinR;
    private final ModelPart legL;
    private final ModelPart shinL;
    private final ModelPart wingR;
    private final ModelPart wingL;
    private final ModelPart wingLowR;
    private final ModelPart wingLowL;
    /** Haltung der Arme in Grad: Schulter x, y, z, Ellbogen x, y, z (zu den Grundwinkeln hinzu). */
    private final float[] r = new float[6];
    private final float[] l = new float[6];

    public MissMystikModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.hat = this.head.getChild("hat");
        this.feather = this.hat.getChild("feather");
        this.ponytail = this.head.getChild("ponytail");
        this.ponytailTip = this.ponytail.getChild("ponytail_tip");
        this.armR = this.chest.getChild("right_arm");
        this.foreR = this.armR.getChild("right_forearm");
        this.wand = this.foreR.getChild("wand");
        this.armL = this.chest.getChild("left_arm");
        this.foreL = this.armL.getChild("left_forearm");
        this.cards = this.foreL.getChild("cards");
        this.coattails = this.body.getChild("coattails");
        this.skirt = this.body.getChild("skirt");
        this.legR = this.body.getChild("right_leg");
        this.shinR = this.legR.getChild("right_shin");
        this.legL = this.body.getChild("left_leg");
        this.shinL = this.legL.getChild("left_shin");
        this.wingR = this.chest.getChild("right_wing");
        this.wingL = this.chest.getChild("left_wing");
        this.wingLowR = this.chest.getChild("right_wing_lower");
        this.wingLowL = this.chest.getChild("left_wing_lower");
    }

    private static void set(float[] arm, float ax, float ay, float az, float fx, float fy, float fz) {
        arm[0] = ax;
        arm[1] = ay;
        arm[2] = az;
        arm[3] = fx;
        arm[4] = fy;
        arm[5] = fz;
    }

    /** Blendet die Armhaltung mit dem Gewicht {@code w} zur Zielhaltung über. */
    private static void toward(float[] arm, float w, float ax, float ay, float az, float fx, float fy, float fz) {
        if (w <= 0.0f) {
            return;
        }
        arm[0] += (ax - arm[0]) * w;
        arm[1] += (ay - arm[1]) * w;
        arm[2] += (az - arm[2]) * w;
        arm[3] += (fx - arm[3]) * w;
        arm[4] += (fy - arm[4]) * w;
        arm[5] += (fz - arm[5]) * w;
    }

    private static void add(ModelPart part, float w, float x, float y, float z) {
        if (w != 0.0f) {
            part.xRot += x * D * w;
            part.yRot += y * D * w;
            part.zRot += z * D * w;
        }
    }

    @Override
    public void setupAnim(MissMystikRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float a = state.attackTime;
        MissMystikEntity.Attack attack = state.attack >= 0 && state.attack < ATTACKS.length ? ATTACKS[state.attack] : MissMystikEntity.Attack.NONE;
        float death = Mth.clamp(state.deathTime / MissMystikEntity.DEATH_TICKS * 1.6f, 0.0f, 1.0f);

        // ---------------------------------------------------------- Grundhaltung: Stab vor sich, linke Hand in der Hüfte
        set(this.r, -25.0f + Mth.sin(t * 0.05f) * 4.0f, 0.0f, 8.0f, -35.0f, 0.0f, 0.0f);
        set(this.l, 8.0f, 0.0f, -38.0f, -10.0f, 0.0f, 80.0f);
        boolean showCards = false;
        float bow = 0.0f;
        float lean = 0.0f;
        float lookUp = 0.0f;
        float spread = 0.0f;

        // ---------------------------------------------------------- Gesten je Trick
        switch (attack) {
            case APPEAR -> {
                float b = MissMystikEntity.APPEAR_BURST;
                float tada = Anim.bump(a - b, 4.0f, 20.0f, 26.0f);
                toward(this.r, tada, -20.0f, 0.0f, 140.0f, -15.0f, 0.0f, 0.0f);
                toward(this.l, tada, -20.0f, 0.0f, -140.0f, -15.0f, 0.0f, 0.0f);
                lookUp = tada;
                spread = tada;
                bow = Anim.bump(a - b - 22.0f, 6.0f, 12.0f, 18.0f);
            }
            case CARDS -> {
                float up = Anim.bump(a, 6.0f, 12.0f, 15.0f);
                float fling = Anim.bump(a - 13.0f, 2.0f, 6.0f, 18.0f);
                toward(this.l, up, -10.0f, 0.0f, -100.0f, 0.0f, 0.0f, -60.0f);
                toward(this.l, fling, -80.0f, -30.0f, -10.0f, -20.0f, 0.0f, 0.0f);
                if (state.encore) {
                    float up2 = Anim.bump(a - 15.0f, 3.0f, 7.0f, 9.0f);
                    float fling2 = Anim.bump(a - 22.0f, 2.0f, 5.0f, 12.0f);
                    toward(this.l, up2, -10.0f, 0.0f, -100.0f, 0.0f, 0.0f, -60.0f);
                    toward(this.l, fling2, -80.0f, -30.0f, -10.0f, -20.0f, 0.0f, 0.0f);
                    showCards = a < 14.0f || (a > 16.0f && a < 23.0f);
                } else {
                    showCards = a < 14.0f;
                }
                lean = fling * 0.6f;
            }
            case DOVES -> {
                float circle = Anim.bump(a, 3.0f, 10.0f, 13.0f);
                toward(this.r, circle, -90.0f + Mth.sin(a * 0.6f) * 25.0f, 0.0f, 20.0f + Mth.cos(a * 0.6f) * 25.0f, -20.0f, 0.0f, 0.0f);
                float snap = Anim.bump(a - MissMystikEntity.DOVES_SNAP, 2.0f, 10.0f, 20.0f);
                toward(this.r, snap, -20.0f, 0.0f, 140.0f, -15.0f, 0.0f, 0.0f);
                toward(this.l, snap, -20.0f, 0.0f, -140.0f, -15.0f, 0.0f, 0.0f);
                lookUp = snap * 0.6f;
                spread = snap;
            }
            case VANISH -> {
                float tip = Anim.bump(a, 2.0f, 8.0f, 10.0f);
                toward(this.r, tip, -150.0f, 0.0f, 20.0f, -40.0f, 0.0f, 0.0f);
                float strike = Anim.bump(a - MissMystikEntity.VANISH_STRIKE + 3.0f, 2.0f, 6.0f, 14.0f);
                toward(this.r, strike, -90.0f, 0.0f, 5.0f, 0.0f, 0.0f, 0.0f);
                toward(this.l, strike, 20.0f, 0.0f, -45.0f, -10.0f, 0.0f, 0.0f);
                lean = strike;
            }
            case MIRRORS -> {
                float arms = Anim.bump(a, 4.0f, 16.0f, 20.0f);
                toward(this.r, arms, 0.0f, 0.0f, 90.0f, 0.0f, 0.0f, 0.0f);
                toward(this.l, arms, 0.0f, 0.0f, -90.0f, 0.0f, 0.0f, 0.0f);
                spread = arms;
                bow = Anim.bump(a - MissMystikEntity.MIRRORS_SPLIT, 4.0f, 10.0f, 20.0f);
            }
            case HAT -> {
                float reach = Anim.bump(a, 4.0f, 10.0f, 13.0f);
                float fling = Anim.bump(a - 11.0f, 2.0f, 6.0f, 16.0f);
                toward(this.r, reach, -150.0f, 0.0f, 20.0f, -40.0f, 0.0f, 0.0f);
                toward(this.r, fling, -70.0f, 0.0f, 10.0f, -10.0f, 0.0f, 0.0f);
                lean = fling * 0.5f;
            }
            case SWORDS -> {
                float conduct = Anim.bump(a, 4.0f, 30.0f, 34.0f);
                toward(this.r, conduct, -90.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
                toward(this.l, conduct, -20.0f, 0.0f, -120.0f, 0.0f, 0.0f, -40.0f + Mth.sin(a * 0.5f) * 15.0f);
                float slash = Anim.bump(a - MissMystikEntity.SWORDS_STAB + 2.0f, 2.0f, 6.0f, 14.0f);
                toward(this.r, slash, -30.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
                lean = slash * 0.5f;
            }
            case SPOTLIGHT -> {
                float raise = Anim.bump(a, 8.0f, 100.0f, 112.0f);
                toward(this.r, raise, -20.0f, 0.0f, 145.0f, -10.0f, 0.0f, 0.0f);
                toward(this.l, raise, -20.0f, 0.0f, -145.0f, -10.0f, 0.0f, 0.0f);
                lookUp = raise * 0.5f;
                spread = raise;
            }
            case CARD_TRICK -> {
                float present = Anim.bump(a, 6.0f, 28.0f, 32.0f);
                toward(this.l, present, -20.0f, 0.0f, -130.0f, 0.0f, 0.0f, -30.0f);
                toward(this.r, present, -140.0f, 0.0f, 10.0f, -10.0f, 0.0f, 0.0f);
                float voila = Anim.bump(a - MissMystikEntity.TRICK_FLIP, 3.0f, 16.0f, 24.0f);
                toward(this.r, voila, -40.0f, 0.0f, 80.0f, 0.0f, 0.0f, 0.0f);
                toward(this.l, voila, -40.0f, 0.0f, -80.0f, 0.0f, 0.0f, 0.0f);
                lookUp = present * 0.7f;
                spread = voila;
            }
            case FINALE -> {
                float rise = Anim.bump(a, 10.0f, MissMystikEntity.FINALE_BLAST - 2.0f, MissMystikEntity.FINALE_BLAST + 2.0f);
                toward(this.r, rise, -10.0f, 0.0f, 155.0f, -10.0f, 0.0f, 0.0f);
                toward(this.l, rise, -10.0f, 0.0f, -155.0f, -10.0f, 0.0f, 0.0f);
                float blast = Anim.bump(a - MissMystikEntity.FINALE_BLAST, 2.0f, 8.0f, 18.0f);
                toward(this.r, blast, -10.0f, 0.0f, 95.0f, 0.0f, 0.0f, 0.0f);
                toward(this.l, blast, -10.0f, 0.0f, -95.0f, 0.0f, 0.0f, 0.0f);
                lookUp = Math.max(rise * 0.5f, blast);
                spread = Math.max(rise, blast);
            }
            case EXIT -> bow = Anim.bump(a, 6.0f, 22.0f, 30.0f);
            default -> {
            }
        }
        bow = Math.max(bow, death);
        toward(this.r, bow, -80.0f, 0.0f, -30.0f, -50.0f, 0.0f, 0.0f);
        toward(this.l, bow, 10.0f, 0.0f, -70.0f, -10.0f, 0.0f, 0.0f);

        // ---------------------------------------------------------- Arme anwenden
        add(this.armR, 1.0f, this.r[0], this.r[1], this.r[2]);
        add(this.foreR, 1.0f, this.r[3], this.r[4], this.r[5]);
        add(this.armL, 1.0f, this.l[0], this.l[1], this.l[2]);
        add(this.foreL, 1.0f, this.l[3], this.l[4], this.l[5]);
        this.wand.zRot += Mth.sin(t * 0.07f) * 0.12f;
        this.cards.visible = showCards;
        this.hat.visible = state.hat;

        // ---------------------------------------------------------- Leib: schweben, verbeugen, vorlehnen, Kopf
        this.body.y += Mth.sin(t * 0.08f) * 0.6f;
        add(this.body, 1.0f, 32.0f * bow + 12.0f * lean - 8.0f * lookUp + Mth.clamp(state.speed * 30.0f, 0.0f, 10.0f), 0.0f,
                Mth.sin(t * 0.05f) * 2.0f);
        this.head.yRot += Mth.clamp(state.yRot, -50.0f, 50.0f) * D * 0.8f * (1.0f - bow);
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * D * 0.5f * (1.0f - bow);
        add(this.head, 1.0f, 18.0f * bow - 15.0f * lookUp, 0.0f, Mth.sin(t * 0.04f) * 3.0f);

        // ---------------------------------------------------------- Beine baumeln
        float dangle = Mth.sin(t * 0.06f);
        add(this.legL, 1.0f, dangle * 5.0f + 14.0f * bow, 0.0f, 0.0f);
        add(this.legR, 1.0f, -dangle * 5.0f - 12.0f * bow, 0.0f, 0.0f);
        add(this.shinL, 1.0f, Mth.sin(t * 0.06f + 1.0f) * 4.0f, 0.0f, 0.0f);
        add(this.shinR, 1.0f, -Mth.sin(t * 0.06f + 1.0f) * 4.0f, 0.0f, 0.0f);

        // ---------------------------------------------------------- Flügel schwirren (die unteren etwas nach)
        float f = state.flap * 2.5f;
        float calm = 1.0f - death * 0.8f;
        float beat = Mth.sin(f) * 18.0f * calm;
        float beatLow = Mth.sin(f - 0.7f) * 13.0f * calm;
        float open = 16.0f * spread;
        add(this.wingL, 1.0f, 0.0f, beat + open, Mth.cos(f) * 5.0f * calm + 10.0f * spread);
        add(this.wingR, 1.0f, 0.0f, -beat - open, -Mth.cos(f) * 5.0f * calm - 10.0f * spread);
        add(this.wingLowL, 1.0f, 0.0f, beatLow + open * 0.7f, Mth.cos(f - 0.7f) * 4.0f * calm);
        add(this.wingLowR, 1.0f, 0.0f, -beatLow - open * 0.7f, -Mth.cos(f - 0.7f) * 4.0f * calm);

        // ---------------------------------------------------------- Haar, Feder, Schöße, Rock wehen
        float wind = Mth.clamp(state.speed * 3.0f, 0.0f, 1.0f);
        this.ponytail.xRot += (Mth.sin(t * 0.09f) * 4.0f + 14.0f * wind + 10.0f * bow) * D;
        this.ponytail.zRot += Mth.sin(t * 0.06f) * 6.0f * D;
        this.ponytailTip.xRot += (Mth.sin(t * 0.09f - 0.8f) * 7.0f + 8.0f * wind) * D;
        this.ponytailTip.zRot += Mth.sin(t * 0.06f - 0.6f) * 8.0f * D;
        this.feather.zRot += Mth.sin(t * 0.2f) * 5.0f * D;
        this.coattails.xRot += (Mth.sin(t * 0.1f) * 3.0f + 20.0f * wind + 6.0f * spread) * D;
        this.skirt.zRot += Mth.sin(t * 0.08f) * 1.5f * D;
    }
}
