package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Stundenwächters (seit 1.01): Ritter aus Uhrbronze über geschwärztem Stahl, Uhrwerkfenster mit zwei Zahnrädern in der Brust, Topfhelm mit T-Sehschlitz und Zeigerkamm, ein Zifferblatt wie ein Heiligenschein hinter dem Kopf mit gebläuten Zeigern, Schulterstücke mit Zahnrädern, Minutenzeiger und Stundenzeiger als Klingen, Pendel und zwei Uhrgewichte am Gürtel, drei kreisende Zahnräder; im Spiel ×1,6.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class HourWardenGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 128;

    private HourWardenGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(183, 0).addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(200, 48).addBox(-2.5F, 0.1F, -2.9F, 5F, 6F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(209, 67).addBox(-2.5F, 0.1F, -2.9F, 5F, 1F, 3F, new CubeDeformation(0F, -0.1F, -0.2F)),
                PartPose.offset(3F, 6.5F, 0.2F));
        PartDefinition p_shin_l = p_leg_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-2F, -0.2F, -2F, 4F, 9F, 4F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(54, 37).addBox(-2.5F, 1F, -2.6F, 5F, 7F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(226, 67).addBox(-2.5F, 7.45F, -2.65F, 5F, 1F, 3F, new CubeDeformation(-0.2F, -0.15F, -0.15F))
                .texOffs(58, 48).addBox(-2.5F, 8.2F, -4.7F, 5F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(34, 74).addBox(-2F, 8.4F, -6F, 4F, 2F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.4F))
                .texOffs(150, 67).addBox(-2F, -1.4F, -3.3F, 4F, 3F, 2F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_knee_gear_l = p_shin_l.addOrReplaceChild("knee_gear_l", CubeListBuilder.create()
                .texOffs(86, 74).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.25F)),
                PartPose.offset(0F, 0.1F, -3.6F));
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(204, 0).addBox(-2.5F, 0F, -2.5F, 5F, 8F, 5F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(217, 48).addBox(-2.5F, 0.1F, -2.9F, 5F, 6F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(0, 74).addBox(-2.5F, 0.1F, -2.9F, 5F, 1F, 3F, new CubeDeformation(0F, -0.1F, -0.2F)),
                PartPose.offset(-3F, 6.5F, 0.2F));
        PartDefinition p_shin_r = p_leg_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(17, 23).addBox(-2F, -0.2F, -2F, 4F, 9F, 4F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(71, 37).addBox(-2.5F, 1F, -2.6F, 5F, 7F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(17, 74).addBox(-2.5F, 7.45F, -2.65F, 5F, 1F, 3F, new CubeDeformation(-0.2F, -0.15F, -0.15F))
                .texOffs(83, 48).addBox(-2.5F, 8.2F, -4.7F, 5F, 2F, 7F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(47, 74).addBox(-2F, 8.4F, -6F, 4F, 2F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.4F))
                .texOffs(163, 67).addBox(-2F, -1.4F, -3.3F, 4F, 3F, 2F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_knee_gear_r = p_shin_r.addOrReplaceChild("knee_gear_r", CubeListBuilder.create()
                .texOffs(95, 74).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.25F)),
                PartPose.offset(0F, 0.1F, -3.6F));
        PartDefinition p_hips = root.addOrReplaceChild("hips", CubeListBuilder.create()
                .texOffs(83, 23).addBox(-5F, -2.9F, -3F, 10F, 5F, 6F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(148, 37).addBox(-5F, -3.1F, -3.5F, 10F, 2F, 7F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(0, 79).addBox(-1.5F, -3.1F, -3.8F, 3F, 2F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_tasset_fl = p_hips.addOrReplaceChild("tasset_fl", CubeListBuilder.create()
                .texOffs(147, 58).addBox(-2F, -0.4F, -0.5F, 4F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, -3.2F, -0.1571F, 0F, 0F));
        PartDefinition p_tasset_fr = p_hips.addOrReplaceChild("tasset_fr", CubeListBuilder.create()
                .texOffs(158, 58).addBox(-2F, -0.4F, -0.5F, 4F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, -3.2F, -0.1571F, 0F, 0F));
        PartDefinition p_tasset_bl = p_hips.addOrReplaceChild("tasset_bl", CubeListBuilder.create()
                .texOffs(169, 58).addBox(-2F, -0.4F, -0.5F, 4F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(2.2F, -1.4F, 3.2F, -0.1571F, 3.1416F, 0F));
        PartDefinition p_tasset_br = p_hips.addOrReplaceChild("tasset_br", CubeListBuilder.create()
                .texOffs(180, 58).addBox(-2F, -0.4F, -0.5F, 4F, 6F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(-2.2F, -1.4F, 3.2F, -0.1571F, 3.1416F, 0F));
        PartDefinition p_tasset_l = p_hips.addOrReplaceChild("tasset_l", CubeListBuilder.create()
                .texOffs(208, 23).addBox(-0.5F, -0.2F, -2.5F, 1F, 6F, 5F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offsetAndRotation(4.9F, -1.4F, 0F, 0F, 0F, -0.1222F));
        PartDefinition p_tasset_r = p_hips.addOrReplaceChild("tasset_r", CubeListBuilder.create()
                .texOffs(221, 23).addBox(-0.5F, -0.2F, -2.5F, 1F, 6F, 5F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-4.9F, -1.4F, 0F, 0F, 0F, 0.1222F));
        PartDefinition p_pendulum = p_hips.addOrReplaceChild("pendulum", CubeListBuilder.create()
                .texOffs(127, 58).addBox(-0.5F, -0.3F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(176, 67).addBox(-2F, 5.6F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, -1.2F, -3.9F));
        PartDefinition p_weight_l = p_hips.addOrReplaceChild("weight_l", CubeListBuilder.create()
                .texOffs(227, 58).addBox(-0.5F, -0.3F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(60, 74).addBox(-1.5F, 5F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(191, 58).addBox(-1F, 5.5F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(103, 79).addBox(-0.5F, 10.1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(5F, -1.8F, -1.2F));
        PartDefinition p_weight_r = p_hips.addOrReplaceChild("weight_r", CubeListBuilder.create()
                .texOffs(232, 58).addBox(-0.5F, -0.3F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(73, 74).addBox(-1.5F, 5F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(200, 58).addBox(-1F, 5.5F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(108, 79).addBox(-0.5F, 10.1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(-5F, -1.8F, -1.2F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(45, 23).addBox(-4.5F, -5.7F, -3F, 9F, 6F, 6F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(154, 74).addBox(-4.5F, -5.6F, -3F, 9F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.05F))
                .texOffs(175, 74).addBox(-4.5F, -3.8F, -3F, 9F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.05F))
                .texOffs(196, 74).addBox(-4.5F, -2F, -3F, 9F, 2F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.05F)),
                PartPose.offset(0F, 3F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(86, 0).addBox(-6F, -11F, -2.9F, 12F, 11F, 7F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(0, 67).addBox(-6F, -11.7F, -4.2F, 12F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(121, 67).addBox(-6F, -2.4F, -4.2F, 12F, 3F, 2F, new CubeDeformation(0F, -0.4F, -0.3F))
                .texOffs(105, 58).addBox(-6F, -8F, -4.2F, 3F, 6F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(116, 58).addBox(3F, -8F, -4.2F, 3F, 6F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(9, 79).addBox(-3F, -8F, -4.2F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(16, 79).addBox(2F, -8F, -4.2F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(23, 79).addBox(-3F, -3F, -4.2F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(30, 79).addBox(2F, -3F, -4.2F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(52, 0).addBox(-6F, -11.6F, -2.8F, 1F, 12F, 7F, new CubeDeformation(0F, -0.2F, -0.2F))
                .texOffs(69, 0).addBox(5F, -11.6F, -2.8F, 1F, 12F, 7F, new CubeDeformation(0F, -0.2F, -0.2F))
                .texOffs(154, 0).addBox(-6F, -11.6F, 3.2F, 12F, 12F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(214, 37).addBox(-3.5F, -13.1F, -3.4F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, -0.4F)),
                PartPose.offset(0F, -5F, 0F));
        PartDefinition p_gear_big = p_chest.addOrReplaceChild("gear_big", CubeListBuilder.create()
                .texOffs(132, 58).addBox(-3F, -3F, -0.5F, 6F, 6F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(-0.6F, -4.6F, -2.75F));
        PartDefinition p_gear_small = p_chest.addOrReplaceChild("gear_small", CubeListBuilder.create()
                .texOffs(104, 74).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(1.9F, -6.6F, -3.05F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(125, 0).addBox(-3.5F, -7.6F, -3.5F, 7F, 8F, 7F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(183, 37).addBox(-3.5F, -7.6F, -4F, 7F, 1F, 8F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(0, 58).addBox(-3.5F, -0.9F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(149, 74).addBox(-0.5F, -7.35F, -4F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.35F, -0.25F))
                .texOffs(86, 79).addBox(-3.5F, -5.5F, -3.975F, 7F, 1F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.275F)),
                PartPose.offset(0F, -12.6F, -0.3F));
        PartDefinition p_crest = p_head.addOrReplaceChild("crest", CubeListBuilder.create()
                .texOffs(225, 0).addBox(-0.5F, -4.6F, -3.8F, 1F, 5F, 8F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offset(0F, -7.6F, 0F));
        PartDefinition p_dial = p_chest.addOrReplaceChild("dial", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-10F, -10F, -0.5F, 20F, 20F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(114, 67).addBox(-1F, 9F, -0.4F, 2F, 5F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(0F, -15.6F, 4.9F, -0.1396F, 0F, 0F));
        PartDefinition p_hand_hour = p_dial.addOrReplaceChild("hand_hour", CubeListBuilder.create()
                .texOffs(234, 48).addBox(-1F, -6.7F, -0.5F, 2F, 8F, 1F, new CubeDeformation(0F, -0.3F, -0.35F)),
                PartPose.offset(0F, 0F, -0.45F));
        PartDefinition p_hand_minute = p_dial.addOrReplaceChild("hand_minute", CubeListBuilder.create()
                .texOffs(76, 23).addBox(-1F, -9.6F, -0.5F, 2F, 11F, 1F, new CubeDeformation(0F, -0.2F, -0.35F))
                .texOffs(37, 79).addBox(-1F, -1F, -0.8F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, -0.7F));
        PartDefinition p_pauldron_l = p_chest.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(116, 23).addBox(-1F, -1.5F, -4F, 6F, 3F, 8F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(0, 37).addBox(0.6F, 0.9F, -4F, 5F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(108, 48).addBox(1.3F, 2.5F, -3.5F, 5F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(0, 48).addBox(-1F, -1.75F, -4F, 6F, 1F, 8F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(5.6F, -10.8F, 0.4F, 0F, 0F, -0.1745F));
        PartDefinition p_pgear_l = p_pauldron_l.addOrReplaceChild("pgear_l", CubeListBuilder.create()
                .texOffs(122, 37).addBox(-0.5F, -2.5F, -2.5F, 1F, 5F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(6.5F, 1.2F, 0F));
        PartDefinition p_arm_l = p_chest.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(174, 23).addBox(-2F, -0.6F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(71, 58).addBox(-2F, 1.1F, -2F, 4F, 4F, 4F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(6.9F, -9.6F, 0.4F, -0.1047F, 0F, -0.1396F));
        PartDefinition p_fore_l = p_arm_l.addOrReplaceChild("fore_l", CubeListBuilder.create()
                .texOffs(29, 58).addBox(-2.5F, -1.3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(88, 37).addBox(-2F, 0.7F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(158, 48).addBox(-2.5F, 5.8F, -2.5F, 5F, 4F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(29, 67).addBox(-2.5F, 5.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, 6.6F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_hour_blade = p_fore_l.addOrReplaceChild("hour_blade", CubeListBuilder.create()
                .texOffs(113, 74).addBox(-1F, 1.75F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.15F))
                .texOffs(209, 58).addBox(-1F, -2.55F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F))
                .texOffs(217, 74).addBox(-3F, -3.05F, -1F, 6F, 1F, 2F, new CubeDeformation(-0.4F, -0.05F, -0.25F))
                .texOffs(44, 79).addBox(-3.9F, -3.55F, -0.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(51, 79).addBox(1.9F, -3.55F, -0.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(58, 79).addBox(-1F, -4.7F, -0.5F, 2F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.05F))
                .texOffs(34, 23).addBox(-2F, -16.1F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(71, 67).addBox(-3F, -20.6F, -0.5F, 6F, 5F, 1F, new CubeDeformation(-0.24F, -0.2F, -0.2F))
                .texOffs(101, 67).addBox(-2.5F, -9.408F, -0.5F, 5F, 5F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.9F, -0.4F, 2.7925F, 0F, 0F));
        PartDefinition p_pauldron_r = p_chest.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(145, 23).addBox(-5F, -1.5F, -4F, 6F, 3F, 8F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(27, 37).addBox(-5.6F, 0.9F, -4F, 5F, 2F, 8F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(133, 48).addBox(-6.3F, 2.5F, -3.5F, 5F, 2F, 7F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(29, 48).addBox(-5F, -1.75F, -4F, 6F, 1F, 8F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-5.6F, -10.8F, 0.4F, 0F, 0F, 0.1745F));
        PartDefinition p_pgear_r = p_pauldron_r.addOrReplaceChild("pgear_r", CubeListBuilder.create()
                .texOffs(135, 37).addBox(-0.5F, -2.5F, -2.5F, 1F, 5F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(-6.5F, 1.2F, 0F));
        PartDefinition p_arm_r = p_chest.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(191, 23).addBox(-2F, -0.6F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(88, 58).addBox(-2F, 1.1F, -2F, 4F, 4F, 4F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(-6.9F, -9.6F, 0.4F, -0.1047F, 0F, 0.1396F));
        PartDefinition p_fore_r = p_arm_r.addOrReplaceChild("fore_r", CubeListBuilder.create()
                .texOffs(50, 58).addBox(-2.5F, -1.3F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(105, 37).addBox(-2F, 0.7F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(179, 48).addBox(-2.5F, 5.8F, -2.5F, 5F, 4F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(50, 67).addBox(-2.5F, 5.5F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, 6.6F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_minute_blade = p_fore_r.addOrReplaceChild("minute_blade", CubeListBuilder.create()
                .texOffs(122, 74).addBox(-1F, 1.75F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.15F, -0.15F, -0.15F))
                .texOffs(218, 58).addBox(-1F, -2.55F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F))
                .texOffs(234, 74).addBox(-3F, -3.05F, -1F, 6F, 1F, 2F, new CubeDeformation(-0.4F, -0.05F, -0.25F))
                .texOffs(65, 79).addBox(-3.9F, -3.55F, -0.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(72, 79).addBox(1.9F, -3.55F, -0.5F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(79, 79).addBox(-1F, -4.7F, -0.5F, 2F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.05F))
                .texOffs(0, 0).addBox(-1.5F, -25.3F, -0.5F, 3F, 21F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(86, 67).addBox(-3F, -29.8F, -0.5F, 6F, 5F, 1F, new CubeDeformation(-0.48F, -0.4F, -0.2F))
                .texOffs(187, 67).addBox(-2F, -10.976F, -0.5F, 4F, 4F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 7.9F, -0.4F, 2.7925F, 0F, 0F));
        PartDefinition p_orbit_0 = root.addOrReplaceChild("orbit_0", CubeListBuilder.create()
                .texOffs(198, 67).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offset(0F, -2F, 0F));
        PartDefinition p_orbit_1 = root.addOrReplaceChild("orbit_1", CubeListBuilder.create()
                .texOffs(131, 74).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.25F)),
                PartPose.offset(0F, -2F, 0F));
        PartDefinition p_orbit_2 = root.addOrReplaceChild("orbit_2", CubeListBuilder.create()
                .texOffs(140, 74).addBox(-1.5F, -1.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.25F)),
                PartPose.offset(0F, -2F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
