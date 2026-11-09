package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.end.AsteriaEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.StarBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code star_boss}): Asteria, die Sternenreiterin, über dem Sternenhof einer echten Sternwarte im End – der
 * Sternstein, ihr Herabstoßen in der Lichtsäule, die Bossleiste, ihre Angriffe gegen einen (zähen) Ritter, die Supernova,
 * ihr Strahlen, ihr Tod, der erloschene Stein und Sternenlanze und Sternenschwingen am Rüstungsständer.
 */
final class StarShowcase {
    private static final String STAGE_TAG = "kaw_star_stage";

    private StarShowcase() {}

    /** Die Sternwarte, der Sternstein und die Ids von Asteria und Ritter. */
    private static final class Stage {
        SitePiece watch;
        BlockPos stone;
        int rider = -1;
        int knight = -1;
        int state;
    }

    private static ServerLevel end(ServerLevel any) {
        return any.getServer().getLevel(Level.END);
    }

    static void boss(Deque<Step> steps) {
        Stage stage = new Stage();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(end(any), stage)));
        steps.add(Step.until(() -> stage.state != 0, 9000));
        // der Sternenhof mit dem leuchtenden Sternstein, dahinter der Turm
        steps.add(at(stage, 9.0, 7.5, 18.0, 0.0, 4.0, 0.0));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(stage.stone).inflate(32, 20, 32))) {
                e.discard();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("star_plaza"));
        steps.add(at(stage, 2.2, 3.0, 6.6, 0.0, 2.4, 4.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("star_stone"));
        // ein Ritter tritt heran: sie stößt herab
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(stage, 0, 1, 9), 180.0f);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setSilent(true);
            stage.knight = knight.getId();
            if (level.getBlockEntity(stage.stone) instanceof StarstoneBlockEntity stone) {
                AsteriaEntity asteria = stone.summon(level);
                if (asteria != null) {
                    stage.rider = asteria.getId();
                    asteria.setTarget(knight);
                }
            }
        }));
        steps.add(Step.waitTicks(36));
        steps.add(watch(stage, -2.0, 9.0, 13.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("star_descent"));
        steps.add(Step.waitTicks(10));
        steps.add(watch(stage, 3.0, 6.5, 12.0));
        steps.add(Step.waitTicks(28));
        steps.add(watch(stage, 3.0, 6.5, 12.0));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("star_arrival"));
        steps.add(Step.waitTicks(16));
        // Bildnisse: sie hält still über dem Hof (Blick zur offenen Seite), die Kamera schräg darüber und darunter
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.rider) instanceof AsteriaEntity asteria) {
                asteria.setNoAi(true);
                Vec3 a = local(stage, 0, 6, 3);
                Vec3 face = local(stage, 3, 6, 12).subtract(a);
                float yaw = (float) (Math.atan2(face.z, face.x) * 180.0 / Math.PI) - 90.0f;
                asteria.snapTo(a.x, a.y, a.z, yaw, 0.0f);
                asteria.setYBodyRot(yaw);
                asteria.setYHeadRot(yaw);
                asteria.setDeltaMovement(Vec3.ZERO);
            }
        }));
        steps.add(at(stage, 5.5, 10.5, 12.5, 0.0, 7.0, 3.0));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("star_portrait"));
        steps.add(at(stage, -2.5, 4.0, 9.5, 0.0, 7.6, 3.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("star_portrait_low"));
        steps.add(at(stage, 2.8, 8.4, 6.8, 0.0, 7.8, 3.2));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("star_portrait_rider"));
        steps.add(Step.server(any -> {
            if (end(any).getEntity(stage.rider) instanceof AsteriaEntity asteria) {
                asteria.setNoAi(false);
            }
        }));
        // mit Bossleiste
        steps.add(Step.hud(true));
        steps.add(follow(stage, -30.0, 12.0, 2.0, 0.3));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("star_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe: {Angriff, Ticks bis zum Bild, Name, Blickwinkel zur Linie Asteria–Ritter (Grad), Abstand, Höhe, Blick zum Ritter 0..1}
        for (String[] shot : new String[][] {{"TJOST", "32", "star_tjost", "-62", "10.0", "3.5", "0.0"},
                {"STARFALL", "40", "star_starfall", "-100", "13.0", "3.0", "0.6"},
                {"CONSTELLATION", "38", "star_constellation", "-140", "12.0", "9.0", "0.8"},
                {"COMET", "22", "star_comet", "-105", "9.0", "2.5", "0.4"},
                {"WINGBEAT", "17", "star_wingbeat", "-35", "8.0", "2.5", "0.0"},
                {"BLACK_HOLE", "46", "star_black_hole", "-100", "13.0", "2.0", "0.7"},
                {"BLACK_HOLE", "78", "star_black_hole_collapse", "-100", "13.0", "3.0", "0.7"},
                {"CRESCENT", "21", "star_crescent", "-75", "9.0", "1.4", "0.5"},
                {"SUPERNOVA", "52", "star_supernova", "0", "0", "0", "0"},
                {"CRESCENT", "21", "star_crescent_radiant", "-60", "11.0", "4.0", "0.5"}}) {
            if (!Step.wanted(shot[2]) && !shot[0].equals("SUPERNOVA")) {
                continue;
            }
            steps.add(Step.server(any -> {
                ServerLevel level = end(any);
                if (level.getEntity(stage.rider) instanceof AsteriaEntity asteria && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                    Vec3 k = local(stage, 0, 1, 9);
                    knight.teleportTo(k.x, k.y, k.z);
                    knight.setDeltaMovement(Vec3.ZERO);
                    knight.removeAllEffects();
                    knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
                    knight.setHealth(knight.getMaxHealth());
                    Vec3 a = shot[0].equals("WINGBEAT") ? local(stage, 0, 4.5, 3) : local(stage, 0, 8, -1);
                    asteria.teleportTo(a.x, a.y, a.z);
                    asteria.setDeltaMovement(Vec3.ZERO);
                    asteria.setTarget(knight);
                    asteria.lookAt(knight, 360.0f, 360.0f);
                    asteria.setYBodyRot(asteria.getYRot());
                    asteria.setYHeadRot(asteria.getYRot());
                    if (shot[0].equals("SUPERNOVA")) {
                        asteria.setHealth(asteria.getMaxHealth() * 0.6f);
                    }
                    asteria.forceAttack(AsteriaEntity.Attack.valueOf(shot[0]));
                }
            }));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            // die Supernova von vorn unten (sie steigt über den Sternstein), sonst neben ihr
            steps.add(shot[0].equals("SUPERNOVA") ? watch(stage, 3.0, 12.0, 11.5) : follow(stage, Double.parseDouble(shot[3]),
                    Double.parseDouble(shot[4]), Double.parseDouble(shot[5]), Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(shot[0].equals("SUPERNOVA") ? 60 : 45));
        }
        // strahlend, aus der Nähe
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.rider) instanceof AsteriaEntity asteria) {
                asteria.forceRadiant();
                asteria.forceAttack(AsteriaEntity.Attack.NONE);
                asteria.setNoAi(true);
                Vec3 a = local(stage, 0, 6, 4);
                Vec3 face = local(stage, -4, 6, 12).subtract(a);
                float yaw = (float) (Math.atan2(face.z, face.x) * 180.0 / Math.PI) - 90.0f;
                asteria.snapTo(a.x, a.y, a.z, yaw, 0.0f);
                asteria.setYBodyRot(yaw);
                asteria.setYHeadRot(yaw);
                asteria.setDeltaMovement(Vec3.ZERO);
            }
        }));
        steps.add(at(stage, -5.0, 9.5, 11.5, 0.0, 7.0, 4.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("star_radiant"));
        // ihr Ende (der Ritter tritt beiseite)
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.knight) instanceof LivingEntity knight) {
                Vec3 k = local(stage, 8, 1, 10);
                knight.teleportTo(k.x, k.y, k.z);
            }
            if (level.getEntity(stage.rider) instanceof AsteriaEntity asteria) {
                asteria.setNoAi(false);
                Vec3 a = local(stage, 0, 9, 4);
                asteria.teleportTo(a.x, a.y, a.z);
                asteria.setHealth(1.0f);
                asteria.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(Step.waitTicks(34));
        steps.add(watch(stage, 4.0, 8.5, 10.5));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("star_death"));
        steps.add(Step.waitTicks(AsteriaEntity.DEATH_TICKS));
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.knight) instanceof Entity knight) {
                knight.discard();
            }
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(stage.stone).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(at(stage, 2.2, 3.0, 6.6, 0.0, 2.4, 4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("star_stone_dim"));
        // Sternenlanze und Sternenschwingen am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            Vec3 p = local(stage, 3, 1, 7);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            float yaw = stage.watch.rotation().rotate(net.minecraft.core.Direction.SOUTH).toYRot() + 200.0f;
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(StarBlocks.STAR_LANCE.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(StarBlocks.STAR_WINGS.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 4.8, 2.2, 10.4, 3.0, 2.0, 7.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("star_lance_stand"));
        steps.add(at(stage, 1.4, 2.4, 4.6, 3.0, 1.9, 7.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("star_wings_stand"));
    }

    /**
     * Kamera bei Asteria: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Asteria → Ritter gedreht,
     * {@code height} über ihr; Blick auf sie ({@code toward} 0) oder auf den Ritter ({@code toward} 1). Auf dem Server
     * berechnet (dort stehen beide an ihren aktuellen Plätzen) und der Zuschauer dorthin versetzt.
     */
    private static Step follow(Stage stage, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = end(any);
            if (level == null || !(level.getEntity(stage.rider) instanceof Entity rider) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            // sie fliegt weiter, bis das Bild entsteht (vier Ticks später): dorthin zielen, wo sie dann ist
            Vec3 c = rider.position().add(rider.getDeltaMovement().scale(4.0)).add(0.0, 1.4, 0.0);
            Vec3 k = level.getEntity(stage.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(0.0, 0.0, -1.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, -1.0);
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

    /** Kamera am festen Punkt (dx, dy, dz) der Warte (Augenhöhe), Blick auf Asteria (dort, wo sie gerade fliegt). */
    private static Step watch(Stage stage, double dx, double dy, double dz) {
        return Step.server(any -> {
            ServerLevel level = end(any);
            if (level == null || stage.watch == null || !(level.getEntity(stage.rider) instanceof Entity rider)
                    || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 eye = local(stage, dx, dy, dz);
            Vec3 look = rider.position().add(0.0, 1.3, 0.0).subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Sucht die nächste Sternwarte im End, lässt ihre Chunks entstehen und merkt sich Warte und Sternstein. */
    private static void locate(ServerLevel level, Stage stage) {
        if (level == null) {
            stage.state = -1;
            return;
        }
        var holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(SiteKind.STARWATCH));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), new BlockPos(1200, 64, 0), 300,
                false);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] keine Sternwarte gefunden");
            stage.state = -1;
            return;
        }
        ChunkPos chunk = ChunkPos.containing(found.getFirst());
        StructureStart start = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
        if (start == null || !start.isValid()) {
            stage.state = -1;
            return;
        }
        for (var piece : start.getPieces()) {
            if (piece instanceof SitePiece site && site.kind() == SiteKind.STARWATCH) {
                stage.watch = site;
            }
        }
        if (stage.watch == null) {
            stage.state = -1;
            return;
        }
        stage.stone = SiteSamples.starwatchStone(stage.watch);
        int cx = stage.stone.getX() >> 4;
        int cz = stage.stone.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Sternwarte bei {}, Sternstein bei {} ({})", stage.watch.origin(), stage.stone,
                level.getBlockState(stage.stone));
        stage.state = 1;
    }

    /** Ein Punkt der Sternwarte (Rahmen der Warte, y relativ zu ihrem Boden) in Weltkoordinaten (Blockmitte). */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        BlockPos base = stage.watch.origin();
        double[] r = turn(x, z, stage.watch.rotation());
        return new Vec3(base.getX() + r[0] + 0.5, base.getY() + y, base.getZ() + r[1] + 0.5);
    }

    private static double[] turn(double x, double z, net.minecraft.world.level.block.Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new double[] {-z, x};
            case CLOCKWISE_180 -> new double[] {-x, -z};
            case COUNTERCLOCKWISE_90 -> new double[] {z, -x};
            default -> new double[] {x, z};
        };
    }

    /** Kamera am Punkt (dx, dy, dz) der Warte (Augenhöhe), Blick auf (tx, ty, tz) – im End. */
    private static Step at(Stage stage, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (stage.watch == null) {
                return;
            }
            Vec3 eye = local(stage, dx, dy, dz);
            Vec3 target = local(stage, tx, ty, tz);
            Vec3 look = target.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in minecraft:the_end run tp @s %.2f %.2f %.2f %.1f %.1f",
                    eye.x, eye.y - 1.62, eye.z, yaw, pitch));
        });
    }

    private static <T extends Mob> T creature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, Vec3 at, float yaw) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(at.x, at.y, at.z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        return mob;
    }
}
