package io.github.sirschrolli.kingdomsatwar.hero;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Locale;
import net.minecraft.resources.Identifier;
import net.minecraft.util.StringRepresentable;
import org.jspecify.annotations.Nullable;

/**
 * Die aktiven Fähigkeiten des Helden (seit 1.11): Sie werden auf dem Heldenweg freigeschaltet – je eine auf den Stufen
 * 10, 30, 60, 100, 150, 250, 350 und 450 – und ruhen nach jedem Einsatz eine Weile. Eingesetzt wird die gewählte mit der
 * Fähigkeitstaste (kurz tippen); gehalten öffnet dieselbe Taste das Fähigkeitenrad, und im Charakterfenster lässt sich die
 * gewählte auch anklicken.
 */
public enum HeroAbility implements StringRepresentable {
    /** Sturmlauf: sechs Sekunden schneller (Tempo II) und höher springen. */
    SPRINT(10, 45, 0xFF7AE4FF),
    /** Kampfschrei: Monster ringsum (acht Blöcke) werden geschwächt und langsam, eigene Soldaten und Tiere stärker. */
    WAR_CRY(30, 60, 0xFFFFB84A),
    /** Eiserne Haut: sechs Sekunden Resistenz II. */
    IRON_SKIN(60, 90, 0xFFB8CCFF),
    /** Zweiter Atem: heilt sofort drei Zehntel der Lebenspunkte und nimmt alle schädlichen Wirkungen. */
    SECOND_WIND(100, 120, 0xFFFF6A86),
    /** Wirbelschlag: ein Rundumschlag – jeder Feind bis vier Blöcke nimmt anderthalbfachen Waffenschaden und fliegt zurück. */
    WHIRLWIND(150, 30, 0xFF9AE0FF),
    /** Adlerblick: Feinde bis 48 Blöcke leuchten zwölf Sekunden lang durch Wände. */
    EAGLE_EYE(250, 60, 0xFFFFD24A),
    /** Himmelssturz: ein Satz hoch und weit nach vorn, beim Landen eine Schockwelle; kein Fallschaden. */
    SKYFALL(350, 45, 0xFF8AC4FF),
    /** Letztes Gefecht: acht Sekunden lang sinkt man nicht unter ein halbes Herz; danach ist man erschöpft. */
    LAST_STAND(450, 300, 0xFFFF4A30);

    private final int level;
    private final int cooldownSeconds;
    private final int color;

    HeroAbility(int level, int cooldownSeconds, int color) {
        this.level = level;
        this.cooldownSeconds = cooldownSeconds;
        this.color = color;
    }

    /** Ab dieser Heldenstufe. */
    public int level() {
        return this.level;
    }

    /** So lange ruht sie nach einem Einsatz (Sekunden). */
    public int cooldownSeconds() {
        return this.cooldownSeconds;
    }

    public int cooldownTicks() {
        return this.cooldownSeconds * 20;
    }

    /** Ihre Farbe (ARGB). */
    public int color() {
        return this.color;
    }

    public boolean unlocked(int heroLevel) {
        return heroLevel >= this.level;
    }

    /** Ihr Bild ({@code textures/gui/sprites/hero/ability/<name>.png}). */
    public Identifier icon() {
        return KingdomsAtWar.id("hero/ability/" + this.getSerializedName());
    }

    public String translationKey() {
        return "hero.kingdomsatwar.ability." + this.getSerializedName();
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** Die Fähigkeit, die auf genau dieser Stufe freigeschaltet wird (oder null). */
    public static @Nullable HeroAbility unlockedAt(int level) {
        for (HeroAbility ability : values()) {
            if (ability.level == level) {
                return ability;
            }
        }
        return null;
    }

    /** Wie viele Fähigkeiten auf dieser Stufe freigeschaltet sind. */
    public static int count(int heroLevel) {
        int n = 0;
        for (HeroAbility ability : values()) {
            if (ability.unlocked(heroLevel)) {
                n++;
            }
        }
        return n;
    }

    public static HeroAbility byIndex(int index) {
        HeroAbility[] all = values();
        return all[Math.floorMod(index, all.length)];
    }
}
