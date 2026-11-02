package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schreinfunkens (Knochen "main" wie beim Vanilla-Shulker-Geschoss).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SchreinfunkeGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private SchreinfunkeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_main = root.addOrReplaceChild("main", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F)
                .texOffs(23, 0).addBox(-1F, -4.5F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(0, 19).addBox(-4.5F, -1F, -1F, 9F, 2F, 2F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(0, 0).addBox(-1F, -1F, -4.5F, 2F, 2F, 9F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
