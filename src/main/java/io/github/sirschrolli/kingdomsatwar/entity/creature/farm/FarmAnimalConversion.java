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
        return Replacements.MAP;
    }

    /** Vanilla-Typ -> eigener Typ (einmal gebaut; die Lieferanten lösen erst beim Umwandeln auf). */
    private static final class Replacements {
        static final Map<EntityType<?>, Supplier<? extends EntityType<? extends Mob>>> MAP = Map.ofEntries(
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
                Map.entry(EntityTypes.TRADER_LLAMA, ModEntities.TRADER_LLAMA),
                // Monster (E4a)
                Map.entry(EntityTypes.ZOMBIE, ModEntities.ZOMBIE),
                Map.entry(EntityTypes.HUSK, ModEntities.HUSK),
                Map.entry(EntityTypes.DROWNED, ModEntities.DROWNED),
                Map.entry(EntityTypes.ZOMBIE_VILLAGER, ModEntities.ZOMBIE_VILLAGER),
                Map.entry(EntityTypes.SKELETON, ModEntities.SKELETON),
                Map.entry(EntityTypes.STRAY, ModEntities.STRAY),
                Map.entry(EntityTypes.BOGGED, ModEntities.BOGGED),
                Map.entry(EntityTypes.PARCHED, ModEntities.PARCHED),
                Map.entry(EntityTypes.CREEPER, ModEntities.CREEPER),
                Map.entry(EntityTypes.SPIDER, ModEntities.SPIDER),
                Map.entry(EntityTypes.CAVE_SPIDER, ModEntities.CAVE_SPIDER),
                Map.entry(EntityTypes.SLIME, ModEntities.SLIME),
                Map.entry(EntityTypes.SULFUR_CUBE, ModEntities.SULFUR_CUBE),
                Map.entry(EntityTypes.WITCH, ModEntities.WITCH),
                Map.entry(EntityTypes.PHANTOM, ModEntities.PHANTOM),
                Map.entry(EntityTypes.SILVERFISH, ModEntities.SILVERFISH),
                Map.entry(EntityTypes.ZOMBIE_HORSE, ModEntities.ZOMBIE_HORSE),
                Map.entry(EntityTypes.SKELETON_HORSE, ModEntities.SKELETON_HORSE),
                Map.entry(EntityTypes.CAMEL_HUSK, ModEntities.CAMEL_HUSK),
                // übrige Monster (E4b)
                Map.entry(EntityTypes.PILLAGER, ModEntities.PILLAGER),
                Map.entry(EntityTypes.VINDICATOR, ModEntities.VINDICATOR),
                Map.entry(EntityTypes.EVOKER, ModEntities.EVOKER),
                Map.entry(EntityTypes.ILLUSIONER, ModEntities.ILLUSIONER),
                Map.entry(EntityTypes.VEX, ModEntities.VEX),
                Map.entry(EntityTypes.RAVAGER, ModEntities.RAVAGER),
                Map.entry(EntityTypes.GUARDIAN, ModEntities.GUARDIAN),
                Map.entry(EntityTypes.ELDER_GUARDIAN, ModEntities.ELDER_GUARDIAN),
                Map.entry(EntityTypes.BREEZE, ModEntities.BREEZE),
                Map.entry(EntityTypes.CREAKING, ModEntities.CREAKING),
                Map.entry(EntityTypes.WARDEN, ModEntities.WARDEN),
                // Wassertiere (E4c)
                Map.entry(EntityTypes.COD, ModEntities.COD),
                Map.entry(EntityTypes.SALMON, ModEntities.SALMON),
                Map.entry(EntityTypes.PUFFERFISH, ModEntities.PUFFERFISH),
                Map.entry(EntityTypes.TROPICAL_FISH, ModEntities.TROPICAL_FISH),
                Map.entry(EntityTypes.SQUID, ModEntities.SQUID),
                Map.entry(EntityTypes.GLOW_SQUID, ModEntities.GLOW_SQUID),
                Map.entry(EntityTypes.DOLPHIN, ModEntities.DOLPHIN),
                Map.entry(EntityTypes.TURTLE, ModEntities.TURTLE),
                Map.entry(EntityTypes.AXOLOTL, ModEntities.AXOLOTL),
                Map.entry(EntityTypes.FROG, ModEntities.FROG),
                Map.entry(EntityTypes.TADPOLE, ModEntities.TADPOLE),
                Map.entry(EntityTypes.NAUTILUS, ModEntities.NAUTILUS),
                Map.entry(EntityTypes.ZOMBIE_NAUTILUS, ModEntities.ZOMBIE_NAUTILUS),
                // Golems und Wichtel (E4d)
                Map.entry(EntityTypes.IRON_GOLEM, ModEntities.IRON_GOLEM),
                Map.entry(EntityTypes.SNOW_GOLEM, ModEntities.SNOW_GOLEM),
                Map.entry(EntityTypes.COPPER_GOLEM, ModEntities.COPPER_GOLEM),
                Map.entry(EntityTypes.ALLAY, ModEntities.ALLAY));
    }

    /** Für die Datengenerierung: das eigene Gegenstück eines Vanilla-Typs ({@code null}, wenn keins). */
    public static @Nullable EntityType<? extends Mob> replacementType(EntityType<?> vanilla) {
        Supplier<? extends EntityType<? extends Mob>> target = replacements().get(vanilla);
        return target == null ? null : target.get();
    }

    /** Merkmal für Vanilla-Wesen, die schon anders ersetzt wurden (geheilte Dorfbewohner) und nicht erscheinen sollen. */
    public static final String REPLACED_TAG = "kingdomsatwar_replaced";

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
        if (mob.entityTags().contains(REPLACED_TAG)) {
            event.setCanceled(true);
            return;
        }
        if (!replacements().containsKey(mob.getType())) {
            return;
        }
        if (mob.isPassenger() || mob.isVehicle() || mob.getType() == EntityTypes.TRADER_LLAMA) {
            // Reiter und Reittiere (Spinnenreiter, Hühnerreiter, Zombiepferde und Dörrkamele mit Reitern) werden am Ende
            // des Ticks als Ganzes ersetzt und wieder aufgesattelt; der fahrende Händler legt sein Lama erst NACH dem
            // Erscheinen an die Leine – auch das wartet bis zum Ende des Ticks, damit die Leine mit umzieht
            PENDING.add(mob);
            return;
        }
        // die UUID bleibt: Prüfkammer-Spawner und Überfälle erkennen ihre Wesen daran (das Original erscheint ja nie)
        Mob replacement = convert(level, mob, true);
        if (replacement != null) {
            event.setCanceled(true);
            level.addFreshEntity(replacement);
            RECENT.add(new Converted(mob, replacement));
        }
    }

    /**
     * Gerade umgewandelte Wesen: Manches geschieht mit dem Original erst NACH seinem Erscheinen im selben Tick – ein
     * Überfall setzt den Reiter erst danach auf den Verwüster, ein Knarzherz bindet seinen Knarz erst danach an sich.
     * Am Ende des Ticks wird das auf die Gegenstücke übertragen.
     */
    private record Converted(Mob original, Mob replacement) {}

    private static final java.util.List<Converted> RECENT = new java.util.ArrayList<>();

    private static void applyLateChanges(ServerLevel level) {
        for (Converted converted : java.util.List.copyOf(RECENT)) {
            Mob original = converted.original();
            Mob replacement = converted.replacement();
            if (replacement.level() != level) {
                continue;
            }
            RECENT.remove(converted);
            if (replacement.isRemoved()) {
                continue;
            }
            // nachträglich aufgesessen: das Gegenstück reitet auf dem, was jetzt unter dieser UUID in der Welt ist
            Entity vehicle = original.getVehicle();
            if (vehicle != null && !replacement.isPassenger()) {
                Entity target = level.getEntity(vehicle.getUUID());
                if (target != null && target != replacement) {
                    replacement.startRiding(target, true, false);
                }
            }
            for (Entity passenger : java.util.List.copyOf(original.getPassengers())) {
                Entity rider = level.getEntity(passenger.getUUID());
                if (rider != null && rider != replacement && rider.getVehicle() != replacement) {
                    rider.stopRiding();
                    rider.startRiding(replacement, true, false);
                }
            }
            // Kupfergolem aus Kupferblock und Kürbis: die Verwitterung des Blocks bekommt er erst nach dem Erscheinen
            if (original instanceof net.minecraft.world.entity.animal.golem.CopperGolem copper
                    && replacement instanceof net.minecraft.world.entity.animal.golem.CopperGolem ours) {
                ours.setWeatherState(copper.getWeatherState());
            }
            // Knarz: Heimat und Herz gehören jetzt dem Gegenstück
            if (original instanceof net.minecraft.world.entity.monster.creaking.Creaking creaking
                    && replacement instanceof net.minecraft.world.entity.monster.creaking.Creaking ours && creaking.getHomePos() != null) {
                net.minecraft.core.BlockPos home = creaking.getHomePos();
                if (ours.getHomePos() == null) {
                    ours.setTransient(home);
                }
                if (level.getBlockEntity(home) instanceof net.minecraft.world.level.block.entity.CreakingHeartBlockEntity heart) {
                    heart.setCreakingInfo(ours);
                    level.broadcastEntityEvent(ours, (byte) 60);
                }
            }
        }
    }

    /** Wesen, die erst am Ende des Ticks ersetzt werden (Reiter, Reittiere, Händlerlamas). */
    private static final java.util.List<Mob> PENDING = new java.util.ArrayList<>();

    @SubscribeEvent
    static void onLevelTick(net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        if (!RECENT.isEmpty()) {
            applyLateChanges(level);
        }
        if (PENDING.isEmpty()) {
            return;
        }
        java.util.Set<Entity> roots = new java.util.LinkedHashSet<>();
        for (Mob mob : java.util.List.copyOf(PENDING)) {
            if (mob.level() == level) {
                PENDING.remove(mob);
                if (!mob.isRemoved()) {
                    roots.add(mob.getRootVehicle());
                }
            }
        }
        for (Entity root : roots) {
            replaceStack(level, root);
        }
    }

    /**
     * Ersetzt alle Vanilla-Wesen eines Reiterstapels (Reittier und Reiter) durch die eigenen und sattelt sie in derselben
     * Reihenfolge wieder auf; Leinen ziehen mit um.
     */
    private static void replaceStack(ServerLevel level, Entity root) {
        java.util.List<Entity> all = new java.util.ArrayList<>();
        all.add(root);
        for (Entity passenger : root.getIndirectPassengers()) {
            all.add(passenger);
        }
        java.util.Map<Entity, Mob> swap = new java.util.LinkedHashMap<>();
        for (Entity entity : all) {
            if (entity instanceof Mob mob && !mob.isRemoved() && mob.isAlive() && replacements().containsKey(mob.getType())) {
                Mob replacement = convert(level, mob, true);
                if (replacement != null) {
                    swap.put(mob, replacement);
                }
            }
        }
        if (swap.isEmpty()) {
            return;
        }
        java.util.Map<Entity, Entity> vehicleOf = new java.util.LinkedHashMap<>();
        for (Entity entity : all) {
            if (entity.getVehicle() != null) {
                vehicleOf.put(entity, entity.getVehicle());
            }
        }
        for (Entity entity : all) {
            entity.stopRiding();
        }
        for (var entry : swap.entrySet()) {
            if (entry.getKey() instanceof Mob original) {
                original.removeLeash();
                original.discard();
            }
            level.addFreshEntity(entry.getValue());
        }
        for (var entry : vehicleOf.entrySet()) {
            Entity rider = swap.containsKey(entry.getKey()) ? swap.get(entry.getKey()) : entry.getKey();
            Entity vehicle = swap.containsKey(entry.getValue()) ? swap.get(entry.getValue()) : entry.getValue();
            rider.startRiding(vehicle, true, false);
        }
    }

    /** Baut das eigene Gegenstück eines Vanilla-Tiers (ohne es der Welt hinzuzufügen); {@code null}, wenn keins. */
    public static @Nullable Mob convert(ServerLevel level, Mob original) {
        return convert(level, original, original instanceof net.minecraft.world.entity.raid.Raider);
    }

    /**
     * Wie {@link #convert(ServerLevel, Mob)}; mit {@code keepUuid} übernimmt das Gegenstück die UUID (nur, wenn das
     * Original nicht in der Welt bleibt).
     */
    private static @Nullable Mob convert(ServerLevel level, Mob original, boolean keepUuid) {
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
        if (!keepUuid) {
            tag.remove("UUID");
        }
        replacement.load(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), tag));
        if (original.getPose() != net.minecraft.world.entity.Pose.STANDING) {
            replacement.setPose(original.getPose());          // z. B. ein Wärter, der gerade aus dem Boden auftaucht
        }

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
            case io.github.sirschrolli.kingdomsatwar.entity.creature.golem.HouseSprite sprite -> {
                // ein verdoppelter Wichtel erscheint genau dort, wo sein Vorbild schwebt – und trägt dieselbe Mütze
                var parent = level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.creature.golem.HouseSprite.class,
                        original.getBoundingBox().inflate(0.25), other -> other != sprite && other.position().distanceToSqr(original.position()) < 1.0e-4);
                if (parent.isEmpty()) {
                    sprite.chooseLook(level, original.blockPosition());
                } else {
                    sprite.setLook(parent.getFirst().getLook());
                }
            }
            case io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Monsters.Varied varied ->
                    varied.chooseLook(level, original.blockPosition());
            default -> { }
        }
        return replacement;
    }

    /** Für Tests und Zubehör: ob ein Typ durch ein eigenes Tier ersetzt wird. */
    public static boolean isReplaced(Entity entity) {
        return replacements().containsKey(entity.getType());
    }
}
