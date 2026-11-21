package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kraken (Kopf mit goldenen Augen, hoher Mantel mit Flossen, Schnabel, acht Arme in sechs Gliedern mit Saugnäpfen, zwei Fangarme in acht Gliedern mit Keule; im Spiel ×3).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class KrakenGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private KrakenGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-11F, -9F, -10F, 22F, 15F, 20F)
                .texOffs(85, 0).addBox(-12F, -6F, -8F, 24F, 10F, 16F)
                .texOffs(77, 61).addBox(-9F, 6F, -9F, 18F, 4F, 18F)
                .texOffs(185, 105).addBox(-8F, -11F, -8F, 16F, 2F, 15F)
                .texOffs(0, 124).addBox(-15.2F, -7F, -9.7F, 5F, 8F, 9F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(174, 159).addBox(-16.15F, -9.6F, -10.3F, 7F, 3F, 10F, new CubeDeformation(-0.25F, -0.2F, -0.1F))
                .texOffs(100, 187).addBox(-15.5F, 0.6F, -9.7F, 6F, 2F, 9F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(29, 124).addBox(10.2F, -7F, -9.7F, 5F, 8F, 9F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(209, 159).addBox(9.15F, -9.6F, -10.3F, 7F, 3F, 10F, new CubeDeformation(-0.25F, -0.2F, -0.1F))
                .texOffs(131, 187).addBox(9.5F, 0.6F, -9.7F, 6F, 2F, 9F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(186, 212).addBox(7F, 0F, 6F, 4F, 4F, 6F),
                PartPose.offset(0F, 18F, 0F));
        PartDefinition p_mantle = p_body.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(166, 0).addBox(-10.5F, -3F, -10.5F, 21F, 3F, 21F, new CubeDeformation(-0.2605F, 0F, -0.2605F))
                .texOffs(0, 36).addBox(-10.5F, -6F, -10.5F, 21F, 3F, 21F, new CubeDeformation(-0.3071F, 0F, -0.3071F))
                .texOffs(85, 36).addBox(-10.5F, -9F, -10.5F, 21F, 3F, 21F, new CubeDeformation(-0.4922F, 0F, -0.4922F))
                .texOffs(170, 36).addBox(-10F, -12F, -10F, 20F, 3F, 20F, new CubeDeformation(-0.3054F, 0F, -0.3054F))
                .texOffs(0, 61).addBox(-9.5F, -15F, -9.5F, 19F, 3F, 19F, new CubeDeformation(-0.2407F, 0F, -0.2407F))
                .texOffs(150, 61).addBox(-9F, -18F, -9F, 18F, 3F, 18F, new CubeDeformation(-0.2934F, 0F, -0.2934F))
                .texOffs(0, 84).addBox(-8.5F, -21F, -8.5F, 17F, 3F, 17F, new CubeDeformation(-0.4593F, 0F, -0.4593F))
                .texOffs(69, 84).addBox(-7.5F, -24F, -7.5F, 15F, 3F, 15F, new CubeDeformation(-0.2353F, 0F, -0.2353F))
                .texOffs(58, 124).addBox(-6.5F, -27F, -6.5F, 13F, 3F, 13F, new CubeDeformation(-0.1209F, 0F, -0.1209F))
                .texOffs(132, 142).addBox(-5.5F, -30F, -5.5F, 11F, 3F, 11F, new CubeDeformation(-0.1215F, 0F, -0.1215F))
                .texOffs(0, 174).addBox(-4.5F, -33F, -4.5F, 9F, 3F, 9F, new CubeDeformation(-0.2537F, 0F, -0.2537F))
                .texOffs(126, 224).addBox(-3F, -36F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.0617F, 0F, -0.0617F))
                .texOffs(77, 235).addBox(-2F, -39F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(239, 224).addBox(-1.5F, -12F, -10.5668F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(0, 235).addBox(-1.5F, -18F, -9.6974F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(11, 235).addBox(-1.5F, -24F, -8.3664F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(22, 235).addBox(-1.5F, -30F, -6.5938F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(33, 235).addBox(-1.5F, -12F, 8.5668F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(44, 235).addBox(-1.5F, -18F, 7.6974F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(55, 235).addBox(-1.5F, -24F, 6.3664F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(66, 235).addBox(-1.5F, -30F, 4.5938F, 3F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -10F, 1F, -0.2443F, 0F, 0F));
        PartDefinition p_fin_left = p_mantle.addOrReplaceChild("fin_left", CubeListBuilder.create()
                .texOffs(114, 249).addBox(-0.1F, 2.55F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.2F))
                .texOffs(46, 249).addBox(0F, 0.85F, -1F, 6F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(189, 244).addBox(-0.3F, -0.85F, -1F, 9F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.2F))
                .texOffs(209, 235).addBox(-0.45F, -2.55F, -1F, 11F, 2F, 2F, new CubeDeformation(-0.45F, -0.15F, -0.2F))
                .texOffs(0, 244).addBox(0F, -4.25F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(27, 244).addBox(0F, -5.95F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(54, 244).addBox(-0.45F, -7.65F, -1F, 11F, 2F, 2F, new CubeDeformation(-0.45F, -0.15F, -0.2F))
                .texOffs(212, 244).addBox(-0.3F, -9.35F, -1F, 9F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.2F))
                .texOffs(63, 249).addBox(0F, -11.05F, -1F, 6F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(125, 249).addBox(-0.1F, -12.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.2F)),
                PartPose.offsetAndRotation(5.1997F, -29F, 0F, 0F, 0F, -0.1047F));
        PartDefinition p_fin_right = p_mantle.addOrReplaceChild("fin_right", CubeListBuilder.create()
                .texOffs(136, 249).addBox(-2.9F, 2.55F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.2F))
                .texOffs(80, 249).addBox(-6F, 0.85F, -1F, 6F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(0, 249).addBox(-8.7F, -0.85F, -1F, 9F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.2F))
                .texOffs(81, 244).addBox(-10.55F, -2.55F, -1F, 11F, 2F, 2F, new CubeDeformation(-0.45F, -0.15F, -0.2F))
                .texOffs(108, 244).addBox(-11F, -4.25F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(135, 244).addBox(-11F, -5.95F, -1F, 11F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(162, 244).addBox(-10.55F, -7.65F, -1F, 11F, 2F, 2F, new CubeDeformation(-0.45F, -0.15F, -0.2F))
                .texOffs(23, 249).addBox(-8.7F, -9.35F, -1F, 9F, 2F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.2F))
                .texOffs(97, 249).addBox(-6F, -11.05F, -1F, 6F, 2F, 2F, new CubeDeformation(0F, -0.15F, -0.2F))
                .texOffs(147, 249).addBox(-2.9F, -12.75F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.1F, -0.15F, -0.2F)),
                PartPose.offsetAndRotation(-5.1997F, -29F, 0F, 0F, 0F, 0.1047F));
        PartDefinition p_beak = p_body.addOrReplaceChild("beak", CubeListBuilder.create()
                .texOffs(161, 212).addBox(-3F, 0F, -3F, 6F, 4F, 6F)
                .texOffs(196, 235).addBox(-1.5F, 3.5F, -1.5F, 3F, 2F, 3F),
                PartPose.offset(0F, 9F, -1F));
        PartDefinition p_arm0 = p_body.addOrReplaceChild("arm0", CubeListBuilder.create()
                .texOffs(130, 84).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(-3.0615F, 8F, -7.391F, 0.5585F, 0.3927F, 0F));
        PartDefinition p_arm0_1 = p_arm0.addOrReplaceChild("arm0_1", CubeListBuilder.create()
                .texOffs(111, 124).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm0_2 = p_arm0_1.addOrReplaceChild("arm0_2", CubeListBuilder.create()
                .texOffs(177, 142).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm0_3 = p_arm0_2.addOrReplaceChild("arm0_3", CubeListBuilder.create()
                .texOffs(37, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm0_4 = p_arm0_3.addOrReplaceChild("arm0_4", CubeListBuilder.create()
                .texOffs(162, 187).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm0_5 = p_arm0_4.addOrReplaceChild("arm0_5", CubeListBuilder.create()
                .texOffs(207, 212).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm1 = p_body.addOrReplaceChild("arm1", CubeListBuilder.create()
                .texOffs(167, 84).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(-7.391F, 8F, -3.0615F, 0.5585F, 1.1781F, 0F));
        PartDefinition p_arm1_1 = p_arm1.addOrReplaceChild("arm1_1", CubeListBuilder.create()
                .texOffs(144, 124).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm1_2 = p_arm1_1.addOrReplaceChild("arm1_2", CubeListBuilder.create()
                .texOffs(206, 142).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm1_3 = p_arm1_2.addOrReplaceChild("arm1_3", CubeListBuilder.create()
                .texOffs(64, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm1_4 = p_arm1_3.addOrReplaceChild("arm1_4", CubeListBuilder.create()
                .texOffs(187, 187).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm1_5 = p_arm1_4.addOrReplaceChild("arm1_5", CubeListBuilder.create()
                .texOffs(228, 212).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm2 = p_body.addOrReplaceChild("arm2", CubeListBuilder.create()
                .texOffs(204, 84).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(-7.391F, 8F, 3.0615F, 0.5585F, 1.9635F, 0F));
        PartDefinition p_arm2_1 = p_arm2.addOrReplaceChild("arm2_1", CubeListBuilder.create()
                .texOffs(177, 124).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm2_2 = p_arm2_1.addOrReplaceChild("arm2_2", CubeListBuilder.create()
                .texOffs(0, 159).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm2_3 = p_arm2_2.addOrReplaceChild("arm2_3", CubeListBuilder.create()
                .texOffs(91, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm2_4 = p_arm2_3.addOrReplaceChild("arm2_4", CubeListBuilder.create()
                .texOffs(212, 187).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm2_5 = p_arm2_4.addOrReplaceChild("arm2_5", CubeListBuilder.create()
                .texOffs(0, 224).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm3 = p_body.addOrReplaceChild("arm3", CubeListBuilder.create()
                .texOffs(0, 105).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(-3.0615F, 8F, 7.391F, 0.5585F, 2.7489F, 0F));
        PartDefinition p_arm3_1 = p_arm3.addOrReplaceChild("arm3_1", CubeListBuilder.create()
                .texOffs(210, 124).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm3_2 = p_arm3_1.addOrReplaceChild("arm3_2", CubeListBuilder.create()
                .texOffs(29, 159).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm3_3 = p_arm3_2.addOrReplaceChild("arm3_3", CubeListBuilder.create()
                .texOffs(118, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm3_4 = p_arm3_3.addOrReplaceChild("arm3_4", CubeListBuilder.create()
                .texOffs(0, 200).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm3_5 = p_arm3_4.addOrReplaceChild("arm3_5", CubeListBuilder.create()
                .texOffs(21, 224).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm4 = p_body.addOrReplaceChild("arm4", CubeListBuilder.create()
                .texOffs(37, 105).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(3.0615F, 8F, 7.391F, 0.5585F, 3.5343F, 0F));
        PartDefinition p_arm4_1 = p_arm4.addOrReplaceChild("arm4_1", CubeListBuilder.create()
                .texOffs(0, 142).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm4_2 = p_arm4_1.addOrReplaceChild("arm4_2", CubeListBuilder.create()
                .texOffs(58, 159).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm4_3 = p_arm4_2.addOrReplaceChild("arm4_3", CubeListBuilder.create()
                .texOffs(145, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm4_4 = p_arm4_3.addOrReplaceChild("arm4_4", CubeListBuilder.create()
                .texOffs(25, 200).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm4_5 = p_arm4_4.addOrReplaceChild("arm4_5", CubeListBuilder.create()
                .texOffs(42, 224).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm5 = p_body.addOrReplaceChild("arm5", CubeListBuilder.create()
                .texOffs(74, 105).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(7.391F, 8F, 3.0615F, 0.5585F, 4.3197F, 0F));
        PartDefinition p_arm5_1 = p_arm5.addOrReplaceChild("arm5_1", CubeListBuilder.create()
                .texOffs(33, 142).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm5_2 = p_arm5_1.addOrReplaceChild("arm5_2", CubeListBuilder.create()
                .texOffs(87, 159).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm5_3 = p_arm5_2.addOrReplaceChild("arm5_3", CubeListBuilder.create()
                .texOffs(172, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm5_4 = p_arm5_3.addOrReplaceChild("arm5_4", CubeListBuilder.create()
                .texOffs(50, 200).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm5_5 = p_arm5_4.addOrReplaceChild("arm5_5", CubeListBuilder.create()
                .texOffs(63, 224).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm6 = p_body.addOrReplaceChild("arm6", CubeListBuilder.create()
                .texOffs(111, 105).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(7.391F, 8F, -3.0615F, 0.5585F, 5.1051F, 0F));
        PartDefinition p_arm6_1 = p_arm6.addOrReplaceChild("arm6_1", CubeListBuilder.create()
                .texOffs(66, 142).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm6_2 = p_arm6_1.addOrReplaceChild("arm6_2", CubeListBuilder.create()
                .texOffs(116, 159).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm6_3 = p_arm6_2.addOrReplaceChild("arm6_3", CubeListBuilder.create()
                .texOffs(199, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm6_4 = p_arm6_3.addOrReplaceChild("arm6_4", CubeListBuilder.create()
                .texOffs(75, 200).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm6_5 = p_arm6_4.addOrReplaceChild("arm6_5", CubeListBuilder.create()
                .texOffs(84, 224).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_arm7 = p_body.addOrReplaceChild("arm7", CubeListBuilder.create()
                .texOffs(148, 105).addBox(-3.5F, -3.5F, -11F, 7F, 7F, 11F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offsetAndRotation(3.0615F, 8F, -7.391F, 0.5585F, 5.8905F, 0F));
        PartDefinition p_arm7_1 = p_arm7.addOrReplaceChild("arm7_1", CubeListBuilder.create()
                .texOffs(99, 142).addBox(-3F, -3F, -10F, 6F, 6F, 10F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10.6F, -0.1396F, 0F, 0F));
        PartDefinition p_arm7_2 = p_arm7_1.addOrReplaceChild("arm7_2", CubeListBuilder.create()
                .texOffs(145, 159).addBox(-2.5F, -2.5F, -9F, 5F, 5F, 9F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -9.6F, -0.2443F, 0F, 0F));
        PartDefinition p_arm7_3 = p_arm7_2.addOrReplaceChild("arm7_3", CubeListBuilder.create()
                .texOffs(226, 174).addBox(-2.5F, -2F, -8F, 5F, 4F, 8F, new CubeDeformation(-0.45F, -0.155F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -8.6F, -0.3142F, 0F, 0F));
        PartDefinition p_arm7_4 = p_arm7_3.addOrReplaceChild("arm7_4", CubeListBuilder.create()
                .texOffs(100, 200).addBox(-2F, -1.5F, -7.75F, 4F, 3F, 8F, new CubeDeformation(-0.4F, -0.06F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -7.6F, -0.384F, 0F, 0F));
        PartDefinition p_arm7_5 = p_arm7_4.addOrReplaceChild("arm7_5", CubeListBuilder.create()
                .texOffs(105, 224).addBox(-1.5F, -1.5F, -7F, 3F, 3F, 7F, new CubeDeformation(-0.3F, -0.42F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.1F, -0.4538F, 0F, 0F));
        PartDefinition p_tentacle_left = p_body.addOrReplaceChild("tentacle_left", CubeListBuilder.create()
                .texOffs(0, 187).addBox(-2F, -2F, -8F, 4F, 4F, 8F, new CubeDeformation(-0.35F, -0.35F, 0F)),
                PartPose.offsetAndRotation(2.8168F, 4F, -8.2977F, 0.3142F, -0.4887F, 0F));
        PartDefinition p_tentacle_left_1 = p_tentacle_left.addOrReplaceChild("tentacle_left_1", CubeListBuilder.create()
                .texOffs(25, 187).addBox(-2F, -2F, -8F, 4F, 4F, 8F, new CubeDeformation(-0.425F, -0.425F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_left_2 = p_tentacle_left_1.addOrReplaceChild("tentacle_left_2", CubeListBuilder.create()
                .texOffs(125, 200).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_left_3 = p_tentacle_left_2.addOrReplaceChild("tentacle_left_3", CubeListBuilder.create()
                .texOffs(148, 200).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.075F, -0.075F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_left_4 = p_tentacle_left_3.addOrReplaceChild("tentacle_left_4", CubeListBuilder.create()
                .texOffs(171, 200).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.15F, -0.15F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_left_5 = p_tentacle_left_4.addOrReplaceChild("tentacle_left_5", CubeListBuilder.create()
                .texOffs(194, 200).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.225F, -0.225F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1745F, 0F, 0F));
        PartDefinition p_tentacle_left_6 = p_tentacle_left_5.addOrReplaceChild("tentacle_left_6", CubeListBuilder.create()
                .texOffs(217, 200).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1745F, 0F, 0F));
        PartDefinition p_tentacle_left_7 = p_tentacle_left_6.addOrReplaceChild("tentacle_left_7", CubeListBuilder.create()
                .texOffs(0, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.375F, -0.375F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1745F, 0F, 0F));
        PartDefinition p_tentacle_left_club = p_tentacle_left_7.addOrReplaceChild("tentacle_left_club", CubeListBuilder.create()
                .texOffs(132, 235).addBox(-2.5F, -1.5F, -3F, 5F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, 0F))
                .texOffs(197, 224).addBox(-3F, -2F, -6.75F, 6F, 4F, 4F, new CubeDeformation(-0.1F, -0.4F, -0.25F))
                .texOffs(151, 224).addBox(-3.5F, -2F, -10.25F, 7F, 4F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.25F))
                .texOffs(94, 235).addBox(-3F, -1.5F, -13F, 6F, 3F, 3F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(166, 235).addBox(-2F, -1.5F, -15.7F, 4F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(158, 249).addBox(-1.5F, -1F, -17.4F, 3F, 2F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.2094F, 0F, 0F));
        PartDefinition p_tentacle_right = p_body.addOrReplaceChild("tentacle_right", CubeListBuilder.create()
                .texOffs(50, 187).addBox(-2F, -2F, -8F, 4F, 4F, 8F, new CubeDeformation(-0.35F, -0.35F, 0F)),
                PartPose.offsetAndRotation(-2.8168F, 4F, -8.2977F, 0.3142F, 0.4887F, 0F));
        PartDefinition p_tentacle_right_1 = p_tentacle_right.addOrReplaceChild("tentacle_right_1", CubeListBuilder.create()
                .texOffs(75, 187).addBox(-2F, -2F, -8F, 4F, 4F, 8F, new CubeDeformation(-0.425F, -0.425F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_right_2 = p_tentacle_right_1.addOrReplaceChild("tentacle_right_2", CubeListBuilder.create()
                .texOffs(23, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_right_3 = p_tentacle_right_2.addOrReplaceChild("tentacle_right_3", CubeListBuilder.create()
                .texOffs(46, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.075F, -0.075F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_right_4 = p_tentacle_right_3.addOrReplaceChild("tentacle_right_4", CubeListBuilder.create()
                .texOffs(69, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.15F, -0.15F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1047F, 0F, 0F));
        PartDefinition p_tentacle_right_5 = p_tentacle_right_4.addOrReplaceChild("tentacle_right_5", CubeListBuilder.create()
                .texOffs(92, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.225F, -0.225F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1745F, 0F, 0F));
        PartDefinition p_tentacle_right_6 = p_tentacle_right_5.addOrReplaceChild("tentacle_right_6", CubeListBuilder.create()
                .texOffs(115, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1745F, 0F, 0F));
        PartDefinition p_tentacle_right_7 = p_tentacle_right_6.addOrReplaceChild("tentacle_right_7", CubeListBuilder.create()
                .texOffs(138, 212).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F, new CubeDeformation(-0.375F, -0.375F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.1745F, 0F, 0F));
        PartDefinition p_tentacle_right_club = p_tentacle_right_7.addOrReplaceChild("tentacle_right_club", CubeListBuilder.create()
                .texOffs(149, 235).addBox(-2.5F, -1.5F, -3F, 5F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, 0F))
                .texOffs(218, 224).addBox(-3F, -2F, -6.75F, 6F, 4F, 4F, new CubeDeformation(-0.1F, -0.4F, -0.25F))
                .texOffs(174, 224).addBox(-3.5F, -2F, -10.25F, 7F, 4F, 4F, new CubeDeformation(-0.2F, -0.4F, -0.25F))
                .texOffs(113, 235).addBox(-3F, -1.5F, -13F, 6F, 3F, 3F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(181, 235).addBox(-2F, -1.5F, -15.7F, 4F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(169, 249).addBox(-1.5F, -1F, -17.4F, 3F, 2F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.7F, -0.2094F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
