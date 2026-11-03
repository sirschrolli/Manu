package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Urwühlers (Knochen wie beim Vanilla-Schnüffler; Fellbehang, Buckel, Moosmatte, Backenbart).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class PrimevalDiggerGeometry {
    public static final int TEXTURE_WIDTH = 272;
    public static final int TEXTURE_HEIGHT = 256;

    private PrimevalDiggerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_bone = root.addOrReplaceChild("bone", CubeListBuilder.create(),
                PartPose.offset(0F, 5F, 0F));
        PartDefinition p_body = p_bone.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-12.5F, -14F, -20F, 25F, 29F, 40F)
                .texOffs(0, 70).addBox(-12.5F, 4.75F, -20F, 25F, 13F, 40F, new CubeDeformation(0.3F, 0.05F, 0.3F))
                .texOffs(131, 0).addBox(-12.5F, -14F, -20F, 25F, 26F, 40F, new CubeDeformation(0.65F))
                .texOffs(131, 70).addBox(-12.5F, -14F, -20F, 25F, 13F, 40F, new CubeDeformation(1.1F))
                .texOffs(0, 154).addBox(-11.5F, -18.4F, -20.4F, 23F, 5F, 15F, new CubeDeformation(0F, -0.4F, 0F))
                .texOffs(83, 124).addBox(-10.5F, -16.2F, -4F, 21F, 2F, 22F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(42, 192).addBox(-7.5F, -19.9F, 0F, 3F, 4F, 0F)
                .texOffs(154, 175).addBox(-6F, -19.9F, -1.5F, 0F, 4F, 3F)
                .texOffs(49, 192).addBox(3.5F, -19.65F, 4F, 3F, 4F, 0F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(161, 175).addBox(5F, -19.65F, 2.5F, 0F, 4F, 3F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(56, 192).addBox(-5F, -18.9F, 10F, 3F, 3F, 0F)
                .texOffs(168, 175).addBox(-3.5F, -18.9F, 8.5F, 0F, 3F, 3F)
                .texOffs(175, 175).addBox(5F, -20.65F, 13.5F, 3F, 5F, 0F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(129, 175).addBox(6.5F, -20.65F, 12F, 0F, 5F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 124).addBox(-6.5F, -7.5F, -11.5F, 13F, 18F, 11F)
                .texOffs(92, 175).addBox(-7F, -8.8F, -12.7F, 14F, 6F, 4F, new CubeDeformation(0F, -0.4F, -0.4F))
                .texOffs(0, 175).addBox(-7.5F, 2.75F, -9F, 15F, 8F, 8F, new CubeDeformation(-0.4F, -0.25F, 0F)),
                PartPose.offset(0F, 6.5F, -19.48F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(49, 124).addBox(0F, 0F, -3F, 1F, 19F, 7F),
                PartPose.offset(6.51F, -7.5F, -4.51F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(66, 124).addBox(-1F, 0F, -3F, 1F, 19F, 7F),
                PartPose.offset(-6.51F, -7.5F, -4.51F));
        PartDefinition p_nose = p_head.addOrReplaceChild("nose", CubeListBuilder.create()
                .texOffs(47, 175).addBox(-6.5F, -2F, -9F, 13F, 2F, 9F),
                PartPose.offset(0F, -4.5F, -11.5F));
        PartDefinition p_lower_beak = p_head.addOrReplaceChild("lower_beak", CubeListBuilder.create()
                .texOffs(170, 124).addBox(-6.5F, -7F, -8F, 13F, 12F, 9F)
                .texOffs(136, 175).addBox(-6.4F, 2.6F, -8.8F, 2F, 5F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F))
                .texOffs(145, 175).addBox(4.4F, 2.6F, -8.8F, 2F, 5F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.2F)),
                PartPose.offset(0F, 2.5F, -12.5F));
        PartDefinition p_right_front_leg = p_bone.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(77, 154).addBox(-3.5F, -1F, -4F, 7F, 10F, 8F)
                .texOffs(182, 175).addBox(-4F, 7.2F, -5.5F, 8F, 2F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(-7.5F, 10F, -15F));
        PartDefinition p_right_mid_leg = p_bone.addOrReplaceChild("right_mid_leg", CubeListBuilder.create()
                .texOffs(108, 154).addBox(-3.5F, -1F, -4F, 7F, 10F, 8F)
                .texOffs(203, 175).addBox(-4F, 7.2F, -5.5F, 8F, 2F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(-7.5F, 10F, 0F));
        PartDefinition p_right_hind_leg = p_bone.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(139, 154).addBox(-3.5F, -1F, -4F, 7F, 10F, 8F)
                .texOffs(224, 175).addBox(-4F, 7.2F, -5.5F, 8F, 2F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(-7.5F, 10F, 15F));
        PartDefinition p_left_front_leg = p_bone.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(170, 154).addBox(-3.5F, -1F, -4F, 7F, 10F, 8F)
                .texOffs(245, 175).addBox(-4F, 7.2F, -5.5F, 8F, 2F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(7.5F, 10F, -15F));
        PartDefinition p_left_mid_leg = p_bone.addOrReplaceChild("left_mid_leg", CubeListBuilder.create()
                .texOffs(201, 154).addBox(-3.5F, -1F, -4F, 7F, 10F, 8F)
                .texOffs(0, 192).addBox(-4F, 7.2F, -5.5F, 8F, 2F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(7.5F, 10F, 0F));
        PartDefinition p_left_hind_leg = p_bone.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(232, 154).addBox(-3.5F, -1F, -4F, 7F, 10F, 8F)
                .texOffs(21, 192).addBox(-4F, 7.2F, -5.5F, 8F, 2F, 2F, new CubeDeformation(-0.3F, -0.2F, -0.2F)),
                PartPose.offset(7.5F, 10F, 15F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
