package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.stream.Stream;
import net.minecraft.data.loot.EntityLootSubProvider;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.EmptyLootItem;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.EnchantedCountIncreaseFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemKilledByPlayerCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithEnchantedBonusCondition;
import net.minecraft.world.level.storage.loot.providers.number.ints.ContextIntProviders;
import net.minecraft.world.level.storage.loot.providers.number.floats.ContextFloatProviders;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Beute der Lebewesen. Drachen lassen ihre Schuppen über eigenen Code fallen (je nach Wachstumsstufe),
 * Soldaten nur die Ausrüstung, die ihnen der Spieler gegeben hat. Banditen tragen erbeutete Smaragde bei sich –
 * die fallen auch, wenn ein Soldat den Banditen besiegt. Wilde Kreaturen geben Fleisch, Leder, Federn,
 * Irrlichtstaub, Erddrachen-Schuppen und (selten) Edelsteine, Draconium oder eine Trollkeule.
 */
public class ModEntityLootProvider extends EntityLootSubProvider {
    public ModEntityLootProvider(LootTableSubProvider.Context output) {
        super(FeatureFlags.REGISTRY.allFlags(), output);
    }

    @Override
    public void generate() {
        ModEntities.DRAGONS.values().forEach(type -> this.add(type.get(), LootTable.lootTable()));
        ModEntities.SOLDIERS.values().forEach(type -> this.add(type.get(), LootTable.lootTable()));
        // Bürger lassen nichts fallen (was sie tragen, liegt in der Truhe ihrer Werkstatt)
        this.add(ModEntities.CITIZEN.get(), LootTable.lootTable());
        // Dorfbewohner lassen nichts fallen (wer sie umbringt, gewinnt nichts außer schlechtem Ruf)
        this.add(ModEntities.VILLAGER.get(), LootTable.lootTable());
        // Drachen-Bosse: viele Schuppen, Draconium und Edelsteine (das Ei erscheint gesondert auf dem Nest)
        this.add(ModEntities.PYRRHON.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 8, 14))
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.FIRE).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4)));
        this.add(ModEntities.GLACIARA.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 8, 14))
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.ICE).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4)));
        this.add(ModEntities.VERDANTIS.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 8, 14))
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.NATURE).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.LIFE_BLOSSOM.get(), 2, 4)));
        this.add(ModEntities.TERRAGON.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 8, 14))
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.EARTH).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 3, 6))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.AMBER_CRYSTAL.get(), 4, 8)));
        this.add(ModEntities.VOLTARION.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 8, 14))
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.LIGHTNING).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.STORM_CRYSTAL.get(), 3, 6)));
        this.add(ModEntities.THALASSA.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 8, 14))
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.WATER).get(), 18, 26))
                .withPool(drop(ModItems.DRACONIUM.get(), 3, 6))
                .withPool(drop(ModItems.GEMSTONE.get(), 2, 4))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.NACRE_BLOCK.get(), 3, 6))
                .withPool(drop(net.minecraft.world.item.Items.PRISMARINE_SHARD, 6, 12)));
        // Nyxar: Abyssit, Seelenkristalle und Schattenessenz (das Herz der Schatten erscheint gesondert)
        this.add(ModEntities.NYXAR.get(), LootTable.lootTable()
                .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_DRAGON, 100))
                .withPool(drop(ModItems.GOLD_COIN.get(), 20, 32))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SHARD.get(), 6, 10))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SOUL_CRYSTAL.get(), 8, 14))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.SHADOW_ESSENCE.get(), 10, 16))
                .withPool(drop(io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_UPGRADE_SMITHING_TEMPLATE.get(), 1, 2))
                .withPool(drop(ModItems.GEMSTONE.get(), 4, 8)));
        // Erzmagier: immer ihre Zaubertafel, dazu Gold, Aetherit und ein verzaubertes Buch
        for (DragonType type : DragonType.values()) {
            this.add(ModEntities.MAGES.get(type).get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                            .add(LootItem.lootTableItem(ModItems.SPELL_TABLETS.get(io.github.sirschrolli.kingdomsatwar.magic.Spell.of(type)).get())))
                    .withPool(drop(ModItems.GOLD_COIN.get(), 3, 6))
                    .withPool(drop(ModItems.AETHERIT.get(), 2, 5))
                    .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_ARCANE, 50))
                    .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                            .add(LootItem.lootTableItem(net.minecraft.world.item.Items.BOOK)
                                    .apply(net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction
                                            .randomApplicableEnchantment(this.enchantments)))));
        }
        this.add(ModEntities.VINELING.get(), LootTable.lootTable()
                .withPool(drop(Items.VINE, 0, 2))
                .withPool(rare(io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.GLOWCAP.get(), 0.25f, 0.05f)));
        this.add(ModEntities.BANDIT.get(), banditLoot(false));
        ModEntities.REALM_WARRIORS.forEach((role, type) -> this.add(type.get(), this.warriorLoot(role)));
        this.add(ModEntities.BANDIT_ARCHER.get(), banditLoot(true));

        // Eigene Hof- und Weidetiere: dieselbe Beute wie die Vanilla-Tiere (Wolle nach Farbe, Braten im Feuer …)
        this.add(ModEntities.CATTLE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.COW));
        this.add(ModEntities.FARM_PIG.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PIG));
        this.add(ModEntities.FARM_SHEEP.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SHEEP));
        this.add(ModEntities.FARM_CHICKEN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CHICKEN));
        this.add(ModEntities.FARM_HORSE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.HORSE));
        this.add(ModEntities.WARHORSE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.HORSE));
        this.add(ModEntities.FARM_DONKEY.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.DONKEY));
        this.add(ModEntities.FARM_MULE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.MULE));
        this.add(ModEntities.FARM_GOAT.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.GOAT));
        this.add(ModEntities.HARE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.RABBIT));
        // Eigene Wildtiere: Beute wie die Vanilla-Tiere, die sie ersetzen
        this.add(ModEntities.WOLF.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.WOLF));
        this.add(ModEntities.FOX.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.FOX));
        this.add(ModEntities.BEAR.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.POLAR_BEAR));
        this.add(ModEntities.PANDA.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PANDA));
        this.add(ModEntities.CAT.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CAT));
        this.add(ModEntities.WILDCAT.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.OCELOT));
        this.add(ModEntities.PARROT.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PARROT));
        this.add(ModEntities.BEE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.BEE));
        this.add(ModEntities.BAT.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.BAT));
        this.add(ModEntities.ARMADILLO.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ARMADILLO));
        this.add(ModEntities.CAMEL.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CAMEL));
        this.add(ModEntities.LLAMA.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.LLAMA));
        this.add(ModEntities.TRADER_LLAMA.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.TRADER_LLAMA));
        // Eigene Monster (E4a): Beute wie bei Vanilla (Bedingungen wie aufgeladener Kriecher, Plattenspieler-Disc greifen mit)
        this.add(ModEntities.ZOMBIE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ZOMBIE));
        this.add(ModEntities.HUSK.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.HUSK));
        this.add(ModEntities.DROWNED.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.DROWNED));
        this.add(ModEntities.ZOMBIE_VILLAGER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ZOMBIE_VILLAGER));
        this.add(ModEntities.SKELETON.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SKELETON));
        this.add(ModEntities.STRAY.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.STRAY));
        this.add(ModEntities.BOGGED.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.BOGGED));
        this.add(ModEntities.PARCHED.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PARCHED));
        this.add(ModEntities.CREEPER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CREEPER));
        this.add(ModEntities.SPIDER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SPIDER));
        this.add(ModEntities.CAVE_SPIDER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CAVE_SPIDER));
        this.add(ModEntities.SLIME.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SLIME));
        this.add(ModEntities.SULFUR_CUBE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SULFUR_CUBE));
        this.add(ModEntities.WITCH.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.WITCH));
        this.add(ModEntities.PHANTOM.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PHANTOM));
        this.add(ModEntities.SILVERFISH.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SILVERFISH));
        this.add(ModEntities.ZOMBIE_HORSE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ZOMBIE_HORSE));
        this.add(ModEntities.SKELETON_HORSE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SKELETON_HORSE));
        this.add(ModEntities.CAMEL_HUSK.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CAMEL_HUSK));
        // übrige Monster (E4b): Beute wie bei Vanilla (Totem, Banner des Hauptmanns, Schwämme, Windruten, Katalysator …)
        this.add(ModEntities.PILLAGER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PILLAGER));
        this.add(ModEntities.VINDICATOR.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.VINDICATOR));
        this.add(ModEntities.EVOKER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.EVOKER));
        this.add(ModEntities.ILLUSIONER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ILLUSIONER));
        this.add(ModEntities.VEX.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.VEX));
        this.add(ModEntities.RAVAGER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.RAVAGER));
        this.add(ModEntities.GUARDIAN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.GUARDIAN));
        this.add(ModEntities.ELDER_GUARDIAN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ELDER_GUARDIAN));
        this.add(ModEntities.BREEZE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.BREEZE));
        this.add(ModEntities.CREAKING.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.CREAKING));
        this.add(ModEntities.WARDEN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.WARDEN));
        // Wassertiere (E4c): Beute wie bei Vanilla (Fische, Knochenmehl, Tinte, Schuppen …)
        this.add(ModEntities.COD.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.COD));
        this.add(ModEntities.SALMON.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SALMON));
        this.add(ModEntities.PUFFERFISH.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PUFFERFISH));
        this.add(ModEntities.TROPICAL_FISH.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.TROPICAL_FISH));
        this.add(ModEntities.SQUID.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SQUID));
        this.add(ModEntities.GLOW_SQUID.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.GLOW_SQUID));
        this.add(ModEntities.DOLPHIN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.DOLPHIN));
        // Golems und Wichtel (E4d): Eisen und Mohn, Schneebälle, Kupfer – wie bei Vanilla
        this.add(ModEntities.IRON_GOLEM.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.IRON_GOLEM));
        this.add(ModEntities.SNOW_GOLEM.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SNOW_GOLEM));
        this.add(ModEntities.COPPER_GOLEM.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.COPPER_GOLEM));
        this.add(ModEntities.ALLAY.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ALLAY));
        this.add(ModEntities.SNIFFER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SNIFFER));
        this.add(ModEntities.HAPPY_GHAST.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.HAPPY_GHAST));
        this.add(ModEntities.WANDERING_TRADER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.WANDERING_TRADER));
        this.add(ModEntities.PIGLIN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PIGLIN));
        this.add(ModEntities.PIGLIN_BRUTE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.PIGLIN_BRUTE));
        this.add(ModEntities.ZOMBIFIED_PIGLIN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ZOMBIFIED_PIGLIN));
        this.add(ModEntities.HOGLIN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.HOGLIN));
        this.add(ModEntities.ZOGLIN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ZOGLIN));
        this.add(ModEntities.STRIDER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.STRIDER));
        this.add(ModEntities.GHAST.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.GHAST));
        this.add(ModEntities.BLAZE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.BLAZE));
        this.add(ModEntities.MAGMA_CUBE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.MAGMA_CUBE));
        // Brandgebein: Kohle, Knochen und selten sein Schädel; den Netherstern lässt der Aschenkönig selbst fallen (wie der Wither)
        this.add(ModEntities.WITHER_SKELETON.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.WITHER_SKELETON));
        this.add(ModEntities.WITHER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.WITHER));
        // Wesen des Ends (E5c): Enderperlen wie gewohnt
        this.add(ModEntities.ENDERMAN.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ENDERMAN));
        this.add(ModEntities.ENDERMITE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ENDERMITE));
        this.add(ModEntities.SHULKER.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.SHULKER));
        this.add(ModEntities.TURTLE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.TURTLE));
        this.add(ModEntities.AXOLOTL.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.AXOLOTL));
        this.add(ModEntities.FROG.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.FROG));
        this.add(ModEntities.TADPOLE.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.TADPOLE));
        this.add(ModEntities.NAUTILUS.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.NAUTILUS));
        this.add(ModEntities.ZOMBIE_NAUTILUS.get(), vanillaLoot(net.minecraft.world.entity.EntityTypes.ZOMBIE_NAUTILUS));

        // Wilde Kreaturen
        this.add(ModEntities.WILD_BOAR.get(), LootTable.lootTable()
                .withPool(meat(Items.PORKCHOP, 1, 3))
                .withPool(drop(Items.LEATHER, 0, 2)));
        this.add(ModEntities.DEER.get(), LootTable.lootTable()
                .withPool(meat(ModItems.VENISON.get(), 1, 3))
                .withPool(drop(Items.LEATHER, 0, 2)));
        this.add(ModEntities.GOOSE.get(), LootTable.lootTable()
                .withPool(drop(Items.FEATHER, 1, 3))
                .withPool(meat(Items.CHICKEN, 1, 1)));
        this.add(ModEntities.WILL_O_WISP.get(), LootTable.lootTable()
                .withPool(drop(ModItems.WISP_DUST.get(), 1, 2)));
        this.add(ModEntities.CAVE_TROLL.get(), LootTable.lootTable()
                .withPool(drop(Items.LEATHER, 1, 3))
                .withPool(drop(Items.IRON_NUGGET, 3, 8))
                .withPool(drop(Items.BONE, 0, 2))
                .withPool(rare(ModItems.GEMSTONE.get(), 0.12f, 0.04f))
                .withPool(rare(ModItems.TROLL_CLUB.get(), 0.08f, 0.03f)));
        this.add(ModEntities.LINDWORM.get(), LootTable.lootTable()
                .withPool(drop(ModItems.DRAGON_SCALES.get(DragonType.EARTH).get(), 1, 3))
                .withPool(drop(Items.BONE, 1, 3))
                .withPool(rare(ModItems.DRACONIUM.get(), 0.1f, 0.03f)));

        // Kreaturen der Schattenwelt
        this.add(ModEntities.SHADE_STALKER.get(), LootTable.lootTable()
                .withPool(drop(ShadowItems.SHADOW_ESSENCE.get(), 0, 2))
                .withPool(rare(ShadowItems.SOUL_CRYSTAL.get(), 0.05f, 0.02f)));
        this.add(ModEntities.CRYSTAL_CRAWLER.get(), LootTable.lootTable()
                .withPool(drop(ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 1, 3))
                .withPool(rare(ShadowItems.SOUL_CRYSTAL.get(), 0.04f, 0.02f)));
        this.add(ModEntities.NIGHT_WING.get(), LootTable.lootTable()
                .withPool(drop(Items.PHANTOM_MEMBRANE, 0, 2))
                .withPool(drop(ShadowItems.SHADOW_ESSENCE.get(), 0, 1)));
        this.add(ModEntities.MOON_STAG.get(), LootTable.lootTable()
                .withPool(meat(ModItems.VENISON.get(), 1, 3))
                .withPool(drop(Items.LEATHER, 0, 2))
                .withPool(drop(ShadowItems.NIGHT_CRYSTAL_SHARD.get(), 0, 1)));
        this.add(ModEntities.GLIMMER_MOTH.get(), LootTable.lootTable()
                .withPool(drop(Items.GLOWSTONE_DUST, 0, 1)));
        // Umbrer lassen nichts fallen (wie Dorfbewohner), Wächter ein wenig Mondsilber
        this.add(ModEntities.UMBRER.get(), LootTable.lootTable());
        this.add(ModEntities.UMBRER_GUARD.get(), LootTable.lootTable()
                .withPool(drop(ShadowItems.MOONSILVER_NUGGET.get(), 0, 3)));
    }

    /** Fleisch: wird gebraten, wenn das Tier brennend stirbt; Plünderung bringt mehr. */
    private LootPool.Builder meat(ItemLike item, int min, int max) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)))
                        .apply(SmeltItemFunction.smelted().when(this.shouldSmeltLoot()))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f))));
    }

    /** Ein Buch mit einer Verzauberung dieser Mod (aus der Beute-Gruppe {@code tag}), mit {@code chance} Prozent. */
    private LootPool.Builder modBook(net.minecraft.tags.TagKey<net.minecraft.world.item.enchantment.Enchantment> tag, int chance) {
        LootPool.Builder pool = LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(Items.BOOK).setWeight(chance)
                        .apply(net.minecraft.world.level.storage.loot.functions.EnchantRandomlyFunction.randomEnchantment()
                                .withOneOf(this.enchantments.getOrThrow(tag))));
        if (chance < 100) {
            pool.add(net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem().setWeight(100 - chance));
        }
        return pool;
    }

    private LootPool.Builder drop(ItemLike item, int min, int max) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item)
                        .apply(SetItemCountFunction.setCount(ContextIntProviders.between(min, max)))
                        .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f))));
    }

    /** Seltene Beute – nur, wenn ein Spieler das Lebewesen besiegt. */
    private LootPool.Builder rare(ItemLike item, float chance, float lootingBonus) {
        return LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(LootItem.lootTableItem(item))
                .when(LootItemKilledByPlayerCondition.killedByPlayer())
                .when(LootItemRandomChanceWithEnchantedBonusCondition.randomChanceAndLootingBoost(this.enchantments, chance, lootingBonus));
    }

    /**
     * Beute der Krieger der fremden Reiche: ein paar Bronzemünzen (Ritter und Hauptmann mehr), Pfeile bei den Schützen,
     * Eisen und Stahl bei den Gerüsteten; der Herrscher hat Gold, Edelsteine und manchmal einen Diamanten bei sich.
     */
    private LootTable.Builder warriorLoot(io.github.sirschrolli.kingdomsatwar.war.WarriorRole role) {
        int coins = switch (role) {
            case KNIGHT -> 5;
            case CAPTAIN -> 8;
            case LORD -> 24;
            default -> 3;
        };
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.BRONZE_COIN.get())
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(Math.max(1, coins / 2), coins)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments,
                                        ContextFloatProviders.between(0.0f, 1.0f)))));
        switch (role) {
            case ARCHER, CROSSBOWMAN -> table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.ARROW).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 5)))));
            case KNIGHT, CAPTAIN -> table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(ModItems.STEEL_INGOT.get()).setWeight(2))
                    .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(4))
                    .add(net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem().setWeight(6)));
            case LORD -> table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.between(2, 4))
                    .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3))))
                    .add(LootItem.lootTableItem(ModItems.GEMSTONE.get()).setWeight(3))
                    .add(LootItem.lootTableItem(Items.EMERALD).setWeight(3).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 3))))
                    .add(LootItem.lootTableItem(Items.DIAMOND).setWeight(1)))
                    .withPool(this.modBook(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Enchantments.LOOT_REALM, 100));
            default -> table.withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.BREAD).setWeight(2))
                    .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(3).apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 4))))
                    .add(net.minecraft.world.level.storage.loot.entries.EmptyLootItem.emptyItem().setWeight(5)));
        }
        return table;
    }

    private LootTable.Builder banditLoot(boolean archer) {
        LootTable.Builder table = LootTable.lootTable()
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(ModItems.BRONZE_COIN.get())
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 4)))
                                .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f)))))
                .withPool(LootPool.lootPool()
                        .setRolls(ContextIntProviders.exactly(1))
                        .add(LootItem.lootTableItem(Items.GOLD_NUGGET).setWeight(3)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 4))))
                        .add(LootItem.lootTableItem(Items.IRON_NUGGET).setWeight(3)
                                .apply(SetItemCountFunction.setCount(ContextIntProviders.between(1, 5))))
                        .add(LootItem.lootTableItem(Items.BREAD).setWeight(2))
                        .add(LootItem.lootTableItem(Items.LEATHER).setWeight(2))
                        .add(EmptyLootItem.emptyItem().setWeight(4)));
        if (archer) {
            table.withPool(LootPool.lootPool()
                    .setRolls(ContextIntProviders.exactly(1))
                    .add(LootItem.lootTableItem(Items.ARROW)
                            .apply(SetItemCountFunction.setCount(ContextIntProviders.between(0, 3)))
                            .apply(EnchantedCountIncreaseFunction.lootingMultiplier(this.enchantments, ContextFloatProviders.between(0.0f, 1.0f)))));
        }
        table.withPool(this.rare(ModItems.SILVER_COIN.get(), 0.1f, 0.03f));
        return table;
    }

    @Override
    protected Stream<EntityType<?>> getKnownEntityTypes() {
        return ModEntities.ENTITIES.getEntries().stream().map(DeferredHolder::value);
    }

    /** Verweist auf die Beute-Tabelle eines Vanilla-Tiers (die Bedingungen darin greifen auch bei den eigenen Tieren). */
    private LootTable.Builder vanillaLoot(net.minecraft.world.entity.EntityType<?> vanilla) {
        return LootTable.lootTable().withPool(LootPool.lootPool().setRolls(ContextIntProviders.exactly(1))
                .add(net.minecraft.world.level.storage.loot.entries.NestedLootTable.lootTableReference(
                        this.lootTables.getOrThrow(vanilla.getDefaultLootTable().orElseThrow()))));
    }
}
