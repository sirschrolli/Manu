package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssLanding;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.AmethystClusterBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Vorführung des Abgrunds (0.72): Fernblicke über das Chaos an verstreuten Orten (verschiedene Biome), ein Blick hinauf zu
 * den Rissen am Himmel, eine Ankerinsel, ein frisch geöffneter Abgrundriss in der Leere und ein Hebekristall, der zwei
 * Dorfbewohner emporträgt.
 */
final class AbyssShowcase {
    private static final String ABYSS = "kingdomsatwar:the_abyss";
    private static final String VOID = "kingdomsatwar:the_void";

    private AbyssShowcase() {}

    /** {@code only}: nur „vista“, „sky“ (der Blick hinauf), „anchor“ oder „gate“ – oder alles. */
    static void tour(Deque<Step> steps, String only) {
        BlockPos[] anchor = new BlockPos[1];
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.fov().set(70);
            minecraft.options.renderDistance().set(12);
        }));

        if (only.isEmpty() || only.equals("vista") || only.equals("sky")) {
            // Fernblicke: (Standort, Blickziel) – an weit verstreuten Orten, damit verschiedene Biome ins Bild kommen
            double[][] vistas = {
                    {0, 150, 0, 120, 125, 160},
                    {900, 150, -600, 760, 120, -420},
                    {-1500, 140, 900, -1350, 115, 1080},
                    {2400, 150, 1800, 2240, 120, 1980},
                    {-2600, 150, -2200, -2440, 120, -2020},
                    {1500, 100, -1500, 1620, 150, -1400},
                    {-800, 200, -900, -700, 120, -800},
                    {300, 110, 1200, 450, 112, 1300},
                    {600, 120, 2600, 640, 260, 2690},
            };
            for (int i = only.equals("sky") ? vistas.length - 1 : 0; i < vistas.length; i++) {
                double[] v = vistas[i];
                arrive(steps, ABYSS, v[0], v[1], v[2], v[3], v[4], v[5]);
                steps.add(Step.screenshot(i == vistas.length - 1 ? "abyss_sky" : "abyss_vista_" + (i + 1)));
            }
        }

        if (only.isEmpty() || only.equals("anchor")) {
            // eine Ankerinsel, darauf ein Hebekristall, der zwei Dorfbewohner emporträgt
            arrive(steps, ABYSS, 3000, 150, 2980, 3000, 120, 3000);
            steps.add(Step.server(o -> {
                ServerLevel abyss = o.getServer().getLevel(AbyssGate.THE_ABYSS);
                anchor[0] = AbyssLanding.ensure(abyss, new BlockPos(3000, 120, 3000), Direction.Axis.X);
                BlockPos lift = anchor[0].offset(3, 0, 2);
                abyss.setBlock(lift, AbyssBlocks.LIFT_CRYSTAL.get().defaultBlockState().setValue(AmethystClusterBlock.FACING, Direction.UP),
                        Block.UPDATE_ALL);
                for (int k = 0; k < 2; k++) {
                    Mob villager = ModEntities.VILLAGER.get().create(abyss, EntitySpawnReason.COMMAND);
                    if (villager != null) {
                        villager.snapTo(lift.getX() + 0.5 + (k - 0.5) * 0.8, lift.getY() + 1.0 + k * 3.0, lift.getZ() + 0.5, 0.0f, 0.0f);
                        villager.setPersistenceRequired();
                        abyss.addFreshEntity(villager);
                    }
                }
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.once(minecraft -> { }));
            lookAtAnchor(steps, anchor, -9.0, 4.0, 11.0, 0.0, 3.0, 0.0);
            steps.add(Step.waitTicks(50));
            steps.add(Step.screenshot("abyss_anchor"));
            lookAtAnchor(steps, anchor, 9.0, 1.5, 7.0, 3.0, 7.0, 2.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("abyss_lift"));
        }

        if (only.isEmpty() || only.equals("gate")) {
            // in der Leere: ein Riss in den Abgrund auf einer kleinen Plattform aus Leerenstein
            arrive(steps, VOID, 5000, 121, 4986, 5000, 124, 5000);
            steps.add(Step.server(o -> {
                ServerLevel voidLevel = o.getServer().getLevel(VoidGate.THE_VOID);
                BlockState stone = VoidBlocks.POLISHED_VOID_STONE.base().get().defaultBlockState();
                for (int dx = -8; dx <= 8; dx++) {
                    for (int dz = -8; dz <= 8; dz++) {
                        if (dx * dx + dz * dz <= 64) {
                            voidLevel.setBlock(new BlockPos(5000 + dx, 119, 5000 + dz), stone, Block.UPDATE_CLIENTS);
                        }
                    }
                }
                AbyssGate.ensureThroneGate(voidLevel, new BlockPos(5000, 119, 5028), false);
            }));
            steps.add(Step.waitTicks(100));
            look(steps, VOID, 5000, 120, 5013, 5000, 125, 5000);
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot("abyss_gate"));
        }
    }

    private static void lookAtAnchor(Deque<Step> steps, BlockPos[] anchor, double ox, double oy, double oz, double tx, double ty, double tz) {
        steps.add(Step.once(minecraft -> {
            BlockPos a = anchor[0];
            if (a == null) {
                return;
            }
            double x = a.getX() + 0.5 + ox;
            double y = a.getY() + oy;
            double z = a.getZ() + 0.5 + oz;
            double lx = a.getX() + 0.5 + tx - x;
            double ly = a.getY() + ty - (y + 1.62);
            double lz = a.getZ() + 0.5 + tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", ABYSS,
                    x, y, z, yaw, pitch));
        }));
    }

    private static void arrive(Deque<Step> steps, String dim, double x, double y, double z, double tx, double ty, double tz) {
        look(steps, dim, x, y, z, tx, ty, tz);
        steps.add(Step.waitTicks(40));
        steps.add(settle(1400));
        steps.add(Step.waitTicks(20));
    }

    /** Wartet, bis der Client die Chunks ringsum hat und alles gezeichnet ist (neue Welten erzeugen sich langsam). */
    static Step settle(int maxTicks) {
        int[] last = {-1, -1};
        int[] stable = {0, 0};
        return (minecraft, ticks) -> {
            if (ticks == 0) {
                last[0] = -1;
                last[1] = -1;
                stable[0] = 0;
                stable[1] = 0;
            }
            int sections = minecraft.levelRenderer.visibleSections().size();
            int chunks = minecraft.level == null ? 0 : minecraft.level.getChunkSource().getLoadedChunksCount();
            stable[0] = sections > 0 && sections == last[0] && minecraft.levelRenderer.hasRenderedAllSections() ? stable[0] + 1 : 0;
            stable[1] = chunks == last[1] ? stable[1] + 1 : 0;
            last[0] = sections;
            last[1] = chunks;
            boolean done = ticks > maxTicks || ticks > 40 && stable[0] >= 60 && stable[1] >= 100;
            if (done) {
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Abgrund geladen: Tick {}, sichtbar {}, Chunks {}", ticks,
                        sections, chunks);
            }
            return done;
        };
    }

    /** Kamera mit den Füßen bei (x, y, z) in der Dimension {@code dim}, Blick auf (tx, ty, tz). */
    static void look(Deque<Step> steps, String dim, double x, double y, double z, double tx, double ty, double tz) {
        steps.add(Step.once(minecraft -> lookNow(minecraft, dim, x, y, z, tx, ty, tz)));
    }

    /** Wie {@link #look}, sofort. */
    static void lookNow(net.minecraft.client.Minecraft minecraft, String dim, double x, double y, double z, double tx, double ty, double tz) {
        double lx = tx - x;
        double ly = ty - (y + 1.62);
        double lz = tz - z;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", dim, x, y, z,
                yaw, pitch));
    }
}
