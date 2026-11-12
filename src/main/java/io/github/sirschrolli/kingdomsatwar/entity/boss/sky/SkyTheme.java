package io.github.sirschrolli.kingdomsatwar.entity.boss.sky;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import java.util.function.Supplier;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;

/**
 * Das Gesicht der Himmelswacht eines Bosses: Farbe von Siegel und Bannpfeil, die Funken, die er zieht, wie stark er ist
 * (Stufe 1 bis 3) – und was der Treffer außer dem Flugbann noch anrichtet (siehe {@link SkyBoltEntity}).
 */
public enum SkyTheme {
    /** Pyrrhon und der Feuermagier: der Getroffene brennt. */
    FIRE(1, 0xFF7A2A, 0xFFE08A, () -> ModParticles.BOSS_FLAME.get()),
    /** Glaciara und der Eismagier: der Getroffene erstarrt. */
    ICE(1, 0x9FE8FF, 0xF2FCFF, () -> ModParticles.BOSS_FROST.get()),
    /** Voltarion und der Blitzmagier: der Getroffene ist geschwächt und leuchtet. */
    LIGHTNING(1, 0xFFE94A, 0xFFFFFF, () -> ModParticles.BOSS_SPARK.get()),
    /** Verdantis und der Naturmagier: der Getroffene ist vergiftet. */
    NATURE(1, 0x7FE05A, 0xE4FFB0, () -> ModParticles.BOSS_SPORE.get()),
    /** Terragon und der Erdmagier: der Getroffene wird schwer wie Stein. */
    EARTH(1, 0xD0A468, 0xFFE8C0, () -> ModParticles.BOSS_DUST.get()),
    /** Thalassa und der Wassermagier: der Getroffene ist durchnässt und träge. */
    WATER(1, 0x4FD0F0, 0xD8F8FF, () -> ModParticles.BOSS_WATER.get()),
    /** Nyxar: dem Getroffenen wird schwarz vor Augen. */
    SHADOW(1, 0x9A6CFF, 0xE0D0FF, () -> ModParticles.BOSS_SHADOW.get()),
    /** Karkinos, der Riffkoloss: der Getroffene hängt in einer Blase aus Brackwasser. */
    REEF(1, 0x3FE0C0, 0xD0FFF0, () -> ModParticles.SEA_GLOW.get()),
    /** Noctua, die Mottenkönigin: der Getroffene klebt in Seide. */
    MOTH(1, 0xE6C8FF, 0xFFFFFF, () -> ModParticles.FAE_SPARK.get()),
    /** Der Aschenkönig: der Getroffene verdorrt. */
    ASH(1, 0x6FD8E8, 0xD8FFFF, () -> ParticleTypes.SOUL_FIRE_FLAME),
    /** MissMystik: ein Knall, Rauch – der Getroffene sieht nichts. */
    MYSTIK(2, 0xFF5AB4, 0xFFE0F2, () -> ModParticles.CONFETTI.get()),
    /** Malleus, der Glutschmied: der Getroffene brennt und wird zum Amboss gerissen. */
    FORGE(2, 0xFF8A2A, 0xFFD890, () -> ModParticles.BOSS_FLAME.get()),
    /** Asteria und der Sternenfresser: der Getroffene stürzt auf sie zu. */
    STAR(2, 0xBFD0FF, 0xFFFFFF, () -> ParticleTypes.END_ROD),
    /** Anni der Troll: „Fliegen ist auf diesem Server nicht erlaubt“ – der Getroffene steht wieder am Boden. */
    GLITCH(2, 0x20E8FF, 0xF800F8, () -> ModParticles.GLITCH_PIXEL.get()),
    /** Der Zerbrochene König: der Getroffene verdorrt in der Leere. */
    VOID(2, 0xB45AFF, 0xF0D8FF, () -> ParticleTypes.REVERSE_PORTAL),
    /** Der Weltenfresser: der Getroffene wird in den Schlund gezogen. */
    MAW(3, 0xFF3A5A, 0xFFC0C8, () -> ModParticles.BOSS_SHADOW.get()),
    /** Schrolli: Fesseln aus Licht – der Getroffene ist geschwächt und leuchtet. */
    CREATOR(3, 0xFFD24A, 0xFFF8DC, () -> ModParticles.DIVINE_SPARK.get()),
    /** Der Steinhüter (0.96): Mondstein umschließt den Getroffenen – er ist schwer und langsam. */
    KEEPER(1, 0x9ED0FF, 0xF2FAFF, () -> ModParticles.FAE_SPARK.get()),
    /** Der Stundenwächter (1.1): die Zeit stockt um den Getroffenen – langsam in Schritt und Schlag, und sie zieht ihn zurück. */
    TIME(2, 0xFFB347, 0xFFE7A0, () -> ParticleTypes.WAX_ON);

    private static final SkyTheme[] VALUES = values();

    /** 1: Drachen, Magier und die frühen Bosse · 2: die großen Bosse · 3: die letzten beiden. */
    public final int tier;
    /** Grundfarbe von Siegel, Ziellinie und Pfeil (RGB). */
    public final int color;
    /** Helle Zweitfarbe (Kern des Pfeils, Runen des Siegels). */
    public final int accent;
    private final Supplier<ParticleOptions> particle;

    SkyTheme(int tier, int color, int accent, Supplier<ParticleOptions> particle) {
        this.tier = tier;
        this.color = color;
        this.accent = accent;
        this.particle = particle;
    }

    public ParticleOptions particle() {
        return this.particle.get();
    }

    public static SkyTheme byIndex(int index) {
        return VALUES[Mth.clamp(index, 0, VALUES.length - 1)];
    }

    /** Das Thema der Drachen und Magier eines Elements. */
    public static SkyTheme of(DragonType element) {
        return switch (element) {
            case FIRE -> FIRE;
            case ICE -> ICE;
            case LIGHTNING -> LIGHTNING;
            case NATURE -> NATURE;
            case EARTH -> EARTH;
            case WATER -> WATER;
        };
    }

    /** Schaden des Bannpfeils (vor Rüstung und Härte). */
    public float damage() {
        return 6.0f + this.tier * 2.0f;
    }

    /** Dauer des Flugbanns in Ticks (vor dem Abzug guter Schwingen). */
    public int banTicks() {
        return 120 + this.tier * 40;
    }

    /** Höchstgeschwindigkeit des Bannpfeils in Blöcken je Tick. */
    public double speed() {
        return 1.7 + this.tier * 0.2;
    }

    /** Wie scharf der Bannpfeil wenden kann (Bogenmaß je Tick). */
    public double turn() {
        return (4.5 + this.tier) * Mth.DEG_TO_RAD;
    }
}
