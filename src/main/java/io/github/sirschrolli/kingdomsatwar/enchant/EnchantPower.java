package io.github.sirschrolli.kingdomsatwar.enchant;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import java.util.Set;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Die Verzauberungen von Minecraft wirken etwas stärker (seit 0.87) – spürbar, aber nicht übertrieben:
 * <ul>
 *     <li><b>Schärfe</b>: +0,25 Schaden je Stufe dazu (Schärfe V: 4,25 statt 3)</li>
 *     <li><b>Bann, Nemesis der Gliederfüßer, Harpune</b>: +0,5 je Stufe gegen ihre Ziele (Stufe V: 15 statt 12,5)</li>
 *     <li><b>Stärke</b>: Pfeile +0,15 Grundschaden je Stufe (ein voll gespannter Schuss mit Stärke V trifft um ein Siebtel härter)</li>
 *     <li><b>Schutz</b> aller Art (auch Federfall): wirkt um 15 % stärker; mehr als 80 % hält er wie bei Minecraft nie ab</li>
 *     <li><b>Effizienz</b>: +6 % Abbautempo je Stufe</li>
 *     <li><b>Köder</b>: ein Fisch beißt je Stufe 6 % schneller an als ohnehin (siehe Angeln)</li>
 * </ul>
 * Dazu bringt das <b>Eulenauge</b> der Mod hier seine Nachtsicht – ruhig, ohne Partikelwirbel und ohne das Flackern, mit
 * dem ein Trank ausläuft.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class EnchantPower {
    public static final float SHARPNESS_EXTRA = 0.25f;
    public static final float SLAYER_EXTRA = 0.5f;
    public static final float POWER_EXTRA = 0.15f;
    public static final float PROTECTION = 1.15f;
    public static final float EFFICIENCY_EXTRA = 0.06f;
    public static final float LURE_EXTRA = 0.06f;

    /** Die Verzauberungen von Minecraft, die hier stärker wirken (für den Hinweis im Buch). */
    public static final Set<ResourceKey<Enchantment>> BOOSTED = Set.of(Enchantments.SHARPNESS, Enchantments.SMITE, Enchantments.BANE_OF_ARTHROPODS,
            Enchantments.IMPALING, Enchantments.POWER, Enchantments.PROTECTION, Enchantments.FIRE_PROTECTION, Enchantments.BLAST_PROTECTION,
            Enchantments.PROJECTILE_PROTECTION, Enchantments.FEATHER_FALLING, Enchantments.EFFICIENCY, Enchantments.LURE);

    /** Wie lange die Nachtsicht des Eulenauges noch mindestens reicht (darunter flackerte sie wie ein auslaufender Trank). */
    private static final int OWL_KEEP = 260;
    private static final int OWL_DURATION = 320;

    private EnchantPower() {}

    /** Die Stufe einer Verzauberung auf einem Gegenstand (0, wenn er sie nicht trägt). */
    public static int level(ItemStack stack, ResourceKey<Enchantment> key) {
        if (stack == null || stack.isEmpty()) {
            return 0;
        }
        ItemEnchantments enchantments = stack.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        if (enchantments.isEmpty()) {
            return 0;
        }
        for (var entry : enchantments.entrySet()) {
            Holder<Enchantment> holder = entry.getKey();
            if (holder.is(key)) {
                return entry.getIntValue();
            }
        }
        return 0;
    }

    /**
     * Was Schärfe, Bann, Nemesis, Harpune und Stärke hier über Minecraft hinaus dazugeben (zu dem Betrag, den Minecraft
     * mit allen Verzauberungen der Waffe errechnet).
     */
    public static float extraDamage(ItemStack weapon, Entity victim, DamageSource source) {
        if (weapon == null || weapon.isEmpty() || !weapon.isEnchanted()) {
            return 0.0f;
        }
        float extra = 0.0f;
        Entity direct = source.getDirectEntity();
        boolean arrow = direct != null && direct.typeHolder().is(EntityTypeTags.ARROWS);
        if (arrow) {
            // der Bogen wirkt nur über seine Pfeile (Schärfe auf einer Armbrust gibt es nicht)
            return POWER_EXTRA * level(weapon, Enchantments.POWER);
        }
        extra += SHARPNESS_EXTRA * level(weapon, Enchantments.SHARPNESS);
        if (victim != null) {
            if (victim.typeHolder().is(EntityTypeTags.SENSITIVE_TO_SMITE)) {
                extra += SLAYER_EXTRA * level(weapon, Enchantments.SMITE);
            }
            if (victim.typeHolder().is(EntityTypeTags.SENSITIVE_TO_BANE_OF_ARTHROPODS)) {
                extra += SLAYER_EXTRA * level(weapon, Enchantments.BANE_OF_ARTHROPODS);
            }
            if (victim.typeHolder().is(EntityTypeTags.SENSITIVE_TO_IMPALING)) {
                extra += SLAYER_EXTRA * level(weapon, Enchantments.IMPALING);
            }
        }
        return extra;
    }

    /** Effizienz: etwas schneller abbauen – nur, wo das Werkzeug ohnehin taugt (wie bei Minecraft). */
    @SubscribeEvent
    static void onBreakSpeed(PlayerEvent.BreakSpeed event) {
        ItemStack tool = event.getEntity().getMainHandItem();
        int efficiency = level(tool, Enchantments.EFFICIENCY);
        if (efficiency > 0 && tool.getDestroySpeed(event.getState()) > 1.0f) {
            event.setNewSpeed(event.getNewSpeed() * (1.0f + EFFICIENCY_EXTRA * efficiency));
        }
    }

    /** Eulenauge: Nachtsicht, solange der Helm getragen wird. */
    @SubscribeEvent
    static void onPlayerTick(PlayerTickEvent.Post event) {
        Player player = event.getEntity();
        if (!(player instanceof ServerPlayer) || player.tickCount % 10 != 0) {
            return;
        }
        owlEye(player);
    }

    /** Gibt die Nachtsicht des Eulenauges oder nimmt sie wieder (nur die eigene – ein Trank bleibt unberührt). */
    public static void owlEye(Player player) {
        boolean worn = level(player.getItemBySlot(EquipmentSlot.HEAD), ModEnchantments.OWL_EYE) > 0;
        MobEffectInstance current = player.getEffect(MobEffects.NIGHT_VISION);
        boolean ours = current != null && current.isAmbient() && !current.isVisible() && current.getDuration() <= OWL_DURATION;
        if (worn) {
            if (current == null || ours && current.getDuration() < OWL_KEEP) {
                player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, OWL_DURATION, 0, true, false, true));
            }
        } else if (ours) {
            player.removeEffect(MobEffects.NIGHT_VISION);
        }
    }
}
