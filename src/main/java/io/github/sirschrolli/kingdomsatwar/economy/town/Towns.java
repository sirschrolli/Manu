package io.github.sirschrolli.kingdomsatwar.economy.town;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Alle Siedlungen einer Welt (Anhang an der Oberwelt, wird mit dem Spielstand gespeichert). Jeder Spieler hat höchstens
 * eine Siedlung.
 */
public final class Towns {
    public static final Codec<Towns> CODEC = Town.CODEC.listOf().xmap(Towns::new, towns -> List.copyOf(towns.towns));

    private final List<Town> towns = new ArrayList<>();

    public Towns() {
    }

    private Towns(List<Town> towns) {
        this.towns.addAll(towns);
    }

    public static Towns of(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.TOWNS);
    }

    /** Merkt die Änderungen zum Speichern vor. */
    public void save(MinecraftServer server) {
        server.overworld().setData(ModAttachments.TOWNS, this);
    }

    public List<Town> all() {
        return List.copyOf(this.towns);
    }

    public @Nullable Town byOwner(UUID owner) {
        for (Town town : this.towns) {
            if (town.owner().equals(owner)) {
                return town;
            }
        }
        return null;
    }

    public @Nullable Town byId(UUID id) {
        for (Town town : this.towns) {
            if (town.id().equals(id)) {
                return town;
            }
        }
        return null;
    }

    /** Die Siedlung, deren Gebiet diese Stelle umfasst (die nächstgelegene, falls mehrere). */
    public @Nullable Town at(ResourceKey<Level> dimension, BlockPos pos) {
        Town best = null;
        double bestDist = Double.MAX_VALUE;
        for (Town town : this.towns) {
            if (town.contains(dimension, pos)) {
                double d = town.banner().distSqr(pos);
                if (d < bestDist) {
                    best = town;
                    bestDist = d;
                }
            }
        }
        return best;
    }

    /** Die Siedlung, deren Banner hier steht. */
    public @Nullable Town byBanner(ResourceKey<Level> dimension, BlockPos pos) {
        for (Town town : this.towns) {
            if (town.hasBanner() && town.dimension().equals(dimension) && town.banner().equals(pos)) {
                return town;
            }
        }
        return null;
    }

    /** Die Siedlung, zu der ein Bürger gehört. */
    public @Nullable Town byResident(UUID entity) {
        for (Town town : this.towns) {
            if (town.resident(entity) != null) {
                return town;
            }
        }
        return null;
    }

    void add(Town town) {
        this.towns.add(town);
    }

    void remove(Town town) {
        this.towns.remove(town);
    }
}
