package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.Wonders;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Vorführung der Bauwerke von 2.0:
 * <ul>
 *     <li>{@code wlab_<dimension|art>}: die Musterstücke in einer Werkstatt hoch über der Welt ihrer Dimension (Boden,
 *     Wasser oder Lava wie in den Tests) – zum Gestalten;</li>
 *     <li>{@code wonder_<dimension|art>}: die nächsten echten Bauwerke, wie die Welt sie erzeugt (wie /locate).</li>
 * </ul>
 * Jedes Bauwerk wird aus den Blicken seines Eintrags in {@link Wonders} fotografiert.
 */
final class WonderShowcase {
    private static final int SLOTS = 8;
    private static final int LAB_Y = 170;
    private static final int R = 30;

    private WonderShowcase() {}

    private static List<Wonders.Wonder> pick(String which) {
        List<Wonders.Wonder> out = new ArrayList<>();
        for (Wonders.Wonder w : Wonders.all()) {
            if (which.isEmpty() || w.kind().id().equals(which) || w.kind().world().name().toLowerCase(Locale.ROOT).equals(which)) {
                out.add(w);
            }
        }
        return out;
    }

    /** Die Werkstatt: jedes Bauwerk an seinem eigenen Platz, hoch über der Welt seiner Dimension. */
    static void lab(Deque<Step> steps, String which) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(10)));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.command("gamemode spectator"));
        List<Wonders.Wonder> wonders = pick(which);
        for (int i = 0; i < wonders.size(); i++) {
            Wonders.Wonder wonder = wonders.get(i);
            ResourceKey<Level> dim = KawPlaces.level(wonder.kind().world());
            BlockPos origin = new BlockPos(30000 + i * 112, LAB_Y, 0);
            List<double[]> views = new ArrayList<>();
            int[] state = {0};
            steps.add(Step.server(level -> {
                ServerLevel world = level.getServer().getLevel(dim);
                if (world == null) {
                    state[0] = -1;
                    return;
                }
                SitePiece piece = io.github.sirschrolli.kingdomsatwar.worldgen.site.WonderGround.build(world, wonder, origin);
                for (double[] v : wonder.views()) {
                    add(views, piece, v);
                }
                KingdomsAtWar.LOGGER.info("[Showcase] Werkstatt {}: bei {} in {}", wonder.kind().id(), origin, dim.identifier());
                state[0] = 1;
            }));
            steps.add(Step.until(() -> state[0] != 0, 6000));
            shots(steps, views, dim, "wlab_" + wonder.kind().id());
        }
    }

    /** Die echten Bauwerke: das nächste jeder Art ab dem Ursprung ihrer Dimension. */
    static void world(Deque<Step> steps, String which) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(10)));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.command("gamemode spectator"));
        for (Wonders.Wonder wonder : pick(which)) {
            ResourceKey<Level> dim = KawPlaces.level(wonder.kind().world());
            List<double[]> views = new ArrayList<>();
            int[] state = {0};
            steps.add(Step.server(level -> {
                ServerLevel world = level.getServer().getLevel(dim);
                long started = System.currentTimeMillis();
                Holder<Structure> holder = world.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(wonder.kind()));
                var found = world.getChunkSource().getGenerator().findNearestMapStructure(world, HolderSet.direct(holder), new BlockPos(0, 80, 0), 200,
                        false);
                if (found == null) {
                    KingdomsAtWar.LOGGER.info("[Showcase] {} nicht gefunden", wonder.kind().id());
                    state[0] = -1;
                    return;
                }
                ChunkPos chunk = ChunkPos.containing(found.getFirst());
                StructureStart start = world.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
                if (start == null || !start.isValid()) {
                    state[0] = -1;
                    return;
                }
                BoundingBox box = start.getBoundingBox();
                for (int cx = (box.minX() >> 4) - 2; cx <= (box.maxX() >> 4) + 2; cx++) {
                    for (int cz = (box.minZ() >> 4) - 2; cz <= (box.maxZ() >> 4) + 2; cz++) {
                        world.getChunk(cx, cz, ChunkStatus.FULL, true);
                    }
                }
                SitePiece first = null;
                for (StructurePiece p : start.getPieces()) {
                    if (p instanceof SitePiece site) {
                        first = site;
                        break;
                    }
                }
                if (first == null) {
                    state[0] = -1;
                    return;
                }
                for (double[] v : wonder.views()) {
                    add(views, first, v);
                }
                KingdomsAtWar.LOGGER.info("[Showcase] {} bei {} ({}), Daten {}, Fläche {}×{}×{}, {} ms", wonder.kind().id(), first.origin(), first.rotation(),
                        java.util.Arrays.toString(new int[] {first.data(0), first.data(1), first.data(2)}), box.getXSpan(), box.getYSpan(), box.getZSpan(),
                        System.currentTimeMillis() - started);
                state[0] = 1;
            }));
            steps.add(Step.until(() -> state[0] != 0, 9000));
            shots(steps, views, dim, "wonder_" + wonder.kind().id());
        }
    }

    private static void shots(Deque<Step> steps, List<double[]> views, ResourceKey<Level> dim, String name) {
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
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                            dim.identifier(), v[0], v[1], v[2], yaw, pitch));
                }
            }));
            steps.add((minecraft, ticks) -> slot >= views.size() || ticks > (slot == 0 ? 200 : 50));
            steps.add((minecraft, ticks) -> slot >= views.size() || Step.waitForChunks(1500).run(minecraft, ticks));
            Step shot = Step.screenshot(name + "_" + n);
            steps.add((minecraft, ticks) -> slot >= views.size() || shot.run(minecraft, ticks));
        }
    }

    /** Ein Blick in eigenen Koordinaten des Bauteils (gedreht) als Weltkoordinaten. */
    private static void add(List<double[]> views, SitePiece piece, double[] v) {
        BlockPos o = piece.origin();
        Rotation r = piece.rotation();
        double[] eye = turn(v[0], v[2], r);
        double[] look = turn(v[3], v[5], r);
        views.add(new double[] {o.getX() + eye[0] + 0.5, o.getY() + v[1], o.getZ() + eye[1] + 0.5, o.getX() + look[0] + 0.5, o.getY() + v[4],
                o.getZ() + look[1] + 0.5});
    }

    private static double[] turn(double x, double z, Rotation rotation) {
        return switch (rotation) {
            case NONE -> new double[] {x, z};
            case CLOCKWISE_90 -> new double[] {-z, x};
            case CLOCKWISE_180 -> new double[] {-x, -z};
            case COUNTERCLOCKWISE_90 -> new double[] {z, -x};
        };
    }
}
