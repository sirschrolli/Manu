package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Windgeists (Körper: Kopf mit Kapuze, Runensteine, Augen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BreezeGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private BreezeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_rods = p_body.addOrReplaceChild("rods", CubeListBuilder.create(),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_rod_1 = p_rods.addOrReplaceChild("rod_1", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-1.5F, 0.4F, -3.2F, 3F, 6F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(25, 42).addBox(-0.5F, 6.4F, -2.7F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(2.5981F, -3F, 1.5F, -2.7489F, -1.0472F, 3.1416F));
        PartDefinition p_rod_2 = p_rods.addOrReplaceChild("rod_2", CubeListBuilder.create()
                .texOffs(11, 33).addBox(-1.5F, 0.4F, -3.2F, 3F, 6F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(30, 42).addBox(-0.5F, 6.4F, -2.7F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(-2.5981F, -3F, 1.5F, -2.7489F, 1.0472F, 3.1416F));
        PartDefinition p_rod_3 = p_rods.addOrReplaceChild("rod_3", CubeListBuilder.create()
                .texOffs(22, 33).addBox(-1.5F, 0.4F, -3.2F, 3F, 6F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(35, 42).addBox(-0.5F, 6.4F, -2.7F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, -3F, -3F, 0.3927F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(33, 33).addBox(-5F, -5F, -4.2F, 10F, 3F, 4F)
                .texOffs(41, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_eyes = p_head.addOrReplaceChild("eyes", CubeListBuilder.create()
                .texOffs(62, 33).addBox(-5F, -5F, -4.2F, 10F, 3F, 4F)
                .texOffs(74, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hood = p_head.addOrReplaceChild("hood", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -9.05F, -3.75F, 11F, 7F, 9F, new CubeDeformation(-0.3F, -0.15F, -0.45F))
                .texOffs(73, 17).addBox(-3.5F, -9.9F, -1.9F, 7F, 1F, 7F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(0, 42).addBox(-5F, -9.65F, -4.9F, 10F, 2F, 2F, new CubeDeformation(-0.3F, -0.35F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hood_tip = p_hood.addOrReplaceChild("hood_tip", CubeListBuilder.create()
                .texOffs(91, 33).addBox(-2F, -1.6F, -0.25F, 4F, 3F, 4F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(108, 33).addBox(-1F, -1.1F, 3.5F, 2F, 2F, 3F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, -9F, 4.4F, -0.3142F, 0F, 0F));
        PartDefinition p_streamer_right = p_hood.addOrReplaceChild("streamer_right", CubeListBuilder.create()
                .texOffs(50, 17).addBox(-1F, -0.5F, 0F, 2F, 1F, 9F),
                PartPose.offsetAndRotation(-2.2F, -5F, 4.8F, 0.2793F, -0.3142F, 0F));
        PartDefinition p_streamer_left = p_hood.addOrReplaceChild("streamer_left", CubeListBuilder.create()
                .texOffs(102, 17).addBox(-1F, -0.5F, 0F, 2F, 1F, 7F),
                PartPose.offsetAndRotation(2.2F, -5F, 4.8F, 0.1745F, 0.3491F, 0F));
        PartDefinition p_plume_right = p_hood.addOrReplaceChild("plume_right", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-1F, -2F, 0F, 1F, 4F, 11F),
                PartPose.offsetAndRotation(-5.2F, -6.2F, 0.5F, -0.2793F, -0.5236F, 0F));
        PartDefinition p_plume_left = p_hood.addOrReplaceChild("plume_left", CubeListBuilder.create()
                .texOffs(25, 17).addBox(0F, -2F, 0F, 1F, 4F, 11F),
                PartPose.offsetAndRotation(5.2F, -6.2F, 0.5F, -0.2793F, 0.5236F, 0F));
        PartDefinition p_wind_body = root.addOrReplaceChild("wind_body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_wind_bottom = p_wind_body.addOrReplaceChild("wind_bottom", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_wind_mid = p_wind_bottom.addOrReplaceChild("wind_mid", CubeListBuilder.create(),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_wind_top = p_wind_mid.addOrReplaceChild("wind_top", CubeListBuilder.create(),
                PartPose.offset(0F, -6F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
