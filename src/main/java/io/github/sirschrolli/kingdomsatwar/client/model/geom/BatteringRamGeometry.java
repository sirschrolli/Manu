package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Rammbocks (fahrbares Schutzdach mit Häuten, Widder als Pendel an zwei Ketten; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BatteringRamGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private BatteringRamGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(0, 130).addBox(10F, -10F, -28F, 3F, 3F, 56F)
                .texOffs(0, 68).addBox(10F, -25F, -29F, 3F, 3F, 58F)
                .texOffs(75, 238).addBox(10F, -22F, -28F, 3F, 12F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(88, 238).addBox(10F, -22F, -1.5F, 3F, 12F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(101, 238).addBox(10F, -22F, 25F, 3F, 12F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(208, 254).addBox(10F, -7.25F, -20F, 3F, 4F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(223, 254).addBox(10F, -7.25F, 16F, 3F, 4F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(266, 0).addBox(12.525F, -15.65F, -28F, 1F, 6F, 56F, new CubeDeformation(-0.225F, -0.15F, -0.25F))
                .texOffs(245, 264).addBox(9.5F, -10.5F, -27F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(256, 264).addBox(9.5F, -10.5F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(267, 264).addBox(9.5F, -10.5F, 26F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(119, 130).addBox(-13F, -10F, -28F, 3F, 3F, 56F)
                .texOffs(123, 68).addBox(-13F, -25F, -29F, 3F, 3F, 58F)
                .texOffs(114, 238).addBox(-13F, -22F, -28F, 3F, 12F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(127, 238).addBox(-13F, -22F, -1.5F, 3F, 12F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(140, 238).addBox(-13F, -22F, 25F, 3F, 12F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(238, 254).addBox(-13F, -7.25F, -20F, 3F, 4F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(253, 254).addBox(-13F, -7.25F, 16F, 3F, 4F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(381, 0).addBox(-13.525F, -15.65F, -28F, 1F, 6F, 56F, new CubeDeformation(-0.225F, -0.15F, -0.25F))
                .texOffs(278, 264).addBox(-13.5F, -10.5F, -27F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(289, 264).addBox(-13.5F, -10.5F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(300, 264).addBox(-13.5F, -10.5F, 26F, 4F, 4F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(311, 264).addBox(-14F, -7F, -19F, 28F, 2F, 2F)
                .texOffs(372, 264).addBox(-14F, -7F, 17F, 28F, 2F, 2F)
                .texOffs(0, 264).addBox(-10F, -9.5F, 25.25F, 20F, 2F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(47, 264).addBox(-8F, -31.5F, -28.25F, 16F, 2F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(86, 264).addBox(-8F, -31.5F, 25.25F, 16F, 2F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(139, 0).addBox(-1.5F, -38F, -30F, 3F, 3F, 60F)
                .texOffs(332, 254).addBox(-1.5F, -35.25F, -28F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(345, 254).addBox(-1.5F, -35.25F, 25F, 3F, 4F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(410, 254).addBox(-2F, -38.5F, -7F, 4F, 4F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.35F))
                .texOffs(423, 270).addBox(-4F, -34.25F, -28.8F, 8F, 3F, 1F, new CubeDeformation(-0.4F, -0.25F, -0.2F))
                .texOffs(442, 270).addBox(-4F, -34.25F, 27.8F, 8F, 3F, 1F, new CubeDeformation(-0.4F, -0.25F, -0.2F))
                .texOffs(25, 275).addBox(4.8F, -23.2F, -29.3F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(413, 238).addBox(5.35F, -22.45F, -29.15F, 5F, 10F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(40, 275).addBox(-10.8F, -23.2F, -29.3F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(426, 238).addBox(-10.35F, -22.45F, -29.15F, 5F, 10F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.25F))
                .texOffs(0, 0).addBox(-3F, -42.1F, -31.5F, 6F, 4F, 63F, new CubeDeformation(-0.4F, -0.5F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_roof_l = p_frame.addOrReplaceChild("roof_l", CubeListBuilder.create()
                .texOffs(243, 206).addBox(-0.3327F, -1.6F, -30.9F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(318, 206).addBox(-0.3327F, -1.6F, -18.7F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(393, 206).addBox(-0.3327F, -1.6F, -6.5F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(0, 222).addBox(-0.3327F, -1.6F, 5.7F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(75, 222).addBox(-0.3327F, -1.6F, 17.9F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(433, 264).addBox(0.9173F, -0.2F, -28.4F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(0, 270).addBox(0.9173F, -0.2F, -14.7F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(47, 270).addBox(0.9173F, -0.2F, -1F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(94, 270).addBox(0.9173F, -0.2F, 12.7F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(141, 270).addBox(0.9173F, -0.2F, 26.4F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(372, 130).addBox(-0.6827F, -1.975F, -31.2F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(0, 190).addBox(-0.6827F, -2.475F, -19.1F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(81, 190).addBox(-0.6827F, -1.975F, -7F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(162, 190).addBox(-0.6827F, -2.475F, 5.1F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(243, 190).addBox(-0.6827F, -1.975F, 17.2F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F)),
                PartPose.offsetAndRotation(0F, -38F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_roof_r = p_frame.addOrReplaceChild("roof_r", CubeListBuilder.create()
                .texOffs(150, 222).addBox(-23.6673F, -1.6F, -30.9F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(225, 222).addBox(-23.6673F, -1.6F, -18.7F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(300, 222).addBox(-23.6673F, -1.6F, -6.5F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(375, 222).addBox(-23.6673F, -1.6F, 5.7F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(0, 238).addBox(-23.6673F, -1.6F, 17.9F, 24F, 2F, 13F, new CubeDeformation(-0.3327F, -0.4F, -0.4F))
                .texOffs(188, 270).addBox(-21.9173F, -0.2F, -28.4F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(235, 270).addBox(-21.9173F, -0.2F, -14.7F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(282, 270).addBox(-21.9173F, -0.2F, -1F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(329, 270).addBox(-21.9173F, -0.2F, 12.7F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(376, 270).addBox(-21.9173F, -0.2F, 26.4F, 21F, 2F, 2F, new CubeDeformation(-0.0827F, -0.2F, 0F))
                .texOffs(324, 190).addBox(-25.3173F, -1.975F, -31.2F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(405, 190).addBox(-25.3173F, -2.475F, -19.1F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(0, 206).addBox(-25.3173F, -1.975F, -7F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(81, 206).addBox(-25.3173F, -2.475F, 5.1F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F))
                .texOffs(162, 206).addBox(-25.3173F, -1.975F, 17.2F, 26F, 1F, 14F, new CubeDeformation(-0.3827F, -0.225F, -0.2F)),
                PartPose.offsetAndRotation(0F, -38F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_log = p_frame.addOrReplaceChild("log", CubeListBuilder.create()
                .texOffs(365, 68).addBox(-3F, 15F, -24.25F, 6F, 4F, 55F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(246, 68).addBox(-2F, 14F, -24.25F, 4F, 6F, 55F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(191, 254).addBox(-3F, 14F, 29.45F, 6F, 6F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F))
                .texOffs(115, 254).addBox(-3.5F, 13.5F, -11F, 7F, 7F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(134, 254).addBox(-3.5F, 13.5F, 9F, 7F, 7F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(153, 254).addBox(-3.5F, 13.5F, -19F, 7F, 7F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(172, 254).addBox(-3.5F, 13.5F, 23.5F, 7F, 7F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(7, 275).addBox(-1F, 12.75F, -11F, 2F, 1F, 2F, new CubeDeformation(-0.4F, -0.05F, -0.4F))
                .texOffs(16, 275).addBox(-1F, 12.75F, 9F, 2F, 1F, 2F, new CubeDeformation(-0.4F, -0.05F, -0.4F))
                .texOffs(369, 238).addBox(-3.5F, 13.5F, -25F, 7F, 7F, 4F)
                .texOffs(392, 238).addBox(-3F, 13.4F, -29F, 6F, 7F, 4F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(302, 254).addBox(-2.5F, 13.75F, -31F, 5F, 5F, 2F, new CubeDeformation(-0.1F, -0.15F, 0F))
                .texOffs(317, 254).addBox(-2F, 16.2F, -31.8F, 4F, 4F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(268, 254).addBox(2.6F, 12.8F, -28.5F, 3F, 2F, 5F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(358, 254).addBox(2.6F, 14.4F, -23.55F, 3F, 5F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.05F))
                .texOffs(380, 254).addBox(2.6F, 17.4F, -27.25F, 3F, 2F, 4F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(461, 270).addBox(2.8F, 15.5F, -27.3F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(285, 254).addBox(-5.6F, 12.8F, -28.5F, 3F, 2F, 5F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(369, 254).addBox(-5.6F, 14.4F, -23.55F, 3F, 5F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.05F))
                .texOffs(395, 254).addBox(-5.6F, 17.4F, -27.25F, 3F, 2F, 4F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(470, 270).addBox(-4.8F, 15.5F, -27.3F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(479, 270).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, -35F, -6F));
        PartDefinition p_log_chain_front = p_log.addOrReplaceChild("log_chain_front", CubeListBuilder.create()
                .texOffs(238, 130).addBox(-0.5F, -0.2994F, -0.5F, 1F, 17F, 1F, new CubeDeformation(0F, -0.0994F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.6557F, 0F, 0F));
        PartDefinition p_log_chain_rear = p_log.addOrReplaceChild("log_chain_rear", CubeListBuilder.create()
                .texOffs(243, 130).addBox(-0.5F, -0.2994F, -0.5F, 1F, 17F, 1F, new CubeDeformation(0F, -0.0994F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.6557F, 0F, 0F));
        PartDefinition p_wheel_fl = root.addOrReplaceChild("wheel_fl", CubeListBuilder.create()
                .texOffs(248, 130).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(153, 238).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(180, 238).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(439, 238).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(462, 238).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(125, 264).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(140, 264).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(423, 254).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(467, 254).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(488, 270).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(15F, 18F, -18F));
        PartDefinition p_wheel_fr = root.addOrReplaceChild("wheel_fr", CubeListBuilder.create()
                .texOffs(279, 130).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(207, 238).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(234, 238).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(485, 238).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(0, 254).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(155, 264).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(170, 264).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(434, 254).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(474, 254).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(495, 270).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-15F, 18F, -18F));
        PartDefinition p_wheel_bl = root.addOrReplaceChild("wheel_bl", CubeListBuilder.create()
                .texOffs(310, 130).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(261, 238).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(288, 238).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(23, 254).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(46, 254).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(185, 264).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(200, 264).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(445, 254).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(481, 254).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(502, 270).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(15F, 18F, 18F));
        PartDefinition p_wheel_br = root.addOrReplaceChild("wheel_br", CubeListBuilder.create()
                .texOffs(341, 130).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(315, 238).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(342, 238).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(69, 254).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(92, 254).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(215, 264).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(230, 264).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(456, 254).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(488, 254).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(0, 275).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-15F, 18F, 18F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
