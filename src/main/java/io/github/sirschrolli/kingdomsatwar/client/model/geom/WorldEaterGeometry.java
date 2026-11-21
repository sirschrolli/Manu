package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kopfs des Weltenfressers (im Spiel doppelt so groß).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WorldEaterGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 512;

    private WorldEaterGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-18F, -16F, -12F, 36F, 32F, 28F)
                .texOffs(425, 0).addBox(-14F, -13F, 15F, 28F, 26F, 7F)
                .texOffs(453, 61).addBox(-10F, -9F, 21F, 20F, 18F, 4F)
                .texOffs(170, 61).addBox(-13F, 14F, -10F, 26F, 5F, 24F)
                .texOffs(250, 0).addBox(17F, -11F, -15F, 6F, 20F, 20F)
                .texOffs(378, 129).addBox(22.9F, -9.4F, -13.5F, 1F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(387, 129).addBox(22.9F, -6F, -9.5F, 1F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(396, 129).addBox(22.9F, -2.6F, -5.5F, 1F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(303, 0).addBox(-23F, -11F, -15F, 6F, 20F, 20F)
                .texOffs(405, 129).addBox(-23.9F, -9.4F, -13.5F, 1F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(414, 129).addBox(-23.9F, -6F, -9.5F, 1F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(423, 129).addBox(-23.9F, -2.6F, -5.5F, 1F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(38, 92).addBox(-15F, -15F, -22F, 30F, 7F, 10F)
                .texOffs(119, 92).addBox(-15F, 8F, -22F, 30F, 7F, 10F)
                .texOffs(292, 61).addBox(8F, -8F, -22F, 7F, 16F, 10F)
                .texOffs(327, 61).addBox(-15F, -8F, -22F, 7F, 16F, 10F)
                .texOffs(356, 0).addBox(-16.5F, -16.5F, -12.8F, 33F, 33F, 1F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(362, 92).addBox(-8F, -8F, -13F, 16F, 16F, 1F)
                .texOffs(63, 139).addBox(5.4F, -1F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(74, 139).addBox(4.5426F, 2.2F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(85, 139).addBox(2.2F, 4.5426F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(96, 139).addBox(-1F, 5.4F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(107, 139).addBox(-4.2F, 4.5426F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(118, 139).addBox(-6.5426F, 2.2F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(129, 139).addBox(-7.4F, -1F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(140, 139).addBox(-6.5426F, -4.2F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(151, 139).addBox(-4.2F, -6.5426F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(162, 139).addBox(-1F, -7.4F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(173, 139).addBox(2.2F, -6.5426F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(184, 139).addBox(4.5426F, -4.2F, -19F, 2F, 2F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_skull_oct = p_head.addOrReplaceChild("skull_oct", CubeListBuilder.create()
                .texOffs(129, 0).addBox(-17F, -17F, -11F, 34F, 34F, 26F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(0, 61).addBox(-9F, -4F, -2F, 18F, 6F, 24F),
                PartPose.offsetAndRotation(9F, -17F, -14F, 0.2094F, 0F, 0.2793F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(85, 61).addBox(-9F, -4F, -2F, 18F, 6F, 24F),
                PartPose.offsetAndRotation(-9F, -17F, -14F, 0.2094F, 0F, -0.2793F));
        PartDefinition p_snout_oct = p_head.addOrReplaceChild("snout_oct", CubeListBuilder.create()
                .texOffs(200, 92).addBox(-15F, -15F, -22F, 30F, 7F, 10F)
                .texOffs(281, 92).addBox(-15F, 8F, -22F, 30F, 7F, 10F)
                .texOffs(362, 61).addBox(8F, -8F, -22F, 7F, 16F, 10F)
                .texOffs(397, 61).addBox(-15F, -8F, -22F, 7F, 16F, 10F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_lip0_base = p_head.addOrReplaceChild("lip0_base", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, -22F));
        PartDefinition p_lip0 = p_lip0_base.addOrReplaceChild("lip0", CubeListBuilder.create()
                .texOffs(0, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(25, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(209, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(432, 129).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(441, 129).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(195, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(204, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip1_base = p_head.addOrReplaceChild("lip1_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 0.7854F));
        PartDefinition p_lip1 = p_lip1_base.addOrReplaceChild("lip1", CubeListBuilder.create()
                .texOffs(39, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(48, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(222, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(450, 129).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(459, 129).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(213, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(222, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip2_base = p_head.addOrReplaceChild("lip2_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 1.5708F));
        PartDefinition p_lip2 = p_lip2_base.addOrReplaceChild("lip2", CubeListBuilder.create()
                .texOffs(78, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(71, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(235, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(468, 129).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(477, 129).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(231, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(240, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip3_base = p_head.addOrReplaceChild("lip3_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 2.3562F));
        PartDefinition p_lip3 = p_lip3_base.addOrReplaceChild("lip3", CubeListBuilder.create()
                .texOffs(117, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(94, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(248, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(486, 129).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(495, 129).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(249, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(258, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip4_base = p_head.addOrReplaceChild("lip4_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 3.1416F));
        PartDefinition p_lip4 = p_lip4_base.addOrReplaceChild("lip4", CubeListBuilder.create()
                .texOffs(156, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(117, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(261, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(504, 129).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(0, 139).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(267, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(276, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip5_base = p_head.addOrReplaceChild("lip5_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 3.927F));
        PartDefinition p_lip5 = p_lip5_base.addOrReplaceChild("lip5", CubeListBuilder.create()
                .texOffs(195, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(140, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(274, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(9, 139).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(18, 139).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(285, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(294, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip6_base = p_head.addOrReplaceChild("lip6_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 4.7124F));
        PartDefinition p_lip6 = p_lip6_base.addOrReplaceChild("lip6", CubeListBuilder.create()
                .texOffs(234, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(163, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(287, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(27, 139).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(36, 139).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(303, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(312, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_lip7_base = p_head.addOrReplaceChild("lip7_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, -22F, 0F, 0F, 5.4978F));
        PartDefinition p_lip7 = p_lip7_base.addOrReplaceChild("lip7", CubeListBuilder.create()
                .texOffs(273, 115).addBox(-5F, -2.9F, -9F, 10F, 4F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(186, 129).addBox(-3F, -2.3F, -14F, 6F, 3F, 5F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(300, 129).addBox(-1.5F, -2.2F, -17F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.4F, 0F))
                .texOffs(45, 139).addBox(-3F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(54, 139).addBox(1F, 0.2F, -4.2F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(321, 139).addBox(-3F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(330, 139).addBox(1F, 0.4F, -10.2F, 2F, 3F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11F, 0F, -0.3142F, 0F, 0F));
        PartDefinition p_tusk_left = p_head.addOrReplaceChild("tusk_left", CubeListBuilder.create()
                .texOffs(448, 92).addBox(-2F, -2F, -10F, 4F, 4F, 10F),
                PartPose.offsetAndRotation(13.5F, 11F, -20F, 0.4887F, 0.2443F, 0.1047F));
        PartDefinition p_tusk_left_tip = p_tusk_left.addOrReplaceChild("tusk_left_tip", CubeListBuilder.create()
                .texOffs(312, 115).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F)
                .texOffs(313, 129).addBox(-1F, -1F, -12F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.5934F, 0F, 0F));
        PartDefinition p_tusk_right = p_head.addOrReplaceChild("tusk_right", CubeListBuilder.create()
                .texOffs(477, 92).addBox(-2F, -2F, -10F, 4F, 4F, 10F),
                PartPose.offsetAndRotation(-13.5F, 11F, -20F, 0.4887F, -0.2443F, -0.1047F));
        PartDefinition p_tusk_right_tip = p_tusk_right.addOrReplaceChild("tusk_right_tip", CubeListBuilder.create()
                .texOffs(335, 115).addBox(-1.5F, -1.5F, -8F, 3F, 3F, 8F)
                .texOffs(326, 129).addBox(-1F, -1F, -12F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, -10F, -0.5934F, 0F, 0F));
        PartDefinition p_shard0 = p_head.addOrReplaceChild("shard0", CubeListBuilder.create()
                .texOffs(358, 115).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(271, 61).addBox(-2.5F, -22F, -2.5F, 5F, 22F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(339, 129).addBox(-1.5F, -25F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -19F, -4F, -0.6632F, 0F, 0F));
        PartDefinition p_shard1 = p_head.addOrReplaceChild("shard1", CubeListBuilder.create()
                .texOffs(383, 115).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 92).addBox(-2.5F, -17F, -2.5F, 5F, 17F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(352, 129).addBox(-1.5F, -20F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(9F, -19F, 0F, -0.5236F, -0.3142F, -0.384F));
        PartDefinition p_shard2 = p_head.addOrReplaceChild("shard2", CubeListBuilder.create()
                .texOffs(408, 115).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(432, 61).addBox(-2.5F, -18F, -2.5F, 5F, 18F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(365, 129).addBox(-1.5F, -21F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-9F, -19F, 0F, -0.5236F, 0.3142F, 0.384F));
        PartDefinition p_shard3 = p_head.addOrReplaceChild("shard3", CubeListBuilder.create()
                .texOffs(433, 115).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(397, 92).addBox(-2F, -13F, -2F, 4F, 13F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(339, 139).addBox(-1F, -16F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(15F, -16F, 8F, -0.3491F, -0.5236F, -0.6981F));
        PartDefinition p_shard4 = p_head.addOrReplaceChild("shard4", CubeListBuilder.create()
                .texOffs(458, 115).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(21, 92).addBox(-2F, -14F, -2F, 4F, 14F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(348, 139).addBox(-1F, -17F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-15F, -16F, 8F, -0.384F, 0.5236F, 0.6981F));
        PartDefinition p_shard5 = p_head.addOrReplaceChild("shard5", CubeListBuilder.create()
                .texOffs(483, 115).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(414, 92).addBox(-2F, -12F, -2F, 4F, 12F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(357, 139).addBox(-1F, -15F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(5F, -18F, 13F, -1.0123F, -0.1745F, -0.1745F));
        PartDefinition p_shard6 = p_head.addOrReplaceChild("shard6", CubeListBuilder.create()
                .texOffs(0, 129).addBox(-3F, -2.6F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(431, 92).addBox(-2F, -11F, -2F, 4F, 11F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(366, 139).addBox(-1F, -14F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-6F, -18F, 14F, -1.0821F, 0.2094F, 0.2094F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
