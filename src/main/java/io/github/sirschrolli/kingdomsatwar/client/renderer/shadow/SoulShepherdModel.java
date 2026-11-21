package io.github.sirschrolli.kingdomsatwar.client.renderer.shadow;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.shadow.SoulShepherdEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Der Seelenhirte: Er atmet schwer und gebeugt, die Fetzen seiner Kutte, das Fell und der Umhang wehen, die Laterne pendelt
 * an ihrem Haken (und hängt immer lotrecht, wie er den Stab auch schwingt), die Talismane am Strick schaukeln, und drei
 * Irrlichter kreisen um ihn. Er geht schwer auf den Stab gestützt. In Andacht hält er den Kopf tief gesenkt. Jeder Angriff
 * hat seine Gebärde: den Haken vorschnellen lassen und zurückreißen, die Laterne in weitem Bogen schwingen, die Laterne
 * hochhalten (Irrlichter), die Handglocke heben und läuten, sich in den Nebel ducken, mit der Laterne einen Kreis ziehen
 * (Pferch), den Stab über dem Kopf in den Boden rammen, mit ausgebreiteten Armen die Seelen um sich wirbeln (Sturm) – und
 * im Zorn den Schädel zurückwerfen, das Maul aufreißen und dreimal läuten.
 *
 * <p>Alle Haltungen werden zur Ruhehaltung des Modells addiert (diese hält den Stab lotrecht vor ihm).
 */
public class SoulShepherdModel extends EntityModel<SoulShepherdRenderState> {
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart head;
    private final ModelPart jaw;
    private final ModelPart hoodTail;
    private final ModelPart[] rags;
    private final ModelPart[] mantleSides;
    private final ModelPart cloak;
    private final ModelPart cloakTip;
    private final ModelPart leftArm;
    private final ModelPart leftFore;
    private final ModelPart leftRag;
    private final ModelPart bell;
    private final ModelPart rightArm;
    private final ModelPart rightFore;
    private final ModelPart rightRag;
    private final ModelPart crook;
    private final ModelPart lantern;
    private final ModelPart skirt;
    private final ModelPart[] hems;
    private final ModelPart leftLeg;
    private final ModelPart rightLeg;
    private final ModelPart[] charms;
    private final ModelPart[] wisps;

    public SoulShepherdModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.head = this.chest.getChild("head");
        this.jaw = this.head.getChild("jaw");
        this.hoodTail = this.head.getChild("hood_tail");
        ModelPart mantle = this.chest.getChild("mantle");
        this.rags = new ModelPart[4];
        for (int i = 0; i < 4; i++) {
            this.rags[i] = mantle.getChild("mantle_rag_" + i);
        }
        this.mantleSides = new ModelPart[] {mantle.getChild("mantle_side_0"), mantle.getChild("mantle_side_1")};
        this.cloak = this.chest.getChild("cloak");
        this.cloakTip = this.cloak.getChild("cloak_tip");
        this.leftArm = this.chest.getChild("left_arm");
        this.leftFore = this.leftArm.getChild("left_arm_fore");
        this.leftRag = this.leftFore.getChild("left_arm_rag");
        this.bell = this.leftFore.getChild("hand_bell");
        this.rightArm = this.chest.getChild("right_arm");
        this.rightFore = this.rightArm.getChild("right_arm_fore");
        this.rightRag = this.rightFore.getChild("right_arm_rag");
        this.crook = this.rightFore.getChild("crook");
        ModelPart hook = this.crook;
        for (int i = 0; i < 6; i++) {
            hook = hook.getChild("hook_" + i);
        }
        this.lantern = hook.getChild("lantern");
        this.skirt = root.getChild("skirt");
        this.hems = new ModelPart[12];
        for (int i = 0; i < 12; i++) {
            this.hems[i] = this.skirt.getChild("hem_" + i);
        }
        this.leftLeg = root.getChild("left_leg");
        this.rightLeg = root.getChild("right_leg");
        this.charms = new ModelPart[] {this.body.getChild("charm_0"), this.body.getChild("charm_1"), this.body.getChild("charm_2")};
        this.wisps = new ModelPart[] {root.getChild("wisp_0"), root.getChild("wisp_1"), root.getChild("wisp_2")};
    }

    private static SoulShepherdEntity.Attack attack(SoulShepherdRenderState state) {
        return SoulShepherdEntity.Attack.values()[Mth.clamp(state.attack, 0, SoulShepherdEntity.Attack.values().length - 1)];
    }

    @Override
    public void setupAnim(SoulShepherdRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        SoulShepherdEntity.Attack attack = attack(state);
        float a = state.attackTime;
        Object key = state.owner != null ? state.owner : this;
        float trance = Anim.follow(key, 0, attack == SoulShepherdEntity.Attack.DORMANT ? 1.0f : 0.0f, t, 2.0f);
        float fury = state.enraged ? 1.0f : 0.0f;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float pos = state.walkAnimationPos;

        // ------------------------------------------------------------ Atem und Grundhaltung
        float breath = Mth.sin(t * 0.055f);
        this.chest.xRot += breath * 0.03f;
        this.head.xRot += -breath * 0.025f;
        this.head.yRot += Mth.clamp(state.yRot, -40.0f, 40.0f) * Anim.DEG * (1.0f - trance);
        this.head.xRot += Mth.clamp(state.xRot, -25.0f, 25.0f) * Anim.DEG * 0.6f * (1.0f - trance);
        this.head.zRot += Mth.sin(t * 0.031f) * 0.03f;
        this.leftArm.zRot += -Mth.sin(t * 0.05f + 0.5f) * 0.03f;

        // ------------------------------------------------------------ Gehen: schwer, auf den Stab gestützt
        float step = Mth.cos(pos * 0.55f);
        this.leftLeg.xRot += step * 0.55f * speed;
        this.rightLeg.xRot -= step * 0.55f * speed;
        this.body.y += Mth.abs(Mth.sin(pos * 0.55f)) * 0.9f * speed;
        this.body.zRot += step * 0.05f * speed;
        this.skirt.zRot += step * 0.04f * speed;
        this.skirt.xRot += Mth.sin(pos * 1.1f) * 0.03f * speed;
        this.leftArm.xRot -= step * 0.35f * speed;
        // den Stab setzt er bei jedem zweiten Schritt vor
        this.rightArm.xRot += Mth.sin(pos * 0.275f) * 0.18f * speed;
        this.chest.xRot += 0.06f * speed;

        // ------------------------------------------------------------ Tuch, Fell, Talismane
        float wind = Mth.sin(t * 0.07f);
        for (int i = 0; i < this.hems.length; i++) {
            float w = t * 0.09f + i * 1.3f;
            this.hems[i].xRot += -Mth.abs(Mth.sin(w)) * 0.1f - speed * 0.25f * Mth.abs(Mth.sin(pos * 0.55f + i));
            this.hems[i].zRot += Mth.sin(w * 0.7f) * 0.05f;
        }
        for (int i = 0; i < this.rags.length; i++) {
            this.rags[i].xRot += -Mth.abs(Mth.sin(t * 0.06f + i * 0.9f)) * 0.08f - speed * 0.2f;
        }
        this.mantleSides[0].zRot -= Mth.abs(wind) * 0.05f;
        this.mantleSides[1].zRot += Mth.abs(wind) * 0.05f;
        this.cloak.xRot += 0.04f + wind * 0.04f + speed * 0.28f;
        this.cloakTip.xRot += Mth.sin(t * 0.07f - 1.0f) * 0.07f + speed * 0.12f;
        this.cloak.zRot += Mth.sin(t * 0.043f) * 0.03f;
        this.hoodTail.xRot += Mth.sin(t * 0.06f - 0.5f) * 0.06f + speed * 0.15f;
        this.leftRag.xRot += Mth.sin(t * 0.08f) * 0.1f + speed * 0.3f;
        this.rightRag.xRot += Mth.sin(t * 0.08f + 1.1f) * 0.1f + speed * 0.3f;
        for (int i = 0; i < this.charms.length; i++) {
            this.charms[i].zRot += Mth.sin(t * 0.1f + i * 1.7f) * 0.12f + step * 0.2f * speed;
            this.charms[i].xRot += Mth.sin(t * 0.08f + i) * 0.08f;
        }
        this.bell.zRot += Mth.sin(t * 0.09f) * 0.06f + step * 0.25f * speed;
        this.bell.xRot += Mth.sin(t * 0.07f + 0.6f) * 0.05f;

        // ------------------------------------------------------------ Andacht: tief gesenkter Kopf, die Glocke hängt still
        if (trance > 0.001f) {
            this.head.xRot += trance * (0.55f + Mth.sin(t * 0.03f) * 0.03f);
            this.chest.xRot += trance * 0.14f;
            this.body.xRot += trance * 0.04f;
            this.leftArm.xRot += trance * 0.18f;
            this.leftArm.zRot += trance * 0.08f;
            this.rightArm.xRot += trance * 0.1f;
            this.jaw.xRot -= trance * 0.25f;
        }

        // ------------------------------------------------------------ Gebärden der Angriffe
        switch (attack) {
            case AWAKEN -> this.awaken(a, t);
            case HOOK -> this.hook(a);
            case LANTERN -> this.lanternSwing(a);
            case WISPS -> this.wispsCall(a, t);
            case BELL -> this.ring(Anim.bump(a - 4.0f, 7.0f, 16.0f, 26.0f), swingAt(a, 14.0f));
            case FOG -> this.fog(a);
            case PEN -> this.pen(a);
            case CRASH -> this.crash(a);
            case STORM -> this.storm(a, t);
            case FURY -> this.fury(a, t);
            default -> { }
        }

        // ------------------------------------------------------------ Tod: Er sinkt in die Knie, der Kopf fällt, der Stab kippt
        if (state.deathTime > 0.0f) {
            float d = Mth.clamp(state.deathTime / 40.0f, 0.0f, 1.0f);
            float e = d * d * (3.0f - 2.0f * d);
            this.body.y += e * 6.0f;
            this.skirt.y += e * 4.0f;
            this.skirt.xScale = this.skirt.zScale = 1.0f + e * 0.25f;
            this.leftLeg.y += e * 4.0f;
            this.rightLeg.y += e * 4.0f;
            this.leftLeg.xRot -= e * 1.2f;
            this.rightLeg.xRot -= e * 1.2f;
            this.body.xRot += e * 0.25f;
            this.chest.xRot += e * 0.35f;
            this.head.xRot += e * 0.6f;
            this.jaw.xRot += e * 0.5f;
            this.leftArm.xRot += e * 0.4f;
            this.leftArm.zRot -= e * 0.25f;
            this.rightArm.zRot += e * 0.35f;
            this.crook.zRot += e * 0.5f;
        }

        // ------------------------------------------------------------ die Laterne hängt lotrecht und pendelt nach
        float chainX = this.body.xRot - this.body.getInitialPose().xRot() + this.chest.xRot - this.chest.getInitialPose().xRot()
                + this.rightArm.xRot - this.rightArm.getInitialPose().xRot() + this.rightFore.xRot - this.rightFore.getInitialPose().xRot()
                + this.crook.xRot - this.crook.getInitialPose().xRot();
        float chainZ = this.body.zRot + this.rightArm.zRot - this.rightArm.getInitialPose().zRot() + this.crook.zRot
                - this.crook.getInitialPose().zRot();
        float swing = Anim.follow(key, 1, chainX, t, 3.5f) - chainX;
        float swingZ = Anim.follow(key, 2, chainZ, t, 3.5f) - chainZ;
        this.lantern.xRot -= chainX - swing * 0.8f - Mth.sin(t * 0.08f) * 0.06f;
        this.lantern.zRot -= chainZ - swingZ * 0.8f - Mth.sin(t * 0.063f + 0.4f) * 0.07f;

        // ------------------------------------------------------------ Irrlichter kreisen
        this.orbitWisps(state, attack, a, t, trance, fury);
    }

    /** Drei Irrlichter auf schrägen Bahnen um ihn; beim Freilassen fliegen sie hinaus und kommen später zurück. */
    private void orbitWisps(SoulShepherdRenderState state, SoulShepherdEntity.Attack attack, float a, float t, float trance, float fury) {
        float out = 0.0f;
        float gone = 0.0f;
        if (attack == SoulShepherdEntity.Attack.WISPS) {
            out = Anim.bump(a, 10.0f, 12.0f, 13.0f);
            gone = a >= 12.0f && a < 36.0f ? 1.0f : a >= 36.0f ? Mth.clamp(1.0f - (a - 36.0f) / 8.0f, 0.0f, 1.0f) : 0.0f;
        }
        float storm = attack == SoulShepherdEntity.Attack.STORM ? Anim.bump(a, 10.0f, 60.0f, 72.0f) : 0.0f;
        boolean hidden = state.deathTime > 30.0f;
        for (int i = 0; i < this.wisps.length; i++) {
            ModelPart w = this.wisps[i];
            float speed = (0.045f + 0.02f * fury) * (1.0f - trance * 0.6f) + storm * 0.12f;
            float ang = t * speed + i * Mth.TWO_PI / 3.0f;
            float radius = 12.5f + Mth.sin(t * 0.05f + i * 2.0f) * 1.5f + out * 10.0f - trance * 2.0f + storm * 4.0f;
            float y = -2.0f + Mth.sin(t * 0.08f + i * 2.1f) * 3.0f + trance * 14.0f - storm * 4.0f + i * 2.0f;
            w.x = Mth.cos(ang) * radius;
            w.z = Mth.sin(ang) * radius;
            w.y = y;
            w.yRot = -ang;
            w.zRot = Mth.sin(t * 0.2f + i) * 0.3f;
            float s = (1.0f - gone) * (0.9f + 0.12f * Mth.sin(t * 0.4f + i * 1.3f));
            w.xScale = w.yScale = w.zScale = Math.max(0.0f, s);
            w.visible = !hidden && s > 0.02f;
        }
    }

    /** Erwachen: Der Kopf hebt sich, er reckt die Laterne empor, dann hebt er die Glocke und läutet (Tick 44). */
    private void awaken(float a, float t) {
        float rise = Anim.bump(a, 24.0f, 60.0f, 72.0f);
        float lift = Anim.bump(a - 14.0f, 14.0f, 30.0f, 52.0f);
        this.head.xRot -= rise * 0.62f;
        this.chest.xRot -= rise * 0.16f;
        this.rightArm.xRot -= lift * 0.85f;
        this.rightArm.zRot += lift * 0.12f;
        this.jaw.xRot += Anim.bump(a - 20.0f, 6.0f, 12.0f, 22.0f) * 0.45f;
        this.ring(Anim.bump(a - 34.0f, 7.0f, 16.0f, 26.0f), swingAt(a, 44.0f));
    }

    /** Hakenzug: ausholen, den Haken vorschnellen lassen, halten – und mit einem Ruck zurückreißen (Schlag bei Tick 24). */
    private void hook(float a) {
        float wind = Anim.bump(a, 6.0f, 9.0f, 12.0f);
        float thrust = Anim.bump(a - 9.0f, 3.0f, 12.0f, 16.0f);
        float yank = Anim.bump(a - 21.0f, 2.0f, 6.0f, 16.0f);
        this.rightArm.xRot += wind * 0.4f - thrust * 1.35f - yank * 0.9f;
        this.rightFore.xRot += wind * 0.3f + thrust * 0.55f - yank * 0.2f;
        this.crook.xRot -= thrust * 0.9f - yank * 0.7f;
        this.chest.yRot -= wind * 0.25f - thrust * 0.2f;
        this.chest.xRot += thrust * 0.18f - yank * 0.12f;
        this.leftArm.xRot -= thrust * 0.3f;
        this.jaw.xRot += yank * 0.35f;
    }

    /** Laternenschwung: Die Laterne über die rechte Schulter zurück – und in einem weiten Bogen nach links (Tick 14). */
    private void lanternSwing(float a) {
        float back = Anim.bump(a, 7.0f, 10.0f, 13.0f);
        float sweep = Mth.clamp((a - 11.0f) / 6.0f, 0.0f, 1.0f);
        float arc = Anim.bump(a - 10.0f, 2.0f, 8.0f, 18.0f);
        float yaw = Mth.lerp(sweep * sweep * (3.0f - 2.0f * sweep), -0.9f, 1.0f);
        this.rightArm.xRot += -back * 1.5f - arc * 1.2f;
        this.rightArm.yRot += back * -0.7f + arc * yaw;
        this.rightArm.zRot += back * 0.3f;
        this.crook.xRot -= arc * 0.6f;
        this.chest.yRot += back * -0.35f + arc * yaw * 0.45f;
        this.body.yRot += arc * yaw * 0.15f;
        this.leftArm.zRot -= arc * 0.5f;
    }

    /** Irrlichter: Er reckt die Laterne hoch über den Kopf und breitet den anderen Arm aus. */
    private void wispsCall(float a, float t) {
        float w = Anim.bump(a, 8.0f, 34.0f, 44.0f);
        this.rightArm.xRot -= w * 1.25f;
        this.rightArm.zRot += w * 0.15f;
        this.rightFore.xRot += w * 0.35f;
        this.leftArm.xRot -= w * 0.5f;
        this.leftArm.zRot -= w * (0.9f + Mth.sin(t * 0.2f) * 0.05f);
        this.head.xRot -= w * 0.35f;
        this.chest.xRot -= w * 0.12f;
    }

    /** Der Schwung der Glocke nach einem Schlag im Tick {@code at}: heftig, dann klingt er aus. */
    private static float swingAt(float a, float at) {
        return a >= at ? Mth.sin((a - at) * 1.25f) * Mth.clamp(1.0f - (a - at) / 14.0f, 0.0f, 1.0f) : 0.0f;
    }

    /** Glocke läuten: die Handglocke hoch ({@code raise} 0..1) und der Schwung der Schläge. */
    private void ring(float raise, float swing) {
        this.leftArm.xRot -= raise * 2.1f;
        this.leftArm.zRot -= raise * 0.25f;
        this.leftFore.xRot += raise * 0.4f;
        this.bell.zRot += swing * 0.75f * raise;
        this.bell.xRot += raise * 1.6f - swing * 0.3f * raise;
        this.chest.xRot -= raise * 0.1f;
        this.chest.zRot += swing * 0.05f * raise;
        this.head.xRot -= raise * 0.15f;
    }

    /** Nebelgang: Er duckt sich in den Umhang (dann ist er fort) und taucht hinter dem Ziel geduckt auf – und schlägt zu. */
    private void fog(float a) {
        float duck = Anim.bump(a, 7.0f, 9.0f, 10.0f) + Anim.bump(a - 20.0f, 0.5f, 2.0f, 6.0f);
        float strike = Anim.bump(a - 21.0f, 3.0f, 5.0f, 12.0f);
        this.chest.xRot += duck * 0.45f;
        this.head.xRot += duck * 0.3f;
        this.leftArm.xRot -= duck * 0.6f;
        this.leftArm.zRot += duck * 0.4f;
        this.rightArm.xRot -= strike * 1.9f;
        this.crook.xRot -= strike * 0.7f;
        this.chest.xRot += strike * 0.2f;
    }

    /** Pferch: Mit der Laterne zieht er einen Kreis in die Luft, dann weist der Haken auf das Ziel (Feuer bei Tick 22). */
    private void pen(float a) {
        float circle = Anim.bump(a, 5.0f, 18.0f, 22.0f);
        float point = Anim.bump(a - 18.0f, 4.0f, 14.0f, 22.0f);
        float c = a * 0.5f;
        this.rightArm.xRot += circle * (-1.2f + Mth.sin(c) * 0.35f) - point * 1.25f;
        this.rightArm.zRot += circle * (0.2f + Mth.cos(c) * 0.35f);
        this.crook.xRot -= point * 0.75f;
        this.leftArm.xRot -= point * 0.4f;
        this.leftArm.zRot -= circle * 0.5f;
        this.head.xRot += point * 0.1f;
    }

    /** Hirtenstoß: den Stab hoch über den Kopf – und mit aller Wucht vor ihm in den Boden (Tick 16). */
    private void crash(float a) {
        float raise = Anim.bump(a, 8.0f, 13.0f, 16.0f);
        float slam = Anim.bump(a - 14.0f, 2.0f, 10.0f, 18.0f);
        this.rightArm.xRot += -raise * 2.6f - slam * 1.25f;
        this.rightArm.zRot -= raise * 0.25f;
        this.crook.xRot += raise * 0.4f - slam * 1.15f;
        this.leftArm.xRot -= raise * 2.2f + slam * 0.9f;
        this.leftArm.zRot += raise * 0.35f;
        this.chest.xRot += -raise * 0.2f + slam * 0.42f;
        this.body.xRot += slam * 0.18f;
        this.head.xRot += slam * 0.2f;
    }

    /** Seelensturm: die Arme weit ausgebreitet, der Schädel zurückgelegt; er wiegt sich, die Laterne kreist. */
    private void storm(float a, float t) {
        float w = Anim.bump(a, 10.0f, 60.0f, 72.0f);
        float c = t * 0.35f;
        this.leftArm.zRot -= w * 1.25f;
        this.leftArm.xRot -= w * 0.3f;
        this.rightArm.zRot += w * 1.0f;
        this.rightArm.xRot -= w * 0.45f;
        this.crook.zRot -= w * 0.6f;
        this.head.xRot -= w * 0.45f;
        this.chest.xRot -= w * 0.18f;
        this.body.yRot += Mth.sin(t * 0.12f) * 0.25f * w;
        this.lantern.zRot += Mth.sin(c) * 0.5f * w;
        this.lantern.xRot += Mth.cos(c) * 0.5f * w;
        this.jaw.xRot += w * 0.3f;
        for (ModelPart hem : this.hems) {
            hem.xRot -= w * 0.35f;
        }
        this.cloak.xRot += w * 0.35f;
    }

    /** Zorn: der Schädel fliegt zurück, das Maul reißt auf – dann läutet er dreimal (Ticks 10, 26, 42). */
    private void fury(float a, float t) {
        float roar = Anim.bump(a, 4.0f, 8.0f, 14.0f);
        float shake = Mth.sin(t * 2.1f) * 0.04f * roar;
        this.head.xRot -= roar * 0.7f + shake;
        this.jaw.xRot += roar * 0.65f;
        this.chest.xRot -= roar * 0.25f;
        this.rightArm.zRot += roar * 0.5f;
        this.leftArm.zRot -= roar * 0.6f;
        this.ring(Anim.bump(a - 2.0f, 6.0f, 46.0f, 54.0f), swingAt(a, 10.0f) + swingAt(a, 26.0f) + swingAt(a, 42.0f));
    }
}
