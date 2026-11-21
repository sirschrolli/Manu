package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Kaulquappe (Knochen wie bei Vanilla, Flossensaum durchscheinend).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PolliwogGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private PolliwogGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-1.5F, -1F, 0F, 3F, 2F, 3F)
                .texOffs(13, 13).addBox(-1F, -1.5F, 0.5F, 2F, 1F, 2F)
                .texOffs(22, 13).addBox(-1F, 0.5F, 0.5F, 2F, 1F, 2F)
                .texOffs(0, 19).addBox(-1F, -0.5F, -0.5F, 2F, 1F, 1F)
                .texOffs(7, 19).addBox(-2F, -0.9F, 0.5F, 1F, 1F, 1F)
                .texOffs(12, 19).addBox(1F, -0.9F, 0.5F, 1F, 1F, 1F),
                PartPose.offset(0F, 22F, -3F));
        PartDefinition p_tail = root.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(17, 0).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 5F)
                .texOffs(0, 0).addBox(0F, -2F, 0F, 0F, 4F, 8F),
                PartPose.offset(0F, 22F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
