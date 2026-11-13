package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Seraphlings (Lichtbote Schrollis): Antlitz, Heiligenschein, zwei Paar Schwingen.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SeraphlingGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private SeraphlingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(29, 13).addBox(-2.5F, 0F, -2F, 5F, 5F, 4F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(42, 32).addBox(-3F, 0F, -2F, 6F, 1F, 4F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_robe_low = p_body.addOrReplaceChild("robe_low", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-3F, 0F, -2F, 6F, 6F, 4F),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -6F, -3F, 6F, 6F, 6F)
                .texOffs(0, 13).addBox(-3.5F, -6.9F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.2F, -0.3F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_halo = p_head.addOrReplaceChild("halo", CubeListBuilder.create(),
                PartPose.offset(0F, -8.2F, 0F));
        PartDefinition p_halo_0 = p_halo.addOrReplaceChild("halo_0", CubeListBuilder.create()
                .texOffs(55, 49).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(3.6F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_halo_1 = p_halo.addOrReplaceChild("halo_1", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(2.9125F, 0F, 2.116F, 0F, -2.1991F, 0F));
        PartDefinition p_halo_2 = p_halo.addOrReplaceChild("halo_2", CubeListBuilder.create()
                .texOffs(9, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(1.1125F, 0F, 3.4238F, 0F, -2.8274F, 0F));
        PartDefinition p_halo_3 = p_halo.addOrReplaceChild("halo_3", CubeListBuilder.create()
                .texOffs(18, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-1.1125F, 0F, 3.4238F, 0F, -3.4558F, 0F));
        PartDefinition p_halo_4 = p_halo.addOrReplaceChild("halo_4", CubeListBuilder.create()
                .texOffs(27, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-2.9125F, 0F, 2.116F, 0F, -4.0841F, 0F));
        PartDefinition p_halo_5 = p_halo.addOrReplaceChild("halo_5", CubeListBuilder.create()
                .texOffs(36, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-3.6F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_halo_6 = p_halo.addOrReplaceChild("halo_6", CubeListBuilder.create()
                .texOffs(45, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-2.9125F, 0F, -2.116F, 0F, -5.3407F, 0F));
        PartDefinition p_halo_7 = p_halo.addOrReplaceChild("halo_7", CubeListBuilder.create()
                .texOffs(54, 52).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-1.1125F, 0F, -3.4238F, 0F, -5.969F, 0F));
        PartDefinition p_halo_8 = p_halo.addOrReplaceChild("halo_8", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(1.1125F, 0F, -3.4238F, 0F, -6.5973F, 0F));
        PartDefinition p_halo_9 = p_halo.addOrReplaceChild("halo_9", CubeListBuilder.create()
                .texOffs(9, 55).addBox(-1.5F, -0.5F, -0.5F, 3F, 1F, 1F, new CubeDeformation(-0.169F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(2.9125F, 0F, -2.116F, 0F, -7.2257F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1F, -0.2F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(0, 45).addBox(-1F, 3.6F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(2.9F, 0.6F, 0F, -0.3491F, 0F, -0.1745F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(9, 32).addBox(-1F, -0.2F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(9, 45).addBox(-1F, 3.6F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-2.9F, 0.6F, 0F, -0.3491F, 0F, 0.1745F));
        PartDefinition p_wing_upper_left = p_body.addOrReplaceChild("wing_upper_left", CubeListBuilder.create()
                .texOffs(44, 45).addBox(0F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offsetAndRotation(1.2F, -1F, 1.4F, -0.0698F, -0.3491F, -0.6283F));
        PartDefinition p_wing_upper_left_f0 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f0", CubeListBuilder.create()
                .texOffs(24, 39).addBox(-1.5F, 0F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offsetAndRotation(0.6F, 0.4F, 0.05F, 0F, 0F, -0.0698F));
        PartDefinition p_wing_upper_left_f1 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f1", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-1.5F, -0.125F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.2F, -0.125F, 0F)),
                PartPose.offsetAndRotation(2.2F, 0.4F, 0.15F, 0F, 0F, -0.1571F));
        PartDefinition p_wing_upper_left_f2 = p_wing_upper_left.addOrReplaceChild("wing_upper_left_f2", CubeListBuilder.create()
                .texOffs(18, 32).addBox(-1.5F, -0.25F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.2F, -0.25F, 0F)),
                PartPose.offsetAndRotation(3.8F, 0.4F, 0.25F, 0F, 0F, -0.2443F));
        PartDefinition p_wing_upper_left_tip = p_wing_upper_left.addOrReplaceChild("wing_upper_left_tip", CubeListBuilder.create()
                .texOffs(18, 45).addBox(-0.25F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(-0.25F, 0F, -0.1F)),
                PartPose.offsetAndRotation(4F, 0F, 0F, 0F, -0.1396F, 0.1745F));
        PartDefinition p_wing_upper_left_tip_f0 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f0", CubeListBuilder.create()
                .texOffs(24, 23).addBox(-1.5F, 0F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(0.675F, 0.4F, 0.05F, 0F, 0F, -0.3491F));
        PartDefinition p_wing_upper_left_tip_f1 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f1", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-1.5F, 0F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(1.875F, 0.4F, 0.15F, 0F, 0F, -0.5585F));
        PartDefinition p_wing_upper_left_tip_f2 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f2", CubeListBuilder.create()
                .texOffs(48, 13).addBox(-1.5F, 0F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(3.075F, 0.4F, 0.25F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_upper_left_tip_f3 = p_wing_upper_left_tip.addOrReplaceChild("wing_upper_left_tip_f3", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1.5F, 0F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(4.275F, 0.4F, 0.35F, 0F, 0F, -0.9774F));
        PartDefinition p_wing_upper_right = p_body.addOrReplaceChild("wing_upper_right", CubeListBuilder.create()
                .texOffs(0, 49).addBox(-4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-1.2F, -1F, 1.4F, -0.0698F, 0.3491F, 0.6283F));
        PartDefinition p_wing_upper_right_f0 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f0", CubeListBuilder.create()
                .texOffs(31, 39).addBox(-1.5F, 0F, 0F, 3F, 4F, 0F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offsetAndRotation(-0.6F, 0.4F, 0.05F, 0F, 0F, 0.0698F));
        PartDefinition p_wing_upper_right_f1 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f1", CubeListBuilder.create()
                .texOffs(7, 39).addBox(-1.5F, -0.125F, 0F, 3F, 5F, 0F, new CubeDeformation(-0.2F, -0.125F, 0F)),
                PartPose.offsetAndRotation(-2.2F, 0.4F, 0.15F, 0F, 0F, 0.1571F));
        PartDefinition p_wing_upper_right_f2 = p_wing_upper_right.addOrReplaceChild("wing_upper_right_f2", CubeListBuilder.create()
                .texOffs(25, 32).addBox(-1.5F, -0.25F, 0F, 3F, 6F, 0F, new CubeDeformation(-0.2F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-3.8F, 0.4F, 0.25F, 0F, 0F, 0.2443F));
        PartDefinition p_wing_upper_right_tip = p_wing_upper_right.addOrReplaceChild("wing_upper_right_tip", CubeListBuilder.create()
                .texOffs(31, 45).addBox(-4.75F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(-0.25F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-4F, 0F, 0F, 0F, 0.1396F, -0.1745F));
        PartDefinition p_wing_upper_right_tip_f0 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f0", CubeListBuilder.create()
                .texOffs(31, 23).addBox(-1.5F, 0F, 0F, 3F, 7F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(-0.675F, 0.4F, 0.05F, 0F, 0F, 0.3491F));
        PartDefinition p_wing_upper_right_tip_f1 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f1", CubeListBuilder.create()
                .texOffs(7, 23).addBox(-1.5F, 0F, 0F, 3F, 8F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(-1.875F, 0.4F, 0.15F, 0F, 0F, 0.5585F));
        PartDefinition p_wing_upper_right_tip_f2 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f2", CubeListBuilder.create()
                .texOffs(55, 13).addBox(-1.5F, 0F, 0F, 3F, 9F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(-3.075F, 0.4F, 0.25F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_upper_right_tip_f3 = p_wing_upper_right_tip.addOrReplaceChild("wing_upper_right_tip_f3", CubeListBuilder.create()
                .texOffs(53, 0).addBox(-1.5F, 0F, 0F, 3F, 10F, 0F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(-4.275F, 0.4F, 0.35F, 0F, 0F, 0.9774F));
        PartDefinition p_wing_lower_left = p_body.addOrReplaceChild("wing_lower_left", CubeListBuilder.create()
                .texOffs(11, 49).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.1F)),
                PartPose.offsetAndRotation(1.2F, 1.6F, 1.2F, 0.0698F, -0.2793F, 0.384F));
        PartDefinition p_wing_lower_left_f0 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f0", CubeListBuilder.create()
                .texOffs(38, 39).addBox(-1F, -0.48F, 0F, 2F, 4F, 0F, new CubeDeformation(-0.012F, -0.48F, 0F)),
                PartPose.offsetAndRotation(0.48F, 0.4F, 0.05F, 0F, 0F, -0.0698F));
        PartDefinition p_wing_lower_left_f1 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f1", CubeListBuilder.create()
                .texOffs(43, 39).addBox(-1F, -0.195F, 0F, 2F, 4F, 0F, new CubeDeformation(-0.012F, -0.195F, 0F)),
                PartPose.offsetAndRotation(1.76F, 0.4F, 0.15F, 0F, 0F, -0.1571F));
        PartDefinition p_wing_lower_left_f2 = p_wing_lower_left.addOrReplaceChild("wing_lower_left_f2", CubeListBuilder.create()
                .texOffs(14, 39).addBox(-1F, -0.41F, 0F, 2F, 5F, 0F, new CubeDeformation(-0.012F, -0.41F, 0F)),
                PartPose.offsetAndRotation(3.04F, 0.4F, 0.25F, 0F, 0F, -0.2443F));
        PartDefinition p_wing_lower_left_tip = p_wing_lower_left.addOrReplaceChild("wing_lower_left_tip", CubeListBuilder.create()
                .texOffs(22, 49).addBox(-0.2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.1F)),
                PartPose.offsetAndRotation(3.2F, 0F, 0F, 0F, -0.1396F, 0.1745F));
        PartDefinition p_wing_lower_left_tip_f0 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f0", CubeListBuilder.create()
                .texOffs(32, 32).addBox(-1F, -0.34F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.088F, -0.34F, 0F)),
                PartPose.offsetAndRotation(0.54F, 0.4F, 0.05F, 0F, 0F, -0.3491F));
        PartDefinition p_wing_lower_left_tip_f1 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f1", CubeListBuilder.create()
                .texOffs(38, 23).addBox(-1F, -0.46F, 0F, 2F, 7F, 0F, new CubeDeformation(-0.088F, -0.46F, 0F)),
                PartPose.offsetAndRotation(1.5F, 0.4F, 0.15F, 0F, 0F, -0.5585F));
        PartDefinition p_wing_lower_left_tip_f2 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f2", CubeListBuilder.create()
                .texOffs(43, 23).addBox(-1F, -0.08F, 0F, 2F, 7F, 0F, new CubeDeformation(-0.088F, -0.08F, 0F)),
                PartPose.offsetAndRotation(2.46F, 0.4F, 0.25F, 0F, 0F, -0.7679F));
        PartDefinition p_wing_lower_left_tip_f3 = p_wing_lower_left_tip.addOrReplaceChild("wing_lower_left_tip_f3", CubeListBuilder.create()
                .texOffs(14, 23).addBox(-1F, -0.2F, 0F, 2F, 8F, 0F, new CubeDeformation(-0.088F, -0.2F, 0F)),
                PartPose.offsetAndRotation(3.42F, 0.4F, 0.35F, 0F, 0F, -0.9774F));
        PartDefinition p_wing_lower_right = p_body.addOrReplaceChild("wing_lower_right", CubeListBuilder.create()
                .texOffs(33, 49).addBox(-3.6F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-1.2F, 1.6F, 1.2F, 0.0698F, 0.2793F, -0.384F));
        PartDefinition p_wing_lower_right_f0 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f0", CubeListBuilder.create()
                .texOffs(48, 39).addBox(-1F, -0.48F, 0F, 2F, 4F, 0F, new CubeDeformation(-0.012F, -0.48F, 0F)),
                PartPose.offsetAndRotation(-0.48F, 0.4F, 0.05F, 0F, 0F, 0.0698F));
        PartDefinition p_wing_lower_right_f1 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f1", CubeListBuilder.create()
                .texOffs(53, 39).addBox(-1F, -0.195F, 0F, 2F, 4F, 0F, new CubeDeformation(-0.012F, -0.195F, 0F)),
                PartPose.offsetAndRotation(-1.76F, 0.4F, 0.15F, 0F, 0F, 0.1571F));
        PartDefinition p_wing_lower_right_f2 = p_wing_lower_right.addOrReplaceChild("wing_lower_right_f2", CubeListBuilder.create()
                .texOffs(19, 39).addBox(-1F, -0.41F, 0F, 2F, 5F, 0F, new CubeDeformation(-0.012F, -0.41F, 0F)),
                PartPose.offsetAndRotation(-3.04F, 0.4F, 0.25F, 0F, 0F, 0.2443F));
        PartDefinition p_wing_lower_right_tip = p_wing_lower_right.addOrReplaceChild("wing_lower_right_tip", CubeListBuilder.create()
                .texOffs(44, 49).addBox(-3.8F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-3.2F, 0F, 0F, 0F, 0.1396F, -0.1745F));
        PartDefinition p_wing_lower_right_tip_f0 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f0", CubeListBuilder.create()
                .texOffs(37, 32).addBox(-1F, -0.34F, 0F, 2F, 6F, 0F, new CubeDeformation(-0.088F, -0.34F, 0F)),
                PartPose.offsetAndRotation(-0.54F, 0.4F, 0.05F, 0F, 0F, 0.3491F));
        PartDefinition p_wing_lower_right_tip_f1 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f1", CubeListBuilder.create()
                .texOffs(48, 23).addBox(-1F, -0.46F, 0F, 2F, 7F, 0F, new CubeDeformation(-0.088F, -0.46F, 0F)),
                PartPose.offsetAndRotation(-1.5F, 0.4F, 0.15F, 0F, 0F, 0.5585F));
        PartDefinition p_wing_lower_right_tip_f2 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f2", CubeListBuilder.create()
                .texOffs(53, 23).addBox(-1F, -0.08F, 0F, 2F, 7F, 0F, new CubeDeformation(-0.088F, -0.08F, 0F)),
                PartPose.offsetAndRotation(-2.46F, 0.4F, 0.25F, 0F, 0F, 0.7679F));
        PartDefinition p_wing_lower_right_tip_f3 = p_wing_lower_right_tip.addOrReplaceChild("wing_lower_right_tip_f3", CubeListBuilder.create()
                .texOffs(19, 23).addBox(-1F, -0.2F, 0F, 2F, 8F, 0F, new CubeDeformation(-0.088F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-3.42F, 0.4F, 0.35F, 0F, 0F, 0.9774F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
