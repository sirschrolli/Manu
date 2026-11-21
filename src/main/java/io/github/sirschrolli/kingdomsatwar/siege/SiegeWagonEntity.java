package io.github.sirschrolli.kingdomsatwar.siege;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.HasCustomInventoryScreen;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Der Planwagen: ein Kastenwagen unter einer Leinenplane. Auf dem Kutschbock sitzt einer, hinten im Wagen vier – und
 * dazwischen ist Platz für sechs Reihen Ladung (so viel wie eine große Truhe): Baustoffe, Proviant, Munition.
 *
 * <ul>
 *   <li>Selbst fährt er nicht: Ein Pferd zieht ihn an der Leine (Leine an den Wagen, dann schleichend auf das Pferd
 *   klicken – oder das Pferd an der Leine führen und schleichend auf den Wagen klicken). Wer auf dem Pferd reitet,
 *   fährt den Wagen; leer rollt er leicht, das Pferd wird nicht langsamer.</li>
 *   <li>Laderaum: schleichend mit leerer Hand – oder im Wagen sitzend mit der Inventartaste.</li>
 *   <li>Zerlegen: schleichend mit einer Axt (die Ladung fällt dabei heraus).</li>
 * </ul>
 */
public class SiegeWagonEntity extends SiegeVehicleEntity implements HasCustomInventoryScreen {
    private static final EntityDataAccessor<Integer> DATA_CARGO = SynchedEntityData.defineId(SiegeWagonEntity.class, EntityDataSerializers.INT);
    /** Reihen des Laderaums (je neun Fächer). */
    public static final int ROWS = 6;
    /** Plätze hinten im Wagen (auf dem Wagenboden, zwischen der Ladung). */
    private static final Vec3[] BACK_SEATS = {new Vec3(0.42, 0.95, 0.45), new Vec3(-0.42, 0.95, 0.45), new Vec3(0.42, 0.95, -0.55),
            new Vec3(-0.42, 0.95, -0.55)};

    private final SimpleContainer cargo = new SimpleContainer(ROWS * 9) {
        @Override
        public void setChanged() {
            super.setChanged();
            SiegeWagonEntity.this.cargoChanged();
        }

        @Override
        public boolean stillValid(Player player) {
            SiegeWagonEntity wagon = SiegeWagonEntity.this;
            return wagon.isAlive() && (wagon.hasPassenger(player) || player.distanceToSqr(wagon) < 8.0 * 8.0);
        }

        @Override
        public void startOpen(ContainerUser user) {
            SiegeWagonEntity wagon = SiegeWagonEntity.this;
            wagon.level().playSound(null, wagon.getX(), wagon.getY() + 1.0, wagon.getZ(), SoundEvents.BARREL_OPEN, SoundSource.NEUTRAL, 0.8f, 0.85f);
        }

        @Override
        public void stopOpen(ContainerUser user) {
            SiegeWagonEntity wagon = SiegeWagonEntity.this;
            wagon.level().playSound(null, wagon.getX(), wagon.getY() + 1.0, wagon.getZ(), SoundEvents.BARREL_CLOSE, SoundSource.NEUTRAL, 0.8f, 0.85f);
        }
    };

    public SiegeWagonEntity(EntityType<? extends SiegeWeaponEntity> type, Level level) {
        super(type, level, SiegeType.WAGON);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
        builder.define(DATA_CARGO, 0);
    }

    @Override
    protected Vec3 seat(int index) {
        return index <= 0 ? this.siege().seat() : BACK_SEATS[Math.min(index, BACK_SEATS.length) - 1];
    }

    // =====================================================================================
    // Laderaum
    // =====================================================================================

    /** Der Laderaum (sechs Reihen). */
    public Container cargo() {
        return this.cargo;
    }

    /** Wie voll der Wagen ist: 0 leer, 1 bis ein Drittel, 2 bis zwei Drittel, 3 voll (auf dem Bildschirm: die Ladung). */
    public int cargoLevel() {
        return this.entityData.get(DATA_CARGO);
    }

    /** Wie viele Fächer belegt sind. */
    public int usedSlots() {
        int used = 0;
        for (ItemStack stack : this.cargo.getItems()) {
            if (!stack.isEmpty()) {
                used++;
            }
        }
        return used;
    }

    private void cargoChanged() {
        if (this.level().isClientSide()) {
            return;
        }
        int used = this.usedSlots();
        int size = this.cargo.getContainerSize();
        int level = used == 0 ? 0 : used * 3 <= size ? 1 : used * 3 <= size * 2 ? 2 : 3;
        if (level != this.cargoLevel()) {
            this.entityData.set(DATA_CARGO, level);
        }
    }

    /** Öffnet den Laderaum. */
    public void openCargo(Player player) {
        if (this.level().isClientSide()) {
            return;
        }
        player.openMenu(new SimpleMenuProvider((id, inventory, p) -> ChestMenu.sixRows(id, inventory, this.cargo), this.getDisplayName()));
        this.gameEvent(GameEvent.CONTAINER_OPEN, player);
    }

    /** Wer im Wagen sitzt, öffnet mit der Inventartaste den Laderaum (darunter sein eigenes Inventar). */
    @Override
    public void openCustomInventoryScreen(Player player) {
        this.openCargo(player);
    }

    /** Schleichend: mit leerer Hand in den Laderaum, mit einer Axt zerlegen. */
    @Override
    protected @Nullable InteractionResult secondaryUse(Player player, ItemStack stack) {
        if (stack.isEmpty()) {
            this.openCargo(player);
            return InteractionResult.SUCCESS;
        }
        if (stack.is(ItemTags.AXES)) {
            return super.secondaryUse(player, ItemStack.EMPTY);
        }
        return null;
    }

    @Override
    protected void dropContents(ServerLevel level) {
        Containers.dropContents(level, this, this.cargo);
    }

    // =====================================================================================
    // Anzeige, Speichern
    // =====================================================================================

    @Override
    protected Component status() {
        Component name = this.getDisplayName().copy().withStyle(ChatFormatting.GOLD);
        return Component.translatable("siege.kingdomsatwar.status.wagon", name, this.usedSlots(), this.cargo.getContainerSize(),
                Math.max(0, this.getPassengers().size() - 1), this.healthLine());
    }

    @Override
    protected Component riderStatus() {
        return Component.translatable("siege.kingdomsatwar.controls.wagon.rider").withStyle(ChatFormatting.GRAY);
    }

    @Override
    protected void addAdditionalSaveData(ValueOutput output) {
        super.addAdditionalSaveData(output);
        ContainerHelper.saveAllItems(output.child("Cargo"), this.cargo.getItems());
    }

    @Override
    protected void readAdditionalSaveData(ValueInput input) {
        super.readAdditionalSaveData(input);
        this.cargo.clearContent();
        input.child("Cargo").ifPresent(cargo -> ContainerHelper.loadAllItems(cargo, this.cargo.getItems()));
        this.cargoChanged();
    }
}
