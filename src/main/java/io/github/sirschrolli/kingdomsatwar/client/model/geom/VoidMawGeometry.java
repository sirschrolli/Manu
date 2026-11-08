package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Leerenschlunds (Glocke, Saum, Maul mit vier Lippen, Fangfäden).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VoidMawGeometry {
    public static final int TEXTURE_WIDTH = 160;
    public static final int TEXTURE_HEIGHT = 128;

    private VoidMawGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bell = root.addOrReplaceChild("bell", CubeListBuilder.create()
                .texOffs(106, 41).addBox(-4.5F, -5.8F, -4.5F, 9F, 7F, 9F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(0, 41).addBox(-8F, -3.75F, -6F, 16F, 5F, 12F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(61, 0).addBox(-6F, -4F, -8F, 12F, 5F, 16F)
                .texOffs(0, 0).addBox(-7.5F, -4.75F, -7.5F, 15F, 6F, 15F, new CubeDeformation(-0.3F, -0.25F, -0.3F))
                .texOffs(42, 59).addBox(-7F, -6.5F, -5F, 14F, 3F, 10F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(0, 22).addBox(-5F, -7.25F, -7F, 10F, 4F, 14F, new CubeDeformation(0F, -0.25F, -0.4F))
                .texOffs(57, 41).addBox(-6F, -7.45F, -6F, 12F, 4F, 12F, new CubeDeformation(0F, -0.05F, 0F))
                .texOffs(37, 74).addBox(-5F, -9.15F, -4F, 10F, 3F, 8F, new CubeDeformation(-0.2F, -0.35F, -0.4F))
                .texOffs(91, 59).addBox(-4F, -9.35F, -5F, 8F, 3F, 10F, new CubeDeformation(-0.4F, -0.15F, -0.2F))
                .texOffs(0, 74).addBox(-4.5F, -10.05F, -4.5F, 9F, 4F, 9F, new CubeDeformation(-0.2F, -0.45F, -0.2F))
                .texOffs(90, 97).addBox(-3F, -10.6F, -2F, 6F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(136, 74).addBox(-2F, -10.8F, -3F, 4F, 2F, 6F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(113, 104).addBox(-4.2F, -2.4F, -8.7F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(120, 104).addBox(2.2F, -2.4F, -8.7F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(15, 104).addBox(-8.7F, -2F, -4.8F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(22, 104).addBox(7.7F, -2F, -4.8F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(29, 104).addBox(-8.7F, -1.6F, 2.6F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(36, 104).addBox(7.7F, -1.6F, 2.6F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_shard_0 = p_bell.addOrReplaceChild("shard_0", CubeListBuilder.create()
                .texOffs(74, 74).addBox(-1F, -7.25F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(2.5F, -8.5F, 1.5F, -0.3142F, 0F, 0.384F));
        PartDefinition p_shard_1 = p_bell.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(127, 74).addBox(-1F, -5.25F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -8.5F, 2.5F, 0.4189F, 0.6458F, -0.2793F));
        PartDefinition p_shard_2 = p_bell.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(0, 88).addBox(-1F, -4.25F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(0.5F, -8.5F, -3F, -0.4538F, 1.2915F, -0.1396F));
        PartDefinition p_skirt_n = p_bell.addOrReplaceChild("skirt_n", CubeListBuilder.create()
                .texOffs(0, 97).addBox(-8.5F, -0.1F, -5F, 17F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0.6F, -7.6F));
        PartDefinition p_skirt_s = p_bell.addOrReplaceChild("skirt_s", CubeListBuilder.create()
                .texOffs(45, 97).addBox(-8.5F, -0.1F, 0F, 17F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0.6F, 7.6F));
        PartDefinition p_skirt_w = p_bell.addOrReplaceChild("skirt_w", CubeListBuilder.create()
                .texOffs(49, 22).addBox(-5F, -0.1F, -8.5F, 5F, 1F, 17F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(-7.6F, 0.6F, 0F));
        PartDefinition p_skirt_e = p_bell.addOrReplaceChild("skirt_e", CubeListBuilder.create()
                .texOffs(94, 22).addBox(0F, -0.1F, -8.5F, 5F, 1F, 17F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(7.6F, 0.6F, 0F));
        PartDefinition p_maw = p_bell.addOrReplaceChild("maw", CubeListBuilder.create(),
                PartPose.offset(0F, 1F, 0F));
        PartDefinition p_lip_n = p_maw.addOrReplaceChild("lip_n", CubeListBuilder.create()
                .texOffs(111, 97).addBox(-4.5F, 0F, -0.5F, 9F, 5F, 1F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(43, 104).addBox(-3.5F, 2.2F, 0.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(48, 104).addBox(-1.5F, 2.2F, 0.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(53, 104).addBox(0.5F, 2.2F, 0.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(58, 104).addBox(2.5F, 2.2F, 0.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, -4.4F));
        PartDefinition p_lip_s = p_maw.addOrReplaceChild("lip_s", CubeListBuilder.create()
                .texOffs(132, 97).addBox(-4.5F, 0F, -0.5F, 9F, 5F, 1F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(63, 104).addBox(-3.5F, 2.2F, -1.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(68, 104).addBox(-1.5F, 2.2F, -1.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(73, 104).addBox(0.5F, 2.2F, -1.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(78, 104).addBox(2.5F, 2.2F, -1.4F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 4.4F));
        PartDefinition p_lip_w = p_maw.addOrReplaceChild("lip_w", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-0.5F, 0F, -4.5F, 1F, 5F, 9F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(83, 104).addBox(0.4F, 2.2F, -2.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(88, 104).addBox(0.4F, 2.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(93, 104).addBox(0.4F, 2.2F, 1.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(-4.4F, 0F, 0F));
        PartDefinition p_lip_e = p_maw.addOrReplaceChild("lip_e", CubeListBuilder.create()
                .texOffs(21, 59).addBox(-0.5F, 0F, -4.5F, 1F, 5F, 9F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(98, 104).addBox(-1.4F, 2.2F, -2.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(103, 104).addBox(-1.4F, 2.2F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(108, 104).addBox(-1.4F, 2.2F, 1.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(4.4F, 0F, 0F));
        PartDefinition p_arm_0 = p_maw.addOrReplaceChild("arm_0", CubeListBuilder.create()
                .texOffs(83, 74).addBox(-2F, 0F, -0.5F, 4F, 8F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-3.4F, 0.5F, -3.4F, 0F, 0.7854F, 0F));
        PartDefinition p_arm_0_tip = p_arm_0.addOrReplaceChild("arm_0_tip", CubeListBuilder.create()
                .texOffs(9, 88).addBox(-1.5F, 0F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_arm_1 = p_maw.addOrReplaceChild("arm_1", CubeListBuilder.create()
                .texOffs(94, 74).addBox(-2F, 0F, -0.5F, 4F, 8F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F)),
                PartPose.offsetAndRotation(3.4F, 0.5F, -3.4F, 0F, 2.3562F, 0F));
        PartDefinition p_arm_1_tip = p_arm_1.addOrReplaceChild("arm_1_tip", CubeListBuilder.create()
                .texOffs(18, 88).addBox(-1.5F, 0F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_arm_2 = p_maw.addOrReplaceChild("arm_2", CubeListBuilder.create()
                .texOffs(105, 74).addBox(-2F, 0F, -0.5F, 4F, 8F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F)),
                PartPose.offsetAndRotation(3.4F, 0.5F, 3.4F, 0F, 3.927F, 0F));
        PartDefinition p_arm_2_tip = p_arm_2.addOrReplaceChild("arm_2_tip", CubeListBuilder.create()
                .texOffs(27, 88).addBox(-1.5F, 0F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_arm_3 = p_maw.addOrReplaceChild("arm_3", CubeListBuilder.create()
                .texOffs(116, 74).addBox(-2F, 0F, -0.5F, 4F, 8F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-3.4F, 0.5F, 3.4F, 0F, 5.4978F, 0F));
        PartDefinition p_arm_3_tip = p_arm_3.addOrReplaceChild("arm_3_tip", CubeListBuilder.create()
                .texOffs(36, 88).addBox(-1.5F, 0F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tentacle_0 = p_bell.addOrReplaceChild("tentacle_0", CubeListBuilder.create()
                .texOffs(45, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F),
                PartPose.offset(6.0976F, 1.2F, 2.5257F));
        PartDefinition p_tentacle_0_mid = p_tentacle_0.addOrReplaceChild("tentacle_0_mid", CubeListBuilder.create()
                .texOffs(50, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_0_tip = p_tentacle_0_mid.addOrReplaceChild("tentacle_0_tip", CubeListBuilder.create()
                .texOffs(85, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(127, 104).addBox(-0.5F, 5.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_1 = p_bell.addOrReplaceChild("tentacle_1", CubeListBuilder.create()
                .texOffs(90, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F),
                PartPose.offset(2.5257F, 1.2F, 6.0976F));
        PartDefinition p_tentacle_1_mid = p_tentacle_1.addOrReplaceChild("tentacle_1_mid", CubeListBuilder.create()
                .texOffs(95, 88).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tentacle_1_tip = p_tentacle_1_mid.addOrReplaceChild("tentacle_1_tip", CubeListBuilder.create()
                .texOffs(153, 97).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(132, 104).addBox(-0.5F, 4.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_tentacle_2 = p_bell.addOrReplaceChild("tentacle_2", CubeListBuilder.create()
                .texOffs(55, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F),
                PartPose.offset(-2.5257F, 1.2F, 6.0976F));
        PartDefinition p_tentacle_2_mid = p_tentacle_2.addOrReplaceChild("tentacle_2_mid", CubeListBuilder.create()
                .texOffs(60, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_2_tip = p_tentacle_2_mid.addOrReplaceChild("tentacle_2_tip", CubeListBuilder.create()
                .texOffs(100, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(137, 104).addBox(-0.5F, 5.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_3 = p_bell.addOrReplaceChild("tentacle_3", CubeListBuilder.create()
                .texOffs(105, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F),
                PartPose.offset(-6.0976F, 1.2F, 2.5257F));
        PartDefinition p_tentacle_3_mid = p_tentacle_3.addOrReplaceChild("tentacle_3_mid", CubeListBuilder.create()
                .texOffs(110, 88).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tentacle_3_tip = p_tentacle_3_mid.addOrReplaceChild("tentacle_3_tip", CubeListBuilder.create()
                .texOffs(0, 104).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(142, 104).addBox(-0.5F, 4.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_tentacle_4 = p_bell.addOrReplaceChild("tentacle_4", CubeListBuilder.create()
                .texOffs(65, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F),
                PartPose.offset(-6.0976F, 1.2F, -2.5257F));
        PartDefinition p_tentacle_4_mid = p_tentacle_4.addOrReplaceChild("tentacle_4_mid", CubeListBuilder.create()
                .texOffs(70, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_4_tip = p_tentacle_4_mid.addOrReplaceChild("tentacle_4_tip", CubeListBuilder.create()
                .texOffs(115, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(147, 104).addBox(-0.5F, 5.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_5 = p_bell.addOrReplaceChild("tentacle_5", CubeListBuilder.create()
                .texOffs(120, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F),
                PartPose.offset(-2.5257F, 1.2F, -6.0976F));
        PartDefinition p_tentacle_5_mid = p_tentacle_5.addOrReplaceChild("tentacle_5_mid", CubeListBuilder.create()
                .texOffs(125, 88).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tentacle_5_tip = p_tentacle_5_mid.addOrReplaceChild("tentacle_5_tip", CubeListBuilder.create()
                .texOffs(5, 104).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(152, 104).addBox(-0.5F, 4.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_tentacle_6 = p_bell.addOrReplaceChild("tentacle_6", CubeListBuilder.create()
                .texOffs(75, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F),
                PartPose.offset(2.5257F, 1.2F, -6.0976F));
        PartDefinition p_tentacle_6_mid = p_tentacle_6.addOrReplaceChild("tentacle_6_mid", CubeListBuilder.create()
                .texOffs(80, 88).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_6_tip = p_tentacle_6_mid.addOrReplaceChild("tentacle_6_tip", CubeListBuilder.create()
                .texOffs(130, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(0, 111).addBox(-0.5F, 5.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_tentacle_7 = p_bell.addOrReplaceChild("tentacle_7", CubeListBuilder.create()
                .texOffs(135, 88).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F),
                PartPose.offset(6.0976F, 1.2F, -2.5257F));
        PartDefinition p_tentacle_7_mid = p_tentacle_7.addOrReplaceChild("tentacle_7_mid", CubeListBuilder.create()
                .texOffs(140, 88).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_tentacle_7_tip = p_tentacle_7_mid.addOrReplaceChild("tentacle_7_tip", CubeListBuilder.create()
                .texOffs(10, 104).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(5, 111).addBox(-0.5F, 4.7F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 5.5F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
