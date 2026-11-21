package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Klumpens Gift, den Quetzara spuckt (1.10).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VenomGlobGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private VenomGlobGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_glob = root.addOrReplaceChild("glob", CubeListBuilder.create()
                .texOffs(17, 0).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(0, 0).addBox(-2F, -3.5F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(38, 0).addBox(-3.5F, -2F, -2F, 7F, 4F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.2F))
                .texOffs(0, 12).addBox(-1.5F, -1.5F, 2.1F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(13, 12).addBox(-1F, -1F, 4.6F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
