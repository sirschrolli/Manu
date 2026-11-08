package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der neuen großen Drachenangriffe: Jeder der sechs Bosse steht in einer eigenen Arena (in Phase 3, also mit
 * allen Wirbeln, Spalten und Kugeln), vor ihm ein Trupp unverwundbarer Ritter; jeder neue Angriff wird ausgelöst und im
 * besten Moment fotografiert.
 */
final class DragonAttackShowcase {
    private DragonAttackShowcase() {}

    /** Ein Angriff und die Ticks nach dem Auslösen, zu denen fotografiert wird. */
    private record Shot(String attack, int[] at, double forward, double side, double up, double look) {}

    private record Arena(DragonType type, BlockState floor, BlockState rim, List<Shot> shots) {}

    private static final List<Arena> ARENAS = List.of(
            new Arena(DragonType.FIRE, Blocks.BASALT.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), List.of(
                    new Shot("fire_tornado", new int[] {50, 95}, 30.0, 24.0, 10.0, 4.0),
                    new Shot("magma_fissures", new int[] {36, 49}, 24.0, 26.0, 14.0, 0.0))),
            new Arena(DragonType.ICE, Blocks.PACKED_ICE.defaultBlockState(), Blocks.BLUE_ICE.defaultBlockState(), List.of(
                    new Shot("frost_beam", new int[] {40, 56}, 18.0, 26.0, 9.0, 3.0),
                    new Shot("shard_nova", new int[] {22, 33}, 20.0, 22.0, 9.0, 3.0))),
            new Arena(DragonType.LIGHTNING, Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState(), List.of(
                    new Shot("ball_lightning", new int[] {40, 70}, 24.0, 24.0, 9.0, 3.0),
                    new Shot("storm_cage", new int[] {45, 73}, 26.0, 22.0, 10.0, 0.0))),
            new Arena(DragonType.NATURE, Blocks.MOSS_BLOCK.defaultBlockState(), Blocks.OAK_LOG.defaultBlockState(), List.of(
                    new Shot("thorn_wave", new int[] {24, 40}, 22.0, 22.0, 9.0, 0.0),
                    new Shot("solar_beam", new int[] {24, 38}, 18.0, 24.0, 8.0, 4.0))),
            new Arena(DragonType.EARTH, Blocks.COARSE_DIRT.defaultBlockState(), Blocks.STONE.defaultBlockState(), List.of(
                    new Shot("rolling_boulders", new int[] {26, 38}, 22.0, 22.0, 8.0, 0.0),
                    new Shot("earthsplitter", new int[] {34, 46}, 20.0, 24.0, 9.0, 0.0))),
            new Arena(DragonType.WATER, Blocks.SAND.defaultBlockState(), Blocks.PRISMARINE_BRICKS.defaultBlockState(), List.of(
                    new Shot("geyser_field", new int[] {30, 60}, 24.0, 24.0, 9.0, 1.0),
                    new Shot("water_serpents", new int[] {30, 44}, 22.0, 24.0, 10.0, 3.0))));

    /** Schaubilder für die Übersicht: je ein Angriff aus der Nähe, im besten Moment. */
    private static final List<Arena> HEROES = List.of(
            new Arena(DragonType.FIRE, Blocks.BASALT.defaultBlockState(), Blocks.BLACKSTONE.defaultBlockState(), List.of(
                    new Shot("fire_tornado", new int[] {58}, 26.0, 15.0, 5.5, 5.0))),
            new Arena(DragonType.ICE, Blocks.PACKED_ICE.defaultBlockState(), Blocks.BLUE_ICE.defaultBlockState(), List.of(
                    new Shot("shard_nova", new int[] {33}, 14.0, 15.0, 6.0, 3.0))),
            new Arena(DragonType.LIGHTNING, Blocks.STONE.defaultBlockState(), Blocks.DEEPSLATE.defaultBlockState(), List.of(
                    new Shot("storm_cage", new int[] {73}, 30.0, 15.0, 6.5, 3.0))),
            new Arena(DragonType.WATER, Blocks.SAND.defaultBlockState(), Blocks.PRISMARINE_BRICKS.defaultBlockState(), List.of(
                    new Shot("water_serpents", new int[] {44}, 22.0, 14.0, 6.0, 3.0))));

    static void attacks(Deque<Step> steps, String only) {
        if (only.equals("hero")) {
            run(steps, HEROES, "");
            return;
        }
        run(steps, ARENAS, only);
    }

    private static void run(Deque<Step> steps, List<Arena> arenas, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        int index = 0;
        for (Arena arena : arenas) {
            int ox = 9000 + index * 160;
            int oz = 9000;
            index++;
            if (!only.isEmpty() && !arena.type().id().equals(only)) {
                continue;
            }
            DragonBossEntity[] boss = new DragonBossEntity[1];
            steps.add(Showcase.tp(ox + 0.5, 30, oz - 40.5, 0, 30));
            steps.add(Step.waitTicks(20));
            steps.add(Step.waitForChunks(600));
            steps.add(Step.server(level -> {
                int g = Showcase.ground;
                Showcase.fill(level, new BlockPos(ox - 48, g - 4, oz - 48), new BlockPos(ox + 48, g - 1, oz + 48), arena.floor());
                Showcase.fill(level, new BlockPos(ox - 48, g, oz - 48), new BlockPos(ox + 48, g + 34, oz + 48), Blocks.AIR.defaultBlockState());
                if (arena.type() == DragonType.WATER) {
                    // ein See um Thalassa, die Ritter stehen am Ufer
                    for (BlockPos pos : BlockPos.betweenClosed(ox - 12, g - 4, oz - 12, ox + 12, g - 1, oz + 12)) {
                        if (pos.distSqr(new BlockPos(ox, pos.getY(), oz)) <= 12 * 12) {
                            level.setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                        }
                    }
                }
                for (int i = 0; i < 48; i++) {
                    double a = i * Math.PI * 2 / 48;
                    BlockPos p = new BlockPos(ox + (int) Math.round(Math.cos(a) * 40), g, oz + (int) Math.round(Math.sin(a) * 40));
                    Showcase.fill(level, p, p.above(2 + (i % 3)), arena.rim());
                }
                var type = ModEntities.bossType(arena.type());
                DragonBossEntity b = type.create(level, EntitySpawnReason.COMMAND);
                b.snapTo(ox + 0.5, g - (arena.type() == DragonType.WATER ? 2 : 0), oz + 0.5, 0, 0);
                b.setYBodyRot(0.0f);
                b.setYHeadRot(0.0f);
                b.setHome(new BlockPos(ox, g, oz));
                level.addFreshEntity(b);
                b.cinematic(arena.type() == DragonType.WATER ? DragonBossEntity.MoveMode.WATER : DragonBossEntity.MoveMode.GROUND, 3);
                boss[0] = b;
                // ein Trupp Ritter vor dem Drachen (unverwundbar, ohne eigenen Willen)
                SoldierType[] types = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.SHIELDBEARER, SoldierType.SPEARMAN, SoldierType.KNIGHT};
                for (int i = 0; i < types.length; i++) {
                    SoldierEntity s = ModEntities.SOLDIERS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
                    s.equip();
                    s.snapTo(ox - 8 + i * 4 + 0.5, g, oz + 17.5 + (i % 2) * 2, 180, 0);
                    s.setYBodyRot(180.0f);
                    s.setYHeadRot(180.0f);
                    s.setNoAi(true);
                    s.setPermanentlyInvulnerable(true);
                    s.setPersistenceRequired();
                    level.addFreshEntity(s);
                }
            }));
            steps.add(Step.waitTicks(40));
            for (Shot shot : arena.shots()) {
                steps.add(Step.server(level -> {
                    DragonBossEntity b = boss[0];
                    // zurück in die Ausgangsstellung, damit jeder Angriff gleich beginnt
                    b.snapTo(ox + 0.5, b.getY(), oz + 0.5, 0, 0);
                    b.setYBodyRot(0.0f);
                    BossAttack attack = b.findAttack(shot.attack());
                    var target = level.getEntitiesOfClass(SoldierEntity.class, b.getBoundingBox().inflate(40)).stream()
                            .min(java.util.Comparator.comparingDouble(s -> s.distanceToSqr(b))).orElse(null);
                    if (attack != null && target != null) {
                        b.startAttack(level, attack, target);
                    }
                }));
                steps.add(Step.once(minecraft -> {
                    DragonBossEntity b = boss[0];
                    Vec3 f = Vec3.directionFromRotation(0, 0);
                    Vec3 left = new Vec3(f.z, 0, -f.x);
                    Vec3 base = new Vec3(ox + 0.5, b.getY(), oz + 0.5);
                    Vec3 cam = base.add(f.scale(shot.forward() * 0.45)).add(left.scale(shot.side())).add(0, shot.up(), 0);
                    Vec3 focus = base.add(f.scale(shot.forward() * 0.45)).add(0, shot.look(), 0);
                    Vec3 look = focus.subtract(cam);
                    float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
                    float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x, cam.y, cam.z,
                            yaw, pitch));
                }));
                int waited = 0;
                for (int i = 0; i < shot.at().length; i++) {
                    steps.add(Step.waitTicks(shot.at()[i] - waited));
                    waited = shot.at()[i];
                    steps.add(Step.screenshot("dragon_" + arena.type().id() + "_" + shot.attack() + "_" + i));
                }
                steps.add(Step.until(() -> boss[0].currentAttack() == null, 400));
                steps.add(Step.waitTicks(10));
            }
            steps.add(Step.server(level -> {
                if (boss[0] != null) {
                    boss[0].discard();
                }
                for (SoldierEntity s : level.getEntitiesOfClass(SoldierEntity.class, new net.minecraft.world.phys.AABB(new BlockPos(ox, Showcase.ground, oz))
                        .inflate(60))) {
                    s.discard();
                }
            }));
        }
    }
}
