package io.github.sirschrolli.kingdomsatwar.client.armor;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.item.nether.EmberwalkerArmor;
import net.minecraft.client.renderer.fog.FogData;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.material.FogType;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ViewportEvent;

/**
 * Glutblick (seit 1.1.6): Mit dem Helm der Glutwandler sieht man in der Lava weit – statt einer Armlänge (Minecraft: einen
 * Block, mit Feuerresistenz fünf) {@link #SIGHT} Blöcke, mit Feuerresistenz {@link #SIGHT_RESISTANT}.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class EmberwalkerSight {
    public static final float SIGHT = 18.0f;
    public static final float SIGHT_RESISTANT = 30.0f;

    private EmberwalkerSight() {}

    @SubscribeEvent
    static void onRenderFog(ViewportEvent.RenderFog event) {
        if (event.getType() != FogType.LAVA) {
            return;
        }
        Entity entity = event.getCamera().entity();
        if (!EmberwalkerArmor.hasHelmet(entity) || entity.isSpectator()) {
            return;
        }
        float far = entity instanceof LivingEntity living && living.hasEffect(MobEffects.FIRE_RESISTANCE) ? SIGHT_RESISTANT : SIGHT;
        event.setNearPlaneDistance(0.0f);
        event.setFarPlaneDistance(far);
        FogData fog = event.getFogData();
        fog.skyEnd = far;
        fog.cloudEnd = far;
    }
}
