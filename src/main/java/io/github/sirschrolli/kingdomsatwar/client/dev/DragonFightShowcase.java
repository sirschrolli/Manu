package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Prüfung der Drachen im echten Kampf ({@code drachen_kampf}, mit eigener KI): der eigene Drache gegen einen
 * Wiedergänger am Boden, gegen einen Schützen auf einer Säule (aus der Luft), der Drache fliegt zu seinem Herrn und
 * landet, und Pyrrhon gegen einen Ritter – je als Bildfolge (ein Bild je Tick), die Kamera folgt dem Geschehen.
 */
final class DragonFightShowcase {
    private static final int X0 = 16000;
    private static final int Z0 = 16000;

    private DragonFightShowcase() {}

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    static void fights(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        int[] g = new int[1];
        steps.add(tp(X0 + 0.5, 140, Z0 - 20, 0, 10));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.server(level -> {
            g[0] = 120;
            Showcase.fill(level, new BlockPos(X0 - 50, g[0] - 1, Z0 - 50), new BlockPos(X0 + 50, g[0] - 1, Z0 + 50),
                    Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(X0 - 50, g[0], Z0 - 50), new BlockPos(X0 + 50, g[0] + 40, Z0 + 50), Blocks.AIR.defaultBlockState());
        }));
        steps.add(Step.waitTicks(10));
        if (only.isEmpty() || only.equals("boden")) {
            ground(steps, g);
        }
        if (only.isEmpty() || only.equals("luft")) {
            air(steps, g);
        }
        if (only.isEmpty() || only.equals("folgt")) {
            follow(steps, g);
        }
        if (only.isEmpty() || only.equals("boss")) {
            boss(steps, g);
        }
        steps.add(Step.command("gamemode spectator"));
    }

    /** Teleport an eine feste Höhe (nicht relativ zum Boden der Vorführwelt). */
    private static Step tp(double x, double y, double z, float yaw, float pitch) {
        return Step.once(minecraft -> minecraft.player.connection.sendCommand(
                String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %s %s", x, y, z, yaw, pitch)));
    }

    private static void clear(Deque<Step> steps) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.waitTicks(5));
    }

    private static DragonEntity dragon(ServerLevel level, DragonType type, Vec3 at, float yaw, boolean flying) {
        DragonEntity d = ModEntities.DRAGONS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        d.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        d.setYBodyRot(yaw);
        d.setYHeadRot(yaw);
        d.applyStage(DragonStage.ADULT, false);
        d.setHealth(d.getMaxHealth());
        d.setPersistenceRequired();
        level.addFreshEntity(d);
        d.bondWith(player(level));
        if (flying) {
            d.setFlying(true);
        }
        return d;
    }

    private static <T extends Mob> T tough(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, Vec3 at, double health) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        mob.snapTo(at.x, at.y, at.z, 180.0f, 0.0f);
        mob.getAttribute(Attributes.MAX_HEALTH).setBaseValue(health);
        mob.setHealth((float) health);
        mob.setPersistenceRequired();
        mob.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
        level.addFreshEntity(mob);
        return mob;
    }

    /** Kamera von der Seite auf die Mitte zwischen zwei Wesen (Abstand wächst mit ihrem Abstand). */
    private static void watch(net.minecraft.client.Minecraft minecraft, LivingEntity a, LivingEntity b, double extra) {
        // vor allem auf den Drachen (a), das Gegenüber noch im Bild
        Vec3 mid = a.position().add(b.position().subtract(a.position()).scale(0.3)).add(0, 2.0, 0);
        double spread = a.position().distanceTo(b.position());
        double dist = 9.0 + Math.min(spread, 30.0) * 0.35 + extra;
        // quer zur Linie zwischen den beiden (sonst stünde das Gegenüber vor der Kamera)
        Vec3 line = b.position().subtract(a.position()).multiply(1, 0, 1);
        Vec3 side = line.lengthSqr() < 1.0e-4 ? new Vec3(0, 0, -1) : new Vec3(-line.z, 0, line.x).normalize();
        Vec3 cam = mid.add(side.scale(dist)).add(0, dist * 0.22, 0);
        Vec3 look = mid.subtract(cam);
        float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x, cam.y - 1.62,
                cam.z, yaw, pitch));
    }

    /** Am Boden: der Feuerdrache gegen einen zähen Wiedergänger. */
    private static void ground(Deque<Step> steps, int[] g) {
        clear(steps);
        steps.add(Step.command("gamemode spectator"));
        LivingEntity[] pair = new LivingEntity[2];
        steps.add(Step.server(level -> {
            pair[0] = dragon(level, DragonType.FIRE, new Vec3(X0 + 0.5, g[0], Z0 + 10.5), 180.0f, false);
            pair[1] = tough(level, ModEntities.ZOMBIE.get(), new Vec3(X0 + 3.5, g[0], Z0 - 6.5), 600.0);
            ((Mob) pair[1]).setTarget(pair[0]);
            ((Mob) pair[0]).setTarget(pair[1]);
        }));
        steps.add(Step.until(() -> pair[1] != null && pair[0] != null, 1200));
        steps.add(Step.waitTicks(4));
        steps.add(Step.record("dkampf_boden", 110, minecraft -> watch(minecraft, pair[0], pair[1], 0.0)));
    }

    /** In der Luft: der Eisdrache gegen einen zähen Schützen auf einer Säule. */
    private static void air(Deque<Step> steps, int[] g) {
        clear(steps);
        LivingEntity[] pair = new LivingEntity[2];
        steps.add(Step.server(level -> {
            for (int y = 0; y < 6; y++) {
                level.setBlock(new BlockPos(X0, g[0] + y, Z0 - 8), Blocks.STONE_BRICKS.defaultBlockState(), 2);
            }
            pair[0] = dragon(level, DragonType.ICE, new Vec3(X0 + 0.5, g[0], Z0 + 12.5), 180.0f, false);
            pair[1] = tough(level, ModEntities.SKELETON.get(), new Vec3(X0 + 0.5, g[0] + 6, Z0 - 7.5), 600.0);
            pair[1].setItemInHand(net.minecraft.world.InteractionHand.MAIN_HAND, new ItemStack(Items.BOW));
            pair[1].addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.SLOWNESS, 20000, 9));
            ((Mob) pair[1]).setTarget(pair[0]);
            ((Mob) pair[0]).setTarget(pair[1]);
        }));
        steps.add(Step.until(() -> pair[1] != null && pair[0] != null, 1200));
        steps.add(Step.waitTicks(4));
        steps.add(Step.record("dkampf_luft", 160, minecraft -> watch(minecraft, pair[0], pair[1], 3.0)));
    }

    /** Der Naturdrache fliegt aus der Ferne zu seinem Herrn (die Kamera) und landet neben ihm. */
    private static void follow(Deque<Step> steps, int[] g) {
        clear(steps);
        steps.add(Step.command("gamemode creative"));
        steps.add(tp(X0 + 0.5, 120, Z0 + 0.5, 135, 5));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(level -> {
            ServerPlayer p = player(level);
            p.getAbilities().flying = false;
            p.onUpdateAbilities();
            dragon(level, DragonType.NATURE, new Vec3(X0 - 26.5, g[0] + 9, Z0 - 26.5), -45.0f, true);
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.record("dfolgt", 130, minecraft -> { }));
        steps.add(Step.command("gamemode spectator"));
    }

    /** Pyrrhon gegen einen zähen Ritter. */
    private static void boss(Deque<Step> steps, int[] g) {
        clear(steps);
        steps.add(Step.command("gamemode spectator"));
        LivingEntity[] pair = new LivingEntity[2];
        steps.add(Step.server(level -> {
            var type = ModEntities.bossType(DragonType.FIRE);
            DragonBossEntity boss = type.create(level, EntitySpawnReason.COMMAND);
            boss.snapTo(X0 + 0.5, g[0], Z0 + 12.5, 180.0f, 0.0f);
            boss.setHome(new BlockPos(X0, g[0], Z0 + 12));
            level.addFreshEntity(boss);
            boss.devPose(DragonBossEntity.MoveMode.GROUND);
            SoldierEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
            knight.snapTo(X0 + 0.5, g[0], Z0 - 12.5, 0.0f, 0.0f);
            knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(4000.0);
            knight.setHealth(4000.0f);
            knight.setPersistenceRequired();
            level.addFreshEntity(knight);
            pair[0] = boss;
            pair[1] = knight;
        }));
        steps.add(Step.until(() -> pair[1] != null && pair[0] != null, 1200));
        steps.add(Step.waitTicks(4));
        steps.add(Step.record("dboss", 180, minecraft -> watch(minecraft, pair[0], pair[1], 9.0)));
    }
}
