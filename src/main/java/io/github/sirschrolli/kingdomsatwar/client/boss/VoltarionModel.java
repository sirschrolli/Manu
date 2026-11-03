package io.github.sirschrolli.kingdomsatwar.client.boss;

import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Voltarions Modell: ein Wyvern – sechs Halsglieder, zwölf Schwanzglieder mit Blitzklinge, nur zwei Hinterbeine, die
 * Flügel sind zugleich die Vorderglieder.
 *
 * <p>Am Boden legt er die Flügel nicht auf den Rücken wie die anderen Drachen, sondern stützt sich auf die
 * Handgelenke: Der Oberarm zeigt nach außen und hinten, der Unterarm fällt schräg nach innen und vorn zum Boden (die
 * Ellbogen stehen seitlich ab wie bei einer Fledermaus), die angelegten Finger
 * ragen nach hinten oben, die Flughäute sind gerafft. Beim Gehen setzt er die Handgelenke im Wechsel mit den
 * Hinterbeinen (Viertakt: hinten links, vorn links, hinten rechts, vorn rechts). Die Haltung ist mit
 * tools/entitygen (Kriechhaltung) berechnet: ZYX-Winkel für den linken Flügel, der rechte ist gespiegelt.
 */
public class VoltarionModel extends DragonBossModel {
    /** Kriechhaltung (linker Flügel): Oberarm, Unterarm, Hand – x, y, z in Bogenmaß. */
    private static final float[] ARM = {-1.6519f, -0.8654f, 0.4519f};
    private static final float[] FORE = {-2.5316f, -1.2171f, 2.5612f};
    private static final float[] HAND = {0.3782f, 0.2775f, -2.6978f};
    /** Achse des Ellbogens im Raum des Oberarms (linker Flügel): Knicken hebt das Handgelenk an. */
    private static final float[] ELBOW = {0.0f, -0.9801f, 0.1984f};

    private final Quaternionf[] armCrawl = new Quaternionf[2];
    private final Quaternionf[] foreCrawl = new Quaternionf[2];
    private final Quaternionf[] handCrawl = new Quaternionf[2];
    private final @Nullable ModelPart[] webArm = new ModelPart[2];
    private final @Nullable ModelPart[] webFore = new ModelPart[2];
    private final Quaternionf work = new Quaternionf();
    private final Quaternionf work2 = new Quaternionf();
    private final Vector3f euler = new Vector3f();

    public VoltarionModel(ModelPart root) {
        super(root, 6, 12, false, 3, 34.0f);
        Function<String, @Nullable ModelPart> part = root.createPartLookup();
        String[] sides = {"l", "r"};
        for (int side = 0; side < 2; side++) {
            float sx = side == 0 ? 1.0f : -1.0f;
            this.armCrawl[side] = mirrored(ARM, sx);
            this.foreCrawl[side] = mirrored(FORE, sx);
            this.handCrawl[side] = mirrored(HAND, sx);
            this.webArm[side] = part.apply("wing_" + sides[side] + "_web");
            this.webFore[side] = part.apply("wing_" + sides[side] + "_fore_web");
        }
    }

    private static Quaternionf mirrored(float[] e, float sx) {
        return new Quaternionf().rotationZYX(e[2] * sx, e[1] * sx, e[0]);
    }

    @Override
    public DragonMotion createMotion() {
        return new VoltarionMotion(this.neck.length, this.tail.length, false, this.bodyHeight);
    }

    @Override
    protected void foldWing(BossRenderState s, Wing w, int side) {
        float sx = w.sx();
        float fa = s.foldArm[side];
        float ff = s.foldFore[side];
        float fh = s.foldHand[side];
        float[] gait = s.legs[side == 0 ? DragonPose.FRONT_L : DragonPose.FRONT_R];
        // Oberarm: Kriechhaltung, beim Gehen um die Querachse der Schulter geschwungen
        this.work.identity().slerp(this.armCrawl[side], fa);
        this.work2.rotationX(gait[DragonPose.UPPER_X] * fa).mul(this.work);
        add(w.arm(), this.work2, this.euler);
        // Unterarm: Kriechhaltung, der Ellbogen knickt ein und hebt das Handgelenk vom Boden
        this.work.identity().slerp(this.foreCrawl[side], ff);
        this.work2.rotationAxis(gait[DragonPose.SHIN_X] * ff, ELBOW[0], ELBOW[1] * sx, ELBOW[2] * sx).mul(this.work);
        add(w.fore(), this.work2, this.euler);
        // Hand: Daumenkralle in den Boden, die Finger nach hinten oben angelegt
        this.work.identity().slerp(this.handCrawl[side], fh);
        add(w.hand(), this.work, this.euler);
        // Flughäute raffen (eigene Knochen – das Skelett bleibt unverzerrt)
        ModelPart arm = this.webArm[side];
        if (arm != null) {
            arm.zScale *= 1.0f - 0.85f * fa;
        }
        ModelPart fore = this.webFore[side];
        if (fore != null) {
            fore.zScale *= 1.0f - 0.85f * ff;
        }
        this.foldFingers(s, w, fh);
    }

    private static void add(ModelPart part, Quaternionf q, Vector3f euler) {
        q.getEulerAnglesZYX(euler);
        part.xRot += euler.x;
        part.yRot += euler.y;
        part.zRot += euler.z;
    }
}
