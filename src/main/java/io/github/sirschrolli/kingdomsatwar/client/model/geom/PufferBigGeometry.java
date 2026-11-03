package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kugelfischs (gross, Knochen wie beim Vanilla-Kugelfisch).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PufferBigGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private PufferBigGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F)
                .texOffs(33, 0).addBox(-5F, -7F, -3F, 1F, 6F, 6F)
                .texOffs(48, 0).addBox(4F, -7F, -3F, 1F, 6F, 6F)
                .texOffs(25, 39).addBox(-3F, -7F, -5F, 6F, 6F, 1F)
                .texOffs(40, 39).addBox(-3F, -7F, 4F, 6F, 6F, 1F)
                .texOffs(37, 28).addBox(-3F, -9F, -3F, 6F, 1F, 6F)
                .texOffs(0, 39).addBox(-3F, 0F, -3F, 6F, 1F, 6F)
                .texOffs(0, 47).addBox(-5.75F, -7F, -3F, 1F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(7, 47).addBox(4.75F, -7F, -3F, 1F, 2F, 2F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(52, 52).addBox(-1F, -4F, -6F, 2F, 1F, 1F),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_right_blue_fin = root.addOrReplaceChild("right_blue_fin", CubeListBuilder.create()
                .texOffs(30, 52).addBox(-3F, 0.5F, 1F, 3F, 0F, 2F),
                PartPose.offset(-4F, 15F, -2F));
        PartDefinition p_left_blue_fin = root.addOrReplaceChild("left_blue_fin", CubeListBuilder.create()
                .texOffs(41, 52).addBox(0F, 0.5F, 1F, 3F, 0F, 2F),
                PartPose.offset(4F, 15F, -2F));
        PartDefinition p_top_front_fin = root.addOrReplaceChild("top_front_fin", CubeListBuilder.create()
                .texOffs(14, 47).addBox(-4F, -2F, 0F, 8F, 2F, 0F),
                PartPose.offsetAndRotation(0F, 14F, -4F, 0.7854F, 0F, 0F));
        PartDefinition p_top_middle_fin = root.addOrReplaceChild("top_middle_fin", CubeListBuilder.create()
                .texOffs(17, 52).addBox(-3F, -3F, 0F, 6F, 2F, 0F),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_top_back_fin = root.addOrReplaceChild("top_back_fin", CubeListBuilder.create()
                .texOffs(31, 47).addBox(-4F, -2F, 0F, 8F, 2F, 0F),
                PartPose.offsetAndRotation(0F, 14F, 4F, -0.7854F, 0F, 0F));
        PartDefinition p_right_front_fin = root.addOrReplaceChild("right_front_fin", CubeListBuilder.create()
                .texOffs(17, 28).addBox(-2F, -8F, 0F, 2F, 8F, 0F),
                PartPose.offsetAndRotation(-4F, 22F, -4F, 0F, -0.7854F, 0F));
        PartDefinition p_left_front_fin = root.addOrReplaceChild("left_front_fin", CubeListBuilder.create()
                .texOffs(22, 28).addBox(0F, -8F, 0F, 2F, 8F, 0F),
                PartPose.offsetAndRotation(4F, 22F, -4F, 0F, 0.7854F, 0F));
        PartDefinition p_bottom_front_fin = root.addOrReplaceChild("bottom_front_fin", CubeListBuilder.create()
                .texOffs(48, 47).addBox(-4F, 0F, 0F, 8F, 2F, 0F),
                PartPose.offsetAndRotation(0F, 22F, -4F, -0.7854F, 0F, 0F));
        PartDefinition p_bottom_middle_fin = root.addOrReplaceChild("bottom_middle_fin", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-3F, 1F, 0F, 6F, 1F, 0F),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_bottom_back_fin = root.addOrReplaceChild("bottom_back_fin", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-4F, 0F, 0F, 8F, 2F, 0F),
                PartPose.offsetAndRotation(0F, 22F, 4F, 0.7854F, 0F, 0F));
        PartDefinition p_right_back_fin = root.addOrReplaceChild("right_back_fin", CubeListBuilder.create()
                .texOffs(27, 28).addBox(-2F, -8F, 0F, 2F, 8F, 0F),
                PartPose.offsetAndRotation(-4F, 22F, 4F, 0F, 0.7854F, 0F));
        PartDefinition p_left_back_fin = root.addOrReplaceChild("left_back_fin", CubeListBuilder.create()
                .texOffs(32, 28).addBox(0F, -8F, 0F, 2F, 8F, 0F),
                PartPose.offsetAndRotation(4F, 22F, 4F, 0F, -0.7854F, 0F));
        PartDefinition p_top_right_spines = root.addOrReplaceChild("top_right_spines", CubeListBuilder.create()
                .texOffs(0, 17).addBox(0F, -2F, -4F, 0F, 2F, 8F),
                PartPose.offsetAndRotation(-4F, 14F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_top_left_spines = root.addOrReplaceChild("top_left_spines", CubeListBuilder.create()
                .texOffs(17, 17).addBox(0F, -2F, -4F, 0F, 2F, 8F),
                PartPose.offsetAndRotation(4F, 14F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_bottom_right_spines = root.addOrReplaceChild("bottom_right_spines", CubeListBuilder.create()
                .texOffs(34, 17).addBox(0F, 0F, -4F, 0F, 2F, 8F),
                PartPose.offsetAndRotation(-4F, 22F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_bottom_left_spines = root.addOrReplaceChild("bottom_left_spines", CubeListBuilder.create()
                .texOffs(0, 28).addBox(0F, 0F, -4F, 0F, 2F, 8F),
                PartPose.offsetAndRotation(4F, 22F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_tail_fin = root.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(55, 39).addBox(0F, -2F, 0F, 0F, 4F, 3F),
                PartPose.offset(0F, 18F, 5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
