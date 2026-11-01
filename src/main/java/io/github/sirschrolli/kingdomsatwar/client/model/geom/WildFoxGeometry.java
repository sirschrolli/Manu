package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Füchse (Fennek mit großen Ohren als eigene Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildFoxGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private WildFoxGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 18).addBox(-3F, -2F, -5F, 8F, 6F, 6F)
                .texOffs(0, 31).addBox(-3.6F, 1F, -4.6F, 1F, 3F, 3.4F)
                .texOffs(11, 31).addBox(4.6F, 1F, -4.6F, 1F, 3F, 3.4F),
                PartPose.offset(-1F, 16.5F, -3F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(15, 39).addBox(-1F, -2.5F, 0F, 2F, 2.5F, 1F),
                PartPose.offset(-2F, -2F, -4F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(22, 39).addBox(-1F, -2.5F, 0F, 2F, 2.5F, 1F),
                PartPose.offset(4F, -2F, -4F));
        PartDefinition p_ear_big_r = p_head.addOrReplaceChild("ear_big_r", CubeListBuilder.create()
                .texOffs(37, 31).addBox(-1.5F, -5F, 0F, 3F, 5F, 1F),
                PartPose.offsetAndRotation(-2F, -2F, -4F, 0F, 0F, -0.2443F));
        PartDefinition p_ear_big_l = p_head.addOrReplaceChild("ear_big_l", CubeListBuilder.create()
                .texOffs(46, 31).addBox(-1.5F, -5F, 0F, 3F, 5F, 1F),
                PartPose.offsetAndRotation(4F, -2F, -4F, 0F, 0F, 0.2443F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(0, 39).addBox(-1F, 2.01F, -8F, 4F, 2F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, 3.999F, -3.5F, 6F, 11F, 6F)
                .texOffs(22, 31).addBox(-2.5F, 3.6F, -3.8F, 5F, 3.5F, 2F),
                PartPose.offsetAndRotation(0F, 16F, -6F, 1.5708F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(25, 0).addBox(1.5F, 0F, -1.5F, 5F, 9.5F, 6F),
                PartPose.offsetAndRotation(-4F, 15F, -1F, -0.0524F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(29, 18).addBox(2F, 0.5F, -1F, 2F, 6F, 2F),
                PartPose.offset(-5F, 17.5F, 7F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(38, 18).addBox(2F, 0.5F, -1F, 2F, 6F, 2F),
                PartPose.offset(-1F, 17.5F, 7F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(47, 18).addBox(2F, 0.5F, -1F, 2F, 6F, 2F),
                PartPose.offset(-5F, 17.5F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(56, 18).addBox(2F, 0.5F, -1F, 2F, 6F, 2F),
                PartPose.offset(-1F, 17.5F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
