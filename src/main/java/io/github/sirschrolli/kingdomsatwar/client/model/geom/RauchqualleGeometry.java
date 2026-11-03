package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Rauchqualle (Körper und Fangarme wie beim Vanilla-Ghast, dazu Haube und Kranz; im Spiel ×4,5).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class RauchqualleGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private RauchqualleGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -8F, -8F, 16F, 16F, 16F)
                .texOffs(0, 33).addBox(-9.5F, 0.875F, -9.5F, 19F, 9F, 19F, new CubeDeformation(0F, -0.125F, 0F))
                .texOffs(0, 62).addBox(-9.5F, -9.5F, -9.5F, 19F, 6F, 19F, new CubeDeformation(-0.25F, -0.25F, -0.25F)),
                PartPose.offset(0F, 17.6F, 0F));
        PartDefinition p_tentacle0 = root.addOrReplaceChild("tentacle0", CubeListBuilder.create()
                .texOffs(9, 88).addBox(-1F, 0F, -1F, 2F, 9F, 2F),
                PartPose.offset(-3.75F, 24.6F, -5F));
        PartDefinition p_tentacle1 = root.addOrReplaceChild("tentacle1", CubeListBuilder.create()
                .texOffs(86, 62).addBox(-1F, 0F, -1F, 2F, 12F, 2F),
                PartPose.offset(1.25F, 24.6F, -5F));
        PartDefinition p_tentacle2 = root.addOrReplaceChild("tentacle2", CubeListBuilder.create()
                .texOffs(27, 88).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(6.25F, 24.6F, -5F));
        PartDefinition p_tentacle3 = root.addOrReplaceChild("tentacle3", CubeListBuilder.create()
                .texOffs(104, 62).addBox(-1F, 0F, -1F, 2F, 11F, 2F),
                PartPose.offset(-6.25F, 24.6F, 0F));
        PartDefinition p_tentacle4 = root.addOrReplaceChild("tentacle4", CubeListBuilder.create()
                .texOffs(77, 62).addBox(-1F, 0F, -1F, 2F, 13F, 2F),
                PartPose.offset(-1.25F, 24.6F, 0F));
        PartDefinition p_tentacle5 = root.addOrReplaceChild("tentacle5", CubeListBuilder.create()
                .texOffs(0, 88).addBox(-1F, 0F, -1F, 2F, 10F, 2F),
                PartPose.offset(3.75F, 24.6F, 0F));
        PartDefinition p_tentacle6 = root.addOrReplaceChild("tentacle6", CubeListBuilder.create()
                .texOffs(95, 62).addBox(-1F, 0F, -1F, 2F, 12F, 2F),
                PartPose.offset(-3.75F, 24.6F, 5F));
        PartDefinition p_tentacle7 = root.addOrReplaceChild("tentacle7", CubeListBuilder.create()
                .texOffs(18, 88).addBox(-1F, 0F, -1F, 2F, 9F, 2F),
                PartPose.offset(1.25F, 24.6F, 5F));
        PartDefinition p_tentacle8 = root.addOrReplaceChild("tentacle8", CubeListBuilder.create()
                .texOffs(113, 62).addBox(-1F, 0F, -1F, 2F, 11F, 2F),
                PartPose.offset(6.25F, 24.6F, 5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
