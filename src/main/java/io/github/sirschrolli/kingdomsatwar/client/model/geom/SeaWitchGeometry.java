package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Meerhexe (seit 0.97): hagere Meerjungfrau mit Tanghaar, treibenden Strähnen, Korallenkrone, Tangumhang, Krallen, Hexenstab mit leuchtender Kugel und einem gebänderten Aalschwanz mit Stacheln und Fächerflossen; im Spiel ×1,5.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SeaWitchGeometry {
    public static final int TEXTURE_WIDTH = 192;
    public static final int TEXTURE_HEIGHT = 192;

    private SeaWitchGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_swim = root.addOrReplaceChild("swim", CubeListBuilder.create(),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_swim.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(29, 31).addBox(-3F, -9.5F, -2F, 6F, 5F, 4F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(130, 31).addBox(-2.5F, -4.75F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.2F))
                .texOffs(17, 57).addBox(-2.5F, -9.85F, -2.3F, 5F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.35F))
                .texOffs(147, 50).addBox(-0.5F, -9.45F, -2.4F, 1F, 2F, 1F, new CubeDeformation(0F, -0.15F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_shawl = p_torso.addOrReplaceChild("shawl", CubeListBuilder.create()
                .texOffs(103, 31).addBox(-4F, -10.45F, -2.5F, 8F, 3F, 5F, new CubeDeformation(-0.3F, -0.35F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_shawl_l = p_shawl.addOrReplaceChild("shawl_l", CubeListBuilder.create()
                .texOffs(59, 41).addBox(-1F, 0F, -0.55F, 2F, 7F, 1F, new CubeDeformation(-0.05F, 0F, -0.25F)),
                PartPose.offsetAndRotation(2.3F, -8F, -2.15F, -0.0698F, 0F, 0.0524F));
        PartDefinition p_shawl_r = p_shawl.addOrReplaceChild("shawl_r", CubeListBuilder.create()
                .texOffs(66, 41).addBox(-1F, 0F, -0.55F, 2F, 7F, 1F, new CubeDeformation(-0.05F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-2.3F, -8F, -2.15F, -0.0698F, 0F, -0.0524F));
        PartDefinition p_cape = p_shawl.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(50, 31).addBox(-4F, 0F, -0.5F, 8F, 8F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -9.4F, 2.15F, 0.1047F, 0F, 0F));
        PartDefinition p_cape_tip = p_cape.addOrReplaceChild("cape_tip", CubeListBuilder.create()
                .texOffs(147, 31).addBox(-3.5F, 0F, -0.5F, 7F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 8F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_head = p_torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(39, 0).addBox(-3.5F, -6.9F, -3.5F, 7F, 7F, 7F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(0, 31).addBox(-3.5F, -7.65F, -3.45F, 7F, 2F, 7F, new CubeDeformation(-0.05F, -0.35F, -0.05F))
                .texOffs(87, 0).addBox(-3.7F, -6.8F, -2.5F, 1F, 6F, 6F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(102, 0).addBox(2.7F, -6.8F, -2.5F, 1F, 6F, 6F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offset(0F, -9.5F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(32, 41).addBox(0F, -2.2F, -0.4F, 0F, 4F, 4F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(3.1F, -3.8F, 0.2F, 0F, 0.5934F, 0F));
        PartDefinition p_locks_l = p_head.addOrReplaceChild("locks_l", CubeListBuilder.create()
                .texOffs(117, 0).addBox(-1F, 0F, -1F, 2F, 10F, 2F, new CubeDeformation(-0.45F, 0F, -0.1F)),
                PartPose.offsetAndRotation(3F, -5.6F, -1.4F, -0.1047F, 0F, -0.1047F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(41, 41).addBox(0F, -2.2F, -0.4F, 0F, 4F, 4F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-3.1F, -3.8F, 0.2F, 0F, -0.5934F, 0F));
        PartDefinition p_locks_r = p_head.addOrReplaceChild("locks_r", CubeListBuilder.create()
                .texOffs(126, 0).addBox(-1F, 0F, -1F, 2F, 10F, 2F, new CubeDeformation(-0.45F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-3F, -5.6F, -1.4F, -0.1047F, 0F, 0.1047F));
        PartDefinition p_hair_back = p_head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                .texOffs(68, 0).addBox(-3.5F, -0.25F, -0.85F, 7F, 10F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.35F)),
                PartPose.offsetAndRotation(0F, -6.2F, 2.9F, 0.1745F, 0F, 0F));
        PartDefinition p_hair_tip = p_hair_back.addOrReplaceChild("hair_tip", CubeListBuilder.create()
                .texOffs(69, 31).addBox(-3F, 0F, -0.4F, 6F, 8F, 1F),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_strand_0 = p_head.addOrReplaceChild("strand_0", CubeListBuilder.create()
                .texOffs(93, 31).addBox(-0.5F, -7.75F, -0.5F, 1F, 8F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-2.4F, -7F, 1.6F, -0.4887F, 0F, -0.6632F));
        PartDefinition p_strand_0_tip = p_strand_0.addOrReplaceChild("strand_0_tip", CubeListBuilder.create()
                .texOffs(153, 41).addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, -7.3F, 0F, 0.2793F, 0F, 0.2094F));
        PartDefinition p_strand_1 = p_head.addOrReplaceChild("strand_1", CubeListBuilder.create()
                .texOffs(162, 0).addBox(-0.5F, -8.75F, -0.5F, 1F, 9F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-0.9F, -7F, 2.4F, -0.6632F, 0F, -0.2094F));
        PartDefinition p_strand_1_tip = p_strand_1.addOrReplaceChild("strand_1_tip", CubeListBuilder.create()
                .texOffs(87, 41).addBox(-0.5F, -6.9F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -8.3F, 0F, -0.3142F, 0F, 0.2094F));
        PartDefinition p_strand_2 = p_head.addOrReplaceChild("strand_2", CubeListBuilder.create()
                .texOffs(98, 31).addBox(-0.5F, -8F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(0.9F, -7F, 2.3F, -0.6283F, 0F, 0.2443F));
        PartDefinition p_strand_2_tip = p_strand_2.addOrReplaceChild("strand_2_tip", CubeListBuilder.create()
                .texOffs(92, 41).addBox(-0.5F, -6.7F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -7.8F, 0F, 0.2793F, 0F, -0.2094F));
        PartDefinition p_strand_3 = p_head.addOrReplaceChild("strand_3", CubeListBuilder.create()
                .texOffs(97, 41).addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(2.5F, -7F, 1.4F, -0.4538F, 0F, 0.6981F));
        PartDefinition p_strand_3_tip = p_strand_3.addOrReplaceChild("strand_3_tip", CubeListBuilder.create()
                .texOffs(158, 41).addBox(-0.5F, -5.8F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0F, -6.8F, 0F, -0.3142F, 0F, -0.2094F));
        PartDefinition p_strand_4 = p_head.addOrReplaceChild("strand_4", CubeListBuilder.create()
                .texOffs(163, 41).addBox(-0.5F, -6F, -0.5F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(0F, -7F, 0.4F, -0.2094F, 0F, 0.0698F));
        PartDefinition p_strand_4_tip = p_strand_4.addOrReplaceChild("strand_4_tip", CubeListBuilder.create()
                .texOffs(22, 50).addBox(-0.5F, -4.9F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -5.8F, 0F, 0.2793F, 0F, -0.2094F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-3.5F, -0.5F, -3.7F, 7F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(140, 50).addBox(-1F, -1.6F, -4.05F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offset(0F, -7.2F, 0F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(88, 50).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(51, 57).addBox(0.05F, -1.76F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-2.9F, 0F, -2.4F, -0.4887F, 0F, -0.384F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(37, 50).addBox(-0.5F, -3.8F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(56, 57).addBox(-1.05F, -2.36F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-1.5F, 0F, -3.2F, -0.4538F, 0F, -0.1745F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(27, 50).addBox(-0.5F, -4.8F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(61, 57).addBox(0.05F, -2.96F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, -3.4F, -0.4189F, 0F, 0F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(42, 50).addBox(-0.5F, -3.8F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(66, 57).addBox(-1.05F, -2.36F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(1.5F, 0F, -3.2F, -0.4538F, 0F, 0.1745F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(93, 50).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(71, 57).addBox(0.05F, -1.76F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(2.9F, 0F, -2.4F, -0.4887F, 0F, 0.384F));
        PartDefinition p_crown_5 = p_crown.addOrReplaceChild("crown_5", CubeListBuilder.create()
                .texOffs(98, 50).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(76, 57).addBox(-1.05F, -1.52F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-3.1F, 0F, 0.2F, 0F, 0F, -0.5236F));
        PartDefinition p_crown_6 = p_crown.addOrReplaceChild("crown_6", CubeListBuilder.create()
                .texOffs(103, 50).addBox(-0.5F, -2.6F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(81, 57).addBox(0.05F, -1.52F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(3.1F, 0F, 0.2F, 0F, 0F, 0.5236F));
        PartDefinition p_left_arm = p_torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(135, 0).addBox(-1F, -0.8F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(52, 50).addBox(-1F, 7.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(152, 50).addBox(-0.5F, 9.1F, -1.175F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F))
                .texOffs(157, 50).addBox(-0.15F, 9.1F, -0.425F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F))
                .texOffs(162, 50).addBox(-0.5F, 9.1F, 0.325F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F))
                .texOffs(73, 41).addBox(0.8F, 2.3F, -0.4F, 0F, 5F, 3F, new CubeDeformation(0F, -0.3F, -0.2F))
                .texOffs(113, 50).addBox(-1F, 5.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.05F, -0.1F, -0.05F)),
                PartPose.offset(3.6F, -8.9F, 0F));
        PartDefinition p_right_arm = p_torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(144, 0).addBox(-1F, -0.8F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(61, 50).addBox(-1F, 7.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(167, 50).addBox(-0.5F, 9.1F, -1.175F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F))
                .texOffs(172, 50).addBox(-0.85F, 9.1F, -0.425F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F))
                .texOffs(177, 50).addBox(-0.5F, 9.1F, 0.325F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F))
                .texOffs(80, 41).addBox(-0.8F, 2.3F, -0.4F, 0F, 5F, 3F, new CubeDeformation(0F, -0.3F, -0.2F))
                .texOffs(122, 50).addBox(-1F, 5.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.05F, -0.1F, -0.05F)),
                PartPose.offset(-3.6F, -8.9F, 0F));
        PartDefinition p_staff = p_right_arm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -18F, -0.5F, 1F, 29F, 1F, new CubeDeformation(-0.05F, 0F, -0.05F))
                .texOffs(70, 50).addBox(-1F, -8.3F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(131, 50).addBox(-1F, 2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(164, 31).addBox(-2F, -22.5F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(0F, 8.6F, -0.1F));
        PartDefinition p_staff_prong_0 = p_staff.addOrReplaceChild("staff_prong_0", CubeListBuilder.create()
                .texOffs(168, 41).addBox(-0.5F, -5.8F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-0.75F, -17.8F, 0F, 0F, 0F, -0.384F));
        PartDefinition p_staff_prong_1 = p_staff.addOrReplaceChild("staff_prong_1", CubeListBuilder.create()
                .texOffs(173, 41).addBox(-0.5F, -5.8F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0.75F, -17.8F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_staff_prong_2 = p_staff.addOrReplaceChild("staff_prong_2", CubeListBuilder.create()
                .texOffs(178, 41).addBox(-0.5F, -5.8F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, -17.8F, 0.75F, 0.384F, 0F, 0F));
        PartDefinition p_staff_charm_0 = p_staff.addOrReplaceChild("staff_charm_0", CubeListBuilder.create()
                .texOffs(47, 50).addBox(-0.5F, -0.4F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.38F, -0.4F, -0.38F))
                .texOffs(30, 57).addBox(-1F, 3.2F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.45F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0.5F, -17.2F, -0.5F, 0F, 0F, -0.1396F));
        PartDefinition p_staff_charm_1 = p_staff.addOrReplaceChild("staff_charm_1", CubeListBuilder.create()
                .texOffs(32, 50).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.38F, -0.4F, -0.38F))
                .texOffs(37, 57).addBox(-1F, 4.2F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.45F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-0.5F, -17.2F, 0.4F, 0F, 0F, 0.1396F));
        PartDefinition p_staff_charm_2 = p_staff.addOrReplaceChild("staff_charm_2", CubeListBuilder.create()
                .texOffs(108, 50).addBox(-0.5F, -0.3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.38F, -0.3F, -0.38F))
                .texOffs(44, 57).addBox(-1F, 2.4F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.45F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0.4F, -17.2F, 0.5F, 0F, 0F, -0.1396F));
        PartDefinition p_tail1 = p_swim.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(102, 41).addBox(-2.5F, 0F, -1.5F, 5F, 4F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fan_l = p_tail1.addOrReplaceChild("fan_l", CubeListBuilder.create()
                .texOffs(5, 0).addBox(0F, -1F, -1.6F, 0F, 11F, 8F),
                PartPose.offsetAndRotation(2.4F, 1F, 0.4F, 0F, 0.2793F, -0.5934F));
        PartDefinition p_fan_r = p_tail1.addOrReplaceChild("fan_r", CubeListBuilder.create()
                .texOffs(22, 0).addBox(0F, -1F, -1.6F, 0F, 11F, 8F),
                PartPose.offsetAndRotation(-2.4F, 1F, 0.4F, 0F, -0.2793F, 0.5934F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-2.5F, -0.3F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_spines_0 = p_tail2.addOrReplaceChild("spines_0", CubeListBuilder.create()
                .texOffs(153, 0).addBox(0F, -5.6F, -0.4F, 0F, 6F, 4F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.4F, 1.2F, -1.0123F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(17, 41).addBox(-2F, -0.4F, -1.5F, 4F, 5F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 4.4F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_spines_1 = p_tail3.addOrReplaceChild("spines_1", CubeListBuilder.create()
                .texOffs(84, 31).addBox(0F, -4.7F, -0.4F, 0F, 5F, 4F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.4F, 1F, -0.9076F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, 0F, -1F, 3F, 4F, 2F, new CubeDeformation(0F, 0F, -0.05F)),
                PartPose.offsetAndRotation(0F, 4.2F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_spines_2 = p_tail4.addOrReplaceChild("spines_2", CubeListBuilder.create()
                .texOffs(50, 41).addBox(0F, -3.8F, -0.4F, 0F, 4F, 4F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0.4F, 0.85F, -0.8029F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(11, 50).addBox(-1.5F, -0.1F, -1F, 3F, 4F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fluke = p_tail5.addOrReplaceChild("fluke", CubeListBuilder.create()
                .texOffs(79, 50).addBox(-1F, -0.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 3.4F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_fluke_l = p_fluke.addOrReplaceChild("fluke_l", CubeListBuilder.create()
                .texOffs(119, 41).addBox(0F, -0.3F, 0F, 8F, 7F, 0F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0.4F, 0.2F, 0F, 0F, 0F, 0.5236F));
        PartDefinition p_fluke_r = p_fluke.addOrReplaceChild("fluke_r", CubeListBuilder.create()
                .texOffs(136, 41).addBox(-8F, -0.3F, 0F, 8F, 7F, 0F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(-0.4F, 0.2F, 0F, 0F, 0F, -0.5236F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
