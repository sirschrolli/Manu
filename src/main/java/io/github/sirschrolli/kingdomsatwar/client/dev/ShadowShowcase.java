package io.github.sirschrolli.kingdomsatwar.client.dev;

import com.mojang.datafixers.util.Pair;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.shadow.CounterGate;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowTerrain;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeResolver;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.RandomState;

/**
 * Nur für die Entwicklung: Szenen in der Schattenwelt (siehe {@link ShowcaseRunner}).
 *
 * <ul>
 *   <li>{@code shadowmap} – Karte der Schattenwelt (Biome und Höhen, ohne Chunks zu erzeugen) als {@code shadow_map.ppm}
 *   <li>{@code shadow} – Rundgang: das Gegentor, dann jedes Biom von oben und vom Boden aus
 *   <li>{@code shadow_<biom>} – nur dieses eine Biom
 * </ul>
 */
final class ShadowShowcase {
    private static final String DIM = "kingdomsatwar:shadow_world";
    private static final Map<ResourceKey<Biome>, Integer> MAP_COLORS = new LinkedHashMap<>();

    static {
        MAP_COLORS.put(ShadowBiomes.SHADOW_FOREST, 0x3A2A6A);
        MAP_COLORS.put(ShadowBiomes.PALE_WASTES, 0xB8B0C8);
        MAP_COLORS.put(ShadowBiomes.CRYSTAL_CHASMS, 0x4A7AD0);
        MAP_COLORS.put(ShadowBiomes.SOUL_MARSH, 0x2E6A5A);
        MAP_COLORS.put(ShadowBiomes.SHADOW_PEAKS, 0x8A8AA0);
        MAP_COLORS.put(ShadowBiomes.GLIMMER_MEADOWS, 0x8A78D8);
        MAP_COLORS.put(ShadowBiomes.ABYSSAL_SEA, 0x141838);
        MAP_COLORS.put(ShadowBiomes.FLOATING_ISLES, 0xF0C8FF);
        MAP_COLORS.put(ShadowBiomes.SHADOW_DEPTHS, 0x201A2A);
    }

    private ShadowShowcase() {}

    static ServerLevel shadow(ServerLevel any) {
        ServerLevel level = any.getServer().getLevel(ShadowGate.SHADOW_WORLD);
        if (level == null) {
            throw new IllegalStateException("Keine Schattenwelt in dieser Welt");
        }
        return level;
    }

    // =====================================================================================
    // Karte
    // =====================================================================================

    /** Karte (Biome und Höhen) von {@code size} Blöcken Kantenlänge um den Ursprung, ein Bildpunkt je {@code step} Blöcke. */
    static void map(Deque<Step> steps, int size, int step) {
        boolean[] done = {false};
        steps.add(Step.server(level -> {
            ServerLevel shadow = shadow(level);
            ChunkGenerator generator = shadow.getChunkSource().getGenerator();
            RandomState random = shadow.getChunkSource().randomState();
            BiomeResolver biomes = generator.getBiomeSource().createUncachedResolver(random);
            int n = size / step;
            var router = ((net.minecraft.world.level.levelgen.NoiseBasedChunkGenerator) generator).generatorSettings().value().noiseRouter();
            for (int k = 0; k < 24; k++) {
                int x = k * 97 - 1100;
                int z = k * 61 - 700;
                int ground = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                StringBuilder line = new StringBuilder(String.format(Locale.ROOT, "x=%d z=%d ground=%d", x, z, ground));
                for (int dy : new int[] {-16, 0, 16, 48}) {
                    int yy = ground + dy;
                    line.append(String.format(Locale.ROOT, " | y%+d D=%.3f N=%.3f", dy, random.sampleBlockValueUncached(router.depth(), x, yy, z),
                            random.sampleBlockValueUncached(router.finalDensity(), x, yy, z)));
                }
                line.append(String.format(Locale.ROOT, " C=%.3f E=%.3f W=%.3f T=%.3f H=%.3f biome=%s",
                        random.sampleBlockValueUncached(router.continents(), x, ground, z),
                        random.sampleBlockValueUncached(router.erosion(), x, ground, z),
                        random.sampleBlockValueUncached(router.ridges(), x, ground, z),
                        random.sampleBlockValueUncached(router.temperature(), x, ground, z),
                        random.sampleBlockValueUncached(router.vegetation(), x, ground, z),
                        biomes.getNoiseBiome(x >> 2, ground >> 2, z >> 2).unwrapKey().map(key -> key.identifier().getPath()).orElse("?")));
                KingdomsAtWar.LOGGER.info("[Showcase] Probe {}", line);
                if (k % 6 == 0) {
                    var column = generator.getBaseColumn(x, z, shadow, random);
                    StringBuilder prof = new StringBuilder();
                    for (int yy = 300; yy >= -64; yy -= 8) {
                        var st = column.getBlock(yy);
                        char ch = st.isAir() ? '.' : !st.getFluidState().isEmpty() ? '~' : '#';
                        float nv = random.sampleBlockValueUncached(router.finalDensity(), x, yy, z);
                        prof.append(String.format(Locale.ROOT, "%d%c%.2f ", yy, ch, nv));
                    }
                    KingdomsAtWar.LOGGER.info("[Showcase] Säule {} {}: {}", x, z, prof);
                }
            }
            if (size <= 0) {
                done[0] = true;
                return;
            }
            byte[] pixels = new byte[n * n * 3];
            Map<ResourceKey<Biome>, Integer> counts = new LinkedHashMap<>();
            long start = System.currentTimeMillis();
            int islands = 0;
            for (int j = 0; j < n; j++) {
                for (int i = 0; i < n; i++) {
                    int x = (i - n / 2) * step;
                    int z = (j - n / 2) * step;
                    int ground = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                    int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, shadow, random);
                    boolean water = ground < ShadowTerrain.SEA_LEVEL && surface <= ShadowTerrain.SEA_LEVEL + 1;
                    boolean island = surface > ShadowTerrain.ISLAND_BOTTOM - 10 && surface - ground > 30;
                    int y = island ? surface - 2 : Math.max(ground, ShadowTerrain.SEA_LEVEL) + 1;
                    Holder<Biome> biome = biomes.getNoiseBiome(x >> 2, (island ? surface - 2 : ground) >> 2, z >> 2);
                    ResourceKey<Biome> key = biome.unwrapKey().orElse(null);
                    counts.merge(key, 1, Integer::sum);
                    int color = MAP_COLORS.getOrDefault(key, 0xFF00FF);
                    double shade;
                    if (island) {
                        islands++;
                        shade = 1.0;
                    } else if (water) {
                        color = mix(0x1A2050, color, 0.3);
                        shade = 0.6 + Math.max(-0.4, (ground - ShadowTerrain.SEA_LEVEL) / 60.0);
                    } else {
                        shade = 0.55 + (y - ShadowTerrain.SEA_LEVEL) / 140.0;
                        // Hangschattierung: Nachbar im Nordwesten
                        int west = generator.getBaseHeight(x - step, z - step, Heightmap.Types.OCEAN_FLOOR_WG, shadow, random);
                        shade += Math.max(-0.25, Math.min(0.25, (ground - west) / 24.0));
                    }
                    int idx = (j * n + i) * 3;
                    pixels[idx] = (byte) clamp((color >> 16 & 0xFF) * shade);
                    pixels[idx + 1] = (byte) clamp((color >> 8 & 0xFF) * shade);
                    pixels[idx + 2] = (byte) clamp((color & 0xFF) * shade);
                }
            }
            Path file = Minecraft.getInstance().gameDirectory.toPath().resolve("shadow_map.ppm");
            try {
                byte[] header = ("P6\n" + n + " " + n + "\n255\n").getBytes(java.nio.charset.StandardCharsets.US_ASCII);
                byte[] all = new byte[header.length + pixels.length];
                System.arraycopy(header, 0, all, 0, header.length);
                System.arraycopy(pixels, 0, all, header.length, pixels.length);
                Files.write(file, all);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
            int total = n * n;
            StringBuilder stats = new StringBuilder();
            counts.forEach((key, count) -> stats.append(String.format(Locale.ROOT, "%n  %-28s %5.1f %%",
                    key == null ? "?" : key.identifier().getPath(), 100.0 * count / total)));
            KingdomsAtWar.LOGGER.info("[Showcase] Schattenkarte {}x{} in {} ms nach {} (Inseln {} %):{}", n, n,
                    System.currentTimeMillis() - start, file, String.format(Locale.ROOT, "%.1f", 100.0 * islands / total), stats);
            done[0] = true;
        }));
        steps.add(Step.until(() -> done[0], 20 * 600));
    }

    private static int mix(int a, int b, double t) {
        int r = (int) ((a >> 16 & 0xFF) * (1 - t) + (b >> 16 & 0xFF) * t);
        int g = (int) ((a >> 8 & 0xFF) * (1 - t) + (b >> 8 & 0xFF) * t);
        int bl = (int) ((a & 0xFF) * (1 - t) + (b & 0xFF) * t);
        return r << 16 | g << 8 | bl;
    }

    private static int clamp(double v) {
        return (int) Math.max(0, Math.min(255, v));
    }

    // =====================================================================================
    // Kreativ-Tab und Schaukasten
    // =====================================================================================

    /** Der Kreativ-Tab der Schattenwelt (zwei Seiten). */
    static void tab(Deque<Step> steps) {
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screen(minecraft -> new net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen(minecraft.player,
                minecraft.player.connection.enabledFeatures(), false)));
        steps.add(Step.waitTicks(5));
        steps.add(Step.once(minecraft -> {
            selectTab(minecraft, io.github.sirschrolli.kingdomsatwar.registry.ModCreativeTabs.SHADOW_WORLD.get());
            Showcase.parkMouse(minecraft);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("tab_shadow"));
        steps.add(Step.once(minecraft -> minecraft.gui.screen().mouseScrolled(100, 100, 0, -30)));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("tab_shadow_2"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.waitTicks(5));
        steps.add(Step.command("gamemode spectator"));
    }

    private static void selectTab(Minecraft minecraft, net.minecraft.world.item.CreativeModeTab tab) {
        if (minecraft.gui.screen() instanceof net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen screen) {
            try {
                var select = net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab",
                        net.minecraft.world.item.CreativeModeTab.class);
                select.setAccessible(true);
                select.invoke(screen, tab);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    /**
     * Schaukasten in der (flachen, hellen) Oberwelt: alle Blöcke der Schattenwelt in Reihen, davor zwei Rüstungsständer
     * in Nachtstahl und Abyssit mit Waffen, dazu ein gewachsener Schattenbaum.
     */
    static void display(Deque<Step> steps, int[] ground) {
        steps.add(Step.server(level -> {
            int y = ground[0];
            List<net.minecraft.world.level.block.Block> blocks = new java.util.ArrayList<>();
            for (var supplier : io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.shadowTab()) {
                if (supplier.get().asItem() instanceof net.minecraft.world.item.BlockItem blockItem) {
                    blocks.add(blockItem.getBlock());
                }
            }
            int perRow = 14;
            for (int x = -3; x <= perRow * 2 + 2; x++) {
                for (int z = -12; z <= (blocks.size() / perRow + 1) * 3 + 2; z++) {
                    level.setBlock(new BlockPos(x, y - 1, z), io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.POLISHED_SHADOW_STONE
                            .base().get().defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
            }
            for (int i = 0; i < blocks.size(); i++) {
                BlockPos pos = new BlockPos((i % perRow) * 2, y, (i / perRow) * 3);
                var block = blocks.get(i);
                var state = block.defaultBlockState();
                if (block instanceof net.minecraft.world.level.block.VegetationBlock || block instanceof
                        io.github.sirschrolli.kingdomsatwar.block.CrystalClusterBlock) {
                    level.setBlock(pos.below(), io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_GRASS.get().defaultBlockState(),
                            net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                }
                level.setBlock(pos, state, net.minecraft.world.level.block.Block.UPDATE_ALL);
            }
            // Rüstungsständer
            standWith(level, new BlockPos(6, y, -6), io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_HELMET.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_CHESTPLATE.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_LEGGINGS.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_BOOTS.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.NIGHTSTEEL_SWORD.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.MOONSILVER_BLADE.get());
            standWith(level, new BlockPos(10, y, -6), io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_HELMET.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_CHESTPLATE.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_LEGGINGS.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_BOOTS.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_SWORD.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowItems.ABYSSITE_PICKAXE.get());
            // ein gewachsener Schattenbaum
            BlockPos tree = new BlockPos(20, y, -8);
            level.setBlock(tree.below(), io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_GRASS.get().defaultBlockState(), 3);
            level.setBlock(tree, io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_SAPLING.get().defaultBlockState(), 3);
            for (int i = 0; i < 6; i++) {
                var state = level.getBlockState(tree);
                if (state.getBlock() instanceof net.minecraft.world.level.block.SaplingBlock sapling) {
                    sapling.advanceTree(level, tree, state, level.getRandom());
                }
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 6000"));
        steps.add(tpOver(ground, 13, 12, -16, 0, 40));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_overview"));
        steps.add(tpOver(ground, 7, 5, -2, 0, 38));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_1"));
        steps.add(tpOver(ground, 21, 5, -2, 0, 38));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_2"));
        steps.add(tpOver(ground, 7, 5, 4, 0, 38));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_3"));
        steps.add(tpOver(ground, 21, 5, 4, 0, 38));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_4"));
        steps.add(tpOver(ground, 8.5, 2.2, -10.5, 0, 10));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_armor"));
        steps.add(tpOver(ground, 20.5, 6, -18, 0, 15));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("shadow_display_tree"));
        steps.add(Step.hud(true));
    }

    private static void standWith(ServerLevel level, BlockPos pos, net.minecraft.world.item.Item helmet, net.minecraft.world.item.Item chest,
            net.minecraft.world.item.Item legs, net.minecraft.world.item.Item boots, net.minecraft.world.item.Item main,
            net.minecraft.world.item.Item off) {
        var stand = new net.minecraft.world.entity.decoration.ArmorStand(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
        stand.setYRot(180.0f);
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(helmet));
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, new net.minecraft.world.item.ItemStack(chest));
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, new net.minecraft.world.item.ItemStack(legs));
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, new net.minecraft.world.item.ItemStack(boots));
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, new net.minecraft.world.item.ItemStack(main));
        stand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.OFFHAND, new net.minecraft.world.item.ItemStack(off));
        stand.setShowArms(true);
        level.addFreshEntity(stand);
    }

    private static Step tpOver(int[] ground, double x, double dy, double z, float yaw, float pitch) {
        return Step.once(minecraft -> minecraft.player.connection.sendCommand(
                String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %s %s", x, ground[0] + dy, z, yaw, pitch)));
    }

    // =====================================================================================
    // Rundgang
    // =====================================================================================

    /** Das Gegentor und alle (oder ein) Biome: von oben, dann zwei Blicke vom Boden aus. */
    static void tour(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        if (only.isEmpty() || only.equals("gate")) {
            gate(steps);
        }
        List<ResourceKey<Biome>> biomes = ShadowBiomes.ALL.stream()
                .filter(key -> only.isEmpty() || key.identifier().getPath().equals(only))
                .toList();
        for (ResourceKey<Biome> key : biomes) {
            biome(steps, key);
        }
    }

    private static void gate(Deque<Step> steps) {
        double[] c = new double[4];
        steps.add(Step.server(level -> {
            ServerLevel shadow = shadow(level);
            BlockPos gate = CounterGate.ensure(shadow, BlockPos.ZERO);
            c[0] = gate.getX() + 0.5;
            c[1] = gate.getZ() + 0.5;
            c[2] = gate.getY();
            c[3] = 1;
            KingdomsAtWar.LOGGER.info("[Showcase] Gegentor bei {}", gate);
        }));
        steps.add(Step.until(() -> c[3] != 0, 2400));
        steps.add(view(c, 0, 14, 26, 2));
        steps.add(Step.waitTicks(120));
        steps.add(Step.waitForChunks(2000));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("shadow_gate"));
        steps.add(view(c, 9, 3, 12, 3));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.screenshot("shadow_gate_near"));
    }

    private static void biome(Deque<Step> steps, ResourceKey<Biome> key) {
        String name = key.identifier().getPath();
        double[] c = new double[4];
        steps.add(Step.server(level -> {
            ServerLevel shadow = shadow(level);
            int originY = key == ShadowBiomes.FLOATING_ISLES ? 200 : key == ShadowBiomes.SHADOW_DEPTHS ? -20 : 80;
            Pair<BlockPos, Holder<Biome>> found = shadow.findClosestBiome3d(biome -> biome.is(key), new BlockPos(0, originY, 0),
                    6000, 32, 24);
            if (found == null) {
                KingdomsAtWar.LOGGER.info("[Showcase] Biom {} nicht gefunden", name);
                c[3] = -1;
                return;
            }
            BlockPos pos = found.getFirst();
            int y;
            if (key == ShadowBiomes.FLOATING_ISLES) {
                // eine echte Insel in der Nähe suchen
                BlockPos island = null;
                for (BlockPos.MutableBlockPos p : BlockPos.spiralAround(pos.atY(0), 40, net.minecraft.core.Direction.EAST,
                        net.minecraft.core.Direction.SOUTH)) {
                    int x = pos.getX() + (p.getX() - pos.getX()) * 8;
                    int z = pos.getZ() + (p.getZ() - pos.getZ()) * 8;
                    shadow.getChunk(x >> 4, z >> 4);
                    int top = firstSolidBelow(shadow, x, 300, z);
                    if (top > ShadowTerrain.ISLAND_BOTTOM) {
                        island = new BlockPos(x, top, z);
                        break;
                    }
                }
                pos = island != null ? island : pos;
                y = island != null ? island.getY() : 200;
            } else if (key == ShadowBiomes.CRYSTAL_CHASMS) {
                // die tiefste Stelle der Schlucht in der Nähe
                BlockPos best = pos;
                int lowest = Integer.MAX_VALUE;
                for (int dx = -48; dx <= 48; dx += 6) {
                    for (int dz = -48; dz <= 48; dz += 6) {
                        int x = pos.getX() + dx;
                        int z = pos.getZ() + dz;
                        shadow.getChunk(x >> 4, z >> 4);
                        int floor = firstSolidBelow(shadow, x, 165, z);
                        if (floor < lowest) {
                            lowest = floor;
                            best = new BlockPos(x, floor, z);
                        }
                    }
                }
                pos = best;
                y = lowest;
            } else if (key == ShadowBiomes.SHADOW_DEPTHS) {
                y = cave(shadow, pos);
            } else {
                y = firstSolidBelow(shadow, pos.getX(), 165, pos.getZ());
            }
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    shadow.getChunk((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz);
                }
            }
            c[0] = pos.getX() + 0.5;
            c[1] = pos.getZ() + 0.5;
            c[2] = y;
            c[3] = 1;
            KingdomsAtWar.LOGGER.info("[Showcase] Biom {} bei {} (Höhe {})", name, pos, y);
        }));
        steps.add(Step.until(() -> c[3] != 0, 20 * 300));
        boolean chasm = key == ShadowBiomes.CRYSTAL_CHASMS;
        steps.add(chasm ? view(c, -14, 46, -14, 0) : view(c, -30, 26, -30, 0));
        steps.add(Step.waitTicks(120));
        steps.add(Step.waitForChunks(2400));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("shadow_" + name + "_overview"));
        steps.add(viewDir(c, 2.2, 0.0, 6.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("shadow_" + name + "_ground_a"));
        steps.add(viewDir(c, 2.2, 135.0, 4.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("shadow_" + name + "_ground_b"));
    }

    // =====================================================================================
    // Kreaturen
    // =====================================================================================

    /**
     * Die Kreaturen der Schattenwelt auf einer Lichtung in den Glimmerweiten: Mondhirsche (Hirsch, Hirschkuh, Kitz),
     * Kristallkriecher, Schattenpirscher, Nachtschwinge und Glimmerfalter – erst in Reihe (ohne eigenen Willen), mit
     * {@code live} danach als Videos: die Tiere frei laufend und der Pirscher auf der Jagd.
     */
    static void creatures(Deque<Step> steps, boolean live) {
        double[] c = new double[4];          // x0, y, lz, bereit
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(10)));
        // Monster brauchen eine Schwierigkeit (im Frieden verschwinden sie); von selbst erscheint aber niemand
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s resistance infinite 5 true"));
        steps.add(Step.command("effect give @s saturation infinite 5 true"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> {
            ServerLevel shadow = shadow(level);
            Pair<BlockPos, Holder<Biome>> found = shadow.findClosestBiome3d(biome -> biome.is(ShadowBiomes.GLIMMER_MEADOWS),
                    new BlockPos(0, 80, 0), 6000, 32, 24);
            BlockPos pos = found != null ? found.getFirst() : BlockPos.ZERO;
            // die Bühne bleibt geladen, damit die Kreaturen dieselben bleiben
            for (int dx = -3; dx <= 3; dx++) {
                for (int dz = -3; dz <= 3; dz++) {
                    shadow.setChunkForced((pos.getX() >> 4) + dx, (pos.getZ() >> 4) + dz, true);
                }
            }
            int y = Math.max(firstSolidBelow(shadow, pos.getX(), 165, pos.getZ()), ShadowTerrain.SEA_LEVEL + 2);
            int x0 = pos.getX() - 13;
            int z0 = pos.getZ() - 22;
            var grass = io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_GRASS.get().defaultBlockState();
            var soil = io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_SOIL.get().defaultBlockState();
            var air = net.minecraft.world.level.block.Blocks.AIR.defaultBlockState();
            for (int dx = -2; dx < 29; dx++) {
                for (int dz = 0; dz < 50; dz++) {
                    for (int dy = -4; dy < 14; dy++) {
                        var state = dy < -1 ? soil : dy == -1 ? grass : air;
                        shadow.setBlock(new BlockPos(x0 + dx, y + dy, z0 + dz), state, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                    }
                }
            }
            // Pflanzen hinter der Reihe und am Rand
            var plants = List.of(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_SPROUTS.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.GHOSTCAP.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_SPROUTS.get(),
                    io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.PALE_GRASS.get());
            var random = net.minecraft.util.RandomSource.create(42);
            for (int i = 0; i < 70; i++) {
                int dx = random.nextInt(29) - 2;
                int dz = random.nextInt(50);
                if (dz > 17 && dz < 46) {
                    continue;                // vor der Reihe frei lassen
                }
                shadow.setBlock(new BlockPos(x0 + dx, y, z0 + dz), plants.get(random.nextInt(plants.size())).defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
            double lz = z0 + 20.5;
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MOON_STAG.get(), x0 + 3.5, y, lz, -20,
                    m -> m.setStag(true));
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MOON_STAG.get(), x0 + 6.0, y, lz + 0.5, 10,
                    m -> m.setStag(false));
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.MOON_STAG.get(), x0 + 7.8, y, lz + 1.2, 30,
                    m -> m.setBaby(true));
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.CRYSTAL_CRAWLER.get(), x0 + 10.5, y, lz, 15, m -> { });
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SHADE_STALKER.get(), x0 + 13.5, y, lz, 0, m -> { });
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.NIGHT_WING.get(), x0 + 17.0, y + 2.6, lz, -25, m -> {
                m.setNoGravity(true);
                m.setPhantomSize(1);
            });
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.GLIMMER_MOTH.get(), x0 + 20.5, y + 1.3, lz - 0.5, 30,
                    m -> m.setNoGravity(true));
            creature(shadow, io.github.sirschrolli.kingdomsatwar.registry.ModEntities.GLIMMER_MOTH.get(), x0 + 21.8, y + 2.0, lz + 0.8, -40,
                    m -> m.setNoGravity(true));
            c[0] = x0;
            c[1] = y;
            c[2] = lz;
            c[3] = 1;
            KingdomsAtWar.LOGGER.info("[Showcase] Kreaturen bei {} {} {}", x0, y, lz);
        }));
        steps.add(Step.until(() -> c[3] != 0, 20 * 300));
        // Standbilder aus der Luft (Zuschauermodus: die Kamera schwebt genau dort)
        steps.add(at(c, 12.6, 2.8, 8.5, 12.6, 1.0, 0.0));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.waitTicks(40));
        shot(steps, "shadow_creatures_overview");
        steps.add(at(c, 5.8, 1.6, 4.4, 5.8, 1.0, 0.0));
        shot(steps, "shadow_creatures_stags");
        steps.add(at(c, 11.6, 1.2, 3.6, 11.6, 0.8, 0.0));
        shot(steps, "shadow_creatures_crawler_stalker");
        steps.add(at(c, 18.8, 1.0, 4.6, 19.2, 2.2, 0.0));
        shot(steps, "shadow_creatures_flyers");
        steps.add(at(c, 12.5, 2.4, -6.5, 12.5, 1.0, 0.0));
        shot(steps, "shadow_creatures_back");
        // Der Pirscher: Spieler am Boden und im Überlebensmodus, damit er ihn jagt
        steps.add(Step.command("gamemode survival"));
        steps.add(at(c, 13.5, 0.0, 3.4, 13.5, 1.9, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> tagged(level).forEach(m -> {
            if (m instanceof io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity stalker) {
                stalker.setTarget(level.getServer().getPlayerList().getPlayers().get(0));
                stalker.setAggressive(true);
            }
        })));
        steps.add(Step.waitTicks(40));
        shot(steps, "shadow_creatures_stalker");
        // zur Seite geblickt: unbeobachtet verblasst der Pirscher (rechts im Bild)
        steps.add(at(c, 11.0, 0.0, 4.5, 10.0, 1.4, 0.0));
        steps.add(Step.waitTicks(50));
        shot(steps, "shadow_creatures_stalker_lurking");
        if (!live) {
            steps.add(Step.command("gamemode creative"));
            return;
        }
        // Video 1: die Tiere leben (Zuschauermodus, damit niemand angreift)
        steps.add(Step.command("gamemode spectator"));
        steps.add(at(c, 12.6, 2.6, 9.0, 12.6, 1.0, 0.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> tagged(level).forEach(m -> {
            if (!(m instanceof io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity)) {
                m.setNoAi(false);
                m.setNoGravity(false);
            }
        })));
        steps.add(Step.record("shadow_life", 240, minecraft -> { }));
        // Video 2: der Pirscher jagt – angesehen langsam und deutlich, unbeobachtet schnell und kaum zu sehen
        steps.add(Step.server(level -> tagged(level).forEach(m -> {
            if (!(m instanceof io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity)) {
                m.discard();
            }
        })));
        steps.add(Step.command("gamemode survival"));
        steps.add(at(c, 13.5, 0.0, 14.0, 13.5, 1.9, 0.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> tagged(level).forEach(m -> {
            m.setNoAi(false);
            m.setTarget(level.getServer().getPlayerList().getPlayers().get(0));
        })));
        int[] frame = new int[1];
        steps.add(Step.record("shadow_stalker", 115, minecraft -> {
            int f = frame[0]++;
            var player = minecraft.player;
            var stalker = minecraft.level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity.class,
                    player.getBoundingBox().inflate(40)).stream().findFirst().orElse(null);
            if (stalker == null) {
                return;
            }
            double dx = stalker.getX() - player.getX();
            double dz = stalker.getZ() - player.getZ();
            double dy = stalker.getY(0.6) - player.getEyeY();
            float toStalker = (float) (-Math.atan2(dx, dz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
            // 0–30: ansehen · 30–60: 42° daneben blicken · ab 60: wieder ansehen
            float yaw = f >= 30 && f < 60 ? toStalker - 42.0f : toStalker;
            player.connection.sendCommand(String.format(Locale.ROOT, "tp @s ~ ~ ~ %.1f %.1f", yaw, pitch));
            if (f % 10 == 0) {
                KingdomsAtWar.LOGGER.info("[Showcase] Pirscher: Abstand {} sichtbar {} beobachtet {} jagt {}",
                        String.format(Locale.ROOT, "%.1f", Math.sqrt(dx * dx + dz * dz)),
                        String.format(Locale.ROOT, "%.2f", stalker.visibility), stalker.isWatched(), stalker.isAggressive());
            }
        }));
        steps.add(Step.command("gamemode creative"));
    }

    /** Die Kreaturen der Bühne (am Merkmal erkennbar, auch wenn sie neu geladen wurden). */
    private static List<net.minecraft.world.entity.Mob> tagged(ServerLevel any) {
        List<net.minecraft.world.entity.Mob> mobs = new java.util.ArrayList<>();
        shadow(any).getAllEntities().forEach(entity -> {
            if (entity instanceof net.minecraft.world.entity.Mob mob && mob.entityTags().contains(STAGE_TAG)) {
                mobs.add(mob);
            }
        });
        return mobs;
    }

    private static final String STAGE_TAG = "kaw_showcase";

    private static void shot(Deque<Step> steps, String name) {
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot(name));
        steps.add(Step.once(minecraft -> minecraft.level.getEntitiesOfClass(
                        io.github.sirschrolli.kingdomsatwar.entity.shadow.ShadeStalkerEntity.class, minecraft.player.getBoundingBox().inflate(40))
                .forEach(stalker -> KingdomsAtWar.LOGGER.info("[Showcase] {}: Pirscher sichtbar {} beobachtet {} jagt {}", name,
                        stalker.visibility, stalker.isWatched(), stalker.isAggressive()))));
    }

    /** Kamera an (x0+dx, y+dy, lz+dz), Blick auf (x0+tx, y+ty, lz+tz) – in der Schattenwelt. */
    private static Step at(double[] c, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            double cx = c[0] + dx;
            double cy = c[1] + dy;
            double cz = c[2] + dz;
            double lx = tx - dx;
            double ly = ty - (dy + 1.62);
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                    DIM, cx, cy, cz, yaw, pitch));
        });
    }

    private static <T extends net.minecraft.world.entity.Mob> T creature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type,
            double x, double y, double z, float yaw, java.util.function.Consumer<T> setup) {
        T mob = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Kreatur lässt sich nicht erzeugen: " + type);
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

    /** Oberste feste Stelle (kein Laub, kein Wasser) in der Säule ab {@code fromY} abwärts, +1. */
    private static int firstSolidBelow(ServerLevel shadow, int x, int fromY, int z) {
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos(x, fromY, z);
        while (p.getY() > shadow.getMinY()) {
            var state = shadow.getBlockState(p);
            if (state.is(net.minecraft.tags.BlockTags.BLOCKS_MOTION_IN_HEIGHTMAP_NO_LEAVES) && state.getFluidState().isEmpty()) {
                return p.getY() + 1;
            }
            p.move(0, -1, 0);
        }
        return shadow.getMinY();
    }

    /** Höhle für den Blick in die Tiefe: erste Luftblase ab {@code pos} nach oben. */
    private static int cave(ServerLevel shadow, BlockPos pos) {
        BlockPos.MutableBlockPos p = pos.mutable();
        for (int i = 0; i < 80; i++) {
            if (shadow.getBlockState(p).isAir() && shadow.getBlockState(p.above()).isAir() && !shadow.getBlockState(p.below()).isAir()) {
                return p.getY();
            }
            p.move(0, 1, 0);
        }
        return pos.getY();
    }

    /** Kamera relativ zum Punkt c setzen und auf c (plus {@code lookUp}) blicken lassen – in der Schattenwelt. */
    private static Step view(double[] c, double dx, double dy, double dz, double lookUp) {
        return Step.once(minecraft -> {
            if (c[3] < 0) {
                return;
            }
            double cx = c[0] + dx;
            double cy = c[2] + dy;
            double cz = c[1] + dz;
            double lx = c[0] - cx;
            double ly = c[2] + lookUp - cy;
            double lz = c[1] - cz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                    DIM, cx, cy, cz, yaw, pitch));
        });
    }

    /** Kamera am Punkt c in Augenhöhe, Blick in Richtung {@code yaw}, leicht nach unten. */
    private static Step viewDir(double[] c, double eye, double yaw, double pitch) {
        return Step.once(minecraft -> {
            if (c[3] < 0) {
                return;
            }
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                    DIM, c[0], c[2] + eye, c[1], yaw, pitch));
        });
    }
}
