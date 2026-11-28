package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Schwerts der Schwerterkiste (MissMystik).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MagicSwordGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private MagicSwordGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_sword = root.addOrReplaceChild("sword", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -1F, -13F, 1F, 2F, 11F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(57, 0).addBox(-0.5F, -1F, -14.75F, 1F, 2F, 2F, new CubeDeformation(-0.25F, -0.3F, -0.25F))
                .texOffs(0, 14).addBox(-0.5F, -0.5F, -16.1F, 1F, 1F, 2F, new CubeDeformation(-0.3F, -0.15F, -0.4F))
                .texOffs(25, 0).addBox(-0.5F, -3F, -2.5F, 1F, 6F, 1F)
                .texOffs(30, 0).addBox(-1F, -1F, -3F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.35F, -0.35F))
                .texOffs(39, 0).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(48, 0).addBox(-1F, -1F, 1.2F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
