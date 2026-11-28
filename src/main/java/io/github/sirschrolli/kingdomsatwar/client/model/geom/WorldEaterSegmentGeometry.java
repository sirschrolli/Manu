package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Leibglieds des Weltenfressers (je Glied ist eines der vier Weltstücke zu sehen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WorldEaterSegmentGeometry {
    public static final int TEXTURE_WIDTH = 512;
    public static final int TEXTURE_HEIGHT = 256;

    private WorldEaterSegmentGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-15F, -15F, -16F, 30F, 30F, 32F)
                .texOffs(366, 0).addBox(-17F, -19F, -14F, 34F, 5F, 28F)
                .texOffs(0, 63).addBox(-13F, 14F, -14F, 26F, 4F, 28F)
                .texOffs(244, 0).addBox(15F, -12F, -13F, 4F, 22F, 26F)
                .texOffs(305, 0).addBox(-19F, -12F, -13F, 4F, 22F, 26F)
                .texOffs(363, 96).addBox(-16.5F, -16.5F, -16.8F, 33F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(434, 96).addBox(-16.5F, 14.5F, -16.8F, 33F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(109, 63).addBox(-16.5F, -14.5F, -16.8F, 2F, 29F, 2F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(118, 63).addBox(14.5F, -14.5F, -16.8F, 2F, 29F, 2F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body_oct = p_body.addOrReplaceChild("body_oct", CubeListBuilder.create()
                .texOffs(125, 0).addBox(-14.5F, -14.5F, -15F, 29F, 29F, 30F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_side_crystal_l = p_body.addOrReplaceChild("side_crystal_l", CubeListBuilder.create()
                .texOffs(145, 96).addBox(-2F, -9F, -2F, 4F, 9F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(18F, -6F, 2F, -0.3491F, 0F, -1.0123F));
        PartDefinition p_side_crystal_r = p_body.addOrReplaceChild("side_crystal_r", CubeListBuilder.create()
                .texOffs(162, 96).addBox(-2F, -9F, -2F, 4F, 9F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-18F, -6F, 2F, -0.3491F, 0F, 1.0123F));
        PartDefinition p_world_overworld = p_body.addOrReplaceChild("world_overworld", CubeListBuilder.create()
                .texOffs(127, 63).addBox(-11F, -7F, -11F, 22F, 7F, 22F)
                .texOffs(91, 96).addBox(-1.5F, -19F, -1.5F, 3F, 12F, 3F)
                .texOffs(0, 96).addBox(-7F, -28F, -7F, 14F, 10F, 14F)
                .texOffs(196, 96).addBox(-4F, -31F, -4F, 8F, 3F, 8F),
                PartPose.offsetAndRotation(0F, -19F, 0F, 0.0698F, 0.1396F, -0.1047F));
        PartDefinition p_world_nether = p_body.addOrReplaceChild("world_nether", CubeListBuilder.create()
                .texOffs(216, 63).addBox(-11F, -7F, -11F, 22F, 7F, 22F)
                .texOffs(298, 96).addBox(-6F, -8F, -5F, 7F, 1F, 7F)
                .texOffs(229, 96).addBox(3F, -15F, 1F, 2F, 8F, 2F)
                .texOffs(104, 96).addBox(-1F, -19F, -3F, 10F, 4F, 10F),
                PartPose.offsetAndRotation(0F, -19F, 0F, -0.0873F, -0.1745F, 0.1222F));
        PartDefinition p_world_end = p_body.addOrReplaceChild("world_end", CubeListBuilder.create()
                .texOffs(305, 63).addBox(-11F, -7F, -11F, 22F, 7F, 22F)
                .texOffs(78, 96).addBox(-1.5F, -21F, -1.5F, 3F, 14F, 3F)
                .texOffs(327, 96).addBox(-7.5F, -16F, -1.5F, 6F, 3F, 3F)
                .texOffs(259, 96).addBox(-7.5F, -22F, -1.5F, 3F, 6F, 3F)
                .texOffs(346, 96).addBox(1.5F, -18F, -1.5F, 5F, 3F, 3F)
                .texOffs(272, 96).addBox(3.5F, -24F, -1.5F, 3F, 6F, 3F)
                .texOffs(238, 96).addBox(-2.5F, -25F, -2.5F, 5F, 4F, 5F),
                PartPose.offsetAndRotation(0F, -19F, 0F, 0.1047F, 0.2443F, 0.0873F));
        PartDefinition p_world_frost = p_body.addOrReplaceChild("world_frost", CubeListBuilder.create()
                .texOffs(394, 63).addBox(-11F, -7F, -11F, 22F, 7F, 22F)
                .texOffs(57, 96).addBox(-6F, -22F, -4F, 5F, 15F, 5F)
                .texOffs(179, 96).addBox(2F, -16F, 1F, 4F, 9F, 4F)
                .texOffs(285, 96).addBox(-1F, -27F, -2.5F, 3F, 6F, 3F),
                PartPose.offsetAndRotation(0F, -19F, 0F, -0.0698F, -0.2793F, -0.0873F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
