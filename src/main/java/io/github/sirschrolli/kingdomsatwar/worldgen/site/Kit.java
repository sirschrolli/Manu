package io.github.sirschrolli.kingdomsatwar.worldgen.site;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.VillageBuilder;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.TrapDoorBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.resources.ResourceKey;
import org.jspecify.annotations.Nullable;

/**
 * Werkzeugkasten der Bauwerke von 2.0 (sechs je Dimension): Holzarten als Satz, Einebnen und schwebende Felsen, Wände,
 * Fenster, Möbel und Kleinkram, Verfall, Bewohner – und die Planung (ebenes Land, freier Himmel, Meeresgrund, Wasserspiegel,
 * Lavameer, Höhlengrund der Unterwelt). Alle Koordinaten in eigenen Koordinaten des {@link VillageBuilder} (gedreht).
 */
final class Kit {
    private Kit() {}

    static BlockState s(Block block) {
        return block.defaultBlockState();
    }

    /** Ein Block aus dem Register (Vanilla oder Mod); fehlt er, {@code fallback}. */
    static Block block(String id, Block fallback) {
        Identifier key = id.contains(":") ? Identifier.parse(id) : Identifier.withDefaultNamespace(id);
        return BuiltInRegistries.BLOCK.getOptional(key).orElse(fallback);
    }

    static Block mod(String name, Block fallback) {
        return BuiltInRegistries.BLOCK.getOptional(KingdomsAtWar.id(name)).orElse(fallback);
    }

    // =====================================================================================
    // Holz
    // =====================================================================================

    /** Ein Holz als Satz: Stamm, geschälter Stamm, Holz (mit Rinde), Bretter, Treppe, Stufe, Zaun, Tor, Tür, Falltür, Druckplatte, Regal. */
    record Wood(Block log, Block stripped, Block bark, Block planks, Block stairs, Block slab, Block fence, Block gate, Block door, Block trapdoor,
            Block plate, Block shelf) {
        /** Ein Vanilla-Holz ({@code stem}: Karmesin und Wirr mit Stiel und Hyphen). */
        static Wood vanilla(String name, boolean stem) {
            String log = name + (stem ? "_stem" : "_log");
            return new Wood(block(log, Blocks.OAK_LOG), block("stripped_" + log, Blocks.STRIPPED_OAK_LOG),
                    block(name + (stem ? "_hyphae" : "_wood"), Blocks.OAK_WOOD), block(name + "_planks", Blocks.OAK_PLANKS),
                    block(name + "_stairs", Blocks.OAK_STAIRS), block(name + "_slab", Blocks.OAK_SLAB), block(name + "_fence", Blocks.OAK_FENCE),
                    block(name + "_fence_gate", Blocks.OAK_FENCE_GATE), block(name + "_door", Blocks.OAK_DOOR),
                    block(name + "_trapdoor", Blocks.OAK_TRAPDOOR), block(name + "_pressure_plate", Blocks.OAK_PRESSURE_PLATE),
                    block(name + "_shelf", Blocks.OAK_SHELF));
        }

        /** Ein Holz der Mod (Schattenholz, Glimmerholz): Bretter als Baustoffsatz ("…_plank_stairs"). */
        static Wood mod(String name, Wood fallback) {
            return new Wood(Kit.mod(name + "_log", fallback.log), Kit.mod("stripped_" + name + "_log", fallback.stripped),
                    Kit.mod(name + "_wood", fallback.bark), Kit.mod(name + "_planks", fallback.planks), Kit.mod(name + "_plank_stairs", fallback.stairs),
                    Kit.mod(name + "_plank_slab", fallback.slab), Kit.mod(name + "_fence", fallback.fence), Kit.mod(name + "_fence_gate", fallback.gate),
                    Kit.mod(name + "_door", fallback.door), Kit.mod(name + "_trapdoor", fallback.trapdoor), fallback.plate, fallback.shelf);
        }

        BlockState logY() {
            return this.log.defaultBlockState();
        }

        BlockState log(Direction.Axis axis) {
            BlockState state = this.log.defaultBlockState();
            return state.hasProperty(RotatedPillarBlock.AXIS) ? state.setValue(RotatedPillarBlock.AXIS, axis) : state;
        }

        BlockState stair(Direction facing, Half half) {
            return VillageBuilder.stairs(this.stairs, facing, half);
        }

        BlockState slab(SlabType type) {
            return this.slab.defaultBlockState().setValue(SlabBlock.TYPE, type);
        }
    }

    static Wood oak() {
        return Wood.vanilla("oak", false);
    }

    static Wood spruce() {
        return Wood.vanilla("spruce", false);
    }

    static Wood darkOak() {
        return Wood.vanilla("dark_oak", false);
    }

    // =====================================================================================
    // Gelände
    // =====================================================================================

    /**
     * Eine runde Fläche (Halbmesser {@code r}) wird eben: darunter bis zum festen Grund aufgefüllt ({@code fill}, oben
     * {@code surface}), darüber {@code clear} Blöcke hoch alles fort – auch Bäume. Am Rand (bis r + 2) nur noch das Grün.
     */
    static void pad(VillageBuilder b, double r, BlockState surface, BlockState fill, int clear) {
        int R = (int) Math.ceil(r) + 5;
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                double d = Math.sqrt(x * x + z * z) + (b.noise(x, 91, z) - 0.5) * 1.2;
                if (d > r + 5) {
                    continue;
                }
                if (d <= r) {
                    column(b, x, z, surface, fill, clear);
                    continue;
                }
                // der Rand: eine Böschung vom ebenen Platz zum Gelände (fünf Blöcke breit), Bäume und Büsche fort
                int natural = groundAt(b, x, z, 14);
                if (natural == Integer.MIN_VALUE) {
                    continue;
                }
                double t = (d - r) / 5.0;
                int target = (int) Math.round(natural * t * t * (3 - 2 * t));
                if (target < natural) {
                    for (int y = target; y < natural; y++) {
                        b.set(x, y, z, Blocks.AIR.defaultBlockState());
                    }
                    b.set(x, target - 1, z, surface);
                } else if (target > natural) {
                    for (int y = natural; y < target - 1; y++) {
                        b.set(x, y, z, fill);
                    }
                    b.set(x, target - 1, z, surface);
                }
                // nahe am Platz auch Bäume und Büsche fort
                if (t < 0.6) {
                    for (int y = target; y <= target + clear; y++) {
                        BlockState old = b.get(x, y, z);
                        if (old.is(BlockTags.LOGS) || old.is(BlockTags.LEAVES) || old.is(BlockTags.REPLACEABLE) || old.is(BlockTags.FLOWERS)) {
                            b.set(x, y, z, Blocks.AIR.defaultBlockState());
                        }
                    }
                }
            }
        }
    }

    /** Wo in der Säule (x, z) der gewachsene Boden liegt: die Höhe des ersten freien Blocks darüber (oder MIN_VALUE). */
    static int groundAt(VillageBuilder b, int x, int z, int range) {
        if (!b.here(x, 0, z)) {
            return Integer.MIN_VALUE;
        }
        for (int y = range; y >= -range; y--) {
            BlockState state = b.get(x, y, z);
            if (VillageBuilder.isSolidGround(state) || state.is(BlockTags.DIRT) || state.is(BlockTags.SAND)) {
                return y + 1;
            }
        }
        return Integer.MIN_VALUE;
    }

    /** Ein Rechteck wird eben (wie {@link #pad}). */
    static void rect(VillageBuilder b, int x0, int z0, int x1, int z1, BlockState surface, BlockState fill, int clear) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                column(b, x, z, surface, fill, clear);
            }
        }
    }

    /** Eine Säule: unter y = 0 bis zum festen Grund auffüllen (höchstens 24 tief), oben {@code surface}; darüber frei. */
    static void column(VillageBuilder b, int x, int z, BlockState surface, BlockState fill, int clear) {
        for (int y = -1; y > -24; y--) {
            BlockState old = b.get(x, y, z);
            if (y < -1 && VillageBuilder.isSolidGround(old) && !old.is(BlockTags.LOGS)) {
                break;
            }
            b.set(x, y, z, y == -1 ? surface : fill);
        }
        for (int y = 0; y < clear; y++) {
            if (!b.get(x, y, z).isAir()) {
                b.set(x, y, z, Blocks.AIR.defaultBlockState());
            }
        }
    }

    /**
     * Ein schwebender Fels, Oberseite bei y = -1 ({@code top}, darunter zwei Lagen {@code under}), nach unten spitz zulaufend
     * ({@code depth} tief in der Mitte), der Rand zerfranst; im Gestein mit {@code chance} der Zierstein {@code accent}.
     */
    static void floatingRock(VillageBuilder b, int cx, int cz, double r, int depth, BlockState top, BlockState under, BlockState rock,
            @Nullable BlockState accent, float chance) {
        int R = (int) Math.ceil(r) + 1;
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                double d = Math.sqrt(x * x + z * z) + (b.noise(cx + x, 7, cz + z) - 0.5) * 1.8;
                if (d > r) {
                    continue;
                }
                double k = d / r;
                int down = 1 + (int) Math.round((depth - 1) * Math.pow(1.0 - k, 1.25) + (b.noise(cx + x, 3, cz + z) - 0.5) * 2.4);
                b.set(cx + x, -1, cz + z, top);
                for (int dy = 2; dy <= Math.max(1, down); dy++) {
                    BlockState state = dy <= 3 ? under : accent != null && b.chance(cx + x, -dy, cz + z, chance) ? accent : rock;
                    b.set(cx + x, -dy, cz + z, state);
                }
            }
        }
    }

    /** Räumt eine Kugel frei (ein „Auge“ im Chaos des Abgrunds oder zwischen den Bruchstücken der Leere). */
    static void clearBall(VillageBuilder b, int cx, int cy, int cz, double r) {
        int R = (int) Math.ceil(r);
        for (int x = -R; x <= R; x++) {
            for (int y = -R; y <= R; y++) {
                for (int z = -R; z <= R; z++) {
                    if (x * x + y * y + z * z <= r * r && !b.get(cx + x, cy + y, cz + z).isAir()) {
                        b.set(cx + x, cy + y, cz + z, Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Bauteile
    // =====================================================================================

    /** Vier Wände (nur der Rand) von y0 bis y1; an den Ecken {@code corner}. */
    static void walls(VillageBuilder b, int x0, int z0, int x1, int z1, int y0, int y1, BlockState wall, BlockState corner) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edgeX = x == x0 || x == x1;
                    boolean edgeZ = z == z0 || z == z1;
                    if (edgeX || edgeZ) {
                        b.set(x, y, z, edgeX && edgeZ ? corner : wall);
                    }
                }
            }
        }
    }

    /** Fachwerk: Wände aus {@code fill}, Balken ({@code beam}) an Ecken, unten, oben und alle {@code every} Blöcke. */
    static void timber(VillageBuilder b, int x0, int z0, int x1, int z1, int y0, int y1, BlockState fill, Wood wood, int every) {
        for (int y = y0; y <= y1; y++) {
            for (int x = x0; x <= x1; x++) {
                for (int z = z0; z <= z1; z++) {
                    boolean edgeX = x == x0 || x == x1;
                    boolean edgeZ = z == z0 || z == z1;
                    if (!edgeX && !edgeZ) {
                        continue;
                    }
                    boolean post = edgeX && edgeZ || (edgeZ && (x - x0) % every == 0) || (edgeX && (z - z0) % every == 0);
                    if (post) {
                        b.set(x, y, z, wood.logY());
                    } else if (y == y0 || y == y1) {
                        b.set(x, y, z, wood.log(edgeZ ? Direction.Axis.X : Direction.Axis.Z));
                    } else {
                        b.set(x, y, z, fill);
                    }
                }
            }
        }
    }

    /** Boden (eine Lage) von (x0, z0) bis (x1, z1) in Höhe y. */
    static void floor(VillageBuilder b, int x0, int z0, int x1, int z1, int y, BlockState state) {
        b.fill(x0, y, z0, x1, y, z1, state);
    }

    /** Eine runde Wand (Turm) um (cx, cz) mit Halbmesser {@code r} von y0 bis y1; innen leer. */
    static void roundWall(VillageBuilder b, int cx, int cz, double r, int y0, int y1, BlockState wall) {
        int R = (int) Math.ceil(r) + 1;
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                double d = Math.sqrt(x * x + z * z);
                if (d <= r + 0.5 && d > r - 0.5) {
                    for (int y = y0; y <= y1; y++) {
                        b.set(cx + x, y, cz + z, wall);
                    }
                }
            }
        }
    }

    /** Eine runde Scheibe (Boden) um (cx, cz). */
    static void disc(VillageBuilder b, int cx, int cz, double r, int y, BlockState state) {
        int R = (int) Math.ceil(r);
        for (int x = -R; x <= R; x++) {
            for (int z = -R; z <= R; z++) {
                if (x * x + z * z <= r * r + 0.5) {
                    b.set(cx + x, y, cz + z, state);
                }
            }
        }
    }

    /** Ein Kegeldach über einem runden Turm: Ring um Ring nach innen aus {@code roof}, oben {@code tip}. */
    static void cone(VillageBuilder b, int cx, int cz, double r, int y, int height, BlockState roof, BlockState tip) {
        for (int k = 0; k < height; k++) {
            double rr = r * (1.0 - (double) k / height) + 0.2;
            int R = (int) Math.ceil(rr) + 1;
            for (int x = -R; x <= R; x++) {
                for (int z = -R; z <= R; z++) {
                    double d = Math.sqrt(x * x + z * z);
                    if (d <= rr + 0.35 && d > rr - 1.2) {
                        b.set(cx + x, y + k, cz + z, roof);
                    }
                }
            }
        }
        b.set(cx, y + height, cz, tip);
    }

    /** Eine Fensteröffnung aus Scheiben {@code pane} (y bis y + h - 1). */
    static void window(VillageBuilder b, int x, int y, int z, int h, BlockState pane) {
        for (int i = 0; i < h; i++) {
            b.set(x, y + i, z, pane);
        }
    }

    /** Eine Laterne, auf dem Boden ({@code hanging} falsch) oder hängend (mit Kette bis zum Halt). */
    static void lantern(VillageBuilder b, int x, int y, int z, Block lantern, boolean hanging) {
        if (hanging) {
            b.hangingLantern(x, y, z, lantern);
        } else {
            b.set(x, y, z, lantern.defaultBlockState());
        }
    }

    // =====================================================================================
    // Möbel und Kleinkram
    // =====================================================================================

    /** Ein Tisch: Zaunpfosten mit einer Druckplatte darauf. */
    static void table(VillageBuilder b, int x, int y, int z, Wood wood) {
        b.set(x, y, z, wood.fence);
        b.set(x, y + 1, z, wood.plate);
    }

    /** Ein Stuhl: eine Treppe, deren Lehne von {@code facing} weg zeigt (man sitzt mit Blick nach {@code facing}). */
    static void chair(VillageBuilder b, int x, int y, int z, Wood wood, Direction facing) {
        b.set(x, y, z, VillageBuilder.stairs(wood.stairs, facing.getOpposite(), Half.BOTTOM));
    }

    /** Ein Fass, dessen Deckel nach {@code facing} zeigt. */
    static void barrel(VillageBuilder b, int x, int y, int z, Direction facing) {
        b.set(x, y, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, facing));
    }

    /** Eine Truhe (Vorderseite nach {@code facing}) mit Beute. */
    static void chest(VillageBuilder b, int x, int y, int z, Direction facing, ResourceKey<LootTable> loot) {
        b.chest(x, y, z, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, facing), loot);
    }

    /** Ein Fass mit Beute (Deckel nach oben). */
    static void lootBarrel(VillageBuilder b, int x, int y, int z, ResourceKey<LootTable> loot) {
        b.chest(x, y, z, Blocks.BARREL.defaultBlockState().setValue(BarrelBlock.FACING, Direction.UP), loot);
    }

    /** Ein Block mit Blickrichtung (Ofen, Räucherofen, Werkbank-artiges), zeigt nach {@code facing}. */
    static void facing(VillageBuilder b, int x, int y, int z, Block block, Direction facing) {
        BlockState state = block.defaultBlockState();
        if (state.hasProperty(HorizontalDirectionalBlock.FACING)) {
            state = state.setValue(HorizontalDirectionalBlock.FACING, facing);
        }
        b.set(x, y, z, state);
    }

    /** Kerzen (1–4) einer Farbe, angezündet oder nicht. */
    static void candles(VillageBuilder b, int x, int y, int z, Block candle, int count, boolean lit) {
        b.set(x, y, z, candle.defaultBlockState().setValue(CandleBlock.CANDLES, Math.clamp(count, 1, 4)).setValue(CandleBlock.LIT, lit));
    }

    /** Ein Lagerfeuer (oder Seelenfeuer), brennend oder aus. */
    static void campfire(VillageBuilder b, int x, int y, int z, Block fire, boolean lit) {
        b.set(x, y, z, fire.defaultBlockState().setValue(CampfireBlock.LIT, lit));
    }

    /** Eine Falltür an der Wand, aufgeklappt (als Fensterladen oder Brett), zeigt nach {@code facing}. */
    static void shutter(VillageBuilder b, int x, int y, int z, Block trapdoor, Direction facing) {
        b.set(x, y, z, trapdoor.defaultBlockState().setValue(TrapDoorBlock.FACING, facing).setValue(TrapDoorBlock.OPEN, true));
    }

    /** Eine Falltür flach oben in einem Block (Tischplatte, Deckel). */
    static void lid(VillageBuilder b, int x, int y, int z, Block trapdoor) {
        b.set(x, y, z, trapdoor.defaultBlockState().setValue(TrapDoorBlock.HALF, Half.TOP));
    }

    /** Pflanze nur, wo Platz ist und ein Halt darunter. */
    static void plant(VillageBuilder b, int x, int y, int z, BlockState state) {
        if (b.get(x, y, z).isAir()) {
            b.set(x, y, z, state);
        }
    }

    // =====================================================================================
    // Verfall
    // =====================================================================================

    /** Ein Block mit Verfall: mit {@code moss} bemoost, mit {@code crack} rissig, sonst wie er ist. */
    static BlockState weathered(VillageBuilder b, int x, int y, int z, BlockState normal, BlockState mossy, float moss, BlockState cracked,
            float crack) {
        float n = b.noise(x * 7 + 3, y * 5 + 1, z * 11 + 7);
        return n < moss ? mossy : n < moss + crack ? cracked : normal;
    }

    /** Bricht oben an Mauern etwas weg: Blöcke ab {@code from} fehlen mit wachsender Wahrscheinlichkeit nach oben. */
    static void crumble(VillageBuilder b, int x0, int z0, int x1, int z1, int from, int to, float chance) {
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = from; y <= to; y++) {
                    float p = chance * (y - from + 1) / (float) (to - from + 1);
                    if (b.chance(x, y, z, p)) {
                        for (int yy = y; yy <= to; yy++) {
                            if (!b.get(x, yy, z).isAir()) {
                                b.set(x, yy, z, Blocks.AIR.defaultBlockState());
                            }
                        }
                        break;
                    }
                }
            }
        }
    }

    /** Spinnweben in Ecken (mit {@code chance} je Platz, nur in Luft). */
    static void cobwebs(VillageBuilder b, int x0, int y0, int z0, int x1, int y1, int z1, float chance) {
        for (int x = x0; x <= x1; x++) {
            for (int y = y0; y <= y1; y++) {
                for (int z = z0; z <= z1; z++) {
                    if (b.get(x, y, z).isAir() && b.chance(x, y + 300, z, chance)) {
                        b.set(x, y, z, Blocks.COBWEB.defaultBlockState());
                    }
                }
            }
        }
    }

    // =====================================================================================
    // Bewohner
    // =====================================================================================

    /** Ein Wesen an seinem Platz (Füße bei y, mittig im Block, Blick nach {@code facing}); es bleibt (verschwindet nicht). */
    static @Nullable Mob mob(VillageBuilder b, EntityType<? extends Mob> type, int x, int y, int z, Direction facing) {
        BlockPos pos = b.world(x, y, z);
        if (!b.inside(pos)) {
            return null;
        }
        Mob mob = type.create(b.level.getLevel(), EntitySpawnReason.STRUCTURE);
        if (mob == null) {
            return null;
        }
        float yaw = b.worldDirection(facing).toYRot();
        mob.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, yaw, 0.0f);
        mob.setYHeadRot(yaw);
        mob.setYBodyRot(yaw);
        mob.finalizeSpawn(b.level, b.level.getCurrentDifficultyAt(pos), EntitySpawnReason.STRUCTURE, null);
        mob.setPersistenceRequired();
        b.level.addFreshEntityWithPassengers(mob);
        return mob;
    }

    // =====================================================================================
    // Planung
    // =====================================================================================

    static Rotation rotation(long seed) {
        return Rotation.values()[Math.floorMod((int) (seed >>> 9), 4)];
    }

    static Optional<Structure.GenerationStub> stub(SitePiece piece) {
        return Optional.of(new Structure.GenerationStub(piece.origin(), builder -> builder.addPiece(piece)));
    }

    /**
     * Ebenes, trockenes Land unter der Mitte des Chunks: Höhenunterschied höchstens {@code maxDiff} im Umkreis {@code r},
     * im passenden Biom. Gibt den ersten freien Block über dem Boden zurück (oder null).
     */
    static @Nullable BlockPos land(Structure.GenerationContext context, SiteTerrain terrain, int r, int maxDiff) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int ground = terrain.ground(x, z);
        if (terrain.water(x, z) || ground < terrain.minY() + 8) {
            return null;
        }
        int[] span = terrain.span(x, z, r, Math.max(2, r / 3));
        if (span[1] - span[0] > maxDiff || span[0] < terrain.minY() + 8) {
            return null;
        }
        for (int[] d : new int[][] {{r, 0}, {-r, 0}, {0, r}, {0, -r}}) {
            if (terrain.water(x + d[0], z + d[1])) {
                return null;
            }
        }
        if (!terrain.valid(x, ground + 1, z)) {
            return null;
        }
        // in der Mitte des Spielraums: so steht nichts in der Luft und nichts ist tief vergraben
        int y = Math.round((span[0] + span[1] + 1) / 2.0f) + 1;
        return new BlockPos(x, Math.max(y, ground + 1 - maxDiff / 2), z);
    }

    /**
     * Ein Platz am Himmel (für schwebende Bauwerke): Höhe aus dem Samen zwischen {@code lo} und {@code hi}; im Umkreis
     * {@code r} höchstens {@code maxRock} Proben in festem Fels (der Rest wird beim Bau freigeräumt); im passenden Biom.
     */
    static @Nullable BlockPos sky(Structure.GenerationContext context, SiteTerrain terrain, long seed, int lo, int hi, int r, int maxRock) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int y = lo + Math.floorMod((int) ((seed * 0x9E3779B97F4A7C15L) >>> 40), Math.max(1, hi - lo + 1));
        if (!terrain.valid(x, y, z)) {
            return null;
        }
        int rock = 0;
        for (int dx = -r; dx <= r; dx += Math.max(2, r / 3)) {
            for (int dz = -r; dz <= r; dz += Math.max(2, r / 3)) {
                for (int dy = -r / 2; dy <= r / 2; dy += Math.max(2, r / 4)) {
                    if (terrain.rock(x + dx, y + dy, z + dz)) {
                        rock++;
                    }
                }
            }
        }
        return rock > maxRock ? null : new BlockPos(x, y, z);
    }

    /**
     * Meeresgrund unter der Mitte des Chunks: mindestens {@code minDepth} Wasser darüber, Grund im Umkreis {@code r} höchstens
     * {@code maxDiff} uneben. Gibt den ersten Block über dem Grund (im Wasser) zurück.
     */
    static @Nullable BlockPos seabed(Structure.GenerationContext context, SiteTerrain terrain, int r, int maxDiff, int minDepth) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        if (terrain.waterDepth(x, z) < minDepth) {
            return null;
        }
        int[] span = terrain.span(x, z, r, Math.max(2, r / 3));
        if (span[1] - span[0] > maxDiff) {
            return null;
        }
        for (int[] d : new int[][] {{r, 0}, {-r, 0}, {0, r}, {0, -r}}) {
            if (terrain.waterDepth(x + d[0], z + d[1]) < minDepth - maxDiff) {
                return null;
            }
        }
        int ground = terrain.ground(x, z);
        if (!terrain.valid(x, ground + 1, z)) {
            return null;
        }
        return new BlockPos(x, (span[0] + span[1]) / 2 + 1, z);
    }

    /** Offenes Wasser an der Mitte des Chunks (mindestens {@code minDepth} tief im Umkreis {@code r}): der Wasserspiegel (y). */
    static @Nullable BlockPos openWater(Structure.GenerationContext context, SiteTerrain terrain, int r, int minDepth) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        for (int[] d : new int[][] {{0, 0}, {r, 0}, {-r, 0}, {0, r}, {0, -r}, {r, r}, {-r, -r}, {r, -r}, {-r, r}}) {
            if (terrain.waterDepth(x + d[0], z + d[1]) < minDepth) {
                return null;
            }
        }
        int surface = terrain.surface(x, z);
        if (!terrain.valid(x, surface - 1, z)) {
            return null;
        }
        return new BlockPos(x, surface, z);
    }

    /**
     * Ein Höhlengrund der Unterwelt: unter der Mitte des Chunks, von einer Höhe aus dem Samen abwärts, der erste Boden über dem
     * Lavameer, über dem {@code headroom} Blöcke frei sind und um den herum (Umkreis {@code r}) Boden in ähnlicher Höhe liegt
     * (höchstens {@code maxDiff} Unterschied, darüber Luft); im passenden Biom. Gibt den ersten freien Block zurück.
     */
    static @Nullable BlockPos cavern(Structure.GenerationContext context, SiteTerrain terrain, long seed, int r, int maxDiff, int headroom) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int sea = terrain.seaLevel();
        int top = Math.min(terrain.minY() + context.heightAccessor().getHeight(), 128) - 6 - headroom;
        if (top <= sea + 4) {
            return null;
        }
        for (int attempt = 0; attempt < 4; attempt++) {
            int start = top - Math.floorMod((int) (seed >>> (11 + attempt * 7)), Math.max(1, top - sea - 4));
            int floor = Integer.MIN_VALUE;
            boolean air = false;
            for (int y = start; y > sea + 1; y--) {
                boolean free = !terrain.rock(x, y, z) && !terrain.fluid(x, y, z);
                air |= free;
                if (air && free && terrain.rock(x, y - 1, z)) {
                    floor = y;
                    break;
                }
            }
            if (floor == Integer.MIN_VALUE || !terrain.valid(x, floor, z)) {
                continue;
            }
            boolean open = true;
            for (int y = floor; y < floor + headroom && open; y++) {
                open = !terrain.rock(x, y, z) && !terrain.fluid(x, y, z);
            }
            if (!open) {
                continue;
            }
            int misses = 0;
            for (int[] d : new int[][] {{r, 0}, {-r, 0}, {0, r}, {0, -r}, {r * 7 / 10, r * 7 / 10}, {-r * 7 / 10, r * 7 / 10}, {r * 7 / 10, -r * 7 / 10},
                    {-r * 7 / 10, -r * 7 / 10}, {r / 2, 0}, {-r / 2, 0}, {0, r / 2}, {0, -r / 2}}) {
                int found = Integer.MIN_VALUE;
                for (int y = floor + maxDiff; y >= floor - maxDiff; y--) {
                    if (!terrain.rock(x + d[0], y, z + d[1]) && !terrain.fluid(x + d[0], y, z + d[1]) && terrain.rock(x + d[0], y - 1, z + d[1])) {
                        found = y;
                        break;
                    }
                }
                if (found == Integer.MIN_VALUE || terrain.rock(x + d[0], found + 4, z + d[1])) {
                    misses++;
                }
            }
            if (misses <= 2) {
                return new BlockPos(x, floor, z);
            }
        }
        return null;
    }

    /**
     * Offenes Lavameer an der Mitte des Chunks: im Umkreis {@code r} überall Lava unter dem Meeresspiegel und Luft darüber,
     * über der Mitte {@code clear} Blöcke frei; im passenden Biom. Gibt den ersten Block über der Lava zurück.
     */
    static @Nullable BlockPos lavaSea(Structure.GenerationContext context, SiteTerrain terrain, int r, int clear) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int sea = terrain.seaLevel();
        if (!terrain.valid(x, sea, z)) {
            return null;
        }
        for (int[] d : new int[][] {{0, 0}, {r, 0}, {-r, 0}, {0, r}, {0, -r}, {r * 7 / 10, r * 7 / 10}, {-r * 7 / 10, -r * 7 / 10},
                {r * 7 / 10, -r * 7 / 10}, {-r * 7 / 10, r * 7 / 10}}) {
            if (!terrain.fluid(x + d[0], sea - 1, z + d[1]) || terrain.rock(x + d[0], sea + 1, z + d[1]) || terrain.rock(x + d[0], sea + 3, z + d[1])) {
                return null;
            }
        }
        for (int y = sea; y < sea + clear; y++) {
            if (terrain.rock(x, y, z)) {
                return null;
            }
        }
        return new BlockPos(x, sea, z);
    }

    /** Ein Schlüssel für die Beute einer Truhe eines Bauwerks von 2.0. */
    static ResourceKey<LootTable> loot(String name) {
        return io.github.sirschrolli.kingdomsatwar.loot.ModLootTables.wonder(name);
    }
}
