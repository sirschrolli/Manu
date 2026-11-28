package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Phönix (Geschoss des Feuerzaubers, fliegt nach -Z, Rumpfmitte im Ursprung; Flügel schlagen über zRot von left_wing/right_wing und den Handschwingen, Schweif und Kopf bewegen sich).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PhoenixGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 96;

    private PhoenixGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 25).addBox(-3F, -2.45F, -5F, 6F, 5F, 10F, new CubeDeformation(0F, -0.35F, 0F))
                .texOffs(47, 53).addBox(-2.5F, -2.85F, -4.6F, 5F, 1F, 9F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(76, 53).addBox(-2.5F, 1.9F, -4.5F, 5F, 1F, 8F, new CubeDeformation(-0.2F, -0.1F, -0.3F))
                .texOffs(103, 53).addBox(-2.5F, -1.65F, -6.8F, 5F, 5F, 3F, new CubeDeformation(0F, -0.35F, -0.2F))
                .texOffs(59, 64).addBox(-2.5F, -2.2F, 4.3F, 5F, 4F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(76, 64).addBox(-2F, -2.7F, -1.9F, 4F, 4F, 3F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(0, 64).addBox(-2F, -3.5F, -4.4F, 4F, 4F, 4F)
                .texOffs(61, 73).addBox(-1F, -2.6F, -6.8F, 2F, 2F, 3F, new CubeDeformation(-0.15F, -0.3F, -0.4F))
                .texOffs(62, 79).addBox(-1F, -1.65F, -7.05F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.35F, -0.05F))
                .texOffs(53, 79).addBox(-1F, -1.125F, -6.05F, 2F, 1F, 2F, new CubeDeformation(-0.35F, -0.175F, -0.25F))
                .texOffs(33, 0).addBox(0F, -12.4F, -3.2F, 0F, 9F, 12F),
                PartPose.offset(0F, -1F, -5F));
        PartDefinition p_left_wing = p_body.addOrReplaceChild("left_wing", CubeListBuilder.create()
                .texOffs(17, 64).addBox(-0.8F, -1.6F, -2.5F, 6F, 3F, 4F, new CubeDeformation(0F, -0.45F, -0.2F))
                .texOffs(106, 64).addBox(4.7F, -1.05F, -2F, 8F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(72, 73).addBox(0F, -0.5F, 0F, 12F, 1F, 3F, new CubeDeformation(0F, -0.05F, -0.2F))
                .texOffs(33, 25).addBox(-0.8F, 0F, -1F, 13F, 0F, 11F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(3F, -1F, -1F));
        PartDefinition p_left_wing_tip = p_left_wing.addOrReplaceChild("left_wing_tip", CubeListBuilder.create()
                .texOffs(23, 73).addBox(-1F, -1.05F, -1.9F, 6F, 2F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(31, 79).addBox(4.1F, -0.5F, -1.4F, 3F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.15F))
                .texOffs(49, 41).addBox(-0.4F, 0F, -1.8F, 13F, 0F, 10F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(12F, 0F, 0F));
        PartDefinition p_right_wing = p_body.addOrReplaceChild("right_wing", CubeListBuilder.create()
                .texOffs(38, 64).addBox(-5.2F, -1.6F, -2.5F, 6F, 3F, 4F, new CubeDeformation(0F, -0.45F, -0.2F))
                .texOffs(0, 73).addBox(-12.7F, -1.05F, -2F, 8F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(0, 79).addBox(-12F, -0.5F, 0F, 12F, 1F, 3F, new CubeDeformation(0F, -0.05F, -0.2F))
                .texOffs(0, 41).addBox(-12.2F, 0F, -1F, 13F, 0F, 11F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offset(-3F, -1F, -1F));
        PartDefinition p_right_wing_tip = p_right_wing.addOrReplaceChild("right_wing_tip", CubeListBuilder.create()
                .texOffs(42, 73).addBox(-5F, -1.05F, -1.9F, 6F, 2F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(42, 79).addBox(-7.1F, -0.5F, -1.4F, 3F, 1F, 2F, new CubeDeformation(-0.1F, 0F, -0.15F))
                .texOffs(0, 53).addBox(-12.6F, 0F, -1.8F, 13F, 0F, 10F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(-12F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(91, 64).addBox(-2F, -1.75F, -0.4F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.45F, 0F))
                .texOffs(58, 0).addBox(-9F, 0F, 0F, 18F, 0F, 16F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(0, 0).addBox(0F, -4.9F, 0.2F, 0F, 8F, 16F, new CubeDeformation(0F, -0.3F, -0.2F)),
                PartPose.offset(0F, 0F, 5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
