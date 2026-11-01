package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Huhns (Hahn mit großem Kamm und Sichelfedern).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FarmChickenGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private FarmChickenGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -3.5F, -4F, 6F, 6F, 8F)
                .texOffs(43, 15).addBox(-2.5F, -1F, -5F, 5F, 4F, 2F),
                PartPose.offset(0F, 16F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-2F, -5F, -2F, 4F, 5F, 3.5F)
                .texOffs(0, 32).addBox(-1F, -3.5F, -3.6F, 2F, 1F, 1.6F)
                .texOffs(45, 32).addBox(-0.75F, -2.5F, -3F, 1.5F, 1.8F, 1F)
                .texOffs(57, 32).addBox(2F, -4F, -1.2F, 0.3F, 1F, 1F)
                .texOffs(0, 36).addBox(-2.3F, -4F, -1.2F, 0.3F, 1F, 1F),
                PartPose.offset(0F, -3.5F, -4F));
        PartDefinition p_comb = p_head.addOrReplaceChild("comb", CubeListBuilder.create()
                .texOffs(26, 25).addBox(-0.5F, -1.5F, -1.5F, 1F, 1.5F, 3F),
                PartPose.offset(0F, -5F, -0.5F));
        PartDefinition p_comb_big = p_head.addOrReplaceChild("comb_big", CubeListBuilder.create()
                .texOffs(17, 15).addBox(-0.5F, -2.8F, -2F, 1F, 2.8F, 4.2F),
                PartPose.offset(0F, -5F, -0.5F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(29, 0).addBox(0F, 0F, 0F, 1F, 4.5F, 6.5F),
                PartPose.offset(3F, -2.5F, -2.5F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1F, 0F, 0F, 1F, 4.5F, 6.5F),
                PartPose.offset(-3F, -2.5F, -2.5F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(30, 15).addBox(-2F, -4F, -0.6F, 4F, 4.5F, 1.6F)
                .texOffs(35, 25).addBox(-1.5F, -5.5F, -0.3F, 3F, 1.5F, 1.1F),
                PartPose.offsetAndRotation(0F, -2.5F, 3.2F, -0.4887F, 0F, 0F));
        PartDefinition p_sickle = p_tail.addOrReplaceChild("sickle", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-1.25F, -4.2F, -0.4F, 2.5F, 4.4F, 0.8F),
                PartPose.offsetAndRotation(0F, -5F, 0.6F, -0.5236F, 0F, 0F));
        PartDefinition p_sickle_tip = p_sickle.addOrReplaceChild("sickle_tip", CubeListBuilder.create()
                .texOffs(9, 25).addBox(-1F, -4.2F, -0.4F, 2F, 4.4F, 0.8F)
                .texOffs(52, 32).addBox(-0.5F, -5.6F, -0.3F, 1F, 1.6F, 0.6F),
                PartPose.offsetAndRotation(0F, -4F, 0F, -1.7453F, 0F, 0F));
        PartDefinition p_leg_l = root.addOrReplaceChild("leg_l", CubeListBuilder.create()
                .texOffs(16, 25).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
                .texOffs(46, 25).addBox(-0.5F, 4.6F, -2.6F, 1F, 0.4F, 2.1F)
                .texOffs(9, 32).addBox(-1.6F, 4.6F, -1.6F, 1.1F, 0.4F, 1.1F)
                .texOffs(18, 32).addBox(0.5F, 4.6F, -1.6F, 1.1F, 0.4F, 1.1F)
                .texOffs(5, 36).addBox(-0.4F, 4.6F, 0.5F, 0.8F, 0.4F, 0.9F),
                PartPose.offset(1.5F, 19F, 0.5F));
        PartDefinition p_leg_r = root.addOrReplaceChild("leg_r", CubeListBuilder.create()
                .texOffs(21, 25).addBox(-0.5F, 0F, -0.5F, 1F, 5F, 1F)
                .texOffs(55, 25).addBox(-0.5F, 4.6F, -2.6F, 1F, 0.4F, 2.1F)
                .texOffs(27, 32).addBox(-1.6F, 4.6F, -1.6F, 1.1F, 0.4F, 1.1F)
                .texOffs(36, 32).addBox(0.5F, 4.6F, -1.6F, 1.1F, 0.4F, 1.1F)
                .texOffs(10, 36).addBox(-0.4F, 4.6F, 0.5F, 0.8F, 0.4F, 0.9F),
                PartPose.offset(-1.5F, 19F, 0.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
