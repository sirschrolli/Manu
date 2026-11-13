package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.FlameSanctuary;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code ignara_boss}, seit 1.2): Ignara, die Flammenpriesterin, in einem echten Flammenheiligtum im Lavameer –
 * das Heiligtum von oben, sie betet vor dem Altar, erwacht; dann ihre Angriffe gegen einen (zähen) Ritter, die Phönixgestalt
 * und der Sturzflug, die Bossleiste, ihr Tod, der erloschene Altar und die Flammenpeitsche am Rüstungsständer.
 */
final class IgnaraShowcase {
    private static final String STAGE_TAG = "kaw_ignara_stage";

    private IgnaraShowcase() {}

    private static final class Stage {
        SitePiece sanctum;
        BlockPos altar;
        int priestess = -1;
        int knight = -1;
        int state;
    }

    private static ServerLevel nether(ServerLevel any) {
        return any.getServer().getLevel(Level.NETHER);
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
        steps.add(Step.server(any -> locate(nether(any), stage)));
        steps.add(Step.until(() -> stage.state != 0, 9000));
        // das Heiligtum von oben und von der Seite
        steps.add(at(stage, 18.0, 16.0, 24.0, 0.0, -1.0, -1.0));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(stage.altar).inflate(40, 20, 40), m -> !(m instanceof IgnaraEntity))) {
                e.discard();
            }
            if (level.getBlockEntity(stage.altar) instanceof FlameAltarBlockEntity altar) {
                IgnaraEntity priestess = altar.priestess(level);
                if (priestess == null) {
                    priestess = altar.spawn(level, false);
                }
                if (priestess != null) {
                    stage.priestess = priestess.getId();
                }
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("ignara_sanctuary"));
        steps.add(at(stage, -22.0, 6.0, 16.0, 0.0, 0.0, -2.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("ignara_sanctuary_side"));
        // sie betet
        steps.add(at(stage, 3.6, 2.6, 1.6, 0.0, 2.2, -4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("ignara_pray"));
        steps.add(at(stage, -3.2, 2.2, -0.8, 0.0, 1.8, -5.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("ignara_altar"));
        // sie erwacht
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getBlockEntity(stage.altar) instanceof FlameAltarBlockEntity altar) {
                altar.awaken(level);
            }
            Vec3 k = local(stage, 0.0, 1.0, 6.0);
            LivingEntity knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), k, 180.0f);
            knight.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000.0);
            knight.setHealth(1000.0f);
            stage.knight = knight.getId();
        }));
        steps.add(at(stage, 0.0, 2.6, 4.2, 0.0, 2.6, -3.0));
        steps.add(Step.waitTicks(36));
        steps.add(Step.screenshot("ignara_awaken"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("ignara_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe: beide an ihre Plätze, dann der Angriff
        // {Angriff, Ticks bis zum Bild, Name, Blickwinkel zur Linie Priesterin–Ritter (Grad), Abstand, Höhe}
        for (String[] shot : new String[][] {{"LASH", "13", "ignara_lash", "-80", "6.4", "0.6"},
                {"SNARE", "24", "ignara_snare", "-70", "8.5", "1.4"}, {"WAVE", "26", "ignara_wave", "-60", "9.0", "4.0"},
                {"PILLARS", "33", "ignara_pillars", "-110", "9.5", "3.0"}, {"OPAL_RAIN", "40", "ignara_rain", "-75", "9.0", "2.4"},
                {"CALL", "22", "ignara_call", "-60", "7.6", "1.6"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(Step.server(any -> {
                ServerLevel level = nether(any);
                if (level.getEntity(stage.priestess) instanceof IgnaraEntity ignara && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                    Vec3 p = local(stage, 0.0, 1.0 + IgnaraEntity.HOVER, 1.0);
                    Vec3 k = local(stage, 0.0, 1.0, shot[0].equals("SNARE") || shot[0].equals("OPAL_RAIN") ? 9.0 : 6.0);
                    ignara.teleportTo(p.x, p.y, p.z);
                    knight.teleportTo(k.x, k.y, k.z);
                    knight.setDeltaMovement(Vec3.ZERO);
                    knight.setHealth(knight.getMaxHealth());
                    knight.clearFire();
                    ignara.setTarget(knight);
                    ignara.lookAt(knight, 360.0f, 360.0f);
                    ignara.setYBodyRot(ignara.getYRot());
                    ignara.setYHeadRot(ignara.getYRot());
                    ignara.forceAttack(IgnaraEntity.Attack.valueOf(shot[0]));
                }
            }));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(50));
            steps.add(Step.server(any -> {
                ServerLevel level = nether(any);
                for (var e : level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Glutgeist.class,
                        new AABB(stage.altar).inflate(30, 16, 30))) {
                    e.discard();
                }
            }));
        }
        // die Phönixgestalt und der Sturzflug
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getEntity(stage.priestess) instanceof IgnaraEntity ignara && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                Vec3 p = local(stage, 0.0, 1.0 + IgnaraEntity.HOVER, 1.0);
                ignara.teleportTo(p.x, p.y, p.z);
                ignara.setTarget(knight);
                ignara.forceAttack(IgnaraEntity.Attack.PHOENIX);
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(follow(stage, -50.0, 9.0, 1.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("ignara_phoenix"));
        steps.add(Step.waitTicks(50));
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getEntity(stage.priestess) instanceof IgnaraEntity ignara && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                Vec3 k = local(stage, 0.0, 1.0, 8.0);
                knight.teleportTo(k.x, k.y, k.z);
                ignara.setTarget(knight);
                ignara.forceAttack(IgnaraEntity.Attack.DIVE);
            }
        }));
        steps.add(Step.waitTicks(15));
        steps.add(follow(stage, -95.0, 10.0, 0.5));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("ignara_dive"));
        steps.add(Step.waitTicks(40));
        // ihr Tod
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getEntity(stage.knight) instanceof Entity knight) {
                knight.discard();
            }
            if (level.getEntity(stage.priestess) instanceof IgnaraEntity ignara) {
                Vec3 p = local(stage, 0.0, 1.0 + IgnaraEntity.HOVER, 0.0);
                ignara.teleportTo(p.x, p.y, p.z);
                ignara.forceAttack(IgnaraEntity.Attack.NONE);
                ignara.setInvulnerableTime(0);
                ignara.setHealth(1.0f);
                ignara.hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(at(stage, 4.5, 2.6, 5.5, 0.0, 2.6, 0.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("ignara_death"));
        steps.add(Step.waitTicks(IgnaraEntity.DEATH_TICKS));
        steps.add(at(stage, -3.2, 2.2, -0.8, 0.0, 1.8, -5.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("ignara_altar_out"));
        // die Flammenpeitsche am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            Vec3 at = local(stage, 2.0, 0.0, 3.0);
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            float yaw = stage.sanctum.rotation().rotate(net.minecraft.core.Direction.SOUTH).toYRot();
            stand.setShowArms(true);
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(FlameSanctuary.FLAME_WHIP.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 2.6, 1.9, 6.0, 2.0, 1.2, 3.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("ignara_whip_stand"));
    }

    /** Kamera bei der Priesterin: Abstand, Drehung aus der Linie Priesterin → Ritter (Grad), Höhe über ihrer Brust. */
    private static Step follow(Stage stage, double angle, double distance, double height) {
        return Step.server(any -> {
            ServerLevel level = nether(any);
            if (level == null || !(level.getEntity(stage.priestess) instanceof Entity priestess) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = priestess.position().add(0.0, 2.0, 0.0);
            Vec3 toward = level.getEntity(stage.knight) instanceof Entity knight ? knight.position().subtract(priestess.position())
                    .multiply(1.0, 0.0, 1.0) : new Vec3(0.0, 0.0, 1.0);
            if (toward.lengthSqr() < 1.0e-3) {
                toward = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 dir = toward.normalize().yRot((float) Math.toRadians(angle));
            Vec3 focus = c.add(toward.normalize().scale(Math.min(2.5, toward.length() * 0.35)));
            Vec3 eye = c.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Sucht das nächste Flammenheiligtum, lässt seine Chunks entstehen und merkt sich Heiligtum und Altar. */
    private static void locate(ServerLevel level, Stage stage) {
        if (level == null) {
            stage.state = -1;
            return;
        }
        var holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(SiteKind.FLAME_SANCTUARY));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), new BlockPos(0, 64, 0), 400,
                false);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] kein Flammenheiligtum gefunden");
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
            if (piece instanceof SitePiece site && site.kind() == SiteKind.FLAME_SANCTUARY && site.part() == 0) {
                stage.sanctum = site;
            }
        }
        if (stage.sanctum == null) {
            stage.state = -1;
            return;
        }
        stage.altar = SiteSamples.flameSanctumAltar(stage.sanctum);
        int cx = stage.altar.getX() >> 4;
        int cz = stage.altar.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Flammenheiligtum bei {}, Altar bei {} ({})", stage.sanctum.origin(), stage.altar,
                level.getBlockState(stage.altar));
        stage.state = 1;
    }

    /** Ein Punkt im Rahmen des Heiligtums (Mitte der Plattform bei 0, 0, 0; vorn ist +z) in Weltkoordinaten (Blockmitte). */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        BlockPos base = stage.sanctum.origin();
        double[] r = turn(x, z, stage.sanctum.rotation());
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

    /** Kamera am Punkt (dx, dy, dz) des Heiligtums (Augenhöhe), Blick auf (tx, ty, tz) – in der Unterwelt. */
    private static Step at(Stage stage, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (stage.sanctum == null) {
                return;
            }
            Vec3 eye = local(stage, dx, dy, dz);
            Vec3 target = local(stage, tx, ty, tz);
            Vec3 look = target.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in minecraft:the_nether run tp @s %.2f %.2f %.2f %.1f %.1f",
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
