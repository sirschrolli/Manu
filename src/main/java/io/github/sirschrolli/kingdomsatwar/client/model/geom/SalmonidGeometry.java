package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Lachses (Knochen wie beim Vanilla-Lachs, dazu Buckel, Hakenkiefer und Flossen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SalmonidGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private SalmonidGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body_front = root.addOrReplaceChild("body_front", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -2.5F, 0F, 3F, 5F, 8F),
                PartPose.offset(0F, 20F, -7.2F));
        PartDefinition p_hump = p_body_front.addOrReplaceChild("hump", CubeListBuilder.create()
                .texOffs(31, 14).addBox(-1F, -1F, 0F, 2F, 1F, 4F),
                PartPose.offset(0F, -2.5F, 1F));
        PartDefinition p_top_front_fin = p_body_front.addOrReplaceChild("top_front_fin", CubeListBuilder.create()
                .texOffs(0, 14).addBox(0F, -1F, -2F, 0F, 3F, 4F),
                PartPose.offset(0F, -4.5F, 5F));
        PartDefinition p_pelvic_fin = p_body_front.addOrReplaceChild("pelvic_fin", CubeListBuilder.create()
                .texOffs(51, 14).addBox(0F, 0F, 0F, 0F, 2F, 2F),
                PartPose.offset(0F, 2.5F, 5F));
        PartDefinition p_body_back = root.addOrReplaceChild("body_back", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-1.5F, -2.5F, 0F, 3F, 5F, 3F)
                .texOffs(9, 14).addBox(-1.5F, -2F, 3F, 3F, 4F, 2F)
                .texOffs(20, 14).addBox(-1F, -1.5F, 5F, 2F, 3F, 3F),
                PartPose.offset(0F, 20F, 0.8F));
        PartDefinition p_back_fin = p_body_back.addOrReplaceChild("back_fin", CubeListBuilder.create()
                .texOffs(23, 0).addBox(0F, -3.5F, 0F, 0F, 7F, 5F),
                PartPose.offset(0F, 0F, 8F));
        PartDefinition p_top_back_fin = p_body_back.addOrReplaceChild("top_back_fin", CubeListBuilder.create()
                .texOffs(56, 14).addBox(0F, 1F, 6F, 0F, 2F, 2F),
                PartPose.offset(0F, -4.5F, -1F));
        PartDefinition p_anal_fin = p_body_back.addOrReplaceChild("anal_fin", CubeListBuilder.create()
                .texOffs(0, 22).addBox(0F, 0F, 0F, 0F, 2F, 2F),
                PartPose.offset(0F, 2F, 3F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(47, 0).addBox(-1F, -2F, -3F, 2F, 4F, 3F)
                .texOffs(44, 14).addBox(-1F, -1F, -4F, 2F, 3F, 1F),
                PartPose.offset(0F, 20F, -7.2F));
        PartDefinition p_kype = p_head.addOrReplaceChild("kype", CubeListBuilder.create()
                .texOffs(32, 22).addBox(-1F, 0F, -1F, 2F, 1F, 1F)
                .texOffs(27, 22).addBox(-0.5F, 0F, -2F, 1F, 2F, 1F),
                PartPose.offset(0F, -1F, -4F));
        PartDefinition p_right_fin = root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(5, 22).addBox(-2F, 0F, 0F, 2F, 0F, 3F),
                PartPose.offsetAndRotation(-1.5F, 21.5F, -7.2F, 0F, 0F, -0.7854F));
        PartDefinition p_left_fin = root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(16, 22).addBox(0F, 0F, 0F, 2F, 0F, 3F),
                PartPose.offsetAndRotation(1.5F, 21.5F, -7.2F, 0F, 0F, 0.7854F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
