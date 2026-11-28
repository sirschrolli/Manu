package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Sternenrochens (Schwingen in drei Gliedern, Peitschenschwanz).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class StarRayGeometry {
    public static final int TEXTURE_WIDTH = 192;
    public static final int TEXTURE_HEIGHT = 128;

    private StarRayGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5.5F, -2F, -8.5F, 11F, 4F, 17F)
                .texOffs(57, 0).addBox(-7F, -1.5F, -6.5F, 14F, 3F, 13F)
                .texOffs(55, 22).addBox(-3.5F, -2.75F, -6F, 7F, 1F, 11F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 19F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(67, 39).addBox(-5F, -1.5F, -3.75F, 10F, 3F, 4F, new CubeDeformation(0F, -0.1F, -0.25F))
                .texOffs(53, 51).addBox(4.8F, -1.4F, -3F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(60, 51).addBox(-5.8F, -1.4F, -3F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0.2F, -8.5F));
        PartDefinition p_left_cephalic = p_head.addOrReplaceChild("left_cephalic", CubeListBuilder.create()
                .texOffs(0, 51).addBox(-1F, -0.9F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.3F, 0F)),
                PartPose.offsetAndRotation(4F, 0F, -3.2F, 0.1047F, 0.1047F, 0F));
        PartDefinition p_left_cephalic_curl = p_left_cephalic.addOrReplaceChild("left_cephalic_curl", CubeListBuilder.create()
                .texOffs(26, 51).addBox(-1F, -0.95F, -1.8F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.45F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.6F, -4F, -0.9599F, 0F, 0F));
        PartDefinition p_right_cephalic = p_head.addOrReplaceChild("right_cephalic", CubeListBuilder.create()
                .texOffs(13, 51).addBox(-1F, -0.9F, -4F, 2F, 2F, 4F, new CubeDeformation(-0.2F, -0.3F, 0F)),
                PartPose.offsetAndRotation(-4F, 0F, -3.2F, 0.1047F, -0.1047F, 0F));
        PartDefinition p_right_cephalic_curl = p_right_cephalic.addOrReplaceChild("right_cephalic_curl", CubeListBuilder.create()
                .texOffs(35, 51).addBox(-1F, -0.95F, -1.8F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.45F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.6F, -4F, -0.9599F, 0F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(112, 0).addBox(0F, -1.5F, -6.5F, 14F, 3F, 13F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(6.5F, 0F, -1.5F));
        PartDefinition p_left_wing_mid = p_left_wing.addOrReplaceChild("left_wing_mid", CubeListBuilder.create()
                .texOffs(92, 22).addBox(0F, -1F, -5.25F, 12F, 2F, 9F, new CubeDeformation(0F, -0.3F, -0.25F)),
                PartPose.offsetAndRotation(14F, 0F, -1F, 0F, -0.2793F, 0F));
        PartDefinition p_left_wing_tip = p_left_wing_mid.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(96, 39).addBox(0F, -0.5F, -3F, 8F, 1F, 5F, new CubeDeformation(0F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(12F, 0F, -1.5F, 0F, -0.4538F, 0F));
        PartDefinition p_left_pelvic = p_body.addOrReplaceChild("left_pelvic", CubeListBuilder.create()
                .texOffs(150, 39).addBox(-1.5F, -0.5F, -0.25F, 3F, 1F, 5F, new CubeDeformation(0F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(3.2F, 0.5F, 7.5F, 0F, 0.384F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-14F, -1.5F, -6.5F, 14F, 3F, 13F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(-6.5F, 0F, -1.5F));
        PartDefinition p_right_wing_mid = p_right_wing.addOrReplaceChild("right_wing_mid", CubeListBuilder.create()
                .texOffs(135, 22).addBox(-12F, -1F, -5.25F, 12F, 2F, 9F, new CubeDeformation(0F, -0.3F, -0.25F)),
                PartPose.offsetAndRotation(-14F, 0F, -1F, 0F, 0.2793F, 0F));
        PartDefinition p_right_wing_tip = p_right_wing_mid.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(123, 39).addBox(-8F, -0.5F, -3F, 8F, 1F, 5F, new CubeDeformation(0F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-12F, 0F, -1.5F, 0F, 0.4538F, 0F));
        PartDefinition p_right_pelvic = p_body.addOrReplaceChild("right_pelvic", CubeListBuilder.create()
                .texOffs(167, 39).addBox(-1.5F, -0.5F, -0.25F, 3F, 1F, 5F, new CubeDeformation(0F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-3.2F, 0.5F, 7.5F, 0F, -0.384F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-1F, -1F, 0F, 2F, 2F, 9F, new CubeDeformation(-0.3F, -0.4F, 0F)),
                PartPose.offset(0F, 0F, 8.5F));
        PartDefinition p_tail_mid = p_tail.addOrReplaceChild("tail_mid", CubeListBuilder.create()
                .texOffs(23, 39).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 10F, new CubeDeformation(0F, -0.05F, 0F)),
                PartPose.offset(0F, 0F, 9F));
        PartDefinition p_tail_tip = p_tail_mid.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(46, 39).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 9F, new CubeDeformation(-0.15F, -0.15F, 0F))
                .texOffs(44, 51).addBox(-1F, -1F, 8.1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 10F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
