package io.github.sirschrolli.kingdomsatwar.waystone;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import org.jspecify.annotations.Nullable;

/**
 * Die Wegsteine, die ein Wegkompass kennt (höchstens {@link #MAX}), und welcher davon gerade sein Ziel ist ({@code selected};
 * −1: keiner – dann zeigt die Nadel wie bei einem gewöhnlichen Kompass zum Spawn).
 */
public record Waypoints(List<Waypoint> entries, int selected) {
    /** So viele Wegsteine kann sich ein Kompass merken. */
    public static final int MAX = 8;
    public static final Waypoints EMPTY = new Waypoints(List.of(), -1);

    /** Ein gemerkter Wegstein: wo er steht und wie er heißt. */
    public record Waypoint(GlobalPos pos, Component name) {
        public static final Codec<Waypoint> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                GlobalPos.CODEC.fieldOf("pos").forGetter(Waypoint::pos),
                ComponentSerialization.CODEC.fieldOf("name").forGetter(Waypoint::name)).apply(instance, Waypoint::new));
        public static final StreamCodec<RegistryFriendlyByteBuf, Waypoint> STREAM_CODEC = StreamCodec.composite(
                GlobalPos.STREAM_CODEC, Waypoint::pos, ComponentSerialization.STREAM_CODEC, Waypoint::name, Waypoint::new);
    }

    public static final Codec<Waypoints> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Waypoint.CODEC.listOf().fieldOf("entries").forGetter(Waypoints::entries),
            Codec.INT.optionalFieldOf("selected", -1).forGetter(Waypoints::selected)).apply(instance, Waypoints::new));
    public static final StreamCodec<RegistryFriendlyByteBuf, Waypoints> STREAM_CODEC = StreamCodec.composite(
            Waypoint.STREAM_CODEC.apply(ByteBufCodecs.list(MAX)), Waypoints::entries, ByteBufCodecs.VAR_INT, Waypoints::selected, Waypoints::new);

    public Waypoints {
        entries = List.copyOf(entries.size() > MAX ? entries.subList(0, MAX) : entries);
        selected = selected >= 0 && selected < entries.size() ? selected : -1;
    }

    /** Das Ziel (oder null: die Nadel zeigt zum Spawn). */
    public @Nullable Waypoint target() {
        return this.selected >= 0 ? this.entries.get(this.selected) : null;
    }

    /** Wo dieser Wegstein in der Liste steht (−1: unbekannt). */
    public int indexOf(GlobalPos pos) {
        for (int i = 0; i < this.entries.size(); i++) {
            if (this.entries.get(i).pos().equals(pos)) {
                return i;
            }
        }
        return -1;
    }

    public boolean full() {
        return this.entries.size() >= MAX;
    }

    /** Ein Wegstein mehr (hinten angefügt). */
    public Waypoints with(Waypoint waypoint) {
        List<Waypoint> next = new ArrayList<>(this.entries);
        next.add(waypoint);
        return new Waypoints(next, this.selected);
    }

    /** Ein anderes Ziel (−1: keins). */
    public Waypoints select(int index) {
        return new Waypoints(this.entries, index);
    }

    /** Einen Wegstein vergessen (das Ziel bleibt, wenn es ein anderer war). */
    public Waypoints without(int index) {
        if (index < 0 || index >= this.entries.size()) {
            return this;
        }
        List<Waypoint> next = new ArrayList<>(this.entries);
        next.remove(index);
        int selected = this.selected == index ? -1 : this.selected > index ? this.selected - 1 : this.selected;
        return new Waypoints(next, selected);
    }

    /** Ein Wegstein hat einen neuen Namen bekommen. */
    public Waypoints renamed(int index, Component name) {
        if (index < 0 || index >= this.entries.size() || this.entries.get(index).name().equals(name)) {
            return this;
        }
        List<Waypoint> next = new ArrayList<>(this.entries);
        next.set(index, new Waypoint(this.entries.get(index).pos(), name));
        return new Waypoints(next, this.selected);
    }
}
