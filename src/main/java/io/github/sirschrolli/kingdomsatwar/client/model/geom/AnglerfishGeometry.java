package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Anglerfischs (Unterkiefer mit Nadelzähnen, Angel in zwei Gliedern mit leuchtendem Köder).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class AnglerfishGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 96;

    private AnglerfishGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -5F, -4F, 10F, 9F, 10F)
                .texOffs(41, 0).addBox(-6F, -4F, -3F, 12F, 7F, 8F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(62, 20).addBox(-4F, -6F, -3F, 8F, 1F, 8F)
                .texOffs(43, 33).addBox(-3F, -6.8F, -1.5F, 6F, 1F, 5F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(95, 20).addBox(-4F, 3.9F, -2F, 8F, 1F, 7F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 33).addBox(-5.5F, -4.25F, -5F, 11F, 6F, 2F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 42).addBox(-5F, 1.1F, -4.6F, 10F, 3F, 1F, new CubeDeformation(-0.4F, -0.3F, -0.2F))
                .texOffs(23, 42).addBox(-4.9F, 1.2F, -5.1F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(62, 42).addBox(-3.1F, 1.3F, -5.1F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(28, 42).addBox(-1.3F, 1.2F, -5.1F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(67, 42).addBox(0.5F, 1.3F, -5.1F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(33, 42).addBox(2.3F, 1.2F, -5.1F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(72, 42).addBox(3.9F, 1.3F, -5.1F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(112, 42).addBox(5.1F, -3.4F, -4.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(117, 42).addBox(-6.1F, -3.4F, -4.4F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(0F, 17F, 0F));
        PartDefinition p_left_pectoral = p_body.addOrReplaceChild("left_pectoral", CubeListBuilder.create()
                .texOffs(84, 33).addBox(0F, -0.5F, -1.25F, 4F, 1F, 4F, new CubeDeformation(0F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(5.6F, 1.5F, 1F, 0F, -0.3491F, 0.4363F));
        PartDefinition p_right_pectoral = p_body.addOrReplaceChild("right_pectoral", CubeListBuilder.create()
                .texOffs(101, 33).addBox(-4F, -0.5F, -1.25F, 4F, 1F, 4F, new CubeDeformation(0F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-5.6F, 1.5F, 1F, 0F, 0.3491F, -0.4363F));
        PartDefinition p_jaw = p_body.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 20).addBox(-5.5F, -0.2F, -8.75F, 11F, 3F, 9F, new CubeDeformation(0F, -0.2F, -0.25F))
                .texOffs(38, 42).addBox(-5F, -2.9F, -8.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(77, 42).addBox(-3.2F, -2F, -8.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(43, 42).addBox(-1.4F, -2.9F, -8.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(82, 42).addBox(0.4F, -2F, -8.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(48, 42).addBox(2.2F, -2.9F, -8.5F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(87, 42).addBox(4F, -2F, -8.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(92, 42).addBox(-5.4F, -1.8F, -5.7F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(97, 42).addBox(4.4F, -1.8F, -5.7F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(0F, 3.5F, 2.5F));
        PartDefinition p_rod = p_body.addOrReplaceChild("rod", CubeListBuilder.create()
                .texOffs(27, 33).addBox(-0.5F, -6.75F, -0.5F, 1F, 7F, 1F, new CubeDeformation(-0.15F, -0.25F, -0.15F)),
                PartPose.offsetAndRotation(0F, -6F, -2F, -0.6109F, 0F, 0F));
        PartDefinition p_rod_tip = p_rod.addOrReplaceChild("rod_tip", CubeListBuilder.create()
                .texOffs(79, 33).addBox(-0.5F, -5F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, 1.309F, 0F, 0F));
        PartDefinition p_lure = p_rod_tip.addOrReplaceChild("lure", CubeListBuilder.create()
                .texOffs(66, 33).addBox(-1.5F, -1.5F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(53, 42).addBox(-1F, -1.8F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(122, 42).addBox(-0.5F, 1.05F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.05F, -0.1F))
                .texOffs(102, 42).addBox(-0.95F, 1.7F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.35F))
                .texOffs(107, 42).addBox(-0.05F, 1.5F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.35F, -0.4F, -0.35F)),
                PartPose.offset(0F, -5F, 0F));
        PartDefinition p_dorsal = p_body.addOrReplaceChild("dorsal", CubeListBuilder.create()
                .texOffs(32, 33).addBox(-0.5F, -2.75F, -1.25F, 1F, 3F, 4F, new CubeDeformation(-0.2F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.5F, 3.5F, -0.3491F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(41, 20).addBox(-3F, -3F, -0.25F, 6F, 6F, 4F, new CubeDeformation(-0.2F, 0F, -0.25F)),
                PartPose.offset(0F, -0.5F, 6F));
        PartDefinition p_tail_fin = p_tail.addOrReplaceChild("tail_fin", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-0.5F, -4.5F, 0F, 1F, 9F, 4F, new CubeDeformation(-0.1F, 0F, 0F)),
                PartPose.offset(0F, 0F, 3.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
