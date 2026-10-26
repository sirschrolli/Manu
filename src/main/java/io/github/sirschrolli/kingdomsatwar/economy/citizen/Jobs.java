package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Arbeit der Berufe. Jeder Beruf ist eine kleine Abfolge von Schritten (hingehen, arbeiten, abliefern); was die
 * Bürger erarbeiten, bringen sie in die Truhe ihrer Werkstatt.
 */
final class Jobs {
    private Jobs() {}

    static Job create(CitizenEntity citizen) {
        return switch (citizen.getProfession()) {
            case BUILDER -> new BuilderJob(citizen);
            case FARMER -> new FarmerJob(citizen);
            case LUMBERJACK -> new LumberjackJob(citizen);
            case MINER -> new MinerJob(citizen);
            default -> new ShopJob(citizen);
        };
    }

    // =====================================================================================
    // Grundlage
    // =====================================================================================

    abstract static class Job {
        final CitizenEntity citizen;
        private int repath;
        private @Nullable BlockPos goal;
        private double bestDist = Double.MAX_VALUE;
        private int stuck;

        Job(CitizenEntity citizen) {
            this.citizen = citizen;
        }

        Profession profession() {
            return this.citizen.getProfession();
        }

        ServerLevel level() {
            return (ServerLevel) this.citizen.level();
        }

        @Nullable Town town() {
            return this.citizen.town();
        }

        /** Das Gebäude, in dem der Bürger arbeitet. */
        Town.@Nullable Building workplace() {
            Town town = this.town();
            BlockPos work = this.citizen.getWork();
            return town == null || work == null ? null : town.building(work);
        }

        abstract boolean canStart();

        void start() {
        }

        abstract boolean running();

        abstract void tick();

        void stop() {
            this.citizen.getNavigation().stop();
        }

        /**
         * Geht zu einer Stelle; {@code true}, sobald sie näher als {@code reach} ist. Bleibt der Bürger lange stecken,
         * eilt er (ungesehen) hinüber.
         */
        boolean walkTo(BlockPos target, double reach, double speed) {
            double dist = this.citizen.position().distanceToSqr(Vec3.atBottomCenterOf(target));
            if (dist <= reach * reach) {
                this.citizen.getNavigation().stop();
                this.goal = null;
                return true;
            }
            if (!target.equals(this.goal)) {
                this.goal = target;
                this.bestDist = dist;
                this.stuck = 0;
                this.repath = 0;
            }
            if (dist < this.bestDist - 0.5) {
                this.bestDist = dist;
                this.stuck = 0;
            } else if (++this.stuck > 240) {
                // ein Weg findet sich nicht: hinübereilen (neben das Ziel, auf freien Boden)
                BlockPos spot = standingSpot(this.level(), target);
                if (spot != null) {
                    this.citizen.teleportTo(spot.getX() + 0.5, spot.getY(), spot.getZ() + 0.5);
                }
                this.stuck = 0;
                this.bestDist = Double.MAX_VALUE;
            }
            if (--this.repath <= 0 || this.citizen.getNavigation().isDone()) {
                this.repath = 30;
                this.citizen.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, speed);
            }
            return false;
        }

        void lookAt(BlockPos pos) {
            this.citizen.getLookControl().setLookAt(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
        }

        /** Arm schwingen mit Geräusch und Splittern des Blocks. */
        void strike(BlockPos pos, BlockState state) {
            this.citizen.swingArm();
            SoundType sound = state.getSoundType();
            this.level().playSound(null, pos, sound.getHitSound(), SoundSource.BLOCKS, sound.getVolume() * 0.6f, sound.getPitch() * 0.8f);
            if (!state.isAir()) {
                this.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5,
                        pos.getZ() + 0.5, 6, 0.25, 0.25, 0.25, 0.05);
            }
        }

        /** Trägt der Bürger genug, um es abzuliefern? */
        boolean loaded(int items) {
            int n = 0;
            SimpleContainer carried = this.citizen.carried();
            for (int i = 0; i < carried.getContainerSize(); i++) {
                n += carried.getItem(i).getCount();
            }
            return n >= items || carried.getItem(carried.getContainerSize() - 1).getCount() > 0;
        }

        boolean carriesAnything() {
            return !this.citizen.carried().isEmpty();
        }

        /** Bringt alles in die Truhe der Werkstatt; {@code true}, wenn abgeliefert (oder keine Truhe da ist). */
        boolean deliver() {
            Town.Building work = this.workplace();
            BlockPos chestLocal = work == null ? null : work.type().blueprint().chest();
            if (work == null || chestLocal == null) {
                return true;
            }
            BlockPos chest = work.world(chestLocal);
            if (!this.walkTo(chest, 2.4, 0.7)) {
                return false;
            }
            this.lookAt(chest);
            BlockEntity be = this.level().getBlockEntity(chest);
            if (be instanceof Container container) {
                SimpleContainer carried = this.citizen.carried();
                for (int i = 0; i < carried.getContainerSize(); i++) {
                    ItemStack stack = carried.getItem(i);
                    if (!stack.isEmpty()) {
                        carried.setItem(i, insert(container, stack));
                    }
                }
                container.setChanged();
                BlockState state = this.level().getBlockState(chest);
                if (state.getBlock() instanceof ChestBlock) {
                    this.level().blockEvent(chest, state.getBlock(), 1, 1);
                    this.level().playSound(null, chest, SoundEvents.CHEST_OPEN, SoundSource.BLOCKS, 0.5f, 1.0f);
                    this.level().scheduleTick(chest, state.getBlock(), 1);
                }
                this.citizen.swingArm();
            }
            return true;
        }

        void carry(ItemStack stack) {
            ItemStack rest = this.citizen.carried().addItem(stack);
            if (!rest.isEmpty()) {
                // voll: der Rest bleibt liegen
                this.citizen.spawnAtLocation(this.level(), rest);
            }
        }
    }

    /** Legt einen Stapel in einen Behälter; gibt zurück, was nicht hineinpasst. */
    static ItemStack insert(Container container, ItemStack stack) {
        ItemStack rest = stack.copy();
        for (int i = 0; i < container.getContainerSize() && !rest.isEmpty(); i++) {
            ItemStack slot = container.getItem(i);
            if (!slot.isEmpty() && ItemStack.isSameItemSameComponents(slot, rest) && slot.getCount() < slot.getMaxStackSize()) {
                int move = Math.min(rest.getCount(), slot.getMaxStackSize() - slot.getCount());
                slot.grow(move);
                rest.shrink(move);
            }
        }
        for (int i = 0; i < container.getContainerSize() && !rest.isEmpty(); i++) {
            if (container.getItem(i).isEmpty()) {
                container.setItem(i, rest.copy());
                rest = ItemStack.EMPTY;
            }
        }
        return rest;
    }

    /** Ein freier Stehplatz bei einer Stelle (fester Boden, zwei Blöcke Luft). */
    static @Nullable BlockPos standingSpot(ServerLevel level, BlockPos near) {
        for (int r = 0; r <= 3; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) {
                        continue;
                    }
                    for (int dy = 1; dy >= -2; dy--) {
                        BlockPos p = near.offset(dx, dy, dz);
                        if (level.getBlockState(p.below()).isFaceSturdy(level, p.below(), Direction.UP)
                                && level.getBlockState(p).getCollisionShape(level, p).isEmpty()
                                && level.getBlockState(p.above()).getCollisionShape(level, p.above()).isEmpty()) {
                            return p;
                        }
                    }
                }
            }
        }
        return null;
    }

    // =====================================================================================
    // Baumeister
    // =====================================================================================

    /**
     * Der Baumeister arbeitet die Baustellen der Siedlung ab: erst räumt er den Platz, dann legt er das Fundament, dann
     * setzt er Block für Block den Bauplan – mit Hammerschlag, Geräusch und Staub.
     */
    static final class BuilderJob extends Job {
        private int cooldown;
        private int index;
        /** Wie lange der Baumeister schon auf der Baustelle steht, statt davor. */
        private int onSiteTicks;
        /** So weit reicht der Baumeister von seinem Platz neben der Baustelle (Blöcke). */
        private static final double REACH = 16.0;

        BuilderJob(CitizenEntity citizen) {
            super(citizen);
        }

        private Town.@Nullable Project project() {
            Town town = this.town();
            if (town == null || town.projects().isEmpty()) {
                return null;
            }
            // mehrere Baumeister teilen sich die Baustellen
            List<Town.Resident> builders = new ArrayList<>();
            for (Town.Resident r : town.residents()) {
                if (r.profession() == Profession.BUILDER) {
                    builders.add(r);
                }
            }
            int me = 0;
            for (int i = 0; i < builders.size(); i++) {
                if (builders.get(i).entity().equals(this.citizen.getUUID())) {
                    me = i;
                }
            }
            this.index = me;
            return town.projects().get(me % town.projects().size());
        }

        @Override
        boolean canStart() {
            return this.project() != null;
        }

        @Override
        boolean running() {
            return this.project() != null;
        }

        @Override
        void tick() {
            Town town = this.town();
            Town.Project project = this.project();
            if (town == null || project == null) {
                return;
            }
            List<Blueprint.Step> steps = project.steps();
            int skipped = 0;
            while (project.progress < steps.size() && skipped < 48 && !Blueprint.needsWork(this.level(), steps.get(project.progress))) {
                project.progress++;
                skipped++;
            }
            if (project.progress >= steps.size()) {
                TownManager.completeProject(this.level(), town, project);
                this.citizen.setWorking(false);
                return;
            }
            Blueprint.Step step = steps.get(project.progress);
            BlockPos at = step.pos();
            // nah genug heran (der Baumeister reicht weit) – gebaut wird aber nur von außerhalb der Baustelle,
            // sonst mauerte er sich selbst ein
            var box = project.bounds();
            BlockPos feet = this.citizen.blockPosition();
            boolean onSite = feet.getX() >= box.minX() - 1 && feet.getX() <= box.maxX() + 1
                    && feet.getZ() >= box.minZ() - 1 && feet.getZ() <= box.maxZ() + 1;
            if (onSite) {
                // von der Baustelle herunter (auf kürzestem Weg); findet er keinen Weg hinaus, tritt er hinaus
                this.citizen.setWorking(false);
                BlockPos exit = this.exit(box);
                if (++this.onSiteTicks > 80) {
                    this.citizen.teleportTo(exit.getX() + 0.5, exit.getY(), exit.getZ() + 0.5);
                    this.onSiteTicks = 0;
                }
                this.walkTo(exit, 1.0, 0.85);
                return;
            }
            BlockPos approach = this.approach(project, at);
            boolean arrived = this.citizen.position().distanceToSqr(Vec3.atBottomCenterOf(approach)) <= 2.5 * 2.5;
            boolean near = this.citizen.position().distanceToSqr(Vec3.atCenterOf(at)) <= REACH * REACH;
            if (!near && !arrived) {
                this.citizen.setWorking(false);
                this.walkTo(approach, 2.0, 0.85);
                return;
            }
            this.onSiteTicks = 0;
            this.citizen.getNavigation().stop();
            this.citizen.setWorking(true);
            this.lookAt(at);
            if (--this.cooldown > 0) {
                return;
            }
            BlockState before = this.level().getBlockState(at);
            Blueprint.apply(this.level(), step);
            BlockState shown = step.state().isAir() ? before : step.state();
            this.citizen.swingArm();
            if (!shown.isAir()) {
                SoundType sound = shown.getSoundType();
                this.level().playSound(null, at, step.state().isAir() ? sound.getBreakSound() : sound.getPlaceSound(), SoundSource.BLOCKS,
                        0.7f, sound.getPitch() * 0.9f);
                this.level().sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, shown), at.getX() + 0.5, at.getY() + 0.5,
                        at.getZ() + 0.5, 5, 0.3, 0.3, 0.3, 0.05);
            }
            project.progress++;
            // Wegräumen geht schneller als Bauen
            this.cooldown = step.state().isAir() ? 1 : 3;
        }

        /** Der nächste Platz neben der Baustelle (zwei Blöcke außerhalb ihres Umrisses), vom Baumeister aus. */
        private BlockPos exit(net.minecraft.world.level.levelgen.structure.BoundingBox box) {
            BlockPos feet = this.citizen.blockPosition();
            int x = feet.getX();
            int z = feet.getZ();
            int toMinX = x - (box.minX() - 2);
            int toMaxX = box.maxX() + 2 - x;
            int toMinZ = z - (box.minZ() - 2);
            int toMaxZ = box.maxZ() + 2 - z;
            int m = Math.min(Math.min(toMinX, toMaxX), Math.min(toMinZ, toMaxZ));
            if (m == toMinX) {
                x = box.minX() - 2;
            } else if (m == toMaxX) {
                x = box.maxX() + 2;
            } else if (m == toMinZ) {
                z = box.minZ() - 2;
            } else {
                z = box.maxZ() + 2;
            }
            int y = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            return new BlockPos(x, y, z);
        }

        /** Wo der Baumeister steht: vor der Baustelle, auf der Seite des Bauschritts. */
        private BlockPos approach(Town.Project project, BlockPos at) {
            var box = project.bounds();
            int x = Math.max(box.minX() - 2, Math.min(box.maxX() + 2, at.getX()));
            int z = Math.max(box.minZ() - 2, Math.min(box.maxZ() + 2, at.getZ()));
            // außerhalb der Grundfläche bleiben
            if (x > box.minX() - 2 && x < box.maxX() + 2 && z > box.minZ() - 2 && z < box.maxZ() + 2) {
                int toMinX = x - (box.minX() - 2);
                int toMaxX = box.maxX() + 2 - x;
                int toMinZ = z - (box.minZ() - 2);
                int toMaxZ = box.maxZ() + 2 - z;
                int m = Math.min(Math.min(toMinX, toMaxX), Math.min(toMinZ, toMaxZ));
                if (m == toMinX) {
                    x = box.minX() - 2;
                } else if (m == toMaxX) {
                    x = box.maxX() + 2;
                } else if (m == toMinZ) {
                    z = box.minZ() - 2;
                } else {
                    z = box.maxZ() + 2;
                }
            }
            int y = this.level().getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
            return new BlockPos(x, y, z);
        }
    }

    // =====================================================================================
    // Bauer
    // =====================================================================================

    /**
     * Der Bauer erntet reife Feldfrüchte auf seinem Feld, sät sofort neu und pflegt die Pflanzen (sie wachsen dann
     * schneller); die Ernte bringt er in die Truhe am Feld.
     */
    static final class FarmerJob extends Job {
        private @Nullable BlockPos target;
        private int work;
        private int tend;

        FarmerJob(CitizenEntity citizen) {
            super(citizen);
        }

        private List<BlockPos> crops() {
            Town.Building field = this.workplace();
            List<BlockPos> crops = new ArrayList<>();
            if (field == null) {
                return crops;
            }
            for (Blueprint.Entry e : field.type().blueprint().blocks()) {
                if (e.state().getBlock() instanceof CropBlock) {
                    crops.add(field.world(e.pos()));
                }
            }
            return crops;
        }

        @Override
        boolean canStart() {
            return this.workplace() != null;
        }

        @Override
        boolean running() {
            return this.workplace() != null;
        }

        @Override
        void tick() {
            if (this.loaded(24) || (this.carriesAnything() && CitizenGoals.time(this.citizen) > 11000)) {
                this.citizen.setWorking(false);
                this.deliver();
                return;
            }
            ServerLevel level = this.level();
            if (this.target == null || !this.worthIt(this.target)) {
                this.target = this.nextCrop();
                this.work = 0;
            }
            if (this.target == null) {
                // nichts reif: Pflanzen pflegen
                this.tendField();
                return;
            }
            if (!this.walkTo(this.target, 1.8, 0.7)) {
                this.citizen.setWorking(false);
                return;
            }
            this.citizen.setWorking(true);
            this.lookAt(this.target);
            if (++this.work < 16) {
                if (this.work % 6 == 0) {
                    this.citizen.swingArm();
                }
                return;
            }
            BlockState state = level.getBlockState(this.target);
            if (state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)) {
                for (ItemStack drop : Block.getDrops(state, level, this.target, null, this.citizen, this.citizen.getMainHandItem())) {
                    this.carry(drop);
                }
                level.levelEvent(2001, this.target, Block.getId(state));
                level.setBlock(this.target, crop.getStateForAge(0), Block.UPDATE_ALL);
                this.useSeed(crop);
            } else if (state.isAir() && level.getBlockState(this.target.below()).is(Blocks.FARMLAND)) {
                Block crop = this.originalCrop(this.target);
                if (crop != null) {
                    level.setBlock(this.target, crop.defaultBlockState(), Block.UPDATE_ALL);
                    level.playSound(null, this.target, SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 0.8f, 1.0f);
                }
            }
            this.citizen.swingArm();
            this.target = null;
        }

        private boolean worthIt(BlockPos pos) {
            BlockState state = this.level().getBlockState(pos);
            return state.getBlock() instanceof CropBlock crop && crop.isMaxAge(state)
                    || state.isAir() && this.level().getBlockState(pos.below()).is(Blocks.FARMLAND);
        }

        private @Nullable BlockPos nextCrop() {
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (BlockPos pos : this.crops()) {
                if (this.worthIt(pos)) {
                    double d = pos.distSqr(this.citizen.blockPosition());
                    if (d < bestDist) {
                        best = pos;
                        bestDist = d;
                    }
                }
            }
            return best;
        }

        private @Nullable Block originalCrop(BlockPos world) {
            Town.Building field = this.workplace();
            if (field == null) {
                return null;
            }
            for (Blueprint.Entry e : field.type().blueprint().blocks()) {
                if (field.world(e.pos()).equals(world) && e.state().getBlock() instanceof CropBlock) {
                    return e.state().getBlock();
                }
            }
            return null;
        }

        /** Für das Nachsäen wird ein Samen aus der Ernte verbraucht, wenn einer da ist. */
        private void useSeed(CropBlock crop) {
            ItemStack seed = new ItemStack(crop.asItem());
            SimpleContainer carried = this.citizen.carried();
            for (int i = 0; i < carried.getContainerSize(); i++) {
                if (ItemStack.isSameItem(carried.getItem(i), seed)) {
                    carried.getItem(i).shrink(1);
                    return;
                }
            }
        }

        private void tendField() {
            List<BlockPos> crops = this.crops();
            if (crops.isEmpty()) {
                return;
            }
            RandomSource random = this.citizen.getRandom();
            if (this.target == null && --this.tend <= 0) {
                BlockPos pick = crops.get(random.nextInt(crops.size()));
                this.tend = 60;
                if (this.walkTo(pick, 1.8, 0.6)) {
                    this.citizen.swingArm();
                    BlockState state = this.level().getBlockState(pick);
                    if (state.getBlock() instanceof CropBlock && state.isRandomlyTicking()) {
                        // gepflegte Pflanzen wachsen schneller
                        state.randomTick(this.level(), pick, random);
                        this.level().sendParticles(ParticleTypes.HAPPY_VILLAGER, pick.getX() + 0.5, pick.getY() + 0.4, pick.getZ() + 0.5,
                                3, 0.25, 0.2, 0.25, 0.0);
                    }
                } else {
                    this.tend = 2;
                }
            }
        }
    }

    // =====================================================================================
    // Holzfäller
    // =====================================================================================

    /**
     * Der Holzfäller sucht Bäume rund um seine Hütte (nur natürliche Bäume mit Laub, nie Balken aus Häusern), fällt sie
     * Stamm für Stamm von oben nach unten, pflanzt einen Setzling nach und bringt das Holz in die Truhe der Hütte.
     */
    static final class LumberjackJob extends Job {
        private final ArrayDeque<BlockPos> logs = new ArrayDeque<>();
        private @Nullable BlockPos base;
        private @Nullable Block sapling;
        private int chop;
        private int searchCooldown;

        LumberjackJob(CitizenEntity citizen) {
            super(citizen);
        }

        @Override
        boolean canStart() {
            return this.workplace() != null;
        }

        @Override
        boolean running() {
            return this.workplace() != null;
        }

        @Override
        void tick() {
            if (this.loaded(32) || (this.logs.isEmpty() && this.carriesAnything() && CitizenGoals.time(this.citizen) > 11000)) {
                this.citizen.setWorking(false);
                if (this.deliver() && this.logs.isEmpty()) {
                    this.base = null;
                }
                return;
            }
            if (this.logs.isEmpty()) {
                if (this.base != null) {
                    this.replant();
                    this.base = null;
                }
                if (--this.searchCooldown <= 0) {
                    this.searchCooldown = 100;
                    this.findTree();
                }
                if (this.logs.isEmpty()) {
                    this.citizen.setWorking(false);
                    return;
                }
            }
            BlockPos log = this.logs.peekLast();
            BlockState state = this.level().getBlockState(log);
            if (!state.is(BlockTags.LOGS)) {
                this.logs.pollLast();
                return;
            }
            BlockPos stand = this.base != null ? this.base : log;
            if (!this.walkTo(stand, 2.6, 0.75)) {
                this.citizen.setWorking(false);
                return;
            }
            this.citizen.setWorking(true);
            this.lookAt(log);
            if (++this.chop % 6 == 0) {
                this.strike(log, state);
            }
            if (this.chop >= 18) {
                this.chop = 0;
                for (ItemStack drop : Block.getDrops(state, this.level(), log, null, this.citizen, this.citizen.getMainHandItem())) {
                    this.carry(drop);
                }
                this.level().levelEvent(2001, log, Block.getId(state));
                this.level().removeBlock(log, false);
                this.logs.pollLast();
            }
        }

        /** Sucht den nächsten natürlichen Baum (Stamm auf Erde, oben Laub) im Umkreis von 24 Blöcken um die Hütte. */
        private void findTree() {
            Town.Building hut = this.workplace();
            Town town = this.town();
            if (hut == null || town == null) {
                return;
            }
            ServerLevel level = this.level();
            BlockPos center = hut.origin();
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int dx = -24; dx <= 24; dx += 1) {
                for (int dz = -24; dz <= 24; dz += 1) {
                    int x = center.getX() + dx;
                    int z = center.getZ() + dz;
                    int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                    for (int y = top - 1; y >= top - 3; y--) {
                        p.set(x, y, z);
                        BlockState state = level.getBlockState(p);
                        if (state.is(BlockTags.LOGS) && level.getBlockState(p.below()).is(BlockTags.DIRT) && !insideBuilding(town, p)) {
                            double d = p.distSqr(center);
                            if (d < bestDist && hasLeaves(level, p)) {
                                best = p.immutable();
                                bestDist = d;
                            }
                            break;
                        }
                    }
                }
            }
            if (best == null) {
                return;
            }
            // alle verbundenen Stämme (höchstens 96), von unten nach oben – gefällt wird von oben
            Set<BlockPos> seen = new HashSet<>();
            ArrayDeque<BlockPos> open = new ArrayDeque<>();
            List<BlockPos> found = new ArrayList<>();
            open.add(best);
            seen.add(best);
            while (!open.isEmpty() && found.size() < 96) {
                BlockPos cur = open.poll();
                found.add(cur);
                for (int dx = -1; dx <= 1; dx++) {
                    for (int dy = 0; dy <= 1; dy++) {
                        for (int dz = -1; dz <= 1; dz++) {
                            BlockPos next = cur.offset(dx, dy, dz);
                            if (seen.add(next) && level.getBlockState(next).is(BlockTags.LOGS) && !insideBuilding(town, next)) {
                                open.add(next);
                            }
                        }
                    }
                }
            }
            found.sort(Comparator.comparingInt(BlockPos::getY));
            this.logs.addAll(found);
            this.base = best;
            this.sapling = saplingFor(level.getBlockState(best).getBlock());
        }

        private void replant() {
            if (this.base == null || this.sapling == null) {
                return;
            }
            ServerLevel level = this.level();
            if (level.getBlockState(this.base).isAir() && level.getBlockState(this.base.below()).is(BlockTags.DIRT)) {
                level.setBlock(this.base, this.sapling.defaultBlockState(), Block.UPDATE_ALL);
                level.playSound(null, this.base, SoundEvents.GRASS_PLACE, SoundSource.BLOCKS, 0.8f, 1.0f);
                this.citizen.swingArm();
            }
        }

        private static boolean hasLeaves(ServerLevel level, BlockPos log) {
            for (int dy = 2; dy <= 12; dy++) {
                for (Direction d : Direction.Plane.HORIZONTAL) {
                    if (level.getBlockState(log.above(dy).relative(d)).is(BlockTags.LEAVES)) {
                        return true;
                    }
                }
                if (!level.getBlockState(log.above(dy)).is(BlockTags.LOGS)) {
                    return level.getBlockState(log.above(dy)).is(BlockTags.LEAVES);
                }
            }
            return false;
        }

        private static @Nullable Block saplingFor(Block log) {
            String id = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(log).getPath().replace("stripped_", "");
            String wood = id.endsWith("_log") ? id.substring(0, id.length() - 4) : id.endsWith("_wood") ? id.substring(0, id.length() - 5) : id;
            var key = net.minecraft.resources.Identifier.withDefaultNamespace(wood.equals("mangrove") ? "mangrove_propagule" : wood + "_sapling");
            Block sapling = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getValue(key);
            return sapling == Blocks.AIR ? Blocks.OAK_SAPLING : sapling;
        }
    }

    /** Liegt die Stelle in einem Gebäude oder auf einer Baustelle der Siedlung? */
    static boolean insideBuilding(Town town, BlockPos pos) {
        for (Town.Building b : town.buildings()) {
            if (b.bounds().inflatedBy(1).isInside(pos)) {
                return true;
            }
        }
        for (Town.Project p : town.projects()) {
            if (p.bounds().inflatedBy(1).isInside(pos)) {
                return true;
            }
        }
        return false;
    }

    // =====================================================================================
    // Bergmann
    // =====================================================================================

    /**
     * Der Bergmann steigt die Leiter in die Kammer seiner Mine hinab, hackt an der Erzwand (Kohle, Eisen, Kupfer, selten
     * Gold und Edelsteine), steigt wieder hinauf und bringt die Ausbeute in die Truhe.
     */
    static final class MinerJob extends Job {
        private enum Phase { TO_SHAFT, DOWN, DIG, UP, DELIVER }

        private Phase phase = Phase.TO_SHAFT;
        private int dig;

        MinerJob(CitizenEntity citizen) {
            super(citizen);
        }

        @Override
        boolean canStart() {
            return this.workplace() != null;
        }

        @Override
        boolean running() {
            return this.workplace() != null;
        }

        @Override
        void start() {
            Town.Building mine = this.workplace();
            // wer gerade unten ist (z. B. nach dem Laden der Welt), arbeitet unten weiter
            if (mine != null && this.citizen.getY() < mine.origin().getY() - 3) {
                this.phase = Phase.DIG;
            } else {
                this.phase = this.carriesAnything() ? Phase.DELIVER : Phase.TO_SHAFT;
            }
        }

        @Override
        void stop() {
            super.stop();
            this.citizen.setNoGravity(false);
        }

        @Override
        void tick() {
            Town.Building mine = this.workplace();
            if (mine == null) {
                return;
            }
            Blueprint plan = mine.type().blueprint();
            BlockPos top = mine.world(new BlockPos(0, 1, 1));
            BlockPos bottom = mine.world(new BlockPos(0, -11, 1));
            BlockPos spot = plan.workSpot() != null ? mine.world(plan.workSpot()) : bottom;
            boolean endOfDay = CitizenGoals.time(this.citizen) > 10500;
            switch (this.phase) {
                case TO_SHAFT -> {
                    this.citizen.setWorking(false);
                    if (this.walkTo(mine.world(new BlockPos(0, 1, 2)), 1.2, 0.8)) {
                        this.phase = Phase.DOWN;
                    }
                }
                case DOWN -> {
                    if (this.climb(top, bottom, -0.18)) {
                        this.phase = Phase.DIG;
                    }
                }
                case DIG -> {
                    if (this.loaded(16) || endOfDay) {
                        this.phase = Phase.UP;
                        return;
                    }
                    if (!this.walkTo(spot, 1.3, 0.6)) {
                        return;
                    }
                    this.citizen.setWorking(true);
                    BlockPos wall = spot.relative(this.facingWall(mine));
                    this.lookAt(wall.above());
                    if (++this.dig % 14 == 0) {
                        BlockPos face = wall.above(this.citizen.getRandom().nextInt(2));
                        BlockState state = this.level().getBlockState(face);
                        this.strike(face, state.isAir() ? Blocks.STONE.defaultBlockState() : state);
                    }
                    if (this.dig >= 110) {
                        this.dig = 0;
                        this.carry(ore(this.citizen.getRandom()));
                    }
                }
                case UP -> {
                    this.citizen.setWorking(false);
                    if (this.citizen.position().distanceToSqr(Vec3.atBottomCenterOf(bottom)) > 1.5 * 1.5
                            && this.citizen.getY() < top.getY() - 2) {
                        this.walkTo(bottom, 0.9, 0.6);
                        return;
                    }
                    if (this.climb(bottom, top, 0.18)) {
                        this.citizen.teleportTo(mine.world(new BlockPos(0, 1, 2)).getX() + 0.5, top.getY(),
                                mine.world(new BlockPos(0, 1, 2)).getZ() + 0.5);
                        this.phase = Phase.DELIVER;
                    }
                }
                case DELIVER -> {
                    if (this.deliver()) {
                        this.phase = endOfDay ? Phase.DELIVER : Phase.TO_SHAFT;
                    }
                }
            }
        }

        /** Die Leiter hinauf oder hinab: der Bürger hängt an der Leiter und bewegt sich Stück für Stück. */
        private boolean climb(BlockPos from, BlockPos to, double step) {
            this.citizen.getNavigation().stop();
            this.citizen.setNoGravity(true);
            double x = to.getX() + 0.5;
            double z = to.getZ() + 0.35;
            double y = this.citizen.getY() + step;
            boolean done = step < 0 ? y <= to.getY() : y >= to.getY();
            if (done) {
                y = to.getY();
                this.citizen.setNoGravity(false);
            }
            this.citizen.setPos(x, y, z);
            this.citizen.setDeltaMovement(Vec3.ZERO);
            this.citizen.setYRot(0.0f);
            this.citizen.setYHeadRot(0.0f);
            if (this.citizen.tickCount % 6 == 0) {
                this.level().playSound(null, this.citizen.blockPosition(), SoundEvents.LADDER_STEP, SoundSource.NEUTRAL, 0.4f, 1.0f);
            }
            return done;
        }

        private Direction facingWall(Town.Building mine) {
            return mine.rotation().rotate(Direction.NORTH);
        }

        /** Was der Bergmann aus der Erzwand schlägt. */
        static ItemStack ore(RandomSource random) {
            int r = random.nextInt(1000);
            if (r < 430) {
                return new ItemStack(Items.COBBLESTONE, 2 + random.nextInt(3));
            } else if (r < 680) {
                return new ItemStack(Items.COAL, 1 + random.nextInt(2));
            } else if (r < 800) {
                return new ItemStack(Items.RAW_IRON);
            } else if (r < 880) {
                return new ItemStack(Items.RAW_COPPER, 2);
            } else if (r < 930) {
                return new ItemStack(ModItems.CARBON.get());
            } else if (r < 960) {
                return new ItemStack(Items.RAW_GOLD);
            } else if (r < 975) {
                return new ItemStack(Items.LAPIS_LAZULI, 2 + random.nextInt(3));
            } else if (r < 990) {
                return new ItemStack(Items.REDSTONE, 2 + random.nextInt(3));
            } else if (r < 996) {
                return new ItemStack(ModItems.GEMSTONE.get());
            }
            return new ItemStack(Items.DIAMOND);
        }
    }

    // =====================================================================================
    // Händler, Schmied, Wirt, Kartograf
    // =====================================================================================

    /** Händler stehen an ihrem Platz in der Werkstatt; der Schmied hämmert am Amboss, der Wirt geht zwischen Theke und Tischen. */
    static final class ShopJob extends Job {
        private int work;

        ShopJob(CitizenEntity citizen) {
            super(citizen);
        }

        private @Nullable BlockPos spot() {
            Town.Building shop = this.workplace();
            Town town = this.town();
            if (shop == null || town == null) {
                return null;
            }
            Blueprint plan = shop.type().blueprint();
            // der wievielte Bürger in dieser Werkstatt bin ich?
            int me = 0;
            int i = 0;
            for (Town.Resident r : town.residents()) {
                if (r.work().isPresent() && r.work().get().equals(shop.origin())) {
                    if (r.entity().equals(this.citizen.getUUID())) {
                        me = i;
                    }
                    i++;
                }
            }
            if (me > 0 && me - 1 < plan.marks().size()) {
                return shop.world(plan.marks().get(me - 1));
            }
            return plan.workSpot() != null ? shop.world(plan.workSpot()) : shop.origin();
        }

        @Override
        boolean canStart() {
            return this.spot() != null;
        }

        @Override
        boolean running() {
            return this.spot() != null;
        }

        @Override
        void tick() {
            BlockPos spot = this.spot();
            if (spot == null || !this.walkTo(spot, 1.3, 0.6)) {
                this.citizen.setWorking(false);
                return;
            }
            Town.Building shop = this.workplace();
            this.work++;
            if (this.profession() == Profession.SMITH && shop != null) {
                // am Amboss hämmern
                BlockPos anvil = shop.world(new BlockPos(0, 1, -2));
                this.citizen.setWorking(true);
                this.lookAt(anvil);
                if (this.work % 20 == 0) {
                    this.citizen.swingArm();
                    this.level().playSound(null, anvil, SoundEvents.ANVIL_USE, SoundSource.BLOCKS, 0.35f, 1.1f + this.citizen.getRandom().nextFloat() * 0.2f);
                    this.level().sendParticles(ParticleTypes.LAVA, anvil.getX() + 0.5, anvil.getY() + 1.0, anvil.getZ() + 0.5, 2, 0.1, 0.05, 0.1, 0.0);
                }
            } else {
                this.citizen.setWorking(false);
                if (this.work % 120 == 0) {
                    this.citizen.getLookControl().setLookAt(this.citizen.getX() + this.citizen.getRandom().nextGaussian() * 4,
                            this.citizen.getEyeY(), this.citizen.getZ() + this.citizen.getRandom().nextGaussian() * 4);
                }
            }
        }
    }

    /** Hilfe für Tests: das Erzeugnis einer Mine. */
    static ItemStack mineOre(RandomSource random) {
        return MinerJob.ore(random);
    }

    static boolean isWorkplace(BuildingType type) {
        return type == BuildingType.FIELD || type == BuildingType.LUMBER_HUT || type == BuildingType.MINE;
    }
}
