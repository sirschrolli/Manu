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
        PLACED, NOT_OWNER, NO_TOWN, OUTSIDE, OVERLAP, UNEVEN, WATER, TIER, NOT_ENOUGH, LIMIT, UNKNOWN, FOREIGN;

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
    public static PlaceResult check(ServerLevel level, Town town, BuildingType type, int design, BlockPos origin, Rotation rotation) {
        if (town.tier().ordinal() < type.tier().ordinal()) {
            return PlaceResult.TIER;
        }
        return check(level, town, type.blueprint(design), origin, rotation);
    }

    /** Prüft einen Bauplatz für einen Bauplan (Gebiet, Überschneidungen, Gelände, Zahl der Baustellen). */
    public static PlaceResult check(ServerLevel level, Town town, Blueprint plan, BlockPos origin, Rotation rotation) {
        if (town.projects().size() >= 3) {
            return PlaceResult.LIMIT;
        }
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
        // große Bauten dürfen auf etwas unebenerem Gelände stehen (das Fundament gleicht es aus)
        int slack = 5 + Math.max(box.getXSpan(), box.getZSpan()) / 12;
        if (max - min > slack || origin.getY() - min > slack) {
            return PlaceResult.UNEVEN;
        }
        return PlaceResult.PLACED;
    }

    /**
     * Prüft einen Bauplatz für einen eigenen Plan, ohne den ganzen Plan aufzubauen. Ein Bau, der nie ganz ins Gebiet der
     * Siedlung passen würde (eine große Burg), darf darüber hinausreichen: er muss das Gebiet berühren – meist steht er
     * dann gleich neben der Siedlung –, darf aber wie jeder Bau weder Banner noch Häuser überbauen und kein Gebiet einer
     * anderen Siedlung berühren. Das Gelände wird an höchstens einigen tausend Säulen der Grundfläche geprüft (nur, wo
     * die Welt schon geladen ist).
     */
    public static PlaceResult check(ServerLevel level, Town town, io.github.sirschrolli.kingdomsatwar.architect.PlanInfo info, BlockPos origin,
            Rotation rotation) {
        if (town.projects().size() >= 3) {
            return PlaceResult.LIMIT;
        }
        BoundingBox box = info.worldBounds(origin, rotation);
        boolean inside = true;
        for (BlockPos corner : new BlockPos[] {new BlockPos(box.minX(), origin.getY(), box.minZ()),
                new BlockPos(box.maxX(), origin.getY(), box.maxZ()), new BlockPos(box.minX(), origin.getY(), box.maxZ()),
                new BlockPos(box.maxX(), origin.getY(), box.minZ())}) {
            inside &= town.contains(level.dimension(), corner);
        }
        if (!inside) {
            int span = Math.max(box.getXSpan(), box.getZSpan());
            if (span <= town.radius() * 1.4 || !level.dimension().equals(town.dimension()) || !touches(box, town.banner(), town.radius())) {
                return PlaceResult.OUTSIDE;
            }
            for (Town other : Towns.of(level.getServer()).all()) {
                if (other != town && other.dimension().equals(level.dimension()) && touches(box, other.banner(), other.radius())) {
                    return PlaceResult.FOREIGN;
                }
            }
        }
        if (town.overlaps(box)) {
            return PlaceResult.OVERLAP;
        }
        io.github.sirschrolli.kingdomsatwar.architect.PlanScan scan = io.github.sirschrolli.kingdomsatwar.architect.PlanLibrary.scan(info.id());
        if (scan == null) {
            return PlaceResult.UNKNOWN;
        }
        // Gelände: nicht zu steil, nicht im Wasser
        int stride = Math.max(1, (int) Math.sqrt(scan.sizeX * (double) scan.sizeZ / 4096.0));
        int ox = (scan.sizeX - 1) / 2;
        int oz = (scan.sizeZ - 1) / 2;
        int min = Integer.MAX_VALUE;
        int max = Integer.MIN_VALUE;
        int columns = 0;
        int water = 0;
        for (int gz = 0; gz < scan.sizeZ; gz += stride) {
            for (int gx = 0; gx < scan.sizeX; gx += stride) {
                if (!scan.footprintColumn(gx, gz)) {
                    continue;
                }
                BlockPos w = Blueprint.world(origin, rotation, new BlockPos(gx - ox, 0, gz - oz));
                if (!level.hasChunkAt(w)) {
                    continue;
                }
                int ground = level.getHeight(Heightmap.Types.OCEAN_FLOOR, w.getX(), w.getZ());
                int surface = level.getHeight(Heightmap.Types.WORLD_SURFACE, w.getX(), w.getZ());
                columns++;
                if (surface > ground + 1) {
                    water++;
                }
                min = Math.min(min, ground);
                max = Math.max(max, ground);
            }
        }
        if (columns > 0 && water > columns / 6) {
            return PlaceResult.WATER;
        }
        int slack = 5 + Math.max(box.getXSpan(), box.getZSpan()) / 12;
        if (columns > 0 && (max - min > slack || origin.getY() - min > slack)) {
            return PlaceResult.UNEVEN;
        }
        return PlaceResult.PLACED;
    }

    /** Berührt ein Bereich den Kreis um einen Punkt? */
    private static boolean touches(BoundingBox box, BlockPos center, int radius) {
        long dx = center.getX() - Math.max(box.minX(), Math.min(center.getX(), box.maxX()));
        long dz = center.getZ() - Math.max(box.minZ(), Math.min(center.getZ(), box.maxZ()));
        return dx * dx + dz * dz <= (long) radius * radius;
    }

    /** Richtet eine Baustelle ein (bezahlt den Bau; erster Entwurf); der Baumeister beginnt dann zu bauen. */
    public static PlaceResult placeProject(ServerPlayer player, BuildingType type, BlockPos origin, Rotation rotation) {
        return placeProject(player, type, 0, origin, rotation);
    }

    /** Richtet eine Baustelle für einen Entwurf ein (bezahlt den Bau); der Baumeister beginnt dann zu bauen. */
    public static PlaceResult placeProject(ServerPlayer player, BuildingType type, int design, BlockPos origin, Rotation rotation) {
        ServerLevel level = player.level();
        Towns towns = Towns.of(level.getServer());
        Town town = towns.byOwner(player.getUUID());
        if (town == null) {
            return PlaceResult.NO_TOWN;
        }
        if (!town.dimension().equals(level.dimension())) {
            return PlaceResult.OUTSIDE;
        }
        PlaceResult result = check(level, town, type, design, origin, rotation);
        if (result != PlaceResult.PLACED) {
            return result;
        }
        int price = buildPrice(town, type, player);
        if (!Coins.pay(player, price)) {
            return PlaceResult.NOT_ENOUGH;
        }
        town.addProject(new Town.Project(type, origin, rotation, 0, design));
        towns.save(level.getServer());
        level.playSound(null, origin, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        boolean builder = town.count(Profession.BUILDER) > 0;
        player.sendSystemMessage(Component.translatable(builder ? "message.kingdomsatwar.town.project_started"
                : "message.kingdomsatwar.town.project_no_builder", Component.translatable(type.translationKey())).withStyle(
                builder ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        if (type.costsMaterials()) {
            // was der Bau an Baustoffen braucht und was davon im Lager liegt
            int[] costs = type.costs(design);
            int[] stock = TownStorage.counts(level, town);
            Component[] parts = new Component[Material.values().length];
            for (Material m : Material.values()) {
                parts[m.ordinal()] = Component.translatable("message.kingdomsatwar.town.material", costs[m.ordinal()],
                        Component.translatable(m.translationKey()), stock[m.ordinal()])
                        .withStyle(stock[m.ordinal()] >= costs[m.ordinal()] ? ChatFormatting.GRAY : ChatFormatting.RED);
            }
            player.sendSystemMessage(Component.translatable(TownStorage.exists(level, town) ? "message.kingdomsatwar.town.materials"
                    : "message.kingdomsatwar.town.materials_no_storage", parts[0], parts[1], parts[2]).withStyle(ChatFormatting.GRAY));
        }
        return PlaceResult.PLACED;
    }

    /** Preis eines Baus nach eigenem Plan (nach der Krönung baut der König umsonst). */
    public static int buildPrice(Town town, io.github.sirschrolli.kingdomsatwar.architect.PlanInfo info, Player player) {
        if (town.crowned() && town.owner().equals(player.getUUID())) {
            return 0;
        }
        return Reputation.discounted(info.price(), Reputation.rank(player));
    }

    /** Der Name einer Baustelle: der Name des eigenen Plans oder der Gebäudeart. */
    public static Component name(Town.Project project) {
        io.github.sirschrolli.kingdomsatwar.architect.PlanInfo info = project.info();
        return info != null ? Component.literal(info.name()) : Component.translatable(project.type.translationKey());
    }

    /**
     * Richtet eine Baustelle nach einem eigenen Bauplan ein (Dragon Architect; bezahlt den Bau). Der Baumeister holt die
     * Baustoffe und kostbaren Blöcke aus dem Lager.
     */
    public static PlaceResult placeCustom(ServerPlayer player, io.github.sirschrolli.kingdomsatwar.architect.PlanInfo info, BlockPos origin,
            Rotation rotation) {
        ServerLevel level = player.level();
        Towns towns = Towns.of(level.getServer());
        Town town = towns.byOwner(player.getUUID());
        if (town == null) {
            return PlaceResult.NO_TOWN;
        }
        if (!town.dimension().equals(level.dimension())) {
            return PlaceResult.OUTSIDE;
        }
        if (info.costs().blocks() <= 0 || io.github.sirschrolli.kingdomsatwar.architect.PlanLibrary.scan(info.id()) == null) {
            return PlaceResult.UNKNOWN;
        }
        PlaceResult result = check(level, town, info, origin, rotation);
        if (result != PlaceResult.PLACED) {
            return result;
        }
        int price = buildPrice(town, info, player);
        if (!Coins.pay(player, price)) {
            return PlaceResult.NOT_ENOUGH;
        }
        Town.Project project = new Town.Project(BuildingType.CUSTOM, origin, rotation, 0, 0, info.id());
        // große Bauten gleich abschnittsweise
        project.fast = info.costs().blocks() >= FAST_FROM || SiteSections.large(info);
        town.addProject(project);
        towns.save(level.getServer());
        level.playSound(null, origin, SoundEvents.WOOD_PLACE, SoundSource.BLOCKS, 1.0f, 0.8f);
        boolean builder = town.count(Profession.BUILDER) > 0;
        player.sendSystemMessage(Component.translatable(builder ? "message.kingdomsatwar.town.project_started"
                : "message.kingdomsatwar.town.project_no_builder", Component.literal(info.name())).withStyle(
                builder ? ChatFormatting.GREEN : ChatFormatting.GOLD));
        int[] stock = TownStorage.counts(level, town);
        Component[] parts = new Component[Material.values().length];
        for (Material m : Material.values()) {
            int need = info.costs().units(m);
            parts[m.ordinal()] = Component.translatable("message.kingdomsatwar.town.material", need, Component.translatable(m.translationKey()),
                    stock[m.ordinal()]).withStyle(stock[m.ordinal()] >= need ? ChatFormatting.GRAY : ChatFormatting.RED);
        }
        player.sendSystemMessage(Component.translatable(TownStorage.exists(level, town) ? "message.kingdomsatwar.town.materials"
                : "message.kingdomsatwar.town.materials_no_storage", parts[0], parts[1], parts[2]).withStyle(ChatFormatting.GRAY));
        for (io.github.sirschrolli.kingdomsatwar.architect.PlanInfo.Precious p : info.costs().precious()) {
            int have = TownStorage.count(level, town, p.item());
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.precious", p.count(), new net.minecraft.world.item.ItemStack(p.item()).getHoverName(), have)
                    .withStyle(have >= p.count() ? ChatFormatting.GRAY : ChatFormatting.RED));
        }
        return PlaceResult.PLACED;
    }

    /** Ab so vielen Blöcken baut der Baumeister einen eigenen Plan von Anfang an abschnittsweise. */
    public static final int FAST_FROM = 1500;

    /** Schaltet die Bauweise einer Baustelle um (Block für Block ↔ abschnittsweise). */
    public static void toggleFast(ServerPlayer player, Town town, int index) {
        if (!town.owner().equals(player.getUUID()) || index < 0 || index >= town.projects().size()) {
            return;
        }
        Town.Project project = town.projects().get(index);
        project.fast = !project.fast;
        Towns.of(player.level().getServer()).save(player.level().getServer());
    }

    /** Eine Baustelle ist fertig: wird zum Gebäude, Betten und Werkstätten werden verteilt. */
    public static void completeProject(ServerLevel level, Town town, Town.Project project) {
        if (!town.projects().contains(project)) {
            return;
        }
        town.removeProject(project);
        clearScaffolds(level, project.bounds());
        town.addBuilding(new Town.Building(project.type, project.origin, project.rotation, project.design,
                java.util.Optional.ofNullable(project.custom)));
        assignHomes(level, town);
        Towns.of(level.getServer()).save(level.getServer());
        BlockPos door = Blueprint.world(project.origin, project.rotation, project.entranceLocal());
        celebrate(level, door);
        if (project.type == BuildingType.BARRACKS) {
            // die Garnison zieht vor die neue Kaserne
            for (io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity soldier : level.getEntitiesOfClass(
                    io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity.class, new net.minecraft.world.phys.AABB(town.banner())
                            .inflate(town.radius() + 32.0), s -> town.owner().equals(s.getOwnerId()) && !s.isOnCampaign())) {
                io.github.sirschrolli.kingdomsatwar.army.Garrison.station(soldier);
            }
        }
        ServerPlayer owner = level.getServer().getPlayerList().getPlayer(town.owner());
        if (owner != null) {
            owner.sendSystemMessage(Component.translatable("message.kingdomsatwar.town.project_done",
                    name(project), Component.literal(town.name())).withStyle(ChatFormatting.GREEN));
            Reputation.add(owner, Reputation.BUILDING);
        }
    }

    /** Richtet eine Baustelle ohne Preis und ohne Prüfung der Stufe ein (Befehle, Tests). */
    public static Town.Project startProject(ServerLevel level, Town town, BuildingType type, BlockPos origin, Rotation rotation) {
        return startProject(level, town, type, 0, origin, rotation);
    }

    /** Richtet eine Baustelle nach einem eigenen Plan ein, ohne Preis und ohne Prüfung (Befehle, Tests, Vorführungen). */
    public static Town.Project startCustom(ServerLevel level, Town town, io.github.sirschrolli.kingdomsatwar.architect.PlanInfo info,
            BlockPos origin, Rotation rotation) {
        Town.Project project = new Town.Project(BuildingType.CUSTOM, origin, rotation, 0, 0, info.id());
        project.fast = info.costs().blocks() >= FAST_FROM || SiteSections.large(info);
        town.addProject(project);
        Towns.of(level.getServer()).save(level.getServer());
        return project;
    }

    /** Wie {@link #startProject(ServerLevel, Town, BuildingType, BlockPos, Rotation)}, für einen bestimmten Entwurf. */
    public static Town.Project startProject(ServerLevel level, Town town, BuildingType type, int design, BlockPos origin, Rotation rotation) {
        Town.Project project = new Town.Project(type, origin, rotation, 0, design);
        town.addProject(project);
        Towns.of(level.getServer()).save(level.getServer());
        return project;
    }

    /**
     * Räumt die Baugerüste rund um eine Baustelle weg (fertig gebaut oder abgebrochen). Auch bei einer ganzen Burg schnell:
     * Lagen der Chunks, in denen kein Gerüst vorkommt, werden übersprungen.
     */
    public static void clearScaffolds(ServerLevel level, net.minecraft.world.level.levelgen.structure.BoundingBox bounds) {
        var box = bounds.inflatedBy(6, 0, 6);
        net.minecraft.world.level.block.state.BlockState scaffold =
                io.github.sirschrolli.kingdomsatwar.registry.ModBlocks.BUILDER_SCAFFOLD.get().defaultBlockState();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int cx = box.minX() >> 4; cx <= box.maxX() >> 4; cx++) {
            for (int cz = box.minZ() >> 4; cz <= box.maxZ() >> 4; cz++) {
                if (!level.hasChunk(cx, cz)) {
                    continue;
                }
                net.minecraft.world.level.chunk.LevelChunk chunk = level.getChunk(cx, cz);
                for (int y = box.maxY() + 6; y >= box.minY() - 2; y--) {
                    int si = chunk.getSectionIndex(y);
                    if (si < 0 || si >= chunk.getSectionsCount() || !chunk.getSection(si).maybeHas(st -> st.is(scaffold.getBlock()))) {
                        y &= ~15; // weiter mit der Lage darunter
                        continue;
                    }
                    for (int z = Math.max(box.minZ(), cz << 4); z <= Math.min(box.maxZ(), (cz << 4) + 15); z++) {
                        for (int x = Math.max(box.minX(), cx << 4); x <= Math.min(box.maxX(), (cx << 4) + 15); x++) {
                            pos.set(x, y, z);
                            if (level.getBlockState(pos).is(scaffold.getBlock())) {
                                level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                                        net.minecraft.world.level.block.Block.UPDATE_ALL);
                            }
                        }
                    }
                }
            }
        }
    }

    /** Baut einen Plan sofort (Kreativmodus, Befehle, Tests). */
    public static void buildInstantly(ServerLevel level, Town town, BuildingType type, BlockPos origin, Rotation rotation) {
        buildInstantly(level, town, type, 0, origin, rotation);
    }

    /** Baut einen Entwurf sofort. */
    public static void buildInstantly(ServerLevel level, Town town, BuildingType type, int design, BlockPos origin, Rotation rotation) {
        type.blueprint(design).placeAll(level, origin, rotation);
        town.addBuilding(new Town.Building(type, origin, rotation, design));
        assignHomes(level, town);
        Towns.of(level.getServer()).save(level.getServer());
    }

    public static void cancelProject(ServerPlayer player, Town town, int index) {
        if (!town.owner().equals(player.getUUID()) || index < 0 || index >= town.projects().size()) {
            return;
        }
        Town.Project project = town.projects().get(index);
        town.removeProject(project);
        clearScaffolds(player.level(), project.bounds());
        // halbe Rückzahlung, wenn noch nichts gebaut ist sogar ganz (vom Preis, den der Spieler heute zahlen würde)
        io.github.sirschrolli.kingdomsatwar.architect.PlanInfo info = project.info();
        int price = info != null ? buildPrice(town, info, player) : project.custom != null ? 0 : buildPrice(town, project.type, player);
        int refund = !project.started() ? price : price / 2;
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
