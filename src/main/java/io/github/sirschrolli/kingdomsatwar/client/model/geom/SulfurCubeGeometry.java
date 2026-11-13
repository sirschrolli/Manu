package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Schwefelwürfel (Hülle und Kern, groß und klein, je als eigener Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SulfurCubeGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private SulfurCubeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_cube = root.addOrReplaceChild("cube", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -9F, -9F, 18F, 18F, 18F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_inner = root.addOrReplaceChild("inner", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-8F, -8F, -8F, 16F, 16F, 16F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_small = root.addOrReplaceChild("small", CubeListBuilder.create()
                .texOffs(65, 37).addBox(-5F, -5F, -5F, 10F, 10F, 10F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_small_inner = root.addOrReplaceChild("small_inner", CubeListBuilder.create()
                .texOffs(0, 70).addBox(-4F, -4F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
