package io.github.sirschrolli.kingdomsatwar.entity.villager;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.Locale;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import org.jspecify.annotations.Nullable;

/**
 * Die Berufe der Dorfbewohner. Zusammen decken sie alles ab, was es früher bei den Dorfbewohnern zu tauschen gab – nur
 * bezahlt man jetzt mit Münzen (siehe {@link VillageTrades}). {@code variants}: Anzahl der Gesichter (das letzte ist
 * eine Frau).
 */
public enum VillageProfession implements StringRepresentable {
    /** Tagelöhner: hilft hier und da, handelt nicht – weiß aber, was man sich erzählt. */
    PEASANT(3),
    FARMER(3),
    FISHER(3),
    SHEPHERD(3),
    /** Bogner: Bögen, Armbrüste und Pfeile. */
    FLETCHER(3),
    /** Schreiber: Bücher, auch verzauberte. */
    SCRIBE(3),
    CARTOGRAPHER(3),
    /** Kräuterkundige: Tränke, Redstone, Lapis, Enderperlen. */
    HERBALIST(3),
    ARMORER(3),
    WEAPONSMITH(3),
    TOOLSMITH(3),
    BUTCHER(3),
    /** Gerber: Leder, Sättel, Pferderüstung. */
    TANNER(3),
    MASON(3),
    BAKER(3),
    INNKEEPER(3),
    /** Händler am Markt: Waren aus fernen Ländern. */
    MERCHANT(3);

    public static final Codec<VillageProfession> CODEC = StringRepresentable.fromEnum(VillageProfession::values);

    private final int variants;

    VillageProfession(int variants) {
        this.variants = variants;
    }

    public String id() {
        return this.name().toLowerCase(Locale.ROOT);
    }

    @Override
    public String getSerializedName() {
        return this.id();
    }

    public int variants() {
        return this.variants;
    }

    /** Handelt mit Spielern (alle außer den Tagelöhnern). */
    public boolean trader() {
        return this != PEASANT;
    }

    public String translationKey() {
        return "villager.kingdomsatwar.profession." + this.id();
    }

    /** Hält bei der Arbeit einen Hammer bzw. Meißel bereit (für die Haltung im Bild). */
    public boolean hammer() {
        return this == ARMORER || this == WEAPONSMITH || this == TOOLSMITH || this == MASON;
    }

    /** Was der Bewohner bei der Arbeit in der Hand hält. */
    public ItemStack workItem() {
        return switch (this) {
            case PEASANT, MERCHANT -> ItemStack.EMPTY;
            case FARMER -> new ItemStack(Items.IRON_HOE);
            case FISHER -> new ItemStack(Items.FISHING_ROD);
            case SHEPHERD -> new ItemStack(Items.SHEARS);
            case FLETCHER -> new ItemStack(Items.ARROW);
            case SCRIBE -> new ItemStack(Items.WRITABLE_BOOK);
            case CARTOGRAPHER -> new ItemStack(Items.MAP);
            case HERBALIST -> PotionContents.createItemStack(Items.POTION, Potions.HEALING);
            case ARMORER, WEAPONSMITH, TOOLSMITH -> new ItemStack(Items.MACE);
            case BUTCHER -> new ItemStack(Items.IRON_AXE);
            case TANNER -> new ItemStack(Items.LEATHER);
            case MASON -> new ItemStack(Items.IRON_PICKAXE);
            case BAKER -> new ItemStack(Items.BREAD);
            case INNKEEPER -> new ItemStack(ModItems.ALE.get());
        };
    }

    /** Geräusch der Arbeit ({@code null}: arbeitet leise). */
    public @Nullable SoundEvent workSound() {
        return switch (this) {
            case PEASANT, MERCHANT, INNKEEPER -> null;
            case FARMER -> SoundEvents.VILLAGER_WORK_FARMER;
            case FISHER -> SoundEvents.VILLAGER_WORK_FISHERMAN;
            case SHEPHERD -> SoundEvents.VILLAGER_WORK_SHEPHERD;
            case FLETCHER -> SoundEvents.VILLAGER_WORK_FLETCHER;
            case SCRIBE -> SoundEvents.VILLAGER_WORK_LIBRARIAN;
            case CARTOGRAPHER -> SoundEvents.VILLAGER_WORK_CARTOGRAPHER;
            case HERBALIST -> SoundEvents.VILLAGER_WORK_CLERIC;
            case ARMORER -> SoundEvents.VILLAGER_WORK_ARMORER;
            case WEAPONSMITH -> SoundEvents.VILLAGER_WORK_WEAPONSMITH;
            case TOOLSMITH -> SoundEvents.VILLAGER_WORK_TOOLSMITH;
            case BUTCHER, BAKER -> SoundEvents.VILLAGER_WORK_BUTCHER;
            case TANNER -> SoundEvents.VILLAGER_WORK_LEATHERWORKER;
            case MASON -> SoundEvents.VILLAGER_WORK_MASON;
        };
    }

    public static VillageProfession byId(String id) {
        for (VillageProfession p : values()) {
            if (p.id().equals(id)) {
                return p;
            }
        }
        return PEASANT;
    }
}
