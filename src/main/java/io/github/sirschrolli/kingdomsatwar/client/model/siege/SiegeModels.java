package io.github.sirschrolli.kingdomsatwar.client.model.siege;

import io.github.sirschrolli.kingdomsatwar.siege.SiegePoses;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.util.Mth;

/**
 * Die Belagerungswaffen als bewegte Modelle (Geometrie aus tools/generate_siege.py): Wurfarm, Schlinge und Gegengewicht,
 * Bogenarme und Sehnen, Widder und Räder folgen {@link SiegePoses} – dieselben Bewegungen, nach denen der Server das
 * Geschoss abschickt.
 */
public final class SiegeModels {
    /** Die kleinen Räder (Balliste, Tribok) drehen sich bei gleicher Fahrt anderthalbmal so schnell wie die großen. */
    private static final float SMALL_WHEEL = 0.375f / 0.25f;
    /** Der Brocken in der Schlinge des Triboks (und im Flug). */
    public static final float TREBUCHET_STONE_SCALE = 1.9f;

    private SiegeModels() {}

    /** Was die Modelle zum Zeichnen brauchen. */
    public static class State extends EntityRenderState {
        public SiegeType siege = SiegeType.CATAPULT;
        public float yRot;
        /** Ticks seit dem letzten Schuss. */
        public float since = 100_000.0f;
        /** Liegt ein Geschoss bereit (oder ist es gerade noch im Löffel)? */
        public boolean loaded;
        /** Neigung der Balliste (Grad, positiv = nach oben). */
        public float aim;
        public float wheel;
        public float hurt;
        public float health = 1.0f;
        /** Belagerungsturm: Winkel der Fallbrücke (Grad ab senkrecht). */
        public float bridge;
        /** Planwagen: ist etwas geladen? */
        public boolean cargo;
    }

    // =====================================================================================
    // Katapult
    // =====================================================================================

    public static final class Catapult extends Model<State> {
        private final ModelPart arm;
        private final ModelPart stone;
        private final ModelPart winch;
        private final ModelPart[] wheels;

        public Catapult(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            ModelPart frame = root.getChild("frame");
            this.arm = frame.getChild("arm");
            this.stone = this.arm.getChild("cup").getChild("stone");
            this.winch = frame.getChild("winch");
            this.wheels = new ModelPart[] {root.getChild("wheel_fl"), root.getChild("wheel_fr"), root.getChild("wheel_bl"), root.getChild("wheel_br")};
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            this.arm.xRot = SiegePoses.catapultArm(s.since, SiegeType.CATAPULT.reload());
            // die Winde dreht sich, während sie den Arm zurückzieht
            this.winch.xRot = this.arm.xRot * 4.0f;
            this.stone.visible = s.loaded;
            for (ModelPart wheel : this.wheels) {
                wheel.xRot = s.wheel;
            }
        }
    }

    // =====================================================================================
    // Tribok
    // =====================================================================================

    public static final class Trebuchet extends Model<State> {
        private final ModelPart arm;
        private final ModelPart counterweight;
        private final ModelPart sling;
        private final ModelPart stone;
        private final ModelPart winch;
        private final ModelPart[] wheels;

        public Trebuchet(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            ModelPart base = root.getChild("base");
            this.arm = base.getChild("arm");
            this.counterweight = this.arm.getChild("counterweight");
            this.sling = this.arm.getChild("sling");
            this.stone = this.sling.getChild("stone");
            this.winch = base.getChild("winch");
            this.wheels = new ModelPart[] {base.getChild("base_wheel_fl"), base.getChild("base_wheel_fr"), base.getChild("base_wheel_bl"),
                    base.getChild("base_wheel_br")};
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            int reload = SiegeType.TREBUCHET.reload();
            float arm = SiegePoses.trebuchetArm(s.since, reload);
            this.arm.xRot = arm;
            // das Gegengewicht hängt immer senkrecht, die Schlinge schwingt in der Welt
            this.counterweight.xRot = -arm;
            this.sling.xRot = SiegePoses.trebuchetSling(s.since, reload) - arm;
            // ein schwerer Brocken (zwei Steinkugeln), der tief im Beutel liegt
            this.stone.visible = s.loaded;
            this.stone.y = SiegePoses.TREBUCHET_STONE;
            this.stone.xScale = TREBUCHET_STONE_SCALE;
            this.stone.yScale = TREBUCHET_STONE_SCALE;
            this.stone.zScale = TREBUCHET_STONE_SCALE;
            this.winch.xRot = -arm * 3.0f;
            for (ModelPart wheel : this.wheels) {
                wheel.xRot = s.wheel * SMALL_WHEEL;
            }
        }
    }

    // =====================================================================================
    // Balliste
    // =====================================================================================

    public static final class Ballista extends Model<State> {
        /** Drehpunkt der Bogenarme und Sehnenspitze am Arm (Modell-Einheiten, im Bogen). */
        private static final float ARM_X = 4.5f;
        private static final float ARM_Z = -10.5f;
        private static final float TIP_X = 11.0368f;
        private static final float TIP_Z = 5.8684f;
        /** Die Klaue hält die gespannte Sehne hier. */
        private static final float CLAW_Z = 6.5f;
        private static final float CORD_LENGTH = 19.12f;
        private static final float CORD_YAW = 0.9491f;

        private final ModelPart bow;
        private final ModelPart leftArm;
        private final ModelPart rightArm;
        private final ModelPart leftString;
        private final ModelPart rightString;
        private final ModelPart leftCord;
        private final ModelPart rightCord;
        private final ModelPart bolt;
        private final ModelPart winch;
        private final ModelPart[] wheels;

        public Ballista(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            ModelPart stand = root.getChild("stand");
            this.bow = stand.getChild("bow");
            this.leftArm = this.bow.getChild("left_arm");
            this.rightArm = this.bow.getChild("right_arm");
            this.leftString = this.leftArm.getChild("string_left");
            this.rightString = this.rightArm.getChild("string_right");
            this.leftCord = this.leftString.getChild("string_left_cord");
            this.rightCord = this.rightString.getChild("string_right_cord");
            this.bolt = this.bow.getChild("bolt");
            this.winch = this.bow.getChild("winch");
            this.wheels = new ModelPart[] {stand.getChild("stand_wheel_fl"), stand.getChild("stand_wheel_fr"), stand.getChild("stand_wheel_bl"),
                    stand.getChild("stand_wheel_br")};
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            int reload = SiegeType.BALLISTA.reload();
            this.bow.xRot = -Mth.clamp(s.aim, -16.0f, 20.0f) * Mth.DEG_TO_RAD;
            float delta = SiegePoses.ballistaArms(s.since, reload);
            float draw = SiegePoses.ballistaDraw(s.since, reload);
            this.leftArm.yRot = delta;
            this.rightArm.yRot = -delta;
            string(this.leftString, this.leftCord, delta, draw, 1.0f);
            string(this.rightString, this.rightCord, -delta, draw, -1.0f);
            this.bolt.visible = s.loaded;
            this.winch.xRot = draw * Mth.TWO_PI * 3.0f;
            for (ModelPart wheel : this.wheels) {
                wheel.xRot = s.wheel * SMALL_WHEEL;
            }
        }

        /**
         * Sehne von der Armspitze zum Scheitel: gespannt hält ihn die Klaue hinten, gelöst liegt er auf gleicher Höhe wie die
         * Spitzen (die Sehne gerade gespannt). {@code side}: +1 links, −1 rechts.
         */
        private static void string(ModelPart string, ModelPart cord, float delta, float draw, float side) {
            float cos = Mth.cos(delta);
            float sin = Mth.sin(delta);
            float tipX = side * ARM_X + side * TIP_X * cos + TIP_Z * sin;
            float tipZ = ARM_Z - side * TIP_X * sin + TIP_Z * cos;
            float apexZ = tipZ + (CLAW_Z - tipZ) * draw;
            float dx = -tipX;
            float dz = apexZ - tipZ;
            float angle = (float) Mth.atan2(dx, dz);
            string.yRot = angle - delta + side * CORD_YAW;
            // die Schnur selbst wird gestreckt (sie ist gegen ihren Halter gedreht, gestreckt wird entlang der Schnur)
            cord.zScale = Math.max(0.05f, Mth.sqrt(dx * dx + dz * dz) / CORD_LENGTH);
        }
    }

    // =====================================================================================
    // Rammbock
    // =====================================================================================

    public static final class BatteringRam extends Model<State> {
        private final ModelPart log;
        private final ModelPart[] wheels;

        public BatteringRam(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.log = root.getChild("frame").getChild("log");
            this.wheels = new ModelPart[] {root.getChild("wheel_fl"), root.getChild("wheel_fr"), root.getChild("wheel_bl"), root.getChild("wheel_br")};
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            this.log.xRot = SiegePoses.ramLog(s.since);
            for (ModelPart wheel : this.wheels) {
                wheel.xRot = s.wheel;
            }
        }
    }

    // =====================================================================================
    // Geschosse
    // =====================================================================================

    /** Steinkugel bzw. Brocken (dasselbe Modell, beim Tribok größer). */
    // =====================================================================================
    // Belagerungsturm
    // =====================================================================================

    /** Der Belagerungsturm: die Fallbrücke schwenkt um ihr Scharnier, die vier Vollräder rollen. */
    public static final class SiegeTower extends Model<State> {
        private final ModelPart bridge;
        private final ModelPart[] wheels;

        public SiegeTower(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.bridge = root.getChild("frame").getChild("bridge");
            this.wheels = new ModelPart[] {root.getChild("wheel_fl"), root.getChild("wheel_fr"), root.getChild("wheel_bl"), root.getChild("wheel_br")};
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            this.bridge.xRot = s.bridge * Mth.DEG_TO_RAD;
            for (ModelPart wheel : this.wheels) {
                wheel.xRot = s.wheel;
            }
        }
    }

    // =====================================================================================
    // Planwagen
    // =====================================================================================

    /** Der Planwagen: Kisten, Fässer und Säcke nur, wenn etwas geladen ist; die Räder rollen. */
    public static final class Wagon extends Model<State> {
        private final ModelPart cargo;
        private final ModelPart[] wheels;

        public Wagon(ModelPart root) {
            super(root, RenderTypes::entityCutout);
            this.cargo = root.getChild("frame").getChild("cargo");
            this.wheels = new ModelPart[] {root.getChild("wheel_fl"), root.getChild("wheel_fr"), root.getChild("wheel_bl"), root.getChild("wheel_br")};
        }

        @Override
        public void setupAnim(State s) {
            super.setupAnim(s);
            this.cargo.visible = s.cargo;
            for (ModelPart wheel : this.wheels) {
                wheel.xRot = s.wheel;
            }
        }
    }

    public static final class Stone extends Model<EntityRenderState> {
        public Stone(ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }
    }

    /** Ballistenbolzen. */
    public static final class Bolt extends Model<EntityRenderState> {
        public Bolt(ModelPart root) {
            super(root, RenderTypes::entityCutout);
        }
    }
}
