package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Mooslinge (Arachnophobie-Modus: Spinnenknochen ohne Beine).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MooslingGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private MooslingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7F, -6F, -7F, 14F, 12F, 14F)
                .texOffs(0, 27).addBox(-6F, -7F, -6F, 12F, 1F, 12F)
                .texOffs(49, 27).addBox(-6F, 6F, -6F, 12F, 1F, 12F)
                .texOffs(57, 0).addBox(-8F, -5F, -6F, 1F, 10F, 12F)
                .texOffs(84, 0).addBox(7F, -5F, -6F, 1F, 10F, 12F)
                .texOffs(98, 27).addBox(-6F, -5F, 7F, 12F, 10F, 1F)
                .texOffs(0, 41).addBox(-6F, -5F, -8F, 12F, 10F, 1F),
                PartPose.offset(0F, 16F, 0F));
        PartDefinition p_sprout = p_head.addOrReplaceChild("sprout", CubeListBuilder.create()
                .texOffs(0, 53).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offset(0F, -7F, 0F));
        PartDefinition p_leaf_left = p_sprout.addOrReplaceChild("leaf_left", CubeListBuilder.create()
                .texOffs(95, 41).addBox(0F, -1F, -1.5F, 5F, 1F, 3F),
                PartPose.offsetAndRotation(0F, -3F, 0F, 0F, 0F, 0.5585F));
        PartDefinition p_leaf_right = p_sprout.addOrReplaceChild("leaf_right", CubeListBuilder.create()
                .texOffs(112, 41).addBox(-5F, -1F, -1.5F, 5F, 1F, 3F),
                PartPose.offsetAndRotation(0F, -3F, 0F, 0F, 0F, -0.5585F));
        PartDefinition p_body0 = root.addOrReplaceChild("body0", CubeListBuilder.create()
                .texOffs(27, 41).addBox(-5.5F, 8F, -5F, 4F, 1F, 4F)
                .texOffs(44, 41).addBox(1.5F, 8F, -5F, 4F, 1F, 4F),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_body1 = root.addOrReplaceChild("body1", CubeListBuilder.create()
                .texOffs(61, 41).addBox(-5.5F, 8F, 1F, 4F, 1F, 4F)
                .texOffs(78, 41).addBox(1.5F, 8F, 1F, 4F, 1F, 4F),
                PartPose.offset(0F, 15F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4F, 15F, 2F, 0F, 0.7854F, -0.7854F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4F, 15F, 2F, 0F, -0.7854F, 0.7854F));
        PartDefinition p_right_middle_hind_leg = root.addOrReplaceChild("right_middle_hind_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4F, 15F, 1F, 0F, 0.3927F, -0.5812F));
        PartDefinition p_left_middle_hind_leg = root.addOrReplaceChild("left_middle_hind_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4F, 15F, 1F, 0F, -0.3927F, 0.5812F));
        PartDefinition p_right_middle_front_leg = root.addOrReplaceChild("right_middle_front_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4F, 15F, 0F, 0F, -0.3927F, -0.5812F));
        PartDefinition p_left_middle_front_leg = root.addOrReplaceChild("left_middle_front_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4F, 15F, 0F, 0F, 0.3927F, 0.5812F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(-4F, 15F, -1F, 0F, -0.7854F, -0.7854F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create(),
                PartPose.offsetAndRotation(4F, 15F, -1F, 0F, 0.7854F, 0.7854F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
