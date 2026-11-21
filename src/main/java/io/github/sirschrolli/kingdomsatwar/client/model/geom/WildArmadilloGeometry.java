package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Gürteltiere (mit eingerollter Kugel).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class WildArmadilloGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private WildArmadilloGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -7F, -10F, 8F, 8F, 12F, new CubeDeformation(0.3F))
                .texOffs(41, 0).addBox(-4F, -7F, -10F, 8F, 8F, 12F),
                PartPose.offset(0F, 21F, 4F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(11, 21).addBox(-0.5F, -0.0865F, 0.0933F, 1F, 6F, 1F),
                PartPose.offsetAndRotation(0F, -3F, 1F, 0.5061F, 0F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create(),
                PartPose.offset(0F, -2F, -11F));
        PartDefinition p_head_cube = p_head.addOrReplaceChild("head_cube", CubeListBuilder.create()
                .texOffs(0, 21).addBox(-1.5F, -1F, -1F, 3F, 5F, 2F),
                PartPose.offsetAndRotation(0F, 0F, 0F, -0.3927F, 0F, 0F));
        PartDefinition p_right_ear = p_head.addOrReplaceChild("right_ear", CubeListBuilder.create(),
                PartPose.offset(-1F, -1F, 0F));
        PartDefinition p_right_ear_cube = p_right_ear.addOrReplaceChild("right_ear_cube", CubeListBuilder.create()
                .texOffs(52, 21).addBox(-2F, -3F, 0F, 2F, 5F, 0F),
                PartPose.offsetAndRotation(-0.5F, 0F, -0.6F, 0.1885F, -0.3857F, -0.0716F));
        PartDefinition p_left_ear = p_head.addOrReplaceChild("left_ear", CubeListBuilder.create(),
                PartPose.offset(1F, -2F, 0F));
        PartDefinition p_left_ear_cube = p_left_ear.addOrReplaceChild("left_ear_cube", CubeListBuilder.create()
                .texOffs(57, 21).addBox(0F, -3F, 0F, 2F, 5F, 0F),
                PartPose.offsetAndRotation(0.5F, 1F, -0.6F, 0.1885F, 0.3857F, 0.0716F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(16, 21).addBox(-1F, 0F, -1F, 2F, 3F, 2F),
                PartPose.offset(-2F, 21F, 4F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(25, 21).addBox(-1F, 0F, -1F, 2F, 3F, 2F),
                PartPose.offset(2F, 21F, 4F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(34, 21).addBox(-1F, 0F, -1F, 2F, 3F, 2F),
                PartPose.offset(-2F, 21F, -4F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(43, 21).addBox(-1F, 0F, -1F, 2F, 3F, 2F),
                PartPose.offset(2F, 21F, -4F));
        PartDefinition p_cube = root.addOrReplaceChild("cube", CubeListBuilder.create()
                .texOffs(82, 0).addBox(-5F, -10F, -6F, 10F, 10F, 10F),
                PartPose.offset(0F, 24F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
