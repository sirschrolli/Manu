package io.github.sirschrolli.kingdomsatwar.entity.combat;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.util.RandomSource;

/**
 * Der Kampfstil eines einzelnen Monsters: sein Temperament, die besonderen Angriffe, die gerade es beherrscht (eine
 * zufällige Auswahl aus denen seiner Familie), und sein eigenes Tempo. Wird beim ersten Erscheinen ausgewürfelt und mit
 * dem Monster gespeichert.
 *
 * @param tempo Faktor auf die Wartezeit zwischen den besonderen Angriffen (kleiner = öfter)
 */
public record Style(Temper temper, List<MoveKind> moves, float tempo) {
    public static final Style NONE = new Style(Temper.UNGESTUEM, List.of(), 1.0f);

    public static final Codec<Style> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Temper.CODEC.fieldOf("temper").forGetter(Style::temper),
            MoveKind.CODEC.listOf().fieldOf("moves").forGetter(Style::moves),
            Codec.FLOAT.fieldOf("tempo").forGetter(Style::tempo)
    ).apply(instance, Style::new));

    public Style {
        moves = List.copyOf(moves);
    }

    public boolean isNone() {
        return this == NONE || this.moves.isEmpty() && this.tempo == 1.0f && this.temper == Temper.UNGESTUEM;
    }

    /** Würfelt einen Kampfstil für ein Monster dieser Familie aus. */
    public static Style roll(RandomSource random, Family family, boolean ranged) {
        Temper temper = Temper.roll(random);
        List<MoveKind> pool = new ArrayList<>(family.pool(ranged));
        List<MoveKind> chosen = new ArrayList<>();
        int count = pool.isEmpty() ? 0 : Math.min(pool.size(), family.min() + random.nextInt(family.max() - family.min() + 1));
        for (int i = 0; i < count; i++) {
            chosen.add(pool.remove(random.nextInt(pool.size())));
        }
        // was das Temperament mitbringt (nur, wo es passt)
        switch (temper) {
            case LAUERND -> {
                if (family.melee() && !ranged && !family.brain() && !family.flying()) {
                    chosen.add(MoveKind.CIRCLE);
                }
            }
            case ZAEH -> {
                if (family.melee() && !ranged && !family.flying()) {
                    chosen.add(MoveKind.GUARD);
                }
            }
            case ANFUEHRER -> {
                if (!chosen.contains(MoveKind.ROAR)) {
                    chosen.add(MoveKind.ROAR);
                }
            }
            default -> {
            }
        }
        float tempo = 0.82f + random.nextFloat() * 0.4f;
        return new Style(temper, chosen, tempo);
    }
}
