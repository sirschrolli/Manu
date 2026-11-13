package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Nachtwidders.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class NightRamGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private NightRamGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(59, 0).addBox(-5F, -5F, -8F, 10F, 9F, 16F)
                .texOffs(54, 29).addBox(-4.5F, -7.1F, -8.45F, 9F, 3F, 8F, new CubeDeformation(-0.1F, -0.3F, -0.25F)),
                PartPose.offset(0F, 9F, 1F));
        PartDefinition p_shag = p_body.addOrReplaceChild("shag", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-6F, -5.6F, -8.5F, 12F, 11F, 17F, new CubeDeformation(-0.4F, -0.2F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(103, 44).addBox(-2F, -0.9F, -0.9F, 4F, 5F, 3F, new CubeDeformation(-0.4F, -0.4F, -0.4F)),
                PartPose.offsetAndRotation(0F, -3F, 8.4F, 0.6632F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(29, 29).addBox(-3F, -3.1F, -4.9F, 6F, 7F, 6F, new CubeDeformation(-0.2F, -0.3F, -0.3F))
                .texOffs(0, 29).addBox(-3.5F, -6.2F, -9.9F, 7F, 7F, 7F, new CubeDeformation(-0.3F, -0.3F, -0.1F))
                .texOffs(89, 29).addBox(-2.5F, -4.3F, -14.1F, 5F, 5F, 5F, new CubeDeformation(-0.1F, -0.3F, -0.3F))
                .texOffs(100, 63).addBox(-1.5F, -3.5F, -14.4F, 3F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.3F))
                .texOffs(109, 63).addBox(2.825F, -4.45F, -9.65F, 1F, 1F, 2F, new CubeDeformation(-0.275F, -0.05F, -0.25F))
                .texOffs(116, 63).addBox(-3.825F, -4.45F, -9.65F, 1F, 1F, 2F, new CubeDeformation(-0.275F, -0.05F, -0.25F)),
                PartPose.offsetAndRotation(0F, -3.2F, -8F, 0.3142F, 0F, 0F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(101, 55).addBox(-2F, -0.4F, -3.8F, 4F, 2F, 4F, new CubeDeformation(-0.1F, -0.4F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0.2F, -9.4F, 0.0698F, 0F, 0F));
        PartDefinition p_beard = p_jaw.addOrReplaceChild("beard", CubeListBuilder.create()
                .texOffs(90, 55).addBox(-1.5F, -0.2F, -1F, 3F, 5F, 2F, new CubeDeformation(-0.3F, -0.2F, 0F)),
                PartPose.offsetAndRotation(0F, 1F, -2F, -0.1396F, 0F, 0F));
        PartDefinition p_left_horn = p_head.addOrReplaceChild("left_horn", CubeListBuilder.create()
                .texOffs(0, 55).addBox(-1.5F, -1.5F, -0.1F, 3F, 3F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(2.6F, -5.2F, -6.4F, 0.8378F, 0.4538F, 0F));
        PartDefinition p_left_horn2 = p_left_horn.addOrReplaceChild("left_horn2", CubeListBuilder.create()
                .texOffs(15, 55).addBox(-1.5F, -1.5F, -0.2F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 3.4F, -1.0123F, 0.1745F, 0F));
        PartDefinition p_left_horn3 = p_left_horn2.addOrReplaceChild("left_horn3", CubeListBuilder.create()
                .texOffs(30, 55).addBox(-1.5F, -1.5F, -0.3F, 3F, 3F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 3.2F, -1.0123F, 0.1745F, 0F));
        PartDefinition p_left_horn4 = p_left_horn3.addOrReplaceChild("left_horn4", CubeListBuilder.create()
                .texOffs(0, 63).addBox(-1F, -1F, -0.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, -0.9774F, 0.1396F, 0F));
        PartDefinition p_left_horn5 = p_left_horn4.addOrReplaceChild("left_horn5", CubeListBuilder.create()
                .texOffs(78, 63).addBox(-1F, -1F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 2.7F, -0.8727F, 0.1047F, 0F));
        PartDefinition p_right_horn = p_head.addOrReplaceChild("right_horn", CubeListBuilder.create()
                .texOffs(45, 55).addBox(-1.5F, -1.5F, -0.1F, 3F, 3F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.1F)),
                PartPose.offsetAndRotation(-2.6F, -5.2F, -6.4F, 0.8378F, -0.4538F, 0F));
        PartDefinition p_right_horn2 = p_right_horn.addOrReplaceChild("right_horn2", CubeListBuilder.create()
                .texOffs(60, 55).addBox(-1.5F, -1.5F, -0.2F, 3F, 3F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F)),
                PartPose.offsetAndRotation(0F, 0F, 3.4F, -1.0123F, -0.1745F, 0F));
        PartDefinition p_right_horn3 = p_right_horn2.addOrReplaceChild("right_horn3", CubeListBuilder.create()
                .texOffs(75, 55).addBox(-1.5F, -1.5F, -0.3F, 3F, 3F, 4F, new CubeDeformation(-0.35F, -0.35F, -0.3F)),
                PartPose.offsetAndRotation(0F, 0F, 3.2F, -1.0123F, -0.1745F, 0F));
        PartDefinition p_right_horn4 = p_right_horn3.addOrReplaceChild("right_horn4", CubeListBuilder.create()
                .texOffs(13, 63).addBox(-1F, -1F, -0.45F, 2F, 2F, 4F, new CubeDeformation(-0.05F, -0.05F, -0.45F)),
                PartPose.offsetAndRotation(0F, 0F, 3F, -0.9774F, -0.1396F, 0F));
        PartDefinition p_right_horn5 = p_right_horn4.addOrReplaceChild("right_horn5", CubeListBuilder.create()
                .texOffs(89, 63).addBox(-1F, -1F, -0.1F, 2F, 2F, 3F, new CubeDeformation(-0.3F, -0.3F, -0.1F)),
                PartPose.offsetAndRotation(0F, 0F, 2.7F, -0.8727F, -0.1047F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(110, 29).addBox(-2F, -0.7F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(51, 44).addBox(-1.5F, 4.1F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(26, 63).addBox(-1.5F, 10.2F, -1.55F, 3F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.05F)),
                PartPose.offset(3.1F, 12F, -5.6F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 44).addBox(-2F, -0.7F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(64, 44).addBox(-1.5F, 4.1F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(39, 63).addBox(-1.5F, 10.2F, -1.55F, 3F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.05F)),
                PartPose.offset(-3.1F, 12F, -5.6F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(17, 44).addBox(-2F, -0.7F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(77, 44).addBox(-1.5F, 4.1F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(52, 63).addBox(-1.5F, 10.2F, -1.55F, 3F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.05F)),
                PartPose.offset(3.1F, 12F, 6F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(34, 44).addBox(-2F, -0.7F, -2F, 4F, 6F, 4F, new CubeDeformation(-0.2F, -0.2F, -0.2F))
                .texOffs(90, 44).addBox(-1.5F, 4.1F, -1.5F, 3F, 7F, 3F, new CubeDeformation(-0.3F, -0.4F, -0.3F))
                .texOffs(65, 63).addBox(-1.5F, 10.2F, -1.55F, 3F, 2F, 3F, new CubeDeformation(-0.1F, -0.2F, -0.05F)),
                PartPose.offset(-3.1F, 12F, 6F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
