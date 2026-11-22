package io.github.sirschrolli.kingdomsatwar.waystone;

import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.LodestoneTracker;

/**
 * Was der Wegkompass tut (seit 1.12): Wegsteine merken (bis zu {@link Waypoints#MAX}), eins davon als Ziel wählen, Wegsteine
 * vergessen. Das Ziel bekommt die Nadel über den {@code lodestone_tracker} von Minecraft (ohne Leitstein) – so dreht sie
 * sich wie bei einem Leitsteinkompass; ohne Ziel zeigt sie zum Spawn.
 */
public final class WaystoneCompass {
    /** Aktionen aus dem Kompassfenster. */
    public static final int SELECT = 0;
    public static final int REMOVE = 1;
    public static final int CLEAR = 2;

    private WaystoneCompass() {}

    public static Waypoints of(ItemStack stack) {
        return stack.getOrDefault(ModDataComponents.WAYPOINTS.get(), Waypoints.EMPTY);
    }

    /** Speichert die Wegsteine und richtet die Nadel auf das Ziel (ohne Ziel zum Spawn). */
    public static void set(ItemStack stack, Waypoints waypoints) {
        stack.set(ModDataComponents.WAYPOINTS.get(), waypoints);
        Waypoints.Waypoint target = waypoints.target();
        if (target == null) {
            stack.remove(DataComponents.LODESTONE_TRACKER);
        } else {
            stack.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(target.pos()), false));
        }
    }

    /**
     * Mit dem Kompass an einen Wegstein: Kennt er ihn noch nicht, merkt er ihn sich und nimmt ihn als Ziel; kennt er ihn, wird er
     * das Ziel (und lernt einen neuen Namen). Mit acht gemerkten Wegsteinen ist der Kompass voll.
     */
    public static void remember(ServerPlayer player, ItemStack stack, ServerLevel level, BlockPos pos, WaystoneBlockEntity stone) {
        Waypoints waypoints = of(stack);
        GlobalPos at = GlobalPos.of(level.dimension(), pos);
        Component name = stone.getName();
        int index = waypoints.indexOf(at);
        if (index >= 0) {
            set(stack, waypoints.renamed(index, name).select(index));
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.target", name.copy().withStyle(ChatFormatting.AQUA))
                    .withStyle(ChatFormatting.GOLD));
        } else if (waypoints.full()) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.full", Waypoints.MAX).withStyle(ChatFormatting.RED));
            level.playSound(null, pos, SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED, SoundSource.PLAYERS, 0.8f, 0.7f);
            return;
        } else {
            Waypoints next = waypoints.with(new Waypoints.Waypoint(at, name));
            set(stack, next.select(next.entries().size() - 1));
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.saved", name.copy().withStyle(ChatFormatting.AQUA),
                    next.entries().size(), Waypoints.MAX).withStyle(ChatFormatting.GREEN));
            level.sendParticles(ParticleTypes.END_ROD, pos.getX() + 0.5, pos.getY() + 1.1, pos.getZ() + 0.5, 14, 0.25, 0.3, 0.25, 0.03);
        }
        level.playSound(null, pos, SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.ENCHANT, pos.getX() + 0.5, pos.getY() + 1.3, pos.getZ() + 0.5, 24, 0.35, 0.35, 0.35, 0.5);
    }

    /** Eine Aktion aus dem Kompassfenster (der Kompass liegt in {@code hand}); der Server prüft alles selbst. */
    public static void act(ServerPlayer player, InteractionHand hand, int action, int index) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(stack.getItem() instanceof WaystoneCompassItem)) {
            return;
        }
        Waypoints waypoints = of(stack);
        switch (action) {
            case SELECT -> {
                if (index < 0 || index >= waypoints.entries().size()) {
                    return;
                }
                Waypoints.Waypoint waypoint = waypoints.entries().get(index);
                if (gone(player.level().getServer(), waypoint)) {
                    set(stack, waypoints.without(index));
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.gone", waypoint.name()).withStyle(ChatFormatting.RED));
                    player.level().playSound(null, player.blockPosition(), SoundEvents.CHISELED_BOOKSHELF_PICKUP_ENCHANTED, SoundSource.PLAYERS, 0.8f,
                            0.7f);
                    return;
                }
                if (waypoints.selected() == index) {
                    set(stack, waypoints.select(-1));
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.spawn").withStyle(ChatFormatting.GRAY));
                } else {
                    set(stack, waypoints.select(index));
                    player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.target",
                            waypoint.name().copy().withStyle(ChatFormatting.AQUA)).withStyle(ChatFormatting.GOLD));
                }
                player.level().playSound(null, player.blockPosition(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 0.7f, 1.2f);
            }
            case REMOVE -> {
                if (index < 0 || index >= waypoints.entries().size()) {
                    return;
                }
                Waypoints.Waypoint waypoint = waypoints.entries().get(index);
                set(stack, waypoints.without(index));
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.forgotten", waypoint.name()).withStyle(ChatFormatting.GRAY));
                player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
            case CLEAR -> {
                set(stack, waypoints.select(-1));
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.waystone.spawn").withStyle(ChatFormatting.GRAY));
            }
            default -> {
            }
        }
    }

    /** Steht der Wegstein nicht mehr? (Prüfbar nur, solange seine Gegend geladen ist – sonst gilt er als da.) */
    public static boolean gone(MinecraftServer server, Waypoints.Waypoint waypoint) {
        ServerLevel level = server.getLevel(waypoint.pos().dimension());
        if (level == null) {
            return true;
        }
        BlockPos pos = waypoint.pos().pos();
        return level.isLoaded(pos) && !(level.getBlockState(pos).getBlock() instanceof WaystoneBlock);
    }
}
