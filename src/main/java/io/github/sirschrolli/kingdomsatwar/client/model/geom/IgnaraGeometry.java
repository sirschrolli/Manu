package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Ignara, der Flammenpriesterin (1.2).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class IgnaraGeometry {
    public static final int TEXTURE_WIDTH = 192;
    public static final int TEXTURE_HEIGHT = 192;

    private IgnaraGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(69, 30).addBox(-4.5F, -2F, -3F, 9F, 3F, 6F, new CubeDeformation(-0.2F, -0.4F, -0.1F))
                .texOffs(28, 57).addBox(-1F, -1.9F, -3.6F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offset(0F, 2F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 30).addBox(-4.5F, -0.2F, -3F, 9F, 5F, 6F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(69, 16).addBox(-5.5F, 4.4F, -3.5F, 11F, 5F, 7F, new CubeDeformation(-0.3F, -0.2F, -0.1F))
                .texOffs(91, 0).addBox(-6F, 9.1F, -4F, 12F, 5F, 8F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 0).addBox(-7F, 14F, -5F, 14F, 5F, 10F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hem = p_skirt.addOrReplaceChild("hem", CubeListBuilder.create()
                .texOffs(138, 42).addBox(-7F, 0F, -4.7F, 14F, 5F, 0F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(0, 51).addBox(-7F, 0F, 4.7F, 14F, 5F, 0F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(49, 0).addBox(-6.9F, 0F, -5F, 0F, 5F, 10F, new CubeDeformation(0F, 0F, -0.4F))
                .texOffs(70, 0).addBox(6.9F, 0F, -5F, 0F, 5F, 10F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offset(0F, 18.6F, 0F));
        PartDefinition p_torso = p_body.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(132, 0).addBox(-4F, -8.3F, -2.5F, 8F, 8F, 5F, new CubeDeformation(0F, -0.3F, -0.1F))
                .texOffs(59, 57).addBox(-2.1F, -6.35F, -3.025F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.275F))
                .texOffs(64, 57).addBox(-0.5F, -5.65F, -3.025F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.275F))
                .texOffs(69, 57).addBox(1.1F, -6.35F, -3.025F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.275F))
                .texOffs(0, 42).addBox(-5F, -8.8F, -3F, 10F, 2F, 6F, new CubeDeformation(0F, 0F, -0.1F)),
                PartPose.offset(0F, -1F, 0F));
        PartDefinition p_cape = p_torso.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(156, 16).addBox(-5F, 0F, -0.2F, 10F, 11F, 1F, new CubeDeformation(-0.4F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -8.4F, 2.6F, 0.1396F, 0F, 0F));
        PartDefinition p_cape_low = p_cape.addOrReplaceChild("cape_low", CubeListBuilder.create()
                .texOffs(48, 30).addBox(-4.5F, 0F, -0.25F, 9F, 9F, 1F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 11F, 0.05F, 0.1047F, 0F, 0F));
        PartDefinition p_head = p_torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 51).addBox(-1.5F, -1.4F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(159, 0).addBox(-3F, -7.6F, -3F, 6F, 7F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(136, 51).addBox(-3.5F, -6.1F, -3.5F, 7F, 3F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.2F))
                .texOffs(33, 42).addBox(-3.5F, -7.6F, -3.5F, 7F, 1F, 7F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offset(0F, -8.6F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(35, 57).addBox(-1F, -1.95F, -0.675F, 2F, 2F, 1F, new CubeDeformation(-0.35F, -0.35F, -0.225F)),
                PartPose.offset(0F, -7.4F, -3.1F));
        PartDefinition p_crown_ray_0 = p_crown.addOrReplaceChild("crown_ray_0", CubeListBuilder.create()
                .texOffs(49, 57).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.28F, 0F, -0.28F)),
                PartPose.offsetAndRotation(-2.4F, 0F, 0F, -0.1745F, 0F, -0.5934F));
        PartDefinition p_crown_ray_1 = p_crown.addOrReplaceChild("crown_ray_1", CubeListBuilder.create()
                .texOffs(9, 57).addBox(-0.5F, -2.9F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.28F, -0.1F, -0.28F)),
                PartPose.offsetAndRotation(-1.2F, 0F, 0F, -0.1745F, 0F, -0.2793F));
        PartDefinition p_crown_ray_2 = p_crown.addOrReplaceChild("crown_ray_2", CubeListBuilder.create()
                .texOffs(116, 51).addBox(-0.5F, -3.8F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.28F, -0.2F, -0.28F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.1745F, 0F, 0F));
        PartDefinition p_crown_ray_3 = p_crown.addOrReplaceChild("crown_ray_3", CubeListBuilder.create()
                .texOffs(14, 57).addBox(-0.5F, -2.9F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.28F, -0.1F, -0.28F)),
                PartPose.offsetAndRotation(1.2F, 0F, 0F, -0.1745F, 0F, 0.2793F));
        PartDefinition p_crown_ray_4 = p_crown.addOrReplaceChild("crown_ray_4", CubeListBuilder.create()
                .texOffs(54, 57).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.28F, 0F, -0.28F)),
                PartPose.offsetAndRotation(2.4F, 0F, 0F, -0.1745F, 0F, 0.5934F));
        PartDefinition p_hair = p_head.addOrReplaceChild("hair", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -6.6F, 0.8F, -0.3142F, 0F, 0F));
        PartDefinition p_hair_1 = p_hair.addOrReplaceChild("hair_1", CubeListBuilder.create()
                .texOffs(50, 16).addBox(-4.5F, -13F, 0F, 9F, 13F, 0F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hair_2 = p_hair.addOrReplaceChild("hair_2", CubeListBuilder.create()
                .texOffs(31, 30).addBox(-4F, -11F, 0F, 8F, 11F, 0F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_hair_3 = p_hair.addOrReplaceChild("hair_3", CubeListBuilder.create()
                .texOffs(100, 30).addBox(-3.5F, -9F, 0F, 7F, 9F, 0F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_hair_4 = p_hair.addOrReplaceChild("hair_4", CubeListBuilder.create()
                .texOffs(115, 30).addBox(-3.5F, -9F, 0F, 7F, 9F, 0F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.7854F, 0F));
        PartDefinition p_lock_l = p_head.addOrReplaceChild("lock_l", CubeListBuilder.create()
                .texOffs(156, 30).addBox(-1F, 0F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(3F, -5.6F, 1F, 0.2443F, 0F, -0.1745F));
        PartDefinition p_lock_r = p_head.addOrReplaceChild("lock_r", CubeListBuilder.create()
                .texOffs(165, 30).addBox(-1F, 0F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-3F, -5.6F, 1F, 0.2443F, 0F, 0.1745F));
        PartDefinition p_left_arm = p_torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(130, 30).addBox(-1.5F, -0.5F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(153, 51).addBox(-1.5F, 1.2F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4.7F, -7.4F, 0F, -0.1396F, 0F, -0.2094F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(79, 42).addBox(-1F, -0.4F, -1F, 2F, 6F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(97, 42).addBox(-1.5F, 2F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(42, 51).addBox(-1F, 4.6F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 5.4F, 0F, -0.4538F, 0F, 0F));
        PartDefinition p_orb = p_left_forearm.addOrReplaceChild("orb", CubeListBuilder.create()
                .texOffs(62, 42).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(179, 51).addBox(-1F, -1F, -1F, 2F, 2F, 2F),
                PartPose.offset(0F, 9.4F, -1.4F));
        PartDefinition p_right_arm = p_torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(143, 30).addBox(-1.5F, -0.5F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(166, 51).addBox(-1.5F, 1.2F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4.7F, -7.4F, 0F, -0.1396F, 0F, 0.2094F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(88, 42).addBox(-1F, -0.4F, -1F, 2F, 6F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(110, 42).addBox(-1.5F, 2F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.25F, -0.4F, -0.25F))
                .texOffs(51, 51).addBox(-1F, 4.6F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offsetAndRotation(0F, 5.4F, 0F, -0.4538F, 0F, 0F));
        PartDefinition p_whip = p_right_forearm.addOrReplaceChild("whip", CubeListBuilder.create()
                .texOffs(174, 30).addBox(-1F, -1.5F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(0, 57).addBox(-1F, -2.8F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.15F, -0.2F, -0.15F))
                .texOffs(19, 57).addBox(-1F, 4.5F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, 6.2F, 0F, -1.3963F, 0F, 0F));
        PartDefinition p_whip_1 = p_whip.addOrReplaceChild("whip_1", CubeListBuilder.create()
                .texOffs(123, 42).addBox(-0.5F, -0.2F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.025F, -0.2F, -0.025F)),
                PartPose.offsetAndRotation(0F, 5.4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_whip_2 = p_whip_1.addOrReplaceChild("whip_2", CubeListBuilder.create()
                .texOffs(128, 42).addBox(-0.5F, -0.3F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, 4.6F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_whip_3 = p_whip_2.addOrReplaceChild("whip_3", CubeListBuilder.create()
                .texOffs(133, 42).addBox(-0.5F, -0.4F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.09F, -0.4F, -0.09F)),
                PartPose.offsetAndRotation(0F, 4.4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_whip_4 = p_whip_3.addOrReplaceChild("whip_4", CubeListBuilder.create()
                .texOffs(121, 51).addBox(-0.5F, 0F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.13F, 0F, -0.13F)),
                PartPose.offsetAndRotation(0F, 4.2F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_whip_5 = p_whip_4.addOrReplaceChild("whip_5", CubeListBuilder.create()
                .texOffs(126, 51).addBox(-0.5F, -0.1F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.18F, -0.1F, -0.18F)),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_whip_6 = p_whip_5.addOrReplaceChild("whip_6", CubeListBuilder.create()
                .texOffs(131, 51).addBox(-0.5F, -0.2F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.24F, -0.2F, -0.24F)),
                PartPose.offsetAndRotation(0F, 3.8F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_wheel = p_torso.addOrReplaceChild("wheel", CubeListBuilder.create()
                .texOffs(42, 57).addBox(-1F, -1F, -0.5F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, -6F, 4.6F));
        PartDefinition p_shard_0 = p_wheel.addOrReplaceChild("shard_0", CubeListBuilder.create()
                .texOffs(60, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(74, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_shard_1 = p_wheel.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(67, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(79, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_shard_2 = p_wheel.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(74, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(84, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 1.5708F));
        PartDefinition p_shard_3 = p_wheel.addOrReplaceChild("shard_3", CubeListBuilder.create()
                .texOffs(81, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(89, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 2.3562F));
        PartDefinition p_shard_4 = p_wheel.addOrReplaceChild("shard_4", CubeListBuilder.create()
                .texOffs(88, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(94, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 3.1416F));
        PartDefinition p_shard_5 = p_wheel.addOrReplaceChild("shard_5", CubeListBuilder.create()
                .texOffs(95, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(99, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 3.927F));
        PartDefinition p_shard_6 = p_wheel.addOrReplaceChild("shard_6", CubeListBuilder.create()
                .texOffs(102, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(104, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 4.7124F));
        PartDefinition p_shard_7 = p_wheel.addOrReplaceChild("shard_7", CubeListBuilder.create()
                .texOffs(109, 51).addBox(-1F, -9F, -0.5F, 2F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.1F))
                .texOffs(109, 57).addBox(-0.5F, -9.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.05F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 5.4978F));
        PartDefinition p_left_wing = p_torso.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(106, 16).addBox(0F, -7F, 0F, 12F, 12F, 0F),
                PartPose.offsetAndRotation(1.6F, -6.6F, 2.8F, 0F, -0.4189F, -0.2094F));
        PartDefinition p_left_wing_tip = p_left_wing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(0, 16).addBox(0F, -6F, 0F, 12F, 13F, 0F),
                PartPose.offsetAndRotation(11F, -4F, 0F, 0F, -0.2443F, -0.3142F));
        PartDefinition p_right_wing = p_torso.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(131, 16).addBox(-12F, -7F, 0F, 12F, 12F, 0F),
                PartPose.offsetAndRotation(-1.6F, -6.6F, 2.8F, 0F, 0.4189F, 0.2094F));
        PartDefinition p_right_wing_tip = p_right_wing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(25, 16).addBox(-12F, -6F, 0F, 12F, 13F, 0F),
                PartPose.offsetAndRotation(-11F, -4F, 0F, 0F, 0.2443F, 0.3142F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
