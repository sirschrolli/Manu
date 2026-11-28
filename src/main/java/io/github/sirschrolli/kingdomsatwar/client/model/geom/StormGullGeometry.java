package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Sturmmöwe (Ruhehaltung: Flügel ausgebreitet).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class StormGullGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 48;

    private StormGullGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -2F, -4.1F, 5F, 4F, 8F, new CubeDeformation(-0.4F, 0F, -0.2F))
                .texOffs(36, 20).addBox(-2F, -1.7F, -4.75F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.05F))
                .texOffs(23, 20).addBox(-2F, -1.75F, 3.5F, 4F, 3F, 2F, new CubeDeformation(-0.4F, -0.15F, -0.1F)),
                PartPose.offset(0F, 18.2F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(27, 0).addBox(-1.5F, -3.75F, -3.3F, 3F, 4F, 4F, new CubeDeformation(-0.05F, -0.45F, -0.4F))
                .texOffs(37, 26).addBox(-0.5F, -2.175F, -5.6F, 1F, 1F, 3F, new CubeDeformation(-0.05F, -0.125F, -0.2F))
                .texOffs(55, 26).addBox(-0.5F, -1.7F, -5.7F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.25F))
                .texOffs(60, 26).addBox(0.96F, -2.75F, -2.15F, 1F, 1F, 1F, new CubeDeformation(-0.44F, -0.15F, -0.15F))
                .texOffs(0, 31).addBox(-1.96F, -2.75F, -2.15F, 1F, 1F, 1F, new CubeDeformation(-0.44F, -0.15F, -0.15F)),
                PartPose.offset(0F, -1.3F, -3.6F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(46, 26).addBox(-0.5F, -0.25F, -2.65F, 1F, 1F, 3F, new CubeDeformation(-0.1F, -0.25F, -0.35F)),
                PartPose.offset(0F, -1.3F, -2.9F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(42, 0).addBox(0F, -0.5F, -0.9F, 5F, 1F, 5F, new CubeDeformation(0F, -0.2F, -0.3F)),
                PartPose.offset(2F, -1.6F, -2.2F));
        PartDefinition p_wing_l_tip = p_wing_l.addOrReplaceChild("wing_l_tip", CubeListBuilder.create()
                .texOffs(21, 13).addBox(-0.3F, -0.5F, -0.6F, 7F, 1F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.2F)),
                PartPose.offset(5F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 13).addBox(-5F, -0.5F, -0.9F, 5F, 1F, 5F, new CubeDeformation(0F, -0.2F, -0.3F)),
                PartPose.offset(-2F, -1.6F, -2.2F));
        PartDefinition p_wing_r_tip = p_wing_r.addOrReplaceChild("wing_r_tip", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-6.7F, -0.5F, -0.6F, 7F, 1F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.2F)),
                PartPose.offset(-5F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-2F, -0.5F, -0.4F, 4F, 1F, 3F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offset(0F, -1.1F, 5F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(47, 20).addBox(-0.5F, -0.1F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(15, 26).addBox(-1F, 3.4F, -2.15F, 2F, 1F, 3F, new CubeDeformation(-0.05F, -0.4F, -0.45F)),
                PartPose.offset(0.9F, 20F, 0.6F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(52, 20).addBox(-0.5F, -0.1F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(26, 26).addBox(-1F, 3.4F, -2.15F, 2F, 1F, 3F, new CubeDeformation(-0.05F, -0.4F, -0.45F)),
                PartPose.offset(-0.9F, 20F, 0.6F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
