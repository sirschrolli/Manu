package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Flamingos (Ruhehaltung: Flügel ausgebreitet).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FlamingoGeometry {
    public static final int TEXTURE_WIDTH = 64;
    public static final int TEXTURE_HEIGHT = 64;

    private FlamingoGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.5F, -2.6F, -3.7F, 5F, 5F, 8F, new CubeDeformation(-0.3F, -0.2F, -0.3F))
                .texOffs(53, 29).addBox(-2F, -2.1F, -4.2F, 4F, 4F, 1F, new CubeDeformation(-0.1F, -0.1F, 0F))
                .texOffs(27, 0).addBox(-2F, -3.1F, -2.9F, 4F, 1F, 6F, new CubeDeformation(-0.2F, -0.2F, -0.3F))
                .texOffs(21, 29).addBox(-2F, 1.9F, -2.4F, 4F, 1F, 5F, new CubeDeformation(-0.2F, -0.2F, 0F)),
                PartPose.offset(0F, 10F, 0.5F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(40, 29).addBox(-1.5F, -0.9F, -0.6F, 3F, 2F, 3F, new CubeDeformation(-0.1F, -0.3F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1.6F, 3.8F, 0.2094F, 0F, 0F));
        PartDefinition p_neck1 = p_body.addOrReplaceChild("neck1", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-1F, -4.6F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.25F, -0.2F, -0.25F)),
                PartPose.offsetAndRotation(0F, -1.4F, -3.4F, -0.2094F, 0F, 0F));
        PartDefinition p_neck2 = p_neck1.addOrReplaceChild("neck2", CubeListBuilder.create()
                .texOffs(0, 14).addBox(-1F, -4.5F, -1F, 2F, 5F, 2F, new CubeDeformation(-0.3F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, -4.2F, 0F, -0.384F, 0F, 0F));
        PartDefinition p_neck3 = p_neck2.addOrReplaceChild("neck3", CubeListBuilder.create()
                .texOffs(22, 36).addBox(-1F, -2.8F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.35F, 0F, -0.35F)),
                PartPose.offsetAndRotation(0F, -4F, 0F, 0.8378F, 0F, 0F));
        PartDefinition p_head = p_neck3.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-1F, -1.7F, -2F, 2F, 2F, 3F, new CubeDeformation(0F, 0F, -0.1F))
                .texOffs(60, 36).addBox(0.53F, -1.3F, -1.4F, 1F, 1F, 1F, new CubeDeformation(-0.45F, -0.2F, -0.2F))
                .texOffs(0, 42).addBox(-1.53F, -1.3F, -1.4F, 1F, 1F, 1F, new CubeDeformation(-0.45F, -0.2F, -0.2F))
                .texOffs(11, 36).addBox(-1F, -1.7F, -4.4F, 2F, 2F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, -2.6F, 0F, -0.2443F, 0F, 0F));
        PartDefinition p_beak_tip = p_head.addOrReplaceChild("beak_tip", CubeListBuilder.create()
                .texOffs(53, 36).addBox(-0.5F, -0.95F, -1.7F, 1F, 2F, 2F, new CubeDeformation(0F, -0.45F, -0.1F)),
                PartPose.offsetAndRotation(0F, -0.5F, -3.8F, 0.9076F, 0F, 0F));
        PartDefinition p_wing_l = p_body.addOrReplaceChild("wing_l", CubeListBuilder.create()
                .texOffs(29, 14).addBox(-0.25F, -0.5F, -0.5F, 7F, 1F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.1F)),
                PartPose.offset(1.9F, -1.9F, -2.4F));
        PartDefinition p_wing_l_tip = p_wing_l.addOrReplaceChild("wing_l_tip", CubeListBuilder.create()
                .texOffs(25, 22).addBox(0F, -0.5F, -0.7F, 5F, 1F, 5F, new CubeDeformation(0F, -0.25F, -0.4F)),
                PartPose.offset(6.5F, 0F, 0F));
        PartDefinition p_wing_r = p_body.addOrReplaceChild("wing_r", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-6.75F, -0.5F, -0.5F, 7F, 1F, 5F, new CubeDeformation(-0.25F, -0.2F, -0.1F)),
                PartPose.offset(-1.9F, -1.9F, -2.4F));
        PartDefinition p_wing_r_tip = p_wing_r.addOrReplaceChild("wing_r_tip", CubeListBuilder.create()
                .texOffs(0, 29).addBox(-5F, -0.5F, -0.7F, 5F, 1F, 5F, new CubeDeformation(0F, -0.25F, -0.4F)),
                PartPose.offset(-6.5F, 0F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(9, 14).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(1F, 12F, 1F));
        PartDefinition p_left_shin = p_left_leg.addOrReplaceChild("left_shin", CubeListBuilder.create()
                .texOffs(5, 42).addBox(-0.5F, -0.4F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(14, 14).addBox(-0.5F, -0.1F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.22F, -0.1F, -0.22F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_left_foot = p_left_shin.addOrReplaceChild("left_foot", CubeListBuilder.create()
                .texOffs(31, 36).addBox(-1F, -0.4F, -2.15F, 2F, 1F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.45F)),
                PartPose.offset(0F, 5.8F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(19, 14).addBox(-0.5F, 0F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offset(-1F, 12F, 1F));
        PartDefinition p_right_shin = p_right_leg.addOrReplaceChild("right_shin", CubeListBuilder.create()
                .texOffs(10, 42).addBox(-0.5F, -0.4F, -0.5F, 1F, 1F, 1F, new CubeDeformation(-0.15F, -0.1F, -0.15F))
                .texOffs(24, 14).addBox(-0.5F, -0.1F, -0.5F, 1F, 6F, 1F, new CubeDeformation(-0.22F, -0.1F, -0.22F)),
                PartPose.offset(0F, 6F, 0F));
        PartDefinition p_right_foot = p_right_shin.addOrReplaceChild("right_foot", CubeListBuilder.create()
                .texOffs(42, 36).addBox(-1F, -0.4F, -2.15F, 2F, 1F, 3F, new CubeDeformation(-0.1F, -0.4F, -0.45F)),
                PartPose.offset(0F, 5.8F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
