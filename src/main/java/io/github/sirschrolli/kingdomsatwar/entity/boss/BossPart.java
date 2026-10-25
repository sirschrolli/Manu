package io.github.sirschrolli.kingdomsatwar.entity.boss;

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
 * Eine Trefferzone eines Drachen-Bosses (Kopf, Hals, Körper, Schwanz, Flügel). Treffer werden mit dem
 * Schadensfaktor der Zone an den Boss weitergegeben – der Kopf ist die verwundbarste Stelle.
 */
public class BossPart extends PartEntity<DragonBossEntity> {
    public final String name;
    public final float damageFactor;
    private final EntityDimensions size;

    public BossPart(DragonBossEntity parent, String name, float width, float height, float damageFactor) {
        super(parent);
        this.name = name;
        this.damageFactor = damageFactor;
        this.size = EntityDimensions.scalable(width, height);
        this.refreshDimensions();
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
        return true;
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
