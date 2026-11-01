package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Ziegen (Hörner je Art, einzeln abbrechbar).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FarmGoatGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private FarmGoatGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -4F, -8F, 8F, 8F, 15F)
                .texOffs(56, 36).addBox(-3.5F, -4.8F, -7.5F, 7F, 1F, 6F),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(47, 0).addBox(-4.5F, 0.5F, -8.2F, 9F, 5.5F, 15.5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(98, 0).addBox(-2F, -5.5F, -3F, 4F, 6.5F, 4.5F)
                .texOffs(0, 24).addBox(-2.5F, -8.5F, -5.5F, 5F, 4.5F, 6F)
                .texOffs(17, 36).addBox(-1.75F, -7.2F, -8.6F, 3.5F, 3.2F, 3.2F)
                .texOffs(117, 46).addBox(2.5F, -7.5F, -4.2F, 0.3F, 1F, 1.5F)
                .texOffs(0, 53).addBox(-2.8F, -7.5F, -4.2F, 0.3F, 1F, 1.5F),
                PartPose.offset(0F, -3F, -7F));
        PartDefinition p_beard = p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(36, 46).addBox(-0.75F, 0F, -1F, 1.5F, 2.6F, 1.5F),
                PartPose.offsetAndRotation(0F, -4.3F, -5.4F, -0.1396F, 0F, 0F));
        PartDefinition p_beard_long = p_head.addOrReplaceChild("beard_long", CubeListBuilder.create()
                .texOffs(83, 36).addBox(-1.25F, 0F, -1.3F, 2.5F, 3.6F, 2.1F),
                PartPose.offsetAndRotation(0F, -4.3F, -5.2F, -0.1396F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(91, 46).addBox(0F, -0.5F, -0.8F, 3.2F, 1F, 1.8F),
                PartPose.offsetAndRotation(2.4F, -7.6F, -1.8F, 0F, -0.3491F, 0.4887F));
        PartDefinition p_horn_ibex_l = p_head.addOrReplaceChild("horn_ibex_l", CubeListBuilder.create()
                .texOffs(34, 36).addBox(-1F, -4.6F, -1.1F, 2F, 4.6F, 2.2F),
                PartPose.offsetAndRotation(1.3F, -8.4F, -3.6F, -0.5236F, 0F, 0.1047F));
        PartDefinition p_horn_ibex_mid_l = p_horn_ibex_l.addOrReplaceChild("horn_ibex_mid_l", CubeListBuilder.create()
                .texOffs(96, 36).addBox(-0.85F, -4.2F, -0.95F, 1.7F, 4.2F, 1.9F),
                PartPose.offsetAndRotation(0F, -4.4F, 0F, -0.733F, 0F, 0.0698F));
        PartDefinition p_horn_ibex_tip_l = p_horn_ibex_mid_l.addOrReplaceChild("horn_ibex_tip_l", CubeListBuilder.create()
                .texOffs(114, 36).addBox(-0.65F, -3.6F, -0.75F, 1.3F, 3.6F, 1.5F),
                PartPose.offsetAndRotation(0F, -4F, 0F, -0.8378F, 0F, 0.0698F));
        PartDefinition p_horn_chamois_l = p_head.addOrReplaceChild("horn_chamois_l", CubeListBuilder.create()
                .texOffs(81, 46).addBox(-0.5F, -3.4F, -0.5F, 1F, 3.4F, 1F),
                PartPose.offsetAndRotation(1.17F, -8.4F, -3.8F, -0.1047F, 0F, 0.0698F));
        PartDefinition p_horn_chamois_tip_l = p_horn_chamois_l.addOrReplaceChild("horn_chamois_tip_l", CubeListBuilder.create()
                .texOffs(7, 53).addBox(-0.45F, -1.8F, -0.45F, 0.9F, 1.8F, 0.9F),
                PartPose.offsetAndRotation(0F, -3.3F, 0.1F, -2.0944F, 0F, 0F));
        PartDefinition p_horn_snow_l = p_head.addOrReplaceChild("horn_snow_l", CubeListBuilder.create()
                .texOffs(45, 46).addBox(-0.6F, -3F, -0.6F, 1.2F, 3F, 1.2F),
                PartPose.offsetAndRotation(1.3F, -8.4F, -3.6F, -0.3142F, 0F, 0.0873F));
        PartDefinition p_horn_snow_tip_l = p_horn_snow_l.addOrReplaceChild("horn_snow_tip_l", CubeListBuilder.create()
                .texOffs(12, 53).addBox(-0.45F, -2F, -0.45F, 0.9F, 2F, 0.9F),
                PartPose.offsetAndRotation(0F, -2.9F, 0F, -0.4538F, 0F, 0F));
        PartDefinition p_horn_domestic_l = p_head.addOrReplaceChild("horn_domestic_l", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-0.8F, -3.6F, -0.8F, 1.6F, 3.6F, 1.6F),
                PartPose.offsetAndRotation(1.3F, -8.4F, -3.6F, -0.5585F, 0F, 0.1745F));
        PartDefinition p_horn_domestic_tip_l = p_horn_domestic_l.addOrReplaceChild("horn_domestic_tip_l", CubeListBuilder.create()
                .texOffs(54, 46).addBox(-0.6F, -2.8F, -0.6F, 1.2F, 2.8F, 1.2F),
                PartPose.offsetAndRotation(0F, -3.5F, 0F, -0.733F, 0F, 0.1396F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(104, 46).addBox(-3.2F, -0.5F, -0.8F, 3.2F, 1F, 1.8F),
                PartPose.offsetAndRotation(-2.4F, -7.6F, -1.8F, 0F, 0.3491F, -0.4887F));
        PartDefinition p_horn_ibex_r = p_head.addOrReplaceChild("horn_ibex_r", CubeListBuilder.create()
                .texOffs(45, 36).addBox(-1F, -4.6F, -1.1F, 2F, 4.6F, 2.2F),
                PartPose.offsetAndRotation(-1.3F, -8.4F, -3.6F, -0.5236F, 0F, -0.1047F));
        PartDefinition p_horn_ibex_mid_r = p_horn_ibex_r.addOrReplaceChild("horn_ibex_mid_r", CubeListBuilder.create()
                .texOffs(105, 36).addBox(-0.85F, -4.2F, -0.95F, 1.7F, 4.2F, 1.9F),
                PartPose.offsetAndRotation(0F, -4.4F, 0F, -0.733F, 0F, -0.0698F));
        PartDefinition p_horn_ibex_tip_r = p_horn_ibex_mid_r.addOrReplaceChild("horn_ibex_tip_r", CubeListBuilder.create()
                .texOffs(9, 46).addBox(-0.65F, -3.6F, -0.75F, 1.3F, 3.6F, 1.5F),
                PartPose.offsetAndRotation(0F, -4F, 0F, -0.8378F, 0F, -0.0698F));
        PartDefinition p_horn_chamois_r = p_head.addOrReplaceChild("horn_chamois_r", CubeListBuilder.create()
                .texOffs(86, 46).addBox(-0.5F, -3.4F, -0.5F, 1F, 3.4F, 1F),
                PartPose.offsetAndRotation(-1.17F, -8.4F, -3.8F, -0.1047F, 0F, -0.0698F));
        PartDefinition p_horn_chamois_tip_r = p_horn_chamois_r.addOrReplaceChild("horn_chamois_tip_r", CubeListBuilder.create()
                .texOffs(17, 53).addBox(-0.45F, -1.8F, -0.45F, 0.9F, 1.8F, 0.9F),
                PartPose.offsetAndRotation(0F, -3.3F, 0.1F, -2.0944F, 0F, 0F));
        PartDefinition p_horn_snow_r = p_head.addOrReplaceChild("horn_snow_r", CubeListBuilder.create()
                .texOffs(63, 46).addBox(-0.6F, -3F, -0.6F, 1.2F, 3F, 1.2F),
                PartPose.offsetAndRotation(-1.3F, -8.4F, -3.6F, -0.3142F, 0F, -0.0873F));
        PartDefinition p_horn_snow_tip_r = p_horn_snow_r.addOrReplaceChild("horn_snow_tip_r", CubeListBuilder.create()
                .texOffs(22, 53).addBox(-0.45F, -2F, -0.45F, 0.9F, 2F, 0.9F),
                PartPose.offsetAndRotation(0F, -2.9F, 0F, -0.4538F, 0F, 0F));
        PartDefinition p_horn_domestic_r = p_head.addOrReplaceChild("horn_domestic_r", CubeListBuilder.create()
                .texOffs(18, 46).addBox(-0.8F, -3.6F, -0.8F, 1.6F, 3.6F, 1.6F),
                PartPose.offsetAndRotation(-1.3F, -8.4F, -3.6F, -0.5585F, 0F, -0.1745F));
        PartDefinition p_horn_domestic_tip_r = p_horn_domestic_r.addOrReplaceChild("horn_domestic_tip_r", CubeListBuilder.create()
                .texOffs(72, 46).addBox(-0.6F, -2.8F, -0.6F, 1.2F, 2.8F, 1.2F),
                PartPose.offsetAndRotation(0F, -3.5F, 0F, -0.733F, 0F, -0.1396F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(27, 46).addBox(-1F, -3F, -0.4F, 2F, 3.2F, 1.4F),
                PartPose.offsetAndRotation(0F, -3.2F, 7F, -0.6632F, 0F, 0F));
        PartDefinition p_front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(23, 24).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(2.5F, 16F, -5.5F));
        PartDefinition p_front_left_leg_pants = p_front_left_leg.addOrReplaceChild("front_left_leg_pants", CubeListBuilder.create()
                .texOffs(75, 24).addBox(-1.9F, -0.5F, -1.9F, 3.8F, 4.5F, 3.8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(36, 24).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(-2.5F, 16F, -5.5F));
        PartDefinition p_front_right_leg_pants = p_front_right_leg.addOrReplaceChild("front_right_leg_pants", CubeListBuilder.create()
                .texOffs(92, 24).addBox(-1.9F, -0.5F, -1.9F, 3.8F, 4.5F, 3.8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(49, 24).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(2.5F, 16F, 4.8F));
        PartDefinition p_hind_left_leg_pants = p_hind_left_leg.addOrReplaceChild("hind_left_leg_pants", CubeListBuilder.create()
                .texOffs(109, 24).addBox(-1.9F, -0.5F, -1.9F, 3.8F, 4.5F, 3.8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(62, 24).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(-2.5F, 16F, 4.8F));
        PartDefinition p_hind_right_leg_pants = p_hind_right_leg.addOrReplaceChild("hind_right_leg_pants", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-1.9F, -0.5F, -1.9F, 3.8F, 4.5F, 3.8F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
