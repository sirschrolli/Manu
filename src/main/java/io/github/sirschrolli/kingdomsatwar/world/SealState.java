package io.github.sirschrolli.kingdomsatwar.world;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import org.jspecify.annotations.Nullable;

/**
 * Zustand des Siegels in einer Welt (Anhang an der Oberwelt): welche Siegelsplitter schon in ihren Monolithen stecken,
 * wo der Siegelkreis liegt (sobald ihn jemand gefunden hat), ob der Siegelbruch gerade läuft und ob das Siegel
 * gebrochen ist.
 */
public final class SealState {
    public static final Codec<SealState> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            DragonType.CODEC.listOf().optionalFieldOf("inserted", List.of()).forGetter(s -> List.copyOf(s.inserted)),
            Codec.BOOL.optionalFieldOf("broken", false).forGetter(s -> s.broken),
            BlockPos.CODEC.optionalFieldOf("center").forGetter(s -> Optional.ofNullable(s.center)),
            Codec.INT.optionalFieldOf("break_ticks", -1).forGetter(s -> s.breakTicks),
            BlockPos.CODEC.optionalFieldOf("shadow_gate").forGetter(s -> Optional.ofNullable(s.shadowGate)),
            Codec.INT.optionalFieldOf("nyxar_defeats", 0).forGetter(s -> s.nyxarDefeats)
    ).apply(instance, SealState::new));

    private final EnumSet<DragonType> inserted = EnumSet.noneOf(DragonType.class);
    private boolean broken;
    private @Nullable BlockPos center;
    /** Fortschritt des Siegelbruchs in Ticks (-1: läuft nicht). */
    private int breakTicks;
    /** Mitte des Gegentors in der Schattenwelt (sobald es jemand durchschritten hat). */
    private @Nullable BlockPos shadowGate;
    /** Wie oft Nyxar schon besiegt wurde (beim ersten Mal endet die Geschichte). */
    private int nyxarDefeats;

    public SealState() {
        this(List.of(), false, Optional.empty(), -1, Optional.empty(), 0);
    }

    private SealState(List<DragonType> inserted, boolean broken, Optional<BlockPos> center, int breakTicks, Optional<BlockPos> shadowGate,
            int nyxarDefeats) {
        this.nyxarDefeats = nyxarDefeats;
        this.inserted.addAll(inserted);
        this.broken = broken;
        this.center = center.orElse(null);
        this.breakTicks = breakTicks;
        this.shadowGate = shadowGate.orElse(null);
    }

    public static SealState of(ServerLevel level) {
        return level.getServer().overworld().getData(ModAttachments.SEAL);
    }

    /** Merkt die Änderungen zum Speichern vor. */
    public void save(ServerLevel level) {
        level.getServer().overworld().setData(ModAttachments.SEAL, this);
    }

    public boolean isInserted(DragonType element) {
        return this.inserted.contains(element);
    }

    public Set<DragonType> inserted() {
        return EnumSet.copyOf(this.inserted.isEmpty() ? EnumSet.noneOf(DragonType.class) : this.inserted);
    }

    public int insertedCount() {
        return this.inserted.size();
    }

    public void insert(DragonType element) {
        this.inserted.add(element);
    }

    public boolean isBroken() {
        return this.broken;
    }

    public void setBroken(boolean broken) {
        this.broken = broken;
    }

    public @Nullable BlockPos center() {
        return this.center;
    }

    public void setCenter(BlockPos center) {
        this.center = center.immutable();
    }

    public int breakTicks() {
        return this.breakTicks;
    }

    public boolean isBreaking() {
        return this.breakTicks >= 0;
    }

    public void setBreakTicks(int ticks) {
        this.breakTicks = ticks;
    }

    public @Nullable BlockPos shadowGate() {
        return this.shadowGate;
    }

    public void setShadowGate(@Nullable BlockPos pos) {
        this.shadowGate = pos == null ? null : pos.immutable();
    }

    public int nyxarDefeats() {
        return this.nyxarDefeats;
    }

    public void addNyxarDefeat() {
        this.nyxarDefeats++;
    }

    /** Alles zurück auf Anfang (nur für Befehle und Tests). */
    public void reset() {
        this.inserted.clear();
        this.broken = false;
        this.breakTicks = -1;
    }
}
