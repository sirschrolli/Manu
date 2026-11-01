package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Lamas (Skelett wie bei Vanilla, damit Teppich und Kisten passen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildLlamaGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private WildLlamaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(51, 29).addBox(-2F, -14F, -10F, 4F, 4F, 9F)
                .texOffs(45, 0).addBox(-4F, -16F, -6F, 8F, 18F, 6F)
                .texOffs(74, 0).addBox(-4.4F, -9F, -6.4F, 8.8F, 11F, 6.8F),
                PartPose.offset(0F, 7F, -6F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(97, 48).addBox(-1.5F, -3.5F, -1F, 3F, 3.5F, 2F),
                PartPose.offsetAndRotation(2.5F, -16F, -3F, -0.1047F, 0F, 0.1745F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(108, 48).addBox(-1.5F, -3.5F, -1F, 3F, 3.5F, 2F),
                PartPose.offsetAndRotation(-2.5F, -16F, -3F, -0.1047F, 0F, -0.1745F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -10F, -7F, 12F, 18F, 10F)
                .texOffs(84, 48).addBox(-1.5F, 7.5F, -6F, 3F, 2.5F, 3F),
                PartPose.offsetAndRotation(0F, 5F, 2F, 1.5708F, 0F, 0F));
        PartDefinition p_right_chest = root.addOrReplaceChild("right_chest", CubeListBuilder.create()
                .texOffs(78, 29).addBox(-3F, 0F, 0F, 8F, 8F, 3F),
                PartPose.offsetAndRotation(-8.5F, 3F, 3F, 0F, 1.5708F, 0F));
        PartDefinition p_left_chest = root.addOrReplaceChild("left_chest", CubeListBuilder.create()
                .texOffs(101, 29).addBox(-3F, 0F, 0F, 8F, 8F, 3F),
                PartPose.offsetAndRotation(5.5F, 3F, 3F, 0F, 1.5708F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(107, 0).addBox(-2F, 0F, -2F, 4F, 14F, 4F)
                .texOffs(0, 48).addBox(-2.4F, -0.4F, -2.4F, 4.8F, 6F, 4.8F),
                PartPose.offset(-3.5F, 10F, 6F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-2F, 0F, -2F, 4F, 14F, 4F)
                .texOffs(21, 48).addBox(-2.4F, -0.4F, -2.4F, 4.8F, 6F, 4.8F),
                PartPose.offset(3.5F, 10F, 6F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(17, 29).addBox(-2F, 0F, -2F, 4F, 14F, 4F)
                .texOffs(42, 48).addBox(-2.4F, -0.4F, -2.4F, 4.8F, 6F, 4.8F),
                PartPose.offset(-3.5F, 10F, -5F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(34, 29).addBox(-2F, 0F, -2F, 4F, 14F, 4F)
                .texOffs(63, 48).addBox(-2.4F, -0.4F, -2.4F, 4.8F, 6F, 4.8F),
                PartPose.offset(3.5F, 10F, -5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
