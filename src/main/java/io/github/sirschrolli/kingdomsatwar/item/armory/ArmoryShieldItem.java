package io.github.sirschrolli.kingdomsatwar.item.armory;

import net.minecraft.world.item.ShieldItem;

/**
 * Ein Schild der Waffenkammer (0.84). Das Blocken selbst ist der Schild von Minecraft (die Komponente
 * {@code blocks_attacks}); was jeder Schild darüber hinaus kann, steht in {@link ArmoryShields}. Er ist ein {@link ShieldItem},
 * damit Minecraft ihn beim Blocken aus eigener Sicht hebt wie einen Schild (und nicht schräg wie ein Schwert).
 */
public class ArmoryShieldItem extends ShieldItem {
    /** Die vier Schilde. */
    public enum Kind {
        /** Stahl-Rundschild: hebt sich schnell; wer im rechten Moment pariert, stößt den Angreifer zurück und schwächt ihn. */
        BUCKLER,
        /** Drachenschild: wer dagegen schlägt, fängt Feuer. */
        DRAGON,
        /** Turmschild: deckt auch die Seiten, steht fest – macht aber langsamer. */
        TOWER,
        /** Spiegelschild: Geschosse prallen dorthin zurück, wohin sein Träger schaut. */
        MIRROR
    }

    private final Kind kind;

    public ArmoryShieldItem(Properties properties, Kind kind) {
        super(properties);
        this.kind = kind;
    }

    public Kind kind() {
        return this.kind;
    }
}
