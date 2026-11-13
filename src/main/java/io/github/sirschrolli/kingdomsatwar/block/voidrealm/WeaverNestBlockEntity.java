package io.github.sirschrolli.kingdomsatwar.block.voidrealm;

import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.DustParticleOptions;
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
 * Wacht über die Sternenweberin: Kommt jemand näher als {@link #SPAWN_RADIUS} Blöcke, hängt sie (in Andacht) über ihrem
 * Gelege – fehlt sie, lässt sie sich aus der Höhe herab. Ist sie besiegt, erlischt das Gelege; ein Himmelseisenbarren darin
 * lockt eine neue, und sie lässt sich sofort fallen. Wer sich dem Netz nähert, hört es von fern singen.
 */
public class WeaverNestBlockEntity extends BlockEntity {
    /** So nah muss jemand kommen, damit die Weberin (in Andacht) über ihrem Gelege hängt. */
    public static final double SPAWN_RADIUS = 48.0;
    private static final double WARN_RADIUS = 30.0;

    private WeaverNestBlock.Phase phase = WeaverNestBlock.Phase.SLEEPING;
    private @Nullable UUID weaver;
    /** Wo die Weberin ihren Platz hat (relativ zum Gelege; sie hängt darüber). */
    private BlockPos homeOffset = BlockPos.ZERO;
    private int missing;
    private final Map<UUID, Long> warned = new HashMap<>();

    public WeaverNestBlockEntity(BlockPos pos, BlockState state) {
        super(StarWeaver.WEAVER_NEST_ENTITY.get(), pos, state);
    }

    /** Legt fest, wo die Weberin ihren Platz hat (für das Bauwerk und Tests). */
    public void setHome(BlockPos home) {
        this.homeOffset = home.subtract(this.worldPosition);
        this.setChanged();
    }

    public BlockPos home() {
        return this.worldPosition.offset(this.homeOffset);
    }

    public WeaverNestBlock.Phase phase() {
        return this.phase;
    }

    /** Gehört diese Weberin zu diesem Gelege? */
    public boolean owns(StarWeaverEntity entity) {
        return entity.getUUID().equals(this.weaver);
    }

    public @Nullable StarWeaverEntity weaver(ServerLevel level) {
        if (this.weaver != null && level.getEntity(this.weaver) instanceof StarWeaverEntity entity && entity.isAlive()) {
            return entity;
        }
        List<StarWeaverEntity> near = level.getEntitiesOfClass(StarWeaverEntity.class, new AABB(this.worldPosition).inflate(44.0),
                e -> e.isAlive() && this.worldPosition.equals(e.nest()));
        if (!near.isEmpty()) {
            this.weaver = near.getFirst().getUUID();
            return near.getFirst();
        }
        return null;
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 20 != 7 || this.phase == WeaverNestBlock.Phase.DEFEATED) {
            return;
        }
        Vec3 c = Vec3.atCenterOf(this.worldPosition);
        Player near = level.getNearestPlayer(c.x, c.y, c.z, SPAWN_RADIUS, e -> e instanceof Player p && !p.isSpectator() && p.isAlive());
        StarWeaverEntity entity = this.weaver(level);
        if (entity == null) {
            if (near == null || level.getDifficulty() == Difficulty.PEACEFUL || !level.isPositionEntityTicking(this.home())) {
                this.missing = 0;
                return;
            }
            // erst nach drei Prüfungen: ihr Chunk könnte gerade erst laden
            if (++this.missing >= 3) {
                this.missing = 0;
                this.spawn(level, false);
            }
            return;
        }
        this.missing = 0;
        this.setPhase(level, entity.isDormant() ? WeaverNestBlock.Phase.SLEEPING : WeaverNestBlock.Phase.AWAKE);
        if (entity.isDormant()) {
            this.warn(level);
        }
    }

    /** Das Netz singt von fern für alle, die sich nähern (höchstens alle 45 Sekunden je Spieler). */
    private void warn(ServerLevel level) {
        long now = level.getGameTime();
        for (ServerPlayer player : level.players()) {
            if (player.isCreative() || player.isSpectator() || player.distanceToSqr(Vec3.atCenterOf(this.worldPosition)) > WARN_RADIUS * WARN_RADIUS) {
                continue;
            }
            Long last = this.warned.get(player.getUUID());
            if (last == null || now - last > 900) {
                this.warned.put(player.getUUID(), now);
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.weaver_nest.warning").withColor(0xB8D4FF));
                level.playSound(null, this.worldPosition, ModSounds.STAR_WEAVER_AMBIENT.get(), SoundSource.HOSTILE, 1.0f, 0.8f);
            }
        }
    }

    /** Setzt die Weberin über das Gelege: in Andacht ({@code awake} = false) oder sofort fallend. Gibt sie zurück (oder null). */
    public @Nullable StarWeaverEntity spawn(ServerLevel level, boolean awake) {
        StarWeaverEntity entity = ModEntities.STAR_WEAVER.get().create(level, EntitySpawnReason.TRIGGERED);
        if (entity == null) {
            return null;
        }
        BlockPos home = this.home();
        if (awake) {
            entity.rise(home, this.worldPosition);
        } else {
            entity.bindTo(home, this.worldPosition);
        }
        level.addFreshEntity(entity);
        this.weaver = entity.getUUID();
        this.setPhase(level, awake ? WeaverNestBlock.Phase.AWAKE : WeaverNestBlock.Phase.SLEEPING);
        if (awake) {
            double y = home.getY() + StarWeaverEntity.HANG;
            level.playSound(null, home, ModSounds.STAR_WEAVER_AWAKEN.get(), SoundSource.HOSTILE, 3.0f, 1.0f);
            level.sendParticles(new DustParticleOptions(0xB8D4FF, 1.4f), home.getX() + 0.5, y + 1.0, home.getZ() + 0.5, 40, 1.2, 1.0, 1.2, 0.0);
            level.sendParticles(ParticleTypes.END_ROD, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 0.5,
                    this.worldPosition.getZ() + 0.5, 30, 0.4, 0.3, 0.4, 0.06);
        }
        this.setChanged();
        return entity;
    }

    /** Ein Himmelseisenbarren im erloschenen Gelege: Eine neue Weberin lässt sich herab. */
    public boolean recall(ServerLevel level, @Nullable Player player) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.weaver_nest.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        if (this.weaver(level) != null) {
            return false;
        }
        return this.spawn(level, true) != null;
    }

    /** Die Weberin ist besiegt: Das Gelege erlischt. */
    public void onWeaverDefeated(ServerLevel level) {
        this.weaver = null;
        this.setPhase(level, WeaverNestBlock.Phase.DEFEATED);
        this.setChanged();
    }

    /** Für Befehle: zurück auf Anfang (die Weberin, wenn da, verschwindet; beim nächsten Besuch hängt eine neue in Andacht). */
    public void reset(ServerLevel level) {
        StarWeaverEntity entity = this.weaver(level);
        if (entity != null) {
            entity.discard();
        }
        this.weaver = null;
        this.missing = 0;
        this.setPhase(level, WeaverNestBlock.Phase.SLEEPING);
        this.setChanged();
    }

    private void setPhase(ServerLevel level, WeaverNestBlock.Phase next) {
        this.phase = next;
        BlockState state = this.getBlockState();
        if (state.hasProperty(WeaverNestBlock.PHASE) && state.getValue(WeaverNestBlock.PHASE) != next) {
            level.setBlock(this.worldPosition, state.setValue(WeaverNestBlock.PHASE, next), 3);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.getSerializedName());
        if (this.weaver != null) {
            output.store("Weaver", UUIDUtil.CODEC, this.weaver);
        }
        output.putLong("HomeOffset", this.homeOffset.asLong());
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        String phase = input.getStringOr("Phase", "sleeping");
        for (WeaverNestBlock.Phase p : WeaverNestBlock.Phase.values()) {
            if (p.getSerializedName().equals(phase)) {
                this.phase = p;
            }
        }
        this.weaver = input.read("Weaver", UUIDUtil.CODEC).orElse(null);
        input.getLong("HomeOffset").ifPresent(v -> this.homeOffset = BlockPos.of(v));
    }
}
