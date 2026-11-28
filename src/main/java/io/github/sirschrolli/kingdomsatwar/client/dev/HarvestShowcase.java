package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.Cuisine;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;

/**
 * Die Feldfrüchte der Welten (2.0) in der Vorführung: ein Garten mit jeder Pflanze in ihren Stufen auf ihrem Grund – der Reis
 * im seichten Wasser, die Meertrauben in einem Becken, die Schattenknollen auf Schattenacker, die Glutschoten auf Seelenerde,
 * die vier Sträucher auf Endstein, Leerenmoos, Chaosstein und Feengras –, bei Tag und bei Nacht; dazu alle Speisen in
 * Rahmen an einer Wand und einige in der Hand (aus eigener Sicht und von vorn).
 */
final class HarvestShowcase {
    private static final int OZ = 560;

    private HarvestShowcase() {}

    static void harvest(Deque<Step> steps) {
        steps.add(Step.server(HarvestShowcase::build));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Showcase.tp(5.5, 6.0, OZ + 13.0, 180, 35));
        steps.add(Step.waitForChunks(300));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("harvest_garden"));
        steps.add(Showcase.tp(3.5, 1.6, OZ + 5.2, 180, 24));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("harvest_rice"));
        steps.add(Showcase.tp(3.5, 1.8, OZ + 9.2, 180, 24));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("harvest_tubers_peppers"));
        steps.add(Showcase.tp(4.0, 2.6, OZ + 1.5, 180, 24));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("harvest_bushes"));
        // die Meertrauben unter Wasser, durch das Glas
        steps.add(Showcase.tp(14.5, -0.3, OZ + 5.2, 180, 8));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("harvest_sea_grapes"));
        // bei Nacht: was reif ist, leuchtet
        steps.add(Step.command("time set 18000"));
        steps.add(Showcase.tp(5.5, 5.0, OZ + 12.0, 180, 30));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("harvest_night"));
        steps.add(Showcase.tp(4.0, 2.6, OZ + 1.5, 180, 24));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("harvest_bushes_night"));
        steps.add(Step.command("time set 6000"));
        // die Speisen in Rahmen an der Wand
        steps.add(Showcase.tp(-11.0, 1.4, OZ + 4.6, 180, 4));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("harvest_foods_wall"));
        // in der Hand: aus eigener Sicht und von vorn
        steps.add(Step.command("gamemode creative"));
        for (var food : List.of(Cuisine.STARFRUIT, Cuisine.CHAOS_BEAN_STEW, Cuisine.SUSHI, Cuisine.FAE_PANCAKES)) {
            String name = food.getId().getPath();
            steps.add(Step.server(level -> {
                ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
                player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(food.get()));
                player.setItemInHand(InteractionHand.OFF_HAND, ItemStack.EMPTY);
            }));
            steps.add(Showcase.tp(6.5, 0.0, OZ + 14.5, 180, 15));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
            steps.add(Step.hud(true));
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot("harvest_hold_" + name + "_ich"));
            steps.add(Step.hud(false));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot("harvest_hold_" + name + "_vorn"));
            steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        }
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(true));
    }

    private static void build(ServerLevel level) {
        int g = Showcase.ground;
        Showcase.fill(level, new BlockPos(-16, g - 2, OZ - 12), new BlockPos(24, g - 1, OZ + 18), Blocks.GRASS_BLOCK.defaultBlockState());
        Showcase.fill(level, new BlockPos(-16, g, OZ - 12), new BlockPos(24, g + 8, OZ + 18), Blocks.AIR.defaultBlockState());
        int[] ages8 = {0, 3, 5, 7};
        // Reis: im seichten Wasser über Erde, eingefasst
        for (int x = -1; x <= 8; x++) {
            for (int z = OZ + 1; z <= OZ + 3; z++) {
                boolean rim = x == -1 || x == 8 || z == OZ + 1 || z == OZ + 3;
                level.setBlock(new BlockPos(x, g - 2, z), Blocks.DIRT.defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(new BlockPos(x, g - 1, z), rim ? Blocks.MUD_BRICKS.defaultBlockState() : Blocks.WATER.defaultBlockState(),
                        Block.UPDATE_CLIENTS);
                if (!rim) {
                    level.setBlock(new BlockPos(x, g - 2, z), Blocks.MUD.defaultBlockState(), Block.UPDATE_CLIENTS);
                    CropBlock rice = Cuisine.RICE_PLANT.get();
                    level.setBlock(new BlockPos(x, g - 1, z), rice.getStateForAge(ages8[x / 2]), Block.UPDATE_CLIENTS);
                }
            }
        }
        // Schattenknollen auf Schattenacker
        for (int x = 0; x < 8; x++) {
            level.setBlock(new BlockPos(x, g - 1, OZ + 6), ShadowBlocks.SHADOW_FARMLAND.get().defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(x, g, OZ + 6), Cuisine.SHADOW_TUBERS.get().getStateForAge(ages8[x / 2]), Block.UPDATE_CLIENTS);
        }
        // Glutschoten auf Seelenerde
        for (int x = 9; x < 13; x++) {
            level.setBlock(new BlockPos(x, g - 1, OZ + 6), Blocks.SOUL_SOIL.defaultBlockState(), Block.UPDATE_CLIENTS);
            level.setBlock(new BlockPos(x, g, OZ + 6), Cuisine.EMBER_PEPPERS.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.NetherWartBlock.AGE, x - 9), Block.UPDATE_CLIENTS);
        }
        // Meertrauben: ein Becken aus Glas, zwei Blöcke Wasser über Sand
        for (int x = 12; x <= 17; x++) {
            for (int z = OZ + 1; z <= OZ + 3; z++) {
                boolean rim = x == 12 || x == 17 || z == OZ + 1 || z == OZ + 3;
                level.setBlock(new BlockPos(x, g - 2, z), rim ? Blocks.GLASS.defaultBlockState() : Blocks.SAND.defaultBlockState(), Block.UPDATE_CLIENTS);
                for (int y = g - 1; y <= g + 1; y++) {
                    level.setBlock(new BlockPos(x, y, z), rim ? Blocks.GLASS.defaultBlockState() : Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
        }
        for (int x = 13; x <= 16; x++) {
            level.setBlock(new BlockPos(x, g - 1, OZ + 2), Cuisine.SEA_GRAPE_PLANT.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.crop.SeaGrapeBlock.AGE, x - 13), Block.UPDATE_CLIENTS);
        }
        // die vier Sträucher, je in vier Stufen, auf ihrem Grund
        List<Block> soils = List.of(Blocks.END_STONE, VoidBlocks.VOID_MOSS.get(), AbyssBlocks.CHAOS_STONE.base().get(), FaeBlocks.FAE_GRASS.get());
        for (int row = 0; row < 4; row++) {
            var bush = Cuisine.bushes().get(row).get();
            for (int i = 0; i < 4; i++) {
                int x = row * 5 - 5 + i;
                BlockPos soil = new BlockPos(x, g - 1, OZ - 4);
                level.setBlock(soil, soils.get(row).defaultBlockState(), Block.UPDATE_CLIENTS);
                level.setBlock(soil.above(), bush.defaultBlockState().setValue(io.github.sirschrolli.kingdomsatwar.block.crop.BerryBushBlock.AGE, i),
                        Block.UPDATE_CLIENTS);
            }
        }
        // die Wand mit den Speisen in Rahmen (zwei Reihen)
        for (int x = -15; x <= -8; x++) {
            for (int y = g; y <= g + 3; y++) {
                level.setBlock(new BlockPos(x, y, OZ), Blocks.SPRUCE_PLANKS.defaultBlockState(), Block.UPDATE_CLIENTS);
            }
        }
        List<ItemStack> foods = List.of(new ItemStack(Cuisine.RICE.get()), new ItemStack(Cuisine.RICE_PUDDING.get()), new ItemStack(Cuisine.SUSHI.get()),
                new ItemStack(Cuisine.SEA_GRAPES.get()), new ItemStack(Cuisine.SEA_GRAPE_SALAD.get()), new ItemStack(Cuisine.FLOAT_BERRIES.get()),
                new ItemStack(Cuisine.FLOAT_BERRY_TART.get()), new ItemStack(Cuisine.SHADOW_TUBER.get()), new ItemStack(Cuisine.ROASTED_SHADOW_TUBER.get()),
                new ItemStack(Cuisine.SHADOW_TUBER_STEW.get()), new ItemStack(Cuisine.STARFRUIT.get()), new ItemStack(Cuisine.STARFRUIT_JUICE.get()),
                new ItemStack(Cuisine.CHAOS_BEANS.get()), new ItemStack(Cuisine.CHAOS_BEAN_STEW.get()), new ItemStack(Cuisine.FAE_BERRIES.get()),
                new ItemStack(Cuisine.FAE_PANCAKES.get()));
        for (int i = 0; i < foods.size(); i++) {
            BlockPos at = new BlockPos(-15 + i % 8, g + 2 - i / 8, OZ + 1);
            ItemFrame frame = new ItemFrame(level, at, Direction.SOUTH);
            frame.setItem(foods.get(i), false);
            level.addFreshEntity(frame);
        }
    }
}
