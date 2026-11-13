package io.github.sirschrolli.kingdomsatwar.fae;

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
 * Alle Feenportale einer Welt (Anhang an der Oberwelt): wo jedes steht (unterster Portalblock links, Ausrichtung,
 * Breite). Eine Reise führt zum nächsten Portal in der anderen Welt – gibt es dort in der Nähe keins, entsteht eins.
 */
public final class FaeGateState {
    /**
     * Ein Portal.
     *
     * @param dimension Welt, in der es steht
     * @param corner    unterster, erster Portalblock (der Rahmen liegt einen Block darunter und daneben)
     * @param axis      Ausrichtung (X: die Fläche liegt in Ost-West-Richtung)
     * @param width     Breite der Portalfläche
     */
    public record Gate(ResourceKey<Level> dimension, BlockPos corner, Direction.Axis axis, int width) {
        public static final Codec<Gate> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceKey.codec(Registries.DIMENSION).fieldOf("dimension").forGetter(Gate::dimension),
                BlockPos.CODEC.fieldOf("corner").forGetter(Gate::corner),
                Direction.Axis.CODEC.fieldOf("axis").forGetter(Gate::axis),
                Codec.INT.optionalFieldOf("width", 2).forGetter(Gate::width)
        ).apply(instance, Gate::new));

        /** Mitte der Unterkante (dort kommt man an). */
        public BlockPos foot() {
            Direction along = this.axis == Direction.Axis.X ? Direction.EAST : Direction.SOUTH;
            return this.corner.relative(along, this.width / 2);
        }
    }

    public static final Codec<FaeGateState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Gate.CODEC.listOf().optionalFieldOf("gates", List.of()).forGetter(s -> List.copyOf(s.gates))
    ).apply(instance, FaeGateState::new));

    private final List<Gate> gates = new ArrayList<>();

    public FaeGateState() {
        this(List.of());
    }

    private FaeGateState(List<Gate> gates) {
        this.gates.addAll(gates);
    }

    public static FaeGateState of(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.FAE_GATES);
    }

    public void save(ServerLevel level) {
        level.getServer().overworld().setData(ModAttachments.FAE_GATES, this);
    }

    public List<Gate> gates() {
        return List.copyOf(this.gates);
    }

    public void add(Gate gate) {
        this.gates.removeIf(g -> g.dimension().equals(gate.dimension()) && g.corner().equals(gate.corner()));
        this.gates.add(gate);
    }

    public void remove(Gate gate) {
        this.gates.remove(gate);
    }

    /** Das nächste Portal in dieser Welt in höchstens {@code radius} Blöcken (waagrecht) um {@code pos}. */
    public @Nullable Gate nearest(ResourceKey<Level> dimension, BlockPos pos, double radius) {
        Gate best = null;
        double bestDistance = radius * radius;
        for (Gate gate : this.gates) {
            if (!gate.dimension().equals(dimension)) {
                continue;
            }
            double dx = gate.corner().getX() - pos.getX();
            double dz = gate.corner().getZ() - pos.getZ();
            double d = dx * dx + dz * dz;
            if (d <= bestDistance) {
                best = gate;
                bestDistance = d;
            }
        }
        return best;
    }
}
