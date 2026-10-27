package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Pyrrhon, dem Feuerdrachen-Boss (Grundgröße, im Spiel ×2,1).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PyrrhonGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private PyrrhonGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(117, 0).addBox(-12.5F, -7.5F, -21F, 25F, 11F, 42F)
                .texOffs(0, 0).addBox(-8F, -12F, -20.92F, 16F, 20F, 41.84F)
                .texOffs(240, 136).addBox(-14F, -8F, -23F, 28F, 12F, 16F)
                .texOffs(349, 0).addBox(-9F, -13F, -22.92F, 18F, 22F, 15.84F)
                .texOffs(131, 170).addBox(-13.2F, -8F, 7F, 26.4F, 10.5F, 13F)
                .texOffs(0, 136).addBox(-8.7F, -12.5F, 7.08F, 17.4F, 19.5F, 12.84F)
                .texOffs(252, 0).addBox(-6F, -15F, -20F, 12F, 4F, 36F)
                .texOffs(449, 298).addBox(9F, -12.5F, -17F, 6F, 3F, 11F)
                .texOffs(0, 262).addBox(11F, -14.5F, -16.92F, 2F, 7F, 10.84F)
                .texOffs(0, 314).addBox(-15F, -12.5F, -17F, 6F, 3F, 11F)
                .texOffs(27, 262).addBox(-13F, -14.5F, -16.92F, 2F, 7F, 10.84F)
                .texOffs(275, 242).addBox(-9.5F, 8F, -22F, 19F, 2.2F, 15F)
                .texOffs(63, 136).addBox(-9F, 6.8F, -7F, 18F, 2F, 27F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_back_spike1 = p_body.addOrReplaceChild("back_spike1", CubeListBuilder.create()
                .texOffs(264, 342).addBox(-1.8F, -3.052F, -2.25F, 3.6F, 3.652F, 4.5F)
                .texOffs(445, 360).addBox(-1.17F, -5.401F, -1.4625F, 2.34F, 2.599F, 2.925F)
                .texOffs(275, 374).addBox(-0.54F, -7.75F, -0.675F, 1.08F, 2.599F, 1.35F),
                PartPose.offsetAndRotation(0F, -14.6F, -17F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike2 = p_body.addOrReplaceChild("back_spike2", CubeListBuilder.create()
                .texOffs(370, 329).addBox(-1.8F, -3.892F, -2.25F, 3.6F, 4.492F, 4.5F)
                .texOffs(343, 352).addBox(-1.17F, -6.821F, -1.4625F, 2.34F, 3.179F, 2.925F)
                .texOffs(169, 367).addBox(-0.54F, -9.75F, -0.675F, 1.08F, 3.179F, 1.35F),
                PartPose.offsetAndRotation(0F, -14.6F, -9F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike3 = p_body.addOrReplaceChild("back_spike3", CubeListBuilder.create()
                .texOffs(389, 329).addBox(-1.8F, -4.102F, -2.25F, 3.6F, 4.702F, 4.5F)
                .texOffs(356, 352).addBox(-1.17F, -7.176F, -1.4625F, 2.34F, 3.324F, 2.925F)
                .texOffs(178, 367).addBox(-0.54F, -10.25F, -0.675F, 1.08F, 3.324F, 1.35F),
                PartPose.offsetAndRotation(0F, -14.6F, -1F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike4 = p_body.addOrReplaceChild("back_spike4", CubeListBuilder.create()
                .texOffs(408, 329).addBox(-1.8F, -3.472F, -2.25F, 3.6F, 4.072F, 4.5F)
                .texOffs(458, 360).addBox(-1.17F, -6.111F, -1.4625F, 2.34F, 2.889F, 2.925F)
                .texOffs(284, 374).addBox(-0.54F, -8.75F, -0.675F, 1.08F, 2.889F, 1.35F),
                PartPose.offsetAndRotation(0F, -14.6F, 7F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike5 = p_body.addOrReplaceChild("back_spike5", CubeListBuilder.create()
                .texOffs(283, 342).addBox(-1.8F, -2.632F, -2.25F, 3.6F, 3.232F, 4.5F)
                .texOffs(471, 360).addBox(-1.17F, -4.691F, -1.4625F, 2.34F, 2.309F, 2.925F)
                .texOffs(293, 374).addBox(-0.54F, -6.75F, -0.675F, 1.08F, 2.309F, 1.35F),
                PartPose.offsetAndRotation(0F, -14.6F, 15F, -0.5236F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(121, 281).addBox(-7.25F, -2.8F, -8.6F, 14.5F, 5.6F, 9.2F)
                .texOffs(251, 170).addBox(-3.05F, -7F, -8.52F, 6.1F, 14F, 9.04F)
                .texOffs(220, 329).addBox(-3.05F, 6F, -8F, 6.1F, 1.6F, 8F),
                PartPose.offsetAndRotation(0F, -6F, -21F, -0.5236F, 0F, 0F));
        PartDefinition p_neck1_spike = p_neck1.addOrReplaceChild("neck1_spike", CubeListBuilder.create()
                .texOffs(52, 352).addBox(-1.6F, -1.834F, -2F, 3.2F, 2.434F, 4F)
                .texOffs(363, 367).addBox(-1.04F, -3.342F, -1.3F, 2.08F, 1.758F, 2.6F)
                .texOffs(503, 380).addBox(-0.48F, -4.85F, -0.6F, 0.96F, 1.758F, 1.2F),
                PartPose.offsetAndRotation(0F, -6.6F, -3.6F, -0.5585F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(453, 281).addBox(-6.75F, -2.6F, -8.1F, 13.5F, 5.2F, 8.7F)
                .texOffs(343, 197).addBox(-2.85F, -6.5F, -8.02F, 5.7F, 13F, 8.54F)
                .texOffs(251, 329).addBox(-2.85F, 5.5F, -7.5F, 5.7F, 1.6F, 7.5F),
                PartPose.offsetAndRotation(0F, 0F, -8F, -0.2094F, 0F, 0F));
        PartDefinition p_neck2_spike = p_neck2.addOrReplaceChild("neck2_spike", CubeListBuilder.create()
                .texOffs(69, 352).addBox(-1.6F, -1.708F, -2F, 3.2F, 2.308F, 4F)
                .texOffs(376, 367).addBox(-1.04F, -3.129F, -1.3F, 2.08F, 1.671F, 2.6F)
                .texOffs(0, 385).addBox(-0.48F, -4.55F, -0.6F, 0.96F, 1.671F, 1.2F),
                PartPose.offsetAndRotation(0F, -6.1F, -3.375F, -0.5585F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(164, 298).addBox(-6.25F, -2.4F, -7.6F, 12.5F, 4.8F, 8.2F)
                .texOffs(106, 220).addBox(-2.65F, -6F, -7.52F, 5.3F, 12F, 8.04F)
                .texOffs(187, 342).addBox(-2.65F, 5F, -7F, 5.3F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.0349F, 0F, 0F));
        PartDefinition p_neck3_spike = p_neck3.addOrReplaceChild("neck3_spike", CubeListBuilder.create()
                .texOffs(103, 352).addBox(-1.5F, -1.582F, -1.875F, 3F, 2.182F, 3.75F)
                .texOffs(454, 367).addBox(-0.975F, -2.916F, -1.2188F, 1.95F, 1.584F, 2.4375F)
                .texOffs(7, 385).addBox(-0.45F, -4.25F, -0.5625F, 0.9F, 1.584F, 1.125F),
                PartPose.offsetAndRotation(0F, -5.6F, -3.15F, -0.5585F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(116, 314).addBox(-5.75F, -2.2F, -7.1F, 11.5F, 4.4F, 7.7F)
                .texOffs(194, 242).addBox(-2.45F, -5.5F, -7.02F, 4.9F, 11F, 7.54F)
                .texOffs(214, 342).addBox(-2.45F, 4.5F, -6.5F, 4.9F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.1047F, 0F, 0F));
        PartDefinition p_neck4_spike = p_neck4.addOrReplaceChild("neck4_spike", CubeListBuilder.create()
                .texOffs(118, 352).addBox(-1.38F, -1.456F, -1.725F, 2.76F, 2.056F, 3.45F)
                .texOffs(465, 367).addBox(-0.897F, -2.703F, -1.1212F, 1.794F, 1.497F, 2.2425F)
                .texOffs(14, 385).addBox(-0.414F, -3.95F, -0.5175F, 0.828F, 1.497F, 1.035F),
                PartPose.offsetAndRotation(0F, -5.1F, -2.925F, -0.5585F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(411, 314).addBox(-5.25F, -2F, -7.1F, 10.5F, 4F, 7.7F)
                .texOffs(54, 262).addBox(-2.25F, -5F, -7.02F, 4.5F, 10F, 7.54F)
                .texOffs(239, 342).addBox(-2.25F, 4F, -6.5F, 4.5F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1571F, 0F, 0F));
        PartDefinition p_neck5_spike = p_neck5.addOrReplaceChild("neck5_spike", CubeListBuilder.create()
                .texOffs(430, 360).addBox(-1.26F, -1.33F, -1.575F, 2.52F, 1.93F, 3.15F)
                .texOffs(476, 367).addBox(-0.819F, -2.49F, -1.0237F, 1.638F, 1.41F, 2.0475F)
                .texOffs(111, 385).addBox(-0.378F, -3.65F, -0.4725F, 0.756F, 1.41F, 0.945F),
                PartPose.offsetAndRotation(0F, -4.6F, -2.925F, -0.5585F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(450, 314).addBox(-5F, -1.9F, -6.6F, 10F, 3.8F, 7.2F)
                .texOffs(81, 262).addBox(-2.15F, -4.75F, -6.52F, 4.3F, 9.5F, 7.04F)
                .texOffs(340, 342).addBox(-2.15F, 3.75F, -6F, 4.3F, 1.6F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1396F, 0F, 0F));
        PartDefinition p_neck6_spike = p_neck6.addOrReplaceChild("neck6_spike", CubeListBuilder.create()
                .texOffs(389, 367).addBox(-1.2F, -1.162F, -1.5F, 2.4F, 1.762F, 3F)
                .texOffs(341, 380).addBox(-0.78F, -2.206F, -0.975F, 1.56F, 1.294F, 1.95F)
                .texOffs(116, 385).addBox(-0.36F, -3.25F, -0.45F, 0.72F, 1.294F, 0.9F),
                PartPose.offsetAndRotation(0F, -4.35F, -2.7F, -0.5585F, 0F, 0F));
        PartDefinition p_head = p_neck6.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 220).addBox(-6.5F, -3.6F, -13F, 13F, 5.3F, 14.5F)
                .texOffs(440, 136).addBox(-3.9F, -6.2F, -12.92F, 7.8F, 10.5F, 14.34F)
                .texOffs(473, 342).addBox(-5.2F, -4.8F, -16F, 10.4F, 0.6F, 6F)
                .texOffs(129, 342).addBox(-4F, -6F, -15.92F, 8F, 3F, 5.84F)
                .texOffs(299, 298).addBox(-4.9F, -2.4F, -21F, 9.8F, 4.4F, 9F)
                .texOffs(448, 242).addBox(-2.9F, -4.4F, -20.92F, 5.8F, 8.4F, 8.84F)
                .texOffs(194, 314).addBox(-4.4F, -2.1F, -28F, 8.8F, 4.3F, 8F)
                .texOffs(297, 281).addBox(-2.6F, -3.9F, -27.92F, 5.2F, 7.9F, 7.84F)
                .texOffs(86, 298).addBox(-2.8F, -5.4F, -26.5F, 5.6F, 1.8F, 13F)
                .texOffs(33, 352).addBox(-3.4F, -3.5F, -28.9F, 6.8F, 4.2F, 1.2F)
                .texOffs(394, 385).addBox(1.4F, -3.2F, -29.1F, 1.4F, 0.9F, 0.6F)
                .texOffs(153, 329).addBox(5F, 0.1F, -14F, 2F, 3.6F, 8F)
                .texOffs(328, 314).addBox(5.7F, -0.6F, -13.92F, 0.6F, 5F, 7.84F)
                .texOffs(484, 360).addBox(4.9F, -3.9F, -13.4F, 1.85F, 2F, 3.8F)
                .texOffs(77, 385).addBox(3.55F, 4F, -26.45F, 0.9F, 2.025F, 0.9F)
                .texOffs(121, 385).addBox(3.55F, 4F, -24.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(126, 385).addBox(3.55F, 4F, -22.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(131, 385).addBox(3.55F, 4F, -20.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(136, 385).addBox(3.55F, 4F, -18.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(82, 385).addBox(3.55F, 4F, -16.45F, 0.9F, 2.025F, 0.9F)
                .texOffs(302, 374).addBox(3F, 4F, -26.2F, 1.2F, 3F, 1.2F)
                .texOffs(401, 385).addBox(-2.8F, -3.2F, -29.1F, 1.4F, 0.9F, 0.6F)
                .texOffs(174, 329).addBox(-7F, 0.1F, -14F, 2F, 3.6F, 8F)
                .texOffs(347, 314).addBox(-6.3F, -0.6F, -13.92F, 0.6F, 5F, 7.84F)
                .texOffs(497, 360).addBox(-6.75F, -3.9F, -13.4F, 1.85F, 2F, 3.8F)
                .texOffs(87, 385).addBox(-4.45F, 4F, -26.45F, 0.9F, 2.025F, 0.9F)
                .texOffs(141, 385).addBox(-4.45F, 4F, -24.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(146, 385).addBox(-4.45F, 4F, -22.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(151, 385).addBox(-4.45F, 4F, -20.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(156, 385).addBox(-4.45F, 4F, -18.45F, 0.9F, 1.5F, 0.9F)
                .texOffs(92, 385).addBox(-4.45F, 4F, -16.45F, 0.9F, 2.025F, 0.9F)
                .texOffs(311, 374).addBox(-4.2F, 4F, -26.2F, 1.2F, 3F, 1.2F)
                .texOffs(161, 385).addBox(-2.6F, 4F, -27.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(166, 385).addBox(-1.15F, 4F, -27.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(171, 385).addBox(0.35F, 4F, -27.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(176, 385).addBox(1.8F, 4F, -27.8F, 0.8F, 1.2F, 0.8F)
                .texOffs(0, 298).addBox(-3.6F, 4.02F, -27.2F, 7.2F, 0.3F, 13.5F),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.5236F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(68, 329).addBox(-2.4F, -1.4F, -0.6F, 4.8F, 2.6F, 9F),
                PartPose.offsetAndRotation(4.4F, -5.4F, -12.8F, 0.1047F, 0F, -0.1745F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(97, 329).addBox(-2.4F, -1.4F, -0.6F, 4.8F, 2.6F, 9F),
                PartPose.offsetAndRotation(-4.4F, -5.4F, -12.8F, 0.1047F, 0F, 0.1745F));
        PartDefinition p_crest1 = p_head.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(402, 367).addBox(-1.1F, -0.994F, -1.375F, 2.2F, 1.594F, 2.75F)
                .texOffs(350, 380).addBox(-0.715F, -1.922F, -0.8938F, 1.43F, 1.178F, 1.7875F)
                .texOffs(181, 385).addBox(-0.33F, -2.85F, -0.4125F, 0.66F, 1.178F, 0.825F),
                PartPose.offsetAndRotation(0F, -6F, -9F, -0.9599F, 0F, 0F));
        PartDefinition p_crest2 = p_head.addOrReplaceChild("crest2", CubeListBuilder.create()
                .texOffs(415, 367).addBox(-1.1F, -1.246F, -1.375F, 2.2F, 1.846F, 2.75F)
                .texOffs(359, 380).addBox(-0.715F, -2.348F, -0.8938F, 1.43F, 1.352F, 1.7875F)
                .texOffs(186, 385).addBox(-0.33F, -3.45F, -0.4125F, 0.66F, 1.352F, 0.825F),
                PartPose.offsetAndRotation(0F, -6F, -4.5F, -0.9599F, 0F, 0F));
        PartDefinition p_crest3 = p_head.addOrReplaceChild("crest3", CubeListBuilder.create()
                .texOffs(428, 367).addBox(-1.1F, -1.078F, -1.375F, 2.2F, 1.678F, 2.75F)
                .texOffs(368, 380).addBox(-0.715F, -2.064F, -0.8938F, 1.43F, 1.236F, 1.7875F)
                .texOffs(191, 385).addBox(-0.33F, -3.05F, -0.4125F, 0.66F, 1.236F, 0.825F),
                PartPose.offsetAndRotation(0F, -6F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(307, 329).addBox(-2.2F, -2.2F, -0.4F, 4.4F, 4.4F, 4.8F)
                .texOffs(405, 342).addBox(-1.364F, -1.364F, 4F, 2.728F, 2.728F, 4.25F),
                PartPose.offsetAndRotation(4.6F, -5.6F, -3.5F, 0.4538F, 0.4189F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(302, 342).addBox(-1.7F, -1.7F, -0.4F, 3.4F, 3.4F, 4.525F)
                .texOffs(133, 352).addBox(-1.054F, -1.054F, 3.75F, 2.108F, 2.108F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.1571F, 0.1047F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(148, 352).addBox(-1.3F, -1.3F, -0.4F, 2.6F, 2.6F, 3.975F)
                .texOffs(0, 367).addBox(-0.806F, -0.806F, 3.25F, 1.612F, 1.612F, 3.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.1047F, 0.0349F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(13, 367).addBox(-0.9F, -0.9F, -0.4F, 1.8F, 1.8F, 3.425F)
                .texOffs(487, 367).addBox(-0.558F, -0.558F, 2.75F, 1.116F, 1.116F, 3F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.1745F, -0.0698F, 0F));
        PartDefinition p_horn_l_low1 = p_head.addOrReplaceChild("horn_l_low1", CubeListBuilder.create()
                .texOffs(163, 352).addBox(-1.3F, -1.3F, -0.4F, 2.6F, 2.6F, 3.425F)
                .texOffs(498, 367).addBox(-0.806F, -0.806F, 2.75F, 1.612F, 1.612F, 3F),
                PartPose.offsetAndRotation(6F, -2.6F, -2.5F, 0.1047F, 0.733F, 0F));
        PartDefinition p_horn_l_low2 = p_horn_l_low1.addOrReplaceChild("horn_l_low2", CubeListBuilder.create()
                .texOffs(0, 374).addBox(-0.95F, -0.95F, -0.4F, 1.9F, 1.9F, 2.875F)
                .texOffs(11, 374).addBox(-0.589F, -0.589F, 2.25F, 1.178F, 1.178F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, 0.1047F, 0F));
        PartDefinition p_horn_l_low3 = p_horn_l_low2.addOrReplaceChild("horn_l_low3", CubeListBuilder.create()
                .texOffs(22, 374).addBox(-0.65F, -0.65F, -0.4F, 1.3F, 1.3F, 2.325F)
                .texOffs(97, 385).addBox(-0.403F, -0.403F, 1.75F, 0.806F, 0.806F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(377, 380).addBox(-0.8F, -1.33F, -1F, 1.6F, 1.93F, 2F)
                .texOffs(386, 380).addBox(-0.52F, -2.49F, -0.65F, 1.04F, 1.41F, 1.3F)
                .texOffs(196, 385).addBox(-0.25F, -3.65F, -0.3F, 0.5F, 1.41F, 0.6F),
                PartPose.offsetAndRotation(7F, 1.5F, -7F, -1.7453F, 0.5236F, 0F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(395, 380).addBox(-0.7F, -1.078F, -0.875F, 1.4F, 1.678F, 1.75F)
                .texOffs(21, 385).addBox(-0.455F, -2.064F, -0.5687F, 0.91F, 1.236F, 1.1375F)
                .texOffs(201, 385).addBox(-0.25F, -3.05F, -0.2625F, 0.5F, 1.236F, 0.525F),
                PartPose.offsetAndRotation(6.8F, 3.6F, -4.5F, -1.9199F, 0.4538F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(328, 329).addBox(-2.2F, -2.2F, -0.4F, 4.4F, 4.4F, 4.8F)
                .texOffs(422, 342).addBox(-1.364F, -1.364F, 4F, 2.728F, 2.728F, 4.25F),
                PartPose.offsetAndRotation(-4.6F, -5.6F, -3.5F, 0.4538F, -0.4189F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(321, 342).addBox(-1.7F, -1.7F, -0.4F, 3.4F, 3.4F, 4.525F)
                .texOffs(178, 352).addBox(-1.054F, -1.054F, 3.75F, 2.108F, 2.108F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.1571F, -0.1047F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(193, 352).addBox(-1.3F, -1.3F, -0.4F, 2.6F, 2.6F, 3.975F)
                .texOffs(26, 367).addBox(-0.806F, -0.806F, 3.25F, 1.612F, 1.612F, 3.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.1047F, -0.0349F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(39, 367).addBox(-0.9F, -0.9F, -0.4F, 1.8F, 1.8F, 3.425F)
                .texOffs(33, 374).addBox(-0.558F, -0.558F, 2.75F, 1.116F, 1.116F, 3F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.1745F, 0.0698F, 0F));
        PartDefinition p_horn_r_low1 = p_head.addOrReplaceChild("horn_r_low1", CubeListBuilder.create()
                .texOffs(208, 352).addBox(-1.3F, -1.3F, -0.4F, 2.6F, 2.6F, 3.425F)
                .texOffs(44, 374).addBox(-0.806F, -0.806F, 2.75F, 1.612F, 1.612F, 3F),
                PartPose.offsetAndRotation(-6F, -2.6F, -2.5F, 0.1047F, -0.733F, 0F));
        PartDefinition p_horn_r_low2 = p_horn_r_low1.addOrReplaceChild("horn_r_low2", CubeListBuilder.create()
                .texOffs(55, 374).addBox(-0.95F, -0.95F, -0.4F, 1.9F, 1.9F, 2.875F)
                .texOffs(66, 374).addBox(-0.589F, -0.589F, 2.25F, 1.178F, 1.178F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, -0.1047F, 0F));
        PartDefinition p_horn_r_low3 = p_horn_r_low2.addOrReplaceChild("horn_r_low3", CubeListBuilder.create()
                .texOffs(77, 374).addBox(-0.65F, -0.65F, -0.4F, 1.3F, 1.3F, 2.325F)
                .texOffs(104, 385).addBox(-0.403F, -0.403F, 1.75F, 0.806F, 0.806F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(404, 380).addBox(-0.8F, -1.33F, -1F, 1.6F, 1.93F, 2F)
                .texOffs(413, 380).addBox(-0.52F, -2.49F, -0.65F, 1.04F, 1.41F, 1.3F)
                .texOffs(206, 385).addBox(-0.25F, -3.65F, -0.3F, 0.5F, 1.41F, 0.6F),
                PartPose.offsetAndRotation(-7F, 1.5F, -7F, -1.7453F, -0.5236F, 0F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(422, 380).addBox(-0.7F, -1.078F, -0.875F, 1.4F, 1.678F, 1.75F)
                .texOffs(28, 385).addBox(-0.455F, -2.064F, -0.5687F, 0.91F, 1.236F, 1.1375F)
                .texOffs(211, 385).addBox(-0.25F, -3.05F, -0.2625F, 0.5F, 1.236F, 0.525F),
                PartPose.offsetAndRotation(-6.8F, 3.6F, -4.5F, -1.9199F, -0.4538F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(344, 242).addBox(-4.5F, 1.3F, -16.2F, 9F, 1F, 16.5F)
                .texOffs(57, 220).addBox(-3.2F, 0F, -16.12F, 6.4F, 3.6F, 16.34F)
                .texOffs(0, 352).addBox(-5.2F, 0.9F, -3F, 10.4F, 1.6F, 5F)
                .texOffs(280, 329).addBox(-3.8F, -0.5F, -2.92F, 7.6F, 4.4F, 4.84F)
                .texOffs(158, 342).addBox(-3.4F, 2.6F, -15.8F, 6.8F, 1.6F, 7F)
                .texOffs(216, 385).addBox(3.275F, -1.655F, -15.0583F, 0.85F, 1.755F, 0.85F)
                .texOffs(221, 385).addBox(3.275F, -1.2F, -13.125F, 0.85F, 1.3F, 0.85F)
                .texOffs(226, 385).addBox(3.275F, -1.2F, -11.1917F, 0.85F, 1.3F, 0.85F)
                .texOffs(231, 385).addBox(3.275F, -1.2F, -9.2583F, 0.85F, 1.3F, 0.85F)
                .texOffs(236, 385).addBox(3.275F, -1.2F, -7.325F, 0.85F, 1.3F, 0.85F)
                .texOffs(241, 385).addBox(3.275F, -1.655F, -5.3917F, 0.85F, 1.755F, 0.85F)
                .texOffs(246, 385).addBox(-4.125F, -1.655F, -15.0583F, 0.85F, 1.755F, 0.85F)
                .texOffs(251, 385).addBox(-4.125F, -1.2F, -13.125F, 0.85F, 1.3F, 0.85F)
                .texOffs(256, 385).addBox(-4.125F, -1.2F, -11.1917F, 0.85F, 1.3F, 0.85F)
                .texOffs(261, 385).addBox(-4.125F, -1.2F, -9.2583F, 0.85F, 1.3F, 0.85F)
                .texOffs(266, 385).addBox(-4.125F, -1.2F, -7.325F, 0.85F, 1.3F, 0.85F)
                .texOffs(271, 385).addBox(-4.125F, -1.655F, -5.3917F, 0.85F, 1.755F, 0.85F)
                .texOffs(108, 262).addBox(-3.8F, -0.15F, -15.8F, 7.6F, 0.3F, 16F)
                .texOffs(157, 314).addBox(-2.6F, -0.5F, -14.5F, 5.2F, 0.6F, 12F)
                .texOffs(0, 329).addBox(-3.2F, 3.4F, -11F, 6.4F, 0.8F, 11F),
                PartPose.offsetAndRotation(0F, 4F, -11.5F, 0.0698F, 0F, 0F));
        PartDefinition p_beard1 = p_jaw.addOrReplaceChild("beard1", CubeListBuilder.create()
                .texOffs(431, 380).addBox(-0.8F, -0.994F, -1F, 1.6F, 1.594F, 2F)
                .texOffs(440, 380).addBox(-0.52F, -1.922F, -0.65F, 1.04F, 1.178F, 1.3F)
                .texOffs(276, 385).addBox(-0.25F, -2.85F, -0.3F, 0.5F, 1.178F, 0.6F),
                PartPose.offsetAndRotation(0F, 3.8F, -6F, -2.618F, 0F, 0F));
        PartDefinition p_beard_l = p_jaw.addOrReplaceChild("beard_l", CubeListBuilder.create()
                .texOffs(449, 380).addBox(-0.7F, -0.826F, -0.875F, 1.4F, 1.426F, 1.75F)
                .texOffs(35, 385).addBox(-0.455F, -1.638F, -0.5687F, 0.91F, 1.062F, 1.1375F)
                .texOffs(281, 385).addBox(-0.25F, -2.45F, -0.2625F, 0.5F, 1.062F, 0.525F),
                PartPose.offsetAndRotation(2.6F, 3.6F, -3F, -2.618F, -0.1745F, 0F));
        PartDefinition p_beard_r = p_jaw.addOrReplaceChild("beard_r", CubeListBuilder.create()
                .texOffs(458, 380).addBox(-0.7F, -0.826F, -0.875F, 1.4F, 1.426F, 1.75F)
                .texOffs(42, 385).addBox(-0.455F, -1.638F, -0.5687F, 0.91F, 1.062F, 1.1375F)
                .texOffs(286, 385).addBox(-0.25F, -2.45F, -0.2625F, 0.5F, 1.062F, 0.525F),
                PartPose.offsetAndRotation(-2.6F, 3.6F, -3F, -2.618F, 0.1745F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(428, 197).addBox(-7F, -2.6F, -0.6F, 14F, 5.2F, 14.2F)
                .texOffs(329, 136).addBox(-3.1F, -6.5F, -0.52F, 6.2F, 13F, 14.04F)
                .texOffs(45, 298).addBox(-3.1F, 5.5F, 0F, 6.2F, 1.6F, 13F),
                PartPose.offsetAndRotation(0F, -4F, 20F, -0.1745F, 0F, 0F));
        PartDefinition p_tail1_spike = p_tail1.addOrReplaceChild("tail1_spike", CubeListBuilder.create()
                .texOffs(86, 352).addBox(-1.6F, -2.002F, -2F, 3.2F, 2.602F, 4F)
                .texOffs(441, 367).addBox(-1.04F, -3.626F, -1.3F, 2.08F, 1.874F, 2.6F)
                .texOffs(49, 385).addBox(-0.48F, -5.25F, -0.6F, 0.96F, 1.874F, 1.2F),
                PartPose.offsetAndRotation(0F, -6.1F, 5.85F, -0.5585F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(139, 242).addBox(-6.1F, -2.24F, -0.6F, 12.2F, 4.48F, 13.2F)
                .texOffs(0, 170).addBox(-2.74F, -5.6F, -0.52F, 5.48F, 11.2F, 13.04F)
                .texOffs(338, 298).addBox(-2.74F, 4.6F, 0F, 5.48F, 1.6F, 12F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_tail2_spike = p_tail2.addOrReplaceChild("tail2_spike", CubeListBuilder.create()
                .texOffs(223, 352).addBox(-1.464F, -1.834F, -1.83F, 2.928F, 2.434F, 3.66F)
                .texOffs(88, 374).addBox(-0.9516F, -3.342F, -1.1895F, 1.9032F, 1.758F, 2.379F)
                .texOffs(56, 385).addBox(-0.4392F, -4.85F, -0.549F, 0.8784F, 1.758F, 1.098F),
                PartPose.offsetAndRotation(0F, -5.2F, 5.4F, -0.5585F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(397, 242).addBox(-5.3F, -1.92F, -0.6F, 10.6F, 3.84F, 13.2F)
                .texOffs(212, 170).addBox(-2.42F, -4.8F, -0.52F, 4.84F, 9.6F, 13.04F)
                .texOffs(35, 314).addBox(-2.42F, 3.8F, 0F, 4.84F, 1.6F, 12F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3_spike = p_tail3.addOrReplaceChild("tail3_spike", CubeListBuilder.create()
                .texOffs(238, 352).addBox(-1.272F, -1.666F, -1.59F, 2.544F, 2.266F, 3.18F)
                .texOffs(99, 374).addBox(-0.8268F, -3.058F, -1.0335F, 1.6536F, 1.642F, 2.067F)
                .texOffs(291, 385).addBox(-0.3816F, -4.45F, -0.477F, 0.7632F, 1.642F, 0.954F),
                PartPose.offsetAndRotation(0F, -4.4F, 5.4F, -0.5585F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(157, 262).addBox(-4.6F, -1.64F, -0.6F, 9.2F, 3.28F, 12.2F)
                .texOffs(306, 197).addBox(-2.14F, -4.1F, -0.52F, 4.28F, 8.2F, 12.04F)
                .texOffs(264, 314).addBox(-2.14F, 3.1F, 0F, 4.28F, 1.6F, 11F),
                PartPose.offset(0F, 0F, 12F));
        PartDefinition p_tail4_spike = p_tail4.addOrReplaceChild("tail4_spike", CubeListBuilder.create()
                .texOffs(52, 367).addBox(-1.104F, -1.456F, -1.38F, 2.208F, 2.056F, 2.76F)
                .texOffs(467, 380).addBox(-0.7176F, -2.703F, -0.897F, 1.4352F, 1.497F, 1.794F)
                .texOffs(296, 385).addBox(-0.3312F, -3.95F, -0.414F, 0.6624F, 1.497F, 0.828F),
                PartPose.offsetAndRotation(0F, -3.7F, 4.95F, -0.5585F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(172, 281).addBox(-3.9F, -1.4F, -0.6F, 7.8F, 2.8F, 12.2F)
                .texOffs(211, 220).addBox(-1.8F, -3.5F, -0.52F, 3.6F, 7F, 12.04F)
                .texOffs(297, 314).addBox(-1.8F, 2.5F, 0F, 3.6F, 1.6F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0524F, 0F, 0F));
        PartDefinition p_tail5_spike = p_tail5.addOrReplaceChild("tail5_spike", CubeListBuilder.create()
                .texOffs(110, 374).addBox(-0.936F, -1.246F, -1.17F, 1.872F, 1.846F, 2.34F)
                .texOffs(476, 380).addBox(-0.6084F, -2.348F, -0.7605F, 1.2168F, 1.352F, 1.521F)
                .texOffs(301, 385).addBox(-0.2808F, -3.45F, -0.351F, 0.5616F, 1.352F, 0.702F),
                PartPose.offsetAndRotation(0F, -3.1F, 4.95F, -0.5585F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(125, 298).addBox(-3.2F, -1.16F, -0.6F, 6.4F, 2.32F, 11.2F)
                .texOffs(479, 242).addBox(-1.46F, -2.9F, -0.52F, 2.92F, 5.8F, 11.04F)
                .texOffs(126, 329).addBox(-1.46F, 1.9F, 0F, 2.92F, 1.6F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0698F, 0F, 0F));
        PartDefinition p_tail6_spike = p_tail6.addOrReplaceChild("tail6_spike", CubeListBuilder.create()
                .texOffs(485, 380).addBox(-0.768F, -1.036F, -0.96F, 1.536F, 1.636F, 1.92F)
                .texOffs(63, 385).addBox(-0.4992F, -1.993F, -0.624F, 0.9984F, 1.207F, 1.248F)
                .texOffs(306, 385).addBox(-0.25F, -2.95F, -0.288F, 0.5F, 1.207F, 0.576F),
                PartPose.offsetAndRotation(0F, -2.5F, 4.5F, -0.5585F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(229, 314).addBox(-2.6F, -0.96F, -0.6F, 5.2F, 1.92F, 10.2F)
                .texOffs(326, 281).addBox(-1.16F, -2.4F, -0.52F, 2.32F, 4.8F, 10.04F)
                .texOffs(195, 329).addBox(-1.16F, 1.4F, 0F, 2.32F, 1.6F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail7_spike = p_tail7.addOrReplaceChild("tail7_spike", CubeListBuilder.create()
                .texOffs(494, 380).addBox(-0.7F, -0.826F, -0.875F, 1.4F, 1.426F, 1.75F)
                .texOffs(70, 385).addBox(-0.455F, -1.638F, -0.5687F, 0.91F, 1.062F, 1.1375F)
                .texOffs(311, 385).addBox(-0.25F, -2.45F, -0.2625F, 0.5F, 1.062F, 0.525F),
                PartPose.offsetAndRotation(0F, -2F, 4.05F, -0.5585F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(37, 329).addBox(-2.1F, -0.76F, -0.6F, 4.2F, 1.52F, 9.2F)
                .texOffs(70, 314).addBox(-0.96F, -1.9F, -0.52F, 1.92F, 3.8F, 9.04F)
                .texOffs(349, 329).addBox(-0.96F, 0.9F, 0F, 1.92F, 1.6F, 8F)
                .texOffs(366, 314).addBox(-5.5F, -0.5F, 4F, 11F, 1F, 11F)
                .texOffs(95, 314).addBox(-0.5F, -5F, 3F, 1F, 4.5F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(286, 170).addBox(-5F, -4F, -2.8F, 10F, 17.5F, 5.6F)
                .texOffs(374, 136).addBox(-2.8F, -3.92F, -5F, 5.6F, 17.34F, 10F)
                .texOffs(375, 298).addBox(-5.6F, -5.5F, -2.6F, 11.2F, 7.5F, 5.2F)
                .texOffs(137, 220).addBox(-2.6F, -5.42F, -5.6F, 5.2F, 7.34F, 11.2F),
                PartPose.offsetAndRotation(11.5F, 4F, -14F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(221, 242).addBox(-3.75F, -1.4F, -2.1F, 7.5F, 13.6F, 4.2F)
                .texOffs(374, 197).addBox(-2.1F, -1.32F, -3.75F, 4.2F, 13.44F, 7.5F),
                PartPose.offsetAndRotation(0F, 12.9F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(209, 298).addBox(-4.75F, 1.05F, -8.4F, 9.5F, 1.4F, 12F)
                .texOffs(215, 281).addBox(-3.7F, 0F, -8.32F, 7.4F, 3.5F, 11.84F)
                .texOffs(65, 367).addBox(-4.3167F, 1.6F, -11.3F, 1.9F, 1.9F, 4F)
                .texOffs(121, 374).addBox(-4.0317F, 2.075F, -13.7F, 1.33F, 1.71F, 2.7F)
                .texOffs(78, 367).addBox(-0.95F, 1.6F, -11.3F, 1.9F, 1.9F, 4F)
                .texOffs(132, 374).addBox(-0.665F, 2.075F, -13.7F, 1.33F, 1.71F, 2.7F)
                .texOffs(91, 367).addBox(2.4167F, 1.6F, -11.3F, 1.9F, 1.9F, 4F)
                .texOffs(143, 374).addBox(2.7017F, 2.075F, -13.7F, 1.33F, 1.71F, 2.7F),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(154, 136).addBox(-6F, -4.8F, -4.36F, 12F, 19.8F, 8.72F)
                .texOffs(133, 101).addBox(-3.36F, -4.72F, -7F, 6.72F, 19.64F, 14F)
                .texOffs(204, 262).addBox(-6.6F, -6.6F, -4F, 13.2F, 9F, 8F)
                .texOffs(41, 170).addBox(-3F, -6.52F, -7.6F, 6F, 8.84F, 15.2F),
                PartPose.offsetAndRotation(12F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(246, 220).addBox(-4.25F, -1.4F, -2.38F, 8.5F, 14.1F, 4.76F)
                .texOffs(352, 170).addBox(-2.38F, -1.32F, -4.25F, 4.76F, 13.94F, 8.5F),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(355, 281).addBox(-5.25F, 1.05F, -9.1F, 10.5F, 1.4F, 13F)
                .texOffs(249, 262).addBox(-4.2F, 0F, -9.02F, 8.4F, 3.5F, 12.84F)
                .texOffs(253, 352).addBox(-4.65F, 1.6F, -12.5F, 1.9F, 1.9F, 4.5F)
                .texOffs(154, 374).addBox(-4.365F, 2.075F, -15.1F, 1.33F, 1.71F, 2.9F)
                .texOffs(268, 352).addBox(-0.95F, 1.6F, -12.5F, 1.9F, 1.9F, 4.5F)
                .texOffs(165, 374).addBox(-0.665F, 2.075F, -15.1F, 1.33F, 1.71F, 2.9F)
                .texOffs(283, 352).addBox(2.75F, 1.6F, -12.5F, 1.9F, 1.9F, 4.5F)
                .texOffs(176, 374).addBox(3.035F, 2.075F, -15.1F, 1.33F, 1.71F, 2.9F),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(319, 170).addBox(-5F, -4F, -2.8F, 10F, 17.5F, 5.6F)
                .texOffs(407, 136).addBox(-2.8F, -3.92F, -5F, 5.6F, 17.34F, 10F)
                .texOffs(412, 298).addBox(-5.6F, -5.5F, -2.6F, 11.2F, 7.5F, 5.2F)
                .texOffs(174, 220).addBox(-2.6F, -5.42F, -5.6F, 5.2F, 7.34F, 11.2F),
                PartPose.offsetAndRotation(-11.5F, 4F, -14F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(248, 242).addBox(-3.75F, -1.4F, -2.1F, 7.5F, 13.6F, 4.2F)
                .texOffs(401, 197).addBox(-2.1F, -1.32F, -3.75F, 4.2F, 13.44F, 7.5F),
                PartPose.offsetAndRotation(0F, 12.9F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(254, 298).addBox(-4.75F, 1.05F, -8.4F, 9.5F, 1.4F, 12F)
                .texOffs(256, 281).addBox(-3.7F, 0F, -8.32F, 7.4F, 3.5F, 11.84F)
                .texOffs(104, 367).addBox(-4.3167F, 1.6F, -11.3F, 1.9F, 1.9F, 4F)
                .texOffs(187, 374).addBox(-4.0317F, 2.075F, -13.7F, 1.33F, 1.71F, 2.7F)
                .texOffs(117, 367).addBox(-0.95F, 1.6F, -11.3F, 1.9F, 1.9F, 4F)
                .texOffs(198, 374).addBox(-0.665F, 2.075F, -13.7F, 1.33F, 1.71F, 2.7F)
                .texOffs(130, 367).addBox(2.4167F, 1.6F, -11.3F, 1.9F, 1.9F, 4F)
                .texOffs(209, 374).addBox(2.7017F, 2.075F, -13.7F, 1.33F, 1.71F, 2.7F),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(197, 136).addBox(-6F, -4.8F, -4.36F, 12F, 19.8F, 8.72F)
                .texOffs(176, 101).addBox(-3.36F, -4.72F, -7F, 6.72F, 19.64F, 14F)
                .texOffs(294, 262).addBox(-6.6F, -6.6F, -4F, 13.2F, 9F, 8F)
                .texOffs(86, 170).addBox(-3F, -6.52F, -7.6F, 6F, 8.84F, 15.2F),
                PartPose.offsetAndRotation(-12F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(275, 220).addBox(-4.25F, -1.4F, -2.38F, 8.5F, 14.1F, 4.76F)
                .texOffs(381, 170).addBox(-2.38F, -1.32F, -4.25F, 4.76F, 13.94F, 8.5F),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(404, 281).addBox(-5.25F, 1.05F, -9.1F, 10.5F, 1.4F, 13F)
                .texOffs(339, 262).addBox(-4.2F, 0F, -9.02F, 8.4F, 3.5F, 12.84F)
                .texOffs(298, 352).addBox(-4.65F, 1.6F, -12.5F, 1.9F, 1.9F, 4.5F)
                .texOffs(220, 374).addBox(-4.365F, 2.075F, -15.1F, 1.33F, 1.71F, 2.9F)
                .texOffs(313, 352).addBox(-0.95F, 1.6F, -12.5F, 1.9F, 1.9F, 4.5F)
                .texOffs(231, 374).addBox(-0.665F, 2.075F, -15.1F, 1.33F, 1.71F, 2.9F)
                .texOffs(328, 352).addBox(2.75F, 1.6F, -12.5F, 1.9F, 1.9F, 4.5F)
                .texOffs(242, 374).addBox(3.035F, 2.075F, -15.1F, 1.33F, 1.71F, 2.9F),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(47, 342).addBox(-1F, -2.75F, -1.21F, 16.4F, 5.5F, 2.42F)
                .texOffs(427, 329).addBox(-0.92F, -1.21F, -2.75F, 16.24F, 2.42F, 5.5F)
                .texOffs(360, 360).addBox(14F, -1.8F, -0.792F, 14.8F, 3.6F, 1.584F)
                .texOffs(204, 360).addBox(14.08F, -0.792F, -1.8F, 14.64F, 1.584F, 3.6F)
                .texOffs(219, 101).addBox(0F, 0F, 1.3F, 28F, 0F, 33F),
                PartPose.offset(11F, -11F, -12F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(243, 360).addBox(-0.6F, -1.755F, -0.7722F, 16.6F, 3.51F, 1.5444F)
                .texOffs(118, 360).addBox(-0.52F, -0.7722F, -1.755F, 16.44F, 1.5444F, 3.51F)
                .texOffs(277, 367).addBox(14.4F, -1.2375F, -0.5445F, 18.2F, 2.475F, 1.089F)
                .texOffs(187, 367).addBox(14.48F, -0.5445F, -1.2375F, 18.04F, 1.089F, 2.475F)
                .texOffs(294, 63).addBox(0F, 0F, 0.7375F, 32F, 0F, 34F),
                PartPose.offset(28F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(439, 342).addBox(-2.1375F, -2.1375F, -1.0687F, 4.275F, 4.275F, 2.1375F)
                .texOffs(363, 342).addBox(-2.0575F, -1.0687F, -2.1375F, 4.115F, 2.1375F, 4.275F)
                .texOffs(143, 367).addBox(-0.9F, -0.9F, -5.3375F, 1.8F, 1.8F, 3.4F)
                .texOffs(253, 374).addBox(-0.6F, -0.5F, -7.5375F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(32F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(369, 352).addBox(0F, -1.2F, -1.2F, 28.08F, 2.4F, 2.4F)
                .texOffs(320, 374).addBox(27F, -0.72F, -0.72F, 27F, 1.44F, 1.44F)
                .texOffs(0, 197).addBox(0F, 0F, 0.42F, 54F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1047F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(0, 360).addBox(0F, -1.05F, -1.05F, 26F, 2.1F, 2.1F)
                .texOffs(438, 374).addBox(25F, -0.63F, -0.63F, 25F, 1.26F, 1.26F)
                .texOffs(304, 220).addBox(0F, 0F, 0.33F, 50F, 0F, 19F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5236F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(55, 380).addBox(0F, -0.9F, -0.9F, 22.88F, 1.8F, 1.8F)
                .texOffs(157, 380).addBox(22F, -0.54F, -0.54F, 22F, 1.08F, 1.08F)
                .texOffs(384, 262).addBox(0F, 0F, 0.24F, 44F, 0F, 16F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9425F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(255, 380).addBox(0F, -0.75F, -0.75F, 18.72F, 1.5F, 1.5F)
                .texOffs(316, 385).addBox(18F, -0.45F, -0.45F, 18F, 0.9F, 0.9F)
                .texOffs(0, 63).addBox(0F, 0F, 0.15F, 36F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.3614F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(88, 342).addBox(-15.4F, -2.75F, -1.21F, 16.4F, 5.5F, 2.42F)
                .texOffs(0, 342).addBox(-15.32F, -1.21F, -2.75F, 16.24F, 2.42F, 5.5F)
                .texOffs(395, 360).addBox(-28.8F, -1.8F, -0.792F, 14.8F, 3.6F, 1.584F)
                .texOffs(282, 360).addBox(-28.72F, -0.792F, -1.8F, 14.64F, 1.584F, 3.6F)
                .texOffs(342, 101).addBox(-28F, 0F, 1.3F, 28F, 0F, 33F),
                PartPose.offset(-11F, -11F, -12F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(321, 360).addBox(-16F, -1.755F, -0.7722F, 16.6F, 3.51F, 1.5444F)
                .texOffs(161, 360).addBox(-15.92F, -0.7722F, -1.755F, 16.44F, 1.5444F, 3.51F)
                .texOffs(320, 367).addBox(-32.6F, -1.2375F, -0.5445F, 18.2F, 2.475F, 1.089F)
                .texOffs(232, 367).addBox(-32.52F, -0.5445F, -1.2375F, 18.04F, 1.089F, 2.475F)
                .texOffs(0, 101).addBox(-32F, 0F, 0.7375F, 32F, 0F, 34F),
                PartPose.offset(-28F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(456, 342).addBox(-2.1375F, -2.1375F, -1.0687F, 4.275F, 4.275F, 2.1375F)
                .texOffs(384, 342).addBox(-2.0575F, -1.0687F, -2.1375F, 4.115F, 2.1375F, 4.275F)
                .texOffs(156, 367).addBox(-0.9F, -0.9F, -5.3375F, 1.8F, 1.8F, 3.4F)
                .texOffs(264, 374).addBox(-0.6F, -0.5F, -7.5375F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(-32F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(434, 352).addBox(-28.08F, -1.2F, -1.2F, 28.08F, 2.4F, 2.4F)
                .texOffs(379, 374).addBox(-54F, -0.72F, -0.72F, 27F, 1.44F, 1.44F)
                .texOffs(153, 197).addBox(-54F, 0F, 0.42F, 54F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1047F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(59, 360).addBox(-26F, -1.05F, -1.05F, 26F, 2.1F, 2.1F)
                .texOffs(0, 380).addBox(-50F, -0.63F, -0.63F, 25F, 1.26F, 1.26F)
                .texOffs(0, 242).addBox(-50F, 0F, 0.33F, 50F, 0F, 19F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(106, 380).addBox(-22.88F, -0.9F, -0.9F, 22.88F, 1.8F, 1.8F)
                .texOffs(206, 380).addBox(-44F, -0.54F, -0.54F, 22F, 1.08F, 1.08F)
                .texOffs(0, 281).addBox(-44F, 0F, 0.24F, 44F, 0F, 16F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9425F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(298, 380).addBox(-18.72F, -0.75F, -0.75F, 18.72F, 1.5F, 1.5F)
                .texOffs(355, 385).addBox(-36F, -0.45F, -0.45F, 18F, 0.9F, 0.9F)
                .texOffs(147, 63).addBox(-36F, 0F, 0.15F, 36F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.3614F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
