package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Combat;
import io.github.sirschrolli.kingdomsatwar.entity.combat.MoveKind;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Style;
import io.github.sirschrolli.kingdomsatwar.entity.combat.Temper;
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
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der Kampfstile aus 1.14: je ein Monster auf einer kleinen Bühne, davor ein Ritter ohne eigenen Willen; der
 * besondere Angriff wird ausgelöst und im besten Moment fotografiert ({@code combat114_<name>}).
 * Aufruf: {@code -Dkingdomsatwar.showcase=combat114} (alle) oder {@code combat114_<name>} (eine Szene).
 */
final class CombatShowcase {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private CombatShowcase() {}

    /**
     * Eine Szene: das Monster (dx, dz vor der Mitte), seine Waffe, der Angriff, die Ritter (je dx, dz), die Kamera und der
     * Blickpunkt (relativ zur Mitte am Boden) und der Tick nach dem Auslösen, zu dem fotografiert wird; dazu Artgenossen
     * (für den Kampfschrei).
     */
    private record Scene(String name, Supplier<EntityType<? extends Mob>> type, double[] at, Item weapon, MoveKind move, BlockState floor,
            double[][] knights, double[] cam, double[] focus, int shot, int allies) {}

    private static final List<Scene> SCENES = List.of(
            new Scene("revenant_slam", () -> ModEntities.ZOMBIE.get(), new double[] {0.0, -1.0}, Items.AIR, MoveKind.SLAM,
                    Blocks.COARSE_DIRT.defaultBlockState(), new double[][] {{0.0, 1.3}}, new double[] {4.6, 2.4, 3.4},
                    new double[] {0.0, 1.2, 0.0}, 13, 0),
            new Scene("revenant_lunge", () -> ModEntities.ZOMBIE.get(), new double[] {-3.0, 0.0}, Items.AIR, MoveKind.LUNGE,
                    Blocks.GRASS_BLOCK.defaultBlockState(), new double[][] {{3.5, 0.0}}, new double[] {0.5, 2.2, 6.0},
                    new double[] {0.0, 1.4, 0.0}, 18, 0),
            new Scene("bones_aimed", () -> ModEntities.SKELETON.get(), new double[] {-5.0, -1.0}, Items.BOW, MoveKind.AIMED,
                    Blocks.STONE.defaultBlockState(), new double[][] {{5.0, 1.0}}, new double[] {0.0, 3.2, 6.5},
                    new double[] {0.0, 1.2, 0.0}, 31, 0),
            new Scene("spider_web", () -> ModEntities.SPIDER.get(), new double[] {-4.0, 0.0}, Items.AIR, MoveKind.SPIT,
                    Blocks.MOSS_BLOCK.defaultBlockState(), new double[][] {{3.5, 0.0}}, new double[] {0.0, 2.6, 6.0},
                    new double[] {0.0, 1.0, 0.0}, 18, 0),
            new Scene("creeper_vine", () -> ModEntities.CREEPER.get(), new double[] {-3.5, 0.0}, Items.AIR, MoveKind.VINE,
                    Blocks.GRASS_BLOCK.defaultBlockState(), new double[][] {{4.0, 0.0}}, new double[] {0.0, 2.6, 6.0},
                    new double[] {0.0, 1.0, 0.0}, 14, 0),
            new Scene("butcher_sweep", () -> ModEntities.VINDICATOR.get(), new double[] {0.0, 0.0}, Items.IRON_AXE, MoveKind.SWEEP,
                    Blocks.SPRUCE_PLANKS.defaultBlockState(), new double[][] {{0.0, 2.2}, {1.9, -1.2}, {-1.9, -1.2}},
                    new double[] {4.5, 2.6, 4.5}, new double[] {0.0, 1.0, 0.0}, 13, 0),
            new Scene("devastator_charge", () -> ModEntities.RAVAGER.get(), new double[] {-6.0, 0.0}, Items.AIR, MoveKind.CHARGE,
                    Blocks.DIRT_PATH.defaultBlockState(), new double[][] {{6.0, 0.0}}, new double[] {1.0, 3.0, 8.0},
                    new double[] {0.0, 1.2, 0.0}, 24, 0),
            new Scene("hag_curse", () -> ModEntities.WITCH.get(), new double[] {-4.0, -1.5}, Items.AIR, MoveKind.CURSE,
                    Blocks.PODZOL.defaultBlockState(), new double[][] {{3.0, 1.0}}, new double[] {0.0, 3.4, 6.5},
                    new double[] {0.5, 0.6, 0.0}, 18, 0),
            new Scene("conjurer_fangs", () -> ModEntities.EVOKER.get(), new double[] {-4.0, -1.0}, Items.AIR, MoveKind.FANGS,
                    Blocks.DARK_OAK_PLANKS.defaultBlockState(), new double[][] {{3.0, 1.0}}, new double[] {0.0, 3.6, 6.5},
                    new double[] {0.8, 0.6, 0.0}, 23, 0),
            new Scene("blaze_ring", () -> ModEntities.BLAZE.get(), new double[] {0.0, 0.0}, Items.AIR, MoveKind.FIRE_RING,
                    Blocks.NETHERRACK.defaultBlockState(), new double[][] {{4.5, 0.0}, {-3.5, 2.5}}, new double[] {0.0, 4.5, 8.0},
                    new double[] {0.0, 0.5, 0.0}, 22, 0),
            new Scene("enderman_ambush", () -> ModEntities.ENDERMAN.get(), new double[] {-5.0, -2.0}, Items.AIR, MoveKind.AMBUSH,
                    Blocks.END_STONE.defaultBlockState(), new double[][] {{1.5, 0.0}}, new double[] {-2.5, 2.4, 5.0},
                    new double[] {1.0, 1.4, -0.6}, 13, 0),
            new Scene("leader_roar", () -> ModEntities.ZOMBIE.get(), new double[] {0.0, -1.0}, Items.IRON_SWORD, MoveKind.ROAR,
                    Blocks.COARSE_DIRT.defaultBlockState(), new double[][] {{0.0, 5.0}}, new double[] {5.0, 3.0, 4.5},
                    new double[] {0.0, 1.2, -0.5}, 15, 3));

    static void combat(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        int index = 0;
        boolean first = true;
        for (Scene scene : SCENES) {
            int ox = 41000 + index * 120;
            int oz = 41000;
            index++;
            if (!only.isEmpty() && !scene.name().equals(only)) {
                continue;
            }
            Mob[] mob = new Mob[1];
            List<Entity> cast = new ArrayList<>();
            steps.add(Showcase.tp(ox + 0.5, 30, oz - 20.5, 0, 30));
            steps.add(Step.waitTicks(first ? 100 : 20));
            steps.add(Step.waitForChunks(600));
            first = false;
            steps.add(Step.server(level -> {
                int g = Showcase.ground;
                int r = 10;
                Showcase.fill(level, new BlockPos(ox - r, g - 3, oz - r), new BlockPos(ox + r, g - 1, oz + r), scene.floor());
                Showcase.fill(level, new BlockPos(ox - r, g, oz - r), new BlockPos(ox + r, g + 10, oz + r), Blocks.AIR.defaultBlockState());
                for (double[] k : scene.knights()) {
                    SoldierEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
                    if (knight == null) {
                        continue;
                    }
                    knight.equip();
                    float yaw = (float) Math.toDegrees(Math.atan2(-(scene.at()[0] - k[0]), scene.at()[1] - k[1]));
                    knight.snapTo(ox + 0.5 + k[0], g, oz + 0.5 + k[1], yaw, 0.0f);
                    knight.setYBodyRot(yaw);
                    knight.setYHeadRot(yaw);
                    knight.setNoAi(true);
                    knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
                    knight.setHealth(1000.0f);
                    knight.setPersistenceRequired();
                    level.addFreshEntity(knight);
                    cast.add(knight);
                }
                Mob m = spawn(level, scene.type().get(), new Vec3(ox + 0.5 + scene.at()[0], g, oz + 0.5 + scene.at()[1]), scene.weapon());
                cast.add(m);
                mob[0] = m;
                Combat.setStyle(m, new Style(scene.move() == MoveKind.ROAR ? Temper.ANFUEHRER : Temper.UNGESTUEM, List.of(scene.move()), 1.0f));
                Combat.pause(m, 100000);
                for (int i = 0; i < scene.allies(); i++) {
                    double a = Math.PI * (0.7 + i * 0.3);
                    Mob ally = spawn(level, scene.type().get(), new Vec3(ox + 0.5 + Math.cos(a) * 2.6, g, oz - 0.5 + Math.sin(a) * 2.6 - 1.5),
                            Items.AIR);
                    Combat.pause(ally, 100000);
                    ally.setNoAi(true);
                    cast.add(ally);
                }
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(level -> {
                // Sonnenschirme hoch über dem Weg (sonst brennen die Untoten in der Mittagssonne), wie in MonsterShowcase
                int g = Showcase.ground;
                for (double[] k : scene.knights()) {
                    umbrella(level, ox + scene.at()[0], oz + scene.at()[1], ox + k[0], oz + k[1], g + 40);
                }
                for (Entity e : cast) {
                    umbrella(level, e.getX() - 0.5, e.getZ() - 0.5, e.getX() - 0.5, e.getZ() - 0.5, g + 40);
                    e.clearFire();
                }
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.once(minecraft -> {
                Vec3 o = new Vec3(ox + 0.5, Showcase.ground, oz + 0.5);
                Vec3 cam = o.add(scene.cam()[0], scene.cam()[1], scene.cam()[2]);
                Vec3 look = o.add(scene.focus()[0], scene.focus()[1], scene.focus()[2]).subtract(cam);
                float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x, cam.y - 1.62, cam.z,
                        yaw, pitch));
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.server(level -> {
                Mob m = mob[0];
                LivingEntity target = cast.stream().filter(e -> e instanceof SoldierEntity).map(e -> (LivingEntity) e).findFirst().orElse(null);
                if (m != null && target != null) {
                    m.setTarget(target);
                    Combat.force(m, scene.move(), target);
                }
            }));
            steps.add(Step.waitTicks(3));
            steps.add(Step.server(level -> {
                if (mob[0] != null) {
                    LOGGER.info("[Showcase] {}: lebt={} Angriff={}", scene.name(), mob[0].isAlive(), Combat.current(mob[0]));
                }
            }));
            steps.add(Step.waitTicks(Math.max(1, scene.shot() - 3)));
            steps.add(Step.screenshot("combat114_" + scene.name()));
            steps.add(Step.waitTicks(20));
            steps.add(Step.server(level -> {
                cast.forEach(Entity::discard);
                level.getEntitiesOfClass(Entity.class, new AABB(new BlockPos(ox, Showcase.ground, oz)).inflate(18, 20, 18),
                        e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);
            }));
        }
    }

    /** Eine Reihe getönter Glasblöcke hoch über der Strecke (ein Block breit – darunter bleibt es hell). */
    private static void umbrella(ServerLevel level, double x0, double z0, double x1, double z1, int y) {
        int steps = (int) Math.ceil(Math.max(Math.abs(x1 - x0), Math.abs(z1 - z0))) + 1;
        for (int i = 0; i <= steps; i++) {
            double t = steps == 0 ? 0.0 : (double) i / steps;
            BlockPos pos = BlockPos.containing(x0 + (x1 - x0) * t + 0.5, y, z0 + (z1 - z0) * t + 0.5);
            level.setBlock(pos, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            level.setBlock(pos, Blocks.TINTED_GLASS.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
        }
    }

    private static Mob spawn(ServerLevel level, EntityType<? extends Mob> type, Vec3 at, Item weapon) {
        Mob mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setPersistenceRequired();
        mob.snapTo(at.x, at.y, at.z, 0.0f, 0.0f);
        if (weapon != Items.AIR) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(weapon));
        }
        level.addFreshEntity(mob);
        return mob;
    }
}
