package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.enchant.ModEnchantments;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.EnchantmentTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.tags.EnchantmentTags;
import net.minecraft.world.item.enchantment.Enchantment;

/**
 * Verzauberungs-Tags: Die Verzauberungen der Mod sind Schätze (kein Zaubertisch, kein Händler), Drachentöter schließt
 * Schärfe & Co. aus, Drachenschuppe die anderen Schutzverzauberungen; der Fluch der Schwere ist ein Fluch und kann auf
 * zufällig verzauberter Beute liegen. Dazu, wo welche Verzauberungen als Beute liegen.
 */
public class ModEnchantmentTagsProvider extends EnchantmentTagsProvider {
    public ModEnchantmentTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        var treasure = this.tag(EnchantmentTags.TREASURE);
        var order = this.tag(EnchantmentTags.TOOLTIP_ORDER);
        for (ResourceKey<Enchantment> enchantment : ModEnchantments.ALL) {
            treasure.add(enchantment);
            order.add(enchantment);
        }
        this.tag(EnchantmentTags.DAMAGE_EXCLUSIVE).add(ModEnchantments.DRAGONSLAYER);
        this.tag(EnchantmentTags.ARMOR_EXCLUSIVE).add(ModEnchantments.DRAGONSCALE);
        this.tag(EnchantmentTags.CURSE).add(ModEnchantments.WEIGHT_CURSE);
        this.tag(EnchantmentTags.ON_RANDOM_LOOT).add(ModEnchantments.WEIGHT_CURSE);
        this.tag(ModTags.Enchantments.LOOT_REALM).add(ModEnchantments.LIFESTEAL, ModEnchantments.ARMOR_BREAKER, ModEnchantments.VOLLEY,
                ModEnchantments.WINDSTRIDE);
        this.tag(ModTags.Enchantments.LOOT_DRAGON).add(ModEnchantments.DRAGONSLAYER, ModEnchantments.DRAGONSCALE);
        this.tag(ModTags.Enchantments.LOOT_ARCANE).add(ModEnchantments.FROSTBITE, ModEnchantments.LIFESTEAL, ModEnchantments.WINDSTRIDE);
    }
}
