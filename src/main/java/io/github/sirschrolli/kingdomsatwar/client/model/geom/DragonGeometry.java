package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Drachen-Modells (ausgewachsene Größe).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DragonGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private DragonGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -9F, -16F, 18F, 15F, 30F)
                .texOffs(75, 106).addBox(-7F, -10.5F, -13F, 14F, 2F, 22F)
                .texOffs(0, 106).addBox(-7.5F, 5F, -14F, 15F, 2F, 22F)
                .texOffs(17, 201).addBox(-1F, -14.5F, -12F, 2F, 4.5F, 3F)
                .texOffs(39, 201).addBox(-1F, -13.5F, -5F, 2F, 3.5F, 3F)
                .texOffs(28, 201).addBox(-1F, -14.5F, 2F, 2F, 4.5F, 3F)
                .texOffs(50, 201).addBox(-1F, -13.5F, 9F, 2F, 3.5F, 3F)
                .texOffs(149, 46).addBox(0F, -16F, -14F, 0F, 6F, 26F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(0, 131).addBox(-5.5F, -5.5F, -11F, 11F, 11F, 11F)
                .texOffs(163, 201).addBox(-1F, -8.5F, -8F, 2F, 3F, 3F),
                PartPose.offsetAndRotation(0F, -3.5F, -14F, -0.6632F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(205, 131).addBox(-4.5F, -4.5F, -10F, 9F, 9F, 10F)
                .texOffs(174, 201).addBox(-1F, -7F, -7F, 2F, 2.5F, 3F),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.1396F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(37, 154).addBox(-4F, -4F, -9F, 8F, 8F, 9F),
                PartPose.offsetAndRotation(0F, 0F, -9F, 0.3142F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(45, 131).addBox(-5F, -5F, -12F, 10F, 9F, 12F)
                .texOffs(194, 154).addBox(-3.5F, -2.5F, -22F, 7F, 5F, 10F)
                .texOffs(205, 188).addBox(-4F, -6F, -9F, 8F, 1.5F, 6F)
                .texOffs(145, 188).addBox(-3F, 2.5F, -21.5F, 6F, 1F, 9F)
                .texOffs(216, 210).addBox(-2.5F, -3.5F, -21F, 1.5F, 1F, 2F)
                .texOffs(225, 210).addBox(1F, -3.5F, -21F, 1.5F, 1F, 2F)
                .texOffs(0, 210).addBox(4.4F, -2.5F, -10.5F, 1F, 2F, 3F)
                .texOffs(9, 210).addBox(-5.4F, -2.5F, -10.5F, 1F, 2F, 3F)
                .texOffs(72, 154).addBox(0F, -9F, -10F, 0F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 0F, -8F, 0.4887F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(155, 154).addBox(-3.5F, 0F, -11.5F, 7F, 2.5F, 11.5F)
                .texOffs(176, 188).addBox(-3F, -1F, -11F, 6F, 1F, 8F),
                PartPose.offsetAndRotation(0F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(79, 173).addBox(-1.25F, -1.25F, 0F, 2.5F, 2.5F, 10F),
                PartPose.offsetAndRotation(3.2F, -4.5F, -2F, 0.5236F, 0.2443F, 0F));
        PartDefinition p_horn_l2 = p_head.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(234, 188).addBox(-0.75F, -0.75F, 0F, 1.5F, 1.5F, 6F),
                PartPose.offsetAndRotation(4.6F, -1.5F, -1F, 0.1745F, 0.5585F, 0F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(106, 173).addBox(-1.25F, -1.25F, 0F, 2.5F, 2.5F, 10F),
                PartPose.offsetAndRotation(-3.2F, -4.5F, -2F, 0.5236F, -0.2443F, 0F));
        PartDefinition p_horn_r2 = p_head.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(0, 201).addBox(-0.75F, -0.75F, 0F, 1.5F, 1.5F, 6F),
                PartPose.offsetAndRotation(-4.6F, -1.5F, -1F, 0.1745F, -0.5585F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(148, 106).addBox(-5F, -5F, 0F, 10F, 10F, 14F)
                .texOffs(185, 201).addBox(-0.75F, -7.5F, 4.9F, 1.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0F, -2.5F, 13.5F, -0.2443F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(197, 106).addBox(-4.25F, -4.25F, 0F, 8.5F, 8.5F, 14F)
                .texOffs(196, 201).addBox(-0.75F, -6.75F, 4.9F, 1.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.1047F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(164, 131).addBox(-3.5F, -3.5F, 0F, 7F, 7F, 13F)
                .texOffs(207, 201).addBox(-0.75F, -6F, 4.55F, 1.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 154).addBox(-2.75F, -2.75F, 0F, 5.5F, 5.5F, 12F)
                .texOffs(218, 201).addBox(-0.75F, -5.25F, 4.2F, 1.5F, 2.5F, 2.5F)
                .texOffs(130, 154).addBox(0F, -6F, 0F, 0F, 4F, 12F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.1047F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(97, 154).addBox(-2.25F, -2.25F, 0F, 4.5F, 4.5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.1047F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(0, 173).addBox(-1.75F, -1.75F, 0F, 3.5F, 3.5F, 10F)
                .texOffs(100, 188).addBox(-6F, 0F, 7F, 12F, 0F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.1047F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(29, 173).addBox(-3F, -2.5F, -3F, 6F, 8F, 6F),
                PartPose.offsetAndRotation(7.5F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(0, 188).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(42, 188).addBox(-3F, 0F, -5.5F, 6F, 2.5F, 8F)
                .texOffs(132, 210).addBox(-2.5F, 1F, -7F, 1F, 1.5F, 2F)
                .texOffs(139, 210).addBox(-0.5F, 1F, -7F, 1F, 1.5F, 2F)
                .texOffs(146, 210).addBox(1.5F, 1F, -7F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 6F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(90, 131).addBox(-4F, -3.5F, -5F, 8F, 11F, 10F),
                PartPose.offsetAndRotation(8F, 1F, 9F, -0.1047F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(133, 173).addBox(-3F, 0F, -3F, 6F, 6.5F, 6F),
                PartPose.offsetAndRotation(0F, 7F, 1.5F, 0.1745F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(183, 173).addBox(-3.5F, 0F, -6.5F, 7F, 2.5F, 9F)
                .texOffs(153, 210).addBox(-3F, 1F, -8F, 1F, 1.5F, 2F)
                .texOffs(160, 210).addBox(-0.5F, 1F, -8F, 1F, 1.5F, 2F)
                .texOffs(167, 210).addBox(2F, 1F, -8F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 6.5F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(54, 173).addBox(-3F, -2.5F, -3F, 6F, 8F, 6F),
                PartPose.offsetAndRotation(-7.5F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(21, 188).addBox(-2.5F, 0F, -2.5F, 5F, 6.5F, 5F),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(71, 188).addBox(-3F, 0F, -5.5F, 6F, 2.5F, 8F)
                .texOffs(174, 210).addBox(-2.5F, 1F, -7F, 1F, 1.5F, 2F)
                .texOffs(181, 210).addBox(-0.5F, 1F, -7F, 1F, 1.5F, 2F)
                .texOffs(188, 210).addBox(1.5F, 1F, -7F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 6F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(127, 131).addBox(-4F, -3.5F, -5F, 8F, 11F, 10F),
                PartPose.offsetAndRotation(-8F, 1F, 9F, -0.1047F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(158, 173).addBox(-3F, 0F, -3F, 6F, 6.5F, 6F),
                PartPose.offsetAndRotation(0F, 7F, 1.5F, 0.1745F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(216, 173).addBox(-3.5F, 0F, -6.5F, 7F, 2.5F, 9F)
                .texOffs(195, 210).addBox(-3F, 1F, -8F, 1F, 1.5F, 2F)
                .texOffs(202, 210).addBox(-0.5F, 1F, -8F, 1F, 1.5F, 2F)
                .texOffs(209, 210).addBox(2F, 1F, -8F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 6.5F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(61, 201).addBox(0F, -1.5F, -1.5F, 22F, 3F, 3F)
                .texOffs(0, 81).addBox(0F, 0F, 1F, 22F, 0F, 24F),
                PartPose.offset(8F, -8F, -9F));
        PartDefinition p_wing_tip_l = p_wing_l.addOrReplaceChild("wing_tip_l", CubeListBuilder.create()
                .texOffs(18, 210).addBox(0F, -1F, -1F, 26F, 2F, 2F)
                .texOffs(97, 0).addBox(0F, 0F, 1F, 40F, 0F, 34F)
                .texOffs(229, 201).addBox(25F, -2F, -2F, 2F, 3F, 3F),
                PartPose.offset(21F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(112, 201).addBox(-22F, -1.5F, -1.5F, 22F, 3F, 3F)
                .texOffs(93, 81).addBox(-22F, 0F, 1F, 22F, 0F, 24F),
                PartPose.offset(-8F, -8F, -9F));
        PartDefinition p_wing_tip_r = p_wing_r.addOrReplaceChild("wing_tip_r", CubeListBuilder.create()
                .texOffs(75, 210).addBox(-26F, -1F, -1F, 26F, 2F, 2F)
                .texOffs(0, 46).addBox(-40F, 0F, 1F, 40F, 0F, 34F)
                .texOffs(240, 201).addBox(-27F, -2F, -2F, 2F, 3F, 3F),
                PartPose.offset(-21F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
