package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.economy.Coins;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Alles, was mit einer Siedlung geschieht: gründen (Stadtbanner setzen), Bürger anheuern und entlassen, Baustellen
 * einrichten und fertigstellen, Betten und Werkstätten verteilen. Läuft nur auf dem Server.
 */
public final class TownManager {
    /** Wie weit Siedlungen verschiedener Spieler mindestens auseinanderliegen (zusätzlich zum Radius). */
    public static final int TOWN_SPACING = 48;

    private TownManager() {}

    // =====================================================================================
    // Gründen
    // =====================================================================================

    public enum FoundResult {
        FOUNDED, MOVED, HAS_BANNER, TOO_CLOSE;

        public String key() {
            return "message.kingdomsatwar.town.found." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** Ein Spieler setzt ein Stadtbanner: gründet seine Siedlung oder versetzt das Banner seiner Siedlung hierher. */
    public static FoundResult placeBanner(ServerPlayer player, ServerLevel level, BlockPos pos) {
        MinecraftServer server = level.getServer();
        Towns towns = Towns.of(server);
        Town own = towns.byOwner(player.getUUID());
        if (own != null && own.hasBanner() && !own.banner().equals(pos)) {
            return FoundResult.HAS_BANNER;
        }
        for (Town other : towns.all()) {
            if (other != own && other.dimension().equals(level.dimension())) {
                double min = other.radius() + TOWN_SPACING;
                if (other.banner().distSqr(pos) < min * min) {
                    return FoundResult.TOO_CLOSE;
                }
            }
        }
        if (own != null) {
            own.moveBanner(pos);
            own.setOwnerName(player.getName().getString());
            towns.save(server);
            return FoundResult.MOVED;
        }
        Town town = Town.found(player.getUUID(), player.getName().getString(), TownNames.town(level.getRandom()), level.dimension(),
                pos, level.getGameTime());
        towns.add(town);
        towns.save(server);
        celebrate(level, pos);
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.founded", Component.literal(town.name())
                .withStyle(ChatFormatting.GOLD)).withStyle(ChatFormatting.YELLOW));
        io.github.sirschrolli.kingdomsatwar.story.Story.onTownFounded(player);
        return FoundResult.FOUNDED;
    }

    /** Das Banner wurde abgebaut: die Siedlung bleibt, bis ein neues Banner steht. */
    public static void bannerRemoved(ServerLevel level, BlockPos pos) {
        Towns towns = Towns.of(level.getServer());
        Town town = towns.byBanner(level.dimension(), pos);
        if (town != null) {
            town.removeBanner();
            towns.save(level.getServer());
            ServerPlayer owner = level.getServer().getPlayerList().getPlayer(town.owner());
            if (owner != null) {
                owner.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.banner_removed", Component.literal(town.name()))
                        .withStyle(ChatFormatting.GOLD));
            }
        }
    }

    public static void rename(ServerPlayer player, Town town, String name) {
        String clean = name.strip();
        if (!town.owner().equals(player.getUUID()) || clean.isEmpty() || clean.length() > 32) {
            return;
        }
        town.setName(clean);
        Towns.of(player.level().getServer()).save(player.level().getServer());
    }

    public static void setQueen(ServerPlayer player, Town town, boolean queen) {
        if (town.owner().equals(player.getUUID())) {
            town.setQueen(queen);
            Towns.of(player.level().getServer()).save(player.level().getServer());
        }
    }

    // =====================================================================================
    // Bürger
    // =====================================================================================

    public enum HireResult {
        HIRED, NOT_OWNER, NO_BANNER, NO_WORKPLACE, NO_BED, NOT_ENOUGH, NO_SPACE;

        public String key() {
            return "message.kingdomsatwar.town.hire." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** Lohn beim Anheuern (nach der Krönung zahlt der König nichts mehr). */
    public static int hirePrice(Town town, Profession profession, Player player) {
        if (town.crowned() && town.owner().equals(player.getUUID())) {
            return 0;
        }
        return Reputation.discounted(profession.price(), Reputation.rank(player));
    }

    /** Hat die Siedlung eine freie Werkstatt für diesen Beruf? */
    public static Town.@Nullable Building freeWorkplace(Town town, Profession profession) {
        BuildingType type = profession.workplace();
        if (type == null) {
            return null;
        }
        for (Town.Building b : town.buildings()) {
            if (b.type() == type && town.workers(b.origin()) < type.jobs()) {
                return b;
            }
        }
        return null;
    }

    public static boolean hasWorkplace(Town town, Profession profession) {
        if (profession.workplace() == null) {
            return town.count(profession) < town.jobs(profession);
        }
        return freeWorkplace(town, profession) != null;
    }

    public static HireResult hire(ServerPlayer player, Town town, Profession profession) {
        if (!town.owner().equals(player.getUUID())) {
            return HireResult.NOT_OWNER;
        }
        if (!town.hasBanner()) {
            return HireResult.NO_BANNER;
        }
        if (!hasWorkplace(town, profession)) {
            return HireResult.NO_WORKPLACE;
        }
        if (town.freeBeds() <= 0) {
            return HireResult.NO_BED;
        }
        int price = hirePrice(town, profession, player);
        if (!Coins.canAfford(player, price)) {
            return HireResult.NOT_ENOUGH;
        }
        ServerLevel level = player.level().getServer().getLevel(town.dimension());
        if (level == null) {
            return HireResult.NO_SPACE;
        }
        CitizenEntity citizen = spawnCitizen(level, town, profession);
        if (citizen == null) {
            return HireResult.NO_SPACE;
        }
        Coins.pay(player, price);
        Reputation.add(player, Reputation.HIRE);
        player.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.hired", citizen.getName(),
                Component.translatable(profession.translationKey())).withStyle(ChatFormatting.GREEN));
        io.github.sirschrolli.kingdomsatwar.story.Story.onRecruited(player);
        return HireResult.HIRED;
    }

    /** Lässt einen neuen Bürger am Banner erscheinen und trägt ihn in die Siedlung ein (mit Bett und Werkstatt). */
    public static @Nullable CitizenEntity spawnCitizen(ServerLevel level, Town town, Profession profession) {
        CitizenEntity citizen = ModEntities.CITIZEN.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (citizen == null) {
            return null;
        }
        Vec3 spot = spotNear(level, town.banner());
        citizen.snapTo(spot.x, spot.y, spot.z, level.getRandom().nextFloat() * 360.0f, 0.0f);
        citizen.setProfession(profession);
        citizen.setVariant(level.getRandom().nextInt(profession.variants()));
        String name = TownNames.citizen(level.getRandom(), citizen.isFeminine());
        citizen.setCustomName(Component.literal(name));
        Town.Building work = freeWorkplace(town, profession);
        Town.Resident resident = new Town.Resident(citizen.getUUID(), profession, name, citizen.getVariant(), Optional.empty(),
                Optional.ofNullable(work == null ? null : work.origin()));
        town.addResident(resident);
        citizen.assign(town.id(), null, resident.work().orElse(null));
        level.addFreshEntity(citizen);
        assignHomes(level, town);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, spot.x, spot.y + 1.0, spot.z, 14, 0.4, 0.6, 0.4, 0.0);
        level.playSound(null, spot.x, spot.y, spot.z, SoundEvents.VILLAGER_CELEBRATE, SoundSource.NEUTRAL, 0.8f, 1.0f);
        Towns.of(level.getServer()).save(level.getServer());
        return citizen;
    }

    public static void dismiss(ServerPlayer player, Town town, java.util.UUID entity) {
        if (!town.owner().equals(player.getUUID()) || town.resident(entity) == null) {
            return;
        }
        town.removeResident(entity);
        ServerLevel level = player.level().getServer().getLevel(town.dimension());
        if (level != null && level.getEntity(entity) instanceof CitizenEntity citizen) {
            level.sendParticles(ParticleTypes.POOF, citizen.getX(), citizen.getY() + 1.0, citizen.getZ(), 10, 0.3, 0.5, 0.3, 0.02);
            citizen.assign(town.id(), null, null);
            citizen.discard();
        }
        Towns.of(player.level().getServer()).save(player.level().getServer());
    }

    /** Ein Bürger ist gestorben (oder wurde entfernt): aus der Siedlung austragen und dem Besitzer melden. */
    public static void onCitizenDied(ServerLevel level, CitizenEntity citizen, @Nullable DamageSource source) {
        Towns towns = Towns.of(level.getServer());
        Town town = citizen.getTownId() == null ? null : towns.byId(citizen.getTownId());
        if (town == null || !town.removeResident(citizen.getUUID())) {
            return;
        }
        towns.save(level.getServer());
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(town.owner());
        if (owner != null && source != null) {
            owner.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.citizen_died", citizen.getName(),
                    Component.translatable(citizen.getProfession().translationKey()), Component.literal(town.name())).withStyle(ChatFormatting.RED));
            if (source.getEntity() == owner) {
                Reputation.add(owner, -60);
            }
        }
    }

    /** Freie Betten an Bürger ohne Bett verteilen (und den Bürgern ihr Bett mitteilen). */
    public static void assignHomes(ServerLevel level, Town town) {
        List<BlockPos> free = new ArrayList<>();
        for (BlockPos bed : town.beds()) {
            if (!town.bedTaken(bed)) {
                free.add(bed);
            }
        }
        for (Town.Resident r : town.residents()) {
            Town.Resident updated = r;
            if (r.bed().isEmpty() && !free.isEmpty()) {
                updated = r.withBed(free.removeFirst());
            }
            if (r.work().isEmpty() && r.profession().workplace() != null) {
                Town.Building work = freeWorkplace(town, r.profession());
                if (work != null) {
                    updated = updated.withWork(work.origin());
                }
            }
            if (updated != r) {
                town.updateResident(updated);
            }
            if (level.getEntity(updated.entity()) instanceof CitizenEntity citizen) {
                citizen.assign(town.id(), updated.bed().orElse(null), updated.work().orElse(null));
            }
        }
    }

    // =====================================================================================
    // Bauen
    // =====================================================================================

    public enum PlaceResult {
        PLACED, NOT_OWNER, NO_TOWN, OUTSIDE, OVERLAP, UNEVEN, WATER, TIER, NOT_ENOUGH, LIMIT;

        public String key() {
            return "message.kingdomsatwar.town.place." + this.name().toLowerCase(Locale.ROOT);
        }
    }

    /** Preis eines Baus (nach der Krönung baut der König umsonst). */
    public static int buildPrice(Town town, BuildingType type, Player player) {
        if (town.crowned() && town.owner().equals(player.getUUID())) {
            return 0;
        }
        return Reputation.discounted(type.price(), Reputation.rank(player));
    }

    /** Prüft einen Bauplatz (ohne zu bezahlen); {@code PLACED} heißt: hier darf gebaut werden. */
    public static PlaceResult check(ServerLevel level, Town town, BuildingType type, BlockPos origin, Rotation rotation) {
        if (town.tier().ordinal() < type.tier().ordinal()) {
            return PlaceResult.TIER;
        }
        if (town.projects().size() >= 3) {
            return PlaceResult.LIMIT;
        }
        Blueprint plan = type.blueprint();
        BoundingBox box = plan.worldBounds(origin, rotation);
        for (BlockPos corner : new BlockPos[] {new BlockPos(box.minX(), origin.getY(), box.minZ()),
                new BlockPos(box.maxX(), origin.getY(), box.maxZ()), new BlockPos(box.minX(), origin.getY(), box.maxZ()),
                new BlockPos(box.maxX(), origin.getY(), box.minZ())}) {
            if (!town.contains(level.dimension(), corner)) {
                return PlaceResult.OUTSIDE;
            }
        }
        if (town.overlaps(box)) {
            return PlaceResult.OVERLAP;
        }
        // Gelände: nicht zu steil, nicht im Wasser
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int water = 0;
        for (BlockPos column : plan.footprint()) {
            BlockPos w = Blueprint.world(origin, rotation, column);
            int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, w.getX(), w.getZ());
            int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, w.getX(), w.getZ());
            if (surface > ground + 1) {
                water++;
            }
            min = Math.min(min, ground);
            max = Math.max(max, ground);
        }
        if (water > plan.footprint().size() / 6) {
            return PlaceResult.WATER;
        }
        if (max - min > 5 || origin.getY() - min > 5) {
            return PlaceResult.UNEVEN;
        }
        return PlaceResult.PLACED;
    }

    /** Richtet eine Baustelle ein (bezahlt den Bau); der Baumeister beginnt dann zu bauen. */
    public static PlaceResult placeProject(ServerPlayer player, BuildingType type, BlockPos origin, Rotation rotation) {
        ServerLevel level = player.level();
        Towns towns = Towns.of(level.getServer());
        Town town = towns.byOwner(player.getUUID());
        if (town == null) {
            return PlaceResult.NO_TOWN;
        }
        if (!town.dimension().equals(level.dimension())) {
            return PlaceResult.OUTSIDE;
        }
        PlaceResult result = check(level, town, type, origin, rotation);
        if (result != PlaceResult.PLACED) {
            return result;
        }
        int price = buildPrice(town, type, player);
        if (!Coins.pay(player, price)) {
            return PlaceResult.NOT_ENOUGH;
        }
        town.addProject(new Town.Project(type, origin, rotation, 0));
        towns.save(level.getServer());
        level.playSound(null, origin, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        boolean builder = town.count(Profession.BUILDER) > 0;
        player.sendSystemMessage(Component.translatable(builder ? "message.kingdomsatwar.town.project_started"
                : "message.kingdomsatwar.town.project_no_builder", Component.translatable(type.translationKey())).withStyle(
                builder ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        return PlaceResult.PLACED;
    }

    /** Eine Baustelle ist fertig: wird zum Gebäude, Betten und Werkstätten werden verteilt. */
    public static void completeProject(ServerLevel level, Town town, Town.Project project) {
        if (!town.projects().contains(project)) {
            return;
        }
        town.removeProject(project);
        town.addBuilding(new Town.Building(project.type, project.origin, project.rotation));
        assignHomes(level, town);
        Towns.of(level.getServer()).save(level.getServer());
        BlockPos door = Blueprint.world(project.origin, project.rotation, project.type.blueprint().entrance());
        celebrate(level, door);
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(town.owner());
        if (owner != null) {
            owner.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.project_done",
                    Component.translatable(project.type.translationKey()), Component.literal(town.name())).withStyle(ChatFormatting.GREEN));
            Reputation.add(owner, Reputation.BUILDING);
        }
    }

    /** Baut einen Plan sofort (Kreativmodus, Befehle, Tests). */
    public static void buildInstantly(ServerLevel level, Town town, BuildingType type, BlockPos origin, Rotation rotation) {
        type.blueprint().placeAll(level, origin, rotation);
        town.addBuilding(new Town.Building(type, origin, rotation));
        assignHomes(level, town);
        Towns.of(level.getServer()).save(level.getServer());
    }

    public static void cancelProject(ServerPlayer player, Town town, int index) {
        if (!town.owner().equals(player.getUUID()) || index < 0 || index >= town.projects().size()) {
            return;
        }
        Town.Project project = town.projects().get(index);
        town.removeProject(project);
        // halbe Rückzahlung, wenn noch nichts gebaut ist sogar ganz (vom Preis, den der Spieler heute zahlen würde)
        int price = buildPrice(town, project.type, player);
        int refund = project.progress == 0 ? price : price / 2;
        Coins.give(player, refund);
        Towns.of(player.level().getServer()).save(player.level().getServer());
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static void celebrate(ServerLevel level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BELL_BLOCK, SoundSource.BLOCKS, 1.0f, 1.0f);
        level.sendParticles(ParticleTypes.HAPPY_VILLAGER, pos.getX() + 0.5, pos.getY() + 1.5, pos.getZ() + 0.5, 24, 1.2, 1.0, 1.2, 0.0);
        level.sendParticles(ParticleTypes.FIREWORK, pos.getX() + 0.5, pos.getY() + 2.5, pos.getZ() + 0.5, 30, 0.6, 0.8, 0.6, 0.08);
    }

    /** Ein freier Platz neben dem Banner. */
    private static Vec3 spotNear(ServerLevel level, BlockPos banner) {
        for (int r = 1; r <= 4; r++) {
            for (Direction d : Direction.Plane.HORIZONTAL) {
                for (int dy : new int[] {0, 1, -1}) {
                    BlockPos p = banner.relative(d, r).above(dy);
                    if (level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)
                            && level.getBlockState(p).getCollisionShape(level, p).isEmpty()
                            && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()) {
                        return Vec3.atBottomCenterOf(p);
                    }
                }
            }
        }
        return Vec3.atBottomCenterOf(banner.above());
    }

    /** Löst eine Siedlung auf: die Bürger gehen, die Gebäude bleiben stehen (für Befehle und Tests). */
    public static void disband(MinecraftServer server, Town town) {
        ServerLevel level = server.getLevel(town.dimension());
        Towns towns = Towns.of(server);
        towns.remove(town);
        if (level != null) {
            for (Town.Resident r : town.residents()) {
                if (level.getEntity(r.entity()) instanceof CitizenEntity citizen) {
                    citizen.discard();
                }
            }
        }
        towns.save(server);
    }

    /** Für Befehle und Tests: das Lebewesen eines Bürgers (wenn geladen). */
    public static @Nullable CitizenEntity citizen(ServerLevel level, java.util.UUID id) {
        Entity e = level.getEntity(id);
        return e instanceof CitizenEntity c ? c : null;
    }
}
