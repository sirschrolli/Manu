package io.github.sirschrolli.kingdomsatwar.entity.umbrer;

import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
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
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.InteractGoal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.LookAtTradingPlayerGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.PanicGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TradeWithPlayerGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.npc.villager.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Ein Umbrer: Bewohner der Schattenwelt, groß und schlank, mit spitzen Ohren und silbrig leuchtenden Augen. Er lebt in
 * einem Dorf (bleibt in dessen Nähe), geht seinem Beruf nach und tauscht gegen Mondsilber. Vor den Schattenwesen
 * weicht er zurück; wer ihn angreift, bekommt es mit den Wächtern zu tun.
 */
public class UmbrerEntity extends AbstractVillager implements UmbrerLike {
    private static final EntityDataAccessor<Integer> DATA_PROFESSION =
            SynchedEntityData.defineId(UmbrerEntity.class, EntityDataSerializers.INT);
    /** Handelt gerade (für die Haltung im Modell, auch auf dem Client bekannt). */
    private static final EntityDataAccessor<Boolean> DATA_TRADING =
            SynchedEntityData.defineId(UmbrerEntity.class, EntityDataSerializers.BOOLEAN);
    /** So oft (Ticks) füllen sich die Angebote wieder auf. */
    private static final int RESTOCK_INTERVAL = 6000;

    private int restockCooldown = RESTOCK_INTERVAL;
    private boolean professionChosen;

    public UmbrerEntity(EntityType<? extends UmbrerEntity> type, Level level) {
        super(type, level);
        // Umbrer öffnen Türen (und schließen sie hinter sich) – so kommen sie aus ihren Häusern
        this.getNavigation().setCanOpenDoors(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 24.0)
                .add(Attributes.MOVEMENT_SPEED, 0.5)
                .add(Attributes.FOLLOW_RANGE, 32.0);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_PROFESSION, UmbrerProfession.MOONSMITH.ordinal());
        builder.define(DATA_TRADING, false);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TradeWithPlayerGoal(this));
        this.goalSelector.addGoal(1, new AvoidEntityGoal<>(this, Mob.class, 10.0f, 0.55, 0.7,
                entity -> entity.is(ModTags.EntityTypes.SHADOW_CREATURES)));
        this.goalSelector.addGoal(1, new PanicGoal(this, 0.7));
        this.goalSelector.addGoal(1, new LookAtTradingPlayerGoal(this));
        this.goalSelector.addGoal(2, new OpenDoorGoal(this, true));
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.5));
        this.goalSelector.addGoal(6, new WaterAvoidingRandomStrollGoal(this, 0.4));
        this.goalSelector.addGoal(8, new InteractGoal(this, Player.class, 4.0f, 1.0f));
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Mob.class, 8.0f));
        this.goalSelector.addGoal(10, new RandomLookAroundGoal(this));
    }

    // =====================================================================================
    // Beruf
    // =====================================================================================

    @Override
    public UmbrerProfession profession() {
        return UmbrerProfession.byId(this.entityData.get(DATA_PROFESSION));
    }

    public void setProfession(UmbrerProfession profession) {
        this.entityData.set(DATA_PROFESSION, profession.ordinal());
        this.professionChosen = true;
        this.offers = null;
    }

    @Override
    protected Component getTypeName() {
        return Component.translatable(this.getType().getDescriptionId() + "." + this.profession().id());
    }

    @Override
    public @Nullable SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, EntitySpawnReason reason,
            @Nullable SpawnGroupData groupData) {
        if (!this.professionChosen) {
            this.setProfession(UmbrerProfession.randomTrader(this.random));
        }
        return super.finalizeSpawn(level, difficulty, reason, groupData);
    }

    /** Bindet den Umbrer an sein Dorf: er entfernt sich nicht weiter als {@code radius} Blöcke vom Mittelpunkt. */
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
                this.makeSound(SoundEvents.WANDERING_TRADER_NO);
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
        offers.addAll(UmbrerTrades.create(this.profession(), level, this.random));
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
        if (this.level().isClientSide()) {
            if (this.getUnhappyCounter() > 0) {
                this.setUnhappyCounter(this.getUnhappyCounter() - 1);
            }
            return;
        }
        if (this.getUnhappyCounter() > 0) {
            this.setUnhappyCounter(this.getUnhappyCounter() - 1);
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
                    serverLevel.sendParticles(ParticleTypes.END_ROD, this.getX(), this.getY(1.0), this.getZ(), 6, 0.3, 0.3, 0.3, 0.01);
                }
            }
        }
    }

    // =====================================================================================
    // Schutz durch die Wächter
    // =====================================================================================

    @Override
    public boolean hurtServer(ServerLevel level, DamageSource source, float amount) {
        boolean hurt = super.hurtServer(level, source, amount);
        if (hurt && source.getEntity() instanceof LivingEntity attacker && !(attacker instanceof UmbrerLike)) {
            UmbrerGuardEntity.alert(level, this, attacker);
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
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putString("profession", this.profession().id());
        output.putInt("restock", this.restockCooldown);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        String id = input.getStringOr("profession", "");
        for (UmbrerProfession profession : UmbrerProfession.values()) {
            if (profession.id().equals(id)) {
                this.entityData.set(DATA_PROFESSION, profession.ordinal());
                this.professionChosen = true;
            }
        }
        this.restockCooldown = input.getIntOr("restock", RESTOCK_INTERVAL);
    }

    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return this.isTrading() ? SoundEvents.WANDERING_TRADER_TRADE : SoundEvents.WANDERING_TRADER_AMBIENT;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource source) {
        return SoundEvents.WANDERING_TRADER_HURT;
    }

    @Override
    protected SoundEvent getDeathSound() {
        return SoundEvents.WANDERING_TRADER_DEATH;
    }

    @Override
    protected SoundEvent getTradeUpdatedSound(boolean validTrade) {
        return validTrade ? SoundEvents.WANDERING_TRADER_YES : SoundEvents.WANDERING_TRADER_NO;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.WANDERING_TRADER_YES;
    }

    @Override
    public float getVoicePitch() {
        // tiefere, ruhigere Stimmen als die der Händler aus der Oberwelt
        return 0.72f + this.random.nextFloat() * 0.12f;
    }
}
