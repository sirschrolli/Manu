package io.github.sirschrolli.kingdomsatwar.block.glitch;

import io.github.sirschrolli.kingdomsatwar.entity.glitch.AnniTrollEntity;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den Fehlerkern: Tritt jemand näher als zwölf Blöcke heran, wird Anni geladen – Pixel strömen herbei, und er
 * setzt sich neben dem Kern zusammen. Findet er niemanden mehr, kehrt er in den Kern zurück (der wartet wieder); nach seinem
 * Absturz ist er erloschen, und erst ein Redstoneblock startet ihn neu.
 */
public class ErrorCoreBlockEntity extends BlockEntity {
    public static final double WAKE_RADIUS = 12.0;
    private static final double WARN_RADIUS = 44.0;

    public enum Phase { WAITING, RUNNING, CRASHED }

    private Phase phase = Phase.WAITING;
    private @Nullable UUID troll;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public ErrorCoreBlockEntity(BlockPos pos, BlockState state) {
        super(AnniBlocks.ERROR_CORE_ENTITY.get(), pos, state);
    }

    public Phase phase() {
        return this.phase;
    }

    /** Ob diese Anni die ist, die zu diesem Kern gehört (sonst ist er eine verirrte Kopie). */
    public boolean owns(Entity entity) {
        return this.phase != Phase.CRASHED && entity.getUUID().equals(this.troll);
    }

    public static boolean wakes(BlockPos core, double x, double y, double z) {
        double dx = x - (core.getX() + 0.5);
        double dz = z - (core.getZ() + 0.5);
        double dy = y - core.getY();
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -8.0 && dy < 12.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        switch (this.phase) {
            case WAITING -> this.tickWaiting(level);
            case RUNNING -> this.tickRunning(level);
            case CRASHED -> {
            }
        }
    }

    private @Nullable AnniTrollEntity troll(ServerLevel level) {
        return this.troll != null && level.getEntity(this.troll) instanceof AnniTrollEntity entity && entity.isAlive() ? entity : null;
    }

    private void tickWaiting(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPos c = this.worldPosition;
        for (ServerPlayer player : level.players()) {
            if (!counts(player)) {
                continue;
            }
            if (wakes(c, player.getX(), player.getY(), player.getZ())) {
                this.begin(level);
                return;
            }
            double dx = player.getX() - (c.getX() + 0.5);
            double dz = player.getZ() - (c.getZ() + 0.5);
            double dy = player.getY() - c.getY();
            if (dx * dx + dz * dz < WARN_RADIUS * WARN_RADIUS && dy > -30 && dy < 30) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.error_core.warning").withColor(0x20E8FF));
                }
            }
        }
    }

    /** Lädt Anni (für die Vorführung und Befehle) – sofern der Kern wartet. */
    public @Nullable AnniTrollEntity summon(ServerLevel level) {
        if (this.phase != Phase.WAITING) {
            return this.troll(level);
        }
        return this.begin(level);
    }

    /** Anni wird geladen: Er setzt sich auf einem freien Platz neben dem Kern zusammen. */
    private @Nullable AnniTrollEntity begin(ServerLevel level) {
        AnniTrollEntity entity = ModEntities.ANNI_TROLL.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos c = this.worldPosition;
        Vec3 spot = new Vec3(c.getX() + 0.5, c.getY(), c.getZ() + 0.5 + 4.0);
        for (int[] d : new int[][] {{0, 4}, {4, 0}, {0, -4}, {-4, 0}, {3, 3}, {-3, 3}, {3, -3}, {-3, -3}}) {
            boolean found = false;
            for (int dy = -2; dy <= 3 && !found; dy++) {
                Vec3 at = new Vec3(c.getX() + 0.5 + d[0], c.getY() + dy, c.getZ() + 0.5 + d[1]);
                entity.setPos(at.x, at.y, at.z);
                if (level.noCollision(entity) && !level.noCollision(entity, entity.getBoundingBox().move(0, -0.2, 0))) {
                    spot = at;
                    found = true;
                }
            }
            if (found) {
                break;
            }
        }
        Player nearest = level.getNearestPlayer(c.getX() + 0.5, c.getY(), c.getZ() + 0.5, 48.0, false);
        float yaw = nearest == null ? 0.0f : (float) Math.toDegrees(Math.atan2(nearest.getZ() - spot.z, nearest.getX() - spot.x)) - 90.0f;
        entity.snapTo(spot.x, spot.y, spot.z, yaw, 0.0f);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);
        entity.bindTo(c);
        entity.load();
        level.addFreshEntity(entity);
        this.troll = entity.getUUID();
        this.missing = 0;
        this.setPhase(level, Phase.RUNNING);
        level.sendParticles(ModParticles.GLITCH_PIXEL.get(), c.getX() + 0.5, c.getY() + 1.0, c.getZ() + 0.5, 40, 0.5, 0.6, 0.5, 0.1);
        return entity;
    }

    private void tickRunning(ServerLevel level) {
        AnniTrollEntity entity = this.troll(level);
        if (entity != null) {
            this.missing = 0;
            return;
        }
        // fort, ohne besiegt zu sein (etwa Chunk entladen und Welt neu geladen): nach einer Weile wartet der Kern wieder
        if (++this.missing > 60) {
            this.troll = null;
            this.missing = 0;
            this.setPhase(level, Phase.WAITING);
        }
    }

    /** Er hat niemanden mehr gefunden und ist in den Kern zurückgekehrt: der wartet wieder. */
    public void onTrollDeparted(ServerLevel level) {
        if (this.phase == Phase.CRASHED) {
            return;
        }
        this.troll = null;
        this.setPhase(level, Phase.WAITING);
    }

    public void onTrollDefeated(ServerLevel level) {
        this.troll = null;
        this.setPhase(level, Phase.CRASHED);
        level.playSound(null, this.worldPosition, ModSounds.ERROR_CORE_OFF.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
    }

    /** Ein Redstoneblock an den erloschenen Kern: Er startet neu, und Anni wird sofort wieder geladen. */
    public boolean restart(ServerLevel level, @Nullable Player player) {
        if (this.phase != Phase.CRASHED) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.error_core.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = Phase.WAITING;
        this.setState(level, ErrorCoreBlock.Phase.WAITING);
        level.playSound(null, this.worldPosition, ModSounds.ERROR_CORE_ON.get(), SoundSource.BLOCKS, 2.0f, 1.0f);
        return this.begin(level) != null;
    }

    /** Setzt den Kern zurück (für Befehle und Tests); eine Anni verschwindet. */
    public void reset(ServerLevel level) {
        AnniTrollEntity entity = this.troll(level);
        if (entity != null) {
            entity.discard();
        }
        this.troll = null;
        this.missing = 0;
        this.setPhase(level, Phase.WAITING);
    }

    private void setPhase(ServerLevel level, Phase phase) {
        this.phase = phase;
        this.setState(level, switch (phase) {
            case WAITING -> ErrorCoreBlock.Phase.WAITING;
            case RUNNING -> ErrorCoreBlock.Phase.RUNNING;
            case CRASHED -> ErrorCoreBlock.Phase.CRASHED;
        });
        this.setChanged();
    }

    private void setState(ServerLevel level, ErrorCoreBlock.Phase phase) {
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.hasProperty(ErrorCoreBlock.PHASE) && state.getValue(ErrorCoreBlock.PHASE) != phase) {
            level.setBlock(this.worldPosition, state.setValue(ErrorCoreBlock.PHASE, phase), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.name());
        if (this.troll != null) {
            output.store("Troll", UUIDUtil.CODEC, this.troll);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            this.phase = Phase.valueOf(input.getStringOr("Phase", Phase.WAITING.name()));
        } catch (IllegalArgumentException e) {
            this.phase = Phase.WAITING;
        }
        this.troll = input.read("Troll", UUIDUtil.CODEC).orElse(null);
    }
}
