package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Seelenhirten (seit 0.98): hagerer, gebeugter Riese in zerschlissener Kutte mit Runensaum, Schaffell über den Schultern, Kapuze mit Widderschädel, gerollten Hörnern und Seelenlicht in den Augen, knochige Arme, Hirtenstab mit Haken und Seelenlaterne, Handglocke, Talismane am Strick, drei Irrlichter; im Spiel ×1,5.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SoulShepherdGeometry {
    public static final int TEXTURE_WIDTH = 256;
    public static final int TEXTURE_HEIGHT = 256;

    private SoulShepherdGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(86, 0).addBox(-1.5F, -0.3F, -1.5F, 3F, 13F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(119, 61).addBox(-1.5F, 12.1F, -4.4F, 3F, 2F, 5F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(168, 85).addBox(-1.5F, 12.45F, -5.25F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.45F, -0.05F)),
                PartPose.offset(2F, 10F, 0.4F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(99, 0).addBox(-1.5F, -0.3F, -1.5F, 3F, 13F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.4F))
                .texOffs(136, 61).addBox(-1.5F, 12.1F, -4.4F, 3F, 2F, 5F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(177, 85).addBox(-1.5F, 12.45F, -5.25F, 3F, 2F, 1F, new CubeDeformation(-0.3F, -0.45F, -0.05F)),
                PartPose.offset(-2F, 10F, 0.4F));
        PartDefinition p_skirt = root.addOrReplaceChild("skirt", CubeListBuilder.create()
                .texOffs(0, 47).addBox(-4.5F, -1.5F, -3F, 9F, 7F, 6F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(207, 0).addBox(-5F, 4.6F, -3.5F, 10F, 6F, 7F, new CubeDeformation(-0.2F, -0.4F, -0.1F)),
                PartPose.offset(0F, 10F, 0.4F));
        PartDefinition p_hem_0 = p_skirt.addOrReplaceChild("hem_0", CubeListBuilder.create()
                .texOffs(44, 79).addBox(-1.5F, -0.1F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(-3.6F, 10F, -3.4F, -0.0698F, 0F, 0F));
        PartDefinition p_hem_1 = p_skirt.addOrReplaceChild("hem_1", CubeListBuilder.create()
                .texOffs(53, 79).addBox(-1.5F, -0.4F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-1.2F, 10F, -3.4F, -0.1222F, 0F, 0F));
        PartDefinition p_hem_2 = p_skirt.addOrReplaceChild("hem_2", CubeListBuilder.create()
                .texOffs(62, 79).addBox(-1.5F, -0.2F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(1.2F, 10F, -3.4F, -0.0698F, 0F, 0F));
        PartDefinition p_hem_3 = p_skirt.addOrReplaceChild("hem_3", CubeListBuilder.create()
                .texOffs(233, 79).addBox(-1.5F, 0F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.25F, 0F, -0.15F)),
                PartPose.offsetAndRotation(3.6F, 10F, -3.4F, -0.1222F, 0F, 0F));
        PartDefinition p_hem_4 = p_skirt.addOrReplaceChild("hem_4", CubeListBuilder.create()
                .texOffs(71, 79).addBox(-1.5F, -0.3F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.15F)),
                PartPose.offsetAndRotation(-3.6F, 10F, 3.4F, -0.0698F, 3.1416F, 0F));
        PartDefinition p_hem_5 = p_skirt.addOrReplaceChild("hem_5", CubeListBuilder.create()
                .texOffs(80, 79).addBox(-1.5F, 0F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, 0F, -0.15F)),
                PartPose.offsetAndRotation(-1.2F, 10F, 3.4F, -0.1222F, 3.1416F, 0F));
        PartDefinition p_hem_6 = p_skirt.addOrReplaceChild("hem_6", CubeListBuilder.create()
                .texOffs(89, 79).addBox(-1.5F, -0.4F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(1.2F, 10F, 3.4F, -0.0698F, 3.1416F, 0F));
        PartDefinition p_hem_7 = p_skirt.addOrReplaceChild("hem_7", CubeListBuilder.create()
                .texOffs(98, 79).addBox(-1.5F, -0.1F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(3.6F, 10F, 3.4F, -0.1222F, 3.1416F, 0F));
        PartDefinition p_hem_8 = p_skirt.addOrReplaceChild("hem_8", CubeListBuilder.create()
                .texOffs(107, 79).addBox(-1.5F, -0.3F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.3F, -0.15F)),
                PartPose.offsetAndRotation(4.8F, 10F, -1.4F, -0.0698F, 1.5708F, 0F));
        PartDefinition p_hem_9 = p_skirt.addOrReplaceChild("hem_9", CubeListBuilder.create()
                .texOffs(116, 79).addBox(-1.5F, -0.1F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.1F, -0.15F)),
                PartPose.offsetAndRotation(4.8F, 10F, 1.4F, -0.1222F, 1.5708F, 0F));
        PartDefinition p_hem_10 = p_skirt.addOrReplaceChild("hem_10", CubeListBuilder.create()
                .texOffs(125, 79).addBox(-1.5F, -0.2F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.15F)),
                PartPose.offsetAndRotation(-4.8F, 10F, -1.4F, -0.0698F, -1.5708F, 0F));
        PartDefinition p_hem_11 = p_skirt.addOrReplaceChild("hem_11", CubeListBuilder.create()
                .texOffs(134, 79).addBox(-1.5F, -0.4F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.25F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(-4.8F, 10F, 1.4F, -0.1222F, -1.5708F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(112, 0).addBox(-4F, -8.6F, -2.5F, 8F, 9F, 5F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(54, 61).addBox(-4F, -2.3F, -3F, 8F, 2F, 6F, new CubeDeformation(-0.05F, -0.4F, -0.25F))
                .texOffs(204, 85).addBox(2.3F, -2.5F, -3F, 2F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, 10F, 0.4F, 0.1396F, 0F, 0F));
        PartDefinition p_charm_0 = p_body.addOrReplaceChild("charm_0", CubeListBuilder.create()
                .texOffs(215, 79).addBox(-0.5F, -0.2F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.35F))
                .texOffs(90, 85).addBox(-0.5F, 3.3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.15F, -0.3F, -0.15F))
                .texOffs(0, 90).addBox(-1F, 5.65F, -0.5F, 2F, 1F, 1F, new CubeDeformation(-0.3F, -0.15F, 0F)),
                PartPose.offsetAndRotation(-2.4F, -1.4F, -2.9F, -0.1396F, 0F, -0.0873F));
        PartDefinition p_charm_1 = p_body.addOrReplaceChild("charm_1", CubeListBuilder.create()
                .texOffs(187, 71).addBox(-0.5F, -0.2F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.35F, -0.2F, -0.35F))
                .texOffs(242, 79).addBox(-1F, 4.35F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(211, 85).addBox(-0.5F, 5.9F, -1.4F, 1F, 1F, 2F, new CubeDeformation(0F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0.8F, -1.4F, -2.9F, -0.1396F, 0F, 0.0873F));
        PartDefinition p_charm_2 = p_body.addOrReplaceChild("charm_2", CubeListBuilder.create()
                .texOffs(95, 85).addBox(-0.5F, -0.1F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.35F, -0.1F, -0.35F))
                .texOffs(7, 90).addBox(-0.5F, 2.55F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(0, 85).addBox(-1F, 3.2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(3.2F, -1.4F, -2.9F, -0.1396F, 0F, -0.0873F));
        PartDefinition p_chest = p_body.addOrReplaceChild("chest", CubeListBuilder.create()
                .texOffs(55, 0).addBox(-4.5F, -9.5F, -3F, 9F, 10F, 6F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offsetAndRotation(0F, -8.2F, 0F, 0.2443F, 0F, 0F));
        PartDefinition p_mantle = p_chest.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(164, 0).addBox(-6.5F, -1F, -3.9F, 13F, 5F, 8F, new CubeDeformation(-0.3F, -0.2F, -0.2F))
                .texOffs(17, 61).addBox(-5.5F, -1.6F, -3.4F, 11F, 1F, 7F, new CubeDeformation(-0.1F, 0F, -0.4F)),
                PartPose.offset(0F, -9.4F, 0F));
        PartDefinition p_mantle_rag_0 = p_mantle.addOrReplaceChild("mantle_rag_0", CubeListBuilder.create()
                .texOffs(143, 79).addBox(-1.5F, -0.2F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-4.4F, 3.8F, -3.5F, -0.1047F, 0F, 0F));
        PartDefinition p_mantle_rag_1 = p_mantle.addOrReplaceChild("mantle_rag_1", CubeListBuilder.create()
                .texOffs(169, 71).addBox(-1.5F, -0.2F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.1F)),
                PartPose.offsetAndRotation(-1.6F, 3.8F, -3.5F, -0.1047F, 0F, 0F));
        PartDefinition p_mantle_rag_2 = p_mantle.addOrReplaceChild("mantle_rag_2", CubeListBuilder.create()
                .texOffs(152, 79).addBox(-1.5F, -0.1F, -0.5F, 3F, 4F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F)),
                PartPose.offsetAndRotation(1.4F, 3.8F, -3.5F, -0.1047F, 0F, 0F));
        PartDefinition p_mantle_rag_3 = p_mantle.addOrReplaceChild("mantle_rag_3", CubeListBuilder.create()
                .texOffs(178, 71).addBox(-1.5F, -0.3F, -0.5F, 3F, 5F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(4.2F, 3.8F, -3.5F, -0.1047F, 0F, 0F));
        PartDefinition p_mantle_side_0 = p_mantle.addOrReplaceChild("mantle_side_0", CubeListBuilder.create()
                .texOffs(206, 47).addBox(-1F, 0F, -3F, 2F, 3F, 6F, new CubeDeformation(-0.2F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-5.4F, 3.8F, 0F, 0F, 0F, -0.1396F));
        PartDefinition p_mantle_side_1 = p_mantle.addOrReplaceChild("mantle_side_1", CubeListBuilder.create()
                .texOffs(189, 47).addBox(-1F, -0.3F, -3F, 2F, 4F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(5.4F, 3.8F, 0F, 0F, 0F, 0.1396F));
        PartDefinition p_cloak = p_chest.addOrReplaceChild("cloak", CubeListBuilder.create()
                .texOffs(139, 0).addBox(-5.5F, 0F, -0.15F, 11F, 13F, 1F, new CubeDeformation(-0.3F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, -8.6F, 3.4F, -0.2618F, 0F, 0F));
        PartDefinition p_cloak_tip = p_cloak.addOrReplaceChild("cloak_tip", CubeListBuilder.create()
                .texOffs(96, 47).addBox(-5F, 0F, -0.2F, 10F, 10F, 1F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, 13F, 0F, 0.0698F, 0F, 0F));
        PartDefinition p_head = p_chest.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(119, 47).addBox(-3F, -6.6F, -3.4F, 6F, 6F, 5F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(57, 47).addBox(-4.5F, -10.1F, -5.1F, 9F, 2F, 10F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(9, 0).addBox(3.6F, -8.5F, -5.1F, 1F, 9F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(160, 47).addBox(3.75F, -9F, -5.65F, 1F, 10F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(32, 0).addBox(-4.6F, -8.5F, -5.1F, 1F, 9F, 10F, new CubeDeformation(-0.1F, -0.1F, -0.3F))
                .texOffs(165, 47).addBox(-4.75F, -9F, -5.65F, 1F, 10F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F))
                .texOffs(170, 47).addBox(-4F, -8.5F, 3.7F, 8F, 9F, 1F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(145, 85).addBox(-5F, -10.3F, -5.65F, 10F, 2F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, -9.6F, -1.2F, -0.2793F, 0F, 0F));
        PartDefinition p_snout = p_head.addOrReplaceChild("snout", CubeListBuilder.create()
                .texOffs(83, 61).addBox(-1.5F, -0.15F, -4.5F, 3F, 3F, 5F, new CubeDeformation(0F, -0.15F, -0.3F))
                .texOffs(220, 71).addBox(-1.5F, -0.2F, -5.85F, 3F, 3F, 2F, new CubeDeformation(-0.35F, -0.4F, -0.25F)),
                PartPose.offsetAndRotation(0F, -4F, -3.4F, 0.6283F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(78, 71).addBox(-1.5F, -0.05F, -4.8F, 3F, 1F, 5F, new CubeDeformation(-0.2F, -0.05F, 0F)),
                PartPose.offsetAndRotation(0F, -1.2F, -2.6F, 0.8029F, 0F, 0F));
        PartDefinition p_hood_peak = p_head.addOrReplaceChild("hood_peak", CubeListBuilder.create()
                .texOffs(192, 71).addBox(-2F, -1.55F, -2.6F, 4F, 2F, 3F, new CubeDeformation(-0.2F, -0.35F, 0F)),
                PartPose.offsetAndRotation(0F, -9.8F, -4.4F, 0.4887F, 0F, 0F));
        PartDefinition p_hood_tail = p_head.addOrReplaceChild("hood_tail", CubeListBuilder.create()
                .texOffs(100, 61).addBox(-2.5F, -2.6F, -0.4F, 5F, 3F, 4F, new CubeDeformation(-0.3F, -0.4F, -0.4F))
                .texOffs(207, 71).addBox(-1.5F, -1.8F, 2.6F, 3F, 2F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, -9F, 3.8F, -1.0821F, 0F, 0F));
        PartDefinition p_horn_l_0 = p_head.addOrReplaceChild("horn_l_0", CubeListBuilder.create()
                .texOffs(170, 61).addBox(-1.5F, -1.5F, -0.325F, 3F, 3F, 4F, new CubeDeformation(0F, 0F, -0.325F)),
                PartPose.offsetAndRotation(4.2F, -7.6F, -2.2F, 0.8029F, 0.4887F, 0F));
        PartDefinition p_horn_l_1 = p_horn_l_0.addOrReplaceChild("horn_l_1", CubeListBuilder.create()
                .texOffs(185, 61).addBox(-1.5F, -1.5F, -0.325F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.325F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, -0.8727F, 0.1047F, 0F));
        PartDefinition p_horn_l_2 = p_horn_l_1.addOrReplaceChild("horn_l_2", CubeListBuilder.create()
                .texOffs(200, 61).addBox(-1.5F, -1.5F, -0.425F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.425F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, -0.9076F, 0.1047F, 0F));
        PartDefinition p_horn_l_3 = p_horn_l_2.addOrReplaceChild("horn_l_3", CubeListBuilder.create()
                .texOffs(95, 71).addBox(-1.5F, -1.5F, -0.025F, 3F, 3F, 3F, new CubeDeformation(-0.35F, -0.35F, -0.025F)),
                PartPose.offsetAndRotation(0F, 0F, 2.8F, -0.9076F, 0.1047F, 0F));
        PartDefinition p_horn_l_4 = p_horn_l_3.addOrReplaceChild("horn_l_4", CubeListBuilder.create()
                .texOffs(231, 71).addBox(-1F, -1F, -0.125F, 2F, 2F, 3F, new CubeDeformation(0F, 0F, -0.125F)),
                PartPose.offsetAndRotation(0F, 0F, 2.6F, -0.8727F, 0.1396F, 0F));
        PartDefinition p_horn_l_5 = p_horn_l_4.addOrReplaceChild("horn_l_5", CubeListBuilder.create()
                .texOffs(242, 71).addBox(-1F, -1F, -0.225F, 2F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.225F)),
                PartPose.offsetAndRotation(0F, 0F, 2.4F, -0.8029F, 0.1396F, 0F));
        PartDefinition p_horn_l_6 = p_horn_l_5.addOrReplaceChild("horn_l_6", CubeListBuilder.create()
                .texOffs(0, 79).addBox(-1F, -1F, -0.375F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.375F)),
                PartPose.offsetAndRotation(0F, 0F, 2.2F, -0.6981F, 0.1745F, 0F));
        PartDefinition p_horn_r_0 = p_head.addOrReplaceChild("horn_r_0", CubeListBuilder.create()
                .texOffs(215, 61).addBox(-1.5F, -1.5F, -0.325F, 3F, 3F, 4F, new CubeDeformation(0F, 0F, -0.325F)),
                PartPose.offsetAndRotation(-4.2F, -7.6F, -2.2F, 0.8029F, -0.4887F, 0F));
        PartDefinition p_horn_r_1 = p_horn_r_0.addOrReplaceChild("horn_r_1", CubeListBuilder.create()
                .texOffs(230, 61).addBox(-1.5F, -1.5F, -0.325F, 3F, 3F, 4F, new CubeDeformation(-0.1F, -0.1F, -0.325F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, -0.8727F, -0.1047F, 0F));
        PartDefinition p_horn_r_2 = p_horn_r_1.addOrReplaceChild("horn_r_2", CubeListBuilder.create()
                .texOffs(0, 71).addBox(-1.5F, -1.5F, -0.425F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.425F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, -0.9076F, -0.1047F, 0F));
        PartDefinition p_horn_r_3 = p_horn_r_2.addOrReplaceChild("horn_r_3", CubeListBuilder.create()
                .texOffs(108, 71).addBox(-1.5F, -1.5F, -0.025F, 3F, 3F, 3F, new CubeDeformation(-0.35F, -0.35F, -0.025F)),
                PartPose.offsetAndRotation(0F, 0F, 2.8F, -0.9076F, -0.1047F, 0F));
        PartDefinition p_horn_r_4 = p_horn_r_3.addOrReplaceChild("horn_r_4", CubeListBuilder.create()
                .texOffs(11, 79).addBox(-1F, -1F, -0.125F, 2F, 2F, 3F, new CubeDeformation(0F, 0F, -0.125F)),
                PartPose.offsetAndRotation(0F, 0F, 2.6F, -0.8727F, -0.1396F, 0F));
        PartDefinition p_horn_r_5 = p_horn_r_4.addOrReplaceChild("horn_r_5", CubeListBuilder.create()
                .texOffs(22, 79).addBox(-1F, -1F, -0.225F, 2F, 2F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.225F)),
                PartPose.offsetAndRotation(0F, 0F, 2.4F, -0.8029F, -0.1396F, 0F));
        PartDefinition p_horn_r_6 = p_horn_r_5.addOrReplaceChild("horn_r_6", CubeListBuilder.create()
                .texOffs(33, 79).addBox(-1F, -1F, -0.375F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.375F)),
                PartPose.offsetAndRotation(0F, 0F, 2.2F, -0.6981F, -0.1745F, 0F));
        PartDefinition p_left_arm = p_chest.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(31, 47).addBox(-1.5F, -1F, -1.5F, 3F, 10F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(5.2F, -8.2F, 0F, -0.4538F, 0F, -0.1047F));
        PartDefinition p_left_arm_fore = p_left_arm.addOrReplaceChild("left_arm_fore", CubeListBuilder.create()
                .texOffs(142, 47).addBox(-1F, -0.2F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(223, 47).addBox(-2F, -0.6F, -2F, 4F, 5F, 4F)
                .texOffs(121, 71).addBox(-1.5F, 8F, -1.6F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(100, 85).addBox(-0.5F, 10.3F, -1.45F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(105, 85).addBox(-0.5F, 10.1F, -0.55F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(110, 85).addBox(-0.5F, 10.3F, 0.35F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(239, 85).addBox(-1.4F, 9.2F, -1.8F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8.8F, 0F, -0.2793F, 0F, 0F));
        PartDefinition p_left_arm_rag = p_left_arm_fore.addOrReplaceChild("left_arm_rag", CubeListBuilder.create()
                .texOffs(147, 71).addBox(-2F, -0.2F, -0.55F, 4F, 5F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 4.4F, 1.9F, 0.1745F, 0F, 0F));
        PartDefinition p_hand_bell = p_left_arm_fore.addOrReplaceChild("hand_bell", CubeListBuilder.create()
                .texOffs(115, 85).addBox(-0.5F, -0.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(220, 79).addBox(-1.5F, 1.95F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.2F, -0.05F, -0.2F))
                .texOffs(153, 61).addBox(-2F, 2.7F, -2F, 4F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(15, 71).addBox(-2.5F, 5.45F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.2F, -0.05F, -0.2F))
                .texOffs(244, 85).addBox(-0.5F, 5.4F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.05F, -0.4F, -0.05F)),
                PartPose.offsetAndRotation(0F, 10.6F, -0.4F, 0.3463F, -0.07F, 0.0779F));
        PartDefinition p_right_arm = p_chest.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(44, 47).addBox(-1.5F, -1F, -1.5F, 3F, 10F, 3F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offsetAndRotation(-5.2F, -8.2F, 0F, -0.2793F, 0F, 0.3142F));
        PartDefinition p_right_arm_fore = p_right_arm.addOrReplaceChild("right_arm_fore", CubeListBuilder.create()
                .texOffs(151, 47).addBox(-1F, -0.2F, -1F, 2F, 9F, 2F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(240, 47).addBox(-2F, -0.6F, -2F, 4F, 5F, 4F)
                .texOffs(134, 71).addBox(-1.5F, 8F, -1.6F, 3F, 3F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.2F))
                .texOffs(120, 85).addBox(-0.5F, 10.3F, -1.45F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(125, 85).addBox(-0.5F, 10.1F, -0.55F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(130, 85).addBox(-0.5F, 10.3F, 0.35F, 1F, 3F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F))
                .texOffs(249, 85).addBox(0.4F, 9.2F, -1.8F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 8.8F, 0F, -0.7679F, 0F, 0F));
        PartDefinition p_right_arm_rag = p_right_arm_fore.addOrReplaceChild("right_arm_rag", CubeListBuilder.create()
                .texOffs(158, 71).addBox(-2F, -0.2F, -0.55F, 4F, 5F, 1F, new CubeDeformation(-0.3F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, 4.4F, 1.9F, 0.1745F, 0F, 0F));
        PartDefinition p_crook = p_right_arm_fore.addOrReplaceChild("crook", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -27F, -1F, 2F, 44F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(9, 85).addBox(-1F, -4.2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(161, 79).addBox(-1F, -1.3F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(18, 85).addBox(-1F, 15.5F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(27, 85).addBox(-1F, -28.5F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.1F, -0.05F)),
                PartPose.offsetAndRotation(0F, 9.6F, -0.2F, 0.6412F, 0.2709F, -0.1611F));
        PartDefinition p_hook_0 = p_crook.addOrReplaceChild("hook_0", CubeListBuilder.create()
                .texOffs(170, 79).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offsetAndRotation(0F, -28F, 0F, 0.2793F, 0F, 0F));
        PartDefinition p_hook_1 = p_hook_0.addOrReplaceChild("hook_1", CubeListBuilder.create()
                .texOffs(179, 79).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -2.6F, 0F, 0.6981F, 0F, 0F));
        PartDefinition p_hook_2 = p_hook_1.addOrReplaceChild("hook_2", CubeListBuilder.create()
                .texOffs(188, 79).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -2.6F, 0F, 0.8029F, 0F, 0F));
        PartDefinition p_hook_3 = p_hook_2.addOrReplaceChild("hook_3", CubeListBuilder.create()
                .texOffs(197, 79).addBox(-1F, -2.75F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, -0.05F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.6F, 0F, 0.8029F, 0F, 0F));
        PartDefinition p_hook_4 = p_hook_3.addOrReplaceChild("hook_4", CubeListBuilder.create()
                .texOffs(206, 79).addBox(-1F, -2.7F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.45F, -0.1F, -0.45F)),
                PartPose.offsetAndRotation(0F, -2.5F, 0F, 0.733F, 0F, 0F));
        PartDefinition p_hook_5 = p_hook_4.addOrReplaceChild("hook_5", CubeListBuilder.create()
                .texOffs(135, 85).addBox(-0.5F, -2.5F, -0.5F, 1F, 3F, 1F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offsetAndRotation(0F, -2.4F, 0F, 0.5934F, 0F, 0F));
        PartDefinition p_lantern = p_hook_5.addOrReplaceChild("lantern", CubeListBuilder.create()
                .texOffs(140, 85).addBox(-0.5F, -0.3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.3F, -0.3F, -0.3F))
                .texOffs(186, 85).addBox(-1F, 2.1F, -1F, 2F, 1F, 2F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(36, 71).addBox(-2.5F, 3F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(0, 61).addBox(-2F, 3.7F, -2F, 4F, 5F, 4F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(57, 71).addBox(-2.5F, 8.4F, -2.5F, 5F, 1F, 5F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(195, 85).addBox(-1F, 9.3F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, -0.1F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2F, 0F, -3.9095F, 0F, 0F));
        PartDefinition p_wisp_0 = root.addOrReplaceChild("wisp_0", CubeListBuilder.create()
                .texOffs(36, 85).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(45, 85).addBox(-1F, -1F, 0.7F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.2F))
                .texOffs(218, 85).addBox(-0.5F, -0.5F, 2.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_wisp_1 = root.addOrReplaceChild("wisp_1", CubeListBuilder.create()
                .texOffs(54, 85).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(63, 85).addBox(-1F, -1F, 0.7F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.2F))
                .texOffs(225, 85).addBox(-0.5F, -0.5F, 2.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offset(0F, -4F, 0F));
        PartDefinition p_wisp_2 = root.addOrReplaceChild("wisp_2", CubeListBuilder.create()
                .texOffs(72, 85).addBox(-1F, -1F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(81, 85).addBox(-1F, -1F, 0.7F, 2F, 2F, 2F, new CubeDeformation(-0.45F, -0.45F, -0.2F))
                .texOffs(232, 85).addBox(-0.5F, -0.5F, 2.1F, 1F, 1F, 2F, new CubeDeformation(-0.2F, -0.2F, -0.4F)),
                PartPose.offset(0F, -4F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
