package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Codfish;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.Salmonid;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaDolphin;
import io.github.sirschrolli.kingdomsatwar.entity.creature.water.SeaTurtle;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.axolotl.Axolotl;
import net.minecraft.world.entity.animal.fish.Salmon;
import net.minecraft.world.entity.animal.fish.TropicalFish;
import net.minecraft.world.entity.animal.frog.FrogVariant;
import net.minecraft.world.entity.animal.frog.FrogVariants;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.KelpBlock;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung der eigenen Wassertiere (E4c): Meeresbecken, in denen die Tiere im Wasser schweben und die Kamera selbst
 * unter Wasser steht – Dorsche und Lachse, Kugelfische und Rifffische, Tintenfische und Tiefseekraken, Delfine,
 * Nautilusse, Molche mit Kaulquappen – dazu Frösche und Meeresschildkröten am Strand. Nachts leuchten Tiefseekrake,
 * Blaumolch und Geisternautilus.
 */
final class WaterShowcase {
    private static final int OZ = 840;
    private static final int WIDTH = 15;
    private static final int HEIGHT = 8;

    private WaterShowcase() {}

    private static int group(int index) {
        return -320 + index * 64;
    }

    private static <T extends Mob> void spawn(ServerLevel level, EntityType<T> type, double x, double z, double height, float yaw,
            Consumer<T> setup) {
        Showcase.spawnCreature(level, type, (int) Math.floor(x), (int) Math.floor(z), yaw, mob -> {
            mob.setNoGravity(height > 0.0);
            mob.setPos(x, Showcase.ground + height, z);
            setup.accept(mob);
        });
    }

    /**
     * Ein Meeresbecken, in dem die Kamera selbst unter Wasser steht: Boden, Rückwand mit Seegras und Tang, ringsum Wände,
     * Wasser bis zur Oberkante. Unter Wasser sieht man die Tiere klar und ohne die Wasserfläche dazwischen.
     */
    private static void tank(ServerLevel level, int x0, BlockState back, BlockState floor) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 7, g - 1, OZ - 5), new BlockPos(x0 + WIDTH + 6, g + HEIGHT, OZ + 13), back);
        Showcase.fill(level, new BlockPos(x0 - 7, g - 1, OZ - 5), new BlockPos(x0 + WIDTH + 6, g - 1, OZ + 13), floor);
        Showcase.fill(level, new BlockPos(x0 - 6, g, OZ - 4), new BlockPos(x0 + WIDTH + 5, g + HEIGHT - 1, OZ + 12), Blocks.WATER.defaultBlockState());
        Showcase.fill(level, new BlockPos(x0 - 6, g + HEIGHT, OZ - 4), new BlockPos(x0 + WIDTH + 5, g + HEIGHT, OZ + 12), Blocks.AIR.defaultBlockState());
        for (int dx = -4; dx <= WIDTH + 3; dx++) {
            int h = Math.floorMod(dx * 7 + 3, 5);
            if (h == 0 && dx % 2 == 0) {
                int height = 2 + Math.floorMod(dx, 3);
                for (int dy = 0; dy < height; dy++) {
                    BlockState kelp = dy == height - 1 ? Blocks.KELP.defaultBlockState().setValue(KelpBlock.AGE, 25)
                            : Blocks.KELP_PLANT.defaultBlockState();
                    level.setBlock(new BlockPos(x0 + dx, g + dy, OZ - 4), kelp, 2);
                }
            } else if (h < 3) {
                level.setBlock(new BlockPos(x0 + dx, g, OZ - 4), Blocks.SEAGRASS.defaultBlockState(), 2);
            }
        }
        for (int dx : new int[] {-3, WIDTH + 2}) {
            level.setBlock(new BlockPos(x0 + dx, g, OZ + 3), Blocks.SEAGRASS.defaultBlockState(), 2);
        }
    }

    private static void reef(Mob mob, TropicalFish.Pattern pattern, DyeColor base, DyeColor patternColor) {
        mob.setComponent(DataComponents.TROPICAL_FISH_PATTERN, pattern);
        mob.setComponent(DataComponents.TROPICAL_FISH_BASE_COLOR, base);
        mob.setComponent(DataComponents.TROPICAL_FISH_PATTERN_COLOR, patternColor);
    }

    private static void frog(ServerLevel level, Mob mob, ResourceKey<FrogVariant> variant) {
        mob.setComponent(DataComponents.FROG_VARIANT, level.registryAccess().lookupOrThrow(Registries.FROG_VARIANT).getOrThrow(variant));
    }

    static void water(Deque<Step> steps) {
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamemode spectator"));                  // unter Wasser: klare Sicht nach wenigen Ticks
        steps.add(Step.command("time set 6000"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int i = 0; i < 7; i++) {
                int base = group(i);
                Showcase.fill(level, new BlockPos(base - 12, g - 1, OZ - 14), new BlockPos(base + 28, g - 1, OZ + 16),
                        Blocks.SAND.defaultBlockState());
                Showcase.fill(level, new BlockPos(base - 12, g, OZ - 14), new BlockPos(base + 28, g + 12, OZ + 16), Blocks.AIR.defaultBlockState());
            }
            BlockState sand = Blocks.SAND.defaultBlockState();

            // Dorsche und Lachse (Lachse in allen drei Größen)
            int b = group(0);
            tank(level, b, Blocks.PRISMARINE_BRICKS.defaultBlockState(), sand);
            spawn(level, ModEntities.COD.get(), b + 3.0, OZ - 0.5, 3.6, 75, m -> m.setLook(Codfish.Look.DORSCH));
            spawn(level, ModEntities.COD.get(), b + 3.8, OZ - 1.5, 1.6, 105, m -> m.setLook(Codfish.Look.SEELACHS));
            spawn(level, ModEntities.COD.get(), b + 5.4, OZ - 0.5, 2.6, 285, m -> m.setLook(Codfish.Look.DORSCH));
            spawn(level, ModEntities.SALMON.get(), b + 8.0, OZ - 0.5, 4.0, 70, m -> {
                m.setLook(Salmonid.Look.LACHS);
                m.setComponent(DataComponents.SALMON_SIZE, Salmon.Variant.LARGE);
            });
            spawn(level, ModEntities.SALMON.get(), b + 8.5, OZ - 1.0, 1.6, 100, m -> {
                m.setLook(Salmonid.Look.LACHS);
                m.setComponent(DataComponents.SALMON_SIZE, Salmon.Variant.SMALL);
            });
            spawn(level, ModEntities.SALMON.get(), b + 11.2, OZ - 0.5, 2.8, 290, m -> {
                m.setLook(Salmonid.Look.ROTLACHS);
                m.setComponent(DataComponents.SALMON_SIZE, Salmon.Variant.LARGE);
            });
            spawn(level, ModEntities.SALMON.get(), b + 11.8, OZ - 1.5, 1.4, 80, m -> {
                m.setLook(Salmonid.Look.ROTLACHS);
                m.setComponent(DataComponents.SALMON_SIZE, Salmon.Variant.MEDIUM);
            });

            // Kugelfische (schlank, halb und ganz aufgebläht) und Rifffische in sechs Mustern
            b = group(1);
            tank(level, b, Blocks.PRISMARINE.defaultBlockState(), sand);
            for (int i = 0; i < 3; i++) {
                int puff = i;
                spawn(level, ModEntities.PUFFERFISH.get(), b + 2.8 + i * 1.9, OZ - 0.5, 1.5 + i * 0.9, 60, m -> m.setPuffState(puff));
            }
            spawn(level, ModEntities.TROPICAL_FISH.get(), b + 8.4, OZ - 0.5, 3.8, 70,
                    m -> reef(m, TropicalFish.Pattern.KOB, DyeColor.ORANGE, DyeColor.WHITE));
            spawn(level, ModEntities.TROPICAL_FISH.get(), b + 10.0, OZ - 1.0, 4.3, 290,
                    m -> reef(m, TropicalFish.Pattern.STRIPEY, DyeColor.YELLOW, DyeColor.GRAY));
            spawn(level, ModEntities.TROPICAL_FISH.get(), b + 11.8, OZ - 0.5, 3.6, 80,
                    m -> reef(m, TropicalFish.Pattern.BETTY, DyeColor.RED, DyeColor.WHITE));
            spawn(level, ModEntities.TROPICAL_FISH.get(), b + 8.8, OZ - 1.0, 1.8, 285,
                    m -> reef(m, TropicalFish.Pattern.GLITTER, DyeColor.LIGHT_BLUE, DyeColor.WHITE));
            spawn(level, ModEntities.TROPICAL_FISH.get(), b + 10.6, OZ - 0.5, 2.5, 75,
                    m -> reef(m, TropicalFish.Pattern.DASHER, DyeColor.CYAN, DyeColor.PINK));
            spawn(level, ModEntities.TROPICAL_FISH.get(), b + 12.2, OZ - 1.0, 1.4, 280,
                    m -> reef(m, TropicalFish.Pattern.CLAYFISH, DyeColor.WHITE, DyeColor.ORANGE));

            // Tintenfische und Tiefseekraken, je mit Jungtier
            b = group(2);
            tank(level, b, Blocks.DARK_PRISMARINE.defaultBlockState(), Blocks.GRAVEL.defaultBlockState());
            spawn(level, ModEntities.SQUID.get(), b + 3.0, OZ - 1.0, 1.4, 20, m -> { });
            spawn(level, ModEntities.SQUID.get(), b + 6.0, OZ - 0.5, 3.0, 340, m -> m.setAge(-24000));
            spawn(level, ModEntities.GLOW_SQUID.get(), b + 9.0, OZ - 0.5, 3.0, 20, m -> m.setAge(-24000));
            spawn(level, ModEntities.GLOW_SQUID.get(), b + 12.0, OZ - 1.0, 1.4, 340, m -> { });

            // Großer Tümmler (einer mit Kabeljau im Schnabel), Schwertwal und ein Jungtier
            b = group(3);
            tank(level, b, Blocks.PRISMARINE_BRICKS.defaultBlockState(), sand);
            spawn(level, ModEntities.DOLPHIN.get(), b + 3.5, OZ - 0.5, 3.4, 60, m -> m.setLook(SeaDolphin.Look.TUEMMLER));
            spawn(level, ModEntities.DOLPHIN.get(), b + 7.5, OZ - 1.0, 1.4, 300, m -> {
                m.setLook(SeaDolphin.Look.TUEMMLER);
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.COD));
            });
            spawn(level, ModEntities.DOLPHIN.get(), b + 11.5, OZ - 0.5, 3.2, 290, m -> m.setLook(SeaDolphin.Look.SCHWERTWAL));
            spawn(level, ModEntities.DOLPHIN.get(), b + 12.0, OZ - 1.0, 1.0, 70, m -> {
                m.setLook(SeaDolphin.Look.SCHWERTWAL);
                m.setAge(-24000);
            });

            // Nautilusse: frei, gesattelt mit Kupferrüstung; Geisternautilusse frei und gesattelt
            b = group(4);
            tank(level, b, Blocks.DARK_PRISMARINE.defaultBlockState(), sand);
            spawn(level, ModEntities.NAUTILUS.get(), b + 3.0, OZ - 0.5, 1.2, 40, m -> { });
            spawn(level, ModEntities.NAUTILUS.get(), b + 6.0, OZ - 1.0, 2.8, 320, m -> {
                m.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                m.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.COPPER_NAUTILUS_ARMOR));
            });
            spawn(level, ModEntities.ZOMBIE_NAUTILUS.get(), b + 9.2, OZ - 0.5, 2.8, 40, m -> { });
            spawn(level, ModEntities.ZOMBIE_NAUTILUS.get(), b + 12.2, OZ - 1.0, 1.2, 320,
                    m -> m.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE)));

            // Molche in allen fünf Arten, ein Jungtier, dazu Kaulquappen
            b = group(5);
            tank(level, b, Blocks.MOSSY_COBBLESTONE.defaultBlockState(), Blocks.CLAY.defaultBlockState());
            Axolotl.Variant[] variants = Axolotl.Variant.values();
            for (int i = 0; i < variants.length; i++) {
                Axolotl.Variant variant = variants[i];
                spawn(level, ModEntities.AXOLOTL.get(), b + 2.5 + i * 2.5, OZ - 0.5, i % 2 == 0 ? 0.0 : 1.5, i % 2 == 0 ? 50 : 310,
                        m -> m.setComponent(DataComponents.AXOLOTL_VARIANT, variant));
            }
            spawn(level, ModEntities.AXOLOTL.get(), b + 7.5, OZ + 0.5, 2.6, 70, m -> {
                m.setComponent(DataComponents.AXOLOTL_VARIANT, Axolotl.Variant.GOLD);
                m.setAge(-24000);
            });
            spawn(level, ModEntities.TADPOLE.get(), b + 4.6, OZ + 0.5, 2.7, 80, m -> { });
            spawn(level, ModEntities.TADPOLE.get(), b + 10.4, OZ + 0.5, 2.9, 290, m -> { });

            // Strand: Frösche (gemäßigt, warm, kalt) und Meeresschildkröten mit Schlüpfling
            b = group(6);
            Showcase.fill(level, new BlockPos(b + 5, g - 1, OZ - 6), new BlockPos(b + 9, g - 1, OZ - 3), Blocks.WATER.defaultBlockState());
            level.setBlock(new BlockPos(b + 6, g, OZ - 5), Blocks.LILY_PAD.defaultBlockState(), 2);
            level.setBlock(new BlockPos(b + 8, g, OZ - 4), Blocks.LILY_PAD.defaultBlockState(), 2);
            spawn(level, ModEntities.FROG.get(), b + 3.0, OZ - 0.5, 0.0, 20, m -> frog(level, m, FrogVariants.TEMPERATE));
            spawn(level, ModEntities.FROG.get(), b + 4.6, OZ - 1.5, 0.0, 345, m -> frog(level, m, FrogVariants.WARM));
            spawn(level, ModEntities.FROG.get(), b + 6.2, OZ - 0.5, 0.0, 15, m -> frog(level, m, FrogVariants.COLD));
            spawn(level, ModEntities.TURTLE.get(), b + 8.8, OZ + 0.5, 0.0, 30, m -> m.setLook(SeaTurtle.Look.SUPPEN));
            spawn(level, ModEntities.TURTLE.get(), b + 11.6, OZ - 0.5, 0.0, 330, m -> m.setLook(SeaTurtle.Look.KARETT));
            spawn(level, ModEntities.TURTLE.get(), b + 13.4, OZ + 1.0, 0.0, 20, m -> {
                m.setLook(SeaTurtle.Look.KARETT);
                m.setAge(-24000);
            });
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(50)));
        shot(steps, "water_fish", group(0) + 7.5, 2.9, 2.6, 9.0, true);
        shot(steps, "water_puffer_reef", group(1) + 7.5, 2.9, 2.7, 9.0, true);
        shot(steps, "water_reef_close", group(1) + 10.4, 3.2, 3.0, 4.6, false);
        shot(steps, "water_squids", group(2) + 7.5, 2.9, 2.4, 9.5, true);
        shot(steps, "water_dolphins", group(3) + 7.5, 2.9, 2.4, 9.5, true);
        shot(steps, "water_nautilus", group(4) + 7.5, 2.7, 2.1, 9.0, true);
        shot(steps, "water_ghost_nautilus", group(4) + 10.7, 2.6, 2.0, 5.0, false);
        shot(steps, "water_newts", group(5) + 7.5, 2.4, 1.3, 8.5, true);
        shot(steps, "water_beach", group(6) + 8.2, 2.4, 0.4, 8.5, true);
        shot(steps, "water_frogs", group(6) + 4.6, 1.3, 0.3, 3.6, false);
        shot(steps, "water_turtles", group(6) + 11.2, 1.8, 0.2, 5.0, false);
        steps.add(Step.command("time set 18000"));
        steps.add(Step.waitTicks(20));
        shot(steps, "water_night_squids", group(2) + 7.5, 2.9, 2.4, 9.5, true);
        shot(steps, "water_night_nautilus", group(4) + 7.5, 2.7, 2.1, 9.0, true);
        shot(steps, "water_night_newts", group(5) + 7.5, 2.4, 1.3, 8.5, true);
        shot(steps, "water_night_blaumolch", group(5) + 12.5, 1.1, 0.35, 3.4, false);
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /**
     * Bild eines Beckens: die Kamera steht {@code distance} Blöcke südlich der Beckenmitte in Augenhöhe {@code eye} und
     * blickt nach Norden auf die Höhe {@code target} (beides über dem Boden).
     */
    private static void shot(Deque<Step> steps, String name, double x, double eye, double target, double distance, boolean first) {
        float pitch = (float) Math.toDegrees(Math.atan2(eye - target, distance));
        steps.add(Showcase.tp(x, eye - 1.62, OZ - 1 + 0.5 + distance, 180, pitch));
        if (first) {
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.waitTicks(80));
            steps.add(Step.waitForChunks(200));
        } else {
            steps.add(Step.waitTicks(30));
        }
        steps.add(Step.screenshot(name));
    }
}
