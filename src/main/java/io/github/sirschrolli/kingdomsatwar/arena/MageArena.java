package io.github.sirschrolli.kingdomsatwar.arena;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights.Fight;
import io.github.sirschrolli.kingdomsatwar.arena.ArenaFights.Fighter;
import io.github.sirschrolli.kingdomsatwar.entity.boss.mage.MageBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.worldgen.tower.MageArenaBuilder;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.TriState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.FireChargeItem;
import net.minecraft.world.item.FlintAndSteelItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.BlockEvent;
import net.neoforged.neoforge.event.level.ExplosionEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Der Kampf in der Arkanen Sphäre (die Regeln – nichts bauen, nichts abbauen, Explosionen ohne Wirkung – gelten auch in den
 * Kampfdimensionen der großen Bosse, siehe {@link BossArenas}): Betritt jemand das Sanktum, zieht der Erzmagier ihn, alle Mitspieler im Sanktum und
 * ihre Soldaten in der Nähe in seine Arena. Dort lässt sich nichts bauen und nichts abbauen; wer stirbt, verliert alles,
 * was er bei sich trägt. Wer vom Rand stürzt, wirft die Sphäre verletzt zurück. Fällt der Magier, kehren alle mit der
 * Beute in sein Sanktum zurück; fällt niemand mehr, der ihm die Stirn bietet, kehrt er geheilt in seinen Turm zurück.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class MageArena {
    /** So nah am Platz des Magiers müssen Spieler sein, um mitgezogen zu werden. */
    public static final double PULL_RADIUS = 14.0;
    /** So nah an einem mitgezogenen Spieler müssen seine Soldaten sein. */
    public static final double TROOP_RADIUS = 24.0;
    /** Wer tiefer fällt als so viele Blöcke unter den Arenaboden, wird zurückgeworfen. */
    public static final int FALL_LIMIT = 14;
    /** So viel Schaden nimmt, wen die Sphäre zurückwirft. */
    public static final float FALL_DAMAGE = 6.0f;
    /** Halbmesser, in dem der Magier sich in der Arena bewegt (im Sanktum ist es enger). */
    public static final double ARENA_ROOM = 12.0;
    /** So lange (Ticks) liegt die Beute nach dem Ende der Todesanimation, bevor die Sphäre alle zurückschickt. */
    public static final int RETURN_DELAY = 100;

    /** Magier, die ihre Gegner beim nächsten Server-Tick in die Sphäre ziehen. */
    private static final Map<UUID, ResourceKey<Level>> PENDING = new LinkedHashMap<>();

    private MageArena() {}

    // =====================================================================================
    // Hineinziehen
    // =====================================================================================

    /**
     * Ein Erzmagier im Turm erwacht: Statt im Sanktum zu kämpfen, zieht er seine Gegner in die Sphäre (beim nächsten
     * Server-Tick). Gibt {@code false} zurück, wenn er schon in der Sphäre ist oder es sie nicht gibt.
     */
    public static boolean requestPull(ServerLevel level, MageBossEntity mage) {
        if (ArcaneSphere.is(level) || level.getServer().getLevel(ArcaneSphere.LEVEL) == null || mage.home() == null) {
            return false;
        }
        // auf dem GameTest-Server prüfen die Tests den Kampf im Sanktum und die Sphäre getrennt (die Sphäre über pull())
        if (level.getServer() instanceof net.minecraft.gametest.framework.GameTestServer) {
            return false;
        }
        PENDING.put(mage.getUUID(), level.dimension());
        return true;
    }

    /** Zieht die Spieler im Sanktum, ihre Soldaten in der Nähe und den Magier in eine frisch gebaute Arena. */
    public static @Nullable Fight pull(ServerLevel tower, MageBossEntity mage) {
        ServerLevel arena = tower.getServer().getLevel(ArcaneSphere.LEVEL);
        if (arena == null) {
            return null;
        }
        int slot = ArenaFights.of(tower.getServer()).freeSlot();
        return pull(tower, mage, arena, slot, ArenaFights.arenaCenter(slot));
    }

    /** Wie {@link #pull(ServerLevel, MageBossEntity)}, mit Arena-Welt und Mitte (für Tests auch in der Oberwelt). */
    public static @Nullable Fight pull(ServerLevel tower, MageBossEntity mage, ServerLevel arena, int slot, BlockPos center) {
        MinecraftServer server = tower.getServer();
        BlockPos home = mage.home();
        if (home == null || !mage.isAlive() || mage.state() != MageBossEntity.State.DORMANT) {
            return null;
        }
        ArenaFights fights = ArenaFights.of(server);
        if (fights.busy(home) || fights.byMage(mage.getUUID()) != null) {
            return null;
        }
        Vec3 sanctum = Vec3.atBottomCenterOf(home);
        List<ServerPlayer> players = new ArrayList<>();
        for (ServerPlayer player : tower.players()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative() && player.position().distanceToSqr(sanctum) < PULL_RADIUS * PULL_RADIUS
                    && Math.abs(player.getY() - home.getY()) < 8.0) {
                players.add(player);
            }
        }
        if (players.isEmpty()) {
            return null;
        }
        Set<UUID> owners = new HashSet<>();
        players.forEach(p -> owners.add(p.getUUID()));
        List<SoldierEntity> troops = new ArrayList<>();
        for (ServerPlayer player : players) {
            for (SoldierEntity soldier : tower.getEntitiesOfClass(SoldierEntity.class, player.getBoundingBox().inflate(TROOP_RADIUS),
                    s -> s.isAlive() && s.getOwnerId() != null && owners.contains(s.getOwnerId()))) {
                if (!troops.contains(soldier)) {
                    troops.add(soldier);
                }
            }
        }
        for (long[] chunk : MageArenaBuilder.chunks(center)) {
            arena.getChunk((int) chunk[0], (int) chunk[1]);
            arena.setChunkForced((int) chunk[0], (int) chunk[1], true);
        }
        MageArenaBuilder.build(arena, center, mage.element());

        List<Fighter> fighters = new ArrayList<>();
        List<Vec3> spots = MageArenaBuilder.spawnPoints(center, players.size() + troops.size());
        int i = 0;
        Component pulled = Component.translatable("message.kingdomsatwar.arena.pulled",
                Component.translatable(mage.getType().getDescriptionId()).withColor(mage.element().color())).withStyle(ChatFormatting.LIGHT_PURPLE);
        for (ServerPlayer player : players) {
            fighters.add(new Fighter(player.getUUID(), true, tower.dimension(), player.position(), player.getYRot()));
            Vec3 spot = spots.get(i++);
            tower.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX(), player.getY() + 1.0, player.getZ(), 40, 0.4, 0.8, 0.4, 0.05);
            player.teleport(new TeleportTransition(arena, spot, Vec3.ZERO, 180.0f, 0.0f, TeleportTransition.DO_NOTHING));
            player.sendSystemMessage(pulled);
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.arena.rules").withStyle(ChatFormatting.GRAY));
        }
        for (SoldierEntity soldier : troops) {
            Vec3 from = soldier.position();
            float yaw = soldier.getYRot();
            Entity moved = soldier.teleport(new TeleportTransition(arena, spots.get(i++), Vec3.ZERO, 180.0f, 0.0f, TeleportTransition.DO_NOTHING));
            if (moved != null) {
                fighters.add(new Fighter(moved.getUUID(), false, tower.dimension(), from, yaw));
            }
        }
        Entity moved = mage.teleport(new TeleportTransition(arena, Vec3.atBottomCenterOf(center.above()), Vec3.ZERO, 0.0f, 0.0f,
                TeleportTransition.DO_NOTHING));
        if (!(moved instanceof MageBossEntity arcane)) {
            KingdomsAtWar.LOGGER.error("Erzmagier {} ließ sich nicht in die Arkane Sphäre bringen", mage.getUUID());
            for (Fighter fighter : fighters) {
                Entity entity = fighter.player() ? server.getPlayerList().getPlayer(fighter.id()) : arena.getEntity(fighter.id());
                ServerLevel back = server.getLevel(fighter.level());
                if (entity != null && back != null) {
                    entity.teleport(new TeleportTransition(back, fighter.pos(), Vec3.ZERO, fighter.yaw(), 0.0f, TeleportTransition.DO_NOTHING));
                }
            }
            return null;
        }
        arcane.setHome(center.above());
        arcane.setRoom(ARENA_ROOM);
        arcane.setTower(tower.dimension(), home);   // so findet er auch ohne Kampfeintrag heim
        long now = server.overworld().getGameTime();
        Fight fight = new Fight(slot, center, mage.element(), arcane.getUUID(), tower.dimension(), home, fighters, now, false, 0L);
        fights.add(fight);
        fights.save(server);
        arena.playSound(null, center.above(), SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 0.6f, 1.4f);
        arcane.beginAwakening(arena);
        KingdomsAtWar.LOGGER.info("{} zieht {} Spieler und {} Soldaten in die Arkane Sphäre (Arena {})", mage.element().id(), players.size(),
                troops.size(), slot);
        return fight;
    }

    // =====================================================================================
    // Jeden Tick
    // =====================================================================================

    @SubscribeEvent
    static void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (!PENDING.isEmpty()) {
            Map<UUID, ResourceKey<Level>> pending = new LinkedHashMap<>(PENDING);
            PENDING.clear();
            pending.forEach((id, dimension) -> {
                ServerLevel level = server.getLevel(dimension);
                if (level != null && level.getEntity(id) instanceof MageBossEntity mage && pull(level, mage) == null
                        && mage.isAlive() && mage.state() == MageBossEntity.State.DORMANT) {
                    // niemand im Sanktum (etwa ein Schütze von draußen): er kämpft wie früher in seinem Turm
                    mage.beginAwakening(level);
                }
            });
        }
        ArenaFights fights = ArenaFights.of(server);
        if (fights.all().isEmpty()) {
            return;
        }
        ServerLevel arena = server.getLevel(ArcaneSphere.LEVEL);
        if (arena == null) {
            return;
        }
        boolean changed = false;
        for (Fight fight : fights.all()) {
            changed |= tick(server, arena, fights, fight);
        }
        if (changed) {
            fights.save(server);
        }
    }

    /** Ein Kampf: Abgestürzte zurückwerfen, Sieg oder Niederlage erkennen. Gibt zurück, ob sich etwas geändert hat. */
    public static boolean tick(MinecraftServer server, ServerLevel arena, ArenaFights fights, Fight fight) {
        BlockPos center = fight.center();
        long now = server.overworld().getGameTime();
        for (Fighter fighter : fight.fighters()) {
            Entity entity = fighter.player() ? server.getPlayerList().getPlayer(fighter.id()) : arena.getEntity(fighter.id());
            if (entity != null && inArena(entity, arena, fight) && entity.isAlive() && entity.getY() < center.getY() - FALL_LIMIT) {
                rescue(arena, entity, center);
            }
        }
        Entity mage = arena.getEntity(fight.mage());
        if (fight.won()) {
            if (mage != null && !mage.isRemoved()) {
                return false;
            }
            if (now - fight.wonAt() < MageBossEntity.DEATH_TICKS + RETURN_DELAY) {
                return false;
            }
            finish(server, arena, fights, fight, true);
            return true;
        }
        if (mage == null || mage.isRemoved()) {
            if (!entitiesLoaded(arena, center)) {
                // nach einem Neustart sind die Wesen der Arena noch nicht geladen – der Magier ist nicht fort, er kommt gleich
                return false;
            }
            // der Magier ist fort, ohne zu sterben (etwa durch einen Befehl entfernt): der Kampf ist vorbei
            fight.win(now - MageBossEntity.DEATH_TICKS);
            return true;
        }
        if (!anyoneLeft(server, arena, fight)) {
            finish(server, arena, fights, fight, false);
            return true;
        }
        return false;
    }

    /** Sind die Wesen in allen Chunks der Arena geladen (sonst lässt sich nicht sagen, wer noch da ist)? */
    public static boolean entitiesLoaded(ServerLevel arena, BlockPos center) {
        for (long[] chunk : MageArenaBuilder.chunks(center)) {
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

    /** Ist das Wesen in der Arena dieses Kampfs (in der Sphäre, nahe ihrer Mitte)? */
    public static boolean inArena(Entity entity, ServerLevel arena, Fight fight) {
        Vec3 center = Vec3.atBottomCenterOf(fight.center());
        return entity.level() == arena && entity.position().subtract(center).horizontalDistance() < MageArenaBuilder.REACH + 16
                && Math.abs(entity.getY() - center.y) < 96.0;
    }

    /** Wer in den Abgrund stürzt, landet verletzt wieder in der Arena. */
    static void rescue(ServerLevel arena, Entity entity, BlockPos center) {
        Vec3 spot = MageArenaBuilder.spawnPoints(center, 1).getFirst();
        entity.teleport(new TeleportTransition(arena, spot, Vec3.ZERO, 180.0f, 0.0f, TeleportTransition.DO_NOTHING));
        entity.resetFallDistance();
        if (entity instanceof LivingEntity living) {
            living.hurtServer(arena, arena.damageSources().magic(), FALL_DAMAGE);
        }
        arena.sendParticles(ParticleTypes.REVERSE_PORTAL, spot.x, spot.y + 1.0, spot.z, 30, 0.4, 0.8, 0.4, 0.05);
        arena.playSound(null, BlockPos.containing(spot), SoundEvents.ENDERMAN_TELEPORT, SoundSource.PLAYERS, 1.0f, 0.7f);
        if (entity instanceof ServerPlayer player) {
            player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.arena.rescued").withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    /** Das Ende eines Kampfs: Sieg (alle mit der Beute zurück) oder Niederlage (der Magier kehrt geheilt heim). */
    public static void finish(MinecraftServer server, ServerLevel arena, ArenaFights fights, Fight fight, boolean won) {
        ServerLevel tower = server.getLevel(fight.towerLevel());
        if (tower == null) {
            tower = server.overworld();
        }
        BlockPos center = fight.center();
        AABB area = new AABB(center).inflate(MageArenaBuilder.REACH, 48.0, MageArenaBuilder.REACH);
        if (won) {
            // die Beute landet im Sanktum, wo der Magier meditierte
            Vec3 loot = Vec3.atBottomCenterOf(fight.towerHome());
            for (ItemEntity item : arena.getEntitiesOfClass(ItemEntity.class, area)) {
                item.teleport(new TeleportTransition(tower, loot.add((item.getRandom().nextDouble() - 0.5) * 2.0, 0.2,
                        (item.getRandom().nextDouble() - 0.5) * 2.0), Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.PLACE_PORTAL_TICKET));
            }
            for (ExperienceOrb orb : arena.getEntitiesOfClass(ExperienceOrb.class, area)) {
                orb.teleport(new TeleportTransition(tower, loot.add(0, 0.5, 0), Vec3.ZERO, 0.0f, 0.0f, TeleportTransition.PLACE_PORTAL_TICKET));
            }
        } else {
            // der Magier hat gewonnen: er kehrt geheilt in seinen Turm zurück
            if (arena.getEntity(fight.mage()) instanceof MageBossEntity mage && mage.isAlive()) {
                returnHome(mage, tower, fight.towerHome());
            }
        }
        Component message = Component.translatable(won ? "message.kingdomsatwar.arena.victory" : "message.kingdomsatwar.arena.defeat")
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
        // was noch in der Arena ist (Spiegelbilder, Diener, Geschosse), verschwindet
        for (Entity entity : arena.getEntitiesOfClass(Entity.class, area, e -> !(e instanceof Player))) {
            entity.discard();
        }
        for (long[] chunk : MageArenaBuilder.chunks(center)) {
            arena.setChunkForced((int) chunk[0], (int) chunk[1], false);
        }
        fights.remove(fight);
        KingdomsAtWar.LOGGER.info("Kampf in Arena {} beendet: {}", fight.slot(), won ? "Sieg" : "Niederlage");
    }

    /**
     * Der Magier kehrt in seinen Turm zurück: geheilt, an seinen Platz im Sanktum, wieder in Meditation – mit einem
     * Wirbel aus Funken, wo er erscheint. Gibt den Magier im Turm zurück (oder {@code null}, wenn es nicht ging).
     */
    public static @Nullable MageBossEntity returnHome(MageBossEntity mage, ServerLevel tower, BlockPos towerHome) {
        if (mage.level() instanceof ServerLevel from) {
            mage.retreat(from);
        }
        Vec3 spot = Vec3.atBottomCenterOf(towerHome);
        Entity back = mage.level() == tower ? mage : mage.teleport(new TeleportTransition(tower, spot, Vec3.ZERO, mage.getYRot(), 0.0f,
                TeleportTransition.PLACE_PORTAL_TICKET));
        if (!(back instanceof MageBossEntity home)) {
            KingdomsAtWar.LOGGER.error("Erzmagier {} fand nicht in seinen Turm bei {} zurück", mage.getUUID(), towerHome);
            return null;
        }
        home.setHome(towerHome);
        home.setRoom(MageBossEntity.ROOM_RADIUS);
        home.clearTower();
        home.retreat(tower);
        tower.sendParticles(ParticleTypes.REVERSE_PORTAL, spot.x, spot.y + 1.0, spot.z, 60, 0.5, 1.0, 0.5, 0.06);
        tower.sendParticles(new DustParticleOptions(home.element().color(), 1.6f), spot.x, spot.y + 1.0, spot.z, 30, 0.6, 1.0, 0.6, 0.0);
        tower.playSound(null, towerHome, SoundEvents.PORTAL_TRAVEL, SoundSource.HOSTILE, 0.5f, 1.6f);
        KingdomsAtWar.LOGGER.info("{} kehrt in seinen Turm bei {} zurück", home.element().id(), towerHome);
        return home;
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
        entity.teleport(new TeleportTransition(destination, fighter.pos(), Vec3.ZERO, fighter.yaw(), 0.0f,
                fighter.player() ? TeleportTransition.DO_NOTHING : TeleportTransition.PLACE_PORTAL_TICKET));
    }

    // =====================================================================================
    // Tod, Anmeldung
    // =====================================================================================

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(entity.level() instanceof ServerLevel level)) {
            return;
        }
        if (entity instanceof MageBossEntity mage) {
            ArenaFights fights = ArenaFights.of(level.getServer());
            Fight fight = fights.byMage(mage.getUUID());
            if (fight != null) {
                fight.win(level.getServer().overworld().getGameTime());
                fights.save(level.getServer());
            }
        } else if (entity instanceof ServerPlayer player && !player.isCreative() && ArcaneSphere.is(level)) {
            // ein Unsterblicher (ab Heldenstufe 200) behält, was er bei sich trägt
            if (io.github.sirschrolli.kingdomsatwar.hero.ImmortalKeep.keeps(player)) {
                io.github.sirschrolli.kingdomsatwar.hero.ImmortalKeep.keep(player);
            } else {
                loseEverything(player);
            }
        }
    }

    /** Wer in der Sphäre stirbt, verliert alles, was er bei sich trägt (auch mit „keepInventory“). */
    public static void loseEverything(Player player) {
        player.getInventory().clearContent();
        player.containerMenu.setCarried(ItemStack.EMPTY);
        player.inventoryMenu.getCraftSlots().clearContent();
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.arena.lost_everything").withStyle(ChatFormatting.DARK_RED));
    }

    /** Wer in der Sphäre stand, als sein Kampf zu Ende ging (abgemeldet), kehrt beim Anmelden zurück. */
    @SubscribeEvent
    static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level) || !ArcaneSphere.is(level)) {
            return;
        }
        MinecraftServer server = level.getServer();
        ArenaFights fights = ArenaFights.of(server);
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

    // =====================================================================================
    // Regeln der Sphäre: nichts bauen, nichts abbauen
    // =====================================================================================

    /** Gilt hier das Bauverbot der Sphäre (für alle außer Spieler im Kreativmodus)? */
    public static boolean forbidden(@Nullable Entity entity, LevelAccessor level) {
        return level instanceof Level l && forbidden(entity, l.dimension());
    }

    /** Gilt in dieser Dimension für dieses Wesen das Bauverbot (in der Sphäre und in den Kampfdimensionen der Bosse)? */
    public static boolean forbidden(@Nullable Entity entity, ResourceKey<Level> dimension) {
        return (dimension == ArcaneSphere.LEVEL || BossArenaKind.isArena(dimension)) && !(entity instanceof Player player && player.isCreative());
    }

    private static void refuse(@Nullable Entity entity) {
        if (entity instanceof ServerPlayer player) {
            String key = BossArenaKind.isArena(player.level()) ? "message.kingdomsatwar.boss_arena.forbidden" : "message.kingdomsatwar.arena.forbidden";
            player.sendOverlayMessage(Component.translatable(key).withStyle(ChatFormatting.LIGHT_PURPLE));
        }
    }

    @SubscribeEvent
    static void onPlace(BlockEvent.EntityPlaceEvent event) {
        if (forbidden(event.getEntity(), event.getLevel())) {
            event.setCanceled(true);
            refuse(event.getEntity());
        }
    }

    @SubscribeEvent
    static void onBreak(net.neoforged.neoforge.event.level.block.BreakBlockEvent event) {
        if (forbidden(event.getPlayer(), event.getLevel())) {
            event.setCanceled(true);
            refuse(event.getPlayer());
        }
    }

    @SubscribeEvent
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        if (forbidden(event.getEntity(), event.getEntity().level())) {
            event.setCanceled(true);
        }
    }

    /** Nichts auf Blöcke setzen: keine Blöcke, kein Wasser, kein Feuer, keine Wesen aus Eiern. */
    @SubscribeEvent
    static void onUseOnBlock(PlayerInteractEvent.RightClickBlock event) {
        if (forbidden(event.getEntity(), event.getLevel()) && places(event.getItemStack())) {
            event.setUseItem(TriState.FALSE);
            refuse(event.getEntity());
        }
    }

    @SubscribeEvent
    static void onUseItem(PlayerInteractEvent.RightClickItem event) {
        if (forbidden(event.getEntity(), event.getLevel()) && event.getItemStack().getItem() instanceof BucketItem) {
            event.setCanceled(true);
            refuse(event.getEntity());
        }
    }

    private static boolean places(ItemStack stack) {
        return stack.getItem() instanceof BlockItem || stack.getItem() instanceof BucketItem || stack.getItem() instanceof FlintAndSteelItem
                || stack.getItem() instanceof FireChargeItem || stack.getItem() instanceof SpawnEggItem;
    }

    @SubscribeEvent
    static void onExplosion(ExplosionEvent.Detonate event) {
        if (ArcaneSphere.is(event.getLevel()) || BossArenaKind.isArena(event.getLevel())) {
            event.getAffectedBlocks().clear();
        }
    }
}
