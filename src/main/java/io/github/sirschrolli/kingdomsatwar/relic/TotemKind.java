package io.github.sirschrolli.kingdomsatwar.relic;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.Holder;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jspecify.annotations.Nullable;

/**
 * Die neun Totems der Mod (seit 0.80). Jedes tut zweierlei:
 * <ul>
 *     <li><b>In der Zweithand</b> wirkt es dauernd ({@link #attributes()}, {@link #aura()} und {@link Totems}).</li>
 *     <li><b>Beim Tod</b> hält es ihn auf wie das Totem der Unsterblichkeit – aber sein Träger steht mit
 *     {@link #reviveHealth() mehr Herzen} wieder auf, bekommt eigene Zustände ({@link #reviveEffects()}) und entfesselt die
 *     Kraft seines Elements ({@link Totems#revive}).</li>
 * </ul>
 * Sechs gehören den Drachen (aus ihren Schuppen), eines den Schatten, eines der Leere – und das Totem des Schöpfers
 * vereint die sechs der Drachen: Es hält dreimal.
 */
public enum TotemKind implements StringRepresentable {
    /** Glut: Feuer kann dir nichts, Angreifer brennen; beim Tod eine Feuernova. */
    EMBER("ember", 16.0f, 1, 0xFF8A2A, 2, 2),
    /** Frost: kein Erfrieren, Angreifer erstarren; beim Tod eine Frostnova. */
    FROST("frost", 16.0f, 1, 0x9AD8FF, 2, 2),
    /** Sturm: schneller laufen und schlagen; beim Tod fahren Blitze in die Feinde. */
    STORM("storm", 16.0f, 1, 0xFFE95A, 0, 2),
    /** Fels: Rüstung, Härte, fester Stand; beim Tod ein Erdstoß und ein Panzer aus zehn Extraherzen. */
    STONE("stone", 20.0f, 1, 0xC8C8B4, 0, 2),
    /** Hain: heilt langsam, kein Gift; beim Tod zwölf Herzen und Heilung für Gefährten. */
    GROVE("grove", 24.0f, 1, 0x7CD05C, 2, 2),
    /** Gezeiten: atmen, sehen und schwimmen unter Wasser; beim Tod eine Flutwelle. */
    TIDE("tide", 16.0f, 1, 0x3CC4C8, 1, 2),
    /** Schatten: Nachtsicht, Feinde bemerken dich erst aus halber Entfernung; beim Tod verschwindest du. */
    SHADOW("shadow", 16.0f, 1, 0xB070F0, 2, 2),
    /** Leere: kein Fallschaden; rettet auch aus dem Nichts unter der Welt. */
    VOID("void", 16.0f, 1, 0x4CBCE6, 0, 2),
    /** Schöpfer: fünf Herzen mehr; beim Tod volle Herzen und eine Lichtnova – dreimal. */
    CREATOR("creator", -1.0f, 3, 0xFFD76A, 0, 3);

    public static final Codec<TotemKind> CODEC = StringRepresentable.fromEnum(TotemKind::values);

    private final String id;
    private final float reviveHealth;
    private final int charges;
    private final int color;
    private final int passiveLines;
    private final int reviveLines;

    TotemKind(String id, float reviveHealth, int charges, int color, int passiveLines, int reviveLines) {
        this.id = id;
        this.reviveHealth = reviveHealth;
        this.charges = charges;
        this.color = color;
        this.passiveLines = passiveLines;
        this.reviveLines = reviveLines;
    }

    /** Zeilen am Gegenstand über die Wirkung in der Zweithand (neben den Werten, die Minecraft selbst nennt). */
    public int passiveLines() {
        return this.passiveLines;
    }

    /** Zeilen am Gegenstand über das, was beim Tod geschieht. */
    public int reviveLines() {
        return this.reviveLines;
    }

    public String id() {
        return this.id;
    }

    /** Name des Gegenstands ({@code totem_…}). */
    public String itemName() {
        return "totem_" + this.id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    /** Mit so vielen Lebenspunkten steht der Träger wieder auf (−1: mit allen). */
    public float reviveHealth() {
        return this.reviveHealth;
    }

    /** So oft hält das Totem den Tod auf, bevor es zerfällt. */
    public int charges() {
        return this.charges;
    }

    /** Farbe für Schrift und Funken. */
    public int color() {
        return this.color;
    }

    public String key(String part) {
        return "totem." + KingdomsAtWar.MODID + "." + this.id + "." + part;
    }

    /** Was das Totem in der Zweithand an den Werten des Trägers ändert (steht von selbst am Gegenstand). */
    public ItemAttributeModifiers attributes() {
        ItemAttributeModifiers.Builder builder = ItemAttributeModifiers.builder();
        switch (this) {
            case STORM -> {
                add(builder, Attributes.MOVEMENT_SPEED, "speed", 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                add(builder, Attributes.ATTACK_SPEED, "attack_speed", 0.10, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            }
            case STONE -> {
                add(builder, Attributes.ARMOR, "armor", 3.0, AttributeModifier.Operation.ADD_VALUE);
                add(builder, Attributes.ARMOR_TOUGHNESS, "toughness", 2.0, AttributeModifier.Operation.ADD_VALUE);
                add(builder, Attributes.KNOCKBACK_RESISTANCE, "knockback", 0.4, AttributeModifier.Operation.ADD_VALUE);
            }
            case FROST -> add(builder, Attributes.ARMOR, "armor", 2.0, AttributeModifier.Operation.ADD_VALUE);
            case TIDE -> add(builder, Attributes.WATER_MOVEMENT_EFFICIENCY, "swim", 0.5, AttributeModifier.Operation.ADD_VALUE);
            case VOID -> add(builder, Attributes.FALL_DAMAGE_MULTIPLIER, "fall", -1.0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
            case CREATOR -> add(builder, Attributes.MAX_HEALTH, "health", 10.0, AttributeModifier.Operation.ADD_VALUE);
            default -> { }
        }
        return builder.build();
    }

    private void add(ItemAttributeModifiers.Builder builder, Holder<Attribute> attribute, String name, double amount,
            AttributeModifier.Operation operation) {
        builder.add(attribute, new AttributeModifier(KingdomsAtWar.id("totem." + this.id + "." + name), amount, operation), EquipmentSlotGroup.OFFHAND);
    }

    /** Der Zustand, den das Totem in der Zweithand dauernd gibt (null: keiner). */
    public @Nullable Holder<MobEffect> aura() {
        return switch (this) {
            case EMBER -> MobEffects.FIRE_RESISTANCE;
            case TIDE -> MobEffects.CONDUIT_POWER;
            case SHADOW -> MobEffects.NIGHT_VISION;
            default -> null;
        };
    }

    /** Die Zustände nach dem Aufstehen (Minecraft hat vorher alle genommen). */
    public List<MobEffectInstance> reviveEffects() {
        return switch (this) {
            case EMBER -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.FIRE_RESISTANCE, 120, 0),
                    effect(MobEffects.ABSORPTION, 30, 1));
            case FROST -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.RESISTANCE, 20, 1),
                    effect(MobEffects.ABSORPTION, 30, 1));
            case STORM -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.SPEED, 30, 1), effect(MobEffects.HASTE, 30, 1));
            case STONE -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.ABSORPTION, 60, 4),
                    effect(MobEffects.RESISTANCE, 8, 2));
            case GROVE -> List.of(effect(MobEffects.REGENERATION, 30, 2), effect(MobEffects.SATURATION, 1, 3));
            case TIDE -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.CONDUIT_POWER, 180, 0),
                    effect(MobEffects.DOLPHINS_GRACE, 60, 0), effect(MobEffects.WATER_BREATHING, 180, 0));
            case SHADOW -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.INVISIBILITY, 20, 0),
                    effect(MobEffects.SPEED, 20, 1));
            case VOID -> List.of(effect(MobEffects.REGENERATION, 20, 1), effect(MobEffects.SLOW_FALLING, 30, 0));
            case CREATOR -> List.of(effect(MobEffects.REGENERATION, 30, 2), effect(MobEffects.ABSORPTION, 120, 4),
                    effect(MobEffects.RESISTANCE, 30, 1), effect(MobEffects.FIRE_RESISTANCE, 120, 0));
        };
    }

    private static MobEffectInstance effect(Holder<MobEffect> effect, int seconds, int amplifier) {
        return new MobEffectInstance(effect, seconds * 20, amplifier);
    }

    public static @Nullable TotemKind byId(String id) {
        String wanted = id.toLowerCase(Locale.ROOT);
        for (TotemKind kind : values()) {
            if (kind.id.equals(wanted)) {
                return kind;
            }
        }
        return null;
    }
}
