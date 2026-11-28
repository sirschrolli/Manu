package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Glutkeiler und Moderkeiler (Knochen wie beim Vanilla-Hoglin; Pilze nur beim Karmesinkeiler, Kruste nur beim Schlackenkeiler bemalt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlutkeilerGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 128;

    private GlutkeilerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(39, 0).addBox(-8F, -7F, -13F, 16F, 14F, 13F)
                .texOffs(95, 57).addBox(-7F, -9.6F, -12.5F, 14F, 3F, 11F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(181, 74).addBox(-7.5F, -6F, -1.2F, 15F, 2F, 5F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(0, 31).addBox(-7.5F, -5F, -0.25F, 15F, 12F, 13F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(175, 0).addBox(-9F, -9.1F, -13.8F, 18F, 13F, 12F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(98, 0).addBox(-8.5F, 4.1F, -11.5F, 17F, 4F, 21F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(240, 74).addBox(-1F, -4.05F, 12.4F, 2F, 6F, 1F, new CubeDeformation(-0.25F, -0.25F, 0F))
                .texOffs(122, 88).addBox(-1.5F, 1.1F, 11.9F, 3F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(170, 31).addBox(-8.5F, -10.1F, -12.7F, 17F, 6F, 10F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(147, 88).addBox(2.7F, -7.2F, 3F, 1F, 3F, 1F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(46, 88).addBox(1.2F, -8.5F, 1.5F, 4F, 2F, 4F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(159, 88).addBox(-3.1F, -6.4F, 6.7F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(63, 88).addBox(-4.6F, -7.9F, 5.2F, 4F, 2F, 4F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(152, 88).addBox(-5.3F, -7.3F, 1.2F, 3F, 3F, 0F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(80, 88).addBox(-3.8F, -7.3F, -0.3F, 0F, 3F, 3F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(164, 88).addBox(0.9F, -6.6F, 9.4F, 3F, 2F, 0F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(133, 88).addBox(2.4F, -6.6F, 7.9F, 0F, 2F, 3F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(171, 88).addBox(-1.1F, -6.5F, 5.6F, 3F, 2F, 0F, new CubeDeformation(-0.3F, -0.1F, 0F))
                .texOffs(140, 88).addBox(0.4F, -6.5F, 4.1F, 0F, 2F, 3F, new CubeDeformation(0F, -0.1F, -0.3F)),
                PartPose.offset(0F, 7F, 0F));
        PartDefinition p_mane = p_body.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(127, 31).addBox(-2F, 2.2F, -8.9F, 4F, 6F, 17F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(0, 0).addBox(0F, -2.7F, -9.5F, 0F, 11F, 19F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offset(0F, -14F, -7F));
        PartDefinition p_mane_left = p_mane.addOrReplaceChild("mane_left", CubeListBuilder.create()
                .texOffs(57, 31).addBox(0F, -7.4F, -8.8F, 0F, 8F, 17F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1.2F, 4F, 0F, 0F, 0F, 0.5236F));
        PartDefinition p_mane_right = p_mane.addOrReplaceChild("mane_right", CubeListBuilder.create()
                .texOffs(92, 31).addBox(0F, -7.4F, -8.8F, 0F, 8F, 17F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1.2F, 4F, 0F, 0F, 0F, -0.5236F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-6.5F, -3.5F, -9F, 13F, 7F, 9F)
                .texOffs(87, 88).addBox(-7F, -4.95F, -7F, 14F, 2F, 3F, new CubeDeformation(-0.1F, -0.35F, -0.2F))
                .texOffs(146, 57).addBox(-5F, -3F, -17F, 10F, 6F, 8F)
                .texOffs(140, 74).addBox(-4.5F, -3F, -19F, 9F, 6F, 2F, new CubeDeformation(-0.3F, -0.4F, 0F))
                .texOffs(19, 74).addBox(-4.5F, 2.8F, -15.6F, 9F, 2F, 9F, new CubeDeformation(-0.1F, -0.2F, 0F))
                .texOffs(233, 57).addBox(-7.9F, -2.9F, -8.5F, 2F, 6F, 7F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(0, 74).addBox(5.9F, -2.9F, -8.5F, 2F, 6F, 7F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(0F, 2F, -12F, 0.8727F, 0F, 0F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(163, 74).addBox(-1F, -5.7F, -1F, 2F, 6F, 2F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(-5.4F, 1.6F, -14.2F));
        PartDefinition p_right_horn_tip = p_right_horn.addOrReplaceChild("right_horn_tip", CubeListBuilder.create()
                .texOffs(222, 74).addBox(-1F, -4.8F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.2F, 0F, -0.5236F, 0F, -0.2094F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(0, 88).addBox(-5.75F, -0.5F, -2.5F, 6F, 1F, 5F, new CubeDeformation(-0.25F, 0F, -0.3F)),
                PartPose.offsetAndRotation(-6F, -2.8F, -3F, 0F, 0F, -0.6981F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(172, 74).addBox(-1F, -5.7F, -1F, 2F, 6F, 2F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(5.4F, 1.6F, -14.2F));
        PartDefinition p_left_horn_tip = p_left_horn.addOrReplaceChild("left_horn_tip", CubeListBuilder.create()
                .texOffs(231, 74).addBox(-1F, -4.8F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.2F, 0F, -0.5236F, 0F, 0.2094F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(23, 88).addBox(-0.25F, -0.5F, -2.5F, 6F, 1F, 5F, new CubeDeformation(-0.25F, 0F, -0.3F)),
                PartPose.offsetAndRotation(6F, -2.8F, -3F, 0F, 0F, 0.6981F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(45, 57).addBox(-3F, -0.25F, -3F, 6F, 9F, 6F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(56, 74).addBox(-2.5F, 8.25F, -2.5F, 5F, 6F, 5F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(-4F, 10F, -8.5F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(70, 57).addBox(-3F, -0.25F, -3F, 6F, 9F, 6F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(77, 74).addBox(-2.5F, 8.25F, -2.5F, 5F, 6F, 5F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(4F, 10F, -8.5F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(183, 57).addBox(-3F, -0.4F, -3F, 6F, 7F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(98, 74).addBox(-2.5F, 6.1F, -2.5F, 5F, 5F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offset(-5F, 13F, 10F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(208, 57).addBox(-3F, -0.4F, -3F, 6F, 7F, 6F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(119, 74).addBox(-2.5F, 6.1F, -2.5F, 5F, 5F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offset(5F, 13F, 10F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
