package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Bären (Skelett wie beim Eisbären, Buckel beim Braunbären).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BearGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private BearGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 45).addBox(-3.5F, -3F, -3F, 7F, 7F, 7F)
                .texOffs(82, 45).addBox(-2.5F, 1F, -6F, 5F, 3F, 3F)
                .texOffs(56, 45).addBox(-4.3F, -0.5F, -2.5F, 0.8F, 4.5F, 4.5F)
                .texOffs(69, 45).addBox(3.5F, -0.5F, -2.5F, 0.8F, 4.5F, 4.5F),
                PartPose.offset(0F, 10F, -16F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(99, 45).addBox(-1F, -1.5F, -0.5F, 2F, 2F, 1F),
                PartPose.offset(-3.5F, -3F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(106, 45).addBox(-1F, -1.5F, -0.5F, 2F, 2F, 1F),
                PartPose.offset(3.5F, -3F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -13F, -7F, 14F, 14F, 11F)
                .texOffs(51, 0).addBox(-4F, -25F, -7F, 12F, 12F, 10F)
                .texOffs(25, 26).addBox(-5.5F, -14F, 3.5F, 15F, 15F, 1.5F),
                PartPose.offsetAndRotation(-2F, 9F, 12F, 1.5708F, 0F, 0F));
        PartDefinition p_hump = p_body.addOrReplaceChild("hump", CubeListBuilder.create()
                .texOffs(29, 45).addBox(-3F, -22F, -8.5F, 10F, 8F, 2.5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-2F, 0F, -2F, 4F, 10F, 8F),
                PartPose.offset(-4.5F, 14F, 6F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 26).addBox(-2F, 0F, -2F, 4F, 10F, 8F),
                PartPose.offset(4.5F, 14F, 6F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(60, 26).addBox(-2F, 0F, -2F, 4F, 10F, 6F),
                PartPose.offset(-3.5F, 14F, -8F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(81, 26).addBox(-2F, 0F, -2F, 4F, 10F, 6F),
                PartPose.offset(3.5F, 14F, -8F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
