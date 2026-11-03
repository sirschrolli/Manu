package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Dornenranke (Kette seg0–seg5, jedes Glied mit Drehpunkt an seinem Fuß; schwingt über xRot/zRot).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ThornVineGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 80;

    private ThornVineGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_seg0 = root.addOrReplaceChild("seg0", CubeListBuilder.create()
                .texOffs(23, 0).addBox(-3F, -10.2F, -2.5F, 6F, 11F, 5F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(0, 0).addBox(-2.5F, -10.2F, -3F, 5F, 11F, 6F, new CubeDeformation(-0.4F, -0.25F, 0F))
                .texOffs(63, 43).addBox(2F, -2.1F, -1.1F, 5F, 3F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(30, 43).addBox(-1.9F, -2.1F, 2.2F, 3F, 3F, 4F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(97, 43).addBox(-6.3F, -2.1F, -1.9F, 4F, 3F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.4F))
                .texOffs(65, 32).addBox(-1.1F, -2.1F, -7.1F, 3F, 3F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(60, 18).addBox(-4F, -3F, -4F, 8F, 4F, 8F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(9, 64).addBox(2.5F, -4F, -1.5F, 1F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(54, 59).addBox(2.5F, -5.74F, -0.5F, 4F, 4F, 0F, new CubeDeformation(0F, -0.16F, 0F))
                .texOffs(70, 64).addBox(2.5F, -3F, -2F, 4F, 0F, 3F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(15, 69).addBox(-0.5F, -6.4F, -3.5F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, 0F))
                .texOffs(82, 32).addBox(0.5F, -8.14F, -6.5F, 0F, 4F, 4F, new CubeDeformation(0F, -0.16F, 0F))
                .texOffs(61, 52).addBox(-1F, -5.4F, -6.5F, 3F, 0F, 4F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(22, 69).addBox(-1.5F, -8.8F, 2.5F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, 0F))
                .texOffs(91, 32).addBox(-0.5F, -10.54F, 2.5F, 0F, 4F, 4F, new CubeDeformation(0F, -0.16F, 0F))
                .texOffs(76, 52).addBox(-2F, -7.8F, 2.5F, 3F, 0F, 4F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_seg1 = p_seg0.addOrReplaceChild("seg1", CubeListBuilder.create()
                .texOffs(67, 0).addBox(-3F, -9.2F, -2F, 6F, 10F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.18F))
                .texOffs(46, 0).addBox(-2F, -9.2F, -3F, 4F, 10F, 6F, new CubeDeformation(-0.18F, -0.25F, -0.4F))
                .texOffs(11, 32).addBox(-3.5F, -1.25F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.45F, -0.15F, -0.45F))
                .texOffs(29, 69).addBox(-1.48F, -4.3F, -3.08F, 2F, 2F, 1F, new CubeDeformation(-0.376F, -0.376F, -0.02F))
                .texOffs(100, 32).addBox(-0.48F, -6.0144F, -6.02F, 0F, 4F, 4F, new CubeDeformation(0F, -0.2296F, -0.08F))
                .texOffs(91, 52).addBox(-1.98F, -3.3F, -6.02F, 3F, 0F, 4F, new CubeDeformation(-0.444F, 0F, -0.08F))
                .texOffs(16, 64).addBox(-3.08F, -6.46F, -0.52F, 1F, 2F, 2F, new CubeDeformation(-0.02F, -0.376F, -0.376F))
                .texOffs(63, 59).addBox(-6.02F, -8.1744F, 0.48F, 4F, 4F, 0F, new CubeDeformation(-0.08F, -0.2296F, 0F))
                .texOffs(85, 64).addBox(-6.02F, -5.46F, -1.02F, 4F, 0F, 3F, new CubeDeformation(-0.08F, 0F, -0.444F))
                .texOffs(23, 64).addBox(2.08F, -8.62F, -1.48F, 1F, 2F, 2F, new CubeDeformation(-0.02F, -0.376F, -0.376F))
                .texOffs(72, 59).addBox(2.02F, -10.3344F, -0.48F, 4F, 4F, 0F, new CubeDeformation(-0.08F, -0.2296F, 0F))
                .texOffs(100, 64).addBox(2.02F, -7.62F, -1.98F, 4F, 0F, 3F, new CubeDeformation(-0.08F, 0F, -0.444F))
                .texOffs(112, 43).addBox(1.9F, -9.35F, 0.15F, 7F, 6F, 0F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(0, 18).addBox(0.15F, -12.05F, -8.9F, 0F, 6F, 7F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_seg2 = p_seg1.addOrReplaceChild("seg2", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-2.5F, -8.95F, -2F, 5F, 10F, 4F, new CubeDeformation(-0.3F, -0.45F, -0.46F))
                .texOffs(107, 0).addBox(-2F, -8.45F, -2.5F, 4F, 9F, 5F, new CubeDeformation(-0.46F, 0F, -0.3F))
                .texOffs(40, 32).addBox(-3F, -1.25F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.35F, -0.15F, -0.35F))
                .texOffs(30, 64).addBox(-2.66F, -3.55F, -1.46F, 1F, 2F, 2F, new CubeDeformation(-0.04F, -0.402F, -0.402F))
                .texOffs(81, 59).addBox(-5.54F, -5.2388F, -0.46F, 4F, 4F, 0F, new CubeDeformation(-0.16F, -0.2992F, 0F))
                .texOffs(0, 69).addBox(-5.54F, -2.55F, -1.96F, 4F, 0F, 3F, new CubeDeformation(-0.16F, 0F, -0.488F))
                .texOffs(36, 69).addBox(-0.54F, -5.59F, 1.66F, 2F, 2F, 1F, new CubeDeformation(-0.402F, -0.402F, -0.04F))
                .texOffs(109, 32).addBox(0.46F, -7.2788F, 1.54F, 0F, 4F, 4F, new CubeDeformation(0F, -0.2992F, -0.16F))
                .texOffs(106, 52).addBox(-1.04F, -4.59F, 1.54F, 3F, 0F, 4F, new CubeDeformation(-0.488F, 0F, -0.16F))
                .texOffs(43, 69).addBox(-1.46F, -7.63F, -2.66F, 2F, 2F, 1F, new CubeDeformation(-0.402F, -0.402F, -0.04F))
                .texOffs(118, 32).addBox(-0.46F, -9.3188F, -5.54F, 0F, 4F, 4F, new CubeDeformation(0F, -0.2992F, -0.16F))
                .texOffs(0, 59).addBox(-1.96F, -6.63F, -5.54F, 3F, 0F, 4F, new CubeDeformation(-0.488F, 0F, -0.16F))
                .texOffs(0, 52).addBox(-8.5F, -9.125F, -0.15F, 7F, 6F, 0F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(15, 18).addBox(0.15F, -11.675F, 1.5F, 0F, 6F, 7F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offset(0F, -9F, 0F));
        PartDefinition p_seg3 = p_seg2.addOrReplaceChild("seg3", CubeListBuilder.create()
                .texOffs(93, 18).addBox(-2F, -8.2F, -1.5F, 4F, 9F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.24F))
                .texOffs(30, 18).addBox(-1.5F, -8.2F, -2F, 3F, 9F, 4F, new CubeDeformation(-0.24F, -0.25F, -0.2F))
                .texOffs(9, 43).addBox(-2.5F, -1.25F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.25F, -0.15F, -0.25F))
                .texOffs(50, 69).addBox(-1.44F, -4F, 1.24F, 2F, 2F, 1F, new CubeDeformation(-0.428F, -0.428F, -0.06F))
                .texOffs(0, 43).addBox(-0.44F, -5.6632F, 1.06F, 0F, 4F, 4F, new CubeDeformation(0F, -0.3688F, -0.24F))
                .texOffs(15, 59).addBox(-1.44F, -3F, 1.06F, 2F, 0F, 4F, new CubeDeformation(-0.032F, 0F, -0.24F))
                .texOffs(37, 64).addBox(1.24F, -5.92F, -0.56F, 1F, 2F, 2F, new CubeDeformation(-0.06F, -0.428F, -0.428F))
                .texOffs(90, 59).addBox(1.06F, -7.5832F, 0.44F, 4F, 4F, 0F, new CubeDeformation(-0.24F, -0.3688F, 0F))
                .texOffs(71, 69).addBox(1.06F, -4.92F, -0.56F, 4F, 0F, 2F, new CubeDeformation(-0.24F, 0F, -0.032F))
                .texOffs(44, 64).addBox(-2.24F, -7.84F, -1.44F, 1F, 2F, 2F, new CubeDeformation(-0.06F, -0.428F, -0.428F))
                .texOffs(99, 59).addBox(-5.06F, -9.5032F, -0.44F, 4F, 4F, 0F, new CubeDeformation(-0.24F, -0.3688F, 0F))
                .texOffs(84, 69).addBox(-5.06F, -6.84F, -1.44F, 4F, 0F, 2F, new CubeDeformation(-0.24F, 0F, -0.032F))
                .texOffs(15, 52).addBox(1.1F, -8.9F, 0.15F, 7F, 6F, 0F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(45, 18).addBox(0.15F, -11.3F, -8.1F, 0F, 6F, 7F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offset(0F, -8.5F, 0F));
        PartDefinition p_seg4 = p_seg3.addOrReplaceChild("seg4", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1.5F, -7.2F, -1F, 3F, 8F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.02F))
                .texOffs(108, 18).addBox(-1F, -7.2F, -1.5F, 2F, 8F, 3F, new CubeDeformation(-0.02F, -0.25F, -0.1F))
                .texOffs(80, 43).addBox(-2F, -1.25F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.15F, -0.15F, -0.15F))
                .texOffs(51, 64).addBox(0.82F, -3.1F, -1.42F, 1F, 2F, 2F, new CubeDeformation(-0.08F, -0.454F, -0.454F))
                .texOffs(108, 59).addBox(0.58F, -4.7376F, -0.42F, 4F, 4F, 0F, new CubeDeformation(-0.32F, -0.4384F, 0F))
                .texOffs(97, 69).addBox(0.58F, -2.1F, -1.42F, 4F, 0F, 2F, new CubeDeformation(-0.32F, 0F, -0.076F))
                .texOffs(58, 64).addBox(-1.82F, -6.46F, -0.58F, 1F, 2F, 2F, new CubeDeformation(-0.08F, -0.454F, -0.454F))
                .texOffs(117, 59).addBox(-4.58F, -8.0976F, 0.42F, 4F, 4F, 0F, new CubeDeformation(-0.32F, -0.4384F, 0F))
                .texOffs(110, 69).addBox(-4.58F, -5.46F, -0.58F, 4F, 0F, 2F, new CubeDeformation(-0.32F, 0F, -0.076F)),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_seg5 = p_seg4.addOrReplaceChild("seg5", CubeListBuilder.create()
                .texOffs(30, 52).addBox(-1F, -3.2F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(39, 52).addBox(-1F, -3.2F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.3F, -0.25F, 0F))
                .texOffs(48, 52).addBox(-1.5F, -1.25F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.05F, -0.15F, -0.05F))
                .texOffs(0, 64).addBox(-1F, -4.7F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.1F, -0.35F))
                .texOffs(65, 64).addBox(-0.5F, -7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(57, 69).addBox(-1.4F, -3.4F, -1.4F, 2F, 2F, 1F, new CubeDeformation(-0.48F, -0.48F, -0.1F))
                .texOffs(45, 43).addBox(-0.4F, -4.512F, -4.1F, 0F, 3F, 4F, new CubeDeformation(0F, -0.008F, -0.4F))
                .texOffs(28, 59).addBox(-1.4F, -2.4F, -4.1F, 2F, 0F, 4F, new CubeDeformation(-0.12F, 0F, -0.4F))
                .texOffs(64, 69).addBox(-0.6F, -6.28F, 0.4F, 2F, 2F, 1F, new CubeDeformation(-0.48F, -0.48F, -0.1F))
                .texOffs(54, 43).addBox(0.4F, -7.392F, 0.1F, 0F, 3F, 4F, new CubeDeformation(0F, -0.008F, -0.4F))
                .texOffs(41, 59).addBox(-0.6F, -5.28F, 0.1F, 2F, 0F, 4F, new CubeDeformation(-0.12F, 0F, -0.4F)),
                PartPose.offset(0F, -7F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
