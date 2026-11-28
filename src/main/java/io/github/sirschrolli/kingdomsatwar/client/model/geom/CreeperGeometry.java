package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Kriecher (Hauptquader wie beim Vanilla-Creeper, Blätter und Blüten als Hülle).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CreeperGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private CreeperGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(66, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_head_shell = p_head.addOrReplaceChild("head_shell", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -8.5F, -4.5F, 9F, 9F, 9F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_flower = p_head.addOrReplaceChild("flower", CubeListBuilder.create()
                .texOffs(113, 19).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F)
                .texOffs(73, 19).addBox(-1.5F, -3F, -1.5F, 3F, 1F, 3F),
                PartPose.offsetAndRotation(1.5F, -8.6F, 0.5F, -0.1047F, 0F, 0.1745F));
        PartDefinition p_icicles = p_head.addOrReplaceChild("icicles", CubeListBuilder.create()
                .texOffs(99, 19).addBox(-3.5F, 0F, -3.8F, 1F, 3F, 1F)
                .texOffs(118, 19).addBox(-0.6F, 0F, -3.6F, 1F, 2F, 1F)
                .texOffs(68, 19).addBox(2.2F, 0F, -3.9F, 1F, 4F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mushrooms = p_head.addOrReplaceChild("mushrooms", CubeListBuilder.create()
                .texOffs(123, 19).addBox(-0.5F, -2F, -0.5F, 1F, 2F, 1F)
                .texOffs(86, 19).addBox(-1.5F, -3F, -1.5F, 3F, 1F, 3F)
                .texOffs(0, 30).addBox(2.5F, -1F, -2.5F, 1F, 1F, 1F)
                .texOffs(104, 19).addBox(2F, -2F, -3F, 2F, 1F, 2F),
                PartPose.offset(-1.5F, -8F, 1F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(99, 0).addBox(-4F, 0F, -2F, 8F, 12F, 4F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_body_shell = p_body.addOrReplaceChild("body_shell", CubeListBuilder.create()
                .texOffs(37, 0).addBox(-4.5F, 0F, -2.5F, 9F, 12F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-2F, 0F, -2F, 4F, 6F, 4F),
                PartPose.offset(-2F, 18F, 4F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(17, 19).addBox(-2F, 0F, -2F, 4F, 6F, 4F),
                PartPose.offset(2F, 18F, 4F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(34, 19).addBox(-2F, 0F, -2F, 4F, 6F, 4F),
                PartPose.offset(-2F, 18F, -4F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(51, 19).addBox(-2F, 0F, -2F, 4F, 6F, 4F),
                PartPose.offset(2F, 18F, -4F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
