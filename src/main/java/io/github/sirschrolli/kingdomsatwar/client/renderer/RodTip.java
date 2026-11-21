package io.github.sirschrolli.kingdomsatwar.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.mixin.client.CameraAccess;
import io.github.sirschrolli.kingdomsatwar.mixin.client.FirstPersonHandsAccess;
import io.github.sirschrolli.kingdomsatwar.mixin.client.LivingEntityRendererInvoker;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.client.renderer.entity.state.AvatarRenderState;
import net.minecraft.client.resources.model.cuboid.ItemTransform;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.FishingRodItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.SwingAnimationType;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Wo die Schnur an den Angeln in 3D ansetzt (seit 1.1.3): genau an der Spitze der ausgeworfenen Rute, so wie sie gerade
 * gezeichnet wird. Minecraft setzt die Schnur dort an, wo es die Spitze seiner flachen Angel vermutet – aus der eigenen Sicht
 * an einer festen Stelle des Bildes (die nur bei 16:9 und ohne Ausholen passt), von außen an einer festen Stelle vor dem
 * Spieler (die beim Gehen, Schleichen oder Reiten nicht mitgeht). Hier wird die Spitze nachgerechnet:
 * <ul>
 *     <li><b>aus der eigenen Sicht</b> mit denselben Schritten wie die Hand (Nachschwingen beim Umsehen, Heben nach dem
 *     Wechseln oder Auswerfen, Ausholen, Lage des Modells in der Hand) und an dieselbe Stelle des Bildes übertragen – auch bei
 *     anderem Seitenverhältnis, anderem Sichtfeld und unter Wasser;</li>
 *     <li><b>von außen</b> (andere Spieler, die Ansichten mit F5) am echten Spielermodell: dieselbe Haltung, dieselben
 *     Bewegungen von Arm und Körper wie beim Zeichnen.</li>
 * </ul>
 */
public final class RodTip {
    private RodTip() {}

    /** Die Spitze der ausgeworfenen Rute im Modell (Pixel): das Ende des obersten Rings – bei allen Angeln an derselben Stelle. */
    public static final Vector3fc TIP = new Vector3f(16.13f, 13.15f, 8.0f);

    /** Wie die Angeln in der Hand liegen: wie die Angel von Minecraft („handheld_rod“); dieselben Werte nutzt das Modell. */
    public static final float[] THIRD_ROTATION = {0.0f, 90.0f, 55.0f};
    public static final float[] THIRD_TRANSLATION = {0.0f, 4.0f, 2.5f};
    public static final float THIRD_SCALE = 0.85f;
    public static final float[] FIRST_ROTATION = {0.0f, 90.0f, 25.0f};
    public static final float[] FIRST_TRANSLATION = {0.0f, 1.6f, 0.8f};
    public static final float FIRST_SCALE = 0.68f;

    private static final ItemTransform THIRD_RIGHT = transform(THIRD_ROTATION, THIRD_TRANSLATION, THIRD_SCALE, false);
    private static final ItemTransform THIRD_LEFT = transform(THIRD_ROTATION, THIRD_TRANSLATION, THIRD_SCALE, true);
    private static final ItemTransform FIRST_RIGHT = transform(FIRST_ROTATION, FIRST_TRANSLATION, FIRST_SCALE, false);
    private static final ItemTransform FIRST_LEFT = transform(FIRST_ROTATION, FIRST_TRANSLATION, FIRST_SCALE, true);

    /** Die Lage in der Hand wie im Modell (für die linke Hand spiegelbildlich eingetragen, wie bei „handheld_rod“). */
    private static ItemTransform transform(float[] rotation, float[] translation, float scale, boolean left) {
        float sign = left ? -1.0f : 1.0f;
        return new ItemTransform(new Vector3f(rotation[0], rotation[1] * sign, rotation[2] * sign),
                new Vector3f(translation[0], translation[1], translation[2]).div(16.0f), new Vector3f(scale, scale, scale));
    }

    /** Hat diese Angel ein Modell in 3D (die Angeln der Mod und die von Minecraft)? */
    public static boolean hasModel(ItemStack rod) {
        return rod.getItem() instanceof FishingRodItem && (rod.is(Items.FISHING_ROD)
                || KingdomsAtWar.MODID.equals(BuiltInRegistries.ITEM.getKey(rod.getItem()).getNamespace()));
    }

    /** Die Spitze in der Welt – aus der eigenen Sicht oder von außen; {@code null}, wenn sie sich nicht bestimmen lässt. */
    public static @Nullable Vec3 tip(Player owner, HumanoidArm arm, float partialTicks) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.options.getCameraType().isFirstPerson() && owner == minecraft.player) {
            return firstPerson(minecraft, minecraft.player, arm, partialTicks);
        }
        return thirdPerson(minecraft, owner, arm, partialTicks);
    }

    /**
     * Aus der eigenen Sicht: die Spitze im Bild der Hand (Sichtfeld der Hand, meist 70°) und dorthin in die Welt, wo sie
     * im Bild der Welt (eigenes Sichtfeld, Sprinten, Wasser …) an derselben Stelle erscheint – in derselben Tiefe, damit auch
     * das Wippen beim Gehen beide gleich bewegt.
     */
    private static @Nullable Vec3 firstPerson(Minecraft minecraft, LocalPlayer player, HumanoidArm arm, float partialTicks) {
        Camera camera = minecraft.gameRenderer.mainCamera();
        boolean left = arm == HumanoidArm.LEFT;
        int invert = left ? -1 : 1;
        InteractionHand hand = arm == player.getMainArm() ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND;
        PoseStack pose = new PoseStack();
        // die Hand schwingt beim Umsehen etwas nach
        float xBob = Mth.lerp(partialTicks, player.xBobO, player.xBob);
        float yBob = Mth.lerp(partialTicks, player.yBobO, player.yBob);
        pose.rotateDegrees(Axis.XP, (player.getViewXRot(partialTicks) - xBob) * 0.1f);
        pose.rotateDegrees(Axis.YP, (player.getViewYRot(partialTicks) - yBob) * 0.1f);
        // gesenkt nach dem Wechseln und gleich nach dem Auswerfen, dann wieder gehoben
        FirstPersonHandsAccess hands = (FirstPersonHandsAccess) player.firstPersonHandsAndItems();
        float height = hand == InteractionHand.MAIN_HAND
                ? Mth.lerp(partialTicks, hands.kingdomsatwar$oMainHandHeight(), hands.kingdomsatwar$mainHandHeight())
                : Mth.lerp(partialTicks, hands.kingdomsatwar$oOffHandHeight(), hands.kingdomsatwar$offHandHeight());
        float lowered = minecraft.getItemModelResolver().swapAnimationScale(player.getItemInHand(hand)) * (1.0f - height);
        pose.translate(invert * 0.56f, -0.52f + lowered * -0.6f, -0.72f);
        // Ausholen beim Auswerfen und Einholen
        LivingEntity.SwingDescription swing = player.getCurrentSwing();
        if (swing != null && swing.hand() == hand && swing.animation().type() == SwingAnimationType.WHACK) {
            float attack = player.getSwingAnimation(partialTicks);
            pose.translate(invert * -0.4f * Mth.sin(Mth.sqrt(attack) * Mth.PI), 0.2f * Mth.sin(Mth.sqrt(attack) * Mth.TWO_PI),
                    -0.2f * Mth.sin(attack * Mth.PI));
            float ySwing = Mth.sin(attack * attack * Mth.PI);
            pose.rotateDegrees(Axis.YP, invert * (45.0f + ySwing * -20.0f));
            float xzSwing = Mth.sin(Mth.sqrt(attack) * Mth.PI);
            pose.rotateDegrees(Axis.ZP, invert * xzSwing * -20.0f);
            pose.rotateDegrees(Axis.XP, xzSwing * -80.0f);
            pose.rotateDegrees(Axis.YP, invert * -45.0f);
        }
        (left ? FIRST_LEFT : FIRST_RIGHT).apply(left, pose.last());
        Vector3f view = pose.last().pose().transformPosition(TIP.x() / 16.0f, TIP.y() / 16.0f, TIP.z() / 16.0f, new Vector3f());
        if (view.z > -0.05f) {
            return null;
        }
        // Bild der Hand → Bild der Welt: gleiche Stelle im Bild, gleiche Tiefe
        float hudFov = ((CameraAccess) camera).kingdomsatwar$hudFov();
        float worldFov = camera.getFov();
        if (hudFov <= 0.0f || worldFov <= 0.0f) {
            return null;
        }
        double k = Math.tan(Math.toRadians(worldFov) / 2.0) / Math.tan(Math.toRadians(hudFov) / 2.0);
        Vector3fc forward = camera.forwardVector();
        Vector3fc up = camera.upVector();
        Vector3fc leftward = camera.leftVector();
        double depth = -view.z;
        double right = view.x * k;
        double upward = view.y * k;
        Vec3 eye = camera.position();
        return new Vec3(eye.x + forward.x() * depth + up.x() * upward - leftward.x() * right,
                eye.y + forward.y() * depth + up.y() * upward - leftward.y() * right,
                eye.z + forward.z() * depth + up.z() * upward - leftward.z() * right);
    }

    /**
     * Von außen: am echten Spielermodell, mit denselben Schritten wie beim Zeichnen (Lage und Drehung des Körpers, Haltung und
     * Bewegung des Arms, Lage der Angel in der Hand).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static @Nullable Vec3 thirdPerson(Minecraft minecraft, Player owner, HumanoidArm arm, float partialTicks) {
        if (!(owner instanceof AbstractClientPlayer client)) {
            return null;
        }
        EntityRenderer<? super AbstractClientPlayer, ?> renderer = minecraft.getEntityRenderDispatcher().getRenderer(client);
        if (!(renderer instanceof AvatarRenderer<?> avatar) || !(avatar.getModel() instanceof PlayerModel model)) {
            return null;
        }
        AvatarRenderState state = (AvatarRenderState) ((EntityRenderer) renderer).createRenderState(client, partialTicks);
        PoseStack pose = new PoseStack();
        Vec3 offset = avatar.getRenderOffset(state);
        pose.translate(offset.x, offset.y, offset.z);
        pose.scale(state.scale, state.scale, state.scale);
        ((LivingEntityRendererInvoker) avatar).kingdomsatwar$setupRotations(state, pose, state.bodyRot, state.scale);
        pose.scale(-1.0f, -1.0f, 1.0f);
        ((LivingEntityRendererInvoker) avatar).kingdomsatwar$scale(state, pose);
        pose.translate(0.0f, -1.501f, 0.0f);
        model.setupAnim(state);
        model.translateToHand(state, arm, pose);
        pose.rotateDegrees(Axis.XP, -90.0f);
        pose.rotateDegrees(Axis.YP, 180.0f);
        boolean left = arm == HumanoidArm.LEFT;
        pose.translate((left ? -1.0f : 1.0f) / 16.0f, 2.0f / 16.0f, -10.0f / 16.0f);
        (left ? THIRD_LEFT : THIRD_RIGHT).apply(left, pose.last());
        Vector3f tip = pose.last().pose().transformPosition(TIP.x() / 16.0f, TIP.y() / 16.0f, TIP.z() / 16.0f, new Vector3f());
        return new Vec3(state.x + tip.x, state.y + tip.y, state.z + tip.z);
    }
}
