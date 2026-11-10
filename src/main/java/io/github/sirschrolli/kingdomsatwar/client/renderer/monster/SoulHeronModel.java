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
        this.head.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * Anim.DEG * 0.7f;
        this.neck3.yRot += Mth.clamp(state.yRot, -60.0f, 60.0f) * Anim.DEG * 0.3f;
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
        if (fly > 0.0f) {
            float flap = Mth.sin(t * 0.55f);
            // Leib waagrecht, Hals eingezogen, Beine nach hinten
            this.body.xRot += 0.3f * fly;
            this.neck1.xRot += -0.6f * fly;
            this.neck2.xRot += 0.9f * fly;
            this.neck3.xRot += -0.4f * fly;
            this.head.xRot += 0.05f * fly;
            this.leftLeg.xRot += 1.25f * fly;
            this.rightLeg.xRot += 1.25f * fly;
            this.leftShin.xRot += -0.2f * fly;
            this.rightShin.xRot += -0.2f * fly;
            this.leftFoot.xRot += 0.6f * fly;
            this.rightFoot.xRot += 0.6f * fly;
            // Schwingen auf (nach außen gedreht) und schlagen
            this.leftWing.yRot += 1.45f * fly;
            this.rightWing.yRot -= 1.45f * fly;
            this.leftWing.zRot += (-0.15f - flap * 0.65f) * fly;
            this.rightWing.zRot += (0.15f + flap * 0.65f) * fly;
            this.leftWingOuter.xRot += 0.06f * fly;
            this.rightWingOuter.xRot += 0.06f * fly;
            this.leftWingOuter.zRot += -Mth.sin(t * 0.55f - 0.7f) * 0.35f * fly;
            this.rightWingOuter.zRot += Mth.sin(t * 0.55f - 0.7f) * 0.35f * fly;
            this.tail.xRot += -0.2f * fly;
            this.crest.xRot += 0.25f * fly;
        }

        // ------------------------------------------------------------ Sturzflug
        float dive = Anim.seconds(state.dive, t);
        if (dive >= 0.0f && dive < 1.6f && fly > 0.3f) {
            float tuck = Anim.bump(dive, 0.25f, 1.2f, 1.6f) * fly;
            this.leftWing.zRot += 0.55f * tuck;
            this.rightWing.zRot -= 0.55f * tuck;
            this.leftWing.yRot -= 0.5f * tuck;
            this.rightWing.yRot += 0.5f * tuck;
            this.body.xRot += 0.35f * tuck;
            this.neck1.xRot += 0.9f * tuck;
            this.neck2.xRot += -0.6f * tuck;
            this.neck3.xRot += 0.4f * tuck;
            this.head.xRot += 0.2f * tuck;
        }

        // ------------------------------------------------------------ Schnabelstoß
        float stab = Anim.seconds(state.stab, t);
        if (stab >= 0.0f && stab < 0.95f) {
            float coil = Anim.bump(stab, 0.3f, 0.42f, 0.5f);
            float thrust = Anim.bump(stab - 0.44f, 0.06f, 0.2f, 0.5f);
            this.body.xRot += -0.12f * coil + 0.22f * thrust;
            this.neck1.xRot += -0.45f * coil + 0.75f * thrust;
            this.neck2.xRot += -0.5f * coil + 0.95f * thrust;
            this.neck3.xRot += 0.35f * coil + 0.3f * thrust;
            this.head.xRot += 0.4f * coil - 0.65f * thrust;
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
