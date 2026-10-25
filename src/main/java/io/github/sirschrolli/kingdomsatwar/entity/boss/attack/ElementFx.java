package io.github.sirschrolli.kingdomsatwar.entity.boss.attack;

import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import java.util.function.BiConsumer;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;

/**
 * Wie das Element eines Bosses aussieht und wirkt: Haupt- und Nebenpartikel, Geräusch des Odems und die Wirkung
 * auf getroffene Gegner (Feuer: in Brand setzen, Eis: verlangsamen und erstarren, …).
 */
public record ElementFx(ParticleOptions main, ParticleOptions secondary, SoundEvent breathSound,
        BiConsumer<DragonBossEntity, LivingEntity> onHit) {
}
