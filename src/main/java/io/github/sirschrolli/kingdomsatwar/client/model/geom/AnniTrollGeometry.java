package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie Annis, des Trolls (im Spiel 1,2-fach vergrößert): Flickenkittel, Bannhammer, Haarschopf, Pixelkrone, zerfallener Arm.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class AnniTrollGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private AnniTrollGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(98, 28).addBox(-8.5F, -3F, -5.5F, 17F, 7F, 11F)
                .texOffs(13, 0).addBox(-9.5F, -17F, -6F, 19F, 14F, 12F)
                .texOffs(84, 81).addBox(-7.5F, -12F, -8.5F, 15F, 9F, 3F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(95, 65).addBox(-10.5F, -19.75F, -5F, 21F, 4F, 10F, new CubeDeformation(-0.3F, -0.25F, -0.2F))
                .texOffs(174, 96).addBox(-4F, -20.5F, -3.5F, 8F, 2F, 7F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(15, 109).addBox(1.4F, -11.2F, -9.1F, 5F, 5F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(0, 48).addBox(-10F, -4.6F, -6.5F, 20F, 3F, 13F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(172, 109).addBox(-2.5F, -5.2F, -7.3F, 5F, 4F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F)),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.3142F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-9.5F, -0.25F, -6.5F, 19F, 6F, 13F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 4F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-6.5F, -12F, -9.75F, 13F, 12F, 12F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(109, 109).addBox(-7F, -10.6F, -10.6F, 14F, 3F, 2F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(211, 109).addBox(-5.3F, -8F, -10.2F, 4F, 3F, 1F, new CubeDeformation(-0.1F, 0F, -0.25F))
                .texOffs(185, 109).addBox(0.8F, -8.6F, -10.25F, 5F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.2F))
                .texOffs(0, 109).addBox(-2F, -7.4F, -12.2F, 4F, 3F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(226, 96).addBox(-3F, -5.6F, -13.2F, 6F, 3F, 4F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(100, 48).addBox(-7F, -13.5F, -8.4F, 14F, 2F, 13F, new CubeDeformation(-0.1F, -0.1F, -0.4F))
                .texOffs(60, 96).addBox(-7F, -12F, 1.6F, 14F, 9F, 2F, new CubeDeformation(-0.2F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -19F, -5F, -0.4189F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(158, 65).addBox(-6F, -0.3F, -5.4F, 12F, 4F, 10F, new CubeDeformation(0F, -0.3F, -0.2F))
                .texOffs(91, 109).addBox(-5.3F, -3.6F, -5.2F, 2F, 4F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(100, 109).addBox(3.3F, -3.6F, -5.2F, 2F, 4F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.2F)),
                PartPose.offset(0F, -1F, -5F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(142, 109).addBox(-0.4F, -2F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(222, 109).addBox(4.2F, -3.9F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.05F))
                .texOffs(72, 116).addBox(5.9F, -5.3F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(231, 109).addBox(0.75F, 1.45F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(6.3F, -7F, -3.5F, 0F, -0.6109F, -0.3142F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(157, 109).addBox(-5.6F, -2F, -0.5F, 6F, 4F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(240, 109).addBox(-7.2F, -3.9F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.05F))
                .texOffs(79, 116).addBox(-7.9F, -5.3F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(0, 116).addBox(-2.75F, 1.45F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.25F, -0.15F, -0.3F)),
                PartPose.offsetAndRotation(-6.3F, -7F, -3.5F, 0F, 0.6109F, 0.3142F));
        PartDefinition p_tuft = p_head.addOrReplaceChild("tuft", CubeListBuilder.create()
                .texOffs(121, 81).addBox(-4.5F, -3.7F, -4F, 9F, 4F, 8F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(155, 48).addBox(-5F, -9F, -4.2F, 10F, 6F, 9F, new CubeDeformation(0F, -0.4F, -0.2F))
                .texOffs(126, 96).addBox(-4F, -12.3F, -2.8F, 8F, 4F, 7F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(205, 96).addBox(-2.5F, -14.9F, -1.2F, 5F, 3F, 5F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(198, 109).addBox(-1.5F, -16.6F, 0.8F, 3F, 2F, 3F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(9, 116).addBox(-1F, -17.2F, 3.6F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, 0F))
                .texOffs(80, 109).addBox(-6.8F, -7.9F, -1.5F, 2F, 3F, 3F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(28, 109).addBox(4.4F, -9.6F, -0.4F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(41, 81).addBox(-5.5F, -1.6F, -5F, 11F, 2F, 10F, new CubeDeformation(-0.3F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, -13.2F, -1F, -0.2094F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offset(0F, -16F, -1F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(41, 109).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(6F, 0F, 0F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(54, 109).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(-3F, 0F, 5.2F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(67, 109).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(-3F, 0F, -5.2F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(176, 0).addBox(-3.5F, -2.9F, -4F, 7F, 13F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(10.6F, -16F, -0.4F, -0.1047F, 0F, -0.1571F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 9.6F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_left_shard_0 = p_left_forearm.addOrReplaceChild("left_shard_0", CubeListBuilder.create()
                .texOffs(156, 81).addBox(-4F, -0.4F, -4F, 8F, 4F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_shard_1 = p_left_forearm.addOrReplaceChild("left_shard_1", CubeListBuilder.create()
                .texOffs(93, 96).addBox(-4F, 0F, -4F, 8F, 3F, 8F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(0F, 3.6F, 0F));
        PartDefinition p_left_shard_2 = p_left_forearm.addOrReplaceChild("left_shard_2", CubeListBuilder.create()
                .texOffs(189, 81).addBox(-4F, -0.4F, -4F, 8F, 4F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.2F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_left_hand = p_left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(194, 48).addBox(-4.5F, 0F, -4.5F, 9F, 6F, 9F, new CubeDeformation(-0.3F, 0F, -0.1F)),
                PartPose.offset(0F, 10.6F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(207, 0).addBox(-3.5F, -2.9F, -4F, 7F, 13F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(-10.6F, -16F, -0.4F, -0.1047F, 0F, 0.1571F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(65, 28).addBox(-4F, -0.4F, -4F, 8F, 11F, 8F, new CubeDeformation(-0.4F, -0.4F, -0.2F))
                .texOffs(25, 96).addBox(-4F, 6.35F, -4.5F, 8F, 2F, 9F, new CubeDeformation(-0.05F, -0.05F, -0.35F)),
                PartPose.offsetAndRotation(0F, 9.6F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_right_hand = p_right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(0, 65).addBox(-4.5F, 0F, -4.5F, 9F, 6F, 9F, new CubeDeformation(-0.3F, 0F, -0.1F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_hammer = p_right_hand.addOrReplaceChild("hammer", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -5F, -1.5F, 3F, 24F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(157, 96).addBox(-2F, -2.75F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(127, 0).addBox(-5F, 17.6F, -7F, 10F, 10F, 14F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(222, 81).addBox(-5F, 17.6F, -1.8F, 10F, 10F, 2F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(0, 96).addBox(-5F, 17.6F, 2.4F, 10F, 10F, 2F, new CubeDeformation(-0.05F, 0F, -0.2F))
                .texOffs(67, 48).addBox(-5F, 17.6F, -7.15F, 10F, 10F, 6F, new CubeDeformation(-0.15F, -0.15F, -0.3F)),
                PartPose.offset(0F, 3F, 0F));
        PartDefinition p_hammer_px_0 = p_hammer.addOrReplaceChild("hammer_px_0", CubeListBuilder.create()
                .texOffs(18, 116).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offset(5.6F, 20F, -5F));
        PartDefinition p_hammer_px_1 = p_hammer.addOrReplaceChild("hammer_px_1", CubeListBuilder.create()
                .texOffs(27, 116).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F)),
                PartPose.offset(-6.4F, 23.5F, -6.2F));
        PartDefinition p_hammer_px_2 = p_hammer.addOrReplaceChild("hammer_px_2", CubeListBuilder.create()
                .texOffs(86, 116).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(1F, 16F, -8.4F));
        PartDefinition p_pixel_0 = p_body.addOrReplaceChild("pixel_0", CubeListBuilder.create()
                .texOffs(36, 116).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(11.5F, -21F, -3F));
        PartDefinition p_pixel_1 = p_body.addOrReplaceChild("pixel_1", CubeListBuilder.create()
                .texOffs(45, 116).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.35F, -0.35F, -0.35F)),
                PartPose.offset(-12.5F, -10F, 2F));
        PartDefinition p_pixel_2 = p_body.addOrReplaceChild("pixel_2", CubeListBuilder.create()
                .texOffs(54, 116).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.45F)),
                PartPose.offset(10F, -2F, 5F));
        PartDefinition p_pixel_3 = p_body.addOrReplaceChild("pixel_3", CubeListBuilder.create()
                .texOffs(63, 116).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(-9F, -22F, 4F));
        PartDefinition p_pixel_4 = p_body.addOrReplaceChild("pixel_4", CubeListBuilder.create()
                .texOffs(91, 116).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F)),
                PartPose.offset(13F, -12F, -5F));
        PartDefinition p_pixel_5 = p_body.addOrReplaceChild("pixel_5", CubeListBuilder.create()
                .texOffs(96, 116).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(-13F, 2F, -3F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(155, 28).addBox(-4F, -0.2F, -4F, 8F, 9F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(5F, 7F, 0.6F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(37, 65).addBox(-3.5F, -0.3F, -3.5F, 7F, 8F, 7F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(203, 65).addBox(-4.5F, 6.2F, -7.5F, 9F, 3F, 11F, new CubeDeformation(-0.4F, -0.4F, -0.1F)),
                PartPose.offset(0F, 8.2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(188, 28).addBox(-4F, -0.2F, -4F, 8F, 9F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.1F)),
                PartPose.offset(-5F, 7F, 0.6F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(66, 65).addBox(-3.5F, -0.3F, -3.5F, 7F, 8F, 7F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(0, 81).addBox(-4.5F, 6.2F, -7.5F, 9F, 3F, 11F, new CubeDeformation(-0.4F, -0.4F, -0.1F)),
                PartPose.offset(0F, 8.2F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
