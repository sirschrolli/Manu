package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Seelenreihers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SoulHeronGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 96;

    private SoulHeronGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -3.4F, -7F, 7F, 7F, 13F)
                .texOffs(0, 21).addBox(-3F, 2.3F, -6.2F, 6F, 2F, 10F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(33, 21).addBox(-3F, -4.6F, -6.7F, 6F, 2F, 10F, new CubeDeformation(-0.4F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(0F, 5F, 1F, -0.2793F, 0F, 0F));
        PartDefinition p_plumes = p_body.addOrReplaceChild("plumes", CubeListBuilder.create()
                .texOffs(95, 34).addBox(-2.5F, -0.25F, -0.75F, 5F, 7F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.05F)),
                PartPose.offsetAndRotation(0F, 0.6F, -7F, 0.2443F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(34, 34).addBox(-3F, -1F, 0F, 6F, 2F, 6F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, -1.6F, 6F, 0.384F, 0F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(41, 0).addBox(-0.4F, -0.3F, -0.25F, 2F, 7F, 9F, new CubeDeformation(-0.4F, -0.3F, -0.25F)),
                PartPose.offsetAndRotation(3.4F, -2.9F, -5.6F, -0.0524F, 0F, 0F));
        PartDefinition p_left_wing_outer = p_left_wing.addOrReplaceChild("left_wing_outer", CubeListBuilder.create()
                .texOffs(87, 0).addBox(0.1F, -0.4F, 0F, 1F, 6F, 7F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0.4F, 8.4F, -0.1047F, 0F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(64, 0).addBox(-1.6F, -0.3F, -0.25F, 2F, 7F, 9F, new CubeDeformation(-0.4F, -0.3F, -0.25F)),
                PartPose.offsetAndRotation(-3.4F, -2.9F, -5.6F, -0.0524F, 0F, 0F));
        PartDefinition p_right_wing_outer = p_right_wing.addOrReplaceChild("right_wing_outer", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-1.1F, -0.4F, 0F, 1F, 6F, 7F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0.4F, 8.4F, -0.1047F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(89, 21).addBox(-1.5F, -6.6F, -1.5F, 3F, 7F, 3F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, -2.4F, -6.2F, 0.6981F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(21, 34).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.35F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, -0.9774F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(15, 44).addBox(-1F, -4.8F, -1F, 2F, 5F, 2F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -5.8F, 0F, 0.6283F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(59, 34).addBox(-2F, -2.6F, -3.6F, 4F, 3F, 5F, new CubeDeformation(-0.4F, 0F, -0.2F))
                .texOffs(66, 21).addBox(-1F, -1.7F, -11.9F, 2F, 1F, 9F, new CubeDeformation(-0.35F, 0F, -0.3F))
                .texOffs(7, 52).addBox(1.175F, -2F, -2.75F, 1F, 1F, 1F, new CubeDeformation(-0.375F, -0.15F, -0.05F))
                .texOffs(12, 52).addBox(-2.175F, -2F, -2.75F, 1F, 1F, 1F, new CubeDeformation(-0.375F, -0.15F, -0.05F)),
                PartPose.offsetAndRotation(0F, -4.4F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-1F, -0.15F, -7.9F, 2F, 1F, 8F, new CubeDeformation(-0.45F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(0F, -0.7F, -3.2F, 0.0349F, 0F, 0F));
        PartDefinition p_crest = p_head.addOrReplaceChild("crest", CubeListBuilder.create()
                .texOffs(78, 34).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F, new CubeDeformation(-0.05F, -0.25F, 0F))
                .texOffs(108, 34).addBox(0.15F, -0.2F, -0.4F, 1F, 1F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.4F))
                .texOffs(0, 44).addBox(-1.15F, -0.2F, -0.4F, 1F, 1F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.3F, 0.8F, 1.0123F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(64, 44).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(24, 44).addBox(-1F, 2.6F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(1.7F, 8.2F, 1.6F, -0.1745F, 0F, 0F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(102, 21).addBox(-0.5F, -0.2F, -0.5F, 1F, 9F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 7.4F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_left_foot = p_left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(42, 44).addBox(-0.5F, -0.525F, -3.7F, 1F, 1F, 4F, new CubeDeformation(-0.2F, -0.225F, -0.1F))
                .texOffs(82, 44).addBox(-1.75F, -0.55F, -2.8F, 1F, 1F, 3F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(91, 44).addBox(0.75F, -0.55F, -2.8F, 1F, 1F, 3F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(118, 44).addBox(-0.5F, -0.55F, 0F, 1F, 1F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.1047F, 0F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(73, 44).addBox(-1F, -0.4F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(33, 44).addBox(-1F, 2.6F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(-1.7F, 8.2F, 1.6F, -0.1745F, 0F, 0F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(107, 21).addBox(-0.5F, -0.2F, -0.5F, 1F, 9F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 7.4F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_right_foot = p_right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(53, 44).addBox(-0.5F, -0.525F, -3.7F, 1F, 1F, 4F, new CubeDeformation(-0.2F, -0.225F, -0.1F))
                .texOffs(100, 44).addBox(-1.75F, -0.55F, -2.8F, 1F, 1F, 3F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(109, 44).addBox(0.75F, -0.55F, -2.8F, 1F, 1F, 3F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(0, 52).addBox(-0.5F, -0.55F, 0F, 1F, 1F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, 8.4F, 0F, -0.1047F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
