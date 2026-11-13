package io.github.sirschrolli.kingdomsatwar.client.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.model.geom.ArmorPartsGeometry;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.HumanoidRenderState;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.equipment.Equippable;
import org.jspecify.annotations.Nullable;

/**
 * 3D-Teile der getragenen Rüstungen (seit 0.95), über der gewöhnlichen flachen Rüstung – passend zum Material:
 * <ul>
 *   <li><b>Stahl:</b> Kamm und roter Helmbusch, Schulterstücke aus drei Platten mit Goldsaum, Ellbogenkacheln, Beintaschen,
 *   Kniebuckel, Schuhspitzen und goldene Sporen;</li>
 *   <li><b>Konterit:</b> Flügel an den Schläfen und Knöcheln, ein Stirnstein, sichelförmige Schulterstücke, ein kurzer
 *   Umhang, Kniesteine;</li>
 *   <li><b>Nachtstahl:</b> eine Krone aus Nachtkristallen, Kristallstacheln auf Schultern, Rücken, Knien und Fersen;</li>
 *   <li><b>Abyssit:</b> zwei große Hörner mit leuchtenden Adern, ein Stirnkristall, schwere Schulterstücke mit Kristallen,
 *   Kristalle auf Rücken, Schenkeln und Fersen;</li>
 *   <li><b>Drachenschuppen:</b> Hörner je Element (Feuer mit Glut, Eis aus Kristall, Blitz gezackt, Natur ein Geweih,
 *   Erde Widderhörner, Wasser Flossen), Kamm, Rückenstacheln, Schulterschuppen mit Kralle, ein Drachenschwanz zu den
 *   Beinschienen und Krallen an den Stiefeln;</li>
 *   <li><b>Edelsteinkrone:</b> Zacken mit Perlen und Edelsteine über dem Reif;</li>
 *   <li><b>Mondstein</b> (seit 0.96): ein Stirnreif mit leuchtender Mondsichel und Mondstein, ein Sternenmantel bis an die Knie,
 *   Mondsicheln auf den Schultern, Armreife und Gürtelschnalle mit Mondsteinen, Robenschöße und spitze, aufgebogene Schuhe;</li>
 *   <li><b>Phasit</b> (seit 1.4, die Rüstung der Leerwandler): eine tiefe Kapuze mit einer Krone aus schwebenden
 *   Phasitsplittern, ein langer Mantel bis an die Knie, Schulterplatten mit schwebenden Splittern, Armreife mit leuchtender
 *   Naht, Phasitschnalle und -knie, Stiefel mit leuchtenden Schwebesohlen;</li>
 *   <li>die Rüstungen von <b>Minecraft</b>: Leder (Feder an der Kappe, Pelzkragen, Gürtel mit Tasche, Pelzstulpen),
 *   Kettenhemd (Kettenkragen am Helm, Kettenschultern, Kettenschurz), Eisen (Nasenschutz, Grat, Schulterstücke,
 *   Kniebuckel, Schuhkappen), Gold (Strahlenkamm, Rubine, Sonnenscheibe, Schulterstücke), Diamant (Kamm und Schultern
 *   aus Diamantsplittern), Netherit (Hörner, schwere Schulterstücke mit Dornen), Kupfer (Helmspitze, Platten mit
 *   Grünspan) und der Schildkrötenpanzer (ein gewölbter Panzer aus Schilden).</li>
 * </ul>
 * Erkannt wird ein Teil am Ausrüstungs-Asset der Mod ({@code kingdomsatwar:steel} …); Geometrie und Textur entstehen in
 * tools/entitygen/models/armor_parts.py (ArmorPartsGeometry). Leuchtende Stellen kommen aus einer eigenen Leuchtebene;
 * verzauberte Teile schimmern wie die Rüstung.
 */
public class ArmorPartsLayer<S extends HumanoidRenderState, M extends HumanoidModel<S>> extends RenderLayer<S, M> {
    public static final ModelLayerLocation LAYER = new ModelLayerLocation(KingdomsAtWar.id("armor_parts"), "main");
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/armor_parts/armor_parts.png");
    private static final Identifier GLOW = KingdomsAtWar.id("textures/entity/armor_parts/armor_parts_glow.png");
    /** Ausrüstungs-Asset der Mod → Name des Satzes in ArmorPartsGeometry. */
    private static final Map<String, String> SETS = Map.ofEntries(
            Map.entry("steel", "steel"), Map.entry("konterit", "konterit"), Map.entry("nightsteel", "nightsteel"),
            Map.entry("abyssite", "abyssite"), Map.entry("fire_dragon_scale", "fire"), Map.entry("ice_dragon_scale", "ice"),
            Map.entry("lightning_dragon_scale", "lightning"), Map.entry("nature_dragon_scale", "nature"),
            Map.entry("earth_dragon_scale", "earth"), Map.entry("water_dragon_scale", "water"),
            Map.entry("gemstone_crown", "crown"), Map.entry("moonstone", "moonstone"), Map.entry("aquamarine", "aquamarine"),
            Map.entry("dusk", "dusk"), Map.entry("starwalker", "starwalker"), Map.entry("timewarden", "timewarden"),
            Map.entry("emberwalker", "emberwalker"), Map.entry("voidwalker", "voidwalker"), Map.entry("listener", "listener"));
    /** Die Rüstungen von Minecraft (Ausrüstungs-Asset minecraft:…) → Name ihres Satzes. */
    private static final Map<String, String> VANILLA = Map.of("leather", "leather", "chainmail", "chainmail", "iron", "iron",
            "gold", "gold", "diamond", "diamond", "netherite", "netherite", "copper", "copper", "turtle_scute", "turtle");
    private static final List<String> HEAD = List.of("head");
    private static final List<String> CHEST = List.of("body", "left_arm", "right_arm");
    private static final List<String> LEGS = List.of("body", "left_leg", "right_leg");
    private static final List<String> FEET = List.of("left_leg", "right_leg");

    private final Map<String, ModelPart> parts = new HashMap<>();

    public ArmorPartsLayer(RenderLayerParent<S, M> parent, ModelPart root) {
        super(parent);
        for (String set : java.util.stream.Stream.concat(SETS.values().stream(), VANILLA.values().stream()).toList()) {
            put(root, set, "head", HEAD);
            put(root, set, "chest", CHEST);
            put(root, set, "legs", LEGS);
            put(root, set, "feet", FEET);
        }
    }

    private void put(ModelPart root, String set, String slot, List<String> bones) {
        for (String bone : bones) {
            String name = set + "_" + slot + "_" + bone;
            ModelPart part = root.getChild(name);
            if (part.getAllParts().stream().anyMatch(p -> !p.isEmpty())) {
                this.parts.put(name, part);
            }
        }
    }

    public static LayerDefinition createLayer() {
        return ArmorPartsGeometry.createLayer();
    }

    /** Hat dieser Gegenstand 3D-Teile (für Prüfungen)? */
    public static boolean hasParts(ItemStack stack) {
        return set(stack) != null;
    }

    private static @Nullable String set(ItemStack stack) {
        Equippable equippable = stack.get(DataComponents.EQUIPPABLE);
        if (equippable == null || equippable.assetId().isEmpty()) {
            return null;
        }
        Identifier asset = equippable.assetId().get().identifier();
        if (KingdomsAtWar.MODID.equals(asset.getNamespace())) {
            return SETS.get(asset.getPath());
        }
        return "minecraft".equals(asset.getNamespace()) ? VANILLA.get(asset.getPath()) : null;
    }

    @Override
    public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, S state, float yRot, float xRot) {
        if (state.isBaby) {
            return;
        }
        M model = this.getParentModel();
        this.piece(poseStack, collector, light, state.headEquipment, "head", HEAD, model);
        this.piece(poseStack, collector, light, state.chestEquipment, "chest", CHEST, model);
        this.piece(poseStack, collector, light, state.legsEquipment, "legs", LEGS, model);
        this.piece(poseStack, collector, light, state.feetEquipment, "feet", FEET, model);
    }

    private void piece(PoseStack poseStack, SubmitNodeCollector collector, int light, ItemStack stack, String slot, List<String> bones,
            M model) {
        String set = set(stack);
        if (set == null) {
            return;
        }
        boolean foil = stack.hasFoil();
        for (String bone : bones) {
            ModelPart part = this.parts.get(set + "_" + slot + "_" + bone);
            ModelPart parent = switch (bone) {
                case "head" -> model.head;
                case "body" -> model.body;
                case "left_arm" -> model.leftArm;
                case "right_arm" -> model.rightArm;
                case "left_leg" -> model.leftLeg;
                default -> model.rightLeg;
            };
            if (part == null || !parent.visible) {
                continue;
            }
            poseStack.pushPose();
            parent.translateAndRotate(poseStack);
            collector.submitModelPart(part, poseStack, foil ? RenderTypes.armorCutoutNoCullGlint(TEXTURE) : RenderTypes.armorCutoutNoCull(TEXTURE),
                    light, OverlayTexture.NO_OVERLAY, null);
            collector.submitModelPart(part, poseStack, RenderTypes.eyes(GLOW), light, OverlayTexture.NO_OVERLAY, null);
            poseStack.popPose();
        }
    }
}
