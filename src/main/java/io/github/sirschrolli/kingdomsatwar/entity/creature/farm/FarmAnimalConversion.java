package io.github.sirschrolli.kingdomsatwar.entity.creature.farm;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Map;
import java.util.function.Supplier;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.ProblemReporter;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NonTameRandomTargetGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.chicken.Chicken;
import net.minecraft.world.entity.animal.wolf.Wolf;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jspecify.annotations.Nullable;

/**
 * Ersetzt neu erscheinende Vanilla-Tiere (Spawn-Eier, Befehle, geworfene Eier, Nachwuchs alter Tiere) durch die eigenen
 * Tiere der Mod. Alles Gespeicherte – Gesundheit, Alter, Name, Zähmung, Ausrüstung, Wollfarbe, Hörner – wird
 * übernommen; die Rasse richtet sich nach der Vanilla-Variante oder dem Biom. Schon gespeicherte Tiere bleiben, wie
 * sie sind.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class FarmAnimalConversion {
    private FarmAnimalConversion() {}

    private static Map<EntityType<?>, Supplier<? extends EntityType<? extends Mob>>> replacements() {
        return Map.of(
                EntityTypes.COW, ModEntities.CATTLE,
                EntityTypes.MOOSHROOM, ModEntities.CATTLE,
                EntityTypes.PIG, ModEntities.FARM_PIG,
                EntityTypes.SHEEP, ModEntities.FARM_SHEEP,
                EntityTypes.CHICKEN, ModEntities.FARM_CHICKEN,
                EntityTypes.HORSE, ModEntities.FARM_HORSE,
                EntityTypes.DONKEY, ModEntities.FARM_DONKEY,
                EntityTypes.MULE, ModEntities.FARM_MULE,
                EntityTypes.GOAT, ModEntities.FARM_GOAT,
                EntityTypes.RABBIT, ModEntities.HARE);
    }

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (mob instanceof Wolf wolf) {
            // Vanilla-Wölfe jagen nur Vanilla-Schafe und -Kaninchen – auch die eigenen sollen vor ihnen auf der Hut sein
            wolf.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(wolf, Animal.class, false,
                    (target, targetLevel) -> target instanceof FarmSheep || target instanceof Hare));
            return;
        }
        if (event.loadedFromDisk()) {
            return;
        }
        if (!replacements().containsKey(mob.getType()) || mob.isPassenger() || mob.isVehicle()
                || mob instanceof Chicken chicken && chicken.isChickenJockey()) {
            return;
        }
        Mob replacement = convert(level, mob);
        if (replacement != null) {
            event.setCanceled(true);
            level.addFreshEntity(replacement);
        }
    }

    /** Baut das eigene Gegenstück eines Vanilla-Tiers (ohne es der Welt hinzuzufügen); {@code null}, wenn keins. */
    public static @Nullable Mob convert(ServerLevel level, Mob original) {
        Supplier<? extends EntityType<? extends Mob>> target = replacements().get(original.getType());
        if (target == null) {
            return null;
        }
        Mob replacement = target.get().create(level, EntitySpawnReason.CONVERSION);
        if (replacement == null) {
            return null;
        }
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        original.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        tag.remove("UUID");
        replacement.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));

        Holder<Biome> biome = level.getBiome(original.blockPosition());
        RandomSource random = level.getRandom();
        switch (replacement) {
            case Cattle cattle -> cattle.setBreed(original.getType() == EntityTypes.MOOSHROOM
                    ? Cattle.Breed.PILZ : Cattle.breedFor(biome, random));
            case FarmPig pig -> pig.setBreed(FarmPig.breedFor(biome, random));
            case FarmSheep sheep -> sheep.setBreed(FarmSheep.breedFor(biome, random));
            case FarmHorse horse -> {
                horse.setBreed(FarmHorse.breedFor(biome, random));
                horse.applyBreedBuild();
            }
            case FarmDonkey donkey -> donkey.setBreed(random.nextInt(10) < 7 ? FarmDonkey.Breed.GRAU : FarmDonkey.Breed.BRAUN);
            case FarmMule mule -> mule.setBreed(random.nextInt(4) == 0 ? FarmMule.Breed.RAPPE : FarmMule.Breed.BRAUN);
            case FarmGoat goat -> goat.setBreed(FarmGoat.breedFor(biome, random));
            case FarmChicken chicken -> {
                if (random.nextInt(5) == 0 && !chicken.isBaby()) {
                    chicken.setBreed(FarmChicken.Breed.ROOSTER);
                }
            }
            default -> { }
        }
        return replacement;
    }

    /** Für Tests und Zubehör: ob ein Typ durch ein eigenes Tier ersetzt wird. */
    public static boolean isReplaced(Entity entity) {
        return replacements().containsKey(entity.getType());
    }
}
