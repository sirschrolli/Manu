package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Phasmor, dem Leerwandlerkönig (1.5; im Spiel 1,35-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PhasmorGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private PhasmorGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-4F, -11.5F, -2F, 8F, 12F, 4F)
                .texOffs(0, 66).addBox(-6F, -12.5F, -2.5F, 12F, 2F, 5F, new CubeDeformation(-0.4F, -0.2F, -0.1F))
                .texOffs(64, 66).addBox(-4.5F, -11.2F, -2.6F, 9F, 6F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(56, 81).addBox(-1.5F, -10.1F, -3.2F, 3F, 3F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.2F))
                .texOffs(35, 66).addBox(-4.5F, -1.9F, -2.5F, 9F, 2F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(74, 81).addBox(-1F, -1.9F, -3F, 2F, 2F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.25F)),
                PartPose.offset(0F, -3F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-4F, -7.8F, -0.15F, 8F, 8F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(0F, -12F, 1.5F, -0.2443F, 0F, 0F));
        PartDefinition p_collar_l = p_collar.addOrReplaceChild("collar_l", CubeListBuilder.create()
                .texOffs(78, 56).addBox(-0.3F, -6.7F, -0.3F, 4F, 7F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(3.7F, 0F, 0.2F, 0F, -0.5236F, -0.1396F));
        PartDefinition p_collar_r = p_collar.addOrReplaceChild("collar_r", CubeListBuilder.create()
                .texOffs(89, 56).addBox(-3.7F, -6.7F, -0.3F, 4F, 7F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(-3.7F, 0F, 0.2F, 0F, 0.5236F, 0.1396F));
        PartDefinition p_mantle = p_body.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-6F, 0F, -0.15F, 12F, 14F, 1F, new CubeDeformation(-0.4F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, -12.2F, 2.4F, 0.0873F, 0F, 0F));
        PartDefinition p_mantle_low = p_mantle.addOrReplaceChild("mantle_low", CubeListBuilder.create()
                .texOffs(27, 28).addBox(-5.5F, 0F, -0.2F, 11F, 13F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 14F, 0.05F, 0.0873F, 0F, 0F));
        PartDefinition p_tabard = p_body.addOrReplaceChild("tabard", CubeListBuilder.create()
                .texOffs(95, 28).addBox(-2.5F, 0F, -0.55F, 5F, 11F, 1F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offset(0F, -0.9F, -2.35F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(57, 0).addBox(-3.5F, -9.75F, -3.5F, 7F, 10F, 7F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(13, 44).addBox(-4F, -10.3F, -4F, 8F, 2F, 8F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(81, 81).addBox(-1F, -9.95F, -4.45F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, -11.5F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offset(0F, -12.2F, 0F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(19, 56).addBox(-1F, -6.8F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, -3.3F, 0.2793F, 0F, 0F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(85, 66).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(3.1385F, 0F, -1.0198F, 0.0863F, 0F, 0.2656F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(15, 74).addBox(-1F, -4F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(1.9397F, 0F, 2.6698F, -0.2259F, 0F, 0.1641F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(24, 74).addBox(-1F, -4F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-1.9397F, 0F, 2.6698F, -0.2259F, 0F, -0.1641F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(94, 66).addBox(-1F, -5F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-3.1385F, 0F, -1.0198F, 0.0863F, 0F, -0.2656F));
        PartDefinition p_jaw = p_body.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(70, 28).addBox(-3F, -7F, -3F, 6F, 7F, 6F),
                PartPose.offset(0F, -11.5F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(39, 0).addBox(-1F, -1.75F, -1F, 2F, 16F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(53, 74).addBox(-1.5F, 12.7F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(5.6F, -10F, 0F));
        PartDefinition p_left_pauldron = p_left_arm.addOrReplaceChild("left_pauldron", CubeListBuilder.create()
                .texOffs(72, 44).addBox(-3F, -1.8F, -3F, 6F, 3F, 6F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(28, 56).addBox(-3F, 0.6F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0.6F, -1.2F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_left_float_0 = p_left_pauldron.addOrReplaceChild("left_float_0", CubeListBuilder.create()
                .texOffs(116, 74).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(0.9F, -3.2F, -1F, 0.1745F, 0F, 0.2443F));
        PartDefinition p_left_float_1 = p_left_pauldron.addOrReplaceChild("left_float_1", CubeListBuilder.create()
                .texOffs(0, 81).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(-0.6F, -3.7F, 1.3F, -0.2094F, 0F, 0.1047F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(86, 0).addBox(-1F, -0.25F, -1F, 2F, 15F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(46, 44).addBox(-1.5F, 5.5F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(33, 74).addBox(-1.15F, 13.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(38, 74).addBox(0.15F, 13.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(27, 81).addBox(-0.5F, 13.95F, 0.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.2F)),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-1F, -1.75F, -1F, 2F, 16F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(66, 74).addBox(-1.5F, 12.7F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(-5.6F, -10F, 0F));
        PartDefinition p_right_pauldron = p_right_arm.addOrReplaceChild("right_pauldron", CubeListBuilder.create()
                .texOffs(97, 44).addBox(-3F, -1.8F, -3F, 6F, 3F, 6F, new CubeDeformation(0F, -0.4F, -0.1F))
                .texOffs(53, 56).addBox(-3F, 0.6F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-0.6F, -1.2F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_right_float_0 = p_right_pauldron.addOrReplaceChild("right_float_0", CubeListBuilder.create()
                .texOffs(9, 81).addBox(-1F, -3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, 0F, -0.45F)),
                PartPose.offsetAndRotation(-0.9F, -3.2F, -1F, 0.1745F, 0F, -0.2443F));
        PartDefinition p_right_float_1 = p_right_pauldron.addOrReplaceChild("right_float_1", CubeListBuilder.create()
                .texOffs(18, 81).addBox(-1F, -2.6F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.4F, -0.45F)),
                PartPose.offsetAndRotation(0.6F, -3.7F, 1.3F, -0.2094F, 0F, -0.1047F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-1F, -0.25F, -1F, 2F, 15F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(59, 44).addBox(-1.5F, 5.5F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(43, 74).addBox(-1.15F, 13.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(48, 74).addBox(0.15F, 13.8F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(32, 81).addBox(-0.5F, 13.95F, 0.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.05F, -0.2F)),
                PartPose.offset(0F, 14F, 0F));
        PartDefinition p_blade = p_right_forearm.addOrReplaceChild("blade", CubeListBuilder.create()
                .texOffs(103, 66).addBox(-1F, -2.5F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.35F, -0.1F, -0.35F))
                .texOffs(65, 81).addBox(-1F, 2.35F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(37, 81).addBox(-3.5F, -4.05F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.2F, -0.35F, 0F))
                .texOffs(105, 74).addBox(-1F, -5.3F, -1.5F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.4F, -0.2F))
                .texOffs(0, 0).addBox(-1F, -27.8F, -2F, 2F, 23F, 4F, new CubeDeformation(-0.45F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(0F, 15.6F, -0.2F, 1.5708F, 0F, 0F));
        PartDefinition p_left_leg = p_body.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(13, 0).addBox(-1.5F, -0.25F, -1.5F, 3F, 15F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(79, 74).addBox(-1.5F, 13F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offset(2.2F, 0.5F, 0F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(52, 28).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(108, 28).addBox(-1.5F, 0.9F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(112, 66).addBox(-1.5F, 10.25F, -3.2F, 3F, 2F, 4F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(0F, 14.5F, 0F));
        PartDefinition p_right_leg = p_body.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(26, 0).addBox(-1.5F, -0.25F, -1.5F, 3F, 15F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(92, 74).addBox(-1.5F, 13F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offset(-2.2F, 0.5F, 0F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(61, 28).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(0, 44).addBox(-1.5F, 0.9F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.05F, -0.3F, -0.05F))
                .texOffs(0, 74).addBox(-1.5F, 10.25F, -3.2F, 3F, 2F, 4F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offset(0F, 14.5F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
