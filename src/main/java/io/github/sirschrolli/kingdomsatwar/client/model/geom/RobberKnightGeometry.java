package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Kuno von Rabenfels, dem Raubritter, und seinem Ross (1.8; im Spiel 1,1-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class RobberKnightGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private RobberKnightGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_horse = root.addOrReplaceChild("horse", CubeListBuilder.create()
                .texOffs(110, 0).addBox(-6F, -6F, -13F, 12F, 12F, 26F)
                .texOffs(0, 87).addBox(-5F, -4.8F, -14.8F, 10F, 10F, 2F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(0, 43).addBox(-6.5F, -6.75F, -12.2F, 13F, 1F, 25F, new CubeDeformation(-0.05F, -0.05F, -0.2F))
                .texOffs(51, 87).addBox(-4.5F, -8.1F, -4.2F, 9F, 2F, 9F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(147, 111).addBox(-4F, -12F, 3.8F, 8F, 5F, 2F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(233, 120).addBox(-3F, -10.3F, -5.7F, 6F, 3F, 2F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_trapper_l = p_horse.addOrReplaceChild("trapper_l", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.15F, -0.7F, -13F, 1F, 16F, 26F, new CubeDeformation(-0.15F, -0.2F, -0.1F)),
                PartPose.offset(6.3F, -6F, 0F));
        PartDefinition p_trapper_r = p_horse.addOrReplaceChild("trapper_r", CubeListBuilder.create()
                .texOffs(55, 0).addBox(-0.85F, -0.7F, -13F, 1F, 16F, 26F, new CubeDeformation(-0.15F, -0.2F, -0.1F)),
                PartPose.offset(-6.3F, -6F, 0F));
        PartDefinition p_trapper_front = p_horse.addOrReplaceChild("trapper_front", CubeListBuilder.create()
                .texOffs(97, 70).addBox(-6.5F, -0.4F, -0.85F, 13F, 13F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offset(0F, -4.6F, -14.75F));
        PartDefinition p_trapper_back = p_horse.addOrReplaceChild("trapper_back", CubeListBuilder.create()
                .texOffs(40, 70).addBox(-6.5F, -0.4F, -0.15F, 13F, 14F, 1F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offset(0F, -5.6F, 13.05F));
        PartDefinition p_neck = p_horse.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(77, 43).addBox(-3F, -16.75F, -4F, 6F, 18F, 8F, new CubeDeformation(0F, -0.25F, -0.1F))
                .texOffs(129, 43).addBox(-1.5F, -18.8F, 2.45F, 3F, 20F, 4F, new CubeDeformation(-0.1F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(0F, -3F, -12F, 0.5236F, 0F, 0F));
        PartDefinition p_mane_drape = p_neck.addOrReplaceChild("mane_drape", CubeListBuilder.create()
                .texOffs(144, 43).addBox(-0.4F, 0F, -1.5F, 2F, 16F, 5F, new CubeDeformation(-0.4F, 0F, -0.1F)),
                PartPose.offset(1.4F, -16.2F, 3.8F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(9, 70).addBox(-3.5F, -4.6F, -3.05F, 7F, 7F, 8F, new CubeDeformation(0F, -0.3F, -0.05F))
                .texOffs(126, 70).addBox(-3F, -2.8F, -9.7F, 6F, 6F, 7F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(177, 87).addBox(-4F, -5.1F, -3.55F, 8F, 1F, 9F, new CubeDeformation(-0.35F, -0.1F, -0.4F))
                .texOffs(224, 100).addBox(-3F, -3.55F, -9.8F, 6F, 1F, 7F, new CubeDeformation(-0.25F, -0.1F, 0F))
                .texOffs(56, 128).addBox(-1.5F, -3.55F, -10.125F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.05F, -0.275F))
                .texOffs(110, 120).addBox(-1F, -9.45F, 0.15F, 2F, 5F, 2F, new CubeDeformation(-0.25F, -0.45F, -0.25F))
                .texOffs(0, 128).addBox(-2F, -5.75F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.2F, -0.15F, -0.3F)),
                PartPose.offset(0F, -15.2F, -0.8F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(206, 120).addBox(-1F, -3.6F, -0.95F, 2F, 4F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(1.95F, -4.6F, 2.8F, -0.1745F, 0F, 0.2094F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(215, 120).addBox(-1F, -3.6F, -0.95F, 2F, 4F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.35F)),
                PartPose.offsetAndRotation(-1.95F, -4.6F, 2.8F, -0.1745F, 0F, -0.2094F));
        PartDefinition p_leg_fl = p_horse.addOrReplaceChild("leg_fl", CubeListBuilder.create()
                .texOffs(153, 70).addBox(-2F, -1F, -2.5F, 4F, 8F, 5F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(3.7F, 5F, -9.8F));
        PartDefinition p_shin_fl = p_leg_fl.addOrReplaceChild("shin_fl", CubeListBuilder.create()
                .texOffs(93, 100).addBox(-1.5F, -0.3F, -2F, 3F, 6F, 4F, new CubeDeformation(0F, -0.3F, -0.35F))
                .texOffs(27, 111).addBox(-2.5F, 3.4F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.45F, -0.2F, -0.25F))
                .texOffs(210, 111).addBox(-2F, 5.2F, -2.5F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_leg_fr = p_horse.addOrReplaceChild("leg_fr", CubeListBuilder.create()
                .texOffs(172, 70).addBox(-2F, -1F, -2.5F, 4F, 8F, 5F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(-3.7F, 5F, -9.8F));
        PartDefinition p_shin_fr = p_leg_fr.addOrReplaceChild("shin_fr", CubeListBuilder.create()
                .texOffs(108, 100).addBox(-1.5F, -0.3F, -2F, 3F, 6F, 4F, new CubeDeformation(0F, -0.3F, -0.35F))
                .texOffs(48, 111).addBox(-2.5F, 3.4F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.45F, -0.2F, -0.25F))
                .texOffs(229, 111).addBox(-2F, 5.2F, -2.5F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_leg_hl = p_horse.addOrReplaceChild("leg_hl", CubeListBuilder.create()
                .texOffs(191, 70).addBox(-2F, -1F, -2.5F, 4F, 8F, 5F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(3.7F, 5F, 9.6F));
        PartDefinition p_shin_hl = p_leg_hl.addOrReplaceChild("shin_hl", CubeListBuilder.create()
                .texOffs(123, 100).addBox(-1.5F, -0.3F, -2F, 3F, 6F, 4F, new CubeDeformation(0F, -0.3F, -0.35F))
                .texOffs(69, 111).addBox(-2.5F, 3.4F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.45F, -0.2F, -0.25F))
                .texOffs(0, 120).addBox(-2F, 5.2F, -2.5F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_leg_hr = p_horse.addOrReplaceChild("leg_hr", CubeListBuilder.create()
                .texOffs(210, 70).addBox(-2F, -1F, -2.5F, 4F, 8F, 5F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(-3.7F, 5F, 9.6F));
        PartDefinition p_shin_hr = p_leg_hr.addOrReplaceChild("shin_hr", CubeListBuilder.create()
                .texOffs(138, 100).addBox(-1.5F, -0.3F, -2F, 3F, 6F, 4F, new CubeDeformation(0F, -0.3F, -0.35F))
                .texOffs(90, 111).addBox(-2.5F, 3.4F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.45F, -0.2F, -0.25F))
                .texOffs(19, 120).addBox(-2F, 5.2F, -2.5F, 4F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tail = p_horse.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(25, 87).addBox(-2F, -0.4F, -1F, 4F, 8F, 4F, new CubeDeformation(-0.2F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -4.4F, 13.1F, 0.4887F, 0F, 0F));
        PartDefinition p_tail_low = p_tail.addOrReplaceChild("tail_low", CubeListBuilder.create()
                .texOffs(69, 70).addBox(-2.5F, 0F, -1F, 5F, 11F, 4F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 7.2F, 0.2F));
        PartDefinition p_knight = root.addOrReplaceChild("knight", CubeListBuilder.create(),
                PartPose.offset(0F, -3F, 0.6F));
        PartDefinition p_torso = p_knight.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(159, 43).addBox(-4.5F, -12F, -3F, 9F, 12F, 6F, new CubeDeformation(-0.1F, 0F, -0.4F))
                .texOffs(191, 100).addBox(-5F, -2.1F, -3F, 10F, 2F, 6F, new CubeDeformation(-0.25F, -0.2F, -0.05F))
                .texOffs(0, 111).addBox(-3.5F, -13.55F, -3F, 7F, 2F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tabard = p_torso.addOrReplaceChild("tabard", CubeListBuilder.create()
                .texOffs(153, 100).addBox(-4F, -0.3F, -0.75F, 8F, 8F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, -0.4F, -2.75F));
        PartDefinition p_tabard_back = p_torso.addOrReplaceChild("tabard_back", CubeListBuilder.create()
                .texOffs(172, 100).addBox(-4F, -0.3F, -0.25F, 8F, 8F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.25F)),
                PartPose.offset(0F, -0.4F, 2.75F));
        PartDefinition p_cape = p_torso.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(190, 43).addBox(-5F, -0.25F, -0.2F, 10F, 17F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.2F)),
                PartPose.offset(0F, -11.8F, 2.95F));
        PartDefinition p_horn = p_torso.addOrReplaceChild("horn", CubeListBuilder.create()
                .texOffs(169, 120).addBox(-0.35F, -0.35F, -2.9F, 2F, 2F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.3F))
                .texOffs(65, 128).addBox(-0.35F, -0.9F, -4.3F, 2F, 2F, 2F, new CubeDeformation(-0.1F, 0F, -0.2F)),
                PartPose.offset(4.9F, -1.4F, -0.4F));
        PartDefinition p_helm = p_torso.addOrReplaceChild("helm", CubeListBuilder.create()
                .texOffs(213, 43).addBox(-4F, -8.8F, -4F, 8F, 9F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, -12.6F, 0F));
        PartDefinition p_crest = p_helm.addOrReplaceChild("crest", CubeListBuilder.create()
                .texOffs(132, 111).addBox(-1F, -2.9F, -1.9F, 2F, 3F, 5F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(74, 128).addBox(-1F, -4.2F, -2.9F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(195, 120).addBox(-1F, -6.05F, -4.3F, 2F, 3F, 3F, new CubeDeformation(-0.05F, -0.45F, -0.4F))
                .texOffs(137, 128).addBox(-0.5F, -5.1F, -5.8F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.1F, 0F))
                .texOffs(17, 128).addBox(-1F, -2.3F, 2.2F, 2F, 1F, 4F, new CubeDeformation(-0.15F, -0.1F, -0.4F)),
                PartPose.offset(0F, -8.6F, 0.4F));
        PartDefinition p_wing_l = p_crest.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(119, 120).addBox(0F, -0.5F, -2F, 7F, 1F, 5F, new CubeDeformation(0F, -0.05F, -0.4F)),
                PartPose.offsetAndRotation(0.9F, -2.4F, 0.2F, 0F, -0.5934F, -0.9076F));
        PartDefinition p_wing_r = p_crest.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(144, 120).addBox(-7F, -0.5F, -2F, 7F, 1F, 5F, new CubeDeformation(0F, -0.05F, -0.4F)),
                PartPose.offsetAndRotation(-0.9F, -2.4F, 0.2F, 0F, 0.5934F, 0.9076F));
        PartDefinition p_arm_r = p_torso.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(109, 87).addBox(-2F, -1.5F, -2F, 4F, 7F, 4F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(212, 87).addBox(-3F, -2.65F, -3F, 6F, 4F, 6F, new CubeDeformation(-0.25F, -0.15F, -0.35F)),
                PartPose.offset(-5.6F, -10.8F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(25, 100).addBox(-2F, -0.3F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(168, 111).addBox(-2.5F, -1.05F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.35F, -0.15F, -0.25F)),
                PartPose.offset(0F, 5.4F, 0F));
        PartDefinition p_hand_r = p_forearm_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(76, 120).addBox(-2F, -0.05F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.35F, -0.05F, -0.35F)),
                PartPose.offset(0F, 5.4F, 0F));
        PartDefinition p_lance = p_hand_r.addOrReplaceChild("lance", CubeListBuilder.create()
                .texOffs(42, 87).addBox(-1F, -1.6F, -1F, 2F, 10F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(111, 111).addBox(-2.5F, -4F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(246, 43).addBox(-1F, -17.4F, -1F, 2F, 14F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 70).addBox(-1F, -31F, -1F, 2F, 14F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(224, 120).addBox(-1F, -35F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 1.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_pennant = p_lance.addOrReplaceChild("pennant", CubeListBuilder.create()
                .texOffs(229, 70).addBox(-0.5F, 0F, -6.6F, 1F, 7F, 6F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, -28.5F, 0F));
        PartDefinition p_mace = p_hand_r.addOrReplaceChild("mace", CubeListBuilder.create()
                .texOffs(88, 70).addBox(-1F, -9F, -1F, 2F, 13F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(88, 87).addBox(-2.5F, -14.7F, -2.5F, 5F, 6F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(83, 128).addBox(-1F, -12.9F, -3.85F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.35F))
                .texOffs(92, 128).addBox(-1F, -12.9F, 1.85F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.35F))
                .texOffs(101, 128).addBox(-3.85F, -12.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.3F, -0.45F))
                .texOffs(110, 128).addBox(1.85F, -12.9F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.3F, -0.45F))
                .texOffs(119, 128).addBox(-1F, -16.1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.3F, -0.45F)),
                PartPose.offsetAndRotation(0F, 1.6F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_arm_l = p_torso.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(126, 87).addBox(-2F, -1.5F, -2F, 4F, 7F, 4F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 100).addBox(-3F, -2.65F, -3F, 6F, 4F, 6F, new CubeDeformation(-0.25F, -0.15F, -0.35F)),
                PartPose.offset(5.6F, -10.8F, 0F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(42, 100).addBox(-2F, -0.3F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(189, 111).addBox(-2.5F, -1.05F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.35F, -0.15F, -0.25F)),
                PartPose.offset(0F, 5.4F, 0F));
        PartDefinition p_hand_l = p_forearm_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(93, 120).addBox(-2F, -0.05F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.35F, -0.05F, -0.35F)),
                PartPose.offset(0F, 5.4F, 0F));
        PartDefinition p_horn_hand = p_hand_l.addOrReplaceChild("horn_hand", CubeListBuilder.create()
                .texOffs(182, 120).addBox(-1F, -0.95F, -2.9F, 2F, 2F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.3F))
                .texOffs(128, 128).addBox(-1F, -1.5F, -4.3F, 2F, 2F, 2F, new CubeDeformation(-0.1F, 0F, -0.2F)),
                PartPose.offset(0F, 2F, 0F));
        PartDefinition p_shield = p_forearm_l.addOrReplaceChild("shield", CubeListBuilder.create()
                .texOffs(106, 43).addBox(0F, -6F, -5F, 1F, 14F, 10F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offset(2F, 2.6F, 0F));
        PartDefinition p_thigh_r = p_knight.addOrReplaceChild("thigh_r", CubeListBuilder.create()
                .texOffs(143, 87).addBox(-2F, -0.3F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.05F, -0.3F, 0F)),
                PartPose.offset(-2.3F, 0F, 0F));
        PartDefinition p_shin_r = p_thigh_r.addOrReplaceChild("shin_r", CubeListBuilder.create()
                .texOffs(59, 100).addBox(-2F, -0.35F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.15F))
                .texOffs(30, 128).addBox(-2F, -0.95F, -2.5F, 4F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, -0.2F))
                .texOffs(38, 120).addBox(-2F, 4.25F, -3.05F, 4F, 2F, 5F, new CubeDeformation(-0.2F, -0.35F, -0.05F))
                .texOffs(144, 128).addBox(-0.5F, 4.55F, 1.6F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.25F)),
                PartPose.offset(0F, 6.4F, 0F));
        PartDefinition p_thigh_l = p_knight.addOrReplaceChild("thigh_l", CubeListBuilder.create()
                .texOffs(160, 87).addBox(-2F, -0.3F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.05F, -0.3F, 0F)),
                PartPose.offset(2.3F, 0F, 0F));
        PartDefinition p_shin_l = p_thigh_l.addOrReplaceChild("shin_l", CubeListBuilder.create()
                .texOffs(76, 100).addBox(-2F, -0.35F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.25F, -0.35F, -0.15F))
                .texOffs(43, 128).addBox(-2F, -0.95F, -2.5F, 4F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, -0.2F))
                .texOffs(57, 120).addBox(-2F, 4.25F, -3.05F, 4F, 2F, 5F, new CubeDeformation(-0.2F, -0.35F, -0.05F))
                .texOffs(151, 128).addBox(-0.5F, 4.55F, 1.6F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.25F)),
                PartPose.offset(0F, 6.4F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
