package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.item.BlueprintItem;
import io.github.sirschrolli.kingdomsatwar.network.OpenTownPayload;
import java.util.List;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Prediction;
import org.jspecify.annotations.Nullable;

/** Was im Stadtfenster angeklickt wurde – auf dem Server ausgeführt und noch einmal geprüft. */
public final class TownActions {
    public static final int HIRE = 0;
    public static final int BLUEPRINT = 1;
    public static final int RENAME = 2;
    public static final int QUEEN = 3;
    public static final int DISMISS = 4;
    public static final int CANCEL = 5;
    public static final int REFRESH = 6;

    /** Wie weit man vom Banner weg sein darf, solange das Fenster offen ist. */
    public static final double RANGE = 16.0;

    private TownActions() {}

    /** Öffnet (oder erneuert) das Stadtfenster für einen Spieler. */
    public static void open(ServerPlayer player, Town town, BlockPos banner) {
        open(player, town, banner, null, true);
    }

    private static void open(ServerPlayer player, Town town, BlockPos banner, @Nullable Component notice, boolean good) {
        io.github.sirschrolli.kingdomsatwar.network.ModNetwork.send(player, new OpenTownPayload(banner, TownView.of(player, town), Optional.ofNullable(notice), good));
    }

    public static void handle(ServerPlayer player, BlockPos banner, int action, int arg, String text) {
        Towns towns = Towns.of(player.level().getServer());
        Town town = towns.byBanner(player.level().dimension(), banner);
        if (town == null || player.distanceToSqr(banner.getX() + 0.5, banner.getY(), banner.getZ() + 0.5) > RANGE * RANGE) {
            return;
        }
        boolean owner = town.owner().equals(player.getUUID());
        Component notice = null;
        boolean good = true;
        switch (action) {
            case HIRE -> {
                if (arg >= 0 && arg < Profession.values().length) {
                    Profession profession = Profession.values()[arg];
                    TownManager.HireResult result = TownManager.hire(player, town, profession);
                    good = result == TownManager.HireResult.HIRED;
                    notice = good ? Component.translatable("gui.kingdomsatwar.town.notice.hired", Component.translatable(profession.translationKey()))
                            : Component.translatable(result.key());
                }
            }
            case BLUEPRINT -> {
                if (owner && arg >= 0 && arg < BuildingType.values().length) {
                    BuildingType type = BuildingType.values()[arg];
                    if (town.tier().ordinal() < type.tier().ordinal()) {
                        good = false;
                        notice = Component.translatable(TownManager.PlaceResult.TIER.key());
                    } else {
                        player.getInventory().placeItemBackInInventory(BlueprintItem.of(type), Prediction.SERVER_ONLY);
                        notice = Component.translatable("message.kingdomsatwar.town.blueprint_taken", Component.translatable(type.translationKey()));
                    }
                }
            }
            case RENAME -> {
                String before = town.name();
                TownManager.rename(player, town, text);
                if (!before.equals(town.name())) {
                    notice = Component.translatable("gui.kingdomsatwar.town.notice.renamed", Component.literal(town.name()));
                }
            }
            case QUEEN -> TownManager.setQueen(player, town, arg == 1);
            case DISMISS -> {
                List<Town.Resident> residents = town.residents();
                if (owner && arg >= 0 && arg < residents.size()) {
                    Town.Resident r = residents.get(arg);
                    TownManager.dismiss(player, town, r.entity());
                    notice = Component.translatable("gui.kingdomsatwar.town.notice.dismissed", Component.literal(r.name()));
                }
            }
            case CANCEL -> {
                if (owner && arg >= 0 && arg < town.projects().size()) {
                    BuildingType type = town.projects().get(arg).type;
                    TownManager.cancelProject(player, town, arg);
                    notice = Component.translatable("gui.kingdomsatwar.town.notice.cancelled", Component.translatable(type.translationKey()));
                }
            }
            default -> {
            }
        }
        open(player, town, banner, notice, good);
    }
}
