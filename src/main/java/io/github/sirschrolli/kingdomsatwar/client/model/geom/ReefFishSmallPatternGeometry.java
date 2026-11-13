package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Muster-Ebene des Rifffischs (Form A).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ReefFishSmallPatternGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private ReefFishSmallPatternGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -1.5F, -3F, 2F, 3F, 6F, new CubeDeformation(0.008F))
                .texOffs(18, 10).addBox(-0.5F, -0.5F, -4F, 1F, 2F, 1F, new CubeDeformation(0.008F))
                .texOffs(11, 10).addBox(0F, 1.5F, 0F, 0F, 1F, 3F, new CubeDeformation(0.008F)),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(17, 0).addBox(0F, -2.5F, 0F, 0F, 5F, 4F, new CubeDeformation(0.008F)),
                PartPose.offset(0F, 22F, 3F));
        PartDefinition p_right_fin = root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(23, 10).addBox(-2F, -1F, 0F, 2F, 2F, 0F, new CubeDeformation(0.008F)),
                PartPose.offsetAndRotation(-1F, 22.5F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_left_fin = root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(28, 10).addBox(0F, -1F, 0F, 2F, 2F, 0F, new CubeDeformation(0.008F)),
                PartPose.offsetAndRotation(1F, 22.5F, 0F, 0F, -0.7854F, 0F));
        PartDefinition p_top_fin = root.addOrReplaceChild("top_fin", CubeListBuilder.create()
                .texOffs(0, 10).addBox(0F, -2F, 1F, 0F, 2F, 5F, new CubeDeformation(0.008F)),
                PartPose.offset(0F, 20.5F, -3F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
