package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Wiedergänger, Mumie und Wasserleiche (Menschen-Skelett der Zombies, Zubehör als eigene Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class UndeadGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private UndeadGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.5F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(78, 35).addBox(-3F, -0.1F, -2.7F, 6F, 2F, 3F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(0F, -0.6F, -2F, 0.2094F, 0F, 0F));
        PartDefinition p_helm = p_head.addOrReplaceChild("helm", CubeListBuilder.create()
                .texOffs(68, 18).addBox(-5F, -9.7F, -5F, 10F, 4F, 10F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(0, 35).addBox(-6F, -6.5F, -6F, 12F, 1F, 12F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(49, 35).addBox(-4.5F, 9.5F, -2.5F, 9F, 5F, 5F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_robe = p_body.addOrReplaceChild("robe", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, 9.25F, -2.5F, 9F, 12F, 5F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-3F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(17, 18).addBox(-1F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(34, 18).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(51, 18).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(1.9F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
