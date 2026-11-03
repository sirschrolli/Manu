package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Steinkugel von Katapult und Tribok (Drehpunkt in der Mitte; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SiegeStoneGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private SiegeStoneGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_stone = root.addOrReplaceChild("stone", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-5F, -4F, -4.25F, 10F, 8F, 9F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(0, 19).addBox(-4F, -5F, -4F, 8F, 10F, 8F)
                .texOffs(0, 0).addBox(-4.25F, -4.25F, -5F, 9F, 8F, 10F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(0, 56).addBox(-2.25F, -6.25F, -2.75F, 4F, 2F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(39, 38).addBox(4.25F, -1.75F, -2.25F, 2F, 4F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(17, 56).addBox(-3F, 4.25F, 0F, 3F, 2F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(52, 38).addBox(-6.25F, -2.75F, 0.75F, 2F, 4F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
