package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Verwüster (Knochen wie beim Vanilla-Verwüster, Hörner, Fell und Geschirr).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class RavagerGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private RavagerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_neck = root.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(130, 42).addBox(-5F, -1F, -18F, 10F, 10F, 18F),
                PartPose.offset(0F, -7F, 5.5F));
        PartDefinition p_mane = p_neck.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(122, 79).addBox(-6F, -4.5F, -17F, 12F, 5F, 15F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_dewlap = p_neck.addOrReplaceChild("dewlap", CubeListBuilder.create()
                .texOffs(215, 104).addBox(-3.5F, 8.25F, -16F, 7F, 4F, 12F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(187, 42).addBox(-7F, -19F, -13F, 14F, 12F, 13F)
                .texOffs(44, 123).addBox(-5.5F, -10F, -19F, 11F, 8F, 6F)
                .texOffs(168, 104).addBox(-6F, -7F, -13F, 12F, 5F, 11F),
                PartPose.offset(0F, 16F, -17F));
        PartDefinition p_forelock = p_head.addOrReplaceChild("forelock", CubeListBuilder.create()
                .texOffs(0, 139).addBox(-4.5F, -20.7F, -12.5F, 9F, 3F, 7F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_chanfron = p_head.addOrReplaceChild("chanfron", CubeListBuilder.create()
                .texOffs(75, 139).addBox(-4F, -17.6F, -13.9F, 8F, 9F, 1F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(187, 139).addBox(-2F, -10.7F, -18.5F, 4F, 1F, 6F, new CubeDeformation(-0.4F, -0.2F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_spike = p_chanfron.addOrReplaceChild("spike", CubeListBuilder.create()
                .texOffs(208, 139).addBox(-1.5F, -1.5F, -2.6F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(221, 139).addBox(-1F, -1F, -3.9F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offsetAndRotation(0F, -13.1F, -13.8F, -0.1745F, 0F, 0F));
        PartDefinition p_nose_ring = p_head.addOrReplaceChild("nose_ring", CubeListBuilder.create()
                .texOffs(230, 139).addBox(-2.15F, -4.7F, -19.85F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(235, 139).addBox(1.15F, -4.7F, -19.85F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(11, 150).addBox(-2F, -2.55F, -19.85F, 4F, 1F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(240, 139).addBox(-3.75F, -1F, -1F, 4F, 2F, 1F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offsetAndRotation(-7F, -13.5F, -4.5F, 0F, -0.3491F, 0.4887F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(0, 150).addBox(-0.25F, -1F, -1F, 4F, 2F, 1F, new CubeDeformation(-0.25F, 0F, 0F)),
                PartPose.offsetAndRotation(7F, -13.5F, -4.5F, 0F, 0.3491F, -0.4887F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create(),
                PartPose.offset(-6.2F, -17.2F, -7F));
        PartDefinition p_horn_a_r1 = p_right_horn.addOrReplaceChild("horn_a_r1", CubeListBuilder.create()
                .texOffs(179, 123).addBox(-1.5F, -1.5F, -6.65F, 3F, 3F, 8F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.2433F, 1.4514F, 0F));
        PartDefinition p_horn_a_r2 = p_right_horn.addOrReplaceChild("horn_a_r2", CubeListBuilder.create()
                .texOffs(79, 123).addBox(-1.5F, -1.5F, -7.79F, 3F, 3F, 9F, new CubeDeformation(-0.35F, -0.35F, -0.29F)),
                PartPose.offsetAndRotation(-6.2636F, -1.5659F, -0.7516F, -0.3756F, 0.5028F, 0F));
        PartDefinition p_horn_a_r3 = p_right_horn.addOrReplaceChild("horn_a_r3", CubeListBuilder.create()
                .texOffs(94, 139).addBox(-1F, -1F, -5.95F, 2F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(-9.6261F, -4.317F, -6.8652F, -1.0527F, -0.661F, 0F));
        PartDefinition p_horn_s_r1 = p_right_horn.addOrReplaceChild("horn_s_r1", CubeListBuilder.create()
                .texOffs(104, 123).addBox(-2F, -2F, -5.99F, 4F, 4F, 8F, new CubeDeformation(-0.1F, -0.1F, -0.49F)),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 0.4187F, 1.7197F, 0F));
        PartDefinition p_horn_s_r2 = p_right_horn.addOrReplaceChild("horn_s_r2", CubeListBuilder.create()
                .texOffs(33, 139).addBox(-1.5F, -1.5F, -5.65F, 3F, 3F, 7F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offsetAndRotation(-4.9693F, 2.8362F, 0.7454F, -1.1389F, 1.7895F, 0F));
        PartDefinition p_horn_s_r3 = p_right_horn.addOrReplaceChild("horn_s_r3", CubeListBuilder.create()
                .texOffs(113, 139).addBox(-1.5F, -1.5F, -4.83F, 3F, 3F, 6F, new CubeDeformation(-0.45F, -0.45F, -0.33F)),
                PartPose.offsetAndRotation(-7.217F, -2.1587F, 1.2449F, -0.4116F, -0.245F, 0F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create(),
                PartPose.offset(6.2F, -17.2F, -7F));
        PartDefinition p_horn_a_l1 = p_left_horn.addOrReplaceChild("horn_a_l1", CubeListBuilder.create()
                .texOffs(202, 123).addBox(-1.5F, -1.5F, -6.65F, 3F, 3F, 8F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.2433F, -1.4514F, 0F));
        PartDefinition p_horn_a_l2 = p_left_horn.addOrReplaceChild("horn_a_l2", CubeListBuilder.create()
                .texOffs(129, 123).addBox(-1.5F, -1.5F, -7.79F, 3F, 3F, 9F, new CubeDeformation(-0.35F, -0.35F, -0.29F)),
                PartPose.offsetAndRotation(6.2636F, -1.5659F, -0.7516F, -0.3756F, -0.5028F, 0F));
        PartDefinition p_horn_a_l3 = p_left_horn.addOrReplaceChild("horn_a_l3", CubeListBuilder.create()
                .texOffs(132, 139).addBox(-1F, -1F, -5.95F, 2F, 2F, 7F, new CubeDeformation(-0.25F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(9.6261F, -4.317F, -6.8652F, -1.0527F, 0.661F, 0F));
        PartDefinition p_horn_s_l1 = p_left_horn.addOrReplaceChild("horn_s_l1", CubeListBuilder.create()
                .texOffs(154, 123).addBox(-2F, -2F, -5.99F, 4F, 4F, 8F, new CubeDeformation(-0.1F, -0.1F, -0.49F)),
                PartPose.offsetAndRotation(0F, 0.6F, 0F, 0.4187F, -1.7197F, 0F));
        PartDefinition p_horn_s_l2 = p_left_horn.addOrReplaceChild("horn_s_l2", CubeListBuilder.create()
                .texOffs(54, 139).addBox(-1.5F, -1.5F, -5.65F, 3F, 3F, 7F, new CubeDeformation(0F, 0F, -0.15F)),
                PartPose.offsetAndRotation(4.9693F, 2.8362F, 0.7454F, -1.1389F, -1.7895F, 0F));
        PartDefinition p_horn_s_l3 = p_left_horn.addOrReplaceChild("horn_s_l3", CubeListBuilder.create()
                .texOffs(151, 139).addBox(-1.5F, -1.5F, -4.83F, 3F, 3F, 6F, new CubeDeformation(-0.45F, -0.45F, -0.33F)),
                PartPose.offsetAndRotation(7.217F, -2.1587F, 1.2449F, -0.4116F, 0.245F, 0F));
        PartDefinition p_mouth = p_head.addOrReplaceChild("mouth", CubeListBuilder.create()
                .texOffs(0, 79).addBox(-5F, 0F, -21F, 10F, 3F, 21F),
                PartPose.offset(0F, -2F, 2F));
        PartDefinition p_beard = p_mouth.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(0, 123).addBox(-3.5F, 2.75F, -18F, 7F, 5F, 10F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-7F, -10F, -7F, 14F, 16F, 20F)
                .texOffs(69, 42).addBox(-6F, 6F, -7F, 12F, 13F, 18F),
                PartPose.offsetAndRotation(0F, 1F, 2F, 1.5708F, 0F, 0F));
        PartDefinition p_hump = p_body.addOrReplaceChild("hump", CubeListBuilder.create()
                .texOffs(123, 104).addBox(-8F, -13.7F, -11.4F, 16F, 11F, 6F, new CubeDeformation(-0.1F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -1.5708F, 0F, 0F));
        PartDefinition p_caparison = p_body.addOrReplaceChild("caparison", CubeListBuilder.create()
                .texOffs(63, 79).addBox(-8F, -13.6F, -6F, 16F, 11F, 13F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -1.5708F, 0F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(132, 0).addBox(-7.5F, 2.5F, -10.75F, 15F, 10F, 30F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -1.5708F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(35, 123).addBox(-1F, 0F, -1F, 2F, 13F, 2F),
                PartPose.offsetAndRotation(0F, 19F, 9F, -1.2217F, 0F, 0F));
        PartDefinition p_tuft = p_tail.addOrReplaceChild("tuft", CubeListBuilder.create()
                .texOffs(170, 139).addBox(-2F, 0F, -2F, 4F, 5F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 12.5F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 4F, -4F, 8F, 33F, 8F),
                PartPose.offset(-8F, -13F, 18F));
        PartDefinition p_right_hind_leg_fur = p_right_hind_leg.addOrReplaceChild("right_hind_leg_fur", CubeListBuilder.create()
                .texOffs(177, 79).addBox(-5F, 24F, -5F, 10F, 8F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(33, 0).addBox(-4F, 4F, -4F, 8F, 33F, 8F),
                PartPose.offset(8F, -13F, 18F));
        PartDefinition p_left_hind_leg_fur = p_left_hind_leg.addOrReplaceChild("left_hind_leg_fur", CubeListBuilder.create()
                .texOffs(0, 104).addBox(-5F, 24F, -5F, 10F, 8F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-4F, 4F, -4F, 8F, 33F, 8F),
                PartPose.offset(-8F, -13F, -5F));
        PartDefinition p_right_front_leg_fur = p_right_front_leg.addOrReplaceChild("right_front_leg_fur", CubeListBuilder.create()
                .texOffs(41, 104).addBox(-5F, 24F, -5F, 10F, 8F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(99, 0).addBox(-4F, 4F, -4F, 8F, 33F, 8F),
                PartPose.offset(8F, -13F, -5F));
        PartDefinition p_left_front_leg_fur = p_left_front_leg.addOrReplaceChild("left_front_leg_fur", CubeListBuilder.create()
                .texOffs(82, 104).addBox(-5F, 24F, -5F, 10F, 8F, 10F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
