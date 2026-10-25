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
    public static final int TEXTURE_HEIGHT = 384;

    private PyrrhonGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13F, -12F, -22F, 26F, 22F, 44F)
                .texOffs(141, 0).addBox(-10.5F, -15.5F, -19F, 21F, 4F, 34F)
                .texOffs(273, 140).addBox(-8F, -17.5F, -14F, 16F, 2.5F, 22F)
                .texOffs(299, 105).addBox(-11.5F, 8F, -21F, 23F, 5F, 24F)
                .texOffs(0, 192).addBox(-10.5F, 8F, 3F, 21F, 3.5F, 17F)
                .texOffs(207, 140).addBox(-15F, -8F, -20F, 3F, 13F, 13F)
                .texOffs(240, 140).addBox(12F, -8F, -20F, 3F, 13F, 13F)
                .texOffs(137, 140).addBox(-14.5F, -6F, 8F, 2.5F, 13F, 14F)
                .texOffs(172, 140).addBox(12F, -6F, 8F, 2.5F, 13F, 14F)
                .texOffs(101, 251).addBox(-1.7F, -24F, -19F, 3.4F, 7.5F, 5.1F)
                .texOffs(257, 234).addBox(-1.7F, -26F, -11F, 3.4F, 9.5F, 5.1F)
                .texOffs(313, 214).addBox(-1.7F, -27F, -3F, 3.4F, 10.5F, 5.1F)
                .texOffs(401, 234).addBox(-1.7F, -25.5F, 5F, 3.4F, 9F, 5.1F)
                .texOffs(330, 251).addBox(-1.7F, -23.5F, 13F, 3.4F, 7F, 5.1F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(0, 140).addBox(-7.5F, -7.5F, -11.5F, 15F, 15F, 12F)
                .texOffs(351, 251).addBox(-6.5F, 7F, -10.5F, 13F, 1.5F, 10F)
                .texOffs(271, 266).addBox(-1.5F, -13F, -7.7F, 3F, 6F, 4.2F),
                PartPose.offsetAndRotation(0F, -7F, -20F, -0.6981F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(401, 140).addBox(-6.5F, -6.5F, -10.5F, 13F, 13F, 11F)
                .texOffs(118, 266).addBox(-5.5F, 6F, -9.5F, 11F, 1.5F, 9F)
                .texOffs(288, 266).addBox(-1.43F, -11.5F, -7F, 2.86F, 5.5F, 4.004F),
                PartPose.offsetAndRotation(0F, 0F, -11F, -0.1745F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(0, 168).addBox(-5.75F, -5.75F, -10.5F, 11.5F, 11.5F, 11F)
                .texOffs(159, 266).addBox(-4.75F, 5.25F, -9.5F, 9.5F, 1.5F, 9F)
                .texOffs(159, 279).addBox(-1.265F, -10.25F, -7F, 2.53F, 5F, 3.542F),
                PartPose.offsetAndRotation(0F, 0F, -10F, 0.2094F, 0F, 0F));
        PartDefinition p_neck4 = p_neck3.addOrReplaceChild("neck4", CubeListBuilder.create()
                .texOffs(241, 192).addBox(-5F, -5F, -9.5F, 10F, 10F, 10F)
                .texOffs(0, 279).addBox(-4F, 4.5F, -8.5F, 8F, 1.5F, 8F)
                .texOffs(174, 279).addBox(-1.1F, -9F, -6.3F, 2.2F, 4.5F, 3.08F),
                PartPose.offsetAndRotation(0F, 0F, -10F, 0.3142F, 0F, 0F));
        PartDefinition p_head = p_neck4.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(394, 105).addBox(-7F, -6F, -15F, 14F, 12F, 15F)
                .texOffs(456, 266).addBox(-7.5F, -7.5F, -14F, 15F, 2.5F, 7F)
                .texOffs(427, 168).addBox(-5F, -3.5F, -29F, 10F, 8F, 14F)
                .texOffs(268, 251).addBox(-3F, -5F, -27.5F, 6F, 2F, 11F)
                .texOffs(291, 298).addBox(-4F, -4F, -29.3F, 2.5F, 1.5F, 1.5F)
                .texOffs(302, 298).addBox(1.5F, -4F, -29.3F, 2.5F, 1.5F, 1.5F)
                .texOffs(0, 251).addBox(-4.5F, 4.5F, -28.5F, 9F, 1.5F, 12F)
                .texOffs(278, 234).addBox(-4.5F, 4.4F, -28.4F, 9F, 0.2F, 13.5F)
                .texOffs(211, 234).addBox(6.2F, -2F, -14F, 1.6F, 7F, 9F)
                .texOffs(234, 234).addBox(-7.8F, -2F, -14F, 1.6F, 7F, 9F)
                .texOffs(13, 290).addBox(6.3F, -4.6F, -13.8F, 1F, 2.2F, 3.6F)
                .texOffs(24, 290).addBox(-7.3F, -4.6F, -13.8F, 1F, 2.2F, 3.6F)
                .texOffs(35, 290).addBox(-0.9F, -9F, -12F, 1.8F, 3.5F, 2.7F)
                .texOffs(46, 290).addBox(-0.9F, -9.5F, -7F, 1.8F, 4F, 2.7F)
                .texOffs(57, 290).addBox(-0.9F, -9F, -2F, 1.8F, 3.5F, 2.7F),
                PartPose.offsetAndRotation(0F, 0F, -8.5F, 0.5236F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(77, 192).addBox(-5F, 0F, -17F, 10F, 4F, 17F)
                .texOffs(186, 251).addBox(-4.2F, -1.3F, -16.5F, 8.4F, 1.3F, 11F)
                .texOffs(325, 234).addBox(-3.6F, -0.2F, -15.5F, 7.2F, 0.4F, 14F)
                .texOffs(116, 290).addBox(-1F, 3.5F, -10F, 2F, 2.5F, 3F),
                PartPose.offsetAndRotation(0F, 4.5F, -12F, 0.0524F, 0F, 0F));
        PartDefinition p_horn_l1 = p_head.addOrReplaceChild("horn_l1", CubeListBuilder.create()
                .texOffs(43, 251).addBox(-2.3F, -2.3F, -0.5F, 4.6F, 4.6F, 8.8F),
                PartPose.offsetAndRotation(5.6F, -6.2F, -7F, -0.2094F, 0.6632F, 0F));
        PartDefinition p_horn_l2 = p_horn_l1.addOrReplaceChild("horn_l2", CubeListBuilder.create()
                .texOffs(466, 251).addBox(-2F, -2F, -0.5F, 4F, 4F, 7.8F),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.9599F, 0.1396F, 0F));
        PartDefinition p_horn_l3 = p_horn_l2.addOrReplaceChild("horn_l3", CubeListBuilder.create()
                .texOffs(0, 266).addBox(-1.65F, -1.65F, -0.5F, 3.3F, 3.3F, 7.3F),
                PartPose.offsetAndRotation(0F, 0F, 7F, -1.0821F, 0.1047F, 0F));
        PartDefinition p_horn_l4 = p_horn_l3.addOrReplaceChild("horn_l4", CubeListBuilder.create()
                .texOffs(56, 279).addBox(-1.25F, -1.25F, -0.5F, 2.5F, 2.5F, 6.3F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.9599F, -0.1047F, 0F));
        PartDefinition p_horn_l_back = p_head.addOrReplaceChild("horn_l_back", CubeListBuilder.create()
                .texOffs(225, 266).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 8F),
                PartPose.offsetAndRotation(3F, -6.5F, -3F, -0.384F, 0.2094F, 0F));
        PartDefinition p_horn_r1 = p_head.addOrReplaceChild("horn_r1", CubeListBuilder.create()
                .texOffs(72, 251).addBox(-2.3F, -2.3F, -0.5F, 4.6F, 4.6F, 8.8F),
                PartPose.offsetAndRotation(-5.6F, -6.2F, -7F, -0.2094F, -0.6632F, 0F));
        PartDefinition p_horn_r2 = p_horn_r1.addOrReplaceChild("horn_r2", CubeListBuilder.create()
                .texOffs(25, 266).addBox(-2F, -2F, -0.5F, 4F, 4F, 7.8F),
                PartPose.offsetAndRotation(0F, 0F, 8F, -0.9599F, -0.1396F, 0F));
        PartDefinition p_horn_r3 = p_horn_r2.addOrReplaceChild("horn_r3", CubeListBuilder.create()
                .texOffs(50, 266).addBox(-1.65F, -1.65F, -0.5F, 3.3F, 3.3F, 7.3F),
                PartPose.offsetAndRotation(0F, 0F, 7F, -1.0821F, -0.1047F, 0F));
        PartDefinition p_horn_r4 = p_horn_r3.addOrReplaceChild("horn_r4", CubeListBuilder.create()
                .texOffs(77, 279).addBox(-1.25F, -1.25F, -0.5F, 2.5F, 2.5F, 6.3F),
                PartPose.offsetAndRotation(0F, 0F, 6.5F, -0.9599F, 0.1047F, 0F));
        PartDefinition p_horn_r_back = p_head.addOrReplaceChild("horn_r_back", CubeListBuilder.create()
                .texOffs(248, 266).addBox(-1.2F, -1.2F, 0F, 2.4F, 2.4F, 8F),
                PartPose.offsetAndRotation(-3F, -6.5F, -3F, -0.384F, -0.2094F, 0F));
        PartDefinition p_tail1 = p_body.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(453, 105).addBox(-7F, -6.5F, -0.5F, 14F, 13F, 14F)
                .texOffs(422, 234).addBox(-6F, 6F, 0.5F, 12F, 1.5F, 12F)
                .texOffs(305, 266).addBox(-1.5F, -11.5F, 3.9F, 3F, 5.5F, 4.2F),
                PartPose.offsetAndRotation(0F, -4F, 21F, -0.1745F, 0F, 0F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(350, 140).addBox(-6F, -5.5F, -0.5F, 12F, 11F, 13F)
                .texOffs(143, 251).addBox(-5F, 5F, 0.5F, 10F, 1.5F, 11F)
                .texOffs(189, 279).addBox(-1.32F, -10F, 3.6F, 2.64F, 5F, 3.696F),
                PartPose.offsetAndRotation(0F, 0F, 13F, -0.1396F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(450, 140).addBox(-5.25F, -4.75F, -0.5F, 10.5F, 9.5F, 13F)
                .texOffs(227, 251).addBox(-4.25F, 4.25F, 0.5F, 8.5F, 1.5F, 11F)
                .texOffs(204, 279).addBox(-1.155F, -8.75F, 3.6F, 2.31F, 4.5F, 3.234F),
                PartPose.offsetAndRotation(0F, 0F, 12F, -0.0698F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(198, 192).addBox(-4.5F, -4F, -0.5F, 9F, 8F, 12F)
                .texOffs(398, 251).addBox(-3.5F, 3.5F, 0.5F, 7F, 1.5F, 10F)
                .texOffs(68, 290).addBox(-0.99F, -7.5F, 3.3F, 1.98F, 4F, 2.772F),
                PartPose.offset(0F, 0F, 12F));
        PartDefinition p_tail5 = p_tail4.addOrReplaceChild("tail5", CubeListBuilder.create()
                .texOffs(139, 214).addBox(-3.75F, -3.5F, -0.5F, 7.5F, 7F, 12F)
                .texOffs(433, 251).addBox(-2.75F, 3F, 0.5F, 5.5F, 1.5F, 10F)
                .texOffs(79, 290).addBox(-0.825F, -6.5F, 3.3F, 1.65F, 3.5F, 2.31F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0524F, 0F, 0F));
        PartDefinition p_tail6 = p_tail5.addOrReplaceChild("tail6", CubeListBuilder.create()
                .texOffs(278, 214).addBox(-3F, -3F, -0.5F, 6F, 6F, 11F)
                .texOffs(198, 266).addBox(-2F, 2.5F, 0.5F, 4F, 1.5F, 9F)
                .texOffs(127, 290).addBox(-0.75F, -5.5F, 3F, 1.5F, 3F, 2.1F),
                PartPose.offsetAndRotation(0F, 0F, 11F, 0.0698F, 0F, 0F));
        PartDefinition p_tail7 = p_tail6.addOrReplaceChild("tail7", CubeListBuilder.create()
                .texOffs(370, 234).addBox(-2.5F, -2.5F, -0.5F, 5F, 5F, 10F)
                .texOffs(33, 279).addBox(-1.5F, 2F, 0.5F, 3F, 1.5F, 8F),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_tail8 = p_tail7.addOrReplaceChild("tail8", CubeListBuilder.create()
                .texOffs(303, 251).addBox(-2F, -2F, -0.5F, 4F, 4F, 9F)
                .texOffs(140, 279).addBox(-1F, 1.5F, 0.5F, 2F, 1.5F, 7F)
                .texOffs(75, 266).addBox(-5.5F, -0.5F, 5F, 11F, 1F, 10F)
                .texOffs(122, 251).addBox(-0.5F, -5F, 4F, 1F, 4.5F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0.0524F, 0F, 0F));
        PartDefinition p_front_leg_l = p_body.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(55, 140).addBox(-5F, -3.5F, -5F, 10F, 17F, 10F),
                PartPose.offsetAndRotation(11.5F, 4F, -14F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_l = p_front_leg_l.addOrReplaceChild("front_shin_l", CubeListBuilder.create()
                .texOffs(132, 192).addBox(-3.75F, -1F, -3.75F, 7.5F, 13F, 7.5F),
                PartPose.offsetAndRotation(0F, 13F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_l = p_front_shin_l.addOrReplaceChild("front_foot_l", CubeListBuilder.create()
                .texOffs(121, 234).addBox(-4.75F, 0F, -8.4F, 9.5F, 3.5F, 12F)
                .texOffs(365, 279).addBox(-3.9167F, 1.225F, -11.9F, 1.5F, 2.275F, 3.5F)
                .texOffs(378, 279).addBox(-0.75F, 1.225F, -11.9F, 1.5F, 2.275F, 3.5F)
                .texOffs(391, 279).addBox(2.4167F, 1.225F, -11.9F, 1.5F, 2.275F, 3.5F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_l = p_body.addOrReplaceChild("hind_leg_l", CubeListBuilder.create()
                .texOffs(413, 67).addBox(-6F, -4.2F, -7F, 12F, 19.2F, 14F),
                PartPose.offsetAndRotation(12F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_l = p_hind_leg_l.addOrReplaceChild("hind_shin_l", CubeListBuilder.create()
                .texOffs(47, 168).addBox(-4.25F, -1F, -4.25F, 8.5F, 13.5F, 8.5F),
                PartPose.offsetAndRotation(0F, 14.5F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_l = p_hind_shin_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(180, 214).addBox(-5.25F, 0F, -9.1F, 10.5F, 3.5F, 13F)
                .texOffs(404, 279).addBox(-4.25F, 1.225F, -13.1F, 1.5F, 2.275F, 4F)
                .texOffs(417, 279).addBox(-0.75F, 1.225F, -13.1F, 1.5F, 2.275F, 4F)
                .texOffs(430, 279).addBox(2.75F, 1.225F, -13.1F, 1.5F, 2.275F, 4F),
                PartPose.offsetAndRotation(0F, 12F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_front_leg_r = p_body.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(96, 140).addBox(-5F, -3.5F, -5F, 10F, 17F, 10F),
                PartPose.offsetAndRotation(-11.5F, 4F, -14F, 0.1396F, 0F, 0F));
        PartDefinition p_front_shin_r = p_front_leg_r.addOrReplaceChild("front_shin_r", CubeListBuilder.create()
                .texOffs(165, 192).addBox(-3.75F, -1F, -3.75F, 7.5F, 13F, 7.5F),
                PartPose.offsetAndRotation(0F, 13F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_front_foot_r = p_front_shin_r.addOrReplaceChild("front_foot_r", CubeListBuilder.create()
                .texOffs(166, 234).addBox(-4.75F, 0F, -8.4F, 9.5F, 3.5F, 12F)
                .texOffs(443, 279).addBox(-3.9167F, 1.225F, -11.9F, 1.5F, 2.275F, 3.5F)
                .texOffs(456, 279).addBox(-0.75F, 1.225F, -11.9F, 1.5F, 2.275F, 3.5F)
                .texOffs(469, 279).addBox(2.4167F, 1.225F, -11.9F, 1.5F, 2.275F, 3.5F),
                PartPose.offsetAndRotation(0F, 11.5F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_hind_leg_r = p_body.addOrReplaceChild("hind_leg_r", CubeListBuilder.create()
                .texOffs(0, 105).addBox(-6F, -4.2F, -7F, 12F, 19.2F, 14F),
                PartPose.offsetAndRotation(-12F, 2F, 14F, -0.2094F, 0F, 0F));
        PartDefinition p_hind_shin_r = p_hind_leg_r.addOrReplaceChild("hind_shin_r", CubeListBuilder.create()
                .texOffs(84, 168).addBox(-4.25F, -1F, -4.25F, 8.5F, 13.5F, 8.5F),
                PartPose.offsetAndRotation(0F, 14.5F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_hind_foot_r = p_hind_shin_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(229, 214).addBox(-5.25F, 0F, -9.1F, 10.5F, 3.5F, 13F)
                .texOffs(482, 279).addBox(-4.25F, 1.225F, -13.1F, 1.5F, 2.275F, 4F)
                .texOffs(495, 279).addBox(-0.75F, 1.225F, -13.1F, 1.5F, 2.275F, 4F)
                .texOffs(0, 290).addBox(2.75F, 1.225F, -13.1F, 1.5F, 2.275F, 4F),
                PartPose.offsetAndRotation(0F, 12F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(322, 266).addBox(0F, -2.25F, -2.25F, 28F, 4.5F, 4.5F)
                .texOffs(53, 105).addBox(0F, 0F, 1.75F, 28F, 0F, 33F),
                PartPose.offset(11F, -11F, -12F));
        PartDefinition p_wing_l_fore = p_wing_l.addOrReplaceChild("wing_l_fore", CubeListBuilder.create()
                .texOffs(219, 279).addBox(0F, -1.6875F, -1.6875F, 32F, 3.375F, 3.375F)
                .texOffs(147, 67).addBox(0F, 0F, 1.1875F, 32F, 0F, 34F),
                PartPose.offset(28F, 0F, 0F));
        PartDefinition p_wing_l_hand = p_wing_l_fore.addOrReplaceChild("wing_l_hand", CubeListBuilder.create()
                .texOffs(98, 279).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(90, 290).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(32F, 0F, 0F));
        PartDefinition p_wing_l_f1 = p_wing_l_hand.addOrReplaceChild("wing_l_f1", CubeListBuilder.create()
                .texOffs(138, 290).addBox(0F, -0.925F, -0.925F, 54F, 1.85F, 1.85F)
                .texOffs(121, 168).addBox(0F, 0F, 0.525F, 54F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.1047F, 0F));
        PartDefinition p_wing_l_f2 = p_wing_l_hand.addOrReplaceChild("wing_l_f2", CubeListBuilder.create()
                .texOffs(364, 290).addBox(0F, -0.75F, -0.75F, 50F, 1.5F, 1.5F)
                .texOffs(282, 192).addBox(0F, 0F, 0.35F, 50F, 0F, 19F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.5236F, 0F));
        PartDefinition p_wing_l_f3 = p_wing_l_hand.addOrReplaceChild("wing_l_f3", CubeListBuilder.create()
                .texOffs(105, 298).addBox(0F, -0.575F, -0.575F, 44F, 1.15F, 1.15F)
                .texOffs(334, 214).addBox(0F, 0F, 0.175F, 44F, 0F, 16F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.9425F, 0F));
        PartDefinition p_wing_l_f4 = p_wing_l_hand.addOrReplaceChild("wing_l_f4", CubeListBuilder.create()
                .texOffs(313, 298).addBox(0F, -0.5F, -0.5F, 36F, 1F, 1F)
                .texOffs(252, 0).addBox(0F, 0F, 0.1F, 36F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -1.3614F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(389, 266).addBox(-28F, -2.25F, -2.25F, 28F, 4.5F, 4.5F)
                .texOffs(176, 105).addBox(-28F, 0F, 1.75F, 28F, 0F, 33F),
                PartPose.offset(-11F, -11F, -12F));
        PartDefinition p_wing_r_fore = p_wing_r.addOrReplaceChild("wing_r_fore", CubeListBuilder.create()
                .texOffs(292, 279).addBox(-32F, -1.6875F, -1.6875F, 32F, 3.375F, 3.375F)
                .texOffs(280, 67).addBox(-32F, 0F, 1.1875F, 32F, 0F, 34F),
                PartPose.offset(-28F, 0F, 0F));
        PartDefinition p_wing_r_hand = p_wing_r_fore.addOrReplaceChild("wing_r_hand", CubeListBuilder.create()
                .texOffs(119, 279).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F)
                .texOffs(103, 290).addBox(-1F, -1F, -6F, 2F, 2F, 4F),
                PartPose.offset(-32F, 0F, 0F));
        PartDefinition p_wing_r_f1 = p_wing_r_hand.addOrReplaceChild("wing_r_f1", CubeListBuilder.create()
                .texOffs(251, 290).addBox(-54F, -0.925F, -0.925F, 54F, 1.85F, 1.85F)
                .texOffs(274, 168).addBox(-54F, 0F, 0.525F, 54F, 0F, 22F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.1047F, 0F));
        PartDefinition p_wing_r_f2 = p_wing_r_hand.addOrReplaceChild("wing_r_f2", CubeListBuilder.create()
                .texOffs(0, 298).addBox(-50F, -0.75F, -0.75F, 50F, 1.5F, 1.5F)
                .texOffs(0, 214).addBox(-50F, 0F, 0.35F, 50F, 0F, 19F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.5236F, 0F));
        PartDefinition p_wing_r_f3 = p_wing_r_hand.addOrReplaceChild("wing_r_f3", CubeListBuilder.create()
                .texOffs(198, 298).addBox(-44F, -0.575F, -0.575F, 44F, 1.15F, 1.15F)
                .texOffs(0, 234).addBox(-44F, 0F, 0.175F, 44F, 0F, 16F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.9425F, 0F));
        PartDefinition p_wing_r_f4 = p_wing_r_hand.addOrReplaceChild("wing_r_f4", CubeListBuilder.create()
                .texOffs(388, 298).addBox(-36F, -0.5F, -0.5F, 36F, 1F, 1F)
                .texOffs(0, 67).addBox(-36F, 0F, 0.1F, 36F, 0F, 37F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.3614F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
