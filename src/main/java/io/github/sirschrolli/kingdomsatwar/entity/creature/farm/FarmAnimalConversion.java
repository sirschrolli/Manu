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
import net.minecraft.world.entity.animal.feline.Cat;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.animal.wolf.Wolf;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Bear;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.HouseCat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildArmadillo;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBee;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildCamel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildFox;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildWolf;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Wildcat;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import org.jspecify.annotations.Nullable;

/**
 * Ersetzt neu erscheinende Vanilla-Tiere (Spawn-Eier, Befehle, geworfene Eier, Nachwuchs alter Tiere, Bienen aus
 * Nestern, Katzen in Dörfern, Lamas der Händler, Papageien von der Schulter) durch die eigenen Tiere der Mod. Alles Gespeicherte – Gesundheit, Alter, Name, Zähmung, Ausrüstung, Wollfarbe, Hörner – wird
 * übernommen; die Rasse richtet sich nach der Vanilla-Variante oder dem Biom. Schon gespeicherte Tiere bleiben, wie
 * sie sind.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class FarmAnimalConversion {
    private FarmAnimalConversion() {}

    private static Map<EntityType<?>, Supplier<? extends EntityType<? extends Mob>>> replacements() {
        return Map.ofEntries(
                Map.entry(EntityTypes.COW, ModEntities.CATTLE),
                Map.entry(EntityTypes.MOOSHROOM, ModEntities.CATTLE),
                Map.entry(EntityTypes.PIG, ModEntities.FARM_PIG),
                Map.entry(EntityTypes.SHEEP, ModEntities.FARM_SHEEP),
                Map.entry(EntityTypes.CHICKEN, ModEntities.FARM_CHICKEN),
                Map.entry(EntityTypes.HORSE, ModEntities.FARM_HORSE),
                Map.entry(EntityTypes.DONKEY, ModEntities.FARM_DONKEY),
                Map.entry(EntityTypes.MULE, ModEntities.FARM_MULE),
                Map.entry(EntityTypes.GOAT, ModEntities.FARM_GOAT),
                Map.entry(EntityTypes.RABBIT, ModEntities.HARE),
                // Wildtiere (E3b)
                Map.entry(EntityTypes.WOLF, ModEntities.WOLF),
                Map.entry(EntityTypes.FOX, ModEntities.FOX),
                Map.entry(EntityTypes.POLAR_BEAR, ModEntities.BEAR),
                Map.entry(EntityTypes.PANDA, ModEntities.PANDA),
                Map.entry(EntityTypes.CAT, ModEntities.CAT),
                Map.entry(EntityTypes.OCELOT, ModEntities.WILDCAT),
                Map.entry(EntityTypes.PARROT, ModEntities.PARROT),
                Map.entry(EntityTypes.BEE, ModEntities.BEE),
                Map.entry(EntityTypes.BAT, ModEntities.BAT),
                Map.entry(EntityTypes.ARMADILLO, ModEntities.ARMADILLO),
                Map.entry(EntityTypes.CAMEL, ModEntities.CAMEL),
                Map.entry(EntityTypes.LLAMA, ModEntities.LLAMA),
                Map.entry(EntityTypes.TRADER_LLAMA, ModEntities.TRADER_LLAMA));
    }

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) {
            return;
        }
        if (mob instanceof Wolf wolf && !(mob instanceof WildWolf) && event.loadedFromDisk()) {
            // alte, schon gespeicherte Vanilla-Wölfe jagen nur Vanilla-Schafe und -Kaninchen – auch die eigenen sollen
            // vor ihnen auf der Hut sein (die eigenen Wölfe tun das von selbst)
            wolf.targetSelector.addGoal(5, new NonTameRandomTargetGoal<>(wolf, Animal.class, false,
                    (target, targetLevel) -> target instanceof FarmSheep || target instanceof Hare || target instanceof WildFox));
            return;
        }
        if (event.loadedFromDisk()) {
            return;
        }
        if (!replacements().containsKey(mob.getType()) || mob.isPassenger() || mob.isVehicle()
                || mob instanceof Chicken chicken && chicken.isChickenJockey()) {
            return;
        }
        if (mob.getType() == EntityTypes.TRADER_LLAMA) {
            // Der fahrende Händler legt sein Lama erst NACH dem Erscheinen an die Leine – also bis zum Ende des Ticks
            // warten, damit die Leine mit umzieht
            PENDING.add(mob);
            return;
        }
        Mob replacement = convert(level, mob);
        if (replacement != null) {
            event.setCanceled(true);
            level.addFreshEntity(replacement);
        }
    }

    /** Tiere, die erst am Ende des Ticks ersetzt werden (Händlerlamas). */
    private static final java.util.List<Mob> PENDING = new java.util.ArrayList<>();

    @SubscribeEvent
    static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (PENDING.isEmpty() || !(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (Mob mob : java.util.List.copyOf(PENDING)) {
            if (mob.level() == level) {
                PENDING.remove(mob);
                replaceLater(level, mob);
            }
        }
    }

    private static void replaceLater(ServerLevel level, Mob original) {
        if (original.isRemoved() || !original.isAlive()) {
            return;
        }
        Mob replacement = convert(level, original);
        if (replacement != null) {
            original.removeLeash();
            original.discard();
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
            case WildWolf wolf -> {
                var variant = original.get(net.minecraft.core.component.DataComponents.WOLF_VARIANT);
                wolf.setBreed(variant != null ? WildWolf.fromVanilla(variant) : WildWolf.breedFor(biome, random));
            }
            case WildFox fox -> {
                WildFox.Breed breed = WildFox.breedFor(biome, random);
                if (original instanceof Fox vanilla && vanilla.getVariant() == Fox.Variant.SNOW) {
                    breed = WildFox.Breed.POLAR;
                } else if (breed == WildFox.Breed.POLAR) {
                    breed = WildFox.Breed.ROT;
                }
                fox.setBreed(breed);
            }
            case Bear bear -> bear.setBreed(Bear.Breed.EIS);
            case HouseCat cat -> cat.setBreed(original instanceof Cat vanilla ? HouseCat.fromVanilla(vanilla.getVariant())
                    : HouseCat.Breed.GETIGERT);
            case Wildcat cat -> cat.setBreed(Wildcat.Breed.OZELOT);
            case WildBee bee -> bee.setBreed(WildBee.breedFor(biome, random));
            case WildBat bat -> bat.setBreed(WildBat.breedFor(biome, random));
            case WildArmadillo armadillo -> armadillo.setBreed(WildArmadillo.breedFor(biome, random));
            case WildCamel camel -> camel.setBreed(WildCamel.breedFor(biome));
            default -> { }
        }
        return replacement;
    }

    /** Für Tests und Zubehör: ob ein Typ durch ein eigenes Tier ersetzt wird. */
    public static boolean isReplaced(Entity entity) {
        return replacements().containsKey(entity.getType());
    }
}
