package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der jungen Lavastelzer (Knochen wie beim Vanilla-Schreiter-Jungtier).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class LavastelzerBabyGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private LavastelzerBabyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -3.75F, -4F, 7F, 7F, 8F),
                PartPose.offset(0F, 16.75F, 0F));
        PartDefinition p_bristle0 = p_body.addOrReplaceChild("bristle0", CubeListBuilder.create()
                .texOffs(13, 16).addBox(-3.5F, -2.5F, 0F, 7F, 3F, 0F),
                PartPose.offset(0F, -4.25F, 2F));
        PartDefinition p_bristle1 = p_body.addOrReplaceChild("bristle1", CubeListBuilder.create()
                .texOffs(28, 16).addBox(-3.5F, -2.5F, 0F, 7F, 3F, 0F),
                PartPose.offset(0F, -4.25F, 0F));
        PartDefinition p_bristle2 = p_body.addOrReplaceChild("bristle2", CubeListBuilder.create()
                .texOffs(43, 16).addBox(-3.5F, -2.5F, 0F, 7F, 3F, 0F),
                PartPose.offset(0F, -4.25F, -2F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(31, 0).addBox(-1F, 0F, -1F, 2F, 4F, 2F)
                .texOffs(49, 0).addBox(-1.5F, 3.1F, -1.6F, 3F, 1F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(-1.5F, 20F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(40, 0).addBox(-1F, 0F, -1F, 2F, 4F, 2F)
                .texOffs(0, 16).addBox(-1.5F, 3.1F, -1.6F, 3F, 1F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(1.5F, 20F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
