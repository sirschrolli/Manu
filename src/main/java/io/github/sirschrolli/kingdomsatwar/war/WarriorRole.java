package io.github.sirschrolli.kingdomsatwar.war;

/**
 * Die Krieger eines fremden Reichs.
 *
 * @param health    Lebenspunkte
 * @param armor     Rüstungswert (die Rüstung ist Teil des Aussehens)
 * @param toughness Rüstungshärte
 * @param attack    Grundschaden (dazu kommt die Waffe)
 * @param speed     Laufgeschwindigkeit
 * @param knockback Rückstoßfestigkeit
 * @param variants  Anzahl der Gesichter (Texturen)
 * @param xp        Erfahrung beim Sieg
 */
public enum WarriorRole {
    /** Fußknecht mit Schwert und Schild. */
    FOOTMAN("footman", 30.0, 9.0, 0.0, 3.0, 0.30, 0.1, 3, 8),
    /** Bogenschütze auf dem Wehrgang. */
    ARCHER("archer", 24.0, 4.0, 0.0, 2.0, 0.31, 0.0, 3, 8),
    /** Armbrustschütze auf den Türmen. */
    CROSSBOWMAN("crossbowman", 28.0, 9.0, 0.0, 2.0, 0.28, 0.1, 2, 10),
    /** Ritter in Plattenrüstung, Wache im Thronsaal. */
    KNIGHT("knight", 50.0, 16.0, 4.0, 4.0, 0.28, 0.5, 2, 20),
    /** Hauptmann am Tor: führt die Wachen an. */
    CAPTAIN("captain", 60.0, 14.0, 3.0, 5.0, 0.30, 0.4, 1, 30),
    /** Der Herrscher selbst. */
    LORD("lord", 180.0, 16.0, 6.0, 7.0, 0.30, 0.8, 1, 120);

    private final String id;
    private final double health;
    private final double armor;
    private final double toughness;
    private final double attack;
    private final double speed;
    private final double knockback;
    private final int variants;
    private final int xp;

    WarriorRole(String id, double health, double armor, double toughness, double attack, double speed, double knockback, int variants,
            int xp) {
        this.id = id;
        this.health = health;
        this.armor = armor;
        this.toughness = toughness;
        this.attack = attack;
        this.speed = speed;
        this.knockback = knockback;
        this.variants = variants;
        this.xp = xp;
    }

    public String id() {
        return this.id;
    }

    /** Name des Lebewesens in der Registry ("realm_footman" …). */
    public String entityId() {
        return "realm_" + this.id;
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

    public int xp() {
        return this.xp;
    }

    public boolean archer() {
        return this == ARCHER;
    }

    public boolean crossbow() {
        return this == CROSSBOWMAN;
    }

    public boolean ranged() {
        return this == ARCHER || this == CROSSBOWMAN;
    }

    /** Wer seinen Posten nicht verlässt, solange niemand angreift (Schützen und Ritter); die anderen gehen umher. */
    public boolean patrols() {
        return this == FOOTMAN || this == CAPTAIN;
    }

    public static WarriorRole byId(String id) {
        for (WarriorRole role : values()) {
            if (role.id.equals(id)) {
                return role;
            }
        }
        return FOOTMAN;
    }
}
