package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.LanternBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung der Meereswesen (0.61): ein Riff mit Mantarochen, Hai und Leuchtquallen, der Hai mit aufgerissenem
 * Maul, ein Quallenschwarm bei Nacht, der Anglerfisch in der dunklen Tiefe, Riesenkrabben am Strand mit Tiefseelaternen
 * und ein Mantarochen, der aus dem Wasser springt.
 */
final class SeaShowcase {
    private static final int OZ = 1400;
    private static final int WIDTH = 21;
    private static final int HEIGHT = 10;

    private SeaShowcase() {}

    private static int group(int index) {
        return -400 + index * 64;
    }

    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double z, double height, float yaw,
            Consumer<T> setup) {
        @SuppressWarnings("unchecked")
        T[] made = (T[]) new Mob[1];
        Showcase.spawnCreature(level, type, (int) Math.floor(x), (int) Math.floor(z), yaw, mob -> {
            mob.setNoGravity(height > 0.0);
            mob.setPos(x, Showcase.ground + height, z);
            setup.accept(mob);
            made[0] = mob;
        });
        return made[0];
    }

    /** Ein Meeresbecken, die Kamera steht darin unter Wasser: Boden, Rückwand mit Tang, Seegras und Korallen. */
    private static void tank(ServerLevel level, int x0, BlockState back, BlockState floor, boolean reef) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 8, g - 1, OZ - 6), new BlockPos(x0 + WIDTH + 7, g + HEIGHT, OZ + 16), back);
        Showcase.fill(level, new BlockPos(x0 - 8, g - 1, OZ - 6), new BlockPos(x0 + WIDTH + 7, g - 1, OZ + 16), floor);
        Showcase.fill(level, new BlockPos(x0 - 7, g, OZ - 5), new BlockPos(x0 + WIDTH + 6, g + HEIGHT - 1, OZ + 15), Blocks.WATER.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 7, g + HEIGHT, OZ - 5), new BlockPos(x0 + WIDTH + 6, g + HEIGHT, OZ + 15), Blocks.AIR.defaultBlockState());
        if (!reef) {
            return;
        }
        BlockState[] corals = {Blocks.BRAIN_CORAL_BLOCK.defaultBlockState(), Blocks.TUBE_CORAL_BLOCK.defaultBlockState(),
                Blocks.FIRE_CORAL_BLOCK.defaultBlockState(), Blocks.HORN_CORAL_BLOCK.defaultBlockState()};
        for (int dx = -6; dx <= WIDTH + 5; dx++) {
            int h = Math.floorMod(dx * 7 + 3, 5);
            if (h == 0 && dx % 2 == 0) {
                int height = 4 + Math.floorMod(dx, 4);
                for (int dy = 0; dy < height; dy++) {
                    BlockState kelp = dy == height - 1 ? Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, 25)
                            : Blocks.KELP_PLANT.defaultBlockState();
                    level.setBlock(new BlockPos(x0 + dx, g + dy, OZ - 5), kelp, 2);
                }
            } else if (h < 3) {
                level.setBlock(new BlockPos(x0 + dx, g, OZ - 5), Blocks.SEAGRASS.defaultBlockState(), 2);
            } else {
                level.setBlock(new BlockPos(x0 + dx, g, OZ - 5), corals[Math.floorMod(dx, corals.length)], 2);
                level.setBlock(new BlockPos(x0 + dx, g + 1, OZ - 5), Blocks.SEAGRASS.defaultBlockState(), 2);
            }
            if (Math.floorMod(dx * 11, 7) == 0) {
                level.setBlock(new BlockPos(x0 + dx, g, OZ - 3), Blocks.SEAGRASS.defaultBlockState(), 2);
            }
        }
    }

    static void sea(Deque<Step> steps) {
        // der Anglerfisch ist ein Monster (verschwände im Friedlichen); andere Monster erscheinen trotzdem nicht
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        Mob[] shark = new Mob[1];
        Mob[] angler = new Mob[1];
        Mob[] crab = new Mob[1];
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int i = 0; i < 6; i++) {
                int base = group(i);
                Showcase.fill(level, new BlockPos(base - 14, g - 1, OZ - 16), new BlockPos(base + 40, g - 1, OZ + 22), Blocks.SAND.defaultBlockState());
                Showcase.fill(level, new BlockPos(base - 14, g, OZ - 16), new BlockPos(base + 40, g + 14, OZ + 22), Blocks.AIR.defaultBlockState());
            }
            BlockState sand = Blocks.SAND.defaultBlockState();

            // 1. Riff: Mantarochen über dem Grund, Hai im Hintergrund, Quallen in drei Farben
            int b = group(0);
            tank(level, b, Blocks.PRISMARINE_BRICKS.defaultBlockState(), sand, true);
            spawn(level, ModEntities.MANTA_RAY.get(), b + 10.0, OZ + 1.5, 3.4, 145, m -> m.setXRot(-6.0f));
            spawn(level, ModEntities.SHARK.get(), b + 17.0, OZ - 2.5, 2.2, 75, m -> { });
            for (int i = 0; i < 3; i++) {
                int variant = i;
                spawn(level, ModEntities.GLOW_JELLYFISH.get(), b + 2.5 + i * 1.8, OZ + 0.5 - i * 0.7, 5.6 - i * 0.9, 0, m -> m.setVariant(variant));
            }

            // 2. Hai aus der Nähe, das Maul zum Biss geöffnet
            b = group(1);
            tank(level, b, Blocks.PRISMARINE.defaultBlockState(), sand, true);
            shark[0] = spawn(level, ModEntities.SHARK.get(), b + 10.5, OZ + 0.5, 3.0, 105, m -> m.setXRot(-6.0f));

            // 3. Quallenschwarm (bei Nacht): acht Quallen in drei Farben über dunklem Grund
            b = group(2);
            tank(level, b, Blocks.DARK_PRISMARINE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState(), true);
            double[][] spots = {{5.5, 0.5, 6.2}, {8.0, -1.5, 4.6}, {10.5, 1.0, 6.8}, {12.5, -0.5, 3.6}, {14.5, 0.0, 5.8}, {16.5, -2.0, 7.2},
                    {9.5, 2.0, 2.4}, {13.8, 2.2, 7.6}};
            for (int i = 0; i < spots.length; i++) {
                int variant = i % 3;
                double[] s = spots[i];
                spawn(level, ModEntities.GLOW_JELLYFISH.get(), b + s[0], OZ + s[1], s[2], i * 40, m -> m.setVariant(variant));
            }

            // 4. Anglerfisch in der Tiefe (bei Nacht, schwarzer Grund und Felsen)
            b = group(3);
            tank(level, b, Blocks.DEEPSLATE.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), false);
            for (int dx = -6; dx <= WIDTH + 5; dx += 3) {
                level.setBlock(new BlockPos(b + dx, g, OZ - 4), Blocks.BASALT.defaultBlockState(), 2);
                level.setBlock(new BlockPos(b + dx + 1, g, OZ - 2), Blocks.SEA_PICKLE.defaultBlockState(), 2);
            }
            angler[0] = spawn(level, ModEntities.ANGLERFISH.get(), b + 10.5, OZ + 1.0, 2.0, 160, m -> { });

            // 5. Strand: Riesenkrabben (eine klappert), ein Jungtier, Tiefseelaternen an einem Steg
            b = group(4);
            Showcase.fill(level, new BlockPos(b - 6, g - 1, OZ - 12), new BlockPos(b + 26, g - 1, OZ - 4), Blocks.WATER.defaultBlockState());
            Showcase.fill(level, new BlockPos(b - 6, g - 2, OZ - 12), new BlockPos(b + 26, g - 2, OZ - 4), sand);
            crab[0] = spawn(level, ModEntities.GIANT_CRAB.get(), b + 9.0, OZ + 0.5, 0.0, 200, m -> { });
            spawn(level, ModEntities.GIANT_CRAB.get(), b + 12.5, OZ - 1.0, 0.0, 150, m -> { });
            spawn(level, ModEntities.GIANT_CRAB.get(), b + 11.0, OZ + 2.0, 0.0, 190, m -> m.setAge(-24000));
            for (int dx : new int[] {5, 16}) {
                for (int dy = 0; dy < 2; dy++) {
                    level.setBlock(new BlockPos(b + dx, g + dy, OZ - 2), Blocks.OAK_FENCE.defaultBlockState(), 3);
                }
                level.setBlock(new BlockPos(b + dx, g + 2, OZ - 2), SeaBlocks.DEEP_SEA_LANTERN.get().defaultBlockState(), 3);
            }
            level.setBlock(new BlockPos(b + 7, g, OZ - 3), SeaBlocks.DEEP_SEA_LANTERN.get().defaultBlockState().setValue(LanternBlock.HANGING, false), 3);

            // 6. Der Sprung: ein Mantarochen hoch über dem Wasser, darunter Gischt
            b = group(5);
            Showcase.fill(level, new BlockPos(b - 14, g - 6, OZ - 16), new BlockPos(b + 40, g - 1, OZ + 22), Blocks.WATER.defaultBlockState());
            Showcase.fill(level, new BlockPos(b - 14, g - 7, OZ - 16), new BlockPos(b + 40, g - 7, OZ + 22), sand);
            spawn(level, ModEntities.MANTA_RAY.get(), b + 10.5, OZ + 0.5, 2.2, 120, m -> m.setXRot(-22.0f));
            spawn(level, ModEntities.MANTA_RAY.get(), b + 18.0, OZ - 6.0, -1.4, 95, m -> m.setXRot(0.0f));
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));
        shot(steps, "sea_reef", group(0) + 10.5, 6.2, 3.4, 13.0, true);
        shot(steps, "sea_manta_close", group(0) + 10.0, 8.4, 3.4, 6.5, false);
        shot(steps, "sea_shark", group(1) + 10.5, 3.6, 3.2, 7.0, false);
        event(steps, "sea_shark_bite", group(1) + 8.6, 3.5, 3.0, 4.2, () -> shark[0], (byte) 72, 6);
        shot(steps, "sea_crabs", group(4) + 10.8, 2.2, 0.5, 6.0, true);
        event(steps, "sea_crabs_clack", group(4) + 9.4, 1.4, 0.4, 3.4, () -> crab[0], (byte) 75, 10);
        // Sprung: Gischt unter dem Rochen, Kamera knapp über dem Wasser
        steps.add(Step.server(level -> level.sendParticles(ParticleTypes.SPLASH, group(5) + 10.5, Showcase.ground + 0.2, OZ + 0.5, 160, 1.6,
                0.2, 1.6, 0.4)));
        steps.add(Showcase.tp(group(5) + 10.5, 1.0 - 1.62, OZ + 0.5 + 8.0, 180, -9.0f));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.server(level -> level.sendParticles(ParticleTypes.SPLASH, group(5) + 10.5, Showcase.ground + 0.2, OZ + 0.5, 200, 1.8,
                0.2, 1.8, 0.5)));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("sea_manta_leap"));
        // Nacht: Quallen und Anglerfisch
        steps.add(Step.command("time set 18000"));
        steps.add(Step.waitTicks(20));
        shot(steps, "sea_jellyfish_night", group(2) + 11.0, 5.0, 5.0, 11.0, false);
        shot(steps, "sea_jellyfish_close", group(2) + 10.5, 5.6, 6.2, 4.6, false);
        shot(steps, "sea_anglerfish", group(3) + 10.5, 2.9, 2.4, 6.0, true);
        event(steps, "sea_anglerfish_snap", group(3) + 9.4, 2.6, 2.4, 3.8, () -> angler[0], (byte) 73, 4);
        shot(steps, "sea_lanterns_night", group(4) + 10.8, 2.6, 1.2, 9.0, false);
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /**
     * Bild eines Beckens: die Kamera steht {@code distance} Blöcke südlich der Beckenmitte in Augenhöhe {@code eye} und
     * blickt nach Norden auf die Höhe {@code target} (beides über dem Boden).
     */
    private static void shot(Deque<Step> steps, String name, double x, double eye, double target, double distance, boolean first) {
        float pitch = (float) Math.toDegrees(Math.atan2(eye - target, distance));
        steps.add(Showcase.tp(x, eye - 1.62, OZ + 0.5 + distance, 180, pitch));
        if (first) {
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.waitTicks(100));
            steps.add(Step.waitForChunks(200));
        } else {
            steps.add(Step.waitTicks(40));
        }
        steps.add(Step.screenshot(name));
    }

    /**
     * Wie {@link #shot}, aber erst steht die Kamera, dann löst das Wesen eine Bewegung aus (Biss, Klappern …) und das Bild
     * entsteht {@code delay} Ticks danach.
     */
    private static void event(Deque<Step> steps, String name, double x, double eye, double target, double distance,
            java.util.function.Supplier<Mob> mob, byte id, int delay) {
        float pitch = (float) Math.toDegrees(Math.atan2(eye - target, distance));
        steps.add(Showcase.tp(x, eye - 1.62, OZ + 0.5 + distance, 180, pitch));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.server(level -> {
            Mob m = mob.get();
            if (m != null) {
                level.broadcastEntityEvent(m, id);
            }
        }));
        steps.add(Step.waitTicks(delay));
        steps.add(Step.screenshot(name));
    }
}
