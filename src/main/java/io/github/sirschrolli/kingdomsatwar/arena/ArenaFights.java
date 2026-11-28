package io.github.sirschrolli.kingdomsatwar.arena;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die laufenden Kämpfe in der Arkanen Sphäre (Anhang an der Oberwelt, damit ein Kampf einen Neustart übersteht): welcher
 * Erzmagier, aus welchem Turm, in welcher Arena, wer mitgezogen wurde und wohin jeder zurückkehrt.
 */
public final class ArenaFights {
    /** Ein Kämpfer (Spieler oder Soldat) und wo er herkam. */
    public record Fighter(UUID id, boolean player, ResourceKey<Level> level, Vec3 pos, float yaw) {
        static final Codec<Fighter> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(Fighter::id),
                Codec.BOOL.fieldOf("player").forGetter(Fighter::player),
                Level.RESOURCE_KEY_CODEC.fieldOf("level").forGetter(Fighter::level),
                Vec3.CODEC.fieldOf("pos").forGetter(Fighter::pos),
                Codec.FLOAT.optionalFieldOf("yaw", 0.0f).forGetter(Fighter::yaw)
        ).apply(instance, Fighter::new));
    }

    /** Ein Kampf. */
    public static final class Fight {
        static final Codec<Fight> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("slot").forGetter(f -> f.slot),
                BlockPos.CODEC.fieldOf("center").forGetter(f -> f.center),
                DragonType.CODEC.fieldOf("element").forGetter(f -> f.element),
                UUIDUtil.CODEC.fieldOf("mage").forGetter(f -> f.mage),
                Level.RESOURCE_KEY_CODEC.fieldOf("tower_level").forGetter(f -> f.towerLevel),
                BlockPos.CODEC.fieldOf("tower_home").forGetter(f -> f.towerHome),
                Fighter.CODEC.listOf().fieldOf("fighters").forGetter(f -> List.copyOf(f.fighters)),
                Codec.LONG.optionalFieldOf("started_at", 0L).forGetter(f -> f.startedAt),
                Codec.BOOL.optionalFieldOf("won", false).forGetter(f -> f.won),
                Codec.LONG.optionalFieldOf("won_at", 0L).forGetter(f -> f.wonAt)
        ).apply(instance, Fight::new));

        private final int slot;
        private final BlockPos center;
        private final DragonType element;
        private UUID mage;
        private final ResourceKey<Level> towerLevel;
        private final BlockPos towerHome;
        private final List<Fighter> fighters = new ArrayList<>();
        private final long startedAt;
        private boolean won;
        private long wonAt;

        public Fight(int slot, BlockPos center, DragonType element, UUID mage, ResourceKey<Level> towerLevel, BlockPos towerHome,
                List<Fighter> fighters, long startedAt, boolean won, long wonAt) {
            this.slot = slot;
            this.center = center.immutable();
            this.element = element;
            this.mage = mage;
            this.towerLevel = towerLevel;
            this.towerHome = towerHome.immutable();
            this.fighters.addAll(fighters);
            this.startedAt = startedAt;
            this.won = won;
            this.wonAt = wonAt;
        }

        public int slot() {
            return this.slot;
        }

        public DragonType element() {
            return this.element;
        }

        public UUID mage() {
            return this.mage;
        }

        void setMage(UUID mage) {
            this.mage = mage;
        }

        public ResourceKey<Level> towerLevel() {
            return this.towerLevel;
        }

        /** Wo der Magier im Turm meditiert (dorthin kehrt er zurück, dort landet die Beute). */
        public BlockPos towerHome() {
            return this.towerHome;
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

        /** Die Mitte der Arena (Boden). */
        public BlockPos center() {
            return this.center;
        }

        public @Nullable Fighter fighter(UUID id) {
            for (Fighter fighter : this.fighters) {
                if (fighter.id().equals(id)) {
                    return fighter;
                }
            }
            return null;
        }
    }

    public static final Codec<ArenaFights> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Fight.CODEC.listOf().optionalFieldOf("fights", List.of()).forGetter(state -> List.copyOf(state.fights)),
            Fighter.CODEC.listOf().optionalFieldOf("stranded", List.of()).forGetter(state -> List.copyOf(state.stranded))
    ).apply(instance, ArenaFights::new));

    private final List<Fight> fights = new ArrayList<>();
    /** Spieler, deren Kampf endete, während sie abgemeldet waren (sie kehren beim Anmelden zurück). */
    private final List<Fighter> stranded = new ArrayList<>();

    public ArenaFights() {
        this(List.of(), List.of());
    }

    private ArenaFights(List<Fight> fights, List<Fighter> stranded) {
        this.fights.addAll(fights);
        this.stranded.addAll(stranded);
    }

    /** Ein Spieler war abgemeldet, als sein Kampf endete. */
    public void strand(Fighter fighter) {
        this.stranded.removeIf(f -> f.id().equals(fighter.id()));
        this.stranded.add(fighter);
    }

    /** Wohin ein Spieler zurückkehrt, der in der Sphäre zurückblieb (und aus der Liste damit). */
    public @Nullable Fighter takeStranded(UUID id) {
        for (Fighter fighter : this.stranded) {
            if (fighter.id().equals(id)) {
                this.stranded.remove(fighter);
                return fighter;
            }
        }
        return null;
    }

    public static ArenaFights of(MinecraftServer server) {
        return server.overworld().getData(ModAttachments.ARENA_FIGHTS);
    }

    public void save(MinecraftServer server) {
        server.overworld().setData(ModAttachments.ARENA_FIGHTS, this);
    }

    /** Die Mitte (Boden) der Arena in diesem Platz. */
    public static BlockPos arenaCenter(int slot) {
        return new BlockPos(slot * ArcaneSphere.SPACING, ArcaneSphere.FLOOR_Y, 0);
    }

    public List<Fight> all() {
        return List.copyOf(this.fights);
    }

    /** Der kleinste freie Platz. */
    public int freeSlot() {
        int slot = 0;
        while (true) {
            int s = slot;
            if (this.fights.stream().noneMatch(f -> f.slot == s)) {
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

    public @Nullable Fight byMage(UUID mage) {
        for (Fight fight : this.fights) {
            if (fight.mage.equals(mage)) {
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

    /** Läuft gerade ein Kampf mit dem Magier aus diesem Turm? */
    public boolean busy(BlockPos towerHome) {
        return this.fights.stream().anyMatch(f -> f.towerHome.equals(towerHome));
    }
}
