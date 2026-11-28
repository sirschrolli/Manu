package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Mondsteinkristalle (Kristallspur des Steinhüters, seit 0.96).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MoonCrystalGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 96;

    private MoonCrystalGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_crystals = root.addOrReplaceChild("crystals", CubeListBuilder.create(),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_crystal_0 = p_crystals.addOrReplaceChild("crystal_0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3.5F, -19.9F, -3.5F, 7F, 23F, 7F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.0698F, 0.1745F, 0.0524F));
        PartDefinition p_crystal_0_b = p_crystal_0.addOrReplaceChild("crystal_0_b", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-3F, -19.65F, -3F, 6F, 23F, 6F, new CubeDeformation(-0.06F, -0.35F, -0.06F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_0_tip = p_crystal_0.addOrReplaceChild("crystal_0_tip", CubeListBuilder.create()
                .texOffs(85, 31).addBox(-2.5F, -7.4F, -2.5F, 5F, 8F, 5F, new CubeDeformation(-0.33F, -0.2F, -0.33F)),
                PartPose.offsetAndRotation(0F, -19.4F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_0_tip_b = p_crystal_0_tip.addOrReplaceChild("crystal_0_tip_b", CubeListBuilder.create()
                .texOffs(106, 31).addBox(-2F, -7F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.1772F, -0.4F, -0.1772F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_0_point = p_crystal_0_tip.addOrReplaceChild("crystal_0_point", CubeListBuilder.create()
                .texOffs(88, 50).addBox(-1.5F, -3.65F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.38F, -0.05F, -0.38F)),
                PartPose.offsetAndRotation(0F, -6.9F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_1 = p_crystals.addOrReplaceChild("crystal_1", CubeListBuilder.create()
                .texOffs(96, 0).addBox(-2.5F, -11.61F, -2.5F, 5F, 15F, 5F, new CubeDeformation(-0.2F, -0.39F, -0.2F)),
                PartPose.offsetAndRotation(-5.6F, 0F, 2.4F, 0.2443F, 0.6109F, -0.5236F));
        PartDefinition p_crystal_1_b = p_crystal_1.addOrReplaceChild("crystal_1_b", CubeListBuilder.create()
                .texOffs(0, 31).addBox(-2F, -10.86F, -2F, 4F, 14F, 4F, new CubeDeformation(-0.068F, -0.14F, -0.068F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_1_tip = p_crystal_1.addOrReplaceChild("crystal_1_tip", CubeListBuilder.create()
                .texOffs(0, 50).addBox(-1.5F, -4.34F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.074F, -0.26F, -0.074F)),
                PartPose.offsetAndRotation(0F, -10.82F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_1_tip_b = p_crystal_1_tip.addOrReplaceChild("crystal_1_tip_b", CubeListBuilder.create()
                .texOffs(13, 50).addBox(-1.5F, -3.94F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.3022F, -0.46F, -0.3022F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_1_point = p_crystal_1_tip.addOrReplaceChild("crystal_1_point", CubeListBuilder.create()
                .texOffs(71, 59).addBox(-1F, -2.37F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.264F, -0.33F, -0.264F)),
                PartPose.offsetAndRotation(0F, -3.78F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_2 = p_crystals.addOrReplaceChild("crystal_2", CubeListBuilder.create()
                .texOffs(54, 0).addBox(-2.5F, -12.77F, -2.5F, 5F, 16F, 5F, new CubeDeformation(-0.1F, -0.23F, -0.1F)),
                PartPose.offsetAndRotation(5.2F, 0F, -2.2F, -0.2793F, -0.3491F, 0.4887F));
        PartDefinition p_crystal_2_b = p_crystal_2.addOrReplaceChild("crystal_2_b", CubeListBuilder.create()
                .texOffs(75, 0).addBox(-2.5F, -12.52F, -2.5F, 5F, 16F, 5F, new CubeDeformation(-0.484F, -0.48F, -0.484F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_2_tip = p_crystal_2.addOrReplaceChild("crystal_2_tip", CubeListBuilder.create()
                .texOffs(26, 50).addBox(-1.5F, -4.58F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.012F, -0.02F, -0.012F)),
                PartPose.offsetAndRotation(0F, -12.14F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_2_tip_b = p_crystal_2_tip.addOrReplaceChild("crystal_2_tip_b", CubeListBuilder.create()
                .texOffs(39, 50).addBox(-1.5F, -4.18F, -1.5F, 3F, 5F, 3F, new CubeDeformation(-0.2501F, -0.22F, -0.2501F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_2_point = p_crystal_2_tip.addOrReplaceChild("crystal_2_point", CubeListBuilder.create()
                .texOffs(80, 59).addBox(-1F, -2.49F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.232F, -0.21F, -0.232F)),
                PartPose.offsetAndRotation(0F, -4.26F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_3 = p_crystals.addOrReplaceChild("crystal_3", CubeListBuilder.create()
                .texOffs(17, 31).addBox(-2F, -9.62F, -2F, 4F, 13F, 4F, new CubeDeformation(0F, -0.38F, 0F)),
                PartPose.offsetAndRotation(1.6F, 0F, 5.8F, 0.5934F, 1.0472F, 0.1396F));
        PartDefinition p_crystal_3_b = p_crystal_3.addOrReplaceChild("crystal_3_b", CubeListBuilder.create()
                .texOffs(34, 31).addBox(-2F, -8.87F, -2F, 4F, 12F, 4F, new CubeDeformation(-0.32F, -0.13F, -0.32F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_3_tip = p_crystal_3.addOrReplaceChild("crystal_3_tip", CubeListBuilder.create()
                .texOffs(101, 50).addBox(-1.5F, -3.48F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.26F, -0.12F, -0.26F)),
                PartPose.offsetAndRotation(0F, -8.84F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_3_tip_b = p_crystal_3_tip.addOrReplaceChild("crystal_3_tip_b", CubeListBuilder.create()
                .texOffs(114, 50).addBox(-1.5F, -3.08F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.4584F, -0.32F, -0.4584F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_3_point = p_crystal_3_tip.addOrReplaceChild("crystal_3_point", CubeListBuilder.create()
                .texOffs(98, 59).addBox(-1F, -1.69F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.36F, -0.01F, -0.36F)),
                PartPose.offsetAndRotation(0F, -3.06F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_4 = p_crystals.addOrReplaceChild("crystal_4", CubeListBuilder.create()
                .texOffs(51, 31).addBox(-2F, -7.96F, -2F, 4F, 11F, 4F, new CubeDeformation(-0.2F, -0.04F, -0.2F)),
                PartPose.offsetAndRotation(-2.2F, 0F, -5.6F, -0.5585F, 0.2618F, -0.2094F));
        PartDefinition p_crystal_4_b = p_crystal_4.addOrReplaceChild("crystal_4_b", CubeListBuilder.create()
                .texOffs(68, 31).addBox(-2F, -7.71F, -2F, 4F, 11F, 4F, new CubeDeformation(-0.488F, -0.29F, -0.488F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_4_tip = p_crystal_4.addOrReplaceChild("crystal_4_tip", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-1.5F, -3.24F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.384F, -0.36F, -0.384F)),
                PartPose.offsetAndRotation(0F, -7.52F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_crystal_4_tip_b = p_crystal_4_tip.addOrReplaceChild("crystal_4_tip_b", CubeListBuilder.create()
                .texOffs(89, 59).addBox(-1F, -2.34F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.0626F, -0.06F, -0.0626F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crystal_4_point = p_crystal_4_tip.addOrReplaceChild("crystal_4_point", CubeListBuilder.create()
                .texOffs(107, 59).addBox(-1F, -1.57F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.424F, -0.13F, -0.424F)),
                PartPose.offsetAndRotation(0F, -2.58F, 0F, 0F, 0.3927F, 0F));
        PartDefinition p_rubble = root.addOrReplaceChild("rubble", CubeListBuilder.create()
                .texOffs(71, 50).addBox(-9F, -2F, -3.8F, 4F, 3F, 4F, new CubeDeformation(0F, -0.2F, -0.3F))
                .texOffs(28, 59).addBox(7.2F, -1.2F, -6F, 4F, 2F, 3F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(52, 50).addBox(7.8F, -2.2F, 3.8F, 5F, 3F, 4F, new CubeDeformation(-0.4F, 0F, -0.2F))
                .texOffs(43, 59).addBox(-3.2F, -1.2F, 8.5F, 4F, 2F, 3F, new CubeDeformation(-0.2F, 0F, -0.1F))
                .texOffs(58, 59).addBox(-8.2F, -1F, 6.1F, 3F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(13, 59).addBox(2F, -1.9F, -9.8F, 4F, 3F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.2F)),
                PartPose.offset(0F, 24F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
