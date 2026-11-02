package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.HouseSprite;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.IronSentinel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.golem.Snowman;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.golem.CopperGolem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Weltvorführung der eigenen Golems und Wichtel (E4d): die vier Eisenwächter (auch bei Nacht, wenn Augen und
 * Schmiedefeuer glühen und einer einem Dorfbewohner Mohn reicht), Schneemänner mit und ohne Kürbis, Kupferknechte in
 * allen Stufen der Verwitterung an ihren Truhen, schwebende Wichtel und die Statuen des Kupferknechts.
 */
final class GolemShowcase {
    private static final int OZ = 1000;

    private GolemShowcase() {}

    private static int group(int index) {
        return -200 + index * 40;
    }

    private static <T extends Mob> void spawn(ServerLevel level, EntityType<T> type, double x, double z, double height, float yaw, Consumer<T> setup) {
        Showcase.spawnCreature(level, type, (int) Math.floor(x), (int) Math.floor(z), yaw, mob -> {
            mob.setNoGravity(height > 0.0);
            mob.setPos(x, Showcase.ground + height, z);
            setup.accept(mob);
        });
    }

    private static void ground(ServerLevel level, int x0, BlockState floor) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(x0 - 10, g - 1, OZ - 14), new BlockPos(x0 + 26, g - 1, OZ + 16), floor);
        Showcase.fill(level, new BlockPos(x0 - 10, g, OZ - 14), new BlockPos(x0 + 26, g + 10, OZ + 16), Blocks.AIR.defaultBlockState());
    }

    static void golems(Deque<Step> steps) {
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();

            // Eisenwächter in allen vier Gestalten, dahinter eine Mauer; daneben einer, der einem Dorfbewohner Mohn reicht
            int b = group(0);
            ground(level, b, grass);
            Showcase.fill(level, new BlockPos(b - 2, g, OZ - 4), new BlockPos(b + 18, g + 4, OZ - 4), Blocks.STONE_BRICKS.defaultBlockState());
            Showcase.fill(level, new BlockPos(b - 2, g + 5, OZ - 4), new BlockPos(b + 18, g + 5, OZ - 4), Blocks.STONE_BRICK_WALL.defaultBlockState());
            IronSentinel.Look[] looks = IronSentinel.Look.values();
            for (int i = 0; i < looks.length; i++) {
                IronSentinel.Look look = looks[i];
                spawn(level, ModEntities.IRON_GOLEM.get(), b + 2.5 + i * 3.6, OZ - 0.5, 0.0, i % 2 == 0 ? 10 : 350, m -> m.setLook(look));
            }
            spawn(level, ModEntities.IRON_GOLEM.get(), b + 18.6, OZ + 0.5, 0.0, 270, m -> m.setLook(IronSentinel.Look.SCHMIEDEEISEN));
            BlockPos home = new BlockPos(b + 21, g, OZ);
            spawn(level, ModEntities.VILLAGER.get(), b + 21.0, OZ + 0.5, 0.0, 90, m -> m.setup(VillageProfession.FARMER, 0, "Hanna",
                    home, "Vorführdorf", null, null, 7L));

            // Schneemänner: zwei mit Kürbis, zwei geschoren (Kohlegesicht, Möhrennase, Strickmütze)
            b = group(1);
            ground(level, b, Blocks.SNOW_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(b - 2, g, OZ - 4), new BlockPos(b + 14, g, OZ - 4), Blocks.SPRUCE_LEAVES.defaultBlockState());
            Snowman.Look[] scarves = Snowman.Look.values();
            for (int i = 0; i < scarves.length; i++) {
                Snowman.Look look = scarves[i];
                boolean pumpkin = i < 2;
                spawn(level, ModEntities.SNOW_GOLEM.get(), b + 2.5 + i * 2.8, OZ - 0.5, 0.0, i % 2 == 0 ? 15 : 345, m -> {
                    m.setLook(look);
                    m.setPumpkin(pumpkin);
                    m.setPermanentlyInvulnerable(true);            // in der warmen Vorführwelt schmölzen sie sonst
                });
            }

            // Kupferknechte in allen vier Stufen an Kupfertruhe und Truhen; einer trägt etwas, einer hat eine Blume auf dem Kopf
            b = group(2);
            ground(level, b, Blocks.SPRUCE_PLANKS.defaultBlockState());
            Showcase.fill(level, new BlockPos(b - 2, g, OZ - 3), new BlockPos(b + 14, g + 3, OZ - 3), Blocks.SPRUCE_PLANKS.defaultBlockState());
            level.setBlock(new BlockPos(b + 1, g, OZ - 2), Blocks.COPPER_CHEST.weathering().unaffected().defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(b + 10, g, OZ - 2), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
            level.setBlock(new BlockPos(b + 11, g, OZ - 2), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH), 2);
            WeatheringCopper.WeatherState[] stages = WeatheringCopper.WeatherState.values();
            for (int i = 0; i < stages.length; i++) {
                WeatheringCopper.WeatherState stage = stages[i];
                int index = i;
                // ein Teppich unter den Füßen: ganz oxidiert erstarrt ein Kupferknecht nur auf freiem Feld zur Statue
                level.setBlock(new BlockPos((int) Math.floor(b + 2.5 + i * 2.4), g, OZ - 1), Blocks.CARPET.red().defaultBlockState(), 2);
                spawn(level, ModEntities.COPPER_GOLEM.get(), b + 2.5 + i * 2.4, OZ - 0.5, 0.0, i % 2 == 0 ? 20 : 340, m -> {
                    m.setWeatherState(stage);
                    if (index == 1) {
                        m.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.IRON_INGOT, 8));
                    }
                    if (index == 2) {
                        m.setItemSlot(CopperGolem.EQUIPMENT_SLOT_ANTENNA, new ItemStack(Items.POPPY));
                    }
                });
            }

            // Wichtel schweben vor Hecke und Blumen; einer trägt einen Edelstein
            b = group(3);
            ground(level, b, grass);
            Showcase.fill(level, new BlockPos(b - 2, g, OZ - 4), new BlockPos(b + 14, g + 1, OZ - 4), Blocks.OAK_LEAVES.defaultBlockState());
            for (int x = b; x <= b + 12; x += 2) {
                level.setBlock(new BlockPos(x, g, OZ - 3), (x / 2 % 2 == 0 ? Blocks.CORNFLOWER : Blocks.OXEYE_DAISY).defaultBlockState(), 2);
            }
            HouseSprite.Look[] sprites = HouseSprite.Look.values();
            for (int i = 0; i < sprites.length; i++) {
                HouseSprite.Look look = sprites[i];
                int index = i;
                spawn(level, ModEntities.ALLAY.get(), b + 2.5 + i * 2.2, OZ - 0.5, 1.0 + (i % 2) * 0.35, i % 2 == 0 ? 20 : 340, m -> {
                    m.setLook(look);
                    if (index == 3) {
                        m.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.AMETHYST_SHARD));
                    }
                });
            }

            // Statuen des Kupferknechts: vier Haltungen in vier Stufen der Verwitterung
            b = group(4);
            ground(level, b, Blocks.POLISHED_ANDESITE.defaultBlockState());
            CopperGolemStatueBlock.Pose[] poses = CopperGolemStatueBlock.Pose.values();
            for (int i = 0; i < poses.length; i++) {
                BlockState statue = Blocks.COPPER_GOLEM_STATUE.weathering().pick(stages[i]).defaultBlockState()
                        .setValue(CopperGolemStatueBlock.POSE, poses[i]).setValue(CopperGolemStatueBlock.FACING, Direction.SOUTH);
                level.setBlock(new BlockPos(b + 2 + i * 2, g, OZ - 1), statue, 2);
            }
        }));
        steps.add(Step.waitTicks(120));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(50)));
        shot(steps, "golems_iron", group(0) + 7.9, 2.0, 1.5, 11.0, true);
        shot(steps, "golems_iron_close", group(0) + 2.5, 2.3, 1.9, 3.6, false);
        // erst jetzt, da die Kamera nah genug ist, erfahren die Zuschauer davon: der Eisenwächter reicht Hanna eine Mohnblume
        steps.add(Step.server(level -> level.getEntitiesOfClass(IronSentinel.class,
                new net.minecraft.world.phys.AABB(group(0) + 17, Showcase.ground, OZ - 2, group(0) + 20, Showcase.ground + 3, OZ + 3))
                .forEach(golem -> golem.offerFlower(true))));
        shot(steps, "golems_gift", group(0) + 19.8, 1.9, 1.3, 4.6, false);
        shot(steps, "golems_snow", group(1) + 6.7, 1.6, 0.9, 7.0, true);
        shot(steps, "golems_copper", group(2) + 6.1, 1.2, 0.5, 5.6, true);
        shot(steps, "golems_sprites", group(3) + 5.8, 1.6, 1.3, 4.6, true);
        shot(steps, "golems_statues", group(4) + 5.5, 1.5, 0.6, 6.2, true);
        steps.add(Step.command("time set 18000"));
        steps.add(Step.waitTicks(20));
        shot(steps, "golems_iron_night", group(0) + 7.9, 2.0, 1.5, 11.0, false);
        shot(steps, "golems_copper_night", group(2) + 6.1, 1.2, 0.5, 5.6, false);
        steps.add(Step.command("time set 6000"));
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
