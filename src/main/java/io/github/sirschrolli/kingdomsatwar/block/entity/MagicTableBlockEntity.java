package io.github.sirschrolli.kingdomsatwar.block.entity;

import io.github.sirschrolli.kingdomsatwar.magic.AetheritStaffItem;
import io.github.sirschrolli.kingdomsatwar.magic.Inscription;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Containers;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jspecify.annotations.Nullable;

/**
 * Der Magietisch hält Stab, Zaubertafel und Aetherit (sie bleiben liegen, auch wenn das Fenster geschlossen wird – der
 * Stab schwebt dann über dem Tisch). Auf dem Bildschirm laufen hier die Animationen: kreisende Runen, die sich
 * schneller drehen, wenn jemand vor dem Tisch steht, und das Ritual beim Einschreiben.
 */
public class MagicTableBlockEntity extends BlockEntity implements Container {
    public static final int STAFF = 0;
    public static final int TABLET = 1;
    public static final int CATALYST = 2;
    /** Dauer des Einschreibe-Rituals (Ticks). */
    public static final int RITUAL_TICKS = 46;
    /** Ereignis-Nummer: Ritual beginnt (Wert = Zauber). */
    public static final int EVENT_RITUAL = 1;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);

    // Animation (nur auf dem Bildschirm)
    public int time;
    public float spin;
    public float spinO;
    /** 0..1: wie nah ein Spieler ist (Runen kreisen schneller und leuchten heller). */
    public float presence;
    public float presenceO;
    public int ritual;
    public Spell ritualSpell = Spell.AETHER_BOLT;

    public MagicTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.MAGIC_TABLE.get(), pos, state);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MagicTableBlockEntity table) {
        table.time++;
        table.presenceO = table.presence;
        table.spinO = table.spin;
        Player near = level.getNearestPlayer(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 4.0, false);
        float target = near != null ? 1.0f : 0.0f;
        table.presence += (target - table.presence) * 0.08f;
        float speed = 0.012f + 0.03f * table.presence + (table.ritual > 0 ? 0.16f * table.ritual / RITUAL_TICKS : 0.0f);
        table.spin += speed;
        if (table.spin > Mth.TWO_PI) {
            table.spin -= Mth.TWO_PI;
            table.spinO -= Mth.TWO_PI;
        }
        if (table.ritual > 0) {
            table.ritual--;
        }
    }

    @Override
    public boolean triggerEvent(int id, int param) {
        if (id == EVENT_RITUAL) {
            this.ritual = RITUAL_TICKS;
            this.ritualSpell = Spell.values()[Mth.clamp(param, 0, Spell.values().length - 1)];
            return true;
        }
        return super.triggerEvent(id, param);
    }

    public ItemStack staff() {
        return this.items.get(STAFF);
    }

    public ItemStack tablet() {
        return this.items.get(TABLET);
    }

    public ItemStack catalyst() {
        return this.items.get(CATALYST);
    }

    // =====================================================================================
    // Container
    // =====================================================================================

    @Override
    public int getContainerSize() {
        return this.items.size();
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int count) {
        ItemStack removed = ContainerHelper.removeItem(this.items, slot, count);
        if (!removed.isEmpty()) {
            this.setChanged();
        }
        return removed;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        stack.limitSize(this.getMaxStackSize(stack));
        this.setChanged();
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return switch (slot) {
            case STAFF -> stack.getItem() instanceof AetheritStaffItem;
            case TABLET -> Inscription.taughtBy(stack) != null;
            case CATALYST -> Inscription.isCatalyst(stack);
            default -> false;
        };
    }

    @Override
    public boolean stillValid(Player player) {
        return Container.stillValidBlockEntity(this, player);
    }

    @Override
    public void clearContent() {
        this.items.clear();
    }

    /** Jede Änderung geht sofort an die Spieler in der Nähe (der Stab schwebt sichtbar über dem Tisch). */
    @Override
    public void setChanged() {
        super.setChanged();
        if (this.level != null && !this.level.isClientSide()) {
            this.level.sendBlockUpdated(this.worldPosition, this.getBlockState(), this.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        if (this.level != null) {
            Containers.dropContents(this.level, pos, this.items);
        }
    }

    // =====================================================================================
    // Speichern und Abgleich
    // =====================================================================================

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items.clear();
        ContainerHelper.loadAllItems(input, this.items);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        ContainerHelper.saveAllItems(output, this.items, true);
    }

    @Override
    public @Nullable ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        try (ProblemReporter.ScopedCollector reporter = new ProblemReporter.ScopedCollector(this.problemPath(), org.slf4j.LoggerFactory.getLogger(
                MagicTableBlockEntity.class))) {
            TagValueOutput output = TagValueOutput.createWithContext(reporter, registries);
            ContainerHelper.saveAllItems(output, this.items, true);
            return output.buildResult();
        }
    }
}
