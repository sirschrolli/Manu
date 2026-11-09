package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.sea.ReefColossusEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.AnemoneGardenFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.GlowFieldFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.KelpForestFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.SeaStackFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.ShellBedFeature;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.feature.VentFieldFeature;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Nur zur Prüfung des Meeres (0.62), in einer echten Welt: das Versunkene Atoll aus der Luft, in der Lagune, am Hügel
 * und unter Wasser, bei Nacht mit Meeresleuchten; dann Karkinos – Erwachen, Bossleiste, jeder Angriff, Tod und der
 * Gezeiten-Speer (in der Hand und von außen). Die Szene {@code seafloor} setzt die neuen Landschaften des Meeresgrunds
 * (Tangwald, Anemonengarten, Muschelbank, heiße Quellen, Leuchtfeld, Felsnadeln) an passende Stellen im Meer um das
 * Atoll und zeigt sie bei Tag und Nacht.
 */
final class AtollShowcase {
    private AtollShowcase() {}

    /** Mitte der Lagune: x, z, Höhe (Meeresspiegel), gefunden (1) / keins (-1). */
    private static void locate(Deque<Step> steps, double[] c) {
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.server(level -> {
            BlockPos center = AtollLocator.center(level);
            if (center == null) {
                c[3] = -1;
                KingdomsAtWar.LOGGER.info("[Showcase] Kein Atoll in dieser Welt");
                return;
            }
            c[0] = center.getX() + 0.5;
            c[1] = center.getZ() + 0.5;
            c[2] = center.getY();
            c[3] = 1;
            KingdomsAtWar.LOGGER.info("[Showcase] Atoll bei {}", center);
        }));
        steps.add(Step.until(() -> c[3] != 0, 600));
    }

    /** Kamera relativ zur Mitte (dx, dy, dz), Blick auf einen Punkt relativ zur Mitte (tx, ty, tz). */
    private static Step look(double[] c, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            double cx = c[0] + dx;
            double cy = c[2] + dy;
            double cz = c[1] + dz;
            double lx = tx - dx;
            double ly = ty - dy;
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cx, cy - 1.62, cz, yaw, pitch));
        });
    }

    /**
     * Sucht rund um die Mitte einen Blickpunkt ({@code distance} Blöcke entfernt, {@code height} hoch), von dem aus keine
     * Säule und keine Palme die Sicht auf den Koloss verstellt – beginnend bei {@code prefer} Grad (0 = Osten, -90 = Norden).
     */
    private static Step pickView(double[] c, double[] cam, double distance, double height, double ty, double prefer) {
        return Step.server(level -> {
            Vec3 target = new Vec3(c[0], c[2] + ty, c[1]);
            for (int i = 0; i < 36; i++) {
                double off = (i % 2 == 0 ? 1 : -1) * ((i + 1) / 2) * 10.0;
                double a = Math.toRadians(prefer + off);
                Vec3 eye = new Vec3(c[0] + Math.cos(a) * distance, c[2] + height, c[1] + Math.sin(a) * distance);
                boolean clear = true;
                for (double up : new double[] {-1.8, 0.0, 1.8, 3.4}) {
                    for (double side : new double[] {-3.8, -2.0, 0.0, 2.0, 3.8}) {
                        Vec3 to = target.add(-Math.sin(a) * side, up, Math.cos(a) * side);
                        if (level.clip(new ClipContext(eye, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, CollisionContext.empty()))
                                .getType() != HitResult.Type.MISS) {
                            clear = false;
                        }
                    }
                }
                if (clear) {
                    cam[0] = eye.x - c[0];
                    cam[1] = height;
                    cam[2] = eye.z - c[1];
                    KingdomsAtWar.LOGGER.info("[Showcase] Freier Blick bei {} Grad", Math.round(prefer + off));
                    return;
                }
            }
            cam[0] = Math.cos(Math.toRadians(prefer)) * distance;
            cam[1] = height;
            cam[2] = Math.sin(Math.toRadians(prefer)) * distance;
        });
    }

    /** Kamera an den gewählten Blickpunkt, Blick auf die Mitte (plus {@code ty}). */
    private static Step lookFromCam(double[] c, double[] cam, double ty) {
        return Step.once(minecraft -> {
            double dx = cam[0];
            double dy = cam[1];
            double dz = cam[2];
            float yaw = (float) (-Math.atan2(-dx, -dz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ty - dy, Math.sqrt(dx * dx + dz * dz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", c[0] + dx, c[2] + dy - 1.62,
                    c[1] + dz, yaw, pitch));
        });
    }

    private static void shot(Deque<Step> steps, String name, double[] c, double dx, double dy, double dz, double tx, double ty, double tz,
            int settle) {
        steps.add(look(c, dx, dy, dz, tx, ty, tz));
        steps.add(Step.waitTicks(settle));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot(name));
    }

    static void atoll(Deque<Step> steps) {
        double[] c = new double[4];
        locate(steps, c);
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // aus der Luft
        steps.add(look(c, 0, 72, 86, 0, 0, 0));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(3000));
        steps.add(Step.waitTicks(120));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(level -> {
            BlockPos center = BlockPos.containing(c[0], c[2], c[1]);
            StringBuilder col = new StringBuilder();
            for (int y = (int) c[2] - 8; y <= c[2] + 6; y++) {
                col.append(y).append('=').append(level.getBlockState(center.atY(y)).getBlock().getDescriptionId()
                        .replace("block.", "").replace("kingdomsatwar.", "k:").replace("minecraft.", "")).append(' ');
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Säule Mitte: {}", col);
            KingdomsAtWar.LOGGER.info("[Showcase] Herz: {}", level.getBlockEntity(AtollLocator.core(center)));
        }));
        steps.add(Step.waitTicks(200));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.screenshot("atoll_aerial"));
        shot(steps, "atoll_aerial_low", c, -58, 26, -46, 0, 0, 0, 200);
        shot(steps, "atoll_lagoon", c, 15, 3.2, 23, 0, 2.0, 0, 60);
        shot(steps, "atoll_mound", c, 5.5, 2.6, 8.5, 0, 1.2, 0, 40);
        shot(steps, "atoll_islands", c, -14, 3.0, 6, -30, 3.0, -14, 60);
        shot(steps, "atoll_beach", c, 21, 2.4, -6, 28, 2.5, -18, 60);
        // unter Wasser: die Riffkante außen am Ring und ein Korallenkopf in der Lagune
        shot(steps, "atoll_reef", c, 0, -3.2, 41, 0, -3.5, 30, 80);
        shot(steps, "atoll_lagoon_underwater", c, 9, -0.6, 15, 3, -1.2, 9, 60);
        // Nacht: die Lagune leuchtet
        steps.add(Step.command("time set 18000"));
        shot(steps, "atoll_night", c, 14, 4.5, 20, 0, 1.5, 0, 80);
        steps.add(Step.command("time set 23200"));
        shot(steps, "atoll_dawn", c, -40, 16, 30, 0, 2, 0, 80);
        steps.add(Step.command("time set 6000"));
        fight(steps, c);
    }

    /** Karkinos: Erwachen, Bossleiste, jeder Angriff, Tod und der Gezeiten-Speer. */
    private static void fight(Deque<Step> steps, double[] c) {
        ReefColossusEntity[] boss = new ReefColossusEntity[1];
        List<LivingEntity> soldiers = new ArrayList<>();
        double[] near = new double[3];
        double[] wide = new double[3];
        steps.add(look(c, 6, 4.0, -13, 0, 2.0, 0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            BlockPos center = BlockPos.containing(c[0], c[2], c[1]);
            if (level.getBlockEntity(AtollLocator.core(center)) instanceof AtollCoreBlockEntity core) {
                boss[0] = core.awaken(level);
            }
            KingdomsAtWar.LOGGER.info("[Showcase] Karkinos erwacht: {}", boss[0]);
        }));
        // Blick von vorn (er erwacht mit dem Gesicht nach Norden), ohne Säule davor (der Hügel ist jetzt fort)
        steps.add(pickView(c, near, 14.0, 4.8, 2.4, -70));
        steps.add(pickView(c, wide, 21.0, 7.0, 2.8, -75));
        steps.add(lookFromCam(c, near, 2.0));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("reef_awaken_1"));
        steps.add(Step.waitTicks(26));
        steps.add(Step.screenshot("reef_awaken_2"));
        steps.add(Step.waitTicks(50));
        steps.add(Step.server(level -> {
            SoldierType[] types = {SoldierType.KNIGHT, SoldierType.SWORDSMAN, SoldierType.CAPTAIN};
            double[][] spots = {{-4.5, -10.5}, {3.5, -11.0}, {-0.5, -13.5}};
            for (int i = 0; i < types.length; i++) {
                SoldierEntity s = ModEntities.SOLDIERS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
                if (s == null) {
                    continue;
                }
                s.snapTo(c[0] + spots[i][0], c[2], c[1] + spots[i][1], 0.0f, 0.0f);
                s.setPermanentlyInvulnerable(true);
                s.setPersistenceRequired();
                level.addFreshEntity(s);
                soldiers.add(s);
            }
            if (boss[0] != null && !soldiers.isEmpty()) {
                boss[0].forceAttack(level, "none", soldiers.get(0), true);
            }
        }));
        steps.add(Step.hud(true));
        steps.add(lookFromCam(c, wide, 2.4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("reef_title"));
        steps.add(Step.waitTicks(110));
        steps.add(Step.screenshot("reef_fight_bar"));
        steps.add(Step.hud(false));
        Object[][] attacks = {{"slam", 19}, {"grab", 24}, {"jet", 36}, {"roll", 44}, {"summon", 32}, {"tide", 34}, {"lance", 37},
                {"burrow", 52}};
        for (Object[] attack : attacks) {
            String name = (String) attack[0];
            steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 160));
            steps.add(Step.server(level -> {
                ReefColossusEntity b = boss[0];
                if (b == null) {
                    return;
                }
                // zurück in die Mitte, mit dem Gesicht zu den Soldaten
                b.teleportTo(c[0], c[2], c[1]);
                b.setYRot(180.0f);
                b.setYBodyRot(180.0f);
                b.setYHeadRot(180.0f);
                if ("tide".equals(name) || "lance".equals(name)) {
                    b.setHealth(b.getMaxHealth() * 0.35f);
                }
                b.forceAttack(level, name, soldiers.isEmpty() ? null : soldiers.get(0), true);
            }));
            steps.add(lookFromCam(c, "tide".equals(name) || "lance".equals(name) || "summon".equals(name) ? wide : near, 2.0));
            steps.add(Step.waitTicks((Integer) attack[1]));
            steps.add(Step.screenshot("reef_" + name));
            if ("burrow".equals(name)) {
                steps.add(Step.waitTicks(36));
                steps.add(Step.screenshot("reef_erupt"));
            }
            if ("roll".equals(name)) {
                steps.add(Step.waitTicks(40));
                steps.add(Step.screenshot("reef_dizzy"));
            }
        }
        // Tod: er sackt zusammen und versinkt, der Speer fällt in den Sand
        steps.add(Step.until(() -> boss[0] == null || "none".equals(boss[0].currentAttack()), 200));
        steps.add(Step.server(level -> {
            ReefColossusEntity b = boss[0];
            if (b == null) {
                return;
            }
            b.teleportTo(c[0], c[2], c[1]);
            b.setYRot(180.0f);
            b.setYBodyRot(180.0f);
            b.setYHeadRot(180.0f);
            b.setHealth(1.0f);
            b.hurtServer(level, b.damageSources().genericKill(), 100.0f);
            soldiers.forEach(LivingEntity::discard);
        }));
        steps.add(lookFromCam(c, near, 1.4));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("reef_death"));
        steps.add(Step.waitTicks(42));
        steps.add(Step.screenshot("reef_spear_drop"));
        steps.add(Step.waitTicks(60));
        steps.add(look(c, 2.5, 1.6, -4.5, 0, 0.4, -1.5));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("reef_spear_ground"));
        // der Gezeiten-Speer in der Hand (erste Person und von vorn), im flachen Wasser der Lagune
        steps.add(Step.command("kill @e[type=item]"));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("give @s kingdomsatwar:tide_spear"));
        steps.add(Step.command("give @s kingdomsatwar:tide_horn"));
        steps.add(Step.command("give @s kingdomsatwar:giant_kelp 16"));
        steps.add(Step.command("give @s kingdomsatwar:purple_sea_anemone 8"));
        steps.add(Step.command("give @s kingdomsatwar:scallop_shells 8"));
        steps.add(Step.command("give @s kingdomsatwar:glow_algae 8"));
        steps.add(Step.command("give @s kingdomsatwar:reef_colossus_spawn_egg"));
        steps.add(Step.server(level -> level.players().forEach(player -> {
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
            player.getInventory().setSelectedSlot(0);
        })));
        steps.add(look(c, 3, 2.2, -9, -6, 1.5, -20));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitTicks(40));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("tide_spear_hand"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("tide_spear_front"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("tide_spear_back"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.hud(true));
        steps.add(Step.command("gamemode spectator"));
    }

    // =====================================================================================
    // Meeresgrund
    // =====================================================================================

    /** Eine Landschaft des Meeresgrunds: wie tief es dort sein soll, wie weit die Kamera steht, ob sie über Wasser blickt. */
    private record Scene(String name, int minDepth, int maxDepth, double distance, double eye, boolean above, boolean night) {}

    private static final Scene[] SCENES = {
            new Scene("seafloor_kelp", 10, 22, 13.0, 4.0, false, false),
            new Scene("seafloor_anemones", 4, 12, 7.0, 2.6, false, false),
            new Scene("seafloor_shells", 2, 7, 6.0, 2.4, false, false),
            new Scene("seafloor_vents", 15, 32, 11.0, 4.0, false, false),
            new Scene("seafloor_glow", 13, 32, 11.0, 4.5, false, true),
            new Scene("seafloor_stacks", 6, 16, 26.0, 7.0, true, false),
    };

    static void seafloor(Deque<Step> steps) {
        double[] c = new double[4];
        locate(steps, c);
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(10)));
        double[][] spots = new double[SCENES.length][4];   // x, z, Grund, gefunden
        steps.add(Step.server(level -> {
            var generator = level.getChunkSource().getGenerator();
            var random = level.getChunkSource().randomState();
            int sea = generator.getSeaLevel();
            for (int i = 0; i < SCENES.length; i++) {
                Scene scene = SCENES[i];
                BlockPos found = null;
                search:
                for (int r = 70; r <= 420 && found == null; r += 9) {
                    int n = Math.max(16, (int) (2 * Math.PI * r / 12));
                    for (int k = 0; k < n; k++) {
                        double a = i * 1.05 + 2 * Math.PI * k / n;
                        int x = (int) Math.round(c[0] + Math.cos(a) * r);
                        int z = (int) Math.round(c[1] + Math.sin(a) * r);
                        int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                        int depth = sea - floor;
                        if (depth < scene.minDepth() || depth > scene.maxDepth()) {
                            continue;
                        }
                        // eben genug und ringsum Meer (die Kamera steht südlich davon)
                        boolean ok = true;
                        for (int[] d : new int[][] {{8, 0}, {-8, 0}, {0, 8}, {0, -8}, {0, (int) scene.distance()}}) {
                            int f2 = generator.getBaseHeight(x + d[0], z + d[1], Heightmap.Types.OCEAN_FLOOR_WG, level, random);
                            if (Math.abs(f2 - floor) > 4 || sea - f2 < 2) {
                                ok = false;
                                break;
                            }
                        }
                        if (ok) {
                            found = new BlockPos(x, floor, z);
                            break search;
                        }
                    }
                }
                if (found == null) {
                    KingdomsAtWar.LOGGER.info("[Showcase] Keine Stelle für {}", scene.name());
                    continue;
                }
                for (int dx = -2; dx <= 2; dx++) {
                    for (int dz = -2; dz <= 2; dz++) {
                        level.getChunk((found.getX() >> 4) + dx, (found.getZ() >> 4) + dz);
                    }
                }
                int floor = level.getHeight(Heightmap.Types.OCEAN_FLOOR, found.getX(), found.getZ());
                BlockPos at = new BlockPos(found.getX(), floor, found.getZ());
                boolean placed = place(level, scene.name(), at);
                spots[i][0] = at.getX() + 0.5;
                spots[i][1] = at.getZ() + 0.5;
                spots[i][2] = floor;
                spots[i][3] = 1;
                KingdomsAtWar.LOGGER.info("[Showcase] {} bei {} (Tiefe {}, gesetzt {})", scene.name(), at, sea - floor, placed);
            }
        }));
        for (int i = 0; i < SCENES.length; i++) {
            Scene scene = SCENES[i];
            double[] s = spots[i];
            if (scene.night()) {
                steps.add(Step.command("time set 18000"));
            }
            steps.add(Step.once(minecraft -> {
                if (s[3] == 0) {
                    return;
                }
                double sea = minecraft.level.getSeaLevel();
                double camY = scene.above() ? sea + scene.eye() : s[2] + scene.eye();
                double targetY = scene.above() ? sea + 3.0 : s[2] + 1.2;
                double cx = s[0] + scene.distance() * 0.35;
                double cz = s[1] + scene.distance();
                double lx = s[0] - cx;
                double ly = targetY - camY;
                double lz = s[1] - cz;
                float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cx, camY - 1.62, cz, yaw, pitch));
            }));
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(1500));
            steps.add(Step.waitTicks(scene.night() ? 120 : 60));
            steps.add(Step.screenshot(scene.name()));
            if ("seafloor_vents".equals(scene.name())) {
                // die Quellen bei Nacht
                steps.add(Step.command("time set 18000"));
                steps.add(Step.waitTicks(60));
                steps.add(Step.screenshot("seafloor_vents_night"));
                steps.add(Step.command("time set 6000"));
            }
            if ("seafloor_anemones".equals(scene.name())) {
                steps.add(Step.command("time set 18000"));
                steps.add(Step.waitTicks(60));
                steps.add(Step.screenshot("seafloor_anemones_night"));
                steps.add(Step.command("time set 6000"));
            }
            if (scene.night()) {
                steps.add(Step.command("time set 6000"));
            }
        }
        steps.add(Step.hud(true));
    }

    private static boolean place(ServerLevel level, String name, BlockPos at) {
        var generator = level.getChunkSource().getGenerator();
        RandomSource random = RandomSource.create(name.hashCode());
        return switch (name) {
            case "seafloor_kelp" -> {
                boolean a = new KelpForestFeature(0.6f).place(level, generator, random, at);
                new KelpForestFeature(0.6f).place(level, generator, random, at.offset(-7, 0, -6));
                yield a;
            }
            case "seafloor_anemones" -> new AnemoneGardenFeature().place(level, generator, random, at);
            case "seafloor_shells" -> {
                boolean a = new ShellBedFeature().place(level, generator, random, at);
                new ShellBedFeature().place(level, generator, random, at.offset(4, 0, -3));
                yield a;
            }
            case "seafloor_vents" -> new VentFieldFeature().place(level, generator, random, at);
            case "seafloor_glow" -> new GlowFieldFeature().place(level, generator, random, at);
            case "seafloor_stacks" -> new SeaStackFeature(Blocks.SANDSTONE.defaultBlockState(), Blocks.CALCITE.defaultBlockState(),
                    Blocks.SMOOTH_SANDSTONE.defaultBlockState()).place(level, generator, random, at);
            default -> false;
        };
    }
}
