package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Tintenfische und Tiefseekraken (Knochen wie beim Vanilla-Tintenfisch).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class InkfishGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private InkfishGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-5F, -6F, -5F, 10F, 10F, 10F, new CubeDeformation(0.02F))
                .texOffs(37, 21).addBox(-4F, -10F, -4F, 8F, 4F, 8F)
                .texOffs(104, 21).addBox(-2.5F, -13F, -2.5F, 5F, 3F, 5F)
                .texOffs(0, 21).addBox(-4.5F, 4F, -4.5F, 9F, 4F, 9F, new CubeDeformation(0.02F))
                .texOffs(0, 35).addBox(-5.5F, 4.5F, -2F, 1F, 3F, 4F)
                .texOffs(11, 35).addBox(4.5F, 4.5F, -2F, 1F, 3F, 4F)
                .texOffs(70, 21).addBox(-11F, -14F, -0.5F, 7F, 11F, 1F)
                .texOffs(87, 21).addBox(4F, -14F, -0.5F, 7F, 11F, 1F),
                PartPose.offset(0F, 8F, 0F));
        PartDefinition p_tentacle0 = root.addOrReplaceChild("tentacle0", CubeListBuilder.create()
                .texOffs(41, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(5F, 15F, 0F, 0F, 1.5708F, 0F));
        PartDefinition p_tentacle1 = root.addOrReplaceChild("tentacle1", CubeListBuilder.create()
                .texOffs(50, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(3.5355F, 15F, 3.5355F, 0F, 0.7854F, 0F));
        PartDefinition p_tentacle2 = root.addOrReplaceChild("tentacle2", CubeListBuilder.create()
                .texOffs(59, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offset(0F, 15F, 5F));
        PartDefinition p_tentacle3 = root.addOrReplaceChild("tentacle3", CubeListBuilder.create()
                .texOffs(68, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(-3.5355F, 15F, 3.5355F, 0F, -0.7854F, 0F));
        PartDefinition p_tentacle4 = root.addOrReplaceChild("tentacle4", CubeListBuilder.create()
                .texOffs(77, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(-5F, 15F, 0F, 0F, -1.5708F, 0F));
        PartDefinition p_tentacle5 = root.addOrReplaceChild("tentacle5", CubeListBuilder.create()
                .texOffs(86, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(-3.5355F, 15F, -3.5355F, 0F, -2.3562F, 0F));
        PartDefinition p_tentacle6 = root.addOrReplaceChild("tentacle6", CubeListBuilder.create()
                .texOffs(95, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(0F, 15F, -5F, 0F, -3.1416F, 0F));
        PartDefinition p_tentacle7 = root.addOrReplaceChild("tentacle7", CubeListBuilder.create()
                .texOffs(104, 0).addBox(-1F, 0F, -1F, 2F, 18F, 2F),
                PartPose.offsetAndRotation(3.5355F, 15F, -3.5355F, 0F, -3.927F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
