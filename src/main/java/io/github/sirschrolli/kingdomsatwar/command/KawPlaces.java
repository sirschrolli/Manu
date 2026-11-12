package io.github.sirschrolli.kingdomsatwar.command;

import com.mojang.brigadier.exceptions.CommandSyntaxException;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardLocator;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.block.abyss.ClockHeartBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.abyss.HoardSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.end.StarstoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.entity.DragonLairCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.fae.KeeperStoneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.fae.MothCocoonBlock;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.glitch.ErrorCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.nether.GlutAnvilBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.sea.AtollCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.sea.KrakenCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.sea.WitchCauldronBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.shadow.SoulBellBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.ThroneSealBlockEntity;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.fae.Mondhain;
import io.github.sirschrolli.kingdomsatwar.fae.Mondsteinbruch;
import io.github.sirschrolli.kingdomsatwar.fae.MystikStage;
import io.github.sirschrolli.kingdomsatwar.fae.feature.MystikStageFeature;
import io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.urmeer.UrmeerDimension;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmLocator;
import io.github.sirschrolli.kingdomsatwar.world.SealCircle;
import io.github.sirschrolli.kingdomsatwar.world.SealState;
import io.github.sirschrolli.kingdomsatwar.world.ShadowGate;
import io.github.sirschrolli.kingdomsatwar.worldgen.atoll.AtollLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.clock.ClockTowerStructure;
import io.github.sirschrolli.kingdomsatwar.worldgen.kraken.KrakenMawLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.shepherd.SoulPenPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator;
import io.github.sirschrolli.kingdomsatwar.worldgen.umbrer.ShadowSiteStructure;
import io.github.sirschrolli.kingdomsatwar.worldgen.village.VillageStyle;
import io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebStructure;
import io.github.sirschrolli.kingdomsatwar.worldgen.witch.WitchGrottoPiece;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;
import java.util.function.Supplier;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructurePiece;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.level.material.FluidState;
import org.jspecify.annotations.Nullable;

/**
 * Die Orte, die {@code /kaw} findet (seit 1.1.2): alle Bauwerke ({@code /kaw structure <Name>}) und alle Bosse in ihren
 * Horten ({@code /kaw boss <Name>}). Jeder Ort weiß, in welcher Dimension er liegt, wie man den nächsten findet – aus jeder
 * Dimension heraus – und wo man landet, wenn man hinreist: auf festem Boden, nicht in Lava oder im Nichts, mit Blick auf das
 * Bauwerk. Bosse lassen sich außerdem wecken und zurücksetzen (die beiden alten Drachen auch vor sich herbeirufen).
 */
public final class KawPlaces {
    /** Ein gefundener Ort: seine Mitte (die Höhe nur, wenn {@code knownY}) und, falls bekannt, der Rahmen des Bauwerks. */
    public record Spot(BlockPos center, boolean knownY, @Nullable BoundingBox box) {}

    /** Wo man nach der Reise steht und wohin man blickt. */
    public record Landing(double x, double y, double z, float yaw, float pitch) {}

    /** Sucht den nächsten Ort ab {@code from} (in der Dimension des Orts); null: keiner in dieser Welt. */
    @FunctionalInterface
    public interface Finder {
        @Nullable Spot find(ServerLevel level, BlockPos from);
    }

    /** Wo man an einem gefundenen Ort landet (lädt dafür, was nötig ist). */
    @FunctionalInterface
    public interface Lander {
        Landing land(ServerLevel level, Spot spot);
    }

    /** Wecken, Zurücksetzen oder Herbeirufen eines Bosses an seinem Hort. */
    @FunctionalInterface
    public interface Action {
        int run(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) throws CommandSyntaxException;
    }

    /** Ein Bauwerk: Name für den Befehl, Dimension, Suche, Landeplatz – und das Bauwerk der Mod, das es ist (null bei Lichtungen). */
    public record Site(String id, ResourceKey<Level> dimension, @Nullable ResourceKey<Structure> structure, Finder finder, Lander lander) {
        public Component name() {
            return Component.translatable("place.kingdomsatwar." + this.id);
        }
    }

    /** Ein Boss: Name für den Befehl, sein Hort und was sich mit ihm anstellen lässt (null: geht bei ihm nicht). */
    public record Boss(String id, Supplier<? extends EntityType<?>> type, Site site, @Nullable Action awaken, @Nullable Action reset,
            @Nullable Action summon) {
        public Component name() {
            return this.type.get().getDescription();
        }
    }

    /** Wie man sich einem Bauwerk nähert. */
    private enum Approach {
        /** von Süden, auf dem Boden knapp vor dem Bauwerk (für Häuser, Türme, Burgen, Inseln) */
        FRONT,
        /** mitten hinein (für Dörfer und Lager, die man am besten von innen sieht) */
        CENTER,
        /** in das Bauwerk hinein, auf den höchsten Boden nahe der Mitte (für Bauwerke unter der Erde) */
        INSIDE
    }

    private static final Map<String, Site> SITES = new LinkedHashMap<>();
    private static final Map<String, Boss> BOSSES = new LinkedHashMap<>();

    // =====================================================================================
    // Die Bauwerke
    // =====================================================================================

    // ---- Oberwelt
    public static final Site FALLEN_STAR = siteKind("sternensturz", SiteKind.FALLEN_STAR, Approach.FRONT, 40);
    public static final Site AQUEDUCT = siteKind("wasserstollen", SiteKind.AQUEDUCT, Approach.INSIDE, 0);
    public static final Site TIDE_SHRINE = siteKind("gezeitenschrein", SiteKind.TIDE_SHRINE, Approach.FRONT, 16);
    public static final Site SEA_DRAGON = siteKind("seedrachengebeine", SiteKind.SEA_DRAGON, Approach.FRONT, 24);
    public static final Site CARAVANSERAI = siteKind("karawanserei", SiteKind.CARAVANSERAI, Approach.FRONT, 28);
    public static final Site SERPENT_TEMPLE = siteKind("schlangentempel", SiteKind.SERPENT_TEMPLE, Approach.FRONT, 28);
    public static final Site WITCH_TREE = siteKind("hexenbaum", SiteKind.WITCH_TREE, Approach.FRONT, 20);
    public static final Site FROST_GROTTO = siteKind("frostgrotte", SiteKind.FROST_GROTTO, Approach.FRONT, 16);
    public static final Site BANDIT_CAMP = siteKind("raeuberlager", SiteKind.BANDIT_CAMP, Approach.FRONT, 24);
    public static final Site CHARCOAL_CAMP = siteKind("koehlerei", SiteKind.CHARCOAL_CAMP, Approach.FRONT, 20);
    public static final Site EMBER_GATE = siteKind("glutpforte", SiteKind.EMBER_GATE, Approach.FRONT, 16);
    public static final Site CLAM_FIELD = siteKind("muschelfeld", SiteKind.CLAM_FIELD, Approach.CENTER, 0);
    public static final Site CORAL_PALACE = siteKind("korallenpalast", SiteKind.CORAL_PALACE, Approach.FRONT, 32);
    public static final Site BURIED_ABBEY = siteKind("verschuettetes_kloster", SiteKind.BURIED_ABBEY, Approach.FRONT, 24);
    public static final Site SILENT_CATHEDRAL = siteKind("stille_kathedrale", SiteKind.SILENT_CATHEDRAL, Approach.INSIDE, 0);
    public static final Site TRIAL_ARENA = siteKind("pruefungsarena", SiteKind.TRIAL_ARENA, Approach.INSIDE, 0);
    public static final Site ROBBER_CASTLE = siteKind("raubritterburg", SiteKind.ROBBER_CASTLE, Approach.FRONT, 40);
    public static final Site ERROR_ZONE = register(new Site("fehlerzone", Level.OVERWORLD, ModStructures.site(SiteKind.ERROR_ZONE),
            chunkCenter(ModStructures.site(SiteKind.ERROR_ZONE)), (level, spot) -> surfaceAt(level, spot.center(), 0, 19, 10.0f)));
    public static final Site SUNKEN_TEMPLE = siteKind("versunkener_tempel", SiteKind.SUNKEN_TEMPLE, Approach.FRONT, 32);
    public static final Site ATOLL = register(new Site("atoll", Level.OVERWORLD, ModStructures.SUNKEN_ATOLL_STRUCTURE,
            (level, from) -> known(AtollLocator.center(level)),
            (level, spot) -> fixed(spot.center(), 0.5, 2, 16.5, 180.0f, 10.0f)));
    public static final Map<DragonType, Site> LAIRS = Collections.unmodifiableMap(lairs());
    public static final Map<DragonType, Site> MAGE_TOWERS = Collections.unmodifiableMap(mageTowers());
    public static final Map<VillageStyle, Site> VILLAGES = Collections.unmodifiableMap(villages());
    public static final Site REALM_CASTLE = register(new Site("stammburg", Level.OVERWORLD, ModStructures.REALM_CASTLE_STRUCTURE,
            KawPlaces::nearestCastle, approach(Approach.FRONT, 48)));
    public static final Site SEAL_CIRCLE = register(new Site("siegelkreis", Level.OVERWORLD, ModStructures.SEAL_CIRCLE_STRUCTURE,
            (level, from) -> sealCircle(level), approach(Approach.FRONT, 9)));

    // ---- Nether
    public static final Site ASH_FORGE = siteKind("aschenesse", Level.NETHER, SiteKind.ASH_FORGE, Approach.FRONT, 40);
    public static final Site HORTFESTE = siteKind("hortfeste", Level.NETHER, SiteKind.HORTFESTE, Approach.INSIDE, 0);
    public static final Site GIANT_BONES = siteKind("riesengebeine", Level.NETHER, SiteKind.GIANT_BONES, Approach.FRONT, 20);
    public static final Site LOST_GATE = siteKind("verlorene_pforte", Level.NETHER, SiteKind.LOST_GATE, Approach.FRONT, 28);
    public static final Site FLAME_SANCTUARY = siteKind("flammenheiligtum", Level.NETHER, SiteKind.FLAME_SANCTUARY, Approach.FRONT, 30);

    // ---- End
    public static final Site STARWATCH = siteKind("sternwarte", Level.END, SiteKind.STARWATCH, Approach.FRONT, 40);

    // ---- Schattenwelt
    public static final Site CITADEL = register(new Site("zitadelle", ShadowGate.SHADOW_WORLD, ModStructures.SHADOW_CITADEL_STRUCTURE,
            (level, from) -> citadel(level), (level, spot) -> surfaceAt(level, spot.center(), 0, 76, 0.0f)));
    public static final Site SOUL_PEN = register(new Site("seelenpferch", ShadowGate.SHADOW_WORLD, ModStructures.SOUL_PEN_STRUCTURE,
            pieceCenter(ModStructures.SOUL_PEN_STRUCTURE, SoulPenPiece.class, SoulPenPiece::center), KawPlaces::soulPenLanding));
    public static final Site UMBRER_VILLAGE = register(new Site("umbrerdorf", ShadowGate.SHADOW_WORLD, ModStructures.UMBRER_VILLAGE_STRUCTURE,
            structure(ModStructures.UMBRER_VILLAGE_STRUCTURE), approach(Approach.CENTER, 0)));
    public static final Site MOON_TEMPLE = shadowSite("mondtempel", ShadowSiteStructure.Kind.TEMPLE, Approach.FRONT);
    public static final Site CRYSTAL_MINE = shadowSite("kristallmine", ShadowSiteStructure.Kind.MINE, Approach.FRONT);
    public static final Site LIGHTHOUSE = shadowSite("leuchtturm", ShadowSiteStructure.Kind.LIGHTHOUSE, Approach.FRONT);
    public static final Site OLD_WORLD_RUIN = shadowSite("ruine_der_alten_welt", ShadowSiteStructure.Kind.RUIN, Approach.FRONT);

    // ---- Leere
    public static final Site THRONE = register(new Site("thron", VoidGate.THE_VOID, ModStructures.VOID_THRONE_STRUCTURE,
            (level, from) -> known(ThroneLocator.center(level)), (level, spot) -> fixed(spot.center(), 0.5, 0, 20.5, 180.0f, 0.0f)));
    public static final Site STAR_WEB = register(new Site("sternennetz", VoidGate.THE_VOID, ModStructures.STAR_WEB_STRUCTURE,
            KawPlaces::starWeb, (level, spot) -> fixed(spot.center(), 0.5, 0, 0.5 + StarWebPiece.DISC - 1.5, 180.0f, 10.0f)));

    // ---- Abgrund
    public static final Site HOARD = register(new Site("schlund_der_welten", AbyssGate.THE_ABYSS, ModStructures.WORLD_EATER_HOARD_STRUCTURE,
            (level, from) -> known(HoardLocator.center(level)),
            (level, spot) -> fixed(spot.center(), 0.5, 0, 0.5 + HoardPiece.R_OUT - 3.0, 180.0f, 5.0f)));
    public static final Site CLOCK_TOWER = register(new Site("uhrturm", AbyssGate.THE_ABYSS, ModStructures.CLOCK_TOWER_STRUCTURE,
            KawPlaces::clockTower, (level, spot) -> fixed(spot.center(), 0.5, 0, 0.5 + ClockTowerPiece.DIAL - 1.5, 180.0f, 5.0f)));

    // ---- Urmeer
    public static final Site KRAKEN_MAW = register(new Site("krakenschlund", UrmeerDimension.LEVEL, ModStructures.KRAKEN_MAW_STRUCTURE,
            (level, from) -> known(KrakenMawLocator.center(level)), (level, spot) -> fixed(spot.center(), 0.5, 12, 40.5, 180.0f, 15.0f)));
    public static final Site SEA_VILLAGE = register(new Site("meeresdorf", UrmeerDimension.LEVEL, ModStructures.SEA_VILLAGE_STRUCTURE,
            structure(ModStructures.SEA_VILLAGE_STRUCTURE), approach(Approach.CENTER, 0)));
    public static final Site WITCH_GROTTO = register(new Site("hexengrotte", UrmeerDimension.LEVEL, ModStructures.WITCH_GROTTO_STRUCTURE,
            pieceCenter(ModStructures.WITCH_GROTTO_STRUCTURE, WitchGrottoPiece.class, WitchGrottoPiece::center), KawPlaces::witchGrottoLanding));

    // ---- Feenreich (Lichtungen, keine Bauwerke: ihre Lage folgt aus dem Startwert)
    public static final Site MONDHAIN = register(new Site("mondhain", FaeGate.FAE_REALM, null,
            (level, from) -> new Spot(Mondhain.nearest(level.getSeed(), from.getX(), from.getZ()), false, null),
            (level, spot) -> surfaceAt(level, spot.center(), 0, 9, 10.0f)));
    public static final Site MYSTIK_STAGE = register(new Site("zauberbuehne", FaeGate.FAE_REALM, null,
            (level, from) -> unknownY(MystikStage.nearest(level.getSeed(), from.getX(), from.getZ())),
            (level, spot) -> surfaceAt(level, spot.center(), 0, 17, 10.0f)));
    public static final Site MOONSTONE_QUARRY = register(new Site("mondsteinbruch", FaeGate.FAE_REALM, null,
            (level, from) -> unknownY(Mondsteinbruch.nearest(level.getSeed(), from.getX(), from.getZ())),
            (level, spot) -> surfaceAt(level, spot.center(), 0, 19, 25.0f)));

    // =====================================================================================
    // Die Bosse
    // =====================================================================================

    static {
        for (DragonType type : io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.ORDER) {
            boss(new Boss(dragonName(type), dragonType(type), LAIRS.get(type), KawPlaces::awakenDragon, KawPlaces::resetDragon,
                    type == DragonType.FIRE ? KawPlaces::summonDragon : null));
        }
        boss(new Boss("nyxar", ModEntities.NYXAR, CITADEL, KawPlaces::awakenNyxar, KawPlaces::resetNyxar, KawPlaces::summonDragon));
        boss(new Boss("karkinos", ModEntities.REEF_COLOSSUS, ATOLL, KawPlaces::awakenKarkinos, KawPlaces::resetKarkinos, null));
        boss(new Boss("anni", ModEntities.ANNI_TROLL, ERROR_ZONE, KawPlaces::awakenAnni, KawPlaces::resetAnni, null));
        boss(new Boss("malleus", ModEntities.GLUTSCHMIED, ASH_FORGE, KawPlaces::awakenMalleus, KawPlaces::resetMalleus, null));
        boss(new Boss("ignara", ModEntities.IGNARA, FLAME_SANCTUARY, KawPlaces::awakenIgnara, KawPlaces::resetIgnara, null));
        boss(new Boss("asteria", ModEntities.ASTERIA, STARWATCH, KawPlaces::awakenAsteria, KawPlaces::resetAsteria, null));
        boss(new Boss("noctua", ModEntities.MOTH_QUEEN, MONDHAIN, KawPlaces::awakenNoctua, null, null));
        boss(new Boss("missmystik", ModEntities.MISS_MYSTIK, MYSTIK_STAGE, KawPlaces::awakenMystik, KawPlaces::resetMystik, null));
        boss(new Boss("steinhueter", ModEntities.STONE_KEEPER, MOONSTONE_QUARRY, KawPlaces::awakenKeeper, KawPlaces::resetKeeper, null));
        boss(new Boss("seelenhirte", ModEntities.SOUL_SHEPHERD, SOUL_PEN, KawPlaces::awakenShepherd, KawPlaces::resetShepherd, null));
        boss(new Boss("koenig", ModEntities.SHATTERED_KING, THRONE, KawPlaces::awakenKing, KawPlaces::resetKing, null));
        boss(new Boss("sternenweberin", ModEntities.STAR_WEAVER, STAR_WEB, KawPlaces::awakenWeaver, KawPlaces::resetWeaver, null));
        boss(new Boss("weltenfresser", ModEntities.WORLD_EATER, HOARD, KawPlaces::awakenWorldEater, KawPlaces::resetWorldEater, null));
        boss(new Boss("stundenwaechter", ModEntities.HOUR_WARDEN, CLOCK_TOWER, KawPlaces::awakenWarden, KawPlaces::resetWarden, null));
        boss(new Boss("kraken", ModEntities.KRAKEN, KRAKEN_MAW, KawPlaces::awakenKraken, KawPlaces::resetKraken, null));
        boss(new Boss("meerhexe", ModEntities.SEA_WITCH, WITCH_GROTTO, KawPlaces::awakenWitch, KawPlaces::resetWitch, null));
        // die Erzmagier warten in ihren Türmen – wer das Sanktum betritt, wird in ihre Sphäre gezogen
        for (DragonType type : io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.ORDER) {
            boss(new Boss(mageName(type), ModEntities.MAGES.get(type), MAGE_TOWERS.get(type), null, null, null));
        }
    }

    private KawPlaces() {}

    /** Alle Bauwerke in der Reihenfolge der Vorschläge (nach Dimension). */
    public static List<Site> sites() {
        return List.copyOf(SITES.values());
    }

    /** Alle Bosse in der Reihenfolge der Vorschläge. */
    public static List<Boss> bosses() {
        return List.copyOf(BOSSES.values());
    }

    public static @Nullable Site site(String id) {
        return SITES.get(id);
    }

    // ---------------------------------------------------------------- Anmelden

    private static Site register(Site site) {
        if (SITES.put(site.id(), site) != null) {
            throw new IllegalStateException("Doppelter Ort: " + site.id());
        }
        return site;
    }

    private static void boss(Boss boss) {
        if (BOSSES.put(boss.id(), boss) != null) {
            throw new IllegalStateException("Doppelter Boss: " + boss.id());
        }
    }

    private static Site siteKind(String id, SiteKind kind, Approach how, int back) {
        return siteKind(id, Level.OVERWORLD, kind, how, back);
    }

    private static Site siteKind(String id, ResourceKey<Level> dimension, SiteKind kind, Approach how, int back) {
        ResourceKey<Structure> key = ModStructures.site(kind);
        return register(new Site(id, dimension, key, structure(key), approach(how, back)));
    }

    private static Site shadowSite(String id, ShadowSiteStructure.Kind kind, Approach how) {
        ResourceKey<Structure> key = ModStructures.shadowSite(kind);
        return register(new Site(id, ShadowGate.SHADOW_WORLD, key, structure(key), approach(how, 28)));
    }

    /** Die sechs Nester: die Lage aus dem Startwert, die Mitte aus der Chronik der Nester (sobald eines erkundet ist). */
    private static Map<DragonType, Site> lairs() {
        Map<DragonType, Site> map = new LinkedHashMap<>();
        for (DragonType type : io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.ORDER) {
            String id = switch (type) {
                case FIRE -> "vulkankrater";
                case ICE -> "gletscherkessel";
                case LIGHTNING -> "sturmgipfel";
                case NATURE -> "uralter_hain";
                case EARTH -> "felsschlucht";
                case WATER -> "versunkene_grotte";
            };
            map.put(type, register(new Site(id, Level.OVERWORLD, ModStructures.lair(type), (level, from) -> nest(level, type),
                    approach(Approach.FRONT, 48))));
        }
        return map;
    }

    private static Map<DragonType, Site> mageTowers() {
        Map<DragonType, Site> map = new LinkedHashMap<>();
        for (DragonType type : io.github.sirschrolli.kingdomsatwar.item.DragonCompassItem.ORDER) {
            ResourceKey<Structure> key = ModStructures.mageTower(type);
            map.put(type, register(new Site("magierturm_" + element(type), Level.OVERWORLD, key, structure(key), approach(Approach.FRONT, 24))));
        }
        return map;
    }

    private static Map<VillageStyle, Site> villages() {
        Map<VillageStyle, Site> map = new LinkedHashMap<>();
        for (VillageStyle style : VillageStyle.values()) {
            String id = switch (style) {
                case TIMBER -> "fachwerkdorf";
                case NORDIC -> "nordisches_dorf";
                case CLAY -> "lehmdorf";
                case STILT -> "pfahldorf";
            };
            ResourceKey<Structure> key = ModStructures.village(style);
            map.put(style, register(new Site(id, Level.OVERWORLD, key, structure(key), approach(Approach.CENTER, 0))));
        }
        return map;
    }

    /** Das Element auf Deutsch (für die Namen der Magiertürme). */
    private static String element(DragonType type) {
        return switch (type) {
            case FIRE -> "feuer";
            case ICE -> "eis";
            case LIGHTNING -> "blitz";
            case NATURE -> "natur";
            case EARTH -> "erde";
            case WATER -> "wasser";
        };
    }

    private static String dragonName(DragonType type) {
        return switch (type) {
            case FIRE -> "pyrrhon";
            case ICE -> "glaciara";
            case LIGHTNING -> "voltarion";
            case NATURE -> "verdantis";
            case EARTH -> "terragon";
            case WATER -> "thalassa";
        };
    }

    private static String mageName(DragonType type) {
        return switch (type) {
            case FIRE -> "ignatius";
            case ICE -> "isolde";
            case LIGHTNING -> "fulgor";
            case NATURE -> "sylvana";
            case EARTH -> "gorvan";
            case WATER -> "marena";
        };
    }

    private static Supplier<? extends EntityType<?>> dragonType(DragonType type) {
        return switch (type) {
            case FIRE -> ModEntities.PYRRHON;
            case ICE -> ModEntities.GLACIARA;
            case LIGHTNING -> ModEntities.VOLTARION;
            case NATURE -> ModEntities.VERDANTIS;
            case EARTH -> ModEntities.TERRAGON;
            case WATER -> ModEntities.THALASSA;
        };
    }

    // =====================================================================================
    // Suchen
    // =====================================================================================

    private static @Nullable Spot known(@Nullable BlockPos center) {
        return center == null ? null : new Spot(center, true, null);
    }

    private static @Nullable Spot unknownY(@Nullable BlockPos center) {
        return center == null ? null : new Spot(center, false, null);
    }

    private static @Nullable Holder<Structure> holder(ServerLevel level, ResourceKey<Structure> key) {
        return level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(key).orElse(null);
    }

    /** Der Chunk des nächsten Bauwerks dieser Art (wie {@code /locate}), null wenn es keins gibt. */
    private static @Nullable ChunkPos nearestChunk(ServerLevel level, @Nullable Holder<Structure> holder, BlockPos from) {
        if (holder == null) {
            return null;
        }
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), from, 100, false);
        return found == null ? null : ChunkPos.containing(found.getFirst());
    }

    /** Der Anfang eines Bauwerks in diesem Chunk (nur die Anfänge werden dafür erzeugt, nicht das Gelände). */
    private static @Nullable StructureStart start(ServerLevel level, Structure structure, ChunkPos chunk) {
        ChunkAccess access = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS);
        StructureStart start = access.getStartForStructure(structure);
        return start != null && start.isValid() ? start : null;
    }

    /** Das nächste Bauwerk dieser Art: Mitte und Rahmen aus seinem Anfang (sonst die Mitte seines Chunks). */
    private static Finder structure(ResourceKey<Structure> key) {
        return (level, from) -> {
            Holder<Structure> holder = holder(level, key);
            ChunkPos chunk = nearestChunk(level, holder, from);
            if (chunk == null) {
                return null;
            }
            StructureStart start = start(level, holder.value(), chunk);
            if (start == null) {
                return new Spot(new BlockPos(chunk.getMiddleBlockX(), level.getSeaLevel(), chunk.getMiddleBlockZ()), false, null);
            }
            BoundingBox box = start.getBoundingBox();
            return new Spot(new BlockPos((box.minX() + box.maxX()) / 2, box.minY(), (box.minZ() + box.maxZ()) / 2), false, box);
        };
    }

    /** Wie {@link #structure}, aber die Mitte liegt in der Mitte des Chunks (Bauwerke, die dort beginnen). */
    private static Finder chunkCenter(ResourceKey<Structure> key) {
        return (level, from) -> {
            Holder<Structure> holder = holder(level, key);
            ChunkPos chunk = nearestChunk(level, holder, from);
            if (chunk == null) {
                return null;
            }
            StructureStart start = start(level, holder.value(), chunk);
            return new Spot(new BlockPos(chunk.getMiddleBlockX(), level.getSeaLevel(), chunk.getMiddleBlockZ()), false,
                    start != null ? start.getBoundingBox() : null);
        };
    }

    /**
     * Das nächste Bauwerk dieser Art, die Mitte (mit Höhe des Bodens) aus seinem Bauteil – dort, wo es wirklich steht (ein
     * Seelenpferch sucht sich seinen Platz rings um den Chunk der Verteilung).
     */
    private static <P extends StructurePiece> Finder pieceCenter(ResourceKey<Structure> key, Class<P> type,
            java.util.function.Function<P, BlockPos> center) {
        return (level, from) -> {
            Holder<Structure> holder = holder(level, key);
            ChunkPos chunk = nearestChunk(level, holder, from);
            if (chunk == null) {
                return null;
            }
            StructureStart start = start(level, holder.value(), chunk);
            if (start != null) {
                for (StructurePiece piece : start.getPieces()) {
                    if (type.isInstance(piece)) {
                        return new Spot(center.apply(type.cast(piece)), true, start.getBoundingBox());
                    }
                }
            }
            return new Spot(new BlockPos(chunk.getMiddleBlockX(), level.getSeaLevel(), chunk.getMiddleBlockZ()), false,
                    start != null ? start.getBoundingBox() : null);
        };
    }

    /** Das Nest eines Drachen: seine Mitte aus der Chronik (sobald erkundet), sonst die Mitte seines Chunks. */
    private static @Nullable Spot nest(ServerLevel level, DragonType type) {
        ChunkPos chunk = NestLocator.all(level).get(type);
        if (chunk == null) {
            return null;
        }
        Holder<Structure> holder = holder(level, ModStructures.lair(type));
        StructureStart start = holder == null ? null : start(level, holder.value(), chunk);
        return new Spot(DragonCompassItem.nestCenter(level, type, chunk), false, start != null ? start.getBoundingBox() : null);
    }

    /** Die Schattenzitadelle (eine je Welt, ihre Lage folgt aus dem Startwert). */
    private static @Nullable Spot citadel(ServerLevel level) {
        ChunkPos chunk = CitadelLocator.find(level);
        return chunk == null ? null : new Spot(new BlockPos(chunk.getMiddleBlockX(), level.getSeaLevel(), chunk.getMiddleBlockZ()), false, null);
    }

    /** Die nächste der vier Stammburgen. */
    private static @Nullable Spot nearestCastle(ServerLevel level, BlockPos from) {
        Holder<Structure> holder = holder(level, ModStructures.REALM_CASTLE_STRUCTURE);
        ChunkPos best = null;
        double bestDistance = Double.MAX_VALUE;
        for (Realm realm : Realm.values()) {
            ChunkPos chunk = RealmLocator.find(level, realm);
            if (chunk == null) {
                continue;
            }
            double dx = chunk.getMiddleBlockX() - from.getX();
            double dz = chunk.getMiddleBlockZ() - from.getZ();
            double distance = dx * dx + dz * dz;
            if (distance < bestDistance) {
                best = chunk;
                bestDistance = distance;
            }
        }
        if (best == null) {
            return null;
        }
        StructureStart start = holder == null ? null : start(level, holder.value(), best);
        if (start == null) {
            return new Spot(new BlockPos(best.getMiddleBlockX(), level.getSeaLevel(), best.getMiddleBlockZ()), false, null);
        }
        BoundingBox box = start.getBoundingBox();
        return new Spot(new BlockPos((box.minX() + box.maxX()) / 2, box.minY(), (box.minZ() + box.maxZ()) / 2), false, box);
    }

    /** Der Siegelkreis: genau, wenn er schon gefunden wurde, sonst ungefähr (Höhe unbekannt). */
    private static @Nullable Spot sealCircle(ServerLevel level) {
        BlockPos center = SealCircle.center(level);
        if (center == null) {
            return null;
        }
        boolean exact = SealState.of(level.getServer().overworld()).center() != null;
        return new Spot(center, exact, new BoundingBox(center.getX() - 6, center.getY() - 4, center.getZ() - 6, center.getX() + 6,
                center.getY() + 6, center.getZ() + 6));
    }

    /** Das nächste Sternennetz: die Mitte steht im Bauwerk (die Suche kann es vom Chunk der Verteilung fortgerückt haben). */
    private static @Nullable Spot starWeb(ServerLevel level, BlockPos from) {
        Holder<Structure> holder = holder(level, ModStructures.STAR_WEB_STRUCTURE);
        ChunkPos chunk = nearestChunk(level, holder, from);
        if (chunk == null) {
            return null;
        }
        StructureStart start = start(level, holder.value(), chunk);
        if (start != null) {
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof StarWebPiece web) {
                    return new Spot(web.center(), true, start.getBoundingBox());
                }
            }
        }
        return new Spot(new BlockPos(chunk.getMiddleBlockX(), StarWebStructure.HEIGHTS[0], chunk.getMiddleBlockZ()), true, null);
    }

    /** Der nächste Uhrturm: die Mitte des Zifferblatts steht im Bauwerk. */
    private static @Nullable Spot clockTower(ServerLevel level, BlockPos from) {
        Holder<Structure> holder = holder(level, ModStructures.CLOCK_TOWER_STRUCTURE);
        ChunkPos chunk = nearestChunk(level, holder, from);
        if (chunk == null) {
            return null;
        }
        StructureStart start = start(level, holder.value(), chunk);
        if (start != null) {
            for (StructurePiece piece : start.getPieces()) {
                if (piece instanceof ClockTowerPiece tower) {
                    return new Spot(tower.center(), true, start.getBoundingBox());
                }
            }
        }
        return new Spot(new BlockPos(chunk.getMiddleBlockX(), ClockTowerStructure.HEIGHTS[0], chunk.getMiddleBlockZ()), true, null);
    }

    // =====================================================================================
    // Landen
    // =====================================================================================

    /** Ein fester Platz relativ zur Mitte (Bauwerke, deren Boden dort sicher liegt: Thron, Netz, Zifferblatt, Atoll, Schlund). */
    private static Landing fixed(BlockPos c, double dx, double dy, double dz, float yaw, float pitch) {
        return new Landing(c.getX() + dx, c.getY() + dy, c.getZ() + dz, yaw, pitch);
    }

    /** Auf dem Boden {@code dz} Blöcke südlich (und {@code dx} östlich) der Mitte, mit Blick zur Mitte. */
    private static Landing surfaceAt(ServerLevel level, BlockPos c, int dx, int dz, float pitch) {
        BlockPos spot = nearColumn(level, c.getX() + dx, c.getZ() + dz, null);
        return look(spot != null ? spot : fallback(level, c.getX() + dx, c.getZ() + dz), c, pitch);
    }

    private static Lander approach(Approach how, int maxBack) {
        return (level, spot) -> approach(level, spot, how, maxBack);
    }

    /**
     * Wie wählerisch ein Durchgang der Suche ist: erst trocken, frei (keine Lücke zwischen Mauern) und mit freiem Blick zum
     * Bauwerk, dann nur trocken, zuletzt auch im Wasser (bei Bauwerken unter Wasser am Grund neben ihnen, sonst schwimmt man an
     * der Oberfläche).
     */
    private record Pass(boolean wet, int open) {}

    private static final Pass[] PASSES = {new Pass(false, 6), new Pass(false, 0), new Pass(true, 0)};

    /** Wo man an einem Bauwerk landet (siehe {@link Approach}); nirgends Lava, nirgends im Nichts. */
    private static Landing approach(ServerLevel level, Spot spot, Approach how, int maxBack) {
        BlockPos c = spot.center();
        BoundingBox box = spot.box();
        if (how == Approach.INSIDE && box != null) {
            for (Pass pass : new Pass[] {new Pass(false, 4), new Pass(false, 0), new Pass(true, 0)}) {
                BlockPos inside = inside(level, c, box, pass);
                if (inside != null) {
                    return widest(level, inside);
                }
            }
        }
        if (how == Approach.CENTER) {
            // nicht genau in die Mitte (dort steht oft ein Brunnen), sondern ein paar Schritte daneben, mit Blick zur Mitte
            for (Pass pass : PASSES) {
                for (int r = 4; r <= 20; r += 2) {
                    int steps = r * 2;
                    for (int k = 0; k < steps; k++) {
                        double a = k * 2.0 * Math.PI / steps;
                        int x = c.getX() + (int) Math.round(Math.sin(a) * r);
                        int z = c.getZ() + (int) Math.round(Math.cos(a) * r);
                        BlockPos found = column(level, x, z, box, pass, c);
                        if (found != null) {
                            return look(found, c, 10.0f);
                        }
                    }
                }
            }
        }
        // von Süden: knapp vor dem Rahmen beginnen und auf das Bauwerk zugehen, bis Boden da ist (unter einer Decke wie im
        // Nether am Rand des Bauwerks selbst – davor läge man in einer Höhle im Fels)
        boolean ceiling = level.dimensionType().hasCeiling();
        int back = box == null ? Math.min(12, Math.max(maxBack, 6))
                : ceiling ? Math.max(0, box.maxZ() - c.getZ() - 1)
                : Math.max(4, Math.min(box.maxZ() - c.getZ() + 10, Math.max(maxBack, 6)));
        int[] sides = {0, 3, -3, 6, -6};
        for (Pass pass : PASSES) {
            for (int t = back; t >= 0; t--) {
                for (int dx : sides) {
                    BlockPos found = column(level, c.getX() + dx, c.getZ() + t, box, pass, c);
                    if (found != null) {
                        return look(found, c, 10.0f);
                    }
                }
            }
        }
        return look(fallback(level, c.getX(), c.getZ() + back), c, 10.0f);
    }

    /**
     * Im Bauwerk: in den Säulen nahe der Mitte (ringweise nach außen) der Boden, der der halben Höhe des Bauwerks am nächsten
     * liegt – so landet man in der Halle oder im Gang, nicht auf dem Dach oder im Keller.
     */
    private static @Nullable BlockPos inside(ServerLevel level, BlockPos c, BoundingBox box, Pass pass) {
        int reach = Math.min(24, Math.max(box.getXSpan(), box.getZSpan()) / 2);
        int middle = (box.minY() + box.maxY()) / 2;
        for (int r = 0; r <= reach; r += 2) {
            int steps = Math.max(1, r * 3);
            for (int k = 0; k < steps; k++) {
                double a = k * 2.0 * Math.PI / steps;
                int x = c.getX() + (int) Math.round(Math.sin(a) * r);
                int z = c.getZ() + (int) Math.round(Math.cos(a) * r);
                if (x < box.minX() || x > box.maxX() || z < box.minZ() || z > box.maxZ()) {
                    continue;
                }
                level.getChunk(x >> 4, z >> 4);
                BlockPos best = null;
                BlockPos.MutableBlockPos feet = new BlockPos.MutableBlockPos(x, 0, z);
                for (int y = Math.min(box.maxY() + 1, level.getMaxY() - 1); y >= Math.max(box.minY() + 1, level.getMinY() + 1); y--) {
                    feet.setY(y);
                    if ((best == null || Math.abs(y - middle) < Math.abs(best.getY() - middle)) && fits(level, feet, pass, null)) {
                        best = feet.immutable();
                    }
                }
                if (best != null) {
                    return best;
                }
            }
        }
        return null;
    }

    /** Ein sicherer Stand nahe (x, z): erst genau dort, dann ringsum (bis 12 Blöcke). */
    private static @Nullable BlockPos nearColumn(ServerLevel level, int x, int z, @Nullable BoundingBox box) {
        for (Pass pass : PASSES) {
            for (int r = 0; r <= 12; r += 2) {
                int steps = Math.max(1, r * 3);
                for (int k = 0; k < steps; k++) {
                    double a = k * 2.0 * Math.PI / steps;
                    BlockPos found = column(level, x + (int) Math.round(Math.sin(a) * r), z + (int) Math.round(Math.cos(a) * r), box,
                            new Pass(pass.wet(), Math.min(pass.open(), 5)), null);
                    if (found != null) {
                        return found;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Der Stand in einer Säule: unter freiem Himmel die Oberfläche (bis 48 Blöcke darunter, nicht tiefer als der Rahmen), in
     * Welten mit Decke (Nether) der höchste Boden in der Höhe des Bauwerks. Im Wasser (nur wenn erlaubt): bei einem bekannten
     * Bauwerk der Grund neben ihm (man taucht hin), sonst die Oberfläche.
     */
    public static @Nullable BlockPos column(ServerLevel level, int x, int z, @Nullable BoundingBox box, boolean wet) {
        return column(level, x, z, box, new Pass(wet, 0), null);
    }

    private static @Nullable BlockPos column(ServerLevel level, int x, int z, @Nullable BoundingBox box, Pass pass, @Nullable BlockPos target) {
        level.getChunk(x >> 4, z >> 4);
        if (level.dimensionType().hasCeiling()) {
            int ceiling = level.getMinY() + level.dimensionType().logicalHeight() - 3;
            int top = box != null ? Math.min(box.maxY() + 2, ceiling) : ceiling;
            int bottom = box != null ? box.minY() - 8 : level.getMinY() + 1;
            return scan(level, x, z, top, bottom, pass, target);
        }
        int surface = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        int bottom = Math.max(surface - 48, box != null ? box.minY() - 8 : level.getMinY() + 1);
        if (surface <= level.getMinY() + 1 || surface < bottom) {
            return null;
        }
        // wählerisch: nur unter freiem Himmel (nicht unter einem Dach, nicht in einer Höhle)
        BlockPos found = scan(level, x, z, surface, pass.open() > 0 ? surface : bottom, new Pass(false, pass.open()), target);
        if (found != null || !pass.wet()) {
            return found;
        }
        if (box != null) {
            // zu einem Bauwerk unter Wasser hinabtauchen (bis an seinen Grund)
            found = scan(level, x, z, surface, Math.max(box.minY() - 8, surface - 160), pass, target);
            if (found != null) {
                return found;
            }
        }
        FluidState top = level.getFluidState(new BlockPos(x, surface - 1, z));
        if (!top.isEmpty() && !top.is(FluidTags.LAVA)) {
            return new BlockPos(x, surface, z);
        }
        return box != null ? null : scan(level, x, z, surface, bottom, pass, target);
    }

    /** Der höchste sichere Stand (Füße) zwischen {@code top} und {@code bottom}. */
    private static @Nullable BlockPos scan(ServerLevel level, int x, int z, int top, int bottom, Pass pass, @Nullable BlockPos target) {
        top = Math.min(top, level.getMaxY() - 1);
        bottom = Math.max(bottom, level.getMinY() + 1);
        BlockPos.MutableBlockPos feet = new BlockPos.MutableBlockPos(x, top, z);
        for (int y = top; y >= bottom; y--) {
            feet.setY(y);
            if (fits(level, feet, pass, target)) {
                return feet.immutable();
            }
        }
        return null;
    }

    /** Passt der Stand zum Durchgang: sicher, frei genug und (wählerisch, mit Ziel) mit freiem Blick dorthin? */
    private static boolean fits(ServerLevel level, BlockPos feet, Pass pass, @Nullable BlockPos target) {
        return standable(level, feet, pass.wet()) && (pass.open() == 0 || openness(level, feet) >= pass.open() && clearView(level, feet, target));
    }

    /** Wie frei man steht: wie viele der acht Nachbarn auf Fuß- und Kopfhöhe ohne Hindernis sind. */
    private static int openness(ServerLevel level, BlockPos feet) {
        int free = 0;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                BlockPos side = feet.offset(dx, 0, dz);
                if (level.getBlockState(side).getCollisionShape(level, side).isEmpty()
                        && level.getBlockState(side.above()).getCollisionShape(level, side.above()).isEmpty()) {
                    free++;
                }
            }
        }
        return free;
    }

    /** Freier Blick zum Ziel: die nächsten vier Blöcke in seiner Richtung (auf Augenhöhe) sind frei – kein Baum, keine Wand. */
    private static boolean clearView(ServerLevel level, BlockPos feet, @Nullable BlockPos target) {
        if (target == null) {
            return true;
        }
        double dx = target.getX() - feet.getX();
        double dz = target.getZ() - feet.getZ();
        double length = Math.sqrt(dx * dx + dz * dz);
        if (length < 5.0) {
            return true;
        }
        for (int step = 1; step <= 4; step++) {
            BlockPos eye = BlockPos.containing(feet.getX() + 0.5 + dx / length * step, feet.getY() + 1.5, feet.getZ() + 0.5 + dz / length * step);
            if (!level.getBlockState(eye).getCollisionShape(level, eye).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Fester Boden ohne Glut (auch Stufen und Treppen), darüber zwei Blöcke Platz ohne Lava, Feuer und Pulverschnee (Wasser nur
     * mit {@code wet}).
     */
    public static boolean standable(ServerLevel level, BlockPos feet, boolean wet) {
        BlockPos below = feet.below();
        BlockState floor = level.getBlockState(below);
        // fester Boden: mindestens eine halbe Stufe hoch (Stufen und Treppen zählen), nicht höher als ein Block (kein Zaun)
        net.minecraft.world.phys.shapes.VoxelShape shape = floor.getCollisionShape(level, below);
        if (shape.isEmpty() || shape.max(Direction.Axis.Y) < 0.5 || shape.max(Direction.Axis.Y) > 1.0 || floor.is(BlockTags.FIRE)
                || floor.is(BlockTags.CAMPFIRES) || floor.is(Blocks.MAGMA_BLOCK) || floor.is(Blocks.CACTUS) || floor.getFluidState().is(FluidTags.LAVA)) {
            return false;
        }
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos pos = feet.above(dy);
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty() || state.is(BlockTags.FIRE) || state.is(Blocks.POWDER_SNOW)
                    || state.is(Blocks.SWEET_BERRY_BUSH) || state.is(Blocks.COBWEB) || state.is(Blocks.WITHER_ROSE)) {
                return false;
            }
            FluidState fluid = state.getFluidState();
            if (fluid.is(FluidTags.LAVA) || (!fluid.isEmpty() && !wet)) {
                return false;
            }
        }
        return true;
    }

    /**
     * Prüft einen Landeplatz, bevor man reist (auch die festen Plätze: das Gelände dort entsteht dafür): Steht man nicht sicher,
     * wird ringsum gesucht – erst dicht daneben und ein paar Blöcke höher oder tiefer, dann auf dem Boden in der Nähe.
     */
    public static Landing safe(ServerLevel level, Landing landing) {
        BlockPos feet = BlockPos.containing(landing.x(), landing.y() + 0.01, landing.z());
        if (standable(level, feet, true) || swimming(level, feet)) {
            return landing;
        }
        int[] heights = {0, 1, -1, 2, -2, 3, -3, 4, -4, 5, 6};
        for (Pass pass : PASSES) {
            for (int r = 0; r <= 6; r++) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                            continue;
                        }
                        for (int dy : heights) {
                            BlockPos at = feet.offset(dx, dy, dz);
                            if (fits(level, at, new Pass(pass.wet(), Math.min(pass.open(), 5)), null)) {
                                return new Landing(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, landing.yaw(), landing.pitch());
                            }
                        }
                    }
                }
            }
        }
        BlockPos near = nearColumn(level, feet.getX(), feet.getZ(), null);
        return near != null ? new Landing(near.getX() + 0.5, near.getY(), near.getZ() + 0.5, landing.yaw(), landing.pitch()) : landing;
    }

    /** Mitten im Wasser (Füße und Kopf, ohne Hindernis): dort schwimmt man – auch das ist ein sicherer Platz (Krakenschlund). */
    private static boolean swimming(ServerLevel level, BlockPos feet) {
        for (int dy = 0; dy <= 1; dy++) {
            BlockPos pos = feet.above(dy);
            BlockState state = level.getBlockState(pos);
            if (!state.getCollisionShape(level, pos).isEmpty() || !state.getFluidState().is(FluidTags.WATER)) {
                return false;
            }
        }
        return true;
    }

    /** Wenn gar nichts passt: der sichere Platz von {@code /kaw dim} (im End: ein Stück Boden in der Nähe). */
    private static BlockPos fallback(ServerLevel level, int x, int z) {
        if (level.dimension() == Level.END) {
            BlockPos found = nearColumn(level, x, z, null);
            if (found != null) {
                return found;
            }
        }
        return DimensionCommand.safeSpot(level, x, z);
    }

    /** Im Inneren: Blick in die Richtung, in die man am weitesten sieht (den Gang hinunter, in die Halle hinein). */
    private static Landing widest(ServerLevel level, BlockPos feet) {
        float best = 180.0f;
        int bestReach = -1;
        for (int k = 0; k < 16; k++) {
            float yaw = k * 22.5f;
            double dx = -Mth.sin(yaw * Mth.DEG_TO_RAD);
            double dz = Mth.cos(yaw * Mth.DEG_TO_RAD);
            int reach = 0;
            while (reach < 24) {
                BlockPos eye = BlockPos.containing(feet.getX() + 0.5 + dx * (reach + 1), feet.getY() + 1.5, feet.getZ() + 0.5 + dz * (reach + 1));
                if (!level.getBlockState(eye).getCollisionShape(level, eye).isEmpty()) {
                    break;
                }
                reach++;
            }
            if (reach > bestReach) {
                bestReach = reach;
                best = yaw;
            }
        }
        return new Landing(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, Mth.wrapDegrees(best), 5.0f);
    }

    /** Landeplatz mit Blick auf das Ziel. */
    private static Landing look(BlockPos feet, BlockPos target, float pitch) {
        double dx = target.getX() - feet.getX();
        double dz = target.getZ() - feet.getZ();
        float yaw = dx * dx + dz * dz < 1.0 ? 180.0f : (float) (Mth.atan2(dz, dx) * Mth.RAD_TO_DEG) - 90.0f;
        return new Landing(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5, Mth.wrapDegrees(yaw), pitch);
    }

    /** Vor dem Eingang der Hexengrotte, am Anfang des Pfads, mit Blick auf die Rippen. */
    private static Landing witchGrottoLanding(ServerLevel level, Spot spot) {
        int cx = spot.center().getX();
        int cz = spot.center().getZ();
        WitchCauldronBlockEntity cauldron = cauldron(level, spot);
        int floor = cauldron != null ? cauldron.getBlockPos().getY() - WitchGrottoPiece.CAULDRON_Y
                : spot.knownY() ? spot.center().getY() : level.getHeight(Heightmap.Types.OCEAN_FLOOR, cx, cz);
        Direction door = Direction.SOUTH;
        Holder<Structure> holder = holder(level, ModStructures.WITCH_GROTTO_STRUCTURE);
        StructureStart start = holder == null ? null : start(level, holder.value(), ChunkPos.containing(spot.center()));
        if (start != null && start.getPieces().getFirst() instanceof WitchGrottoPiece piece) {
            door = piece.door();
        }
        int reach = WitchGrottoPiece.REACH - 3;
        return new Landing(cx + 0.5 + door.getStepX() * reach, floor + 4, cz + 0.5 + door.getStepZ() * reach, door.getOpposite().toYRot(), 10.0f);
    }

    /** Ans Ende des Stegs vor dem Haupttor des Seelenpferchs, mit Blick auf den Glockenstuhl. */
    private static Landing soulPenLanding(ServerLevel level, Spot spot) {
        int cx = spot.center().getX();
        int cz = spot.center().getZ();
        SoulBellBlockEntity bell = soulBell(level, spot);
        int floor = bell != null ? bell.getBlockPos().getY() - SoulPenPiece.BELL_Y
                : spot.knownY() ? spot.center().getY() : level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, cx, cz);
        Direction gate = SoulPenPiece.GATE;
        int reach = SoulPenPiece.PATH_END;
        return new Landing(cx + 0.5 + gate.getStepX() * reach, floor, cz + 0.5 + gate.getStepZ() * reach, gate.getOpposite().toYRot(), 5.0f);
    }

    // =====================================================================================
    // Wecken und Zurücksetzen
    // =====================================================================================

    /** Lädt die Chunks um eine Stelle (der Hort muss stehen, bevor man den Boss weckt). */
    public static void load(ServerLevel level, BlockPos center, int radius) {
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                level.getChunk(cx + dx, cz + dz);
            }
        }
    }

    /** Der nächste Block-Entity dieser Art um eine Stelle (Chunks ringweise geladen, die nächsten zuerst). */
    public static <T extends BlockEntity> @Nullable T nearest(ServerLevel level, BlockPos center, int radius, Class<T> type, Predicate<T> filter) {
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int r = 0; r <= radius; r++) {
            T best = null;
            double bestDistance = Double.MAX_VALUE;
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    LevelChunk chunk = level.getChunk(cx + dx, cz + dz);
                    // auch die Block-Entities, die beim Erzeugen nur vorgemerkt wurden (getBlockEntity weckt sie)
                    for (BlockPos pos : chunk.getBlockEntitiesPos()) {
                        BlockEntity entity = level.getBlockEntity(pos);
                        if (type.isInstance(entity) && filter.test(type.cast(entity))) {
                            double distance = entity.getBlockPos().distSqr(center);
                            if (distance < bestDistance) {
                                best = type.cast(entity);
                                bestDistance = distance;
                            }
                        }
                    }
                }
            }
            if (best != null) {
                return best;
            }
        }
        return null;
    }

    /** Sucht einen Block in der Säule (von knapp über der Oberfläche nach unten). */
    private static @Nullable BlockPos findDown(ServerLevel level, int x, int z, Heightmap.Types heightmap, int above, int depth,
            net.minecraft.world.level.block.Block block) {
        int top = level.getHeight(heightmap, x, z);
        for (int y = top + above; y > top - depth; y--) {
            BlockPos pos = new BlockPos(x, y, z);
            if (level.getBlockState(pos).is(block)) {
                return pos;
            }
        }
        return null;
    }

    private static int awakened(CommandSourceStack source, Boss boss) {
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.boss.awakened", boss.name()), true);
        return 1;
    }

    private static int resetDone(CommandSourceStack source, Boss boss) {
        source.sendSuccess(() -> Component.translatable("command.kingdomsatwar.boss.reset", boss.name()), true);
        return 1;
    }

    private static int alreadyAwake(CommandSourceStack source, Boss boss) {
        source.sendFailure(Component.translatable("command.kingdomsatwar.boss.already_awake", boss.name()));
        return 0;
    }

    private static int noLair(CommandSourceStack source, Boss boss) {
        source.sendFailure(Component.translatable("command.kingdomsatwar.boss.no_lair", boss.name(), boss.site().name()));
        return 0;
    }

    private static int cannot(CommandSourceStack source, Boss boss) {
        source.sendFailure(Component.translatable("command.kingdomsatwar.boss.cannot_awaken", boss.name()));
        return 0;
    }

    // ---- Drachen

    private static @Nullable DragonLairCoreBlockEntity lairCore(ServerLevel level, Spot spot, Boss boss) {
        DragonType type = null;
        for (Map.Entry<DragonType, Site> entry : LAIRS.entrySet()) {
            if (entry.getValue() == boss.site()) {
                type = entry.getKey();
            }
        }
        DragonType element = type;
        return element == null ? null : nearest(level, spot.center(), 5, DragonLairCoreBlockEntity.class, core -> core.element() == element);
    }

    private static int awakenDragon(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        DragonLairCoreBlockEntity core = lairCore(level, spot, boss);
        if (core == null) {
            return noLair(source, boss);
        }
        if (core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        if (core.phase() == io.github.sirschrolli.kingdomsatwar.world.LairRecords.Phase.DEFEATED) {
            core.reset(level);
        }
        return core.awaken(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetDragon(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        DragonLairCoreBlockEntity core = lairCore(level, spot, boss);
        if (core == null) {
            return noLair(source, boss);
        }
        core.reset(level);
        return resetDone(source, boss);
    }

    /** Lässt den Drachen vor einem erwachen, wo man gerade steht (zum Ausprobieren; sein Hort ist dort, wo er erwacht). */
    private static int summonDragon(CommandSourceStack source, ServerLevel ignored, Spot spot, Boss boss) throws CommandSyntaxException {
        ServerPlayer player = source.getPlayerOrException();
        ServerLevel level = player.level();
        net.minecraft.world.phys.Vec3 lookDir = player.getLookAngle().multiply(1, 0, 1);
        lookDir = lookDir.lengthSqr() < 1.0e-4 ? new net.minecraft.world.phys.Vec3(0, 0, 1) : lookDir.normalize();
        net.minecraft.world.phys.Vec3 at = player.position().add(lookDir.scale(26));
        int y = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, Mth.floor(at.x), Mth.floor(at.z));
        BlockPos home = BlockPos.containing(at.x, y, at.z);
        boolean nyxar = boss.type().get() == ModEntities.NYXAR.get();
        DragonBossEntity dragon = nyxar ? ModEntities.NYXAR.get().create(level, EntitySpawnReason.COMMAND)
                : ModEntities.PYRRHON.get().create(level, EntitySpawnReason.COMMAND);
        if (dragon == null) {
            return 0;
        }
        dragon.snapTo(home.getX() + 0.5, home.getY() - (nyxar ? 0 : 11), home.getZ() + 0.5, player.getYRot() + 180.0f, 0.0f);
        dragon.yBodyRot = player.getYRot() + 180.0f;
        dragon.yHeadRot = dragon.yBodyRot;
        level.addFreshEntity(dragon);
        dragon.beginAwakening(level, home);
        player.sendSystemMessage(Component.translatable("command.kingdomsatwar.boss.summoned", dragon.getDisplayName()));
        return 1;
    }

    // ---- Nyxar

    private static @Nullable ShadowThroneCoreBlockEntity nyxarCore(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 6, ShadowThroneCoreBlockEntity.class, core -> true);
    }

    private static int awakenNyxar(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ShadowThroneCoreBlockEntity core = nyxarCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        core.reset(level);
        return core.awaken(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetNyxar(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ShadowThroneCoreBlockEntity core = nyxarCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        core.reset(level);
        return resetDone(source, boss);
    }

    // ---- Karkinos und der Kraken

    private static @Nullable AtollCoreBlockEntity atollCore(ServerLevel level, Spot spot) {
        BlockPos pos = AtollLocator.core(spot.center());
        level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return level.getBlockEntity(pos) instanceof AtollCoreBlockEntity core ? core : null;
    }

    private static int awakenKarkinos(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        AtollCoreBlockEntity core = atollCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        if (core.phase() == AtollCoreBlockEntity.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        core.reset(level);
        core.awaken(level);
        return awakened(source, boss);
    }

    private static int resetKarkinos(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        AtollCoreBlockEntity core = atollCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        core.reset(level);
        return resetDone(source, boss);
    }

    private static @Nullable KrakenCoreBlockEntity krakenCore(ServerLevel level, Spot spot) {
        BlockPos pos = KrakenMawLocator.core(spot.center());
        level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return level.getBlockEntity(pos) instanceof KrakenCoreBlockEntity core ? core : null;
    }

    private static int awakenKraken(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        KrakenCoreBlockEntity core = krakenCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        if (core.phase() == KrakenCoreBlockEntity.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        core.reset(level);
        core.awaken(level);
        return awakened(source, boss);
    }

    private static int resetKraken(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        KrakenCoreBlockEntity core = krakenCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        core.reset(level);
        return resetDone(source, boss);
    }

    // ---- Anni der Troll

    private static @Nullable ErrorCoreBlockEntity errorCore(ServerLevel level, Spot spot) {
        load(level, spot.center(), 1);
        return nearest(level, spot.center(), 1, ErrorCoreBlockEntity.class, core -> true);
    }

    private static int awakenAnni(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ErrorCoreBlockEntity core = errorCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        if (core.phase() == ErrorCoreBlockEntity.Phase.RUNNING) {
            return alreadyAwake(source, boss);
        }
        return core.summon(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetAnni(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ErrorCoreBlockEntity core = errorCore(level, spot);
        if (core == null) {
            return noLair(source, boss);
        }
        core.reset(level);
        return resetDone(source, boss);
    }

    // ---- Malleus und Asteria

    private static @Nullable GlutAnvilBlockEntity anvil(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 4, GlutAnvilBlockEntity.class, anvil -> true);
    }

    private static int awakenMalleus(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        GlutAnvilBlockEntity anvil = anvil(level, spot);
        if (anvil == null) {
            return noLair(source, boss);
        }
        if (anvil.phase() == GlutAnvilBlockEntity.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        if (anvil.phase() == GlutAnvilBlockEntity.Phase.DEFEATED) {
            anvil.reset(level);
        }
        return anvil.awaken(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetMalleus(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        GlutAnvilBlockEntity anvil = anvil(level, spot);
        if (anvil == null) {
            return noLair(source, boss);
        }
        anvil.reset(level);
        return resetDone(source, boss);
    }

    private static io.github.sirschrolli.kingdomsatwar.block.nether.@Nullable FlameAltarBlockEntity flameAltar(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 12, io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlockEntity.class, altar -> true);
    }

    private static int awakenIgnara(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        var altar = flameAltar(level, spot);
        if (altar == null) {
            return noLair(source, boss);
        }
        if (altar.phase() == io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlock.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        if (altar.phase() == io.github.sirschrolli.kingdomsatwar.block.nether.FlameAltarBlock.Phase.DEFEATED) {
            altar.reset(level);
        }
        return altar.awaken(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetIgnara(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        var altar = flameAltar(level, spot);
        if (altar == null) {
            return noLair(source, boss);
        }
        altar.reset(level);
        return resetDone(source, boss);
    }

    private static @Nullable StarstoneBlockEntity starstone(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 4, StarstoneBlockEntity.class, stone -> true);
    }

    private static int awakenAsteria(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        StarstoneBlockEntity stone = starstone(level, spot);
        if (stone == null) {
            return noLair(source, boss);
        }
        if (stone.phase() == StarstoneBlockEntity.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        if (stone.phase() == StarstoneBlockEntity.Phase.DEFEATED) {
            stone.reset(level);
        }
        return stone.summon(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetAsteria(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        StarstoneBlockEntity stone = starstone(level, spot);
        if (stone == null) {
            return noLair(source, boss);
        }
        stone.reset(level);
        return resetDone(source, boss);
    }

    // ---- Feenreich: Noctua, MissMystik, der Steinhüter

    private static int awakenNoctua(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        load(level, spot.center(), 1);
        BlockPos cocoon = null;
        for (int r = 0; r <= 2 && cocoon == null; r++) {
            for (int dx = -r; dx <= r && cocoon == null; dx++) {
                for (int dz = -r; dz <= r && cocoon == null; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) == r) {
                        cocoon = findDown(level, spot.center().getX() + dx, spot.center().getZ() + dz, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                                2, 12, FaeBlocks.MOTH_COCOON.get());
                    }
                }
            }
        }
        if (cocoon == null) {
            // der Kokon ist aufgerissen: Fliegt die Königin noch, ist sie wach
            boolean flying = !level.getEntitiesOfClass(io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity.class,
                    new net.minecraft.world.phys.AABB(spot.center()).inflate(96.0, 64.0, 96.0), net.minecraft.world.entity.Entity::isAlive).isEmpty();
            return flying ? alreadyAwake(source, boss) : noLair(source, boss);
        }
        return MothCocoonBlock.awaken(level, cocoon, null) ? awakened(source, boss) : cannot(source, boss);
    }

    private static @Nullable StageHatBlockEntity stageHat(ServerLevel level, Spot spot) {
        return nearest(level, spot.center().offset(0, 0, MystikStageFeature.HAT_DZ), 2, StageHatBlockEntity.class, hat -> true);
    }

    private static int awakenMystik(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        StageHatBlockEntity hat = stageHat(level, spot);
        if (hat == null) {
            return noLair(source, boss);
        }
        if (hat.phase() == StageHatBlockEntity.Phase.SHOW) {
            return alreadyAwake(source, boss);
        }
        return hat.summon(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetMystik(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        StageHatBlockEntity hat = stageHat(level, spot);
        if (hat == null) {
            return noLair(source, boss);
        }
        hat.reset(level);
        return resetDone(source, boss);
    }

    private static @Nullable KeeperStoneBlockEntity keeperStone(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 2, KeeperStoneBlockEntity.class, stone -> true);
    }

    private static int awakenKeeper(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        KeeperStoneBlockEntity stone = keeperStone(level, spot);
        if (stone == null) {
            return noLair(source, boss);
        }
        var keeper = stone.keeper(level);
        if (keeper == null) {
            keeper = stone.spawn(level, true);
        } else {
            keeper.awaken();
        }
        return keeper != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetKeeper(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        KeeperStoneBlockEntity stone = keeperStone(level, spot);
        if (stone == null) {
            return noLair(source, boss);
        }
        stone.reset(level);
        return resetDone(source, boss);
    }

    // ---- Schattenwelt: der Seelenhirte

    private static @Nullable SoulBellBlockEntity soulBell(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 2, SoulBellBlockEntity.class, bell -> true);
    }

    private static int awakenShepherd(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        SoulBellBlockEntity bell = soulBell(level, spot);
        if (bell == null) {
            return noLair(source, boss);
        }
        var shepherd = bell.shepherd(level);
        if (shepherd == null) {
            shepherd = bell.spawn(level, true);
        } else {
            shepherd.awaken();
        }
        return shepherd != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetShepherd(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        SoulBellBlockEntity bell = soulBell(level, spot);
        if (bell == null) {
            return noLair(source, boss);
        }
        bell.reset(level);
        return resetDone(source, boss);
    }

    // ---- Leere: der Zerbrochene König und die Sternenweberin

    private static @Nullable ThroneSealBlockEntity throneSeal(ServerLevel level, Spot spot) {
        BlockPos pos = ThroneLocator.seal(spot.center());
        level.getChunk(pos.getX() >> 4, pos.getZ() >> 4);
        return level.getBlockEntity(pos) instanceof ThroneSealBlockEntity seal ? seal : null;
    }

    private static int awakenKing(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ThroneSealBlockEntity seal = throneSeal(level, spot);
        if (seal == null) {
            return noLair(source, boss);
        }
        if (seal.phase() == ThroneSealBlockEntity.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        seal.reset(level);
        return seal.awaken(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetKing(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ThroneSealBlockEntity seal = throneSeal(level, spot);
        if (seal == null) {
            return noLair(source, boss);
        }
        seal.reset(level);
        return resetDone(source, boss);
    }

    private static @Nullable WeaverNestBlockEntity weaverNest(ServerLevel level, Spot spot) {
        load(level, spot.center(), 3);
        return level.getBlockEntity(StarWebPiece.nest(spot.center())) instanceof WeaverNestBlockEntity nest ? nest : null;
    }

    private static int awakenWeaver(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        WeaverNestBlockEntity nest = weaverNest(level, spot);
        if (nest == null) {
            return noLair(source, boss);
        }
        var weaver = nest.weaver(level);
        if (weaver == null) {
            weaver = nest.spawn(level, true);
        } else {
            weaver.awaken();
        }
        return weaver != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetWeaver(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        WeaverNestBlockEntity nest = weaverNest(level, spot);
        if (nest == null) {
            return noLair(source, boss);
        }
        nest.reset(level);
        return resetDone(source, boss);
    }

    // ---- Abgrund: der Weltenfresser und der Stundenwächter

    private static @Nullable HoardSealBlockEntity hoardSeal(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 3, HoardSealBlockEntity.class, seal -> true);
    }

    private static int awakenWorldEater(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        HoardSealBlockEntity seal = hoardSeal(level, spot);
        if (seal == null) {
            return noLair(source, boss);
        }
        if (seal.phase() == HoardSealBlockEntity.Phase.AWAKE) {
            return alreadyAwake(source, boss);
        }
        seal.reset(level);
        return seal.awaken(level) != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetWorldEater(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        HoardSealBlockEntity seal = hoardSeal(level, spot);
        if (seal == null) {
            return noLair(source, boss);
        }
        seal.reset(level);
        return resetDone(source, boss);
    }

    private static @Nullable ClockHeartBlockEntity clockHeart(ServerLevel level, Spot spot) {
        load(level, spot.center(), 3);
        return level.getBlockEntity(ClockTowerPiece.heart(spot.center())) instanceof ClockHeartBlockEntity heart ? heart : null;
    }

    private static int awakenWarden(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ClockHeartBlockEntity heart = clockHeart(level, spot);
        if (heart == null) {
            return noLair(source, boss);
        }
        var warden = heart.warden(level);
        if (warden == null) {
            warden = heart.spawn(level, true);
        } else {
            warden.awaken();
        }
        return warden != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetWarden(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        ClockHeartBlockEntity heart = clockHeart(level, spot);
        if (heart == null) {
            return noLair(source, boss);
        }
        heart.reset(level);
        return resetDone(source, boss);
    }

    // ---- Urmeer: die Meerhexe

    private static @Nullable WitchCauldronBlockEntity cauldron(ServerLevel level, Spot spot) {
        return nearest(level, spot.center(), 2, WitchCauldronBlockEntity.class, cauldron -> true);
    }

    private static int awakenWitch(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        WitchCauldronBlockEntity cauldron = cauldron(level, spot);
        if (cauldron == null) {
            return noLair(source, boss);
        }
        var witch = cauldron.witch(level);
        if (witch == null) {
            witch = cauldron.spawn(level, true);
        } else {
            witch.awaken();
        }
        return witch != null ? awakened(source, boss) : cannot(source, boss);
    }

    private static int resetWitch(CommandSourceStack source, ServerLevel level, Spot spot, Boss boss) {
        WitchCauldronBlockEntity cauldron = cauldron(level, spot);
        if (cauldron == null) {
            return noLair(source, boss);
        }
        cauldron.reset(level);
        return resetDone(source, boss);
    }

    /** Alle Bauwerke der Mod, für die es einen Ort gibt (für die Prüfung, dass keins fehlt). */
    public static List<ResourceKey<Structure>> coveredStructures() {
        List<ResourceKey<Structure>> keys = new ArrayList<>();
        for (Site site : SITES.values()) {
            if (site.structure() != null) {
                keys.add(site.structure());
            }
        }
        return keys;
    }
}
