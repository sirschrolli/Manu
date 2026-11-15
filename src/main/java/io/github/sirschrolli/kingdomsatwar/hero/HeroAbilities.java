package io.github.sirschrolli.kingdomsatwar.hero;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingFallEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Die aktiven Fähigkeiten des Helden auf dem Server (seit 1.11): welche er gewählt hat und wann jede wieder bereit ist
 * ({@link State}, bleibt nach dem Tod und wird nur ihm selbst geschickt), das Einsetzen (der Server prüft Stufe und
 * Abklingzeit) und was jede bewirkt – dazu die Landung nach dem Himmelssturz und das Letzte Gefecht.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class HeroAbilities {
    /** Sturmlauf, Eiserne Haut: so lange wirken sie (Ticks). */
    public static final int SPRINT_TICKS = 120;
    public static final int IRON_SKIN_TICKS = 120;
    /** Kampfschrei: Reichweite gegen Monster und für die eigenen Leute, wie lange es wirkt. */
    public static final double WAR_CRY_RADIUS = 8.0;
    public static final double WAR_CRY_ALLIES = 16.0;
    public static final int WAR_CRY_TICKS = 120;
    /** Zweiter Atem: dieser Anteil der Lebenspunkte. */
    public static final float SECOND_WIND_HEAL = 0.3f;
    /** Wirbelschlag: Reichweite und Faktor auf den Waffenschaden. */
    public static final double WHIRLWIND_RADIUS = 4.0;
    public static final float WHIRLWIND_FACTOR = 1.5f;
    /** Adlerblick: Reichweite und Dauer. */
    public static final double EAGLE_EYE_RADIUS = 48.0;
    public static final int EAGLE_EYE_TICKS = 240;
    /** Himmelssturz: Schockwelle beim Landen (Reichweite, Grundschaden, Anteil des Waffenschadens). */
    public static final double SKYFALL_RADIUS = 5.0;
    public static final float SKYFALL_DAMAGE = 6.0f;
    public static final float SKYFALL_FACTOR = 0.5f;
    /** Letztes Gefecht: so lange, und so lange danach erschöpft. */
    public static final int LAST_STAND_TICKS = 160;
    public static final int EXHAUSTED_TICKS = 120;

    /** Die gewählte Fähigkeit und wann jede wieder bereit ist (Spielzeit; in der Reihenfolge von {@link HeroAbility}). */
    public record State(int selected, List<Long> readyAt) {
        public static final State NONE = new State(0, List.of());
        public static final Codec<State> CODEC = RecordCodecBuilder.create(i -> i.group(
                Codec.INT.optionalFieldOf("selected", 0).forGetter(State::selected),
                Codec.LONG.listOf().optionalFieldOf("ready_at", List.of()).forGetter(State::readyAt)).apply(i, State::new));
        public static final StreamCodec<ByteBuf, State> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, State::selected,
                ByteBufCodecs.VAR_LONG.apply(ByteBufCodecs.list(HeroAbility.values().length)), State::readyAt,
                State::new);

        public State {
            selected = Math.floorMod(selected, HeroAbility.values().length);
            List<Long> fixed = new ArrayList<>(readyAt);
            while (fixed.size() < HeroAbility.values().length) {
                fixed.add(0L);
            }
            readyAt = List.copyOf(fixed.subList(0, HeroAbility.values().length));
        }

        public HeroAbility selectedAbility() {
            return HeroAbility.byIndex(this.selected);
        }

        public long readyAt(HeroAbility ability) {
            return this.readyAt.get(ability.ordinal());
        }

        State select(HeroAbility ability) {
            return new State(ability.ordinal(), this.readyAt);
        }

        State used(HeroAbility ability, long readyAt) {
            List<Long> list = new ArrayList<>(this.readyAt);
            list.set(ability.ordinal(), readyAt);
            return new State(this.selected, list);
        }
    }

    /** Wer gerade im Himmelssturz ist (seit wann), und wessen Letztes Gefecht wann endet – nur auf dem Server. */
    private static final Map<UUID, Long> SKYFALL = new HashMap<>();
    private static final Map<UUID, Long> LAST_STAND = new HashMap<>();

    private HeroAbilities() {}

    public static State state(Player player) {
        return player.getData(ModAttachments.HERO_ABILITIES);
    }

    /** Wie viele Ticks die Fähigkeit noch ruht (0: bereit). */
    public static long cooldown(Player player, HeroAbility ability) {
        return Math.max(0L, state(player).readyAt(ability) - player.level().getGameTime());
    }

    /** Wählt die Fähigkeit für die Taste (nur freigeschaltete). */
    public static boolean select(ServerPlayer player, HeroAbility ability) {
        if (!ability.unlocked(HeroLevel.of(player).level())) {
            return false;
        }
        player.setData(ModAttachments.HERO_ABILITIES, state(player).select(ability));
        return true;
    }

    /** Setzt die Fähigkeit ein, wenn sie freigeschaltet und bereit ist. */
    public static boolean use(ServerPlayer player, HeroAbility ability) {
        if (player.isSpectator() || !player.isAlive()) {
            return false;
        }
        int level = HeroLevel.of(player).level();
        if (!ability.unlocked(level)) {
            player.sendOverlayMessage(Component.translatable("hero.kingdomsatwar.ability.locked", Component.translatable(ability.translationKey()),
                    ability.level()).withStyle(ChatFormatting.GRAY));
            return false;
        }
        long left = cooldown(player, ability);
        if (left > 0L) {
            player.sendOverlayMessage(Component.translatable("hero.kingdomsatwar.ability.resting", Component.translatable(ability.translationKey()),
                    (left + 19L) / 20L).withStyle(ChatFormatting.GRAY));
            return false;
        }
        perform(player, ability);
        player.setData(ModAttachments.HERO_ABILITIES, state(player).used(ability, player.level().getGameTime() + ability.cooldownTicks()));
        return true;
    }

    /** Für Tests und Befehle: alle Fähigkeiten sofort wieder bereit. */
    public static void refresh(ServerPlayer player) {
        player.setData(ModAttachments.HERO_ABILITIES, new State(state(player).selected(), List.of()));
    }

    // ------------------------------------------------------------------------------------------------ Wirkungen

    private static void perform(ServerPlayer player, HeroAbility ability) {
        ServerLevel level = player.level();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        DustParticleOptions dust = new DustParticleOptions(ability.color() & 0xFFFFFF, 1.2f);
        switch (ability) {
            case SPRINT -> {
                player.addEffect(new MobEffectInstance(MobEffects.SPEED, SPRINT_TICKS, 1, false, true, true));
                player.addEffect(new MobEffectInstance(MobEffects.JUMP_BOOST, SPRINT_TICKS, 0, false, true, true));
                level.sendParticles(ParticleTypes.GUST, x, y + 0.6, z, 1, 0.0, 0.0, 0.0, 0.0);
                level.sendParticles(ParticleTypes.CLOUD, x, y + 0.2, z, 18, 0.5, 0.1, 0.5, 0.06);
                level.playSound(null, x, y, z, SoundEvents.BREEZE_WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.8f, 1.4f);
            }
            case WAR_CRY -> {
                for (LivingEntity foe : foes(player, WAR_CRY_RADIUS)) {
                    foe.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, WAR_CRY_TICKS, 0), player);
                    foe.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, WAR_CRY_TICKS, 0), player);
                    level.sendParticles(ParticleTypes.ANGRY_VILLAGER, foe.getX(), foe.getEyeY() + 0.4, foe.getZ(), 2, 0.2, 0.1, 0.2, 0.0);
                }
                for (LivingEntity ally : level.getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(WAR_CRY_ALLIES),
                        e -> e instanceof OwnableEntity own && player.equals(own.getOwner()) && e.isAlive())) {
                    ally.addEffect(new MobEffectInstance(MobEffects.STRENGTH, 200, 0), player);
                    level.sendParticles(dust, ally.getX(), ally.getY(1.0), ally.getZ(), 6, 0.3, 0.2, 0.3, 0.0);
                }
                ring(level, x, y + 0.15, z, WAR_CRY_RADIUS * 0.6, dust, 40);
                level.playSound(null, x, y, z, SoundEvents.RAID_HORN, SoundSource.PLAYERS, 1.6f, 1.25f);
            }
            case IRON_SKIN -> {
                player.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, IRON_SKIN_TICKS, 1, false, true, true));
                level.sendParticles(ParticleTypes.WAX_ON, x, y + 1.0, z, 24, 0.4, 0.6, 0.4, 0.0);
                level.sendParticles(dust, x, y + 1.0, z, 16, 0.4, 0.6, 0.4, 0.0);
                level.playSound(null, x, y, z, SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.5f, 1.6f);
            }
            case SECOND_WIND -> {
                player.heal(player.getMaxHealth() * SECOND_WIND_HEAL);
                for (MobEffectInstance effect : new ArrayList<>(player.getActiveEffects())) {
                    if (effect.getEffect().value().getCategory() == MobEffectCategory.HARMFUL) {
                        player.removeEffect(effect.getEffect());
                    }
                }
                level.sendParticles(ParticleTypes.HEART, x, y + 1.6, z, 6, 0.5, 0.4, 0.5, 0.0);
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, x, y + 1.0, z, 14, 0.5, 0.6, 0.5, 0.0);
                level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 0.9f);
                level.playSound(null, x, y, z, SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0f, 1.0f);
            }
            case WHIRLWIND -> {
                float damage = (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * WHIRLWIND_FACTOR;
                for (LivingEntity foe : foes(player, WHIRLWIND_RADIUS)) {
                    if (foe.hurtServer(level, player.damageSources().playerAttack(player), damage)) {
                        Vec3 away = foe.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
                        away = away.lengthSqr() < 1.0e-4 ? new Vec3(1.0, 0.0, 0.0) : away.normalize();
                        foe.push(away.x * 0.8, 0.3, away.z * 0.8);
                    }
                }
                for (int i = 0; i < 12; i++) {
                    double a = i * Mth.TWO_PI / 12.0;
                    level.sendParticles(ParticleTypes.SWEEP_ATTACK, x + Math.cos(a) * 2.2, y + 1.0, z + Math.sin(a) * 2.2, 1, 0.0, 0.0, 0.0, 0.0);
                }
                level.sendParticles(ParticleTypes.SMALL_GUST, x, y + 0.8, z, 6, 1.2, 0.3, 1.2, 0.0);
                level.playSound(null, x, y, z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.2f, 0.8f);
                level.playSound(null, x, y, z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 0.6f, 1.2f);
            }
            case EAGLE_EYE -> {
                for (LivingEntity foe : foes(player, EAGLE_EYE_RADIUS)) {
                    foe.addEffect(new MobEffectInstance(MobEffects.GLOWING, EAGLE_EYE_TICKS, 0), player);
                }
                level.sendParticles(dust, x, player.getEyeY(), z, 20, 0.6, 0.3, 0.6, 0.0);
                level.playSound(null, x, y, z, SoundEvents.SPYGLASS_USE, SoundSource.PLAYERS, 1.2f, 0.8f);
                level.playSound(null, x, y, z, SoundEvents.PHANTOM_FLAP, SoundSource.PLAYERS, 0.8f, 1.4f);
            }
            case SKYFALL -> {
                Vec3 look = player.getViewVector(1.0f).multiply(1.0, 0.0, 1.0);
                look = look.lengthSqr() < 1.0e-4 ? Vec3.ZERO : look.normalize();
                player.setDeltaMovement(look.x * 1.3, 1.05, look.z * 1.3);
                player.needsSync = true;
                player.resetFallDistance();
                SKYFALL.put(player.getUUID(), level.getGameTime());
                level.sendParticles(ParticleTypes.GUST, x, y + 0.2, z, 1, 0.0, 0.0, 0.0, 0.0);
                level.sendParticles(ParticleTypes.CLOUD, x, y + 0.1, z, 20, 0.6, 0.05, 0.6, 0.04);
                level.playSound(null, x, y, z, SoundEvents.WIND_CHARGE_BURST, SoundSource.PLAYERS, 1.0f, 0.8f);
            }
            case LAST_STAND -> {
                LAST_STAND.put(player.getUUID(), level.getGameTime() + LAST_STAND_TICKS);
                level.sendParticles(dust, x, y + 1.0, z, 30, 0.5, 0.8, 0.5, 0.0);
                level.sendParticles(ParticleTypes.FLAME, x, y + 0.2, z, 24, 0.6, 0.1, 0.6, 0.02);
                level.playSound(null, x, y, z, SoundEvents.ENDER_DRAGON_GROWL, SoundSource.PLAYERS, 0.5f, 1.5f);
                level.playSound(null, x, y, z, SoundEvents.TOTEM_USE, SoundSource.PLAYERS, 0.4f, 0.7f);
            }
        }
    }

    /**
     * Gegen wen eine Fähigkeit geht: Monster (und wer den Helden gerade angreift) im Umkreis – nicht seine eigenen Soldaten
     * und Tiere, keine Rüstungsständer, keine Spieler.
     */
    static List<LivingEntity> foes(ServerPlayer player, double radius) {
        return player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius), e -> e != player && e.isAlive()
                && !(e instanceof Player) && !(e instanceof ArmorStand) && !(e instanceof OwnableEntity own && player.equals(own.getOwner()))
                && (e instanceof Enemy || e instanceof Mob mob && player.equals(mob.getTarget()))
                && e.distanceToSqr(player) <= radius * radius);
    }

    private static void ring(ServerLevel level, double x, double y, double z, double radius, DustParticleOptions dust, int count) {
        for (int i = 0; i < count; i++) {
            double a = i * Mth.TWO_PI / count;
            level.sendParticles(dust, x + Math.cos(a) * radius, y, z + Math.sin(a) * radius, 1, 0.0, 0.0, 0.0, 0.0);
        }
    }

    // ------------------------------------------------------------------------------------------------ Himmelssturz

    /** Ist der Held gerade im Himmelssturz? */
    public static boolean falling(Player player) {
        return SKYFALL.containsKey(player.getUUID());
    }

    /** Die Landung nach dem Himmelssturz: eine Schockwelle, die Feinde ringsum trifft und hochwirft. */
    public static void land(ServerPlayer player) {
        if (SKYFALL.remove(player.getUUID()) == null) {
            return;
        }
        ServerLevel level = player.level();
        double x = player.getX();
        double y = player.getY();
        double z = player.getZ();
        float damage = SKYFALL_DAMAGE + (float) player.getAttributeValue(Attributes.ATTACK_DAMAGE) * SKYFALL_FACTOR;
        for (LivingEntity foe : foes(player, SKYFALL_RADIUS)) {
            if (foe.hurtServer(level, player.damageSources().playerAttack(player), damage)) {
                Vec3 away = foe.position().subtract(player.position()).multiply(1.0, 0.0, 1.0);
                away = away.lengthSqr() < 1.0e-4 ? Vec3.ZERO : away.normalize();
                foe.push(away.x * 0.6, 0.7, away.z * 0.6);
            }
        }
        level.sendParticles(ParticleTypes.EXPLOSION, x, y + 0.2, z, 3, 1.0, 0.1, 1.0, 0.0);
        ring(level, x, y + 0.15, z, 2.0, new DustParticleOptions(HeroAbility.SKYFALL.color() & 0xFFFFFF, 1.6f), 32);
        ring(level, x, y + 0.15, z, 3.8, new DustParticleOptions(0xFFFFFF, 1.2f), 40);
        level.sendParticles(ParticleTypes.CLOUD, x, y + 0.1, z, 30, 2.0, 0.05, 2.0, 0.05);
        level.playSound(null, x, y, z, SoundEvents.MACE_SMASH_GROUND_HEAVY, SoundSource.PLAYERS, 1.4f, 0.8f);
    }

    @SubscribeEvent
    static void onFall(LivingFallEvent event) {
        if (event.getEntity() instanceof Player player && falling(player)) {
            event.setDamageMultiplier(0.0f);
        }
    }

    // ------------------------------------------------------------------------------------------------ Letztes Gefecht

    /** Hält das Letzte Gefecht gerade? */
    public static boolean standing(Player player) {
        Long until = LAST_STAND.get(player.getUUID());
        return until != null && player.level().getGameTime() < until;
    }

    /** Im Letzten Gefecht sinkt niemand unter ein halbes Herz (gegen /kill und die Leere hilft es nicht). */
    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Pre event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !standing(player) || event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            return;
        }
        float room = player.getHealth() - 1.0f;
        if (event.getNewDamage() > room) {
            event.setNewDamage(Math.max(0.0f, room));
        }
    }

    @SubscribeEvent
    static void onTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) {
            return;
        }
        long now = player.level().getGameTime();
        Long since = SKYFALL.get(player.getUUID());
        if (since != null) {
            if (now - since > 4 && (player.onGround() || player.isInWater())) {
                land(player);
            } else if (now - since > 200) {
                SKYFALL.remove(player.getUUID());
            } else if (now % 2 == 0) {
                player.level().sendParticles(ParticleTypes.CLOUD, player.getX(), player.getY() + 0.3, player.getZ(), 1, 0.1, 0.1, 0.1, 0.0);
            }
        }
        Long until = LAST_STAND.get(player.getUUID());
        if (until != null) {
            if (now >= until) {
                LAST_STAND.remove(player.getUUID());
                player.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, EXHAUSTED_TICKS, 1));
                player.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EXHAUSTED_TICKS, 0));
                player.sendOverlayMessage(Component.translatable("hero.kingdomsatwar.ability.last_stand.over").withStyle(ChatFormatting.GRAY));
            } else if (now % 4 == 0) {
                player.level().sendParticles(new DustParticleOptions(HeroAbility.LAST_STAND.color() & 0xFFFFFF, 1.0f), player.getX(),
                        player.getY() + 1.0, player.getZ(), 3, 0.35, 0.6, 0.35, 0.0);
            }
        }
    }

    @SubscribeEvent
    static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SKYFALL.remove(event.getEntity().getUUID());
        LAST_STAND.remove(event.getEntity().getUUID());
    }
}
