package io.github.sirschrolli.kingdomsatwar.worldgen.realm;

import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Arrays;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Die Stammburg eines Reichs. Wo sie steht, bestimmt {@link RealmCastlePlacement}; welches Reich dort herrscht, liest sie
 * bei {@link RealmLocator} nach. Der Hof liegt auf der mittleren Geländehöhe des Bauplatzes, das Tor zeigt zum
 * Weltursprung (von dort kommen die Spieler). Gebaut wird sie von {@link RealmCastlePiece}.
 */
public class RealmCastleStructure extends Structure {
    public static final MapCodec<RealmCastleStructure> CODEC = simpleCodec(RealmCastleStructure::new);

    public RealmCastleStructure(Structure.StructureSettings settings) {
        super(settings);
    }

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        Realm realm = RealmLocator.at(context.randomState(), chunk.x(), chunk.z());
        if (realm == null) {
            // von Hand gesetzt (/place): ein Reich nach dem Ort
            realm = Realm.values()[Math.floorMod(chunk.x() * 31 + chunk.z() * 17, Realm.values().length)];
        }
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        int[] heights = new int[81];
        int n = 0;
        for (int dx = -32; dx <= 32; dx += 8) {
            for (int dz = -32; dz <= 32; dz += 8) {
                heights[n++] = context.chunkGenerator().getBaseHeight(x + dx, z + dz, Heightmap.Types.WORLD_SURFACE_WG,
                        context.heightAccessor(), context.randomState());
            }
        }
        Arrays.sort(heights);
        int floor = Math.max(heights[40] - 1, context.chunkGenerator().getSeaLevel() + 1);
        BlockPos center = new BlockPos(x, floor, z);
        Rotation rotation = facingOrigin(x, z);
        long seed = context.random().nextLong();
        Realm chosen = realm;
        return Optional.of(new Structure.GenerationStub(center,
                builder -> builder.addPiece(new RealmCastlePiece(chosen, center, rotation, seed))));
    }

    /** Die Drehung, mit der das Tor (eigene Koordinaten: Süden) zum Weltursprung zeigt. */
    static Rotation facingOrigin(int x, int z) {
        int dx = -x;
        int dz = -z;
        if (Math.abs(dz) >= Math.abs(dx)) {
            return dz >= 0 ? Rotation.NONE : Rotation.CLOCKWISE_180;
        }
        return dx > 0 ? Rotation.COUNTERCLOCKWISE_90 : Rotation.CLOCKWISE_90;
    }

    @Override
    public StructureType<?> type() {
        return ModStructures.REALM_CASTLE.get();
    }
}
