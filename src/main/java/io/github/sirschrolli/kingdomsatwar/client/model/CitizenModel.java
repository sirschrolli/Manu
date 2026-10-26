package io.github.sirschrolli.kingdomsatwar.client.model;

import io.github.sirschrolli.kingdomsatwar.client.renderer.CitizenRenderState;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.model.player.PlayerModel;
import net.minecraft.util.Mth;

/**
 * Menschen-Modell der Bürger: wie ein Spieler (mit zweiter Hautschicht), dazu die Kleidung ihres Berufs – Strohhut
 * mit breiter Krempe (Bauer), Grubenhelm mit Lampe (Bergmann), Kappen und Barett mit Feder, Schürze (Schmied und
 * Wirt), Kartenköcher (Kartograf), Geldbeutel. Frauen tragen lange Haare und ein Kleid, dessen Vorder- und Rückteil
 * beim Gehen von den Beinen mitgenommen wird.
 *
 * <p>Die Textur ist 128x64 groß: links das gewohnte Spieler-Schema, rechts die Zusatzteile (siehe
 * {@code tools/generate_citizens.py}).
 */
public class CitizenModel extends HumanoidModel<CitizenRenderState> {
    private final ModelPart crown;
    private final ModelPart brim;
    private final ModelPart cap;
    private final ModelPart lamp;
    private final ModelPart feather;
    private final ModelPart hair;
    private final ModelPart pouch;
    private final ModelPart scroll;
    private final ModelPart flapFront;
    private final ModelPart flapBack;

    public CitizenModel(ModelPart root) {
        super(root);
        this.crown = this.head.getChild("crown");
        this.brim = this.head.getChild("brim");
        this.cap = this.head.getChild("cap");
        this.lamp = this.head.getChild("lamp");
        this.feather = this.head.getChild("feather");
        this.hair = this.head.getChild("long_hair");
        this.pouch = this.body.getChild("pouch");
        this.scroll = this.body.getChild("scroll");
        this.flapFront = this.body.getChild("flap_front");
        this.flapBack = this.body.getChild("flap_back");
    }

    public static LayerDefinition createLayer() {
        MeshDefinition mesh = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = mesh.getRoot();
        PartDefinition head = root.getChild("head");
        PartDefinition body = root.getChild("body");
        // breite Krempe (Strohhut) auf Stirnhöhe
        head.addOrReplaceChild("brim", CubeListBuilder.create().texOffs(64, 0).addBox(-7.0f, -7.6f, -7.0f, 14.0f, 1.0f, 14.0f),
                PartPose.ZERO);
        // hoher Hut bzw. Helm
        head.addOrReplaceChild("crown", CubeListBuilder.create().texOffs(64, 16)
                .addBox(-4.0f, -12.0f, -4.0f, 8.0f, 4.0f, 8.0f, new CubeDeformation(0.6f)), PartPose.ZERO);
        // flache Kappe bzw. Barett
        head.addOrReplaceChild("cap", CubeListBuilder.create().texOffs(96, 16)
                .addBox(-4.0f, -10.0f, -4.0f, 8.0f, 2.0f, 8.0f, new CubeDeformation(0.6f)), PartPose.ZERO);
        // Grubenlampe vorn am Helm
        head.addOrReplaceChild("lamp", CubeListBuilder.create().texOffs(92, 28).addBox(-1.0f, -11.0f, -5.5f, 2.0f, 2.0f, 1.0f),
                PartPose.ZERO);
        // Feder am Barett (schräg nach hinten)
        head.addOrReplaceChild("feather", CubeListBuilder.create().texOffs(84, 28).addBox(-0.5f, -5.0f, -1.5f, 1.0f, 5.0f, 3.0f),
                PartPose.offsetAndRotation(3.6f, -9.8f, 1.5f, -0.55f, 0.0f, 0.3f));
        // lange Haare über den Rücken
        head.addOrReplaceChild("long_hair", CubeListBuilder.create().texOffs(64, 28)
                        .addBox(-4.5f, 0.0f, 0.0f, 9.0f, 7.0f, 1.0f, new CubeDeformation(0.0f, 0.0f, 0.1f)),
                PartPose.offsetAndRotation(0.0f, -4.2f, 4.2f, -0.24f, 0.0f, 0.0f));
        // Geldbeutel an der Hüfte
        body.addOrReplaceChild("pouch", CubeListBuilder.create().texOffs(98, 28).addBox(-1.0f, 0.0f, -1.0f, 2.0f, 3.0f, 2.0f),
                PartPose.offsetAndRotation(4.2f, 10.0f, -0.8f, 0.0f, 0.0f, -0.08f));
        // Kartenköcher schräg auf dem Rücken
        body.addOrReplaceChild("scroll", CubeListBuilder.create().texOffs(106, 28).addBox(-1.5f, -1.0f, 0.0f, 3.0f, 10.0f, 3.0f),
                PartPose.offsetAndRotation(0.5f, 1.5f, 2.3f, 0.0f, 0.0f, 0.55f));
        // Schürze bzw. Rock: Vorder- und Rückteil hängen an der Hüfte
        body.addOrReplaceChild("flap_front", CubeListBuilder.create().texOffs(64, 42).addBox(-4.5f, 0.0f, -1.0f, 9.0f, 7.0f, 1.0f),
                PartPose.offset(0.0f, 11.0f, -2.3f));
        body.addOrReplaceChild("flap_back", CubeListBuilder.create().texOffs(64, 52).addBox(-4.5f, 0.0f, 0.0f, 9.0f, 7.0f, 1.0f),
                PartPose.offset(0.0f, 11.0f, 2.3f));
        return LayerDefinition.create(mesh, 128, 64);
    }

    @Override
    public void setupAnim(CitizenRenderState state) {
        super.setupAnim(state);
        Profession p = state.profession;
        this.brim.visible = p == Profession.FARMER;
        this.crown.visible = p == Profession.FARMER || p == Profession.MINER;
        this.cap.visible = p == Profession.BUILDER || p == Profession.LUMBERJACK || p == Profession.MERCHANT || p == Profession.CARTOGRAPHER;
        this.lamp.visible = p == Profession.MINER;
        this.feather.visible = p == Profession.MERCHANT;
        this.pouch.visible = p == Profession.MERCHANT || p == Profession.BUILDER;
        this.scroll.visible = p == Profession.CARTOGRAPHER;
        this.hair.visible = state.feminine;
        boolean apron = p == Profession.SMITH || p == Profession.INNKEEPER;
        this.flapFront.visible = apron || state.feminine;
        this.flapBack.visible = state.feminine;

        // Vorder- und Rückteil werden von den Beinen mitgenommen (vorn das vordere Bein, hinten das hintere)
        float forward = Math.min(0.0f, Math.min(this.rightLeg.xRot, this.leftLeg.xRot));
        float backward = Math.max(0.0f, Math.max(this.rightLeg.xRot, this.leftLeg.xRot));
        this.flapFront.xRot = forward * 0.95f;
        this.flapBack.xRot = backward * 0.95f;
        // die Feder wippt beim Gehen leicht nach
        this.feather.xRot = -0.55f + Mth.sin(state.walkAnimationPos * 0.6662f * 2.0f) * state.walkAnimationSpeed * 0.12f;
        // die Haare schwingen mit dem Kopf nach (bleiben hinten am Rücken, wenn der Kopf nach unten schaut)
        this.hair.xRot = -0.24f - Math.max(0.0f, this.head.xRot) * 0.9f;
        if (state.working && p == Profession.SMITH) {
            // der Schmied hält den Hammer bereit
            this.rightArm.xRot = Math.min(this.rightArm.xRot, -0.45f);
        }
    }
}
