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
                .texOffs(0, 0).addBox(-7F, -12F, -22F, 14F, 19F, 44F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(152, 138).addBox(-13F, -8F, -24F, 26F, 11F, 16F)
                .texOffs(155, 64).addBox(-8F, -13F, -24F, 16F, 21F, 16F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(411, 169).addBox(-12.5F, -8.25F, 8F, 25F, 10F, 13F, new CubeDeformation(-0.3F, -0.25F, 0F))
                .texOffs(344, 103).addBox(-8F, -12.75F, 8F, 16F, 19F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(252, 0).addBox(-5.5F, -15F, -21F, 11F, 4F, 38F)
                .texOffs(406, 298).addBox(8.5F, -12.5F, -18F, 6F, 3F, 11F)
                .texOffs(272, 242).addBox(10.5F, -14.5F, -18F, 2F, 7F, 11F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(25, 314).addBox(10.7F, -6.25F, -15.4F, 2F, 11F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(361, 314).addBox(10.7F, -5.65F, -9.9F, 2F, 10F, 3F, new CubeDeformation(-0.3F, -0.05F, -0.4F))
                .texOffs(372, 314).addBox(10.7F, -5.55F, -4.4F, 2F, 10F, 3F, new CubeDeformation(-0.3F, -0.35F, -0.4F))
                .texOffs(253, 329).addBox(10.7F, -4.95F, 1.1F, 2F, 9F, 3F, new CubeDeformation(-0.3F, -0.15F, -0.4F))
                .texOffs(441, 298).addBox(-14.5F, -12.5F, -18F, 6F, 3F, 11F)
                .texOffs(299, 242).addBox(-12.5F, -14.5F, -18F, 2F, 7F, 11F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(36, 314).addBox(-12.7F, -6.25F, -15.4F, 2F, 11F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.4F))
                .texOffs(383, 314).addBox(-12.7F, -5.65F, -9.9F, 2F, 10F, 3F, new CubeDeformation(-0.3F, -0.05F, -0.4F))
                .texOffs(394, 314).addBox(-12.7F, -5.55F, -4.4F, 2F, 10F, 3F, new CubeDeformation(-0.3F, -0.35F, -0.4F))
                .texOffs(264, 329).addBox(-12.7F, -4.95F, 1.1F, 2F, 9F, 3F, new CubeDeformation(-0.3F, -0.15F, -0.4F))
                .texOffs(106, 242).addBox(-9F, 7.1F, -23F, 18F, 3F, 15F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(403, 103).addBox(-8.5F, 6.4F, -8F, 17F, 2F, 28F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_back_spike1 = p_body.addOrReplaceChild("back_spike1", CubeListBuilder.create()
                .texOffs(42, 342).addBox(-2F, -3.936F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.3F, -0.464F, -0.375F))
                .texOffs(173, 370).addBox(-1.5F, -6.1665F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.395F, -0.0555F, -0.1187F))
                .texOffs(187, 384).addBox(-1F, -8.8055F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.49F, -0.0555F, -0.3625F)),
                PartPose.offsetAndRotation(0F, -14.6F, -18F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike2 = p_body.addOrReplaceChild("back_spike2", CubeListBuilder.create()
                .texOffs(61, 342).addBox(-2F, -4.356F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.3F, -0.044F, -0.375F))
                .texOffs(15, 362).addBox(-1.5F, -7.7965F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.395F, -0.2655F, -0.1187F))
                .texOffs(208, 377).addBox(-1F, -11.0155F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.49F, -0.2655F, -0.3625F)),
                PartPose.offsetAndRotation(0F, -14.6F, -11F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike3 = p_body.addOrReplaceChild("back_spike3", CubeListBuilder.create()
                .texOffs(369, 329).addBox(-2F, -5.066F, -2.5F, 4F, 6F, 5F, new CubeDeformation(-0.3F, -0.334F, -0.375F))
                .texOffs(28, 362).addBox(-1.5F, -8.3615F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.395F, -0.1205F, -0.1187F))
                .texOffs(217, 377).addBox(-1F, -11.8705F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.49F, -0.1205F, -0.3625F)),
                PartPose.offsetAndRotation(0F, -14.6F, -4F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike4 = p_body.addOrReplaceChild("back_spike4", CubeListBuilder.create()
                .texOffs(80, 342).addBox(-2F, -4.356F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.3F, -0.044F, -0.375F))
                .texOffs(41, 362).addBox(-1.5F, -7.7965F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.395F, -0.2655F, -0.1187F))
                .texOffs(226, 377).addBox(-1F, -11.0155F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.49F, -0.2655F, -0.3625F)),
                PartPose.offsetAndRotation(0F, -14.6F, 3F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike5 = p_body.addOrReplaceChild("back_spike5", CubeListBuilder.create()
                .texOffs(99, 342).addBox(-2F, -3.936F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.3F, -0.464F, -0.375F))
                .texOffs(186, 370).addBox(-1.5F, -6.1665F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.395F, -0.0555F, -0.1187F))
                .texOffs(196, 384).addBox(-1F, -8.8055F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.49F, -0.0555F, -0.3625F)),
                PartPose.offsetAndRotation(0F, -14.6F, 10F, -0.4538F, 0F, 0F));
        PartDefinition p_back_spike6 = p_body.addOrReplaceChild("back_spike6", CubeListBuilder.create()
                .texOffs(431, 342).addBox(-2F, -3.016F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.3F, -0.384F, -0.375F))
                .texOffs(199, 370).addBox(-1.5F, -5.0365F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.395F, -0.3455F, -0.1187F))
                .texOffs(205, 384).addBox(-1F, -7.0955F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.49F, -0.3455F, -0.3625F)),
                PartPose.offsetAndRotation(0F, -14.6F, 16F, -0.4538F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(260, 280).addBox(-7F, -3F, -8.25F, 14F, 6F, 9F, new CubeDeformation(-0.25F, -0.4F, -0.15F))
                .texOffs(0, 219).addBox(-3F, -6.5F, -8.25F, 6F, 13F, 9F, new CubeDeformation(-0.15F, 0F, -0.23F))
                .texOffs(419, 329).addBox(-3F, 5.3F, -7.75F, 6F, 2F, 8F, new CubeDeformation(-0.15F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -6F, -22F, -0.5236F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(117, 298).addBox(-6.5F, -2.5F, -8F, 13F, 5F, 9F, new CubeDeformation(-0.2F, -0.06F, -0.4F))
                .texOffs(31, 219).addBox(-3F, -6.5F, -8F, 6F, 13F, 9F, new CubeDeformation(-0.36F, -0.4F, -0.48F))
                .texOffs(302, 342).addBox(-3F, 4.9F, -7F, 6F, 2F, 7F, new CubeDeformation(-0.36F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.2094F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(162, 298).addBox(-6F, -2.5F, -8F, 12F, 5F, 9F, new CubeDeformation(-0.1F, -0.22F, -0.4F))
                .texOffs(62, 219).addBox(-2.5F, -6F, -8F, 5F, 12F, 9F, new CubeDeformation(-0.02F, -0.3F, -0.48F))
                .texOffs(356, 342).addBox(-2.5F, 4.5F, -7F, 5F, 2F, 7F, new CubeDeformation(-0.02F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, -0.0698F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(47, 314).addBox(-5.5F, -2.5F, -7.25F, 11F, 5F, 8F, new CubeDeformation(0F, -0.38F, -0.15F))
                .texOffs(33, 242).addBox(-2.5F, -5.5F, -7.25F, 5F, 11F, 8F, new CubeDeformation(-0.18F, -0.2F, -0.23F))
                .texOffs(381, 342).addBox(-2.5F, 4.1F, -6.75F, 5F, 2F, 7F, new CubeDeformation(-0.18F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.0524F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(405, 314).addBox(-5.5F, -2F, -7.25F, 11F, 4F, 8F, new CubeDeformation(-0.3F, 0F, -0.15F))
                .texOffs(326, 242).addBox(-2.5F, -5F, -7.25F, 5F, 10F, 8F, new CubeDeformation(-0.3F, 0F, -0.23F))
                .texOffs(406, 342).addBox(-2.5F, 3.8F, -6.75F, 5F, 2F, 7F, new CubeDeformation(-0.3F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1047F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(444, 314).addBox(-5F, -2F, -7F, 10F, 4F, 8F, new CubeDeformation(-0.1F, -0.12F, -0.4F))
                .texOffs(353, 242).addBox(-2.5F, -5F, -7F, 5F, 10F, 8F, new CubeDeformation(-0.42F, -0.3F, -0.48F))
                .texOffs(485, 342).addBox(-2.5F, 3.5F, -6F, 5F, 2F, 6F, new CubeDeformation(-0.42F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1396F, 0F, 0F));
        PartDefinition p_neck7 = p_neck6.addOrReplaceChild("neck7", CubeListBuilder.create()
                .texOffs(0, 329).addBox(-5F, -2F, -7F, 10F, 4F, 8F, new CubeDeformation(-0.3F, -0.2F, -0.4F))
                .texOffs(58, 280).addBox(-2.5F, -4.5F, -7F, 5F, 9F, 8F, new CubeDeformation(-0.5F, 0F, -0.48F))
                .texOffs(0, 353).addBox(-2.5F, 3.3F, -6F, 5F, 2F, 6F, new CubeDeformation(-0.5F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.1222F, 0F, 0F));
        PartDefinition p_neck7_spike = p_neck7.addOrReplaceChild("neck7_spike", CubeListBuilder.create()
                .texOffs(437, 377).addBox(-1.5F, -1.302F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.098F, 0F))
                .texOffs(310, 390).addBox(-1F, -2.6155F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.3385F, -0.025F))
                .texOffs(216, 395).addBox(-0.5F, -3.6885F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.3385F, -0.05F)),
                PartPose.offsetAndRotation(0F, -4.1F, -2.7F, -0.6283F, 0F, 0F));
        PartDefinition p_head = p_neck7.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(137, 219).addBox(-6.5F, -3.5F, -13.25F, 13F, 5F, 15F, new CubeDeformation(-0.3F, -0.1F, -0.25F))
                .texOffs(171, 169).addBox(-4F, -6F, -13.25F, 8F, 10F, 15F, new CubeDeformation(-0.4F, 0F, -0.33F))
                .texOffs(450, 342).addBox(-5F, -5.2F, -17F, 10F, 1F, 7F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(388, 329).addBox(-4F, -6.2F, -17F, 8F, 3F, 7F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(291, 298).addBox(-5F, -2.4F, -22F, 10F, 4F, 10F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(173, 242).addBox(-3F, -4.4F, -22F, 6F, 8F, 10F, new CubeDeformation(-0.4F, 0F, -0.08F))
                .texOffs(123, 314).addBox(-4F, -2.2F, -30F, 8F, 4F, 9F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(0, 280).addBox(-2.5F, -4.2F, -30F, 5F, 8F, 9F, new CubeDeformation(-0.4F, -0.3F, -0.08F))
                .texOffs(446, 262).addBox(-2.5F, -5.6F, -28.5F, 5F, 2F, 15F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(156, 370).addBox(-3F, -3.4F, -31.3F, 6F, 4F, 2F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(86, 400).addBox(0.9F, -3.25F, -31.3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.2F))
                .texOffs(211, 329).addBox(4.8F, -0.4F, -14F, 2F, 4F, 8F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(323, 314).addBox(5.3F, -0.9F, -14F, 1F, 5F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.08F))
                .texOffs(212, 370).addBox(4.65F, -3.8F, -13.8F, 2F, 2F, 4F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(463, 377).addBox(4.7F, -4.45F, -9.3F, 2F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.3F))
                .texOffs(319, 390).addBox(4.7F, -4.8F, -5.7F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.1F))
                .texOffs(175, 395).addBox(3.2F, 3.28F, -28.5F, 1F, 3F, 1F, new CubeDeformation(-0.075F, -0.42F, -0.075F))
                .texOffs(221, 395).addBox(3.2F, 3.5F, -26.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(226, 395).addBox(3.2F, 3.5F, -24.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(231, 395).addBox(3.2F, 3.5F, -22.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(236, 395).addBox(3.2F, 3.5F, -20.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(241, 395).addBox(3.2F, 3.5F, -18.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(180, 395).addBox(3.2F, 3.28F, -16.5F, 1F, 3F, 1F, new CubeDeformation(-0.075F, -0.42F, -0.075F))
                .texOffs(235, 377).addBox(2.3F, 3.3F, -28.6F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(93, 400).addBox(-2.9F, -3.25F, -31.3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.2F))
                .texOffs(232, 329).addBox(-6.8F, -0.4F, -14F, 2F, 4F, 8F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(342, 314).addBox(-6.3F, -0.9F, -14F, 1F, 5F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.08F))
                .texOffs(225, 370).addBox(-6.65F, -3.8F, -13.8F, 2F, 2F, 4F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(474, 377).addBox(-6.7F, -4.45F, -9.3F, 2F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.3F))
                .texOffs(328, 390).addBox(-6.7F, -4.8F, -5.7F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.1F))
                .texOffs(185, 395).addBox(-4.2F, 3.28F, -28.5F, 1F, 3F, 1F, new CubeDeformation(-0.075F, -0.42F, -0.075F))
                .texOffs(246, 395).addBox(-4.2F, 3.5F, -26.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(251, 395).addBox(-4.2F, 3.5F, -24.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(256, 395).addBox(-4.2F, 3.5F, -22.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(261, 395).addBox(-4.2F, 3.5F, -20.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(266, 395).addBox(-4.2F, 3.5F, -18.5F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.2F, -0.075F))
                .texOffs(190, 395).addBox(-4.2F, 3.28F, -16.5F, 1F, 3F, 1F, new CubeDeformation(-0.075F, -0.42F, -0.075F))
                .texOffs(244, 377).addBox(-4.3F, 3.3F, -28.6F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(271, 395).addBox(-2.5F, 3.3F, -29.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(276, 395).addBox(-1.2F, 3.3F, -29.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(281, 395).addBox(0.2F, 3.3F, -29.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(286, 395).addBox(1.5F, 3.3F, -29.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(180, 262).addBox(-3.5F, 3.37F, -29.45F, 7F, 1F, 16F, new CubeDeformation(-0.1F, -0.35F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.5236F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(68, 329).addBox(-2.5F, -1.7F, -0.6F, 5F, 3F, 9F, new CubeDeformation(-0.2F, -0.3F, 0F)),
                PartPose.offsetAndRotation(4.2F, -5.3F, -12.8F, 0.1396F, 0F, -0.2094F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(97, 329).addBox(-2.5F, -1.7F, -0.6F, 5F, 3F, 9F, new CubeDeformation(-0.2F, -0.3F, 0F)),
                PartPose.offsetAndRotation(-4.2F, -5.3F, -12.8F, 0.1396F, 0F, 0.2094F));
        PartDefinition p_crest1 = p_head.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(485, 377).addBox(-1F, -1.155F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.245F, -0.25F))
                .texOffs(337, 390).addBox(-1F, -2.22F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.44F, -0.1875F))
                .texOffs(291, 395).addBox(-0.5F, -3.09F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.44F, -0.125F)),
                PartPose.offsetAndRotation(0F, -6F, -10F, -0.9599F, 0F, 0F));
        PartDefinition p_crest2 = p_head.addOrReplaceChild("crest2", CubeListBuilder.create()
                .texOffs(496, 377).addBox(-1F, -1.281F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.119F, -0.25F))
                .texOffs(346, 390).addBox(-1F, -2.559F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.353F, -0.1875F))
                .texOffs(296, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.353F, -0.125F)),
                PartPose.offsetAndRotation(0F, -6F, -5.5F, -0.9599F, 0F, 0F));
        PartDefinition p_crest3 = p_head.addOrReplaceChild("crest3", CubeListBuilder.create()
                .texOffs(0, 384).addBox(-1F, -1.365F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.035F, -0.25F))
                .texOffs(355, 390).addBox(-1F, -2.785F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.295F, -0.1875F))
                .texOffs(301, 395).addBox(-0.5F, -3.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.295F, -0.125F)),
                PartPose.offsetAndRotation(0F, -6F, -1F, -0.9599F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(300, 329).addBox(-2.5F, -2.5F, -0.725F, 5F, 5F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.325F))
                .texOffs(65, 353).addBox(-1.5F, -1.5F, 4.375F, 3F, 3F, 5F, new CubeDeformation(-0.198F, -0.198F, -0.125F)),
                PartPose.offsetAndRotation(4.4F, -5.4F, -3.5F, 0.384F, 0.384F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(475, 329).addBox(-2F, -2F, -0.8625F, 4F, 4F, 6F, new CubeDeformation(-0.35F, -0.35F, -0.4625F))
                .texOffs(82, 353).addBox(-1.5F, -1.5F, 4F, 3F, 3F, 5F, new CubeDeformation(-0.477F, -0.477F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1745F, 0.1222F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(99, 353).addBox(-1.5F, -1.5F, -0.6375F, 3F, 3F, 5F, new CubeDeformation(-0.25F, -0.25F, -0.2375F))
                .texOffs(238, 370).addBox(-1F, -1F, 3.75F, 2F, 2F, 4F, new CubeDeformation(-0.225F, -0.225F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.1571F, 0.0524F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(251, 370).addBox(-1F, -1F, -0.4125F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.0125F))
                .texOffs(264, 370).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.442F, -0.442F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.0698F, -0.0349F, 0F));
        PartDefinition p_horn_l5 = p_horn_l4.addOrReplaceChild("horn_l5", CubeListBuilder.create()
                .texOffs(277, 370).addBox(-1F, -1F, -0.825F, 2F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.425F))
                .texOffs(364, 390).addBox(-0.5F, -0.5F, 2.375F, 1F, 1F, 3F, new CubeDeformation(-0.128F, -0.128F, -0.125F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2094F, -0.1047F, 0F));
        PartDefinition p_horn_l_up1 = p_head.addOrReplaceChild("horn_l_up1", CubeListBuilder.create()
                .texOffs(232, 353).addBox(-1.5F, -1.5F, -0.4125F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.0125F))
                .texOffs(290, 370).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.132F, -0.132F, -0.25F)),
                PartPose.offsetAndRotation(2.6F, -6F, -6.5F, 1.0821F, 0.1745F, 0F));
        PartDefinition p_horn_l_up2 = p_horn_l_up1.addOrReplaceChild("horn_l_up2", CubeListBuilder.create()
                .texOffs(303, 370).addBox(-1F, -1F, -0.55F, 2F, 2F, 4F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(316, 370).addBox(-1F, -1F, 2.625F, 2F, 2F, 4F, new CubeDeformation(-0.38F, -0.38F, -0.375F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2443F, 0.0698F, 0F));
        PartDefinition p_horn_l_up3 = p_horn_l_up2.addOrReplaceChild("horn_l_up3", CubeListBuilder.create()
                .texOffs(329, 370).addBox(-1F, -1F, -0.825F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.425F))
                .texOffs(373, 390).addBox(-0.5F, -0.5F, 2.375F, 1F, 1F, 3F, new CubeDeformation(-0.066F, -0.066F, -0.125F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0.2793F, 0F, 0F));
        PartDefinition p_horn_l_low1 = p_head.addOrReplaceChild("horn_l_low1", CubeListBuilder.create()
                .texOffs(247, 353).addBox(-1.5F, -1.5F, -0.6875F, 3F, 3F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.2875F))
                .texOffs(11, 384).addBox(-1F, -1F, 2.75F, 2F, 2F, 3F, new CubeDeformation(-0.256F, -0.256F, 0F)),
                PartPose.offsetAndRotation(5.8F, -2.2F, -2.5F, 0.0698F, 0.7679F, 0F));
        PartDefinition p_horn_l_low2 = p_horn_l_low1.addOrReplaceChild("horn_l_low2", CubeListBuilder.create()
                .texOffs(22, 384).addBox(-1F, -1F, -0.4625F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.0625F))
                .texOffs(33, 384).addBox(-1F, -1F, 2F, 2F, 2F, 3F, new CubeDeformation(-0.442F, -0.442F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, 0.1396F, 0F));
        PartDefinition p_horn_l_low3 = p_horn_l_low2.addOrReplaceChild("horn_l_low3", CubeListBuilder.create()
                .texOffs(44, 384).addBox(-1F, -1F, -0.7375F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.3375F))
                .texOffs(195, 395).addBox(-0.5F, -0.5F, 1.75F, 1F, 1F, 2F, new CubeDeformation(-0.128F, -0.128F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(214, 384).addBox(-1F, -1.907F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.493F, -0.0625F))
                .texOffs(126, 395).addBox(-0.5F, -2.898F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.0125F, -0.266F, -0.3906F))
                .texOffs(306, 395).addBox(-0.5F, -4.116F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.266F, -0.2187F)),
                PartPose.offsetAndRotation(6.6F, 1.4F, -7F, -1.7453F, 0.5236F, 0F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(382, 390).addBox(-1F, -1.281F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.119F, -0.1875F))
                .texOffs(133, 395).addBox(-0.5F, -2.559F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.0775F, -0.353F, -0.4719F))
                .texOffs(311, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.353F, -0.25F)),
                PartPose.offsetAndRotation(6.4F, 3.4F, -4.5F, -1.9199F, 0.4538F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(323, 329).addBox(-2.5F, -2.5F, -0.725F, 5F, 5F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.325F))
                .texOffs(116, 353).addBox(-1.5F, -1.5F, 4.375F, 3F, 3F, 5F, new CubeDeformation(-0.198F, -0.198F, -0.125F)),
                PartPose.offsetAndRotation(-4.4F, -5.4F, -3.5F, 0.384F, -0.384F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(0, 342).addBox(-2F, -2F, -0.8625F, 4F, 4F, 6F, new CubeDeformation(-0.35F, -0.35F, -0.4625F))
                .texOffs(133, 353).addBox(-1.5F, -1.5F, 4F, 3F, 3F, 5F, new CubeDeformation(-0.477F, -0.477F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, -0.1745F, -0.1222F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(150, 353).addBox(-1.5F, -1.5F, -0.6375F, 3F, 3F, 5F, new CubeDeformation(-0.25F, -0.25F, -0.2375F))
                .texOffs(342, 370).addBox(-1F, -1F, 3.75F, 2F, 2F, 4F, new CubeDeformation(-0.225F, -0.225F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, -0.1571F, -0.0524F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(355, 370).addBox(-1F, -1F, -0.4125F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.0125F))
                .texOffs(368, 370).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.442F, -0.442F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.0698F, 0.0349F, 0F));
        PartDefinition p_horn_r5 = p_horn_r4.addOrReplaceChild("horn_r5", CubeListBuilder.create()
                .texOffs(381, 370).addBox(-1F, -1F, -0.825F, 2F, 2F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.425F))
                .texOffs(391, 390).addBox(-0.5F, -0.5F, 2.375F, 1F, 1F, 3F, new CubeDeformation(-0.128F, -0.128F, -0.125F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2094F, 0.1047F, 0F));
        PartDefinition p_horn_r_up1 = p_head.addOrReplaceChild("horn_r_up1", CubeListBuilder.create()
                .texOffs(262, 353).addBox(-1.5F, -1.5F, -0.4125F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.0125F))
                .texOffs(394, 370).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.132F, -0.132F, -0.25F)),
                PartPose.offsetAndRotation(-2.6F, -6F, -6.5F, 1.0821F, -0.1745F, 0F));
        PartDefinition p_horn_r_up2 = p_horn_r_up1.addOrReplaceChild("horn_r_up2", CubeListBuilder.create()
                .texOffs(407, 370).addBox(-1F, -1F, -0.55F, 2F, 2F, 4F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(420, 370).addBox(-1F, -1F, 2.625F, 2F, 2F, 4F, new CubeDeformation(-0.38F, -0.38F, -0.375F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.2443F, -0.0698F, 0F));
        PartDefinition p_horn_r_up3 = p_horn_r_up2.addOrReplaceChild("horn_r_up3", CubeListBuilder.create()
                .texOffs(433, 370).addBox(-1F, -1F, -0.825F, 2F, 2F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.425F))
                .texOffs(400, 390).addBox(-0.5F, -0.5F, 2.375F, 1F, 1F, 3F, new CubeDeformation(-0.066F, -0.066F, -0.125F)),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0.2793F, 0F, 0F));
        PartDefinition p_horn_r_low1 = p_head.addOrReplaceChild("horn_r_low1", CubeListBuilder.create()
                .texOffs(277, 353).addBox(-1.5F, -1.5F, -0.6875F, 3F, 3F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.2875F))
                .texOffs(55, 384).addBox(-1F, -1F, 2.75F, 2F, 2F, 3F, new CubeDeformation(-0.256F, -0.256F, 0F)),
                PartPose.offsetAndRotation(-5.8F, -2.2F, -2.5F, 0.0698F, -0.7679F, 0F));
        PartDefinition p_horn_r_low2 = p_horn_r_low1.addOrReplaceChild("horn_r_low2", CubeListBuilder.create()
                .texOffs(66, 384).addBox(-1F, -1F, -0.4625F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.0625F))
                .texOffs(77, 384).addBox(-1F, -1F, 2F, 2F, 2F, 3F, new CubeDeformation(-0.442F, -0.442F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, -0.1396F, 0F));
        PartDefinition p_horn_r_low3 = p_horn_r_low2.addOrReplaceChild("horn_r_low3", CubeListBuilder.create()
                .texOffs(88, 384).addBox(-1F, -1F, -0.7375F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.3375F))
                .texOffs(202, 395).addBox(-0.5F, -0.5F, 1.75F, 1F, 1F, 2F, new CubeDeformation(-0.128F, -0.128F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(223, 384).addBox(-1F, -1.907F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.493F, -0.0625F))
                .texOffs(140, 395).addBox(-0.5F, -2.898F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.0125F, -0.266F, -0.3906F))
                .texOffs(316, 395).addBox(-0.5F, -4.116F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.266F, -0.2187F)),
                PartPose.offsetAndRotation(-6.6F, 1.4F, -7F, -1.7453F, -0.5236F, 0F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(409, 390).addBox(-1F, -1.281F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.119F, -0.1875F))
                .texOffs(147, 395).addBox(-0.5F, -2.559F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.0775F, -0.353F, -0.4719F))
                .texOffs(321, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.353F, -0.25F)),
                PartPose.offsetAndRotation(-6.4F, 3.4F, -4.5F, -1.9199F, -0.4538F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(194, 219).addBox(-4.5F, 1.2F, -18.45F, 9F, 1F, 19F, new CubeDeformation(-0.3F, -0.1F, -0.25F))
                .texOffs(0, 195).addBox(-3F, -0.3F, -18.45F, 6F, 4F, 19F, new CubeDeformation(-0.1F, -0.3F, -0.33F))
                .texOffs(201, 353).addBox(-5F, 0.6F, -3F, 10F, 2F, 5F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(448, 329).addBox(-4F, -0.9F, -3F, 8F, 5F, 5F, new CubeDeformation(-0.4F, -0.4F, -0.08F))
                .texOffs(329, 342).addBox(-3F, 2.2F, -17.8F, 6F, 2F, 7F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(326, 395).addBox(2.9F, -1.845F, -17.1286F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.055F, -0.1F))
                .texOffs(331, 395).addBox(2.9F, -1.6F, -15.1857F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(336, 395).addBox(2.9F, -1.6F, -13.2429F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(341, 395).addBox(2.9F, -1.6F, -11.3F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(346, 395).addBox(2.9F, -1.6F, -9.3571F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(351, 395).addBox(2.9F, -1.6F, -7.4143F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(356, 395).addBox(2.9F, -1.845F, -5.4714F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.055F, -0.1F))
                .texOffs(361, 395).addBox(-3.9F, -1.845F, -17.1286F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.055F, -0.1F))
                .texOffs(366, 395).addBox(-3.9F, -1.6F, -15.1857F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(371, 395).addBox(-3.9F, -1.6F, -13.2429F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(376, 395).addBox(-3.9F, -1.6F, -11.3F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(381, 395).addBox(-3.9F, -1.6F, -9.3571F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(386, 395).addBox(-3.9F, -1.6F, -7.4143F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(391, 395).addBox(-3.9F, -1.845F, -5.4714F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.055F, -0.1F))
                .texOffs(448, 219).addBox(-3.5F, -0.5F, -17.8F, 7F, 1F, 18F, new CubeDeformation(0F, -0.35F, 0F))
                .texOffs(442, 280).addBox(-2.5F, -0.7F, -16.5F, 5F, 1F, 14F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(86, 314).addBox(-3F, 3.1F, -12F, 6F, 1F, 12F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, 3.7F, -11.5F, 0.0698F, 0F, 0F));
        PartDefinition p_beard_l = p_jaw.addOrReplaceChild("beard_l", CubeListBuilder.create()
                .texOffs(418, 390).addBox(-1F, -1.281F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.119F, -0.125F))
                .texOffs(154, 395).addBox(-0.5F, -2.559F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.353F, -0.4313F))
                .texOffs(396, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.353F, -0.2375F)),
                PartPose.offsetAndRotation(2.4F, 3.4F, -4F, -2.618F, -0.1745F, 0F));
        PartDefinition p_beard_r = p_jaw.addOrReplaceChild("beard_r", CubeListBuilder.create()
                .texOffs(427, 390).addBox(-1F, -1.281F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.119F, -0.125F))
                .texOffs(161, 395).addBox(-0.5F, -2.559F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.353F, -0.4313F))
                .texOffs(401, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.353F, -0.2375F)),
                PartPose.offsetAndRotation(-2.4F, 3.4F, -4F, -2.618F, 0.1745F, 0F));
        PartDefinition p_beard1 = p_jaw.addOrReplaceChild("beard1", CubeListBuilder.create()
                .texOffs(436, 390).addBox(-1F, -1.323F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.077F, 0F))
                .texOffs(445, 390).addBox(-1F, -2.672F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.324F, -0.35F))
                .texOffs(406, 395).addBox(-0.5F, -3.774F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.324F, -0.2F)),
                PartPose.offsetAndRotation(0F, 3.6F, -8F, -2.618F, 0F, 0F));
        PartDefinition p_neck6_spike = p_neck6.addOrReplaceChild("neck6_spike", CubeListBuilder.create()
                .texOffs(450, 377).addBox(-1.5F, -1.3755F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.0245F, 0F))
                .texOffs(454, 390).addBox(-1F, -2.8133F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.2878F, -0.025F))
                .texOffs(411, 395).addBox(-0.5F, -3.9878F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.2878F, -0.05F)),
                PartPose.offsetAndRotation(0F, -4.3F, -2.7F, -0.6283F, 0F, 0F));
        PartDefinition p_neck5_spike = p_neck5.addOrReplaceChild("neck5_spike", CubeListBuilder.create()
                .texOffs(446, 370).addBox(-1.5F, -1.949F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.451F, 0F))
                .texOffs(463, 390).addBox(-1F, -3.011F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.237F, -0.025F))
                .texOffs(416, 395).addBox(-0.5F, -4.287F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.237F, -0.05F)),
                PartPose.offsetAndRotation(0F, -4.6F, -2.925F, -0.6283F, 0F, 0F));
        PartDefinition p_neck4_spike = p_neck4.addOrReplaceChild("neck4_spike", CubeListBuilder.create()
                .texOffs(459, 370).addBox(-1.5F, -2.0225F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3775F, 0F))
                .texOffs(472, 390).addBox(-1F, -3.2088F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.1863F, -0.025F))
                .texOffs(421, 395).addBox(-0.5F, -4.5863F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.1863F, -0.05F)),
                PartPose.offsetAndRotation(0F, -4.9F, -2.925F, -0.6283F, 0F, 0F));
        PartDefinition p_neck3_spike = p_neck3.addOrReplaceChild("neck3_spike", CubeListBuilder.create()
                .texOffs(472, 370).addBox(-1.5F, -2.096F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.304F, 0F))
                .texOffs(481, 390).addBox(-1F, -3.4065F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.1355F, -0.025F))
                .texOffs(426, 395).addBox(-0.5F, -4.8855F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.1355F, -0.05F)),
                PartPose.offsetAndRotation(0F, -5.3F, -3.15F, -0.6283F, 0F, 0F));
        PartDefinition p_neck2_spike = p_neck2.addOrReplaceChild("neck2_spike", CubeListBuilder.create()
                .texOffs(485, 370).addBox(-1.5F, -2.1695F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.2305F, 0F))
                .texOffs(490, 390).addBox(-1F, -3.6042F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.0847F, -0.025F))
                .texOffs(431, 395).addBox(-0.5F, -5.1847F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.0847F, -0.05F)),
                PartPose.offsetAndRotation(0F, -5.7F, -3.15F, -0.6283F, 0F, 0F));
        PartDefinition p_neck1_spike = p_neck1.addOrReplaceChild("neck1_spike", CubeListBuilder.create()
                .texOffs(498, 370).addBox(-1.5F, -2.243F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.157F, 0F))
                .texOffs(499, 390).addBox(-1F, -3.802F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.034F, -0.025F))
                .texOffs(436, 395).addBox(-0.5F, -5.484F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.034F, -0.05F)),
                PartPose.offsetAndRotation(0F, -6.1F, -3.375F, -0.6283F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(338, 219).addBox(-7F, -2.5F, -0.75F, 14F, 5F, 14F, new CubeDeformation(-0.25F, 0F, -0.15F))
                .texOffs(237, 138).addBox(-3F, -6.5F, -0.75F, 6F, 13F, 14F, new CubeDeformation(0F, -0.25F, -0.23F))
                .texOffs(0, 298).addBox(-3F, 5.05F, -0.25F, 6F, 2F, 13F, new CubeDeformation(0F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -4F, 21F, -0.1745F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(395, 219).addBox(-6F, -2.5F, -1F, 12F, 5F, 14F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(218, 169).addBox(-3F, -5.5F, -1F, 6F, 11F, 14F, new CubeDeformation(-0.3F, 0F, -0.48F))
                .texOffs(332, 298).addBox(-3F, 4.3F, 0F, 6F, 2F, 12F, new CubeDeformation(-0.3F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(131, 262).addBox(-5.5F, -2F, -0.75F, 11F, 4F, 13F, new CubeDeformation(-0.2F, -0.04F, -0.15F))
                .texOffs(51, 195).addBox(-2.5F, -5F, -0.75F, 5F, 10F, 13F, new CubeDeformation(-0.14F, -0.1F, -0.23F))
                .texOffs(476, 298).addBox(-2.5F, 3.7F, -0.25F, 5F, 2F, 12F, new CubeDeformation(-0.14F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0873F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(227, 262).addBox(-5F, -2F, -1F, 10F, 4F, 13F, new CubeDeformation(-0.3F, -0.28F, -0.4F))
                .texOffs(452, 195).addBox(-2.5F, -4.5F, -1F, 5F, 9F, 13F, new CubeDeformation(-0.38F, -0.2F, -0.48F))
                .texOffs(193, 314).addBox(-2.5F, 3.1F, 0F, 5F, 2F, 11F, new CubeDeformation(-0.38F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 11.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(110, 280).addBox(-4.5F, -2F, -0.75F, 9F, 4F, 12F, new CubeDeformation(-0.4F, -0.48F, -0.15F))
                .texOffs(251, 219).addBox(-2F, -4F, -0.75F, 4F, 8F, 12F, new CubeDeformation(-0.18F, -0.2F, -0.23F))
                .texOffs(292, 314).addBox(-2F, 2.6F, -0.25F, 4F, 2F, 11F, new CubeDeformation(-0.18F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0175F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(401, 280).addBox(-4F, -1.5F, -1F, 8F, 3F, 12F, new CubeDeformation(-0.4F, -0.18F, -0.4F))
                .texOffs(0, 242).addBox(-2F, -3.5F, -1F, 4F, 7F, 12F, new CubeDeformation(-0.38F, -0.2F, -0.48F))
                .texOffs(126, 329).addBox(-2F, 2.1F, 0F, 4F, 2F, 10F, new CubeDeformation(-0.38F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0.0524F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(369, 298).addBox(-3.5F, -1.5F, -0.75F, 7F, 3F, 11F, new CubeDeformation(-0.4F, -0.34F, -0.15F))
                .texOffs(29, 280).addBox(-1.5F, -3F, -0.75F, 3F, 6F, 11F, new CubeDeformation(-0.14F, -0.1F, -0.23F))
                .texOffs(184, 329).addBox(-1.5F, 1.7F, -0.25F, 3F, 2F, 10F, new CubeDeformation(-0.14F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(158, 314).addBox(-3F, -1F, -1F, 6F, 2F, 11F, new CubeDeformation(-0.35F, 0F, -0.4F))
                .texOffs(231, 280).addBox(-1.5F, -2.5F, -1F, 3F, 5F, 11F, new CubeDeformation(-0.35F, 0F, -0.48F))
                .texOffs(275, 329).addBox(-1.5F, 1.3F, 0F, 3F, 2F, 9F, new CubeDeformation(-0.35F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 9.5F, 0.0698F, 0F, 0F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(37, 329).addBox(-2.5F, -1F, -0.75F, 5F, 2F, 10F, new CubeDeformation(-0.25F, -0.16F, -0.15F))
                .texOffs(39, 298).addBox(-1F, -2.5F, -0.75F, 2F, 5F, 10F, new CubeDeformation(-0.01F, -0.4F, -0.23F))
                .texOffs(346, 329).addBox(-1F, 0.9F, -0.25F, 2F, 2F, 9F, new CubeDeformation(-0.01F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(155, 329).addBox(-2F, -1F, -1F, 4F, 2F, 10F, new CubeDeformation(-0.1F, -0.3F, -0.4F))
                .texOffs(0, 314).addBox(-1F, -2F, -1F, 2F, 4F, 10F, new CubeDeformation(-0.15F, -0.25F, -0.48F))
                .texOffs(21, 342).addBox(-1F, 0.55F, 0F, 2F, 2F, 8F, new CubeDeformation(-0.15F, -0.2F, 0F))
                .texOffs(64, 298).addBox(-6.5F, -0.5F, 4F, 13F, 1F, 13F)
                .texOffs(85, 280).addBox(-0.5F, -6.25F, 2F, 1F, 6F, 11F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8.5F, 0.0349F, 0F, 0F));
        PartDefinition p_tail9_spike = p_tail9.addOrReplaceChild("tail9_spike", CubeListBuilder.create()
                .texOffs(0, 395).addBox(-1F, -1.029F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.24F, -0.371F, -0.05F))
                .texOffs(209, 395).addBox(-0.5F, -1.381F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.006F, -0.027F, -0.3825F))
                .texOffs(100, 400).addBox(-0.5F, -2.077F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.027F, -0.215F)),
                PartPose.offsetAndRotation(0F, -1.7F, 3.825F, -0.5934F, 0F, 0F));
        PartDefinition p_tail8_spike = p_tail8.addOrReplaceChild("tail8_spike", CubeListBuilder.create()
                .texOffs(99, 384).addBox(-1F, -1.1235F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.16F, -0.2765F, -0.45F))
                .texOffs(9, 395).addBox(-1F, -2.1353F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.454F, -0.4617F, -0.3175F))
                .texOffs(441, 395).addBox(-0.5F, -2.9618F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.248F, -0.4617F, -0.185F)),
                PartPose.offsetAndRotation(0F, -2.1F, 4.05F, -0.5934F, 0F, 0F));
        PartDefinition p_tail7_spike = p_tail7.addOrReplaceChild("tail7_spike", CubeListBuilder.create()
                .texOffs(110, 384).addBox(-1F, -1.218F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.08F, -0.182F, -0.35F))
                .texOffs(18, 395).addBox(-1F, -2.3895F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.402F, -0.3965F, -0.2525F))
                .texOffs(446, 395).addBox(-0.5F, -3.3465F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.224F, -0.3965F, -0.155F)),
                PartPose.offsetAndRotation(0F, -2.5F, 4.275F, -0.5934F, 0F, 0F));
        PartDefinition p_tail6_spike = p_tail6.addOrReplaceChild("tail6_spike", CubeListBuilder.create()
                .texOffs(121, 384).addBox(-1F, -1.3125F, -1.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.0875F, -0.25F))
                .texOffs(27, 395).addBox(-1F, -2.6438F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.3312F, -0.1875F))
                .texOffs(451, 395).addBox(-0.5F, -3.7313F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.3312F, -0.125F)),
                PartPose.offsetAndRotation(0F, -2.9F, 4.5F, -0.5934F, 0F, 0F));
        PartDefinition p_tail5_spike = p_tail5.addOrReplaceChild("tail5_spike", CubeListBuilder.create()
                .texOffs(0, 377).addBox(-1.5F, -1.907F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.42F, -0.493F, -0.15F))
                .texOffs(36, 395).addBox(-1F, -2.898F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.298F, -0.266F, -0.1225F))
                .texOffs(456, 395).addBox(-0.5F, -4.116F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.176F, -0.266F, -0.095F)),
                PartPose.offsetAndRotation(0F, -3.4F, 4.725F, -0.5934F, 0F, 0F));
        PartDefinition p_tail4_spike = p_tail4.addOrReplaceChild("tail4_spike", CubeListBuilder.create()
                .texOffs(13, 377).addBox(-1.5F, -2.0015F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.34F, -0.3985F, -0.05F))
                .texOffs(45, 395).addBox(-1F, -3.1523F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.246F, -0.2007F, -0.0575F))
                .texOffs(461, 395).addBox(-0.5F, -4.5008F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.152F, -0.2007F, -0.065F)),
                PartPose.offsetAndRotation(0F, -3.9F, 4.95F, -0.5934F, 0F, 0F));
        PartDefinition p_tail3_spike = p_tail3.addOrReplaceChild("tail3_spike", CubeListBuilder.create()
                .texOffs(292, 353).addBox(-1.5F, -2.096F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.26F, -0.304F, -0.45F))
                .texOffs(132, 384).addBox(-1F, -3.4065F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.194F, -0.1355F, -0.4925F))
                .texOffs(466, 395).addBox(-0.5F, -4.8855F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.128F, -0.1355F, -0.035F)),
                PartPose.offsetAndRotation(0F, -4.5F, 5.175F, -0.5934F, 0F, 0F));
        PartDefinition p_tail2_spike = p_tail2.addOrReplaceChild("tail2_spike", CubeListBuilder.create()
                .texOffs(307, 353).addBox(-1.5F, -2.1905F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.18F, -0.2095F, -0.35F))
                .texOffs(143, 384).addBox(-1F, -3.6607F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.142F, -0.0703F, -0.4275F))
                .texOffs(471, 395).addBox(-0.5F, -5.2702F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.104F, -0.0703F, -0.005F)),
                PartPose.offsetAndRotation(0F, -5.1F, 5.4F, -0.5934F, 0F, 0F));
        PartDefinition p_tail1_spike = p_tail1.addOrReplaceChild("tail1_spike", CubeListBuilder.create()
                .texOffs(322, 353).addBox(-1.5F, -2.285F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.115F, -0.25F))
                .texOffs(154, 384).addBox(-1F, -3.915F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.09F, -0.005F, -0.3625F))
                .texOffs(168, 395).addBox(-0.5F, -5.655F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.08F, -0.005F, -0.475F)),
                PartPose.offsetAndRotation(0F, -5.85F, 5.625F, -0.5934F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(345, 169).addBox(-5F, -3.9F, -3F, 10F, 18F, 6F, new CubeDeformation(-0.25F, -0.1F, -0.34F))
                .texOffs(86, 138).addBox(-3F, -3.9F, -5F, 6F, 18F, 10F, new CubeDeformation(-0.34F, -0.18F, -0.25F))
                .texOffs(226, 314).addBox(-5.5F, -5.6625F, -2.5F, 11F, 8F, 5F, new CubeDeformation(-0.15F, -0.4375F, 0F))
                .texOffs(206, 242).addBox(-2.5F, -5.1625F, -5.5F, 5F, 7F, 11F, new CubeDeformation(0F, -0.0175F, -0.15F)),
                PartPose.offsetAndRotation(10.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(60, 242).addBox(-3.5F, -1.85F, -2F, 7F, 15F, 4F, new CubeDeformation(0F, -0.45F, -0.04F))
                .texOffs(91, 219).addBox(-2F, -1.35F, -3.5F, 4F, 14F, 7F, new CubeDeformation(-0.04F, -0.03F, 0F)),
                PartPose.offsetAndRotation(0F, 13.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_shin_l_spike = p_front_shin_l.addOrReplaceChild("front_shin_l_spike", CubeListBuilder.create()
                .texOffs(54, 395).addBox(-1F, -1.281F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.119F, 0F))
                .texOffs(63, 395).addBox(-1F, -2.559F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.353F, -0.35F))
                .texOffs(476, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.353F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.5F, -3.5F, -1.9199F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(205, 298).addBox(-4.5F, 0.7F, -8.4F, 9F, 2F, 12F, new CubeDeformation(0F, -0.32F, 0F))
                .texOffs(153, 280).addBox(-3.5F, -0.3F, -8.4F, 7F, 4F, 12F, new CubeDeformation(-0.02F, -0.3F, -0.08F))
                .texOffs(337, 353).addBox(-4.2F, 1.45F, -12.05F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(26, 377).addBox(-4.2F, 1.83F, -15.15F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(352, 353).addBox(-1F, 1.45F, -12.05F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(39, 377).addBox(-1F, 1.83F, -15.15F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(367, 353).addBox(2.2F, 1.45F, -12.05F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(52, 377).addBox(2.2F, 1.83F, -15.15F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.35F)),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(0, 138).addBox(-6F, -5.05F, -4.5F, 12F, 21F, 9F, new CubeDeformation(-0.25F, -0.45F, -0.28F))
                .texOffs(258, 103).addBox(-3.5F, -4.55F, -7F, 7F, 20F, 14F, new CubeDeformation(-0.28F, -0.03F, -0.25F))
                .texOffs(274, 262).addBox(-6.5F, -6.5125F, -4F, 13F, 9F, 8F, new CubeDeformation(-0.15F, -0.1875F, -0.1F))
                .texOffs(259, 169).addBox(-3F, -6.5125F, -7.5F, 6F, 9F, 15F, new CubeDeformation(-0.1F, -0.2675F, -0.15F)),
                PartPose.offsetAndRotation(11F, 2F, 15F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(284, 219).addBox(-4F, -1.6F, -2.5F, 8F, 15F, 5F, new CubeDeformation(0F, -0.2F, -0.26F))
                .texOffs(88, 195).addBox(-2.5F, -1.6F, -4F, 5F, 15F, 8F, new CubeDeformation(-0.26F, -0.28F, 0F)),
                PartPose.offsetAndRotation(0F, 14.9F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_shin_l_spike = p_hind_shin_l.addOrReplaceChild("hind_shin_l_spike", CubeListBuilder.create()
                .texOffs(72, 395).addBox(-1F, -1.365F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.035F, 0F))
                .texOffs(81, 395).addBox(-1F, -2.785F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.295F, -0.35F))
                .texOffs(481, 395).addBox(-0.5F, -3.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.295F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.5F, 4F, 1.9199F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(307, 280).addBox(-5F, 0.7F, -9.1F, 10F, 2F, 13F, new CubeDeformation(0F, -0.32F, 0F))
                .texOffs(317, 262).addBox(-4F, -0.3F, -9.1F, 8F, 4F, 13F, new CubeDeformation(-0.02F, -0.3F, -0.08F))
                .texOffs(382, 353).addBox(-4.5333F, 1.45F, -12.95F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(65, 377).addBox(-4.5333F, 1.83F, -16.35F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(397, 353).addBox(-1F, 1.45F, -12.95F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(78, 377).addBox(-1F, 1.83F, -16.35F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(412, 353).addBox(2.5333F, 1.45F, -12.95F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(91, 377).addBox(2.5333F, 1.83F, -16.35F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(378, 169).addBox(-5F, -3.9F, -3F, 10F, 18F, 6F, new CubeDeformation(-0.25F, -0.1F, -0.34F))
                .texOffs(119, 138).addBox(-3F, -3.9F, -5F, 6F, 18F, 10F, new CubeDeformation(-0.34F, -0.18F, -0.25F))
                .texOffs(259, 314).addBox(-5.5F, -5.6625F, -2.5F, 11F, 8F, 5F, new CubeDeformation(-0.15F, -0.4375F, 0F))
                .texOffs(239, 242).addBox(-2.5F, -5.1625F, -5.5F, 5F, 7F, 11F, new CubeDeformation(0F, -0.0175F, -0.15F)),
                PartPose.offsetAndRotation(-10.5F, 4F, -15F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(83, 242).addBox(-3.5F, -1.85F, -2F, 7F, 15F, 4F, new CubeDeformation(0F, -0.45F, -0.04F))
                .texOffs(114, 219).addBox(-2F, -1.35F, -3.5F, 4F, 14F, 7F, new CubeDeformation(-0.04F, -0.03F, 0F)),
                PartPose.offsetAndRotation(0F, 13.4F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_shin_r_spike = p_front_shin_r.addOrReplaceChild("front_shin_r_spike", CubeListBuilder.create()
                .texOffs(90, 395).addBox(-1F, -1.281F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.119F, 0F))
                .texOffs(99, 395).addBox(-1F, -2.559F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.353F, -0.35F))
                .texOffs(486, 395).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.353F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.5F, -3.5F, -1.9199F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(248, 298).addBox(-4.5F, 0.7F, -8.4F, 9F, 2F, 12F, new CubeDeformation(0F, -0.32F, 0F))
                .texOffs(192, 280).addBox(-3.5F, -0.3F, -8.4F, 7F, 4F, 12F, new CubeDeformation(-0.02F, -0.3F, -0.08F))
                .texOffs(427, 353).addBox(-4.2F, 1.45F, -12.05F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(104, 377).addBox(-4.2F, 1.83F, -15.15F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(442, 353).addBox(-1F, 1.45F, -12.05F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(117, 377).addBox(-1F, 1.83F, -15.15F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.35F))
                .texOffs(457, 353).addBox(2.2F, 1.45F, -12.05F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(130, 377).addBox(2.2F, 1.83F, -15.15F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.35F)),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(43, 138).addBox(-6F, -5.05F, -4.5F, 12F, 21F, 9F, new CubeDeformation(-0.25F, -0.45F, -0.28F))
                .texOffs(301, 103).addBox(-3.5F, -4.55F, -7F, 7F, 20F, 14F, new CubeDeformation(-0.28F, -0.03F, -0.25F))
                .texOffs(360, 262).addBox(-6.5F, -6.5125F, -4F, 13F, 9F, 8F, new CubeDeformation(-0.15F, -0.1875F, -0.1F))
                .texOffs(302, 169).addBox(-3F, -6.5125F, -7.5F, 6F, 9F, 15F, new CubeDeformation(-0.1F, -0.2675F, -0.15F)),
                PartPose.offsetAndRotation(-11F, 2F, 15F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(311, 219).addBox(-4F, -1.6F, -2.5F, 8F, 15F, 5F, new CubeDeformation(0F, -0.2F, -0.26F))
                .texOffs(115, 195).addBox(-2.5F, -1.6F, -4F, 5F, 15F, 8F, new CubeDeformation(-0.26F, -0.28F, 0F)),
                PartPose.offsetAndRotation(0F, 14.9F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_shin_r_spike = p_hind_shin_r.addOrReplaceChild("hind_shin_r_spike", CubeListBuilder.create()
                .texOffs(108, 395).addBox(-1F, -1.365F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.035F, 0F))
                .texOffs(117, 395).addBox(-1F, -2.785F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.295F, -0.35F))
                .texOffs(491, 395).addBox(-0.5F, -3.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.295F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.5F, 4F, 1.9199F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(354, 280).addBox(-5F, 0.7F, -9.1F, 10F, 2F, 13F, new CubeDeformation(0F, -0.32F, 0F))
                .texOffs(403, 262).addBox(-4F, -0.3F, -9.1F, 8F, 4F, 13F, new CubeDeformation(-0.02F, -0.3F, -0.08F))
                .texOffs(472, 353).addBox(-4.5333F, 1.45F, -12.95F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(143, 377).addBox(-4.5333F, 1.83F, -16.35F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(487, 353).addBox(-1F, 1.45F, -12.95F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(156, 377).addBox(-1F, 1.83F, -16.35F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.25F))
                .texOffs(0, 362).addBox(2.5333F, 1.45F, -12.95F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(169, 377).addBox(2.5333F, 1.83F, -16.35F, 2F, 2F, 4F, new CubeDeformation(-0.335F, -0.145F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.4F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(216, 342).addBox(-1.25F, -3F, -1.5F, 18F, 6F, 3F, new CubeDeformation(-0.25F, -0.3F, -0.312F))
                .texOffs(118, 342).addBox(-1.25F, -1.5F, -3F, 18F, 3F, 6F, new CubeDeformation(-0.33F, -0.312F, -0.3F))
                .texOffs(82, 370).addBox(14.9F, -2F, -1F, 16F, 4F, 2F, new CubeDeformation(-0.1F, -0.24F, -0.2256F))
                .texOffs(416, 362).addBox(14.9F, -1F, -2F, 16F, 2F, 4F, new CubeDeformation(-0.18F, -0.2256F, -0.24F))
                .texOffs(0, 103).addBox(0F, 0F, 1.26F, 30F, 0F, 34F),
                PartPose.offset(10.5F, -11F, -13F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(457, 362).addBox(-0.8F, -2F, -1F, 18F, 4F, 2F, new CubeDeformation(-0.2F, -0.284F, -0.245F))
                .texOffs(326, 362).addBox(-0.8F, -1F, -2F, 18F, 2F, 4F, new CubeDeformation(-0.28F, -0.245F, -0.284F))
                .texOffs(347, 377).addBox(14.95F, -1.5F, -1F, 20F, 3F, 2F, new CubeDeformation(-0.35F, -0.29F, -0.4676F))
                .texOffs(253, 377).addBox(14.95F, -1F, -1.5F, 20F, 2F, 3F, new CubeDeformation(-0.43F, -0.4676F, -0.29F))
                .texOffs(220, 64).addBox(0F, 0F, 0.71F, 34F, 0F, 35F),
                PartPose.offset(30F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(167, 353).addBox(-2.5F, -2.5F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.41F, -0.41F, -0.455F))
                .texOffs(23, 353).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.49F, -0.455F, -0.41F))
                .texOffs(182, 377).addBox(-1F, -1F, -5.59F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(165, 384).addBox(-1F, -0.75F, -7.79F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(34F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(54, 362).addBox(-0.4F, -1.5F, -1.5F, 32F, 3F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(232, 384).addBox(30F, -1F, -1F, 30F, 2F, 2F, new CubeDeformation(0F, -0.28F, -0.28F))
                .texOffs(278, 138).addBox(0F, 0F, 0.42F, 60F, 0F, 25F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1047F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(196, 362).addBox(-0.2F, -1.5F, -1.5F, 29F, 3F, 3F, new CubeDeformation(-0.2F, -0.45F, -0.45F))
                .texOffs(362, 384).addBox(27.25F, -1F, -1F, 28F, 2F, 2F, new CubeDeformation(-0.25F, -0.37F, -0.37F))
                .texOffs(142, 195).addBox(0F, 0F, 0.33F, 55F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5411F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(0, 390).addBox(-0.02F, -1F, -1F, 25F, 2F, 2F, new CubeDeformation(-0.02F, -0.1F, -0.1F))
                .texOffs(110, 390).addBox(24F, -1F, -1F, 24F, 2F, 2F, new CubeDeformation(0F, -0.46F, -0.46F))
                .texOffs(380, 242).addBox(0F, 0F, 0.24F, 48F, 0F, 17F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9774F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(216, 390).addBox(-0.36F, -1F, -1F, 21F, 2F, 2F, new CubeDeformation(-0.36F, -0.25F, -0.25F))
                .texOffs(0, 400).addBox(19.25F, -0.5F, -0.5F, 20F, 1F, 1F, new CubeDeformation(-0.25F, -0.05F, -0.05F))
                .texOffs(351, 0).addBox(0F, 0F, 0.15F, 39F, 0F, 38F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.3963F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(259, 342).addBox(-16.75F, -3F, -1.5F, 18F, 6F, 3F, new CubeDeformation(-0.25F, -0.3F, -0.312F))
                .texOffs(167, 342).addBox(-16.75F, -1.5F, -3F, 18F, 3F, 6F, new CubeDeformation(-0.33F, -0.312F, -0.3F))
                .texOffs(119, 370).addBox(-30.9F, -2F, -1F, 16F, 4F, 2F, new CubeDeformation(-0.1F, -0.24F, -0.2256F))
                .texOffs(0, 370).addBox(-30.9F, -1F, -2F, 16F, 2F, 4F, new CubeDeformation(-0.18F, -0.2256F, -0.24F))
                .texOffs(129, 103).addBox(-30F, 0F, 1.26F, 30F, 0F, 34F),
                PartPose.offset(-10.5F, -11F, -13F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(41, 370).addBox(-17.2F, -2F, -1F, 18F, 4F, 2F, new CubeDeformation(-0.2F, -0.284F, -0.245F))
                .texOffs(371, 362).addBox(-17.2F, -1F, -2F, 18F, 2F, 4F, new CubeDeformation(-0.28F, -0.245F, -0.284F))
                .texOffs(392, 377).addBox(-34.95F, -1.5F, -1F, 20F, 3F, 2F, new CubeDeformation(-0.35F, -0.29F, -0.4676F))
                .texOffs(300, 377).addBox(-34.95F, -1F, -1.5F, 20F, 2F, 3F, new CubeDeformation(-0.43F, -0.4676F, -0.29F))
                .texOffs(359, 64).addBox(-34F, 0F, 0.71F, 34F, 0F, 35F),
                PartPose.offset(-30F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(184, 353).addBox(-2.5F, -2.5F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.41F, -0.41F, -0.455F))
                .texOffs(44, 353).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.49F, -0.455F, -0.41F))
                .texOffs(195, 377).addBox(-1F, -1F, -5.59F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(176, 384).addBox(-1F, -0.75F, -7.79F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(-34F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(125, 362).addBox(-31.6F, -1.5F, -1.5F, 32F, 3F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(297, 384).addBox(-60F, -1F, -1F, 30F, 2F, 2F, new CubeDeformation(0F, -0.28F, -0.28F))
                .texOffs(0, 169).addBox(-60F, 0F, 0.42F, 60F, 0F, 25F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1047F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(261, 362).addBox(-28.8F, -1.5F, -1.5F, 29F, 3F, 3F, new CubeDeformation(-0.2F, -0.45F, -0.45F))
                .texOffs(423, 384).addBox(-55.25F, -1F, -1F, 28F, 2F, 2F, new CubeDeformation(-0.25F, -0.37F, -0.37F))
                .texOffs(297, 195).addBox(-55F, 0F, 0.33F, 55F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5411F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(55, 390).addBox(-24.98F, -1F, -1F, 25F, 2F, 2F, new CubeDeformation(-0.02F, -0.1F, -0.1F))
                .texOffs(163, 390).addBox(-48F, -1F, -1F, 24F, 2F, 2F, new CubeDeformation(0F, -0.46F, -0.46F))
                .texOffs(0, 262).addBox(-48F, 0F, 0.24F, 48F, 0F, 17F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9774F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(263, 390).addBox(-20.64F, -1F, -1F, 21F, 2F, 2F, new CubeDeformation(-0.36F, -0.25F, -0.25F))
                .texOffs(43, 400).addBox(-39.25F, -0.5F, -0.5F, 20F, 1F, 1F, new CubeDeformation(-0.25F, -0.05F, -0.05F))
                .texOffs(0, 64).addBox(-39F, 0F, 0.15F, 39F, 0F, 38F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.3963F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
