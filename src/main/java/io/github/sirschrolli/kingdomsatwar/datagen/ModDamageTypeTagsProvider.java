package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModDamageTypes;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.DamageTypeTagsProvider;
import net.minecraft.tags.DamageTypeTags;

/**
 * Tags der eigenen Schadensarten: Steinkugeln und Bolzen sind Geschosse (ein Schild fängt sie ab), der Rammbock und die
 * Steinkugel treffen auch den Helm.
 */
public class ModDamageTypeTagsProvider extends DamageTypeTagsProvider {
    public ModDamageTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, lookupProvider, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(DamageTypeTags.IS_PROJECTILE).add(ModDamageTypes.SIEGE_STONE, ModDamageTypes.BALLISTA_BOLT);
        this.tag(DamageTypeTags.DAMAGES_HELMET).add(ModDamageTypes.SIEGE_STONE);
        this.tag(DamageTypeTags.NO_KNOCKBACK).add(ModDamageTypes.SIEGE_STONE, ModDamageTypes.BALLISTA_BOLT, ModDamageTypes.BATTERING_RAM,
                ModDamageTypes.DIVINE);
        // das Licht des Schöpfers: durch jede Rüstung und jeden Schild (Verzauberungen und Widerstand helfen weiter)
        this.tag(DamageTypeTags.BYPASSES_ARMOR).add(ModDamageTypes.DIVINE);
        this.tag(DamageTypeTags.BYPASSES_SHIELD).add(ModDamageTypes.DIVINE);
    }
}
