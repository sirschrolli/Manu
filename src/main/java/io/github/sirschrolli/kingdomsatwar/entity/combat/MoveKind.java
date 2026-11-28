package io.github.sirschrolli.kingdomsatwar.entity.combat;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.entity.combat.moves.MeleeMoves;
import io.github.sirschrolli.kingdomsatwar.entity.combat.moves.RangedMoves;
import io.github.sirschrolli.kingdomsatwar.entity.combat.moves.SpecialMoves;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.util.StringRepresentable;

/**
 * Alle besonderen Angriffe der Monster. Die Reihenfolge ist Teil des Netzprotokolls (die Haltung im Spiel kommt über
 * die Nummer) – neue Angriffe nur hinten anhängen.
 */
public enum MoveKind implements StringRepresentable {
    // ---- Nahkampf
    /** Duckt sich, springt das Ziel an und reißt es um. */
    LUNGE("Ansprung", true, true, Pose.CROUCH, MeleeMoves.Lunge::new),
    /** Hebt beide Arme (oder Hufe, Hauer) und schmettert sie auf den Boden: Wucht im Umkreis, bricht Schilde. */
    SLAM("Hammerschlag", true, true, Pose.RAISE, MeleeMoves.Slam::new),
    /** Holt weit aus und dreht sich einmal um sich selbst – trifft alle ringsum. */
    SWEEP("Wirbelhieb", true, true, Pose.COIL, MeleeMoves.Sweep::new),
    /** Scharrt, senkt den Kopf und stürmt geradeaus los; prallt er gegen eine Wand, ist er benommen. */
    CHARGE("Sturmangriff", true, true, Pose.TREMBLE, MeleeMoves.Charge::new),
    /** Packt zu und hält fest (langsam, kein Abbauen), bis man ihn kräftig trifft. */
    GRAB("Umklammern", true, true, Pose.GRAB, MeleeMoves.Grab::new),
    /** Schneller, giftiger Biss. */
    BITE("Giftbiss", true, true, Pose.CROUCH, MeleeMoves.Bite::new),
    /** Umkreist das Ziel und stößt aus der Flanke zu (die Lauernden). */
    CIRCLE("Umkreisen", true, false, Pose.NONE, MeleeMoves.Circle::new),
    /** Deckt sich (von vorn kaum verwundbar) und schlägt zurück, wenn man in die Deckung schlägt (die Zähen). */
    GUARD("Deckung", true, true, Pose.GUARD, MeleeMoves.Guard::new),
    /** Kurzer Stoß mit dem Bogen, der Abstand schafft. */
    BASH("Bogenstoß", true, true, Pose.RAISE, MeleeMoves.Bash::new),
    /** Schleim springt hoch hinauf und landet mit Wucht. */
    BURST("Schmetterlandung", true, true, Pose.CROUCH, MeleeMoves.Burst::new),
    /** Stürzt sich im Flug geradewegs auf das Ziel. */
    DIVE("Sturzflug", true, false, Pose.TREMBLE, MeleeMoves.Dive::new),

    // ---- Fernkampf
    /** Drei Geschosse auf einmal im Fächer. */
    VOLLEY("Salve", true, true, Pose.AIM, RangedMoves.Volley::new),
    /** Zielt lange (eine Linie verrät, wohin) und schießt einen durchschlagenden Schuss. */
    AIMED("Zielschuss", true, true, Pose.AIM, RangedMoves.Aimed::new),
    /** Springt vom Ziel weg nach hinten. */
    BACKSTEP("Rückzugssprung", true, false, Pose.CROUCH, RangedMoves.Backstep::new),
    /** Speit etwas auf das Ziel: Netz, Schleim, Glut. */
    SPIT("Speien", true, true, Pose.REAR, RangedMoves.Spit::new),
    /** Wirft einen Brocken, Knochen oder Schädel im Bogen. */
    THROW("Wurf", true, true, Pose.THROW, RangedMoves.Throw::new),
    /** Drei Tränke rasch hintereinander. */
    BARRAGE("Trankhagel", true, true, Pose.THROW, RangedMoves.Barrage::new),
    /** Huscht zur Seite und kommt aus einer anderen Richtung wieder. */
    SKITTER("Huschen", true, false, Pose.NONE, RangedMoves.Skitter::new),
    /** Verschwindet in Rauch und taucht ein Stück entfernt wieder auf. */
    BLINK("Entrücken", true, false, Pose.NONE, RangedMoves.Blink::new),

    // ---- Besonderes
    /** Kampfschrei: Artgenossen ringsum werden schneller und stärker und stürzen sich auf dasselbe Ziel. */
    ROAR("Kampfschrei", true, true, Pose.REAR, SpecialMoves.Roar::new),
    /** Zischt und bläht sich auf – und weicht dann doch zurück (Kriecher). */
    FEINT("Finte", true, false, Pose.NONE, SpecialMoves.Feint::new),
    /** Springt das Ziel mit brennender Lunte an (Kriecher). */
    FUSE_LEAP("Sprungzünder", true, true, Pose.CROUCH, SpecialMoves.FuseLeap::new),
    /** Ein Stoß Sporen: übel und langsam (Kriecher). */
    SPORES("Sporenstoß", true, true, Pose.REAR, SpecialMoves.Spores::new),
    /** Eine Ranke schnellt hervor und zieht das Ziel heran (Kriecher). */
    VINE("Rankenzug", true, true, Pose.REAR, SpecialMoves.Vine::new),
    /** Ein Fluchkreis unter dem Ziel: Wer nicht hinaustritt, wird langsam und schwach (Hexe). */
    CURSE("Fluchkreis", false, false, Pose.NONE, SpecialMoves.Curse::new),
    /** Leerwandler: Funken verraten den Ort – dann steht er hinter dem Ziel und schlägt zu. */
    AMBUSH("Hinterhalt", true, true, Pose.NONE, SpecialMoves.Ambush::new),
    /** Ein Ring aus Fangzähnen bricht rings um das Ziel aus dem Boden (Beschwörer). */
    FANGS("Fangkreis", false, false, Pose.NONE, SpecialMoves.Fangs::new),
    /** Ein Feuerring breitet sich am Boden aus – darüber springen (Glutgeist). */
    FIRE_RING("Feuerring", false, false, Pose.NONE, SpecialMoves.FireRing::new),
    /** Ein Glutkreis unter dem Ziel, dann schießt eine Flammensäule empor (Glutgeist). */
    PILLAR("Flammensäule", false, false, Pose.NONE, SpecialMoves.Pillar::new),
    /** Ein gellender Schrei: schwach und langsam (Nachtmahr). */
    SCREECH("Schreckensschrei", false, false, Pose.NONE, SpecialMoves.Screech::new),
    /** Eine Rauchwolke hüllt das Ziel ein (Rauchqualle). */
    SMOKE("Rauchschwall", false, false, Pose.NONE, SpecialMoves.Smoke::new);

    public static final Codec<MoveKind> CODEC = StringRepresentable.fromEnum(MoveKind::values);

    /** Die Haltung, die man im Spiel sieht (Körper; bei Menschengestalten auch die Arme). */
    public enum Pose {
        NONE, CROUCH, RAISE, COIL, TREMBLE, GRAB, GUARD, AIM, REAR, THROW
    }

    private final String title;
    private final boolean control;
    private final boolean root;
    private final Pose pose;
    private final Supplier<Move> factory;
    private Move impl;

    MoveKind(String title, boolean control, boolean root, Pose pose, Supplier<Move> factory) {
        this.title = title;
        this.control = control;
        this.root = root;
        this.pose = pose;
        this.factory = factory;
    }

    @Override
    public String getSerializedName() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    /** Deutscher Name (für Vorführungen und Tests). */
    public String title() {
        return this.title;
    }

    /** Übernimmt der Angriff Bewegung und Blick (alles andere wartet), oder läuft er neben dem Gewohnten her? */
    public boolean control() {
        return this.control;
    }

    /** Bleibt das Monster dabei stehen (läuft es nicht von selbst weiter)? */
    public boolean root() {
        return this.root;
    }

    public Pose pose() {
        return this.pose;
    }

    public Move impl() {
        if (this.impl == null) {
            this.impl = this.factory.get();
        }
        return this.impl;
    }

    public static MoveKind byId(int id) {
        MoveKind[] all = values();
        return id >= 0 && id < all.length ? all[id] : null;
    }
}
