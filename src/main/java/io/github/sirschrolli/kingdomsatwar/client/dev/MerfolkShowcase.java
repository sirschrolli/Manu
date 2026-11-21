package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidEntity;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidLook;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.MermaidProfession;
import io.github.sirschrolli.kingdomsatwar.entity.merfolk.TritonEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerDimension;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerTerrain;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.merfolk.SeaVillagePiece;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;

/**
 * Nur zur Prüfung des Meervolks (0.93): ein gläsernes Becken in der flachen Testwelt mit allen sechs Gestalten (je ein Beruf)
 * und einer Tritonwache, aus der Nähe und beim Schwimmen; dann ein Meeresdorf im Urmeer – von oben, der Platz mit der
 * Dreizacksäule, der Gesangsturm und die Häuser.
 */
final class MerfolkShowcase {
    private static final int BASE_X = 5000;
    private static final int TANK_X = 30;
    private static final int TANK_Z = 14;
    private static final int TANK_Y = 12;

    private MerfolkShowcase() {}

    // =====================================================================================
    // Das Becken mit allen Gestalten
    // =====================================================================================

    static void tank(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.command("time set 6000"));
        int[] g = {0};
        List<Mob> cast = new ArrayList<>();
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %d %d %d", BASE_X + 15,
                Showcase.ground + 6, TANK_Z + 4))));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.server(level -> {
            g[0] = Showcase.ground;
            build(level, g[0], cast);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(lookRel(BASE_X + 15.0, 5.0, TANK_Z - 1.5, BASE_X + 15.0, 3.4, 6.5));
        shot(steps, "meervolk_reihe");
        // je zwei aus der Nähe (Gesicht auf Augenhöhe)
        for (int pair = 0; pair < 3; pair++) {
            double mx = BASE_X + 3.5 + pair * 7.2 + 1.8;
            steps.add(lookRel(mx, 3.5, 10.2, mx, 3.0, 6.5));
            shot(steps, "meervolk_nah_" + (pair + 1));
        }
        steps.add(lookRel(BASE_X + 25.0, 3.6, 9.8, BASE_X + 26.0, 3.0, 6.5));
        shot(steps, "meervolk_wache");
        // eine Meerjungfrau schwimmt quer durchs Becken (waagrecht, der Schwanz schlägt)
        steps.add(Step.server(level -> {
            Mob swimmer = cast.get(2);
            swimmer.teleportTo(BASE_X + 1.5, g[0] + 3.0, 9.8);
            swimmer.setYRot(-90.0f);
            swimmer.setYBodyRot(-90.0f);
            swimmer.setYHeadRot(-90.0f);
        }));
        steps.add(lookRel(BASE_X + 9.5, 4.2, TANK_Z - 1.0, BASE_X + 9.0, 4.0, 9.8));
        steps.add((minecraft, ticks) -> {
            var server = minecraft.getSingleplayerServer();
            server.execute(() -> {
                Mob swimmer = cast.get(2);
                swimmer.teleportTo(swimmer.getX() + 0.16, swimmer.getY() + Math.sin(ticks * 0.2) * 0.01, swimmer.getZ());
                swimmer.setYRot(-90.0f);
                swimmer.setYBodyRot(-90.0f);
                swimmer.setYHeadRot(-90.0f);
            });
            return ticks >= 46;
        });
        steps.add(Step.screenshot("meervolk_schwimmen"));
        steps.add(Step.waitTicks(10));
    }

    private static Step lookRel(double x, double dy, double z, double tx, double tdy, double tz) {
        return Step.once(minecraft -> {
            double gy = Showcase.ground;
            double lx = tx - x;
            double ly = tdy - dy;
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in minecraft:overworld run tp @s %.2f %.2f %.2f %.1f %.1f",
                    x, gy + dy - 1.62, z, yaw, pitch));
        });
    }

    private static void shot(Deque<Step> steps, String name) {
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot(name));
    }

    private static void set(ServerLevel level, BlockPos pos, Block block) {
        level.setBlock(pos, block.defaultBlockState(), Block.UPDATE_CLIENTS);
    }

    private static void build(ServerLevel level, int ground, List<Mob> cast) {
        for (int x = -1; x <= TANK_X; x++) {
            for (int z = -1; z <= TANK_Z; z++) {
                boolean wall = x == -1 || z == -1 || x == TANK_X || z == TANK_Z;
                BlockPos foot = new BlockPos(BASE_X + x, ground - 1, z);
                set(level, foot, wall ? Blocks.GLASS : Blocks.SAND);
                for (int y = 0; y < TANK_Y; y++) {
                    set(level, foot.above(1 + y), wall ? Blocks.GLASS : Blocks.WATER);
                }
            }
        }
        // ein wenig Grund: Seegras, Korallen, Meeresgurken
        net.minecraft.util.RandomSource random = net.minecraft.util.RandomSource.create(930L);
        Block[] plants = {Blocks.SEAGRASS, Blocks.SEAGRASS, Blocks.TUBE_CORAL, Blocks.BRAIN_CORAL_FAN, Blocks.FIRE_CORAL, Blocks.HORN_CORAL_FAN};
        for (int k = 0; k < 90; k++) {
            BlockPos p = new BlockPos(BASE_X + random.nextInt(TANK_X), ground, random.nextInt(TANK_Z));
            if (level.getBlockState(p).is(Blocks.WATER)) {
                Block plant = plants[random.nextInt(plants.length)];
                var state = plant.defaultBlockState();
                if (state.hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED)) {
                    state = state.setValue(net.minecraft.world.level.block.state.properties.BlockStateProperties.WATERLOGGED, true);
                }
                level.setBlock(p, state, Block.UPDATE_CLIENTS);
            }
        }
        // die sechs Gestalten nebeneinander, je ein Beruf; rechts die Wache
        MermaidLook[] looks = MermaidLook.values();
        MermaidProfession[] jobs = {MermaidProfession.PEARL_DIVER, MermaidProfession.CORAL_GARDENER, MermaidProfession.TIDE_SINGER,
                MermaidProfession.SHELL_BUILDER, MermaidProfession.PEARL_DIVER, MermaidProfession.TIDE_SINGER};
        for (int i = 0; i < looks.length; i++) {
            MermaidEntity mermaid = ModEntities.MERMAID.get().create(level, EntitySpawnReason.COMMAND);
            mermaid.setProfession(jobs[i]);
            mermaid.setLook(looks[i]);
            place(level, mermaid, BASE_X + 3.5 + i * 3.6, ground + 2.0, 6.5, cast);
        }
        TritonEntity triton = ModEntities.TRITON.get().create(level, EntitySpawnReason.COMMAND);
        triton.setLook(MermaidLook.GOLD);
        place(level, triton, BASE_X + 26.0, ground + 2.0, 6.5, cast);
        KingdomsAtWar.LOGGER.info("[Showcase] Meervolk im Becken: {}", cast.size());
    }

    private static void place(ServerLevel level, Mob mob, double x, double y, double z, List<Mob> cast) {
        mob.snapTo(x, y, z, 0.0f, 0.0f);
        mob.setYBodyRot(0.0f);
        mob.setYHeadRot(0.0f);
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        level.addFreshEntity(mob);
        cast.add(mob);
    }

    // =====================================================================================
    // Ein Meeresdorf im Urmeer
    // =====================================================================================

    static void village(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        BlockPos[] center = new BlockPos[1];
        List<BlockPos[]> spots = new ArrayList<>();
        boolean[] ready = new boolean[1];
        steps.add(Step.server(overworld -> {
            ServerLevel level = overworld.getServer().getLevel(UrmeerDimension.LEVEL);
            if (level == null) {
                ready[0] = true;
                return;
            }
            var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.SEA_VILLAGE_STRUCTURE);
            // nicht jede Stelle trägt ein Dorf (zu flach, zu uneben) – die nächsten Kandidaten der Reihe nach prüfen
            BlockPos origin = new BlockPos(0, UrmeerTerrain.SEA_LEVEL, 0);
            for (int tries = 0; tries < 12 && center[0] == null; tries++) {
                var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), origin, 64, false);
                if (found == null) {
                    break;
                }
                BlockPos pos = found.getFirst();
                var chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
                StructureStart start = chunk.getStartForStructure(structure.value());
                if (start != null && start.isValid() && start.getPieces().get(0) instanceof SeaVillagePiece piece) {
                    center[0] = piece.center();
                    spots.addAll(piece.showcaseSpots());
                    // alle Chunks des Dorfs fertig erzeugen
                    for (int dx = -3; dx <= 3; dx++) {
                        for (int dz = -3; dz <= 3; dz++) {
                            level.getChunk((center[0].getX() >> 4) + dx, (center[0].getZ() >> 4) + dz);
                        }
                    }
                    KingdomsAtWar.LOGGER.info("[Showcase] Meeresdorf bei {} ({} Häuser)", center[0], spots.size());
                } else {
                    origin = pos.offset(400, 0, 0);
                }
            }
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 12000));
        String dim = "kingdomsatwar:urmeer";
        int sea = UrmeerTerrain.SEA_LEVEL;
        // von schräg oben über das ganze Dorf
        steps.add(villageLook(center, c -> new double[] {c.getX() + 27.5, Math.min(sea - 2.0, c.getY() + 21.0), c.getZ() + 27.5,
                c.getX() + 0.5, c.getY() + 2.0, c.getZ() + 0.5}, dim));
        villageShot(steps, "meeresdorf_ueberblick");
        steps.add(villageLook(center, c -> new double[] {c.getX() - 17.5, Math.min(sea - 2.0, c.getY() + 19.0), c.getZ() + 17.5,
                c.getX() + 0.5, c.getY() + 2.0, c.getZ() + 0.5}, dim));
        villageShot(steps, "meeresdorf_ueberblick_2");
        // der Platz mit der Dreizacksäule
        steps.add(villageLook(center, c -> new double[] {c.getX() + 10.5, c.getY() + 5.0, c.getZ() + 6.5, c.getX() + 0.5, c.getY() + 6.0,
                c.getZ() + 0.5}, dim));
        villageShot(steps, "meeresdorf_platz");
        // der Gesangsturm und zwei Häuser, je vom Platz vor dem Eingang aus
        for (int i = 0; i < 3; i++) {
            int index = i;
            steps.add(Step.once(minecraft -> {
                if (spots.size() <= index || center[0] == null) {
                    return;
                }
                BlockPos house = spots.get(index)[0];
                BlockPos front = spots.get(index)[1];
                double dx = front.getX() - house.getX();
                double dz = front.getZ() - house.getZ();
                double len = Math.max(1.0, Math.hypot(dx, dz));
                double cx = house.getX() + 0.5 + dx / len * (index == 0 ? 15.0 : 11.0) + dz / len * 3.0;
                double cz = house.getZ() + 0.5 + dz / len * (index == 0 ? 15.0 : 11.0) - dx / len * 3.0;
                double cy = house.getY() + (index == 0 ? 7.0 : 4.5);
                double ty = house.getY() + (index == 0 ? 7.0 : 2.5);
                float yaw = (float) (-Math.atan2(house.getX() + 0.5 - cx, house.getZ() + 0.5 - cz) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(ty - cy, Math.hypot(house.getX() + 0.5 - cx, house.getZ() + 0.5 - cz)) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", dim, cx,
                        cy - 1.62, cz, yaw, pitch));
            }));
            villageShot(steps, index == 0 ? "meeresdorf_turm" : "meeresdorf_haus_" + index);
        }
    }

    private static Step villageLook(BlockPos[] center, java.util.function.Function<BlockPos, double[]> where, String dim) {
        return Step.once(minecraft -> {
            if (center[0] == null) {
                return;
            }
            double[] v = where.apply(center[0]);
            double lx = v[3] - v[0];
            double ly = v[4] - v[1];
            double lz = v[5] - v[2];
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", dim, v[0],
                    v[1] - 1.62, v[2], yaw, pitch));
        });
    }

    private static void villageShot(Deque<Step> steps, String name) {
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(900));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot(name));
    }
}
