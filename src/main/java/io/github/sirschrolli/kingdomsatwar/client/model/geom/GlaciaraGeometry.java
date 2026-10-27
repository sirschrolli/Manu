package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Glaciara, dem Eisdrachen-Boss (Grundgröße, im Spiel ×2,1).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlaciaraGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 384;

    private GlaciaraGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(93, 0).addBox(-8.5F, -5.2F, -18F, 17F, 9.4F, 36F)
                .texOffs(0, 0).addBox(-4.7F, -9F, -17.92F, 9.4F, 17F, 35.84F)
                .texOffs(325, 88).addBox(-9.5F, -5.8F, -19F, 19F, 10.6F, 14F)
                .texOffs(137, 54).addBox(-5.3F, -10F, -18.92F, 10.6F, 19F, 13.84F)
                .texOffs(106, 117).addBox(-9F, -5.7F, 6F, 18F, 9.4F, 11F)
                .texOffs(218, 88).addBox(-5.2F, -9.5F, 6.08F, 10.4F, 17F, 10.84F)
                .texOffs(188, 54).addBox(-4.5F, -10.7F, -16F, 9F, 0.9F, 30F)
                .texOffs(200, 0).addBox(-3.2F, -12F, -15.92F, 6.4F, 3.5F, 29.84F)
                .texOffs(479, 214).addBox(6.5F, -9.9F, -14F, 4.5F, 2.3F, 9F)
                .texOffs(79, 198).addBox(8.1F, -11.5F, -13.92F, 1.3F, 5.5F, 8.84F)
                .texOffs(0, 228).addBox(-11F, -9.9F, -14F, 4.5F, 2.3F, 9F)
                .texOffs(102, 198).addBox(-9.4F, -11.5F, -13.92F, 1.3F, 5.5F, 8.84F)
                .texOffs(336, 161).addBox(-7F, 8F, -18F, 14F, 2F, 15F)
                .texOffs(0, 117).addBox(-6.5F, 6.5F, -4F, 13F, 2F, 20F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_back_crystal1 = p_body.addOrReplaceChild("back_crystal1", CubeListBuilder.create()
                .texOffs(186, 252).addBox(-1.3F, -2.422F, -1.625F, 2.6F, 3.022F, 3.25F)
                .texOffs(247, 261).addBox(-0.845F, -4.336F, -1.0563F, 1.69F, 2.164F, 2.1125F)
                .texOffs(391, 280).addBox(-0.39F, -6.25F, -0.4875F, 0.78F, 2.164F, 0.975F),
                PartPose.offsetAndRotation(0F, -11.6F, -14F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal1_s0 = p_body.addOrReplaceChild("back_crystal1_s0", CubeListBuilder.create()
                .texOffs(188, 280).addBox(-0.78F, -1.288F, -0.975F, 1.56F, 1.888F, 1.95F)
                .texOffs(396, 280).addBox(-0.25F, -3.55F, -0.2925F, 0.5F, 2.512F, 0.585F),
                PartPose.offsetAndRotation(0.9F, -11.3F, -14.8F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal1_s1 = p_body.addOrReplaceChild("back_crystal1_s1", CubeListBuilder.create()
                .texOffs(197, 280).addBox(-0.78F, -1.288F, -0.975F, 1.56F, 1.888F, 1.95F)
                .texOffs(401, 280).addBox(-0.25F, -3.55F, -0.2925F, 0.5F, 2.512F, 0.585F),
                PartPose.offsetAndRotation(-0.9F, -11.3F, -13.2F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal2 = p_body.addOrReplaceChild("back_crystal2", CubeListBuilder.create()
                .texOffs(201, 252).addBox(-1.3F, -3.262F, -1.625F, 2.6F, 3.862F, 3.25F)
                .texOffs(258, 261).addBox(-0.845F, -5.756F, -1.0563F, 1.69F, 2.744F, 2.1125F)
                .texOffs(406, 280).addBox(-0.39F, -8.25F, -0.4875F, 0.78F, 2.744F, 0.975F),
                PartPose.offsetAndRotation(0F, -11.6F, -8F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal2_s0 = p_body.addOrReplaceChild("back_crystal2_s0", CubeListBuilder.create()
                .texOffs(360, 268).addBox(-0.78F, -1.75F, -0.975F, 1.56F, 2.35F, 1.95F)
                .texOffs(486, 268).addBox(-0.25F, -4.65F, -0.2925F, 0.5F, 3.15F, 0.585F),
                PartPose.offsetAndRotation(0.9F, -11.3F, -8.8F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal2_s1 = p_body.addOrReplaceChild("back_crystal2_s1", CubeListBuilder.create()
                .texOffs(369, 268).addBox(-0.78F, -1.75F, -0.975F, 1.56F, 2.35F, 1.95F)
                .texOffs(491, 268).addBox(-0.25F, -4.65F, -0.2925F, 0.5F, 3.15F, 0.585F),
                PartPose.offsetAndRotation(-0.9F, -11.3F, -7.2F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal3 = p_body.addOrReplaceChild("back_crystal3", CubeListBuilder.create()
                .texOffs(345, 241).addBox(-1.3F, -3.682F, -1.625F, 2.6F, 4.282F, 3.25F)
                .texOffs(415, 252).addBox(-0.845F, -6.466F, -1.0563F, 1.69F, 3.034F, 2.1125F)
                .texOffs(496, 268).addBox(-0.39F, -9.25F, -0.4875F, 0.78F, 3.034F, 0.975F),
                PartPose.offsetAndRotation(0F, -11.6F, -2F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal3_s0 = p_body.addOrReplaceChild("back_crystal3_s0", CubeListBuilder.create()
                .texOffs(378, 268).addBox(-0.78F, -1.981F, -0.975F, 1.56F, 2.581F, 1.95F)
                .texOffs(501, 268).addBox(-0.25F, -5.2F, -0.2925F, 0.5F, 3.469F, 0.585F),
                PartPose.offsetAndRotation(0.9F, -11.3F, -2.8F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal3_s1 = p_body.addOrReplaceChild("back_crystal3_s1", CubeListBuilder.create()
                .texOffs(387, 268).addBox(-0.78F, -1.981F, -0.975F, 1.56F, 2.581F, 1.95F)
                .texOffs(506, 268).addBox(-0.25F, -5.2F, -0.2925F, 0.5F, 3.469F, 0.585F),
                PartPose.offsetAndRotation(-0.9F, -11.3F, -1.2F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal4 = p_body.addOrReplaceChild("back_crystal4", CubeListBuilder.create()
                .texOffs(216, 252).addBox(-1.3F, -3.262F, -1.625F, 2.6F, 3.862F, 3.25F)
                .texOffs(269, 261).addBox(-0.845F, -5.756F, -1.0563F, 1.69F, 2.744F, 2.1125F)
                .texOffs(411, 280).addBox(-0.39F, -8.25F, -0.4875F, 0.78F, 2.744F, 0.975F),
                PartPose.offsetAndRotation(0F, -11.6F, 4F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal4_s0 = p_body.addOrReplaceChild("back_crystal4_s0", CubeListBuilder.create()
                .texOffs(396, 268).addBox(-0.78F, -1.75F, -0.975F, 1.56F, 2.35F, 1.95F)
                .texOffs(0, 274).addBox(-0.25F, -4.65F, -0.2925F, 0.5F, 3.15F, 0.585F),
                PartPose.offsetAndRotation(0.9F, -11.3F, 3.2F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal4_s1 = p_body.addOrReplaceChild("back_crystal4_s1", CubeListBuilder.create()
                .texOffs(405, 268).addBox(-0.78F, -1.75F, -0.975F, 1.56F, 2.35F, 1.95F)
                .texOffs(5, 274).addBox(-0.25F, -4.65F, -0.2925F, 0.5F, 3.15F, 0.585F),
                PartPose.offsetAndRotation(-0.9F, -11.3F, 4.8F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal5 = p_body.addOrReplaceChild("back_crystal5", CubeListBuilder.create()
                .texOffs(231, 252).addBox(-1.3F, -2.632F, -1.625F, 2.6F, 3.232F, 3.25F)
                .texOffs(280, 261).addBox(-0.845F, -4.691F, -1.0563F, 1.69F, 2.309F, 2.1125F)
                .texOffs(416, 280).addBox(-0.39F, -6.75F, -0.4875F, 0.78F, 2.309F, 0.975F),
                PartPose.offsetAndRotation(0F, -11.6F, 10F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal5_s0 = p_body.addOrReplaceChild("back_crystal5_s0", CubeListBuilder.create()
                .texOffs(414, 268).addBox(-0.78F, -1.4035F, -0.975F, 1.56F, 2.0035F, 1.95F)
                .texOffs(421, 280).addBox(-0.25F, -3.825F, -0.2925F, 0.5F, 2.6715F, 0.585F),
                PartPose.offsetAndRotation(0.9F, -11.3F, 9.2F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal5_s1 = p_body.addOrReplaceChild("back_crystal5_s1", CubeListBuilder.create()
                .texOffs(423, 268).addBox(-0.78F, -1.4035F, -0.975F, 1.56F, 2.0035F, 1.95F)
                .texOffs(426, 280).addBox(-0.25F, -3.825F, -0.2925F, 0.5F, 2.6715F, 0.585F),
                PartPose.offsetAndRotation(-0.9F, -11.3F, 10.8F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(267, 198).addBox(-5.5F, -2.2F, -7.1F, 11F, 4.4F, 7.7F)
                .texOffs(257, 140).addBox(-2.2F, -5.5F, -7.02F, 4.4F, 11F, 7.54F)
                .texOffs(117, 241).addBox(-2.2F, 4.5F, -6.5F, 4.4F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, -6F, -17F, -0.5934F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(106, 214).addBox(-5F, -2F, -6.8F, 10F, 4F, 7.4F)
                .texOffs(252, 161).addBox(-2F, -5F, -6.72F, 4F, 10F, 7.24F)
                .texOffs(192, 241).addBox(-2F, 4F, -6.2F, 4F, 1.6F, 6.2F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, -0.2793F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(143, 214).addBox(-4.6F, -1.84F, -6.6F, 9.2F, 3.68F, 7.2F)
                .texOffs(277, 161).addBox(-1.84F, -4.6F, -6.52F, 3.68F, 9.2F, 7.04F)
                .texOffs(66, 252).addBox(-1.84F, 3.6F, -6F, 3.68F, 1.6F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6.2F, -0.1396F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(180, 214).addBox(-4.25F, -1.7F, -6.6F, 8.5F, 3.4F, 7.2F)
                .texOffs(0, 180).addBox(-1.7F, -4.25F, -6.52F, 3.4F, 8.5F, 7.04F)
                .texOffs(87, 252).addBox(-1.7F, 3.25F, -6F, 3.4F, 1.6F, 6F),
                PartPose.offsetAndRotation(0F, 0F, -6F, -0.0349F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(176, 228).addBox(-4F, -1.6F, -6.4F, 8F, 3.2F, 7F)
                .texOffs(125, 198).addBox(-1.6F, -4F, -6.32F, 3.2F, 8F, 6.84F)
                .texOffs(108, 252).addBox(-1.6F, 3F, -5.8F, 3.2F, 1.6F, 5.8F),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.0698F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(452, 228).addBox(-3.75F, -1.5F, -6.2F, 7.5F, 3F, 6.8F)
                .texOffs(148, 198).addBox(-1.5F, -3.75F, -6.12F, 3F, 7.5F, 6.64F)
                .texOffs(129, 252).addBox(-1.5F, 2.75F, -5.6F, 3F, 1.6F, 5.6F),
                PartPose.offsetAndRotation(0F, 0F, -5.8F, 0.1571F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(483, 228).addBox(-3.5F, -1.4F, -6.1F, 7F, 2.8F, 6.7F)
                .texOffs(225, 198).addBox(-1.4F, -3.5F, -6.02F, 2.8F, 7F, 6.54F)
                .texOffs(148, 252).addBox(-1.4F, 2.5F, -5.5F, 2.8F, 1.6F, 5.5F),
                PartPose.offsetAndRotation(0F, 0F, -5.6F, 0.2094F, 0F, 0F));
        PartDefinition p_neck8 = p_neck7.addOrReplaceChild("neck8", CubeListBuilder.create()
                .texOffs(0, 241).addBox(-3.3F, -1.32F, -6F, 6.6F, 2.64F, 6.6F)
                .texOffs(246, 198).addBox(-1.32F, -3.3F, -5.92F, 2.64F, 6.6F, 6.44F)
                .texOffs(167, 252).addBox(-1.32F, 2.3F, -5.4F, 2.64F, 1.6F, 5.4F),
                PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.1571F, 0F, 0F));
        PartDefinition p_head = p_neck8.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(135, 161).addBox(-5F, -2.4F, -11.5F, 10F, 4.2F, 12.5F)
                .texOffs(67, 117).addBox(-2.8F, -4.6F, -11.42F, 5.6F, 8.6F, 12.34F)
                .texOffs(39, 252).addBox(-4F, -4.4F, -14F, 8F, 2.4F, 5F)
                .texOffs(285, 214).addBox(-3.8F, -1.6F, -18F, 7.6F, 3.6F, 7.5F)
                .texOffs(0, 198).addBox(-2.2F, -3.2F, -17.92F, 4.4F, 6.8F, 7.34F)
                .texOffs(417, 214).addBox(-3.3F, -1.4F, -25F, 6.6F, 3.6F, 7.5F)
                .texOffs(54, 198).addBox(-1.9F, -2.8F, -24.92F, 3.8F, 6.4F, 7.34F)
                .texOffs(169, 198).addBox(-1.8F, -3.9F, -23.5F, 3.6F, 1.4F, 12F)
                .texOffs(136, 268).addBox(-2.6F, -2.4F, -25.8F, 5.2F, 3.4F, 1F)
                .texOffs(483, 285).addBox(0.9F, -2.1F, -26F, 1.1F, 0.7F, 0.5F)
                .texOffs(491, 252).addBox(3.6F, -2.8F, -11.9F, 1.55F, 1.6F, 3.4F)
                .texOffs(176, 285).addBox(2.625F, 3.6F, -24.0536F, 0.75F, 1.485F, 0.75F)
                .texOffs(181, 285).addBox(2.625F, 3.6F, -22.4107F, 0.75F, 1.1F, 0.75F)
                .texOffs(186, 285).addBox(2.625F, 3.6F, -20.7679F, 0.75F, 1.1F, 0.75F)
                .texOffs(191, 285).addBox(2.625F, 3.6F, -19.125F, 0.75F, 1.1F, 0.75F)
                .texOffs(196, 285).addBox(2.625F, 3.6F, -17.4821F, 0.75F, 1.1F, 0.75F)
                .texOffs(201, 285).addBox(2.625F, 3.6F, -15.8393F, 0.75F, 1.1F, 0.75F)
                .texOffs(206, 285).addBox(2.625F, 3.6F, -14.1964F, 0.75F, 1.485F, 0.75F)
                .texOffs(431, 280).addBox(2.35F, 3.6F, -23.2F, 0.9F, 2.2F, 0.9F)
                .texOffs(490, 285).addBox(-2F, -2.1F, -26F, 1.1F, 0.7F, 0.5F)
                .texOffs(0, 261).addBox(-5.15F, -2.8F, -11.9F, 1.55F, 1.6F, 3.4F)
                .texOffs(211, 285).addBox(-3.375F, 3.6F, -24.0536F, 0.75F, 1.485F, 0.75F)
                .texOffs(216, 285).addBox(-3.375F, 3.6F, -22.4107F, 0.75F, 1.1F, 0.75F)
                .texOffs(221, 285).addBox(-3.375F, 3.6F, -20.7679F, 0.75F, 1.1F, 0.75F)
                .texOffs(226, 285).addBox(-3.375F, 3.6F, -19.125F, 0.75F, 1.1F, 0.75F)
                .texOffs(231, 285).addBox(-3.375F, 3.6F, -17.4821F, 0.75F, 1.1F, 0.75F)
                .texOffs(236, 285).addBox(-3.375F, 3.6F, -15.8393F, 0.75F, 1.1F, 0.75F)
                .texOffs(241, 285).addBox(-3.375F, 3.6F, -14.1964F, 0.75F, 1.485F, 0.75F)
                .texOffs(436, 280).addBox(-3.25F, 3.6F, -23.2F, 0.9F, 2.2F, 0.9F)
                .texOffs(497, 285).addBox(-1.85F, 3.6F, -25.4F, 0.7F, 0.9F, 0.7F)
                .texOffs(502, 285).addBox(-0.85F, 3.6F, -25.4F, 0.7F, 0.9F, 0.7F)
                .texOffs(507, 285).addBox(0.15F, 3.6F, -25.4F, 0.7F, 0.9F, 0.7F)
                .texOffs(0, 289).addBox(1.15F, 3.6F, -25.4F, 0.7F, 0.9F, 0.7F)
                .texOffs(306, 198).addBox(-2.7F, 3.62F, -24.3F, 5.4F, 0.3F, 11.5F),
                PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.5585F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(215, 241).addBox(-1.9F, -1.1F, -0.5F, 3.8F, 2F, 7F),
                PartPose.offsetAndRotation(3.4F, -4F, -11.6F, 0.0873F, 0F, -0.1745F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(432, 268).addBox(-0.65F, -1.834F, -0.8125F, 1.3F, 2.434F, 1.625F)
                .texOffs(10, 274).addBox(-0.25F, -4.85F, -0.25F, 0.5F, 3.266F, 0.5F),
                PartPose.offsetAndRotation(4.6F, -0.5F, -6F, -1.7453F, 0.6109F, 0F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(441, 268).addBox(-0.65F, -1.498F, -0.8125F, 1.3F, 2.098F, 1.625F)
                .texOffs(441, 280).addBox(-0.25F, -4.05F, -0.25F, 0.5F, 2.802F, 0.5F),
                PartPose.offsetAndRotation(4.6F, 1.5F, -4.5F, -1.7453F, 0.6109F, 0F));
        PartDefinition p_frill_l3 = p_head.addOrReplaceChild("frill_l3", CubeListBuilder.create()
                .texOffs(206, 280).addBox(-0.65F, -1.078F, -0.8125F, 1.3F, 1.678F, 1.625F)
                .texOffs(446, 280).addBox(-0.25F, -3.05F, -0.25F, 0.5F, 2.222F, 0.5F),
                PartPose.offsetAndRotation(4.6F, 3.2F, -3F, -1.7453F, 0.6109F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(295, 252).addBox(-1.3F, -1.3F, -0.4F, 2.6F, 2.6F, 3.975F)
                .texOffs(13, 261).addBox(-0.806F, -0.806F, 3.25F, 1.612F, 1.612F, 3.5F),
                PartPose.offsetAndRotation(3F, -4.2F, -3.5F, 0.3142F, 0.2793F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(26, 261).addBox(-1F, -1F, -0.4F, 2F, 2F, 3.7F)
                .texOffs(39, 261).addBox(-0.62F, -0.62F, 3F, 1.24F, 1.24F, 3.25F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.0698F, 0.0698F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(52, 261).addBox(-0.75F, -0.75F, -0.4F, 1.5F, 1.5F, 3.425F)
                .texOffs(215, 280).addBox(-0.465F, -0.465F, 2.75F, 0.93F, 0.93F, 3F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.0698F, 0.0349F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(224, 280).addBox(-0.5F, -0.5F, -0.4F, 1F, 1F, 2.875F)
                .texOffs(233, 280).addBox(-0.31F, -0.31F, 2.25F, 0.62F, 0.62F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, 0.1047F, 0F, 0F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(238, 241).addBox(-1.9F, -1.1F, -0.5F, 3.8F, 2F, 7F),
                PartPose.offsetAndRotation(-3.4F, -4F, -11.6F, 0.0873F, 0F, 0.1745F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(450, 268).addBox(-0.65F, -1.834F, -0.8125F, 1.3F, 2.434F, 1.625F)
                .texOffs(15, 274).addBox(-0.25F, -4.85F, -0.25F, 0.5F, 3.266F, 0.5F),
                PartPose.offsetAndRotation(-4.6F, -0.5F, -6F, -1.7453F, -0.6109F, 0F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(459, 268).addBox(-0.65F, -1.498F, -0.8125F, 1.3F, 2.098F, 1.625F)
                .texOffs(451, 280).addBox(-0.25F, -4.05F, -0.25F, 0.5F, 2.802F, 0.5F),
                PartPose.offsetAndRotation(-4.6F, 1.5F, -4.5F, -1.7453F, -0.6109F, 0F));
        PartDefinition p_frill_r3 = p_head.addOrReplaceChild("frill_r3", CubeListBuilder.create()
                .texOffs(242, 280).addBox(-0.65F, -1.078F, -0.8125F, 1.3F, 1.678F, 1.625F)
                .texOffs(456, 280).addBox(-0.25F, -3.05F, -0.25F, 0.5F, 2.222F, 0.5F),
                PartPose.offsetAndRotation(-4.6F, 3.2F, -3F, -1.7453F, -0.6109F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(310, 252).addBox(-1.3F, -1.3F, -0.4F, 2.6F, 2.6F, 3.975F)
                .texOffs(65, 261).addBox(-0.806F, -0.806F, 3.25F, 1.612F, 1.612F, 3.5F),
                PartPose.offsetAndRotation(-3F, -4.2F, -3.5F, 0.3142F, -0.2793F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(78, 261).addBox(-1F, -1F, -0.4F, 2F, 2F, 3.7F)
                .texOffs(91, 261).addBox(-0.62F, -0.62F, 3F, 1.24F, 1.24F, 3.25F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.0698F, -0.0698F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(104, 261).addBox(-0.75F, -0.75F, -0.4F, 1.5F, 1.5F, 3.425F)
                .texOffs(251, 280).addBox(-0.465F, -0.465F, 2.75F, 0.93F, 0.93F, 3F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.0698F, -0.0349F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(260, 280).addBox(-0.5F, -0.5F, -0.4F, 1F, 1F, 2.875F)
                .texOffs(269, 280).addBox(-0.31F, -0.31F, 2.25F, 0.62F, 0.62F, 2.5F),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, 0.1047F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -4.4F, -6F, -0.4887F, 0F, 0F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(426, 252).addBox(-0.85F, -2.842F, -1.0625F, 1.7F, 3.442F, 2.125F)
                .texOffs(468, 268).addBox(-0.5525F, -5.046F, -0.6906F, 1.105F, 2.454F, 1.3813F)
                .texOffs(461, 280).addBox(-0.255F, -7.25F, -0.3188F, 0.51F, 2.454F, 0.6375F),
                PartPose.offsetAndRotation(-4F, 0.3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(246, 252).addBox(-1F, -4.102F, -1.25F, 2F, 4.702F, 2.5F)
                .texOffs(324, 261).addBox(-0.65F, -7.176F, -0.8125F, 1.3F, 3.324F, 1.625F)
                .texOffs(20, 274).addBox(-0.3F, -10.25F, -0.375F, 0.6F, 3.324F, 0.75F),
                PartPose.offsetAndRotation(-2.1F, 0.3F, 0F, 0F, 0F, 0.1283F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(360, 241).addBox(-1.2F, -5.362F, -1.5F, 2.4F, 5.962F, 3F)
                .texOffs(448, 252).addBox(-0.78F, -9.306F, -0.975F, 1.56F, 4.194F, 1.95F)
                .texOffs(342, 261).addBox(-0.36F, -13.25F, -0.45F, 0.72F, 4.194F, 0.9F),
                PartPose.offset(0F, 0.3F, 0F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(257, 252).addBox(-1F, -4.102F, -1.25F, 2F, 4.702F, 2.5F)
                .texOffs(333, 261).addBox(-0.65F, -7.176F, -0.8125F, 1.3F, 3.324F, 1.625F)
                .texOffs(25, 274).addBox(-0.3F, -10.25F, -0.375F, 0.6F, 3.324F, 0.75F),
                PartPose.offsetAndRotation(2.1F, 0.3F, 0F, 0F, 0F, -0.1283F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(437, 252).addBox(-0.85F, -2.842F, -1.0625F, 1.7F, 3.442F, 2.125F)
                .texOffs(477, 268).addBox(-0.5525F, -5.046F, -0.6906F, 1.105F, 2.454F, 1.3813F)
                .texOffs(466, 280).addBox(-0.255F, -7.25F, -0.3188F, 0.51F, 2.454F, 0.6375F),
                PartPose.offsetAndRotation(4F, 0.3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(25, 180).addBox(-3.2F, 1F, -14.8F, 6.4F, 0.8F, 15F)
                .texOffs(182, 161).addBox(-2.2F, 0F, -14.72F, 4.4F, 2.8F, 14.84F)
                .texOffs(268, 252).addBox(-4F, 0.7F, -3F, 8F, 1.4F, 4.5F)
                .texOffs(261, 241).addBox(-2.9F, -0.4F, -2.92F, 5.8F, 3.6F, 4.34F)
                .texOffs(246, 285).addBox(2.35F, -1.25F, -13.9714F, 0.7F, 1.35F, 0.7F)
                .texOffs(5, 289).addBox(2.35F, -0.9F, -12.4143F, 0.7F, 1F, 0.7F)
                .texOffs(10, 289).addBox(2.35F, -0.9F, -10.8571F, 0.7F, 1F, 0.7F)
                .texOffs(15, 289).addBox(2.35F, -0.9F, -9.3F, 0.7F, 1F, 0.7F)
                .texOffs(20, 289).addBox(2.35F, -0.9F, -7.7429F, 0.7F, 1F, 0.7F)
                .texOffs(25, 289).addBox(2.35F, -0.9F, -6.1857F, 0.7F, 1F, 0.7F)
                .texOffs(251, 285).addBox(2.35F, -1.25F, -4.6286F, 0.7F, 1.35F, 0.7F)
                .texOffs(256, 285).addBox(-3.05F, -1.25F, -13.9714F, 0.7F, 1.35F, 0.7F)
                .texOffs(30, 289).addBox(-3.05F, -0.9F, -12.4143F, 0.7F, 1F, 0.7F)
                .texOffs(35, 289).addBox(-3.05F, -0.9F, -10.8571F, 0.7F, 1F, 0.7F)
                .texOffs(40, 289).addBox(-3.05F, -0.9F, -9.3F, 0.7F, 1F, 0.7F)
                .texOffs(45, 289).addBox(-3.05F, -0.9F, -7.7429F, 0.7F, 1F, 0.7F)
                .texOffs(50, 289).addBox(-3.05F, -0.9F, -6.1857F, 0.7F, 1F, 0.7F)
                .texOffs(261, 285).addBox(-3.05F, -1.25F, -4.6286F, 0.7F, 1.35F, 0.7F)
                .texOffs(70, 180).addBox(-2.8F, -0.15F, -14.6F, 5.6F, 0.3F, 14.5F)
                .texOffs(448, 214).addBox(-1.9F, -0.45F, -13.5F, 3.8F, 0.5F, 11F)
                .texOffs(207, 228).addBox(-2.4F, 2.6F, -10F, 4.8F, 0.7F, 10F),
                PartPose.offsetAndRotation(0F, 3.6F, -10F, 0.0524F, 0F, 0F));
        PartDefinition p_chin_crystal = p_jaw.addOrReplaceChild("chin_crystal", CubeListBuilder.create()
                .texOffs(278, 280).addBox(-0.75F, -0.994F, -0.9375F, 1.5F, 1.594F, 1.875F)
                .texOffs(471, 280).addBox(-0.25F, -2.85F, -0.2813F, 0.5F, 2.106F, 0.5625F),
                PartPose.offsetAndRotation(0F, 2.6F, -8F, -2.618F, 0F, 0F));
        PartDefinition p_neck7_crystal = p_neck7.addOrReplaceChild("neck7_crystal", CubeListBuilder.create()
                .texOffs(151, 268).addBox(-0.85F, -0.7084F, -1.0625F, 1.7F, 1.3084F, 2.125F)
                .texOffs(160, 285).addBox(-0.5525F, -1.4392F, -0.6906F, 1.105F, 0.9808F, 1.3813F)
                .texOffs(55, 289).addBox(-0.255F, -2.17F, -0.3188F, 0.51F, 0.9808F, 0.6375F),
                PartPose.offsetAndRotation(0F, -3.1F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_neck5_crystal = p_neck5.addOrReplaceChild("neck5_crystal", CubeListBuilder.create()
                .texOffs(162, 268).addBox(-0.85F, -0.9436F, -1.0625F, 1.7F, 1.5436F, 2.125F)
                .texOffs(287, 280).addBox(-0.5525F, -1.8368F, -0.6906F, 1.105F, 1.1432F, 1.3813F)
                .texOffs(266, 285).addBox(-0.255F, -2.73F, -0.3188F, 0.51F, 1.1432F, 0.6375F),
                PartPose.offsetAndRotation(0F, -3.6F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_neck3_crystal = p_neck3.addOrReplaceChild("neck3_crystal", CubeListBuilder.create()
                .texOffs(173, 268).addBox(-0.85F, -1.1788F, -1.0625F, 1.7F, 1.7788F, 2.125F)
                .texOffs(296, 280).addBox(-0.5525F, -2.2344F, -0.6906F, 1.105F, 1.3056F, 1.3813F)
                .texOffs(271, 285).addBox(-0.255F, -3.29F, -0.3188F, 0.51F, 1.3056F, 0.6375F),
                PartPose.offsetAndRotation(0F, -4.2F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_neck1_crystal = p_neck1.addOrReplaceChild("neck1_crystal", CubeListBuilder.create()
                .texOffs(291, 261).addBox(-0.85F, -1.414F, -1.0625F, 1.7F, 2.014F, 2.125F)
                .texOffs(305, 280).addBox(-0.5525F, -2.632F, -0.6906F, 1.105F, 1.468F, 1.3813F)
                .texOffs(276, 285).addBox(-0.255F, -3.85F, -0.3188F, 0.51F, 1.468F, 0.6375F),
                PartPose.offsetAndRotation(0F, -5.1F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(424, 180).addBox(-5F, -1.9F, -0.6F, 10F, 3.8F, 10.2F)
                .texOffs(165, 117).addBox(-2.15F, -4.75F, -0.52F, 4.3F, 9.5F, 10.04F)
                .texOffs(238, 228).addBox(-2.15F, 3.75F, 0F, 4.3F, 1.6F, 9F),
                PartPose.offsetAndRotation(0F, -3F, 17F, -0.2094F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(467, 180).addBox(-4.5F, -1.7F, -0.6F, 9F, 3.4F, 10.2F)
                .texOffs(149, 140).addBox(-1.95F, -4.25F, -0.52F, 3.9F, 8.5F, 10.04F)
                .texOffs(296, 228).addBox(-1.95F, 3.25F, 0F, 3.9F, 1.6F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(343, 198).addBox(-4F, -1.5F, -0.6F, 8F, 3F, 9.7F)
                .texOffs(223, 161).addBox(-1.75F, -3.75F, -0.52F, 3.5F, 7.5F, 9.54F)
                .texOffs(323, 228).addBox(-1.75F, 2.75F, 0F, 3.5F, 1.6F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0873F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(380, 198).addBox(-3.5F, -1.32F, -0.6F, 7F, 2.64F, 9.7F)
                .texOffs(461, 161).addBox(-1.52F, -3.3F, -0.52F, 3.04F, 6.6F, 9.54F)
                .texOffs(350, 228).addBox(-1.52F, 2.3F, 0F, 3.04F, 1.6F, 8.5F),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(415, 198).addBox(-3.1F, -1.16F, -0.6F, 6.2F, 2.32F, 9.2F)
                .texOffs(113, 180).addBox(-1.36F, -2.9F, -0.52F, 2.72F, 5.8F, 9.04F)
                .texOffs(29, 241).addBox(-1.36F, 1.9F, 0F, 2.72F, 1.6F, 8F),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(318, 214).addBox(-2.7F, -1F, -0.6F, 5.4F, 2F, 9.2F)
                .texOffs(27, 198).addBox(-1.2F, -2.5F, -0.52F, 2.4F, 5F, 9.04F)
                .texOffs(52, 241).addBox(-1.2F, 1.5F, 0F, 2.4F, 1.6F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(267, 228).addBox(-2.3F, -0.88F, -0.6F, 4.6F, 1.76F, 8.7F)
                .texOffs(202, 198).addBox(-0.98F, -2.2F, -0.52F, 1.96F, 4.4F, 8.54F)
                .texOffs(75, 241).addBox(-0.98F, 1.2F, 0F, 1.96F, 1.6F, 7.5F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0524F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(377, 228).addBox(-1.9F, -0.72F, -0.6F, 3.8F, 1.44F, 8.7F)
                .texOffs(83, 214).addBox(-0.82F, -1.8F, -0.52F, 1.64F, 3.6F, 8.54F)
                .texOffs(96, 241).addBox(-0.82F, 0.8F, 0F, 1.64F, 1.6F, 7.5F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(404, 228).addBox(-1.6F, -0.6F, -0.6F, 3.2F, 1.2F, 8.2F)
                .texOffs(87, 228).addBox(-0.7F, -1.5F, -0.52F, 1.4F, 3F, 8.04F)
                .texOffs(307, 241).addBox(-0.7F, 0.5F, 0F, 1.4F, 1.6F, 7F),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(284, 241).addBox(-1.3F, -0.48F, -0.6F, 2.6F, 0.96F, 7.7F)
                .texOffs(431, 228).addBox(-0.58F, -1.2F, -0.52F, 1.16F, 2.4F, 7.54F)
                .texOffs(326, 241).addBox(-0.58F, 0.2F, 0F, 1.16F, 1.6F, 6.5F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0349F, 0F, 0F));
        PartDefinition p_tailfan_0 = p_tail10.addOrReplaceChild("tailfan_0", CubeListBuilder.create()
                .texOffs(140, 180).addBox(-0.4F, -2.5F, 0F, 0.8F, 5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0F, -0.384F, 0F));
        PartDefinition p_tailfan_1 = p_tail10.addOrReplaceChild("tailfan_1", CubeListBuilder.create()
                .texOffs(226, 140).addBox(-0.4F, -2.5F, 0F, 0.8F, 5F, 14F),
                PartPose.offset(0F, 0F, 6F));
        PartDefinition p_tailfan_2 = p_tail10.addOrReplaceChild("tailfan_2", CubeListBuilder.create()
                .texOffs(165, 180).addBox(-0.4F, -2.5F, 0F, 0.8F, 5F, 11F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0F, 0.384F, 0F));
        PartDefinition p_tail7_crystal = p_tail7.addOrReplaceChild("tail7_crystal", CubeListBuilder.create()
                .texOffs(314, 280).addBox(-0.64F, -0.616F, -0.8F, 1.28F, 1.216F, 1.6F)
                .texOffs(169, 285).addBox(-0.416F, -1.283F, -0.52F, 0.832F, 0.917F, 1.04F)
                .texOffs(60, 289).addBox(-0.25F, -1.95F, -0.25F, 0.5F, 0.917F, 0.5F),
                PartPose.offsetAndRotation(0F, -1.8F, 3.375F, -0.6632F, 0F, 0F));
        PartDefinition p_tail6_crystal = p_tail6.addOrReplaceChild("tail6_crystal", CubeListBuilder.create()
                .texOffs(323, 280).addBox(-0.7F, -0.805F, -0.875F, 1.4F, 1.405F, 1.75F)
                .texOffs(377, 280).addBox(-0.455F, -1.6025F, -0.5687F, 0.91F, 1.0475F, 1.1375F)
                .texOffs(281, 285).addBox(-0.25F, -2.4F, -0.2625F, 0.5F, 1.0475F, 0.525F),
                PartPose.offsetAndRotation(0F, -2.1F, 3.6F, -0.6632F, 0F, 0F));
        PartDefinition p_tail5_crystal = p_tail5.addOrReplaceChild("tail5_crystal", CubeListBuilder.create()
                .texOffs(332, 280).addBox(-0.76F, -0.994F, -0.95F, 1.52F, 1.594F, 1.9F)
                .texOffs(384, 280).addBox(-0.494F, -1.922F, -0.6175F, 0.988F, 1.178F, 1.235F)
                .texOffs(286, 285).addBox(-0.25F, -2.85F, -0.285F, 0.5F, 1.178F, 0.57F),
                PartPose.offsetAndRotation(0F, -2.5F, 3.6F, -0.6632F, 0F, 0F));
        PartDefinition p_tail4_crystal = p_tail4.addOrReplaceChild("tail4_crystal", CubeListBuilder.create()
                .texOffs(184, 268).addBox(-0.82F, -1.183F, -1.025F, 1.64F, 1.783F, 2.05F)
                .texOffs(341, 280).addBox(-0.533F, -2.2415F, -0.6663F, 1.066F, 1.3085F, 1.3325F)
                .texOffs(291, 285).addBox(-0.25F, -3.3F, -0.3075F, 0.5F, 1.3085F, 0.615F),
                PartPose.offsetAndRotation(0F, -2.9F, 3.825F, -0.6632F, 0F, 0F));
        PartDefinition p_tail3_crystal = p_tail3.addOrReplaceChild("tail3_crystal", CubeListBuilder.create()
                .texOffs(195, 268).addBox(-0.88F, -1.372F, -1.1F, 1.76F, 1.972F, 2.2F)
                .texOffs(350, 280).addBox(-0.572F, -2.561F, -0.715F, 1.144F, 1.439F, 1.43F)
                .texOffs(296, 285).addBox(-0.264F, -3.75F, -0.33F, 0.528F, 1.439F, 0.66F),
                PartPose.offsetAndRotation(0F, -3.35F, 3.825F, -0.6632F, 0F, 0F));
        PartDefinition p_tail2_crystal = p_tail2.addOrReplaceChild("tail2_crystal", CubeListBuilder.create()
                .texOffs(302, 261).addBox(-0.94F, -1.561F, -1.175F, 1.88F, 2.161F, 2.35F)
                .texOffs(359, 280).addBox(-0.611F, -2.8805F, -0.7637F, 1.222F, 1.5695F, 1.5275F)
                .texOffs(301, 285).addBox(-0.282F, -4.2F, -0.3525F, 0.564F, 1.5695F, 0.705F),
                PartPose.offsetAndRotation(0F, -3.85F, 4.05F, -0.6632F, 0F, 0F));
        PartDefinition p_tail1_crystal = p_tail1.addOrReplaceChild("tail1_crystal", CubeListBuilder.create()
                .texOffs(313, 261).addBox(-1F, -1.75F, -1.25F, 2F, 2.35F, 2.5F)
                .texOffs(368, 280).addBox(-0.65F, -3.2F, -0.8125F, 1.3F, 1.7F, 1.625F)
                .texOffs(306, 285).addBox(-0.3F, -4.65F, -0.375F, 0.6F, 1.7F, 0.75F),
                PartPose.offsetAndRotation(0F, -4.35F, 4.05F, -0.6632F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(180, 140).addBox(-3.25F, -2.6F, -1.82F, 6.5F, 15.6F, 3.64F)
                .texOffs(450, 88).addBox(-1.82F, -2.52F, -3.25F, 3.64F, 15.44F, 6.5F)
                .texOffs(142, 241).addBox(-3.85F, -3.575F, -1.9F, 7.7F, 4.875F, 3.8F)
                .texOffs(33, 214).addBox(-1.9F, -3.495F, -3.85F, 3.8F, 4.715F, 7.7F),
                PartPose.offsetAndRotation(8F, 3F, -12F, 0.1047F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(302, 161).addBox(-2.5F, -1.4F, -1.4F, 5F, 14.1F, 2.8F)
                .texOffs(326, 140).addBox(-1.4F, -1.32F, -2.5F, 2.8F, 13.94F, 5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(110, 228).addBox(-3.25F, 0.78F, -6.3F, 6.5F, 1.04F, 9F)
                .texOffs(29, 228).addBox(-2.47F, 0F, -6.22F, 4.94F, 2.6F, 8.84F)
                .texOffs(117, 261).addBox(-3.3167F, 0.7F, -8.9F, 1.9F, 1.9F, 3.7F)
                .texOffs(206, 268).addBox(-3.0317F, 1.175F, -10.9F, 1.33F, 1.71F, 2.3F)
                .texOffs(130, 261).addBox(-0.95F, 0.7F, -8.9F, 1.9F, 1.9F, 3.7F)
                .texOffs(217, 268).addBox(-0.665F, 1.175F, -10.9F, 1.33F, 1.71F, 2.3F)
                .texOffs(143, 261).addBox(1.4167F, 0.7F, -8.9F, 1.9F, 1.9F, 3.7F)
                .texOffs(228, 268).addBox(1.7017F, 1.175F, -10.9F, 1.33F, 1.71F, 2.3F),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(392, 88).addBox(-4F, -3.2F, -2.99F, 8F, 17.7F, 5.98F)
                .texOffs(263, 88).addBox(-2.24F, -3.12F, -4.75F, 4.48F, 17.54F, 9.5F)
                .texOffs(351, 214).addBox(-4.6F, -4.4F, -2.95F, 9.2F, 6F, 5.9F)
                .texOffs(395, 161).addBox(-2.2F, -4.32F, -5.35F, 4.4F, 5.84F, 10.7F),
                PartPose.offsetAndRotation(8.5F, 2F, 11F, -0.1745F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(284, 140).addBox(-2.8F, -1.4F, -1.568F, 5.6F, 14.6F, 3.136F)
                .texOffs(198, 117).addBox(-1.568F, -1.32F, -2.8F, 3.136F, 14.44F, 5.6F),
                PartPose.offsetAndRotation(0F, 13.9F, 0F, 0.5236F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(215, 214).addBox(-3.5F, 0.84F, -7F, 7F, 1.12F, 10F)
                .texOffs(450, 198).addBox(-2.66F, 0F, -6.92F, 5.32F, 2.8F, 9.84F)
                .texOffs(325, 252).addBox(-3.4833F, 0.9F, -10F, 1.9F, 1.9F, 4.1F)
                .texOffs(239, 268).addBox(-3.1983F, 1.375F, -12.2F, 1.33F, 1.71F, 2.5F)
                .texOffs(340, 252).addBox(-0.95F, 0.9F, -10F, 1.9F, 1.9F, 4.1F)
                .texOffs(250, 268).addBox(-0.665F, 1.375F, -12.2F, 1.33F, 1.71F, 2.5F)
                .texOffs(355, 252).addBox(1.5833F, 0.9F, -10F, 1.9F, 1.9F, 4.1F)
                .texOffs(261, 268).addBox(1.8683F, 1.375F, -12.2F, 1.33F, 1.71F, 2.5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(203, 140).addBox(-3.25F, -2.6F, -1.82F, 6.5F, 15.6F, 3.64F)
                .texOffs(473, 88).addBox(-1.82F, -2.52F, -3.25F, 3.64F, 15.44F, 6.5F)
                .texOffs(167, 241).addBox(-3.85F, -3.575F, -1.9F, 7.7F, 4.875F, 3.8F)
                .texOffs(58, 214).addBox(-1.9F, -3.495F, -3.85F, 3.8F, 4.715F, 7.7F),
                PartPose.offsetAndRotation(-8F, 3F, -12F, 0.1047F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(319, 161).addBox(-2.5F, -1.4F, -1.4F, 5F, 14.1F, 2.8F)
                .texOffs(343, 140).addBox(-1.4F, -1.32F, -2.5F, 2.8F, 13.94F, 5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(143, 228).addBox(-3.25F, 0.78F, -6.3F, 6.5F, 1.04F, 9F)
                .texOffs(58, 228).addBox(-2.47F, 0F, -6.22F, 4.94F, 2.6F, 8.84F)
                .texOffs(156, 261).addBox(-3.3167F, 0.7F, -8.9F, 1.9F, 1.9F, 3.7F)
                .texOffs(272, 268).addBox(-3.0317F, 1.175F, -10.9F, 1.33F, 1.71F, 2.3F)
                .texOffs(169, 261).addBox(-0.95F, 0.7F, -8.9F, 1.9F, 1.9F, 3.7F)
                .texOffs(283, 268).addBox(-0.665F, 1.175F, -10.9F, 1.33F, 1.71F, 2.3F)
                .texOffs(182, 261).addBox(1.4167F, 0.7F, -8.9F, 1.9F, 1.9F, 3.7F)
                .texOffs(294, 268).addBox(1.7017F, 1.175F, -10.9F, 1.33F, 1.71F, 2.3F),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(421, 88).addBox(-4F, -3.2F, -2.99F, 8F, 17.7F, 5.98F)
                .texOffs(294, 88).addBox(-2.24F, -3.12F, -4.75F, 4.48F, 17.54F, 9.5F)
                .texOffs(384, 214).addBox(-4.6F, -4.4F, -2.95F, 9.2F, 6F, 5.9F)
                .texOffs(428, 161).addBox(-2.2F, -4.32F, -5.35F, 4.4F, 5.84F, 10.7F),
                PartPose.offsetAndRotation(-8.5F, 2F, 11F, -0.1745F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(305, 140).addBox(-2.8F, -1.4F, -1.568F, 5.6F, 14.6F, 3.136F)
                .texOffs(219, 117).addBox(-1.568F, -1.32F, -2.8F, 3.136F, 14.44F, 5.6F),
                PartPose.offsetAndRotation(0F, 13.9F, 0F, 0.5236F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(250, 214).addBox(-3.5F, 0.84F, -7F, 7F, 1.12F, 10F)
                .texOffs(0, 214).addBox(-2.66F, 0F, -6.92F, 5.32F, 2.8F, 9.84F)
                .texOffs(370, 252).addBox(-3.4833F, 0.9F, -10F, 1.9F, 1.9F, 4.1F)
                .texOffs(305, 268).addBox(-3.1983F, 1.375F, -12.2F, 1.33F, 1.71F, 2.5F)
                .texOffs(385, 252).addBox(-0.95F, 0.9F, -10F, 1.9F, 1.9F, 4.1F)
                .texOffs(316, 268).addBox(-0.665F, 1.375F, -12.2F, 1.33F, 1.71F, 2.5F)
                .texOffs(400, 252).addBox(1.5833F, 0.9F, -10F, 1.9F, 1.9F, 4.1F)
                .texOffs(327, 268).addBox(1.8683F, 1.375F, -12.2F, 1.33F, 1.71F, 2.5F),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(459, 241).addBox(-1F, -2.3F, -1.012F, 15.3F, 4.6F, 2.024F)
                .texOffs(373, 241).addBox(-0.92F, -1.012F, -2.3F, 15.14F, 2.024F, 4.6F)
                .texOffs(70, 268).addBox(13F, -1.44F, -0.6336F, 13.8F, 2.88F, 1.2672F)
                .texOffs(0, 268).addBox(13.08F, -0.6336F, -1.44F, 13.64F, 1.2672F, 2.88F)
                .texOffs(0, 88).addBox(0F, 0F, 0.94F, 26F, 0F, 28F),
                PartPose.offset(8.5F, -9F, -10F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(347, 261).addBox(-0.6F, -1.404F, -0.6178F, 16.1F, 2.808F, 1.2355F)
                .texOffs(386, 261).addBox(-0.52F, -0.6178F, -1.404F, 15.94F, 1.2355F, 2.808F)
                .texOffs(82, 285).addBox(13.95F, -0.99F, -0.4356F, 17.65F, 1.98F, 0.8712F)
                .texOffs(0, 285).addBox(14.03F, -0.4356F, -0.99F, 17.49F, 0.8712F, 1.98F)
                .texOffs(267, 54).addBox(0F, 0F, 0.49F, 31F, 0F, 30F),
                PartPose.offset(26F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(195, 261).addBox(-1.71F, -1.71F, -0.855F, 3.42F, 3.42F, 1.71F)
                .texOffs(457, 252).addBox(-1.6416F, -0.855F, -1.71F, 3.2832F, 1.71F, 3.42F)
                .texOffs(208, 261).addBox(-0.9F, -0.9F, -4.91F, 1.8F, 1.8F, 3.4F)
                .texOffs(338, 268).addBox(-0.6F, -0.5F, -7.11F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(31F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(30, 274).addBox(0F, -1F, -1F, 28.08F, 2F, 2F)
                .texOffs(156, 274).addBox(27F, -0.6F, -0.6F, 27F, 1.2F, 1.2F)
                .texOffs(240, 117).addBox(0F, 0F, 0.3F, 54F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1396F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(274, 274).addBox(0F, -0.85F, -0.85F, 25.48F, 1.7F, 1.7F)
                .texOffs(388, 274).addBox(24.5F, -0.51F, -0.51F, 24.5F, 1.02F, 1.02F)
                .texOffs(360, 140).addBox(0F, 0F, 0.21F, 49F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5236F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(0, 280).addBox(0F, -0.7F, -0.7F, 22.36F, 1.4F, 1.4F)
                .texOffs(311, 285).addBox(21.5F, -0.42F, -0.42F, 21.5F, 0.84F, 0.84F)
                .texOffs(190, 180).addBox(0F, 0F, 0.12F, 43F, 0F, 15F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9076F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(102, 280).addBox(0F, -0.55F, -0.55F, 18.2F, 1.1F, 1.1F)
                .texOffs(405, 285).addBox(17.5F, -0.35F, -0.35F, 17.5F, 0.7F, 0.7F)
                .texOffs(275, 0).addBox(0F, 0F, 0.05F, 35F, 0F, 33F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.2915F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 252).addBox(-14.3F, -2.3F, -1.012F, 15.3F, 4.6F, 2.024F)
                .texOffs(416, 241).addBox(-14.22F, -1.012F, -2.3F, 15.14F, 2.024F, 4.6F)
                .texOffs(103, 268).addBox(-26.8F, -1.44F, -0.6336F, 13.8F, 2.88F, 1.2672F)
                .texOffs(35, 268).addBox(-26.72F, -0.6336F, -1.44F, 13.64F, 1.2672F, 2.88F)
                .texOffs(109, 88).addBox(-26F, 0F, 0.94F, 26F, 0F, 28F),
                PartPose.offset(-8.5F, -9F, -10F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(425, 261).addBox(-15.5F, -1.404F, -0.6178F, 16.1F, 2.808F, 1.2355F)
                .texOffs(464, 261).addBox(-15.42F, -0.6178F, -1.404F, 15.94F, 1.2355F, 2.808F)
                .texOffs(121, 285).addBox(-31.6F, -0.99F, -0.4356F, 17.65F, 1.98F, 0.8712F)
                .texOffs(41, 285).addBox(-31.52F, -0.4356F, -0.99F, 17.49F, 0.8712F, 1.98F)
                .texOffs(390, 54).addBox(-31F, 0F, 0.49F, 31F, 0F, 30F),
                PartPose.offset(-26F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(221, 261).addBox(-1.71F, -1.71F, -0.855F, 3.42F, 3.42F, 1.71F)
                .texOffs(474, 252).addBox(-1.6416F, -0.855F, -1.71F, 3.2832F, 1.71F, 3.42F)
                .texOffs(234, 261).addBox(-0.9F, -0.9F, -4.91F, 1.8F, 1.8F, 3.4F)
                .texOffs(349, 268).addBox(-0.6F, -0.5F, -7.11F, 1.2F, 1.5F, 2.4F),
                PartPose.offset(-31F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(93, 274).addBox(-28.08F, -1F, -1F, 28.08F, 2F, 2F)
                .texOffs(215, 274).addBox(-54F, -0.6F, -0.6F, 27F, 1.2F, 1.2F)
                .texOffs(0, 140).addBox(-54F, 0F, 0.3F, 54F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1396F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(331, 274).addBox(-25.48F, -0.85F, -0.85F, 25.48F, 1.7F, 1.7F)
                .texOffs(443, 274).addBox(-49F, -0.51F, -0.51F, 24.5F, 1.02F, 1.02F)
                .texOffs(0, 161).addBox(-49F, 0F, 0.21F, 49F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(51, 280).addBox(-22.36F, -0.7F, -0.7F, 22.36F, 1.4F, 1.4F)
                .texOffs(358, 285).addBox(-43F, -0.42F, -0.42F, 21.5F, 0.84F, 0.84F)
                .texOffs(307, 180).addBox(-43F, 0F, 0.12F, 43F, 0F, 15F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9076F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(145, 280).addBox(-18.2F, -0.55F, -0.55F, 18.2F, 1.1F, 1.1F)
                .texOffs(444, 285).addBox(-35F, -0.35F, -0.35F, 17.5F, 0.7F, 0.7F)
                .texOffs(0, 54).addBox(-35F, 0F, 0.05F, 35F, 0F, 33F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.2915F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
