package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights.Fighter;
import io.github.sirschrolli.kingdomsatwar.arena.BossFights.Fight;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Die Kämpfe in den Kampfdimensionen der großen Bosse (wie die Arkane Sphäre der Erzmagier): Betritt jemand den Hort,
 * zieht der Boss ihn, alle Mitstreiter am Hort und ihre Soldaten in seine Welt, wo ein Abbild seines Horts schwebt; wer
 * später an den Hort kommt, folgt ihnen. Dort lässt sich nichts bauen und nichts abbauen, und wer stirbt, verliert alles,
 * was er bei sich trägt (ein Unsterblicher – ab Heldenstufe 200 – behält es); wer vom Rand stürzt, wird verletzt zurückgeworfen.
 *
 * <p>Fällt der Boss, kehren alle mit der Beute an den Hort zurück, und der Hort gilt als besiegt (mit allem, was dazugehört,
 * etwa dem Riss hinter dem Thron). Steht niemand mehr, der ihm die Stirn bietet, ist der Kampf verloren: Der Boss vergeht,
 * der Hort ruht wieder, und wer wiederkommt, trifft auf einen Boss bei voller Kraft und in seiner ursprünglichen Gestalt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class BossArenas {
    /** So nah an einem mitgezogenen Spieler müssen seine Soldaten sein. */
    public static final double TROOP_RADIUS = 24.0;
    /** So viel Schaden nimmt, wen die Welt zurückwirft. */
    public static final float FALL_DAMAGE = 6.0f;
    /** So lange (Ticks) liegt die Beute nach dem Sieg, bevor alle zurückkehren. */
    public static final int RETURN_DELAY = 100;
    /** Fehlt der Boss so lange (Sekunden) und erwacht kein neuer, endet der Kampf. */
    public static final int BOSSLESS_LIMIT = 120;

    /** Horte, deren Boss beim nächsten Server-Tick seine Gegner in seine Welt zieht. */
    private static final Set<GlobalPos> PENDING = new LinkedHashSet<>();

    private BossArenas() {}

    // =====================================================================================
    // Hineinziehen
    // =====================================================================================

    /**
     * Ein Hort erwacht: Statt hier zu kämpfen, zieht sein Boss die Gegner beim nächsten Server-Tick in seine Kampfdimension.
     * Gibt {@code true} zurück, wenn das geschieht (oder dort schon gekämpft wird) – dann erwacht der Hort selbst nicht;
     * {@code false}, wenn hier gekämpft wird (ein Abbild in der Kampfdimension, Tests, keine Kampfdimension).
     */
    public static boolean requestPull(ServerLevel level, BlockPos lairPos) {
        if (!(level.getBlockEntity(lairPos) instanceof BossLair lair) || lair.isArenaReplica() || BossArenaKind.isArena(level)) {
            return false;
        }
        // auf dem GameTest-Server prüfen die Tests den Kampf am Hort und die Kampfdimension getrennt (über pull())
        if (level.getServer() instanceof net.minecraft.gametest.framework.GameTestServer) {
            return false;
        }
        if (level.getServer().getLevel(lair.arenaKind().level()) == null) {
            return false;
        }
        if (!busy(level, lairPos)) {
            PENDING.add(GlobalPos.of(level.dimension(), lairPos.immutable()));
        }
        return true;
    }

    /** Wird um diesen Hort gerade in seiner Kampfdimension gekämpft (oder zieht er gleich hinein)? */
    public static boolean busy(ServerLevel level, BlockPos lairPos) {
        return PENDING.contains(GlobalPos.of(level.dimension(), lairPos)) || BossFights.of(level.getServer()).byLair(level.dimension(), lairPos) != null;
    }

    /** Zieht die Gegner am Hort in eine frisch gebaute Arena seiner Kampfdimension. */
    public static @Nullable Fight pull(ServerLevel level, BlockPos lairPos) {
        if (!(level.getBlockEntity(lairPos) instanceof BossLair lair)) {
            return null;
        }
        BossArenaKind kind = lair.arenaKind();
        ServerLevel arena = level.getServer().getLevel(kind.level());
        if (arena == null) {
            return null;
        }
        int slot = BossFights.of(level.getServer()).freeSlot(kind);
        return pull(level, lairPos, arena, slot, BossArenaKind.center(slot, kind.arenaY(lair.arenaFocus().getY())));
    }

    /** Wie {@link #pull(ServerLevel, BlockPos)}, mit Arena-Welt, Platz und Mitte (für Tests auch in der Oberwelt). */
    public static @Nullable Fight pull(ServerLevel level, BlockPos lairPos, ServerLevel arena, int slot, BlockPos center) {
        MinecraftServer server = level.getServer();
        if (!(level.getBlockEntity(lairPos) instanceof BossLair lair) || lair.isArenaReplica()) {
            return null;
        }
        BossArenaKind kind = lair.arenaKind();
        BossFights fights = BossFights.of(server);
        if (fights.byLair(level.dimension(), lairPos) != null) {
            return null;
        }
        List<ServerPlayer> players = challengers(level, lair);
        if (players.isEmpty()) {
            return null;
        }
        List<SoldierEntity> troops = troops(level, players);
        List<long[]> chunks = chunks(center, kind.reach());
        for (long[] chunk : chunks) {
            arena.getChunk((int) chunk[0], (int) chunk[1]);
            arena.setChunkForced((int) chunk[0], (int) chunk[1], true);
        }
        // was von einem früheren Kampf an diesem Platz übrig ist, verschwindet
        for (Entity entity : arena.getEntitiesOfClass(Entity.class, area(center, kind), e -> !(e instanceof Player))) {
            entity.discard();
        }
        BlockPos trigger = BossArenaBuilder.build(level, lair, lairPos, arena, center);
        if (trigger == null || !(arena.getBlockEntity(trigger) instanceof BossLair replica)) {
            KingdomsAtWar.LOGGER.error("Kampfdimension {}: das Abbild des Horts bei {} fehlt", kind.id(), trigger);
            for (long[] chunk : chunks) {
                arena.setChunkForced((int) chunk[0], (int) chunk[1], false);
            }
            return null;
        }
        replica.arenaPrepareReplica(arena);
        lair.arenaAway(level);

        List<Fighter> fighters = new ArrayList<>();
        List<Vec3> spots = BossArenaBuilder.spawnPoints(arena, center, kind, players.size() + troops.size());
        Vec3 look = Vec3.atBottomCenterOf(center);
        int i = 0;
        Component pulled = Component.translatable("message.kingdomsatwar.boss_arena." + kind.id() + ".pulled").withStyle(ChatFormatting.LIGHT_PURPLE);
        for (ServerPlayer player : players) {
            fighters.add(new Fighter(player.getUUID(), true, level.dimension(), player.position(), player.getYRot()));
            Vec3 spot = spots.get(i++);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
            player.teleport(new TeleportTransition(arena, spot, Vec3.ZERO, yawTowards(spot, look), 0.0f, TeleportTransition.DO_NOTHING));
            player.sendSystemMessage(pulled);
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.arena.rules").withStyle(ChatFormatting.GRAY));
        }
        for (SoldierEntity soldier : troops) {
            Vec3 from = soldier.position();
            float yaw = soldier.getYRot();
            Vec3 spot = spots.get(i++);
            Entity moved = soldier.teleport(new TeleportTransition(arena, spot, Vec3.ZERO, yawTowards(spot, look), 0.0f,
                    TeleportTransition.DO_NOTHING));
            if (moved != null) {
                fighters.add(new Fighter(moved.getUUID(), false, level.dimension(), from, yaw));
            }
        }
        long now = server.overworld().getGameTime();
        Fight fight = new Fight(kind, arena.dimension(), slot, center, trigger, level.dimension(), lairPos, fighters, now, false, 0L, 0);
        fights.add(fight);
        fights.save(server);
        LivingEntity boss = replica.arenaAwaken(arena);
        if (boss == null) {
            KingdomsAtWar.LOGGER.warn("Kampfdimension {}: der Boss erwacht nicht sofort (er erwacht, sobald jemand an den Hort tritt)", kind.id());
        }
        arena.playSound(null, center, SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 0.6f, 1.2f);
        KingdomsAtWar.LOGGER.info("Kampfdimension {}: {} Spieler und {} Soldaten (Arena {})", kind.id(), players.size(), troops.size(), slot);
        return fight;
    }

    /** Wer am Hort steht und mitgezogen wird (lebend, weder im Kreativ- noch im Zuschauermodus). */
    public static List<ServerPlayer> challengers(ServerLevel level, BossLair lair) {
        BossArenaKind kind = lair.arenaKind();
        BlockPos focus = lair.arenaFocus();
        List<ServerPlayer> out = new ArrayList<>();
        for (ServerPlayer player : level.players()) {
            if (!player.isAlive() || player.isSpectator() || player.isCreative()) {
                continue;
            }
            double dx = player.getX() - (focus.getX() + 0.5);
            double dz = player.getZ() - (focus.getZ() + 0.5);
            if (dx * dx + dz * dz <= kind.pullRadius() * kind.pullRadius() && Math.abs(player.getY() - focus.getY()) <= kind.pullHeight()) {
                out.add(player);
            }
        }
        return out;
    }

    private static List<SoldierEntity> troops(ServerLevel level, List<ServerPlayer> players) {
        Set<UUID> owners = new HashSet<>();
        players.forEach(p -> owners.add(p.getUUID()));
        List<SoldierEntity> troops = new ArrayList<>();
        for (ServerPlayer player : players) {
            for (SoldierEntity soldier : level.getEntitiesOfClass(SoldierEntity.class, player.getBoundingBox().inflate(TROOP_RADIUS),
                    s -> s.isAlive() && s.getOwnerId() != null && owners.contains(s.getOwnerId()))) {
                if (!troops.contains(soldier)) {
                    troops.add(soldier);
                }
            }
        }
        return troops;
    }

    /** Wer später an den Hort kommt, während drüben gekämpft wird, folgt den anderen in die Kampfdimension. */
    public static boolean join(ServerLevel level, BlockPos lairPos, ServerPlayer player) {
        MinecraftServer server = level.getServer();
        BossFights fights = BossFights.of(server);
        Fight fight = fights.byLair(level.dimension(), lairPos);
        if (fight == null || fight.won() || fights.byFighter(player.getUUID()) != null && fight.fighter(player.getUUID()) == null) {
            return false;
        }
        ServerLevel arena = server.getLevel(fight.arenaLevel());
        if (arena == null) {
            return false;
        }
        Vec3 spot = BossArenaBuilder.spawnPoints(arena, fight.center(), fight.kind(), 1).getFirst();
        fight.add(new Fighter(player.getUUID(), true, level.dimension(), player.position(), player.getYRot()));
        fights.save(server);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
        player.teleport(new TeleportTransition(arena, spot, Vec3.ZERO, yawTowards(spot, Vec3.atBottomCenterOf(fight.center())), 0.0f,
                TeleportTransition.DO_NOTHING));
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.boss_arena." + fight.kind().id() + ".pulled")
                .withStyle(ChatFormatting.LIGHT_PURPLE));
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.arena.rules").withStyle(ChatFormatting.GRAY));
        return true;
    }

    private static float yawTowards(Vec3 from, Vec3 to) {
        return (float) Math.toDegrees(Math.atan2(to.z - from.z, to.x - from.x)) - 90.0f;
    }

    /** Die Chunks einer Arena (zum Laden und Festhalten). */
    public static List<long[]> chunks(BlockPos center, int reach) {
        List<long[]> out = new ArrayList<>();
        for (int cx = (center.getX() - reach) >> 4; cx <= (center.getX() + reach) >> 4; cx++) {
            for (int cz = (center.getZ() - reach) >> 4; cz <= (center.getZ() + reach) >> 4; cz++) {
                out.add(new long[] {cx, cz});
            }
        }
        return out;
    }

    private static AABB area(BlockPos center, BossArenaKind kind) {
        return new AABB(center).inflate(kind.reach(), 200.0, kind.reach());
    }

    // =====================================================================================
    // Jeden Tick
    // =====================================================================================

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!PENDING.isEmpty()) {
            List<GlobalPos> pending = new ArrayList<>(PENDING);
            PENDING.clear();
            for (GlobalPos at : pending) {
                ServerLevel level = server.getLevel(at.dimension());
                if (level == null || !(level.getBlockEntity(at.pos()) instanceof BossLair lair)) {
                    continue;
                }
                if (pull(level, at.pos()) == null) {
                    // niemand mehr am Hort (oder es ging nicht): der Hort ruht wieder
                    lair.arenaReset(level);
                }
            }
        }
        BossFights fights = BossFights.of(server);
        if (fights.all().isEmpty()) {
            return;
        }
        boolean changed = false;
        for (Fight fight : fights.all()) {
            ServerLevel arena = server.getLevel(fight.arenaLevel());
            if (arena != null) {
                changed |= tick(server, arena, fights, fight);
            }
        }
        if (changed) {
            fights.save(server);
        }
    }

    /** Ein Kampf: Abgestürzte zurückwerfen, Sieg oder Niederlage erkennen. Gibt zurück, ob sich etwas geändert hat. */
    public static boolean tick(MinecraftServer server, ServerLevel arena, BossFights fights, Fight fight) {
        BlockPos center = fight.center();
        BossArenaKind kind = fight.kind();
        long now = server.overworld().getGameTime();
        for (Fighter fighter : fight.fighters()) {
            Entity entity = fighter.player() ? server.getPlayerList().getPlayer(fighter.id()) : arena.getEntity(fighter.id());
            if (entity != null && inArena(entity, arena, fight) && entity.isAlive() && entity.getY() < center.getY() - kind.fallDepth()) {
                rescue(arena, entity, fight);
            }
        }
        if (fight.won()) {
            if (lingers(arena, fight)) {
                // er vergeht noch (und lässt seine Beute womöglich erst am Ende fallen): erst danach beginnt die Wartezeit
                fight.holdWin(now);
                return false;
            }
            if (now - fight.wonAt() < RETURN_DELAY) {
                return false;
            }
            finish(server, arena, fights, fight, true);
            return true;
        }
        BossLair replica = arena.isLoaded(fight.trigger()) && arena.getBlockEntity(fight.trigger()) instanceof BossLair lair ? lair : null;
        if (replica != null && replica.arenaDefeated()) {
            fight.win(now);
            return true;
        }
        if (!anyoneLeft(server, arena, fight)) {
            finish(server, arena, fights, fight, false);
            return true;
        }
        if (now % 20 == 0 && replica != null && entitiesLoaded(arena, fight)) {
            if (replica.arenaBoss(arena) != null) {
                if (fight.bossless() != 0) {
                    fight.setBossless(0);
                    return true;
                }
            } else {
                fight.setBossless(fight.bossless() + 1);
                if (fight.bossless() > BOSSLESS_LIMIT) {
                    // der Boss ist fort und kehrt nicht wieder: der Kampf verebbt
                    finish(server, arena, fights, fight, false);
                }
                return true;
            }
        }
        return false;
    }

    /** Ist der Boss dieses Kampfs noch da (etwa mitten in seinem Tod)? */
    static boolean lingers(ServerLevel arena, Fight fight) {
        return !arena.getEntitiesOfClass(LivingEntity.class, area(fight.center(), fight.kind()),
                e -> e instanceof BossLair.Bound bound && fight.trigger().equals(bound.lairPos()) && !e.isRemoved()).isEmpty();
    }

    /** Sind die Wesen in allen Chunks der Arena geladen (sonst lässt sich nicht sagen, wer noch da ist)? */
    public static boolean entitiesLoaded(ServerLevel arena, Fight fight) {
        for (long[] chunk : chunks(fight.center(), Math.min(fight.kind().reach(), 48))) {
            if (!arena.areEntitiesLoaded(ChunkPos.pack((int) chunk[0], (int) chunk[1]))) {
                return false;
            }
        }
        return true;
    }

    /** Steht noch ein mitgezogener Spieler lebend in der Arena? */
    public static boolean anyoneLeft(MinecraftServer server, ServerLevel arena, Fight fight) {
        for (Fighter fighter : fight.fighters()) {
            if (fighter.player()) {
                ServerPlayer player = server.getPlayerList().getPlayer(fighter.id());
                if (player != null && inArena(player, arena, fight) && player.isAlive() && !player.isSpectator()) {
                    return true;
                }
            }
        }
        return false;
    }

    /** Ist das Wesen in der Arena dieses Kampfs? */
    public static boolean inArena(Entity entity, ServerLevel arena, Fight fight) {
        Vec3 center = Vec3.atBottomCenterOf(fight.center());
        return entity.level() == arena && entity.position().subtract(center).horizontalDistance() < fight.kind().reach() + 24
                && Math.abs(entity.getY() - center.y) < 160.0;
    }

    /** Wer in die Tiefe stürzt, landet verletzt wieder am Hort. */
    static void rescue(ServerLevel arena, Entity entity, Fight fight) {
        Vec3 spot = BossArenaBuilder.spawnPoints(arena, fight.center(), fight.kind(), 1).getFirst();
        entity.teleport(new TeleportTransition(arena, spot, Vec3.ZERO, entity.getYRot(), 0.0f, TeleportTransition.DO_NOTHING));
        entity.resetFallDistance();
        entity.setDeltaMovement(Vec3.ZERO);
        if (entity instanceof LivingEntity living) {
            living.hurtServer(arena, arena.damageSources().magic(), FALL_DAMAGE);
        }
        arena.sendParticles(ParticleTypes.REVERSE_PORTAL, spot.x, spot.y + 1.0, spot.z, 30, 0.4, 0.8, 0.4, 0.05);
        arena.playSound(null, BlockPos.containing(spot), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.7f);
        if (entity instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.boss_arena.rescued").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    /**
     * Das Ende eines Kampfs: Sieg (alle mit der Beute an den Hort zurück, der Hort gilt als besiegt) oder Niederlage (der
     * Boss vergeht, der Hort ruht wieder – der nächste Boss erwacht unversehrt und in seiner ursprünglichen Gestalt).
     */
    public static void finish(MinecraftServer server, ServerLevel arena, BossFights fights, Fight fight, boolean won) {
        ServerLevel home = server.getLevel(fight.lairLevel());
        if (home == null) {
            home = server.overworld();
        }
        // der Hort liegt womöglich in einem entladenen Chunk (alle waren ja fort)
        home.getChunk(fight.lair().getX() >> 4, fight.lair().getZ() >> 4);
        BossLair lair = home.getBlockEntity(fight.lair()) instanceof BossLair l && !l.isArenaReplica() ? l : null;
        BossArenaKind kind = fight.kind();
        AABB area = area(fight.center(), kind);
        if (won) {
            Vec3 loot = Vec3.atBottomCenterOf(lair != null ? lair.arenaLootSpot() : fight.lair().above());
            for (ItemEntity item : arena.getEntitiesOfClass(ItemEntity.class, area)) {
                item.teleport(new TeleportTransition(home, loot.add((item.getRandom().nextDouble() - 0.5) * 2.0, 0.2,
                        (item.getRandom().nextDouble() - 0.5) * 2.0), Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.PLACE_PORTAL_TICKET));
            }
            for (ExperienceOrb orb : arena.getEntitiesOfClass(ExperienceOrb.class, area)) {
                orb.teleport(new TeleportTransition(home, loot.add(0, 0.5, 0), Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.PLACE_PORTAL_TICKET));
            }
            if (lair != null) {
                lair.arenaVictory(home);
            }
        } else if (lair != null) {
            lair.arenaReset(home);
        }
        Component message = Component.translatable("message.kingdomsatwar.boss_arena." + kind.id() + (won ? ".victory" : ".defeat"))
                .withStyle(won ? ChatFormatting.GOLD : ChatFormatting.DARK_PURPLE);
        for (Fighter fighter : fight.fighters()) {
            if (fighter.player()) {
                ServerPlayer player = server.getPlayerList().getPlayer(fighter.id());
                if (player == null) {
                    fights.strand(fighter);
                    continue;
                }
                if (inArena(player, arena, fight)) {
                    player.sendSystemMessage(message);
                }
            }
            sendBack(server, arena, fight, fighter);
        }
        // was noch in der Arena ist (der Boss, Diener, Geschosse), verschwindet
        for (Entity entity : arena.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
            entity.discard();
        }
        for (long[] chunk : chunks(fight.center(), kind.reach())) {
            arena.setChunkForced((int) chunk[0], (int) chunk[1], false);
        }
        fights.remove(fight);
        KingdomsAtWar.LOGGER.info("Kampfdimension {}: Kampf in Arena {} beendet – {}", kind.id(), fight.slot(), won ? "Sieg" : "Niederlage");
    }

    /** Einen Kämpfer dorthin zurückschicken, wo er herkam (wenn er noch in der Arena ist und lebt). */
    static void sendBack(MinecraftServer server, ServerLevel arena, Fight fight, Fighter fighter) {
        Entity entity = fighter.player() ? server.getPlayerList().getPlayer(fighter.id()) : arena.getEntity(fighter.id());
        if (entity == null || !inArena(entity, arena, fight) || !entity.isAlive()) {
            return;
        }
        sendBack(server, entity, fighter);
    }

    private static void sendBack(MinecraftServer server, Entity entity, Fighter fighter) {
        ServerLevel destination = server.getLevel(fighter.level());
        if (destination == null) {
            destination = server.overworld();
        }
        entity.resetFallDistance();
        entity.teleport(new TeleportTransition(destination, fighter.pos(), Vec3.ZERO, fighter.yaw(), 0.0f,
                fighter.player() ? TeleportTransition.DO_NOTHING : TeleportTransition.PLACE_PORTAL_TICKET));
    }

    // =====================================================================================
    // Tod, Anmeldung
    // =====================================================================================

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level) || player.isCreative()) {
            return;
        }
        if (BossArenaKind.isArena(level)) {
            // ein Unsterblicher (ab Heldenstufe 200) behält, was er bei sich trägt
            if (io.github.sirschrolli.kingdomsatwar.hero.ImmortalKeep.keeps(player)) {
                io.github.sirschrolli.kingdomsatwar.hero.ImmortalKeep.keep(player);
            } else {
                loseEverything(player);
            }
        } else {
            settleLocalBosses(level, player);
        }
    }

    /** Wer in einer Kampfdimension stirbt, verliert alles, was er bei sich trägt (auch mit „keepInventory“). */
    public static void loseEverything(Player player) {
        player.getInventory().clearContent();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.getCraftSlots().clearContent();
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.boss_arena.lost_everything").withStyle(ChatFormatting.DARK_RED));
    }

    /**
     * Ein Spieler fällt an einem Hort, an dem (ausnahmsweise) in der Welt gekämpft wird: Steht kein anderer mehr dem Boss
     * gegenüber, vergeht er, und der Hort ruht wieder – wer wiederkommt, trifft auf einen unversehrten Boss in seiner
     * ursprünglichen Gestalt.
     */
    static void settleLocalBosses(ServerLevel level, ServerPlayer dead) {
        for (Mob boss : level.getEntitiesOfClass(Mob.class, dead.getBoundingBox().inflate(96.0), e -> e instanceof BossLair.Bound && e.isAlive())) {
            BlockPos lairPos = ((BossLair.Bound) boss).lairPos();
            if (lairPos == null || !(level.getBlockEntity(lairPos) instanceof BossLair lair) || lair.isArenaReplica()) {
                continue;
            }
            if (lair.arenaBoss(level) != boss || othersStand(level, lair, dead)) {
                continue;
            }
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, boss.getX(), boss.getY(0.5), boss.getZ(), 60, 0.8, 1.2, 0.8, 0.06);
            level.playSound(null, boss.blockPosition(), SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 0.4f, 1.6f);
            lair.arenaReset(level);
            KingdomsAtWar.LOGGER.info("{} hat keinen Gegner mehr – der Hort bei {} ruht wieder", boss.getType().getDescriptionId(), lairPos);
        }
    }

    /** Steht außer {@code dead} noch jemand (lebend, nicht im Kreativ- oder Zuschauermodus) am Hort? */
    private static boolean othersStand(ServerLevel level, BossLair lair, ServerPlayer dead) {
        BlockPos focus = lair.arenaFocus();
        double reach = lair.arenaKind().pullRadius() * 2.5;
        for (ServerPlayer player : level.players()) {
            if (player != dead && player.isAlive() && !player.isSpectator() && !player.isCreative()
                    && player.distanceToSqr(Vec3.atCenterOf(focus)) < reach * reach) {
                return true;
            }
        }
        return false;
    }

    /** Wer in einer Kampfdimension stand, als sein Kampf zu Ende ging (abgemeldet), kehrt beim Anmelden zurück. */
    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level) || !BossArenaKind.isArena(level)) {
            return;
        }
        MinecraftServer server = level.getServer();
        BossFights fights = BossFights.of(server);
        if (fights.byFighter(player.getUUID()) != null) {
            return;
        }
        Fighter stranded = fights.takeStranded(player.getUUID());
        fights.save(server);
        if (stranded != null) {
            sendBack(server, player, stranded);
        } else {
            player.teleport(TeleportTransition.createDefault(player, TeleportTransition.DO_NOTHING));
        }
    }
}
