package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schneemanns (Knochen wie beim Vanilla-Schneegolem; Nase und Mütze nur ohne Kürbis sichtbar).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SnowmanGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private SnowmanGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(95, 43).addBox(-4F, -6.5F, -3F, 8F, 5F, 6F, new CubeDeformation(-0.1F, -0.082F, -0.192F))
                .texOffs(29, 43).addBox(-3F, -6.5F, -4F, 6F, 5F, 8F, new CubeDeformation(-0.192F, -0.082F, -0.1F))
                .texOffs(0, 43).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F, new CubeDeformation(-0.224F, -0.1F, -0.224F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(105, 59).addBox(-0.5F, -0.5F, -3.8F, 1F, 1F, 4F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(116, 59).addBox(-1F, -1F, -0.9F, 2F, 2F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, -4.2F, -3.6F, -0.1396F, 0F, 0F));
        PartDefinition p_cap = p_head.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-4F, -1.6F, -4F, 8F, 2F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(33, 59).addBox(-3.5F, -4.1F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(62, 59).addBox(-2.5F, -5.3F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(92, 59).addBox(-1.5F, -6.7F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offset(0F, -7.2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(21, 70).addBox(-0.75F, -0.5F, -0.5F, 12F, 1F, 1F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offsetAndRotation(5F, 6F, 1F, 0F, 0F, 1F));
        PartDefinition p_left_arm_twig = p_left_arm.addOrReplaceChild("left_arm_twig", CubeListBuilder.create()
                .texOffs(75, 70).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(6.5F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_left_arm_finger_a = p_left_arm.addOrReplaceChild("left_arm_finger_a", CubeListBuilder.create()
                .texOffs(97, 70).addBox(-0.4F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(10.6F, 0F, 0F, 0F, 0F, 0.5585F));
        PartDefinition p_left_arm_finger_b = p_left_arm.addOrReplaceChild("left_arm_finger_b", CubeListBuilder.create()
                .texOffs(115, 70).addBox(0F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(10.6F, 0F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(48, 70).addBox(-0.75F, -0.5F, -0.5F, 12F, 1F, 1F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offsetAndRotation(-5F, 6F, -1F, 0F, 3.1416F, -1F));
        PartDefinition p_right_arm_twig = p_right_arm.addOrReplaceChild("right_arm_twig", CubeListBuilder.create()
                .texOffs(86, 70).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(6.5F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_right_arm_finger_a = p_right_arm.addOrReplaceChild("right_arm_finger_a", CubeListBuilder.create()
                .texOffs(106, 70).addBox(-0.4F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(10.6F, 0F, 0F, 0F, 0F, 0.5585F));
        PartDefinition p_right_arm_finger_b = p_right_arm.addOrReplaceChild("right_arm_finger_b", CubeListBuilder.create()
                .texOffs(122, 70).addBox(0F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(10.6F, 0F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_upper_body = root.addOrReplaceChild("upper_body", CubeListBuilder.create()
                .texOffs(82, 25).addBox(-5F, -8.5F, -4F, 10F, 7F, 8F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(45, 25).addBox(-4F, -8.5F, -5F, 8F, 7F, 10F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(90, 0).addBox(-4.5F, -10F, -4.5F, 9F, 10F, 9F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(58, 43).addBox(-4.5F, -10.7F, -4.5F, 9F, 2F, 9F, new CubeDeformation(0F, 0.1F, 0F))
                .texOffs(83, 59).addBox(1F, -9.6F, -4.95F, 3F, 6F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.05F))
                .texOffs(0, 70).addBox(-1F, -8F, -5.55F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(7, 70).addBox(-1F, -5.8F, -5.55F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(14, 70).addBox(-1F, -3.6F, -5.55F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F)),
                PartPose.offset(0F, 13F, 0F));
        PartDefinition p_lower_body = root.addOrReplaceChild("lower_body", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-6.5F, -10.2F, -4.5F, 13F, 8F, 9F, new CubeDeformation(-0.3F, -0.156F, -0.036F))
                .texOffs(45, 0).addBox(-4.5F, -10.2F, -6.5F, 9F, 8F, 13F, new CubeDeformation(-0.036F, -0.156F, -0.3F))
                .texOffs(0, 0).addBox(-5.5F, -12.7F, -5.5F, 11F, 13F, 11F, new CubeDeformation(-0.292F, -0.3F, -0.292F)),
                PartPose.offset(0F, 24F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
