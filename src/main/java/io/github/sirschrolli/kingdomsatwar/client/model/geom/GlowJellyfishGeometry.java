package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Leuchtqualle (Glocke in Stufen, leuchtender Saum, vier Mundarme, acht Fangfäden in drei Gliedern; durchscheinend).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlowJellyfishGeometry {
    public static final int TEXTURE_WIDTH = 96;
    public static final int TEXTURE_HEIGHT = 64;

    private GlowJellyfishGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bell = root.addOrReplaceChild("bell", CubeListBuilder.create()
                .texOffs(25, 41).addBox(-1.5F, -6.6F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(9, 26).addBox(-2.5F, -5.9F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(37, 14).addBox(-3.5F, -5F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(0, 14).addBox(-4.5F, -4.3F, -4.5F, 9F, 2F, 9F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(45, 0).addBox(-5F, -3F, -5F, 10F, 3F, 10F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(30, 26).addBox(-2F, -3.7F, -2F, 4F, 2F, 4F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_rim = p_bell.addOrReplaceChild("rim", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -0.3F, -5.5F, 11F, 2F, 11F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(0F, -0.4F, 0F));
        PartDefinition p_arm_0 = p_bell.addOrReplaceChild("arm_0", CubeListBuilder.create()
                .texOffs(66, 14).addBox(-1F, -0.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(38, 41).addBox(-1F, 0.8F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(1.2728F, 0.8F, 1.2728F));
        PartDefinition p_arm_0_low = p_arm_0.addOrReplaceChild("arm_0_low", CubeListBuilder.create()
                .texOffs(58, 34).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
                .texOffs(30, 34).addBox(-0.5F, 0.3F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_arm_1 = p_bell.addOrReplaceChild("arm_1", CubeListBuilder.create()
                .texOffs(75, 14).addBox(-1F, -0.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(45, 41).addBox(-1F, 0.8F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(-1.2728F, 0.8F, 1.2728F));
        PartDefinition p_arm_1_low = p_arm_1.addOrReplaceChild("arm_1_low", CubeListBuilder.create()
                .texOffs(63, 34).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
                .texOffs(37, 34).addBox(-0.5F, 0.3F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_arm_2 = p_bell.addOrReplaceChild("arm_2", CubeListBuilder.create()
                .texOffs(84, 14).addBox(-1F, -0.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(52, 41).addBox(-1F, 0.8F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(-1.2728F, 0.8F, -1.2728F));
        PartDefinition p_arm_2_low = p_arm_2.addOrReplaceChild("arm_2_low", CubeListBuilder.create()
                .texOffs(68, 34).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
                .texOffs(44, 34).addBox(-0.5F, 0.3F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_arm_3 = p_bell.addOrReplaceChild("arm_3", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-1F, -0.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(59, 41).addBox(-1F, 0.8F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(1.2728F, 0.8F, -1.2728F));
        PartDefinition p_arm_3_low = p_arm_3.addOrReplaceChild("arm_3_low", CubeListBuilder.create()
                .texOffs(73, 34).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F)
                .texOffs(51, 34).addBox(-0.5F, 0.3F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_0 = p_bell.addOrReplaceChild("tentacle_0", CubeListBuilder.create()
                .texOffs(47, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(4.4346F, 0.9F, 1.8369F));
        PartDefinition p_tentacle_0_mid = p_tentacle_0.addOrReplaceChild("tentacle_0_mid", CubeListBuilder.create()
                .texOffs(52, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_0_tip = p_tentacle_0_mid.addOrReplaceChild("tentacle_0_tip", CubeListBuilder.create()
                .texOffs(78, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_1 = p_bell.addOrReplaceChild("tentacle_1", CubeListBuilder.create()
                .texOffs(57, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(1.8369F, 0.9F, 4.4346F));
        PartDefinition p_tentacle_1_mid = p_tentacle_1.addOrReplaceChild("tentacle_1_mid", CubeListBuilder.create()
                .texOffs(62, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_1_tip = p_tentacle_1_mid.addOrReplaceChild("tentacle_1_tip", CubeListBuilder.create()
                .texOffs(83, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_2 = p_bell.addOrReplaceChild("tentacle_2", CubeListBuilder.create()
                .texOffs(67, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(-1.8369F, 0.9F, 4.4346F));
        PartDefinition p_tentacle_2_mid = p_tentacle_2.addOrReplaceChild("tentacle_2_mid", CubeListBuilder.create()
                .texOffs(72, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_2_tip = p_tentacle_2_mid.addOrReplaceChild("tentacle_2_tip", CubeListBuilder.create()
                .texOffs(88, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_3 = p_bell.addOrReplaceChild("tentacle_3", CubeListBuilder.create()
                .texOffs(77, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(-4.4346F, 0.9F, 1.8369F));
        PartDefinition p_tentacle_3_mid = p_tentacle_3.addOrReplaceChild("tentacle_3_mid", CubeListBuilder.create()
                .texOffs(82, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_3_tip = p_tentacle_3_mid.addOrReplaceChild("tentacle_3_tip", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_4 = p_bell.addOrReplaceChild("tentacle_4", CubeListBuilder.create()
                .texOffs(87, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(-4.4346F, 0.9F, -1.8369F));
        PartDefinition p_tentacle_4_mid = p_tentacle_4.addOrReplaceChild("tentacle_4_mid", CubeListBuilder.create()
                .texOffs(92, 26).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_4_tip = p_tentacle_4_mid.addOrReplaceChild("tentacle_4_tip", CubeListBuilder.create()
                .texOffs(5, 41).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_5 = p_bell.addOrReplaceChild("tentacle_5", CubeListBuilder.create()
                .texOffs(0, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(-1.8369F, 0.9F, -4.4346F));
        PartDefinition p_tentacle_5_mid = p_tentacle_5.addOrReplaceChild("tentacle_5_mid", CubeListBuilder.create()
                .texOffs(5, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_5_tip = p_tentacle_5_mid.addOrReplaceChild("tentacle_5_tip", CubeListBuilder.create()
                .texOffs(10, 41).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_6 = p_bell.addOrReplaceChild("tentacle_6", CubeListBuilder.create()
                .texOffs(10, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(1.8369F, 0.9F, -4.4346F));
        PartDefinition p_tentacle_6_mid = p_tentacle_6.addOrReplaceChild("tentacle_6_mid", CubeListBuilder.create()
                .texOffs(15, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_6_tip = p_tentacle_6_mid.addOrReplaceChild("tentacle_6_tip", CubeListBuilder.create()
                .texOffs(15, 41).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_7 = p_bell.addOrReplaceChild("tentacle_7", CubeListBuilder.create()
                .texOffs(20, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(4.4346F, 0.9F, -1.8369F));
        PartDefinition p_tentacle_7_mid = p_tentacle_7.addOrReplaceChild("tentacle_7_mid", CubeListBuilder.create()
                .texOffs(25, 34).addBox(-0.5F, -0.25F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 4.5F, 0F));
        PartDefinition p_tentacle_7_tip = p_tentacle_7_mid.addOrReplaceChild("tentacle_7_tip", CubeListBuilder.create()
                .texOffs(20, 41).addBox(-0.5F, -0.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4.5F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
