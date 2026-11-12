package io.github.sirschrolli.kingdomsatwar.entity.merfolk;

import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.stats.Stats;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.SmoothSwimmingLookControl;
import net.minecraft.world.entity.ai.control.SmoothSwimmingMoveControl;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Eine Meerjungfrau (seit 0.93): lebt in den Meeresdörfern des Urmeers, schwimmt (wassertretend aufrecht, schnell
 * waagrecht wie ein Delfin), geht ihrem Beruf nach und tauscht gegen Perlen. Vor Haien, Ertrunkenen und dem Kraken
 * weicht sie zurück; wer sie angreift, bekommt es mit den Tritonwachen zu tun. Sie atmet unter Wasser.
 */
public class MermaidEntity extends AbstractVillager implements MerfolkLike {
    private static final EntityDataAccessor<Integer> DATA_PROFESSION = SynchedEntityData.defineId(MermaidEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_LOOK = SynchedEntityData.defineId(MermaidEntity.class, EntityDataSerializers.INT);
    /** Handelt gerade (für die Haltung im Modell, auch auf dem Client bekannt). */
    private static final EntityDataAccessor<Boolean> DATA_TRADING = SynchedEntityData.defineId(MermaidEntity.class, EntityDataSerializers.BOOLEAN);
    /** So oft (Ticks) füllen sich die Angebote wieder auf. */
    private static final int RESTOCK_INTERVAL = 6000;

    public final Merfolk.SwimMotion motion = new Merfolk.SwimMotion();
    private int restockCooldown = RESTOCK_INTERVAL;
    private boolean professionChosen;
    private boolean lookChosen;

    public MermaidEntity(EntityType<? extends MermaidEntity> type, Level level) {
        super(type, level);
        this.moveControl = new SmoothSwimmingMoveControl<>(this, 85, 10, 0.02f, 0.1f, false);
        this.lookControl = new SmoothSwimmingLookControl(this, 10);
        this.setPathfindingMalus(PathType.WATER, 0.0f);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 1.0)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFESSION, MermaidProfession.PEARL_DIVER.ordinal());
        builder.define(DATA_LOOK, MermaidLook.CORAL.ordinal());
        builder.define(DATA_TRADING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Mob.class, 10.0f, 1.0, 1.2,
                entity -> entity.is(ModTags.EntityTypes.MERFOLK_THREATS)));
        this.goalSelector.addGoal(1, new PanicGoal(this, 1.2));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.8));
        this.goalSelector.addGoal(5, new RandomSwimmingGoal(this, 0.7, 50));
        this.goalSelector.addGoal(8, new InteractGoal(this, Player.class, 5.0f, 1.0f));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    // =====================================================================================
    // Schwimmen
    // =====================================================================================

    @Override
    protected PathNavigation createNavigation(Level level) {
        return new WaterBoundPathNavigation(this, level);
    }

    @Override
    protected void travelInWater(Vec3 input, double baseGravity, boolean isFalling, double oldY) {
        Merfolk.swim(this, input);
    }

    @Override
    public boolean canBreatheUnderwater() {
        return true;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public float swimPose(float partialTicks) {
        return this.motion.pose(partialTicks);
    }

    @Override
    public float swimPhase(float partialTicks) {
        return this.motion.phase(partialTicks);
    }

    // =====================================================================================
    // Beruf und Gestalt
    // =====================================================================================

    @Override
    public MermaidProfession profession() {
        return MermaidProfession.byId(this.entityData.get(DATA_PROFESSION));
    }

    public void setProfession(MermaidProfession profession) {
        this.entityData.set(DATA_PROFESSION, profession.ordinal());
        this.professionChosen = true;
        this.offers = null;
    }

    @Override
    public MermaidLook look() {
        return MermaidLook.byId(this.entityData.get(DATA_LOOK));
    }

    public void setLook(MermaidLook look) {
        this.entityData.set(DATA_LOOK, look.ordinal());
        this.lookChosen = true;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(this.getType().getDescriptionId() + "." + this.profession().id());
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        if (!this.professionChosen) {
            this.setProfession(MermaidProfession.randomTrader(this.random));
        }
        if (!this.lookChosen) {
            this.setLook(MermaidLook.forBiome(level.getBiome(this.blockPosition()), this.random));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Bindet die Meerjungfrau an ihr Dorf: sie entfernt sich nicht weiter als {@code radius} Blöcke vom Platz. */
    public void settle(BlockPos center, int radius) {
        this.setHomeTo(center, radius);
    }

    // =====================================================================================
    // Tauschen
    // =====================================================================================

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.getItem() instanceof SpawnEggItem || !this.isAlive() || this.isTrading()) {
            return super.mobInteract(player, hand);
        }
        if (hand == InteractionHand.MAIN_HAND) {
            player.awardStat(Stats.TALKED_TO_VILLAGER);
        }
        if (!this.level().isClientSide()) {
            if (this.getOffers().isEmpty()) {
                this.setUnhappyCounter(40);
                this.makeSound(ModSounds.MERMAID_NO.get());
                return InteractionResult.CONSUME;
            }
            this.setTradingPlayer(player);
            this.openTradingScreen(player, this.getDisplayName(), 1);
        }
        return InteractionResult.SUCCESS_SERVER;
    }

    @Override
    protected void updateTrades(ServerLevel level) {
        MerchantOffers offers = this.getOffers();
        offers.addAll(MermaidTrades.create(this.profession(), level, this.random));
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        super.setTradingPlayer(player);
        if (!this.level().isClientSide()) {
            this.entityData.set(DATA_TRADING, player != null);
        }
    }

    /** Handelt gerade mit einem Spieler (auch auf dem Client). */
    public boolean isShowingTrade() {
        return this.entityData.get(DATA_TRADING);
    }

    @Override
    protected void rewardTradeXp(MerchantOffer offer) {
        if (offer.shouldRewardExp()) {
            int xp = 3 + this.random.nextInt(4);
            this.level().addFreshEntity(new ExperienceOrb(this.level(), this.getX(), this.getY() + 0.5, this.getZ(), xp));
        }
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getUnhappyCounter() > 0) {
            this.setUnhappyCounter(this.getUnhappyCounter() - 1);
        }
        if (this.level().isClientSide()) {
            this.motion.tick(this);
            return;
        }
        // die Angebote füllen sich von Zeit zu Zeit wieder auf (nicht mitten im Handel)
        if (!this.isTrading() && --this.restockCooldown <= 0) {
            this.restockCooldown = RESTOCK_INTERVAL + this.random.nextInt(2400);
            if (this.offers != null) {
                boolean restocked = false;
                for (MerchantOffer offer : this.offers) {
                    if (offer.getUses() > 0) {
                        offer.resetUses();
                        restocked = true;
                    }
                }
                if (restocked && this.level() instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(ParticleTypes.BUBBLE_POP, this.getX(), this.getY(1.0), this.getZ(), 10, 0.3, 0.4, 0.3, 0.02);
                }
            }
        }
    }

    // =====================================================================================
    // Schutz durch die Wachen
    // =====================================================================================

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker) {
            Merfolk.alert(level, this, attacker);
        }
        return hurt;
    }

    // =====================================================================================
    // Sonstiges
    // =====================================================================================

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel level, AgeableMob partner) {
        return null;
    }

    @Override
    public boolean removeWhenFarAway(double distSqr) {
        return false;
    }

    @Override
    public boolean canBeLeashed() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("profession", this.profession().id());
        output.putString("look", this.look().id());
        output.putInt("restock", this.restockCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String profession = input.getStringOr("profession", "");
        if (!profession.isEmpty()) {
            this.entityData.set(DATA_PROFESSION, MermaidProfession.byName(profession).ordinal());
            this.professionChosen = true;
        }
        String look = input.getStringOr("look", "");
        if (!look.isEmpty()) {
            this.entityData.set(DATA_LOOK, MermaidLook.byName(look).ordinal());
            this.lookChosen = true;
        }
        this.restockCooldown = input.getIntOr("restock", RESTOCK_INTERVAL);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isTrading() ? ModSounds.MERMAID_TRADE.get() : ModSounds.MERMAID_AMBIENT.get();
    }

    @Override
    public int getAmbientSoundInterval() {
        return 160;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return ModSounds.MERMAID_HURT.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.MERMAID_DEATH.get();
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean validTrade) {
        return validTrade ? ModSounds.MERMAID_YES.get() : ModSounds.MERMAID_NO.get();
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return ModSounds.MERMAID_YES.get();
    }

    @Override
    public float getVoicePitch() {
        return 0.94f + this.random.nextFloat() * 0.16f;
    }
}
