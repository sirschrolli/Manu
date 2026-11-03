package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Soldaten als Bedienmannschaft einer Belagerungswaffe – befehligt mit dem Kommandostab:
 *
 * <ul>
 *   <li>Stab auf die eigene, leere Waffe: der nächste freie Soldat eilt herbei und bemannt sie.</li>
 *   <li>Stab auf die bemannte Waffe: sie ist gewählt (eine halbe Minute lang) – zeigt man dann mit dem Stab auf eine
 *   Stelle (bis 160 Blöcke weit), beschießt die Mannschaft sie, bis dort nichts mehr zu zerschlagen ist; zeigt man auf
 *   einen Gegner, beschießt sie ihn; zeigt man in den Himmel, sucht sie sich ihre Ziele selbst.</li>
 *   <li>Schleichend den Stab auf die Waffe: die Mannschaft steigt ab.</li>
 * </ul>
 *
 * Ohne Befehl nimmt die Mannschaft selbst Gegner in Reichweite unter Beschuss. Die Mannschaft eines Rammbocks fährt
 * auf Befehl an das gezeigte Tor und stößt zu, bis es bricht. Munition legt der Spieler selbst in die Waffe.
 */
public final class SiegeCrew {
    /** So lange bleibt eine Waffe zum Zielen gewählt. */
    private static final int SELECT_TICKS = 600;
    /** So weit reicht der Stab beim Zielen. */
    private static final double ORDER_RANGE = 160.0;
    /** So oft schießt die Mannschaft höchstens auf eine befohlene Stelle. */
    private static final int MAX_SHOTS = 12;
    /** Wie lange ein herbeigerufener Soldat höchstens braucht. */
    private static final int ASSIGN_TICKS = 600;
    /** Gewählte Waffe je Spieler (Kennung der Waffe, bis wann). */
    private static final Map<UUID, Selection> SELECTED = new HashMap<>();

    private record Selection(UUID weapon, long until) {}

    private SiegeCrew() {}

    /** Befehle für die Mannschaft einer Waffe (werden mit der Waffe gespeichert). */
    public static final class Orders {
        @Nullable Vec3 targetPos;
        @Nullable UUID targetEntity;
        int shots;
        @Nullable UUID assigned;
        long assignedAt;
        @Nullable UUID autoTarget;
        int idleTicks;

        void shotFired() {
            if (this.targetPos != null) {
                this.shots++;
            }
        }

        void clearTarget() {
            this.targetPos = null;
            this.targetEntity = null;
            this.shots = 0;
        }

        public @Nullable Vec3 targetPos() {
            return this.targetPos;
        }

        public @Nullable UUID targetEntity() {
            return this.targetEntity;
        }

        void save(ValueOutput output) {
            if (this.targetPos != null) {
                output.store("CrewTarget", Vec3.CODEC, this.targetPos);
                output.putInt("CrewShots", this.shots);
            }
            if (this.targetEntity != null) {
                output.store("CrewTargetEntity", UUIDUtil.CODEC, this.targetEntity);
            }
        }

        void load(ValueInput input) {
            this.targetPos = input.read("CrewTarget", Vec3.CODEC).orElse(null);
            this.shots = input.getIntOr("CrewShots", 0);
            this.targetEntity = input.read("CrewTargetEntity", UUIDUtil.CODEC).orElse(null);
        }
    }

    // =====================================================================================
    // Jeden Tick (aus der Waffe)
    // =====================================================================================

    /** Ein Soldat bedient die Waffe: Ziel wählen, drehen, zielen, schießen. @return Fahrbefehl (nur beim Rammbock) */
    static double tick(ServerLevel level, SiegeWeaponEntity weapon, SoldierEntity crew) {
        Orders orders = weapon.orders;
        if (weapon.siege() == SiegeType.RAM) {
            return ramTick(level, weapon, crew, orders);
        }
        Vec3 aim = null;
        if (orders.targetEntity != null) {
            Entity entity = level.getEntity(orders.targetEntity);
            if (entity instanceof LivingEntity living && living.isAlive() && inRange(level, weapon, living.position())) {
                aim = lead(weapon, living);
            } else {
                orders.targetEntity = null;
            }
        }
        if (aim == null && orders.targetPos != null) {
            if (orders.shots >= MAX_SHOTS || !inRange(level, weapon, orders.targetPos)) {
                orders.clearTarget();
            } else {
                aim = orders.targetPos;
            }
        }
        if (aim == null) {
            // ohne Befehl: Gegner in Reichweite unter Beschuss nehmen
            if (orders.autoTarget == null || (level.getGameTime() + weapon.getId()) % 20 == 0) {
                LivingEntity enemy = findEnemy(level, weapon, crew);
                orders.autoTarget = enemy != null ? enemy.getUUID() : null;
            }
            if (orders.autoTarget != null && level.getEntity(orders.autoTarget) instanceof LivingEntity living && living.isAlive()
                    && inRange(level, weapon, living.position())) {
                aim = lead(weapon, living);
            } else {
                orders.autoTarget = null;
            }
        }
        if (aim == null) {
            return 0.0;
        }
        Vec3 from = weapon.position();
        float yaw = (float) (Mth.atan2(-(aim.x - from.x), aim.z - from.z) * Mth.RAD_TO_DEG);
        weapon.turnToward(yaw, weapon.siege().turnRate());
        crew.getLookControl().setLookAt(aim.x, aim.y, aim.z);
        weapon.aimAt(aim);
        if (weapon.yawError(yaw) < 2.5f && weapon.ready() && weapon.ammo() >= weapon.ammoPerShot()) {
            weapon.fire(level, crew);
        }
        return 0.0;
    }

    /** Rammbock: zum befohlenen Tor fahren und zustoßen, bis es bricht; ohne Befehl stößt er zu, wenn er vor einem Tor steht. */
    private static double ramTick(ServerLevel level, SiegeWeaponEntity ram, SoldierEntity crew, Orders orders) {
        Vec3 goal = orders.targetPos;
        Vec3 fwd = Vec3.directionFromRotation(0.0f, ram.getYRot());
        Vec3 head = ram.launchPoint();
        boolean gateAhead = gateAhead(level, ram);
        if (goal != null) {
            Vec3 to = goal.subtract(ram.position()).multiply(1.0, 0.0, 1.0);
            double dist = to.length();
            float yaw = (float) (Mth.atan2(-to.x, to.z) * Mth.RAD_TO_DEG);
            ram.turnToward(yaw, ram.siege().turnRate());
            crew.getLookControl().setLookAt(goal.x, goal.y + 1.0, goal.z);
            if (gateAhead || head.distanceTo(goal) < 1.2) {
                if (ram.ready()) {
                    ram.fire(level, crew);
                }
                if (!gateAhead && ++orders.idleTicks > 80) {
                    orders.clearTarget();          // das Tor ist offen
                    orders.idleTicks = 0;
                }
                return 0.0;
            }
            orders.idleTicks = 0;
            if (orders.shots >= MAX_SHOTS * 2) {
                orders.clearTarget();
                return 0.0;
            }
            return ram.yawError(yaw) < 20.0f && dist > 1.0 ? 1.0 : 0.0;
        }
        if (gateAhead && ram.ready()) {
            ram.fire(level, crew);
        }
        return 0.0;
    }

    /** Steht vor dem Widderkopf ein Tor, eine Tür, ein Gitter oder Holz, das der Rammbock brechen kann? */
    private static boolean gateAhead(ServerLevel level, SiegeWeaponEntity ram) {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, ram.getYRot());
        Vec3 head = ram.launchPoint().add(fwd.scale(0.55));
        for (int h = -1; h <= 1; h++) {
            BlockPos pos = BlockPos.containing(head.add(0.0, h, 0.0));
            if (SiegeImpacts.ramHits(level.getBlockState(pos)) > 0) {
                return true;
            }
        }
        return false;
    }

    /** Liegt die Stelle in Reichweite der Waffe (Wurfwaffen: zwischen kürzestem und weitestem Wurf; Balliste: freie Sicht)? */
    static boolean inRange(ServerLevel level, SiegeWeaponEntity weapon, Vec3 target) {
        SiegeType siege = weapon.siege();
        Vec3 from = weapon.launchPoint();
        double dist = target.subtract(from).horizontalDistance();
        double dy = target.y - from.y;
        if (siege.lobs()) {
            return dist >= SiegeAim.reach(siege, siege.minVelocity(), dy) * 0.9 && dist <= SiegeAim.reach(siege, siege.maxVelocity(), dy);
        }
        if (siege == SiegeType.BALLISTA) {
            return dist <= 64.0 && level.clip(new ClipContext(from, target.add(0.0, 0.5, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE,
                    weapon)).getType() == HitResult.Type.MISS;
        }
        return false;
    }

    /** Wohin zielen, damit das Geschoss ein laufendes Ziel trifft (etwa dort, wo es bei der Ankunft ist). */
    private static Vec3 lead(SiegeWeaponEntity weapon, LivingEntity target) {
        Vec3 at = target.position().add(0.0, target.getBbHeight() * 0.4, 0.0);
        Vec3 v = target.getDeltaMovement().multiply(1.0, 0.0, 1.0);
        double dist = at.subtract(weapon.launchPoint()).horizontalDistance();
        double speed = weapon.siege() == SiegeType.BALLISTA ? weapon.siege().maxVelocity()
                : Math.max(0.5, SiegeAim.solve(weapon.siege(), weapon.launchPoint(), at) * Math.cos(SiegeAim.launchAngle(weapon.siege())));
        return at.add(v.scale(dist / speed * 0.8));
    }

    /** Der nächste Gegner in Reichweite (Monster und feindliche Krieger, keine Verbündeten). */
    private static @Nullable LivingEntity findEnemy(ServerLevel level, SiegeWeaponEntity weapon, SoldierEntity crew) {
        double reach = weapon.siege().lobs() ? SiegeAim.reach(weapon.siege(), weapon.siege().maxVelocity(), 0.0) : 64.0;
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : level.getEntitiesOfClass(LivingEntity.class, weapon.getBoundingBox().inflate(reach, 24.0, reach),
                e -> e.isAlive() && e instanceof Enemy && !(e instanceof Creeper) && !crew.isAlliedTo(e) && crew.canAttack(e))) {
            double d = e.distanceToSqr(weapon);
            if (d < bestDist && inRange(level, weapon, e.position())) {
                best = e;
                bestDist = d;
            }
        }
        return best;
    }

    /** Ein herbeigerufener Soldat läuft zur Waffe und steigt auf. */
    static void tickAssignment(ServerLevel level, SiegeWeaponEntity weapon) {
        Orders orders = weapon.orders;
        if (orders.assigned == null) {
            return;
        }
        if (weapon.isVehicle() || level.getGameTime() - orders.assignedAt > ASSIGN_TICKS
                || !(level.getEntity(orders.assigned) instanceof SoldierEntity soldier) || !soldier.isAlive() || soldier.isPassenger()) {
            orders.assigned = null;
            return;
        }
        if (soldier.distanceToSqr(weapon) < 3.5 * 3.5) {
            soldier.getNavigation().stop();
            if (soldier.startRiding(weapon, true, true)) {
                soldier.setMode(SoldierEntity.MODE_HOLD, weapon.blockPosition());
                soldier.salute();
                level.playSound(null, weapon.getX(), weapon.getY(), weapon.getZ(), SoundEvents.ARMOR_EQUIP_CHAIN.value(), SoundSource.NEUTRAL, 0.8f, 0.9f);
            }
            orders.assigned = null;
        } else if ((level.getGameTime() + weapon.getId()) % 20 == 0) {
            soldier.getNavigation().moveTo(weapon, 1.15);
        }
    }

    // =====================================================================================
    // Kommandostab
    // =====================================================================================

    /**
     * Kommandostab: Befehle an Belagerungswaffen. @return ob der Stab damit etwas getan hat (sonst gelten die gewöhnlichen
     * Befehle an die Soldaten)
     */
    public static boolean baton(ServerLevel level, ServerPlayer player) {
        SiegeWeaponEntity weapon = pickWeapon(player, 8.0);
        if (weapon != null) {
            if (weapon.owner() != null && !weapon.isOwnedBy(player)) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.not_yours").withStyle(ChatFormatting.RED));
                return true;
            }
            if (weapon.owner() == null) {
                weapon.setOwner(player.getUUID());
            }
            Component name = weapon.getDisplayName();
            if (weapon.getFirstPassenger() instanceof SoldierEntity crew) {
                if (player.isSecondaryUseActive()) {
                    crew.stopRiding();
                    weapon.orders.clearTarget();
                    SELECTED.remove(player.getUUID());
                    player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.dismissed", crew.getDisplayName(), name)
                            .withStyle(ChatFormatting.GRAY));
                    return true;
                }
                SELECTED.put(player.getUUID(), new Selection(weapon.getUUID(), level.getGameTime() + SELECT_TICKS));
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, weapon.getX(), weapon.getY() + weapon.getBbHeight() + 0.3, weapon.getZ(), 8, 0.4,
                        0.2, 0.4, 0.0);
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.selected", name).withStyle(ChatFormatting.GOLD));
                return true;
            }
            if (weapon.isVehicle()) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.occupied", name).withStyle(ChatFormatting.GRAY));
                return true;
            }
            SoldierEntity free = nearestFree(player, weapon);
            if (free == null) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.nobody").withStyle(ChatFormatting.RED));
                return true;
            }
            weapon.orders.assigned = free.getUUID();
            weapon.orders.assignedAt = level.getGameTime();
            free.setTarget(null);
            free.setMode(SoldierEntity.MODE_HOLD, weapon.blockPosition());
            free.getNavigation().moveTo(weapon, 1.15);
            free.speak("hold");
            player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.assigned", free.getDisplayName(), name)
                    .withStyle(ChatFormatting.GOLD));
            return true;
        }
        Selection selection = SELECTED.get(player.getUUID());
        if (selection == null) {
            return false;
        }
        if (level.getGameTime() > selection.until() || !(level.getEntity(selection.weapon()) instanceof SiegeWeaponEntity chosen) || !chosen.isAlive()
                || !(chosen.getFirstPassenger() instanceof SoldierEntity)) {
            SELECTED.remove(player.getUUID());
            return false;
        }
        order(level, player, chosen);
        SELECTED.remove(player.getUUID());
        return true;
    }

    /** Das Ziel für die gewählte Waffe: ein Gegner, eine Stelle – oder (in den Himmel gezeigt) freie Wahl. */
    private static void order(ServerLevel level, ServerPlayer player, SiegeWeaponEntity weapon) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(ORDER_RANGE));
        BlockHitResult block = level.clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        EntityHitResult entity = ProjectileUtil.getEntityHitResult(player, eye, limit, new AABB(eye, limit).inflate(1.0),
                e -> e instanceof LivingEntity living && living.isAlive() && !e.isSpectator() && !SiegeImpacts.friendly(player, e),
                eye.distanceToSqr(limit));
        Component name = weapon.getDisplayName();
        Orders orders = weapon.orders;
        if (entity != null && entity.getEntity() instanceof LivingEntity target) {
            orders.clearTarget();
            orders.targetEntity = target.getUUID();
            player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.attack", name, target.getDisplayName())
                    .withStyle(ChatFormatting.GOLD));
        } else if (block.getType() != HitResult.Type.MISS) {
            Vec3 at = block.getLocation();
            if (weapon.siege() != SiegeType.RAM && !inRange(level, weapon, at)) {
                player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.out_of_range", name).withStyle(ChatFormatting.RED));
                return;
            }
            orders.clearTarget();
            orders.targetPos = at;
            level.sendParticles(ParticleTypes.ANGRY_VILLAGER, at.x, at.y + 0.5, at.z, 4, 0.3, 0.2, 0.3, 0.0);
            player.sendOverlayMessage(Component.translatable(weapon.siege() == SiegeType.RAM ? "siege.kingdomsatwar.crew.ram"
                    : "siege.kingdomsatwar.crew.bombard", name).withStyle(ChatFormatting.GOLD));
        } else {
            orders.clearTarget();
            player.sendOverlayMessage(Component.translatable("siege.kingdomsatwar.crew.free", name).withStyle(ChatFormatting.GOLD));
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.NOTE_BLOCK_BELL.value(), SoundSource.PLAYERS, 0.6f, 1.2f);
    }

    /** Auf welche Belagerungswaffe zeigt der Spieler (aus der Nähe)? */
    private static @Nullable SiegeWeaponEntity pickWeapon(Player player, double reach) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(reach));
        BlockHitResult block = player.level().clip(new ClipContext(eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));
        Vec3 limit = block.getType() == HitResult.Type.MISS ? end : block.getLocation();
        EntityHitResult hit = ProjectileUtil.getEntityHitResult(player, eye, limit, new AABB(eye, limit).inflate(1.0),
                e -> e instanceof SiegeWeaponEntity && e.isAlive(), eye.distanceToSqr(limit));
        return hit != null && hit.getEntity() instanceof SiegeWeaponEntity weapon ? weapon : null;
    }

    /** Der nächste eigene Soldat, der nicht schon eine Waffe bedient oder auf dem Weg zu einer ist. */
    private static @Nullable SoldierEntity nearestFree(ServerPlayer player, SiegeWeaponEntity weapon) {
        SoldierEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (SoldierEntity soldier : SoldierEntity.soldiersOf(player, SoldierEntity.COMMAND_RANGE)) {
            if (soldier.isPassenger() || isAssigned(player.level(), soldier)) {
                continue;
            }
            double d = soldier.distanceToSqr(weapon);
            if (d < bestDist) {
                best = soldier;
                bestDist = d;
            }
        }
        return best;
    }

    private static boolean isAssigned(net.minecraft.world.level.Level level, SoldierEntity soldier) {
        for (SiegeWeaponEntity weapon : level.getEntitiesOfClass(SiegeWeaponEntity.class, soldier.getBoundingBox().inflate(64.0))) {
            if (soldier.getUUID().equals(weapon.orders.assigned)) {
                return true;
            }
        }
        return false;
    }

    /** Bedient der Soldat gerade eine Belagerungswaffe (dann gelten die gewöhnlichen Befehle nicht für ihn)? */
    public static boolean isCrew(Entity soldier) {
        return soldier.getVehicle() instanceof SiegeWeaponEntity;
    }
}
