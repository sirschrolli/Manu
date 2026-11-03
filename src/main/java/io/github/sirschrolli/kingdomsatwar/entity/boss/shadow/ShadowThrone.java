package io.github.sirschrolli.kingdomsatwar.entity.boss.shadow;

import io.github.sirschrolli.kingdomsatwar.block.entity.ShadowThroneCoreBlockEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ShadowItems;
import io.github.sirschrolli.kingdomsatwar.world.SealState;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;

/**
 * Was geschieht, wenn Nyxar fällt: Über dem Thronhof bricht Licht durch, das Herz der Schatten schwebt dort, wo er
 * zusammenbrach, alle Spieler erfahren es – und beim ersten Sieg endet die Geschichte vom Siegel mit einer Titelkarte
 * für alle, die im Thronhof dabei waren.
 */
public final class ShadowThrone {
    private ShadowThrone() {}

    public static void onNyxarDefeated(ServerLevel level, NyxarEntity nyxar, BlockPos home) {
        SealState seal = SealState.of(level);
        boolean first = seal.nyxarDefeats() == 0;
        seal.addNyxarDefeat();
        seal.save(level);

        BlockPos spot = BlockPos.containing(nyxar.getX(), home.getY(), nyxar.getZ());
        spawnHeart(level, spot);
        // eine Lichtsäule über dem Thronhof
        for (int i = 0; i < 60; i++) {
            level.sendParticles(ParticleTypes.END_ROD, home.getX() + 0.5, home.getY() + 1 + i * 0.8, home.getZ() + 0.5, 4, 0.3, 0.3, 0.3, 0.01);
        }
        level.playSound(null, home, SoundEvents.BEACON_ACTIVATE, SoundSource.BLOCKS, 4.0f, 0.6f);
        level.playSound(null, home, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.BLOCKS, 3.0f, 0.5f);

        if (nyxar.getLairCore() != null && level.getBlockEntity(nyxar.getLairCore()) instanceof ShadowThroneCoreBlockEntity core) {
            core.onBossDefeated(level);
        }

        Component name = Component.translatable("entity.kingdomsatwar.shadow_dragon_boss").withColor(0xB46CFF);
        for (ServerPlayer player : level.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.translatable("message.kingdomsatwar.story.nyxar_fallen", name).withStyle(ChatFormatting.GOLD));
        }
        if (first) {
            for (ServerPlayer player : nyxar.nearbyPlayers(level, 120.0)) {
                player.connection.send(new ClientboundSetTitlesAnimationPacket(30, 120, 50));
                player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.kingdomsatwar.story.ending_title")
                        .withStyle(ChatFormatting.GOLD)));
                player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("message.kingdomsatwar.story.ending_subtitle")
                        .withStyle(ChatFormatting.GRAY)));
                player.sendSystemMessage(Component.translatable("message.kingdomsatwar.story.ending")
                        .withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.ITALIC));
                level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
        }
    }

    /** Das Herz der Schatten schwebt leuchtend über dem Boden, bis jemand es nimmt. */
    private static void spawnHeart(ServerLevel level, BlockPos spot) {
        ItemEntity heart = new ItemEntity(level, spot.getX() + 0.5, spot.getY() + 1.6, spot.getZ() + 0.5,
                new ItemStack(ShadowItems.SHADOW_HEART.get()), 0.0, 0.0, 0.0);
        heart.setNoGravity(true);
        heart.setUnlimitedLifetime();
        heart.setPermanentlyInvulnerable(true);
        heart.setGlowingTag(true);
        heart.setPickUpDelay(60);
        level.addFreshEntity(heart);
        level.sendParticles(ParticleTypes.SCULK_SOUL, heart.getX(), heart.getY(), heart.getZ(), 40, 0.4, 0.4, 0.4, 0.05);
        level.sendParticles(ParticleTypes.END_ROD, heart.getX(), heart.getY(), heart.getZ(), 30, 0.3, 0.3, 0.3, 0.05);
    }
}
