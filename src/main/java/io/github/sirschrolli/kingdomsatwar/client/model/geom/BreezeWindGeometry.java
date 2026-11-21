package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Windgeists (Wind-Ebene mit nahtlos rollender Textur).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BreezeWindGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private BreezeWindGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_rods = p_body.addOrReplaceChild("rods", CubeListBuilder.create(),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_rod_1 = p_rods.addOrReplaceChild("rod_1", CubeListBuilder.create(),
                PartPose.offsetAndRotation(2.5981F, -3F, 1.5F, -2.7489F, -1.0472F, 3.1416F));
        PartDefinition p_rod_2 = p_rods.addOrReplaceChild("rod_2", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.5981F, -3F, 1.5F, -2.7489F, 1.0472F, 3.1416F));
        PartDefinition p_rod_3 = p_rods.addOrReplaceChild("rod_3", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -3F, -3F, 0.3927F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_eyes = p_head.addOrReplaceChild("eyes", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hood = p_head.addOrReplaceChild("hood", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hood_tip = p_hood.addOrReplaceChild("hood_tip", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -9F, 4.4F, -0.3142F, 0F, 0F));
        PartDefinition p_streamer_right = p_hood.addOrReplaceChild("streamer_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-2.2F, -5F, 4.8F, 0.2793F, -0.3142F, 0F));
        PartDefinition p_streamer_left = p_hood.addOrReplaceChild("streamer_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(2.2F, -5F, 4.8F, 0.1745F, 0.3491F, 0F));
        PartDefinition p_plume_right = p_hood.addOrReplaceChild("plume_right", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-5.2F, -6.2F, 0.5F, -0.2793F, -0.5236F, 0F));
        PartDefinition p_plume_left = p_hood.addOrReplaceChild("plume_left", CubeListBuilder.create(),
                PartPose.offsetAndRotation(5.2F, -6.2F, 0.5F, -0.2793F, 0.5236F, 0F));
        PartDefinition p_wind_body = root.addOrReplaceChild("wind_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wind_bottom = p_wind_body.addOrReplaceChild("wind_bottom", CubeListBuilder.create()
                .texOffs(103, 27).addBox(-2.5F, -7F, -2.5F, 5F, 7F, 5F),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_wind_mid = p_wind_bottom.addOrReplaceChild("wind_mid", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-6F, -6F, -6F, 12F, 6F, 12F)
                .texOffs(49, 27).addBox(-4F, -6F, -4F, 8F, 6F, 8F)
                .texOffs(0, 46).addBox(-2.5F, -6F, -2.5F, 5F, 6F, 5F),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_wind_top = p_wind_mid.addOrReplaceChild("wind_top", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, -8F, -9F, 18F, 8F, 18F)
                .texOffs(73, 0).addBox(-6F, -8F, -6F, 12F, 8F, 12F)
                .texOffs(82, 27).addBox(-2.5F, -8F, -2.5F, 5F, 8F, 5F),
                PartPose.offset(0F, -6F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
