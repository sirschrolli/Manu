package io.github.sirschrolli.kingdomsatwar.block.fae;

import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den Steinhüter am Grund des Mondsteinbruchs: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, kniet der Hüter
 * (schlafend) an seinem Platz – fehlt er, setzt er sich dort aus dem Geröll zusammen. Ist er besiegt, erlischt der Stein;
 * ein Mondstein darauf ruft einen neuen, der sofort erwacht. Wer sich dem Bruch nähert, hört ein Raunen aus der Tiefe.
 */
public class KeeperStoneBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit der Hüter (schlafend) an seinem Platz ist. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 30.0;

    private KeeperStoneBlock.Phase phase = KeeperStoneBlock.Phase.SLEEPING;
    private @Nullable UUID keeper;
    /** Wo der Hüter kniet (relativ zum Stein). */
    private BlockPos homeOffset = new BlockPos(0, 0, -4);
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public KeeperStoneBlockEntity(BlockPos pos, BlockState state) {
        super(FaeBlocks.KEEPER_STONE_ENTITY.get(), pos, state);
    }

    /** Legt fest, wo der Hüter kniet (für das Bauwerk und Tests). */
    public void setHome(BlockPos home) {
        this.homeOffset = home.subtract(this.worldPosition);
        this.setChanged();
    }

    public BlockPos home() {
        return this.worldPosition.offset(this.homeOffset);
    }

    public KeeperStoneBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört dieser Hüter zu diesem Stein? */
    public boolean owns(StoneKeeperEntity entity) {
        return entity.getUUID().equals(this.keeper);
    }

    public @Nullable StoneKeeperEntity keeper(ServerLevel level) {
        if (this.keeper != null && level.getEntity(this.keeper) instanceof StoneKeeperEntity entity && entity.isAlive()) {
            return entity;
        }
        List<StoneKeeperEntity> near = level.getEntitiesOfClass(StoneKeeperEntity.class, new AABB(this.worldPosition).inflate(40.0),
                e -> e.isAlive() && this.worldPosition.equals(e.lair()));
        if (!near.isEmpty()) {
            this.keeper = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 20 != 7) {
            return;
        }
        if (this.phase == KeeperStoneBlock.Phase.DEFEATED) {
            return;
        }
        Player near = this.nearestPlayer(level, SPAWN_RADIUS);
        StoneKeeperEntity entity = this.keeper(level);
        if (entity == null) {
            if (near == null || level.getDifficulty() == Difficulty.PEACEFUL || !level.isPositionEntityTicking(this.home())) {
                this.missing = 0;
                return;
            }
            // erst nach drei Prüfungen: sein Chunk könnte gerade erst laden
            if (++this.missing >= 3) {
                this.missing = 0;
                this.spawn(level, false);
            }
            return;
        }
        this.missing = 0;
        this.setPhase(level, entity.isDormant() ? KeeperStoneBlock.Phase.SLEEPING : KeeperStoneBlock.Phase.AWAKE);
        if (entity.isDormant()) {
            this.warn(level);
        }
    }

    private @Nullable Player nearestPlayer(ServerLevel level, double radius) {
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        return level.getNearestPlayer(c.x, c.y, c.z, radius, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
    }

    /** Ein Raunen aus der Tiefe für alle, die sich dem Bruch nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.keeper_stone.warning").withColor(0xA8D4FF));
            }
        }
    }

    /**
     * Setzt den Hüter an seinen Platz: schlafend ({@code awake} = false) oder sich aus dem Geröll erhebend. Gibt ihn zurück
     * (oder null, wenn es nicht ging).
     */
    public @Nullable StoneKeeperEntity spawn(ServerLevel level, boolean awake) {
        StoneKeeperEntity entity = ModEntities.STONE_KEEPER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        double dx = this.worldPosition.getX() - home.getX();
        double dz = this.worldPosition.getZ() - home.getZ();
        float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
        entity.snapTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5, yaw, 0.0f);
        entity.setYBodyRot(yaw);
        entity.setYHeadRot(yaw);
        if (awake) {
            entity.assemble(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        level.addFreshEntity(entity);
        this.keeper = entity.getUUID();
        this.setPhase(level, awake ? KeeperStoneBlock.Phase.AWAKE : KeeperStoneBlock.Phase.SLEEPING);
        if (awake) {
            level.playSound(null, home, ModSounds.STONE_KEEPER_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 0.9f);
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, FaeBlocks.FAESTONE.base().get().defaultBlockState()), home.getX() + 0.5,
                    home.getY() + 1.5, home.getZ() + 0.5, 120, 2.0, 1.4, 2.0, 0.2);
            level.sendParticles(ParticleTypes.END_ROD, home.getX() + 0.5, home.getY() + 2.0, home.getZ() + 0.5, 50, 1.4, 1.4, 1.4, 0.06);
        }
        this.setChanged();
        return entity;
    }

    /** Ein Mondstein auf den erloschenen Stein: ein neuer Hüter setzt sich zusammen. */
    public boolean recall(ServerLevel level, @Nullable Player player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.keeper_stone.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        if (this.keeper(level) != null) {
            return false;
        }
        return this.spawn(level, true) != null;
    }

    /** Der Hüter ist besiegt: Der Stein erlischt. */
    public void onKeeperDefeated(ServerLevel level) {
        this.keeper = null;
        this.setPhase(level, KeeperStoneBlock.Phase.DEFEATED);
        this.setChanged();
    }

    /** Für Befehle: zurück auf Anfang (der Hüter, wenn da, verschwindet; beim nächsten Besuch kniet ein neuer). */
    public void reset(ServerLevel level) {
        StoneKeeperEntity entity = this.keeper(level);
        if (entity != null) {
            entity.discard();
        }
        this.keeper = null;
        this.missing = 0;
        this.setPhase(level, KeeperStoneBlock.Phase.SLEEPING);
        this.setChanged();
    }

    private void setPhase(ServerLevel level, KeeperStoneBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(KeeperStoneBlock.PHASE) && state.getValue(KeeperStoneBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(KeeperStoneBlock.PHASE, next), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.keeper != null) {
            output.store("Keeper", UUIDUtil.CODEC, this.keeper);
        }
        output.putLong("HomeOffset", this.homeOffset.asLong());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "sleeping");
        for (KeeperStoneBlock.Phase p : KeeperStoneBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.keeper = input.read("Keeper", UUIDUtil.CODEC).orElse(null);
        input.getLong("HomeOffset").ifPresent(v -> this.homeOffset = BlockPos.of(v));
    }
}
