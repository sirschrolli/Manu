package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Katapults (Onager auf vier Rädern; Wurfarm senkrecht = abgefeuert, Becher, Stein und Winde als eigene Knochen; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CatapultGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private CatapultGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(0, 0).addBox(9F, -13F, -19F, 3F, 4F, 43F)
                .texOffs(0, 89).addBox(9F, -9.25F, -14F, 3F, 5F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(15, 89).addBox(9F, -9.25F, 12F, 3F, 5F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(93, 48).addBox(9F, -25F, 1.5F, 3F, 12F, 3F)
                .texOffs(21, 107).addBox(8.5F, -13.5F, -18F, 4F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(32, 107).addBox(8.5F, -13.5F, 20.7F, 4F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(225, 107).addBox(8.5F, -15F, 1F, 4F, 1F, 4F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(232, 65).addBox(11.7F, -17.5F, 0.25F, 1F, 6F, 4F, new CubeDeformation(-0.3F, 0F, -0.25F))
                .texOffs(116, 89).addBox(11.75F, -13.5F, 5F, 1F, 5F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(90, 89).addBox(11.75F, -13F, 4.5F, 1F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(0, 78).addBox(12.3F, -15.5F, 6.5F, 1F, 9F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(93, 0).addBox(-12F, -13F, -19F, 3F, 4F, 43F)
                .texOffs(30, 89).addBox(-12F, -9.25F, -14F, 3F, 5F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(45, 89).addBox(-12F, -9.25F, 12F, 3F, 5F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(106, 48).addBox(-12F, -25F, 1.5F, 3F, 12F, 3F)
                .texOffs(43, 107).addBox(-12.5F, -13.5F, -18F, 4F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(54, 107).addBox(-12.5F, -13.5F, 20.7F, 4F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(0, 114).addBox(-12.5F, -15F, 1F, 4F, 1F, 4F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(243, 65).addBox(-12.7F, -17.5F, 0.25F, 1F, 6F, 4F, new CubeDeformation(-0.3F, 0F, -0.25F))
                .texOffs(127, 89).addBox(-12.75F, -13.5F, 5F, 1F, 5F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(103, 89).addBox(-12.75F, -13F, 4.5F, 1F, 4F, 5F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(5, 78).addBox(-13.3F, -15.5F, 6.5F, 1F, 9F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(131, 99).addBox(-9F, -13F, -19F, 18F, 3F, 3F)
                .texOffs(174, 99).addBox(-9F, -12.25F, 1.5F, 18F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(146, 107).addBox(-9F, -12.25F, 21.5F, 18F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(193, 120).addBox(-2.5F, -12.675F, 21.5F, 5F, 1F, 2F, new CubeDeformation(0F, -0.275F, -0.3F))
                .texOffs(0, 120).addBox(-13F, -7F, -13F, 26F, 2F, 2F)
                .texOffs(57, 120).addBox(-13F, -7F, 13F, 26F, 2F, 2F)
                .texOffs(0, 99).addBox(-12.5F, -28F, 1F, 25F, 3F, 4F)
                .texOffs(60, 89).addBox(10.6F, -28.5F, 0.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, -0.25F, -0.25F))
                .texOffs(75, 89).addBox(-12.6F, -28.5F, 0.5F, 2F, 4F, 5F, new CubeDeformation(-0.3F, -0.25F, -0.25F))
                .texOffs(17, 114).addBox(-3.5F, -28.5F, 4.75F, 7F, 4F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(126, 65).addBox(-10F, -13.5F, 4.5F, 20F, 5F, 5F)
                .texOffs(138, 89).addBox(-9.5F, -14F, 5.5F, 2F, 6F, 3F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(149, 89).addBox(7.5F, -14F, 5.5F, 2F, 6F, 3F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(59, 99).addBox(-15F, -16F, 16.5F, 7F, 1F, 6F)
                .texOffs(208, 120).addBox(-11.5F, -15F, 17.5F, 2F, 2F, 1F)
                .texOffs(215, 120).addBox(-11.5F, -15F, 20.5F, 2F, 2F, 1F),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_brace_front_l = p_frame.addOrReplaceChild("brace_front_l", CubeListBuilder.create()
                .texOffs(199, 0).addBox(-1F, -0.6592F, -1F, 2F, 19F, 2F, new CubeDeformation(0F, -0.0592F, 0F)),
                PartPose.offsetAndRotation(10.5F, -23.5F, 1.5F, -0.9137F, 0F, 0F));
        PartDefinition p_brace_rear_l = p_frame.addOrReplaceChild("brace_rear_l", CubeListBuilder.create()
                .texOffs(119, 48).addBox(-1F, -0.9067F, -1F, 2F, 13F, 2F, new CubeDeformation(0F, -0.3067F, 0F)),
                PartPose.offsetAndRotation(10.5F, -21F, 4.5F, 0.7348F, 0F, 0F));
        PartDefinition p_brace_front_r = p_frame.addOrReplaceChild("brace_front_r", CubeListBuilder.create()
                .texOffs(208, 0).addBox(-1F, -0.6592F, -1F, 2F, 19F, 2F, new CubeDeformation(0F, -0.0592F, 0F)),
                PartPose.offsetAndRotation(-10.5F, -23.5F, 1.5F, -0.9137F, 0F, 0F));
        PartDefinition p_brace_rear_r = p_frame.addOrReplaceChild("brace_rear_r", CubeListBuilder.create()
                .texOffs(128, 48).addBox(-1F, -0.9067F, -1F, 2F, 13F, 2F, new CubeDeformation(0F, -0.3067F, 0F)),
                PartPose.offsetAndRotation(-10.5F, -21F, 4.5F, 0.7348F, 0F, 0F));
        PartDefinition p_arm = p_frame.addOrReplaceChild("arm", CubeListBuilder.create()
                .texOffs(186, 0).addBox(-1.5F, -26F, -1.5F, 3F, 28F, 3F)
                .texOffs(198, 65).addBox(-2F, -9F, -2F, 4F, 6F, 4F)
                .texOffs(217, 99).addBox(-2.5F, -8F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(0, 107).addBox(-2.5F, -5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(34, 114).addBox(-2F, -15F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(51, 114).addBox(-2F, -20F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(68, 114).addBox(-2F, -26.4F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, -11F, 7F));
        PartDefinition p_cup = p_arm.addOrReplaceChild("cup", CubeListBuilder.create()
                .texOffs(194, 78).addBox(-4F, -4F, -1F, 8F, 8F, 1F)
                .texOffs(108, 65).addBox(3F, -4F, -4F, 1F, 8F, 3F)
                .texOffs(117, 65).addBox(-4F, -4F, -4F, 1F, 8F, 3F)
                .texOffs(114, 120).addBox(-3F, -4F, -4F, 6F, 1F, 3F)
                .texOffs(133, 120).addBox(-3F, 3F, -4F, 6F, 1F, 3F)
                .texOffs(187, 107).addBox(-2.5F, -3.5F, -0.5F, 5F, 1F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(206, 107).addBox(-2.5F, 2.5F, -0.5F, 5F, 1F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.1F)),
                PartPose.offset(0F, -22F, -1.5F));
        PartDefinition p_stone = p_cup.addOrReplaceChild("stone", CubeListBuilder.create()
                .texOffs(195, 89).addBox(-3F, -2F, -2F, 6F, 4F, 4F)
                .texOffs(215, 65).addBox(-2F, -3F, -2F, 4F, 6F, 4F)
                .texOffs(177, 65).addBox(-2F, -2F, -3F, 4F, 4F, 6F)
                .texOffs(152, 120).addBox(-1.75F, -3.7F, -1.25F, 3F, 1F, 3F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(109, 107).addBox(2.5F, -0.85F, -2.35F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.35F, -0.15F)),
                PartPose.offset(0F, 0F, -4F));
        PartDefinition p_winch = p_frame.addOrReplaceChild("winch", CubeListBuilder.create()
                .texOffs(86, 99).addBox(-9.5F, -1.5F, -1.5F, 19F, 3F, 3F)
                .texOffs(170, 89).addBox(-4F, -2F, -2F, 8F, 4F, 4F)
                .texOffs(216, 89).addBox(7.9F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(160, 89).addBox(6.5F, -4F, -0.5F, 1F, 8F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(213, 78).addBox(6.5F, -0.5F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(227, 89).addBox(-8.9F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(165, 89).addBox(-7.5F, -4F, -0.5F, 1F, 8F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(232, 78).addBox(-7.5F, -0.5F, -4F, 1F, 1F, 8F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, -9.5F, 18F));
        PartDefinition p_wheel_fl = root.addOrReplaceChild("wheel_fl", CubeListBuilder.create()
                .texOffs(217, 0).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(137, 48).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(164, 48).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(10, 78).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(33, 78).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(85, 114).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(100, 114).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(65, 107).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(118, 107).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(165, 120).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(14F, 18F, -12F));
        PartDefinition p_wheel_fr = root.addOrReplaceChild("wheel_fr", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(191, 48).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(218, 48).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(56, 78).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(79, 78).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(115, 114).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(130, 114).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(76, 107).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(125, 107).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(172, 120).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-14F, 18F, -12F));
        PartDefinition p_wheel_bl = root.addOrReplaceChild("wheel_bl", CubeListBuilder.create()
                .texOffs(31, 48).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(0, 65).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(27, 65).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(102, 78).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(125, 78).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(145, 114).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(160, 114).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(87, 107).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(132, 107).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(179, 120).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(14F, 18F, 14F));
        PartDefinition p_wheel_br = root.addOrReplaceChild("wheel_br", CubeListBuilder.create()
                .texOffs(62, 48).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(54, 65).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(81, 65).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(148, 78).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(171, 78).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(175, 114).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(190, 114).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(98, 107).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(139, 107).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(186, 120).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-14F, 18F, 14F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
