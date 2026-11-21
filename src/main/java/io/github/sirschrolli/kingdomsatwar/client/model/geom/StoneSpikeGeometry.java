package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Steinstachels (Felsdorn "spike" und Geröll "rubble", beide mit Drehpunkt auf dem Boden).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class StoneSpikeGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 80;

    private StoneSpikeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_spike = root.addOrReplaceChild("spike", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -9.95F, -5F, 10F, 12F, 10F, new CubeDeformation(0F, -0.45F, -0.4F))
                .texOffs(41, 0).addBox(-4.5F, -10.15F, -5F, 9F, 11F, 10F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 23).addBox(-3.6F, -17.55F, -4.5F, 9F, 9F, 8F, new CubeDeformation(-0.3F, -0.05F, 0F))
                .texOffs(80, 0).addBox(-2.9F, -17.85F, -5F, 8F, 9F, 9F, new CubeDeformation(-0.3F, -0.35F, -0.1F))
                .texOffs(35, 23).addBox(-1.6F, -24.55F, -3.9F, 7F, 8F, 7F, new CubeDeformation(-0.1F, -0.05F, -0.4F))
                .texOffs(64, 23).addBox(-1.1F, -25.25F, -3.9F, 6F, 8F, 7F, new CubeDeformation(-0.1F, -0.35F, 0F))
                .texOffs(91, 23).addBox(-0.6F, -30.3F, -1.9F, 6F, 7F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(0, 41).addBox(0.1F, -30.1F, -2.4F, 5F, 6F, 6F, new CubeDeformation(-0.4F, -0.1F, -0.3F))
                .texOffs(23, 41).addBox(-0.1F, -34.7F, -0.7F, 4F, 6F, 4F, new CubeDeformation(-0.1F, -0.5F, -0.45F))
                .texOffs(40, 41).addBox(0.4F, -34.9F, -0.7F, 3F, 5F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.05F))
                .texOffs(115, 41).addBox(0F, -36.6F, 0.1F, 3F, 3F, 3F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(100, 54).addBox(0.2F, -39.05F, 0.8F, 2F, 3F, 2F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(109, 54).addBox(0F, -41.05F, 0.9F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.4F))
                .texOffs(20, 61).addBox(0.4F, -43F, 1.5F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F))
                .texOffs(55, 41).addBox(-6.2F, -6.5F, -2.4F, 2F, 3F, 5F, new CubeDeformation(-0.2F, 0F, -0.4F))
                .texOffs(87, 41).addBox(4.3F, -14.7F, 0.7F, 2F, 3F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(13, 54).addBox(-2.8F, -20.9F, 1.9F, 2F, 3F, 3F, new CubeDeformation(-0.2F, -0.3F, -0.1F))
                .texOffs(24, 54).addBox(1.6F, -13.2F, -6F, 4F, 3F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(89, 54).addBox(4F, -27F, -1F, 2F, 2F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(37, 54).addBox(-1.6F, -4.2F, 4.7F, 4F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(11, 61).addBox(0.1F, -31.5F, 2.3F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_rubble = root.addOrReplaceChild("rubble", CubeListBuilder.create()
                .texOffs(100, 41).addBox(-9.8F, -2.1F, -3F, 4F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(118, 54).addBox(-9.4F, -3.5F, -2.6F, 3F, 2F, 2F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(50, 54).addBox(6.8F, -1.3F, -7.1F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(70, 41).addBox(8.3F, -2.3F, 3.1F, 4F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(63, 54).addBox(8.7F, -3.8F, 3.5F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.3F, -0.4F))
                .texOffs(76, 54).addBox(-2.5F, -1.2F, 7.9F, 3F, 2F, 3F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(0, 61).addBox(-8F, -1.1F, 6.4F, 3F, 2F, 2F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(0, 54).addBox(2.5F, -2F, -10F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.4F))
                .texOffs(25, 61).addBox(2.9F, -2.5F, -9.6F, 2F, 1F, 2F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 24F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
