package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Meervolks (seit 0.93): Meerjungfrauen aller Berufe und Tritonwachen, Zubehör je Beruf; sechs Gestalten.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class MermaidGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private MermaidGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_swim = root.addOrReplaceChild("swim", CubeListBuilder.create(),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_torso = p_swim.addOrReplaceChild("torso", CubeListBuilder.create()
                .texOffs(68, 41).addBox(-3F, -9.5F, -2F, 6F, 5F, 4F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(110, 41).addBox(-2.5F, -4.75F, -1.5F, 5F, 5F, 3F, new CubeDeformation(-0.1F, -0.25F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_top = p_torso.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(66, 67).addBox(-3.075F, -8.8F, -2.4F, 3F, 3F, 1F, new CubeDeformation(-0.125F, -0.2F, -0.15F))
                .texOffs(75, 67).addBox(0.075F, -8.8F, -2.4F, 3F, 3F, 1F, new CubeDeformation(-0.125F, -0.2F, -0.15F))
                .texOffs(43, 60).addBox(-3.5F, -7.8F, -2F, 7F, 1F, 4F, new CubeDeformation(-0.35F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_necklace = p_torso.addOrReplaceChild("necklace", CubeListBuilder.create()
                .texOffs(95, 73).addBox(-2.5F, -9.85F, -2.3F, 5F, 1F, 1F, new CubeDeformation(-0.4F, -0.25F, -0.35F))
                .texOffs(69, 73).addBox(-1F, -9.75F, -2.35F, 2F, 2F, 1F, new CubeDeformation(-0.45F, -0.45F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_torso.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(5, 0).addBox(-3.5F, -6.9F, -3.5F, 7F, 7F, 7F, new CubeDeformation(-0.1F, -0.1F, -0.2F))
                .texOffs(69, 29).addBox(-4F, -7.65F, -3.95F, 8F, 2F, 8F, new CubeDeformation(-0.35F, -0.35F, -0.45F))
                .texOffs(22, 73).addBox(-4F, -6.55F, -3.9F, 8F, 2F, 1F, new CubeDeformation(-0.4F, -0.15F, -0.25F))
                .texOffs(21, 29).addBox(-3.9F, -6.7F, -2.65F, 1F, 5F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.05F))
                .texOffs(36, 29).addBox(2.9F, -6.7F, -2.65F, 1F, 5F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.05F)),
                PartPose.offset(0F, -9.5F, 0F));
        PartDefinition p_ear_l = p_head.addOrReplaceChild("ear_l", CubeListBuilder.create()
                .texOffs(43, 52).addBox(0F, -2F, -0.4F, 0F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(3.3F, -3.6F, 0.2F, 0F, 0.4887F, 0F));
        PartDefinition p_locks_l = p_head.addOrReplaceChild("locks_l", CubeListBuilder.create()
                .texOffs(51, 29).addBox(-1F, 0F, -0.95F, 2F, 9F, 2F, new CubeDeformation(-0.45F, 0F, -0.05F)),
                PartPose.offsetAndRotation(3.15F, -5.6F, -1.4F, -0.1047F, 0F, -0.0698F));
        PartDefinition p_ear_r = p_head.addOrReplaceChild("ear_r", CubeListBuilder.create()
                .texOffs(50, 52).addBox(0F, -2F, -0.4F, 0F, 4F, 3F, new CubeDeformation(0F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(-3.3F, -3.6F, 0.2F, 0F, -0.4887F, 0F));
        PartDefinition p_locks_r = p_head.addOrReplaceChild("locks_r", CubeListBuilder.create()
                .texOffs(60, 29).addBox(-1F, 0F, -0.95F, 2F, 9F, 2F, new CubeDeformation(-0.45F, 0F, -0.05F)),
                PartPose.offsetAndRotation(-3.15F, -5.6F, -1.4F, -0.1047F, 0F, 0.0698F));
        PartDefinition p_hair_back = p_head.addOrReplaceChild("hair_back", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-4F, 0F, -0.8F, 8F, 9F, 2F, new CubeDeformation(-0.4F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -6.2F, 3F, 0.1396F, 0F, 0F));
        PartDefinition p_hair_tip = p_hair_back.addOrReplaceChild("hair_tip", CubeListBuilder.create()
                .texOffs(102, 29).addBox(-3.5F, -0.25F, -0.85F, 7F, 8F, 2F, new CubeDeformation(-0.3F, -0.25F, -0.45F)),
                PartPose.offsetAndRotation(0F, 9F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_diver_band = p_head.addOrReplaceChild("diver_band", CubeListBuilder.create()
                .texOffs(35, 41).addBox(-4F, -7.3F, -4F, 8F, 1F, 8F, new CubeDeformation(-0.2F, -0.15F, -0.2F))
                .texOffs(76, 73).addBox(-1F, -7.85F, -4.3F, 2F, 2F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_coral_crown = p_head.addOrReplaceChild("coral_crown", CubeListBuilder.create(),
                PartPose.offset(0F, -7.3F, -1F));
        PartDefinition p_coral_0 = p_coral_crown.addOrReplaceChild("coral_0", CubeListBuilder.create()
                .texOffs(7, 73).addBox(-0.5F, -2.8F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.1F))
                .texOffs(121, 73).addBox(0.05F, -1.84F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-2.2F, 0F, 0F, -0.1396F, 0F, -0.4189F));
        PartDefinition p_coral_1 = p_coral_crown.addOrReplaceChild("coral_1", CubeListBuilder.create()
                .texOffs(20, 67).addBox(-0.5F, -3.7F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(0, 78).addBox(-1.05F, -2.36F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(-0.6F, 0F, 0F, -0.1396F, 0F, -0.1047F));
        PartDefinition p_coral_2 = p_coral_crown.addOrReplaceChild("coral_2", CubeListBuilder.create()
                .texOffs(12, 73).addBox(-0.5F, -3F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(5, 78).addBox(0.05F, -2.1F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(1F, 0F, 0F, -0.1396F, 0F, 0.1745F));
        PartDefinition p_coral_3 = p_coral_crown.addOrReplaceChild("coral_3", CubeListBuilder.create()
                .texOffs(17, 73).addBox(-0.5F, -2.7F, -0.5F, 1F, 3F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.1F))
                .texOffs(10, 78).addBox(-1.05F, -1.71F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.15F, -0.15F)),
                PartPose.offsetAndRotation(2.4F, 0F, 0F, -0.1396F, 0F, 0.4538F));
        PartDefinition p_flower = p_head.addOrReplaceChild("flower", CubeListBuilder.create()
                .texOffs(117, 52).addBox(-1.3F, -1.5F, -1.5F, 2F, 3F, 3F, new CubeDeformation(-0.4F, -0.3F, -0.3F)),
                PartPose.offset(-3.7F, -5F, 0.4F));
        PartDefinition p_shell_comb = p_head.addOrReplaceChild("shell_comb", CubeListBuilder.create()
                .texOffs(84, 67).addBox(-1.5F, -2.5F, -0.5F, 3F, 3F, 1F, new CubeDeformation(-0.1F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(2.8F, -6.6F, 1.4F, 0F, 0F, 0.384F));
        PartDefinition p_starfish = p_head.addOrReplaceChild("starfish", CubeListBuilder.create()
                .texOffs(108, 60).addBox(-2F, -2F, -0.5F, 4F, 4F, 1F, new CubeDeformation(-0.4F, -0.4F, -0.35F)),
                PartPose.offsetAndRotation(-2.7F, -6.5F, -2.4F, 0F, -0.3491F, -0.2443F));
        PartDefinition p_helmet = p_head.addOrReplaceChild("helmet", CubeListBuilder.create()
                .texOffs(34, 0).addBox(-4F, -7.9F, -4F, 8F, 5F, 8F, new CubeDeformation(-0.25F, -0.3F, -0.2F))
                .texOffs(41, 73).addBox(-4F, -7.8F, -4.15F, 8F, 2F, 1F, new CubeDeformation(-0.25F, -0.2F, -0.3F))
                .texOffs(0, 41).addBox(-0.5F, -10.2F, -3F, 1F, 3F, 7F, new CubeDeformation(-0.05F, -0.2F, 0F))
                .texOffs(90, 73).addBox(-0.5F, -3.4F, -4.45F, 1F, 2F, 1F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(0, 60).addBox(-4.05F, -3.6F, -2.7F, 1F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.1F))
                .texOffs(9, 60).addBox(3.05F, -3.6F, -2.7F, 1F, 3F, 3F, new CubeDeformation(-0.2F, -0.2F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_arm = p_torso.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(17, 41).addBox(-1F, -0.6F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(93, 67).addBox(-1F, 7.15F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(18, 60).addBox(0.9F, 2.9F, -0.2F, 0F, 4F, 2F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(3.75F, -8.9F, 0F));
        PartDefinition p_bracelet_l = p_left_arm.addOrReplaceChild("bracelet_l", CubeListBuilder.create()
                .texOffs(40, 67).addBox(-1.5F, 5.45F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.45F, -0.15F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pauldron_l = p_left_arm.addOrReplaceChild("pauldron_l", CubeListBuilder.create()
                .texOffs(91, 52).addBox(-1.5F, -1.65F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -0.6F, 0F, 0F, 0F, -0.2094F));
        PartDefinition p_right_arm = p_torso.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(26, 41).addBox(-1F, -0.6F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(102, 67).addBox(-1F, 7.15F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.05F, 0F))
                .texOffs(23, 60).addBox(-0.9F, 2.9F, -0.2F, 0F, 4F, 2F, new CubeDeformation(0F, -0.3F, 0F)),
                PartPose.offset(-3.75F, -8.9F, 0F));
        PartDefinition p_bracelet_r = p_right_arm.addOrReplaceChild("bracelet_r", CubeListBuilder.create()
                .texOffs(53, 67).addBox(-1.5F, 5.45F, -1.5F, 3F, 1F, 3F, new CubeDeformation(-0.45F, -0.15F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_pauldron_r = p_right_arm.addOrReplaceChild("pauldron_r", CubeListBuilder.create()
                .texOffs(104, 52).addBox(-1.5F, -1.65F, -1.5F, 3F, 3F, 3F, new CubeDeformation(0F, -0.35F, -0.1F)),
                PartPose.offsetAndRotation(0F, -0.6F, 0F, 0F, 0F, 0.2094F));
        PartDefinition p_trident = p_right_arm.addOrReplaceChild("trident", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-0.5F, -15F, -0.5F, 1F, 27F, 1F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(108, 73).addBox(-2.5F, -15.85F, -0.5F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.05F, -0.05F))
                .texOffs(28, 60).addBox(-0.5F, -20.6F, -0.5F, 1F, 5F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(25, 67).addBox(-2.5F, -19.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.45F, -0.1F))
                .texOffs(30, 67).addBox(1.5F, -19.25F, -0.5F, 1F, 4F, 1F, new CubeDeformation(-0.1F, -0.45F, -0.1F)),
                PartPose.offset(0F, 8.2F, -0.1F));
        PartDefinition p_armor = p_torso.addOrReplaceChild("armor", CubeListBuilder.create()
                .texOffs(67, 0).addBox(-3.5F, -10.15F, -2.5F, 7F, 7F, 5F, new CubeDeformation(-0.2F, -0.35F, -0.45F))
                .texOffs(66, 60).addBox(-3F, -3.6F, -2F, 6F, 1F, 4F, new CubeDeformation(-0.1F, 0F, -0.25F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail1 = p_swim.addOrReplaceChild("tail1", CubeListBuilder.create()
                .texOffs(89, 41).addBox(-3F, 0F, -2F, 6F, 4F, 4F, new CubeDeformation(-0.4F, 0F, -0.45F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_veil_l = p_tail1.addOrReplaceChild("veil_l", CubeListBuilder.create()
                .texOffs(92, 0).addBox(0F, -0.25F, -1.8F, 0F, 8F, 4F, new CubeDeformation(0F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(2.55F, 0.6F, 0.2F, 0F, 0.1745F, -0.2094F));
        PartDefinition p_veil_r = p_tail1.addOrReplaceChild("veil_r", CubeListBuilder.create()
                .texOffs(101, 0).addBox(0F, -0.25F, -1.8F, 0F, 8F, 4F, new CubeDeformation(0F, -0.25F, -0.2F)),
                PartPose.offsetAndRotation(-2.55F, 0.6F, 0.2F, 0F, -0.1745F, 0.2094F));
        PartDefinition p_tail2 = p_tail1.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(0, 52).addBox(-2.5F, -0.2F, -1.5F, 5F, 4F, 3F, new CubeDeformation(-0.3F, -0.2F, -0.15F))
                .texOffs(33, 60).addBox(0F, -0.1F, 1.15F, 0F, 4F, 2F, new CubeDeformation(0F, -0.3F, -0.05F)),
                PartPose.offsetAndRotation(0F, 4F, 0F, 0.1047F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(17, 52).addBox(-2F, -0.3F, -1.5F, 4F, 4F, 3F, new CubeDeformation(-0.25F, -0.3F, -0.4F))
                .texOffs(38, 60).addBox(0F, -0.2F, 0.8F, 0F, 4F, 2F, new CubeDeformation(0F, -0.4F, -0.15F)),
                PartPose.offsetAndRotation(0F, 3.6F, 0F, 0.1745F, 0F, 0F));
        PartDefinition p_tail4 = p_tail3.addOrReplaceChild("tail4", CubeListBuilder.create()
                .texOffs(0, 67).addBox(-1.5F, 0F, -1F, 3F, 3F, 2F, new CubeDeformation(-0.25F, 0F, -0.15F)),
                PartPose.offsetAndRotation(0F, 3.4F, 0F, 0.2094F, 0F, 0F));
        PartDefinition p_fluke = p_tail4.addOrReplaceChild("fluke", CubeListBuilder.create()
                .texOffs(111, 67).addBox(-1F, -0.6F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.8F, 0.05F, 0.1396F, 0F, 0F));
        PartDefinition p_fluke_l = p_fluke.addOrReplaceChild("fluke_l", CubeListBuilder.create()
                .texOffs(57, 52).addBox(-0.25F, -0.2F, 0F, 8F, 6F, 0F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0.4F, 0.2F, 0F, 0F, 0F, 0.4887F));
        PartDefinition p_fluke_r = p_fluke.addOrReplaceChild("fluke_r", CubeListBuilder.create()
                .texOffs(74, 52).addBox(-7.75F, -0.2F, 0F, 8F, 6F, 0F, new CubeDeformation(-0.25F, -0.2F, 0F)),
                PartPose.offsetAndRotation(-0.4F, 0.2F, 0F, 0F, 0F, -0.4887F));
        PartDefinition p_pearl_net = p_tail1.addOrReplaceChild("pearl_net", CubeListBuilder.create()
                .texOffs(32, 52).addBox(-1.8F, -0.4F, -1.5F, 2F, 4F, 3F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(60, 73).addBox(-1.8F, -0.75F, -1F, 2F, 1F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(-2.6F, 0.4F, -0.4F, 0F, 0F, 0.1396F));
        PartDefinition p_tool_belt = p_tail1.addOrReplaceChild("tool_belt", CubeListBuilder.create()
                .texOffs(87, 60).addBox(-3F, 0.1F, -2F, 6F, 1F, 4F, new CubeDeformation(-0.25F, -0.1F, -0.3F))
                .texOffs(35, 67).addBox(2.4F, -0.4F, -1.2F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(120, 67).addBox(1.9F, 2.5F, -1.7F, 2F, 2F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.1F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_conch = p_tail1.addOrReplaceChild("conch", CubeListBuilder.create()
                .texOffs(11, 67).addBox(-0.25F, -0.2F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.25F, -0.2F, 0F))
                .texOffs(0, 73).addBox(0.2F, 2.3F, -1F, 1F, 2F, 2F, new CubeDeformation(0F, -0.3F, -0.4F))
                .texOffs(83, 73).addBox(0.25F, -0.85F, -1F, 1F, 1F, 2F, new CubeDeformation(-0.05F, -0.15F, -0.4F)),
                PartPose.offsetAndRotation(2.6F, 0.6F, -0.2F, 0F, 0F, -0.3491F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
