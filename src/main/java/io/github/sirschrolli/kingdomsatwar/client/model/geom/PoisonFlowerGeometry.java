package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Giftblume (Stiel, Samenkapsel "bulb", sechs Blütenblätter petal0–petal5 mit Drehung i·60° um Y, zeigen nach lokal -Z; öffnen/schließen über xRot; vier Blätter leaf0–leaf3 am Fuß).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PoisonFlowerGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 80;

    private PoisonFlowerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_stem = root.addOrReplaceChild("stem", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -9.4F, -2.5F, 5F, 11F, 5F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(50, 0).addBox(-1.8F, -17.3F, -2.05F, 4F, 9F, 4F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(67, 0).addBox(-1.2F, -24.5F, -2.4F, 4F, 8F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.15F))
                .texOffs(29, 17).addBox(-3F, -1.6F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(102, 50).addBox(-1.7F, -24.5F, -3.7F, 5F, 3F, 2F, new CubeDeformation(-0.2F, -0.3F, -0.4F))
                .texOffs(0, 56).addBox(-1.7F, -24.5F, 1.7F, 5F, 3F, 2F, new CubeDeformation(-0.2F, -0.3F, -0.4F))
                .texOffs(79, 17).addBox(-3.1F, -24.5F, -1.9F, 2F, 3F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.3F))
                .texOffs(94, 17).addBox(2.3F, -24.5F, -1.9F, 2F, 3F, 5F, new CubeDeformation(-0.4F, -0.3F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_leaf0 = p_stem.addOrReplaceChild("leaf0", CubeListBuilder.create()
                .texOffs(100, 27).addBox(-2.5F, -1F, -5.5F, 5F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(0, 27).addBox(-3F, -0.4F, -10.9F, 6F, 1F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1.131F, -2.4F, -1.131F, 0F, 0.7854F, 0F));
        PartDefinition p_leaf1 = p_stem.addOrReplaceChild("leaf1", CubeListBuilder.create()
                .texOffs(0, 35).addBox(-2.5F, -1F, -5.5F, 5F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(25, 27).addBox(-3F, -0.4F, -10.9F, 6F, 1F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(-1.131F, -2.9F, 1.131F, 0F, 2.3562F, 0F));
        PartDefinition p_leaf2 = p_stem.addOrReplaceChild("leaf2", CubeListBuilder.create()
                .texOffs(23, 35).addBox(-2.5F, -1F, -5.5F, 5F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(50, 27).addBox(-3F, -0.4F, -10.9F, 6F, 1F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1.131F, -2.4F, 1.131F, 0F, 3.927F, 0F));
        PartDefinition p_leaf3 = p_stem.addOrReplaceChild("leaf3", CubeListBuilder.create()
                .texOffs(46, 35).addBox(-2.5F, -1F, -5.5F, 5F, 1F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.1F))
                .texOffs(75, 27).addBox(-3F, -0.4F, -10.9F, 6F, 1F, 6F, new CubeDeformation(-0.1F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1.131F, -2.9F, -1.131F, 0F, 5.4978F, 0F));
        PartDefinition p_bulb = p_stem.addOrReplaceChild("bulb", CubeListBuilder.create()
                .texOffs(54, 17).addBox(-3F, -1.3F, -3F, 6F, 2F, 6F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(84, 0).addBox(-4F, -5F, -3F, 8F, 4F, 6F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(21, 0).addBox(-3F, -5.55F, -4F, 6F, 5F, 8F, new CubeDeformation(0F, -0.45F, 0F))
                .texOffs(0, 17).addBox(-3.5F, -6.4F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.4F, -0.4F, -0.4F))
                .texOffs(100, 43).addBox(-2F, -6.75F, -2F, 4F, 1F, 4F, new CubeDeformation(-0.4F, -0.05F, -0.4F))
                .texOffs(117, 56).addBox(1.4053F, -8.7F, 0.6F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(20, 62).addBox(1.4053F, -9.45F, 0.6F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(122, 56).addBox(-0.5F, -8.7F, 1.7F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(25, 62).addBox(-0.5F, -9.45F, 1.7F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(0, 62).addBox(-2.4053F, -8.7F, 0.6F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(30, 62).addBox(-2.4053F, -9.45F, 0.6F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(5, 62).addBox(-2.4053F, -8.7F, -1.6F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(35, 62).addBox(-2.4053F, -9.45F, -1.6F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(10, 62).addBox(-0.5F, -8.7F, -2.7F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(40, 62).addBox(-0.5F, -9.45F, -2.7F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(15, 62).addBox(1.4053F, -8.7F, -1.6F, 1F, 3F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.25F))
                .texOffs(45, 62).addBox(1.4053F, -9.45F, -1.6F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F)),
                PartPose.offset(0.8F, -24F, -0.4F));
        PartDefinition p_petal0 = p_bulb.addOrReplaceChild("petal0", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-2F, -0.5F, -3.4F, 4F, 1F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(69, 35).addBox(-3.5F, -0.5F, -8.05F, 7F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.05F))
                .texOffs(15, 56).addBox(-2.5F, -1.1F, -10.55F, 5F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.15F)),
                PartPose.offset(0F, -4.4F, -3F));
        PartDefinition p_petal1 = p_bulb.addOrReplaceChild("petal1", CubeListBuilder.create()
                .texOffs(17, 50).addBox(-2F, -0.5F, -3.4F, 4F, 1F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(94, 35).addBox(-3.5F, -0.5F, -8.05F, 7F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.05F))
                .texOffs(32, 56).addBox(-2.5F, -1.1F, -10.55F, 5F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.15F)),
                PartPose.offsetAndRotation(-2.598F, -4.4F, -1.5F, 0F, 1.0472F, 0F));
        PartDefinition p_petal2 = p_bulb.addOrReplaceChild("petal2", CubeListBuilder.create()
                .texOffs(34, 50).addBox(-2F, -0.5F, -3.4F, 4F, 1F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(0, 43).addBox(-3.5F, -0.5F, -8.05F, 7F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.05F))
                .texOffs(49, 56).addBox(-2.5F, -1.1F, -10.55F, 5F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.15F)),
                PartPose.offsetAndRotation(-2.598F, -4.4F, 1.5F, 0F, 2.0944F, 0F));
        PartDefinition p_petal3 = p_bulb.addOrReplaceChild("petal3", CubeListBuilder.create()
                .texOffs(51, 50).addBox(-2F, -0.5F, -3.4F, 4F, 1F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(25, 43).addBox(-3.5F, -0.5F, -8.05F, 7F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.05F))
                .texOffs(66, 56).addBox(-2.5F, -1.1F, -10.55F, 5F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, -4.4F, 3F, 0F, 3.1416F, 0F));
        PartDefinition p_petal4 = p_bulb.addOrReplaceChild("petal4", CubeListBuilder.create()
                .texOffs(68, 50).addBox(-2F, -0.5F, -3.4F, 4F, 1F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(50, 43).addBox(-3.5F, -0.5F, -8.05F, 7F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.05F))
                .texOffs(83, 56).addBox(-2.5F, -1.1F, -10.55F, 5F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.15F)),
                PartPose.offsetAndRotation(2.598F, -4.4F, 1.5F, 0F, 4.1888F, 0F));
        PartDefinition p_petal5 = p_bulb.addOrReplaceChild("petal5", CubeListBuilder.create()
                .texOffs(85, 50).addBox(-2F, -0.5F, -3.4F, 4F, 1F, 4F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(75, 43).addBox(-3.5F, -0.5F, -8.05F, 7F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.05F))
                .texOffs(100, 56).addBox(-2.5F, -1.1F, -10.55F, 5F, 1F, 3F, new CubeDeformation(-0.2F, 0F, -0.15F)),
                PartPose.offsetAndRotation(2.598F, -4.4F, -1.5F, 0F, 5.236F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
