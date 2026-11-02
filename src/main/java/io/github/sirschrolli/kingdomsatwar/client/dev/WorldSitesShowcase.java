package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Vorführung der eigenen Bauwerke der Oberwelt, so wie die Welt sie erzeugt: von einem festen Punkt aus das nächste
 * Bauwerk jeder Art suchen (wie /locate), seine Chunks vollständig erzeugen lassen und es von innen und außen zeigen
 * (mit Nachtsicht, damit man unter der Erde etwas erkennt).
 */
final class WorldSitesShowcase {
    private static final int SLOTS = 32;

    private WorldSitesShowcase() {}

    static void sites(Deque<Step> steps, String only) {
        if (only.equals("survey")) {
            survey(steps, 48);
            return;
        }
        steps.add(Step.hud(false));
        // in der Unterwelt weiter sehen: dort reicht der Nebel nur bis zur halben Sichtweite
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(only.equals("ash_forge") || only.equals("hortfeste") ? 16 : 8)));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        for (SiteKind kind : SiteKind.values()) {
            if (!only.isEmpty() && !kind.id().equals(only)) {
                continue;
            }
            List<double[]> views = new ArrayList<>();
            int[] state = {0};
            // die Aschenesse steht in der Unterwelt; dort schwebt die Kamera als Zuschauer (über Lava, durch Felsen)
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = kind == SiteKind.ASH_FORGE || kind == SiteKind.HORTFESTE
                    ? net.minecraft.world.level.Level.NETHER : net.minecraft.world.level.Level.OVERWORLD;
            if (dim != net.minecraft.world.level.Level.OVERWORLD) {
                steps.add(Step.command("gamemode spectator"));
            }
            steps.add(Step.server(level -> locate(level.getServer().getLevel(dim), kind, views, state)));
            steps.add(Step.until(() -> state[0] != 0, 9000));
            for (int n = 0; n < SLOTS; n++) {
                int slot = n;
                steps.add(Step.once(minecraft -> {
                    if (slot < views.size()) {
                        double[] v = views.get(slot);
                        double lx = v[3] - v[0];
                        double ly = v[4] - (v[1] + 1.62);
                        double lz = v[5] - v[2];
                        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
                        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
                        minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                                "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", dim.identifier(), v[0], v[1], v[2], yaw, pitch));
                    }
                }));
                steps.add((minecraft, ticks) -> slot >= views.size() || ticks > (slot == 0 ? 300 : 60));
                steps.add((minecraft, ticks) -> slot >= views.size() || Step.waitForChunks(1500).run(minecraft, ticks));
                Step shot = Step.screenshot("site_" + kind.id() + "_" + n);
                steps.add((minecraft, ticks) -> slot >= views.size() || shot.run(minecraft, ticks));
            }
        }
    }

    /**
     * Zählung ({@code worldsite_survey}): alle Bauwerke, die in einem Quadrat von 2 × {@code radius} Chunks um den Ursprung
     * beginnen, dazu die Burgruinen (ein Feature in festen Feldern) – je Art die Zahl, die Dichte je Quadratkilometer und
     * ein paar Fundorte.
     */
    static void survey(Deque<Step> steps, int radius) {
        steps.add(Step.server(level -> {
            long started = System.currentTimeMillis();
            java.util.Map<String, Integer> counts = new java.util.TreeMap<>();
            java.util.Map<String, List<String>> where = new java.util.TreeMap<>();
            var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            for (int cx = -radius; cx < radius; cx++) {
                for (int cz = -radius; cz < radius; cz++) {
                    var chunk = level.getChunk(cx, cz, ChunkStatus.STRUCTURE_STARTS, true);
                    for (var e : chunk.getAllStarts().entrySet()) {
                        if (!e.getValue().isValid()) {
                            continue;
                        }
                        String name = String.valueOf(registry.getKey(e.getKey()));
                        counts.merge(name, 1, Integer::sum);
                        // Bauwerke unter der Erde: reichen sie bis an die Oberfläche?
                        if (name.endsWith(":aqueduct") || name.endsWith(":buried_abbey") || name.endsWith(":trial_arena")
                                || name.endsWith(":silent_cathedral")) {
                            var box = e.getValue().getBoundingBox();
                            int surface = level.getChunkSource().getGenerator().getBaseHeight(box.getCenter().getX(), box.getCenter().getZ(),
                                    net.minecraft.world.level.levelgen.Heightmap.Types.OCEAN_FLOOR_WG, level, level.getChunkSource().randomState());
                            counts.merge(name + " (bis an die Oberfläche)", box.maxY() >= surface - 1 ? 1 : 0, Integer::sum);
                        }
                        List<String> list = where.computeIfAbsent(name, k -> new ArrayList<>());
                        if (list.size() < 3) {
                            BlockPos c = e.getValue().getBoundingBox().getCenter();
                            list.add(c.getX() + " " + e.getValue().getBoundingBox().minY() + ".." + e.getValue().getBoundingBox().maxY() + " " + c.getZ());
                        }
                    }
                }
            }
            // Burgruinen: in jedem Feld ein fester Chunk; zählen, wo das Biom passt
            var gen = level.getChunkSource().getGenerator();
            var resolver = gen.getBiomeSource().createResolver(level.getChunkSource().randomState()
                    .createClimateSampler(net.minecraft.world.level.levelgen.densityfunction.SamplerContext.EMPTY_UNCACHED));
            var ruinBiomes = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(io.github.sirschrolli.kingdomsatwar.registry.ModTags.Biomes.HAS_CASTLE_RUINS);
            for (var ruin : io.github.sirschrolli.kingdomsatwar.worldgen.ModWorldgen.STRUCTURES) {
                int n = 0;
                for (int cx = -radius; cx < radius; cx++) {
                    for (int cz = -radius; cz < radius; cz++) {
                        if (io.github.sirschrolli.kingdomsatwar.worldgen.SpacedPlacement.isGridChunk(level.getSeed(), ruin.salt(), cx, cz, ruin.spacing(),
                                ruin.separation())) {
                            var biome = resolver.getNoiseBiome(cx * 4 + 2, 16, cz * 4 + 2);
                            if (ruinBiomes.contains(biome)) {
                                n++;
                            }
                        }
                    }
                }
                counts.put("feature:" + ruin.name(), n);
            }
            double km2 = Math.pow(radius * 2 * 16 / 1000.0, 2);
            KingdomsAtWar.LOGGER.info("[Showcase] Zählung: {} × {} Blöcke ({} km²), {} ms", radius * 32, radius * 32, String.format(Locale.ROOT, "%.2f", km2),
                    System.currentTimeMillis() - started);
            int total = 0;
            for (var e : counts.entrySet()) {
                total += e.getValue();
                KingdomsAtWar.LOGGER.info("[Showcase]   {}: {} ({} je km²) {}", e.getKey(), e.getValue(), String.format(Locale.ROOT, "%.1f", e.getValue() / km2),
                        where.getOrDefault(e.getKey(), List.of()));
            }
            KingdomsAtWar.LOGGER.info("[Showcase]   zusammen: {} ({} je km²)", total, String.format(Locale.ROOT, "%.1f", total / km2));
        }));
    }

    private static void locate(ServerLevel level, SiteKind kind, List<double[]> views, int[] state) {
        long started = System.currentTimeMillis();
        Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(kind));
        // beim Sternensturz von drei Seiten suchen: so finden sich ein Krater und ein Sternfels
        BlockPos[] from = kind == SiteKind.FALLEN_STAR
                ? new BlockPos[] {new BlockPos(0, 80, 0), new BlockPos(-1000, 80, 1700), new BlockPos(-1000, 80, -1700), new BlockPos(2000, 80, 0)}
                : kind == SiteKind.ASH_FORGE || kind == SiteKind.HORTFESTE ? new BlockPos[] {new BlockPos(0, 80, 0), new BlockPos(900, 80, -700)}
                : new BlockPos[] {new BlockPos(0, 80, 0)};
        List<StructureStart> starts = new ArrayList<>();
        for (BlockPos origin : from) {
            var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), origin, 300, false);
            if (found == null) {
                continue;
            }
            ChunkPos chunk = ChunkPos.containing(found.getFirst());
            StructureStart start = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
            if (start != null && start.isValid() && starts.stream().noneMatch(s -> s.getChunkPos().equals(start.getChunkPos()))) {
                starts.add(start);
            }
        }
        if (starts.isEmpty()) {
            KingdomsAtWar.LOGGER.info("[Showcase] {} nicht gefunden", kind.id());
            state[0] = -1;
            return;
        }
        if (kind == SiteKind.FALLEN_STAR) {
            StructureStart crater = null;
            StructureStart rock = null;
            for (StructureStart s : starts) {
                int variant = ((SitePiece) s.getPieces().getFirst()).data(0);
                if (variant == 0 && crater == null) {
                    crater = s;
                } else if (variant == 1 && rock == null) {
                    rock = s;
                }
            }
            starts.clear();
            if (crater != null) {
                starts.add(crater);
            }
            if (rock != null) {
                starts.add(rock);
            }
        }
        for (StructureStart start : starts) {
            show(level, kind, start, views, started);
        }
        state[0] = 1;
    }

    private static void show(ServerLevel level, SiteKind kind, StructureStart start, List<double[]> views, long started) {
        BoundingBox box = start.getBoundingBox();
        int around = kind == SiteKind.FALLEN_STAR ? 4 : 2;
        for (int cx = (box.minX() >> 4) - around; cx <= (box.maxX() >> 4) + around; cx++) {
            for (int cz = (box.minZ() >> 4) - around; cz <= (box.maxZ() >> 4) + around; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        List<SitePiece> pieces = new ArrayList<>();
        for (StructurePiece p : start.getPieces()) {
            if (p instanceof SitePiece site) {
                pieces.add(site);
            }
        }
        SitePiece first = pieces.getFirst();
        KingdomsAtWar.LOGGER.info("[Showcase] {} bei {} ({}), {} Teile, Daten {}, Fläche {}×{}×{}, {} ms", kind.id(), first.origin(),
                first.rotation(), pieces.size(), java.util.Arrays.toString(new int[] {first.data(0), first.data(1), first.data(2), first.data(3),
                        first.data(4)}), box.getXSpan(), box.getYSpan(), box.getZSpan(), System.currentTimeMillis() - started);
        switch (kind) {
            case FALLEN_STAR -> {
                int floor = first.data(1);
                int camp = first.data(4);
                boolean rock = first.data(0) == 1;
                int[] a = switch (camp & 3) {
                    case 1 -> new int[] {1, 0};
                    case 2 -> new int[] {0, 1};
                    case 3 -> new int[] {-1, 0};
                    default -> new int[] {0, -1};
                };
                if (rock) {
                    add(views, first, -24, 12, -20, 0, 0, 0);
                    add(views, first, -26, 2, 18, 0, -2, 0);
                    add(views, first, 4, 6, -3, 0, floor + 6, 0);
                } else {
                    add(views, first, -26, 34, -20, 0, -8, 0);
                    add(views, first, 9, 1, 7, 0, floor + 4, 0);
                    add(views, first, a[0] * 19 + a[1] * 7, -1, a[1] * 19 - a[0] * 7, a[0] * 12, -5, a[1] * 12);
                }
                add(views, first, 9, floor + 3, -6, 0, floor + 2.5, 0);
                add(views, first, -4, floor + 9, 6, 0, floor + 2, 0);
                add(views, first, -a[0] * 3, floor + 2, -a[1] * 3, -a[0] * 9, floor + 1.5, -a[1] * 9);
                add(views, first, 1.5, floor + 1, 1.5, 3, 30, 3);
            }
            case AQUEDUCT -> {
                int shaft = first.data(1);
                add(views, first, 3, 1.5, 3, -3, 1, -3);
                if (shaft > 0) {
                    add(views, first, 8, shaft + 4, 8, 0, shaft + 2, 0);
                }
                boolean tunnel = false;
                boolean cistern = false;
                boolean stairs = false;
                for (SitePiece p : pieces) {
                    if (p.part() == 1 && !tunnel) {
                        add(views, p, 0.5, 1, 0.5, 0, 2, 12);
                        tunnel = true;
                    } else if (p.part() == 2 && !cistern) {
                        add(views, p, -6, 4, -2, 2, -1, 2);
                        add(views, p, 6, 5, 2, -2, 0, -2);
                        cistern = true;
                    } else if (p.part() == 3 && !stairs) {
                        add(views, p, 0, 1, 0.5, 0, p.data(1) * 4 + 2, 8);
                        stairs = true;
                    }
                }
                for (SitePiece p : pieces) {
                    KingdomsAtWar.LOGGER.info("[Showcase]   Teil {} bei {} ({}), Daten {} {}", p.part(), p.origin(), p.rotation(), p.data(0), p.data(1));
                }
            }
            case SEA_DRAGON -> {
                add(views, first, -16, 12, -6, 0, 3, 14);
                add(views, first, -22, 9, 12, 0, 3, 14);
                add(views, first, 4, 5, -24, 0, 3, -2);
                add(views, first, 6, 24, 16, 0, 0, 17);
                add(views, first, 2, 2, 9, 0, 1, 18);
                add(views, first, 6, 4, -9, 0, 3, -5);
            }
            case CARAVANSERAI -> {
                add(views, first, -26, 26, 30, 0, 0, 0);
                add(views, first, 0, 2, 17, 0, 3, 0);
                add(views, first, 3, 2, -3, -4, 1, 4);
                add(views, first, 0.5, -6, 3, 0, -6, 7);
                add(views, first, -22, 10, 20, 0, 4, 0);
            }
            case SERPENT_TEMPLE -> {
                add(views, first, -14, 16, -16, 4, 3, 8);
                add(views, first, 14, 9, 6, 0, 4, 9);
                add(views, first, 0, 1.5, 1, 0, 1.5, 8);
                add(views, first, 4, -5, 18, -1, -6, 26);
                add(views, first, -3, -5, 21, 4, -6, 20);
            }
            case WITCH_TREE -> {
                add(views, first, -14, 6, 14, 0, 8, 0);
                add(views, first, 1, 1, 1.5, -1, 1.5, -1.5);
                add(views, first, 9, 21, 9, 0, 14, 0);
            }
            case FROST_GROTTO -> {
                add(views, first, -12, 6, 16, 0, 2, 0);
                add(views, first, 2.5, 1, 2, -3, 1.5, -2);
                add(views, first, -2, -5, 1.5, -5, -5, 0);
            }
            case BANDIT_CAMP -> {
                add(views, first, -20, 14, 22, 0, 0, 0);
                add(views, first, 3, 1.5, 6, -4, 1, -6);
                add(views, first, -2, 1, -5, -2, 0.5, -8);
            }
            case CHARCOAL_CAMP -> {
                add(views, first, -9, 16, 9, 0, 1, 0);
                add(views, first, 7, 2, -1, -3, 2, -2);
            }
            case EMBER_GATE -> {
                add(views, first, 0.5, 2, 8, 0.5, 2.5, -2);
                add(views, first, -9, 5, 6, 0, 2, -2);
            }
            case CLAM_FIELD -> {
                add(views, first, -11, 6, 11, 0, 2, 0);
                add(views, first, 7, 3, -6, 0, 2, 0);
                add(views, first, 3, 2.5, 3, 0, 1.5, 0);
            }
            case CORAL_PALACE -> {
                add(views, first, -24, 16, -26, 0, 9, 0);
                add(views, first, 22, 6, 18, 0, 8, 0);
                add(views, first, 0.5, 4, -7, 0.5, 4, 6);
                add(views, first, 6, 12, 6, -2, 14, -2);
                add(views, first, 3, -2.5, 3, -3, -3, -3);
            }
            case BURIED_ABBEY -> {
                int surface = first.data(0);
                int top = Math.max(surface + 6, 12);
                add(views, first, -6, surface + 9, 6, 9, surface + 4, -9);
                add(views, first, 9, top - 2, -8.5, 9, top - 1.5, -12);
                add(views, first, -5.5, 1, -5.5, 5, 2, -5.5);
                add(views, first, 0.5, 1, -7, 0.5, 1.5, -11);
                add(views, first, 0.5, 1, 7, 0.5, 1.5, 11);
                add(views, first, 9, 1, -6, 9, top - 3, -9.5);
            }
            case SILENT_CATHEDRAL -> {
                add(views, first, -8, 8, 30, 2, 12, 10);
                add(views, first, 0.5, 1, 15, 0.5, 6, -14);
                add(views, first, 0.5, 2, -11, 0.5, 6, -22);
                add(views, first, 18, 4, 2, 6, 12, 2);
                add(views, first, 9, 1, 14, 9, 2, -4);
                add(views, first, -8, 1, -12, -10, 2, -16);
                add(views, first, 0.5, 2, 35, 0.5, 12, 19);
            }
            case TRIAL_ARENA -> {
                add(views, first, 0, 16, 20, 0, 2, -4);
                add(views, first, 0.5, 9, -9, 0.5, 8.5, -17);
                add(views, first, -14, 11, 0, 8, 2, 4);
                add(views, first, 0.5, 1, 26, 0.5, 3, 12);
                add(views, first, 30, 6, 10, 20, 6, 4);
                add(views, first, 9, 1, 3, 13, 1.5, 8);
            }
            case ASH_FORGE -> {
                if (!views.isEmpty()) {
                    // die zweite Esse nur von außen und von ihren Brücken
                    add(views, first, -14, 20, 14, 0, 30, 0);
                    add(views, first, -19, 3, 10, 0, 28, 0);
                    for (SitePiece p : pieces) {
                        if (p.part() == 1) {
                            KingdomsAtWar.LOGGER.info("[Showcase]   Brücke {} ({}): Länge {}, Ende {}, Ausgang {}", p.data(3), p.rotation(), p.data(0),
                                    p.data(1), p.data(2));
                            add(views, p, 2.5, 10, 14, 0, 11, p.data(0) + 4);
                        }
                    }
                    return;
                }
                add(views, first, -14, 20, 14, 0, 30, 0);
                add(views, first, 15, 44, -15, 0, 48, 0);
                add(views, first, -19, 3, 10, 0, 28, 0);
                add(views, first, 0.5, 3, 24, 0.5, 5, 8);
                add(views, first, 7.5, 10, 7.5, -5, 11, -5);
                add(views, first, -8, 13, -3, 6, 11, -6);
                add(views, first, -6.5, 22, 4, -6, 19, -7);
                add(views, first, 2, 28, 7, -7, 29, 5);
                add(views, first, 6.5, 38.2, -0.5, 0, 39.5, 9);
                add(views, first, -6, 50, 8, 0, 55, 0);
                for (SitePiece p : pieces) {
                    if (p.part() != 1) {
                        continue;
                    }
                    int len = p.data(0);
                    KingdomsAtWar.LOGGER.info("[Showcase]   Brücke {} ({}): Länge {}, Ende {}, Ausgang {}", p.data(3), p.rotation(), len, p.data(1), p.data(2));
                    add(views, p, 2.5, 10, 14, 0, 11, len + 4);
                    if (p.data(3) == 0) {
                        add(views, p, 0.5, 10.5, len - 1, 0.5, 16, 0);
                    }
                    if (p.data(1) == 1) {
                        add(views, p, 3.5, 10, len + 7, -1, 10.5, len + 13);
                    } else {
                        add(views, p, 5, 15, len - 2, 0, 10, len + 6);
                    }
                    add(views, p, 9, 3, len - 7, 0, 5, len - 7);
                }
            }
            case HORTFESTE -> {
                if (!views.isEmpty()) {
                    // die zweite Feste nur von außen
                    add(views, first, -20, 18, 38, 0, 14, 4);
                    add(views, first, 28, 16, -24, 0, 14, 0);
                    return;
                }
                add(views, first, -20, 18, 38, 0, 14, 4);
                add(views, first, 0, 5, 50, 0, 18, 18);
                add(views, first, 9, 34, 16, 0, 33, 2);
                add(views, first, -8, 33, -10, 0, 33, 1);
                add(views, first, 13, 32, 3, 0, 33, 1);
                add(views, first, 28, 16, -24, 0, 14, 0);
                add(views, first, 0, 3, 13, 0, 3, -2);
                add(views, first, 10, 3, -10, -6, 2, 6);
                add(views, first, -11, 3, -11, -7, 1, -17);
                add(views, first, 10, 13, 10, -4, 12, -6);
                add(views, first, -2, 22, 4, 0, 21, -6);
                add(views, first, 16, 13, 16, 0, 15, 0);
            }
            case ROBBER_CASTLE -> {
                add(views, first, -26, 18, 30, 0, 6, 0);
                add(views, first, 0.5, 6, 36, 0.5, 4, 14);
                add(views, first, -6, 2, 6, 6, 4, -6);
                add(views, first, 6, 2, -3, 8, 2, 4);
                add(views, first, 0.5, 14, -9, -1, 14, -13);
                add(views, first, 26, 14, -22, 0, 10, -8);
                add(views, first, 0.5, -5, -9.5, 0.5, -4, -13);
            }
            case TIDE_SHRINE -> {
                add(views, first, 8, 5, 8, 0, 2, 0);
                add(views, first, -10, 3, 3, 0, 3, 0);
                add(views, first, 1, -3, -1, 0, -3, 0.5);
            }
        }
    }

    /** Ein Blick in eigenen Koordinaten des Bauteils (Füße bei x, y, z; Blick auf lx, ly, lz). */
    private static void add(List<double[]> views, SitePiece piece, double x, double y, double z, double lx, double ly, double lz) {
        BlockPos o = piece.origin();
        Rotation r = piece.rotation();
        double[] eye = turn(x, z, r);
        double[] look = turn(lx, lz, r);
        views.add(new double[] {o.getX() + eye[0] + 0.5, o.getY() + y, o.getZ() + eye[1] + 0.5, o.getX() + look[0] + 0.5, o.getY() + ly,
                o.getZ() + look[1] + 0.5});
    }

    /** Wie {@code BlockPos.rotate}. */
    private static double[] turn(double x, double z, Rotation rotation) {
        return switch (rotation) {
            case NONE -> new double[] {x, z};
            case CLOCKWISE_90 -> new double[] {-z, x};
            case CLOCKWISE_180 -> new double[] {-x, -z};
            case COUNTERCLOCKWISE_90 -> new double[] {z, -x};
        };
    }
}
