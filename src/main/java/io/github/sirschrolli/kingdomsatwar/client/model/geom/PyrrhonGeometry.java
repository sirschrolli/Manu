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
                .texOffs(0, 0).addBox(-8F, -12F, -21F, 16F, 20F, 42F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(240, 136).addBox(-14F, -8F, -23F, 28F, 12F, 16F)
                .texOffs(349, 0).addBox(-9F, -13F, -23F, 18F, 22F, 16F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(131, 170).addBox(-13.5F, -8.25F, 7F, 27F, 11F, 13F, new CubeDeformation(-0.3F, -0.25F, 0F))
                .texOffs(0, 136).addBox(-9F, -12.75F, 7F, 18F, 20F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(252, 0).addBox(-6F, -15F, -20F, 12F, 4F, 36F)
                .texOffs(449, 298).addBox(9F, -12.5F, -17F, 6F, 3F, 11F)
                .texOffs(0, 262).addBox(11F, -14.5F, -17F, 2F, 7F, 11F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(0, 314).addBox(-15F, -12.5F, -17F, 6F, 3F, 11F)
                .texOffs(27, 262).addBox(-13F, -14.5F, -17F, 2F, 7F, 11F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(275, 242).addBox(-9.5F, 7.6F, -22F, 19F, 3F, 15F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(63, 136).addBox(-9F, 6.8F, -7F, 18F, 2F, 27F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_back_spike1 = p_body.addOrReplaceChild("back_spike1", CubeListBuilder.create()
                .texOffs(264, 342).addBox(-2F, -3.226F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.2F, -0.174F, -0.25F))
                .texOffs(445, 360).addBox(-1.5F, -5.6015F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.33F, -0.2005F, -0.0375F))
                .texOffs(275, 374).addBox(-1F, -7.9505F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.46F, -0.2005F, -0.325F)),
                PartPose.offsetAndRotation(0F, -14.6F, -17F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike2 = p_body.addOrReplaceChild("back_spike2", CubeListBuilder.create()
                .texOffs(370, 329).addBox(-2F, -4.146F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.2F, -0.254F, -0.25F))
                .texOffs(343, 352).addBox(-1.5F, -7.2315F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.33F, -0.4105F, -0.0375F))
                .texOffs(169, 367).addBox(-1F, -10.1605F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.46F, -0.4105F, -0.325F)),
                PartPose.offsetAndRotation(0F, -14.6F, -9F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike3 = p_body.addOrReplaceChild("back_spike3", CubeListBuilder.create()
                .texOffs(389, 329).addBox(-2F, -4.251F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.2F, -0.149F, -0.25F))
                .texOffs(356, 352).addBox(-1.5F, -7.514F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.33F, -0.338F, -0.0375F))
                .texOffs(178, 367).addBox(-1F, -10.588F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.46F, -0.338F, -0.325F)),
                PartPose.offsetAndRotation(0F, -14.6F, -1F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike4 = p_body.addOrReplaceChild("back_spike4", CubeListBuilder.create()
                .texOffs(408, 329).addBox(-2F, -3.936F, -2.5F, 4F, 5F, 5F, new CubeDeformation(-0.2F, -0.464F, -0.25F))
                .texOffs(458, 360).addBox(-1.5F, -6.1665F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.33F, -0.0555F, -0.0375F))
                .texOffs(284, 374).addBox(-1F, -8.8055F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.46F, -0.0555F, -0.325F)),
                PartPose.offsetAndRotation(0F, -14.6F, 7F, -0.5236F, 0F, 0F));
        PartDefinition p_back_spike5 = p_body.addOrReplaceChild("back_spike5", CubeListBuilder.create()
                .texOffs(283, 342).addBox(-2F, -3.016F, -2.5F, 4F, 4F, 5F, new CubeDeformation(-0.2F, -0.384F, -0.25F))
                .texOffs(471, 360).addBox(-1.5F, -5.0365F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.33F, -0.3455F, -0.0375F))
                .texOffs(293, 374).addBox(-1F, -7.0955F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.46F, -0.3455F, -0.325F)),
                PartPose.offsetAndRotation(0F, -14.6F, 15F, -0.5236F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(121, 281).addBox(-7.5F, -3F, -9F, 15F, 6F, 10F, new CubeDeformation(-0.25F, -0.2F, -0.4F))
                .texOffs(251, 170).addBox(-3.5F, -7F, -9F, 7F, 14F, 10F, new CubeDeformation(-0.45F, 0F, -0.48F))
                .texOffs(220, 329).addBox(-3.5F, 5.8F, -8F, 7F, 2F, 8F, new CubeDeformation(-0.45F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -6F, -21F, -0.5236F, 0F, 0F));
        PartDefinition p_neck1_spike = p_neck1.addOrReplaceChild("neck1_spike", CubeListBuilder.create()
                .texOffs(52, 352).addBox(-2F, -2.117F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.283F, 0F))
                .texOffs(363, 367).addBox(-1.5F, -3.463F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.46F, -0.121F, -0.2F))
                .texOffs(503, 380).addBox(-0.5F, -4.971F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.02F, -0.121F, -0.4F)),
                PartPose.offsetAndRotation(0F, -6.6F, -3.6F, -0.5585F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(453, 281).addBox(-7F, -3F, -8.25F, 14F, 6F, 9F, new CubeDeformation(-0.25F, -0.4F, -0.15F))
                .texOffs(343, 197).addBox(-3F, -6.5F, -8.25F, 6F, 13F, 9F, new CubeDeformation(-0.15F, 0F, -0.23F))
                .texOffs(251, 329).addBox(-3F, 5.3F, -7.75F, 6F, 2F, 8F, new CubeDeformation(-0.15F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -8F, -0.2094F, 0F, 0F));
        PartDefinition p_neck2_spike = p_neck2.addOrReplaceChild("neck2_spike", CubeListBuilder.create()
                .texOffs(69, 352).addBox(-2F, -2.054F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.346F, 0F))
                .texOffs(376, 367).addBox(-1.5F, -3.2935F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.46F, -0.1645F, -0.2F))
                .texOffs(0, 385).addBox(-0.5F, -4.7145F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.02F, -0.1645F, -0.4F)),
                PartPose.offsetAndRotation(0F, -6.1F, -3.375F, -0.5585F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(164, 298).addBox(-6.5F, -2.5F, -8F, 13F, 5F, 9F, new CubeDeformation(-0.25F, -0.1F, -0.4F))
                .texOffs(106, 220).addBox(-3F, -6F, -8F, 6F, 12F, 9F, new CubeDeformation(-0.35F, 0F, -0.48F))
                .texOffs(187, 342).addBox(-3F, 4.8F, -7F, 6F, 2F, 7F, new CubeDeformation(-0.35F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.0349F, 0F, 0F));
        PartDefinition p_neck3_spike = p_neck3.addOrReplaceChild("neck3_spike", CubeListBuilder.create()
                .texOffs(103, 352).addBox(-1.5F, -1.991F, -2F, 3F, 3F, 4F, new CubeDeformation(0F, -0.409F, -0.125F))
                .texOffs(454, 367).addBox(-1F, -3.124F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.025F, -0.208F, -0.2812F))
                .texOffs(7, 385).addBox(-0.5F, -4.458F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.05F, -0.208F, -0.4375F)),
                PartPose.offsetAndRotation(0F, -5.6F, -3.15F, -0.5585F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(116, 314).addBox(-6F, -2.5F, -7.25F, 12F, 5F, 8F, new CubeDeformation(-0.25F, -0.3F, -0.15F))
                .texOffs(194, 242).addBox(-2.5F, -5.5F, -7.25F, 5F, 11F, 8F, new CubeDeformation(-0.05F, 0F, -0.23F))
                .texOffs(214, 342).addBox(-2.5F, 4.3F, -6.75F, 5F, 2F, 7F, new CubeDeformation(-0.05F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.1047F, 0F, 0F));
        PartDefinition p_neck4_spike = p_neck4.addOrReplaceChild("neck4_spike", CubeListBuilder.create()
                .texOffs(118, 352).addBox(-1.5F, -1.928F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.12F, -0.472F, -0.275F))
                .texOffs(465, 367).addBox(-1F, -2.9545F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.103F, -0.2515F, -0.3788F))
                .texOffs(14, 385).addBox(-0.5F, -4.2015F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.086F, -0.2515F, -0.4825F)),
                PartPose.offsetAndRotation(0F, -5.1F, -2.925F, -0.5585F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(411, 314).addBox(-5.5F, -2F, -7.25F, 11F, 4F, 8F, new CubeDeformation(-0.25F, 0F, -0.15F))
                .texOffs(54, 262).addBox(-2.5F, -5F, -7.25F, 5F, 10F, 8F, new CubeDeformation(-0.25F, 0F, -0.23F))
                .texOffs(239, 342).addBox(-2.5F, 3.8F, -6.75F, 5F, 2F, 7F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1571F, 0F, 0F));
        PartDefinition p_neck5_spike = p_neck5.addOrReplaceChild("neck5_spike", CubeListBuilder.create()
                .texOffs(430, 360).addBox(-1.5F, -1.365F, -2F, 3F, 2F, 4F, new CubeDeformation(-0.24F, -0.035F, -0.425F))
                .texOffs(476, 367).addBox(-1F, -2.785F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.181F, -0.295F, -0.4763F))
                .texOffs(111, 385).addBox(-0.5F, -3.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.122F, -0.295F, -0.0275F)),
                PartPose.offsetAndRotation(0F, -4.6F, -2.925F, -0.5585F, 0F, 0F));
        PartDefinition p_neck6 = p_neck5.addOrReplaceChild("neck6", CubeListBuilder.create()
                .texOffs(450, 314).addBox(-5F, -2F, -7F, 10F, 4F, 8F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(81, 262).addBox(-2.5F, -5F, -7F, 5F, 10F, 8F, new CubeDeformation(-0.35F, -0.25F, -0.48F))
                .texOffs(340, 342).addBox(-2.5F, 3.55F, -6F, 5F, 2F, 6F, new CubeDeformation(-0.35F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.1396F, 0F, 0F));
        PartDefinition p_neck6_spike = p_neck6.addOrReplaceChild("neck6_spike", CubeListBuilder.create()
                .texOffs(389, 367).addBox(-1.5F, -1.281F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.119F, 0F))
                .texOffs(341, 380).addBox(-1F, -2.559F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.22F, -0.353F, -0.025F))
                .texOffs(116, 385).addBox(-0.5F, -3.603F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.14F, -0.353F, -0.05F)),
                PartPose.offsetAndRotation(0F, -4.35F, -2.7F, -0.5585F, 0F, 0F));
        PartDefinition p_head = p_neck6.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 220).addBox(-6.5F, -3.95F, -13.25F, 13F, 6F, 15F, new CubeDeformation(0F, -0.35F, -0.25F))
                .texOffs(440, 136).addBox(-4F, -6.45F, -13.25F, 8F, 11F, 15F, new CubeDeformation(-0.1F, -0.25F, -0.33F))
                .texOffs(473, 342).addBox(-5.5F, -5F, -16F, 11F, 1F, 6F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(129, 342).addBox(-4F, -6F, -16F, 8F, 3F, 6F, new CubeDeformation(0F, 0F, -0.08F))
                .texOffs(299, 298).addBox(-5F, -2.7F, -21F, 10F, 5F, 9F, new CubeDeformation(-0.1F, -0.3F, 0F))
                .texOffs(448, 242).addBox(-3F, -4.7F, -21F, 6F, 9F, 9F, new CubeDeformation(-0.1F, -0.3F, -0.08F))
                .texOffs(194, 314).addBox(-4.5F, -2.45F, -28F, 9F, 5F, 8F, new CubeDeformation(-0.1F, -0.35F, 0F))
                .texOffs(297, 281).addBox(-3F, -3.95F, -28F, 6F, 8F, 8F, new CubeDeformation(-0.4F, -0.05F, -0.08F))
                .texOffs(86, 298).addBox(-3F, -5.5F, -26.5F, 6F, 2F, 13F, new CubeDeformation(-0.2F, -0.1F, 0F))
                .texOffs(33, 352).addBox(-3.5F, -3.9F, -29.3F, 7F, 5F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.4F))
                .texOffs(394, 385).addBox(1.1F, -3.25F, -29.3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.2F))
                .texOffs(153, 329).addBox(5F, -0.1F, -14F, 2F, 4F, 8F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(328, 314).addBox(5.5F, -0.6F, -14F, 1F, 5F, 8F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(484, 360).addBox(4.825F, -3.9F, -13.5F, 2F, 2F, 4F, new CubeDeformation(-0.075F, 0F, -0.1F))
                .texOffs(77, 385).addBox(3.5F, 3.5125F, -26.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4875F, -0.05F))
                .texOffs(121, 385).addBox(3.5F, 3.75F, -24.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(126, 385).addBox(3.5F, 3.75F, -22.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(131, 385).addBox(3.5F, 3.75F, -20.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(136, 385).addBox(3.5F, 3.75F, -18.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(82, 385).addBox(3.5F, 3.5125F, -16.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4875F, -0.05F))
                .texOffs(302, 374).addBox(2.6F, 4F, -26.6F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(401, 385).addBox(-3.1F, -3.25F, -29.3F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.05F, -0.2F))
                .texOffs(174, 329).addBox(-7F, -0.1F, -14F, 2F, 4F, 8F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(347, 314).addBox(-6.5F, -0.6F, -14F, 1F, 5F, 8F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(497, 360).addBox(-6.825F, -3.9F, -13.5F, 2F, 2F, 4F, new CubeDeformation(-0.075F, 0F, -0.1F))
                .texOffs(87, 385).addBox(-4.5F, 3.5125F, -26.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4875F, -0.05F))
                .texOffs(141, 385).addBox(-4.5F, 3.75F, -24.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(146, 385).addBox(-4.5F, 3.75F, -22.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(151, 385).addBox(-4.5F, 3.75F, -20.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(156, 385).addBox(-4.5F, 3.75F, -18.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(92, 385).addBox(-4.5F, 3.5125F, -16.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.4875F, -0.05F))
                .texOffs(311, 374).addBox(-4.6F, 4F, -26.6F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(161, 385).addBox(-2.7F, 3.6F, -27.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(166, 385).addBox(-1.25F, 3.6F, -27.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(171, 385).addBox(0.25F, 3.6F, -27.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(176, 385).addBox(1.7F, 3.6F, -27.9F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(0, 298).addBox(-4F, 3.67F, -27.45F, 8F, 1F, 14F, new CubeDeformation(-0.4F, -0.35F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.5236F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(68, 329).addBox(-2.5F, -1.6F, -0.6F, 5F, 3F, 9F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offsetAndRotation(4.4F, -5.4F, -12.8F, 0.1047F, 0F, -0.1745F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(97, 329).addBox(-2.5F, -1.6F, -0.6F, 5F, 3F, 9F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-4.4F, -5.4F, -12.8F, 0.1047F, 0F, 0.1745F));
        PartDefinition p_crest1 = p_head.addOrReplaceChild("crest1", CubeListBuilder.create()
                .texOffs(402, 367).addBox(-1.5F, -1.197F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.4F, -0.203F, -0.125F))
                .texOffs(350, 380).addBox(-1F, -2.333F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.285F, -0.411F, -0.1062F))
                .texOffs(181, 385).addBox(-0.5F, -3.261F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.411F, -0.0875F)),
                PartPose.offsetAndRotation(0F, -6F, -9F, -0.9599F, 0F, 0F));
        PartDefinition p_crest2 = p_head.addOrReplaceChild("crest2", CubeListBuilder.create()
                .texOffs(415, 367).addBox(-1.5F, -1.323F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.4F, -0.077F, -0.125F))
                .texOffs(359, 380).addBox(-1F, -2.672F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.285F, -0.324F, -0.1062F))
                .texOffs(186, 385).addBox(-0.5F, -3.774F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.324F, -0.0875F)),
                PartPose.offsetAndRotation(0F, -6F, -4.5F, -0.9599F, 0F, 0F));
        PartDefinition p_crest3 = p_head.addOrReplaceChild("crest3", CubeListBuilder.create()
                .texOffs(428, 367).addBox(-1.5F, -1.239F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.4F, -0.161F, -0.125F))
                .texOffs(368, 380).addBox(-1F, -2.446F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.285F, -0.382F, -0.1062F))
                .texOffs(191, 385).addBox(-0.5F, -3.432F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.17F, -0.382F, -0.0875F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(307, 329).addBox(-2.5F, -2.5F, -0.5F, 5F, 5F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(405, 342).addBox(-1.5F, -1.5F, 3.625F, 3F, 3F, 5F, new CubeDeformation(-0.136F, -0.136F, -0.375F)),
                PartPose.offsetAndRotation(4.6F, -5.6F, -3.5F, 0.4538F, 0.4189F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(302, 342).addBox(-2F, -2F, -0.6375F, 4F, 4F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.2375F))
                .texOffs(133, 352).addBox(-1.5F, -1.5F, 3.75F, 3F, 3F, 4F, new CubeDeformation(-0.446F, -0.446F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.1571F, 0.1047F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(148, 352).addBox(-1.5F, -1.5F, -0.4125F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.0125F))
                .texOffs(0, 367).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.194F, -0.194F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.1047F, 0.0349F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(13, 367).addBox(-1F, -1F, -0.6875F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2875F))
                .texOffs(487, 367).addBox(-1F, -1F, 2.75F, 2F, 2F, 3F, new CubeDeformation(-0.442F, -0.442F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.1745F, -0.0698F, 0F));
        PartDefinition p_horn_l_low1 = p_head.addOrReplaceChild("horn_l_low1", CubeListBuilder.create()
                .texOffs(163, 352).addBox(-1.5F, -1.5F, -0.6875F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2875F))
                .texOffs(498, 367).addBox(-1F, -1F, 2.75F, 2F, 2F, 3F, new CubeDeformation(-0.194F, -0.194F, 0F)),
                PartPose.offsetAndRotation(6F, -2.6F, -2.5F, 0.1047F, 0.733F, 0F));
        PartDefinition p_horn_l_low2 = p_horn_l_low1.addOrReplaceChild("horn_l_low2", CubeListBuilder.create()
                .texOffs(0, 374).addBox(-1F, -1F, -0.4625F, 2F, 2F, 3F, new CubeDeformation(-0.05F, -0.05F, -0.0625F))
                .texOffs(11, 374).addBox(-1F, -1F, 2F, 2F, 2F, 3F, new CubeDeformation(-0.411F, -0.411F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, 0.1047F, 0F));
        PartDefinition p_horn_l_low3 = p_horn_l_low2.addOrReplaceChild("horn_l_low3", CubeListBuilder.create()
                .texOffs(22, 374).addBox(-1F, -1F, -0.7375F, 2F, 2F, 3F, new CubeDeformation(-0.35F, -0.35F, -0.3375F))
                .texOffs(97, 385).addBox(-0.5F, -0.5F, 1.75F, 1F, 1F, 2F, new CubeDeformation(-0.097F, -0.097F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(377, 380).addBox(-1F, -1.365F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.035F, 0F))
                .texOffs(386, 380).addBox(-1F, -2.785F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.295F, -0.35F))
                .texOffs(196, 385).addBox(-0.5F, -3.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.295F, -0.2F)),
                PartPose.offsetAndRotation(7F, 1.5F, -7F, -1.7453F, 0.5236F, 0F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(395, 380).addBox(-1F, -1.239F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.161F, -0.125F))
                .texOffs(21, 385).addBox(-0.5F, -2.446F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.382F, -0.4313F))
                .texOffs(201, 385).addBox(-0.5F, -3.432F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.382F, -0.2375F)),
                PartPose.offsetAndRotation(6.8F, 3.6F, -4.5F, -1.9199F, 0.4538F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(328, 329).addBox(-2.5F, -2.5F, -0.5F, 5F, 5F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(422, 342).addBox(-1.5F, -1.5F, 3.625F, 3F, 3F, 5F, new CubeDeformation(-0.136F, -0.136F, -0.375F)),
                PartPose.offsetAndRotation(-4.6F, -5.6F, -3.5F, 0.4538F, -0.4189F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(321, 342).addBox(-2F, -2F, -0.6375F, 4F, 4F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.2375F))
                .texOffs(178, 352).addBox(-1.5F, -1.5F, 3.75F, 3F, 3F, 4F, new CubeDeformation(-0.446F, -0.446F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.1571F, -0.1047F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(193, 352).addBox(-1.5F, -1.5F, -0.4125F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.0125F))
                .texOffs(26, 367).addBox(-1F, -1F, 3F, 2F, 2F, 4F, new CubeDeformation(-0.194F, -0.194F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 7.5F, -0.1047F, -0.0349F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(39, 367).addBox(-1F, -1F, -0.6875F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2875F))
                .texOffs(33, 374).addBox(-1F, -1F, 2.75F, 2F, 2F, 3F, new CubeDeformation(-0.442F, -0.442F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, 0.1745F, 0.0698F, 0F));
        PartDefinition p_horn_r_low1 = p_head.addOrReplaceChild("horn_r_low1", CubeListBuilder.create()
                .texOffs(208, 352).addBox(-1.5F, -1.5F, -0.6875F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2875F))
                .texOffs(44, 374).addBox(-1F, -1F, 2.75F, 2F, 2F, 3F, new CubeDeformation(-0.194F, -0.194F, 0F)),
                PartPose.offsetAndRotation(-6F, -2.6F, -2.5F, 0.1047F, -0.733F, 0F));
        PartDefinition p_horn_r_low2 = p_horn_r_low1.addOrReplaceChild("horn_r_low2", CubeListBuilder.create()
                .texOffs(55, 374).addBox(-1F, -1F, -0.4625F, 2F, 2F, 3F, new CubeDeformation(-0.05F, -0.05F, -0.0625F))
                .texOffs(66, 374).addBox(-1F, -1F, 2F, 2F, 2F, 3F, new CubeDeformation(-0.411F, -0.411F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 5.5F, -0.1396F, -0.1047F, 0F));
        PartDefinition p_horn_r_low3 = p_horn_r_low2.addOrReplaceChild("horn_r_low3", CubeListBuilder.create()
                .texOffs(77, 374).addBox(-1F, -1F, -0.7375F, 2F, 2F, 3F, new CubeDeformation(-0.35F, -0.35F, -0.3375F))
                .texOffs(104, 385).addBox(-0.5F, -0.5F, 1.75F, 1F, 1F, 2F, new CubeDeformation(-0.097F, -0.097F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 4.5F, -0.1047F, 0F, 0F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(404, 380).addBox(-1F, -1.365F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.035F, 0F))
                .texOffs(413, 380).addBox(-1F, -2.785F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.295F, -0.35F))
                .texOffs(206, 385).addBox(-0.5F, -3.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.295F, -0.2F)),
                PartPose.offsetAndRotation(-7F, 1.5F, -7F, -1.7453F, -0.5236F, 0F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(422, 380).addBox(-1F, -1.239F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.161F, -0.125F))
                .texOffs(28, 385).addBox(-0.5F, -2.446F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.382F, -0.4313F))
                .texOffs(211, 385).addBox(-0.5F, -3.432F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.382F, -0.2375F)),
                PartPose.offsetAndRotation(-6.8F, 3.6F, -4.5F, -1.9199F, -0.4538F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(344, 242).addBox(-4.5F, 1.3F, -16.45F, 9F, 1F, 17F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(57, 220).addBox(-3.5F, -0.2F, -16.45F, 7F, 4F, 17F, new CubeDeformation(-0.3F, -0.2F, -0.33F))
                .texOffs(0, 352).addBox(-5.5F, 0.7F, -3F, 11F, 2F, 5F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(280, 329).addBox(-4F, -0.8F, -3F, 8F, 5F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.08F))
                .texOffs(158, 342).addBox(-3.5F, 2.4F, -15.8F, 7F, 2F, 7F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(216, 385).addBox(3.2F, -1.7775F, -15.1333F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.1225F, -0.075F))
                .texOffs(221, 385).addBox(3.2F, -1.55F, -13.2F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(226, 385).addBox(3.2F, -1.55F, -11.2667F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(231, 385).addBox(3.2F, -1.55F, -9.3333F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(236, 385).addBox(3.2F, -1.55F, -7.4F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(241, 385).addBox(3.2F, -1.7775F, -5.4667F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.1225F, -0.075F))
                .texOffs(246, 385).addBox(-4.2F, -1.7775F, -15.1333F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.1225F, -0.075F))
                .texOffs(251, 385).addBox(-4.2F, -1.55F, -13.2F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(256, 385).addBox(-4.2F, -1.55F, -11.2667F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(261, 385).addBox(-4.2F, -1.55F, -9.3333F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(266, 385).addBox(-4.2F, -1.55F, -7.4F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.35F, -0.075F))
                .texOffs(271, 385).addBox(-4.2F, -1.7775F, -5.4667F, 1F, 2F, 1F, new CubeDeformation(-0.075F, -0.1225F, -0.075F))
                .texOffs(108, 262).addBox(-4F, -0.5F, -15.8F, 8F, 1F, 16F, new CubeDeformation(-0.2F, -0.35F, 0F))
                .texOffs(157, 314).addBox(-3F, -0.7F, -14.5F, 6F, 1F, 12F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(0, 329).addBox(-3.5F, 3.3F, -11F, 7F, 1F, 11F, new CubeDeformation(-0.3F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, 4F, -11.5F, 0.0698F, 0F, 0F));
        PartDefinition p_beard1 = p_jaw.addOrReplaceChild("beard1", CubeListBuilder.create()
                .texOffs(431, 380).addBox(-1F, -1.197F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.203F, 0F))
                .texOffs(440, 380).addBox(-1F, -2.333F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.48F, -0.411F, -0.35F))
                .texOffs(276, 385).addBox(-0.5F, -3.261F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.411F, -0.2F)),
                PartPose.offsetAndRotation(0F, 3.8F, -6F, -2.618F, 0F, 0F));
        PartDefinition p_beard_l = p_jaw.addOrReplaceChild("beard_l", CubeListBuilder.create()
                .texOffs(449, 380).addBox(-1F, -1.113F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.287F, -0.125F))
                .texOffs(35, 385).addBox(-0.5F, -2.107F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.469F, -0.4313F))
                .texOffs(281, 385).addBox(-0.5F, -2.919F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.469F, -0.2375F)),
                PartPose.offsetAndRotation(2.6F, 3.6F, -3F, -2.618F, -0.1745F, 0F));
        PartDefinition p_beard_r = p_jaw.addOrReplaceChild("beard_r", CubeListBuilder.create()
                .texOffs(458, 380).addBox(-1F, -1.113F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.287F, -0.125F))
                .texOffs(42, 385).addBox(-0.5F, -2.107F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.469F, -0.4313F))
                .texOffs(286, 385).addBox(-0.5F, -2.919F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.469F, -0.2375F)),
                PartPose.offsetAndRotation(-2.6F, 3.6F, -3F, -2.618F, 0.1745F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(428, 197).addBox(-7F, -3F, -1F, 14F, 6F, 15F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(329, 136).addBox(-3.5F, -6.5F, -1F, 7F, 13F, 15F, new CubeDeformation(-0.4F, 0F, -0.48F))
                .texOffs(45, 298).addBox(-3.5F, 5.3F, 0F, 7F, 2F, 13F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, -4F, 20F, -0.1745F, 0F, 0F));
        PartDefinition p_tail1_spike = p_tail1.addOrReplaceChild("tail1_spike", CubeListBuilder.create()
                .texOffs(86, 352).addBox(-2F, -2.201F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, -0.199F, 0F))
                .texOffs(441, 367).addBox(-1.5F, -3.689F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.46F, -0.063F, -0.2F))
                .texOffs(49, 385).addBox(-0.5F, -5.313F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.02F, -0.063F, -0.4F)),
                PartPose.offsetAndRotation(0F, -6.1F, 5.85F, -0.5585F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(139, 242).addBox(-6.5F, -2.5F, -1F, 13F, 5F, 14F, new CubeDeformation(-0.4F, -0.26F, -0.4F))
                .texOffs(0, 170).addBox(-3F, -6F, -1F, 6F, 12F, 14F, new CubeDeformation(-0.26F, -0.4F, -0.48F))
                .texOffs(338, 298).addBox(-3F, 4.4F, 0F, 6F, 2F, 12F, new CubeDeformation(-0.26F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_tail2_spike = p_tail2.addOrReplaceChild("tail2_spike", CubeListBuilder.create()
                .texOffs(223, 352).addBox(-1.5F, -2.117F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.036F, -0.283F, -0.17F))
                .texOffs(88, 374).addBox(-1F, -3.463F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.0484F, -0.121F, -0.3105F))
                .texOffs(56, 385).addBox(-0.5F, -4.971F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.0608F, -0.121F, -0.451F)),
                PartPose.offsetAndRotation(0F, -5.2F, 5.4F, -0.5585F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(397, 242).addBox(-5.5F, -2F, -1F, 11F, 4F, 14F, new CubeDeformation(-0.2F, -0.08F, -0.4F))
                .texOffs(212, 170).addBox(-2.5F, -5F, -1F, 5F, 10F, 14F, new CubeDeformation(-0.08F, -0.2F, -0.48F))
                .texOffs(35, 314).addBox(-2.5F, 3.6F, 0F, 5F, 2F, 12F, new CubeDeformation(-0.08F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0698F, 0F, 0F));
        PartDefinition p_tail3_spike = p_tail3.addOrReplaceChild("tail3_spike", CubeListBuilder.create()
                .texOffs(238, 352).addBox(-1.5F, -2.033F, -2F, 3F, 3F, 4F, new CubeDeformation(-0.228F, -0.367F, -0.41F))
                .texOffs(99, 374).addBox(-1F, -3.237F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.1732F, -0.179F, -0.4665F))
                .texOffs(291, 385).addBox(-0.5F, -4.629F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1184F, -0.179F, -0.023F)),
                PartPose.offsetAndRotation(0F, -4.4F, 5.4F, -0.5585F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(157, 262).addBox(-5F, -2F, -1F, 10F, 4F, 13F, new CubeDeformation(-0.4F, -0.36F, -0.4F))
                .texOffs(306, 197).addBox(-2.5F, -4.5F, -1F, 5F, 9F, 13F, new CubeDeformation(-0.36F, -0.4F, -0.48F))
                .texOffs(264, 314).addBox(-2.5F, 2.9F, 0F, 5F, 2F, 11F, new CubeDeformation(-0.36F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 12F));
        PartDefinition p_tail4_spike = p_tail4.addOrReplaceChild("tail4_spike", CubeListBuilder.create()
                .texOffs(52, 367).addBox(-1.5F, -1.928F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.396F, -0.472F, -0.12F))
                .texOffs(467, 380).addBox(-1F, -2.9545F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2824F, -0.2515F, -0.103F))
                .texOffs(296, 385).addBox(-0.5F, -4.2015F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1688F, -0.2515F, -0.086F)),
                PartPose.offsetAndRotation(0F, -3.7F, 4.95F, -0.5585F, 0F, 0F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(172, 281).addBox(-4F, -1.5F, -1F, 8F, 3F, 13F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(211, 220).addBox(-2F, -3.5F, -1F, 4F, 7F, 13F, new CubeDeformation(-0.2F, 0F, -0.48F))
                .texOffs(297, 314).addBox(-2F, 2.3F, 0F, 4F, 2F, 11F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0524F, 0F, 0F));
        PartDefinition p_tail5_spike = p_tail5.addOrReplaceChild("tail5_spike", CubeListBuilder.create()
                .texOffs(110, 374).addBox(-1F, -1.323F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.064F, -0.077F, -0.33F))
                .texOffs(476, 380).addBox(-1F, -2.672F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3916F, -0.324F, -0.2395F))
                .texOffs(301, 385).addBox(-0.5F, -3.774F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2192F, -0.324F, -0.149F)),
                PartPose.offsetAndRotation(0F, -3.1F, 4.95F, -0.5585F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(125, 298).addBox(-3.5F, -1.5F, -1F, 7F, 3F, 12F, new CubeDeformation(-0.3F, -0.34F, -0.4F))
                .texOffs(479, 242).addBox(-1.5F, -3F, -1F, 3F, 6F, 12F, new CubeDeformation(-0.04F, -0.1F, -0.48F))
                .texOffs(126, 329).addBox(-1.5F, 1.7F, 0F, 3F, 2F, 10F, new CubeDeformation(-0.04F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0698F, 0F, 0F));
        PartDefinition p_tail6_spike = p_tail6.addOrReplaceChild("tail6_spike", CubeListBuilder.create()
                .texOffs(485, 380).addBox(-1F, -1.218F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.232F, -0.182F, -0.04F))
                .texOffs(63, 385).addBox(-0.5F, -2.3895F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.0008F, -0.3965F, -0.376F))
                .texOffs(306, 385).addBox(-0.5F, -3.3465F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3965F, -0.212F)),
                PartPose.offsetAndRotation(0F, -2.5F, 4.5F, -0.5585F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(229, 314).addBox(-3F, -1F, -1F, 6F, 2F, 11F, new CubeDeformation(-0.4F, -0.04F, -0.4F))
                .texOffs(326, 281).addBox(-1.5F, -2.5F, -1F, 3F, 5F, 11F, new CubeDeformation(-0.34F, -0.1F, -0.48F))
                .texOffs(195, 329).addBox(-1.5F, 1.2F, 0F, 3F, 2F, 9F, new CubeDeformation(-0.34F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail7_spike = p_tail7.addOrReplaceChild("tail7_spike", CubeListBuilder.create()
                .texOffs(494, 380).addBox(-1F, -1.113F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.287F, -0.125F))
                .texOffs(70, 385).addBox(-0.5F, -2.107F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.045F, -0.469F, -0.4313F))
                .texOffs(311, 385).addBox(-0.5F, -2.919F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.469F, -0.2375F)),
                PartPose.offsetAndRotation(0F, -2F, 4.05F, -0.5585F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(37, 329).addBox(-2.5F, -1F, -1F, 5F, 2F, 10F, new CubeDeformation(-0.4F, -0.24F, -0.4F))
                .texOffs(70, 314).addBox(-1F, -2F, -1F, 2F, 4F, 10F, new CubeDeformation(-0.04F, -0.1F, -0.48F))
                .texOffs(349, 329).addBox(-1F, 0.7F, 0F, 2F, 2F, 8F, new CubeDeformation(-0.04F, -0.2F, 0F))
                .texOffs(366, 314).addBox(-5.5F, -0.5F, 4F, 11F, 1F, 11F)
                .texOffs(95, 314).addBox(-0.5F, -5.25F, 3F, 1F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(286, 170).addBox(-5F, -4.25F, -3F, 10F, 18F, 6F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(374, 136).addBox(-3F, -4.25F, -5F, 6F, 18F, 10F, new CubeDeformation(-0.2F, -0.33F, 0F))
                .texOffs(375, 298).addBox(-6F, -5.75F, -3F, 12F, 8F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(137, 220).addBox(-3F, -5.75F, -6F, 6F, 8F, 12F, new CubeDeformation(-0.4F, -0.33F, -0.4F)),
                PartPose.offsetAndRotation(11.5F, 4F, -14F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(221, 242).addBox(-4F, -1.6F, -2.5F, 8F, 14F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.4F))
                .texOffs(374, 197).addBox(-2.5F, -1.6F, -4F, 5F, 14F, 8F, new CubeDeformation(-0.4F, -0.28F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.9F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(209, 298).addBox(-5F, 0.75F, -8.4F, 10F, 2F, 12F, new CubeDeformation(-0.25F, -0.3F, 0F))
                .texOffs(215, 281).addBox(-4F, -0.25F, -8.4F, 8F, 4F, 12F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(65, 367).addBox(-4.3667F, 1.55F, -11.3F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(121, 374).addBox(-4.3667F, 1.93F, -13.85F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.15F))
                .texOffs(78, 367).addBox(-1F, 1.55F, -11.3F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(132, 374).addBox(-1F, 1.93F, -13.85F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.15F))
                .texOffs(91, 367).addBox(2.3667F, 1.55F, -11.3F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(143, 374).addBox(2.3667F, 1.93F, -13.85F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.15F)),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(154, 136).addBox(-6F, -4.9F, -4.5F, 12F, 20F, 9F, new CubeDeformation(0F, -0.1F, -0.14F))
                .texOffs(133, 101).addBox(-3.5F, -4.9F, -7F, 7F, 20F, 14F, new CubeDeformation(-0.14F, -0.18F, 0F))
                .texOffs(204, 262).addBox(-7F, -6.6F, -4F, 14F, 9F, 8F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(41, 170).addBox(-3F, -6.6F, -8F, 6F, 9F, 16F, new CubeDeformation(0F, -0.08F, -0.4F)),
                PartPose.offsetAndRotation(12F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(246, 220).addBox(-4.5F, -1.85F, -2.5F, 9F, 15F, 5F, new CubeDeformation(-0.25F, -0.45F, -0.12F))
                .texOffs(352, 170).addBox(-2.5F, -1.35F, -4.5F, 5F, 14F, 9F, new CubeDeformation(-0.12F, -0.03F, -0.25F)),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(355, 281).addBox(-5.5F, 0.75F, -9.1F, 11F, 2F, 13F, new CubeDeformation(-0.25F, -0.3F, 0F))
                .texOffs(249, 262).addBox(-4.5F, -0.25F, -9.1F, 9F, 4F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(253, 352).addBox(-4.7F, 1.55F, -12.75F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(154, 374).addBox(-4.7F, 1.93F, -15.15F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(268, 352).addBox(-1F, 1.55F, -12.75F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(165, 374).addBox(-1F, 1.93F, -15.15F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(283, 352).addBox(2.7F, 1.55F, -12.75F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(176, 374).addBox(2.7F, 1.93F, -15.15F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.05F)),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(319, 170).addBox(-5F, -4.25F, -3F, 10F, 18F, 6F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(407, 136).addBox(-3F, -4.25F, -5F, 6F, 18F, 10F, new CubeDeformation(-0.2F, -0.33F, 0F))
                .texOffs(412, 298).addBox(-6F, -5.75F, -3F, 12F, 8F, 6F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(174, 220).addBox(-3F, -5.75F, -6F, 6F, 8F, 12F, new CubeDeformation(-0.4F, -0.33F, -0.4F)),
                PartPose.offsetAndRotation(-11.5F, 4F, -14F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(248, 242).addBox(-4F, -1.6F, -2.5F, 8F, 14F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.4F))
                .texOffs(401, 197).addBox(-2.5F, -1.6F, -4F, 5F, 14F, 8F, new CubeDeformation(-0.4F, -0.28F, -0.25F)),
                PartPose.offsetAndRotation(0F, 12.9F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(254, 298).addBox(-5F, 0.75F, -8.4F, 10F, 2F, 12F, new CubeDeformation(-0.25F, -0.3F, 0F))
                .texOffs(256, 281).addBox(-4F, -0.25F, -8.4F, 8F, 4F, 12F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(104, 367).addBox(-4.3667F, 1.55F, -11.3F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(187, 374).addBox(-4.3667F, 1.93F, -13.85F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.15F))
                .texOffs(117, 367).addBox(-1F, 1.55F, -11.3F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(198, 374).addBox(-1F, 1.93F, -13.85F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.15F))
                .texOffs(130, 367).addBox(2.3667F, 1.55F, -11.3F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(209, 374).addBox(2.3667F, 1.93F, -13.85F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.15F)),
                PartPose.offsetAndRotation(0F, 11.4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(197, 136).addBox(-6F, -4.9F, -4.5F, 12F, 20F, 9F, new CubeDeformation(0F, -0.1F, -0.14F))
                .texOffs(176, 101).addBox(-3.5F, -4.9F, -7F, 7F, 20F, 14F, new CubeDeformation(-0.14F, -0.18F, 0F))
                .texOffs(294, 262).addBox(-7F, -6.6F, -4F, 14F, 9F, 8F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(86, 170).addBox(-3F, -6.6F, -8F, 6F, 9F, 16F, new CubeDeformation(0F, -0.08F, -0.4F)),
                PartPose.offsetAndRotation(-12F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(275, 220).addBox(-4.5F, -1.85F, -2.5F, 9F, 15F, 5F, new CubeDeformation(-0.25F, -0.45F, -0.12F))
                .texOffs(381, 170).addBox(-2.5F, -1.35F, -4.5F, 5F, 14F, 9F, new CubeDeformation(-0.12F, -0.03F, -0.25F)),
                PartPose.offsetAndRotation(0F, 14.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(404, 281).addBox(-5.5F, 0.75F, -9.1F, 11F, 2F, 13F, new CubeDeformation(-0.25F, -0.3F, 0F))
                .texOffs(339, 262).addBox(-4.5F, -0.25F, -9.1F, 9F, 4F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.08F))
                .texOffs(298, 352).addBox(-4.7F, 1.55F, -12.75F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(220, 374).addBox(-4.7F, 1.93F, -15.15F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(313, 352).addBox(-1F, 1.55F, -12.75F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(231, 374).addBox(-1F, 1.93F, -15.15F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.05F))
                .texOffs(328, 352).addBox(2.7F, 1.55F, -12.75F, 2F, 2F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(242, 374).addBox(2.7F, 1.93F, -15.15F, 2F, 2F, 3F, new CubeDeformation(-0.335F, -0.145F, -0.05F)),
                PartPose.offsetAndRotation(0F, 11.9F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(47, 342).addBox(-1.3F, -3F, -1.5F, 17F, 6F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.29F))
                .texOffs(427, 329).addBox(-1.3F, -1.5F, -3F, 17F, 3F, 6F, new CubeDeformation(-0.38F, -0.29F, -0.25F))
                .texOffs(360, 360).addBox(13.9F, -2F, -1F, 15F, 4F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.208F))
                .texOffs(204, 360).addBox(13.9F, -1F, -2F, 15F, 2F, 4F, new CubeDeformation(-0.18F, -0.208F, -0.2F))
                .texOffs(219, 101).addBox(0F, 0F, 1.3F, 28F, 0F, 33F),
                PartPose.offset(11F, -11F, -12F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(243, 360).addBox(-0.8F, -2F, -1F, 17F, 4F, 2F, new CubeDeformation(-0.2F, -0.245F, -0.2278F))
                .texOffs(118, 360).addBox(-0.8F, -1F, -2F, 17F, 2F, 4F, new CubeDeformation(-0.28F, -0.2278F, -0.245F))
                .texOffs(277, 367).addBox(14F, -1.5F, -1F, 19F, 3F, 2F, new CubeDeformation(-0.4F, -0.2625F, -0.4555F))
                .texOffs(187, 367).addBox(14F, -1F, -1.5F, 19F, 2F, 3F, new CubeDeformation(-0.48F, -0.4555F, -0.2625F))
                .texOffs(294, 63).addBox(0F, 0F, 0.7375F, 32F, 0F, 34F),
                PartPose.offset(28F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(439, 342).addBox(-2.5F, -2.5F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.3625F, -0.3625F, -0.4313F))
                .texOffs(363, 342).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.4425F, -0.4313F, -0.3625F))
                .texOffs(143, 367).addBox(-1F, -1F, -5.6375F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(253, 374).addBox(-1F, -0.75F, -7.8375F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(32F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(369, 352).addBox(-0.46F, -1.5F, -1.5F, 29F, 3F, 3F, new CubeDeformation(-0.46F, -0.3F, -0.3F))
                .texOffs(320, 374).addBox(27F, -1F, -1F, 27F, 2F, 2F, new CubeDeformation(0F, -0.28F, -0.28F))
                .texOffs(0, 197).addBox(0F, 0F, 0.42F, 54F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1047F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(0, 360).addBox(0F, -1.5F, -1.5F, 26F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F))
                .texOffs(438, 374).addBox(25F, -1F, -1F, 25F, 2F, 2F, new CubeDeformation(0F, -0.37F, -0.37F))
                .texOffs(304, 220).addBox(0F, 0F, 0.33F, 50F, 0F, 19F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5236F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(55, 380).addBox(-0.06F, -1F, -1F, 23F, 2F, 2F, new CubeDeformation(-0.06F, -0.1F, -0.1F))
                .texOffs(157, 380).addBox(22F, -1F, -1F, 22F, 2F, 2F, new CubeDeformation(0F, -0.46F, -0.46F))
                .texOffs(384, 262).addBox(0F, 0F, 0.24F, 44F, 0F, 16F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9425F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(255, 380).addBox(-0.14F, -1F, -1F, 19F, 2F, 2F, new CubeDeformation(-0.14F, -0.25F, -0.25F))
                .texOffs(316, 385).addBox(18F, -0.5F, -0.5F, 18F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.05F))
                .texOffs(0, 63).addBox(0F, 0F, 0.15F, 36F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.3614F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(88, 342).addBox(-15.7F, -3F, -1.5F, 17F, 6F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.29F))
                .texOffs(0, 342).addBox(-15.7F, -1.5F, -3F, 17F, 3F, 6F, new CubeDeformation(-0.38F, -0.29F, -0.25F))
                .texOffs(395, 360).addBox(-28.9F, -2F, -1F, 15F, 4F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.208F))
                .texOffs(282, 360).addBox(-28.9F, -1F, -2F, 15F, 2F, 4F, new CubeDeformation(-0.18F, -0.208F, -0.2F))
                .texOffs(342, 101).addBox(-28F, 0F, 1.3F, 28F, 0F, 33F),
                PartPose.offset(-11F, -11F, -12F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(321, 360).addBox(-16.2F, -2F, -1F, 17F, 4F, 2F, new CubeDeformation(-0.2F, -0.245F, -0.2278F))
                .texOffs(161, 360).addBox(-16.2F, -1F, -2F, 17F, 2F, 4F, new CubeDeformation(-0.28F, -0.2278F, -0.245F))
                .texOffs(320, 367).addBox(-33F, -1.5F, -1F, 19F, 3F, 2F, new CubeDeformation(-0.4F, -0.2625F, -0.4555F))
                .texOffs(232, 367).addBox(-33F, -1F, -1.5F, 19F, 2F, 3F, new CubeDeformation(-0.48F, -0.4555F, -0.2625F))
                .texOffs(0, 101).addBox(-32F, 0F, 0.7375F, 32F, 0F, 34F),
                PartPose.offset(-28F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(456, 342).addBox(-2.5F, -2.5F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.3625F, -0.3625F, -0.4313F))
                .texOffs(384, 342).addBox(-2.5F, -1.5F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.4425F, -0.4313F, -0.3625F))
                .texOffs(156, 367).addBox(-1F, -1F, -5.6375F, 2F, 2F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(264, 374).addBox(-1F, -0.75F, -7.8375F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.3F)),
                PartPose.offset(-32F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(434, 352).addBox(-28.54F, -1.5F, -1.5F, 29F, 3F, 3F, new CubeDeformation(-0.46F, -0.3F, -0.3F))
                .texOffs(379, 374).addBox(-54F, -1F, -1F, 27F, 2F, 2F, new CubeDeformation(0F, -0.28F, -0.28F))
                .texOffs(153, 197).addBox(-54F, 0F, 0.42F, 54F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1047F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(59, 360).addBox(-26F, -1.5F, -1.5F, 26F, 3F, 3F, new CubeDeformation(0F, -0.45F, -0.45F))
                .texOffs(0, 380).addBox(-50F, -1F, -1F, 25F, 2F, 2F, new CubeDeformation(0F, -0.37F, -0.37F))
                .texOffs(0, 242).addBox(-50F, 0F, 0.33F, 50F, 0F, 19F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(106, 380).addBox(-22.94F, -1F, -1F, 23F, 2F, 2F, new CubeDeformation(-0.06F, -0.1F, -0.1F))
                .texOffs(206, 380).addBox(-44F, -1F, -1F, 22F, 2F, 2F, new CubeDeformation(0F, -0.46F, -0.46F))
                .texOffs(0, 281).addBox(-44F, 0F, 0.24F, 44F, 0F, 16F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9425F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(298, 380).addBox(-18.86F, -1F, -1F, 19F, 2F, 2F, new CubeDeformation(-0.14F, -0.25F, -0.25F))
                .texOffs(355, 385).addBox(-36F, -0.5F, -0.5F, 18F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.05F))
                .texOffs(147, 63).addBox(-36F, 0F, 0.15F, 36F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.3614F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
