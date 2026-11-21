package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Honigbiene und Hummel.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildBeeGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private WildBeeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bone = root.addOrReplaceChild("bone", CubeListBuilder.create(),
                PartPose.offset(0F, 19F, 0F));
        PartDefinition p_body = p_bone.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -4F, -5F, 7F, 7F, 10F)
                .texOffs(35, 0).addBox(-4F, -4.3F, -5.6F, 8F, 4F, 4F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_stinger = p_body.addOrReplaceChild("stinger", CubeListBuilder.create()
                .texOffs(18, 25).addBox(0F, -1F, 5F, 0F, 1F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_antenna = p_body.addOrReplaceChild("left_antenna", CubeListBuilder.create()
                .texOffs(0, 25).addBox(1.5F, -2F, -3F, 1F, 2F, 3F),
                PartPose.offset(0F, -2F, -5F));
        PartDefinition p_right_antenna = p_body.addOrReplaceChild("right_antenna", CubeListBuilder.create()
                .texOffs(9, 25).addBox(-2.5F, -2F, -3F, 1F, 2F, 3F),
                PartPose.offset(0F, -2F, -5F));
        PartDefinition p_right_wing = p_bone.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-9F, 0F, 0F, 9F, 0F, 6F),
                PartPose.offsetAndRotation(-1.5F, -4F, -3F, 0F, -0.2618F, 0F));
        PartDefinition p_left_wing = p_bone.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(31, 18).addBox(0F, 0F, 0F, 9F, 0F, 6F),
                PartPose.offsetAndRotation(1.5F, -4F, -3F, 0F, 0.2618F, 0F));
        PartDefinition p_front_legs = p_bone.addOrReplaceChild("front_legs", CubeListBuilder.create()
                .texOffs(23, 25).addBox(-5F, 0F, 0F, 7F, 2F, 0F),
                PartPose.offset(1.5F, 3F, -2F));
        PartDefinition p_middle_legs = p_bone.addOrReplaceChild("middle_legs", CubeListBuilder.create()
                .texOffs(38, 25).addBox(-5F, 0F, 0F, 7F, 2F, 0F),
                PartPose.offset(1.5F, 3F, 0F));
        PartDefinition p_back_legs = p_bone.addOrReplaceChild("back_legs", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-5F, 0F, 0F, 7F, 2F, 0F),
                PartPose.offset(1.5F, 3F, 2F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
