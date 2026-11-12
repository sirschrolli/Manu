package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.urmeer.TideGate;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerBiomes;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerDimension;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerTerrain;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.IntPredicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Nur zur Prüfung des Urmeers (seit 0.89): Bilder aus jedem Biom – über dem Wasser (die Sturminseln, das offene Meer) und
 * darunter (Korallenbänke, Tangwälder der Weiten, ein Tiefseegraben) –, eine Karte von oben und ein Gezeitentor auf seiner
 * Plattform.
 */
final class UrmeerShowcase {
    private UrmeerShowcase() {}

    /**
     * Eine Ansicht: wo (Biom und eine Bedingung an die Höhe des Grunds), ob unter Wasser, wie weit weg und wie hoch über
     * dem Grund die Kamera steht, aus welcher Richtung.
     */
    private record View(String name, ResourceKey<Biome> biome, IntPredicate ground, boolean under, double distance, double height, float turn,
            boolean dark) {
        View(String name, ResourceKey<Biome> biome, IntPredicate ground, boolean under, double distance, double height, float turn) {
            this(name, biome, ground, under, distance, height, turn, false);
        }

        /** Blick zum Horizont (die Kamera steht am Ort selbst, schaut flach in Richtung {@code turn}). */
        boolean horizon() {
            return this.distance <= 0;
        }
    }

    private static final int SEA = UrmeerTerrain.SEA_LEVEL;

    private static final List<View> VIEWS = List.of(
            new View("urmeer_isles", UrmeerBiomes.ISLES, h -> h > SEA + 10, false, 64, 10, 20),
            new View("urmeer_beach", UrmeerBiomes.ISLES, h -> h > SEA && h < SEA + 4, false, 14, 3, 200),
            new View("urmeer_sea", UrmeerBiomes.DEEP, h -> h < SEA - 30, false, 0, 5, 100),
            new View("urmeer_reef", UrmeerBiomes.REEF, h -> h > SEA - 9 && h < SEA - 3, true, 9, 3, 60),
            new View("urmeer_kelp", UrmeerBiomes.DEEP, h -> h > UrmeerTerrain.FLOOR - 4 && h < UrmeerTerrain.FLOOR + 6, true, 16, 12, 140),
            new View("urmeer_trench", UrmeerBiomes.TRENCH, h -> h < 24, true, 12, 22, 250),
            new View("urmeer_overview", UrmeerBiomes.ISLES, h -> h > SEA + 8, false, 110, 70, 35),
            // seit 0.92 die Landschaften unter Wasser
            new View("urmeer_kelpforest", UrmeerBiomes.KELP_FOREST, h -> h > UrmeerTerrain.FLOOR - 6 && h < SEA - 24, true, 18, 13, 150),
            new View("urmeer_coralgarden", UrmeerBiomes.CORAL_GARDEN, h -> h > SEA - 26 && h < SEA - 10, true, 12, 6, 120),
            new View("urmeer_pearlgrounds", UrmeerBiomes.PEARL_GROUNDS, h -> h > UrmeerTerrain.FLOOR - 6 && h < SEA - 24, true, 14, 8, 200),
            new View("urmeer_glowdepths", UrmeerBiomes.GLOW_DEPTHS, h -> h < 30, true, 16, 6, 120),
            // dieselbe Tiefe ohne Nachtsicht: nur das Leuchten der Bäume und Felder
            new View("urmeer_glowdepths_dark", UrmeerBiomes.GLOW_DEPTHS, h -> h < 30, true, 16, 6, 120, true));

    static void views(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        for (View view : VIEWS) {
            if (!only.isEmpty() && !view.name().contains(only)) {
                continue;
            }
            double[] cam = new double[5];
            boolean[] ready = new boolean[1];
            steps.add(Step.server(overworld -> {
                ServerLevel level = overworld.getServer().getLevel(UrmeerDimension.LEVEL);
                if (level == null) {
                    ready[0] = true;
                    return;
                }
                var found = level.findClosestBiome3d(h -> h.is(view.biome()), new BlockPos(Math.floorMod(view.name().hashCode(), 900), SEA, 0),
                        6000, 32, 64);
                BlockPos target = found != null ? found.getFirst() : new BlockPos(0, SEA, 0);
                var generator = level.getChunkSource().getGenerator();
                var random = level.getChunkSource().randomState();
                BlockPos at = null;
                search:
                for (int r = 0; r <= 600; r += 6) {
                    int count = r == 0 ? 1 : (int) (2 * Math.PI * r / 6);
                    for (int i = 0; i < count; i++) {
                        double ang = 2 * Math.PI * i / count;
                        int x = target.getX() + (int) (Math.cos(ang) * r);
                        int z = target.getZ() + (int) (Math.sin(ang) * r);
                        int h = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                        if (view.ground().test(h) && level.getBiome(new BlockPos(x, Math.max(h, SEA - 1), z)).is(view.biome())) {
                            at = new BlockPos(x, h, z);
                            break search;
                        }
                    }
                }
                if (at == null) {
                    at = new BlockPos(target.getX(), SEA, target.getZ());
                }
                double a = Math.toRadians(view.turn());
                cam[0] = at.getX() + Math.cos(a) * view.distance();
                cam[2] = at.getZ() + Math.sin(a) * view.distance();
                // über dem Wasser soll die Kamera über Wasser stehen, darunter nicht im Fels einer Nadel: sonst weiter hinaus
                // bzw. ringsum suchen
                for (int tries = 0; tries < 24; tries++) {
                    int hx = generator.getBaseHeight((int) Math.floor(cam[0]), (int) Math.floor(cam[2]), Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                    // unter Wasser auch nicht über einem Hügel oder in einer Baumkrone, von wo die Kamera steil nach unten sähe
                    boolean ok;
                    if (view.under()) {
                        int qx = (int) Math.floor(cam[0]);
                        int qz = (int) Math.floor(cam[2]);
                        level.getChunk(qx >> 4, qz >> 4);
                        int top = level.getHeight(Heightmap.Types.OCEAN_FLOOR, qx, qz);
                        ok = hx < SEA - 8 && top <= at.getY() + view.height() - 2;
                    } else {
                        ok = view.horizon() || hx < SEA - 1;
                    }
                    if (ok) {
                        break;
                    }
                    if (view.under()) {
                        a += Math.toRadians(30);
                    }
                    double d = view.distance() + (view.under() ? 0 : (tries + 1) * 8);
                    cam[0] = at.getX() + Math.cos(a) * d;
                    cam[2] = at.getZ() + Math.sin(a) * d;
                }
                int cx = (int) Math.floor(cam[0]);
                int cz = (int) Math.floor(cam[2]);
                level.getChunk(cx >> 4, cz >> 4);
                double look;
                if (view.horizon()) {
                    cam[1] = SEA + view.height();
                    cam[3] = -Math.toDegrees(Math.atan2(Math.cos(a), Math.sin(a))) + 180;
                    cam[4] = 6;
                    KingdomsAtWar.LOGGER.info("[Showcase] {}: Horizont bei {} {}", view.name(), (int) cam[0], (int) cam[2]);
                    ready[0] = true;
                    return;
                }
                if (view.under()) {
                    // unter Wasser: über dem Grund an der Kamera, aber unter der Oberfläche
                    int floorHere = level.getHeight(Heightmap.Types.OCEAN_FLOOR, cx, cz);
                    cam[1] = Math.min(SEA - 2, Math.max(at.getY() + view.height(), floorHere + 2));
                    look = at.getY() + 1;
                } else {
                    int below = level.getHeight(Heightmap.Types.MOTION_BLOCKING, cx, cz);
                    cam[1] = Math.max(Math.max(at.getY(), SEA) + view.height(), below + 3);
                    look = Math.max(at.getY(), SEA) + 1;
                }
                double dx = at.getX() + 0.5 - cam[0];
                double dz = at.getZ() + 0.5 - cam[2];
                cam[3] = -Math.toDegrees(Math.atan2(dx, dz));
                cam[4] = Math.toDegrees(Math.atan2(cam[1] + 1.62 - look, Math.hypot(dx, dz)));
                KingdomsAtWar.LOGGER.info("[Showcase] {}: Grund bei {} (Biom {}), Kamera {} {} {}", view.name(), at,
                        level.getBiome(at).unwrapKey().map(k -> k.identifier().getPath()).orElse("?"), (int) cam[0], (int) cam[1], (int) cam[2]);
                ready[0] = true;
            }));
            steps.add(Step.until(() -> ready[0], 6000));
            if (view.dark()) {
                steps.add(Step.command("effect clear @s minecraft:night_vision"));
                steps.add(Step.command("effect clear @s minecraft:conduit_power"));
            }
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:urmeer run tp @s %.1f %.1f %.1f %.1f %.1f", cam[0], cam[1], cam[2], cam[3], cam[4]))));
            steps.add(arrived(cam));
            steps.add(Step.waitTicks(40));
            steps.add(Step.waitForChunks(800));
            steps.add(Step.waitTicks(view.dark() ? 160 : 80));
            steps.add(Step.screenshot(view.name()));
            if (view.dark()) {
                steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
                steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
            }
        }
    }

    static Step arrived(double[] cam) {
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.player != null && minecraft.level.dimension() == UrmeerDimension.LEVEL
                    && minecraft.player.distanceToSqr(cam[0], cam[1], cam[2]) < 4.0;
        }, 1200);
    }

    /** Karte des Urmeers von oben (Biomfarbe, nach Tiefe schattiert, Inseln grün) als Bild im Spielordner. */
    static void map(Deque<Step> steps) {
        boolean[] done = new boolean[1];
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(UrmeerDimension.LEVEL);
            if (level == null) {
                done[0] = true;
                return;
            }
            var generator = level.getChunkSource().getGenerator();
            var random = level.getChunkSource().randomState();
            int size = 512;
            int step = 6;
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var resolver = generator.getBiomeSource().createUncachedResolver(random);
            int[] counts = new int[5];
            int land = 0;
            for (int px = 0; px < size; px++) {
                for (int pz = 0; pz < size; pz++) {
                    int x = (px - size / 2) * step;
                    int z = (pz - size / 2) * step;
                    int h = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                    var biome = resolver.getNoiseBiome(x >> 2, SEA >> 2, z >> 2);
                    int index = biome.is(UrmeerBiomes.DEEP) ? 0 : biome.is(UrmeerBiomes.REEF) ? 1 : biome.is(UrmeerBiomes.TRENCH) ? 2
                            : biome.is(UrmeerBiomes.ISLES) ? 3 : 4;
                    counts[index]++;
                    int rgb;
                    if (h >= SEA) {
                        land++;
                        float shade = 0.6f + 0.4f * Math.min(1.0f, (h - SEA) / 24.0f);
                        rgb = mix(0x000000, h < SEA + 3 ? 0xE8D8A0 : 0x5AA84A, shade);
                    } else {
                        float depth = Math.min(1.0f, (SEA - h) / 110.0f);
                        int base = switch (index) {
                            case 1 -> 0x40D0D0;
                            case 2 -> 0x183060;
                            default -> 0x3A88D0;
                        };
                        rgb = mix(base, 0x000818, depth * 0.85f);
                    }
                    img.setRGB(px, pz, rgb);
                }
            }
            try {
                javax.imageio.ImageIO.write(img, "png", new java.io.File("urmeer_map.png"));
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
            int total = size * size;
            KingdomsAtWar.LOGGER.info("[Showcase] Urmeer-Karte ({} Blöcke): Weiten {} %, Korallenbänke {} %, Gräben {} %, Sturminseln {} %, Land {} %",
                    size * step, counts[0] * 100 / total, counts[1] * 100 / total, counts[2] * 100 / total, counts[3] * 100 / total,
                    land * 100 / total);
            done[0] = true;
        }));
        steps.add(Step.until(() -> done[0], 12000));
        steps.add(Step.waitTicks(20));
    }

    private static int mix(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    /** Ein Gezeitentor, wie es im Urmeer bei der Ankunft entsteht: auf seiner Plattform über dem Wasser, von vorn und schräg. */
    static void gate(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        int[] foot = new int[3];
        boolean[] ready = new boolean[1];
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(UrmeerDimension.LEVEL);
            if (level == null) {
                ready[0] = true;
                return;
            }
            // eine Stelle über offenem Wasser
            BlockPos spot = new BlockPos(3000, SEA, 0);
            var generator = level.getChunkSource().getGenerator();
            var random = level.getChunkSource().randomState();
            for (int i = 0; i < 64; i++) {
                int h = generator.getBaseHeight(spot.getX(), spot.getZ(), Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                if (h < SEA - 20) {
                    break;
                }
                spot = spot.offset(48, 0, 0);
            }
            var gate = TideGate.build(level, spot, Direction.Axis.X);
            foot[0] = gate.foot().getX();
            foot[1] = gate.foot().getY();
            foot[2] = gate.foot().getZ();
            KingdomsAtWar.LOGGER.info("[Showcase] Gezeitentor bei {}", gate.foot());
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        double[][] cams = {{0.5, 3, 15}, {12, 6, 10}, {-9, 9, -12}};
        for (int i = 0; i < cams.length; i++) {
            double[] c = cams[i];
            double[] cam = new double[3];
            String name = "urmeer_gate_" + i;
            steps.add(Step.once(minecraft -> {
                cam[0] = foot[0] + c[0];
                cam[1] = foot[1] + c[1];
                cam[2] = foot[2] + c[2];
                double dx = foot[0] + 0.5 - cam[0];
                double dz = foot[2] + 0.5 - cam[2];
                double dy = (foot[1] + TideGate.RADIUS) - (cam[1] + 1.62);
                float yaw = (float) (-Math.toDegrees(Math.atan2(dx, dz)));
                float pitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.hypot(dx, dz))));
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in kingdomsatwar:urmeer run tp @s %.1f %.1f %.1f %.1f %.1f",
                        cam[0], cam[1], cam[2], yaw, pitch));
            }));
            steps.add(Step.waitTicks(10));
            steps.add(arrived(cam));
            steps.add(Step.waitForChunks(600));
            steps.add(Step.waitTicks(60));
            steps.add(Step.screenshot(name));
        }
    }
}
