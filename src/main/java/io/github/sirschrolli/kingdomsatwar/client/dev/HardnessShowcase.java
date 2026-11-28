package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.DeerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.Feature;

/**
 * Weltvorführung zum Härte-Paket (1.3): der Hirsch, der sich wehrt – mit gesenktem Geweih und beim Stoß –, daneben
 * Hirschkuh und Kitz, die fliehen würden. Ein Waldrand mit echten Bäumen, Blumen und Gras.
 */
final class HardnessShowcase {
    private static final int OZ = 1200;
    private static final int X = -200;
    /** Wohin der Hirsch blickt (Grad, Minecraft-Gierwinkel): nach Südosten, auf seinen unsichtbaren Gegner. */
    private static final float STAG_YAW = 300.0f;

    private HardnessShowcase() {}

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

    private static void tree(ServerLevel level, String name, int x, int z, long seed) {
        Feature feature = level.registryAccess().lookupOrThrow(Registries.FEATURE)
                .getOrThrow(ResourceKey.create(Registries.FEATURE, Identifier.withDefaultNamespace(name))).value();
        BlockPos at = new BlockPos(x, Showcase.ground, z);
        for (int attempt = 0; attempt < 6; attempt++) {
            if (feature.place(level, level.getChunkSource().getGenerator(), RandomSource.create(seed + attempt * 97L), at)) {
                return;
            }
        }
    }

    /** Die Kamera steht in Richtung {@code angle} (Grad, vom Blick des Hirschs aus gegen den Uhrzeigersinn) und blickt auf ihn. */
    private static Step around(double distance, double angle, double eye, double lookAt) {
        double facing = Math.toRadians(STAG_YAW + angle);
        double dx = -Math.sin(facing) * distance;
        double dz = Math.cos(facing) * distance;
        float yaw = (float) Math.toDegrees(Math.atan2(dx, -dz));
        float pitch = (float) Math.toDegrees(Math.atan2(eye - lookAt, distance));
        return Showcase.tp(X + 0.5 + dx, eye - 1.62, OZ + 0.5 + dz, yaw, pitch);
    }

    static void deer(Deque<Step> steps) {
        UUID[] stag = new UUID[1];
        steps.add(Step.command("difficulty peaceful"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 4500"));
        steps.add(Step.command("weather clear"));
        // erst hin, damit die Gegend geladen bleibt (sonst werden die Hirsche entladen und vergessen ihr Ziel)
        steps.add(around(4.0, 25.0, 1.1, 1.3));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
            Showcase.fill(level, new BlockPos(X - 30, g - 1, OZ - 34), new BlockPos(X + 30, g - 1, OZ + 30), grass);
            Showcase.fill(level, new BlockPos(X - 30, g, OZ - 34), new BlockPos(X + 30, g + 24, OZ + 30), Blocks.AIR.defaultBlockState());
            // ein Waldrand: Eichen, Birken und große Eichen, unregelmäßig, dahinter dichter
            RandomSource random = RandomSource.create(1303L);
            String[] kinds = {"fancy_oak", "oak", "birch", "oak", "fancy_oak", "birch"};
            int k = 0;
            for (int x = X - 28; x <= X + 28; x += 4 + random.nextInt(3)) {
                int z = OZ - 14 - random.nextInt(6);
                tree(level, kinds[k++ % kinds.length], x, z, 7L * x + z);
                if (random.nextBoolean()) {
                    tree(level, kinds[(k + 2) % kinds.length], x + 2, z - 7 - random.nextInt(4), 13L * x - z);
                }
            }
            // Wiese: Gras, Farne, Blumen – vorn spärlich, zum Wald hin dichter
            Block[] flowers = {Blocks.POPPY, Blocks.CORNFLOWER, Blocks.OXEYE_DAISY, Blocks.DANDELION, Blocks.AZURE_BLUET};
            for (int x = X - 28; x <= X + 28; x++) {
                for (int z = OZ - 12; z <= OZ + 12; z++) {
                    BlockPos pos = new BlockPos(x, g, z);
                    double dense = z < OZ - 4 ? 0.45 : z < OZ + 2 ? 0.18 : 0.08;
                    if (Math.abs(x - X) <= 2 && Math.abs(z - OZ) <= 2 || !level.getBlockState(pos).isAir()) {
                        continue;                       // um den Hirsch frei
                    }
                    float r = random.nextFloat();
                    if (r < dense * 0.55) {
                        level.setBlock(pos, Blocks.SHORT_GRASS.defaultBlockState(), Block.UPDATE_CLIENTS);
                    } else if (r < dense * 0.7) {
                        level.setBlock(pos, Blocks.FERN.defaultBlockState(), Block.UPDATE_CLIENTS);
                    } else if (r < dense * 0.85) {
                        level.setBlock(pos, flowers[random.nextInt(flowers.length)].defaultBlockState(), Block.UPDATE_CLIENTS);
                    } else if (r < dense * 0.9 && z < OZ - 3) {
                        BlockPos top = pos.above();
                        level.setBlock(pos, Blocks.TALL_GRASS.defaultBlockState(), Block.UPDATE_CLIENTS);
                        level.setBlock(top, Blocks.TALL_GRASS.defaultBlockState().setValue(
                                net.minecraft.world.level.block.DoublePlantBlock.HALF, net.minecraft.world.level.block.state.properties.DoubleBlockHalf.UPPER),
                                Block.UPDATE_CLIENTS);
                    }
                }
            }
            // ein unsichtbarer Angreifer, auf den der Hirsch losgeht (ein Rüstungsständer taugt nicht als Gegner)
            double facing = Math.toRadians(STAG_YAW);
            DeerEntity dummy = spawn(level, ModEntities.DEER.get(), X + 0.5 - Math.sin(facing) * 9.0, OZ + 0.5 + Math.cos(facing) * 9.0, 0.0f, m -> {
                m.setInvisible(true);
                m.setSilent(true);
            });
            stag[0] = spawn(level, ModEntities.DEER.get(), X + 0.5, OZ + 0.5, STAG_YAW, m -> {
                m.setStag(true);
                m.setTarget(dummy);
            }).getUUID();
            // Hirschkuh und Kitz flüchten im Hintergrund
            spawn(level, ModEntities.DEER.get(), X - 6.0, OZ - 5.0, 135.0f, m -> m.setStag(false));
            spawn(level, ModEntities.DEER.get(), X - 4.4, OZ - 4.1, 140.0f, m -> {
                m.setStag(false);
                m.setAge(-24000);
            });
        }));
        steps.add(Step.waitTicks(80));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));
        // schräg von vorn: das gesenkte Geweih
        steps.add(around(4.0, 25.0, 1.1, 1.3));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("hard_stag"));
        // von der Seite: der Stoß (eine Bildfolge, zweimal gestoßen)
        steps.add(around(4.6, 80.0, 1.25, 1.2));
        steps.add(Step.waitTicks(30));
        int[] frame = {0};
        steps.add(Step.record("hard_ram", 24, minecraft -> {
            if (frame[0]++ % 12 == 0 && minecraft.getSingleplayerServer() != null) {
                var server = minecraft.getSingleplayerServer();
                server.execute(() -> {
                    if (server.overworld().getEntity(stag[0]) instanceof DeerEntity deer) {
                        deer.level().broadcastEntityEvent(deer, DeerEntity.EVENT_RAM);
                    }
                });
            }
        }));
        enchanting(steps);
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /** Der Zaubertisch: Er will doppelt so viel Lapislazuli (im Tooltip „4 Lapislazuli“ für die zweite Wahl). */
    private static void enchanting(Deque<Step> steps) {
        BlockPos table = new BlockPos(X + 20, 0, OZ + 20);
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("xp add @s 30 levels"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            BlockPos at = table.atY(g);
            Showcase.fill(level, at.offset(-3, -1, -3), at.offset(3, -1, 3), Blocks.OAK_PLANKS.defaultBlockState());
            Showcase.fill(level, at.offset(-3, 0, -3), at.offset(3, 3, 3), Blocks.AIR.defaultBlockState());
            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    if ((Math.abs(dx) == 2 || Math.abs(dz) == 2) && !(dz == 2 && Math.abs(dx) <= 1)) {
                        level.setBlock(at.offset(dx, 0, dz), Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_CLIENTS);
                        level.setBlock(at.offset(dx, 1, dz), Blocks.BOOKSHELF.defaultBlockState(), Block.UPDATE_CLIENTS);
                    }
                }
            }
            level.setBlock(at, Blocks.ENCHANTING_TABLE.defaultBlockState(), Block.UPDATE_CLIENTS);
        }));
        steps.add(Showcase.tp(table.getX() + 0.5, 0.0, table.getZ() + 2.0, 180.0f, 30.0f));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            BlockPos at = table.atY(Showcase.ground);
            net.minecraft.server.level.ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.openMenu(new net.minecraft.world.SimpleMenuProvider((id, inventory, p) -> new net.minecraft.world.inventory.EnchantmentMenu(id,
                    inventory, net.minecraft.world.inventory.ContainerLevelAccess.create(level, at)),
                    net.minecraft.network.chat.Component.translatable("container.enchant")));
            player.containerMenu.getSlot(0).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.DIAMOND_SWORD));
            player.containerMenu.getSlot(1).set(new net.minecraft.world.item.ItemStack(net.minecraft.world.item.Items.LAPIS_LAZULI, 5));
            player.containerMenu.broadcastChanges();
        }));
        steps.add(Step.waitTicks(20));
        // die Maus über die zweite Wahl (Fenster 176 × 166 in der Mitte, die Wahl bei 60/33, 108 × 17)
        steps.add(Step.once(minecraft -> {
            double scale = minecraft.getWindow().getGuiScale();
            int left = (minecraft.getWindow().getGuiScaledWidth() - 176) / 2;
            int top = (minecraft.getWindow().getGuiScaledHeight() - 166) / 2;
            hover(minecraft, (left + 60 + 54) * scale, (top + 14 + 19 + 8) * scale);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("hard_enchant"));
        steps.add(Step.once(minecraft -> {
            minecraft.player.closeContainer();
            Showcase.parkMouse(minecraft);
        }));
    }

    private static void hover(net.minecraft.client.Minecraft minecraft, double x, double y) {
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? x : y);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
