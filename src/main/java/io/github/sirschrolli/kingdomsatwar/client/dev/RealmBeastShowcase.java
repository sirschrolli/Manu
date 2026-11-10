package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.monster.ChorusMawEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.NightRamEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.RiftHoundEntity;
import io.github.sirschrolli.kingdomsatwar.entity.monster.SoulHeronEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.shadow.ShadowBiomes;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.phys.AABB;

/**
 * Vorführung der Monster der Schattenwelt und des Ends (0.71) an ihren echten Orten: ein Seelensumpf und ein
 * Schattengipfel in der Schattenwelt (unter ihrem ewigen Zwielicht), eine Choruswald-Insel im End. Die Bühnen werden hoch
 * über dem Gelände gebaut (keine Berge im Weg), die Wesen erscheinen erst, wenn die Kamera da ist.
 */
final class RealmBeastShowcase {
    private static final String STAGE_TAG = "kaw_realm_stage";
    private static final String SHADOW = "kingdomsatwar:shadow_world";
    private static final String END = "minecraft:the_end";
    /** Bühnen: Mitte (x, Boden-y, z). */
    private static final int MX = 4000;
    private static final int MZ = 4000;
    private static final int PX = 4200;
    private static final int PZ = 4000;
    private static final int SY = 210;
    private static final int EX = 1400;
    private static final int EZ = 1400;
    private static final int EY = 80;

    private RealmBeastShowcase() {}

    /** {@code only}: nur diese Bühne (marsh, peaks, end) – oder alle. */
    static void beasts(Deque<Step> steps, String only) {
        Mob[] m = new Mob[8];
        steps.add(Step.command("difficulty hard"));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));

        // ------------------------------------------------------------------ Seelensumpf
        if (only.isEmpty() || only.equals("marsh")) {
            build(steps, ShadowGate.SHADOW_WORLD, level -> marsh(level, MX, SY, MZ), ShadowBiomes.SOUL_MARSH, MX, SY, MZ);
            arrive(steps, SHADOW, MX + 0.6, SY + 0.4, MZ - 2.0, MX + 0.6, SY + 1.5, MZ + 3.2);
            steps.add(Step.server(o -> {
                ServerLevel level = shadow(o);
                m[0] = creature(level, ModEntities.SOUL_HERON.get(), MX - 1.8, SY, MZ + 2.4, 245.0f, h -> { });
                m[1] = creature(level, ModEntities.SOUL_HERON.get(), MX + 3.0, SY, MZ + 4.6, 160.0f, h -> { });
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[1], SoulHeronEntity.EVENT_BUFFET)));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot("realm_heron_marsh"));
            // der Stoß nach einem Ritter, von der Seite
            steps.add(Step.server(o -> {
                ServerLevel level = shadow(o);
                m[1].discard();
                m[0].snapTo(MX - 2.0, SY, MZ + 2.5, 0.0f, 0.0f);
                m[2] = soldier(level, MX + 1.3, SY, MZ + 2.5, 90.0f);
                face(m[0], m[2]);
                face(m[2], m[0]);
            }));
            look(steps, SHADOW, MX - 0.4, SY + 0.6, MZ - 2.2, MX - 0.4, SY + 1.8, MZ + 2.5);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], SoulHeronEntity.EVENT_STAB)));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot("realm_heron_stab"));
            // im Flug, mit ausgebreiteten Schwingen
            steps.add(Step.server(o -> {
                ServerLevel level = shadow(o);
                m[0].discard();
                m[2].discard();
                m[3] = creature(level, ModEntities.SOUL_HERON.get(), MX + 0.5, SY + 2.4, MZ + 3.4, 125.0f, h -> h.forceFlying(true));
            }));
            look(steps, SHADOW, MX - 1.4, SY + 0.6, MZ - 0.9, MX + 0.3, SY + 3.4, MZ + 3.4);
            steps.add(Step.waitTicks(36));
            steps.add(Step.screenshot("realm_heron_flight"));
            // der Kopf aus der Nähe
            steps.add(Step.server(o -> {
                m[3].discard();
                m[4] = creature(shadow(o), ModEntities.SOUL_HERON.get(), MX + 0.5, SY, MZ + 2.6, 200.0f, h -> { });
            }));
            look(steps, SHADOW, MX - 0.5, SY + 1.0, MZ + 0.2, MX + 0.5, SY + 2.2, MZ + 2.4);
            steps.add(Step.waitTicks(35));
            steps.add(Step.screenshot("realm_heron_close"));
            steps.add(Step.server(o -> clear(shadow(o), MX, SY, MZ)));
        }

        // ------------------------------------------------------------------ Schattengipfel
        if (only.isEmpty() || only.equals("peaks")) {
            build(steps, ShadowGate.SHADOW_WORLD, level -> peaks(level, PX, SY, PZ), ShadowBiomes.SHADOW_PEAKS, PX, SY, PZ);
            arrive(steps, SHADOW, PX + 0.3, SY + 0.3, PZ - 1.5, PX + 0.3, SY + 0.9, PZ + 3.6);
            steps.add(Step.server(o -> {
                ServerLevel level = shadow(o);
                m[0] = creature(level, ModEntities.NIGHT_RAM.get(), PX - 2.2, SY, PZ + 2.6, 210.0f, r -> { });
                m[1] = creature(level, ModEntities.NIGHT_RAM.get(), PX + 2.6, SY, PZ + 4.6, 150.0f, r -> r.forceState(NightRamEntity.PREPARE));
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[1], NightRamEntity.EVENT_PREPARE)));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot("realm_ram_peaks"));
            // im Ansturm, von der Seite
            steps.add(Step.server(o -> {
                m[0].discard();
                ((NightRamEntity) m[1]).forceState(NightRamEntity.CHARGE);
                m[1].snapTo(PX - 0.5, SY, PZ + 2.6, 90.0f, 0.0f);
                m[1].setYBodyRot(90.0f);
                m[1].setYHeadRot(90.0f);
            }));
            look(steps, SHADOW, PX - 0.5, SY + 0.5, PZ - 1.9, PX - 0.5, SY + 0.9, PZ + 2.6);
            steps.add(Step.waitTicks(33));
            steps.add(Step.screenshot("realm_ram_charge"));
            // aufgebäumt zum Stampfen
            steps.add(Step.server(o -> {
                m[1].discard();
                m[2] = creature(shadow(o), ModEntities.NIGHT_RAM.get(), PX + 0.5, SY, PZ + 2.5, 240.0f, r -> r.forceState(NightRamEntity.STOMP));
            }));
            look(steps, SHADOW, PX + 0.2, SY + 0.1, PZ - 1.2, PX + 0.6, SY + 1.4, PZ + 2.5);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[2], NightRamEntity.EVENT_STOMP)));
            steps.add(Step.waitTicks(11));
            steps.add(Step.screenshot("realm_ram_stomp"));
            // die Hörner aus der Nähe
            steps.add(Step.server(o -> {
                ((NightRamEntity) m[2]).forceState(NightRamEntity.IDLE);
                m[2].snapTo(PX + 0.5, SY, PZ + 2.5, 250.0f, 0.0f);
                m[2].setYBodyRot(250.0f);
                m[2].setYHeadRot(250.0f);
            }));
            look(steps, SHADOW, PX + 2.0, SY - 0.25, PZ + 0.4, PX + 0.6, SY + 1.15, PZ + 2.4);
            steps.add(Step.waitTicks(35));
            steps.add(Step.screenshot("realm_ram_close"));
            steps.add(Step.server(o -> clear(shadow(o), PX, SY, PZ)));
        }

        // ------------------------------------------------------------------ End-Insel mit Choruswald
        if (only.isEmpty() || only.equals("end")) {
            build(steps, Level.END, level -> endIsland(level, EX, EY, EZ), Biomes.END_HIGHLANDS, EX, EY, EZ);
            arrive(steps, END, EX + 0.2, EY + 0.1, EZ - 1.6, EX + 0.2, EY + 0.8, EZ + 3.0);
            steps.add(Step.server(o -> {
                ServerLevel level = end(o);
                purge(level, EX, EY, EZ);
                m[0] = creature(level, ModEntities.RIFT_HOUND.get(), EX - 1.4, EY, EZ + 2.0, 235.0f, h -> { });
                m[1] = creature(level, ModEntities.RIFT_HOUND.get(), EX + 1.8, EY, EZ + 3.4, 120.0f, h -> { });
                m[2] = creature(level, ModEntities.CHORUS_MAW.get(), EX + 4.5, EY, EZ + 7.5, 205.0f, c -> c.forceOpen(true));
                m[3] = creature(level, ModEntities.CHORUS_MAW.get(), EX - 3.8, EY, EZ + 7.6, 160.0f, c -> { });
            }));
            look(steps, END, EX + 0.2, EY + 0.1, EZ - 1.6, EX + 0.2, EY + 0.8, EZ + 3.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[1], RiftHoundEntity.EVENT_HOWL)));
            steps.add(Step.waitTicks(16));
            steps.add(Step.screenshot("realm_end_hounds"));
            // der Phasensprung: zerfließend, daneben einer, der zubeißt
            steps.add(Step.server(o -> {
                m[1].snapTo(EX + 1.2, EY, EZ + 2.4, 160.0f, 0.0f);
                m[0].snapTo(EX - 1.4, EY, EZ + 2.2, 270.0f, 0.0f);
                m[0].setYBodyRot(270.0f);
                m[0].setYHeadRot(270.0f);
            }));
            look(steps, END, EX - 0.1, EY + 0.0, EZ - 0.9, EX - 0.1, EY + 0.8, EZ + 2.3);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> {
                event(m[1], RiftHoundEntity.EVENT_PHASE_OUT);
                event(m[0], RiftHoundEntity.EVENT_BITE);
            }));
            steps.add(Step.waitTicks(3));
            steps.add(Step.screenshot("realm_hound_phase"));
            // der Chorusschlund, offen, mit einem Rankenhieb nach einem Ritter
            steps.add(Step.server(o -> {
                m[0].discard();
                m[1].discard();
                m[3].discard();
                m[4] = soldier(end(o), EX + 2.0, EY, EZ + 4.6, 0.0f);
                face(m[2], m[4]);
                face(m[4], m[2]);
            }));
            look(steps, END, EX + 5.6, EY + 0.0, EZ + 4.0, EX + 3.3, EY + 1.3, EZ + 6.1);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[2], ChorusMawEntity.EVENT_LASH_LEFT)));
            steps.add(Step.waitTicks(7));
            steps.add(Step.screenshot("realm_maw_lash"));
            // der offene Schlund aus der Nähe
            steps.add(Step.server(o -> m[4].discard()));
            look(steps, END, EX + 3.2, EY + 0.6, EZ + 5.4, EX + 4.5, EY + 1.8, EZ + 7.5);
            steps.add(Step.waitTicks(35));
            steps.add(Step.screenshot("realm_maw_close"));
            steps.add(Step.server(o -> clear(end(o), EX, EY, EZ)));
        }
    }

    // =====================================================================================
    // Bühnen
    // =====================================================================================

    private static void build(Deque<Step> steps, ResourceKey<Level> dim, Consumer<ServerLevel> stage, ResourceKey<Biome> biome, int cx,
            int y, int cz) {
        boolean[] ready = new boolean[1];
        steps.add(Step.server(o -> {
            ServerLevel level = o.getServer().getLevel(dim);
            if (level != null) {
                stage.accept(level);
                var holder = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(biome);
                for (int x = cx - 48; x <= cx + 48; x += 8) {
                    net.minecraft.server.commands.FillBiomeCommand.fill(level, new BlockPos(x, y - 12, cz - 32),
                            new BlockPos(x + 7, y + 20, cz + 50), holder);
                }
            }
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
    }

    /** Freiraum über der Bühne und eine weite Fläche, damit Tiefe und Gelände nicht ins Bild ragen. */
    private static void clearAbove(ServerLevel level, int cx, int y, int cz) {
        Showcase.fill(level, new BlockPos(cx - 46, y, cz - 30), new BlockPos(cx + 46, y + 26, cz + 48), Blocks.AIR.defaultBlockState());
    }

    /** Seelensumpf: Schattengras, flache Wasserlöcher mit Seelenschilf, Geisterpilze, Sprossen, Mondfarne, Schattenbäume. */
    private static void marsh(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(71);
        Showcase.fill(level, new BlockPos(cx - 46, y - 3, cz - 30), new BlockPos(cx + 46, y - 2, cz + 48),
                ShadowBlocks.SHADOW_SOIL.get().defaultBlockState());
        Showcase.fill(level, new BlockPos(cx - 46, y - 1, cz - 30), new BlockPos(cx + 46, y - 1, cz + 48),
                ShadowBlocks.SHADOW_GRASS.get().defaultBlockState());
        // Wasserlöcher (eins vorn, wo die Reiher waten)
        double[][] pools = {{-6.5, 6.0, 3.4}, {7.0, 10.0, 3.6}, {-12.0, 13.0, 5.0}, {6.0, 22.0, 6.0}, {-20.0, 26.0, 7.0}, {22.0, 2.0, 4.0}};
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 48; z++) {
                boolean water = false;
                for (double[] p : pools) {
                    double dx = x + 0.5 - (cx + p[0]);
                    double dz = z + 0.5 - (cz + p[1]);
                    double r = p[2] + Mth.sin((float) (x * 0.7 + z * 0.3)) * 0.8;
                    if (dx * dx + dz * dz < r * r) {
                        water = true;
                    }
                }
                if (water) {
                    set(level, x, y - 1, z, Blocks.WATER.defaultBlockState());
                    if (random.nextInt(7) == 0 && Math.abs(x - cx) > 2) {
                        tall(level, x, y - 1, z, ShadowBlocks.SOUL_REEDS.get().defaultBlockState(), true);
                    }
                    continue;
                }
                int r = random.nextInt(100);
                if (r < 14) {
                    set(level, x, y - 1, z, ShadowBlocks.GLIMMER_MOSS.get().defaultBlockState());
                } else if (r < 30) {
                    set(level, x, y, z, ShadowBlocks.SHADOW_SPROUTS.get().defaultBlockState());
                } else if (r < 33) {
                    set(level, x, y, z, ShadowBlocks.GHOSTCAP.get().defaultBlockState());
                } else if (r < 36 && !front(x, z, cx, cz)) {
                    tall(level, x, y, z, ShadowBlocks.MOON_FERN.get().defaultBlockState(), false);
                }
            }
        }
        // Schattenbäume ringsum (vorn frei)
        for (int i = 0; i < 46; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz - 26 + random.nextInt(72);
            if (front(x, z, cx, cz) || Math.abs(x - cx) < 9 && z < cz + 14) {
                continue;
            }
            tree(level, x, y, z, 5 + random.nextInt(5), random);
        }
        // Mondlaternen auf Pfählen
        for (int[] p : new int[][] {{-6, 6}, {7, 2}, {-2, 12}}) {
            for (int dy = 0; dy < 3; dy++) {
                set(level, cx + p[0], y + dy, cz + p[1], ShadowBlocks.SHADOW_FENCE.get().defaultBlockState());
            }
            set(level, cx + p[0], y + 3, cz + p[1], ShadowBlocks.MOON_LANTERN.get().defaultBlockState());
        }
    }

    /** Schattengipfel: Schattenstein mit bleichem Gras und Aschenschnee, Felsen mit Nachtkristallen, hinten eine Felswand. */
    private static void peaks(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(73);
        BlockState stone = ShadowBlocks.SHADOW_STONE.base().get().defaultBlockState();
        Showcase.fill(level, new BlockPos(cx - 46, y - 3, cz - 30), new BlockPos(cx + 46, y - 1, cz + 48), stone);
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 48; z++) {
                int r = random.nextInt(100);
                if (r < 9) {
                    set(level, x, y - 1, z, ShadowBlocks.ASH_SNOW_BLOCK.get().defaultBlockState());
                } else if (r < 30) {
                    set(level, x, y, z, ShadowBlocks.PALE_GRASS.get().defaultBlockState());
                } else if (r < 33) {
                    set(level, x, y, z, ShadowBlocks.ASH_SNOW.get().defaultBlockState());
                }
            }
        }
        // Felsen und Grate, hinten höher (wie ein Gipfelkamm)
        for (int i = 0; i < 70; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz - 24 + random.nextInt(70);
            if (front(x, z, cx, cz) || Math.abs(x - cx) < 10 && z < cz + 12) {
                continue;
            }
            int h = 2 + random.nextInt(z > cz + 16 ? 14 : 5);
            int w = 1 + random.nextInt(3);
            for (int dx = -w; dx <= w; dx++) {
                for (int dz = -w; dz <= w; dz++) {
                    int hh = h - (Math.abs(dx) + Math.abs(dz)) * 2;
                    for (int dy = 0; dy < hh; dy++) {
                        set(level, x + dx, y + dy, z + dz, stone);
                    }
                    if (hh > 0) {
                        set(level, x + dx, y + hh, z + dz, random.nextInt(3) == 0 ? ShadowBlocks.ASH_SNOW.get().defaultBlockState()
                                : (random.nextInt(9) == 0 ? ShadowBlocks.NIGHT_CRYSTAL_CLUSTER.get().defaultBlockState()
                                        : Blocks.AIR.defaultBlockState()));
                    }
                }
            }
        }
    }

    /** Eine End-Insel: Endstein, zum Rand hin abgebrochen, darauf ein Wald aus Chorusbäumen (vorn frei). */
    private static void endIsland(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(75);
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 48; z++) {
                double dx = (x - cx) / 44.0;
                double dz = (z - cz - 9) / 38.0;
                double d = Math.sqrt(dx * dx + dz * dz);
                if (d > 1.0) {
                    continue;
                }
                int depth = (int) ((1.0 - d) * 12) + 1;
                for (int dy = 1; dy <= depth; dy++) {
                    set(level, x, y - dy, z, Blocks.END_STONE.defaultBlockState());
                }
            }
        }
        for (int i = 0; i < 40; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = cz - 18 + random.nextInt(60);
            if (front(x, z, cx, cz) || Math.abs(x - cx) < 9 && z < cz + 12) {
                continue;
            }
            double dx = (x - cx) / 44.0;
            double dz = (z - cz - 9) / 38.0;
            if (dx * dx + dz * dz > 0.8) {
                continue;
            }
            ChorusFlowerBlock.generatePlant(level, new BlockPos(x, y, z), random, 8);
        }
        // ein paar Endstäbe als Licht (sonst sind die dunklen Wesen im End kaum zu sehen)
        for (int[] p : new int[][] {{-5, 0}, {5, 1}, {-3, 6}, {7, 5}, {0, 11}}) {
            set(level, cx + p[0], y, cz + p[1], Blocks.END_ROD.defaultBlockState());
        }
    }

    private static boolean front(int x, int z, int cx, int cz) {
        return Math.abs(x - cx) < 10 && z > cz - 14 && z < cz + 10;
    }

    private static void tree(ServerLevel level, int x, int y, int z, int height, RandomSource random) {
        for (int dy = 0; dy < height; dy++) {
            set(level, x, y + dy, z, ShadowBlocks.SHADOW_LOG.get().defaultBlockState());
        }
        int r = 2 + random.nextInt(2);
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                for (int dy = -1; dy <= 2; dy++) {
                    if (dx * dx + dz * dz + dy * dy * 2 <= r * r + 1 && level.getBlockState(new BlockPos(x + dx, y + height + dy, z + dz)).isAir()) {
                        set(level, x + dx, y + height + dy, z + dz,
                                ShadowBlocks.SHADOW_LEAVES.get().defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true));
                    }
                }
            }
        }
    }

    private static void tall(ServerLevel level, int x, int y, int z, BlockState lower, boolean waterlogged) {
        BlockState base = lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER);
        if (base.hasProperty(BlockStateProperties.WATERLOGGED)) {
            base = base.setValue(BlockStateProperties.WATERLOGGED, waterlogged);
        }
        set(level, x, y, z, base);
        BlockState top = lower.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER);
        if (top.hasProperty(BlockStateProperties.WATERLOGGED)) {
            top = top.setValue(BlockStateProperties.WATERLOGGED, false);
        }
        set(level, x, y + 1, z, top);
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static ServerLevel shadow(ServerLevel overworld) {
        return overworld.getServer().getLevel(ShadowGate.SHADOW_WORLD);
    }

    private static ServerLevel end(ServerLevel overworld) {
        return overworld.getServer().getLevel(Level.END);
    }

    private static <T extends Mob> T creature(ServerLevel level, EntityType<T> type, double x, double y, double z, float yaw, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(x, y, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        setup.accept(mob);
        return mob;
    }

    private static Mob soldier(ServerLevel level, double x, double y, double z, float yaw) {
        Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), x, y, z, yaw, k -> { });
        knight.finalizeSpawn(level, level.getCurrentDifficultyAt(knight.blockPosition()), EntitySpawnReason.COMMAND, null);
        knight.setNoAi(true);
        return knight;
    }

    private static void face(Mob mob, Entity target) {
        float yaw = (float) (Mth.atan2(target.getZ() - mob.getZ(), target.getX() - mob.getX()) * Mth.RAD_TO_DEG) - 90.0f;
        mob.snapTo(mob.getX(), mob.getY(), mob.getZ(), yaw, 0.0f);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
    }

    private static void event(Entity entity, byte id) {
        if (entity.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(entity, id);
        }
    }

    /** Wilde Monster der Gegend (Endermen …) verlassen die Bühne. */
    private static void purge(ServerLevel level, int cx, int y, int cz) {
        AABB all = new AABB(cx - 64, y - 20, cz - 48, cx + 64, y + 40, cz + 64);
        for (Entity e : level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, all,
                e -> !e.entityTags().contains(STAGE_TAG))) {
            e.discard();
        }
    }

    private static void clear(ServerLevel level, int cx, int y, int cz) {
        AABB all = new AABB(cx - 50, y - 10, cz - 34, cx + 50, y + 30, cz + 52);
        for (Entity e : level.getEntitiesOfClass(Entity.class, all, e -> e.entityTags().contains(STAGE_TAG))) {
            e.discard();
        }
    }

    private static void arrive(Deque<Step> steps, String dim, double x, double y, double z, double tx, double ty, double tz) {
        look(steps, dim, x, y, z, tx, ty, tz);
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(30));
    }

    /** Kamera mit den Füßen bei (x, y, z) in der Dimension {@code dim}, Blick auf (tx, ty, tz). */
    private static void look(Deque<Step> steps, String dim, double x, double y, double z, double tx, double ty, double tz) {
        steps.add(Step.once(minecraft -> {
            double lx = tx - x;
            double ly = ty - (y + 1.62);
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", dim, x, y, z,
                    yaw, pitch));
        }));
    }
}
