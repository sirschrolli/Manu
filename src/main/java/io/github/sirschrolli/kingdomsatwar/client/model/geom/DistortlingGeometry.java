package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Zerrlings (Leib aus Bruchstücken mit Lücken dazwischen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class DistortlingGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private DistortlingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-1.5F, -0.4F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offset(-2.1F, 6.5F, 0F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(0, 25).addBox(-1.5F, 7.55F, -3.3F, 3F, 2F, 5F, new CubeDeformation(0F, -0.25F, -0.4F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(67, 0).addBox(-1.5F, -0.4F, -1.5F, 3F, 8F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offset(2.1F, 6.5F, 0F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(93, 0).addBox(-1.5F, 0F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(17, 25).addBox(-1.5F, 7.55F, -3.3F, 3F, 2F, 5F, new CubeDeformation(0F, -0.25F, -0.4F)),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(74, 14).addBox(-3F, -2.4F, -2F, 6F, 3F, 4F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(0F, 6.5F, 0F));
        PartDefinition p_belly = p_body.addOrReplaceChild("belly", CubeListBuilder.create()
                .texOffs(34, 25).addBox(-2.5F, -3.6F, -1.5F, 5F, 4F, 3F, new CubeDeformation(-0.2F, -0.4F, 0F)),
                PartPose.offset(0F, -3F, 0F));
        PartDefinition p_chest = p_belly.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(25, 0).addBox(-4.5F, -6.9F, -2.5F, 9F, 7F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.3F))
                .texOffs(51, 25).addBox(-5F, -7.7F, -2.5F, 3F, 2F, 5F, new CubeDeformation(-0.2F, -0.3F, 0F))
                .texOffs(68, 25).addBox(2F, -7.7F, -2.5F, 3F, 2F, 5F, new CubeDeformation(-0.2F, -0.3F, 0F)),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_chest_slice = p_chest.addOrReplaceChild("chest_slice", CubeListBuilder.create()
                .texOffs(45, 14).addBox(-4.5F, -1F, -2.5F, 9F, 2F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.2F)),
                PartPose.offset(1.3F, -3.6F, 0F));
        PartDefinition p_chest_slice2 = p_chest.addOrReplaceChild("chest_slice2", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-4.5F, -0.5F, -2.5F, 9F, 1F, 5F, new CubeDeformation(-0.3F, -0.1F, -0.25F)),
                PartPose.offset(-0.9F, -5.7F, 0F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -7F, -3F, 6F, 7F, 6F)
                .texOffs(85, 25).addBox(-3.5F, -6.8F, -3.6F, 7F, 6F, 1F, new CubeDeformation(-0.3F, -0.4F, -0.25F)),
                PartPose.offset(0F, -7.8F, -0.3F));
        PartDefinition p_head_slice = p_head.addOrReplaceChild("head_slice", CubeListBuilder.create()
                .texOffs(18, 14).addBox(-3F, -0.5F, -3.5F, 6F, 1F, 7F, new CubeDeformation(0F, 0F, -0.45F)),
                PartPose.offset(-1.1F, -2.2F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(95, 14).addBox(-2.5F, -0.2F, -2.7F, 5F, 2F, 5F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(0F, 0.6F, -0.4F));
        PartDefinition p_head_shards = p_head.addOrReplaceChild("head_shards", CubeListBuilder.create()
                .texOffs(85, 33).addBox(2.5F, -2.9F, 1.3F, 2F, 2F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(0, 40).addBox(-3.4F, -0.9F, 1F, 1F, 2F, 1F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(5, 40).addBox(1.1F, -4.3F, 2.3F, 2F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offset(0F, -4F, 2F));
        PartDefinition p_right_arm = p_chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(106, 0).addBox(-1F, -1F, -1F, 2F, 8F, 2F),
                PartPose.offset(-5.4F, -6F, 0F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(115, 0).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 7.8F, 0F));
        PartDefinition p_right_hand = p_right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(29, 33).addBox(-1.5F, -0.4F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(55, 33).addBox(-1.275F, 1.8F, -1.275F, 1F, 5F, 1F, new CubeDeformation(-0.275F, -0.2F, -0.275F))
                .texOffs(65, 33).addBox(-0.075F, 2F, -1.375F, 1F, 4F, 1F, new CubeDeformation(-0.275F, 0F, -0.275F))
                .texOffs(70, 33).addBox(-0.675F, 1.7F, 0.125F, 1F, 4F, 1F, new CubeDeformation(-0.275F, -0.3F, -0.275F)),
                PartPose.offset(0F, 8.8F, 0F));
        PartDefinition p_left_arm = p_chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1F, -1F, -1F, 2F, 8F, 2F),
                PartPose.offset(5.4F, -6F, 0F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(9, 14).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 7.8F, 0F));
        PartDefinition p_left_hand = p_left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(42, 33).addBox(-1.5F, -0.4F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(60, 33).addBox(-1.275F, 1.8F, -1.275F, 1F, 5F, 1F, new CubeDeformation(-0.275F, -0.2F, -0.275F))
                .texOffs(75, 33).addBox(-0.075F, 2F, -1.375F, 1F, 4F, 1F, new CubeDeformation(-0.275F, 0F, -0.275F))
                .texOffs(80, 33).addBox(-0.675F, 1.7F, 0.125F, 1F, 4F, 1F, new CubeDeformation(-0.275F, -0.3F, -0.275F)),
                PartPose.offset(0F, 8.8F, 0F));
        PartDefinition p_glitch = p_body.addOrReplaceChild("glitch", CubeListBuilder.create(),
                PartPose.offset(0F, -6F, 0F));
        PartDefinition p_bit_0 = p_glitch.addOrReplaceChild("bit_0", CubeListBuilder.create()
                .texOffs(94, 33).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(5.6F, -4F, -1F));
        PartDefinition p_bit_1 = p_glitch.addOrReplaceChild("bit_1", CubeListBuilder.create()
                .texOffs(112, 33).addBox(-0.5F, -1.5F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.1F)),
                PartPose.offset(-6.2F, 0.5F, 1F));
        PartDefinition p_bit_2 = p_glitch.addOrReplaceChild("bit_2", CubeListBuilder.create()
                .texOffs(12, 40).addBox(-1F, -0.5F, -0.5F, 2F, 1F, 1F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(4.4F, 3.5F, 2.6F));
        PartDefinition p_bit_3 = p_glitch.addOrReplaceChild("bit_3", CubeListBuilder.create()
                .texOffs(19, 40).addBox(-0.5F, -0.5F, -0.5F, 1F, 1F, 1F),
                PartPose.offset(-4.6F, -6.5F, -2.4F));
        PartDefinition p_bit_4 = p_glitch.addOrReplaceChild("bit_4", CubeListBuilder.create()
                .texOffs(117, 33).addBox(-1F, -0.5F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0.5F, -9.5F, 3.4F));
        PartDefinition p_bit_5 = p_glitch.addOrReplaceChild("bit_5", CubeListBuilder.create()
                .texOffs(103, 33).addBox(-0.5F, -0.5F, -1.5F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.15F, -0.4F)),
                PartPose.offset(-2F, 4.6F, -3.2F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
