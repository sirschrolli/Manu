package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Seelenreiher: Im Stehen wiegt er sich kaum, der Hals pendelt, der Schopf weht. Beim Waten heben sich die Stelzenbeine
 * im Wechsel, der Kopf nickt mit jedem Schritt vor und zurück. Zum Stoß zieht er den Hals in ein enges S zurück und lässt
 * ihn dann mit dem Speerschnabel vorschnellen; beim Schwingenschlag reißt er die Flügel weit auf, öffnet den Schnabel und
 * fegt sie nach vorn. Im Flug liegen Leib und eingezogener Hals waagrecht, die Beine ragen nach hinten, die Schwingen
 * schlagen; im Sturzflug legt er sie halb an und stößt den Schnabel voran.
 */
public class SoulHeronModel extends EntityModel<SoulHeronRenderState> {
    private final ModelPart body;
    private final ModelPart plumes;
    private final ModelPart tail;
    private final ModelPart leftWing;
    private final ModelPart rightWing;
    private final ModelPart leftWingOuter;
    private final ModelPart rightWingOuter;
    private final ModelPart neck1;
    private final ModelPart neck2;
    private final ModelPart neck3;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart crest;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart leftShin;
    private final ModelPart rightShin;
    private final ModelPart leftFoot;
    private final ModelPart rightFoot;

    public SoulHeronModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.plumes = this.body.getChild("plumes");
        this.tail = this.body.getChild("tail");
        this.leftWing = this.body.getChild("left_wing");
        this.rightWing = this.body.getChild("right_wing");
        this.leftWingOuter = this.leftWing.getChild("left_wing_outer");
        this.rightWingOuter = this.rightWing.getChild("right_wing_outer");
        this.neck1 = this.body.getChild("neck1");
        this.neck2 = this.neck1.getChild("neck2");
        this.neck3 = this.neck2.getChild("neck3");
        this.head = this.neck3.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.crest = this.head.getChild("crest");
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.leftShin = this.leftLeg.getChild("left_shin");
        this.rightShin = this.rightLeg.getChild("right_shin");
        this.leftFoot = this.leftShin.getChild("left_foot");
        this.rightFoot = this.rightShin.getChild("right_foot");
    }

    @Override
    public void setupAnim(SoulHeronRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float fly = state.flying;
        float ground = 1.0f - fly;
        float pos = state.walkAnimationPos;
        float speed = Math.min(1.0f, state.walkAnimationSpeed) * ground;

        // ------------------------------------------------------------ Stehen und Waten
        float sway = Mth.sin(t * 0.05f);
        this.body.xRot += sway * 0.02f;
        this.neck1.xRot += Mth.sin(t * 0.04f) * 0.05f;
        this.neck2.xRot += Mth.sin(t * 0.04f + 0.8f) * 0.06f;
        this.head.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * Anim.DEG * 0.7f * ground;
        this.neck3.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * Anim.DEG * 0.3f * ground;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.5f;
        this.crest.xRot += Mth.sin(t * 0.12f) * 0.1f;
        this.crest.yRot += Mth.sin(t * 0.07f) * 0.08f;
        this.plumes.xRot += Mth.sin(t * 0.09f) * 0.05f;
        this.tail.xRot += Mth.sin(t * 0.06f) * 0.04f;
        // Schritte: das eine Bein hebt sich (Ferse knickt), das andere stützt; der Kopf nickt mit
        float step = Mth.cos(pos * 0.55f);
        this.leftLeg.xRot += step * 0.55f * speed;
        this.rightLeg.xRot -= step * 0.55f * speed;
        this.leftShin.xRot += Math.max(0.0f, -step) * 0.7f * speed;
        this.rightShin.xRot += Math.max(0.0f, step) * 0.7f * speed;
        this.leftFoot.xRot += Math.max(0.0f, -step) * 0.5f * speed;
        this.rightFoot.xRot += Math.max(0.0f, step) * 0.5f * speed;
        this.neck2.xRot += Mth.sin(pos * 1.1f) * 0.15f * speed;
        this.head.xRot -= Mth.sin(pos * 1.1f) * 0.12f * speed;
        this.body.y += Math.abs(Mth.sin(pos * 0.55f)) * -0.4f * speed;

        // ------------------------------------------------------------ Flug
        float dive = Anim.seconds(state.dive, t);
        float tuck = dive >= 0.0f && dive < 1.6f ? Anim.bump(dive, 0.25f, 1.2f, 1.6f) * fly : 0.0f;
        if (fly > 0.0f) {
            float beat = 1.0f - tuck;
            float flap = Mth.sin(t * 0.55f) * beat;
            float lag = Mth.sin(t * 0.55f - 0.7f) * beat;
            // Leib waagrecht; der Hals liegt in einem engen S, der Kopf ruht vorn zwischen den Schultern, der Schnabel voran
            this.body.xRot += 0.21f * fly;
            this.neck1.xRot += 1.47f * fly;
            this.neck2.xRot += -1.47f * fly;
            this.neck3.xRot += 1.2f * fly;
            this.head.xRot += -1.41f * fly;
            // die Beine lang nach hinten gestreckt
            this.leftLeg.xRot += 1.6f * fly;
            this.rightLeg.xRot += 1.6f * fly;
            this.leftShin.xRot += -0.25f * fly;
            this.rightShin.xRot += -0.25f * fly;
            this.leftFoot.xRot += 0.5f * fly;
            this.rightFoot.xRot += 0.5f * fly;
            // die Schwingen waagrecht ausgebreitet (die glimmende Hinterkante nach hinten) und weit gestreckt; sie schlagen
            // auf und ab, die Handschwingen folgen ein wenig später
            this.leftWing.xRot += Mth.HALF_PI * fly;
            this.rightWing.xRot += Mth.HALF_PI * fly;
            this.leftWing.zRot += (Mth.HALF_PI - 0.1f - flap * 0.6f) * fly;
            this.rightWing.zRot += (-Mth.HALF_PI + 0.1f + flap * 0.6f) * fly;
            this.leftWing.zScale = 1.0f + 0.3f * fly;
            this.rightWing.zScale = 1.0f + 0.3f * fly;
            this.leftWing.yScale = 1.0f + 0.12f * fly;
            this.rightWing.yScale = 1.0f + 0.12f * fly;
            this.leftWingOuter.yRot += -lag * 0.4f * fly;
            this.rightWingOuter.yRot += lag * 0.4f * fly;
            this.tail.xRot += -0.38f * fly;
            this.crest.xRot += -0.75f * fly;                      // der Schopf weht flach nach hinten
        }

        // ------------------------------------------------------------ Sturzflug
        if (tuck > 0.0f) {
            // die Schwingen hoch zum V angelegt, der Leib kippt nach vorn, der Hals streckt sich wie ein Speer
            this.leftWing.zRot += -0.95f * tuck;
            this.rightWing.zRot += 0.95f * tuck;
            this.leftWing.zScale -= 0.2f * tuck;
            this.rightWing.zScale -= 0.2f * tuck;
            this.body.xRot += 0.35f * tuck;
            this.neck1.xRot += -0.6f * tuck;
            this.neck2.xRot += 2.44f * tuck;
            this.neck3.xRot += -1.83f * tuck;
            this.head.xRot += -0.09f * tuck;
        }

        // ------------------------------------------------------------ Schnabelstoß
        float stab = Anim.seconds(state.stab, t);
        if (stab >= 0.0f && stab < 0.95f) {
            float coil = Anim.bump(stab, 0.3f, 0.42f, 0.5f);
            float thrust = Anim.bump(stab - 0.44f, 0.06f, 0.2f, 0.5f);
            // ausholen: ein enges S nach hinten; zustoßen: der Hals streckt sich weit vor, der Kopf bleibt waagrecht
            this.body.xRot += -0.12f * coil + 0.22f * thrust;
            this.neck1.xRot += -0.65f * coil + 0.58f * thrust;
            this.neck2.xRot += 0.1f * coil + 1.06f * thrust;
            this.neck3.xRot += 1.1f * coil - 0.54f * thrust;
            this.head.xRot += -0.45f * coil - 1.15f * thrust;
            this.jaw.xRot += 0.15f * thrust;
            this.crest.xRot += 0.3f * coil - 0.2f * thrust;
        }

        // ------------------------------------------------------------ Schwingenschlag
        float buffet = Anim.seconds(state.buffet, t);
        if (buffet >= 0.0f && buffet < 0.95f) {
            float spread = Anim.bump(buffet, 0.32f, 0.45f, 0.62f);
            float sweep = Anim.bump(buffet - 0.42f, 0.1f, 0.22f, 0.5f);
            this.leftWing.yRot += 1.3f * spread - 0.6f * sweep;
            this.rightWing.yRot -= 1.3f * spread - 0.6f * sweep;
            this.leftWing.zRot += -0.75f * spread + 0.5f * sweep;
            this.rightWing.zRot += 0.75f * spread - 0.5f * sweep;
            this.leftWingOuter.zRot += -0.3f * spread;
            this.rightWingOuter.zRot += 0.3f * spread;
            this.neck1.xRot += -0.25f * spread;
            this.head.xRot += -0.35f * spread;
            this.jaw.xRot += 0.55f * spread;
            this.body.xRot += -0.15f * spread + 0.2f * sweep;
            this.crest.xRot += 0.4f * spread;
        }
    }
}
