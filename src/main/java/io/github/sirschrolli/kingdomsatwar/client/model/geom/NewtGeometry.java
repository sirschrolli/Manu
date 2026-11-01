package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Molchs (Knochen wie beim Vanilla-Axolotl).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class NewtGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private NewtGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-4F, -2F, -9F, 8F, 4F, 10F)
                .texOffs(0, 19).addBox(0F, -4F, -7F, 0F, 2F, 8F),
                PartPose.offset(0F, 19.5F, 5F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(17, 19).addBox(-4.5F, -3F, -5F, 9F, 4F, 5F)
                .texOffs(22, 30).addBox(-4F, 1F, -5F, 8F, 1F, 5F),
                PartPose.offset(0F, 0F, -9F));
        PartDefinition p_top_gills = p_head.addOrReplaceChild("top_gills", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-4.5F, -4F, 0F, 9F, 4F, 0F),
                PartPose.offset(0F, -3F, -1F));
        PartDefinition p_left_gills = p_head.addOrReplaceChild("left_gills", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-5F, -6F, 0F, 5F, 8F, 0F),
                PartPose.offset(-4F, 0F, -1F));
        PartDefinition p_right_gills = p_head.addOrReplaceChild("right_gills", CubeListBuilder.create()
                .texOffs(11, 30).addBox(0F, -6F, 0F, 5F, 8F, 0F),
                PartPose.offset(4F, 0F, -1F));
        PartDefinition p_right_hind_leg = p_body.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(34, 44).addBox(-1F, 0F, -0.5F, 2F, 3F, 1F)
                .texOffs(19, 39).addBox(-2F, 3F, -3.5F, 4F, 0F, 4F),
                PartPose.offset(-3.5F, 1F, -1F));
        PartDefinition p_left_hind_leg = p_body.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(41, 44).addBox(-1F, 0F, -0.5F, 2F, 3F, 1F)
                .texOffs(36, 39).addBox(-2F, 3F, -3.5F, 4F, 0F, 4F),
                PartPose.offset(3.5F, 1F, -1F));
        PartDefinition p_right_front_leg = p_body.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(48, 44).addBox(-1F, 0F, -0.5F, 2F, 3F, 1F)
                .texOffs(0, 44).addBox(-2F, 3F, -3.5F, 4F, 0F, 4F),
                PartPose.offset(-3.5F, 1F, -8F));
        PartDefinition p_left_front_leg = p_body.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(55, 44).addBox(-1F, 0F, -0.5F, 2F, 3F, 1F)
                .texOffs(17, 44).addBox(-2F, 3F, -3.5F, 4F, 0F, 4F),
                PartPose.offset(3.5F, 1F, -8F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(46, 19).addBox(-1F, -1.5F, 0F, 2F, 3F, 5F)
                .texOffs(49, 30).addBox(-0.5F, -1F, 5F, 1F, 2F, 4F)
                .texOffs(0, 0).addBox(0F, -3F, 0F, 0F, 6F, 12F),
                PartPose.offset(0F, 0F, 1F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
