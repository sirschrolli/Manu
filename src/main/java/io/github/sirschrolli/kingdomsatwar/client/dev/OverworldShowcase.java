package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Vorführung der eigenen Oberwelt-Biome ({@code biomes} oder {@code biome_<name>}): sucht jedes Biom in der neuen Welt,
 * wählt eine Stelle mitten darin und fotografiert es von oben und vom Boden aus (die Aschefelder und das Nebelmoor auch
 * nachts). Dazu schreibt sie ins Log, woraus Boden und Bewuchs dort bestehen.
 */
final class OverworldShowcase {
    private OverworldShowcase() {}

    static void tour(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        for (ResourceKey<Biome> key : OverworldBiomes.ALL) {
            if (only.isEmpty() || key.identifier().getPath().equals(only)) {
                biome(steps, key);
            }
        }
    }

    /**
     * Galerie der eigenen Features ({@code features} oder {@code features_<biom>}) in der flachen Welt: jedes auf
     * eigenem, passend gestaltetem Boden, dreimal nebeneinander, schräg von oben fotografiert.
     */
    static void gallery(Deque<Step> steps, int[] ground, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        int index = 0;
        for (io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldFeatures.Entry entry
                : io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldFeatures.entries()) {
            if (entry.own() == null || !only.isEmpty() && !entry.name().startsWith(only + "/")) {
                continue;
            }
            int x = 3000 + index * 64;
            int z = 3000;
            index++;
            String name = entry.name().replace('/', '_');
            boolean small = entry.name().matches(".*(lilies|flowers|crystals|glowcaps|bells|brambles|ferns|moss_carpets|clusters|moonflowers"
                    + "|flower_fields|icicles|reeds|fulgurites|reefs|vents|fissures|drifts|dunes|pools|shards).*");
            steps.add(Step.command(String.format(Locale.ROOT, "tp @s %d %d %d 135 35", x - 14, ground[0] + 14, z - 14)));
            steps.add(Step.waitTicks(20));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.server(level -> {
                var feature = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.FEATURE)
                        .getOrThrow(entry.featureKey()).value();
                BlockPos center = new BlockPos(x, ground[0], z);
                int[][] spots = small ? new int[][] {{0, 0}, {-5, 4}, {5, -4}, {4, 6}, {-6, -5}} : new int[][] {{0, 0}, {-11, 9}, {10, -9}};
                prepareGround(level, center, entry.name(), 22);
                int seed = 0;
                for (int[] spot : spots) {
                    BlockPos at = center.offset(spot[0], 0, spot[1]);
                    for (int attempt = 0; attempt < 4; attempt++) {
                        if (feature.place(level, level.getChunkSource().getGenerator(), net.minecraft.util.RandomSource.create(4242L + seed++), at)) {
                            break;
                        }
                    }
                }
            }));
            steps.add(Step.waitTicks(30));
            double dist = small ? 11.0 : 24.0;
            double up = small ? 7.0 : 16.0;
            steps.add(Step.command(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f -45 %.1f", x - dist, ground[0] + up, z - dist,
                    Math.toDegrees(Math.atan2(up - 3.0, Math.sqrt(2) * dist)))));
            steps.add(Step.waitTicks(20));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.screenshot("feature_" + name));
        }
    }

    /** Boden für ein Feature (wie im GameTest): je nach Biom Asche, Schnee, Sturmstein, Sand oder Gras; Teich, Becken, Felsdach. */
    private static void prepareGround(ServerLevel level, BlockPos origin, String name, int radius) {
        BlockState ground = Blocks.GRASS_BLOCK.defaultBlockState();
        if (name.startsWith("ash_fields/")) {
            ground = (name.endsWith("fire_lilies") ? io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.CINDER_SOIL.get()
                    : io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.ASH_BLOCK.get()).defaultBlockState();
        } else if (name.startsWith("frost_peaks/")) {
            ground = name.endsWith("frost_flowers") ? io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.FROZEN_SOIL.get().defaultBlockState()
                    : name.endsWith("icicles") ? io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.RIME_STONE.base().get().defaultBlockState()
                    : Blocks.SNOW_BLOCK.defaultBlockState();
        } else if (name.startsWith("storm_coast/") && !name.endsWith("trees")) {
            ground = io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.STORMSTONE.base().get().defaultBlockState();
        } else if (name.startsWith("coral_coast/")) {
            ground = Blocks.SAND.defaultBlockState();
        }
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                for (int dy = -6; dy <= 36; dy++) {
                    pos.set(origin.getX() + dx, origin.getY() + dy, origin.getZ() + dz);
                    BlockState state = dy >= 0 ? Blocks.AIR.defaultBlockState() : dy == -6 ? Blocks.STONE.defaultBlockState()
                            : dy == -1 ? ground : ground.is(Blocks.GRASS_BLOCK) ? Blocks.DIRT.defaultBlockState() : ground;
                    level.setBlock(pos, state, 2);
                }
            }
        }
        if (name.equals("coral_coast/reefs")) {
            for (int dx = -10; dx <= 10; dx++) {
                for (int dz = -10; dz <= 10; dz++) {
                    for (int dy = -1; dy >= -3; dy--) {
                        level.setBlock(origin.offset(dx, dy, dz), Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }
        } else if (name.equals("mist_moor/reeds")) {
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    if (dx * dx + dz * dz <= 22) {
                        level.setBlock(origin.offset(dx, -1, dz), Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }
        } else if (name.equals("frost_peaks/icicles")) {
            for (int dx = -8; dx <= 8; dx++) {
                for (int dz = -8; dz <= 8; dz++) {
                    level.setBlock(origin.offset(dx, 5, dz), Blocks.PACKED_ICE.defaultBlockState(), 2);
                }
            }
        }
    }

    private static void biome(Deque<Step> steps, ResourceKey<Biome> key) {
        String name = key.identifier().getPath();
        // c: x, z, Bodenhöhe, gefunden (1) / nicht gefunden (-1), Blickrichtung und Höhe der Übersicht,
        //    dann zwei Standpunkte am Boden (x, Augenhöhe, z, Blickrichtung)
        double[] c = new double[14];
        steps.add(Step.command("time set 6000"));
        steps.add(Step.server(level -> locate(level, key, c)));
        steps.add(Step.until(() -> c[3] != 0, 20 * 300));
        steps.add(overview(c));
        steps.add(Step.waitTicks(120));
        steps.add(Step.waitForChunks(2400));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("biome_" + name + "_overview"));
        steps.add(ground(c, 6, 6.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("biome_" + name + "_ground_a"));
        steps.add(ground(c, 10, 4.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("biome_" + name + "_ground_b"));
        if (key == OverworldBiomes.STORM_COAST) {
            // die Sturmküste im Gewitter
            steps.add(Step.command("weather thunder"));
            steps.add(overview(c));
            steps.add(Step.waitTicks(200));
            steps.add(Step.waitForChunks(1200));
            steps.add(Step.screenshot("biome_" + name + "_storm"));
            steps.add(Step.command("weather clear"));
            steps.add(Step.waitTicks(100));
        }
        if (key == OverworldBiomes.ASH_FIELDS) {
            steps.add(Step.command("time set 18000"));
            steps.add(overview(c));
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(1200));
            steps.add(Step.screenshot("biome_" + name + "_night"));
            steps.add(Step.command("time set 6000"));
        }
        steps.add(Step.server(level -> analyse(level, key, c)));
        steps.add(Step.waitTicks(10));
    }

    /** Sucht das Biom und darin eine Stelle, um die herum möglichst viel davon liegt; lädt das Gelände dort. */
    private static void locate(ServerLevel level, ResourceKey<Biome> key, double[] c) {
        String name = key.identifier().getPath();
        int sampleY = key == OverworldBiomes.FROST_PEAKS ? 150 : 70;
        Pair<BlockPos, Holder<Biome>> found = level.findClosestBiome3d(biome -> biome.is(key), new BlockPos(0, sampleY, 0), 6400, 32, 64);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] Biom {} nicht gefunden", name);
            c[3] = -1;
            return;
        }
        BlockPos start = found.getFirst();
        boolean coast = key == OverworldBiomes.CORAL_COAST || key == OverworldBiomes.STORM_COAST;
        // die Stelle mit der größten Abdeckung im Umkreis
        BlockPos best = start;
        int bestScore = -1;
        for (int i = -3; i <= 3; i++) {
            for (int j = -3; j <= 3; j++) {
                int x = start.getX() + i * 24;
                int z = start.getZ() + j * 24;
                int score = 0;
                for (int a = -3; a <= 3; a++) {
                    for (int b = -3; b <= 3; b++) {
                        int sx = x + a * 10;
                        int sz = z + b * 10;
                        if (!biomeAt(level, sx, sampleY, sz).is(key)) {
                            continue;
                        }
                        // an Küsten zählt nur, was knapp über dem Wasser liegt: Strand und Ufer, nicht das offene Meer
                        if (coast) {
                            int h = level.getChunkSource().getGenerator().getBaseHeight(sx, sz, Heightmap.Types.OCEAN_FLOOR_WG, level,
                                    level.getChunkSource().randomState());
                            if (h < 62 || h > 74) {
                                continue;
                            }
                        }
                        score++;
                    }
                }
                if (score > bestScore) {
                    bestScore = score;
                    best = new BlockPos(x, sampleY, z);
                }
            }
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.getChunk((best.getX() >> 4) + dx, (best.getZ() >> 4) + dz);
            }
        }
        int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, best.getX(), best.getZ());
        // Übersicht: aus der Richtung, in der das Gelände am tiefsten liegt (freie Sicht, an Küsten vom Wasser aus)
        double bestYaw = 0;
        int lowest = Integer.MAX_VALUE;
        for (int d = 0; d < 8; d++) {
            double yaw = d * 45.0;
            int[] xz = behind(best.getX() + 0.5, best.getZ() + 0.5, yaw, 36.0);
            level.getChunk(xz[0] >> 4, xz[1] >> 4);
            int h = level.getHeight(Heightmap.Types.MOTION_BLOCKING, xz[0], xz[1]);
            if (h < lowest) {
                lowest = h;
                bestYaw = yaw;
            }
        }
        c[0] = best.getX() + 0.5;
        c[1] = best.getZ() + 0.5;
        c[2] = ground;
        c[4] = bestYaw;
        // an Küsten tiefer (Blick übers Wasser auf die Küste), sonst höher
        c[5] = coast ? Math.max(ground + 10, lowest + 6) : Math.max(ground + 18, lowest + 6);
        // zwei freie Standpunkte am Boden: einer blickt zur offenen Seite, einer quer ins Biom
        openSpot(level, best, bestYaw + 180.0, c, 6);
        openSpot(level, best, bestYaw + 70.0, c, 10);
        c[3] = 1;
        KingdomsAtWar.LOGGER.info("[Showcase] Biom {} bei {} {} (Boden {}, Abdeckung {}/49)", name, best.getX(), best.getZ(), ground, bestScore);
    }

    /**
     * Sucht von der Mitte aus spiralförmig eine Stelle, an der man frei steht und in Blickrichtung {@code yaw} ein paar
     * Blöcke weit nichts vor den Augen hat; schreibt x, Augenhöhe, z und Blickrichtung ab {@code c[at]}.
     */
    private static void openSpot(ServerLevel level, BlockPos center, double yaw, double[] c, int at) {
        double rad = Math.toRadians(yaw);
        double dx = -Math.sin(rad);
        double dz = Math.cos(rad);
        BlockPos found = null;
        for (BlockPos.MutableBlockPos p : BlockPos.spiralAround(center.atY(0), 24, net.minecraft.core.Direction.EAST,
                net.minecraft.core.Direction.SOUTH)) {
            int ground = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, p.getX(), p.getZ());
            BlockPos eye = new BlockPos(p.getX(), ground + 1, p.getZ());
            if (!free(level, eye.below()) || !free(level, eye) || !level.getFluidState(eye.below()).isEmpty()) {
                continue;
            }
            // auch rechts und links nichts direkt vor den Augen (Felsnadeln, Stämme)
            boolean roomy = true;
            for (net.minecraft.core.Direction side : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                roomy &= free(level, eye.relative(side)) && free(level, eye.relative(side, 2));
            }
            if (!roomy) {
                continue;
            }
            boolean clear = true;
            for (int k = 1; k <= 5 && clear; k++) {
                clear = free(level, BlockPos.containing(eye.getX() + 0.5 + dx * k, eye.getY(), eye.getZ() + 0.5 + dz * k));
            }
            if (clear) {
                found = eye;
                break;
            }
        }
        if (found == null) {
            found = new BlockPos(center.getX(), level.getHeight(Heightmap.Types.MOTION_BLOCKING, center.getX(), center.getZ()) + 1, center.getZ());
        }
        c[at] = found.getX() + 0.5;
        c[at + 1] = found.getY() - 0.5;
        c[at + 2] = found.getZ() + 0.5;
        c[at + 3] = yaw;
    }

    private static boolean free(ServerLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        return state.isAir() || state.canBeReplaced() && !state.is(net.minecraft.tags.BlockTags.LEAVES);
    }

    private static Holder<Biome> biomeAt(ServerLevel level, int x, int y, int z) {
        return level.getUncachedNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(y), QuartPos.fromBlock(z));
    }

    /** Ein Punkt {@code distance} Blöcke hinter der Mitte, wenn man in Richtung {@code yaw} blickt. */
    private static int[] behind(double x, double z, double yaw, double distance) {
        double rad = Math.toRadians(yaw);
        // Blickrichtung bei Minecraft-Gierwinkel: (-sin, cos)
        return new int[] {(int) Math.floor(x + Math.sin(rad) * distance), (int) Math.floor(z - Math.cos(rad) * distance)};
    }

    /** Die Übersicht: 36 Blöcke vor der Mitte, schräg von oben, mit Blick über die Mitte hinweg ins Biom. */
    private static Step overview(double[] c) {
        return Step.once(minecraft -> {
            if (c[3] < 0) {
                return;
            }
            int[] xz = behind(c[0], c[1], c[4], 36.0);
            double cx = xz[0] + 0.5;
            double cz = xz[1] + 0.5;
            double cy = c[5];
            double rad = Math.toRadians(c[4]);
            double tx = c[0] - Math.sin(rad) * 10.0;
            double tz = c[1] + Math.cos(rad) * 10.0;
            double lx = tx - cx;
            double ly = c[2] - cy;
            double lz = tz - cz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cx, cy, cz, yaw, pitch));
        });
    }

    /** Ein Standpunkt am Boden (aus {@link #openSpot}). */
    private static Step ground(double[] c, int at, double pitch) {
        return Step.once(minecraft -> {
            if (c[3] < 0) {
                return;
            }
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    c[at], c[at + 1], c[at + 2], (float) c[at + 3], pitch));
        });
    }

    /** Schreibt ins Log, aus welchen Blöcken Oberfläche und Bewuchs rund um die Stelle bestehen. */
    private static void analyse(ServerLevel level, ResourceKey<Biome> key, double[] c) {
        if (c[3] < 0) {
            return;
        }
        Map<Block, Integer> top = new HashMap<>();
        Map<Block, Integer> above = new HashMap<>();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int columns = 0;
        for (int dx = -40; dx <= 40; dx += 2) {
            for (int dz = -40; dz <= 40; dz += 2) {
                int x = (int) Math.floor(c[0]) + dx;
                int z = (int) Math.floor(c[1]) + dz;
                if (!biomeAt(level, x, (int) c[2], z).is(key)) {
                    continue;
                }
                columns++;
                int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, x, z);
                top.merge(level.getBlockState(pos.set(x, ground - 1, z)).getBlock(), 1, Integer::sum);
                for (int y = ground; y < ground + 30; y++) {
                    BlockState state = level.getBlockState(pos.set(x, y, z));
                    if (!state.isAir() && !state.is(Blocks.WATER)) {
                        above.merge(state.getBlock(), 1, Integer::sum);
                    }
                }
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Biom {}: {} Säulen; Boden: {}; darüber: {}", key.identifier().getPath(), columns,
                summary(top, 8), summary(above, 14));
    }

    private static String summary(Map<Block, Integer> counts, int limit) {
        List<Map.Entry<Block, Integer>> list = new ArrayList<>(counts.entrySet());
        list.sort((a, b) -> b.getValue() - a.getValue());
        StringBuilder out = new StringBuilder();
        for (int i = 0; i < Math.min(limit, list.size()); i++) {
            out.append(BuiltInRegistries.BLOCK.getKey(list.get(i).getKey()).getPath()).append('=').append(list.get(i).getValue()).append(' ');
        }
        return out.toString().trim();
    }
}
