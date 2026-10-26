package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.army.RecruitScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.client.intro.IntroController;
import io.github.sirschrolli.kingdomsatwar.client.intro.IntroScreen;
import io.github.sirschrolli.kingdomsatwar.client.intro.IntroScript;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAnims;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.BuildingSet;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModCreativeTabs;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.FlowerBlock;
import net.minecraft.world.level.block.FlowerPotBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.neoforged.neoforge.registries.DeferredHolder;

/**
 * Szenen für die automatischen Screenshots (siehe {@link ShowcaseRunner}).
 * Die Welt ist flach; die Bodenhöhe wird zu Beginn gemessen ({@link #ground}).
 */
final class Showcase {
    /** Erste freie Höhe über dem Boden. */
    private static volatile int ground = Integer.MIN_VALUE;

    private Showcase() {}

    static void queue(String scenes, Deque<Step> steps) {
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility()
                .set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.server(level -> ground = level.getHeight(Heightmap.Types.WORLD_SURFACE, 0, 0)));
        steps.add(Step.until(() -> ground != Integer.MIN_VALUE, 200));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("gamerule advance_weather false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamerule send_command_feedback false"));
        steps.add(Step.waitTicks(20));
        for (String scene : scenes.split(",")) {
            switch (scene.trim()) {
                case "blocks" -> blocks(steps);
                case "house" -> house(steps);
                case "samples" -> samples(steps);
                case "intro" -> intro(steps);
                case "items" -> items(steps);
                case "dragons" -> dragons(steps);
                case "soldiers" -> soldiers(steps);
                case "eggs" -> eggs(steps);
                case "equipment" -> equipment(steps);
                case "creatures" -> creatures(steps);
                case "decor" -> decor(steps);
                case "farm" -> farm(steps);
                case "magic" -> magic(steps);
                case "structures" -> structures(steps);
                case "boss" -> boss(steps);
                case "lair" -> lair(steps, DragonType.FIRE);
                case "bossmotion" -> bossMotion(steps, DragonType.FIRE);
                case "seal" -> seal(steps);
                case "lairinfo" -> {
                    double[] nest = new double[4];
                    locateNest(steps, nest, DragonType.FIRE);
                    steps.add(Step.until(() -> nest[3] != 0, 1200));
                    steps.add(Step.waitTicks(100));
                    steps.add(Step.server(level -> debugAshlands(level, nest)));
                    steps.add(Step.waitTicks(40));
                }
                default -> {
                    if (scene.startsWith("lair_")) {
                        lair(steps, DragonType.byId(scene.substring(5)));
                    } else if (scene.startsWith("bossmotion_")) {
                        bossMotion(steps, DragonType.byId(scene.substring(11)));
                    }
                }
            }
        }
        steps.add(Step.waitTicks(40));
        steps.add(Step.quit());
    }

    // =====================================================================================
    // Szenen
    // =====================================================================================

    /** Alle Blöcke der Mod in Reihen auf einer Steinplatte. */
    private static void blocks(Deque<Step> steps) {
        steps.add(Step.server(level -> {
            List<Block> blocks = new ArrayList<>();
            for (DeferredHolder<Block, ? extends Block> holder : ModBlocks.BLOCKS.getEntries()) {
                Block block = holder.value();
                if (!(block instanceof FlowerPotBlock) || block == Blocks.FLOWER_POT) {
                    blocks.add(block);
                }
            }
            int perRow = 16;
            int rows = (blocks.size() + perRow - 1) / perRow;
            fill(level, new BlockPos(-2, ground - 1, -2), new BlockPos(perRow * 2, ground - 1, rows * 3 + 1), Blocks.SMOOTH_STONE.defaultBlockState());
            for (int i = 0; i < blocks.size(); i++) {
                BlockPos pos = new BlockPos((i % perRow) * 2, ground, (i / perRow) * 3);
                place(level, pos, blocks.get(i));
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(tp(15, 12, -9, 0, 42));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(false));
        steps.add(Step.screenshot("blocks_overview"));
        // Nahaufnahmen der ersten und zweiten Hälfte
        steps.add(tp(7, 6, -4, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_1"));
        steps.add(tp(23, 6, -4, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_2"));
        steps.add(tp(7, 6, 8, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_3"));
        steps.add(tp(23, 6, 8, 0, 35));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("blocks_close_4"));
        steps.add(Step.hud(true));
    }

    /** Ein kleines Fachwerkhaus aus Mod-Blöcken – zeigt, wie die Blöcke zusammenwirken. */
    private static void house(Deque<Step> steps) {
        int ox = 60;
        int oz = 0;
        int w = 9;
        int d = 7;
        steps.add(Step.server(level -> {
            BlockState stone = ModBlocks.MEDIEVAL_STONE.base().get().defaultBlockState();
            BlockState beam = ModBlocks.TIMBER_BEAM.get().defaultBlockState();
            BlockState frame = ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            BlockState cross = ModBlocks.TIMBER_FRAME_CROSS.get().defaultBlockState();
            BlockState diagonal = ModBlocks.TIMBER_FRAME_DIAGONAL.get().defaultBlockState();
            BlockState floor = ModBlocks.ROUGH_FIELDSTONE.base().get().defaultBlockState();
            fill(level, new BlockPos(ox - 4, ground - 1, oz - 4), new BlockPos(ox + w + 4, ground - 1, oz + d + 4), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(ox - 1, ground - 1, oz - 1), new BlockPos(ox + w, ground - 1, oz + d), floor);
            // Erdgeschoss aus Stein, Obergeschoss in Fachwerk
            for (int y = 0; y < 7; y++) {
                for (int x = 0; x < w; x++) {
                    for (int z = 0; z < d; z++) {
                        boolean edgeX = x == 0 || x == w - 1;
                        boolean edgeZ = z == 0 || z == d - 1;
                        if (!edgeX && !edgeZ) {
                            continue;
                        }
                        BlockPos pos = new BlockPos(ox + x, ground + y, oz + z);
                        BlockState state;
                        if (y < 3) {
                            state = stone;
                        } else if (edgeX && edgeZ) {
                            state = beam; // Eckpfosten (senkrecht)
                        } else if (y == 3 || y == 6) {
                            // Schwelle und Rähm: liegende Balken entlang der Wand
                            state = beam.setValue(RotatedPillarBlock.AXIS, edgeZ ? Direction.Axis.X : Direction.Axis.Z);
                        } else {
                            int k = (x + z + y) % 3;
                            state = k == 0 ? cross : (k == 1 ? diagonal : frame);
                        }
                        level.setBlock(pos, state, Block.UPDATE_CLIENTS);
                    }
                }
            }
            // Tür und Fenster
            level.setBlock(new BlockPos(ox + 4, ground, oz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(ox + 4, ground + 1, oz), Blocks.AIR.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(ox + 2, ground + 4, oz), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(ox + 6, ground + 4, oz), Blocks.GLASS_PANE.defaultBlockState(), Block.UPDATE_CLIENTS);
            // Satteldach aus roten Dachziegeln (First entlang x)
            BlockState south = ModBlocks.RED_ROOF_TILES.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.SOUTH);
            BlockState north = ModBlocks.RED_ROOF_TILES.stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
            int roofBase = ground + 7;
            for (int step = 0; step <= d / 2; step++) {
                for (int x = -1; x <= w; x++) {
                    BlockPos front = new BlockPos(ox + x, roofBase + step, oz - 1 + step);
                    BlockPos back = new BlockPos(ox + x, roofBase + step, oz + d - step);
                    if (front.getZ() < back.getZ()) {
                        level.setBlock(front, south, Block.UPDATE_CLIENTS);
                        level.setBlock(back, north, Block.UPDATE_CLIENTS);
                    } else {
                        level.setBlock(front, ModBlocks.RED_ROOF_TILES.slab().get().defaultBlockState()
                                .setValue(SlabBlock.TYPE, SlabType.BOTTOM), Block.UPDATE_CLIENTS);
                    }
                }
                // Giebel mit Fachwerk füllen
                for (int z = step; z < d - step; z++) {
                    level.setBlock(new BlockPos(ox, roofBase + step, oz + z), frame, Block.UPDATE_CLIENTS);
                    level.setBlock(new BlockPos(ox + w - 1, roofBase + step, oz + z), frame, Block.UPDATE_CLIENTS);
                }
            }
            // Ein paar Blumen und Feldfrüchte vor dem Haus
            int i = 0;
            for (var flower : ModBlocks.FLOWERS.keySet()) {
                level.setBlock(new BlockPos(ox + 1 + i * 2, ground, oz - 3), flower.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                i++;
            }
            for (int c = 0; c < ModBlocks.CROPS.size(); c++) {
                CropBlock crop = ModBlocks.CROPS.get(c).get();
                for (int z = 0; z < 3; z++) {
                    BlockPos soil = new BlockPos(ox + w + 2 + c, ground - 1, oz + 1 + z);
                    level.setBlock(soil, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(soil.above(), crop.getStateForAge(crop.getMaxAge()), Block.UPDATE_CLIENTS);
                }
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(tp(ox - 7, 6, oz - 12, -30, 18));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(false));
        steps.add(Step.screenshot("house_front"));
        steps.add(tp(ox + w + 9, 8, oz + d + 9, 135, 22));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("house_back"));
        steps.add(Step.hud(true));
    }

    /** Das Erzähl-Intro mit Screenshots zu festen Zeitpunkten (am besten zusammen mit "terrain"). */
    private static void intro(Deque<Step> steps) {
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.once(minecraft -> IntroController.request()));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof IntroScreen, 200));
        // Zeitpunkte für ein Intro von 72,9 s – werden auf die tatsächliche Länge aus intro.json gestreckt
        double[] times = {3.5, 10.0, 17.0, 23.0, 29.0, 35.0, 42.0, 48.0, 54.5, 58.0, 63.0, 70.5};
        double[] length = {0.0};
        for (int i = 0; i < times.length; i++) {
            double at = times[i] / 72.913;
            steps.add(Step.until(() -> {
                if (length[0] <= 0.0) {
                    IntroScript script = IntroScript.load(Minecraft.getInstance().getResourceManager());
                    length[0] = script != null ? script.length() : 72.913;
                }
                return Minecraft.getInstance().gui.screen() instanceof IntroScreen intro && intro.elapsed() >= at * length[0];
            }, 2400));
            steps.add(Step.screenshot("intro_%02d".formatted(i + 1)));
        }
        steps.add(Step.until(() -> !(Minecraft.getInstance().gui.screen() instanceof IntroScreen), 400));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("intro_after"));
    }

    /** Drachen-Boss Pyrrhon: Erwachen aus dem Lavasee, Kampf gegen Soldaten (Odem, Flug, Meteorregen), Tod und Ei. */
    private static void boss(Deque<Step> steps) {
        int ox = 600;
        int oz = 0;
        DragonBossEntity[] boss = new DragonBossEntity[1];
        steps.add(tp(ox, 30, oz - 40, 0, 30));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.waitTicks(40));
        steps.add(Step.command("time set 12600"));
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(ox - 45, ground - 3, oz - 45), new BlockPos(ox + 45, ground - 1, oz + 45), Blocks.BASALT.defaultBlockState());
            fill(level, new BlockPos(ox - 45, ground, oz - 45), new BlockPos(ox + 45, ground + 30, oz + 45), Blocks.AIR.defaultBlockState());
            for (BlockPos pos : BlockPos.betweenClosed(ox - 6, ground - 3, oz - 6, ox + 6, ground - 1, oz + 6)) {
                if (pos.distSqr(new BlockPos(ox, pos.getY(), oz)) <= 30) {
                    level.setBlock(pos, Blocks.LAVA.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            for (int i = 0; i < 40; i++) {
                double a = i * Math.PI * 2 / 40;
                BlockPos p = new BlockPos(ox + (int) Math.round(Math.cos(a) * 30), ground, oz + (int) Math.round(Math.sin(a) * 30));
                fill(level, p, p.above(2 + (i % 3)), Blocks.BLACKSTONE.defaultBlockState());
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(level -> {
            var b = ModEntities.PYRRHON.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            BlockPos home = new BlockPos(ox, ground, oz);
            b.snapTo(ox + 0.5, ground - 11, oz + 0.5, 180, 0);
            b.yBodyRot = 180;
            b.yHeadRot = 180;
            level.addFreshEntity(b);
            b.beginAwakening(level, home);
            boss[0] = b;
        }));
        // Erwachen: von unten, nah am Lavasee
        steps.add(tp(ox + 3.5, 1.6, oz - 19, 5, -14));
        steps.add(Step.hud(false));
        for (double at : new double[] {3.2, 5.0, 6.9, 7.8}) {
            steps.add(Step.until(() -> boss[0] != null && boss[0].getStateTicks() >= at * 20, 400));
            steps.add(Step.screenshot("boss_awaken_" + (int) (at * 10)));
        }
        steps.add(Step.until(() -> boss[0].getBossState() == DragonBossEntity.State.FIGHT, 200));
        // Soldaten treten an
        steps.add(Step.server(level -> {
            SoldierType[] types = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.ARCHER, SoldierType.SWORDSMAN, SoldierType.KNIGHT,
                    SoldierType.ARCHER, SoldierType.CAPTAIN};
            for (int i = 0; i < types.length; i++) {
                SoldierEntity s = ModEntities.SOLDIERS.get(types[i]).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                s.snapTo(ox - 12 + i * 4 + 0.5, ground, oz - 18.5, 0, 0);
                s.finalizeSpawn(level, level.getCurrentDifficultyAt(s.blockPosition()), net.minecraft.world.entity.EntitySpawnReason.COMMAND, null);
                s.setPersistenceRequired();
                level.addFreshEntity(s);
            }
        }));
        steps.add(tp(ox + 2.5, 7, oz - 36, 0, 6));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("boss_fight_bar"));
        steps.add(Step.hud(false));
        // Seitenansicht in voller Länge
        steps.add(near(boss, 0, 30, 3, 4));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("boss_profile"));
        // Odem, von schräg vorn und tief
        steps.add(forceAttack(boss, BossAnims.BREATH));
        steps.add(near(boss, 20, 12, 1.5, 6));
        steps.add(Step.waitTicks(16));
        steps.add(Step.screenshot("boss_breath_charge"));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("boss_breath"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("boss_breath_2"));
        steps.add(Step.waitTicks(40));
        steps.add(forceAttack(boss, BossAnims.ROAR));
        steps.add(near(boss, 16, -4, 1.0, 8));
        steps.add(Step.waitTicks(18));
        steps.add(Step.screenshot("boss_roar"));
        steps.add(Step.waitTicks(30));
        steps.add(forceAttack(boss, BossAnims.BITE));
        steps.add(near(boss, 13, 9, 2.5, 6));
        steps.add(Step.waitTicks(11));
        steps.add(Step.screenshot("boss_bite"));
        steps.add(Step.waitTicks(30));
        steps.add(forceAttack(boss, BossAnims.TAKE_OFF));
        steps.add(near(boss, 26, 22, 2, 6));
        steps.add(Step.waitTicks(16));
        steps.add(Step.screenshot("boss_takeoff"));
        steps.add(Step.waitTicks(50));
        steps.add(tp(ox + 0.5, 1.6, oz + 4.5, 180, -38));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("boss_flying_below"));
        steps.add(Step.server(level -> boss[0].setHealth(boss[0].getMaxHealth() * 0.5f)));
        steps.add(Step.waitTicks(20));
        steps.add(forceAttack(boss, BossAnims.RAIN));
        steps.add(tp(ox + 26.5, 10, oz - 26, 45, 0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("boss_meteor_rain"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("boss_meteor_rain_2"));
        steps.add(Step.waitTicks(60));
        steps.add(forceAttack(boss, BossAnims.LAND));
        steps.add(Step.until(() -> !boss[0].isFlying(), 200));
        steps.add(Step.waitTicks(4));
        steps.add(near(boss, 22, 10, 2, 5));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("boss_landed"));
        // Der Todesstoß
        steps.add(Step.server(level -> {
            boss[0].setHealth(1.0f);
            boss[0].hurtServer(level, level.damageSources().magic(), 100.0f);
        }));
        steps.add(near(boss, 20, 14, 3, 3));
        for (double at : new double[] {0.8, 2.6, 4.5, 7.8}) {
            steps.add(Step.until(() -> boss[0].deathTime >= at * 20, 300));
            steps.add(Step.screenshot("boss_death_" + (int) (at * 10)));
        }
        steps.add(Step.until(() -> boss[0].isRemoved(), 200));
        steps.add(Step.waitTicks(30));
        steps.add(tp(ox + 0.5, 4, oz - 9, 0, 22));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("boss_egg"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.hud(true));
    }

    /** Kamera relativ zum Boss: {@code forward} Blöcke vor ihm, {@code side} nach links, {@code up} hoch, Blick auf seine Mitte. */
    private static Step near(DragonBossEntity[] boss, double forward, double side, double up, double lookHeight) {
        return Step.once(minecraft -> {
            DragonBossEntity b = boss[0];
            if (b == null) {
                return;
            }
            net.minecraft.world.phys.Vec3 f = net.minecraft.world.phys.Vec3.directionFromRotation(0, b.yBodyRot);
            net.minecraft.world.phys.Vec3 left = new net.minecraft.world.phys.Vec3(f.z, 0, -f.x);
            net.minecraft.world.phys.Vec3 cam = b.position().add(f.scale(forward)).add(left.scale(side)).add(0, up, 0);
            net.minecraft.world.phys.Vec3 look = b.position().add(0, lookHeight, 0).subtract(cam);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cam.x, cam.y, cam.z, yaw, pitch));
        });
    }

    private static Step forceAttack(DragonBossEntity[] boss, String name) {
        return Step.server(level -> {
            DragonBossEntity b = boss[0];
            BossAttack attack = b.findAttack(name);
            net.minecraft.world.entity.LivingEntity target = b.getTarget();
            if (target == null) {
                target = level.getEntitiesOfClass(SoldierEntity.class, b.getBoundingBox().inflate(60)).stream().findFirst().orElse(null);
            }
            if (attack != null && target != null) {
                b.startAttack(level, attack, target);
            }
        });
    }

    private static Step forceAttack(DragonBossEntity[] boss, int anim) {
        return Step.server(level -> {
            DragonBossEntity b = boss[0];
            BossAttack attack = b.findAttack(anim);
            net.minecraft.world.entity.LivingEntity target = b.getTarget();
            if (target == null) {
                target = level.getEntitiesOfClass(SoldierEntity.class, b.getBoundingBox().inflate(60)).stream().findFirst().orElse(null);
            }
            if (attack != null && target != null) {
                b.startAttack(level, attack, target);
            }
        });
    }

    /** Mauerproben (3x3 Blöcke) aus der Nähe – zum Vergleichen der Texturen. Kamera schaut nach Norden. */
    private static void samples(Deque<Step> steps) {
        int ox = 120;
        int oz = 0;
        // erst hinfliegen, damit alle Chunks geladen sind, bevor gebaut und fotografiert wird
        steps.add(tp(ox + 60.5, 30.0, oz + 9.0, 180, 30));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.waitTicks(60));
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(ox - 4, ground - 1, oz - 6), new BlockPos(ox + 140, ground - 1, oz + 12), Blocks.GRASS_BLOCK.defaultBlockState());
            // 1) acht Stein-Sets in zwei Gruppen
            List<BuildingSet> stones = List.of(ModBlocks.MEDIEVAL_STONE, ModBlocks.ROUGH_FIELDSTONE, ModBlocks.HEWN_SANDSTONE,
                    ModBlocks.DARK_STONE_BRICKS, ModBlocks.LIMESTONE_BRICKS, ModBlocks.WEATHERED_STONE_BRICKS,
                    ModBlocks.MOSSY_WALL_STONES, ModBlocks.CRACKED_STONE_BLOCKS);
            for (int i = 0; i < stones.size(); i++) {
                int gx = ox + (i / 4) * 20 + (i % 4) * 4;
                wall(level, gx, oz, uniform(stones.get(i).base().get().defaultBlockState()));
            }
            // 2) Holz: Fachwerk, Balken, Säule
            BlockState frame = ModBlocks.TIMBER_FRAME.get().defaultBlockState();
            BlockState cross = ModBlocks.TIMBER_FRAME_CROSS.get().defaultBlockState();
            BlockState diagonal = ModBlocks.TIMBER_FRAME_DIAGONAL.get().defaultBlockState();
            int wx = ox + 40;
            wall(level, wx, oz, new BlockState[][] {{diagonal, frame, diagonal}, {cross, frame, cross}, {frame, cross, frame}});
            wall(level, wx + 4, oz, uniform(ModBlocks.TIMBER_BEAM.get().defaultBlockState()));
            for (int y = 0; y < 3; y++) {
                level.setBlock(new BlockPos(wx + 9, ground + y, oz), ModBlocks.STONE_PILLAR.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(wx + 11, ground + y, oz), ModBlocks.STONE_PILLAR.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            // 3) sechs kleine Dächer (fallen zur Kamera hin ab)
            List<BuildingSet> roofs = List.of(ModBlocks.RED_ROOF_TILES, ModBlocks.BROWN_ROOF_TILES, ModBlocks.LIGHT_ROOF_TILES,
                    ModBlocks.DARK_SLATE_SHINGLES, ModBlocks.WOODEN_SHINGLES, ModBlocks.COPPER_ROOF);
            for (int i = 0; i < roofs.size(); i++) {
                int rx = ox + 60 + (i / 3) * 20 + (i % 3) * 4;
                BlockState stairs = roofs.get(i).stairs().get().defaultBlockState().setValue(StairBlock.FACING, Direction.NORTH);
                BlockState full = roofs.get(i).base().get().defaultBlockState();
                for (int step = 0; step < 3; step++) {
                    for (int x = 0; x < 3; x++) {
                        level.setBlock(new BlockPos(rx + x, ground + step, oz + 2 - step), stairs, Block.UPDATE_CLIENTS);
                        for (int below = 0; below < step; below++) {
                            level.setBlock(new BlockPos(rx + x, ground + below, oz + 2 - step), full, Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            // 4) Erze (oben Stein, Mitte Tiefenschiefer) und Rohstoffblöcke (unten)
            int ex = ox + 100;
            List<Block> stoneOres = List.of(ModBlocks.CARBON_ORE.get(), ModBlocks.GEMSTONE_ORE.get(), ModBlocks.KONTERIT_ORE.get(),
                    ModBlocks.AETHERIT_ORE.get(), ModBlocks.DRACONIUM_ORE.get(), ModBlocks.WYVERNIT_ORE.get(), Blocks.STONE);
            List<Block> deepOres = List.of(ModBlocks.DEEPSLATE_CARBON_ORE.get(), ModBlocks.DEEPSLATE_GEMSTONE_ORE.get(),
                    ModBlocks.DEEPSLATE_KONTERIT_ORE.get(), ModBlocks.DEEPSLATE_AETHERIT_ORE.get(), ModBlocks.DEEPSLATE_DRACONIUM_ORE.get(),
                    Blocks.DEEPSLATE, Blocks.DEEPSLATE);
            List<Block> storage = List.of(ModBlocks.CARBON_BLOCK.get(), ModBlocks.GEMSTONE_BLOCK.get(), ModBlocks.KONTERIT_BLOCK.get(),
                    ModBlocks.AETHERIT_BLOCK.get(), ModBlocks.DRACONIUM_BLOCK.get(), ModBlocks.WYVERNIT_BLOCK.get(), ModBlocks.STEEL_BLOCK.get());
            for (int i = 0; i < 7; i++) {
                level.setBlock(new BlockPos(ex + i, ground + 2, oz), stoneOres.get(i).defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(ex + i, ground + 1, oz), deepOres.get(i).defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(ex + i, ground, oz), storage.get(i).defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            // 5) Dracheneier: alle Arten, dahinter ein Feuerei in jeder Brutstufe
            int gx = ox + 120;
            int k = 0;
            for (var egg : ModBlocks.DRAGON_EGGS.values()) {
                level.setBlock(new BlockPos(gx + k, ground, oz + 2), egg.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                k++;
            }
            var fire = ModBlocks.DRAGON_EGGS.get(DragonType.FIRE).get();
            for (int h = 0; h <= 3; h++) {
                level.setBlock(new BlockPos(gx + 1 + h, ground, oz), fire.defaultBlockState().setValue(
                        io.github.sirschrolli.kingdomsatwar.block.ElementalDragonEggBlock.HATCH, h), Block.UPDATE_CLIENTS);
            }
        }));
        steps.add(Step.waitTicks(40));
        String[] names = {"samples_stone_1", "samples_stone_2", "samples_wood", "samples_roof_1", "samples_roof_2", "samples_ores", "samples_eggs"};
        double[][] cams = { // x, dy, z, yaw, pitch
            {ox + 7.5, 1.6, oz + 9.0, 180, 4}, {ox + 27.5, 1.6, oz + 9.0, 180, 4}, {ox + 46.5, 1.6, oz + 9.0, 180, 4},
            {ox + 65.5, 4.2, oz + 9.5, 180, 22}, {ox + 85.5, 4.2, oz + 9.5, 180, 22},
            {ox + 103.5, 1.6, oz + 6.5, 180, 4}, {ox + 123.5, 1.4, oz + 4.6, 180, 25},
        };
        for (int i = 0; i < names.length; i++) {
            steps.add(tp(cams[i][0], cams[i][1], cams[i][2], (float) cams[i][3], (float) cams[i][4]));
            steps.add(Step.waitForChunks(200));
            steps.add(Step.waitTicks(i == 0 ? 40 : 25));
            steps.add(Step.hud(false));
            steps.add(Step.screenshot(names[i]));
        }
        steps.add(Step.hud(true));
    }

    private static void wall(ServerLevel level, int x0, int z, BlockState[][] wall) {
        for (int y = 0; y < 3; y++) {
            for (int x = 0; x < 3; x++) {
                level.setBlock(new BlockPos(x0 + x, ground + 2 - y, z), wall[y][x], Block.UPDATE_CLIENTS);
            }
        }
    }

    private static BlockState[][] uniform(BlockState state) {
        return new BlockState[][] {{state, state, state}, {state, state, state}, {state, state, state}};
    }

    /** Alle Drachenarten, alle Wachstumsstufen, Flug-, Sitz- und Odem-Haltung. */
    private static void dragons(Deque<Step> steps) {
        int oz = 40;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 6), new BlockPos(46, ground - 1, oz + 40), Blocks.GRASS_BLOCK.defaultBlockState());
            DragonType[] types = DragonType.values();
            // Reihe 1: sechs ausgewachsene Drachen, seitlich (Blick nach Osten)
            for (int i = 0; i < types.length; i++) {
                spawnDragon(level, types[i], DragonStage.ADULT, new BlockPos(i * 7, ground, oz), -90, d -> { });
            }
            // Reihe 2: alle Wachstumsstufen des Feuerdrachen
            DragonStage[] stages = DragonStage.values();
            for (int i = 0; i < stages.length; i++) {
                spawnDragon(level, DragonType.FIRE, stages[i], new BlockPos(i * 7, ground, oz + 12), -90, d -> { });
            }
            // Reihe 3: fliegend, sitzend, Odem
            spawnDragon(level, DragonType.ICE, DragonStage.ADULT, new BlockPos(0, ground + 3, oz + 24), -90, d -> d.setFlying(true));
            spawnDragon(level, DragonType.NATURE, DragonStage.ADULT, new BlockPos(9, ground, oz + 24), -90,
                    d -> { d.setOrderedToSit(true); d.setInSittingPose(true); });
            spawnDragon(level, DragonType.FIRE, DragonStage.ELDER, new BlockPos(18, ground, oz + 24), -90, d -> d.startBreath(null));
            spawnDragon(level, DragonType.LIGHTNING, DragonStage.ADULT, new BlockPos(27, ground + 3, oz + 24), 180, d -> d.setFlying(true));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(tp(17, 7, oz - 14, 0, 20));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(20));
        steps.add(Step.hud(false));
        steps.add(Step.screenshot("dragons_adults"));
        steps.add(tp(14, 5, oz + 3, 0, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("dragons_stages"));
        steps.add(tp(14, 7, oz + 13, 0, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> DragonEntity.nearby(level, new net.minecraft.world.phys.Vec3(18, ground, oz + 24), 4)
                .forEach(d -> d.startBreath(null))));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("dragons_poses"));
        steps.add(Step.waitTicks(7));
        steps.add(Step.screenshot("dragons_poses_2"));
        // Nahaufnahme Kopf
        steps.add(tp(3, 4, oz - 5, -20, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("dragon_closeup"));
        steps.add(Step.hud(true));
    }

    /** Rüstungen auf Rüstungsständern (vorne und hinten), Drachen mit Panzer und Waffen in der Hand. */
    private static void equipment(Deque<Step> steps) {
        int oz = 140;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 30), new BlockPos(40, ground - 1, oz + 12), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-6, ground, oz - 30), new BlockPos(40, ground + 8, oz + 12), Blocks.AIR.defaultBlockState());
            List<List<Item>> sets = new ArrayList<>();
            sets.add(List.of(ModItems.STEEL_HELMET.get(), ModItems.STEEL_CHESTPLATE.get(), ModItems.STEEL_LEGGINGS.get(),
                    ModItems.STEEL_BOOTS.get(), ModItems.STEEL_SWORD.get()));
            sets.add(List.of(ModItems.KONTERIT_HELMET.get(), ModItems.KONTERIT_CHESTPLATE.get(), ModItems.KONTERIT_LEGGINGS.get(),
                    ModItems.KONTERIT_BOOTS.get(), ModItems.STEEL_PICKAXE.get()));
            for (DragonType type : DragonType.values()) {
                var set = ModItems.DRAGON_SCALE_ARMOR.get(type);
                sets.add(List.of(set.get(0).get(), set.get(1).get(), set.get(2).get(), set.get(3).get(),
                        type == DragonType.FIRE ? ModItems.DRACONIUM_SWORD.get() : type == DragonType.EARTH
                                ? ModItems.DRACONIUM_AXE.get() : Items.AIR));
            }
            sets.add(List.of(ModItems.GEMSTONE_CROWN.get(), ModItems.WYVERNIT_GLIDER.get(), Items.AIR, Items.AIR, ModItems.STEEL_AXE.get()));
            EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET, EquipmentSlot.MAINHAND};
            for (int i = 0; i < sets.size(); i++) {
                ArmorStand stand = EntityTypes.ARMOR_STAND.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (stand == null) {
                    continue;
                }
                stand.setShowArms(true);
                stand.setNoBasePlate(true);
                stand.setRightArmPose(new Rotations(-20, 0, 10));
                for (int j = 0; j < slots.length; j++) {
                    stand.setItemSlot(slots[j], new ItemStack(sets.get(i).get(j)));
                }
                stand.snapTo(i * 2 + 0.5, ground, oz + 0.5, 0, 0);
                stand.setYBodyRot(0);
                level.addFreshEntity(stand);
            }
            // Drachen mit Panzer (Stahl und Draconium)
            spawnDragon(level, DragonType.EARTH, DragonStage.ADULT, new BlockPos(4, ground, oz - 14), -90, d -> {
                d.setTame(true, false);
                d.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.STEEL_DRAGON_ARMOR.get()));
            });
            spawnDragon(level, DragonType.FIRE, DragonStage.ADULT, new BlockPos(16, ground, oz - 14), -90, d -> {
                d.setTame(true, false);
                d.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.DRACONIUM_DRAGON_ARMOR.get()));
            });
            spawnDragon(level, DragonType.ICE, DragonStage.ELDER, new BlockPos(28, ground + 3, oz - 14), -90, d -> {
                d.setTame(true, false);
                d.setFlying(true);
                d.setItemSlot(EquipmentSlot.BODY, new ItemStack(ModItems.STEEL_DRAGON_ARMOR.get()));
            });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(8.5, 1.4, oz + 5.5, 180, 8));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("equipment_front"));
        steps.add(tp(4.0, 1.5, oz + 3.2, 180, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_front_close"));
        steps.add(tp(13.0, 1.5, oz + 3.2, 180, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_front_close_2"));
        steps.add(tp(8.5, 1.6, oz - 4.5, 0, 8));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_back"));
        steps.add(tp(16.5, 6, oz - 1, 180, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_dragons"));
        steps.add(tp(10.5, 3.5, oz - 6.5, 160, 15));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("equipment_dragon_close"));
        steps.add(Step.hud(true));
    }

    /** Die wilden Kreaturen: alle Arten und Varianten, von vorne, von der Seite und das Irrlicht bei Nacht. */
    private static void creatures(Deque<Step> steps) {
        int oz = 200;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 20), new BlockPos(40, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-6, ground, oz - 20), new BlockPos(40, ground + 8, oz + 14), Blocks.AIR.defaultBlockState());
            int x = 0;
            spawnCreature(level, ModEntities.WILD_BOAR.get(), x, oz, 0, m -> { });
            spawnCreature(level, ModEntities.WILD_BOAR.get(), x + 2, oz, 0, m -> m.setBaby(true));
            spawnCreature(level, ModEntities.DEER.get(), x + 5, oz, 0, m -> m.setStag(true));
            spawnCreature(level, ModEntities.DEER.get(), x + 7, oz, 0, m -> m.setStag(false));
            spawnCreature(level, ModEntities.DEER.get(), x + 9, oz, 0, m -> m.setBaby(true));
            spawnCreature(level, ModEntities.GOOSE.get(), x + 11, oz, 0, m -> m.setVariant(0));
            spawnCreature(level, ModEntities.GOOSE.get(), x + 12, oz, 0, m -> m.setVariant(1));
            spawnCreature(level, ModEntities.GOOSE.get(), x + 13, oz, 0, m -> m.setBaby(true));
            spawnCreature(level, ModEntities.WILL_O_WISP.get(), x + 15, oz, 0, m -> {
                m.setNoGravity(true);
                m.setPos(m.getX(), m.getY() + 1.2, m.getZ());
            });
            spawnCreature(level, ModEntities.CAVE_TROLL.get(), x + 18, oz, 0, m -> { });
            spawnCreature(level, ModEntities.LINDWORM.get(), x + 24, oz, -90, m -> { });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(13.0, 2.2, oz + 10.5, 180, 8));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("creatures_overview"));
        steps.add(tp(3.5, 1.3, oz + 4.2, 200, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_boars"));
        steps.add(tp(8.0, 1.6, oz + 4.6, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_animals"));
        steps.add(tp(1.5, 1.6, oz - 3.0, -50, 8));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_animals_side"));
        steps.add(tp(14.5, 1.0, oz + 3.2, 180, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_geese_wisp"));
        steps.add(tp(21.5, 2.2, oz + 6.5, 160, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_troll_lindworm"));
        steps.add(tp(11.0, 1.5, oz - 5.5, -30, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("creatures_side"));
        steps.add(Step.command("time set 18000"));
        steps.add(tp(15.5, 1.8, oz + 4.0, 180, 6));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("creatures_wisp_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
    }

    /**
     * Ein Thronsaal aus den Einrichtungs-Blöcken: Putzwände mit Bleiglasfenstern und Fensterläden, Burgtür,
     * Zinnen, Kronleuchter und Laternen, Tafeln mit Stühlen und Bänken, ein Ritter auf dem Thron.
     */
    private static void decor(Deque<Step> steps) {
        int oz = 260;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-12, ground - 1, oz - 8), new BlockPos(22, ground - 1, oz + 18), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-12, ground, oz - 8), new BlockPos(22, ground + 9, oz + 18), Blocks.AIR.defaultBlockState());
            fill(level, new BlockPos(0, ground - 1, oz), new BlockPos(10, ground - 1, oz + 7), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            BlockState plaster = ModBlocks.WHITE_PLASTER.base().get().defaultBlockState();
            BlockState clay = ModBlocks.CLAY_PLASTER.base().get().defaultBlockState();
            BlockState limestone = ModBlocks.LIMESTONE_BRICKS.base().get().defaultBlockState();
            BlockState beam = ModBlocks.TIMBER_BEAM.get().defaultBlockState();
            BlockState glass = ModBlocks.LEADED_GLASS.get().defaultBlockState();
            // Rückwand (Kalkputz, zwei Fenster), Seitenwände (Lehmputz links, Kalkstein rechts), Ecken aus Balken
            fill(level, new BlockPos(1, ground, oz), new BlockPos(9, ground + 4, oz), plaster);
            fill(level, new BlockPos(0, ground, oz + 1), new BlockPos(0, ground + 4, oz + 6), clay);
            fill(level, new BlockPos(10, ground, oz + 1), new BlockPos(10, ground + 4, oz + 6), limestone);
            for (int[] corner : new int[][] {{0, oz}, {10, oz}, {0, oz + 7}, {10, oz + 7}}) {
                fill(level, new BlockPos(corner[0], ground, corner[1]), new BlockPos(corner[0], ground + 4, corner[1]), beam);
            }
            fill(level, new BlockPos(2, ground + 1, oz), new BlockPos(3, ground + 2, oz), glass);
            fill(level, new BlockPos(7, ground + 1, oz), new BlockPos(8, ground + 2, oz), glass);
            // Fensterläden innen neben den Fenstern (geöffnet, flach an der Wand)
            for (int x : new int[] {1, 4, 6, 9}) {
                Block shutters = x < 5 ? ModBlocks.GREEN_SHUTTERS.get() : ModBlocks.RED_SHUTTERS.get();
                for (int y = 1; y <= 2; y++) {
                    level.setBlock(new BlockPos(x, ground + y, oz + 1), shutters.defaultBlockState()
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.SOUTH)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, true)
                            .setValue(net.minecraft.world.level.block.TrapDoorBlock.HALF, y == 1 ? Half.BOTTOM : Half.TOP), Block.UPDATE_CLIENTS);
                }
            }
            // Fenster mit Bleiglasscheiben in der linken Wand
            for (int z = oz + 3; z <= oz + 4; z++) {
                for (int y = 1; y <= 2; y++) {
                    BlockPos pos = new BlockPos(0, ground + y, z);
                    level.setBlock(pos, ModBlocks.LEADED_GLASS_PANE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            // Burgtür in der rechten Wand
            BlockState door = ModBlocks.CASTLE_DOOR.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.DoorBlock.FACING, Direction.WEST);
            level.setBlock(new BlockPos(10, ground, oz + 3), door, Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(10, ground + 1, oz + 3), door.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                    net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
            // Decke und Zinnen obenauf
            fill(level, new BlockPos(0, ground + 5, oz), new BlockPos(10, ground + 5, oz + 7), Blocks.DARK_OAK_PLANKS.defaultBlockState());
            for (int x = 0; x <= 10; x++) {
                level.setBlock(new BlockPos(x, ground + 6, oz), ModBlocks.MEDIEVAL_STONE_BATTLEMENT.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
            }
            for (int z = oz + 1; z <= oz + 7; z++) {
                level.setBlock(new BlockPos(0, ground + 6, z), ModBlocks.DARK_STONE_BRICK_BATTLEMENT.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, Direction.EAST), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(10, ground + 6, z), ModBlocks.LIMESTONE_BRICK_BATTLEMENT.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, Direction.WEST), Block.UPDATE_CLIENTS);
            }
            // Licht: Kronleuchter in der Mitte, Laternen an der Decke und auf den Tischen
            level.setBlock(new BlockPos(5, ground + 4, oz + 4), ModBlocks.CHANDELIER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            for (int[] spot : new int[][] {{2, oz + 6}, {8, oz + 6}}) {
                level.setBlock(new BlockPos(spot[0], ground + 4, spot[1]), ModBlocks.AETHERIT_LANTERN.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.LanternBlock.HANGING, true), Block.UPDATE_CLIENTS);
            }
            // Thron mit rotem Teppich
            level.setBlock(new BlockPos(5, ground, oz + 1), ModBlocks.THRONE.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, Direction.SOUTH), Block.UPDATE_CLIENTS);
            fill(level, new BlockPos(5, ground, oz + 2), new BlockPos(5, ground, oz + 9), Blocks.CARPET.red().defaultBlockState());
            // Tafeln links und rechts, mit Stühlen und Bänken
            for (int x : new int[] {2, 8}) {
                for (int z = oz + 3; z <= oz + 5; z++) {
                    BlockPos pos = new BlockPos(x, ground, z);
                    level.setBlock(pos, ModBlocks.OAK_TABLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            for (int x : new int[] {2, 8}) {
                for (int z = oz + 3; z <= oz + 5; z++) {
                    BlockPos pos = new BlockPos(x, ground, z);
                    level.setBlock(pos, Block.updateFromNeighbourShapes(level.getBlockState(pos), level, pos), Block.UPDATE_CLIENTS);
                }
                boolean left = x < 5;
                Direction outer = left ? Direction.EAST : Direction.WEST;    // Stühle an der Wand schauen zum Tisch
                for (int z = oz + 3; z <= oz + 5; z += 2) {
                    level.setBlock(new BlockPos(left ? 1 : 9, ground, z), ModBlocks.OAK_CHAIR.get().defaultBlockState()
                            .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, outer), Block.UPDATE_CLIENTS);
                }
                for (int z = oz + 3; z <= oz + 5; z++) {
                    level.setBlock(new BlockPos(left ? 3 : 7, ground, z), ModBlocks.OAK_BENCH.get().defaultBlockState()
                            .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, outer.getOpposite()), Block.UPDATE_CLIENTS);
                }
            }
            level.setBlock(new BlockPos(2, ground + 1, oz + 4), ModBlocks.STEEL_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(8, ground + 1, oz + 4), ModBlocks.STEEL_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            // Ein Ritter auf dem Thron, Soldaten an der Tafel
            spawnSoldier(level, SoldierType.CAPTAIN, 0, 5, oz + 1, 0, s -> seat(level, s, new BlockPos(5, ground, oz + 1)));
            spawnSoldier(level, SoldierType.SWORDSMAN, 1, 3, oz + 4, 0, s -> seat(level, s, new BlockPos(3, ground, oz + 4)));
            spawnSoldier(level, SoldierType.ARCHER, 0, 9, oz + 3, 0, s -> seat(level, s, new BlockPos(9, ground, oz + 3)));

            // Ausstellung daneben: jedes Stück einzeln (Thron leer), Zinnenreihe, Tür offen und zu
            Direction south = Direction.SOUTH;
            level.setBlock(new BlockPos(15, ground, oz + 4), ModBlocks.THRONE.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(17, ground, oz + 4), ModBlocks.OAK_CHAIR.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(19, ground, oz + 4), ModBlocks.OAK_BENCH.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(20, ground, oz + 4), ModBlocks.OAK_BENCH.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING, south), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(22, ground, oz + 4), ModBlocks.OAK_TABLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(22, ground + 1, oz + 4), ModBlocks.AETHERIT_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(24, ground + 2, oz + 4), Blocks.DARK_OAK_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(24, ground + 1, oz + 4), ModBlocks.CHANDELIER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            for (int x = 14; x <= 24; x++) {
                Block battlement = x < 18 ? ModBlocks.MEDIEVAL_STONE_BATTLEMENT.get()
                        : x < 22 ? ModBlocks.DARK_STONE_BRICK_BATTLEMENT.get() : ModBlocks.LIMESTONE_BRICK_BATTLEMENT.get();
                level.setBlock(new BlockPos(x, ground, oz), battlement.defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.BattlementBlock.FACING, south), Block.UPDATE_CLIENTS);
            }
            for (int i = 0; i < 2; i++) {
                BlockState castleDoor = ModBlocks.CASTLE_DOOR.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.DoorBlock.FACING, south)
                        .setValue(net.minecraft.world.level.block.DoorBlock.OPEN, i == 1);
                level.setBlock(new BlockPos(14 + i * 2, ground, oz + 8), castleDoor, Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(14 + i * 2, ground + 1, oz + 8), castleDoor.setValue(net.minecraft.world.level.block.DoorBlock.HALF,
                        net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER), Block.UPDATE_CLIENTS);
                BlockState shutter = ModBlocks.RED_SHUTTERS.get().defaultBlockState()
                        .setValue(net.minecraft.world.level.block.TrapDoorBlock.FACING, Direction.NORTH)
                        .setValue(net.minecraft.world.level.block.TrapDoorBlock.OPEN, i == 1);
                level.setBlock(new BlockPos(19 + i * 2, ground, oz + 8), shutter, Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(19 + i * 2, ground + 1, oz + 8), ModBlocks.STEEL_LANTERN.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            level.setBlock(new BlockPos(23, ground, oz + 8), ModBlocks.LEADED_GLASS.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(24, ground, oz + 8), ModBlocks.LEADED_GLASS_PANE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(5.5, 2.6, oz + 14.5, 180, 8));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("decor_hall"));
        steps.add(tp(5.5, 1.7, oz + 6.6, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_throne"));
        steps.add(tp(7.2, 2.0, oz + 6.9, 120, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_furniture"));
        steps.add(tp(4.0, 1.6, oz + 3.5, -90, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_door"));
        steps.add(tp(5.5, 1.4, oz + 6.5, 180, -32));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_lights"));
        steps.add(tp(-6.0, 5.0, oz + 13.0, 225, 18));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_outside"));
        steps.add(tp(19.5, 2.4, oz + 13.5, 180, 14));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_display"));
        steps.add(tp(17.5, 1.6, oz + 7.5, 160, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_display_close"));
        steps.add(tp(19.0, 2.4, oz + 3.0, 180, 30));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_battlements"));
        steps.add(tp(19.5, 1.8, oz + 11.5, 180, 12));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("decor_doors"));
        steps.add(Step.command("time set 18000"));
        steps.add(tp(5.5, 2.6, oz + 14.5, 180, 8));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("decor_hall_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
    }

    /** Hopfen, Weinreben und Knoblauch in allen Wachstumsstufen, Mondblumen bei Nacht, Festmahl auf der Tafel. */
    private static void farm(Deque<Step> steps) {
        int oz = 320;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-8, ground - 1, oz - 10), new BlockPos(24, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-8, ground, oz - 10), new BlockPos(24, ground + 6, oz + 14), Blocks.AIR.defaultBlockState());
            List<io.github.sirschrolli.kingdomsatwar.block.ModCropBlock> crops = List.of(ModBlocks.HOPS.get(), ModBlocks.GRAPES.get(),
                    ModBlocks.GARLIC_CROP.get());
            int[] ages = {0, 2, 5, 7};
            for (int row = 0; row < crops.size(); row++) {
                for (int i = 0; i < 8; i++) {
                    BlockPos soil = new BlockPos(i, ground - 1, oz - row * 2);
                    level.setBlock(soil, Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                    level.setBlock(soil.above(), crops.get(row).getStateForAge(ages[Math.min(3, i / 2)]), Block.UPDATE_CLIENTS);
                }
                level.setBlock(new BlockPos(8, ground - 1, oz - row * 2), Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            // Mondblumen
            for (int[] spot : new int[][] {{12, 0}, {13, 1}, {14, -1}, {15, 1}, {12, 2}, {16, 0}, {14, 2}}) {
                level.setBlock(new BlockPos(spot[0], ground, oz + spot[1]), ModBlocks.MOONFLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            level.setBlock(new BlockPos(17, ground, oz + 2), ModBlocks.POTTED_MOONFLOWER.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            // Tafel mit vier Festmahlen (voll bis fast aufgegessen)
            for (int i = 0; i < 4; i++) {
                BlockPos table = new BlockPos(1 + i, ground, oz + 4);
                level.setBlock(table, ModBlocks.OAK_TABLE.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            }
            for (int i = 0; i < 4; i++) {
                BlockPos table = new BlockPos(1 + i, ground, oz + 4);
                level.setBlock(table, Block.updateFromNeighbourShapes(level.getBlockState(table), level, table), Block.UPDATE_CLIENTS);
                level.setBlock(table.above(), ModBlocks.FEAST.get().defaultBlockState()
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.FeastBlock.FACING, Direction.SOUTH)
                        .setValue(io.github.sirschrolli.kingdomsatwar.block.FeastBlock.EATEN, i), Block.UPDATE_CLIENTS);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(4.0, 2.6, oz + 5.5, 180, 30));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("farm_crops"));
        steps.add(tp(4.0, 1.6, oz + 2.8, 180, 22));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("farm_crops_close"));
        steps.add(tp(3.0, 2.2, oz + 7.5, 180, 38));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("farm_feast"));
        steps.add(tp(2.6, 2.0, oz + 5.9, 180, 48));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("farm_feast_close"));
        steps.add(Step.command("time set 18000"));
        steps.add(tp(14.5, 1.6, oz + 5.0, 180, 20));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("farm_moonflowers_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
    }

    /** Der Aetherit-Stab in Aktion: jeder Zauber einmal, kurz nach dem Wirken fotografiert. */
    private static void magic(Deque<Step> steps) {
        int oz = 380;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-8, ground - 1, oz - 16), new BlockPos(16, ground - 1, oz + 6), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-8, ground, oz - 16), new BlockPos(16, ground + 8, oz + 6), Blocks.AIR.defaultBlockState());
            fill(level, new BlockPos(2, ground - 1, oz - 6), new BlockPos(7, ground - 1, oz - 3), Blocks.WATER.defaultBlockState());
            for (int x : new int[] {1, 8}) {
                var golem = net.minecraft.world.entity.EntityTypes.IRON_GOLEM.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                golem.setNoAi(true);
                golem.snapTo(x + 0.5, ground, oz - 9 + 0.5, 0, 0);
                level.addFreshEntity(golem);
            }
        }));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:aetherit_staff"));
        steps.add(Step.command("item replace entity @s hotbar.1 with kingdomsatwar:fire_rune"));
        steps.add(Step.command("item replace entity @s hotbar.2 with kingdomsatwar:ice_rune"));
        steps.add(Step.command("item replace entity @s hotbar.3 with kingdomsatwar:lightning_rune"));
        steps.add(Step.command("item replace entity @s hotbar.4 with kingdomsatwar:nature_rune"));
        steps.add(Step.command("item replace entity @s hotbar.5 with kingdomsatwar:earth_rune"));
        steps.add(Step.command("item replace entity @s hotbar.6 with kingdomsatwar:water_rune"));
        steps.add(tp(4.5, 0, oz + 1.5, 180, 14));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(120));
        String[][] casts = {{"", "AETHER_BOLT"}, {"fire_rune", "FIRE"}, {"ice_rune", "ICE"}, {"lightning_rune", "LIGHTNING"},
                {"nature_rune", "NATURE"}, {"earth_rune", "EARTH"}, {"water_rune", "WATER"}};
        for (String[] cast : casts) {
            steps.add(Step.command(cast[0].isEmpty() ? "item replace entity @s weapon.offhand with air"
                    : "item replace entity @s weapon.offhand with kingdomsatwar:" + cast[0]));
            steps.add(Step.waitTicks(4));
            steps.add(Step.server(level -> {
                var player = level.getServer().getPlayerList().getPlayers().get(0);
                io.github.sirschrolli.kingdomsatwar.magic.Spell.valueOf(cast[1]).cast(level, player);
            }));
            steps.add(Step.waitTicks(cast[1].equals("LIGHTNING") ? 2 : 5));
            steps.add(Step.screenshot("magic_" + cast[1].toLowerCase(java.util.Locale.ROOT)));
            steps.add(Step.waitTicks(30));
        }
        steps.add(Step.command("gamemode spectator"));
    }

    /** Eine Burgruine, wie sie die Weltgenerierung baut (die Drachennester zeigt die Szene "lair"). */
    private static void structures(Deque<Step> steps) {
        int oz = 440;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-10, ground - 3, oz - 14), new BlockPos(90, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-10, ground, oz - 14), new BlockPos(90, ground + 10, oz + 14), Blocks.AIR.defaultBlockState());
            var generator = level.getChunkSource().getGenerator();
            int x = 84;
            new io.github.sirschrolli.kingdomsatwar.worldgen.CastleRuinFeature()
                    .place(level, generator, net.minecraft.util.RandomSource.create(3), new BlockPos(x + 2, ground, oz));
            level.getEntitiesOfClass(net.minecraft.world.entity.Mob.class, new net.minecraft.world.phys.AABB(-20, ground - 5, oz - 20, 120, ground + 20, oz + 20))
                    .forEach(mob -> {
                        mob.setNoAi(true);
                        mob.setPersistenceRequired();
                    });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(0.5, 9.0, oz + 13.0, 180, 35));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.waitTicks(100));
        steps.add(tp(14.5, 2.2, oz + 3.0, 180, 30));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("structure_nest_ice_close"));
        steps.add(tp(6 * 14 + 2.5, 9.0, oz + 13.0, 180, 32));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("structure_castle_ruin"));
        steps.add(tp(6 * 14 + 10.0, 6.0, oz + 9.0, 135, 22));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("structure_castle_ruin_side"));
        steps.add(Step.hud(true));
    }

    /** Setzt ein Lebewesen auf das Sitzmöbel bei {@code pos}. */
    private static void seat(ServerLevel level, net.minecraft.world.entity.LivingEntity sitter, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof io.github.sirschrolli.kingdomsatwar.block.SeatBlock seat) {
            io.github.sirschrolli.kingdomsatwar.entity.SeatEntity.sit(level, pos, seat.seatHeight(),
                    state.getValue(io.github.sirschrolli.kingdomsatwar.block.SeatBlock.FACING), sitter);
        }
    }

    private static <T extends net.minecraft.world.entity.Mob> void spawnCreature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type,
            int x, int z, float yaw, java.util.function.Consumer<T> setup) {
        T mob = type.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (mob == null) {
            return;
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.snapTo(x + 0.5, ground, z + 0.5, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setup.accept(mob);
        level.addFreshEntity(mob);
    }

    /** Die sechs Dracheneier neben dem echten Drachenei, aus Spielersicht. */
    private static void eggs(Deque<Step> steps) {
        int oz = 100;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-6, ground - 1, oz - 6), new BlockPos(18, ground - 1, oz + 8), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-6, ground, oz - 6), new BlockPos(18, ground + 4, oz + 8), Blocks.AIR.defaultBlockState());
            level.setBlock(new BlockPos(0, ground, oz), Blocks.DRAGON_EGG.defaultBlockState(), Block.UPDATE_CLIENTS);
            int x = 2;
            for (DragonType type : DragonType.values()) {
                level.setBlock(new BlockPos(x, ground, oz), ModBlocks.DRAGON_EGGS.get(type).get().defaultBlockState(), Block.UPDATE_CLIENTS);
                x += 2;
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(false));
        steps.add(tp(6.5, 1.62, oz + 5.5, 180, 18));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("eggs_player_view"));
        steps.add(tp(3.5, 1.2, oz + 2.6, 180, 20));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("eggs_close"));
        steps.add(Step.hud(true));
    }

    /** Soldaten aller Arten und Gesichter, Banditen, Gesten, ein Gefecht und das Anwerbe-Fenster. */
    private static void soldiers(Deque<Step> steps) {
        int oz = -60;
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(-10, ground - 1, oz - 30), new BlockPos(40, ground - 1, oz + 14), Blocks.GRASS_BLOCK.defaultBlockState());
            fill(level, new BlockPos(-10, ground, oz - 30), new BlockPos(40, ground + 6, oz + 14), Blocks.AIR.defaultBlockState());
            // Reihe 1: Schwertkämpfer und Bogenschützen (alle Gesichter), Blick nach Süden zur Kamera
            int x = 0;
            for (SoldierType type : List.of(SoldierType.SWORDSMAN, SoldierType.ARCHER)) {
                for (int v = 0; v < type.variants(); v++) {
                    spawnSoldier(level, type, v, x, oz, 0, s -> { });
                    x += 2;
                }
            }
            // Reihe 2: Ritter, Hauptleute, Banditen
            x = 0;
            for (SoldierType type : List.of(SoldierType.KNIGHT, SoldierType.CAPTAIN)) {
                for (int v = 0; v < type.variants(); v++) {
                    spawnSoldier(level, type, v, x, oz - 6, 0, s -> { });
                    x += 2;
                }
            }
            for (boolean archer : new boolean[] {false, true}) {
                for (int v = 0; v < BanditEntity.VARIANTS; v++) {
                    BanditEntity bandit = (archer ? ModEntities.BANDIT_ARCHER : ModEntities.BANDIT).get()
                            .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                    bandit.setVariant(v);
                    bandit.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                            new net.minecraft.world.item.ItemStack(archer ? net.minecraft.world.item.Items.BOW : net.minecraft.world.item.Items.IRON_AXE));
                    bandit.setNoAi(true);
                    bandit.snapTo(x + 0.5, ground, oz - 12 + 0.5, 0, 0);
                    bandit.setYBodyRot(0);
                    bandit.setYHeadRot(0);
                    bandit.setPersistenceRequired();
                    level.addFreshEntity(bandit);
                    x += 2;
                }
            }
            // Reihe 3: Gesten und Kampfhaltungen
            spawnSoldier(level, SoldierType.CAPTAIN, 0, 0, oz - 18, 0, s -> { });
            spawnSoldier(level, SoldierType.SWORDSMAN, 0, 2, oz - 18, 0, s -> { });
            spawnSoldier(level, SoldierType.KNIGHT, 1, 4, oz - 18, 0, s -> s.startUsingItem(net.minecraft.world.InteractionHand.OFF_HAND));
            spawnSoldier(level, SoldierType.ARCHER, 0, 6, oz - 18, 0, s -> {
                s.setAggressive(true);
                s.startUsingItem(net.minecraft.world.InteractionHand.MAIN_HAND);
            });
            // Rekrutierungstisch
            level.setBlock(new BlockPos(30, ground, oz), ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.RecruitmentTableBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(tp(7.0, 1.7, oz + 7.0, 180, 6));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("soldiers_line_1"));
        steps.add(tp(7.0, 1.7, oz - 7.0 + 0.0, 0, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("soldiers_back_1"));
        steps.add(tp(4.0, 1.7, oz + 1.0, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("soldiers_line_2"));
        steps.add(tp(15.0, 1.7, oz - 5.5, 180, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("soldiers_bandits"));
        // Gesten: Salut und Jubel, Schild, Bogen
        steps.add(tp(3.5, 1.6, oz - 13.5, 180, 4));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> level.getEntitiesOfClass(SoldierEntity.class,
                new net.minecraft.world.phys.AABB(-1, ground - 1, oz - 19, 3, ground + 3, oz - 17)).forEach(s -> {
                    if (s.getSoldierType() == SoldierType.CAPTAIN) {
                        s.salute();
                    } else {
                        s.cheer();
                    }
                })));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("soldiers_gestures_a"));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("soldiers_gestures"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("soldiers_gestures_c"));
        // Rückansicht der Zusatzteile (Umhang, Feder, Köcher)
        steps.add(tp(3.5, 1.9, oz - 22.0, 0, 6));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("soldiers_gestures_back"));
        // Gefecht: vier Soldaten gegen vier Banditen
        steps.add(Step.server(level -> {
            int bx = 14;
            int bz = oz - 24;
            SoldierType[] army = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.CAPTAIN, SoldierType.ARCHER};
            for (int i = 0; i < army.length; i++) {
                SoldierEntity soldier = ModEntities.SOLDIERS.get(army[i]).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                soldier.equip();
                soldier.setVariant(i % army[i].variants());
                soldier.snapTo(bx + i * 2 + 0.5, ground, bz + 0.5 + (army[i] == SoldierType.ARCHER ? -3 : 0), 0, 0);
                soldier.setPersistenceRequired();
                level.addFreshEntity(soldier);
            }
            for (int i = 0; i < 4; i++) {
                BanditEntity bandit = (i == 3 ? ModEntities.BANDIT_ARCHER : ModEntities.BANDIT).get()
                        .create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                bandit.setVariant(i % BanditEntity.VARIANTS);
                bandit.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                        new net.minecraft.world.item.ItemStack(i == 3 ? net.minecraft.world.item.Items.BOW : net.minecraft.world.item.Items.IRON_AXE));
                bandit.snapTo(bx + i * 2 + 0.5, ground, bz + 9.5, 180, 0);
                bandit.setPersistenceRequired();
                level.addFreshEntity(bandit);
            }
        }));
        steps.add(tp(25.0, 4.0, oz - 19.5, 125, 18));
        steps.add(Step.waitTicks(45));
        steps.add(Step.screenshot("soldiers_battle_1"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("soldiers_battle_2"));
        // Rekrutierungstisch und Anwerbe-Fenster
        steps.add(tp(30.5, 1.4, oz + 2.6, 180, 32));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("recruitment_table"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.hud(true));
        steps.add(Step.screen(minecraft -> new RecruitScreen(new BlockPos(30, ground, oz), 32)));
        steps.add(Step.waitTicks(15));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("recruit_screen_1"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof RecruitScreen screen) {
                screen.select(SoldierType.KNIGHT);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("recruit_screen_2"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.waitTicks(5));
    }

    private static void spawnSoldier(ServerLevel level, SoldierType type, int variant, int x, int z, float yaw,
            java.util.function.Consumer<SoldierEntity> setup) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (soldier == null) {
            return;
        }
        soldier.equip();
        soldier.setVariant(variant);
        soldier.giveRandomName();
        soldier.setNoAi(true);
        soldier.snapTo(x + 0.5, ground, z + 0.5, yaw, 0);
        soldier.setYBodyRot(yaw);
        soldier.setYHeadRot(yaw);
        level.addFreshEntity(soldier);
        setup.accept(soldier);
    }

    private static void spawnDragon(ServerLevel level, DragonType type, DragonStage stage, BlockPos pos, float yaw,
            java.util.function.Consumer<DragonEntity> setup) {
        DragonEntity dragon = ModEntities.DRAGONS.get(type).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
        if (dragon == null) {
            return;
        }
        dragon.applyStage(stage, true);
        dragon.setNoAi(true);
        dragon.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0);
        dragon.setYBodyRot(yaw);
        dragon.setYHeadRot(yaw);
        dragon.setPersistenceRequired();
        level.addFreshEntity(dragon);
        setup.accept(dragon);
    }

    /** Die vier Kreativ-Tabs der Mod. */
    private static void items(Deque<Step> steps) {
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        List<CreativeModeTab> tabs = List.of(ModCreativeTabs.BUILDING.get(), ModCreativeTabs.RESOURCES.get(),
                ModCreativeTabs.DRAGONS.get(), ModCreativeTabs.FARMING.get(), ModCreativeTabs.KINGDOM.get(),
                ModCreativeTabs.EQUIPMENT.get());
        String[] names = {"tab_building", "tab_resources", "tab_dragons", "tab_farming", "tab_kingdom", "tab_equipment"};
        for (int i = 0; i < tabs.size(); i++) {
            CreativeModeTab tab = tabs.get(i);
            steps.add(Step.screen(minecraft -> new CreativeModeInventoryScreen(minecraft.player,
                    minecraft.player.connection.enabledFeatures(), false)));
            steps.add(Step.waitTicks(5));
            steps.add(Step.once(minecraft -> {
                selectTab(minecraft, tab);
                parkMouse(minecraft);
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot(names[i]));
            if (i == 0) {
                // Der Bau-Tab ist länger als eine Seite: ans Ende blättern
                steps.add(Step.once(minecraft -> minecraft.gui.screen().mouseScrolled(100, 100, 0, -30)));
                steps.add(Step.waitTicks(5));
                steps.add(Step.screenshot(names[i] + "_2"));
            }
            steps.add(Step.screen(minecraft -> null));
            steps.add(Step.waitTicks(5));
        }
        steps.add(Step.command("gamemode spectator"));
    }

    /**
     * Pyrrhons Vulkankrater in einer echten (nicht flachen) Welt: Lage aus dem {@code NestLocator}, Bilder aus der Luft,
     * vom Kraterrand, in der Dämmerung und beim Erwachen des Bosses – zum Schluss der Drachenkompass in der Hand.
     */
    private static void lair(Deque<Step> steps, DragonType type) {
        String id = type.id();
        double[] nest = new double[4];   // x, z, Bodenhöhe der Mitte, gefunden (1) / keins (-1)
        locateNest(steps, nest, type);
        steps.add(Step.until(() -> nest[3] != 0, 1200));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(lookFrom(nest, 0, 70, 105, 0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitTicks(400));
        steps.add(Step.waitForChunks(3000));
        steps.add(Step.waitTicks(60));
        steps.add(Step.server(level -> {
            var entry = io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(level).get(type);
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Nesteintrag: {}", entry);
            if (entry != null) {
                nest[2] = entry.center().getY();
            }
        }));
        steps.add(Step.screenshot("lair_" + id + "_overview"));
        // Aschelande: aus großer Höhe und vom Boden aus
        steps.add(lookFrom(nest, 40, 95, 150, 0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("lair_" + id + "_zone_aerial"));
        groundView(steps, nest, 50, 52, 6.0, 14, "lair_" + id + "_zone_ground");
        groundView(steps, nest, -78, 18, 7.0, 10, "lair_" + id + "_zone_ground_2");
        groundView(steps, nest, 25, -82, 6.0, 12, "lair_" + id + "_zone_ground_3");
        steps.add(lookFrom(nest, -70, 55, -80, 0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("lair_" + id + "_overview_2"));
        steps.add(lookFrom(nest, -8, 27, 29, 0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.screenshot("lair_" + id + "_rim"));
        steps.add(lookFrom(nest, 5, 5, 13, 2));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.screenshot("lair_" + id + "_inside"));
        // Dämmerung: die Lava leuchtet
        steps.add(Step.command("time set 13200"));
        steps.add(lookFrom(nest, 0, 45, 80, 0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.screenshot("lair_" + id + "_dusk"));
        // Erwachen über den Nestkern
        steps.add(lookFrom(nest, 2, 8, 23, 6));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            var entry = io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(level).get(type);
            BlockPos center = entry != null ? entry.center() : BlockPos.containing(nest[0], nest[2], nest[1]);
            BlockPos core = io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairDesigns.of(type).corePos(center);
            if (level.getBlockEntity(core) instanceof io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity be) {
                var boss = be.awaken(level);
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Nestkern bei {} weckt {}", core, boss);
            } else {
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Kein Nestkern bei {}: {}", core, level.getBlockState(core));
            }
        }));
        steps.add(Step.waitTicks(70));
        steps.add(Step.screenshot("lair_" + id + "_awakening_1"));
        steps.add(Step.waitTicks(55));
        steps.add(Step.screenshot("lair_" + id + "_awakening_2"));
        steps.add(Step.waitTicks(55));
        steps.add(Step.screenshot("lair_" + id + "_awakening_3"));
        // Drachenkompass in der Hand, ein Stück außerhalb des Kraters (die Nadel zeigt zum Nest)
        steps.add(Step.command("kill @e[type=kingdomsatwar:" + id + "_dragon_boss]"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("give @s kingdomsatwar:dragon_compass"));
        steps.add(Step.server(level -> level.players().forEach(player -> {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.getInventory().setSelectedSlot(0);
        })));
        steps.add(lookFrom(nest, 40, 40, 160, 10));
        steps.add(Step.waitTicks(60));
        steps.add(Step.server(level -> level.players().forEach(player ->
                player.getMainHandItem().use(level, player, net.minecraft.world.InteractionHand.MAIN_HAND))));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("lair_" + id + "_compass"));
        steps.add(Step.command("gamemode spectator"));
    }

    /**
     * Der Siegelkreis in einer echten Welt: aus der Luft, vom Boden, ein Monolith aus der Nähe, die Chronik, eine
     * Kapitelkarte, drei leuchtende Monolithen in der Dämmerung und der ganze Siegelbruch als Bildfolge (Video).
     */
    private static void seal(Deque<Step> steps) {
        double[] c = new double[4];   // x, z, Bodenhöhe der Mitte, gefunden (1) / keiner (-1)
        steps.add(Step.server(level -> {
            var chunk = io.github.sirschrolli.kingdomsatwar.worldgen.seal.SealCircleLocator.find(level);
            if (chunk == null) {
                c[3] = -1;
                return;
            }
            for (int x = chunk.x() - 4; x <= chunk.x() + 4; x++) {
                for (int z = chunk.z() - 4; z <= chunk.z() + 4; z++) {
                    level.getChunk(x, z);
                }
            }
            BlockPos center = io.github.sirschrolli.kingdomsatwar.world.SealCircle.findCenter(level);
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Siegelkreis: Chunk {} Mitte {}", chunk, center);
            if (center == null) {
                c[3] = -1;
                return;
            }
            c[0] = center.getX() + 0.5;
            c[1] = center.getZ() + 0.5;
            c[2] = center.getY() + 1;
            c[3] = 1;
        }));
        steps.add(Step.until(() -> c[3] != 0, 1200));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.command("time set 2000"));
        steps.add(Step.command("weather clear"));
        steps.add(lookFrom(c, 0, 42, 48, 0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(2000));
        steps.add(Step.waitTicks(200));
        steps.add(Step.screenshot("seal_overview"));
        steps.add(lookFrom(c, 34, 16, 26, 3));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(800));
        steps.add(Step.screenshot("seal_angle"));
        steps.add(lookFrom(c, 4, 1.6, 19, 5));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(800));
        steps.add(Step.screenshot("seal_ground"));
        // ein Monolith aus der Nähe: der Feuer-Monolith steht im Norden, sein Sockel zeigt zur Mitte
        steps.add(lookAt(c, 2.5, 1.6, -7.0, 0.0, 3.0, -12.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("seal_monolith"));
        // Chronik und Kapitelkarte (mit Anzeige) – im Kreativmodus gilt der Siegelkreis als gefunden
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(level -> level.players().forEach(player -> {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        })));
        steps.add(Step.waitTicks(50));
        steps.add(Step.once(minecraft -> io.github.sirschrolli.kingdomsatwar.client.story.ChapterCardOverlay.clear()));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.server(level -> level.players().forEach(player ->
                io.github.sirschrolli.kingdomsatwar.story.Story.openChronicle(player))));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen()
                instanceof io.github.sirschrolli.kingdomsatwar.client.story.ChronicleScreen, 100));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("chronicle_dragons"));
        for (int part : new int[] {0, 1, 2}) {
            steps.add(Step.once(minecraft -> {
                if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.story.ChronicleScreen screen) {
                    screen.select(part);
                }
            }));
            steps.add(Step.waitTicks(5));
            steps.add(Step.screenshot("chronicle_part_" + part));
        }
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> io.github.sirschrolli.kingdomsatwar.client.story.ChapterCardOverlay.clear()));
        steps.add(lookFrom(c, 0, 9, 30, 4));
        steps.add(Step.server(level -> level.players().forEach(player -> io.github.sirschrolli.kingdomsatwar.story.Story.sendCard(player,
                io.github.sirschrolli.kingdomsatwar.story.Chapter.DRAGON_2, DragonType.FIRE.ordinal()))));
        steps.add(Step.waitTicks(90));
        steps.add(Step.screenshot("chapter_card"));
        steps.add(Step.waitTicks(200));
        steps.add(Step.hud(false));
        // drei Splitter eingesetzt – in der Dämmerung leuchten Runen und Bodenzeichen
        steps.add(Step.command("time set 13300"));
        steps.add(Step.server(level -> {
            for (DragonType type : new DragonType[] {DragonType.FIRE, DragonType.ICE, DragonType.NATURE}) {
                boolean ok = io.github.sirschrolli.kingdomsatwar.world.SealCircle.insert(level, type);
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Splitter {} eingesetzt: {}", type, ok);
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(lookFrom(c, 22, 13, 22, 3));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("seal_three_lit"));
        steps.add(lookAt(c, 2.5, 1.6, -7.0, 0.0, 3.5, -12.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("seal_monolith_lit"));
        // die letzten drei Splitter: das Siegel bricht (Bildfolge aus langsam kreisender Kamera)
        steps.add(Step.command("time set 14500"));
        double[] orbit = new double[1];
        steps.add(lookFrom(c, 0, 16, 32, 6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            for (DragonType type : new DragonType[] {DragonType.LIGHTNING, DragonType.EARTH, DragonType.WATER}) {
                io.github.sirschrolli.kingdomsatwar.world.SealCircle.insert(level, type);
            }
        }));
        steps.add(Step.record("seal_break", io.github.sirschrolli.kingdomsatwar.world.SealCircle.BREAK_TICKS + 20, minecraft -> {
            orbit[0] += 0.0035;
            double a = Math.PI / 2 + orbit[0];
            double r = 31.0;
            double cx = c[0] + Math.cos(a) * r;
            double cz = c[1] + Math.sin(a) * r;
            double cy = c[2] + 14.0;
            double lx = c[0] - cx;
            double ly = c[2] + 6.0 - cy;
            double lz = c[1] - cz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cx, cy, cz, yaw, pitch));
        }));
        steps.add(Step.command("tick unfreeze"));
        steps.add(Step.waitTicks(40));
        steps.add(lookAt(c, 0.0, 6.5, 9.0, 0.0, 0.0, 0.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("seal_gate"));
        steps.add(Step.hud(true));
        steps.add(Step.server(level -> level.players().forEach(player ->
                io.github.sirschrolli.kingdomsatwar.story.Story.openChronicle(player))));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen()
                instanceof io.github.sirschrolli.kingdomsatwar.client.story.ChronicleScreen, 100));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("chronicle_seal"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        steps.add(Step.command("gamemode spectator"));
    }

    /** Kamera relativ zur Mitte (dx, dy, dz) setzen und auf einen Punkt relativ zur Mitte (tx, ty, tz) blicken lassen. */
    private static Step lookAt(double[] c, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            double cx = c[0] + dx;
            double cy = c[2] + dy;
            double cz = c[1] + dz;
            double lx = c[0] + tx - cx;
            double ly = c[2] + ty - cy;
            double lz = c[1] + tz - cz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cx, cy, cz, yaw, pitch));
        });
    }

    /** Sucht das Nest einer Art in dieser Welt (x, z, Bodenhöhe der Mitte, gefunden = 1 / keins = -1) und erzeugt seine Chunks. */
    private static void locateNest(Deque<Step> steps, double[] nest, DragonType type) {
        steps.add(Step.server(level -> {
            var nests = io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator.all(level);
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Nester dieser Welt: {}", nests);
            var chunk = nests.get(type);
            if (chunk == null) {
                nest[3] = -1;
                return;
            }
            nest[0] = chunk.getMiddleBlockX() + 0.5;
            nest[1] = chunk.getMiddleBlockZ() + 0.5;
            // das ganze Nest erzeugen lassen (dauert einen Moment)
            for (int x = chunk.x() - 8; x <= chunk.x() + 8; x++) {
                for (int z = chunk.z() - 8; z <= chunk.z() + 8; z++) {
                    level.getChunk(x, z);
                }
            }
            var entry = io.github.sirschrolli.kingdomsatwar.world.LairRecords.of(level).get(type);
            nest[2] = entry != null ? entry.center().getY()
                    : level.getHeight(Heightmap.Types.MOTION_BLOCKING, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ());
            nest[3] = 1;
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Nest {} bei {} {} {}", type.id(), nest[0], nest[2], nest[1]);
        }));
    }

    /**
     * Bewegungen des Drachen-Bosses als Bildfolgen (werden zu Videos zusammengesetzt): Erwachen, Kampf gegen ein paar
     * Soldaten (Gehen, Drehen, Angriffe, Launen) und Flug. Die Kamera folgt dem Boss von der Seite.
     */
    private static void bossMotion(Deque<Step> steps, DragonType type) {
        String id = type.id();
        BlockState floorBlock = switch (type) {
            case ICE -> Blocks.SNOW_BLOCK.defaultBlockState();
            case NATURE -> Blocks.GRASS_BLOCK.defaultBlockState();
            case EARTH -> Blocks.COARSE_DIRT.defaultBlockState();
            case LIGHTNING -> ModBlocks.STORMSTONE.base().get().defaultBlockState();
            default -> Blocks.BASALT.defaultBlockState();
        };
        BlockState poolBlock = switch (type) {
            case ICE -> Blocks.PACKED_ICE.defaultBlockState();
            case NATURE -> Blocks.MOSS_BLOCK.defaultBlockState();
            case EARTH -> Blocks.GRAVEL.defaultBlockState();
            case LIGHTNING -> ModBlocks.STORMSTONE.base().get().defaultBlockState();
            default -> Blocks.LAVA.defaultBlockState();
        };
        int ox = 600;
        int oz = 0;
        DragonBossEntity[] boss = new DragonBossEntity[1];
        steps.add(tp(ox, 30, oz - 40, 0, 30));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.waitTicks(40));
        steps.add(Step.command("time set 9000"));
        steps.add(Step.server(level -> {
            fill(level, new BlockPos(ox - 60, ground - 3, oz - 60), new BlockPos(ox + 60, ground - 1, oz + 60), floorBlock);
            fill(level, new BlockPos(ox - 60, ground, oz - 60), new BlockPos(ox + 60, ground + 40, oz + 60), Blocks.AIR.defaultBlockState());
            for (BlockPos pos : BlockPos.betweenClosed(ox - 6, ground - 3, oz - 6, ox + 6, ground - 1, oz + 6)) {
                if (pos.distSqr(new BlockPos(ox, pos.getY(), oz)) <= 30) {
                    level.setBlock(pos, poolBlock, Block.UPDATE_CLIENTS);
                }
            }
            if (type == DragonType.ICE) {
                // der Eiskristall, den Glaciara beim Erwachen sprengt
                fill(level, new BlockPos(ox - 3, ground, oz - 3), new BlockPos(ox + 3, ground + 10, oz + 3),
                        ModBlocks.FROST_CRYSTAL.get().defaultBlockState());
            }
            if (type == DragonType.EARTH) {
                var ctx = new io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext(level,
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(ox - 40, ground - 8, oz - 40, ox + 40, ground + 20, oz + 40),
                        new BlockPos(ox, ground, oz), 1234L);
                io.github.sirschrolli.kingdomsatwar.worldgen.lair.EarthLairDesign.mound(ctx, ox, ground, oz);
            }
            if (type == DragonType.LIGHTNING) {
                // das Podest mit dem Blitzstern, auf das Voltarion herabfährt
                var ctx = new io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext(level,
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(ox - 40, ground - 8, oz - 40, ox + 40, ground + 20, oz + 40),
                        new BlockPos(ox, ground, oz), 1234L);
                io.github.sirschrolli.kingdomsatwar.worldgen.lair.LightningLairDesign.dais(ctx, ox, ground, oz);
            }
            if (type == DragonType.NATURE) {
                // der überwucherte Hügel und die Lebensblüten
                var ctx = new io.github.sirschrolli.kingdomsatwar.worldgen.lair.LairContext(level,
                        new net.minecraft.world.level.levelgen.structure.BoundingBox(ox - 40, ground - 8, oz - 40, ox + 40, ground + 20, oz + 40),
                        new BlockPos(ox, ground, oz), 1234L);
                io.github.sirschrolli.kingdomsatwar.worldgen.lair.NatureLairDesign.mound(ctx, ox, ground, oz);
                for (BlockPos spot : io.github.sirschrolli.kingdomsatwar.worldgen.lair.NatureLairDesign.blossomRing(new BlockPos(ox, ground, oz))) {
                    io.github.sirschrolli.kingdomsatwar.worldgen.lair.NatureLairDesign.blossomBed(ctx, spot.getX(), ground, spot.getZ());
                }
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Step.server(level -> {
            var b = ModEntities.bossType(type).create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            b.snapTo(ox + 0.5, ground - 11, oz + 0.5, 200, 0);
            b.yBodyRot = 200;
            b.yHeadRot = 200;
            level.addFreshEntity(b);
            b.beginAwakening(level, new BlockPos(ox, ground, oz));
            boss[0] = b;
        }));
        steps.add(Step.waitTicks(2));
        if (type == DragonType.LIGHTNING) {
            // er fährt aus dem Himmel herab: Die Kamera bleibt beim Podest
            steps.add(Step.record("motion_" + id + "_awakening", 195, fixedCam(ox, oz, 15.0, 6.5, -14.0, 5.0)));
        } else {
            steps.add(Step.record("motion_" + id + "_awakening", type == DragonType.NATURE ? 200 : type == DragonType.EARTH ? 190 : 175,
                    follow(boss, 24.0, 8.0, 7.0)));
        }
        // Kampf: ein paar Soldaten, damit der Boss geht, sich dreht und angreift
        steps.add(Step.server(level -> {
            for (int i = 0; i < 5; i++) {
                double a = -0.6 + i * 0.3;
                SoldierType soldierType = i % 2 == 0 ? SoldierType.KNIGHT : SoldierType.ARCHER;
                var soldier = ModEntities.SOLDIERS.get(soldierType).get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (soldier != null) {
                    soldier.snapTo(ox + Math.sin(a) * 14, ground, oz - Math.cos(a) * 14, 0, 0);
                    soldier.setPermanentlyInvulnerable(true);
                    level.addFreshEntity(soldier);
                }
            }
        }));
        steps.add(Step.record("motion_" + id + "_fight", 300, follow(boss, 22.0, 6.0, 5.0)));
        int[][] special = switch (type) {
            // Wurzelfessel, Rankensäulen, Giftsporen, Rankendiener, Heilung
            case NATURE -> new int[][] {{BossAnims.SPECIAL_1, 70}, {BossAnims.ERUPT, 60}, {BossAnims.RING, 60}, {BossAnims.STOMP, 70},
                    {BossAnims.SPECIAL_2, 125}};
            // Eingraben, Stalagmiten, Erdbeben, Felsbrocken, Kristallpanzer
            case EARTH -> new int[][] {{BossAnims.SPECIAL_1, 175}, {BossAnims.ERUPT, 60}, {BossAnims.STOMP, 65}, {BossAnims.VOLLEY, 60},
                    {BossAnims.SPECIAL_2, 90}};
            default -> new int[0][];
        };
        for (int i = 0; i < special.length; i++) {
            int anim = special[i][0];
            steps.add(Step.server(level -> boss[0].setHealth(boss[0].getMaxHealth() * 0.5f)));
            steps.add(forceAttack(boss, anim));
            steps.add(Step.record("motion_" + id + "_special_" + i, special[i][1], follow(boss, 24.0, 7.0, 5.0)));
        }
        if (type == DragonType.LIGHTNING) {
            // Kettenblitz, Blitzfeld, Donnerschrei, Blitzodem – dann in der Luft: Gewitter und Sturzflug
            Object[][] groundAttacks = {{"chain_lightning", 60}, {"lightning_field", 160}, {"thunder_roar", 50}, {"breath", 80}};
            int i = 0;
            // zuerst landen (er fliegt viel): auf den Boden setzen und kurz zur Ruhe kommen lassen
            steps.add(Step.server(level -> {
                DragonBossEntity b = boss[0];
                b.setMoveMode(DragonBossEntity.MoveMode.GROUND);
                b.setPos(b.getX(), b.groundY(b.getX(), b.getZ()), b.getZ());
                b.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            }));
            steps.add(Step.record("motion_" + id + "_walk", 70, follow(boss, 24.0, 6.0, 5.0)));
            for (Object[] attack : groundAttacks) {
                steps.add(Step.server(level -> boss[0].setHealth(boss[0].getMaxHealth() * 0.5f)));
                steps.add(forceAttack(boss, (String) attack[0]));
                steps.add(Step.record("motion_" + id + "_special_" + i++, (Integer) attack[1], follow(boss, 26.0, 7.0, 5.0)));
            }
            steps.add(forceAttack(boss, BossAnims.TAKE_OFF));
            steps.add(Step.record("motion_" + id + "_flight", 140, follow(boss, 32.0, 5.0, 4.0)));
            steps.add(forceAttack(boss, "thunderstorm"));
            steps.add(Step.record("motion_" + id + "_special_" + i++, 125, follow(boss, 34.0, 4.0, 2.0)));
            steps.add(forceAttack(boss, "dive_charge"));
            steps.add(Step.record("motion_" + id + "_special_" + i, 95, follow(boss, 30.0, 6.0, 3.0)));
            steps.add(Step.hud(true));
            return;
        }
        if (type != DragonType.EARTH) {
            steps.add(forceAttack(boss, BossAnims.TAKE_OFF));
            steps.add(Step.record("motion_" + id + "_flight", 240, follow(boss, 30.0, 5.0, 4.0)));
        }
        steps.add(Step.hud(true));
    }

    /** Feste Kamera (dx, dy, dz) neben dem Punkt (ox, Boden, oz), Blick auf (ox, Boden + lookUp, oz). */
    private static java.util.function.Consumer<net.minecraft.client.Minecraft> fixedCam(double ox, double oz, double dx, double dy, double dz,
            double lookUp) {
        float yaw = (float) (-Math.atan2(-dx, -dz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(lookUp - dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
        return minecraft -> minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                ox + dx, ground + dy, oz + dz, yaw, pitch));
    }

    /** Kamera folgt dem Boss: seitlich in {@code distance} Blöcken, {@code up} über ihm, Blick auf {@code lookUp}. */
    private static java.util.function.Consumer<net.minecraft.client.Minecraft> follow(DragonBossEntity[] boss, double distance, double up,
            double lookUp) {
        double[] cam = new double[4];   // geglättete Kameraposition, gesetzt (1)
        return minecraft -> {
            DragonBossEntity b = boss[0];
            if (b == null) {
                return;
            }
            net.minecraft.world.phys.Vec3 center = b.position().add(0, 3.0, 0);
            // Blick schräg von vorn-seitlich auf den Körper
            net.minecraft.world.phys.Vec3 side = net.minecraft.world.phys.Vec3.directionFromRotation(0, b.yBodyRot + 115.0f);
            net.minecraft.world.phys.Vec3 want = center.add(side.scale(distance)).add(0, up, 0);
            if (cam[3] == 0) {
                cam[0] = want.x;
                cam[1] = want.y;
                cam[2] = want.z;
                cam[3] = 1;
            } else {
                cam[0] += (want.x - cam[0]) * 0.06;
                cam[1] += (want.y - cam[1]) * 0.06;
                cam[2] += (want.z - cam[2]) * 0.06;
            }
            net.minecraft.world.phys.Vec3 look = b.position().add(0, lookUp, 0).subtract(cam[0], cam[1], cam[2]);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cam[0], cam[1], cam[2], yaw, pitch));
        };
    }

    /** Bild vom Boden aus: Kamera auf Augenhöhe über dem Gelände bei (dx, dz), Blick zur Nestmitte. */
    private static void groundView(Deque<Step> steps, double[] nest, double dx, double dz, double eye, double lookUp, String name) {
        double[] y = new double[1];
        steps.add(Step.server(level -> y[0] = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                (int) Math.floor(nest[0] + dx), (int) Math.floor(nest[1] + dz))));
        steps.add(Step.waitTicks(3));
        steps.add(Step.once(minecraft -> {
            double cx = nest[0] + dx;
            double cy = y[0] + eye;
            double cz = nest[1] + dz;
            double lx = nest[0] - cx;
            double ly = nest[2] + lookUp - cy;
            double lz = nest[1] - cz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cx, cy, cz, yaw, pitch));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot(name));
    }

    /** Fehlersuche: freiliegender Sand/Sandstein im inneren Biom und Bauwerke in der Nähe. */
    private static void debugAshlands(ServerLevel level, double[] nest) {
        int cx = (int) Math.floor(nest[0]);
        int cz = (int) Math.floor(nest[1]);
        int found = 0;
        java.util.Map<String, Integer> kinds = new java.util.TreeMap<>();
        for (int x = cx - 90; x <= cx + 90; x++) {
            for (int z = cz - 90; z <= cz + 90; z++) {
                double d = Math.hypot(x - cx, z - cz);
                if (d < 50 || d > 85) {
                    continue;
                }
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = top; y > top - 50; y--) {
                    BlockPos pos = new BlockPos(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (!(state.is(net.minecraft.tags.BlockTags.SAND) || state.getBlock().getDescriptionId().contains("sandstone"))) {
                        continue;
                    }
                    boolean exposed = false;
                    for (Direction dir : Direction.values()) {
                        if (level.getBlockState(pos.relative(dir)).isAir()) {
                            exposed = true;
                        }
                    }
                    if (exposed) {
                        kinds.merge(state.getBlock().getDescriptionId(), 1, Integer::sum);
                        if (found++ < 12) {
                            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Sand frei: {} {} d={} top={}",
                                    pos.toShortString(), state, (int) d, top);
                        }
                    }
                }
            }
        }
        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Freiliegend im inneren Biom: {} Blöcke {}", found, kinds);
        java.util.Map<String, Integer> plants = new java.util.TreeMap<>();
        for (int x = cx - 110; x <= cx + 110; x++) {
            for (int z = cz - 110; z <= cz + 110; z++) {
                double d = Math.hypot(x - cx, z - cz);
                if (d < 50 || d > 95) {
                    continue;
                }
                int top = level.getHeight(Heightmap.Types.WORLD_SURFACE, x, z);
                for (int y = top; y > top - 4; y--) {
                    BlockState state = level.getBlockState(new BlockPos(x, y, z));
                    if (state.is(Blocks.CACTUS) || state.is(net.minecraft.tags.BlockTags.REPLACEABLE_BY_TREES) || state.is(net.minecraft.tags.BlockTags.LEAVES)) {
                        plants.merge(state.getBlock().getDescriptionId(), 1, Integer::sum);
                    }
                }
            }
        }
        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Pflanzen im inneren Biom: {}", plants);
        java.util.Set<String> structures = new java.util.TreeSet<>();
        for (int x = cx - 128; x <= cx + 128; x += 16) {
            for (int z = cz - 128; z <= cz + 128; z += 16) {
                level.structureManager().getAllStructuresAt(new BlockPos(x, 64, z)).keySet().forEach(structure ->
                        structures.add(level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.STRUCTURE)
                                .getKey(structure).toString()));
            }
        }
        io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Bauwerke um das Nest: {}", structures);
    }

    /** Wartet, bis der Spieler alle Chunks im Umkreis {@code radius} um die Nestmitte erhalten hat. */
    private static Step chunksAround(double[] nest, int radius, int maxTicks) {
        return (minecraft, ticks) -> {
            if (ticks > maxTicks) {
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Chunks um das Nest nach {} Ticks noch nicht da", ticks);
                return true;
            }
            if (minecraft.level == null || ticks < 20) {
                return false;
            }
            int cx = net.minecraft.core.SectionPos.blockToSectionCoord((int) Math.floor(nest[0]));
            int cz = net.minecraft.core.SectionPos.blockToSectionCoord((int) Math.floor(nest[1]));
            for (int x = cx - radius; x <= cx + radius; x++) {
                for (int z = cz - radius; z <= cz + radius; z++) {
                    if (!minecraft.level.getChunkSource().hasChunk(x, z)) {
                        return false;
                    }
                }
            }
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Chunks um das Nest nach {} Ticks da", ticks);
            return true;
        };
    }

    /** Kamera relativ zur Nestmitte setzen und auf die Mitte (plus {@code lookUp}) blicken lassen. */
    private static Step lookFrom(double[] nest, double dx, double dy, double dz, double lookUp) {
        return Step.once(minecraft -> {
            double cx = nest[0] + dx;
            double cy = nest[2] + dy;
            double cz = nest[1] + dz;
            double lx = nest[0] - cx;
            double ly = nest[2] + lookUp - cy;
            double lz = nest[1] - cz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cx, cy, cz, yaw, pitch));
        });
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /** Kamera (Zuschauer) an eine Stelle relativ zur Bodenhöhe setzen. */
    private static Step tp(int x, int dy, int z, float yaw, float pitch) {
        return Step.once(minecraft -> minecraft.player.connection.sendCommand(
                "tp @s %d %d %d %s %s".formatted(x, ground + dy, z, yaw, pitch)));
    }

    private static Step tp(double x, double dy, double z, float yaw, float pitch) {
        return Step.once(minecraft -> minecraft.player.connection.sendCommand(
                String.format(java.util.Locale.ROOT, "tp @s %.2f %.2f %.2f %s %s", x, ground + dy, z, yaw, pitch)));
    }

    /** Mauszeiger aus dem Bild schieben, damit keine Tooltips erscheinen. */
    static void parkMouse(Minecraft minecraft) {
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, 1.0);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }

    private static void selectTab(Minecraft minecraft, CreativeModeTab tab) {
        if (minecraft.gui.screen() instanceof CreativeModeInventoryScreen screen) {
            try {
                // Nur Entwicklungscode: selectTab ist privat
                Method select = CreativeModeInventoryScreen.class.getDeclaredMethod("selectTab", CreativeModeTab.class);
                select.setAccessible(true);
                select.invoke(screen, tab);
            } catch (ReflectiveOperationException e) {
                throw new IllegalStateException(e);
            }
        }
    }

    private static void fill(ServerLevel level, BlockPos from, BlockPos to, BlockState state) {
        for (BlockPos pos : BlockPos.betweenClosed(from, to)) {
            level.setBlock(pos, state, Block.UPDATE_CLIENTS);
        }
    }

    private static void place(ServerLevel level, BlockPos pos, Block block) {
        BlockState state = block.defaultBlockState();
        if (block instanceof CropBlock crop) {
            level.setBlock(pos.below(), Blocks.FARMLAND.defaultBlockState(), Block.UPDATE_CLIENTS);
            state = crop.getStateForAge(crop.getMaxAge());
        } else if (block instanceof FlowerBlock) {
            level.setBlock(pos.below(), Blocks.GRASS_BLOCK.defaultBlockState(), Block.UPDATE_CLIENTS);
        } else if (block instanceof StairBlock) {
            state = state.setValue(StairBlock.FACING, Direction.SOUTH).setValue(StairBlock.HALF, Half.BOTTOM);
        }
        level.setBlock(pos, state, Block.UPDATE_ALL);
    }
}
