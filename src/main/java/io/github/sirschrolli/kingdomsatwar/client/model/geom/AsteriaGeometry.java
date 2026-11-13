package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie Asterias, der Sternenreiterin, auf ihrem gepanzerten Sternenrochen (im Spiel 1,2-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class AsteriaGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private AsteriaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-8.5F, -3F, -13F, 17F, 6F, 26F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(96, 0).addBox(-10.5F, -2.5F, -10F, 21F, 5F, 20F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(166, 37).addBox(-5.5F, -3.875F, -9.25F, 11F, 1F, 17F, new CubeDeformation(-0.25F, -0.125F, -0.25F))
                .texOffs(138, 124).addBox(-2F, -4.45F, 5.3F, 4F, 1F, 3F, new CubeDeformation(-0.4F, -0.05F, -0.3F))
                .texOffs(153, 124).addBox(-1.5F, -4.2F, 8.3F, 3F, 1F, 3F, new CubeDeformation(-0.25F, -0.05F, -0.3F))
                .texOffs(179, 124).addBox(-1F, -3.95F, 11.1F, 2F, 1F, 3F, new CubeDeformation(-0.1F, -0.05F, -0.3F))
                .texOffs(0, 95).addBox(-9F, -3.75F, -5F, 18F, 1F, 9F, new CubeDeformation(-0.4F, -0.25F, 0F))
                .texOffs(0, 80).addBox(-8.95F, -3.9F, -5F, 1F, 5F, 9F, new CubeDeformation(-0.25F, -0.4F, 0F))
                .texOffs(21, 80).addBox(7.95F, -3.9F, -5F, 1F, 5F, 9F, new CubeDeformation(-0.25F, -0.4F, 0F))
                .texOffs(152, 80).addBox(-4F, -5.45F, -5F, 8F, 2F, 9F, new CubeDeformation(0F, -0.05F, -0.4F))
                .texOffs(48, 124).addBox(-2F, -7.9F, -5.5F, 4F, 3F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(139, 116).addBox(-4F, -9.2F, 2.7F, 8F, 4F, 2F, new CubeDeformation(-0.2F, 0F, -0.3F))
                .texOffs(18, 124).addBox(-6.7F, -4.2F, -7F, 3F, 1F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(33, 124).addBox(3.7F, -4.2F, -7F, 3F, 1F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(109, 80).addBox(-7.5F, -2.5F, -5.625F, 15F, 5F, 6F, new CubeDeformation(0F, -0.4F, -0.375F))
                .texOffs(108, 116).addBox(-5F, -2.9F, -5.2F, 10F, 1F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(44, 131).addBox(-1F, -3.6F, -4.6F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(244, 124).addBox(7.45F, -1.6F, -4F, 1F, 2F, 2F, new CubeDeformation(-0.05F, -0.1F, -0.1F))
                .texOffs(0, 131).addBox(-8.45F, -1.6F, -4F, 1F, 2F, 2F, new CubeDeformation(-0.05F, -0.1F, -0.1F)),
                PartPose.offset(0F, 0.3F, -12.75F));
        PartDefinition p_left_cephalic = p_head.addOrReplaceChild("left_cephalic", CubeListBuilder.create()
                .texOffs(109, 106).addBox(-1F, -1F, -5.55F, 2F, 2F, 6F, new CubeDeformation(-0.1F, -0.325F, -0.45F)),
                PartPose.offsetAndRotation(6F, -0.3F, -4.8F, -0.1745F, 0.1396F, 0F));
        PartDefinition p_left_cephalic_curl = p_left_cephalic.addOrReplaceChild("left_cephalic_curl", CubeListBuilder.create()
                .texOffs(190, 124).addBox(-1F, -1F, -1.975F, 2F, 2F, 2F, new CubeDeformation(-0.175F, -0.4F, -0.025F)),
                PartPose.offsetAndRotation(0F, 0.675F, -5.1F, -1.0821F, 0F, 0F));
        PartDefinition p_right_cephalic = p_head.addOrReplaceChild("right_cephalic", CubeListBuilder.create()
                .texOffs(126, 106).addBox(-1F, -1F, -5.55F, 2F, 2F, 6F, new CubeDeformation(-0.1F, -0.325F, -0.45F)),
                PartPose.offsetAndRotation(-6F, -0.3F, -4.8F, -0.1745F, -0.1396F, 0F));
        PartDefinition p_right_cephalic_curl = p_right_cephalic.addOrReplaceChild("right_cephalic_curl", CubeListBuilder.create()
                .texOffs(199, 124).addBox(-1F, -1F, -1.975F, 2F, 2F, 2F, new CubeDeformation(-0.175F, -0.4F, -0.025F)),
                PartPose.offsetAndRotation(0F, 0.675F, -5.1F, -1.0821F, 0F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 37).addBox(0F, -2F, -10F, 21F, 4F, 20F, new CubeDeformation(0F, -0.35F, -0.25F)),
                PartPose.offset(9.75F, 0F, -2.25F));
        PartDefinition p_left_wing_mid = p_left_wing.addOrReplaceChild("left_wing_mid", CubeListBuilder.create()
                .texOffs(35, 62).addBox(0F, -1.5F, -7.625F, 18F, 3F, 13F, new CubeDeformation(0F, -0.45F, -0.125F)),
                PartPose.offsetAndRotation(21F, 0F, -1.5F, 0F, -0.2793F, 0F));
        PartDefinition p_left_wing_tip = p_left_wing_mid.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(94, 95).addBox(0F, -1F, -4.25F, 12F, 2F, 7F, new CubeDeformation(0F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(18F, 0F, -2.25F, 0F, -0.4538F, 0F));
        PartDefinition p_left_pelvic = p_body.addOrReplaceChild("left_pelvic", CubeListBuilder.create()
                .texOffs(172, 95).addBox(-2.5F, -1F, -0.125F, 5F, 2F, 7F, new CubeDeformation(-0.25F, -0.4F, -0.125F)),
                PartPose.offsetAndRotation(4.8F, 0.75F, 11.25F, 0F, 0.384F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(83, 37).addBox(-21F, -2F, -10F, 21F, 4F, 20F, new CubeDeformation(0F, -0.35F, -0.25F)),
                PartPose.offset(-9.75F, 0F, -2.25F));
        PartDefinition p_right_wing_mid = p_right_wing.addOrReplaceChild("right_wing_mid", CubeListBuilder.create()
                .texOffs(98, 62).addBox(-18F, -1.5F, -7.625F, 18F, 3F, 13F, new CubeDeformation(0F, -0.45F, -0.125F)),
                PartPose.offsetAndRotation(-21F, 0F, -1.5F, 0F, 0.2793F, 0F));
        PartDefinition p_right_wing_tip = p_right_wing_mid.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(133, 95).addBox(-12F, -1F, -4.25F, 12F, 2F, 7F, new CubeDeformation(0F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(-18F, 0F, -2.25F, 0F, 0.4538F, 0F));
        PartDefinition p_right_pelvic = p_body.addOrReplaceChild("right_pelvic", CubeListBuilder.create()
                .texOffs(197, 95).addBox(-2.5F, -1F, -0.125F, 5F, 2F, 7F, new CubeDeformation(-0.25F, -0.4F, -0.125F)),
                PartPose.offsetAndRotation(-4.8F, 0.75F, 11.25F, 0F, -0.384F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(161, 62).addBox(-1.5F, -1F, -0.25F, 3F, 2F, 14F, new CubeDeformation(-0.45F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 12.75F));
        PartDefinition p_tail_mid = p_tail.addOrReplaceChild("tail_mid", CubeListBuilder.create()
                .texOffs(0, 62).addBox(-1F, -1F, 0F, 2F, 2F, 15F, new CubeDeformation(-0.25F, -0.325F, 0F)),
                PartPose.offset(0F, 0F, 13.5F));
        PartDefinition p_tail_tip = p_tail_mid.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(196, 62).addBox(-1F, -1F, -0.25F, 2F, 2F, 14F, new CubeDeformation(-0.475F, -0.475F, -0.25F))
                .texOffs(208, 124).addBox(-1F, -1F, 12.65F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offset(0F, 0F, 15F));
        PartDefinition p_rider = p_body.addOrReplaceChild("rider", CubeListBuilder.create()
                .texOffs(230, 106).addBox(-3F, -2.6F, -2F, 6F, 3F, 4F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, -5.4F, 0.6F));
        PartDefinition p_torso = p_rider.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(42, 80).addBox(-3.5F, -9F, -2F, 7F, 9F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(222, 95).addBox(-3.5F, -9.5F, -2.5F, 7F, 4F, 5F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(57, 106).addBox(-2.5F, -6.1F, -2.7F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(70, 106).addBox(-2.5F, -6.1F, 1.7F, 5F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(100, 124).addBox(-2F, -9.35F, -3.05F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.35F))
                .texOffs(180, 106).addBox(-3.5F, -1.6F, -2.5F, 7F, 2F, 5F, new CubeDeformation(-0.1F, -0.4F, -0.3F))
                .texOffs(202, 116).addBox(-2F, -10.6F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_helm = p_torso.addOrReplaceChild("helm", CubeListBuilder.create()
                .texOffs(65, 80).addBox(-3F, -5.8F, -3F, 6F, 6F, 6F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(114, 131).addBox(-2F, -3.9F, -3.3F, 4F, 1F, 1F, new CubeDeformation(0F, 0F, -0.35F))
                .texOffs(14, 131).addBox(-0.5F, -3.4F, -3.3F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, -0.35F))
                .texOffs(13, 124).addBox(-0.5F, -5.9F, -3.2F, 1F, 5F, 1F, new CubeDeformation(-0.15F, 0F, -0.3F))
                .texOffs(38, 116).addBox(-0.5F, -6.25F, -3F, 1F, 1F, 6F, new CubeDeformation(-0.15F, -0.25F, -0.4F)),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_crown = p_helm.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(205, 106).addBox(-3F, -0.95F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.15F, -0.05F, -0.05F))
                .texOffs(89, 131).addBox(-2.7957F, -2.55F, -1.7206F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.2F))
                .texOffs(125, 131).addBox(-2.7957F, -3F, -1.7206F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(94, 131).addBox(-1.8778F, -2.9F, -2.7049F, 1F, 2F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(130, 131).addBox(-1.8778F, -3.7F, -2.7049F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(19, 131).addBox(-0.5F, -3.85F, -3.1F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.2F))
                .texOffs(135, 131).addBox(-0.5F, -4.6F, -3.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(99, 131).addBox(0.8778F, -2.9F, -2.7049F, 1F, 2F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(140, 131).addBox(0.8778F, -3.7F, -2.7049F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(104, 131).addBox(1.7957F, -2.55F, -1.7206F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.2F))
                .texOffs(145, 131).addBox(1.7957F, -3F, -1.7206F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, -5.6F, 0F));
        PartDefinition p_plume = p_helm.addOrReplaceChild("plume", CubeListBuilder.create()
                .texOffs(217, 124).addBox(-1F, -1F, -0.8F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(21, 116).addBox(-1.5F, -1F, 0.6F, 3F, 2F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, -5F, 2.5F, -0.3491F, 0F, 0F));
        PartDefinition p_plume_mid = p_plume.addOrReplaceChild("plume_mid", CubeListBuilder.create()
                .texOffs(53, 116).addBox(-1F, -1F, 0F, 2F, 2F, 5F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 5.4F, -0.1745F, 0F, 0F));
        PartDefinition p_plume_tip = p_plume_mid.addOrReplaceChild("plume_tip", CubeListBuilder.create()
                .texOffs(61, 124).addBox(-1F, -0.5F, 0F, 2F, 1F, 4F, new CubeDeformation(-0.3F, -0.05F, 0F))
                .texOffs(226, 124).addBox(-0.5F, -0.5F, 3.6F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.1396F, 0F, 0F));
        PartDefinition p_cape = p_torso.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(90, 80).addBox(-4F, 0F, -0.2F, 8F, 11F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -8.8F, 2.3F, 0.6632F, 0F, 0F));
        PartDefinition p_cape_tip = p_cape.addOrReplaceChild("cape_tip", CubeListBuilder.create()
                .texOffs(0, 106).addBox(-4F, -0.25F, -0.2F, 8F, 8F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(0F, 11F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_left_arm = p_torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(19, 106).addBox(-2F, -2.2F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(160, 116).addBox(-2.5F, 0.8F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(187, 80).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.35F, 0F, -0.35F))
                .texOffs(236, 116).addBox(-1.5F, 6.2F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(4.3F, -8.2F, 0F, -0.3491F, 0F, -0.1396F));
        PartDefinition p_shield = p_left_arm.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(229, 62).addBox(-0.05F, -4F, -4F, 1F, 8F, 8F, new CubeDeformation(-0.05F, -0.2F, -0.2F))
                .texOffs(98, 116).addBox(-0.05F, -3F, -4.7F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.1F))
                .texOffs(103, 116).addBox(-0.05F, -3F, 3.7F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.1F))
                .texOffs(68, 116).addBox(-0.05F, -4.7F, -3F, 1F, 1F, 6F, new CubeDeformation(-0.05F, -0.1F, -0.2F))
                .texOffs(83, 116).addBox(-0.05F, 3.7F, -3F, 1F, 1F, 6F, new CubeDeformation(-0.05F, -0.1F, -0.2F))
                .texOffs(158, 106).addBox(0.6F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(1.5F, 5.4F, -0.4F, 0.1745F, -0.1396F, 0F));
        PartDefinition p_right_arm = p_torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(38, 106).addBox(-2F, -2.2F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(181, 116).addBox(-2.5F, 0.8F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(200, 80).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.35F, 0F, -0.35F))
                .texOffs(0, 124).addBox(-1.5F, 6.2F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-4.3F, -8.2F, 0F, -0.3491F, 0F, 0.1396F));
        PartDefinition p_lance = p_right_arm.addOrReplaceChild("lance", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -8F, -1F, 2F, 34F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(235, 124).addBox(-1F, -9.6F, -1F, 2F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(111, 124).addBox(-1F, -1.3F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(0, 116).addBox(-2.5F, -3F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(219, 116).addBox(-2F, -4.2F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(53, 131).addBox(-1F, 9.9F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(62, 131).addBox(-1F, 17.9F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(166, 124).addBox(-1.5F, 25.4F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(120, 124).addBox(-1F, 26F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(29, 131).addBox(-3F, 26.5F, -0.5F, 6F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.15F))
                .texOffs(143, 106).addBox(-0.5F, 26.5F, -3F, 1F, 2F, 6F, new CubeDeformation(-0.15F, -0.4F, -0.4F))
                .texOffs(129, 124).addBox(-1F, 28.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(24, 131).addBox(-0.5F, 31.3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(109, 131).addBox(-0.5F, 33.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 3.4907F, 0F, -0.1745F));
        PartDefinition p_pennant = p_lance.addOrReplaceChild("pennant", CubeListBuilder.create()
                .texOffs(55, 95).addBox(-0.5F, -2.5F, -5F, 1F, 5F, 5F, new CubeDeformation(-0.4F, -0.1F, 0F)),
                PartPose.offset(0F, 22.6F, -0.6F));
        PartDefinition p_pennant_tip = p_pennant.addOrReplaceChild("pennant_tip", CubeListBuilder.create()
                .texOffs(169, 106).addBox(-0.5F, -2F, -3.75F, 1F, 4F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.25F))
                .texOffs(7, 131).addBox(-0.5F, -1F, -5.5F, 1F, 2F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 0F, -5F));
        PartDefinition p_left_leg = p_rider.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(68, 95).addBox(-1.5F, -0.3F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(71, 131).addBox(-1.5F, 4.95F, -1.7F, 3F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(2F, -0.6F, -0.2F, -1.3439F, -0.5411F, 0F));
        PartDefinition p_left_shin = p_rider.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(83, 106).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(74, 124).addBox(-1.5F, 5.3F, -1.6F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(5.2F, 0.8F, -5.6F, 1.4661F, -0.0524F, 0F));
        PartDefinition p_right_leg = p_rider.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(81, 95).addBox(-1.5F, -0.3F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(80, 131).addBox(-1.5F, 4.95F, -1.7F, 3F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(-2F, -0.6F, -0.2F, -1.3439F, 0.5411F, 0F));
        PartDefinition p_right_shin = p_rider.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(96, 106).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(87, 124).addBox(-1.5F, 5.3F, -1.6F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-5.2F, 0.8F, -5.6F, 1.4661F, 0.0524F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
