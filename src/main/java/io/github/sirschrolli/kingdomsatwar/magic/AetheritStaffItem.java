package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Aetherit-Stab: Rechtsklick wirkt den gewählten Zauber. Jeder Stab kennt den Aether-Blitz; weitere Zauber lernt er am
 * Magietisch aus Zaubertafeln, gewählt wird im Zauberkreis (Taste halten, Standard: V).
 *
 * <p>Jeder Zauber hat eine eigene Abklingzeit. Dafür trägt der Stab die Abklingzeit-Gruppe des gewählten Zaubers als
 * {@link DataComponents#USE_COOLDOWN}: Vanilla setzt sie nach jedem Zauber und zeigt sie in der Schnellleiste – beim
 * Wechsel auf einen anderen Zauber ist dessen eigene Abklingzeit zu sehen.
 */
public class AetheritStaffItem extends Item {
    public AetheritStaffItem(Properties properties) {
        super(properties);
    }

    /** Abklingzeit-Angabe eines neuen Stabs (Aether-Blitz, Stufe 1). */
    public static UseCooldown defaultCooldown() {
        return cooldownFor(Spell.AETHER_BOLT, 1);
    }

    private static UseCooldown cooldownFor(Spell spell, int level) {
        // etwas über der vollen Tickzahl, damit Vanilla beim Abrunden (Sekunden * 20) genau sie erhält
        return new UseCooldown((spell.cooldown(level) + 0.05f) / 20.0f, Optional.of(spell.cooldownGroup()));
    }

    /** Bringt die Abklingzeit-Angabe des Stabs auf den gewählten Zauber und seine Stufe. */
    public static void syncCooldown(ItemStack staff) {
        KnownSpells spells = KnownSpells.of(staff);
        UseCooldown wanted = cooldownFor(spells.selected(), spells.selectedLevel());
        if (!wanted.equals(staff.get(DataComponents.USE_COOLDOWN))) {
            staff.set(DataComponents.USE_COOLDOWN, wanted);
        }
    }

    /** Speichert neue Zauber am Stab (ohne Angabe, solange er nur den Aether-Blitz kennt). */
    public static void setSpells(ItemStack staff, KnownSpells spells) {
        if (spells.equals(KnownSpells.DEFAULT)) {
            staff.remove(ModDataComponents.SPELLS.get());
        } else {
            staff.set(ModDataComponents.SPELLS.get(), spells);
        }
        syncCooldown(staff);
    }

    /** Wählt einen bekannten Zauber. @return ob sich etwas geändert hat */
    public static boolean select(ItemStack staff, Spell spell) {
        KnownSpells spells = KnownSpells.of(staff);
        if (!spells.knows(spell) || spells.selected() == spell) {
            return false;
        }
        setSpells(staff, spells.select(spell));
        return true;
    }

    /** Der Stab in der Hand (Haupthand zuerst), sonst ein leerer Stapel. */
    public static ItemStack heldStaff(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof AetheritStaffItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack staff = player.getItemInHand(hand);
        KnownSpells spells = KnownSpells.of(staff);
        Spell spell = spells.selected();
        int spellLevel = spells.selectedLevel();
        if (!spell.canCast(level, player, spellLevel)) {
            return InteractionResult.FAIL;
        }
        if (level instanceof ServerLevel server) {
            if (!spell.cast(server, player, spellLevel)) {
                return InteractionResult.FAIL;
            }
            staff.hurtAndBreak(spell.cost(), player, hand);
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        // die Abklingzeit setzt Vanilla aus USE_COOLDOWN (Gruppe des gewählten Zaubers)
        return InteractionResult.SUCCESS;
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        syncCooldown(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        KnownSpells spells = KnownSpells.of(stack);
        builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.spells").withStyle(ChatFormatting.GRAY));
        for (Spell spell : spells.known()) {
            boolean selected = spell == spells.selected();
            builder.accept(Component.literal(selected ? " ▶ " : "    ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(spell.displayName().withColor(selected ? spell.color() : 0xB8B0C8))
                    .append(Component.literal(" " + Spell.numeral(spells.level(spell))).withStyle(ChatFormatting.DARK_GRAY)));
        }
        builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.wheel",
                Component.keybind("key.kingdomsatwar.spell_wheel").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
