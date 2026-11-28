package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Weltbrockens (Glut, Frost, End, Schatten).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WorldChunkGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private WorldChunkGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_chunk = root.addOrReplaceChild("chunk", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -6F, -6F, 12F, 12F, 12F)
                .texOffs(70, 0).addBox(-8F, -4F, -3F, 3F, 7F, 6F)
                .texOffs(49, 0).addBox(5F, -5F, -4F, 3F, 8F, 7F)
                .texOffs(0, 25).addBox(-4F, -8.5F, -4F, 8F, 3F, 7F)
                .texOffs(89, 0).addBox(-3F, 5F, -5F, 7F, 3F, 8F)
                .texOffs(31, 25).addBox(-4F, -3F, 5F, 6F, 6F, 3F)
                .texOffs(50, 25).addBox(-1.5F, -1.5F, -7.25F, 3F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
