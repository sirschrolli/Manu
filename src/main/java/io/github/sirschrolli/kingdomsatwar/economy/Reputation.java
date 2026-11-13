package io.github.sirschrolli.kingdomsatwar.economy;

import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

/**
 * Das Ansehen eines Spielers im eigenen Königreich: Es wächst mit Siegen über Drachen, Bauten, Bewohnern, Handel und
 * dem Schutz der Siedlung. Je höher das Ansehen, desto günstiger handeln die eigenen Bürger; für die Krönung braucht
 * es das höchste Ansehen.
 */
public final class Reputation {
    /** Stufen des Ansehens (ab wie vielen Punkten) und der Rabatt bei den eigenen Bürgern. */
    public enum Rank {
        STRANGER(0, 0),
        KNOWN(50, 5),
        RESPECTED(150, 10),
        HONORED(350, 15),
        CELEBRATED(650, 20),
        LEGENDARY(1000, 30);

        private final int points;
        private final int discount;

        Rank(int points, int discount) {
            this.points = points;
            this.discount = discount;
        }

        public int points() {
            return this.points;
        }

        /** Rabatt in Prozent. */
        public int discount() {
            return this.discount;
        }

        public String translationKey() {
            return "reputation.kingdomsatwar." + this.name().toLowerCase(Locale.ROOT);
        }

        public Rank next() {
            return this == LEGENDARY ? LEGENDARY : values()[this.ordinal() + 1];
        }

        public static Rank of(int points) {
            Rank rank = STRANGER;
            for (Rank r : values()) {
                if (points >= r.points) {
                    rank = r;
                }
            }
            return rank;
        }
    }

    /** Wofür es Ansehen gibt (Punkte). */
    public static final int HIRE = 5;
    public static final int BUILDING = 15;
    public static final int TRADE = 1;
    public static final int DEFEND = 2;
    public static final int DRAGON = 120;
    public static final int NYXAR = 250;

    private Reputation() {}

    public static int points(Player player) {
        return player.getData(ModAttachments.REPUTATION);
    }

    public static Rank rank(Player player) {
        return Rank.of(points(player));
    }

    /** Ansehen dazugewinnen (oder verlieren); beim Aufstieg in eine neue Stufe gibt es eine Meldung. */
    public static void add(ServerPlayer player, int amount) {
        int before = points(player);
        int after = Math.max(0, before + amount);
        player.setData(ModAttachments.REPUTATION, after);
        Rank old = Rank.of(before);
        Rank now = Rank.of(after);
        if (now.ordinal() > old.ordinal()) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.reputation.rank_up",
                    Component.translatable(now.translationKey()).withStyle(ChatFormatting.GOLD), now.discount()).withStyle(ChatFormatting.YELLOW));
            player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
        }
    }

    /** Preis nach Rabatt (mindestens 1 Bronze, wenn er vorher etwas kostete). */
    public static int discounted(int price, Rank rank) {
        if (price <= 0) {
            return price;
        }
        return Math.max(1, Math.round(price * (100 - rank.discount()) / 100.0f));
    }
}
