package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Seelenschädels, den der Aschenkönig abfeuert (Knochen "head" wie beim Vanilla-Schädel, mit Flammenschweif).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SeelenschaedelGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private SeelenschaedelGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-3.5F, -7.5F, -3.5F, 7F, 7F, 7F)
                .texOffs(19, 36).addBox(-3.5F, -5.65F, -4.175F, 7F, 1F, 1F, new CubeDeformation(0F, -0.05F, -0.325F))
                .texOffs(0, 36).addBox(-2.5F, -0.7F, -3.65F, 5F, 2F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.25F))
                .texOffs(0, 0).addBox(0F, -10F, -0.5F, 0F, 10F, 12F)
                .texOffs(0, 23).addBox(-5.5F, -4F, -0.5F, 11F, 0F, 12F),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
