package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Nyxar, dem Schattendrachen (Endgegner, Grundgröße, im Spiel ×2,3).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class NyxarGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private NyxarGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(117, 0).addBox(-11.5F, -7.5F, -22F, 23F, 10F, 44F)
                .texOffs(0, 0).addBox(-7F, -12F, -21.92F, 14F, 19F, 43.84F)
                .texOffs(152, 138).addBox(-13F, -8F, -24F, 26F, 11F, 16F)
                .texOffs(155, 64).addBox(-8F, -13F, -23.92F, 16F, 21F, 15.84F)
                .texOffs(411, 169).addBox(-12.2F, -8F, 8F, 24.4F, 9.5F, 13F)
                .texOffs(344, 103).addBox(-7.7F, -12.5F, 8.08F, 15.4F, 18.5F, 12.84F)
                .texOffs(252, 0).addBox(-5.5F, -15F, -21F, 11F, 4F, 38F)
                .texOffs(406, 298).addBox(8.5F, -12.5F, -18F, 6F, 3F, 11F)
                .texOffs(272, 242).addBox(10.5F, -14.5F, -17.92F, 2F, 7F, 10.84F)
                .texOffs(25, 314).addBox(11F, -6F, -15F, 1.4F, 10.5F, 2.2F)
                .texOffs(361, 314).addBox(11F, -5.6F, -9.5F, 1.4F, 9.9F, 2.2F)
                .texOffs(372, 314).addBox(11F, -5.2F, -4F, 1.4F, 9.3F, 2.2F)
                .texOffs(253, 329).addBox(11F, -4.8F, 1.5F, 1.4F, 8.7F, 2.2F)
                .texOffs(441, 298).addBox(-14.5F, -12.5F, -18F, 6F, 3F, 11F)
                .texOffs(299, 242).addBox(-12.5F, -14.5F, -17.92F, 2F, 7F, 10.84F)
                .texOffs(36, 314).addBox(-12.4F, -6F, -15F, 1.4F, 10.5F, 2.2F)
                .texOffs(383, 314).addBox(-12.4F, -5.6F, -9.5F, 1.4F, 9.9F, 2.2F)
                .texOffs(394, 314).addBox(-12.4F, -5.2F, -4F, 1.4F, 9.3F, 2.2F)
                .texOffs(264, 329).addBox(-12.4F, -4.8F, 1.5F, 1.4F, 8.7F, 2.2F)
                .texOffs(106, 242).addBox(-9F, 7.5F, -23F, 18F, 2.2F, 15F)
                .texOffs(403, 103).addBox(-8.5F, 6.4F, -8F, 17F, 2F, 28F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_back_spike1 = p_body.addOrReplaceChild("back_spike1", CubeListBuilder.create()
                .texOffs(42, 342).addBox(-1.7F, -3.472F, -2.125F, 3.4F, 4.072F, 4.25F)
                .texOffs(173, 370).addBox(-1.105F, -6.111F, -1.3813F, 2.21F, 2.889F, 2.7625F)
                .texOffs(187, 384).addBox(-0.51F, -8.75F, -0.6375F, 1.02F, 2.889F, 1.275F),
                PartPose.offsetAndRotation(0F, -14.6F, -18F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike2 = p_body.addOrReplaceChild("back_spike2", CubeListBuilder.create()
                .texOffs(61, 342).addBox(-1.7F, -4.312F, -2.125F, 3.4F, 4.912F, 4.25F)
                .texOffs(15, 362).addBox(-1.105F, -7.531F, -1.3813F, 2.21F, 3.469F, 2.7625F)
                .texOffs(208, 377).addBox(-0.51F, -10.75F, -0.6375F, 1.02F, 3.469F, 1.275F),
                PartPose.offsetAndRotation(0F, -14.6F, -11F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike3 = p_body.addOrReplaceChild("back_spike3", CubeListBuilder.create()
                .texOffs(369, 329).addBox(-1.7F, -4.732F, -2.125F, 3.4F, 5.332F, 4.25F)
                .texOffs(28, 362).addBox(-1.105F, -8.241F, -1.3813F, 2.21F, 3.759F, 2.7625F)
                .texOffs(217, 377).addBox(-0.51F, -11.75F, -0.6375F, 1.02F, 3.759F, 1.275F),
                PartPose.offsetAndRotation(0F, -14.6F, -4F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike4 = p_body.addOrReplaceChild("back_spike4", CubeListBuilder.create()
                .texOffs(80, 342).addBox(-1.7F, -4.312F, -2.125F, 3.4F, 4.912F, 4.25F)
                .texOffs(41, 362).addBox(-1.105F, -7.531F, -1.3813F, 2.21F, 3.469F, 2.7625F)
                .texOffs(226, 377).addBox(-0.51F, -10.75F, -0.6375F, 1.02F, 3.469F, 1.275F),
                PartPose.offsetAndRotation(0F, -14.6F, 3F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike5 = p_body.addOrReplaceChild("back_spike5", CubeListBuilder.create()
                .texOffs(99, 342).addBox(-1.7F, -3.472F, -2.125F, 3.4F, 4.072F, 4.25F)
                .texOffs(186, 370).addBox(-1.105F, -6.111F, -1.3813F, 2.21F, 2.889F, 2.7625F)
                .texOffs(196, 384).addBox(-0.51F, -8.75F, -0.6375F, 1.02F, 2.889F, 1.275F),
                PartPose.offsetAndRotation(0F, -14.6F, 10F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike6 = p_body.addOrReplaceChild("back_spike6", CubeListBuilder.create()
                .texOffs(431, 342).addBox(-1.7F, -2.632F, -2.125F, 3.4F, 3.232F, 4.25F)
                .texOffs(199, 370).addBox(-1.105F, -4.691F, -1.3813F, 2.21F, 2.309F, 2.7625F)
                .texOffs(205, 384).addBox(-0.51F, -6.75F, -0.6375F, 1.02F, 2.309F, 1.275F),
                PartPose.offsetAndRotation(0F, -14.6F, 16F, -0.4538F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(260, 280).addBox(-6.75F, -2.6F, -8.1F, 13.5F, 5.2F, 8.7F)
                .texOffs(0, 219).addBox(-2.85F, -6.5F, -8.02F, 5.7F, 13F, 8.54F)
                .texOffs(419, 329).addBox(-2.85F, 5.5F, -7.5F, 5.7F, 1.6F, 7.5F),
                PartPose.offsetAndRotation(0F, -6F, -22F, -0.5236F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(117, 298).addBox(-6.3F, -2.44F, -7.6F, 12.6F, 4.88F, 8.2F)
                .texOffs(31, 219).addBox(-2.64F, -6.1F, -7.52F, 5.28F, 12.2F, 8.04F)
                .texOffs(302, 342).addBox(-2.64F, 5.1F, -7F, 5.28F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.2094F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(162, 298).addBox(-5.9F, -2.28F, -7.6F, 11.8F, 4.56F, 8.2F)
                .texOffs(62, 219).addBox(-2.48F, -5.7F, -7.52F, 4.96F, 11.4F, 8.04F)
                .texOffs(356, 342).addBox(-2.48F, 4.7F, -7F, 4.96F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.0698F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(47, 314).addBox(-5.5F, -2.12F, -7.1F, 11F, 4.24F, 7.7F)
                .texOffs(33, 242).addBox(-2.32F, -5.3F, -7.02F, 4.64F, 10.6F, 7.54F)
                .texOffs(381, 342).addBox(-2.32F, 4.3F, -6.5F, 4.64F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.0524F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(405, 314).addBox(-5.2F, -2F, -7.1F, 10.4F, 4F, 7.7F)
                .texOffs(326, 242).addBox(-2.2F, -5F, -7.02F, 4.4F, 10F, 7.54F)
                .texOffs(406, 342).addBox(-2.2F, 4F, -6.5F, 4.4F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1047F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(444, 314).addBox(-4.9F, -1.88F, -6.6F, 9.8F, 3.76F, 7.2F)
                .texOffs(353, 242).addBox(-2.08F, -4.7F, -6.52F, 4.16F, 9.4F, 7.04F)
                .texOffs(485, 342).addBox(-2.08F, 3.7F, -6F, 4.16F, 1.6F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1396F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(0, 329).addBox(-4.7F, -1.8F, -6.6F, 9.4F, 3.6F, 7.2F)
                .texOffs(58, 280).addBox(-2F, -4.5F, -6.52F, 4F, 9F, 7.04F)
                .texOffs(0, 353).addBox(-2F, 3.5F, -6F, 4F, 1.6F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.1222F, 0F, 0F));
        PartDefinition p_neck7_spike = p_neck7.addOrReplaceChild("neck7_spike", CubeListBuilder.create()
                .texOffs(437, 377).addBox(-1.2F, -1.204F, -1.5F, 2.4F, 1.804F, 3F)
                .texOffs(310, 390).addBox(-0.78F, -2.277F, -0.975F, 1.56F, 1.323F, 1.95F)
                .texOffs(216, 395).addBox(-0.36F, -3.35F, -0.45F, 0.72F, 1.323F, 0.9F),
                PartPose.offsetAndRotation(0F, -4.1F, -2.7F, -0.6283F, 0F, 0F));
        PartDefinition p_head = p_neck7.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(137, 219).addBox(-6.2F, -3.4F, -13F, 12.4F, 4.8F, 14.5F)
                .texOffs(171, 169).addBox(-3.6F, -6F, -12.92F, 7.2F, 10F, 14.34F)
                .texOffs(450, 342).addBox(-5F, -5F, -17F, 10F, 0.6F, 7F)
                .texOffs(388, 329).addBox(-3.8F, -6.2F, -16.92F, 7.6F, 3F, 6.84F)
                .texOffs(291, 298).addBox(-4.6F, -2.4F, -22F, 9.2F, 4F, 10F)
                .texOffs(173, 242).addBox(-2.6F, -4.4F, -21.92F, 5.2F, 8F, 9.84F)
                .texOffs(123, 314).addBox(-3.9F, -2.1F, -30F, 7.8F, 3.8F, 9F)
                .texOffs(0, 280).addBox(-2.1F, -3.9F, -29.92F, 4.2F, 7.4F, 8.84F)
                .texOffs(446, 262).addBox(-2.4F, -5.5F, -28.5F, 4.8F, 1.8F, 15F)
                .texOffs(156, 370).addBox(-3F, -3.4F, -30.9F, 6F, 4F, 1.2F)
                .texOffs(86, 400).addBox(1.2F, -3.2F, -31.1F, 1.4F, 0.9F, 0.6F)
                .texOffs(211, 329).addBox(4.8F, -0.1F, -14F, 2F, 3.4F, 8F)
                .texOffs(323, 314).addBox(5.5F, -0.8F, -13.92F, 0.6F, 4.8F, 7.84F)
                .texOffs(212, 370).addBox(4.75F, -3.8F, -13.6F, 1.8F, 2F, 3.6F)
                .texOffs(463, 377).addBox(4.95F, -4.2F, -9F, 1.5F, 1.5F, 2.4F)
                .texOffs(319, 390).addBox(5.05F, -4.4F, -5.6F, 1.3F, 1.2F, 1.8F)
                .texOffs(175, 395).addBox(3.275F, 3.7F, -28.425F, 0.85F, 2.16F, 0.85F)
                .texOffs(221, 395).addBox(3.275F, 3.7F, -26.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(226, 395).addBox(3.275F, 3.7F, -24.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(231, 395).addBox(3.275F, 3.7F, -22.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(236, 395).addBox(3.275F, 3.7F, -20.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(241, 395).addBox(3.275F, 3.7F, -18.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(180, 395).addBox(3.275F, 3.7F, -16.425F, 0.85F, 2.16F, 0.85F)
                .texOffs(235, 377).addBox(2.7F, 3.7F, -28.2F, 1.2F, 3.2F, 1.2F)
                .texOffs(93, 400).addBox(-2.6F, -3.2F, -31.1F, 1.4F, 0.9F, 0.6F)
                .texOffs(232, 329).addBox(-6.8F, -0.1F, -14F, 2F, 3.4F, 8F)
                .texOffs(342, 314).addBox(-6.1F, -0.8F, -13.92F, 0.6F, 4.8F, 7.84F)
                .texOffs(225, 370).addBox(-6.55F, -3.8F, -13.6F, 1.8F, 2F, 3.6F)
                .texOffs(474, 377).addBox(-6.45F, -4.2F, -9F, 1.5F, 1.5F, 2.4F)
                .texOffs(328, 390).addBox(-6.35F, -4.4F, -5.6F, 1.3F, 1.2F, 1.8F)
                .texOffs(185, 395).addBox(-4.125F, 3.7F, -28.425F, 0.85F, 2.16F, 0.85F)
                .texOffs(246, 395).addBox(-4.125F, 3.7F, -26.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(251, 395).addBox(-4.125F, 3.7F, -24.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(256, 395).addBox(-4.125F, 3.7F, -22.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(261, 395).addBox(-4.125F, 3.7F, -20.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(266, 395).addBox(-4.125F, 3.7F, -18.425F, 0.85F, 1.6F, 0.85F)
                .texOffs(190, 395).addBox(-4.125F, 3.7F, -16.425F, 0.85F, 2.16F, 0.85F)
                .texOffs(244, 377).addBox(-3.9F, 3.7F, -28.2F, 1.2F, 3.2F, 1.2F)
                .texOffs(271, 395).addBox(-2.4F, 3.7F, -29.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(276, 395).addBox(-1.1F, 3.7F, -29.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(281, 395).addBox(0.3F, 3.7F, -29.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(286, 395).addBox(1.6F, 3.7F, -29.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(180, 262).addBox(-3.4F, 3.72F, -29.2F, 6.8F, 0.3F, 15.5F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.5236F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(68, 329).addBox(-2.3F, -1.4F, -0.6F, 4.6F, 2.4F, 9F),
                PartPose.offsetAndRotation(4.2F, -5.3F, -12.8F, 0.1396F, 0F, -0.2094F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(97, 329).addBox(-2.3F, -1.4F, -0.6F, 4.6F, 2.4F, 9F),
                PartPose.offsetAndRotation(-4.2F, -5.3F, -12.8F, 0.1396F, 0F, 0.2094F));
        PartDefinition p_crest1 = p_head.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(485, 377).addBox(-1F, -0.91F, -1.25F, 2F, 1.51F, 2.5F)
                .texOffs(337, 390).addBox(-0.65F, -1.78F, -0.8125F, 1.3F, 1.12F, 1.625F)
                .texOffs(291, 395).addBox(-0.3F, -2.65F, -0.375F, 0.6F, 1.12F, 0.75F),
                PartPose.offsetAndRotation(0F, -6F, -10F, -0.9599F, 0F, 0F));
        PartDefinition p_crest2 = p_head.addOrReplaceChild("crest2", CubeListBuilder.create()
                .texOffs(496, 377).addBox(-1F, -1.162F, -1.25F, 2F, 1.762F, 2.5F)
                .texOffs(346, 390).addBox(-0.65F, -2.206F, -0.8125F, 1.3F, 1.294F, 1.625F)
                .texOffs(296, 395).addBox(-0.3F, -3.25F, -0.375F, 0.6F, 1.294F, 0.75F),
                PartPose.offsetAndRotation(0F, -6F, -5.5F, -0.9599F, 0F, 0F));
        PartDefinition p_crest3 = p_head.addOrReplaceChild("crest3", CubeListBuilder.create()
                .texOffs(0, 384).addBox(-1F, -1.33F, -1.25F, 2F, 1.93F, 2.5F)
                .texOffs(355, 390).addBox(-0.65F, -2.49F, -0.8125F, 1.3F, 1.41F, 1.625F)
                .texOffs(301, 395).addBox(-0.3F, -3.65F, -0.375F, 0.6F, 1.41F, 0.75F),
                PartPose.offsetAndRotation(0F, -6F, -1F, -0.9599F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(300, 329).addBox(-2.1F, -2.1F, -0.4F, 4.2F, 4.2F, 5.35F)
                .texOffs(65, 353).addBox(-1.302F, -1.302F, 4.5F, 2.604F, 2.604F, 4.75F),
                PartPose.offsetAndRotation(4.4F, -5.4F, -3.5F, 0.384F, 0.384F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(475, 329).addBox(-1.65F, -1.65F, -0.4F, 3.3F, 3.3F, 5.075F)
                .texOffs(82, 353).addBox(-1.023F, -1.023F, 4.25F, 2.046F, 2.046F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1745F, 0.1222F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(99, 353).addBox(-1.25F, -1.25F, -0.4F, 2.5F, 2.5F, 4.525F)
                .texOffs(238, 370).addBox(-0.775F, -0.775F, 3.75F, 1.55F, 1.55F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.1571F, 0.0524F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(251, 370).addBox(-0.9F, -0.9F, -0.4F, 1.8F, 1.8F, 3.975F)
                .texOffs(264, 370).addBox(-0.558F, -0.558F, 3.25F, 1.116F, 1.116F, 3.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.0698F, -0.0349F, 0F));
        PartDefinition p_horn_l5 = p_horn_l4.addOrReplaceChild("horn_l5", CubeListBuilder.create()
                .texOffs(277, 370).addBox(-0.6F, -0.6F, -0.4F, 1.2F, 1.2F, 3.15F)
                .texOffs(364, 390).addBox(-0.372F, -0.372F, 2.5F, 0.744F, 0.744F, 2.75F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2094F, -0.1047F, 0F));
        PartDefinition p_horn_l_up1 = p_head.addOrReplaceChild("horn_l_up1", CubeListBuilder.create()
                .texOffs(232, 353).addBox(-1.4F, -1.4F, -0.4F, 2.8F, 2.8F, 3.975F)
                .texOffs(290, 370).addBox(-0.868F, -0.868F, 3.25F, 1.736F, 1.736F, 3.5F),
                PartPose.offsetAndRotation(2.6F, -6F, -6.5F, 1.0821F, 0.1745F, 0F));
        PartDefinition p_horn_l_up2 = p_horn_l_up1.addOrReplaceChild("horn_l_up2", CubeListBuilder.create()
                .texOffs(303, 370).addBox(-1F, -1F, -0.4F, 2F, 2F, 3.7F)
                .texOffs(316, 370).addBox(-0.62F, -0.62F, 3F, 1.24F, 1.24F, 3.25F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2443F, 0.0698F, 0F));
        PartDefinition p_horn_l_up3 = p_horn_l_up2.addOrReplaceChild("horn_l_up3", CubeListBuilder.create()
                .texOffs(329, 370).addBox(-0.7F, -0.7F, -0.4F, 1.4F, 1.4F, 3.15F)
                .texOffs(373, 390).addBox(-0.434F, -0.434F, 2.5F, 0.868F, 0.868F, 2.75F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0.2793F, 0F, 0F));
        PartDefinition p_horn_l_low1 = p_head.addOrReplaceChild("horn_l_low1", CubeListBuilder.create()
                .texOffs(247, 353).addBox(-1.2F, -1.2F, -0.4F, 2.4F, 2.4F, 3.425F)
                .texOffs(11, 384).addBox(-0.744F, -0.744F, 2.75F, 1.488F, 1.488F, 3F),
                PartPose.offsetAndRotation(5.8F, -2.2F, -2.5F, 0.0698F, 0.7679F, 0F));
        PartDefinition p_horn_l_low2 = p_horn_l_low1.addOrReplaceChild("horn_l_low2", CubeListBuilder.create()
                .texOffs(22, 384).addBox(-0.9F, -0.9F, -0.4F, 1.8F, 1.8F, 2.875F)
                .texOffs(33, 384).addBox(-0.558F, -0.558F, 2.25F, 1.116F, 1.116F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, 0.1396F, 0F));
        PartDefinition p_horn_l_low3 = p_horn_l_low2.addOrReplaceChild("horn_l_low3", CubeListBuilder.create()
                .texOffs(44, 384).addBox(-0.6F, -0.6F, -0.4F, 1.2F, 1.2F, 2.325F)
                .texOffs(195, 395).addBox(-0.372F, -0.372F, 1.75F, 0.744F, 0.744F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(214, 384).addBox(-0.75F, -1.414F, -0.9375F, 1.5F, 2.014F, 1.875F)
                .texOffs(126, 395).addBox(-0.4875F, -2.632F, -0.6094F, 0.975F, 1.468F, 1.2188F)
                .texOffs(306, 395).addBox(-0.25F, -3.85F, -0.2813F, 0.5F, 1.468F, 0.5625F),
                PartPose.offsetAndRotation(6.6F, 1.4F, -7F, -1.7453F, 0.5236F, 0F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(382, 390).addBox(-0.65F, -1.162F, -0.8125F, 1.3F, 1.762F, 1.625F)
                .texOffs(133, 395).addBox(-0.4225F, -2.206F, -0.5281F, 0.845F, 1.294F, 1.0563F)
                .texOffs(311, 395).addBox(-0.25F, -3.25F, -0.25F, 0.5F, 1.294F, 0.5F),
                PartPose.offsetAndRotation(6.4F, 3.4F, -4.5F, -1.9199F, 0.4538F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(323, 329).addBox(-2.1F, -2.1F, -0.4F, 4.2F, 4.2F, 5.35F)
                .texOffs(116, 353).addBox(-1.302F, -1.302F, 4.5F, 2.604F, 2.604F, 4.75F),
                PartPose.offsetAndRotation(-4.4F, -5.4F, -3.5F, 0.384F, -0.384F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(0, 342).addBox(-1.65F, -1.65F, -0.4F, 3.3F, 3.3F, 5.075F)
                .texOffs(133, 353).addBox(-1.023F, -1.023F, 4.25F, 2.046F, 2.046F, 4.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1745F, -0.1222F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(150, 353).addBox(-1.25F, -1.25F, -0.4F, 2.5F, 2.5F, 4.525F)
                .texOffs(342, 370).addBox(-0.775F, -0.775F, 3.75F, 1.55F, 1.55F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.1571F, -0.0524F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(355, 370).addBox(-0.9F, -0.9F, -0.4F, 1.8F, 1.8F, 3.975F)
                .texOffs(368, 370).addBox(-0.558F, -0.558F, 3.25F, 1.116F, 1.116F, 3.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.0698F, 0.0349F, 0F));
        PartDefinition p_horn_r5 = p_horn_r4.addOrReplaceChild("horn_r5", CubeListBuilder.create()
                .texOffs(381, 370).addBox(-0.6F, -0.6F, -0.4F, 1.2F, 1.2F, 3.15F)
                .texOffs(391, 390).addBox(-0.372F, -0.372F, 2.5F, 0.744F, 0.744F, 2.75F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2094F, 0.1047F, 0F));
        PartDefinition p_horn_r_up1 = p_head.addOrReplaceChild("horn_r_up1", CubeListBuilder.create()
                .texOffs(262, 353).addBox(-1.4F, -1.4F, -0.4F, 2.8F, 2.8F, 3.975F)
                .texOffs(394, 370).addBox(-0.868F, -0.868F, 3.25F, 1.736F, 1.736F, 3.5F),
                PartPose.offsetAndRotation(-2.6F, -6F, -6.5F, 1.0821F, -0.1745F, 0F));
        PartDefinition p_horn_r_up2 = p_horn_r_up1.addOrReplaceChild("horn_r_up2", CubeListBuilder.create()
                .texOffs(407, 370).addBox(-1F, -1F, -0.4F, 2F, 2F, 3.7F)
                .texOffs(420, 370).addBox(-0.62F, -0.62F, 3F, 1.24F, 1.24F, 3.25F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2443F, -0.0698F, 0F));
        PartDefinition p_horn_r_up3 = p_horn_r_up2.addOrReplaceChild("horn_r_up3", CubeListBuilder.create()
                .texOffs(433, 370).addBox(-0.7F, -0.7F, -0.4F, 1.4F, 1.4F, 3.15F)
                .texOffs(400, 390).addBox(-0.434F, -0.434F, 2.5F, 0.868F, 0.868F, 2.75F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0.2793F, 0F, 0F));
        PartDefinition p_horn_r_low1 = p_head.addOrReplaceChild("horn_r_low1", CubeListBuilder.create()
                .texOffs(277, 353).addBox(-1.2F, -1.2F, -0.4F, 2.4F, 2.4F, 3.425F)
                .texOffs(55, 384).addBox(-0.744F, -0.744F, 2.75F, 1.488F, 1.488F, 3F),
                PartPose.offsetAndRotation(-5.8F, -2.2F, -2.5F, 0.0698F, -0.7679F, 0F));
        PartDefinition p_horn_r_low2 = p_horn_r_low1.addOrReplaceChild("horn_r_low2", CubeListBuilder.create()
                .texOffs(66, 384).addBox(-0.9F, -0.9F, -0.4F, 1.8F, 1.8F, 2.875F)
                .texOffs(77, 384).addBox(-0.558F, -0.558F, 2.25F, 1.116F, 1.116F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, -0.1396F, 0F));
        PartDefinition p_horn_r_low3 = p_horn_r_low2.addOrReplaceChild("horn_r_low3", CubeListBuilder.create()
                .texOffs(88, 384).addBox(-0.6F, -0.6F, -0.4F, 1.2F, 1.2F, 2.325F)
                .texOffs(202, 395).addBox(-0.372F, -0.372F, 1.75F, 0.744F, 0.744F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(223, 384).addBox(-0.75F, -1.414F, -0.9375F, 1.5F, 2.014F, 1.875F)
                .texOffs(140, 395).addBox(-0.4875F, -2.632F, -0.6094F, 0.975F, 1.468F, 1.2188F)
                .texOffs(316, 395).addBox(-0.25F, -3.85F, -0.2813F, 0.5F, 1.468F, 0.5625F),
                PartPose.offsetAndRotation(-6.6F, 1.4F, -7F, -1.7453F, -0.5236F, 0F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(409, 390).addBox(-0.65F, -1.162F, -0.8125F, 1.3F, 1.762F, 1.625F)
                .texOffs(147, 395).addBox(-0.4225F, -2.206F, -0.5281F, 0.845F, 1.294F, 1.0563F)
                .texOffs(321, 395).addBox(-0.25F, -3.25F, -0.25F, 0.5F, 1.294F, 0.5F),
                PartPose.offsetAndRotation(-6.4F, 3.4F, -4.5F, -1.9199F, -0.4538F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(194, 219).addBox(-4.2F, 1.3F, -18.2F, 8.4F, 0.8F, 18.5F)
                .texOffs(0, 195).addBox(-2.9F, 0F, -18.12F, 5.8F, 3.4F, 18.34F)
                .texOffs(201, 353).addBox(-5F, 0.9F, -3F, 10F, 1.4F, 5F)
                .texOffs(448, 329).addBox(-3.6F, -0.5F, -2.92F, 7.2F, 4.2F, 4.84F)
                .texOffs(329, 342).addBox(-3F, 2.4F, -17.8F, 6F, 1.6F, 7F)
                .texOffs(326, 395).addBox(3F, -1.79F, -17.0286F, 0.8F, 1.89F, 0.8F)
                .texOffs(331, 395).addBox(3F, -1.3F, -15.0857F, 0.8F, 1.4F, 0.8F)
                .texOffs(336, 395).addBox(3F, -1.3F, -13.1429F, 0.8F, 1.4F, 0.8F)
                .texOffs(341, 395).addBox(3F, -1.3F, -11.2F, 0.8F, 1.4F, 0.8F)
                .texOffs(346, 395).addBox(3F, -1.3F, -9.2571F, 0.8F, 1.4F, 0.8F)
                .texOffs(351, 395).addBox(3F, -1.3F, -7.3143F, 0.8F, 1.4F, 0.8F)
                .texOffs(356, 395).addBox(3F, -1.79F, -5.3714F, 0.8F, 1.89F, 0.8F)
                .texOffs(361, 395).addBox(-3.8F, -1.79F, -17.0286F, 0.8F, 1.89F, 0.8F)
                .texOffs(366, 395).addBox(-3.8F, -1.3F, -15.0857F, 0.8F, 1.4F, 0.8F)
                .texOffs(371, 395).addBox(-3.8F, -1.3F, -13.1429F, 0.8F, 1.4F, 0.8F)
                .texOffs(376, 395).addBox(-3.8F, -1.3F, -11.2F, 0.8F, 1.4F, 0.8F)
                .texOffs(381, 395).addBox(-3.8F, -1.3F, -9.2571F, 0.8F, 1.4F, 0.8F)
                .texOffs(386, 395).addBox(-3.8F, -1.3F, -7.3143F, 0.8F, 1.4F, 0.8F)
                .texOffs(391, 395).addBox(-3.8F, -1.79F, -5.3714F, 0.8F, 1.89F, 0.8F)
                .texOffs(448, 219).addBox(-3.5F, -0.15F, -17.8F, 7F, 0.3F, 18F)
                .texOffs(442, 280).addBox(-2.4F, -0.5F, -16.5F, 4.8F, 0.6F, 14F)
                .texOffs(86, 314).addBox(-3F, 3.2F, -12F, 6F, 0.8F, 12F),
                PartPose.offsetAndRotation(0F, 3.7F, -11.5F, 0.0698F, 0F, 0F));
        PartDefinition p_beard_l = p_jaw.addOrReplaceChild("beard_l", CubeListBuilder.create()
                .texOffs(418, 390).addBox(-0.7F, -1.162F, -0.875F, 1.4F, 1.762F, 1.75F)
                .texOffs(154, 395).addBox(-0.455F, -2.206F, -0.5687F, 0.91F, 1.294F, 1.1375F)
                .texOffs(396, 395).addBox(-0.25F, -3.25F, -0.2625F, 0.5F, 1.294F, 0.525F),
                PartPose.offsetAndRotation(2.4F, 3.4F, -4F, -2.618F, -0.1745F, 0F));
        PartDefinition p_beard_r = p_jaw.addOrReplaceChild("beard_r", CubeListBuilder.create()
                .texOffs(427, 390).addBox(-0.7F, -1.162F, -0.875F, 1.4F, 1.762F, 1.75F)
                .texOffs(161, 395).addBox(-0.455F, -2.206F, -0.5687F, 0.91F, 1.294F, 1.1375F)
                .texOffs(401, 395).addBox(-0.25F, -3.25F, -0.2625F, 0.5F, 1.294F, 0.525F),
                PartPose.offsetAndRotation(-2.4F, 3.4F, -4F, -2.618F, 0.1745F, 0F));
        PartDefinition p_beard1 = p_jaw.addOrReplaceChild("beard1", CubeListBuilder.create()
                .texOffs(436, 390).addBox(-0.8F, -1.246F, -1F, 1.6F, 1.846F, 2F)
                .texOffs(445, 390).addBox(-0.52F, -2.348F, -0.65F, 1.04F, 1.352F, 1.3F)
                .texOffs(406, 395).addBox(-0.25F, -3.45F, -0.3F, 0.5F, 1.352F, 0.6F),
                PartPose.offsetAndRotation(0F, 3.6F, -8F, -2.618F, 0F, 0F));
        PartDefinition p_neck6_spike = p_neck6.addOrReplaceChild("neck6_spike", CubeListBuilder.create()
                .texOffs(450, 377).addBox(-1.2F, -1.351F, -1.5F, 2.4F, 1.951F, 3F)
                .texOffs(454, 390).addBox(-0.78F, -2.5255F, -0.975F, 1.56F, 1.4245F, 1.95F)
                .texOffs(411, 395).addBox(-0.36F, -3.7F, -0.45F, 0.72F, 1.4245F, 0.9F),
                PartPose.offsetAndRotation(0F, -4.3F, -2.7F, -0.6283F, 0F, 0F));
        PartDefinition p_neck5_spike = p_neck5.addOrReplaceChild("neck5_spike", CubeListBuilder.create()
                .texOffs(446, 370).addBox(-1.2F, -1.498F, -1.5F, 2.4F, 2.098F, 3F)
                .texOffs(463, 390).addBox(-0.78F, -2.774F, -0.975F, 1.56F, 1.526F, 1.95F)
                .texOffs(416, 395).addBox(-0.36F, -4.05F, -0.45F, 0.72F, 1.526F, 0.9F),
                PartPose.offsetAndRotation(0F, -4.6F, -2.925F, -0.6283F, 0F, 0F));
        PartDefinition p_neck4_spike = p_neck4.addOrReplaceChild("neck4_spike", CubeListBuilder.create()
                .texOffs(459, 370).addBox(-1.2F, -1.645F, -1.5F, 2.4F, 2.245F, 3F)
                .texOffs(472, 390).addBox(-0.78F, -3.0225F, -0.975F, 1.56F, 1.6275F, 1.95F)
                .texOffs(421, 395).addBox(-0.36F, -4.4F, -0.45F, 0.72F, 1.6275F, 0.9F),
                PartPose.offsetAndRotation(0F, -4.9F, -2.925F, -0.6283F, 0F, 0F));
        PartDefinition p_neck3_spike = p_neck3.addOrReplaceChild("neck3_spike", CubeListBuilder.create()
                .texOffs(472, 370).addBox(-1.2F, -1.792F, -1.5F, 2.4F, 2.392F, 3F)
                .texOffs(481, 390).addBox(-0.78F, -3.271F, -0.975F, 1.56F, 1.729F, 1.95F)
                .texOffs(426, 395).addBox(-0.36F, -4.75F, -0.45F, 0.72F, 1.729F, 0.9F),
                PartPose.offsetAndRotation(0F, -5.3F, -3.15F, -0.6283F, 0F, 0F));
        PartDefinition p_neck2_spike = p_neck2.addOrReplaceChild("neck2_spike", CubeListBuilder.create()
                .texOffs(485, 370).addBox(-1.2F, -1.939F, -1.5F, 2.4F, 2.539F, 3F)
                .texOffs(490, 390).addBox(-0.78F, -3.5195F, -0.975F, 1.56F, 1.8305F, 1.95F)
                .texOffs(431, 395).addBox(-0.36F, -5.1F, -0.45F, 0.72F, 1.8305F, 0.9F),
                PartPose.offsetAndRotation(0F, -5.7F, -3.15F, -0.6283F, 0F, 0F));
        PartDefinition p_neck1_spike = p_neck1.addOrReplaceChild("neck1_spike", CubeListBuilder.create()
                .texOffs(498, 370).addBox(-1.2F, -2.086F, -1.5F, 2.4F, 2.686F, 3F)
                .texOffs(499, 390).addBox(-0.78F, -3.768F, -0.975F, 1.56F, 1.932F, 1.95F)
                .texOffs(436, 395).addBox(-0.36F, -5.45F, -0.45F, 0.72F, 1.932F, 0.9F),
                PartPose.offsetAndRotation(0F, -6.1F, -3.375F, -0.6283F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(338, 219).addBox(-6.75F, -2.5F, -0.6F, 13.5F, 5F, 13.7F)
                .texOffs(237, 138).addBox(-3F, -6.25F, -0.52F, 6F, 12.5F, 13.54F)
                .texOffs(0, 298).addBox(-3F, 5.25F, 0F, 6F, 1.6F, 12.5F),
                PartPose.offsetAndRotation(0F, -4F, 21F, -0.1745F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(395, 219).addBox(-6F, -2.2F, -0.6F, 12F, 4.4F, 13.2F)
                .texOffs(218, 169).addBox(-2.7F, -5.5F, -0.52F, 5.4F, 11F, 13.04F)
                .texOffs(332, 298).addBox(-2.7F, 4.5F, 0F, 5.4F, 1.6F, 12F),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(131, 262).addBox(-5.3F, -1.96F, -0.6F, 10.6F, 3.92F, 12.7F)
                .texOffs(51, 195).addBox(-2.36F, -4.9F, -0.52F, 4.72F, 9.8F, 12.54F)
                .texOffs(476, 298).addBox(-2.36F, 3.9F, 0F, 4.72F, 1.6F, 11.5F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0873F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(227, 262).addBox(-4.7F, -1.72F, -0.6F, 9.4F, 3.44F, 12.2F)
                .texOffs(452, 195).addBox(-2.12F, -4.3F, -0.52F, 4.24F, 8.6F, 12.04F)
                .texOffs(193, 314).addBox(-2.12F, 3.3F, 0F, 4.24F, 1.6F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 11.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(110, 280).addBox(-4.1F, -1.52F, -0.6F, 8.2F, 3.04F, 11.7F)
                .texOffs(251, 219).addBox(-1.82F, -3.8F, -0.52F, 3.64F, 7.6F, 11.54F)
                .texOffs(292, 314).addBox(-1.82F, 2.8F, 0F, 3.64F, 1.6F, 10.5F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0175F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(401, 280).addBox(-3.6F, -1.32F, -0.6F, 7.2F, 2.64F, 11.2F)
                .texOffs(0, 242).addBox(-1.62F, -3.3F, -0.52F, 3.24F, 6.6F, 11.04F)
                .texOffs(126, 329).addBox(-1.62F, 2.3F, 0F, 3.24F, 1.6F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(369, 298).addBox(-3.1F, -1.16F, -0.6F, 6.2F, 2.32F, 10.7F)
                .texOffs(29, 280).addBox(-1.36F, -2.9F, -0.52F, 2.72F, 5.8F, 10.54F)
                .texOffs(184, 329).addBox(-1.36F, 1.9F, 0F, 2.72F, 1.6F, 9.5F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(158, 314).addBox(-2.65F, -1F, -0.6F, 5.3F, 2F, 10.2F)
                .texOffs(231, 280).addBox(-1.15F, -2.5F, -0.52F, 2.3F, 5F, 10.04F)
                .texOffs(275, 329).addBox(-1.15F, 1.5F, 0F, 2.3F, 1.6F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9.5F, 0.0698F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(37, 329).addBox(-2.25F, -0.84F, -0.6F, 4.5F, 1.68F, 9.7F)
                .texOffs(39, 298).addBox(-0.99F, -2.1F, -0.52F, 1.98F, 4.2F, 9.54F)
                .texOffs(346, 329).addBox(-0.99F, 1.1F, 0F, 1.98F, 1.6F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(155, 329).addBox(-1.9F, -0.7F, -0.6F, 3.8F, 1.4F, 9.2F)
                .texOffs(0, 314).addBox(-0.85F, -1.75F, -0.52F, 1.7F, 3.5F, 9.04F)
                .texOffs(21, 342).addBox(-0.85F, 0.75F, 0F, 1.7F, 1.6F, 8F)
                .texOffs(64, 298).addBox(-6.5F, -0.5F, 4F, 13F, 1F, 13F)
                .texOffs(85, 280).addBox(-0.5F, -6F, 2F, 1F, 5.5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.0349F, 0F, 0F));
        PartDefinition p_tail9_spike = p_tail9.addOrReplaceChild("tail9_spike", CubeListBuilder.create()
                .texOffs(0, 395).addBox(-0.76F, -0.658F, -0.95F, 1.52F, 1.258F, 1.9F)
                .texOffs(209, 395).addBox(-0.494F, -1.354F, -0.6175F, 0.988F, 0.946F, 1.235F)
                .texOffs(100, 400).addBox(-0.25F, -2.05F, -0.285F, 0.5F, 0.946F, 0.57F),
                PartPose.offsetAndRotation(0F, -1.7F, 3.825F, -0.5934F, 0F, 0F));
        PartDefinition p_tail8_spike = p_tail8.addOrReplaceChild("tail8_spike", CubeListBuilder.create()
                .texOffs(99, 384).addBox(-0.84F, -0.847F, -1.05F, 1.68F, 1.447F, 2.1F)
                .texOffs(9, 395).addBox(-0.546F, -1.6735F, -0.6825F, 1.092F, 1.0765F, 1.365F)
                .texOffs(441, 395).addBox(-0.252F, -2.5F, -0.315F, 0.504F, 1.0765F, 0.63F),
                PartPose.offsetAndRotation(0F, -2.1F, 4.05F, -0.5934F, 0F, 0F));
        PartDefinition p_tail7_spike = p_tail7.addOrReplaceChild("tail7_spike", CubeListBuilder.create()
                .texOffs(110, 384).addBox(-0.92F, -1.036F, -1.15F, 1.84F, 1.636F, 2.3F)
                .texOffs(18, 395).addBox(-0.598F, -1.993F, -0.7475F, 1.196F, 1.207F, 1.495F)
                .texOffs(446, 395).addBox(-0.276F, -2.95F, -0.345F, 0.552F, 1.207F, 0.69F),
                PartPose.offsetAndRotation(0F, -2.5F, 4.275F, -0.5934F, 0F, 0F));
        PartDefinition p_tail6_spike = p_tail6.addOrReplaceChild("tail6_spike", CubeListBuilder.create()
                .texOffs(121, 384).addBox(-1F, -1.225F, -1.25F, 2F, 1.825F, 2.5F)
                .texOffs(27, 395).addBox(-0.65F, -2.3125F, -0.8125F, 1.3F, 1.3375F, 1.625F)
                .texOffs(451, 395).addBox(-0.3F, -3.4F, -0.375F, 0.6F, 1.3375F, 0.75F),
                PartPose.offsetAndRotation(0F, -2.9F, 4.5F, -0.5934F, 0F, 0F));
        PartDefinition p_tail5_spike = p_tail5.addOrReplaceChild("tail5_spike", CubeListBuilder.create()
                .texOffs(0, 377).addBox(-1.08F, -1.414F, -1.35F, 2.16F, 2.014F, 2.7F)
                .texOffs(36, 395).addBox(-0.702F, -2.632F, -0.8775F, 1.404F, 1.468F, 1.755F)
                .texOffs(456, 395).addBox(-0.324F, -3.85F, -0.405F, 0.648F, 1.468F, 0.81F),
                PartPose.offsetAndRotation(0F, -3.4F, 4.725F, -0.5934F, 0F, 0F));
        PartDefinition p_tail4_spike = p_tail4.addOrReplaceChild("tail4_spike", CubeListBuilder.create()
                .texOffs(13, 377).addBox(-1.16F, -1.603F, -1.45F, 2.32F, 2.203F, 2.9F)
                .texOffs(45, 395).addBox(-0.754F, -2.9515F, -0.9425F, 1.508F, 1.5985F, 1.885F)
                .texOffs(461, 395).addBox(-0.348F, -4.3F, -0.435F, 0.696F, 1.5985F, 0.87F),
                PartPose.offsetAndRotation(0F, -3.9F, 4.95F, -0.5934F, 0F, 0F));
        PartDefinition p_tail3_spike = p_tail3.addOrReplaceChild("tail3_spike", CubeListBuilder.create()
                .texOffs(292, 353).addBox(-1.24F, -1.792F, -1.55F, 2.48F, 2.392F, 3.1F)
                .texOffs(132, 384).addBox(-0.806F, -3.271F, -1.0075F, 1.612F, 1.729F, 2.015F)
                .texOffs(466, 395).addBox(-0.372F, -4.75F, -0.465F, 0.744F, 1.729F, 0.93F),
                PartPose.offsetAndRotation(0F, -4.5F, 5.175F, -0.5934F, 0F, 0F));
        PartDefinition p_tail2_spike = p_tail2.addOrReplaceChild("tail2_spike", CubeListBuilder.create()
                .texOffs(307, 353).addBox(-1.32F, -1.981F, -1.65F, 2.64F, 2.581F, 3.3F)
                .texOffs(143, 384).addBox(-0.858F, -3.5905F, -1.0725F, 1.716F, 1.8595F, 2.145F)
                .texOffs(471, 395).addBox(-0.396F, -5.2F, -0.495F, 0.792F, 1.8595F, 0.99F),
                PartPose.offsetAndRotation(0F, -5.1F, 5.4F, -0.5934F, 0F, 0F));
        PartDefinition p_tail1_spike = p_tail1.addOrReplaceChild("tail1_spike", CubeListBuilder.create()
                .texOffs(322, 353).addBox(-1.4F, -2.17F, -1.75F, 2.8F, 2.77F, 3.5F)
                .texOffs(154, 384).addBox(-0.91F, -3.91F, -1.1375F, 1.82F, 1.99F, 2.275F)
                .texOffs(168, 395).addBox(-0.42F, -5.65F, -0.525F, 0.84F, 1.99F, 1.05F),
                PartPose.offsetAndRotation(0F, -5.85F, 5.625F, -0.5934F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(345, 169).addBox(-4.75F, -3.8F, -2.66F, 9.5F, 17.8F, 5.32F)
                .texOffs(86, 138).addBox(-2.66F, -3.72F, -4.75F, 5.32F, 17.64F, 9.5F)
                .texOffs(226, 314).addBox(-5.35F, -5.225F, -2.5F, 10.7F, 7.125F, 5F)
                .texOffs(206, 242).addBox(-2.5F, -5.145F, -5.35F, 5F, 6.965F, 10.7F),
                PartPose.offsetAndRotation(10.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(60, 242).addBox(-3.5F, -1.4F, -1.96F, 7F, 14.1F, 3.92F)
                .texOffs(91, 219).addBox(-1.96F, -1.32F, -3.5F, 3.92F, 13.94F, 7F),
                PartPose.offsetAndRotation(0F, 13.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_shin_l_spike = p_front_shin_l.addOrReplaceChild("front_shin_l_spike", CubeListBuilder.create()
                .texOffs(54, 395).addBox(-0.8F, -1.162F, -1F, 1.6F, 1.762F, 2F)
                .texOffs(63, 395).addBox(-0.52F, -2.206F, -0.65F, 1.04F, 1.294F, 1.3F)
                .texOffs(476, 395).addBox(-0.25F, -3.25F, -0.3F, 0.5F, 1.294F, 0.6F),
                PartPose.offsetAndRotation(0F, 0.5F, -3.5F, -1.9199F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(205, 298).addBox(-4.5F, 1.02F, -8.4F, 9F, 1.36F, 12F)
                .texOffs(153, 280).addBox(-3.48F, 0F, -8.32F, 6.96F, 3.4F, 11.84F)
                .texOffs(337, 353).addBox(-4.15F, 1.5F, -11.8F, 1.9F, 1.9F, 4.5F)
                .texOffs(26, 377).addBox(-3.865F, 1.975F, -14.8F, 1.33F, 1.71F, 3.3F)
                .texOffs(352, 353).addBox(-0.95F, 1.5F, -11.8F, 1.9F, 1.9F, 4.5F)
                .texOffs(39, 377).addBox(-0.665F, 1.975F, -14.8F, 1.33F, 1.71F, 3.3F)
                .texOffs(367, 353).addBox(2.25F, 1.5F, -11.8F, 1.9F, 1.9F, 4.5F)
                .texOffs(52, 377).addBox(2.535F, 1.975F, -14.8F, 1.33F, 1.71F, 3.3F),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(0, 138).addBox(-5.75F, -4.6F, -4.22F, 11.5F, 20.1F, 8.44F)
                .texOffs(258, 103).addBox(-3.22F, -4.52F, -6.75F, 6.44F, 19.94F, 13.5F)
                .texOffs(274, 262).addBox(-6.35F, -6.325F, -3.9F, 12.7F, 8.625F, 7.8F)
                .texOffs(259, 169).addBox(-2.9F, -6.245F, -7.35F, 5.8F, 8.465F, 14.7F),
                PartPose.offsetAndRotation(11F, 2F, 15F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(284, 219).addBox(-4F, -1.4F, -2.24F, 8F, 14.6F, 4.48F)
                .texOffs(88, 195).addBox(-2.24F, -1.32F, -4F, 4.48F, 14.44F, 8F),
                PartPose.offsetAndRotation(0F, 14.9F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_shin_l_spike = p_hind_shin_l.addOrReplaceChild("hind_shin_l_spike", CubeListBuilder.create()
                .texOffs(72, 395).addBox(-0.8F, -1.33F, -1F, 1.6F, 1.93F, 2F)
                .texOffs(81, 395).addBox(-0.52F, -2.49F, -0.65F, 1.04F, 1.41F, 1.3F)
                .texOffs(481, 395).addBox(-0.25F, -3.65F, -0.3F, 0.5F, 1.41F, 0.6F),
                PartPose.offsetAndRotation(0F, 0.5F, 4F, 1.9199F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(307, 280).addBox(-5F, 1.02F, -9.1F, 10F, 1.36F, 13F)
                .texOffs(317, 262).addBox(-3.98F, 0F, -9.02F, 7.96F, 3.4F, 12.84F)
                .texOffs(382, 353).addBox(-4.4833F, 1.5F, -12.9F, 1.9F, 1.9F, 4.9F)
                .texOffs(65, 377).addBox(-4.1983F, 1.975F, -16.1F, 1.33F, 1.71F, 3.5F)
                .texOffs(397, 353).addBox(-0.95F, 1.5F, -12.9F, 1.9F, 1.9F, 4.9F)
                .texOffs(78, 377).addBox(-0.665F, 1.975F, -16.1F, 1.33F, 1.71F, 3.5F)
                .texOffs(412, 353).addBox(2.5833F, 1.5F, -12.9F, 1.9F, 1.9F, 4.9F)
                .texOffs(91, 377).addBox(2.8683F, 1.975F, -16.1F, 1.33F, 1.71F, 3.5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(378, 169).addBox(-4.75F, -3.8F, -2.66F, 9.5F, 17.8F, 5.32F)
                .texOffs(119, 138).addBox(-2.66F, -3.72F, -4.75F, 5.32F, 17.64F, 9.5F)
                .texOffs(259, 314).addBox(-5.35F, -5.225F, -2.5F, 10.7F, 7.125F, 5F)
                .texOffs(239, 242).addBox(-2.5F, -5.145F, -5.35F, 5F, 6.965F, 10.7F),
                PartPose.offsetAndRotation(-10.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(83, 242).addBox(-3.5F, -1.4F, -1.96F, 7F, 14.1F, 3.92F)
                .texOffs(114, 219).addBox(-1.96F, -1.32F, -3.5F, 3.92F, 13.94F, 7F),
                PartPose.offsetAndRotation(0F, 13.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_shin_r_spike = p_front_shin_r.addOrReplaceChild("front_shin_r_spike", CubeListBuilder.create()
                .texOffs(90, 395).addBox(-0.8F, -1.162F, -1F, 1.6F, 1.762F, 2F)
                .texOffs(99, 395).addBox(-0.52F, -2.206F, -0.65F, 1.04F, 1.294F, 1.3F)
                .texOffs(486, 395).addBox(-0.25F, -3.25F, -0.3F, 0.5F, 1.294F, 0.6F),
                PartPose.offsetAndRotation(0F, 0.5F, -3.5F, -1.9199F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(248, 298).addBox(-4.5F, 1.02F, -8.4F, 9F, 1.36F, 12F)
                .texOffs(192, 280).addBox(-3.48F, 0F, -8.32F, 6.96F, 3.4F, 11.84F)
                .texOffs(427, 353).addBox(-4.15F, 1.5F, -11.8F, 1.9F, 1.9F, 4.5F)
                .texOffs(104, 377).addBox(-3.865F, 1.975F, -14.8F, 1.33F, 1.71F, 3.3F)
                .texOffs(442, 353).addBox(-0.95F, 1.5F, -11.8F, 1.9F, 1.9F, 4.5F)
                .texOffs(117, 377).addBox(-0.665F, 1.975F, -14.8F, 1.33F, 1.71F, 3.3F)
                .texOffs(457, 353).addBox(2.25F, 1.5F, -11.8F, 1.9F, 1.9F, 4.5F)
                .texOffs(130, 377).addBox(2.535F, 1.975F, -14.8F, 1.33F, 1.71F, 3.3F),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(43, 138).addBox(-5.75F, -4.6F, -4.22F, 11.5F, 20.1F, 8.44F)
                .texOffs(301, 103).addBox(-3.22F, -4.52F, -6.75F, 6.44F, 19.94F, 13.5F)
                .texOffs(360, 262).addBox(-6.35F, -6.325F, -3.9F, 12.7F, 8.625F, 7.8F)
                .texOffs(302, 169).addBox(-2.9F, -6.245F, -7.35F, 5.8F, 8.465F, 14.7F),
                PartPose.offsetAndRotation(-11F, 2F, 15F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(311, 219).addBox(-4F, -1.4F, -2.24F, 8F, 14.6F, 4.48F)
                .texOffs(115, 195).addBox(-2.24F, -1.32F, -4F, 4.48F, 14.44F, 8F),
                PartPose.offsetAndRotation(0F, 14.9F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_shin_r_spike = p_hind_shin_r.addOrReplaceChild("hind_shin_r_spike", CubeListBuilder.create()
                .texOffs(108, 395).addBox(-0.8F, -1.33F, -1F, 1.6F, 1.93F, 2F)
                .texOffs(117, 395).addBox(-0.52F, -2.49F, -0.65F, 1.04F, 1.41F, 1.3F)
                .texOffs(491, 395).addBox(-0.25F, -3.65F, -0.3F, 0.5F, 1.41F, 0.6F),
                PartPose.offsetAndRotation(0F, 0.5F, 4F, 1.9199F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(354, 280).addBox(-5F, 1.02F, -9.1F, 10F, 1.36F, 13F)
                .texOffs(403, 262).addBox(-3.98F, 0F, -9.02F, 7.96F, 3.4F, 12.84F)
                .texOffs(472, 353).addBox(-4.4833F, 1.5F, -12.9F, 1.9F, 1.9F, 4.9F)
                .texOffs(143, 377).addBox(-4.1983F, 1.975F, -16.1F, 1.33F, 1.71F, 3.5F)
                .texOffs(487, 353).addBox(-0.95F, 1.5F, -12.9F, 1.9F, 1.9F, 4.9F)
                .texOffs(156, 377).addBox(-0.665F, 1.975F, -16.1F, 1.33F, 1.71F, 3.5F)
                .texOffs(0, 362).addBox(2.5833F, 1.5F, -12.9F, 1.9F, 1.9F, 4.9F)
                .texOffs(169, 377).addBox(2.8683F, 1.975F, -16.1F, 1.33F, 1.71F, 3.5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(216, 342).addBox(-1F, -2.7F, -1.188F, 17.5F, 5.4F, 2.376F)
                .texOffs(118, 342).addBox(-0.92F, -1.188F, -2.7F, 17.34F, 2.376F, 5.4F)
                .texOffs(82, 370).addBox(15F, -1.76F, -0.7744F, 15.8F, 3.52F, 1.5488F)
                .texOffs(416, 362).addBox(15.08F, -0.7744F, -1.76F, 15.64F, 1.5488F, 3.52F)
                .texOffs(0, 103).addBox(0F, 0F, 1.26F, 30F, 0F, 34F),
                PartPose.offset(10.5F, -11F, -13F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(457, 362).addBox(-0.6F, -1.716F, -0.755F, 17.6F, 3.432F, 1.5101F)
                .texOffs(326, 362).addBox(-0.52F, -0.755F, -1.716F, 17.44F, 1.5101F, 3.432F)
                .texOffs(347, 377).addBox(15.3F, -1.21F, -0.5324F, 19.3F, 2.42F, 1.0648F)
                .texOffs(253, 377).addBox(15.38F, -0.5324F, -1.21F, 19.14F, 1.0648F, 2.42F)
                .texOffs(220, 64).addBox(0F, 0F, 0.71F, 34F, 0F, 35F),
                PartPose.offset(30F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(167, 353).addBox(-2.09F, -2.09F, -1.045F, 4.18F, 4.18F, 2.09F)
                .texOffs(23, 353).addBox(-2.01F, -1.045F, -2.09F, 4.02F, 2.09F, 4.18F)
                .texOffs(182, 377).addBox(-0.9F, -0.9F, -5.29F, 1.8F, 1.8F, 3.4F)
                .texOffs(165, 384).addBox(-0.6F, -0.5F, -7.49F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(34F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(54, 362).addBox(0F, -1.2F, -1.2F, 31.2F, 2.4F, 2.4F)
                .texOffs(232, 384).addBox(30F, -0.72F, -0.72F, 30F, 1.44F, 1.44F)
                .texOffs(278, 138).addBox(0F, 0F, 0.42F, 60F, 0F, 25F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1047F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(196, 362).addBox(0F, -1.05F, -1.05F, 28.6F, 2.1F, 2.1F)
                .texOffs(362, 384).addBox(27.5F, -0.63F, -0.63F, 27.5F, 1.26F, 1.26F)
                .texOffs(142, 195).addBox(0F, 0F, 0.33F, 55F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5411F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(0, 390).addBox(0F, -0.9F, -0.9F, 24.96F, 1.8F, 1.8F)
                .texOffs(110, 390).addBox(24F, -0.54F, -0.54F, 24F, 1.08F, 1.08F)
                .texOffs(380, 242).addBox(0F, 0F, 0.24F, 48F, 0F, 17F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9774F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(216, 390).addBox(0F, -0.75F, -0.75F, 20.28F, 1.5F, 1.5F)
                .texOffs(0, 400).addBox(19.5F, -0.45F, -0.45F, 19.5F, 0.9F, 0.9F)
                .texOffs(351, 0).addBox(0F, 0F, 0.15F, 39F, 0F, 38F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.3963F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(259, 342).addBox(-16.5F, -2.7F, -1.188F, 17.5F, 5.4F, 2.376F)
                .texOffs(167, 342).addBox(-16.42F, -1.188F, -2.7F, 17.34F, 2.376F, 5.4F)
                .texOffs(119, 370).addBox(-30.8F, -1.76F, -0.7744F, 15.8F, 3.52F, 1.5488F)
                .texOffs(0, 370).addBox(-30.72F, -0.7744F, -1.76F, 15.64F, 1.5488F, 3.52F)
                .texOffs(129, 103).addBox(-30F, 0F, 1.26F, 30F, 0F, 34F),
                PartPose.offset(-10.5F, -11F, -13F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(41, 370).addBox(-17F, -1.716F, -0.755F, 17.6F, 3.432F, 1.5101F)
                .texOffs(371, 362).addBox(-16.92F, -0.755F, -1.716F, 17.44F, 1.5101F, 3.432F)
                .texOffs(392, 377).addBox(-34.6F, -1.21F, -0.5324F, 19.3F, 2.42F, 1.0648F)
                .texOffs(300, 377).addBox(-34.52F, -0.5324F, -1.21F, 19.14F, 1.0648F, 2.42F)
                .texOffs(359, 64).addBox(-34F, 0F, 0.71F, 34F, 0F, 35F),
                PartPose.offset(-30F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(184, 353).addBox(-2.09F, -2.09F, -1.045F, 4.18F, 4.18F, 2.09F)
                .texOffs(44, 353).addBox(-2.01F, -1.045F, -2.09F, 4.02F, 2.09F, 4.18F)
                .texOffs(195, 377).addBox(-0.9F, -0.9F, -5.29F, 1.8F, 1.8F, 3.4F)
                .texOffs(176, 384).addBox(-0.6F, -0.5F, -7.49F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(-34F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(125, 362).addBox(-31.2F, -1.2F, -1.2F, 31.2F, 2.4F, 2.4F)
                .texOffs(297, 384).addBox(-60F, -0.72F, -0.72F, 30F, 1.44F, 1.44F)
                .texOffs(0, 169).addBox(-60F, 0F, 0.42F, 60F, 0F, 25F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1047F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(261, 362).addBox(-28.6F, -1.05F, -1.05F, 28.6F, 2.1F, 2.1F)
                .texOffs(423, 384).addBox(-55F, -0.63F, -0.63F, 27.5F, 1.26F, 1.26F)
                .texOffs(297, 195).addBox(-55F, 0F, 0.33F, 55F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5411F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(55, 390).addBox(-24.96F, -0.9F, -0.9F, 24.96F, 1.8F, 1.8F)
                .texOffs(163, 390).addBox(-48F, -0.54F, -0.54F, 24F, 1.08F, 1.08F)
                .texOffs(0, 262).addBox(-48F, 0F, 0.24F, 48F, 0F, 17F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9774F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(263, 390).addBox(-20.28F, -0.75F, -0.75F, 20.28F, 1.5F, 1.5F)
                .texOffs(43, 400).addBox(-39F, -0.45F, -0.45F, 19.5F, 0.9F, 0.9F)
                .texOffs(0, 64).addBox(-39F, 0F, 0.15F, 39F, 0F, 38F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.3963F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
