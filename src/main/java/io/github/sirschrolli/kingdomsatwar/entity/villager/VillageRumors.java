package io.github.sirschrolli.kingdomsatwar.entity.villager;

import io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading;
import io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageLocator;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import org.jspecify.annotations.Nullable;

/**
 * Was sich die Dorfbewohner erzählen (geduckter Rechtsklick): wo ein Drachennest liegen soll, wo der alte Steinkreis
 * steht, welches Dorf das nächste ist – oder einfach, was sie gerade beschäftigt. Richtungen und Entfernungen stimmen;
 * so wird jedes Dorf zu einem Ort, an dem man erfährt, wohin die Reise gehen kann.
 */
public final class VillageRumors {
    /** Anzahl der allgemeinen Sätze ({@code villager.kingdomsatwar.rumor.generic.N}). */
    public static final int GENERIC = 14;
    /** Sätze je Beruf ({@code villager.kingdomsatwar.rumor.<beruf>.N}). */
    public static final int PER_PROFESSION = 2;

    private VillageRumors() {}

    public static void talk(ServerPlayer player, VillagerEntity villager) {
        Component line = line(player.level(), villager, villager.getRandom());
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.villager.says", villager.describe().copy().withStyle(ChatFormatting.GOLD),
                line.copy().withStyle(ChatFormatting.WHITE)));
        villager.level().playSound(null, villager.getX(), villager.getY(), villager.getZ(), SoundEvents.VILLAGER_AMBIENT, SoundSource.NEUTRAL, 0.5f,
                villager.getVoicePitch());
    }

    /** Ein Satz des Bewohners. */
    static Component line(ServerLevel level, VillagerEntity villager, RandomSource random) {
        ServerLevel overworld = level.getServer().overworld();
        BlockPos from = villager.blockPosition();
        int roll = random.nextInt(100);
        if (level.isRaining() && roll < 12) {
            return Component.translatable("villager.kingdomsatwar.rumor.rain");
        }
        if (level == overworld) {
            if (roll < 28) {
                BlockPos nest = Trading.nearestNest(overworld, from);
                if (nest != null) {
                    return Component.translatable("villager.kingdomsatwar.rumor.nest", direction(from, nest), distance(from, nest));
                }
            } else if (roll < 38) {
                BlockPos seal = SealCircleLocator.approximateCenter(overworld);
                if (seal != null) {
                    return Component.translatable("villager.kingdomsatwar.rumor.seal", direction(from, seal), distance(from, seal));
                }
            } else if (roll < 52) {
                VillageLocator.Found other = neighbour(overworld, villager);
                if (other != null) {
                    return Component.translatable("villager.kingdomsatwar.rumor.village", direction(from, other.center()), other.name(),
                            distance(from, other.center()));
                }
            }
        }
        if (roll < 78) {
            return Component.translatable("villager.kingdomsatwar.rumor." + villager.getProfession().id() + "." + (1 + random.nextInt(PER_PROFESSION)));
        }
        return Component.translatable("villager.kingdomsatwar.rumor.generic." + (1 + random.nextInt(GENERIC)));
    }

    /** Das nächste andere Dorf (die Suche läuft nur einmal je Bewohner und Sitzung). */
    private static VillageLocator.@Nullable Found neighbour(ServerLevel overworld, VillagerEntity villager) {
        if (villager.neighbourSearched) {
            return villager.neighbour;
        }
        BlockPos center = villager.getVillageCenter();
        villager.neighbour = VillageLocator.nearest(overworld, center != null ? center : villager.blockPosition(),
                center == null ? null : VillageLocator.startChunk(center), 2);
        villager.neighbourSearched = true;
        return villager.neighbour;
    }

    /** Die Himmelsrichtung von {@code from} nach {@code to} (acht Richtungen). */
    public static Component direction(BlockPos from, BlockPos to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();
        double degrees = Math.toDegrees(Math.atan2(dx, -dz));
        int sector = Math.floorMod((int) Math.round(degrees / 45.0), 8);
        String[] names = {"north", "northeast", "east", "southeast", "south", "southwest", "west", "northwest"};
        return Component.translatable("direction.kingdomsatwar." + names[sector]);
    }

    /** Ungefähre Entfernung in Schritten (gerundet, wie man sie schätzt). */
    public static String distance(BlockPos from, BlockPos to) {
        double d = Math.sqrt(Math.pow(to.getX() - from.getX(), 2) + Math.pow(to.getZ() - from.getZ(), 2));
        int step = d < 500 ? 50 : d < 3000 ? 100 : 500;
        return String.valueOf(Math.max(step, (int) Math.round(d / step) * step));
    }
}
