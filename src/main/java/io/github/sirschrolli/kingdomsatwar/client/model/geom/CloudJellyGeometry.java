package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie der Wolkenqualle (Körper und Fangarme wie beim glücklichen Ghast, dazu der Ballenkranz; im Spiel ×4).
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class CloudJellyGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private CloudJellyGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-8F, -8F, -8F, 16F, 16F, 16F)
                .texOffs(0, 33).addBox(-9F, 3F, -9F, 18F, 6F, 18F, new CubeDeformation(-0.25F, 0F, -0.25F)),
                PartPose.offset(0F, 16F, 0F));
        PartDefinition p_tentacle0 = p_body.addOrReplaceChild("tentacle0", CubeListBuilder.create()
                .texOffs(109, 33).addBox(-1F, 0F, -1F, 2F, 5F, 2F),
                PartPose.offset(-3.75F, 7F, -5F));
        PartDefinition p_tentacle1 = p_body.addOrReplaceChild("tentacle1", CubeListBuilder.create()
                .texOffs(91, 33).addBox(-1F, 0F, -1F, 2F, 7F, 2F),
                PartPose.offset(1.25F, 7F, -5F));
        PartDefinition p_tentacle2 = p_body.addOrReplaceChild("tentacle2", CubeListBuilder.create()
                .texOffs(18, 58).addBox(-1F, 0F, -1F, 2F, 4F, 2F),
                PartPose.offset(6.25F, 7F, -5F));
        PartDefinition p_tentacle3 = p_body.addOrReplaceChild("tentacle3", CubeListBuilder.create()
                .texOffs(118, 33).addBox(-1F, 0F, -1F, 2F, 5F, 2F),
                PartPose.offset(-6.25F, 7F, 0F));
        PartDefinition p_tentacle4 = p_body.addOrReplaceChild("tentacle4", CubeListBuilder.create()
                .texOffs(0, 58).addBox(-1F, 0F, -1F, 2F, 5F, 2F),
                PartPose.offset(-1.25F, 7F, 0F));
        PartDefinition p_tentacle5 = p_body.addOrReplaceChild("tentacle5", CubeListBuilder.create()
                .texOffs(100, 33).addBox(-1F, 0F, -1F, 2F, 7F, 2F),
                PartPose.offset(3.75F, 7F, 0F));
        PartDefinition p_tentacle6 = p_body.addOrReplaceChild("tentacle6", CubeListBuilder.create()
                .texOffs(73, 33).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(-3.75F, 7F, 5F));
        PartDefinition p_tentacle7 = p_body.addOrReplaceChild("tentacle7", CubeListBuilder.create()
                .texOffs(82, 33).addBox(-1F, 0F, -1F, 2F, 8F, 2F),
                PartPose.offset(1.25F, 7F, 5F));
        PartDefinition p_tentacle8 = p_body.addOrReplaceChild("tentacle8", CubeListBuilder.create()
                .texOffs(9, 58).addBox(-1F, 0F, -1F, 2F, 5F, 2F),
                PartPose.offset(6.25F, 7F, 5F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
