package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Felsbrockens, den der Steinhüter schleudert.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class KeeperBoulderGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private KeeperBoulderGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_boulder = root.addOrReplaceChild("boulder", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -6F, -6.5F, 14F, 12F, 13F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(0, 26).addBox(-5F, -7.2F, -4.5F, 10F, 2F, 9F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(55, 0).addBox(-5.5F, 5.1F, -5F, 11F, 2F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_boulder_crystal_0 = p_boulder.addOrReplaceChild("boulder_crystal_0", CubeListBuilder.create()
                .texOffs(39, 26).addBox(-1F, -2.802F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.198F, 0F)),
                PartPose.offsetAndRotation(3.4F, -5F, -2F, -0.3491F, 0F, 0.5236F));
        PartDefinition p_boulder_crystal_0_tip = p_boulder_crystal_0.addOrReplaceChild("boulder_crystal_0_tip", CubeListBuilder.create()
                .texOffs(48, 26).addBox(-1F, -2.508F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.38F, -0.492F, -0.38F)),
                PartPose.offsetAndRotation(0F, -2.304F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_boulder_crystal_1 = p_boulder.addOrReplaceChild("boulder_crystal_1", CubeListBuilder.create()
                .texOffs(57, 26).addBox(-1F, -1.992F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.008F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -4.8F, 2.4F, 0.4189F, 0F, -0.4538F));
        PartDefinition p_boulder_crystal_1_tip = p_boulder_crystal_1.addOrReplaceChild("boulder_crystal_1_tip", CubeListBuilder.create()
                .texOffs(66, 26).addBox(-0.5F, -1.768F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.004F, -0.232F, -0.004F)),
                PartPose.offsetAndRotation(0F, -1.684F, 0F, 0F, 0.7854F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
