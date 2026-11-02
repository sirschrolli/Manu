package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Borstling;
import io.github.sirschrolli.kingdomsatwar.entity.creature.nether.RottenBristling;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.AbstractPiglin;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung des Nether-Volks (E5a): Borstlinge in drei Gestalten (mit Armbrust, Goldschwert, einer bewundert einen
 * Goldbarren), der Borstenhüne mit Goldaxt, zwei Moderborstlinge, die Kinder und ein tanzender Borstling – vor einer
 * Mauer aus Netherziegeln mit Karmesinstämmen und Pilzlichtern.
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
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(50)));
        shot(steps, "nether_folk", group(0) + 9.0, 1.9, 1.15, 10.5, true);
        shot(steps, "nether_close", group(0) + 4.5, 2.0, 1.4, 4.8, false);
        shot(steps, "nether_brute", group(0) + 12.0, 2.0, 1.3, 4.8, false);
        shot(steps, "nether_babies", group(1) + 5.8, 1.5, 0.9, 5.2, true);
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
