package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidBiomes;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidLanding;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;

/**
 * Nur zur Prüfung der Leere: Bilder aus jedem Biom der Leere (die Kamera steht schräg über einem Bruchstück), dazu ein
 * frisch geöffneter Riss in der Oberwelt und die Ankerinsel auf der anderen Seite.
 */
final class VoidShowcase {
    private VoidShowcase() {}

    private record View(String name, ResourceKey<Biome> biome, double distance, double height, float turn) {}

    private static final List<View> VIEWS = List.of(
            new View("void_crystal_forest", VoidBiomes.CRYSTAL_FOREST, 22, 10, 0),
            new View("void_ruins", VoidBiomes.WEIGHTLESS_RUINS, 20, 9, 30),
            new View("void_stardust", VoidBiomes.STARDUST_FIELDS, 26, 12, 60),
            new View("void_shards", VoidBiomes.SHARD_CLIFFS, 30, 12, 90),
            new View("void_remnants", VoidBiomes.WORLD_REMNANTS, 22, 9, 120),
            new View("void_expanse", VoidBiomes.EXPANSE, 40, 20, 150),
            new View("void_overview", VoidBiomes.CRYSTAL_FOREST, 70, 40, 200));

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
                ServerLevel level = overworld.getServer().getLevel(VoidGate.THE_VOID);
                if (level == null) {
                    return;
                }
                var found = level.findClosestBiome3d(h -> h.is(view.biome()), BlockPos.ZERO.offset(view.name().hashCode() % 900, 100, 0), 3000,
                        32, 64);
                BlockPos target = found != null ? found.getFirst() : new BlockPos(0, 100, 0);
                // das nächste Bruchstück dieses Bioms suchen (ringförmig, nur echte Oberseiten)
                var generator = level.getChunkSource().getGenerator();
                var random = level.getChunkSource().randomState();
                BlockPos at = null;
                search:
                for (int r = 0; r <= 400; r += 8) {
                    int steps2 = r == 0 ? 1 : (int) (2 * Math.PI * r / 8);
                    for (int i = 0; i < steps2; i++) {
                        double ang = 2 * Math.PI * i / steps2;
                        int x = target.getX() + (int) (Math.cos(ang) * r);
                        int z = target.getZ() + (int) (Math.sin(ang) * r);
                        int h = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                        if (h > 20 && h < 240 && level.getBiome(new BlockPos(x, h, z)).is(view.biome())) {
                            int h2 = generator.getBaseHeight(x + 6, z + 6, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                            if (Math.abs(h2 - h) < 6) {
                                at = new BlockPos(x, h, z);
                                break search;
                            }
                        }
                    }
                }
                if (at == null) {
                    at = target;
                }
                double a = Math.toRadians(view.turn());
                cam[0] = at.getX() + Math.cos(a) * view.distance();
                cam[1] = at.getY() + view.height();
                cam[2] = at.getZ() + Math.sin(a) * view.distance();
                double dx = at.getX() - cam[0];
                double dz = at.getZ() - cam[2];
                cam[3] = -Math.toDegrees(Math.atan2(dx, dz));
                cam[4] = Math.toDegrees(Math.atan2(view.height() + 2, Math.hypot(dx, dz)));
                ready[0] = true;
            }));
            // die Suche läuft auf dem Server und kann dauern – erst danach springen, und erst im Bild, wenn die Leere da ist
            steps.add(Step.until(() -> ready[0], 6000));
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:the_void run tp @s %.1f %.1f %.1f %.1f %.1f", cam[0], cam[1], cam[2], cam[3], cam[4]))));
            steps.add(arrived(cam));
            steps.add(Step.waitTicks(40));
            steps.add(Step.waitForChunks(600));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot(view.name()));
        }
    }

    /** Karte der Leere von oben (Höhe der obersten Oberfläche, Biomfarbe) als Bild im Spielordner. */
    static void map(Deque<Step> steps) {
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(VoidGate.THE_VOID);
            if (level == null) {
                return;
            }
            var generator = level.getChunkSource().getGenerator();
            var random = level.getChunkSource().randomState();
            int size = 512;
            int step = 4;
            java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(size, size, java.awt.image.BufferedImage.TYPE_INT_RGB);
            int land = 0;
            var resolver = generator.getBiomeSource().createUncachedResolver(random);
            for (int px = 0; px < size; px++) {
                for (int pz = 0; pz < size; pz++) {
                    int x = (px - size / 2) * step;
                    int z = (pz - size / 2) * step;
                    int h = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, random);
                    int rgb = 0x07061A;
                    if (h > -40) {
                        land++;
                        var biome = resolver.getNoiseBiome(x >> 2, h >> 2, z >> 2);
                        int base = biome.is(VoidBiomes.CRYSTAL_FOREST) ? 0x9A70FF : biome.is(VoidBiomes.WEIGHTLESS_RUINS) ? 0xE8E0F0
                                : biome.is(VoidBiomes.STARDUST_FIELDS) ? 0x8AA8F0 : biome.is(VoidBiomes.SHARD_CLIFFS) ? 0xC060E0
                                : biome.is(VoidBiomes.WORLD_REMNANTS) ? 0x7AB060 : 0x5A5A70;
                        float shade = 0.35f + 0.65f * Math.min(1.0f, Math.max(0.0f, h / 250.0f));
                        int r = (int) (((base >> 16) & 255) * shade);
                        int g = (int) (((base >> 8) & 255) * shade);
                        int b2 = (int) ((base & 255) * shade);
                        rgb = (r << 16) | (g << 8) | b2;
                    }
                    img.setRGB(px, pz, rgb);
                }
            }
            try {
                javax.imageio.ImageIO.write(img, "png", new java.io.File("void_map.png"));
            } catch (java.io.IOException e) {
                throw new IllegalStateException(e);
            }
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Leere-Karte: {} % Land", land * 100 / (size * size));
        }));
        steps.add(Step.waitTicks(40));
    }

    /** Ein Riss in der Oberwelt und die Ankerinsel in der Leere. */
    static void rift(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in minecraft:overworld run tp @s 21000.5 %d 14.5 180 -4", Showcase.ground + 4))));
        steps.add(Step.waitTicks(10));
        steps.add(Step.waitForChunks(400));
        int[] anchor = new int[3];
        boolean[] ready = new boolean[1];
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Showcase.fill(level, new BlockPos(20990, g - 1, -10), new BlockPos(21010, g - 1, 10), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(20990, g, -10), new BlockPos(21010, g + 14, 10), Blocks.AIR.defaultBlockState());
            VoidGate.build(level, new BlockPos(21000, g, 0), Direction.Axis.X);
            ServerLevel voidLevel = level.getServer().getLevel(VoidGate.THE_VOID);
            if (voidLevel != null) {
                BlockPos bottom = VoidLanding.ensure(voidLevel, new BlockPos(21000, 100, 0), Direction.Axis.X);
                anchor[0] = bottom.getX();
                anchor[1] = bottom.getY();
                anchor[2] = bottom.getZ();
            }
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_rift_overworld"));
        double[] cam = new double[3];
        steps.add(Step.once(minecraft -> {
            cam[0] = anchor[0] + 9.5;
            cam[1] = anchor[1] + 6.0;
            cam[2] = anchor[2] + 12.5;
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:the_void run tp @s %.1f %.1f %.1f %.1f %.1f", cam[0], cam[1], cam[2], 145.0, 18.0));
        }));
        steps.add(arrived(cam));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("void_anchor"));
    }

    /** Wartet, bis der Spieler in der Leere an der Kamerastelle steht (der Wechsel der Dimension dauert einen Moment). */
    static Step arrived(double[] cam) {
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.player != null && minecraft.level.dimension() == VoidGate.THE_VOID
                    && minecraft.player.distanceToSqr(cam[0], cam[1], cam[2]) < 4.0;
        }, 1200);
    }

    // =====================================================================================
    // Die Wesen der Leere
    // =====================================================================================

    private static final String STAGE_TAG = "kaw_void_stage";

    /** Eine schwebende Bühne in der Leere mit allen vier Wesen; Bilder von vorn und aus der Nähe. */
    static void creatures(Deque<Step> steps) {
        double[] c = new double[4];
        boolean[] ready = new boolean[1];
        steps.add(Step.hud(false));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(VoidGate.THE_VOID);
            if (level == null) {
                ready[0] = true;
                return;
            }
            int x0 = 6000;
            int y = 140;
            int z0 = 6000;
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    level.setChunkForced((x0 >> 4) + dx, (z0 >> 4) + dz, true);
                }
            }
            var moss = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_MOSS.get().defaultBlockState();
            var stone = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_STONE.base().get().defaultBlockState();
            var air = Blocks.AIR.defaultBlockState();
            var random = net.minecraft.util.RandomSource.create(77);
            // eine runde, nach unten spitz zulaufende Insel
            for (int dx = -16; dx <= 16; dx++) {
                for (int dz = -12; dz <= 12; dz++) {
                    double r = Math.sqrt(dx * dx / 256.0 + dz * dz / 144.0);
                    for (int dy = -14; dy <= 16; dy++) {
                        BlockPos pos = new BlockPos(x0 + dx, y + dy, z0 + dz);
                        boolean solid = r <= 1.0 && dy < 0 && -dy <= (1.0 - r) * 14 + 1 + random.nextInt(2);
                        level.setBlock(pos, solid ? (dy == -1 ? moss : stone) : air, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                }
            }
            // Kristalle, Blüten und eine gebrochene Säule im Hintergrund
            var cluster = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_CRYSTAL_CLUSTER.get().defaultBlockState();
            var bloom = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.VOID_BLOOM.get().defaultBlockState();
            for (int i = 0; i < 40; i++) {
                int dx = random.nextInt(31) - 15;
                int dz = random.nextInt(23) - 11;
                if (dz < 4 && dz > -6 && Math.abs(dx) < 13 || dx * dx / 256.0 + dz * dz / 144.0 > 0.9) {
                    continue;
                }
                level.setBlock(new BlockPos(x0 + dx, y, z0 + dz), random.nextInt(3) == 0 ? cluster : bloom,
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
            var pillar = io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks.ASTRAL_PILLAR.get().defaultBlockState();
            for (int[] col : new int[][] {{-9, 7, 5}, {-4, 8, 3}, {6, 8, 6}, {11, 6, 2}}) {
                for (int h = 0; h < col[2]; h++) {
                    level.setBlock(new BlockPos(x0 + col[0], y + h, z0 + col[1]), pillar, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
            }
            level.setBlock(new BlockPos(x0 + 6, y + 9, z0 + 8), pillar, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            double lz = z0 + 0.5;
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SHARD_WARDEN.get(), x0 - 7.5, y, lz, 165,
                    m -> m.setDormant(false));
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.VOID_MAW.get(), x0 - 1.5, y + 2.2, lz, 185, m -> {
                m.setNoGravity(true);
                m.setPhase(io.github.sirschrolli.kingdomsatwar.entity.voidrealm.VoidMawEntity.INHALE);
            });
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.DISTORTLING.get(), x0 + 3.5, y, lz, 195, m -> { });
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.STAR_RAY.get(), x0 + 9.0, y + 4.0, lz + 1.5, 215,
                    m -> m.setNoGravity(true));
            creature(level, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SHARD_WARDEN.get(), x0 - 11.5, y, z0 + 6.5, 200,
                    m -> m.setDormant(true));
            c[0] = x0;
            c[1] = y;
            c[2] = lz;
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        double[] cam = new double[3];
        steps.add(at(c, cam, 0.5, 2.6, -11.5, 0.5, 2.2, 0.0));
        steps.add(arrived(cam));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("void_creatures_overview"));
        steps.add(at(c, cam, -5.6, 1.4, -5.4, -7.5, 1.9, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_creatures_warden"));
        steps.add(at(c, cam, -0.6, 0.6, -5.2, -1.5, 3.3, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_creatures_maw"));
        steps.add(at(c, cam, 3.9, 0.4, -3.4, 3.5, 1.6, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_creatures_distortling"));
        steps.add(at(c, cam, 7.6, 2.4, -2.6, 9.0, 4.3, 1.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_creatures_ray"));
        steps.add(at(c, cam, -10.5, 0.8, 2.2, -11.5, 1.4, 6.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_creatures_dormant"));
    }

    // =====================================================================================
    // Der Thron der Alten und der Zerbrochene König
    // =====================================================================================

    /** Der Thron der Alten (von oben, von unten, das Podest), dann das Erwachen des Königs und jeder seiner Angriffe. */
    static void throne(Deque<Step> steps) {
        double[] c = new double[3];
        boolean[] ready = new boolean[1];
        io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity[] king =
                new io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity[1];
        net.minecraft.world.entity.LivingEntity[] dummy = new net.minecraft.world.entity.LivingEntity[1];
        steps.add(Step.hud(false));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(VoidGate.THE_VOID);
            BlockPos center = level == null ? null : io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.center(level);
            if (center == null) {
                ready[0] = true;
                return;
            }
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    level.setChunkForced((center.getX() >> 4) + dx, (center.getZ() >> 4) + dz, true);
                }
            }
            c[0] = center.getX() + 0.5;
            c[1] = center.getY();
            c[2] = center.getZ() + 0.5;
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
        double[] cam = new double[3];
        steps.add(at(c, cam, 30, 15, 42, 0, -1, -3));
        steps.add(arrived(cam));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(100));
        steps.add(Step.screenshot("void_throne_overview"));
        steps.add(at(c, cam, 30, -34, 30, 0, -16, 0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("void_throne_below"));
        steps.add(at(c, cam, 7, 3, -5, 0, 4, -17));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_throne_dais"));
        // der König erwacht; ein unverwundbarer Ritter dient als Ziel
        steps.add(at(c, cam, 5.5, 2.5, 3.5, 0, 5.0, -6.5));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(VoidGate.THE_VOID);
            BlockPos center = io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.center(level);
            if (!(level.getBlockEntity(io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator.seal(center))
                    instanceof io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity seal)) {
                return;
            }
            seal.reset(level);
            king[0] = seal.awaken(level);
            var soldier = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SOLDIERS
                    .get(io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType.KNIGHT).get()
                    .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (soldier != null && king[0] != null) {
                soldier.setNoAi(true);
                soldier.setPermanentlyInvulnerable(true);
                soldier.snapTo(c[0], c[1], c[2] + 5.0, 180.0f, 0.0f);
                level.addFreshEntity(soldier);
                dummy[0] = soldier;
                king[0].forceAttack(level, "", soldier, true);
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("void_king_awaken"));
        steps.add(Step.waitTicks(31));
        steps.add(Step.screenshot("void_king_roar"));
        steps.add(at(c, cam, 4, 2.2, 12, -0.5, 5.2, -0.5));
        steps.add(Step.waitTicks(60));
        String[][] attacks = {{"slam_right", "17", "void_king_slam_raise"}, {"", "6", "void_king_slam"}, {"sweep", "15", "void_king_sweep"},
                {"volley", "19", "void_king_volley"}, {"gravity", "32", "void_king_gravity"}, {"rift", "30", "void_king_rift_tear"},
                {"rift", "41", "void_king_rift_erupt"}, {"quake", "15", "void_king_quake_raise"}, {"quake", "38", "void_king_quake_rings"},
                {"beam", "62", "void_king_beam"}};
        for (String[] attack : attacks) {
            if (!Step.wanted(attack[2]) && !(attack[0].isEmpty() && Step.wanted("void_king_slam_raise"))) {
                continue;
            }
            if (!attack[0].isEmpty()) {
                // die Risse von der Seite (sie laufen auf den Ritter zu und an ihm vorbei), sonst hinter dem Ritter
                steps.add(attack[0].equals("rift") ? at(c, cam, 14, 5.0, 14, 0, 1.5, 7) : at(c, cam, 4, 2.2, 12, -0.5, 5.2, -0.5));
                steps.add(Step.server(overworld -> {
                    if (king[0] != null && dummy[0] != null) {
                        king[0].forceAttack(overworld.getServer().getLevel(VoidGate.THE_VOID), attack[0], dummy[0], true);
                    }
                }));
            }
            steps.add(Step.waitTicks(Integer.parseInt(attack[1])));
            steps.add(Step.screenshot(attack[2]));
            if (!attack[0].equals("slam_right")) {
                steps.add(Step.waitTicks(attack[0].equals("beam") ? 70 : 50));
            }
        }
        steps.add(Step.server(overworld -> {
            if (king[0] != null) {
                ServerLevel level = overworld.getServer().getLevel(VoidGate.THE_VOID);
                king[0].setHealth(1.0f);
                king[0].hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(Step.waitTicks(36));
        steps.add(Step.screenshot("void_king_death"));
        steps.add(Step.waitTicks(60));
        // seine Krone, getragen (3D-Modell): der Spieler selbst trägt sie, von vorn gesehen, dahinter der Thron
        steps.add(Step.command("kill @e[type=item]"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("item replace entity @s armor.head with kingdomsatwar:shattered_crown"));
        steps.add(Step.command("item replace entity @s armor.chest with minecraft:golden_chestplate"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in kingdomsatwar:the_void run tp @s %.2f %.2f %.2f 20 -22", c[0] + 1.5, c[1], c[2] + 1.5))));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(30)));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("void_king_crown"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.fov().set(70);
            minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
        }));
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

    /** Kamera an c + (dx, dy, dz) (Augenhöhe), Blick auf c + (tx, ty, tz) – in der Leere. */
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
                    "execute in kingdomsatwar:the_void run tp @s %.2f %.2f %.2f %.1f %.1f", cam[0], cam[1], cam[2], yaw, pitch));
        });
    }
}
