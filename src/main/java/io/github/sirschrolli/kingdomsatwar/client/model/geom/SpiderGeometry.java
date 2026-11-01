package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Spinnen (Vanilla-Knochen, geknickte Beine als Kind-Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SpiderGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private SpiderGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-4F, -4F, -8F, 8F, 8F, 8F),
                PartPose.offset(0F, 15F, -3F));
        PartDefinition p_chelicerae = p_head.addOrReplaceChild("chelicerae", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-2.6F, 1.5F, -9.2F, 2F, 3F, 2F)
                .texOffs(9, 21).addBox(0.6F, 1.5F, -9.2F, 2F, 3F, 2F)
                .texOffs(51, 42).addBox(-1.8F, 4.5F, -9.4F, 1F, 1F, 1F)
                .texOffs(56, 42).addBox(0.8F, 4.5F, -9.4F, 1F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_palp = p_head.addOrReplaceChild("right_palp", CubeListBuilder.create()
                .texOffs(103, 0).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(-3F, 2.5F, -7.5F, 0.4887F, 0.2443F, 0F));
        PartDefinition p_left_palp = p_head.addOrReplaceChild("left_palp", CubeListBuilder.create()
                .texOffs(114, 0).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(3F, 2.5F, -7.5F, 0.4887F, -0.2443F, 0F));
        PartDefinition p_body0 = root.addOrReplaceChild("body0", CubeListBuilder.create()
                .texOffs(78, 0).addBox(-3F, -3F, -3F, 6F, 6F, 6F),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_body1 = root.addOrReplaceChild("body1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -4F, -6F, 10F, 8F, 12F),
                PartPose.offset(0F, 15F, 9F));
        PartDefinition p_spinnerets = p_body1.addOrReplaceChild("spinnerets", CubeListBuilder.create()
                .texOffs(44, 42).addBox(-1F, 0.5F, 5.6F, 2F, 2F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(84, 37).addBox(-3F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(-4F, 15F, 2F, 0F, 0.7854F, -0.7854F));
        PartDefinition p_right_hind_femur = p_right_hind_leg.addOrReplaceChild("right_hind_femur", CubeListBuilder.create()
                .texOffs(31, 32).addBox(-9F, -1F, -1F, 9F, 2F, 2F),
                PartPose.offsetAndRotation(-2.5F, 0F, 0F, 0F, 0F, 1.0821F));
        PartDefinition p_right_hind_tibia = p_right_hind_femur.addOrReplaceChild("right_hind_tibia", CubeListBuilder.create()
                .texOffs(18, 21).addBox(-13F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0F, -1.7453F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(95, 37).addBox(0F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(4F, 15F, 2F, 0F, -0.7854F, 0.7854F));
        PartDefinition p_left_hind_femur = p_left_hind_leg.addOrReplaceChild("left_hind_femur", CubeListBuilder.create()
                .texOffs(54, 32).addBox(0F, -1F, -1F, 9F, 2F, 2F),
                PartPose.offsetAndRotation(2.5F, 0F, 0F, 0F, 0F, -1.0821F));
        PartDefinition p_left_hind_tibia = p_left_hind_femur.addOrReplaceChild("left_hind_tibia", CubeListBuilder.create()
                .texOffs(49, 21).addBox(0F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, 0F, 1.7453F));
        PartDefinition p_right_middle_hind_leg = root.addOrReplaceChild("right_middle_hind_leg", CubeListBuilder.create()
                .texOffs(106, 37).addBox(-3F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(-4F, 15F, 1F, 0F, 0.3927F, -0.5812F));
        PartDefinition p_right_middle_hind_femur = p_right_middle_hind_leg.addOrReplaceChild("right_middle_hind_femur", CubeListBuilder.create()
                .texOffs(77, 32).addBox(-9F, -1F, -1F, 9F, 2F, 2F),
                PartPose.offsetAndRotation(-2.5F, 0F, 0F, 0F, 0F, 1.117F));
        PartDefinition p_right_middle_hind_tibia = p_right_middle_hind_femur.addOrReplaceChild("right_middle_hind_tibia", CubeListBuilder.create()
                .texOffs(80, 21).addBox(-13F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, 0F, -1.885F));
        PartDefinition p_left_middle_hind_leg = root.addOrReplaceChild("left_middle_hind_leg", CubeListBuilder.create()
                .texOffs(117, 37).addBox(0F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(4F, 15F, 1F, 0F, -0.3927F, 0.5812F));
        PartDefinition p_left_middle_hind_femur = p_left_middle_hind_leg.addOrReplaceChild("left_middle_hind_femur", CubeListBuilder.create()
                .texOffs(100, 32).addBox(0F, -1F, -1F, 9F, 2F, 2F),
                PartPose.offsetAndRotation(2.5F, 0F, 0F, 0F, 0F, -1.117F));
        PartDefinition p_left_middle_hind_tibia = p_left_middle_hind_femur.addOrReplaceChild("left_middle_hind_tibia", CubeListBuilder.create()
                .texOffs(0, 27).addBox(0F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, 0F, 1.885F));
        PartDefinition p_right_middle_front_leg = root.addOrReplaceChild("right_middle_front_leg", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-3F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(-4F, 15F, 0F, 0F, -0.3927F, -0.5812F));
        PartDefinition p_right_middle_front_femur = p_right_middle_front_leg.addOrReplaceChild("right_middle_front_femur", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-8F, -1F, -1F, 8F, 2F, 2F),
                PartPose.offsetAndRotation(-2.5F, 0F, 0F, 0F, 0F, 1.2566F));
        PartDefinition p_right_middle_front_tibia = p_right_middle_front_femur.addOrReplaceChild("right_middle_front_tibia", CubeListBuilder.create()
                .texOffs(31, 27).addBox(-13F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, 0F, -2.0595F));
        PartDefinition p_left_middle_front_leg = root.addOrReplaceChild("left_middle_front_leg", CubeListBuilder.create()
                .texOffs(11, 42).addBox(0F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(4F, 15F, 0F, 0F, 0.3927F, 0.5812F));
        PartDefinition p_left_middle_front_femur = p_left_middle_front_leg.addOrReplaceChild("left_middle_front_femur", CubeListBuilder.create()
                .texOffs(21, 37).addBox(0F, -1F, -1F, 8F, 2F, 2F),
                PartPose.offsetAndRotation(2.5F, 0F, 0F, 0F, 0F, -1.2566F));
        PartDefinition p_left_middle_front_tibia = p_left_middle_front_femur.addOrReplaceChild("left_middle_front_tibia", CubeListBuilder.create()
                .texOffs(62, 27).addBox(0F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, 0F, 2.0595F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(22, 42).addBox(-3F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(-4F, 15F, -1F, 0F, -0.7854F, -0.7854F));
        PartDefinition p_right_front_femur = p_right_front_leg.addOrReplaceChild("right_front_femur", CubeListBuilder.create()
                .texOffs(42, 37).addBox(-8F, -1F, -1F, 8F, 2F, 2F),
                PartPose.offsetAndRotation(-2.5F, 0F, 0F, 0F, 0F, 1.2217F));
        PartDefinition p_right_front_tibia = p_right_front_femur.addOrReplaceChild("right_front_tibia", CubeListBuilder.create()
                .texOffs(93, 27).addBox(-13F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-8F, 0F, 0F, 0F, 0F, -2.1991F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(33, 42).addBox(0F, -1F, -1F, 3F, 2F, 2F),
                PartPose.offsetAndRotation(4F, 15F, -1F, 0F, 0.7854F, 0.7854F));
        PartDefinition p_left_front_femur = p_left_front_leg.addOrReplaceChild("left_front_femur", CubeListBuilder.create()
                .texOffs(63, 37).addBox(0F, -1F, -1F, 8F, 2F, 2F),
                PartPose.offsetAndRotation(2.5F, 0F, 0F, 0F, 0F, -1.2217F));
        PartDefinition p_left_front_tibia = p_left_front_femur.addOrReplaceChild("left_front_tibia", CubeListBuilder.create()
                .texOffs(0, 32).addBox(0F, -0.75F, -0.75F, 13F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(8F, 0F, 0F, 0F, 0F, 2.1991F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
