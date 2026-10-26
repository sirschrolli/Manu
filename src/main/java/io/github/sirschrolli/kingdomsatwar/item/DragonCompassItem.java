package io.github.sirschrolli.kingdomsatwar.item;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import io.github.sirschrolli.kingdomsatwar.world.LairRecords;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.NestLocator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.codec.NeoForgeStreamCodecs;
import org.jspecify.annotations.Nullable;

/**
 * Drachenkompass: Seine Nadel zeigt zum Nest eines der sechs Drachen-Bosse – jedes Nest gibt es nur einmal in der
 * Welt. Er sucht von sich aus das nächste noch unbesiegte Nest (Reihenfolge: vom nächsten zum fernsten). Rechtsklick
 * nennt Richtung und Entfernung, Schleichen + Rechtsklick wechselt zum nächsten Nest. Der Edelstein in der Fassung
 * leuchtet in der Farbe des gesuchten Drachen. In anderen Dimensionen dreht sich die Nadel ziellos.
 *
 * <p>Technisch nutzt der Kompass die Vanilla-Komponente {@code lodestone_tracker} (ohne Leitstein) – so dreht das
 * Spiel die Nadel selbst. Die Farbe des Edelsteins kommt über {@code custom_model_data}.
 */
public class DragonCompassItem extends Item {
    /** Reihenfolge der Nester – vom nächsten zum fernsten, so wie die Chronik sie erzählt. */
    public static final List<DragonType> ORDER = List.of(
            DragonType.NATURE, DragonType.WATER, DragonType.EARTH, DragonType.ICE, DragonType.LIGHTNING, DragonType.FIRE);
    private static final String[] DIRECTIONS = {"n", "ne", "e", "se", "s", "sw", "w", "nw"};

    /** Gesuchtes Nest und ob sein Drache schon besiegt ist. */
    public record Target(DragonType element, boolean defeated) {
        public static final Codec<Target> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                DragonType.CODEC.fieldOf("element").forGetter(Target::element),
                Codec.BOOL.optionalFieldOf("defeated", false).forGetter(Target::defeated)
        ).apply(instance, Target::new));
        public static final StreamCodec<FriendlyByteBuf, Target> STREAM_CODEC = StreamCodec.composite(
                NeoForgeStreamCodecs.enumCodec(DragonType.class), Target::element,
                ByteBufCodecs.BOOL, Target::defeated,
                Target::new);
    }

    public DragonCompassItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level instanceof ServerLevel server && server.dimension() == io.github.sirschrolli.kingdomsatwar.world.ShadowGate.SHADOW_WORLD) {
            // in der Schattenwelt kennt der Kompass nur ein Ziel: Nyxars Zitadelle
            BlockPos citadel = io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator.approximateCenter(server);
            if (citadel == null) {
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.dragon_compass.no_nests").withStyle(ChatFormatting.GRAY));
                return InteractionResult.FAIL;
            }
            aimCitadel(stack, citadel);
            player.sendOverlayMessage(describeCitadel(player, citadel));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS, 1.0f, 0.5f);
            return InteractionResult.SUCCESS;
        }
        if (level instanceof ServerLevel server) {
            ServerLevel overworld = server.getServer().overworld();
            Map<DragonType, ChunkPos> nests = NestLocator.all(overworld);
            if (nests.isEmpty()) {
                player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.dragon_compass.no_nests").withStyle(ChatFormatting.GRAY));
                return InteractionResult.FAIL;
            }
            Target current = stack.get(ModDataComponents.COMPASS_TARGET.get());
            boolean switching = current != null && player.isShiftKeyDown();
            DragonType next;
            if (current == null || !nests.containsKey(current.element())) {
                next = defaultTarget(nests, overworld);
            } else {
                next = switching ? following(nests, current.element()) : current.element();
            }
            aim(stack, overworld, next, nests.get(next));
            player.sendOverlayMessage(describe(player, overworld, next, nestCenter(overworld, next, nests.get(next))));
            if (switching || current == null) {
                level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.LODESTONE_COMPASS_LOCK, SoundSource.PLAYERS,
                        1.0f, 0.8f + 0.1f * ORDER.indexOf(next));
            }
        }
        return InteractionResult.SUCCESS;
    }

    /** Hält Ziel, Nadel und Farbe aktuell (einmal pro Sekunde; neu hergestellte Kompasse richten sich selbst aus). */
    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        if ((level.getGameTime() + owner.getId()) % 20 != 0) {
            return;
        }
        if (level.dimension() == io.github.sirschrolli.kingdomsatwar.world.ShadowGate.SHADOW_WORLD) {
            BlockPos citadel = io.github.sirschrolli.kingdomsatwar.worldgen.citadel.CitadelLocator.approximateCenter(level);
            if (citadel != null) {
                aimCitadel(stack, citadel);
            }
            return;
        }
        ServerLevel overworld = level.getServer().overworld();
        Map<DragonType, ChunkPos> nests = NestLocator.all(overworld);
        if (nests.isEmpty()) {
            return;
        }
        Target current = stack.get(ModDataComponents.COMPASS_TARGET.get());
        DragonType element;
        if (current == null || !nests.containsKey(current.element())) {
            element = defaultTarget(nests, overworld);
        } else if (!current.defeated() && LairRecords.of(overworld).isDefeated(current.element())) {
            element = defaultTarget(nests, overworld);   // gerade besiegt: weiter zum nächsten Nest
        } else {
            element = current.element();
        }
        aim(stack, overworld, element, nests.get(element));
    }

    /** Das erste noch nicht besiegte Nest (sonst das erste überhaupt). */
    public static DragonType defaultTarget(Map<DragonType, ChunkPos> nests, ServerLevel overworld) {
        LairRecords records = LairRecords.of(overworld);
        DragonType first = null;
        for (DragonType type : ORDER) {
            if (nests.containsKey(type)) {
                if (first == null) {
                    first = type;
                }
                if (!records.isDefeated(type)) {
                    return type;
                }
            }
        }
        return first != null ? first : DragonType.FIRE;
    }

    private static DragonType following(Map<DragonType, ChunkPos> nests, DragonType current) {
        int start = ORDER.indexOf(current);
        for (int i = 1; i <= ORDER.size(); i++) {
            DragonType type = ORDER.get(Math.floorMod(start + i, ORDER.size()));
            if (nests.containsKey(type)) {
                return type;
            }
        }
        return current;
    }

    /** Richtet den Kompass auf ein Nest aus (setzt Komponenten nur, wenn sich etwas ändert). */
    public static void aim(ItemStack stack, ServerLevel overworld, DragonType element, ChunkPos chunk) {
        Target target = new Target(element, LairRecords.of(overworld).isDefeated(element));
        if (!target.equals(stack.get(ModDataComponents.COMPASS_TARGET.get()))) {
            stack.set(ModDataComponents.COMPASS_TARGET.get(), target);
        }
        LodestoneTracker tracker = new LodestoneTracker(Optional.of(GlobalPos.of(Level.OVERWORLD, nestCenter(overworld, element, chunk))), false);
        if (!tracker.equals(stack.get(DataComponents.LODESTONE_TRACKER))) {
            stack.set(DataComponents.LODESTONE_TRACKER, tracker);
        }
        CustomModelData color = new CustomModelData(List.of(), List.of(), List.of(), List.of(element.color()));
        if (!color.equals(stack.get(DataComponents.CUSTOM_MODEL_DATA))) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, color);
        }
    }

    /** Richtet den Kompass auf die Schattenzitadelle aus (violett). */
    public static void aimCitadel(ItemStack stack, BlockPos citadel) {
        LodestoneTracker tracker = new LodestoneTracker(Optional.of(GlobalPos.of(io.github.sirschrolli.kingdomsatwar.world.ShadowGate.SHADOW_WORLD,
                citadel)), false);
        if (!tracker.equals(stack.get(DataComponents.LODESTONE_TRACKER))) {
            stack.set(DataComponents.LODESTONE_TRACKER, tracker);
        }
        CustomModelData color = new CustomModelData(List.of(), List.of(), List.of(), List.of(0xB46CFF));
        if (!color.equals(stack.get(DataComponents.CUSTOM_MODEL_DATA))) {
            stack.set(DataComponents.CUSTOM_MODEL_DATA, color);
        }
    }

    /** „Nyxars Schattenzitadelle – etwa 1240 Blöcke im Nordosten“. */
    private static Component describeCitadel(Player player, BlockPos center) {
        MutableComponent name = Component.translatable("item.kingdomsatwar.dragon_compass.citadel").withColor(0xB46CFF);
        double dx = center.getX() + 0.5 - player.getX();
        double dz = center.getZ() + 0.5 - player.getZ();
        int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
        MutableComponent where;
        if (distance < 48) {
            where = Component.translatable("item.kingdomsatwar.dragon_compass.here");
        } else {
            double bearing = Math.toDegrees(Math.atan2(dx, -dz));
            int index = Mth.positiveModulo((int) Math.round(bearing / 45.0), 8);
            where = Component.translatable("item.kingdomsatwar.dragon_compass.distance", Math.round(distance / 10.0) * 10,
                    Component.translatable("direction.kingdomsatwar." + DIRECTIONS[index]));
        }
        return Component.translatable("item.kingdomsatwar.dragon_compass.aimed", name, where).withStyle(ChatFormatting.GOLD);
    }

    /** Mitte des Nests: genau, sobald das Nest erzeugt wurde, sonst die Mitte seines Chunks. */
    public static BlockPos nestCenter(ServerLevel overworld, DragonType element, ChunkPos chunk) {
        LairRecords.Entry entry = LairRecords.of(overworld).get(element);
        if (entry != null) {
            return entry.center();
        }
        return new BlockPos(chunk.getMiddleBlockX(), overworld.getSeaLevel(), chunk.getMiddleBlockZ());
    }

    /** „Pyrrhons Vulkankrater – etwa 1240 Blöcke im Nordosten“ (oder „in der Oberwelt“ aus anderen Dimensionen). */
    private static Component describe(Player player, ServerLevel overworld, DragonType element, BlockPos center) {
        MutableComponent name = Component.translatable("lair.kingdomsatwar." + element.id() + ".name").withColor(element.color());
        MutableComponent where;
        if (player.level().dimension() != Level.OVERWORLD) {
            where = Component.translatable("item.kingdomsatwar.dragon_compass.other_dimension");
        } else {
            double dx = center.getX() + 0.5 - player.getX();
            double dz = center.getZ() + 0.5 - player.getZ();
            int distance = (int) Math.round(Math.sqrt(dx * dx + dz * dz));
            if (distance < 48) {
                where = Component.translatable("item.kingdomsatwar.dragon_compass.here");
            } else {
                double bearing = Math.toDegrees(Math.atan2(dx, -dz));   // 0 = Norden, 90 = Osten
                int index = Mth.positiveModulo((int) Math.round(bearing / 45.0), 8);
                where = Component.translatable("item.kingdomsatwar.dragon_compass.distance", Math.round(distance / 10.0) * 10,
                        Component.translatable("direction.kingdomsatwar." + DIRECTIONS[index]));
            }
        }
        MutableComponent line = Component.translatable("item.kingdomsatwar.dragon_compass.aimed", name, where).withStyle(ChatFormatting.GOLD);
        if (LairRecords.of(overworld).isDefeated(element)) {
            line.append(Component.translatable("item.kingdomsatwar.dragon_compass.defeated_suffix").withStyle(ChatFormatting.GRAY));
        }
        return line;
    }

    @Override
    @SuppressWarnings("deprecation")
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        Target target = stack.get(ModDataComponents.COMPASS_TARGET.get());
        if (target == null) {
            builder.accept(Component.translatable("item.kingdomsatwar.dragon_compass.unaimed").withStyle(ChatFormatting.GRAY));
            return;
        }
        builder.accept(Component.translatable("item.kingdomsatwar.dragon_compass.seeking",
                Component.translatable("lair.kingdomsatwar." + target.element().id() + ".name").withColor(target.element().color()))
                .withStyle(ChatFormatting.GRAY));
        if (target.defeated()) {
            builder.accept(Component.translatable("item.kingdomsatwar.dragon_compass.defeated").withStyle(ChatFormatting.DARK_GRAY));
        }
        builder.accept(Component.translatable("item.kingdomsatwar.dragon_compass.hint").withStyle(ChatFormatting.DARK_GRAY));
    }
}
