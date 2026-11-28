package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Schlammbatzens, den der Moorschlurfer wirft (1.12.1).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MudBallGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private MudBallGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_ball = root.addOrReplaceChild("ball", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(0, 11).addBox(-3F, -1.9F, -2.1F, 6F, 4F, 4F, new CubeDeformation(0F, -0.2F, -0.2F))
                .texOffs(21, 0).addBox(-1.9F, -3F, -1.8F, 4F, 6F, 4F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(38, 0).addBox(-1.4F, -1.4F, -3.2F, 3F, 3F, 6F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(32, 11).addBox(1.4F, 1.2F, -2.8F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.16F, -0.2F))
                .texOffs(41, 11).addBox(-3.65F, -3F, 0.05F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.31F))
                .texOffs(57, 11).addBox(-0.6F, -4.35F, -1.1F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(0, 20).addBox(-0.1F, -3.65F, -0.45F, 1F, 1F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.25F))
                .texOffs(21, 11).addBox(-1F, -0.1F, 2.2F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(50, 11).addBox(-0.5F, 0.7F, 4.5F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
