package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Rothirschs (mit Geweih).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DeerGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private DeerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -4F, -8F, 7F, 8F, 15F),
                PartPose.offset(0F, 10F, 0F));
        PartDefinition p_neck = p_body.addOrReplaceChild("neck", CubeListBuilder.create()
                .texOffs(18, 24).addBox(-1.5F, -2F, -7F, 3F, 4F, 8F),
                PartPose.offsetAndRotation(0F, -2F, -7F, -0.8727F, 0F, 0F));
        PartDefinition p_head = p_neck.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(41, 24).addBox(-2.5F, -3F, -5F, 5F, 4F, 5F)
                .texOffs(10, 38).addBox(-1.5F, -2F, -8.5F, 3F, 3F, 3.5F)
                .texOffs(15, 47).addBox(2.3F, -1.8F, -4F, 0.5F, 1F, 1F)
                .texOffs(20, 47).addBox(-2.8F, -1.8F, -4F, 0.5F, 1F, 1F),
                PartPose.offsetAndRotation(0F, 0F, -6.5F, 0.8727F, 0F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(25, 38).addBox(-1F, -3.5F, -0.5F, 2F, 3.5F, 1F),
                PartPose.offsetAndRotation(2F, -2.8F, -1.2F, -0.2618F, -0.2618F, 0.6632F));
        PartDefinition p_antler_l = p_head.addOrReplaceChild("antler_l", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(1.3F, -3F, -2.5F, -0.2618F, 0F, 0.384F));
        PartDefinition p_antler_l_tine1 = p_antler_l.addOrReplaceChild("antler_l_tine1", CubeListBuilder.create()
                .texOffs(46, 38).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -2.5F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_antler_l_tine2 = p_antler_l.addOrReplaceChild("antler_l_tine2", CubeListBuilder.create()
                .texOffs(51, 38).addBox(-0.5F, -2.5F, -0.5F, 1F, 2.5F, 1F),
                PartPose.offsetAndRotation(0F, -5F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_antler_l_crown = p_antler_l.addOrReplaceChild("antler_l_crown", CubeListBuilder.create()
                .texOffs(56, 38).addBox(-0.5F, -2.5F, -0.5F, 1F, 2.5F, 1F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.4363F, 0F, -0.2618F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(32, 38).addBox(-1F, -3.5F, -0.5F, 2F, 3.5F, 1F),
                PartPose.offsetAndRotation(-2F, -2.8F, -1.2F, -0.2618F, 0.2618F, -0.6632F));
        PartDefinition p_antler_r = p_head.addOrReplaceChild("antler_r", CubeListBuilder.create()
                .texOffs(5, 38).addBox(-0.5F, -7F, -0.5F, 1F, 7F, 1F),
                PartPose.offsetAndRotation(-1.3F, -3F, -2.5F, -0.2618F, 0F, -0.384F));
        PartDefinition p_antler_r_tine1 = p_antler_r.addOrReplaceChild("antler_r_tine1", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -2.5F, 0F, -0.9599F, 0F, 0F));
        PartDefinition p_antler_r_tine2 = p_antler_r.addOrReplaceChild("antler_r_tine2", CubeListBuilder.create()
                .texOffs(5, 47).addBox(-0.5F, -2.5F, -0.5F, 1F, 2.5F, 1F),
                PartPose.offsetAndRotation(0F, -5F, 0F, -0.7854F, 0F, 0F));
        PartDefinition p_antler_r_crown = p_antler_r.addOrReplaceChild("antler_r_crown", CubeListBuilder.create()
                .texOffs(10, 47).addBox(-0.5F, -2.5F, -0.5F, 1F, 2.5F, 1F),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 0.4363F, 0F, 0.2618F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(39, 38).addBox(-1F, 0F, -0.5F, 2F, 3F, 1F),
                PartPose.offsetAndRotation(0F, -3F, 7F, 0.5236F, 0F, 0F));
        PartDefinition p_front_left_leg = root.addOrReplaceChild("front_left_leg", CubeListBuilder.create()
                .texOffs(45, 0).addBox(-1F, 0F, -1F, 2F, 11F, 2F),
                PartPose.offset(2F, 13F, -6F));
        PartDefinition p_front_right_leg = root.addOrReplaceChild("front_right_leg", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1F, 0F, -1F, 2F, 11F, 2F),
                PartPose.offset(-2F, 13F, -6F));
        PartDefinition p_hind_left_leg = root.addOrReplaceChild("hind_left_leg", CubeListBuilder.create()
                .texOffs(0, 24).addBox(-1F, 0F, -1F, 2F, 11F, 2F),
                PartPose.offset(2F, 13F, 5.5F));
        PartDefinition p_hind_right_leg = root.addOrReplaceChild("hind_right_leg", CubeListBuilder.create()
                .texOffs(9, 24).addBox(-1F, 0F, -1F, 2F, 11F, 2F),
                PartPose.offset(-2F, 13F, 5.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
