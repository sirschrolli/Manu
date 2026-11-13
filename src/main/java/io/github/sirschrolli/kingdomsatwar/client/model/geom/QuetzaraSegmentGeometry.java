package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Glieds von Quetzaras Leib (1.10; je nach Glied dicker oder dünner gezeichnet).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class QuetzaraSegmentGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private QuetzaraSegmentGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -7F, -8F, 14F, 14F, 16F)
                .texOffs(0, 31).addBox(-5.5F, 6.75F, -8F, 11F, 1F, 16F, new CubeDeformation(-0.3F, -0.05F, -0.4F))
                .texOffs(55, 31).addBox(-1.5F, -7.85F, -7.5F, 3F, 1F, 15F, new CubeDeformation(-0.1F, -0.05F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body_oct = p_body.addOrReplaceChild("body_oct", CubeListBuilder.create()
                .texOffs(61, 0).addBox(-6.5F, -6.5F, -7.5F, 13F, 13F, 15F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_tuft = p_body.addOrReplaceChild("tuft", CubeListBuilder.create()
                .texOffs(92, 31).addBox(-1.5F, -8.75F, -0.5F, 3F, 9F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -7.5F, -2.5F, -1.117F, 0F, 0F));
        PartDefinition p_tuft2 = p_body.addOrReplaceChild("tuft2", CubeListBuilder.create()
                .texOffs(101, 31).addBox(-1.5F, -6F, -0.5F, 3F, 6F, 1F, new CubeDeformation(-0.4F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -7.5F, 2.5F, -1.2566F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
