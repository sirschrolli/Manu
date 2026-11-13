package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.arena.ArenaSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.arena.GladiatorNetEntity;
import io.github.sirschrolli.kingdomsatwar.entity.arena.VentorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ArenaGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Vorführung ({@code ventor_boss}, seit 1.9): Ventor, der Gladiator der Winde, in der Arena der Prüfungen – in der flachen
 * Testwelt in einen Felsblock gebaut (die Arena liegt in ihrer Höhle). Die Arena von den Rängen, das Podest mit den
 * Windsiegeln (drei Käfige bezwungen, dann alle), sein Aufstieg aus dem Spawner der Böen, Titel und Bossleiste; dann seine
 * Angriffe gegen einen (zähen) Ritter im Sand: Böe, Dreizackstoß, Netzwurf, Dreizackwurf, Windkugeln, Windhosen, Sturzflug,
 * der entfesselte Sturm und der Zyklon; sein Tod; zuletzt der Sturmdreizack am Ständer und das Wurfnetz.
 */
final class VentorShowcase {
    private static final String STAGE_TAG = "kaw_ventor_stage";
    private static final int OX = 0;
    private static final int OZ = 1500;
    /** Die Linie der Kämpfe im Sand (z), dort, wo hinter ihnen die Ehrentribüne steht; Ventor steht bei x = {@link #FIGHT_X}. */
    private static final double FIGHT_Z = -5.0;
    private static final double FIGHT_X = -7.5;

    private VentorShowcase() {}

    private static final class Stage {
        BlockPos origin;
        BlockPos seal;
        int ventor = -1;
        int soldier = -1;
        boolean ready;
    }

    static void boss(Deque<Step> steps) {
        Stage stage = new Stage();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(8);
            minecraft.options.gamma().set(1.0);
            ShowcaseRunner.hideHand = true;
        }));
        for (String command : new String[] {"gamerule spawn_monsters false", "gamerule spawn_wardens false", "difficulty normal",
                "gamerule advance_time false", "time set 6000", "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(level -> build(level, stage)));
        steps.add(Step.until(() -> stage.ready, 6000));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // die Arena von den Rängen im Südosten: das Podest mit dem Spawner der Böen, die Käfige in der Brüstung, hinten die Tribüne
        steps.add(at(stage, 9.5, 12.6, 14.5, -0.5, 2.0, -2.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(level -> {
            if (level.getBlockEntity(stage.seal) instanceof ArenaSealBlockEntity seal) {
                seal.scan(level);
                for (int i = 0; i < 3; i++) {
                    seal.conquerForShow(level, i);
                }
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("arena_ventor_uebersicht"));
        // das Podest nah: drei Windsiegel leuchten, drei sind matt
        steps.add(at(stage, 3.3, 5.9, 3.9, 0.0, 4.3, 0.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("arenasiegel_drei"));
        // alle Käfige bezwungen: die Siegel kreisen und leuchten
        steps.add(Step.server(level -> {
            if (level.getBlockEntity(stage.seal) instanceof ArenaSealBlockEntity seal) {
                for (int i = 0; i < seal.cages().size(); i++) {
                    seal.conquerForShow(level, i);
                }
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("arenasiegel_bereit"));
        // er steigt aus dem Spawner der Böen auf (von vorn)
        steps.add(Step.server(level -> {
            LivingEntity soldier = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(stage, 0.5, 1.0, 8.0), 180.0f);
            soldier.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            soldier.setHealth(1000.0f);
            stage.soldier = soldier.getId();
            if (level.getBlockEntity(stage.seal) instanceof ArenaSealBlockEntity seal) {
                VentorEntity ventor = seal.summon(level);
                if (ventor != null) {
                    stage.ventor = ventor.getId();
                    ventor.lookAt(soldier, 360.0f, 360.0f);
                    ventor.setYBodyRot(ventor.getYRot());
                    ventor.setYHeadRot(ventor.getYRot());
                }
            }
        }));
        steps.add(at(stage, 0.5, 6.0, 5.1, 0.0, 5.7, 0.0));
        steps.add(Step.waitTicks(16));
        steps.add(Step.screenshot("ventor_aufstieg"));
        steps.add(Step.waitTicks(26));
        steps.add(Step.screenshot("ventor_arme"));
        steps.add(Step.waitTicks(18));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("ventor_gruss"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("ventor_bossleiste"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(10));
        // seine Angriffe im Sand vor der Ehrentribüne (er im Westen, der Ritter östlich von ihm; die Kamera im Süden)
        // {Angriff, Tick des Bildes, Name, Blickwinkel zur Linie Ventor–Ritter (Grad), Abstand, Höhe, Abstand des Ritters}
        for (String[] shot : new String[][] {{"GUST", "9", "ventor_boee_ausholen", "-62", "6.5", "0.4", "4.5"},
                {"GUST", "14", "ventor_boee", "-70", "7.5", "0.6", "4.5"},
                {"THRUST", "12", "ventor_dreizackstoss", "-75", "6.5", "0.4", "4.2"},
                {"NET", "15", "ventor_netz_schwingen", "-60", "6.5", "0.6", "7.0"},
                {"NET", "25", "ventor_netzwurf", "-80", "8.0", "0.8", "8.0"},
                {"THROW", "14", "ventor_wurf_ausholen", "-58", "6.5", "0.4", "9.0"},
                {"THROW", "21", "ventor_dreizackwurf", "-88", "8.5", "0.8", "10.0"},
                {"CHARGES", "17", "ventor_windkugeln", "-75", "7.5", "0.6", "9.0"},
                {"VORTEX", "18", "ventor_windhosen_ruf", "-50", "6.5", "0.4", "7.0"},
                {"VORTEX", "44", "ventor_windhosen", "-80", "10.0", "3.0", "7.0"},
                {"DIVE", "36", "ventor_sturzflug_kreis", "-80", "10.0", "2.0", "6.0"},
                {"DIVE", "51", "ventor_sturzflug", "-75", "8.0", "0.8", "6.0"},
                {"RAGE", "22", "ventor_sturm_entfesselt", "-60", "7.0", "0.6", "6.0"},
                {"CYCLONE", "30", "ventor_zyklon", "-65", "7.5", "1.0", "5.0"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(calm(stage));
            steps.add(place(stage, shot[0], FIGHT_X, FIGHT_X + Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(shot[0].equals("DIVE") || shot[0].equals("CYCLONE") || shot[0].equals("VORTEX") ? 50 : 34));
            steps.add(Step.server(level -> clear(level, stage)));
        }
        // im Netz gefangen: der Ritter steckt fest
        steps.add(calm(stage));
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.soldier) instanceof LivingEntity soldier && level.getEntity(stage.ventor) instanceof VentorEntity ventor) {
                Vec3 k = local(stage, 2.5, 1.0, 6.0);
                soldier.teleportTo(k.x, k.y, k.z);
                GladiatorNetEntity.snare(level, soldier, 200);
                Vec3 p = local(stage, -2.0, 1.0, 6.0);
                ventor.teleportTo(p.x, p.y, p.z);
                ventor.lookAt(soldier, 360.0f, 360.0f);
                ventor.setYBodyRot(ventor.getYRot());
                ventor.setYHeadRot(ventor.getYRot());
            }
        }));
        steps.add(at(stage, 4.0, 2.6, 9.0, 1.5, 1.6, 6.0));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("ventor_im_netz"));
        steps.add(Step.server(level -> clear(level, stage)));
        // sein Tod: der Wind erlahmt, das Standbild stürzt
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.soldier) instanceof Entity soldier) {
                soldier.discard();
            }
            clear(level, stage);
            if (level.getEntity(stage.ventor) instanceof VentorEntity ventor) {
                Vec3 p = local(stage, -1.0, 1.0, 6.0);
                ventor.teleportTo(p.x, p.y, p.z);
                ventor.setYRot(160.0f);
                ventor.setYBodyRot(160.0f);
                ventor.forceAttack(VentorEntity.Attack.NONE);
                ventor.setInvulnerableTime(0);
                ventor.setHealth(1.0f);
                ventor.hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(at(stage, 2.4, 3.0, 10.5, -1.0, 2.0, 6.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("ventor_tod_sinken"));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("ventor_tod"));
        steps.add(Step.waitTicks(VentorEntity.DEATH_TICKS));
        // der Sturmdreizack am Rüstungsständer (die Beute fort, sie läge sonst im Bild), daneben ein Netz
        steps.add(Step.server(level -> {
            for (var e : level.getEntitiesOfClass(ItemEntity.class, area(stage))) {
                e.discard();
            }
            Vec3 at = local(stage, 1.0, 1.0, 7.0);
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            stand.setShowArms(true);
            // wie ein Standbild: den Dreizack aufrecht vor sich, das Netz hängt in der Linken
            stand.setRightArmPose(new Rotations(-95.0f, -8.0f, 0.0f));
            stand.setLeftArmPose(new Rotations(-12.0f, 0.0f, -14.0f));
            stand.setYRot(-40.0f);
            stand.setYBodyRot(-40.0f);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(ArenaGear.STORM_TRIDENT.get()));
            stand.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(ArenaGear.GLADIATOR_NET.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 1.6, 2.9, 10.4, 1.0, 2.4, 7.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("sturmdreizack_staender"));
        steps.add(Step.quit());
    }

    /**
     * Baut einen Felsblock in die flache Welt und darin die Arena der Prüfungen (sie höhlt ihn aus): die Mitte der Arena bei
     * {@code origin} (man steht auf y + 1), das Arenasiegel zwei Blöcke darüber unter dem Spawner der Böen.
     */
    private static void build(ServerLevel level, Stage stage) {
        long started = System.currentTimeMillis();
        stage.origin = new BlockPos(OX, Showcase.ground + 6, OZ);
        BlockPos o = stage.origin;
        int r = 44;
        for (int cx = (o.getX() - r) >> 4; cx <= (o.getX() + r) >> 4; cx++) {
            for (int cz = (o.getZ() - r) >> 4; cz <= (o.getZ() + r) >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
            }
        }
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int top = o.getY() + 33;
        for (int x = o.getX() - r; x <= o.getX() + r; x++) {
            for (int z = o.getZ() - r; z <= o.getZ() + r; z++) {
                for (int y = Showcase.ground; y <= top; y++) {
                    level.setBlock(p.set(x, y, z), (y == top ? Blocks.GRASS_BLOCK : y > top - 3 ? Blocks.DIRT : Blocks.STONE).defaultBlockState(), 2 | 16);
                }
            }
        }
        SitePiece piece = SiteSamples.trialArena(o, 0x7A1L);
        BoundingBox all = new BoundingBox(o.getX() - 42, o.getY() - 8, o.getZ() - 42, o.getX() + 42, o.getY() + 34, o.getZ() + 42);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), all, ChunkPos.containing(o), o);
        // die Knochen im Sand fort (sie lägen sonst mitten in den Bildern)
        for (BlockPos q : BlockPos.betweenClosed(o.offset(-12, 1, -12), o.offset(12, 1, 12))) {
            if (level.getBlockState(q).is(Blocks.BONE_BLOCK) || level.getBlockState(q).is(Blocks.SKELETON_SKULL)) {
                level.setBlock(q, Blocks.AIR.defaultBlockState(), 2);
            }
        }
        stage.seal = o.offset(0, 2, 0);
        KingdomsAtWar.LOGGER.info("[Showcase] Arena der Prüfungen bei {}, Siegel {} ({}), {} ms", o, stage.seal, level.getBlockState(stage.seal),
                System.currentTimeMillis() - started);
        stage.ready = true;
    }

    private static AABB area(Stage stage) {
        return new AABB(stage.origin).inflate(30, 20, 30);
    }

    /**
     * Stellt Ventor und den Ritter auf ihre Plätze im Sand (Ventor bei {@code vx}, der Ritter bei {@code sx}, beide auf der Linie
     * {@link #FIGHT_Z}) und beginnt den Angriff.
     */
    private static Step place(Stage stage, String attack, double vx, double sx) {
        return Step.server(level -> {
            if (level.getEntity(stage.ventor) instanceof VentorEntity ventor && level.getEntity(stage.soldier) instanceof LivingEntity soldier) {
                Vec3 p = local(stage, vx, 1.0, FIGHT_Z);
                Vec3 k = local(stage, sx, 1.0, FIGHT_Z);
                ventor.setNoGravity(false);
                ventor.teleportTo(p.x, p.y, p.z);
                ventor.setDeltaMovement(Vec3.ZERO);
                ventor.catchTrident();
                soldier.teleportTo(k.x, k.y, k.z);
                soldier.setDeltaMovement(Vec3.ZERO);
                soldier.resetFallDistance();
                soldier.setHealth(soldier.getMaxHealth());
                ventor.setTarget(soldier);
                ventor.lookAt(soldier, 360.0f, 360.0f);
                ventor.setYBodyRot(ventor.getYRot());
                ventor.setYHeadRot(ventor.getYRot());
                ventor.setEnraged(attack.equals("CYCLONE"));
                ventor.forceAttack(VentorEntity.Attack.valueOf(attack));
            }
        });
    }

    /** Beendet seinen Angriff einen Tick vor dem nächsten (sonst sähe der Client nicht, dass derselbe Angriff neu beginnt). */
    private static Step calm(Stage stage) {
        return Step.server(level -> {
            if (level.getEntity(stage.ventor) instanceof VentorEntity ventor) {
                ventor.forceAttack(VentorEntity.Attack.NONE);
            }
        });
    }

    /** Räumt Beute, Geschosse, Windhosen und Netze fort (ein neuer Angriff beginnt ohne sie). */
    private static void clear(ServerLevel level, Stage stage) {
        for (var e : level.getEntitiesOfClass(Entity.class, area(stage), e -> e instanceof ItemEntity || e instanceof Projectile
                || e.getType() == ModEntities.WIND_VORTEX.get())) {
            e.discard();
        }
        BlockPos o = stage.origin;
        for (BlockPos pos : BlockPos.betweenClosed(o.offset(-13, 1, -13), o.offset(13, 4, 13))) {
            if (level.getBlockState(pos).is(ArenaGear.GLADIATOR_NET_BLOCK.get())) {
                level.removeBlock(pos, false);
            }
        }
    }

    /**
     * Kamera bei Ventor: Abstand, Drehung aus der Linie Ventor → Ritter (Grad), Höhe über seiner Brust. Sie bleibt über dem Sand
     * (nicht in der Brüstung) und steigt, bis nichts mehr zwischen ihr und den beiden steht (Podest, Spawner, Laternen).
     */
    private static Step follow(Stage stage, double angle, double distance, double height) {
        return Step.server(level -> {
            if (!(level.getEntity(stage.ventor) instanceof VentorEntity ventor) || level.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = ventor.position().add(0.0, 2.0, 0.0);
            Entity soldier = level.getEntity(stage.soldier);
            Vec3 toward = soldier != null ? soldier.position().subtract(ventor.position()).multiply(1.0, 0.0, 1.0) : new Vec3(1.0, 0.0, 0.0);
            if (toward.lengthSqr() < 1.0e-3) {
                toward = new Vec3(1.0, 0.0, 0.0);
            }
            Vec3 dir = toward.normalize().yRot((float) Math.toRadians(angle));
            Vec3 focus = c.add(toward.normalize().scale(Math.min(2.5, toward.length() * 0.4)));
            Vec3 middle = Vec3.atBottomCenterOf(stage.origin);
            double d = distance;
            while (d > 3.0 && horizontal(c.add(dir.scale(d)), middle) > 11.3) {
                d -= 0.25;
            }
            double h = height;
            Vec3 eye = c.add(dir.scale(d)).add(0.0, h, 0.0);
            for (int i = 0; i < 14 && (blocked(level, eye, c) || soldier != null && blocked(level, eye, soldier.position().add(0.0, 1.2, 0.0))); i++) {
                h += 0.5;
                eye = c.add(dir.scale(d)).add(0.0, h, 0.0);
            }
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    private static double horizontal(Vec3 a, Vec3 b) {
        return Math.sqrt((a.x - b.x) * (a.x - b.x) + (a.z - b.z) * (a.z - b.z));
    }

    /** Steht ein fester Block zwischen {@code from} und {@code to}? */
    private static boolean blocked(ServerLevel level, Vec3 from, Vec3 to) {
        return level.clip(new ClipContext(from, to, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, CollisionContext.empty())).getType()
                != HitResult.Type.MISS;
    }

    /** Ein Punkt der Arena (Mitte bei 0, 0, 0; man steht auf y = 1; das Haupttor liegt bei +z) in Weltkoordinaten. */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        return new Vec3(stage.origin.getX() + x + 0.5, stage.origin.getY() + y, stage.origin.getZ() + z + 0.5);
    }

    /** Kamera am Punkt (dx, dy, dz) der Arena (Augenhöhe), Blick auf (tx, ty, tz). */
    private static Step at(Stage stage, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (stage.origin == null) {
                return;
            }
            Vec3 eye = local(stage, dx, dy, dz);
            Vec3 target = local(stage, tx, ty, tz);
            Vec3 look = target.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", eye.x, eye.y - 1.62, eye.z, yaw,
                    pitch));
        });
    }

    private static <T extends Mob> T creature(ServerLevel level, EntityType<T> type, Vec3 at, float yaw) {
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
