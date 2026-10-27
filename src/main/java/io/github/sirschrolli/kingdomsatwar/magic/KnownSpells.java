package io.github.sirschrolli.kingdomsatwar.magic;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

/**
 * Die Zauber, die ein Aetherit-Stab kennt (mit ihrer Stufe 1–3), und welcher davon gerade gewählt ist. Den
 * Aether-Blitz kennt jeder Stab; alles andere lernt er am Magietisch.
 */
public record KnownSpells(Map<Spell, Integer> levels, Spell selected) {
    /** Ein neuer Stab: nur der Aether-Blitz auf Stufe 1. */
    public static final KnownSpells DEFAULT = new KnownSpells(Map.of(Spell.AETHER_BOLT, 1), Spell.AETHER_BOLT);

    public static final Codec<KnownSpells> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.unboundedMap(Spell.CODEC, Codec.intRange(1, Spell.MAX_LEVEL)).fieldOf("levels").forGetter(KnownSpells::levels),
            Spell.CODEC.optionalFieldOf("selected", Spell.AETHER_BOLT).forGetter(KnownSpells::selected)
    ).apply(instance, KnownSpells::new));

    public static final StreamCodec<ByteBuf, KnownSpells> STREAM_CODEC = StreamCodec.of(
            (buf, spells) -> {
                ByteBufCodecs.VAR_INT.encode(buf, spells.levels.size());
                for (Map.Entry<Spell, Integer> entry : spells.levels.entrySet()) {
                    Spell.STREAM_CODEC.encode(buf, entry.getKey());
                    ByteBufCodecs.VAR_INT.encode(buf, entry.getValue());
                }
                Spell.STREAM_CODEC.encode(buf, spells.selected);
            },
            buf -> {
                int count = Mth.clamp(ByteBufCodecs.VAR_INT.decode(buf), 0, Spell.values().length);
                Map<Spell, Integer> levels = new EnumMap<>(Spell.class);
                for (int i = 0; i < count; i++) {
                    Spell spell = Spell.STREAM_CODEC.decode(buf);
                    levels.put(spell, ByteBufCodecs.VAR_INT.decode(buf));
                }
                return new KnownSpells(levels, Spell.STREAM_CODEC.decode(buf));
            });

    public KnownSpells {
        Map<Spell, Integer> copy = new EnumMap<>(Spell.class);
        for (Map.Entry<Spell, Integer> entry : levels.entrySet()) {
            copy.put(entry.getKey(), Mth.clamp(entry.getValue(), 1, Spell.MAX_LEVEL));
        }
        copy.putIfAbsent(Spell.AETHER_BOLT, 1);
        levels = Collections.unmodifiableMap(copy);
        if (!levels.containsKey(selected)) {
            selected = Spell.AETHER_BOLT;
        }
    }

    /** Die Zauber eines Stabs (ohne eigene Angabe: nur der Aether-Blitz). */
    public static KnownSpells of(ItemStack staff) {
        return staff.getOrDefault(ModDataComponents.SPELLS.get(), DEFAULT);
    }

    /** Stufe des Zaubers (0 = unbekannt). */
    public int level(Spell spell) {
        return this.levels.getOrDefault(spell, 0);
    }

    public boolean knows(Spell spell) {
        return this.levels.containsKey(spell);
    }

    /** Stufe des gewählten Zaubers. */
    public int selectedLevel() {
        return this.level(this.selected);
    }

    /** Alle bekannten Zauber in fester Reihenfolge (Aether-Blitz zuerst, dann die Elemente). */
    public List<Spell> known() {
        List<Spell> list = new ArrayList<>(this.levels.keySet());
        Collections.sort(list);
        return list;
    }

    /** Derselbe Stab mit einem Zauber auf einer neuen Stufe. */
    public KnownSpells withLevel(Spell spell, int level) {
        Map<Spell, Integer> copy = new EnumMap<>(this.levels);
        copy.put(spell, level);
        return new KnownSpells(copy, this.selected);
    }

    /** Wählt einen bekannten Zauber (unbekannte werden nicht gewählt). */
    public KnownSpells select(Spell spell) {
        return this.knows(spell) ? new KnownSpells(this.levels, spell) : this;
    }
}
