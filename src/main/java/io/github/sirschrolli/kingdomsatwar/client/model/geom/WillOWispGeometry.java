package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Irrlichts.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WillOWispGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private WillOWispGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_core = root.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, 16F, 0F));
        PartDefinition p_halo = p_core.addOrReplaceChild("halo", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -3F, -3F, 6F, 6F, 6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_spark1 = p_core.addOrReplaceChild("spark1", CubeListBuilder.create()
                .texOffs(13, 13).addBox(3.5F, -0.5F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_spark2 = p_core.addOrReplaceChild("spark2", CubeListBuilder.create()
                .texOffs(18, 13).addBox(3.5F, -0.5F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_spark3 = p_core.addOrReplaceChild("spark3", CubeListBuilder.create()
                .texOffs(23, 13).addBox(3.5F, -0.5F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
