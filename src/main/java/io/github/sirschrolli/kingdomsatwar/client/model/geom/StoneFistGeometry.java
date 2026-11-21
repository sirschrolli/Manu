package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Steinfaust (Unterarm "arm" mit Drehpunkt auf dem Boden, darauf die Faust "fist", Knöchel oben).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class StoneFistGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 112;

    private StoneFistGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_arm = root.addOrReplaceChild("arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -9.7F, -5.5F, 11F, 11F, 11F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(45, 0).addBox(-5F, -9.5F, -5.5F, 10F, 11F, 11F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(47, 23).addBox(-5F, -19.3F, -4.5F, 10F, 11F, 9F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(86, 23).addBox(-4.5F, -18.7F, -5F, 9F, 10F, 10F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(0, 45).addBox(-7F, -3.6F, -6.5F, 14F, 5F, 13F, new CubeDeformation(-0.4F, -0.2F, -0.1F))
                .texOffs(30, 89).addBox(-5.6F, -5.3F, -7.4F, 8F, 3F, 2F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(70, 80).addBox(1.8F, -5F, 5.3F, 4F, 3F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(29, 80).addBox(-8.3F, -1.7F, 0.8F, 3F, 3F, 4F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(44, 80).addBox(-6.8F, -11.5F, -2F, 2F, 3F, 4F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(57, 80).addBox(4.9F, -8.2F, 0.3F, 2F, 3F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(51, 89).addBox(-2F, -8F, 4.8F, 4F, 3F, 2F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(64, 89).addBox(1F, -13.3F, -5.7F, 4F, 3F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(38, 64).addBox(-5.5F, -18.5F, -5.5F, 11F, 3F, 11F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_fist = p_arm.addOrReplaceChild("fist", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-7F, -10.7F, -3.5F, 14F, 12F, 9F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(83, 64).addBox(-6.5F, -9.8F, 4.3F, 13F, 9F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(0, 64).addBox(-7.7F, -11.4F, -7.4F, 4F, 10F, 5F, new CubeDeformation(-0.3F, 0F, -0.2F))
                .texOffs(85, 80).addBox(-7.2F, -12.9F, -4.2F, 3F, 2F, 4F, new CubeDeformation(-0.05F, -0.2F, -0.3F))
                .texOffs(77, 89).addBox(-7.2F, -12.25F, -7.9F, 3F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(55, 45).addBox(-4F, -12.1F, -7.4F, 4F, 11F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(100, 80).addBox(-3.5F, -13.3F, -4.2F, 3F, 2F, 4F, new CubeDeformation(-0.05F, -0.2F, -0.3F))
                .texOffs(88, 89).addBox(-3.5F, -12.65F, -7.9F, 3F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(74, 45).addBox(-0.3F, -12.1F, -7.4F, 4F, 11F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(0, 89).addBox(0.2F, -13.3F, -4.2F, 3F, 2F, 4F, new CubeDeformation(-0.05F, -0.2F, -0.3F))
                .texOffs(99, 89).addBox(0.2F, -12.65F, -7.9F, 3F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(19, 64).addBox(3.4F, -11.4F, -7.4F, 4F, 10F, 5F, new CubeDeformation(-0.3F, 0F, -0.2F))
                .texOffs(15, 89).addBox(3.9F, -12.9F, -4.2F, 3F, 2F, 4F, new CubeDeformation(-0.05F, -0.2F, -0.3F))
                .texOffs(110, 89).addBox(3.9F, -12.25F, -7.9F, 3F, 3F, 2F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(93, 45).addBox(-8.8F, -7.8F, -7.5F, 4F, 8F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(0, 80).addBox(-6.1F, -7F, -10.3F, 10F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(121, 89).addBox(2.8F, -6.5F, -10.3F, 2F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offset(0F, -19F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
