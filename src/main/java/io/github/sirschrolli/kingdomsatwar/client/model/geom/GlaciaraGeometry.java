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
                .texOffs(93, 0).addBox(-8.5F, -5.5F, -18F, 17F, 10F, 36F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(0, 0).addBox(-5F, -9F, -18F, 10F, 17F, 36F, new CubeDeformation(-0.3F, 0F, -0.08F))
                .texOffs(325, 88).addBox(-9.5F, -6F, -19F, 19F, 11F, 14F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(137, 54).addBox(-5.5F, -10F, -19F, 11F, 19F, 14F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(106, 117).addBox(-9F, -6F, 6F, 18F, 10F, 11F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(218, 88).addBox(-5.5F, -9.5F, 6F, 11F, 17F, 11F, new CubeDeformation(-0.3F, 0F, -0.08F))
                .texOffs(188, 54).addBox(-4.5F, -10.75F, -16F, 9F, 1F, 30F, new CubeDeformation(0F, -0.05F, 0F))
                .texOffs(200, 0).addBox(-3.5F, -12.25F, -16F, 7F, 4F, 30F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(479, 214).addBox(6.25F, -10.25F, -14F, 5F, 3F, 9F, new CubeDeformation(-0.25F, -0.35F, 0F))
                .texOffs(79, 198).addBox(7.75F, -11.75F, -14F, 2F, 6F, 9F, new CubeDeformation(-0.35F, -0.25F, -0.08F))
                .texOffs(0, 228).addBox(-11.25F, -10.25F, -14F, 5F, 3F, 9F, new CubeDeformation(-0.25F, -0.35F, 0F))
                .texOffs(102, 198).addBox(-9.75F, -11.75F, -14F, 2F, 6F, 9F, new CubeDeformation(-0.35F, -0.25F, -0.08F))
                .texOffs(336, 161).addBox(-7F, 8F, -18F, 14F, 2F, 15F)
                .texOffs(0, 117).addBox(-6.5F, 6.5F, -4F, 13F, 2F, 20F),
                PartPose.offset(0F, -10F, 0F));
        PartDefinition p_back_crystal1 = p_body.addOrReplaceChild("back_crystal1", CubeListBuilder.create()
                .texOffs(186, 252).addBox(-1.5F, -2.911F, -2F, 3F, 4F, 4F, new CubeDeformation(-0.2F, -0.489F, -0.375F))
                .texOffs(247, 261).addBox(-1F, -4.754F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.155F, -0.418F, -0.4437F))
                .texOffs(391, 280).addBox(-0.5F, -6.668F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.11F, -0.418F, -0.0125F)),
                PartPose.offsetAndRotation(0F, -11.6F, -14F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal1_s0 = p_body.addOrReplaceChild("back_crystal1_s0", CubeListBuilder.create()
                .texOffs(188, 280).addBox(-1F, -1.344F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.056F, -0.025F))
                .texOffs(396, 280).addBox(-0.5F, -3.794F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.244F, -0.2075F)),
                PartPose.offsetAndRotation(0.9F, -11.3F, -14.8F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal1_s1 = p_body.addOrReplaceChild("back_crystal1_s1", CubeListBuilder.create()
                .texOffs(197, 280).addBox(-1F, -1.344F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.056F, -0.025F))
                .texOffs(401, 280).addBox(-0.5F, -3.794F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.244F, -0.2075F)),
                PartPose.offsetAndRotation(-0.9F, -11.3F, -13.2F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal2 = p_body.addOrReplaceChild("back_crystal2", CubeListBuilder.create()
                .texOffs(201, 252).addBox(-1.5F, -3.331F, -2F, 3F, 4F, 4F, new CubeDeformation(-0.2F, -0.069F, -0.375F))
                .texOffs(258, 261).addBox(-1F, -5.884F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.155F, -0.128F, -0.4437F))
                .texOffs(406, 280).addBox(-0.5F, -8.378F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.11F, -0.128F, -0.0125F)),
                PartPose.offsetAndRotation(0F, -11.6F, -8F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal2_s0 = p_body.addOrReplaceChild("back_crystal2_s0", CubeListBuilder.create()
                .texOffs(360, 268).addBox(-1F, -2.075F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.325F, -0.025F))
                .texOffs(486, 268).addBox(-0.5F, -5.075F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.2075F)),
                PartPose.offsetAndRotation(0.9F, -11.3F, -8.8F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal2_s1 = p_body.addOrReplaceChild("back_crystal2_s1", CubeListBuilder.create()
                .texOffs(369, 268).addBox(-1F, -2.075F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.325F, -0.025F))
                .texOffs(491, 268).addBox(-0.5F, -5.075F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.2075F)),
                PartPose.offsetAndRotation(-0.9F, -11.3F, -7.2F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal3 = p_body.addOrReplaceChild("back_crystal3", CubeListBuilder.create()
                .texOffs(345, 241).addBox(-1.5F, -4.041F, -2F, 3F, 5F, 4F, new CubeDeformation(-0.2F, -0.359F, -0.375F))
                .texOffs(415, 252).addBox(-1F, -6.949F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.155F, -0.483F, -0.4437F))
                .texOffs(496, 268).addBox(-0.5F, -9.733F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.11F, -0.483F, -0.0125F)),
                PartPose.offsetAndRotation(0F, -11.6F, -2F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal3_s0 = p_body.addOrReplaceChild("back_crystal3_s0", CubeListBuilder.create()
                .texOffs(378, 268).addBox(-1F, -2.1905F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.2095F, -0.025F))
                .texOffs(501, 268).addBox(-0.5F, -5.4655F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.2655F, -0.2075F)),
                PartPose.offsetAndRotation(0.9F, -11.3F, -2.8F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal3_s1 = p_body.addOrReplaceChild("back_crystal3_s1", CubeListBuilder.create()
                .texOffs(387, 268).addBox(-1F, -2.1905F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.2095F, -0.025F))
                .texOffs(506, 268).addBox(-0.5F, -5.4655F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.2655F, -0.2075F)),
                PartPose.offsetAndRotation(-0.9F, -11.3F, -1.2F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal4 = p_body.addOrReplaceChild("back_crystal4", CubeListBuilder.create()
                .texOffs(216, 252).addBox(-1.5F, -3.331F, -2F, 3F, 4F, 4F, new CubeDeformation(-0.2F, -0.069F, -0.375F))
                .texOffs(269, 261).addBox(-1F, -5.884F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.155F, -0.128F, -0.4437F))
                .texOffs(411, 280).addBox(-0.5F, -8.378F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.11F, -0.128F, -0.0125F)),
                PartPose.offsetAndRotation(0F, -11.6F, 4F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal4_s0 = p_body.addOrReplaceChild("back_crystal4_s0", CubeListBuilder.create()
                .texOffs(396, 268).addBox(-1F, -2.075F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.325F, -0.025F))
                .texOffs(0, 274).addBox(-0.5F, -5.075F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.2075F)),
                PartPose.offsetAndRotation(0.9F, -11.3F, 3.2F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal4_s1 = p_body.addOrReplaceChild("back_crystal4_s1", CubeListBuilder.create()
                .texOffs(405, 268).addBox(-1F, -2.075F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.325F, -0.025F))
                .texOffs(5, 274).addBox(-0.5F, -5.075F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.425F, -0.2075F)),
                PartPose.offsetAndRotation(-0.9F, -11.3F, 4.8F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_back_crystal5 = p_body.addOrReplaceChild("back_crystal5", CubeListBuilder.create()
                .texOffs(231, 252).addBox(-1.5F, -3.016F, -2F, 3F, 4F, 4F, new CubeDeformation(-0.2F, -0.384F, -0.375F))
                .texOffs(280, 261).addBox(-1F, -5.0365F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.155F, -0.3455F, -0.4437F))
                .texOffs(416, 280).addBox(-0.5F, -7.0955F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.11F, -0.3455F, -0.0125F)),
                PartPose.offsetAndRotation(0F, -11.6F, 10F, -0.384F, 0F, 0F));
        PartDefinition p_back_crystal5_s0 = p_body.addOrReplaceChild("back_crystal5_s0", CubeListBuilder.create()
                .texOffs(414, 268).addBox(-1F, -1.9017F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.4983F, -0.025F))
                .texOffs(421, 280).addBox(-0.5F, -3.9892F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1643F, -0.2075F)),
                PartPose.offsetAndRotation(0.9F, -11.3F, 9.2F, -0.5236F, 0F, 0.4189F));
        PartDefinition p_back_crystal5_s1 = p_body.addOrReplaceChild("back_crystal5_s1", CubeListBuilder.create()
                .texOffs(423, 268).addBox(-1F, -1.9017F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.22F, -0.4983F, -0.025F))
                .texOffs(426, 280).addBox(-0.5F, -3.9892F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1643F, -0.2075F)),
                PartPose.offsetAndRotation(-0.9F, -11.3F, 10.8F, -0.5236F, 0F, -0.4189F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(267, 198).addBox(-5.5F, -2.5F, -7.25F, 11F, 5F, 8F, new CubeDeformation(0F, -0.3F, -0.15F))
                .texOffs(257, 140).addBox(-2.5F, -5.5F, -7.25F, 5F, 11F, 8F, new CubeDeformation(-0.3F, 0F, -0.23F))
                .texOffs(117, 241).addBox(-2.5F, 4.3F, -6.75F, 5F, 2F, 7F, new CubeDeformation(-0.3F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -6F, -17F, -0.5934F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(106, 214).addBox(-5F, -2F, -7.1F, 10F, 4F, 8F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(252, 161).addBox(-2F, -5F, -7.1F, 4F, 10F, 8F, new CubeDeformation(0F, 0F, -0.38F))
                .texOffs(192, 241).addBox(-2F, 3.8F, -6.6F, 4F, 2F, 7F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, -0.2793F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(143, 214).addBox(-5F, -2F, -7F, 10F, 4F, 8F, new CubeDeformation(-0.4F, -0.16F, -0.4F))
                .texOffs(277, 161).addBox(-2F, -5F, -7F, 4F, 10F, 8F, new CubeDeformation(-0.16F, -0.4F, -0.48F))
                .texOffs(66, 252).addBox(-2F, 3.4F, -6F, 4F, 2F, 6F, new CubeDeformation(-0.16F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6.2F, -0.1396F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(180, 214).addBox(-4.5F, -2F, -7F, 9F, 4F, 8F, new CubeDeformation(-0.25F, -0.3F, -0.4F))
                .texOffs(0, 180).addBox(-2F, -4.5F, -7F, 4F, 9F, 8F, new CubeDeformation(-0.3F, -0.25F, -0.48F))
                .texOffs(87, 252).addBox(-2F, 3.05F, -6F, 4F, 2F, 6F, new CubeDeformation(-0.3F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6F, -0.0349F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(176, 228).addBox(-4F, -2F, -6.4F, 8F, 4F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(125, 198).addBox(-2F, -4F, -6.4F, 4F, 8F, 7F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(108, 252).addBox(-2F, 2.8F, -5.9F, 4F, 2F, 6F, new CubeDeformation(-0.4F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.0698F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(452, 228).addBox(-4F, -1.5F, -6.3F, 8F, 3F, 7F, new CubeDeformation(-0.25F, 0F, -0.1F))
                .texOffs(148, 198).addBox(-1.5F, -4F, -6.3F, 3F, 8F, 7F, new CubeDeformation(0F, -0.25F, -0.18F))
                .texOffs(129, 252).addBox(-1.5F, 2.55F, -5.8F, 3F, 2F, 6F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, -5.8F, 0.1571F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(483, 228).addBox(-3.5F, -1.5F, -6.25F, 7F, 3F, 7F, new CubeDeformation(0F, -0.1F, -0.15F))
                .texOffs(225, 198).addBox(-1.5F, -3.5F, -6.25F, 3F, 7F, 7F, new CubeDeformation(-0.1F, 0F, -0.23F))
                .texOffs(148, 252).addBox(-1.5F, 2.3F, -5.75F, 3F, 2F, 6F, new CubeDeformation(-0.1F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -5.6F, 0.2094F, 0F, 0F));
        PartDefinition p_neck8 = p_neck7.addOrReplaceChild("neck8", CubeListBuilder.create()
                .texOffs(0, 241).addBox(-3.5F, -1.5F, -6.2F, 7F, 3F, 7F, new CubeDeformation(-0.2F, -0.18F, -0.2F))
                .texOffs(246, 198).addBox(-1.5F, -3.5F, -6.2F, 3F, 7F, 7F, new CubeDeformation(-0.18F, -0.2F, -0.28F))
                .texOffs(167, 252).addBox(-1.5F, 2.1F, -5.7F, 3F, 2F, 6F, new CubeDeformation(-0.18F, -0.2F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.1571F, 0F, 0F));
        PartDefinition p_head = p_neck8.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(135, 161).addBox(-5F, -2.8F, -11.75F, 10F, 5F, 13F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(67, 117).addBox(-3F, -4.8F, -11.75F, 6F, 9F, 13F, new CubeDeformation(-0.2F, -0.2F, -0.33F))
                .texOffs(39, 252).addBox(-4F, -4.7F, -14F, 8F, 3F, 5F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(285, 214).addBox(-4F, -1.8F, -18.25F, 8F, 4F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(0, 198).addBox(-2.5F, -3.3F, -18.25F, 5F, 7F, 8F, new CubeDeformation(-0.3F, -0.1F, -0.33F))
                .texOffs(417, 214).addBox(-3.5F, -1.6F, -25.25F, 7F, 4F, 8F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(54, 198).addBox(-2F, -3.1F, -25.25F, 4F, 7F, 8F, new CubeDeformation(-0.1F, -0.3F, -0.33F))
                .texOffs(169, 198).addBox(-2F, -4.2F, -23.5F, 4F, 2F, 12F, new CubeDeformation(-0.2F, -0.3F, 0F))
                .texOffs(136, 268).addBox(-3F, -2.7F, -25.8F, 6F, 4F, 1F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(483, 285).addBox(0.45F, -2.25F, -26.25F, 2F, 1F, 1F, new CubeDeformation(-0.45F, -0.15F, -0.25F))
                .texOffs(491, 252).addBox(3.375F, -3F, -12.2F, 2F, 2F, 4F, new CubeDeformation(-0.225F, -0.2F, -0.3F))
                .texOffs(176, 285).addBox(2.5F, 3.3425F, -24.1786F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(181, 285).addBox(2.5F, 3.15F, -22.5357F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(186, 285).addBox(2.5F, 3.15F, -20.8929F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(191, 285).addBox(2.5F, 3.15F, -19.25F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(196, 285).addBox(2.5F, 3.15F, -17.6071F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(201, 285).addBox(2.5F, 3.15F, -15.9643F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(206, 285).addBox(2.5F, 3.3425F, -14.3214F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(431, 280).addBox(2.3F, 3.2F, -23.25F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(490, 285).addBox(-2.45F, -2.25F, -26.25F, 2F, 1F, 1F, new CubeDeformation(-0.45F, -0.15F, -0.25F))
                .texOffs(0, 261).addBox(-5.375F, -3F, -12.2F, 2F, 2F, 4F, new CubeDeformation(-0.225F, -0.2F, -0.3F))
                .texOffs(211, 285).addBox(-3.5F, 3.3425F, -24.1786F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(216, 285).addBox(-3.5F, 3.15F, -22.5357F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(221, 285).addBox(-3.5F, 3.15F, -20.8929F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(226, 285).addBox(-3.5F, 3.15F, -19.25F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(231, 285).addBox(-3.5F, 3.15F, -17.6071F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(236, 285).addBox(-3.5F, 3.15F, -15.9643F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(241, 285).addBox(-3.5F, 3.3425F, -14.3214F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(436, 280).addBox(-3.3F, 3.2F, -23.25F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(497, 285).addBox(-2F, 3.55F, -25.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(502, 285).addBox(-1F, 3.55F, -25.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(507, 285).addBox(0F, 3.55F, -25.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(0, 289).addBox(1F, 3.55F, -25.55F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.15F))
                .texOffs(306, 198).addBox(-3F, 3.27F, -24.55F, 6F, 1F, 12F, new CubeDeformation(-0.3F, -0.35F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -5.5F, 0.5585F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(215, 241).addBox(-2F, -1.1F, -0.5F, 4F, 2F, 7F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(3.4F, -4F, -11.6F, 0.0873F, 0F, -0.1745F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(432, 268).addBox(-1F, -2.117F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.283F, -0.1875F))
                .texOffs(10, 274).addBox(-0.5F, -5.217F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.367F, -0.25F)),
                PartPose.offsetAndRotation(4.6F, -0.5F, -6F, -1.7453F, 0.6109F, 0F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(441, 268).addBox(-1F, -1.949F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.451F, -0.1875F))
                .texOffs(441, 280).addBox(-0.5F, -4.149F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.099F, -0.25F)),
                PartPose.offsetAndRotation(4.6F, 1.5F, -4.5F, -1.7453F, 0.6109F, 0F));
        PartDefinition p_frill_l3 = p_head.addOrReplaceChild("frill_l3", CubeListBuilder.create()
                .texOffs(206, 280).addBox(-1F, -1.239F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.161F, -0.1875F))
                .texOffs(446, 280).addBox(-0.5F, -3.439F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.389F, -0.25F)),
                PartPose.offsetAndRotation(4.6F, 3.2F, -3F, -1.7453F, 0.6109F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(295, 252).addBox(-1.5F, -1.5F, -0.4125F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.0125F))
                .texOffs(13, 261).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.194F, -0.194F, -0.25F)),
                PartPose.offsetAndRotation(3F, -4.2F, -3.5F, 0.3142F, 0.2793F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(26, 261).addBox(-1F, -1F, -0.55F, 2F, 2F, 4F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(39, 261).addBox(-1F, -1F, 2.625F, 2F, 2F, 4F, new CubeDeformation(-0.38F, -0.38F, -0.375F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.0698F, 0.0698F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(52, 261).addBox(-1F, -1F, -0.6875F, 2F, 2F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.2875F))
                .texOffs(215, 280).addBox(-0.5F, -0.5F, 2.75F, 1F, 1F, 3F, new CubeDeformation(-0.035F, -0.035F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.0698F, 0.0349F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(224, 280).addBox(-0.5F, -0.5F, -0.4625F, 1F, 1F, 3F, new CubeDeformation(0F, 0F, -0.0625F))
                .texOffs(233, 280).addBox(-0.5F, -0.5F, 2F, 1F, 1F, 3F, new CubeDeformation(-0.19F, -0.19F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, 0.1047F, 0F, 0F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(238, 241).addBox(-2F, -1.1F, -0.5F, 4F, 2F, 7F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(-3.4F, -4F, -11.6F, 0.0873F, 0F, 0.1745F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(450, 268).addBox(-1F, -2.117F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.283F, -0.1875F))
                .texOffs(15, 274).addBox(-0.5F, -5.217F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.25F, -0.367F, -0.25F)),
                PartPose.offsetAndRotation(-4.6F, -0.5F, -6F, -1.7453F, -0.6109F, 0F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(459, 268).addBox(-1F, -1.949F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, -0.451F, -0.1875F))
                .texOffs(451, 280).addBox(-0.5F, -4.149F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.099F, -0.25F)),
                PartPose.offsetAndRotation(-4.6F, 1.5F, -4.5F, -1.7453F, -0.6109F, 0F));
        PartDefinition p_frill_r3 = p_head.addOrReplaceChild("frill_r3", CubeListBuilder.create()
                .texOffs(242, 280).addBox(-1F, -1.239F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.161F, -0.1875F))
                .texOffs(456, 280).addBox(-0.5F, -3.439F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.389F, -0.25F)),
                PartPose.offsetAndRotation(-4.6F, 3.2F, -3F, -1.7453F, -0.6109F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(310, 252).addBox(-1.5F, -1.5F, -0.4125F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.0125F))
                .texOffs(65, 261).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.194F, -0.194F, -0.25F)),
                PartPose.offsetAndRotation(-3F, -4.2F, -3.5F, 0.3142F, -0.2793F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(78, 261).addBox(-1F, -1F, -0.55F, 2F, 2F, 4F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(91, 261).addBox(-1F, -1F, 2.625F, 2F, 2F, 4F, new CubeDeformation(-0.38F, -0.38F, -0.375F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.0698F, -0.0698F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(104, 261).addBox(-1F, -1F, -0.6875F, 2F, 2F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.2875F))
                .texOffs(251, 280).addBox(-0.5F, -0.5F, 2.75F, 1F, 1F, 3F, new CubeDeformation(-0.035F, -0.035F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.0698F, -0.0349F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(260, 280).addBox(-0.5F, -0.5F, -0.4625F, 1F, 1F, 3F, new CubeDeformation(0F, 0F, -0.0625F))
                .texOffs(269, 280).addBox(-0.5F, -0.5F, 2F, 1F, 1F, 3F, new CubeDeformation(-0.19F, -0.19F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, 0.1047F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -4.4F, -6F, -0.4887F, 0F, 0F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(426, 252).addBox(-1F, -3.121F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.15F, -0.279F, -0.4375F))
                .texOffs(468, 268).addBox(-1F, -5.319F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4475F, -0.273F, -0.3094F))
                .texOffs(461, 280).addBox(-0.5F, -7.523F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.245F, -0.273F, -0.1812F)),
                PartPose.offsetAndRotation(-4F, 0.3F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(246, 252).addBox(-1F, -4.251F, -1.5F, 2F, 5F, 3F, new CubeDeformation(0F, -0.149F, -0.25F))
                .texOffs(324, 261).addBox(-1F, -7.514F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.35F, -0.338F, -0.1875F))
                .texOffs(20, 274).addBox(-0.5F, -10.588F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.338F, -0.125F)),
                PartPose.offsetAndRotation(-2.1F, 0.3F, 0F, 0F, 0F, 0.1283F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(360, 241).addBox(-1.5F, -5.381F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.3F, -0.019F, 0F))
                .texOffs(448, 252).addBox(-1F, -9.709F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.22F, -0.403F, -0.025F))
                .texOffs(342, 261).addBox(-0.5F, -13.653F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.14F, -0.403F, -0.05F)),
                PartPose.offset(0F, 0.3F, 0F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(257, 252).addBox(-1F, -4.251F, -1.5F, 2F, 5F, 3F, new CubeDeformation(0F, -0.149F, -0.25F))
                .texOffs(333, 261).addBox(-1F, -7.514F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.35F, -0.338F, -0.1875F))
                .texOffs(25, 274).addBox(-0.5F, -10.588F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.338F, -0.125F)),
                PartPose.offsetAndRotation(2.1F, 0.3F, 0F, 0F, 0F, -0.1283F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(437, 252).addBox(-1F, -3.121F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.15F, -0.279F, -0.4375F))
                .texOffs(477, 268).addBox(-1F, -5.319F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4475F, -0.273F, -0.3094F))
                .texOffs(466, 280).addBox(-0.5F, -7.523F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.245F, -0.273F, -0.1812F)),
                PartPose.offsetAndRotation(4F, 0.3F, 0F, 0F, 0F, -0.2443F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(25, 180).addBox(-3.5F, 0.9F, -14.8F, 7F, 1F, 15F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(182, 161).addBox(-2.5F, -0.1F, -14.8F, 5F, 3F, 15F, new CubeDeformation(-0.3F, -0.1F, -0.08F))
                .texOffs(268, 252).addBox(-4F, 0.4F, -3.25F, 8F, 2F, 5F, new CubeDeformation(0F, -0.3F, -0.25F))
                .texOffs(261, 241).addBox(-3F, -0.6F, -3.25F, 6F, 4F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.33F))
                .texOffs(246, 285).addBox(2.2F, -1.575F, -14.1214F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.325F, -0.15F))
                .texOffs(5, 289).addBox(2.2F, -0.9F, -12.5643F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(10, 289).addBox(2.2F, -0.9F, -11.0071F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(15, 289).addBox(2.2F, -0.9F, -9.45F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(20, 289).addBox(2.2F, -0.9F, -7.8929F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(25, 289).addBox(2.2F, -0.9F, -6.3357F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(251, 285).addBox(2.2F, -1.575F, -4.7786F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.325F, -0.15F))
                .texOffs(256, 285).addBox(-3.2F, -1.575F, -14.1214F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.325F, -0.15F))
                .texOffs(30, 289).addBox(-3.2F, -0.9F, -12.5643F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(35, 289).addBox(-3.2F, -0.9F, -11.0071F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(40, 289).addBox(-3.2F, -0.9F, -9.45F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(45, 289).addBox(-3.2F, -0.9F, -7.8929F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(50, 289).addBox(-3.2F, -0.9F, -6.3357F, 1F, 1F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F))
                .texOffs(261, 285).addBox(-3.2F, -1.575F, -4.7786F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.325F, -0.15F))
                .texOffs(70, 180).addBox(-3F, -0.5F, -14.85F, 6F, 1F, 15F, new CubeDeformation(-0.2F, -0.35F, -0.25F))
                .texOffs(448, 214).addBox(-2F, -0.7F, -13.5F, 4F, 1F, 11F, new CubeDeformation(-0.1F, -0.25F, 0F))
                .texOffs(207, 228).addBox(-2.5F, 2.45F, -10F, 5F, 1F, 10F, new CubeDeformation(-0.1F, -0.15F, 0F)),
                PartPose.offsetAndRotation(0F, 3.6F, -10F, 0.0524F, 0F, 0F));
        PartDefinition p_chin_crystal = p_jaw.addOrReplaceChild("chin_crystal", CubeListBuilder.create()
                .texOffs(278, 280).addBox(-1F, -1.197F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.203F, -0.0625F))
                .texOffs(471, 280).addBox(-0.5F, -3.297F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.447F, -0.2187F)),
                PartPose.offsetAndRotation(0F, 2.6F, -8F, -2.618F, 0F, 0F));
        PartDefinition p_neck7_crystal = p_neck7.addOrReplaceChild("neck7_crystal", CubeListBuilder.create()
                .texOffs(151, 268).addBox(-1F, -1.0542F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.3458F, -0.4375F))
                .texOffs(160, 285).addBox(-1F, -1.4488F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4475F, -0.0096F, -0.3094F))
                .texOffs(55, 289).addBox(-0.5F, -2.1796F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.245F, -0.0096F, -0.1812F)),
                PartPose.offsetAndRotation(0F, -3.1F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_neck5_crystal = p_neck5.addOrReplaceChild("neck5_crystal", CubeListBuilder.create()
                .texOffs(162, 268).addBox(-1F, -1.1718F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.2282F, -0.4375F))
                .texOffs(287, 280).addBox(-1F, -2.2652F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4475F, -0.4284F, -0.3094F))
                .texOffs(266, 285).addBox(-0.5F, -3.1584F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.245F, -0.4284F, -0.1812F)),
                PartPose.offsetAndRotation(0F, -3.6F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_neck3_crystal = p_neck3.addOrReplaceChild("neck3_crystal", CubeListBuilder.create()
                .texOffs(173, 268).addBox(-1F, -1.2894F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.1106F, -0.4375F))
                .texOffs(296, 280).addBox(-1F, -2.5816F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4475F, -0.3472F, -0.3094F))
                .texOffs(271, 285).addBox(-0.5F, -3.6372F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.245F, -0.3472F, -0.1812F)),
                PartPose.offsetAndRotation(0F, -4.2F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_neck1_crystal = p_neck1.addOrReplaceChild("neck1_crystal", CubeListBuilder.create()
                .texOffs(291, 261).addBox(-1F, -1.907F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.15F, -0.493F, -0.4375F))
                .texOffs(305, 280).addBox(-1F, -2.898F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4475F, -0.266F, -0.3094F))
                .texOffs(276, 285).addBox(-0.5F, -4.116F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.245F, -0.266F, -0.1812F)),
                PartPose.offsetAndRotation(0F, -5.1F, -3F, -0.6109F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(424, 180).addBox(-5F, -2F, -1F, 10F, 4F, 11F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(165, 117).addBox(-2.5F, -5F, -1F, 5F, 10F, 11F, new CubeDeformation(-0.35F, -0.25F, -0.48F))
                .texOffs(238, 228).addBox(-2.5F, 3.55F, 0F, 5F, 2F, 9F, new CubeDeformation(-0.35F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -3F, 17F, -0.2094F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(467, 180).addBox(-4.5F, -2F, -1F, 9F, 4F, 11F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(149, 140).addBox(-2F, -4.5F, -1F, 4F, 9F, 11F, new CubeDeformation(-0.05F, -0.25F, -0.48F))
                .texOffs(296, 228).addBox(-2F, 3.05F, 0F, 4F, 2F, 9F, new CubeDeformation(-0.05F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(343, 198).addBox(-4F, -1.5F, -0.75F, 8F, 3F, 10F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(223, 161).addBox(-2F, -4F, -0.75F, 4F, 8F, 10F, new CubeDeformation(-0.25F, -0.25F, -0.23F))
                .texOffs(323, 228).addBox(-2F, 2.55F, -0.25F, 4F, 2F, 9F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.0873F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(380, 198).addBox(-3.5F, -1.5F, -0.75F, 7F, 3F, 10F, new CubeDeformation(0F, -0.18F, -0.15F))
                .texOffs(461, 161).addBox(-2F, -3.5F, -0.75F, 4F, 7F, 10F, new CubeDeformation(-0.48F, -0.2F, -0.23F))
                .texOffs(350, 228).addBox(-2F, 2.1F, -0.25F, 4F, 2F, 9F, new CubeDeformation(-0.48F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(415, 198).addBox(-3.5F, -1.5F, -1F, 7F, 3F, 10F, new CubeDeformation(-0.4F, -0.34F, -0.4F))
                .texOffs(113, 180).addBox(-1.5F, -3F, -1F, 3F, 6F, 10F, new CubeDeformation(-0.14F, -0.1F, -0.48F))
                .texOffs(29, 241).addBox(-1.5F, 1.7F, 0F, 3F, 2F, 8F, new CubeDeformation(-0.14F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(318, 214).addBox(-3F, -1F, -1F, 6F, 2F, 10F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(27, 198).addBox(-1.5F, -2.5F, -1F, 3F, 5F, 10F, new CubeDeformation(-0.3F, 0F, -0.48F))
                .texOffs(52, 241).addBox(-1.5F, 1.3F, 0F, 3F, 2F, 8F, new CubeDeformation(-0.3F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0349F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(267, 228).addBox(-2.5F, -1F, -0.75F, 5F, 2F, 9F, new CubeDeformation(-0.2F, -0.12F, -0.15F))
                .texOffs(202, 198).addBox(-1F, -2.5F, -0.75F, 2F, 5F, 9F, new CubeDeformation(-0.02F, -0.3F, -0.23F))
                .texOffs(75, 241).addBox(-1F, 1F, -0.25F, 2F, 2F, 8F, new CubeDeformation(-0.02F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0524F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(377, 228).addBox(-2F, -1F, -0.75F, 4F, 2F, 9F, new CubeDeformation(-0.1F, -0.28F, -0.15F))
                .texOffs(83, 214).addBox(-1F, -2F, -0.75F, 2F, 4F, 9F, new CubeDeformation(-0.18F, -0.2F, -0.23F))
                .texOffs(96, 241).addBox(-1F, 0.6F, -0.25F, 2F, 2F, 8F, new CubeDeformation(-0.18F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(404, 228).addBox(-2F, -1F, -1F, 4F, 2F, 9F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(87, 228).addBox(-1F, -1.5F, -1F, 2F, 3F, 9F, new CubeDeformation(-0.3F, 0F, -0.48F))
                .texOffs(307, 241).addBox(-1F, 0.3F, 0F, 2F, 2F, 7F, new CubeDeformation(-0.3F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(284, 241).addBox(-1.5F, -0.5F, -0.75F, 3F, 1F, 8F, new CubeDeformation(-0.2F, -0.02F, -0.15F))
                .texOffs(431, 228).addBox(-1F, -1.5F, -0.75F, 2F, 3F, 8F, new CubeDeformation(-0.42F, -0.3F, -0.23F))
                .texOffs(326, 241).addBox(-1F, 0F, -0.25F, 2F, 2F, 7F, new CubeDeformation(-0.42F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.0349F, 0F, 0F));
        PartDefinition p_tailfan_0 = p_tail10.addOrReplaceChild("tailfan_0", CubeListBuilder.create()
                .texOffs(140, 180).addBox(-0.5F, -2.5F, 0F, 1F, 5F, 11F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0F, -0.384F, 0F));
        PartDefinition p_tailfan_1 = p_tail10.addOrReplaceChild("tailfan_1", CubeListBuilder.create()
                .texOffs(226, 140).addBox(-0.5F, -2.5F, 0F, 1F, 5F, 14F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 6F));
        PartDefinition p_tailfan_2 = p_tail10.addOrReplaceChild("tailfan_2", CubeListBuilder.create()
                .texOffs(165, 180).addBox(-0.5F, -2.5F, 0F, 1F, 5F, 11F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0F, 0.384F, 0F));
        PartDefinition p_tail7_crystal = p_tail7.addOrReplaceChild("tail7_crystal", CubeListBuilder.create()
                .texOffs(314, 280).addBox(-1F, -1.008F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.36F, -0.392F, -0.2F))
                .texOffs(169, 285).addBox(-0.5F, -1.3245F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.084F, -0.0415F, -0.48F))
                .texOffs(60, 289).addBox(-0.5F, -1.9915F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.0415F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.8F, 3.375F, -0.6632F, 0F, 0F));
        PartDefinition p_tail6_crystal = p_tail6.addOrReplaceChild("tail6_crystal", CubeListBuilder.create()
                .texOffs(323, 280).addBox(-1F, -1.1025F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.2975F, -0.125F))
                .texOffs(377, 280).addBox(-0.5F, -2.0788F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.4762F, -0.4313F))
                .texOffs(281, 285).addBox(-0.5F, -2.8763F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.4762F, -0.2375F)),
                PartPose.offsetAndRotation(0F, -2.1F, 3.6F, -0.6632F, 0F, 0F));
        PartDefinition p_tail5_crystal = p_tail5.addOrReplaceChild("tail5_crystal", CubeListBuilder.create()
                .texOffs(332, 280).addBox(-1F, -1.197F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.24F, -0.203F, -0.05F))
                .texOffs(384, 280).addBox(-0.5F, -2.333F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.006F, -0.411F, -0.3825F))
                .texOffs(286, 285).addBox(-0.5F, -3.261F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.411F, -0.215F)),
                PartPose.offsetAndRotation(0F, -2.5F, 3.6F, -0.6632F, 0F, 0F));
        PartDefinition p_tail4_crystal = p_tail4.addOrReplaceChild("tail4_crystal", CubeListBuilder.create()
                .texOffs(184, 268).addBox(-1F, -1.2915F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.18F, -0.1085F, -0.475F))
                .texOffs(341, 280).addBox(-1F, -2.5873F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.467F, -0.3458F, -0.3337F))
                .texOffs(291, 285).addBox(-0.5F, -3.6458F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3458F, -0.1925F)),
                PartPose.offsetAndRotation(0F, -2.9F, 3.825F, -0.6632F, 0F, 0F));
        PartDefinition p_tail3_crystal = p_tail3.addOrReplaceChild("tail3_crystal", CubeListBuilder.create()
                .texOffs(195, 268).addBox(-1F, -1.386F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.12F, -0.014F, -0.4F))
                .texOffs(350, 280).addBox(-1F, -2.8415F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.428F, -0.2805F, -0.285F))
                .texOffs(296, 285).addBox(-0.5F, -4.0305F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.236F, -0.2805F, -0.17F)),
                PartPose.offsetAndRotation(0F, -3.35F, 3.825F, -0.6632F, 0F, 0F));
        PartDefinition p_tail2_crystal = p_tail2.addOrReplaceChild("tail2_crystal", CubeListBuilder.create()
                .texOffs(302, 261).addBox(-1F, -1.9805F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.06F, -0.4195F, -0.325F))
                .texOffs(359, 280).addBox(-1F, -3.0957F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.389F, -0.2153F, -0.2363F))
                .texOffs(301, 285).addBox(-0.5F, -4.4152F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.218F, -0.2153F, -0.1475F)),
                PartPose.offsetAndRotation(0F, -3.85F, 4.05F, -0.6632F, 0F, 0F));
        PartDefinition p_tail1_crystal = p_tail1.addOrReplaceChild("tail1_crystal", CubeListBuilder.create()
                .texOffs(313, 261).addBox(-1F, -2.075F, -1.5F, 2F, 3F, 3F, new CubeDeformation(0F, -0.325F, -0.25F))
                .texOffs(368, 280).addBox(-1F, -3.35F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.15F, -0.1875F))
                .texOffs(306, 285).addBox(-0.5F, -4.8F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.15F, -0.125F)),
                PartPose.offsetAndRotation(0F, -4.35F, 4.05F, -0.6632F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(180, 140).addBox(-3.5F, -2.8F, -2F, 7F, 16F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.18F))
                .texOffs(450, 88).addBox(-2F, -2.8F, -3.5F, 4F, 16F, 7F, new CubeDeformation(-0.18F, -0.28F, -0.25F))
                .texOffs(142, 241).addBox(-4F, -3.6375F, -2F, 8F, 5F, 4F, new CubeDeformation(-0.15F, -0.0625F, -0.1F))
                .texOffs(33, 214).addBox(-2F, -3.6375F, -4F, 4F, 5F, 8F, new CubeDeformation(-0.1F, -0.1425F, -0.15F)),
                PartPose.offsetAndRotation(8F, 3F, -12F, 0.1047F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(302, 161).addBox(-2.5F, -1.85F, -1.5F, 5F, 15F, 3F, new CubeDeformation(0F, -0.45F, -0.1F))
                .texOffs(326, 140).addBox(-1.5F, -1.35F, -2.5F, 3F, 14F, 5F, new CubeDeformation(-0.1F, -0.03F, 0F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(110, 228).addBox(-3.5F, 0.3F, -6.3F, 7F, 2F, 9F, new CubeDeformation(-0.25F, -0.48F, 0F))
                .texOffs(29, 228).addBox(-2.5F, -0.2F, -6.3F, 5F, 3F, 9F, new CubeDeformation(-0.03F, -0.2F, -0.08F))
                .texOffs(117, 261).addBox(-3.3667F, 0.65F, -9.05F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(206, 268).addBox(-3.3667F, 1.03F, -11.25F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(130, 261).addBox(-1F, 0.65F, -9.05F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(217, 268).addBox(-1F, 1.03F, -11.25F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(143, 261).addBox(1.3667F, 0.65F, -9.05F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(228, 268).addBox(1.3667F, 1.03F, -11.25F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F)),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(392, 88).addBox(-4F, -3.35F, -3F, 8F, 18F, 6F, new CubeDeformation(0F, -0.15F, -0.01F))
                .texOffs(263, 88).addBox(-2.5F, -3.35F, -5F, 5F, 18F, 10F, new CubeDeformation(-0.26F, -0.23F, -0.25F))
                .texOffs(351, 214).addBox(-5F, -4.4F, -3F, 10F, 6F, 6F, new CubeDeformation(-0.4F, 0F, -0.05F))
                .texOffs(395, 161).addBox(-2.5F, -4.4F, -5.5F, 5F, 6F, 11F, new CubeDeformation(-0.3F, -0.08F, -0.15F)),
                PartPose.offsetAndRotation(8.5F, 2F, 11F, -0.1745F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(284, 140).addBox(-3F, -1.6F, -2F, 6F, 15F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.432F))
                .texOffs(198, 117).addBox(-2F, -1.6F, -3F, 4F, 15F, 6F, new CubeDeformation(-0.432F, -0.28F, -0.2F)),
                PartPose.offsetAndRotation(0F, 13.9F, 0F, 0.5236F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(215, 214).addBox(-3.5F, 0.4F, -7F, 7F, 2F, 10F, new CubeDeformation(0F, -0.44F, 0F))
                .texOffs(450, 198).addBox(-3F, -0.1F, -7F, 6F, 3F, 10F, new CubeDeformation(-0.34F, -0.1F, -0.08F))
                .texOffs(325, 252).addBox(-3.5333F, 0.85F, -10.45F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.45F))
                .texOffs(239, 268).addBox(-3.5333F, 1.23F, -12.45F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(340, 252).addBox(-1F, 0.85F, -10.45F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.45F))
                .texOffs(250, 268).addBox(-1F, 1.23F, -12.45F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(355, 252).addBox(1.5333F, 0.85F, -10.45F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.45F))
                .texOffs(261, 268).addBox(1.5333F, 1.23F, -12.45F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(203, 140).addBox(-3.5F, -2.8F, -2F, 7F, 16F, 4F, new CubeDeformation(-0.25F, -0.2F, -0.18F))
                .texOffs(473, 88).addBox(-2F, -2.8F, -3.5F, 4F, 16F, 7F, new CubeDeformation(-0.18F, -0.28F, -0.25F))
                .texOffs(167, 241).addBox(-4F, -3.6375F, -2F, 8F, 5F, 4F, new CubeDeformation(-0.15F, -0.0625F, -0.1F))
                .texOffs(58, 214).addBox(-2F, -3.6375F, -4F, 4F, 5F, 8F, new CubeDeformation(-0.1F, -0.1425F, -0.15F)),
                PartPose.offsetAndRotation(-8F, 3F, -12F, 0.1047F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(319, 161).addBox(-2.5F, -1.85F, -1.5F, 5F, 15F, 3F, new CubeDeformation(0F, -0.45F, -0.1F))
                .texOffs(343, 140).addBox(-1.5F, -1.35F, -2.5F, 3F, 14F, 5F, new CubeDeformation(-0.1F, -0.03F, 0F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(143, 228).addBox(-3.5F, 0.3F, -6.3F, 7F, 2F, 9F, new CubeDeformation(-0.25F, -0.48F, 0F))
                .texOffs(58, 228).addBox(-2.5F, -0.2F, -6.3F, 5F, 3F, 9F, new CubeDeformation(-0.03F, -0.2F, -0.08F))
                .texOffs(156, 261).addBox(-3.3667F, 0.65F, -9.05F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(272, 268).addBox(-3.3667F, 1.03F, -11.25F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(169, 261).addBox(-1F, 0.65F, -9.05F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(283, 268).addBox(-1F, 1.03F, -11.25F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(182, 261).addBox(1.3667F, 0.65F, -9.05F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(294, 268).addBox(1.3667F, 1.03F, -11.25F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.35F)),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(421, 88).addBox(-4F, -3.35F, -3F, 8F, 18F, 6F, new CubeDeformation(0F, -0.15F, -0.01F))
                .texOffs(294, 88).addBox(-2.5F, -3.35F, -5F, 5F, 18F, 10F, new CubeDeformation(-0.26F, -0.23F, -0.25F))
                .texOffs(384, 214).addBox(-5F, -4.4F, -3F, 10F, 6F, 6F, new CubeDeformation(-0.4F, 0F, -0.05F))
                .texOffs(428, 161).addBox(-2.5F, -4.4F, -5.5F, 5F, 6F, 11F, new CubeDeformation(-0.3F, -0.08F, -0.15F)),
                PartPose.offsetAndRotation(-8.5F, 2F, 11F, -0.1745F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(305, 140).addBox(-3F, -1.6F, -2F, 6F, 15F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.432F))
                .texOffs(219, 117).addBox(-2F, -1.6F, -3F, 4F, 15F, 6F, new CubeDeformation(-0.432F, -0.28F, -0.2F)),
                PartPose.offsetAndRotation(0F, 13.9F, 0F, 0.5236F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(250, 214).addBox(-3.5F, 0.4F, -7F, 7F, 2F, 10F, new CubeDeformation(0F, -0.44F, 0F))
                .texOffs(0, 214).addBox(-3F, -0.1F, -7F, 6F, 3F, 10F, new CubeDeformation(-0.34F, -0.1F, -0.08F))
                .texOffs(370, 252).addBox(-3.5333F, 0.85F, -10.45F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.45F))
                .texOffs(305, 268).addBox(-3.5333F, 1.23F, -12.45F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(385, 252).addBox(-1F, 0.85F, -10.45F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.45F))
                .texOffs(316, 268).addBox(-1F, 1.23F, -12.45F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(400, 252).addBox(1.5333F, 0.85F, -10.45F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.45F))
                .texOffs(327, 268).addBox(1.5333F, 1.23F, -12.45F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(459, 241).addBox(-1.35F, -2.5F, -1.5F, 16F, 5F, 3F, new CubeDeformation(-0.35F, -0.2F, -0.488F))
                .texOffs(373, 241).addBox(-1.35F, -1.5F, -2.5F, 16F, 3F, 5F, new CubeDeformation(-0.43F, -0.488F, -0.2F))
                .texOffs(70, 268).addBox(12.9F, -1.5F, -1F, 14F, 3F, 2F, new CubeDeformation(-0.1F, -0.06F, -0.3664F))
                .texOffs(0, 268).addBox(12.9F, -1F, -1.5F, 14F, 2F, 3F, new CubeDeformation(-0.18F, -0.3664F, -0.06F))
                .texOffs(0, 88).addBox(0F, 0F, 0.94F, 26F, 0F, 28F),
                PartPose.offset(8.5F, -9F, -10F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(347, 261).addBox(-1.05F, -1.5F, -1F, 17F, 3F, 2F, new CubeDeformation(-0.45F, -0.096F, -0.3822F))
                .texOffs(386, 261).addBox(-0.55F, -1F, -1.5F, 16F, 2F, 3F, new CubeDeformation(-0.03F, -0.3822F, -0.096F))
                .texOffs(82, 285).addBox(13.775F, -1F, -0.5F, 18F, 2F, 1F, new CubeDeformation(-0.175F, -0.01F, -0.0644F))
                .texOffs(0, 285).addBox(13.775F, -0.5F, -1F, 18F, 1F, 2F, new CubeDeformation(-0.255F, -0.0644F, -0.01F))
                .texOffs(267, 54).addBox(0F, 0F, 0.49F, 31F, 0F, 30F),
                PartPose.offset(26F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(195, 261).addBox(-2F, -2F, -1F, 4F, 4F, 2F, new CubeDeformation(-0.29F, -0.29F, -0.145F))
                .texOffs(457, 252).addBox(-2F, -1F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.3584F, -0.145F, -0.29F))
                .texOffs(208, 261).addBox(-1F, -1F, -5.21F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(338, 268).addBox(-1F, -0.75F, -7.41F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(31F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(30, 274).addBox(-0.46F, -1F, -1F, 29F, 2F, 2F, new CubeDeformation(-0.46F, 0F, 0F))
                .texOffs(156, 274).addBox(27F, -1F, -1F, 27F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(240, 117).addBox(0F, 0F, 0.3F, 54F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1396F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(274, 274).addBox(-0.26F, -1F, -1F, 26F, 2F, 2F, new CubeDeformation(-0.26F, -0.15F, -0.15F))
                .texOffs(388, 274).addBox(24.25F, -1F, -1F, 25F, 2F, 2F, new CubeDeformation(-0.25F, -0.49F, -0.49F))
                .texOffs(360, 140).addBox(0F, 0F, 0.21F, 49F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5236F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(0, 280).addBox(-0.32F, -1F, -1F, 23F, 2F, 2F, new CubeDeformation(-0.32F, -0.3F, -0.3F))
                .texOffs(311, 285).addBox(21.25F, -0.5F, -0.5F, 22F, 1F, 1F, new CubeDeformation(-0.25F, -0.08F, -0.08F))
                .texOffs(190, 180).addBox(0F, 0F, 0.12F, 43F, 0F, 15F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9076F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(102, 280).addBox(-0.4F, -1F, -1F, 19F, 2F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.45F))
                .texOffs(405, 285).addBox(17.25F, -0.5F, -0.5F, 18F, 1F, 1F, new CubeDeformation(-0.25F, -0.15F, -0.15F))
                .texOffs(275, 0).addBox(0F, 0F, 0.05F, 35F, 0F, 33F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.2915F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 252).addBox(-14.65F, -2.5F, -1.5F, 16F, 5F, 3F, new CubeDeformation(-0.35F, -0.2F, -0.488F))
                .texOffs(416, 241).addBox(-14.65F, -1.5F, -2.5F, 16F, 3F, 5F, new CubeDeformation(-0.43F, -0.488F, -0.2F))
                .texOffs(103, 268).addBox(-26.9F, -1.5F, -1F, 14F, 3F, 2F, new CubeDeformation(-0.1F, -0.06F, -0.3664F))
                .texOffs(35, 268).addBox(-26.9F, -1F, -1.5F, 14F, 2F, 3F, new CubeDeformation(-0.18F, -0.3664F, -0.06F))
                .texOffs(109, 88).addBox(-26F, 0F, 0.94F, 26F, 0F, 28F),
                PartPose.offset(-8.5F, -9F, -10F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(425, 261).addBox(-15.95F, -1.5F, -1F, 17F, 3F, 2F, new CubeDeformation(-0.45F, -0.096F, -0.3822F))
                .texOffs(464, 261).addBox(-15.45F, -1F, -1.5F, 16F, 2F, 3F, new CubeDeformation(-0.03F, -0.3822F, -0.096F))
                .texOffs(121, 285).addBox(-31.775F, -1F, -0.5F, 18F, 2F, 1F, new CubeDeformation(-0.175F, -0.01F, -0.0644F))
                .texOffs(41, 285).addBox(-31.775F, -0.5F, -1F, 18F, 1F, 2F, new CubeDeformation(-0.255F, -0.0644F, -0.01F))
                .texOffs(390, 54).addBox(-31F, 0F, 0.49F, 31F, 0F, 30F),
                PartPose.offset(-26F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(221, 261).addBox(-2F, -2F, -1F, 4F, 4F, 2F, new CubeDeformation(-0.29F, -0.29F, -0.145F))
                .texOffs(474, 252).addBox(-2F, -1F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.3584F, -0.145F, -0.29F))
                .texOffs(234, 261).addBox(-1F, -1F, -5.21F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(349, 268).addBox(-1F, -0.75F, -7.41F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(-31F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(93, 274).addBox(-28.54F, -1F, -1F, 29F, 2F, 2F, new CubeDeformation(-0.46F, 0F, 0F))
                .texOffs(215, 274).addBox(-54F, -1F, -1F, 27F, 2F, 2F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(0, 140).addBox(-54F, 0F, 0.3F, 54F, 0F, 20F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1396F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(331, 274).addBox(-25.74F, -1F, -1F, 26F, 2F, 2F, new CubeDeformation(-0.26F, -0.15F, -0.15F))
                .texOffs(443, 274).addBox(-49.25F, -1F, -1F, 25F, 2F, 2F, new CubeDeformation(-0.25F, -0.49F, -0.49F))
                .texOffs(0, 161).addBox(-49F, 0F, 0.21F, 49F, 0F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(51, 280).addBox(-22.68F, -1F, -1F, 23F, 2F, 2F, new CubeDeformation(-0.32F, -0.3F, -0.3F))
                .texOffs(358, 285).addBox(-43.25F, -0.5F, -0.5F, 22F, 1F, 1F, new CubeDeformation(-0.25F, -0.08F, -0.08F))
                .texOffs(307, 180).addBox(-43F, 0F, 0.12F, 43F, 0F, 15F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9076F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(145, 280).addBox(-18.6F, -1F, -1F, 19F, 2F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.45F))
                .texOffs(444, 285).addBox(-35.25F, -0.5F, -0.5F, 18F, 1F, 1F, new CubeDeformation(-0.25F, -0.15F, -0.15F))
                .texOffs(0, 54).addBox(-35F, 0F, 0.05F, 35F, 0F, 33F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.2915F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
