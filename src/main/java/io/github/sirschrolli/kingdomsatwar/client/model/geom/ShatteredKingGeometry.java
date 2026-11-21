package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Zerbrochenen Königs (Boss der Leere): Leib mit offener Brust und zersprungenem Herz, Kopf mit Bart und schwebender Kristallkrone, Schultern, Gewand in Bahnen, zwei frei schwebende Hände und kreisende Trümmer.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ShatteredKingGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private ShatteredKingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(41, 20).addBox(-8F, -12F, 1.25F, 16F, 12F, 4F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(87, 0).addBox(-8.4F, -12F, -4.5F, 6F, 12F, 6F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(112, 0).addBox(2.4F, -12F, -4.5F, 6F, 12F, 6F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(144, 52).addBox(-3F, -12.4F, -4.5F, 6F, 4F, 6F, new CubeDeformation(-0.2F, -0.4F, 0F))
                .texOffs(0, 64).addBox(-3F, -2.9F, -4.35F, 6F, 3F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.15F))
                .texOffs(185, 64).addBox(-3F, -8.8F, 0.8F, 6F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(172, 20).addBox(-9F, -13F, -5.3F, 18F, 2F, 11F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(43, 38).addBox(-6.5F, 0F, -4F, 13F, 4F, 8F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(58, 74).addBox(-3.6F, -8.8F, -5.1F, 1F, 6F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(63, 74).addBox(2.6F, -8.8F, -5.1F, 1F, 6F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.3F)),
                PartPose.offset(0F, 2F, 0F));
        PartDefinition p_heart = p_body.addOrReplaceChild("heart", CubeListBuilder.create(),
                PartPose.offset(0F, -5.8F, -1.8F));
        PartDefinition p_heart_l = p_heart.addOrReplaceChild("heart_l", CubeListBuilder.create()
                .texOffs(169, 52).addBox(-2.65F, -3F, -2F, 3F, 6F, 4F, new CubeDeformation(-0.45F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(-0.4F, 0F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_heart_r = p_heart.addOrReplaceChild("heart_r", CubeListBuilder.create()
                .texOffs(25, 64).addBox(-0.35F, -2.5F, -2F, 3F, 5F, 4F, new CubeDeformation(-0.45F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0.4F, 0F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -9.75F, -4.5F, 9F, 10F, 9F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(54, 82).addBox(-5F, -7.5F, -5.4F, 10F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.4F)),
                PartPose.offset(0F, -12.5F, -0.5F));
        PartDefinition p_beard = p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(130, 64).addBox(-4F, -0.4F, -1F, 8F, 5F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.1F)),
                PartPose.offset(0F, -1.2F, -4.4F));
        PartDefinition p_beard_tip = p_beard.addOrReplaceChild("beard_tip", CubeListBuilder.create()
                .texOffs(39, 82).addBox(-2.5F, -0.3F, -1F, 5F, 3F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.3F)),
                PartPose.offset(0F, 4.2F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offset(0F, -12.5F, 0F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(117, 52).addBox(-1F, -7.65F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, -5.6F, 0.2443F, 0F, 0F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(40, 64).addBox(-1F, -5.65F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(4.3783F, 0F, -3.4915F, 0.1523F, 0F, 0.191F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(76, 64).addBox(-1F, -4.65F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(5.4596F, 0F, 1.2461F, -0.0544F, 0F, 0.2382F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(85, 64).addBox(-1F, -4.65F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(2.4297F, 0F, 5.0454F, -0.2201F, 0F, 0.106F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(94, 64).addBox(-1F, -4.65F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(-2.4297F, 0F, 5.0454F, -0.2201F, 0F, -0.106F));
        PartDefinition p_crown_5 = p_crown.addOrReplaceChild("crown_5", CubeListBuilder.create()
                .texOffs(103, 64).addBox(-1F, -4.65F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(-5.4596F, 0F, 1.2461F, -0.0544F, 0F, -0.2382F));
        PartDefinition p_crown_6 = p_crown.addOrReplaceChild("crown_6", CubeListBuilder.create()
                .texOffs(49, 64).addBox(-1F, -5.65F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.1F)),
                PartPose.offsetAndRotation(-4.3783F, 0F, -3.4915F, 0.1523F, 0F, -0.191F));
        PartDefinition p_left_shoulder = p_body.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
                .texOffs(187, 0).addBox(-4.5F, -2.5F, -5.5F, 9F, 6F, 11F)
                .texOffs(82, 20).addBox(-5F, 2.7F, -6F, 10F, 2F, 12F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(191, 38).addBox(-2F, 3.75F, -2.5F, 4F, 7F, 5F, new CubeDeformation(-0.1F, -0.25F, -0.3F))
                .texOffs(200, 64).addBox(-1.5F, 10F, -2F, 3F, 3F, 4F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(10F, -10.5F, 0F));
        PartDefinition p_left_shoulder_shard_0 = p_left_shoulder.addOrReplaceChild("left_shoulder_shard_0", CubeListBuilder.create()
                .texOffs(126, 52).addBox(-1F, -7.4F, -1F, 2F, 9F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0.5F, -2.5F, -2F, -0.2793F, 0F, 0.4189F));
        PartDefinition p_left_shoulder_shard_1 = p_left_shoulder.addOrReplaceChild("left_shoulder_shard_1", CubeListBuilder.create()
                .texOffs(58, 64).addBox(-1F, -5.4F, -1F, 2F, 7F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(2.5F, -2.5F, 2F, 0.3142F, 0F, 0.5934F));
        PartDefinition p_left_shoulder_shard_2 = p_left_shoulder.addOrReplaceChild("left_shoulder_shard_2", CubeListBuilder.create()
                .texOffs(112, 64).addBox(-1F, -4.4F, -1F, 2F, 6F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-1F, -2.5F, 2.5F, 0.4538F, 0F, 0.1745F));
        PartDefinition p_right_shoulder = p_body.addOrReplaceChild("right_shoulder", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-4.5F, -2.5F, -5.5F, 9F, 6F, 11F)
                .texOffs(127, 20).addBox(-5F, 2.7F, -6F, 10F, 2F, 12F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(210, 38).addBox(-2F, 3.75F, -2.5F, 4F, 7F, 5F, new CubeDeformation(-0.1F, -0.25F, -0.3F))
                .texOffs(215, 64).addBox(-1.5F, 10F, -2F, 3F, 3F, 4F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(-10F, -10.5F, 0F));
        PartDefinition p_right_shoulder_shard_0 = p_right_shoulder.addOrReplaceChild("right_shoulder_shard_0", CubeListBuilder.create()
                .texOffs(135, 52).addBox(-1F, -7.4F, -1F, 2F, 9F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-0.5F, -2.5F, -2F, -0.2793F, 0F, -0.4189F));
        PartDefinition p_right_shoulder_shard_1 = p_right_shoulder.addOrReplaceChild("right_shoulder_shard_1", CubeListBuilder.create()
                .texOffs(67, 64).addBox(-1F, -5.4F, -1F, 2F, 7F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-2.5F, -2.5F, 2F, 0.3142F, 0F, -0.5934F));
        PartDefinition p_right_shoulder_shard_2 = p_right_shoulder.addOrReplaceChild("right_shoulder_shard_2", CubeListBuilder.create()
                .texOffs(121, 64).addBox(-1F, -4.4F, -1F, 2F, 6F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(1F, -2.5F, 2.5F, 0.4538F, 0F, -0.1745F));
        PartDefinition p_robe = p_body.addOrReplaceChild("robe", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-6.5F, 0F, -4F, 13F, 5F, 8F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_robe_front = p_robe.addOrReplaceChild("robe_front", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-8.5F, 0F, -1F, 17F, 9F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -4.3F, -0.2443F, 0F, 0F));
        PartDefinition p_robe_front_low = p_robe_front.addOrReplaceChild("robe_front_low", CubeListBuilder.create()
                .texOffs(39, 52).addBox(-8.5F, 0F, -1F, 17F, 9F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_robe_back = p_robe.addOrReplaceChild("robe_back", CubeListBuilder.create()
                .texOffs(78, 52).addBox(-8.5F, 0F, -1F, 17F, 9F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 4.3F, 0.2094F, 0F, 0F));
        PartDefinition p_robe_back_low = p_robe_back.addOrReplaceChild("robe_back_low", CubeListBuilder.create()
                .texOffs(86, 38).addBox(-8.5F, 0F, -1F, 17F, 10F, 2F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_robe_left = p_robe.addOrReplaceChild("robe_left", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-1F, 0F, -5F, 2F, 9F, 10F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(6.9F, 0F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_robe_left_low = p_robe_left.addOrReplaceChild("robe_left_low", CubeListBuilder.create()
                .texOffs(137, 0).addBox(-1F, 0F, -5F, 2F, 8F, 10F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_robe_right = p_robe.addOrReplaceChild("robe_right", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-1F, 0F, -5F, 2F, 9F, 10F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-6.9F, 0F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_robe_right_low = p_robe_right.addOrReplaceChild("robe_right_low", CubeListBuilder.create()
                .texOffs(162, 0).addBox(-1F, 0F, -5F, 2F, 8F, 10F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 9F, 0F));
        PartDefinition p_left_hand = root.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(125, 38).addBox(-4F, -2.2F, -4.1F, 8F, 4F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(184, 52).addBox(-4.5F, -2.8F, 3F, 9F, 5F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(151, 64).addBox(-2.5F, -1.9F, 6F, 5F, 4F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(19F, 3F, -5F, 0.1745F, -0.2094F, 0.1396F));
        PartDefinition p_left_finger_0 = p_left_hand.addOrReplaceChild("left_finger_0", CubeListBuilder.create()
                .texOffs(83, 74).addBox(-1F, -1F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.7F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_left_finger_0_tip = p_left_finger_0.addOrReplaceChild("left_finger_0_tip", CubeListBuilder.create()
                .texOffs(96, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(79, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.5585F, 0F, 0F));
        PartDefinition p_left_finger_1 = p_left_hand.addOrReplaceChild("left_finger_1", CubeListBuilder.create()
                .texOffs(230, 64).addBox(-1F, -1F, -4.8F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-0.9F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_left_finger_1_tip = p_left_finger_1.addOrReplaceChild("left_finger_1_tip", CubeListBuilder.create()
                .texOffs(109, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(88, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4.6F, 0.5585F, 0F, 0F));
        PartDefinition p_left_finger_2 = p_left_hand.addOrReplaceChild("left_finger_2", CubeListBuilder.create()
                .texOffs(0, 74).addBox(-1F, -1F, -4.8F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0.9F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_left_finger_2_tip = p_left_finger_2.addOrReplaceChild("left_finger_2_tip", CubeListBuilder.create()
                .texOffs(122, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(97, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4.6F, 0.5585F, 0F, 0F));
        PartDefinition p_left_finger_3 = p_left_hand.addOrReplaceChild("left_finger_3", CubeListBuilder.create()
                .texOffs(135, 74).addBox(-1F, -1F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.7F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_left_finger_3_tip = p_left_finger_3.addOrReplaceChild("left_finger_3_tip", CubeListBuilder.create()
                .texOffs(148, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(106, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.5585F, 0F, 0F));
        PartDefinition p_left_thumb = p_left_hand.addOrReplaceChild("left_thumb", CubeListBuilder.create()
                .texOffs(161, 74).addBox(-1F, -1F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(115, 82).addBox(-1F, -1F, -5.45F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(-3.6F, 0.8F, -1.5F, 0.1745F, 0.6981F, 0F));
        PartDefinition p_right_hand = root.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(158, 38).addBox(-4F, -2.2F, -4.1F, 8F, 4F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(211, 52).addBox(-4.5F, -2.8F, 3F, 9F, 5F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(168, 64).addBox(-2.5F, -1.9F, 6F, 5F, 4F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(-19F, 3F, -5F, 0.1745F, 0.2094F, -0.1396F));
        PartDefinition p_right_finger_0 = p_right_hand.addOrReplaceChild("right_finger_0", CubeListBuilder.create()
                .texOffs(174, 74).addBox(-1F, -1F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.7F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_right_finger_0_tip = p_right_finger_0.addOrReplaceChild("right_finger_0_tip", CubeListBuilder.create()
                .texOffs(187, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(124, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.5585F, 0F, 0F));
        PartDefinition p_right_finger_1 = p_right_hand.addOrReplaceChild("right_finger_1", CubeListBuilder.create()
                .texOffs(15, 74).addBox(-1F, -1F, -4.8F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(-0.9F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_right_finger_1_tip = p_right_finger_1.addOrReplaceChild("right_finger_1_tip", CubeListBuilder.create()
                .texOffs(200, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(133, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4.6F, 0.5585F, 0F, 0F));
        PartDefinition p_right_finger_2 = p_right_hand.addOrReplaceChild("right_finger_2", CubeListBuilder.create()
                .texOffs(30, 74).addBox(-1F, -1F, -4.8F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0.9F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_right_finger_2_tip = p_right_finger_2.addOrReplaceChild("right_finger_2_tip", CubeListBuilder.create()
                .texOffs(213, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(142, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4.6F, 0.5585F, 0F, 0F));
        PartDefinition p_right_finger_3 = p_right_hand.addOrReplaceChild("right_finger_3", CubeListBuilder.create()
                .texOffs(226, 74).addBox(-1F, -1F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.7F, 0.2F, -4F, 0.3142F, 0F, 0F));
        PartDefinition p_right_finger_3_tip = p_right_finger_3.addOrReplaceChild("right_finger_3_tip", CubeListBuilder.create()
                .texOffs(239, 74).addBox(-1F, -1F, -3.6F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.4F))
                .texOffs(151, 82).addBox(-1F, -1F, -4.65F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.5585F, 0F, 0F));
        PartDefinition p_right_thumb = p_right_hand.addOrReplaceChild("right_thumb", CubeListBuilder.create()
                .texOffs(0, 82).addBox(-1F, -1F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(160, 82).addBox(-1F, -1F, -5.45F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(3.6F, 0.8F, -1.5F, 0.1745F, -0.6981F, 0F));
        PartDefinition p_orbit = root.addOrReplaceChild("orbit", CubeListBuilder.create(),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_fragment_0 = p_orbit.addOrReplaceChild("fragment_0", CubeListBuilder.create()
                .texOffs(68, 74).addBox(-2F, -1.5F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offsetAndRotation(15.0351F, -6F, 5.4723F, 0F, 0F, 0.1745F));
        PartDefinition p_fragment_1 = p_orbit.addOrReplaceChild("fragment_1", CubeListBuilder.create()
                .texOffs(45, 74).addBox(-1.5F, -2F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-5.9854F, 2F, 16.4446F, 0.2618F, 0.5236F, 0.1745F));
        PartDefinition p_fragment_2 = p_orbit.addOrReplaceChild("fragment_2", CubeListBuilder.create()
                .texOffs(13, 82).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(-14.0954F, -9F, -5.1303F, 0.5236F, 1.0472F, 0.1745F));
        PartDefinition p_fragment_3 = p_orbit.addOrReplaceChild("fragment_3", CubeListBuilder.create()
                .texOffs(26, 82).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.4F, -0.3F)),
                PartPose.offsetAndRotation(6.1564F, 4F, -16.9145F, 0.7854F, 1.5708F, 0.1745F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
