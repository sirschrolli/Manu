package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.BossFury;
import io.github.sirschrolli.kingdomsatwar.entity.boss.fury.FuryAttack;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Vorführung des Zorns der Bosse aus 1.14: ein Boss auf einer Bühne, um ihn Ritter ohne eigenen Willen; ein Zornangriff
 * (oder der Wutausbruch) wird ausgelöst und im besten Moment fotografiert ({@code fury114_<name>}).
 * Aufruf: {@code -Dkingdomsatwar.showcase=fury114} (alle) oder {@code fury114_<name>} (eine Szene).
 */
final class FuryShowcase {
    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    private FuryShowcase() {}

    /** Eine Szene: der Boss, der Angriff (null: der Wutausbruch), der Boden, die Ritter (dx, dz), Kamera, Blickpunkt, Zeitpunkt. */
    private record Scene(String name, Supplier<EntityType<? extends Mob>> type, @Nullable FuryAttack attack, BlockState floor, double[][] knights,
            double[] cam, double[] focus, int shot) {}

    private static final List<Scene> SCENES = List.of(
            new Scene("glutschmied_rain", () -> ModEntities.GLUTSCHMIED.get(), FuryAttack.RAIN, Blocks.BLACKSTONE.defaultBlockState(),
                    new double[][] {{3.5, -3.0}, {-3.0, -3.5}, {0.5, -6.0}}, new double[] {5.0, 6.0, 11.0}, new double[] {0.0, 1.0, -2.0}, 28),
            new Scene("stonekeeper_lines", () -> ModEntities.STONE_KEEPER.get(), FuryAttack.LINES, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                    new double[][] {{6.0, 1.0}, {-2.5, 6.0}, {-4.0, -4.5}}, new double[] {2.0, 13.0, 13.0}, new double[] {0.0, 0.0, 0.5}, 27),
            new Scene("sordin_waves", () -> ModEntities.SORDIN.get(), FuryAttack.WAVES, Blocks.DEEPSLATE_TILES.defaultBlockState(),
                    new double[][] {{3.0, 5.0}, {-4.5, 4.0}, {5.5, -1.0}}, new double[] {0.0, 7.0, 13.0}, new double[] {0.0, 0.0, 1.0}, 37),
            new Scene("mage_pull", () -> ModEntities.MAGES.get(DragonType.WATER).get(), FuryAttack.PULL, Blocks.PRISMARINE_BRICKS.defaultBlockState(),
                    new double[][] {{4.0, 2.0}, {-3.5, 3.0}, {1.0, 5.5}}, new double[] {6.0, 6.0, 10.5}, new double[] {0.0, 2.2, 0.5}, 40),
            new Scene("hourwarden_beams", () -> ModEntities.HOUR_WARDEN.get(), FuryAttack.BEAMS, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),
                    new double[][] {{5.0, 4.0}, {-5.5, 2.5}, {2.0, -6.0}}, new double[] {9.0, 7.5, 10.0}, new double[] {0.0, 0.0, 0.0}, 34),
            new Scene("mystik_sectors", () -> ModEntities.MISS_MYSTIK.get(), FuryAttack.SECTORS, Blocks.MOSS_BLOCK.defaultBlockState(),
                    new double[][] {{4.0, 4.0}, {-5.0, 2.0}, {1.0, -6.0}}, new double[] {0.0, 14.0, 12.0}, new double[] {0.0, 0.0, 0.5}, 25),
            new Scene("glutschmied_nova", () -> ModEntities.GLUTSCHMIED.get(), FuryAttack.NOVA, Blocks.BLACKSTONE.defaultBlockState(),
                    new double[][] {{3.5, 4.0}, {-4.0, 3.0}}, new double[] {6.0, 4.0, 9.5}, new double[] {0.0, 2.0, 0.0}, 50),
            new Scene("robber_enrage", () -> ModEntities.ROBBER_KNIGHT.get(), null, Blocks.COBBLESTONE.defaultBlockState(),
                    new double[][] {{3.0, 2.5}, {-3.0, 2.0}, {0.0, -3.5}}, new double[] {6.0, 4.0, 9.0}, new double[] {0.0, 1.5, 0.0}, 4));

    static void fury(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        int index = 0;
        boolean first = true;
        for (Scene scene : SCENES) {
            int ox = 43000 + index * 140;
            int oz = 43000;
            index++;
            if (!only.isEmpty() && !scene.name().equals(only)) {
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
                for (double[] k : scene.knights()) {
                    SoldierEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
                    if (knight == null) {
                        continue;
                    }
                    knight.equip();
                    float yaw = (float) Math.toDegrees(Math.atan2(k[0], -k[1]));
                    knight.snapTo(ox + 0.5 + k[0], g, oz + 0.5 + k[1], yaw, 0.0f);
                    knight.setYBodyRot(yaw);
                    knight.setYHeadRot(yaw);
                    knight.setNoAi(true);
                    knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(2000.0);
                    knight.setHealth(2000.0f);
                    knight.setPersistenceRequired();
                    level.addFreshEntity(knight);
                    cast.add(knight);
                }
                Mob m = scene.type().get().create(level, EntitySpawnReason.COMMAND);
                if (m == null) {
                    return;
                }
                m.snapTo(ox + 0.5, g, oz + 0.5, 0.0f, 0.0f);
                m.setPersistenceRequired();
                m.setNoAi(true);
                level.addFreshEntity(m);
                // der Boss schaut zur Kamera
                Vec3 cam = new Vec3(scene.cam()[0], 0.0, scene.cam()[2]);
                float yaw = (float) Math.toDegrees(Math.atan2(-cam.x, cam.z));
                m.setYRot(yaw);
                m.setYBodyRot(yaw);
                m.setYHeadRot(yaw);
                BossFury.hold(m, 100000);
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
                if (m == null) {
                    LOGGER.warn("[Showcase] {}: kein Boss", scene.name());
                    return;
                }
                boolean started = scene.attack() == null ? BossFury.enrage(m) : BossFury.force(m, scene.attack());
                LOGGER.info("[Showcase] {}: Zorn={} gestartet={}", scene.name(), BossFury.of(m) != null, started);
            }));
            steps.add(Step.waitTicks(scene.shot()));
            steps.add(Step.screenshot("fury114_" + scene.name()));
            steps.add(Step.waitTicks(10));
            steps.add(Step.server(level -> {
                cast.forEach(Entity::discard);
                level.getEntitiesOfClass(Entity.class, new AABB(new BlockPos(ox, Showcase.ground, oz)).inflate(24, 24, 24),
                        e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);
            }));
        }
    }
}
