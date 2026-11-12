package io.github.sirschrolli.kingdomsatwar.datagen;

import net.minecraft.world.item.ItemDisplayContext;
import net.neoforged.neoforge.client.model.generators.template.ExtendedModelTemplateBuilder;

/**
 * Wie die Speere in 3D in der Hand liegen (seit 1.1.3): genau wie die Speere von Minecraft – deren Haltungen (ruhig, Ansturm,
 * Stoß, aus der eigenen Sicht und von außen) sind für einen Speer gemacht, der quer über das Bild von unten rechts nach oben
 * links liegt, gut 22 Pixel lang ist und in der Mitte gegriffen wird. Die Speere der Mod waren länger (25 bis 31 Pixel) und
 * lagen mit der Mitte ein Stück zur Spitze hin: in der Hand ragten sie weit über den Kopf, und aus der eigenen Sicht war die
 * Spitze abgeschnitten. Jetzt wird jeder Speer auf die Mitte gesetzt und so verkleinert, dass er so lang erscheint wie ein
 * Speer von Minecraft (ein Hauch länger).
 *
 * <p>Die Teile eines Speers liegen längs der Y-Achse (von y bis y in den Spalten 1 und 4), mitten auf x = z = 8; gedreht wird
 * er danach um +45° um Z um die Mitte (8, 8, 8).
 */
final class SpearHold {
    private SpearHold() {}

    /** So lang soll ein Speer in der Hand erscheinen (Pixel des Modells; die Speere von Minecraft: knapp 22). */
    static final float LENGTH = 23.0f;

    private static float min(float[][] parts) {
        float min = Float.MAX_VALUE;
        for (float[] p : parts) {
            min = Math.min(min, Math.min(p[1], p[4]));
        }
        return min;
    }

    private static float max(float[][] parts) {
        float max = -Float.MAX_VALUE;
        for (float[] p : parts) {
            max = Math.max(max, Math.max(p[1], p[4]));
        }
        return max;
    }

    /** So weit müssen die Teile längs Y verschoben werden, damit die Mitte des Speers auf der Mitte des Modells liegt. */
    static float lift(float[][] parts) {
        return 8.0f - (min(parts) + max(parts)) / 2.0f;
    }

    /** So viel kleiner als ein Speer von Minecraft gleicher Größe erscheint er in der Hand. */
    static float factor(float[][] parts) {
        return LENGTH / (max(parts) - min(parts));
    }

    /** Die Haltungen der Speere von Minecraft („spear_in_hand“), auf die Länge dieses Speers umgerechnet. */
    static ExtendedModelTemplateBuilder hold(ExtendedModelTemplateBuilder builder, float[][] parts) {
        float f = factor(parts);
        float first = 1.36f * f;
        float third = 1.7f * f;
        return builder
                .transform(ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
                        t -> t.rotation(-20, 90, -35).translation(3.13f, 2.0f, 0.13f).scale(first, first, first * 0.5f))
                .transform(ItemDisplayContext.FIRST_PERSON_LEFT_HAND,
                        t -> t.rotation(-20, -90, 35).translation(3.13f, 2.0f, 0.13f).scale(first, first, first * 0.5f))
                .transform(ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
                        t -> t.rotation(5, 270, -40).translation(0, 2, 2).scale(third, third, third * 0.5f))
                .transform(ItemDisplayContext.THIRD_PERSON_LEFT_HAND,
                        t -> t.rotation(5, -270, 40).translation(0, 2, 2).scale(third, third, third * 0.5f));
    }
}
