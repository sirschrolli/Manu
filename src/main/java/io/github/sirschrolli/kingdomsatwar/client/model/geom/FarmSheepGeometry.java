package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schafs (Wolle als eigene Knochen, eingefärbt im Renderer).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FarmSheepGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private FarmSheepGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(57, 0).addBox(-4F, -4F, -7.5F, 8F, 8F, 15F),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_wool_body = p_body.addOrReplaceChild("wool_body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -6F, -8.5F, 11F, 10.5F, 17F)
                .texOffs(0, 29).addBox(-4.5F, -7F, -7F, 9F, 1.5F, 13F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(45, 29).addBox(-2.5F, -3.5F, -6.5F, 5F, 5.5F, 6.5F)
                .texOffs(0, 45).addBox(-2F, -1F, -8.5F, 4F, 3.5F, 2.5F)
                .texOffs(41, 53).addBox(2.5F, -2.2F, -5.5F, 0.5F, 1F, 1F)
                .texOffs(46, 53).addBox(-3F, -2.2F, -5.5F, 0.5F, 1F, 1F),
                PartPose.offsetAndRotation(0F, -3F, -8.5F, 0.2094F, 0F, 0F));
        PartDefinition p_wool_head = p_head.addOrReplaceChild("wool_head", CubeListBuilder.create()
                .texOffs(106, 29).addBox(-3F, -4.5F, -5F, 6F, 2.5F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(9, 53).addBox(0F, -0.5F, -0.75F, 3F, 1F, 1.5F),
                PartPose.offsetAndRotation(2.4F, -2.6F, -2.5F, 0F, 0F, 0.8727F));
        PartDefinition p_horn_l = p_head.addOrReplaceChild("horn_l", CubeListBuilder.create()
                .texOffs(76, 45).addBox(-0.8F, -2.5F, -0.8F, 1.6F, 2.5F, 1.6F),
                PartPose.offsetAndRotation(2F, -3.6F, -2.5F, -0.5236F, 0F, 0.5236F));
        PartDefinition p_horn_mid_l = p_horn_l.addOrReplaceChild("horn_mid_l", CubeListBuilder.create()
                .texOffs(85, 45).addBox(-0.7F, -2.6F, -0.7F, 1.4F, 2.6F, 1.4F),
                PartPose.offsetAndRotation(0F, -2.3F, 0F, -1.6581F, 0F, 0F));
        PartDefinition p_horn_low_l = p_horn_mid_l.addOrReplaceChild("horn_low_l", CubeListBuilder.create()
                .texOffs(94, 45).addBox(-0.6F, -2.4F, -0.6F, 1.2F, 2.4F, 1.2F),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, -1.3963F, 0F, -0.1745F));
        PartDefinition p_horn_tip_l = p_horn_low_l.addOrReplaceChild("horn_tip_l", CubeListBuilder.create()
                .texOffs(31, 53).addBox(-0.5F, -1.8F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, -1.0472F, 0F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(20, 53).addBox(-3F, -0.5F, -0.75F, 3F, 1F, 1.5F),
                PartPose.offsetAndRotation(-2.4F, -2.6F, -2.5F, 0F, 0F, -0.8727F));
        PartDefinition p_horn_r = p_head.addOrReplaceChild("horn_r", CubeListBuilder.create()
                .texOffs(103, 45).addBox(-0.8F, -2.5F, -0.8F, 1.6F, 2.5F, 1.6F),
                PartPose.offsetAndRotation(-2F, -3.6F, -2.5F, -0.5236F, 0F, -0.5236F));
        PartDefinition p_horn_mid_r = p_horn_r.addOrReplaceChild("horn_mid_r", CubeListBuilder.create()
                .texOffs(112, 45).addBox(-0.7F, -2.6F, -0.7F, 1.4F, 2.6F, 1.4F),
                PartPose.offsetAndRotation(0F, -2.3F, 0F, -1.6581F, 0F, 0F));
        PartDefinition p_horn_low_r = p_horn_mid_r.addOrReplaceChild("horn_low_r", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-0.6F, -2.4F, -0.6F, 1.2F, 2.4F, 1.2F),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, -1.3963F, 0F, 0.1745F));
        PartDefinition p_horn_tip_r = p_horn_low_r.addOrReplaceChild("horn_tip_r", CubeListBuilder.create()
                .texOffs(36, 53).addBox(-0.5F, -1.8F, -0.5F, 1F, 1.8F, 1F),
                PartPose.offsetAndRotation(0F, -2.2F, 0F, -1.0472F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(67, 45).addBox(-1F, 0F, -0.5F, 2F, 3.5F, 1.5F),
                PartPose.offsetAndRotation(0F, -3F, 7.5F, 0.4363F, 0F, 0F));
        PartDefinition p_front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(70, 29).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(2.2F, 16F, -5F));
        PartDefinition p_wool_front_left_leg = p_front_left_leg.addOrReplaceChild("wool_front_left_leg", CubeListBuilder.create()
                .texOffs(15, 45).addBox(-1.5F, -0.5F, -1.5F, 3F, 3.5F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(79, 29).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-2.2F, 16F, -5F));
        PartDefinition p_wool_front_right_leg = p_front_right_leg.addOrReplaceChild("wool_front_right_leg", CubeListBuilder.create()
                .texOffs(28, 45).addBox(-1.5F, -0.5F, -1.5F, 3F, 3.5F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(88, 29).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(2.2F, 16F, 5F));
        PartDefinition p_wool_hind_left_leg = p_hind_left_leg.addOrReplaceChild("wool_hind_left_leg", CubeListBuilder.create()
                .texOffs(41, 45).addBox(-1.5F, -0.5F, -1.5F, 3F, 3.5F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(97, 29).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-2.2F, 16F, 5F));
        PartDefinition p_wool_hind_right_leg = p_hind_right_leg.addOrReplaceChild("wool_hind_right_leg", CubeListBuilder.create()
                .texOffs(54, 45).addBox(-1.5F, -0.5F, -1.5F, 3F, 3.5F, 3F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
