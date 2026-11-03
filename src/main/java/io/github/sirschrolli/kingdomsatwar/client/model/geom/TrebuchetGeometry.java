package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Triboks (Wurfbalken waagerecht gebaut, Gegengewicht und Schlinge hängen, Winde hinten; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class TrebuchetGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    private TrebuchetGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_base = root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(94, 0).addBox(11F, -10F, -24F, 4F, 4F, 48F)
                .texOffs(48, 113).addBox(11F, -6F, -20.5F, 4F, 3F, 5F)
                .texOffs(67, 113).addBox(11F, -6F, 15.5F, 4F, 3F, 5F)
                .texOffs(295, 131).addBox(12.95F, -5F, -19F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.25F, -0.25F))
                .texOffs(304, 131).addBox(12.95F, -5F, 17F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.25F, -0.25F))
                .texOffs(253, 123).addBox(10.5F, -10.5F, -22.5F, 5F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(266, 123).addBox(10.5F, -10.5F, 21.5F, 5F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(199, 0).addBox(-15F, -10F, -24F, 4F, 4F, 48F)
                .texOffs(86, 113).addBox(-15F, -6F, -20.5F, 4F, 3F, 5F)
                .texOffs(105, 113).addBox(-15F, -6F, 15.5F, 4F, 3F, 5F)
                .texOffs(313, 131).addBox(-14.95F, -5F, -19F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.25F, -0.25F))
                .texOffs(322, 131).addBox(-14.95F, -5F, 17F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.25F, -0.25F))
                .texOffs(279, 123).addBox(-15.5F, -10.5F, -22.5F, 5F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(292, 123).addBox(-15.5F, -10.5F, 21.5F, 5F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(325, 113).addBox(-11F, -9.5F, -23.25F, 22F, 3F, 4F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(232, 113).addBox(-21F, -9.5F, -2F, 42F, 3F, 4F)
                .texOffs(493, 97).addBox(19F, -10F, -2.5F, 1F, 4F, 5F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(0, 113).addBox(-20F, -10F, -2.5F, 1F, 4F, 5F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(378, 113).addBox(-11F, -9.5F, 19.25F, 22F, 3F, 4F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(68, 0).addBox(11.5F, -62F, -1.5F, 3F, 52F, 3F)
                .texOffs(61, 69).addBox(11.5F, -38F, -11.5F, 3F, 3F, 23F, new CubeDeformation(-0.25F, 0F, -0.3F))
                .texOffs(304, 0).addBox(11.5F, -24F, -16F, 3F, 3F, 32F, new CubeDeformation(-0.25F, 0F, -0.4F))
                .texOffs(383, 69).addBox(10.5F, -72F, -3.5F, 5F, 10F, 7F)
                .texOffs(305, 97).addBox(10F, -71F, -4F, 6F, 1F, 8F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(334, 97).addBox(10F, -64.5F, -4F, 6F, 1F, 8F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(401, 123).addBox(15.4F, -69.5F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(81, 0).addBox(-14.5F, -62F, -1.5F, 3F, 52F, 3F)
                .texOffs(114, 69).addBox(-14.5F, -38F, -11.5F, 3F, 3F, 23F, new CubeDeformation(-0.25F, 0F, -0.3F))
                .texOffs(375, 0).addBox(-14.5F, -24F, -16F, 3F, 3F, 32F, new CubeDeformation(-0.25F, 0F, -0.4F))
                .texOffs(408, 69).addBox(-15.5F, -72F, -3.5F, 5F, 10F, 7F)
                .texOffs(363, 97).addBox(-16F, -71F, -4F, 6F, 1F, 8F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(392, 97).addBox(-16F, -64.5F, -4F, 6F, 1F, 8F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(410, 123).addBox(-16.4F, -69.5F, -1.5F, 1F, 3F, 3F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(143, 131).addBox(-16F, -69F, -1F, 32F, 2F, 2F)
                .texOffs(184, 113).addBox(8.95F, -70F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.45F, -0.4F, -0.4F))
                .texOffs(197, 113).addBox(-10.95F, -70F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.45F, -0.4F, -0.4F))
                .texOffs(169, 97).addBox(9F, -18.25F, 19.5F, 2F, 9F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(219, 123).addBox(8.5F, -11.5F, 19F, 3F, 1F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(182, 97).addBox(-11F, -18.25F, 19.5F, 2F, 9F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(236, 123).addBox(-11.5F, -11.5F, 19F, 3F, 1F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(212, 131).addBox(-11F, -12F, 25.5F, 8F, 1F, 3F)
                .texOffs(195, 97).addBox(-10.75F, -11F, 26F, 2F, 11F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(204, 97).addBox(-4.75F, -11F, 26F, 2F, 11F, 2F, new CubeDeformation(-0.25F, 0F, -0.2F))
                .texOffs(375, 131).addBox(-10F, -6.4F, 26.5F, 6F, 2F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(75, 131).addBox(-10.75F, -8.5F, 22.6F, 2F, 1F, 4F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_leg_l_front = p_base.addOrReplaceChild("leg_l_front", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2F, -1.0878F, -2F, 4F, 64F, 4F, new CubeDeformation(-0.25F, -0.0878F, -0.25F)),
                PartPose.offsetAndRotation(13F, -68F, 0F, -0.3294F, 0F, 0F));
        PartDefinition p_leg_l_rear = p_base.addOrReplaceChild("leg_l_rear", CubeListBuilder.create()
                .texOffs(17, 0).addBox(-2F, -1.0878F, -2F, 4F, 64F, 4F, new CubeDeformation(-0.25F, -0.0878F, -0.25F)),
                PartPose.offsetAndRotation(13F, -68F, 0F, 0.3294F, 0F, 0F));
        PartDefinition p_brace_l_out = p_base.addOrReplaceChild("brace_l_out", CubeListBuilder.create()
                .texOffs(446, 0).addBox(-1.5F, -0.8838F, -1.5F, 3F, 26F, 3F, new CubeDeformation(-0.25F, -0.0838F, -0.25F)),
                PartPose.offsetAndRotation(14F, -33F, 0F, 0F, 0F, -0.229F));
        PartDefinition p_brace_l_rear = p_base.addOrReplaceChild("brace_l_rear", CubeListBuilder.create()
                .texOffs(273, 69).addBox(-1F, -0.6759F, -1F, 2F, 18F, 2F, new CubeDeformation(0F, -0.0759F, 0F)),
                PartPose.offsetAndRotation(13F, -23.5F, 12F, -2.4669F, 0F, 0F));
        PartDefinition p_brace_l_front = p_base.addOrReplaceChild("brace_l_front", CubeListBuilder.create()
                .texOffs(282, 69).addBox(-1F, -0.6759F, -1F, 2F, 18F, 2F, new CubeDeformation(0F, -0.0759F, 0F)),
                PartPose.offsetAndRotation(13F, -23.5F, -12F, 2.4669F, 0F, 0F));
        PartDefinition p_leg_r_front = p_base.addOrReplaceChild("leg_r_front", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-2F, -1.0878F, -2F, 4F, 64F, 4F, new CubeDeformation(-0.25F, -0.0878F, -0.25F)),
                PartPose.offsetAndRotation(-13F, -68F, 0F, -0.3294F, 0F, 0F));
        PartDefinition p_leg_r_rear = p_base.addOrReplaceChild("leg_r_rear", CubeListBuilder.create()
                .texOffs(51, 0).addBox(-2F, -1.0878F, -2F, 4F, 64F, 4F, new CubeDeformation(-0.25F, -0.0878F, -0.25F)),
                PartPose.offsetAndRotation(-13F, -68F, 0F, 0.3294F, 0F, 0F));
        PartDefinition p_brace_r_out = p_base.addOrReplaceChild("brace_r_out", CubeListBuilder.create()
                .texOffs(459, 0).addBox(-1.5F, -0.8838F, -1.5F, 3F, 26F, 3F, new CubeDeformation(-0.25F, -0.0838F, -0.25F)),
                PartPose.offsetAndRotation(-14F, -33F, 0F, 0F, 0F, 0.229F));
        PartDefinition p_brace_r_rear = p_base.addOrReplaceChild("brace_r_rear", CubeListBuilder.create()
                .texOffs(291, 69).addBox(-1F, -0.6759F, -1F, 2F, 18F, 2F, new CubeDeformation(0F, -0.0759F, 0F)),
                PartPose.offsetAndRotation(-13F, -23.5F, 12F, -2.4669F, 0F, 0F));
        PartDefinition p_brace_r_front = p_base.addOrReplaceChild("brace_r_front", CubeListBuilder.create()
                .texOffs(300, 69).addBox(-1F, -0.6759F, -1F, 2F, 18F, 2F, new CubeDeformation(0F, -0.0759F, 0F)),
                PartPose.offsetAndRotation(-13F, -23.5F, -12F, 2.4669F, 0F, 0F));
        PartDefinition p_winch = p_base.addOrReplaceChild("winch", CubeListBuilder.create()
                .texOffs(174, 123).addBox(-9.5F, -1.5F, -1.5F, 19F, 3F, 3F)
                .texOffs(23, 113).addBox(-4F, -2F, -2F, 8F, 4F, 4F)
                .texOffs(210, 113).addBox(5.5F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(13, 113).addBox(7F, -4F, -0.5F, 1F, 8F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(421, 97).addBox(7F, -0.5F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(221, 113).addBox(-6.5F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(18, 113).addBox(-8F, -4F, -0.5F, 1F, 8F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(440, 97).addBox(-8F, -0.5F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, -15F, 21.5F));
        PartDefinition p_base_wheel_fl = p_base.addOrReplaceChild("base_wheel_fl", CubeListBuilder.create()
                .texOffs(213, 97).addBox(-1.5F, -2F, -4F, 3F, 4F, 8F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(456, 113).addBox(-1.5F, -3F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(475, 113).addBox(-1.5F, 2F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(456, 123).addBox(-1.5F, -4F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(471, 123).addBox(-1.5F, 3F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(357, 123).addBox(1.15F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(428, 123).addBox(1.15F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(340, 131).addBox(1.8F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(16.25F, -4F, -18F));
        PartDefinition p_base_wheel_fr = p_base.addOrReplaceChild("base_wheel_fr", CubeListBuilder.create()
                .texOffs(236, 97).addBox(-1.5F, -2F, -4F, 3F, 4F, 8F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(494, 113).addBox(-1.5F, -3F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(0, 123).addBox(-1.5F, 2F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(486, 123).addBox(-1.5F, -4F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(0, 131).addBox(-1.5F, 3F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(368, 123).addBox(-2.15F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(435, 123).addBox(-2.15F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(347, 131).addBox(-2.8F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(-16.25F, -4F, -18F));
        PartDefinition p_base_wheel_bl = p_base.addOrReplaceChild("base_wheel_bl", CubeListBuilder.create()
                .texOffs(259, 97).addBox(-1.5F, -2F, -4F, 3F, 4F, 8F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(19, 123).addBox(-1.5F, -3F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(38, 123).addBox(-1.5F, 2F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(15, 131).addBox(-1.5F, -4F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(30, 131).addBox(-1.5F, 3F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(379, 123).addBox(1.15F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(442, 123).addBox(1.15F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(354, 131).addBox(1.8F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(16.25F, -4F, 18F));
        PartDefinition p_base_wheel_br = p_base.addOrReplaceChild("base_wheel_br", CubeListBuilder.create()
                .texOffs(282, 97).addBox(-1.5F, -2F, -4F, 3F, 4F, 8F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(57, 123).addBox(-1.5F, -3F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(76, 123).addBox(-1.5F, 2F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(45, 131).addBox(-1.5F, -4F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(60, 131).addBox(-1.5F, 3F, -2F, 3F, 1F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(390, 123).addBox(-2.15F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(449, 123).addBox(-2.15F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(361, 131).addBox(-2.8F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(-16.25F, -4F, 18F));
        PartDefinition p_arm = p_base.addOrReplaceChild("arm", CubeListBuilder.create()
                .texOffs(130, 97).addBox(-3F, -3.5F, -4F, 6F, 7F, 8F)
                .texOffs(348, 69).addBox(-2.5F, -3F, -16F, 5F, 6F, 12F)
                .texOffs(230, 69).addBox(-2.5F, -2.5F, 4F, 5F, 5F, 16F)
                .texOffs(167, 69).addBox(-2F, -2F, 20F, 4F, 4F, 18F)
                .texOffs(309, 69).addBox(-1.5F, -1.5F, 38F, 3F, 3F, 16F)
                .texOffs(305, 123).addBox(-2F, -2F, 54F, 4F, 4F, 2F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(235, 131).addBox(-4.5F, -1F, -17F, 9F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(124, 113).addBox(-3F, -3.5F, -13.5F, 6F, 7F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(139, 113).addBox(-3F, -3.5F, -8.5F, 6F, 7F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(459, 97).addBox(-3.5F, -4F, -3F, 7F, 8F, 1F, new CubeDeformation(-0.35F, -0.2F, 0F))
                .texOffs(476, 97).addBox(-3.5F, -4F, 2F, 7F, 8F, 1F, new CubeDeformation(-0.35F, -0.2F, 0F))
                .texOffs(95, 123).addBox(-3F, -3F, 8.5F, 6F, 6F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(110, 123).addBox(-3F, -3F, 16.5F, 6F, 6F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(318, 123).addBox(-2.5F, -2.5F, 25.5F, 5F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(331, 123).addBox(-2.5F, -2.5F, 33.5F, 5F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(101, 131).addBox(-2F, -2F, 45.5F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, -68F, 0F));
        PartDefinition p_counterweight = p_arm.addOrReplaceChild("counterweight", CubeListBuilder.create()
                .texOffs(212, 69).addBox(2.6F, -2.05F, -1.5F, 1F, 19F, 3F, new CubeDeformation(0F, -0.45F, -0.25F))
                .texOffs(112, 131).addBox(2.1F, 7.5F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(221, 69).addBox(-3.6F, -2.05F, -1.5F, 1F, 19F, 3F, new CubeDeformation(0F, -0.45F, -0.25F))
                .texOffs(123, 131).addBox(-4.1F, 7.5F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(125, 123).addBox(-10F, 15F, -2F, 20F, 2F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(0, 69).addBox(-9F, 17F, -6F, 18F, 15F, 12F)
                .texOffs(0, 97).addBox(-9.5F, 19.1F, -6.5F, 19F, 2F, 13F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(65, 97).addBox(-9.5F, 28.6F, -6.5F, 19F, 2F, 13F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(433, 69).addBox(8.45F, 16.55F, 5.45F, 1F, 16F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(438, 69).addBox(8.45F, 16.55F, -6.45F, 1F, 16F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(443, 69).addBox(-9.45F, 16.55F, 5.45F, 1F, 16F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(448, 69).addBox(-9.45F, 16.55F, -6.45F, 1F, 16F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(390, 131).addBox(-6.2F, 15.7F, -4.4F, 4F, 1F, 2F, new CubeDeformation(0F, 0F, -0.5F))
                .texOffs(134, 131).addBox(-5.2F, 14.7F, -4.4F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.5F))
                .texOffs(88, 131).addBox(-5.2F, 15.7F, -5.4F, 2F, 1F, 4F, new CubeDeformation(0F, 0F, -0.5F))
                .texOffs(418, 131).addBox(1.3F, 15.9F, 2.8F, 5F, 1F, 1F)
                .texOffs(331, 131).addBox(2.3F, 14.9F, 2.8F, 3F, 3F, 1F)
                .texOffs(258, 131).addBox(2.3F, 15.9F, 1.8F, 3F, 1F, 3F)
                .texOffs(431, 131).addBox(3.1F, 16.6F, -4.4F, 3F, 0F, 2F, new CubeDeformation(0F, 0F, -0.5F))
                .texOffs(368, 131).addBox(4.1F, 15.6F, -4.4F, 1F, 2F, 2F, new CubeDeformation(0F, 0F, -0.5F))
                .texOffs(284, 131).addBox(4.1F, 16.6F, -5.4F, 1F, 0F, 4F, new CubeDeformation(0F, 0F, -0.5F)),
                PartPose.offset(0F, 0F, -16F));
        PartDefinition p_sling = p_arm.addOrReplaceChild("sling", CubeListBuilder.create()
                .texOffs(403, 131).addBox(-3F, 1.6F, -0.5F, 6F, 1F, 1F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(159, 97).addBox(1.75F, 2.4F, -0.5F, 1F, 14F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(154, 113).addBox(1.875F, 16.4F, -3F, 1F, 2F, 6F, new CubeDeformation(-0.125F, 0F, 0F))
                .texOffs(164, 97).addBox(-2.75F, 2.4F, -0.5F, 1F, 14F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(169, 113).addBox(-2.875F, 16.4F, -3F, 1F, 2F, 6F, new CubeDeformation(-0.125F, 0F, 0F))
                .texOffs(431, 113).addBox(-3F, 18.4F, -3F, 6F, 1F, 6F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 0F, 55F));
        PartDefinition p_stone = p_sling.addOrReplaceChild("stone", CubeListBuilder.create()
                .texOffs(271, 131).addBox(-2F, -1F, -1F, 4F, 2F, 2F)
                .texOffs(419, 123).addBox(-1F, -2F, -1F, 2F, 4F, 2F)
                .texOffs(344, 123).addBox(-1F, -1F, -2F, 2F, 2F, 4F),
                PartPose.offset(0F, 16.4F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
