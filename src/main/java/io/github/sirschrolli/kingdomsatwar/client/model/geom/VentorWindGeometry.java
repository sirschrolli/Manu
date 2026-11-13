package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Ventors Wind-Ebene (gleiche Knochen wie VentorGeometry, nur der Dunst der Ringe; das Bild rollt).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class VentorWindGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private VentorWindGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_wind = root.addOrReplaceChild("wind", CubeListBuilder.create(),
                PartPose.offset(0F, 1.5F, 0F));
        PartDefinition p_ring_0 = p_wind.addOrReplaceChild("ring_0", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, 0F, -5F, 10F, 6F, 10F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(74, 0).addBox(-3.5F, 0F, -3.5F, 7F, 6F, 7F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_ring_1 = p_ring_0.addOrReplaceChild("ring_1", CubeListBuilder.create()
                .texOffs(41, 0).addBox(-4F, 0F, -4F, 8F, 6F, 8F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(0, 17).addBox(-2.5F, 0F, -2.5F, 5F, 6F, 5F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_ring_2 = p_ring_1.addOrReplaceChild("ring_2", CubeListBuilder.create()
                .texOffs(103, 0).addBox(-3F, -0.25F, -3F, 6F, 6F, 6F, new CubeDeformation(-0.1F, -0.25F, -0.1F))
                .texOffs(21, 17).addBox(-2F, -0.25F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.3F, -0.25F, -0.3F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_ring_3 = p_ring_2.addOrReplaceChild("ring_3", CubeListBuilder.create()
                .texOffs(38, 17).addBox(-2F, 0F, -2F, 4F, 5F, 4F, new CubeDeformation(-0.1F, 0F, -0.1F))
                .texOffs(55, 17).addBox(-1F, 0F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.1F, 0F, -0.1F)),
                PartPose.offset(0F, 5.5F, 0F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create(),
                PartPose.offset(0F, 1.5F, 0F));
        PartDefinition p_loin_front = p_body.addOrReplaceChild("loin_front", CubeListBuilder.create(),
                PartPose.offset(0F, 0.4F, -2.75F));
        PartDefinition p_loin_back = p_body.addOrReplaceChild("loin_back", CubeListBuilder.create(),
                PartPose.offset(0F, 0.4F, 2.75F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offset(0F, -16.2F, 0F));
        PartDefinition p_crest_tail = p_head.addOrReplaceChild("crest_tail", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, -12F, 5.7F, 0.2793F, 0F, 0F));
        PartDefinition p_arm_r = p_body.addOrReplaceChild("arm_r", CubeListBuilder.create(),
                PartPose.offset(-6.4F, -13.8F, 0F));
        PartDefinition p_forearm_r = p_arm_r.addOrReplaceChild("forearm_r", CubeListBuilder.create(),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_hand_r = p_forearm_r.addOrReplaceChild("hand_r", CubeListBuilder.create(),
                PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_trident = p_hand_r.addOrReplaceChild("trident", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 1.7F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_arm_l = p_body.addOrReplaceChild("arm_l", CubeListBuilder.create(),
                PartPose.offset(6.4F, -13.8F, 0F));
        PartDefinition p_galerus = p_arm_l.addOrReplaceChild("galerus", CubeListBuilder.create(),
                PartPose.offsetAndRotation(1.6F, -1.9F, 0F, 0F, 0F, 0.2443F));
        PartDefinition p_forearm_l = p_arm_l.addOrReplaceChild("forearm_l", CubeListBuilder.create(),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_hand_l = p_forearm_l.addOrReplaceChild("hand_l", CubeListBuilder.create(),
                PartPose.offset(0F, 6.6F, 0F));
        PartDefinition p_net = p_hand_l.addOrReplaceChild("net", CubeListBuilder.create(),
                PartPose.offset(0F, 2.6F, 0F));
        PartDefinition p_net_a = p_net.addOrReplaceChild("net_a", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 1.8F, 0F, 0.1396F, 0.6109F, 0.1047F));
        PartDefinition p_net_b = p_net.addOrReplaceChild("net_b", CubeListBuilder.create(),
                PartPose.offsetAndRotation(0F, 1.8F, 0F, -0.1047F, -0.9076F, -0.0698F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
