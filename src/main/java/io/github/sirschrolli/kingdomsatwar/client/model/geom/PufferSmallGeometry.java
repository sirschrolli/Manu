package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kugelfischs (klein, Knochen wie beim Vanilla-Kugelfisch).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PufferSmallGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private PufferSmallGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -3F, -2F, 3F, 3F, 3F)
                .texOffs(18, 0).addBox(-1F, -2.5F, 1F, 2F, 2F, 1F)
                .texOffs(25, 0).addBox(-1F, -2F, -2.75F, 2F, 1F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(24, 7).addBox(0F, -4F, 0F, 0F, 1F, 1F)
                .texOffs(27, 7).addBox(0F, 0F, 0F, 0F, 1F, 1F),
                PartPose.offset(0F, 23F, 0F));
        PartDefinition p_right_eye = root.addOrReplaceChild("right_eye", CubeListBuilder.create()
                .texOffs(14, 7).addBox(-2F, -0.5F, -1.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_left_eye = root.addOrReplaceChild("left_eye", CubeListBuilder.create()
                .texOffs(19, 7).addBox(1F, -0.5F, -1.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_back_fin = root.addOrReplaceChild("back_fin", CubeListBuilder.create()
                .texOffs(13, 0).addBox(0F, -2F, 0.5F, 0F, 3F, 2F),
                PartPose.offset(0F, 22F, 1.5F));
        PartDefinition p_right_fin = root.addOrReplaceChild("right_fin", CubeListBuilder.create()
                .texOffs(0, 7).addBox(-1F, 0F, 0F, 1F, 0F, 2F),
                PartPose.offset(-1.5F, 22F, -1.5F));
        PartDefinition p_left_fin = root.addOrReplaceChild("left_fin", CubeListBuilder.create()
                .texOffs(7, 7).addBox(0F, 0F, 0F, 1F, 0F, 2F),
                PartPose.offset(1.5F, 22F, -1.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
