package io.github.sirschrolli.kingdomsatwar.entity.deep;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlockEntity;
import io.github.sirschrolli.kingdomsatwar.item.deep.ListenerArmor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.VanillaGameEvent;
import net.neoforged.neoforge.event.level.NoteBlockEvent;

/**
 * Das Gehör von Sordin, dem Kantor der Stille: Lärm in der Nähe der Orgel weckt ihn (wie den Tiefenhüter: wer schleicht,
 * dessen Schritte hört er nicht, und die Wollsohlen der Lauscher verschlucken sie ganz); ein Notenblock, der in seiner Nähe
 * erklingt, weckt ihn – oder lässt ihn im Kampf taumeln.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class SordinEvents {
    /** So weit hört er eine Orgel (einen Notenblock). */
    public static final double ORGAN_RADIUS = 40.0;

    private SordinEvents() {}

    /** Lärm: alles, was ein Sculk-Sensor hört, wenn es ein Spieler verursacht. Erst nach den Wollsohlen (die ihn verschlucken). */
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onGameEvent(VanillaGameEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !(event.getCause() instanceof Player player)
                || !event.getVanillaEvent().is(GameEventTags.VIBRATIONS)) {
            return;
        }
        if (player.isSteppingCarefully() && event.getVanillaEvent().is(GameEventTags.IGNORE_VIBRATIONS_SNEAKING)
                || ListenerArmor.hasBoots(player) && ListenerArmor.muffled(event.getVanillaEvent())) {
            return;
        }
        OrganDeskBlockEntity.hear(level, event.getEventPosition(), player);
    }

    /** Ein Notenblock erklingt: Jeder Kantor in Hörweite erwacht oder taumelt. */
    @SubscribeEvent
    static void onNote(NoteBlockEvent.Play event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }
        for (SordinEntity cantor : level.getEntitiesOfClass(SordinEntity.class, new AABB(event.getPos()).inflate(ORGAN_RADIUS),
                e -> e.isAlive() && e.distanceToSqr(net.minecraft.world.phys.Vec3.atCenterOf(event.getPos())) <= ORGAN_RADIUS * ORGAN_RADIUS)) {
            cantor.hearOrgan(level);
        }
    }
}
