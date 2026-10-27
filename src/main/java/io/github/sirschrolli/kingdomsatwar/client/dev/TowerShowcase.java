package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerBuilder;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageTowerPiece;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;

/**
 * Vorführung der Magiertürme: Jeder Turm wird auf echtem Gelände in seinem Biom gebaut und von außen (Weitwinkel, Rückseite,
 * Eingang, Krone) und innen (Halle, Bibliothek, Laboratorium, Kammer, Sanktum mit dem Erzmagier, Blick ins Dach) gezeigt.
 * {@code towerworld} sucht dagegen die Türme, die der Weltgenerator selbst gebaut hat.
 */
final class TowerShowcase {
    private static final String DIM = "minecraft:overworld";

    private TowerShowcase() {}

    /** Kamera: Standpunkt (x, Füße y, z) und Blickpunkt (x, y, z) in Turmkoordinaten, dazu Name und Sichtfeld. */
    private record View(String name, double ex, double ey, double ez, double lx, double ly, double lz, int fov) {
        boolean outside() {
            return Math.max(Math.abs(this.ex), Math.abs(this.ez)) > 12;
        }
    }

    private static final List<View> VIEWS = List.of(
            new View("wide", -30, 10, -44, 0, 27, 0, 70),
            new View("back", 36, 16, 38, 0, 29, 0, 70),
            new View("entrance", -6, 1.5, -17, 0, 4, -6, 70),
            new View("crown", 17, 27, -21, 0, 35, 0, 70),
            new View("hall", 3.3, 2, -3.6, -2, 3.2, 3, 95),
            new View("library", 2.6, 8, -2.4, -3, 9.6, 1.5, 95),
            new View("laboratory", -1.6, 14, 2.8, 0, 14.6, -2, 95),
            new View("quarters", -2.4, 20, -1.8, 3.5, 20.6, 2, 95),
            new View("sanctum", -5.5, 26, 5.5, 1, 27.6, -1, 90),
            new View("sanctum_top", 0.5, 30.5, 6.6, 0, 25, -0.8, 95),
            new View("sanctum_up", 2.2, 26, 2.2, 0.4, 46, 0.4, 100));

    static void towers(Deque<Step> steps, List<DragonType> elements) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
        }));
        for (DragonType element : elements) {
            double[] c = new double[5];
            steps.add(Step.server(level -> placeInBiome(level, element, c)));
            steps.add(Step.until(() -> c[3] != 0, 6000));
            addViews(steps, element, c, "tower_");
        }
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
    }

    /** Die Türme, die der Weltgenerator gebaut hat (wie /locate), je einmal aus der Weite und im Sanktum. */
    static void towerWorld(Deque<Step> steps, List<DragonType> elements) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
        }));
        for (DragonType element : elements) {
            double[] c = new double[5];
            steps.add(Step.server(level -> locateAndGenerate(level, element, c)));
            steps.add(Step.until(() -> c[3] != 0, 8000));
            int n = 0;
            for (View view : List.of(VIEWS.get(0), VIEWS.get(1), VIEWS.get(8))) {
                at(steps, c, view);
                steps.add(Step.waitTicks(n++ == 0 ? 200 : 60));
                steps.add(Step.waitForChunks(2000));
                steps.add(Step.screenshot("towerworld_" + element.id() + "_" + view.name()));
            }
        }
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
    }

    private static void addViews(Deque<Step> steps, DragonType element, double[] c, String prefix) {
        int n = 0;
        for (View view : VIEWS) {
            at(steps, c, view);
            steps.add(Step.waitTicks(n == 0 ? 320 : view.outside() ? 120 : 40));
            steps.add(Step.waitForChunks(3000));
            steps.add(Step.screenshot(prefix + element.id() + "_" + String.format(Locale.ROOT, "%02d", n++) + "_" + view.name()));
        }
    }

    // =====================================================================================
    // Kampf
    // =====================================================================================

    /**
     * Der Kampf im Sanktum, aus Sicht eines (unverwundbaren) Spielers mit Bossleiste: Erwachen, Kampf, Runenschild,
     * Phasenwechsel, großer Zauber, Spiegelbilder, Tod und Beute.
     */
    static void mageFights(Deque<Step> steps, List<DragonType> elements) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(8);
            minecraft.options.cloudStatus().set(net.minecraft.client.CloudStatus.OFF);
        }));
        for (DragonType element : elements) {
            String id = "magefight_" + element.id();
            double[] c = new double[5];
            steps.add(Step.server(level -> placeInBiome(level, element, c, 2400)));
            steps.add(Step.until(() -> c[3] != 0, 6000));
            at(steps, c, new View("fight", STAND[0], STAND[1], STAND[2], 0, MageTowerBuilder.SANCTUM + 2.6, 0, 80));
            steps.add(Step.waitTicks(40));
            // erst wenn der ganze Turm beim Spieler angekommen und gezeichnet ist, betritt er das Sanktum
            steps.add(Step.until(() -> towerLoaded(c), 1200));
            steps.add(Step.waitForChunks(1200));
            steps.add(Step.waitTicks(20));
            steps.add(Step.command("gamemode survival"));
            steps.add(Step.server(level -> level.players().forEach(player -> {
                player.removeAllEffects();
                player.clearFire();
                player.setTicksFrozen(0);
            })));
            steps.add(Step.command("effect give @s minecraft:resistance infinite 255 true"));
            steps.add(Step.command("effect give @s minecraft:regeneration infinite 255 true"));
            steps.add(Step.command("effect give @s minecraft:saturation infinite 255 true"));
            steps.add(Step.command("effect give @s minecraft:fire_resistance infinite 0 true"));
            steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:aetherit_staff"));
            steps.add(Step.hud(true));
            steps.add(Step.until(() -> false, 22));
            shot(steps, c, id + "_1_awaken");
            steps.add(Step.waitTicks(70));
            steps.add(Step.command("title @s clear"));
            shot(steps, c, id + "_2_fight");
            steps.add(Step.server(level -> withMage(level, c, mage -> mage.devAttack(4))));
            steps.add(Step.waitTicks(16));
            steps.add(Step.command("title @s clear"));
            shot(steps, c, id + "_3_shield");
            steps.add(Step.server(level -> withMage(level, c, mage -> mage.setHealth(mage.getMaxHealth() * 0.3f))));
            steps.add(Step.waitTicks(12));
            shot(steps, c, id + "_4_phase");
            steps.add(Step.waitTicks(40));
            steps.add(Step.server(level -> withMage(level, c, mage -> mage.devAttack(5))));
            steps.add(Step.waitTicks(element == DragonType.LIGHTNING ? 30 : 34));
            shot(steps, c, id + "_5_ultimate");
            steps.add(Step.waitTicks(22));
            shot(steps, c, id + "_6_ultimate");
            steps.add(Step.waitTicks(MageUltimatesDuration.MAX + 30));
            steps.add(Step.server(level -> withMage(level, c, mage -> mage.devAttack(3))));
            steps.add(Step.waitTicks(24));
            shot(steps, c, id + "_7_images");
            steps.add(Step.server(level -> withMage(level, c, mage -> {
                mage.setInvulnerableTime(0);
                mage.hurtServer(level, level.damageSources().genericKill(), 10_000.0f);
            })));
            steps.add(Step.waitTicks(30));
            shot(steps, c, id + "_8_dying");
            steps.add(Step.waitTicks(60));
            shot(steps, c, id + "_9_loot");
            steps.add(Step.hud(false));
            steps.add(Step.command("gamemode spectator"));
        }
    }

    /** Sind alle Chunks rund um den Turm beim Spieler angekommen? */
    private static boolean towerLoaded(double[] c) {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) {
            return false;
        }
        int cx = net.minecraft.util.Mth.floor(c[0]) >> 4;
        int cz = net.minecraft.util.Mth.floor(c[1]) >> 4;
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                if (!level.getChunkSource().hasChunk(cx + dx, cz + dz)) {
                    return false;
                }
            }
        }
        return true;
    }

    /** Wie lange der längste große Zauber dauert (in Ticks). */
    private static final class MageUltimatesDuration {
        static final int MAX = 90;
    }

    private static void withMage(ServerLevel level, double[] c, java.util.function.Consumer<MageBossEntity> action) {
        BlockPos home = BlockPos.containing(c[0], c[2] + MageTowerBuilder.SANCTUM + 1, c[1]);
        level.getEntitiesOfClass(MageBossEntity.class, new AABB(home).inflate(12)).stream().findFirst().ifPresent(action);
    }

    /** Wo der Spieler im Sanktum steht (Füße, Turmkoordinaten ohne Drehung). */
    private static final double[] STAND = {-4.0, MageTowerBuilder.SANCTUM + 1, 4.4};

    /**
     * Bild aus Sicht des Spielers: zurück an seinen Platz (Stöße und Sog tragen ihn sonst bis an die Wand, und das Bild
     * zeigt nur noch Mauer), zum Magier blicken, kurz warten (bis Brand und Frost auch beim Spieler erloschen sind).
     */
    private static void shot(Deque<Step> steps, double[] c, String name) {
        steps.add(Step.server(level -> level.players().forEach(player -> {
            // Gift, Übelkeit, Lähmung aus dem Kampf: im Bild nur Wirbel und Verzerrung – weg damit
            player.getActiveEffects().stream().filter(e -> !e.getEffect().value().isBeneficial()).map(e -> e.getEffect()).toList()
                    .forEach(player::removeEffect);
            player.setDeltaMovement(net.minecraft.world.phys.Vec3.ZERO);
            player.teleportTo(level, c[0] + STAND[0], c[2] + STAND[1], c[1] + STAND[2], java.util.Set.of(), player.getYRot(), player.getXRot(),
                    true);
        })));
        steps.add(aimAtMage());
        steps.add(Step.waitTicks(2));
        steps.add(aimAtMage());
        steps.add(Step.screenshot(name));
    }

    /** Blick auf den Magier richten (die Kamera ist der Spieler selbst). */
    private static Step aimAtMage() {
        return Step.once(minecraft -> {
            io.github.sirschrolli.kingdomsatwar.client.story.ChapterCardOverlay.clear();
            var server = minecraft.getSingleplayerServer();
            if (server != null) {
                server.execute(() -> server.getPlayerList().getPlayers().forEach(p -> {
                    p.clearFire();
                    p.setTicksFrozen(0);
                }));
            }
            var player = minecraft.player;
            minecraft.level.getEntitiesOfClass(MageBossEntity.class, player.getBoundingBox().inflate(24)).stream().findFirst().ifPresent(mage -> {
                var eye = player.getEyePosition();
                var to = mage.position().add(0, 1.6, 0).subtract(eye);
                float yaw = (float) (-Math.atan2(to.x, to.z) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(to.y, Math.sqrt(to.x * to.x + to.z * to.z)) * 180.0 / Math.PI);
                player.setYRot(yaw);
                player.setXRot(pitch);
                player.setYHeadRot(yaw);
            });
        });
    }

    // =====================================================================================
    // Bauplatz im Biom
    // =====================================================================================

    /** Sucht in Spiralen um den Ursprung einen ebenen Ort im Land des Elements und baut dort den Turm. */
    private static void placeInBiome(ServerLevel level, DragonType element, double[] c) {
        placeInBiome(level, element, c, 0);
    }

    /**
     * Wie oben, die Suche beginnt aber {@code farther} Blöcke weiter draußen (die Kämpfe finden so nicht in einem Turm
     * statt, den eine andere Szene derselben Vorführung schon gebaut hat – samt dessen Magier).
     */
    private static void placeInBiome(ServerLevel level, DragonType element, double[] c, int farther) {
        var generator = level.getChunkSource().getGenerator();
        var randomState = level.getChunkSource().randomState();
        var biomes = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ModTags.Biomes.HAS_MAGE_TOWERS.get(element));
        int sea = generator.getSeaLevel();
        var resolver = generator.getBiomeSource().createUncachedResolver(randomState);
        int start = 900 + element.ordinal() * 700 + farther;
        for (int r = start; r <= start + 9000; r += 48) {
            int count = Math.max(8, r / 40);
            for (int a = 0; a < count; a++) {
                double ang = a * Math.PI * 2 / count + element.ordinal();
                int x = (int) Math.round(Math.cos(ang) * r);
                int z = (int) Math.round(Math.sin(ang) * r);
                int floor = generator.getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, level, randomState);
                Holder<Biome> biome = resolver.getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(floor), QuartPos.fromBlock(z));
                if (!biomes.contains(biome) || floor <= sea + 1) {
                    continue;
                }
                // Marenas Turm für die Bilder an einer offenen Küste (im Mangrovensumpf sähe man vor Bäumen nichts)
                if (element == DragonType.WATER && !biome.is(net.minecraft.tags.BiomeTags.IS_BEACH)) {
                    continue;
                }
                int surface = generator.getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                if (surface > floor) {
                    continue;
                }
                int min = floor;
                int max = floor;
                boolean wet = false;
                for (int[] d : new int[][] {{-10, -10}, {10, -10}, {-10, 10}, {10, 10}, {0, -12}, {0, 12}, {-12, 0}, {12, 0}}) {
                    int h = generator.getBaseHeight(x + d[0], z + d[1], Heightmap.Types.OCEAN_FLOOR_WG, level, randomState);
                    int s = generator.getBaseHeight(x + d[0], z + d[1], Heightmap.Types.WORLD_SURFACE_WG, level, randomState);
                    wet |= s > h;
                    min = Math.min(min, h);
                    max = Math.max(max, h);
                }
                if (wet || max - min > 4) {
                    continue;
                }
                build(level, element, new BlockPos(x, floor, z), c);
                return;
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] kein Bauplatz für den Turm {}", element.id());
        c[3] = -1;
    }

    private static void build(ServerLevel level, DragonType element, BlockPos origin, double[] c) {
        MageTowerPiece piece = new MageTowerPiece(element, origin, Rotation.NONE, 991L + element.ordinal());
        BoundingBox box = piece.getBoundingBox();
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        clearTrees(level, origin.getX(), origin.getZ(), 56);
        // stand hier schon ein Turm (z. B. aus der Szene „towers“), müssen seine Bewohner weichen – sonst gäbe es zwei Magier
        for (var old : level.getEntities((net.minecraft.world.entity.Entity) null, AABB.of(box).inflate(8),
                e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
            old.discard();
        }
        var generator = level.getChunkSource().getGenerator();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                BoundingBox chunkBox = new BoundingBox(cx << 4, level.getMinY(), cz << 4, (cx << 4) + 15, level.getMaxY(), (cz << 4) + 15);
                piece.postProcess(level, level.structureManager(), generator, RandomSource.create(1), chunkBox, new ChunkPos(cx, cz), origin);
            }
        }
        c[0] = origin.getX() + 0.5;
        c[1] = origin.getZ() + 0.5;
        c[2] = origin.getY();
        c[4] = Rotation.NONE.ordinal();
        c[3] = 1;
        KingdomsAtWar.LOGGER.info("[Showcase] Turm {} gebaut bei {} (Biom {})", element.id(), origin,
                level.getBiome(origin).unwrapKey().map(k -> k.identifier().toString()).orElse("?"));
    }

    private static void locateAndGenerate(ServerLevel level, DragonType element, double[] c) {
        long started = System.currentTimeMillis();
        Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.mageTower(element));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder),
                new BlockPos(0, 80, 0), 200, false);
        StructureStart start = found == null ? null : level.getChunk(ChunkPos.containing(found.getFirst()).x(),
                ChunkPos.containing(found.getFirst()).z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
        if (start == null || !start.isValid() || !(start.getPieces().getFirst() instanceof MageTowerPiece piece)) {
            KingdomsAtWar.LOGGER.info("[Showcase] Turm {} nicht gefunden", element.id());
            c[3] = -1;
            return;
        }
        BoundingBox box = piece.getBoundingBox();
        for (int cx = (box.minX() >> 4) - 1; cx <= (box.maxX() >> 4) + 1; cx++) {
            for (int cz = (box.minZ() >> 4) - 1; cz <= (box.maxZ() >> 4) + 1; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        BlockPos origin = piece.origin();
        c[0] = origin.getX() + 0.5;
        c[1] = origin.getZ() + 0.5;
        c[2] = origin.getY();
        c[4] = piece.rotation().ordinal();
        c[3] = 1;
        int mages = level.getEntitiesOfClass(MageBossEntity.class, new AABB(piece.home()).inflate(4)).size();
        KingdomsAtWar.LOGGER.info("[Showcase] Turm {} von der Welt erzeugt bei {} ({}), Biom {}, Magier: {}, {} ms", element.id(), origin,
                piece.rotation(), level.getBiome(origin).unwrapKey().map(k -> k.identifier().toString()).orElse("?"), mages,
                System.currentTimeMillis() - started);
    }

    /** Nur für die Vorführung: Bäume nahe am Turm entfernen, damit die Kamera freie Sicht hat. */
    private static void clearTrees(ServerLevel level, int sx, int sz, int radius) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = (sx - radius) >> 4; cx <= (sx + radius) >> 4; cx++) {
            for (int cz = (sz - radius) >> 4; cz <= (sz + radius) >> 4; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        for (int x = sx - radius; x <= sx + radius; x++) {
            for (int z = sz - radius; z <= sz + radius; z++) {
                if ((x - sx) * (x - sx) + (z - sz) * (z - sz) > radius * radius) {
                    continue;
                }
                int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z);
                for (int y = top + 1; y >= level.getSeaLevel() - 4; y--) {
                    pos.set(x, y, z);
                    BlockState state = level.getBlockState(pos);
                    if (state.is(BlockTags.LOGS) || state.is(BlockTags.LEAVES)) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 2 | 16);
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Kamera
    // =====================================================================================

    private static double[] turn(double x, double z, Rotation rotation) {
        return switch (rotation) {
            case NONE -> new double[] {x, z};
            case CLOCKWISE_90 -> new double[] {-z, x};
            case CLOCKWISE_180 -> new double[] {-x, -z};
            case COUNTERCLOCKWISE_90 -> new double[] {z, -x};
        };
    }

    /** Kamera an einen Standpunkt setzen (berechnet auf dem Server mit dem echten Gelände, dann per Befehl). */
    private static void at(Deque<Step> steps, double[] c, View v) {
        double[] cam = new double[6];
        steps.add(Step.server(level -> camera(level, c, v, cam)));
        steps.add(Step.until(() -> cam[5] != 0, 400));
        steps.add(Step.once(minecraft -> {
            if (c[3] < 0 || cam[5] == 0) {
                return;
            }
            minecraft.options.fov().set(v.fov());
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f",
                    DIM, cam[0], cam[1], cam[2], cam[3], cam[4]));
        }));
    }

    private static void camera(ServerLevel level, double[] c, View v, double[] cam) {
        if (c[3] < 0) {
            cam[5] = 1;
            return;
        }
        Rotation rotation = Rotation.values()[(int) c[4]];
        double ey = v.ey();
        double[] eye = turn(v.ex(), v.ez(), rotation);
        if (v.outside()) {
            // von außen: notfalls um den Turm herumgehen oder höher steigen, bis weder Hügel noch Dünen die Sicht verdecken
            search:
            for (int lift = 0; lift <= 30; lift += 10) {
                for (int turn = 0; turn < 4; turn++) {
                    Rotation r = Rotation.values()[(int) c[4] + turn & 3];
                    double[] e = turn(v.ex(), v.ez(), r);
                    double eyeY = Math.max(ey, surface(level, c[0] + e[0], c[1] + e[1]) - c[2] + 2.0) + lift;
                    if (clearView(level, c, e, eyeY, v.ly()) || lift == 30 && turn == 3) {
                        eye = e;
                        rotation = r;
                        ey = eyeY;
                        break search;
                    }
                }
            }
        }
        double[] look = turn(v.lx(), v.lz(), rotation);
        double lx = look[0] - eye[0];
        double ly = v.ly() - (ey + 1.62);
        double lz = look[1] - eye[1];
        cam[0] = c[0] + eye[0];
        cam[1] = c[2] + ey;
        cam[2] = c[1] + eye[1];
        cam[3] = -Math.atan2(lx, lz) * 180.0 / Math.PI;
        cam[4] = -Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI;
        cam[5] = 1;
    }

    /** Höhe des Geländes samt Dünen und Bäumen (der Chunk wird dafür erzeugt). */
    private static int surface(ServerLevel level, double x, double z) {
        int bx = (int) Math.floor(x);
        int bz = (int) Math.floor(z);
        level.getChunk(bx >> 4, bz >> 4, ChunkStatus.FULL, true);
        return level.getHeight(Heightmap.Types.MOTION_BLOCKING, bx, bz);
    }

    /** Verdeckt das Gelände den Blick vom Standpunkt zur Turmmitte (in Höhe {@code lookY})? */
    private static boolean clearView(ServerLevel level, double[] c, double[] eye, double eyeY, double lookY) {
        for (int i = 1; i < 12; i++) {
            double t = i / 12.0;
            double x = eye[0] * (1 - t);
            double z = eye[1] * (1 - t);
            if (Math.max(Math.abs(x), Math.abs(z)) < 13) {
                break;
            }
            double y = eyeY + 1.62 + (lookY - eyeY - 1.62) * t;
            if (surface(level, c[0] + x, c[1] + z) - c[2] > y) {
                return false;
            }
        }
        return true;
    }
}
