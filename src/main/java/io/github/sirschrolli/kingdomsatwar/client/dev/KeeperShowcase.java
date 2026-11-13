package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.KeeperStoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.fae.KeeperBoulderEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.fae.Mondsteinbruch;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.FaeGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code keeper_boss}): der alte Mondsteinbruch im Feenreich – von oben, der Kran, die Hütte des Vorarbeiters,
 * Gerüst und Rampen, eine Erzader – und der Steinhüter: schlafend an seinem Platz, sein Erwachen, die Bossleiste, alle Angriffe
 * gegen einen (zähen) Ritter, der Kristallpanzer, wie er zerspringt, sein Tod und der erloschene Stein; dazu Mondsteinrüstung
 * und Hüterhammer am Rüstungsständer.
 */
final class KeeperShowcase {
    private static final String TAG = "kaw_keeper_show";

    private KeeperShowcase() {}

    /** Der Hüterstein (Weltkoordinaten) und Mitte (Fuß des Steins), die Ids von Hüter und Ritter. */
    private static final class Quarry {
        final double[] c = new double[3];
        final double[] cam = new double[3];
        BlockPos stone;
        BlockPos ore;
        int keeper = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel fae(ServerLevel any) {
        return any.getServer().getLevel(FaeGate.FAE_REALM);
    }

    static void boss(Deque<Step> steps) {
        Quarry q = new Quarry();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(fae(any), q)));
        steps.add(Step.until(() -> q.ready, 9000));
        // der Bruch von oben (vom Südrand)
        steps.add(at(q, 0.0, 26.0, 30.0, 0.0, 2.0, -3.0));
        steps.add(FaeShowcase.arrived(q.cam));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(q.stone).inflate(48, 30, 48))) {
                e.discard();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("quarry_overview"));
        steps.add(at(q, -22.0, 20.0, 12.0, 2.0, 4.0, -4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("quarry_overview_west"));
        steps.add(at(q, 13.0, 16.5, -3.0, 0.0, 17.0, -15.0));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("quarry_crane"));
        steps.add(at(q, -6.5, 16.0, 7.0, -15.0, 13.2, 15.0));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("quarry_hut"));
        steps.add(at(q, -14.6, 13.7, 14.0, -16.0, 12.4, 16.2));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quarry_hut_inside"));
        steps.add(at(q, 1.5, 3.0, 3.5, 0.5, 6.0, 12.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quarry_scaffold"));
        steps.add(at(q, 6.0, 2.6, 7.0, -3.0, 6.5, -10.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quarry_inside"));
        steps.add(oreView(q));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quarry_ore"));

        // der Hüter kniet an seinem Platz
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getBlockEntity(q.stone) instanceof KeeperStoneBlockEntity stone) {
                stone.reset(level);
                StoneKeeperEntity keeper = stone.spawn(level, false);
                if (keeper != null) {
                    q.keeper = keeper.getId();
                }
            }
        }));
        steps.add(at(q, 4.0, 2.4, -0.8, 0.0, 1.6, -4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("keeper_sleeping"));
        steps.add(at(q, -1.6, 3.4, 1.6, 0.0, 1.0, -2.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("keeper_stone"));
        // er erwacht
        steps.add(Step.server(any -> {
            if (fae(any).getEntity(q.keeper) instanceof StoneKeeperEntity keeper) {
                keeper.awaken();
            }
        }));
        steps.add(at(q, 2.0, 3.4, 1.8, 0.0, 2.8, -4.0));
        steps.add(attackAt(q, StoneKeeperEntity.Attack.AWAKEN, 26));
        steps.add(Step.screenshot("keeper_awaken"));
        steps.add(attackAt(q, StoneKeeperEntity.Attack.AWAKEN, 44));
        steps.add(Step.screenshot("keeper_roar"));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.getEntity(q.keeper) instanceof StoneKeeperEntity keeper
                    && keeper.currentAttack() != StoneKeeperEntity.Attack.AWAKEN;
        }, 200));
        // ein Ritter als Gegner
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(q, -0.5, 0.0, 2.0), 90.0f);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setSilent(true);
            q.knight = knight.getId();
        }));
        steps.add(stage(q, 5.5, false));
        steps.add(Step.hud(true));
        steps.add(follow(q, 40.0, 7.0, 2.0, 0.2));
        steps.add(Step.waitTicks(80));
        steps.add(follow(q, 40.0, 7.0, 2.0, 0.2));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("keeper_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe: {Angriff, Tick, Name, Winkel, Abstand, Höhe, Blick zum Ritter, Abstand des Ritters, Zorn}
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : new String[][] {
                {"SMASH", "12", "keeper_smash_windup", "60", "7.5", "2.0", "0.3", "3.2", ""},
                {"SMASH", "18", "keeper_smash", "60", "7.5", "2.0", "0.4", "3.2", ""},
                {"STOMP", "20", "keeper_stomp", "75", "9.0", "4.5", "0.3", "4.0", ""},
                {"BOULDER", "28", "keeper_boulder_lift", "25", "7.0", "0.4", "0.0", "11.0", ""},
                {"BOULDER", "41", "keeper_boulder_throw", "90", "9.0", "4.0", "0.45", "11.0", ""},
                {"CRYSTALS", "24", "keeper_crystals", "50", "9.0", "4.0", "0.5", "8.0", ""},
                {"BEAM", "48", "keeper_beam", "70", "9.5", "3.5", "0.45", "9.0", "1"},
                {"LEAP", "21", "keeper_leap", "90", "12.0", "3.0", "0.5", "12.0", "1"},
                {"ARMOR", "36", "keeper_armor", "30", "7.0", "2.0", "0.0", "6.0", "1"}}) {
            StoneKeeperEntity.Attack attack = StoneKeeperEntity.Attack.valueOf(shot[0]);
            int tick = Integer.parseInt(shot[1]);
            if (started.add(shot[0])) {
                steps.add(stage(q, Double.parseDouble(shot[7]), !shot[8].isEmpty()));
                steps.add(Step.waitTicks(2));
                steps.add(Step.server(any -> {
                    if (fae(any).getEntity(q.keeper) instanceof StoneKeeperEntity keeper) {
                        keeper.forceAttack(attack);
                    }
                }));
            }
            steps.add(attackAt(q, attack, tick - 3));
            steps.add(follow(q, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]),
                    Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot(shot[2]));
        }
        // im Kristallpanzer, aus der Nähe
        steps.add(Step.waitTicks(20));
        steps.add(pose(q, 0.0, 0.0, -4.0, 4.0, 2.0, 2.0));
        steps.add(at(q, 2.6, 4.4, 1.6, 0.0, 3.0, -4.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("keeper_armored"));
        steps.add(at(q, -3.6, 3.6, -1.2, 0.0, 2.6, -4.0));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("keeper_armored_side"));
        // der Panzer zerspringt, er ist benommen
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getEntity(q.keeper) instanceof StoneKeeperEntity keeper) {
                keeper.setNoAi(false);
                keeper.damageCooldownTime = 0;
                keeper.hurtServer(level, level.damageSources().generic(), StoneKeeperEntity.ARMOR_HEALTH + 5.0f);
            }
        }));
        steps.add(at(q, 2.6, 3.8, 2.0, 0.0, 2.6, -4.0));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("keeper_shatter"));
        steps.add(attackAt(q, StoneKeeperEntity.Attack.STAGGER, 24));
        steps.add(Step.screenshot("keeper_stagger"));
        // besiegt
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getEntity(q.knight) instanceof Entity knight) {
                knight.discard();
            }
            if (level.getEntity(q.keeper) instanceof StoneKeeperEntity keeper) {
                keeper.damageCooldownTime = 0;
                keeper.setHealth(1.0f);
                keeper.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(at(q, 3.0, 3.4, 1.4, 0.0, 2.0, -4.0));
        steps.add(Step.waitTicks(36));
        steps.add(Step.screenshot("keeper_death"));
        steps.add(Step.waitTicks(StoneKeeperEntity.DEATH_TICKS - 36));
        steps.add(Step.screenshot("keeper_crumble"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(any -> {
            for (ItemEntity item : fae(any).getEntitiesOfClass(ItemEntity.class, new AABB(q.stone).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(at(q, -1.6, 3.4, 1.6, 0.0, 1.0, -2.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("keeper_stone_dark"));
        // Mondsteinrüstung und Hüterhammer am Rüstungsständer (am Rand vor der Hütte)
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            Vec3 p = local(q, -10.5, 12.0, 13.5);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            stand.setRightArmPose(new Rotations(-25, 0, 10));
            stand.setYRot(-140.0f);
            stand.setYBodyRot(-140.0f);
            stand.setYHeadRot(-140.0f);
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(FaeGear.MOONSTONE_HELMET.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(FaeGear.MOONSTONE_CHESTPLATE.get()));
            stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(FaeGear.MOONSTONE_LEGGINGS.get()));
            stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(FaeGear.MOONSTONE_BOOTS.get()));
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(FaeGear.KEEPER_MAUL.get()));
            stand.addTag(TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(q, -8.0, 14.4, 10.6, -10.5, 13.2, 13.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("keeper_gear"));
        steps.add(at(q, -9.0, 14.0, 11.8, -10.5, 13.6, 13.5));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("keeper_gear_close"));
        steps.add(Step.server(any -> {
            for (Entity e : fae(any).getEntitiesOfClass(Entity.class, new AABB(q.stone).inflate(48, 40, 48), e -> e.entityTags().contains(TAG)
                    || e instanceof SpellFxEntity || e instanceof KeeperBoulderEntity)) {
                e.discard();
            }
        }));
    }

    /** Sucht den nächsten Mondsteinbruch, lässt seine Chunks entstehen und merkt sich den Hüterstein und eine Erzader. */
    private static void locate(ServerLevel level, Quarry q) {
        if (level == null) {
            q.ready = true;
            return;
        }
        BlockPos center = Mondsteinbruch.nearest(level.getSeed(), 600, 600);
        if (center == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] kein Mondsteinbruch gefunden");
            q.ready = true;
            return;
        }
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, center.getX(), center.getZ());
        BlockPos found = null;
        for (int y = top + 2; y > top - 24 && found == null; y--) {
            BlockPos p = new BlockPos(center.getX(), y, center.getZ());
            if (level.getBlockState(p).is(FaeBlocks.KEEPER_STONE.get())) {
                found = p;
            }
        }
        q.stone = found != null ? found : new BlockPos(center.getX(), top, center.getZ());
        q.c[0] = q.stone.getX() + 0.5;
        q.c[1] = q.stone.getY();
        q.c[2] = q.stone.getZ() + 0.5;
        // eine offene Erzader in der Wand (möglichst nah am Grund, im Osten)
        double best = Double.MAX_VALUE;
        for (BlockPos p : BlockPos.betweenClosed(q.stone.offset(-18, -1, -18), q.stone.offset(18, 10, 18))) {
            if (!level.getBlockState(p).is(FaeBlocks.MOONSTONE_ORE.get())) {
                continue;
            }
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (level.getBlockState(p.relative(d)).isAir() && level.getBlockState(p.relative(d, 2)).isAir()) {
                    double score = p.getY() - q.stone.getY() + Math.abs(p.getX() - q.stone.getX()) * 0.05;
                    if (score < best) {
                        best = score;
                        q.ore = p.immutable();
                    }
                }
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Mondsteinbruch bei {}, Hüterstein bei {} ({}), Erz bei {}", center, q.stone,
                level.getBlockState(q.stone), q.ore);
        q.ready = true;
    }

    /** Kamera vor der gefundenen Erzader (zweieinhalb Blöcke davor, leicht erhöht). */
    private static Step oreView(Quarry q) {
        return Step.once(minecraft -> {
            if (q.ore == null || minecraft.level == null) {
                return;
            }
            Direction open = Direction.NORTH;
            for (Direction d : Direction.Plane.HORIZONTAL) {
                if (minecraft.level.getBlockState(q.ore.relative(d)).isAir() && minecraft.level.getBlockState(q.ore.relative(d, 2)).isAir()) {
                    open = d;
                    break;
                }
            }
            Vec3 face = Vec3.atCenterOf(q.ore);
            Vec3 eye = face.add(open.getStepX() * 2.8, 0.6, open.getStepZ() * 2.8).add(open.getClockWise().getStepX() * 0.8, 0.0,
                    open.getClockWise().getStepZ() * 0.8);
            Vec3 look = face.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:fae_realm run tp @s %.2f %.2f %.2f %.1f %.1f", eye.x, eye.y - 1.62, eye.z, yaw, pitch));
        });
    }

    /**
     * Stellt Hüter und Ritter für den nächsten Angriff auf: der Hüter östlich des Sockels (nach Osten blickend), der Ritter
     * {@code ahead} Blöcke vor ihm; beide geheilt, Reste früherer Angriffe fort.
     */
    private static Step stage(Quarry q, double ahead, boolean enraged) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(q.stone).inflate(40, 30, 40),
                    e -> e instanceof SpellFxEntity || e instanceof KeeperBoulderEntity)) {
                e.discard();
            }
            if (!(level.getEntity(q.keeper) instanceof StoneKeeperEntity keeper) || !(level.getEntity(q.knight) instanceof LivingEntity knight)) {
                return;
            }
            Vec3 k = local(q, -6.0 + ahead, 0.0, 2.0);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.setHealth(knight.getMaxHealth());
            knight.removeAllEffects();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            Vec3 a = local(q, -6.0, 0.0, 2.0);
            keeper.setNoAi(false);
            keeper.snapTo(a.x, a.y, a.z, -90.0f, 0.0f);
            keeper.setYBodyRot(-90.0f);
            keeper.setYHeadRot(-90.0f);
            keeper.setDeltaMovement(Vec3.ZERO);
            keeper.setHealth(keeper.getMaxHealth());
            if (enraged) {
                keeper.forceEnraged();
            }
            keeper.setTarget(knight);
            keeper.forceAttack(StoneKeeperEntity.Attack.NONE);
        });
    }

    /**
     * Wartet, bis der Hüter (im Client) den Angriff {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn schon
     * beendet hat.
     */
    private static Step attackAt(Quarry q, StoneKeeperEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(q.keeper) instanceof StoneKeeperEntity keeper)) {
                return false;
            }
            if (keeper.currentAttack() == attack) {
                seen[0] = true;
                return keeper.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 400);
    }

    /** Er hält still an (x, y, z), zugewandt dem Punkt (fx, fy, fz). */
    private static Step pose(Quarry q, double x, double y, double z, double fx, double fy, double fz) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getEntity(q.keeper) instanceof StoneKeeperEntity keeper) {
                keeper.forceAttack(StoneKeeperEntity.Attack.NONE);
                keeper.setNoAi(true);
                Vec3 a = local(q, x, y, z);
                Vec3 face = local(q, fx, fy, fz).subtract(a);
                float yaw = (float) (Math.atan2(face.z, face.x) * 180.0 / Math.PI) - 90.0f;
                keeper.snapTo(a.x, a.y, a.z, yaw, 0.0f);
                keeper.setYBodyRot(yaw);
                keeper.setYHeadRot(yaw);
                keeper.setDeltaMovement(Vec3.ZERO);
            }
        });
    }

    /**
     * Kamera beim Hüter: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Hüter → Ritter gedreht, {@code height}
     * über seiner Brust; Blick auf ihn ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Quarry q, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level == null || !(level.getEntity(q.keeper) instanceof Entity keeper) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = keeper.position().add(keeper.getDeltaMovement().scale(2.0)).add(0.0, 2.2, 0.0);
            Vec3 k = level.getEntity(q.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(1.0, 0.0, 0.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(1.0, 0.0, 0.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(angle));
            Vec3 eye = focus.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Ein Punkt (relativ zum Fuß des Hütersteins) in Weltkoordinaten. */
    private static Vec3 local(Quarry q, double x, double y, double z) {
        return new Vec3(q.c[0] + x, q.c[1] + y, q.c[2] + z);
    }

    /** Kamera an Mitte + (dx, dy, dz) (Augenhöhe), Blick auf Mitte + (tx, ty, tz) – im Feenreich. */
    private static Step at(Quarry q, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            q.cam[0] = q.c[0] + dx;
            q.cam[1] = q.c[1] + dy - 1.62;
            q.cam[2] = q.c[2] + dz;
            double lx = tx - dx;
            double ly = ty - dy;
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:fae_realm run tp @s %.2f %.2f %.2f %.1f %.1f", q.cam[0], q.cam[1], q.cam[2], yaw, pitch));
        });
    }

    private static <T extends Mob> T creature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, Vec3 at, float yaw) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(TAG);
        mob.snapTo(at.x, at.y, at.z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        return mob;
    }
}
