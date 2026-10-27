package io.github.sirschrolli.kingdomsatwar.magic;

import com.mojang.serialization.Codec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.ElementalBoss;
import io.github.sirschrolli.kingdomsatwar.entity.boss.lightning.LightningAttacks;
import io.netty.buffer.ByteBuf;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.function.Predicate;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.entity.projectile.hurtingprojectile.SmallFireball;
import net.minecraft.world.item.BoneMealItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Die Zauber des Aetherit-Stabs. Den Aether-Blitz kennt jeder Stab; die Zauber der sechs Elemente – derselben, aus denen
 * die Drachen geboren sind – lernt er am Magietisch aus den Zaubertafeln der Erzmagier. Jeder Zauber hat drei Stufen:
 * mit jeder Stufe wird er stärker, reicht weiter, ist schneller wieder bereit und bekommt eine neue Wirkung dazu.
 *
 * <p>Jeder Zauber kostet Haltbarkeit des Stabs und hat eine eigene Abklingzeit (eigene Gruppe), man kann also zwischen
 * den Zaubern wechseln, während einer abklingt. Zaubern können auch die Magier-Bosse – dann gelten ihre Gegner als Ziel.
 */
public enum Spell implements StringRepresentable {
    /** Aether-Blitz: trifft das anvisierte Wesen mit reiner Magie (Stufe III springt auf ein zweites Ziel über). */
    AETHER_BOLT("aether_bolt", null, 1, 15),
    /** Feuerball, wie ihn die Lohe schleudert (Stufe II drei Feuerbälle, Stufe III zerbersten sie in Flammen). */
    FIRE("fire", DragonType.FIRE, 1, 20),
    /** Frosthauch: verlangsamt, lässt erstarren und friert Wasser zu Eis (Stufe III friert Gegner fest). */
    ICE("ice", DragonType.ICE, 1, 30),
    /** Ruft einen Blitz an die anvisierte Stelle (Stufe II und III springen Blitze auf weitere Gegner über). */
    LIGHTNING("lightning", DragonType.LIGHTNING, 3, 80),
    /** Heilt einen selbst und seine Verbündeten und lässt Pflanzen sprießen (ab Stufe II Schutz, Stufe III Wurzeln). */
    NATURE("nature", DragonType.NATURE, 2, 160),
    /** Erdstoß: schleudert Feinde in der Nähe von sich (Stufe III mit Felsdornen, die taumeln lassen). */
    EARTH("earth", DragonType.EARTH, 2, 80),
    /** Woge: drängt alles zurück, löscht Feuer, lässt einen unter Wasser atmen (ab Stufe II heilend, Stufe III Wasserschild). */
    WATER("water", DragonType.WATER, 1, 60);

    public static final Codec<Spell> CODEC = StringRepresentable.fromEnum(Spell::values);
    public static final StreamCodec<ByteBuf, Spell> STREAM_CODEC =
            ByteBufCodecs.idMapper(i -> values()[Mth.clamp(i, 0, values().length - 1)], Enum::ordinal);
    /** Höchste Stufe eines Zaubers. */
    public static final int MAX_LEVEL = 3;
    /** Farbe des Aethers (der Zauber ohne Element). */
    public static final int AETHER_COLOR = 0xC79BFF;

    private static final float[] POWER = {1.0f, 1.35f, 1.7f};
    private static final float[] COOLDOWN = {1.0f, 0.85f, 0.7f};

    private final String id;
    private final @Nullable DragonType element;
    private final int cost;
    private final int cooldown;

    Spell(String id, @Nullable DragonType element, int cost, int cooldown) {
        this.id = id;
        this.element = element;
        this.cost = cost;
        this.cooldown = cooldown;
    }

    /** Zauber zum Element (oder der Aether-Blitz ohne Element). */
    public static Spell of(@Nullable DragonType element) {
        for (Spell spell : values()) {
            if (spell.element == element) {
                return spell;
            }
        }
        return AETHER_BOLT;
    }

    public String id() {
        return this.id;
    }

    @Override
    public String getSerializedName() {
        return this.id;
    }

    public @Nullable DragonType element() {
        return this.element;
    }

    /** Leitfarbe (RGB) für Schrift, Symbole und Partikel. */
    public int color() {
        return this.element != null ? this.element.color() : AETHER_COLOR;
    }

    public MutableComponent displayName() {
        return Component.translatable("spell." + KingdomsAtWar.MODID + "." + this.id);
    }

    public MutableComponent description() {
        return Component.translatable("spell." + KingdomsAtWar.MODID + "." + this.id + ".description");
    }

    /** Was die Stufe (2 oder 3) Neues bringt. */
    public MutableComponent levelDescription(int level) {
        return Component.translatable("spell." + KingdomsAtWar.MODID + "." + this.id + ".level" + Mth.clamp(level, 2, MAX_LEVEL));
    }

    /** Symbol in Oberflächen ({@code textures/gui/sprites/spell/<id>.png}). */
    public Identifier icon() {
        return KingdomsAtWar.id("spell/" + this.id);
    }

    /** Eigene Abklingzeit-Gruppe je Zauber (siehe {@link AetheritStaffItem#syncCooldown}). */
    public Identifier cooldownGroup() {
        return KingdomsAtWar.id("spell/" + this.id);
    }

    /** Haltbarkeit, die der Zauber den Stab kostet. */
    public int cost() {
        return this.cost;
    }

    /** Abklingzeit der ersten Stufe in Ticks. */
    public int cooldown() {
        return this.cooldown;
    }

    /** Abklingzeit auf einer Stufe in Ticks (höhere Stufen sind schneller wieder bereit). */
    public int cooldown(int level) {
        return Math.max(1, Math.round(this.cooldown * COOLDOWN[Mth.clamp(level, 1, MAX_LEVEL) - 1]));
    }

    /** Stärke der Stufe (Schaden, Heilung, Rückstoß): 100 %, 135 %, 170 %. */
    public static float power(int level) {
        return POWER[Mth.clamp(level, 1, MAX_LEVEL) - 1];
    }

    /** Reichweite der Stufe: je Stufe 15 % weiter. */
    public static double range(int level) {
        return 1.0 + 0.15 * (Mth.clamp(level, 1, MAX_LEVEL) - 1);
    }

    /** Römische Ziffer der Stufe (I, II, III). */
    public static String numeral(int level) {
        return switch (Mth.clamp(level, 1, MAX_LEVEL)) {
            case 1 -> "I";
            case 2 -> "II";
            default -> "III";
        };
    }

    /**
     * Kann der Zauber jetzt gewirkt werden? Läuft auf Server und Client gleich, damit beide dieselbe Abklingzeit sehen
     * (ein Blitz braucht ein Ziel in Reichweite).
     */
    public boolean canCast(Level level, LivingEntity caster, int spellLevel) {
        if (this == LIGHTNING) {
            return ProjectileUtil.getHitResultOnViewVector(caster, target -> canHit(caster, target), 48.0 * range(spellLevel))
                    .getType() != HitResult.Type.MISS;
        }
        return true;
    }

    /** Wirkt den Zauber auf Stufe 1. */
    public boolean cast(ServerLevel level, LivingEntity caster) {
        return this.cast(level, caster, 1);
    }

    /** Wirkt den Zauber. @return ob er etwas bewirkt hat (sonst keine Kosten und keine Abklingzeit) */
    public boolean cast(ServerLevel level, LivingEntity caster, int spellLevel) {
        int lv = Mth.clamp(spellLevel, 1, MAX_LEVEL);
        return switch (this) {
            case AETHER_BOLT -> aetherBolt(level, caster, lv);
            case FIRE -> fireball(level, caster, lv);
            case ICE -> frost(level, caster, lv);
            case LIGHTNING -> lightning(level, caster, lv);
            case NATURE -> nature(level, caster, lv);
            case EARTH -> earthquake(level, caster, lv);
            case WATER -> wave(level, caster, lv);
        };
    }

    // =====================================================================================
    // Die Zauber
    // =====================================================================================

    private static boolean aetherBolt(ServerLevel level, LivingEntity caster, int lv) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(caster, target -> canHit(caster, target), 24.0 * range(lv));
        Vec3 from = handPosition(caster);
        trail(level, from, hit.getLocation(), ParticleTypes.END_ROD, 3);
        if (lv >= 2) {
            trail(level, from, hit.getLocation(), ParticleTypes.WITCH, 1);
        }
        sound(level, caster, SoundEvents.AMETHYST_BLOCK_RESONATE, 1.2f, 1.4f);
        sound(level, caster, SoundEvents.EVOKER_CAST_SPELL, 0.6f, 1.6f);
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity target) {
            float damage = 6.0f * power(lv);
            DamageSource source = level.damageSources().indirectMagic(caster, caster);
            target.hurtServer(level, source, damage);
            level.sendParticles(ParticleTypes.GLOW, target.getX(), target.getY(0.5), target.getZ(), 15, 0.3, 0.4, 0.3, 0.05);
            if (lv >= 3) {
                // der Blitz springt auf den nächsten Gegner in der Nähe über
                LivingEntity next = nearestEnemy(level, caster, target.getBoundingBox().getCenter(), 6.0, e -> e != target);
                if (next != null) {
                    trail(level, target.getBoundingBox().getCenter(), next.getBoundingBox().getCenter(), ParticleTypes.END_ROD, 4);
                    next.hurtServer(level, source, damage * 0.6f);
                    level.sendParticles(ParticleTypes.GLOW, next.getX(), next.getY(0.5), next.getZ(), 12, 0.3, 0.4, 0.3, 0.05);
                    level.playSound(null, next.getX(), next.getY(), next.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.0f, 1.8f);
                }
            }
        }
        return true;
    }

    private static boolean fireball(ServerLevel level, LivingEntity caster, int lv) {
        Vec3 look = caster.getLookAngle();
        Vec3 start = handPosition(caster);
        int count = lv >= 2 ? 3 : 1;
        for (int i = 0; i < count; i++) {
            float spread = count == 1 ? 0.0f : (i - 1) * 9.0f;
            Vec3 dir = look.yRot(spread * Mth.DEG_TO_RAD);
            SpellFireball fireball = new SpellFireball(level, caster, dir.scale(1.6), 5.0f * power(lv), lv >= 3);
            fireball.setPos(start.x, start.y, start.z);
            level.addFreshEntity(fireball);
        }
        sound(level, caster, SoundEvents.BLAZE_SHOOT, 1.0f, lv >= 3 ? 0.8f : 1.0f);
        level.sendParticles(ParticleTypes.SMALL_FLAME, start.x, start.y, start.z, 6 * count, 0.08, 0.08, 0.08, 0.02);
        return true;
    }

    /** Frosthauch in einem Kegel vor dem Zaubernden (8 Blöcke, je Stufe weiter). */
    private static boolean frost(ServerLevel level, LivingEntity caster, int lv) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        double range = 8.0 * range(lv);
        for (int i = 2; i <= range; i++) {
            Vec3 point = eye.add(look.scale(i));
            double spread = 0.15 * i;
            level.sendParticles(ParticleTypes.SNOWFLAKE, point.x, point.y, point.z, 6, spread, spread, spread, 0.02);
            if (lv >= 3 && i % 2 == 0) {
                level.sendParticles(io.github.sirschrolli.kingdomsatwar.registry.ModParticles.BOSS_FROST.get(), point.x, point.y, point.z,
                        2, spread, spread, spread, 0.01);
            }
        }
        for (LivingEntity target : inCone(level, caster, range, 0.8)) {
            target.hurtServer(level, level.damageSources().freeze(), 4.0f * power(lv));
            target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 100 + 40 * (lv - 1), lv >= 3 ? 3 : 2), caster);
            target.setTicksFrozen(Math.max(target.getTicksFrozen(), target.getTicksRequiredToFreeze() + 120 + 60 * (lv - 1)));
            if (lv >= 3) {
                // festgefroren: anderthalb Sekunden kaum Bewegung, eine Hülle aus Eissplittern
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 30, 6), caster);
                target.addEffect(new MobEffectInstance(MobEffects.MINING_FATIGUE, 30, 2), caster);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PACKED_ICE.defaultBlockState()),
                        target.getX(), target.getY(0.5), target.getZ(), 24, target.getBbWidth() * 0.4, target.getBbHeight() * 0.4,
                        target.getBbWidth() * 0.4, 0.05);
            }
        }
        // Wasser vor einem friert zu brüchigem Eis (wie mit Frostläufer)
        BlockPos center = caster.blockPosition();
        int reach = (int) Math.ceil(range - 2);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-reach, -2, -reach), center.offset(reach, 1, reach))) {
            Vec3 toBlock = Vec3.atCenterOf(pos).subtract(eye);
            if (toBlock.length() > range - 0.5 || toBlock.normalize().dot(look) < 0.7) {
                continue;
            }
            BlockState state = level.getBlockState(pos);
            if (state.getFluidState().is(Fluids.WATER) && state.getFluidState().isSource() && level.getBlockState(pos.above()).isAir()) {
                level.setBlockAndUpdate(pos, Blocks.FROSTED_ICE.defaultBlockState());
                level.scheduleTick(pos, Blocks.FROSTED_ICE, Mth.nextInt(caster.getRandom(), 60, 120));
            }
        }
        sound(level, caster, SoundEvents.POWDER_SNOW_BREAK, 1.2f, 0.7f);
        sound(level, caster, SoundEvents.PLAYER_HURT_FREEZE, 0.6f, 1.4f);
        if (lv >= 3) {
            sound(level, caster, SoundEvents.GLASS_BREAK, 0.8f, 0.6f);
        }
        return true;
    }

    private static boolean lightning(ServerLevel level, LivingEntity caster, int lv) {
        HitResult hit = ProjectileUtil.getHitResultOnViewVector(caster, target -> canHit(caster, target), 48.0 * range(lv));
        if (hit.getType() == HitResult.Type.MISS) {
            sound(level, caster, SoundEvents.BEACON_DEACTIVATE, 0.6f, 1.6f);
            return false;
        }
        LightningBolt bolt = EntityTypes.LIGHTNING_BOLT.create(level, EntitySpawnReason.TRIGGERED);
        if (bolt == null) {
            return false;
        }
        Vec3 at = hit.getLocation();
        bolt.snapTo(at);
        if (caster instanceof ServerPlayer player) {
            bolt.setCause(player);
        }
        level.addFreshEntity(bolt);
        trail(level, handPosition(caster), at, ParticleTypes.ELECTRIC_SPARK, 2);
        if (lv >= 2) {
            // Kettenblitz: springt auf weitere Gegner in der Nähe über (Stufe II zwei, Stufe III vier)
            Entity main = hit instanceof EntityHitResult entityHit ? entityHit.getEntity() : null;
            double reach = lv >= 3 ? 7.0 : 5.0;
            List<LivingEntity> chain = enemiesNear(level, caster, at, reach, e -> e != main);
            chain.sort(Comparator.comparingDouble(e -> e.distanceToSqr(at)));
            Vec3 from = at.add(0, 1.0, 0);
            DamageSource source = level.damageSources().indirectMagic(caster, caster);
            for (int i = 0; i < Math.min(chain.size(), lv >= 3 ? 4 : 2); i++) {
                LivingEntity target = chain.get(i);
                Vec3 to = target.getBoundingBox().getCenter();
                LightningAttacks.arc(level, from, to);
                LightningAttacks.visualBolt(level, target.position());
                target.hurtServer(level, source, 5.0f * power(lv));
                target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 20, 5), caster);
                from = to;
            }
            if (!chain.isEmpty()) {
                level.playSound(null, at.x, at.y, at.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS, 1.6f, 1.3f);
            }
        }
        return true;
    }

    private static boolean nature(ServerLevel level, LivingEntity caster, int lv) {
        double radius = 8.0 * range(lv);
        List<LivingEntity> allies = new ArrayList<>(List.of(caster));
        allies.addAll(level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius),
                entity -> entity != caster && entity.isAlive() && isAlly(caster, entity)));
        for (LivingEntity ally : allies) {
            ally.addEffect(new MobEffectInstance(MobEffects.REGENERATION, lv >= 2 ? 160 : 120, lv >= 3 ? 2 : 1), caster);
            if (lv >= 2) {
                ally.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, 600, lv - 2), caster);
            }
            if (ally != caster) {     // beim Zaubernden selbst nicht – direkt vor der Kamera wären die Funken riesig
                level.sendParticles(ParticleTypes.HAPPY_VILLAGER, ally.getX(), ally.getY(0.6), ally.getZ(), 10, 0.4, 0.5, 0.4, 0.0);
            }
        }
        if (lv >= 3) {
            // Wurzeln brechen aus dem Boden und halten Gegner fest
            BlockState roots = Blocks.ROOTED_DIRT.defaultBlockState();
            for (LivingEntity enemy : enemiesNear(level, caster, caster.position(), 6.0 * range(lv), e -> true)) {
                enemy.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 4), caster);
                level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, roots), enemy.getX(), enemy.getY() + 0.2, enemy.getZ(),
                        20, 0.4, 0.1, 0.4, 0.05);
                level.sendParticles(ParticleTypes.COMPOSTER, enemy.getX(), enemy.getY() + 0.5, enemy.getZ(), 8, 0.3, 0.4, 0.3, 0.0);
            }
            sound(level, caster, SoundEvents.ROOTED_DIRT_BREAK, 1.2f, 0.6f);
        }
        BlockPos center = caster.blockPosition();
        ItemStack boneMeal = new ItemStack(Items.BONE_MEAL, 64);
        int tries = 10 + 4 * (lv - 1);
        int spread = 4 + lv;
        for (int i = 0; i < tries; i++) {
            BlockPos pos = center.offset(caster.getRandom().nextInt(spread * 2 + 1) - spread, caster.getRandom().nextInt(3) - 1,
                    caster.getRandom().nextInt(spread * 2 + 1) - spread);
            if (BoneMealItem.applyBonemeal(boneMeal, level, pos, caster instanceof Player player ? player : null)) {
                level.levelEvent(1505, pos, 15);    // Knochenmehl-Partikel
            }
        }
        level.sendParticles(ParticleTypes.SPORE_BLOSSOM_AIR, caster.getX(), caster.getY(1.0), caster.getZ(), 40, 3.0, 1.0, 3.0, 0.0);
        sound(level, caster, SoundEvents.AMETHYST_BLOCK_CHIME, 1.4f, 0.8f);
        sound(level, caster, SoundEvents.BONE_MEAL_USE, 1.0f, 1.0f);
        return true;
    }

    private static boolean earthquake(ServerLevel level, LivingEntity caster, int lv) {
        double radius = 5.0 * range(lv);
        BlockState ground = level.getBlockState(caster.blockPosition().below());
        ParticleOptions dust = ground.isAir() ? ParticleTypes.CLOUD : new BlockParticleOption(ParticleTypes.BLOCK, ground);
        for (int ring = 1; ring <= radius; ring++) {
            for (int a = 0; a < ring * 8; a++) {
                double angle = Math.PI * 2 * a / (ring * 8);
                level.sendParticles(dust, caster.getX() + Math.cos(angle) * ring, caster.getY() + 0.1, caster.getZ() + Math.sin(angle) * ring,
                        2, 0.1, 0.1, 0.1, 0.1);
            }
        }
        DamageSource source = level.damageSources().indirectMagic(caster, caster);
        double knock = 1.4 * (1.0 + 0.15 * (lv - 1));
        BlockState spike = Blocks.POINTED_DRIPSTONE.defaultBlockState();
        for (LivingEntity target : level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(radius, 2.0, radius),
                entity -> entity.isAlive() && canHit(caster, entity))) {
            if (target.hurtServer(level, source, 6.0f * power(lv))) {
                Vec3 away = target.position().subtract(caster.position()).multiply(1.0, 0.0, 1.0).normalize();
                target.knockback(knock, -away.x, -away.z, source, 6.0f);
                target.push(0.0, 0.5 + 0.1 * (lv - 1), 0.0);
                if (lv >= 3) {
                    // Felsdornen schießen unter dem Gegner hervor und lassen ihn taumeln
                    target.addEffect(new MobEffectInstance(MobEffects.SLOWNESS, 60, 1), caster);
                    target.addEffect(new MobEffectInstance(MobEffects.NAUSEA, 60, 0), caster);
                    for (int k = 0; k < 4; k++) {
                        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, spike), target.getX(), target.getY() + k * 0.4,
                                target.getZ(), 8, 0.15, 0.1, 0.15, 0.1);
                    }
                }
            }
        }
        sound(level, caster, SoundEvents.GENERIC_EXPLODE.value(), 0.7f, 0.55f);
        sound(level, caster, SoundEvents.RAVAGER_STEP, 1.2f, 0.6f);
        if (lv >= 3) {
            sound(level, caster, SoundEvents.POINTED_DRIPSTONE_LAND, 1.4f, 0.6f);
        }
        return true;
    }

    private static boolean wave(ServerLevel level, LivingEntity caster, int lv) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        double range = 7.0 * range(lv);
        for (int i = 2; i <= range; i++) {
            Vec3 point = eye.add(look.scale(i)).add(0, -0.8, 0);
            double spread = 0.2 * i;
            level.sendParticles(ParticleTypes.SPLASH, point.x, point.y, point.z, 20, spread, 0.3, spread, 0.1);
            level.sendParticles(ParticleTypes.BUBBLE_POP, point.x, point.y, point.z, 6, spread, 0.3, spread, 0.05);
        }
        DamageSource source = level.damageSources().indirectMagic(caster, caster);
        double knock = 1.8 * (1.0 + 0.2 * (lv - 1));
        for (LivingEntity target : inCone(level, caster, range, 0.6)) {
            target.clearFire();
            target.knockback(knock, -look.x, -look.z, source, 0.0f);
            target.push(0.0, 0.25, 0.0);
        }
        // Feuer vor einem löschen
        BlockPos center = caster.blockPosition();
        int reach = (int) Math.ceil(range - 1);
        for (BlockPos pos : BlockPos.betweenClosed(center.offset(-reach, -1, -reach), center.offset(reach, 2, reach))) {
            Vec3 toBlock = Vec3.atCenterOf(pos).subtract(eye);
            if (toBlock.length() <= range && toBlock.normalize().dot(look) > 0.5 && level.getBlockState(pos).is(Blocks.FIRE)) {
                level.removeBlock(pos, false);
                level.levelEvent(1009, pos, 0);   // Zischen
            }
        }
        caster.clearFire();
        caster.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 600, 0), caster);
        caster.addEffect(new MobEffectInstance(MobEffects.DOLPHINS_GRACE, 200, 0), caster);
        if (lv >= 2) {
            caster.heal(lv >= 3 ? 5.0f : 3.0f);
        }
        if (lv >= 3) {
            // Wasserschild: eine Hülle aus Wasser fängt Schläge ab
            caster.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 120, 0), caster);
            for (int a = 0; a < 24; a++) {
                double angle = Mth.TWO_PI * a / 24;
                level.sendParticles(ParticleTypes.DRIPPING_WATER, caster.getX() + Math.cos(angle) * 1.1, caster.getY(0.5) + Math.sin(a) * 0.6,
                        caster.getZ() + Math.sin(angle) * 1.1, 1, 0, 0, 0, 0);
            }
            sound(level, caster, SoundEvents.BUBBLE_COLUMN_UPWARDS_INSIDE, 1.0f, 1.2f);
        }
        sound(level, caster, SoundEvents.GENERIC_SPLASH, 1.2f, 0.8f);
        return true;
    }

    // =====================================================================================
    // Feuerball des Stabs
    // =====================================================================================

    /**
     * Feuerball des Stabs: verbrennt Wesen, setzt aber keine Blöcke in Brand (keine abgebrannten Burgen aus Versehen);
     * Verbündete fliegt er vorbei. Auf Stufe III zerbirst er beim Aufprall in einer Feuerwolke.
     */
    private static final class SpellFireball extends SmallFireball {
        private final float damage;
        private final boolean burst;

        SpellFireball(Level level, LivingEntity owner, Vec3 direction, float damage, boolean burst) {
            super(level, owner, direction);
            this.damage = damage;
            this.burst = burst;
        }

        @Override
        protected boolean canHitEntity(Entity entity) {
            return super.canHitEntity(entity) && !(this.getOwner() instanceof LivingEntity owner && isAlly(owner, entity));
        }

        @Override
        protected void onHitEntity(EntityHitResult hit) {
            if (this.level() instanceof ServerLevel server) {
                Entity target = hit.getEntity();
                int fire = target.getRemainingFireTicks();
                target.igniteForSeconds(5.0f);
                if (!target.hurtServer(server, this.damageSources().fireball(this, this.getOwner()), this.damage)) {
                    target.setRemainingFireTicks(fire);
                }
            }
        }

        @Override
        protected void onHitBlock(BlockHitResult hit) {
            if (this.level() instanceof ServerLevel server) {
                Vec3 at = hit.getLocation();
                server.sendParticles(ParticleTypes.SMOKE, at.x, at.y, at.z, 8, 0.15, 0.15, 0.15, 0.02);
            }
        }

        @Override
        protected void onHit(HitResult hit) {
            super.onHit(hit);
            if (this.burst && this.level() instanceof ServerLevel server && this.getOwner() instanceof LivingEntity owner) {
                Vec3 at = hit.getLocation();
                server.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, 1, 0, 0, 0, 0);
                server.sendParticles(ParticleTypes.FLAME, at.x, at.y, at.z, 30, 1.0, 0.6, 1.0, 0.06);
                server.sendParticles(ParticleTypes.LAVA, at.x, at.y, at.z, 6, 0.6, 0.3, 0.6, 0.1);
                server.playSound(null, at.x, at.y, at.z, SoundEvents.GENERIC_EXPLODE.value(), SoundSource.PLAYERS, 0.9f, 1.3f);
                DamageSource source = this.damageSources().fireball(this, owner);
                for (LivingEntity e : enemiesNear(server, owner, at, 3.0, e -> !(hit instanceof EntityHitResult h && h.getEntity() == e))) {
                    e.igniteForSeconds(4.0f);
                    e.hurtServer(server, source, this.damage * 0.6f);
                }
            }
        }
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    /**
     * Verbündete: man selbst, eigene Tiere, Drachen und Soldaten sowie Mitglieder des eigenen Teams. Für einen
     * Magier-Boss alles, was nicht sein Gegner ist (seine Diener, andere Bosse).
     */
    public static boolean isAlly(LivingEntity caster, Entity entity) {
        if (entity == caster || caster.isAlliedTo(entity)) {
            return true;
        }
        if (caster instanceof ElementalBoss boss) {
            return !(entity instanceof LivingEntity living) || !boss.isBossEnemy(living);
        }
        return entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null
                && caster.getUUID().equals(ownable.getOwnerReference().getUUID());
    }

    static boolean canHit(LivingEntity caster, Entity entity) {
        return entity instanceof LivingEntity && entity.isPickable() && !entity.isSpectator() && !isAlly(caster, entity);
    }

    /** Gegner des Zaubernden im Umkreis eines Punktes. */
    private static List<LivingEntity> enemiesNear(ServerLevel level, LivingEntity caster, Vec3 center, double radius,
            Predicate<LivingEntity> filter) {
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(center, center).inflate(radius), e -> e.isAlive()
                && canHit(caster, e) && filter.test(e) && e.getBoundingBox().getCenter().distanceToSqr(center) <= radius * radius);
    }

    private static @Nullable LivingEntity nearestEnemy(ServerLevel level, LivingEntity caster, Vec3 center, double radius,
            Predicate<LivingEntity> filter) {
        LivingEntity best = null;
        double bestDist = Double.MAX_VALUE;
        for (LivingEntity e : enemiesNear(level, caster, center, radius, filter)) {
            double d = e.distanceToSqr(center);
            if (d < bestDist) {
                bestDist = d;
                best = e;
            }
        }
        return best;
    }

    /** Lebewesen im Kegel vor dem Zaubernden (Reichweite, Mindest-Kosinus zur Blickrichtung). */
    private static List<LivingEntity> inCone(ServerLevel level, LivingEntity caster, double range, double minDot) {
        Vec3 eye = caster.getEyePosition();
        Vec3 look = caster.getLookAngle();
        return level.getEntitiesOfClass(LivingEntity.class, new AABB(eye, eye).inflate(range), entity -> {
            if (!entity.isAlive() || !canHit(caster, entity)) {
                return false;
            }
            Vec3 to = entity.getBoundingBox().getCenter().subtract(eye);
            return to.length() <= range && to.normalize().dot(look) >= minDot;
        });
    }

    /** Spitze des Stabs: etwas vor und unterhalb der Augen (Partikel direkt vor der Kamera verdecken sonst die Sicht). */
    private static Vec3 handPosition(LivingEntity caster) {
        Vec3 look = caster.getLookAngle();
        return caster.getEyePosition().add(look.scale(1.2)).add(0, -0.25, 0);
    }

    private static void trail(ServerLevel level, Vec3 from, Vec3 to, ParticleOptions particle, int perBlock) {
        Vec3 delta = to.subtract(from);
        int steps = Math.max(1, (int) (delta.length() * perBlock));
        for (int i = 1; i <= steps; i++) {
            Vec3 point = from.add(delta.scale((double) i / steps));
            level.sendParticles(particle, point.x, point.y, point.z, 1, 0.02, 0.02, 0.02, 0.0);
        }
    }

    private static void sound(ServerLevel level, LivingEntity caster, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), sound,
                caster instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE, volume, pitch);
    }
}
