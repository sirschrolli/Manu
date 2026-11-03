package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Froschs (Knochen wie beim Vanilla-Frosch).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ToadGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private ToadGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -2F, -8F, 7F, 3F, 9F)
                .texOffs(66, 0).addBox(-3.5F, -1F, -8F, 7F, 0F, 9F),
                PartPose.offset(0F, -2F, 4F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-3.5F, -1F, -7F, 7F, 0F, 9F)
                .texOffs(33, 0).addBox(-3.5F, -2F, -7F, 7F, 3F, 9F),
                PartPose.offset(0F, -2F, -1F));
        PartDefinition p_eyes = p_head.addOrReplaceChild("eyes", CubeListBuilder.create(),
                PartPose.offset(-0.5F, 0F, 2F));
        PartDefinition p_right_eye = p_eyes.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1.25F, -1.6F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(-1.5F, -3F, -6.5F));
        PartDefinition p_left_eye = p_eyes.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(13, 32).addBox(-1.75F, -1.6F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(2.5F, -3F, -6.5F));
        PartDefinition p_croaking_body = p_body.addOrReplaceChild("croaking_body", CubeListBuilder.create()
                .texOffs(74, 32).addBox(-3.5F, -0.1F, -2.9F, 7F, 2F, 3F, new CubeDeformation(-0.1F)),
                PartPose.offset(0F, -1F, -5F));
        PartDefinition p_tongue = p_body.addOrReplaceChild("tongue", CubeListBuilder.create()
                .texOffs(66, 23).addBox(-2F, 0F, -7.1F, 4F, 0F, 7F),
                PartPose.offset(0F, -1.01F, 1F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(52, 32).addBox(-1F, 0F, -1F, 2F, 3F, 3F),
                PartPose.offset(4F, -1F, -6.5F));
        PartDefinition p_left_hand = p_left_arm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(33, 13).addBox(-4F, 0.01F, -4F, 8F, 0F, 8F),
                PartPose.offset(0F, 3F, -1F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(63, 32).addBox(-1F, 0F, -1F, 2F, 3F, 3F),
                PartPose.offset(-4F, -1F, -6.5F));
        PartDefinition p_right_hand = p_right_arm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(66, 13).addBox(-4F, 0.01F, -5F, 8F, 0F, 8F),
                PartPose.offset(0F, 3F, 0F));
        PartDefinition p_left_leg = p_root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(89, 23).addBox(-1F, 0F, -2F, 3F, 3F, 4F)
                .texOffs(26, 32).addBox(-0.2F, 0.9F, -5.5F, 2F, 2F, 4F),
                PartPose.offset(3.5F, -3F, 4F));
        PartDefinition p_left_foot = p_left_leg.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-4F, 0.01F, -4F, 8F, 0F, 8F),
                PartPose.offset(2F, 3F, 0F));
        PartDefinition p_right_leg = p_root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(104, 23).addBox(-2F, 0F, -2F, 3F, 3F, 4F)
                .texOffs(39, 32).addBox(-1.8F, 0.9F, -5.5F, 2F, 2F, 4F),
                PartPose.offset(-3.5F, -3F, 4F));
        PartDefinition p_right_foot = p_right_leg.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(33, 23).addBox(-4F, 0.01F, -4F, 8F, 0F, 8F),
                PartPose.offset(-2F, 3F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
