package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Aschenkönigs (drei Köpfe wo der Vanilla-Wither feuert, Krone, Hörner, Joch, Brustkorb mit Herz, Mantel, Schwanz und Krallenarme; im Spiel ×2). Mit Hülle für den Seelenschild.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class AschenkoenigGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private AschenkoenigGeometry() {}

    public static MeshDefinition createMesh() {
        return createMesh(0F);
    }

    /** Wie {@link #createMesh()}, alle Quader um {@code grow} vergrößert (Hüllen). */
    public static MeshDefinition createMesh(float grow) {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_center_head = root.addOrReplaceChild("center_head", CubeListBuilder.create()
                .texOffs(39, 0).addBox(-4.5F, -5F, -4.5F, 9F, 9F, 9F, new CubeDeformation(0F + grow))
                .texOffs(35, 72).addBox(-4.5F, -2.85F, -5.2F, 9F, 2F, 1F, new CubeDeformation(0F + grow, -0.45F + grow, -0.3F + grow)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_center_jaw = p_center_head.addOrReplaceChild("center_jaw", CubeListBuilder.create()
                .texOffs(86, 34).addBox(-3.5F, -0.4F, -4.15F, 7F, 3F, 5F, new CubeDeformation(-0.25F + grow, -0.4F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(0F, 4F, -0.5F, 0.1222F, 0F, 0F));
        PartDefinition p_crown = p_center_head.addOrReplaceChild("crown", CubeListBuilder.create()
                .texOffs(58, 19).addBox(-5F, -1.5F, -5F, 10F, 2F, 10F, new CubeDeformation(0F + grow))
                .texOffs(9, 66).addBox(-1F, -5.5F, -5.4F, 2F, 4F, 1F, new CubeDeformation(-0.25F + grow, 0F + grow, -0.1F + grow))
                .texOffs(0, 72).addBox(-3.75F, -4.25F, -5.4F, 1F, 3F, 1F, new CubeDeformation(0F + grow, -0.25F + grow, -0.15F + grow))
                .texOffs(5, 72).addBox(2.75F, -4.25F, -5.4F, 1F, 3F, 1F, new CubeDeformation(0F + grow, -0.25F + grow, -0.15F + grow))
                .texOffs(10, 72).addBox(-5.4F, -4.5F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F + grow, 0F + grow, 0F + grow))
                .texOffs(15, 72).addBox(4.4F, -4.5F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F + grow, 0F + grow, 0F + grow))
                .texOffs(20, 72).addBox(-0.5F, -4.25F, 4.4F, 1F, 3F, 1F, new CubeDeformation(0F + grow, -0.25F + grow, -0.1F + grow))
                .texOffs(56, 72).addBox(-3.75F, -3.25F, 4.3F, 1F, 2F, 1F, new CubeDeformation(0F + grow, -0.25F + grow, -0.15F + grow))
                .texOffs(61, 72).addBox(2.75F, -3.25F, 4.3F, 1F, 2F, 1F, new CubeDeformation(0F + grow, -0.25F + grow, -0.15F + grow)),
                PartPose.offset(0F, -5F, 0F));
        PartDefinition p_crown_fire = p_crown.addOrReplaceChild("crown_fire", CubeListBuilder.create(),
                PartPose.offset(0F, -0.5F, 0F));
        PartDefinition p_crown_fire_1 = p_crown_fire.addOrReplaceChild("crown_fire_1", CubeListBuilder.create()
                .texOffs(87, 52).addBox(-4F, -6F, 0F, 8F, 6F, 0F, new CubeDeformation(0F + grow)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, 0.7854F, 0F));
        PartDefinition p_crown_fire_2 = p_crown_fire.addOrReplaceChild("crown_fire_2", CubeListBuilder.create()
                .texOffs(104, 52).addBox(-4F, -6F, 0F, 8F, 6F, 0F, new CubeDeformation(0F + grow)),
                PartPose.offsetAndRotation(0F, 0F, 0F, 0F, -0.7854F, 0F));
        PartDefinition p_right_head = root.addOrReplaceChild("right_head", CubeListBuilder.create()
                .texOffs(0, 19).addBox(-3.5F, -3.5F, -3.5F, 7F, 7F, 7F, new CubeDeformation(0F + grow))
                .texOffs(66, 72).addBox(-3.5F, -1.65F, -4.175F, 7F, 1F, 1F, new CubeDeformation(0F + grow, -0.05F + grow, -0.325F + grow)),
                PartPose.offset(-9.5F, 2.5F, 0F));
        PartDefinition p_right_jaw = p_right_head.addOrReplaceChild("right_jaw", CubeListBuilder.create()
                .texOffs(49, 52).addBox(-2.5F, -0.1F, -3.15F, 5F, 2F, 4F, new CubeDeformation(-0.1F + grow, -0.1F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(0F, 3.5F, -0.5F, 0.1222F, 0F, 0F));
        PartDefinition p_right_horn_inner = p_right_head.addOrReplaceChild("right_horn_inner", CubeListBuilder.create()
                .texOffs(0, 59).addBox(-1F, -1F, -0.25F, 2F, 2F, 4F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(2.2F, -3F, 0.5F, 0.6981F, 0.4363F, 0F));
        PartDefinition p_right_horn_inner_tip = p_right_horn_inner.addOrReplaceChild("right_horn_inner_tip", CubeListBuilder.create()
                .texOffs(78, 66).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F, new CubeDeformation(0F + grow)),
                PartPose.offsetAndRotation(0F, 0F, 3.5F, 0.6109F, 0F, 0F));
        PartDefinition p_right_horn_outer = p_right_head.addOrReplaceChild("right_horn_outer", CubeListBuilder.create()
                .texOffs(13, 59).addBox(-1F, -1F, -0.25F, 2F, 2F, 4F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(-2.2F, -3F, 0.5F, 0.6981F, -0.4363F, 0F));
        PartDefinition p_right_horn_outer_tip = p_right_horn_outer.addOrReplaceChild("right_horn_outer_tip", CubeListBuilder.create()
                .texOffs(87, 66).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F, new CubeDeformation(0F + grow)),
                PartPose.offsetAndRotation(0F, 0F, 3.5F, 0.6109F, 0F, 0F));
        PartDefinition p_left_head = root.addOrReplaceChild("left_head", CubeListBuilder.create()
                .texOffs(29, 19).addBox(-3.5F, -3.5F, -3.5F, 7F, 7F, 7F, new CubeDeformation(0F + grow))
                .texOffs(83, 72).addBox(-3.5F, -1.65F, -4.175F, 7F, 1F, 1F, new CubeDeformation(0F + grow, -0.05F + grow, -0.325F + grow)),
                PartPose.offset(9.5F, 2.5F, 0F));
        PartDefinition p_left_jaw = p_left_head.addOrReplaceChild("left_jaw", CubeListBuilder.create()
                .texOffs(68, 52).addBox(-2.5F, -0.1F, -3.15F, 5F, 2F, 4F, new CubeDeformation(-0.1F + grow, -0.1F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(0F, 3.5F, -0.5F, 0.1222F, 0F, 0F));
        PartDefinition p_left_horn_inner = p_left_head.addOrReplaceChild("left_horn_inner", CubeListBuilder.create()
                .texOffs(26, 59).addBox(-1F, -1F, -0.25F, 2F, 2F, 4F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(-2.2F, -3F, 0.5F, 0.6981F, -0.4363F, 0F));
        PartDefinition p_left_horn_inner_tip = p_left_horn_inner.addOrReplaceChild("left_horn_inner_tip", CubeListBuilder.create()
                .texOffs(96, 66).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F, new CubeDeformation(0F + grow)),
                PartPose.offsetAndRotation(0F, 0F, 3.5F, 0.6109F, 0F, 0F));
        PartDefinition p_left_horn_outer = p_left_head.addOrReplaceChild("left_horn_outer", CubeListBuilder.create()
                .texOffs(39, 59).addBox(-1F, -1F, -0.25F, 2F, 2F, 4F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(2.2F, -3F, 0.5F, 0.6981F, 0.4363F, 0F));
        PartDefinition p_left_horn_outer_tip = p_left_horn_outer.addOrReplaceChild("left_horn_outer_tip", CubeListBuilder.create()
                .texOffs(105, 66).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F, new CubeDeformation(0F + grow)),
                PartPose.offsetAndRotation(0F, 0F, 3.5F, 0.6109F, 0F, 0F));
        PartDefinition p_shoulders = root.addOrReplaceChild("shoulders", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-12F, 6F, -2F, 24F, 3F, 4F, new CubeDeformation(0F + grow))
                .texOffs(52, 59).addBox(-1.5F, 3.6F, -0.5F, 3F, 3F, 3F, new CubeDeformation(0F + grow, -0.4F + grow, 0F + grow))
                .texOffs(0, 52).addBox(-10F, 5F, -0.1F, 20F, 2F, 4F, new CubeDeformation(0F + grow, -0.4F + grow, -0.3F + grow))
                .texOffs(0, 34).addBox(-14.75F, 5.25F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.25F + grow, -0.25F + grow, 0F + grow))
                .texOffs(25, 34).addBox(8.75F, 5.25F, -3F, 6F, 3F, 6F, new CubeDeformation(-0.25F + grow, -0.25F + grow, 0F + grow)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_spike_1 = p_shoulders.addOrReplaceChild("right_spike_1", CubeListBuilder.create()
                .texOffs(65, 59).addBox(-1F, -4F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F + grow, 0F + grow, -0.4F + grow)),
                PartPose.offsetAndRotation(-13.5F, 5.8F, -1.5F, -0.2618F, 0F, -0.5236F));
        PartDefinition p_right_spike_2 = p_shoulders.addOrReplaceChild("right_spike_2", CubeListBuilder.create()
                .texOffs(16, 66).addBox(-0.5F, -3.6F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F + grow, -0.4F + grow, 0F + grow)),
                PartPose.offsetAndRotation(-12.8F, 5.8F, 1.6F, 0.384F, 0F, -0.4189F));
        PartDefinition p_left_spike_1 = p_shoulders.addOrReplaceChild("left_spike_1", CubeListBuilder.create()
                .texOffs(74, 59).addBox(-1F, -4F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F + grow, 0F + grow, -0.4F + grow)),
                PartPose.offsetAndRotation(13.5F, 5.8F, -1.5F, -0.2618F, 0F, 0.5236F));
        PartDefinition p_left_spike_2 = p_shoulders.addOrReplaceChild("left_spike_2", CubeListBuilder.create()
                .texOffs(21, 66).addBox(-0.5F, -3.6F, -0.5F, 1F, 4F, 1F, new CubeDeformation(0F + grow, -0.4F + grow, 0F + grow)),
                PartPose.offsetAndRotation(12.8F, 5.8F, 1.6F, 0.384F, 0F, 0.4189F));
        PartDefinition p_ribcage = root.addOrReplaceChild("ribcage", CubeListBuilder.create()
                .texOffs(76, 0).addBox(-5F, 0F, -3.5F, 10F, 8F, 6F, new CubeDeformation(0F + grow))
                .texOffs(99, 19).addBox(-1F, -0.5F, 1.6F, 2F, 10F, 2F, new CubeDeformation(0F + grow, 0F + grow, -0.1F + grow))
                .texOffs(57, 44).addBox(-4.5F, 8.25F, -2F, 9F, 3F, 4F, new CubeDeformation(0F + grow, -0.25F + grow, 0F + grow)),
                PartPose.offsetAndRotation(0F, 8.5F, 0.5F, 0.2094F, 0F, 0F));
        PartDefinition p_heart = p_ribcage.addOrReplaceChild("heart", CubeListBuilder.create()
                .texOffs(111, 34).addBox(-2F, -2F, -2F, 4F, 4F, 4F, new CubeDeformation(0F + grow, 0F + grow, -0.25F + grow)),
                PartPose.offset(0F, 3.5F, -0.5F));
        PartDefinition p_cape = p_ribcage.addOrReplaceChild("cape", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9F, 0F, -0.25F, 18F, 17F, 1F, new CubeDeformation(0F + grow, 0F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(0F, -2.5F, 3.2F, 0.0698F, 0F, 0F));
        PartDefinition p_tail = p_ribcage.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(84, 44).addBox(-1.5F, -0.25F, -1.5F, 3F, 4F, 3F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow))
                .texOffs(114, 66).addBox(-0.5F, 0.4F, 1F, 1F, 2F, 2F, new CubeDeformation(-0.1F + grow, 0F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(0F, 10.5F, 0.8F, 0.4887F, 0F, 0F));
        PartDefinition p_tail2 = p_tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(109, 59).addBox(-1F, 0F, -1F, 2F, 3F, 2F, new CubeDeformation(0F + grow))
                .texOffs(121, 66).addBox(-0.5F, 0.1F, 0.6F, 1F, 2F, 2F, new CubeDeformation(-0.15F + grow, -0.2F + grow, -0.4F + grow)),
                PartPose.offsetAndRotation(0F, 3.5F, 0F, 0.3142F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(118, 59).addBox(-1F, -0.25F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow))
                .texOffs(0, 66).addBox(-1.5F, 2.5F, -0.5F, 3F, 4F, 1F, new CubeDeformation(0F + grow, 0F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(0F, 3F, 0F, 0.3142F, 0F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(50, 34).addBox(-1F, -0.5F, -1F, 2F, 7F, 2F, new CubeDeformation(0F + grow))
                .texOffs(83, 59).addBox(-1.5F, 5.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(-11F, 8F, 0F, -0.1396F, 0F, 0.2443F));
        PartDefinition p_right_forearm = p_right_arm.addOrReplaceChild("right_forearm", CubeListBuilder.create()
                .texOffs(59, 34).addBox(-1F, -0.25F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.2F + grow, -0.25F + grow, -0.2F + grow)),
                PartPose.offsetAndRotation(0F, 7F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_right_hand = p_right_forearm.addOrReplaceChild("right_hand", CubeListBuilder.create()
                .texOffs(56, 66).addBox(-1.5F, -0.25F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.2F + grow, -0.25F + grow, 0F + grow))
                .texOffs(26, 66).addBox(-1.2F, 1F, -1.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F + grow, -0.4F + grow, -0.2F + grow))
                .texOffs(31, 66).addBox(-0.5F, 1.3F, -1.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F + grow, -0.1F + grow, -0.2F + grow))
                .texOffs(36, 66).addBox(0.2F, 1F, -1.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F + grow, -0.4F + grow, -0.2F + grow))
                .texOffs(25, 72).addBox(0.8F, 0.2F, -1.4F, 1F, 3F, 1F, new CubeDeformation(-0.2F + grow, -0.4F + grow, -0.2F + grow)),
                PartPose.offsetAndRotation(0F, 6.5F, 0F, -0.4363F, 0F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(68, 34).addBox(-1F, -0.5F, -1F, 2F, 7F, 2F, new CubeDeformation(0F + grow))
                .texOffs(96, 59).addBox(-1.5F, 5.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F + grow, -0.25F + grow, -0.25F + grow)),
                PartPose.offsetAndRotation(11F, 8F, 0F, -0.1396F, 0F, -0.2443F));
        PartDefinition p_left_forearm = p_left_arm.addOrReplaceChild("left_forearm", CubeListBuilder.create()
                .texOffs(77, 34).addBox(-1F, -0.25F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.2F + grow, -0.25F + grow, -0.2F + grow)),
                PartPose.offsetAndRotation(0F, 7F, 0F, -0.6632F, 0F, 0F));
        PartDefinition p_left_hand = p_left_forearm.addOrReplaceChild("left_hand", CubeListBuilder.create()
                .texOffs(67, 66).addBox(-1.5F, -0.25F, -1F, 3F, 2F, 2F, new CubeDeformation(-0.2F + grow, -0.25F + grow, 0F + grow))
                .texOffs(41, 66).addBox(-1.2F, 1F, -1.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F + grow, -0.4F + grow, -0.2F + grow))
                .texOffs(46, 66).addBox(-0.5F, 1.3F, -1.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F + grow, -0.1F + grow, -0.2F + grow))
                .texOffs(51, 66).addBox(0.2F, 1F, -1.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F + grow, -0.4F + grow, -0.2F + grow))
                .texOffs(30, 72).addBox(-1.8F, 0.2F, -1.4F, 1F, 3F, 1F, new CubeDeformation(-0.2F + grow, -0.4F + grow, -0.2F + grow)),
                PartPose.offsetAndRotation(0F, 6.5F, 0F, -0.4363F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return createLayer(0F);
    }

    public static LayerDefinition createLayer(float grow) {
        return LayerDefinition.create(createMesh(grow), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
