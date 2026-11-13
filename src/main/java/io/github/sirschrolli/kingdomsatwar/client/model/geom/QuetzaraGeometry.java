package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Kopfs von Quetzara, der Gefiederten Schlange (1.10; im Spiel 1,5-fach vergrößert): Kiefer, Zähne, Zunge, Kamm, Krause.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class QuetzaraGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private QuetzaraGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(55, 0).addBox(-6F, -6F, -2F, 12F, 12F, 8F)
                .texOffs(0, 0).addBox(-8F, -6F, -12F, 16F, 10F, 11F)
                .texOffs(46, 22).addBox(7.4F, -3.6F, -10.5F, 2F, 7F, 8F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(67, 22).addBox(-9.4F, -3.6F, -10.5F, 2F, 7F, 8F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(73, 70).addBox(-7F, -5.9F, -16.5F, 14F, 6F, 5F, new CubeDeformation(-0.4F, -0.1F, -0.3F))
                .texOffs(39, 57).addBox(-5F, -5.6F, -21.6F, 10F, 6F, 6F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(52, 95).addBox(-4F, -4.7F, -23.9F, 8F, 5F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.1F))
                .texOffs(68, 83).addBox(-3.5F, -6.9F, -11F, 7F, 1F, 9F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(75, 95).addBox(5.7F, -5.2F, -15.8F, 2F, 3F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(88, 95).addBox(-7.7F, -5.2F, -15.8F, 2F, 3F, 4F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(15, 105).addBox(1.3F, -4.95F, -23.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(20, 105).addBox(-2.3F, -4.95F, -23.5F, 1F, 1F, 1F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_neck_oct = p_head.addOrReplaceChild("neck_oct", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-5.5F, -5.5F, -1.5F, 11F, 11F, 7F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.7854F));
        PartDefinition p_brow_l = p_head.addOrReplaceChild("brow_l", CubeListBuilder.create()
                .texOffs(72, 57).addBox(-3F, -1.65F, -3.5F, 6F, 2F, 10F, new CubeDeformation(-0.2F, -0.15F, 0F)),
                PartPose.offsetAndRotation(5.2F, -6F, -13F, -0.1222F, 0F, -0.3142F));
        PartDefinition p_brow_r = p_head.addOrReplaceChild("brow_r", CubeListBuilder.create()
                .texOffs(0, 70).addBox(-3F, -1.65F, -3.5F, 6F, 2F, 10F, new CubeDeformation(-0.2F, -0.15F, 0F)),
                PartPose.offsetAndRotation(-5.2F, -6F, -13F, -0.1222F, 0F, 0.3142F));
        PartDefinition p_lids = p_head.addOrReplaceChild("lids", CubeListBuilder.create()
                .texOffs(112, 83).addBox(5.775F, -5.7F, -16.3F, 2F, 4F, 5F, new CubeDeformation(-0.125F, -0.45F, -0.45F))
                .texOffs(0, 95).addBox(-7.775F, -5.7F, -16.3F, 2F, 4F, 5F, new CubeDeformation(-0.125F, -0.45F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_fangs = p_head.addOrReplaceChild("fangs", CubeListBuilder.create()
                .texOffs(123, 95).addBox(2.4F, -0.2F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(5, 105).addBox(2.4F, 3.25F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.2F))
                .texOffs(0, 105).addBox(-3.4F, -0.2F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(10, 105).addBox(-3.4F, 3.25F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.35F, -0.2F)),
                PartPose.offset(0F, -0.2F, -19.6F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-5F, -0.3F, -19.6F, 10F, 3F, 9F, new CubeDeformation(-0.4F, -0.3F, 0F))
                .texOffs(15, 95).addBox(-6.5F, 0F, -10.8F, 13F, 3F, 5F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(25, 83).addBox(-7.5F, -0.1F, -6F, 15F, 4F, 6F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(25, 105).addBox(3.45F, -0.9F, -18.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(30, 105).addBox(-4.45F, -0.9F, -18.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(35, 105).addBox(3.45F, -0.9F, -15.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(40, 105).addBox(-4.45F, -0.9F, -15.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(45, 105).addBox(3.45F, -0.9F, -12.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(50, 105).addBox(-4.45F, -0.9F, -12.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, -4F));
        PartDefinition p_tongue = p_jaw.addOrReplaceChild("tongue", CubeListBuilder.create()
                .texOffs(0, 83).addBox(-1F, -0.5F, -10F, 2F, 1F, 10F, new CubeDeformation(-0.4F, -0.3F, 0F)),
                PartPose.offset(0F, -0.3F, -8F));
        PartDefinition p_tongue_l = p_tongue.addOrReplaceChild("tongue_l", CubeListBuilder.create()
                .texOffs(101, 95).addBox(-0.5F, -0.5F, -3.6F, 1F, 1F, 4F, new CubeDeformation(-0.2F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0.25F, 0F, -10F, 0F, -0.3491F, 0F));
        PartDefinition p_tongue_r = p_tongue.addOrReplaceChild("tongue_r", CubeListBuilder.create()
                .texOffs(112, 95).addBox(-0.5F, -0.5F, -3.6F, 1F, 1F, 4F, new CubeDeformation(-0.2F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(-0.25F, 0F, -10F, 0F, 0.3491F, 0F));
        PartDefinition p_crest = p_head.addOrReplaceChild("crest", CubeListBuilder.create(),
                PartPose.offset(0F, -6.6F, -7.5F));
        PartDefinition p_crest_0 = p_crest.addOrReplaceChild("crest_0", CubeListBuilder.create()
                .texOffs(37, 22).addBox(-1.5F, -17F, -0.5F, 3F, 17F, 1F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.8378F, 0F, 0F));
        PartDefinition p_crest_1 = p_crest.addOrReplaceChild("crest_1", CubeListBuilder.create()
                .texOffs(11, 41).addBox(-1.5F, -14F, -0.5F, 3F, 14F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(2.2F, 0F, 2.64F, -0.925F, 0F, -0.2269F));
        PartDefinition p_crest_2 = p_crest.addOrReplaceChild("crest_2", CubeListBuilder.create()
                .texOffs(20, 41).addBox(-1.5F, -14F, -0.5F, 3F, 14F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-2.2F, 0F, 2.64F, -0.925F, 0F, 0.2269F));
        PartDefinition p_crest_3 = p_crest.addOrReplaceChild("crest_3", CubeListBuilder.create()
                .texOffs(55, 70).addBox(-1.5F, -11F, -0.5F, 3F, 11F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(4F, 0F, 4.8F, -1.0123F, 0F, -0.4712F));
        PartDefinition p_crest_4 = p_crest.addOrReplaceChild("crest_4", CubeListBuilder.create()
                .texOffs(64, 70).addBox(-1.5F, -11F, -0.5F, 3F, 11F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-4F, 0F, 4.8F, -1.0123F, 0F, 0.4712F));
        PartDefinition p_ruff = p_head.addOrReplaceChild("ruff", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 1.5F));
        PartDefinition p_ruff_0_base = p_ruff.addOrReplaceChild("ruff_0_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -2.0944F));
        PartDefinition p_ruff_0 = p_ruff_0_base.addOrReplaceChild("ruff_0", CubeListBuilder.create()
                .texOffs(51, 41).addBox(-2F, -11.75F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_1_base = p_ruff.addOrReplaceChild("ruff_1_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -1.7453F));
        PartDefinition p_ruff_1 = p_ruff_1_base.addOrReplaceChild("ruff_1", CubeListBuilder.create()
                .texOffs(88, 22).addBox(-2F, -13.75F, -0.5F, 4F, 14F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_2_base = p_ruff.addOrReplaceChild("ruff_2_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -1.3963F));
        PartDefinition p_ruff_2 = p_ruff_2_base.addOrReplaceChild("ruff_2", CubeListBuilder.create()
                .texOffs(99, 22).addBox(-2F, -13.7158F, -0.5F, 4F, 14F, 1F, new CubeDeformation(-0.3F, -0.2842F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_3_base = p_ruff.addOrReplaceChild("ruff_3_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -1.0472F));
        PartDefinition p_ruff_3 = p_ruff_3_base.addOrReplaceChild("ruff_3", CubeListBuilder.create()
                .texOffs(29, 41).addBox(-2F, -12.9486F, -0.5F, 4F, 13F, 1F, new CubeDeformation(-0.3F, -0.0514F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_4_base = p_ruff.addOrReplaceChild("ruff_4_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.6981F));
        PartDefinition p_ruff_4 = p_ruff_4_base.addOrReplaceChild("ruff_4", CubeListBuilder.create()
                .texOffs(62, 41).addBox(-2F, -11.9463F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.3F, -0.0537F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_5_base = p_ruff.addOrReplaceChild("ruff_5_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, -0.3491F));
        PartDefinition p_ruff_5 = p_ruff_5_base.addOrReplaceChild("ruff_5", CubeListBuilder.create()
                .texOffs(33, 70).addBox(-2F, -10.7695F, -0.5F, 4F, 11F, 1F, new CubeDeformation(-0.3F, -0.2305F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_6_base = p_ruff.addOrReplaceChild("ruff_6_base", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ruff_6 = p_ruff_6_base.addOrReplaceChild("ruff_6", CubeListBuilder.create()
                .texOffs(101, 83).addBox(-2F, -9F, -0.5F, 4F, 9F, 1F, new CubeDeformation(-0.3F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_7_base = p_ruff.addOrReplaceChild("ruff_7_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.3491F));
        PartDefinition p_ruff_7 = p_ruff_7_base.addOrReplaceChild("ruff_7", CubeListBuilder.create()
                .texOffs(44, 70).addBox(-2F, -10.7695F, -0.5F, 4F, 11F, 1F, new CubeDeformation(-0.3F, -0.2305F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_8_base = p_ruff.addOrReplaceChild("ruff_8_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 0.6981F));
        PartDefinition p_ruff_8 = p_ruff_8_base.addOrReplaceChild("ruff_8", CubeListBuilder.create()
                .texOffs(73, 41).addBox(-2F, -11.9463F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.3F, -0.0537F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_9_base = p_ruff.addOrReplaceChild("ruff_9_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 1.0472F));
        PartDefinition p_ruff_9 = p_ruff_9_base.addOrReplaceChild("ruff_9", CubeListBuilder.create()
                .texOffs(40, 41).addBox(-2F, -12.9486F, -0.5F, 4F, 13F, 1F, new CubeDeformation(-0.3F, -0.0514F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_10_base = p_ruff.addOrReplaceChild("ruff_10_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 1.3963F));
        PartDefinition p_ruff_10 = p_ruff_10_base.addOrReplaceChild("ruff_10", CubeListBuilder.create()
                .texOffs(110, 22).addBox(-2F, -13.7158F, -0.5F, 4F, 14F, 1F, new CubeDeformation(-0.3F, -0.2842F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_11_base = p_ruff.addOrReplaceChild("ruff_11_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 1.7453F));
        PartDefinition p_ruff_11 = p_ruff_11_base.addOrReplaceChild("ruff_11", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-2F, -13.75F, -0.5F, 4F, 14F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        PartDefinition p_ruff_12_base = p_ruff.addOrReplaceChild("ruff_12_base", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0F, 2.0944F));
        PartDefinition p_ruff_12 = p_ruff_12_base.addOrReplaceChild("ruff_12", CubeListBuilder.create()
                .texOffs(84, 41).addBox(-2F, -11.75F, -0.5F, 4F, 12F, 1F, new CubeDeformation(-0.3F, -0.25F, -0.25F)),
                PartPose.offsetAndRotation(0F, -5.4F, 0F, -1.309F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
