package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Pendels des Stundenwächters (seit 1.01): ein Stück der Stange (ein Block, nach unten), das Lager (ein Zahnrad quer zu x) und die Klinge – eine Pendelscheibe mit Stundenmarken und eine Sichel aus gebläutem Stahl.
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class ClockPendulumGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 128;

    private ClockPendulumGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_rod = root.addOrReplaceChild("rod", CubeListBuilder.create()
                .texOffs(114, 0).addBox(-1F, 0F, -1F, 2F, 16F, 2F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_cap = root.addOrReplaceChild("cap", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1.5F, -10F, -10F, 3F, 20F, 20F)
                .texOffs(33, 61).addBox(-2.5F, -2F, -2F, 5F, 4F, 4F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_blade = root.addOrReplaceChild("blade", CubeListBuilder.create()
                .texOffs(47, 0).addBox(-15F, 0F, -1.5F, 30F, 30F, 3F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_blade_0 = p_blade.addOrReplaceChild("blade_0", CubeListBuilder.create()
                .texOffs(0, 41).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-24.7275F, 23.0344F, 0F, 0F, 0F, 1.2566F));
        PartDefinition p_blade_1 = p_blade.addOrReplaceChild("blade_1", CubeListBuilder.create()
                .texOffs(33, 41).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-19.3218F, 32.3974F, 0F, 0F, 0F, 0.8378F));
        PartDefinition p_blade_2 = p_blade.addOrReplaceChild("blade_2", CubeListBuilder.create()
                .texOffs(66, 41).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(-10.5752F, 38.7522F, 0F, 0F, 0F, 0.4189F));
        PartDefinition p_blade_3 = p_blade.addOrReplaceChild("blade_3", CubeListBuilder.create()
                .texOffs(0, 51).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offset(0F, 41F, 0F));
        PartDefinition p_blade_4 = p_blade.addOrReplaceChild("blade_4", CubeListBuilder.create()
                .texOffs(33, 51).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(10.5752F, 38.7522F, 0F, 0F, 0F, -0.4189F));
        PartDefinition p_blade_5 = p_blade.addOrReplaceChild("blade_5", CubeListBuilder.create()
                .texOffs(66, 51).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(19.3218F, 32.3974F, 0F, 0F, 0F, -0.8378F));
        PartDefinition p_blade_6 = p_blade.addOrReplaceChild("blade_6", CubeListBuilder.create()
                .texOffs(0, 61).addBox(-7F, -4F, -1F, 14F, 7F, 2F, new CubeDeformation(-0.4F, 0F, 0F)),
                PartPose.offsetAndRotation(24.7275F, 23.0344F, 0F, 0F, 0F, -1.2566F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
