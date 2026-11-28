package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der jungen Borstlinge und Moderborstlinge (Knochen wie beim Vanilla-Piglin-Kind).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BorstlingBabyGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private BorstlingBabyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-3F, -3F, -1F, 6F, 5F, 3F)
                .texOffs(13, 23).addBox(-3.5F, 0.2F, -1.5F, 7F, 2F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offset(0F, 18F, -0.5F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -6F, -3.5F, 9F, 6F, 7F)
                .texOffs(49, 23).addBox(-1.5F, -3F, -4.5F, 3F, 3F, 1F)
                .texOffs(36, 23).addBox(-1F, -7.8F, -2F, 2F, 2F, 4F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create(),
                PartPose.offset(4.2F, -4F, 0F));
        PartDefinition p_left_ear_r1 = p_left_ear.addOrReplaceChild("left_ear_r1", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-0.5F, -3F, -2F, 1F, 6F, 4F),
                PartPose.offsetAndRotation(1F, 1.75F, 0F, 0F, 0F, -0.6109F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create(),
                PartPose.offset(-4.2F, -4F, 0F));
        PartDefinition p_right_ear_r1 = p_right_ear.addOrReplaceChild("right_ear_r1", CubeListBuilder.create()
                .texOffs(44, 0).addBox(-0.5F, -3F, -2F, 1F, 6F, 4F),
                PartPose.offsetAndRotation(-1F, 1.75F, 0F, 0F, 0F, 0.6109F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(19, 14).addBox(-1F, 0F, -1.5F, 2F, 5F, 3F),
                PartPose.offset(4F, 15F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(30, 14).addBox(-1F, 0F, -1.5F, 2F, 5F, 3F),
                PartPose.offset(-4F, 15F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(41, 14).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F),
                PartPose.offset(-1.5F, 20F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-1.5F, 0F, -1.5F, 3F, 4F, 3F),
                PartPose.offset(1.5F, 20F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
