package io.github.sirschrolli.kingdomsatwar.mixin;

import io.github.sirschrolli.kingdomsatwar.fishing.Fishing;
import io.github.sirschrolli.kingdomsatwar.fishing.HookAccess;
import io.github.sirschrolli.kingdomsatwar.fishing.Medium;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FishingHook;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Das Angeln (seit 0.79) am Haken selbst – Minecraft kennt nur Wasser und hebt jeden Fang sofort heraus:
 * <ul>
 *     <li>Der Schwimmer treibt auch auf <b>Lava</b> (er verbrennt nicht) und in der <b>Leere</b>: Fällt er ein Stück unter
 *     die Füße des Anglers und ist unter ihm weit und breit nichts als Luft, bleibt er dort hängen wie auf einer
 *     Wasseroberfläche.</li>
 *     <li>Wer bei einem Biss anzieht, hebt den Fisch nicht heraus, sondern beginnt den Drill ({@link Fishing#onReel}) – der
 *     Haken bleibt draußen, der Biss hält an, bis der Drill entschieden ist.</li>
 *     <li>Köder verkürzen das Warten; in Lava und Leere gibt es eigene Spuren und Klänge statt der Wasserspritzer.</li>
 * </ul>
 */
@Mixin(FishingHook.class)
public abstract class FishingHookMixin extends Projectile implements HookAccess {
    @Shadow
    private int nibble;
    @Shadow
    private int timeUntilLured;
    @Shadow
    private int timeUntilHooked;
    @Shadow
    private float fishAngle;
    @Shadow
    @Final
    private int luck;
    @Shadow
    private Entity hookedIn;
    @Shadow
    @Final
    private static EntityDataAccessor<Boolean> DATA_BITING;

    @Unique
    private Medium kingdomsatwar$medium = Medium.WATER;
    @Unique
    private boolean kingdomsatwar$afloat;
    @Unique
    private double kingdomsatwar$voidLevel = Double.NaN;
    @Unique
    private int kingdomsatwar$nibbleBefore;
    @Unique
    private boolean kingdomsatwar$idleBefore;

    protected FishingHookMixin(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }

    @Shadow
    public abstract Player getPlayerOwner();

    // =====================================================================================
    // Worin der Schwimmer treibt
    // =====================================================================================

    /** Die erste Frage des Hakens je Tick: Bin ich im Wasser? Lava und die Leere zählen jetzt auch. */
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 0))
    private boolean kingdomsatwar$floats(FluidState state, TagKey<Fluid> water) {
        if (state.is(water)) {
            this.kingdomsatwar$medium = Medium.WATER;
            this.kingdomsatwar$afloat = true;
        } else if (state.is(FluidTags.LAVA)) {
            this.kingdomsatwar$medium = Medium.LAVA;
            this.kingdomsatwar$afloat = true;
        } else if (this.kingdomsatwar$overVoid()) {
            this.kingdomsatwar$medium = Medium.VOID;
            this.kingdomsatwar$afloat = true;
        } else {
            this.kingdomsatwar$afloat = false;
        }
        return this.kingdomsatwar$afloat;
    }

    /** Die zweite Frage (ob die Schwerkraft zieht): nicht, solange er treibt. */
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;is(Lnet/minecraft/tags/TagKey;)Z", ordinal = 1))
    private boolean kingdomsatwar$noGravity(FluidState state, TagKey<Fluid> water) {
        return this.kingdomsatwar$afloat || state.is(water);
    }

    /** Die Höhe der Oberfläche im Block des Hakens – in der Leere liegt sie auf der gemerkten Höhe. */
    @Redirect(method = "tick", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/level/material/FluidState;getHeight(Lnet/minecraft/world/level/BlockGetter;Lnet/minecraft/core/BlockPos;)F"))
    private float kingdomsatwar$surface(FluidState state, BlockGetter level, BlockPos pos) {
        if (this.kingdomsatwar$medium == Medium.VOID) {
            return (float) (this.kingdomsatwar$voidLevel - pos.getY());
        }
        return state.getHeight(level, pos);
    }

    /** Treibt der Haken in der Leere (oder beginnt er gerade damit)? Das entscheidet der Server und meldet die Höhe. */
    @Unique
    private boolean kingdomsatwar$overVoid() {
        if (!Double.isNaN(this.kingdomsatwar$voidLevel)) {
            if (!this.level().isClientSide() && this.tickCount % 40 == 0) {
                Fishing.anchored((FishingHook) (Object) this, this.kingdomsatwar$voidLevel);
            }
            return true;
        }
        Player owner = this.getPlayerOwner();
        if (this.level().isClientSide() || owner == null || this.onGround() || this.hookedIn != null || this.getDeltaMovement().y >= 0.0
                || this.getY() > owner.getY() - Fishing.VOID_DROP) {
            return false;
        }
        // nichts als Luft unter ihm – in den Welten über dem Nichts, so weit die Schnur reichen würde, sonst bis zum Boden der
        // Welt? (Sonst fällt er einfach auf den Grund.)
        Level level = this.level();
        BlockPos.MutableBlockPos cursor = this.blockPosition().mutable();
        for (int y = cursor.getY(), bottom = Fishing.voidBottom(level, y); y >= bottom; y--) {
            cursor.setY(y);
            if (!level.getBlockState(cursor).isAir()) {
                return false;
            }
        }
        // knapp unter einer Blockgrenze, wie eine Wasseroberfläche
        this.kingdomsatwar$voidLevel = Math.floor(this.getY()) + 0.9;
        Fishing.anchored((FishingHook) (Object) this, this.kingdomsatwar$voidLevel);
        return true;
    }

    /** Der Schwimmer verbrennt nicht (er treibt ja auf Lava). */
    @Override
    public boolean fireImmune() {
        return true;
    }

    // =====================================================================================
    // Warten und Biss
    // =====================================================================================

    @Inject(method = "catchingFish", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$beforeCatching(BlockPos pos, CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;
        if (Fishing.isFighting(self)) {
            // der Biss hält an, bis der Drill entschieden ist
            Fishing.fightFx(self, this.kingdomsatwar$medium);
            ci.cancel();
            return;
        }
        this.kingdomsatwar$nibbleBefore = this.nibble;
        this.kingdomsatwar$idleBefore = this.nibble <= 0 && this.timeUntilHooked <= 0 && this.timeUntilLured <= 0;
    }

    @Inject(method = "catchingFish", at = @At("TAIL"))
    private void kingdomsatwar$afterCatching(BlockPos pos, CallbackInfo ci) {
        FishingHook self = (FishingHook) (Object) this;
        // eine neue Wartezeit hat begonnen: Köder verkürzen sie
        if (this.kingdomsatwar$idleBefore && this.timeUntilLured > 0) {
            float haste = Fishing.baitHaste(self);
            if (haste > 0.0f) {
                this.timeUntilLured = Math.max(20, Math.round(this.timeUntilLured * (1.0f - haste)));
            }
        }
        if (this.kingdomsatwar$medium != Medium.WATER) {
            Fishing.foreignFx(self, this.kingdomsatwar$medium, this.timeUntilHooked, this.fishAngle,
                    this.kingdomsatwar$nibbleBefore <= 0 && this.nibble > 0, this.timeUntilLured);
        }
    }

    /** Unter einer Decke beißt es bei Minecraft langsamer – in Lava und Leere soll das nicht gelten (dort gibt es keinen Himmel). */
    @Redirect(method = "catchingFish", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/Level;canSeeSky(Lnet/minecraft/core/BlockPos;)Z"))
    private boolean kingdomsatwar$openSky(Level level, BlockPos pos) {
        return this.kingdomsatwar$medium != Medium.WATER || level.canSeeSky(pos);
    }

    /** Das Platschen beim Biss passt nur zum Wasser. */
    @Redirect(method = "catchingFish", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/entity/projectile/FishingHook;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void kingdomsatwar$biteSound(FishingHook hook, SoundEvent sound, float volume, float pitch) {
        Fishing.biteSound(hook, this.kingdomsatwar$medium, volume, pitch);
    }

    // =====================================================================================
    // Anziehen
    // =====================================================================================

    @Inject(method = "retrieve", at = @At("HEAD"), cancellable = true)
    private void kingdomsatwar$reel(ItemStack rod, CallbackInfoReturnable<Integer> cir) {
        FishingHook self = (FishingHook) (Object) this;
        if (this.level().isClientSide() || this.hookedIn != null || !(this.getPlayerOwner() instanceof ServerPlayer player)) {
            return;
        }
        if (Fishing.isFighting(self)) {
            // mitten im Drill wird nicht eingeholt
            cir.setReturnValue(0);
            return;
        }
        if (this.nibble > 0 && player.canInteractWithLevel()) {
            Integer damage = Fishing.onReel(player, self, rod, this.luck, this.kingdomsatwar$medium);
            if (damage != null) {
                cir.setReturnValue(damage);
            }
        }
    }

    // =====================================================================================
    // Zugriff von außen
    // =====================================================================================

    @Override
    public void kingdomsatwar$endBite() {
        this.nibble = 0;
        this.timeUntilLured = 0;
        this.timeUntilHooked = 0;
        this.getEntityData().set(DATA_BITING, false);
    }

    @Override
    public Medium kingdomsatwar$medium() {
        return this.kingdomsatwar$medium;
    }

    @Override
    public void kingdomsatwar$anchor(double voidLevel) {
        this.kingdomsatwar$voidLevel = voidLevel;
    }

    @Override
    public void kingdomsatwar$forceBite() {
        this.timeUntilLured = 0;
        this.timeUntilHooked = 0;
        this.nibble = 40;
        this.getEntityData().set(DATA_BITING, true);
    }
}
