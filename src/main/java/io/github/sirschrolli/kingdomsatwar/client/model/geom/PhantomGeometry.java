package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Nachtmahre (Knochen wie beim Vanilla-Phantom).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PhantomGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private PhantomGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -2F, -8F, 5F, 3F, 9F),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1F, 0F, 0F));
        PartDefinition p_crest = p_body.addOrReplaceChild("crest", CubeListBuilder.create()
                .texOffs(45, 36).addBox(-0.5F, -4F, -7F, 0F, 2F, 7F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail_base = p_body.addOrReplaceChild("tail_base", CubeListBuilder.create()
                .texOffs(25, 47).addBox(-2F, 0F, 0F, 3F, 2F, 6F),
                PartPose.offset(0F, -2F, 1F));
        PartDefinition p_tail_tip = p_tail_base.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(44, 47).addBox(-1F, 0F, 0F, 1F, 1F, 6F),
                PartPose.offset(0F, 0.5F, 6F));
        PartDefinition p_tail_fin = p_tail_tip.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(22, 56).addBox(-2.5F, 0F, 0F, 5F, 0F, 4F),
                PartPose.offset(-0.5F, 0.5F, 4F));
        PartDefinition p_left_wing_base = p_body.addOrReplaceChild("left_wing_base", CubeListBuilder.create()
                .texOffs(29, 0).addBox(0F, 0F, 0F, 6F, 2F, 9F),
                PartPose.offsetAndRotation(2F, -2F, -8F, 0F, 0F, 0.1F));
        PartDefinition p_left_wing_tip = p_left_wing_base.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(0, 25).addBox(0F, 0F, 0F, 13F, 1F, 9F),
                PartPose.offsetAndRotation(6F, 0F, 0F, 0F, 0F, 0.1F));
        PartDefinition p_right_wing_base = p_body.addOrReplaceChild("right_wing_base", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-6F, 0F, 0F, 6F, 2F, 9F),
                PartPose.offsetAndRotation(-3F, -2F, -8F, 0F, 0F, -0.1F));
        PartDefinition p_right_wing_tip = p_right_wing_base.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-13F, 0F, 0F, 13F, 1F, 9F),
                PartPose.offsetAndRotation(-6F, 0F, 0F, 0F, 0F, -0.1F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-4F, -2F, -5F, 7F, 3F, 5F),
                PartPose.offsetAndRotation(0F, 1F, -7F, 0.2F, 0F, 0F));
        PartDefinition p_horns = p_head.addOrReplaceChild("horns", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-2.5F, -1F, 0F, 1F, 1F, 4F)
                .texOffs(11, 56).addBox(1.5F, -1F, 0F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(-0.5F, -2F, -1.5F, 0.4887F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
