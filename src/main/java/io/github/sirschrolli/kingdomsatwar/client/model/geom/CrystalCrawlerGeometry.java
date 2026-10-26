package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kristallkriechers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CrystalCrawlerGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 96;

    private CrystalCrawlerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -3F, -7F, 10F, 5F, 13F)
                .texOffs(0, 19).addBox(-4F, 2F, -6F, 8F, 1F, 11F),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_abdomen = p_body.addOrReplaceChild("abdomen", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-4F, -2F, 0F, 8F, 5F, 7F)
                .texOffs(18, 45).addBox(-2.5F, -1F, 7F, 5F, 3F, 2F),
                PartPose.offsetAndRotation(0F, -1F, 6F, -0.1396F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(40, 32).addBox(-3.5F, -2.5F, -4F, 7F, 4F, 4F)
                .texOffs(0, 72).addBox(-2.4F, -1.6F, -4.3F, 1F, 1F, 0.5F)
                .texOffs(5, 72).addBox(1.4F, -1.6F, -4.3F, 1F, 1F, 0.5F)
                .texOffs(10, 72).addBox(-1.6F, -0.4F, -4.3F, 1F, 1F, 0.5F)
                .texOffs(15, 72).addBox(0.6F, -0.4F, -4.3F, 1F, 1F, 0.5F),
                PartPose.offset(0F, 0F, -7F));
        PartDefinition p_mandible_l = p_head.addOrReplaceChild("mandible_l", CubeListBuilder.create()
                .texOffs(33, 45).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(2.2F, 0.5F, -4F, 0F, 0.384F, 0F));
        PartDefinition p_mandible_r = p_head.addOrReplaceChild("mandible_r", CubeListBuilder.create()
                .texOffs(44, 45).addBox(-0.5F, -0.5F, -4F, 1F, 1F, 4F),
                PartPose.offsetAndRotation(-2.2F, 0.5F, -4F, 0F, -0.384F, 0F));
        PartDefinition p_leg_l1 = p_body.addOrReplaceChild("leg_l1", CubeListBuilder.create()
                .texOffs(0, 54).addBox(0F, -0.75F, -0.75F, 9F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(4.5F, 0F, -4.5F, 0F, -0.3491F, 0.733F));
        PartDefinition p_leg_l1_foot = p_leg_l1.addOrReplaceChild("leg_l1_foot", CubeListBuilder.create()
                .texOffs(51, 64).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(8.5F, 0F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_leg_r1 = p_body.addOrReplaceChild("leg_r1", CubeListBuilder.create()
                .texOffs(23, 54).addBox(-9F, -0.75F, -0.75F, 9F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-4.5F, 0F, -4.5F, 0F, 0.3491F, -0.733F));
        PartDefinition p_leg_r1_foot = p_leg_r1.addOrReplaceChild("leg_r1_foot", CubeListBuilder.create()
                .texOffs(0, 69).addBox(-5F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(-8.5F, 0F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_leg_l2 = p_body.addOrReplaceChild("leg_l2", CubeListBuilder.create()
                .texOffs(0, 59).addBox(0F, -0.75F, -0.75F, 9F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(4.5F, 0F, 0F, 0F, 0F, 0.733F));
        PartDefinition p_leg_l2_foot = p_leg_l2.addOrReplaceChild("leg_l2_foot", CubeListBuilder.create()
                .texOffs(13, 69).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(8.5F, 0F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_leg_r2 = p_body.addOrReplaceChild("leg_r2", CubeListBuilder.create()
                .texOffs(23, 59).addBox(-9F, -0.75F, -0.75F, 9F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-4.5F, 0F, 0F, 0F, 0F, -0.733F));
        PartDefinition p_leg_r2_foot = p_leg_r2.addOrReplaceChild("leg_r2_foot", CubeListBuilder.create()
                .texOffs(26, 69).addBox(-5F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(-8.5F, 0F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_leg_l3 = p_body.addOrReplaceChild("leg_l3", CubeListBuilder.create()
                .texOffs(0, 64).addBox(0F, -0.75F, -0.75F, 9F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(4.5F, 0F, 4.5F, 0F, 0.3491F, 0.733F));
        PartDefinition p_leg_l3_foot = p_leg_l3.addOrReplaceChild("leg_l3_foot", CubeListBuilder.create()
                .texOffs(39, 69).addBox(0F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(8.5F, 0F, 0F, 0F, 0F, 0.8727F));
        PartDefinition p_leg_r3 = p_body.addOrReplaceChild("leg_r3", CubeListBuilder.create()
                .texOffs(23, 64).addBox(-9F, -0.75F, -0.75F, 9F, 1.5F, 1.5F),
                PartPose.offsetAndRotation(-4.5F, 0F, 4.5F, 0F, -0.3491F, -0.733F));
        PartDefinition p_leg_r3_foot = p_leg_r3.addOrReplaceChild("leg_r3_foot", CubeListBuilder.create()
                .texOffs(52, 69).addBox(-5F, -0.5F, -0.5F, 5F, 1F, 1F),
                PartPose.offsetAndRotation(-8.5F, 0F, 0F, 0F, 0F, -0.8727F));
        PartDefinition p_crystals = p_body.addOrReplaceChild("crystals", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crystal1 = p_crystals.addOrReplaceChild("crystal1", CubeListBuilder.create()
                .texOffs(31, 32).addBox(-1F, -8F, -1F, 2F, 8F, 2F),
                PartPose.offsetAndRotation(-1.5F, -3F, -2F, -0.1396F, 0F, 0.1745F));
        PartDefinition p_crystal2 = p_crystals.addOrReplaceChild("crystal2", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-1F, -6F, -1F, 2F, 6F, 2F),
                PartPose.offsetAndRotation(2F, -3F, 1F, 0.2443F, 0F, -0.2793F));
        PartDefinition p_crystal3 = p_crystals.addOrReplaceChild("crystal3", CubeListBuilder.create()
                .texOffs(9, 45).addBox(-1F, -5F, -1F, 2F, 5F, 2F),
                PartPose.offsetAndRotation(0.5F, -3F, -5F, -0.384F, 0F, -0.1396F));
        PartDefinition p_crystal4 = p_crystals.addOrReplaceChild("crystal4", CubeListBuilder.create()
                .texOffs(55, 45).addBox(-0.5F, -4F, -0.5F, 1F, 4F, 1F),
                PartPose.offsetAndRotation(-2.5F, -3F, 3F, 0.3491F, 0F, 0.384F));
        PartDefinition p_crystal5 = p_crystals.addOrReplaceChild("crystal5", CubeListBuilder.create()
                .texOffs(46, 64).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(1F, -3F, 5F, 0.5236F, 0F, -0.0698F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
