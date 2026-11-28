package io.github.sirschrolli.kingdomsatwar.entity;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

/**
 * Unsichtbarer Träger der Lichteffekte im Erzähl-Intro (Schattensäule, Strahlen der Drachen, Siegel am Boden, Splitter,
 * Lichtsäulen am Horizont). Es gibt ihn nur auf dem eigenen Bildschirm, solange das Intro läuft – der Server erzeugt ihn
 * nie, und er wird nicht gespeichert. Gezeichnet wird er von {@code IntroVisionRenderer}.
 */
public class IntroVisionEntity extends Entity {
    public IntroVisionEntity(EntityType<? extends IntroVisionEntity> type, Level level) {
        super(type, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData(net.minecraft.network.syncher.SynchedEntityData.Builder builder) {}

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    /** Die Effekte reichen weit über das Land: aus jeder Entfernung zeichnen. */
    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {}

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {}
}
