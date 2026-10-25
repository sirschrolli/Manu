package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Drachenpanzer (gleiche Knochen wie das Drachen-Modell, nur Panzerplatten).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DragonArmorGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 128;

    private DragonArmorGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -10F, -15F, 18F, 9F, 28F, new CubeDeformation(0.7F))
                .texOffs(185, 38).addBox(-7F, -7F, -16F, 14F, 11F, 1F, new CubeDeformation(0.6F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(185, 0).addBox(-5.5F, -5.5F, -11F, 11F, 7F, 10F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0F, -3.5F, -14F, -0.6632F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(74, 38).addBox(-4.5F, -4.5F, -10F, 9F, 6F, 9F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.1396F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(152, 38).addBox(-4F, -4F, -9F, 8F, 5F, 8F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0F, 0F, -9F, 0.3142F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(111, 38).addBox(-5F, -6F, -12F, 10F, 3F, 10F, new CubeDeformation(0.5F))
                .texOffs(25, 56).addBox(-3.5F, -3F, -21F, 7F, 1F, 9F, new CubeDeformation(0.45F))
                .texOffs(150, 56).addBox(-0.5F, -5.5F, -16.5F, 1F, 2.5F, 3F)
                .texOffs(159, 56).addBox(-0.5F, -7.5F, -15.5F, 1F, 2F, 1.5F),
                PartPose.offsetAndRotation(0F, 0F, -8F, 0.4887F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create(),
                PartPose.offsetAndRotation(3.2F, -4.5F, -2F, 0.5236F, 0.2443F, 0F));
        PartDefinition p_horn_l2 = p_head.addOrReplaceChild("horn_l2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.6F, -1.5F, -1F, 0.1745F, 0.5585F, 0F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-3.2F, -4.5F, -2F, 0.5236F, -0.2443F, 0F));
        PartDefinition p_horn_r2 = p_head.addOrReplaceChild("horn_r2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.6F, -1.5F, -1F, 0.1745F, -0.5585F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(93, 0).addBox(-5F, -5F, 0F, 10F, 6F, 13F, new CubeDeformation(0.6F)),
                PartPose.offsetAndRotation(0F, -2.5F, 13.5F, -0.2443F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(140, 0).addBox(-4.5F, -4.5F, 0F, 9F, 5F, 13F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.1047F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 13F, 0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 12F, 0.1047F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.1047F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.1047F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(216, 38).addBox(-3F, -2.5F, -3F, 6F, 6F, 6F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(7.5F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(108, 56).addBox(-2.5F, 1F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0.45F)),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 6F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-4F, -3.5F, -5F, 8F, 7F, 10F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(8F, 1F, 9F, -0.1047F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(58, 56).addBox(-3F, 1F, -3F, 6F, 4F, 6F, new CubeDeformation(0.45F)),
                PartPose.offsetAndRotation(0F, 7F, 1.5F, 0.1745F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 6.5F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-3F, -2.5F, -3F, 6F, 6F, 6F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(-7.5F, 3F, -10F, 0.0698F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(129, 56).addBox(-2.5F, 1F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0.45F)),
                PartPose.offsetAndRotation(0F, 5.5F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 6F, 0F, 0.0349F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(37, 38).addBox(-4F, -3.5F, -5F, 8F, 7F, 10F, new CubeDeformation(0.5F)),
                PartPose.offsetAndRotation(-8F, 1F, 9F, -0.1047F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(83, 56).addBox(-3F, 1F, -3F, 6F, 4F, 6F, new CubeDeformation(0.45F)),
                PartPose.offsetAndRotation(0F, 7F, 1.5F, 0.1745F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 6.5F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create(),
                PartPose.offset(8F, -8F, -9F));
        PartDefinition p_wing_tip_l = p_wing_l.addOrReplaceChild("wing_tip_l", CubeListBuilder.create(),
                PartPose.offset(21F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create(),
                PartPose.offset(-8F, -8F, -9F));
        PartDefinition p_wing_tip_r = p_wing_r.addOrReplaceChild("wing_tip_r", CubeListBuilder.create(),
                PartPose.offset(-21F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
