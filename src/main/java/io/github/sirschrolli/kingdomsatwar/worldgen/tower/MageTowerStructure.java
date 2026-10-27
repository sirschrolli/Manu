package io.github.sirschrolli.kingdomsatwar.worldgen.tower;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.QuartPos;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import org.jspecify.annotations.Nullable;

/**
 * Turm eines Erzmagiers – je Element einer, im passenden Land: Ignatius in Wüsten und Tafelbergen, Isolde im Schnee,
 * Fulgor auf den windigen Höhen, Sylvana in den Wäldern, Gorvan in Savanne, Steppe und Taiga, Marena an Küsten und Sümpfen.
 * Der Turm braucht festen, einigermaßen ebenen Grund (mit seiner Terrasse gleicht er etwas aus); liegt die Chunkmitte
 * ungünstig, wird in der Nähe weitergesucht. Marenas Turm steht zudem nur dort, wo Wasser in der Nähe ist.
 */
public class MageTowerStructure extends Structure {
    public static final MapCodec<MageTowerStructure> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
            settingsCodec(instance),
            DragonType.CODEC.fieldOf("element").forGetter(s -> s.element)
    ).apply(instance, MageTowerStructure::new));

    /** Versuchsorte: die Chunkmitte, dann je acht Orte im Abstand von 16 und von 32 Blöcken. */
    private static final int[][] RING = {{1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}};

    private final DragonType element;

    public MageTowerStructure(Structure.StructureSettings settings, DragonType element) {
        super(settings);
        this.element = element;
    }

    public DragonType element() {
        return this.element;
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        Terrain terrain = new Terrain(context);
        long seed = context.random().nextLong();
        int turn = Math.floorMod((int) (seed >>> 11), 8);
        for (int i = 0; i < 17; i++) {
            int x = chunk.getMiddleBlockX();
            int z = chunk.getMiddleBlockZ();
            if (i > 0) {
                int[] dir = RING[(i - 1 + turn) % 8];
                int dist = i <= 8 ? 16 : 32;
                x += dir[0] * dist;
                z += dir[1] * dist;
            }
            // erst das Biom (billig), dann das Gelände (teuer) – so bleibt auch /locate flink
            int ground = terrain.ground(x, z);
            if (!context.validBiome().test(context.biomeResolver().getNoiseBiome(QuartPos.fromBlock(x), QuartPos.fromBlock(ground + 1),
                    QuartPos.fromBlock(z)))) {
                continue;
            }
            Integer floor = this.plan(terrain, x, z);
            if (floor == null) {
                continue;
            }
            BlockPos check = new BlockPos(x, floor, z);
            Rotation rotation = this.facing(terrain, x, z, seed);
            BlockPos origin = new BlockPos(x, floor, z);
            return Optional.of(new Structure.GenerationStub(check,
                    builder -> builder.addPiece(new MageTowerPiece(this.element, origin, rotation, seed))));
        }
        return Optional.empty();
    }

    /**
     * Passt der Turm mit seiner Mitte bei (x, z)? Gibt die Höhe der Terrasse zurück (eine Lage über dem mittleren
     * Gelände), sonst {@code null}.
     */
    private @Nullable Integer plan(Terrain terrain, int x, int z) {
        int sea = terrain.seaLevel();
        if (terrain.water(x, z)) {
            return null;
        }
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int sum = 0;
        int n = 0;
        for (int k = 0; k < 13; k++) {
            int dx = 0;
            int dz = 0;
            if (k > 0) {
                double a = (k - 1) * Math.PI / 6.0;
                double r = k % 2 == 0 ? 10.0 : 6.0;
                dx = (int) Math.round(Math.cos(a) * r);
                dz = (int) Math.round(Math.sin(a) * r);
            }
            if (terrain.water(x + dx, z + dz)) {
                return null;
            }
            int h = terrain.ground(x + dx, z + dz);
            min = Math.min(min, h);
            max = Math.max(max, h);
            sum += h;
            n++;
        }
        if (max - min > 6) {
            return null;
        }
        int floor = Mth.floor(sum / (double) n + 0.5) + 1;
        if (floor <= sea + 1) {
            return null;
        }
        if (this.element == DragonType.WATER && !this.nearWater(terrain, x, z)) {
            return null;
        }
        return floor;
    }

    /** Marenas Turm: in 16 bis 40 Blöcken Entfernung muss Wasser liegen. */
    private boolean nearWater(Terrain terrain, int x, int z) {
        for (int k = 0; k < 16; k++) {
            double a = k * Math.PI / 8.0;
            for (int r = 16; r <= 40; r += 8) {
                if (terrain.water(x + (int) Math.round(Math.cos(a) * r), z + (int) Math.round(Math.sin(a) * r))) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Die Tür zeigt zur offenen, tiefer gelegenen Seite (sonst zufällig). */
    private Rotation facing(Terrain terrain, int x, int z, long seed) {
        Rotation best = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        int lowest = Integer.MAX_VALUE;
        for (Rotation r : Rotation.values()) {
            BlockPos front = new BlockPos(0, 0, -14).rotate(r);
            int h = terrain.ground(x + front.getX(), z + front.getZ());
            if (h < lowest - 1) {
                lowest = h;
                best = r;
            }
        }
        return best;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.MAGE_TOWER.get();
    }

    /** Geländehöhen aus dem Weltgenerator (ohne Bäume), zwischengespeichert. */
    private static final class Terrain {
        private final Structure.GenerationContext context;
        private final Map<Long, int[]> cache = new HashMap<>();

        Terrain(Structure.GenerationContext context) {
            this.context = context;
        }

        private int[] column(int x, int z) {
            return this.cache.computeIfAbsent(BlockPos.asLong(x, 0, z), key -> new int[] {
                    this.context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.OCEAN_FLOOR_WG, this.context.heightAccessor(),
                            this.context.randomState()),
                    this.context.chunkGenerator().getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, this.context.heightAccessor(),
                            this.context.randomState())});
        }

        int ground(int x, int z) {
            return this.column(x, z)[0] - 1;
        }

        boolean water(int x, int z) {
            int[] c = this.column(x, z);
            return c[1] > c[0];
        }

        int seaLevel() {
            return this.context.chunkGenerator().getSeaLevel();
        }
    }
}
