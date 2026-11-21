package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.entity.MagicTableBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jspecify.annotations.Nullable;

/**
 * Fenster des Magietischs: in der Mitte des Siegelkreises der Stab, rechts die Zaubertafel und das Aetherit. Knopf 0
 * schreibt ein (siehe {@link Inscription}); der Server prüft alles noch einmal.
 */
public class MagicTableMenu extends AbstractContainerMenu {
    public static final int BUTTON_INSCRIBE = 0;
    // Lage der Fächer im Fenster (siehe MagicTableScreen)
    public static final int STAFF_X = 55;
    public static final int STAFF_Y = 57;
    public static final int TABLET_X = 135;
    public static final int TABLET_Y = 22;
    public static final int CATALYST_X = 135;
    public static final int CATALYST_Y = 45;
    public static final int INVENTORY_X = 40;
    public static final int INVENTORY_Y = 140;

    private static final Identifier EMPTY_STAFF = KingdomsAtWar.id("container/slot/staff");
    private static final Identifier EMPTY_TABLET = KingdomsAtWar.id("container/slot/spell_tablet");
    private static final Identifier EMPTY_AETHERIT = KingdomsAtWar.id("container/slot/aetherit");

    private final Container table;
    private final ContainerLevelAccess access;
    private final BlockPos pos;
    private final Player player;

    /** Auf dem Bildschirm: Inhalt und Lage kommen vom Server. */
    public MagicTableMenu(int id, Inventory inventory, RegistryFriendlyByteBuf buf) {
        this(id, inventory, new SimpleContainer(3), ContainerLevelAccess.NULL, buf.readBlockPos());
    }

    public MagicTableMenu(int id, Inventory inventory, Container table, ContainerLevelAccess access, BlockPos pos) {
        super(ModMenus.MAGIC_TABLE.get(), id);
        checkContainerSize(table, 3);
        this.table = table;
        this.access = access;
        this.pos = pos;
        this.player = inventory.player;
        table.startOpen(inventory.player);
        this.addSlot(new TableSlot(table, MagicTableBlockEntity.STAFF, STAFF_X, STAFF_Y, 1, EMPTY_STAFF));
        this.addSlot(new TableSlot(table, MagicTableBlockEntity.TABLET, TABLET_X, TABLET_Y, 16, EMPTY_TABLET));
        this.addSlot(new TableSlot(table, MagicTableBlockEntity.CATALYST, CATALYST_X, CATALYST_Y, 64, EMPTY_AETHERIT));
        this.addStandardInventorySlots(inventory, INVENTORY_X, INVENTORY_Y);
    }

    /** Ein Fach des Tischs: nimmt nur, was hineingehört. */
    private static final class TableSlot extends Slot {
        private final int max;
        private final Identifier icon;

        TableSlot(Container container, int index, int x, int y, int max, Identifier icon) {
            super(container, index, x, y);
            this.max = max;
            this.icon = icon;
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return this.container.canPlaceItem(this.getContainerSlot(), stack);
        }

        @Override
        public int getMaxStackSize() {
            return this.max;
        }

        @Override
        public @Nullable Identifier getNoItemIcon() {
            return this.icon;
        }
    }

    public BlockPos pos() {
        return this.pos;
    }

    public ItemStack staff() {
        return this.table.getItem(MagicTableBlockEntity.STAFF);
    }

    public ItemStack tablet() {
        return this.table.getItem(MagicTableBlockEntity.TABLET);
    }

    public ItemStack catalyst() {
        return this.table.getItem(MagicTableBlockEntity.CATALYST);
    }

    /** Was ein Klick auf "Einschreiben" jetzt bewirken würde. */
    public Inscription plan() {
        return Inscription.plan(this.staff(), this.tablet(), this.catalyst(), this.player);
    }

    @Override
    public boolean clickMenuButton(Player player, int button) {
        if (button != BUTTON_INSCRIBE) {
            return false;
        }
        Inscription plan = Inscription.plan(this.staff(), this.tablet(), this.catalyst(), player);
        if (player.level().isClientSide()) {
            return plan.ready();
        }
        if (!Inscription.perform(this.staff(), this.tablet(), this.catalyst(), player)) {
            return false;
        }
        Spell spell = plan.spell() != null ? plan.spell() : Spell.AETHER_BOLT;
        this.table.setChanged();
        this.access.execute((level, at) -> {
            level.blockEvent(at, ModBlocks.MAGIC_TABLE.get(), MagicTableBlockEntity.EVENT_RITUAL, spell.ordinal());
            level.playSound(null, at, SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 1.0f, 0.8f);
            level.playSound(null, at, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.BLOCKS, 1.4f, 0.6f);
            level.playSound(null, at, SoundEvents.EVOKER_CAST_SPELL, SoundSource.BLOCKS, 0.8f, 1.3f);
            if (level instanceof ServerLevel server) {
                server.sendParticles(ParticleTypes.ENCHANT, at.getX() + 0.5, at.getY() + 1.4, at.getZ() + 0.5, 40, 0.6, 0.5, 0.6, 0.8);
                server.sendParticles(new net.minecraft.core.particles.DustParticleOptions(spell.color(), 1.2f),
                        at.getX() + 0.5, at.getY() + 1.4, at.getZ() + 0.5, 30, 0.35, 0.45, 0.35, 0.02);
                server.sendParticles(ParticleTypes.END_ROD, at.getX() + 0.5, at.getY() + 1.5, at.getZ() + 0.5, 6, 0.2, 0.3, 0.2, 0.03);
            }
        });
        this.broadcastChanges();
        return true;
    }

    @Override
    public ItemStack quickMoveStack(Player player, int index) {
        Slot slot = this.slots.get(index);
        if (!slot.hasItem()) {
            return ItemStack.EMPTY;
        }
        ItemStack stack = slot.getItem();
        ItemStack original = stack.copy();
        int tableEnd = 3;
        int end = this.slots.size();
        if (index < tableEnd) {
            if (!this.moveItemStackTo(stack, tableEnd, end, true)) {
                return ItemStack.EMPTY;
            }
        } else {
            int target = stack.getItem() instanceof AetheritStaffItem ? MagicTableBlockEntity.STAFF
                    : Inscription.taughtBy(stack) != null ? MagicTableBlockEntity.TABLET
                    : Inscription.isCatalyst(stack) ? MagicTableBlockEntity.CATALYST : -1;
            if (target >= 0) {
                if (!this.moveItemStackTo(stack, target, target + 1, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (index < tableEnd + 27) {
                if (!this.moveItemStackTo(stack, tableEnd + 27, end, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(stack, tableEnd, tableEnd + 27, false)) {
                return ItemStack.EMPTY;
            }
        }
        if (stack.isEmpty()) {
            slot.setByPlayer(ItemStack.EMPTY);
        } else {
            slot.setChanged();
        }
        if (stack.getCount() == original.getCount()) {
            return ItemStack.EMPTY;
        }
        slot.onTake(player, stack);
        return original;
    }

    @Override
    public boolean stillValid(Player player) {
        return this.table.stillValid(player);
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.table.stopOpen(player);
    }
}
