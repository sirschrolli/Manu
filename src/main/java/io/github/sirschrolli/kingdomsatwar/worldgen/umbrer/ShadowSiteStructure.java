package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;

/**
 * Die kleineren Bauwerke der Schattenwelt: verlassene Mondtempel der Umbrer, Kristallminen, Mondleuchttürme an der
 * Küste und Ruinen der alten Welt (aus der Zeit, bevor das Siegel die Welten trennte). Wo ein Bauwerk stehen darf, prüft
 * {@link #findGenerationPoint} am Gelände (eben genug, an der Küste, nicht unter Wasser); gebaut wird es von
 * {@link ShadowSitePiece}.
 */
public class ShadowSiteStructure extends Structure {
    /** Art des Bauwerks: Name, halbe Kantenlänge des Bauplatzes, Tiefe unter und Höhe über dem Boden. */
    public enum Kind implements StringRepresentable {
        TEMPLE("moon_temple", 23, 12, 24, 23),
        MINE("crystal_mine", 36, 36, 14, 8),
        LIGHTHOUSE("lighthouse", 23, 16, 42, 23),
        RUIN("old_world_ruin", 13, 8, 12, 13);

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String id;
        final int radius;
        final int below;
        final int above;
        /** Wie weit das Bauwerk an der Oberfläche reicht (bei der Mine nur der Förderturm). */
        final int surface;

        Kind(String id, int radius, int below, int above, int surface) {
            this.id = id;
            this.radius = radius;
            this.below = below;
            this.above = above;
            this.surface = surface;
        }

        public String id() {
            return this.id;
        }

        @Override
        public String getSerializedName() {
            return this.id;
        }
    }

    public static final MapCodec<ShadowSiteStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            Kind.CODEC.fieldOf("kind").forGetter(s -> s.kind)
    ).apply(instance, ShadowSiteStructure::new));

    private final Kind kind;

    public ShadowSiteStructure(Structure.StructureSettings settings, Kind kind) {
        super(settings);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }

    /** Versuchsorte rund um die Chunkmitte: erst die Mitte, dann je acht Orte im Abstand von 16 und von 32 Blöcken. */
    private static final int[][] RING = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        if (UmbrerVillageStructure.nearCitadelArea(context, chunk, 200)) {
            return Optional.empty();
        }
        UmbrerVillageStructure.Terrain terrain = new UmbrerVillageStructure.Terrain(context);
        long seed = context.random().nextLong();
        int turn = Math.floorMod((int) (seed >>> 11), 8);
        List<BlockPos> villages = villageCandidates(context, chunk);
        int keepAway = VILLAGE_REACH + this.kind.surface + 8;
        // Liegt die Mitte ungünstig (Hang, Wasser, falsches Biom), wird in der Nähe weitergesucht
        for (int i = 0; i < 17; i++) {
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            if (i > 0) {
                int[] dir = RING[(i - 1 + turn) % 8];
                int dist = i <= 8 ? 16 : 32;
                x += dir[0] * dist;
                z += dir[1] * dist;
            }
            if (near(villages, x, z, keepAway)) {
                continue;
            }
            int[] plan = this.plan(terrain, x, z, seed);
            if (plan == null) {
                continue;
            }
            int floor = plan[0];
            BlockPos check = new BlockPos(x, Math.max(floor, terrain.surface(x, z)), z);
            if (!context.validBiome().test(context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(check.getX()),
                    QuartPos.fromBlock(check.getY()), QuartPos.fromBlock(check.getZ())))) {
                continue;
            }
            BlockPos origin = new BlockPos(x, floor, z);
            Rotation rotation = Rotation.values()[plan[1]];
            return Optional.of(new Structure.GenerationStub(check,
                    builder -> builder.addPiece(new ShadowSitePiece(this.kind, origin, rotation, seed))));
        }
        return Optional.empty();
    }

    /** So weit reichen die Wege, Häuser und Wachtürme eines Umbrer-Dorfs von seiner Mitte aus. */
    private static final int VILLAGE_REACH = 52;

    /**
     * Wo in der Nähe ein Dorf der Umbrer entstehen kann (die Mitte des möglichen Dorf-Chunks in diesem und den acht
     * Nachbarfeldern der Dorf-Verteilung). Bauwerke halten davon Abstand, damit sie nie in ein Dorf hineinragen.
     */
    private static List<BlockPos> villageCandidates(Structure.GenerationContext context, ChunkPos chunk) {
        List<BlockPos> list = new ArrayList<>();
        var set = context.registryAccess().lookupOrThrow(Registries.STRUCTURE_SET).get(ModStructures.UMBRER_VILLAGES);
        if (set.isEmpty() || !(set.get().value().placement() instanceof RandomSpreadStructurePlacement placement)) {
            return list;
        }
        int spacing = placement.spacing();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                ChunkPos candidate = placement.getPotentialStructureChunk(context.seed(), chunk.x() + i * spacing, chunk.z() + j * spacing);
                list.add(new BlockPos(candidate.getMiddleBlockX(), 0, candidate.getMiddleBlockZ()));
            }
        }
        return list;
    }

    private static boolean near(List<BlockPos> points, int x, int z, int distance) {
        for (BlockPos p : points) {
            long dx = p.getX() - x;
            long dz = p.getZ() - z;
            if (dx * dx + dz * dz < (long) distance * distance) {
                return true;
            }
        }
        return false;
    }

    /**
     * Passt das Bauwerk mit seiner Mitte bei (x, z)? Gibt Höhe des Bodens (erste Lage über dem Gelände) und Drehung
     * zurück, sonst {@code null}.
     */
    private int @org.jspecify.annotations.Nullable [] plan(UmbrerVillageStructure.Terrain terrain, int x, int z, long seed) {
        int sea = terrain.seaLevel();
        if (terrain.water(x, z) || terrain.ground(x, z) <= sea) {
            return null;
        }
        int rotation = Math.floorMod((int) (seed >>> 7), 4);
        switch (this.kind) {
            case LIGHTHOUSE -> {
                // an der Küste: in einer Richtung muss in 14–26 Blöcken offenes Wasser liegen; dorthin zeigt die Rückseite
                int ground = terrain.ground(x, z);
                if (ground > sea + 12) {
                    return null;
                }
                for (Rotation r : Rotation.values()) {
                    BlockPos back = new BlockPos(0, 0, 1).rotate(r);
                    int wet = 0;
                    for (int d = 14; d <= 26; d += 4) {
                        if (terrain.water(x + back.getX() * d, z + back.getZ() * d)) {
                            wet++;
                        }
                    }
                    if (wet >= 3) {
                        return new int[] {ground + 1, r.ordinal()};
                    }
                }
                return null;
            }
            case MINE -> {
                int[] h = this.sample(terrain, x, z, 5);
                return h[1] - h[0] > 6 ? null : new int[] {h[2] + 1, rotation};
            }
            default -> {
                // der Tempel gleicht mit seinem Sockel etwas aus, die kleine Ruine braucht ebenen Grund
                int[] h = this.sample(terrain, x, z, this.kind == Kind.TEMPLE ? 13 : 7);
                return h[1] - h[0] > (this.kind == Kind.TEMPLE ? 7 : 4) ? null : new int[] {h[2] + 1, rotation};
            }
        }
    }

    /** Höhen (oberster fester Block) auf einem Ring: kleinster, größter und mittlerer Wert. */
    private int[] sample(UmbrerVillageStructure.Terrain terrain, int x, int z, int radius) {
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int sum = 0;
        int n = 0;
        for (int dx = -radius; dx <= radius; dx += radius) {
            for (int dz = -radius; dz <= radius; dz += radius) {
                if (terrain.water(x + dx, z + dz)) {
                    return new int[] {0, 999, 0};
                }
                int h = terrain.ground(x + dx, z + dz);
                min = Math.min(min, h);
                max = Math.max(max, h);
                sum += h;
                n++;
            }
        }
        return new int[] {min, max, Mth.floor(sum / (double) n + 0.5)};
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.SHADOW_SITE.get();
    }
}
