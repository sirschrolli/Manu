package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Planwagens (Kasten auf vier Rädern, Plane über drei Bögen, Kutschbock, Deichsel; Ladung als eigener Knochen; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WagonGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private WagonGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(0, 0).addBox(6.75F, -11.25F, -27F, 4F, 4F, 55F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(255, 221).addBox(9.5F, -7.5F, -16F, 3F, 2F, 4F)
                .texOffs(270, 221).addBox(9.5F, -7.5F, 15F, 3F, 2F, 4F)
                .texOffs(119, 0).addBox(-10.75F, -11.25F, -27F, 4F, 4F, 55F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(285, 221).addBox(-12.5F, -7.5F, -16F, 3F, 2F, 4F)
                .texOffs(300, 221).addBox(-12.5F, -7.5F, 15F, 3F, 2F, 4F)
                .texOffs(200, 231).addBox(-14F, -7F, -15F, 28F, 2F, 2F)
                .texOffs(409, 221).addBox(-12.5F, -7.5F, -15.5F, 25F, 2F, 3F)
                .texOffs(261, 231).addBox(-14F, -7F, 16F, 28F, 2F, 2F)
                .texOffs(0, 231).addBox(-12.5F, -7.5F, 15.5F, 25F, 2F, 3F)
                .texOffs(103, 117).addBox(-13F, -13F, -22F, 26F, 2F, 48F)
                .texOffs(0, 60).addBox(12F, -21F, -22F, 1F, 8F, 48F)
                .texOffs(388, 60).addBox(11.5F, -22.75F, -22.5F, 2F, 2F, 49F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(439, 169).addBox(12.6F, -22F, -22F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(448, 169).addBox(12.6F, -22F, -7F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(457, 169).addBox(12.6F, -22F, 9F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(466, 169).addBox(12.6F, -22F, 24F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(99, 60).addBox(-13F, -21F, -22F, 1F, 8F, 48F)
                .texOffs(0, 117).addBox(-13.5F, -22.75F, -22.5F, 2F, 2F, 49F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(475, 169).addBox(-14.6F, -22F, -22F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(484, 169).addBox(-14.6F, -22F, -7F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(493, 169).addBox(-14.6F, -22F, 9F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(502, 169).addBox(-14.6F, -22F, 24F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(388, 169).addBox(-12F, -25F, -22F, 24F, 12F, 1F)
                .texOffs(189, 221).addBox(-12F, -19F, 25F, 24F, 6F, 1F)
                .texOffs(124, 237).addBox(9.75F, -20F, 25.8F, 2F, 2F, 1F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(131, 237).addBox(-11.75F, -20F, 25.8F, 2F, 2F, 1F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(382, 208).addBox(-12.5F, -24.5F, -28F, 25F, 2F, 7F)
                .texOffs(241, 208).addBox(10.5F, -22.75F, -27.5F, 2F, 10F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(250, 208).addBox(-12.5F, -22.75F, -27.5F, 2F, 10F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(138, 221).addBox(-11.5F, -30.25F, -22.2F, 23F, 6F, 2F, new CubeDeformation(0F, -0.25F, -0.4F))
                .texOffs(58, 237).addBox(-13F, -26.25F, -28.75F, 26F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(379, 117).addBox(2F, -7.5F, -50F, 2F, 2F, 36F)
                .texOffs(0, 169).addBox(-4F, -7.5F, -50F, 2F, 2F, 36F)
                .texOffs(57, 231).addBox(-4.5F, -8F, -36.5F, 9F, 3F, 2F)
                .texOffs(322, 231).addBox(-10F, -7.5F, -51.5F, 20F, 2F, 2F)
                .texOffs(315, 221).addBox(8.75F, -8F, -52F, 2F, 3F, 3F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(326, 221).addBox(-10.75F, -8F, -52F, 2F, 3F, 3F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(259, 208).addBox(12.1F, -30F, -20F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(268, 208).addBox(-14.1F, -30F, -20F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(367, 231).addBox(-9F, -41.9F, -20F, 18F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(277, 208).addBox(12.1F, -30F, 1F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(286, 208).addBox(-14.1F, -30F, 1F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(408, 231).addBox(-9F, -41.9F, 1F, 18F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(295, 208).addBox(12.1F, -30F, 22F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(304, 208).addBox(-14.1F, -30F, 22F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(449, 231).addBox(-9F, -41.9F, 22F, 18F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(198, 60).addBox(12.65F, -30.2F, -20.75F, 1F, 8F, 46F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(293, 60).addBox(-13.65F, -30.2F, -20.75F, 1F, 8F, 46F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(252, 117).addBox(-8.5F, -41.45F, -20.75F, 17F, 1F, 46F, new CubeDeformation(0F, -0.15F, -0.25F))
                .texOffs(113, 237).addBox(-1.5F, -40.3F, -21.5F, 3F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(240, 221).addBox(-2F, -39.7F, -22F, 4F, 4F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cover_l = p_frame.addOrReplaceChild("cover_l", CubeListBuilder.create()
                .texOffs(238, 0).addBox(-0.5F, -12.6424F, -20.75F, 1F, 13F, 46F, new CubeDeformation(-0.15F, -0.1576F, -0.25F))
                .texOffs(284, 169).addBox(-1F, -12.6424F, -20F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(293, 169).addBox(-1F, -12.6424F, 1F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(302, 169).addBox(-1F, -12.6424F, 22F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F)),
                PartPose.offsetAndRotation(13.3F, -30F, 0F, 0F, 0F, -0.3883F));
        PartDefinition p_cover_r = p_frame.addOrReplaceChild("cover_r", CubeListBuilder.create()
                .texOffs(333, 0).addBox(-0.5F, -12.6424F, -20.75F, 1F, 13F, 46F, new CubeDeformation(-0.15F, -0.1576F, -0.25F))
                .texOffs(311, 169).addBox(-1F, -12.6424F, -20F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(320, 169).addBox(-1F, -12.6424F, 1F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(329, 169).addBox(-1F, -12.6424F, 22F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F)),
                PartPose.offsetAndRotation(-13.3F, -30F, 0F, 0F, 0F, 0.3883F));
        PartDefinition p_cargo = p_frame.addOrReplaceChild("cargo", CubeListBuilder.create()
                .texOffs(77, 169).addBox(-10F, -8F, 8F, 8F, 8F, 8F)
                .texOffs(216, 208).addBox(-9F, -14F, 9F, 6F, 6F, 6F)
                .texOffs(338, 169).addBox(2.5F, -9F, 9.5F, 7F, 9F, 5F)
                .texOffs(234, 169).addBox(3.5F, -9F, 8.5F, 5F, 9F, 7F)
                .texOffs(363, 169).addBox(2.5F, -9F, 2F, 7F, 9F, 5F)
                .texOffs(259, 169).addBox(3.5F, -9F, 1F, 5F, 9F, 7F)
                .texOffs(313, 208).addBox(-9F, -5F, -4.5F, 6F, 5F, 5F)
                .texOffs(490, 231).addBox(-8F, -6F, -3.5F, 4F, 1F, 3F)
                .texOffs(336, 208).addBox(-4F, -5F, -10.5F, 6F, 5F, 5F)
                .texOffs(0, 237).addBox(-3F, -6F, -9.5F, 4F, 1F, 3F)
                .texOffs(359, 208).addBox(2F, -5F, -8.5F, 6F, 5F, 5F)
                .texOffs(15, 237).addBox(3F, -6F, -7.5F, 4F, 1F, 3F),
                PartPose.offset(0F, -13F, 4F));
        PartDefinition p_wheel_fl = root.addOrReplaceChild("wheel_fl", CubeListBuilder.create()
                .texOffs(110, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(0, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(27, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(447, 208).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(470, 208).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(80, 231).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(95, 231).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(337, 221).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(381, 221).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(30, 237).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(15F, 18F, -14F));
        PartDefinition p_wheel_fr = root.addOrReplaceChild("wheel_fr", CubeListBuilder.create()
                .texOffs(141, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(54, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(81, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(0, 221).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(23, 221).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(110, 231).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(125, 231).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(348, 221).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(388, 221).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(37, 237).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-15F, 18F, -14F));
        PartDefinition p_wheel_bl = root.addOrReplaceChild("wheel_bl", CubeListBuilder.create()
                .texOffs(172, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(108, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(135, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(46, 221).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(69, 221).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(140, 231).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(155, 231).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(359, 221).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(395, 221).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(44, 237).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(15F, 18F, 17F));
        PartDefinition p_wheel_br = root.addOrReplaceChild("wheel_br", CubeListBuilder.create()
                .texOffs(203, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(162, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(189, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(92, 221).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(115, 221).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(170, 231).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(185, 231).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(370, 221).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(402, 221).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(51, 237).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-15F, 18F, 17F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
