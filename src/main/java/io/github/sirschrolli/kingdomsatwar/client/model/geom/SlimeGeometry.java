package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Schleime (Kern mit Gesicht und durchscheinende Hülle als eigener Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SlimeGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private SlimeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_cube = root.addOrReplaceChild("cube", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-3F, 17F, -3F, 6F, 6F, 6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_eye = root.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-3.25F, 18F, -3.5F, 2F, 2F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_eye = root.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(9, 17).addBox(1.25F, 18F, -3.5F, 2F, 2F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mouth = root.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(18, 17).addBox(0F, 21F, -3.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_outer = root.addOrReplaceChild("outer", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 16F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
