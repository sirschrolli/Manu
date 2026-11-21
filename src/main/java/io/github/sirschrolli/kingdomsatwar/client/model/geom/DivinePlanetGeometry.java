package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie einer Welt der Schöpfung (Knochen "planet" und "ring").
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DivinePlanetGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private DivinePlanetGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_planet = root.addOrReplaceChild("planet", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-6F, -4F, -4F, 12F, 8F, 8F)
                .texOffs(86, 0).addBox(-4F, -6F, -4F, 8F, 12F, 8F)
                .texOffs(45, 0).addBox(-4F, -4F, -6F, 8F, 8F, 12F)
                .texOffs(0, 0).addBox(-5.5F, -5.5F, -5.5F, 11F, 11F, 11F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ring = p_planet.addOrReplaceChild("ring", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.3142F, 0F, 0.4189F));
        PartDefinition p_ring_0 = p_ring.addOrReplaceChild("ring_0", CubeListBuilder.create()
                .texOffs(41, 23).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(10.5F, 0F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_ring_1 = p_ring.addOrReplaceChild("ring_1", CubeListBuilder.create()
                .texOffs(58, 23).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(9.9861F, 0F, 3.2447F, 0F, -1.885F, 0F));
        PartDefinition p_ring_2 = p_ring.addOrReplaceChild("ring_2", CubeListBuilder.create()
                .texOffs(75, 23).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(8.4947F, 0F, 6.1717F, 0F, -2.1991F, 0F));
        PartDefinition p_ring_3 = p_ring.addOrReplaceChild("ring_3", CubeListBuilder.create()
                .texOffs(92, 23).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(6.1717F, 0F, 8.4947F, 0F, -2.5133F, 0F));
        PartDefinition p_ring_4 = p_ring.addOrReplaceChild("ring_4", CubeListBuilder.create()
                .texOffs(109, 23).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(3.2447F, 0F, 9.9861F, 0F, -2.8274F, 0F));
        PartDefinition p_ring_5 = p_ring.addOrReplaceChild("ring_5", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 10.5F, 0F, -3.1416F, 0F));
        PartDefinition p_ring_6 = p_ring.addOrReplaceChild("ring_6", CubeListBuilder.create()
                .texOffs(17, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-3.2447F, 0F, 9.9861F, 0F, -3.4558F, 0F));
        PartDefinition p_ring_7 = p_ring.addOrReplaceChild("ring_7", CubeListBuilder.create()
                .texOffs(34, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-6.1717F, 0F, 8.4947F, 0F, -3.7699F, 0F));
        PartDefinition p_ring_8 = p_ring.addOrReplaceChild("ring_8", CubeListBuilder.create()
                .texOffs(51, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-8.4947F, 0F, 6.1717F, 0F, -4.0841F, 0F));
        PartDefinition p_ring_9 = p_ring.addOrReplaceChild("ring_9", CubeListBuilder.create()
                .texOffs(68, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-9.9861F, 0F, 3.2447F, 0F, -4.3982F, 0F));
        PartDefinition p_ring_10 = p_ring.addOrReplaceChild("ring_10", CubeListBuilder.create()
                .texOffs(85, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-10.5F, 0F, 0F, 0F, -4.7124F, 0F));
        PartDefinition p_ring_11 = p_ring.addOrReplaceChild("ring_11", CubeListBuilder.create()
                .texOffs(102, 40).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-9.9861F, 0F, -3.2447F, 0F, -5.0265F, 0F));
        PartDefinition p_ring_12 = p_ring.addOrReplaceChild("ring_12", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-8.4947F, 0F, -6.1717F, 0F, -5.3407F, 0F));
        PartDefinition p_ring_13 = p_ring.addOrReplaceChild("ring_13", CubeListBuilder.create()
                .texOffs(17, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-6.1717F, 0F, -8.4947F, 0F, -5.6549F, 0F));
        PartDefinition p_ring_14 = p_ring.addOrReplaceChild("ring_14", CubeListBuilder.create()
                .texOffs(34, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(-3.2447F, 0F, -9.9861F, 0F, -5.969F, 0F));
        PartDefinition p_ring_15 = p_ring.addOrReplaceChild("ring_15", CubeListBuilder.create()
                .texOffs(51, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -10.5F, 0F, -6.2832F, 0F));
        PartDefinition p_ring_16 = p_ring.addOrReplaceChild("ring_16", CubeListBuilder.create()
                .texOffs(68, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(3.2447F, 0F, -9.9861F, 0F, -6.5973F, 0F));
        PartDefinition p_ring_17 = p_ring.addOrReplaceChild("ring_17", CubeListBuilder.create()
                .texOffs(85, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(6.1717F, 0F, -8.4947F, 0F, -6.9115F, 0F));
        PartDefinition p_ring_18 = p_ring.addOrReplaceChild("ring_18", CubeListBuilder.create()
                .texOffs(102, 46).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(8.4947F, 0F, -6.1717F, 0F, -7.2257F, 0F));
        PartDefinition p_ring_19 = p_ring.addOrReplaceChild("ring_19", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-2F, -0.5F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.0507F, -0.35F, -0.4F)),
                PartPose.offsetAndRotation(9.9861F, 0F, -3.2447F, 0F, -7.5398F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
