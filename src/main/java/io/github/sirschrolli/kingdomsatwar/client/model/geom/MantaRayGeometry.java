package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Mantarochens (Brustflossen in drei Gliedern, Kopfflossen, Peitschenschwanz).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MantaRayGeometry {
    public static final int TEXTURE_WIDTH = 192;
    public static final int TEXTURE_HEIGHT = 128;

    private MantaRayGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -2F, -9F, 12F, 4F, 18F)
                .texOffs(61, 0).addBox(-8.5F, -1.5F, -7F, 17F, 3F, 13F)
                .texOffs(51, 23).addBox(-3.5F, -2.8F, -6.5F, 7F, 1F, 10F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offset(0F, 20F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(51, 40).addBox(-6.5F, -2F, -2.8F, 13F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(20, 49).addBox(-5F, -1.5F, -3.4F, 10F, 3F, 1F, new CubeDeformation(0F, -0.3F, -0.2F))
                .texOffs(43, 49).addBox(6.3F, -0.9F, -2.2F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(48, 49).addBox(-7.3F, -0.9F, -2.2F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(0F, 0.3F, -9F));
        PartDefinition p_left_cephalic = p_head.addOrReplaceChild("left_cephalic", CubeListBuilder.create()
                .texOffs(164, 23).addBox(-0.9F, -1F, -5.7F, 2F, 2F, 6F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(6F, 0.1F, -2.4F, 0.0698F, -0.1396F, 0F));
        PartDefinition p_left_cephalic_tip = p_left_cephalic.addOrReplaceChild("left_cephalic_tip", CubeListBuilder.create()
                .texOffs(172, 40).addBox(-0.95F, -1F, -2.7F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -5.4F, -0.1745F, 0.1047F, 0F));
        PartDefinition p_right_cephalic = p_head.addOrReplaceChild("right_cephalic", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-1.1F, -1F, -5.7F, 2F, 2F, 6F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-6F, 0.1F, -2.4F, 0.0698F, 0.1396F, 0F));
        PartDefinition p_right_cephalic_tip = p_right_cephalic.addOrReplaceChild("right_cephalic_tip", CubeListBuilder.create()
                .texOffs(0, 49).addBox(-1.05F, -1F, -2.7F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, -5.4F, -0.1745F, -0.1047F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(122, 0).addBox(0F, -1.5F, 0F, 12F, 3F, 13F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(8F, 0F, -7F, 0F, -0.1745F, 0F));
        PartDefinition p_left_wing_mid = p_left_wing.addOrReplaceChild("left_wing_mid", CubeListBuilder.create()
                .texOffs(86, 23).addBox(0F, -1F, 0F, 11F, 2F, 8F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(12F, 0F, 0F, 0F, -0.2793F, 0F));
        PartDefinition p_left_wing_tip = p_left_wing_mid.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(84, 40).addBox(0F, -0.5F, -0.2F, 10F, 1F, 4F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(11F, 0F, 0F, 0F, -0.4189F, 0F));
        PartDefinition p_left_pelvic = p_body.addOrReplaceChild("left_pelvic", CubeListBuilder.create()
                .texOffs(142, 40).addBox(-1.5F, -0.5F, -0.2F, 3F, 1F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(3F, 0.4F, 8F, 0F, 0.3142F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-12F, -1.5F, 0F, 12F, 3F, 13F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(-8F, 0F, -7F, 0F, 0.1745F, 0F));
        PartDefinition p_right_wing_mid = p_right_wing.addOrReplaceChild("right_wing_mid", CubeListBuilder.create()
                .texOffs(125, 23).addBox(-11F, -1F, 0F, 11F, 2F, 8F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-12F, 0F, 0F, 0F, 0.2793F, 0F));
        PartDefinition p_right_wing_tip = p_right_wing_mid.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(113, 40).addBox(-10F, -0.5F, -0.2F, 10F, 1F, 4F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-11F, 0F, 0F, 0F, 0.4189F, 0F));
        PartDefinition p_right_pelvic = p_body.addOrReplaceChild("right_pelvic", CubeListBuilder.create()
                .texOffs(157, 40).addBox(-1.5F, -0.5F, -0.2F, 3F, 1F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(-3F, 0.4F, 8F, 0F, -0.3142F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(17, 40).addBox(-1F, -1F, 0F, 2F, 2F, 6F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(11, 49).addBox(-0.5F, -2.5F, 0.3F, 1F, 2F, 3F, new CubeDeformation(-0.2F, -0.1F, -0.3F)),
                PartPose.offset(0F, -0.2F, 9F));
        PartDefinition p_tail_tip = p_tail.addOrReplaceChild("tail_tip", CubeListBuilder.create()
                .texOffs(34, 40).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 7F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 6F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
