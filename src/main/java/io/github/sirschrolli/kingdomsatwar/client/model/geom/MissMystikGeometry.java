package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie MissMystiks, der Feen-Zauberkünstlerin (Frack, Zylinder, Maske, zwei Paar Feenflügel, Zauberstab, Kartenfächer).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MissMystikGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private MissMystikGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(58, 50).addBox(-3F, -4F, -2F, 6F, 4F, 4F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(109, 50).addBox(-3F, -1.625F, -2F, 6F, 2F, 4F, new CubeDeformation(0F, -0.375F, 0F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(48, 37).addBox(-3.5F, -7F, -2F, 7F, 7F, 4F)
                .texOffs(43, 59).addBox(-1.5F, -8.375F, -1.5F, 3F, 2F, 3F, new CubeDeformation(0F, -0.375F, 0F))
                .texOffs(133, 59).addBox(-1F, -8.875F, -1F, 2F, 2F, 2F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(227, 59).addBox(-2F, -8.125F, -2.625F, 4F, 2F, 1F, new CubeDeformation(-0.25F, -0.375F, -0.125F))
                .texOffs(7, 66).addBox(1.4F, -5.6F, -2.65F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(161, 0).addBox(-3F, -7F, -3F, 6F, 7F, 6F)
                .texOffs(0, 50).addBox(-3.5F, -7.5F, -3.375F, 7F, 1F, 7F, new CubeDeformation(-0.25F, 0F, -0.375F))
                .texOffs(193, 59).addBox(-3.5F, -7.25F, -3.3F, 7F, 2F, 1F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(71, 37).addBox(-3.5F, -7F, 1.375F, 7F, 8F, 2F, new CubeDeformation(-0.25F, 0F, -0.125F))
                .texOffs(222, 37).addBox(-3.6F, -6.25F, -2.6F, 1F, 7F, 2F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(229, 37).addBox(2.6F, -6.25F, -2.6F, 1F, 7F, 2F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(56, 59).addBox(-3.8F, -4.75F, -0.75F, 1F, 3F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(63, 59).addBox(2.8F, -4.75F, -0.75F, 1F, 3F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(210, 59).addBox(-3.5F, -5F, -3.75F, 7F, 2F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, -8.5F, 0F));
        PartDefinition p_ponytail = p_head.addOrReplaceChild("ponytail", CubeListBuilder.create()
                .texOffs(122, 59).addBox(-1.5F, -0.975F, -1.025F, 3F, 2F, 2F, new CubeDeformation(0F, -0.375F, -0.125F))
                .texOffs(90, 37).addBox(-1.5F, 0.5F, -0.7F, 3F, 8F, 2F),
                PartPose.offsetAndRotation(0F, -6.25F, 3F, 0.5236F, 0F, 0F));
        PartDefinition p_ponytail_tip = p_ponytail.addOrReplaceChild("ponytail_tip", CubeListBuilder.create()
                .texOffs(211, 37).addBox(-1.5F, 0F, -1F, 3F, 7F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(142, 59).addBox(-1F, 6.75F, -1.25F, 2F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 8.25F, 0.25F, -0.3491F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(7, 37).addBox(-5F, -0.875F, -5F, 10F, 1F, 10F, new CubeDeformation(-0.25F, -0.125F, -0.25F))
                .texOffs(132, 0).addBox(-3.5F, -7.625F, -3.5F, 7F, 7F, 7F, new CubeDeformation(-0.25F, -0.125F, -0.25F))
                .texOffs(29, 50).addBox(-3.5F, -8.375F, -3.5F, 7F, 1F, 7F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(101, 37).addBox(-3.5F, -2.625F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.1F, -0.125F, -0.1F))
                .texOffs(151, 59).addBox(-1F, -3.125F, -3.9F, 2F, 3F, 1F, new CubeDeformation(0F, -0.375F, -0.25F))
                .texOffs(233, 50).addBox(2.525F, -3.875F, -2.125F, 2F, 3F, 3F, new CubeDeformation(-0.375F, -0.375F, -0.375F)),
                PartPose.offsetAndRotation(0F, -7.25F, 0F, -0.0524F, 0F, -0.1396F));
        PartDefinition p_feather = p_hat.addOrReplaceChild("feather", CubeListBuilder.create()
                .texOffs(0, 37).addBox(0F, -9F, -1.5F, 0F, 9F, 3F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(3.4F, -2.75F, 0.5F, -0.4538F, 0F, 0.2793F));
        PartDefinition p_right_arm = p_chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(79, 50).addBox(-1.5F, -1.375F, -1.975F, 3F, 3F, 4F, new CubeDeformation(0F, -0.125F, -0.375F))
                .texOffs(159, 37).addBox(-1.5F, -0.25F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(-4.4F, -6.25F, 0F, 0F, 0F, 0.1047F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(244, 50).addBox(-1F, -0.25F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(70, 59).addBox(-1.5F, 2.625F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.25F, -0.125F, -0.25F))
                .texOffs(83, 59).addBox(-1.5F, 3.375F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.125F, -0.4F))
                .texOffs(181, 50).addBox(-1.5F, 4F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, 5.25F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_wand = p_right_forearm.addOrReplaceChild("wand", CubeListBuilder.create()
                .texOffs(186, 0).addBox(-0.5F, -0.5F, -9.25F, 1F, 1F, 11F, new CubeDeformation(-0.15F, -0.15F, -0.25F))
                .texOffs(238, 59).addBox(-0.5F, -0.5F, -10.75F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.05F, -0.25F))
                .texOffs(12, 66).addBox(-0.5F, -0.5F, 1.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, 5.5F, -0.5F, 0.3142F, 0F, 0F));
        PartDefinition p_left_arm = p_chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(94, 50).addBox(-1.5F, -1.375F, -1.975F, 3F, 3F, 4F, new CubeDeformation(0F, -0.125F, -0.375F))
                .texOffs(172, 37).addBox(-1.5F, -0.25F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(4.4F, -6.25F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-1F, -0.25F, -1F, 2F, 4F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(96, 59).addBox(-1.5F, 2.625F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.25F, -0.125F, -0.25F))
                .texOffs(109, 59).addBox(-1.5F, 3.375F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.4F, -0.125F, -0.4F))
                .texOffs(194, 50).addBox(-1.5F, 4F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, 5.25F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_cards = p_left_forearm.addOrReplaceChild("cards", CubeListBuilder.create(),
                PartPose.offset(0F, 5.5F, -1F));
        PartDefinition p_card0 = p_cards.addOrReplaceChild("card0", CubeListBuilder.create()
                .texOffs(158, 59).addBox(-1.5F, -0.25F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_card1 = p_cards.addOrReplaceChild("card1", CubeListBuilder.create()
                .texOffs(165, 59).addBox(-1.5F, -0.25F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -0.05F, 0F, 0F, -0.3491F));
        PartDefinition p_card2 = p_cards.addOrReplaceChild("card2", CubeListBuilder.create()
                .texOffs(172, 59).addBox(-1.5F, -0.25F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, -0.1F));
        PartDefinition p_card3 = p_cards.addOrReplaceChild("card3", CubeListBuilder.create()
                .texOffs(179, 59).addBox(-1.5F, -0.25F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -0.15F, 0F, 0F, 0.3491F));
        PartDefinition p_card4 = p_cards.addOrReplaceChild("card4", CubeListBuilder.create()
                .texOffs(186, 59).addBox(-1.5F, -0.25F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -0.2F, 0F, 0F, 0.6981F));
        PartDefinition p_right_wing = p_chest.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, -16F, -0.5F, 0F, 17F, 19F),
                PartPose.offsetAndRotation(-1F, -5F, 2F, -0.1745F, -1.0821F, -0.2793F));
        PartDefinition p_right_wing_lower = p_chest.addOrReplaceChild("right_wing_lower", CubeListBuilder.create()
                .texOffs(78, 0).addBox(0F, -1.75F, -0.5F, 0F, 13F, 13F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-0.8F, -2.5F, 2F, 0.1396F, -0.8727F, -0.5585F));
        PartDefinition p_left_wing = p_chest.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(39, 0).addBox(0F, -16F, -0.5F, 0F, 17F, 19F),
                PartPose.offsetAndRotation(1F, -5F, 2F, -0.1745F, 1.0821F, 0.2793F));
        PartDefinition p_left_wing_lower = p_chest.addOrReplaceChild("left_wing_lower", CubeListBuilder.create()
                .texOffs(105, 0).addBox(0F, -1.75F, -0.5F, 0F, 13F, 13F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0.8F, -2.5F, 2F, 0.1396F, 0.8727F, 0.5585F));
        PartDefinition p_coattails = p_body.addOrReplaceChild("coattails", CubeListBuilder.create()
                .texOffs(211, 0).addBox(-3F, 0F, -0.25F, 6F, 11F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.5F, 1.75F, 0.2443F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(130, 50).addBox(-3F, 0F, -2F, 6F, 2F, 4F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(130, 37).addBox(-4F, 1.5F, -3F, 8F, 3F, 6F, new CubeDeformation(-0.25F, 0F, -0.4F)),
                PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_right_leg = p_body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(226, 0).addBox(-1.5F, -0.25F, -1.5F, 3F, 9F, 3F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-1.45F, 1.5F, 0F, -0.1745F, 0F, 0.0349F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(207, 50).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(185, 37).addBox(-1.5F, 2.75F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(9, 59).addBox(-2F, 2.375F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.4F, -0.125F, -0.4F))
                .texOffs(151, 50).addBox(-1.5F, 8.125F, -3.75F, 3F, 2F, 4F, new CubeDeformation(-0.2F, -0.375F, 0F))
                .texOffs(245, 59).addBox(-1F, 8.375F, 0.25F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.375F, 0F)),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.3142F, 0F, 0F));
        PartDefinition p_left_leg = p_body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(239, 0).addBox(-1.5F, -0.25F, -1.5F, 3F, 9F, 3F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(1.45F, 1.5F, 0F, 0.0698F, 0F, -0.0349F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(220, 50).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(198, 37).addBox(-1.5F, 2.75F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(26, 59).addBox(-2F, 2.375F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.4F, -0.125F, -0.4F))
                .texOffs(166, 50).addBox(-1.5F, 8.125F, -3.75F, 3F, 2F, 4F, new CubeDeformation(-0.2F, -0.375F, 0F))
                .texOffs(0, 66).addBox(-1F, 8.375F, 0.25F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.375F, 0F)),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.1396F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
