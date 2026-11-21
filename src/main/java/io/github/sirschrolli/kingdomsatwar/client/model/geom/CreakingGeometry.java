package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Knorz (Knochen wie beim Vanilla-Knarz, Geweih aus Ästen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CreakingGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private CreakingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_upper_body = p_root.addOrReplaceChild("upper_body", CubeListBuilder.create(),
                PartPose.offset(-1F, -19F, 0F));
        PartDefinition p_head = p_upper_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(75, 0).addBox(-3.5F, -10F, -3F, 7F, 10F, 6F)
                .texOffs(67, 52).addBox(-3.5F, -7F, -4F, 7F, 2F, 1F)
                .texOffs(10, 52).addBox(-3F, -3F, -4F, 6F, 3F, 1F)
                .texOffs(78, 25).addBox(-3.5F, -13F, -3F, 7F, 3F, 6F),
                PartPose.offset(-3F, -11F, 0F));
        PartDefinition p_antler_r = p_head.addOrReplaceChild("antler_r", CubeListBuilder.create()
                .texOffs(105, 25).addBox(-0.5F, -8F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(-2.6F, -12F, 0.5F, -0.1396F, 0F, -0.6632F));
        PartDefinition p_antler_r_tine = p_antler_r.addOrReplaceChild("antler_r_tine", CubeListBuilder.create()
                .texOffs(48, 45).addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(0F, -3.5F, 0F, 0.2443F, 0F, 0.733F));
        PartDefinition p_antler_r_tip = p_antler_r.addOrReplaceChild("antler_r_tip", CubeListBuilder.create()
                .texOffs(107, 45).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(0F, -7.5F, 0F, -0.3142F, 0F, -0.5236F));
        PartDefinition p_antler_r_spur = p_antler_r.addOrReplaceChild("antler_r_spur", CubeListBuilder.create()
                .texOffs(47, 52).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -5.5F, 0F, 0F, 0F, -1.0821F));
        PartDefinition p_antler_l = p_head.addOrReplaceChild("antler_l", CubeListBuilder.create()
                .texOffs(110, 25).addBox(-0.5F, -8F, -0.5F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(2.6F, -12F, 0.5F, -0.1396F, 0F, 0.6632F));
        PartDefinition p_antler_l_tine = p_antler_l.addOrReplaceChild("antler_l_tine", CubeListBuilder.create()
                .texOffs(53, 45).addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F),
                PartPose.offsetAndRotation(0F, -3.5F, 0F, 0.2443F, 0F, -0.733F));
        PartDefinition p_antler_l_tip = p_antler_l.addOrReplaceChild("antler_l_tip", CubeListBuilder.create()
                .texOffs(112, 45).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(0F, -7.5F, 0F, -0.3142F, 0F, 0.5236F));
        PartDefinition p_antler_l_spur = p_antler_l.addOrReplaceChild("antler_l_spur", CubeListBuilder.create()
                .texOffs(52, 52).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -5.5F, 0F, 0F, 0F, 1.0821F));
        PartDefinition p_body = p_upper_body.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(52, 0).addBox(0F, -3F, -3F, 6F, 13F, 5F)
                .texOffs(102, 0).addBox(-6F, -4F, -3F, 6F, 7F, 5F)
                .texOffs(68, 45).addBox(-5.5F, -5F, -2.5F, 4F, 1F, 4F)
                .texOffs(102, 37).addBox(1F, 9.75F, -2.5F, 4F, 2F, 4F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, -7F, 1F));
        PartDefinition p_right_arm = p_upper_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2F, -1.5F, -1.5F, 3F, 21F, 3F)
                .texOffs(0, 37).addBox(-2.5F, 7.5F, -2F, 4F, 3F, 4F)
                .texOffs(17, 37).addBox(-2.5F, 19.5F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(117, 45).addBox(-2.2F, 22.5F, -1.8F, 1F, 4F, 1F)
                .texOffs(58, 45).addBox(-0.6F, 22.5F, -1.8F, 1F, 5F, 1F)
                .texOffs(122, 45).addBox(0.3F, 22.25F, 0.2F, 1F, 4F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(57, 52).addBox(-1.6F, 22.5F, 0.8F, 1F, 3F, 1F),
                PartPose.offset(-7F, -9.5F, 1.5F));
        PartDefinition p_left_arm = p_upper_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(26, 0).addBox(0F, -1F, -1.5F, 3F, 16F, 3F)
                .texOffs(39, 45).addBox(0.5F, -5F, -1F, 2F, 4F, 2F)
                .texOffs(34, 37).addBox(-0.5F, 5.5F, -2F, 4F, 3F, 4F)
                .texOffs(51, 37).addBox(-0.5F, 15F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(0, 52).addBox(2.2F, 18F, -1.8F, 1F, 4F, 1F)
                .texOffs(63, 45).addBox(0.6F, 18F, -1.8F, 1F, 5F, 1F)
                .texOffs(5, 52).addBox(-0.3F, 17.75F, 0.2F, 1F, 4F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(62, 52).addBox(1.6F, 18F, 0.8F, 1F, 3F, 1F),
                PartPose.offset(6F, -9F, 0.5F));
        PartDefinition p_left_leg = p_root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(39, 0).addBox(-1.5F, 0F, -1.5F, 3F, 16F, 3F)
                .texOffs(68, 37).addBox(-2F, 11.5F, -2F, 4F, 3F, 4F)
                .texOffs(0, 45).addBox(-1F, 14.25F, -5.25F, 2F, 2F, 4F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(25, 52).addBox(1.5F, 14.4F, -1F, 3F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(85, 45).addBox(-1.2F, 14.4F, 1.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(0, 25).addBox(-2.5F, 15.7F, -5.5F, 8F, 0F, 11F),
                PartPose.offset(1.5F, -16F, 0.5F));
        PartDefinition p_right_leg = p_root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(13, 0).addBox(-3F, -1.5F, -1.5F, 3F, 19F, 3F)
                .texOffs(13, 45).addBox(-3F, -4.5F, -1.5F, 3F, 3F, 3F)
                .texOffs(85, 37).addBox(-3.5F, 13F, -2F, 4F, 3F, 4F)
                .texOffs(26, 45).addBox(-2.5F, 15.75F, -5.25F, 2F, 2F, 4F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(36, 52).addBox(-6F, 15.9F, -1F, 3F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(96, 45).addBox(-2.8F, 15.9F, 1.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(39, 25).addBox(-7F, 17.2F, -5.5F, 8F, 0F, 11F),
                PartPose.offset(-1F, -17.5F, 0.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
