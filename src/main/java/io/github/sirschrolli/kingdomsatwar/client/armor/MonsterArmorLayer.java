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
 *   <li><b>Wolfskopf-Haube:</b> der Wolfskopf sitzt auf dem Kopf – Brauenwulst, Schnauze mit Nase und Fängen, spitze
 *   Ohren; die Bernsteinaugen glühen (auch im Dunkeln);</li>
 *   <li><b>Pelzmantel:</b> ein dichter Pelzkragen, geschichtete Pelzbüschel auf den Schultern und ein Pelzumhang bis zur
 *   Hüfte, unten ausgefranst;</li>
 *   <li><b>Pelzbeinlinge:</b> ein buschiger Wolfsschwanz;</li>
 *   <li><b>Pelzstiefel:</b> Pelzstulpen;</li>
 *   <li><b>Glutwurm-Hörnerhelm:</b> zwei große, nach hinten geschwungene Hörner mit glühenden Spitzen, ein Kamm aus
 *   Flossen und eine Stirnplatte mit glühender Naht;</li>
 *   <li><b>Glutwurm-Schuppenpanzer:</b> vier Rückenstacheln und Schulterplatten mit je zwei Dornen, glühend an den
 *   Spitzen.</li>
 * </ul>
 * Glühende Stellen kommen aus einer eigenen Leuchtebene (additiv, leuchten auch im Dunkeln).
 */
public class MonsterArmorLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(KingdomsAtWar.id("monster_armor_parts"), "main");
    private static final Identifier PELT = KingdomsAtWar.id("textures/entity/equipment_parts/werewolf_pelt.png");
    private static final Identifier PELT_GLOW = KingdomsAtWar.id("textures/entity/equipment_parts/werewolf_pelt_glow.png");
    private static final Identifier EMBER = KingdomsAtWar.id("textures/entity/equipment_parts/ember_scale.png");
    private static final Identifier EMBER_GLOW = KingdomsAtWar.id("textures/entity/equipment_parts/ember_scale_glow.png");

    private final ModelPart peltHead;
    private final ModelPart peltBody;
    private final ModelPart peltLeftArm;
    private final ModelPart peltRightArm;
    private final ModelPart peltTail;
    private final ModelPart peltLeftLeg;
    private final ModelPart peltRightLeg;
    private final ModelPart emberHead;
    private final ModelPart emberBody;
    private final ModelPart emberLeftArm;
    private final ModelPart emberRightArm;

    public MonsterArmorLayer(RenderLayerParent<S, M> parent, ModelPart root) {
        super(parent);
        this.peltHead = root.getChild("pelt_head");
        this.peltBody = root.getChild("pelt_body");
        this.peltLeftArm = root.getChild("pelt_left_arm");
        this.peltRightArm = root.getChild("pelt_right_arm");
        this.peltTail = root.getChild("pelt_tail");
        this.peltLeftLeg = root.getChild("pelt_left_leg");
        this.peltRightLeg = root.getChild("pelt_right_leg");
        this.emberHead = root.getChild("ember_head");
        this.emberBody = root.getChild("ember_body");
        this.emberLeftArm = root.getChild("ember_left_arm");
        this.emberRightArm = root.getChild("ember_right_arm");
    }

    /** Texturlage (64x64) siehe tools/monster_gear_textures.py (pelt_parts, ember_parts). */
    public static LayerDefinition createLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        pelt(root);
        ember(root);
        return LayerDefinition.create(mesh, 64, 64);
    }

    private static void pelt(PartDefinition root) {
        CubeDeformation fur = new CubeDeformation(0.15f);
        // Der Wolfskopf sitzt auf der Haube (die Haube ist um einen Pixel aufgeblasen: vorn bei z = -5, oben bei y = -9).
        PartDefinition head = root.addOrReplaceChild("pelt_head", CubeListBuilder.create()
                .texOffs(0, 0).addBox(-2.0f, -9.2f, -9.0f, 4.0f, 3.0f, 4.0f, fur)                // Schnauze
                .texOffs(16, 0).addBox(-1.0f, -9.55f, -9.5f, 2.0f, 1.0f, 1.0f)                    // Nase
                .texOffs(24, 0).addBox(-4.5f, -9.6f, -5.6f, 9.0f, 1.0f, 1.0f, new CubeDeformation(0.1f)) // Brauenwulst
                .texOffs(28, 8).addBox(-1.6f, -6.4f, -8.7f, 1.0f, 2.0f, 1.0f, new CubeDeformation(-0.15f)) // Fänge
                .texOffs(34, 8).addBox(0.6f, -6.4f, -8.7f, 1.0f, 2.0f, 1.0f, new CubeDeformation(-0.15f))
                .texOffs(40, 8).addBox(-3.5f, -7.4f, -5.08f, 0.8f, 0.5f, 0.0f)                    // Augen (schmal, glühend)
                .texOffs(40, 10).addBox(2.7f, -7.4f, -5.08f, 0.8f, 0.5f, 0.0f), PartPose.ZERO);
        ear(head, "left_ear", -2.9f, -0.22f, 0);
        ear(head, "right_ear", 2.9f, 0.22f, 14);
        // Pelzkragen; darunter ein Pelzumhang über den Schultern bis zur Hüfte (zwei Bahnen, die untere ausgefranst)
        PartDefinition body = root.addOrReplaceChild("pelt_body", CubeListBuilder.create()
                .texOffs(0, 12).addBox(-5.0f, -0.9f, -3.0f, 10.0f, 3.0f, 6.0f, new CubeDeformation(0.6f)), PartPose.ZERO);
        PartDefinition mantle = body.addOrReplaceChild("mantle", CubeListBuilder.create()
                .texOffs(0, 32).addBox(-5.0f, 0.0f, 0.0f, 10.0f, 5.0f, 1.0f, new CubeDeformation(0.15f)),
                PartPose.offsetAndRotation(0.0f, 0.3f, 3.1f, 0.1f, 0.0f, 0.0f));
        mantle.addOrReplaceChild("hem", CubeListBuilder.create()
                .texOffs(0, 40).addBox(-4.5f, 0.0f, 0.0f, 9.0f, 6.0f, 1.0f, new CubeDeformation(0.1f)),
                PartPose.offsetAndRotation(0.0f, 4.8f, 0.15f, 0.08f, 0.0f, 0.0f));
        // Schultern: zwei Lagen Pelz übereinander
        root.addOrReplaceChild("pelt_left_arm", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-2.0f, -3.9f, -3.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.3f))
                .texOffs(24, 22).addBox(-2.0f, -2.1f, -3.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.62f)), PartPose.ZERO);
        root.addOrReplaceChild("pelt_right_arm", CubeListBuilder.create()
                .texOffs(0, 22).addBox(-4.0f, -3.9f, -3.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.3f))
                .texOffs(24, 22).addBox(-4.0f, -2.1f, -3.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.62f)), PartPose.ZERO);
        // Wolfsschwanz an den Beinlingen (hängt am Körper, unten am Rücken)
        PartDefinition tail = root.addOrReplaceChild("pelt_tail", CubeListBuilder.create()
                .texOffs(48, 0).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.4f)),
                PartPose.offsetAndRotation(0.0f, 10.4f, 3.0f, 0.55f, 0.0f, 0.0f));
        PartDefinition mid = tail.addOrReplaceChild("mid", CubeListBuilder.create()
                .texOffs(48, 6).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.7f)),
                PartPose.offsetAndRotation(0.0f, 3.0f, 0.0f, 0.2f, 0.0f, 0.0f));
        mid.addOrReplaceChild("tip", CubeListBuilder.create()
                .texOffs(48, 12).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 2.0f, 2.0f, new CubeDeformation(0.45f)),
                PartPose.offsetAndRotation(0.0f, 3.3f, 0.0f, 0.15f, 0.0f, 0.0f));
        // Pelzstulpen der Stiefel
        root.addOrReplaceChild("pelt_left_leg", CubeListBuilder.create()
                .texOffs(24, 32).addBox(-3.0f, 5.6f, -3.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.35f)), PartPose.ZERO);
        root.addOrReplaceChild("pelt_right_leg", CubeListBuilder.create()
                .texOffs(24, 32).addBox(-3.0f, 5.6f, -3.0f, 6.0f, 2.0f, 6.0f, new CubeDeformation(0.35f)), PartPose.ZERO);
    }

    /** Ein spitzes Ohr: Grund und Spitze, leicht nach außen und nach vorn geneigt. */
    private static void ear(PartDefinition head, String name, float x, float outward, int u) {
        PartDefinition ear = head.addOrReplaceChild(name, CubeListBuilder.create()
                        .texOffs(u, 8).addBox(-1.5f, -2.0f, -0.5f, 3.0f, 2.0f, 1.0f, new CubeDeformation(0.1f)),
                PartPose.offsetAndRotation(x, -8.9f, -1.6f, 0.12f, 0.0f, outward));
        ear.addOrReplaceChild("tip", CubeListBuilder.create()
                        .texOffs(u + 8, 8).addBox(-1.0f, -1.0f, -0.5f, 2.0f, 1.0f, 1.0f, new CubeDeformation(-0.05f)),
                PartPose.offset(0.0f, -1.9f, 0.0f));
    }

    private static void ember(PartDefinition root) {
        PartDefinition head = root.addOrReplaceChild("ember_head", CubeListBuilder.create()
                .texOffs(32, 0).addBox(-4.5f, -8.1f, -5.6f, 9.0f, 1.0f, 1.0f, new CubeDeformation(0.05f)), PartPose.ZERO); // Stirnplatte
        horn(head, "left_horn", -3.2f, -0.5f);
        horn(head, "right_horn", 3.2f, 0.5f);
        // ein Kamm aus Flossen über den Scheitel, vorn die größte
        for (int i = 0; i < 4; i++) {
            head.addOrReplaceChild("fin" + i, CubeListBuilder.create()
                            .texOffs(24, 0).addBox(-0.5f, -2.0f, -1.0f, 1.0f, 2.0f, 2.0f, new CubeDeformation(0.25f - i * 0.12f)),
                    PartPose.offsetAndRotation(0.0f, -8.7f, -3.4f + i * 2.3f, -0.45f, 0.0f, 0.0f));
        }
        // vier Stacheln entlang des Rückgrats, nach hinten oben gerichtet, nach unten kleiner
        PartDefinition back = root.addOrReplaceChild("ember_body", CubeListBuilder.create(), PartPose.ZERO);
        for (int i = 0; i < 4; i++) {
            float grow = 0.25f - i * 0.12f;
            PartDefinition spike = back.addOrReplaceChild("spike" + i, CubeListBuilder.create()
                            .texOffs(0, 8).addBox(-1.0f, -1.0f, 0.0f, 2.0f, 2.0f, 3.0f, new CubeDeformation(grow)),
                    PartPose.offsetAndRotation(0.0f, 1.2f + i * 3.1f, 2.4f, 0.75f, 0.0f, 0.0f));
            spike.addOrReplaceChild("tip", CubeListBuilder.create()
                            .texOffs(10, 8).addBox(-0.5f, -0.5f, 0.0f, 1.0f, 1.0f, 2.0f, new CubeDeformation(grow * 0.5f)),
                    PartPose.offsetAndRotation(0.0f, 0.0f, 2.7f, 0.3f, 0.0f, 0.0f));
        }
        PartDefinition left = root.addOrReplaceChild("ember_left_arm", pauldron(-2.5f), PartPose.ZERO);
        PartDefinition right = root.addOrReplaceChild("ember_right_arm", pauldron(-4.5f), PartPose.ZERO);
        for (int i = 0; i < 2; i++) {
            float z = -1.3f + i * 2.6f;
            left.addOrReplaceChild("spike" + i, CubeListBuilder.create()
                            .texOffs(28, 16).addBox(-0.5f, -3.0f, -0.5f, 1.0f, 3.0f, 1.0f, new CubeDeformation(0.1f - i * 0.1f)),
                    PartPose.offsetAndRotation(2.4f, -3.5f, z, 0.0f, 0.0f, 0.5f));
            right.addOrReplaceChild("spike" + i, CubeListBuilder.create()
                            .texOffs(28, 16).addBox(-0.5f, -3.0f, -0.5f, 1.0f, 3.0f, 1.0f, new CubeDeformation(0.1f - i * 0.1f)),
                    PartPose.offsetAndRotation(-2.4f, -3.5f, z, 0.0f, 0.0f, -0.5f));
        }
    }

    /** Eine Schulterplatte aus Schuppen über der Rüstung (x0: linke Kante im Armraum). */
    private static CubeListBuilder pauldron(float x0) {
        return CubeListBuilder.create().texOffs(0, 16).addBox(x0, -3.7f, -3.5f, 7.0f, 2.0f, 7.0f, new CubeDeformation(0.1f));
    }

    /** Ein Horn aus vier Gliedern: erst nach oben und außen, dann weit nach hinten geschwungen; die Spitze glüht. */
    private static void horn(PartDefinition head, String name, float x, float outward) {
        PartDefinition base = head.addOrReplaceChild(name, CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(0.2f)),
                PartPose.offsetAndRotation(x, -8.4f, -1.4f, -0.3f, 0.0f, outward));
        PartDefinition second = base.addOrReplaceChild("second", CubeListBuilder.create()
                        .texOffs(8, 0).addBox(-1.0f, -3.0f, -1.0f, 2.0f, 3.0f, 2.0f, new CubeDeformation(-0.15f)),
                PartPose.offsetAndRotation(0.0f, -2.9f, 0.0f, -0.45f, 0.0f, -outward * 0.25f));
        PartDefinition third = second.addOrReplaceChild("third", CubeListBuilder.create()
                        .texOffs(16, 0).addBox(-0.5f, -2.0f, -0.5f, 1.0f, 2.0f, 1.0f, new CubeDeformation(0.15f)),
                PartPose.offsetAndRotation(0.0f, -2.7f, 0.0f, -0.55f, 0.0f, -outward * 0.3f));
        third.addOrReplaceChild("tip", CubeListBuilder.create()
                        .texOffs(20, 0).addBox(-0.5f, -2.0f, -0.5f, 1.0f, 2.0f, 1.0f, new CubeDeformation(-0.12f)),
                PartPose.offsetAndRotation(0.0f, -1.9f, 0.0f, -0.55f, 0.0f, 0.0f));
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        M model = this.getParentModel();
        ItemStack head = state.headEquipment;
        ItemStack chest = state.chestEquipment;
        ItemStack legs = state.legsEquipment;
        ItemStack feet = state.feetEquipment;
        if (head.is(MonsterGear.PELT_HELMET.get())) {
            this.part(poseStack, collector, light, model.head, this.peltHead, PELT, PELT_GLOW);
        } else if (head.is(MonsterGear.EMBER_HELMET.get())) {
            this.part(poseStack, collector, light, model.head, this.emberHead, EMBER, EMBER_GLOW);
        }
        if (chest.is(MonsterGear.PELT_CHESTPLATE.get())) {
            this.part(poseStack, collector, light, model.body, this.peltBody, PELT, null);
            this.part(poseStack, collector, light, model.leftArm, this.peltLeftArm, PELT, null);
            this.part(poseStack, collector, light, model.rightArm, this.peltRightArm, PELT, null);
        } else if (chest.is(MonsterGear.EMBER_CHESTPLATE.get())) {
            this.part(poseStack, collector, light, model.body, this.emberBody, EMBER, EMBER_GLOW);
            this.part(poseStack, collector, light, model.leftArm, this.emberLeftArm, EMBER, EMBER_GLOW);
            this.part(poseStack, collector, light, model.rightArm, this.emberRightArm, EMBER, EMBER_GLOW);
        }
        if (legs.is(MonsterGear.PELT_LEGGINGS.get())) {
            this.part(poseStack, collector, light, model.body, this.peltTail, PELT, null);
        }
        if (feet.is(MonsterGear.PELT_BOOTS.get())) {
            this.part(poseStack, collector, light, model.leftLeg, this.peltLeftLeg, PELT, null);
            this.part(poseStack, collector, light, model.rightLeg, this.peltRightLeg, PELT, null);
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
