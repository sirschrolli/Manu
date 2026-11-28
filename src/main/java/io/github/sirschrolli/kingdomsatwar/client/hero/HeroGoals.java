package io.github.sirschrolli.kingdomsatwar.client.hero;

import io.github.sirschrolli.kingdomsatwar.hero.HeroAbility;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Die nächsten Ziele auf dem Heldenweg (seit 1.12): neue Ränge, Fähigkeiten und Titel – damit der Held immer sieht, wofür
 * er kämpft. Das Charakterfenster zeigt das nächste Ziel mit seinem Fortschritt, die Heldenleiste meldet es kurz vorher,
 * und die Aufstiegs-Szene nennt es am Ende.
 */
public final class HeroGoals {
    /** Was auf ein Ziel wartet. */
    public enum Kind {
        RANK,
        ABILITY,
        TITLE;

        String key() {
            return this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** Ein Ziel: ab welcher Stufe, was es ist, sein Bild und seine Farbe. */
    public record Goal(Kind kind, int level, Component name, int color, Identifier icon, HeroLevel.@Nullable Rank rank) {
        /** „Rang Held“, „Fähigkeit Sturmlauf“, „Titel Lichtbringer“. */
        public Component label() {
            return Component.translatable("hero.kingdomsatwar.goal." + this.kind.key(), this.name);
        }

        /** Was er dort bekommt (eine Zeile, für Tooltips). */
        public Component reward() {
            if (this.rank == HeroLevel.Rank.LEGEND) {
                return Component.translatable("hero.kingdomsatwar.goal.reward.legend", HeroLevel.RANK_BONUS);
            }
            if (this.rank == HeroLevel.Rank.IMMORTAL) {
                return Component.translatable("hero.kingdomsatwar.goal.reward.immortal", HeroLevel.IMMORTAL_MINUTES);
            }
            return Component.translatable("hero.kingdomsatwar.goal.reward." + this.kind.key(), HeroLevel.RANK_BONUS);
        }
    }

    private static @Nullable List<Goal> all;

    private HeroGoals() {}

    /** Alle Ziele des Weges, nach Stufe geordnet. */
    public static List<Goal> all() {
        if (all == null) {
            List<Goal> goals = new ArrayList<>();
            for (HeroLevel.Rank rank : HeroLevel.Rank.values()) {
                if (rank.minLevel() > 1) {
                    goals.add(new Goal(Kind.RANK, rank.minLevel(), Component.translatable(rank.translationKey()), HeroGui.rankColor(rank),
                            HeroGui.badge(rank), rank));
                }
            }
            for (HeroAbility ability : HeroAbility.values()) {
                goals.add(new Goal(Kind.ABILITY, ability.level(), Component.translatable(ability.translationKey()), ability.color(), ability.icon(), null));
            }
            for (HeroLevel.Title title : HeroLevel.Title.values()) {
                goals.add(new Goal(Kind.TITLE, title.minLevel(), Component.translatable(title.translationKey()), title.color(), title.sprite(), null));
            }
            goals.sort(Comparator.comparingInt(Goal::level).thenComparing(Goal::kind));
            all = List.copyOf(goals);
        }
        return all;
    }

    /** Die nächsten {@code count} Ziele über dieser Stufe. */
    public static List<Goal> upcoming(int level, int count) {
        return all().stream().filter(goal -> goal.level() > level).limit(count).toList();
    }

    /** Das nächste Ziel über dieser Stufe (null am Ende des Weges). */
    public static @Nullable Goal next(int level) {
        for (Goal goal : all()) {
            if (goal.level() > level) {
                return goal;
            }
        }
        return null;
    }

    /** Die Stufe des letzten erreichten Ziels (1, wenn noch keins erreicht ist). */
    public static int previous(int level) {
        int previous = 1;
        for (Goal goal : all()) {
            if (goal.level() <= level) {
                previous = goal.level();
            }
        }
        return previous;
    }

    /** Wie weit der Weg vom letzten erreichten Ziel bis zu diesem schon gegangen ist (nach Heldenerfahrung, 0 bis 1). */
    public static float progress(int experience, Goal goal) {
        int level = HeroLevel.levelFor(experience);
        long from = HeroLevel.total(previous(level));
        long to = HeroLevel.total(goal.level());
        return to <= from ? 1.0f : Mth.clamp((float) ((experience - from) / (double) (to - from)), 0.0f, 1.0f);
    }
}
