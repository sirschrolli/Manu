package io.github.sirschrolli.kingdomsatwar.worldgen.umbrer;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

/**
 * Die kleineren Bauwerke der Schattenwelt: verlassene Mondtempel der Umbrer, Kristallminen, Mondleuchttürme an der
 * Küste und Ruinen der alten Welt (aus der Zeit, bevor das Siegel die Welten trennte). Wo ein Bauwerk stehen darf, prüft
 * {@link #findGenerationPoint} am Gelände (eben genug, an der Küste, nicht unter Wasser); gebaut wird es von
 * {@link ShadowSitePiece}.
 */
public class ShadowSiteStructure extends Structure {
    /** Art des Bauwerks: Name, halbe Kantenlänge des Bauplatzes, Tiefe unter und Höhe über dem Boden. */
    public enum Kind implements StringRepresentable {
        TEMPLE("moon_temple", 23, 12, 24),
        MINE("crystal_mine", 36, 36, 14),
        LIGHTHOUSE("lighthouse", 23, 16, 42),
        RUIN("old_world_ruin", 13, 8, 12);

        public static final Codec<Kind> CODEC = StringRepresentable.fromEnum(Kind::values);

        private final String id;
        final int radius;
        final int below;
        final int above;

        Kind(String id, int radius, int below, int above) {
            this.id = id;
            this.radius = radius;
            this.below = below;
            this.above = above;
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

    @Override
    protected Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        UmbrerVillageStructure.Terrain terrain = new UmbrerVillageStructure.Terrain(context);
        long seed = context.random().nextLong();
        int sea = terrain.seaLevel();
        if (terrain.water(x, z) || terrain.ground(x, z) <= sea) {
            return Optional.empty();
        }
        Rotation rotation = Rotation.values()[Math.floorMod((int) (seed >>> 7), 4)];
        int floor;
        switch (this.kind) {
            case LIGHTHOUSE -> {
                // an der Küste: in einer Richtung muss in 14–26 Blöcken offenes Wasser liegen; dorthin zeigt die Rückseite
                Rotation toSea = null;
                for (Rotation r : Rotation.values()) {
                    BlockPos back = new BlockPos(0, 0, 1).rotate(r);
                    int wet = 0;
                    for (int d = 14; d <= 26; d += 4) {
                        if (terrain.water(x + back.getX() * d, z + back.getZ() * d)) {
                            wet++;
                        }
                    }
                    if (wet >= 3) {
                        toSea = r;
                        break;
                    }
                }
                int ground = terrain.ground(x, z);
                if (toSea == null || ground > sea + 12) {
                    return Optional.empty();
                }
                rotation = toSea;
                floor = ground + 1;
            }
            case MINE -> {
                int[] h = this.sample(terrain, x, z, 5);
                if (h[1] - h[0] > 6) {
                    return Optional.empty();
                }
                floor = h[2] + 1;
            }
            default -> {
                int r = this.kind == Kind.TEMPLE ? 13 : 7;
                int[] h = this.sample(terrain, x, z, r);
                // der Tempel gleicht mit seinem Sockel etwas aus, die kleine Ruine braucht ebenen Grund
                if (h[1] - h[0] > (this.kind == Kind.TEMPLE ? 7 : 4)) {
                    return Optional.empty();
                }
                floor = h[2] + 1;
            }
        }
        if (UmbrerVillageStructure.nearCitadelArea(context, chunk, 200)) {
            return Optional.empty();
        }
        BlockPos origin = new BlockPos(x, floor, z);
        BlockPos check = new BlockPos(x, Math.max(floor, terrain.surface(x, z)), z);
        Rotation finalRotation = rotation;
        return Optional.of(new Structure.GenerationStub(check,
                builder -> builder.addPiece(new ShadowSitePiece(this.kind, origin, finalRotation, seed))));
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
