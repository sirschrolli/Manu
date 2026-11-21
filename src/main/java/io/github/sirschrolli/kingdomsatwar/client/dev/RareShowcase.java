package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.rare.CloudJelly;
import io.github.sirschrolli.kingdomsatwar.entity.creature.rare.PrimevalDigger;
import io.github.sirschrolli.kingdomsatwar.entity.villager.TravelingMerchant;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung der seltenen Wesen und des fahrenden Händlers (E4d-2): die drei Urwühler mit ihren Jungen, die drei
 * Wolkenquallen (eine mit Geschirr) samt Wölkchen, eine Wolkenqualle von unten, und die fahrenden Händler mit ihren
 * Lamas – von vorn und von hinten (Kiepe).
 */
final class RareShowcase {
    private static final int OZ = 1200;

    private RareShowcase() {}

    private static int group(int index) {
        return -200 + index * 50;
    }

    private static <T extends Mob> T spawn(ServerLevel level, EntityType<T> type, double x, double z, double height, float yaw, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("kein " + type.getDescriptionId());
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.setNoGravity(height > 0.0);
        mob.snapTo(x, Showcase.ground + height, z, yaw, 0.0f);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        setup.accept(mob);
        level.addFreshEntity(mob);
        return mob;
    }

    private static void ground(ServerLevel level, int x0, BlockState floor, int height) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 14, g - 1, OZ - 20), new BlockPos(x0 + 34, g - 1, OZ + 24), floor);
        Showcase.fill(level, new BlockPos(x0 - 14, g, OZ - 20), new BlockPos(x0 + 34, g + height, OZ + 24), Blocks.AIR.defaultBlockState());
    }

    static void rare(Deque<Step> steps) {
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

            // Urwühler: die drei Gestalten in Dreiviertelansicht, davor ihre Jungen; hinten Farne und Moos
            int b = group(0);
            ground(level, b, grass, 8);
            Showcase.fill(level, new BlockPos(b - 4, g - 1, OZ - 5), new BlockPos(b + 20, g - 1, OZ - 3), Blocks.MOSS_BLOCK.defaultBlockState());
            for (int x = b - 4; x <= b + 20; x++) {
                level.setBlock(new BlockPos(x, g, OZ - 5), (x % 3 == 0 ? Blocks.LARGE_FERN : Blocks.FERN).defaultBlockState(), 2);
            }
            PrimevalDigger.Look[] diggers = PrimevalDigger.Look.values();
            for (int i = 0; i < diggers.length; i++) {
                PrimevalDigger.Look look = diggers[i];
                spawn(level, ModEntities.SNIFFER.get(), b + 2.5 + i * 7.0, OZ - 0.5, 0.0, i == 1 ? 20 : i == 0 ? 330 : 40, m -> m.setLook(look));
                spawn(level, ModEntities.SNIFFER.get(), b + 5.6 + i * 7.0, OZ + 0.8, 0.0, i % 2 == 0 ? 15 : 345, m -> {
                    m.setLook(look);
                    m.setAge(-24000);
                });
            }

            // Wolkenquallen: drei Gestalten (die Schönwetterwolke mit rotem Geschirr), davor drei Wölkchen
            b = group(1);
            ground(level, b, grass, 16);
            for (int x = b - 6; x <= b + 26; x += 2) {
                level.setBlock(new BlockPos(x, g, OZ + 6), (x % 4 == 0 ? Blocks.POPPY : Blocks.OXEYE_DAISY).defaultBlockState(), 2);
            }
            CloudJelly.Look[] clouds = CloudJelly.Look.values();
            for (int i = 0; i < clouds.length; i++) {
                CloudJelly.Look look = clouds[i];
                int index = i;
                spawn(level, ModEntities.HAPPY_GHAST.get(), b + 1.5 + i * 7.0, OZ - 2.0, 1.2 + (i % 2) * 0.8, i % 2 == 0 ? 15 : 345, m -> {
                    m.setLook(look);
                    if (index == 0) {
                        m.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.HARNESS.pick(DyeColor.RED)));
                    }
                });
                spawn(level, ModEntities.HAPPY_GHAST.get(), b + 2.5 + i * 7.0, OZ + 3.5, 0.6 + (i % 2) * 0.4, 0, m -> {
                    m.setLook(look);
                    m.setAge(-24000);
                });
            }
            // eine Wolkenqualle hoch über der Wiese (für den Blick von unten)
            spawn(level, ModEntities.HAPPY_GHAST.get(), b + 30.5, OZ - 1.5, 7.0, 0, m -> m.setLook(CloudJelly.Look.REGENWOLKE));

            // fahrende Händler mit ihren Lamas; einer von hinten (Kiepe)
            b = group(2);
            ground(level, b, Blocks.DIRT_PATH.defaultBlockState(), 8);
            Showcase.fill(level, new BlockPos(b - 14, g - 1, OZ - 20), new BlockPos(b + 34, g - 1, OZ - 3), grass);
            Showcase.fill(level, new BlockPos(b - 14, g - 1, OZ + 4), new BlockPos(b + 34, g - 1, OZ + 24), grass);
            String[] names = {"Jorin", "Mattes", "Ilka"};
            for (int i = 0; i < TravelingMerchant.VARIANTS; i++) {
                int variant = i;
                TravelingMerchant merchant = spawn(level, ModEntities.WANDERING_TRADER.get(), b + 2.5 + i * 4.0, OZ + 0.5, 0.0,
                        i == 1 ? 0 : i == 0 ? 340 : 20, m -> m.setup(variant, names[variant], 11L * (variant + 1)));
                spawn(level, ModEntities.TRADER_LLAMA.get(), b + 3.6 + i * 4.0, OZ - 2.0, 0.0, 200, llama -> llama.setLeashedTo(merchant, true));
            }
            spawn(level, ModEntities.WANDERING_TRADER.get(), b + 17.5, OZ + 0.5, 0.0, 180, m -> m.setup(0, "Jorin", 5L));
            spawn(level, ModEntities.WANDERING_TRADER.get(), b + 19.5, OZ + 0.5, 0.0, 150, m -> m.setup(2, "Ilka", 6L));
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(50)));
        shot(steps, "rare_diggers", group(0) + 11.0, 3.2, 1.0, 15.0, true);
        shot(steps, "rare_digger_close", group(0) + 2.4, 2.0, 1.1, 5.4, false);
        shot(steps, "rare_clouds", group(1) + 9.5, 4.6, 3.0, 22.0, true);
        shot(steps, "rare_cloud_close", group(1) + 1.6, 3.4, 3.2, 8.5, false);
        shot(steps, "rare_cloud_below", group(1) + 30.5, 1.4, 8.6, 6.0, false);
        shot(steps, "rare_cloudlets", group(1) + 9.5, 1.9, 1.5, 9.0, false);
        shot(steps, "rare_traders", group(2) + 6.5, 2.0, 1.2, 8.2, true);
        shot(steps, "rare_trader_back", group(2) + 18.5, 2.0, 1.2, 4.6, false);
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
