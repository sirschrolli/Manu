package io.github.sirschrolli.kingdomsatwar.registry;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageScaling;
import net.minecraft.world.damagesource.DamageType;

/**
 * Eigene Schadensarten (Datenpaket-Einträge, erzeugt von der Datengenerierung) – mit eigenen Todesmeldungen:
 * „… wurde von einer Steinkugel zerschmettert“, „… wurde von einem Ballistenbolzen durchbohrt“, „… wurde vom Rammbock
 * niedergestoßen“.
 */
public final class ModDamageTypes {
    public static final ResourceKey<DamageType> SIEGE_STONE = key("siege_stone");
    public static final ResourceKey<DamageType> BALLISTA_BOLT = key("ballista_bolt");
    public static final ResourceKey<DamageType> BATTERING_RAM = key("battering_ram");

    private ModDamageTypes() {}

    public static void bootstrap(BootstrapContext<DamageType> context) {
        context.register(SIEGE_STONE, new DamageType(KingdomsAtWar.MODID + ".siege_stone", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));
        context.register(BALLISTA_BOLT, new DamageType(KingdomsAtWar.MODID + ".ballista_bolt", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));
        context.register(BATTERING_RAM, new DamageType(KingdomsAtWar.MODID + ".battering_ram", DamageScaling.WHEN_CAUSED_BY_LIVING_NON_PLAYER, 0.1f));
    }

    private static ResourceKey<DamageType> key(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, KingdomsAtWar.id(name));
    }
}
