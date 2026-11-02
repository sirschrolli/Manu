package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Borstlinge, Borstenhünen und Moderborstlinge (Knochen wie beim Vanilla-Piglin; Helm und Panzer nur beim Hünen bemalt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BorstlingGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private BorstlingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -8F, -4F, 10F, 8F, 8F)
                .texOffs(98, 45).addBox(-2.5F, -4.05F, -6F, 5F, 4F, 2F, new CubeDeformation(0F, -0.15F, 0F))
                .texOffs(43, 55).addBox(-5.5F, -7.55F, -4.6F, 11F, 2F, 1F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(33, 55).addBox(-3.6F, -3F, -5.6F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(38, 55).addBox(2.6F, -3F, -5.6F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(68, 55).addBox(-1F, -1.2F, -6.5F, 2F, 1F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(0, 34).addBox(-1.5F, -9.6F, -3.4F, 3F, 2F, 8F)
                .texOffs(0, 45).addBox(-1F, -5.5F, 3.2F, 2F, 7F, 2F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(17, 17).addBox(-6F, -9F, -5F, 12F, 5F, 10F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(80, 45).addBox(-7.4F, -11.5F, -1F, 2F, 5F, 2F)
                .texOffs(89, 45).addBox(5.4F, -11.5F, -1F, 2F, 5F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(107, 34).addBox(0F, 0F, -2F, 1F, 5F, 4F)
                .texOffs(15, 55).addBox(-0.2F, 4F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(4.5F, -6F, 0F, 0F, 0F, -0.5236F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(118, 34).addBox(-1F, 0F, -2F, 1F, 5F, 4F)
                .texOffs(24, 55).addBox(-1.8F, 4F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(-4.5F, -6F, 0F, 0F, 0F, 0.5236F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F)
                .texOffs(91, 17).addBox(-5F, -0.8F, -3F, 10F, 4F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(62, 17).addBox(-4.5F, 0.6F, -2.5F, 9F, 7F, 5F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(9, 45).addBox(-4.5F, 7.3F, -2.5F, 9F, 2F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(113, 45).addBox(-3F, 9F, -2.75F, 6F, 5F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(0, 55).addBox(-3F, 9F, 1.75F, 6F, 5F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-3F, -2F, -2F, 4F, 12F, 4F)
                .texOffs(38, 45).addBox(-3.5F, 3.3F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(23, 34).addBox(-3.5F, -2.7F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(79, 0).addBox(-1F, -2F, -2F, 4F, 12F, 4F)
                .texOffs(59, 45).addBox(-1.5F, 3.3F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(44, 34).addBox(-1.5F, -2.7F, -2.5F, 5F, 4F, 5F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(65, 34).addBox(-2.5F, 6.4F, -2.5F, 5F, 4F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-2F, 0F, -2F, 4F, 12F, 4F)
                .texOffs(86, 34).addBox(-2.5F, 6.4F, -2.5F, 5F, 4F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offset(1.9F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
