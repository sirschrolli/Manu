package io.github.sirschrolli.kingdomsatwar.entity.soldier;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Die Soldatenarten (angeworben in den Dörfern, ausgebildet am Feldherrentisch): Schwertkämpfer und Bogenschützen als
 * Grundstock, dazu Ritter und Hauptleute, Speerträger (lange Reichweite, hart gegen Reiter), Armbrustschützen (langsam,
 * aber durchschlagend), Schildträger (ziehen die Angriffe auf sich), Waffenknechte (schwer gepanzert, der Streitkolben
 * trifft auch die Nebenleute), Lanzenreiter (auf dem eigenen Schlachtross, mit dem Lanzenangriff) und Belagerungsknechte
 * (laden Belagerungswaffen schneller und bessern sie aus).
 *
 * @param health    Lebenspunkte
 * @param armor     Rüstungswert (die Rüstung ist Teil des Aussehens, nicht als Gegenstand angelegt)
 * @param toughness Rüstungshärte
 * @param attack    Grundschaden (dazu kommt die Waffe)
 * @param speed     Laufgeschwindigkeit
 * @param knockback Rückstoßfestigkeit
 * @param variants  Anzahl der Gesichter (Texturvarianten)
 * @param coins     Sold beim Anwerben (in Bronze, siehe {@link io.github.sirschrolli.kingdomsatwar.economy.Coins})
 * @param cost      dazu nötige Ausrüstung (Stahl für Ritter, ein Edelstein für Hauptleute)
 */
public enum SoldierType {
    SWORDSMAN("swordsman", 30.0, 8.0, 0.0, 2.0, 0.30, 0.1, 4, 45, List.of()),
    ARCHER("archer", 24.0, 4.0, 0.0, 2.0, 0.32, 0.0, 4, 63, List.of()),
    KNIGHT("knight", 44.0, 14.0, 4.0, 3.0, 0.28, 0.5, 2, 126, List.of(cost(() -> ModItems.STEEL_INGOT.get(), 2))),
    CAPTAIN("captain", 50.0, 12.0, 2.0, 4.0, 0.30, 0.3, 3, 207, List.of(cost(() -> ModItems.GEMSTONE.get(), 1))),
    SPEARMAN("spearman", 32.0, 9.0, 0.0, 2.5, 0.30, 0.2, 3, 54, List.of(cost(() -> Items.IRON_SPEAR, 1))),
    CROSSBOWMAN("crossbowman", 28.0, 8.0, 0.0, 2.0, 0.29, 0.1, 3, 81, List.of(cost(() -> Items.CROSSBOW, 1))),
    SHIELDBEARER("shieldbearer", 40.0, 12.0, 2.0, 2.0, 0.27, 0.6, 3, 90, List.of(cost(() -> Items.SHIELD, 1), cost(() -> Items.IRON_INGOT, 2))),
    MAN_AT_ARMS("man_at_arms", 52.0, 18.0, 4.0, 4.0, 0.25, 0.8, 2, 162, List.of(cost(() -> ModItems.STEEL_INGOT.get(), 4))),
    LANCER("lancer", 44.0, 14.0, 3.0, 3.0, 0.30, 0.5, 2, 243, List.of(cost(() -> Items.SADDLE, 1), cost(() -> ModItems.STEEL_INGOT.get(), 2))),
    ENGINEER("engineer", 26.0, 5.0, 0.0, 1.5, 0.30, 0.0, 3, 72, List.of(cost(() -> Items.IRON_INGOT, 3)));

    private final String id;
    private final double health;
    private final double armor;
    private final double toughness;
    private final double attack;
    private final double speed;
    private final double knockback;
    private final int variants;
    private final int coins;
    private final List<Cost> cost;

    SoldierType(String id, double health, double armor, double toughness, double attack, double speed, double knockback, int variants,
            int coins, List<Cost> cost) {
        this.id = id;
        this.health = health;
        this.armor = armor;
        this.toughness = toughness;
        this.attack = attack;
        this.speed = speed;
        this.knockback = knockback;
        this.variants = variants;
        this.coins = coins;
        this.cost = cost;
    }

    /** Ein Posten des Preises, z. B. 12 Smaragde. */
    public record Cost(Supplier<Item> item, int count) {
        public ItemStack stack() {
            return new ItemStack(this.item.get(), this.count);
        }
    }

    private static Cost cost(Supplier<Item> item, int count) {
        return new Cost(item, count);
    }

    public String id() {
        return this.id;
    }

    public double health() {
        return this.health;
    }

    public double armor() {
        return this.armor;
    }

    public double toughness() {
        return this.toughness;
    }

    public double attack() {
        return this.attack;
    }

    public double speed() {
        return this.speed;
    }

    public double knockback() {
        return this.knockback;
    }

    public int variants() {
        return this.variants;
    }

    /**
     * Die letzte Gesichtsvariante von Schwertkämpfern, Bogen- und Armbrustschützen, Speerträgern und Belagerungsknechten
     * ist eine Frau (bei den anderen Arten verdeckt der Helm das Gesicht).
     */
    public boolean isFeminine(int variant) {
        return (this == SWORDSMAN || this == ARCHER || this == SPEARMAN || this == CROSSBOWMAN || this == ENGINEER)
                && variant == this.variants - 1;
    }

    /** Sold in Bronze. */
    public int coins() {
        return this.coins;
    }

    public List<Cost> cost() {
        return this.cost;
    }

    /** Fernkämpfer (Bogen oder Armbrust). */
    public boolean ranged() {
        return this == ARCHER || this == CROSSBOWMAN;
    }

    /** Schießt mit der Armbrust. */
    public boolean crossbow() {
        return this == CROSSBOWMAN;
    }

    /** Trägt einen Schild (Ritter und Schildträger) und deckt sich damit. */
    public boolean shielded() {
        return this == KNIGHT || this == SHIELDBEARER;
    }

    /** Kämpft mit dem Speer bzw. der Lanze (lange Reichweite). */
    public boolean spear() {
        return this == SPEARMAN || this == LANCER;
    }

    /** Trägt einen geschlossenen Helm (das Gesicht verrät nicht, ob Mann oder Frau darunter steckt). */
    public boolean helmeted() {
        return this == KNIGHT || this == MAN_AT_ARMS || this == LANCER;
    }

    /** Reitet sein eigenes Schlachtross. */
    public boolean mounted() {
        return this == LANCER;
    }

    /** Was er in der Hand trägt – für die Symbole der Fenster. */
    public Item weaponIcon() {
        return switch (this) {
            case ARCHER -> Items.BOW;
            case CROSSBOWMAN -> Items.CROSSBOW;
            case SPEARMAN, LANCER -> Items.IRON_SPEAR;
            case MAN_AT_ARMS -> Items.MACE;
            case ENGINEER -> Items.IRON_AXE;
            case KNIGHT, CAPTAIN -> ModItems.STEEL_SWORD.get();
            default -> Items.IRON_SWORD;
        };
    }

    public String translationKey() {
        return "entity.kingdomsatwar." + this.id;
    }

    public String descriptionKey() {
        return "soldier.kingdomsatwar." + this.id + ".description";
    }
}
