package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des jungen Urwühlers (Knochen wie beim Vanilla-Schnüfflerjungen; Flaum, Stirnbüschel, Farnsprössling).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PrimevalDiggerBabyGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private PrimevalDiggerBabyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bone = root.addOrReplaceChild("bone", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_body = p_bone.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-13F, -14F, -0.5F, 14F, 15F, 20F)
                .texOffs(0, 36).addBox(-13F, -14F, -0.5F, 14F, 13F, 20F, new CubeDeformation(0.5F))
                .texOffs(99, 80).addBox(-7.5F, -16.75F, 8.5F, 3F, 3F, 0F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(92, 80).addBox(-6F, -16.75F, 7F, 0F, 3F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(6F, -3F, -9.5F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(69, 36).addBox(-5F, -4.25F, -7.5F, 10F, 9F, 9F)
                .texOffs(63, 80).addBox(-5.5F, -5.1F, -8.4F, 11F, 3F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.3F))
                .texOffs(46, 80).addBox(-2F, -6.8F, -6F, 4F, 3F, 4F, new CubeDeformation(0F, -0.4F, 0F)),
                PartPose.offset(-6F, -4.75F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(108, 36).addBox(0F, 0F, -2F, 1F, 11F, 3F),
                PartPose.offset(5F, -4.25F, -1.5F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(117, 36).addBox(-1F, 0F, -2F, 1F, 11F, 3F),
                PartPose.offset(-5F, -4.25F, -1.5F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(17, 80).addBox(-5F, -3F, -2F, 10F, 3F, 4F),
                PartPose.offset(0F, -1.25F, -9.5F));
        PartDefinition p_lower_beak = p_head.addOrReplaceChild("lower_beak", CubeListBuilder.create()
                .texOffs(0, 70).addBox(-5F, -2.5F, -2F, 10F, 5F, 4F),
                PartPose.offset(0F, 1.25F, -9.5F));
        PartDefinition p_right_front_leg = p_bone.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(29, 70).addBox(-2F, -1F, -2F, 4F, 5F, 4F),
                PartPose.offset(-4F, -4F, -7F));
        PartDefinition p_right_mid_leg = p_bone.addOrReplaceChild("right_mid_leg", CubeListBuilder.create()
                .texOffs(46, 70).addBox(-2F, -1F, -2F, 4F, 5F, 4F),
                PartPose.offset(-4F, -4F, 0F));
        PartDefinition p_right_hind_leg = p_bone.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(63, 70).addBox(-2F, -1F, -2F, 4F, 5F, 4F),
                PartPose.offset(-4F, -4F, 7F));
        PartDefinition p_left_front_leg = p_bone.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(80, 70).addBox(-2F, -1F, -2F, 4F, 5F, 4F),
                PartPose.offset(4F, -4F, -7F));
        PartDefinition p_left_mid_leg = p_bone.addOrReplaceChild("left_mid_leg", CubeListBuilder.create()
                .texOffs(97, 70).addBox(-2F, -1F, -2F, 4F, 5F, 4F),
                PartPose.offset(4F, -4F, 0F));
        PartDefinition p_left_hind_leg = p_bone.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 80).addBox(-2F, -1F, -2F, 4F, 5F, 4F),
                PartPose.offset(4F, -4F, 7F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
