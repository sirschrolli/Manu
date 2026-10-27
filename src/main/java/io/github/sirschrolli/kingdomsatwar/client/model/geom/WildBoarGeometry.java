package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Wildschweins.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildBoarGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private WildBoarGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -5F, -9F, 9F, 9F, 17F)
                .texOffs(39, 27).addBox(-3.5F, -6.5F, -9F, 7F, 2F, 8F)
                .texOffs(53, 0).addBox(0F, -10F, -9.5F, 0F, 4F, 13F),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-3.5F, -3.5F, -6.5F, 7F, 7F, 7F)
                .texOffs(70, 27).addBox(-2F, 0F, -9.5F, 4F, 3F, 3F)
                .texOffs(104, 27).addBox(-2.6F, 0.2F, -9F, 1F, 2.5F, 1F)
                .texOffs(109, 27).addBox(1.6F, 0.2F, -9F, 1F, 2.5F, 1F)
                .texOffs(114, 27).addBox(3.4F, -1.5F, -5.5F, 0.5F, 1F, 1F)
                .texOffs(119, 27).addBox(-3.9F, -1.5F, -5.5F, 0.5F, 1F, 1F),
                PartPose.offsetAndRotation(0F, -1F, -9F, 0.2094F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(90, 27).addBox(-1F, -3F, -0.5F, 2F, 3F, 1F),
                PartPose.offsetAndRotation(2.6F, -3.2F, -2F, -0.2618F, 0F, 0.4363F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(97, 27).addBox(-1F, -3F, -0.5F, 2F, 3F, 1F),
                PartPose.offsetAndRotation(-2.6F, -3.2F, -2F, -0.2618F, 0F, -0.4363F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(85, 27).addBox(-0.5F, 0F, 0F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(0F, -3.5F, 8F, 0.6109F, 0F, 0F));
        PartDefinition p_front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(109, 0).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(2.5F, 16F, -6F));
        PartDefinition p_front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(-2.5F, 16F, -6F));
        PartDefinition p_hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(13, 27).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(2.5F, 16F, 5.5F));
        PartDefinition p_hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(26, 27).addBox(-1.5F, 0F, -1.5F, 3F, 8F, 3F),
                PartPose.offset(-2.5F, 16F, 5.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
