package io.github.sirschrolli.kingdomsatwar.client.model.mage;

import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.DEG;
import static io.github.sirschrolli.kingdomsatwar.client.model.creature.Anim.bump;

import io.github.sirschrolli.kingdomsatwar.client.renderer.mage.MageRenderState;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

/**
 * Die Erzmagier: schweben mit wehender Robe und Umhang, den Stab aufrecht in der Rechten, der Blick folgt dem Gegner.
 * Gesten: Zauber (Stab hoch, dann nach vorn gestoßen), Geschosse (Stab zeigt nach vorn, Rückstoß bei jedem Schuss),
 * großer Zauber (beide Arme weit erhoben, Kopf in den Nacken), Schild (linke Hand abwehrend vor), Spiegelbilder (Arme
 * vor und auseinander), Erwachen (aus der Meditation aufrichten), Phasenwechsel (Arme ausbreiten). In der Meditation
 * hängt der Kopf, die Hände ruhen am Stab; sterbend breitet er die Arme aus und steigt auf.
 */
public class MageModel extends EntityModel<MageRenderState> {
    /** Kopfschmuck, Bart und Haare je Element (Knochen-Namen wie in tools/entitygen/models/mage.py). */
    private static final Map<DragonType, Set<String>> GEAR = new EnumMap<>(DragonType.class);

    static {
        GEAR.put(DragonType.FIRE, Set.of("hood", "flame_crown", "beard"));
        GEAR.put(DragonType.ICE, Set.of("crystal_crown", "hair"));
        GEAR.put(DragonType.LIGHTNING, Set.of("hat", "beard", "beard_long"));
        GEAR.put(DragonType.NATURE, Set.of("antlers", "hair"));
        GEAR.put(DragonType.EARTH, Set.of("hood", "stone_circlet", "beard"));
        GEAR.put(DragonType.WATER, Set.of("shell_crown", "hair"));
    }

    private final ModelPart body;
    private final ModelPart skirt;
    private final ModelPart cape;
    private final ModelPart head;
    private final ModelPart leftArm;
    private final ModelPart rightArm;
    private final ModelPart staff;
    private final ModelPart shardL;
    private final ModelPart shardR;
    private final ModelPart hatTip;
    private final Map<String, ModelPart> gear = new LinkedHashMap<>();

    public MageModel(ModelPart root) {
        super(root);
        this.body = root.getChild("body");
        this.skirt = this.body.getChild("skirt");
        this.cape = this.body.getChild("cape");
        this.head = this.body.getChild("head");
        this.leftArm = this.body.getChild("left_arm");
        this.rightArm = this.body.getChild("right_arm");
        this.staff = this.rightArm.getChild("staff");
        this.shardL = this.body.getChild("shard_l");
        this.shardR = this.body.getChild("shard_r");
        ModelPart hat = this.head.getChild("hat");
        this.hatTip = hat.getChild("hat_mid").getChild("hat_top");
        ModelPart beard = this.head.getChild("beard");
        this.gear.put("hood", this.head.getChild("hood"));
        this.gear.put("flame_crown", this.head.getChild("flame_crown"));
        this.gear.put("crystal_crown", this.head.getChild("crystal_crown"));
        this.gear.put("hat", hat);
        this.gear.put("antlers", this.head.getChild("antlers"));
        this.gear.put("stone_circlet", this.head.getChild("stone_circlet"));
        this.gear.put("shell_crown", this.head.getChild("shell_crown"));
        this.gear.put("beard", beard);
        this.gear.put("beard_long", beard.getChild("beard_long"));
        this.gear.put("hair", this.head.getChild("hair"));
    }

    @Override
    public void setupAnim(MageRenderState state) {
        super.setupAnim(state);
        Set<String> wears = GEAR.get(state.element);
        for (Map.Entry<String, ModelPart> entry : this.gear.entrySet()) {
            entry.getValue().visible = wears.contains(entry.getKey());
        }
        float t = state.ageInTicks;
        float move = Math.min(1.0f, state.walkAnimationSpeed * 2.0f);

        // Schweben: der ganze Körper hebt und senkt sich, Robe und Umhang wehen nach
        float bob = Mth.sin(t * 0.08f);
        this.body.y += bob * 0.6f;
        this.skirt.xRot = 0.04f * Mth.sin(t * 0.08f + 0.6f) + 0.1f * move;
        this.skirt.zRot = 0.025f * Mth.sin(t * 0.06f);
        this.cape.xRot += 0.06f + 0.05f * Mth.sin(t * 0.07f + 1.0f) + 0.35f * move;
        this.cape.zRot = 0.03f * Mth.sin(t * 0.05f);
        this.shardL.zRot += 0.05f * Mth.sin(t * 0.1f);
        this.shardR.zRot -= 0.05f * Mth.sin(t * 0.1f + 1.3f);
        this.hatTip.xRot += 0.08f * Mth.sin(t * 0.06f);

        // Blick zum Gegner
        this.head.yRot = state.yRot * DEG;
        this.head.xRot = state.xRot * DEG;

        // Grundhaltung: Stab aufrecht in der Rechten, linke Hand locker vor dem Körper
        this.rightArm.xRot = -0.38f + 0.03f * bob;
        this.rightArm.zRot = 0.08f;
        this.staff.xRot = 0.38f;
        this.leftArm.xRot = -0.25f + 0.04f * Mth.sin(t * 0.08f + 1.2f);
        this.leftArm.zRot = -0.12f;

        if (state.dying > 0.0f) {
            this.dying(state, t);
            return;
        }
        if (state.dormant) {
            this.meditation(t);
            return;
        }
        float s = state.gestureTime;
        if (s < 0.0f) {
            return;
        }
        switch (state.gesture) {
            case MageBossEntity.ANIM_CAST -> this.cast(s);
            case MageBossEntity.ANIM_MISSILES -> this.missiles(s);
            case MageBossEntity.ANIM_ULTIMATE -> this.ultimate(s, t);
            case MageBossEntity.ANIM_SHIELD -> this.shield(s);
            case MageBossEntity.ANIM_SUMMON -> this.summon(s);
            case MageBossEntity.ANIM_AWAKEN -> this.awaken(s, t);
            case MageBossEntity.ANIM_TRANSITION -> this.transition(s);
            default -> {
            }
        }
    }

    /** Stab hoch über den Kopf, dann nach vorn gestoßen; die Linke lenkt den Zauber. */
    private void cast(float s) {
        float raise = bump(s, 0.35f, 0.55f, 1.3f);
        float thrust = bump(s - 0.5f, 0.1f, 0.2f, 0.6f);
        this.rightArm.xRot = Mth.lerp(raise, this.rightArm.xRot, -2.5f) + 1.0f * thrust;
        this.rightArm.zRot = Mth.lerp(raise, this.rightArm.zRot, 0.25f);
        this.staff.xRot = Mth.lerp(raise, this.staff.xRot, 0.6f) - 0.4f * thrust;
        this.leftArm.xRot = Mth.lerp(bump(s, 0.3f, 0.7f, 1.3f), this.leftArm.xRot, -1.35f);
        this.leftArm.zRot = Mth.lerp(raise, this.leftArm.zRot, -0.35f);
        this.head.xRot -= 0.25f * raise;
        this.body.xRot = -0.08f * raise + 0.12f * thrust;
    }

    /** Stab zeigt nach vorn, bei jedem Geschoss ein kleiner Rückstoß. */
    private void missiles(float s) {
        float aim = bump(s, 0.25f, 1.6f, 2.0f);
        float recoil = s > 0.38f && s < 1.8f ? Math.max(0.0f, Mth.sin((s - 0.38f) * Mth.TWO_PI / 0.3f)) * 0.15f : 0.0f;
        this.rightArm.xRot = Mth.lerp(aim, this.rightArm.xRot, -1.45f) + recoil;
        this.staff.xRot = Mth.lerp(aim, this.staff.xRot, 1.25f) - recoil * 0.5f;
        this.rightArm.yRot = -0.15f * aim;
        this.leftArm.xRot = Mth.lerp(aim, this.leftArm.xRot, -0.6f);
        this.leftArm.zRot = Mth.lerp(aim, this.leftArm.zRot, -0.5f);
        this.body.yRot = 0.12f * aim;
    }

    /** Großer Zauber: beide Arme weit erhoben, der Kopf in den Nacken, der Körper bebt. */
    private void ultimate(float s, float t) {
        float up = bump(s, 0.6f, 3.6f, 4.3f);
        float tremble = Mth.sin(t * 1.3f) * 0.03f * up;
        this.rightArm.xRot = Mth.lerp(up, this.rightArm.xRot, -2.7f) + tremble;
        this.rightArm.zRot = Mth.lerp(up, this.rightArm.zRot, 0.55f);
        this.staff.xRot = Mth.lerp(up, this.staff.xRot, 0.3f);
        this.leftArm.xRot = Mth.lerp(up, this.leftArm.xRot, -2.6f) - tremble;
        this.leftArm.zRot = Mth.lerp(up, this.leftArm.zRot, -0.65f);
        this.head.xRot = Mth.lerp(up, this.head.xRot, -0.45f);
        this.body.xRot = -0.12f * up;
        this.cape.xRot += 0.25f * up;
    }

    /** Schild: die Linke abwehrend vorgestreckt, der Stab schützend vor dem Körper. */
    private void shield(float s) {
        float f = bump(s, 0.25f, 0.8f, 1.2f);
        this.leftArm.xRot = Mth.lerp(f, this.leftArm.xRot, -1.55f);
        this.leftArm.zRot = Mth.lerp(f, this.leftArm.zRot, 0.15f);
        this.rightArm.xRot = Mth.lerp(f, this.rightArm.xRot, -0.9f);
        this.rightArm.zRot = Mth.lerp(f, this.rightArm.zRot, -0.3f);
        this.staff.xRot = Mth.lerp(f, this.staff.xRot, 0.9f);
    }

    /** Spiegelbilder: Arme vor, dann weit auseinander. */
    private void summon(float s) {
        float forward = bump(s, 0.3f, 0.5f, 0.8f);
        float spread = bump(s - 0.6f, 0.25f, 0.6f, 1.0f);
        this.leftArm.xRot = Mth.lerp(forward, this.leftArm.xRot, -1.4f) + 0.7f * spread;
        this.leftArm.zRot = this.leftArm.zRot - 1.1f * spread;
        this.rightArm.xRot = Mth.lerp(forward, this.rightArm.xRot, -1.3f) + 0.6f * spread;
        this.rightArm.zRot = this.rightArm.zRot + 1.0f * spread;
        this.head.xRot -= 0.15f * spread;
    }

    /** Meditation: Kopf gesenkt, die Hände ruhen am aufrechten Stab. */
    private void meditation(float t) {
        this.head.xRot = 0.55f + 0.03f * Mth.sin(t * 0.05f);
        this.head.yRot = 0.0f;
        this.rightArm.xRot = -0.55f;
        this.rightArm.zRot = -0.05f;
        this.staff.xRot = 0.55f;
        this.leftArm.xRot = -0.7f;
        this.leftArm.zRot = 0.55f;
        this.leftArm.yRot = 0.3f;
        this.body.xRot = 0.06f;
    }

    /** Erwachen: richtet sich auf, breitet die Arme aus, hebt den Stab. */
    private void awaken(float s, float t) {
        float rise = Mth.clamp(s / 1.2f, 0.0f, 1.0f);
        float open = bump(s - 0.6f, 0.5f, 1.3f, 2.4f);
        this.head.xRot = Mth.lerp(rise, 0.55f, this.head.xRot) - 0.3f * open;
        this.leftArm.zRot = Mth.lerp(open, this.leftArm.zRot, -1.0f);
        this.leftArm.xRot = Mth.lerp(open, this.leftArm.xRot, -0.5f);
        this.rightArm.xRot = Mth.lerp(open, this.rightArm.xRot, -2.3f);
        this.rightArm.zRot = Mth.lerp(open, this.rightArm.zRot, 0.4f);
        this.staff.xRot = Mth.lerp(open, this.staff.xRot, 0.5f);
        this.body.xRot = Mth.lerp(rise, 0.06f, 0.0f) - 0.08f * open;
    }

    /** Phasenwechsel: Arme ausgebreitet, Kopf zurück – die Kraft bricht aus ihm heraus. */
    private void transition(float s) {
        float f = bump(s, 0.3f, 1.0f, 1.5f);
        this.leftArm.zRot = Mth.lerp(f, this.leftArm.zRot, -1.35f);
        this.leftArm.xRot = Mth.lerp(f, this.leftArm.xRot, -0.2f);
        this.rightArm.zRot = Mth.lerp(f, this.rightArm.zRot, 1.25f);
        this.rightArm.xRot = Mth.lerp(f, this.rightArm.xRot, -0.3f);
        this.head.xRot = Mth.lerp(f, this.head.xRot, -0.5f);
        this.body.xRot = -0.15f * f;
        this.cape.xRot += 0.4f * f;
    }

    /** Sterbend: die Arme sinken auseinander, der Kopf fällt zurück. */
    private void dying(MageRenderState state, float t) {
        float f = Mth.clamp(state.dying / 20.0f, 0.0f, 1.0f);
        this.leftArm.zRot = Mth.lerp(f, this.leftArm.zRot, -1.1f);
        this.rightArm.zRot = Mth.lerp(f, this.rightArm.zRot, 1.0f);
        this.leftArm.xRot = Mth.lerp(f, this.leftArm.xRot, -0.3f);
        this.rightArm.xRot = Mth.lerp(f, this.rightArm.xRot, -0.3f);
        this.head.xRot = Mth.lerp(f, this.head.xRot, -0.6f);
        this.body.xRot = -0.2f * f + Mth.sin(t * 0.9f) * 0.02f * f;
    }
}
