package io.github.sirschrolli.kingdomsatwar.item.armory;

import com.mojang.math.Transformation;
import io.github.sirschrolli.kingdomsatwar.registry.Armory;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Brightness;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Display;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

/**
 * Das Leuchten des Erzspürers: ein Blockbild genau im Erz (ein wenig kleiner, damit es nicht mit dem Block flimmert), mit
 * leuchtendem Umriss in der Farbe des Erzes – der Umriss ist durch Wände zu sehen. Nur der Spieler, der den Spürer benutzt
 * hat, bekommt es zu sehen; nach {@link #LIFETIME} Ticks verlischt es und wird nie gespeichert.
 */
public class OreGlowEntity extends Display.BlockDisplay {
    public static final int LIFETIME = 160;
    private @Nullable UUID owner;

    public OreGlowEntity(EntityType<?> type, Level level) {
        super(type, level);
        this.noPhysics = true;
    }

    /** Ein Leuchten für das Erz an {@code pos}, sichtbar nur für {@code owner}. */
    public static OreGlowEntity create(ServerLevel level, BlockPos pos, BlockState state, int color, ServerPlayer owner) {
        OreGlowEntity glow = new OreGlowEntity(Armory.ORE_GLOW.get(), level);
        glow.owner = owner.getUUID();
        float scale = 0.94f;
        float shift = (1.0f - scale) / 2.0f;
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, level.registryAccess());
        out.store("block_state", BlockState.CODEC, state);
        out.store("transformation", Transformation.EXTENDED_CODEC,
                new Transformation(new Vector3f(shift, shift, shift), null, new Vector3f(scale, scale, scale), null));
        out.putInt("glow_color_override", color);
        out.store("brightness", Brightness.CODEC, new Brightness(15, 15));
        out.putFloat("view_range", 2.0f);
        glow.readAdditionalSaveData(TagValueInput.create(ProblemReporter.DISCARDING, level.registryAccess(), out.buildResult()));
        glow.setPos(pos.getX(), pos.getY(), pos.getZ());
        glow.setGlowingTag(true);
        return glow;
    }

    public @Nullable UUID owner() {
        return this.owner;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide() && this.tickCount > LIFETIME) {
            this.discard();
        }
    }

    @Override
    public boolean broadcastToPlayer(ServerPlayer player) {
        return this.owner == null || this.owner.equals(player.getUUID());
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }
}
