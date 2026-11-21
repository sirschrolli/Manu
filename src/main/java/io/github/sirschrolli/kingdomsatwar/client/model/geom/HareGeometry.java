package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Hase und Kaninchen.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class HareGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private HareGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -2.5F, -4.5F, 5F, 5F, 8F)
                .texOffs(18, 24).addBox(-2F, -3F, -1.5F, 4F, 1F, 5F),
                PartPose.offsetAndRotation(0F, 19F, 2F, -0.3142F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-1F, -1F, -0.75F, 2F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.2F, 3.4F, -0.5236F, 0F, 0F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(27, 0).addBox(-2F, -3.6F, -4.4F, 4F, 4F, 5F, new CubeDeformation(0F, -0.1F, -0.4F))
                .texOffs(51, 24).addBox(-1.5F, -2F, -5.4F, 3F, 2F, 2F, new CubeDeformation(-0.25F, 0F, -0.4F))
                .texOffs(9, 32).addBox(1.7F, -3.2F, -3.3F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(16, 32).addBox(-2.7F, -3.2F, -3.3F, 1F, 2F, 2F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(0F, 17.1F, -1.5F));
        PartDefinition p_ear_long_l = p_head.addOrReplaceChild("ear_long_l", CubeListBuilder.create()
                .texOffs(15, 14).addBox(-1F, -7F, -0.55F, 2F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F)),
                PartPose.offsetAndRotation(1F, -3.3F, -1.2F, -0.384F, -0.1396F, 0.1745F));
        PartDefinition p_ear_short_l = p_head.addOrReplaceChild("ear_short_l", CubeListBuilder.create()
                .texOffs(37, 24).addBox(-1F, -5F, -0.55F, 2F, 5F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F)),
                PartPose.offsetAndRotation(1F, -3.3F, -1.2F, -0.3142F, -0.1396F, 0.2094F));
        PartDefinition p_ear_long_r = p_head.addOrReplaceChild("ear_long_r", CubeListBuilder.create()
                .texOffs(22, 14).addBox(-1F, -7F, -0.55F, 2F, 7F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F)),
                PartPose.offsetAndRotation(-1F, -3.3F, -1.2F, -0.384F, 0.1396F, -0.1745F));
        PartDefinition p_ear_short_r = p_head.addOrReplaceChild("ear_short_r", CubeListBuilder.create()
                .texOffs(44, 24).addBox(-1F, -5F, -0.55F, 2F, 5F, 1F, new CubeDeformation(-0.1F, 0F, -0.05F)),
                PartPose.offsetAndRotation(-1F, -3.3F, -1.2F, -0.3142F, 0.1396F, -0.2094F));
        PartDefinition p_front_leg_l = root.addOrReplaceChild("front_leg_l", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-1F, -0.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(23, 32).addBox(-1F, 3.7F, -1.6F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(1.4F, 19.5F, -2F));
        PartDefinition p_haunch_l = root.addOrReplaceChild("haunch_l", CubeListBuilder.create()
                .texOffs(46, 0).addBox(-1F, -2F, -2.5F, 2F, 4F, 5F),
                PartPose.offset(2.4F, 19.5F, 3F));
        PartDefinition p_hind_foot_l = p_haunch_l.addOrReplaceChild("hind_foot_l", CubeListBuilder.create()
                .texOffs(29, 14).addBox(-1F, 1.95F, -5F, 2F, 1F, 6F, new CubeDeformation(0F, -0.05F, 0F)),
                PartPose.offset(0F, 2F, 1.5F));
        PartDefinition p_front_leg_r = root.addOrReplaceChild("front_leg_r", CubeListBuilder.create()
                .texOffs(9, 24).addBox(-1F, -0.25F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F))
                .texOffs(32, 32).addBox(-1F, 3.7F, -1.6F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(-1.4F, 19.5F, -2F));
        PartDefinition p_haunch_r = root.addOrReplaceChild("haunch_r", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1F, -2F, -2.5F, 2F, 4F, 5F),
                PartPose.offset(-2.4F, 19.5F, 3F));
        PartDefinition p_hind_foot_r = p_haunch_r.addOrReplaceChild("hind_foot_r", CubeListBuilder.create()
                .texOffs(46, 14).addBox(-1F, 1.95F, -5F, 2F, 1F, 6F, new CubeDeformation(0F, -0.05F, 0F)),
                PartPose.offset(0F, 2F, 1.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
