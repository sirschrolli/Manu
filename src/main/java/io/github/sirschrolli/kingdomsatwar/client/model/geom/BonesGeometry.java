package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Gerippe, Frostgebein, Moorgebein und Dörrgebein (Skelett-Knochen wie bei Vanilla, Pilze am Kopf).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class BonesGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private BonesGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(62, 0).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.5F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 37).addBox(-3F, 0F, -2F, 6F, 2F, 2F),
                PartPose.offsetAndRotation(0F, -0.2F, -2.2F, 0.1745F, 0F, 0F));
        PartDefinition p_mushrooms = p_head.addOrReplaceChild("mushrooms", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_red_mushroom_1 = p_mushrooms.addOrReplaceChild("red_mushroom_1", CubeListBuilder.create()
                .texOffs(17, 37).addBox(-3F, -4F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(2F, -8F, 1.5F, 0F, 0.7854F, 0F));
        PartDefinition p_red_mushroom_2 = p_mushrooms.addOrReplaceChild("red_mushroom_2", CubeListBuilder.create()
                .texOffs(30, 37).addBox(-3F, -4F, 0F, 6F, 4F, 0F),
                PartPose.offsetAndRotation(2F, -8F, 1.5F, 0F, 2.3562F, 0F));
        PartDefinition p_brown_mushroom_1 = p_mushrooms.addOrReplaceChild("brown_mushroom_1", CubeListBuilder.create()
                .texOffs(59, 37).addBox(-2.5F, -3F, 0F, 5F, 3F, 0F),
                PartPose.offsetAndRotation(-2.5F, -8F, -1.5F, 0F, 0.7854F, 0F));
        PartDefinition p_brown_mushroom_2 = p_mushrooms.addOrReplaceChild("brown_mushroom_2", CubeListBuilder.create()
                .texOffs(70, 37).addBox(-2.5F, -3F, 0F, 5F, 3F, 0F),
                PartPose.offsetAndRotation(-2.5F, -8F, -1.5F, 0F, 2.3562F, 0F));
        PartDefinition p_shelf_fungus = p_mushrooms.addOrReplaceChild("shelf_fungus", CubeListBuilder.create()
                .texOffs(43, 37).addBox(0F, -1F, -1.5F, 2F, 1F, 3F)
                .texOffs(81, 37).addBox(-0.3F, 0.6F, -0.5F, 2F, 1F, 2F, new CubeDeformation(-0.3F, 0F, 0F)),
                PartPose.offset(4F, -3F, 1F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(18, 22).addBox(-4F, 0F, -2F, 8F, 8F, 4F)
                .texOffs(9, 22).addBox(-1F, -0.25F, 0.05F, 2F, 11F, 2F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(87, 22).addBox(-3.5F, 10F, -1.5F, 7F, 2F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_garb = p_body.addOrReplaceChild("garb", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.5F, -0.5F, -2.5F, 9F, 16F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(54, 22).addBox(-5F, -1F, -3F, 10F, 3F, 6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_quiver = p_body.addOrReplaceChild("quiver", CubeListBuilder.create()
                .texOffs(43, 22).addBox(-1.5F, 0F, 0F, 3F, 10F, 2F),
                PartPose.offsetAndRotation(-3F, -0.5F, 2.7F, 0F, 0F, -0.4538F));
        PartDefinition p_arrows = p_quiver.addOrReplaceChild("arrows", CubeListBuilder.create()
                .texOffs(54, 37).addBox(-1.1F, -2.5F, 0.5F, 1F, 3F, 1F)
                .texOffs(108, 22).addBox(0.1F, -3.2F, 0.6F, 1F, 4F, 1F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-1F, -2F, -1F, 2F, 12F, 2F)
                .texOffs(90, 37).addBox(-1F, 3.5F, -1F, 2F, 1F, 2F, new CubeDeformation(0.25F)),
                PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-1F, -2F, -1F, 2F, 12F, 2F)
                .texOffs(99, 37).addBox(-1F, 3.5F, -1F, 2F, 1F, 2F, new CubeDeformation(0.25F)),
                PartPose.offset(5F, 2F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(113, 0).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(108, 37).addBox(-1F, 5F, -1F, 2F, 1F, 2F, new CubeDeformation(0.3F))
                .texOffs(117, 37).addBox(-1F, 11F, -2.8F, 2F, 1F, 2F),
                PartPose.offset(-2F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-1F, 0F, -1F, 2F, 12F, 2F)
                .texOffs(0, 42).addBox(-1F, 5F, -1F, 2F, 1F, 2F, new CubeDeformation(0.3F))
                .texOffs(9, 42).addBox(-1F, 11F, -2.8F, 2F, 1F, 2F),
                PartPose.offset(2F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
