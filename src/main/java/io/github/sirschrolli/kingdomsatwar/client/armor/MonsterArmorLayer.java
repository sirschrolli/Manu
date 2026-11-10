package io.github.sirschrolli.kingdomsatwar.client.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.MonsterGear;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

/**
 * Die 3D-Teile der Monsterrüstungen (0.70), getragen über der gewöhnlichen Rüstung:
 * <ul>
 *   <li><b>Wolfskopf-Haube:</b> spitze Ohren, die Wolfsschnauze über der Stirn mit Nase und Fängen;</li>
 *   <li><b>Pelzmantel:</b> ein dichter Pelzkragen und Pelzbüschel auf den Schultern;</li>
 *   <li><b>Glutwurm-Hörnerhelm:</b> zwei geschwungene Hörner und ein Kamm, an den Spitzen glühend;</li>
 *   <li><b>Glutwurm-Schuppenpanzer:</b> drei Stacheln entlang des Rückgrats, glühende Spitzen.</li>
 * </ul>
 * Glühende Stellen kommen aus einer eigenen Leuchtebene (leuchten auch im Dunkeln).
 */
public class MonsterArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(KingdomsAtWar.id("monster_armor_parts"), "main");
    private static final Identifier PELT = KingdomsAtWar.id("textures/entity/equipment_parts/werewolf_pelt.png");
    private static final Identifier EMBER = KingdomsAtWar.id("textures/entity/equipment_parts/ember_scale.png");
    private static final Identifier EMBER_GLOW = KingdomsAtWar.id("textures/entity/equipment_parts/ember_scale_glow.png");

    private final ModelPart peltHead;
    private final ModelPart peltBody;
    private final ModelPart peltLeftArm;
    private final ModelPart peltRightArm;
    private final ModelPart emberHead;
    private final ModelPart emberBody;

    public MonsterArmorLayer(RenderLayerParent<S, M> parent, ModelPart root) {
        super(parent);
        this.peltHead = root.getChild("pelt_head");
        this.peltBody = root.getChild("pelt_body");
        this.peltLeftArm = root.getChild("pelt_left_arm");
        this.peltRightArm = root.getChild("pelt_right_arm");
        this.emberHead = root.getChild("ember_head");
        this.emberBody = root.getChild("ember_body");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        CubeDeformation fur = new CubeDeformation(0.15f);
        // Wolfskopf: Ohren auf der Haube, die Schnauze über der Stirn (die Haube ist um einen Pixel aufgeblasen)
        root.addOrReplaceChild("pelt_head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-4.6f, -12.2f, -0.6f, 3.0f, 3.0f, 1.0f, fur)
                .texOffs(8, 0).addBox(1.6f, -12.2f, -0.6f, 3.0f, 3.0f, 1.0f, fur)
                .texOffs(0, 4).addBox(-2.0f, -9.8f, -9.2f, 4.0f, 3.0f, 4.0f, fur)
                .texOffs(16, 4).addBox(-1.0f, -10.2f, -9.8f, 2.0f, 1.0f, 1.0f)
                .texOffs(16, 0).addBox(-1.8f, -6.9f, -8.9f, 1.0f, 1.0f, 1.0f)
                .texOffs(20, 0).addBox(0.8f, -6.9f, -8.9f, 1.0f, 1.0f, 1.0f), PartPose.ZERO);
        // Pelzkragen um den Hals, Pelzbüschel auf den Schultern
        root.addOrReplaceChild("pelt_body", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-5.0f, -0.8f, -3.0f, 10.0f, 3.0f, 6.0f, new CubeDeformation(0.6f)), PartPose.ZERO);
        root.addOrReplaceChild("pelt_left_arm", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-1.5f, -3.4f, -2.5f, 5.0f, 3.0f, 5.0f, new CubeDeformation(0.3f)), PartPose.ZERO);
        root.addOrReplaceChild("pelt_right_arm", CubeListBuilder.create()
                .texOffs(32, 8).addBox(-3.5f, -3.4f, -2.5f, 5.0f, 3.0f, 5.0f, new CubeDeformation(0.3f)), PartPose.ZERO);
        // Glutwurm-Hörner: je drei Glieder, nach hinten und außen geschwungen; dazu ein Kamm über den Scheitel
        PartDefinition ember = root.addOrReplaceChild("ember_head", CubeListBuilder.create()
                .texOffs(24, 0).addBox(-1.0f, -10.4f, -4.0f, 2.0f, 1.5f, 8.0f), PartPose.ZERO);
        horn(ember, "left_horn", -3.2f, -0.38f, 0, 0);
        horn(ember, "right_horn", 3.2f, 0.38f, 0, 5);
        // Stacheln entlang des Rückgrats
        PartDefinition back = root.addOrReplaceChild("ember_body", CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < 3; i++) {
            float size = 1.0f - i * 0.15f;
            PartDefinition spike = back.addOrReplaceChild("spike" + i, CubeListBuilder.create()
                            .texOffs(0, 12 + i * 5).addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(size * 0.2f - 0.2f)),
                    PartPose.offsetAndRotation(0.0f, 1.5f + i * 3.6f, 3.0f, 0.55f, 0.0f, 0.0f));
            spike.addOrReplaceChild("tip", CubeListBuilder.create()
                    .texOffs(8, 12 + i * 5).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 2.0f), PartPose.offset(0.0f, 0.0f, 1.8f));
        }
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static void horn(PartDefinition head, String name, float x, float outward, int u, int v) {
        PartDefinition base = head.addOrReplaceChild(name, CubeListBuilder.create()
                        .texOffs(u, v).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 3.0f, 2.0f),
                PartPose.offsetAndRotation(x, -8.6f, -1.2f, -0.5f, 0.0f, outward));
        PartDefinition mid = base.addOrReplaceChild("mid", CubeListBuilder.create()
                        .texOffs(u + 8, v).addBox(-0.75f, -2.5f, -0.75f, 1.5f, 2.5f, 1.5f),
                PartPose.offsetAndRotation(0.0f, -2.8f, 0.0f, -0.55f, 0.0f, outward * 0.5f));
        mid.addOrReplaceChild("tip", CubeListBuilder.create()
                        .texOffs(u + 14, v).addBox(-0.5f, -2.0f, -0.5f, 1.0f, 2.0f, 1.0f),
                PartPose.offsetAndRotation(0.0f, -2.4f, 0.0f, -0.6f, 0.0f, 0.0f));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        M model = this.getParentModel();
        ItemStack head = state.headEquipment;
        ItemStack chest = state.chestEquipment;
        if (head.is(MonsterGear.PELT_HELMET.get())) {
            this.part(poseStack, collector, light, model.head, this.peltHead, PELT, null);
        } else if (head.is(MonsterGear.EMBER_HELMET.get())) {
            this.part(poseStack, collector, light, model.head, this.emberHead, EMBER, EMBER_GLOW);
        }
        if (chest.is(MonsterGear.PELT_CHESTPLATE.get())) {
            this.part(poseStack, collector, light, model.body, this.peltBody, PELT, null);
            this.part(poseStack, collector, light, model.leftArm, this.peltLeftArm, PELT, null);
            this.part(poseStack, collector, light, model.rightArm, this.peltRightArm, PELT, null);
        } else if (chest.is(MonsterGear.EMBER_CHESTPLATE.get())) {
            this.part(poseStack, collector, light, model.body, this.emberBody, EMBER, EMBER_GLOW);
        }
    }

    private void part(PoseStack poseStack, SubmitNodeCollector collector, int light, ModelPart bone, ModelPart part, Identifier texture,
            Identifier glow) {
        poseStack.pushPose();
        bone.translateAndRotate(poseStack);
        collector.submitModelPart(part, poseStack, RenderTypes.entityCutout(texture), light, OverlayTexture.NO_OVERLAY, null);
        if (glow != null) {
            collector.submitModelPart(part, poseStack, RenderTypes.eyes(glow), light, OverlayTexture.NO_OVERLAY, null);
        }
        poseStack.popPose();
    }
}
