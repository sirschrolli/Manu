package io.github.sirschrolli.kingdomsatwar.hero;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Herz- oder Manakristall in der Hand: Rechtsklick nimmt ihn auf – ein Herz bzw. 20 Mana mehr, für immer (siehe
 * {@link Crystals}). Sind schon alle zehn dieser Sorte aufgenommen, bleibt er in der Hand.
 */
public class CrystalItem extends Item {
    private final Crystals.Kind kind;

    public CrystalItem(Crystals.Kind kind, Properties properties) {
        super(properties);
        this.kind = kind;
    }

    public Crystals.Kind kind() {
        return this.kind;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (Crystals.of(player).of(this.kind) >= Crystals.MAX) {
            if (player instanceof ServerPlayer server) {
                server.sendOverlayMessage(Component.translatable("message.kingdomsatwar.crystal." + this.kind.getSerializedName() + ".full",
                        Crystals.MAX).withStyle(ChatFormatting.GRAY));
            }
            return InteractionResult.FAIL;
        }
        if (!(player instanceof ServerPlayer server) || !(level instanceof ServerLevel serverLevel)) {
            return InteractionResult.SUCCESS;
        }
        if (!Crystals.absorb(server, this.kind)) {
            return InteractionResult.FAIL;
        }
        stack.consume(1, player);
        player.awardStat(Stats.ITEM_USED.get(this));
        this.celebrate(serverLevel, server);
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Klang, ein Kranz aus Funken um den Spieler und die Nachricht, wie viele es jetzt sind. */
    private void celebrate(ServerLevel level, ServerPlayer player) {
        Crystals.Count count = Crystals.of(player);
        boolean heart = this.kind == Crystals.Kind.HEART;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f,
                heart ? 0.7f : 1.1f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), heart ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, heart ? 1.4f : 1.0f);
        DustParticleOptions dust = new DustParticleOptions(heart ? 0xFF3B5C : 0x4FA8FF, 1.2f);
        for (int i = 0; i < 24; i++) {
            float a = i * Mth.TWO_PI / 24.0f;
            double x = player.getX() + Mth.cos(a) * 0.9;
            double z = player.getZ() + Mth.sin(a) * 0.9;
            level.sendParticles(dust, x, player.getY() + 0.2 + (i % 4) * 0.45, z, 1, 0.0, 0.0, 0.0, 0.0);
        }
        level.sendParticles(heart ? ParticleTypes.HEART : ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.4, player.getZ(), heart ? 7 : 40,
                0.5, 0.5, 0.5, heart ? 0.05 : 0.6);
        Component message = heart
                ? Component.translatable("message.kingdomsatwar.crystal.heart.absorbed", (int) Math.round(player.getMaxHealth() / 2.0), count.hearts(),
                        Crystals.MAX)
                : Component.translatable("message.kingdomsatwar.crystal.mana.absorbed",
                        Math.round(io.github.sirschrolli.kingdomsatwar.magic.Mana.maxFor(player)), count.mana(), Crystals.MAX);
        player.sendOverlayMessage(message.copy().withStyle(heart ? ChatFormatting.RED : ChatFormatting.AQUA));
    }
}
