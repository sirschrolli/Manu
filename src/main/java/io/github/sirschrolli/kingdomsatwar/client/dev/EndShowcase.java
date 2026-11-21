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
        stage(level, true);
    }

    private static void stage(ServerLevel level, boolean pillar) {
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
        if (pillar) {
            Showcase.fill(level, new BlockPos(x0 + 5, g, OZ - 22), new BlockPos(x0 + 8, g + 18, OZ - 19), Blocks.OBSIDIAN.defaultBlockState());
        }
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

    /**
     * Schreinlinge in einer Ecke aus Purpur (wie in einer Endstadt): am Boden, an der Wand und an der Decke, in mehreren
     * Farben, einer späht mit offenem Deckel; davor Schreinfunken.
     */
    static void shulkers(Deque<Step> steps) {
        steps.add(Step.command("difficulty easy"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 18000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            stage(level);
            int g = Showcase.ground;
            int x0 = X0 + 22;
            BlockState purpur = Blocks.PURPUR_BLOCK.defaultBlockState();
            Showcase.fill(level, new BlockPos(x0, g - 1, OZ - 7), new BlockPos(x0 + 10, g - 1, OZ - 1), purpur);
            Showcase.fill(level, new BlockPos(x0, g, OZ - 7), new BlockPos(x0 + 10, g + 6, OZ - 7), purpur);     // Rückwand
            Showcase.fill(level, new BlockPos(x0, g, OZ - 6), new BlockPos(x0, g + 6, OZ - 1), purpur);         // Seitenwand
            Showcase.fill(level, new BlockPos(x0, g + 6, OZ - 6), new BlockPos(x0 + 10, g + 6, OZ - 4), purpur); // Decke
            for (int x = x0 + 2; x <= x0 + 10; x += 4) {
                level.setBlock(new BlockPos(x, g + 5, OZ - 6), Blocks.END_ROD.defaultBlockState()
                        .setValue(net.minecraft.world.level.block.EndRodBlock.FACING, net.minecraft.core.Direction.DOWN), 2);
            }
            shulker(level, x0 + 3.5, g, OZ - 2.5, null, false);                // am Boden, geschlossen
            shulker(level, x0 + 6.5, g, OZ - 3.5, null, true);                 // späht
            shulker(level, x0 + 5.5, g + 2, OZ - 5.5, net.minecraft.world.item.DyeColor.RED, false);         // an der Rückwand
            shulker(level, x0 + 8.5, g + 5, OZ - 5.5, net.minecraft.world.item.DyeColor.LIGHT_BLUE, false);  // an der Decke
            shulker(level, x0 + 1.5, g + 2, OZ - 2.5, net.minecraft.world.item.DyeColor.YELLOW, false);      // an der Seitenwand
            shulker(level, x0 + 9.5, g, OZ - 2.0, net.minecraft.world.item.DyeColor.BLACK, false);
            for (int i = 0; i < 3; i++) {
                var bullet = net.minecraft.world.entity.EntityTypes.SHULKER_BULLET.create(level, EntitySpawnReason.COMMAND);
                bullet.setNoGravity(true);
                bullet.snapTo(x0 + 4.0 + i * 1.6, g + 1.4 + (i % 2) * 0.5, OZ + 0.5, 0, 0);
                bullet.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                level.addFreshEntity(bullet);
            }
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(60)));
        shot(steps, "end_shulkers", X0 + 27.5, 3.2, 2.2, 8.0, true);
        // erst jetzt späht er (das Spähen wird beim Neuladen der Chunks zurückgesetzt)
        steps.add(Step.command("data merge entity @e[type=kingdomsatwar:shulker,tag=spaeht,limit=1] {Peek:100b}"));
        steps.add(Step.waitTicks(20));
        shot(steps, "end_shulker_open", X0 + 28.5, 1.6, 0.6, 3.6, false);
        shot(steps, "end_shulkers_wide", X0 + 27.5, 3.2, 2.2, 8.0, false);
        steps.add(Step.command("time set 6000"));
        shot(steps, "end_shulkers_day", X0 + 27.5, 3.2, 2.2, 8.0, false);
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamerule spawn_mobs true"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /**
     * Der Sternenfresser (das Aussehen des Enderdrachen) über dem Endstein: von vorn, von der Seite, von unten; ein
     * zweiter stirbt und zerfällt in violetten Strahlen.
     */
    static void dragon(Deque<Step> steps) {
        steps.add(Step.command("difficulty easy"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 18000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            stage(level, false);
            dragonAt(level, X0 + 9.0, Showcase.ground + 9.0, OZ - 4.0, "schwebt");
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // von vorn (die Kamera steht im Norden und blickt nach Süden)
        steps.add(Showcase.tp(X0 + 9.0, 11.0, OZ - 30.0, 0, 6));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("end_dragon_front"));
        steps.add(Showcase.tp(X0 + 9.0, 13.0, OZ - 17.0, 0, 16));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("end_dragon_head"));
        // Nahaufnahmen des Kopfes (er hängt etwa sechs Blöcke vor dem Drachen)
        steps.add(Showcase.tp(X0 + 14.5, 12.0, OZ - 15.5, 45, 10));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("end_dragon_portrait"));
        steps.add(Showcase.tp(X0 + 16.0, 11.4, OZ - 10.0, 90, 4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("end_dragon_profile"));
        // von der Seite, schräg von oben und von unten
        steps.add(Showcase.tp(X0 + 33.0, 11.0, OZ + 2.0, 90, 4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("end_dragon_side"));
        steps.add(Showcase.tp(X0 - 6.0, 22.0, OZ + 12.0, 223, 32));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("end_dragon_above"));
        steps.add(Showcase.tp(X0 + 9.0, 1.0, OZ + 14.0, 180, -24));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("end_dragon_below"));
        steps.add(Step.command("time set 6000"));
        steps.add(Showcase.tp(X0 + 9.0, 11.0, OZ - 30.0, 0, 6));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("end_dragon_day"));
        // ein zweiter fliegt (mit Verstand): Flügelschlag, Hals und Schwanz folgen der Flugbahn
        steps.add(Step.command("time set 18000"));
        steps.add(Step.server(level -> {
            var flyer = net.minecraft.world.entity.EntityTypes.ENDER_DRAGON.create(level, EntitySpawnReason.COMMAND);
            flyer.setPersistenceRequired();
            flyer.setFightOrigin(new BlockPos(X0 + 9, Showcase.ground, OZ + 60));
            flyer.snapTo(X0 + 9.0, Showcase.ground + 16.0, OZ + 50.0, 90, 0);
            flyer.addTag("fliegt");
            level.addFreshEntity(flyer);
        }));
        steps.add(Showcase.tp(X0 + 9.0, 18.0, OZ + 20.0, 0, 2));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("end_dragon_flight"));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("end_dragon_flight2"));
        steps.add(Step.command("kill @e[type=minecraft:ender_dragon,tag=fliegt]"));
        steps.add(Showcase.tp(X0 + 9.0, 11.0, OZ - 30.0, 0, 6));
        steps.add(Step.waitTicks(20));
        // er stirbt und zerfällt
        // /kill nähme ihn sofort weg – ohne Lebenskraft aber zerfällt er wie im Kampf
        steps.add(Step.server(level -> {
            for (var dragon : level.getEntitiesOfClass(net.minecraft.world.entity.boss.enderdragon.EnderDragon.class,
                    new net.minecraft.world.phys.AABB(new BlockPos(X0, Showcase.ground, OZ)).inflate(60.0), d -> d.entityTags().contains("schwebt"))) {
                dragon.setHealth(0.0f);
            }
        }));
        steps.add(Step.waitTicks(70));
        steps.add(Step.screenshot("end_dragon_dying"));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("end_dragon_dying2"));
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamerule spawn_mobs true"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.hud(true));
    }

    private static void dragonAt(ServerLevel level, double x, double y, double z, String tag) {
        var dragon = net.minecraft.world.entity.EntityTypes.ENDER_DRAGON.create(level, EntitySpawnReason.COMMAND);
        dragon.setNoAi(true);
        dragon.setPersistenceRequired();
        dragon.snapTo(x, y, z, 0, 0);
        dragon.addTag(tag);
        level.addFreshEntity(dragon);
    }

    private static void shulker(ServerLevel level, double x, double y, double z, net.minecraft.world.item.DyeColor color, boolean open) {
        var shulker = ModEntities.SHULKER.get().create(level, EntitySpawnReason.COMMAND);
        shulker.setNoAi(true);
        shulker.setPersistenceRequired();
        shulker.snapTo(Math.floor(x) + 0.5, y, Math.floor(z) + 0.5, 0, 0);
        if (color != null) {
            shulker.setComponent(net.minecraft.core.component.DataComponents.SHULKER_COLOR, color);
        }
        if (open) {
            shulker.addTag("spaeht");
        }
        level.addFreshEntity(shulker);
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
