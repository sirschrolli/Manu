package io.github.sirschrolli.kingdomsatwar.command;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.logging.LogUtils;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces.Boss;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces.Landing;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces.Site;
import io.github.sirschrolli.kingdomsatwar.command.KawPlaces.Spot;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentUtils;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.level.border.WorldBorder;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.CustomClickActionEvent;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;

/**
 * {@code /kaw boss <Boss> [tp|awaken|reset|summon]} und {@code /kaw structure <Bauwerk> [tp]} (seit 1.1.2, mit Cheats): wo
 * der Hort eines Bosses oder das nächste Bauwerk liegt – aus jeder Dimension heraus. Die Koordinaten im Chat lassen sich
 * anklicken: Man reist sofort hin, auch in eine andere Dimension. Der Klick läuft über eine eigene Klick-Aktion (keine
 * Rückfrage wie bei Befehlen, die Rechte brauchen); ob man reisen darf, prüft der Server.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class KawLocate {
    private static final Logger LOGGER = LogUtils.getLogger();
    /** Klick auf Koordinaten. */
    public static final Identifier TRAVEL = KingdomsAtWar.id("travel");

    private KawLocate() {}

    /** {@code /kaw boss <Boss>}: nur die Bosse, je mit {@code tp} und (wo es geht) {@code awaken}, {@code reset}, {@code summon}. */
    public static LiteralArgumentBuilder<CommandSourceStack> bossCommand() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("boss").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Boss boss : KawPlaces.bosses()) {
            LiteralArgumentBuilder<CommandSourceStack> node = Commands.literal(boss.id())
                    .executes(context -> locate(context.getSource(), boss.site(), boss))
                    .then(Commands.literal("tp").executes(context -> travel(context.getSource(), boss.site())));
            if (boss.awaken() != null) {
                node.then(Commands.literal("awaken").executes(context -> act(context.getSource(), boss, boss.awaken())));
            }
            if (boss.reset() != null) {
                node.then(Commands.literal("reset").executes(context -> act(context.getSource(), boss, boss.reset())));
            }
            if (boss.summon() != null) {
                node.then(Commands.literal("summon").executes(context -> boss.summon().run(context.getSource(), context.getSource().getLevel(),
                        new Spot(BlockPos.containing(context.getSource().getPosition()), true, null), boss)));
            }
            root.then(node);
        }
        return root;
    }

    /** {@code /kaw structure <Bauwerk>}: nur die Bauwerke, je mit {@code tp}. */
    public static LiteralArgumentBuilder<CommandSourceStack> structureCommand() {
        LiteralArgumentBuilder<CommandSourceStack> root = Commands.literal("structure").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS));
        for (Site site : KawPlaces.sites()) {
            root.then(Commands.literal(site.id())
                    .executes(context -> locate(context.getSource(), site, null))
                    .then(Commands.literal("tp").executes(context -> travel(context.getSource(), site))));
        }
        return root;
    }

    // ---------------------------------------------------------------- Befehle

    private static @Nullable ServerLevel level(CommandSourceStack source, Site site) {
        ServerLevel level = source.getServer().getLevel(site.dimension());
        if (level == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.dim.missing"));
        }
        return level;
    }

    private static @Nullable Spot find(CommandSourceStack source, ServerLevel level, Site site) {
        Spot spot = site.finder().find(level, from(source, level));
        if (spot == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.locate.none", site.name(), DimensionCommand.name(level.dimension())));
        }
        return spot;
    }

    private static int locate(CommandSourceStack source, Site site, @Nullable Boss boss) {
        ServerLevel level = level(source, site);
        Spot spot = level == null ? null : find(source, level, site);
        if (spot == null) {
            return 0;
        }
        Component line = describe(source, level, site, boss, spot);
        source.sendSuccess(() -> line, false);
        return 1;
    }

    private static int travel(CommandSourceStack source, Site site) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = level(source, site);
        Spot spot = level == null ? null : find(source, level, site);
        if (spot == null) {
            return 0;
        }
        go(player, level, site, spot);
        return 1;
    }

    private static int act(CommandSourceStack source, Boss boss, KawPlaces.Action action) throws CommandSyntaxException {
        ServerLevel level = level(source, boss.site());
        Spot spot = level == null ? null : find(source, level, boss.site());
        if (spot == null) {
            return 0;
        }
        return action.run(source, level, spot, boss);
    }

    /**
     * Von wo aus gesucht wird: in derselben Dimension vom Standort, aus einer anderen von der Stelle, an die {@code /kaw dim}
     * einen brächte (im Nether ein Achtel der Koordinaten).
     */
    public static BlockPos from(CommandSourceStack source, ServerLevel level) {
        Vec3 pos = source.getPosition();
        if (source.getLevel() == level) {
            return BlockPos.containing(pos);
        }
        double scale = DimensionType.getTeleportationScale(source.getLevel().dimensionType(), level.dimensionType());
        WorldBorder border = level.getWorldBorder();
        return BlockPos.containing(Mth.clamp(pos.x * scale, border.getMinX() + 16, border.getMaxX() - 16), level.getSeaLevel(),
                Mth.clamp(pos.z * scale, border.getMinZ() + 16, border.getMaxZ() - 16));
    }

    // ---------------------------------------------------------------- Ausgabe

    /** „Der Stundenwächter – Uhrturm im Abgrund: [1234, 140, -567] (820 Blöcke entfernt)“ (die Entfernung nur in derselben Dimension). */
    public static Component describe(@Nullable CommandSourceStack source, ServerLevel level, Site site, @Nullable Boss boss, Spot spot) {
        Component where = Component.translatable("command.kingdomsatwar.locate.where", site.name().copy().withStyle(ChatFormatting.WHITE),
                DimensionCommand.name(level.dimension()));
        Component coordinates = coordinates(level, site, spot);
        MutableComponent line = boss == null
                ? Component.translatable("command.kingdomsatwar.locate.site", where, coordinates)
                : Component.translatable("command.kingdomsatwar.locate.boss", boss.name().copy().withStyle(ChatFormatting.GOLD), where, coordinates);
        if (source != null && source.getLevel() == level) {
            Vec3 pos = source.getPosition();
            double dx = spot.center().getX() + 0.5 - pos.x;
            double dz = spot.center().getZ() + 0.5 - pos.z;
            line.append(Component.translatable("command.kingdomsatwar.locate.distance", Mth.floor(Math.sqrt(dx * dx + dz * dz)))
                    .withStyle(ChatFormatting.GRAY));
        }
        return line.withStyle(ChatFormatting.GRAY);
    }

    /** Die Koordinaten als Link (grün, in eckigen Klammern, Höhe „~“, wenn sie nicht feststeht): ein Klick bringt einen hin. */
    public static Component coordinates(ServerLevel level, Site site, Spot spot) {
        BlockPos c = spot.center();
        MutableComponent text = ComponentUtils.wrapInSquareBrackets(Component.translatable("chat.coordinates", c.getX(),
                spot.knownY() ? String.valueOf(c.getY()) : "~", c.getZ()));
        CompoundTag payload = new CompoundTag();
        payload.putString("site", site.id());
        payload.putString("dim", level.dimension().identifier().toString());
        payload.putInt("x", c.getX());
        payload.putInt("y", c.getY());
        payload.putInt("z", c.getZ());
        Component hover = Component.translatable("command.kingdomsatwar.locate.click", site.name(), DimensionCommand.name(level.dimension()));
        return text.withStyle(style -> style.withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent.Custom(TRAVEL, Optional.of(payload)))
                .withHoverEvent(new HoverEvent.ShowText(hover)));
    }

    // ---------------------------------------------------------------- Reisen

    /** Bringt den Spieler an den Ort (und lädt dort, was zum Landen nötig ist). */
    public static void go(ServerPlayer player, ServerLevel level, Site site, Spot spot) {
        Landing landing = KawPlaces.safe(level, site.lander().land(level, spot));
        player.teleportTo(level, landing.x(), landing.y(), landing.z(), Set.of(), landing.yaw(), landing.pitch(), true);
        player.resetFallDistance();
        if (level.getFluidState(BlockPos.containing(landing.x(), landing.y() + 1.0, landing.z())).is(net.minecraft.tags.FluidTags.WATER)) {
            // unter Wasser angekommen (Bauwerke am Meeresgrund): eine Weile Luft, um sich umzusehen oder aufzutauchen
            player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WATER_BREATHING, 900, 0,
                    true, false, true));
        }
        level.playSound(null, BlockPos.containing(landing.x(), landing.y(), landing.z()), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS,
                0.8f, 1.2f);
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.locate.arrived", site.name(), DimensionCommand.name(level.dimension()),
                Mth.floor(landing.x()), Mth.floor(landing.y()), Mth.floor(landing.z())).withStyle(ChatFormatting.GOLD));
    }

    @SubscribeEvent
    static void onCustomClick(CustomClickActionEvent event) {
        if (!TRAVEL.equals(event.getIdentifier())) {
            return;
        }
        event.setCanceled(true);
        if (event.getPlayer() != null && event.getPayload() instanceof CompoundTag tag) {
            click(event.getPlayer(), tag);
        }
    }

    /**
     * Ein Klick auf Koordinaten. Was im Klick steht, kommt vom Spiel des Spielers – deshalb reist nur, wer die Befehle nutzen
     * darf (Cheats bzw. Operator), und nur zu einem Ort, den es gibt: Gesucht wird neu, von der angeklickten Stelle aus
     * (dort steht das Bauwerk, also findet sich genau dieses).
     */
    public static boolean click(ServerPlayer player, CompoundTag tag) {
        if (!Commands.LEVEL_GAMEMASTERS.check(player.permissions())) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.locate.denied").withStyle(ChatFormatting.RED));
            return false;
        }
        Site site = KawPlaces.site(tag.getStringOr("site", ""));
        Identifier dimension = Identifier.tryParse(tag.getStringOr("dim", ""));
        if (site == null || dimension == null || !site.dimension().identifier().equals(dimension)) {
            return false;
        }
        ServerLevel level = player.level().getServer().getLevel(site.dimension());
        if (level == null) {
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.dim.missing").withStyle(ChatFormatting.RED));
            return false;
        }
        WorldBorder border = level.getWorldBorder();
        int x = Mth.floor(Mth.clamp(tag.getIntOr("x", 0), border.getMinX() + 16, border.getMaxX() - 16));
        int z = Mth.floor(Mth.clamp(tag.getIntOr("z", 0), border.getMinZ() + 16, border.getMaxZ() - 16));
        try {
            Spot spot = site.finder().find(level, new BlockPos(x, level.getSeaLevel(), z));
            if (spot == null) {
                player.sendSystemMessage(Component.translatable("command.kingdomsatwar.locate.none", site.name(),
                        DimensionCommand.name(level.dimension())).withStyle(ChatFormatting.RED));
                return false;
            }
            go(player, level, site, spot);
            return true;
        } catch (RuntimeException e) {
            LOGGER.warn("Reise zu {} ({}, {}) fehlgeschlagen", site.id(), x, z, e);
            player.sendSystemMessage(Component.translatable("command.kingdomsatwar.locate.none", site.name(),
                    DimensionCommand.name(level.dimension())).withStyle(ChatFormatting.RED));
            return false;
        }
    }
}
