package io.github.sirschrolli.kingdomsatwar.economy.town;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import org.jspecify.annotations.Nullable;

/**
 * Eine Siedlung: gegründet mit dem Stadtbanner, gehört einem Spieler, hat einen Namen, Bewohner, fertige Gebäude und
 * Baustellen. Die Stufe ergibt sich aus der Zahl der Bewohner ({@link TownTier}).
 */
public final class Town {
    /**
     * Ein angeheuerter Bürger: sein Lebewesen, Beruf, Name, sein Bett (Kopfende, in der Welt) und seine Werkstatt
     * (Ursprung des Gebäudes, in dem er arbeitet).
     */
    public record Resident(UUID entity, Profession profession, String name, int variant, Optional<BlockPos> bed, Optional<BlockPos> work) {
        public static final Codec<Resident> CODEC = RecordCodecBuilder.create(i -> i.group(
                UUIDUtil.CODEC.fieldOf("entity").forGetter(Resident::entity),
                Profession.CODEC.fieldOf("profession").forGetter(Resident::profession),
                Codec.STRING.fieldOf("name").forGetter(Resident::name),
                Codec.INT.optionalFieldOf("variant", 0).forGetter(Resident::variant),
                BlockPos.CODEC.optionalFieldOf("bed").forGetter(Resident::bed),
                BlockPos.CODEC.optionalFieldOf("work").forGetter(Resident::work)
        ).apply(i, Resident::new));

        Resident withBed(@Nullable BlockPos bed) {
            return new Resident(this.entity, this.profession, this.name, this.variant, Optional.ofNullable(bed), this.work);
        }

        Resident withWork(@Nullable BlockPos work) {
            return new Resident(this.entity, this.profession, this.name, this.variant, this.bed, Optional.ofNullable(work));
        }
    }

    /** Ein fertiges Gebäude (Ursprung und Drehung des Bauplans in der Welt). */
    public record Building(BuildingType type, BlockPos origin, Rotation rotation) {
        public static final Codec<Building> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuildingType.CODEC.fieldOf("type").forGetter(Building::type),
                BlockPos.CODEC.fieldOf("origin").forGetter(Building::origin),
                Rotation.CODEC.fieldOf("rotation").forGetter(Building::rotation)
        ).apply(i, Building::new));

        public BlockPos world(BlockPos local) {
            return Blueprint.world(this.origin, this.rotation, local);
        }

        public BoundingBox bounds() {
            return this.type.blueprint().worldBounds(this.origin, this.rotation);
        }
    }

    /** Eine Baustelle: was gebaut wird, wo, und wie weit der Baumeister schon ist (Index im Bauablauf). */
    public static final class Project {
        public static final Codec<Project> CODEC = RecordCodecBuilder.create(i -> i.group(
                BuildingType.CODEC.fieldOf("type").forGetter(p -> p.type),
                BlockPos.CODEC.fieldOf("origin").forGetter(p -> p.origin),
                Rotation.CODEC.fieldOf("rotation").forGetter(p -> p.rotation),
                Codec.INT.optionalFieldOf("progress", 0).forGetter(p -> p.progress)
        ).apply(i, Project::new));

        public final BuildingType type;
        public final BlockPos origin;
        public final Rotation rotation;
        public int progress;
        /** Bauablauf (wird beim ersten Gebrauch berechnet, nicht gespeichert). */
        private @Nullable List<Blueprint.Step> steps;

        public Project(BuildingType type, BlockPos origin, Rotation rotation, int progress) {
            this.type = type;
            this.origin = origin;
            this.rotation = rotation;
            this.progress = progress;
        }

        public List<Blueprint.Step> steps() {
            if (this.steps == null) {
                this.steps = this.type.blueprint().steps(this.origin, this.rotation);
            }
            return this.steps;
        }

        /** Wie weit der Bau ist: gezählt werden nur die gesetzten Blöcke (das Räumen des Platzes zählt nicht). */
        public float fraction() {
            List<Blueprint.Step> steps = this.steps();
            int total = 0;
            int done = 0;
            for (int i = 0; i < steps.size(); i++) {
                if (!steps.get(i).state().isAir()) {
                    total++;
                    if (i < this.progress) {
                        done++;
                    }
                }
            }
            return total == 0 ? 1.0f : Math.min(1.0f, done / (float) total);
        }

        public BoundingBox bounds() {
            return this.type.blueprint().worldBounds(this.origin, this.rotation);
        }
    }

    public static final Codec<Town> CODEC = RecordCodecBuilder.create(i -> i.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(t -> t.id),
            UUIDUtil.CODEC.fieldOf("owner").forGetter(t -> t.owner),
            Codec.STRING.fieldOf("owner_name").forGetter(t -> t.ownerName),
            Codec.STRING.fieldOf("name").forGetter(t -> t.name),
            Level.RESOURCE_KEY_CODEC.fieldOf("dimension").forGetter(t -> t.dimension),
            BlockPos.CODEC.fieldOf("banner").forGetter(t -> t.banner),
            Codec.BOOL.optionalFieldOf("has_banner", true).forGetter(t -> t.hasBanner),
            Codec.LONG.optionalFieldOf("founded", 0L).forGetter(t -> t.founded),
            Codec.BOOL.optionalFieldOf("queen", false).forGetter(t -> t.queen),
            Codec.BOOL.optionalFieldOf("crowned", false).forGetter(t -> t.crowned),
            Resident.CODEC.listOf().optionalFieldOf("residents", List.of()).forGetter(t -> List.copyOf(t.residents)),
            Building.CODEC.listOf().optionalFieldOf("buildings", List.of()).forGetter(t -> List.copyOf(t.buildings)),
            Project.CODEC.listOf().optionalFieldOf("projects", List.of()).forGetter(t -> List.copyOf(t.projects))
    ).apply(i, Town::new));

    /** Betten im Lager am Stadtbanner (für die ersten Bewohner, bevor es Häuser gibt). */
    public static final int CAMP_BEDS = 2;

    private final UUID id;
    private final UUID owner;
    private String ownerName;
    private String name;
    private final ResourceKey<Level> dimension;
    private BlockPos banner;
    private boolean hasBanner;
    private final long founded;
    private boolean queen;
    private boolean crowned;
    private final List<Resident> residents = new ArrayList<>();
    private final List<Building> buildings = new ArrayList<>();
    private final List<Project> projects = new ArrayList<>();

    private Town(UUID id, UUID owner, String ownerName, String name, ResourceKey<Level> dimension, BlockPos banner, boolean hasBanner,
            long founded, boolean queen, boolean crowned, List<Resident> residents, List<Building> buildings, List<Project> projects) {
        this.id = id;
        this.owner = owner;
        this.ownerName = ownerName;
        this.name = name;
        this.dimension = dimension;
        this.banner = banner;
        this.hasBanner = hasBanner;
        this.founded = founded;
        this.queen = queen;
        this.crowned = crowned;
        this.residents.addAll(residents);
        this.buildings.addAll(buildings);
        this.projects.addAll(projects);
    }

    public static Town found(UUID owner, String ownerName, String name, ResourceKey<Level> dimension, BlockPos banner, long time) {
        return new Town(UUID.randomUUID(), owner, ownerName, name, dimension, banner, true, time, false, false, List.of(), List.of(), List.of());
    }

    // ---------------------------------------------------------------- Grunddaten

    public UUID id() {
        return this.id;
    }

    public UUID owner() {
        return this.owner;
    }

    public String ownerName() {
        return this.ownerName;
    }

    void setOwnerName(String ownerName) {
        this.ownerName = ownerName;
    }

    public String name() {
        return this.name;
    }

    void setName(String name) {
        this.name = name;
    }

    public ResourceKey<Level> dimension() {
        return this.dimension;
    }

    public BlockPos banner() {
        return this.banner;
    }

    public boolean hasBanner() {
        return this.hasBanner;
    }

    void moveBanner(BlockPos pos) {
        this.banner = pos;
        this.hasBanner = true;
    }

    void removeBanner() {
        this.hasBanner = false;
    }

    public long founded() {
        return this.founded;
    }

    public boolean queen() {
        return this.queen;
    }

    void setQueen(boolean queen) {
        this.queen = queen;
    }

    public boolean crowned() {
        return this.crowned;
    }

    void setCrowned(boolean crowned) {
        this.crowned = crowned;
    }

    // ---------------------------------------------------------------- Stufe und Fläche

    public TownTier tier() {
        return TownTier.of(this.residents.size());
    }

    public int radius() {
        return this.tier().radius();
    }

    public boolean contains(ResourceKey<Level> dimension, BlockPos pos) {
        if (!dimension.equals(this.dimension)) {
            return false;
        }
        long dx = pos.getX() - this.banner.getX();
        long dz = pos.getZ() - this.banner.getZ();
        int r = this.radius();
        return dx * dx + dz * dz <= (long) r * r;
    }

    // ---------------------------------------------------------------- Bewohner

    public List<Resident> residents() {
        return List.copyOf(this.residents);
    }

    public int count(Profession profession) {
        int n = 0;
        for (Resident r : this.residents) {
            if (r.profession() == profession) {
                n++;
            }
        }
        return n;
    }

    public @Nullable Resident resident(UUID entity) {
        for (Resident r : this.residents) {
            if (r.entity().equals(entity)) {
                return r;
            }
        }
        return null;
    }

    void addResident(Resident resident) {
        this.residents.add(resident);
    }

    boolean removeResident(UUID entity) {
        return this.residents.removeIf(r -> r.entity().equals(entity));
    }

    void updateResident(Resident resident) {
        for (int i = 0; i < this.residents.size(); i++) {
            if (this.residents.get(i).entity().equals(resident.entity())) {
                this.residents.set(i, resident);
            }
        }
    }

    /** Alle Betten der Siedlung (in fertigen Gebäuden), in der Welt. */
    public List<BlockPos> beds() {
        List<BlockPos> beds = new ArrayList<>();
        for (Building building : this.buildings) {
            for (BlockPos bed : building.type().blueprint().beds()) {
                beds.add(building.world(bed));
            }
        }
        return beds;
    }

    /** Wie viele Bewohner Platz haben: Betten in Gebäuden plus das Lager am Banner. */
    public int capacity() {
        return CAMP_BEDS + this.beds().size();
    }

    public int freeBeds() {
        return Math.max(0, this.capacity() - this.residents.size());
    }

    // ---------------------------------------------------------------- Gebäude und Baustellen

    public List<Building> buildings() {
        return List.copyOf(this.buildings);
    }

    public int count(BuildingType type) {
        int n = 0;
        for (Building b : this.buildings) {
            if (b.type() == type) {
                n++;
            }
        }
        return n;
    }

    /** Arbeitsplätze für einen Beruf (der Baumeister hat immer einen, solange die Siedlung steht). */
    public int jobs(Profession profession) {
        BuildingType workplace = profession.workplace();
        if (workplace == null) {
            return this.tier().ordinal() >= TownTier.VILLAGE.ordinal() ? 2 : 1;
        }
        return this.count(workplace) * workplace.jobs();
    }

    public @Nullable Building building(BlockPos origin) {
        for (Building b : this.buildings) {
            if (b.origin().equals(origin)) {
                return b;
            }
        }
        return null;
    }

    /** Wie viele Bewohner in diesem Gebäude arbeiten. */
    public int workers(BlockPos origin) {
        int n = 0;
        for (Resident r : this.residents) {
            if (r.work().isPresent() && r.work().get().equals(origin)) {
                n++;
            }
        }
        return n;
    }

    /** Ist dieses Bett schon vergeben? */
    public boolean bedTaken(BlockPos bed) {
        for (Resident r : this.residents) {
            if (r.bed().isPresent() && r.bed().get().equals(bed)) {
                return true;
            }
        }
        return false;
    }

    void addBuilding(Building building) {
        this.buildings.add(building);
    }

    public List<Project> projects() {
        return this.projects;
    }

    void addProject(Project project) {
        this.projects.add(project);
    }

    void removeProject(Project project) {
        this.projects.remove(project);
    }

    /** Überschneidet sich ein Bereich mit einem Gebäude, einer Baustelle oder dem Banner? */
    public boolean overlaps(BoundingBox box) {
        BoundingBox inflated = box.inflatedBy(1);
        for (Building b : this.buildings) {
            if (b.bounds().intersects(inflated)) {
                return true;
            }
        }
        for (Project p : this.projects) {
            if (p.bounds().intersects(inflated)) {
                return true;
            }
        }
        return this.hasBanner && inflated.isInside(this.banner);
    }
}
