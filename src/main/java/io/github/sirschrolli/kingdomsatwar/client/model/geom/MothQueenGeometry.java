package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Mottenkönigin (im Spiel 2,5-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MothQueenGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private MothQueenGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(63, 33).addBox(-3F, -3F, -4F, 6F, 6F, 7F)
                .texOffs(90, 33).addBox(-3.5F, -4F, -4.6F, 7F, 3F, 8F, new CubeDeformation(0F, -0.4F, -0.2F)),
                PartPose.offset(0F, 12F, 0F));
        PartDefinition p_abdomen = p_body.addOrReplaceChild("abdomen", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-2.5F, -2.4F, 0F, 5F, 5F, 5F),
                PartPose.offset(0F, 0.5F, 3F));
        PartDefinition p_tail = p_abdomen.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(21, 47).addBox(-2F, -2F, -0.25F, 4F, 4F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.25F))
                .texOffs(0, 58).addBox(-1F, -1F, 4.2F, 2F, 2F, 2F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, 0.2F, 5F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(40, 47).addBox(-2.5F, -2.5F, -3.6F, 5F, 5F, 4F, new CubeDeformation(-0.3F, -0.3F, -0.2F))
                .texOffs(89, 47).addBox(-3.45F, -1.9F, -3.4F, 2F, 3F, 3F, new CubeDeformation(-0.15F, 0F, 0F))
                .texOffs(100, 47).addBox(1.45F, -1.9F, -3.4F, 2F, 3F, 3F, new CubeDeformation(-0.15F, 0F, 0F))
                .texOffs(111, 47).addBox(-2F, -3.8F, -3.2F, 4F, 2F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(9, 58).addBox(-1F, -5.2F, -2.9F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(18, 58).addBox(-1.95F, -4.4F, -2.4F, 1F, 1F, 1F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(23, 58).addBox(0.95F, -4.4F, -2.4F, 1F, 1F, 1F, new CubeDeformation(-0.05F, 0F, 0F)),
                PartPose.offset(0F, -0.5F, -4F));
        PartDefinition p_left_antenna = p_head.addOrReplaceChild("left_antenna", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-0.5F, -9F, -2.5F, 1F, 9F, 5F, new CubeDeformation(-0.01F)),
                PartPose.offsetAndRotation(1F, -2.2F, -2.8F, -0.4363F, -0.3142F, 0.384F));
        PartDefinition p_right_antenna = p_head.addOrReplaceChild("right_antenna", CubeListBuilder.create()
                .texOffs(13, 18).addBox(-0.5F, -9F, -2.5F, 1F, 9F, 5F, new CubeDeformation(-0.01F)),
                PartPose.offsetAndRotation(-1F, -2.2F, -2.8F, -0.4363F, 0.3142F, -0.384F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(26, 18).addBox(0F, 0F, -7F, 18F, 0F, 13F),
                PartPose.offset(2.6F, -2F, -1.5F));
        PartDefinition p_left_hind_wing = p_body.addOrReplaceChild("left_hind_wing", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0F, 0F, 13F, 0F, 17F),
                PartPose.offset(2.4F, -1F, 1.5F));
        PartDefinition p_left_leg0 = p_body.addOrReplaceChild("left_leg0", CubeListBuilder.create()
                .texOffs(59, 47).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(2.4F, 2.6F, -2.5F, 0F, 0F, 0.6109F));
        PartDefinition p_left_leg1 = p_body.addOrReplaceChild("left_leg1", CubeListBuilder.create()
                .texOffs(64, 47).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(2.4F, 2.6F, 0F, 0F, 0F, 0.6109F));
        PartDefinition p_left_leg2 = p_body.addOrReplaceChild("left_leg2", CubeListBuilder.create()
                .texOffs(69, 47).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(2.4F, 2.6F, 2.5F, 0F, 0F, 0.6109F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-18F, 0F, -7F, 18F, 0F, 13F),
                PartPose.offset(-2.6F, -2F, -1.5F));
        PartDefinition p_right_hind_wing = p_body.addOrReplaceChild("right_hind_wing", CubeListBuilder.create()
                .texOffs(61, 0).addBox(-13F, 0F, 0F, 13F, 0F, 17F),
                PartPose.offset(-2.4F, -1F, 1.5F));
        PartDefinition p_right_leg0 = p_body.addOrReplaceChild("right_leg0", CubeListBuilder.create()
                .texOffs(74, 47).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-2.4F, 2.6F, -2.5F, 0F, 0F, -0.6109F));
        PartDefinition p_right_leg1 = p_body.addOrReplaceChild("right_leg1", CubeListBuilder.create()
                .texOffs(79, 47).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-2.4F, 2.6F, 0F, 0F, 0F, -0.6109F));
        PartDefinition p_right_leg2 = p_body.addOrReplaceChild("right_leg2", CubeListBuilder.create()
                .texOffs(84, 47).addBox(-0.5F, -0.25F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-2.4F, 2.6F, 2.5F, 0F, 0F, -0.6109F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
