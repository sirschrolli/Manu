package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kristallsplitters (Geschoss des Splitterwächters).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WardenShardGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private WardenShardGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_shard = root.addOrReplaceChild("shard", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -1.5F, -3.5F, 3F, 3F, 7F, new CubeDeformation(-0.4F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_tip = p_shard.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(21, 0).addBox(-1F, -1F, -3F, 2F, 2F, 3F, new CubeDeformation(-0.35F, -0.35F, 0F)),
                PartPose.offset(0F, 0F, -3.5F));
        PartDefinition p_tail = p_shard.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 11).addBox(-1F, -1F, -0.1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.1F)),
                PartPose.offset(0F, 0F, 3.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
