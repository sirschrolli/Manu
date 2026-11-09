package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import java.util.EnumMap;
import java.util.Map;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Alle Geräusche der Mod. Welche Audiodateien dahinterstehen, steht in
 * {@code assets/kingdomsatwar/sounds.json} (viele Mob-Geräusche nutzen Vanilla-Klänge mit eigener Tonhöhe).
 */
public final class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(Registries.SOUND_EVENT, KingdomsAtWar.MODID);

    // ---------------------------------------------------------------- Drachen
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_AMBIENT = register("entity.dragon.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_BABY_AMBIENT = register("entity.dragon.baby_ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_HURT = register("entity.dragon.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_DEATH = register("entity.dragon.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_ROAR = register("entity.dragon.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_FLAP = register("entity.dragon.flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_STEP = register("entity.dragon.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_EAT = register("entity.dragon.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_BITE = register("entity.dragon.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_GROW = register("entity.dragon.grow");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_EGG_CRACK = register("block.dragon_egg.crack");
    public static final DeferredHolder<SoundEvent, SoundEvent> DRAGON_EGG_HATCH = register("block.dragon_egg.hatch");
    public static final Map<DragonType, DeferredHolder<SoundEvent, SoundEvent>> DRAGON_BREATH = new EnumMap<>(DragonType.class);

    // ---------------------------------------------------------------- Soldaten und Banditen
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_HURT = register("entity.soldier.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_DEATH = register("entity.soldier.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOLDIER_ARMOR = register("entity.soldier.armor");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANDIT_AMBIENT = register("entity.bandit.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANDIT_HURT = register("entity.bandit.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANDIT_DEATH = register("entity.bandit.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> COMMAND_BATON = register("item.command_baton.command");

    // ---------------------------------------------------------------- Wilde Kreaturen
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_BOAR_AMBIENT = register("entity.wild_boar.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_BOAR_ANGRY = register("entity.wild_boar.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_BOAR_HURT = register("entity.wild_boar.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_BOAR_DEATH = register("entity.wild_boar.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WILD_BOAR_STEP = register("entity.wild_boar.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_AMBIENT = register("entity.deer.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_HURT = register("entity.deer.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_DEATH = register("entity.deer.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DEER_STEP = register("entity.deer.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_AMBIENT = register("entity.goose.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_HISS = register("entity.goose.hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_HURT = register("entity.goose.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GOOSE_DEATH = register("entity.goose.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_AMBIENT = register("entity.will_o_wisp.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_HURT = register("entity.will_o_wisp.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_DEATH = register("entity.will_o_wisp.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WISP_VANISH = register("entity.will_o_wisp.vanish");
    public static final DeferredHolder<SoundEvent, SoundEvent> TROLL_AMBIENT = register("entity.cave_troll.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TROLL_HURT = register("entity.cave_troll.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TROLL_DEATH = register("entity.cave_troll.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TROLL_STEP = register("entity.cave_troll.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> TROLL_SMASH = register("entity.cave_troll.smash");
    public static final DeferredHolder<SoundEvent, SoundEvent> TROLL_PETRIFY = register("entity.cave_troll.petrify");
    public static final DeferredHolder<SoundEvent, SoundEvent> LINDWORM_AMBIENT = register("entity.lindworm.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> LINDWORM_HURT = register("entity.lindworm.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> LINDWORM_DEATH = register("entity.lindworm.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> LINDWORM_BITE = register("entity.lindworm.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> LINDWORM_SLITHER = register("entity.lindworm.slither");

    public static final DeferredHolder<SoundEvent, SoundEvent> NYXAR_BREATH = register("entity.nyxar.breath");
    public static final DeferredHolder<SoundEvent, SoundEvent> NYXAR_SHRIEK = register("entity.nyxar.shriek");

    // ---------------------------------------------------------------- Wesen der Leere
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_MAW_AMBIENT = register("entity.void_maw.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_MAW_HURT = register("entity.void_maw.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_MAW_DEATH = register("entity.void_maw.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_MAW_INHALE = register("entity.void_maw.inhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> VOID_MAW_BITE = register("entity.void_maw.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_AMBIENT = register("entity.shard_warden.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_HURT = register("entity.shard_warden.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_DEATH = register("entity.shard_warden.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_STEP = register("entity.shard_warden.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_AWAKEN = register("entity.shard_warden.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_CHARGE = register("entity.shard_warden.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_SHOOT = register("entity.shard_warden.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARD_WARDEN_SLAM = register("entity.shard_warden.slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> WARDEN_SHARD_HIT = register("entity.warden_shard.hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTORTLING_AMBIENT = register("entity.distortling.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTORTLING_HURT = register("entity.distortling.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTORTLING_DEATH = register("entity.distortling.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTORTLING_BLINK = register("entity.distortling.blink");
    public static final DeferredHolder<SoundEvent, SoundEvent> DISTORTLING_ATTACK = register("entity.distortling.attack");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_RAY_AMBIENT = register("entity.star_ray.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_RAY_HURT = register("entity.star_ray.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_RAY_DEATH = register("entity.star_ray.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_AMBIENT = register("entity.shattered_king.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_HURT = register("entity.shattered_king.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_DEATH = register("entity.shattered_king.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_AWAKEN = register("entity.shattered_king.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_ROAR = register("entity.shattered_king.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_SWEEP = register("entity.shattered_king.sweep");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_GRAVITY = register("entity.shattered_king.gravity");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_BEAM = register("entity.shattered_king.beam");

    // ---------------------------------------------------------------- Wesen des Meeres
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTA_RAY_HURT = register("entity.manta_ray.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTA_RAY_DEATH = register("entity.manta_ray.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTA_RAY_LEAP = register("entity.manta_ray.leap");
    public static final DeferredHolder<SoundEvent, SoundEvent> MANTA_RAY_SPLASH = register("entity.manta_ray.splash");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_JELLYFISH_AMBIENT = register("entity.glow_jellyfish.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_JELLYFISH_HURT = register("entity.glow_jellyfish.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_JELLYFISH_DEATH = register("entity.glow_jellyfish.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLOW_JELLYFISH_STING = register("entity.glow_jellyfish.sting");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARK_HURT = register("entity.shark.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARK_DEATH = register("entity.shark.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHARK_BITE = register("entity.shark.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGLERFISH_AMBIENT = register("entity.anglerfish.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGLERFISH_HURT = register("entity.anglerfish.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGLERFISH_DEATH = register("entity.anglerfish.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGLERFISH_SNAP = register("entity.anglerfish.snap");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANGLERFISH_BITE = register("entity.anglerfish.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIANT_CRAB_CLACK = register("entity.giant_crab.clack");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIANT_CRAB_PINCH = register("entity.giant_crab.pinch");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIANT_CRAB_HURT = register("entity.giant_crab.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIANT_CRAB_DEATH = register("entity.giant_crab.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GIANT_CRAB_STEP = register("entity.giant_crab.step");
    // Karkinos, der Riffkoloss
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_AMBIENT = register("entity.reef_colossus.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_HURT = register("entity.reef_colossus.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_DEATH = register("entity.reef_colossus.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_ROAR = register("entity.reef_colossus.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_CLACK = register("entity.reef_colossus.clack");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_SLAM = register("entity.reef_colossus.slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_JET = register("entity.reef_colossus.jet");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_BURROW = register("entity.reef_colossus.burrow");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_ERUPT = register("entity.reef_colossus.erupt");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_ROLL = register("entity.reef_colossus.roll");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_STEP = register("entity.reef_colossus.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_TIDE = register("entity.reef_colossus.tide");
    public static final DeferredHolder<SoundEvent, SoundEvent> REEF_COLOSSUS_LANCE = register("entity.reef_colossus.lance");
    // Feenreich (0.63)
    public static final DeferredHolder<SoundEvent, SoundEvent> FAE_PORTAL_AMBIENT = register("block.fae_portal.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAE_PORTAL_LIGHT = register("block.fae_portal.light");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAE_PORTAL_TRAVEL = register("block.fae_portal.travel");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAIRY_AMBIENT = register("entity.fairy.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAIRY_HURT = register("entity.fairy.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAIRY_DEATH = register("entity.fairy.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAIRY_GIFT = register("entity.fairy.gift");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSHROOMLING_AMBIENT = register("entity.mushroomling.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSHROOMLING_HURT = register("entity.mushroomling.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSHROOMLING_DEATH = register("entity.mushroomling.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MUSHROOMLING_PUFF = register("entity.mushroomling.puff");
    public static final DeferredHolder<SoundEvent, SoundEvent> THORN_VOLLEY = register("entity.thorn_wight.volley");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_SCREECH = register("entity.moth_queen.screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_AWAKEN = register("entity.moth_queen.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_GUST = register("entity.moth_queen.gust");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_SCALES = register("entity.moth_queen.scales");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_BEAM_CHARGE = register("entity.moth_queen.beam_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_BEAM = register("entity.moth_queen.beam");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_LULLABY = register("entity.moth_queen.lullaby");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_AMBIENT = register("entity.moth_queen.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_HURT = register("entity.moth_queen.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_DEATH = register("entity.moth_queen.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MOTH_QUEEN_DIVE = register("entity.moth_queen.dive");
    // Malleus, der Glutschmied (0.64)
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_ROAR = register("entity.glutschmied.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_CLANG = register("entity.glutschmied.clang");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_THRUST = register("entity.glutschmied.thrust");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_SLAM = register("entity.glutschmied.slam");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_SPARKS = register("entity.glutschmied.sparks");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_THROW = register("entity.glutschmied.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_GLOB = register("entity.glutschmied.glob");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_CALL = register("entity.glutschmied.call");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_STOKE = register("entity.glutschmied.stoke");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_AMBIENT = register("entity.glutschmied.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_HURT = register("entity.glutschmied.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_DEATH = register("entity.glutschmied.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLUTSCHMIED_STEP = register("entity.glutschmied.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> FAE_ELYTRA_FLAP = register("item.fae_elytra.flap");
    // Asteria, die Sternenreiterin (0.65)
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_CRY = register("entity.asteria.cry");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_DESCENT = register("entity.asteria.descent");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_CHARGE = register("entity.asteria.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_IMPACT = register("entity.asteria.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_STARFALL = register("entity.asteria.starfall");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_STAR_IMPACT = register("entity.asteria.star_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_CONSTELLATION = register("entity.asteria.constellation");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_COMET_THROW = register("entity.asteria.comet_throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_COMET_IMPACT = register("entity.asteria.comet_impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_WINGBEAT = register("entity.asteria.wingbeat");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_SUPERNOVA_CHARGE = register("entity.asteria.supernova_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_SUPERNOVA = register("entity.asteria.supernova");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_AMBIENT = register("entity.asteria.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_HURT = register("entity.asteria.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_DEATH = register("entity.asteria.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WINGS_BOOST = register("item.star_wings.boost");

    // ---------------------------------------------------------------- Held
    /** Streicheln (die drei Tätschel-Töne aus PatPat, siehe licenses/patpat/NOTICE.md). */
    public static final DeferredHolder<SoundEvent, SoundEvent> HERO_PAT = register("hero.pat");

    static {
        for (DragonType type : DragonType.values()) {
            DRAGON_BREATH.put(type, register("entity.dragon.breath." + type.id()));
        }
    }

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, id -> SoundEvent.createVariableRangeEvent(id));
    }
}
