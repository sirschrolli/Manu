package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jspecify.annotations.Nullable;

/**
 * Der Belagerungsturm: zehn Blöcke hoch bis zur Plattform, vorn mit nassen Häuten verkleidet. Einer schiebt ihn (rechts
 * neben dem Turm, von dort sieht er an ihm vorbei), oben auf der Plattform stehen bis zu acht Krieger.
 *
 * <ul>
 *   <li><b>W/S</b> schieben, der Turm dreht sich langsam in die Blickrichtung; ein Pferd zieht ihn an der Leine schneller
 *   (aber schwer beladen).</li>
 *   <li><b>Leertaste:</b> Die Fallbrücke oben fällt nach vorn, bis sie aufliegt – auf dem Wehrgang einer Mauer, die
 *   sieben bis zwölf Blöcke hoch ist. Liegt sie auf, stürmen die Soldaten oben einer nach dem anderen hinüber und halten
 *   dort Stellung; Spieler gehen mit <b>Schleichen</b> hinüber. Noch einmal die Leertaste zieht die Brücke wieder hoch –
 *   erst dann rollt der Turm wieder.</li>
 * </ul>
 *
 * Wohin die Brücke fällt, rechnet der Server aus: Sie schwenkt vom Senkrechten nach vorn, bis ihre Unterseite auf einen
 * Block trifft (ist die Mauer zu hoch, trifft sie zu früh; ist nichts in Reichweite, träfe sie nichts). Drüben sucht er
 * die Stelle, an der man steht – möglichst hinter der Brustwehr auf dem Wehrgang.
 */
public class SiegeTowerEntity extends SiegeVehicleEntity {
    private static final EntityDataAccessor<Float> DATA_BRIDGE = SynchedEntityData.defineId(SiegeTowerEntity.class, EntityDataSerializers.FLOAT);

    /** So hoch über dem Boden stehen die Krieger oben (Oberkante der Plattform). */
    public static final double PLATFORM = 10.0;
    /** Das Scharnier der Brücke: so weit vor der Mitte, auf Höhe der Plattform. */
    public static final double HINGE = 23.0 / 16.0;
    /** Länge der Brücke. */
    public static final double BRIDGE = 3.0;
    /** Halbe Breite der Brücke (ohne die Geländer). */
    private static final double BRIDGE_HALF = 0.8;
    /** Flacher als so (Grad ab senkrecht) bleibt sie nicht liegen: Dann ist die Mauer zu hoch. */
    private static final float MIN_REST = 50.0f;
    /** Weiter als so fällt sie nicht – darunter ist nichts mehr zu erreichen. */
    private static final float MAX_REST = 140.0f;
    private static final float SWEEP_STEP = 1.5f;
    /** Plätze oben auf der Plattform (der Leiterausstieg hinten in der Mitte bleibt frei). */
    private static final Vec3[] PLATFORM_SLOTS = {
            new Vec3(0.0, PLATFORM, 0.7), new Vec3(0.72, PLATFORM, 0.7), new Vec3(-0.72, PLATFORM, 0.7),
            new Vec3(0.72, PLATFORM, 0.0), new Vec3(-0.72, PLATFORM, 0.0), new Vec3(0.0, PLATFORM, 0.0),
            new Vec3(0.72, PLATFORM, -0.72), new Vec3(-0.72, PLATFORM, -0.72)};

    /** Server: der Winkel, auf den die Brücke zuläuft (0: hochgezogen). */
    private float bridgeTarget;
    private float bridgeSpeed;
    /** Wo man drüben steht (null: die Brücke liegt nirgends auf). */
    private @Nullable Vec3 landing;
    /** Der Block, auf dem die Brücke aufliegt. */
    private @Nullable BlockPos rest;
    private int crossTimer;
    private boolean announced;
    /** Auf dem Bildschirm: der Winkel der Brücke im letzten und in diesem Tick. */
    private float bridgeO;
    private float bridgeNow;

    public SiegeTowerEntity(EntityType<? extends SiegeWeaponEntity> type, Level level) {
        super(type, level, SiegeType.SIEGE_TOWER);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_BRIDGE, 0.0f);
    }

    @Override
    protected Vec3 seat(int index) {
        return index <= 0 ? this.siege().seat() : PLATFORM_SLOTS[Math.min(index, PLATFORM_SLOTS.length) - 1];
    }

    // =====================================================================================
    // Brücke
    // =====================================================================================

    /** Winkel der Brücke in Grad ab senkrecht: 0 hochgezogen, um 90 waagerecht, mehr: nach unten geneigt. */
    public float bridge() {
        return this.entityData.get(DATA_BRIDGE);
    }

    /** Für das Modell: der Winkel zwischen zwei Ticks. */
    public float bridge(float partialTicks) {
        return Mth.lerp(partialTicks, this.bridgeO, this.bridgeNow);
    }

    /** Ist die Brücke ganz oben (nur dann rollt der Turm)? */
    public boolean bridgeUp() {
        return this.bridgeTarget <= 0.0f && this.bridge() <= 0.01f;
    }

    /** Liegt die Brücke auf, und kann man drüben stehen? */
    public boolean open() {
        return this.bridgeTarget > 0.0f && this.bridge() >= this.bridgeTarget - 0.01f && this.landing != null;
    }

    /** Wo man drüben auf der Mauer steht (null: die Brücke liegt nicht auf). */
    public @Nullable Vec3 landing() {
        return this.landing;
    }

    /** Leertaste: Brücke herablassen – oder, wenn sie unten ist, wieder hochziehen. */
    @Override
    protected void jump(ServerLevel level, ServerPlayer player) {
        if (this.bridgeTarget > 0.0f) {
            this.raise(level);
        } else {
            this.lower(level, player);
        }
    }

    /**
     * Lässt die Brücke fallen, wenn vorn eine Mauer in Reichweite ist; sonst erfährt {@code by}, warum es nicht geht.
     *
     * @return ob die Brücke fällt (oder schon unten ist)
     */
    public boolean lower(ServerLevel level, @Nullable LivingEntity by) {
        if (this.bridgeTarget > 0.0f) {
            return true;
        }
        Survey survey = this.survey(level);
        if (survey.problem() != null) {
            if (by instanceof Player player) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.bridge." + survey.problem()).withStyle(ChatFormatting.RED));
                this.overlayTimer = 40;
            }
            level.playSound(null, this.getX(), this.getY() + PLATFORM, this.getZ(), SoundEvents.CHAIN_HIT, SoundSource.NEUTRAL, 0.8f, 0.6f);
            return false;
        }
        this.bridgeTarget = survey.angle();
        this.bridgeSpeed = 0.0f;
        this.rest = survey.rest();
        this.landing = survey.spot();
        this.announced = false;
        this.crossTimer = 12;
        Vec3 hinge = this.hinge();
        level.playSound(null, hinge.x, hinge.y, hinge.z, SoundEvents.CHAIN_BREAK, SoundSource.NEUTRAL, 1.2f, 0.55f);
        level.playSound(null, hinge.x, hinge.y, hinge.z, SoundEvents.WOODEN_TRAPDOOR_OPEN, SoundSource.NEUTRAL, 1.2f, 0.5f);
        return true;
    }

    /** Zieht die Brücke wieder hoch (die Winde knarrt). */
    public void raise(ServerLevel level) {
        if (this.bridgeTarget <= 0.0f) {
            return;
        }
        this.bridgeTarget = 0.0f;
        this.landing = null;
        this.rest = null;
        Vec3 hinge = this.hinge();
        level.playSound(null, hinge.x, hinge.y, hinge.z, SoundEvents.CROSSBOW_LOADING_START.value(), SoundSource.NEUTRAL, 1.0f, 0.45f);
    }

    /** Ist vorn eine Mauer, auf die die Brücke fallen könnte (für die Mannschaft)? */
    public boolean canLand(Level level) {
        return this.bridgeUp() && this.survey(level).problem() == null;
    }

    /** Das Scharnier der Brücke in der Welt (Oberkante der Plattform, vorn in der Mitte). */
    public Vec3 hinge() {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        return this.position().add(fwd.scale(HINGE)).add(0.0, PLATFORM, 0.0);
    }

    private void bridgeTick(ServerLevel level) {
        float angle = this.bridge();
        if (angle < this.bridgeTarget) {
            // sie fällt: immer schneller, bis sie aufschlägt
            this.bridgeSpeed = Math.min(this.bridgeSpeed + 0.7f, 11.0f);
            angle = Math.min(this.bridgeTarget, angle + this.bridgeSpeed);
            this.entityData.set(DATA_BRIDGE, angle);
            if (angle >= this.bridgeTarget) {
                this.slam(level);
            }
        } else if (angle > this.bridgeTarget) {
            // die Winde zieht sie langsam hoch
            angle = Math.max(this.bridgeTarget, angle - 2.2f);
            this.entityData.set(DATA_BRIDGE, angle);
            Vec3 hinge = this.hinge();
            if (angle <= 0.0f) {
                level.playSound(null, hinge.x, hinge.y, hinge.z, SoundEvents.WOODEN_TRAPDOOR_CLOSE, SoundSource.NEUTRAL, 1.2f, 0.5f);
            } else if (this.tickCount % 6 == 0) {
                level.playSound(null, hinge.x, hinge.y, hinge.z, SoundEvents.CROSSBOW_LOADING_MIDDLE.value(), SoundSource.NEUTRAL, 0.7f, 0.4f);
            }
        }
        if (this.bridgeTarget > 0.0f && angle >= this.bridgeTarget) {
            if ((this.tickCount + this.getId()) % 20 == 0) {
                this.recheck(level);
            }
            if (this.open()) {
                this.crossTick(level);
            }
        }
    }

    /** Die Brücke schlägt auf: ein Krachen, Staub, und oben ruft man zum Sturm. */
    private void slam(ServerLevel level) {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        double a = this.bridgeTarget * Mth.DEG_TO_RAD;
        Vec3 tip = this.hinge().add(fwd.scale(BRIDGE * Math.sin(a))).add(0.0, BRIDGE * Math.cos(a), 0.0);
        level.playSound(null, tip.x, tip.y, tip.z, SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR, SoundSource.NEUTRAL, 1.6f, 0.55f);
        level.playSound(null, tip.x, tip.y, tip.z, SoundEvents.WOOD_BREAK, SoundSource.NEUTRAL, 1.0f, 0.6f);
        BlockState dust = this.rest != null ? level.getBlockState(this.rest) : Blocks.STONE_BRICKS.defaultBlockState();
        if (dust.isAir()) {
            dust = Blocks.STONE_BRICKS.defaultBlockState();
        }
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, dust), tip.x, tip.y, tip.z, 30, 0.6, 0.1, 0.6, 0.15);
        level.sendParticles(ParticleTypes.POOF, tip.x, tip.y + 0.2, tip.z, 8, 0.5, 0.1, 0.5, 0.02);
        if (!this.announced) {
            this.announced = true;
            List<Entity> passengers = this.getPassengers();
            for (int i = 1; i < passengers.size(); i++) {
                if (passengers.get(i) instanceof ServerPlayer player) {
                    player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.bridge.cross").withStyle(ChatFormatting.GOLD));
                }
            }
        }
    }

    /** Liegt die Brücke noch auf? Ist der Block darunter zerschlagen, fällt sie weiter – oder wird hochgezogen. */
    private void recheck(ServerLevel level) {
        if (this.rest != null && this.landing != null && !level.getBlockState(this.rest).getCollisionShape(level, this.rest).isEmpty()) {
            return;
        }
        Contact contact = this.sweep(level, this.bridgeTarget);
        Vec3 spot = contact != null ? this.spotAt(level, contact.angle()) : null;
        if (contact == null || spot == null) {
            this.raise(level);
            return;
        }
        if (contact.angle() > this.bridgeTarget + 0.5f) {
            this.bridgeSpeed = 0.0f;
        }
        this.bridgeTarget = contact.angle();
        this.rest = contact.pos();
        this.landing = spot;
    }

    /** Soldaten oben stürmen einer nach dem anderen hinüber (wer schiebt, bleibt beim Turm). */
    private void crossTick(ServerLevel level) {
        if (--this.crossTimer > 0) {
            return;
        }
        List<Entity> passengers = this.getPassengers();
        for (int i = 1; i < passengers.size(); i++) {
            if (passengers.get(i) instanceof SoldierEntity soldier) {
                this.crossTimer = 7;
                soldier.stopRiding();
                soldier.setMode(SoldierEntity.MODE_HOLD, soldier.blockPosition());
                soldier.speak("attack");
                level.playSound(null, soldier.getX(), soldier.getY(), soldier.getZ(), SoundEvents.WOOD_STEP, SoundSource.NEUTRAL, 1.0f, 0.8f);
                return;
            }
        }
    }

    // =====================================================================================
    // Wohin die Brücke fällt
    // =====================================================================================

    /** Wohin die Brücke fiele: Winkel, worauf sie liegt, wo man drüben steht – oder warum es nicht geht. */
    record Survey(float angle, @Nullable BlockPos rest, @Nullable Vec3 spot, @Nullable String problem) {
        static Survey fail(String problem) {
            return new Survey(0.0f, null, null, problem);
        }
    }

    private record Contact(float angle, BlockPos pos) {}

    Survey survey(Level level) {
        Contact contact = this.sweep(level, 0.0f);
        if (contact == null) {
            return Survey.fail("no_wall");
        }
        if (contact.angle() < MIN_REST) {
            return Survey.fail("too_high");
        }
        Vec3 spot = this.spotAt(level, contact.angle());
        if (spot == null) {
            return Survey.fail("no_room");
        }
        return new Survey(contact.angle(), contact.pos(), spot, null);
    }

    /**
     * Die Brücke schwenkt vom Winkel {@code from} an nach vorn, bis ihre Unterseite (die Querleisten, eine Handbreit unter
     * der Lauffläche) auf einen Block trifft. @return der letzte freie Winkel und der getroffene Block; null: sie träfe
     * nichts
     */
    private @Nullable Contact sweep(Level level, float from) {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 side = new Vec3(fwd.z, 0.0, -fwd.x);
        Vec3 hinge = this.hinge();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        float previous = from;
        for (float angle = Math.max(from, 10.0f); angle <= MAX_REST; angle += SWEEP_STEP) {
            double a = angle * Mth.DEG_TO_RAD;
            Vec3 along = fwd.scale(Math.sin(a)).add(0.0, Math.cos(a), 0.0);
            Vec3 under = fwd.scale(Math.cos(a)).add(0.0, -Math.sin(a), 0.0).scale(0.2);
            for (double s = 0.5; s <= BRIDGE + 1.0e-3; s += 0.25) {
                for (int w = -1; w <= 1; w++) {
                    Vec3 p = hinge.add(along.scale(s)).add(under).add(side.scale(w * BRIDGE_HALF));
                    pos.set(Mth.floor(p.x), Mth.floor(p.y), Mth.floor(p.z));
                    if (solid(level, pos, p)) {
                        return new Contact(previous, pos.immutable());
                    }
                }
            }
            previous = angle;
        }
        return null;
    }

    private static boolean solid(Level level, BlockPos pos, Vec3 p) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir()) {
            return false;
        }
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return false;
        }
        double x = p.x - pos.getX();
        double y = p.y - pos.getY();
        double z = p.z - pos.getZ();
        for (AABB box : shape.toAabbs()) {
            if (box.contains(x, y, z)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wo man ankommt, wenn die Brücke im Winkel {@code angle} liegt: die erste Stelle unter der Brücke, auf der man stehen
     * kann (Mauerkrone, Zinne), und von dort bis anderthalb Blöcke weiter hinein, solange es nicht bergauf geht – so landet
     * man hinter der Brustwehr auf dem Wehrgang statt auf einer Zinne.
     */
    private @Nullable Vec3 spotAt(Level level, float angle) {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 hinge = this.hinge();
        double a = angle * Mth.DEG_TO_RAD;
        double sin = Math.max(0.05, Math.sin(a));
        double reach = BRIDGE * Math.sin(a);
        Vec3 first = null;
        Vec3 best = null;
        double firstAt = 0.0;
        for (double d = 0.4; d <= reach + 1.01; d += 0.5) {
            double height = hinge.y + Math.min(d, reach) / sin * Math.cos(a);
            double top = best != null ? best.y + 0.1 : height + 0.6;
            double bottom = best != null ? best.y - 1.6 : height - 2.2;
            Vec3 column = hinge.add(fwd.scale(d));
            Vec3 spot = stand(level, column.x, column.z, top, bottom);
            if (first == null) {
                if (spot != null) {
                    first = spot;
                    best = spot;
                    firstAt = d;
                }
                continue;
            }
            if (spot == null || d - firstAt > 1.6) {
                break;
            }
            best = spot;
        }
        return best;
    }

    /** Die höchste Stelle zwischen {@code top} und {@code bottom} in dieser Säule, auf der ein Krieger stehen kann. */
    private static @Nullable Vec3 stand(Level level, double x, double z, double top, double bottom) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        int bx = Mth.floor(x);
        int bz = Mth.floor(z);
        for (int y = Mth.floor(top); y >= Mth.floor(bottom) - 1; y--) {
            pos.set(bx, y, bz);
            BlockState ground = level.getBlockState(pos);
            VoxelShape shape = ground.getCollisionShape(level, pos);
            if (shape.isEmpty()) {
                continue;
            }
            double surface = y + shape.max(Direction.Axis.Y);
            if (surface > top + 0.01) {
                return null;   // Wand: hier geht es nicht hinauf
            }
            if (surface < bottom) {
                return null;
            }
            AABB body = new AABB(bx + 0.2, surface + 0.01, bz + 0.2, bx + 0.8, surface + 1.8, bz + 0.8);
            return level.noBlockCollision(null, body) ? new Vec3(bx + 0.5, surface, bz + 0.5) : null;
        }
        return null;
    }

    /** Ein freier Platz drüben für diesen Krieger (nebeneinander, nicht übereinander). */
    private @Nullable Vec3 landingFor(LivingEntity passenger) {
        Vec3 spot = this.landing;
        if (spot == null) {
            return null;
        }
        Vec3 fwd = Vec3.directionFromRotation(0.0f, this.getYRot());
        Vec3 side = new Vec3(fwd.z, 0.0, -fwd.x);
        int start = this.random.nextInt(3);
        for (int k = 0; k < 3; k++) {
            int lane = (start + k) % 3 - 1;
            Vec3 at = spot.add(side.scale(lane * 0.9));
            Vec3 ground = stand(this.level(), at.x, at.z, spot.y + 0.6, spot.y - 1.2);
            if (ground != null && this.level().noCollision(passenger, passenger.getType().getSpawnAABB(ground.x, ground.y, ground.z))) {
                return ground;
            }
        }
        return spot;
    }

    // =====================================================================================
    // Ablauf
    // =====================================================================================

    @Override
    public void tick() {
        super.tick();
        if (this.level() instanceof ServerLevel level && !this.isRemoved()) {
            this.bridgeTick(level);
        }
    }

    @Override
    protected void clientTick() {
        super.clientTick();
        this.bridgeO = this.bridgeNow;
        this.bridgeNow = this.bridge();
        if (this.tickCount <= 1) {
            this.bridgeO = this.bridgeNow;
        }
    }

    /** Solange die Brücke nicht oben ist, rollt der Turm nicht (zieht ein Pferd an, wird sie hochgezogen). */
    @Override
    protected boolean canRoll() {
        if (this.bridgeTarget > 0.0f && this.level() instanceof ServerLevel level) {
            this.raise(level);
        }
        return this.bridgeUp();
    }

    @Override
    protected double control(ServerLevel level, boolean towed) {
        double drive = super.control(level, towed);
        if (drive != 0.0 && !this.bridgeUp()) {
            if (this.getFirstPassenger() instanceof ServerPlayer player && this.overlayTimer < 8) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.bridge.raise_first").withStyle(ChatFormatting.RED));
                this.overlayTimer = 40;
            }
            return 0.0;
        }
        return drive;
    }

    /** Ist die Brücke unten, steigt man über sie auf die Mauer ab – sonst klettert man hinten die Leiter hinunter. */
    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        if (this.open()) {
            Vec3 spot = this.landingFor(passenger);
            if (spot != null) {
                return spot;
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    @Override
    protected Component status() {
        Component name = this.getDisplayName().copy().withStyle(ChatFormatting.GOLD);
        Component bridge = this.open() ? Component.translatable("siege.kingdomsatwar.bridge.down").withStyle(ChatFormatting.GREEN)
                : this.bridgeUp() ? Component.translatable("siege.kingdomsatwar.bridge.up").withStyle(ChatFormatting.GRAY)
                : Component.translatable("siege.kingdomsatwar.bridge.moving").withStyle(ChatFormatting.YELLOW);
        return Component.translatable("siege.kingdomsatwar.status.siege_tower", name, bridge, Math.max(0, this.getPassengers().size() - 1),
                this.healthLine());
    }

    @Override
    protected Component riderStatus() {
        return this.open() ? Component.translatable("siege.kingdomsatwar.bridge.cross").withStyle(ChatFormatting.GOLD)
                : Component.translatable("siege.kingdomsatwar.controls.siege_tower.rider").withStyle(ChatFormatting.GRAY);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        output.putFloat("SiegeBridge", this.bridgeTarget);
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.bridgeTarget = Mth.clamp(input.getFloatOr("SiegeBridge", 0.0f), 0.0f, MAX_REST);
        this.entityData.set(DATA_BRIDGE, this.bridgeTarget);
        // worauf sie liegt, sucht der Turm nach dem Laden selbst wieder (siehe recheck)
        this.rest = null;
        this.landing = null;
        this.announced = true;
    }

    /** Nur für Tests und Vorführungen: die Brücke sofort in diesen Winkel (ohne Fallen). */
    public void devBridge(float angle) {
        this.bridgeTarget = angle;
        this.entityData.set(DATA_BRIDGE, angle);
        this.bridgeO = angle;
        this.bridgeNow = angle;
    }
}
