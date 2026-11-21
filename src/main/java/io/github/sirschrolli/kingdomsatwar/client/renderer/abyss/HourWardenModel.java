package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.HourWardenEntity;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Der Stundenwächter: Sein Uhrwerk läuft – die Zeiger auf dem Zifferblatt hinter seinem Kopf rücken vor, die Zahnräder in
 * Brust, Schultern und Knien drehen sich, das Pendel schwingt, die Uhrgewichte pendeln, drei Zahnräder kreisen um ihn. Er
 * schreitet schwer. Erstarrt steht er still, die Klingen vor sich in den Boden gestützt, der Kopf gesenkt; erwachend hebt er
 * den Kopf und reißt die Klingen hoch. Jeder Angriff hat seine Gebärde: weit ausholen und quer hauen (Zeigerhieb), beide
 * Klingen über den Kopf und hinab (Stundenschlag), hinter sich greifen und schleudern (Zahnradwurf), die linke Hand heben
 * (Zeitstopp), mit der Klinge nach oben weisen und mitschwingen (Pendelschwung), die Stundenklinge in den Boden stoßen
 * (Sanduhr), sich ducken und von hinten zustoßen (Zeitsprung), niederknien (Rückspulen), taumeln, beide Klingen gekreuzt
 * hochreißen (Die Zwölfte Stunde) und bei jedem Glockenschlag nachfedern. Im Sterben sinkt er auf die Knie und sackt vornüber.
 *
 * <p>Alle Haltungen werden zur Ruhehaltung des Modells addiert (die Klingen zeigen darin schräg nach vorn unten).
 */
public class HourWardenModel extends EntityModel<HourWardenRenderState> {
    private final ModelPart legL;
    private final ModelPart legR;
    private final ModelPart shinL;
    private final ModelPart shinR;
    private final ModelPart kneeGearL;
    private final ModelPart kneeGearR;
    private final ModelPart hips;
    private final ModelPart pendulum;
    private final ModelPart weightL;
    private final ModelPart weightR;
    private final ModelPart[] tassets;
    private final ModelPart body;
    private final ModelPart chest;
    private final ModelPart gearBig;
    private final ModelPart gearSmall;
    private final ModelPart head;
    private final ModelPart crest;
    private final ModelPart dial;
    private final ModelPart handHour;
    private final ModelPart handMinute;
    private final ModelPart pgearL;
    private final ModelPart pgearR;
    private final ModelPart armL;
    private final ModelPart armR;
    private final ModelPart foreL;
    private final ModelPart foreR;
    private final ModelPart hourBlade;
    private final ModelPart minuteBlade;
    private final ModelPart[] orbits;

    public HourWardenModel(ModelPart root) {
        super(root);
        this.legL = root.getChild("leg_l");
        this.legR = root.getChild("leg_r");
        this.shinL = this.legL.getChild("shin_l");
        this.shinR = this.legR.getChild("shin_r");
        this.kneeGearL = this.shinL.getChild("knee_gear_l");
        this.kneeGearR = this.shinR.getChild("knee_gear_r");
        this.hips = root.getChild("hips");
        this.pendulum = this.hips.getChild("pendulum");
        this.weightL = this.hips.getChild("weight_l");
        this.weightR = this.hips.getChild("weight_r");
        this.tassets = new ModelPart[] {this.hips.getChild("tasset_fl"), this.hips.getChild("tasset_fr"), this.hips.getChild("tasset_bl"),
                this.hips.getChild("tasset_br"), this.hips.getChild("tasset_l"), this.hips.getChild("tasset_r")};
        this.body = root.getChild("body");
        this.chest = this.body.getChild("chest");
        this.gearBig = this.chest.getChild("gear_big");
        this.gearSmall = this.chest.getChild("gear_small");
        this.head = this.chest.getChild("head");
        this.crest = this.head.getChild("crest");
        this.dial = this.chest.getChild("dial");
        this.handHour = this.dial.getChild("hand_hour");
        this.handMinute = this.dial.getChild("hand_minute");
        this.pgearL = this.chest.getChild("pauldron_l").getChild("pgear_l");
        this.pgearR = this.chest.getChild("pauldron_r").getChild("pgear_r");
        this.armL = this.chest.getChild("arm_l");
        this.armR = this.chest.getChild("arm_r");
        this.foreL = this.armL.getChild("fore_l");
        this.foreR = this.armR.getChild("fore_r");
        this.hourBlade = this.foreL.getChild("hour_blade");
        this.minuteBlade = this.foreR.getChild("minute_blade");
        this.orbits = new ModelPart[] {root.getChild("orbit_0"), root.getChild("orbit_1"), root.getChild("orbit_2")};
    }

    private static HourWardenEntity.Attack attack(HourWardenRenderState state) {
        return HourWardenEntity.Attack.values()[Mth.clamp(state.attack, 0, HourWardenEntity.Attack.values().length - 1)];
    }

    @Override
    public void setupAnim(HourWardenRenderState state) {
        super.setupAnim(state);
        float t = state.ageInTicks;
        HourWardenEntity.Attack attack = attack(state);
        float a = state.attackTime;
        Object key = state.owner != null ? state.owner : this;
        float frozen = Anim.follow(key, 0, attack == HourWardenEntity.Attack.DORMANT ? 1.0f : 0.0f, t, 2.5f);
        float live = 1.0f - frozen;
        float speed = Math.min(state.walkAnimationSpeed, 1.0f);
        float pos = state.walkAnimationPos;
        float g = state.gears * Anim.DEG;

        // ------------------------------------------------------------ das Uhrwerk: Zeiger, Zahnräder, Pendel, Gewichte
        float minute = state.minute;
        if (state.deathTime > 0.0f) {
            // im Sterben fallen die Zeiger nach unten (halb sieben)
            float k = Mth.clamp((state.deathTime - 30.0f) / 20.0f, 0.0f, 1.0f);
            minute = Mth.lerp(k * k, minute, 180.0f);
        }
        this.handMinute.zRot = minute * Anim.DEG;
        this.handHour.zRot = (-0.5f + (minute + 6.0f) / 12.0f) * Anim.DEG;
        if (state.deathTime > 0.0f) {
            this.handHour.zRot = Mth.lerp(Mth.clamp((state.deathTime - 34.0f) / 20.0f, 0.0f, 1.0f), this.handHour.zRot, 170.0f * Anim.DEG);
        }
        this.gearBig.zRot = g;
        this.gearSmall.zRot = -g * 1.8f;
        this.pgearL.xRot = g * 0.8f;
        this.pgearR.xRot = -g * 0.8f;
        this.kneeGearL.zRot = g * 1.4f;
        this.kneeGearR.zRot = -g * 1.4f;
        float swing = Mth.sin(state.gears * 0.035f) * 0.35f * live;
        this.pendulum.zRot = swing;
        this.weightL.zRot = Mth.sin(t * 0.07f) * 0.06f * live - speed * Mth.cos(pos * 0.5f) * 0.12f;
        this.weightR.zRot = Mth.sin(t * 0.07f + 1.2f) * 0.06f * live + speed * Mth.cos(pos * 0.5f) * 0.12f;
        this.weightL.xRot = -speed * 0.2f;
        this.weightR.xRot = -speed * 0.2f;
        this.crest.zRot = 0.0f;

        // drei Zahnräder kreisen um ihn (erstarrt hängen sie still in der Luft)
        for (int i = 0; i < 3; i++) {
            float ang = (live * t * 0.045f) + i * Mth.TWO_PI / 3.0f + (attack == HourWardenEntity.Attack.REWIND ? -a * 0.25f : 0.0f);
            float r = 15.0f + Mth.sin(t * 0.03f + i) * 1.5f * live;
            this.orbits[i].x = Mth.cos(ang) * r;
            this.orbits[i].z = Mth.sin(ang) * r;
            this.orbits[i].y = -3.0f - i * 3.5f + Mth.sin(t * 0.05f + i * 1.7f) * 1.5f * live;
            this.orbits[i].yRot = -ang;
            this.orbits[i].zRot = g * (1.2f + i * 0.3f);
        }

        // ------------------------------------------------------------ Atem, Blick, Gehen
        float breath = Mth.sin(t * 0.06f) * live;
        this.chest.xRot += breath * 0.02f;
        this.head.xRot += -breath * 0.015f;
        this.head.yRot += Mth.clamp(state.yRot, -45.0f, 45.0f) * Anim.DEG * live;
        this.head.xRot += Mth.clamp(state.xRot, -30.0f, 30.0f) * Anim.DEG * 0.6f * live;
        float step = Mth.cos(pos * 0.5f);
        this.legL.xRot += step * 0.6f * speed;
        this.legR.xRot -= step * 0.6f * speed;
        this.shinL.xRot += Math.max(0.0f, -step) * 0.5f * speed;
        this.shinR.xRot += Math.max(0.0f, step) * 0.5f * speed;
        float bob = Mth.abs(Mth.sin(pos * 0.5f)) * 1.1f * speed;
        this.body.y += bob;
        this.hips.y += bob;
        this.body.zRot += step * 0.04f * speed;
        this.armL.xRot -= step * 0.25f * speed;
        this.armR.xRot += step * 0.25f * speed;
        for (ModelPart tasset : this.tassets) {
            tasset.xRot += -speed * 0.12f;
        }

        // ------------------------------------------------------------ erstarrt: auf die Klingen gestützt, Kopf gesenkt
        if (frozen > 0.001f) {
            this.rest(frozen);
        }

        // ------------------------------------------------------------ die Angriffe
        switch (attack) {
            case AWAKEN -> this.awaken(a);
            case SWEEP -> this.sweep(a, state.enraged);
            case STRIKE -> this.strike(a);
            case GEARS -> this.gears(a);
            case TIME_STOP -> this.timeStop(a);
            case PENDULUM -> this.pendulumPose(a);
            case HOURGLASS -> this.hourglass(a);
            case BLINK -> this.blink(a);
            case REWIND -> this.kneel(Anim.bump(a, 12.0f, 72.0f, 84.0f), a);
            case STAGGER -> this.stagger(a, t);
            case FURY -> this.fury(Anim.bump(a, 10.0f, 30.0f, 31.0f));
            case TOLLS -> this.tolls(a);
            default -> { }
        }
        if (state.enraged && attack != HourWardenEntity.Attack.DORMANT) {
            this.head.zRot += Mth.sin(t * 0.8f) * 0.015f;
        }

        // ------------------------------------------------------------ Sterben: auf die Knie, dann vornüber
        if (state.deathTime > 0.0f) {
            float kneel = Mth.clamp(state.deathTime / 24.0f, 0.0f, 1.0f);
            this.kneel(kneel, 0.0f);
            float slump = Mth.clamp((state.deathTime - 40.0f) / 30.0f, 0.0f, 1.0f);
            this.body.xRot += slump * 0.55f;
            this.head.xRot += slump * 0.5f;
            this.armL.xRot += slump * 0.4f;
            this.armR.xRot += slump * 0.4f;
            this.minuteBlade.xRot += slump * 0.5f;
            this.hourBlade.xRot += slump * 0.5f;
        }
    }

    // =====================================================================================
    // Haltungen
    // =====================================================================================

    /** Erstarrt: beide Hände vor dem Bauch auf den Griffen, die Klingen senkrecht in den Boden gestützt, der Kopf gesenkt. */
    private void rest(float k) {
        this.armR.xRot += -0.55f * k;
        this.armR.yRot += 0.35f * k;
        this.armR.zRot += -0.15f * k;
        this.armL.xRot += -0.55f * k;
        this.armL.yRot += -0.35f * k;
        this.armL.zRot += 0.15f * k;
        this.foreR.xRot += -0.5f * k;
        this.foreL.xRot += -0.5f * k;
        // die Klingen zeigen senkrecht nach unten (Spitze auf dem Zifferblatt)
        this.minuteBlade.xRot += 0.55f * k;
        this.hourBlade.xRot += 0.55f * k;
        this.head.xRot += 0.35f * k;
        this.chest.xRot += 0.08f * k;
    }

    /** Erwachen: der Kopf hebt sich (die Augen glimmen auf), beim Glockenschlag reißt er die Klingen hoch und geht in Stellung. */
    private void awaken(float a) {
        float lift = Mth.clamp((a - 8.0f) / 26.0f, 0.0f, 1.0f);
        float rest = 1.0f - Mth.clamp((a - 34.0f) / 8.0f, 0.0f, 1.0f);
        this.rest(rest);
        this.head.xRot += -0.35f * lift * rest;
        // die Klingen hoch: kurz beide über den Kopf, dann in die Kampfstellung
        float up = Anim.bump(a - 34.0f, 6.0f, 14.0f, 30.0f);
        this.armR.xRot += -2.4f * up;
        this.armL.xRot += -2.4f * up;
        this.armR.zRot += 0.3f * up;
        this.armL.zRot += -0.3f * up;
        this.minuteBlade.xRot += 0.35f * up;
        this.hourBlade.xRot += 0.35f * up;
        this.head.xRot += -0.25f * up;
        this.chest.xRot += -0.12f * up;
    }

    /** Zeigerhieb: weit nach rechts ausholen, quer vor sich hauen; im Zorn schlägt der Stundenzeiger rückhand nach. */
    private void sweep(float a, boolean enraged) {
        float wind = Anim.bump(a, 9.0f, 10.0f, 13.0f);
        float strike = Mth.clamp((a - 10.0f) / 3.0f, 0.0f, 1.0f) * (1.0f - Mth.clamp((a - 18.0f) / 10.0f, 0.0f, 1.0f));
        float raised = Anim.bump(a, 7.0f, 16.0f, 26.0f);
        this.armR.xRot += -1.45f * raised;
        this.armR.yRot += 1.25f * wind - 0.9f * strike;
        this.foreR.xRot += 0.45f * raised;
        this.minuteBlade.xRot += 0.35f * raised;
        this.chest.yRot += 0.45f * wind - 0.45f * strike;
        this.body.yRot += 0.15f * wind - 0.2f * strike;
        if (enraged) {
            float back = Anim.bump(a - 16.0f, 4.0f, 6.0f, 11.0f);
            this.armL.xRot += -1.4f * back;
            this.armL.yRot += -1.0f * back + 1.1f * Mth.clamp((a - 19.0f) / 3.0f, 0.0f, 1.0f) * back;
            this.foreL.xRot += 0.45f * back;
            this.hourBlade.xRot += 0.35f * back;
        }
    }

    /** Stundenschlag: beide Klingen hoch über den Kopf, dann mit aller Kraft hinab ins Zifferblatt. */
    private void strike(float a) {
        float up = Mth.clamp(a / 16.0f, 0.0f, 1.0f) * (1.0f - Mth.clamp((a - 18.0f) / 3.0f, 0.0f, 1.0f));
        float down = Mth.clamp((a - 18.0f) / 3.0f, 0.0f, 1.0f) * (1.0f - Mth.clamp((a - 32.0f) / 12.0f, 0.0f, 1.0f));
        this.armR.xRot += -2.9f * up - 1.2f * down;
        this.armL.xRot += -2.9f * up - 1.2f * down;
        this.armR.yRot += 0.25f * (up + down);
        this.armL.yRot += -0.25f * (up + down);
        this.foreR.xRot += 0.3f * up + 0.4f * down;
        this.foreL.xRot += 0.3f * up + 0.4f * down;
        this.minuteBlade.xRot += 0.35f * (up + down);
        this.hourBlade.xRot += 0.35f * (up + down);
        this.chest.xRot += -0.25f * up + 0.4f * down;
        this.head.xRot += -0.2f * up + 0.2f * down;
        this.body.xRot += 0.15f * down;
        this.legL.xRot += -0.25f * down;
        this.legR.xRot += 0.2f * down;
        this.body.y += 1.5f * down;
        this.hips.y += 1.5f * down;
    }

    /** Zahnradwurf: hinter sich greifen und schleudern – rechts, links, rechts. */
    private void gears(float a) {
        for (int k = 0; k < 3; k++) {
            float at = 14.0f + k * 5.0f;
            float reach = Anim.bump(a - (at - 7.0f), 5.0f, 6.0f, 7.0f);
            float throwing = Anim.bump(a - at, 1.0f, 2.0f, 6.0f);
            ModelPart arm = k == 1 ? this.armL : this.armR;
            float side = k == 1 ? -1.0f : 1.0f;
            arm.xRot += 0.9f * reach - 1.7f * throwing;
            arm.zRot += 0.3f * side * reach;
            this.chest.yRot += 0.3f * side * reach - 0.3f * side * throwing;
        }
    }

    /** Zeitstopp: die linke Hand hoch erhoben (die Uhr tickt schneller), dann – die Zeit steht – ein Hieb mit dem Minutenzeiger. */
    private void timeStop(float a) {
        float raise = Anim.bump(a, 12.0f, 42.0f, 52.0f);
        this.armL.xRot += -2.7f * raise;
        this.armL.zRot += -0.2f * raise;
        this.foreL.xRot += 0.4f * raise;
        this.hourBlade.xRot += 0.6f * raise;
        this.head.xRot += -0.2f * raise;
        float hold = Mth.clamp((a - HourWardenEntity.STOP_AT) / 2.0f, 0.0f, 1.0f) * (1.0f - Mth.clamp((a - 40.0f) / 4.0f, 0.0f, 1.0f));
        this.chest.xRot += -0.1f * hold;
        float strikeAt = HourWardenEntity.STOP_AT + 18.0f;
        float wind = Anim.bump(a - (strikeAt - 8.0f), 6.0f, 8.0f, 9.0f);
        float hit = Anim.bump(a - strikeAt, 1.0f, 3.0f, 12.0f);
        this.armR.xRot += -1.5f * (wind + hit);
        this.armR.yRot += 1.1f * wind - 0.8f * hit;
        this.minuteBlade.xRot += 0.35f * (wind + hit);
        this.chest.yRot += 0.35f * wind - 0.35f * hit;
    }

    /** Pendelschwung: Er weist mit dem Minutenzeiger nach oben und schwingt mit dem Pendel mit. */
    private void pendulumPose(float a) {
        float point = Anim.bump(a, 8.0f, 80.0f, 92.0f);
        this.armR.xRot += -2.8f * point;
        this.armR.zRot += 0.15f * point;
        this.foreR.xRot += 0.35f * point;
        this.minuteBlade.xRot += 0.4f * point;
        this.head.xRot += -0.35f * Anim.bump(a, 8.0f, 30.0f, 40.0f);
        if (a > 34.0f) {
            float s = Mth.cos((a - 34.0f) * Mth.PI / 20.0f) * point;
            this.armR.xRot += 0.25f * s;
            this.chest.xRot += -0.06f * s;
        }
    }

    /** Sanduhr: Er hebt den Stundenzeiger und stößt ihn senkrecht in den Boden. */
    private void hourglass(float a) {
        float raise = Anim.bump(a, 10.0f, 13.0f, 16.0f);
        float plant = Mth.clamp((a - 13.0f) / 3.0f, 0.0f, 1.0f) * (1.0f - Mth.clamp((a - 34.0f) / 10.0f, 0.0f, 1.0f));
        this.armL.xRot += -2.5f * raise - 0.6f * plant;
        this.foreL.xRot += 0.3f * raise - 0.4f * plant;
        this.hourBlade.xRot += 0.5f * raise + 0.65f * plant;
        this.chest.xRot += 0.25f * plant;
        this.head.xRot += 0.2f * plant;
        this.body.y += 1.5f * plant;
        this.hips.y += 1.5f * plant;
        this.legL.xRot += -0.35f * plant;
        this.shinL.xRot += 0.5f * plant;
        this.legR.xRot += 0.25f * plant;
    }

    /** Zeitsprung: Er duckt sich, vergeht – und stößt von hinten mit dem Minutenzeiger zu. */
    private void blink(float a) {
        float duck = Anim.bump(a, 5.0f, 7.0f, 10.0f);
        this.chest.xRot += 0.35f * duck;
        this.body.y += 1.5f * duck;
        this.hips.y += 1.5f * duck;
        float thrust = Anim.bump(a - 10.0f, 5.0f, 8.0f, 20.0f);
        this.armR.xRot += -1.55f * thrust;
        this.foreR.xRot += 0.45f * thrust;
        this.minuteBlade.xRot += 0.35f * thrust;
        this.chest.yRot += -0.3f * thrust;
        this.legR.xRot += -0.4f * thrust;
    }

    /** Auf die Knie (rechtes Bein vorn aufgestellt, linkes Knie am Boden); beim Rückspulen die Klingen gestützt, Kopf gesenkt. */
    private void kneel(float k, float a) {
        if (k <= 0.001f) {
            return;
        }
        float drop = 8.0f * k;
        this.body.y += drop;
        this.hips.y += drop;
        this.legL.y += drop;
        this.legR.y += drop;
        this.legR.xRot += -1.5f * k;
        this.shinR.xRot += 1.5f * k;
        this.legL.xRot += 0.05f * k;
        this.shinL.xRot += 1.45f * k;
        this.chest.xRot += 0.18f * k;
        if (a > 0.0f) {
            this.rest(k);
            this.head.xRot += 0.15f * k;
        }
    }

    /** Er taumelt: der Leib schwankt, der Kopf pendelt, die Arme hängen. */
    private void stagger(float a, float t) {
        float k = Anim.bump(a, 4.0f, 44.0f, 56.0f);
        this.body.zRot += Mth.sin(t * 0.35f) * 0.12f * k;
        this.body.xRot += 0.15f * k;
        this.head.zRot += Mth.sin(t * 0.5f + 1.0f) * 0.25f * k;
        this.head.xRot += 0.3f * k;
        this.armL.xRot += 0.4f * k;
        this.armR.xRot += 0.3f * k;
        this.armL.zRot += -0.3f * k;
        this.armR.zRot += 0.3f * k;
        this.legL.xRot += -0.2f * k;
        this.shinL.xRot += 0.4f * k;
        this.body.y += 1.0f * k;
        this.hips.y += 1.0f * k;
    }

    /** Die Zwölfte Stunde: beide Klingen gekreuzt hoch über dem Kopf, der Kopf in den Nacken. */
    private void fury(float k) {
        this.armR.xRot += -2.95f * k;
        this.armL.xRot += -2.95f * k;
        this.armR.yRot += -0.35f * k;
        this.armL.yRot += 0.35f * k;
        this.armR.zRot += -0.25f * k;
        this.armL.zRot += 0.25f * k;
        this.minuteBlade.xRot += 0.35f * k;
        this.hourBlade.xRot += 0.35f * k;
        this.head.xRot += -0.4f * k;
        this.chest.xRot += -0.18f * k;
    }

    /** Die zwölf Glockenschläge: die Klingen bleiben oben, bei jedem Schlag federt er nach. */
    private void tolls(float a) {
        float k = Anim.bump(a, 4.0f, 118.0f, 128.0f);
        this.fury(k);
        float since = (a - 6.0f) % HourWardenEntity.TOLL_GAP;
        float pulse = a >= 6.0f && a < 6.0f + 12 * HourWardenEntity.TOLL_GAP ? Anim.bump(since, 1.0f, 2.0f, 6.0f) : 0.0f;
        this.body.y += 1.2f * pulse * k;
        this.hips.y += 1.2f * pulse * k;
        this.armR.xRot += 0.35f * pulse * k;
        this.armL.xRot += 0.35f * pulse * k;
        this.head.xRot += 0.15f * pulse * k;
    }
}
