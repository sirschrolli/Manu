package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Moorschnappers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MireSnapperGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private MireSnapperGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -2.3F, -6.5F, 11F, 4F, 13F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(49, 0).addBox(-5F, -3.85F, -6F, 10F, 2F, 12F, new CubeDeformation(-0.4F, -0.05F, -0.4F))
                .texOffs(45, 18).addBox(-3.5F, -4.6F, -4.5F, 7F, 1F, 9F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(0, 18).addBox(-5F, 1.25F, -6F, 10F, 1F, 12F, new CubeDeformation(-0.1F, -0.05F, -0.1F))
                .texOffs(70, 40).addBox(-1F, -6F, -4F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(79, 40).addBox(-1F, -6F, -1.8F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(88, 40).addBox(-1F, -6F, 0.4F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(97, 40).addBox(-1F, -6F, 2.6F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(0, 47).addBox(-3.4F, -4.8F, -3.3F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(9, 47).addBox(-3.4F, -4.8F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(18, 47).addBox(-3.4F, -4.8F, 1.3F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(27, 47).addBox(1.4F, -4.8F, -3.3F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(36, 47).addBox(1.4F, -4.8F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(45, 47).addBox(1.4F, -4.8F, 1.3F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(63, 47).addBox(-4.05F, -1.6F, 6.4F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(68, 47).addBox(-1.65F, -1.6F, 6.4F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(73, 47).addBox(0.75F, -1.6F, 6.4F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(78, 47).addBox(3.15F, -1.6F, 6.4F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(44, 40).addBox(-4F, -4.5F, -5.1F, 3F, 1F, 3F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(57, 40).addBox(1.4F, -4.5F, 2F, 3F, 1F, 3F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(54, 47).addBox(-3.6F, -5.15F, 2.6F, 2F, 1F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(106, 40).addBox(-6.05F, 1F, -2.25F, 1F, 3F, 1F, new CubeDeformation(-0.45F, -0.2F, -0.25F))
                .texOffs(111, 40).addBox(5.05F, 1.2F, 0.75F, 1F, 3F, 1F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(116, 40).addBox(-6.05F, 0.8F, 3.35F, 1F, 3F, 1F, new CubeDeformation(-0.45F, -0.4F, -0.25F))
                .texOffs(121, 40).addBox(1.95F, 0.9F, 6.35F, 1F, 3F, 1F, new CubeDeformation(-0.45F, -0.3F, -0.25F))
                .texOffs(83, 47).addBox(-2.65F, 1.1F, 6.35F, 1F, 2F, 1F, new CubeDeformation(-0.45F, -0.1F, -0.25F)),
                PartPose.offset(0F, 20.4F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2F, -1.6F, -3.4F, 4F, 3F, 4F, new CubeDeformation(-0.1F, 0F, -0.2F)),
                PartPose.offset(0F, -0.2F, -6F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(78, 18).addBox(-2.5F, -2.5F, -4.7F, 5F, 4F, 5F, new CubeDeformation(0F, -0.3F, -0.1F))
                .texOffs(33, 40).addBox(-1.5F, -2.25F, -6.25F, 3F, 3F, 2F, new CubeDeformation(0F, -0.45F, -0.25F))
                .texOffs(88, 47).addBox(-1F, 0.05F, -6.4F, 2F, 1F, 1F, new CubeDeformation(-0.1F, -0.05F, -0.1F))
                .texOffs(95, 47).addBox(1.95F, -1.5F, -3.5F, 1F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(100, 47).addBox(-2.95F, -1.5F, -3.5F, 1F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.1F)),
                PartPose.offset(0F, -0.2F, -3F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(99, 18).addBox(-2.5F, -0.45F, -4.9F, 5F, 2F, 5F, new CubeDeformation(-0.4F, -0.45F, -0.3F)),
                PartPose.offset(0F, 0.9F, -1F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(17, 32).addBox(-1.5F, -1F, -0.2F, 3F, 2F, 5F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(105, 47).addBox(-0.5F, -1.8F, 0.55F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F))
                .texOffs(110, 47).addBox(-0.5F, -1.8F, 1.95F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F))
                .texOffs(115, 47).addBox(-0.5F, -1.8F, 3.35F, 1F, 1F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F)),
                PartPose.offset(0F, -0.4F, 6.4F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(34, 32).addBox(-1F, -1F, -0.4F, 2F, 2F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.4F))
                .texOffs(120, 47).addBox(-0.5F, -1.4F, 0.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(0, 51).addBox(-0.5F, -1.4F, 1.8F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(5, 51).addBox(-0.5F, -1.4F, 3.1F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.1F)),
                PartPose.offset(0F, 0.1F, 4.5F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(49, 32).addBox(-1F, -1.3F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(109, 32).addBox(1F, 0.1F, -1.6F, 2F, 3F, 3F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(10, 51).addBox(0.925F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(15, 51).addBox(1.525F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(20, 51).addBox(2.125F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F)),
                PartPose.offset(4.4F, 21.2F, -3.8F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(64, 32).addBox(-1F, -1.3F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(0, 40).addBox(1F, 0.1F, -1.6F, 2F, 3F, 3F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(25, 51).addBox(0.925F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(30, 51).addBox(1.525F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(35, 51).addBox(2.125F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F)),
                PartPose.offset(4.4F, 21.2F, 3.9F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(79, 32).addBox(-3F, -1.3F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(11, 40).addBox(-3F, 0.1F, -1.6F, 2F, 3F, 3F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(40, 51).addBox(-1.925F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(45, 51).addBox(-2.525F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(50, 51).addBox(-3.125F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F)),
                PartPose.offset(-4.4F, 21.2F, -3.8F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(94, 32).addBox(-3F, -1.3F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(22, 40).addBox(-3F, 0.1F, -1.6F, 2F, 3F, 3F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(55, 51).addBox(-1.925F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(60, 51).addBox(-2.525F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F))
                .texOffs(65, 51).addBox(-3.125F, 1.975F, -2.35F, 1F, 1F, 1F, new CubeDeformation(-0.275F, -0.175F, -0.2F)),
                PartPose.offset(-4.4F, 21.2F, 3.9F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
