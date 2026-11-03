package io.github.sirschrolli.kingdomsatwar.war;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import io.github.sirschrolli.kingdomsatwar.economy.Coin;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownStorage;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Container;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import org.jspecify.annotations.Nullable;

/**
 * Heerhaufen der Reiche: Liegt ein Spieler mit einem Reich im Krieg, schickt es alle paar Spieltage (Einstellung
 * {@code raidIntervalDays}) einen Heerhaufen gegen seine Siedlung – aber nur, wenn er selbst dort ist. Der Haufen
 * sammelt sich auf der Seite, auf der die Stammburg des Reichs liegt, stößt ins Horn und zieht zum Stadtbanner; er
 * kämpft gegen den Spieler und dessen Soldaten. Ist er geschlagen, liegt seine Kriegskasse im Lager der Siedlung. Steht am
 * Banner niemand mehr, der es verteidigt, plündert er das Lager und zieht ab. Jeder Heerhaufen eines Reichs ist größer
 * als der vorige (und eine große Siedlung lockt mehr Krieger an). Wer die Stammburg erobert, hat Ruhe vor diesem Reich.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class RealmRaids {
    private static final long DAY = 24000L;
    /** So oft wird geprüft, ob ein Reich angreift (Ticks). */
    private static final int CHECK_INTERVAL = 100;
    /** So nah am Banner muss der Spieler sein, damit ein Heerhaufen kommt. */
    private static final double PRESENT = 96.0;
    /** So weit vom Banner sammelt sich der Heerhaufen. */
    private static final double MUSTER = 44.0;
    /** Am Banner: so nah müssen Plünderer sein, so weit zählen Verteidiger. */
    public static final double PLUNDER_RADIUS = 10.0;
    private static final double DEFEND_RADIUS = 18.0;
    /** So lange (Ticks) muss das Banner unverteidigt sein, bis geplündert wird. */
    public static final int PLUNDER_TICKS = 300;
    /** Diesen Anteil jedes Stapels im Lager nehmen Plünderer mit. */
    private static final double PLUNDER_SHARE = 0.3;
    /** Nach dieser Zeit gibt der Heerhaufen auf und zieht ab (Ticks). */
    private static final long GIVE_UP = 20L * 60 * 12;
    /** So lange hat er Zeit, abzuziehen; wer dann noch da ist, verschwindet. */
    private static final long RETREAT_TIME = 20L * 50;
    /** Größter Heerhaufen. */
    public static final int MAX_BAND = 14;
    /** Spieler in diesem Umkreis um das Banner sehen die Leiste des Heerhaufens. */
    private static final double BAR_RANGE = 160.0;

    /** Laufende Leisten (nicht gespeichert). */
    private static final Map<UUID, ServerBossEvent> BARS = new HashMap<>();

    private RealmRaids() {}

    // =====================================================================================
    // Daten
    // =====================================================================================

    public enum Phase implements StringRepresentable {
        MARCH, RETREAT;

        static final Codec<Phase> CODEC = StringRepresentable.fromEnum(Phase::values);

        @Override
        public String getSerializedName() {
            return this.name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** Fehde zwischen Reich und Spieler: wann zuletzt ein Heerhaufen kam und wie viele es schon waren. */
    static final class Feud {
        static final Codec<Feud> CODEC = RecordCodecBuilder.create(i -> i.group(
                Realm.CODEC.fieldOf("realm").forGetter(f -> f.realm),
                UUIDUtil.CODEC.fieldOf("player").forGetter(f -> f.player),
                Codec.LONG.fieldOf("last").forGetter(f -> f.last),
                Codec.INT.optionalFieldOf("count", 0).forGetter(f -> f.count)).apply(i, Feud::new));

        final Realm realm;
        final UUID player;
        long last;
        int count;

        Feud(Realm realm, UUID player, long last, int count) {
            this.realm = realm;
            this.player = player;
            this.last = last;
            this.count = count;
        }
    }

    /** Ein Heerhaufen unterwegs. */
    public static final class Raid {
        static final Codec<Raid> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("id").forGetter(r -> r.id),
                Realm.CODEC.fieldOf("realm").forGetter(r -> r.realm),
                UUIDUtil.CODEC.fieldOf("player").forGetter(r -> r.player),
                UUIDUtil.CODEC.fieldOf("town").forGetter(r -> r.town),
                BlockPos.CODEC.fieldOf("banner").forGetter(r -> r.banner),
                BlockPos.CODEC.fieldOf("muster").forGetter(r -> r.muster),
                Codec.LONG.fieldOf("started").forGetter(r -> r.started),
                Codec.INT.fieldOf("number").forGetter(r -> r.number),
                UUIDUtil.CODEC.listOf().fieldOf("raiders").forGetter(r -> List.copyOf(r.raiders)),
                UUIDUtil.CODEC.listOf().optionalFieldOf("fallen", List.of()).forGetter(r -> List.copyOf(r.fallen)),
                Phase.CODEC.optionalFieldOf("phase", Phase.MARCH).forGetter(r -> r.phase),
                Codec.LONG.optionalFieldOf("retreat_at", 0L).forGetter(r -> r.retreatAt),
                Codec.INT.optionalFieldOf("quiet", 0).forGetter(r -> r.quiet)).apply(i, Raid::new));

        final UUID id;
        final Realm realm;
        final UUID player;
        final UUID town;
        final BlockPos banner;
        final BlockPos muster;
        final long started;
        /** Der wievielte Heerhaufen dieses Reichs gegen den Spieler. */
        final int number;
        final List<UUID> raiders = new ArrayList<>();
        final Set<UUID> fallen = new HashSet<>();
        Phase phase;
        long retreatAt;
        /** So lange (Ticks) ist das Banner schon unverteidigt, während Plünderer davor stehen. */
        int quiet;

        Raid(UUID id, Realm realm, UUID player, UUID town, BlockPos banner, BlockPos muster, long started, int number, List<UUID> raiders,
                List<UUID> fallen, Phase phase, long retreatAt, int quiet) {
            this.id = id;
            this.realm = realm;
            this.player = player;
            this.town = town;
            this.banner = banner;
            this.muster = muster;
            this.started = started;
            this.number = number;
            this.raiders.addAll(raiders);
            this.fallen.addAll(fallen);
            this.phase = phase;
            this.retreatAt = retreatAt;
            this.quiet = quiet;
        }

        public UUID id() {
            return this.id;
        }

        public Realm realm() {
            return this.realm;
        }

        public BlockPos banner() {
            return this.banner;
        }

        public BlockPos muster() {
            return this.muster;
        }

        public Phase phase() {
            return this.phase;
        }

        public int number() {
            return this.number;
        }

        public List<UUID> raiders() {
            return List.copyOf(this.raiders);
        }

        public int total() {
            return this.raiders.size();
        }

        public int alive() {
            return this.raiders.size() - this.fallen.size();
        }
    }

    /** Alle Fehden und laufenden Heerhaufen (Anhang an der Oberwelt). */
    public static final class State {
        public static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group(
                Feud.CODEC.listOf().optionalFieldOf("feuds", List.of()).forGetter(s -> List.copyOf(s.feuds)),
                Raid.CODEC.listOf().optionalFieldOf("raids", List.of()).forGetter(s -> List.copyOf(s.raids))).apply(i, State::new));

        final List<Feud> feuds = new ArrayList<>();
        final List<Raid> raids = new ArrayList<>();

        public State() {}

        State(List<Feud> feuds, List<Raid> raids) {
            this.feuds.addAll(feuds);
            this.raids.addAll(raids);
        }

        public List<Raid> raids() {
            return List.copyOf(this.raids);
        }
    }

    public static State state(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.RAIDS);
    }

    private static void save(ServerLevel level, State state) {
        level.getServer().overworld().setData(ModAttachments.RAIDS, state);
    }

    /** Der laufende Heerhaufen gegen diese Siedlung (oder keiner). */
    public static @Nullable Raid against(ServerLevel level, Town town) {
        for (Raid raid : state(level).raids) {
            if (raid.town.equals(town.id())) {
                return raid;
            }
        }
        return null;
    }

    private static Feud feud(State state, Realm realm, UUID player, long now) {
        for (Feud feud : state.feuds) {
            if (feud.realm == realm && feud.player.equals(player)) {
                return feud;
            }
        }
        // die erste Kriegserklärung: der erste Heerhaufen kommt frühestens nach einer vollen Frist
        Feud feud = new Feud(realm, player, now, 0);
        state.feuds.add(feud);
        return feud;
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.dimension() != Level.OVERWORLD) {
            return;
        }
        long now = level.getGameTime();
        if (now % 20 == 0) {
            tickRaids(level);
        }
        if (now % CHECK_INTERVAL == 0 && ModServerConfig.REALM_RAIDS.get()) {
            checkFeuds(level);
        }
    }

    /** Ist es Zeit für einen Heerhaufen? Für jeden Spieler, der in seiner Siedlung ist und mit einem Reich im Krieg liegt. */
    private static void checkFeuds(ServerLevel level) {
        State state = state(level);
        RealmState realms = RealmState.of(level);
        long now = level.getGameTime();
        long interval = ModServerConfig.RAID_INTERVAL_DAYS.get() * DAY;
        int feudsBefore = state.feuds.size();
        for (ServerPlayer player : level.players()) {
            if (player.isSpectator()) {
                continue;
            }
            Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
            if (town == null || !town.hasBanner() || town.dimension() != Level.OVERWORLD
                    || player.distanceToSqr(Vec3.atCenterOf(town.banner())) > PRESENT * PRESENT || against(level, town) != null) {
                continue;
            }
            Feud due = null;
            for (Realm realm : Realm.values()) {
                if (realms.isConquered(realm) || !realms.atWar(realm, player.getUUID())) {
                    continue;
                }
                Feud feud = feud(state, realm, player.getUUID(), now);
                if (now - feud.last >= interval && (due == null || feud.last < due.last)) {
                    due = feud;
                }
            }
            if (due != null && start(level, due.realm, player, town) != null) {
                return;
            }
        }
        if (state.feuds.size() != feudsBefore) {
            save(level, state);
        }
    }

    /**
     * Ein Heerhaufen von {@code realm} zieht gegen die Siedlung {@code town} des Spielers. @return der Heerhaufen (oder
     * {@code null}, wenn sich kein Sammelplatz fand)
     */
    public static @Nullable Raid start(ServerLevel level, Realm realm, ServerPlayer player, Town town) {
        State state = state(level);
        long now = level.getGameTime();
        BlockPos banner = town.banner();
        BlockPos muster = musterPoint(level, realm, banner);
        if (muster == null) {
            return null;
        }
        Feud feud = feud(state, realm, player.getUUID(), now);
        feud.count++;
        feud.last = now;
        int number = feud.count;
        Raid raid = new Raid(UUID.randomUUID(), realm, player.getUUID(), town.id(), banner, muster, now, number, List.of(), List.of(), Phase.MARCH,
                0L, 0);
        RandomSource random = level.getRandom();
        for (WarriorRole role : band(number, town.tier().ordinal(), random)) {
            RealmWarriorEntity warrior = ModEntities.REALM_WARRIORS.get(role).get().create(level, EntitySpawnReason.EVENT);
            if (warrior == null) {
                continue;
            }
            BlockPos at = ground(level, muster.offset(random.nextInt(7) - 3, 0, random.nextInt(7) - 3));
            if (at == null) {
                at = muster;
            }
            warrior.setRealm(realm);
            float yaw = (float) (Mth.atan2(banner.getZ() - at.getZ(), banner.getX() - at.getX()) * Mth.RAD_TO_DEG) - 90.0f;
            warrior.snapTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, yaw, 0.0f);
            warrior.finalizeSpawn(level, level.getCurrentDifficultyAt(at), EntitySpawnReason.EVENT, null);
            warrior.joinRaid(raid.id, banner);
            warrior.setPersistenceRequired();
            level.addFreshEntity(warrior);
            raid.raiders.add(warrior.getUUID());
        }
        if (raid.raiders.isEmpty()) {
            return null;
        }
        state.raids.add(raid);
        save(level, state);
        announce(level, raid, town);
        KingdomsAtWar.LOGGER.info("Heerhaufen Nr. {} von {} ({} Krieger) gegen {} bei {}", number, realm.id(), raid.raiders.size(), town.name(),
                muster);
        return raid;
    }

    /**
     * Zusammensetzung: immer ein Hauptmann; dazu Fußknechte, Bogen- und Armbrustschützen, ab dem zweiten Heerhaufen auch
     * Ritter. Größe: 6 beim ersten, je Heerhaufen einer mehr und für jede Stufe der Siedlung noch einer (höchstens 14).
     */
    public static List<WarriorRole> band(int number, int tier, RandomSource random) {
        int size = Mth.clamp(5 + number + tier, 6, MAX_BAND);
        List<WarriorRole> band = new ArrayList<>();
        band.add(WarriorRole.CAPTAIN);
        while (band.size() < size) {
            int roll = random.nextInt(100);
            if (number >= 2 && roll < 12) {
                band.add(WarriorRole.KNIGHT);
            } else if (roll < 30) {
                band.add(WarriorRole.ARCHER);
            } else if (roll < 44) {
                band.add(WarriorRole.CROSSBOWMAN);
            } else {
                band.add(WarriorRole.FOOTMAN);
            }
        }
        return band;
    }

    /**
     * Sammelplatz: auf der Seite der Siedlung, auf der die Stammburg liegt, {@link #MUSTER} Blöcke vom Banner – auf festem,
     * trockenem, geladenem Boden (sonst etwas weiter herum).
     */
    private static @Nullable BlockPos musterPoint(ServerLevel level, Realm realm, BlockPos banner) {
        BlockPos castle = RealmState.of(level).castle(realm);
        if (castle == null) {
            castle = RealmLocator.approximateCenter(level, realm);
        }
        double base = castle != null ? Math.atan2(castle.getZ() - banner.getZ(), castle.getX() - banner.getX()) : realm.ordinal() * Math.PI / 2;
        for (double offset : new double[] {0, 0.35, -0.35, 0.7, -0.7, 1.2, -1.2, 1.8, -1.8, Math.PI}) {
            for (double distance : new double[] {MUSTER, MUSTER * 0.75, MUSTER * 1.2}) {
                double a = base + offset;
                BlockPos at = ground(level, banner.offset((int) Math.round(Math.cos(a) * distance), 0, (int) Math.round(Math.sin(a) * distance)));
                if (at != null) {
                    return at;
                }
            }
        }
        return null;
    }

    /** Fester, trockener Stand an dieser Stelle (Höhe aus der Karte), nur in geladenen Chunks. */
    private static @Nullable BlockPos ground(ServerLevel level, BlockPos pos) {
        if (!level.isLoaded(pos)) {
            return null;
        }
        BlockPos top = level.getHeightmapPos(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, pos);
        var below = level.getBlockState(top.below());
        if (!below.getFluidState().isEmpty() || below.getCollisionShape(level, top.below()).isEmpty()
                || !level.getBlockState(top).getCollisionShape(level, top).isEmpty()
                || !level.getBlockState(top.above()).getCollisionShape(level, top.above()).isEmpty()) {
            return null;
        }
        return top;
    }

    private static void announce(ServerLevel level, Raid raid, Town town) {
        Component title = Component.translatable("raid.kingdomsatwar.title").withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD);
        Component subtitle = Component.translatable("raid.kingdomsatwar.subtitle", raid.realm.shortName(), raid.total())
                .withStyle(ChatFormatting.GOLD);
        Component message = Component.translatable("raid.kingdomsatwar.start", raid.realm.shortName(), town.name(), raid.total())
                .withStyle(ChatFormatting.RED);
        Component hint = Component.translatable("raid.kingdomsatwar.hint").withStyle(ChatFormatting.YELLOW);
        for (ServerPlayer player : level.players()) {
            if (player.getUUID().equals(raid.player) || player.distanceToSqr(Vec3.atCenterOf(raid.banner)) < BAR_RANGE * BAR_RANGE) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
                player.connection.send(new ClientboundSetTitleTextPacket(title));
                player.connection.send(new ClientboundSetSubtitleTextPacket(subtitle));
                player.sendSystemMessage(message);
                player.sendSystemMessage(hint);
            }
        }
        // das Horn ist weit zu hören – aus der Richtung, aus der der Haufen kommt
        level.playSound(null, raid.muster, SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 64.0f, 0.9f);
    }

    /** Jede Sekunde: Leiste, Sieg, Plündern, Abzug. */
    private static void tickRaids(ServerLevel level) {
        State state = state(level);
        if (state.raids.isEmpty()) {
            if (!BARS.isEmpty()) {
                BARS.values().forEach(ServerBossEvent::removeAllPlayers);
                BARS.clear();
            }
            return;
        }
        boolean changed = false;
        long now = level.getGameTime();
        for (Raid raid : List.copyOf(state.raids)) {
            Town town = Towns.of(level.getServer()).byId(raid.town);
            List<RealmWarriorEntity> present = present(level, raid);
            updateBar(level, raid);
            if (raid.alive() <= 0) {
                victory(level, raid, town);
                end(level, state, raid);
                changed = true;
                continue;
            }
            if (town == null || !town.hasBanner()) {
                if (raid.phase == Phase.MARCH) {
                    retreat(level, raid, present);
                    changed = true;
                }
            }
            if (raid.phase == Phase.MARCH) {
                if (now - raid.started > GIVE_UP) {
                    retreat(level, raid, present);
                    changed = true;
                    continue;
                }
                boolean atBanner = present.stream().anyMatch(w -> w.distanceToSqr(Vec3.atCenterOf(raid.banner)) < PLUNDER_RADIUS * PLUNDER_RADIUS);
                if (atBanner && !defended(level, raid)) {
                    raid.quiet += 20;
                    changed = true;
                    if (raid.quiet >= PLUNDER_TICKS && town != null) {
                        plunder(level, raid, town);
                        retreat(level, raid, present);
                    }
                } else if (raid.quiet != 0) {
                    raid.quiet = 0;
                    changed = true;
                }
            } else {
                // Abzug: wer am Sammelplatz ankommt (oder wenn die Zeit um ist), verschwindet
                boolean timeUp = now - raid.retreatAt > RETREAT_TIME;
                for (RealmWarriorEntity warrior : present) {
                    if (timeUp || warrior.distanceToSqr(Vec3.atCenterOf(raid.muster)) < 6.0 * 6.0) {
                        raid.fallen.add(warrior.getUUID());
                        warrior.discard();
                        changed = true;
                    }
                }
                if (timeUp || raid.alive() <= 0) {
                    message(level, raid, Component.translatable("raid.kingdomsatwar.gone", raid.realm.shortName()).withStyle(ChatFormatting.GRAY));
                    end(level, state, raid);
                    changed = true;
                }
            }
        }
        if (changed) {
            save(level, state);
        }
    }

    /** Die geladenen, lebenden Krieger des Heerhaufens. */
    private static List<RealmWarriorEntity> present(ServerLevel level, Raid raid) {
        List<RealmWarriorEntity> present = new ArrayList<>();
        for (UUID id : raid.raiders) {
            if (!raid.fallen.contains(id) && level.getEntity(id) instanceof RealmWarriorEntity warrior && warrior.isAlive()) {
                present.add(warrior);
            }
        }
        return present;
    }

    /** Verteidigt jemand das Banner (der Spieler oder seine Soldaten, lebend und in der Nähe)? */
    private static boolean defended(ServerLevel level, Raid raid) {
        Vec3 banner = Vec3.atCenterOf(raid.banner);
        for (Player player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && player.distanceToSqr(banner) < DEFEND_RADIUS * DEFEND_RADIUS
                    && RealmState.of(level).atWar(raid.realm, player.getUUID())) {
                return true;
            }
        }
        return !level.getEntitiesOfClass(SoldierEntity.class, new net.minecraft.world.phys.AABB(raid.banner).inflate(DEFEND_RADIUS),
                s -> s.isAlive() && s.getOwnerId() != null && RealmState.of(level).atWar(raid.realm, s.getOwnerId())).isEmpty();
    }

    private static void updateBar(ServerLevel level, Raid raid) {
        ServerBossEvent bar = BARS.computeIfAbsent(raid.id, id -> {
            ServerBossEvent b = new ServerBossEvent(id, Component.empty(), color(raid.realm), BossEvent.BossBarOverlay.NOTCHED_10);
            b.setDarkenScreen(false);
            return b;
        });
        bar.setName(Component.translatable(raid.phase == Phase.RETREAT ? "raid.kingdomsatwar.bar.retreat" : "raid.kingdomsatwar.bar",
                raid.realm.shortName(), raid.alive(), raid.total()));
        bar.setProgress(raid.total() == 0 ? 0.0f : Mth.clamp((float) raid.alive() / raid.total(), 0.0f, 1.0f));
        Vec3 banner = Vec3.atCenterOf(raid.banner);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            boolean near = player.level() == level && player.distanceToSqr(banner) < BAR_RANGE * BAR_RANGE;
            if (near && !bar.getPlayers().contains(player)) {
                bar.addPlayer(player);
            } else if (!near && bar.getPlayers().contains(player)) {
                bar.removePlayer(player);
            }
        }
    }

    private static BossEvent.BossBarColor color(Realm realm) {
        return switch (realm) {
            case EISENMARK -> BossEvent.BossBarColor.PURPLE;
            case RABENSTEIN -> BossEvent.BossBarColor.RED;
            case DORNFELS -> BossEvent.BossBarColor.YELLOW;
            case SILBERAU -> BossEvent.BossBarColor.GREEN;
        };
    }

    /** Geschlagen: die Kriegskasse des Heerhaufens kommt ins Lager (was nicht passt, liegt am Banner). */
    private static void victory(ServerLevel level, Raid raid, @Nullable Town town) {
        int silver = 2 + raid.number * 2;
        List<ItemStack> loot = List.of(new ItemStack(Coin.SILVER.item(), Math.min(64, silver)), new ItemStack(Items.IRON_INGOT, 2 + raid.number),
                new ItemStack(Items.ARROW, 8 + 4 * raid.number));
        for (ItemStack stack : loot) {
            ItemStack rest = town != null ? TownStorage.store(level, town, stack, raid.banner) : stack;
            if (!rest.isEmpty()) {
                net.minecraft.world.entity.item.ItemEntity item = new net.minecraft.world.entity.item.ItemEntity(level, raid.banner.getX() + 0.5,
                        raid.banner.getY() + 1.0, raid.banner.getZ() + 0.5, rest);
                level.addFreshEntity(item);
            }
        }
        message(level, raid, Component.translatable("raid.kingdomsatwar.victory", raid.realm.shortName(), silver).withStyle(ChatFormatting.GREEN));
        level.playSound(null, raid.banner, SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 1.0f);
        KingdomsAtWar.LOGGER.info("Heerhaufen Nr. {} von {} geschlagen", raid.number, raid.realm.id());
    }

    /** Plündern: von jedem Stapel im Lager ein Teil. @return wie viele Gegenstände */
    static int plunder(ServerLevel level, Raid raid, Town town) {
        int taken = 0;
        for (BlockPos pos : TownStorage.containers(level, town)) {
            if (!(level.getBlockEntity(pos) instanceof Container container)) {
                continue;
            }
            for (int slot = 0; slot < container.getContainerSize(); slot++) {
                ItemStack stack = container.getItem(slot);
                if (stack.isEmpty()) {
                    continue;
                }
                int n = (int) Math.ceil(stack.getCount() * PLUNDER_SHARE);
                stack.shrink(n);
                taken += n;
            }
            container.setChanged();
        }
        message(level, raid, Component.translatable("raid.kingdomsatwar.plunder", raid.realm.shortName(), town.name(), taken)
                .withStyle(ChatFormatting.DARK_RED));
        level.playSound(null, raid.banner, SoundEvents.PILLAGER_CELEBRATE, SoundSource.HOSTILE, 2.0f, 0.8f);
        KingdomsAtWar.LOGGER.info("Heerhaufen Nr. {} von {} plündert {} ({} Gegenstände)", raid.number, raid.realm.id(), town.name(), taken);
        return taken;
    }

    private static void retreat(ServerLevel level, Raid raid, List<RealmWarriorEntity> present) {
        raid.phase = Phase.RETREAT;
        raid.retreatAt = level.getGameTime();
        for (RealmWarriorEntity warrior : present) {
            warrior.setRaidGoal(raid.muster);
            warrior.setTarget(null);
        }
        message(level, raid, Component.translatable("raid.kingdomsatwar.retreat", raid.realm.shortName()).withStyle(ChatFormatting.GOLD));
    }

    private static void end(ServerLevel level, State state, Raid raid) {
        state.raids.remove(raid);
        ServerBossEvent bar = BARS.remove(raid.id);
        if (bar != null) {
            bar.removeAllPlayers();
        }
    }

    private static void message(ServerLevel level, Raid raid, Component message) {
        Vec3 banner = Vec3.atCenterOf(raid.banner);
        for (ServerPlayer player : level.players()) {
            if (player.getUUID().equals(raid.player) || player.distanceToSqr(banner) < BAR_RANGE * BAR_RANGE) {
                player.sendSystemMessage(message);
            }
        }
    }

    /** Ein Krieger des Heerhaufens ist gefallen. */
    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof RealmWarriorEntity warrior) || warrior.raid() == null
                || !(warrior.level() instanceof ServerLevel level)) {
            return;
        }
        State state = state(level);
        for (Raid raid : state.raids) {
            if (raid.id.equals(warrior.raid())) {
                raid.fallen.add(warrior.getUUID());
                save(level, state);
                return;
            }
        }
    }

    /** Für Tests und Befehle: alles vergessen (die Krieger laufender Heerhaufen verschwinden). */
    public static void reset(ServerLevel level) {
        State state = state(level);
        for (Raid raid : List.copyOf(state.raids)) {
            for (RealmWarriorEntity warrior : present(level, raid)) {
                warrior.discard();
            }
            end(level, state, raid);
        }
        state.feuds.clear();
        save(level, state);
    }

    // =====================================================================================
    // Befehl
    // =====================================================================================

    /** {@code /kaw raid [Reich]}: sofort einen Heerhaufen gegen die eigene Siedlung (das Reich erklärt dafür den Krieg). */
    public static LiteralArgumentBuilder<CommandSourceStack> command() {
        return Commands.literal("raid").requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                .executes(context -> startCommand(context.getSource(), null))
                .then(Commands.literal("stop").executes(context -> {
                    reset(context.getSource().getServer().overworld());
                    context.getSource().sendSuccess(() -> Component.translatable("command.kingdomsatwar.raid.stopped"), true);
                    return 1;
                }))
                .then(Commands.argument("realm", com.mojang.brigadier.arguments.StringArgumentType.word())
                        .suggests((context, builder) -> net.minecraft.commands.SharedSuggestionProvider.suggest(
                                java.util.Arrays.stream(Realm.values()).map(Realm::id), builder))
                        .executes(context -> startCommand(context.getSource(),
                                Realm.byId(com.mojang.brigadier.arguments.StringArgumentType.getString(context, "realm")))));
    }

    private static int startCommand(CommandSourceStack source, @Nullable Realm chosen) throws com.mojang.brigadier.exceptions.CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = source.getServer().overworld();
        Town town = Towns.of(source.getServer()).byOwner(player.getUUID());
        if (town == null || !town.hasBanner() || town.dimension() != Level.OVERWORLD) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.raid.no_town"));
            return 0;
        }
        if (against(level, town) != null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.raid.running"));
            return 0;
        }
        RealmState realms = RealmState.of(level);
        Realm realm = chosen;
        if (realm == null) {
            for (Realm r : Realm.values()) {
                if (!realms.isConquered(r)) {
                    realm = r;
                    break;
                }
            }
        }
        if (realm == null || realms.isConquered(realm)) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.raid.conquered"));
            return 0;
        }
        realms.declareWar(realm, player.getUUID());
        realms.save(level);
        Raid raid = start(level, realm, player, town);
        if (raid == null) {
            source.sendFailure(Component.translatable("command.kingdomsatwar.raid.no_room"));
            return 0;
        }
        return raid.total();
    }

    /** Für die Vorführung: die Krieger eines Heerhaufens sofort ans Ziel setzen (Abstand {@code distance} vor dem Banner). */
    public static void devAdvance(ServerLevel level, Raid raid, double distance) {
        Vec3 banner = Vec3.atCenterOf(raid.banner);
        Vec3 dir = Vec3.atCenterOf(raid.muster).subtract(banner).multiply(1.0, 0.0, 1.0).normalize();
        int i = 0;
        for (RealmWarriorEntity warrior : present(level, raid)) {
            Vec3 at = banner.add(dir.scale(distance + (i % 3))).add(dir.z * ((i / 3) - 1) * 1.6, 0.0, -dir.x * ((i / 3) - 1) * 1.6);
            BlockPos ground = ground(level, BlockPos.containing(at));
            if (ground != null) {
                warrior.snapTo(at.x, ground.getY(), at.z, warrior.getYRot(), 0.0f);
            }
            i++;
        }
    }

    /** Für Tests: alle Heerhaufen gegen diesen Spieler beenden (ihre Krieger verschwinden) und seine Fehden vergessen. */
    public static void devForget(ServerLevel level, UUID player) {
        State state = state(level);
        for (Raid raid : List.copyOf(state.raids)) {
            if (raid.player.equals(player)) {
                for (RealmWarriorEntity warrior : present(level, raid)) {
                    warrior.discard();
                }
                end(level, state, raid);
            }
        }
        state.feuds.removeIf(f -> f.player.equals(player));
        save(level, state);
    }

    /** Für Tests: wann das Reich zuletzt einen Heerhaufen gegen den Spieler geschickt hat (Spielzeit). */
    public static void devFeud(ServerLevel level, Realm realm, UUID player, long last) {
        State state = state(level);
        feud(state, realm, player, last).last = last;
        save(level, state);
    }

    /** Für Tests: die Krieger, die gerade geladen sind und leben. */
    public static List<RealmWarriorEntity> devRaiders(ServerLevel level, Raid raid) {
        return present(level, raid);
    }

    static Entity anyRaider(ServerLevel level, Raid raid) {
        List<RealmWarriorEntity> present = present(level, raid);
        return present.isEmpty() ? null : present.getFirst();
    }
}
