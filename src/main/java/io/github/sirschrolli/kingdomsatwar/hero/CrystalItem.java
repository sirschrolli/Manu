package io.github.sirschrolli.kingdomsatwar.hero;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustColorTransitionOptions;
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
 * Herz- oder Manakristall in der Hand: Rechtsklick nimmt ihn auf – ein halbes Herz bzw. 10 Mana mehr, für immer (siehe
 * {@link Crystals}). Sind schon alle dieser Sorte aufgenommen (seit 0.77 je 25), bleibt er in der Hand.
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

    /**
     * Klang, eine Spirale feiner Funken, die um den Spieler aufsteigt, und die Nachricht, wie viele es jetzt sind. Beim
     * Herzkristall steigen über ihm kleine Herzen auf, beim Manakristall fliegen Zauberzeichen in ihn hinein und über ihm
     * funkeln blaue Lichter.
     */
    private void celebrate(ServerLevel level, ServerPlayer player) {
        Crystals.Count count = Crystals.of(player);
        boolean heart = this.kind == Crystals.Kind.HEART;
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f,
                heart ? 0.7f : 1.1f);
        level.playSound(null, player.getX(), player.getY(), player.getZ(), heart ? SoundEvents.BEACON_POWER_SELECT : SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1.0f, heart ? 1.4f : 1.0f);
        DustColorTransitionOptions dust = heart ? new DustColorTransitionOptions(0xFF3B5C, 0xFFD0D8, 0.75f)
                : new DustColorTransitionOptions(0x4FA8FF, 0xE6F7FF, 0.75f);
        for (int i = 0; i < 36; i++) {
            float a = i * Mth.TWO_PI / 18.0f;
            level.sendParticles(dust, player.getX() + Mth.cos(a) * 1.25, player.getY() + 0.1 + i * 0.034, player.getZ() + Mth.sin(a) * 1.25, 1,
                    0.0, 0.0, 0.0, 0.0);
        }
        if (heart) {
            level.sendParticles(ParticleTypes.HEART, player.getX(), player.getY() + 2.2, player.getZ(), 6, 0.45, 0.15, 0.45, 0.02);
        } else {
            level.sendParticles(ParticleTypes.ENCHANT, player.getX(), player.getY() + 1.2, player.getZ(), 48, 0.4, 0.5, 0.4, 0.9);
            level.sendParticles(ParticleTypes.GLOW, player.getX(), player.getY() + 2.2, player.getZ(), 6, 0.45, 0.15, 0.45, 0.01);
        }
        Component message = heart
                ? Component.translatable("message.kingdomsatwar.crystal.heart.absorbed", hearts(player.getMaxHealth()), count.hearts(),
                        Crystals.MAX)
                : Component.translatable("message.kingdomsatwar.crystal.mana.absorbed",
                        Math.round(io.github.sirschrolli.kingdomsatwar.magic.Mana.maxFor(player)), count.mana(), Crystals.MAX);
        player.sendOverlayMessage(message.copy().withStyle(heart ? ChatFormatting.RED : ChatFormatting.AQUA));
    }

    /** Die Herzen als Zahl – mit „½“, wenn ein halbes dabei ist (seit 1.3 gibt jeder Herzkristall ein halbes Herz). */
    static String hearts(float maxHealth) {
        int halves = Math.round(maxHealth);
        return halves % 2 == 0 ? Integer.toString(halves / 2) : (halves / 2) + "½";
    }
}
