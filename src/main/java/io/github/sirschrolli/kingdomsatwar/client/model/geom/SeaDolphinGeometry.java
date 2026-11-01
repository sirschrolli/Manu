package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Delfine (Knochen wie beim Vanilla-Delfin, Finne je Art).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SeaDolphinGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private SeaDolphinGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -7F, 0F, 8F, 7F, 9F)
                .texOffs(17, 17).addBox(-3.5F, -6.5F, 9F, 7F, 6F, 3F),
                PartPose.offset(0F, 22F, -5F));
        PartDefinition p_back_fin = p_body.addOrReplaceChild("back_fin", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 1.0472F, 0F, 0F));
        PartDefinition p_dorsal_low = p_back_fin.addOrReplaceChild("dorsal_low", CubeListBuilder.create()
                .texOffs(19, 29).addBox(-0.5F, -8F, 3F, 1F, 2F, 6F)
                .texOffs(0, 47).addBox(-0.5F, -9F, 4F, 1F, 1F, 5F)
                .texOffs(48, 47).addBox(-0.5F, -10F, 5.5F, 1F, 1F, 4F)
                .texOffs(16, 54).addBox(-0.5F, -11F, 7.5F, 1F, 1F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
        PartDefinition p_dorsal_tall = p_back_fin.addOrReplaceChild("dorsal_tall", CubeListBuilder.create()
                .texOffs(13, 47).addBox(-0.5F, -8F, 3F, 1F, 2F, 4F)
                .texOffs(0, 54).addBox(-0.5F, -10F, 4F, 1F, 2F, 3F)
                .texOffs(9, 54).addBox(-0.5F, -12F, 4.5F, 1F, 2F, 2F)
                .texOffs(23, 54).addBox(-0.5F, -14F, 5F, 1F, 2F, 1F),
                PartPose.offsetAndRotation(0F, 0F, 0F, -1.0472F, 0F, 0F));
        PartDefinition p_left_fin = p_body.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(35, 0).addBox(-0.5F, -4F, 0F, 1F, 4F, 7F),
                PartPose.offsetAndRotation(2F, -2F, 4F, 1.0472F, 0F, 2.0944F));
        PartDefinition p_right_fin = p_body.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-0.5F, -4F, 0F, 1F, 4F, 7F),
                PartPose.offsetAndRotation(-2F, -2F, 4F, 1.0472F, 0F, -2.0944F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-2.5F, -3F, 0F, 5F, 5F, 4F)
                .texOffs(34, 29).addBox(-1.5F, -2.5F, 4F, 3F, 4F, 4F)
                .texOffs(24, 47).addBox(-1F, -1.5F, 8F, 2F, 3F, 3F),
                PartPose.offsetAndRotation(0F, -2.5F, 11F, -0.1047F, 0F, 0F));
        PartDefinition p_tail_fin = p_tail.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-5F, -0.5F, 0F, 10F, 1F, 6F),
                PartPose.offset(0F, 0F, 9F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(38, 17).addBox(-3.5F, -2F, 0F, 7F, 6F, 3F)
                .texOffs(33, 39).addBox(-3F, -1F, -2F, 6F, 5F, 2F)
                .texOffs(35, 47).addBox(-2.5F, 0F, -3F, 5F, 4F, 1F),
                PartPose.offset(0F, -4F, -3F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(50, 39).addBox(-1.5F, 2F, -7F, 3F, 2F, 4F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
