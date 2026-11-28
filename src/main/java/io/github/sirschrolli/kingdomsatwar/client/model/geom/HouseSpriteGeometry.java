package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Wichtel (Knochen wie beim Vanilla-Hilfsgeist; Zipfelmütze oder Fliegenpilz je Gestalt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class HouseSpriteGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private HouseSpriteGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_root = root.addOrReplaceChild("root", CubeListBuilder.create(),
                PartPose.offset(0F, 23.5F, 0F));
        PartDefinition p_head = p_root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(33, 16).addBox(-2.5F, -5F, -2.5F, 5F, 5F, 5F)
                .texOffs(14, 50).addBox(-0.5F, -2.5F, -3.15F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(39, 44).addBox(-3.25F, -3.7F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.15F, -0.3F, -0.4F))
                .texOffs(46, 44).addBox(2.25F, -3.7F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.15F, -0.3F, -0.4F))
                .texOffs(0, 27).addBox(-3F, -6.05F, -3F, 6F, 2F, 6F, new CubeDeformation(0F, -0.35F, 0F)),
                PartPose.offset(0F, -3.99F, 0F));
        PartDefinition p_cap_cone = p_head.addOrReplaceChild("cap_cone", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-2.5F, -7.4F, -2.3F, 5F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(17, 44).addBox(-1.5F, -8.85F, -1F, 3F, 2F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cap_tip = p_cap_cone.addOrReplaceChild("cap_tip", CubeListBuilder.create()
                .texOffs(30, 44).addBox(-1F, -2.55F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.35F, -0.25F))
                .texOffs(53, 44).addBox(-1F, -3.1F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, -8.4F, 0.5F, -0.8378F, 0F, 0F));
        PartDefinition p_toadstool = p_head.addOrReplaceChild("toadstool", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-4F, -7.2F, -4F, 8F, 2F, 8F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(25, 27).addBox(-3F, -8.4F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(0, 44).addBox(-2F, -8.8F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = p_root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(36, 36).addBox(-1.5F, 0F, -1F, 3F, 4F, 2F)
                .texOffs(21, 36).addBox(-2F, 2.55F, -1.5F, 4F, 3F, 3F, new CubeDeformation(-0.1F, -0.05F, -0.1F)),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_right_arm = p_body.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(47, 36).addBox(-0.75F, -0.9F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.01F, -0.41F, -0.01F))
                .texOffs(0, 50).addBox(-0.75F, 2.65F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.01F, -0.06F, -0.11F)),
                PartPose.offset(-1.75F, 0.5F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(0, 0).addBox(0F, 0.6F, 0F, 0F, 6F, 9F),
                PartPose.offset(-0.5F, 0F, 0.6F));
        PartDefinition p_left_arm = p_body.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(54, 36).addBox(-0.25F, -0.9F, -1F, 1F, 4F, 2F, new CubeDeformation(-0.01F, -0.41F, -0.01F))
                .texOffs(7, 50).addBox(-0.25F, 2.65F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.01F, -0.06F, -0.11F)),
                PartPose.offset(1.75F, 0.5F, 0F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(19, 0).addBox(0F, 0.6F, 0F, 0F, 6F, 9F),
                PartPose.offset(0.5F, 0F, 0.6F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
