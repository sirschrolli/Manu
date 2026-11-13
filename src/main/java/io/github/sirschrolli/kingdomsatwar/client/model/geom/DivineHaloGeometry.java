package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des geworfenen Heiligenscheins (Knochen "disc", dreht um Y).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DivineHaloGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private DivineHaloGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_disc = root.addOrReplaceChild("disc", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_seg_0 = p_disc.addOrReplaceChild("seg_0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(9F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_seg_1 = p_disc.addOrReplaceChild("seg_1", CubeListBuilder.create()
                .texOffs(15, 0).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(8.3149F, 0F, 3.4442F, 0F, -1.9635F, 0F));
        PartDefinition p_seg_2 = p_disc.addOrReplaceChild("seg_2", CubeListBuilder.create()
                .texOffs(30, 0).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(6.364F, 0F, 6.364F, 0F, -2.3562F, 0F));
        PartDefinition p_seg_3 = p_disc.addOrReplaceChild("seg_3", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(3.4442F, 0F, 8.3149F, 0F, -2.7489F, 0F));
        PartDefinition p_seg_4 = p_disc.addOrReplaceChild("seg_4", CubeListBuilder.create()
                .texOffs(0, 5).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 9F, 0F, -3.1416F, 0F));
        PartDefinition p_seg_5 = p_disc.addOrReplaceChild("seg_5", CubeListBuilder.create()
                .texOffs(15, 5).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-3.4442F, 0F, 8.3149F, 0F, -3.5343F, 0F));
        PartDefinition p_seg_6 = p_disc.addOrReplaceChild("seg_6", CubeListBuilder.create()
                .texOffs(30, 5).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-6.364F, 0F, 6.364F, 0F, -3.927F, 0F));
        PartDefinition p_seg_7 = p_disc.addOrReplaceChild("seg_7", CubeListBuilder.create()
                .texOffs(45, 5).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-8.3149F, 0F, 3.4442F, 0F, -4.3197F, 0F));
        PartDefinition p_seg_8 = p_disc.addOrReplaceChild("seg_8", CubeListBuilder.create()
                .texOffs(0, 10).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-9F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_seg_9 = p_disc.addOrReplaceChild("seg_9", CubeListBuilder.create()
                .texOffs(15, 10).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-8.3149F, 0F, -3.4442F, 0F, -5.1051F, 0F));
        PartDefinition p_seg_10 = p_disc.addOrReplaceChild("seg_10", CubeListBuilder.create()
                .texOffs(30, 10).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-6.364F, 0F, -6.364F, 0F, -5.4978F, 0F));
        PartDefinition p_seg_11 = p_disc.addOrReplaceChild("seg_11", CubeListBuilder.create()
                .texOffs(45, 10).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-3.4442F, 0F, -8.3149F, 0F, -5.8905F, 0F));
        PartDefinition p_seg_12 = p_disc.addOrReplaceChild("seg_12", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, -9F, 0F, -6.2832F, 0F));
        PartDefinition p_seg_13 = p_disc.addOrReplaceChild("seg_13", CubeListBuilder.create()
                .texOffs(15, 15).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(3.4442F, 0F, -8.3149F, 0F, -6.6759F, 0F));
        PartDefinition p_seg_14 = p_disc.addOrReplaceChild("seg_14", CubeListBuilder.create()
                .texOffs(30, 15).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(6.364F, 0F, -6.364F, 0F, -7.0686F, 0F));
        PartDefinition p_seg_15 = p_disc.addOrReplaceChild("seg_15", CubeListBuilder.create()
                .texOffs(45, 15).addBox(-2.5F, -1F, -1F, 5F, 2F, 2F, new CubeDeformation(-0.4829F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(8.3149F, 0F, -3.4442F, 0F, -7.4613F, 0F));
        PartDefinition p_ray_0 = p_disc.addOrReplaceChild("ray_0", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(9.2729F, 0F, 2.4847F, 0F, -0.2618F, 0F));
        PartDefinition p_ray_1 = p_disc.addOrReplaceChild("ray_1", CubeListBuilder.create()
                .texOffs(13, 23).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(6.7882F, 0F, 6.7882F, 0F, -0.7854F, 0F));
        PartDefinition p_ray_2 = p_disc.addOrReplaceChild("ray_2", CubeListBuilder.create()
                .texOffs(13, 20).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.4847F, 0F, 9.2729F, 0F, -1.309F, 0F));
        PartDefinition p_ray_3 = p_disc.addOrReplaceChild("ray_3", CubeListBuilder.create()
                .texOffs(24, 23).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.4847F, 0F, 9.2729F, 0F, -1.8326F, 0F));
        PartDefinition p_ray_4 = p_disc.addOrReplaceChild("ray_4", CubeListBuilder.create()
                .texOffs(26, 20).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-6.7882F, 0F, 6.7882F, 0F, -2.3562F, 0F));
        PartDefinition p_ray_5 = p_disc.addOrReplaceChild("ray_5", CubeListBuilder.create()
                .texOffs(35, 23).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-9.2729F, 0F, 2.4847F, 0F, -2.8798F, 0F));
        PartDefinition p_ray_6 = p_disc.addOrReplaceChild("ray_6", CubeListBuilder.create()
                .texOffs(39, 20).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-9.2729F, 0F, -2.4847F, 0F, -3.4034F, 0F));
        PartDefinition p_ray_7 = p_disc.addOrReplaceChild("ray_7", CubeListBuilder.create()
                .texOffs(46, 23).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-6.7882F, 0F, -6.7882F, 0F, -3.927F, 0F));
        PartDefinition p_ray_8 = p_disc.addOrReplaceChild("ray_8", CubeListBuilder.create()
                .texOffs(52, 20).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-2.4847F, 0F, -9.2729F, 0F, -4.4506F, 0F));
        PartDefinition p_ray_9 = p_disc.addOrReplaceChild("ray_9", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(2.4847F, 0F, -9.2729F, 0F, -4.9742F, 0F));
        PartDefinition p_ray_10 = p_disc.addOrReplaceChild("ray_10", CubeListBuilder.create()
                .texOffs(0, 23).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(6.7882F, 0F, -6.7882F, 0F, -5.4978F, 0F));
        PartDefinition p_ray_11 = p_disc.addOrReplaceChild("ray_11", CubeListBuilder.create()
                .texOffs(11, 26).addBox(-0.4F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(9.2729F, 0F, -2.4847F, 0F, -6.0214F, 0F));
        PartDefinition p_inner_0 = p_disc.addOrReplaceChild("inner_0", CubeListBuilder.create()
                .texOffs(22, 26).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(5.4F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_inner_1 = p_disc.addOrReplaceChild("inner_1", CubeListBuilder.create()
                .texOffs(33, 26).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(4.6765F, 0F, 2.7F, 0F, -2.0944F, 0F));
        PartDefinition p_inner_2 = p_disc.addOrReplaceChild("inner_2", CubeListBuilder.create()
                .texOffs(44, 26).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(2.7F, 0F, 4.6765F, 0F, -2.618F, 0F));
        PartDefinition p_inner_3 = p_disc.addOrReplaceChild("inner_3", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 5.4F, 0F, -3.1416F, 0F));
        PartDefinition p_inner_4 = p_disc.addOrReplaceChild("inner_4", CubeListBuilder.create()
                .texOffs(11, 29).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-2.7F, 0F, 4.6765F, 0F, -3.6652F, 0F));
        PartDefinition p_inner_5 = p_disc.addOrReplaceChild("inner_5", CubeListBuilder.create()
                .texOffs(22, 29).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-4.6765F, 0F, 2.7F, 0F, -4.1888F, 0F));
        PartDefinition p_inner_6 = p_disc.addOrReplaceChild("inner_6", CubeListBuilder.create()
                .texOffs(33, 29).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-5.4F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_inner_7 = p_disc.addOrReplaceChild("inner_7", CubeListBuilder.create()
                .texOffs(44, 29).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-4.6765F, 0F, -2.7F, 0F, -5.236F, 0F));
        PartDefinition p_inner_8 = p_disc.addOrReplaceChild("inner_8", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-2.7F, 0F, -4.6765F, 0F, -5.7596F, 0F));
        PartDefinition p_inner_9 = p_disc.addOrReplaceChild("inner_9", CubeListBuilder.create()
                .texOffs(11, 32).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, -5.4F, 0F, -6.2832F, 0F));
        PartDefinition p_inner_10 = p_disc.addOrReplaceChild("inner_10", CubeListBuilder.create()
                .texOffs(22, 32).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(2.7F, 0F, -4.6765F, 0F, -6.8068F, 0F));
        PartDefinition p_inner_11 = p_disc.addOrReplaceChild("inner_11", CubeListBuilder.create()
                .texOffs(33, 32).addBox(-2F, -0.5F, -0.5F, 4F, 1F, 1F, new CubeDeformation(-0.3863F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(4.6765F, 0F, -2.7F, 0F, -7.3304F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
