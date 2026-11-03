package io.github.sirschrolli.kingdomsatwar.item;

import io.github.sirschrolli.kingdomsatwar.architect.Architect;
import io.github.sirschrolli.kingdomsatwar.architect.PlanScan;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.OpenSavePlanPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Der Vermessungsstab (Dragon Architect): zwei Ecken eines Gebäudes antippen, dann schleichend benutzen – es öffnet sich
 * das Fenster „Bauplan zeichnen“. Ein drittes Antippen beginnt einen neuen Bereich, schleichend ins Leere benutzt ohne
 * zwei Ecken setzt ihn zurück. Solange man den Stab hält, zeigt ein goldener Rahmen den Bereich.
 */
public class SurveyorsRodItem extends Item {
    public SurveyorsRodItem(Item.Properties properties) {
        super(properties);
    }

    public static @Nullable GlobalPos first(ItemStack stack) {
        return stack.get(ModDataComponents.SURVEY_FIRST.get());
    }

    public static @Nullable GlobalPos second(ItemStack stack) {
        return stack.get(ModDataComponents.SURVEY_SECOND.get());
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (player.isSecondaryUseActive()) {
            return this.use(context.getLevel(), player, context.getHand());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            mark(serverPlayer, context.getItemInHand(), context.getClickedPos());
        }
        return InteractionResult.SUCCESS;
    }

    /** Setzt die nächste Ecke (die erste, die zweite, dann wieder von vorn). */
    static void mark(ServerPlayer player, ItemStack stack, BlockPos pos) {
        Level level = player.level();
        GlobalPos here = GlobalPos.of(level.dimension(), pos.immutable());
        GlobalPos first = first(stack);
        GlobalPos second = second(stack);
        if (first == null || second != null || !first.dimension().equals(level.dimension())) {
            stack.set(ModDataComponents.SURVEY_FIRST.get(), here);
            stack.remove(ModDataComponents.SURVEY_SECOND.get());
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect.first", pos.getX(), pos.getY(), pos.getZ())
                    .withStyle(ChatFormatting.GREEN));
            level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 0.6f, 1.6f);
            return;
        }
        if (!PlanScan.fits(first.pos(), pos)) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect.too_large", PlanScan.MAX_WIDTH, PlanScan.MAX_HEIGHT)
                    .withStyle(ChatFormatting.RED));
            level.playSound(null, pos, SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.5f, 1.4f);
            return;
        }
        stack.set(ModDataComponents.SURVEY_SECOND.get(), here);
        BlockPos a = first.pos();
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect.second", Math.abs(a.getX() - pos.getX()) + 1,
                Math.abs(a.getY() - pos.getY()) + 1, Math.abs(a.getZ() - pos.getZ()) + 1).withStyle(ChatFormatting.AQUA));
        level.playSound(null, pos, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 0.6f, 1.25f);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        if (!player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer serverPlayer) {
            GlobalPos first = first(stack);
            GlobalPos second = second(stack);
            if (first != null && second != null && first.dimension().equals(level.dimension()) && second.dimension().equals(level.dimension())) {
                BlockPos min = BlockPos.min(first.pos(), second.pos());
                BlockPos max = BlockPos.max(first.pos(), second.pos());
                if (serverPlayer.distanceToSqr((min.getX() + max.getX() + 1) / 2.0, serverPlayer.getY(), (min.getZ() + max.getZ() + 1) / 2.0)
                        > Architect.RANGE * Architect.RANGE) {
                    serverPlayer.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect.too_far").withStyle(ChatFormatting.RED));
                } else {
                    ModNetwork.send(serverPlayer, new OpenSavePlanPayload(first.pos(), second.pos()));
                    level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.BOOK_PAGE_TURN,
                            SoundSource.PLAYERS, 0.8f, 1.0f);
                }
            } else if (first != null) {
                stack.remove(ModDataComponents.SURVEY_FIRST.get());
                stack.remove(ModDataComponents.SURVEY_SECOND.get());
                serverPlayer.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect.cleared").withStyle(ChatFormatting.GRAY));
                level.playSound(null, serverPlayer.getX(), serverPlayer.getY(), serverPlayer.getZ(), SoundEvents.BOOK_PAGE_TURN,
                        SoundSource.PLAYERS, 0.6f, 0.7f);
            } else {
                serverPlayer.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect.how").withStyle(ChatFormatting.GRAY));
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public boolean canDestroyBlock(ItemStack stack, net.minecraft.world.level.block.state.BlockState state, Level level, BlockPos pos,
            net.minecraft.world.entity.LivingEntity entity) {
        return !(entity instanceof Player p) || !p.isCreative();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        GlobalPos first = first(stack);
        GlobalPos second = second(stack);
        if (first != null && second != null) {
            BlockPos a = first.pos();
            BlockPos b = second.pos();
            builder.accept(Component.translatable("item.kingdomsatwar.surveyors_rod.area", Math.abs(a.getX() - b.getX()) + 1,
                    Math.abs(a.getY() - b.getY()) + 1, Math.abs(a.getZ() - b.getZ()) + 1).withStyle(ChatFormatting.GOLD));
        } else if (first != null) {
            builder.accept(Component.translatable("item.kingdomsatwar.surveyors_rod.one").withStyle(ChatFormatting.GOLD));
        }
        builder.accept(Component.translatable("item.kingdomsatwar.surveyors_rod.lore1").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.kingdomsatwar.surveyors_rod.lore2").withStyle(ChatFormatting.GRAY));
        builder.accept(Component.translatable("item.kingdomsatwar.surveyors_rod.lore3").withStyle(ChatFormatting.DARK_GRAY));
    }
}
