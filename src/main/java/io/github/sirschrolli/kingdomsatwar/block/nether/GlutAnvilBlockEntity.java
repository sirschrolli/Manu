package io.github.sirschrolli.kingdomsatwar.block.nether;

import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ForgeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
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
import org.jspecify.annotations.Nullable;

/**
 * Wacht über den Glutamboss: Kommt jemand in die Nähe der Esse, kniet Malleus schon am Amboss und schmiedet (unverwundbar,
 * er bemerkt niemanden). Tritt jemand näher als sieben Blöcke heran – oder greift ihn an –, erwacht er, nimmt den
 * Glutspeer vom Amboss und kämpft. Findet er niemanden mehr, kehrt er an den Amboss zurück und schmiedet weiter (geheilt).
 * Nach seinem Sieg erkaltet der Amboss; ein Netheritbarren facht ihn wieder an und ruft den Schmied zurück.
 */
public class GlutAnvilBlockEntity extends BlockEntity implements io.github.sirschrolli.kingdomsatwar.arena.BossLair {
    /** So nah (waagerecht) am Amboss erwacht er. */
    public static final double WAKE_RADIUS = 7.0;
    /** Ab dieser Nähe kniet er schon am Amboss und schmiedet. */
    private static final double WORK_RADIUS = 48.0;
    private static final double WARN_RADIUS = 40.0;

    public enum Phase { DORMANT, AWAKE, DEFEATED }

    private Phase phase = Phase.DORMANT;
    private @Nullable UUID smith;
    private int missing;
    /** Ein Abbild in der Kampfdimension des Schmieds (der Glutesse)? */
    private boolean replica;
    private final Map<UUID, Long> warned = new HashMap<>();

    public GlutAnvilBlockEntity(BlockPos pos, BlockState state) {
        super(ForgeBlocks.GLUT_ANVIL_ENTITY.get(), pos, state);
    }

    public Phase phase() {
        return this.phase;
    }

    /** Ob dieser Schmied der ist, der zu diesem Amboss gehört (sonst ist er ein verirrter Doppelgänger). */
    public boolean owns(Entity entity) {
        return this.phase != Phase.DEFEATED && entity.getUUID().equals(this.smith);
    }

    /** Wohin der Amboss blickt: dort kniet der Schmied. */
    public Direction facing() {
        BlockState state = this.getBlockState();
        return state.hasProperty(GlutAnvilBlock.FACING) ? state.getValue(GlutAnvilBlock.FACING) : Direction.SOUTH;
    }

    /** Der Platz des Schmieds vor dem Amboss. */
    public BlockPos workplace() {
        return this.worldPosition.relative(this.facing(), 2);
    }

    public static boolean wakes(BlockPos anvil, double x, double y, double z) {
        double dx = x - (anvil.getX() + 0.5);
        double dz = z - (anvil.getZ() + 0.5);
        double dy = y - anvil.getY();
        return dx * dx + dz * dz <= WAKE_RADIUS * WAKE_RADIUS && dy > -3.0 && dy < 8.0;
    }

    private static boolean counts(Player player) {
        return !player.isSpectator() && !player.isCreative() && player.isAlive();
    }

    public void serverTick(ServerLevel level) {
        if (level.getGameTime() % 10 != 0) {
            return;
        }
        switch (this.phase) {
            case DORMANT -> this.tickDormant(level);
            case AWAKE -> this.tickAwake(level);
            case DEFEATED -> {
            }
        }
    }

    private @Nullable GlutschmiedEntity smith(ServerLevel level) {
        return this.smith != null && level.getEntity(this.smith) instanceof GlutschmiedEntity entity && entity.isAlive() ? entity : null;
    }

    private void tickDormant(ServerLevel level) {
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            return;
        }
        BlockPos a = this.worldPosition;
        boolean near = false;
        for (ServerPlayer player : level.players()) {
            if (!counts(player)) {
                continue;
            }
            if (wakes(a, player.getX(), player.getY(), player.getZ())) {
                // er zieht seine Gegner in die Glutesse (nur in Tests und im Abbild kämpft er hier)
                if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
                    this.arenaAway(level);
                    return;
                }
                this.awaken(level);
                return;
            }
            double dx = player.getX() - (a.getX() + 0.5);
            double dz = player.getZ() - (a.getZ() + 0.5);
            double dy = player.getY() - a.getY();
            double d2 = dx * dx + dz * dz;
            if (d2 < WORK_RADIUS * WORK_RADIUS && dy > -60 && dy < 30) {
                near = true;
            }
            if (d2 < WARN_RADIUS * WARN_RADIUS && dy > -50 && dy < 20) {
                long now = level.getGameTime();
                Long last = this.warned.get(player.getUUID());
                if (last == null || now - last > 900) {
                    this.warned.put(player.getUUID(), now);
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.glut_anvil.warning").withColor(0xFF9A3A));
                }
            }
        }
        // jemand kommt näher: der Schmied kniet schon am Amboss und arbeitet
        if (near && this.smith(level) == null) {
            this.missing++;
            if (this.smith == null || this.missing > 6) {
                this.spawn(level, false);
            }
        } else {
            this.missing = 0;
        }
    }

    /** Ruft den Schmied herbei (für die Vorführung): kniend am Amboss, schmiedend – sofern er nicht schon da ist. */
    public @Nullable GlutschmiedEntity summon(ServerLevel level) {
        if (this.phase != Phase.DORMANT) {
            return null;
        }
        GlutschmiedEntity entity = this.smith(level);
        return entity != null ? entity : this.spawn(level, false);
    }

    /** Ruft den Schmied herbei: kniend am Amboss, schmiedend ({@code fight}: gleich erwachend). */
    private @Nullable GlutschmiedEntity spawn(ServerLevel level, boolean fight) {
        GlutschmiedEntity entity = ModEntities.GLUTSCHMIED.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        BlockPos place = this.workplace();
        float yaw = this.facing().getOpposite().toYRot();
        entity.snapTo(place.getX() + 0.5, place.getY(), place.getZ() + 0.5, yaw, 0.0f);
        entity.yBodyRot = yaw;
        entity.yHeadRot = yaw;
        entity.bindTo(place, this.worldPosition);
        level.addFreshEntity(entity);
        this.smith = entity.getUUID();
        this.missing = 0;
        level.sendParticles(ParticleTypes.FLAME, place.getX() + 0.5, place.getY() + 1.0, place.getZ() + 0.5, 50, 0.6, 1.2, 0.6, 0.04);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, place.getX() + 0.5, place.getY() + 1.5, place.getZ() + 0.5, 20, 0.5, 1.0, 0.5, 0.02);
        if (fight) {
            entity.awaken();
            this.setPhase(level, Phase.AWAKE);
        }
        this.setChanged();
        return entity;
    }

    /** Lässt ihn erwachen (auch für Tests und Befehle): er richtet sich auf, nimmt den Speer vom Amboss und kämpft. */
    public @Nullable GlutschmiedEntity awaken(ServerLevel level) {
        if (this.phase == Phase.DEFEATED) {
            return null;
        }
        GlutschmiedEntity entity = this.smith(level);
        if (entity == null) {
            return this.spawn(level, true);
        }
        entity.awaken();
        this.setPhase(level, Phase.AWAKE);
        return entity;
    }

    /** Der Schmied hat den Speer vom Amboss genommen (während er erwacht). */
    public void onSpearTaken(ServerLevel level) {
        this.setState(level, GlutAnvilBlock.Phase.AWAKE);
        level.playSound(null, this.worldPosition, SoundEvents.ANVIL_PLACE, SoundSource.HOSTILE, 0.7f, 0.6f);
        level.sendParticles(ParticleTypes.LAVA, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5, 8, 0.3, 0.1, 0.3, 0.0);
    }

    private void tickAwake(ServerLevel level) {
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.busy(level, this.worldPosition)) {
            // drüben in der Glutesse wird gekämpft: wer jetzt an den Amboss tritt, folgt den anderen
            this.missing = 0;
            for (ServerPlayer player : level.players()) {
                if (counts(player) && wakes(this.worldPosition, player.getX(), player.getY(), player.getZ())) {
                    io.github.sirschrolli.kingdomsatwar.arena.BossArenas.join(level, this.worldPosition, player);
                }
            }
            return;
        }
        GlutschmiedEntity entity = this.smith(level);
        if (entity != null) {
            this.missing = 0;
            return;
        }
        // fort, ohne besiegt zu sein (etwa Chunk entladen und Welt neu geladen): nach einer Weile wieder ruhend
        if (++this.missing > 60) {
            this.smith = null;
            this.missing = 0;
            this.setPhase(level, Phase.DORMANT);
        }
    }

    /** Der Schmied hat niemanden mehr gefunden und ist an den Amboss zurückgekehrt: der Speer liegt wieder darauf. */
    public void onSmithReturned(ServerLevel level, GlutschmiedEntity entity) {
        if (this.phase == Phase.DEFEATED) {
            return;
        }
        this.smith = entity.getUUID();
        this.setPhase(level, Phase.DORMANT);
    }

    public void onSmithDefeated(ServerLevel level) {
        this.smith = null;
        this.setPhase(level, Phase.DEFEATED);
        level.playSound(null, this.worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 2.0f, 0.6f);
        level.sendParticles(ParticleTypes.LARGE_SMOKE, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5, 30, 0.4, 0.3, 0.4, 0.03);
    }

    /** Facht den erkalteten Amboss mit einem Netheritbarren neu an und ruft den Schmied zurück. */
    public boolean rekindle(ServerLevel level, @Nullable Player player) {
        if (this.phase != Phase.DEFEATED) {
            return false;
        }
        if (level.getDifficulty() == Difficulty.PEACEFUL) {
            if (player != null) {
                player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.glut_anvil.peaceful").withStyle(ChatFormatting.GRAY));
            }
            return false;
        }
        this.phase = Phase.DORMANT;
        this.setState(level, GlutAnvilBlock.Phase.DORMANT);
        level.playSound(null, this.worldPosition, ModSounds.GLUTSCHMIED_STOKE.get(), SoundSource.BLOCKS, 2.0f, 1.2f);
        level.sendParticles(ParticleTypes.FLAME, this.worldPosition.getX() + 0.5, this.worldPosition.getY() + 1.0,
                this.worldPosition.getZ() + 0.5, 40, 0.4, 0.4, 0.4, 0.06);
        if (!this.replica && io.github.sirschrolli.kingdomsatwar.arena.BossArenas.requestPull(level, this.worldPosition)) {
            this.arenaAway(level);
            return true;
        }
        return this.spawn(level, true) != null;
    }

    /** Setzt den Amboss zurück (für Befehle und Tests); ein Schmied verschwindet. */
    public void reset(ServerLevel level) {
        GlutschmiedEntity entity = this.smith(level);
        if (entity != null) {
            entity.discard();
        }
        this.smith = null;
        this.missing = 0;
        this.setPhase(level, Phase.DORMANT);
    }

    private void setPhase(ServerLevel level, Phase phase) {
        this.phase = phase;
        // der Speer liegt auf dem Amboss, bis der Schmied ihn beim Erwachen aufnimmt
        if (phase != Phase.AWAKE) {
            this.setState(level, phase == Phase.DEFEATED ? GlutAnvilBlock.Phase.COLD : GlutAnvilBlock.Phase.DORMANT);
        }
        this.setChanged();
    }

    private void setState(ServerLevel level, GlutAnvilBlock.Phase phase) {
        BlockState state = level.getBlockState(this.worldPosition);
        if (state.hasProperty(GlutAnvilBlock.PHASE) && state.getValue(GlutAnvilBlock.PHASE) != phase) {
            level.setBlock(this.worldPosition, state.setValue(GlutAnvilBlock.PHASE, phase), Block.UPDATE_ALL);
        }
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("Phase", this.phase.name());
        if (this.smith != null) {
            output.store("Smith", UUIDUtil.CODEC, this.smith);
        }
        if (this.replica) {
            output.putBoolean("ArenaReplica", true);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            this.phase = Phase.valueOf(input.getStringOr("Phase", Phase.DORMANT.name()));
        } catch (IllegalArgumentException e) {
            this.phase = Phase.DORMANT;
        }
        this.smith = input.read("Smith", UUIDUtil.CODEC).orElse(null);
        this.replica = input.getBooleanOr("ArenaReplica", false);
    }

    // =====================================================================================
    // Die Kampfdimension (die Glutesse)
    // =====================================================================================

    @Override
    public io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind arenaKind() {
        return io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind.MALLEUS;
    }

    @Override
    public BlockPos arenaFocus() {
        return this.worldPosition;
    }

    /** Wo der Schmied sonst kniet: auf dem Dach vor dem Amboss. */
    @Override
    public BlockPos arenaLootSpot() {
        return this.workplace();
    }

    @Override
    public boolean arenaDefeated() {
        return this.phase == Phase.DEFEATED;
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaBoss(ServerLevel level) {
        return this.smith(level);
    }

    @Override
    public net.minecraft.world.entity.@Nullable LivingEntity arenaAwaken(ServerLevel level) {
        return this.awaken(level);
    }

    /** Der Schmied ist in seiner Glutesse: Wer hier noch kniete, ist fort, und der Speer liegt nicht mehr auf dem Amboss. */
    @Override
    public void arenaAway(ServerLevel level) {
        GlutschmiedEntity entity = this.smith(level);
        if (entity != null) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, entity.getX(), entity.getY(1.0), entity.getZ(), 30, 0.5, 0.8, 0.5, 0.03);
            entity.discard();
        }
        this.smith = null;
        this.missing = 0;
        this.setPhase(level, Phase.AWAKE);
        this.setState(level, GlutAnvilBlock.Phase.AWAKE);
    }

    @Override
    public void arenaVictory(ServerLevel level) {
        this.onSmithDefeated(level);
    }

    @Override
    public void arenaReset(ServerLevel level) {
        this.reset(level);
    }

    @Override
    public void arenaPrepareReplica(ServerLevel level) {
        this.replica = true;
        this.reset(level);
    }

    @Override
    public boolean isArenaReplica() {
        return this.replica;
    }
}
