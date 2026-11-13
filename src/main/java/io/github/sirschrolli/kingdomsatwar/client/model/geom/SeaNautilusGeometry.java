package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Nautilusse (Knochen wie beim Vanilla-Nautilus, Fangarme an den Mundteilen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SeaNautilusGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private SeaNautilusGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0F, 29F, -6F));
        PartDefinition p_shell = p_root.addOrReplaceChild("shell", CubeListBuilder.create()
                .texOffs(53, 47).addBox(-6F, -10F, -5F, 12F, 1F, 12F)
                .texOffs(59, 30).addBox(-7F, -9F, -6F, 14F, 1F, 14F)
                .texOffs(0, 0).addBox(-7F, -8F, -6F, 14F, 14F, 15F)
                .texOffs(102, 47).addBox(-6F, -7F, -7F, 12F, 12F, 1F)
                .texOffs(0, 30).addBox(-7F, 6F, -6F, 14F, 1F, 15F)
                .texOffs(0, 47).addBox(-6F, 7F, -5F, 12F, 1F, 14F)
                .texOffs(0, 63).addBox(6F, 0F, 9F, 1F, 7F, 4F)
                .texOffs(11, 63).addBox(-7F, 0F, 9F, 1F, 7F, 4F)
                .texOffs(0, 92).addBox(-7F, 7F, 9F, 14F, 1F, 4F),
                PartPose.offset(0F, -13F, 5F));
        PartDefinition p_corals = p_shell.addOrReplaceChild("corals", CubeListBuilder.create()
                .texOffs(19, 98).addBox(-3F, -8F, -7F, 2F, 1F, 1F)
                .texOffs(40, 98).addBox(-4F, -8F, -7F, 1F, 1F, 1F)
                .texOffs(26, 98).addBox(2F, -10F, -6F, 2F, 1F, 1F)
                .texOffs(14, 98).addBox(6F, -3F, -7F, 1F, 2F, 1F)
                .texOffs(45, 98).addBox(-7F, 1F, -7F, 1F, 1F, 1F)
                .texOffs(33, 98).addBox(1F, 5F, -7F, 2F, 1F, 1F)
                .texOffs(50, 98).addBox(-4F, 7F, -6F, 1F, 1F, 1F)
                .texOffs(55, 98).addBox(4F, -8F, -7F, 1F, 1F, 1F)
                .texOffs(41, 63).addBox(2.5F, 8F, -2F, 0F, 4F, 5F)
                .texOffs(89, 75).addBox(-3.5F, 8F, 2F, 0F, 3F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(59, 0).addBox(-5F, -4.51F, -3F, 10F, 8F, 10F)
                .texOffs(37, 92).addBox(5F, -2.5F, 5.7F, 1F, 2F, 2F)
                .texOffs(44, 92).addBox(-6F, -2.5F, 5.7F, 1F, 2F, 2F),
                PartPose.offset(0F, -8.5F, 12.3F));
        PartDefinition p_upper_mouth = p_body.addOrReplaceChild("upper_mouth", CubeListBuilder.create()
                .texOffs(52, 63).addBox(-5F, -2F, 0F, 10F, 4F, 4F, new CubeDeformation(-0.001F)),
                PartPose.offset(0F, -2.51F, 7F));
        PartDefinition p_tentacle_u0 = p_upper_mouth.addOrReplaceChild("tentacle_u0", CubeListBuilder.create()
                .texOffs(45, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 5F),
                PartPose.offsetAndRotation(-4.5F, 1.5F, 3.5F, 0.2443F, -0.3142F, 0F));
        PartDefinition p_tentacle_u0_tip = p_tentacle_u0.addOrReplaceChild("tentacle_u0_tip", CubeListBuilder.create()
                .texOffs(51, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 5F, 0.5236F, 0F, 0F));
        PartDefinition p_tentacle_u1 = p_upper_mouth.addOrReplaceChild("tentacle_u1", CubeListBuilder.create()
                .texOffs(21, 75).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(-3F, 0.5F, 3.5F, 0.1396F, -0.2094F, 0F));
        PartDefinition p_tentacle_u1_tip = p_tentacle_u1.addOrReplaceChild("tentacle_u1_tip", CubeListBuilder.create()
                .texOffs(58, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.384F, 0F, 0F));
        PartDefinition p_tentacle_u2 = p_upper_mouth.addOrReplaceChild("tentacle_u2", CubeListBuilder.create()
                .texOffs(100, 75).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(-1.5F, 1.5F, 3.5F, 0.2094F, -0.1047F, 0F));
        PartDefinition p_tentacle_u2_tip = p_tentacle_u2.addOrReplaceChild("tentacle_u2_tip", CubeListBuilder.create()
                .texOffs(65, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.3142F, 0F, 0F));
        PartDefinition p_tentacle_u3 = p_upper_mouth.addOrReplaceChild("tentacle_u3", CubeListBuilder.create()
                .texOffs(22, 63).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 8F),
                PartPose.offsetAndRotation(0F, 0.5F, 3.5F, 0.1047F, 0F, 0F));
        PartDefinition p_tentacle_u3_tip = p_tentacle_u3.addOrReplaceChild("tentacle_u3_tip", CubeListBuilder.create()
                .texOffs(72, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.4538F, 0F, 0F));
        PartDefinition p_tentacle_u4 = p_upper_mouth.addOrReplaceChild("tentacle_u4", CubeListBuilder.create()
                .texOffs(0, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(1.5F, 1.5F, 3.5F, 0.192F, 0.1047F, 0F));
        PartDefinition p_tentacle_u4_tip = p_tentacle_u4.addOrReplaceChild("tentacle_u4_tip", CubeListBuilder.create()
                .texOffs(79, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.3491F, 0F, 0F));
        PartDefinition p_tentacle_u5 = p_upper_mouth.addOrReplaceChild("tentacle_u5", CubeListBuilder.create()
                .texOffs(38, 75).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(3F, 0.5F, 3.5F, 0.1571F, 0.2094F, 0F));
        PartDefinition p_tentacle_u5_tip = p_tentacle_u5.addOrReplaceChild("tentacle_u5_tip", CubeListBuilder.create()
                .texOffs(86, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.4189F, 0F, 0F));
        PartDefinition p_tentacle_u6 = p_upper_mouth.addOrReplaceChild("tentacle_u6", CubeListBuilder.create()
                .texOffs(58, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 5F),
                PartPose.offsetAndRotation(4.5F, 1.5F, 3.5F, 0.2618F, 0.3142F, 0F));
        PartDefinition p_tentacle_u6_tip = p_tentacle_u6.addOrReplaceChild("tentacle_u6_tip", CubeListBuilder.create()
                .texOffs(93, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 5F, 0.5585F, 0F, 0F));
        PartDefinition p_inner_mouth = p_body.addOrReplaceChild("inner_mouth", CubeListBuilder.create()
                .texOffs(0, 75).addBox(-3F, -2F, -0.5F, 6F, 4F, 4F),
                PartPose.offset(0F, -0.51F, 7.5F));
        PartDefinition p_lower_mouth = p_body.addOrReplaceChild("lower_mouth", CubeListBuilder.create()
                .texOffs(81, 63).addBox(-5F, -1.98F, 0F, 10F, 4F, 4F, new CubeDeformation(-0.001F)),
                PartPose.offset(0F, 1.49F, 7F));
        PartDefinition p_tentacle_l0 = p_lower_mouth.addOrReplaceChild("tentacle_l0", CubeListBuilder.create()
                .texOffs(71, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 5F),
                PartPose.offsetAndRotation(-3.75F, -0.5F, 3.5F, -0.1745F, -0.2618F, 0F));
        PartDefinition p_tentacle_l0_tip = p_tentacle_l0.addOrReplaceChild("tentacle_l0_tip", CubeListBuilder.create()
                .texOffs(100, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.4887F, 0F, 0F));
        PartDefinition p_tentacle_l1 = p_lower_mouth.addOrReplaceChild("tentacle_l1", CubeListBuilder.create()
                .texOffs(55, 75).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(-2.25F, 0.5F, 3.5F, -0.2793F, -0.1571F, 0F));
        PartDefinition p_tentacle_l1_tip = p_tentacle_l1.addOrReplaceChild("tentacle_l1_tip", CubeListBuilder.create()
                .texOffs(107, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 7F, -0.3491F, 0F, 0F));
        PartDefinition p_tentacle_l2 = p_lower_mouth.addOrReplaceChild("tentacle_l2", CubeListBuilder.create()
                .texOffs(15, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(-0.75F, -0.5F, 3.5F, -0.2094F, -0.0524F, 0F));
        PartDefinition p_tentacle_l2_tip = p_tentacle_l2.addOrReplaceChild("tentacle_l2_tip", CubeListBuilder.create()
                .texOffs(114, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 6F, 0.3142F, 0F, 0F));
        PartDefinition p_tentacle_l3 = p_lower_mouth.addOrReplaceChild("tentacle_l3", CubeListBuilder.create()
                .texOffs(30, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 6F),
                PartPose.offsetAndRotation(0.75F, 0.5F, 3.5F, -0.2269F, 0.0524F, 0F));
        PartDefinition p_tentacle_l3_tip = p_tentacle_l3.addOrReplaceChild("tentacle_l3_tip", CubeListBuilder.create()
                .texOffs(121, 92).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 6F, -0.384F, 0F, 0F));
        PartDefinition p_tentacle_l4 = p_lower_mouth.addOrReplaceChild("tentacle_l4", CubeListBuilder.create()
                .texOffs(72, 75).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F),
                PartPose.offsetAndRotation(2.25F, -0.5F, 3.5F, -0.2618F, 0.1571F, 0F));
        PartDefinition p_tentacle_l4_tip = p_tentacle_l4.addOrReplaceChild("tentacle_l4_tip", CubeListBuilder.create()
                .texOffs(0, 98).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 7F, 0.3491F, 0F, 0F));
        PartDefinition p_tentacle_l5 = p_lower_mouth.addOrReplaceChild("tentacle_l5", CubeListBuilder.create()
                .texOffs(84, 84).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 5F),
                PartPose.offsetAndRotation(3.75F, 0.5F, 3.5F, -0.1571F, 0.2618F, 0F));
        PartDefinition p_tentacle_l5_tip = p_tentacle_l5.addOrReplaceChild("tentacle_l5_tip", CubeListBuilder.create()
                .texOffs(7, 98).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 5F, -0.5236F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
