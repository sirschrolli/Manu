package io.github.sirschrolli.kingdomsatwar.architect;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import io.github.sirschrolli.kingdomsatwar.item.SurveyorsRodItem;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.PlanListPayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import org.jspecify.annotations.Nullable;

/**
 * Dragon Architect auf dem Server: einen vermessenen Bereich als Bauplan zeichnen, Abschriften aus dem Verzeichnis
 * nehmen, Pläne löschen. Alles, was das Spiel schickt, wird hier noch einmal geprüft.
 */
public final class Architect {
    /** So weit darf man beim Zeichnen von der Mitte des Bereichs entfernt stehen. */
    public static final double RANGE = 96.0;

    private Architect() {}

    /** Der Vermessungsstab in einer der Hände ({@code null}: keiner). */
    public static @Nullable ItemStack rod(ServerPlayer player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof SurveyorsRodItem) {
                return stack;
            }
        }
        return null;
    }

    /** Zeichnet den vermessenen Bereich als Bauplan (siehe {@link io.github.sirschrolli.kingdomsatwar.network.SavePlanPayload}). */
    public static void save(ServerPlayer player, BlockPos first, BlockPos second, String name, int ground, Direction front) {
        ServerLevel level = player.level();
        MinecraftServer server = level.getServer();
        ItemStack rod = rod(player);
        if (rod == null) {
            return;
        }
        GlobalPos a = rod.get(ModDataComponents.SURVEY_FIRST.get());
        GlobalPos b = rod.get(ModDataComponents.SURVEY_SECOND.get());
        if (a == null || b == null || !a.dimension().equals(level.dimension()) || !b.dimension().equals(level.dimension())
                || !a.pos().equals(first) || !b.pos().equals(second)) {
            return; // nur, was der Stab in der Hand wirklich vermessen hat
        }
        if (!PlanScan.fits(first, second)) {
            fail(player, "too_large", PlanScan.MAX_WIDTH, PlanScan.MAX_HEIGHT);
            return;
        }
        BlockPos min = BlockPos.min(first, second);
        BlockPos max = BlockPos.max(first, second);
        double cx = (min.getX() + max.getX() + 1) / 2.0;
        double cz = (min.getZ() + max.getZ() + 1) / 2.0;
        if (player.distanceToSqr(cx, player.getY(), cz) > RANGE * RANGE) {
            fail(player, "too_far");
            return;
        }
        if (!level.hasChunksAt(min, max)) {
            fail(player, "not_loaded");
            return;
        }
        PlanLibrary library = PlanLibrary.of(server);
        if (library.byAuthor(player.getUUID()).size() >= PlanLibrary.MAX_PER_PLAYER) {
            fail(player, "too_many", PlanLibrary.MAX_PER_PLAYER);
            return;
        }
        boolean free = player.hasInfiniteMaterials();
        if (!free && player.getInventory().countItem(Items.PAPER) <= 0) {
            fail(player, "no_paper");
            return;
        }
        PlanScan scan;
        try {
            scan = PlanScan.scan(level, first, second, ground, front.getAxis().isHorizontal() ? front : Direction.NORTH, level.registryAccess());
        } catch (IllegalArgumentException e) {
            fail(player, "empty");
            return;
        }
        PlanInfo info;
        try {
            info = library.add(server, scan, name, player.getUUID(), player.getName().getString());
        } catch (IOException e) {
            KingdomsAtWar.LOGGER.error("Bauplan konnte nicht gespeichert werden", e);
            fail(player, "failed");
            return;
        }
        if (!free) {
            takePaper(player);
        }
        player.getInventory().placeItemBackInInventory(BlueprintItem.of(info), Prediction.SERVER_ONLY);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, SoundSource.PLAYERS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.WAX_ON, cx, max.getY() + 1.2, cz, 30, (max.getX() - min.getX()) / 3.0, 0.4,
                (max.getZ() - min.getZ()) / 3.0, 0.0);
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.architect.saved", Component.literal(info.name()),
                info.shape().sizeX(), info.height(), info.shape().sizeZ(), info.costs().blocks()).withStyle(ChatFormatting.GREEN));
        if (!scan.skipped().isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.architect.skipped", skippedList(scan.skipped(), 4))
                    .withStyle(ChatFormatting.GOLD));
        }
    }

    /** „3× Spawner, 1× Grundgestein …“ (die häufigsten zuerst). */
    public static MutableComponent skippedList(Map<Block, Integer> skipped, int max) {
        List<Map.Entry<Block, Integer>> list = new ArrayList<>(skipped.entrySet());
        list.sort((x, y) -> Integer.compare(y.getValue(), x.getValue()));
        MutableComponent out = Component.empty();
        for (int i = 0; i < list.size() && i < max; i++) {
            if (i > 0) {
                out.append(", ");
            }
            out.append(Component.translatable("message.kingdomsatwar.architect.count", list.get(i).getValue(), list.get(i).getKey().getName()));
        }
        if (list.size() > max) {
            out.append(" …");
        }
        return out;
    }

    private static void takePaper(ServerPlayer player) {
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (stack.is(Items.PAPER)) {
                stack.shrink(1);
                return;
            }
        }
    }

    private static void fail(ServerPlayer player, String why, Object... args) {
        player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.architect." + why, args).withStyle(ChatFormatting.RED));
        player.level().playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.VILLAGER_NO, SoundSource.PLAYERS, 0.5f, 1.4f);
    }

    // =====================================================================================
    // Verzeichnis (Stadtfenster)
    // =====================================================================================

    /** Schickt dem Spieler seine Pläne (für das Stadtfenster). */
    public static void sendList(ServerPlayer player) {
        ModNetwork.send(player, new PlanListPayload(PlanLibrary.of(player.level().getServer()).byAuthor(player.getUUID())));
    }

    /**
     * Eine Abschrift eines eigenen Plans (kostet ein Blatt Papier, im Kreativmodus nichts). Gibt zurück, ob es geklappt
     * hat (sonst fehlte das Papier oder der Plan).
     */
    public static boolean copy(ServerPlayer player, String id) {
        PlanLibrary library = PlanLibrary.of(player.level().getServer());
        PlanInfo info = library.info(id);
        if (info == null || library.hidden(id) || !info.author().equals(player.getUUID())) {
            return false;
        }
        if (!player.hasInfiniteMaterials()) {
            if (player.getInventory().countItem(Items.PAPER) <= 0) {
                return false;
            }
            takePaper(player);
        }
        player.getInventory().placeItemBackInInventory(BlueprintItem.of(info), Prediction.SERVER_ONLY);
        return true;
    }

    /** Nimmt einen eigenen Plan aus dem Verzeichnis. */
    public static boolean delete(ServerPlayer player, String id) {
        PlanLibrary library = PlanLibrary.of(player.level().getServer());
        PlanInfo info = library.info(id);
        if (info == null || !info.author().equals(player.getUUID())) {
            return false;
        }
        library.hide(player.level().getServer(), id);
        return true;
    }

    /** Ein Vermessungsstab (umsonst, wie die Baupläne der Siedlung). */
    public static void giveRod(ServerPlayer player) {
        player.getInventory().placeItemBackInInventory(new ItemStack(ModItems.SURVEYORS_ROD.get()), Prediction.SERVER_ONLY);
    }
}
