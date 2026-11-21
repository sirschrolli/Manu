package io.github.sirschrolli.kingdomsatwar.item.fae;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicCardEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModParticles;
import io.github.sirschrolli.kingdomsatwar.registry.ModSounds;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Das Kartendeck der Mystik: MissMystiks eigene Zauberkarten. Rechtsklick wirft einen Fächer aus drei leuchtenden Karten,
 * die wirbelnd geradeaus fliegen (je 6 Schaden, Rüstung schützt); jeder fünfte Wurf ist ein <b>Ass im Ärmel</b> – fünf
 * Karten, jede härter. Kurz warten zwischen den Würfen; jeder Wurf kostet etwas Haltbarkeit (Feenstaub repariert).
 */
public class MysticDeckItem extends Item {
    public static final int COOLDOWN = 14;
    private static final String THROWS = KingdomsAtWar.MODID + ":mystic_deck_throws";

    public MysticDeckItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel server)) {
            return InteractionResult.SUCCESS;
        }
        CompoundTag data = player.getPersistentData();
        int throwsDone = data.getIntOr(THROWS, 0) + 1;
        boolean ace = throwsDone >= 5;
        data.putInt(THROWS, ace ? 0 : throwsDone);
        throwFan(server, player, ace ? 5 : 3, ace ? 9.0f : 6.0f, ace ? 10.0f : 7.0f);
        if (ace) {
            player.sendOverlayMessage(Component.translatable("item.kingdomsatwar.mystic_deck.ace").withColor(0xFFD86A));
            server.sendParticles(ModParticles.CONFETTI.get(), player.getX(), player.getEyeY(), player.getZ(), 16, 0.3, 0.2, 0.3, 0.15);
        }
        server.playSound(null, player.getX(), player.getY(), player.getZ(), ModSounds.MYSTIC_DECK_THROW.get(), SoundSource.PLAYERS, 1.0f,
                ace ? 1.25f : 1.0f + player.getRandom().nextFloat() * 0.15f);
        player.getCooldowns().addCooldown(stack, COOLDOWN);
        stack.hurtAndBreak(1, player, hand.asEquipmentSlot());
        return InteractionResult.SUCCESS_SERVER;
    }

    /** Wirft {@code count} Karten im Fächer (halber Öffnungswinkel {@code spread} Grad) in Blickrichtung. */
    public static void throwFan(ServerLevel level, Player player, int count, float damage, float spread) {
        Vec3 look = player.getLookAngle();
        double baseYaw = Math.atan2(look.z, look.x);
        double pitch = Math.asin(Mth.clamp(look.y, -1.0, 1.0));
        for (int i = 0; i < count; i++) {
            double off = count == 1 ? 0.0 : (i / (count - 1.0) - 0.5) * 2.0 * spread * Mth.DEG_TO_RAD;
            double yaw = baseYaw + off;
            Vec3 d = new Vec3(Math.cos(yaw) * Math.cos(pitch), Math.sin(pitch), Math.sin(yaw) * Math.cos(pitch));
            MagicCardEntity card = new MagicCardEntity(level, player);
            card.setSuit(level.getRandom().nextInt(4));
            card.setDamage(damage);
            card.setPos(player.getX() + d.x * 0.6, player.getEyeY() - 0.15 + d.y * 0.6, player.getZ() + d.z * 0.6);
            card.setDeltaMovement(d.scale(1.5));
            level.addFreshEntity(card);
        }
    }
}
