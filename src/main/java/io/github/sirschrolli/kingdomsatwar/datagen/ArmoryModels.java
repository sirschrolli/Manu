package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.Armory;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle der Waffenkammer (0.84).
 * <ul>
 *   <li>Waffen und Spitzhacken: im Inventar das Bild, in der Hand das 3D-Modell aus {@link ArmoryShapes} (erzeugt von
 *   tools/armory_models.py), gehalten und schräg gelegt wie ein Schwert – wie die Waffen aus Monsterteilen.</li>
 *   <li>Schilde: überall das 3D-Modell aus {@link ArmoryShieldShapes} (erzeugt von tools/armory_shields.py), um den Ursprung
 *   gebaut wie der Schild von Minecraft und mit dessen Haltungen – beim Blocken (Bedingung „benutzt gerade“) die
 *   Blockhaltung.</li>
 * </ul>
 */
final class ArmoryModels {
    private ArmoryModels() {}

    static void register(ItemModelGenerators items) {
        // lange Waffen aus eigener Sicht etwas kleiner (Minecraft: 0.68) und ein Stück zur Mitte, damit ihre Köpfe im Bild bleiben
        MonsterGearModels.weapon(items, Armory.STEEL_HALBERD.get(), "steel_halberd", ArmoryShapes.STEEL_HALBERD, 0.55f, 2.0f);
        MonsterGearModels.weapon(items, Armory.DRACONIUM_WARHAMMER.get(), "draconium_warhammer", ArmoryShapes.DRACONIUM_WARHAMMER, 0.55f, 3.5f);
        MonsterGearModels.weapon(items, Armory.NIGHTSTEEL_DAGGER.get(), "nightsteel_dagger", ArmoryShapes.NIGHTSTEEL_DAGGER, 0.68f, 0.0f);
        MonsterGearModels.weapon(items, Armory.ABYSSITE_GREATSWORD.get(), "abyssite_greatsword", ArmoryShapes.ABYSSITE_GREATSWORD, 0.55f, 1.5f);
        MonsterGearModels.weapon(items, Armory.STEEL_DOUBLE_AXE.get(), "steel_double_axe", ArmoryShapes.STEEL_DOUBLE_AXE, 0.6f, 3.0f);
        MonsterGearModels.weapon(items, Armory.MINER_HAMMER.get(), "miner_hammer", ArmoryShapes.MINER_HAMMER, 0.6f, 3.5f);
        MonsterGearModels.weapon(items, Armory.EMBER_PICKAXE.get(), "ember_pickaxe", ArmoryShapes.EMBER_PICKAXE, 0.64f, 2.0f);
        MonsterGearModels.weapon(items, Armory.VEIN_BREAKER.get(), "vein_breaker", ArmoryShapes.VEIN_BREAKER, 0.64f, 1.0f);
        MonsterGearModels.weapon(items, Armory.ORE_SEEKER.get(), "ore_seeker", ArmoryShapes.ORE_SEEKER, 0.62f, 1.0f);
        // der Hüterhammer (0.96): die Beute des Steinhüters
        MonsterGearModels.weapon(items, io.github.sirschrolli.kingdomsatwar.registry.FaeGear.KEEPER_MAUL.get(), "keeper_maul",
                ArmoryShapes.KEEPER_MAUL, 0.55f, 3.5f);
        MonsterGearModels.weapon(items, io.github.sirschrolli.kingdomsatwar.registry.SeaWitch.WITCH_STAFF.get(), "sea_witch_staff",
                ArmoryShapes.SEA_WITCH_STAFF, 0.5f, 3.5f);
        // die runden Schilde sind kleiner als der von Minecraft: aus eigener Sicht etwas höher, damit man sie sieht
        shield(items, Armory.STEEL_BUCKLER.get(), "steel_buckler", ArmoryShieldShapes.STEEL_BUCKLER, 3.5f);
        shield(items, Armory.DRAGON_SHIELD.get(), "dragon_shield", ArmoryShieldShapes.DRAGON_SHIELD, 0.0f);
        shield(items, Armory.TOWER_SHIELD.get(), "tower_shield", ArmoryShieldShapes.TOWER_SHIELD, 0.0f);
        shield(items, Armory.MIRROR_SHIELD.get(), "mirror_shield", ArmoryShieldShapes.MIRROR_SHIELD, 2.5f);
    }

    private static Material tex(String path) {
        return new Material(KingdomsAtWar.id(path));
    }

    private static void shield(ItemModelGenerators items, Item item, String name, float[][] parts, float lift) {
        TextureMapping textures = new TextureMapping()
                .put(MonsterGearModels.MODEL, tex("item/" + name + "_model"))
                .put(MonsterGearModels.MODEL_GLOW, tex("item/" + name + "_model_glow"))
                .put(TextureSlot.PARTICLE, tex("item/" + name + "_model"));
        Identifier normal = shieldTemplate(parts, false, lift).build().create(ModelLocationUtils.getModelLocation(item), textures,
                items.modelOutput);
        Identifier blocking = shieldTemplate(parts, true, lift).build()
                .create(ModelLocationUtils.getModelLocation(item, "_blocking"), textures, items.modelOutput);
        items.itemModelOutput.accept(item, ItemModelUtils.conditional(ItemModelUtils.isUsingItem(), ItemModelUtils.plainModel(blocking),
                ItemModelUtils.plainModel(normal)));
    }

    /**
     * Die Haltungen des Schilds von Minecraft (models/item/shield.json und shield_blocking.json), dazu die Teile; aus eigener
     * Sicht um {@code lift} Einheiten angehoben.
     */
    private static ExtendedModelTemplateBuilder shieldTemplate(float[][] parts, boolean blocking, float lift) {
        ExtendedModelTemplateBuilder b = ExtendedModelTemplateBuilder.builder()
                .requiredTextureSlot(MonsterGearModels.MODEL).requiredTextureSlot(MonsterGearModels.MODEL_GLOW)
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .guiLight(net.minecraft.client.resources.model.UnbakedModel.GuiLight.FRONT)
                .transform(ItemDisplayContext.GUI, t -> t.rotation(15, -25, -5).translation(2, 3, 0).scale(0.65f, 0.65f, 0.65f))
                .transform(ItemDisplayContext.FIXED, t -> t.rotation(0, 180, 0).translation(-4.5f, 4.5f, -5).scale(0.55f, 0.55f, 0.55f))
                .transform(ItemDisplayContext.ON_SHELF, t -> t.rotation(0, 0, 0).translation(11, 18.5f, 8.7f).scale(1.4f, 1.4f, 1.4f))
                .transform(ItemDisplayContext.GROUND, t -> t.rotation(0, 0, 0).translation(2, 4, 2).scale(0.25f, 0.25f, 0.25f));
        if (blocking) {
            b.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(45, 155, 0).translation(-3.49f, 11, -2).scale(1, 1, 1))
                    .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(45, 155, 0).translation(11.51f, 7, 2.5f).scale(1, 1, 1))
                    .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                            t -> t.rotation(0, 180, -5).translation(-15, 3.25f + lift, -11).scale(1.25f, 1.25f, 1.25f))
                    .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                            t -> t.rotation(0, 180, -5).translation(5, 5 + lift, -11).scale(1.25f, 1.25f, 1.25f));
        } else {
            b.transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, t -> t.rotation(0, 90, 0).translation(10, 6, -4).scale(1, 1, 1))
                    .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND, t -> t.rotation(0, 90, 0).translation(10, 6, 12).scale(1, 1, 1))
                    .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                            t -> t.rotation(0, 180, 5).translation(-10, 1.75f + lift, -10).scale(1.25f, 1.25f, 1.25f))
                    .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                            t -> t.rotation(0, 180, 5).translation(10, lift, -10).scale(1.25f, 1.25f, 1.25f));
        }
        for (float[] p : parts) {
            boolean glow = p[6] > 0;
            b.element(e -> {
                e.from(p[0], p[1], p[2]).to(p[3], p[4], p[5]);
                e.allFaces((direction, face) -> {
                    int i = 7 + 4 * switch (direction) {
                        case NORTH -> 0;
                        case SOUTH -> 1;
                        case EAST -> 2;
                        case WEST -> 3;
                        case UP -> 4;
                        case DOWN -> 5;
                    };
                    face.texture(glow ? MonsterGearModels.MODEL_GLOW : MonsterGearModels.MODEL).uvs(p[i], p[i + 1], p[i + 2], p[i + 3]);
                });
                if (glow) {
                    e.lightEmission(15);
                }
            });
        }
        return b;
    }
}
