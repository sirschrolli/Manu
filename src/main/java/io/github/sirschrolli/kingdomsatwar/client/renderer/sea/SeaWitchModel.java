package io.github.sirschrolli.kingdomsatwar.client.renderer.sea;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.sea.SeaWitchEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Die Meerhexe: Sie tritt Wasser, ihr Aalschwanz schlängelt sich in einer Welle bis in die zerrissene Flosse, die Fächerflossen
 * öffnen und schließen sich, ihr Haar und die treibenden Strähnen wogen, die Bahnen ihres Umhangs wehen, die Muscheln an
 * ihrem Stab pendeln. In Trance hält sie die Arme vor der Brust verschränkt, den Kopf gesenkt. Jeder Zauber hat seine
 * Gebärde: den Stab heben und Tinte schleudern, die Arme kreisen lassen (Strudel), singen mit ausgebreiteten Armen, die Hand
 * vorstrecken und zur Faust schließen (Blase), beide Arme hoch (Haie), den Stab niederstoßen (Korallen), die Klauen vorstrecken
 * (Fluch), herumwirbeln (Schwanzhieb) – und im Zorn den Kopf zurückwerfen und schreien.
 */
public class SeaWitchModel extends EntityModel<SeaWitchRenderState> {
    private final ModelPart swim;
    private final ModelPart torso;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart staff;
    private final ModelPart[] charms;
    private final ModelPart hairBack;
    private final ModelPart hairTip;
    private final ModelPart locksL;
    private final ModelPart locksR;
    private final ModelPart earL;
    private final ModelPart earR;
    private final ModelPart[] strands;
    private final ModelPart[] strandTips;
    private final ModelPart shawlL;
    private final ModelPart shawlR;
    private final ModelPart cape;
    private final ModelPart capeTip;
    private final ModelPart[] tail;
    private final ModelPart[] spines;
    private final ModelPart fluke;
    private final ModelPart flukeL;
    private final ModelPart flukeR;
    private final ModelPart fanL;
    private final ModelPart fanR;

    public SeaWitchModel(ModelPart root) {
        super(root);
        this.swim = root.getChild("swim");
        this.torso = this.swim.getChild("torso");
        this.head = this.torso.getChild("head");
        this.leftArm = this.torso.getChild("left_arm");
        this.rightArm = this.torso.getChild("right_arm");
        this.staff = this.rightArm.getChild("staff");
        this.charms = new ModelPart[] {this.staff.getChild("staff_charm_0"), this.staff.getChild("staff_charm_1"), this.staff.getChild("staff_charm_2")};
        this.hairBack = this.head.getChild("hair_back");
        this.hairTip = this.hairBack.getChild("hair_tip");
        this.locksL = this.head.getChild("locks_l");
        this.locksR = this.head.getChild("locks_r");
        this.earL = this.head.getChild("ear_l");
        this.earR = this.head.getChild("ear_r");
        this.strands = new ModelPart[5];
        this.strandTips = new ModelPart[5];
        for (int i = 0; i < 5; i++) {
            this.strands[i] = this.head.getChild("strand_" + i);
            this.strandTips[i] = this.strands[i].getChild("strand_" + i + "_tip");
        }
        ModelPart shawl = this.torso.getChild("shawl");
        this.shawlL = shawl.getChild("shawl_l");
        this.shawlR = shawl.getChild("shawl_r");
        this.cape = shawl.getChild("cape");
        this.capeTip = this.cape.getChild("cape_tip");
        ModelPart t1 = this.swim.getChild("tail1");
        ModelPart t2 = t1.getChild("tail2");
        ModelPart t3 = t2.getChild("tail3");
        ModelPart t4 = t3.getChild("tail4");
        ModelPart t5 = t4.getChild("tail5");
        this.tail = new ModelPart[] {t1, t2, t3, t4, t5};
        this.spines = new ModelPart[] {t2.getChild("spines_0"), t3.getChild("spines_1"), t4.getChild("spines_2")};
        this.fluke = t5.getChild("fluke");
        this.flukeL = this.fluke.getChild("fluke_l");
        this.flukeR = this.fluke.getChild("fluke_r");
        this.fanL = t1.getChild("fan_l");
        this.fanR = t1.getChild("fan_r");
    }

    private static SeaWitchEntity.Attack attack(SeaWitchRenderState state) {
        return SeaWitchEntity.Attack.values()[Mth.clamp(state.attack, 0, SeaWitchEntity.Attack.values().length - 1)];
    }

    @Override
    public void setupAnim(SeaWitchRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        SeaWitchEntity.Attack attack = attack(state);
        float a = state.attackTime;
        Object key = state.owner != null ? state.owner : this;
        float sw = Anim.follow(key, 0, state.swim, t, 4.0f);
        float trance = Anim.follow(key, 1, attack == SeaWitchEntity.Attack.DORMANT ? 1.0f : 0.0f, t, 2.5f);
        float fury = state.enraged ? 1.0f : 0.0f;
        float ph = t * (0.13f + 0.1f * sw + 0.04f * fury);

        // ------------------------------------------------------------ der ganze Körper: wassertretend, beim Schwimmen geneigt
        this.swim.xRot += sw * 0.9f;
        this.swim.y += Mth.sin(t * 0.09f) * 0.6f * (1.0f - sw);
        this.swim.zRot += Mth.sin(t * 0.045f) * 0.05f;
        this.torso.xRot += -Mth.sin(ph + 0.6f) * 0.03f;

        // ------------------------------------------------------------ der Aalschwanz: Welle nach hinten und ein Schlängeln zur Seite
        float amp = 0.12f + 0.2f * sw + 0.05f * fury;
        for (int i = 0; i < this.tail.length; i++) {
            this.tail[i].xRot += Mth.sin(ph - i * 0.7f) * amp * (0.35f + i * 0.2f);
            this.tail[i].zRot += Mth.sin(t * 0.07f - i * 0.65f) * (0.05f + i * 0.015f) * (1.0f - sw * 0.6f);
        }
        this.fluke.xRot += Mth.sin(ph - 3.6f) * amp * 1.4f;
        float flex = Mth.sin(ph - 4.0f) * 0.1f;
        this.flukeL.zRot += flex;
        this.flukeR.zRot -= flex;
        for (int i = 0; i < this.spines.length; i++) {
            this.spines[i].xRot += Mth.sin(t * 0.11f - i * 0.8f) * 0.12f;
        }
        // die Fächerflossen öffnen und schließen sich
        float fan = Mth.sin(t * 0.1f) * 0.14f + sw * 0.35f;
        this.fanL.zRot -= fan;
        this.fanR.zRot += fan;
        this.fanL.xRot += Mth.sin(t * 0.08f) * 0.1f;
        this.fanR.xRot += Mth.sin(t * 0.08f + 0.7f) * 0.1f;

        // ------------------------------------------------------------ Haar, Strähnen, Umhang
        float drift = Mth.sin(t * 0.05f);
        this.hairBack.xRot += 0.15f + drift * 0.1f + sw * 0.8f;
        this.hairTip.xRot += Mth.sin(t * 0.05f - 0.9f) * 0.18f + sw * 0.2f;
        this.hairBack.zRot += Mth.sin(t * 0.037f) * 0.07f;
        this.locksL.zRot += Mth.sin(t * 0.06f) * 0.06f;
        this.locksR.zRot -= Mth.sin(t * 0.06f + 0.4f) * 0.06f;
        this.locksL.xRot += sw * 0.9f + drift * 0.05f;
        this.locksR.xRot += sw * 0.9f + drift * 0.05f;
        for (int i = 0; i < this.strands.length; i++) {
            float w = t * (0.05f + 0.01f * i) + i * 1.7f;
            this.strands[i].xRot += Mth.sin(w) * 0.16f + sw * 0.9f;
            this.strands[i].zRot += Mth.cos(w * 0.8f) * 0.12f;
            this.strandTips[i].xRot += Mth.sin(w - 1.1f) * 0.3f;
            this.strandTips[i].zRot += Mth.cos(w * 0.9f - 0.8f) * 0.22f;
        }
        this.earL.yRot += Mth.sin(t * 0.19f) * 0.12f;
        this.earR.yRot -= Mth.sin(t * 0.19f + 0.3f) * 0.12f;
        this.shawlL.xRot += -Mth.abs(Mth.sin(t * 0.07f)) * 0.15f - sw * 0.6f;
        this.shawlR.xRot += -Mth.abs(Mth.sin(t * 0.07f + 0.9f)) * 0.15f - sw * 0.6f;
        this.cape.xRot += 0.1f + Mth.sin(t * 0.06f) * 0.08f + sw * 0.7f;
        this.capeTip.xRot += Mth.sin(t * 0.06f - 1.0f) * 0.14f + sw * 0.2f;
        for (int i = 0; i < this.charms.length; i++) {
            this.charms[i].zRot += Mth.sin(t * 0.12f + i * 1.4f) * 0.22f;
            this.charms[i].xRot += Mth.sin(t * 0.09f + i) * 0.15f;
        }

        // ------------------------------------------------------------ Kopf und Arme (Grundhaltung)
        this.head.yRot = Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG;
        this.head.xRot = Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * (1.0f - sw) - sw * 0.6f;
        float scull = Mth.sin(t * 0.11f);
        this.leftArm.xRot = scull * 0.25f - 0.3f;
        this.leftArm.zRot = -0.45f - Mth.cos(t * 0.11f) * 0.1f;
        // rechts hält sie den Stab aufrecht vor sich
        this.rightArm.xRot = -0.45f + Mth.sin(t * 0.07f) * 0.05f;
        this.rightArm.zRot = 0.2f;
        this.staff.xRot = 0.45f;

        // ------------------------------------------------------------ Trance: verschränkte Arme, gesenkter Kopf
        if (trance > 0.001f) {
            this.head.xRot = Mth.lerp(trance, this.head.xRot, 0.45f + Mth.sin(t * 0.03f) * 0.04f);
            this.head.yRot *= 1.0f - trance;
            this.leftArm.xRot = Mth.lerp(trance, this.leftArm.xRot, -1.25f);
            this.leftArm.yRot = Mth.lerp(trance, this.leftArm.yRot, -0.75f);
            this.leftArm.zRot = Mth.lerp(trance, this.leftArm.zRot, 0.15f);
            this.rightArm.xRot = Mth.lerp(trance, this.rightArm.xRot, -0.95f);
            this.rightArm.yRot = Mth.lerp(trance, this.rightArm.yRot, 0.45f);
            this.staff.xRot = Mth.lerp(trance, this.staff.xRot, 0.95f);
        }

        // ------------------------------------------------------------ Gebärden der Zauber
        switch (attack) {
            case AWAKEN -> this.awaken(a, t);
            case INK -> this.ink(a);
            case WHIRLPOOL -> this.whirlpool(a, t);
            case SONG -> this.song(a, t);
            case BUBBLE -> this.bubble(a);
            case SHARKS -> this.sharks(a);
            case CORAL -> this.coral(a);
            case CURSE -> this.curse(a, t);
            case TAIL -> this.tailSweep(a);
            case FURY -> this.fury(a, t);
            default -> { }
        }

        // ------------------------------------------------------------ Tod: Sie sinkt rücklings, die Arme treiben schlaff
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 30.0f, 0.0f, 1.0f);
            float e = d * d * (3.0f - 2.0f * d);
            this.swim.xRot = Mth.lerp(e, this.swim.xRot, -1.1f);
            this.head.xRot = Mth.lerp(e, this.head.xRot, -0.5f);
            this.leftArm.xRot = Mth.lerp(e, this.leftArm.xRot, -2.6f);
            this.leftArm.zRot = Mth.lerp(e, this.leftArm.zRot, -0.6f);
            this.rightArm.xRot = Mth.lerp(e, this.rightArm.xRot, -2.4f);
            this.rightArm.zRot = Mth.lerp(e, this.rightArm.zRot, 0.6f);
        }
    }

    /** Erwachen: Kopf hoch, die Arme lösen sich und breiten sich aus, sie lacht mit zurückgeworfenem Kopf. */
    private void awaken(float a, float t) {
        float open = Anim.bump(a, 20.0f, 52.0f, 70.0f);
        float laugh = Anim.bump(a - 36.0f, 4.0f, 14.0f, 24.0f);
        this.leftArm.zRot = Mth.lerp(open, this.leftArm.zRot, -1.35f);
        this.leftArm.xRot = Mth.lerp(open, this.leftArm.xRot, -0.5f);
        this.rightArm.zRot = Mth.lerp(open, this.rightArm.zRot, 1.0f);
        this.rightArm.xRot = Mth.lerp(open, this.rightArm.xRot, -1.2f);
        this.staff.xRot = Mth.lerp(open, this.staff.xRot, 1.2f);
        this.head.xRot = Mth.lerp(laugh, this.head.xRot, -0.55f + Mth.sin(t * 1.1f) * 0.06f);
        this.torso.xRot -= laugh * 0.15f;
    }

    /** Tintengeschosse: den Stab hoch über den Kopf, mit jedem Schuss ein Ruck nach vorn. */
    private void ink(float a) {
        float raise = Anim.bump(a, 8.0f, 28.0f, 40.0f);
        float jolt = 0.0f;
        for (float shot : new float[] {12.0f, 18.0f, 24.0f}) {
            jolt = Math.max(jolt, Anim.bump(a - shot, 1.5f, 2.0f, 5.0f));
        }
        this.rightArm.xRot = Mth.lerp(raise, this.rightArm.xRot, -2.5f + jolt * 0.45f);
        this.rightArm.zRot = Mth.lerp(raise, this.rightArm.zRot, 0.1f);
        this.staff.xRot = Mth.lerp(raise, this.staff.xRot, 0.6f + jolt * 0.3f);
        this.leftArm.xRot = Mth.lerp(raise, this.leftArm.xRot, -0.9f);
        this.leftArm.zRot = Mth.lerp(raise, this.leftArm.zRot, -0.7f);
        this.torso.xRot += jolt * 0.08f;
    }

    /** Strudel: beide Arme kreisen, der Schwanz peitscht zur Seite. */
    private void whirlpool(float a, float t) {
        float w = Anim.bump(a, 6.0f, 40.0f, 60.0f);
        float c = a * 0.35f;
        this.leftArm.xRot = Mth.lerp(w, this.leftArm.xRot, -1.5f + Mth.sin(c) * 0.6f);
        this.leftArm.zRot = Mth.lerp(w, this.leftArm.zRot, -0.6f + Mth.cos(c) * 0.5f);
        this.rightArm.xRot = Mth.lerp(w, this.rightArm.xRot, -1.5f + Mth.sin(c + Mth.PI) * 0.6f);
        this.rightArm.zRot = Mth.lerp(w, this.rightArm.zRot, 0.6f - Mth.cos(c + Mth.PI) * 0.5f);
        for (int i = 1; i < this.tail.length; i++) {
            this.tail[i].zRot += Mth.sin(t * 0.3f - i * 0.8f) * 0.18f * w;
        }
    }

    /** Sirenengesang: der Kopf leicht zurück, die Arme weit und weich geöffnet, sie wiegt sich. */
    private void song(float a, float t) {
        float w = Anim.bump(a, 10.0f, 72.0f, 84.0f);
        this.head.xRot = Mth.lerp(w, this.head.xRot, -0.3f + Mth.sin(t * 0.08f) * 0.06f);
        this.head.zRot += Mth.sin(t * 0.1f) * 0.12f * w;
        this.leftArm.xRot = Mth.lerp(w, this.leftArm.xRot, -0.7f + Mth.sin(t * 0.09f) * 0.15f);
        this.leftArm.zRot = Mth.lerp(w, this.leftArm.zRot, -1.25f);
        this.rightArm.xRot = Mth.lerp(w, this.rightArm.xRot, -0.8f + Mth.sin(t * 0.09f + 1.0f) * 0.12f);
        this.rightArm.zRot = Mth.lerp(w, this.rightArm.zRot, 1.0f);
        this.swim.zRot += Mth.sin(t * 0.12f) * 0.08f * w;
        this.fanL.zRot -= 0.35f * w;
        this.fanR.zRot += 0.35f * w;
    }

    /** Blasengefängnis: die linke Hand vorstrecken – und zur Faust schließen. */
    private void bubble(float a) {
        float w = Anim.bump(a, 7.0f, 22.0f, 36.0f);
        float squeeze = Anim.bump(a - 11.0f, 2.0f, 12.0f, 20.0f);
        this.leftArm.xRot = Mth.lerp(w, this.leftArm.xRot, -1.65f + squeeze * 0.2f);
        this.leftArm.zRot = Mth.lerp(w, this.leftArm.zRot, -0.1f);
        this.leftArm.yRot = Mth.lerp(w, this.leftArm.yRot, 0.2f);
        this.torso.xRot += squeeze * 0.06f;
    }

    /** Haie rufen: beide Arme hoch über den Kopf, dann nach außen gerissen. */
    private void sharks(float a) {
        float up = Anim.bump(a, 8.0f, 20.0f, 26.0f);
        float out = Anim.bump(a - 22.0f, 3.0f, 10.0f, 20.0f);
        this.leftArm.xRot = Mth.lerp(up, this.leftArm.xRot, -2.9f);
        this.leftArm.zRot = Mth.lerp(up, this.leftArm.zRot, -0.25f);
        this.rightArm.xRot = Mth.lerp(up, this.rightArm.xRot, -2.8f);
        this.rightArm.zRot = Mth.lerp(up, this.rightArm.zRot, 0.25f);
        this.leftArm.zRot = Mth.lerp(out, this.leftArm.zRot, -1.5f);
        this.rightArm.zRot = Mth.lerp(out, this.rightArm.zRot, 1.4f);
        this.leftArm.xRot = Mth.lerp(out, this.leftArm.xRot, -0.6f);
        this.rightArm.xRot = Mth.lerp(out, this.rightArm.xRot, -0.8f);
        this.head.xRot -= up * 0.35f;
    }

    /** Korallenspieße: den Stab hoch – und nieder in den Grund stoßen. */
    private void coral(float a) {
        float raise = Anim.bump(a, 6.0f, 12.0f, 16.0f);
        float slam = Anim.bump(a - 14.0f, 2.0f, 18.0f, 30.0f);
        this.rightArm.xRot = Mth.lerp(raise, this.rightArm.xRot, -2.7f);
        this.staff.xRot = Mth.lerp(raise, this.staff.xRot, 0.3f);
        this.rightArm.xRot = Mth.lerp(slam, this.rightArm.xRot, -0.35f);
        this.staff.xRot = Mth.lerp(slam, this.staff.xRot, 1.9f);
        this.torso.xRot += slam * 0.25f;
        this.head.xRot += slam * 0.2f;
        this.leftArm.zRot = Mth.lerp(slam, this.leftArm.zRot, -0.9f);
    }

    /** Fluch der Tiefe: beide Klauen vorgestreckt, zitternd, der Kopf gesenkt. */
    private void curse(float a, float t) {
        float w = Anim.bump(a, 8.0f, 40.0f, 52.0f);
        float tremble = Mth.sin(t * 1.7f) * 0.05f * w;
        this.leftArm.xRot = Mth.lerp(w, this.leftArm.xRot, -1.45f + tremble);
        this.leftArm.zRot = Mth.lerp(w, this.leftArm.zRot, -0.35f);
        this.rightArm.xRot = Mth.lerp(w, this.rightArm.xRot, -1.35f - tremble);
        this.rightArm.zRot = Mth.lerp(w, this.rightArm.zRot, 0.35f);
        this.head.xRot = Mth.lerp(w, this.head.xRot, 0.3f);
    }

    /** Schwanzhieb: Sie wirbelt einmal ganz herum, der Schwanz fegt weit aus. */
    private void tailSweep(float a) {
        float spin = Mth.clamp((a - 6.0f) / 10.0f, 0.0f, 1.0f);
        float s = spin * spin * (3.0f - 2.0f * spin);
        this.swim.yRot += s * Mth.TWO_PI;
        float lash = Anim.bump(a - 6.0f, 3.0f, 8.0f, 14.0f);
        for (int i = 1; i < this.tail.length; i++) {
            this.tail[i].zRot += lash * 0.22f;
            this.tail[i].xRot -= lash * 0.12f;
        }
        this.leftArm.zRot = Mth.lerp(lash, this.leftArm.zRot, -1.3f);
        this.rightArm.zRot = Mth.lerp(lash, this.rightArm.zRot, 1.1f);
    }

    /** Zorn: der Kopf fliegt zurück, die Arme weit nach hinten gerissen – ein Schrei, der den ganzen Körper schüttelt. */
    private void fury(float a, float t) {
        float w = Anim.bump(a, 12.0f, 34.0f, 56.0f);
        float shake = Mth.sin(t * 2.3f) * 0.04f * w;
        this.head.xRot = Mth.lerp(w, this.head.xRot, -0.75f + shake);
        this.torso.xRot -= 0.25f * w;
        this.leftArm.xRot = Mth.lerp(w, this.leftArm.xRot, 0.5f + shake);
        this.leftArm.zRot = Mth.lerp(w, this.leftArm.zRot, -1.7f);
        this.rightArm.xRot = Mth.lerp(w, this.rightArm.xRot, 0.3f - shake);
        this.rightArm.zRot = Mth.lerp(w, this.rightArm.zRot, 1.6f);
        this.fanL.zRot -= 0.5f * w;
        this.fanR.zRot += 0.5f * w;
        for (ModelPart strand : this.strands) {
            strand.xRot -= 0.3f * w;
        }
    }
}
