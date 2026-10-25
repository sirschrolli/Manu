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
                .texOffs(0, 0).addBox(-8F, -8F, -16F, 16F, 14F, 30F)
                .texOffs(186, 80).addBox(-6.5F, -9.5F, -13F, 13F, 2F, 22F)
                .texOffs(63, 180).addBox(-1F, -13.5F, -12F, 2F, 4.5F, 3F)
                .texOffs(85, 180).addBox(-1F, -12.5F, -5F, 2F, 3.5F, 3F)
                .texOffs(74, 180).addBox(-1F, -13.5F, 2F, 2F, 4.5F, 3F)
                .texOffs(96, 180).addBox(-1F, -12.5F, 9F, 2F, 3.5F, 3F)
                .texOffs(149, 45).addBox(0F, -15F, -14F, 0F, 6F, 26F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(207, 105).addBox(-4.5F, -4.5F, -11F, 9F, 9F, 11F)
                .texOffs(209, 180).addBox(-1F, -7.5F, -8F, 2F, 3F, 3F),
                PartPose.offsetAndRotation(0F, -3F, -15F, -0.6632F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(41, 130).addBox(-4F, -4F, -10F, 8F, 8F, 10F)
                .texOffs(220, 180).addBox(-1F, -6.5F, -7F, 2F, 2.5F, 3F),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.1396F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(140, 130).addBox(-3.5F, -3.5F, -9F, 7F, 7F, 9F),
                PartPose.offsetAndRotation(0F, 0F, -9F, 0.3142F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(96, 105).addBox(-5F, -5F, -12F, 10F, 9F, 12F)
                .texOffs(39, 151).addBox(-3.5F, -2.5F, -22F, 7F, 5F, 10F)
                .texOffs(0, 180).addBox(-4F, -6F, -9F, 8F, 1.5F, 6F)
                .texOffs(137, 167).addBox(-3F, 2.5F, -21.5F, 6F, 1F, 9F)
                .texOffs(7, 196).addBox(-2.5F, -3.5F, -21F, 1.5F, 1F, 2F)
                .texOffs(16, 196).addBox(1F, -3.5F, -21F, 1.5F, 1F, 2F)
                .texOffs(44, 189).addBox(4.4F, -2.5F, -10.5F, 1F, 2F, 3F)
                .texOffs(53, 189).addBox(-5.4F, -2.5F, -10.5F, 1F, 2F, 3F)
                .texOffs(115, 130).addBox(0F, -9F, -10F, 0F, 5F, 12F),
                PartPose.offsetAndRotation(0F, 0F, -8F, 0.4887F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 151).addBox(-3.5F, 0F, -11.5F, 7F, 2.5F, 11.5F)
                .texOffs(218, 167).addBox(-3F, -1F, -11F, 6F, 1F, 8F),
                PartPose.offsetAndRotation(0F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(187, 151).addBox(-1.25F, -1.25F, 0F, 2.5F, 2.5F, 10F),
                PartPose.offsetAndRotation(3.2F, -4.5F, -2F, 0.5236F, 0.2443F, 0F));
        PartDefinition p_horn_l2 = p_head.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(29, 180).addBox(-0.75F, -0.75F, 0F, 1.5F, 1.5F, 6F),
                PartPose.offsetAndRotation(4.6F, -1.5F, -1F, 0.1745F, 0.5585F, 0F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(214, 151).addBox(-1.25F, -1.25F, 0F, 2.5F, 2.5F, 10F),
                PartPose.offsetAndRotation(-3.2F, -4.5F, -2F, 0.5236F, -0.2443F, 0F));
        PartDefinition p_horn_r2 = p_head.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(46, 180).addBox(-0.75F, -0.75F, 0F, 1.5F, 1.5F, 6F),
                PartPose.offsetAndRotation(-4.6F, -1.5F, -1F, 0.1745F, -0.5585F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 105).addBox(-5F, -5F, 0F, 10F, 10F, 14F)
                .texOffs(231, 180).addBox(-0.75F, -7.5F, 4.9F, 1.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0F, -2F, 13.5F, -0.2443F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(49, 105).addBox(-4.25F, -4.25F, 0F, 8.5F, 8.5F, 14F)
                .texOffs(242, 180).addBox(-0.75F, -6.75F, 4.9F, 1.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.1047F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 130).addBox(-3.5F, -3.5F, 0F, 7F, 7F, 13F)
                .texOffs(0, 189).addBox(-0.75F, -6F, 4.55F, 1.5F, 2.5F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(78, 130).addBox(-2.75F, -2.75F, 0F, 5.5F, 5.5F, 12F)
                .texOffs(11, 189).addBox(-0.75F, -5.25F, 4.2F, 1.5F, 2.5F, 2.5F)
                .texOffs(206, 130).addBox(0F, -6F, 0F, 0F, 4F, 12F),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.1047F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(173, 130).addBox(-2.25F, -2.25F, 0F, 4.5F, 4.5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.1047F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(116, 151).addBox(-1.75F, -1.75F, 0F, 3.5F, 3.5F, 10F)
                .texOffs(92, 167).addBox(-6F, 0F, 7F, 12F, 0F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.1047F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(74, 151).addBox(-2.5F, -2F, -2.5F, 5F, 10F, 5F),
                PartPose.offsetAndRotation(7.5F, 2F, -10F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(0, 167).addBox(-2F, 0F, -2F, 4F, 8F, 4F),
                PartPose.offsetAndRotation(0F, 8F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(168, 167).addBox(-2.5F, 0F, -5F, 5F, 2.5F, 7F)
                .texOffs(176, 189).addBox(-2F, 1F, -6.5F, 1F, 1.5F, 2F)
                .texOffs(183, 189).addBox(-0.5F, 1F, -6.5F, 1F, 1.5F, 2F)
                .texOffs(190, 189).addBox(1F, 1F, -6.5F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 7.5F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(141, 105).addBox(-3.5F, -3F, -4.5F, 7F, 12F, 9F),
                PartPose.offsetAndRotation(7.5F, 0F, 9F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(145, 151).addBox(-2.5F, 0F, -2.5F, 5F, 9F, 5F),
                PartPose.offsetAndRotation(0F, 8F, 2F, 0.5585F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(34, 167).addBox(-3F, 0F, -6F, 6F, 2.5F, 8F)
                .texOffs(197, 189).addBox(-2.5F, 1F, -7.5F, 1F, 1.5F, 2F)
                .texOffs(204, 189).addBox(-0.5F, 1F, -7.5F, 1F, 1.5F, 2F)
                .texOffs(211, 189).addBox(1.5F, 1F, -7.5F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 8F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(95, 151).addBox(-2.5F, -2F, -2.5F, 5F, 10F, 5F),
                PartPose.offsetAndRotation(-7.5F, 2F, -10F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(17, 167).addBox(-2F, 0F, -2F, 4F, 8F, 4F),
                PartPose.offsetAndRotation(0F, 8F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(193, 167).addBox(-2.5F, 0F, -5F, 5F, 2.5F, 7F)
                .texOffs(218, 189).addBox(-2F, 1F, -6.5F, 1F, 1.5F, 2F)
                .texOffs(225, 189).addBox(-0.5F, 1F, -6.5F, 1F, 1.5F, 2F)
                .texOffs(232, 189).addBox(1F, 1F, -6.5F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 7.5F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(174, 105).addBox(-3.5F, -3F, -4.5F, 7F, 12F, 9F),
                PartPose.offsetAndRotation(-7.5F, 0F, 9F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(166, 151).addBox(-2.5F, 0F, -2.5F, 5F, 9F, 5F),
                PartPose.offsetAndRotation(0F, 8F, 2F, 0.5585F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(63, 167).addBox(-3F, 0F, -6F, 6F, 2.5F, 8F)
                .texOffs(239, 189).addBox(-2.5F, 1F, -7.5F, 1F, 1.5F, 2F)
                .texOffs(246, 189).addBox(-0.5F, 1F, -7.5F, 1F, 1.5F, 2F)
                .texOffs(0, 196).addBox(1.5F, 1F, -7.5F, 1F, 1.5F, 2F),
                PartPose.offsetAndRotation(0F, 8F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(107, 180).addBox(0F, -1.5F, -1.5F, 22F, 3F, 3F)
                .texOffs(0, 80).addBox(0F, 0F, 1F, 22F, 0F, 24F),
                PartPose.offset(7F, -7F, -9F));
        PartDefinition p_wing_tip_l = p_wing_l.addOrReplaceChild("wing_tip_l", CubeListBuilder.create()
                .texOffs(62, 189).addBox(0F, -1F, -1F, 26F, 2F, 2F)
                .texOffs(93, 0).addBox(0F, 0F, 1F, 40F, 0F, 34F)
                .texOffs(22, 189).addBox(25F, -2F, -2F, 2F, 3F, 3F),
                PartPose.offset(21F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(158, 180).addBox(-22F, -1.5F, -1.5F, 22F, 3F, 3F)
                .texOffs(93, 80).addBox(-22F, 0F, 1F, 22F, 0F, 24F),
                PartPose.offset(-7F, -7F, -9F));
        PartDefinition p_wing_tip_r = p_wing_r.addOrReplaceChild("wing_tip_r", CubeListBuilder.create()
                .texOffs(119, 189).addBox(-26F, -1F, -1F, 26F, 2F, 2F)
                .texOffs(0, 45).addBox(-40F, 0F, 1F, 40F, 0F, 34F)
                .texOffs(33, 189).addBox(-27F, -2F, -2F, 2F, 3F, 3F),
                PartPose.offset(-21F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
