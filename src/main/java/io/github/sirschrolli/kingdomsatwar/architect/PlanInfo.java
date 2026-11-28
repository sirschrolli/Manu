package io.github.sirschrolli.kingdomsatwar.architect;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.Material;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

/**
 * Was man über einen eigenen Bauplan wissen muss, ohne ihn ganz zu laden: Name, Zeichner, Größe, Eingang, Betten,
 * Kosten und zwei kleine Bilder (Grundriss und Ansicht). Steht im Verzeichnis der Baupläne, auf dem Bauplan-Gegenstand
 * und geht an das Stadtfenster.
 */
public record PlanInfo(String id, String name, UUID author, String authorName, long created, Shape shape, Costs costs, Pictures pictures) {

    /** Größe (Gitter), Lagen unter der Bodenlinie, Eingang und Betten (lokal). */
    public record Shape(int sizeX, int sizeY, int sizeZ, int ground, BlockPos entrance, List<BlockPos> beds) {
        static final Codec<Shape> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("x").forGetter(Shape::sizeX),
                Codec.INT.fieldOf("y").forGetter(Shape::sizeY),
                Codec.INT.fieldOf("z").forGetter(Shape::sizeZ),
                Codec.INT.optionalFieldOf("ground", 0).forGetter(Shape::ground),
                BlockPos.CODEC.fieldOf("entrance").forGetter(Shape::entrance),
                BlockPos.CODEC.listOf().optionalFieldOf("beds", List.of()).forGetter(Shape::beds)
        ).apply(i, Shape::new));
        static final StreamCodec<RegistryFriendlyByteBuf, Shape> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Shape::sizeX,
                ByteBufCodecs.VAR_INT, Shape::sizeY,
                ByteBufCodecs.VAR_INT, Shape::sizeZ,
                ByteBufCodecs.VAR_INT, Shape::ground,
                BlockPos.STREAM_CODEC, Shape::entrance,
                BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list()), Shape::beds,
                Shape::new);
    }

    /** Ein kostbarer Block, den der Baumeister aus dem Lager braucht, und wie oft. */
    public record Precious(Item item, int count) {
        static final Codec<Precious> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Precious::item),
                Codec.INT.fieldOf("count").forGetter(Precious::count)
        ).apply(i, Precious::new));
        static final StreamCodec<RegistryFriendlyByteBuf, Precious> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.registry(Registries.ITEM), Precious::item,
                ByteBufCodecs.VAR_INT, Precious::count,
                Precious::new);
    }

    /** Blöcke, Einheiten je Baustoff (Holz, Stein, Metall) und kostbare Blöcke. */
    public record Costs(int blocks, List<Integer> units, List<Precious> precious) {
        static final Codec<Costs> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("blocks").forGetter(Costs::blocks),
                Codec.INT.listOf().fieldOf("units").forGetter(Costs::units),
                Precious.CODEC.listOf().optionalFieldOf("precious", List.of()).forGetter(Costs::precious)
        ).apply(i, Costs::new));
        static final StreamCodec<RegistryFriendlyByteBuf, Costs> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Costs::blocks,
                ByteBufCodecs.VAR_INT.apply(ByteBufCodecs.list()), Costs::units,
                Precious.STREAM_CODEC.apply(ByteBufCodecs.list()), Costs::precious,
                Costs::new);

        public int units(Material material) {
            return material.ordinal() < this.units.size() ? this.units.get(material.ordinal()) : 0;
        }

        /** Alle Einheiten eines Baustoffs als Feld (für Vergleiche mit dem Lager). */
        public int[] unitArray() {
            int[] out = new int[Material.values().length];
            for (Material m : Material.values()) {
                out[m.ordinal()] = this.units(m);
            }
            return out;
        }
    }

    /** Grundriss (von oben, vorn unten) und Ansicht (von vorn) als Kartenfarben. */
    public record Pictures(int topW, int topH, byte[] top, int frontW, int frontH, byte[] front) {
        private static final Codec<byte[]> BYTES = Codec.BYTE_BUFFER.xmap(b -> {
            byte[] out = new byte[b.remaining()];
            b.duplicate().get(out);
            return out;
        }, ByteBuffer::wrap);
        static final Codec<Pictures> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.fieldOf("top_w").forGetter(Pictures::topW),
                Codec.INT.fieldOf("top_h").forGetter(Pictures::topH),
                BYTES.fieldOf("top").forGetter(Pictures::top),
                Codec.INT.fieldOf("front_w").forGetter(Pictures::frontW),
                Codec.INT.fieldOf("front_h").forGetter(Pictures::frontH),
                BYTES.fieldOf("front").forGetter(Pictures::front)
        ).apply(i, Pictures::new));
        static final StreamCodec<RegistryFriendlyByteBuf, Pictures> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Pictures::topW,
                ByteBufCodecs.VAR_INT, Pictures::topH,
                ByteBufCodecs.BYTE_ARRAY, Pictures::top,
                ByteBufCodecs.VAR_INT, Pictures::frontW,
                ByteBufCodecs.VAR_INT, Pictures::frontH,
                ByteBufCodecs.BYTE_ARRAY, Pictures::front,
                Pictures::new);

        public PlanScan.Picture topPicture() {
            return picture(this.topW, this.topH, this.top);
        }

        public PlanScan.Picture frontPicture() {
            return picture(this.frontW, this.frontH, this.front);
        }

        private static PlanScan.Picture picture(int w, int h, byte[] pixels) {
            return w > 0 && h > 0 && pixels.length == w * h ? new PlanScan.Picture(w, h, pixels) : PlanScan.Picture.EMPTY;
        }

        @Override
        public boolean equals(Object o) {
            return o instanceof Pictures p && p.topW == this.topW && p.topH == this.topH && java.util.Arrays.equals(p.top, this.top)
                    && p.frontW == this.frontW && p.frontH == this.frontH && java.util.Arrays.equals(p.front, this.front);
        }

        @Override
        public int hashCode() {
            return 31 * java.util.Arrays.hashCode(this.top) + java.util.Arrays.hashCode(this.front);
        }
    }

    public static final Codec<PlanInfo> CODEC = RecordCodecBuilder.create(i -> i.group(
            Codec.STRING.fieldOf("id").forGetter(PlanInfo::id),
            Codec.STRING.fieldOf("name").forGetter(PlanInfo::name),
            UUIDUtil.CODEC.fieldOf("author").forGetter(PlanInfo::author),
            Codec.STRING.optionalFieldOf("author_name", "").forGetter(PlanInfo::authorName),
            Codec.LONG.optionalFieldOf("created", 0L).forGetter(PlanInfo::created),
            Shape.CODEC.fieldOf("shape").forGetter(PlanInfo::shape),
            Costs.CODEC.fieldOf("costs").forGetter(PlanInfo::costs),
            Pictures.CODEC.fieldOf("pictures").forGetter(PlanInfo::pictures)
    ).apply(i, PlanInfo::new));

    public static final StreamCodec<RegistryFriendlyByteBuf, PlanInfo> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8, PlanInfo::id,
            ByteBufCodecs.STRING_UTF8, PlanInfo::name,
            UUIDUtil.STREAM_CODEC, PlanInfo::author,
            ByteBufCodecs.STRING_UTF8, PlanInfo::authorName,
            ByteBufCodecs.VAR_LONG, PlanInfo::created,
            Shape.STREAM_CODEC, PlanInfo::shape,
            Costs.STREAM_CODEC, PlanInfo::costs,
            Pictures.STREAM_CODEC, PlanInfo::pictures,
            PlanInfo::new);

    /** Größte Seitenlänge der kleinen Bilder (Bildpunkte). */
    public static final int PICTURE_SIZE = 40;
    /** Längste Namen. */
    public static final int MAX_NAME = 32;

    /** Die Kurzfassung einer Vermessung. */
    public static PlanInfo of(String id, String name, UUID author, String authorName, long created, PlanScan scan) {
        PlanScan.Costs c = scan.costs();
        List<Integer> units = new ArrayList<>();
        for (int u : c.units()) {
            units.add(u);
        }
        List<Precious> precious = new ArrayList<>();
        for (Map.Entry<Item, Integer> e : c.precious().entrySet()) {
            precious.add(new Precious(e.getKey(), e.getValue()));
        }
        PlanScan.Picture top = scan.top(PICTURE_SIZE);
        PlanScan.Picture front = scan.front(PICTURE_SIZE);
        return new PlanInfo(id, name, author, authorName, created,
                new Shape(scan.sizeX, scan.sizeY, scan.sizeZ, scan.ground, scan.entrance(), scan.beds()),
                new Costs(c.blocks(), units, precious),
                new Pictures(top.width(), top.height(), top.pixels(), front.width(), front.height(), front.pixels()));
    }

    /** Derselbe Plan unter neuem Namen. */
    public PlanInfo renamed(String name) {
        return new PlanInfo(this.id, name, this.author, this.authorName, this.created, this.shape, this.costs, this.pictures);
    }

    /** Ausdehnung im Bauplan (lokal, wie {@link Blueprint#bounds()}). */
    public BoundingBox localBounds() {
        return PlanScan.bounds(this.shape.sizeX, this.shape.sizeY, this.shape.sizeZ, this.shape.ground);
    }

    /** Ausdehnung in der Welt. */
    public BoundingBox worldBounds(BlockPos origin, Rotation rotation) {
        return Blueprint.worldBounds(this.localBounds(), origin, rotation);
    }

    /** Höhe über der Bodenlinie (Lagen). */
    public int height() {
        return this.shape.sizeY - this.shape.ground;
    }

    /** Preis des Baus in Bronze (vor Rabatt): neun und ein Bronzestück je acht Blöcke. */
    public int price() {
        return 9 + this.costs.blocks / 8;
    }
}
