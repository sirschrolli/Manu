package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Glut-Frischlinge und jungen Moderkeiler (Knochen wie beim Vanilla-Hoglin-Jungtier).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class GlutkeilerBabyGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 128;

    private GlutkeilerBabyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(23, 23).addBox(-4.5F, -3F, -6.75F, 9F, 6F, 7F, new CubeDeformation(0F, -0.4F, -0.25F))
                .texOffs(26, 52).addBox(-3.5F, -2F, -10.5F, 7F, 4F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(0, 70).addBox(-3F, -2F, -11.5F, 6F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, 0F))
                .texOffs(0, 62).addBox(-1.5F, -4.1F, -5.4F, 3F, 2F, 5F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(29, 70).addBox(-3.7F, -1.1F, -9.8F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(34, 70).addBox(2.7F, -1.1F, -9.8F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 13F, -7F, 0.8727F, 0F, 0F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(17, 62).addBox(-4.7F, -0.5F, -2F, 5F, 1F, 4F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-4.3F, -1.8F, -1.6F, 0F, 0F, -0.8727F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(36, 62).addBox(-0.3F, -0.5F, -2F, 5F, 1F, 4F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offsetAndRotation(4.3F, -1.8F, -1.6F, 0F, 0F, 0.8727F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -14F, -7F, 8F, 8F, 14F)
                .texOffs(0, 39).addBox(-1.5F, -15.5F, -7.7F, 3F, 2F, 10F, new CubeDeformation(-0.4F, -0.2F, -0.3F))
                .texOffs(0, 23).addBox(0F, -17.9F, -8.1F, 0F, 4F, 11F, new CubeDeformation(0F, -0.1F, -0.3F))
                .texOffs(24, 70).addBox(-0.5F, -13.6F, 6.9F, 1F, 3F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(15, 70).addBox(-1F, -11.4F, 6.4F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(27, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F),
                PartPose.offset(-2.5F, 18F, 4.5F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(40, 39).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F),
                PartPose.offset(2.5F, 18F, 4.5F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F),
                PartPose.offset(-2.5F, 18F, -4.5F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(13, 52).addBox(-1.5F, 0F, -1.5F, 3F, 6F, 3F),
                PartPose.offset(2.5F, 18F, -4.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
