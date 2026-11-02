package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Leermilbe (vier Segmente wie bei der Vanilla-Endermite, Beine, Fühler, Dornen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class LeermilbeGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private LeermilbeGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_segment0 = root.addOrReplaceChild("segment0", CubeListBuilder.create()
                .texOffs(23, 0).addBox(-2F, 0F, -1F, 4F, 3F, 2F)
                .texOffs(36, 0).addBox(-1.85F, -0.45F, -3.8F, 1F, 1F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.2F))
                .texOffs(45, 0).addBox(0.85F, -0.45F, -3.8F, 1F, 1F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.2F))
                .texOffs(35, 15).addBox(-1.5F, 2F, -1.9F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(40, 15).addBox(0.5F, 2F, -1.9F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offset(0F, 21F, -3.5F));
        PartDefinition p_segment1 = root.addOrReplaceChild("segment1", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, 0F, -2.5F, 6F, 4F, 5F)
                .texOffs(57, 10).addBox(-4.6F, 2.35F, -2.1F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(12, 10).addBox(-4.85F, 2.3F, -2.1F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(0, 15).addBox(2.6F, 2.35F, -2.1F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(17, 10).addBox(3.85F, 2.3F, -2.1F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(7, 15).addBox(-4.6F, 2.35F, -0.4F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(22, 10).addBox(-4.85F, 2.3F, -0.4F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(14, 15).addBox(2.6F, 2.35F, -0.4F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(27, 10).addBox(3.85F, 2.3F, -0.4F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(21, 15).addBox(-4.6F, 2.35F, 1.3F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(32, 10).addBox(-4.85F, 2.3F, 1.3F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(28, 15).addBox(2.6F, 2.35F, 1.3F, 2F, 1F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.25F))
                .texOffs(37, 10).addBox(3.85F, 2.3F, 1.3F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(42, 10).addBox(-1.7F, -1.8F, -1.7F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F))
                .texOffs(0, 10).addBox(0.1F, -2.6F, -0.3F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.4F, -0.15F))
                .texOffs(47, 10).addBox(-0.9F, -1.7F, 0.9F, 1F, 2F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F)),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_segment2 = root.addOrReplaceChild("segment2", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1.5F, 0F, -0.5F, 3F, 3F, 1F),
                PartPose.offset(0F, 21F, 3F));
        PartDefinition p_segment3 = root.addOrReplaceChild("segment3", CubeListBuilder.create()
                .texOffs(52, 10).addBox(-0.5F, 0F, -0.5F, 1F, 2F, 1F)
                .texOffs(5, 10).addBox(-0.5F, 0.4F, 0.4F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.1F)),
                PartPose.offset(0F, 22F, 4F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
