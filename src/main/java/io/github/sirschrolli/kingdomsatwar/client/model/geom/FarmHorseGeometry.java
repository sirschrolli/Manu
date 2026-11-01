package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Pferde (Skelett wie beim Vanilla-Pferd, damit Sattel und Rossharnisch passen).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class FarmHorseGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private FarmHorseGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -8F, -17F, 10F, 10F, 22F, new CubeDeformation(0.05F)),
                PartPose.offset(0F, 11F, 5F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(114, 0).addBox(-1.5F, 0F, 0F, 3F, 14F, 4F)
                .texOffs(95, 33).addBox(-2F, 8.75F, -0.6F, 4F, 7F, 5F, new CubeDeformation(0F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(0F, -5F, 2F, 0.5236F, 0F, 0F));
        PartDefinition p_head_parts = root.addOrReplaceChild("head_parts", CubeListBuilder.create()
                .texOffs(91, 0).addBox(-2.05F, -6F, -2F, 4F, 12F, 7F),
                PartPose.offsetAndRotation(0F, 4F, -12F, 0.5236F, 0F, 0F));
        PartDefinition p_head = p_head_parts.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(68, 33).addBox(-3F, -11F, -2F, 6F, 5F, 7F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_forelock = p_head.addOrReplaceChild("forelock", CubeListBuilder.create()
                .texOffs(0, 60).addBox(-1.5F, -11.7F, 1F, 3F, 1F, 4F, new CubeDeformation(0F, -0.2F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_forelock_big = p_head.addOrReplaceChild("forelock_big", CubeListBuilder.create()
                .texOffs(103, 49).addBox(-2F, -11.85F, -0.9F, 4F, 1F, 6F, new CubeDeformation(0F, -0.05F, -0.3F))
                .texOffs(29, 60).addBox(-1.5F, -11.2F, -1.45F, 3F, 2F, 1F, new CubeDeformation(0F, -0.2F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(15, 60).addBox(-1F, -2F, -0.5F, 2F, 3F, 1F),
                PartPose.offset(1.55F, -11F, 4.5F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(22, 60).addBox(-1F, -2F, -0.5F, 2F, 3F, 1F),
                PartPose.offset(-1.55F, -11F, 4.5F));
        PartDefinition p_upper_mouth = p_head_parts.addOrReplaceChild("upper_mouth", CubeListBuilder.create()
                .texOffs(0, 49).addBox(-2F, -11F, -7F, 4F, 5F, 5F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mane = p_head_parts.addOrReplaceChild("mane", CubeListBuilder.create()
                .texOffs(78, 0).addBox(-1.5F, -11.7F, 4.35F, 3F, 17F, 3F, new CubeDeformation(-0.35F, -0.1F, -0.15F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_mane_drape = p_head_parts.addOrReplaceChild("mane_drape", CubeListBuilder.create()
                .texOffs(65, 0).addBox(1.9F, -11F, 2.2F, 1F, 16F, 5F, new CubeDeformation(0F, -0.4F, -0.2F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-3F, -1.01F, -1F, 4F, 11F, 4F),
                PartPose.offset(4F, 14F, 7F));
        PartDefinition p_feather_left_hind_leg = p_left_hind_leg.addOrReplaceChild("feather_left_hind_leg", CubeListBuilder.create()
                .texOffs(19, 49).addBox(-3.5F, 5.45F, -1.5F, 5F, 4F, 5F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(17, 33).addBox(-1F, -1.01F, -1F, 4F, 11F, 4F),
                PartPose.offset(-4F, 14F, 7F));
        PartDefinition p_feather_right_hind_leg = p_right_hind_leg.addOrReplaceChild("feather_right_hind_leg", CubeListBuilder.create()
                .texOffs(40, 49).addBox(-1.5F, 5.45F, -1.5F, 5F, 4F, 5F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(34, 33).addBox(-3F, -1.01F, -1.9F, 4F, 11F, 4F),
                PartPose.offset(4F, 14F, -10F));
        PartDefinition p_feather_left_front_leg = p_left_front_leg.addOrReplaceChild("feather_left_front_leg", CubeListBuilder.create()
                .texOffs(61, 49).addBox(-3.5F, 5.45F, -2.4F, 5F, 4F, 5F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(51, 33).addBox(-1F, -1.01F, -1.9F, 4F, 11F, 4F),
                PartPose.offset(-4F, 14F, -10F));
        PartDefinition p_feather_right_front_leg = p_right_front_leg.addOrReplaceChild("feather_right_front_leg", CubeListBuilder.create()
                .texOffs(82, 49).addBox(-1.5F, 5.45F, -2.4F, 5F, 4F, 5F, new CubeDeformation(-0.05F, -0.15F, -0.05F)),
                PartPose.offset(0F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
