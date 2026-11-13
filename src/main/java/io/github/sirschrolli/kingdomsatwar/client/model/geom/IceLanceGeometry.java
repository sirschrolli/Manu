package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Eislanze (Geschoss des Eiszaubers, ein Knochen "lance", Spitze bei -Z, Mitte im Ursprung; durchscheinend).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class IceLanceGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private IceLanceGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_lance = root.addOrReplaceChild("lance", CubeListBuilder.create()
                .texOffs(37, 19).addBox(-0.5F, -0.5F, -9.95F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.05F))
                .texOffs(42, 19).addBox(-0.5F, -0.5F, -9.15F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(9, 19).addBox(-1F, -1F, -8.35F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.05F))
                .texOffs(16, 19).addBox(-1F, -1F, -7.55F, 2F, 2F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.05F))
                .texOffs(26, 13).addBox(-1.5F, -1.5F, -6.75F, 3F, 3F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.05F))
                .texOffs(35, 13).addBox(-1.5F, -1.5F, -5.95F, 3F, 3F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.05F))
                .texOffs(15, 13).addBox(-1.5F, -1.5F, -5.5F, 3F, 3F, 2F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(44, 13).addBox(-1.5F, -1.5F, -4.05F, 3F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.05F))
                .texOffs(23, 19).addBox(-1F, -1F, -3.25F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(0, 0).addBox(-1F, -1F, -2.8F, 2F, 2F, 10F, new CubeDeformation(-0.2F, -0.2F, -0.4F))
                .texOffs(53, 13).addBox(-1.5F, -1.5F, 1.55F, 3F, 3F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.05F))
                .texOffs(25, 0).addBox(-0.5F, -3.35F, 2.9F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.35F, -0.3F))
                .texOffs(36, 0).addBox(-0.5F, 0.35F, 2.9F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.35F, -0.3F))
                .texOffs(47, 0).addBox(0.35F, -0.5F, 2.9F, 3F, 1F, 4F, new CubeDeformation(-0.35F, -0.2F, -0.3F))
                .texOffs(0, 13).addBox(-3.35F, -0.5F, 2.9F, 3F, 1F, 4F, new CubeDeformation(-0.35F, -0.2F, -0.3F))
                .texOffs(0, 19).addBox(-1F, -1F, 6.2F, 2F, 2F, 2F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(30, 19).addBox(-1F, -1F, 7.6F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(47, 19).addBox(-0.5F, -0.5F, 8.35F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
