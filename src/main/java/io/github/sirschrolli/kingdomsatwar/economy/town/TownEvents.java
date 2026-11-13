package io.github.sirschrolli.kingdomsatwar.economy.town;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.economy.Reputation;
import io.github.sirschrolli.kingdomsatwar.economy.citizen.CitizenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.BanditEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.boss.shadow.NyxarEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.monster.illager.AbstractIllager;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;

/**
 * Die Siedlung in der Welt: Zombies, Plünderer und Banditen greifen Bürger an (wie Dorfbewohner); wer Monster in der
 * eigenen Siedlung besiegt oder einen Drachen bezwingt, gewinnt Ansehen.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class TownEvents {
    /** Wie nah man einem fallenden Drachen sein muss, um am Sieg teilzuhaben. */
    private static final double DRAGON_RANGE = 128.0;

    private TownEvents() {}

    @SubscribeEvent
    static void onEntityJoin(EntityJoinLevelEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Entity entity = event.getEntity();
        if (entity instanceof Zombie || entity instanceof AbstractIllager || entity instanceof BanditEntity) {
            Mob mob = (Mob) entity;
            mob.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(mob, CitizenEntity.class, false));
        }
    }

    @SubscribeEvent
    static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level)) {
            return;
        }
        if (dead instanceof DragonBossEntity) {
            int points = dead instanceof NyxarEntity ? Reputation.NYXAR : Reputation.DRAGON;
            for (ServerPlayer player : level.players()) {
                if (player.distanceToSqr(dead) < DRAGON_RANGE * DRAGON_RANGE && !player.isSpectator()) {
                    Reputation.add(player, points);
                }
            }
            return;
        }
        if (dead instanceof Enemy && event.getSource().getEntity() instanceof ServerPlayer player) {
            Town town = Towns.of(level.getServer()).byOwner(player.getUUID());
            if (town != null && town.contains(level.dimension(), dead.blockPosition())) {
                Reputation.add(player, Reputation.DEFEND);
            }
        }
    }
}
