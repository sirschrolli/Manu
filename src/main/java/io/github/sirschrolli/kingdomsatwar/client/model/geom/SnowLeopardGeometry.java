package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schneeleoparden (Frostgipfel).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SnowLeopardGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private SnowLeopardGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-3.5F, -4.35F, -10F, 7F, 8F, 10F, new CubeDeformation(-0.1F, -0.05F, -0.4F))
                .texOffs(80, 0).addBox(-3F, -3.75F, -1.2F, 6F, 7F, 10F, new CubeDeformation(0F, -0.05F, -0.2F)),
                PartPose.offset(0F, 13F, 0F));
        PartDefinition p_belly_fur = p_body.addOrReplaceChild("belly_fur", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, 2.3F, -8.6F, 6F, 2F, 16F, new CubeDeformation(-0.3F, -0.1F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(65, 19).addBox(-2.5F, -3.1F, -5F, 5F, 6F, 6F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -1.6F, -9F, -0.2793F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(38, 19).addBox(-3.5F, -3.5F, -5.6F, 7F, 6F, 6F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(103, 44).addBox(-2F, -0.85F, -8.15F, 4F, 3F, 3F, new CubeDeformation(0F, -0.05F, -0.25F))
                .texOffs(29, 53).addBox(-1F, -1.1F, -8.4F, 2F, 1F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.35F))
                .texOffs(36, 53).addBox(0.7F, -2.45F, -6.125F, 2F, 1F, 1F, new CubeDeformation(-0.25F, 0F, -0.425F))
                .texOffs(43, 53).addBox(-2.7F, -2.45F, -6.125F, 2F, 1F, 1F, new CubeDeformation(-0.25F, 0F, -0.425F))
                .texOffs(13, 44).addBox(-3.9F, -1.6F, -4.7F, 1F, 4F, 4F, new CubeDeformation(-0.15F, -0.2F, -0.2F))
                .texOffs(24, 44).addBox(2.9F, -1.6F, -4.7F, 1F, 4F, 4F, new CubeDeformation(-0.15F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.9F, -4.2F, 0.2793F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-2F, 0F, -2.7F, 4F, 1F, 3F, new CubeDeformation(-0.4F, 0F, -0.1F)),
                PartPose.offset(0F, 2F, -5.1F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(15, 53).addBox(-0.95F, -1.8F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.1F, 0F)),
                PartPose.offsetAndRotation(2.2F, -3.2F, -1.9F, 0F, 0F, 0.2793F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(22, 53).addBox(-1.05F, -1.8F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.1F, 0F)),
                PartPose.offsetAndRotation(-2.2F, -3.2F, -1.9F, 0F, 0F, -0.2793F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-1.5F, -1.5F, -0.4F, 3F, 3F, 7F, new CubeDeformation(-0.15F, -0.15F, 0F)),
                PartPose.offsetAndRotation(0F, -2.5F, 8.2F, -0.733F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(21, 33).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 6.4F, 0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(42, 33).addBox(-1.5F, -1.5F, -0.7F, 3F, 3F, 7F, new CubeDeformation(-0.18F, -0.18F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 6.2F, 0.2443F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(63, 33).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 6F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 5.9F, 0.6283F, 0F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(88, 19).addBox(-2F, -1.45F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.1F)),
                PartPose.offset(2.3F, 12.4F, -6.6F));
        PartDefinition p_left_front_shin = p_left_front_leg.addOrReplaceChild("left_front_shin", CubeListBuilder.create()
                .texOffs(108, 33).addBox(-1.5F, -0.15F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offset(0F, 6.1F, -0.2F));
        PartDefinition p_left_front_paw = p_left_front_shin.addOrReplaceChild("left_front_paw", CubeListBuilder.create()
                .texOffs(35, 44).addBox(-2F, -0.45F, -2.7F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.45F, -0.1F)),
                PartPose.offset(0F, 4.4F, 0F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-2F, -1.7F, -2.6F, 4F, 8F, 5F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(2.4F, 12F, 6F));
        PartDefinition p_left_hind_shin = p_left_hind_leg.addOrReplaceChild("left_hind_shin", CubeListBuilder.create()
                .texOffs(82, 33).addBox(-1.5F, -0.25F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.15F)),
                PartPose.offset(0F, 5.6F, 1F));
        PartDefinition p_left_hind_paw = p_left_hind_shin.addOrReplaceChild("left_hind_paw", CubeListBuilder.create()
                .texOffs(52, 44).addBox(-2F, -0.45F, -2.75F, 4F, 2F, 4F, new CubeDeformation(-0.25F, -0.45F, -0.15F)),
                PartPose.offset(0F, 5.3F, 0F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(105, 19).addBox(-2F, -1.45F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.1F)),
                PartPose.offset(-2.3F, 12.4F, -6.6F));
        PartDefinition p_right_front_shin = p_right_front_leg.addOrReplaceChild("right_front_shin", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-1.5F, -0.15F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offset(0F, 6.1F, -0.2F));
        PartDefinition p_right_front_paw = p_right_front_shin.addOrReplaceChild("right_front_paw", CubeListBuilder.create()
                .texOffs(69, 44).addBox(-2F, -0.45F, -2.7F, 4F, 2F, 4F, new CubeDeformation(-0.2F, -0.45F, -0.1F)),
                PartPose.offset(0F, 4.4F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(19, 19).addBox(-2F, -1.7F, -2.6F, 4F, 8F, 5F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(-2.4F, 12F, 6F));
        PartDefinition p_right_hind_shin = p_right_hind_leg.addOrReplaceChild("right_hind_shin", CubeListBuilder.create()
                .texOffs(95, 33).addBox(-1.5F, -0.25F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.15F)),
                PartPose.offset(0F, 5.6F, 1F));
        PartDefinition p_right_hind_paw = p_right_hind_shin.addOrReplaceChild("right_hind_paw", CubeListBuilder.create()
                .texOffs(86, 44).addBox(-2F, -0.45F, -2.75F, 4F, 2F, 4F, new CubeDeformation(-0.25F, -0.45F, -0.15F)),
                PartPose.offset(0F, 5.3F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
