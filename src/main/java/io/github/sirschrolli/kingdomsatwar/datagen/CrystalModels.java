package io.github.sirschrolli.kingdomsatwar.datagen;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.data.models.model.ModelTemplate;
import net.minecraft.client.data.models.model.TextureSlot;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Modelle der Herz- und Manakristalle, wie sie in Höhlen wachsen.
 *
 * <ul>
 *   <li><b>Herzkristall:</b> Auf einem Brocken Tuff steht, ein wenig zurückgelehnt, ein geschliffenes Herz aus rotem
 *   Kristall – vorn und hinten das Bild des Herzens (helle Bögen, eine helle Tafel in der Mitte), der Kern steht ein
 *   wenig vor, die Kanten glänzen wie Schliffkanten. Um seinen Fuß wachsen vier spitze Splitter schräg aus dem Fels.</li>
 *   <li><b>Manakristall:</b> eine hohe Säule in der Mitte, um sie herum fünf kleinere, schräg nach außen gewachsen –
 *   jede um 45° gedreht (man sieht immer eine helle und eine dunkle Fläche) und oben mit einer Pyramidenspitze aus vier
 *   Dreiecken.</li>
 * </ul>
 *
 * Die Kristalle leuchten (Lichtstärke der Flächen 14). Die Vorderseite schaut nach Norden; gedreht wird über den
 * Blockzustand. Schräge Teile werden wie im Spiel gedreht: Euler-Winkel x, y, z um ihren Ursprung
 * (Drehung = Rz · Ry · Rx); zusammengesetzte Drehungen rechnet {@link #euler(double[])} in diese Winkel um.
 */
final class CrystalModels {
    static final TextureSlot CRYSTAL = TextureSlot.create("crystal");
    static final TextureSlot TIP = TextureSlot.create("tip");
    static final TextureSlot ROCK = TextureSlot.create("rock");
    private static final int GLOW = 14;

    /** Pixelzeilen des Herzbildes (Zeile r der Textur = Modellhöhe 15 − r bis 16 − r) mit ihren Spannen [von, bis): Höhe 2 bis 13. */
    private static final int[][][] HEART_ROWS = {
            {{3}, {4, 6, 10, 12}}, {{4}, {3, 7, 9, 13}}, {{5}, {2, 14}}, {{6}, {2, 14}}, {{7}, {2, 14}}, {{8}, {3, 13}},
            {{9}, {4, 12}}, {{10}, {5, 11}}, {{11}, {6, 10}}, {{12}, {7, 9}}, {{13}, {7, 9}}};
    /** Der erhabene Kern: das Herz, an den Seiten um einen Pixel schmaler. */
    private static final int[][][] CORE_ROWS = {
            {{4}, {4, 6, 10, 12}}, {{5}, {3, 13}}, {{6}, {3, 13}}, {{7}, {3, 13}}, {{8}, {4, 12}}, {{9}, {5, 11}},
            {{10}, {6, 10}}, {{11}, {7, 9}}};

    private CrystalModels() {}

    private static ExtendedModelTemplateBuilder builder() {
        return ExtendedModelTemplateBuilder.builder()
                .parent(Identifier.withDefaultNamespace("block/block"))
                .requiredTextureSlot(TextureSlot.PARTICLE)
                .requiredTextureSlot(CRYSTAL)
                .requiredTextureSlot(TIP)
                .requiredTextureSlot(ROCK);
    }

    // ------------------------------------------------------------------------------------------------ Herz

    /** Das Herz aus Kristall: Bild vorn und hinten, Schliffkanten, erhabener Kern, vier Splitter im Fels. */
    static ModelTemplate heart() {
        ExtendedModelTemplateBuilder b = builder();
        rock(b);
        double[] lean = rx(10);
        float[] pivot = {8, 2.5f, 8};
        heartLayer(b, HEART_ROWS, 6.5f, 9.5f, lean, pivot);
        heartLayer(b, CORE_ROWS, 6.0f, 10.0f, lean, pivot);
        float[][] shards = {{4.4f, 9.8f, 1.8f, 3.4f, 30}, {11.6f, 9.6f, 1.6f, 2.8f, 34}, {5.0f, 5.8f, 1.4f, 2.4f, 38}, {11.0f, 5.6f, 1.4f, 2.0f, 28}};
        for (float[] s : shards) {
            double azimuth = Math.toDegrees(Math.atan2(s[0] - 8, s[1] - 8));
            shard(b, new float[] {s[0], 1.5f, s[1]}, s[2], s[3], mul(ry(azimuth), rx(s[4])));
        }
        return b.build();
    }

    /** Eine Schicht des Herzens (z1 bis z2 tief): vorn und hinten das Bild, an den Kanten die Schliff-Streifen der Textur. */
    private static void heartLayer(ExtendedModelTemplateBuilder b, int[][][] rows, float z1, float z2, double[] lean, float[] pivot) {
        for (float[] slab : merged(rows)) {
            float y1 = slab[0];
            float y2 = slab[1];
            float x1 = slab[2];
            float x2 = slab[3];
            box(b, new float[] {x1, y1, z1}, new float[] {x2, y2, z2}, CRYSTAL, GLOW, lean, pivot, direction -> switch (direction) {
                case UP -> new float[] {x1, 0, x2, 1};
                case DOWN -> new float[] {x1, 15, x2, 16};
                case WEST -> new float[] {1, 16 - y2, 2, 16 - y1};
                case EAST -> new float[] {14, 16 - y2, 15, 16 - y1};
                default -> null;   // vorn und hinten: das Bild des Herzens (Projektion)
            });
        }
    }

    /** Gleiche Spannen in aufeinander folgenden Zeilen zu einem Block zusammengefasst: {y1, y2, x1, x2}. */
    private static List<float[]> merged(int[][][] rows) {
        List<float[]> slabs = new ArrayList<>();
        for (int[][] row : rows) {
            float y1 = 15 - row[0][0];
            float y2 = 16 - row[0][0];
            int[] spans = row[1];
            for (int i = 0; i < spans.length; i += 2) {
                float x1 = spans[i];
                float x2 = spans[i + 1];
                float[] below = null;
                for (float[] slab : slabs) {
                    if (slab[2] == x1 && slab[3] == x2 && slab[0] == y2) {
                        below = slab;
                        break;
                    }
                }
                if (below != null) {
                    below[0] = y1;
                } else {
                    slabs.add(new float[] {y1, y2, x1, x2});
                }
            }
        }
        return slabs;
    }

    /** Ein Splitter: Säule mit Pyramidenspitze, um 45° gedreht, Seiten abwechselnd hell (Spalte 0) und dunkel (Spalte 14). */
    private static void shard(ExtendedModelTemplateBuilder b, float[] base, float width, float height, double[] rotation) {
        float w = Math.min(width, 2);
        column(b, base, width, height, rotation, direction -> switch (direction) {
            case NORTH, SOUTH -> new float[] {0, 3, w, 3 + height};
            case EAST, WEST -> new float[] {14, 3, 14 + w, 3 + height};
            case UP -> new float[] {0, 1, w, 1 + w};
            case DOWN -> new float[] {14, 12, 14 + w, 12 + w};
        });
        tip(b, base, width, height, width * 1.1f, rotation);
    }

    // ------------------------------------------------------------------------------------------------ Mana

    /** Die Manakristalle: eine hohe Säule in der Mitte, fünf kleinere schräg nach außen, alle mit Pyramidenspitze. */
    static ModelTemplate mana() {
        ExtendedModelTemplateBuilder b = builder();
        rock(b);
        prism(b, new float[] {8, 2.25f, 8}, 3.2f, 8.5f, mul(ry(200), rx(6)));
        float[][] around = {{25, 5.4f, 2.0f, 30}, {100, 4.0f, 1.7f, 40}, {170, 4.8f, 1.9f, 27}, {240, 3.4f, 1.5f, 42}, {305, 4.4f, 1.8f, 34}};
        for (float[] c : around) {
            double a = Math.toRadians(c[0]);
            float[] base = {(float) (8 + Math.sin(a) * 2.7), 1.6f, (float) (8 + Math.cos(a) * 2.7)};
            prism(b, base, c[2], c[1], mul(ry(c[0]), rx(c[3])));
        }
        return b.build();
    }

    /**
     * Eine Kristallsäule aus dem Atlas der Manatextur: Spalten 0–3 helle Fläche, 4–7 dunkle, 8–11 Spitze, 12–15
     * mittlere – Nord hell, Ost dunkel, Süd mittel, West dunkel, oben am Kopf der Spalte die hellen Pixel.
     */
    private static void prism(ExtendedModelTemplateBuilder b, float[] base, float width, float height, double[] rotation) {
        float w = Math.min(width, 4);
        column(b, base, width, height, rotation, direction -> switch (direction) {
            case NORTH -> new float[] {0, 0, w, height};
            case EAST, WEST -> new float[] {4, 0, 4 + w, height};
            case SOUTH -> new float[] {12, 0, 12 + w, height};
            case UP -> new float[] {8, 0, 8 + w, w};
            case DOWN -> new float[] {4, 12, 4 + w, 12 + w};
        });
        tip(b, base, width, height, width * 0.95f, rotation);
    }

    // ------------------------------------------------------------------------------------------------ Bausteine

    /** Ein flacher Brocken Tuff, aus dem die Kristalle wachsen. */
    private static void rock(ExtendedModelTemplateBuilder b) {
        float[][] parts = {{3, 0, 4, 13, 1.5f, 12}, {4.5f, 0, 3, 11.5f, 1, 13}, {5, 1.5f, 5.5f, 11, 2.5f, 10.5f},
                {6.5f, 2.5f, 6.5f, 9.5f, 3.25f, 9.5f}, {10.5f, 1.5f, 9, 12.5f, 2.25f, 11.5f}, {3.5f, 1.5f, 5, 5.5f, 2, 7}};
        for (float[] p : parts) {
            box(b, new float[] {p[0], p[1], p[2]}, new float[] {p[3], p[4], p[5]}, ROCK, 0, null, null, direction -> null);
        }
    }

    /** Eine Säule (width breit, height hoch) über ihrem Fuß base, um 45° um sich selbst gedreht, dann nach rotation geneigt. */
    private static void column(ExtendedModelTemplateBuilder b, float[] base, float width, float height, double[] rotation, Uv uv) {
        float h = width / 2;
        box(b, new float[] {base[0] - h, base[1], base[2] - h}, new float[] {base[0] + h, base[1] + height, base[2] + h}, CRYSTAL, GLOW,
                mul(rotation, ry(45)), base, uv);
    }

    /**
     * Pyramidenspitze über einer Säule: vier Dreiecke (die Textur der Spitze ist ein Dreieck, der Rest durchsichtig), je um
     * ihre Grundkante nach innen geneigt, bis sie sich in der Spitze treffen.
     */
    private static void tip(ExtendedModelTemplateBuilder b, float[] base, float width, float height, float rise, double[] rotation) {
        float half = width / 2;
        float slant = (float) Math.hypot(rise, half);
        double lean = Math.toDegrees(Math.atan2(half, rise));
        for (int k = 0; k < 4; k++) {
            double[] turn = ry(45 + 90 * k);
            double[] edge = apply(mul(rotation, turn), 0, height, -half);   // Mitte der Grundkante
            float[] origin = {(float) (base[0] + edge[0]), (float) (base[1] + edge[1]), (float) (base[2] + edge[2])};
            double[] r = mul(mul(rotation, turn), rx(lean));
            float[] angles = euler(r);
            b.element(e -> {
                e.from(origin[0] - half, origin[1], origin[2]).to(origin[0] + half, origin[1] + slant, origin[2]);
                e.face(Direction.NORTH, face -> face.texture(TIP).uvs(0, 0, 16, 16));
                e.lightEmission(GLOW);
                e.rotation(rot -> rot.origin(origin[0], origin[1], origin[2]).eulerXYZ(angles[0], angles[1], angles[2]));
            });
        }
    }

    /** Texturkoordinaten einer Seite (null: nach der Lage im Block). */
    @FunctionalInterface
    private interface Uv {
        float[] of(Direction direction);
    }

    /** Ein Quader; gedreht um origin, wenn rotation nicht null ist. */
    private static void box(ExtendedModelTemplateBuilder b, float[] from, float[] to, TextureSlot slot, int light, double[] rotation, float[] origin,
            Uv uv) {
        b.element(e -> {
            e.from(from[0], from[1], from[2]).to(to[0], to[1], to[2]);
            for (Direction direction : Direction.values()) {
                float[] coords = uv.of(direction);
                e.face(direction, face -> {
                    face.texture(slot);
                    if (coords != null) {
                        face.uvs(coords[0], coords[1], coords[2], coords[3]);
                    }
                });
            }
            if (light > 0) {
                e.lightEmission(light);
            }
            if (rotation != null) {
                float[] angles = euler(rotation);
                if (Math.abs(angles[0]) > 1.0e-3 || Math.abs(angles[1]) > 1.0e-3 || Math.abs(angles[2]) > 1.0e-3) {
                    e.rotation(rot -> rot.origin(origin[0], origin[1], origin[2]).eulerXYZ(angles[0], angles[1], angles[2]));
                }
            }
        });
    }

    // ------------------------------------------------------------------------------------------------ Drehungen

    /** Drehung um die X-Achse (Zeilen zuerst): +Y kippt nach +Z. */
    private static double[] rx(double degrees) {
        double a = Math.toRadians(degrees);
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[] {1, 0, 0, 0, c, -s, 0, s, c};
    }

    /** Drehung um die Y-Achse (Zeilen zuerst): +Z dreht nach +X. */
    private static double[] ry(double degrees) {
        double a = Math.toRadians(degrees);
        double c = Math.cos(a);
        double s = Math.sin(a);
        return new double[] {c, 0, s, 0, 1, 0, -s, 0, c};
    }

    private static double[] mul(double[] a, double[] b) {
        double[] m = new double[9];
        for (int i = 0; i < 3; i++) {
            for (int j = 0; j < 3; j++) {
                m[i * 3 + j] = a[i * 3] * b[j] + a[i * 3 + 1] * b[3 + j] + a[i * 3 + 2] * b[6 + j];
            }
        }
        return m;
    }

    private static double[] apply(double[] m, double x, double y, double z) {
        return new double[] {m[0] * x + m[1] * y + m[2] * z, m[3] * x + m[4] * y + m[5] * z, m[6] * x + m[7] * y + m[8] * z};
    }

    /** Die Euler-Winkel (Grad) x, y, z mit Drehung = Rz(z) · Ry(y) · Rx(x) – so dreht das Spiel ein Element. */
    static float[] euler(double[] m) {
        double y = Math.asin(Math.max(-1.0, Math.min(1.0, -m[6])));
        double x = Math.atan2(m[7], m[8]);
        double z = Math.atan2(m[3], m[0]);
        return new float[] {(float) Math.toDegrees(x), (float) Math.toDegrees(y), (float) Math.toDegrees(z)};
    }
}
