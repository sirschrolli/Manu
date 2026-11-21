package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Fee (Skelett des Hilfsgeists).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FairyGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private FairyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0F, 23.5F, 0F));
        PartDefinition p_head = p_root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-2.5F, -5F, -2.5F, 5F, 5F, 5F)
                .texOffs(0, 48).addBox(-3.2F, -3.8F, -0.4F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(5, 48).addBox(2.2F, -3.8F, -0.4F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(0, 34).addBox(-3F, -5.4F, -2.05F, 6F, 1F, 5F, new CubeDeformation(-0.3F, 0F, -0.05F))
                .texOffs(21, 23).addBox(-3F, -5.4F, 1.05F, 6F, 7F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.25F))
                .texOffs(38, 23).addBox(-3.1F, -5.1F, -2F, 1F, 5F, 4F, new CubeDeformation(-0.25F, -0.3F, -0.4F))
                .texOffs(49, 23).addBox(2.1F, -5.1F, -2F, 1F, 5F, 4F, new CubeDeformation(-0.25F, -0.3F, -0.4F))
                .texOffs(10, 48).addBox(-3F, -5.95F, -2.85F, 6F, 1F, 1F, new CubeDeformation(-0.1F, -0.05F, -0.05F))
                .texOffs(0, 41).addBox(-2.95F, -5.95F, -3F, 1F, 1F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.2F))
                .texOffs(13, 41).addBox(1.95F, -5.95F, -3F, 1F, 1F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.2F)),
                PartPose.offset(0F, -3.99F, 0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(26, 41).addBox(-1.5F, -0.4F, -1F, 3F, 4F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(44, 34).addBox(-2F, 2F, -1.5F, 4F, 3F, 3F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(23, 34).addBox(-3F, 4.2F, -2F, 6F, 2F, 4F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(37, 41).addBox(-0.7F, -0.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.11F, -0.01F, -0.21F))
                .texOffs(51, 41).addBox(-0.7F, 2.4F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.11F, -0.11F, -0.31F)),
                PartPose.offset(-1.75F, 0.5F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, -2.4F, 0F, 0F, 10F, 12F),
                PartPose.offset(-0.5F, 0F, 0.6F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(44, 41).addBox(-0.3F, -0.5F, -1F, 1F, 3F, 2F, new CubeDeformation(-0.11F, -0.01F, -0.21F))
                .texOffs(58, 41).addBox(-0.3F, 2.4F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.11F, -0.11F, -0.31F)),
                PartPose.offset(1.75F, 0.5F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(25, 0).addBox(0F, -2.4F, 0F, 0F, 10F, 12F),
                PartPose.offset(0.5F, 0F, 0.6F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
