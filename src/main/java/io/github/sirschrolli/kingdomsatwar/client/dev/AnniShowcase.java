package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AfterimageEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.GlitchCloneEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.MissingZoneEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.PixelCubeEntity;
import io.github.sirschrolli.kingdomsatwar.entity.glitch.ScanlineEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code anni_boss}): Anni der Troll in einer echten Fehlerzone der Oberwelt – die Zone bei Tag und bei Nacht,
 * der Fehlerkern, das Laden (Pixel strömen herbei, er setzt sich zusammen, der Titel), Bildnisse, die Bossleiste, alle
 * Angriffe gegen einen (zähen) Ritter, der Absturz, die Wiederherstellung als Anni 2.0 (Zerfall, Sicherung, volle Leiste,
 * der neue Titel, die Welle), Anni 2.0 aus der Nähe, sein Speedhack, der endgültige Absturz, der erloschene Kern und die
 * Beute am Rüstungsständer.
 */
final class AnniShowcase {
    private static final String TAG = "kaw_anni_show";

    private AnniShowcase() {}

    /** Mitte der Arena (Boden + 1), der Kern und die Ids von Anni und Ritter. */
    private static final class Zone {
        final double[] c = new double[3];
        final double[] cam = new double[3];
        BlockPos core;
        int anni = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel overworld(ServerLevel any) {
        return any.getServer().overworld();
    }

    static void boss(Deque<Step> steps) {
        Zone s = new Zone();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(overworld(any), s)));
        steps.add(Step.until(() -> s.ready, 12000));
        // die Zone von oben
        steps.add(at(s, 0.0, 30.0, 21.0, 0.0, 0.0, -1.5));
        steps.add(arrived(s.cam));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(s.core).inflate(48, 30, 48))) {
                e.discard();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("anni_zone_overview"));
        steps.add(at(s, 7.0, 5.0, 8.0, -9.0, 8.0, -12.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("anni_zone"));
        steps.add(at(s, -8.0, 4.5, 7.0, 10.0, 7.0, -10.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("anni_zone_side"));
        steps.add(at(s, 2.6, 2.6, 3.4, 0.0, 1.3, 0.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("anni_core"));
        // ein Ritter tritt in die Arena: der Kern lädt Anni
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), ground(level, s, -3, 6), 180.0f);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setSilent(true);
            s.knight = knight.getId();
            if (level.getBlockEntity(s.core) instanceof ErrorCoreBlockEntity core) {
                AnniTrollEntity anni = core.summon(level);
                if (anni != null) {
                    s.anni = anni.getId();
                    anni.setTarget(knight);
                }
            }
        }));
        steps.add(at(s, 6.0, 4.0, 7.5, 0.0, 2.6, 1.0));
        steps.add(attackAt(s, AnniTrollEntity.Attack.SPAWN, 26));
        steps.add(Step.screenshot("anni_loading"));
        steps.add(attackAt(s, AnniTrollEntity.Attack.SPAWN, AnniTrollEntity.SPAWN_ASSEMBLE - 2));
        steps.add(Step.screenshot("anni_assemble"));
        steps.add(Step.hud(true));
        steps.add(attackAt(s, AnniTrollEntity.Attack.SPAWN, AnniTrollEntity.SPAWN_ASSEMBLE + 22));
        steps.add(Step.screenshot("anni_title"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(40));
        // Bildnisse: er hält still, der Kamera zugewandt
        steps.add(pose(s, -4.0, -2.0, 6.0, 8.0));
        steps.add(faceCam(s, 6.5, 2.4, 2.4, 18.0));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("anni_portrait"));
        steps.add(faceCam(s, 3.0, 3.3, 3.7, 12.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("anni_portrait_face"));
        steps.add(faceCam(s, 5.5, 2.2, 2.6, 80.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("anni_portrait_side"));
        steps.add(faceCam(s, 5.5, 3.0, 2.6, 155.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("anni_portrait_back"));
        steps.add(release(s));
        // mit Bossleiste
        steps.add(Step.hud(true));
        steps.add(follow(s, -35.0, 9.0, 2.0, 0.25));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("anni_bossbar"));
        steps.add(Step.hud(false));
        // Lag-Hieb: erst hängt er (Ladekreis), dann kracht der Hammer
        steps.add(reset(s, AnniTrollEntity.Attack.SMASH, 3.2));
        steps.add(attackAt(s, AnniTrollEntity.Attack.SMASH, AnniTrollEntity.SMASH_RAISE + 1));
        steps.add(follow(s, -70.0, 8.0, 1.5, 0.35));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("anni_smash_lag"));
        steps.add(slamAt(s, 2));
        steps.add(follow(s, -55.0, 10.0, 3.5, 0.5));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("anni_smash"));
        // die übrigen Angriffe: {Angriff, Tick des Bildes, Name, Blickwinkel zur Linie Anni → Ritter (Grad), Abstand, Höhe, Blick zum Ritter 0..1}
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : new String[][] {{"RUBBERBAND", "17", "anni_rubberband", "-60", "9", "3.0", "0.4"},
                {"CLONES", "24", "anni_clones", "-30", "9", "3.5", "0.55"},
                {"MISSING", "34", "anni_missing", "-70", "9", "6.0", "0.7"},
                {"PIXEL_RAIN", "44", "anni_pixel_rain", "-45", "10", "7.0", "0.45"},
                {"ROLLBACK", "44", "anni_rollback", "-80", "8", "3.0", "0.5"},
                {"SCANLINE", "22", "anni_scanline", "-35", "8", "4.5", "0.3"},
                {"CRASH", "30", "anni_crash", "-30", "7", "1.8", "0.0"},
                {"CRASH", "86", "anni_crash_wave", "-30", "10", "8.0", "0.0"}}) {
            String name = shot[0];
            AnniTrollEntity.Attack attack = AnniTrollEntity.Attack.valueOf(name);
            int tick = Integer.parseInt(shot[1]);
            if (!started.contains(name)) {
                started.add(name);
                steps.add(reset(s, attack, attack == AnniTrollEntity.Attack.CLONES || attack == AnniTrollEntity.Attack.RUBBERBAND ? 10.0 : 8.0));
            }
            steps.add(attackAt(s, attack, tick - 5));
            steps.add(follow(s, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]), Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot(shot[2]));
        }
        steps.add(Step.waitTicks(30));
        // der erste tödliche Treffer: Wiederherstellung als Anni 2.0
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            clearEffects(level, s);
            place(level, s, 8.0);
            if (level.getEntity(s.anni) instanceof AnniTrollEntity anni) {
                anni.forceAttack(AnniTrollEntity.Attack.NONE);
                anni.setInvulnerableTime(0);
                anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
            }
        }));
        steps.add(follow(s, -40.0, 8.0, 1.6, 0.0));
        steps.add(attackAt(s, AnniTrollEntity.Attack.REVIVE, 18));
        steps.add(Step.screenshot("anni_fatal"));
        steps.add(Step.hud(true));
        steps.add(attackAt(s, AnniTrollEntity.Attack.REVIVE, 84));
        steps.add(Step.screenshot("anni_restore"));
        steps.add(attackAt(s, AnniTrollEntity.Attack.REVIVE, AnniTrollEntity.REVIVE_ASSEMBLE + 8));
        steps.add(Step.screenshot("anni_v2_title"));
        steps.add(Step.hud(false));
        steps.add(attackAt(s, AnniTrollEntity.Attack.REVIVE, AnniTrollEntity.REVIVE_WAVE + 1));
        steps.add(follow(s, -30.0, 10.0, 8.0, 0.0));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("anni_v2_wave"));
        steps.add(Step.waitTicks(30));
        // Anni 2.0 aus der Nähe
        steps.add(Step.server(any -> clearEffects(overworld(any), s)));
        steps.add(pose(s, -4.0, -2.0, 6.0, 8.0));
        steps.add(faceCam(s, 6.5, 2.4, 2.4, -18.0));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("anni_v2_portrait"));
        steps.add(faceCam(s, 3.0, 3.3, 3.7, -12.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("anni_v2_face"));
        steps.add(release(s));
        steps.add(Step.hud(true));
        steps.add(follow(s, -35.0, 9.0, 2.0, 0.25));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("anni_v2_bossbar"));
        steps.add(Step.hud(false));
        // Speedhack: zielen (Ladekreis, Spur), dann rasen
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            clearEffects(level, s);
            if (level.getEntity(s.anni) instanceof AnniTrollEntity anni && level.getEntity(s.knight) instanceof LivingEntity knight) {
                Vec3 a = ground(level, s, -10, -6);
                Vec3 k = ground(level, s, 2, 10);
                knight.teleportTo(k.x, k.y, k.z);
                knight.setHealth(knight.getMaxHealth());
                anni.teleportTo(a.x, a.y, a.z);
                anni.setDeltaMovement(Vec3.ZERO);
                anni.setTarget(knight);
                anni.lookAt(knight, 360.0f, 360.0f);
                anni.setYBodyRot(anni.getYRot());
                anni.forceAttack(AnniTrollEntity.Attack.SPEEDHACK);
            }
        }));
        steps.add(attackAt(s, AnniTrollEntity.Attack.SPEEDHACK, AnniTrollEntity.HACK_FIRST + 3));
        steps.add(follow(s, -75.0, 11.0, 4.0, 0.45));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("anni_speedhack_aim"));
        steps.add(attackAt(s, AnniTrollEntity.Attack.SPEEDHACK, AnniTrollEntity.HACK_FIRST + AnniTrollEntity.HACK_AIM + 3));
        steps.add(at(s, 3.5, 6.0, -3.5, -4.0, 1.5, 2.0));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("anni_speedhack"));
        steps.add(Step.waitTicks(80));
        // endgültig: er stürzt ab
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            clearEffects(level, s);
            place(level, s, 8.0);
            if (level.getEntity(s.anni) instanceof AnniTrollEntity anni) {
                anni.forceAttack(AnniTrollEntity.Attack.NONE);
                anni.setInvulnerableTime(0);
                anni.hurtServer(level, level.damageSources().generic(), 1.0e6f);
            }
        }));
        steps.add(follow(s, -40.0, 8.0, 1.8, 0.0));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("anni_game_over"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(14));
        steps.add(Step.screenshot("anni_death"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            if (level.getEntity(s.knight) instanceof Entity knight) {
                knight.discard();
            }
            clearEffects(level, s);
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(s.core).inflate(40, 30, 40))) {
                item.discard();
            }
        }));
        steps.add(at(s, 2.6, 2.6, 3.4, 0.0, 1.3, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("anni_core_crashed"));
        // die Beute am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = overworld(any);
            Vec3 p = ground(level, s, 5, 4);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            stand.setYRot(-30.0f);
            stand.setYBodyRot(-30.0f);
            stand.setYHeadRot(-30.0f);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(AnniBlocks.BANNHAMMER.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(AnniBlocks.GLITCH_WINGS.get()));
            stand.addTag(TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(s, 6.0, 2.0, 5.9, 5.0, 1.2, 4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("anni_gear"));
        steps.add(at(s, 4.1, 2.1, 2.3, 5.0, 1.2, 4.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("anni_gear_back"));
        // bei Nacht leuchten fehlende Textur und Bildrauschen
        steps.add(Step.command("time set 13200"));
        steps.add(at(s, 0.0, 30.0, 21.0, 0.0, 0.0, -1.5));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("anni_zone_night"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time true"));
    }

    // ------------------------------------------------------------------ Hilfen

    /** Räumt auf, was Angriffe zurückließen: Klone, Felder, Pixel, Zeilen, Nachbilder und Ringe. */
    private static void clearEffects(ServerLevel level, Zone s) {
        AABB area = new AABB(s.core).inflate(48, 30, 48);
        for (Entity e : level.getEntitiesOfClass(Entity.class, area, e -> e instanceof GlitchCloneEntity || e instanceof MissingZoneEntity
                || e instanceof PixelCubeEntity || e instanceof ScanlineEntity || e instanceof AfterimageEntity || e instanceof SpellFxEntity)) {
            e.discard();
        }
    }

    /** Anni und Ritter an ihre Plätze (Anni im Abstand {@code distance} hinter dem Ritter), einander zugewandt. */
    private static void place(ServerLevel level, Zone s, double distance) {
        if (level.getEntity(s.anni) instanceof AnniTrollEntity anni && level.getEntity(s.knight) instanceof LivingEntity knight) {
            Vec3 k = ground(level, s, -3, 6);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.removeAllEffects();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setHealth(knight.getMaxHealth());
            Vec3 a = ground(level, s, -3, (int) Math.round(6 - distance));
            anni.setNoAi(false);
            anni.teleportTo(a.x, a.y, a.z);
            anni.setDeltaMovement(Vec3.ZERO);
            anni.setTarget(knight);
            anni.lookAt(knight, 360.0f, 360.0f);
            anni.setYBodyRot(anni.getYRot());
            anni.setYHeadRot(anni.getYRot());
        }
    }

    /** Vor einem Angriff: aufräumen, Plätze einnehmen, dann (zwei Ticks später) den Angriff beginnen. */
    private static Step reset(Zone s, AnniTrollEntity.Attack attack, double distance) {
        return Step.server(any -> {
            ServerLevel level = overworld(any);
            clearEffects(level, s);
            place(level, s, distance);
            if (level.getEntity(s.anni) instanceof AnniTrollEntity anni) {
                anni.forceAttack(attack);
            }
        });
    }

    /** Wartet, bis Anni (im Client) den Angriff {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn beendet hat. */
    private static Step attackAt(Zone s, AnniTrollEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(s.anni) instanceof AnniTrollEntity anni)) {
                return false;
            }
            if (anni.currentAttack() == attack) {
                seen[0] = true;
                return anni.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 600);
    }

    /** Wartet beim Lag-Hieb, bis der Hammer seit {@code ticks} Ticks wieder fällt (im Client). */
    private static Step slamAt(Zone s, int ticks) {
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(s.anni) instanceof AnniTrollEntity anni)) {
                return false;
            }
            return anni.currentAttack() != AnniTrollEntity.Attack.SMASH
                    || anni.attackTime(0.0f) > AnniTrollEntity.SMASH_RAISE + 2 && anni.sinceLag(0.0f) >= ticks;
        }, 400);
    }

    /** Er hält still an (x, z), dem Punkt (fx, fz) zugewandt. */
    private static Step pose(Zone s, double x, double z, double fx, double fz) {
        return Step.server(any -> {
            ServerLevel level = overworld(any);
            clearEffects(level, s);
            if (level.getEntity(s.anni) instanceof AnniTrollEntity anni) {
                anni.forceAttack(AnniTrollEntity.Attack.NONE);
                anni.setNoAi(true);
                Vec3 a = ground(level, s, (int) Math.round(x), (int) Math.round(z));
                Vec3 face = new Vec3(s.c[0] + fx, a.y, s.c[2] + fz).subtract(a);
                float yaw = (float) (Math.atan2(face.z, face.x) * 180.0 / Math.PI) - 90.0f;
                anni.snapTo(a.x, a.y, a.z, yaw, 0.0f);
                anni.setYBodyRot(yaw);
                anni.setYHeadRot(yaw);
                anni.setDeltaMovement(Vec3.ZERO);
            }
        });
    }

    private static Step release(Zone s) {
        return Step.server(any -> {
            if (overworld(any).getEntity(s.anni) instanceof AnniTrollEntity anni) {
                anni.setNoAi(false);
            }
        });
    }

    /**
     * Kamera bei Anni: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Anni → Ritter gedreht, {@code height}
     * über seiner Brust; Blick auf ihn ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Zone s, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = overworld(any);
            if (!(level.getEntity(s.anni) instanceof Entity anni) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = anni.position().add(anni.getDeltaMovement().scale(3.0)).add(0.0, 2.4, 0.0);
            Vec3 k = level.getEntity(s.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(0.0, 0.0, 1.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(angle));
            Vec3 eye = inArena(s, focus.add(dir.scale(distance)).add(0.0, height, 0.0));
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Hält die Kamera über der Arena (nicht in den Bäumen ringsum): höchstens elf Blöcke von der Mitte. */
    private static Vec3 inArena(Zone s, Vec3 eye) {
        double dx = eye.x - s.c[0];
        double dz = eye.z - s.c[2];
        double r = Math.sqrt(dx * dx + dz * dz);
        if (r <= 11.0) {
            return eye;
        }
        double k = 11.0 / r;
        return new Vec3(s.c[0] + dx * k, eye.y, s.c[2] + dz * k);
    }

    /**
     * Kamera vor Anni (aus seiner Blickrichtung um {@code angle} Grad gedreht) im Abstand {@code distance}, {@code height}
     * über dem Boden; Blick auf ihn in der Höhe {@code look}.
     */
    private static Step faceCam(Zone s, double distance, double height, double look, double angle) {
        return Step.server(any -> {
            ServerLevel level = overworld(any);
            if (!(level.getEntity(s.anni) instanceof Entity anni) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            double yaw = Math.toRadians(anni.getYRot() + angle);
            Vec3 forward = new Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
            Vec3 eye = anni.position().add(forward.scale(distance)).add(0.0, height, 0.0);
            Vec3 focus = anni.position().add(0.0, look, 0.0);
            Vec3 dir = focus.subtract(eye);
            float camYaw = (float) (-Math.atan2(dir.x, dir.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(dir.y, Math.sqrt(dir.x * dir.x + dir.z * dir.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), camYaw, pitch, true);
        });
    }

    /** Der Boden der Arena an (x, z) (relativ zur Mitte): über dem obersten festen Block (verrutschte Felder beachtet). */
    private static Vec3 ground(ServerLevel level, Zone s, int x, int z) {
        int bx = (int) Math.floor(s.c[0]) + x;
        int bz = (int) Math.floor(s.c[2]) + z;
        int base = (int) Math.floor(s.c[1]);
        for (int y = base + 4; y >= base - 3; y--) {
            BlockPos p = new BlockPos(bx, y, bz);
            if (level.getBlockState(p).isAir() && level.getBlockState(p.above()).isAir() && !level.getBlockState(p.below()).isAir()) {
                return new Vec3(bx + 0.5, y, bz + 0.5);
            }
        }
        return new Vec3(bx + 0.5, base, bz + 0.5);
    }

    /** Sucht die nächste Fehlerzone, lässt ihre Chunks entstehen und merkt sich Mitte und Kern. */
    private static void locate(ServerLevel level, Zone s) {
        var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(ModStructures.site(SiteKind.ERROR_ZONE));
        var found = structure.isEmpty() ? null : level.getChunkSource().getGenerator().findNearestMapStructure(level,
                HolderSet.direct(structure.get()), BlockPos.ZERO, 160, false);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] keine Fehlerzone gefunden");
            s.core = BlockPos.ZERO;
            s.ready = true;
            return;
        }
        int x = (found.getFirst().getX() & ~15) + 8;
        int z = (found.getFirst().getZ() & ~15) + 8;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced((x >> 4) + dx, (z >> 4) + dz, true);
                level.getChunk((x >> 4) + dx, (z >> 4) + dz);
            }
        }
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos core = null;
        for (int y = top + 4; y > top - 24 && core == null; y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (level.getBlockState(p).is(AnniBlocks.ERROR_CORE.get())) {
                core = p;
            }
        }
        s.core = core != null ? core : new BlockPos(x, top, z);
        s.c[0] = x + 0.5;
        s.c[1] = s.core.getY() - 1;
        s.c[2] = z + 0.5;
        KingdomsAtWar.LOGGER.info("[Showcase] Fehlerzone bei {}, Kern bei {} ({})", new BlockPos(x, 0, z), s.core, level.getBlockState(s.core));
        s.ready = true;
    }

    /** Wartet, bis die Kamera in der Oberwelt angekommen ist. */
    private static Step arrived(double[] cam) {
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.player != null && minecraft.level.dimension() == Level.OVERWORLD
                    && minecraft.player.distanceToSqr(cam[0], cam[1], cam[2]) < 4.0;
        }, 1200);
    }

    /** Kamera an Mitte + (dx, dy, dz) (Augenhöhe), Blick auf Mitte + (tx, ty, tz) – in der Oberwelt. */
    private static Step at(Zone s, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            s.cam[0] = s.c[0] + dx;
            s.cam[1] = s.c[1] + dy - 1.62;
            s.cam[2] = s.c[2] + dz;
            double lx = tx - dx;
            double ly = ty - dy;
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in minecraft:overworld run tp @s %.2f %.2f %.2f %.1f %.1f", s.cam[0], s.cam[1], s.cam[2], yaw, pitch));
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
