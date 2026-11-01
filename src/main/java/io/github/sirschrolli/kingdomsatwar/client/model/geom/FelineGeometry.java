package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Hauskatzen und Wildkatzen (Pinsel, Stummelschwanz, Halskrause und Halsband als eigene Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FelineGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private FelineGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 23).addBox(-2.5F, -2F, -3F, 5F, 4F, 5F)
                .texOffs(9, 42).addBox(-1.5F, -0.001F, -4F, 3F, 2F, 2F),
                PartPose.offset(0F, 15F, -9F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(20, 42).addBox(-0.9F, -1.75F, -0.7F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(1.5F, -2F, 0.5F));
        PartDefinition p_tuft_l = p_ear_l.addOrReplaceChild("tuft_l", CubeListBuilder.create()
                .texOffs(38, 42).addBox(-0.3F, -3.3F, -0.2F, 1F, 2F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(29, 42).addBox(-1.1F, -1.75F, -0.7F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(-1.5F, -2F, 0.5F));
        PartDefinition p_tuft_r = p_ear_r.addOrReplaceChild("tuft_r", CubeListBuilder.create()
                .texOffs(43, 42).addBox(-0.5F, -3.3F, -0.2F, 1F, 2F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cheeks = p_head.addOrReplaceChild("cheeks", CubeListBuilder.create()
                .texOffs(43, 33).addBox(-3.4F, -0.6F, -2.2F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(52, 33).addBox(2.4F, -0.6F, -2.2F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-2F, 3F, -8F, 4F, 16F, 6F),
                PartPose.offsetAndRotation(0F, 12F, -10F, 1.5708F, 0F, 0F));
        PartDefinition p_fluff_body = p_body.addOrReplaceChild("fluff_body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, 3.5F, -8.5F, 5F, 15F, 7F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-2.5F, 3.4F, -8.5F, 5F, 1F, 7F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_collar_fluffy = p_body.addOrReplaceChild("collar_fluffy", CubeListBuilder.create()
                .texOffs(0, 23).addBox(-3F, 3.4F, -9F, 6F, 1F, 8F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail1 = root.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(50, 23).addBox(-0.5F, 0F, 0F, 1F, 8F, 1F),
                PartPose.offsetAndRotation(0F, 15F, 8F, 0.9006F, 0F, 0F));
        PartDefinition p_tail_stub = p_tail1.addOrReplaceChild("tail_stub", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-1F, -0.2F, -0.5F, 2F, 4F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail2 = root.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(55, 23).addBox(-0.5F, 0F, 0F, 1F, 8F, 1F),
                PartPose.offset(0F, 20F, 14F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(25, 33).addBox(-1F, 0F, 1F, 2F, 6F, 2F),
                PartPose.offset(1.1F, 18F, 5F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(34, 33).addBox(-1F, 0F, 1F, 2F, 6F, 2F),
                PartPose.offset(-1.1F, 18F, 5F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1F, 0F, 0F, 2F, 10F, 2F),
                PartPose.offset(1.2F, 14.1F, -5F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(55, 0).addBox(-1F, 0F, 0F, 2F, 10F, 2F),
                PartPose.offset(-1.2F, 14.1F, -5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
