package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.voidrealm.WeaverNestBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarSpiderlingEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWeaverEntity;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.StarWebShotEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModTags;
import io.github.sirschrolli.kingdomsatwar.registry.StarWeaver;
import io.github.sirschrolli.kingdomsatwar.registry.VoidGear;
import io.github.sirschrolli.kingdomsatwar.voidrealm.VoidGate;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebPiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.weaver.StarWebStructure;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Rotations;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code star_weaver}): das Sternennetz in den leeren Weiten der Leere – von oben, vom Rand der Netzplatte, das
 * Gelege, Anker und Taue mit Kokons, die Sternenkrone – und die Sternenweberin: in Andacht an ihrem Faden, ihr Erwachen, die
 * Bossleiste, alle Angriffe gegen einen (zähen) Ritter, ihr Zorn mit der Spindel, ihr Tod und das erloschene Gelege; dazu die
 * Rüstung der Sternwandler mit dem Sternenbogen.
 *
 * <p>Lage auf dem Netz: {@code along} zählt nach Süden, {@code side} nach Osten, {@code up} über der Netzplatte.
 */
final class StarWeaverShowcase {
    private static final String TAG = "kaw_weaver_show";

    private StarWeaverShowcase() {}

    private static final class Web {
        BlockPos center;
        BlockPos nest;
        int weaver = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel voidLevel(ServerLevel any) {
        return any.getServer().getLevel(VoidGate.THE_VOID);
    }

    static void boss(Deque<Step> steps) {
        Web g = new Web();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(voidLevel(any), g)));
        steps.add(Step.until(() -> g.ready, 12000));
        steps.add(Step.server(any -> clean(voidLevel(any), g)));
        // das Netz von oben, vom Rand, das Gelege, ein Anker mit Tau und Kokons, die Krone
        steps.add(look(g, 30.0, 26.0, 22.0, 0.0, 0.0, 0.0));
        shot(steps, "web_aerial", true);
        steps.add(look(g, 17.0, 3.0, 4.0, 0.0, 3.0, 0.0));
        shot(steps, "web_edge", false);
        steps.add(look(g, 5.0, 2.4, 3.0, 0.0, 0.4, 0.0));
        shot(steps, "web_nest", false);
        steps.add(look(g, -8.0, 4.0, 18.0, -16.0, 1.0, 26.0));
        shot(steps, "web_anchor", false);
        steps.add(look(g, 12.0, 6.0, -10.0, 0.0, StarWebPiece.CROWN, 0.0));
        shot(steps, "web_crown", false);
        steps.add(look(g, 4.0, -6.0, 15.0, 0.0, -2.0, 18.0));
        shot(steps, "web_cocoons", false);

        // die Weberin in Andacht
        steps.add(Step.server(any -> {
            ServerLevel level = voidLevel(any);
            if (level.getBlockEntity(g.nest) instanceof WeaverNestBlockEntity nest) {
                nest.reset(level);
                StarWeaverEntity weaver = nest.spawn(level, false);
                if (weaver != null) {
                    g.weaver = weaver.getId();
                }
            }
        }));
        steps.add(look(g, 9.0, 4.0, 4.0, 0.0, StarWeaverEntity.HANG, 0.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("weaver_dormant"));
        steps.add(look(g, 4.0, 5.6, 2.2, 0.0, StarWeaverEntity.HANG + 0.6, 0.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("weaver_dormant_close"));
        // sie erwacht
        steps.add(Step.server(any -> {
            if (voidLevel(any).getEntity(g.weaver) instanceof StarWeaverEntity weaver) {
                weaver.awaken();
            }
        }));
        steps.add(look(g, 9.0, 3.4, 3.0, 0.0, 2.6, 0.0));
        steps.add(attackAt(g, StarWeaverEntity.Attack.AWAKEN, 8));
        steps.add(Step.screenshot("weaver_awaken_fall"));
        steps.add(attackAt(g, StarWeaverEntity.Attack.AWAKEN, 40));
        steps.add(Step.screenshot("weaver_awaken"));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.getEntity(g.weaver) instanceof StarWeaverEntity weaver
                    && weaver.currentAttack() != StarWeaverEntity.Attack.AWAKEN;
        }, 200));
        // ein Ritter als Gegner
        steps.add(Step.server(any -> {
            ServerLevel level = voidLevel(any);
            Mob knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.EVENT);
            if (knight == null) {
                return;
            }
            Vec3 p = rel(g, 4.0, 0.0, 6.0);
            knight.snapTo(p.x, p.y, p.z, 90.0f, 0.0f);
            knight.goalSelector.removeAllGoals(goal -> true);
            knight.targetSelector.removeAllGoals(goal -> true);
            knight.setSilent(true);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(net.minecraft.world.item.Items.IRON_SWORD));
            knight.addTag(TAG);
            level.addFreshEntity(knight);
            g.knight = knight.getId();
        }));
        steps.add(stage(g, 6.0, false));
        steps.add(Step.hud(true));
        steps.add(follow(g, 40.0, 8.0, 1.5, 0.3));
        steps.add(Step.waitTicks(60));
        steps.add(follow(g, 40.0, 8.0, 1.5, 0.3));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("weaver_bossbar"));
        steps.add(Step.hud(false));
        // die Angriffe: {Angriff, Tick, Name, Winkel, Abstand, Höhe, Blick zum Ritter, Abstand des Ritters, Zorn}
        java.util.Set<String> started = new java.util.HashSet<>();
        for (String[] shot : new String[][] {
                {"BITE", "11", "weaver_bite", "70", "6.5", "1.4", "0.4", "3.2", ""},
                {"THREAD", "16", "weaver_thread", "60", "9.0", "1.6", "0.5", "12.0", ""},
                {"THREAD", "30", "weaver_thread_reel", "70", "7.0", "1.4", "0.4", "12.0", ""},
                {"WEB", "16", "weaver_web", "55", "9.0", "1.8", "0.5", "9.0", ""},
                {"CONSTELLATION", "28", "weaver_constellation", "40", "10.0", "4.0", "0.8", "7.0", ""},
                {"CONSTELLATION", "45", "weaver_constellation_flare", "40", "10.0", "4.5", "0.8", "7.0", ""},
                {"STARFALL", "30", "weaver_starfall", "45", "11.0", "4.0", "0.6", "8.0", ""},
                {"COCOON", "20", "weaver_cocoon", "65", "6.5", "1.4", "0.6", "3.5", ""},
                {"BROOD", "30", "weaver_brood", "45", "8.0", "2.0", "0.2", "6.0", ""},
                {"DROP", "34", "weaver_drop_hang", "40", "11.0", "5.0", "0.5", "7.0", ""},
                {"DROP", "52", "weaver_drop", "50", "10.0", "2.5", "0.5", "7.0", ""},
                {"FURY", "12", "weaver_fury", "35", "7.0", "1.4", "0.0", "7.0", ""},
                {"SPINDLE", "60", "weaver_spindle", "45", "13.0", "6.5", "0.0", "8.0", "1"}}) {
            StarWeaverEntity.Attack attack = StarWeaverEntity.Attack.valueOf(shot[0]);
            int tick = Integer.parseInt(shot[1]);
            if (started.add(shot[0])) {
                steps.add(stage(g, Double.parseDouble(shot[7]), !shot[8].isEmpty()));
                steps.add(Step.waitTicks(2));
                steps.add(Step.server(any -> {
                    if (voidLevel(any).getEntity(g.weaver) instanceof StarWeaverEntity weaver) {
                        weaver.forceAttack(attack);
                    }
                }));
            }
            steps.add(attackAt(g, attack, tick - 3));
            steps.add(follow(g, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]),
                    Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot(shot[2]));
        }
        // besiegt
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(any -> {
            ServerLevel level = voidLevel(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(40, 30, 40),
                    e -> e instanceof SpellFxEntity || e instanceof StarWebShotEntity || e instanceof StarSpiderlingEntity || e.getId() == g.knight)) {
                e.discard();
            }
            if (level.getEntity(g.weaver) instanceof StarWeaverEntity weaver) {
                Vec3 home = rel(g, 0.0, 0.0, 3.0);
                weaver.forceAttack(StarWeaverEntity.Attack.NONE);
                weaver.snapTo(home.x, home.y, home.z, 180.0f, 0.0f);
                weaver.setYBodyRot(180.0f);
                weaver.setDeltaMovement(Vec3.ZERO);
                weaver.setInvulnerableTime(0);
                weaver.setHealth(1.0f);
                weaver.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(look(g, 7.0, 2.6, 5.0, 0.0, 1.0, 3.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("weaver_death"));
        steps.add(Step.waitTicks(StarWeaverEntity.DEATH_TICKS));
        steps.add(Step.server(any -> {
            for (ItemEntity item : voidLevel(any).getEntitiesOfClass(ItemEntity.class, new AABB(g.center).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(look(g, 3.6, 2.2, 1.6, 0.0, 0.4, 0.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("web_nest_cold"));
        // die Rüstung der Sternwandler mit dem Sternenbogen am Rüstungsständer
        steps.add(Step.server(any -> {
            ServerLevel level = voidLevel(any);
            Vec3 p = rel(g, 6.0, 0.0, -4.0);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            stand.setNoBasePlate(true);
            stand.setNoGravity(true);
            stand.setRightArmPose(new Rotations(-20, 0, 8));
            Vec3 cam = rel(g, 9.6, 1.9, -2.6);
            float yaw = (float) Math.toDegrees(Math.atan2(-(cam.x - p.x), cam.z - p.z)) + 20.0f;
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setYHeadRot(yaw);
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(VoidGear.STARWALKER_HELMET.get()));
            stand.setItemSlot(EquipmentSlot.CHEST, new ItemStack(VoidGear.STARWALKER_CHESTPLATE.get()));
            stand.setItemSlot(EquipmentSlot.LEGS, new ItemStack(VoidGear.STARWALKER_LEGGINGS.get()));
            stand.setItemSlot(EquipmentSlot.FEET, new ItemStack(VoidGear.STARWALKER_BOOTS.get()));
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(StarWeaver.STAR_BOW.get()));
            stand.addTag(TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(look(g, 9.6, 1.9, -2.6, 6.0, 1.1, -4.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("starwalker_bow"));
        steps.add(look(g, 7.6, 1.6, -3.4, 6.0, 1.3, -4.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("starwalker_bow_close"));
        steps.add(Step.server(any -> {
            for (Entity e : voidLevel(any).getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(48, 40, 48), e -> e.entityTags().contains(TAG))) {
                e.discard();
            }
        }));
    }

    private static void shot(Deque<Step> steps, String name, boolean first) {
        if (first) {
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(1500));
        }
        steps.add(Step.waitTicks(first ? 60 : 30));
        steps.add(Step.screenshot(name));
    }

    /** Sucht das nächste Sternennetz (notfalls baut er selbst eines in den Weiten) und lässt seine Chunks entstehen. */
    private static void locate(ServerLevel level, Web g) {
        if (level == null) {
            g.ready = true;
            return;
        }
        var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.STAR_WEB_STRUCTURE);
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(structure), new BlockPos(0, 110, 0), 60,
                false);
        if (found != null) {
            BlockPos pos = found.getFirst();
            var chunk = level.getChunk(pos.getX() >> 4, pos.getZ() >> 4, ChunkStatus.STRUCTURE_STARTS);
            StructureStart start = chunk.getStartForStructure(structure.value());
            if (start != null && start.isValid() && start.getPieces().getFirst() instanceof StarWebPiece piece) {
                g.center = piece.center();
            }
        }
        if (g.center == null) {
            build(level, g);
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced((g.center.getX() >> 4) + dx, (g.center.getZ() >> 4) + dz, true);
                level.getChunk((g.center.getX() >> 4) + dx, (g.center.getZ() >> 4) + dz);
            }
        }
        g.nest = StarWebPiece.nest(g.center);
        KingdomsAtWar.LOGGER.info("[Showcase] Sternennetz bei {}, Gelege {} ({})", g.center, g.nest, level.getBlockState(g.nest));
        g.ready = true;
    }

    /** Notfalls: einen freien Raum in den Weiten suchen (wie beim Erzeugen der Welt) und dort selbst bauen. */
    private static void build(ServerLevel level, Web g) {
        var generator = level.getChunkSource().getGenerator();
        var random = level.getChunkSource().randomState();
        var biomes = generator.getBiomeSource().createUncachedResolver(random);
        var expanse = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(ModTags.Biomes.HAS_STAR_WEB);
        BlockPos spot = null;
        for (int r = 0; r < 24 && spot == null; r++) {
            for (int i = -r; i <= r && spot == null; i++) {
                for (int j = -r; j <= r && spot == null; j++) {
                    if (Math.max(Math.abs(i), Math.abs(j)) != r) {
                        continue;
                    }
                    spot = StarWebStructure.find(generator, level, random, biomes, expanse::contains, new ChunkPos(i * 8, j * 8), 7L * i + j);
                }
            }
        }
        if (spot == null) {
            spot = new BlockPos(8, 118, 8);
        }
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.getChunk((spot.getX() >> 4) + dx, (spot.getZ() >> 4) + dz);
            }
        }
        long seed = 99_011L;
        StarWebPiece.build(level, StarWebPiece.makeBox(spot), spot, seed, net.minecraft.util.RandomSource.create(seed), null);
        g.center = spot;
        KingdomsAtWar.LOGGER.info("[Showcase] kein Sternennetz in Reichweite – selbst gebaut bei {}", spot);
    }

    /** Fremde Wesen fort, damit nichts durchs Bild läuft. */
    private static void clean(ServerLevel level, Web g) {
        if (level == null || g.center == null) {
            return;
        }
        for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(g.center).inflate(48, 30, 48))) {
            e.discard();
        }
    }

    /** Ein Punkt relativ zur Mitte: {@code along} nach Süden, {@code up} über der Netzplatte, {@code side} nach Osten. */
    private static Vec3 rel(Web g, double along, double up, double side) {
        return new Vec3(g.center.getX() + 0.5 + side, g.center.getY() + up, g.center.getZ() + 0.5 + along);
    }

    /** Kamera an (along, up, side), Blick auf (talong, tup, tside) – in der Leere. */
    private static Step look(Web g, double along, double up, double side, double talong, double tup, double tside) {
        return Step.server(any -> {
            ServerLevel level = voidLevel(any);
            if (g.center == null || level == null || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 eye = rel(g, along, up, side);
            Vec3 look = rel(g, talong, tup, tside).subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /**
     * Stellt Weberin und Ritter für den nächsten Angriff auf: die Weberin auf dem Netz (nach Osten blickend), der Ritter
     * {@code ahead} Blöcke vor ihr; beide geheilt, Reste früherer Angriffe fort.
     */
    private static Step stage(Web g, double ahead, boolean enraged) {
        return Step.server(any -> {
            ServerLevel level = voidLevel(any);
            for (Entity e : level.getEntitiesOfClass(Entity.class, new AABB(g.center).inflate(40, 30, 40),
                    e -> e instanceof SpellFxEntity || e instanceof StarWebShotEntity || e instanceof StarSpiderlingEntity)) {
                e.discard();
            }
            if (!(level.getEntity(g.weaver) instanceof StarWeaverEntity weaver) || !(level.getEntity(g.knight) instanceof LivingEntity knight)) {
                return;
            }
            Vec3 k = rel(g, 4.0, 0.0, -4.0 + ahead);
            knight.teleportTo(k.x, k.y, k.z);
            knight.setDeltaMovement(Vec3.ZERO);
            knight.setHealth(knight.getMaxHealth());
            knight.removeAllEffects();
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            Vec3 a = rel(g, 4.0, 0.0, -4.0);
            float yaw = -90.0f;
            weaver.forceAttack(StarWeaverEntity.Attack.NONE);
            weaver.snapTo(a.x, a.y, a.z, yaw, 0.0f);
            weaver.setYBodyRot(yaw);
            weaver.setYHeadRot(yaw);
            weaver.setDeltaMovement(Vec3.ZERO);
            weaver.setHealth(enraged ? weaver.getMaxHealth() * 0.6f : weaver.getMaxHealth());
            if (enraged) {
                weaver.forceEnraged();
            }
            weaver.setTarget(knight);
        });
    }

    /** Wartet, bis die Weberin (im Client) den Angriff {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn beendet hat. */
    private static Step attackAt(Web g, StarWeaverEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(g.weaver) instanceof StarWeaverEntity weaver)) {
                return false;
            }
            if (weaver.currentAttack() == attack) {
                seen[0] = true;
                return weaver.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 400);
    }

    /**
     * Kamera bei der Weberin: im Abstand {@code distance}, um {@code angle} Grad aus der Linie Weberin → Ritter gedreht,
     * {@code height} über ihr; Blick auf sie ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Web g, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = voidLevel(any);
            if (level == null || !(level.getEntity(g.weaver) instanceof Entity weaver) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = weaver.position().add(0.0, 1.2, 0.0);
            Vec3 k = level.getEntity(g.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(0.0, 0.0, 1.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(-angle));
            Vec3 eye = focus.add(dir.scale(distance)).add(0.0, height, 0.0);
            eye = new Vec3(eye.x, Math.max(eye.y, g.center.getY() + 1.0), eye.z);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }
}
