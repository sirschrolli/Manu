package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Meeresschildkröten (Knochen wie bei der Vanilla-Schildkröte).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SeaTurtleGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private SeaTurtleGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-2.5F, -1F, 0F, 5F, 5F, 3F)
                .texOffs(91, 41).addBox(-2.5F, -1F, -2F, 5F, 4F, 2F)
                .texOffs(106, 41).addBox(-1.5F, 0F, -3F, 3F, 3F, 1F)
                .texOffs(66, 50).addBox(-0.5F, 1F, -4F, 1F, 2F, 1F),
                PartPose.offset(0F, 19F, -10F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(29, 0).addBox(-9.5F, 4F, -10F, 19F, 18F, 2F)
                .texOffs(0, 50).addBox(-7.5F, 3F, -10F, 15F, 1F, 2F)
                .texOffs(35, 50).addBox(-6.5F, 22F, -10F, 13F, 1F, 2F)
                .texOffs(0, 22).addBox(-8.5F, 5F, -8F, 17F, 16F, 2F)
                .texOffs(39, 22).addBox(-6.5F, 7F, -6F, 13F, 12F, 2F)
                .texOffs(0, 0).addBox(-5.5F, 3F, -13F, 11F, 18F, 3F),
                PartPose.offsetAndRotation(0F, 11F, -10F, 1.5708F, 0F, 0F));
        PartDefinition p_egg_belly = root.addOrReplaceChild("egg_belly", CubeListBuilder.create()
                .texOffs(72, 0).addBox(-4.5F, 3F, -14F, 9F, 18F, 1F),
                PartPose.offsetAndRotation(0F, 11F, -10F, 1.5708F, 0F, 0F));
        PartDefinition p_right_hind_leg = root.addOrReplaceChild("right_hind_leg", CubeListBuilder.create()
                .texOffs(70, 22).addBox(-2F, 0F, 0F, 4F, 1F, 10F),
                PartPose.offset(-3.5F, 22F, 11F));
        PartDefinition p_left_hind_leg = root.addOrReplaceChild("left_hind_leg", CubeListBuilder.create()
                .texOffs(99, 22).addBox(-2F, 0F, 0F, 4F, 1F, 10F),
                PartPose.offset(3.5F, 22F, 11F));
        PartDefinition p_right_front_leg = root.addOrReplaceChild("right_front_leg", CubeListBuilder.create()
                .texOffs(17, 41).addBox(-13F, 0F, -2F, 13F, 1F, 5F),
                PartPose.offset(-5F, 21F, -4F));
        PartDefinition p_left_front_leg = root.addOrReplaceChild("left_front_leg", CubeListBuilder.create()
                .texOffs(54, 41).addBox(0F, 0F, -2F, 13F, 1F, 5F),
                PartPose.offset(5F, 21F, -4F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
