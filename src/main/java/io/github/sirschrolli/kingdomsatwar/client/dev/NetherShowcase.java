package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Borstling;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutschleim;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Lavastelzer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Moderkeiler;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Rauchqualle;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.RottenBristling;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung des Nether-Volks und seiner Tiere (E5a): Borstlinge in drei Gestalten (mit Armbrust, Goldschwert, einer
 * bewundert einen Goldbarren), der Borstenhüne mit Goldaxt, zwei Moderborstlinge, die Kinder und ein tanzender Borstling
 * vor einer Mauer aus Netherziegeln; die drei Glutkeiler mit Frischlingen und zwei Moderkeilern im Karmesinwald; Glut- und
 * Seelenstelzer in einem Lavasee (einer gesattelt, ein Junges) und ein frierender am Ufer.
 */
final class NetherShowcase {
    private static final int OZ = 1400;

    private NetherShowcase() {}

    private static int group(int index) {
        return -200 + index * 40;
    }

    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double z, float yaw, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("kein " + type.getDescriptionId());
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        if (mob instanceof AbstractPiglin piglin) {
            piglin.setImmuneToZombification(true);                         // die Vorführwelt ist die Oberwelt
        }
        if (mob instanceof Glutkeiler keiler) {
            keiler.setImmuneToZombification(true);
        }
        mob.snapTo(x, Showcase.ground, z, yaw, 0.0f);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setup.accept(mob);
        level.addFreshEntity(mob);
        return mob;
    }

    private static void stage(ServerLevel level, int x0) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 12, g - 1, OZ - 16), new BlockPos(x0 + 30, g - 1, OZ + 20), Blocks.NETHERRACK.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 6, g - 1, OZ - 6), new BlockPos(x0 + 24, g - 1, OZ + 4), Blocks.CRIMSON_NYLIUM.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 12, g, OZ - 16), new BlockPos(x0 + 30, g + 10, OZ + 20), Blocks.AIR.defaultBlockState());
        BlockState bricks = Blocks.NETHER_BRICKS.defaultBlockState();
        Showcase.fill(level, new BlockPos(x0 - 6, g, OZ - 5), new BlockPos(x0 + 24, g + 4, OZ - 5), bricks);
        for (int x = x0 - 6; x <= x0 + 24; x += 6) {
            Showcase.fill(level, new BlockPos(x, g, OZ - 4), new BlockPos(x, g + 5, OZ - 4), Blocks.CRIMSON_STEM.defaultBlockState());
            level.setBlock(new BlockPos(x, g + 6, OZ - 4), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
        }
        for (int x = x0 - 5; x <= x0 + 23; x += 2) {
            if ((x - x0) % 6 != 0) {
                level.setBlock(new BlockPos(x, g, OZ - 4), (x % 4 == 0 ? Blocks.CRIMSON_ROOTS : Blocks.CRIMSON_FUNGUS).defaultBlockState(), 2);
            }
        }
    }

    /** Karmesinwald: Nyzel, Karmesinpilze und -wurzeln, hinten Stämme mit Warzenblöcken und Pilzlichtern. */
    private static void crimsonGrove(ServerLevel level, int x0) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 14, g - 1, OZ - 18), new BlockPos(x0 + 32, g - 1, OZ + 20), Blocks.NETHERRACK.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 10, g - 1, OZ - 10), new BlockPos(x0 + 28, g - 1, OZ + 6), Blocks.CRIMSON_NYLIUM.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 14, g, OZ - 18), new BlockPos(x0 + 32, g + 12, OZ + 20), Blocks.AIR.defaultBlockState());
        for (int x = x0 - 8; x <= x0 + 26; x += 7) {
            int h = 5 + Math.floorMod(x, 3);
            Showcase.fill(level, new BlockPos(x, g, OZ - 7), new BlockPos(x, g + h, OZ - 7), Blocks.CRIMSON_STEM.defaultBlockState());
            Showcase.fill(level, new BlockPos(x - 2, g + h - 1, OZ - 9), new BlockPos(x + 2, g + h + 1, OZ - 5), Blocks.NETHER_WART_BLOCK.defaultBlockState());
            level.setBlock(new BlockPos(x, g + h - 2, OZ - 5), Blocks.SHROOMLIGHT.defaultBlockState(), 2);
            level.setBlock(new BlockPos(x, g + h + 2, OZ - 7), Blocks.NETHER_WART_BLOCK.defaultBlockState(), 2);
        }
        for (int x = x0 - 9; x <= x0 + 27; x++) {
            for (int z = OZ - 6; z <= OZ - 3; z++) {
                int r = Math.floorMod(x * 7 + z * 13, 9);
                if (r == 0) {
                    level.setBlock(new BlockPos(x, g, z), Blocks.CRIMSON_FUNGUS.defaultBlockState(), 2);
                } else if (r < 3) {
                    level.setBlock(new BlockPos(x, g, z), Blocks.CRIMSON_ROOTS.defaultBlockState(), 2);
                }
            }
        }
    }

    /** Lavasee mit Ufer aus Basalt und Schwarzstein. */
    private static void lavaLake(ServerLevel level, int x0) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 14, g - 3, OZ - 18), new BlockPos(x0 + 32, g - 1, OZ + 20), Blocks.BLACKSTONE.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 14, g, OZ - 18), new BlockPos(x0 + 32, g + 12, OZ + 20), Blocks.AIR.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 6, g - 2, OZ - 8), new BlockPos(x0 + 22, g - 1, OZ + 1), Blocks.LAVA.defaultBlockState());
        for (int x = x0 - 7; x <= x0 + 23; x++) {
            level.setBlock(new BlockPos(x, g, OZ - 9), (Math.floorMod(x, 4) == 0 ? Blocks.BASALT : Blocks.BLACKSTONE).defaultBlockState(), 2);
            if (Math.floorMod(x, 5) == 1) {
                level.setBlock(new BlockPos(x, g + 1, OZ - 9), Blocks.BASALT.defaultBlockState(), 2);
            }
        }
        Showcase.fill(level, new BlockPos(x0 - 6, g - 1, OZ + 2), new BlockPos(x0 + 22, g - 1, OZ + 6), Blocks.SOUL_SOIL.defaultBlockState());
    }

    private static Lavastelzer strider(ServerLevel level, int x0, double dx, double dz, float yaw, Lavastelzer.Look look, Consumer<Lavastelzer> setup) {
        Lavastelzer stelzer = spawn(level, ModEntities.STRIDER.get(), x0 + dx, OZ + dz, yaw, m -> {
            m.setLook(look);
            setup.accept(m);
        });
        stelzer.setNoGravity(true);
        stelzer.snapTo(x0 + dx, Showcase.ground - 0.6, OZ + dz, yaw, 0.0f);    // watet in der Lava
        return stelzer;
    }

    static void nether(Deque<Step> steps) {
        // der Borstenhüne kommt im friedlichen Modus nicht zur Welt – leicht, aber ohne nächtliche Besucher
        steps.add(Step.command("difficulty easy"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            // Borstlinge in drei Gestalten, der Borstenhüne, zwei Moderborstlinge
            int b = group(0);
            stage(level, b);
            spawn(level, ModEntities.PIGLIN.get(), b + 1.5, OZ + 0.5, 10, m -> {
                m.setLook(Borstling.Look.KARMESIN);
                m.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.CROSSBOW));
            });
            spawn(level, ModEntities.PIGLIN.get(), b + 4.5, OZ + 0.5, 350, m -> {
                m.setLook(Borstling.Look.ASCHEN);
                m.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
            });
            spawn(level, ModEntities.PIGLIN.get(), b + 7.5, OZ + 0.5, 15, m -> {
                m.setLook(Borstling.Look.GOLD);
                m.setItemInHand(InteractionHand.OFF_HAND, new ItemStack(Items.GOLD_INGOT));   // bewundert den Barren
            });
            spawn(level, ModEntities.PIGLIN_BRUTE.get(), b + 10.5, OZ + 0.5, 345,
                    m -> m.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_AXE)));
            spawn(level, ModEntities.ZOMBIFIED_PIGLIN.get(), b + 13.5, OZ + 0.5, 10, m -> {
                m.setLook(RottenBristling.Look.MODER_KARMESIN);
                m.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.GOLDEN_SWORD));
            });
            spawn(level, ModEntities.ZOMBIFIED_PIGLIN.get(), b + 16.5, OZ + 0.5, 350, m -> m.setLook(RottenBristling.Look.MODER_ASCHEN));

            // Kinder und ein tanzender Borstling
            b = group(1);
            stage(level, b);
            Borstling.Look[] looks = Borstling.Look.values();
            for (int i = 0; i < looks.length; i++) {
                Borstling.Look look = looks[i];
                spawn(level, ModEntities.PIGLIN.get(), b + 2.5 + i * 2.2, OZ + 1.5, i % 2 == 0 ? 15 : 345, m -> {
                    m.setLook(look);
                    m.setBaby(true);
                });
            }
            spawn(level, ModEntities.ZOMBIFIED_PIGLIN.get(), b + 9.1, OZ + 1.5, 340, m -> {
                m.setLook(RottenBristling.Look.MODER_KARMESIN);
                m.setBaby(true);
            });
            spawn(level, ModEntities.PIGLIN.get(), b + 6.0, OZ - 1.5, 0, m -> {
                m.setLook(Borstling.Look.KARMESIN);
                m.setDancing(true);
            });
        }));
        steps.add(Step.server(level -> {
            // Glutkeiler im Karmesinwald: drei Gestalten, Frischlinge, zwei Moderkeiler
            int b = group(2);
            crimsonGrove(level, b);
            spawn(level, ModEntities.HOGLIN.get(), b + 1.5, OZ - 0.5, 20, m -> m.setLook(Glutkeiler.Look.KARMESIN));
            spawn(level, ModEntities.HOGLIN.get(), b + 7.5, OZ - 0.5, 345, m -> m.setLook(Glutkeiler.Look.ASCHEN));
            spawn(level, ModEntities.HOGLIN.get(), b + 13.5, OZ - 0.5, 15, m -> m.setLook(Glutkeiler.Look.SCHLACKE));
            spawn(level, ModEntities.ZOGLIN.get(), b + 19.5, OZ - 0.5, 340, m -> m.setLook(Moderkeiler.Look.MODER_KARMESIN));
            spawn(level, ModEntities.ZOGLIN.get(), b + 25.0, OZ - 0.5, 20, m -> m.setLook(Moderkeiler.Look.MODER_ASCHEN));
            Glutkeiler.Look[] looks = Glutkeiler.Look.values();
            for (int i = 0; i < looks.length; i++) {
                Glutkeiler.Look look = looks[i];
                spawn(level, ModEntities.HOGLIN.get(), b + 4.4 + i * 6.0, OZ + 0.9, i % 2 == 0 ? 25 : 335, m -> {
                    m.setLook(look);
                    m.setBaby(true);
                });
            }
            spawn(level, ModEntities.ZOGLIN.get(), b + 22.4, OZ + 0.9, 10, m -> {
                m.setLook(Moderkeiler.Look.MODER_KARMESIN);
                m.setBaby(true);
            });

            // Lavastelzer im Lavasee: Glut- und Seelenstelzer, einer gesattelt, ein Junges; am Ufer friert einer
            b = group(3);
            lavaLake(level, b);
            strider(level, b, 2.5, -3.5, 15, Lavastelzer.Look.GLUT, m -> {});
            strider(level, b, 7.5, -4.5, 345, Lavastelzer.Look.SEELE, m -> {});
            strider(level, b, 12.5, -3.5, 20, Lavastelzer.Look.GLUT,
                    m -> m.setItemSlot(EquipmentSlot.SADDLE, new net.minecraft.world.item.ItemStack(Items.SADDLE)));
            strider(level, b, 5.2, -1.0, 0, Lavastelzer.Look.GLUT, m -> m.setAge(-24000));
            strider(level, b, 10.0, -1.0, 340, Lavastelzer.Look.SEELE, m -> m.setAge(-24000));
            for (int i = 0; i < 2; i++) {
                Lavastelzer.Look look = i == 0 ? Lavastelzer.Look.GLUT : Lavastelzer.Look.SEELE;
                spawn(level, ModEntities.STRIDER.get(), b + 17.5 + i * 3.0, OZ + 3.5, i == 0 ? 330 : 20, m -> {
                    m.setLook(look);
                    m.setNoAi(false);                                       // nur so merkt er, dass er an Land friert
                    m.removeFreeWill();
                });
            }
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(50)));
        shot(steps, "nether_folk", group(0) + 9.0, 1.9, 1.15, 10.5, true);
        shot(steps, "nether_close", group(0) + 4.5, 2.0, 1.4, 4.8, false);
        shot(steps, "nether_brute", group(0) + 12.0, 2.0, 1.3, 4.8, false);
        shot(steps, "nether_babies", group(1) + 5.8, 1.5, 0.9, 5.2, true);
        shot(steps, "nether_boars", group(2) + 13.0, 3.0, 0.8, 12.0, true);
        shot(steps, "nether_boar_close", group(2) + 3.6, 2.3, 0.9, 6.0, false);
        shot(steps, "nether_boar_rot", group(2) + 21.8, 2.3, 0.9, 6.0, false);
        shot(steps, "nether_striders", group(3) + 8.5, 2.6, 0.3, 9.0, true);
        shot(steps, "nether_strider_close", group(3) + 6.0, 1.8, 0.2, 7.0, false);
        shot(steps, "nether_strider_cold", group(3) + 19.0, 1.8, 1.0, 10.0, false);
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamerule spawn_mobs true"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /** Basaltdelta mit Festungsmauer: Basalt, Schwarzstein, Magmablöcke, hinten Netherziegel; rechts ein Stück Seelensand. */
    private static void delta(ServerLevel level, int x0) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 14, g - 3, OZ - 30), new BlockPos(x0 + 34, g - 1, OZ + 22), Blocks.BLACKSTONE.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 14, g, OZ - 30), new BlockPos(x0 + 34, g + 24, OZ + 22), Blocks.AIR.defaultBlockState());
        for (int x = x0 - 12; x <= x0 + 32; x++) {
            for (int z = OZ - 28; z <= OZ + 20; z++) {
                int r = Math.floorMod(x * 31 + z * 17 + x * z, 23);
                BlockState top = r < 6 ? Blocks.BASALT.defaultBlockState() : r < 8 ? Blocks.MAGMA_BLOCK.defaultBlockState()
                        : Blocks.BLACKSTONE.defaultBlockState();
                if (x > x0 + 18) {
                    top = r < 5 ? Blocks.SOUL_SOIL.defaultBlockState() : Blocks.SOUL_SAND.defaultBlockState();
                }
                level.setBlock(new BlockPos(x, g - 1, z), top, 2);
            }
        }
        BlockState bricks = Blocks.NETHER_BRICKS.defaultBlockState();
        Showcase.fill(level, new BlockPos(x0 - 8, g, OZ - 7), new BlockPos(x0 + 26, g + 5, OZ - 7), bricks);
        for (int x = x0 - 8; x <= x0 + 26; x += 2) {
            level.setBlock(new BlockPos(x, g + 6, OZ - 7), bricks, 2);
            level.setBlock(new BlockPos(x + 1, g + 6, OZ - 7), Blocks.NETHER_BRICK_FENCE.defaultBlockState(), 2);
        }
        for (int x = x0 - 6; x <= x0 + 24; x += 10) {
            level.setBlock(new BlockPos(x, g + 3, OZ - 6), Blocks.SOUL_LANTERN.defaultBlockState(), 2);
        }
        for (int[] spike : new int[][] {{-10, -20, 7}, {-4, -24, 11}, {30, -18, 8}, {24, -26, 13}, {6, -27, 9}}) {
            Showcase.fill(level, new BlockPos(x0 + spike[0], g, OZ + spike[1]), new BlockPos(x0 + spike[0], g + spike[2], OZ + spike[1]),
                    Blocks.BASALT.defaultBlockState());
        }
    }

    /** Rauchquallen über dem Basaltdelta, Glutgeister vor der Festungsmauer, Glutschleime in drei Größen. */
    static void spirits(Deque<Step> steps) {
        steps.add(Step.command("difficulty easy"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 18000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            int b = group(5);
            delta(level, b);
            // Glutschleime: drei Größen je Gestalt
            int[] sizes = {4, 2, 1};
            for (int i = 0; i < 3; i++) {
                int size = sizes[i];
                spawn(level, ModEntities.MAGMA_CUBE.get(), b + 1.5 + i * 3.4, OZ + 1.5, i == 1 ? 345 : 15, m -> {
                    m.setSize(size, true);
                    m.setLook(Glutschleim.Look.GLUT);
                });
                spawn(level, ModEntities.MAGMA_CUBE.get(), b + 12.0 + i * 3.4, OZ + 1.5, i == 1 ? 15 : 345, m -> {
                    m.setSize(size, true);
                    m.setLook(Glutschleim.Look.BASALT);
                });
            }
            // Glutgeister schweben vor der Festungsmauer
            spawn(level, ModEntities.BLAZE.get(), b + 4.5, OZ - 3.0, 10, m -> {
                m.setLook(Glutgeist.Look.GLUT);
                m.setNoGravity(true);
            }).snapTo(b + 4.5, Showcase.ground + 1.2, OZ - 3.0, 10, 0);
            spawn(level, ModEntities.BLAZE.get(), b + 9.0, OZ - 3.5, 350, m -> {
                m.setLook(Glutgeist.Look.GLUT);
                m.setNoGravity(true);
            }).snapTo(b + 9.0, Showcase.ground + 1.8, OZ - 3.5, 350, 0);
            spawn(level, ModEntities.BLAZE.get(), b + 21.5, OZ - 3.0, 345, m -> {
                m.setLook(Glutgeist.Look.SEELE);
                m.setNoGravity(true);
            }).snapTo(b + 21.5, Showcase.ground + 1.4, OZ - 3.0, 345, 0);
            // Rauchquallen hoch über dem Delta
            Rauchqualle.Look[] looks = Rauchqualle.Look.values();
            for (int i = 0; i < looks.length; i++) {
                Rauchqualle.Look look = looks[i];
                double x = b + 2.0 + i * 9.0;
                double y = Showcase.ground + 9.0 + (i == 1 ? 2.0 : 0.0);
                spawn(level, ModEntities.GHAST.get(), x, OZ - 16.0, i == 1 ? 0 : (i == 0 ? 20 : 340), m -> {
                    m.setLook(look);
                    m.setNoGravity(true);
                    m.setCharging(look == Rauchqualle.Look.GLUT);
                }).snapTo(x, y, OZ - 16.0, i == 1 ? 0 : (i == 0 ? 20 : 340), 0);
            }
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));
        shot(steps, "nether_ghasts", group(5) + 11.0, 9.0, 10.5, 18.0, true);
        // erst jetzt (die Chunks sind geladen): die Glutqualle reißt die Augen auf – das Laden wird nicht gespeichert
        steps.add(Step.server(level -> {
            for (Rauchqualle qualle : level.getEntitiesOfClass(Rauchqualle.class, new net.minecraft.world.phys.AABB(
                    new BlockPos(group(5), Showcase.ground, OZ)).inflate(48.0))) {
                qualle.setCharging(qualle.getLook() == Rauchqualle.Look.GLUT);
            }
        }));
        steps.add(Step.waitTicks(10));
        shot(steps, "nether_ghast_fire", group(5) + 11.0, 12.5, 13.0, 9.0, false);
        shot(steps, "nether_spirits", group(5) + 10.0, 3.2, 1.2, 10.5, false);
        shot(steps, "nether_blaze_close", group(5) + 4.5, 2.3, 2.1, 1.8, false);
        shot(steps, "nether_magma_close", group(5) + 8.0, 1.8, 0.6, 6.0, false);
        steps.add(Step.command("time set 6000"));
        shot(steps, "nether_ghasts_day", group(5) + 11.0, 9.0, 10.5, 18.0, false);
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
