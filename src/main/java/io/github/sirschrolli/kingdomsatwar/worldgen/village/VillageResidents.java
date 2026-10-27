package io.github.sirschrolli.kingdomsatwar.worldgen.village;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownNames;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/** Setzt die Bewohner und Wächter eines Dorfs beim Erzeugen der Welt. */
final class VillageResidents {
    private VillageResidents() {}

    /** Ein Dorfbewohner mit Beruf, Gesicht, Namen, Bett, Arbeitsplatz und Dorf. */
    static void villager(WorldGenLevel level, BlockPos pos, VillageProfession profession, @Nullable BlockPos bed, @Nullable BlockPos work,
            @Nullable BlockPos door, @Nullable BoundingBox house, BlockPos center, String villageName,
            RandomSource random) {
        VillagerEntity villager = ModEntities.VILLAGER.get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (villager == null) {
            return;
        }
        int variant = random.nextInt(profession.variants());
        boolean feminine = variant == profession.variants() - 1;
        villager.setup(profession, variant, TownNames.citizen(random, feminine), center, villageName, bed, work, random.nextLong());
        villager.setHouse(door, house);
        villager.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
        villager.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        level.addFreshEntityWithPassengers(villager);
    }

    /** Ein Tier (z. B. Schafe auf der Weide) bei (x, y, z) in Gebäude-Koordinaten, wenn die Stelle im aktuellen Chunk liegt. */
    static void animal(io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder b, KingdomVillagePiece piece, int x, int y, int z,
            net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob> type) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return;
        }
        net.minecraft.world.entity.Mob mob = type.create(b.level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (mob == null) {
            return;
        }
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, b.random.nextFloat() * 360.0f, 0.0f);
        mob.finalizeSpawn(b.level, b.level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        mob.setPersistenceRequired();
        b.level.addFreshEntityWithPassengers(mob);
    }

    /** Ein Ruderboot auf dem Wasser bei (x, z) in Gebäude-Koordinaten (sucht die Wasseroberfläche unterhalb). */
    static void boat(io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder b, int x, int z, VillageStyle style) {
        BlockPos top = b.world(x, 0, z);
        if (!b.inside(top)) {
            return;
        }
        for (int dy = 0; dy >= -4; dy--) {
            BlockPos pos = top.above(dy);
            if (b.level.getFluidState(pos).is(net.minecraft.tags.FluidTags.WATER) && b.level.getBlockState(pos.above()).isAir()) {
                net.minecraft.world.entity.EntityType<?> type = switch (style) {
                    case NORDIC -> net.minecraft.world.entity.EntityTypes.SPRUCE_BOAT;
                    case CLAY -> net.minecraft.world.entity.EntityTypes.ACACIA_BOAT;
                    case STILT -> net.minecraft.world.entity.EntityTypes.MANGROVE_BOAT;
                    case TIMBER -> net.minecraft.world.entity.EntityTypes.OAK_BOAT;
                };
                net.minecraft.world.entity.Entity boat = type.create(b.level.getLevel(), EntitySpawnReason.STRUCTURE);
                if (boat != null) {
                    boat.snapTo(pos.getX() + 0.5, pos.getY() + 0.9, pos.getZ() + 0.5, b.random.nextFloat() * 360.0f, 0.0f);
                    b.level.addFreshEntity(boat);
                }
                return;
            }
        }
    }

    /** Ein Wächter des Dorfs: Schwertkämpfer oder Bogenschütze ohne Herrn, der um seinen Posten patrouilliert. */
    static void guard(WorldGenLevel level, BlockPos pos, boolean archer, BlockPos center, String villageName, RandomSource random) {
        SoldierEntity guard = ModEntities.SOLDIERS.get(archer ? io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.ARCHER
                : io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.SWORDSMAN).get().create(level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (guard == null) {
            return;
        }
        guard.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, random.nextFloat() * 360.0f, 0.0f);
        guard.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        guard.guardVillage(villageName, pos);
        guard.setPersistenceRequired();
        level.addFreshEntityWithPassengers(guard);
    }
}
