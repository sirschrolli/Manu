package io.github.sirschrolli.kingdomsatwar.entity.villager;

import java.util.EnumSet;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.AbstractBedBlock;
import net.minecraft.world.level.block.AnvilBlock;
import net.minecraft.world.level.block.BedBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BedPart;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Tagesablauf der Dorfbewohner (Tageszeit 0 = Sonnenaufgang): Bis zur zweiten Stunde ist Morgen, dann wird
 * gearbeitet (mit Pausen), am Nachmittag ({@link #GATHER_FROM}) trifft man sich auf dem Dorfplatz, am Abend
 * ({@link #HOME_FROM}) geht es heim, und ab Einbruch der Nacht ({@link #SLEEP_FROM}) wird geschlafen.
 */
public final class VillagerGoals {
    static final long WORK_FROM = 1000;
    static final long GATHER_FROM = 9000;
    static final long HOME_FROM = 11500;
    static final long SLEEP_FROM = 12500;
    static final long WAKE = 23500;

    private VillagerGoals() {}

    /** Nur für Tests: findet ein Bewohner keinen Weg ins Bett, darf er nicht hineinspringen (so fallen Wegprobleme auf). */
    public static boolean allowShortcuts = true;

    static long time(VillagerEntity v) {
        return v.timeOverride >= 0 ? v.timeOverride : Math.floorMod(v.level().getOverworldClockTime(), 24000L);
    }

    static boolean workHours(VillagerEntity v) {
        long t = time(v);
        return t >= WORK_FROM && t < GATHER_FROM;
    }

    static boolean gatherHours(VillagerEntity v) {
        long t = time(v);
        return t >= GATHER_FROM && t < HOME_FROM;
    }

    static boolean evening(VillagerEntity v) {
        long t = time(v);
        return t >= HOME_FROM && t < WAKE;
    }

    /** Ein Platz zum Stehen: fester Boden, darüber zwei Blöcke Luft (kein Wasser). */
    static boolean standable(BlockGetter level, BlockPos pos) {
        BlockState below = level.getBlockState(pos.below());
        return below.isFaceSturdy(level, pos.below(), Direction.UP) && level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()
                && level.getBlockState(pos.above()).getCollisionShape(level, pos.above()).isEmpty()
                && level.getFluidState(pos).isEmpty();
    }

    /** Der nächste freie Platz zum Stehen bei {@code pos} (höchstens {@code radius} Blöcke entfernt). */
    static @Nullable BlockPos freeSpotNear(Level level, BlockPos pos, int radius) {
        for (int r = 0; r <= radius; r++) {
            for (int dy = 0; dy <= 2; dy = dy <= 0 ? 1 - dy : -dy) {
                for (int dx = -r; dx <= r; dx++) {
                    for (int dz = -r; dz <= r; dz++) {
                        if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                            continue;
                        }
                        BlockPos p = pos.offset(dx, dy, dz);
                        if (standable(level, p)) {
                            return p;
                        }
                    }
                }
                if (dy == -2) {
                    break;
                }
            }
        }
        return null;
    }

    /** Läuft zu einem Ziel und meldet, ob es (fast) erreicht ist; neue Wege alle zwei Sekunden. */
    private static boolean walk(VillagerEntity v, Vec3 target, double speed, double reach, int[] repath) {
        if (v.position().distanceToSqr(target) <= reach * reach) {
            v.getNavigation().stop();
            return true;
        }
        if (--repath[0] <= 0 || v.getNavigation().isDone()) {
            repath[0] = 40;
            v.getNavigation().moveTo(target.x, target.y, target.z, speed);
        }
        return false;
    }

    /**
     * Der Weg zu einem Platz im eigenen Haus (Bett, Werkbank). Reicht der gefundene Weg nicht bis ans Ziel – etwa weil der
     * Bewohner hinter dem Haus am Hang steht und der Weg außen herum, durch die Tür und die Treppe hinauf zu lang für einen
     * Anlauf ist –, geht er zuerst vor die eigene Haustür und von dort hinein. Wer schon im Haus ist, geht direkt.
     */
    static final class Route {
        private final VillagerEntity v;
        private final int[] repath = {0};
        private boolean viaDoor;
        /** Von der Tür aus geht es nur noch direkt weiter (sonst pendelt er zwischen Tür und Ziel). */
        private boolean doorReached;
        /** Steht er in einer Sackgasse? (Der beste Weg, den die Suche findet, endet da, wo er schon steht.) */
        private boolean deadEnd;

        Route(VillagerEntity v) {
            this.v = v;
        }

        void reset() {
            this.repath[0] = 0;
            this.viaDoor = false;
            this.doorReached = false;
            this.deadEnd = false;
        }

        /** Geht in Richtung Ziel und meldet, ob es (fast) erreicht ist. */
        boolean walk(Vec3 target, double speed, double reach) {
            if (this.v.position().distanceToSqr(target) <= reach * reach) {
                this.v.getNavigation().stop();
                return true;
            }
            BlockPos door = this.v.getFrontDoor();
            if (this.viaDoor && door != null) {
                Vec3 front = Vec3.atBottomCenterOf(door);
                if (this.v.position().distanceToSqr(front) > 1.6 * 1.6) {
                    if (--this.repath[0] <= 0 || this.v.getNavigation().isDone()) {
                        this.repath[0] = 40;
                        this.v.getNavigation().moveTo(front.x, front.y, front.z, speed);
                        this.deadEnd = stuckHere(this.v.getNavigation().getPath());
                    }
                    return false;
                }
                this.viaDoor = false;
                this.doorReached = true;
                this.repath[0] = 0;
            }
            if (--this.repath[0] <= 0 || this.v.getNavigation().isDone()) {
                this.repath[0] = 40;
                this.v.getNavigation().moveTo(target.x, target.y, target.z, speed);
                Path path = this.v.getNavigation().getPath();
                this.deadEnd = stuckHere(path);
                if ((path == null || !path.canReach()) && door != null && !this.doorReached && !this.v.atHome()
                        && this.v.position().distanceToSqr(Vec3.atBottomCenterOf(door)) > 2.0 * 2.0) {
                    this.viaDoor = true;
                    this.repath[0] = 0;
                }
            }
            return false;
        }

        /** Führt der Weg nirgendwohin (nicht ans Ziel und nicht weg von hier)? */
        private static boolean stuckHere(@Nullable Path path) {
            return path == null || !path.canReach() && path.getNodeCount() <= 1;
        }

        boolean deadEnd() {
            return this.deadEnd;
        }

        boolean viaDoor() {
            return this.viaDoor;
        }
    }

    // =====================================================================================
    // Schlafen
    // =====================================================================================

    /**
     * Abends heim und ins eigene Bett, morgens wieder auf. Wer kein Bett (mehr) hat, verbringt die Nacht am Dorfplatz.
     * Findet ein Bewohner nach langer Zeit keinen Weg ins Bett (etwa weil jemand gebaut hat), geht er hinein, sobald es
     * niemand sieht.
     */
    static final class Sleep extends Goal {
        private final VillagerEntity v;
        private final int[] repath = {0};
        private final Route route;
        private int stuck;
        private double lastDist;

        Sleep(VillagerEntity v) {
            this.v = v;
            this.route = new Route(v);
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return evening(this.v) && this.v.getCustomer() == null && (this.bed() != null || this.v.getVillageCenter() != null);
        }

        @Override
        public boolean canContinueToUse() {
            return evening(this.v) && (this.bed() != null || this.v.getVillageCenter() != null);
        }

        /** Das Kopfende des eigenen Betts, wenn es noch steht. */
        private @Nullable BlockPos bed() {
            BlockPos bed = this.v.getBed();
            if (bed == null) {
                return null;
            }
            BlockState state = this.v.level().getBlockState(bed);
            if (!(state.getBlock() instanceof AbstractBedBlock)) {
                return null;
            }
            if (state.hasProperty(BedBlock.PART) && state.getValue(BedBlock.PART) == BedPart.FOOT) {
                return bed.relative(state.getValue(BedBlock.FACING));
            }
            return bed;
        }

        @Override
        public void start() {
            this.repath[0] = 0;
            this.route.reset();
            this.stuck = 0;
            this.lastDist = Double.MAX_VALUE;
            this.v.setWorking(false);
        }

        @Override
        public void tick() {
            if (this.v.isSleeping()) {
                return;
            }
            BlockPos bed = this.bed();
            if (bed == null) {
                BlockPos center = this.v.getVillageCenter();
                if (center != null) {
                    walk(this.v, Vec3.atBottomCenterOf(center), 0.6, 5.0, this.repath);
                }
                return;
            }
            Level level = this.v.level();
            BlockState state = level.getBlockState(bed);
            Vec3 spot = AbstractBedBlock.findStandUpPosition(this.v.getType(), level, bed, state.getValue(BedBlock.FACING), this.v.getYRot())
                    .orElse(Vec3.atBottomCenterOf(bed.above()));
            double dist = this.v.position().distanceTo(spot);
            Vec3 center = Vec3.atBottomCenterOf(bed);
            // direkt am Bett (nicht etwa draußen hinter der Wand, an der es steht)
            boolean beside = Math.abs(this.v.getY() - bed.getY()) <= 1.0
                    && this.v.position().subtract(center).horizontalDistance() < 2.3 && this.clearTo(bed);
            if (dist < 1.6 || beside) {
                this.v.getNavigation().stop();
                this.v.getLookControl().setLookAt(Vec3.atCenterOf(bed));
                if (time(this.v) >= SLEEP_FROM && !state.getValue(BedBlock.OCCUPIED)) {
                    this.v.startSleeping(bed);
                }
                return;
            }
            this.route.walk(spot, 0.7, 0.8);
            // kommt er nicht voran, geht er hinein, sobald es niemand sieht
            if (dist < this.lastDist - 0.5) {
                this.lastDist = dist;
                this.stuck = 0;
            } else if ((this.stuck += this.route.deadEnd() ? 10 : 1) > 600 && dist < 40 && allowShortcuts && !seen(this.v, spot)) {
                this.v.teleportTo(spot.x, spot.y, spot.z);
                this.stuck = 0;
            }
        }

        /** Nichts zwischen dem Bewohner und dem Bett (keine Wand, kein Fenster). */
        private boolean clearTo(BlockPos bed) {
            Level level = this.v.level();
            Vec3 from = this.v.position().add(0.0, 0.7, 0.0);
            BlockHitResult hit = level.clip(new ClipContext(from, Vec3.atCenterOf(bed), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this.v));
            return hit.getType() == HitResult.Type.MISS || level.getBlockState(hit.getBlockPos()).getBlock() instanceof AbstractBedBlock;
        }

        @Override
        public void stop() {
            if (this.v.isSleeping()) {
                this.v.stopSleeping();
            }
        }
    }

    /**
     * Könnte ein Spieler sehen, wie der Bewohner von seinem Platz nach {@code to} springt? Wer ganz nah ist, merkt es
     * immer; sonst nur, wer freie Sicht auf den Bewohner oder auf den Zielplatz hat.
     */
    static boolean seen(VillagerEntity v, Vec3 to) {
        for (Player player : v.level().players()) {
            if (player.isSpectator()) {
                continue;
            }
            double d = player.distanceToSqr(v);
            if (d < 12 * 12) {
                return true;
            }
            if (d < 96 * 96 && (player.hasLineOfSight(v) || clearView(player, to.add(0.0, 1.2, 0.0)))) {
                return true;
            }
        }
        return false;
    }

    private static boolean clearView(Player player, Vec3 to) {
        Vec3 eye = player.getEyePosition();
        if (eye.distanceToSqr(to) > 96 * 96) {
            return false;
        }
        return player.level().clip(new ClipContext(eye, to, ClipContext.Block.VISUAL, ClipContext.Fluid.NONE, player)).getType()
                == HitResult.Type.MISS;
    }

    // =====================================================================================
    // Kunden bedienen
    // =====================================================================================

    /** Solange ein Spieler mit dem Bewohner handelt, bleibt er stehen und schaut ihn an. */
    static final class Serve extends Goal {
        private final VillagerEntity v;

        Serve(VillagerEntity v) {
            this.v = v;
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK, Flag.JUMP));
        }

        @Override
        public boolean canUse() {
            return this.v.getCustomer() != null && !this.v.isSleeping();
        }

        @Override
        public void start() {
            this.v.getNavigation().stop();
        }

        @Override
        public void tick() {
            Player customer = this.v.getCustomer();
            if (customer != null) {
                this.v.getLookControl().setLookAt(customer, 30.0f, 30.0f);
            }
        }
    }

    // =====================================================================================
    // Unterstellen
    // =====================================================================================

    /** Bei Regen und Gewitter unter ein Dach: an den Arbeitsplatz, wenn er drinnen liegt, sonst ans Bett. */
    static final class Shelter extends Goal {
        private final VillagerEntity v;
        private final Route route;
        private @Nullable BlockPos target;

        Shelter(VillagerEntity v) {
            this.v = v;
            this.route = new Route(v);
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean wet() {
            Level level = this.v.level();
            return level.isRaining() && !evening(this.v) && level.isRainingAt(this.v.blockPosition().above());
        }

        @Override
        public boolean canUse() {
            if (this.v.tickCount % 20 != 0 || !this.wet() || this.v.getCustomer() != null) {
                return false;
            }
            this.target = this.roof();
            return this.target != null;
        }

        private @Nullable BlockPos roof() {
            Level level = this.v.level();
            BlockPos work = this.v.getWork();
            if (work != null && !level.canSeeSky(work.above())) {
                return work;
            }
            BlockPos bed = this.v.getBed();
            if (bed != null && level.getBlockState(bed).getBlock() instanceof AbstractBedBlock) {
                BlockState state = level.getBlockState(bed);
                return AbstractBedBlock.findStandUpPosition(this.v.getType(), level, bed, state.getValue(BedBlock.FACING), 0.0f)
                        .map(BlockPos::containing).orElse(null);
            }
            return null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.target != null && this.v.level().isRaining() && !evening(this.v) && this.v.getCustomer() == null;
        }

        @Override
        public void start() {
            this.route.reset();
            this.v.setWorking(false);
        }

        @Override
        public void tick() {
            if (this.target != null) {
                this.route.walk(Vec3.atBottomCenterOf(this.target), 0.75, 1.2);
            }
        }
    }

    // =====================================================================================
    // Arbeiten
    // =====================================================================================

    /**
     * Tagsüber an den Arbeitsplatz: Werkzeug in die Hand, zur Werkbank schauen, ab und zu ein Schlag mit dem Geräusch
     * des Handwerks. Bauern gehen über ihr Feld und ernten hier und da (und säen gleich wieder), Schäfer gehen in der
     * Weide herum. Nach einer Schicht gibt es eine Pause.
     */
    static final class Work extends Goal {
        private final VillagerEntity v;
        private final Route route;
        private int shiftEnd;
        private int nextAction;
        private @Nullable BlockPos station;
        /** Bauern und Schäfer: die Stelle, zu der sie gerade gehen. */
        private @Nullable BlockPos spot;
        private int stuck;
        private double lastDist;

        Work(VillagerEntity v) {
            this.v = v;
            this.route = new Route(v);
            this.setFlags(EnumSet.of(Flag.MOVE, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return workHours(this.v) && this.v.getWork() != null && !this.v.isSleeping() && this.v.tickCount >= this.v.restUntil
                    && this.v.getCustomer() == null;
        }

        @Override
        public boolean canContinueToUse() {
            return workHours(this.v) && this.v.getWork() != null && !this.v.isSleeping() && this.v.tickCount < this.shiftEnd
                    && this.v.getCustomer() == null;
        }

        @Override
        public void start() {
            boolean peasant = this.v.getProfession() == VillageProfession.PEASANT;
            this.shiftEnd = this.v.tickCount + (peasant ? 600 : 1800) + this.v.getRandom().nextInt(peasant ? 900 : 1800);
            this.route.reset();
            this.nextAction = 40;
            this.spot = null;
            this.stuck = 0;
            this.lastDist = Double.MAX_VALUE;
            BlockPos work = this.v.getWork();
            this.station = work == null ? null : findStation(this.v.level(), work, this.v.getProfession());
        }

        @Override
        public void tick() {
            BlockPos work = this.v.getWork();
            if (work == null) {
                return;
            }
            VillageProfession profession = this.v.getProfession();
            boolean roams = profession == VillageProfession.FARMER;
            BlockPos goal = roams && this.spot != null ? this.spot : work;
            Vec3 target = Vec3.atBottomCenterOf(goal);
            boolean there = this.route.walk(target, 0.6, roams ? 1.4 : 1.2)
                    // nah genug, wenn der Weg zu Ende ist (jemand anderes steht vielleicht genau auf dem Platz)
                    || !this.route.viaDoor() && this.v.getNavigation().isDone() && this.v.position().distanceTo(target) < 1.9;
            if (!there) {
                this.v.setWorking(false);
                // kommt er nicht hin (etwa weil er sich verlaufen hat), geht er hin, sobald es niemand sieht
                double dist = this.v.position().distanceTo(target);
                if (dist < this.lastDist - 0.5) {
                    this.lastDist = dist;
                    this.stuck = 0;
                } else if ((this.stuck += this.route.deadEnd() ? 10 : 1) > 900 && dist < 40 && allowShortcuts && !seen(this.v, target)) {
                    this.v.teleportTo(target.x, target.y, target.z);
                    this.stuck = 0;
                }
                return;
            }
            this.stuck = 0;
            this.v.setWorking(profession != VillageProfession.PEASANT);
            if (this.station != null && !roams) {
                this.v.getLookControl().setLookAt(Vec3.atCenterOf(this.station));
            }
            if (--this.nextAction > 0) {
                return;
            }
            this.nextAction = 50 + this.v.getRandom().nextInt(70);
            if (profession == VillageProfession.PEASANT) {
                return;
            }
            this.v.swingArm();
            SoundEvent sound = profession.workSound();
            Level level = this.v.level();
            if (sound != null && this.v.getRandom().nextInt(3) == 0) {
                level.playSound(null, this.v.getX(), this.v.getY(), this.v.getZ(), sound, SoundSource.NEUTRAL, 0.55f,
                        0.9f + this.v.getRandom().nextFloat() * 0.2f);
            }
            if (level instanceof ServerLevel server) {
                this.effects(server, profession);
            }
            if (roams) {
                if (profession == VillageProfession.FARMER && this.spot != null) {
                    tend(server(level), this.spot, this.v);
                }
                this.spot = this.nextSpot(work, profession);
            }
        }

        /** Nach der Schicht eine Pause (manch einer geht dann auf den Dorfplatz); unterbrochen wird ohne Pause. */
        @Override
        public void stop() {
            this.v.setWorking(false);
            this.v.getNavigation().stop();
            if (this.v.tickCount >= this.shiftEnd) {
                this.v.restUntil = this.v.tickCount + 600 + this.v.getRandom().nextInt(900);
            }
        }

        private static @Nullable ServerLevel server(Level level) {
            return level instanceof ServerLevel s ? s : null;
        }

        /** Funken am Amboss, Staub beim Steinmetz. */
        private void effects(ServerLevel level, VillageProfession profession) {
            if (this.station == null) {
                return;
            }
            Vec3 at = Vec3.atCenterOf(this.station).add(0, 0.55, 0);
            switch (profession) {
                case ARMORER, WEAPONSMITH, TOOLSMITH -> level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 5, 0.15, 0.05, 0.15, 0.12);
                case MASON -> level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), at.x, at.y,
                        at.z, 6, 0.2, 0.05, 0.2, 0.05);
                case BAKER, BUTCHER -> level.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 2, 0.1, 0.05, 0.1, 0.01);
                default -> {
                }
            }
        }

        /** Nächster Schritt über Feld bzw. Weide (höchstens fünf Blöcke vom Arbeitsplatz). */
        private BlockPos nextSpot(BlockPos work, VillageProfession profession) {
            Level level = this.v.level();
            for (int attempt = 0; attempt < 12; attempt++) {
                BlockPos p = work.offset(this.v.getRandom().nextInt(11) - 5, 0, this.v.getRandom().nextInt(11) - 5);
                for (int dy = 1; dy >= -2; dy--) {
                    BlockPos q = p.above(dy);
                    BlockState crop = level.getBlockState(q);
                    if (profession == VillageProfession.FARMER ? crop.getBlock() instanceof CropBlock && level.getBlockState(q.below()).is(Blocks.FARMLAND)
                            : standable(level, q)) {
                        return q.immutable();
                    }
                }
            }
            return work;
        }
    }

    /** Ein Bauer erntet hier und da eine reife Frucht und sät gleich wieder (es fällt nichts heraus). */
    private static void tend(@Nullable ServerLevel level, BlockPos pos, VillagerEntity v) {
        if (level == null) {
            return;
        }
        BlockState state = level.getBlockState(pos);
        if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state) && v.getRandom().nextInt(4) == 0) {
            level.levelEvent(2001, pos, Block.getId(state));
            level.setBlockAndUpdate(pos, crop.getStateForAge(0));
        }
    }

    /** Werkstatt-Blöcke je Beruf: dorthin schaut der Bewohner bei der Arbeit. */
    private static boolean isStation(BlockState state, VillageProfession profession) {
        Block b = state.getBlock();
        return switch (profession) {
            case ARMORER -> b == Blocks.BLAST_FURNACE || b instanceof AnvilBlock || b == Blocks.SMITHING_TABLE;
            case WEAPONSMITH -> b == Blocks.GRINDSTONE || b instanceof AnvilBlock;
            case TOOLSMITH -> b == Blocks.SMITHING_TABLE || b instanceof AnvilBlock;
            case FLETCHER -> b == Blocks.FLETCHING_TABLE;
            case SCRIBE -> b == Blocks.LECTERN;
            case CARTOGRAPHER -> b == Blocks.CARTOGRAPHY_TABLE;
            case HERBALIST -> b == Blocks.BREWING_STAND || b == Blocks.CAULDRON || b == Blocks.WATER_CAULDRON;
            case BUTCHER -> b == Blocks.SMOKER;
            case TANNER -> b == Blocks.WATER_CAULDRON || b == Blocks.CAULDRON || b == Blocks.BARREL;
            case MASON -> b == Blocks.STONECUTTER;
            case BAKER -> b == Blocks.SMOKER || b == Blocks.FURNACE;
            case SHEPHERD -> b == Blocks.LOOM;
            case FARMER -> b == Blocks.COMPOSTER;
            case FISHER, INNKEEPER, MERCHANT -> b == Blocks.BARREL;
            case PEASANT -> b == Blocks.CRAFTING_TABLE;
        };
    }

    private static final Set<Block> ANY_STATION = Set.of(Blocks.CRAFTING_TABLE, Blocks.BARREL, Blocks.FURNACE, Blocks.SMOKER,
            Blocks.LECTERN, Blocks.CARTOGRAPHY_TABLE);

    static @Nullable BlockPos findStation(Level level, BlockPos work, VillageProfession profession) {
        BlockPos fallback = null;
        for (BlockPos p : BlockPos.withinManhattan(work, 3)) {
            BlockState state = level.getBlockState(p);
            if (isStation(state, profession)) {
                return p.immutable();
            }
            if (fallback == null && ANY_STATION.contains(state.getBlock())) {
                fallback = p.immutable();
            }
        }
        return fallback;
    }

    // =====================================================================================
    // Dorfplatz
    // =====================================================================================

    /**
     * Am Nachmittag trifft sich das Dorf auf dem Platz: Jeder sucht sich eine Stelle, bleibt eine Weile stehen und schaut
     * den anderen zu, dann sucht er sich eine neue. Auch in den Arbeitspausen geht mancher hinüber.
     */
    static final class Gather extends Goal {
        private final VillagerEntity v;
        private final int[] repath = {0};
        private @Nullable BlockPos spot;
        private int linger;

        Gather(VillagerEntity v) {
            this.v = v;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        private boolean wanted() {
            if (gatherHours(this.v)) {
                return true;
            }
            return workHours(this.v) && this.v.tickCount < this.v.restUntil;
        }

        @Override
        public boolean canUse() {
            if (this.v.getVillageCenter() == null || this.v.isSleeping() || this.v.getCustomer() != null || !this.wanted()) {
                return false;
            }
            if (this.v.getRandom().nextInt(gatherHours(this.v) ? 20 : 80) != 0) {
                return false;
            }
            this.spot = this.pick();
            return this.spot != null;
        }

        @Override
        public boolean canContinueToUse() {
            return this.spot != null && this.wanted() && this.v.getCustomer() == null;
        }

        @Override
        public void start() {
            this.repath[0] = 0;
            this.linger = 0;
            this.v.setWorking(false);
        }

        @Override
        public void tick() {
            if (this.spot == null) {
                return;
            }
            if (walk(this.v, Vec3.atBottomCenterOf(this.spot), 0.55, 1.5, this.repath)) {
                if (++this.linger > 300 + this.v.getRandom().nextInt(300)) {
                    this.linger = 0;
                    BlockPos next = this.pick();
                    if (next != null) {
                        this.spot = next;
                    }
                }
            }
        }

        /** Eine Stelle auf dem Platz (oder kurz dahinter) zum Stehen. */
        private @Nullable BlockPos pick() {
            BlockPos center = this.v.getVillageCenter();
            if (center == null) {
                return null;
            }
            Level level = this.v.level();
            for (int attempt = 0; attempt < 16; attempt++) {
                double angle = this.v.getRandom().nextDouble() * Math.PI * 2.0;
                double r = 3.0 + this.v.getRandom().nextDouble() * 5.0;
                BlockPos p = BlockPos.containing(center.getX() + 0.5 + Math.cos(angle) * r, center.getY() + 1, center.getZ() + 0.5 + Math.sin(angle) * r);
                for (int dy = 0; dy <= 3; dy = dy <= 0 ? 1 - dy : -dy) {
                    BlockPos q = p.above(dy);
                    if (standable(level, q)) {
                        return q.immutable();
                    }
                    if (dy == -3) {
                        break;
                    }
                }
            }
            return null;
        }
    }

    // =====================================================================================
    // Im Dorf bleiben
    // =====================================================================================

    /** Wer sich zu weit vom Dorf entfernt hat (etwa auf der Flucht), geht zurück. */
    static final class StayInVillage extends Goal {
        private final VillagerEntity v;

        StayInVillage(VillagerEntity v) {
            this.v = v;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BlockPos center = this.v.getVillageCenter();
            if (center == null || this.v.tickCount % 40 != 0) {
                return false;
            }
            return this.v.blockPosition().distSqr(center) > (double) VillagerEntity.VILLAGE_RADIUS * VillagerEntity.VILLAGE_RADIUS;
        }

        @Override
        public void start() {
            BlockPos center = this.v.getVillageCenter();
            if (center != null) {
                this.v.getNavigation().moveTo(center.getX() + 0.5, center.getY() + 1, center.getZ() + 0.5, 0.75);
            }
        }

        @Override
        public boolean canContinueToUse() {
            return !this.v.getNavigation().isDone();
        }
    }
}
