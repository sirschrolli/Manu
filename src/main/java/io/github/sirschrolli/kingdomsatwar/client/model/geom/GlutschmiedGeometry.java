package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Glutschmieds Malleus (im Spiel 1,4-fach vergrößert).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlutschmiedGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private GlutschmiedGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(83, 59).addBox(-4.5F, -1F, -2.5F, 9F, 3F, 5F)
                .texOffs(114, 46).addBox(-1F, -9F, 0.6F, 2F, 9F, 2F)
                .texOffs(18, 0).addBox(-5.5F, -17F, -3.5F, 11F, 9F, 7F)
                .texOffs(60, 69).addBox(-2F, -15F, -1.5F, 4F, 4F, 3F)
                .texOffs(39, 46).addBox(-8F, -19.5F, -4F, 16F, 3F, 8F)
                .texOffs(50, 59).addBox(-5F, -2.4F, -3F, 10F, 2F, 6F),
                PartPose.offsetAndRotation(0F, 6F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_apron = p_body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(94, 33).addBox(-4F, 0F, -0.8F, 8F, 11F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1F, -3.2F, -0.0698F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(55, 0).addBox(-3.5F, -7F, -4.5F, 7F, 7F, 7F)
                .texOffs(94, 69).addBox(-2.5F, -3.5F, -6.9F, 5F, 3F, 3F, new CubeDeformation(0F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -19.5F, -1F, -0.1047F, 0F, 0F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(85, 78).addBox(1.5F, -8.5F, -2.3F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(98, 78).addBox(3.1F, -8.9F, -0.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(111, 78).addBox(4.4F, -7.7F, 1.1F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(0, 85).addBox(5.1F, -5.5F, 1.3F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(104, 85).addBox(5.6F, -3F, 0.6F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(113, 85).addBox(5.1F, -1.6F, -1.2F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(0, 92).addBox(4.4F, -0.8F, -2.6F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(13, 85).addBox(-4.5F, -8.5F, -2.3F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(26, 85).addBox(-6.1F, -8.9F, -0.5F, 3F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(39, 85).addBox(-7.4F, -7.7F, 1.1F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(52, 85).addBox(-8.1F, -5.5F, 1.3F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(9, 92).addBox(-7.6F, -3F, 0.6F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(18, 92).addBox(-7.1F, -1.6F, -1.2F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(27, 92).addBox(-6.4F, -0.8F, -2.6F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_flames = p_head.addOrReplaceChild("flames", CubeListBuilder.create(),
                PartPose.offset(0F, -7F, -1F));
        PartDefinition p_flame_1 = p_flames.addOrReplaceChild("flame_1", CubeListBuilder.create()
                .texOffs(111, 69).addBox(-4F, -6F, 0F, 8F, 6F, 0F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_flame_2 = p_flames.addOrReplaceChild("flame_2", CubeListBuilder.create()
                .texOffs(0, 78).addBox(-4F, -6F, 0F, 8F, 6F, 0F),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 2.3562F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(75, 69).addBox(-2.5F, -0.1F, -3.2F, 5F, 2F, 4F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, -0.8F, -3.2F, 0.1396F, 0F, 0F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(84, 0).addBox(-3F, -3.25F, -3.5F, 6F, 5F, 7F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(113, 33).addBox(-1.5F, 0F, -1.5F, 3F, 9F, 3F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(17, 78).addBox(-2F, 8F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(8.5F, -17.5F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(0, 46).addBox(-1.5F, -0.25F, -1.5F, 3F, 9F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.3F))
                .texOffs(112, 59).addBox(-2F, 2F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.05F, 0.05F, -0.05F))
                .texOffs(0, 69).addBox(-2F, 7.95F, -2F, 4F, 4F, 4F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 9F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_hammer = p_left_forearm.addOrReplaceChild("hammer", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-1F, -4F, -1F, 2F, 16F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(27, 33).addBox(-2.5F, 8.75F, -3.5F, 5F, 5F, 7F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(82, 85).addBox(-2F, 9.25F, -4.35F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.15F))
                .texOffs(93, 85).addBox(-2F, 9.25F, 3.35F, 4F, 4F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.15F)),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-3F, -3.25F, -3.5F, 6F, 5F, 7F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(13, 46).addBox(-1.5F, 0F, -1.5F, 3F, 9F, 3F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(34, 78).addBox(-2F, 8F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(-8.5F, -17.5F, 0F, 0F, 0F, 0.1396F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(26, 46).addBox(-1.5F, -0.25F, -1.5F, 3F, 9F, 3F, new CubeDeformation(-0.3F, -0.25F, -0.3F))
                .texOffs(17, 69).addBox(-2F, 2F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.05F, 0.05F, -0.05F))
                .texOffs(34, 69).addBox(-2F, 7.95F, -2F, 4F, 4F, 4F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, 9F, 0F, -0.2094F, 0F, 0F));
        PartDefinition p_spear = p_right_forearm.addOrReplaceChild("spear", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -9F, -1F, 2F, 30F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(36, 92).addBox(-1F, -6.4F, -1F, 2F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(45, 92).addBox(-1F, 11.6F, -1F, 2F, 2F, 2F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(65, 85).addBox(-2.5F, 20.3F, -1.5F, 5F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.4F))
                .texOffs(51, 69).addBox(-1.5F, 21.75F, -0.5F, 3F, 7F, 1F, new CubeDeformation(-0.1F, -0.25F, 0F))
                .texOffs(54, 92).addBox(-1F, 28.25F, -0.5F, 2F, 3F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.1F))
                .texOffs(61, 92).addBox(-0.5F, 30.75F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(0F, 10F, 0F, -1.5708F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(88, 46).addBox(-1.5F, -0.25F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(51, 78).addBox(-2F, 6.9F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(52, 33).addBox(-2.5F, 8.3F, -2.5F, 5F, 7F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(0, 59).addBox(-2.5F, 15F, -3.9F, 5F, 2F, 7F, new CubeDeformation(-0.1F, 0F, -0.3F)),
                PartPose.offset(3.6F, 7F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(101, 46).addBox(-1.5F, -0.25F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(68, 78).addBox(-2F, 6.9F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(73, 33).addBox(-2.5F, 8.3F, -2.5F, 5F, 7F, 5F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(25, 59).addBox(-2.5F, 15F, -3.9F, 5F, 2F, 7F, new CubeDeformation(-0.1F, 0F, -0.3F)),
                PartPose.offset(-3.6F, 7F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
