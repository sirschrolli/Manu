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
                .texOffs(458, 223).addBox(9.5F, -7.5F, -16F, 3F, 2F, 4F)
                .texOffs(473, 223).addBox(9.5F, -7.5F, 15F, 3F, 2F, 4F)
                .texOffs(119, 0).addBox(-10.75F, -11.25F, -27F, 4F, 4F, 55F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(488, 223).addBox(-12.5F, -7.5F, -16F, 3F, 2F, 4F)
                .texOffs(0, 234).addBox(-12.5F, -7.5F, 15F, 3F, 2F, 4F)
                .texOffs(366, 234).addBox(-14F, -7F, -15F, 28F, 2F, 2F)
                .texOffs(109, 234).addBox(-12.5F, -7.5F, -15.5F, 25F, 2F, 3F)
                .texOffs(427, 234).addBox(-14F, -7F, 16F, 28F, 2F, 2F)
                .texOffs(166, 234).addBox(-12.5F, -7.5F, 15.5F, 25F, 2F, 3F)
                .texOffs(103, 117).addBox(-13F, -13F, -22F, 26F, 2F, 48F)
                .texOffs(0, 60).addBox(12F, -21F, -22F, 1F, 8F, 48F)
                .texOffs(388, 60).addBox(11.5F, -22.75F, -22.5F, 2F, 2F, 49F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(76, 208).addBox(12.6F, -22F, -22F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(85, 208).addBox(12.6F, -22F, -7F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(94, 208).addBox(12.6F, -22F, 9F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(103, 208).addBox(12.6F, -22F, 24F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(99, 60).addBox(-13F, -21F, -22F, 1F, 8F, 48F)
                .texOffs(0, 117).addBox(-13.5F, -22.75F, -22.5F, 2F, 2F, 49F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(112, 208).addBox(-14.6F, -22F, -22F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(121, 208).addBox(-14.6F, -22F, -7F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(130, 208).addBox(-14.6F, -22F, 9F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(139, 208).addBox(-14.6F, -22F, 24F, 2F, 11F, 2F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(25, 208).addBox(-12F, -25F, -22F, 24F, 12F, 1F)
                .texOffs(392, 223).addBox(-12F, -19F, 25F, 24F, 6F, 1F)
                .texOffs(352, 241).addBox(9.75F, -20F, 25.8F, 2F, 2F, 1F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(359, 241).addBox(-11.75F, -20F, 25.8F, 2F, 2F, 1F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(92, 223).addBox(-12.5F, -24.5F, -28F, 25F, 2F, 7F)
                .texOffs(389, 208).addBox(10.5F, -22.75F, -27.5F, 2F, 10F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(398, 208).addBox(-12.5F, -22.75F, -27.5F, 2F, 10F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(341, 223).addBox(-11.5F, -30.25F, -22.2F, 23F, 6F, 2F, new CubeDeformation(0F, -0.25F, -0.4F))
                .texOffs(286, 241).addBox(-13F, -26.25F, -28.75F, 26F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(379, 117).addBox(2F, -7.5F, -50F, 2F, 2F, 36F)
                .texOffs(0, 169).addBox(-4F, -7.5F, -50F, 2F, 2F, 36F)
                .texOffs(223, 234).addBox(-4.5F, -8F, -36.5F, 9F, 3F, 2F)
                .texOffs(0, 241).addBox(-10F, -7.5F, -51.5F, 20F, 2F, 2F)
                .texOffs(15, 234).addBox(8.75F, -8F, -52F, 2F, 3F, 3F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(26, 234).addBox(-10.75F, -8F, -52F, 2F, 3F, 3F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(407, 208).addBox(12.1F, -30F, -20F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(416, 208).addBox(-14.1F, -30F, -20F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(45, 241).addBox(-9F, -41.9F, -20F, 18F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(425, 208).addBox(12.1F, -30F, 1F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(434, 208).addBox(-14.1F, -30F, 1F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(86, 241).addBox(-9F, -41.9F, 1F, 18F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(443, 208).addBox(12.1F, -30F, 22F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(452, 208).addBox(-14.1F, -30F, 22F, 2F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(127, 241).addBox(-9F, -41.9F, 22F, 18F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(198, 60).addBox(12.65F, -30.2F, -20.75F, 1F, 8F, 46F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(293, 60).addBox(-13.65F, -30.2F, -20.75F, 1F, 8F, 46F, new CubeDeformation(-0.15F, -0.2F, -0.25F))
                .texOffs(252, 117).addBox(-8.5F, -41.45F, -20.75F, 17F, 1F, 46F, new CubeDeformation(0F, -0.15F, -0.25F))
                .texOffs(341, 241).addBox(-1.5F, -40.3F, -21.5F, 3F, 1F, 2F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(443, 223).addBox(-2F, -39.7F, -22F, 4F, 4F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_cover_l = p_frame.addOrReplaceChild("cover_l", CubeListBuilder.create()
                .texOffs(238, 0).addBox(-0.5F, -12.6424F, -20.75F, 1F, 13F, 46F, new CubeDeformation(-0.15F, -0.1576F, -0.25F))
                .texOffs(405, 169).addBox(-1F, -12.6424F, -20F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(414, 169).addBox(-1F, -12.6424F, 1F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(423, 169).addBox(-1F, -12.6424F, 22F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F)),
                PartPose.offsetAndRotation(13.3F, -30F, 0F, 0F, 0F, -0.3883F));
        PartDefinition p_cover_r = p_frame.addOrReplaceChild("cover_r", CubeListBuilder.create()
                .texOffs(333, 0).addBox(-0.5F, -12.6424F, -20.75F, 1F, 13F, 46F, new CubeDeformation(-0.15F, -0.1576F, -0.25F))
                .texOffs(432, 169).addBox(-1F, -12.6424F, -20F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(441, 169).addBox(-1F, -12.6424F, 1F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F))
                .texOffs(450, 169).addBox(-1F, -12.6424F, 22F, 2F, 13F, 2F, new CubeDeformation(-0.4F, -0.1576F, -0.25F)),
                PartPose.offsetAndRotation(-13.3F, -30F, 0F, 0F, 0F, 0.3883F));
        PartDefinition p_cargo = p_frame.addOrReplaceChild("cargo", CubeListBuilder.create()
                .texOffs(198, 169).addBox(-10F, -8F, 8F, 8F, 8F, 8F)
                .texOffs(488, 169).addBox(2.5F, -9F, 9.5F, 7F, 9F, 5F)
                .texOffs(355, 169).addBox(3.5F, -9F, 8.5F, 5F, 9F, 7F)
                .texOffs(0, 208).addBox(2.5F, -9F, 2F, 7F, 9F, 5F)
                .texOffs(380, 169).addBox(3.5F, -9F, 1F, 5F, 9F, 7F),
                PartPose.offset(0F, -13F, 4F));
        PartDefinition p_cargo_2 = p_cargo.addOrReplaceChild("cargo_2", CubeListBuilder.create()
                .texOffs(364, 208).addBox(-9F, -14F, 9F, 6F, 6F, 6F)
                .texOffs(461, 208).addBox(-9F, -5F, -4.5F, 6F, 5F, 5F)
                .texOffs(168, 241).addBox(-8F, -6F, -3.5F, 4F, 1F, 3F)
                .texOffs(484, 208).addBox(-4F, -5F, -10.5F, 6F, 5F, 5F)
                .texOffs(183, 241).addBox(-3F, -6F, -9.5F, 4F, 1F, 3F)
                .texOffs(0, 223).addBox(2F, -5F, -8.5F, 6F, 5F, 5F)
                .texOffs(198, 241).addBox(3F, -6F, -7.5F, 4F, 1F, 3F)
                .texOffs(161, 169).addBox(-11F, -7F, -23F, 9F, 7F, 9F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cargo_3 = p_cargo.addOrReplaceChild("cargo_3", CubeListBuilder.create()
                .texOffs(77, 169).addBox(1.5F, -17F, 1.25F, 9F, 8F, 14F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(23, 223).addBox(-7F, -11F, -7.5F, 6F, 5F, 5F)
                .texOffs(213, 241).addBox(-6F, -12F, -6.5F, 4F, 1F, 3F)
                .texOffs(46, 223).addBox(-0.5F, -11F, -7F, 6F, 5F, 5F)
                .texOffs(228, 241).addBox(0.5F, -12F, -6F, 4F, 1F, 3F)
                .texOffs(124, 169).addBox(2F, -8F, -23F, 9F, 8F, 9F)
                .texOffs(459, 169).addBox(-10F, -14F, -22F, 7F, 7F, 7F)
                .texOffs(69, 223).addBox(-9F, -19F, 9.5F, 6F, 5F, 5F)
                .texOffs(243, 241).addBox(-8F, -20F, 10.5F, 4F, 1F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wheel_fl = root.addOrReplaceChild("wheel_fl", CubeListBuilder.create()
                .texOffs(231, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(148, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(175, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(157, 223).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(180, 223).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(246, 234).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(261, 234).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(37, 234).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(81, 234).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(258, 241).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(15F, 18F, -14F));
        PartDefinition p_wheel_fr = root.addOrReplaceChild("wheel_fr", CubeListBuilder.create()
                .texOffs(262, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(202, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(229, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(203, 223).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(226, 223).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(276, 234).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(291, 234).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(48, 234).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(88, 234).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(265, 241).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-15F, 18F, -14F));
        PartDefinition p_wheel_bl = root.addOrReplaceChild("wheel_bl", CubeListBuilder.create()
                .texOffs(293, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(256, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(283, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(249, 223).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(272, 223).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(306, 234).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(321, 234).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(59, 234).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(95, 234).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(272, 241).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(15F, 18F, 17F));
        PartDefinition p_wheel_br = root.addOrReplaceChild("wheel_br", CubeListBuilder.create()
                .texOffs(324, 169).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(310, 208).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(337, 208).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(295, 223).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(318, 223).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(336, 234).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(351, 234).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(70, 234).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(102, 234).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(279, 241).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-15F, 18F, 17F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
