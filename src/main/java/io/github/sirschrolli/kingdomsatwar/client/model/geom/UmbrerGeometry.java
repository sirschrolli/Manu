package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Umbrer (alle Berufe, Zubehör je Beruf).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class UmbrerGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private UmbrerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(118, 32).addBox(-1F, 0F, -1F, 2F, 11F, 2F)
                .texOffs(102, 72).addBox(-1.5F, 10.25F, -3.2F, 3F, 2F, 4F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offset(1.6F, 12F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-1F, 0F, -1F, 2F, 11F, 2F)
                .texOffs(0, 81).addBox(-1.5F, 10.25F, -3.2F, 3F, 2F, 4F, new CubeDeformation(-0.3F, -0.25F, 0F)),
                PartPose.offset(-1.6F, 12F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(57, 0).addBox(-3.5F, -12F, -2.5F, 7F, 12F, 5F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(73, 61).addBox(-4F, -1.6F, -3F, 8F, 2F, 6F, new CubeDeformation(-0.2F, 0F, -0.45F))
                .texOffs(15, 81).addBox(-1.5F, -1.45F, -3.2F, 3F, 5F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.3F))
                .texOffs(80, 47).addBox(-4.5F, -13F, -3F, 9F, 4F, 6F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_skirt = p_body.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(60, 32).addBox(-4F, 0F, -3F, 8F, 7F, 6F, new CubeDeformation(-0.2F, 0F, -0.45F))
                .texOffs(25, 32).addBox(-5F, 7F, -3.5F, 10F, 6F, 7F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-3F, -8F, -3F, 6F, 8F, 6F)
                .texOffs(63, 88).addBox(-0.5F, -4.4F, -3.85F, 1F, 2F, 1F, new CubeDeformation(0F, 0F, -0.15F))
                .texOffs(111, 88).addBox(-2.65F, -5.2F, -3.5F, 2F, 1F, 1F, new CubeDeformation(-0.25F, 0F, -0.45F))
                .texOffs(118, 88).addBox(0.65F, -5.2F, -3.5F, 2F, 1F, 1F, new CubeDeformation(-0.25F, 0F, -0.45F)),
                PartPose.offset(0F, -12F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(112, 81).addBox(0F, -1F, -0.5F, 5F, 2F, 1F, new CubeDeformation(0F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(3F, -4.6F, -0.4F, -0.2094F, -0.4887F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(0, 88).addBox(-5F, -1F, -0.5F, 5F, 2F, 1F, new CubeDeformation(0F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(-3F, -4.6F, -0.4F, -0.2094F, 0.4887F, 0F));
        PartDefinition p_hood = p_head.addOrReplaceChild("hood", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-4F, -8.7F, -3.9F, 8F, 9F, 8F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(85, 72).addBox(-2F, -9.6F, 1F, 4F, 2F, 4F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(73, 88).addBox(-4F, -8.7F, -3.9F, 8F, 1F, 1F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_sprig = p_hood.addOrReplaceChild("sprig", CubeListBuilder.create()
                .texOffs(76, 81).addBox(-0.5F, -3.75F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(24, 88).addBox(-1.5F, -4.9F, -0.5F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(3.4F, -7F, -1F, 0F, 0F, 0.4363F));
        PartDefinition p_seer_hat = p_hood.addOrReplaceChild("seer_hat", CubeListBuilder.create()
                .texOffs(48, 61).addBox(-3F, -2.8F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(68, 72).addBox(-2F, -5.2F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(49, 81).addBox(-1F, -7.4F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(68, 88).addBox(-0.5F, -8.5F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, -8.7F, 0F, -0.1396F, 0F, 0F));
        PartDefinition p_goggles = p_head.addOrReplaceChild("goggles", CubeListBuilder.create()
                .texOffs(93, 81).addBox(-4F, -8F, -4F, 8F, 2F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.25F))
                .texOffs(42, 88).addBox(-2.7F, -8F, -4.3F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F))
                .texOffs(49, 88).addBox(0.7F, -8F, -4.3F, 2F, 2F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_straw_hat = p_head.addOrReplaceChild("straw_hat", CubeListBuilder.create()
                .texOffs(9, 47).addBox(-5.5F, -0.7F, -5.5F, 11F, 1F, 11F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 61).addBox(-3.5F, -3.1F, -3.5F, 7F, 3F, 7F, new CubeDeformation(-0.1F, -0.3F, -0.1F)),
                PartPose.offset(0F, -8F, 0F));
        PartDefinition p_helmet = p_head.addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(89, 32).addBox(-3.5F, -9F, -3.5F, 7F, 6F, 7F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(29, 61).addBox(-1F, -11.3F, -3.4F, 2F, 3F, 7F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(88, 81).addBox(-0.5F, -4.6F, -4.15F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(54, 47).addBox(-1.5F, -1F, -1.5F, 3F, 8F, 3F)
                .texOffs(21, 72).addBox(-2F, 6.25F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(58, 81).addBox(-1F, 9.25F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(4.6F, -11F, 0F));
        PartDefinition p_pauldron_l = p_left_arm.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(102, 61).addBox(-2.5F, -1.2F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, -0.1745F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(67, 47).addBox(-1.5F, -1F, -1.5F, 3F, 8F, 3F)
                .texOffs(38, 72).addBox(-2F, 6.25F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(67, 81).addBox(-1F, 9.25F, -1F, 2F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-4.6F, -11F, 0F));
        PartDefinition p_pauldron_r = p_right_arm.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(0, 72).addBox(-2.5F, -1.2F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -1F, 0F, 0F, 0F, 0.1745F));
        PartDefinition p_spear = p_right_arm.addOrReplaceChild("spear", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -16F, -0.5F, 1F, 30F, 1F)
                .texOffs(24, 81).addBox(-1.5F, -21F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.3F, 0F, -0.1F))
                .texOffs(13, 88).addBox(-1.5F, -16.5F, -1F, 3F, 1F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(0F, 10.5F, -0.2F, -1.5708F, 0F, 0F));
        PartDefinition p_apron = p_body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-3F, -7F, -3.05F, 6F, 16F, 1F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(33, 81).addBox(3.5F, -1.75F, -1.7F, 1F, 5F, 1F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(38, 81).addBox(3F, 2.3F, -2.7F, 2F, 2F, 3F, new CubeDeformation(-0.2F, -0.3F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_satchel = p_body.addOrReplaceChild("satchel", CubeListBuilder.create()
                .texOffs(55, 72).addBox(-5.5F, -1.75F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.2F, -0.25F, -0.4F))
                .texOffs(33, 88).addBox(-5.8F, -2.2F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, 0F, -0.4F))
                .texOffs(92, 88).addBox(-4F, -11.9F, -2.9F, 8F, 1F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pendant = p_body.addOrReplaceChild("pendant", CubeListBuilder.create()
                .texOffs(81, 81).addBox(-1F, -9.8F, -2.9F, 2F, 3F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_amulet = p_body.addOrReplaceChild("amulet", CubeListBuilder.create()
                .texOffs(56, 88).addBox(-1F, -9.8F, -2.95F, 2F, 2F, 1F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_armor = p_body.addOrReplaceChild("armor", CubeListBuilder.create()
                .texOffs(97, 0).addBox(-4F, -11.8F, -3F, 8F, 8F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(5, 0).addBox(-4F, 0F, -0.2F, 8F, 21F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -11.5F, 2.3F, 0.1047F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
