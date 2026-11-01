package io.github.sirschrolli.kingdomsatwar.economy.citizen;

import io.github.sirschrolli.kingdomsatwar.economy.town.Blueprint;
import io.github.sirschrolli.kingdomsatwar.economy.town.BuildingType;
import io.github.sirschrolli.kingdomsatwar.economy.town.Profession;
import io.github.sirschrolli.kingdomsatwar.economy.town.Town;
import io.github.sirschrolli.kingdomsatwar.economy.town.TownManager;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
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
     *
     * <p>Er arbeitet immer aus der Nähe: von einem Stehplatz höchstens {@link #REACH} Blöcke neben dem Block – auf dem
     * Boden, auf schon gebauten Böden oder Dachstufen –, aber nie dort, wo der Plan später noch etwas hinsetzt (er mauert
     * sich also nicht ein). Reicht kein Stehplatz bis an eine hohe Mauer heran, stellt er sich ein Baugerüst auf: er stapelt
     * es unter sich hoch, steigt darin hinauf und hinunter und stockt es Lage für Lage mit der Mauer auf. Die Gerüste
     * bleiben stehen, bis der Bau fertig ist; dann baut er sie von oben nach unten ab.
     */
    static final class BuilderJob extends Job {
        /** So weit reicht der Baumeister waagrecht von der Mitte seines Stehplatzes bis zur Mitte des Blocks. */
        static final double REACH = 4.0;
        /** So hoch über seinen Füßen bzw. so tief darunter setzt er Blöcke. */
        static final int UP = 4;
        static final int DOWN = 3;
        /** Höchstens so viele Lagen hat ein Gerüst. */
        static final int MAX_SCAFFOLD = 28;
        /** Findet er so lange (Ticks) keinen Platz für einen Block, setzt er ihn von dort, wo er steht. */
        static final int STALL = 400;
        /** Steiggeschwindigkeit im Gerüst (Blöcke je Tick). */
        private static final double CLIMB_SPEED = 0.3;

        private int cooldown;
        private int stalled;
        private int lastProgress = -1;
        /** Wohin er gerade geht: zu einem Stehplatz, zum Einstieg eines Gerüsts oder an den Fuß eines neuen Gerüsts. */
        private @Nullable Place place;
        private @Nullable BlockPos goingTo;
        private int travel;
        private int travelLimit;
        /** Klettern im Gerüst: von wo nach wo (y), in welcher Säule, und wo er unten aussteigt. */
        private boolean climbing;
        private double climbY;
        private double climbTo;
        private BlockPos climbColumn = BlockPos.ZERO;
        private @Nullable BlockPos climbExit;
        /** Gerüste, die nach dem Bau abgebaut werden (von oben nach unten). */
        private @Nullable ArrayDeque<BlockPos> dismantle;
        /** Zähler für die Fehlersuche (nur mit -Dkingdomsatwar.debugBuilder=true ausgegeben). */
        private int statTeleports;
        private int statClimbs;
        private int statRaises;
        private int statForced;
        private static final boolean DEBUG = Boolean.getBoolean("kingdomsatwar.debugBuilder");
        /** Für welche Baustelle die Karten gelten. */
        private Town.@Nullable Project mapped;
        /** Letzter Bauschritt, der an einer Stelle einen Block setzt bzw. dort etwas wegräumt. */
        private final Map<BlockPos, Integer> lastSolid = new HashMap<>();
        private final Map<BlockPos, Integer> lastClear = new HashMap<>();

        /** Ein Ziel: Stehplatz (Füße), dazu ggf. die Gerüstsäule, auf der er steht bzw. die er aufstellt. */
        private record Place(BlockPos feet, Kind kind, @Nullable BlockPos column) {}

        private enum Kind {
            /** Hingehen und dort stehen. */
            GROUND,
            /** Auf ein schon stehendes Gerüst steigen ({@code column} = unterste Lage). */
            SCAFFOLD,
            /** An den Fuß eines neuen Gerüsts gehen und es unter sich hochstapeln. */
            NEW_SCAFFOLD
        }

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

        // ------------------------------------------------------------------ Ablauf

        @Override
        void tick() {
            Town town = this.town();
            Town.Project project = this.project();
            if (town == null || project == null) {
                return;
            }
            if (this.climbing) {
                this.climb();
                return;
            }
            this.map(project);
            List<Blueprint.Step> steps = project.steps();
            int skipped = 0;
            while (project.progress < steps.size() && skipped < 64 && !this.needsWork(steps.get(project.progress))) {
                project.progress++;
                skipped++;
            }
            if (project.progress >= steps.size()) {
                this.finish(town, project);
                return;
            }
            if (project.progress != this.lastProgress) {
                this.lastProgress = project.progress;
                this.stalled = 0;
            }
            Blueprint.Step step = steps.get(project.progress);
            BlockPos at = step.pos();
            if (this.cooldown > 0) {
                this.cooldown--;
                this.lookAt(at);
                return;
            }
            if (++this.stalled > STALL) {
                // nirgends ein Platz: dann eben von hier aus (der Bau bleibt nie stecken)
                this.statForced++;
                if (DEBUG) {
                    io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info("[Baumeister] kein Platz für {} bei {} (steht bei {})",
                            step.state(), at, this.feet());
                }
                this.build(project, step);
                return;
            }
            BlockPos feet = this.feet();
            boolean onScaffold = this.onScaffold(feet);
            // auf dem Gerüst neben dem Block: aufstocken, bis der Block etwa in Brusthöhe liegt
            if (onScaffold && horizontal(feet, at) <= REACH && at.getY() - feet.getY() > 2 && this.canRaise(feet, project.progress)) {
                this.raise(feet);
                return;
            }
            if (this.canWork(feet, at) && this.safe(feet, project.progress)) {
                this.place = null;
                this.goingTo = null;
                this.build(project, step);
                return;
            }
            this.citizen.setWorking(false);
            if (this.place == null || !this.canWork(this.place.feet(), at) || !this.stillValid(this.place, project.progress)) {
                this.place = this.choose(project, at, feet);
                this.goingTo = null;
            }
            Place place = this.place;
            if (place == null) {
                return; // nichts gefunden: gleich noch einmal versuchen (und nach einer Weile von hier aus bauen)
            }
            // erst vom eigenen Gerüst herunter, wenn es woanders weitergeht
            if (onScaffold && (place.kind() != Kind.SCAFFOLD || !sameColumn(place.feet(), feet))) {
                this.startClimbDown(feet, project.progress);
                return;
            }
            switch (place.kind()) {
                case GROUND -> {
                    if (!this.arrived(place.feet())) {
                        this.go(place.feet());
                    }
                }
                case SCAFFOLD -> {
                    BlockPos column = place.column();
                    BlockPos entry = column == null ? null : this.exitOf(column, project.progress);
                    if (entry == null) {
                        this.teleport(place.feet());
                    } else if (!this.arrived(entry)) {
                        this.go(entry);
                    } else {
                        this.startClimbUp(column, place.feet());
                    }
                }
                case NEW_SCAFFOLD -> {
                    if (!this.arrived(place.feet())) {
                        this.go(place.feet());
                    } else if (this.canRaise(place.feet(), project.progress)) {
                        this.raise(place.feet());
                        this.place = null;
                    }
                }
            }
        }

        @Override
        void stop() {
            super.stop();
            this.citizen.setWorking(false);
            Town.Project project = this.project();
            int progress = project == null ? Integer.MAX_VALUE : project.progress;
            if (this.climbing) {
                this.endClimb();
            }
            BlockPos feet = this.feet();
            if (this.onScaffold(feet)) {
                // Feierabend: aus dem Gerüst herunter
                BlockPos exit = this.exitOf(this.columnBase(feet), progress);
                if (exit != null) {
                    this.teleport(exit);
                }
            } else if (project != null && project.bounds().isInside(feet) && feet.getY() > project.origin.getY() + 1) {
                // oben im Rohbau (ohne Treppe): hinunter vor die Tür
                this.teleport(this.entrance(project));
            }
            this.place = null;
            this.goingTo = null;
        }

        // ------------------------------------------------------------------ Bauen

        private void build(Town.Project project, Blueprint.Step step) {
            BlockPos at = step.pos();
            this.citizen.getNavigation().stop();
            this.citizen.setWorking(true);
            this.lookAt(at);
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
            this.stalled = 0;
            // Wegräumen geht schneller als Bauen
            this.cooldown = step.state().isAir() ? 0 : 2;
        }

        /** Alles gesetzt: erst vom Gerüst herunter, dann die Gerüste abbauen, dann ist das Gebäude fertig. */
        private void finish(Town town, Town.Project project) {
            BlockPos feet = this.feet();
            if (this.onScaffold(feet)) {
                this.startClimbDown(feet, Integer.MAX_VALUE);
                return;
            }
            if (this.dismantle == null) {
                this.dismantle = scaffolds(this.level(), project.bounds());
            }
            if (!this.dismantle.isEmpty()) {
                this.citizen.setWorking(true);
                for (int i = 0; i < 2 && !this.dismantle.isEmpty(); i++) {
                    BlockPos pos = this.dismantle.poll();
                    if (this.level().getBlockState(pos).is(ModBlocks.BUILDER_SCAFFOLD.get())) {
                        this.lookAt(pos);
                        this.level().destroyBlock(pos, false);
                    }
                }
                this.citizen.swingArm();
                return;
            }
            this.dismantle = null;
            this.citizen.setWorking(false);
            BlockPos door = this.entrance(project);
            boolean inside = project.bounds().isInside(this.feet());
            if (DEBUG) {
                io.github.sirschrolli.kingdomsatwar.KingdomsAtWar.LOGGER.info(
                        "[Baumeister] {} fertig: {} Sprünge, {} Kletterpartien, {} Gerüstlagen, {} Blöcke ohne Platz",
                        project.type.id(), this.statTeleports, this.statClimbs, this.statRaises, this.statForced);
            }
            this.statTeleports = this.statClimbs = this.statRaises = this.statForced = 0;
            TownManager.completeProject(this.level(), town, project);
            if (inside) {
                // aus dem fertigen Haus hinaus (oder vom Dach herunter)
                net.minecraft.world.level.pathfinder.Path path = this.citizen.getNavigation().createPath(door, 0);
                if (path != null && path.canReach()) {
                    this.citizen.getNavigation().moveTo(path, 0.8);
                } else {
                    this.teleport(door);
                }
            }
            this.mapped = null;
            this.place = null;
        }

        private BlockPos entrance(Town.Project project) {
            BlockPos door = Blueprint.world(project.origin, project.rotation, project.type.blueprint().entrance());
            BlockPos spot = standingSpot(this.level(), door);
            return spot != null ? spot : door;
        }

        // ------------------------------------------------------------------ Karten der Baustelle

        /** Merkt sich, wo der Plan noch Blöcke setzt bzw. wegräumt; nach dem Laden wird der Fortschritt nachgeprüft. */
        private void map(Town.Project project) {
            if (this.mapped == project) {
                return;
            }
            this.mapped = project;
            this.lastSolid.clear();
            this.lastClear.clear();
            this.dismantle = null;
            List<Blueprint.Step> steps = project.steps();
            for (int i = 0; i < steps.size(); i++) {
                Blueprint.Step s = steps.get(i);
                if (s.foundation()) {
                    for (int k = 0; k < 12; k++) {
                        this.lastSolid.put(s.pos().below(k), i);
                    }
                } else if (s.state().isAir()) {
                    this.lastClear.put(s.pos(), i);
                } else if (traps(s.state())) {
                    this.lastSolid.put(s.pos(), i);
                }
            }
            // ab dem ersten Schritt, der noch Arbeit braucht (z. B. nach einem Neustart oder einem geänderten Ablauf)
            int first = 0;
            while (first < project.progress && !this.needsWork(steps.get(first))) {
                first++;
            }
            project.progress = first;
        }

        /**
         * Würde dieser Block ihn einschließen, wenn er ihn an seinen Füßen oder seinem Kopf setzt? Pflanzen, Teppiche,
         * Fackeln, Wasser und Ähnliches nicht – dort darf er stehen, auch wenn der Plan da später noch etwas hinsetzt.
         */
        private static boolean traps(BlockState state) {
            var shape = state.getCollisionShape(net.minecraft.world.level.EmptyBlockGetter.INSTANCE, BlockPos.ZERO);
            return !shape.isEmpty() && shape.max(net.minecraft.core.Direction.Axis.Y) > 0.25;
        }

        /** Wie {@link Blueprint#needsWork}, nur gilt das eigene Gerüst nicht als „im Weg“. */
        private boolean needsWork(Blueprint.Step step) {
            if (!step.foundation() && step.state().isAir() && this.level().getBlockState(step.pos()).is(ModBlocks.BUILDER_SCAFFOLD.get())) {
                return false;
            }
            return Blueprint.needsWork(this.level(), step);
        }

        /** Setzt der Plan (ab Schritt {@code progress}) an dieser Stelle noch etwas hin? */
        private boolean filledLater(BlockPos pos, int progress) {
            return this.lastSolid.getOrDefault(pos, -1) >= progress;
        }

        /** Hier darf er stehen: Füße und Kopf bleiben frei, und der Boden unter ihm wird nicht mehr weggeräumt. */
        private boolean safe(BlockPos feet, int progress) {
            return !this.filledLater(feet, progress) && !this.filledLater(feet.above(), progress)
                    && this.lastClear.getOrDefault(feet.below(), -1) < progress;
        }

        // ------------------------------------------------------------------ Stehplätze

        private static double horizontal(BlockPos feet, BlockPos at) {
            double dx = at.getX() - feet.getX();
            double dz = at.getZ() - feet.getZ();
            return Math.sqrt(dx * dx + dz * dz);
        }

        /** Von diesem Stehplatz (Füße) aus erreicht er den Block. */
        private boolean canWork(BlockPos feet, BlockPos at) {
            int dy = at.getY() - feet.getY();
            return horizontal(feet, at) <= REACH && dy <= UP && dy >= -DOWN;
        }

        /** Füße des Baumeisters: am Ziel der Stehplatz selbst (er steht dort evtl. auf einer Stufe oder Platte). */
        private BlockPos feet() {
            Place place = this.place;
            if (place != null && place.kind() == Kind.GROUND && this.arrived(place.feet())) {
                return place.feet();
            }
            return BlockPos.containing(this.citizen.getX(), this.citizen.getY() + 0.2, this.citizen.getZ());
        }

        private boolean arrived(BlockPos feet) {
            double dx = this.citizen.getX() - (feet.getX() + 0.5);
            double dz = this.citizen.getZ() - (feet.getZ() + 0.5);
            double dy = this.citizen.getY() - feet.getY();
            return dx * dx + dz * dz <= 0.8 * 0.8 && dy > -1.0 && dy < 1.0;
        }

        /** Luft (oder Gras, Blumen …) ohne Flüssigkeit, durch die man hindurch kann. */
        private boolean open(BlockPos pos) {
            BlockState state = this.level().getBlockState(pos);
            return state.getCollisionShape(this.level(), pos).isEmpty() && state.getFluidState().isEmpty()
                    && !state.is(BlockTags.FIRE) && !state.is(Blocks.SWEET_BERRY_BUSH) && !state.is(Blocks.POWDER_SNOW);
        }

        /** Darauf kann er stehen: fester Untergrund (mindestens eine halbe Stufe hoch, kein Zaun), darüber zwei Blöcke frei. */
        private boolean standable(BlockPos feet) {
            BlockPos down = feet.below();
            BlockState ground = this.level().getBlockState(down);
            var shape = ground.getCollisionShape(this.level(), down);
            if (shape.isEmpty() || shape.max(net.minecraft.core.Direction.Axis.Y) < 0.5 || shape.max(net.minecraft.core.Direction.Axis.Y) > 1.0
                    || ground.is(Blocks.MAGMA_BLOCK) || ground.is(Blocks.CACTUS) || ground.getBlock() instanceof net.minecraft.world.level.block.CampfireBlock) {
                return false;
            }
            return this.open(feet) && this.open(feet.above());
        }

        private boolean onScaffold(BlockPos feet) {
            return this.level().getBlockState(feet.below()).is(ModBlocks.BUILDER_SCAFFOLD.get());
        }

        private static boolean sameColumn(BlockPos a, BlockPos b) {
            return a.getX() == b.getX() && a.getZ() == b.getZ();
        }

        /** Unterste Lage des Gerüsts unter diesen Füßen. */
        private BlockPos columnBase(BlockPos feet) {
            BlockPos.MutableBlockPos pos = feet.below().mutable();
            while (this.level().getBlockState(pos.below()).is(ModBlocks.BUILDER_SCAFFOLD.get()) && pos.getY() > this.level().getMinY()) {
                pos.move(net.minecraft.core.Direction.DOWN);
            }
            return pos.immutable();
        }

        /** Ein freier Platz neben dem Fuß eines Gerüsts (zum Ein- und Aussteigen). */
        private @Nullable BlockPos exitOf(BlockPos base, int progress) {
            BlockPos best = null;
            double bestDist = Double.MAX_VALUE;
            for (net.minecraft.core.Direction dir : net.minecraft.core.Direction.Plane.HORIZONTAL) {
                for (int dy = 0; dy >= -1; dy--) {
                    BlockPos p = base.relative(dir).above(dy);
                    if (this.standable(p) && this.safe(p, progress)) {
                        double d = this.citizen.distanceToSqr(Vec3.atBottomCenterOf(p));
                        if (d < bestDist) {
                            bestDist = d;
                            best = p;
                        }
                        break;
                    }
                }
            }
            return best;
        }

        /** Gilt das Ziel noch? (Inzwischen kann dort etwas gebaut oder ein Gerüst schon aufgestellt sein.) */
        private boolean stillValid(Place place, int progress) {
            return switch (place.kind()) {
                case GROUND, SCAFFOLD -> this.standable(place.feet()) && this.safe(place.feet(), progress);
                case NEW_SCAFFOLD -> this.standable(place.feet()) && this.safe(place.feet(), progress)
                        || this.onScaffold(this.feet());
            };
        }

        /**
         * Sucht den besten Platz für den nächsten Block: einen Stehplatz in Reichweite (gern in Schulterhöhe des Blocks und
         * nicht weit vom jetzigen Platz) oder – wenn keiner hinkommt – den Fuß eines neuen Gerüsts neben dem Block.
         */
        private @Nullable Place choose(Town.Project project, BlockPos at, BlockPos feet) {
            int progress = project.progress;
            var box = project.bounds();
            List<BlockPos> upcoming = this.upcoming(project);
            Place best = null;
            double bestScore = Double.MAX_VALUE;
            int r = (int) Math.ceil(REACH);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double h = Math.sqrt(dx * dx + dz * dz);
                    if (h > REACH) {
                        continue;
                    }
                    for (int y = at.getY() - UP; y <= at.getY() + DOWN; y++) {
                        BlockPos p = new BlockPos(at.getX() + dx, y, at.getZ() + dz);
                        if (!this.standable(p) || !this.safe(p, progress)) {
                            continue;
                        }
                        boolean scaffold = this.onScaffold(p);
                        int rel = at.getY() - y;
                        int raise = 0;
                        if (scaffold && rel > 2) {
                            // ein Gerüst lässt sich oben aufstocken
                            while (rel - raise > 2 && raise < 6 && this.canRaise(p.above(raise), progress)) {
                                raise++;
                            }
                        }
                        int eff = rel - raise;
                        double score = (h < 1.0 ? 2.5 : 0.0) + Math.max(0.0, h - 2.5) * 0.8
                                + (eff > 2 ? (eff - 2) * 0.9 : eff < 0 ? -eff * 0.9 : 0.0) + raise * 0.12
                                + Math.sqrt(p.distSqr(feet)) * 0.08 - this.coverage(p.above(raise), upcoming) * 0.25;
                        if (scaffold && !(this.onScaffold(feet) && sameColumn(p, feet))) {
                            score += 0.5; // hinaufsteigen kostet Zeit
                        }
                        if (score < bestScore) {
                            bestScore = score;
                            best = new Place(p, scaffold ? Kind.SCAFFOLD : Kind.GROUND, scaffold ? this.columnBase(p) : null);
                        }
                    }
                }
            }
            // ein Stehplatz weit oben ohne Weg dorthin (Obergeschoss ohne Treppe, Dach): lieber ein Gerüst, wenn es eins gibt
            if (best != null && best.kind() == Kind.GROUND && best.feet().getY() > feet.getY() + 2) {
                net.minecraft.world.level.pathfinder.Path path = this.citizen.getNavigation().createPath(best.feet(), 0);
                if (path == null || !path.canReach()) {
                    bestScore += 2.0;
                }
            }
            Place column = this.newScaffold(project, at, feet, box, upcoming);
            if (column != null && (best == null || this.columnScore < bestScore)) {
                return column;
            }
            return best;
        }

        private double columnScore;

        /** Die nächsten Blöcke, die noch gesetzt werden (für die Wahl eines Platzes, von dem aus er möglichst viele erreicht). */
        private List<BlockPos> upcoming(Town.Project project) {
            List<Blueprint.Step> steps = project.steps();
            List<BlockPos> list = new ArrayList<>();
            for (int i = project.progress; i < steps.size() && list.size() < 12 && i < project.progress + 40; i++) {
                Blueprint.Step s = steps.get(i);
                if (!s.foundation() && !s.state().isAir()) {
                    list.add(s.pos());
                }
            }
            return list;
        }

        /** Wie viele der nächsten Blöcke er von diesem Platz aus erreicht. */
        private int coverage(BlockPos feet, List<BlockPos> upcoming) {
            int n = 0;
            for (BlockPos p : upcoming) {
                if (this.canWork(feet, p)) {
                    n++;
                }
            }
            return n;
        }

        /**
         * Fuß eines neuen Gerüsts neben dem Block: eine Säule, die vom Boden bis zwei Blöcke unter den Block frei ist und frei
         * bleibt (dort setzt der Plan nichts hin), möglichst außerhalb des Hauses und nicht vor der Tür.
         */
        private @Nullable Place newScaffold(Town.Project project, BlockPos at, BlockPos feet, net.minecraft.world.level.levelgen.structure.BoundingBox box,
                List<BlockPos> upcoming) {
            int progress = project.progress;
            BlockPos door = Blueprint.world(project.origin, project.rotation, project.type.blueprint().entrance());
            int top = at.getY() - 2;   // Füße oben auf dem Gerüst
            Place best = null;
            double bestScore = Double.MAX_VALUE;
            int r = (int) Math.floor(REACH - 0.5);
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    double h = Math.sqrt(dx * dx + dz * dz);
                    if (h < 1.0 || h > REACH - 0.5) {
                        continue;
                    }
                    int x = at.getX() + dx;
                    int z = at.getZ() + dz;
                    if (Math.abs(x - door.getX()) <= 1 && Math.abs(z - door.getZ()) <= 1) {
                        continue;
                    }
                    BlockPos head = new BlockPos(x, top + 1, z);
                    if (!this.open(head) || this.filledLater(head, progress) || !this.open(head.above()) || this.filledLater(head.above(), progress)) {
                        continue;
                    }
                    // nach unten bis zum Boden: alles frei?
                    int y = top;
                    while (y > top - MAX_SCAFFOLD && this.open(new BlockPos(x, y, z)) && !this.filledLater(new BlockPos(x, y, z), progress)) {
                        y--;
                    }
                    BlockPos ground = new BlockPos(x, y, z);
                    BlockPos base = ground.above();
                    if (base.getY() >= top || this.open(ground) || !this.standable(base) || !this.safe(base, progress)
                            || this.level().getBlockState(ground).is(ModBlocks.BUILDER_SCAFFOLD.get())) {
                        continue;
                    }
                    double score = 1.2 + Math.max(0.0, h - 2.0) * 0.8 + (top - base.getY()) * 0.15
                            + (box.isInside(x, box.minY(), z) ? 1.5 : 0.0) + Math.sqrt(base.distSqr(feet)) * 0.05
                            - this.coverage(new BlockPos(x, top, z), upcoming) * 0.25;
                    if (score < bestScore) {
                        bestScore = score;
                        best = new Place(base, Kind.NEW_SCAFFOLD, base);
                    }
                }
            }
            this.columnScore = bestScore;
            return best;
        }

        // ------------------------------------------------------------------ Gehen, Gerüst, Klettern

        /** Geht zu einem Platz; findet sich kein Weg (oder dauert es zu lange), steigt er hinüber bzw. hinauf. */
        private void go(BlockPos target) {
            if (!target.equals(this.goingTo)) {
                this.goingTo = target;
                this.travel = 0;
                net.minecraft.world.level.pathfinder.Path path = this.citizen.getNavigation().createPath(target, 0);
                boolean reach = path != null && path.canReach();
                this.travelLimit = reach ? 40 + path.getNodeCount() * 8 : target.getY() > this.citizen.getY() + 1.5 ? 15 : 60;
                if (path != null) {
                    this.citizen.getNavigation().moveTo(path, 0.85);
                }
            }
            if (++this.travel > this.travelLimit) {
                this.teleport(target);
                return;
            }
            if (this.travel % 20 == 0 && this.citizen.getNavigation().isDone()) {
                this.citizen.getNavigation().moveTo(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.85);
            }
            if (this.citizen.distanceToSqr(Vec3.atBottomCenterOf(target)) < 2.5 * 2.5) {
                // das letzte Stück genau auf den Platz
                this.citizen.getMoveControl().setWantedPosition(target.getX() + 0.5, target.getY(), target.getZ() + 0.5, 0.7);
            }
        }

        private void teleport(BlockPos feet) {
            this.statTeleports++;
            boolean up = feet.getY() > this.citizen.getY() + 1.5;
            this.citizen.getNavigation().stop();
            this.citizen.teleportTo(feet.getX() + 0.5, feet.getY(), feet.getZ() + 0.5);
            if (up) {
                this.level().playSound(null, feet, SoundEvents.LADDER_STEP, SoundSource.NEUTRAL, 0.6f, 1.0f);
            }
            this.goingTo = null;
        }

        /** Kann das Gerüst unter ihm um eine Lage wachsen? (Die Stelle selbst und zwei Blöcke darüber bleiben frei.) */
        private boolean canRaise(BlockPos feet, int progress) {
            if (feet.getY() - this.columnBase(feet).getY() >= MAX_SCAFFOLD) {
                return false;
            }
            for (int i = 0; i <= 2; i++) {
                BlockPos p = feet.above(i);
                if (!this.open(p) || this.filledLater(p, progress)) {
                    return false;
                }
            }
            return true;
        }

        /** Springt hoch und setzt unter sich eine Gerüstlage. */
        private void raise(BlockPos feet) {
            this.statRaises++;
            this.citizen.getNavigation().stop();
            this.citizen.teleportTo(feet.getX() + 0.5, feet.getY() + 1, feet.getZ() + 0.5);
            this.level().setBlock(feet, ModBlocks.BUILDER_SCAFFOLD.get().defaultBlockState(), Block.UPDATE_ALL);
            this.citizen.swingArm();
            this.level().playSound(null, feet, SoundEvents.SCAFFOLDING_PLACE, SoundSource.BLOCKS, 0.8f, 0.9f);
            this.cooldown = 4;
        }

        private void startClimbUp(BlockPos base, BlockPos top) {
            this.statClimbs++;
            this.climbing = true;
            this.climbColumn = base;
            this.climbY = base.getY();
            this.climbTo = top.getY();
            this.climbExit = null;
            this.citizen.getNavigation().stop();
            this.citizen.noPhysics = true;
        }

        private void startClimbDown(BlockPos feet, int progress) {
            BlockPos base = this.columnBase(feet);
            this.climbing = true;
            this.climbColumn = base;
            this.climbY = this.citizen.getY();
            this.climbTo = base.getY();
            this.climbExit = this.exitOf(base, progress);
            this.citizen.getNavigation().stop();
            this.citizen.noPhysics = true;
            this.place = null;
        }

        /** Klettert im Inneren des Gerüsts (zwischen den Stangen) hinauf oder hinunter. */
        private void climb() {
            this.climbY = this.climbTo > this.climbY ? Math.min(this.climbTo, this.climbY + CLIMB_SPEED)
                    : Math.max(this.climbTo, this.climbY - CLIMB_SPEED);
            this.citizen.setDeltaMovement(Vec3.ZERO);
            this.citizen.setPos(this.climbColumn.getX() + 0.5, this.climbY, this.climbColumn.getZ() + 0.5);
            if (this.citizen.tickCount % 4 == 0) {
                this.level().playSound(null, this.citizen.blockPosition(), SoundEvents.SCAFFOLDING_STEP, SoundSource.NEUTRAL, 0.4f, 1.0f);
            }
            if (this.climbY == this.climbTo) {
                this.endClimb();
            }
        }

        private void endClimb() {
            this.climbing = false;
            this.citizen.noPhysics = false;
            this.citizen.setDeltaMovement(Vec3.ZERO);
            if (this.climbTo <= this.climbColumn.getY() + 0.01) {
                // unten angekommen: aus dem Gerüst heraustreten
                BlockPos exit = this.climbExit;
                if (exit == null) {
                    exit = standingSpot(this.level(), this.climbColumn);
                }
                if (exit != null) {
                    this.citizen.teleportTo(exit.getX() + 0.5, exit.getY(), exit.getZ() + 0.5);
                }
            } else {
                this.citizen.teleportTo(this.climbColumn.getX() + 0.5, this.climbTo, this.climbColumn.getZ() + 0.5);
            }
            this.goingTo = null;
        }

        /** Alle Baugerüste rund um eine Baustelle, von oben nach unten. */
        static ArrayDeque<BlockPos> scaffolds(ServerLevel level, net.minecraft.world.level.levelgen.structure.BoundingBox bounds) {
            List<BlockPos> found = new ArrayList<>();
            var box = bounds.inflatedBy(5, 0, 5);
            for (int y = box.maxY() + 4; y >= box.minY() - 2; y--) {
                for (int z = box.minZ(); z <= box.maxZ(); z++) {
                    for (int x = box.minX(); x <= box.maxX(); x++) {
                        BlockPos p = new BlockPos(x, y, z);
                        if (level.getBlockState(p).is(ModBlocks.BUILDER_SCAFFOLD.get())) {
                            found.add(p);
                        }
                    }
                }
            }
            return new ArrayDeque<>(found);
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
