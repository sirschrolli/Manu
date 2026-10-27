package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der sechs Erzmagier (Zubehör je Element, siehe MageModel).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MageGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 128;

    private MageGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(69, 0).addBox(-4F, -12F, -2.5F, 8F, 12F, 5F)
                .texOffs(66, 61).addBox(-4.4F, -1.8F, -2.9F, 8.8F, 2.2F, 5.8F)
                .texOffs(161, 71).addBox(-1.1F, -2.1F, -3.3F, 2.2F, 2.6F, 0.6F)
                .texOffs(170, 35).addBox(-5.2F, -12.6F, -3.2F, 10.4F, 3.4F, 6.4F)
                .texOffs(184, 61).addBox(-4.6F, -16.2F, 1.6F, 9.2F, 4.2F, 1.2F)
                .texOffs(157, 35).addBox(-2.2F, -11.5F, -2.95F, 4.4F, 10.2F, 0.5F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_shard_l = p_body.addOrReplaceChild("shard_l", CubeListBuilder.create()
                .texOffs(131, 61).addBox(-1.1F, -4.8F, -1.1F, 2.2F, 4.8F, 2.2F)
                .texOffs(170, 71).addBox(-0.6F, -6.6F, -0.6F, 1.2F, 1.8F, 1.2F),
                PartPose.offsetAndRotation(5.3F, -12.5F, 0.2F, -0.1047F, 0F, -0.5585F));
        PartDefinition p_shard_l_small = p_shard_l.addOrReplaceChild("shard_l_small", CubeListBuilder.create()
                .texOffs(88, 71).addBox(-0.6F, -3F, -0.6F, 1.2F, 3F, 1.2F),
                PartPose.offsetAndRotation(1F, 0F, 0F, 0F, 0F, -0.384F));
        PartDefinition p_shard_r = p_body.addOrReplaceChild("shard_r", CubeListBuilder.create()
                .texOffs(144, 61).addBox(-1.1F, -4.8F, -1.1F, 2.2F, 4.8F, 2.2F)
                .texOffs(179, 71).addBox(-0.6F, -6.6F, -0.6F, 1.2F, 1.8F, 1.2F),
                PartPose.offsetAndRotation(-5.3F, -12.5F, 0.2F, -0.1047F, 0F, 0.5585F));
        PartDefinition p_shard_r_small = p_shard_r.addOrReplaceChild("shard_r_small", CubeListBuilder.create()
                .texOffs(97, 71).addBox(-0.6F, -3F, -0.6F, 1.2F, 3F, 1.2F),
                PartPose.offsetAndRotation(-1F, 0F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-5F, 0F, 0F, 10F, 24.5F, 0.8F),
                PartPose.offsetAndRotation(0F, -12.2F, 3F, 0.0873F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-4.5F, 0F, -3F, 9F, 7F, 6F)
                .texOffs(216, 0).addBox(-5.2F, 7F, -3.6F, 10.4F, 6F, 7.2F)
                .texOffs(70, 49).addBox(-5.6F, 13F, -4F, 11.2F, 1.6F, 8F)
                .texOffs(205, 0).addBox(-1.8F, 0F, -3.4F, 3.6F, 13.4F, 0.5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(176, 0).addBox(-3.5F, -8F, -3.5F, 7F, 8F, 7F)
                .texOffs(236, 71).addBox(-0.5F, -4.6F, -4.1F, 1F, 2.2F, 0.7F)
                .texOffs(110, 79).addBox(-2.7F, -5.4F, -3.6F, 1.7F, 0.9F, 0.1F)
                .texOffs(117, 79).addBox(1F, -5.4F, -3.6F, 1.7F, 0.9F, 0.1F)
                .texOffs(74, 79).addBox(-3.6F, -6.4F, -3.62F, 2.8F, 0.6F, 0.12F)
                .texOffs(83, 79).addBox(0.8F, -6.4F, -3.62F, 2.8F, 0.6F, 0.12F),
                PartPose.offset(0F, -12F, 0F));
        PartDefinition p_beard = p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(56, 71).addBox(-3.3F, 0F, -0.7F, 6.6F, 2.8F, 1.6F)
                .texOffs(133, 71).addBox(-3.6F, -2.2F, -0.3F, 1F, 2.6F, 1.2F)
                .texOffs(140, 71).addBox(2.6F, -2.2F, -0.3F, 1F, 2.6F, 1.2F)
                .texOffs(63, 79).addBox(-1.8F, -1.4F, -0.75F, 3.6F, 0.9F, 0.5F),
                PartPose.offset(0F, -2.4F, -3.5F));
        PartDefinition p_beard_long = p_beard.addOrReplaceChild("beard_long", CubeListBuilder.create()
                .texOffs(209, 61).addBox(-2.4F, 0F, -0.6F, 4.8F, 4.2F, 1.3F)
                .texOffs(20, 71).addBox(-1.5F, 4F, -0.5F, 3F, 3.6F, 1.1F)
                .texOffs(215, 71).addBox(-0.7F, 7.4F, -0.4F, 1.4F, 2.6F, 0.9F),
                PartPose.offsetAndRotation(0F, 2.6F, 0.1F, -0.1047F, 0F, 0F));
        PartDefinition p_hair = p_head.addOrReplaceChild("hair", CubeListBuilder.create()
                .texOffs(37, 49).addBox(-3.8F, -8.4F, -3.8F, 7.6F, 2.2F, 7.6F)
                .texOffs(96, 0).addBox(-3.9F, -6.4F, 1.6F, 7.8F, 14F, 2.6F)
                .texOffs(31, 35).addBox(2.6F, -6.4F, -3.7F, 1.3F, 11F, 1.6F)
                .texOffs(40, 35).addBox(-3.9F, -6.4F, -3.7F, 1.3F, 11F, 1.6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hood = p_head.addOrReplaceChild("hood", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.2F, -8.9F, -4F, 8.4F, 9.4F, 8.2F)
                .texOffs(42, 79).addBox(-4.2F, -8.9F, -4.1F, 8.4F, 1F, 0.4F)
                .texOffs(5, 71).addBox(-1.6F, -9.8F, 2.2F, 3.2F, 2.2F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_flame_crown = p_head.addOrReplaceChild("flame_crown", CubeListBuilder.create()
                .texOffs(207, 35).addBox(-4.4F, -1.2F, -4.4F, 8.8F, 1.2F, 8.8F)
                .texOffs(222, 71).addBox(-4F, -3.8F, -4.6F, 1.2F, 2.6F, 0.8F)
                .texOffs(147, 71).addBox(-2.2F, -4.8F, -4.6F, 1.2F, 3.6F, 0.8F)
                .texOffs(49, 71).addBox(-0.6F, -5.8F, -4.6F, 1.2F, 4.6F, 0.8F)
                .texOffs(154, 71).addBox(1F, -4.8F, -4.6F, 1.2F, 3.6F, 0.8F)
                .texOffs(229, 71).addBox(2.8F, -3.8F, -4.6F, 1.2F, 2.6F, 0.8F),
                PartPose.offset(0F, -9F, 0F));
        PartDefinition p_crystal_crown = p_head.addOrReplaceChild("crystal_crown", CubeListBuilder.create()
                .texOffs(204, 49).addBox(-4F, -1F, -4F, 8F, 1F, 8F),
                PartPose.offset(0F, -8.2F, 0F));
        PartDefinition p_ice_spike_0 = p_crystal_crown.addOrReplaceChild("ice_spike_0", CubeListBuilder.create()
                .texOffs(31, 71).addBox(-0.6F, -3.2F, -0.6F, 1.2F, 3.2F, 1.2F),
                PartPose.offsetAndRotation(-3F, -1F, -3.6F, -0.1396F, 0F, -0.2443F));
        PartDefinition p_ice_spike_1 = p_crystal_crown.addOrReplaceChild("ice_spike_1", CubeListBuilder.create()
                .texOffs(224, 61).addBox(-0.6F, -4.4F, -0.6F, 1.2F, 4.4F, 1.2F),
                PartPose.offsetAndRotation(-1.4F, -1F, -3.6F, -0.1396F, 0F, -0.1047F));
        PartDefinition p_ice_spike_2 = p_crystal_crown.addOrReplaceChild("ice_spike_2", CubeListBuilder.create()
                .texOffs(157, 61).addBox(-0.6F, -5.6F, -0.6F, 1.2F, 5.6F, 1.2F),
                PartPose.offsetAndRotation(0.2F, -1F, -3.6F, -0.1396F, 0F, 0F));
        PartDefinition p_ice_spike_3 = p_crystal_crown.addOrReplaceChild("ice_spike_3", CubeListBuilder.create()
                .texOffs(233, 61).addBox(-0.6F, -4.4F, -0.6F, 1.2F, 4.4F, 1.2F),
                PartPose.offsetAndRotation(1.8F, -1F, -3.6F, -0.1396F, 0F, 0.1047F));
        PartDefinition p_ice_spike_4 = p_crystal_crown.addOrReplaceChild("ice_spike_4", CubeListBuilder.create()
                .texOffs(40, 71).addBox(-0.6F, -3.2F, -0.6F, 1.2F, 3.2F, 1.2F),
                PartPose.offsetAndRotation(3.2F, -1F, -3.6F, -0.1396F, 0F, 0.2443F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(119, 0).addBox(-6.6F, -0.6F, -6.6F, 13.2F, 1F, 13.2F)
                .texOffs(90, 35).addBox(-4F, -4.4F, -4F, 8F, 3.8F, 8F)
                .texOffs(0, 49).addBox(-4.1F, -1.8F, -4.1F, 8.2F, 1.2F, 8.2F),
                PartPose.offset(0F, -8.4F, 0F));
        PartDefinition p_hat_mid = p_hat.addOrReplaceChild("hat_mid", CubeListBuilder.create()
                .texOffs(111, 49).addBox(-2.9F, -3.8F, -2.9F, 5.8F, 3.8F, 5.8F),
                PartPose.offsetAndRotation(0F, -4.4F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_hat_top = p_hat_mid.addOrReplaceChild("hat_top", CubeListBuilder.create()
                .texOffs(97, 61).addBox(-1.9F, -3.4F, -1.9F, 3.8F, 3.4F, 3.8F),
                PartPose.offsetAndRotation(0F, -3.8F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_hat_tip = p_hat_top.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                .texOffs(106, 71).addBox(-1F, -3F, -1F, 2F, 3F, 2F)
                .texOffs(37, 79).addBox(-0.5F, -4.2F, -0.5F, 1F, 1.4F, 1F),
                PartPose.offsetAndRotation(0F, -3.4F, 0F, -0.4887F, 0F, 0F));
        PartDefinition p_antlers = p_head.addOrReplaceChild("antlers", CubeListBuilder.create()
                .texOffs(0, 61).addBox(-3.9F, -0.9F, -3.9F, 7.8F, 0.9F, 7.8F),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_antler_l = p_antlers.addOrReplaceChild("antler_l", CubeListBuilder.create()
                .texOffs(166, 61).addBox(-0.6F, -6F, -0.6F, 1.2F, 6F, 1.2F)
                .texOffs(92, 79).addBox(0.4F, -4.2F, -0.5F, 2.4F, 1F, 1F)
                .texOffs(124, 79).addBox(0.2F, -7.2F, -0.5F, 1.8F, 1F, 1F)
                .texOffs(241, 71).addBox(-1.6F, -3F, -0.4F, 2.2F, 1.6F, 0.8F)
                .texOffs(9, 79).addBox(0.8F, -6F, -0.4F, 1.8F, 1.4F, 0.8F),
                PartPose.offsetAndRotation(2.6F, -0.8F, -1F, -0.1745F, 0F, 0.4189F));
        PartDefinition p_antler_r = p_antlers.addOrReplaceChild("antler_r", CubeListBuilder.create()
                .texOffs(175, 61).addBox(-0.6F, -6F, -0.6F, 1.2F, 6F, 1.2F)
                .texOffs(101, 79).addBox(-2.8F, -4.2F, -0.5F, 2.4F, 1F, 1F)
                .texOffs(131, 79).addBox(-2F, -7.2F, -0.5F, 1.8F, 1F, 1F)
                .texOffs(0, 79).addBox(-0.6F, -3F, -0.4F, 2.2F, 1.6F, 0.8F)
                .texOffs(16, 79).addBox(-2.6F, -6F, -0.4F, 1.8F, 1.4F, 0.8F),
                PartPose.offsetAndRotation(-2.6F, -0.8F, -1F, -0.1745F, 0F, -0.4189F));
        PartDefinition p_stone_circlet = p_head.addOrReplaceChild("stone_circlet", CubeListBuilder.create()
                .texOffs(49, 35).addBox(-4.6F, -1.6F, -4.6F, 9.2F, 1.8F, 9.2F)
                .texOffs(188, 71).addBox(-1.2F, -3.4F, -4.9F, 2.4F, 2.4F, 0.8F)
                .texOffs(23, 79).addBox(-0.7F, -2.9F, -5.2F, 1.4F, 1.4F, 0.4F),
                PartPose.offset(0F, -8.4F, 0F));
        PartDefinition p_shell_crown = p_head.addOrReplaceChild("shell_crown", CubeListBuilder.create()
                .texOffs(33, 61).addBox(-4F, -1F, -4F, 8F, 1F, 8F)
                .texOffs(75, 71).addBox(-2.2F, -4F, -4.3F, 4.4F, 3.2F, 0.9F)
                .texOffs(30, 79).addBox(-0.6F, -3F, -4.6F, 1.2F, 1.2F, 0.4F),
                PartPose.offset(0F, -8.2F, 0F));
        PartDefinition p_fin_l = p_shell_crown.addOrReplaceChild("fin_l", CubeListBuilder.create()
                .texOffs(178, 49).addBox(0F, -4.6F, -2.4F, 0.6F, 4.6F, 5F),
                PartPose.offsetAndRotation(3.9F, -1F, 0F, 0F, 0F, -0.3491F));
        PartDefinition p_fin_r = p_shell_crown.addOrReplaceChild("fin_r", CubeListBuilder.create()
                .texOffs(191, 49).addBox(-0.6F, -4.6F, -2.4F, 0.6F, 4.6F, 5F),
                PartPose.offsetAndRotation(-3.9F, -1F, 0F, 0F, 0F, 0.3491F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(123, 35).addBox(-1.6F, -1F, -1.6F, 3.2F, 8F, 3.2F)
                .texOffs(136, 49).addBox(-2.5F, 6.4F, -2.5F, 5F, 4.4F, 5F)
                .texOffs(115, 71).addBox(-1F, 10.2F, -1F, 2F, 2.6F, 2F),
                PartPose.offset(5.4F, -11F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(140, 35).addBox(-1.6F, -1F, -1.6F, 3.2F, 8F, 3.2F)
                .texOffs(157, 49).addBox(-2.5F, 6.4F, -2.5F, 5F, 4.4F, 5F)
                .texOffs(124, 71).addBox(-1F, 10.2F, -1F, 2F, 2.6F, 2F),
                PartPose.offset(-5.4F, -11F, 0F));
        PartDefinition p_staff = p_right_arm.addOrReplaceChild("staff", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.6F, -19F, -0.6F, 1.2F, 32F, 1.2F)
                .texOffs(197, 71).addBox(-1F, -20.4F, -1F, 2F, 1.6F, 2F)
                .texOffs(114, 61).addBox(-1.9F, -25.2F, -1.9F, 3.8F, 3.8F, 3.8F)
                .texOffs(242, 61).addBox(-2.05F, -25.8F, -2.05F, 0.7F, 5.2F, 0.7F)
                .texOffs(247, 61).addBox(1.35F, -25.8F, -2.05F, 0.7F, 5.2F, 0.7F)
                .texOffs(252, 61).addBox(-2.05F, -25.8F, 1.35F, 0.7F, 5.2F, 0.7F)
                .texOffs(0, 71).addBox(1.35F, -25.8F, 1.35F, 0.7F, 5.2F, 0.7F)
                .texOffs(206, 71).addBox(-0.9F, 12.6F, -0.9F, 1.8F, 1.4F, 1.8F),
                PartPose.offset(0F, 11.5F, -0.4F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
