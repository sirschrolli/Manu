package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillagePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageBuilding;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillagerEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;

/**
 * Vorführung der Dörfer: {@code village_gallery[_<bauweise>[_<gebäude>]]} baut jedes Gebäude in jeder Bauweise auf
 * flachem Boden und fotografiert es schräg von vorn; {@code villages[_<bauweise>]} sucht in einer echten Welt je ein Dorf
 * und zeigt es von oben und aus den Gassen.
 */
final class VillageShowcase {
    private VillageShowcase() {}

    static void gallery(Deque<Step> steps, int[] ground, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        // erst einmal ankommen: die erste Aufnahme braucht geladene Chunks rundum
        steps.add(Step.command(String.format(Locale.ROOT, "tp @s %d %d %d 0 30", 5000, ground[0] + 20, 4970)));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(60));
        int index = 0;
        for (VillageStyle style : VillageStyle.values()) {
            for (VillageBuilding kind : VillageBuilding.values()) {
                if (kind == VillageBuilding.MARKET_STALL) {
                    continue;
                }
                String name = style.id() + "_" + kind.id();
                if (!only.isEmpty() && !name.startsWith(only) && !style.id().equals(only)) {
                    continue;
                }
                int x = 5000 + index * 48;
                int z = 5000;
                index++;
                steps.add(Step.command(String.format(Locale.ROOT, "tp @s %d %d %d 0 30", x, ground[0] + 20, z - 30)));
                steps.add(Step.waitTicks(10));
                steps.add(Step.waitForChunks(400));
                steps.add(Step.server(level -> {
                    BlockPos origin = new BlockPos(x - kind.width / 2, ground[0] - 1, z);
                    prepare(level, style, origin, kind);
                    if (style == VillageStyle.STILT) {
                        // Pfahlbau: der Boden liegt zwei Blöcke über dem Wasser
                        origin = origin.above(2);
                    }
                    KingdomVillagePiece piece = KingdomVillagePiece.single(style, index(style), kind, origin, Rotation.NONE, 31L * kind.ordinal() + 7);
                    build(level, piece);
                }));
                steps.add(Step.waitTicks(30));
                // schräg von vorn links, leicht von oben
                double cx = x;
                double cz = z + kind.depth / 2.0;
                double dist = Math.max(kind.width, kind.depth) * 0.95 + Math.max(4.0, kind.height * 0.45);
                double yaw = -32.0;
                double lx = -Math.sin(Math.toRadians(yaw));
                double lz = Math.cos(Math.toRadians(yaw));
                double camX = cx - lx * dist;
                double camZ = cz - lz * dist;
                double camY = ground[0] + Math.min(kind.height, 16) * 0.42 + 2.5;
                double pitch = Math.toDegrees(Math.atan2(camY - (ground[0] + kind.height * 0.4), dist));
                steps.add(Step.command(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", camX, camY, camZ, yaw, pitch)));
                steps.add(Step.waitTicks(25));
                steps.add(Step.waitForChunks(400));
                steps.add(Step.screenshot("village_" + name));
                // und von hinten rechts
                steps.add(Step.command(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", cx + lx * dist, camY, cz + lz * dist,
                        yaw + 180.0, pitch)));
                steps.add(Step.waitTicks(15));
                steps.add(Step.screenshot("village_" + name + "_back"));
            }
        }
    }

    /** Spielart je Bauweise in der Galerie (zeigt nebeneinander verschiedene Dachfarben). */
    private static int index(VillageStyle style) {
        return 0;
    }

    /** Boden: Gras (Fachwerk, nordisch), Sand (Lehm), ein flacher Weiher über Schlamm (Pfahlbau). */
    private static void prepare(ServerLevel level, VillageStyle style, BlockPos origin, VillageBuilding kind) {
        BlockState top = switch (style) {
            case TIMBER -> Blocks.GRASS_BLOCK.defaultBlockState();
            case NORDIC -> Blocks.PODZOL.defaultBlockState();
            case CLAY -> Blocks.SAND.defaultBlockState();
            case STILT -> Blocks.MUD.defaultBlockState();
        };
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int r = 22;
        int cx = origin.getX() + kind.width / 2;
        int cz = origin.getZ() + kind.depth / 2;
        for (int dx = -r; dx <= r; dx++) {
            for (int dz = -r - 6; dz <= r; dz++) {
                for (int dy = -6; dy <= 40; dy++) {
                    pos.set(cx + dx, origin.getY() + dy, cz + dz);
                    BlockState state;
                    if (dy > 0) {
                        state = Blocks.AIR.defaultBlockState();
                    } else if (dy == 0) {
                        state = style == VillageStyle.STILT ? Blocks.WATER.defaultBlockState() : top;
                    } else if (dy == -1 && style == VillageStyle.STILT) {
                        state = Blocks.WATER.defaultBlockState();
                    } else if (dy == -6) {
                        state = Blocks.STONE.defaultBlockState();
                    } else {
                        state = style == VillageStyle.CLAY ? Blocks.SANDSTONE.defaultBlockState()
                                : style == VillageStyle.STILT ? Blocks.MUD.defaultBlockState() : Blocks.DIRT.defaultBlockState();
                    }
                    level.setBlock(pos, state, 2);
                }
            }
        }
        // die Fischerhütte steht am Ufer: hinter ihr Wasser (zwei Blöcke tief)
        if (kind == VillageBuilding.FISHING_HUT) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = 6; dz <= r; dz++) {
                    for (int dy = -3; dy <= 0; dy++) {
                        pos.set(origin.getX() + kind.width / 2 + dx, origin.getY() + dy, origin.getZ() + dz);
                        level.setBlock(pos, dy == -3 ? Blocks.SAND.defaultBlockState() : dy == 0 ? Blocks.AIR.defaultBlockState()
                                : Blocks.WATER.defaultBlockState(), 2);
                    }
                }
            }
        }
        // ein Stück Weg vor der Tür (wie im Dorf)
        for (int dx = -kind.width / 2 - 3; dx <= kind.width / 2 + 3; dx++) {
            for (int dz = -4; dz <= -2; dz++) {
                pos.set(cx + dx, origin.getY(), origin.getZ() + dz);
                level.setBlock(pos, style == VillageStyle.STILT ? Blocks.SPRUCE_PLANKS.defaultBlockState()
                        : style == VillageStyle.CLAY ? Blocks.SMOOTH_SANDSTONE.defaultBlockState() : Blocks.DIRT_PATH.defaultBlockState(), 2);
            }
        }
        for (var old : level.getEntities((net.minecraft.world.entity.Entity) null, new AABB(origin).inflate(30),
                e -> !(e instanceof net.minecraft.world.entity.player.Player))) {
            old.discard();
        }
    }

    private static void build(ServerLevel level, KingdomVillagePiece piece) {
        BoundingBox box = piece.getBoundingBox();
        var generator = level.getChunkSource().getGenerator();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        // in einem Zug (in der fertigen Welt rissen Nachbar-Updates sonst Betten an Chunkgrenzen auseinander)
        BoundingBox all = new BoundingBox(box.minX(), level.getMinY(), box.minZ(), box.maxX(), level.getMaxY(), box.maxZ());
        piece.postProcess(level, level.structureManager(), generator, RandomSource.create(1), all, ChunkPos.containing(piece.origin()), piece.origin());
    }

    // =====================================================================================
    // Echte Dörfer
    // =====================================================================================

    /** Sucht je Bauweise das nächste Dorf, erzeugt es und fotografiert es von oben und von der Straße. */
    static void villages(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(12)));
        steps.add(Step.command("time set 5000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        for (VillageStyle style : VillageStyle.values()) {
            if (!only.isEmpty() && !style.id().equals(only)) {
                continue;
            }
            double[] c = new double[6];
            steps.add(Step.server(level -> {
                io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure.debug(
                        line -> KingdomsAtWar.LOGGER.info("[Showcase] Planung {}", line));
                locate(level, style, c);
                io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure.debug(null);
            }));
            steps.add(Step.until(() -> c[3] != 0, 4000));
            steps.add(Step.once(minecraft -> {
                if (c[3] > 0 && minecraft.player != null) {
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f 0 90", c[0], c[2] + 60, c[1]));
                }
            }));
            steps.add(Step.waitTicks(40));
            steps.add(Step.waitForChunks(1200));
            steps.add(Step.waitTicks(60));
            steps.add(Step.screenshot("villages_" + style.id() + "_top"));
            // schräger Überblick aus Südwesten und Nordosten
            for (int k = 0; k < 2; k++) {
                final int kk = k;
                steps.add(Step.once(minecraft -> {
                    if (c[3] > 0 && minecraft.player != null) {
                        double yaw = kk == 0 ? -135.0 : 45.0;
                        double lx = -Math.sin(Math.toRadians(yaw));
                        double lz = Math.cos(Math.toRadians(yaw));
                        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f 32",
                                c[0] - lx * 58, c[2] + 40, c[1] - lz * 58, yaw));
                    }
                }));
                steps.add(Step.waitTicks(80));
                steps.add(Step.waitForChunks(1200));
                steps.add(Step.waitTicks(40));
                steps.add(Step.screenshot("villages_" + style.id() + "_view" + k));
            }
            // vom Rand des Platzes die Straßen entlang (Augenhöhe)
            for (int k = 0; k < 4; k++) {
                final int kk = k;
                steps.add(Step.once(minecraft -> {
                    if (c[3] > 0 && minecraft.player != null) {
                        double yaw = kk * 90.0;
                        double lx = -Math.sin(Math.toRadians(yaw));
                        double lz = Math.cos(Math.toRadians(yaw));
                        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f 4",
                                c[0] + 0.5 + lx * 5 + lz * 1.5, c[2] + 2.8, c[1] + 0.5 + lz * 5 - lx * 1.5, yaw));
                    }
                }));
                steps.add(Step.waitTicks(25));
                steps.add(Step.waitForChunks(600));
                steps.add(Step.screenshot("villages_" + style.id() + "_street" + k));
            }
        }
    }

    // =====================================================================================
    // Dorfbewohner
    // =====================================================================================

    /**
     * Alle Dorfbewohner auf drei Stufen (je Gesicht eine Reihe, die Frauen hinten oben): von vorn mit ihrem Werkzeug,
     * dann von hinten (Köcher, Taschen, Gugeln) und schräg aus der Nähe.
     */
    static void villagers(Deque<Step> steps, int[] ground) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(8)));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        int ox = 7000;
        int oz = 5000;
        steps.add(tp(() -> String.format(Locale.ROOT, "tp @s %d %d %d 0 20", ox + 15, ground[0] + 10, oz - 20)));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(600));
        List<VillagerEntity> lineup = new java.util.ArrayList<>();
        steps.add(Step.server(level -> {
            int g = ground[0];
            BlockState grass = Blocks.SPRUCE_PLANKS.defaultBlockState();
            BlockState border = Blocks.STRIPPED_DARK_OAK_LOG.defaultBlockState().setValue(net.minecraft.world.level.block.RotatedPillarBlock.AXIS,
                    net.minecraft.core.Direction.Axis.X);
            BlockState riser = Blocks.STONE_BRICKS.defaultBlockState();
            BlockState step = Blocks.POLISHED_ANDESITE.defaultBlockState();
            BlockState wall = Blocks.SPRUCE_PLANKS.defaultBlockState();
            BlockState post = Blocks.STRIPPED_SPRUCE_LOG.defaultBlockState();
            for (int x = ox - 6; x <= ox + 38; x++) {
                for (int z = oz - 14; z <= oz + 8; z++) {
                    for (int y = g; y <= g + 8; y++) {
                        level.setBlock(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState(), 2);
                    }
                    level.setBlock(new BlockPos(x, g - 1, z), (z - oz) % 4 == 3 ? border : grass, 2);
                    int row = (z - oz) / 2;
                    if (z >= oz + 1 && row >= 1 && row <= 2 && x >= ox - 1 && x <= ox + 33) {
                        for (int y = g; y < g + row; y++) {
                            level.setBlock(new BlockPos(x, y, z), y == g + row - 1 ? step : riser, 2);
                        }
                    }
                    if (z == oz + 6 && x >= ox - 2 && x <= ox + 34) {
                        for (int y = g; y <= g + 5; y++) {
                            level.setBlock(new BlockPos(x, y, z), (x - ox) % 6 == 0 ? post : wall, 2);
                        }
                    }
                }
            }
            for (VillageProfession profession : VillageProfession.values()) {
                int i = profession.ordinal();
                int half = i < 9 ? 0 : 1;
                int k = half == 0 ? i : i - 9;
                for (int v = 0; v < 3; v++) {
                    VillagerEntity villager = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.COMMAND);
                    villager.setup(profession, v, profession.id(), null, "", null, null, 1L);
                    villager.setNoAi(true);
                    villager.setWorking(true);
                    villager.setItemSlot(EquipmentSlot.MAINHAND, profession.workItem());
                    double x = ox + half * 18 + 0.75 + k * 1.5;
                    double z = oz + v * 2 + 0.5;
                    villager.snapTo(x, g + v, z, 180.0f, 0.0f);
                    villager.setYBodyRot(180.0f);
                    villager.setYHeadRot(180.0f);
                    level.addFreshEntity(villager);
                    lineup.add(villager);
                }
            }
        }));
        steps.add(Step.waitTicks(40));
        for (int half = 0; half < 2; half++) {
            double cx = ox + half * 18 + 6.75;
            steps.add(tp(() -> String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f 0 6", cx, ground[0] + 2.5, oz - 6.9)));
            steps.add(Step.waitTicks(30));
            steps.add(Step.waitForChunks(300));
            steps.add(Step.once(minecraft -> KingdomsAtWar.LOGGER.info("[Showcase] Kamera {} Boden {} Sichtfeld {}", minecraft.player.position(),
                    ground[0], minecraft.options.fov().get())));
            steps.add(Step.screenshot("villagers_front_" + (half + 1)));
        }
        // Bildnisse: je drei Berufe vor einer Holzwand, aus der Nähe (Gesichter abwechselnd)
        steps.add(Step.server(level -> {
            int g = ground[0];
            for (int x = ox + 40; x <= ox + 92; x++) {
                for (int z = oz - 12; z <= oz + 3; z++) {
                    for (int y = g; y <= g + 6; y++) {
                        level.setBlock(new BlockPos(x, y, z), z == oz + 2 && y <= g + 4
                                ? ((x - ox) % 4 == 0 ? Blocks.STRIPPED_SPRUCE_LOG : Blocks.SPRUCE_PLANKS).defaultBlockState()
                                : Blocks.AIR.defaultBlockState(), 2);
                    }
                    level.setBlock(new BlockPos(x, g - 1, z), Blocks.SPRUCE_PLANKS.defaultBlockState(), 2);
                }
            }
            for (VillageProfession profession : VillageProfession.values()) {
                int i = profession.ordinal();
                int group = i / 3;
                int k = i % 3;
                VillagerEntity villager = ModEntities.VILLAGER.get().create(level, EntitySpawnReason.COMMAND);
                int variant = (i + group) % 3;
                villager.setup(profession, variant, profession.id(), null, "", null, null, 1L);
                villager.setNoAi(true);
                villager.setWorking(true);
                villager.setItemSlot(EquipmentSlot.MAINHAND, profession.workItem());
                villager.snapTo(ox + 44 + group * 8 + k * 1.4 + 0.5, g, oz + 0.3, 180.0f, 0.0f);
                villager.setYBodyRot(165.0f + k * 15.0f);
                villager.setYHeadRot(170.0f + k * 10.0f);
                level.addFreshEntity(villager);
            }
        }));
        steps.add(Step.waitTicks(20));
        for (int group = 0; group < 6; group++) {
            double cx = ox + 44 + group * 8 + 1.4 + 0.5;
            steps.add(tp(() -> String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f 0 9", cx, ground[0] + 0.2, oz - 2.5)));
            steps.add(Step.waitTicks(15));
            steps.add(Step.waitForChunks(200));
            steps.add(Step.screenshot("villagers_group_" + (group + 1)));
        }
        // umdrehen: von hinten
        steps.add(Step.server(level -> lineup.forEach(v -> {
            v.setYRot(0.0f);
            v.setYBodyRot(0.0f);
            v.setYHeadRot(0.0f);
        })));
        steps.add(Step.waitTicks(10));
        for (int half = 0; half < 2; half++) {
            double cx = ox + half * 18 + 6.75;
            steps.add(tp(() -> String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f 0 6", cx, ground[0] + 2.5, oz - 6.9)));
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot("villagers_back_" + (half + 1)));
        }
    }

    /**
     * Ein Tag in einem echten Dorf: Arbeit in den Werkstätten, der Nachmittag auf dem Platz, die Nacht in den Betten,
     * dann der Handel mit dem Schreiber und was sich die Leute erzählen.
     */
    static void life(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(10)));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.command("time set 2000"));
        double[] c = new double[6];
        List<KingdomVillagePiece> pieces = new java.util.ArrayList<>();
        steps.add(Step.server(level -> {
            locate(level, VillageStyle.TIMBER, c);
            if (c[3] > 0) {
                ChunkPos chunk = ChunkPos.containing(BlockPos.containing(c[0], c[2], c[1]));
                Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.village(VillageStyle.TIMBER));
                StructureStart start = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
                if (start != null) {
                    start.getPieces().forEach(piece -> pieces.add((KingdomVillagePiece) piece));
                }
            }
        }));
        steps.add(Step.until(() -> c[3] != 0, 4000));
        steps.add(Step.once(minecraft -> {
            if (c[3] > 0 && minecraft.player != null) {
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f 0 60", c[0], c[2] + 30, c[1] - 10));
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1200));
        // Vormittag: alle gehen an die Arbeit
        steps.add(Step.waitTicks(700));
        // die Werkstätten in einer festen Reihenfolge (was das Dorf nicht hat, wird übersprungen)
        List<String> prefer = List.of("smithy", "bakery", "masonry", "tannery", "bowyer", "herb_house", "map_house", "scriptorium", "butchery",
                "field", "sheepfold", "tavern");
        for (int shot = 0; shot < 6; shot++) {
            final int index = shot;
            steps.add(Step.once(minecraft -> {
                List<KingdomVillagePiece> order = pieces.stream().filter(p -> p.building() != null && prefer.contains(p.building().id()))
                        .sorted(java.util.Comparator.comparingInt(p -> prefer.indexOf(p.building().id()))).toList();
                if (index >= order.size() || minecraft.player == null) {
                    return;
                }
                KingdomVillagePiece piece = order.get(index);
                VillageBuilding kind = piece.building();
                BlockPos eye = world(piece, kind.width / 2, 3, -5);
                BlockPos look = world(piece, kind.width / 2, 1, Math.min(3, kind.depth / 2));
                double dx = look.getX() - eye.getX();
                double dz = look.getZ() - eye.getZ();
                double yaw = Math.toDegrees(Math.atan2(-dx, dz));
                double pitch = Math.toDegrees(Math.atan2(eye.getY() + 1.62 - look.getY(), Math.sqrt(dx * dx + dz * dz)));
                KingdomsAtWar.LOGGER.info("[Showcase] Werkstatt {}: {}", index + 1, kind.id());
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f %.1f", eye.getX() + 0.5, eye.getY() + 0.0,
                        eye.getZ() + 0.5, yaw, pitch));
            }));
            steps.add(Step.waitTicks(40));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.screenshot("life_work_" + (shot + 1)));
        }
        // Nachmittag auf dem Platz
        steps.add(Step.command("time set 9800"));
        steps.add(Step.waitTicks(900));
        for (int k = 0; k < 2; k++) {
            final int kk = k;
            steps.add(Step.once(minecraft -> {
                if (c[3] > 0 && minecraft.player != null) {
                    double yaw = kk == 0 ? -140.0 : 40.0;
                    double lx = -Math.sin(Math.toRadians(yaw));
                    double lz = Math.cos(Math.toRadians(yaw));
                    minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f %.1f 30", c[0] + 0.5 - lx * 13,
                            c[2] + 9, c[1] + 0.5 - lz * 13, yaw));
                }
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("life_square_" + (k + 1)));
        }
        // Nacht: wer schläft wo?
        steps.add(Step.command("time set 13500"));
        steps.add(Step.waitTicks(1200));
        double[] sleeper = new double[5];
        steps.add(Step.server(level -> {
            AABB area = new AABB(c[0] - 80, c[2] - 30, c[1] - 80, c[0] + 80, c[2] + 40, c[1] + 80);
            List<VillagerEntity> all = level.getEntitiesOfClass(VillagerEntity.class, area);
            long sleeping = all.stream().filter(VillagerEntity::isSleeping).count();
            KingdomsAtWar.LOGGER.info("[Showcase] Nacht: {} von {} Dorfbewohnern schlafen", sleeping, all.size());
            for (VillagerEntity v : all) {
                if (!v.isSleeping()) {
                    KingdomsAtWar.LOGGER.info("[Showcase]   wach: {} ({}) bei {}, Bett {}, Haustür {}, Ziele {}", v.getName().getString(), v.getProfession().id(),
                            v.blockPosition(), v.getBed(), v.getFrontDoor(), v.runningGoals());
                    for (BlockPos target : java.util.Arrays.asList(v.getFrontDoor(), v.getBed())) {
                        if (target == null) {
                            continue;
                        }
                        var path = v.getNavigation().createPath(target, 1);
                        KingdomsAtWar.LOGGER.info("[Showcase]     Weg nach {}: {}", target, path == null ? "keiner"
                                : (path.canReach() ? "erreicht" : "endet bei " + path.getTarget() + " / " + path.getEndNode()) + ", " + path.getNodeCount() + " Schritte");
                    }
                }
            }
            for (VillagerEntity v : all) {
                if (!v.isSleeping() || v.getBed() == null) {
                    continue;
                }
                // vom Platz zum Aufstehen neben dem Bett aus (der liegt sicher im Raum) auf den Schläfer schauen
                BlockPos bed = v.getBed();
                var state = level.getBlockState(bed);
                if (!(state.getBlock() instanceof net.minecraft.world.level.block.AbstractBedBlock)) {
                    continue;
                }
                var spot = net.minecraft.world.level.block.AbstractBedBlock.findStandUpPosition(v.getType(), level, bed,
                        state.getValue(net.minecraft.world.level.block.BedBlock.FACING), 0.0f);
                if (spot.isEmpty() || level.canSeeSky(BlockPos.containing(spot.get()).above())) {
                    continue;
                }
                double dx = v.getX() - spot.get().x;
                double dz = v.getZ() - spot.get().z;
                sleeper[0] = spot.get().x - dx * 0.4;
                sleeper[1] = spot.get().y + 0.15;
                sleeper[2] = spot.get().z - dz * 0.4;
                sleeper[3] = Math.toDegrees(Math.atan2(-dx, dz));
                sleeper[4] = 1;
                KingdomsAtWar.LOGGER.info("[Showcase] Schläfer: {} ({}) im Bett bei {}", v.getName().getString(), v.getProfession().id(), bed);
                break;
            }
        }));
        steps.add(Step.until(() -> sleeper[4] != 0, 100));
        steps.add(Step.once(minecraft -> {
            if (sleeper[4] != 0 && minecraft.player != null) {
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f 40", sleeper[0], sleeper[1], sleeper[2],
                        sleeper[3]));
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("life_night_bed"));
        steps.add(Step.once(minecraft -> {
            if (c[3] > 0 && minecraft.player != null) {
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f -140 25", c[0] + 9, c[2] + 8, c[1] - 9));
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("life_night_square"));
        // Morgen: Handel mit dem Schreiber (verzauberte Bücher) und was er erzählt
        steps.add(Step.command("time set 4000"));
        steps.add(Step.waitTicks(200));
        int[] trader = {-1};
        steps.add(Step.server(level -> {
            AABB area = new AABB(c[0] - 80, c[2] - 30, c[1] - 80, c[0] + 80, c[2] + 40, c[1] + 80);
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            VillagerEntity best = null;
            int bestScore = -1;
            for (VillagerEntity v : level.getEntitiesOfClass(VillagerEntity.class, area)) {
                // am liebsten ein Händler unter freiem Himmel (fürs Bild), am allerliebsten der Schreiber
                int score = (v.canTrade() ? 1 : 0) + (level.canSeeSky(v.blockPosition().above()) ? 2 : 0)
                        + (v.getProfession() == VillageProfession.SCRIBE ? 4 : 0);
                if (score > bestScore) {
                    best = v;
                    bestScore = score;
                }
            }
            if (best != null) {
                trader[0] = best.getId();
                best.setNoAi(true);
                player.teleportTo(best.getX() + 1.5, best.getY(), best.getZ() + 1.5);
                io.github.sirschrolli.kingdomsatwar.economy.Coins.give(player, 5000);
                io.github.sirschrolli.kingdomsatwar.economy.citizen.Trading.open(player, best);
            }
        }));
        steps.add(Step.until(() -> Minecraft.getInstance().gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.town.TradeScreen, 200));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("life_trade"));
        steps.add(Step.once(minecraft -> minecraft.gui.setScreen(null)));
        // was man sich erzählt: im Chat, mit dem Dorfplatz im Hintergrund
        steps.add(Step.once(minecraft -> {
            if (c[3] > 0 && minecraft.player != null) {
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.1f %.1f %.1f -140 18", c[0] + 8, c[2] + 5, c[1] - 8));
            }
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.FULL)));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            if (level.getEntity(trader[0]) instanceof VillagerEntity v) {
                for (int i = 0; i < 4; i++) {
                    io.github.sirschrolli.kingdomsatwar.entity.villager.VillageRumors.talk(player, v);
                }
            }
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("life_rumors"));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.hud(false));
    }

    /** Teleportiert den Spieler mit einem Befehl, der erst beim Ausführen zusammengesetzt wird (Höhen sind dann bekannt). */
    private static Step tp(java.util.function.Supplier<String> command) {
        return Step.once(minecraft -> {
            if (minecraft.player != null) {
                minecraft.player.connection.sendCommand(command.get());
            }
        });
    }

    /** Weltposition eines Punkts im Rahmen eines Gebäudes (vorn ist z = 0). */
    private static BlockPos world(KingdomVillagePiece piece, int x, int y, int z) {
        return piece.origin().offset(new BlockPos(x, y, z).rotate(piece.rotation()));
    }

    // =====================================================================================
    // Planungsstatistik
    // =====================================================================================

    /**
     * Plant (ohne einen Chunk zu erzeugen) alle Dörfer im Umkreis von {@code regions} Rasterfeldern um den Ursprung und
     * schreibt eine Übersicht ins Protokoll: wie viele Orte taugen, wie viele Gebäude welcher Art entstehen, wie lang die
     * Wege werden und woran Bauplätze scheitern.
     */
    static void stats(Deque<Step> steps, int regions) {
        steps.add(Step.server(level -> {
            var registry = level.registryAccess().lookupOrThrow(Registries.STRUCTURE);
            var cache = level.getChunkSource();
            var state = cache.getGeneratorState();
            java.util.Map<String, Integer> kinds = new java.util.TreeMap<>();
            java.util.Map<String, Integer> reasons = new java.util.TreeMap<>();
            java.util.Map<VillageStyle, int[]> perStyle = new java.util.EnumMap<>(VillageStyle.class);
            List<String> summaries = new java.util.ArrayList<>();
            io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure.debug(line -> {
                if (line.startsWith("map ")) {
                    if (summaries.size() <= 3) {
                        KingdomsAtWar.LOGGER.info("[Showcase] Plan {}", line);
                    }
                    return;
                }
                if (line.startsWith("village ")) {
                    summaries.add(line);
                    int k = line.indexOf("kinds ");
                    int r = line.indexOf(" rejected ");
                    if (k >= 0 && r > k) {
                        for (String kind : line.substring(k + 6, r).split(",")) {
                            if (!kind.isBlank()) {
                                kinds.merge(kind, 1, Integer::sum);
                            }
                        }
                    }
                } else {
                    reasons.merge(line.split(":")[0], 1, Integer::sum);
                }
            });
            long started = System.currentTimeMillis();
            var sampler = cache.randomState().createClimateSampler(
                    net.minecraft.world.level.levelgen.densityfunction.SamplerContext.builder().enableCaches().build());
            for (VillageStyle style : VillageStyle.values()) {
                Holder<Structure> holder = registry.getOrThrow(ModStructures.village(style));
                var placement = state.getPlacementsForStructure(holder).stream()
                        .filter(p -> p instanceof net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement).findFirst();
                if (placement.isEmpty()) {
                    continue;
                }
                var spread = (net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement) placement.get();
                int[] counts = new int[2];
                for (int rx = -regions; rx <= regions; rx++) {
                    for (int rz = -regions; rz <= regions; rz++) {
                        ChunkPos chunk = spread.getPotentialStructureChunk(state.getLevelSeed(), rx * spread.spacing(), rz * spread.spacing());
                        if (!spread.isStructureChunk(state, chunk.x(), chunk.z())) {
                            continue;
                        }
                        var context = new Structure.GenerationContext(level.registryAccess(), cache.getGenerator(), cache.getGenerator().getBiomeSource(),
                                sampler, cache.randomState(), level.getStructureTemplateManager(), level.getSeed(), chunk, level,
                                holder.value().biomes()::contains);
                        if (!context.validBiome().test(level.getUncachedNoiseBiome(chunk.getMiddleBlockX() >> 2, 16, chunk.getMiddleBlockZ() >> 2))) {
                            continue;
                        }
                        counts[0]++;
                        if (holder.value().findValidGenerationPoint(context).isPresent()) {
                            counts[1]++;
                        }
                    }
                }
                perStyle.put(style, counts);
            }
            io.github.sirschrolli.kingdomsatwar.worldgen.village.KingdomVillageStructure.debug(null);
            KingdomsAtWar.LOGGER.info("[Showcase] Statistik ({} ms):", System.currentTimeMillis() - started);
            perStyle.forEach((style, n) -> KingdomsAtWar.LOGGER.info("[Showcase]   {}: {} Orte im passenden Land, {} Dörfer", style.id(), n[0], n[1]));
            summaries.forEach(line -> KingdomsAtWar.LOGGER.info("[Showcase]   {}", line));
            KingdomsAtWar.LOGGER.info("[Showcase]   Gebäude: {}", kinds);
            KingdomsAtWar.LOGGER.info("[Showcase]   abgelehnte Orte: {}", reasons);
        }));
        steps.add(Step.waitTicks(20));
    }

    private static void locate(ServerLevel level, VillageStyle style, double[] c) {
        long started = System.currentTimeMillis();
        Holder<Structure> holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.village(style));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), new BlockPos(0, 80, 0), 300,
                false);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] Dorf {} nicht gefunden", style.id());
            c[3] = -1;
            return;
        }
        ChunkPos chunk = ChunkPos.containing(found.getFirst());
        StructureStart start = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
        if (start == null || !start.isValid()) {
            c[3] = -1;
            return;
        }
        BoundingBox box = start.getBoundingBox();
        // das Dorf und seine Umgebung (für die Aufnahmen aus der Ferne)
        for (int cx = (box.minX() >> 4) - 4; cx <= (box.maxX() >> 4) + 4; cx++) {
            for (int cz = (box.minZ() >> 4) - 4; cz <= (box.maxZ() >> 4) + 4; cz++) {
                level.getChunk(cx, cz, ChunkStatus.FULL, true);
            }
        }
        KingdomVillagePiece square = (KingdomVillagePiece) start.getPieces().getFirst();
        BlockPos center = square.center();
        c[0] = center.getX();
        c[1] = center.getZ();
        c[2] = center.getY();
        c[3] = 1;
        KingdomsAtWar.LOGGER.info("[Showcase] Dorf {} „{}“ bei {}: {} Teile, Fläche {}×{}, {} ms", style.id(), square.villageName(), center,
                start.getPieces().size(), box.getXSpan(), box.getZSpan(), System.currentTimeMillis() - started);
        for (var piece : start.getPieces()) {
            if (piece instanceof KingdomVillagePiece p && p.building() != null) {
                KingdomsAtWar.LOGGER.info("[Showcase]   {} bei {} ({}), Stufe {}", p.building().id(), p.origin(), p.rotation(), p.step());
            }
        }
    }
}
