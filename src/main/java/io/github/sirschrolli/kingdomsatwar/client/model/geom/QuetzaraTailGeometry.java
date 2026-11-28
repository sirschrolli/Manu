package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Quetzaras Schwanz mit dem Federbusch (1.10).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class QuetzaraTailGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private QuetzaraTailGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(35, 0).addBox(-4F, -4F, -4F, 8F, 8F, 12F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(77, 40).addBox(-1F, -4.4F, -3.5F, 2F, 1F, 11F, new CubeDeformation(-0.2F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail2 = p_tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-3F, -3F, 0F, 6F, 6F, 10F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 8F, 0.0524F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(104, 40).addBox(-2F, -2F, 0F, 4F, 4F, 8F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(0, 57).addBox(-2.5F, -2.5F, 5.8F, 5F, 5F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 10F, 0.0698F, 0F, 0F));
        PartDefinition p_plume = p_tail3.addOrReplaceChild("plume", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_streamer_0 = p_plume.addOrReplaceChild("streamer_0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -38F, -0.5F, 5F, 38F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -1.5184F, 0F, 0F));
        PartDefinition p_streamer_1 = p_plume.addOrReplaceChild("streamer_1", CubeListBuilder.create()
                .texOffs(13, 0).addBox(-2F, -33F, -0.5F, 4F, 33F, 1F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.2F, 0F, -1.5708F, -0.1396F, 0F));
        PartDefinition p_streamer_2 = p_plume.addOrReplaceChild("streamer_2", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-2F, -33F, -0.5F, 4F, 33F, 1F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.4F, 0F, -1.5708F, 0.1396F, 0F));
        PartDefinition p_fan_0 = p_plume.addOrReplaceChild("fan_0", CubeListBuilder.create()
                .texOffs(55, 40).addBox(-2F, -11.98F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.4F, -0.02F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.5F, 0F, -1.3963F, -0.9774F, 0F));
        PartDefinition p_fan_1 = p_plume.addOrReplaceChild("fan_1", CubeListBuilder.create()
                .texOffs(33, 40).addBox(-2F, -13.79F, -0.5F, 4F, 14F, 1F, new CubeDeformation(-0.4F, -0.21F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.5F, 0F, -1.3963F, -0.6632F, 0F));
        PartDefinition p_fan_2 = p_plume.addOrReplaceChild("fan_2", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-2F, -15.6F, -0.5F, 4F, 16F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.5F, 0F, -1.3963F, -0.3491F, 0F));
        PartDefinition p_fan_3 = p_plume.addOrReplaceChild("fan_3", CubeListBuilder.create()
                .texOffs(87, 0).addBox(-2F, -15.6F, -0.5F, 4F, 16F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.5F, 0F, -1.3963F, 0.3491F, 0F));
        PartDefinition p_fan_4 = p_plume.addOrReplaceChild("fan_4", CubeListBuilder.create()
                .texOffs(44, 40).addBox(-2F, -13.79F, -0.5F, 4F, 14F, 1F, new CubeDeformation(-0.4F, -0.21F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.5F, 0F, -1.3963F, 0.6632F, 0F));
        PartDefinition p_fan_5 = p_plume.addOrReplaceChild("fan_5", CubeListBuilder.create()
                .texOffs(66, 40).addBox(-2F, -11.98F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.4F, -0.02F, -0.25F)),
                PartPose.offsetAndRotation(0F, -0.5F, 0F, -1.3963F, 0.9774F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
