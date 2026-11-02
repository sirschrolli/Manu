package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageArenaBuilder;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;

/**
 * Vorführung der Arkanen Sphäre: für jedes Element die Arena mit dem Erzmagier in ihrer Mitte – von schräg oben, vom
 * Startplatz der Kämpfer und von unten (Felszapfen, Inseln, Wolkenmeer).
 */
final class ArenaShowcase {
    private static final String DIM = ArcaneSphere.LEVEL.identifier().toString();

    private ArenaShowcase() {}

    static void arenas(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        int i = 0;
        for (DragonType element : DragonType.values()) {
            BlockPos center = ArenaFights.arenaCenter(40 + i);
            boolean[] ready = {false};
            steps.add(Step.server(level -> {
                ServerLevel arena = level.getServer().getLevel(ArcaneSphere.LEVEL);
                if (arena == null) {
                    KingdomsAtWar.LOGGER.error("[Showcase] Keine Arkane Sphäre");
                    return;
                }
                for (long[] chunk : MageArenaBuilder.chunks(center)) {
                    arena.getChunk((int) chunk[0], (int) chunk[1]);
                }
                MageArenaBuilder.build(arena, center, element);
                MageBossEntity mage = ModEntities.MAGES.get(element).get().create(arena, EntitySpawnReason.COMMAND);
                if (mage != null) {
                    mage.snapTo(center.getX() + 0.5, center.getY() + 1.35, center.getZ() + 0.5, 180.0f, 0.0f);
                    mage.setHome(center.above());
                    mage.setNoAi(true);
                    mage.setPersistenceRequired();
                    arena.addFreshEntity(mage);
                }
                ready[0] = true;
            }));
            steps.add(Step.until(() -> ready[0], 2000));
            String name = "arena_" + element.id();
            view(steps, center, 24, 13, 27, 0, 2, 0, name + "_0", true);
            view(steps, center, 0, 1, 13, 0, 2.2, 0, name + "_1", false);
            view(steps, center, -34, -16, 30, 0, -9, 0, name + "_2", false);
            i++;
        }
        steps.add(Step.hud(true));
    }

    /** Kamera (Auge) relativ zur Arenamitte mit Blick auf einen Punkt relativ zur Mitte, dann ein Bild. */
    private static void view(Deque<Step> steps, BlockPos center, double x, double y, double z, double tx, double ty, double tz, String name,
            boolean first) {
        double ex = center.getX() + 0.5 + x;
        double ey = center.getY() + 1 + y;
        double ez = center.getZ() + 0.5 + z;
        double lx = tx - x;
        double ly = ty - y;
        double lz = tz - z;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", DIM, ex, ey - 1.62, ez, yaw, pitch))));
        steps.add(Step.waitTicks(first ? 120 : 40));
        steps.add(Step.waitForChunks(first ? 800 : 300));
        steps.add(Step.screenshot(name));
    }
}
