package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.level.levelgen.structure.Structure;

/**
 * Bauwerk-Tags: Wohin Enderaugen fliegen (zum Sternensturz), wohin Schatzkarten (zum Gezeitenschrein) und
 * Entdeckerkarten führen, was als Dorf, Mine (Wasserstollen) oder Wrack (Seedrachen-Gebeine) gilt – alles zeigt jetzt auf
 * die eigenen Bauwerke; die Vanilla-Bauwerke, deren Rolle sie übernehmen, entstehen nicht mehr.
 */
public class ModStructureTagsProvider extends TagsProvider<Structure> {
    public ModStructureTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.STRUCTURE, lookupProvider, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(StructureTags.EYE_OF_ENDER_LOCATED).replace().add(ModStructures.site(SiteKind.FALLEN_STAR));
        this.tag(StructureTags.ON_TREASURE_MAPS).replace().add(ModStructures.site(SiteKind.TIDE_SHRINE));
        this.tag(StructureTags.MINESHAFT).replace().add(ModStructures.site(SiteKind.AQUEDUCT));
        this.tag(StructureTags.ON_MINESHAFT_MAPS).replace().add(ModStructures.site(SiteKind.AQUEDUCT));
        this.tag(StructureTags.SHIPWRECK).replace().add(ModStructures.site(SiteKind.SEA_DRAGON));
        this.tag(StructureTags.ON_DESERT_PYRAMID_MAPS).replace().add(ModStructures.site(SiteKind.CARAVANSERAI));
        this.tag(StructureTags.ON_JUNGLE_PYRAMID_MAPS).replace().add(ModStructures.site(SiteKind.SERPENT_TEMPLE));
        this.tag(StructureTags.ON_SWAMP_HUT_MAPS).replace().add(ModStructures.site(SiteKind.WITCH_TREE));
        this.tag(StructureTags.CATS_SPAWN_IN).replace().add(ModStructures.site(SiteKind.WITCH_TREE));
        this.tag(StructureTags.CATS_SPAWN_AS_BLACK).replace().add(ModStructures.site(SiteKind.WITCH_TREE));
        // Portalruinen: die Glutpforte in der Oberwelt, die Verlorene Pforte im Nether
        this.tag(StructureTags.RUINED_PORTAL).replace().add(ModStructures.site(SiteKind.EMBER_GATE))
                .add(ModStructures.site(SiteKind.LOST_GATE));
        this.tag(StructureTags.ABANDONED_CAMP).replace().add(ModStructures.site(SiteKind.CHARCOAL_CAMP));
        this.tag(StructureTags.OCEAN_RUIN).replace().add(ModStructures.site(SiteKind.CLAM_FIELD));
        this.tag(StructureTags.ON_OCEAN_RUIN_WARM_MAPS).replace().add(ModStructures.site(SiteKind.CLAM_FIELD));
        this.tag(StructureTags.ON_OCEAN_MONUMENT_MAPS).replace().add(ModStructures.site(SiteKind.CORAL_PALACE));
        this.tag(StructureTags.ON_ANCIENT_CITY_MAPS).replace().add(ModStructures.site(SiteKind.SILENT_CATHEDRAL));
        this.tag(StructureTags.ON_BURIED_TRIAL_CHAMBERS_MAPS).replace().add(ModStructures.site(SiteKind.TRIAL_ARENA));
        this.tag(StructureTags.ON_WOODLAND_MANSION_MAPS).replace().add(ModStructures.site(SiteKind.ROBBER_CASTLE));
        for (var map : java.util.List.of(StructureTags.ON_ABANDONED_CAMP_BAMBOO_JUNGLE_MAPS, StructureTags.ON_ABANDONED_CAMP_CHERRY_GROVE_MAPS,
                StructureTags.ON_ABANDONED_CAMP_BIRCH_FOREST_MAPS, StructureTags.ON_ABANDONED_CAMP_DAPPLED_FOREST_MAPS,
                StructureTags.ON_ABANDONED_CAMP_FLOWER_FOREST_MAPS, StructureTags.ON_ABANDONED_CAMP_PALE_GARDEN_MAPS,
                StructureTags.ON_ABANDONED_CAMP_SWAMP_MAPS, StructureTags.ON_ABANDONED_CAMP_WINDSWEPT_FOREST_MAPS)) {
            this.tag(map).replace().add(ModStructures.site(SiteKind.CHARCOAL_CAMP));
        }
        // Dörfer: alle vier Bauweisen; die Karten der alten Dörfer zeigen auf die passende Bauweise
        var village = this.tag(StructureTags.VILLAGE).replace();
        for (VillageStyle style : VillageStyle.values()) {
            village.add(ModStructures.village(style));
        }
        this.tag(StructureTags.ON_PLAINS_VILLAGE_MAPS).replace().add(ModStructures.village(VillageStyle.TIMBER));
        this.tag(StructureTags.ON_TAIGA_VILLAGE_MAPS).replace().add(ModStructures.village(VillageStyle.NORDIC));
        this.tag(StructureTags.ON_SNOWY_VILLAGE_MAPS).replace().add(ModStructures.village(VillageStyle.NORDIC));
        this.tag(StructureTags.ON_DESERT_VILLAGE_MAPS).replace().add(ModStructures.village(VillageStyle.CLAY));
        this.tag(StructureTags.ON_SAVANNA_VILLAGE_MAPS).replace().add(ModStructures.village(VillageStyle.CLAY));
    }
}
