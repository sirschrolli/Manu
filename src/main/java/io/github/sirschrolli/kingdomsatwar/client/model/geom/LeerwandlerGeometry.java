package io.github.sirschrolli.kingdomsatwar.client.model.geom;

import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Geometrie des Leerwandlers (Knochen wie beim Vanilla-Enderman, Kiefer als "hat", Splitterkranz "halo").
 *
 * <p>ERZEUGT von tools/generate_entities.py – nicht von Hand ändern.
 */
@SuppressWarnings("unused")
public final class LeerwandlerGeometry {
    public static final int TEXTURE_WIDTH = 128;
    public static final int TEXTURE_HEIGHT = 64;

    private LeerwandlerGeometry() {}

    public static MeshDefinition createMesh() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition p_head = root.addOrReplaceChild("head", CubeListBuilder.create()
                .texOffs(57, 0).addBox(-3.5F, -9.75F, -3.5F, 7F, 10F, 7F, new CubeDeformation(0F, -0.25F, 0F)),
                PartPose.offset(0F, -13F, 0F));
        PartDefinition p_hat = p_head.addOrReplaceChild("hat", CubeListBuilder.create()
                .texOffs(0, 33).addBox(-3F, -7F, -3F, 6F, 7F, 6F),
                PartPose.offset(0F, 0F, 0F));
        PartDefinition p_halo = p_head.addOrReplaceChild("halo", CubeListBuilder.create(),
                PartPose.offset(0F, -10.5F, 0F));
        PartDefinition p_shard_0 = p_halo.addOrReplaceChild("shard_0", CubeListBuilder.create()
                .texOffs(54, 33).addBox(-1F, -2F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(5F, -1F, 0F, 0F, 0F, 0.3142F));
        PartDefinition p_shard_1 = p_halo.addOrReplaceChild("shard_1", CubeListBuilder.create()
                .texOffs(63, 33).addBox(-1F, -2F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(-2.5F, 0.6F, 4.3301F, 0F, -2.0944F, -0.2443F));
        PartDefinition p_shard_2 = p_halo.addOrReplaceChild("shard_2", CubeListBuilder.create()
                .texOffs(72, 33).addBox(-1F, -2F, -1F, 2F, 4F, 2F, new CubeDeformation(-0.4F, -0.3F, -0.4F)),
                PartPose.offsetAndRotation(-2.5F, -0.2F, -4.3301F, 0F, -4.1888F, 0.4189F));
        PartDefinition p_body = root.addOrReplaceChild("body", CubeListBuilder.create()
                .texOffs(86, 0).addBox(-3.5F, 0F, -2F, 7F, 12F, 4F, new CubeDeformation(0F, 0F, -0.25F))
                .texOffs(25, 33).addBox(-5F, -0.85F, -2F, 10F, 2F, 4F, new CubeDeformation(0F, -0.25F, 0F))
                .texOffs(36, 0).addBox(-4.5F, -0.3F, 1.5F, 9F, 17F, 1F, new CubeDeformation(0F, 0F, -0.25F)),
                PartPose.offset(0F, -14F, 0F));
        PartDefinition p_right_arm = root.addOrReplaceChild("right_arm", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-1F, -2F, -1F, 2F, 30F, 2F)
                .texOffs(101, 33).addBox(-1.5F, 10.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(81, 33).addBox(-1.15F, 27.25F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(86, 33).addBox(0.15F, 27.25F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(26, 47).addBox(-0.5F, 27.4F, 0.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(-5F, -12F, 0F));
        PartDefinition p_right_leg = root.addOrReplaceChild("right_leg", CubeListBuilder.create()
                .texOffs(9, 0).addBox(-1F, 0F, -1F, 2F, 30F, 2F)
                .texOffs(114, 33).addBox(-1.5F, 12.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(36, 47).addBox(-1.5F, 28.25F, -2.6F, 3F, 2F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(-2F, -5F, 0F));
        PartDefinition p_left_arm = root.addOrReplaceChild("left_arm", CubeListBuilder.create()
                .texOffs(18, 0).addBox(-1F, -2F, -1F, 2F, 30F, 2F)
                .texOffs(0, 47).addBox(-1.5F, 10.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(91, 33).addBox(-1.15F, 27.25F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(96, 33).addBox(0.15F, 27.25F, -1.15F, 1F, 5F, 1F, new CubeDeformation(-0.2F, -0.25F, -0.2F))
                .texOffs(31, 47).addBox(-0.5F, 27.4F, 0.15F, 1F, 4F, 1F, new CubeDeformation(-0.2F, -0.1F, -0.2F)),
                PartPose.offset(5F, -12F, 0F));
        PartDefinition p_left_leg = root.addOrReplaceChild("left_leg", CubeListBuilder.create()
                .texOffs(27, 0).addBox(-1F, 0F, -1F, 2F, 30F, 2F)
                .texOffs(13, 47).addBox(-1.5F, 12.75F, -1.5F, 3F, 2F, 3F, new CubeDeformation(-0.25F, -0.25F, -0.25F))
                .texOffs(47, 47).addBox(-1.5F, 28.25F, -2.6F, 3F, 2F, 2F, new CubeDeformation(-0.4F, -0.25F, -0.2F)),
                PartPose.offset(2F, -5F, 0F));
        return mesh;
    }

    public static LayerDefinition createLayer() {
        return LayerDefinition.create(createMesh(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }
}
