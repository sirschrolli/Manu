package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;

/**
 * Abbauen mit den Spitzhacken der Waffenkammer: weitere Blöcke brechen so, als hätte der Spieler sie selbst abgebaut
 * (Schutz durch andere Mods, Beute samt Verzauberungen, Haltbarkeit, Erfahrung). Ein Spieler, der gerade solche Blöcke
 * bricht, löst dabei nicht erneut eine Fläche oder Ader aus.
 */
public final class ArmoryMining {
    private ArmoryMining() {}

    /** Die größte Ader, die der Adernbrecher auf einmal bricht (ohne den ersten Block). */
    public static final int MAX_VEIN = 48;
    private static final Set<UUID> BREAKING = new HashSet<>();

    /** Spezielle Erzsorten: Erz und Tiefenschiefer-Erz derselben Sorte gehören zu einer Ader. */
    private static final List<TagKey<Block>> ORE_KINDS = List.of(Tags.Blocks.ORES_COAL, Tags.Blocks.ORES_COPPER, Tags.Blocks.ORES_DIAMOND,
            Tags.Blocks.ORES_EMERALD, Tags.Blocks.ORES_GOLD, Tags.Blocks.ORES_IRON, Tags.Blocks.ORES_LAPIS, Tags.Blocks.ORES_NETHERITE_SCRAP,
            Tags.Blocks.ORES_QUARTZ, Tags.Blocks.ORES_REDSTONE);

    public static boolean isBreaking(LivingEntity entity) {
        return BREAKING.contains(entity.getUUID());
    }

    /** Bricht die Blöcke nacheinander als der Spieler; hört auf, wenn das Werkzeug zerbricht. Gibt die Zahl der Blöcke zurück. */
    public static int breakAs(ServerPlayer player, Collection<BlockPos> positions) {
        if (!BREAKING.add(player.getUUID())) {
            return 0;
        }
        int broken = 0;
        try {
            ItemStack tool = player.getMainHandItem();
            for (BlockPos pos : positions) {
                if (tool.isEmpty() || player.getMainHandItem() != tool) {
                    break;
                }
                if (!player.level().getBlockState(pos).isAir() && player.gameMode.destroyBlock(pos)) {
                    broken++;
                }
            }
        } finally {
            BREAKING.remove(player.getUUID());
        }
        return broken;
    }

    /** Die Seite des Blocks, auf die der Spieler schaut (sonst die nächstliegende zur Blickrichtung). */
    public static Direction face(ServerPlayer player, BlockPos pos) {
        Vec3 eye = player.getEyePosition();
        Vec3 end = eye.add(player.getLookAngle().scale(player.blockInteractionRange() + 1.0));
        BlockHitResult hit = player.level().clip(new ClipContext(eye, end, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, player));
        if (hit.getType() == HitResult.Type.BLOCK && hit.getBlockPos().equals(pos)) {
            return hit.getDirection();
        }
        return Direction.getApproximateNearest(player.getLookAngle()).getOpposite();
    }

    /** Die acht Nachbarn in der Fläche quer zur Seite {@code face} (die Fläche, die der Bergmannshammer bricht). */
    public static List<BlockPos> plane(BlockPos center, Direction face) {
        List<BlockPos> out = new ArrayList<>(8);
        Direction.Axis axis = face.getAxis();
        for (int a = -1; a <= 1; a++) {
            for (int b = -1; b <= 1; b++) {
                if (a == 0 && b == 0) {
                    continue;
                }
                out.add(switch (axis) {
                    case X -> center.offset(0, a, b);
                    case Y -> center.offset(a, 0, b);
                    case Z -> center.offset(a, b, 0);
                });
            }
        }
        return out;
    }

    /** Darf der Hammer diesen Nachbarn mitnehmen? Richtiges Werkzeug, nicht viel härter als der Block in der Mitte, kein Blockobjekt. */
    public static boolean fitsHammer(ServerLevel level, ItemStack tool, BlockPos pos, float centerHardness) {
        BlockState state = level.getBlockState(pos);
        if (state.isAir() || level.getBlockEntity(pos) != null || !tool.isCorrectToolForDrops(state)) {
            return false;
        }
        float hardness = state.getDestroySpeed(level, pos);
        return hardness >= 0.0f && hardness <= centerHardness * 2.0f + 1.0f;
    }

    public static boolean isOre(BlockState state) {
        return state.is(Tags.Blocks.ORES);
    }

    /** Gehören zwei Blöcke zur selben Ader? Gleicher Block – oder dieselbe Erzsorte (z. B. Eisenerz und Tiefenschiefer-Eisenerz). */
    public static boolean sameVein(BlockState a, BlockState b) {
        if (a.getBlock() == b.getBlock()) {
            return true;
        }
        for (TagKey<Block> kind : ORE_KINDS) {
            if (a.is(kind) && b.is(kind)) {
                return true;
            }
        }
        return false;
    }

    /** Die Ader um {@code start} (ohne ihn selbst): Nachbarn in allen 26 Richtungen, höchstens {@link #MAX_VEIN} Blöcke. */
    public static List<BlockPos> vein(ServerLevel level, BlockPos start, BlockState kind, ItemStack tool) {
        List<BlockPos> out = new ArrayList<>();
        Set<BlockPos> seen = new HashSet<>();
        seen.add(start);
        ArrayDeque<BlockPos> queue = new ArrayDeque<>();
        queue.add(start);
        while (!queue.isEmpty() && out.size() < MAX_VEIN) {
            BlockPos at = queue.poll();
            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockPos n = at.offset(dx, dy, dz);
                        if (out.size() >= MAX_VEIN || !seen.add(n)) {
                            continue;
                        }
                        BlockState state = level.getBlockState(n);
                        if (sameVein(kind, state) && tool.isCorrectToolForDrops(state)) {
                            out.add(n);
                            queue.add(n);
                        }
                    }
                }
            }
        }
        out.sort((p, q) -> Double.compare(p.distSqr(start), q.distSqr(start)));
        return out;
    }
}
