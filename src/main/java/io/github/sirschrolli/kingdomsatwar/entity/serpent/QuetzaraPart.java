package io.github.sirschrolli.kingdomsatwar.entity.serpent;

import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.entity.PartEntity;
import org.jspecify.annotations.Nullable;

/**
 * Eine Trefferzone Quetzaras: der Kopf (voller Schaden – die verwundbarste Stelle) oder ein Glied ihres Leibs (die Schuppen
 * halten viel ab). Treffer gehen mit dem Schadensfaktor der Zone an die Schlange.
 */
public class QuetzaraPart extends PartEntity<QuetzaraEntity> {
    /** −1: der Kopf; 0 … {@link QuetzaraEntity#SEGMENTS}-1: Glieder von vorn nach hinten; {@link QuetzaraEntity#SEGMENTS}: der Schwanz. */
    public final int index;
    public final float damageFactor;
    private final EntityDimensions size;

    public QuetzaraPart(QuetzaraEntity parent, int index, float width, float height, float damageFactor) {
        super(parent);
        this.index = index;
        this.damageFactor = damageFactor;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
    }

    public boolean isHead() {
        return this.index < 0;
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
    }

    @Override
    public boolean isPickable() {
        return !this.getParent().isCamouflaged() || this.isHead();
    }

    /** Die Zonen gehören fest zur Schlange und werden nie einzeln entfernt. */
    @Override
    public void remove(Entity.RemovalReason reason) {
    }

    @Override
    public @Nullable ItemStack getPickResult() {
        return this.getParent().getPickResult();
    }

    @Override
    public final boolean hurtServer(ServerLevel level, DamageSource source, float damage) {
        return !this.isInvulnerableToBase(source) && this.getParent().hurtFromPart(level, this, source, damage);
    }

    @Override
    public boolean is(Entity other) {
        return this == other || this.getParent() == other;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket(ServerEntity serverEntity) {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
