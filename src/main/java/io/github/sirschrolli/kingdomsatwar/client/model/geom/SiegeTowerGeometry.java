package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Belagerungsturms (zehn Blöcke bis zur Plattform, vorn Häute, seitlich Bretter und Streben, hinten die Leiter; Fallbrücke hochgezogen gebaut, dreht um X nach vorn; erzeugt von tools/generate_siege.py).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SiegeTowerGeometry {
    public static final int TEXTURE_WIDTH = 1024;
    public static final int TEXTURE_HEIGHT = 512;

    private SiegeTowerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_frame = root.addOrReplaceChild("frame", CubeListBuilder.create()
                .texOffs(888, 0).addBox(18F, -10.25F, -25F, 4F, 4F, 50F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(415, 314).addBox(18.5F, -6.5F, -17F, 3F, 3F, 4F)
                .texOffs(430, 314).addBox(18.5F, -6.5F, 13F, 3F, 3F, 4F)
                .texOffs(0, 170).addBox(-22F, -10.25F, -25F, 4F, 4F, 50F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(445, 314).addBox(-21.5F, -6.5F, -17F, 3F, 3F, 4F)
                .texOffs(460, 314).addBox(-21.5F, -6.5F, 13F, 3F, 3F, 4F)
                .texOffs(315, 303).addBox(-22F, -10.25F, -25.25F, 44F, 4F, 4F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(412, 303).addBox(-22F, -10.25F, 21.25F, 44F, 4F, 4F, new CubeDeformation(0F, -0.25F, -0.25F))
                .texOffs(886, 314).addBox(-23F, -7F, -16F, 46F, 2F, 2F)
                .texOffs(0, 322).addBox(-23F, -7F, 14F, 46F, 2F, 2F)
                .texOffs(0, 0).addBox(19F, -176F, -22F, 3F, 166F, 3F)
                .texOffs(13, 0).addBox(19F, -176F, 19F, 3F, 166F, 3F)
                .texOffs(52, 0).addBox(19F, -160F, -1.5F, 3F, 150F, 3F)
                .texOffs(26, 0).addBox(-22F, -176F, -22F, 3F, 166F, 3F)
                .texOffs(39, 0).addBox(-22F, -176F, 19F, 3F, 166F, 3F)
                .texOffs(65, 0).addBox(-22F, -160F, -1.5F, 3F, 150F, 3F)
                .texOffs(78, 0).addBox(-9.5F, -160F, 19F, 3F, 150F, 3F)
                .texOffs(91, 0).addBox(6.5F, -160F, 19F, 3F, 150F, 3F)
                .texOffs(378, 170).addBox(19F, -40.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(473, 170).addBox(-22F, -40.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(707, 303).addBox(-19F, -40.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(790, 303).addBox(-19F, -40.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(369, 225).addBox(-19F, -40F, -19F, 38F, 2F, 38F)
                .texOffs(568, 170).addBox(19F, -80.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(663, 170).addBox(-22F, -80.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(873, 303).addBox(-19F, -80.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(0, 314).addBox(-19F, -80.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(522, 225).addBox(-19F, -80F, -19F, 38F, 2F, 38F)
                .texOffs(758, 170).addBox(19F, -120.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(853, 170).addBox(-22F, -120.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(83, 314).addBox(-19F, -120.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(166, 314).addBox(-19F, -120.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(675, 225).addBox(-19F, -120F, -19F, 38F, 2F, 38F)
                .texOffs(0, 225).addBox(19F, -160.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(95, 225).addBox(-22F, -160.25F, -22F, 3F, 4F, 44F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(249, 314).addBox(-19F, -160.25F, -22F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(332, 314).addBox(-19F, -160.25F, 19F, 38F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(828, 225).addBox(-19F, -160F, -19F, 38F, 2F, 38F)
                .texOffs(190, 225).addBox(-22F, -160.505F, -22.75F, 44F, 2F, 45F, new CubeDeformation(0F, -0.495F, -0.25F))
                .texOffs(0, 274).addBox(-0.5F, -36.6F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(49, 274).addBox(-22.5F, -36.6F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(98, 274).addBox(-0.5F, -61.6F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(147, 274).addBox(-22.5F, -61.6F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(196, 274).addBox(-0.5F, -86.6F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(245, 274).addBox(-22.5F, -86.6F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(294, 274).addBox(-0.5F, -111.6F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(343, 274).addBox(-22.5F, -111.6F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(392, 274).addBox(-0.5F, -136.6F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(441, 274).addBox(-22.5F, -136.6F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(490, 274).addBox(-0.5F, -161.6F, -22.8F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(539, 274).addBox(-22.5F, -161.6F, -23.3F, 23F, 27F, 1F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(310, 322).addBox(-22.5F, -36.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(403, 322).addBox(-22.5F, -61.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(496, 322).addBox(-22.5F, -86.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(589, 322).addBox(-22.5F, -111.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(682, 322).addBox(-22.5F, -136.25F, -23.9F, 45F, 2F, 1F, new CubeDeformation(0F, -0.25F, -0.2F))
                .texOffs(656, 0).addBox(21.8F, -40F, -21.5F, 1F, 30F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(1009, 274).addBox(22.15F, -29F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(1016, 274).addBox(22.15F, -29F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(122, 0).addBox(21.8F, -80F, -21.5F, 1F, 40F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(0, 303).addBox(22.15F, -64F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(7, 303).addBox(22.15F, -64F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(211, 0).addBox(21.8F, -120F, -21.5F, 1F, 40F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(14, 303).addBox(22.15F, -104F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(21, 303).addBox(22.15F, -104F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(300, 0).addBox(21.8F, -160F, -21.5F, 1F, 40F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(28, 303).addBox(22.15F, -144F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(35, 303).addBox(22.15F, -144F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(745, 0).addBox(-22.8F, -40F, -21.5F, 1F, 30F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(42, 303).addBox(-23.15F, -29F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(49, 303).addBox(-23.15F, -29F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(389, 0).addBox(-22.8F, -80F, -21.5F, 1F, 40F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(56, 303).addBox(-23.15F, -64F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(63, 303).addBox(-23.15F, -64F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(478, 0).addBox(-22.8F, -120F, -21.5F, 1F, 40F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(70, 303).addBox(-23.15F, -104F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(77, 303).addBox(-23.15F, -104F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(567, 0).addBox(-22.8F, -160F, -21.5F, 1F, 40F, 43F, new CubeDeformation(-0.2F, 0F, 0F))
                .texOffs(84, 303).addBox(-23.15F, -144F, -11F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(91, 303).addBox(-23.15F, -144F, 9F, 1F, 8F, 2F, new CubeDeformation(-0.45F, 0F, -0.25F))
                .texOffs(104, 0).addBox(-6F, -161F, 17.5F, 2F, 151F, 2F)
                .texOffs(113, 0).addBox(4F, -161F, 17.5F, 2F, 151F, 2F)
                .texOffs(775, 322).addBox(-4F, -13F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(796, 322).addBox(-4F, -19F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(817, 322).addBox(-4F, -25F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(838, 322).addBox(-4F, -31F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(859, 322).addBox(-4F, -37F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(880, 322).addBox(-4F, -43F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(901, 322).addBox(-4F, -49F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(922, 322).addBox(-4F, -55F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(943, 322).addBox(-4F, -61F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(964, 322).addBox(-4F, -67F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(985, 322).addBox(-4F, -73F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(0, 327).addBox(-4F, -79F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(21, 327).addBox(-4F, -85F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(42, 327).addBox(-4F, -91F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(63, 327).addBox(-4F, -97F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(84, 327).addBox(-4F, -103F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(105, 327).addBox(-4F, -109F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(126, 327).addBox(-4F, -115F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(147, 327).addBox(-4F, -121F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(168, 327).addBox(-4F, -127F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(189, 327).addBox(-4F, -133F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(210, 327).addBox(-4F, -139F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(231, 327).addBox(-4F, -145F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(252, 327).addBox(-4F, -151F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(273, 327).addBox(-4F, -157F, 17.5F, 8F, 1F, 2F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(172, 170).addBox(20.25F, -172F, -19F, 2F, 12F, 38F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(577, 303).addBox(20.25F, -176F, -18F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(590, 303).addBox(20.25F, -176F, -10F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(603, 303).addBox(20.25F, -176F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(616, 303).addBox(20.25F, -176F, 6F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(629, 303).addBox(20.25F, -176F, 14F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(253, 170).addBox(-22.25F, -172F, -19F, 2F, 12F, 38F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(642, 303).addBox(-22.25F, -176F, -18F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(655, 303).addBox(-22.25F, -176F, -10F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(668, 303).addBox(-22.25F, -176F, -2F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(681, 303).addBox(-22.25F, -176F, 6F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(694, 303).addBox(-22.25F, -176F, 14F, 2F, 4F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(712, 274).addBox(-19F, -172F, 20.25F, 38F, 12F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(540, 314).addBox(-18F, -176F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(553, 314).addBox(-10F, -176F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(566, 314).addBox(-2F, -176F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(579, 314).addBox(6F, -176F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(592, 314).addBox(14F, -176F, 20.25F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(547, 303).addBox(14.25F, -166F, -22.25F, 5F, 6F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(509, 303).addBox(18.5F, -179F, -23.25F, 4F, 3F, 5F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(268, 322).addBox(17.5F, -178.5F, -24.5F, 1F, 2F, 2F)
                .texOffs(562, 303).addBox(-19.25F, -166F, -22.25F, 5F, 6F, 2F, new CubeDeformation(-0.25F, 0F, -0.25F))
                .texOffs(528, 303).addBox(-22.5F, -179F, -23.25F, 4F, 3F, 5F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(275, 322).addBox(-18.5F, -178.5F, -24.5F, 1F, 2F, 2F)
                .texOffs(981, 225).addBox(19.5F, -206F, 19.5F, 2F, 30F, 2F, new CubeDeformation(-0.3F, 0F, -0.3F))
                .texOffs(98, 303).addBox(20.25F, -205F, 20F, 15F, 8F, 1F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offset(0F, 24F, 0F));
        PartDefinition p_brace_l0 = p_frame.addOrReplaceChild("brace_l0", CubeListBuilder.create()
                .texOffs(360, 170).addBox(-1F, -0.9783F, -1F, 2F, 48F, 2F, new CubeDeformation(0F, -0.4783F, 0F)),
                PartPose.offsetAndRotation(23.3F, -11F, -19F, 2.1708F, 0F, 0F));
        PartDefinition p_brace_l1 = p_frame.addOrReplaceChild("brace_l1", CubeListBuilder.create()
                .texOffs(834, 0).addBox(-1F, -0.8275F, -1F, 2F, 54F, 2F, new CubeDeformation(0F, -0.3275F, 0F)),
                PartPose.offsetAndRotation(23.3F, -41F, 19F, -2.3292F, 0F, 0F));
        PartDefinition p_brace_l2 = p_frame.addOrReplaceChild("brace_l2", CubeListBuilder.create()
                .texOffs(843, 0).addBox(-1F, -0.8275F, -1F, 2F, 54F, 2F, new CubeDeformation(0F, -0.3275F, 0F)),
                PartPose.offsetAndRotation(23.3F, -81F, -19F, 2.3292F, 0F, 0F));
        PartDefinition p_brace_l3 = p_frame.addOrReplaceChild("brace_l3", CubeListBuilder.create()
                .texOffs(852, 0).addBox(-1F, -0.8275F, -1F, 2F, 54F, 2F, new CubeDeformation(0F, -0.3275F, 0F)),
                PartPose.offsetAndRotation(23.3F, -121F, 19F, -2.3292F, 0F, 0F));
        PartDefinition p_brace_r0 = p_frame.addOrReplaceChild("brace_r0", CubeListBuilder.create()
                .texOffs(369, 170).addBox(-1F, -0.9783F, -1F, 2F, 48F, 2F, new CubeDeformation(0F, -0.4783F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -11F, -19F, 2.1708F, 0F, 0F));
        PartDefinition p_brace_r1 = p_frame.addOrReplaceChild("brace_r1", CubeListBuilder.create()
                .texOffs(861, 0).addBox(-1F, -0.8275F, -1F, 2F, 54F, 2F, new CubeDeformation(0F, -0.3275F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -41F, 19F, -2.3292F, 0F, 0F));
        PartDefinition p_brace_r2 = p_frame.addOrReplaceChild("brace_r2", CubeListBuilder.create()
                .texOffs(870, 0).addBox(-1F, -0.8275F, -1F, 2F, 54F, 2F, new CubeDeformation(0F, -0.3275F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -81F, -19F, 2.3292F, 0F, 0F));
        PartDefinition p_brace_r3 = p_frame.addOrReplaceChild("brace_r3", CubeListBuilder.create()
                .texOffs(879, 0).addBox(-1F, -0.8275F, -1F, 2F, 54F, 2F, new CubeDeformation(0F, -0.3275F, 0F)),
                PartPose.offsetAndRotation(-23.3F, -121F, 19F, -2.3292F, 0F, 0F));
        PartDefinition p_bridge = p_frame.addOrReplaceChild("bridge", CubeListBuilder.create()
                .texOffs(109, 170).addBox(-14F, -48F, -2.75F, 28F, 48F, 3F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(97, 322).addBox(-13F, -8F, -4.25F, 26F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(154, 322).addBox(-13F, -24F, -4.25F, 26F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(211, 322).addBox(-13F, -40F, -4.25F, 26F, 2F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(334, 170).addBox(12.25F, -47F, 0F, 2F, 46F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(605, 314).addBox(12.75F, -13F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(616, 314).addBox(12.75F, -37F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(347, 170).addBox(-14.25F, -47F, 0F, 2F, 46F, 4F, new CubeDeformation(-0.25F, 0F, 0F))
                .texOffs(627, 314).addBox(-13.75F, -13F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(638, 314).addBox(-13.75F, -37F, 0F, 1F, 2F, 4F, new CubeDeformation(-0.05F, 0F, 0F))
                .texOffs(841, 314).addBox(-11F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(850, 314).addBox(-6F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(859, 314).addBox(-1F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(868, 314).addBox(4F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(877, 314).addBox(9F, -51F, -2.25F, 2F, 3F, 2F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(475, 314).addBox(-14F, -2F, -3.25F, 28F, 2F, 4F, new CubeDeformation(0F, 0F, -0.45F)),
                PartPose.offset(0F, -160F, -23F));
        PartDefinition p_wheel_fl = root.addOrReplaceChild("wheel_fl", CubeListBuilder.create()
                .texOffs(588, 274).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(793, 274).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(820, 274).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(131, 303).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(154, 303).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(721, 314).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(736, 314).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(649, 314).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(693, 314).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(282, 322).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(24F, 18F, -15F));
        PartDefinition p_wheel_fr = root.addOrReplaceChild("wheel_fr", CubeListBuilder.create()
                .texOffs(619, 274).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(847, 274).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(874, 274).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(177, 303).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(200, 303).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(751, 314).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(766, 314).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(660, 314).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(700, 314).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(289, 322).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-24F, 18F, -15F));
        PartDefinition p_wheel_bl = root.addOrReplaceChild("wheel_bl", CubeListBuilder.create()
                .texOffs(650, 274).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(901, 274).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(928, 274).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(223, 303).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(246, 303).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(781, 314).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(796, 314).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(671, 314).addBox(1.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(707, 314).addBox(1.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(296, 322).addBox(2.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(24F, 18F, 15F));
        PartDefinition p_wheel_br = root.addOrReplaceChild("wheel_br", CubeListBuilder.create()
                .texOffs(681, 274).addBox(-1.5F, -2F, -6F, 3F, 4F, 12F)
                .texOffs(955, 274).addBox(-1.5F, -4F, -5F, 3F, 2F, 10F)
                .texOffs(982, 274).addBox(-1.5F, 2F, -5F, 3F, 2F, 10F)
                .texOffs(269, 303).addBox(-1.5F, -5F, -4F, 3F, 1F, 8F)
                .texOffs(292, 303).addBox(-1.5F, 4F, -4F, 3F, 1F, 8F)
                .texOffs(811, 314).addBox(-1.5F, -6F, -2F, 3F, 1F, 4F)
                .texOffs(826, 314).addBox(-1.5F, 5F, -2F, 3F, 1F, 4F)
                .texOffs(682, 314).addBox(-2.5F, -1F, -2F, 1F, 2F, 4F)
                .texOffs(714, 314).addBox(-2.5F, -2F, -1F, 1F, 4F, 2F)
                .texOffs(303, 322).addBox(-3.3F, -1F, -1F, 1F, 2F, 2F, new CubeDeformation(-0.2F, 0F, 0F)),
                PartPose.offset(-24F, 18F, 15F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
