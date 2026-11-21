package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Glutschleims (acht Scheiben und Kern wie beim Vanilla-Magmawürfel, dazu Schollen und Tropfen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlutschleimGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private GlutschleimGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_cube0 = root.addOrReplaceChild("cube0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 16F, -4F, 8F, 1F, 8F)
                .texOffs(83, 20).addBox(-2.5F, 15.25F, -2F, 4F, 1F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube1 = root.addOrReplaceChild("cube1", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-4F, 17F, -4F, 8F, 1F, 8F)
                .texOffs(98, 20).addBox(-4.75F, 17F, -2F, 1F, 1F, 3F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube2 = root.addOrReplaceChild("cube2", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-4F, 18F, -4F, 8F, 1F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube3 = root.addOrReplaceChild("cube3", CubeListBuilder.create()
                .texOffs(0, 10).addBox(-4F, 19F, -4F, 8F, 1F, 8F)
                .texOffs(107, 20).addBox(3.75F, 19F, 0F, 1F, 1F, 3F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube4 = root.addOrReplaceChild("cube4", CubeListBuilder.create()
                .texOffs(33, 10).addBox(-4F, 20F, -4F, 8F, 1F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube5 = root.addOrReplaceChild("cube5", CubeListBuilder.create()
                .texOffs(66, 10).addBox(-4F, 21F, -4F, 8F, 1F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube6 = root.addOrReplaceChild("cube6", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-4F, 22F, -4F, 8F, 1F, 8F)
                .texOffs(0, 30).addBox(-1F, 22F, 3.75F, 3F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cube7 = root.addOrReplaceChild("cube7", CubeListBuilder.create()
                .texOffs(33, 20).addBox(-4F, 23F, -4F, 8F, 1F, 8F)
                .texOffs(116, 20).addBox(-1.5F, 23.75F, -1.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(9, 30).addBox(2F, 24F, 1F, 1F, 1F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_inside_cube = root.addOrReplaceChild("inside_cube", CubeListBuilder.create()
                .texOffs(66, 20).addBox(-2F, 18F, -2F, 4F, 4F, 4F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
