package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Nachtschwinge.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class NightWingGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private NightWingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -2F, -6F, 6F, 4F, 11F)
                .texOffs(0, 16).addBox(-2F, -3F, -5F, 4F, 1F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(25, 16).addBox(-2.5F, -2F, -4.5F, 5F, 4F, 5F)
                .texOffs(91, 26).addBox(-1.5F, -0.5F, -7.5F, 3F, 2F, 3F)
                .texOffs(104, 26).addBox(-2F, -1F, -4.8F, 1F, 1F, 0.5F)
                .texOffs(109, 26).addBox(1F, -1F, -4.8F, 1F, 1F, 0.5F)
                .texOffs(114, 26).addBox(-1.2F, 1.5F, -7.2F, 0.5F, 1F, 0.5F)
                .texOffs(119, 26).addBox(0.7F, 1.5F, -7.2F, 0.5F, 1F, 0.5F),
                PartPose.offsetAndRotation(0F, -0.5F, -6F, 0.1396F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(56, 26).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 2F),
                PartPose.offsetAndRotation(1.8F, -2F, -2F, -0.3491F, 0F, 0.3142F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(63, 26).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 2F),
                PartPose.offsetAndRotation(-1.8F, -2F, -2F, -0.3491F, 0F, -0.3142F));
        PartDefinition p_left_wing_base = p_body.addOrReplaceChild("left_wing_base", CubeListBuilder.create()
                .texOffs(35, 0).addBox(0F, 0F, 0F, 11F, 1F, 9F),
                PartPose.offset(3F, -1.5F, -4F));
        PartDefinition p_left_wing_tip = p_left_wing_base.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(67, 16).addBox(0F, 0F, 0F, 11F, 1F, 7F),
                PartPose.offset(11F, 0F, 0F));
        PartDefinition p_right_wing_base = p_body.addOrReplaceChild("right_wing_base", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-11F, 0F, 0F, 11F, 1F, 9F),
                PartPose.offset(-3F, -1.5F, -4F));
        PartDefinition p_right_wing_tip = p_right_wing_base.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-11F, 0F, 0F, 11F, 1F, 7F),
                PartPose.offset(-11F, 0F, 0F));
        PartDefinition p_tail_base = p_body.addOrReplaceChild("tail_base", CubeListBuilder.create()
                .texOffs(46, 16).addBox(-1.5F, -1F, 0F, 3F, 2F, 7F),
                PartPose.offset(0F, -0.5F, 5F));
        PartDefinition p_tail_tip = p_tail_base.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(37, 26).addBox(-1F, -0.5F, 0F, 2F, 1F, 7F)
                .texOffs(70, 26).addBox(-3F, 0F, 4F, 6F, 0.5F, 4F),
                PartPose.offset(0F, 0F, 7F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
