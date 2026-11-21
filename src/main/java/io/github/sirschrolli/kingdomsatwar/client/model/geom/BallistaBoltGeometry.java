package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Ballistenbolzens (Drehpunkt in der Mitte, Spitze nach -Z; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BallistaBoltGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private BallistaBoltGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bolt = root.addOrReplaceChild("bolt", CubeListBuilder.create()
                .texOffs(14, 31).addBox(-0.5F, -0.5F, -13.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(0, 31).addBox(-0.5F, -0.5F, -12.75F, 1F, 1F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(41, 21).addBox(-1F, -1F, -11F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(30, 21).addBox(-1.5F, -1.5F, -9.25F, 3F, 3F, 2F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(50, 21).addBox(-1F, -1F, -7.5F, 2F, 2F, 2F)
                .texOffs(0, 0).addBox(-1F, -1F, -5.75F, 2F, 2F, 18F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(7, 31).addBox(-1F, -1F, 12F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bolt_fin_a = p_bolt.addOrReplaceChild("bolt_fin_a", CubeListBuilder.create()
                .texOffs(41, 0).addBox(-0.5F, 0.3F, 5.5F, 1F, 3F, 6F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_bolt_fin_b = p_bolt.addOrReplaceChild("bolt_fin_b", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-0.5F, 0.3F, 5.5F, 1F, 3F, 6F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 2.0944F));
        PartDefinition p_bolt_fin_c = p_bolt.addOrReplaceChild("bolt_fin_c", CubeListBuilder.create()
                .texOffs(15, 21).addBox(-0.5F, 0.3F, 5.5F, 1F, 3F, 6F, new CubeDeformation(-0.25F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -2.0944F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
