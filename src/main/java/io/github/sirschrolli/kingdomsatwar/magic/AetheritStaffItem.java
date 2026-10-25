package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Aetherit-Stab: Rechtsklick wirkt einen Zauber. Ohne Rune ist es ein Aether-Blitz; hält man eine Rune in der
 * anderen Hand, wirkt der Stab den Zauber ihres Elements (Feuer, Eis, Blitz, Natur, Erde, Wasser).
 */
public class AetheritStaffItem extends Item {
    public AetheritStaffItem(Properties properties) {
        super(properties);
    }

    /** Die Rune in der anderen Hand (oder {@code null}). */
    public static @Nullable DragonType runeInOtherHand(Player player, InteractionHand staffHand) {
        ItemStack other = player.getItemInHand(staffHand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        return other.getItem() instanceof RuneItem rune ? rune.element() : null;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack staff = player.getItemInHand(hand);
        Spell spell = Spell.of(runeInOtherHand(player, hand));
        if (level instanceof ServerLevel server) {
            if (!spell.cast(server, player)) {
                return InteractionResult.FAIL;
            }
            staff.hurtAndBreak(spell.cost(), player, hand);
            player.getCooldowns().addCooldown(staff, spell.cooldown());
            player.awardStat(Stats.ITEM_USED.get(this));
        }
        return InteractionResult.SUCCESS;
    }
}
