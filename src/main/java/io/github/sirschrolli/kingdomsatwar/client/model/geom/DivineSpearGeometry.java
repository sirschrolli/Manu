package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Lichtspeers (Knochen "spear", Spitze bei -Z).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DivineSpearGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private DivineSpearGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_spear = root.addOrReplaceChild("spear", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -1F, -6F, 2F, 2F, 18F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(0, 21).addBox(-2F, -2F, -7.4F, 4F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(43, 28).addBox(-4F, -0.5F, -7F, 8F, 1F, 1F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(41, 0).addBox(-1.5F, -1.5F, -11F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(13, 21).addBox(-1F, -1F, -14.4F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(25, 28).addBox(-0.5F, -0.5F, -16.7F, 1F, 1F, 3F, new CubeDeformation(-0.05F, -0.05F, -0.1F))
                .texOffs(34, 28).addBox(-1F, -1F, 11.3F, 2F, 2F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_vane_l = p_spear.addOrReplaceChild("vane_l", CubeListBuilder.create()
                .texOffs(26, 21).addBox(-3.5F, 0F, -0.2F, 7F, 0F, 5F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_vane_r = p_spear.addOrReplaceChild("vane_r", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-3.5F, 0F, -0.2F, 7F, 0F, 5F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0F, 0F, 1.5708F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
