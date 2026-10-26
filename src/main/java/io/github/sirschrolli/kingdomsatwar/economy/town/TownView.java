package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

/**
 * Was das Stadtfenster zeigt: ein Abbild der Siedlung für einen Spieler (Name, Stufe, Bewohner, Preise, Baustellen,
 * Voraussetzungen der Krönung). Wird vom Server geschickt.
 */
public record TownView(String name, String owner, boolean isOwner, int tier, int capacity, int radius, int reputation, boolean crowned,
        boolean queen, List<ResidentView> residents, List<HireView> hires, List<BuildView> builds, List<ProjectView> projects, int crown) {

    /** Ein Bürger: Name, Beruf, Zustand (0 = unterwegs, 1 = bei der Arbeit, 2 = schläft, 3 = fern), mit Bett und Werkstatt. */
    public record ResidentView(String name, int profession, int variant, int state, boolean bed, boolean work) {
        static final StreamCodec<RegistryFriendlyByteBuf, ResidentView> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.STRING_UTF8, ResidentView::name,
                ByteBufCodecs.VAR_INT, ResidentView::profession,
                ByteBufCodecs.VAR_INT, ResidentView::variant,
                ByteBufCodecs.VAR_INT, ResidentView::state,
                ByteBufCodecs.BOOL, ResidentView::bed,
                ByteBufCodecs.BOOL, ResidentView::work,
                ResidentView::new);
    }

    /** Anheuern: Preis, wie viele es schon gibt, und ob es geht (0 = ja, 1 = keine Werkstatt, 2 = kein Bett). */
    public record HireView(int price, int count, int blocked) {
        static final StreamCodec<RegistryFriendlyByteBuf, HireView> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, HireView::price,
                ByteBufCodecs.VAR_INT, HireView::count,
                ByteBufCodecs.VAR_INT, HireView::blocked,
                HireView::new);
    }

    /** Bauen: Preis, wie viele stehen, und ob die Stufe reicht. */
    public record BuildView(int price, int count, boolean allowed) {
        static final StreamCodec<RegistryFriendlyByteBuf, BuildView> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, BuildView::price,
                ByteBufCodecs.VAR_INT, BuildView::count,
                ByteBufCodecs.BOOL, BuildView::allowed,
                BuildView::new);
    }

    /** Eine Baustelle: was gebaut wird und wie weit (Promille). */
    public record ProjectView(int type, int permille) {
        static final StreamCodec<RegistryFriendlyByteBuf, ProjectView> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, ProjectView::type,
                ByteBufCodecs.VAR_INT, ProjectView::permille,
                ProjectView::new);
    }

    /** Bits der Krönungs-Voraussetzungen. */
    public static final int CROWN_SEAL = 1;
    public static final int CROWN_CITY = 2;
    public static final int CROWN_REPUTATION = 4;
    public static final int CROWN_HALL = 8;
    public static final int CROWN_CROWN = 16;
    public static final int CROWN_DONE = 32;

    public static final StreamCodec<RegistryFriendlyByteBuf, TownView> STREAM_CODEC = StreamCodec.of(TownView::write, TownView::read);

    private static void write(RegistryFriendlyByteBuf buf, TownView v) {
        buf.writeUtf(v.name);
        buf.writeUtf(v.owner);
        buf.writeBoolean(v.isOwner);
        buf.writeVarInt(v.tier);
        buf.writeVarInt(v.capacity);
        buf.writeVarInt(v.radius);
        buf.writeVarInt(v.reputation);
        buf.writeBoolean(v.crowned);
        buf.writeBoolean(v.queen);
        ResidentView.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, v.residents);
        HireView.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, v.hires);
        BuildView.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, v.builds);
        ProjectView.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, v.projects);
        buf.writeVarInt(v.crown);
    }

    private static TownView read(RegistryFriendlyByteBuf buf) {
        return new TownView(buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readVarInt(), buf.readVarInt(), buf.readVarInt(),
                buf.readVarInt(), buf.readBoolean(), buf.readBoolean(),
                ResidentView.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                HireView.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                BuildView.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                ProjectView.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                buf.readVarInt());
    }

    public TownTier tierValue() {
        return TownTier.values()[Math.max(0, Math.min(TownTier.values().length - 1, this.tier))];
    }

    public Reputation.Rank rank() {
        return Reputation.Rank.of(this.reputation);
    }

    /** Das Abbild der Siedlung für diesen Spieler. */
    public static TownView of(ServerPlayer player, Town town) {
        ServerLevel level = player.level().getServer().getLevel(town.dimension());
        List<ResidentView> residents = new ArrayList<>();
        for (Town.Resident r : town.residents()) {
            int state = 3;
            if (level != null && level.getEntity(r.entity()) instanceof CitizenEntity citizen) {
                state = citizen.isSleeping() ? 2 : citizen.isWorking() ? 1 : 0;
            }
            residents.add(new ResidentView(r.name(), r.profession().ordinal(), r.variant(), state, r.bed().isPresent(),
                    r.work().isPresent() || r.profession().workplace() == null));
        }
        List<HireView> hires = new ArrayList<>();
        for (Profession p : Profession.values()) {
            int blocked = !TownManager.hasWorkplace(town, p) ? 1 : town.freeBeds() <= 0 ? 2 : 0;
            hires.add(new HireView(TownManager.hirePrice(town, p, player), town.count(p), blocked));
        }
        List<BuildView> builds = new ArrayList<>();
        for (BuildingType type : BuildingType.values()) {
            builds.add(new BuildView(TownManager.buildPrice(town, type, player), town.count(type),
                    town.tier().ordinal() >= type.tier().ordinal()));
        }
        List<ProjectView> projects = new ArrayList<>();
        for (Town.Project p : town.projects()) {
            projects.add(new ProjectView(p.type.ordinal(), Math.round(p.fraction() * 1000)));
        }
        return new TownView(town.name(), town.ownerName(), town.owner().equals(player.getUUID()), town.tier().ordinal(), town.capacity(),
                town.radius(), Reputation.points(player), town.crowned(), town.queen(), residents, hires, builds, projects,
                Coronation.requirements(player, town));
    }
}
