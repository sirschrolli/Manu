package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Dörrkamele (Kamel-Skelett der Mod).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CamelHuskGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private CamelHuskGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-7.5F, -12F, -23.5F, 15F, 12F, 27F),
                PartPose.offset(0F, 4F, 9.5F));
        PartDefinition p_hump = p_body.addOrReplaceChild("hump", CubeListBuilder.create()
                .texOffs(50, 68).addBox(-4.5F, -5F, -5.5F, 9F, 5F, 11F),
                PartPose.offset(0F, -12F, -10F));
        PartDefinition p_hump2 = p_body.addOrReplaceChild("hump2", CubeListBuilder.create()
                .texOffs(0, 95).addBox(-4F, -5.5F, -11.5F, 8F, 5.5F, 6F)
                .texOffs(29, 95).addBox(-4F, -5.5F, 2.5F, 8F, 5.5F, 6F),
                PartPose.offset(0F, -12F, -10F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(91, 68).addBox(-1.5F, 0F, 0F, 3F, 14F, 0F),
                PartPose.offset(0F, -9F, 3.5F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-3.5F, -7F, -15F, 7F, 8F, 19F)
                .texOffs(21, 68).addBox(-3.5F, -21F, -15F, 7F, 14F, 7F)
                .texOffs(58, 95).addBox(-2.5F, -21F, -21F, 5F, 5F, 6F)
                .texOffs(98, 68).addBox(-3.9F, -12F, -15.3F, 7.8F, 6F, 6.6F),
                PartPose.offset(0F, -3F, -19.5F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create()
                .texOffs(81, 95).addBox(-0.5F, 0.5F, -1F, 3F, 1F, 2F),
                PartPose.offset(2.5F, -21F, -9.5F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create()
                .texOffs(92, 95).addBox(-2.5F, 0.5F, -1F, 3F, 1F, 2F),
                PartPose.offset(-2.5F, -21F, -9.5F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(53, 40).addBox(-2.5F, 2F, -2.5F, 5F, 21F, 5F),
                PartPose.offset(4.9F, 1F, 9.5F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(74, 40).addBox(-2.5F, 2F, -2.5F, 5F, 21F, 5F),
                PartPose.offset(-4.9F, 1F, 9.5F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(95, 40).addBox(-2.5F, 2F, -2.5F, 5F, 21F, 5F),
                PartPose.offset(4.9F, 1F, -10.5F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(0, 68).addBox(-2.5F, 2F, -2.5F, 5F, 21F, 5F),
                PartPose.offset(-4.9F, 1F, -10.5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
