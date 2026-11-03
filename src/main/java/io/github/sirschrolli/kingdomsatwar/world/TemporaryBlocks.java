package io.github.sirschrolli.kingdomsatwar.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

/**
 * Blöcke, die Boss-Angriffe nur für kurze Zeit setzen (Eisspeere, Eisgefängnis, Wurzeln, Stalagmiten …): Nach
 * Ablauf verschwinden sie wieder und das Vorherige kehrt zurück – auch nach Speichern und Laden (die Liste hängt an
 * der Welt). Ersetzt werden nur Luft, Flüssigkeiten und leicht Ersetzbares, nie Blöcke mit Inhalt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class TemporaryBlocks {
    /** Ein gesetzter Block: was vorher dort war und wann er verschwindet (Spielzeit). */
    public record Entry(BlockPos pos, BlockState placed, BlockState original, long expires) {
        static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BlockPos.CODEC.fieldOf("pos").forGetter(Entry::pos),
                BlockState.CODEC.fieldOf("placed").forGetter(Entry::placed),
                BlockState.CODEC.fieldOf("original").forGetter(Entry::original),
                Codec.LONG.fieldOf("expires").forGetter(Entry::expires)
        ).apply(instance, Entry::new));
    }

    /** Alle zeitweiligen Blöcke einer Welt. */
    public static final class Store {
        public static final Codec<Store> CODEC = Entry.CODEC.listOf().xmap(Store::new, store -> List.copyOf(store.entries));
        private final List<Entry> entries;

        public Store() {
            this(List.of());
        }

        private Store(List<Entry> entries) {
            this.entries = new ArrayList<>(entries);
        }

        public int size() {
            return this.entries.size();
        }
    }

    private TemporaryBlocks() {}

    /** Kann hier ein zeitweiliger Block hin? */
    public static boolean canReplace(ServerLevel level, BlockPos pos) {
        BlockState old = level.getBlockState(pos);
        return level.isInWorldBounds(pos) && level.getBlockEntity(pos) == null && old.getDestroySpeed(level, pos) >= 0.0f
                && (old.isAir() || old.canBeReplaced() || !old.getFluidState().isEmpty());
    }

    /** Setzt einen Block für {@code ticks} Ticks (false: hier geht es nicht). */
    public static boolean place(ServerLevel level, BlockPos pos, BlockState state, int ticks) {
        if (!canReplace(level, pos)) {
            return false;
        }
        BlockState old = level.getBlockState(pos);
        Store store = level.getData(ModAttachments.TEMPORARY_BLOCKS);
        store.entries.add(new Entry(pos.immutable(), state, old.getFluidState().isEmpty() ? old : old.getFluidState().createLegacyBlock(),
                level.getGameTime() + ticks));
        level.setData(ModAttachments.TEMPORARY_BLOCKS, store);
        level.setBlock(pos, state, Block.UPDATE_ALL);
        return true;
    }

    @SubscribeEvent
    static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level) || level.getGameTime() % 5 != 0) {
            return;
        }
        Store store = level.getExistingDataOrNull(ModAttachments.TEMPORARY_BLOCKS);
        if (store == null || store.entries.isEmpty()) {
            return;
        }
        long now = level.getGameTime();
        boolean changed = false;
        for (int i = store.entries.size() - 1; i >= 0; i--) {
            Entry entry = store.entries.get(i);
            if (entry.expires() > now) {
                continue;
            }
            if (!level.isLoaded(entry.pos())) {
                continue;   // später, wenn der Chunk wieder geladen ist
            }
            if (level.getBlockState(entry.pos()).is(entry.placed().getBlock())) {
                level.setBlock(entry.pos(), entry.original(), Block.UPDATE_ALL);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, entry.placed()), entry.pos().getX() + 0.5,
                        entry.pos().getY() + 0.5, entry.pos().getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.05);
            }
            store.entries.remove(i);
            changed = true;
        }
        if (changed) {
            level.setData(ModAttachments.TEMPORARY_BLOCKS, store);
        }
    }
}
