package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Was der Magietisch mit Stab, Tafel und Aetherit tun würde – und was es kostet. Dieselbe Rechnung läuft im Fenster
 * (Anzeige) und auf dem Server (Ausführung).
 *
 * <ul>
 *   <li>Mit einer Zaubertafel (oder einer alten Rune): der Stab lernt den Zauber auf Stufe I bzw. steigt eine Stufe auf.
 *       Kosten: 3/6/9 Erfahrungsstufen und 1/2/3 Aetherit für Stufe I/II/III; die Tafel wird verbraucht.</li>
 *   <li>Ohne Tafel: der Aether-Blitz wird mit Aetherit gestärkt (Stufe II: 4 Erfahrungsstufen und 3 Aetherit,
 *       Stufe III: 8 und 6).</li>
 * </ul>
 */
public record Inscription(@Nullable Spell spell, int fromLevel, int toLevel, int experience, int aetherit, Status status) {
    /** Ergebnis der Prüfung (in dieser Reihenfolge geprüft). */
    public enum Status {
        /** Kein Stab im Tisch. */
        NO_STAFF,
        /** Der Zauber hat schon die höchste Stufe. */
        MAX_LEVEL,
        /** Zu wenig Aetherit im rechten Fach. */
        NEED_AETHERIT,
        /** Zu wenige Erfahrungsstufen. */
        NEED_EXPERIENCE,
        /** Alles bereit. */
        READY;

        public String key() {
            return "gui.kingdomsatwar.magic_table.status." + this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private static final int[] TABLET_EXPERIENCE = {3, 6, 9};
    private static final int[] TABLET_AETHERIT = {1, 2, 3};
    private static final int[] AETHER_EXPERIENCE = {0, 4, 8};
    private static final int[] AETHER_AETHERIT = {0, 3, 6};

    /** Welchen Zauber lehrt dieser Gegenstand? (Zaubertafel oder alte Rune, sonst {@code null}) */
    public static @Nullable Spell taughtBy(ItemStack stack) {
        if (stack.getItem() instanceof SpellTabletItem tablet) {
            return tablet.spell();
        }
        if (stack.getItem() instanceof RuneItem rune) {
            return rune.spell();
        }
        return null;
    }

    public static boolean isCatalyst(ItemStack stack) {
        return stack.is(ModTags.Items.GEMS_AETHERIT);
    }

    /** Plant das Einschreiben für diese Gegenstände (Spieler: Erfahrung und Kreativmodus). */
    public static Inscription plan(ItemStack staff, ItemStack tablet, ItemStack catalyst, @Nullable Player player) {
        if (!(staff.getItem() instanceof AetheritStaffItem)) {
            return new Inscription(null, 0, 0, 0, 0, Status.NO_STAFF);
        }
        KnownSpells spells = KnownSpells.of(staff);
        Spell taught = taughtBy(tablet);
        Spell spell = taught != null ? taught : Spell.AETHER_BOLT;
        int from = spells.level(spell);
        if (from >= Spell.MAX_LEVEL) {
            return new Inscription(spell, from, from, 0, 0, Status.MAX_LEVEL);
        }
        int to = from + 1;
        int experience = taught != null ? TABLET_EXPERIENCE[to - 1] : AETHER_EXPERIENCE[to - 1];
        int aetherit = taught != null ? TABLET_AETHERIT[to - 1] : AETHER_AETHERIT[to - 1];
        boolean free = player != null && player.hasInfiniteMaterials();
        Status status;
        if (!free && (!isCatalyst(catalyst) || catalyst.getCount() < aetherit)) {
            status = Status.NEED_AETHERIT;
        } else if (!free && (player == null || player.experienceLevel < experience)) {
            status = Status.NEED_EXPERIENCE;
        } else {
            status = Status.READY;
        }
        return new Inscription(spell, from, to, experience, aetherit, status);
    }

    /** Lehrt eine Tafel (statt den Aether-Blitz zu stärken)? */
    public boolean fromTablet(ItemStack tablet) {
        return taughtBy(tablet) != null;
    }

    public boolean ready() {
        return this.status == Status.READY;
    }

    /**
     * Schreibt den Zauber ein: der Stab lernt bzw. steigt auf, Tafel, Aetherit und Erfahrung werden verbraucht
     * (im Kreativmodus nur die Tafel). @return ob es geklappt hat
     */
    public static boolean perform(ItemStack staff, ItemStack tablet, ItemStack catalyst, Player player) {
        Inscription plan = plan(staff, tablet, catalyst, player);
        if (!plan.ready() || plan.spell() == null) {
            return false;
        }
        AetheritStaffItem.setSpells(staff, KnownSpells.of(staff).withLevel(plan.spell(), plan.toLevel()).select(plan.spell()));
        if (plan.fromTablet(tablet)) {
            tablet.shrink(1);
        }
        if (!player.hasInfiniteMaterials()) {
            catalyst.shrink(plan.aetherit());
            player.giveExperienceLevels(-plan.experience());
        }
        return true;
    }
}
