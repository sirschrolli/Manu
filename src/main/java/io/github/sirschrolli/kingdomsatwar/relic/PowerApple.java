package io.github.sirschrolli.kingdomsatwar.relic;

import io.github.sirschrolli.kingdomsatwar.registry.ModEffects;
import java.util.List;
import net.minecraft.core.Holder;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

/**
 * Die vier Äpfel der Macht (seit 0.80): weit stärker als der verzauberte goldene Apfel – und sehr schwer herzustellen (aus
 * den Schuppen aller sechs Drachen, aus der Beute der Sternenreiterin und der Schatten, und der vierte aus den drei anderen
 * und einem Schöpferfunken).
 */
public enum PowerApple {
    /** Drachenapfel: die Kraft der sechs Drachen – Stärke, Schutz und zwölf Extraherzen. */
    DRAGON("dragon_apple", 0xFF8A3A),
    /** Sternenapfel: schnell, flink, glücklich. */
    STAR("star_apple", 0x86DCFA),
    /** Schattenapfel: unsichtbar und gewaltig. */
    SHADOW("shadow_apple", 0xC074F8),
    /** Apfel der Unsterblichkeit: alles zugleich – und ein Zweites Leben. */
    IMMORTAL("immortal_apple", 0xFFE9A0);

    private final String id;
    private final int color;

    PowerApple(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return this.id;
    }

    public int color() {
        return this.color;
    }

    /** Was der Apfel gibt (Sekunden, Stufe ab 0). */
    public List<MobEffectInstance> effects() {
        return switch (this) {
            case DRAGON -> List.of(effect(MobEffects.REGENERATION, 40, 2), effect(MobEffects.ABSORPTION, 300, 5),
                    effect(MobEffects.RESISTANCE, 300, 1), effect(MobEffects.STRENGTH, 300, 1), effect(MobEffects.FIRE_RESISTANCE, 600, 0));
            case STAR -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.ABSORPTION, 300, 3),
                    effect(MobEffects.SPEED, 300, 2), effect(MobEffects.HASTE, 300, 2), effect(MobEffects.JUMP_BOOST, 300, 1),
                    effect(MobEffects.NIGHT_VISION, 600, 0), effect(MobEffects.LUCK, 600, 1));
            case SHADOW -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.ABSORPTION, 300, 3),
                    effect(MobEffects.INVISIBILITY, 300, 0), effect(MobEffects.STRENGTH, 180, 2), effect(MobEffects.RESISTANCE, 180, 1),
                    effect(MobEffects.NIGHT_VISION, 600, 0));
            case IMMORTAL -> List.of(effect(MobEffects.REGENERATION, 60, 3), effect(MobEffects.ABSORPTION, 600, 9),
                    effect(MobEffects.HEALTH_BOOST, 600, 4), effect(MobEffects.RESISTANCE, 300, 2), effect(MobEffects.STRENGTH, 300, 2),
                    effect(MobEffects.SPEED, 600, 1), effect(MobEffects.FIRE_RESISTANCE, 600, 0), effect(ModEffects.SECOND_LIFE, 600, 0));
        };
    }

    private static MobEffectInstance effect(Holder<MobEffect> effect, int seconds, int amplifier) {
        return new MobEffectInstance(effect, seconds * 20, amplifier);
    }
}
