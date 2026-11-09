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
                .texOffs(0, 0).addBox(-0.5F, -1F, -13F, 1F, 2F, 11F, new CubeDeformation(-0.3F, -0.35F, 0F))
                .texOffs(57, 0).addBox(-0.5F, -0.5F, -14.75F, 1F, 1F, 2F, new CubeDeformation(-0.35F, -0.05F, -0.25F))
                .texOffs(0, 14).addBox(-0.5F, -0.5F, -15.5F, 1F, 1F, 1F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(25, 0).addBox(-0.5F, -2.5F, -2.45F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(30, 0).addBox(-1F, -1F, -2.95F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F))
                .texOffs(39, 0).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.15F, 0F))
                .texOffs(48, 0).addBox(-1F, -1F, 1.1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
