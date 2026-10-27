package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.SeatEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.world.SealState;
import it.unimi.dsi.fastutil.ints.IntList;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.FireworkExplosion;
import net.minecraft.world.item.component.Fireworks;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

/**
 * Die Krönung: Wer das Siegel gebrochen hat, eine Stadt regiert, das höchste Ansehen genießt und die Königskrone trägt,
 * setzt sich im eigenen Thronsaal auf den Thron. Glocken läuten, goldenes Licht steigt um den Thron auf, die Bürger
 * eilen herbei, Feuerwerk erhellt den Himmel – und der Spieler wird König (oder Königin). Danach geben die eigenen
 * Bürger alles umsonst.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Coronation {
    /** Dauer der Zeremonie in Ticks. */
    public static final int DURATION = 220;
    private static final Map<UUID, Ceremony> RUNNING = new HashMap<>();

    private record Ceremony(UUID town, BlockPos throne, int[] ticks) {}

    private Coronation() {}

    /** Welche Voraussetzungen erfüllt sind (Bits aus {@link TownView}). */
    public static int requirements(ServerPlayer player, Town town) {
        int bits = 0;
        MinecraftServer server = player.level().getServer();
        if (SealState.of(server.overworld()).isBroken()) {
            bits |= TownView.CROWN_SEAL;
        }
        if (town.tier() == TownTier.CITY) {
            bits |= TownView.CROWN_CITY;
        }
        if (Reputation.rank(player) == Reputation.Rank.LEGENDARY) {
            bits |= TownView.CROWN_REPUTATION;
        }
        if (town.count(BuildingType.THRONE_HALL) > 0) {
            bits |= TownView.CROWN_HALL;
        }
        if (player.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.GEMSTONE_CROWN.get())) {
            bits |= TownView.CROWN_CROWN;
        }
        if (town.crowned()) {
            bits |= TownView.CROWN_DONE;
        }
        return bits;
    }

    public static final int ALL = TownView.CROWN_SEAL | TownView.CROWN_CITY | TownView.CROWN_REPUTATION | TownView.CROWN_HALL
            | TownView.CROWN_CROWN;

    /** Ein Spieler hat sich auf einen Thron gesetzt. */
    public static void onSit(ServerPlayer player, BlockPos throne) {
        MinecraftServer server = player.level().getServer();
        Town town = Towns.of(server).byOwner(player.getUUID());
        if (town == null || !isOwnThrone(town, player.level(), throne)) {
            return;
        }
        if (town.crowned()) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.crown.already", title(town), Component.literal(town.name()))
                    .withStyle(ChatFormatting.GOLD));
            return;
        }
        int bits = requirements(player, town);
        if ((bits & ALL) != ALL) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.crown.missing").withStyle(ChatFormatting.GOLD));
            for (int bit : new int[] {TownView.CROWN_SEAL, TownView.CROWN_CITY, TownView.CROWN_REPUTATION, TownView.CROWN_HALL,
                    TownView.CROWN_CROWN}) {
                boolean ok = (bits & bit) != 0;
                player.sendSystemMessage(Component.literal(ok ? " ✔ " : " ✘ ").withStyle(ok ? ChatFormatting.GREEN : ChatFormatting.RED)
                        .append(Component.translatable("gui.kingdomsatwar.town.crown." + Integer.numberOfTrailingZeros(bit))
                                .withStyle(ok ? ChatFormatting.GRAY : ChatFormatting.WHITE)));
            }
            return;
        }
        if (!RUNNING.containsKey(player.getUUID())) {
            RUNNING.put(player.getUUID(), new Ceremony(town.id(), throne.immutable(), new int[] {0}));
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.crown.begins").withStyle(ChatFormatting.GOLD));
        }
    }

    /** Der Thron im Thronsaal der eigenen Stadt? */
    static boolean isOwnThrone(Town town, ServerLevel level, BlockPos throne) {
        if (!level.dimension().equals(town.dimension())) {
            return false;
        }
        for (Town.Building b : town.buildings()) {
            if (b.type() == BuildingType.THRONE_HALL) {
                for (BlockPos mark : b.type().blueprint().marks()) {
                    if (b.world(mark).equals(throne)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static Component title(Town town) {
        return Component.translatable(town.queen() ? "message.kingdomsatwar.crown.queen" : "message.kingdomsatwar.crown.king");
    }

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        if (RUNNING.isEmpty()) {
            return;
        }
        MinecraftServer server = event.getServer();
        List<UUID> done = new ArrayList<>();
        for (Map.Entry<UUID, Ceremony> entry : RUNNING.entrySet()) {
            ServerPlayer player = server.getPlayerList().getPlayer(entry.getKey());
            Ceremony c = entry.getValue();
            Town town = Towns.of(server).byId(c.town());
            if (player == null || town == null || !(player.getVehicle() instanceof SeatEntity)
                    || player.blockPosition().distSqr(c.throne()) > 4) {
                if (player != null) {
                    player.sendSystemMessage(Component.translatable("message.kingdomsatwar.crown.aborted").withStyle(ChatFormatting.RED));
                }
                done.add(entry.getKey());
                continue;
            }
            if (tick(player, town, c)) {
                done.add(entry.getKey());
            }
        }
        done.forEach(RUNNING::remove);
    }

    /** Ein Tick der Zeremonie; {@code true}, wenn sie vorbei ist. */
    private static boolean tick(ServerPlayer player, Town town, Ceremony c) {
        ServerLevel level = player.level();
        int t = c.ticks()[0]++;
        Vec3 throne = Vec3.atBottomCenterOf(c.throne());
        // goldenes Licht steigt spiralförmig um den Thron auf
        double angle = t * 0.35;
        double radius = 2.2 - Math.min(1.6, t / 140.0);
        level.sendParticles(ParticleTypes.END_ROD, throne.x + Math.cos(angle) * radius, throne.y + 0.2 + (t % 60) / 20.0,
                throne.z + Math.sin(angle) * radius, 1, 0, 0, 0, 0);
        level.sendParticles(ParticleTypes.WAX_ON, throne.x + Math.cos(angle + Math.PI) * radius, throne.y + 0.4 + (t % 40) / 16.0,
                throne.z + Math.sin(angle + Math.PI) * radius, 1, 0, 0, 0, 0);
        if (t == 0) {
            gatherCitizens(level, town, c.throne());
        }
        if (t % 40 == 0 && t <= 160) {
            level.playSound(null, c.throne(), SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 0.8f + t / 400.0f);
        }
        if (t == 60 || t == 90) {
            level.playSound(null, c.throne(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.RECORDS, 1.5f, 1.0f);
            level.playSound(null, c.throne(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.RECORDS, 1.5f, 1.5f);
        }
        if (t >= 120 && t % 12 == 0 && t <= 200) {
            launchFirework(level, c.throne(), t);
        }
        if (t == 150) {
            crown(player, town, c.throne());
        }
        return t >= DURATION;
    }

    private static void crown(ServerPlayer player, Town town, BlockPos throne) {
        MinecraftServer server = player.level().getServer();
        town.setCrowned(true);
        Towns.of(server).save(server);
        Reputation.add(player, Reputation.NYXAR);
        io.github.sirschrolli.kingdomsatwar.story.Story.onCrowned(player);
        Component headline = Component.translatable(town.queen() ? "message.kingdomsatwar.crown.title_queen" : "message.kingdomsatwar.crown.title")
                .withStyle(ChatFormatting.GOLD);
        Component sub = Component.translatable("message.kingdomsatwar.crown.subtitle", player.getDisplayName(), Component.literal(town.name()));
        for (ServerPlayer p : server.getPlayerList().getPlayers()) {
            p.connection.send(new ClientboundSetTitlesAnimationPacket(20, 100, 30));
            p.connection.send(new ClientboundSetTitleTextPacket(headline));
            p.connection.send(new ClientboundSetSubtitleTextPacket(sub));
            p.sendSystemMessage(Component.translatable(town.queen() ? "message.kingdomsatwar.crown.announce_queen" : "message.kingdomsatwar.crown.announce",
                    player.getDisplayName(), Component.literal(town.name())).withStyle(ChatFormatting.GOLD));
        }
        player.level().playSound(null, throne, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2f, 1.0f);
        player.level().sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.6, player.getZ(), 80, 0.6, 0.8, 0.6, 0.4);
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.crown.free").withStyle(ChatFormatting.YELLOW));
    }

    /** Die Bürger eilen in den Thronsaal (sie stellen sich vor dem Podest auf). */
    private static void gatherCitizens(ServerLevel level, Town town, BlockPos throne) {
        int i = 0;
        for (Town.Resident r : town.residents()) {
            if (level.getEntity(r.entity()) instanceof CitizenEntity citizen && citizen.distanceToSqr(Vec3.atCenterOf(throne)) < 96 * 96) {
                if (citizen.isSleeping()) {
                    citizen.stopSleeping();
                }
                Town.Building hall = null;
                for (Town.Building b : town.buildings()) {
                    if (b.type() == BuildingType.THRONE_HALL) {
                        hall = b;
                    }
                }
                if (hall == null) {
                    return;
                }
                int row = i / 4;
                int col = i % 4;
                BlockPos spot = hall.world(new BlockPos(col < 2 ? -2 - col : 2 + (col - 2), 1, 3 - row * 2));
                citizen.getNavigation().moveTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5, 1.0);
                i++;
            }
        }
    }

    private static void launchFirework(ServerLevel level, BlockPos throne, int t) {
        int[][] colors = {{0xF2C23A, 0xFFFFFF}, {0xC0392B, 0xF2C23A}, {0x2E5CB8, 0xFFFFFF}, {0x8E44AD, 0xF2C23A}};
        int[] pick = colors[(t / 12) % colors.length];
        ItemStack rocket = new ItemStack(Items.FIREWORK_ROCKET);
        FireworkExplosion explosion = new FireworkExplosion(t % 24 == 0 ? FireworkExplosion.Shape.LARGE_BALL : FireworkExplosion.Shape.STAR,
                IntList.of(pick[0]), IntList.of(pick[1]), true, true);
        rocket.set(DataComponents.FIREWORKS, new Fireworks(2, List.of(explosion)));
        double a = t * 0.9;
        double x = throne.getX() + 0.5 + Mth.cos((float) a) * 8;
        double z = throne.getZ() + 0.5 + Mth.sin((float) a) * 8;
        int y = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, (int) x, (int) z);
        level.addFreshEntity(new FireworkRocketEntity(level, x, y + 0.5, z, rocket));
    }
}
