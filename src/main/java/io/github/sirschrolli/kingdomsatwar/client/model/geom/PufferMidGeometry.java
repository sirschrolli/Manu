package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kugelfischs (mittel, Knochen wie beim Vanilla-Kugelfisch).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PufferMidGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private PufferMidGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -5F, -2.5F, 5F, 5F, 5F)
                .texOffs(43, 0).addBox(-3.5F, -4F, -1.5F, 1F, 3F, 3F)
                .texOffs(52, 0).addBox(2.5F, -4F, -1.5F, 1F, 3F, 3F)
                .texOffs(43, 11).addBox(-1.5F, -4F, -3.5F, 3F, 3F, 1F)
                .texOffs(52, 11).addBox(-1.5F, -4F, 2.5F, 3F, 3F, 1F)
                .texOffs(17, 11).addBox(-1.5F, -6F, -1.5F, 3F, 1F, 3F)
                .texOffs(30, 11).addBox(-1.5F, 0F, -1.5F, 3F, 1F, 3F)
                .texOffs(25, 17).addBox(-4.25F, -4F, -1.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(30, 17).addBox(3.25F, -4F, -1.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(18, 17).addBox(-1F, -2F, -4.25F, 2F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, 22F, 0F));
        PartDefinition p_right_blue_fin = root.addOrReplaceChild("right_blue_fin", CubeListBuilder.create()
                .texOffs(0, 17).addBox(-2F, 0.5F, 1F, 2F, 0F, 2F),
                PartPose.offset(-2.5F, 18F, -1.5F));
        PartDefinition p_left_blue_fin = root.addOrReplaceChild("left_blue_fin", CubeListBuilder.create()
                .texOffs(9, 17).addBox(0F, 0.5F, 1F, 2F, 0F, 2F),
                PartPose.offset(2.5F, 18F, -1.5F));
        PartDefinition p_top_front_fin = root.addOrReplaceChild("top_front_fin", CubeListBuilder.create()
                .texOffs(35, 17).addBox(-2.5F, -1F, 0F, 5F, 1F, 0F),
                PartPose.offsetAndRotation(0F, 17F, -2.5F, 0.7854F, 0F, 0F));
        PartDefinition p_top_back_fin = root.addOrReplaceChild("top_back_fin", CubeListBuilder.create()
                .texOffs(46, 17).addBox(-2.5F, -1F, 0F, 5F, 1F, 0F),
                PartPose.offsetAndRotation(0F, 17F, 2.5F, -0.7854F, 0F, 0F));
        PartDefinition p_right_front_fin = root.addOrReplaceChild("right_front_fin", CubeListBuilder.create()
                .texOffs(5, 11).addBox(-1F, -5F, 0F, 1F, 5F, 0F),
                PartPose.offsetAndRotation(-2.5F, 22F, -2.5F, 0F, -0.7854F, 0F));
        PartDefinition p_right_back_fin = root.addOrReplaceChild("right_back_fin", CubeListBuilder.create()
                .texOffs(8, 11).addBox(-1F, -5F, 0F, 1F, 5F, 0F),
                PartPose.offsetAndRotation(-2.5F, 22F, 2.5F, 0F, 0.7854F, 0F));
        PartDefinition p_left_back_fin = root.addOrReplaceChild("left_back_fin", CubeListBuilder.create()
                .texOffs(11, 11).addBox(0F, -5F, 0F, 1F, 5F, 0F),
                PartPose.offsetAndRotation(2.5F, 22F, 2.5F, 0F, -0.7854F, 0F));
        PartDefinition p_left_front_fin = root.addOrReplaceChild("left_front_fin", CubeListBuilder.create()
                .texOffs(14, 11).addBox(0F, -5F, 0F, 1F, 5F, 0F),
                PartPose.offsetAndRotation(2.5F, 22F, -2.5F, 0F, 0.7854F, 0F));
        PartDefinition p_bottom_back_fin = root.addOrReplaceChild("bottom_back_fin", CubeListBuilder.create()
                .texOffs(0, 20).addBox(0F, 0F, 0F, 5F, 1F, 0F),
                PartPose.offsetAndRotation(-2.5F, 22F, 2.5F, 0.7854F, 0F, 0F));
        PartDefinition p_bottom_front_fin = root.addOrReplaceChild("bottom_front_fin", CubeListBuilder.create()
                .texOffs(11, 20).addBox(-2.5F, 0F, 0F, 5F, 1F, 0F),
                PartPose.offsetAndRotation(0F, 22F, -2.5F, -0.7854F, 0F, 0F));
        PartDefinition p_top_right_spines = root.addOrReplaceChild("top_right_spines", CubeListBuilder.create()
                .texOffs(21, 0).addBox(0F, -1F, -2.5F, 0F, 1F, 5F),
                PartPose.offsetAndRotation(-2.5F, 17F, 0F, 0F, 0F, -0.7854F));
        PartDefinition p_top_left_spines = root.addOrReplaceChild("top_left_spines", CubeListBuilder.create()
                .texOffs(32, 0).addBox(0F, -1F, -2.5F, 0F, 1F, 5F),
                PartPose.offsetAndRotation(2.5F, 17F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_tail_fin = root.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(0, 11).addBox(0F, -1.5F, 0F, 0F, 3F, 2F),
                PartPose.offset(0F, 19.5F, 3.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
