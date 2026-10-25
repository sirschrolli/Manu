package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.loot.ModLootTables;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.common.data.GlobalLootModifierProvider;
import net.neoforged.neoforge.common.loot.AddTableLootModifier;
import net.neoforged.neoforge.common.loot.LootTableIdCondition;

/**
 * Globale Loot-Modifier: hängen zusätzliche Beute an Vanilla-Loot-Tables an.
 */
public class ModLootModifierProvider extends GlobalLootModifierProvider {
    public ModLootModifierProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries, KingdomsAtWar.MODID);
    }

    @Override
    protected void start() {
        seedsFromGrass("seeds_from_short_grass", "blocks/short_grass");
        seedsFromGrass("seeds_from_tall_grass", "blocks/tall_grass");
    }

    private void seedsFromGrass(String name, String vanillaTable) {
        add(name, new AddTableLootModifier(
                Optional.of(Holder.direct(LootTableIdCondition.builder(Identifier.withDefaultNamespace(vanillaTable)).build())),
                0,
                ModLootTables.GRASS_SEEDS));
    }
}
