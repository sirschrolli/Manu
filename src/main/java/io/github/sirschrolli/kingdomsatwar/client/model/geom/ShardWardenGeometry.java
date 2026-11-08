package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Splitterwächters (offene Brust mit kreisendem Herz).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ShardWardenGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 128;

    private ShardWardenGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(135, 24).addBox(-3F, 0F, -3F, 6F, 9F, 6F)
                .texOffs(31, 42).addBox(-3.5F, 8.25F, -3.5F, 7F, 7F, 7F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(0, 84).addBox(-3F, 6.9F, -4.5F, 6F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(127, 57).addBox(-4F, 15F, -5F, 8F, 3F, 9F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(-4.6F, 6F, 0.5F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(160, 24).addBox(-3F, 0F, -3F, 6F, 9F, 6F)
                .texOffs(60, 42).addBox(-3.5F, 8.25F, -3.5F, 7F, 7F, 7F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(17, 84).addBox(-3F, 6.9F, -4.5F, 6F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(162, 57).addBox(-4F, 15F, -5F, 8F, 3F, 9F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offset(4.6F, 6F, 0.5F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(92, 24).addBox(-6.5F, -6.5F, -4F, 13F, 7F, 8F)
                .texOffs(78, 57).addBox(-7.5F, -2.5F, -4.5F, 15F, 3F, 9F)
                .texOffs(58, 0).addBox(-10F, -21F, 2F, 20F, 15F, 4F)
                .texOffs(0, 0).addBox(-10.2F, -21F, -6F, 6F, 15F, 8F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(29, 0).addBox(4.2F, -21F, -6F, 6F, 15F, 8F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(197, 57).addBox(-4.5F, -21.2F, -6F, 9F, 4F, 8F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(0, 71).addBox(-4.5F, -9.6F, -5.8F, 9F, 4F, 8F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(110, 71).addBox(-4.5F, -17.8F, 1.1F, 9F, 9F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(13, 57).addBox(-11F, -22.2F, -6.9F, 22F, 2F, 10F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(170, 71).addBox(-5.3F, -17.3F, -6.6F, 1F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(175, 71).addBox(4.3F, -17.3F, -6.6F, 1F, 8F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.3F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_core = p_body.addOrReplaceChild("core", CubeListBuilder.create()
                .texOffs(131, 71).addBox(-2.5F, -2.5F, -2.5F, 5F, 5F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, -13.3F, -1.8F));
        PartDefinition p_core_inner = p_core.addOrReplaceChild("core_inner", CubeListBuilder.create()
                .texOffs(180, 71).addBox(-2F, -2F, -2F, 4F, 4F, 4F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0.7854F, 0F, 0.7854F));
        PartDefinition p_back_crystals = p_body.addOrReplaceChild("back_crystals", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_back_shard_0 = p_back_crystals.addOrReplaceChild("back_shard_0", CubeListBuilder.create()
                .texOffs(185, 24).addBox(-1.5F, -10.25F, -1.5F, 3F, 12F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(-4F, -20.5F, 5F, 0.5585F, 0F, -0.2094F));
        PartDefinition p_back_shard_1 = p_back_crystals.addOrReplaceChild("back_shard_1", CubeListBuilder.create()
                .texOffs(142, 0).addBox(-1.5F, -13.25F, -1.5F, 3F, 15F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(1.5F, -21F, 5.5F, 0.4538F, 0F, 0.1396F));
        PartDefinition p_back_shard_2 = p_back_crystals.addOrReplaceChild("back_shard_2", CubeListBuilder.create()
                .texOffs(225, 42).addBox(-1.5F, -8.25F, -1.5F, 3F, 10F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(5.5F, -18F, 5F, 0.6632F, 0F, 0.3491F));
        PartDefinition p_back_shard_3 = p_back_crystals.addOrReplaceChild("back_shard_3", CubeListBuilder.create()
                .texOffs(35, 71).addBox(-1F, -7.25F, -1F, 2F, 9F, 2F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(-1F, -15F, 5.5F, 0.9076F, 0F, -0.0698F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(107, 0).addBox(-4F, -8.25F, -4.75F, 8F, 9F, 9F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(119, 84).addBox(-4.5F, -6.4F, -5.4F, 9F, 2F, 2F, new CubeDeformation(0F, -0.2F, -0.4F))
                .texOffs(142, 84).addBox(-3.5F, -5F, -5.05F, 7F, 2F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(0F, -22F, -1.5F));
        PartDefinition p_crown_0 = p_head.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(243, 71).addBox(-1F, -4.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(0F, -8F, 0.5F, -0.1047F, 0F, 0F));
        PartDefinition p_crown_1 = p_head.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(47, 84).addBox(-1F, -3.25F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(-2.6F, -8F, 0F, 0F, 0F, -0.384F));
        PartDefinition p_crown_2 = p_head.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(56, 84).addBox(-1F, -3.25F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(2.6F, -8F, 0F, 0F, 0F, 0.384F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(155, 0).addBox(-4.5F, -3.75F, -5F, 9F, 7F, 10F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(139, 42).addBox(-5F, 2.1F, -5.5F, 10F, 2F, 11F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(89, 42).addBox(-3F, 3F, -3F, 6F, 8F, 6F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(-12F, -18.5F, 0F));
        PartDefinition p_right_shoulder_shard_0 = p_right_arm.addOrReplaceChild("right_shoulder_shard_0", CubeListBuilder.create()
                .texOffs(238, 42).addBox(-1.5F, -8.25F, -1.5F, 3F, 10F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(-1F, -3F, -1.5F, -0.2443F, 0F, -0.4538F));
        PartDefinition p_right_shoulder_shard_1 = p_right_arm.addOrReplaceChild("right_shoulder_shard_1", CubeListBuilder.create()
                .texOffs(152, 71).addBox(-1F, -6.25F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(-2.5F, -2.5F, 2F, 0.3142F, 0F, -0.6981F));
        PartDefinition p_right_shoulder_shard_2 = p_right_arm.addOrReplaceChild("right_shoulder_shard_2", CubeListBuilder.create()
                .texOffs(212, 71).addBox(-1F, -4.5F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0.5F, -3F, 2.5F, 0.5236F, 0F, -0.2443F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-4F, 0F, -4F, 8F, 9F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(198, 24).addBox(-3.5F, 8.4F, -4F, 7F, 6F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(44, 71).addBox(-4F, 0.7F, -4F, 8F, 2F, 8F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_right_knuckle_0 = p_right_forearm.addOrReplaceChild("right_knuckle_0", CubeListBuilder.create()
                .texOffs(83, 84).addBox(-1F, -2.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-2.2F, 10.2F, -3.6F, -1.0821F, 0F, -0.2443F));
        PartDefinition p_right_knuckle_1 = p_right_forearm.addOrReplaceChild("right_knuckle_1", CubeListBuilder.create()
                .texOffs(65, 84).addBox(-1F, -3.4F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.2F, -3.6F, -1.0821F, 0F, 0F));
        PartDefinition p_right_knuckle_2 = p_right_forearm.addOrReplaceChild("right_knuckle_2", CubeListBuilder.create()
                .texOffs(92, 84).addBox(-1F, -2.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(2.2F, 10.2F, -3.6F, -1.0821F, 0F, 0.2443F));
        PartDefinition p_right_blade = p_right_forearm.addOrReplaceChild("right_blade", CubeListBuilder.create()
                .texOffs(66, 24).addBox(-1F, -6F, -2F, 2F, 12F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-3.6F, 4.5F, 0F, 0F, 0F, 0.1396F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(194, 0).addBox(-4.5F, -3.75F, -5F, 9F, 7F, 10F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(182, 42).addBox(-5F, 2.1F, -5.5F, 10F, 2F, 11F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(114, 42).addBox(-3F, 3F, -3F, 6F, 8F, 6F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(12F, -18.5F, 0F));
        PartDefinition p_left_shoulder_shard_0 = p_left_arm.addOrReplaceChild("left_shoulder_shard_0", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-1.5F, -8.25F, -1.5F, 3F, 10F, 3F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(1F, -3F, -1.5F, -0.2443F, 0F, 0.4538F));
        PartDefinition p_left_shoulder_shard_1 = p_left_arm.addOrReplaceChild("left_shoulder_shard_1", CubeListBuilder.create()
                .texOffs(161, 71).addBox(-1F, -6.25F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(2.5F, -2.5F, 2F, 0.3142F, 0F, 0.6981F));
        PartDefinition p_left_shoulder_shard_2 = p_left_arm.addOrReplaceChild("left_shoulder_shard_2", CubeListBuilder.create()
                .texOffs(221, 71).addBox(-1F, -4.5F, -1F, 2F, 6F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-0.5F, -3F, 2.5F, 0.5236F, 0F, 0.2443F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(33, 24).addBox(-4F, 0F, -4F, 8F, 9F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(0, 42).addBox(-3.5F, 8.4F, -4F, 7F, 6F, 8F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(77, 71).addBox(-4F, 0.7F, -4F, 8F, 2F, 8F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(0F, 11F, 0F));
        PartDefinition p_left_knuckle_0 = p_left_forearm.addOrReplaceChild("left_knuckle_0", CubeListBuilder.create()
                .texOffs(101, 84).addBox(-1F, -2.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(-2.2F, 10.2F, -3.6F, -1.0821F, 0F, -0.2443F));
        PartDefinition p_left_knuckle_1 = p_left_forearm.addOrReplaceChild("left_knuckle_1", CubeListBuilder.create()
                .texOffs(74, 84).addBox(-1F, -3.4F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 10.2F, -3.6F, -1.0821F, 0F, 0F));
        PartDefinition p_left_knuckle_2 = p_left_forearm.addOrReplaceChild("left_knuckle_2", CubeListBuilder.create()
                .texOffs(110, 84).addBox(-1F, -2.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(2.2F, 10.2F, -3.6F, -1.0821F, 0F, 0.2443F));
        PartDefinition p_left_blade = p_left_forearm.addOrReplaceChild("left_blade", CubeListBuilder.create()
                .texOffs(79, 24).addBox(-1F, -6F, -2F, 2F, 12F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(3.6F, 4.5F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_orbit = root.addOrReplaceChild("orbit", CubeListBuilder.create(),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_fragment_0 = p_orbit.addOrReplaceChild("fragment_0", CubeListBuilder.create()
                .texOffs(197, 71).addBox(-2F, -2.5F, -1.5F, 4F, 5F, 3F, new CubeDeformation(-0.3F, -0.2F, 0F)),
                PartPose.offsetAndRotation(14.7224F, -2F, 8.5F, 0F, 0F, 0.2094F));
        PartDefinition p_fragment_1 = p_orbit.addOrReplaceChild("fragment_1", CubeListBuilder.create()
                .texOffs(230, 71).addBox(-1.5F, -2F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(-13.8564F, 4F, 8F, 0.3142F, 0.4363F, 0.2094F));
        PartDefinition p_fragment_2 = p_orbit.addOrReplaceChild("fragment_2", CubeListBuilder.create()
                .texOffs(34, 84).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-3.1257F, -6F, -17.7265F, 0.6283F, 0.8727F, 0.2094F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
