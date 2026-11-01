package io.github.sirschrolli.kingdomsatwar.datagen;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import java.util.concurrent.CompletableFuture;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.entity.EntityType;

/**
 * Tags der Lebewesen: die feindlichen Schattenwesen (gegen die die Mondsilberklinge besonders hart trifft), der
 * Kristallkriecher als Gliederfüßer (Nemesis der Gliederfüßer wirkt).
 */
public class ModEntityTypeTagsProvider extends TagsProvider<EntityType<?>> {
    public ModEntityTypeTagsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider) {
        super(output, Registries.ENTITY_TYPE, lookupProvider, KingdomsAtWar.MODID);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        this.tag(ModTags.EntityTypes.SHADOW_CREATURES)
                .add(ModEntities.SHADE_STALKER.key())
                .add(ModEntities.CRYSTAL_CRAWLER.key())
                .add(ModEntities.NIGHT_WING.key());
        // Münzen: alle Monster der Oberwelt, des Nethers und des Ends, dazu die feindseligen Wesen der Mod
        var coins = this.tag(ModTags.EntityTypes.DROPS_COINS);
        for (String name : java.util.List.of("zombie", "husk", "drowned", "zombie_villager", "skeleton", "stray", "bogged", "creeper",
                "spider", "cave_spider", "witch", "slime", "phantom", "pillager", "vindicator", "evoker", "ravager", "vex", "illusioner",
                "silverfish", "endermite", "enderman", "guardian", "elder_guardian", "shulker", "breeze", "blaze", "ghast",
                "magma_cube", "wither_skeleton", "piglin_brute", "zombified_piglin", "hoglin", "zoglin", "creaking", "parched",
                "camel_husk")) {
            coins.addOptional(net.minecraft.resources.ResourceKey.create(net.minecraft.core.registries.Registries.ENTITY_TYPE,
                    net.minecraft.resources.Identifier.withDefaultNamespace(name)));
        }
        coins.add(ModEntities.CAVE_TROLL.key()).add(ModEntities.LINDWORM.key()).add(ModEntities.SHADE_STALKER.key())
                .add(ModEntities.CRYSTAL_CRAWLER.key()).add(ModEntities.NIGHT_WING.key());
        this.tag(EntityTypeTags.ARTHROPOD).add(ModEntities.CRYSTAL_CRAWLER.key());
        // Eigene Hof- und Weidetiere verhalten sich wie die Vanilla-Tiere, die sie ersetzen
        this.tag(EntityTypeTags.CAN_EQUIP_SADDLE).add(ModEntities.FARM_HORSE.key()).add(ModEntities.FARM_DONKEY.key())
                .add(ModEntities.FARM_MULE.key()).add(ModEntities.FARM_PIG.key());
        this.tag(EntityTypeTags.CAN_WEAR_HORSE_ARMOR).add(ModEntities.FARM_HORSE.key());
        this.tag(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN).add(ModEntities.FARM_HORSE.key()).add(ModEntities.FARM_DONKEY.key())
                .add(ModEntities.FARM_MULE.key());
        this.tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(ModEntities.FARM_HORSE.key()).add(ModEntities.FARM_DONKEY.key())
                .add(ModEntities.FARM_MULE.key()).add(ModEntities.FARM_PIG.key()).add(ModEntities.FARM_CHICKEN.key());
        this.tag(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS).add(ModEntities.CATTLE.key()).add(ModEntities.FARM_PIG.key())
                .add(ModEntities.FARM_SHEEP.key()).add(ModEntities.FARM_CHICKEN.key()).add(ModEntities.FARM_HORSE.key())
                .add(ModEntities.FARM_DONKEY.key()).add(ModEntities.FARM_MULE.key()).add(ModEntities.FARM_GOAT.key())
                .add(ModEntities.HARE.key());
        this.tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(ModEntities.HARE.key());
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.FARM_CHICKEN.key());
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.NIGHT_WING.key()).add(ModEntities.GLIMMER_MOTH.key());
    }
}
