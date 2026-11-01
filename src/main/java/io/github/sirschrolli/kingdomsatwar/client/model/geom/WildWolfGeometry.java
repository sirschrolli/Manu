package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Wölfe (Skelett wie beim Vanilla-Wolf, Halsband als eigener Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildWolfGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private WildWolfGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offset(-1F, 13.5F, -7F));
        PartDefinition p_real_head = p_head.addOrReplaceChild("real_head", CubeListBuilder.create()
                .texOffs(35, 28).addBox(-2F, -3F, -2F, 6F, 6F, 4F)
                .texOffs(0, 51).addBox(-0.5F, -0.001F, -5F, 3F, 3F, 4F)
                .texOffs(40, 40).addBox(-2.6F, -0.5F, -1.6F, 0.6F, 3.5F, 3.2F)
                .texOffs(51, 40).addBox(4F, -0.5F, -1.6F, 0.6F, 3.5F, 3.2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ear_r = p_real_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(28, 51).addBox(-1F, -3F, -0.5F, 2F, 3F, 1F),
                PartPose.offsetAndRotation(-1F, -3F, 0.5F, -0.1396F, 0F, -0.1047F));
        PartDefinition p_ear_l = p_real_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(35, 51).addBox(-1F, -3F, -0.5F, 2F, 3F, 1F),
                PartPose.offsetAndRotation(3F, -3F, 0.5F, -0.1396F, 0F, 0.1047F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -2F, -3F, 6F, 9F, 6F),
                PartPose.offsetAndRotation(0F, 14F, 2F, 1.5708F, 0F, 0F));
        PartDefinition p_upper_body = root.addOrReplaceChild("upper_body", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-3F, -3F, -3F, 8F, 6F, 7F)
                .texOffs(0, 28).addBox(-3.4F, -3.5F, -3.4F, 8.8F, 2.4F, 7.8F),
                PartPose.offsetAndRotation(-1F, 14F, -3F, 1.5708F, 0F, 0F));
        PartDefinition p_collar = p_upper_body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-3.6F, -2.4F, -3.6F, 9.2F, 1.2F, 8.2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(56, 28).addBox(0F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-2.5F, 16F, 7F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 40).addBox(0F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(0.5F, 16F, 7F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(9, 40).addBox(0F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-2.5F, 16F, -4F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(18, 40).addBox(0F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(0.5F, 16F, -4F));
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-1F, 12F, 8F, 0.6283F, 0F, 0F));
        PartDefinition p_real_tail = p_tail.addOrReplaceChild("real_tail", CubeListBuilder.create()
                .texOffs(27, 40).addBox(-0.25F, 0F, -1.25F, 2.5F, 6F, 2.5F)
                .texOffs(15, 51).addBox(-0.5F, 5.5F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
