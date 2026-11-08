package io.github.sirschrolli.kingdomsatwar.war;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastle;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePiece;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.BannerBlock;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.WallBannerBlock;
import net.minecraft.world.level.block.entity.BannerBlockEntity;
import net.minecraft.world.level.block.entity.BannerPatternLayers;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Der Krieg der Königreiche auf dem Server: Wer sich einer Stammburg nähert, erfährt, wessen Land er betritt. Gefallene
 * Krieger ersetzt das Reich nach und nach, solange niemand in der Nähe ist (der Herrscher kehrt nie zurück). Ist der
 * Herrscher gefallen und verteidigt niemand mehr das Reichsbanner, erobert der Spieler die Burg mit einem Griff an das
 * Banner (Rechtsklick auf das Tuch oder die Wand dahinter – oder er will es abreißen): Alle Banner tragen dann sein
 * Wappen, das alte Reichsbanner nimmt er als Trophäe mit, die übrigen Krieger ziehen ab, und jeden Tag liegt Tribut in der
 * Schatztruhe des Herrschers. Solange der Herrscher fehlt, schimmert das Reichsbanner für jeden, der den Thronsaal betritt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class RealmWar {
    /** Ab welcher Entfernung zur Burgmitte der Spieler erfährt, wessen Land er betritt. */
    private static final double DISCOVER = 150.0;
    /** Nachschub rückt nur nach, wenn kein Spieler näher an der Burgmitte ist. */
    private static final double UNSEEN = 72.0;
    /** Alle so viele Ticks Abwesenheit rückt ein Krieger nach (eine Minute). */
    public static final long REINFORCEMENT = 1200L;
    /** So weit um das Reichsbanner darf kein Verteidiger mehr stehen, damit es ergriffen werden kann. */
    public static final double GUARD_RADIUS = 16.0;
    private static final long DAY = 24000L;
    /** So viele Tage Tribut sammeln sich höchstens an. */
    public static final int MAX_TRIBUTE_DAYS = 7;

    private RealmWar() {}

    // =====================================================================================
    // Die Burg finden
    // =====================================================================================

    /** Das Bauteil der Stammburg eines Reichs (nur wenn ihr Mittelpunkt geladen ist). */
    public static @Nullable RealmCastlePiece castle(ServerLevel overworld, Realm realm) {
        ChunkPos chunk = RealmLocator.find(overworld, realm);
        if (chunk == null || !overworld.hasChunk(chunk.x(), chunk.z())) {
            return null;
        }
        Structure structure = overworld.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ModStructures.REALM_CASTLE_STRUCTURE);
        if (structure == null) {
            return null;
        }
        StructureStart start = overworld.getChunk(chunk.x(), chunk.z()).getStartForStructure(structure);
        if (start != null && start.isValid() && !start.getPieces().isEmpty() && start.getPieces().getFirst() instanceof RealmCastlePiece piece) {
            return piece;
        }
        return null;
    }

    /** Die Burg, in der dieser Punkt liegt (oder {@code null}). */
    public static @Nullable RealmCastlePiece castleAt(ServerLevel overworld, BlockPos pos) {
        int reach = RealmCastle.REACH + 24;
        for (Realm realm : Realm.values()) {
            ChunkPos chunk = RealmLocator.find(overworld, realm);
            if (chunk == null || Math.abs(chunk.getMiddleBlockX() - pos.getX()) > reach || Math.abs(chunk.getMiddleBlockZ() - pos.getZ()) > reach) {
                continue;
            }
            RealmCastlePiece piece = castle(overworld, realm);
            if (piece != null && piece.getBoundingBox().isInside(pos)) {
                return piece;
            }
        }
        return null;
    }

    // =====================================================================================
    // Jeden Tick
    // =====================================================================================

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level != level.getServer().overworld()) {
            return;
        }
        long time = level.getGameTime();
        if (time % 40L == 0L) {
            discover(level);
        }
        if (time % 10L == 7L) {
            beckon(level);
        }
        // reihum alle fünf Sekunden ein Reich: Lage merken, Nachschub, Tribut
        if (time % 100L == 50L) {
            Realm realm = Realm.values()[(int) (time / 100L % Realm.values().length)];
            RealmCastlePiece piece = castle(level, realm);
            if (piece != null) {
                remember(level, piece);
                reinforce(level, piece);
                payTribute(level, piece);
            }
        }
    }

    /**
     * Ist der Herrscher gefallen, schimmert das Reichsbanner hinter dem Thron für jeden, der in die Nähe kommt – hier wird
     * die Burg erobert.
     */
    private static void beckon(ServerLevel level) {
        if (level.players().isEmpty()) {
            return;
        }
        RealmState state = RealmState.of(level);
        for (Realm realm : Realm.values()) {
            ChunkPos chunk = state.isLordFallen(realm) && !state.isConquered(realm) ? RealmLocator.find(level, realm) : null;
            if (chunk == null || !anyoneNear(level, chunk.getMiddleBlockX(), chunk.getMiddleBlockZ(), RealmCastle.REACH + 32)) {
                continue;
            }
            RealmCastlePiece piece = castle(level, realm);
            if (piece == null) {
                continue;
            }
            BlockPos standard = piece.world(RealmCastle.STANDARD.above());
            if (level.getNearestPlayer(standard.getX() + 0.5, standard.getY(), standard.getZ() + 0.5, 28.0, p -> !p.isSpectator()) == null) {
                continue;
            }
            Direction facing = piece.rotation().rotate(Direction.SOUTH);
            for (int x = -1; x <= 1; x++) {
                Vec3 lane = Vec3.atCenterOf(piece.world(RealmCastle.STANDARD.offset(x, 1, 0)))
                        .add(facing.getStepX() * -0.3, -0.45, facing.getStepZ() * -0.3);
                level.sendParticles(ParticleTypes.END_ROD, lane.x, lane.y, lane.z, 1, 0.22, 0.5, 0.22, 0.008);
                level.sendParticles(ParticleTypes.WAX_ON, lane.x, lane.y, lane.z, 3, 0.25, 0.55, 0.25, 0.0);
            }
        }
    }

    /** Ist ein Spieler (kein Zuschauer) waagerecht nicht weiter als {@code range} von (x, z) entfernt? */
    private static boolean anyoneNear(ServerLevel level, int x, int z, double range) {
        for (ServerPlayer player : level.players()) {
            double dx = player.getX() - x;
            double dz = player.getZ() - z;
            if (!player.isSpectator() && dx * dx + dz * dz < range * range) {
                return true;
            }
        }
        return false;
    }

    /** Lage der Burg im Zustand vermerken (für Karten und Befehle). */
    private static void remember(ServerLevel level, RealmCastlePiece piece) {
        RealmState state = RealmState.of(level);
        if (!piece.center().equals(state.castle(piece.realm()))) {
            state.setCastle(piece.realm(), piece.center(), piece.rotation().ordinal());
            if (state.lastReinforcement(piece.realm()) == 0L) {
                state.setLastReinforcement(piece.realm(), level.getGameTime());
            }
            state.save(level);
        }
    }

    /** Wer zum ersten Mal in die Nähe einer Stammburg kommt, erfährt, wessen Land er betritt. */
    private static void discover(ServerLevel level) {
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            List<String> seen = player.getData(ModAttachments.REALMS_SEEN);
            for (Realm realm : Realm.values()) {
                if (seen.contains(realm.id())) {
                    continue;
                }
                ChunkPos chunk = RealmLocator.find(level, realm);
                if (chunk == null) {
                    continue;
                }
                double dx = player.getX() - chunk.getMiddleBlockX();
                double dz = player.getZ() - chunk.getMiddleBlockZ();
                if (dx * dx + dz * dz > DISCOVER * DISCOVER) {
                    continue;
                }
                List<String> updated = new ArrayList<>(seen);
                updated.add(realm.id());
                player.setData(ModAttachments.REALMS_SEEN, List.copyOf(updated));
                announceBorder(level, player, realm);
                break;
            }
        }
    }

    private static void announceBorder(ServerLevel level, ServerPlayer player, Realm realm) {
        RealmState state = RealmState.of(level);
        boolean conquered = state.isConquered(realm);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 80, 25));
        player.connection.send(new ClientboundSetTitleTextPacket(realm.displayName()));
        Component sub;
        if (conquered) {
            sub = Component.translatable("realm.kingdomsatwar.discover.conquered").withStyle(ChatFormatting.GOLD);
        } else if (state.isLordFallen(realm)) {
            sub = Component.translatable("realm.kingdomsatwar.discover.leaderless").withStyle(ChatFormatting.GRAY);
        } else {
            sub = Component.translatable("realm.kingdomsatwar.discover.ruler", realm.ruler()).withStyle(ChatFormatting.GRAY);
        }
        player.connection.send(new ClientboundSetSubtitleTextPacket(sub));
        if (!conquered) {
            player.sendSystemMessage(Component.translatable("realm.kingdomsatwar.discover.hint", realm.displayName(), realm.ruler())
                    .withStyle(ChatFormatting.GRAY));
        }
        level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.AMBIENT, 0.6f, 1.15f);
    }

    // =====================================================================================
    // Nachschub
    // =====================================================================================

    /**
     * Freie Posten neu besetzen: Für jede Minute, die niemand in der Nähe war, rückt ein Krieger nach – aber nie mehr, als
     * gefallen sind, und der Herrscher kehrt nicht zurück. Gibt zurück, wie viele nachgerückt sind.
     */
    public static int reinforce(ServerLevel level, RealmCastlePiece piece) {
        return reinforce(level, piece, level::isPositionEntityTicking);
    }

    /** Wie {@link #reinforce(ServerLevel, RealmCastlePiece)}; {@code canSpawn} sagt, auf welchen Posten jemand erscheinen kann. */
    public static int reinforce(ServerLevel level, RealmCastlePiece piece, java.util.function.Predicate<BlockPos> canSpawn) {
        Realm realm = piece.realm();
        RealmState state = RealmState.of(level);
        long now = level.getGameTime();
        if (state.isConquered(realm)) {
            return 0;
        }
        BlockPos center = piece.center();
        if (state.fallen(realm) <= 0 || level.getNearestPlayer(center.getX() + 0.5, center.getY(), center.getZ() + 0.5, UNSEEN,
                player -> !player.isSpectator()) != null) {
            // solange jemand in der Nähe ist (oder niemand fehlt), sammelt sich kein Nachschub an
            state.setLastReinforcement(realm, now);
            state.save(level);
            return 0;
        }
        long due = (now - state.lastReinforcement(realm)) / REINFORCEMENT;
        if (due <= 0) {
            return 0;
        }
        Set<BlockPos> occupied = new HashSet<>();
        for (RealmWarriorEntity warrior : level.getEntitiesOfClass(RealmWarriorEntity.class, AABB.of(piece.getBoundingBox()),
                w -> w.realm() == realm && w.isAlive())) {
            if (warrior.post() != null) {
                occupied.add(warrior.post());
            }
        }
        int added = 0;
        for (RealmCastle.Post post : RealmCastle.posts()) {
            if (added >= due || state.fallen(realm) <= 0) {
                break;
            }
            WarriorRole role = WarriorRole.byId(post.role());
            BlockPos pos = piece.world(new BlockPos(post.x(), post.y(), post.z()));
            if (role == WarriorRole.LORD || occupied.contains(pos) || !canSpawn.test(pos)) {
                continue;
            }
            RealmWarriorEntity warrior = ModEntities.REALM_WARRIORS.get(role).get().create(level, EntitySpawnReason.MOB_SUMMONED);
            if (warrior == null) {
                continue;
            }
            warrior.setRealm(realm);
            warrior.snapTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, level.getRandom().nextFloat() * 360.0f, 0.0f);
            warrior.setPost(pos, center);
            warrior.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), EntitySpawnReason.MOB_SUMMONED, null);
            warrior.setPersistenceRequired();
            level.addFreshEntity(warrior);
            state.replaced(realm);
            added++;
        }
        // was nicht nachrücken konnte (alle Posten besetzt), verfällt
        state.setLastReinforcement(realm, added >= due ? state.lastReinforcement(realm) + added * REINFORCEMENT : now);
        state.save(level);
        return added;
    }

    // =====================================================================================
    // Eroberung
    // =====================================================================================

    @SubscribeEvent
    static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof ServerPlayer player)
                || level != level.getServer().overworld() || player.isSpectator()) {
            return;
        }
        BlockPos pos = event.getPos();
        RealmCastlePiece piece = castleAt(level, pos);
        if (piece == null || !claims(level, piece, pos, false)) {
            return;
        }
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
        // Haupt- und Nebenhand lösen beide aus – gegriffen wird nur einmal
        if (event.getHand() == InteractionHand.MAIN_HAND) {
            seize(level, player, piece, pos, false);
        }
    }

    /** Wer das Reichsbanner abreißen will, greift danach – abreißen lässt es sich erst, wenn die Burg ihm gehört. */
    @SubscribeEvent
    static void onBreakBlock(BreakBlockEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level != level.getServer().overworld()
                || !(event.getPlayer() instanceof ServerPlayer player) || !(event.getState().getBlock() instanceof WallBannerBlock)) {
            return;
        }
        RealmCastlePiece piece = castleAt(level, event.getPos());
        if (piece != null && seize(level, player, piece, event.getPos(), true)) {
            event.setCanceled(true);
            event.setNotifyClient(true);
        }
    }

    /** Gehört dieses Banner zum Reichsbanner hinter dem Thron (eine seiner drei Bahnen)? */
    public static boolean isStandard(RealmCastlePiece piece, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            if (piece.world(RealmCastle.STANDARD.offset(x, 1, 0)).equals(pos)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gehört dieser Block zum Reichsbanner – eine seiner drei Bahnen, die Stelle darunter, über die das Tuch herabhängt,
     * oder die Wand dahinter? Ein Wandbanner lässt sich nur am oberen Rand anklicken; wer mitten auf das Tuch zielt, trifft
     * die Wand dahinter.
     */
    public static boolean inStandard(RealmCastlePiece piece, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -1; z <= 0; z++) {
                    if (piece.world(RealmCastle.STANDARD.offset(x, y, z)).equals(pos)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /**
     * Meint ein Klick auf diesen Block das Reichsbanner? Vor der Eroberung zählt der ganze Bereich des Banners (siehe
     * {@link #inStandard}), danach – wie beim Abreißen – nur noch die Banner selbst.
     */
    public static boolean claims(ServerLevel level, RealmCastlePiece piece, BlockPos pos, boolean breaking) {
        boolean banner = level.getBlockState(pos).getBlock() instanceof WallBannerBlock && isStandard(piece, pos);
        if (breaking || banner) {
            return banner;
        }
        return !RealmState.of(level).isConquered(piece.realm()) && inStandard(piece, pos);
    }

    /**
     * Der Spieler greift nach dem Reichsbanner – per Rechtsklick oder indem er es abreißen will. Ist die Burg reif, ist sie
     * damit erobert; sonst bleibt das Banner hängen (sonst ließe sie sich nie mehr erobern), und er erfährt, was noch im
     * Weg steht. Dem Eroberer gehört es danach – er darf es abnehmen.
     *
     * @return ob der Klick damit erledigt ist (dann ist seine gewöhnliche Wirkung aufgehoben)
     */
    public static boolean seize(ServerLevel level, ServerPlayer player, RealmCastlePiece piece, BlockPos pos, boolean breaking) {
        if (!claims(level, piece, pos, breaking)) {
            return false;
        }
        RealmState state = RealmState.of(level);
        if (breaking && state.isConquered(piece.realm()) && player.getUUID().equals(state.conqueror(piece.realm()))) {
            return false;
        }
        tryConquer(level, player, piece);
        return true;
    }

    /** Was einer Eroberung noch im Weg steht (oder {@code null}, wenn die Burg jetzt erobert werden kann). */
    public static @Nullable Component obstacle(ServerLevel level, UUID player, RealmCastlePiece piece) {
        Realm realm = piece.realm();
        RealmState state = RealmState.of(level);
        if (state.isConquered(realm)) {
            return Component.translatable(player.equals(state.conqueror(realm)) ? "realm.kingdomsatwar.conquer.yours"
                    : "realm.kingdomsatwar.conquer.taken", realm.shortName());
        }
        if (!state.isLordFallen(realm)) {
            if (lord(level, piece) != null || !level.hasChunkAt(piece.world(RealmCastle.THRONE))) {
                return Component.translatable("realm.kingdomsatwar.conquer.lord_alive", realm.ruler()).withStyle(ChatFormatting.RED);
            }
            // der Herrscher ist verschwunden, ohne zu fallen (etwa auf friedlicher Schwierigkeit) – dann gilt er als gefallen
            state.lordFallen(realm);
            state.save(level);
            KingdomsAtWar.LOGGER.info("{}: Herrscher nicht mehr auffindbar – das Reich gilt als herrenlos", realm.id());
        }
        int defenders = defenders(level, piece).size();
        if (defenders > 0) {
            return Component.translatable("realm.kingdomsatwar.conquer.defended", realm.shortName(), defenders).withStyle(ChatFormatting.RED);
        }
        return null;
    }

    /** Die Krieger des Reichs, die das Reichsbanner noch verteidigen (im Thronsaal und rings um ihn). */
    public static List<RealmWarriorEntity> defenders(ServerLevel level, RealmCastlePiece piece) {
        Realm realm = piece.realm();
        AABB guard = new AABB(piece.world(RealmCastle.STANDARD)).inflate(GUARD_RADIUS, 6.0, GUARD_RADIUS);
        return level.getEntitiesOfClass(RealmWarriorEntity.class, guard, w -> w.realm() == realm && w.isAlive());
    }

    /** Der Herrscher, wenn er noch lebt und in oder bei seiner Burg ist. */
    private static @Nullable RealmWarriorEntity lord(ServerLevel level, RealmCastlePiece piece) {
        Realm realm = piece.realm();
        List<RealmWarriorEntity> lords = level.getEntitiesOfClass(RealmWarriorEntity.class, AABB.of(piece.getBoundingBox()).inflate(64.0),
                w -> w.realm() == realm && w.role() == WarriorRole.LORD && w.isAlive());
        return lords.isEmpty() ? null : lords.getFirst();
    }

    /**
     * Die Burg erobern, wenn der Herrscher gefallen ist und niemand mehr das Reichsbanner verteidigt. Steht noch etwas im
     * Weg, leuchten der Herrscher bzw. die letzten Verteidiger zehn Sekunden lang auf (auch durch Wände), damit der Spieler
     * sie findet.
     */
    public static boolean tryConquer(ServerLevel level, ServerPlayer player, RealmCastlePiece piece) {
        Component obstacle = obstacle(level, player.getUUID(), piece);
        if (obstacle != null) {
            player.sendOverlayMessage(obstacle);
            RealmState state = RealmState.of(level);
            if (!state.isConquered(piece.realm())) {
                List<RealmWarriorEntity> reveal = new ArrayList<>(defenders(level, piece));
                RealmWarriorEntity lord = state.isLordFallen(piece.realm()) ? null : lord(level, piece);
                if (lord != null && !reveal.contains(lord)) {
                    reveal.add(lord);
                }
                for (RealmWarriorEntity warrior : reveal) {
                    warrior.addEffect(new MobEffectInstance(MobEffects.GLOWING, 200, 0, false, false));
                }
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BASS.value(), SoundSource.PLAYERS, 0.8f, 0.6f);
            }
            return false;
        }
        conquer(level, player, piece);
        return true;
    }

    /** Die Burg gehört dem Spieler: Banner in seinem Wappen, die übrigen Krieger ziehen ab, Tribut beginnt. */
    public static void conquer(ServerLevel level, ServerPlayer player, RealmCastlePiece piece) {
        Realm realm = piece.realm();
        RealmState state = RealmState.of(level);
        state.conquer(realm, player.getUUID(), level.getGameTime());
        state.save(level);
        // das alte Reichsbanner nimmt der Eroberer als Trophäe mit; hinter dem Thron hängt fortan sein eigenes
        ItemStack trophy = realm.banner(level.registryAccess());
        if (!player.getInventory().add(trophy)) {
            player.spawnAtLocation(level, trophy);
        }
        restoreStandard(level, piece);
        io.github.sirschrolli.kingdomsatwar.hero.HeroLevel.award(player, io.github.sirschrolli.kingdomsatwar.hero.HeroLevel.CONQUEST);
        int banners = repaintBanners(level, piece.getBoundingBox());
        // wer noch lebt, legt die Waffen nieder und zieht ab
        for (RealmWarriorEntity warrior : level.getEntitiesOfClass(RealmWarriorEntity.class, AABB.of(piece.getBoundingBox()).inflate(32.0),
                w -> w.realm() == realm)) {
            level.sendParticles(ParticleTypes.POOF, warrior.getX(), warrior.getY() + 1.0, warrior.getZ(), 12, 0.3, 0.6, 0.3, 0.02);
            warrior.discard();
        }
        BlockPos standard = piece.world(RealmCastle.STANDARD);
        level.playSound(null, standard, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.2f, 1.0f);
        level.playSound(null, standard, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 2.0f, 0.9f);
        level.sendParticles(ParticleTypes.TOTEM_OF_UNDYING, player.getX(), player.getY() + 1.6, player.getZ(), 60, 0.6, 0.8, 0.6, 0.4);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(15, 90, 25));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("realm.kingdomsatwar.conquer.title")
                .withStyle(ChatFormatting.GOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("realm.kingdomsatwar.conquer.subtitle",
                realm.displayName())));
        Component announce = Component.translatable("realm.kingdomsatwar.conquer.announce", player.getDisplayName(), realm.shortName())
                .withStyle(ChatFormatting.GOLD);
        for (ServerPlayer other : level.getServer().getPlayerList().getPlayers()) {
            other.sendSystemMessage(announce);
        }
        player.sendSystemMessage(Component.translatable("realm.kingdomsatwar.conquer.trophy", realm.shortName()).withStyle(ChatFormatting.GOLD));
        player.sendSystemMessage(Component.translatable("realm.kingdomsatwar.conquer.tribute").withStyle(ChatFormatting.YELLOW));
        KingdomsAtWar.LOGGER.info("{} hat {} erobert ({} Banner im neuen Wappen)", player.getName().getString(), realm.id(), banners);
    }

    /** Fehlt eine Bahn des Reichsbanners (abgerissen, gesprengt), hängt sie der Eroberer neu auf. */
    private static void restoreStandard(ServerLevel level, RealmCastlePiece piece) {
        Direction facing = piece.rotation().rotate(Direction.SOUTH);
        for (int x = -1; x <= 1; x++) {
            BlockPos pos = piece.world(RealmCastle.STANDARD.offset(x, 1, 0));
            BlockPos wall = pos.relative(facing.getOpposite());
            BlockState here = level.getBlockState(pos);
            if (here.getBlock() instanceof WallBannerBlock || !here.canBeReplaced() || !level.getBlockState(wall).isFaceSturdy(level, wall, facing)) {
                continue;
            }
            level.setBlock(pos, Blocks.WALL_BANNER.pick(DyeColor.BLUE).defaultBlockState().setValue(WallBannerBlock.FACING, facing), 3);
        }
    }

    /** Das Wappen des Spielers: blau mit goldenem Kreuz und goldenem Rand – wie die Schilde seines Heeres. */
    public static ItemStack kingdomBanner(ServerLevel level) {
        var patterns = level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN);
        ItemStack stack = new ItemStack(Items.BANNER.pick(DyeColor.BLUE));
        stack.set(DataComponents.BANNER_PATTERNS, new BannerPatternLayers.Builder()
                .add(patterns.getOrThrow(BannerPatterns.STRAIGHT_CROSS), DyeColor.YELLOW)
                .add(patterns.getOrThrow(BannerPatterns.BORDER), DyeColor.YELLOW)
                .build());
        return stack;
    }

    /** Alle Banner in der Burg tragen nun das Wappen des Spielers. Gibt zurück, wie viele es sind. */
    public static int repaintBanners(ServerLevel level, BoundingBox box) {
        ItemStack heraldry = kingdomBanner(level);
        List<BlockPos> found = new ArrayList<>();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                for (BlockEntity entity : level.getChunk(cx, cz).getBlockEntities().values()) {
                    if (entity instanceof BannerBlockEntity && box.isInside(entity.getBlockPos())) {
                        found.add(entity.getBlockPos());
                    }
                }
            }
        }
        for (BlockPos pos : found) {
            BlockState old = level.getBlockState(pos);
            BlockState fresh;
            if (old.getBlock() instanceof WallBannerBlock) {
                fresh = Blocks.WALL_BANNER.pick(DyeColor.BLUE).defaultBlockState().setValue(WallBannerBlock.FACING, old.getValue(WallBannerBlock.FACING));
            } else if (old.getBlock() instanceof BannerBlock) {
                fresh = Blocks.BANNER.pick(DyeColor.BLUE).defaultBlockState().setValue(BannerBlock.ROTATION, old.getValue(BannerBlock.ROTATION));
            } else {
                continue;
            }
            level.setBlock(pos, fresh, 2);
            if (level.getBlockEntity(pos) instanceof BannerBlockEntity banner) {
                banner.applyComponentsFromItemStack(heraldry);
                banner.setChanged();
                level.sendBlockUpdated(pos, fresh, fresh, 2);
            }
            level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4, 0.3, 0.6, 0.3, 0.0);
        }
        return found.size();
    }

    // =====================================================================================
    // Tribut
    // =====================================================================================

    /**
     * Jeden Tag Tribut in die Schatztruhe des Herrschers: Bronzemünzen und Waren des Landes. Höchstens eine
     * Woche sammelt sich an. Gibt die Zahl der bezahlten Tage zurück.
     */
    public static int payTribute(ServerLevel level, RealmCastlePiece piece) {
        Realm realm = piece.realm();
        RealmState state = RealmState.of(level);
        if (!state.isConquered(realm)) {
            return 0;
        }
        long days = (level.getGameTime() - state.lastTribute(realm)) / DAY;
        if (days <= 0) {
            return 0;
        }
        BlockPos chest = piece.world(RealmCastle.TREASURY);
        if (!level.isLoaded(chest) || !(level.getBlockEntity(chest) instanceof Container container)) {
            return 0;
        }
        int pay = (int) Math.min(days, MAX_TRIBUTE_DAYS);
        for (int day = 0; day < pay; day++) {
            // nur Bronze: Silber und Gold entstehen allein durch Zusammenlegen und im Handel
            insert(container, new ItemStack(ModItems.BRONZE_COIN.get(), 8 + level.getRandom().nextInt(9)));
            insert(container, switch (level.getRandom().nextInt(6)) {
                case 0 -> new ItemStack(Items.WHEAT, 8 + level.getRandom().nextInt(9));
                case 1 -> new ItemStack(Items.IRON_INGOT, 2 + level.getRandom().nextInt(3));
                case 2 -> new ItemStack(ModItems.BREAD_ROLL.get(), 4 + level.getRandom().nextInt(5));
                case 3 -> new ItemStack(Items.LEATHER, 2 + level.getRandom().nextInt(4));
                case 4 -> new ItemStack(Items.EMERALD, 1 + level.getRandom().nextInt(2));
                default -> new ItemStack(Items.GOLD_NUGGET, 4 + level.getRandom().nextInt(6));
            });
        }
        state.setLastTribute(realm, state.lastTribute(realm) + days * DAY);
        state.save(level);
        container.setChanged();
        return pay;
    }

    private static void insert(Container container, ItemStack stack) {
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, stack) && slot.getCount() < slot.getMaxStackSize()) {
                int move = Math.min(stack.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(move);
                stack.shrink(move);
            }
        }
        for (int i = 0; i < container.getContainerSize() && !stack.isEmpty(); i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, stack.copy());
                stack.setCount(0);
            }
        }
    }

    // =====================================================================================
    // Befehl
    // =====================================================================================

    /** {@code /kaw realms} (Operatoren): Lage und Zustand der vier Reiche; {@code /kaw realms reset} setzt alle zurück. */
    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("realms").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> list(context.getSource()))
                .then(Commands.literal("reset").executes(context -> {
                    ServerLevel overworld = context.getSource().getServer().overworld();
                    RealmState state = RealmState.of(overworld);
                    for (Realm realm : Realm.values()) {
                        state.reset(realm);
                    }
                    state.save(overworld);
                    context.getSource().sendSuccess(() -> Component.translatable("command.kingdomsatwar.realms.reset"), true);
                    return 1;
                }))
                .then(Commands.literal("check").executes(context -> check(context.getSource())));
    }

    /**
     * {@code /kaw realms check}: lädt den Mittel-Chunk jeder Stammburg (erzeugt ihn, falls nötig) und meldet, ob dort
     * wirklich eine Burg steht – mit Lage, Höhe und Drehung. Zum Prüfen der Weltgenerierung.
     */
    private static int check(CommandSourceStack source) {
        ServerLevel overworld = source.getServer().overworld();
        Structure structure = overworld.registryAccess().lookupOrThrow(Registries.STRUCTURE).getValue(ModStructures.REALM_CASTLE_STRUCTURE);
        int found = 0;
        for (Realm realm : Realm.values()) {
            ChunkPos chunk = RealmLocator.find(overworld, realm);
            if (chunk == null || structure == null) {
                source.sendFailure(Component.translatable("command.kingdomsatwar.realms.check.none", realm.shortName()));
                continue;
            }
            StructureStart start = overworld.getChunk(chunk.x(), chunk.z()).getStartForStructure(structure);
            if (start != null && start.isValid() && start.getPieces().getFirst() instanceof RealmCastlePiece piece) {
                found++;
                BlockPos center = piece.center();
                source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.realms.check.found", realm.shortName(), center.getX(),
                        center.getY(), center.getZ(), piece.rotation().name().toLowerCase(java.util.Locale.ROOT)), false);
            } else {
                source.sendFailure(Component.translatable("command.kingdomsatwar.realms.check.missing", realm.shortName(), chunk.getMiddleBlockX(),
                        chunk.getMiddleBlockZ()));
            }
        }
        return found;
    }

    private static int list(CommandSourceStack source) {
        ServerLevel overworld = source.getServer().overworld();
        RealmState state = RealmState.of(overworld);
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.realms.header").withStyle(ChatFormatting.GOLD), false);
        for (Realm realm : Realm.values()) {
            BlockPos center = state.castle(realm) != null ? state.castle(realm) : RealmLocator.approximateCenter(overworld, realm);
            if (center == null) {
                continue;
            }
            String tp = "/tp @s " + center.getX() + " " + (center.getY() + 30) + " " + (center.getZ() + 60);
            Component where = Component.literal(center.getX() + " " + center.getZ()).withStyle(style -> style.withUnderlined(true)
                    .withClickEvent(new ClickEvent.SuggestCommand(tp)));
            String status = state.isConquered(realm) ? "conquered" : state.isLordFallen(realm) ? "leaderless" : "ruled";
            Component line = Component.translatable("command.kingdomsatwar.realms.entry", realm.displayName(), where,
                    Component.translatable("command.kingdomsatwar.realms." + status, realm.ruler()), state.fallen(realm));
            source.sendSuccess(() -> line, false);
        }
        return 1;
    }
}
