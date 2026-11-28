package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Lichttaube (MissMystik).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class LightDoveGeometry {
    public static final int TEXTURE_WIDTH = 32;
    public static final int TEXTURE_HEIGHT = 32;

    private LightDoveGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_dove = root.addOrReplaceChild("dove", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -1.5F, -2.5F, 3F, 3F, 5F, new CubeDeformation(-0.25F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_dove.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-1F, -1.75F, -1.75F, 2F, 2F, 2F)
                .texOffs(9, 21).addBox(-0.5F, -0.95F, -2.55F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.05F)),
                PartPose.offset(0F, -0.9F, -2.3F));
        PartDefinition p_tail = p_dove.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(19, 15).addBox(-1.5F, -0.5F, 0F, 3F, 1F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, -0.5F, 2.4F));
        PartDefinition p_left_wing = p_dove.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 9).addBox(0F, -0.5F, -1.5F, 5F, 1F, 4F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offset(1.25F, -0.9F, -0.6F));
        PartDefinition p_right_wing = p_dove.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 15).addBox(-5F, -0.5F, -1.5F, 5F, 1F, 4F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offset(-1.25F, -0.9F, -0.6F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
