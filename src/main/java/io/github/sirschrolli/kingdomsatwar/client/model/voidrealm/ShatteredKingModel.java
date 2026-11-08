package io.github.sirschrolli.kingdomsatwar.client.model.voidrealm;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.client.renderer.voidrealm.ShatteredKingRenderState;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.joml.Quaternionf;

/**
 * Der Zerbrochene König: schwebt und wogt, das zersprungene Herz schlägt in der offenen Brust, die Krone dreht sich
 * langsam über seinem Haupt, das Gewand weht, Trümmer kreisen um ihn. Seine Hände schweben frei – sie führen alle
 * Angriffe aus:
 * <ul>
 *     <li><b>Erwachen:</b> Leib, Hände, Krone und Trümmer erheben sich vom Boden und fügen sich zusammen, dann ein Schrei;</li>
 *     <li><b>Faustschlag:</b> eine Hand steigt hoch über die Stelle, krümmt die Finger zur Klaue und schlägt flach herab
 *     – genau auf den Punkt, den der Warnkreis zeigt;</li>
 *     <li><b>Wischhieb:</b> beide Hände holen weit hinten aus und fegen gegeneinander in einem Bogen vor ihm über den Boden;</li>
 *     <li><b>Kronensplitter:</b> der Kopf hebt sich, die Krone steigt, dreht sich schneller und spreizt die Zacken, bei
 *     jedem Schuss zucken sie;</li>
 *     <li><b>Schwerkraftbruch:</b> die Hände heben sich mit offenen Handflächen weit empor, klatschen über dem Kopf
 *     zusammen und stoßen dann gemeinsam herab;</li>
 *     <li><b>Ruf der Wächter:</b> die Hände senken sich zum Boden und reißen die Wächter empor;</li>
 *     <li><b>Sternenstrahl:</b> die Hände packen die Ränder der Brust und reißen sie auf, die Herzhälften klaffen auseinander;</li>
 *     <li><b>Tod:</b> er sinkt, die Hände fallen, die Kronensplitter und Trümmer treiben davon.</li>
 * </ul>
 */
public class ShatteredKingModel extends EntityModel<ShatteredKingRenderState> {
    private static final float[] CROWN_ANGLE = new float[7];

    static {
        for (int k = 0; k < 7; k++) {
            CROWN_ANGLE[k] = (-90.0f + k * 360.0f / 7.0f) * DEG;
        }
    }

    private final ModelPart body;
    private final ModelPart heart;
    private final ModelPart heartL;
    private final ModelPart heartR;
    private final ModelPart head;
    private final ModelPart beard;
    private final ModelPart beardTip;
    private final ModelPart crown;
    private final ModelPart[] crownShards = new ModelPart[7];
    private final ModelPart leftShoulder;
    private final ModelPart rightShoulder;
    private final ModelPart robeFront;
    private final ModelPart robeBack;
    private final ModelPart robeLeft;
    private final ModelPart robeRight;
    private final ModelPart robeFrontLow;
    private final ModelPart robeBackLow;
    private final ModelPart robeLeftLow;
    private final ModelPart robeRightLow;
    private final ModelPart orbit;
    private final ModelPart[] fragments = new ModelPart[4];
    private final Hand left;
    private final Hand right;
    private final Hand[] hands;

    public ShatteredKingModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.heart = this.body.getChild("heart");
        this.heartL = this.heart.getChild("heart_l");
        this.heartR = this.heart.getChild("heart_r");
        this.head = this.body.getChild("head");
        this.beard = this.head.getChild("beard");
        this.beardTip = this.beard.getChild("beard_tip");
        this.crown = this.head.getChild("crown");
        for (int k = 0; k < 7; k++) {
            this.crownShards[k] = this.crown.getChild("crown_" + k);
        }
        this.leftShoulder = this.body.getChild("left_shoulder");
        this.rightShoulder = this.body.getChild("right_shoulder");
        ModelPart robe = this.body.getChild("robe");
        this.robeFront = robe.getChild("robe_front");
        this.robeBack = robe.getChild("robe_back");
        this.robeLeft = robe.getChild("robe_left");
        this.robeRight = robe.getChild("robe_right");
        this.robeFrontLow = this.robeFront.getChild("robe_front_low");
        this.robeBackLow = this.robeBack.getChild("robe_back_low");
        this.robeLeftLow = this.robeLeft.getChild("robe_left_low");
        this.robeRightLow = this.robeRight.getChild("robe_right_low");
        this.orbit = root.getChild("orbit");
        for (int k = 0; k < 4; k++) {
            this.fragments[k] = this.orbit.getChild("fragment_" + k);
        }
        this.left = new Hand(root.getChild("left_hand"), "left", 1.0f);
        this.right = new Hand(root.getChild("right_hand"), "right", -1.0f);
        this.hands = new Hand[] {this.left, this.right};
    }

    @Override
    public void setupAnim(ShatteredKingRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float speed = Math.min(state.walkAnimationSpeed * 2.0f, 1.0f);
        this.idle(state, t, speed);

        float awaken = Anim.seconds(state.awaken, t);
        if (awaken >= 0.0f && awaken < 3.7f) {
            this.awaken(awaken, t);
        }
        float slamL = Anim.seconds(state.slamLeft, t);
        float slamR = Anim.seconds(state.slamRight, t);
        if (slamL >= 0.0f && slamL < 1.8f && (slamR < 0.0f || slamL <= slamR)) {
            this.slam(state, this.left, this.right, slamL, t);
        } else if (slamR >= 0.0f && slamR < 1.8f) {
            this.slam(state, this.right, this.left, slamR, t);
        }
        float sweep = Anim.seconds(state.sweep, t);
        if (sweep >= 0.0f && sweep < 1.6f) {
            this.sweep(sweep);
        }
        float volley = Anim.seconds(state.volley, t);
        if (volley >= 0.0f && volley < 1.9f) {
            this.volley(volley, t);
        }
        float gravity = Anim.seconds(state.gravity, t);
        if (gravity >= 0.0f && gravity < 3.6f) {
            this.gravity(gravity, t);
        }
        float summon = Anim.seconds(state.summon, t);
        if (summon >= 0.0f && summon < 2.5f) {
            this.summon(summon);
        }
        float beam = Anim.seconds(state.beam, t);
        if (beam >= 0.0f && beam < ShatteredKingEntity.BEAM_TICKS / 20.0f + 0.45f) {
            this.beam(beam, t);
        }
        if (state.deathTime > 0.0f) {
            this.death(Mth.clamp(state.deathTime / ShatteredKingEntity.DEATH_TICKS, 0.0f, 1.0f), t);
        }
        this.left.apply();
        this.right.apply();
    }

    // ------------------------------------------------------------------ Ruhe

    private void idle(ShatteredKingRenderState state, float t, float speed) {
        // Schweben und Wogen, im Flug in Fahrtrichtung geneigt
        this.body.y += Mth.sin(t * 0.05f) * 0.8f;
        this.body.xRot += speed * 0.14f + Mth.sin(t * 0.031f) * 0.025f;
        this.body.zRot += Mth.sin(t * 0.023f) * 0.02f;
        this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * DEG;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * DEG * 0.8f;
        this.beard.xRot += Mth.sin(t * 0.05f - 0.6f) * 0.03f + speed * 0.12f;
        this.beardTip.xRot += Mth.sin(t * 0.05f - 1.2f) * 0.05f + speed * 0.1f;

        // das zersprungene Herz: ein doppelter Schlag, die Hälften öffnen sich dabei ein wenig
        float phase = (t % 40.0f) / 40.0f;
        float beat = Anim.bump(phase, 0.04f, 0.06f, 0.16f) + 0.6f * Anim.bump(phase - 0.2f, 0.04f, 0.06f, 0.16f);
        float heartScale = 1.0f + beat * 0.12f;
        this.heart.xScale = this.heart.yScale = this.heart.zScale = heartScale;
        this.heart.yRot += t * 0.04f;
        float gap = 0.2f + beat * 0.35f;
        this.heartL.x -= gap;
        this.heartR.x += gap;

        // die Krone dreht sich langsam, jede Zacke wiegt sich
        this.crown.yRot += t * 0.012f;
        this.crown.y += Mth.sin(t * 0.07f) * 0.5f;
        for (int k = 0; k < 7; k++) {
            this.crownShards[k].y += Mth.sin(t * 0.09f + k * 0.9f) * 0.45f;
        }

        // das Gewand weht, im Flug nach hinten
        this.robeFront.xRot += Mth.sin(t * 0.07f) * 0.04f + speed * 0.35f;
        this.robeFrontLow.xRot += Mth.sin(t * 0.07f - 0.8f) * 0.07f + speed * 0.25f;
        this.robeBack.xRot += Mth.sin(t * 0.07f + 1.1f) * 0.04f + speed * 0.22f;
        this.robeBackLow.xRot += Mth.sin(t * 0.07f + 0.3f) * 0.08f + speed * 0.2f;
        this.robeLeft.zRot += Mth.sin(t * 0.06f + 0.5f) * 0.04f;
        this.robeLeftLow.zRot += Mth.sin(t * 0.06f - 0.3f) * 0.07f;
        this.robeRight.zRot += Mth.sin(t * 0.06f + 2.0f) * 0.04f;
        this.robeRightLow.zRot += Mth.sin(t * 0.06f + 1.2f) * 0.07f;
        this.robeLeft.xRot += speed * 0.25f;
        this.robeRight.xRot += speed * 0.25f;

        // Trümmer kreisen
        this.orbit.yRot += t * 0.025f;
        for (int k = 0; k < 4; k++) {
            ModelPart f = this.fragments[k];
            f.xRot += t * 0.02f * (1.0f + k * 0.3f);
            f.yRot += t * 0.015f * (k + 1);
            f.y += Mth.sin(t * 0.05f + k * 1.7f) * 1.2f;
        }

        // die Hände schweben neben ihm, jede in ihrem eigenen Takt; im Flug bleiben sie etwas zurück
        for (Hand h : this.hands) {
            float s = h.side;
            h.x = 19.0f * s + Mth.sin(t * 0.031f + s) * 0.8f;
            h.y = 3.0f + Mth.sin(t * 0.06f + s * 1.3f) * 1.1f;
            h.z = -5.0f + Mth.cos(t * 0.027f + s) * 0.8f + speed * 4.0f;
            h.yaw = -12.0f * DEG * s + Mth.sin(t * 0.02f + s) * 0.05f;
            h.pitch = 10.0f * DEG + Mth.sin(t * 0.05f + s) * 0.06f + speed * 0.2f;
            h.roll = 8.0f * DEG * s;
            h.curl = 0.15f + 0.1f * Mth.sin(t * 0.045f + s);
            h.spread = 0.05f;
            h.saveIdle();
        }
    }

    // ------------------------------------------------------------------ Erwachen

    private void awaken(float s, float t) {
        // der Boden im Modellraum (der König steigt beim Erwachen langsam empor)
        float ground = 31.3f + s * 5.1f;
        float body = smooth(s, 0.5f, 2.9f);
        float hands = smooth(s, 0.2f, 2.3f);
        float rubble = smooth(s, 0.0f, 2.2f);
        float crownForm = smooth(s, 1.4f, 2.8f);
        float apart = 1.0f - body;
        this.body.y += apart * 19.0f;
        this.body.xRot += apart * 1.05f;
        this.body.zRot += apart * 0.18f;
        this.head.xRot += apart * 0.8f;
        this.head.yRot -= apart * 0.3f;
        this.robeFront.xRot -= apart * 0.5f;
        this.robeBack.xRot += apart * 0.6f;
        float heart = 0.25f + 0.75f * smooth(s, 1.0f, 2.6f);
        this.heart.xScale *= heart;
        this.heart.yScale *= heart;
        this.heart.zScale *= heart;
        // die Krone fügt sich zuletzt aus ihren Splittern
        float spread = 1.0f - crownForm;
        float c = Math.max(0.05f, crownForm);
        this.crown.xScale = this.crown.yScale = this.crown.zScale = c;
        this.crown.visible = crownForm > 0.02f;
        this.crown.yRot += spread * 4.0f;
        for (int k = 0; k < 7; k++) {
            ModelPart shard = this.crownShards[k];
            shard.x += Mth.cos(CROWN_ANGLE[k]) * spread * 9.0f;
            shard.z += Mth.sin(CROWN_ANGLE[k]) * spread * 9.0f;
            shard.xRot += spread * (1.5f + k * 0.4f);
        }
        // die Trümmer liegen am Boden und steigen auf
        float down = 1.0f - rubble;
        this.orbit.y += down * (ground + 4.0f) * 0.92f;
        this.orbit.yRot -= down * 1.2f;
        for (int k = 0; k < 4; k++) {
            this.fragments[k].x *= 1.0f + down * 0.35f;
            this.fragments[k].z *= 1.0f + down * 0.35f;
        }
        // die Hände liegen flach am Boden und erheben sich
        for (Hand h : this.hands) {
            float lying = 1.0f - hands;
            h.to(lying, 22.0f * h.side, ground - 2.2f, -9.0f, -0.35f * h.side, 0.0f, 0.0f);
            h.curl = Mth.lerp(lying, h.curl, -0.1f);
            h.spread = Mth.lerp(lying, h.spread, 0.3f);
        }
        // der Schrei, wenn er ganz ist: Kopf zurück, Hände weit auseinander, die Krone spreizt die Zacken
        float roar = Anim.bump(s - 2.85f, 0.18f, 0.4f, 0.8f);
        if (roar > 0.0f) {
            this.head.xRot -= roar * 0.5f;
            this.body.xRot -= roar * 0.2f;
            this.beard.xRot -= roar * 0.15f;
            this.flareCrown(roar * 0.45f);
            for (Hand h : this.hands) {
                h.to(roar, 27.0f * h.side, -9.0f, -7.0f, -0.55f * h.side, -0.6f, 0.35f * h.side);
                h.spread = Mth.lerp(roar, h.spread, 0.35f);
                h.curl = Mth.lerp(roar, h.curl, 0.0f);
                h.x += Mth.sin(t * 2.3f + h.side) * 0.3f * roar;
            }
        }
    }

    // ------------------------------------------------------------------ Faustschlag

    private void slam(ShatteredKingRenderState state, Hand h, Hand other, float s, float t) {
        float lx = state.slamX;
        float ly = state.slamY;
        float lz = state.slamZ;
        float toward = (float) Mth.atan2(-lx, -lz);
        float up = smooth(s, 0.0f, 0.7f);
        float down = easeIn(s, 0.92f, 1.1f);
        float back = smooth(s, 1.35f, 1.75f);
        // hoch über der Stelle, die Finger zur Klaue gekrümmt
        h.to(up, 13.0f * h.side + lx * 0.25f, -15.0f, -8.0f + lz * 0.35f, toward * 0.5f, -0.75f, 0.25f * h.side);
        h.curl = Mth.lerp(up, h.curl, 0.85f);
        h.spread = Mth.lerp(up, h.spread, 0.18f);
        float hold = Anim.bump(s - 0.55f, 0.1f, 0.3f, 0.42f);
        h.x += Mth.sin(t * 2.7f) * 0.3f * hold;
        h.y += Mth.cos(t * 3.1f) * 0.3f * hold;
        // flach herab auf den Punkt – die Finger spreizen sich beim Aufschlag
        h.to(down, lx + 1.5f * h.side, ly - 3.2f, lz + 2.5f, toward, 0.0f, 0.0f);
        h.curl = Mth.lerp(down, h.curl, -0.15f);
        h.spread = Mth.lerp(down, h.spread, 0.28f);
        h.toIdle(back);
        // die andere Hand weicht ein Stück zurück, der Leib folgt dem Schlag
        other.to(up * (1.0f - back) * 0.5f, other.x, other.y - 3.0f, other.z + 3.0f, other.yaw, other.pitch - 0.2f, other.roll);
        float lean = up * (1.0f - back);
        float strike = down * (1.0f - back);
        this.body.zRot -= h.side * 0.07f * lean;
        this.body.yRot += h.side * 0.12f * lean;
        this.body.xRot += 0.18f * strike;
        this.head.xRot += 0.3f * strike - 0.1f * up * (1.0f - down);
        (h.side > 0 ? this.leftShoulder : this.rightShoulder).zRot -= h.side * 0.15f * lean;
        // der Aufschlag lässt ihn erzittern
        this.body.y += Anim.bump(s - 1.1f, 0.03f, 0.05f, 0.25f) * 1.2f;
    }

    // ------------------------------------------------------------------ Wischhieb

    private void sweep(float s) {
        float wind = smooth(s, 0.0f, 0.45f);
        float swing = smooth(s, 0.5f, 0.92f);
        float back = smooth(s, 1.05f, 1.5f);
        for (Hand h : this.hands) {
            float from = -2.2f * h.side;
            float to = 0.75f * h.side;
            float offset = h.side > 0 ? -3.0f : 3.0f;
            // weit hinten ausholen
            h.to(wind, -27.0f * Mth.sin(from), 12.0f + offset, -27.0f * Mth.cos(from), from, 0.1f, h.side * Mth.HALF_PI);
            if (swing > 0.0f) {
                float a = Mth.lerp(swing, from, to);
                float r = 27.0f + 11.0f * swing + 4.0f * Mth.sin(swing * Mth.PI);
                float y = 12.0f + 18.0f * Mth.sin(swing * Mth.PI * 0.85f) + offset;
                h.to(1.0f, -r * Mth.sin(a), y, -r * Mth.cos(a), a, 0.1f, h.side * Mth.HALF_PI);
            }
            h.spread = Mth.lerp(wind, h.spread, 0.22f);
            h.curl = Mth.lerp(wind, h.curl, 0.1f);
            h.toIdle(back);
        }
        float on = wind * (1.0f - back);
        this.body.xRot += 0.2f * swing * (1.0f - back) - 0.08f * wind * (1.0f - swing);
        this.head.xRot += 0.15f * on;
        this.leftShoulder.zRot -= 0.15f * on;
        this.rightShoulder.zRot += 0.15f * on;
    }

    // ------------------------------------------------------------------ Kronensplitter

    private void volley(float s, float t) {
        float raise = smooth(s, 0.0f, 0.5f) * (1.0f - smooth(s, 1.45f, 1.85f));
        float recoil = Anim.bump(s - 0.9f, 0.03f, 0.06f, 0.25f) + Anim.bump(s - 1.3f, 0.03f, 0.06f, 0.25f);
        this.head.xRot -= 0.32f * raise;
        this.body.xRot -= 0.08f * raise + 0.04f * recoil;
        this.crown.y -= 3.5f * raise;
        this.crown.yRot += raise * s * 3.0f;
        this.flareCrown(raise * 0.4f + recoil * 0.35f);
        for (Hand h : this.hands) {
            h.to(raise, 22.0f * h.side, -6.0f, -14.0f, -0.5f * h.side, -0.5f, 0.3f * h.side);
            h.spread = Mth.lerp(raise, h.spread, 0.35f);
            h.curl = Mth.lerp(raise, h.curl, 0.0f);
            h.y -= recoil * 1.5f;
            h.x += Mth.sin(t * 2.1f + h.side) * 0.15f * raise;
        }
    }

    // ------------------------------------------------------------------ Schwerkraftbruch

    private void gravity(float s, float t) {
        float lift = smooth(s, 0.0f, 0.6f);
        float clap = easeIn(s, 2.3f, 2.58f);
        float push = smooth(s, 2.6f, 2.85f);
        float back = smooth(s, 2.95f, 3.5f);
        float hold = lift * (1.0f - clap);
        for (Hand h : this.hands) {
            // weit empor, die Handflächen nach oben
            h.to(lift, 25.0f * h.side, -22.0f, -3.0f, -0.35f * h.side, -0.2f, 2.9f * h.side);
            h.curl = Mth.lerp(lift, h.curl, 0.35f + 0.3f * smooth(s, 0.6f, 2.2f));
            h.spread = Mth.lerp(lift, h.spread, 0.25f);
            h.x += Mth.sin(t * 1.9f + h.side) * 0.35f * hold;
            h.y += Mth.cos(t * 2.3f + h.side) * 0.3f * hold;
            // über dem Kopf zusammen (Handflächen gegeneinander) …
            h.to(clap, 3.4f * h.side, -31.0f, -6.0f, 0.0f, -1.35f, Mth.HALF_PI * h.side);
            h.curl = Mth.lerp(clap, h.curl, 0.05f);
            h.spread = Mth.lerp(clap, h.spread, 0.0f);
            // … und gemeinsam herab
            h.to(push, 4.5f * h.side, 20.0f, -24.0f, 0.0f, 0.5f, 0.0f);
            h.toIdle(back);
        }
        this.body.xRot += -0.2f * hold + 0.3f * push * (1.0f - back);
        this.head.xRot += -0.55f * lift * (1.0f - push) + 0.3f * push * (1.0f - back);
        this.orbit.y -= 10.0f * lift * (1.0f - push);
        this.orbit.yRot += lift * (1.0f - push) * s * 2.0f;
        this.flareCrown(0.2f * hold);
        this.body.y += Anim.bump(s - 2.6f, 0.04f, 0.06f, 0.3f) * 1.5f;
    }

    // ------------------------------------------------------------------ Ruf der Wächter

    private void summon(float s) {
        float lower = smooth(s, 0.0f, 0.55f);
        float rise = smooth(s, 0.6f, 1.0f);
        float back = smooth(s, 1.6f, 2.4f);
        for (Hand h : this.hands) {
            h.to(lower, 26.0f * h.side, 27.0f, -9.0f, -0.4f * h.side, 0.15f, 0.0f);
            h.spread = Mth.lerp(lower, h.spread, 0.4f);
            h.curl = Mth.lerp(lower, h.curl, 0.0f);
            h.to(rise, 24.0f * h.side, -7.0f, -10.0f, -0.4f * h.side, -0.25f, 2.9f * h.side);
            h.curl = Mth.lerp(rise, h.curl, 0.5f);
            h.toIdle(back);
        }
        this.body.xRot += 0.15f * lower * (1.0f - rise);
        this.head.xRot += 0.4f * lower * (1.0f - rise) - 0.3f * rise * (1.0f - back);
        this.flareCrown(0.3f * rise * (1.0f - back));
    }

    // ------------------------------------------------------------------ Sternenstrahl

    private void beam(float s, float t) {
        float end = ShatteredKingEntity.BEAM_TICKS / 20.0f;
        float charge = smooth(s, 0.0f, 1.4f);
        float release = smooth(s, end, end + 0.4f);
        float on = charge * (1.0f - release);
        float firing = smooth(s, 1.4f, 1.7f) * (1.0f - release);
        // die Brust wird aufgerissen, die Herzhälften klaffen und zittern
        this.heartL.x -= 2.6f * on;
        this.heartR.x += 2.6f * on;
        this.heartL.zRot -= 0.25f * on;
        this.heartR.zRot += 0.25f * on;
        float swell = 1.0f + 0.35f * on;
        this.heart.xScale *= swell;
        this.heart.yScale *= swell;
        this.heart.zScale *= swell;
        this.heart.zRot += Mth.sin(t * 1.7f) * 0.06f * on;
        this.body.xRot += -0.14f * charge * (1.0f - firing) + 0.12f * firing;
        this.head.xRot -= 0.35f * on;
        this.beard.xRot -= 0.1f * on;
        this.flareCrown(0.25f * on);
        for (Hand h : this.hands) {
            h.to(on, 11.0f * h.side, -5.0f, -12.0f, 1.0f * h.side, 0.2f, 1.3f * h.side);
            h.curl = Mth.lerp(on, h.curl, 0.7f);
            h.x += Mth.sin(t * 2.3f + h.side) * 0.2f * on;
        }
    }

    // ------------------------------------------------------------------ Tod

    private void death(float d, float t) {
        float sink = smooth(d, 0.0f, 0.6f);
        this.body.xRot += 0.4f * sink;
        this.body.y += 6.0f * d * d;
        this.body.zRot += Mth.sin(t * 0.9f) * 0.08f * d;
        this.head.xRot += 0.55f * smooth(d, 0.1f, 0.7f);
        this.heartL.x -= 3.0f * d;
        this.heartR.x += 3.0f * d;
        this.heart.yRot += d * t * 0.2f;
        for (int k = 0; k < 7; k++) {
            ModelPart shard = this.crownShards[k];
            shard.x += Mth.cos(CROWN_ANGLE[k]) * 10.0f * d;
            shard.z += Mth.sin(CROWN_ANGLE[k]) * 10.0f * d;
            shard.y += -6.0f * d + 14.0f * d * d;
            shard.xRot += d * 3.0f * (k + 1) * 0.3f;
        }
        for (int k = 0; k < 4; k++) {
            this.fragments[k].x *= 1.0f + 1.6f * d;
            this.fragments[k].z *= 1.0f + 1.6f * d;
            this.fragments[k].y += 8.0f * d;
        }
        for (Hand h : this.hands) {
            float fall = d * d;
            h.to(sink, 21.0f * h.side, 3.0f + 30.0f * fall, -6.0f, h.yaw, h.pitch + 0.8f * d, h.roll + 1.2f * d * h.side);
            h.curl = Mth.lerp(sink, h.curl, 1.0f);
        }
        this.robeFront.xRot -= 0.2f * d;
        this.robeBack.xRot += 0.2f * d;
    }

    // ------------------------------------------------------------------ Hilfen

    /** Spreizt die Kronenzacken nach außen. */
    private void flareCrown(float amount) {
        for (int k = 0; k < 7; k++) {
            float a = CROWN_ANGLE[k];
            ModelPart shard = this.crownShards[k];
            shard.xRot += -Mth.sin(a) * amount;
            shard.zRot += Mth.cos(a) * amount;
            shard.x += Mth.cos(a) * 2.0f * amount;
            shard.z += Mth.sin(a) * 2.0f * amount;
        }
    }

    /** 0 bis {@code from}, 1 ab {@code to}, dazwischen weich. */
    private static float smooth(float s, float from, float to) {
        float x = Mth.clamp((s - from) / (to - from), 0.0f, 1.0f);
        return x * x * (3.0f - 2.0f * x);
    }

    /** Wie {@link #smooth}, aber immer schneller (für Schläge, die mit voller Wucht ankommen). */
    private static float easeIn(float s, float from, float to) {
        float x = Mth.clamp((s - from) / (to - from), 0.0f, 1.0f);
        return x * x;
    }

    /**
     * Eine schwebende Hand: Lage (Pixel) und Ausrichtung in der Reihenfolge Gieren (Y), dann Neigen (X) um die eigene
     * Querachse, dann Rollen (Z) um die eigene Längsachse – so kann die Hand in jede Richtung zeigen und dabei die
     * Handfläche drehen. Dazu, wie weit die Finger gekrümmt (0 gestreckt, 1 Faust) und gespreizt sind.
     */
    private static final class Hand {
        final ModelPart part;
        final ModelPart[] fingers = new ModelPart[4];
        final ModelPart[] tips = new ModelPart[4];
        final ModelPart thumb;
        final float side;
        float x;
        float y;
        float z;
        float yaw;
        float pitch;
        float roll;
        float curl;
        float spread;
        private final float[] idle = new float[8];
        private final Quaternionf rotation = new Quaternionf();

        Hand(ModelPart part, String name, float side) {
            this.part = part;
            this.side = side;
            for (int k = 0; k < 4; k++) {
                this.fingers[k] = part.getChild(name + "_finger_" + k);
                this.tips[k] = this.fingers[k].getChild(name + "_finger_" + k + "_tip");
            }
            this.thumb = part.getChild(name + "_thumb");
        }

        void saveIdle() {
            this.idle[0] = this.x;
            this.idle[1] = this.y;
            this.idle[2] = this.z;
            this.idle[3] = this.yaw;
            this.idle[4] = this.pitch;
            this.idle[5] = this.roll;
            this.idle[6] = this.curl;
            this.idle[7] = this.spread;
        }

        /** Bewegt die Hand mit Gewicht {@code w} (0..1) zur angegebenen Haltung. */
        void to(float w, float nx, float ny, float nz, float nyaw, float npitch, float nroll) {
            if (w <= 0.0f) {
                return;
            }
            this.x = Mth.lerp(w, this.x, nx);
            this.y = Mth.lerp(w, this.y, ny);
            this.z = Mth.lerp(w, this.z, nz);
            this.yaw = Mth.lerp(w, this.yaw, nyaw);
            this.pitch = Mth.lerp(w, this.pitch, npitch);
            this.roll = Mth.lerp(w, this.roll, nroll);
        }

        /** Zurück zur Ruhehaltung (mit Gewicht {@code w}). */
        void toIdle(float w) {
            if (w <= 0.0f) {
                return;
            }
            this.to(w, this.idle[0], this.idle[1], this.idle[2], this.idle[3], this.idle[4], this.idle[5]);
            this.curl = Mth.lerp(w, this.curl, this.idle[6]);
            this.spread = Mth.lerp(w, this.spread, this.idle[7]);
        }

        void apply() {
            this.part.x = this.x;
            this.part.y = this.y;
            this.part.z = this.z;
            this.part.setRotation(0.0f, 0.0f, 0.0f);
            this.part.rotateBy(this.rotation.rotationY(this.yaw).rotateX(this.pitch).rotateZ(this.roll));
            float c = this.curl;
            for (int k = 0; k < 4; k++) {
                // die Ruhekrümmung steckt schon in der Ausgangshaltung (18° bzw. 32°)
                this.fingers[k].xRot += (c - 0.15f) * 1.25f;
                this.tips[k].xRot += (c - 0.15f) * 1.05f;
                this.fingers[k].yRot -= (k - 1.5f) * this.spread;
            }
            this.thumb.xRot += (c - 0.15f) * 0.6f;
            this.thumb.yRot -= this.side * (c - 0.15f) * 0.5f - this.side * this.spread * 0.5f;
        }
    }
}
