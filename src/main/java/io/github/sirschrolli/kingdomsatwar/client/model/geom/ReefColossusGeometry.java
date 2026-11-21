package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Karkinos, dem Riffkoloss (Panzer mit Stielaugen, Fühlern und Mundplatten, Brecher- und Greifschere mit beweglichem Finger, sechs Laufbeine in drei Gliedern, die Tempelkuppel mit Korallen, Tang und dem Gezeiten-Speer; im Spiel ×2).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ReefColossusGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private ReefColossusGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(405, 0).addBox(-11F, -5F, -10F, 22F, 9F, 14F)
                .texOffs(368, 53).addBox(-9F, -6.75F, -9F, 18F, 2F, 11F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(334, 72).addBox(-6F, -7.5F, -7F, 12F, 1F, 7F)
                .texOffs(213, 53).addBox(-12F, -3F, -8F, 24F, 5F, 9F)
                .texOffs(65, 94).addBox(-9F, -4.7F, -11.7F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(74, 94).addBox(-5F, -4.7F, -11.7F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(83, 94).addBox(-1F, -4.7F, -11.7F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(92, 94).addBox(3F, -4.7F, -11.7F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(101, 94).addBox(7F, -4.7F, -11.7F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(333, 86).addBox(-1.5F, -5.6F, -12.8F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(110, 94).addBox(-12.2F, -2.6F, -6.7F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(119, 94).addBox(10.2F, -2.6F, -6.7F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(280, 53).addBox(-9F, 3.75F, -9F, 18F, 2F, 12F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 14F, -4F));
        PartDefinition p_mouth_left = p_body.addOrReplaceChild("mouth_left", CubeListBuilder.create()
                .texOffs(346, 86).addBox(-0.3F, -0.1F, -1.6F, 4F, 4F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.4F)),
                PartPose.offset(0.2F, 1.6F, -10.2F));
        PartDefinition p_mouth_right = p_body.addOrReplaceChild("mouth_right", CubeListBuilder.create()
                .texOffs(359, 86).addBox(-3.7F, -0.1F, -1.6F, 4F, 4F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.4F)),
                PartPose.offset(-0.2F, 1.6F, -10.2F));
        PartDefinition p_left_eye = p_body.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(451, 72).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(406, 72).addBox(-2F, -9.4F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(4.5F, -6.5F, -8F));
        PartDefinition p_left_antenna = p_body.addOrReplaceChild("left_antenna", CubeListBuilder.create()
                .texOffs(126, 72).addBox(-0.5F, -0.5F, -10F, 1F, 1F, 10F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(2F, -3.5F, -10.5F, -0.5585F, -0.3142F, 0F));
        PartDefinition p_left_antenna_tip = p_left_antenna.addOrReplaceChild("left_antenna_tip", CubeListBuilder.create()
                .texOffs(199, 72).addBox(-0.5F, -0.5F, -9F, 1F, 1F, 9F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.3142F, 0F, 0F));
        PartDefinition p_right_eye = p_body.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(460, 72).addBox(-1F, -6F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(423, 72).addBox(-2F, -9.4F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(-4.5F, -6.5F, -8F));
        PartDefinition p_right_antenna = p_body.addOrReplaceChild("right_antenna", CubeListBuilder.create()
                .texOffs(149, 72).addBox(-0.5F, -0.5F, -10F, 1F, 1F, 10F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-2F, -3.5F, -10.5F, -0.5585F, 0.3142F, 0F));
        PartDefinition p_right_antenna_tip = p_right_antenna.addOrReplaceChild("right_antenna_tip", CubeListBuilder.create()
                .texOffs(220, 72).addBox(-0.5F, -0.5F, -9F, 1F, 1F, 9F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.3142F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(60, 72).addBox(0F, -3F, -3F, 8F, 6F, 6F),
                PartPose.offsetAndRotation(10F, 1.5F, -7F, 0F, -0.5236F, 0F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(478, 53).addBox(-3F, -3F, -7F, 6F, 6F, 7F),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, 1.309F, 0F));
        PartDefinition p_left_claw = p_left_forearm.addOrReplaceChild("left_claw", CubeListBuilder.create()
                .texOffs(65, 29).addBox(-5.5F, -5F, -12.75F, 11F, 10F, 13F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(155, 53).addBox(-5.5F, -2.7F, -20.25F, 6F, 7F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.25F))
                .texOffs(381, 86).addBox(-2.8F, -5.8F, -8.3F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(190, 94).addBox(0.5F, -5.1F, -4F, 2F, 1F, 2F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(199, 94).addBox(-4F, -5.05F, -3.4F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(128, 94).addBox(1.3F, -5.9F, -9.8F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(269, 94).addBox(-1.1F, -6.8F, -7.7F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0F, -0.1745F, 0F));
        PartDefinition p_left_claw_finger = p_left_claw.addOrReplaceChild("left_claw_finger", CubeListBuilder.create()
                .texOffs(184, 53).addBox(-4.7F, -2.8F, -8.75F, 5F, 6F, 9F, new CubeDeformation(-0.3F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(4.6F, 1.2F, -12.5F, 0F, -0.1396F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(172, 72).addBox(-8F, -2.5F, -2.5F, 8F, 5F, 5F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(-10F, 1.5F, -7F, 0F, 0.5236F, 0F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(241, 72).addBox(-2.5F, -2.5F, -5F, 5F, 5F, 5F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, -1.309F, 0F));
        PartDefinition p_right_claw = p_right_forearm.addOrReplaceChild("right_claw", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-4F, -4F, -10F, 8F, 8F, 10F, new CubeDeformation(-0.2F, -0.4F, 0F))
                .texOffs(341, 53).addBox(-3.8F, -2.2F, -18.75F, 4F, 5F, 9F, new CubeDeformation(-0.2F, -0.4F, -0.25F))
                .texOffs(208, 94).addBox(-1.7F, -4.1F, -6.2F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, -5F, 0F, 0.1745F, 0F));
        PartDefinition p_right_claw_finger = p_right_claw.addOrReplaceChild("right_claw_finger", CubeListBuilder.create()
                .texOffs(0, 72).addBox(-0.4F, -1.9F, -8.9F, 4F, 4F, 9F, new CubeDeformation(-0.4F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-3.6F, 0.6F, -10F, 0F, 0.1396F, 0F));
        PartDefinition p_left_leg_0 = p_body.addOrReplaceChild("left_leg_0", CubeListBuilder.create()
                .texOffs(183, 86).addBox(0F, -1.5F, -1.5F, 9F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(10.5F, 3F, -4.5F, 0F, -0.3491F, -0.4887F));
        PartDefinition p_left_leg_0_low = p_left_leg_0.addOrReplaceChild("left_leg_0_low", CubeListBuilder.create()
                .texOffs(9, 86).addBox(0F, -1.5F, -1.5F, 11F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, 0F, 1.2217F));
        PartDefinition p_left_leg_0_tip = p_left_leg_0_low.addOrReplaceChild("left_leg_0_tip", CubeListBuilder.create()
                .texOffs(475, 86).addBox(0F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(11F, 0F, 0F, 0F, 0F, 0.3491F));
        PartDefinition p_left_leg_1 = p_body.addOrReplaceChild("left_leg_1", CubeListBuilder.create()
                .texOffs(208, 86).addBox(0F, -1.5F, -1.5F, 9F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(10.5F, 3F, 0.5F, 0F, 0F, -0.4887F));
        PartDefinition p_left_leg_1_low = p_left_leg_1.addOrReplaceChild("left_leg_1_low", CubeListBuilder.create()
                .texOffs(38, 86).addBox(0F, -1.5F, -1.5F, 11F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, 0F, 1.2217F));
        PartDefinition p_left_leg_1_tip = p_left_leg_1_low.addOrReplaceChild("left_leg_1_tip", CubeListBuilder.create()
                .texOffs(488, 86).addBox(0F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(11F, 0F, 0F, 0F, 0F, 0.3491F));
        PartDefinition p_left_leg_2 = p_body.addOrReplaceChild("left_leg_2", CubeListBuilder.create()
                .texOffs(233, 86).addBox(0F, -1.5F, -1.5F, 9F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(10.5F, 3F, 5.5F, 0F, 0.3491F, -0.4887F));
        PartDefinition p_left_leg_2_low = p_left_leg_2.addOrReplaceChild("left_leg_2_low", CubeListBuilder.create()
                .texOffs(67, 86).addBox(0F, -1.5F, -1.5F, 11F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, 0F, 1.2217F));
        PartDefinition p_left_leg_2_tip = p_left_leg_2_low.addOrReplaceChild("left_leg_2_tip", CubeListBuilder.create()
                .texOffs(0, 94).addBox(0F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(11F, 0F, 0F, 0F, 0F, 0.3491F));
        PartDefinition p_right_leg_0 = p_body.addOrReplaceChild("right_leg_0", CubeListBuilder.create()
                .texOffs(258, 86).addBox(-9F, -1.5F, -1.5F, 9F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-10.5F, 3F, -4.5F, 0F, 0.3491F, 0.4887F));
        PartDefinition p_right_leg_0_low = p_right_leg_0.addOrReplaceChild("right_leg_0_low", CubeListBuilder.create()
                .texOffs(96, 86).addBox(-11F, -1.5F, -1.5F, 11F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0F, -1.2217F));
        PartDefinition p_right_leg_0_tip = p_right_leg_0_low.addOrReplaceChild("right_leg_0_tip", CubeListBuilder.create()
                .texOffs(13, 94).addBox(-4F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-11F, 0F, 0F, 0F, 0F, -0.3491F));
        PartDefinition p_right_leg_1 = p_body.addOrReplaceChild("right_leg_1", CubeListBuilder.create()
                .texOffs(283, 86).addBox(-9F, -1.5F, -1.5F, 9F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-10.5F, 3F, 0.5F, 0F, 0F, 0.4887F));
        PartDefinition p_right_leg_1_low = p_right_leg_1.addOrReplaceChild("right_leg_1_low", CubeListBuilder.create()
                .texOffs(125, 86).addBox(-11F, -1.5F, -1.5F, 11F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0F, -1.2217F));
        PartDefinition p_right_leg_1_tip = p_right_leg_1_low.addOrReplaceChild("right_leg_1_tip", CubeListBuilder.create()
                .texOffs(26, 94).addBox(-4F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-11F, 0F, 0F, 0F, 0F, -0.3491F));
        PartDefinition p_right_leg_2 = p_body.addOrReplaceChild("right_leg_2", CubeListBuilder.create()
                .texOffs(308, 86).addBox(-9F, -1.5F, -1.5F, 9F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-10.5F, 3F, 5.5F, 0F, -0.3491F, 0.4887F));
        PartDefinition p_right_leg_2_low = p_right_leg_2.addOrReplaceChild("right_leg_2_low", CubeListBuilder.create()
                .texOffs(154, 86).addBox(-11F, -1.5F, -1.5F, 11F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0F, -1.2217F));
        PartDefinition p_right_leg_2_tip = p_right_leg_2_low.addOrReplaceChild("right_leg_2_tip", CubeListBuilder.create()
                .texOffs(39, 94).addBox(-4F, -1F, -1F, 4F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(-11F, 0F, 0F, 0F, 0F, -0.3491F));
        PartDefinition p_shell = p_body.addOrReplaceChild("shell", CubeListBuilder.create()
                .texOffs(192, 29).addBox(-12F, -3F, -7.5F, 24F, 4F, 15F, new CubeDeformation(0F, 0F, -0.06F))
                .texOffs(85, 0).addBox(-7.5F, -3F, -12F, 15F, 4F, 24F, new CubeDeformation(-0.06F, 0F, 0F))
                .texOffs(239, 0).addBox(-10.5F, -3F, -10.5F, 21F, 4F, 21F, new CubeDeformation(-0.18F, 0F, -0.18F))
                .texOffs(356, 29).addBox(-11.5F, -6.75F, -7F, 23F, 4F, 14F, new CubeDeformation(-0.3F, -0.25F, -0.056F))
                .texOffs(164, 0).addBox(-7F, -6.75F, -11.5F, 14F, 4F, 23F, new CubeDeformation(-0.056F, -0.25F, -0.3F))
                .texOffs(324, 0).addBox(-10F, -6.75F, -10F, 20F, 4F, 20F, new CubeDeformation(-0.368F, -0.25F, -0.368F))
                .texOffs(90, 53).addBox(-10F, -9.5F, -6F, 20F, 3F, 12F, new CubeDeformation(-0.4F, 0F, -0.048F))
                .texOffs(0, 29).addBox(-6F, -9.5F, -10F, 12F, 3F, 20F, new CubeDeformation(-0.048F, 0F, -0.4F))
                .texOffs(123, 29).addBox(-8.5F, -9.5F, -8.5F, 17F, 3F, 17F, new CubeDeformation(-0.244F, 0F, -0.244F))
                .texOffs(427, 53).addBox(-7.5F, -12.25F, -5F, 15F, 3F, 10F, new CubeDeformation(-0.1F, -0.25F, -0.412F))
                .texOffs(431, 29).addBox(-5F, -12.25F, -7.5F, 10F, 3F, 15F, new CubeDeformation(-0.412F, -0.25F, -0.1F))
                .texOffs(37, 53).addBox(-6.5F, -12.25F, -6.5F, 13F, 3F, 13F, new CubeDeformation(-0.136F, -0.25F, -0.136F))
                .texOffs(373, 72).addBox(-5F, -14F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.2F, 0F, -0.024F))
                .texOffs(27, 72).addBox(-3F, -14F, -5F, 6F, 2F, 10F, new CubeDeformation(-0.024F, 0F, -0.2F))
                .texOffs(89, 72).addBox(-4.5F, -14F, -4.5F, 9F, 2F, 9F, new CubeDeformation(-0.372F, 0F, -0.372F))
                .texOffs(487, 72).addBox(-2.5F, -15.7F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(137, 94).addBox(-1F, -17.2F, -1F, 2F, 2F, 2F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(271, 29).addBox(-13F, -0.2F, -8F, 26F, 2F, 16F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(0, 0).addBox(-8F, -0.2F, -13F, 16F, 2F, 26F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(262, 72).addBox(3.133F, -6.25F, 8.9779F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(271, 72).addBox(8.9779F, -6.25F, 3.133F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(280, 72).addBox(8.9779F, -6.25F, -5.133F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(289, 72).addBox(3.133F, -6.25F, -10.9779F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(298, 72).addBox(-5.133F, -6.25F, -10.9779F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(307, 72).addBox(-10.9779F, -6.25F, -5.133F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(316, 72).addBox(-10.9779F, -6.25F, 3.133F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(325, 72).addBox(-5.133F, -6.25F, 8.9779F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(394, 86).addBox(-8F, -7.2F, -5.2F, 3F, 2F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(407, 86).addBox(-8F, -8.7F, -5.2F, 3F, 2F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(455, 86).addBox(5.4F, -10.1F, 3.4F, 1F, 4F, 1F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(185, 94).addBox(6.6F, -9.2F, 2.8F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(274, 94).addBox(4.6F, -8.4F, 4.6F, 1F, 2F, 1F)
                .texOffs(284, 94).addBox(5.5F, -11.1F, 3.9F, 3F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(446, 86).addBox(-3.6F, -11.8F, 4F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(146, 94).addBox(-2.6F, -11F, 4.8F, 2F, 2F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(420, 86).addBox(7.9F, -4.7F, -5.9F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(433, 86).addBox(-9.9F, -4.3F, 4.3F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(155, 94).addBox(1.6F, -14.6F, -2.6F, 2F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(217, 94).addBox(-9.2F, -7.6F, 1.4F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(226, 94).addBox(8.8F, -7.8F, -1.2F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(52, 94).addBox(-4.6F, -10.5F, -5.8F, 3F, 1F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, -1F, 11F));
        PartDefinition p_kelp_0 = p_shell.addOrReplaceChild("kelp_0", CubeListBuilder.create()
                .texOffs(469, 72).addBox(-1.5F, -0.25F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(7.3278F, -5F, 8.7329F, 0F, -0.6981F, -0.1396F));
        PartDefinition p_kelp_1 = p_shell.addOrReplaceChild("kelp_1", CubeListBuilder.create()
                .texOffs(372, 86).addBox(-1.5F, 0F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offsetAndRotation(10.3319F, -3.5F, -4.8178F, 0F, -2.0071F, 0.1396F));
        PartDefinition p_kelp_2 = p_shell.addOrReplaceChild("kelp_2", CubeListBuilder.create()
                .texOffs(478, 72).addBox(-1.5F, 0F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-3.899F, -6F, -10.7125F, 0F, -3.4907F, -0.1396F));
        PartDefinition p_kelp_3 = p_shell.addOrReplaceChild("kelp_3", CubeListBuilder.create()
                .texOffs(0, 86).addBox(-1.5F, -0.25F, -0.5F, 3F, 6F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(-10.7125F, -4F, 3.899F, 0F, -5.0615F, 0.1396F));
        PartDefinition p_spear = p_shell.addOrReplaceChild("spear", CubeListBuilder.create()
                .texOffs(114, 29).addBox(-1F, -17F, -1F, 2F, 21F, 2F, new CubeDeformation(-0.45F, 0F, -0.45F))
                .texOffs(235, 94).addBox(-1F, -6F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(244, 94).addBox(-1F, -11F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(253, 94).addBox(-1F, -15.6F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(460, 86).addBox(-2.5F, -18.8F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(262, 94).addBox(-1F, -19.2F, -1.35F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(440, 72).addBox(-2F, -25.6F, -0.5F, 4F, 7F, 1F, new CubeDeformation(-0.3F, 0F, -0.05F))
                .texOffs(164, 94).addBox(-1F, -28.6F, -0.5F, 2F, 3F, 1F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(279, 94).addBox(-0.5F, -30.3F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.25F))
                .texOffs(171, 94).addBox(-2.8F, -21.4F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.15F))
                .texOffs(178, 94).addBox(0.8F, -21.4F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.4F, 0F, -0.15F)),
                PartPose.offsetAndRotation(4.2F, -9.5F, -1.2F, -0.3142F, 0.5236F, -0.5585F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
