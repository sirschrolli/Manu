package io.github.sirschrolli.kingdomsatwar.entity.soldier;

import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.List;
import java.util.function.Supplier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/**
 * Die vier Soldatenarten, die man am Rekrutierungstisch anwerben kann.
 *
 * @param health    Lebenspunkte
 * @param armor     Rüstungswert (die Rüstung ist Teil des Aussehens, nicht als Gegenstand angelegt)
 * @param toughness Rüstungshärte
 * @param attack    Grundschaden (dazu kommt die Waffe)
 * @param speed     Laufgeschwindigkeit
 * @param knockback Rückstoßfestigkeit
 * @param variants  Anzahl der Gesichter (Texturvarianten)
 * @param cost      Preis am Rekrutierungstisch
 */
public enum SoldierType {
    SWORDSMAN("swordsman", 30.0, 8.0, 0.0, 2.0, 0.30, 0.1, 4, List.of(cost(() -> Items.EMERALD, 5))),
    ARCHER("archer", 24.0, 4.0, 0.0, 2.0, 0.32, 0.0, 4, List.of(cost(() -> Items.EMERALD, 7))),
    KNIGHT("knight", 44.0, 14.0, 4.0, 3.0, 0.28, 0.5, 2, List.of(cost(() -> Items.EMERALD, 12), cost(() -> ModItems.STEEL_INGOT.get(), 2))),
    CAPTAIN("captain", 50.0, 12.0, 2.0, 4.0, 0.30, 0.3, 3, List.of(cost(() -> Items.EMERALD, 20), cost(() -> ModItems.GEMSTONE.get(), 1)));

    private final String id;
    private final double health;
    private final double armor;
    private final double toughness;
    private final double attack;
    private final double speed;
    private final double knockback;
    private final int variants;
    private final List<Cost> cost;

    SoldierType(String id, double health, double armor, double toughness, double attack, double speed, double knockback, int variants,
            List<Cost> cost) {
        this.id = id;
        this.health = health;
        this.armor = armor;
        this.toughness = toughness;
        this.attack = attack;
        this.speed = speed;
        this.knockback = knockback;
        this.variants = variants;
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

    /** Die letzte Gesichtsvariante von Schwertkämpfern und Bogenschützen ist eine Frau. */
    public boolean isFeminine(int variant) {
        return (this == SWORDSMAN || this == ARCHER) && variant == this.variants - 1;
    }

    public List<Cost> cost() {
        return this.cost;
    }

    /** Die Smaragde im Preis (auch der Lohn herrenloser Söldner). */
    public int emeraldPrice() {
        return this.cost.stream().filter(c -> c.item().get() == Items.EMERALD).mapToInt(Cost::count).sum();
    }

    public boolean ranged() {
        return this == ARCHER;
    }

    public String translationKey() {
        return "entity.kingdomsatwar." + this.id;
    }

    public String descriptionKey() {
        return "soldier.kingdomsatwar." + this.id + ".description";
    }
}
