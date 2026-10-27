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
