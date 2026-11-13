package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Balliste (Fahrwagen mit Drehscheibe, Geschütz neigt um X, Bogenarme und Sehnen gespannt gebaut; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BallistaGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private BallistaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_stand = root.addOrReplaceChild("stand", CubeListBuilder.create()
                .texOffs(57, 0).addBox(5F, -8F, -10F, 3F, 3F, 20F)
                .texOffs(36, 66).addBox(5F, -5.25F, -8.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(155, 79).addBox(7.75F, -5F, -8F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.4F))
                .texOffs(49, 66).addBox(5F, -5.25F, 5.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(162, 79).addBox(7.75F, -5F, 6F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.4F))
                .texOffs(0, 79).addBox(4.5F, -8.5F, -9F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(11, 79).addBox(4.5F, -8.5F, 8F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(104, 0).addBox(-8F, -8F, -10F, 3F, 3F, 20F)
                .texOffs(62, 66).addBox(-8F, -5.25F, -8.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(169, 79).addBox(-8.75F, -5F, -8F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.4F))
                .texOffs(75, 66).addBox(-8F, -5.25F, 5.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(176, 79).addBox(-8.75F, -5F, 6F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.4F))
                .texOffs(22, 79).addBox(-8.5F, -8.5F, -9F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(33, 79).addBox(-8.5F, -8.5F, 8F, 4F, 4F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(0, 73).addBox(-5F, -7.5F, -10F, 10F, 2F, 3F)
                .texOffs(27, 73).addBox(-5F, -7.5F, 7F, 10F, 2F, 3F)
                .texOffs(146, 48).addBox(-5F, -7.75F, -2F, 10F, 3F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(175, 48).addBox(-2F, -10.25F, -2F, 4F, 3F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(199, 58).addBox(-4F, -11.75F, -2F, 8F, 2F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(239, 79).addBox(-3F, -11.75F, 2F, 6F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(67, 85).addBox(-2F, -11.75F, 3F, 4F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 85).addBox(-3F, -11.75F, -3F, 6F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(78, 85).addBox(-2F, -11.75F, -4F, 4F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(62, 48).addBox(2.25F, -15.5F, -2F, 1F, 4F, 4F)
                .texOffs(183, 79).addBox(3F, -15F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(73, 48).addBox(-3.25F, -15.5F, -2F, 1F, 4F, 4F)
                .texOffs(190, 79).addBox(-4F, -15F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(55, 79).addBox(-3.5F, -15F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.25F, -0.4F, -0.4F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_brace_l = p_stand.addOrReplaceChild("brace_l", CubeListBuilder.create()
                .texOffs(128, 48).addBox(-1F, -0.5767F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.25F, -0.0767F, -0.25F)),
                PartPose.offsetAndRotation(1.9F, -9.4F, 0F, 0F, 0F, -1.1903F));
        PartDefinition p_brace_r = p_stand.addOrReplaceChild("brace_r", CubeListBuilder.create()
                .texOffs(137, 48).addBox(-1F, -0.5767F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.25F, -0.0767F, -0.25F)),
                PartPose.offsetAndRotation(-1.9F, -9.4F, 0F, 0F, 0F, 1.1903F));
        PartDefinition p_brace_b = p_stand.addOrReplaceChild("brace_b", CubeListBuilder.create()
                .texOffs(217, 27).addBox(-1F, -0.5518F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.25F, -0.0518F, -0.25F)),
                PartPose.offsetAndRotation(0F, -9.4F, 1.9F, 1.2766F, 0F, 0F));
        PartDefinition p_brace_f = p_stand.addOrReplaceChild("brace_f", CubeListBuilder.create()
                .texOffs(226, 27).addBox(-1F, -0.5518F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.25F, -0.0518F, -0.25F)),
                PartPose.offsetAndRotation(0F, -9.4F, -1.9F, -1.2766F, 0F, 0F));
        PartDefinition p_stand_wheel_fl = p_stand.addOrReplaceChild("stand_wheel_fl", CubeListBuilder.create()
                .texOffs(103, 27).addBox(-1F, -2F, -4F, 2F, 4F, 8F)
                .texOffs(192, 48).addBox(-1F, -3F, -3F, 2F, 1F, 6F)
                .texOffs(209, 48).addBox(-1F, 2F, -3F, 2F, 1F, 6F)
                .texOffs(84, 73).addBox(-1F, -4F, -2F, 2F, 1F, 4F)
                .texOffs(97, 73).addBox(-1F, 3F, -2F, 2F, 1F, 4F)
                .texOffs(88, 66).addBox(0.8F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(154, 66).addBox(0.8F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(197, 79).addBox(1.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(9.5F, -4F, -7F));
        PartDefinition p_stand_wheel_fr = p_stand.addOrReplaceChild("stand_wheel_fr", CubeListBuilder.create()
                .texOffs(124, 27).addBox(-1F, -2F, -4F, 2F, 4F, 8F)
                .texOffs(226, 48).addBox(-1F, -3F, -3F, 2F, 1F, 6F)
                .texOffs(0, 58).addBox(-1F, 2F, -3F, 2F, 1F, 6F)
                .texOffs(110, 73).addBox(-1F, -4F, -2F, 2F, 1F, 4F)
                .texOffs(123, 73).addBox(-1F, 3F, -2F, 2F, 1F, 4F)
                .texOffs(99, 66).addBox(-1.8F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(161, 66).addBox(-1.8F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(204, 79).addBox(-2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(-9.5F, -4F, -7F));
        PartDefinition p_stand_wheel_bl = p_stand.addOrReplaceChild("stand_wheel_bl", CubeListBuilder.create()
                .texOffs(145, 27).addBox(-1F, -2F, -4F, 2F, 4F, 8F)
                .texOffs(17, 58).addBox(-1F, -3F, -3F, 2F, 1F, 6F)
                .texOffs(34, 58).addBox(-1F, 2F, -3F, 2F, 1F, 6F)
                .texOffs(136, 73).addBox(-1F, -4F, -2F, 2F, 1F, 4F)
                .texOffs(149, 73).addBox(-1F, 3F, -2F, 2F, 1F, 4F)
                .texOffs(110, 66).addBox(0.8F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(168, 66).addBox(0.8F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(211, 79).addBox(1.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(9.5F, -4F, 7F));
        PartDefinition p_stand_wheel_br = p_stand.addOrReplaceChild("stand_wheel_br", CubeListBuilder.create()
                .texOffs(166, 27).addBox(-1F, -2F, -4F, 2F, 4F, 8F)
                .texOffs(51, 58).addBox(-1F, -3F, -3F, 2F, 1F, 6F)
                .texOffs(68, 58).addBox(-1F, 2F, -3F, 2F, 1F, 6F)
                .texOffs(162, 73).addBox(-1F, -4F, -2F, 2F, 1F, 4F)
                .texOffs(175, 73).addBox(-1F, 3F, -2F, 2F, 1F, 4F)
                .texOffs(121, 66).addBox(-1.8F, -1F, -2F, 1F, 2F, 4F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(175, 66).addBox(-1.8F, -2F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(218, 79).addBox(-2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(-9.5F, -4F, 7F));
        PartDefinition p_bow = p_stand.addOrReplaceChild("bow", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2F, -1F, -15F, 4F, 2F, 24F)
                .texOffs(41, 27).addBox(1F, -1.875F, -15F, 1F, 1F, 14F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(187, 27).addBox(2F, -2F, 7F, 1F, 4F, 6F)
                .texOffs(72, 27).addBox(-2F, -1.875F, -15F, 1F, 1F, 14F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(202, 27).addBox(-3F, -2F, 7F, 1F, 4F, 6F)
                .texOffs(93, 79).addBox(-2.5F, -1.5F, -14.6F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(106, 79).addBox(-2.5F, -1.5F, 5F, 5F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(182, 66).addBox(-7.5F, -8F, -12F, 15F, 2F, 3F)
                .texOffs(219, 66).addBox(-7.5F, 1F, -12F, 15F, 2F, 3F)
                .texOffs(30, 48).addBox(3.5F, -6F, -11.5F, 2F, 7F, 2F)
                .texOffs(48, 48).addBox(2F, -6F, -11.5F, 1F, 7F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(15, 85).addBox(2.5F, -8.875F, -11.5F, 4F, 1F, 2F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(188, 73).addBox(3.5F, -8.875F, -12.5F, 2F, 1F, 4F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(28, 85).addBox(2.5F, 2.875F, -11.5F, 4F, 1F, 2F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(201, 73).addBox(3.5F, 2.875F, -12.5F, 2F, 1F, 4F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(85, 58).addBox(4F, -9.575F, -13.5F, 1F, 1F, 6F, new CubeDeformation(0F, -0.175F, 0F))
                .texOffs(145, 58).addBox(6.3F, -8.5F, -12.5F, 1F, 3F, 4F, new CubeDeformation(-0.1F, -0.25F, -0.25F))
                .texOffs(156, 58).addBox(6.3F, 0.5F, -12.5F, 1F, 3F, 4F, new CubeDeformation(-0.1F, -0.25F, -0.25F))
                .texOffs(39, 48).addBox(-5.5F, -6F, -11.5F, 2F, 7F, 2F)
                .texOffs(55, 48).addBox(-3F, -6F, -11.5F, 1F, 7F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(41, 85).addBox(-6.5F, -8.875F, -11.5F, 4F, 1F, 2F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(214, 73).addBox(-5.5F, -8.875F, -12.5F, 2F, 1F, 4F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(54, 85).addBox(-6.5F, 2.875F, -11.5F, 4F, 1F, 2F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(227, 73).addBox(-5.5F, 2.875F, -12.5F, 2F, 1F, 4F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(100, 58).addBox(-5F, -9.575F, -13.5F, 1F, 1F, 6F, new CubeDeformation(0F, -0.175F, 0F))
                .texOffs(167, 58).addBox(-7.3F, -8.5F, -12.5F, 1F, 3F, 4F, new CubeDeformation(-0.1F, -0.25F, -0.25F))
                .texOffs(178, 58).addBox(-7.3F, 0.5F, -12.5F, 1F, 3F, 4F, new CubeDeformation(-0.1F, -0.25F, -0.25F))
                .texOffs(240, 73).addBox(-1.5F, -2.75F, 6.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(225, 79).addBox(0.7F, -3.7F, 5.5F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.2F, -0.4F))
                .texOffs(232, 79).addBox(-1.7F, -3.7F, 5.5F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.2F, -0.4F))
                .texOffs(103, 85).addBox(-0.5F, 1F, 7.4F, 1F, 2F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(108, 85).addBox(-0.5F, -2.25F, 9.35F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.15F)),
                PartPose.offset(0F, -14F, 0F));
        PartDefinition p_left_arm = p_bow.addOrReplaceChild("left_arm", CubeListBuilder.create(),
                PartPose.offset(4.5F, -2.5F, -10.5F));
        PartDefinition p_left_arm_beam = p_left_arm.addOrReplaceChild("left_arm_beam", CubeListBuilder.create()
                .texOffs(224, 58).addBox(0F, -1.5F, -1.5F, 7F, 3F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(54, 73).addBox(6.55F, -1.5F, -1F, 5F, 3F, 2F, new CubeDeformation(-0.45F, -0.25F, 0F))
                .texOffs(132, 66).addBox(11F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.1F, -0.05F, -0.3F))
                .texOffs(84, 48).addBox(2.5F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.45F))
                .texOffs(95, 48).addBox(5.8F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.4887F, 0F));
        PartDefinition p_string_left = p_left_arm.addOrReplaceChild("string_left", CubeListBuilder.create(),
                PartPose.offset(11.0368F, 0F, 5.8684F));
        PartDefinition p_string_left_cord = p_string_left.addOrReplaceChild("string_left_cord", CubeListBuilder.create()
                .texOffs(151, 0).addBox(-0.5F, -0.5F, -0.4402F, 1F, 1F, 20F, new CubeDeformation(-0.25F, -0.25F, -0.4402F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.0262F, -0.9491F, 0F));
        PartDefinition p_right_arm = p_bow.addOrReplaceChild("right_arm", CubeListBuilder.create(),
                PartPose.offset(-4.5F, -2.5F, -10.5F));
        PartDefinition p_right_arm_beam = p_right_arm.addOrReplaceChild("right_arm_beam", CubeListBuilder.create()
                .texOffs(0, 66).addBox(-7F, -1.5F, -1.5F, 7F, 3F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(69, 73).addBox(-11.55F, -1.5F, -1F, 5F, 3F, 2F, new CubeDeformation(-0.45F, -0.25F, 0F))
                .texOffs(143, 66).addBox(-13F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.1F, -0.05F, -0.3F))
                .texOffs(106, 48).addBox(-3.5F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.45F))
                .texOffs(117, 48).addBox(-6.8F, -2F, -2F, 1F, 4F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.4887F, 0F));
        PartDefinition p_string_right = p_right_arm.addOrReplaceChild("string_right", CubeListBuilder.create(),
                PartPose.offset(-11.0368F, 0F, 5.8684F));
        PartDefinition p_string_right_cord = p_string_right.addOrReplaceChild("string_right_cord", CubeListBuilder.create()
                .texOffs(194, 0).addBox(-0.5F, -0.5F, -0.4402F, 1F, 1F, 20F, new CubeDeformation(-0.25F, -0.25F, -0.4402F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.0262F, 0.9491F, 0F));
        PartDefinition p_winch = p_bow.addOrReplaceChild("winch", CubeListBuilder.create()
                .texOffs(74, 79).addBox(-3.5F, -1F, -1F, 7F, 2F, 2F)
                .texOffs(21, 66).addBox(-2F, -1.5F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(189, 58).addBox(3.4F, -3F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(115, 58).addBox(3.4F, -0.5F, -3F, 1F, 1F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(119, 79).addBox(3.05F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.25F, -0.25F))
                .texOffs(194, 58).addBox(-4.4F, -3F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(130, 58).addBox(-4.4F, -0.5F, -3F, 1F, 1F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(128, 79).addBox(-5.05F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.25F, -0.25F)),
                PartPose.offset(0F, -0.25F, 11F));
        PartDefinition p_bolt = p_bow.addOrReplaceChild("bolt", CubeListBuilder.create()
                .texOffs(113, 85).addBox(-0.5F, -0.5F, -13.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(89, 85).addBox(-0.5F, -0.5F, -12.75F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(137, 79).addBox(-1F, -1F, -11F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(44, 79).addBox(-1.5F, -1.5F, -9.25F, 3F, 3F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(146, 79).addBox(-1F, -1F, -7.5F, 2F, 2F, 2F)
                .texOffs(0, 27).addBox(-1F, -1F, -5.75F, 2F, 2F, 18F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(96, 85).addBox(-1F, -1F, 12F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, -2F, -6.5F));
        PartDefinition p_bolt_fin_a = p_bolt.addOrReplaceChild("bolt_fin_a", CubeListBuilder.create()
                .texOffs(235, 27).addBox(-0.5F, 0.3F, 5.5F, 1F, 3F, 6F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bolt_fin_b = p_bolt.addOrReplaceChild("bolt_fin_b", CubeListBuilder.create()
                .texOffs(0, 48).addBox(-0.5F, 0.3F, 5.5F, 1F, 3F, 6F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 2.0944F));
        PartDefinition p_bolt_fin_c = p_bolt.addOrReplaceChild("bolt_fin_c", CubeListBuilder.create()
                .texOffs(15, 48).addBox(-0.5F, 0.3F, 5.5F, 1F, 3F, 6F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -2.0944F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
