package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schwanzes des Weltenfressers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WorldEaterTailGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private WorldEaterTailGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13F, -13F, -16F, 26F, 26F, 20F)
                .texOffs(161, 47).addBox(-14F, -16.5F, -14F, 28F, 4F, 16F)
                .texOffs(109, 82).addBox(-14.5F, -14.5F, -16.9F, 29F, 2F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(172, 82).addBox(-14.5F, 12.5F, -16.9F, 29F, 2F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(92, 47).addBox(-14.5F, -12.5F, -16.9F, 2F, 25F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(101, 47).addBox(12.5F, -12.5F, -16.9F, 2F, 25F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail_oct = p_tail.addOrReplaceChild("tail_oct", CubeListBuilder.create()
                .texOffs(116, 0).addBox(-12.5F, -12.5F, -15F, 25F, 25F, 18F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_tail2 = p_tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-9F, -9F, 0F, 18F, 18F, 16F)
                .texOffs(19, 82).addBox(-10F, -11.5F, 2F, 20F, 3F, 12F),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0.0698F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(110, 47).addBox(-5.5F, -5.5F, 0F, 11F, 11F, 14F),
                PartPose.offsetAndRotation(0F, 0F, 16F, 0.1047F, 0F, 0F));
        PartDefinition p_fin = p_tail3.addOrReplaceChild("fin", CubeListBuilder.create()
                .texOffs(93, 0).addBox(-1.5F, -18F, 0F, 3F, 36F, 8F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(69, 47).addBox(-1.5F, -12F, 8F, 3F, 24F, 8F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(0, 82).addBox(-1.5F, -6F, 16F, 3F, 12F, 6F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(84, 82).addBox(-3F, -3F, -1F, 6F, 6F, 6F),
                PartPose.offset(0F, 0F, 14F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
