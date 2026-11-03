package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Silberfischchen (Vanilla-Segmente mit Beinen und Fühlern).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SilverfishGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private SilverfishGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_segment0 = root.addOrReplaceChild("segment0", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1.5F, 0F, -1F, 3F, 2F, 2F),
                PartPose.offset(0F, 22F, -3.5F));
        PartDefinition p_antenna_right = p_segment0.addOrReplaceChild("antenna_right", CubeListBuilder.create()
                .texOffs(19, 0).addBox(-0.5F, -0.5F, -5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-0.8F, 0.3F, -1F, -0.3142F, 0.4887F, 0F));
        PartDefinition p_antenna_left = p_segment0.addOrReplaceChild("antenna_left", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-0.5F, -0.5F, -5F, 1F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0.8F, 0.3F, -1F, -0.3142F, -0.4887F, 0F));
        PartDefinition p_segment1 = root.addOrReplaceChild("segment1", CubeListBuilder.create()
                .texOffs(0, 8).addBox(-2F, 0F, -1F, 4F, 3F, 2F),
                PartPose.offset(0F, 21F, -1.5F));
        PartDefinition p_segment2 = root.addOrReplaceChild("segment2", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, 0F, -1.5F, 6F, 4F, 3F),
                PartPose.offset(0F, 20F, 1F));
        PartDefinition p_segment3 = root.addOrReplaceChild("segment3", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-1.5F, 0F, -1.5F, 3F, 3F, 3F),
                PartPose.offset(0F, 21F, 4F));
        PartDefinition p_segment4 = root.addOrReplaceChild("segment4", CubeListBuilder.create()
                .texOffs(13, 8).addBox(-1F, 0F, -1.5F, 2F, 2F, 3F),
                PartPose.offset(0F, 22F, 7F));
        PartDefinition p_segment5 = root.addOrReplaceChild("segment5", CubeListBuilder.create()
                .texOffs(11, 14).addBox(-1F, 0F, -1F, 2F, 1F, 2F),
                PartPose.offset(0F, 23F, 9.5F));
        PartDefinition p_segment6 = root.addOrReplaceChild("segment6", CubeListBuilder.create()
                .texOffs(20, 14).addBox(-0.5F, 0F, -1F, 1F, 1F, 2F),
                PartPose.offset(0F, 23F, 11.5F));
        PartDefinition p_cercus_right = p_segment6.addOrReplaceChild("cercus_right", CubeListBuilder.create()
                .texOffs(24, 8).addBox(-0.5F, -0.5F, -0.25F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0.4F, 1F, 0.1396F, -0.5236F, 0F));
        PartDefinition p_cercus_middle = p_segment6.addOrReplaceChild("cercus_middle", CubeListBuilder.create()
                .texOffs(35, 8).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 0.4F, 1F, 0.1396F, 0F, 0F));
        PartDefinition p_cercus_left = p_segment6.addOrReplaceChild("cercus_left", CubeListBuilder.create()
                .texOffs(46, 8).addBox(-0.5F, -0.5F, -0.25F, 1F, 1F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0.4F, 1F, 0.1396F, 0.5236F, 0F));
        PartDefinition p_layer0 = root.addOrReplaceChild("layer0", CubeListBuilder.create()
                .texOffs(27, 14).addBox(-5F, 7F, -1.3F, 10F, 1F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(0, 19).addBox(-4.5F, 7F, 0.4F, 9F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.2F)),
                PartPose.offset(0F, 16F, 1F));
        PartDefinition p_layer1 = root.addOrReplaceChild("layer1", CubeListBuilder.create()
                .texOffs(40, 19).addBox(-3F, 3F, -0.5F, 6F, 1F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 20F, 7F));
        PartDefinition p_layer2 = root.addOrReplaceChild("layer2", CubeListBuilder.create()
                .texOffs(21, 19).addBox(-4F, 4F, -0.5F, 8F, 1F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(0F, 19F, -1.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
