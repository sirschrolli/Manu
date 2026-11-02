package io.github.sirschrolli.kingdomsatwar.war;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.HolderGetter;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;

/**
 * Die vier fremden Reiche einer Welt: jedes mit eigenem Namen, Herrscher, Wappen (eigenes Bannermuster), Farben und
 * einer Stammburg in eigener Bauweise ({@link CastleStyle}). Wo ihre Burgen liegen, berechnet {@link RealmLocator}.
 */
public enum Realm implements StringRepresentable {
    /** Königreich Eisenmark: silberner Turm auf Blau, grauer Stein und Schieferdächer. */
    EISENMARK("eisenmark", DyeColor.BLUE, DyeColor.WHITE, DyeColor.LIGHT_GRAY, 0x3C5AA6, "tower"),
    /** Herzogtum Rabenstein: schwarzer Rabe auf Rot, dunkler Stein und rote Dächer. */
    RABENSTEIN("rabenstein", DyeColor.RED, DyeColor.BLACK, DyeColor.BLACK, 0xA62A2A, "raven"),
    /** Fürstentum Dornfels: goldene Sonne auf Ocker, Sandstein und Ziegeldächer. */
    DORNFELS("dornfels", DyeColor.ORANGE, DyeColor.YELLOW, DyeColor.BROWN, 0xD38A2C, "sun"),
    /** Grafschaft Silberau: weißer Schwan auf Grün, heller Stein und grüne Kupferdächer. */
    SILBERAU("silberau", DyeColor.GREEN, DyeColor.WHITE, DyeColor.WHITE, 0x3F8B4C, "swan");

    public static final Codec<Realm> CODEC = StringRepresentable.fromEnum(Realm::values);

    private final String id;
    private final DyeColor field;
    private final DyeColor charge;
    private final DyeColor border;
    private final int color;
    private final String emblem;

    Realm(String id, DyeColor field, DyeColor charge, DyeColor border, int color, String emblem) {
        this.id = id;
        this.field = field;
        this.charge = charge;
        this.border = border;
        this.color = color;
        this.emblem = emblem;
    }

    public String id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    /** Grundfarbe des Wappens (Banner, Teppiche, Glas, Wappenröcke). */
    public DyeColor field() {
        return this.field;
    }

    /** Farbe des Wappentiers bzw. -zeichens. */
    public DyeColor charge() {
        return this.charge;
    }

    /** Farbe für Bordüren und Akzente. */
    public DyeColor accent() {
        return this.border;
    }

    /** Farbe für Anzeigen (Karten, Leisten, Chronik). */
    public int color() {
        return this.color;
    }

    /** Das Wappenzeichen als eigenes Bannermuster (Turm, Rabe, Sonne, Schwan). */
    public ResourceKey<BannerPattern> emblem() {
        return ResourceKey.create(Registries.BANNER_PATTERN, KingdomsAtWar.id(this.emblem));
    }

    public CastleStyle style() {
        return CastleStyle.of(this);
    }

    public static Realm byId(String id) {
        for (Realm realm : values()) {
            if (realm.id.equals(id)) {
                return realm;
            }
        }
        return EISENMARK;
    }

    // =====================================================================================
    // Namen
    // =====================================================================================

    /** „Königreich Eisenmark“. */
    public Component displayName() {
        return Component.translatable("realm.kingdomsatwar." + this.id).withColor(this.color);
    }

    /** „Eisenmark“. */
    public Component shortName() {
        return Component.translatable("realm.kingdomsatwar." + this.id + ".short").withColor(this.color);
    }

    /** „König Harald der Graue“. */
    public Component ruler() {
        return Component.translatable("realm.kingdomsatwar." + this.id + ".ruler");
    }

    // =====================================================================================
    // Wappen
    // =====================================================================================

    /** Die Schichten des Wappens: Zeichen, Bordüre und ein Schatten am Fuß. */
    public BannerPatternLayers heraldry(HolderGetter<BannerPattern> patterns) {
        return new BannerPatternLayers.Builder()
                .addIfRegistered(patterns, this.emblem(), this.charge)
                .addIfRegistered(patterns, BannerPatterns.BORDER, this.border)
                .addIfRegistered(patterns, BannerPatterns.GRADIENT_UP, DyeColor.BLACK)
                .build();
    }

    /** Ein Banner mit dem Wappen des Reichs (als Gegenstand, z. B. für Banner in Bauwerken und als Beute). */
    public ItemStack banner(HolderLookup.Provider registries) {
        ItemStack stack = new ItemStack(Blocks.BANNER.pick(this.field).asItem());
        stack.set(DataComponents.BANNER_PATTERNS, this.heraldry(registries.lookupOrThrow(Registries.BANNER_PATTERN)));
        return stack;
    }

    public Block standingBanner() {
        return Blocks.BANNER.pick(this.field);
    }

    public Block wallBanner() {
        return Blocks.WALL_BANNER.pick(this.field);
    }

    /** Die vier Wappenzeichen als Bannermuster (Datengenerierung). */
    public static void bootstrapPatterns(BootstrapContext<BannerPattern> context) {
        for (Realm realm : values()) {
            context.register(realm.emblem(), new BannerPattern(KingdomsAtWar.id(realm.emblem), "block.kingdomsatwar.banner." + realm.emblem));
        }
    }

    /** Kennung des Wappenzeichens ("tower", "raven", "sun", "swan"). */
    public String emblemId() {
        return this.emblem;
    }
}
