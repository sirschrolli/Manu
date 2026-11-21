package io.github.sirschrolli.kingdomsatwar.item.armory;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.Tags;

/**
 * Erzspürer: eine Abyssit-Picke mit einem Auge in der Fassung. Mit der rechten Taste schaut das Auge durch den Fels: Alle
 * Erze im Umkreis von {@link #RADIUS} Blöcken leuchten {@link OreGlowEntity#LIFETIME} Ticks lang durch die Wände, jedes in
 * der Farbe seines Erzes (höchstens {@link #MAX_GLOWS}, die nächsten zuerst). Danach ruht er {@link #COOLDOWN} Ticks.
 */
public class OreSeekerItem extends Item {
    public static final int RADIUS = 12;
    public static final int MAX_GLOWS = 160;
    public static final int COOLDOWN = 200;

    public OreSeekerItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server) || !(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.SUCCESS;
        }
        List<BlockPos> ores = scan(server, player.blockPosition());
        reveal(server, serverPlayer, ores);
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        stack.hurtAndBreak(2, player, hand);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Alle Erze im Umkreis (Kugel), die nächsten zuerst, höchstens {@link #MAX_GLOWS}. */
    public static List<BlockPos> scan(ServerLevel level, BlockPos center) {
        List<BlockPos> found = new ArrayList<>();
        int r2 = RADIUS * RADIUS;
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-RADIUS, -RADIUS, -RADIUS), center.offset(RADIUS, RADIUS, RADIUS))) {
            if (pos.distSqr(center) <= r2 && ArmoryMining.isOre(level.getBlockState(pos))) {
                found.add(pos.immutable());
            }
        }
        found.sort(Comparator.comparingDouble(p -> p.distSqr(center)));
        return found.size() > MAX_GLOWS ? found.subList(0, MAX_GLOWS) : found;
    }

    /** Die Erze aufleuchten lassen (nur für diesen Spieler) – mit einem Pulsschlag aus dem Auge. */
    public static int reveal(ServerLevel level, ServerPlayer player, List<BlockPos> ores) {
        for (BlockPos pos : ores) {
            BlockState state = level.getBlockState(pos);
            level.addFreshEntity(OreGlowEntity.create(level, pos, state, color(state), player));
        }
        // ein Ring um die Füße, der sich ausbreitet (nicht vor die Augen – sonst verdeckt er, was leuchtet)
        for (int i = 0; i < 28; i++) {
            double a = Math.PI * 2.0 * i / 28.0;
            double dx = Math.cos(a);
            double dz = Math.sin(a);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, player.getX() + dx * 0.6, player.getY() + 0.1, player.getZ() + dz * 0.6, 0,
                    dx, 0.02, dz, 0.35);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.ENDER_EYE_DEATH, SoundSource.PLAYERS, 0.9f, 0.6f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 0.8f);
        return ores.size();
    }

    /** Die Farbe des Leuchtens je Erzsorte. */
    public static int color(BlockState state) {
        if (state.is(Tags.Blocks.ORES_DIAMOND)) {
            return 0x5CF5E8;
        }
        if (state.is(Tags.Blocks.ORES_EMERALD)) {
            return 0x2BE36F;
        }
        if (state.is(Tags.Blocks.ORES_GOLD)) {
            return 0xFFD83A;
        }
        if (state.is(Tags.Blocks.ORES_IRON)) {
            return 0xE8B898;
        }
        if (state.is(Tags.Blocks.ORES_COPPER)) {
            return 0xE87A4E;
        }
        if (state.is(Tags.Blocks.ORES_REDSTONE)) {
            return 0xFF2A2A;
        }
        if (state.is(Tags.Blocks.ORES_LAPIS)) {
            return 0x3A6BFF;
        }
        if (state.is(Tags.Blocks.ORES_COAL)) {
            return 0x8A8A8A;
        }
        if (state.is(Tags.Blocks.ORES_QUARTZ)) {
            return 0xF4EEE6;
        }
        if (state.is(Tags.Blocks.ORES_NETHERITE_SCRAP)) {
            return 0xB0704E;
        }
        return 0xE45CFF;                                                           // die Erze des Königreichs: violett
    }
}
