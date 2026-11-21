package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Rifffischs (Form B, Grundfarbe getönt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ReefFishLargeGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private ReefFishLargeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1F, -3F, -2F, 2F, 6F, 4F)
                .texOffs(22, 12).addBox(-1F, -2F, -3F, 2F, 4F, 1F)
                .texOffs(0, 23).addBox(-1F, -2F, 2F, 2F, 4F, 1F)
                .texOffs(7, 23).addBox(-0.5F, 0F, -4F, 1F, 1F, 1F),
                PartPose.offset(0F, 19F, 0F));
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(13, 12).addBox(0F, -3F, 0F, 0F, 6F, 4F),
                PartPose.offset(0F, 19F, 3F));
        PartDefinition p_right_fin = root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(12, 23).addBox(-2F, 0F, 0F, 2F, 2F, 0F),
                PartPose.offsetAndRotation(-1F, 20F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_left_fin = root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(17, 23).addBox(0F, 0F, 0F, 2F, 2F, 0F),
                PartPose.offsetAndRotation(1F, 20F, 0F, 0F, -0.7854F, 0F));
        PartDefinition p_top_fin = root.addOrReplaceChild("top_fin", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, -4F, 0F, 0F, 5F, 6F),
                PartPose.offset(0F, 16F, -3F));
        PartDefinition p_bottom_fin = root.addOrReplaceChild("bottom_fin", CubeListBuilder.create()
                .texOffs(13, 0).addBox(0F, -1F, 0F, 0F, 5F, 6F),
                PartPose.offset(0F, 22F, -3F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
