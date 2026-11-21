package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der Schattenzitadelle (Ansichten von außen und innen) und – mit {@code fight} – Nyxars Erwachen auf dem
 * Thronhof samt seiner eigenen Angriffe (als Bilder und Bildfolgen für Videos).
 */
final class CitadelShowcase {
    private static final String DIM = "kingdomsatwar:shadow_world";

    private CitadelShowcase() {}

    static void citadel(Deque<Step> steps, boolean fight) {
        citadel(steps, fight, false);
    }

    static void citadel(Deque<Step> steps, boolean fight, boolean mapOnly) {
        citadel(steps, fight, mapOnly, false);
    }

    static void citadel(Deque<Step> steps, boolean fight, boolean mapOnly, boolean fightOnly) {
        double[] c = new double[4];          // x, z, Boden, bereit
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.server(level -> {
            ServerLevel shadow = ShadowShowcase.shadow(level);
            BlockPos approx = CitadelLocator.approximateCenter(shadow);
            if (approx == null) {
                KingdomsAtWar.LOGGER.info("[Showcase] Keine Zitadelle gefunden");
                c[3] = -1;
                return;
            }
            // die ganze Festung erzeugen (alle Chunks im Umkreis), dann den Boden über dem Thronkern bestimmen
            long started = System.currentTimeMillis();
            int ccx = approx.getX() >> 4;
            int ccz = approx.getZ() >> 4;
            for (int r = 0; r <= 4; r++) {
                for (int x = ccx - r; x <= ccx + r; x++) {
                    for (int z = ccz - r; z <= ccz + r + 1; z++) {
                        if (Math.max(Math.abs(x - ccx), Math.abs(z - ccz)) == r || z == ccz + r + 1) {
                            shadow.getChunk(x, z, ChunkStatus.FULL, true);
                        }
                    }
                }
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Zitadelle erzeugt in {} ms", System.currentTimeMillis() - started);
            int coreZ = approx.getZ() - 25;
            int floor = Integer.MIN_VALUE;
            for (int y = shadow.getMaxY(); y > shadow.getMinY(); y--) {
                if (shadow.getBlockState(new BlockPos(approx.getX(), y, coreZ)).is(ModBlocks.SHADOW_THRONE_CORE.get())) {
                    floor = y + 3;
                    break;
                }
            }
            if (floor == Integer.MIN_VALUE) {
                KingdomsAtWar.LOGGER.info("[Showcase] Kein Thronkern bei {}", approx);
                floor = approx.getY();
            }
            debugMap(shadow, approx, floor);
            c[0] = approx.getX() + 0.5;
            c[1] = approx.getZ() + 0.5;
            c[2] = floor;
            c[3] = 1;
            KingdomsAtWar.LOGGER.info("[Showcase] Zitadelle bei {} {} Boden {}", approx.getX(), approx.getZ(), floor);
        }));
        steps.add(Step.until(() -> c[3] != 0, 4000));
        if (mapOnly) {
            return;
        }
        if (fightOnly) {
            steps.add(at(c, 0, 20, 40, 0, 6, 0));
            steps.add(Step.waitTicks(400));
            steps.add(Step.waitForChunks(3000));
            steps.add(Step.waitTicks(60));
            nyxar(steps, c);
            return;
        }
        // Anflug von oben (lädt die ganze Festung)
        steps.add(at(c, 75, 55, 95, 0, 6, 0));
        steps.add(Step.waitTicks(400));
        steps.add(Step.waitForChunks(3000));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("citadel_aerial"));
        steps.add(at(c, -60, 34, -70, 0, 6, 0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.screenshot("citadel_aerial_back"));
        // die Freitreppe hinauf zum Torhaus
        steps.add(at(c, 9, -10, 82, 0, 9, 40));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("citadel_gate"));
        // durchs Tor in den Thronhof
        steps.add(at(c, 0, 1, 30, 0, 6, -28));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("citadel_court"));
        steps.add(at(c, 9, 4, -12, 0, 8, -29));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("citadel_throne"));
        // auf dem Wehrgang und an einem Turm
        steps.add(at(c, -28, 13, 20, -14, 18, -36));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("citadel_wall"));
        steps.add(at(c, -52, 8, -32, -35, 22, -15));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("citadel_tower"));
        steps.add(at(c, 22, 20, 24, 0, 2, -6));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(1200));
        steps.add(Step.screenshot("citadel_inside"));
        if (!fight) {
            return;
        }
        nyxar(steps, c);
    }

    /** Draufsicht (Blockarten und Höhen) als Bild – zum Prüfen, ob und wie die Festung entstanden ist. */
    private static void debugMap(ServerLevel shadow, BlockPos center, int floor) {
        int r = 70;
        java.awt.image.BufferedImage img = new java.awt.image.BufferedImage(2 * r + 1, 2 * r + 1, java.awt.image.BufferedImage.TYPE_INT_RGB);
        int bricks = 0;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r; dz <= r; dz++) {
                int x = center.getX() + dx;
                int z = center.getZ() + dz;
                int y = shadow.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z) - 1;
                var state = shadow.getBlockState(new BlockPos(x, y, z));
                int col = state.getMapColor(shadow, new BlockPos(x, y, z)).col;
                int shade = Math.max(-60, Math.min(60, (y - floor) * 3));
                int rr = Math.max(0, Math.min(255, ((col >> 16) & 255) + shade));
                int gg = Math.max(0, Math.min(255, ((col >> 8) & 255) + shade));
                int bb = Math.max(0, Math.min(255, (col & 255) + shade));
                img.setRGB(dx + r, dz + r, (rr << 16) | (gg << 8) | bb);
                if (state.is(io.github.sirschrolli.kingdomsatwar.registry.ShadowBlocks.SHADOW_STONE_BRICKS.base().get())) {
                    bricks++;
                }
            }
        }
        try {
            javax.imageio.ImageIO.write(img, "png", new java.io.File("citadel_map.png"));
        } catch (java.io.IOException e) {
            KingdomsAtWar.LOGGER.warn("[Showcase] Karte nicht gespeichert", e);
        }
        var chunk = shadow.getChunk(center.getX() >> 4, center.getZ() >> 4);
        chunk.getAllStarts().forEach((structure, start) -> KingdomsAtWar.LOGGER.info("[Showcase] Start {} gültig {} Teile {} Box {}",
                structure, start.isValid(), start.getPieces().size(), start.getBoundingBox()));
        chunk.getAllReferences().forEach((structure, refs) -> KingdomsAtWar.LOGGER.info("[Showcase] Bezug {} {}", structure, refs.size()));
        for (int dz : new int[] {0, 10, -25}) {
            StringBuilder col = new StringBuilder();
            for (int y = floor - 2; y <= floor + 45; y += 1) {
                var st = shadow.getBlockState(center.offset(0, 0, dz).atY(y));
                col.append(y).append('=').append(st.getBlock().getDescriptionId().replace("block.", "").replace("kingdomsatwar.", "k:")
                        .replace("minecraft.", "")).append(' ');
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Säule dz={}: {}", dz, col);
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Karte: {} Ziegel oben, Mitte oben {} bei {}", bricks,
                shadow.getBlockState(center.atY(floor)), shadow.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING,
                        center.getX(), center.getZ()));
    }

    /** Nyxar erwacht auf dem Thronhof: Soldaten treten an, dann seine Angriffe der Reihe nach. */
    private static void nyxar(Deque<Step> steps, double[] c) {
        DragonBossEntity[] boss = new DragonBossEntity[1];
        steps.add(Step.server(level -> {
            ServerLevel shadow = ShadowShowcase.shadow(level);
            BlockPos core = BlockPos.containing(c[0], c[2] - 3, c[1] - 25);
            var be = shadow.getBlockEntity(core);
            if (be instanceof ShadowThroneCoreBlockEntity throne) {
                KingdomsAtWar.LOGGER.info("[Showcase] Thronkern bei {}, Hofmitte {}, Zustand {}", core, throne.center(), throne.phase());
                boss[0] = throne.awaken(shadow);
            }
            if (boss[0] == null) {
                KingdomsAtWar.LOGGER.info("[Showcase] Nyxar lässt sich nicht wecken: {} bei {} ({})", be, core, shadow.getBlockState(core));
            }
        }));
        steps.add(at(c, 0, 3, 31, 0, 7, 0));
        steps.add(Step.waitTicks(5));
        steps.add(Step.record("nyxar_awaken", 210, minecraft -> { }));
        steps.add(Step.server(level -> {
            ServerLevel shadow = ShadowShowcase.shadow(level);
            SoldierType[] types = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.ARCHER, SoldierType.SWORDSMAN, SoldierType.KNIGHT,
                    SoldierType.ARCHER, SoldierType.CAPTAIN};
            for (int i = 0; i < types.length; i++) {
                SoldierEntity s = ModEntities.SOLDIERS.get(types[i]).get().create(shadow, EntitySpawnReason.COMMAND);
                if (s == null) {
                    continue;
                }
                s.snapTo(c[0] - 12 + i * 4, c[2] + 1, c[1] + 18, 180, 0);
                s.setPermanentlyInvulnerable(true);
                s.setPersistenceRequired();
                shadow.addFreshEntity(s);
            }
        }));
        steps.add(Step.hud(true));
        steps.add(at(c, 0, 9, 30, 0, 6, 0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("nyxar_fight_bar"));
        steps.add(Step.hud(false));
        Object[][] attacks = {{"shadow_shriek", 50}, {"breath", 80}, {"shadow_step", 64}, {"shadow_claws", 90}, {"soul_drain", 86},
                {"eclipse", 150}, {"nova_ring", 52}};
        for (Object[] attack : attacks) {
            steps.add(Step.server(level -> {
                if (boss[0] != null) {
                    boss[0].setHealth(boss[0].getMaxHealth() * 0.5f);
                }
            }));
            steps.add(forceAttack(boss, (String) attack[0]));
            steps.add(Step.record("nyxar_" + attack[0], (Integer) attack[1], courtCam(boss, c, 22.0, 8.0)));
        }
        steps.add(forceAttack(boss, "take_off"));
        steps.add(Step.record("nyxar_flight", 100, courtCam(boss, c, 26.0, 4.0)));
        steps.add(forceAttack(boss, "shadow_rain"));
        steps.add(Step.record("nyxar_shadow_rain", 120, courtCam(boss, c, 26.0, 2.0)));
        // Tod: das Herz der Schatten
        steps.add(forceAttack(boss, "land"));
        steps.add(Step.until(() -> boss[0] == null || !boss[0].isFlying(), 240));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> {
            if (boss[0] == null) {
                return;
            }
            // für die Aufnahme in die Mitte des Thronhofs, Blick zum Tor
            boss[0].teleportTo(c[0], c[2] + 1, c[1] - 4);
            boss[0].setYRot(0);
            boss[0].setYBodyRot(0);
            boss[0].setYHeadRot(0);
            boss[0].setHealth(1.0f);
            boss[0].hurtServer(ShadowShowcase.shadow(level), boss[0].damageSources().genericKill(), 100.0f);
        }));
        steps.add(at(c, 9, 6, 24, 0, 6, -4));
        steps.add(Step.waitTicks(3));
        steps.add(Step.record("nyxar_death", 230, minecraft -> { }));
        steps.add(Step.waitTicks(40));
        steps.add(at(c, 4, 2, 2, 0, 2, -4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("nyxar_heart"));
        steps.add(Step.hud(true));
    }

    private static Step forceAttack(DragonBossEntity[] boss, String name) {
        return Step.server(level -> {
            DragonBossEntity b = boss[0];
            if (b == null) {
                return;
            }
            BossAttack attack = b.findAttack(name);
            net.minecraft.world.entity.LivingEntity target = b.getTarget();
            if (target == null) {
                target = b.level().getEntitiesOfClass(SoldierEntity.class, b.getBoundingBox().inflate(60)).stream().findFirst().orElse(null);
            }
            if (attack != null && target != null) {
                b.startAttack((ServerLevel) b.level(), attack, target);
            } else {
                KingdomsAtWar.LOGGER.info("[Showcase] Angriff {} nicht möglich (Angriff {}, Ziel {})", name, attack, target);
            }
        });
    }

    /**
     * Kamera im Thronhof: zwischen Boss und Hofmitte (höchstens 30 Blöcke von der Mitte, damit sie nie in Mauern oder
     * Türmen steckt), weich nachgeführt, Blick auf den Boss.
     */
    private static Consumer<Minecraft> courtCam(DragonBossEntity[] boss, double[] c, double distance, double up) {
        double[] cam = new double[4];
        return minecraft -> {
            DragonBossEntity b = boss[0];
            if (b == null) {
                return;
            }
            Vec3 center = new Vec3(c[0], c[2] + 1, c[1]);
            Vec3 toCenter = center.subtract(b.position()).multiply(1, 0, 1);
            Vec3 dir = toCenter.lengthSqr() < 9.0 ? new Vec3(0.35, 0, 1).normalize() : toCenter.normalize();
            Vec3 want = b.position().add(dir.scale(distance)).add(0, up + 4.0, 0);
            Vec3 off = want.subtract(center).multiply(1, 0, 1);
            if (off.length() > 30.0) {
                want = new Vec3(center.x + off.x / off.length() * 30.0, want.y, center.z + off.z / off.length() * 30.0);
            }
            want = new Vec3(want.x, Math.max(want.y, c[2] + 4.0), want.z);
            if (cam[3] == 0) {
                cam[0] = want.x;
                cam[1] = want.y;
                cam[2] = want.z;
                cam[3] = 1;
            } else {
                cam[0] += (want.x - cam[0]) * 0.05;
                cam[1] += (want.y - cam[1]) * 0.05;
                cam[2] += (want.z - cam[2]) * 0.05;
            }
            Vec3 look = b.position().add(0, 5.0, 0).subtract(cam[0], cam[1], cam[2]);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f",
                    cam[0], cam[1], cam[2], yaw, pitch));
        };
    }

    /** Kamera an (Mitte + d), Augen auf Höhe Boden + dy + 1,62, Blick auf (Mitte + t) – in der Schattenwelt. */
    private static Step at(double[] c, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (c[3] < 0) {
                return;
            }
            double cx = c[0] + dx;
            double cy = c[2] + dy;
            double cz = c[1] + dz;
            double lx = tx - dx;
            double ly = ty - (dy + 1.62);
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                    DIM, cx, cy, cz, yaw, pitch));
        });
    }
}
