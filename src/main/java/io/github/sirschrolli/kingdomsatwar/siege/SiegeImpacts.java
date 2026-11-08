package io.github.sirschrolli.kingdomsatwar.siege;

import io.github.sirschrolli.kingdomsatwar.arena.ArcaneSphere;
import io.github.sirschrolli.kingdomsatwar.config.ModServerConfig;
import io.github.sirschrolli.kingdomsatwar.economy.town.Towns;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossFx;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.network.ModNetwork;
import io.github.sirschrolli.kingdomsatwar.network.ScreenShakePayload;
import io.github.sirschrolli.kingdomsatwar.registry.ModDamageTypes;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.war.RealmWar;
import io.github.sirschrolli.kingdomsatwar.world.SealCircle;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastle;
import io.github.sirschrolli.kingdomsatwar.worldgen.realm.RealmCastlePiece;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.OwnableEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.Tags;
import org.jspecify.annotations.Nullable;

/**
 * Was Belagerungswaffen anrichten: Einschläge von Steinkugeln und Brocken (Mauerwerk zerspringt, Wesen werden
 * fortgeschleudert), Treffer der Ballistenbolzen und die Stöße des Rammbocks gegen Tore, Türen und Gitter.
 *
 * <p>Geschützt bleibt, was nicht zerstört werden darf: Siedlungen der Spieler, der Siegelkreis, die Arkane Sphäre, das
 * Reichsbanner hinter dem Thron einer Stammburg und alle Blöcke mit dem Tag {@code kingdomsatwar:siege_proof}. Es gibt
 * keine Kettenreaktionen (TNT wird nur zerschlagen, nicht gezündet), und mit der Einstellung {@code siegeBlockDamage}
 * lässt sich der Blockschaden ganz abschalten.
 */
public final class SiegeImpacts {
    /** Um den Siegelkreis bleibt alles heil. */
    private static final double SEAL_PROTECTION = 48.0;
    /** Um das Reichsbanner einer Stammburg bleibt alles heil (sonst ließe sich die Burg nicht mehr erobern). */
    private static final double STANDARD_PROTECTION = 3.5;
    /** Nur die ersten so vielen Blöcke eines Einschlags krachen hörbar – sonst wäre es ein ohrenbetäubendes Geprassel. */
    private static final int LOUD_BLOCKS = 6;

    private SiegeImpacts() {}

    // =====================================================================================
    // Abschuss
    // =====================================================================================

    /** Geräusch und Staub beim Abwurf bzw. Schuss. */
    static void launchEffects(ServerLevel level, SiegeWeaponEntity weapon, Vec3 from) {
        switch (weapon.siege()) {
            case CATAPULT -> {
                sound(level, from, SoundEvents.PLAYER_ATTACK_SWEEP, 1.6f, 0.45f);
                sound(level, from, SoundEvents.TRIDENT_THROW.value(), 1.2f, 0.5f);
                level.sendParticles(ParticleTypes.CLOUD, from.x, from.y, from.z, 6, 0.3, 0.2, 0.3, 0.02);
            }
            case TREBUCHET -> {
                sound(level, from, SoundEvents.PLAYER_ATTACK_SWEEP, 2.4f, 0.3f);
                sound(level, from, SoundEvents.TRIDENT_THROW.value(), 2.0f, 0.35f);
                level.sendParticles(ParticleTypes.CLOUD, from.x, from.y, from.z, 10, 0.5, 0.3, 0.5, 0.03);
            }
            case BALLISTA -> {
                sound(level, from, SoundEvents.CROSSBOW_SHOOT, 2.0f, 0.45f);
                sound(level, from, SoundEvents.TRIDENT_THROW.value(), 1.2f, 0.7f);
                level.sendParticles(ParticleTypes.CRIT, from.x, from.y, from.z, 6, 0.2, 0.1, 0.2, 0.15);
            }
            case RAM, SIEGE_TOWER, WAGON -> {
            }
        }
    }

    // =====================================================================================
    // Steinkugel und Brocken
    // =====================================================================================

    /** Einschlag einer Steinkugel (Katapult) oder eines Brockens (Tribok) bei {@code at}. */
    static void stone(ServerLevel level, SiegeProjectile stone, Vec3 at, @Nullable Entity direct) {
        SiegeType siege = stone.siege();
        boolean heavy = siege == SiegeType.TREBUCHET;
        Entity causing = stone.getOwner();
        DamageSource source = level.damageSources().source(ModDamageTypes.SIEGE_STONE, stone, causing);
        Vec3 dir = stone.getDeltaMovement().lengthSqr() > 1.0e-4 ? stone.getDeltaMovement().normalize() : new Vec3(0.0, -1.0, 0.0);

        // Wesen im Umkreis: in der Mitte voller Schaden, nach außen weniger; alle werden fortgeschleudert
        double radius = siege.blastRadius();
        for (Entity e : level.getEntities(stone, new AABB(at, at).inflate(radius), e -> e.isAlive() && !e.isSpectator())) {
            if (friendly(causing, e) || e instanceof SiegeProjectile) {
                continue;
            }
            double d = Math.sqrt(e.getBoundingBox().distanceToSqr(at));
            if (d > radius) {
                continue;
            }
            float falloff = (float) Mth.clamp(1.0 - d / radius * 0.7, 0.3, 1.0);
            float damage = siege.damage() * falloff * (e == direct ? 1.25f : 1.0f);
            if (e instanceof SiegeWeaponEntity weapon) {
                weapon.damage(level, damage * 1.5f, source);
                continue;
            }
            if (!(e instanceof LivingEntity living)) {
                continue;
            }
            living.hurtServer(level, source, damage);
            double resist = 1.0 - Mth.clamp(living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            Vec3 away = e.position().subtract(at).multiply(1.0, 0.0, 1.0);
            away = away.lengthSqr() < 1.0e-4 ? dir.multiply(1.0, 0.0, 1.0) : away.normalize();
            double push = (heavy ? 1.3 : 0.9) * falloff * resist;
            living.push(away.x * push, (heavy ? 0.5 : 0.35) * falloff * resist, away.z * push);
            living.needsSync = true;
        }

        // Mauerwerk um den Einschlag zerspringt
        int broken = 0;
        BlockState struck = level.getBlockState(BlockPos.containing(at.add(dir.scale(0.3))));
        if (blockDamageAllowed(level, at)) {
            Protection protection = Protection.at(level, at);
            Vec3 core = at.add(dir.scale(0.6));
            double br = siege.blockRadius();
            float maxResistance = heavy ? 9.0f : 6.0f;
            BlockPos c = BlockPos.containing(core);
            int r = Mth.ceil(br);
            List<BlockPos> targets = new ArrayList<>();
            for (BlockPos pos : BlockPos.betweenClosed(c.offset(-r, -r, -r), c.offset(r, r, r))) {
                double dist = Vec3.atCenterOf(pos).distanceTo(core);
                if (dist <= br - stone.getRandom().nextDouble() * 0.6) {
                    targets.add(pos.immutable());
                }
            }
            targets.sort((a, b) -> Double.compare(Vec3.atCenterOf(a).distanceToSqr(core), Vec3.atCenterOf(b).distanceToSqr(core)));
            for (BlockPos pos : targets) {
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || !breakable(level, pos, state, maxResistance) || protection.covers(pos)) {
                    continue;
                }
                if (struck.isAir()) {
                    struck = state;
                }
                smash(level, pos, state, stone, broken < LOUD_BLOCKS, stone.getRandom().nextFloat() < 0.3f);
                broken++;
            }
        }

        // Krach, Staub und Splitter
        float volume = heavy ? 3.0f : 2.0f;
        sound(level, at, SoundEvents.GENERIC_EXPLODE.value(), heavy ? 1.6f : 0.9f, heavy ? 1.05f : 1.4f);
        sound(level, at, SoundEvents.STONE_BREAK, volume, 0.5f);
        sound(level, at, SoundEvents.ANVIL_LAND, heavy ? 0.8f : 0.5f, 0.5f);
        BlockState debris = struck.isAir() ? Blocks.COBBLESTONE.defaultBlockState() : struck;
        BossFx.impact(level, new BlockParticleOption(ParticleTypes.BLOCK, debris), at, heavy ? 70 : 40, 0.6, 0.5, 0.6, 0.25);
        BossFx.impact(level, new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()), at, heavy ? 30 : 16,
                0.4, 0.4, 0.4, 0.3);
        BossFx.impact(level, ParticleTypes.POOF, at, heavy ? 30 : 16, 0.8, 0.4, 0.8, 0.05);
        level.sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE, at.x, at.y + 0.3, at.z, heavy ? 8 : 4, 0.8, 0.3, 0.8, 0.01);
        if (heavy || broken > 0) {
            level.sendParticles(ParticleTypes.EXPLOSION, at.x, at.y, at.z, heavy ? 2 : 1, 0.3, 0.3, 0.3, 0.0);
        }
        shake(level, at, heavy ? 0.9f : 0.45f, heavy ? 14 : 8, heavy ? 28.0 : 16.0);
    }

    // =====================================================================================
    // Ballistenbolzen
    // =====================================================================================

    /** Ein Bolzen trifft ein Wesen. @return ob er getroffen hat (sonst fliegt er weiter, z. B. an Verbündeten vorbei) */
    static boolean bolt(ServerLevel level, SiegeProjectile bolt, Entity target) {
        Entity causing = bolt.getOwner();
        if (friendly(causing, target)) {
            return false;
        }
        DamageSource source = level.damageSources().source(ModDamageTypes.BALLISTA_BOLT, bolt, causing);
        double speed = bolt.getDeltaMovement().length();
        float damage = bolt.siege().damage() * (float) Mth.clamp(speed / bolt.siege().maxVelocity(), 0.5, 1.2);
        Vec3 dir = bolt.getDeltaMovement().normalize();
        if (target instanceof SiegeWeaponEntity weapon) {
            weapon.damage(level, damage * 1.5f, source);
        } else if (target instanceof LivingEntity living) {
            if (!living.hurtServer(level, source, damage)) {
                return true;
            }
            double resist = 1.0 - Mth.clamp(living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
            living.push(dir.x * 1.1 * resist, 0.25 * resist, dir.z * 1.1 * resist);
            living.needsSync = true;
        } else {
            target.hurtServer(level, source, damage);
        }
        Vec3 at = target.getBoundingBox().getCenter();
        sound(level, at, SoundEvents.TRIDENT_HIT, 1.4f, 0.7f);
        sound(level, at, SoundEvents.PLAYER_ATTACK_CRIT, 1.0f, 0.8f);
        level.sendParticles(ParticleTypes.CRIT, at.x, at.y, at.z, 12, 0.3, 0.3, 0.3, 0.3);
        return true;
    }

    /** Zerspringt dieser Block, wenn ein Bolzen ihn trifft (Glas, Scheiben, Eis)? */
    public static boolean shatters(BlockState state) {
        return state.is(Tags.BlockItems.GLASS_BLOCKS.block()) || state.is(Tags.BlockItems.GLASS_PANES.block()) || state.is(Blocks.ICE)
                || state.is(BlockTags.LEAVES);
    }

    // =====================================================================================
    // Rammbock
    // =====================================================================================

    /** Wie viele Stöße dieser Block aushält (0: dem Rammbock gewachsen – Mauerwerk ist Sache der Wurfmaschinen). */
    public static int ramHits(BlockState state) {
        if (state.is(Blocks.IRON_DOOR) || state.is(BlockTags.WALLS)) {
            return 4;
        }
        if (state.is(BlockTags.DOORS)) {
            return state.is(BlockTags.WOODEN_DOORS) ? 2 : 3;
        }
        if (state.is(BlockTags.TRAPDOORS)) {
            return state.is(BlockTags.WOODEN_TRAPDOORS) ? 1 : 3;
        }
        if (state.is(BlockTags.BARS)) {
            return 3;
        }
        if (state.is(BlockTags.FENCE_GATES) || state.is(BlockTags.FENCES) || state.is(BlockTags.CHAINS)) {
            return 2;
        }
        if (state.is(Tags.BlockItems.GLASS_BLOCKS.block()) || state.is(Tags.BlockItems.GLASS_PANES.block())) {
            return 1;
        }
        if (state.is(BlockTags.LOGS)) {
            return 4;
        }
        if (state.is(BlockTags.PLANKS) || state.is(BlockTags.WOODEN_SLABS) || state.is(BlockTags.WOODEN_STAIRS)) {
            return 3;
        }
        return 0;
    }

    /**
     * Der Widder schlägt zu: Tore, Türen, Gitter und Holz vor dem Kopf bekommen einen Stoß (nach einigen Stößen brechen
     * sie, bis dahin zeigen sie Risse), Wesen davor werden umgestoßen.
     */
    static void ramSwing(ServerLevel level, SiegeWeaponEntity ram, @Nullable LivingEntity driver) {
        Vec3 fwd = Vec3.directionFromRotation(0.0f, ram.getYRot());
        Vec3 side = new Vec3(-fwd.z, 0.0, fwd.x);
        Vec3 head = ram.launchPoint();
        Entity causing = driver != null ? driver : ram;
        DamageSource source = level.damageSources().source(ModDamageTypes.BATTERING_RAM, ram, causing);

        // Wesen vor dem Kopf
        AABB reach = new AABB(head, head).inflate(1.0, 0.9, 1.0).expandTowards(fwd.scale(0.8));
        for (LivingEntity victim : level.getEntitiesOfClass(LivingEntity.class, reach,
                e -> e.isAlive() && !e.isSpectator() && e.getVehicle() != ram && !friendly(causing, e))) {
            if (victim.hurtServer(level, source, 9.0f)) {
                double resist = 1.0 - Mth.clamp(victim.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE), 0.0, 1.0);
                victim.push(fwd.x * 1.4 * resist, 0.35 * resist, fwd.z * 1.4 * resist);
                victim.needsSync = true;
            }
        }
        for (SiegeWeaponEntity other : level.getEntitiesOfClass(SiegeWeaponEntity.class, reach, w -> w != ram && !friendly(causing, w))) {
            other.damage(level, 14.0f, source);
        }

        // Blöcke vor dem Kopf: drei breit, drei hoch
        boolean allowed = blockDamageAllowed(level, head);
        Protection protection = allowed ? Protection.at(level, head) : Protection.NONE;
        SoundEvent thud = null;
        BlockState struck = null;
        boolean brokeAny = false;
        for (int h = -1; h <= 1; h++) {
            for (int l = -1; l <= 1; l++) {
                Vec3 point = head.add(fwd.scale(0.55)).add(side.scale(l)).add(0.0, h, 0.0);
                BlockPos pos = BlockPos.containing(point);
                BlockState state = level.getBlockState(pos);
                if (state.isAir() || state.getCollisionShape(level, pos).isEmpty()) {
                    continue;
                }
                if (struck == null || l == 0 && h == 0) {
                    struck = state;
                }
                int hits = ramHits(state);
                if (hits <= 0 || !allowed || !breakable(level, pos, state, 1200.0f) || protection.covers(pos)) {
                    continue;
                }
                thud = state.is(Blocks.IRON_DOOR) || state.is(BlockTags.BARS) || state.is(BlockTags.CHAINS) || state.is(BlockTags.TRAPDOORS)
                        && !state.is(BlockTags.WOODEN_TRAPDOORS) ? SoundEvents.ZOMBIE_ATTACK_IRON_DOOR : SoundEvents.ZOMBIE_ATTACK_WOODEN_DOOR;
                if (ram.ramBlow(level, pos, 10.0f / hits)) {
                    smash(level, pos, state, ram, true, true);
                    brokeAny = true;
                }
            }
        }
        Vec3 hit = head.add(fwd.scale(0.5));
        if (struck != null) {
            sound(level, hit, thud != null ? thud : SoundEvents.ANVIL_LAND, thud != null ? 1.4f : 0.7f, thud != null ? 0.7f : 0.45f);
            if (brokeAny) {
                sound(level, hit, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, 1.4f, 0.8f);
            }
            level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, struck), hit.x, hit.y, hit.z, 24, 0.5, 0.6, 0.5, 0.15);
            level.sendParticles(ParticleTypes.POOF, hit.x, hit.y, hit.z, 6, 0.4, 0.4, 0.4, 0.02);
            shake(level, hit, 0.35f, 6, 10.0);
        } else {
            sound(level, hit, SoundEvents.PLAYER_ATTACK_SWEEP, 1.0f, 0.5f);
        }
    }

    // =====================================================================================
    // Schutz
    // =====================================================================================

    /** Darf um diese Stelle überhaupt Mauerwerk zu Bruch gehen (Einstellung, Arkane Sphäre, Siegelkreis)? */
    static boolean blockDamageAllowed(ServerLevel level, Vec3 at) {
        if (!ModServerConfig.SIEGE_BLOCK_DAMAGE.get() || level.dimension() == ArcaneSphere.LEVEL) {
            return false;
        }
        if (level == level.getServer().overworld()) {
            BlockPos seal = SealCircle.center(level);
            return seal == null || seal.distToCenterSqr(at) > SEAL_PROTECTION * SEAL_PROTECTION;
        }
        return true;
    }

    /** Ist der Block zerstörbar (nicht unzerbrechlich, nicht zu fest, nicht geschützt)? */
    static boolean breakable(ServerLevel level, BlockPos pos, BlockState state, float maxResistance) {
        if (state.isAir() || state.getDestroySpeed(level, pos) < 0.0f || state.is(ModTags.Blocks.SIEGE_PROOF)) {
            return false;
        }
        // auch Wasser und Lava (Widerstand 100) bleiben, wie sie sind
        return state.getBlock().getExplosionResistance() <= maxResistance;
    }

    /** Zerschlägt einen Block (laut mit Bruchgeräusch oder leise nur mit Splittern); manchmal bleibt etwas übrig. */
    private static void smash(ServerLevel level, BlockPos pos, BlockState state, Entity breaker, boolean loud, boolean drop) {
        if (loud) {
            level.destroyBlock(pos, drop, breaker);
            return;
        }
        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
        if (drop) {
            Block.dropResources(state, level, pos, blockEntity, breaker, ItemStack.EMPTY);
        }
        level.setBlock(pos, level.getFluidState(pos).createLegacyBlock(), Block.UPDATE_ALL);
        level.sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, state), pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5, 8,
                0.3, 0.3, 0.3, 0.1);
    }

    /** Siedlungen der Spieler und das Reichsbanner einer Stammburg rund um einen Einschlag. */
    private record Protection(ServerLevel level, @Nullable BlockPos standard, boolean towns) {
        static final Protection NONE = new Protection(null, null, false);

        static Protection at(ServerLevel level, Vec3 at) {
            BlockPos standard = null;
            if (level == level.getServer().overworld()) {
                RealmCastlePiece castle = RealmWar.castleAt(level, BlockPos.containing(at));
                if (castle != null) {
                    standard = castle.world(RealmCastle.STANDARD).above();
                }
            }
            return new Protection(level, standard, !Towns.of(level.getServer()).all().isEmpty());
        }

        boolean covers(BlockPos pos) {
            if (this.level == null) {
                return false;
            }
            if (this.standard != null && this.standard.distSqr(pos) <= STANDARD_PROTECTION * STANDARD_PROTECTION) {
                return true;
            }
            return this.towns && Towns.of(this.level.getServer()).at(this.level.dimension(), pos) != null;
        }
    }

    // =====================================================================================
    // Freund und Feind
    // =====================================================================================

    /**
     * Trifft das eigene Leute? Der Schütze selbst, seine Soldaten, seine Belagerungswaffen und – schießt ein Soldat – sein
     * Herr und dessen übrige Truppen bleiben verschont.
     */
    public static boolean friendly(@Nullable Entity causing, Entity victim) {
        if (causing == null) {
            return false;
        }
        if (victim == causing || victim.getVehicle() == causing || causing.getVehicle() == victim || victim.hasPassenger(causing)) {
            return true;
        }
        if (causing.isAlliedTo(victim) || victim.isAlliedTo(causing)) {
            return true;
        }
        UUID lord = lord(causing);
        if (lord == null) {
            return false;
        }
        if (victim instanceof Player player && player.getUUID().equals(lord)) {
            return true;
        }
        if (victim instanceof SiegeWeaponEntity weapon) {
            return lord.equals(weapon.owner());
        }
        return lord.equals(lord(victim));
    }

    /** Wem gehört dieses Wesen (Spieler: sich selbst; Soldat: seinem Herrn; Belagerungswaffe: ihrem Besitzer)? */
    private static @Nullable UUID lord(Entity entity) {
        if (entity instanceof Player player) {
            return player.getUUID();
        }
        if (entity instanceof SoldierEntity soldier) {
            return soldier.getOwnerId();
        }
        if (entity instanceof SiegeWeaponEntity weapon) {
            return weapon.owner();
        }
        if (entity instanceof OwnableEntity ownable && ownable.getOwnerReference() != null) {
            return ownable.getOwnerReference().getUUID();
        }
        return null;
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    static void sound(ServerLevel level, Vec3 at, SoundEvent sound, float volume, float pitch) {
        level.playSound(null, at.x, at.y, at.z, sound, SoundSource.NEUTRAL, volume, pitch);
    }

    /** Die Erde bebt für alle Spieler in der Nähe (je näher, desto stärker). */
    private static void shake(ServerLevel level, Vec3 at, float strength, int ticks, double range) {
        for (ServerPlayer player : level.players()) {
            double d = player.position().distanceTo(at);
            if (d < range) {
                ModNetwork.send(player, new ScreenShakePayload(strength * (float) (1.0 - d / range), ticks));
            }
        }
    }
}
