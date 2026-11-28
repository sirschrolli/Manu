package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Getragene Krone des Zerbrochenen Königs (Teile wie HumanoidModel, nur der Kopf trägt Reif und schwebende Kristallsplitter); Textur als Rüstungsschicht.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ShatteredCrownGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 32;

    private ShatteredCrownGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5F, -9F, -4.9F, 10F, 2F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(23, 20).addBox(-5F, -9F, 3.9F, 10F, 2F, 1F, new CubeDeformation(-0.4F, -0.1F, -0.2F))
                .texOffs(0, 0).addBox(3.9F, -9F, -4.5F, 1F, 2F, 9F, new CubeDeformation(-0.2F, -0.1F, -0.4F))
                .texOffs(21, 0).addBox(-4.9F, -9F, -4.5F, 1F, 2F, 9F, new CubeDeformation(-0.2F, -0.1F, -0.4F))
                .texOffs(45, 12).addBox(-1F, -9.8F, -5.3F, 2F, 3F, 1F, new CubeDeformation(0F, -0.2F, -0.2F))
                .texOffs(46, 20).addBox(-4.5F, -10.3F, -4.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(51, 20).addBox(-4.5F, -10.3F, 3.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(56, 20).addBox(3.5F, -10.3F, -4.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(0, 24).addBox(3.5F, -10.3F, 3.5F, 1F, 2F, 1F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_crown = p_head.addOrReplaceChild("crown", CubeListBuilder.create(),
                PartPose.offset(0F, -10.6F, 0F));
        PartDefinition p_crown_0 = p_crown.addOrReplaceChild("crown_0", CubeListBuilder.create()
                .texOffs(42, 0).addBox(-1F, -5.8F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, -4F, 0.2793F, 0F, 0F));
        PartDefinition p_crown_1 = p_crown.addOrReplaceChild("crown_1", CubeListBuilder.create()
                .texOffs(51, 0).addBox(-1F, -4.2F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(3.1273F, 0F, -2.494F, 0.1741F, 0F, 0.2183F));
        PartDefinition p_crown_2 = p_crown.addOrReplaceChild("crown_2", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-1F, -3.75F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.4F)),
                PartPose.offsetAndRotation(3.8997F, 0F, 0.8901F, -0.0621F, 0F, 0.2723F));
        PartDefinition p_crown_3 = p_crown.addOrReplaceChild("crown_3", CubeListBuilder.create()
                .texOffs(9, 12).addBox(-1F, -3.75F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.4F)),
                PartPose.offsetAndRotation(1.7355F, 0F, 3.6039F, -0.2516F, 0F, 0.1212F));
        PartDefinition p_crown_4 = p_crown.addOrReplaceChild("crown_4", CubeListBuilder.create()
                .texOffs(18, 12).addBox(-1F, -3.75F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.4F)),
                PartPose.offsetAndRotation(-1.7355F, 0F, 3.6039F, -0.2516F, 0F, -0.1212F));
        PartDefinition p_crown_5 = p_crown.addOrReplaceChild("crown_5", CubeListBuilder.create()
                .texOffs(27, 12).addBox(-1F, -3.75F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, -0.45F, -0.4F)),
                PartPose.offsetAndRotation(-3.8997F, 0F, 0.8901F, -0.0621F, 0F, -0.2723F));
        PartDefinition p_crown_6 = p_crown.addOrReplaceChild("crown_6", CubeListBuilder.create()
                .texOffs(36, 12).addBox(-1F, -4.2F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-3.1273F, 0F, -2.494F, 0.1741F, 0F, -0.2183F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create(),
                PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create(),
                PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create(),
                PartPose.offset(-1.9F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create(),
                PartPose.offset(1.9F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
