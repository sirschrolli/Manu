package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie von Esel und Maultier (Skelett wie bei Vanilla, damit Sattel und Packtaschen passen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FarmDonkeyGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private FarmDonkeyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -8F, -17F, 10F, 10F, 22F, new CubeDeformation(0.05F)),
                PartPose.offset(0F, 11F, 5F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(95, 33).addBox(-0.8F, 0F, 1.2F, 1.6F, 10F, 1.6F),
                PartPose.offsetAndRotation(0F, -5F, 2F, 0.5236F, 0F, 0F));
        PartDefinition p_tail_tuft = p_tail.addOrReplaceChild("tail_tuft", CubeListBuilder.create()
                .texOffs(42, 49).addBox(-1.5F, 8.5F, 0.5F, 3F, 5F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_tail_hair = p_tail.addOrReplaceChild("tail_hair", CubeListBuilder.create()
                .texOffs(99, 0).addBox(-1.5F, 0F, 0F, 3F, 14F, 4F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_chest = p_body.addOrReplaceChild("left_chest", CubeListBuilder.create()
                .texOffs(104, 33).addBox(-4F, 0F, -2F, 8F, 8F, 3F),
                PartPose.offsetAndRotation(6F, -8F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_right_chest = p_body.addOrReplaceChild("right_chest", CubeListBuilder.create()
                .texOffs(0, 49).addBox(-4F, 0F, -2F, 8F, 8F, 3F),
                PartPose.offsetAndRotation(-6F, -8F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_head_parts = root.addOrReplaceChild("head_parts", CubeListBuilder.create()
                .texOffs(65, 0).addBox(-2.05F, -6F, -2F, 4F, 12F, 7F),
                PartPose.offsetAndRotation(0F, 4F, -12F, 0.5236F, 0F, 0F));
        PartDefinition p_head = p_head_parts.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(68, 33).addBox(-3F, -11F, -2F, 6F, 5F, 7F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(55, 49).addBox(-1F, -7F, 0F, 2F, 7F, 1F),
                PartPose.offsetAndRotation(1.25F, -10F, 4F, 0.2618F, 0F, 0.2618F));
        PartDefinition p_left_ear_mule = p_head.addOrReplaceChild("left_ear_mule", CubeListBuilder.create()
                .texOffs(69, 49).addBox(-1F, -5F, 0F, 2F, 5F, 1F),
                PartPose.offsetAndRotation(1.25F, -10F, 4F, 0.2094F, 0F, 0.2269F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(62, 49).addBox(-1F, -7F, 0F, 2F, 7F, 1F),
                PartPose.offsetAndRotation(-1.25F, -10F, 4F, 0.2618F, 0F, -0.2618F));
        PartDefinition p_right_ear_mule = p_head.addOrReplaceChild("right_ear_mule", CubeListBuilder.create()
                .texOffs(76, 49).addBox(-1F, -5F, 0F, 2F, 5F, 1F),
                PartPose.offsetAndRotation(-1.25F, -10F, 4F, 0.2094F, 0F, -0.2269F));
        PartDefinition p_upper_mouth = p_head_parts.addOrReplaceChild("upper_mouth", CubeListBuilder.create()
                .texOffs(23, 49).addBox(-2F, -11F, -7F, 4F, 5F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mane = p_head_parts.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(88, 0).addBox(-1F, -11.2F, 4.6F, 2F, 15.5F, 2.2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-3F, -1.01F, -1F, 4F, 11F, 4F),
                PartPose.offset(4F, 14F, 7F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(17, 33).addBox(-1F, -1.01F, -1F, 4F, 11F, 4F),
                PartPose.offset(-4F, 14F, 7F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(34, 33).addBox(-3F, -1.01F, -1.9F, 4F, 11F, 4F),
                PartPose.offset(4F, 14F, -10F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(51, 33).addBox(-1F, -1.01F, -1.9F, 4F, 11F, 4F),
                PartPose.offset(-4F, 14F, -10F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
