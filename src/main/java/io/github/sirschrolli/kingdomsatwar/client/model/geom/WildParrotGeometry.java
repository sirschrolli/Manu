package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Papageien (Skelett wie beim Vanilla-Papagei).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildParrotGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private WildParrotGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F)
                .texOffs(0, 10).addBox(-2F, -0.2F, -1.9F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 16.5F, -3F, 0.4938F, 0F, 0F));
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1.5F, -1F, -1F, 3F, 5F, 1F)
                .texOffs(38, 10).addBox(-1F, 3.65F, -1F, 2F, 4F, 1F, new CubeDeformation(0F, -0.25F, -0.1F))
                .texOffs(56, 10).addBox(-0.5F, 7.1F, -1F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, 21.07F, 1.16F, 1.0149F, 0F, 0F));
        PartDefinition p_left_wing = root.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(22, 0).addBox(-0.5F, 0F, -1.5F, 1F, 5F, 3F)
                .texOffs(11, 10).addBox(-0.5F, 4.6F, -1.6F, 1F, 2F, 3F, new CubeDeformation(-0.05F, 0F, -0.4F)),
                PartPose.offsetAndRotation(1.5F, 16.94F, -2.76F, -0.6981F, -3.1416F, 0F));
        PartDefinition p_right_wing = root.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(31, 0).addBox(-0.5F, 0F, -1.5F, 1F, 5F, 3F)
                .texOffs(20, 10).addBox(-0.5F, 4.6F, -1.6F, 1F, 2F, 3F, new CubeDeformation(-0.05F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-1.5F, 16.94F, -2.76F, -0.6981F, -3.1416F, 0F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 10).addBox(-1F, -1.5F, -1F, 2F, 3F, 2F)
                .texOffs(45, 10).addBox(-1.5F, -1.7F, -1.1F, 3F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(0F, 15.69F, -2.76F));
        PartDefinition p_head2 = p_head.addOrReplaceChild("head2", CubeListBuilder.create()
                .texOffs(49, 0).addBox(-1F, -0.5F, -2F, 2F, 1F, 4F),
                PartPose.offset(0F, -2F, -1F));
        PartDefinition p_beak1 = p_head.addOrReplaceChild("beak1", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-0.5F, -1F, -0.5F, 1F, 2F, 1F),
                PartPose.offset(0F, -0.5F, -1.5F));
        PartDefinition p_beak2 = p_head.addOrReplaceChild("beak2", CubeListBuilder.create()
                .texOffs(5, 16).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F)
                .texOffs(20, 16).addBox(-0.5F, 1.65F, -0.55F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offset(0F, -1.75F, -2.45F));
        PartDefinition p_feather = p_head.addOrReplaceChild("feather", CubeListBuilder.create()
                .texOffs(13, 0).addBox(0F, -4F, -2F, 0F, 5F, 4F),
                PartPose.offsetAndRotation(0F, -2.15F, 0.15F, -0.2215F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(10, 16).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(1F, 22F, -1.05F, -0.0298F, 0F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(15, 16).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(-1F, 22F, -1.05F, -0.0298F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
