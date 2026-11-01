package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Hexen (Menschen-Skelett mit Spitzhut, Nase, Haaren, Umhang und Kleid).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WitchGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private WitchGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(29, 60).addBox(-1F, -1F, -2F, 2F, 4F, 2F),
                PartPose.offsetAndRotation(0F, -3F, -4F, -0.1396F, 0F, 0F));
        PartDefinition p_wart = p_nose.addOrReplaceChild("wart", CubeListBuilder.create()
                .texOffs(88, 60).addBox(-0.4F, -0.4F, -0.6F, 0.8F, 0.8F, 0.8F),
                PartPose.offset(0.6F, 2.2F, -2F));
        PartDefinition p_long_hair = p_head.addOrReplaceChild("long_hair", CubeListBuilder.create()
                .texOffs(78, 35).addBox(-4.5F, 0F, 0F, 9F, 11F, 1F),
                PartPose.offsetAndRotation(0F, -4.6F, 4.1F, -0.2094F, 0F, 0F));
        PartDefinition p_hat_brim = p_head.addOrReplaceChild("hat_brim", CubeListBuilder.create()
                .texOffs(68, 18).addBox(-7F, -0.5F, -7F, 14F, 1F, 14F),
                PartPose.offset(0F, -7.6F, 0F));
        PartDefinition p_hat_crown = p_hat_brim.addOrReplaceChild("hat_crown", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-4.5F, -4F, -4.5F, 9F, 4F, 9F)
                .texOffs(37, 35).addBox(-4.7F, -1.4F, -4.7F, 9.4F, 1.4F, 9.4F),
                PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_hat_tier1 = p_hat_crown.addOrReplaceChild("hat_tier1", CubeListBuilder.create()
                .texOffs(99, 35).addBox(-3F, -4F, -3F, 6F, 4F, 6F),
                PartPose.offsetAndRotation(0F, -4F, 0.6F, -0.2094F, 0F, 0.0698F));
        PartDefinition p_hat_tier2 = p_hat_tier1.addOrReplaceChild("hat_tier2", CubeListBuilder.create()
                .texOffs(92, 49).addBox(-2F, -3.5F, -2F, 4F, 3.5F, 4F),
                PartPose.offsetAndRotation(0F, -4F, 0.4F, -0.3142F, 0F, 0.0698F));
        PartDefinition p_hat_tip = p_hat_tier2.addOrReplaceChild("hat_tip", CubeListBuilder.create()
                .texOffs(49, 60).addBox(-1F, -3F, -1F, 2F, 3F, 2F),
                PartPose.offsetAndRotation(0F, -3.5F, 0.3F, -0.4887F, 0F, 0.1047F));
        PartDefinition p_hat_deco = p_hat_crown.addOrReplaceChild("hat_deco", CubeListBuilder.create()
                .texOffs(67, 60).addBox(2.6F, -3.2F, -5.2F, 2F, 2F, 1F)
                .texOffs(74, 60).addBox(3.6F, -2.2F, -5F, 1.4F, 1.4F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_shawl = p_body.addOrReplaceChild("shawl", CubeListBuilder.create()
                .texOffs(21, 49).addBox(-4.5F, -0.3F, -2.5F, 9F, 4F, 5F)
                .texOffs(0, 49).addBox(-4.5F, 3.7F, 2F, 9F, 9F, 0.6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dress = p_body.addOrReplaceChild("dress", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.4F, 9.5F, -2.4F, 8.8F, 11.6F, 4.8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_belt = p_body.addOrReplaceChild("belt", CubeListBuilder.create()
                .texOffs(0, 60).addBox(-4.3F, 8.6F, -2.3F, 8.6F, 1.4F, 4.6F)
                .texOffs(58, 60).addBox(-3.6F, 9.6F, -3.2F, 1.6F, 2.4F, 1.2F)
                .texOffs(81, 60).addBox(-1.4F, 9.8F, -3F, 1.2F, 2F, 1F)
                .texOffs(38, 60).addBox(2F, 9.4F, -3F, 2.4F, 2.6F, 1.4F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-3F, -2F, -2F, 4F, 12F, 4F)
                .texOffs(50, 49).addBox(-3.4F, 6.4F, -2.4F, 4.8F, 3F, 4.8F),
                PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(17, 18).addBox(-1F, -2F, -2F, 4F, 12F, 4F)
                .texOffs(71, 49).addBox(-1.4F, 6.4F, -2.4F, 4.8F, 3F, 4.8F),
                PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(34, 18).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(51, 18).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(1.9F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
