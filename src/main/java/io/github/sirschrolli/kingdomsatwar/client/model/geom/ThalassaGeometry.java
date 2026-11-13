package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Thalassa, dem Wasserdrachen-Boss (Grundgröße, im Spiel ×2,0).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ThalassaGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    private ThalassaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-8F, -3.5F, -14F, 16F, 7F, 27F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 0).addBox(-4.5F, -7F, -14F, 9F, 14F, 27F, new CubeDeformation(-0.1F, 0F, -0.08F))
                .texOffs(367, 0).addBox(-6F, -9F, -12F, 12F, 3F, 22F)
                .texOffs(193, 0).addBox(-6.5F, 5.6F, -13F, 13F, 3F, 25F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(270, 0).addBox(-0.5F, -15.25F, -11F, 1F, 7F, 20F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(177, 85).addBox(-3F, -1.25F, -2.5F, 6F, 10F, 5F, new CubeDeformation(-0.4F, -0.25F, -0.3F))
                .texOffs(449, 42).addBox(-1.5F, -1.25F, -4F, 3F, 10F, 8F, new CubeDeformation(-0.3F, -0.33F, -0.4F)),
                PartPose.offsetAndRotation(7.5F, 3.5F, -7.5F, 0.4363F, 0F, -1.0123F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(371, 102).addBox(-2.5F, -0.75F, -2F, 5F, 9F, 4F, new CubeDeformation(-0.4F, -0.25F, -0.1F))
                .texOffs(223, 85).addBox(-1F, -0.75F, -3F, 2F, 9F, 6F, new CubeDeformation(0F, -0.33F, 0F)),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.4887F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(313, 0).addBox(-1F, -0.5F, -5.5F, 2F, 15F, 11F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(200, 85).addBox(-3F, -1.25F, -2.5F, 6F, 10F, 5F, new CubeDeformation(-0.4F, -0.25F, -0.3F))
                .texOffs(472, 42).addBox(-1.5F, -1.25F, -4F, 3F, 10F, 8F, new CubeDeformation(-0.3F, -0.33F, -0.4F)),
                PartPose.offsetAndRotation(-7.5F, 3.5F, -7.5F, 0.4363F, 0F, 1.0123F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(390, 102).addBox(-2.5F, -0.75F, -2F, 5F, 9F, 4F, new CubeDeformation(-0.4F, -0.25F, -0.1F))
                .texOffs(240, 85).addBox(-1F, -0.75F, -3F, 2F, 9F, 6F, new CubeDeformation(0F, -0.33F, 0F)),
                PartPose.offsetAndRotation(0F, 8.5F, 0F, 0.4887F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(340, 0).addBox(-1F, -0.5F, -5.5F, 2F, 15F, 11F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 7.8F, 0F, 0.384F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(349, 85).addBox(-6F, -2.5F, -8.25F, 12F, 5F, 9F, new CubeDeformation(0F, -0.34F, -0.15F))
                .texOffs(179, 42).addBox(-2.5F, -6F, -8.25F, 5F, 12F, 9F, new CubeDeformation(-0.34F, 0F, -0.23F))
                .texOffs(277, 130).addBox(-2.5F, 4.8F, -7.75F, 5F, 2F, 8F, new CubeDeformation(-0.34F, -0.2F, -0.25F))
                .texOffs(342, 117).addBox(-0.5F, -9.85F, -7.25F, 1F, 5F, 7F, new CubeDeformation(-0.15F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -2F, -13.5F, -0.4887F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(146, 102).addBox(-5.5F, -2F, -8.1F, 11F, 4F, 9F, new CubeDeformation(-0.2F, -0.092F, -0.3F))
                .texOffs(239, 42).addBox(-2F, -5.5F, -8.1F, 4F, 11F, 9F, new CubeDeformation(-0.092F, -0.2F, -0.38F))
                .texOffs(358, 130).addBox(-2F, 4.1F, -7.6F, 4F, 2F, 8F, new CubeDeformation(-0.092F, -0.2F, -0.4F))
                .texOffs(359, 117).addBox(-0.5F, -8.95F, -7.1F, 1F, 5F, 7F, new CubeDeformation(-0.15F, -0.45F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -7.5F, -0.2793F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(228, 102).addBox(-5F, -2F, -8F, 10F, 4F, 9F, new CubeDeformation(-0.2F, -0.272F, -0.4F))
                .texOffs(342, 42).addBox(-2F, -5F, -8F, 4F, 10F, 9F, new CubeDeformation(-0.272F, -0.2F, -0.48F))
                .texOffs(105, 142).addBox(-2F, 3.6F, -7F, 4F, 2F, 7F, new CubeDeformation(-0.272F, -0.2F, 0F))
                .texOffs(48, 142).addBox(-0.5F, -7.75F, -6.5F, 1F, 4F, 6F, new CubeDeformation(-0.15F, -0.15F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -7.2F, -0.1047F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(0, 117).addBox(-4.5F, -2F, -7.3F, 9F, 4F, 8F, new CubeDeformation(-0.1F, -0.416F, -0.1F))
                .texOffs(130, 67).addBox(-2F, -4.5F, -7.3F, 4F, 9F, 8F, new CubeDeformation(-0.416F, -0.1F, -0.18F))
                .texOffs(128, 142).addBox(-2F, 3.2F, -6.8F, 4F, 2F, 7F, new CubeDeformation(-0.416F, -0.2F, -0.2F))
                .texOffs(63, 142).addBox(-0.5F, -7.15F, -6.3F, 1F, 4F, 6F, new CubeDeformation(-0.15F, -0.35F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, -7F, 0.1047F, 0F, 0F));
        PartDefinition p_neck5 = p_neck4.addOrReplaceChild("neck5", CubeListBuilder.create()
                .texOffs(376, 117).addBox(-4.5F, -1.5F, -7.1F, 9F, 3F, 8F, new CubeDeformation(-0.4F, -0.024F, -0.3F))
                .texOffs(155, 67).addBox(-1.5F, -4.5F, -7.1F, 3F, 9F, 8F, new CubeDeformation(-0.024F, -0.4F, -0.38F))
                .texOffs(151, 142).addBox(-1.5F, 2.9F, -6.6F, 3F, 2F, 7F, new CubeDeformation(-0.024F, -0.2F, -0.4F))
                .texOffs(248, 142).addBox(-0.5F, -6.15F, -6.1F, 1F, 3F, 6F, new CubeDeformation(-0.15F, -0.05F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -6.6F, 0.2094F, 0F, 0F));
        PartDefinition p_head = p_neck5.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(369, 42).addBox(-6F, -2.75F, -12.25F, 12F, 5F, 13F, new CubeDeformation(0F, -0.15F, -0.25F))
                .texOffs(37, 42).addBox(-4F, -5.25F, -12.25F, 8F, 10F, 13F, new CubeDeformation(-0.4F, -0.25F, -0.33F))
                .texOffs(371, 142).addBox(-5F, -3.8F, -15F, 10F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(273, 142).addBox(-4F, -4.8F, -15F, 8F, 3F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.08F))
                .texOffs(409, 102).addBox(-5F, -1.9F, -19F, 10F, 4F, 8F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(46, 85).addBox(-3F, -3.4F, -19F, 6F, 7F, 8F, new CubeDeformation(-0.2F, 0F, -0.08F))
                .texOffs(477, 117).addBox(-4F, -1.7F, -25.5F, 8F, 4F, 7F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(37, 102).addBox(-2.5F, -3.2F, -25.5F, 5F, 7F, 7F, new CubeDeformation(-0.1F, -0.2F, -0.08F))
                .texOffs(0, 102).addBox(-3F, -4.6F, -24F, 6F, 2F, 12F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(85, 153).addBox(-3.5F, -2.7F, -26.3F, 7F, 4F, 1F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(242, 153).addBox(0.9F, -2.5F, -26.75F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.25F))
                .texOffs(300, 142).addBox(4.175F, -3.6F, -12.8F, 2F, 3F, 5F, new CubeDeformation(-0.025F, -0.4F, -0.4F))
                .texOffs(102, 153).addBox(3.1F, 3.41F, -24.6786F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.19F, -0.1F))
                .texOffs(107, 153).addBox(3.1F, 3.2F, -23.0357F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(112, 153).addBox(3.1F, 3.2F, -21.3929F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(117, 153).addBox(3.1F, 3.2F, -19.75F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(122, 153).addBox(3.1F, 3.2F, -18.1071F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(127, 153).addBox(3.1F, 3.2F, -16.4643F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(132, 153).addBox(3.1F, 3.41F, -14.8214F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.19F, -0.1F))
                .texOffs(57, 153).addBox(2.7F, 3.2F, -23.95F, 1F, 4F, 2F, new CubeDeformation(0F, -0.4F, -0.45F))
                .texOffs(249, 153).addBox(-2.9F, -2.5F, -26.75F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.25F))
                .texOffs(315, 142).addBox(-6.175F, -3.6F, -12.8F, 2F, 3F, 5F, new CubeDeformation(-0.025F, -0.4F, -0.4F))
                .texOffs(137, 153).addBox(-4.1F, 3.41F, -24.6786F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.19F, -0.1F))
                .texOffs(142, 153).addBox(-4.1F, 3.2F, -23.0357F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(147, 153).addBox(-4.1F, 3.2F, -21.3929F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(152, 153).addBox(-4.1F, 3.2F, -19.75F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(157, 153).addBox(-4.1F, 3.2F, -18.1071F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(162, 153).addBox(-4.1F, 3.2F, -16.4643F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(167, 153).addBox(-4.1F, 3.41F, -14.8214F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.19F, -0.1F))
                .texOffs(64, 153).addBox(-3.7F, 3.2F, -23.95F, 1F, 4F, 2F, new CubeDeformation(0F, -0.4F, -0.45F))
                .texOffs(267, 102).addBox(-3.5F, 3.27F, -24.8F, 7F, 1F, 12F, new CubeDeformation(-0.1F, -0.35F, 0F))
                .texOffs(62, 102).addBox(-0.5F, -9.6F, -11F, 1F, 5F, 9F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -6F, 0.5585F, 0F, 0F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(383, 130).addBox(-2F, -1.05F, -0.85F, 4F, 2F, 8F, new CubeDeformation(0F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(4F, -4.4F, -12.4F, 0.1047F, 0F, -0.1396F));
        PartDefinition p_barbel_l1 = p_head.addOrReplaceChild("barbel_l1", CubeListBuilder.create()
                .texOffs(263, 142).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(3.8F, 1.5F, -23F, 0.3491F, 0F, 0.4363F));
        PartDefinition p_barbel_l2 = p_barbel_l1.addOrReplaceChild("barbel_l2", CubeListBuilder.create()
                .texOffs(330, 142).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.2618F, 0F, -0.1745F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(408, 130).addBox(-2F, -1.05F, -0.85F, 4F, 2F, 8F, new CubeDeformation(0F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(-4F, -4.4F, -12.4F, 0.1047F, 0F, 0.1396F));
        PartDefinition p_barbel_r1 = p_head.addOrReplaceChild("barbel_r1", CubeListBuilder.create()
                .texOffs(268, 142).addBox(-0.5F, 0F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(-3.8F, 1.5F, -23F, 0.3491F, 0F, -0.4363F));
        PartDefinition p_barbel_r2 = p_barbel_r1.addOrReplaceChild("barbel_r2", CubeListBuilder.create()
                .texOffs(335, 142).addBox(-0.5F, 0F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.15F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, 7.6F, 0F, 0.2618F, 0F, 0.1745F));
        PartDefinition p_frill_c0 = p_head.addOrReplaceChild("frill_c0", CubeListBuilder.create()
                .texOffs(458, 67).addBox(-5.5F, -15F, -0.5F, 11F, 15F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 0F));
        PartDefinition p_frill_l1 = p_head.addOrReplaceChild("frill_l1", CubeListBuilder.create()
                .texOffs(49, 67).addBox(-6F, -16F, -0.5F, 12F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 0.6981F));
        PartDefinition p_frill_r1 = p_head.addOrReplaceChild("frill_r1", CubeListBuilder.create()
                .texOffs(76, 67).addBox(-6F, -16F, -0.5F, 12F, 16F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -0.6981F));
        PartDefinition p_frill_l2 = p_head.addOrReplaceChild("frill_l2", CubeListBuilder.create()
                .texOffs(102, 85).addBox(-5.5F, -14F, -0.5F, 11F, 14F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 1.3963F));
        PartDefinition p_frill_r2 = p_head.addOrReplaceChild("frill_r2", CubeListBuilder.create()
                .texOffs(127, 85).addBox(-5.5F, -14F, -0.5F, 11F, 14F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -1.3963F));
        PartDefinition p_frill_l3 = p_head.addOrReplaceChild("frill_l3", CubeListBuilder.create()
                .texOffs(241, 117).addBox(-4.5F, -11F, -0.5F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, 2.0246F));
        PartDefinition p_frill_r3 = p_head.addOrReplaceChild("frill_r3", CubeListBuilder.create()
                .texOffs(262, 117).addBox(-4.5F, -11F, -0.5F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -0.5F, -2.5F, -0.4189F, 0F, -2.0246F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 67).addBox(-4F, 1F, -15.3F, 8F, 1F, 16F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(266, 42).addBox(-3F, 0F, -15.3F, 6F, 3F, 16F, new CubeDeformation(-0.1F, 0F, -0.38F))
                .texOffs(340, 142).addBox(-5F, 0.5F, -3.25F, 10F, 2F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.25F))
                .texOffs(78, 142).addBox(-4F, -0.5F, -3.25F, 8F, 4F, 5F, new CubeDeformation(-0.4F, -0.1F, -0.33F))
                .texOffs(172, 153).addBox(2.8F, -1.6425F, -14.3071F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(177, 153).addBox(2.8F, -1.45F, -12.7214F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(182, 153).addBox(2.8F, -1.45F, -11.1357F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(187, 153).addBox(2.8F, -1.45F, -9.55F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(192, 153).addBox(2.8F, -1.45F, -7.9643F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(197, 153).addBox(2.8F, -1.45F, -6.3786F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(202, 153).addBox(2.8F, -1.6425F, -4.7929F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(71, 153).addBox(2.4F, -3.45F, -13.45F, 1F, 4F, 2F, new CubeDeformation(0F, -0.45F, -0.45F))
                .texOffs(207, 153).addBox(-3.8F, -1.6425F, -14.3071F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(212, 153).addBox(-3.8F, -1.45F, -12.7214F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(217, 153).addBox(-3.8F, -1.45F, -11.1357F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(222, 153).addBox(-3.8F, -1.45F, -9.55F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(227, 153).addBox(-3.8F, -1.45F, -7.9643F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(232, 153).addBox(-3.8F, -1.45F, -6.3786F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.45F, -0.125F))
                .texOffs(237, 153).addBox(-3.8F, -1.6425F, -4.7929F, 1F, 2F, 1F, new CubeDeformation(-0.125F, -0.2575F, -0.125F))
                .texOffs(78, 153).addBox(-3.4F, -3.45F, -13.45F, 1F, 4F, 2F, new CubeDeformation(0F, -0.45F, -0.45F))
                .texOffs(386, 67).addBox(-3.5F, -0.5F, -15F, 7F, 1F, 15F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(175, 117).addBox(-2.5F, -0.7F, -13.5F, 5F, 1F, 11F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(411, 117).addBox(-3F, 2.65F, -10F, 6F, 1F, 10F, new CubeDeformation(-0.2F, -0.15F, 0F))
                .texOffs(260, 130).addBox(-0.5F, 2.95F, -11F, 1F, 4F, 7F, new CubeDeformation(-0.1F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 3.4F, -10F, 0.0524F, 0F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(178, 67).addBox(-7.5F, -2.5F, -0.75F, 15F, 5F, 11F, new CubeDeformation(0F, -0.16F, -0.15F))
                .texOffs(436, 0).addBox(-3.5F, -6.5F, -0.75F, 7F, 13F, 11F, new CubeDeformation(-0.16F, 0F, -0.23F))
                .texOffs(35, 117).addBox(-3.5F, 5.3F, -0.25F, 7F, 2F, 10F, new CubeDeformation(-0.16F, -0.2F, -0.25F))
                .texOffs(483, 67).addBox(-0.5F, -11.45F, 0.25F, 1F, 6F, 10F, new CubeDeformation(-0.15F, -0.15F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 12.5F, -0.0349F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(231, 67).addBox(-7.5F, -2.5F, -0.85F, 15F, 5F, 11F, new CubeDeformation(-0.255F, -0.2392F, -0.25F))
                .texOffs(473, 0).addBox(-3.5F, -6.5F, -0.85F, 7F, 13F, 11F, new CubeDeformation(-0.2742F, -0.22F, -0.33F))
                .texOffs(70, 117).addBox(-3.5F, 5.08F, -0.35F, 7F, 2F, 10F, new CubeDeformation(-0.2742F, -0.2F, -0.35F))
                .texOffs(0, 85).addBox(-0.5F, -11.08F, 0.15F, 1F, 6F, 10F, new CubeDeformation(-0.15F, -0.3F, -0.35F)),
                PartPose.offset(0F, 0F, 9.5F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(284, 67).addBox(-7F, -2.5F, -0.95F, 14F, 5F, 11F, new CubeDeformation(-0.065F, -0.3382F, -0.35F))
                .texOffs(0, 42).addBox(-3.5F, -6.5F, -0.95F, 7F, 13F, 11F, new CubeDeformation(-0.4082F, -0.495F, -0.43F))
                .texOffs(105, 117).addBox(-3.5F, 4.805F, -0.45F, 7F, 2F, 10F, new CubeDeformation(-0.4082F, -0.2F, -0.45F))
                .texOffs(23, 85).addBox(-0.5F, -10.655F, 0.05F, 1F, 6F, 10F, new CubeDeformation(-0.15F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 9.3F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(335, 67).addBox(-7F, -2.5F, -1.05F, 14F, 5F, 11F, new CubeDeformation(-0.405F, -0.4426F, -0.45F))
                .texOffs(80, 42).addBox(-3F, -6F, -0.55F, 6F, 12F, 10F, new CubeDeformation(-0.0626F, -0.285F, -0.03F))
                .texOffs(0, 130).addBox(-3F, 4.515F, -0.05F, 6F, 2F, 9F, new CubeDeformation(-0.0626F, -0.2F, -0.05F))
                .texOffs(83, 102).addBox(-0.5F, -9.715F, 0.45F, 1F, 5F, 9F, new CubeDeformation(-0.15F, -0.1F, -0.05F))
                .texOffs(402, 142).addBox(5.795F, 3.615F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(421, 142).addBox(-9.795F, 3.615F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 9.1F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(257, 85).addBox(-6.5F, -2F, -0.65F, 13F, 4F, 10F, new CubeDeformation(-0.26F, -0.0542F, -0.05F))
                .texOffs(113, 42).addBox(-3F, -5.5F, -0.65F, 6F, 11F, 10F, new CubeDeformation(-0.2192F, -0.095F, -0.13F))
                .texOffs(31, 130).addBox(-3F, 4.205F, -0.15F, 6F, 2F, 9F, new CubeDeformation(-0.2192F, -0.2F, -0.15F))
                .texOffs(104, 102).addBox(-0.5F, -9.255F, 0.35F, 1F, 5F, 9F, new CubeDeformation(-0.15F, -0.25F, -0.15F))
                .texOffs(440, 142).addBox(5.44F, 3.305F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(459, 142).addBox(-9.44F, 3.305F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 8.9F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(304, 85).addBox(-6F, -2F, -0.75F, 12F, 4F, 10F, new CubeDeformation(-0.125F, -0.1694F, -0.15F))
                .texOffs(146, 42).addBox(-3F, -5.5F, -0.75F, 6F, 11F, 10F, new CubeDeformation(-0.3794F, -0.415F, -0.23F))
                .texOffs(62, 130).addBox(-3F, 3.885F, -0.25F, 6F, 2F, 9F, new CubeDeformation(-0.3794F, -0.2F, -0.25F))
                .texOffs(125, 102).addBox(-0.5F, -8.785F, 0.25F, 1F, 5F, 9F, new CubeDeformation(-0.15F, -0.4F, -0.25F))
                .texOffs(478, 142).addBox(5.075F, 2.985F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 153).addBox(-9.075F, 2.985F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 8.7F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(392, 85).addBox(-5.5F, -2F, -0.85F, 11F, 4F, 10F, new CubeDeformation(-0.005F, -0.2882F, -0.25F))
                .texOffs(208, 42).addBox(-2.5F, -5F, -0.85F, 5F, 10F, 10F, new CubeDeformation(-0.0482F, -0.245F, -0.33F))
                .texOffs(124, 130).addBox(-2.5F, 3.555F, -0.35F, 5F, 2F, 9F, new CubeDeformation(-0.0482F, -0.2F, -0.35F))
                .texOffs(329, 102).addBox(-0.5F, -7.805F, 0.15F, 1F, 4F, 9F, new CubeDeformation(-0.15F, -0.05F, -0.35F))
                .texOffs(19, 153).addBox(4.695F, 2.655F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(38, 153).addBox(-8.695F, 2.655F, 2F, 4F, 1F, 5F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(435, 85).addBox(-5.5F, -2F, -0.95F, 11F, 4F, 10F, new CubeDeformation(-0.395F, -0.4088F, -0.35F))
                .texOffs(311, 42).addBox(-2.5F, -4.5F, -0.95F, 5F, 9F, 10F, new CubeDeformation(-0.2238F, -0.08F, -0.43F))
                .texOffs(153, 130).addBox(-2.5F, 3.22F, -0.45F, 5F, 2F, 9F, new CubeDeformation(-0.2238F, -0.2F, -0.45F))
                .texOffs(350, 102).addBox(-0.5F, -7.32F, 0.05F, 1F, 4F, 9F, new CubeDeformation(-0.15F, -0.2F, -0.45F)),
                PartPose.offset(0F, 0F, 8.3F));
        PartDefinition p_tail9 = p_tail8.addOrReplaceChild("tail9", CubeListBuilder.create()
                .texOffs(187, 102).addBox(-5F, -1.5F, -1.05F, 10F, 3F, 10F, new CubeDeformation(-0.29F, -0.033F, -0.45F))
                .texOffs(420, 42).addBox(-2.5F, -4.5F, -0.55F, 5F, 9F, 9F, new CubeDeformation(-0.398F, -0.425F, -0.03F))
                .texOffs(304, 130).addBox(-2.5F, 2.875F, -0.05F, 5F, 2F, 8F, new CubeDeformation(-0.398F, -0.2F, -0.05F))
                .texOffs(304, 117).addBox(-0.5F, -6.825F, 0.45F, 1F, 4F, 8F, new CubeDeformation(-0.15F, -0.35F, -0.05F)),
                PartPose.offset(0F, 0F, 8.1F));
        PartDefinition p_tail10 = p_tail9.addOrReplaceChild("tail10", CubeListBuilder.create()
                .texOffs(446, 102).addBox(-4.5F, -1.5F, -0.65F, 9F, 3F, 9F, new CubeDeformation(-0.195F, -0.1608F, -0.05F))
                .texOffs(103, 67).addBox(-2F, -4F, -0.65F, 4F, 8F, 9F, new CubeDeformation(-0.0758F, -0.28F, -0.13F))
                .texOffs(433, 130).addBox(-2F, 2.52F, -0.15F, 4F, 2F, 8F, new CubeDeformation(-0.0758F, -0.2F, -0.15F))
                .texOffs(323, 117).addBox(-0.5F, -6.32F, 0.35F, 1F, 4F, 8F, new CubeDeformation(-0.15F, -0.5F, -0.15F)),
                PartPose.offset(0F, 0F, 7.9F));
        PartDefinition p_tail11 = p_tail10.addOrReplaceChild("tail11", CubeListBuilder.create()
                .texOffs(140, 117).addBox(-4F, -1.5F, -0.75F, 8F, 3F, 9F, new CubeDeformation(-0.105F, -0.2886F, -0.15F))
                .texOffs(431, 67).addBox(-2F, -3.5F, -0.75F, 4F, 7F, 9F, new CubeDeformation(-0.2586F, -0.135F, -0.23F))
                .texOffs(458, 130).addBox(-2F, 2.165F, -0.25F, 4F, 2F, 8F, new CubeDeformation(-0.2586F, -0.2F, -0.25F))
                .texOffs(203, 130).addBox(-0.5F, -5.315F, 0.25F, 1F, 3F, 8F, new CubeDeformation(-0.15F, -0.15F, -0.25F)),
                PartPose.offset(0F, 0F, 7.7F));
        PartDefinition p_tail12 = p_tail11.addOrReplaceChild("tail12", CubeListBuilder.create()
                .texOffs(208, 117).addBox(-3.5F, -1.5F, -0.85F, 7F, 3F, 9F, new CubeDeformation(-0.025F, -0.42F, -0.25F))
                .texOffs(75, 85).addBox(-2F, -3F, -0.85F, 4F, 6F, 9F, new CubeDeformation(-0.445F, 0F, -0.33F))
                .texOffs(483, 130).addBox(-2F, 1.8F, -0.35F, 4F, 2F, 8F, new CubeDeformation(-0.445F, -0.2F, -0.35F))
                .texOffs(222, 130).addBox(-0.5F, -4.8F, 0.15F, 1F, 3F, 8F, new CubeDeformation(-0.15F, -0.3F, -0.35F)),
                PartPose.offset(0F, 0F, 7.5F));
        PartDefinition p_tail13 = p_tail12.addOrReplaceChild("tail13", CubeListBuilder.create()
                .texOffs(444, 117).addBox(-3.5F, -1F, -0.95F, 7F, 2F, 9F, new CubeDeformation(-0.45F, -0.0532F, -0.35F))
                .texOffs(152, 85).addBox(-1.5F, -3F, -0.95F, 3F, 6F, 9F, new CubeDeformation(-0.1332F, -0.37F, -0.43F))
                .texOffs(25, 142).addBox(-1.5F, 1.43F, -0.45F, 3F, 2F, 8F, new CubeDeformation(-0.1332F, -0.2F, -0.45F))
                .texOffs(241, 130).addBox(-0.5F, -4.28F, 0.05F, 1F, 3F, 8F, new CubeDeformation(-0.15F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 7.3F));
        PartDefinition p_tail14 = p_tail13.addOrReplaceChild("tail14", CubeListBuilder.create()
                .texOffs(93, 130).addBox(-3F, -1F, -1.05F, 6F, 2F, 9F, new CubeDeformation(-0.38F, -0.1864F, -0.45F))
                .texOffs(306, 102).addBox(-1.5F, -2.5F, -0.55F, 3F, 5F, 8F, new CubeDeformation(-0.3264F, -0.24F, -0.03F))
                .texOffs(172, 142).addBox(-1.5F, 1.06F, -0.05F, 3F, 2F, 7F, new CubeDeformation(-0.3264F, -0.2F, -0.05F))
                .texOffs(231, 142).addBox(-0.5F, -3.26F, 0.45F, 1F, 2F, 7F, new CubeDeformation(-0.15F, -0.1F, -0.05F)),
                PartPose.offset(0F, 0F, 7.1F));
        PartDefinition p_tail15 = p_tail14.addOrReplaceChild("tail15", CubeListBuilder.create()
                .texOffs(331, 130).addBox(-2.5F, -1F, -0.65F, 5F, 2F, 8F, new CubeDeformation(-0.31F, -0.3232F, -0.05F))
                .texOffs(283, 117).addBox(-1F, -2F, -0.65F, 2F, 4F, 8F, new CubeDeformation(-0.0132F, -0.12F, -0.13F))
                .texOffs(193, 142).addBox(-1F, 0.68F, -0.15F, 2F, 2F, 7F, new CubeDeformation(-0.0132F, -0.2F, -0.15F)),
                PartPose.offset(0F, 0F, 6.9F));
        PartDefinition p_tail16 = p_tail15.addOrReplaceChild("tail16", CubeListBuilder.create()
                .texOffs(0, 142).addBox(-2F, -1F, -0.75F, 4F, 2F, 8F, new CubeDeformation(-0.25F, -0.46F, -0.15F))
                .texOffs(182, 130).addBox(-1F, -1.5F, -0.75F, 2F, 3F, 8F, new CubeDeformation(-0.21F, 0F, -0.23F))
                .texOffs(212, 142).addBox(-1F, 0.3F, -0.25F, 2F, 2F, 7F, new CubeDeformation(-0.21F, -0.2F, -0.25F))
                .texOffs(73, 0).addBox(-1F, -11F, 1.5F, 2F, 22F, 14F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 0F, 6.7F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
