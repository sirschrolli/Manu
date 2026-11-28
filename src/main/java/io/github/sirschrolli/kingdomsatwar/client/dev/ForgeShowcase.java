package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
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
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code forge_boss}): Malleus, der Glutschmied, auf dem Dach einer echten Aschenesse in der Unterwelt – er
 * schmiedet am Amboss, erwacht, nimmt den Glutspeer, brüllt; dann seine Angriffe gegen einen (zähen) Ritter, das Anfachen
 * der Esse, sein Tod, der erkaltete Amboss und der Glutspeer am Rüstungsständer.
 */
final class ForgeShowcase {
    private static final String STAGE_TAG = "kaw_forge_stage";

    private ForgeShowcase() {}

    /** Der Turm (Ursprung, Drehung) und die Ids von Schmied und Ritter. */
    private static final class Stage {
        SitePiece tower;
        BlockPos anvil;
        int smith = -1;
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
        // das Dach: der Schmied kniet am Amboss und schmiedet
        steps.add(at(stage, -7.0, 6.5, 13.5, 0.0, 1.5, 7.0));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (stage.anvil != null && level.getBlockEntity(stage.anvil) instanceof GlutAnvilBlockEntity forge) {
                GlutschmiedEntity smith = forge.summon(level);
                if (smith != null) {
                    stage.smith = smith.getId();
                }
            }
            for (Entity e : level.getEntitiesOfClass(Mob.class, new net.minecraft.world.phys.AABB(stage.anvil).inflate(24, 12, 24),
                    m -> !(m instanceof GlutschmiedEntity))) {
                e.discard();
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("forge_roof"));
        steps.add(at(stage, 0.0, 3.4, 3.4, 0.0, 2.0, 8.0));
        steps.add(Step.waitTicks(GlutschmiedEntity.FORGE_BEAT - 1));
        steps.add(Step.screenshot("forge_smith_working"));
        steps.add(at(stage, 4.2, 4.6, 12.0, 0.0, 1.6, 7.0));
        steps.add(Step.waitTicks(GlutschmiedEntity.FORGE_BEAT - 2));
        steps.add(Step.screenshot("forge_smith_working_side"));
        steps.add(at(stage, 1.6, 3.2, 4.4, 0.0, 1.0, 6.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("forge_anvil_spear"));
        // er erwacht: ein Ritter tritt heran
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(stage, 5, 1, 3), 180.0f);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 1_000_000, 0, false, false));
            knight.setSilent(true);
            stage.knight = knight.getId();
            if (level.getBlockEntity(stage.anvil) instanceof GlutAnvilBlockEntity forge) {
                forge.awaken(level);
            }
            if (level.getEntity(stage.smith) instanceof GlutschmiedEntity smith) {
                smith.setTarget(knight);
            }
        }));
        steps.add(at(stage, -1.5, 3.0, 3.6, 0.0, 2.4, 8.0));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("forge_awaken_rise"));
        steps.add(at(stage, 2.6, 2.2, 4.6, 0.0, 1.4, 6.8));
        steps.add(Step.waitTicks(14));
        steps.add(Step.screenshot("forge_awaken_spear"));
        steps.add(at(stage, 3.0, 3.0, 3.6, 0.0, 3.2, 8.0));
        steps.add(Step.waitTicks(14));
        steps.add(Step.screenshot("forge_awaken_roar"));
        steps.add(Step.waitTicks(30));
        // mit Bossleiste
        steps.add(Step.hud(true));
        steps.add(at(stage, -2.5, 2.4, 3.6, 0.0, 2.8, 8.0));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("forge_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe auf dem freien Teil des Dachs (westlich des Schlots): beide an ihre Plätze, dann der Angriff
        // {Angriff, Ticks bis zum Bild, Name, Blickwinkel zur Linie Schmied–Ritter (Grad), Abstand, Höhe}
        for (String[] shot : new String[][] {{"THRUST", "9", "forge_thrust", "-82", "5.6", "0.5"}, {"SLAM", "20", "forge_slam", "-62", "7.6", "3.6"},
                {"SPARKS", "24", "forge_sparks", "-72", "5.6", "0.7"}, {"GLOBS", "18", "forge_globs", "-105", "8.0", "1.4"},
                {"CALL", "18", "forge_call", "-62", "6.4", "1.0"}, {"CHAIN", "19", "forge_chain", "-75", "6.5", "1.6"},
                {"MOLTEN", "27", "forge_molten", "-128", "10.0", "5.5"}, {"STOKE", "22", "forge_stoke", "-62", "6.8", "2.2"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(Step.server(any -> {
                ServerLevel level = nether(any);
                if (level.getEntity(stage.smith) instanceof GlutschmiedEntity smith && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                    Vec3 s = local(stage, -6, 1, 8);
                    Vec3 k = shot[0].equals("GLOBS") ? local(stage, -6, 1, -6) : local(stage, -6, 1, shot[0].equals("THRUST") ? 4 : 3);
                    smith.teleportTo(s.x, s.y, s.z);
                    knight.teleportTo(k.x, k.y, k.z);
                    knight.setDeltaMovement(Vec3.ZERO);
                    knight.setHealth(knight.getMaxHealth());
                    knight.clearFire();
                    smith.setTarget(knight);
                    smith.lookAt(knight, 360.0f, 360.0f);
                    smith.setYBodyRot(smith.getYRot());
                    smith.setYHeadRot(smith.getYRot());
                    smith.forceAttack(GlutschmiedEntity.Attack.valueOf(shot[0]));
                }
            }));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(45));
            if (shot[0].equals("CALL")) {
                steps.add(Step.server(any -> {
                    ServerLevel level = nether(any);
                    for (var e : level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.creature.nether.Brandgebein.class,
                            new net.minecraft.world.phys.AABB(stage.anvil).inflate(24, 12, 24))) {
                        e.discard();
                    }
                }));
            }
        }
        // weißglühend, aus der Nähe
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getEntity(stage.smith) instanceof GlutschmiedEntity smith) {
                smith.forceStoked();
                smith.forceAttack(GlutschmiedEntity.Attack.NONE);
                Vec3 s = local(stage, -6, 1, 8);
                smith.teleportTo(s.x, s.y, s.z);
                if (level.getEntity(stage.knight) instanceof LivingEntity knight) {
                    Vec3 k = local(stage, -6, 1, 3);
                    knight.teleportTo(k.x, k.y, k.z);
                    smith.setTarget(knight);
                }
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(follow(stage, -72.0, 4.8, 0.7));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("forge_stoked_closeup"));
        // sein Ende (der Ritter tritt beiseite)
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getEntity(stage.knight) instanceof LivingEntity knight) {
                Vec3 k = local(stage, -9, 1, -2);
                knight.teleportTo(k.x, k.y, k.z);
            }
            if (level.getEntity(stage.smith) instanceof GlutschmiedEntity smith) {
                smith.setHealth(1.0f);
                smith.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(Step.waitTicks(24));
        steps.add(follow(stage, -58.0, 5.0, 0.2));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("forge_death"));
        steps.add(Step.waitTicks(GlutschmiedEntity.DEATH_TICKS));
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            if (level.getEntity(stage.knight) instanceof Entity knight) {
                knight.discard();
            }
        }));
        steps.add(at(stage, 1.6, 3.2, 4.4, 0.0, 1.0, 6.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("forge_anvil_cold"));
        // der Glutspeer am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = nether(any);
            Vec3 p = local(stage, -5, 1, 8);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            float yaw = stage.tower.rotation().rotate(net.minecraft.core.Direction.EAST).toYRot();
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ForgeBlocks.GLUT_SPEAR.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, -3.7, 1.7, 9.2, -5.0, 1.9, 8.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("forge_spear_stand"));
    }

    /**
     * Kamera beim Schmied: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Schmied → Ritter gedreht,
     * {@code height} über seiner Brust; Blick auf ihn (etwas zum Ritter hin). Auf dem Server berechnet (dort stehen beide
     * schon an ihren neuen Plätzen) und der Zuschauer dorthin versetzt.
     */
    private static Step follow(Stage stage, double angle, double distance, double height) {
        return Step.server(any -> {
            ServerLevel level = nether(any);
            if (level == null || !(level.getEntity(stage.smith) instanceof Entity smith) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = smith.position().add(0.0, 2.2, 0.0);
            Vec3 toward = level.getEntity(stage.knight) instanceof Entity knight ? knight.position().subtract(smith.position())
                    .multiply(1.0, 0.0, 1.0) : new Vec3(0.0, 0.0, -1.0);
            if (toward.lengthSqr() < 1.0e-3) {
                toward = new Vec3(0.0, 0.0, -1.0);
            }
            Vec3 dir = toward.normalize().yRot((float) Math.toRadians(angle));
            Vec3 focus = c.add(toward.normalize().scale(Math.min(1.5, toward.length() * 0.25)));
            Vec3 eye = c.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Sucht die nächste Aschenesse, lässt ihre Chunks entstehen und merkt sich Turm und Amboss. */
    private static void locate(ServerLevel level, Stage stage) {
        if (level == null) {
            stage.state = -1;
            return;
        }
        var holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(SiteKind.ASH_FORGE));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), new BlockPos(0, 64, 0), 300,
                false);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] keine Aschenesse gefunden");
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
            if (piece instanceof SitePiece site && SiteSamples.isAshForgeTower(site)) {
                stage.tower = site;
            }
        }
        if (stage.tower == null) {
            stage.state = -1;
            return;
        }
        stage.anvil = SiteSamples.ashForgeAnvil(stage.tower);
        int cx = stage.anvil.getX() >> 4;
        int cz = stage.anvil.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Aschenesse bei {}, Amboss bei {} ({})", stage.tower.origin(), stage.anvil,
                level.getBlockState(stage.anvil));
        stage.state = 1;
    }

    /** Ein Punkt auf dem Dach (Rahmen des Turms, y relativ zum Dachboden + 46) in Weltkoordinaten (Blockmitte). */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        BlockPos base = SiteSamples.ashForgeRoof(stage.tower, 0, 46, 0);
        double[] r = turn(x, z, stage.tower.rotation());
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

    /** Kamera am Punkt (dx, dy, dz) des Dachs (Augenhöhe), Blick auf (tx, ty, tz) – in der Unterwelt. */
    private static Step at(Stage stage, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (stage.tower == null) {
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
