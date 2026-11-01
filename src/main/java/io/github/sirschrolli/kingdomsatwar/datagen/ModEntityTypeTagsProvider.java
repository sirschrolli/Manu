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
        // Eigene Wildtiere verhalten sich wie die Vanilla-Wildtiere, die sie ersetzen
        this.tag(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS).add(ModEntities.WOLF.key()).add(ModEntities.FOX.key()).add(ModEntities.BEAR.key())
                .add(ModEntities.PANDA.key()).add(ModEntities.CAT.key()).add(ModEntities.WILDCAT.key()).add(ModEntities.PARROT.key())
                .add(ModEntities.BEE.key()).add(ModEntities.ARMADILLO.key()).add(ModEntities.CAMEL.key()).add(ModEntities.LLAMA.key());
        this.tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(ModEntities.FOX.key());
        this.tag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(ModEntities.BEAR.key());
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.CAT.key()).add(ModEntities.WILDCAT.key()).add(ModEntities.PARROT.key())
                .add(ModEntities.BEE.key()).add(ModEntities.BAT.key());
        this.tag(EntityTypeTags.ARTHROPOD).add(ModEntities.BEE.key());
        this.tag(EntityTypeTags.BEEHIVE_INHABITORS).add(ModEntities.BEE.key());
        this.tag(EntityTypeTags.CAN_EQUIP_SADDLE).add(ModEntities.CAMEL.key());
        this.tag(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN).add(ModEntities.CAMEL.key());
        this.tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(ModEntities.CAMEL.key()).add(ModEntities.LLAMA.key())
                .add(ModEntities.TRADER_LLAMA.key());
        // Eigene Monster (E4a): in denselben Vanilla-Tags wie die Monster, die sie ersetzen (Untote: Bann wirkt, Heilen schadet,
        // Wither verschont sie; Gliederfüßer: Nemesis der Gliederfüßer; Plünderer: Hexe im Überfall)
        this.tag(EntityTypeTags.ZOMBIES).add(ModEntities.ZOMBIE.key()).add(ModEntities.HUSK.key()).add(ModEntities.DROWNED.key())
                .add(ModEntities.ZOMBIE_VILLAGER.key()).add(ModEntities.ZOMBIE_HORSE.key()).add(ModEntities.CAMEL_HUSK.key());
        this.tag(EntityTypeTags.SKELETONS).add(ModEntities.SKELETON.key()).add(ModEntities.STRAY.key()).add(ModEntities.BOGGED.key())
                .add(ModEntities.PARCHED.key()).add(ModEntities.SKELETON_HORSE.key());
        this.tag(EntityTypeTags.UNDEAD).add(ModEntities.PHANTOM.key());
        this.tag(EntityTypeTags.BURN_IN_DAYLIGHT).add(ModEntities.ZOMBIE.key()).add(ModEntities.DROWNED.key())
                .add(ModEntities.ZOMBIE_VILLAGER.key()).add(ModEntities.SKELETON.key()).add(ModEntities.STRAY.key())
                .add(ModEntities.BOGGED.key()).add(ModEntities.PHANTOM.key()).add(ModEntities.ZOMBIE_HORSE.key());
        this.tag(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE).add(ModEntities.ZOMBIE.key()).add(ModEntities.HUSK.key())
                .add(ModEntities.SKELETON.key()).add(ModEntities.STRAY.key()).add(ModEntities.BOGGED.key()).add(ModEntities.SPIDER.key())
                .add(ModEntities.CAVE_SPIDER.key()).add(ModEntities.SLIME.key());
        this.tag(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES).add(ModEntities.DROWNED.key());
        this.tag(EntityTypeTags.FREEZE_IMMUNE_ENTITY_TYPES).add(ModEntities.STRAY.key());
        this.tag(EntityTypeTags.ARTHROPOD).add(ModEntities.SPIDER.key()).add(ModEntities.CAVE_SPIDER.key()).add(ModEntities.SILVERFISH.key());
        this.tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(ModEntities.SPIDER.key()).add(ModEntities.ZOMBIE_HORSE.key());
        this.tag(EntityTypeTags.FROG_FOOD).add(ModEntities.SLIME.key());
        this.tag(EntityTypeTags.IMMUNE_TO_OOZING).add(ModEntities.SLIME.key());
        this.tag(EntityTypeTags.NON_CONTROLLING_RIDER).add(ModEntities.SLIME.key()).add(ModEntities.SULFUR_CUBE.key());
        this.tag(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS).add(ModEntities.SULFUR_CUBE.key());
        this.tag(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(ModEntities.SULFUR_CUBE.key());
        this.tag(EntityTypeTags.RAIDERS).add(ModEntities.WITCH.key());
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.PHANTOM.key());
        this.tag(EntityTypeTags.IMMUNE_TO_INFESTED).add(ModEntities.SILVERFISH.key());
        this.tag(EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS).add(ModEntities.SILVERFISH.key());
        this.tag(EntityTypeTags.CAN_EQUIP_SADDLE).add(ModEntities.ZOMBIE_HORSE.key()).add(ModEntities.SKELETON_HORSE.key())
                .add(ModEntities.CAMEL_HUSK.key());
        this.tag(EntityTypeTags.CAN_FLOAT_WHILE_RIDDEN).add(ModEntities.ZOMBIE_HORSE.key()).add(ModEntities.CAMEL_HUSK.key());
        this.tag(EntityTypeTags.CAN_WEAR_HORSE_ARMOR).add(ModEntities.ZOMBIE_HORSE.key());
        this.tag(EntityTypeTags.CANNOT_BE_AGE_LOCKED).add(ModEntities.ZOMBIE_HORSE.key()).add(ModEntities.SKELETON_HORSE.key());
        this.tag(EntityTypeTags.FOLLOWABLE_FRIENDLY_MOBS).add(ModEntities.SKELETON_HORSE.key());
        // übrige Monster (E4b): Illager (Überfälle, Bann der Illager-Freunde), Wasserwesen, Windgeist
        this.tag(EntityTypeTags.ILLAGER).add(ModEntities.PILLAGER.key()).add(ModEntities.VINDICATOR.key()).add(ModEntities.EVOKER.key())
                .add(ModEntities.ILLUSIONER.key());
        this.tag(EntityTypeTags.RAIDERS).add(ModEntities.PILLAGER.key()).add(ModEntities.VINDICATOR.key()).add(ModEntities.EVOKER.key())
                .add(ModEntities.ILLUSIONER.key()).add(ModEntities.RAVAGER.key());
        this.tag(EntityTypeTags.DISMOUNTS_UNDERWATER).add(ModEntities.RAVAGER.key());
        this.tag(EntityTypeTags.AQUATIC).add(ModEntities.GUARDIAN.key()).add(ModEntities.ELDER_GUARDIAN.key());
        this.tag(EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES).add(ModEntities.GUARDIAN.key()).add(ModEntities.ELDER_GUARDIAN.key());
        this.tag(EntityTypeTags.CAN_BREATHE_UNDER_WATER).add(ModEntities.GUARDIAN.key()).add(ModEntities.ELDER_GUARDIAN.key());
        this.tag(EntityTypeTags.NOT_SCARY_FOR_PUFFERFISH).add(ModEntities.GUARDIAN.key()).add(ModEntities.ELDER_GUARDIAN.key());
        this.tag(EntityTypeTags.CANNOT_BE_PUSHED_ONTO_BOATS).add(ModEntities.ELDER_GUARDIAN.key()).add(ModEntities.CREAKING.key());
        this.tag(EntityTypeTags.CAN_TURN_IN_BOATS).add(ModEntities.BREEZE.key());
        this.tag(EntityTypeTags.DEFLECTS_PROJECTILES).add(ModEntities.BREEZE.key());
        this.tag(EntityTypeTags.FALL_DAMAGE_IMMUNE).add(ModEntities.BREEZE.key());
        this.tag(EntityTypeTags.NO_ANGER_FROM_WIND_CHARGE).add(ModEntities.BREEZE.key());
        var monsterCoins = this.tag(ModTags.EntityTypes.DROPS_COINS);
        for (var monster : java.util.List.of(ModEntities.ZOMBIE, ModEntities.HUSK, ModEntities.DROWNED, ModEntities.ZOMBIE_VILLAGER,
                ModEntities.SKELETON, ModEntities.STRAY, ModEntities.BOGGED, ModEntities.PARCHED, ModEntities.CREEPER, ModEntities.SPIDER,
                ModEntities.CAVE_SPIDER, ModEntities.SLIME, ModEntities.WITCH, ModEntities.PHANTOM, ModEntities.SILVERFISH,
                ModEntities.CAMEL_HUSK, ModEntities.PILLAGER, ModEntities.VINDICATOR, ModEntities.EVOKER, ModEntities.ILLUSIONER,
                ModEntities.VEX, ModEntities.RAVAGER, ModEntities.GUARDIAN, ModEntities.ELDER_GUARDIAN, ModEntities.BREEZE,
                ModEntities.CREAKING)) {
            monsterCoins.add(monster.key());
        }
    }
}
