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

    /**
     * Die Siedlung, die ein Spieler regiert: seine eigene – oder, wenn er keine hat, die, in der er Mitregent ist (seit
     * 1.11.6). Für Garnison, Kasernen, Lager und Ansehen.
     */
    public @Nullable Town ruledBy(UUID player) {
        Town own = this.byOwner(player);
        if (own != null) {
            return own;
        }
        for (Town town : this.towns) {
            if (town.isCoRuler(player)) {
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

    /**
     * Regieren die beiden Spieler eine Siedlung zusammen (Herr und Mitregent, oder zwei Mitregenten)? Dann sind sie
     * Verbündete: Ihre Soldaten kämpfen Seite an Seite.
     */
    public boolean allied(UUID a, UUID b) {
        if (a.equals(b)) {
            return true;
        }
        for (Town town : this.towns) {
            if (town.mayManage(a) && town.mayManage(b)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Die Siedlung, in der ein Spieler hier bauen darf: die, auf deren Gebiet er steht, wenn er sie (mit)regiert – sonst
     * seine eigene.
     */
    public @Nullable Town managedAt(UUID player, ResourceKey<Level> dimension, BlockPos pos) {
        Town here = this.at(dimension, pos);
        if (here != null && here.mayManage(player)) {
            return here;
        }
        return this.byOwner(player);
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
