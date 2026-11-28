package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie eines Zahnrads des Stundenwächters (seit 1.01): zwei Scheiben mit zwölf Zähnen und fünf Fenstern, dazwischen die Nabe mit Chronit; rollt entlang -z, dreht sich um x.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ClockGearGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private ClockGearGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_gear = root.addOrReplaceChild("gear", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.3F, -10.5F, -10.5F, 1F, 21F, 21F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(45, 0).addBox(0.3F, -10.5F, -10.5F, 1F, 21F, 21F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(90, 0).addBox(-1F, -2F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
