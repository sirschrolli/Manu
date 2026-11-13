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
        if (only.startsWith("lab_")) {
            lab(steps, only.substring(4));
            return;
        }
        steps.add(Step.hud(false));
        // in der Unterwelt weiter sehen: dort reicht der Nebel nur bis zur halben Sichtweite
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(only.equals("ash_forge") || only.equals("hortfeste") ? 16
                : only.equals("giant_bones") || only.equals("lost_gate") || only.equals("starwatch") ? 12 : 8)));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        for (SiteKind kind : SiteKind.values()) {
            if (!only.isEmpty() && !kind.id().equals(only)) {
                continue;
            }
            List<double[]> views = new ArrayList<>();
            int[] state = {0};
            // die Aschenesse steht in der Unterwelt; dort schwebt die Kamera als Zuschauer (über Lava, durch Felsen)
            net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = kind == SiteKind.STARWATCH ? net.minecraft.world.level.Level.END
                    : nether(kind) ? net.minecraft.world.level.Level.NETHER : net.minecraft.world.level.Level.OVERWORLD;
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
                : kind == SiteKind.STARWATCH ? new BlockPos[] {new BlockPos(1200, 80, 0), new BlockPos(-900, 80, 900)}
                : kind == SiteKind.LOST_GATE ? new BlockPos[] {new BlockPos(0, 80, 0), new BlockPos(700, 80, -500), new BlockPos(-600, 80, 700),
                        new BlockPos(-900, 80, -800)}
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
        if (kind == SiteKind.GIANT_BONES) {
            // von jeder Art ein Gerippe aus der Nähe des ersten
            StructureStart first = starts.getFirst();
            StructureStart[] byVariant = new StructureStart[4];
            for (int r = 0; r <= 24 && java.util.Arrays.stream(byVariant).anyMatch(java.util.Objects::isNull); r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                            continue;
                        }
                        ChunkPos c = new ChunkPos(first.getChunkPos().x() + dx, first.getChunkPos().z() + dz);
                        StructureStart s = level.getChunk(c.x(), c.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
                        if (s != null && s.isValid()) {
                            int variant = ((SitePiece) s.getPieces().getFirst()).data(0);
                            if (byVariant[variant] == null) {
                                byVariant[variant] = s;
                            }
                        }
                    }
                }
            }
            starts.clear();
            for (StructureStart s : byVariant) {
                if (s != null) {
                    starts.add(s);
                }
            }
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
            case GIANT_BONES -> {
                switch (first.data(0)) {
                    case 0 -> {
                        add(views, first, -19, 10, 13, 0, 4, 2);
                        add(views, first, 1.5, 1.5, -15, 0, 5, 3);
                    }
                    case 1 -> {
                        add(views, first, -8, 5, 15, 0, 3.5, 0);
                        add(views, first, 11, 7, -14, 0, 4, -2);
                        add(views, first, 0.5, 1, -11, 0, 2, 0);
                    }
                    case 2 -> {
                        add(views, first, -17, 9, -4, 0, 1, 2);
                        add(views, first, 6, 3, 19, 0, 1.5, 12);
                    }
                    default -> {
                        add(views, first, -11, 5, 13, 0, 6, 2);
                        add(views, first, 10, 3, -8, 0, 7, 2);
                    }
                }
            }
            case STARWATCH -> {
                add(views, first, -30, 22, 34, 0, 14, -6);
                add(views, first, 0.5, 3, 22, 0.5, 6, -6);
                add(views, first, 34, 30, -6, 0, 18, -10);
                add(views, first, -14, 40, -24, 0, 33, -11);
                add(views, first, 9, 44, 6, 0, 38, -9);
            }
            case LOST_GATE -> {
                add(views, first, -12, 7, 16, 0, 4, 0);
                add(views, first, 0.5, 3, 13, 0.5, 6, 0);
                add(views, first, 15, 5, 3, 3, 2, 3);
                add(views, first, 0.5, 4, -9, 0.5, 5, 6);
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
            case SUNKEN_TEMPLE -> {
                // das Tor steht quer zur Richtung "heraus" (Achse X: Blick von Süden, Achse Z: von Osten)
                boolean alongZ = first.data(0) == 1;
                double ox = alongZ ? 1 : 0;
                double oz = alongZ ? 0 : 1;
                add(views, first, -24, 17, -22, 0, 6, 0);
                add(views, first, ox * 11 + 0.5, 4, oz * 11 + 0.5, 0.5, 6, 0.5);
                add(views, first, ox * 5 + 0.5, 6, oz * 5 + 0.5, 0.5, 6, 0.5);
                add(views, first, 5, 4, -5, -3, 14, 3);
                add(views, first, 18, 4, 6, 0, 5, 0);
            }
            case TIDE_SHRINE -> {
                add(views, first, 8, 5, 8, 0, 2, 0);
                add(views, first, -10, 3, 3, 0, 3, 0);
                add(views, first, 1, -3, -1, 0, -3, 0.5);
            }
        }
    }

    /**
     * Werkstatt ({@code worldsite_lab_bones}, {@code worldsite_lab_gate}): eine ausgehöhlte Halle im Nether (Seelensandtal),
     * darin die Muster nebeneinander – so sieht man die Formen ohne Gelände davor.
     */
    static void lab(Deque<Step> steps, String what) {
        if (what.equals("starwatch")) {
            labEnd(steps);
            return;
        }
        if (what.equals("heads")) {
            labHeads(steps);
            return;
        }
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("gamemode spectator"));
        List<double[]> views = new ArrayList<>();
        int[] state = {0};
        steps.add(Step.server(level -> {
            ServerLevel nether = level.getServer().getLevel(net.minecraft.world.level.Level.NETHER);
            int x0 = -44;
            int x1 = 44;
            int z0 = -30;
            int z1 = 30;
            int floor = 70;
            for (int cx = (x0 >> 4) - 1; cx <= (x1 >> 4) + 1; cx++) {
                for (int cz = (z0 >> 4) - 1; cz <= (z1 >> 4) + 1; cz++) {
                    nether.getChunk(cx, cz, ChunkStatus.FULL, true);
                }
            }
            boolean gate = what.equals("gate");
            BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
            for (int x = x0 - 1; x <= x1 + 1; x++) {
                for (int z = z0 - 1; z <= z1 + 1; z++) {
                    for (int y = floor - 12; y <= floor + 26; y++) {
                        pos.set(x, y, z);
                        boolean wall = x < x0 || x > x1 || z < z0 || z > z1 || y == floor + 26;
                        net.minecraft.world.level.block.state.BlockState state2;
                        if (wall) {
                            state2 = net.minecraft.world.level.block.Blocks.NETHERRACK.defaultBlockState();
                        } else if (y >= floor) {
                            state2 = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
                        } else if (gate && x > 0 && y >= floor - 4) {
                            state2 = net.minecraft.world.level.block.Blocks.LAVA.defaultBlockState();
                        } else if (y >= floor - 3) {
                            state2 = gate ? net.minecraft.world.level.block.Blocks.NETHERRACK.defaultBlockState()
                                    : (x * 7 + z * 13) % 5 == 0 ? net.minecraft.world.level.block.Blocks.SOUL_SOIL.defaultBlockState()
                                    : net.minecraft.world.level.block.Blocks.SOUL_SAND.defaultBlockState();
                        } else {
                            state2 = net.minecraft.world.level.block.Blocks.NETHERRACK.defaultBlockState();
                        }
                        nether.setBlock(pos, state2, 2 | 16);
                    }
                }
            }
            var biome = nether.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(gate
                    ? net.minecraft.world.level.biome.Biomes.NETHER_WASTES : net.minecraft.world.level.biome.Biomes.SOUL_SAND_VALLEY);
            // in Stücken: der Befehl ändert höchstens 32768 Blöcke auf einmal
            for (int bx = x0; bx <= x1; bx += 16) {
                for (int bz = z0; bz <= z1; bz += 16) {
                    net.minecraft.server.commands.FillBiomeCommand.fill(nether, new BlockPos(bx, floor - 12, bz),
                            new BlockPos(Math.min(x1, bx + 15), floor + 26, Math.min(z1, bz + 15)), biome)
                            .ifRight(error -> KingdomsAtWar.LOGGER.warn("[Showcase] Biom nicht gesetzt: {}", error.getMessage()));
                }
            }
            BoundingBox all = new BoundingBox(x0, floor - 12, z0, x1, floor + 26, z1);
            List<SitePiece> pieces = new ArrayList<>();
            if (gate) {
                pieces.add(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.lostGate(new BlockPos(-20, floor, 0), false, 0x6A7DL));
                pieces.add(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.lostGate(new BlockPos(20, floor, 0), true, 0x6A7EL));
            } else {
                int[] xs = {-33, -11, 10, 31};
                for (int v = 0; v < 4; v++) {
                    pieces.add(io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.giantBones(new BlockPos(xs[v], floor, 0), v, 0x5EED1L + v));
                }
            }
            for (SitePiece piece : pieces) {
                piece.postProcess(nether, nether.structureManager(), nether.getChunkSource().getGenerator(), nether.getRandom(), all,
                        ChunkPos.containing(piece.origin()), piece.origin());
            }
            if (gate) {
                for (SitePiece piece : pieces) {
                    add(views, piece, -13, 8, 15, 0, 4, 0);
                    add(views, piece, 0.5, 3, 12, 0.5, 6, 0);
                    add(views, piece, 14, 5, -9, 0, 3, 2);
                }
            } else {
                SitePiece ribs = pieces.get(0);
                add(views, ribs, -9, 13, 19, 0, 4, 1);
                add(views, ribs, 9, 6, 2, 0, 4, 2);
                add(views, ribs, 0.5, 1.5, -17, 0, 5, 3);
                SitePiece skull = pieces.get(1);
                add(views, skull, -6, 5, 14, 0, 3.5, 0);
                add(views, skull, 10, 9, -9, 0, 4, -2);
                add(views, skull, 0.5, 1.5, -12, 0, 2.5, 0);
                SitePiece serpent = pieces.get(2);
                add(views, serpent, -16, 10, -2, 0, 1, 2);
                add(views, serpent, 5, 3, 19, 0, 1.5, 12);
                SitePiece claw = pieces.get(3);
                add(views, claw, -8, 5, 12, 0, 6, 2);
                add(views, claw, 9, 3, -8, 0, 7, 2);
            }
            state[0] = 1;
        }));
        steps.add(Step.until(() -> state[0] != 0, 3000));
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = net.minecraft.world.level.Level.NETHER;
        for (int n = 0; n < 12; n++) {
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
            Step shot = Step.screenshot("lab_" + what + "_" + n);
            steps.add((minecraft, ticks) -> slot >= views.size() || shot.run(minecraft, ticks));
        }
    }

    /**
     * Werkstatt der Köpfe ({@code worldsite_lab_heads}): die sechs Köpfe auf Sockeln und an einer Wand, dazu ein
     * Rüstungsständer mit Kopf – hoch über der Oberwelt.
     */
    static void labHeads(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("gamemode spectator"));
        List<double[]> views = new ArrayList<>();
        int[] state = {0};
        steps.add(Step.server(level -> {
            BlockPos o = new BlockPos(0, 200, 0);
            for (int cx = -2; cx <= 2; cx++) {
                for (int cz = -2; cz <= 2; cz++) {
                    level.getChunk(cx, cz, ChunkStatus.FULL, true);
                }
            }
            net.minecraft.world.level.block.Block[] ground = {net.minecraft.world.level.block.Blocks.SKELETON_SKULL,
                    net.minecraft.world.level.block.Blocks.WITHER_SKELETON_SKULL, net.minecraft.world.level.block.Blocks.ZOMBIE_HEAD,
                    net.minecraft.world.level.block.Blocks.CREEPER_HEAD, net.minecraft.world.level.block.Blocks.PIGLIN_HEAD,
                    net.minecraft.world.level.block.Blocks.DRAGON_HEAD};
            net.minecraft.world.level.block.Block[] wall = {net.minecraft.world.level.block.Blocks.SKELETON_WALL_SKULL,
                    net.minecraft.world.level.block.Blocks.WITHER_SKELETON_WALL_SKULL, net.minecraft.world.level.block.Blocks.ZOMBIE_WALL_HEAD,
                    net.minecraft.world.level.block.Blocks.CREEPER_WALL_HEAD, net.minecraft.world.level.block.Blocks.PIGLIN_WALL_HEAD,
                    net.minecraft.world.level.block.Blocks.DRAGON_WALL_HEAD};
            for (int x = -10; x <= 10; x++) {
                for (int z = -6; z <= 4; z++) {
                    level.setBlock(o.offset(x, -1, z), net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE.defaultBlockState(), 2);
                }
                for (int y = 0; y <= 5; y++) {
                    level.setBlock(o.offset(x, y, -4), net.minecraft.world.level.block.Blocks.DARK_OAK_PLANKS.defaultBlockState(), 2);
                }
            }
            for (int i = 0; i < 6; i++) {
                int x = -8 + i * 3 + (i == 5 ? 1 : 0);
                level.setBlock(o.offset(x, 0, 0), net.minecraft.world.level.block.Blocks.POLISHED_DEEPSLATE_WALL.defaultBlockState(), 2);
                level.setBlock(o.offset(x, 1, 0), ground[i].defaultBlockState().setValue(net.minecraft.world.level.block.SkullBlock.ROTATION, 8), 2);
                level.setBlock(o.offset(x, 3, -3), wall[i].defaultBlockState()
                        .setValue(net.minecraft.world.level.block.WallSkullBlock.FACING, net.minecraft.core.Direction.SOUTH), 2);
            }
            var stand = net.minecraft.world.entity.EntityTypes.ARMOR_STAND.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (stand != null) {
                stand.snapTo(o.getX() - 9.5, o.getY(), o.getZ() + 2.5, 160.0f, 0.0f);
                stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                        new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.CREEPER_HEAD));
                level.addFreshEntity(stand);
            }
            var frame = new net.minecraft.world.entity.decoration.ItemFrame(level, o.offset(6, 1, -3), net.minecraft.core.Direction.SOUTH);
            frame.setItem(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.PIGLIN_HEAD), false);
            level.addFreshEntity(frame);
            views.add(new double[] {o.getX() + 0.5, o.getY() + 1.5, o.getZ() + 9.5, o.getX() + 0.5, o.getY() + 1.8, o.getZ()});
            for (int i = 0; i < 6; i++) {
                double x = o.getX() - 8 + i * 3 + (i == 5 ? 1 : 0) + 0.5;
                double d = i == 5 ? 3.6 : 2.1;
                views.add(new double[] {x + (i == 5 ? 1.6 : 0.9), o.getY() + (i == 5 ? 0.9 : 0.6), o.getZ() + 0.5 + d, x,
                        o.getY() + (i == 5 ? 1.5 : 1.35), o.getZ() + 0.5});
            }
            views.add(new double[] {o.getX() - 7.0, o.getY() + 1.0, o.getZ() + 5.0, o.getX() - 9.5, o.getY() + 1.6, o.getZ() + 2.5});
            views.add(new double[] {o.getX() + 6.5, o.getY() + 0.2, o.getZ() - 0.6, o.getX() + 6.5, o.getY() + 1.5, o.getZ() - 3});
            state[0] = 1;
        }));
        steps.add(Step.until(() -> state[0] != 0, 3000));
        for (int n = 0; n < 9; n++) {
            int slot = n;
            steps.add(Step.once(minecraft -> {
                if (slot < views.size()) {
                    double[] v = views.get(slot);
                    double lx = v[3] - v[0];
                    double ly = v[4] - (v[1] + 1.62);
                    double lz = v[5] - v[2];
                    float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
                    float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", v[0], v[1], v[2], yaw, pitch));
                }
            }));
            steps.add((minecraft, ticks) -> slot >= views.size() || ticks > (slot == 0 ? 200 : 40));
            Step shot = Step.screenshot("lab_heads_" + n);
            steps.add((minecraft, ticks) -> slot >= views.size() || shot.run(minecraft, ticks));
        }
    }

    /** Werkstatt im End ({@code worldsite_lab_starwatch}): die Sternwarte frei im Leeren zwischen den Inseln. */
    static void labEnd(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("gamemode spectator"));
        List<double[]> views = new ArrayList<>();
        int[] state = {0};
        steps.add(Step.server(level -> {
            ServerLevel end = level.getServer().getLevel(net.minecraft.world.level.Level.END);
            BlockPos origin = new BlockPos(0, 70, 420);
            for (int cx = -3; cx <= 3; cx++) {
                for (int cz = (origin.getZ() >> 4) - 3; cz <= (origin.getZ() >> 4) + 3; cz++) {
                    end.getChunk(cx, cz, ChunkStatus.FULL, true);
                }
            }
            SitePiece piece = io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples.starwatch(origin, 0x57A2L);
            BoundingBox all = piece.getBoundingBox();
            piece.postProcess(end, end.structureManager(), end.getChunkSource().getGenerator(), end.getRandom(), all,
                    ChunkPos.containing(piece.origin()), piece.origin());
            add(views, piece, -30, 22, 34, 0, 14, -6);
            add(views, piece, 0.5, 3, 22, 0.5, 6, -6);
            add(views, piece, 34, 30, -6, 0, 18, -10);
            add(views, piece, -14, 40, -24, 0, 33, -11);
            add(views, piece, 3, 1, -13, -2, 3, -8);
            add(views, piece, 3, 9, -7, -2, 10, -13);
            add(views, piece, -2, 17, -14, 1, 20, -12);
            add(views, piece, 2, 25, -9, 0, 27, -16);
            add(views, piece, 9, 44, 6, 0, 38, -9);
            add(views, piece, -9, 18, -7, -16, 19, -11);
            add(views, piece, 0.5, 1.5, 12, 0.5, 2, 2);
            state[0] = 1;
        }));
        steps.add(Step.until(() -> state[0] != 0, 3000));
        net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level> dim = net.minecraft.world.level.Level.END;
        for (int n = 0; n < 12; n++) {
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
            Step shot = Step.screenshot("lab_starwatch_" + n);
            steps.add((minecraft, ticks) -> slot >= views.size() || shot.run(minecraft, ticks));
        }
    }

    private static boolean nether(SiteKind kind) {
        return kind == SiteKind.ASH_FORGE || kind == SiteKind.HORTFESTE || kind == SiteKind.GIANT_BONES || kind == SiteKind.LOST_GATE;
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
