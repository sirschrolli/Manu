package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Pandas (je Wesenszug eine Fellzeichnung).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildPandaGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private WildPandaGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(65, 0).addBox(-6.5F, -5F, -4F, 13F, 10F, 9F)
                .texOffs(100, 40).addBox(-3.5F, 0F, -6F, 7F, 5F, 2F)
                .texOffs(39, 56).addBox(-1.5F, -0.1F, -6.8F, 3F, 2F, 1F, new CubeDeformation(0F, -0.3F, -0.2F)),
                PartPose.offset(0F, 11.5F, -17F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(0, 56).addBox(-2.5F, -3.5F, -0.75F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(-6F, -5F, 0.5F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(13, 56).addBox(-1.5F, -3.5F, -0.75F, 4F, 4F, 2F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(6F, -5F, 0.5F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-9.5F, -13F, -6.5F, 19F, 26F, 13F),
                PartPose.offsetAndRotation(0F, 10F, 0F, 1.5708F, 0F, 0F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(26, 56).addBox(-1.5F, -0.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, 13F, 3F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-3F, 0F, -3F, 6F, 9F, 6F),
                PartPose.offset(-5.5F, 15F, 9F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(25, 40).addBox(-3F, 0F, -3F, 6F, 9F, 6F),
                PartPose.offset(5.5F, 15F, 9F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(50, 40).addBox(-3F, 0F, -3F, 6F, 9F, 6F),
                PartPose.offset(-5.5F, 15F, -9F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(75, 40).addBox(-3F, 0F, -3F, 6F, 9F, 6F),
                PartPose.offset(5.5F, 15F, -9F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
