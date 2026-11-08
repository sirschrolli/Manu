package io.github.sirschrolli.kingdomsatwar.voidrealm;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Alle Leerenrisse einer Welt (Anhang an der Oberwelt): wo jeder Riss steht und wohin er führt. Ein Riss in der Oberwelt
 * (oder einer anderen Welt) und sein Gegenstück auf einer Ankerinsel der Leere sind miteinander verbunden.
 */
public final class RiftState {
    /**
     * Ein Riss.
     *
     * @param dimension Welt, in der er steht
     * @param bottom    unterster Riss-Block (Mitte der Unterkante)
     * @param axis      Ausrichtung (X: die Fläche liegt in Ost-West-Richtung)
     * @param partner   das Gegenstück (falls schon durchschritten)
     */
    public record Rift(ResourceKey<Level> dimension, BlockPos bottom, Direction.Axis axis, Optional<Partner> partner) {
        public static final Codec<Rift> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Rift::dimension),
                BlockPos.CODEC.fieldOf("bottom").forGetter(Rift::bottom),
                Direction.Axis.CODEC.fieldOf("axis").forGetter(Rift::axis),
                Partner.CODEC.optionalFieldOf("partner").forGetter(Rift::partner)
        ).apply(instance, Rift::new));

        Rift withPartner(Partner partner) {
            return new Rift(this.dimension, this.bottom, this.axis, Optional.of(partner));
        }

        /** Mitte des Risses (etwa in Kopfhöhe). */
        public BlockPos center() {
            return this.bottom.above(VoidGate.RIFT_HEIGHT / 2);
        }
    }

    /** Das Gegenstück: Welt und unterster Riss-Block. */
    public record Partner(ResourceKey<Level> dimension, BlockPos bottom) {
        public static final Codec<Partner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Partner::dimension),
                BlockPos.CODEC.fieldOf("bottom").forGetter(Partner::bottom)
        ).apply(instance, Partner::new));
    }

    public static final Codec<RiftState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Rift.CODEC.listOf().optionalFieldOf("rifts", List.of()).forGetter(s -> List.copyOf(s.rifts))
    ).apply(instance, RiftState::new));

    private final List<Rift> rifts = new ArrayList<>();

    public RiftState() {
        this(List.of());
    }

    private RiftState(List<Rift> rifts) {
        this.rifts.addAll(rifts);
    }

    public static RiftState of(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.RIFTS);
    }

    public void save(ServerLevel level) {
        level.getServer().overworld().setData(ModAttachments.RIFTS, this);
    }

    public List<Rift> rifts() {
        return List.copyOf(this.rifts);
    }

    public void add(Rift rift) {
        this.rifts.add(rift);
    }

    public void remove(Rift rift) {
        this.rifts.remove(rift);
    }

    /** Ersetzt einen Riss (gleiche Welt, gleiche Unterkante) durch eine neue Fassung. */
    public void replace(Rift old, Rift updated) {
        int i = this.rifts.indexOf(old);
        if (i >= 0) {
            this.rifts.set(i, updated);
        }
    }

    /** Der Riss in dieser Welt, zu dem der Block {@code pos} gehört (oder der nächste in wenigen Blöcken). */
    public @Nullable Rift near(ResourceKey<Level> dimension, BlockPos pos, double maxDistance) {
        Rift best = null;
        double bestDistance = maxDistance * maxDistance;
        for (Rift rift : this.rifts) {
            if (rift.dimension() != dimension && !rift.dimension().equals(dimension)) {
                continue;
            }
            double d = rift.center().distSqr(pos);
            if (d <= bestDistance) {
                best = rift;
                bestDistance = d;
            }
        }
        return best;
    }

    /** Der Riss mit dieser Unterkante. */
    public @Nullable Rift at(ResourceKey<Level> dimension, BlockPos bottom) {
        for (Rift rift : this.rifts) {
            if (rift.dimension().equals(dimension) && rift.bottom().equals(bottom)) {
                return rift;
            }
        }
        return null;
    }
}
