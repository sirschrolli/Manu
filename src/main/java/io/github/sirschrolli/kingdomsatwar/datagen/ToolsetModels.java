package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.world.item.Item;

/**
 * Werkzeuge und Waffen in 3D (0.88): die Sätze aus Stahl, Nachtstahl und Abyssit, Draconium-Schwert und -Axt, die
 * Mondsilberklinge, die Trollkeule, der Kommandostab und der Vermessungsstab. Im Inventar das bisherige Bild, in der Hand das
 * 3D-Modell aus {@link ToolsetShapes} (erzeugt von tools/toolset_models.py), gehalten wie die Waffen der Waffenkammer.
 */
final class ToolsetModels {
    private ToolsetModels() {}

    static void register(ItemModelGenerators items) {
        // Schwerter wie bei Minecraft groß; Werkzeuge mit Kopf etwas kleiner und zur Bildmitte, damit der Kopf im Bild bleibt
        sword(items, ModItems.STEEL_SWORD.get(), "steel_sword", ToolsetShapes.STEEL_SWORD);
        tool(items, ModItems.STEEL_AXE.get(), "steel_axe", ToolsetShapes.STEEL_AXE);
        tool(items, ModItems.STEEL_PICKAXE.get(), "steel_pickaxe", ToolsetShapes.STEEL_PICKAXE);
        tool(items, ModItems.STEEL_SHOVEL.get(), "steel_shovel", ToolsetShapes.STEEL_SHOVEL);
        tool(items, ModItems.STEEL_HOE.get(), "steel_hoe", ToolsetShapes.STEEL_HOE);
        sword(items, ShadowItems.NIGHTSTEEL_SWORD.get(), "nightsteel_sword", ToolsetShapes.NIGHTSTEEL_SWORD);
        tool(items, ShadowItems.NIGHTSTEEL_AXE.get(), "nightsteel_axe", ToolsetShapes.NIGHTSTEEL_AXE);
        tool(items, ShadowItems.NIGHTSTEEL_PICKAXE.get(), "nightsteel_pickaxe", ToolsetShapes.NIGHTSTEEL_PICKAXE);
        tool(items, ShadowItems.NIGHTSTEEL_SHOVEL.get(), "nightsteel_shovel", ToolsetShapes.NIGHTSTEEL_SHOVEL);
        tool(items, ShadowItems.NIGHTSTEEL_HOE.get(), "nightsteel_hoe", ToolsetShapes.NIGHTSTEEL_HOE);
        sword(items, ShadowItems.ABYSSITE_SWORD.get(), "abyssite_sword", ToolsetShapes.ABYSSITE_SWORD);
        tool(items, ShadowItems.ABYSSITE_AXE.get(), "abyssite_axe", ToolsetShapes.ABYSSITE_AXE);
        tool(items, ShadowItems.ABYSSITE_PICKAXE.get(), "abyssite_pickaxe", ToolsetShapes.ABYSSITE_PICKAXE);
        tool(items, ShadowItems.ABYSSITE_SHOVEL.get(), "abyssite_shovel", ToolsetShapes.ABYSSITE_SHOVEL);
        tool(items, ShadowItems.ABYSSITE_HOE.get(), "abyssite_hoe", ToolsetShapes.ABYSSITE_HOE);
        sword(items, ModItems.DRACONIUM_SWORD.get(), "draconium_sword", ToolsetShapes.DRACONIUM_SWORD);
        tool(items, ModItems.DRACONIUM_AXE.get(), "draconium_axe", ToolsetShapes.DRACONIUM_AXE);
        MonsterGearModels.weapon(items, ShadowItems.MOONSILVER_BLADE.get(), "moonsilver_blade", ToolsetShapes.MOONSILVER_BLADE, 0.52f, 2.0f);
        MonsterGearModels.weapon(items, ModItems.TROLL_CLUB.get(), "troll_club", ToolsetShapes.TROLL_CLUB, 0.54f, 3.0f);
        MonsterGearModels.weapon(items, ModItems.COMMAND_BATON.get(), "command_baton", ToolsetShapes.COMMAND_BATON, 0.6f, 1.0f);
        MonsterGearModels.weapon(items, ModItems.SURVEYORS_ROD.get(), "surveyors_rod", ToolsetShapes.SURVEYORS_ROD, 0.46f, 3.0f);
    }

    private static void sword(ItemModelGenerators items, Item item, String name, float[][] parts) {
        MonsterGearModels.weapon(items, item, name, parts, 0.56f, 2.0f);
    }

    private static void tool(ItemModelGenerators items, Item item, String name, float[][] parts) {
        MonsterGearModels.weapon(items, item, name, parts, 0.54f, 3.0f);
    }
}
