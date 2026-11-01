package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Gesetzlosen (Skelett des Vanilla-Illagers mit Menschenkopf; Kapuzen, Mützen, Bärte, Roben als eigene Knochen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class IllagerGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private IllagerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-4F, -8F, -4F, 8F, 8F, 8F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(33, 27).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(9, 106).addBox(-1F, 0F, -1F, 2F, 2F, 1F),
                PartPose.offset(0F, -4F, -4F));
        PartDefinition p_hood = p_head.addOrReplaceChild("hood", CubeListBuilder.create()
                .texOffs(66, 27).addBox(-4F, -8F, -4F, 8F, 8F, 8F, new CubeDeformation(0.95F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_liripipe = p_hood.addOrReplaceChild("liripipe", CubeListBuilder.create()
                .texOffs(106, 87).addBox(-1.5F, -1.4F, -0.3F, 3F, 3F, 5F, new CubeDeformation(-0.2F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, -6.6F, 4.4F, -0.8727F, 0F, 0F));
        PartDefinition p_liripipe_tip = p_liripipe.addOrReplaceChild("liripipe_tip", CubeListBuilder.create()
                .texOffs(21, 98).addBox(-1F, -0.8F, 0F, 2F, 2F, 5F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 0.2F, 4F, -0.5236F, 0F, 0F));
        PartDefinition p_hood_point = p_hood.addOrReplaceChild("hood_point", CubeListBuilder.create()
                .texOffs(26, 87).addBox(-3.5F, -1.6F, -3.5F, 7F, 2F, 7F, new CubeDeformation(-0.1F, -0.4F, -0.1F))
                .texOffs(0, 98).addBox(-2.5F, -2.8F, -2.5F, 5F, 2F, 5F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(84, 98).addBox(-1.5F, -4.1F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, -8.9F, 0.6F, -0.1745F, 0F, 0F));
        PartDefinition p_jester = p_hood.addOrReplaceChild("jester", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_jester_r = p_jester.addOrReplaceChild("jester_r", CubeListBuilder.create()
                .texOffs(55, 87).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-3.2F, -8.6F, 0F, 0F, 0F, 0.9076F));
        PartDefinition p_jester_r2 = p_jester_r.addOrReplaceChild("jester_r2", CubeListBuilder.create()
                .texOffs(66, 98).addBox(-1F, -4F, -1F, 2F, 4F, 2F)
                .texOffs(119, 98).addBox(-1F, -5.7F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, 1.0123F));
        PartDefinition p_jester_l = p_jester.addOrReplaceChild("jester_l", CubeListBuilder.create()
                .texOffs(68, 87).addBox(-1.5F, -6F, -1.5F, 3F, 6F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(3.2F, -8.6F, 0F, 0F, 0F, -0.9076F));
        PartDefinition p_jester_l2 = p_jester_l.addOrReplaceChild("jester_l2", CubeListBuilder.create()
                .texOffs(75, 98).addBox(-1F, -4F, -1F, 2F, 4F, 2F)
                .texOffs(0, 106).addBox(-1F, -5.7F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(0F, -6F, 0F, 0F, 0F, -1.0123F));
        PartDefinition p_cap = p_head.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(68, 44).addBox(-5F, -9.5F, -5F, 10F, 4F, 10F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(0, 87).addBox(-4.9F, -6.4F, -2.5F, 1F, 5F, 5F, new CubeDeformation(0F, -0.2F, -0.1F))
                .texOffs(13, 87).addBox(3.9F, -6.4F, -2.5F, 1F, 5F, 5F, new CubeDeformation(0F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_beard = p_head.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(47, 98).addBox(-4F, -3.6F, -4.75F, 8F, 5F, 1F, new CubeDeformation(0F, -0.2F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_braid = p_beard.addOrReplaceChild("braid", CubeListBuilder.create()
                .texOffs(36, 98).addBox(-1.5F, -0.3F, -0.8F, 3F, 5F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.4F)),
                PartPose.offset(0F, 1.2F, -4.5F));
        PartDefinition p_beard_long = p_head.addOrReplaceChild("beard_long", CubeListBuilder.create()
                .texOffs(109, 44).addBox(-4F, -3.6F, -4.7F, 8F, 13F, 1F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(58, 0).addBox(-4F, 0F, -3F, 8F, 12F, 6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_coat = p_body.addOrReplaceChild("coat", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-4F, 0F, -3F, 8F, 16F, 6F, new CubeDeformation(0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_robe = p_body.addOrReplaceChild("robe", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, 0F, -3F, 8F, 20F, 6F, new CubeDeformation(0.5F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_collar = p_body.addOrReplaceChild("collar", CubeListBuilder.create()
                .texOffs(15, 61).addBox(-5F, -0.9F, -4F, 10F, 4F, 8F, new CubeDeformation(0F, -0.3F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cape = p_body.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(86, 61).addBox(-5.5F, -1.1F, -4F, 11F, 3F, 8F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(87, 0).addBox(-5F, 1.6F, 2.95F, 10F, 15F, 2F, new CubeDeformation(-0.2F, 0F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_apron = p_body.addOrReplaceChild("apron", CubeListBuilder.create()
                .texOffs(51, 44).addBox(-3.5F, 3.5F, -3.95F, 7F, 14F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_belt = p_body.addOrReplaceChild("belt", CubeListBuilder.create()
                .texOffs(92, 75).addBox(-5F, 8.65F, -4F, 10F, 2F, 8F, new CubeDeformation(-0.4F, -0.15F, -0.4F))
                .texOffs(97, 98).addBox(1.4F, 9.5F, -4.7F, 3F, 3F, 2F, new CubeDeformation(-0.2F, -0.1F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_quiver = p_body.addOrReplaceChild("quiver", CubeListBuilder.create()
                .texOffs(0, 61).addBox(-2F, -5F, -0.4F, 4F, 10F, 3F, new CubeDeformation(-0.4F, 0F, -0.4F))
                .texOffs(108, 98).addBox(-1.5F, -6.8F, 0.1F, 3F, 2F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0.5F, 5.5F, 3.7F, 0F, 0F, 0.4189F));
        PartDefinition p_arms = root.addOrReplaceChild("arms", CubeListBuilder.create()
                .texOffs(52, 61).addBox(-8F, -2F, -2F, 4F, 8F, 4F)
                .texOffs(81, 87).addBox(-4F, 2F, -2F, 8F, 4F, 4F),
                PartPose.offsetAndRotation(0F, 3F, -1F, -0.75F, 0F, 0F));
        PartDefinition p_left_shoulder = p_arms.addOrReplaceChild("left_shoulder", CubeListBuilder.create()
                .texOffs(69, 61).addBox(4F, -2F, -2F, 4F, 8F, 4F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cross_cuffs = p_arms.addOrReplaceChild("cross_cuffs", CubeListBuilder.create()
                .texOffs(0, 75).addBox(-9F, 1.5F, -3F, 6F, 5F, 6F, new CubeDeformation(-0.4F, -0.1F, -0.4F))
                .texOffs(25, 75).addBox(3F, 1.5F, -3F, 6F, 5F, 6F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(99, 27).addBox(-3F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(-5F, 2F, 0F));
        PartDefinition p_right_cuff = p_right_arm.addOrReplaceChild("right_cuff", CubeListBuilder.create()
                .texOffs(50, 75).addBox(-3.5F, 4.3F, -2.5F, 5F, 6F, 5F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-1F, -2F, -2F, 4F, 12F, 4F),
                PartPose.offset(5F, 2F, 0F));
        PartDefinition p_left_cuff = p_left_arm.addOrReplaceChild("left_cuff", CubeListBuilder.create()
                .texOffs(71, 75).addBox(-1.5F, 4.3F, -2.5F, 5F, 6F, 5F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(17, 44).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(-2F, 12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(34, 44).addBox(-2F, 0F, -2F, 4F, 12F, 4F),
                PartPose.offset(2F, 12F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
