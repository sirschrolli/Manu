package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Ventor, dem Gladiator der Winde (1.9; im Spiel 1,1-fach vergrößert): Leib, Helm, Dreizack, Netz, Windfäden.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VentorGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private VentorGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_wind = root.addOrReplaceChild("wind", CubeListBuilder.create(),
                PartPose.offset(0F, 1.5F, 0F));
        PartDefinition p_ring_0 = p_wind.addOrReplaceChild("ring_0", CubeListBuilder.create()
                .texOffs(38, 0).addBox(-5.5F, 0F, -5.5F, 11F, 6F, 11F)
                .texOffs(52, 88).addBox(4.55F, 3.55F, -1.25F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(115, 79).addBox(-5F, 1F, 0.6F, 0F, 2F, 3F, new CubeDeformation(0F, -0.4F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ring_1 = p_ring_0.addOrReplaceChild("ring_1", CubeListBuilder.create()
                .texOffs(0, 28).addBox(-4.5F, 0F, -4.5F, 9F, 6F, 9F)
                .texOffs(57, 88).addBox(-4.2F, 2.1F, 1.5F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(62, 88).addBox(1.25F, 4.25F, -4.15F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.15F))
                .texOffs(117, 88).addBox(2F, 1F, 4.1F, 2F, 1F, 0F),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_ring_2 = p_ring_1.addOrReplaceChild("ring_2", CubeListBuilder.create()
                .texOffs(70, 28).addBox(-3.5F, -0.25F, -3.5F, 7F, 6F, 7F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(67, 88).addBox(2.7F, 1.5F, 1.8F, 1F, 1F, 1F, new CubeDeformation(-0.1F, -0.1F, -0.1F))
                .texOffs(72, 88).addBox(-2.35F, 3.25F, -3.25F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.15F, -0.15F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_ring_3 = p_ring_2.addOrReplaceChild("ring_3", CubeListBuilder.create()
                .texOffs(49, 57).addBox(-2.5F, 0F, -2.5F, 5F, 5F, 5F),
                PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(94, 44).addBox(-4F, -8.2F, -2.5F, 8F, 6F, 5F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(37, 28).addBox(-5F, -15.4F, -3F, 10F, 8F, 6F, new CubeDeformation(-0.1F, -0.4F, -0.2F))
                .texOffs(91, 79).addBox(-2F, -16.6F, -1.5F, 4F, 2F, 3F, new CubeDeformation(-0.4F, 0F, 0F))
                .texOffs(94, 57).addBox(-5F, -2.4F, -3F, 10F, 3F, 6F, new CubeDeformation(-0.4F, 0F, -0.25F))
                .texOffs(106, 79).addBox(-1.5F, -2.95F, -3.4F, 3F, 4F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.3F)),
                PartPose.offset(0F, 1.5F, 0F));
        PartDefinition p_loin_front = p_body.addOrReplaceChild("loin_front", CubeListBuilder.create()
                .texOffs(21, 69).addBox(-3F, -0.25F, -0.7F, 6F, 8F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.3F)),
                PartPose.offset(0F, 0.4F, -2.75F));
        PartDefinition p_loin_back = p_body.addOrReplaceChild("loin_back", CubeListBuilder.create()
                .texOffs(17, 79).addBox(-3F, 0F, -0.3F, 6F, 7F, 1F, new CubeDeformation(-0.2F, 0F, -0.3F)),
                PartPose.offset(0F, 0.4F, 2.75F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(83, 0).addBox(-4F, -8.9F, -4F, 8F, 9F, 8F, new CubeDeformation(-0.3F, -0.1F, -0.1F))
                .texOffs(0, 44).addBox(-5F, -6.1F, -5.1F, 10F, 1F, 11F, new CubeDeformation(-0.3F, -0.1F, -0.2F))
                .texOffs(50, 69).addBox(-3F, -9.8F, -3.5F, 6F, 1F, 7F, new CubeDeformation(0F, 0F, -0.3F))
                .texOffs(0, 69).addBox(-1F, -10.7F, -3.9F, 2F, 1F, 8F, new CubeDeformation(0F, 0F, -0.2F))
                .texOffs(9, 0).addBox(-1F, -17.6F, -5.6F, 2F, 7F, 12F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offset(0F, -16.2F, 0F));
        PartDefinition p_crest_tail = p_head.addOrReplaceChild("crest_tail", CubeListBuilder.create()
                .texOffs(85, 57).addBox(-1F, 0F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.3F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -12F, 5.7F, 0.2793F, 0F, 0F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create()
                .texOffs(43, 44).addBox(-2F, -1.8F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offset(-6.4F, -13.8F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create()
                .texOffs(0, 57).addBox(-2F, -0.2F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(61, 79).addBox(-2F, 4.05F, -2F, 4F, 2F, 4F, new CubeDeformation(-0.15F, -0.25F, -0.15F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_hand_r = p_forearm_r.addOrReplaceChild("hand_r", CubeListBuilder.create()
                .texOffs(98, 69).addBox(-2F, -0.4F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_trident = p_hand_r.addOrReplaceChild("trident", CubeListBuilder.create()
                .texOffs(19, 88).addBox(-1F, 6.55F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.25F, -0.05F))
                .texOffs(0, 0).addBox(-1F, -17.85F, -1F, 2F, 25F, 2F, new CubeDeformation(-0.45F, -0.35F, -0.45F))
                .texOffs(36, 69).addBox(-1F, -3.1F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.2F, -0.3F, -0.2F))
                .texOffs(28, 88).addBox(-1F, -19.2F, -1F, 2F, 2F, 2F, new CubeDeformation(-0.05F, -0.2F, -0.05F))
                .texOffs(0, 88).addBox(-3.5F, -20.7F, -1F, 7F, 2F, 2F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(45, 69).addBox(-0.5F, -28.4F, -0.5F, 1F, 8F, 1F)
                .texOffs(37, 88).addBox(-0.5F, -30F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(51, 79).addBox(2.25F, -26.3F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(77, 88).addBox(2.25F, -27.2F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(42, 88).addBox(3F, -25.25F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.15F))
                .texOffs(56, 79).addBox(-3.25F, -26.3F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.05F, -0.1F, -0.05F))
                .texOffs(82, 88).addBox(-3.25F, -27.2F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F))
                .texOffs(47, 88).addBox(-4F, -25.25F, -0.5F, 1F, 2F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.15F)),
                PartPose.offsetAndRotation(0F, 1.7F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create()
                .texOffs(60, 44).addBox(-2F, -1.8F, -2F, 4F, 8F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(77, 69).addBox(-2.5F, -2.4F, -2.5F, 5F, 3F, 5F, new CubeDeformation(-0.25F, -0.1F, -0.25F)),
                PartPose.offset(6.4F, -13.8F, 0F));
        PartDefinition p_galerus = p_arm_l.addOrReplaceChild("galerus", CubeListBuilder.create()
                .texOffs(34, 57).addBox(-0.9F, -4.9F, -2.5F, 2F, 6F, 5F, new CubeDeformation(-0.3F, -0.3F, 0F))
                .texOffs(32, 79).addBox(-1F, -5.5F, -3F, 3F, 1F, 6F, new CubeDeformation(-0.4F, -0.1F, -0.3F)),
                PartPose.offsetAndRotation(1.6F, -1.9F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create()
                .texOffs(17, 57).addBox(-2F, -0.2F, -2F, 4F, 7F, 4F, new CubeDeformation(-0.4F, -0.2F, -0.4F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_hand_l = p_forearm_l.addOrReplaceChild("hand_l", CubeListBuilder.create()
                .texOffs(0, 79).addBox(-2F, -0.4F, -2F, 4F, 4F, 4F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_net = p_hand_l.addOrReplaceChild("net", CubeListBuilder.create()
                .texOffs(78, 79).addBox(-1.5F, -0.7F, -1.5F, 3F, 3F, 3F, new CubeDeformation(-0.1F, -0.1F, 0F)),
                PartPose.offset(0F, 2.6F, 0F));
        PartDefinition p_net_a = p_net.addOrReplaceChild("net_a", CubeListBuilder.create()
                .texOffs(77, 44).addBox(-4F, -0.25F, 0F, 8F, 12F, 0F, new CubeDeformation(-0.2F, -0.25F, 0F))
                .texOffs(87, 88).addBox(-4.3F, 11.15F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(92, 88).addBox(-0.5F, 11.15F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(97, 88).addBox(3.3F, 11.15F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(0F, 1.8F, 0F, 0.1396F, 0.6109F, 0.1047F));
        PartDefinition p_net_b = p_net.addOrReplaceChild("net_b", CubeListBuilder.create()
                .texOffs(70, 57).addBox(-3.5F, 0F, 0F, 7F, 10F, 0F, new CubeDeformation(-0.3F, 0F, 0F))
                .texOffs(102, 88).addBox(-3.7F, 9.65F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(107, 88).addBox(-0.5F, 9.65F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F))
                .texOffs(112, 88).addBox(2.7F, 9.65F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.05F, -0.05F, -0.05F)),
                PartPose.offsetAndRotation(0F, 1.8F, 0F, -0.1047F, -0.9076F, -0.0698F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
