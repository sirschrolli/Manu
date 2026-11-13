package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Eiszapfens (Geschoss Glaciaras, ein Knochen "icicle", Spitze bei -Z, Mitte im Ursprung; durchscheinend).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class IcicleGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private IcicleGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_icicle = root.addOrReplaceChild("icicle", CubeListBuilder.create()
                .texOffs(58, 16).addBox(-0.4F, -0.55F, -11.271F, 1F, 1F, 2F, new CubeDeformation(-0.2596F, -0.2692F, -0.071F))
                .texOffs(0, 22).addBox(-0.42F, -0.5F, -9.7635F, 1F, 1F, 2F, new CubeDeformation(-0.1796F, -0.1924F, -0.0715F))
                .texOffs(7, 22).addBox(-0.45F, -0.46F, -8.256F, 1F, 1F, 2F, new CubeDeformation(-0.0694F, -0.0866F, -0.071F))
                .texOffs(22, 16).addBox(-1F, -0.94F, -6.748F, 2F, 2F, 2F, new CubeDeformation(-0.4369F, -0.4594F, -0.071F))
                .texOffs(31, 16).addBox(-1.04F, -0.95F, -5.2405F, 2F, 2F, 2F, new CubeDeformation(-0.2861F, -0.3146F, -0.0715F))
                .texOffs(40, 16).addBox(-1.07F, -0.98F, -3.733F, 2F, 2F, 2F, new CubeDeformation(-0.1193F, -0.1545F, -0.071F))
                .texOffs(26, 9).addBox(-1.58F, -1.52F, -2.225F, 3F, 3F, 2F, new CubeDeformation(-0.4382F, -0.4806F, -0.071F))
                .texOffs(37, 9).addBox(-1.55F, -1.55F, -0.717F, 3F, 3F, 2F, new CubeDeformation(-0.244F, -0.2943F, -0.071F))
                .texOffs(48, 9).addBox(-1.5F, -1.56F, 0.7905F, 3F, 3F, 2F, new CubeDeformation(-0.0379F, -0.0964F, -0.0715F))
                .texOffs(47, 0).addBox(-1.96F, -2.04F, 2.298F, 4F, 4F, 2F, new CubeDeformation(-0.3205F, -0.3877F, -0.071F))
                .texOffs(0, 9).addBox(-1.94F, -2F, 3.806F, 4F, 4F, 2F, new CubeDeformation(-0.0925F, -0.1688F, -0.071F))
                .texOffs(17, 0).addBox(-2.46F, -2.47F, 5.3135F, 5F, 5F, 2F, new CubeDeformation(-0.3546F, -0.4404F, -0.0715F))
                .texOffs(32, 0).addBox(-2.5F, -2.5F, 6.646F, 5F, 5F, 2F, new CubeDeformation(-0.1072F, -0.2029F, -0.246F))
                .texOffs(0, 16).addBox(1.8F, -0.5F, 0.1F, 1F, 1F, 4F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(14, 22).addBox(1.8F, -0.5F, -1.4F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(11, 16).addBox(-1.4F, 1.15F, 2.6F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.45F, 0F))
                .texOffs(21, 22).addBox(-0.95F, 1.85F, 0.9F, 1F, 1F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.1F))
                .texOffs(0, 0).addBox(-3F, -3F, 8.1F, 6F, 6F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(13, 9).addBox(-2.7F, -2.8F, 9.8F, 5F, 5F, 1F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(49, 16).addBox(-1.9F, -2.2F, 10.5F, 3F, 3F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
