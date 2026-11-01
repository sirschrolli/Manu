package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Hausschweins (mit Sattel).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FarmPigGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private FarmPigGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -4.5F, -8F, 10F, 9F, 16F)
                .texOffs(84, 0).addBox(-4F, -5.5F, -6.5F, 8F, 1F, 12F),
                PartPose.offset(0F, 13F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(53, 0).addBox(-4F, -4F, -6.5F, 8F, 7.5F, 6.5F)
                .texOffs(111, 26).addBox(-2.5F, -0.5F, -9.5F, 5F, 3.5F, 3F)
                .texOffs(92, 37).addBox(4F, -2F, -5.5F, 0.5F, 1F, 1F)
                .texOffs(97, 37).addBox(-4.5F, -2F, -5.5F, 0.5F, 1F, 1F),
                PartPose.offset(0F, -1F, -8F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(25, 37).addBox(-1.75F, -0.5F, -4.5F, 3.5F, 1F, 4.5F),
                PartPose.offsetAndRotation(2.4F, -4F, -2.5F, 0.6632F, -0.2094F, 0.384F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(44, 37).addBox(-1.75F, -0.5F, -4.5F, 3.5F, 1F, 4.5F),
                PartPose.offsetAndRotation(-2.4F, -4F, -2.5F, 0.6632F, 0.2094F, -0.384F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(63, 37).addBox(-0.5F, -1.5F, 0F, 1F, 1.5F, 1F)
                .texOffs(102, 37).addBox(-0.5F, -2.5F, 0.8F, 1F, 1F, 1F)
                .texOffs(107, 37).addBox(0.3F, -2F, 1.4F, 1F, 1F, 1F),
                PartPose.offsetAndRotation(0F, -3F, 8F, -0.5236F, 0F, 0F));
        PartDefinition p_saddle = p_body.addOrReplaceChild("saddle", CubeListBuilder.create()
                .texOffs(78, 26).addBox(-4.5F, -0.6F, -3.5F, 9F, 1F, 7F)
                .texOffs(0, 37).addBox(-3.5F, -1.6F, -2.5F, 7F, 1F, 5F)
                .texOffs(81, 37).addBox(-2F, -2.6F, -3F, 4F, 1F, 1F)
                .texOffs(68, 37).addBox(-2.5F, -2.4F, 2F, 5F, 1F, 1F)
                .texOffs(0, 26).addBox(4.6F, 0.4F, -2.5F, 0.5F, 4.5F, 5F)
                .texOffs(112, 37).addBox(4.9F, 4.5F, -0.5F, 0.5F, 1F, 1F)
                .texOffs(13, 26).addBox(-5.1F, 0.4F, -2.5F, 0.5F, 4.5F, 5F)
                .texOffs(117, 37).addBox(-5.4F, 4.5F, -0.5F, 0.5F, 1F, 1F),
                PartPose.offset(0F, -5.5F, 0F));
        PartDefinition p_front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(26, 26).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F),
                PartPose.offset(2.8F, 17.5F, -5.5F));
        PartDefinition p_front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(39, 26).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F),
                PartPose.offset(-2.8F, 17.5F, -5.5F));
        PartDefinition p_hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(52, 26).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F),
                PartPose.offset(2.8F, 17.5F, 5.5F));
        PartDefinition p_hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(65, 26).addBox(-1.5F, 0F, -1.5F, 3F, 6.5F, 3F),
                PartPose.offset(-2.8F, 17.5F, 5.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
