package io.github.sirschrolli.kingdomsatwar.world.ambience;

import java.util.ArrayList;
import java.util.List;

/**
 * Die Klangwelt eines Bioms: welche Teppiche (nahtlose Schleifen) leise darunterliegen und welche Einzelklänge ab und zu an
 * einem zufälligen Ort in der Nähe erklingen. Die Namen verweisen auf
 * {@link io.github.sirschrolli.kingdomsatwar.registry.AmbientSounds}; abgespielt wird alles auf der Client-Seite
 * ({@code client.ambience.BiomeAmbience}).
 *
 * @param beds  Teppiche
 * @param spots Einzelklänge
 */
public record AmbienceProfile(List<Bed> beds, List<Spot> spots) {

    /** Wann ein Klang zu hören ist. */
    public enum Time {
        ALWAYS, DAY, NIGHT,
        /** in der Dämmerung und nachts */
        DUSK
    }

    /**
     * Ein Teppich.
     *
     * @param sound  Name (ambient.bed.*)
     * @param volume Lautstärke im Freien
     * @param time   Tageszeit
     * @param indoor Anteil, der in Häusern und Höhlen bleibt (1 = überall gleich laut, auch tief unter der Erde)
     * @param rain   Faktor bei vollem Regen (Vögel verstummen, Wind wird lauter)
     * @param high   Zuwachs hoch über dem Land (Wind auf Gipfeln)
     */
    public record Bed(String sound, float volume, Time time, float indoor, float rain, float high) {}

    /**
     * Ein Satz Einzelklänge.
     *
     * @param sound     Name (ambient.spot.*)
     * @param perMinute wie oft im Schnitt pro Minute (bei passender Tageszeit)
     * @param time      Tageszeit
     * @param near      kleinste Entfernung in Blöcken
     * @param far       größte Entfernung in Blöcken
     * @param low       tiefste Höhe relativ zu den Augen
     * @param high      größte Höhe relativ zu den Augen
     * @param outdoors  nur im Freien
     * @param volume    Lautstärke
     * @param pitch     Tonhöhe (± 6 %)
     */
    public record Spot(String sound, float perMinute, Time time, float near, float far, float low, float high, boolean outdoors,
            float volume, float pitch) {}

    public static Builder builder() {
        return new Builder();
    }

    /** Kurzschreibweise für die Profile. */
    public static final class Builder {
        private final List<Bed> beds = new ArrayList<>();
        private final List<Spot> spots = new ArrayList<>();

        private Builder() {}

        /** Wind: überall im Freien, in Höhlen nur ein Hauch, bei Regen und auf Gipfeln lauter. */
        public Builder wind(String sound, float volume) {
            this.beds.add(new Bed(sound, volume, Time.ALWAYS, 0.08f, 1.2f, 0.9f));
            return this;
        }

        /** Ein Teppich im Freien (Blätter, Wasser, Glut …), in Häusern leiser, bei Regen etwas gedämpft. */
        public Builder outside(String sound, float volume) {
            this.beds.add(new Bed(sound, volume, Time.ALWAYS, 0.0f, 0.85f, 0.0f));
            return this;
        }

        /** Tagsüber im Freien (Vögel, Insekten) – bei Regen fast still. */
        public Builder day(String sound, float volume) {
            this.beds.add(new Bed(sound, volume, Time.DAY, 0.0f, 0.15f, 0.0f));
            return this;
        }

        /** Nachts im Freien (Grillen, Frösche). */
        public Builder night(String sound, float volume) {
            this.beds.add(new Bed(sound, volume, Time.NIGHT, 0.0f, 0.3f, 0.0f));
            return this;
        }

        /** Ein Teppich, der auch drinnen bleibt ({@code indoor} = Anteil drinnen, 1 = auch tief unter der Erde). */
        public Builder everywhere(String sound, float volume, float indoor) {
            this.beds.add(new Bed(sound, volume, Time.ALWAYS, indoor, 1.0f, 0.0f));
            return this;
        }

        public Builder bed(Bed bed) {
            this.beds.add(bed);
            return this;
        }

        /** Ein Einzelklang im Freien, 10–30 Blöcke entfernt, etwas über Augenhöhe. */
        public Builder spot(String sound, float perMinute, Time time) {
            return this.spot(sound, perMinute, time, 10, 30, -2, 8, true, 1.0f, 1.0f);
        }

        /** Ein Einzelklang hoch in der Luft (Greifvögel). */
        public Builder sky(String sound, float perMinute, Time time) {
            return this.spot(sound, perMinute, time, 15, 40, 12, 32, true, 1.0f, 1.0f);
        }

        /** Ein Einzelklang ganz nah (Glühwürmchen, Blubbern, Flüstern) – auch unter Dach. */
        public Builder close(String sound, float perMinute, Time time) {
            return this.spot(sound, perMinute, time, 3, 9, -2, 3, false, 0.8f, 1.0f);
        }

        /** Ein Einzelklang weit weg (Donner, Drachen, Grollen) – auch drinnen zu hören. */
        public Builder far(String sound, float perMinute, Time time) {
            return this.spot(sound, perMinute, time, 30, 60, -6, 20, false, 1.0f, 1.0f);
        }

        public Builder spot(String sound, float perMinute, Time time, float near, float far, float low, float high, boolean outdoors,
                float volume, float pitch) {
            this.spots.add(new Spot(sound, perMinute, time, near, far, low, high, outdoors, volume, pitch));
            return this;
        }

        public AmbienceProfile build() {
            return new AmbienceProfile(List.copyOf(this.beds), List.copyOf(this.spots));
        }
    }
}
