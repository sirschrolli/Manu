package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Schreinlings (Deckel, Sockel und Kopf wie beim Vanilla-Shulker, dazu Medaillon, Beschläge, Zähne, Fuß und Fühler).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SchreinlingGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private SchreinlingGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_lid = root.addOrReplaceChild("lid", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -16F, -8F, 16F, 12F, 16F)
                .texOffs(94, 54).addBox(-3F, -17F, -3F, 6F, 1F, 6F)
                .texOffs(119, 54).addBox(-8.5F, -16.75F, -8.5F, 2F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 74).addBox(-8.5F, -16.75F, 6.5F, 2F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(9, 74).addBox(6.5F, -16.75F, -8.5F, 2F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(18, 74).addBox(6.5F, -16.75F, 6.5F, 2F, 3F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(37, 74).addBox(-6F, -4.2F, -7.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(42, 74).addBox(-6F, -4.2F, 6.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(47, 74).addBox(-7.85F, -4.2F, -6F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(52, 74).addBox(6.85F, -4.2F, -6F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(57, 74).addBox(-2.4F, -4.2F, -7.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(62, 74).addBox(-2.4F, -4.2F, 6.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(67, 74).addBox(-7.85F, -4.2F, -2.4F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(72, 74).addBox(6.85F, -4.2F, -2.4F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(77, 74).addBox(1.2F, -4.2F, -7.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(82, 74).addBox(1.2F, -4.2F, 6.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(87, 74).addBox(-7.85F, -4.2F, 1.2F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(92, 74).addBox(6.85F, -4.2F, 1.2F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(97, 74).addBox(4.8F, -4.2F, -7.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(102, 74).addBox(4.8F, -4.2F, 6.85F, 1F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(107, 74).addBox(-7.85F, -4.2F, 4.8F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(112, 74).addBox(6.85F, -4.2F, 4.8F, 1F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_base = root.addOrReplaceChild("base", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-8F, -8F, -8F, 16F, 8F, 16F)
                .texOffs(0, 54).addBox(-8.5F, -1.75F, -8.5F, 17F, 2F, 17F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(69, 54).addBox(-3F, 0F, -3F, 6F, 6F, 6F)
                .texOffs(27, 74).addBox(-2.35F, -2.6F, -0.65F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F))
                .texOffs(32, 74).addBox(1.35F, -2.6F, -0.65F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.2F, -0.15F)),
                PartPose.offset(0F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
