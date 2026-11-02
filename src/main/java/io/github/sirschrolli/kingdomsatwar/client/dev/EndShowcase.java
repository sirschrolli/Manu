package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.end.Leerwandler;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung der Wesen des Ends (E5c): Leerwandler auf Endstein zwischen Chorus und Purpursäulen – einer trägt
 * einen Grasblock, einer ist zornig und reißt das Maul auf –, ein Wirrwandler und Leermilben.
 */
final class EndShowcase {
    private static final int OZ = 1500;
    private static final int X0 = 40;

    private EndShowcase() {}

    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double z, float yaw, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("kein " + type.getDescriptionId());
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.snapTo(x, Showcase.ground, z, yaw, 0.0f);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setup.accept(mob);
        level.addFreshEntity(mob);
        return mob;
    }

    /** Endstein mit Chorus, Purpursäulen mit Endstäben, dahinter eine Obsidiansäule. */
    private static void stage(ServerLevel level) {
        int g = Showcase.ground;
        int x0 = X0;
        Showcase.fill(level, new BlockPos(x0 - 16, g - 3, OZ - 30), new BlockPos(x0 + 34, g - 1, OZ + 22), Blocks.END_STONE.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 16, g, OZ - 30), new BlockPos(x0 + 34, g + 24, OZ + 22), Blocks.AIR.defaultBlockState());
        for (int x = x0 - 14; x <= x0 + 32; x++) {
            for (int z = OZ - 28; z <= OZ + 20; z++) {
                if (Math.floorMod(x * 31 + z * 17 + x * z, 29) == 0) {
                    level.setBlock(new BlockPos(x, g - 1, z), Blocks.END_STONE_BRICKS.defaultBlockState(), 2);
                }
            }
        }
        BlockState purpur = Blocks.PURPUR_PILLAR.defaultBlockState();
        for (int x = x0 - 8; x <= x0 + 26; x += 11) {
            Showcase.fill(level, new BlockPos(x, g, OZ - 8), new BlockPos(x, g + 5, OZ - 8), purpur);
            level.setBlock(new BlockPos(x, g + 6, OZ - 8), Blocks.END_ROD.defaultBlockState(), 2);
        }
        Showcase.fill(level, new BlockPos(x0 + 5, g, OZ - 22), new BlockPos(x0 + 8, g + 18, OZ - 19), Blocks.OBSIDIAN.defaultBlockState());
        // Chorus: Stängel mit Blüten
        for (int[] c : new int[][] {{-5, -4, 3}, {2, -6, 4}, {17, -5, 3}, {24, -3, 2}, {12, -12, 5}}) {
            int x = x0 + c[0];
            int z = OZ + c[1];
            for (int y = 0; y < c[2]; y++) {
                level.setBlock(new BlockPos(x, g + y, z), Blocks.CHORUS_PLANT.defaultBlockState(), 2);
            }
            level.setBlock(new BlockPos(x, g + c[2], z), Blocks.CHORUS_FLOWER.defaultBlockState(), 2);
        }
    }

    static void creatures(Deque<Step> steps) {
        steps.add(Step.command("difficulty easy"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 18000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            stage(level);
            double x = X0;
            spawn(level, ModEntities.ENDERMAN.get(), x + 4.5, OZ - 1.5, 15, m -> m.setLook(Leerwandler.Look.LEERE));
            spawn(level, ModEntities.ENDERMAN.get(), x + 9.0, OZ - 2.5, 350, m -> {
                m.setLook(Leerwandler.Look.LEERE);
                m.setCarriedBlock(Blocks.GRASS_BLOCK.defaultBlockState());
            });
            spawn(level, ModEntities.ENDERMITE.get(), x + 12.5, OZ + 2.0, 200, m -> { });
            spawn(level, ModEntities.ENDERMAN.get(), x + 13.5, OZ - 1.0, 10, m -> {
                m.setLook(Leerwandler.Look.LEERE);
                m.addTag("zornig");
            });
            spawn(level, ModEntities.ENDERMAN.get(), x + 19.0, OZ - 2.0, 340, m -> m.setLook(Leerwandler.Look.WIRR));
            spawn(level, ModEntities.ENDERMITE.get(), x + 6.5, OZ + 2.5, 160, m -> { });
            spawn(level, ModEntities.ENDERMITE.get(), x + 7.4, OZ + 3.4, 250, m -> { });
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(60)));
        shot(steps, "end_walkers", X0 + 11.5, 3.0, 2.0, 13.0, true);
        shot(steps, "end_walker_close", X0 + 4.5, 2.9, 2.3, 4.2, false);
        // erst jetzt (die Chunks sind geladen) wird er zornig – der Zorn wird nicht gespeichert
        steps.add(Step.server(level -> {
            var box = new net.minecraft.world.phys.AABB(new BlockPos(X0, Showcase.ground, OZ)).inflate(40.0);
            var mites = level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.creature.end.Leermilbe.class, box);
            for (Leerwandler wandler : level.getEntitiesOfClass(Leerwandler.class, box, w -> w.entityTags().contains("zornig"))) {
                if (!mites.isEmpty()) {
                    wandler.setTarget(mites.getFirst());               // zornig: der Kopf hebt sich vom Kiefer
                }
            }
        }));
        steps.add(Step.waitTicks(10));
        shot(steps, "end_walker_angry", X0 + 13.5, 2.9, 2.3, 4.2, false);
        shot(steps, "end_walker_warped", X0 + 19.0, 2.9, 2.2, 4.6, false);
        shot(steps, "end_mites", X0 + 7.0, 1.0, 0.2, 5.8, false);
        steps.add(Step.command("time set 6000"));
        shot(steps, "end_walkers_day", X0 + 11.5, 3.0, 2.0, 13.0, false);
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamerule spawn_mobs true"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /** Die Kamera steht {@code distance} Blöcke südlich, in Augenhöhe {@code eye}, und blickt nach Norden auf die Höhe {@code target}. */
    private static void shot(Deque<Step> steps, String name, double x, double eye, double target, double distance, boolean first) {
        float pitch = (float) Math.toDegrees(Math.atan2(eye - target, distance));
        steps.add(Showcase.tp(x, eye - 1.62, OZ - 1 + 0.5 + distance, 180, pitch));
        if (first) {
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.waitTicks(60));
        } else {
            steps.add(Step.waitTicks(30));
        }
        steps.add(Step.screenshot(name));
    }
}
