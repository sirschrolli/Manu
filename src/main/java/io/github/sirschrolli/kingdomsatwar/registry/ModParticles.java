package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Eigene Partikel: große, animierte Odem-Wolken der Drachen-Bosse (Feuer, Frost, Funken, Sporen, Staub, Gischt), Schlotrauch, Meeresleuchten. */
public final class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES = DeferredRegister.create(Registries.PARTICLE_TYPE, KingdomsAtWar.MODID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_FLAME = simple("boss_flame");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_FROST = simple("boss_frost");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_SPARK = simple("boss_spark");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_SPORE = simple("boss_spore");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_DUST = simple("boss_dust");
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_WATER = simple("boss_water");
    /** Schattenrauch (Nyxar): violett glimmender, schwerer Dunst. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BOSS_SHADOW = simple("boss_shadow");
    /** Die Hand beim Streicheln (folgt dem Kopf des Wesens; als Befehl nur ein stehendes Bild). */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> PAT_HAND = simple("pat_hand");
    /** Schwarzer Rauch eines heißen Schlots am Meeresgrund. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> VENT_SMOKE = simple("vent_smoke");
    /** Meeresleuchten: winzige, türkis leuchtende Punkte im Kielwasser – nachts im Meer. */
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SEA_GLOW = simple("sea_glow");

    private ModParticles() {}

    private static DeferredHolder<ParticleType<?>, SimpleParticleType> simple(String name) {
        return PARTICLE_TYPES.register(name, () -> new SimpleParticleType(true));
    }
}
