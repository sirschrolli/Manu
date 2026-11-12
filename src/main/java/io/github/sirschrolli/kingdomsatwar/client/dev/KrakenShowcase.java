package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.sea.KrakenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SeaBlocks;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerDimension;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerTerrain;
import io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawLocator;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.level.block.Blocks;

/**
 * Nur zur Prüfung des Kraken (0.91), im Urmeer: der Krakenschlund aus der Luft, am Felsenring, unter Wasser (Becken,
 * Wrack, Gerippe, Trichter, Herz); dann der Kampf – Emporsteigen mit Titel und Bossleiste, jeder Angriff, Wut, Tod – und
 * die Krakenangel in der Hand und ausgeworfen.
 */
final class KrakenShowcase {
    private static final int SEA = UrmeerTerrain.SEA_LEVEL;

    private KrakenShowcase() {}

    private static ServerLevel urmeer(ServerLevel any) {
        return any.getServer().getLevel(UrmeerDimension.LEVEL);
    }

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    /** Kamera relativ zur Mitte des Schlunds (an der Wasseroberfläche), Blick auf einen Punkt relativ zur Mitte. */
    private static Step look(double[] c, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            double lx = tx - dx;
            double ly = ty - dy;
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in kingdomsatwar:urmeer run tp @s %.2f %.2f %.2f %.1f %.1f",
                    c[0] + dx, c[2] + dy - 1.62, c[1] + dz, yaw, pitch));
        });
    }

    private static Step arrived(double[] c, double dx, double dy, double dz) {
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.player != null && minecraft.level.dimension() == UrmeerDimension.LEVEL
                    && minecraft.player.distanceToSqr(c[0] + dx, c[2] + dy - 1.62, c[1] + dz) < 4.0;
        }, 1200);
    }

    private static void shot(Deque<Step> steps, String name, double[] c, double dx, double dy, double dz, double tx, double ty, double tz,
            int settle) {
        steps.add(look(c, dx, dy, dz, tx, ty, tz));
        steps.add(arrived(c, dx, dy, dz));
        steps.add(Step.waitTicks(settle));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot(name));
    }

    static void kraken(Deque<Step> steps) {
        // Mitte: x, z, Meeresspiegel, gefunden; dann Wrack (x, y, z) und Gerippe (x, y, z) relativ zur Mitte
        double[] c = new double[4];
        double[] wreck = new double[3];
        double[] bones = new double[3];
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("effect give @s minecraft:night_vision infinite 0 true"));
        steps.add(Step.command("effect give @s minecraft:conduit_power infinite 0 true"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.server(any -> {
            ServerLevel level = urmeer(any);
            BlockPos center = level == null ? null : KrakenMawLocator.center(level);
            if (center == null) {
                c[3] = -1;
                KingdomsAtWar.LOGGER.info("[Showcase] Kein Krakenschlund");
                return;
            }
            c[0] = center.getX() + 0.5;
            c[1] = center.getZ() + 0.5;
            c[2] = center.getY();
            c[3] = 1;
            KingdomsAtWar.LOGGER.info("[Showcase] Krakenschlund bei {}", center);
        }));
        steps.add(Step.until(() -> c[3] != 0, 6000));

        // --- der Schlund ---
        steps.add(look(c, 0, 40, 42, 0, -8, 0));
        steps.add(arrived(c, 0, 40, 42));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(3000));
        steps.add(Step.waitTicks(120));
        steps.add(Step.server(any -> {
            ServerLevel level = urmeer(any);
            BlockPos center = BlockPos.containing(c[0], c[2], c[1]);
            // alle Chunks des Schlunds samt Herz erzeugen, dann Wrack und Gerippe suchen
            for (int x = -3; x <= 3; x++) {
                for (int z = -3; z <= 3; z++) {
                    level.getChunk((center.getX() >> 4) + x, (center.getZ() >> 4) + z);
                }
            }
            BlockPos core = KrakenMawLocator.core(center);
            KingdomsAtWar.LOGGER.info("[Showcase] Herz: {} {}", level.getBlockState(core), level.getBlockEntity(core));
            double bx = 0;
            double by = 0;
            double bz = 0;
            int n = 0;
            for (int x = -30; x <= 30; x++) {
                for (int z = -30; z <= 30; z++) {
                    double r = Math.sqrt(x * x + z * z);
                    if (r < 11 || r > 30) {
                        continue;
                    }
                    for (int y = SEA - 50; y <= SEA - 28; y++) {
                        BlockPos p = center.offset(x, y - center.getY(), z);
                        var state = level.getBlockState(p);
                        if (state.is(Blocks.CHEST)) {
                            wreck[0] = x;
                            wreck[1] = y - SEA;
                            wreck[2] = z;
                        } else if (state.is(Blocks.BONE_BLOCK)) {
                            bx += x;
                            by += y - SEA;
                            bz += z;
                            n++;
                        }
                    }
                }
            }
            if (n > 0) {
                bones[0] = bx / n;
                bones[1] = by / n;
                bones[2] = bz / n;
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Wrack-Truhe {} {} {}, Gerippe ({} Knochen) {} {} {}", wreck[0], wreck[1], wreck[2], n,
                    bones[0], bones[1], bones[2]);
        }));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.screenshot("kraken_schlund_luft"));
        shot(steps, "kraken_schlund_ring", c, -20, 9, -14, 14, 2, 18, 80);
        shot(steps, "kraken_schlund_felsen", c, 6, 3, 6, -24, 4, 22, 60);
        // unter Wasser
        shot(steps, "kraken_becken", c, 13, -30, -15, 0, -46, 0, 80);
        shot(steps, "kraken_trichter", c, 3.5, -55, -2.5, 0, -72, 0, 60);
        shot(steps, "kraken_herz", c, 2.8, -67, -2.8, 0, -71.5, 0, 60);
        // Wrack und Gerippe aus der Nähe (über ihnen, schräg)
        shotAt(steps, "kraken_wrack", c, wreck, 6, 5, 7);
        shotAt(steps, "kraken_gerippe", c, bones, -8, 7, 6);
        fight(steps, c);
        rod(steps, c);
    }

    /** Ein Bild von einem erst zur Laufzeit gefundenen Punkt (relativ zur Mitte), aus dem Abstand (ox, oy, oz). */
    private static void shotAt(Deque<Step> steps, String name, double[] c, double[] at, double ox, double oy, double oz) {
        double[] cam = new double[6];
        steps.add(Step.once(minecraft -> {
            cam[0] = at[0] + ox;
            cam[1] = at[1] + oy;
            cam[2] = at[2] + oz;
            cam[3] = at[0];
            cam[4] = at[1] + 1.0;
            cam[5] = at[2];
            double lx = cam[3] - cam[0];
            double ly = cam[4] - cam[1];
            double lz = cam[5] - cam[2];
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in kingdomsatwar:urmeer run tp @s %.2f %.2f %.2f %.1f %.1f",
                    c[0] + cam[0], c[2] + cam[1] - 1.62, c[1] + cam[2], yaw, pitch));
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot(name));
    }

    /** Der Kampf: Emporsteigen, Titel und Bossleiste, jeder Angriff, Wut und Tod. */
    private static void fight(Deque<Step> steps, double[] c) {
        KrakenEntity[] boss = new KrakenEntity[1];
        List<LivingEntity> soldiers = new ArrayList<>();
        // ein Floß aus Gezeitensteinziegeln vor ihm (er steigt mit dem Gesicht nach Norden empor), darauf drei Soldaten
        steps.add(Step.server(any -> {
            ServerLevel level = urmeer(any);
            BlockPos center = BlockPos.containing(c[0], c[2], c[1]);
            for (int x = -3; x <= 3; x++) {
                for (int z = -19; z <= -13; z++) {
                    level.setBlockAndUpdate(center.offset(x, -1, z), SeaBlocks.TIDE_STONE_BRICKS.base().get().defaultBlockState());
                    for (int y = 0; y <= 3; y++) {
                        level.setBlockAndUpdate(center.offset(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
            SoldierType[] types = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.CAPTAIN};
            double[][] spots = {{-1.5, -14.5}, {1.5, -15.0}, {0.0, -17.0}};
            for (int i = 0; i < types.length; i++) {
                SoldierEntity s = ModEntities.SOLDIERS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
                if (s == null) {
                    continue;
                }
                s.snapTo(c[0] + spots[i][0], c[2], c[1] + spots[i][1], 0.0f, 0.0f);
                s.setPermanentlyInvulnerable(true);
                s.setPersistenceRequired();
                s.setNoAi(true);
                level.addFreshEntity(s);
                soldiers.add(s);
            }
            if (level.getBlockEntity(KrakenMawLocator.core(center)) instanceof KrakenCoreBlockEntity core) {
                core.reset(level);
                boss[0] = core.awaken(level);
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Der Kraken steigt empor: {}", boss[0]);
        }));
        steps.add(look(c, -13, 8, -38, 0, 7.0, 0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("kraken_steigt_1"));
        steps.add(Step.waitTicks(24));
        steps.add(Step.screenshot("kraken_steigt_2"));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("kraken_titel"));
        steps.add(Step.waitTicks(60));
        steps.add(Step.server(any -> {
            if (boss[0] != null && !soldiers.isEmpty()) {
                boss[0].forceAttack(urmeer(any), "none", soldiers.get(0), true);
            }
        }));
        steps.add(Step.screenshot("kraken_bossleiste"));
        steps.add(Step.hud(false));
        shot(steps, "kraken_nah", c, -15, 6, -19, 0, 7.0, 0, 20);
        shot(steps, "kraken_seite", c, 26, 7, -5, 0, 7.0, 0, 20);
        shot(steps, "kraken_unter_wasser", c, -13, -12, -15, 0, -2.5, 0, 30);
        // jeder Angriff (Name, Bild nach so vielen Ticks, weites Bild?)
        Object[][] attacks = {{"slam", 20, false}, {"slam", 28, false}, {"sweep", 18, false}, {"grab", 34, false}, {"ink", 22, false},
                {"whirl", 60, true}, {"whirl", KrakenEntity.WHIRL_BURST + 2, true}, {"call", 44, true}, {"wave", 38, true}};
        int[] index = new int[1];
        for (Object[] attack : attacks) {
            String name = (String) attack[0];
            int at = (Integer) attack[1];
            boolean wide = (Boolean) attack[2];
            String file = "kraken_" + name + "_" + at;
            if (!Step.wanted(file)) {
                continue;
            }
            steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 200));
            steps.add(Step.server(any -> {
                KrakenEntity b = boss[0];
                if (b == null) {
                    return;
                }
                ServerLevel level = urmeer(any);
                b.setYRot(180.0f);
                b.setYBodyRot(180.0f);
                b.setYHeadRot(180.0f);
                if ("wave".equals(name)) {
                    b.setHealth(b.getMaxHealth() * 0.3f);
                }
                // nach dem Würgegriff steht der Gepackte wieder auf dem Floß
                for (int i = 0; i < soldiers.size(); i++) {
                    double[][] spots = {{-1.5, -14.5}, {1.5, -15.0}, {0.0, -17.0}};
                    soldiers.get(i).teleportTo(c[0] + spots[i][0], c[2], c[1] + spots[i][1]);
                    soldiers.get(i).setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
                }
                b.forceAttack(level, name, soldiers.isEmpty() ? null : soldiers.get(index[0]++ % soldiers.size() == 1 ? 1 : 0), true);
            }));
            if (wide) {
                steps.add(look(c, -22, 15, -40, 0, 4.0, -4));
            } else {
                steps.add(look(c, -16, 8, -32, 0, 6.0, -5));
            }
            steps.add(Step.waitTicks(at));
            steps.add(Step.screenshot(file));
        }
        newAttacks(steps, c, boss, soldiers);
        // Tod: er erschlafft und sinkt zurück in den Schlund
        steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 200));
        steps.add(Step.server(any -> {
            KrakenEntity b = boss[0];
            if (b == null) {
                return;
            }
            b.setHealth(1.0f);
            b.hurtServer(urmeer(any), b.damageSources().genericKill(), 100.0f);
            soldiers.forEach(LivingEntity::discard);
        }));
        steps.add(look(c, -13, 8, -34, 0, 6.0, 0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("kraken_tod_1"));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("kraken_tod_2"));
        steps.add(Step.waitTicks(40));
    }

    /** Ein Bild aus einer Kamera, die dem Kraken folgt: seitlich ({@code side}, rechts +), darüber, dahinter ({@code back}, vorn −). */
    private static void follow(Deque<Step> steps, String name, double[] c, KrakenEntity[] boss, double side, double up, double back, double lookUp) {
        steps.add(Step.server(any -> {
            KrakenEntity b = boss[0];
            if (b == null) {
                return;
            }
            double yaw = Math.toRadians(b.getYRot());
            net.minecraft.world.phys.Vec3 fwd = new net.minecraft.world.phys.Vec3(-Math.sin(yaw), 0.0, Math.cos(yaw));
            net.minecraft.world.phys.Vec3 right = new net.minecraft.world.phys.Vec3(-fwd.z, 0.0, fwd.x);
            // innerhalb des Felsenrings bleiben: sonst von der anderen Seite (gespiegelt), notfalls von oben
            net.minecraft.world.phys.Vec3 cam = null;
            for (double[] flip : new double[][] {{1, 1}, {-1, 1}, {1, -1}, {-1, -1}}) {
                net.minecraft.world.phys.Vec3 p = b.position().add(fwd.scale(-back * flip[1])).add(right.scale(side * flip[0])).add(0.0, up, 0.0);
                if (Math.sqrt(Math.pow(p.x - c[0], 2) + Math.pow(p.z - c[1], 2)) <= 22.0) {
                    cam = p;
                    break;
                }
            }
            if (cam == null) {
                cam = b.position().add(fwd.scale(-6.0)).add(0.0, up + 14.0, 0.0);
            }
            net.minecraft.world.phys.Vec3 at = b.position().add(fwd.scale(3.0)).add(0.0, lookUp, 0.0);
            double lx = at.x - cam.x;
            double ly = at.y - cam.y;
            double lz = at.z - cam.z;
            float camYaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float camPitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            player(any).teleportTo(urmeer(any), cam.x, cam.y - 1.62, cam.z, Set.of(), camYaw, camPitch, true);
        }));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot(name));
    }

    /**
     * Seit 1.00: er schwimmt umher (aus mehreren Blickwinkeln), dann Rammstoß, Ausbruch aus der Tiefe und Tentakelkäfig gegen
     * einen Ritter, der östlich von ihm im offenen Wasser treibt, und das Geysirfeld um das Floß.
     */
    private static void newAttacks(Deque<Step> steps, double[] c, KrakenEntity[] boss, List<LivingEntity> soldiers) {
        LivingEntity[] swimmer = new LivingEntity[1];
        steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 200));
        steps.add(Step.server(any -> {
            ServerLevel level = urmeer(any);
            SoldierEntity s = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
            if (s != null) {
                s.snapTo(c[0] + 14.0, c[2] - 0.6, c[1] + 2.0, 90.0f, 0.0f);
                s.setPermanentlyInvulnerable(true);
                s.setPersistenceRequired();
                s.setNoAi(true);
                s.setNoGravity(true);
                level.addFreshEntity(s);
                swimmer[0] = s;
            }
            KrakenEntity b = boss[0];
            if (b != null) {
                b.setHealth(b.getMaxHealth() * 0.8f);
                b.forceSwim(swimmer[0] != null ? swimmer[0] : soldiers.isEmpty() ? null : soldiers.get(0), true);
            }
        }));
        // er schwimmt: von vorn, von der Seite, von oben mit dem Kielwasser
        steps.add(Step.waitTicks(120));
        follow(steps, "kraken_schwimmt_1", c, boss, -12.0, 7.0, -18.0, 5.0);
        steps.add(Step.waitTicks(40));
        follow(steps, "kraken_schwimmt_2", c, boss, 20.0, 6.0, 2.0, 4.0);
        steps.add(Step.waitTicks(40));
        follow(steps, "kraken_schwimmt_3", c, boss, 6.0, 22.0, 20.0, 0.0);
        steps.add(Step.waitTicks(30));
        follow(steps, "kraken_schwimmt_4", c, boss, -9.0, 3.0, 12.0, 4.0);

        // die neuen Angriffe (Name, Ziel: Schwimmer oder Floß, Bilder: Tick, Kamera relativ zur Mitte, Blickpunkt)
        Object[][] attacks = {
                // (die Kameras bleiben innerhalb des Felsenrings, sonst steht ein Fels im Bild)
                {"charge", true, new double[][] {{18, 6, 10, 19, 7, 3, 0}, {31, 6, 10, 19, 10, 3, 0}}},
                {"breach", true, new double[][] {{14, -3, 9, 19, 3, 3, 0}, {48, 8, 20, 16, 11, 0, 2}, {KrakenEntity.BREACH_ERUPT + 3, 4, 9, 18, 13, 8, 2},
                        {KrakenEntity.BREACH_ERUPT + 12, 4, 9, 18, 13, 6, 2}}},
                // (die Geysire steigen zufällig verteilt auf – also von hoch oben, über den Felsen)
                {"geyser", true, new double[][] {{28, 12, 24, 20, 13, 2, 2}, {KrakenEntity.GEYSER_FIRST + KrakenEntity.GEYSER_WARN + 3, 12, 24, 20, 13, 5,
                        2}}},
                {"cage", true, new double[][] {{KrakenEntity.CAGE_SPAWN + 16, 12, 10, 17, 14, 3, 2}, {KrakenEntity.CAGE_SPAWN + 46, 8, 12, 15, 14, 2, 2},
                        {KrakenEntity.CAGE_SPAWN + 59, 6, 9, 14, 14, 3, 2}}}};
        for (Object[] attack : attacks) {
            String name = (String) attack[0];
            boolean atSwimmer = (Boolean) attack[1];
            double[][] shots = (double[][]) attack[2];
            boolean any = false;
            for (double[] shot : shots) {
                any |= Step.wanted("kraken_" + name + "_" + (int) shot[0]);
            }
            if (!any) {
                continue;
            }
            steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 300));
            steps.add(Step.server(any2 -> {
                KrakenEntity b = boss[0];
                if (b == null) {
                    return;
                }
                ServerLevel level = urmeer(any2);
                b.forceSwim(null, false);
                b.debugReturnHome();
                LivingEntity target = atSwimmer ? swimmer[0] : soldiers.isEmpty() ? null : soldiers.get(0);
                if (target != null) {
                    double dx = target.getX() - b.getX();
                    double dz = target.getZ() - b.getZ();
                    float yaw = -((float) Math.atan2(dx, dz)) * (float) (180.0 / Math.PI);
                    b.setYRot(yaw);
                    b.setYBodyRot(yaw);
                    b.setYHeadRot(yaw);
                }
                b.forceAttack(level, name, target, true);
            }));
            int done = 0;
            for (double[] shot : shots) {
                steps.add(look(c, shot[1], shot[2], shot[3], shot[4], shot[5], shot[6]));
                steps.add(Step.waitTicks(Math.max(1, (int) shot[0] - done)));
                done = (int) shot[0];
                steps.add(Step.screenshot("kraken_" + name + "_" + (int) shot[0]));
                done += 3;
            }
        }
        steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 300));
        steps.add(Step.server(any -> {
            if (boss[0] != null) {
                boss[0].debugReturnHome();
            }
            if (swimmer[0] != null) {
                swimmer[0].discard();
            }
        }));
    }

    /** Die Krakenangel: in der Hand (eigene Sicht und von vorn), ausgeworfen über dem Schlund und unter Wasser. */
    private static void rod(Deque<Step> steps, double[] c) {
        steps.add(Step.command("kill @e[type=item]"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("give @s kingdomsatwar:kraken_fishing_rod"));
        steps.add(Step.command("give @s kingdomsatwar:tide_horn"));
        steps.add(Step.command("give @s kingdomsatwar:kraken_spawn_egg"));
        // auf dem Floß stehend (das bleibt nach dem Kampf liegen)
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = urmeer(any);
            player.teleportTo(level, c[0] + 0.5, c[2], c[1] - 16.0, Set.of(), 180.0f, 8.0f, true);
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
            player.getInventory().setSelectedSlot(0);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("krakenangel_ich"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("krakenangel_vorn"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        // ausgeworfen: der Schwimmer treibt vor dem Floß im Schlund
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = urmeer(any);
            if (player.fishing != null) {
                player.fishing.discard();
            }
            FishingHook hook = new FishingHook(player, level, 0, 0);
            hook.snapTo(player.getX() - 1.5, player.getY() - 0.1, player.getZ() + 6.0, 0.0f, 0.0f);
            hook.setDeltaMovement(0.0, -0.05, 0.0);
            level.addFreshEntity(hook);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("krakenangel_wurf"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(6));
        steps.add(Step.screenshot("krakenangel_wurf_ich"));
        // unter Wasser: ein Taucher mit der Krakenangel
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            ServerLevel level = urmeer(any);
            if (player.fishing != null) {
                player.fishing.discard();
            }
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 20 * 600, 0, false,
                    false, false));
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.teleportTo(level, c[0] + 8.5, c[2] - 14.0, c[1] + 8.5, Set.of(), 135.0f, 25.0f, true);
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> {
            ServerPlayer player = player(any);
            player.setItemInHand(InteractionHand.MAIN_HAND, player.getMainHandItem());
            FishingHook hook = new FishingHook(player, urmeer(any), 0, 0);
            urmeer(any).addFreshEntity(hook);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("krakenangel_unter_wasser"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("krakenangel_unter_wasser_hinten"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.hud(true));
        steps.add(Step.command("gamemode spectator"));
    }
}
