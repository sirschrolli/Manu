package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Plagegeister (Knochen wie beim Vanilla-Plagegeist, Kapuze und Fetzen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VexGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private VexGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0F, -2.5F, 0F));
        PartDefinition p_head = p_root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-2.5F, -5F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_hood = p_head.addOrReplaceChild("hood", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-2.5F, -5F, -2.5F, 5F, 5F, 5F, new CubeDeformation(0.55F))
                .texOffs(0, 32).addBox(-2.1F, -5.95F, -3.35F, 4.2F, 1F, 1.3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hood_tip = p_hood.addOrReplaceChild("hood_tip", CubeListBuilder.create()
                .texOffs(32, 14).addBox(-1.5F, -2.2F, -1.6F, 3F, 2.2F, 2.2F),
                PartPose.offsetAndRotation(0F, -5.3F, 2.4F, -0.6632F, 0F, 0F));
        PartDefinition p_hood_tail = p_hood_tip.addOrReplaceChild("hood_tail", CubeListBuilder.create()
                .texOffs(35, 25).addBox(-0.7F, -1.8F, -0.7F, 1.4F, 1.8F, 1.4F),
                PartPose.offsetAndRotation(0F, -2F, 0.2F, -0.5236F, 0F, 0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(45, 14).addBox(-1.5F, 0F, -1F, 3F, 4F, 2F)
                .texOffs(21, 14).addBox(-1.5F, 1F, -1F, 3F, 5F, 2F, new CubeDeformation(-0.2F)),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_mantle = p_body.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(9, 25).addBox(-2F, -0.4F, -1.4F, 4F, 1.6F, 2.8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tatters = p_body.addOrReplaceChild("tatters", CubeListBuilder.create()
                .texOffs(24, 25).addBox(-1.3F, 0F, -0.8F, 2.6F, 3F, 1.6F),
                PartPose.offsetAndRotation(0F, 5.4F, 0F, 0.1396F, 0F, 0F));
        PartDefinition p_wisp = p_tatters.addOrReplaceChild("wisp", CubeListBuilder.create()
                .texOffs(44, 25).addBox(-0.7F, 0F, -0.45F, 1.4F, 2.6F, 0.9F),
                PartPose.offsetAndRotation(0F, 2.8F, 0.1F, 0.2094F, 0F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(56, 14).addBox(-1.25F, -0.5F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.1F)),
                PartPose.offset(-1.75F, 0.25F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-0.75F, -0.5F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.1F)),
                PartPose.offset(1.75F, 0.25F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0F, 0F, 0F, 5F, 8F),
                PartPose.offset(0.5F, 1F, 1F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(17, 0).addBox(0F, 0F, 0F, 0F, 5F, 8F),
                PartPose.offset(-0.5F, 1F, 1F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
