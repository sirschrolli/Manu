package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.abyss.WorldSavior;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardLocator;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.block.abyss.HoardSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldEaterEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.List;
import java.util.function.BiFunction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung des Schlunds der Welten und des Weltenfressers (0.73): der Hort (von fern, auf dem Ring, von unten), das
 * Erwachen (die Kronensplitter zerspringen, er bricht durch den Schlund und brüllt), seine Bahn, jeder Angriff, sein Tod,
 * der Titel und die Beute. Bewegte Augenblicke werden mit {@code /tick freeze} angehalten und von einer Kamera relativ zu
 * Kopf und Ziel festgehalten.
 */
final class HoardShowcase {
    private static final String ABYSS = "kingdomsatwar:the_abyss";

    private HoardShowcase() {}

    /** Was eine Aufnahme braucht: Mitte des Rings, der Weltenfresser, das Ziel. */
    record Scene(Vec3 c, WorldEaterEntity eater, LivingEntity dummy) {
        Vec3 head() {
            return this.eater.headCenter();
        }
    }

    /** {@code only}: „view“ (nur der Hort), „wake“ (bis zum Erwachen), „fight“ (ohne Tod) oder alles. */
    static void hoard(Deque<Step> steps, String only) {
        double[] c = new double[3];
        boolean[] ready = new boolean[1];
        WorldEaterEntity[] eater = new WorldEaterEntity[1];
        LivingEntity[] dummy = new LivingEntity[1];
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.fov().set(70);
            minecraft.options.renderDistance().set(12);
        }));
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            BlockPos center = abyss == null ? null : HoardLocator.center(abyss);
            if (center == null) {
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.warn("[Showcase] kein Schlund der Welten gefunden");
                ready[0] = true;
                return;
            }
            for (int dx = -5; dx <= 5; dx++) {
                for (int dz = -5; dz <= 5; dz++) {
                    abyss.setChunkForced((center.getX() >> 4) + dx, (center.getZ() >> 4) + dz, true);
                }
            }
            c[0] = center.getX() + 0.5;
            c[1] = center.getY();
            c[2] = center.getZ() + 0.5;
            io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Showcase] Schlund der Welten bei {}", center.toShortString());
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));

        // der Hort: von fern, auf dem Ring zwischen den Kronensplittern, von unten
        view(steps, c, 40, 20, 34, 0, -4, 0);
        steps.add(Step.waitTicks(40));
        steps.add(AbyssShowcase.settle(1800));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("hoard_overview"));
        view(steps, c, 9, 1.0, 23, -1, 2.5, -4);
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("hoard_ring"));
        view(steps, c, 34, -30, 28, 0, -12, 0);
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("hoard_below"));
        if (only.equals("view")) {
            return;
        }

        // das Erwachen; ein unverwundbarer Ritter auf dem Ring dient als Ziel
        view(steps, c, 7, 1.4, 25, 0, 2.5, -8);
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            BlockPos home = BlockPos.containing(c[0], c[1], c[2]);
            if (!(abyss.getBlockEntity(HoardPiece.sealPosition(home)) instanceof HoardSealBlockEntity seal)) {
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.warn("[Showcase] kein Siegel bei {}", HoardPiece.sealPosition(home));
                return;
            }
            seal.reset(abyss);
            eater[0] = seal.awaken(abyss);
            var knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(abyss, EntitySpawnReason.COMMAND);
            if (knight != null && eater[0] != null) {
                knight.setNoAi(true);
                knight.setPermanentlyInvulnerable(true);
                knight.snapTo(c[0], c[1], c[2] + 19.5, 180.0f, 0.0f);
                knight.setYHeadRot(180.0f);
                knight.setYBodyRot(180.0f);
                abyss.addFreshEntity(knight);
                dummy[0] = knight;
                eater[0].debugHold(knight);
            }
        }));
        steps.add(Step.waitTicks(46));
        shot(steps, eater, dummy, c, "eater_wake_shards", (s, f) -> eye(s.c(), 7, 1.4, 25, 0, 3.5, -10));
        steps.add(Step.waitTicks(WorldEaterEntity.RISE_TICK + 26 - 46));
        shot(steps, eater, dummy, c, "eater_wake_rise", (s, f) -> eye(s.c(), 20, 4, 26, 0, 6, 0));
        steps.add(Step.waitTicks(16));
        shot(steps, eater, dummy, c, "eater_wake_roar", (s, f) -> {
            Vec3 h = s.head();
            return new Vec3[] {s.c().add(18, 6, 22), h.add(0, -3, 0)};
        });
        steps.add(Step.waitTicks(WorldEaterEntity.AWAKEN_TICKS - (WorldEaterEntity.RISE_TICK + 42) + 50));
        // auf seiner Bahn: vom Ring aus, unter dem Kopf
        shot(steps, eater, dummy, c, "eater_circle", (s, f) -> {
            Vec3 h = s.head();
            Vec3 out = horizontal(h.subtract(s.c()));
            Vec3 eye = s.c().add(out.scale(HoardPiece.R_OUT - 4.0)).add(0, 1.6, 0);
            return new Vec3[] {eye.add(out.scale(-6.0)), h.add(s.eater().heading().scale(-6.0))};
        });
        // der Kopf von vorn (er hält kurz inne) und der Rücken mit den Stücken der verschlungenen Welten von oben
        steps.add(Step.waitTicks(20));
        shot(steps, eater, dummy, c, "eater_head", (s, f) -> {
            Vec3 h = s.head();
            Vec3 fwd = s.eater().heading();
            Vec3 side = horizontal(new Vec3(-fwd.z, 0, fwd.x));
            return new Vec3[] {h.add(fwd.scale(15)).add(side.scale(8)).add(0, 3, 0), h.add(fwd.scale(-2))};
        });
        steps.add(Step.waitTicks(30));
        shot(steps, eater, dummy, c, "eater_back", (s, f) -> {
            Vec3 h = s.head();
            Vec3 fwd = s.eater().heading();
            Vec3 side = horizontal(new Vec3(-fwd.z, 0, fwd.x));
            Vec3 body = h.add(fwd.scale(-18));
            return new Vec3[] {body.add(side.scale(10)).add(0, 24, 0), body.add(fwd.scale(4))};
        });
        if (only.equals("wake")) {
            return;
        }

        // die Angriffe
        attack(steps, eater, dummy, c, "bite", 30, "eater_bite_rear", (s, f) -> eye(s.c(), 7, 2.0, 30, 0, 9, 12));
        steps.add(Step.waitTicks(8));
        shot(steps, eater, dummy, c, "eater_bite", (s, f) -> eye(s.c(), 9, 2.5, 28, 0, 2.5, 18));
        steps.add(Step.waitTicks(50));
        attack(steps, eater, dummy, c, "devour", 60, "eater_devour", (s, f) -> eye(s.c(), -9, 4, 15, 0, 4, 32));
        steps.add(Step.waitTicks(55));
        attack(steps, eater, dummy, c, "spit", 31, "eater_spit", (s, f) -> eye(s.c(), 15, 5, 27, -1, 6, 29));
        steps.add(Step.waitTicks(40));
        attack(steps, eater, dummy, c, "sweep", 50, "eater_sweep", (s, f) -> {
            Vec3 h = s.head();
            Vec3 fwd = s.eater().heading();
            Vec3 side = horizontal(new Vec3(-fwd.z, 0, fwd.x));
            if (side.dot(s.c().subtract(h)) < 0) {
                side = side.scale(-1);
            }
            return new Vec3[] {h.add(side.scale(17)).add(fwd.scale(6)).add(0, 5, 0), h.add(fwd.scale(-9))};
        });
        steps.add(Step.waitTicks(60));
        attack(steps, eater, dummy, c, "breath", 62, "eater_breath", (s, f) -> eye(s.c(), 21, 6, 21, 0, 8, -3));
        steps.add(Step.waitTicks(80));
        attack(steps, eater, dummy, c, "call", 50, "eater_call", (s, f) -> eye(s.c(), 2, 5, 31, 0, 2, 0));
        steps.add(Step.waitTicks(30));
        attack(steps, eater, dummy, c, "wrath", 26, "eater_wrath", (s, f) -> {
            Vec3 h = s.head();
            return new Vec3[] {s.c().add(20, 2, 24), h.add(0, 2, 0)};
        });
        steps.add(Step.waitTicks(60));
        if (only.equals("fight")) {
            return;
        }

        // sein Tod: Glied um Glied zerbricht, dann der Kopf; Beute auf dem Ring und der Titel
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            if (eater[0] != null) {
                eater[0].setHealth(1.0f);
                eater[0].hurtServer(abyss, abyss.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(Step.waitTicks(52));
        shot(steps, eater, dummy, c, "eater_death", (s, f) -> {
            Vec3 h = s.head();
            Vec3 out = horizontal(h.subtract(s.c()));
            Vec3 side = new Vec3(-out.z, 0, out.x);
            return new Vec3[] {h.add(out.scale(14)).add(side.scale(16)).add(0, 3, 0), h.add(out.scale(-10)).add(0, -3, 0)};
        });
        steps.add(Step.waitTicks(WorldEaterEntity.DEATH_TICKS - 52 + 30));
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            List<ServerPlayer> players = abyss.getServer().getPlayerList().getPlayers();
            if (!players.isEmpty()) {
                WorldSavior.grant(players.get(0));
            }
            if (dummy[0] != null) {
                dummy[0].discard();
            }
        }));
        view(steps, c, 4, 1.2, -13.5, 0, 0, -20.5);
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("eater_savior"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(100));
        steps.add(Step.screenshot("eater_hoard"));
        items(steps, c);
    }

    /** Die Belohnungen: die Trophäe auf dem Ring, der Weltenspeer in der Hand und ein Weltenbrocken, den er herabruft. */
    private static void items(Deque<Step> steps, double[] c) {
        LivingEntity[] target = new LivingEntity[1];
        steps.add(Step.command("kill @e[type=item]"));
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            BlockPos at = BlockPos.containing(c[0] + 3.0, c[1], c[2] - 16.0);
            abyss.setBlock(at, io.github.sirschrolli.kingdomsatwar.registry.AbyssBlocks.WORLD_EATER_TROPHY.get().defaultBlockState()
                    .setValue(io.github.sirschrolli.kingdomsatwar.block.abyss.WorldEaterTrophyBlock.FACING, net.minecraft.core.Direction.SOUTH),
                    net.minecraft.world.level.block.Block.UPDATE_ALL);
        }));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(40)));
        view(steps, c, 4.9, 1.25, -13.2, 3.5, 0.62, -15.5);
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("hoard_trophy"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // der Weltenspeer in der Hand: von vorn (dritte Person) und aus der eigenen Sicht
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:world_spear"));
        steps.add(Step.command("item replace entity @s armor.chest with minecraft:netherite_chestplate"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(net.minecraft.client.CameraType.THIRD_PERSON_FRONT)));
        steps.add((minecraft, ticks) -> {
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                    "execute in %s run tp @s %.2f %.2f %.2f 200 -18", ABYSS, c[0] - 2.0, c[1], c[2] + 20.0));
            return true;
        });
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(30)));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("hoard_spear"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.fov().set(70);
            minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
        }));
        steps.add((minecraft, ticks) -> {
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                    "execute in %s run tp @s %.2f %.2f %.2f 160 8", ABYSS, c[0] + 4.0, c[1], c[2] + 22.0));
            return true;
        });
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("hoard_spear_hand"));
        steps.add(Step.hud(false));
        // ein Weltenbrocken stürzt auf ein Ziel (ein Rissjäger, der stillhält)
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            var hound = ModEntities.RIFT_HOUND.get().create(abyss, EntitySpawnReason.COMMAND);
            List<ServerPlayer> players = abyss.getServer().getPlayerList().getPlayers();
            if (hound == null || players.isEmpty()) {
                return;
            }
            hound.setNoAi(true);
            hound.setPermanentlyInvulnerable(true);
            hound.snapTo(c[0] - 4.0, c[1], c[2] + 18.0, 90.0f, 0.0f);
            abyss.addFreshEntity(hound);
            target[0] = hound;
            io.github.sirschrolli.kingdomsatwar.item.abyss.WorldSpearItem.fallingChunk(abyss, hound, players.get(0),
                    io.github.sirschrolli.kingdomsatwar.entity.abyss.WorldChunkEntity.Kind.GLUT);
        }));
        steps.add((minecraft, ticks) -> {
            minecraft.player.connection.sendCommand(String.format(java.util.Locale.ROOT,
                    "execute in %s run tp @s %.2f %.2f %.2f 120 -14", ABYSS, c[0] + 5.0, c[1] + 1.0, c[2] + 22.5));
            return true;
        });
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("hoard_spear_chunk"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("hoard_spear_burst"));
    }

    /** Horizontal und auf Länge 1 (sonst Osten). */
    private static Vec3 horizontal(Vec3 v) {
        Vec3 h = new Vec3(v.x, 0, v.z);
        return h.lengthSqr() < 1.0e-6 ? new Vec3(1, 0, 0) : h.normalize();
    }

    /** Auge bei c + (dx, dy, dz), Blick auf c + (tx, ty, tz). */
    private static Vec3[] eye(Vec3 c, double dx, double dy, double dz, double tx, double ty, double tz) {
        return new Vec3[] {c.add(dx, dy, dz), c.add(tx, ty, tz)};
    }

    /** Beginnt einen Angriff und hält nach {@code ticks} Ticks das Bild fest. */
    private static void attack(Deque<Step> steps, WorldEaterEntity[] eater, LivingEntity[] dummy, double[] c, String name, int ticks, String shot,
            BiFunction<Scene, Integer, Vec3[]> camera) {
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            if (eater[0] != null && dummy[0] != null && eater[0].isAlive()) {
                eater[0].forceAttack(abyss, name, dummy[0]);
            }
        }));
        steps.add(Step.waitTicks(ticks));
        shot(steps, eater, dummy, c, shot, camera);
    }

    /** Hält das Spiel an, stellt die Kamera, macht das Bild und lässt das Spiel weiterlaufen. */
    private static void shot(Deque<Step> steps, WorldEaterEntity[] eater, LivingEntity[] dummy, double[] c, String name,
            BiFunction<Scene, Integer, Vec3[]> camera) {
        if (!Step.wanted(name)) {
            return;
        }
        steps.add(Step.command("tick freeze"));
        steps.add(Step.server(overworld -> {
            ServerLevel abyss = overworld.getServer().getLevel(AbyssGate.THE_ABYSS);
            if (eater[0] == null || dummy[0] == null) {
                return;
            }
            Vec3[] at = camera.apply(new Scene(new Vec3(c[0], c[1], c[2]), eater[0], dummy[0]), 0);
            Vec3 d = at[1].subtract(at[0]);
            float yaw = (float) (-Math.atan2(d.x, d.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(d.y, Math.sqrt(d.x * d.x + d.z * d.z)) * 180.0 / Math.PI);
            for (ServerPlayer player : abyss.getServer().getPlayerList().getPlayers()) {
                player.teleportTo(abyss, at[0].x, at[0].y - 1.62, at[0].z, java.util.Set.of(), yaw, pitch, true);
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot(name));
        steps.add(Step.command("tick unfreeze"));
    }

    /** Kamera (Augenhöhe) bei c + (dx, dy, dz), Blick auf c + (tx, ty, tz). */
    private static void view(Deque<Step> steps, double[] c, double dx, double dy, double dz, double tx, double ty, double tz) {
        steps.add((minecraft, ticks) -> {
            double x = c[0] + dx;
            double y = c[1] + dy - 1.62;
            double z = c[2] + dz;
            AbyssShowcase.lookNow(minecraft, ABYSS, x, y, z, c[0] + tx, c[1] + ty, c[2] + tz);
            return true;
        });
    }
}
