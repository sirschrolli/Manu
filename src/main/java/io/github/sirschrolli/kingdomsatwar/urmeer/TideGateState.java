package io.github.sirschrolli.kingdomsatwar.urmeer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Alle Gezeitentore einer Welt (Anhang an der Oberwelt, seit 0.89): wo jedes steht (der mittlere Block der untersten
 * Reihe seines Schleiers) und wie es ausgerichtet ist. Eine Reise führt zum nächsten Tor in der anderen Welt – gibt es dort
 * in der Nähe keins, entsteht eins.
 */
public final class TideGateState {
    /**
     * Ein Tor.
     *
     * @param dimension Welt, in der es steht
     * @param foot      mittlerer Block der untersten Reihe des Schleiers (davor kommt man an)
     * @param axis      Ausrichtung (X: der Schleier liegt in Ost-West-Richtung)
     */
    public record Gate(ResourceKey<Level> dimension, BlockPos foot, Direction.Axis axis) {
        public static final Codec<Gate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Gate::dimension),
                BlockPos.CODEC.fieldOf("foot").forGetter(Gate::foot),
                Direction.Axis.CODEC.fieldOf("axis").forGetter(Gate::axis)
        ).apply(instance, Gate::new));
    }

    public static final Codec<TideGateState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Gate.CODEC.listOf().optionalFieldOf("gates", List.of()).forGetter(s -> List.copyOf(s.gates))
    ).apply(instance, TideGateState::new));

    private final List<Gate> gates = new ArrayList<>();

    public TideGateState() {
        this(List.of());
    }

    private TideGateState(List<Gate> gates) {
        this.gates.addAll(gates);
    }

    public static TideGateState of(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.TIDE_GATES);
    }

    public void save(ServerLevel level) {
        level.getServer().overworld().setData(ModAttachments.TIDE_GATES, this);
    }

    public List<Gate> gates() {
        return List.copyOf(this.gates);
    }

    /** Merkt sich ein Tor (ein Tor fast an derselben Stelle wird ersetzt). */
    public void add(Gate gate) {
        this.gates.removeIf(g -> g.dimension().equals(gate.dimension()) && g.foot().distManhattan(gate.foot()) <= 2);
        this.gates.add(gate);
    }

    public void remove(Gate gate) {
        this.gates.remove(gate);
    }

    /** Das nächste Tor in dieser Welt in höchstens {@code radius} Blöcken (waagrecht) um {@code pos}. */
    public @Nullable Gate nearest(ResourceKey<Level> dimension, BlockPos pos, double radius) {
        Gate best = null;
        double bestDistance = radius * radius;
        for (Gate gate : this.gates) {
            if (!gate.dimension().equals(dimension)) {
                continue;
            }
            double dx = gate.foot().getX() - pos.getX();
            double dz = gate.foot().getZ() - pos.getZ();
            double d = dx * dx + dz * dz;
            if (d <= bestDistance) {
                best = gate;
                bestDistance = d;
            }
        }
        return best;
    }
}
