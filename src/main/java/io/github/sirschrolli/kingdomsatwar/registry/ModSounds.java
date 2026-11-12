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
    // Neue Monster (0.69)
    public static final DeferredHolder<SoundEvent, SoundEvent> WEREWOLF_GROWL = register("entity.werewolf.growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> WEREWOLF_HOWL = register("entity.werewolf.howl");
    public static final DeferredHolder<SoundEvent, SoundEvent> WEREWOLF_SNARL = register("entity.werewolf.snarl");
    public static final DeferredHolder<SoundEvent, SoundEvent> WEREWOLF_HURT = register("entity.werewolf.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WEREWOLF_DEATH = register("entity.werewolf.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WEREWOLF_STEP = register("entity.werewolf.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_AMBIENT = register("entity.mire_shambler.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_HURT = register("entity.mire_shambler.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_DEATH = register("entity.mire_shambler.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_STEP = register("entity.mire_shambler.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_THROW = register("entity.mire_shambler.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_GRASP = register("entity.mire_shambler.grasp");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_RISE = register("entity.mire_shambler.rise");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SHAMBLER_SPLAT = register("entity.mire_shambler.splat");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_AMBIENT = register("entity.ember_wyrm.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_HURT = register("entity.ember_wyrm.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_DEATH = register("entity.ember_wyrm.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_SURFACE = register("entity.ember_wyrm.surface");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_SPIT = register("entity.ember_wyrm.spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_BITE = register("entity.ember_wyrm.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_WYRM_DIVE = register("entity.ember_wyrm.dive");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WRAITH_AMBIENT = register("entity.ash_wraith.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WRAITH_HURT = register("entity.ash_wraith.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WRAITH_DEATH = register("entity.ash_wraith.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WRAITH_CLOUD = register("entity.ash_wraith.cloud");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WRAITH_BOLT = register("entity.ash_wraith.bolt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASH_WRAITH_FADE = register("entity.ash_wraith.fade");
    // Monster der Schattenwelt und des Ends (0.71)
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HERON_CALL = register("entity.soul_heron.call");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HERON_STAB = register("entity.soul_heron.stab");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HERON_WINGS = register("entity.soul_heron.wings");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HERON_HURT = register("entity.soul_heron.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HERON_DEATH = register("entity.soul_heron.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_HERON_STEP = register("entity.soul_heron.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_AMBIENT = register("entity.night_ram.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_BLEAT = register("entity.night_ram.bleat");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_SCRAPE = register("entity.night_ram.scrape");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_CHARGE = register("entity.night_ram.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_IMPACT = register("entity.night_ram.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_STOMP = register("entity.night_ram.stomp");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_HURT = register("entity.night_ram.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_DEATH = register("entity.night_ram.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> NIGHT_RAM_STEP = register("entity.night_ram.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_AMBIENT = register("entity.rift_hound.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_HOWL = register("entity.rift_hound.howl");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_PHASE = register("entity.rift_hound.phase");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_BITE = register("entity.rift_hound.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_HURT = register("entity.rift_hound.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_DEATH = register("entity.rift_hound.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> RIFT_HOUND_STEP = register("entity.rift_hound.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_AMBIENT = register("entity.chorus_maw.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_OPEN = register("entity.chorus_maw.open");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_LASH = register("entity.chorus_maw.lash");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_BITE = register("entity.chorus_maw.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_SPIT = register("entity.chorus_maw.spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_TELEPORT = register("entity.chorus_maw.teleport");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_HURT = register("entity.chorus_maw.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> CHORUS_MAW_DEATH = register("entity.chorus_maw.death");

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
    // der Weltenfresser (0.73)
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_AMBIENT = register("entity.world_eater.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_HURT = register("entity.world_eater.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_DEATH = register("entity.world_eater.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_AWAKEN = register("entity.world_eater.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_SHATTER = register("entity.world_eater.shatter");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_ROAR = register("entity.world_eater.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_BITE = register("entity.world_eater.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_INHALE = register("entity.world_eater.inhale");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_SNAP = register("entity.world_eater.snap");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_SPIT = register("entity.world_eater.spit");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_CHUNK = register("entity.world_eater.chunk");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_SWEEP = register("entity.world_eater.sweep");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_BREATH = register("entity.world_eater.breath");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_GRAVITY = register("entity.world_eater.gravity");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_CALL = register("entity.world_eater.call");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_RIFT = register("entity.world_eater.rift");
    public static final DeferredHolder<SoundEvent, SoundEvent> WORLD_EATER_CRUMBLE = register("entity.world_eater.crumble");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_SWEEP = register("entity.shattered_king.sweep");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_GRAVITY = register("entity.shattered_king.gravity");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_BEAM = register("entity.shattered_king.beam");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_RIFT = register("entity.shattered_king.rift");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_ERUPT = register("entity.shattered_king.erupt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SHATTERED_KING_QUAKE = register("entity.shattered_king.quake");

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
    // der Kraken (0.91)
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_AMBIENT = register("entity.kraken.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_HURT = register("entity.kraken.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_DEATH = register("entity.kraken.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_RISE = register("entity.kraken.rise");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_ROAR = register("entity.kraken.roar");
    // Das Meervolk (seit 0.93): Summen und Singen der Meerjungfrauen, die tiefen Stimmen der Tritonwachen
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_AMBIENT = register("entity.mermaid.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_TRADE = register("entity.mermaid.trade");
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_YES = register("entity.mermaid.yes");
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_NO = register("entity.mermaid.no");
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_HURT = register("entity.mermaid.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MERMAID_DEATH = register("entity.mermaid.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRITON_AMBIENT = register("entity.triton.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRITON_HURT = register("entity.triton.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> TRITON_DEATH = register("entity.triton.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_WAVE = register("entity.kraken.wave");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SWEEP = register("entity.kraken.sweep");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_GRAB = register("entity.kraken.grab");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_BITE = register("entity.kraken.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_INK = register("entity.kraken.ink");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_WHIRL = register("entity.kraken.whirl");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SLAM = register("entity.kraken.slam");
    // seit 1.00: er schwimmt, rammt, taucht ab und bricht hervor, lässt Geysire steigen und Arme aus dem Wasser brechen
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_SWIM = register("entity.kraken.swim");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_CHARGE = register("entity.kraken.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_DIVE = register("entity.kraken.dive");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_BREACH = register("entity.kraken.breach");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_GEYSER = register("entity.kraken.geyser");
    public static final DeferredHolder<SoundEvent, SoundEvent> KRAKEN_TENTACLE = register("entity.kraken.tentacle");
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
    // der Steinhüter (0.96)
    // die Meerhexe (0.97)
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_AMBIENT = register("entity.sea_witch.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_HURT = register("entity.sea_witch.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_DEATH = register("entity.sea_witch.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_AWAKEN = register("entity.sea_witch.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_LAUGH = register("entity.sea_witch.laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_SONG = register("entity.sea_witch.song");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_CAST = register("entity.sea_witch.cast");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_INK = register("entity.sea_witch.ink");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_INK_HIT = register("entity.sea_witch.ink_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_WHIRLPOOL = register("entity.sea_witch.whirlpool");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_BUBBLE = register("entity.sea_witch.bubble");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_SUMMON = register("entity.sea_witch.summon");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_CORAL = register("entity.sea_witch.coral");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_CURSE = register("entity.sea_witch.curse");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_TAIL = register("entity.sea_witch.tail");
    public static final DeferredHolder<SoundEvent, SoundEvent> SEA_WITCH_FURY = register("entity.sea_witch.fury");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_AMBIENT = register("entity.soul_shepherd.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_HURT = register("entity.soul_shepherd.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_DEATH = register("entity.soul_shepherd.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_STEP = register("entity.soul_shepherd.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_AWAKEN = register("entity.soul_shepherd.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_BELL = register("entity.soul_shepherd.bell");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_HOOK = register("entity.soul_shepherd.hook");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_LANTERN = register("entity.soul_shepherd.lantern");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_WISPS = register("entity.soul_shepherd.wisps");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_WISP_HIT = register("entity.soul_shepherd.wisp_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_FOG = register("entity.soul_shepherd.fog");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_PEN = register("entity.soul_shepherd.pen");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_CRASH = register("entity.soul_shepherd.crash");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_STORM = register("entity.soul_shepherd.storm");
    public static final DeferredHolder<SoundEvent, SoundEvent> SOUL_SHEPHERD_FURY = register("entity.soul_shepherd.fury");
    // die Sternenweberin (0.99)
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_AMBIENT = register("entity.star_weaver.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_HURT = register("entity.star_weaver.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_DEATH = register("entity.star_weaver.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_STEP = register("entity.star_weaver.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_AWAKEN = register("entity.star_weaver.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_THREAD = register("entity.star_weaver.thread");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_WEB = register("entity.star_weaver.web");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_CONSTELLATION = register("entity.star_weaver.constellation");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_STARFALL = register("entity.star_weaver.starfall");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_COCOON = register("entity.star_weaver.cocoon");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_BROOD = register("entity.star_weaver.brood");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_DROP = register("entity.star_weaver.drop");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_BITE = register("entity.star_weaver.bite");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_SPINDLE = register("entity.star_weaver.spindle");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_FURY = register("entity.star_weaver.fury");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WEAVER_SPIDERLING = register("entity.star_weaver.spiderling");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_AMBIENT = register("entity.stone_keeper.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_HURT = register("entity.stone_keeper.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_DEATH = register("entity.stone_keeper.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_STEP = register("entity.stone_keeper.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_AWAKEN = register("entity.stone_keeper.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_ROAR = register("entity.stone_keeper.roar");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_SMASH = register("entity.stone_keeper.smash");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_STOMP = register("entity.stone_keeper.stomp");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_DIG = register("entity.stone_keeper.dig");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_THROW = register("entity.stone_keeper.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_BOULDER = register("entity.stone_keeper.boulder");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_CRYSTAL = register("entity.stone_keeper.crystal");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_BEAM_CHARGE = register("entity.stone_keeper.beam_charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_BEAM = register("entity.stone_keeper.beam");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_ARMOR = register("entity.stone_keeper.armor");
    public static final DeferredHolder<SoundEvent, SoundEvent> STONE_KEEPER_SHATTER = register("entity.stone_keeper.shatter");
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
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_BLACK_HOLE = register("entity.asteria.black_hole");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_COLLAPSE = register("entity.asteria.collapse");
    public static final DeferredHolder<SoundEvent, SoundEvent> ASTERIA_CRESCENT = register("entity.asteria.crescent");
    public static final DeferredHolder<SoundEvent, SoundEvent> STAR_WINGS_BOOST = register("item.star_wings.boost");
    // MissMystik, die Feen-Zauberkünstlerin (0.66)
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_AMBIENT = register("entity.miss_mystik.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_LAUGH = register("entity.miss_mystik.laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_HURT = register("entity.miss_mystik.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_DEATH = register("entity.miss_mystik.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_DRUMROLL = register("entity.miss_mystik.drumroll");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_TADA = register("entity.miss_mystik.tada");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_APPLAUSE = register("entity.miss_mystik.applause");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_CARD_THROW = register("entity.miss_mystik.card_throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_CARD_HIT = register("entity.miss_mystik.card_hit");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_DOVES = register("entity.miss_mystik.doves");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_VANISH = register("entity.miss_mystik.vanish");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_REAPPEAR = register("entity.miss_mystik.reappear");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_MIRRORS = register("entity.miss_mystik.mirrors");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_HAT_THROW = register("entity.miss_mystik.hat_throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_VORTEX = register("entity.miss_mystik.vortex");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_SWORDS = register("entity.miss_mystik.swords");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_STAB = register("entity.miss_mystik.stab");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_SPOTLIGHT = register("entity.miss_mystik.spotlight");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_CARD_TRICK = register("entity.miss_mystik.card_trick");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIK_FIREWORK = register("entity.miss_mystik.firework");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIC_DECK_THROW = register("item.mystic_deck.throw");
    public static final DeferredHolder<SoundEvent, SoundEvent> MYSTIC_HAT_VANISH = register("item.mystic_top_hat.vanish");
    // Anni der Troll (0.67)
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_AMBIENT = register("entity.anni_troll.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_LAUGH = register("entity.anni_troll.laugh");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_HURT = register("entity.anni_troll.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_DEATH = register("entity.anni_troll.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_STEP = register("entity.anni_troll.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_BOOT = register("entity.anni_troll.boot");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_DISCONNECT = register("entity.anni_troll.disconnect");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_WINDUP = register("entity.anni_troll.windup");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_SMASH = register("entity.anni_troll.smash");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_LAG = register("entity.anni_troll.lag");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_TELEPORT = register("entity.anni_troll.teleport");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_MISS = register("entity.anni_troll.miss");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_CLONE = register("entity.anni_troll.clone");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_CLONE_CRASH = register("entity.anni_troll.clone_crash");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_CLONE_POP = register("entity.anni_troll.clone_pop");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_MISSING = register("entity.anni_troll.missing");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_RAIN = register("entity.anni_troll.rain");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_PIXEL = register("entity.anni_troll.pixel");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_ROLLBACK_MARK = register("entity.anni_troll.rollback_mark");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_ROLLBACK = register("entity.anni_troll.rollback");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_SCAN = register("entity.anni_troll.scan");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_CRASH = register("entity.anni_troll.crash");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_RESTART = register("entity.anni_troll.restart");
    public static final DeferredHolder<SoundEvent, SoundEvent> ANNI_REVIVE = register("entity.anni_troll.revive");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERROR_CORE_HUM = register("block.error_core.hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERROR_CORE_ON = register("block.error_core.on");
    public static final DeferredHolder<SoundEvent, SoundEvent> ERROR_CORE_OFF = register("block.error_core.off");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANNHAMMER_JOLT = register("item.bannhammer.jolt");
    public static final DeferredHolder<SoundEvent, SoundEvent> BANNHAMMER_BAN = register("item.bannhammer.ban");
    public static final DeferredHolder<SoundEvent, SoundEvent> GLITCH_WINGS_BLINK = register("item.glitch_wings.blink");

    // ---------------------------------------------------------------- Schrolli der Schöpfer (0.75)
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_AMBIENT = register("entity.schrolli.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_HURT = register("entity.schrolli.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_DEATH = register("entity.schrolli.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_AWAKEN = register("entity.schrolli.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_ASCEND = register("entity.schrolli.ascend");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_VOICE = register("entity.schrolli.voice");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_SPEAR = register("entity.schrolli.spear");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_SPEAR_FLY = register("entity.schrolli.spear_fly");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_PILLAR = register("entity.schrolli.pillar");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_CHARGE = register("entity.schrolli.charge");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_BEAM = register("entity.schrolli.beam");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_SMITE = register("entity.schrolli.smite");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_HALO = register("entity.schrolli.halo");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_ORB = register("entity.schrolli.orb");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_BLAST = register("entity.schrolli.blast");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_WINGS = register("entity.schrolli.wings");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_FEATHERS = register("entity.schrolli.feathers");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_BLINK = register("entity.schrolli.blink");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_CHOIR = register("entity.schrolli.choir");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_JUDGMENT = register("entity.schrolli.judgment");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_TIMESTOP = register("entity.schrolli.timestop");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_TIME_RESUME = register("entity.schrolli.time_resume");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_TICK = register("entity.schrolli.tick");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_WORD = register("entity.schrolli.word");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_SUPERNOVA = register("entity.schrolli.supernova");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_PLANET = register("entity.schrolli.planet");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_IMPACT = register("entity.schrolli.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_STAR = register("entity.schrolli.star");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_STUN = register("entity.schrolli.stun");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_HEAL = register("entity.schrolli.heal");
    // seit 0.76: der Nahkampf am Boden (die Klinge aus Licht)
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_BLADE = register("entity.schrolli.blade");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_SWING = register("entity.schrolli.swing");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_SLASH = register("entity.schrolli.slash");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_DASH = register("entity.schrolli.dash");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_WHIRL = register("entity.schrolli.whirl");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_GUARD = register("entity.schrolli.guard");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_PARRY = register("entity.schrolli.parry");
    public static final DeferredHolder<SoundEvent, SoundEvent> SCHROLLI_LAND = register("entity.schrolli.land");
    public static final DeferredHolder<SoundEvent, SoundEvent> SERAPHLING_AMBIENT = register("entity.seraphling.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SERAPHLING_HURT = register("entity.seraphling.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SERAPHLING_DEATH = register("entity.seraphling.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SERAPHLING_SHOOT = register("entity.seraphling.shoot");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATOR_ALTAR_AWAKEN = register("block.creator_altar.awaken");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATOR_ALTAR_HUM = register("block.creator_altar.hum");
    public static final DeferredHolder<SoundEvent, SoundEvent> CREATOR_SCEPTER_CAST = register("item.creator_scepter.cast");

    // ---------------------------------------------------------------- Held
    /** Streicheln (die drei Tätschel-Töne aus PatPat, siehe licenses/patpat/NOTICE.md). */
    public static final DeferredHolder<SoundEvent, SoundEvent> HERO_PAT = register("hero.pat");

    static {
        for (DragonType type : DragonType.values()) {
            DRAGON_BREATH.put(type, register("entity.dragon.breath." + type.id()));
        }
    }

    // ---------------------------------------------------------------- Himmelswacht der Bosse (0.82)
    /** Ins Visier genommen: ein aufsteigender Warnton beim Flieger. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_MARK = register("boss.sky_ward.mark");
    /** Das Siegel über dem Boss lädt sich auf. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_CHARGE = register("boss.sky_ward.charge");
    /** Der Bannpfeil löst sich. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_LAUNCH = register("boss.sky_ward.launch");
    /** Treffer: die Schwingen versagen. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_HIT = register("boss.sky_ward.hit");
    /** Der Bannpfeil verpufft (Ziel gelandet, Wand getroffen, zu alt). */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_FIZZLE = register("boss.sky_ward.fizzle");
    /** Annäherungston in der Warnanzeige, je näher der Pfeil, desto schneller. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_PING = register("boss.sky_ward.ping");
    /** Der Flugbann endet: die Schwingen tragen wieder. */
    public static final DeferredHolder<SoundEvent, SoundEvent> SKY_WARD_RELEASE = register("boss.sky_ward.release");

    // ---------------------------------------------------------------- Tiere der eigenen Biome (0.85)
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_AMBIENT = register("entity.snow_leopard.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_PURR = register("entity.snow_leopard.purr");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_HISS = register("entity.snow_leopard.hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_EAT = register("entity.snow_leopard.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_HURT = register("entity.snow_leopard.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_DEATH = register("entity.snow_leopard.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> SNOW_LEOPARD_STEP = register("entity.snow_leopard.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_AMBIENT = register("entity.primeval_elk.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_BUGLE = register("entity.primeval_elk.bugle");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_ANGRY = register("entity.primeval_elk.angry");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_IMPACT = register("entity.primeval_elk.impact");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_EAT = register("entity.primeval_elk.eat");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_HURT = register("entity.primeval_elk.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_DEATH = register("entity.primeval_elk.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> PRIMEVAL_ELK_STEP = register("entity.primeval_elk.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> STORM_GULL_CALL = register("entity.storm_gull.call");
    public static final DeferredHolder<SoundEvent, SoundEvent> STORM_GULL_HURT = register("entity.storm_gull.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> STORM_GULL_DEATH = register("entity.storm_gull.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> STORM_GULL_FLAP = register("entity.storm_gull.flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> STORM_GULL_STEP = register("entity.storm_gull.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_SCREECH = register("entity.griffin.screech");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_GROWL = register("entity.griffin.growl");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_WINGS = register("entity.griffin.wings");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_HURT = register("entity.griffin.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_DEATH = register("entity.griffin.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_STRIKE = register("entity.griffin.strike");
    public static final DeferredHolder<SoundEvent, SoundEvent> GRIFFIN_STEP = register("entity.griffin.step");
    // Wesen der eigenen Biome (0.86): Glutsalamander, Moorschnapper, Flamingo
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_SALAMANDER_AMBIENT = register("entity.ember_salamander.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_SALAMANDER_HURT = register("entity.ember_salamander.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_SALAMANDER_DEATH = register("entity.ember_salamander.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_SALAMANDER_STEP = register("entity.ember_salamander.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> EMBER_SALAMANDER_IGNITE = register("entity.ember_salamander.ignite");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SNAPPER_AMBIENT = register("entity.mire_snapper.ambient");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SNAPPER_HISS = register("entity.mire_snapper.hiss");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SNAPPER_SNAP = register("entity.mire_snapper.snap");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SNAPPER_HURT = register("entity.mire_snapper.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SNAPPER_DEATH = register("entity.mire_snapper.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> MIRE_SNAPPER_STEP = register("entity.mire_snapper.step");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLAMINGO_CALL = register("entity.flamingo.call");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLAMINGO_HURT = register("entity.flamingo.hurt");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLAMINGO_DEATH = register("entity.flamingo.death");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLAMINGO_FLAP = register("entity.flamingo.flap");
    public static final DeferredHolder<SoundEvent, SoundEvent> FLAMINGO_STEP = register("entity.flamingo.step");

    private ModSounds() {}

    private static DeferredHolder<SoundEvent, SoundEvent> register(String name) {
        return SOUNDS.register(name, id -> SoundEvent.createVariableRangeEvent(id));
    }
}
