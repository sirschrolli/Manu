package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Rissjägers.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class RiftHoundGeometry {
    public static final int TEXTURE_WIDTH = 96;
    public static final int TEXTURE_HEIGHT = 96;

    private RiftHoundGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-3F, -3.6F, -6.7F, 6F, 7F, 8F, new CubeDeformation(-0.2F, -0.4F, -0.3F))
                .texOffs(58, 0).addBox(-2F, -3.1F, 0.5F, 4F, 5F, 7F, new CubeDeformation(0F, -0.3F, -0.3F)),
                PartPose.offsetAndRotation(0F, 9.5F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_spine = p_body.addOrReplaceChild("spine", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-0.5F, -0.7F, -6.2F, 1F, 1F, 13F, new CubeDeformation(0F, -0.1F, -0.2F)),
                PartPose.offset(0F, -3.2F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 27).addBox(-2F, -2.4F, -3.6F, 4F, 4F, 4F, new CubeDeformation(-0.3F, -0.2F, 0F))
                .texOffs(29, 16).addBox(-2.5F, -3.45F, -7.9F, 5F, 4F, 5F, new CubeDeformation(-0.4F, -0.05F, -0.3F))
                .texOffs(17, 27).addBox(-1.5F, -2.85F, -12.2F, 3F, 3F, 5F, new CubeDeformation(-0.05F, -0.35F, -0.2F)),
                PartPose.offsetAndRotation(0F, -1F, -6.2F, -0.2443F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(49, 27).addBox(-1.5F, 0F, -4.8F, 3F, 1F, 5F, new CubeDeformation(-0.2F, 0F, -0.2F)),
                PartPose.offsetAndRotation(0F, -0.3F, -7.4F, 0.1396F, 0F, 0F));
        PartDefinition p_left_spike = p_head.addOrReplaceChild("left_spike", CubeListBuilder.create()
                .texOffs(79, 27).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 4F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(1.3F, -3.2F, -4.4F, 1.0821F, 0.2443F, 0F));
        PartDefinition p_right_spike = p_head.addOrReplaceChild("right_spike", CubeListBuilder.create()
                .texOffs(0, 36).addBox(-0.5F, -0.5F, -0.2F, 1F, 1F, 4F, new CubeDeformation(0F, 0F, -0.2F)),
                PartPose.offsetAndRotation(-1.3F, -3.2F, -4.4F, 1.0821F, -0.2443F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(34, 27).addBox(-1F, -1F, -0.3F, 2F, 2F, 5F, new CubeDeformation(-0.45F, -0.45F, -0.3F)),
                PartPose.offsetAndRotation(0F, -1.6F, 7F, -0.4189F, 0F, 0F));
        PartDefinition p_tail2 = p_tail.addOrReplaceChild("tail2", CubeListBuilder.create()
                .texOffs(66, 27).addBox(-0.5F, -0.5F, -0.4F, 1F, 1F, 5F, new CubeDeformation(-0.05F, -0.05F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 4.2F, 0.384F, 0F, 0F));
        PartDefinition p_tail3 = p_tail2.addOrReplaceChild("tail3", CubeListBuilder.create()
                .texOffs(0, 42).addBox(-0.5F, -0.5F, 0F, 1F, 1F, 3F, new CubeDeformation(-0.15F, -0.15F, 0F))
                .texOffs(11, 36).addBox(-1F, -1F, 2.4F, 2F, 2F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(0F, 0F, 4F, 0.3142F, 0F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(50, 16).addBox(-1F, -1.2F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offset(1.8F, 11F, -4.4F));
        PartDefinition p_left_front_shin = p_left_front_leg.addOrReplaceChild("left_front_shin", CubeListBuilder.create()
                .texOffs(59, 16).addBox(-1F, 0F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.35F, 0F, -0.35F))
                .texOffs(49, 36).addBox(-1F, 6.55F, -1.95F, 2F, 1F, 3F, new CubeDeformation(-0.15F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(0F, 5.4F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(81, 0).addBox(-1F, -1.6F, -1.5F, 2F, 7F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(1.6F, 11F, 5.6F, -0.2094F, 0F, 0F));
        PartDefinition p_left_hind_shin = p_left_hind_leg.addOrReplaceChild("left_hind_shin", CubeListBuilder.create()
                .texOffs(11, 16).addBox(-1F, -0.2F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(60, 36).addBox(-1F, 6.95F, -2.15F, 2F, 1F, 3F, new CubeDeformation(-0.15F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(0F, 4.8F, 0.4F, 0.2443F, 0F, 0F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(68, 16).addBox(-1F, -1.2F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offset(-1.8F, 11F, -4.4F));
        PartDefinition p_right_front_shin = p_right_front_leg.addOrReplaceChild("right_front_shin", CubeListBuilder.create()
                .texOffs(77, 16).addBox(-1F, 0F, -1F, 2F, 7F, 2F, new CubeDeformation(-0.35F, 0F, -0.35F))
                .texOffs(71, 36).addBox(-1F, 6.55F, -1.95F, 2F, 1F, 3F, new CubeDeformation(-0.15F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(0F, 5.4F, 0F, -0.0698F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 16).addBox(-1F, -1.6F, -1.5F, 2F, 7F, 3F, new CubeDeformation(0F, -0.4F, -0.1F)),
                PartPose.offsetAndRotation(-1.6F, 11F, 5.6F, -0.2094F, 0F, 0F));
        PartDefinition p_right_hind_shin = p_right_hind_leg.addOrReplaceChild("right_hind_shin", CubeListBuilder.create()
                .texOffs(20, 16).addBox(-1F, -0.2F, -1F, 2F, 8F, 2F, new CubeDeformation(-0.4F, -0.2F, -0.4F))
                .texOffs(82, 36).addBox(-1F, 6.95F, -2.15F, 2F, 1F, 3F, new CubeDeformation(-0.15F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(0F, 4.8F, 0.4F, 0.2443F, 0F, 0F));
        PartDefinition p_shard1 = root.addOrReplaceChild("shard1", CubeListBuilder.create()
                .texOffs(22, 36).addBox(-1F, -1.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(0F, 2.4F, -3.2F, 0.3491F, 0F, 0.5236F));
        PartDefinition p_shard2 = root.addOrReplaceChild("shard2", CubeListBuilder.create()
                .texOffs(31, 36).addBox(-1F, -1.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(1F, 1.6F, 0.6F, -0.2618F, 0F, -0.4363F));
        PartDefinition p_shard3 = root.addOrReplaceChild("shard3", CubeListBuilder.create()
                .texOffs(40, 36).addBox(-1F, -1.5F, -1F, 2F, 3F, 2F, new CubeDeformation(-0.4F, 0F, -0.4F)),
                PartPose.offsetAndRotation(-0.8F, 2F, 4F, 0.5236F, 0F, 0.2618F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
