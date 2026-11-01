package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Hausrinds (Hörner, Schopf und Pilze je Rasse).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CattleGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private CattleGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -5F, -10F, 12F, 10F, 20F)
                .texOffs(81, 31).addBox(-4.5F, -6.5F, -9.5F, 9F, 2F, 7F)
                .texOffs(51, 31).addBox(-4F, -4.5F, -13F, 8F, 7.5F, 3.5F)
                .texOffs(23, 45).addBox(-1.5F, 3F, -13.5F, 3F, 4F, 5F)
                .texOffs(40, 45).addBox(-2.5F, 5F, 2.5F, 5F, 1.5F, 5F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(65, 0).addBox(-4F, -4.5F, -6.5F, 8F, 7.5F, 6.5F)
                .texOffs(0, 45).addBox(-3.25F, 0F, -10.5F, 6.5F, 5F, 4F)
                .texOffs(119, 61).addBox(4F, -1F, -5.5F, 0.5F, 1F, 1F)
                .texOffs(124, 61).addBox(-4.5F, -1F, -5.5F, 0.5F, 1F, 1F),
                PartPose.offsetAndRotation(0F, -1F, -12.5F, 0.2443F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(13, 61).addBox(0F, -0.75F, -1F, 3F, 1.5F, 2F),
                PartPose.offsetAndRotation(4F, -3F, -1.8F, -0.1745F, 0F, 0.2094F));
        PartDefinition p_horn_short_l = p_head.addOrReplaceChild("horn_short_l", CubeListBuilder.create()
                .texOffs(24, 61).addBox(0F, -0.75F, -0.75F, 2.5F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(3.6F, -4F, -3.5F, 0F, 0F, -0.3491F));
        PartDefinition p_horn_short_tip_l = p_horn_short_l.addOrReplaceChild("horn_short_tip_l", CubeListBuilder.create()
                .texOffs(105, 61).addBox(0F, -0.5F, -0.5F, 1.5F, 1F, 1F),
                PartPose.offsetAndRotation(2.2F, 0F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_horn_long_l = p_head.addOrReplaceChild("horn_long_l", CubeListBuilder.create()
                .texOffs(28, 55).addBox(0F, -0.9F, -0.9F, 4.5F, 1.8F, 1.8F),
                PartPose.offsetAndRotation(3.6F, -3.8F, -3.5F, 0F, 0F, 0.1396F));
        PartDefinition p_horn_long_mid_l = p_horn_long_l.addOrReplaceChild("horn_long_mid_l", CubeListBuilder.create()
                .texOffs(58, 55).addBox(0F, -0.7F, -0.7F, 3.5F, 1.4F, 1.4F),
                PartPose.offsetAndRotation(4.2F, 0F, 0F, -0.3491F, 0.4363F, -0.6981F));
        PartDefinition p_horn_long_tip_l = p_horn_long_mid_l.addOrReplaceChild("horn_long_tip_l", CubeListBuilder.create()
                .texOffs(87, 61).addBox(0F, -0.5F, -0.5F, 2.5F, 1F, 1F),
                PartPose.offsetAndRotation(3.3F, 0F, 0F, 0F, 0.2618F, -0.6109F));
        PartDefinition p_horn_lyre_l = p_head.addOrReplaceChild("horn_lyre_l", CubeListBuilder.create()
                .texOffs(61, 45).addBox(-0.8F, -4F, -0.8F, 1.6F, 4F, 1.6F),
                PartPose.offsetAndRotation(3.4F, -4F, -3.5F, -0.1745F, 0F, 0.6109F));
        PartDefinition p_horn_lyre_mid_l = p_horn_lyre_l.addOrReplaceChild("horn_lyre_mid_l", CubeListBuilder.create()
                .texOffs(70, 45).addBox(-0.65F, -3.5F, -0.65F, 1.3F, 3.5F, 1.3F),
                PartPose.offsetAndRotation(0F, -3.8F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_horn_lyre_tip_l = p_horn_lyre_mid_l.addOrReplaceChild("horn_lyre_tip_l", CubeListBuilder.create()
                .texOffs(57, 61).addBox(-0.5F, -2.5F, -0.5F, 1F, 2.5F, 1F),
                PartPose.offsetAndRotation(0F, -3.3F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(35, 61).addBox(-3F, -0.75F, -1F, 3F, 1.5F, 2F),
                PartPose.offsetAndRotation(-4F, -3F, -1.8F, -0.1745F, 0F, -0.2094F));
        PartDefinition p_horn_short_r = p_head.addOrReplaceChild("horn_short_r", CubeListBuilder.create()
                .texOffs(46, 61).addBox(-2.5F, -0.75F, -0.75F, 2.5F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-3.6F, -4F, -3.5F, 0F, 0F, 0.3491F));
        PartDefinition p_horn_short_tip_r = p_horn_short_r.addOrReplaceChild("horn_short_tip_r", CubeListBuilder.create()
                .texOffs(112, 61).addBox(-1.5F, -0.5F, -0.5F, 1.5F, 1F, 1F),
                PartPose.offsetAndRotation(-2.2F, 0F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_horn_long_r = p_head.addOrReplaceChild("horn_long_r", CubeListBuilder.create()
                .texOffs(43, 55).addBox(-4.5F, -0.9F, -0.9F, 4.5F, 1.8F, 1.8F),
                PartPose.offsetAndRotation(-3.6F, -3.8F, -3.5F, 0F, 0F, -0.1396F));
        PartDefinition p_horn_long_mid_r = p_horn_long_r.addOrReplaceChild("horn_long_mid_r", CubeListBuilder.create()
                .texOffs(71, 55).addBox(-3.5F, -0.7F, -0.7F, 3.5F, 1.4F, 1.4F),
                PartPose.offsetAndRotation(-4.2F, 0F, 0F, -0.3491F, -0.4363F, 0.6981F));
        PartDefinition p_horn_long_tip_r = p_horn_long_mid_r.addOrReplaceChild("horn_long_tip_r", CubeListBuilder.create()
                .texOffs(96, 61).addBox(-2.5F, -0.5F, -0.5F, 2.5F, 1F, 1F),
                PartPose.offsetAndRotation(-3.3F, 0F, 0F, 0F, -0.2618F, 0.6109F));
        PartDefinition p_horn_lyre_r = p_head.addOrReplaceChild("horn_lyre_r", CubeListBuilder.create()
                .texOffs(79, 45).addBox(-0.8F, -4F, -0.8F, 1.6F, 4F, 1.6F),
                PartPose.offsetAndRotation(-3.4F, -4F, -3.5F, -0.1745F, 0F, -0.6109F));
        PartDefinition p_horn_lyre_mid_r = p_horn_lyre_r.addOrReplaceChild("horn_lyre_mid_r", CubeListBuilder.create()
                .texOffs(88, 45).addBox(-0.65F, -3.5F, -0.65F, 1.3F, 3.5F, 1.3F),
                PartPose.offsetAndRotation(0F, -3.8F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_horn_lyre_tip_r = p_horn_lyre_mid_r.addOrReplaceChild("horn_lyre_tip_r", CubeListBuilder.create()
                .texOffs(62, 61).addBox(-0.5F, -2.5F, -0.5F, 1F, 2.5F, 1F),
                PartPose.offsetAndRotation(0F, -3.3F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_fringe = p_head.addOrReplaceChild("fringe", CubeListBuilder.create()
                .texOffs(97, 45).addBox(-4.25F, 0F, -0.6F, 8.5F, 3.5F, 1F)
                .texOffs(0, 55).addBox(-3F, -0.8F, -0.2F, 6F, 1.5F, 3F),
                PartPose.offset(0F, -4.5F, -6.5F));
        PartDefinition p_mushrooms = p_body.addOrReplaceChild("mushrooms", CubeListBuilder.create()
                .texOffs(67, 61).addBox(-2.9F, -1.6F, -4.9F, 0.8F, 1.6F, 0.8F)
                .texOffs(84, 55).addBox(-3.7F, -2.6F, -5.7F, 2.4F, 1F, 2.4F)
                .texOffs(72, 61).addBox(1.6F, -1.6F, 0.6F, 0.8F, 1.6F, 0.8F)
                .texOffs(97, 55).addBox(0.8F, -2.6F, -0.2F, 2.4F, 1F, 2.4F)
                .texOffs(77, 61).addBox(-1.4F, -1.6F, 5.6F, 0.8F, 1.6F, 0.8F)
                .texOffs(110, 55).addBox(-2.2F, -2.6F, 4.8F, 2.4F, 1F, 2.4F)
                .texOffs(82, 61).addBox(2.6F, -1.6F, -7.4F, 0.8F, 1.6F, 0.8F)
                .texOffs(0, 61).addBox(1.8F, -2.6F, -8.2F, 2.4F, 1F, 2.4F),
                PartPose.offset(0F, -5F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(76, 31).addBox(-0.5F, 0F, -0.5F, 1F, 9F, 1F)
                .texOffs(19, 55).addBox(-1F, 8F, -1F, 2F, 3F, 2F),
                PartPose.offsetAndRotation(0F, -4F, 10F, 0.2094F, 0F, 0F));
        PartDefinition p_front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-2F, 0F, -2F, 4F, 9F, 4F),
                PartPose.offset(3.5F, 15F, -7F));
        PartDefinition p_front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-2F, 0F, -2F, 4F, 9F, 4F),
                PartPose.offset(-3.5F, 15F, -7F));
        PartDefinition p_hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(17, 31).addBox(-2F, 0F, -2F, 4F, 9F, 4F),
                PartPose.offset(3.5F, 15F, 7F));
        PartDefinition p_hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(34, 31).addBox(-2F, 0F, -2F, 4F, 9F, 4F),
                PartPose.offset(-3.5F, 15F, 7F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
