package io.github.sirschrolli.kingdomsatwar.client.renderer.end;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Asteria auf ihrem Sternenrochen. Der Rochen schlägt die Schwingen als Welle von innen nach außen (wie die Sternenrochen
 * der Leere), neigt sich in die Kurven und hebt beim Steigen den Kopf; Peitschenschwanz, Umhang, der Schweif aus
 * Sternenlicht und der Wimpel der Lanze wehen nach. Die Reiterin hält in Ruhe die Lanze aufrecht und nimmt für jeden
 * Angriff ihre Haltung ein (weich ein- und ausgeblendet):
 * <ul>
 *     <li>Tjost: Lanze eingelegt, nach vorn gebeugt, Schild vor der Brust; der Rochen legt die Schwingen an und gleitet;</li>
 *     <li>Sternenregen: die Lanze zum Himmel gereckt, der Blick nach oben;</li>
 *     <li>Sternbild: die Lanze auf das Ziel gerichtet, zum Schluss ein Stoß hinab;</li>
 *     <li>Komet: weit ausholen (Lanze über der Schulter), werfen, die Hand nach vorn – später fängt sie die Lanze;</li>
 *     <li>Schwingenschlag: der Rochen bäumt sich auf, die Schwingen hoch – dann ein mächtiger Schlag nach unten;</li>
 *     <li>Supernova: beide Arme empor, die Schwingen weit ausgebreitet;</li>
 *     <li>Herabstoßen: gleitend, am Ende die Lanze gereckt; Aufsteigen: steil nach oben;</li>
 *     <li>im Tod sinkt die Reiterin nach vorn, die Schwingen hängen.</li>
 * </ul>
 */
public class AsteriaModel extends EntityModel<AsteriaRenderState> {
    private static final float D = Anim.DEG;
    private static final AsteriaEntity.Attack[] ATTACKS = AsteriaEntity.Attack.values();

    private final ModelPart body;
    private final ModelPart head;
    private final ModelPart cephalicL;
    private final ModelPart cephalicR;
    private final ModelPart[] wingsL = new ModelPart[3];
    private final ModelPart[] wingsR = new ModelPart[3];
    private final ModelPart pelvicL;
    private final ModelPart pelvicR;
    private final ModelPart[] tail = new ModelPart[3];
    private final ModelPart rider;
    private final ModelPart torso;
    private final ModelPart helm;
    private final ModelPart[] plume = new ModelPart[3];
    private final ModelPart cape;
    private final ModelPart capeTip;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart lance;
    private final ModelPart pennant;
    private final ModelPart pennantTip;
    private final ModelPart shield;

    public AsteriaModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.head = this.body.getChild("head");
        this.cephalicL = this.head.getChild("left_cephalic");
        this.cephalicR = this.head.getChild("right_cephalic");
        for (String side : new String[] {"left", "right"}) {
            ModelPart w1 = this.body.getChild(side + "_wing");
            ModelPart w2 = w1.getChild(side + "_wing_mid");
            ModelPart w3 = w2.getChild(side + "_wing_tip");
            ModelPart[] target = side.equals("left") ? this.wingsL : this.wingsR;
            target[0] = w1;
            target[1] = w2;
            target[2] = w3;
        }
        this.pelvicL = this.body.getChild("left_pelvic");
        this.pelvicR = this.body.getChild("right_pelvic");
        this.tail[0] = this.body.getChild("tail");
        this.tail[1] = this.tail[0].getChild("tail_mid");
        this.tail[2] = this.tail[1].getChild("tail_tip");
        this.rider = this.body.getChild("rider");
        this.torso = this.rider.getChild("torso");
        this.helm = this.torso.getChild("helm");
        this.plume[0] = this.helm.getChild("plume");
        this.plume[1] = this.plume[0].getChild("plume_mid");
        this.plume[2] = this.plume[1].getChild("plume_tip");
        this.cape = this.torso.getChild("cape");
        this.capeTip = this.cape.getChild("cape_tip");
        this.armL = this.torso.getChild("left_arm");
        this.armR = this.torso.getChild("right_arm");
        this.lance = this.armR.getChild("lance");
        this.pennant = this.lance.getChild("pennant");
        this.pennantTip = this.pennant.getChild("pennant_tip");
        this.shield = this.armL.getChild("shield");
    }

    private static void add(ModelPart part, float w, float x, float y, float z) {
        if (w != 0.0f) {
            part.xRot += x * D * w;
            part.yRot += y * D * w;
            part.zRot += z * D * w;
        }
    }

    @Override
    public void setupAnim(AsteriaRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        float f = state.flap;
        float a = state.attackTime;
        AsteriaEntity.Attack attack = state.attack >= 0 && state.attack < ATTACKS.length ? ATTACKS[state.attack] : AsteriaEntity.Attack.NONE;
        float death = Mth.clamp(state.deathTime / AsteriaEntity.DEATH_TICKS, 0.0f, 1.0f);

        // ---------------------------------------------------------- Haltungen je Angriff (Hüllkurven 0..1)
        float tjost = attack == AsteriaEntity.Attack.TJOST ? Anim.bump(a, 12.0f, AsteriaEntity.TJOST_END, 68.0f) : 0.0f;
        float charge = attack == AsteriaEntity.Attack.TJOST
                ? Anim.bump(a - AsteriaEntity.TJOST_CHARGE + 4.0f, 4.0f, AsteriaEntity.TJOST_END - AsteriaEntity.TJOST_CHARGE + 4.0f, 34.0f) : 0.0f;
        float starfall = attack == AsteriaEntity.Attack.STARFALL ? Anim.bump(a, 10.0f, 50.0f, 64.0f) : 0.0f;
        float constellation = attack == AsteriaEntity.Attack.CONSTELLATION ? Anim.bump(a, 8.0f, 44.0f, 60.0f) : 0.0f;
        float stab = attack == AsteriaEntity.Attack.CONSTELLATION ? Anim.bump(a - 42.0f, 3.0f, 6.0f, 14.0f) : 0.0f;
        float windup = attack == AsteriaEntity.Attack.COMET ? Anim.bump(a, 12.0f, AsteriaEntity.COMET_THROW - 1.0f, AsteriaEntity.COMET_THROW + 3.0f) : 0.0f;
        float release = attack == AsteriaEntity.Attack.COMET ? Anim.bump(a - AsteriaEntity.COMET_THROW + 1.0f, 2.0f, 7.0f, 16.0f) : 0.0f;
        float catchIt = attack == AsteriaEntity.Attack.COMET ? Anim.bump(a - AsteriaEntity.COMET_CATCH + 5.0f, 4.0f, 8.0f, 16.0f) : 0.0f;
        float rear = attack == AsteriaEntity.Attack.WINGBEAT ? Anim.bump(a, 16.0f, 18.0f, 23.0f) : 0.0f;
        float beat = attack == AsteriaEntity.Attack.WINGBEAT ? Anim.bump(a - 18.0f, 2.0f, 5.0f, 16.0f) : 0.0f;
        float nova = attack == AsteriaEntity.Attack.SUPERNOVA ? Anim.bump(a, 16.0f, AsteriaEntity.NOVA_BLAST, AsteriaEntity.NOVA_BLAST + 12.0f) : 0.0f;
        float glide = attack == AsteriaEntity.Attack.AWAKEN ? Anim.bump(a, 6.0f, 70.0f, 90.0f) : 0.0f;
        float hail = attack == AsteriaEntity.Attack.AWAKEN ? Anim.bump(a - 66.0f, 7.0f, 14.0f, 24.0f) : 0.0f;
        float climbUp = attack == AsteriaEntity.Attack.ASCEND ? Anim.bump(a, 10.0f, 60.0f, 70.0f) : 0.0f;
        float raise = Math.max(starfall, hail);

        // ---------------------------------------------------------- der Rochen
        float calm = 1.0f - Math.max(Math.max(charge * 0.8f, rear), Math.max(beat, Math.max(glide * 0.6f, nova * 0.7f)));
        calm *= 1.0f - death;
        float[] amp = {0.3f, 0.4f, 0.48f};
        float[] lag = {0.0f, 0.65f, 1.3f};
        for (int i = 0; i < 3; i++) {
            float w = (Mth.sin(f - lag[i]) * amp[i] + (i == 0 ? 0.04f : 0.02f)) * calm;
            // aufbäumen: Schwingen hoch; Schlag: hinab; Sturmritt: angelegt; im Tod hängen sie
            w += (-rear * (i == 0 ? 50.0f : 22.0f) + beat * (i == 0 ? 38.0f : 16.0f) + death * (i == 0 ? 18.0f : 12.0f)) * D;
            this.wingsL[i].zRot += w;
            this.wingsR[i].zRot -= w;
            float twist = Mth.cos(f - lag[i]) * 0.08f * (i + 1) * calm;
            this.wingsL[i].xRot += twist;
            this.wingsR[i].xRot += twist;
        }
        add(this.wingsL[0], charge, 0, -22, 0);
        add(this.wingsR[0], charge, 0, 22, 0);
        add(this.wingsL[1], charge, 0, -10, 0);
        add(this.wingsR[1], charge, 0, 10, 0);
        this.body.y += -Mth.sin(f) * 0.8f * calm;
        this.body.xRot += Mth.clamp(-state.climb * 1.6f, -0.4f, 0.4f) * (1.0f - rear) + Mth.cos(f) * 0.03f * calm;
        this.body.zRot += state.bank * D;
        add(this.body, 1.0f, -26.0f * rear + 10.0f * beat + 6.0f * charge - 34.0f * climbUp + 8.0f * death, 0, 0);
        this.head.xRot += Mth.sin(f - 0.4f) * 0.04f * calm;
        this.cephalicL.xRot += Mth.sin(t * 0.09f) * 0.12f;
        this.cephalicR.xRot += Mth.sin(t * 0.09f + 0.5f) * 0.12f;
        this.pelvicL.zRot += Mth.sin(f - 0.9f) * 0.15f * calm;
        this.pelvicR.zRot -= Mth.sin(f - 0.9f) * 0.15f * calm;
        for (int i = 0; i < 3; i++) {
            float w = t * 0.08f - i * 0.8f;
            this.tail[i].yRot += Mth.sin(w) * (0.1f + i * 0.08f) * (1.0f - charge * 0.7f) - state.bank * D * 0.3f;
            this.tail[i].xRot += Mth.sin(f * 0.5f - i * 0.9f) * (0.05f + i * 0.04f) + Mth.clamp(state.climb, -0.3f, 0.3f) * 0.4f
                    + rear * 8.0f * D;
        }

        // ---------------------------------------------------------- die Reiterin
        // sie wiegt sich im Takt der Schwingen und lehnt sich in die Kurven
        this.rider.y += Mth.sin(f + 0.6f) * 0.25f * calm;
        this.torso.zRot += -state.bank * D * 0.45f;
        this.torso.xRot += Mth.sin(f + 0.9f) * 0.03f * calm;
        // Blick zum Ziel (der Kopf folgt der Kopfdrehung, gedämpft)
        this.helm.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * D * 0.8f;
        this.helm.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * D * 0.5f;

        add(this.torso, tjost, 12, 0, 0);
        add(this.helm, tjost, -10, 0, 0);
        add(this.armR, tjost, -25, 0, -6);
        add(this.lance, tjost, 109, 0, 10);
        add(this.armL, tjost, -32, 0, 12);
        add(this.shield, tjost, 0, 25, 0);

        add(this.torso, raise, -8, 0, 0);
        add(this.helm, raise, -22, 0, 0);
        add(this.armR, raise, -140, 0, -14);
        add(this.lance, raise, 134, 0, 10);
        add(this.armL, raise, -26, 0, -22);

        add(this.torso, constellation, 5, 0, 0);
        add(this.armR, constellation, -60, 0, -8);
        add(this.lance, constellation, 124, 0, 10);
        add(this.armL, constellation, -50, 0, 18);
        add(this.armR, stab, 22, 0, 0);
        add(this.torso, stab, 10, 0, 0);

        add(this.torso, windup, -10, 16, 0);
        add(this.armR, windup, -150, 0, -12);
        add(this.lance, windup, 209, 0, 10);
        add(this.armL, windup, -40, 0, 20);
        add(this.torso, release, 16, -12, 0);
        add(this.armR, release, -40, 0, 0);
        add(this.armR, catchIt, -110, 0, -10);
        add(this.lance, catchIt, 100, 0, 10);

        add(this.torso, rear, -6, 0, 0);
        add(this.armL, Math.max(rear, beat), -70, 0, 14);
        add(this.shield, Math.max(rear, beat), 0, 30, 0);

        add(this.torso, nova, -10, 0, 0);
        add(this.helm, nova, -25, 0, 0);
        add(this.armR, nova, -150, 0, -15);
        add(this.lance, nova, 144, 0, 10);
        add(this.armL, nova, -150, 0, 15);

        add(this.torso, death, 26, 0, 0);
        add(this.helm, death, 22, 0, 0);
        add(this.armR, death, 25, 0, 0);
        add(this.armL, death, 20, 0, 0);
        add(this.lance, death, 30, 0, 0);

        // Umhang, Schweif und Wimpel wehen (im Sturmritt waagrecht nach hinten)
        float wind = Math.max(charge, climbUp);
        this.cape.xRot += (Mth.sin(t * 0.31f) * 4.0f + 34.0f * wind + 12.0f * glide) * D * (1.0f - death * 0.5f);
        this.capeTip.xRot += (Mth.sin(t * 0.31f - 0.9f) * 7.0f + 6.0f * wind) * D;
        this.cape.zRot += Mth.sin(t * 0.17f) * 2.0f * D;
        for (int i = 0; i < 3; i++) {
            this.plume[i].yRot += Mth.sin(t * 0.27f - i * 0.8f) * (6.0f + i * 5.0f) * D;
            this.plume[i].xRot += (Mth.sin(t * 0.21f - i * 0.6f) * 3.0f + (i == 0 ? 14.0f : 6.0f) * wind) * D;
        }
        this.pennant.yRot += Mth.sin(t * 0.5f) * 14.0f * D;
        this.pennantTip.yRot += Mth.sin(t * 0.5f - 0.9f) * 24.0f * D;
        this.lance.visible = state.lance;
    }
}
