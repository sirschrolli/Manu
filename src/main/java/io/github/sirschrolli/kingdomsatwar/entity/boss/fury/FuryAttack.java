package io.github.sirschrolli.kingdomsatwar.entity.boss.fury;

/**
 * Die Zornangriffe der Bosse (seit 1.14). Jeder Boss hat zwei davon in seinem Element (siehe {@link FuryProfile}), dazu
 * in der Zornphase die {@link #NOVA}. Sie laufen neben seinen eigenen Angriffen her – der Kampf wird dichter.
 */
public enum FuryAttack {
    /** Einschläge prasseln rund um die Spieler herab (erst ein Warnkreis, dann der Einschlag). */
    RAIN,
    /** Drei Ringe breiten sich nacheinander vom Boss aus – darüber springen. */
    WAVES,
    /** Risse laufen sternförmig vom Boss aus (einer auf jeden Spieler), dann bricht das Element entlang der Risse hervor. */
    LINES,
    /** Ein Sog zieht alle zum Boss – wer sich nicht losreißt, steht in der Explosion. */
    PULL,
    /** Strahlen am Boden drehen sich um den Boss – darüber springen oder mitlaufen. */
    BEAMS,
    /** Der Boden rings um den Boss teilt sich in Felder; erst die einen brechen auf, dann die anderen. */
    SECTORS,
    /** Nur im Zorn: der Boss lädt eine gewaltige Explosion – wer nicht in Deckung geht (Blöcke zwischen sich und ihm), wird getroffen. */
    NOVA;

    /** Der Schlüssel des Namens in diesem Element (Sprachdateien: {@link #english} und {@link #german}). */
    public String key(FuryElement element) {
        return "fury.kingdomsatwar." + this.name().toLowerCase(java.util.Locale.ROOT) + "." + element.name().toLowerCase(java.util.Locale.ROOT);
    }

    /** Der englische Name des Angriffs in diesem Element. */
    public String english(FuryElement element) {
        return switch (this) {
            case RAIN -> switch (element) {
                case FIRE -> "Meteor Shower";
                case ICE -> "Hailstorm";
                case LIGHTNING -> "Lightning Storm";
                case NATURE -> "Thorn Rain";
                case EARTH -> "Rockfall";
                case WATER -> "Cloudburst";
                case SHADOW -> "Shadow Rain";
                case LIGHT -> "Starfall";
                case SOUND -> "Thunderclap";
                case VOID -> "Void Rain";
                case POISON -> "Venom Rain";
                case STEEL -> "Arrow Hail";
                case WIND -> "Downdrafts";
            };
            case WAVES -> switch (element) {
                case FIRE -> "Ember Waves";
                case ICE -> "Frost Waves";
                case LIGHTNING -> "Thunder Waves";
                case NATURE -> "Root Waves";
                case EARTH -> "Quake Waves";
                case WATER -> "Flood Rings";
                case SHADOW -> "Shadow Waves";
                case LIGHT -> "Waves of Light";
                case SOUND -> "Sound Waves";
                case VOID -> "Void Waves";
                case POISON -> "Rot Waves";
                case STEEL -> "War Waves";
                case WIND -> "Storm Waves";
            };
            case LINES -> switch (element) {
                case FIRE -> "Ember Rifts";
                case ICE -> "Ice Thorns";
                case LIGHTNING -> "Lightning Paths";
                case NATURE -> "Thorn Paths";
                case EARTH -> "Rock Rifts";
                case WATER -> "Geyser Lines";
                case SHADOW -> "Shadow Rifts";
                case LIGHT -> "Radiant Paths";
                case SOUND -> "Echo Paths";
                case VOID -> "Void Rifts";
                case POISON -> "Venom Furrows";
                case STEEL -> "Blade Paths";
                case WIND -> "Wind Lanes";
            };
            case PULL -> switch (element) {
                case FIRE -> "Fire Vortex";
                case ICE -> "Frost Vortex";
                case LIGHTNING -> "Eye of the Storm";
                case NATURE -> "Vine Pull";
                case EARTH -> "Earthen Pull";
                case WATER -> "Maelstrom";
                case SHADOW -> "Shadow Pull";
                case LIGHT -> "Pull of Light";
                case SOUND -> "Calm Before the Storm";
                case VOID -> "Void Pull";
                case POISON -> "Venom Pull";
                case STEEL -> "Rallying Cry";
                case WIND -> "Whirlwind";
            };
            case BEAMS -> switch (element) {
                case FIRE -> "Cross of Flames";
                case ICE -> "Frost Cross";
                case LIGHTNING -> "Lightning Cross";
                case NATURE -> "Thorn Cross";
                case EARTH -> "Rock Cross";
                case WATER -> "Water Cross";
                case SHADOW -> "Shadow Cross";
                case LIGHT -> "Star Cross";
                case SOUND -> "Sound Cross";
                case VOID -> "Void Cross";
                case POISON -> "Venom Cross";
                case STEEL -> "Blade Cross";
                case WIND -> "Wind Cross";
            };
            case SECTORS -> switch (element) {
                case FIRE -> "Fields of Flame";
                case ICE -> "Ice Fields";
                case LIGHTNING -> "Lightning Fields";
                case NATURE -> "Thorn Fields";
                case EARTH -> "Quake Fields";
                case WATER -> "Flood Fields";
                case SHADOW -> "Shadow Fields";
                case LIGHT -> "Star Fields";
                case SOUND -> "Sound Fields";
                case VOID -> "Void Fields";
                case POISON -> "Venom Fields";
                case STEEL -> "Blade Fields";
                case WIND -> "Wind Fields";
            };
            case NOVA -> switch (element) {
                case FIRE -> "Inferno";
                case ICE -> "Eternal Winter";
                case LIGHTNING -> "Storm's Judgment";
                case NATURE -> "Wild Wrath";
                case EARTH -> "Worldquake";
                case WATER -> "Deluge";
                case SHADOW -> "Eternal Night";
                case LIGHT -> "Supernova";
                case SOUND -> "Great Scream";
                case VOID -> "Annihilation";
                case POISON -> "Plague Breath";
                case STEEL -> "Last Stand";
                case WIND -> "Hurricane";
            };
        };
    }

    /** Der deutsche Name des Angriffs in diesem Element. */
    public String german(FuryElement element) {
        return switch (this) {
            case RAIN -> switch (element) {
                case FIRE -> "Meteorschauer";
                case ICE -> "Hagelsturm";
                case LIGHTNING -> "Blitzgewitter";
                case NATURE -> "Dornenregen";
                case EARTH -> "Steinhagel";
                case WATER -> "Wolkenbruch";
                case SHADOW -> "Schattenregen";
                case LIGHT -> "Sternenfall";
                case SOUND -> "Donnerhall";
                case VOID -> "Leerenregen";
                case POISON -> "Giftregen";
                case STEEL -> "Pfeilhagel";
                case WIND -> "Fallböen";
            };
            case WAVES -> switch (element) {
                case FIRE -> "Glutwellen";
                case ICE -> "Frostwellen";
                case LIGHTNING -> "Donnerwellen";
                case NATURE -> "Wurzelwellen";
                case EARTH -> "Bebenwellen";
                case WATER -> "Flutringe";
                case SHADOW -> "Schattenwellen";
                case LIGHT -> "Lichtwellen";
                case SOUND -> "Schallwellen";
                case VOID -> "Leerenwellen";
                case POISON -> "Fäulniswellen";
                case STEEL -> "Kriegswellen";
                case WIND -> "Sturmwellen";
            };
            case LINES -> switch (element) {
                case FIRE -> "Glutspalten";
                case ICE -> "Eisdornen";
                case LIGHTNING -> "Blitzbahnen";
                case NATURE -> "Dornenpfade";
                case EARTH -> "Felsspalten";
                case WATER -> "Geysirlinien";
                case SHADOW -> "Schattenrisse";
                case LIGHT -> "Strahlenpfade";
                case SOUND -> "Echobahnen";
                case VOID -> "Leerenrisse";
                case POISON -> "Giftfurchen";
                case STEEL -> "Klingenbahnen";
                case WIND -> "Windschneisen";
            };
            case PULL -> switch (element) {
                case FIRE -> "Feuersog";
                case ICE -> "Frostsog";
                case LIGHTNING -> "Sturmauge";
                case NATURE -> "Rankensog";
                case EARTH -> "Erdsog";
                case WATER -> "Mahlstrom";
                case SHADOW -> "Schattensog";
                case LIGHT -> "Lichtsog";
                case SOUND -> "Stille vor dem Sturm";
                case VOID -> "Leerensog";
                case POISON -> "Giftsog";
                case STEEL -> "Kriegsruf";
                case WIND -> "Wirbelsturm";
            };
            case BEAMS -> switch (element) {
                case FIRE -> "Flammenkreuz";
                case ICE -> "Frostkreuz";
                case LIGHTNING -> "Blitzkreuz";
                case NATURE -> "Dornenkreuz";
                case EARTH -> "Felskreuz";
                case WATER -> "Wasserkreuz";
                case SHADOW -> "Schattenkreuz";
                case LIGHT -> "Sternenkreuz";
                case SOUND -> "Klangkreuz";
                case VOID -> "Leerenkreuz";
                case POISON -> "Giftkreuz";
                case STEEL -> "Klingenkreuz";
                case WIND -> "Windkreuz";
            };
            case SECTORS -> switch (element) {
                case FIRE -> "Flammenfelder";
                case ICE -> "Eisfelder";
                case LIGHTNING -> "Blitzfelder";
                case NATURE -> "Dornenfelder";
                case EARTH -> "Bebenfelder";
                case WATER -> "Flutfelder";
                case SHADOW -> "Schattenfelder";
                case LIGHT -> "Sternenfelder";
                case SOUND -> "Klangfelder";
                case VOID -> "Leerenfelder";
                case POISON -> "Giftfelder";
                case STEEL -> "Klingenfelder";
                case WIND -> "Windfelder";
            };
            case NOVA -> switch (element) {
                case FIRE -> "Inferno";
                case ICE -> "Ewiger Winter";
                case LIGHTNING -> "Sturmgericht";
                case NATURE -> "Wilder Zorn";
                case EARTH -> "Weltenbeben";
                case WATER -> "Sintflut";
                case SHADOW -> "Ewige Nacht";
                case LIGHT -> "Supernova";
                case SOUND -> "Großer Schrei";
                case VOID -> "Vernichtung";
                case POISON -> "Pesthauch";
                case STEEL -> "Letztes Gefecht";
                case WIND -> "Orkan";
            };
        };
    }
}
