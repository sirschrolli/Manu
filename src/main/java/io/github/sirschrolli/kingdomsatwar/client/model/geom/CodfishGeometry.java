package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Dorschs (Knochen wie beim Vanilla-Kabeljau, dazu Bartel und Afterflosse).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CodfishGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private CodfishGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(21, 0).addBox(-1F, -3F, 0F, 2F, 5F, 5F)
                .texOffs(11, 14).addBox(-1F, -2F, 5F, 2F, 3F, 2F),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1F, -3F, -3F, 2F, 5F, 3F),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_barbel = p_head.addOrReplaceChild("barbel", CubeListBuilder.create()
                .texOffs(49, 14).addBox(0F, 0F, -0.5F, 0F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 1F, -3.5F, 0.4189F, 0F, 0F));
        PartDefinition p_nose = root.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(20, 14).addBox(-1F, -2F, -1F, 2F, 3F, 1F),
                PartPose.offset(0F, 22F, -3F));
        PartDefinition p_right_fin = root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(27, 14).addBox(-2F, 0F, -1F, 2F, 0F, 3F),
                PartPose.offsetAndRotation(-1F, 23F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_left_fin = root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(38, 14).addBox(0F, 0F, -1F, 2F, 0F, 3F),
                PartPose.offsetAndRotation(1F, 23F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_tail_fin = root.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(36, 0).addBox(0F, -3F, 0F, 0F, 5F, 4F),
                PartPose.offset(0F, 22F, 7F));
        PartDefinition p_top_fin = root.addOrReplaceChild("top_fin", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, -4F, -2F, 0F, 3F, 10F),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_bottom_fin = root.addOrReplaceChild("bottom_fin", CubeListBuilder.create()
                .texOffs(45, 0).addBox(0F, 0F, 0F, 0F, 2F, 6F),
                PartPose.offset(0F, 24F, 0.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
