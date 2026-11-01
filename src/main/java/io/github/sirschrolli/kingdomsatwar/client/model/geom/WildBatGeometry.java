package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Fledermäuse.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildBatGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private WildBatGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(26, 0).addBox(-1.5F, 0F, -1F, 3F, 5F, 2F)
                .texOffs(47, 0).addBox(-1.8F, 0.2F, -1.3F, 3.6F, 2.4F, 2.6F),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-2F, -2F, 0F, 2F, 7F, 0F),
                PartPose.offset(-1.5F, 0F, 0F));
        PartDefinition p_right_wing_tip = p_right_wing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -2F, 0F, 6F, 8F, 0F),
                PartPose.offset(-2F, 0F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(42, 0).addBox(0F, -2F, 0F, 2F, 7F, 0F),
                PartPose.offset(1.5F, 0F, 0F));
        PartDefinition p_left_wing_tip = p_left_wing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(13, 0).addBox(0F, -2F, 0F, 6F, 8F, 0F),
                PartPose.offset(2F, 0F, 0F));
        PartDefinition p_feet = p_body.addOrReplaceChild("feet", CubeListBuilder.create()
                .texOffs(34, 9).addBox(-1.5F, 0F, 0F, 3F, 2F, 0F),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 9).addBox(-2F, -3F, -1F, 4F, 3F, 2F)
                .texOffs(27, 9).addBox(-0.75F, -1.5F, -1.6F, 1.5F, 1.2F, 0.6F),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(13, 9).addBox(-2.5F, -4F, 0F, 3F, 5F, 0F),
                PartPose.offset(-1.5F, -2F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(20, 9).addBox(-0.1F, -3F, 0F, 3F, 5F, 0F),
                PartPose.offset(1.1F, -3F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
