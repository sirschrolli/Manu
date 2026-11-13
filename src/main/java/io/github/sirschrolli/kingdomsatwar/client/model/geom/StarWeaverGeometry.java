package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Sternenweberin (seit 0.99): riesige Spinne, Vorderleib aus nachtblauem Chitin mit acht Sternenaugen, Kieferklauen mit Giftzähnen und Tastern, ein Hinterleib wie Nachthimmel mit Spiralgalaxie, drei Kristalle der Leere, Spinnwarzen, acht Beine aus Hüfte, Schenkel, Schiene und Fuß mit leuchtenden Gelenken; im Spiel ×2.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class StarWeaverGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private StarWeaverGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(190, 24).addBox(-4.5F, 1.2F, -5.5F, 9F, 2F, 11F, new CubeDeformation(-0.3F, -0.2F, -0.1F))
                .texOffs(0, 24).addBox(-5.5F, -1.6F, -6.6F, 11F, 3F, 13F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(145, 24).addBox(-5F, -3.3F, -6.2F, 10F, 2F, 12F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(0, 41).addBox(-4F, -4F, -5.3F, 8F, 1F, 10F, new CubeDeformation(-0.4F, 0F, -0.3F))
                .texOffs(99, 41).addBox(-2.5F, -4.8F, -3.9F, 5F, 1F, 7F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(124, 41).addBox(-4F, -2.9F, -8.75F, 8F, 5F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.15F))
                .texOffs(105, 72).addBox(-3F, -3.65F, -8.5F, 6F, 1F, 3F, new CubeDeformation(0F, -0.05F, -0.3F))
                .texOffs(45, 77).addBox(-2.1F, -2.35F, -9.25F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(52, 77).addBox(0.1F, -2.35F, -9.25F, 2F, 2F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(86, 77).addBox(-3.4F, -1.2F, -9.15F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(91, 77).addBox(2.4F, -1.2F, -9.15F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(96, 77).addBox(-1.8F, -4F, -7.7F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(101, 77).addBox(0.8F, -4F, -7.7F, 1F, 1F, 1F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(106, 77).addBox(-3.45F, -3.35F, -7.05F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(111, 77).addBox(2.45F, -3.35F, -7.05F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.15F))
                .texOffs(211, 53).addBox(-2F, -1.5F, 5.2F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_fangs = p_body.addOrReplaceChild("fangs", CubeListBuilder.create()
                .texOffs(177, 41).addBox(-0.1F, -0.6F, -1.85F, 3F, 4F, 3F, new CubeDeformation(-0.35F, -0.2F, -0.25F))
                .texOffs(190, 41).addBox(-2.9F, -0.6F, -1.85F, 3F, 4F, 3F, new CubeDeformation(-0.35F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 1.2F, -8.2F, -0.1396F, 0F, 0F));
        PartDefinition p_fang_l = p_fangs.addOrReplaceChild("fang_l", CubeListBuilder.create()
                .texOffs(35, 77).addBox(-0.5F, -0.3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(66, 77).addBox(-0.5F, 1.8F, -0.95F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(1.4F, 3.1F, -0.8F, -0.3142F, 0F, -0.4538F));
        PartDefinition p_fang_r = p_fangs.addOrReplaceChild("fang_r", CubeListBuilder.create()
                .texOffs(40, 77).addBox(-0.5F, -0.3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(71, 77).addBox(-0.5F, 1.8F, -0.95F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(-1.4F, 3.1F, -0.8F, -0.3142F, 0F, 0.4538F));
        PartDefinition p_palp_l = p_body.addOrReplaceChild("palp_l", CubeListBuilder.create()
                .texOffs(147, 41).addBox(-1F, -1F, -4.5F, 2F, 2F, 5F, new CubeDeformation(-0.35F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(2.9F, 0.6F, -8F, -0.5236F, 0.2443F, 0F));
        PartDefinition p_palp_l_tip = p_palp_l.addOrReplaceChild("palp_l_tip", CubeListBuilder.create()
                .texOffs(226, 53).addBox(-1F, -1F, -3.5F, 2F, 2F, 4F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.8029F, 0F, 0F));
        PartDefinition p_palp_r = p_body.addOrReplaceChild("palp_r", CubeListBuilder.create()
                .texOffs(162, 41).addBox(-1F, -1F, -4.5F, 2F, 2F, 5F, new CubeDeformation(-0.35F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(-2.9F, 0.6F, -8F, -0.5236F, -0.2443F, 0F));
        PartDefinition p_palp_r_tip = p_palp_r.addOrReplaceChild("palp_r_tip", CubeListBuilder.create()
                .texOffs(239, 53).addBox(-1F, -1F, -3.5F, 2F, 2F, 4F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.8029F, 0F, 0F));
        PartDefinition p_abdomen = p_body.addOrReplaceChild("abdomen", CubeListBuilder.create()
                .texOffs(72, 41).addBox(-3F, -8F, 4.9F, 6F, 2F, 7F, new CubeDeformation(-0.0403F, -0.4F, -0.0729F))
                .texOffs(98, 24).addBox(-5.5F, -6.6F, 2.4F, 11F, 2F, 12F, new CubeDeformation(-0.3619F, -0.2F, -0.0506F))
                .texOffs(69, 0).addBox(-7F, -5.2F, 0.4F, 14F, 3F, 16F, new CubeDeformation(-0.3615F, -0.4F, -0.3133F))
                .texOffs(0, 0).addBox(-8F, -2.6F, -0.6F, 16F, 5F, 18F, new CubeDeformation(-0.4007F, 0F, -0.2008F))
                .texOffs(130, 0).addBox(-7F, 2F, 0.4F, 14F, 3F, 16F, new CubeDeformation(-0.2539F, -0.4F, -0.1887F))
                .texOffs(49, 24).addBox(-5.5F, 4.4F, 1.9F, 11F, 2F, 13F, new CubeDeformation(-0.1521F, -0.2F, -0.3077F))
                .texOffs(37, 41).addBox(-4F, 6.2F, 3.9F, 8F, 1F, 9F, new CubeDeformation(-0.4125F, 0F, -0.3461F))
                .texOffs(88, 60).addBox(-6F, -2.1F, -1.2F, 12F, 4F, 1F, new CubeDeformation(-0.072F, -0.1F, -0.2F))
                .texOffs(115, 60).addBox(-6F, -2.1F, 17F, 12F, 4F, 1F, new CubeDeformation(-0.072F, -0.1F, -0.2F))
                .texOffs(191, 0).addBox(-8.4F, -2.1F, 1.4F, 1F, 4F, 14F, new CubeDeformation(-0.2F, -0.1F, -0.4F))
                .texOffs(222, 0).addBox(7.4F, -2.1F, 1.4F, 1F, 4F, 14F, new CubeDeformation(-0.2F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, -0.6F, 7F, 0.1745F, 0F, 0F));
        PartDefinition p_crystal_0 = p_abdomen.addOrReplaceChild("crystal_0", CubeListBuilder.create()
                .texOffs(203, 41).addBox(-1.5F, -3.68F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4F, -0.32F, -0.4F)),
                PartPose.offsetAndRotation(0.3F, -7F, 4.6F, -0.4189F, 0F, 0.1047F));
        PartDefinition p_crystal_0_tip = p_crystal_0.addOrReplaceChild("crystal_0_tip", CubeListBuilder.create()
                .texOffs(164, 60).addBox(-1F, -2.76F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.395F, -0.24F, -0.395F)),
                PartPose.offset(0F, -3.16F, 0F));
        PartDefinition p_crystal_1 = p_abdomen.addOrReplaceChild("crystal_1", CubeListBuilder.create()
                .texOffs(173, 60).addBox(-1F, -2.76F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.15F, -0.24F, -0.15F)),
                PartPose.offsetAndRotation(-3.6F, -5.6F, 9.6F, -0.5236F, 0F, -0.384F));
        PartDefinition p_crystal_1_tip = p_crystal_1.addOrReplaceChild("crystal_1_tip", CubeListBuilder.create()
                .texOffs(76, 77).addBox(-0.5F, -1.945F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0325F, -0.055F, -0.0325F)),
                PartPose.offset(0F, -2.32F, 0F));
        PartDefinition p_crystal_2 = p_abdomen.addOrReplaceChild("crystal_2", CubeListBuilder.create()
                .texOffs(182, 60).addBox(-1F, -2.58F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.42F, -0.25F)),
                PartPose.offsetAndRotation(3.4F, -5.4F, 12F, -0.6632F, 0F, 0.4189F));
        PartDefinition p_crystal_2_tip = p_crystal_2.addOrReplaceChild("crystal_2_tip", CubeListBuilder.create()
                .texOffs(81, 77).addBox(-0.5F, -1.81F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.0875F, -0.19F, -0.0875F)),
                PartPose.offset(0F, -1.96F, 0F));
        PartDefinition p_spinnerets = p_abdomen.addOrReplaceChild("spinnerets", CubeListBuilder.create()
                .texOffs(142, 60).addBox(-1.85F, -1.05F, -0.4F, 2F, 2F, 3F, new CubeDeformation(-0.45F, -0.45F, -0.4F))
                .texOffs(153, 60).addBox(-0.15F, -1.05F, -0.4F, 2F, 2F, 3F, new CubeDeformation(-0.45F, -0.45F, -0.4F))
                .texOffs(59, 77).addBox(-0.5F, 0.6F, 0.1F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, 4F, 15.968F, 0.5236F, 0F, 0F));
        PartDefinition p_leg_l1 = root.addOrReplaceChild("leg_l1", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.6F, 15F, -4.6F, 0F, 0.9425F, 0F));
        PartDefinition p_leg_l1_femur = p_leg_l1.addOrReplaceChild("leg_l1_femur", CubeListBuilder.create()
                .texOffs(216, 41).addBox(-0.25F, -1.5F, -1.5F, 13F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_leg_l1_tibia = p_leg_l1_femur.addOrReplaceChild("leg_l1_tibia", CubeListBuilder.create()
                .texOffs(191, 60).addBox(-0.55F, -1F, -1F, 15F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(0, 60).addBox(-1.2F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(12.5F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_leg_l1_tarsus = p_leg_l1_tibia.addOrReplaceChild("leg_l1_tarsus", CubeListBuilder.create()
                .texOffs(225, 67).addBox(-0.2F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(162, 72).addBox(-1.1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(234, 72).addBox(7.1F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(14.5F, 0F, 0F, 0F, 0F, -0.1885F));
        PartDefinition p_leg_l2 = root.addOrReplaceChild("leg_l2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5F, 15F, -1.6F, 0F, 0.4189F, 0F));
        PartDefinition p_leg_l2_femur = p_leg_l2.addOrReplaceChild("leg_l2_femur", CubeListBuilder.create()
                .texOffs(33, 53).addBox(-0.25F, -1.5F, -1.5F, 12F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_leg_l2_tibia = p_leg_l2_femur.addOrReplaceChild("leg_l2_tibia", CubeListBuilder.create()
                .texOffs(35, 67).addBox(-0.55F, -1F, -1F, 14F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(11, 60).addBox(-1.2F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(11.5F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_leg_l2_tarsus = p_leg_l2_tibia.addOrReplaceChild("leg_l2_tarsus", CubeListBuilder.create()
                .texOffs(0, 72).addBox(-0.45F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(-0.25F, -0.35F, -0.35F))
                .texOffs(171, 72).addBox(-1.1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(241, 72).addBox(6.6F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(13.5F, 0F, 0F, 0F, 0F, -0.0957F));
        PartDefinition p_leg_l3 = root.addOrReplaceChild("leg_l3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5F, 15F, 1.4F, 0F, -0.1745F, 0F));
        PartDefinition p_leg_l3_femur = p_leg_l3.addOrReplaceChild("leg_l3_femur", CubeListBuilder.create()
                .texOffs(157, 53).addBox(0F, -1.5F, -1.5F, 10F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_leg_l3_tibia = p_leg_l3_femur.addOrReplaceChild("leg_l3_tibia", CubeListBuilder.create()
                .texOffs(167, 67).addBox(-0.3F, -1F, -1F, 12F, 2F, 2F)
                .texOffs(22, 60).addBox(-1.2F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(10F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_leg_l3_tarsus = p_leg_l3_tibia.addOrReplaceChild("leg_l3_tarsus", CubeListBuilder.create()
                .texOffs(124, 72).addBox(-0.2F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(180, 72).addBox(-1.1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(248, 72).addBox(6.1F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(12F, 0F, 0F, 0F, 0F, 0.044F));
        PartDefinition p_leg_l4 = root.addOrReplaceChild("leg_l4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4.6F, 15F, 4.2F, 0F, -0.6981F, 0F));
        PartDefinition p_leg_l4_femur = p_leg_l4.addOrReplaceChild("leg_l4_femur", CubeListBuilder.create()
                .texOffs(64, 53).addBox(0F, -1.5F, -1.5F, 12F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.6632F));
        PartDefinition p_leg_l4_tibia = p_leg_l4_femur.addOrReplaceChild("leg_l4_tibia", CubeListBuilder.create()
                .texOffs(68, 67).addBox(-0.3F, -1F, -1F, 14F, 2F, 2F)
                .texOffs(33, 60).addBox(-1.2F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(12F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_leg_l4_tarsus = p_leg_l4_tibia.addOrReplaceChild("leg_l4_tarsus", CubeListBuilder.create()
                .texOffs(21, 72).addBox(-0.2F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(189, 72).addBox(-1.1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(0, 77).addBox(7.1F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(14F, 0F, 0F, 0F, 0F, -0.1741F));
        PartDefinition p_leg_r1 = root.addOrReplaceChild("leg_r1", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.6F, 15F, -4.6F, 0F, -0.9425F, 0F));
        PartDefinition p_leg_r1_femur = p_leg_r1.addOrReplaceChild("leg_r1_femur", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-12.75F, -1.5F, -1.5F, 13F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_leg_r1_tibia = p_leg_r1_femur.addOrReplaceChild("leg_r1_tibia", CubeListBuilder.create()
                .texOffs(0, 67).addBox(-14.45F, -1F, -1F, 15F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(44, 60).addBox(-0.8F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(-12.5F, 0F, 0F, 0F, 0F, -1.5708F));
        PartDefinition p_leg_r1_tarsus = p_leg_r1_tibia.addOrReplaceChild("leg_r1_tarsus", CubeListBuilder.create()
                .texOffs(42, 72).addBox(-7.8F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(198, 72).addBox(-0.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(7, 77).addBox(-8.1F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-14.5F, 0F, 0F, 0F, 0F, 0.1885F));
        PartDefinition p_leg_r2 = root.addOrReplaceChild("leg_r2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5F, 15F, -1.6F, 0F, -0.4189F, 0F));
        PartDefinition p_leg_r2_femur = p_leg_r2.addOrReplaceChild("leg_r2_femur", CubeListBuilder.create()
                .texOffs(95, 53).addBox(-11.75F, -1.5F, -1.5F, 12F, 3F, 3F, new CubeDeformation(-0.25F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_leg_r2_tibia = p_leg_r2_femur.addOrReplaceChild("leg_r2_tibia", CubeListBuilder.create()
                .texOffs(101, 67).addBox(-13.45F, -1F, -1F, 14F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(55, 60).addBox(-0.8F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(-11.5F, 0F, 0F, 0F, 0F, -1.5708F));
        PartDefinition p_leg_r2_tarsus = p_leg_r2_tibia.addOrReplaceChild("leg_r2_tarsus", CubeListBuilder.create()
                .texOffs(63, 72).addBox(-7.55F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(-0.25F, -0.35F, -0.35F))
                .texOffs(207, 72).addBox(-0.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(14, 77).addBox(-7.6F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-13.5F, 0F, 0F, 0F, 0F, 0.0957F));
        PartDefinition p_leg_r3 = root.addOrReplaceChild("leg_r3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5F, 15F, 1.4F, 0F, 0.1745F, 0F));
        PartDefinition p_leg_r3_femur = p_leg_r3.addOrReplaceChild("leg_r3_femur", CubeListBuilder.create()
                .texOffs(184, 53).addBox(-10F, -1.5F, -1.5F, 10F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_leg_r3_tibia = p_leg_r3_femur.addOrReplaceChild("leg_r3_tibia", CubeListBuilder.create()
                .texOffs(196, 67).addBox(-11.7F, -1F, -1F, 12F, 2F, 2F)
                .texOffs(66, 60).addBox(-0.8F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(-10F, 0F, 0F, 0F, 0F, -1.5708F));
        PartDefinition p_leg_r3_tarsus = p_leg_r3_tibia.addOrReplaceChild("leg_r3_tarsus", CubeListBuilder.create()
                .texOffs(143, 72).addBox(-6.8F, -1F, -1F, 7F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(216, 72).addBox(-0.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(21, 77).addBox(-7.1F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-12F, 0F, 0F, 0F, 0F, -0.044F));
        PartDefinition p_leg_r4 = root.addOrReplaceChild("leg_r4", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4.6F, 15F, 4.2F, 0F, 0.6981F, 0F));
        PartDefinition p_leg_r4_femur = p_leg_r4.addOrReplaceChild("leg_r4_femur", CubeListBuilder.create()
                .texOffs(126, 53).addBox(-12F, -1.5F, -1.5F, 12F, 3F, 3F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.6632F));
        PartDefinition p_leg_r4_tibia = p_leg_r4_femur.addOrReplaceChild("leg_r4_tibia", CubeListBuilder.create()
                .texOffs(134, 67).addBox(-13.7F, -1F, -1F, 14F, 2F, 2F)
                .texOffs(77, 60).addBox(-0.8F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(-12F, 0F, 0F, 0F, 0F, -1.5708F));
        PartDefinition p_leg_r4_tarsus = p_leg_r4_tibia.addOrReplaceChild("leg_r4_tarsus", CubeListBuilder.create()
                .texOffs(84, 72).addBox(-7.8F, -1F, -1F, 8F, 2F, 2F, new CubeDeformation(0F, -0.35F, -0.35F))
                .texOffs(225, 72).addBox(-0.9F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.1F))
                .texOffs(28, 77).addBox(-8.1F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.45F)),
                PartPose.offsetAndRotation(-14F, 0F, 0F, 0F, 0F, 0.1741F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
