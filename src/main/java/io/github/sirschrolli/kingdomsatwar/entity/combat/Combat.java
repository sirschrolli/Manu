package io.github.sirschrolli.kingdomsatwar.entity.combat;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.registry.ModAttachments;
import io.github.sirschrolli.kingdomsatwar.rules.Hardness;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import net.minecraft.core.Holder;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDamageEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import org.jspecify.annotations.Nullable;

/**
 * Kampfstile der Monster (seit 1.14): Jedes Monster der Mod bekommt beim ersten Erscheinen ein Temperament und eine
 * zufällige Auswahl der besonderen Angriffe seiner Familie (siehe {@link Family}, {@link Style}) – kein Kampf gleicht
 * dem anderen, auch nicht gegen dieselbe Art. Hier hängen die Kämpfe an den Monstern, und hier landen die Treffer, die
 * eine Deckung abfängt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID)
public final class Combat {
    static final Identifier TEMPER_SPEED = KingdomsAtWar.id("temper_speed");
    static final Identifier TEMPER_ARMOR = KingdomsAtWar.id("temper_armor");
    static final Identifier TEMPER_STEADY = KingdomsAtWar.id("temper_steady");
    static final Identifier TEMPER_HEALTH = KingdomsAtWar.id("temper_health");
    static final Identifier TEMPER_DAMAGE = KingdomsAtWar.id("temper_damage");
    /** So viel fängt die Deckung von vorn ab (Anteil). */
    public static final float GUARD_BLOCK = 0.8f;
    /** Bis zu diesem Winkel (Grad zur Blickrichtung) gilt ein Treffer als „von vorn“. */
    public static final double GUARD_ANGLE = 75.0;

    private static final Map<Mob, Fight> FIGHTS = new WeakHashMap<>();

    private Combat() {}

    // =====================================================================================
    // Öffentlich (auch für Tests und Vorführungen)
    // =====================================================================================

    /** Der Kampf eines Monsters (null: es hat keinen Kampfstil). */
    public static @Nullable Fight fight(Mob mob) {
        return FIGHTS.get(mob);
    }

    /** Sein Kampfstil (oder null). */
    public static @Nullable Style style(Mob mob) {
        Fight fight = FIGHTS.get(mob);
        return fight == null ? null : fight.style();
    }

    /** Was es gerade tut (oder null). */
    public static @Nullable MoveKind current(Mob mob) {
        Fight fight = FIGHTS.get(mob);
        return fight == null ? null : fight.current();
    }

    /** Läuft gerade die Ausführung (nach der Vorwarnung)? */
    public static boolean acting(Mob mob) {
        Fight fight = FIGHTS.get(mob);
        return fight != null && fight.acting();
    }

    /**
     * Setzt einen Kampfstil (Tests, Vorführungen). Das Temperament wirkt dann ebenso, als wäre es ausgewürfelt worden.
     */
    public static void setStyle(Mob mob, Style style) {
        Fight fight = FIGHTS.get(mob);
        if (fight == null) {
            return;
        }
        fight.dispose();
        clearTemper(mob);
        mob.setData(ModAttachments.COMBAT_STYLE.get(), style);
        applyTemper(mob, style);
        fight.setStyle(style);
        fight.frenzy(false);
    }

    /** Erzwingt einen Angriff gegen dieses Ziel (Tests, Vorführungen) – ohne Rücksicht auf Abstand und Wartezeit. */
    public static boolean force(Mob mob, MoveKind kind, LivingEntity target) {
        Fight fight = FIGHTS.get(mob);
        if (fight == null) {
            return false;
        }
        fight.dispose();
        fight.clearCooldowns();
        fight.forced = true;
        fight.start(kind, target);
        return true;
    }

    /** Hält alle besonderen Angriffe an (bis {@code ticks} vorbei sind). */
    public static void pause(Mob mob, int ticks) {
        Fight fight = FIGHTS.get(mob);
        if (fight != null) {
            fight.dispose();
            fight.global = Math.max(fight.global, ticks);
        }
    }

    /** Raserei (Tests). */
    public static boolean frenzied(Mob mob) {
        Fight fight = FIGHTS.get(mob);
        return fight != null && fight.frenzied;
    }

    // =====================================================================================
    // An die Monster hängen
    // =====================================================================================

    @SubscribeEvent
    static void onJoin(EntityJoinLevelEvent event) {
        if (event.getLevel() instanceof ServerLevel && event.getEntity() instanceof Mob mob) {
            attach(mob);
        }
    }

    @SubscribeEvent
    static void onLeave(EntityLeaveLevelEvent event) {
        if (event.getEntity() instanceof Mob mob && !event.getLevel().isClientSide()) {
            Fight fight = FIGHTS.remove(mob);
            if (fight != null) {
                fight.dispose();
            }
        }
    }

    /** Gibt einem Monster seinen Kampf (beim ersten Mal würfelt es seinen Kampfstil aus). */
    static @Nullable Fight attach(Mob mob) {
        Fight present = FIGHTS.get(mob);
        if (present != null) {
            return present;
        }
        Family family = Family.of(mob);
        if (family == null || !Hardness.isEnemy(mob) || !Family.big(mob)) {
            return null;
        }
        Style style;
        if (mob.hasData(ModAttachments.COMBAT_STYLE.get())) {
            style = mob.getData(ModAttachments.COMBAT_STYLE.get());
        } else {
            style = Style.roll(mob.getRandom(), family, Family.ranged(mob, family));
            mob.setData(ModAttachments.COMBAT_STYLE.get(), style);
            applyTemper(mob, style);
        }
        Fight fight = new Fight(mob, family, style);
        FIGHTS.put(mob, fight);
        for (WrappedGoal wrapped : List.copyOf(mob.goalSelector.getAvailableGoals())) {
            if (wrapped.getGoal() instanceof CombatGoal || wrapped.getGoal() instanceof Ticker) {
                mob.goalSelector.removeGoal(wrapped.getGoal());
            }
        }
        mob.goalSelector.addGoal(0, new CombatGoal(fight));
        mob.goalSelector.addGoal(0, new Ticker(fight));
        return fight;
    }

    /** Was das Temperament am Körper ändert (einmalig beim Auswürfeln; bleibt gespeichert). */
    static void applyTemper(Mob mob, Style style) {
        switch (style.temper()) {
            case UNGESTUEM -> permanent(mob, Attributes.MOVEMENT_SPEED, TEMPER_SPEED, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            case LAUERND -> permanent(mob, Attributes.MOVEMENT_SPEED, TEMPER_SPEED, 0.06, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
            case ZAEH -> {
                permanent(mob, Attributes.MOVEMENT_SPEED, TEMPER_SPEED, -0.08, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                permanent(mob, Attributes.ARMOR, TEMPER_ARMOR, 4.0, AttributeModifier.Operation.ADD_VALUE);
                permanent(mob, Attributes.KNOCKBACK_RESISTANCE, TEMPER_STEADY, 0.4, AttributeModifier.Operation.ADD_VALUE);
            }
            case ANFUEHRER -> {
                permanent(mob, Attributes.MAX_HEALTH, TEMPER_HEALTH, 0.3, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                permanent(mob, Attributes.ATTACK_DAMAGE, TEMPER_DAMAGE, 0.15, AttributeModifier.Operation.ADD_MULTIPLIED_BASE);
                mob.setHealth(mob.getMaxHealth());
            }
            case RASEND -> {
            }
        }
    }

    private static void clearTemper(Mob mob) {
        List<Map.Entry<Holder<Attribute>, Identifier>> all = List.of(
                Map.entry(Attributes.MOVEMENT_SPEED, TEMPER_SPEED), Map.entry(Attributes.ARMOR, TEMPER_ARMOR),
                Map.entry(Attributes.KNOCKBACK_RESISTANCE, TEMPER_STEADY), Map.entry(Attributes.MAX_HEALTH, TEMPER_HEALTH),
                Map.entry(Attributes.ATTACK_DAMAGE, TEMPER_DAMAGE));
        for (Map.Entry<Holder<Attribute>, Identifier> pair : all) {
            AttributeInstance instance = mob.getAttribute(pair.getKey());
            if (instance != null) {
                instance.removeModifier(pair.getValue());
            }
        }
        mob.setHealth(Math.min(mob.getHealth(), mob.getMaxHealth()));
    }

    private static void permanent(Mob mob, Holder<Attribute> attribute, Identifier id, double amount, AttributeModifier.Operation operation) {
        AttributeInstance instance = mob.getAttribute(attribute);
        if (instance != null) {
            instance.addOrReplacePermanentModifier(new AttributeModifier(id, amount, operation));
        }
    }

    // =====================================================================================
    // Treffer
    // =====================================================================================

    /** Die Deckung der Zähen: von vorn fängt sie das meiste ab (dann schlagen sie zurück), von hinten nichts. */
    @SubscribeEvent(priority = EventPriority.LOW)
    static void onIncomingDamage(LivingIncomingDamageEvent event) {
        if (!(event.getEntity() instanceof Mob mob) || !(mob.level() instanceof ServerLevel level)) {
            return;
        }
        Fight fight = FIGHTS.get(mob);
        if (fight == null || fight.current() != MoveKind.GUARD || !fight.acting()) {
            return;
        }
        DamageSource source = event.getSource();
        Vec3 from = source.getSourcePosition();
        if (from == null || source.is(DamageTypeTags.BYPASSES_ARMOR) && !source.is(DamageTypeTags.IS_PROJECTILE)) {
            return;
        }
        Vec3 to = from.subtract(mob.position()).multiply(1.0, 0.0, 1.0);
        if (to.lengthSqr() < 1.0e-4) {
            return;
        }
        Vec3 facing = Vec3.directionFromRotation(0.0f, mob.getYRot());
        double angle = Math.toDegrees(Math.acos(Mth.clamp(to.normalize().dot(facing), -1.0, 1.0)));
        if (angle > GUARD_ANGLE) {
            // von der Seite oder von hinten: die Deckung bricht
            fight.n = -1;
            return;
        }
        event.setAmount(event.getAmount() * (1.0f - GUARD_BLOCK));
        fight.parried = true;
        level.playSound(null, mob.getX(), mob.getY(), mob.getZ(), SoundEvents.SHIELD_BLOCK.value(), mob.getSoundSource(), 1.0f,
                0.8f + mob.getRandom().nextFloat() * 0.3f);
        Vec3 spark = mob.position().add(facing.scale(mob.getBbWidth() * 0.6)).add(0.0, mob.getBbHeight() * 0.6, 0.0);
        level.sendParticles(ParticleTypes.CRIT, spark.x, spark.y, spark.z, 10, 0.15, 0.2, 0.15, 0.25);
        level.sendParticles(ParticleTypes.ENCHANTED_HIT, spark.x, spark.y, spark.z, 6, 0.2, 0.2, 0.2, 0.1);
    }

    /** Wer getroffen hat und wer getroffen wurde – für die Lauernden (zuschlagen und zurück) und das Losreißen. */
    @SubscribeEvent
    static void onDamage(LivingDamageEvent.Post event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide()) {
            return;
        }
        if (victim instanceof Mob mob) {
            Fight fight = FIGHTS.get(mob);
            if (fight != null) {
                fight.taken += event.getHealthDamage();
            }
        }
        if (event.getSource().getEntity() instanceof Mob attacker && event.getSource().getDirectEntity() == attacker) {
            Fight fight = FIGHTS.get(attacker);
            if (fight != null) {
                fight.afterHit(victim);
            }
        }
    }

    // =====================================================================================

    /** Treibt den Kampf jeden Tick an (läuft immer mit, sperrt nichts). */
    static final class Ticker extends Goal {
        private final Fight fight;

        Ticker(Fight fight) {
            this.fight = fight;
            this.setFlags(EnumSet.noneOf(Goal.Flag.class));
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public boolean canContinueToUse() {
            return true;
        }

        @Override
        public boolean isInterruptable() {
            return false;
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public void tick() {
            this.fight.tick();
        }
    }
}
