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
