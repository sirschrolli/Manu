package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.miniboss.MiniBoss;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Vorführung der Mini-Bosse aus 1.15 ({@code miniboss115_<name>}): je ein Mini-Boss auf einer Bühne, ein Ritter als Ziel, eine
 * Kunst wird ausgelöst und im besten Moment fotografiert – dazu jeder Mini-Boss im Schlaf. Nachts, mit Nachtsicht (der
 * Trollvater erstarrt sonst im Sonnenlicht). Aufruf: {@code -Dkingdomsatwar.showcase=miniboss115} (alle) oder eine Szene.
 */
final class MiniBossShowcase {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private MiniBossShowcase() {}

    /**
     * Eine Szene: der Mini-Boss, die Kunst (null: er schläft), in Rage?, der Boden, wo der Ritter steht (dx, dz), Kamera,
     * Blickpunkt (relativ zur Mitte), Zeitpunkt des Bildes (Ticks nach dem Auslösen).
     */
    private record Scene(String name, Supplier<? extends EntityType<? extends Mob>> type, @Nullable String special, boolean enraged, BlockState floor,
            double[] knight, double[] cam, double[] focus, int shot) {}

    private static final BlockState MOSS = Blocks.PALE_MOSS_BLOCK.defaultBlockState();
    private static final BlockState TILES = Blocks.DEEPSLATE_TILES.defaultBlockState();
    private static final BlockState HALL = Blocks.POLISHED_DEEPSLATE.defaultBlockState();
    private static final BlockState DEN = Blocks.MOSS_BLOCK.defaultBlockState();

    private static final List<Scene> SCENES = List.of(
            new Scene("brood_sleep", () -> ModEntities.BROOD_MOTHER.get(), null, false, MOSS, new double[] {0, 12}, new double[] {5.5, 3.5, 6.5},
                    new double[] {0, 1.0, 0}, 0),
            new Scene("brood_web", () -> ModEntities.BROOD_MOTHER.get(), "web", false, MOSS, new double[] {2, 9}, new double[] {7.5, 3.0, 4.5},
                    new double[] {1, 1.8, 3}, 19),
            new Scene("brood_brood", () -> ModEntities.BROOD_MOTHER.get(), "brood", false, MOSS, new double[] {0, 7}, new double[] {6.0, 4.5, 7.5},
                    new double[] {0, 0.8, 1}, 31),
            new Scene("brood_pounce", () -> ModEntities.BROOD_MOTHER.get(), "pounce", false, MOSS, new double[] {0, 9}, new double[] {9.5, 3.0, 4.5},
                    new double[] {0, 2.5, 4}, 19),
            new Scene("brood_venom", () -> ModEntities.BROOD_MOTHER.get(), "venom", true, MOSS, new double[] {1, 4}, new double[] {6.5, 4.5, 7.0},
                    new double[] {0, 1.0, 1}, 26),
            new Scene("crypt_sleep", () -> ModEntities.CRYPT_KING.get(), null, false, TILES, new double[] {0, 12}, new double[] {-1.5, 2.3, 4.0},
                    new double[] {0, 1.7, 0}, 0),
            new Scene("crypt_raise", () -> ModEntities.CRYPT_KING.get(), "raise", false, TILES, new double[] {0, 7}, new double[] {5.5, 3.5, 7.5},
                    new double[] {0, 1.0, 1}, 33),
            new Scene("crypt_cleave", () -> ModEntities.CRYPT_KING.get(), "cleave", false, TILES, new double[] {0, 3}, new double[] {4.0, 2.4, 3.5},
                    new double[] {0, 1.5, 1}, 13),
            new Scene("crypt_chill", () -> ModEntities.CRYPT_KING.get(), "chill", false, TILES, new double[] {1, 3}, new double[] {6.0, 4.5, 6.5},
                    new double[] {0, 0.5, 0}, 21),
            new Scene("crypt_bones", () -> ModEntities.CRYPT_KING.get(), "bones", true, TILES, new double[] {0, 12}, new double[] {6.5, 4.0, 2.5},
                    new double[] {0, 0.5, 6}, 25),
            new Scene("rune_sleep", () -> ModEntities.RUNE_WARDEN.get(), null, false, HALL, new double[] {0, 12}, new double[] {3.5, 2.8, 5.0},
                    new double[] {0, 2.2, 0}, 0),
            new Scene("rune_slam", () -> ModEntities.RUNE_WARDEN.get(), "slam", false, HALL, new double[] {0, 4}, new double[] {5.5, 3.0, 7.0},
                    new double[] {0, 2.0, 0}, 13),
            new Scene("rune_slam_hit", () -> ModEntities.RUNE_WARDEN.get(), "slam", false, HALL, new double[] {0, 4}, new double[] {5.5, 3.0, 7.0},
                    new double[] {0, 1.0, 0}, 23),
            new Scene("rune_beam", () -> ModEntities.RUNE_WARDEN.get(), "beam", false, HALL, new double[] {0, 11}, new double[] {6.5, 3.2, -3.0},
                    new double[] {0, 2.0, 5}, 35),
            new Scene("rune_mines", () -> ModEntities.RUNE_WARDEN.get(), "mines", false, HALL, new double[] {0, 8}, new double[] {7.5, 3.2, 1.0},
                    new double[] {0, 1.0, 5}, 22),
            new Scene("rune_mines_burst", () -> ModEntities.RUNE_WARDEN.get(), "mines", false, HALL, new double[] {0, 8}, new double[] {7.5, 3.2, 1.0},
                    new double[] {0, 1.5, 5}, 32),
            new Scene("rune_ward", () -> ModEntities.RUNE_WARDEN.get(), "ward", true, HALL, new double[] {0, 4}, new double[] {4.5, 3.0, 5.5},
                    new double[] {0, 2.0, 0}, 30),
            new Scene("troll_sleep", () -> ModEntities.TROLL_FATHER.get(), null, false, DEN, new double[] {0, 12}, new double[] {4.5, 3.5, 6.5},
                    new double[] {0, 2.5, 0}, 0),
            new Scene("troll_boulder", () -> ModEntities.TROLL_FATHER.get(), "boulder", false, DEN, new double[] {0, 11}, new double[] {6.0, 3.5, 7.0},
                    new double[] {0, 3.5, 0}, 14),
            new Scene("troll_throw", () -> ModEntities.TROLL_FATHER.get(), "boulder", false, DEN, new double[] {0, 11}, new double[] {7.5, 4.0, 4.0},
                    new double[] {0, 3.0, 5}, 27),
            new Scene("troll_stomp", () -> ModEntities.TROLL_FATHER.get(), "stomp", false, DEN, new double[] {1, 4}, new double[] {7.0, 4.5, 7.0},
                    new double[] {0, 1.0, 0}, 19),
            new Scene("troll_roar", () -> ModEntities.TROLL_FATHER.get(), "roar", false, DEN, new double[] {0, 5}, new double[] {4.0, 3.0, 5.0},
                    new double[] {0, 3.6, 0}, 13),
            new Scene("troll_rockfall", () -> ModEntities.TROLL_FATHER.get(), "roar", false, DEN, new double[] {0, 5}, new double[] {7.0, 6.0, 9.0},
                    new double[] {0, 2.0, 2}, 30),
            new Scene("troll_spin", () -> ModEntities.TROLL_FATHER.get(), "spin", true, DEN, new double[] {0, 3}, new double[] {5.5, 3.5, 6.0},
                    new double[] {0, 2.0, 0}, 16));

    static void miniBosses(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 18000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        int index = 0;
        boolean first = true;
        for (Scene scene : SCENES) {
            int ox = 47000 + index * 90;
            int oz = 47000;
            index++;
            if (!only.isEmpty() && !scene.name().equals(only) && !scene.name().startsWith(only + "_")) {
                continue;
            }
            Mob[] boss = new Mob[1];
            List<Entity> cast = new ArrayList<>();
            steps.add(Showcase.tp(ox + 0.5, 30, oz - 20.5, 0, 30));
            steps.add(Step.waitTicks(first ? 100 : 20));
            steps.add(Step.waitForChunks(600));
            first = false;
            steps.add(Step.server(level -> {
                int g = Showcase.ground;
                int r = 16;
                Showcase.fill(level, new BlockPos(ox - r, g - 3, oz - r), new BlockPos(ox + r, g - 1, oz + r), scene.floor());
                Showcase.fill(level, new BlockPos(ox - r, g, oz - r), new BlockPos(ox + r, g + 16, oz + r), Blocks.AIR.defaultBlockState());
                if (scene.type().get() == ModEntities.CRYPT_KING.get() && scene.special() == null) {
                    throne(level, ox, g, oz);
                }
                SoldierEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
                if (knight != null) {
                    knight.equip();
                    double[] k = scene.knight();
                    float yaw = (float) Math.toDegrees(Math.atan2(k[0], -k[1]));
                    knight.snapTo(ox + 0.5 + k[0], g, oz + 0.5 + k[1], yaw, 0.0f);
                    knight.setYBodyRot(yaw);
                    knight.setYHeadRot(yaw);
                    knight.setNoAi(true);
                    knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0);
                    knight.setHealth(4000.0f);
                    knight.setPersistenceRequired();
                    level.addFreshEntity(knight);
                    cast.add(knight);
                }
                Mob m = scene.type().get().create(level, EntitySpawnReason.COMMAND);
                if (m == null) {
                    return;
                }
                double y = scene.type().get() == ModEntities.CRYPT_KING.get() && scene.special() == null ? g + 0.5 : g;
                // er schaut zum Ritter (nach Süden)
                m.snapTo(ox + 0.5, y, oz + 0.5, 0.0f, 0.0f);
                m.setYBodyRot(0.0f);
                m.setYHeadRot(0.0f);
                m.setPersistenceRequired();
                m.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(0.0);
                level.addFreshEntity(m);
                if (m instanceof MiniBoss mini) {
                    mini.miniBoss().setHome(m.blockPosition());
                    mini.miniBoss().hold(100000);
                    if (scene.enraged()) {
                        // gleich in Rage (ohne Wecken: das gäbe ihm in 40 Ticks einen eigenen Angriff)
                        mini.setEnraged(true);
                        m.setHealth(m.getMaxHealth() * 0.35f);
                    }
                }
                cast.add(m);
                boss[0] = m;
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.once(minecraft -> {
                Vec3 o = new Vec3(ox + 0.5, Showcase.ground, oz + 0.5);
                Vec3 cam = o.add(scene.cam()[0], scene.cam()[1], scene.cam()[2]);
                Vec3 look = o.add(scene.focus()[0], scene.focus()[1], scene.focus()[2]).subtract(cam);
                float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x, cam.y - 1.62, cam.z,
                        yaw, pitch));
            }));
            steps.add(Step.waitTicks(15));
            steps.add(Step.server(level -> {
                Mob m = boss[0];
                if (m == null || scene.special() == null || !(m instanceof MiniBoss mini)) {
                    return;
                }
                Entity target = cast.isEmpty() ? null : cast.getFirst();
                mini.miniBoss().hold(100000);
                boolean started = target instanceof net.minecraft.world.entity.LivingEntity living && mini.miniBoss().force(level, scene.special(), living);
                LOGGER.info("[Showcase] {}: {} gestartet={} Rage={}", scene.name(), scene.special(), started, mini.isEnraged());
            }));
            steps.add(Step.waitTicks(Math.max(1, scene.shot())));
            steps.add(Step.screenshot("miniboss115_" + scene.name()));
            steps.add(Step.waitTicks(10));
            steps.add(Step.server(level -> {
                cast.forEach(Entity::discard);
                level.getEntitiesOfClass(Entity.class, new AABB(new BlockPos(ox, Showcase.ground, oz)).inflate(24, 24, 24),
                        e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);
            }));
        }
    }

    /** Ein Thron für den schlafenden Gruftkönig: Sitz, hohe Lehne mit Goldkrone, Armlehnen, zwei Seelenfeuer. */
    private static void throne(ServerLevel level, int ox, int g, int oz) {
        level.setBlock(new BlockPos(ox, g, oz), Blocks.POLISHED_BLACKSTONE_SLAB.defaultBlockState()
                .setValue(net.minecraft.world.level.block.SlabBlock.TYPE, SlabType.BOTTOM), 2);
        for (int y = 0; y <= 3; y++) {
            level.setBlock(new BlockPos(ox, g + y, oz - 1), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            if (y <= 2) {
                level.setBlock(new BlockPos(ox - 1, g + y, oz - 1), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
                level.setBlock(new BlockPos(ox + 1, g + y, oz - 1), Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(), 2);
            }
        }
        level.setBlock(new BlockPos(ox, g + 4, oz - 1), Blocks.CHISELED_POLISHED_BLACKSTONE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox, g + 5, oz - 1), Blocks.GOLD_BLOCK.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox - 1, g, oz), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox + 1, g, oz), Blocks.POLISHED_BLACKSTONE_BRICK_WALL.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox - 3, g, oz + 1), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);
        level.setBlock(new BlockPos(ox + 3, g, oz + 1), Blocks.SOUL_CAMPFIRE.defaultBlockState(), 2);
    }
}
