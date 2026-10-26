package io.github.sirschrolli.kingdomsatwar.economy.town;

import com.mojang.serialization.Codec;
import java.util.Locale;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.Nullable;

/**
 * Die Berufe der Bürger. Jeder Beruf wird mit Münzen angeheuert und braucht seine Werkstatt (Feld, Holzfällerhütte,
 * Mine, Schmiede, Markt, Taverne, Kartenhaus) – nur der Baumeister fängt ohne an, er baut ja erst alles.
 *
 * @param price   Lohn beim Anheuern (in Bronze)
 * @param trader  handelt mit Spielern (Rechtsklick öffnet den Handel)
 * @param variants Anzahl der Gesichter
 */
public enum Profession implements StringRepresentable {
    BUILDER(40, false, 3),
    FARMER(30, false, 3),
    LUMBERJACK(30, false, 3),
    MINER(50, false, 3),
    SMITH(80, true, 3),
    MERCHANT(80, true, 3),
    INNKEEPER(60, true, 3),
    CARTOGRAPHER(100, true, 3);

    public static final Codec<Profession> CODEC = StringRepresentable.fromEnum(Profession::values);

    private final int price;
    private final boolean trader;
    private final int variants;

    Profession(int price, boolean trader, int variants) {
        this.price = price;
        this.trader = trader;
        this.variants = variants;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    public int price() {
        return this.price;
    }

    public boolean trader() {
        return this.trader;
    }

    public int variants() {
        return this.variants;
    }

    public String translationKey() {
        return "profession.kingdomsatwar." + this.id();
    }

    /** Wo der Bürger arbeitet ({@code null}: der Baumeister braucht keine Werkstatt). */
    public @Nullable BuildingType workplace() {
        return switch (this) {
            case BUILDER -> null;
            case FARMER -> BuildingType.FIELD;
            case LUMBERJACK -> BuildingType.LUMBER_HUT;
            case MINER -> BuildingType.MINE;
            case SMITH -> BuildingType.SMITHY;
            case MERCHANT -> BuildingType.MARKET;
            case INNKEEPER -> BuildingType.TAVERN;
            case CARTOGRAPHER -> BuildingType.MAP_HOUSE;
        };
    }

    /** Werkzeug, das der Bürger in der Hand trägt (und als Bild im Stadtfenster). */
    public Item tool() {
        return switch (this) {
            case BUILDER -> Items.IRON_SHOVEL;
            case FARMER -> Items.IRON_HOE;
            case LUMBERJACK -> Items.IRON_AXE;
            case MINER -> Items.IRON_PICKAXE;
            case SMITH -> Items.ANVIL;
            case MERCHANT -> io.github.sirschrolli.kingdomsatwar.registry.ModItems.GOLD_COIN.get();
            case INNKEEPER -> Items.BREAD;
            case CARTOGRAPHER -> Items.MAP;
        };
    }

    /** Was der Bürger bei der Arbeit in der Hand hält (Händler, Wirt und Kartograf arbeiten mit leeren Händen). */
    public ItemStack workItem() {
        return switch (this) {
            case BUILDER, FARMER, LUMBERJACK, MINER -> new ItemStack(this.tool());
            case SMITH -> new ItemStack(Items.MACE);
            default -> ItemStack.EMPTY;
        };
    }
}
