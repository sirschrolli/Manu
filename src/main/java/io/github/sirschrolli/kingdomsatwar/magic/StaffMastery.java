package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import java.util.List;
import java.util.Locale;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import org.jspecify.annotations.Nullable;

/**
 * Meisterschaft des Aetherit-Stabs: Ein neuer Stab ist schwach – erst mit jedem Zauber und jedem Gegner, den er
 * bezwingt, wächst seine Kraft. Sechs Stufen (Lehrling, dann Stufe 1 bis 5: Adept, Magier, Magister, Erzmagier,
 * Aetherfürst bei 1.000, 2.000, 5.000, 10.000 und 100.000 Erfahrung) bestimmen Stärke (60 % bis 150 %), Abklingzeit, wie
 * schnell sich ein Zauber aufladen lässt und wie viel Mana man hat; mit jeder Stufe wird auch der Stab selbst
 * prächtiger. Erfahrung gibt es für jeden Zauber (aufgeladen doppelt), jeden bezwungenen Gegner und viel für Bosse.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class StaffMastery {
    /** Höchstens so viel Kraft bringt das volle Aufladen (Rechtsklick halten). */
    public static final float CHARGE_BONUS = 0.5f;

    public enum Rank {
        APPRENTICE(0, 0.6f, 24, 1.25f),
        ADEPT(1_000, 0.75f, 22, 1.15f),
        MAGE(2_000, 0.9f, 19, 1.05f),
        MAGISTER(5_000, 1.05f, 16, 1.0f),
        ARCHMAGE(10_000, 1.2f, 13, 0.9f),
        AETHERLORD(100_000, 1.5f, 10, 0.8f);

        private final int xp;
        private final float power;
        private final int chargeTicks;
        private final float cooldown;

        Rank(int xp, float power, int chargeTicks, float cooldown) {
            this.xp = xp;
            this.power = power;
            this.chargeTicks = chargeTicks;
            this.cooldown = cooldown;
        }

        public String id() {
            return this.name().toLowerCase(Locale.ROOT);
        }

        /** Stufe 0 (Lehrling) bis 5 (Aetherfürst). */
        public int level() {
            return this.ordinal();
        }

        /** Ab so viel Erfahrung gilt der Rang. */
        public int xp() {
            return this.xp;
        }

        /** Stärke der Zauber (Schaden, Heilung, Rückstoß). */
        public float power() {
            return this.power;
        }

        /** So viele Ticks dauert das volle Aufladen. */
        public int chargeTicks() {
            return this.chargeTicks;
        }

        /** Faktor auf die Abklingzeit. */
        public float cooldown() {
            return this.cooldown;
        }

        public @Nullable Rank next() {
            return this.ordinal() + 1 < values().length ? values()[this.ordinal() + 1] : null;
        }

        public MutableComponent displayName() {
            return Component.translatable("staff.kingdomsatwar.rank." + this.id());
        }

        public static Rank of(int xp) {
            Rank rank = APPRENTICE;
            for (Rank r : values()) {
                if (xp >= r.xp) {
                    rank = r;
                }
            }
            return rank;
        }
    }

    private StaffMastery() {}

    public static int xp(ItemStack staff) {
        return staff.getOrDefault(ModDataComponents.STAFF_MASTERY.get(), 0);
    }

    public static Rank rank(ItemStack staff) {
        return Rank.of(xp(staff));
    }

    /** Stärke eines Zaubers aus diesem Stab: Rang mal Ladung (0–1). */
    public static float power(ItemStack staff, float charge) {
        return rank(staff).power() * (1.0f + CHARGE_BONUS * Mth.clamp(charge, 0.0f, 1.0f));
    }

    /** Fortschritt bis zum nächsten Rang (0–1; beim höchsten Rang 1). */
    public static float progress(ItemStack staff) {
        int xp = xp(staff);
        Rank rank = Rank.of(xp);
        Rank next = rank.next();
        return next == null ? 1.0f : Mth.clamp((xp - rank.xp()) / (float) (next.xp() - rank.xp()), 0.0f, 1.0f);
    }

    /** Setzt die Erfahrung (Tests, Befehle) und passt das Aussehen an. */
    public static void setXp(ItemStack staff, int xp) {
        if (xp <= 0) {
            staff.remove(ModDataComponents.STAFF_MASTERY.get());
        } else {
            staff.set(ModDataComponents.STAFF_MASTERY.get(), xp);
        }
        syncLook(staff);
    }

    /** Erfahrung für den Stab; beim Aufstieg leuchtet er auf und der Spieler erfährt es. */
    public static void award(ServerPlayer player, ItemStack staff, int amount) {
        if (amount <= 0 || staff.isEmpty()) {
            return;
        }
        Rank before = rank(staff);
        setXp(staff, xp(staff) + amount);
        Rank after = rank(staff);
        if (after != before) {
            ServerLevel level = player.level();
            player.sendSystemMessage(Component.translatable("staff.kingdomsatwar.rank_up", after.level(),
                    after.displayName().withStyle(ChatFormatting.LIGHT_PURPLE)).withStyle(ChatFormatting.GOLD));
            player.sendOverlayMessage(Component.translatable("staff.kingdomsatwar.rank_up.overlay", after.level(), after.displayName())
                    .withColor(Spell.AETHER_COLOR));
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.4f);
            level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 1.2f, 0.8f);
            for (int i = 0; i < 40; i++) {
                double a = i * Mth.TWO_PI / 40;
                level.sendParticles(new DustParticleOptions(Spell.AETHER_COLOR, 1.2f), player.getX() + Math.cos(a) * 1.2, player.getY() + 0.1 + i * 0.05,
                        player.getZ() + Math.sin(a) * 1.2, 1, 0, 0, 0, 0);
            }
            level.sendParticles(ParticleTypes.END_ROD, player.getX(), player.getY(1.0), player.getZ(), 30, 0.6, 0.8, 0.6, 0.08);
        }
    }

    /**
     * Aussehen des Stabs: Rang (Stufe des Modells) und Farbe des gewählten Zaubers (der Kristall leuchtet in ihr) als
     * {@link DataComponents#CUSTOM_MODEL_DATA}.
     */
    public static void syncLook(ItemStack staff) {
        CustomModelData wanted = new CustomModelData(List.of((float) rank(staff).ordinal()), List.of(), List.of(),
                List.of(KnownSpells.of(staff).selected().color()));
        if (!wanted.equals(staff.get(DataComponents.CUSTOM_MODEL_DATA))) {
            staff.set(DataComponents.CUSTOM_MODEL_DATA, wanted);
        }
    }

    /** Wer mit einem Zauber seines Stabs einen Gegner bezwingt, bringt ihn voran (Bosse zählen viel). */
    @SubscribeEvent
    public static void onDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (!(source.getEntity() instanceof ServerPlayer player) || !isSpell(source) || event.getEntity() == player) {
            return;
        }
        ItemStack staff = AetheritStaffItem.heldStaff(player);
        if (!staff.isEmpty()) {
            award(player, staff, event.getEntity() instanceof ElementalBoss ? 40 : 3);
        }
    }

    private static boolean isSpell(DamageSource source) {
        return source.is(DamageTypes.INDIRECT_MAGIC) || source.is(DamageTypes.MAGIC) || source.is(DamageTypes.FIREBALL)
                || source.is(DamageTypes.LIGHTNING_BOLT);
    }
}
