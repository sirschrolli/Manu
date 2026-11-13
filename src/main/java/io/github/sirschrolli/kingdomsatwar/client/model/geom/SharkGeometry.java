package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Hais (Kopf mit aufklappbarem Kiefer, Schwanz in zwei Gliedern).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class SharkGeometry {
    public static final int TEXTURE_WIDTH = 160;
    public static final int TEXTURE_HEIGHT = 96;

    private SharkGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4F, -4F, -8F, 8F, 8F, 16F)
                .texOffs(107, 0).addBox(-3F, -4.9F, -6F, 6F, 1F, 12F, new CubeDeformation(0F, -0.1F, 0F))
                .texOffs(0, 25).addBox(-3F, 3.9F, -6.5F, 6F, 1F, 11F, new CubeDeformation(0F, -0.1F, 0F)),
                PartPose.offset(0F, 18F, 0F));
        PartDefinition p_head = p_body.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(49, 0).addBox(-4F, -3.9F, -6.75F, 8F, 7F, 7F, new CubeDeformation(-0.4F, -0.3F, -0.25F))
                .texOffs(103, 25).addBox(-3F, -3.2F, -9.5F, 6F, 5F, 3F, new CubeDeformation(-0.4F, -0.2F, 0F))
                .texOffs(25, 38).addBox(-2F, -2.8F, -11.25F, 4F, 4F, 2F, new CubeDeformation(-0.4F, -0.4F, -0.25F))
                .texOffs(116, 38).addBox(-2.5F, 1.4F, -9.4F, 5F, 1F, 1F, new CubeDeformation(-0.1F, -0.2F, -0.2F))
                .texOffs(73, 38).addBox(3.4F, -2.2F, -6.4F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.4F))
                .texOffs(80, 38).addBox(-4.4F, -2.2F, -6.4F, 1F, 2F, 2F, new CubeDeformation(-0.2F, -0.4F, -0.4F)),
                PartPose.offset(0F, -0.2F, -8F));
        PartDefinition p_jaw = p_head.addOrReplaceChild("jaw", CubeListBuilder.create()
                .texOffs(78, 25).addBox(-3F, -0.2F, -6F, 6F, 2F, 6F, new CubeDeformation(0F, -0.2F, 0F))
                .texOffs(101, 38).addBox(-3F, -0.8F, -6F, 6F, 1F, 1F, new CubeDeformation(-0.4F, -0.2F, -0.2F)),
                PartPose.offset(0F, 2.8F, -3F));
        PartDefinition p_dorsal = p_body.addOrReplaceChild("dorsal", CubeListBuilder.create()
                .texOffs(35, 25).addBox(-1F, -6.75F, -1.5F, 2F, 7F, 4F, new CubeDeformation(-0.4F, -0.25F, 0F)),
                PartPose.offsetAndRotation(0F, -4.6F, -2.5F, -0.4887F, 0F, 0F));
        PartDefinition p_dorsal_tip = p_dorsal.addOrReplaceChild("dorsal_tip", CubeListBuilder.create()
                .texOffs(38, 38).addBox(-0.5F, -2.75F, -1.4F, 1F, 3F, 3F, new CubeDeformation(-0.1F, -0.25F, -0.4F)),
                PartPose.offsetAndRotation(0F, -6.5F, 0F, -0.3491F, 0F, 0F));
        PartDefinition p_left_pectoral = p_body.addOrReplaceChild("left_pectoral", CubeListBuilder.create()
                .texOffs(122, 25).addBox(0F, -0.5F, -1.25F, 7F, 1F, 5F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(3.6F, 2.8F, -4F, 0.3142F, 0F, 0.5585F));
        PartDefinition p_left_pelvic = p_body.addOrReplaceChild("left_pelvic", CubeListBuilder.create()
                .texOffs(47, 38).addBox(0F, -0.5F, -0.75F, 3F, 1F, 3F, new CubeDeformation(0F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(2.2F, 3.6F, 5.5F, 0.3491F, 0F, 0.4363F));
        PartDefinition p_right_pectoral = p_body.addOrReplaceChild("right_pectoral", CubeListBuilder.create()
                .texOffs(0, 38).addBox(-7F, -0.5F, -1.25F, 7F, 1F, 5F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offsetAndRotation(-3.6F, 2.8F, -4F, 0.3142F, 0F, -0.5585F));
        PartDefinition p_right_pelvic = p_body.addOrReplaceChild("right_pelvic", CubeListBuilder.create()
                .texOffs(60, 38).addBox(-3F, -0.5F, -0.75F, 3F, 1F, 3F, new CubeDeformation(0F, -0.1F, -0.25F)),
                PartPose.offsetAndRotation(-2.2F, 3.6F, 5.5F, 0.3491F, 0F, -0.4363F));
        PartDefinition p_tail = p_body.addOrReplaceChild("tail", CubeListBuilder.create()
                .texOffs(80, 0).addBox(-3F, -3.5F, 0F, 6F, 7F, 7F, new CubeDeformation(0F, -0.3F, 0F))
                .texOffs(87, 38).addBox(-0.5F, -5.2F, 3F, 1F, 2F, 2F, new CubeDeformation(-0.1F, 0F, 0F))
                .texOffs(94, 38).addBox(-0.5F, 3F, 3.5F, 1F, 2F, 2F, new CubeDeformation(-0.1F, -0.2F, 0F)),
                PartPose.offset(0F, -0.4F, 8F));
        PartDefinition p_tail_end = p_tail.addOrReplaceChild("tail_end", CubeListBuilder.create()
                .texOffs(48, 25).addBox(-2F, -2F, 0F, 4F, 4F, 6F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 0F, 7F));
        PartDefinition p_caudal = p_tail_end.addOrReplaceChild("caudal", CubeListBuilder.create(),
                PartPose.offset(0F, 0F, 5.5F));
        PartDefinition p_caudal_upper = p_caudal.addOrReplaceChild("caudal_upper", CubeListBuilder.create()
                .texOffs(144, 0).addBox(-0.5F, -9F, -1.3F, 1F, 9F, 4F, new CubeDeformation(0F, 0F, -0.3F)),
                PartPose.offsetAndRotation(0F, -1F, 0F, 0.6632F, 0F, 0F));
        PartDefinition p_caudal_lower = p_caudal.addOrReplaceChild("caudal_lower", CubeListBuilder.create()
                .texOffs(69, 25).addBox(-0.5F, -0.25F, -1.1F, 1F, 6F, 3F, new CubeDeformation(0F, -0.25F, -0.1F)),
                PartPose.offsetAndRotation(0F, 1F, 0F, -0.6981F, 0F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
