package io.github.sirschrolli.kingdomsatwar.magic;

import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModDataComponents;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
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
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUseAnimation;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.component.UseCooldown;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Aetherit-Stab: Rechtsklick wirkt den gewählten Zauber – kurz tippen für einen schnellen Zauber, gedrückt halten zum
 * Aufladen (unter dem Zaubernden erscheint ein Zauberkreis, der mit der Ladung wächst; voll geladen bis zu 50 % mehr
 * Kraft). Jeder Stab kennt den Aether-Blitz; weitere Zauber lernt er am Magietisch aus Zaubertafeln, gewählt wird im
 * Zauberkreis (Taste halten, Standard: V).
 *
 * <p>Der Stab ist unzerstörbar (keine Haltbarkeit, verbrennt nicht, vergeht nicht) – dafür ist er nur mit großem
 * Aufwand herzustellen und anfangs schwach: Seine Kraft wächst mit der Meisterschaft ({@link StaffMastery}).
 *
 * <p>Jeder Zauber hat eine eigene Abklingzeit. Dafür trägt der Stab die Abklingzeit-Gruppe des gewählten Zaubers als
 * {@link DataComponents#USE_COOLDOWN}; nach dem Zauber setzt sie der Stab selbst (Vanilla tut es bei Gegenständen mit
 * Benutzungsdauer nicht).
 */
public class AetheritStaffItem extends Item {
    /** So lange darf man höchstens aufladen (danach bleibt die Ladung voll). */
    private static final int USE_DURATION = 72000;
    /** Die Zauberkreise der gerade aufladenden Spieler (nur auf dem Server). */
    private static final Map<UUID, SpellFxEntity> CIRCLES = new HashMap<>();

    public AetheritStaffItem(Properties properties) {
        super(properties);
    }

    /** Abklingzeit-Angabe eines neuen Stabs (Aether-Blitz, Stufe 1). */
    public static UseCooldown defaultCooldown() {
        return cooldownFor(Spell.AETHER_BOLT, 1);
    }

    private static UseCooldown cooldownFor(Spell spell, int level) {
        // etwas über der vollen Tickzahl, damit Vanilla beim Abrunden (Sekunden * 20) genau sie erhält
        return new UseCooldown((spell.cooldown(level) + 0.05f) / 20.0f, Optional.of(spell.cooldownGroup()));
    }

    /** Bringt die Abklingzeit-Angabe des Stabs auf den gewählten Zauber und seine Stufe, dazu sein Aussehen. */
    public static void syncCooldown(ItemStack staff) {
        KnownSpells spells = KnownSpells.of(staff);
        UseCooldown wanted = cooldownFor(spells.selected(), spells.selectedLevel());
        if (!wanted.equals(staff.get(DataComponents.USE_COOLDOWN))) {
            staff.set(DataComponents.USE_COOLDOWN, wanted);
        }
        StaffMastery.syncLook(staff);
    }

    /** Speichert neue Zauber am Stab (ohne Angabe, solange er nur den Aether-Blitz kennt). */
    public static void setSpells(ItemStack staff, KnownSpells spells) {
        if (spells.equals(KnownSpells.DEFAULT)) {
            staff.remove(ModDataComponents.SPELLS.get());
        } else {
            staff.set(ModDataComponents.SPELLS.get(), spells);
        }
        syncCooldown(staff);
    }

    /** Wählt einen bekannten Zauber. @return ob sich etwas geändert hat */
    public static boolean select(ItemStack staff, Spell spell) {
        KnownSpells spells = KnownSpells.of(staff);
        if (!spells.knows(spell) || spells.selected() == spell) {
            return false;
        }
        setSpells(staff, spells.select(spell));
        return true;
    }

    /** Der Stab in der Hand (Haupthand zuerst), sonst ein leerer Stapel. */
    public static ItemStack heldStaff(Player player) {
        for (InteractionHand hand : InteractionHand.values()) {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof AetheritStaffItem) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }

    // =====================================================================================
    // Aufladen und Zaubern
    // =====================================================================================

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity user) {
        return USE_DURATION;
    }

    @Override
    public ItemUseAnimation getUseAnimation(ItemStack stack) {
        return ItemUseAnimation.TRIDENT;
    }

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand) {
        ItemStack staff = player.getItemInHand(hand);
        player.startUsingItem(hand);
        if (level instanceof ServerLevel server) {
            Spell spell = KnownSpells.of(staff).selected();
            SpellFxEntity old = CIRCLES.remove(player.getUUID());
            if (old != null) {
                old.discard();
            }
            SpellFxEntity circle = SpellFxEntity.create(server, SpellFxEntity.Kind.CAST_CIRCLE, player.position(), spell.element(), spell.color(),
                    USE_DURATION, 1.6f).anchor(player).onTick(fx -> {
                        if (!player.isUsingItem() || !(player.getUseItem().getItem() instanceof AetheritStaffItem) || player.isRemoved()) {
                            CIRCLES.remove(player.getUUID(), fx);
                            fx.discard();
                        }
                    });
            circle.spawn();
            CIRCLES.put(player.getUUID(), circle);
            server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, 0.6f,
                    1.6f);
        }
        return InteractionResult.CONSUME;
    }

    /** Ladung 0–1 nach so vielen Ticks Halten. */
    public static float charge(ItemStack staff, int heldTicks) {
        return Mth.clamp((heldTicks - 2) / (float) StaffMastery.rank(staff).chargeTicks(), 0.0f, 1.0f);
    }

    @Override
    public void onUseTick(Level level, LivingEntity entity, ItemStack staff, int remaining) {
        int held = USE_DURATION - remaining;
        float charge = charge(staff, held);
        if (level instanceof ServerLevel server) {
            SpellFxEntity circle = CIRCLES.get(entity.getUUID());
            if (circle != null && !circle.isRemoved()) {
                circle.setParam(charge);
            }
            if (charge >= 1.0f && charge(staff, held - 1) < 1.0f) {
                server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.4f);
                server.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BEACON_POWER_SELECT, SoundSource.PLAYERS, 0.5f, 1.8f);
            }
        } else if (entity.getRandom().nextFloat() < 0.3f + charge * 0.5f) {
            // Funken ziehen zur Hand
            Spell spell = KnownSpells.of(staff).selected();
            double a = entity.getRandom().nextDouble() * Mth.TWO_PI;
            double r = 0.8 + entity.getRandom().nextDouble() * 0.5;
            net.minecraft.world.phys.Vec3 hand = entity.getEyePosition().add(entity.getLookAngle().scale(0.9)).add(0, -0.4, 0);
            level.addParticle(new net.minecraft.core.particles.DustParticleOptions(spell.color(), 0.6f + charge * 0.5f),
                    hand.x + Math.cos(a) * r, hand.y + (entity.getRandom().nextDouble() - 0.3) * r, hand.z + Math.sin(a) * r,
                    0.0, 0.0, 0.0);
        }
    }

    @Override
    public boolean releaseUsing(ItemStack staff, Level level, LivingEntity entity, int remaining) {
        if (level instanceof ServerLevel server) {
            SpellFxEntity circle = CIRCLES.remove(entity.getUUID());
            if (circle != null) {
                circle.discard();
            }
        }
        if (!(entity instanceof Player player)) {
            return false;
        }
        return cast(level, player, staff, charge(staff, USE_DURATION - remaining));
    }

    @Override
    public void onStopUsing(ItemStack stack, LivingEntity entity, int count) {
        SpellFxEntity circle = CIRCLES.remove(entity.getUUID());
        if (circle != null) {
            circle.discard();
        }
    }

    /**
     * Wirkt den gewählten Zauber des Stabs mit der Ladung 0–1 (auf dem Server; auf dem Bildschirm nur, ob er gewirkt
     * werden kann). Setzt die Abklingzeit und bringt dem Stab Erfahrung.
     */
    public static boolean cast(Level level, Player player, ItemStack staff, float charge) {
        KnownSpells spells = KnownSpells.of(staff);
        Spell spell = spells.selected();
        int spellLevel = spells.selectedLevel();
        if (player.getCooldowns().isOnCooldown(staff) || !spell.canCast(level, player, spellLevel)) {
            return false;
        }
        int cost = Mana.cost(spell, spellLevel, charge);
        if (!Mana.has(player, cost)) {
            if (level instanceof ServerLevel server) {
                server.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.PLAYERS, 0.6f, 1.8f);
                server.sendParticles(ParticleTypes.SMOKE, player.getX(), player.getY(1.0), player.getZ(), 6, 0.3, 0.3, 0.3, 0.01);
                player.sendOverlayMessage(Component.translatable("message.kingdomsatwar.mana.empty", cost, Math.round(Mana.get(player)))
                        .withColor(0x6FA8FF));
            }
            return false;
        }
        if (level instanceof ServerLevel server) {
            if (!spell.cast(server, player, spellLevel, StaffMastery.power(staff, charge))) {
                return false;
            }
            Mana.spend(player, cost);
            player.awardStat(Stats.ITEM_USED.get(staff.getItem()));
            if (player instanceof ServerPlayer serverPlayer) {
                StaffMastery.award(serverPlayer, staff, charge >= 1.0f ? 2 : 1);
            }
        }
        int ticks = Math.max(1, Math.round(spell.cooldown(spellLevel) * StaffMastery.rank(staff).cooldown()));
        player.getCooldowns().addCooldown(staff, ticks);
        return true;
    }

    // =====================================================================================
    // Unzerstörbar
    // =====================================================================================

    /** Als Gegenstand am Boden vergeht er nicht. */
    @Override
    public boolean onEntityItemUpdate(ItemStack stack, ItemEntity entity) {
        if (entity.getAge() >= 0) {
            entity.setUnlimitedLifetime();
        }
        return false;
    }

    /** Weder Feuer noch Lava, Explosionen oder Kakteen können ihm etwas anhaben. */
    @Override
    public boolean canBeHurtBy(ItemStack stack, net.minecraft.world.damagesource.DamageSource source) {
        return source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY);
    }

    @Override
    public void inventoryTick(ItemStack stack, ServerLevel level, Entity owner, @Nullable EquipmentSlot slot) {
        syncCooldown(stack);
    }

    // =====================================================================================
    // Anzeige
    // =====================================================================================

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay display, Consumer<Component> builder,
            TooltipFlag flag) {
        StaffMastery.Rank rank = StaffMastery.rank(stack);
        StaffMastery.Rank next = rank.next();
        Component rankLine = Component.translatable("tooltip.kingdomsatwar.staff.level", rank.level(),
                rank.displayName().withColor(rank == StaffMastery.Rank.AETHERLORD ? 0xFFD86A : Spell.AETHER_COLOR),
                Math.round(rank.power() * 100)).withStyle(ChatFormatting.GRAY);
        builder.accept(rankLine);
        if (next != null) {
            builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.progress", StaffMastery.xp(stack), next.xp(),
                    next.level(), next.displayName()).withStyle(ChatFormatting.DARK_GRAY));
        } else {
            builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.mastered", StaffMastery.xp(stack)).withStyle(ChatFormatting.GOLD));
        }
        KnownSpells spells = KnownSpells.of(stack);
        builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.mana", Math.round(Mana.max(stack)),
                Mana.cost(spells.selected(), spells.selectedLevel(), 0.0f), spells.selected().displayName()).withColor(0x6FA8FF));
        builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.spells").withStyle(ChatFormatting.GRAY));
        for (Spell spell : spells.known()) {
            boolean selected = spell == spells.selected();
            builder.accept(Component.literal(selected ? " ▶ " : "    ").withStyle(ChatFormatting.LIGHT_PURPLE)
                    .append(spell.displayName().withColor(selected ? spell.color() : 0xB8B0C8))
                    .append(Component.literal(" " + Spell.numeral(spells.level(spell))).withStyle(ChatFormatting.DARK_GRAY)));
        }
        builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.charge").withStyle(ChatFormatting.DARK_PURPLE));
        builder.accept(Component.translatable("tooltip.kingdomsatwar.staff.wheel",
                Component.keybind("key.kingdomsatwar.spell_wheel").withStyle(ChatFormatting.WHITE)).withStyle(ChatFormatting.DARK_PURPLE));
    }
}
