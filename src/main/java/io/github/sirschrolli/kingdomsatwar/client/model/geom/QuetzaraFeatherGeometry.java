package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie einer vergifteten Feder aus Quetzaras Krause (1.10).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class QuetzaraFeatherGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private QuetzaraFeatherGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_feather = root.addOrReplaceChild("feather", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-2.5F, 0F, -7F, 5F, 0F, 14F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(0, 0).addBox(-0.5F, -0.5F, -8F, 1F, 1F, 15F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
