package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.fae.FaeBiomes;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGateState;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Nur zur Prüfung des Feenreichs: Bilder aus jedem Biom (die Kamera steht schräg über dem Land – nach Westen in die
 * Abendröte oder nach Osten zum Mond), eine Karte von oben und das Feenportal in der Oberwelt samt seinem Gegenstück.
 */
final class FaeShowcase {
    private FaeShowcase() {}

    private record View(String name, ResourceKey<Biome> biome, double distance, double height, float turn) {}

    private static final List<View> VIEWS = List.of(
            new View("fae_meadows", FaeBiomes.FAE_MEADOWS, 26, 10, 5),
            new View("fae_glimmerwood", FaeBiomes.GLIMMERWOOD, 22, 12, 25),
            new View("fae_mushrooms", FaeBiomes.MUSHROOM_GROVE, 24, 11, 200),
            new View("fae_lakes", FaeBiomes.MOONLIT_LAKES, 30, 13, 340),
            new View("fae_thornwild", FaeBiomes.THORNWILD, 22, 11, 160),
            new View("fae_overview", FaeBiomes.FAE_MEADOWS, 80, 46, 15),
            new View("fae_moonside", FaeBiomes.GLIMMERWOOD, 34, 16, 190));

    static void views(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        for (View view : VIEWS) {
            if (!only.isEmpty() && !view.name().contains(only)) {
                continue;
            }
            double[] cam = new double[5];
            boolean[] ready = new boolean[1];
            steps.add(Step.server(overworld -> {
                ServerLevel level = overworld.getServer().getLevel(FaeGate.FAE_REALM);
                if (level == null) {
                    ready[0] = true;
                    return;
                }
                var found = level.findClosestBiome3d(h -> h.is(view.biome()), new BlockPos(Math.floorMod(view.name().hashCode(), 900), 70, 0),
                        4000, 32, 64);
                BlockPos target = found != null ? found.getFirst() : new BlockPos(0, 70, 0);
                var generator = level.getChunkSource().getGenerator();
                var random = level.getChunkSource().randomState();
                BlockPos at = null;
                search:
                for (int r = 0; r <= 400; r += 8) {
                    int count = r == 0 ? 1 : (int) (2 * Math.PI * r / 8);
                    for (int i = 0; i < count; i++) {
                        double ang = 2 * Math.PI * i / count;
                        int x = target.getX() + (int) (Math.cos(ang) * r);
                        int z = target.getZ() + (int) (Math.sin(ang) * r);
                        int h = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                        boolean wet = view.biome() == FaeBiomes.MOONLIT_LAKES;
                        if ((wet ? h < 60 : h > 63) && level.getBiome(new BlockPos(x, h, z)).is(view.biome())) {
                            at = new BlockPos(x, Math.max(h, 62), z);
                            break search;
                        }
                    }
                }
                if (at == null) {
                    at = target;
                }
                double a = Math.toRadians(view.turn());
                cam[0] = at.getX() + Math.cos(a) * view.distance();
                cam[2] = at.getZ() + Math.sin(a) * view.distance();
                // über Bäumen und Pilzen bleiben (die fertigen Chunks zählen, nicht nur das Gelände)
                int cx = (int) Math.floor(cam[0]);
                int cz = (int) Math.floor(cam[2]);
                level.getChunk(cx >> 4, cz >> 4);
                int below = level.getHeight(Heightmap.Types.MOTION_BLOCKING, cx, cz);
                for (int dx = -2; dx <= 2; dx += 2) {
                    for (int dz = -2; dz <= 2; dz += 2) {
                        level.getChunk((cx + dx) >> 4, (cz + dz) >> 4);
                        below = Math.max(below, level.getHeight(Heightmap.Types.MOTION_BLOCKING, cx + dx, cz + dz));
                    }
                }
                cam[1] = Math.max(at.getY() + view.height(), below + 3);
                double dx = at.getX() - cam[0];
                double dz = at.getZ() - cam[2];
                cam[3] = -Math.toDegrees(Math.atan2(dx, dz));
                cam[4] = Math.toDegrees(Math.atan2(cam[1] - at.getY() - 2, Math.hypot(dx, dz)));
                ready[0] = true;
            }));
            steps.add(Step.until(() -> ready[0], 6000));
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:fae_realm run tp @s %.1f %.1f %.1f %.1f %.1f", cam[0], cam[1], cam[2], cam[3], cam[4]))));
            steps.add(arrived(cam));
            steps.add(Step.waitTicks(40));
            steps.add(Step.waitForChunks(600));
            steps.add(Step.waitTicks(60));
            steps.add(Step.screenshot(view.name()));
        }
    }

    /** Karte des Feenreichs von oben (Biomfarbe, nach Höhe schattiert, Wasser blau) als Bild im Spielordner. */
    static void map(Deque<Step> steps) {
        boolean[] done = new boolean[1];
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(FaeGate.FAE_REALM);
            if (level == null) {
                done[0] = true;
                return;
            }
            var generator = level.getChunkSource().getGenerator();
            var random = level.getChunkSource().randomState();
            int size = 512;
            int step = 4;
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            var resolver = generator.getBiomeSource().createUncachedResolver(random);
            int[] counts = new int[6];
            for (int px = 0; px < size; px++) {
                for (int pz = 0; pz < size; pz++) {
                    int x = (px - size / 2) * step;
                    int z = (pz - size / 2) * step;
                    int h = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                    var biome = resolver.getNoiseBiome(x >> 2, Math.max(h, 62) >> 2, z >> 2);
                    int index = biome.is(FaeBiomes.FAE_MEADOWS) ? 0 : biome.is(FaeBiomes.GLIMMERWOOD) ? 1 : biome.is(FaeBiomes.MUSHROOM_GROVE) ? 2
                            : biome.is(FaeBiomes.MOONLIT_LAKES) ? 3 : biome.is(FaeBiomes.THORNWILD) ? 4 : 5;
                    counts[index]++;
                    int base = switch (index) {
                        case 0 -> 0x9CE07A;
                        case 1 -> 0xF08CC0;
                        case 2 -> 0x9A70E0;
                        case 3 -> 0x6ACAD8;
                        case 4 -> 0x5A3A6A;
                        default -> 0x808080;
                    };
                    int rgb;
                    if (h < 62) {
                        float depth = Math.min(1.0f, (62 - h) / 14.0f);
                        rgb = mix(0x6AB4F0, 0x203A90, depth);
                    } else {
                        float shade = 0.55f + 0.45f * Math.min(1.0f, Math.max(0.0f, (h - 55) / 50.0f));
                        rgb = mix(0x000000, base, shade);
                    }
                    img.setRGB(px, pz, rgb);
                }
            }
            try {
                javax.imageio.ImageIO.write(img, "png", new java.io.File("fae_map.png"));
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
            int total = size * size;
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info(
                    "[Showcase] Feenreich-Karte: Wiesen {} %, Glimmerwald {} %, Pilzhain {} %, Mondseen {} %, Dornenwildnis {} %",
                    counts[0] * 100 / total, counts[1] * 100 / total, counts[2] * 100 / total, counts[3] * 100 / total,
                    counts[4] * 100 / total);
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

    /** Ein Rahmen aus Mondsteinziegeln in der Oberwelt, mit Feenstaub entzündet – und das Gegenstück im Feenreich. */
    static void portal(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in minecraft:overworld run tp @s 30001.5 %d 7.5 180 8", Showcase.ground + 3))));
        steps.add(Step.waitTicks(10));
        steps.add(Step.waitForChunks(400));
        int[] arrival = new int[4];
        boolean[] ready = new boolean[1];
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Showcase.fill(level, new BlockPos(29990, g - 1, -10), new BlockPos(30012, g - 1, 12), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(29990, g, -10), new BlockPos(30012, g + 14, 12), Blocks.AIR.defaultBlockState());
            // ein Rahmen 4 × 5 (innen 2 × 3), die Ecken gemeißelt, davor ein paar Blumen und Moos
            BlockState frame = FaeBlocks.MOONSTONE_BRICKS.base().get().defaultBlockState();
            BlockState corner = FaeBlocks.CHISELED_MOONSTONE.get().defaultBlockState();
            for (int w = -1; w <= 2; w++) {
                for (int h = 0; h <= 4; h++) {
                    boolean edge = w == -1 || w == 2 || h == 0 || h == 4;
                    if (edge) {
                        boolean c = (w == -1 || w == 2) && (h == 0 || h == 4);
                        level.setBlockAndUpdate(new BlockPos(30000 + w, g - 1 + h, 0), c ? corner : frame);
                    }
                }
            }
            for (int[] p : new int[][] {{-2, 2}, {3, 2}, {-1, 3}, {2, 4}, {0, 5}, {-3, 4}}) {
                level.setBlockAndUpdate(new BlockPos(30000 + p[0], g, p[1]), FaeBlocks.MOONBELL.get().defaultBlockState());
            }
            for (int[] p : new int[][] {{1, 3}, {-2, 5}, {3, 5}}) {
                level.setBlockAndUpdate(new BlockPos(30000 + p[0], g, p[1]), FaeBlocks.FAE_ROSE.get().defaultBlockState());
            }
            FaeGate.tryLight(level, new BlockPos(30000, g - 1, 0), Direction.UP);
            // das Gegenstück im Feenreich schon jetzt bauen (wie bei der ersten Reise)
            ServerLevel fae = level.getServer().getLevel(FaeGate.FAE_REALM);
            if (fae != null) {
                FaeGateState state = FaeGateState.of(level);
                FaeGateState.Gate gate = state.nearest(FaeGate.FAE_REALM, new BlockPos(30000, g, 0), 48);
                if (gate == null) {
                    gate = FaeGate.build(fae, new BlockPos(30000, g, 0), Direction.Axis.X);
                    state.add(gate);
                    state.save(level);
                }
                arrival[0] = gate.corner().getX();
                arrival[1] = gate.corner().getY();
                arrival[2] = gate.corner().getZ();
            }
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("fae_portal_overworld"));
        double[] cam = new double[5];
        steps.add(Step.once(minecraft -> {
            cam[0] = arrival[0] + 1.0 + 4.5;
            cam[1] = arrival[1] + 3.0;
            cam[2] = arrival[2] + 7.5;
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:fae_realm run tp @s %.1f %.1f %.1f %.1f %.1f", cam[0], cam[1], cam[2], 148.0, 12.0));
        }));
        steps.add(arrived(cam));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("fae_portal_realm"));
    }

    /** Wartet, bis der Spieler im Feenreich an der Kamerastelle steht (der Wechsel der Dimension dauert einen Moment). */
    static Step arrived(double[] cam) {
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.player != null && minecraft.level.dimension() == FaeGate.FAE_REALM
                    && minecraft.player.distanceToSqr(cam[0], cam[1], cam[2]) < 4.0;
        }, 1200);
    }

    // =====================================================================================
    // Die Wesen des Feenreichs
    // =====================================================================================

    private static final String STAGE_TAG = "kaw_fae_stage";

    static void creatures(Deque<Step> steps) {
        double[] c = new double[3];
        boolean[] ready = new boolean[1];
        int[] wight = new int[1];
        steps.add(Step.hud(false));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(FaeGate.FAE_REALM);
            if (level == null) {
                ready[0] = true;
                return;
            }
            var found = level.findClosestBiome3d(h -> h.is(FaeBiomes.FAE_MEADOWS), new BlockPos(300, 70, 300), 3000, 32, 64);
            BlockPos target = found != null ? found.getFirst() : new BlockPos(300, 70, 300);
            int x0 = target.getX();
            int z0 = target.getZ();
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.setChunkForced((x0 >> 4) + dx, (z0 >> 4) + dz, true);
                }
            }
            // eine schwebende Lichtung hoch über den Bäumen (so stört nichts im Bild)
            int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x0, z0) + 24;
            BlockState grass = FaeBlocks.FAE_GRASS.get().defaultBlockState();
            BlockState soil = FaeBlocks.FAE_SOIL.get().defaultBlockState();
            BlockState air = Blocks.AIR.defaultBlockState();
            var random = net.minecraft.util.RandomSource.create(63);
            // eine ebene Lichtung: Gras auf Erde, darüber frei; hinten Blumen, ein Glimmerbaum und ein Riesenpilz
            for (int dx = -16; dx <= 16; dx++) {
                for (int dz = -12; dz <= 14; dz++) {
                    BlockPos base = new BlockPos(x0 + dx, y - 1, z0 + dz);
                    double edge = Math.sqrt(dx * dx / 289.0 + (dz - 1) * (dz - 1) / 196.0);
                    if (edge > 1.0) {
                        continue;
                    }
                    level.setBlock(base, grass, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    int depth = 1 + (int) ((1.0 - edge) * 7) + random.nextInt(2);
                    for (int d = 1; d <= depth; d++) {
                        level.setBlock(base.below(d), d > 3 ? FaeBlocks.FAESTONE.base().get().defaultBlockState() : soil,
                                net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                    for (int h = 0; h <= 14; h++) {
                        level.setBlock(base.above(1 + h), air, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                }
            }
            BlockState[] flowers = {FaeBlocks.MOONBELL.get().defaultBlockState(), FaeBlocks.FAE_ROSE.get().defaultBlockState(),
                    FaeBlocks.FAE_SPROUTS.get().defaultBlockState(), FaeBlocks.LANTERN_MUSHROOM.get().defaultBlockState()};
            for (int i = 0; i < 70; i++) {
                int dx = random.nextInt(31) - 15;
                int dz = random.nextInt(25) - 11;
                if (dz < 3 && dz > -10 && Math.abs(dx) < 12 || dx * dx / 289.0 + (dz - 1) * (dz - 1) / 196.0 > 0.9) {
                    continue;
                }
                level.setBlock(new BlockPos(x0 + dx, y, z0 + dz), flowers[random.nextInt(flowers.length)],
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
            var placer = level.getChunkSource().getGenerator();
            new io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature(
                    io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature.Kind.SLENDER,
                    io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature.Palette.ROSE)
                    .place(level, placer, random, new BlockPos(x0 - 9, y, z0 + 9));
            new io.github.sirschrolli.kingdomsatwar.fae.feature.GiantMushroomFeature(
                    io.github.sirschrolli.kingdomsatwar.fae.feature.GiantMushroomFeature.Kind.VIOLET, false)
                    .place(level, placer, random, new BlockPos(x0 + 10, y, z0 + 9));
            new io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature(
                    io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature.Kind.SLENDER,
                    io.github.sirschrolli.kingdomsatwar.fae.feature.GlimmerTreeFeature.Palette.AZURE)
                    .place(level, placer, random, new BlockPos(x0 + 1, y, z0 + 13));
            double lz = z0 + 0.5;
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.FAIRY.get(), x0 - 7.5, y + 1.4, lz, 160, m -> {
                m.setNoGravity(true);
                m.setVariant(0);
            });
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.FAIRY.get(), x0 - 5.2, y + 2.3, lz + 1.0, 200, m -> {
                m.setNoGravity(true);
                m.setVariant(1);
            });
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.FAIRY.get(), x0 - 3.0, y + 1.2, lz - 0.8, 185, m -> {
                m.setNoGravity(true);
                m.setVariant(2);
            });
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.FAE_WISP.get(), x0 - 0.5, y + 1.9, lz, 180,
                    m -> m.setNoGravity(true));
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MUSHROOMLING.get(), x0 + 2.0, y, lz - 0.5, 195,
                    m -> m.setVariant(0));
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MUSHROOMLING.get(), x0 + 3.6, y, lz + 0.6, 170,
                    m -> m.setVariant(1));
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MUSHROOMLING.get(), x0 + 5.0, y, lz - 0.2, 150, m -> {
                m.setVariant(2);
                m.setHiding(true);
            });
            var w = creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.THORN_WIGHT.get(), x0 + 9.0, y, lz + 0.5, 200,
                    m -> { });
            wight[0] = w.getId();
            c[0] = x0;
            c[1] = y;
            c[2] = lz;
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        double[] cam = new double[3];
        steps.add(at(c, cam, 0.8, 1.2, -8.0, 0.8, 1.4, 0.0));
        steps.add(arrived(cam));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("fae_creatures_overview"));
        steps.add(at(c, cam, -5.2, 0.6, -2.6, -5.2, 1.9, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("fae_creatures_fairies"));
        steps.add(at(c, cam, 3.2, -0.6, -2.7, 3.4, 0.4, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("fae_creatures_mushroomlings"));
        steps.add(at(c, cam, 7.8, 0.5, -3.6, 9.0, 1.6, 0.5));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(FaeGate.FAE_REALM);
            if (level != null && level.getEntity(wight[0]) instanceof net.minecraft.world.entity.Entity e) {
                level.broadcastEntityEvent(e, (byte) 71);
            }
        }));
        steps.add(Step.waitTicks(9));
        steps.add(Step.screenshot("fae_creatures_wight"));
        steps.add(at(c, cam, -1.4, 0.6, -2.2, -0.5, 1.9, 0.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("fae_creatures_wisp"));
    }

    private static <T extends net.minecraft.world.entity.Mob> T creature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type,
            double x, double y, double z, float yaw, java.util.function.Consumer<T> setup) {
        T mob = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(x, y, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setup.accept(mob);
        level.addFreshEntity(mob);
        return mob;
    }

    /** Kamera an c + (dx, dy, dz) (Augenhöhe), Blick auf c + (tx, ty, tz) – im Feenreich. */
    private static Step at(double[] c, double[] cam, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            cam[0] = c[0] + dx;
            cam[1] = c[1] + dy;
            cam[2] = c[2] + dz;
            double lx = tx - dx;
            double ly = ty - (dy + 1.62);
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:fae_realm run tp @s %.2f %.2f %.2f %.1f %.1f", cam[0], cam[1], cam[2], yaw, pitch));
        });
    }
}
