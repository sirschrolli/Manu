package io.github.sirschrolli.kingdomsatwar.client.dev;

import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.arrive;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.build;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.clear;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.clearAbove;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.creature;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.event;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.look;
import static io.github.sirschrolli.kingdomsatwar.client.dev.BiomeBeastShowcase.set;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.ButterflyEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.EmberSalamanderEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.FlamingoEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.MireSnapperEntity;
import io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.DoublePlantBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;

/**
 * Vorführung der Wesen von 0.86 auf kleinen Bühnen hoch über dem Gelände, jede im Biom ihres Wesens: der Glutsalamander an
 * einem Lavatümpel in den Aschefeldern, der Moorschnapper in einem Tümpel im Nebelmoor, die Flamingos in einer Lagune an
 * der Korallenküste, die Schmetterlinge auf einer Blumenwiese der Königsauen.
 */
final class BiomeBeastShowcase2 {
    private static final int AX = 7200;
    private static final int MX = 7500;
    private static final int FX = 7800;
    private static final int BX = 8100;
    private static final int CZ = 6000;
    private static final int SY = 200;

    private BiomeBeastShowcase2() {}

    /** {@code only}: nur diese Bühnen (z. B. "salamander_snapper") – oder alle. */
    static void beasts(Deque<Step> steps, String only) {
        Mob[] m = new Mob[12];
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));

        // ------------------------------------------------------------------ Glutsalamander
        if (only.isEmpty() || only.contains("salamander")) {
            build(steps, level -> ash(level, AX, SY, CZ), OverworldBiomes.ASH_FIELDS, AX, SY, CZ);
            // am Lavatümpel: einer läuft über die Lava, einer sonnt sich am Rand
            arrive(steps, AX - 1.6, SY + 0.1, CZ + 1.0, AX + 2.2, SY + 0.2, CZ + 5.2);
            steps.add(Step.server(o -> {
                m[0] = salamander(o, AX + 2.8, SY - 0.1, CZ + 5.4, 120.0f, 0, s -> { });
                m[1] = salamander(o, AX + 0.6, SY, CZ + 4.1, 200.0f, 1, s -> s.setBasking(true));
            }));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_salamander_lava"));
            // nah: der Goldsalamander zwischen Feuerlilien
            steps.add(Step.server(o -> {
                clear(o, AX, SY, CZ);
                m[0] = salamander(o, AX + 4.4, SY, CZ - 1.6, 150.0f, 1, s -> { });
            }));
            look(steps, AX + 5.0, SY - 0.55, CZ - 3.0, AX + 4.4, SY + 0.15, CZ - 1.6);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("beast_salamander_close"));
            // die Familie: zwei Alte und zwei Junge an der Glut
            steps.add(Step.server(o -> {
                clear(o, AX, SY, CZ);
                m[0] = salamander(o, AX + 2.6, SY, CZ + 1.2, 160.0f, 0, s -> s.setBasking(true));
                m[1] = salamander(o, AX + 4.2, SY, CZ + 1.5, 210.0f, 0, s -> { });
                m[2] = salamander(o, AX + 3.0, SY, CZ + 0.2, 130.0f, 0, s -> s.setBaby(true));
                m[3] = salamander(o, AX + 3.9, SY, CZ + 0.5, 185.0f, 1, s -> s.setBaby(true));
            }));
            look(steps, AX + 3.4, SY - 0.2, CZ - 1.9, AX + 3.4, SY + 0.2, CZ + 1.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("beast_salamander_family"));
            steps.add(Step.server(o -> clear(o, AX, SY, CZ)));
        }

        // ------------------------------------------------------------------ Moorschnapper
        if (only.isEmpty() || only.contains("snapper")) {
            build(steps, level -> moor(level, MX, SY, CZ), OverworldBiomes.MIST_MOOR, MX, SY, CZ);
            // im Tümpel auf der Lauer: nur Panzer und Kopf über dem Wasser
            arrive(steps, MX - 1.6, SY + 0.25, CZ + 1.6, MX - 2.4, SY - 0.2, CZ + 4.6);
            steps.add(Step.server(o -> m[0] = snapper(o, MX - 2.4, SY - 0.55, CZ + 4.6, 160.0f, s -> { })));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_snapper_lurk"));
            // am Ufer: er faucht einen Zombie an
            steps.add(Step.server(o -> {
                clear(o, MX, SY, CZ);
                m[0] = snapper(o, MX - 2.6, SY, CZ + 2.2, 110.0f, s -> { });
                m[1] = creature(o, ModEntities.HUSK.get(), MX - 4.8, SY, CZ + 1.5, 290.0f, z -> { });
            }));
            look(steps, MX - 3.0, SY + 0.2, CZ - 0.6, MX - 3.2, SY + 0.5, CZ + 2.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], MireSnapperEntity.EVENT_HISS)));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot("beast_snapper_hiss"));
            // er schnappt zu
            steps.add(Step.server(o -> {
                clear(o, MX, SY, CZ);
                m[0] = snapper(o, MX - 2.6, SY, CZ + 2.2, 110.0f, s -> { });
                m[1] = creature(o, ModEntities.HUSK.get(), MX - 4.1, SY, CZ + 1.7, 290.0f, z -> { });
            }));
            look(steps, MX - 3.3, SY + 0.1, CZ - 0.4, MX - 3.3, SY + 0.5, CZ + 2.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], MireSnapperEntity.EVENT_SNAP)));
            steps.add(Step.waitTicks(2));
            steps.add(Step.screenshot("beast_snapper_snap"));
            // in den Panzer zurückgezogen, im Schlamm
            steps.add(Step.server(o -> {
                clear(o, MX, SY, CZ);
                m[0] = snapper(o, MX - 2.4, SY, CZ + 2.4, 160.0f, s -> s.setWithdrawn(true));
            }));
            look(steps, MX - 1.8, SY + 0.2, CZ + 0.9, MX - 2.4, SY + 0.3, CZ + 2.4);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("beast_snapper_shell"));
            steps.add(Step.server(o -> clear(o, MX, SY, CZ)));
        }

        // ------------------------------------------------------------------ Flamingo
        if (only.isEmpty() || only.contains("flamingo")) {
            build(steps, level -> lagoon(level, FX, SY, CZ), OverworldBiomes.CORAL_COAST, FX, SY, CZ, 120, 170);
            // die Schar in der Lagune: einige seihen, zwei ruhen auf einem Bein, die übrigen stehen und schauen
            arrive(steps, FX + 0.2, SY + 0.5, CZ + 0.8, FX + 0.2, SY + 0.8, CZ + 6.4);
            steps.add(Step.server(o -> {
                m[0] = flamingo(o, FX - 2.2, SY - 1.0, CZ + 4.2, 150.0f, 0, f -> f.setFeeding(true));
                m[1] = flamingo(o, FX - 0.6, SY - 1.0, CZ + 5.6, 200.0f, 0, f -> { });
                m[2] = flamingo(o, FX + 1.4, SY - 1.0, CZ + 4.6, 230.0f, 1, f -> f.setResting(true));
                m[3] = flamingo(o, FX + 2.9, SY - 1.0, CZ + 6.4, 170.0f, 0, f -> f.setFeeding(true));
                m[4] = flamingo(o, FX - 3.6, SY - 1.0, CZ + 7.0, 120.0f, 0, f -> f.setResting(true));
                m[5] = flamingo(o, FX + 0.6, SY - 1.0, CZ + 8.2, 160.0f, 1, f -> { });
                m[6] = flamingo(o, FX - 1.6, SY - 1.0, CZ + 9.4, 90.0f, 0, f -> f.setFeeding(true));
                m[7] = flamingo(o, FX + 3.4, SY - 1.0, CZ + 9.8, 250.0f, 0, f -> { });
            }));
            steps.add(Step.waitTicks(50));
            steps.add(Step.screenshot("beast_flamingo_flock"));
            // nah: zwei seihen mit dem Kopf nach unten
            steps.add(Step.server(o -> {
                clear(o, FX, SY, CZ);
                m[0] = flamingo(o, FX - 0.4, SY - 1.0, CZ + 3.4, 100.0f, 0, f -> f.setFeeding(true));
                m[1] = flamingo(o, FX + 1.0, SY - 1.0, CZ + 4.2, 250.0f, 1, f -> f.setFeeding(true));
            }));
            look(steps, FX + 0.3, SY + 0.25, CZ + 1.0, FX + 0.3, SY + 0.3, CZ + 3.8);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_flamingo_feed"));
            // auf einem Bein, der Kubaflamingo
            steps.add(Step.server(o -> {
                clear(o, FX, SY, CZ);
                m[0] = flamingo(o, FX + 0.2, SY - 1.0, CZ + 3.2, 130.0f, 1, f -> f.setResting(true));
                m[1] = flamingo(o, FX - 1.4, SY - 1.0, CZ + 4.6, 160.0f, 0, f -> { });
            }));
            look(steps, FX + 1.4, SY + 0.3, CZ + 1.2, FX - 0.2, SY + 0.8, CZ + 3.6);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_flamingo_rest"));
            // im Flug über der Lagune
            steps.add(Step.server(o -> {
                clear(o, FX, SY, CZ);
                m[0] = flamingo(o, FX - 0.4, SY + 2.6, CZ + 3.4, 280.0f, 0, f -> f.setFlying(true));
                m[1] = flamingo(o, FX + 1.6, SY + 3.4, CZ + 5.6, 285.0f, 1, f -> f.setFlying(true));
                m[2] = flamingo(o, FX - 2.2, SY + 3.1, CZ + 6.4, 275.0f, 0, f -> f.setFlying(true));
            }));
            look(steps, FX - 0.2, SY + 0.6, CZ - 0.6, FX - 0.2, SY + 3.0, CZ + 4.6);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_flamingo_flight"));
            steps.add(Step.server(o -> clear(o, FX, SY, CZ)));
        }

        // ------------------------------------------------------------------ Schmetterlinge
        if (only.isEmpty() || only.contains("butterfly")) {
            build(steps, level -> meadow(level, BX, SY, CZ), OverworldBiomes.ROYAL_MEADOWS, BX, SY, CZ);
            // über der Wiese: alle sechs Arten, einige sitzen auf den Blüten
            arrive(steps, BX + 0.0, SY + 0.3, CZ - 0.9, BX + 0.0, SY + 0.8, CZ + 2.6);
            steps.add(Step.server(o -> {
                m[0] = butterfly(o, BX - 0.9, SY + 1.15, CZ + 1.6, 30.0f, 0, false);
                m[1] = butterfly(o, BX + 0.8, SY + 1.35, CZ + 2.4, 300.0f, 1, false);
                m[2] = butterfly(o, BX + 0.1, SY + 1.7, CZ + 3.4, 200.0f, 2, false);
                m[3] = butterfly(o, BX - 1.7, SY + 1.5, CZ + 3.6, 80.0f, 3, false);
                m[4] = butterfly(o, BX + 1.8, SY + 1.1, CZ + 3.2, 250.0f, 4, false);
                m[5] = butterfly(o, BX + 0.5, SY + 0.62, CZ + 2.5, 150.0f, 5, true);
                m[6] = butterfly(o, BX + 1.5, SY + 0.62, CZ + 1.5, 210.0f, 0, true);
                m[7] = butterfly(o, BX - 1.5, SY + 0.62, CZ + 2.5, 120.0f, 3, true);
            }));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_butterfly_meadow"));
            // die sechs Arten nebeneinander, nah
            steps.add(Step.server(o -> {
                clear(o, BX, SY, CZ);
                for (int i = 0; i < ButterflyEntity.VARIANTS; i++) {
                    m[i] = butterfly(o, BX - 1.0 + (i % 3) + 0.5, SY + 0.62, CZ + 1.5 + (i / 3), 180.0f, i, true);
                }
            }));
            look(steps, BX + 0.0, SY + 1.15, CZ - 0.4, BX + 0.0, SY + 0.6, CZ + 2.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("beast_butterfly_species"));
            // auf einer Blume: ein Tagpfauenauge sonnt sich
            steps.add(Step.server(o -> {
                clear(o, BX, SY, CZ);
                m[0] = butterfly(o, BX + 0.5, SY + 0.62, CZ + 1.5, 160.0f, 0, true);
            }));
            look(steps, BX + 0.75, SY - 0.15, CZ + 0.55, BX + 0.5, SY + 0.62, CZ + 1.5);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("beast_butterfly_perch"));
            steps.add(Step.server(o -> clear(o, BX, SY, CZ)));
        }
    }

    // =====================================================================================
    // Wesen
    // =====================================================================================

    private static Mob salamander(ServerLevel level, double x, double y, double z, float yaw, int variant, Consumer<EmberSalamanderEntity> setup) {
        return creature(level, BiomeBeasts.EMBER_SALAMANDER.get(), x, y, z, yaw, s -> {
            s.setVariant(variant);
            setup.accept(s);
        });
    }

    private static Mob snapper(ServerLevel level, double x, double y, double z, float yaw, Consumer<MireSnapperEntity> setup) {
        return creature(level, BiomeBeasts.MIRE_SNAPPER.get(), x, y, z, yaw, setup);
    }

    private static Mob flamingo(ServerLevel level, double x, double y, double z, float yaw, int variant, Consumer<FlamingoEntity> setup) {
        return creature(level, BiomeBeasts.FLAMINGO.get(), x, y, z, yaw, f -> {
            f.setVariant(variant);
            setup.accept(f);
        });
    }

    private static Mob butterfly(ServerLevel level, double x, double y, double z, float yaw, int variant, boolean perched) {
        return creature(level, BiomeBeasts.BUTTERFLY.get(), x, y, z, yaw, b -> {
            b.setVariant(variant);
            b.setPerched(perched);
        });
    }

    // =====================================================================================
    // Bühnen
    // =====================================================================================

    private static BlockState st(Block block) {
        return block.defaultBlockState();
    }

    /** Aschefelder: Asche, Schlackeboden, Vulkangestein, ein Lavatümpel mit glühendem Rand, Feuerlilien, verkohlte Bäume, Rauch. */
    private static void ash(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(91);
        BlockState rock = ModBlocks.VOLCANIC_ROCK.base().get().defaultBlockState();
        Showcase.fill(level, new BlockPos(cx - 46, y - 4, cz - 30), new BlockPos(cx + 46, y - 2, cz + 58), rock);
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 58; z++) {
                int r = random.nextInt(100);
                BlockState top = r < 45 ? st(ModBlocks.ASH_BLOCK.get()) : r < 70 ? st(ModBlocks.CINDER_SOIL.get()) : r < 88 ? rock
                        : r < 95 ? ModBlocks.COBBLED_VOLCANIC_ROCK.base().get().defaultBlockState() : st(ModBlocks.GLOWING_VOLCANIC_ROCK.get());
                set(level, x, y - 1, z, top);
            }
        }
        // der Lavatümpel vorn rechts, mit glühendem Rand
        for (int x = cx - 2; x <= cx + 8; x++) {
            for (int z = cz + 2; z <= cz + 10; z++) {
                double d = Math.pow((x - cx - 3.0) / 4.2, 2) + Math.pow((z - cz - 6.0) / 3.4, 2);
                if (d < 1.0) {
                    set(level, x, y - 1, z, st(Blocks.LAVA));
                    set(level, x, y - 2, z, rock);
                } else if (d < 1.45) {
                    set(level, x, y - 1, z, st(ModBlocks.GLOWING_VOLCANIC_ROCK.get()));
                }
            }
        }
        // Feuerlilien links vorn
        for (int i = 0; i < 14; i++) {
            int x = cx - 5 + random.nextInt(6);
            int z = cz + random.nextInt(6);
            if (level.getBlockState(new BlockPos(x, y - 1, z)).is(Blocks.LAVA)) {
                continue;
            }
            set(level, x, y - 1, z, st(ModBlocks.CINDER_SOIL.get()));
            set(level, x, y, z, st(ModBlocks.FIRE_LILY.get()));
        }
        // hinten: Felsnadeln aus Vulkangestein, verkohlte Bäume und Rauch aus zwei Feuern
        for (int i = 0; i < 22; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz + 14 + random.nextInt(40);
            int h = 3 + random.nextInt(z > cz + 30 ? 14 : 6);
            double r0 = 1.2 + random.nextDouble() * 2.0;
            for (int dy = 0; dy < h; dy++) {
                double r = r0 * (1.0 - dy / (h * 1.3));
                for (int dx = -3; dx <= 3; dx++) {
                    for (int dz = -3; dz <= 3; dz++) {
                        if (Math.sqrt(dx * dx + dz * dz) <= r) {
                            set(level, x + dx, y + dy, z + dz, random.nextInt(12) == 0 ? st(ModBlocks.GLOWING_VOLCANIC_ROCK.get()) : rock);
                        }
                    }
                }
            }
        }
        for (int i = 0; i < 10; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = cz - 10 + random.nextInt(40);
            if (Math.abs(x - cx) < 10 && z < cz + 14) {
                continue;
            }
            charred(level, x, y, z, 4 + random.nextInt(4), random);
        }
        for (int[] f : new int[][] {{-9, 22}, {12, 30}}) {
            set(level, cx + f[0], y - 1, cz + f[1], st(Blocks.HAY_BLOCK));
            set(level, cx + f[0], y, cz + f[1], st(Blocks.CAMPFIRE).setValue(CampfireBlock.SIGNAL_FIRE, true));
        }
    }

    /** Ein verkohlter Baum: schwarzer Stamm, zwei, drei kahle Äste. */
    private static void charred(ServerLevel level, int x, int y, int z, int h, RandomSource random) {
        BlockState log = st(ModBlocks.CHARRED_LOG.get());
        for (int dy = 0; dy < h; dy++) {
            set(level, x, y + dy, z, log);
        }
        for (int k = 0; k < 2 + random.nextInt(2); k++) {
            int dx = random.nextInt(3) - 1;
            int dz = dx == 0 ? (random.nextBoolean() ? 1 : -1) : 0;
            int by = y + h - 1 - random.nextInt(2);
            set(level, x + dx, by, z + dz, log);
            set(level, x + dx * 2, by + 1, z + dz * 2, log);
        }
    }

    /** Nebelmoor: Torf, Schlamm, Gras, ein Tümpel mit Seerosen und Schilf, tote Bäume mit hängendem Moos, Mondblumen, Farne. */
    private static void moor(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(92);
        Showcase.fill(level, new BlockPos(cx - 46, y - 4, cz - 30), new BlockPos(cx + 46, y - 2, cz + 58), st(Blocks.DIRT));
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 58; z++) {
                int r = random.nextInt(100);
                BlockState top = r < 40 ? st(Blocks.GRASS_BLOCK) : r < 62 ? st(ModBlocks.PEAT.get()) : r < 82 ? st(Blocks.MUD)
                        : r < 92 ? st(Blocks.COARSE_DIRT) : st(Blocks.MOSS_BLOCK);
                set(level, x, y - 1, z, top);
                int p = random.nextInt(100);
                if (top.is(Blocks.GRASS_BLOCK) && p < 20 && Math.abs(x - cx) + Math.abs(z - cz - 3) > 6) {
                    set(level, x, y, z, st(p < 8 ? Blocks.FERN : Blocks.SHORT_GRASS));
                } else if (top.is(Blocks.GRASS_BLOCK) && p < 22) {
                    set(level, x, y, z, st(ModBlocks.MOONFLOWER.get()));
                }
            }
        }
        // der Tümpel: flach am Rand, in der Mitte zwei Blöcke tief, Seerosen; Schilf am Ufer
        for (int x = cx - 5; x <= cx + 6; x++) {
            for (int z = cz + 2; z <= cz + 11; z++) {
                double d = Math.pow((x - cx - 0.5) / 5.4, 2) + Math.pow((z - cz - 6.5) / 4.4, 2);
                if (d < 1.0) {
                    set(level, x, y, z, st(Blocks.AIR));
                    set(level, x, y - 1, z, st(Blocks.WATER));
                    set(level, x, y - 2, z, d < 0.35 ? st(Blocks.WATER) : st(Blocks.MUD));
                    if (d < 0.35) {
                        set(level, x, y - 3, z, st(Blocks.MUD));
                    }
                    if (random.nextInt(9) == 0 && d > 0.2) {
                        set(level, x, y, z, st(Blocks.LILY_PAD));
                    }
                } else if (d < 1.5 && random.nextInt(3) == 0 && (z > cz + 6 || Math.abs(x - cx) > 3)) {
                    reed(level, x, y, z);
                }
            }
        }
        // tote Bäume mit hängendem Moos
        for (int i = 0; i < 12; i++) {
            int x = cx - 42 + random.nextInt(85);
            int z = cz - 6 + random.nextInt(50);
            if (Math.abs(x - cx) < 9 && z < cz + 14) {
                continue;
            }
            deadTree(level, x, y, z, 5 + random.nextInt(5), random);
        }
    }

    private static void reed(ServerLevel level, int x, int y, int z) {
        BlockState reeds = st(ModBlocks.REEDS.get());
        level.setBlock(new BlockPos(x, y, z), reeds.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
        level.setBlock(new BlockPos(x, y + 1, z), reeds.setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
    }

    private static void deadTree(ServerLevel level, int x, int y, int z, int h, RandomSource random) {
        BlockState log = st(Blocks.DARK_OAK_LOG);
        for (int dy = 0; dy < h; dy++) {
            set(level, x, y + dy, z, log);
        }
        for (int k = 0; k < 3; k++) {
            int dx = random.nextInt(3) - 1;
            int dz = dx == 0 ? (random.nextBoolean() ? 1 : -1) : 0;
            int by = y + h - 2 - random.nextInt(3);
            set(level, x + dx, by, z + dz, log.setValue(BlockStateProperties.AXIS, dx != 0 ? net.minecraft.core.Direction.Axis.X
                    : net.minecraft.core.Direction.Axis.Z));
            set(level, x + dx * 2, by + 1, z + dz * 2, log);
            for (int m = 1; m <= 2; m++) {
                set(level, x + dx * 2, by + 1 - m, z + dz * 2, st(Blocks.PALE_HANGING_MOSS));
            }
        }
    }

    /** Korallenküste: weißer Sand, eine türkise, flache Lagune (ein Block tief), weiter draußen Riffe; Palmen, Perlmuttfelsen. */
    private static void lagoon(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(93);
        BlockState sand = st(Blocks.SAND);
        Showcase.fill(level, new BlockPos(cx - 120, y - 5, cz - 30), new BlockPos(cx + 120, y - 5, cz + 170), st(Blocks.SANDSTONE));
        Showcase.fill(level, new BlockPos(cx - 120, y - 4, cz - 30), new BlockPos(cx + 120, y - 1, cz + 170), st(Blocks.WATER));
        for (int x = cx - 46; x <= cx + 46; x++) {
            int dx = x - cx;
            int shore = cz + (int) Math.round(1.2 * Math.sin(dx * 0.2) + 1.5 * Math.sin(dx * 0.07 + 0.6));
            for (int z = cz - 30; z <= cz + 60; z++) {
                if (z <= shore) {
                    Showcase.fill(level, new BlockPos(x, y - 4, z), new BlockPos(x, y - 1, z), sand);
                } else if (z <= cz + 16) {
                    // die Lagune: ein Block tief über Sand
                    Showcase.fill(level, new BlockPos(x, y - 4, z), new BlockPos(x, y - 2, z), sand);
                    if (random.nextInt(14) == 0) {
                        set(level, x, y - 1, z, st(Blocks.SEAGRASS));
                    }
                } else {
                    // draußen: drei Blöcke tief, Korallen und Seegras
                    Showcase.fill(level, new BlockPos(x, y - 4, z), new BlockPos(x, y - 4, z), sand);
                    int r = random.nextInt(100);
                    if (r < 6) {
                        set(level, x, y - 3, z, st(r < 2 ? Blocks.BRAIN_CORAL_BLOCK : r < 4 ? Blocks.FIRE_CORAL_BLOCK : Blocks.TUBE_CORAL_BLOCK));
                    } else if (r < 14) {
                        set(level, x, y - 3, z, st(Blocks.SEAGRASS));
                    }
                }
            }
        }
        // Perlmuttfelsen in der Lagune
        for (int[] b : new int[][] {{-8, 6}, {9, 12}, {-14, 13}, {15, 4}}) {
            set(level, cx + b[0], y - 1, cz + b[1], st(ModBlocks.NACRE_BLOCK.get()));
            set(level, cx + b[0] + 1, y - 1, cz + b[1], st(ModBlocks.NACRE_BLOCK.get()));
            set(level, cx + b[0], y, cz + b[1], st(ModBlocks.NACRE_BLOCK.get()));
        }
        // Palmen an den Seiten
        for (int[] p : new int[][] {{-11, -4}, {-16, 2}, {12, -3}, {18, 1}, {-22, -8}, {24, -6}}) {
            palm(level, cx + p[0], y, cz + p[1], 6 + random.nextInt(3), random);
        }
    }

    private static void palm(ServerLevel level, int x, int y, int z, int h, RandomSource random) {
        BlockState log = st(Blocks.JUNGLE_LOG);
        BlockState leaves = st(Blocks.JUNGLE_LEAVES).setValue(BlockStateProperties.PERSISTENT, true);
        int lean = random.nextBoolean() ? 1 : -1;
        int ox = 0;
        for (int dy = 0; dy < h; dy++) {
            if (dy == h / 2 || dy == h - 2) {
                ox += lean;
            }
            set(level, x + ox, y + dy, z, log);
        }
        int top = y + h;
        set(level, x + ox, top, z, leaves);
        for (int[] d : new int[][] {{1, 0}, {-1, 0}, {0, 1}, {0, -1}}) {
            for (int k = 1; k <= 3; k++) {
                set(level, x + ox + d[0] * k, top - (k == 3 ? 1 : 0), z + d[1] * k, leaves);
            }
        }
        for (int[] d : new int[][] {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) {
            set(level, x + ox + d[0], top, z + d[1], leaves);
            set(level, x + ox + d[0] * 2, top - 1, z + d[1] * 2, leaves);
        }
    }

    /** Königsauen: Gras, dicht mit Lavendel, Gartenrosen und Wiesenblumen, hohe Blumen, eine alte Eiche, Birken. */
    private static void meadow(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(94);
        Showcase.fill(level, new BlockPos(cx - 46, y - 4, cz - 30), new BlockPos(cx + 46, y - 2, cz + 58), st(Blocks.DIRT));
        Showcase.fill(level, new BlockPos(cx - 46, y - 1, cz - 30), new BlockPos(cx + 46, y - 1, cz + 58), st(Blocks.GRASS_BLOCK));
        Block[] flowers = {ModBlocks.LAVENDER.get(), ModBlocks.LAVENDER.get(), ModBlocks.GARDEN_ROSE.get(), Blocks.CORNFLOWER, Blocks.OXEYE_DAISY,
                Blocks.POPPY, Blocks.ALLIUM, Blocks.WHITE_TULIP, Blocks.AZURE_BLUET, ModBlocks.LAVENDER.get()};
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 58; z++) {
                int p = random.nextInt(100);
                if (p < 38) {
                    int k = Math.floorMod((x / 3) * 7 + (z / 3) * 13 + random.nextInt(3), flowers.length);
                    set(level, x, y, z, st(flowers[k]));
                } else if (p < 58) {
                    set(level, x, y, z, st(Blocks.SHORT_GRASS));
                } else if (p < 62 && (Math.abs(x - cx) > 4 || z > cz + 7)) {
                    Block tall = random.nextBoolean() ? Blocks.ROSE_BUSH : random.nextBoolean() ? Blocks.LILAC : Blocks.PEONY;
                    level.setBlock(new BlockPos(x, y, z), st(tall).setValue(DoublePlantBlock.HALF, DoubleBlockHalf.LOWER), Block.UPDATE_CLIENTS);
                    level.setBlock(new BlockPos(x, y + 1, z), st(tall).setValue(DoublePlantBlock.HALF, DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
                }
            }
        }
        // vorn: Lavendel und Rosen, wo die Falter sitzen
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = 0; dz <= 3; dz++) {
                set(level, cx + dx, y, cz + dz, st(dx == 1 || dz == 3 ? ModBlocks.GARDEN_ROSE.get() : ModBlocks.LAVENDER.get()));
            }
        }
        Showcase.fill(level, new BlockPos(cx - 2, y, cz - 3), new BlockPos(cx + 2, y + 1, cz - 1), st(Blocks.AIR));
        // eine alte Eiche hinten, Birken an den Seiten
        oak(level, cx + 6, y, cz + 18, random);
        for (int i = 0; i < 14; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = cz + 6 + random.nextInt(45);
            if (Math.abs(x - cx) < 10 && z < cz + 22) {
                continue;
            }
            birch(level, x, y, z, 5 + random.nextInt(3));
        }
    }

    private static void oak(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState log = st(Blocks.OAK_LOG);
        BlockState leaves = st(Blocks.OAK_LEAVES).setValue(BlockStateProperties.PERSISTENT, true);
        for (int dy = 0; dy < 8; dy++) {
            set(level, x, y + dy, z, log);
            set(level, x + 1, y + dy, z, log);
            set(level, x, y + dy, z + 1, log);
            set(level, x + 1, y + dy, z + 1, log);
        }
        for (int dx = -6; dx <= 7; dx++) {
            for (int dz = -6; dz <= 7; dz++) {
                for (int dy = 5; dy <= 12; dy++) {
                    double d = Math.pow(dx - 0.5, 2) / 42.0 + Math.pow(dz - 0.5, 2) / 42.0 + Math.pow(dy - 9.0, 2) / 12.0;
                    if (d < 1.0 && random.nextInt(10) > 0 && level.getBlockState(new BlockPos(x + dx, y + dy, z + dz)).isAir()) {
                        set(level, x + dx, y + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void birch(ServerLevel level, int x, int y, int z, int h) {
        BlockState log = st(Blocks.BIRCH_LOG);
        BlockState leaves = st(Blocks.BIRCH_LEAVES).setValue(BlockStateProperties.PERSISTENT, true);
        for (int dy = 0; dy < h; dy++) {
            set(level, x, y + dy, z, log);
        }
        for (int dy = h - 3; dy <= h + 1; dy++) {
            int r = dy >= h ? 1 : 2;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if ((Math.abs(dx) < r || Math.abs(dz) < r) && level.getBlockState(new BlockPos(x + dx, y + dy, z + dz)).isAir()) {
                        set(level, x + dx, y + dy, z + dz, leaves);
                    }
                }
            }
        }
    }
}
