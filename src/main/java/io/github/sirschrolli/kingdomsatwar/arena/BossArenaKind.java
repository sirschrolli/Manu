package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.level.dimension.LevelStem;
import org.jspecify.annotations.Nullable;

/**
 * Die Kampfdimensionen der großen Bosse (wie die Arkane Sphäre der Erzmagier): Betritt jemand den Hort, zieht der Boss ihn,
 * seine Mitstreiter und ihre Soldaten in seine eigene Welt – dort schwebt ein Abbild seines Horts unter einem fremden
 * Himmel, und dort wird gekämpft.
 *
 * <ul>
 *     <li>{@link #MYSTIK} – die Mondbühne: die Zauberbühne als Feeninsel unter dem Vollmond, ein rosa Wolkenmeer tief unten;</li>
 *     <li>{@link #MALLEUS} – die Glutesse: der Glutdom als offener Krater unter einem brennenden Himmel, Asche rieselt;</li>
 *     <li>{@link #ASTERIA} – das Sternenzelt: die Sternwarte auf ihrer Endinsel, ringsum nur Sterne und ein leuchtendes Wolkenmeer;</li>
 *     <li>{@link #ANNI} – der Fehlerraum: die Fehlerzone als herausgerissener Chunk in einem Raum aus Bildrauschen;</li>
 *     <li>{@link #KING} – die Thronwelt: der Thron der Alten unter einem Himmel voller kalter Sterne;</li>
 *     <li>{@link #WORLD_EATER} – der Weltenschlund: der Schlund der Welten unter flammenden Rissen;</li>
 *     <li>{@link #CREATOR} – die Schöpfung: der Hof Schrollis des Schöpfers, die größte von allen – ein Kreis aus Gold und
 *     Licht über einem Meer aus goldenen Wolken, vier Welten ringsum, darüber die Räder des Himmels (eigener Himmel).</li>
 * </ul>
 */
public enum BossArenaKind implements net.minecraft.util.StringRepresentable {
    MYSTIK("moon_stage", 34, 30, 22.0, 12.0, 9, 15),
    MALLEUS("ember_forge", 40, 60, 20.0, 12.0, 3, 7),
    ASTERIA("star_canopy", 40, 34, 22.0, 14.0, 4, 9),
    ANNI("error_space", 36, 28, 22.0, 14.0, 5, 10),
    KING("throne_realm", 48, 40, 26.0, 16.0, 6, 13),
    WORLD_EATER("world_maw", 92, 48, 34.0, 18.0, 15, 24),
    CREATOR("creation", 132, 70, CreatorArena.CALL_RADIUS, 10.0, 16, 30);

    /** Abstand der Arenen voneinander (jeder Kampf bekommt seine eigene). */
    public static final int SPACING = 4096;
    public static final com.mojang.serialization.Codec<BossArenaKind> CODEC =
            net.minecraft.util.StringRepresentable.fromEnum(BossArenaKind::values);

    private final String id;
    private final ResourceKey<Level> level;
    private final ResourceKey<DimensionType> type;
    private final ResourceKey<LevelStem> stem;
    private final ResourceKey<Biome> biome;
    /** So weit (waagerecht) reicht alles, was zur Arena gehört, um ihre Mitte. */
    private final int reach;
    /** Wer tiefer fällt als so viele Blöcke unter die Mitte, wird zurückgeworfen. */
    private final int fallDepth;
    /** So nah (waagerecht) an der Mitte des Horts müssen Spieler sein, um mitgezogen zu werden … */
    private final double pullRadius;
    /** … und höchstens so weit darüber oder darunter. */
    private final double pullHeight;
    /** Zwischen diesen Abständen von der Mitte erscheinen die Kämpfer. */
    private final int spawnMin;
    private final int spawnMax;

    BossArenaKind(String id, int reach, int fallDepth, double pullRadius, double pullHeight, int spawnMin, int spawnMax) {
        this.id = id;
        this.level = ResourceKey.create(Registries.DIMENSION, KingdomsAtWar.id(id));
        this.type = ResourceKey.create(Registries.DIMENSION_TYPE, KingdomsAtWar.id(id));
        this.stem = ResourceKey.create(Registries.LEVEL_STEM, KingdomsAtWar.id(id));
        this.biome = ResourceKey.create(Registries.BIOME, KingdomsAtWar.id(id));
        this.reach = reach;
        this.fallDepth = fallDepth;
        this.pullRadius = pullRadius;
        this.pullHeight = pullHeight;
        this.spawnMin = spawnMin;
        this.spawnMax = spawnMax;
    }

    public String id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public ResourceKey<Level> level() {
        return this.level;
    }

    public ResourceKey<DimensionType> type() {
        return this.type;
    }

    public ResourceKey<LevelStem> stem() {
        return this.stem;
    }

    public ResourceKey<Biome> biome() {
        return this.biome;
    }

    public int reach() {
        return this.reach;
    }

    public int fallDepth() {
        return this.fallDepth;
    }

    public double pullRadius() {
        return this.pullRadius;
    }

    public double pullHeight() {
        return this.pullHeight;
    }

    public int spawnMin() {
        return this.spawnMin;
    }

    public int spawnMax() {
        return this.spawnMax;
    }

    /** Die Kampfdimension zu dieser Welt (oder {@code null}, wenn sie keine ist). */
    public static @Nullable BossArenaKind of(ResourceKey<Level> dimension) {
        for (BossArenaKind kind : values()) {
            if (kind.level == dimension) {
                return kind;
            }
        }
        return null;
    }

    /** Ist diese Welt eine Kampfdimension eines Bosses? */
    public static boolean isArena(ResourceKey<Level> dimension) {
        return of(dimension) != null;
    }

    public static boolean isArena(Level level) {
        return isArena(level.dimension());
    }

    /**
     * Gilt diese Welt als {@code home} – sie selbst oder die Kampfdimension, die ihr nachgebildet ist (für Waffen, die in
     * ihrer Heimat stärker sind: die Sternenlanze im End und im Sternenzelt, der Glutspeer im Nether und in der Glutesse …).
     */
    public static boolean counts(Level level, ResourceKey<Level> home) {
        ResourceKey<Level> dimension = level.dimension();
        if (dimension == home) {
            return true;
        }
        BossArenaKind kind = of(dimension);
        return kind != null && kind.home() == home;
    }

    /** Die Welt, deren Hort diese Kampfdimension nachbildet. */
    public ResourceKey<Level> home() {
        return switch (this) {
            case MYSTIK -> io.github.sirschrolli.kingdomsatwar.fae.FaeGate.FAE_REALM;
            case MALLEUS -> Level.NETHER;
            case ASTERIA -> Level.END;
            case ANNI -> Level.OVERWORLD;
            case KING -> io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate.THE_VOID;
            case WORLD_EATER -> io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate.THE_ABYSS;
            case CREATOR -> Level.OVERWORLD;
        };
    }

    /**
     * In welcher Höhe die Mitte der Arena liegt: bei den Abbildern auf der Höhe des Horts, die Schöpfung (neu gebaut, frei
     * schwebend) immer auf derselben Höhe.
     */
    public int arenaY(int focusY) {
        return this == CREATOR ? CreatorArena.FLOOR_Y + 1 : focusY;
    }

    /** Die Mitte der Arena in diesem Platz – waagerecht; die Höhe ist die des Horts ({@code y}). */
    public static net.minecraft.core.BlockPos center(int slot, int y) {
        return new net.minecraft.core.BlockPos(slot * SPACING, y, 0);
    }
}
