package io.github.sirschrolli.kingdomsatwar.arena;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights.Fighter;
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
 * Die laufenden Kämpfe in den Kampfdimensionen der großen Bosse (Anhang an der Oberwelt, damit ein Kampf einen Neustart
 * übersteht): welcher Hort, in welcher Arena, wo dort das Abbild des Horts steht, wer mitgezogen wurde und wohin jeder
 * zurückkehrt.
 */
public final class BossFights {
    /** Ein Kampf. */
    public static final class Fight {
        static final Codec<Fight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BossArenaKind.CODEC.fieldOf("kind").forGetter(f -> f.kind),
                Level.RESOURCE_KEY_CODEC.fieldOf("arena").forGetter(f -> f.arena),
                Codec.INT.fieldOf("slot").forGetter(f -> f.slot),
                BlockPos.CODEC.fieldOf("center").forGetter(f -> f.center),
                BlockPos.CODEC.fieldOf("trigger").forGetter(f -> f.trigger),
                Level.RESOURCE_KEY_CODEC.fieldOf("lair_level").forGetter(f -> f.lairLevel),
                BlockPos.CODEC.fieldOf("lair").forGetter(f -> f.lair),
                Fighter.CODEC.listOf().fieldOf("fighters").forGetter(f -> List.copyOf(f.fighters)),
                Codec.LONG.optionalFieldOf("started_at", 0L).forGetter(f -> f.startedAt),
                Codec.BOOL.optionalFieldOf("won", false).forGetter(f -> f.won),
                Codec.LONG.optionalFieldOf("won_at", 0L).forGetter(f -> f.wonAt),
                Codec.INT.optionalFieldOf("bossless", 0).forGetter(f -> f.bossless)
        ).apply(instance, Fight::new));

        private final BossArenaKind kind;
        private final ResourceKey<Level> arena;
        private final int slot;
        private final BlockPos center;
        private final BlockPos trigger;
        private final ResourceKey<Level> lairLevel;
        private final BlockPos lair;
        private final List<Fighter> fighters = new ArrayList<>();
        private final long startedAt;
        private boolean won;
        private long wonAt;
        private int bossless;

        public Fight(BossArenaKind kind, ResourceKey<Level> arena, int slot, BlockPos center, BlockPos trigger, ResourceKey<Level> lairLevel,
                BlockPos lair, List<Fighter> fighters, long startedAt, boolean won, long wonAt, int bossless) {
            this.kind = kind;
            this.arena = arena;
            this.slot = slot;
            this.center = center.immutable();
            this.trigger = trigger.immutable();
            this.lairLevel = lairLevel;
            this.lair = lair.immutable();
            this.fighters.addAll(fighters);
            this.startedAt = startedAt;
            this.won = won;
            this.wonAt = wonAt;
            this.bossless = bossless;
        }

        public BossArenaKind kind() {
            return this.kind;
        }

        /** In welcher Welt die Arena liegt (die Kampfdimension; in Tests die Oberwelt). */
        public ResourceKey<Level> arenaLevel() {
            return this.arena;
        }

        public int slot() {
            return this.slot;
        }

        /** Die Mitte der Arena (das Abbild der Mitte des Horts). */
        public BlockPos center() {
            return this.center;
        }

        /** Wo das Abbild des Horts (seine Blockentität) in der Arena steht. */
        public BlockPos trigger() {
            return this.trigger;
        }

        public ResourceKey<Level> lairLevel() {
            return this.lairLevel;
        }

        /** Wo das Original (seine Blockentität) in der Welt steht. */
        public BlockPos lair() {
            return this.lair;
        }

        public List<Fighter> fighters() {
            return this.fighters;
        }

        public long startedAt() {
            return this.startedAt;
        }

        public boolean won() {
            return this.won;
        }

        public long wonAt() {
            return this.wonAt;
        }

        public void win(long gameTime) {
            if (!this.won) {
                this.won = true;
                this.wonAt = gameTime;
            }
        }

        /** Der Sieg zählt erst ab jetzt (der Boss vergeht noch). */
        void holdWin(long gameTime) {
            if (this.won) {
                this.wonAt = gameTime;
            }
        }

        /** Wie lange (in Prüfungen) der Boss schon fehlt. */
        int bossless() {
            return this.bossless;
        }

        void setBossless(int bossless) {
            this.bossless = bossless;
        }

        public @Nullable Fighter fighter(UUID id) {
            for (Fighter fighter : this.fighters) {
                if (fighter.id().equals(id)) {
                    return fighter;
                }
            }
            return null;
        }

        void add(Fighter fighter) {
            this.fighters.removeIf(f -> f.id().equals(fighter.id()));
            this.fighters.add(fighter);
        }
    }

    public static final Codec<BossFights> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Fight.CODEC.listOf().optionalFieldOf("fights", List.of()).forGetter(state -> List.copyOf(state.fights)),
            Fighter.CODEC.listOf().optionalFieldOf("stranded", List.of()).forGetter(state -> List.copyOf(state.stranded))
    ).apply(instance, BossFights::new));

    private final List<Fight> fights = new ArrayList<>();
    /** Spieler, deren Kampf endete, während sie abgemeldet waren (sie kehren beim Anmelden zurück). */
    private final List<Fighter> stranded = new ArrayList<>();

    public BossFights() {
        this(List.of(), List.of());
    }

    private BossFights(List<Fight> fights, List<Fighter> stranded) {
        this.fights.addAll(fights);
        this.stranded.addAll(stranded);
    }

    public static BossFights of(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.BOSS_FIGHTS);
    }

    public void save(MinecraftServer server) {
        server.overworld().setData(ModAttachments.BOSS_FIGHTS, this);
    }

    public List<Fight> all() {
        return List.copyOf(this.fights);
    }

    /** Der kleinste freie Platz in dieser Kampfdimension. */
    public int freeSlot(BossArenaKind kind) {
        int slot = 0;
        while (true) {
            int s = slot;
            if (this.fights.stream().noneMatch(f -> f.kind == kind && f.slot == s)) {
                return slot;
            }
            slot++;
        }
    }

    public void add(Fight fight) {
        this.fights.add(fight);
    }

    public void remove(Fight fight) {
        this.fights.remove(fight);
    }

    /** Der Kampf um diesen Hort (oder {@code null}). */
    public @Nullable Fight byLair(ResourceKey<Level> level, BlockPos lair) {
        for (Fight fight : this.fights) {
            if (fight.lairLevel == level && fight.lair.equals(lair)) {
                return fight;
            }
        }
        return null;
    }

    public @Nullable Fight byFighter(UUID id) {
        for (Fight fight : this.fights) {
            if (fight.fighter(id) != null) {
                return fight;
            }
        }
        return null;
    }

    /** Ein Spieler war abgemeldet, als sein Kampf endete. */
    public void strand(Fighter fighter) {
        this.stranded.removeIf(f -> f.id().equals(fighter.id()));
        this.stranded.add(fighter);
    }

    /** Wohin ein Spieler zurückkehrt, der in einer Kampfdimension zurückblieb (und aus der Liste damit). */
    public @Nullable Fighter takeStranded(UUID id) {
        for (Fighter fighter : this.stranded) {
            if (fighter.id().equals(id)) {
                this.stranded.remove(fighter);
                return fighter;
            }
        }
        return null;
    }
}
