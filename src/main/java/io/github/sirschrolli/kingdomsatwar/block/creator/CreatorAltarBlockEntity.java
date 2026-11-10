package io.github.sirschrolli.kingdomsatwar.block.creator;

import io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenas;
import io.github.sirschrolli.kingdomsatwar.arena.BossLair;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineFx;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.CreatorBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Altar des Schöpfers (siehe {@link CreatorAltarBlock}): verwahrt den Schöpferfunken, ruft Schrolli den Schöpfer und
 * zieht die Herausforderer in die Schöpfung, seinen Hof. Im Hof steht ein Abbild des Altars vor dem Thron – dort erwacht
 * der Schöpfer, und dort liegt nach dem Sieg seine Beute.
 */
public class CreatorAltarBlockEntity extends BlockEntity implements BossLair {
    /** So lange erwacht der Altar, bevor der Schöpfer kommt. */
    public static final int CHARGE_TICKS = 100;
    /** Wer so nah am Altar steht, wird mitgezogen (siehe BossArenaKind.CREATOR). */
    public static final double CALL_RADIUS = 16.0;

    public enum State { EMPTY, ARMED, CHARGING, AWAKE, DEFEATED }

    private State state = State.EMPTY;
    private int charge;
    private int missing;
    private @Nullable UUID boss;
    private @Nullable Vec3 center;
    private boolean replica;
    /** Im Hof: welche Bühne gerade steht (0 der Hof, 1 das Firmament, 2 das Herz der Sonne – seit 0.78, siehe CreatorStages). */
    private int stage;

    public CreatorAltarBlockEntity(BlockPos pos, BlockState state) {
        super(CreatorBlocks.CREATOR_ALTAR_ENTITY.get(), pos, state);
    }

    public State state() {
        return this.state;
    }

    /** Die Mitte des Kampfs (im Hof: seine Mitte; in der Welt: ein Stück vor dem Altar). */
    public Vec3 center() {
        if (this.center != null) {
            return this.center;
        }
        Direction facing = this.getBlockState().hasProperty(CreatorAltarBlock.FACING) ? this.getBlockState().getValue(CreatorAltarBlock.FACING)
                : Direction.NORTH;
        return Vec3.atBottomCenterOf(this.worldPosition.relative(facing, 10));
    }

    /** Im Hof: wo seine Mitte liegt (der Bauplan des Hofs setzt sie). */
    public void setup(Vec3 center) {
        this.center = center;
        this.setChanged();
    }

    /** Welche Bühne gerade steht (0 der Hof, 1 das Firmament, 2 das Herz der Sonne). */
    public int arenaStage() {
        return this.stage;
    }

    public void setArenaStage(int stage) {
        this.stage = stage;
        this.setChanged();
    }

    public boolean owns(SchrolliEntity entity) {
        return entity.getUUID().equals(this.boss);
    }

    // ------------------------------------------------------------------ Bedienung

    /** Ein Schöpferfunke in den leeren Altar. */
    public boolean insertSpark(ServerLevel level, Player player) {
        if (this.state != State.EMPTY || this.replica) {
            return false;
        }
        this.setState(level, State.ARMED);
        Vec3 c = Vec3.atCenterOf(this.worldPosition).add(0.0, 0.8, 0.0);
        DivineFx.flash(level, c, DivineFx.GOLD);
        DivineFx.sparks(level, c, 40, 0.4, 0.15);
        level.playSound(null, this.worldPosition, ModSounds.CREATOR_ALTAR_HUM.get(), SoundSource.BLOCKS, 1.5f, 1.2f);
        player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.creator_altar.armed").withColor(0xFFD24A));
        return true;
    }

    /** Berühren: rufen, Funken zurücknehmen, mitkämpfen oder ein Hinweis. */
    public void touch(ServerLevel level, Player player) {
        switch (this.state) {
            case EMPTY -> player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.creator_altar.hint").withColor(0xFFF2C4));
            case ARMED -> {
                if (this.replica) {
                    return;
                }
                if (player.isShiftKeyDown()) {
                    this.setState(level, State.EMPTY);
                    ItemStack spark = new ItemStack(CreatorBlocks.CREATOR_SPARK.get());
                    player.getInventory().placeItemBackInInventory(spark, net.minecraft.util.Prediction.SERVER_ONLY);
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.creator_altar.taken").withColor(0xFFF2C4));
                    return;
                }
                if (player.isCreative() || player.isSpectator()) {
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.creator_altar.creative").withColor(0xC8C8C8));
                    return;
                }
                this.charge = 0;
                this.setState(level, State.CHARGING);
                Vec3 c = Vec3.atBottomCenterOf(this.worldPosition);
                DivineFx.fx(level, SpellFxEntity.Kind.PILLAR, c, DivineFx.HOLY, CHARGE_TICKS, 1.2f).param(64.0f).spawn();
                DivineFx.fx(level, SpellFxEntity.Kind.CIRCLE, c.add(0.0, 0.06, 0.0), DivineFx.GOLD, CHARGE_TICKS, (float) CALL_RADIUS).spawn();
                level.playSound(null, this.worldPosition, ModSounds.CREATOR_ALTAR_AWAKEN.get(), SoundSource.BLOCKS, 4.0f, 1.0f);
                for (ServerPlayer p : level.players()) {
                    if (p.distanceToSqr(c) < 48.0 * 48.0) {
                        p.sendSystemMessage(Component.translatable("block.kingdomsatwar.creator_altar.calling").withColor(0xFFD24A));
                    }
                }
            }
            case CHARGING -> player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.creator_altar.coming").withColor(0xFFD24A));
            case AWAKE -> {
                if (!this.replica && BossArenas.busy(level, this.worldPosition) && player instanceof ServerPlayer sp && !sp.isCreative()
                        && !sp.isSpectator()) {
                    BossArenas.join(level, this.worldPosition, sp);
                } else {
                    player.sendOverlayMessage(Component.translatable("block.kingdomsatwar.creator_altar.awake").withColor(0xFFD24A));
                }
            }
            case DEFEATED -> {
            }
        }
    }

    public void serverTick(ServerLevel level) {
        switch (this.state) {
            case CHARGING -> this.tickCharging(level);
            case AWAKE -> this.tickAwake(level);
            default -> {
            }
        }
    }

    private void tickCharging(ServerLevel level) {
        this.charge++;
        Vec3 c = Vec3.atCenterOf(this.worldPosition).add(0.0, 0.8, 0.0);
        if (this.charge % 2 == 0) {
            DivineFx.gather(level, c, 4.0 + this.charge * 0.06, 4);
        }
        if (this.charge % 20 == 0) {
            level.playSound(null, this.worldPosition, ModSounds.CREATOR_ALTAR_HUM.get(), SoundSource.BLOCKS, 2.0f, 0.8f + this.charge / 200.0f);
        }
        if (this.charge < CHARGE_TICKS) {
            return;
        }
        DivineFx.flash(level, c, DivineFx.WHITE);
        // er zieht seine Herausforderer in die Schöpfung (nur in Tests und im Abbild kämpft er hier)
        if (!this.replica && BossArenas.requestPull(level, this.worldPosition)) {
            this.arenaAway(level);
            return;
        }
        this.awaken(level);
    }

    private void tickAwake(ServerLevel level) {
        if (!this.replica && BossArenas.busy(level, this.worldPosition)) {
            // drüben wird gekämpft: wer jetzt an den Altar tritt, folgt den anderen
            if (level.getGameTime() % 10 == 0) {
                Vec3 c = Vec3.atCenterOf(this.worldPosition);
                for (ServerPlayer player : level.players()) {
                    if (!player.isSpectator() && !player.isCreative() && player.isAlive() && player.distanceToSqr(c) < 3.5 * 3.5) {
                        BossArenas.join(level, this.worldPosition, player);
                    }
                }
            }
            this.missing = 0;
            return;
        }
        if (level.getGameTime() % 20 != 0) {
            return;
        }
        Entity entity = this.boss != null ? level.getEntity(this.boss) : null;
        if (entity instanceof SchrolliEntity && entity.isAlive()) {
            this.missing = 0;
            return;
        }
        // der Schöpfer ist fort, ohne besiegt zu sein: der Funke ruht wieder im Altar
        if (++this.missing > 30 && entity == null) {
            this.boss = null;
            this.setState(level, this.replica ? State.ARMED : State.ARMED);
        }
    }

    /** Ruft den Schöpfer hier (im Abbild im Hof, in Tests oder wo es keinen Hof gibt). */
    public @Nullable SchrolliEntity awaken(ServerLevel level) {
        SchrolliEntity entity = ModEntities.SCHROLLI.get().create(level, EntitySpawnReason.STRUCTURE);
        if (entity == null) {
            return null;
        }
        Vec3 c = this.center();
        entity.snapTo(c.x, c.y + 44.0, c.z, 0.0f, 0.0f);
        entity.bindTo(this.worldPosition, c);
        int players = 0;
        for (ServerPlayer player : level.players()) {
            if (player.isAlive() && !player.isSpectator() && !player.isCreative() && player.position().distanceTo(c) < 120.0) {
                players++;
            }
        }
        entity.scaleForChallengers(Math.max(1, players));
        level.addFreshEntity(entity);
        entity.awaken();
        this.boss = entity.getUUID();
        this.missing = 0;
        this.setState(level, State.AWAKE);
        return entity;
    }

    /** Der Schöpfer ist gefallen: im Abbild merkt sich der Altar den Sieg; in der Welt vergeht der Funke. */
    public void onSchrolliDefeated(ServerLevel level) {
        this.boss = null;
        if (this.replica) {
            this.setState(level, State.DEFEATED);
            return;
        }
        this.setState(level, State.EMPTY);
    }

    /** Der Schöpfer ist gegangen (niemand mehr da): der Funke bleibt. */
    public void onSchrolliLeft(ServerLevel level) {
        this.boss = null;
        this.setState(level, State.ARMED);
    }

    private void setState(ServerLevel level, State next) {
        this.state = next;
        this.setChanged();
        CreatorAltarBlock.Phase phase = switch (next) {
            case EMPTY, DEFEATED -> CreatorAltarBlock.Phase.EMPTY;
            case ARMED, CHARGING -> CreatorAltarBlock.Phase.ARMED;
            case AWAKE -> CreatorAltarBlock.Phase.AWAKE;
        };
        BlockState current = level.getBlockState(this.worldPosition);
        if (current.hasProperty(CreatorAltarBlock.PHASE) && current.getValue(CreatorAltarBlock.PHASE) != phase) {
            level.setBlock(this.worldPosition, current.setValue(CreatorAltarBlock.PHASE, phase), 3);
        }
    }

    // ------------------------------------------------------------------ Speichern

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putString("State", this.state.name());
        if (this.boss != null) {
            output.store("Boss", UUIDUtil.CODEC, this.boss);
        }
        if (this.center != null) {
            output.store("Center", Vec3.CODEC, this.center);
        }
        if (this.replica) {
            output.putBoolean("ArenaReplica", true);
        }
        if (this.stage != 0) {
            output.putInt("ArenaStage", this.stage);
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        try {
            this.state = State.valueOf(input.getStringOr("State", State.EMPTY.name()));
        } catch (IllegalArgumentException e) {
            this.state = State.EMPTY;
        }
        // mitten im Erwachen gespeichert: der Funke ruht wieder
        if (this.state == State.CHARGING) {
            this.state = State.ARMED;
        }
        this.boss = input.read("Boss", UUIDUtil.CODEC).orElse(null);
        this.center = input.read("Center", Vec3.CODEC).orElse(null);
        this.replica = input.getBooleanOr("ArenaReplica", false);
        this.stage = input.getIntOr("ArenaStage", 0);
    }

    // =====================================================================================
    // Der Hof des Schöpfers (die Kampfdimension „Die Schöpfung“)
    // =====================================================================================

    @Override
    public BossArenaKind arenaKind() {
        return BossArenaKind.CREATOR;
    }

    @Override
    public BlockPos arenaFocus() {
        return this.worldPosition;
    }

    /** Vor dem Altar (im Hof: auf dem Thronplatz). */
    @Override
    public BlockPos arenaLootSpot() {
        Direction facing = this.getBlockState().hasProperty(CreatorAltarBlock.FACING) ? this.getBlockState().getValue(CreatorAltarBlock.FACING)
                : Direction.NORTH;
        return this.worldPosition.relative(facing, this.replica ? 4 : 2);
    }

    @Override
    public boolean arenaDefeated() {
        return this.state == State.DEFEATED;
    }

    @Override
    public @Nullable LivingEntity arenaBoss(ServerLevel level) {
        return this.boss != null && level.getEntity(this.boss) instanceof SchrolliEntity entity && entity.isAlive() ? entity : null;
    }

    @Override
    public @Nullable LivingEntity arenaAwaken(ServerLevel level) {
        LivingEntity alive = this.arenaBoss(level);
        return alive != null ? alive : this.awaken(level);
    }

    @Override
    public void arenaAway(ServerLevel level) {
        if (this.boss != null && level.getEntity(this.boss) instanceof SchrolliEntity entity) {
            entity.discard();
        }
        this.boss = null;
        this.missing = 0;
        this.setState(level, State.AWAKE);
    }

    /** Der Schöpfer ist in seinem Hof gefallen: der Funke ist verglüht. */
    @Override
    public void arenaVictory(ServerLevel level) {
        this.boss = null;
        this.setState(level, State.EMPTY);
        Vec3 c = Vec3.atCenterOf(this.worldPosition).add(0.0, 0.8, 0.0);
        DivineFx.flash(level, c, DivineFx.WHITE);
        DivineFx.sparks(level, c, 60, 0.6, 0.2);
    }

    /** Unterlegen: der Funke ruht weiter im Altar – man kann es erneut wagen. */
    @Override
    public void arenaReset(ServerLevel level) {
        if (this.boss != null && level.getEntity(this.boss) instanceof SchrolliEntity entity) {
            entity.discard();
        }
        this.boss = null;
        this.missing = 0;
        this.charge = 0;
        this.setState(level, this.state == State.EMPTY ? State.EMPTY : State.ARMED);
    }

    @Override
    public void arenaPrepareReplica(ServerLevel level) {
        this.replica = true;
        this.boss = null;
        this.setState(level, State.ARMED);
    }

    @Override
    public boolean isArenaReplica() {
        return this.replica;
    }
}
