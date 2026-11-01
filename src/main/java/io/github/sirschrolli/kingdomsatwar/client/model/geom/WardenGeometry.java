package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Tiefenhüters (Knochen wie beim Vanilla-Wärter, Hörner statt Fühler).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WardenGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private WardenGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bone = root.addOrReplaceChild("bone", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_body = p_bone.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-9F, -13F, -4F, 18F, 21F, 11F)
                .texOffs(93, 104).addBox(-2.5F, -12F, 7F, 5F, 3F, 2F)
                .texOffs(108, 104).addBox(-2F, -7.5F, 7F, 4F, 3F, 1.6F)
                .texOffs(0, 112).addBox(-1.5F, -3F, 7F, 3F, 2.5F, 1.2F),
                PartPose.offset(0F, -21F, 0F));
        PartDefinition p_right_ribcage = p_body.addOrReplaceChild("right_ribcage", CubeListBuilder.create()
                .texOffs(41, 64).addBox(-2F, -11F, -0.1F, 9F, 21F, 0F),
                PartPose.offset(-7F, -2F, -4F));
        PartDefinition p_left_ribcage = p_body.addOrReplaceChild("left_ribcage", CubeListBuilder.create()
                .texOffs(60, 64).addBox(-7F, -11F, -0.1F, 9F, 21F, 0F),
                PartPose.offset(7F, -2F, -4F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-8F, -16F, -5F, 16F, 16F, 10F)
                .texOffs(54, 104).addBox(-8.5F, -12F, -6.2F, 17F, 2.5F, 1.5F)
                .texOffs(66, 87).addBox(-7F, -5F, -6F, 14F, 5F, 1.2F)
                .texOffs(99, 87).addBox(-9.6F, -14F, -1.5F, 2F, 4F, 3F)
                .texOffs(110, 87).addBox(7.6F, -14F, -1.5F, 2F, 4F, 3F),
                PartPose.offset(0F, -13F, 0F));
        PartDefinition p_right_tendril = p_head.addOrReplaceChild("right_tendril", CubeListBuilder.create()
                .texOffs(0, 87).addBox(-16F, -13F, 0F, 16F, 16F, 0F),
                PartPose.offset(-8F, -12F, 0F));
        PartDefinition p_left_tendril = p_head.addOrReplaceChild("left_tendril", CubeListBuilder.create()
                .texOffs(33, 87).addBox(0F, -13F, 0F, 16F, 16F, 0F),
                PartPose.offset(8F, -12F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 0F, -4F, 8F, 28F, 8F)
                .texOffs(53, 37).addBox(-4.6F, 13F, -4.6F, 9.2F, 12F, 9.2F)
                .texOffs(18, 104).addBox(-3.4F, 27F, -4.2F, 2F, 4F, 2F)
                .texOffs(0, 104).addBox(-1F, 27F, -4.2F, 2F, 5F, 2F)
                .texOffs(27, 104).addBox(1.4F, 27F, -4.2F, 2F, 4F, 2F)
                .texOffs(11, 112).addBox(-3.4F, 27F, 1.2F, 2F, 3F, 2F),
                PartPose.offset(-13F, -13F, 1F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-4F, 0F, -4F, 8F, 28F, 8F)
                .texOffs(0, 64).addBox(-4.6F, 13F, -4.6F, 9.2F, 12F, 9.2F)
                .texOffs(36, 104).addBox(-3.4F, 27F, -4.2F, 2F, 4F, 2F)
                .texOffs(9, 104).addBox(-1F, 27F, -4.2F, 2F, 5F, 2F)
                .texOffs(45, 104).addBox(1.4F, 27F, -4.2F, 2F, 4F, 2F)
                .texOffs(20, 112).addBox(1.8F, 27F, 1.2F, 2F, 3F, 2F),
                PartPose.offset(13F, -13F, 1F));
        PartDefinition p_right_leg = p_bone.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(79, 64).addBox(-3.1F, 0F, -3F, 6F, 13F, 6F)
                .texOffs(29, 112).addBox(-3.4F, 11F, -5F, 6.6F, 2F, 2F),
                PartPose.offset(-5.9F, -13F, 0F));
        PartDefinition p_left_leg = p_bone.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(104, 64).addBox(-2.9F, 0F, -3F, 6F, 13F, 6F)
                .texOffs(48, 112).addBox(-3.2F, 11F, -5F, 6.6F, 2F, 2F),
                PartPose.offset(5.9F, -13F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
