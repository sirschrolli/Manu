package io.github.sirschrolli.kingdomsatwar.entity.soldier;

import io.github.sirschrolli.kingdomsatwar.entity.creature.farm.FarmHorse;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.UUID;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Das Schlachtross eines Lanzenreiters: ein schweres, gesatteltes und gepanzertes Pferd aus den Ställen des Heeres. Reitet
 * ein Soldat darauf, lenkt er es (es scheut nicht und galoppiert beim Lanzenangriff an); Verbündete verletzen es nicht.
 * Fällt der Reiter, bleibt das Ross dem Herrn des Heeres – man kann es selbst reiten.
 */
public class Warhorse extends FarmHorse {
    public Warhorse(EntityType<? extends Warhorse> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return AbstractHorse.createBaseHorseAttributes()
                .add(Attributes.MAX_HEALTH, 40.0)
                .add(Attributes.MOVEMENT_SPEED, 0.34)
                .add(Attributes.JUMP_STRENGTH, 0.75)
                .add(Attributes.ARMOR, 2.0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0.4);
    }

    /**
     * Ein neues Schlachtross für den Reiter {@code rider} an seiner Stelle: gezähmt (dem Herrn des Reiters), gesattelt und
     * im Rossharnisch; ein Rappe (Friese) oder ein Schimmel, je nach Gesicht des Reiters.
     */
    public static @Nullable Warhorse saddleUp(ServerLevel level, SoldierEntity rider) {
        Warhorse horse = ModEntities.WARHORSE.get().create(level, EntitySpawnReason.MOB_SUMMONED);
        if (horse == null) {
            return null;
        }
        horse.snapTo(rider.getX(), rider.getY(), rider.getZ(), rider.getYRot(), 0.0f);
        horse.setYHeadRot(rider.getYRot());
        horse.setYBodyRot(rider.getYRot());
        horse.setBreed(rider.getVariant() % 2 == 0 ? Breed.FRIESE : Breed.SCHIMMEL);
        horse.setTamed(true);
        UUID owner = rider.getOwnerId();
        if (owner != null && level.getPlayerByUUID(owner) instanceof Player player) {
            horse.setOwner(player);
        }
        horse.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
        horse.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.IRON_HORSE_ARMOR));
        horse.setDropChance(EquipmentSlot.SADDLE, 0.0f);
        horse.setDropChance(EquipmentSlot.BODY, 0.0f);
        horse.setHealth(horse.getMaxHealth());
        horse.setPersistenceRequired();
        level.addFreshEntity(horse);
        return horse;
    }

    /** Lenkt es ein Soldat? Dann scheut es nicht und gehorcht dem Reiter. */
    @Override
    public boolean isMobControlled() {
        return this.getFirstPassenger() instanceof Mob;
    }

    /** Beim Lanzenangriff galoppiert es kräftig an. */
    @Override
    public float chargeSpeedModifier() {
        return 1.45f;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    /** Der Reiter (wenn ein Soldat darauf sitzt). */
    public @Nullable SoldierEntity rider() {
        return this.getFirstPassenger() instanceof SoldierEntity soldier ? soldier : null;
    }

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        Entity attacker = source.getEntity();
        SoldierEntity rider = this.rider();
        if (attacker != null && (rider != null && (attacker == rider || rider.isAlliedTo(attacker))
                || this.getOwnerReference() != null && attacker.getUUID().equals(this.getOwnerReference().getUUID())
                || attacker instanceof SoldierEntity soldier && this.getOwnerReference() != null
                && this.getOwnerReference().getUUID().equals(soldier.getOwnerId()))) {
            return false; // kein Beschuss und kein versehentlicher Schlag aus den eigenen Reihen
        }
        return super.hurtServer(level, source, damage);
    }

    /** Solange ein Soldat im Sattel sitzt, lässt sich das Ross weder besteigen noch absatteln. */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.rider() != null) {
            return InteractionResult.PASS;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public boolean canMate(net.minecraft.world.entity.animal.Animal partner) {
        return false;
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(this.getType().getDescriptionId());
    }
}
