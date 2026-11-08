package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Belagerungsturms (Unterbau und zwei aufsetzbare Stockwerke: 7, 10 oder 13 Blöcke bis zur Plattform; vorn Häute, seitlich Bretter und Streben, hinten die Leiter; Fallbrücke hochgezogen gebaut, dreht um X nach vorn; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SiegeTowerGeometry {
    public static final int TEXTURE_WIDTH = 1024;
    public static final int TEXTURE_HEIGHT = 1024;

    private SiegeTowerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(906, 0).addBox(18F, -10.25F, -25F, 4F, 4F, 50F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(959, 269).addBox(18.5F, -6.5F, -17F, 3F, 3F, 4F)
                .texOffs(974, 269).addBox(18.5F, -6.5F, 13F, 3F, 3F, 4F)
                .texOffs(0, 98).addBox(-22F, -10.25F, -25F, 4F, 4F, 50F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(989, 269).addBox(-21.5F, -6.5F, -17F, 3F, 3F, 4F)
                .texOffs(1004, 269).addBox(-21.5F, -6.5F, 13F, 3F, 3F, 4F)
                .texOffs(918, 243).addBox(-22F, -10.25F, -25.25F, 44F, 4F, 4F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(0, 269).addBox(-22F, -10.25F, 21.25F, 44F, 4F, 4F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(411, 278).addBox(-23F, -7F, -16F, 46F, 2F, 2F)
                .texOffs(508, 278).addBox(-23F, -7F, 14F, 46F, 2F, 2F)
                .texOffs(784, 0).addBox(19F, -64F, -22F, 3F, 54F, 3F)
                .texOffs(797, 0).addBox(19F, -64F, 19F, 3F, 54F, 3F)
                .texOffs(810, 0).addBox(19F, -64F, -1.5F, 3F, 54F, 3F)
                .texOffs(823, 0).addBox(-22F, -64F, -22F, 3F, 54F, 3F)
                .texOffs(836, 0).addBox(-22F, -64F, 19F, 3F, 54F, 3F)
                .texOffs(849, 0).addBox(-22F, -64F, -1.5F, 3F, 54F, 3F)
                .texOffs(862, 0).addBox(-9.5F, -64F, 19F, 3F, 54F, 3F)
                .texOffs(875, 0).addBox(6.5F, -64F, 19F, 3F, 54F, 3F)
                .texOffs(726, 98).addBox(19F, -64.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(821, 98).addBox(-22F, -64.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(295, 269).addBox(-19F, -64.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(378, 269).addBox(-19F, -64.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(654, 153).addBox(-19F, -64F, -19F, 38F, 2F, 38F)
                .texOffs(0, 0).addBox(21.8F, -64F, -21.5F, 1F, 54F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(589, 243).addBox(22.15F, -41F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(596, 243).addBox(22.15F, -41F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(89, 0).addBox(-22.8F, -64F, -21.5F, 1F, 54F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(603, 243).addBox(-23.15F, -41F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(610, 243).addBox(-23.15F, -41F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(315, 202).addBox(-0.5F, -38.6F, -23.3F, 23F, 29F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(364, 202).addBox(-22.5F, -38.6F, -22.8F, 23F, 29F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(413, 202).addBox(-0.5F, -64F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(462, 202).addBox(-22.5F, -64F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(818, 278).addBox(-22.5F, -38.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(911, 278).addBox(-22.5F, -64.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(888, 0).addBox(-6F, -64F, 17.5F, 2F, 54F, 2F)
                .texOffs(897, 0).addBox(4F, -64F, 17.5F, 2F, 54F, 2F)
                .texOffs(558, 285).addBox(-4F, -13F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(579, 285).addBox(-4F, -19F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(600, 285).addBox(-4F, -25F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(621, 285).addBox(-4F, -31F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(642, 285).addBox(-4F, -37F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(663, 285).addBox(-4F, -43F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(684, 285).addBox(-4F, -49F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(705, 285).addBox(-4F, -55F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(726, 285).addBox(-4F, -61F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(172, 98).addBox(19F, -112F, -22F, 3F, 48F, 3F)
                .texOffs(185, 98).addBox(19F, -112F, 19F, 3F, 48F, 3F)
                .texOffs(198, 98).addBox(19F, -112F, -1.5F, 3F, 48F, 3F)
                .texOffs(211, 98).addBox(-22F, -112F, -22F, 3F, 48F, 3F)
                .texOffs(224, 98).addBox(-22F, -112F, 19F, 3F, 48F, 3F)
                .texOffs(237, 98).addBox(-22F, -112F, -1.5F, 3F, 48F, 3F)
                .texOffs(250, 98).addBox(-9.5F, -112F, 19F, 3F, 48F, 3F)
                .texOffs(263, 98).addBox(6.5F, -112F, 19F, 3F, 48F, 3F)
                .texOffs(916, 98).addBox(19F, -112.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 153).addBox(-22F, -112.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(461, 269).addBox(-19F, -112.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(544, 269).addBox(-19F, -112.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(807, 153).addBox(-19F, -112F, -19F, 38F, 2F, 38F)
                .texOffs(178, 0).addBox(21.8F, -112F, -21.5F, 1F, 48F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(617, 243).addBox(22.15F, -92F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(624, 243).addBox(22.15F, -92F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(267, 0).addBox(-22.8F, -112F, -21.5F, 1F, 48F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(631, 243).addBox(-23.15F, -92F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(638, 243).addBox(-23.15F, -92F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(511, 202).addBox(-0.5F, -89.6F, -22.8F, 23F, 26F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(560, 202).addBox(-22.5F, -89.6F, -23.3F, 23F, 26F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(805, 202).addBox(-0.5F, -112F, -23.3F, 23F, 24F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(854, 202).addBox(-22.5F, -112F, -22.8F, 23F, 24F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(0, 285).addBox(-22.5F, -89.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(93, 285).addBox(-22.5F, -112.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(672, 98).addBox(-6F, -112F, 17.5F, 2F, 48F, 2F)
                .texOffs(681, 98).addBox(4F, -112F, 17.5F, 2F, 48F, 2F)
                .texOffs(747, 285).addBox(-4F, -67F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(768, 285).addBox(-4F, -73F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(789, 285).addBox(-4F, -79F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(810, 285).addBox(-4F, -85F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(831, 285).addBox(-4F, -91F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(852, 285).addBox(-4F, -97F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(873, 285).addBox(-4F, -103F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(894, 285).addBox(-4F, -109F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_brace_l0 = p_frame.addOrReplaceChild("brace_l0", CubeListBuilder.create()
                .texOffs(712, 0).addBox(-1F, -0.5994F, -1F, 2F, 64F, 2F, new CubeDeformation(0F, -0.0994F, 0F)),
                PartPose.offsetAndRotation(23.3F, -11F, -19F, 2.4917F, 0F, 0F));
        PartDefinition p_brace_r0 = p_frame.addOrReplaceChild("brace_r0", CubeListBuilder.create()
                .texOffs(721, 0).addBox(-1F, -0.5994F, -1F, 2F, 64F, 2F, new CubeDeformation(0F, -0.0994F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -11F, -19F, 2.4917F, 0F, 0F));
        PartDefinition p_brace_l1 = p_frame.addOrReplaceChild("brace_l1", CubeListBuilder.create()
                .texOffs(730, 0).addBox(-1F, -0.9311F, -1F, 2F, 60F, 2F, new CubeDeformation(0F, -0.4311F, 0F)),
                PartPose.offsetAndRotation(23.3F, -65F, 19F, -2.4292F, 0F, 0F));
        PartDefinition p_brace_r1 = p_frame.addOrReplaceChild("brace_r1", CubeListBuilder.create()
                .texOffs(739, 0).addBox(-1F, -0.9311F, -1F, 2F, 60F, 2F, new CubeDeformation(0F, -0.4311F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -65F, 19F, -2.4292F, 0F, 0F));
        PartDefinition p_storey_a = p_frame.addOrReplaceChild("storey_a", CubeListBuilder.create()
                .texOffs(276, 98).addBox(19F, -48F, -22F, 3F, 48F, 3F)
                .texOffs(289, 98).addBox(19F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(302, 98).addBox(19F, -48F, -1.5F, 3F, 48F, 3F)
                .texOffs(315, 98).addBox(-22F, -48F, -22F, 3F, 48F, 3F)
                .texOffs(328, 98).addBox(-22F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(341, 98).addBox(-22F, -48F, -1.5F, 3F, 48F, 3F)
                .texOffs(354, 98).addBox(-9.5F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(367, 98).addBox(6.5F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(95, 153).addBox(19F, -48.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(190, 153).addBox(-22F, -48.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(627, 269).addBox(-19F, -48.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(710, 269).addBox(-19F, -48.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 202).addBox(-19F, -48F, -19F, 38F, 2F, 38F)
                .texOffs(356, 0).addBox(21.8F, -48F, -21.5F, 1F, 48F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(645, 243).addBox(22.15F, -28F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(652, 243).addBox(22.15F, -28F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(445, 0).addBox(-22.8F, -48F, -21.5F, 1F, 48F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(659, 243).addBox(-23.15F, -28F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(666, 243).addBox(-23.15F, -28F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(609, 202).addBox(-0.5F, -25.6F, -23.3F, 23F, 26F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(658, 202).addBox(-22.5F, -25.6F, -22.8F, 23F, 26F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(903, 202).addBox(-0.5F, -48F, -22.8F, 23F, 24F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(952, 202).addBox(-22.5F, -48F, -23.3F, 23F, 24F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(186, 285).addBox(-22.5F, -25.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(279, 285).addBox(-22.5F, -48.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(690, 98).addBox(-6F, -48F, 17.5F, 2F, 48F, 2F)
                .texOffs(699, 98).addBox(4F, -48F, 17.5F, 2F, 48F, 2F)
                .texOffs(915, 285).addBox(-4F, -3F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(936, 285).addBox(-4F, -9F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(957, 285).addBox(-4F, -15F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(978, 285).addBox(-4F, -21F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(999, 285).addBox(-4F, -27F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(0, 289).addBox(-4F, -33F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(21, 289).addBox(-4F, -39F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(42, 289).addBox(-4F, -45F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, -112F, 0F));
        PartDefinition p_brace_l2 = p_storey_a.addOrReplaceChild("brace_l2", CubeListBuilder.create()
                .texOffs(748, 0).addBox(-1F, -0.9311F, -1F, 2F, 60F, 2F, new CubeDeformation(0F, -0.4311F, 0F)),
                PartPose.offsetAndRotation(23.3F, -1F, -19F, 2.4292F, 0F, 0F));
        PartDefinition p_brace_r2 = p_storey_a.addOrReplaceChild("brace_r2", CubeListBuilder.create()
                .texOffs(757, 0).addBox(-1F, -0.9311F, -1F, 2F, 60F, 2F, new CubeDeformation(0F, -0.4311F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -1F, -19F, 2.4292F, 0F, 0F));
        PartDefinition p_storey_b = p_frame.addOrReplaceChild("storey_b", CubeListBuilder.create()
                .texOffs(380, 98).addBox(19F, -48F, -22F, 3F, 48F, 3F)
                .texOffs(393, 98).addBox(19F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(406, 98).addBox(19F, -48F, -1.5F, 3F, 48F, 3F)
                .texOffs(419, 98).addBox(-22F, -48F, -22F, 3F, 48F, 3F)
                .texOffs(432, 98).addBox(-22F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(445, 98).addBox(-22F, -48F, -1.5F, 3F, 48F, 3F)
                .texOffs(458, 98).addBox(-9.5F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(471, 98).addBox(6.5F, -48F, 19F, 3F, 48F, 3F)
                .texOffs(285, 153).addBox(19F, -48.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(380, 153).addBox(-22F, -48.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(793, 269).addBox(-19F, -48.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(876, 269).addBox(-19F, -48.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(153, 202).addBox(-19F, -48F, -19F, 38F, 2F, 38F)
                .texOffs(534, 0).addBox(21.8F, -48F, -21.5F, 1F, 48F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(673, 243).addBox(22.15F, -28F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(680, 243).addBox(22.15F, -28F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(623, 0).addBox(-22.8F, -48F, -21.5F, 1F, 48F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(687, 243).addBox(-23.15F, -28F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(694, 243).addBox(-23.15F, -28F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(707, 202).addBox(-0.5F, -25.6F, -22.8F, 23F, 26F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(756, 202).addBox(-22.5F, -25.6F, -23.3F, 23F, 26F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(0, 243).addBox(-0.5F, -48F, -23.3F, 23F, 24F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(49, 243).addBox(-22.5F, -48F, -22.8F, 23F, 24F, 1F, new CubeDeformation(-0.1F, 0F, -0.2F))
                .texOffs(372, 285).addBox(-22.5F, -25.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(465, 285).addBox(-22.5F, -48.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(708, 98).addBox(-6F, -48F, 17.5F, 2F, 48F, 2F)
                .texOffs(717, 98).addBox(4F, -48F, 17.5F, 2F, 48F, 2F)
                .texOffs(63, 289).addBox(-4F, -3F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(84, 289).addBox(-4F, -9F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(105, 289).addBox(-4F, -15F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(126, 289).addBox(-4F, -21F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(147, 289).addBox(-4F, -27F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(168, 289).addBox(-4F, -33F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(189, 289).addBox(-4F, -39F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(210, 289).addBox(-4F, -45F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, -160F, 0F));
        PartDefinition p_brace_l3 = p_storey_b.addOrReplaceChild("brace_l3", CubeListBuilder.create()
                .texOffs(766, 0).addBox(-1F, -0.9311F, -1F, 2F, 60F, 2F, new CubeDeformation(0F, -0.4311F, 0F)),
                PartPose.offsetAndRotation(23.3F, -1F, 19F, -2.4292F, 0F, 0F));
        PartDefinition p_brace_r3 = p_storey_b.addOrReplaceChild("brace_r3", CubeListBuilder.create()
                .texOffs(775, 0).addBox(-1F, -0.9311F, -1F, 2F, 60F, 2F, new CubeDeformation(0F, -0.4311F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -1F, 19F, -2.4292F, 0F, 0F));
        PartDefinition p_top = p_frame.addOrReplaceChild("top", CubeListBuilder.create()
                .texOffs(475, 153).addBox(-22F, -0.505F, -22.75F, 44F, 2F, 45F, new CubeDeformation(0F, -0.495F, -0.25F))
                .texOffs(98, 243).addBox(19F, -16F, -22F, 3F, 16F, 3F)
                .texOffs(111, 243).addBox(19F, -16F, 19F, 3F, 16F, 3F)
                .texOffs(124, 243).addBox(-22F, -16F, -22F, 3F, 16F, 3F)
                .texOffs(137, 243).addBox(-22F, -16F, 19F, 3F, 16F, 3F)
                .texOffs(484, 98).addBox(20.25F, -12F, -19F, 2F, 12F, 38F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(165, 269).addBox(20.25F, -16F, -18F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(178, 269).addBox(20.25F, -16F, -10F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(191, 269).addBox(20.25F, -16F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(204, 269).addBox(20.25F, -16F, 6F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(217, 269).addBox(20.25F, -16F, 14F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(565, 98).addBox(-22.25F, -12F, -19F, 2F, 12F, 38F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(230, 269).addBox(-22.25F, -16F, -18F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(243, 269).addBox(-22.25F, -16F, -10F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(256, 269).addBox(-22.25F, -16F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(269, 269).addBox(-22.25F, -16F, 6F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(282, 269).addBox(-22.25F, -16F, 14F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(274, 243).addBox(-19F, -12F, 20.25F, 38F, 12F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(65, 278).addBox(-18F, -16F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(78, 278).addBox(-10F, -16F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(91, 278).addBox(-2F, -16F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(104, 278).addBox(6F, -16F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(117, 278).addBox(14F, -16F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(135, 269).addBox(14.25F, -6F, -22.25F, 5F, 6F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(97, 269).addBox(18.5F, -19F, -23.25F, 4F, 3F, 5F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(776, 278).addBox(17.5F, -18.5F, -24.5F, 1F, 2F, 2F)
                .texOffs(150, 269).addBox(-19.25F, -6F, -22.25F, 5F, 6F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(116, 269).addBox(-22.5F, -19F, -23.25F, 4F, 3F, 5F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(783, 278).addBox(-18.5F, -18.5F, -24.5F, 1F, 2F, 2F)
                .texOffs(571, 243).addBox(-6F, -10F, 17.5F, 2F, 10F, 2F)
                .texOffs(580, 243).addBox(4F, -10F, 17.5F, 2F, 10F, 2F)
                .texOffs(306, 202).addBox(19.5F, -46F, 19.5F, 2F, 30F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(701, 243).addBox(20.25F, -45F, 20F, 15F, 8F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offset(0F, -208F, 0F));
        PartDefinition p_bridge = p_top.addOrReplaceChild("bridge", CubeListBuilder.create()
                .texOffs(109, 98).addBox(-14F, -48F, -2.75F, 28F, 48F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(605, 278).addBox(-13F, -8F, -4.25F, 26F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(662, 278).addBox(-13F, -24F, -4.25F, 26F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(719, 278).addBox(-13F, -40F, -4.25F, 26F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(646, 98).addBox(12.25F, -47F, 0F, 2F, 46F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(130, 278).addBox(12.75F, -13F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(141, 278).addBox(12.75F, -37F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(659, 98).addBox(-14.25F, -47F, 0F, 2F, 46F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(152, 278).addBox(-13.75F, -13F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(163, 278).addBox(-13.75F, -37F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(366, 278).addBox(-11F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(375, 278).addBox(-6F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(384, 278).addBox(-1F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(393, 278).addBox(4F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(402, 278).addBox(9F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(0, 278).addBox(-14F, -2F, -3.25F, 28F, 2F, 4F, new CubeDeformation(0F, 0F, -0.45F)),
                PartPose.offset(0F, 0F, -23F));
        PartDefinition p_wheel_fl = root.addOrReplaceChild("wheel_fl", CubeListBuilder.create()
                .texOffs(150, 243).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(355, 243).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(382, 243).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(734, 243).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(757, 243).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(246, 278).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(261, 278).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(174, 278).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(218, 278).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(790, 278).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(24F, 18F, -15F));
        PartDefinition p_wheel_fr = root.addOrReplaceChild("wheel_fr", CubeListBuilder.create()
                .texOffs(181, 243).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(409, 243).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(436, 243).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(780, 243).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(803, 243).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(276, 278).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(291, 278).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(185, 278).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(225, 278).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(797, 278).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-24F, 18F, -15F));
        PartDefinition p_wheel_bl = root.addOrReplaceChild("wheel_bl", CubeListBuilder.create()
                .texOffs(212, 243).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(463, 243).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(490, 243).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(826, 243).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(849, 243).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(306, 278).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(321, 278).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(196, 278).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(232, 278).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(804, 278).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(24F, 18F, 15F));
        PartDefinition p_wheel_br = root.addOrReplaceChild("wheel_br", CubeListBuilder.create()
                .texOffs(243, 243).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(517, 243).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(544, 243).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(872, 243).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(895, 243).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(336, 278).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(351, 278).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(207, 278).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(239, 278).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(811, 278).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-24F, 18F, 15F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
