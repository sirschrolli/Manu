package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.serpent.SerpentAltarBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.ShedSkinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.SerpentGear;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Rotations;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code quetzara_boss}, seit 1.10): Quetzara, die Gefiederte Schlange, im Schlangentempel – in der flachen
 * Testwelt auf einem Hügel im Dschungel gebaut (die Grube liegt in ihm). Der Tempel von außen, die Grube von der Empore mit der
 * schlafenden Schlange auf ihrer Insel, sie schlafend aus der Nähe; ihr Erwachen mit Titel und Bossleiste; dann ihre Angriffe
 * gegen einen (zähen) Ritter: Giftbiss, Würgegriff, Schwanzpeitsche, Federsturm, Giftspucke; die Häutung (die leere Haut, sie
 * selbst getarnt) und ihr neues Kleid; ihr Tod und der zerbrochene Altar; zuletzt der Schlangenzahn am Ständer.
 */
final class QuetzaraShowcase {
    private static final String STAGE_TAG = "kaw_quetzara_stage";
    private static final int OX = 0;
    private static final int OZ = 1800;

    private QuetzaraShowcase() {}

    private static final class Stage {
        BlockPos origin;
        BlockPos altar;
        int serpent = -1;
        int soldier = -1;
        boolean ready;
    }

    static void boss(Deque<Step> steps) {
        Stage stage = new Stage();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(8);
            minecraft.options.gamma().set(1.0);
            ShowcaseRunner.hideHand = true;
        }));
        for (String command : new String[] {"gamerule spawn_monsters false", "gamerule spawn_wardens false", "difficulty normal",
                "gamerule advance_time false", "time set 4500", "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(level -> build(level, stage)));
        steps.add(Step.until(() -> stage.ready, 6000));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // der Tempel von außen: der Kopf der steinernen Schlange im Dschungel, das Maul offen
        steps.add(at(stage, -13.0, 7.0, -13.0, 0.0, 3.5, 6.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(level -> {
            if (level.getBlockEntity(stage.altar) instanceof SerpentAltarBlockEntity altar) {
                QuetzaraEntity serpent = altar.serpent(level);
                if (serpent == null) {
                    serpent = altar.spawn(level, false);
                }
                if (serpent != null) {
                    stage.serpent = serpent.getId();
                }
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("schlangentempel"));
        // die Grube von der Empore: die Insel im Graben, darauf der Altar, um ihn schläft sie
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(80)));
        steps.add(at(stage, 0.0, -5.2, 20.0, 0.0, -12.0, 38.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("schlangengrube"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // sie schläft, aus der Nähe
        steps.add(at(stage, -4.6, -9.6, 33.4, 0.0, -11.6, 39.6));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quetzara_schlaf"));
        // sie erwacht: richtet sich auf, spreizt die Krause, kreischt
        steps.add(Step.server(level -> {
            LivingEntity soldier = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(stage, 0.0, -13.0, 49.0), 180.0f);
            soldier.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            soldier.setHealth(1000.0f);
            stage.soldier = soldier.getId();
            if (level.getBlockEntity(stage.altar) instanceof SerpentAltarBlockEntity altar) {
                altar.awaken(level);
            }
        }));
        steps.add(at(stage, -3.4, -10.2, 30.6, 0.0, -8.4, 38.6));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("quetzara_erwacht"));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("quetzara_bossleiste"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(60));
        // ihre Angriffe: {Angriff, Tick des Bildes, Name, Lage der Schlange (x, z, Blick, Krümmung), Lage des Ritters (x, y, z),
        // Kamera (x, y, z), Blickziel (x, y, z)}
        for (String[] shot : new String[][] {
                {"BITE", "9", "quetzara_biss_ausholen", "0,43,0,9", "0,-13,49", "-5.5,-10.0,46.0", "0,-10.4,45.5"},
                {"BITE", "15", "quetzara_giftbiss", "0,43,0,9", "0,-13,49", "-5.5,-10.0,46.0", "0,-11.2,46.5"},
                {"SPIT", "16", "quetzara_giftspucke", "0,43,0,9", "0,-13,52", "-6.5,-9.6,45.0", "0,-10.5,48.0"},
                {"FEATHERS", "23", "quetzara_federsturm", "0,43,0,9", "0,-13,52", "-7.0,-9.0,45.5", "0,-8.6,45.0"},
                {"WHIP", "15", "quetzara_schwanzpeitsche", "2,44,-90,-14", "-4,-12,38", "-1.0,-6.0,50.0", "-1.0,-11.0,40.0"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(calm(stage));
            steps.add(place(stage, shot[0], shot[3], shot[4]));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            String[] eye = shot[5].split(",");
            String[] look = shot[6].split(",");
            steps.add(at(stage, Double.parseDouble(eye[0]), Double.parseDouble(eye[1]), Double.parseDouble(eye[2]), Double.parseDouble(look[0]),
                    Double.parseDouble(look[1]), Double.parseDouble(look[2])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(40));
            steps.add(Step.server(level -> clear(level, stage)));
        }
        // der Würgegriff: sie umkreist den Ritter auf der Insel, rollt sich um ihn und drückt zu
        if (Step.wanted("quetzara_wuergegriff")) {
            steps.add(calm(stage));
            steps.add(place(stage, "CONSTRICT", "-3,38,180,6", "-3,-12,41.5"));
            steps.add(Step.waitTicks(46));
            steps.add(at(stage, -9.0, -7.4, 45.0, -3.0, -10.6, 41.5));
            steps.add(Step.waitTicks(14));
            steps.add(Step.screenshot("quetzara_wuergegriff"));
            steps.add(Step.waitTicks(60));
            steps.add(Step.server(level -> clear(level, stage)));
        }
        // die Häutung: die leere Haut bleibt liegen, sie selbst gleitet getarnt davon
        steps.add(calm(stage));
        steps.add(place(stage, "MOLT", "0,43,0,9", "0,-13,49"));
        steps.add(at(stage, -6.0, -8.8, 46.0, 0.0, -11.0, 41.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quetzara_haeutung"));
        steps.add(Step.waitTicks(QuetzaraEntity.MOLT_SPLIT + 16));
        steps.add(at(stage, -7.0, -8.0, 47.0, 0.0, -11.0, 41.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("quetzara_haut"));
        // ihr neues Kleid (gehäutet, nicht mehr getarnt)
        steps.add(Step.server(level -> {
            for (Entity e : level.getEntitiesOfClass(ShedSkinEntity.class, area(stage))) {
                e.discard();
            }
        }));
        steps.add(calm(stage));
        steps.add(place(stage, "FEATHERS", "0,43,0,9", "0,-13,52"));
        steps.add(Step.waitTicks(20));
        steps.add(at(stage, -5.5, -9.2, 47.5, 0.0, -8.6, 44.5));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("quetzara_neues_kleid"));
        steps.add(Step.waitTicks(50));
        steps.add(Step.server(level -> clear(level, stage)));
        // ihr Tod: sie bäumt sich auf und zerfällt; das Schlangenbild auf dem Altar zerspringt
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.soldier) instanceof Entity soldier) {
                soldier.discard();
            }
            if (level.getEntity(stage.serpent) instanceof QuetzaraEntity serpent) {
                serpent.layOut(local(stage, 0.0, -12.0, 43.0), 0.0f, 9.0f);
                serpent.forceAttack(QuetzaraEntity.Attack.NONE);
                serpent.setInvulnerableTime(0);
                serpent.setHealth(1.0f);
                serpent.hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(at(stage, -6.0, -9.0, 47.0, 0.0, -10.0, 42.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("quetzara_tod"));
        steps.add(Step.waitTicks(QuetzaraEntity.DEATH_TICKS + 10));
        steps.add(at(stage, -2.2, -10.4, 37.6, 0.0, -11.4, 40.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("schlangenaltar_zerbrochen"));
        // der Schlangenzahn am Rüstungsständer (die Beute fort)
        steps.add(Step.server(level -> {
            for (var e : level.getEntitiesOfClass(ItemEntity.class, area(stage))) {
                e.discard();
            }
            Vec3 at = local(stage, 3.0, -12.0, 42.0);
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            stand.setShowArms(true);
            stand.setRightArmPose(new Rotations(-60.0f, -12.0f, 0.0f));
            stand.setLeftArmPose(new Rotations(-20.0f, 0.0f, -10.0f));
            stand.setYRot(-150.0f);
            stand.setYBodyRot(-150.0f);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(SerpentGear.SERPENT_FANG.get()));
            stand.setItemSlot(EquipmentSlot.OFFHAND, new ItemStack(SerpentGear.QUETZAL_FEATHER.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 1.2, -10.4, 39.8, 3.0, -10.6, 42.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("schlangenzahn_staender"));
        steps.add(Step.quit());
    }

    /**
     * Baut einen Hügel in die flache Welt (die Grube braucht Tiefe) und darauf den Schlangentempel: die Schnauze bei
     * {@code origin} (Boden), ringsum Dschungelbäume (nicht vor dem Kopf).
     */
    private static void build(ServerLevel level, Stage stage) {
        long started = System.currentTimeMillis();
        stage.origin = new BlockPos(OX, Showcase.ground + 22, OZ);
        BlockPos o = stage.origin;
        int x0 = o.getX() - 44;
        int x1 = o.getX() + 44;
        int z0 = o.getZ() - 40;
        int z1 = o.getZ() + 80;
        for (int cx = x0 >> 4; cx <= x1 >> 4; cx++) {
            for (int cz = z0 >> 4; cz <= z1 >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
            }
        }
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int top = o.getY() - 1;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = Showcase.ground; y <= top; y++) {
                    level.setBlock(p.set(x, y, z), (y == top ? Blocks.GRASS_BLOCK : y > top - 3 ? Blocks.DIRT : Blocks.STONE).defaultBlockState(), 2 | 16);
                }
            }
        }
        // der Dschungel ringsum (nicht vor dem Kopf, wo die Kamera steht)
        var features = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.FEATURE);
        var random = net.minecraft.util.RandomSource.create(1910L);
        for (int k = 0; k < 70; k++) {
            double a = k * 2.39996;
            double r = 20.0 + (k * 7 % 22);
            int x = (int) Math.round(Math.cos(a) * r);
            int z = 10 + (int) Math.round(Math.sin(a) * r * 1.4);
            if (z < 2 && Math.abs(x) < 24 || Math.abs(x) < 17 && z < 32) {
                continue;
            }
            BlockPos at = o.offset(x, 0, z);
            var tree = k % 4 == 0 ? net.minecraft.data.worldgen.features.TreeFeatures.MEGA_JUNGLE_TREE
                    : k % 4 == 1 ? net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_BUSH
                    : net.minecraft.data.worldgen.features.TreeFeatures.JUNGLE_TREE;
            features.getOrThrow(tree).value().place(level, level.getChunkSource().getGenerator(), random, at);
        }
        SitePiece piece = SiteSamples.serpentTemple(o);
        BoundingBox all = new BoundingBox(o.getX() - 40, o.getY() - 20, o.getZ() - 20, o.getX() + 40, o.getY() + 40, o.getZ() + 60);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), all, ChunkPos.containing(o), o);
        stage.altar = SiteSamples.serpentAltar(o);
        // Farn und Gras auf der Lichtung
        for (int k = 0; k < 700; k++) {
            int x = random.nextInt(x1 - x0) + x0 - o.getX();
            int z = random.nextInt(z1 - z0) + z0 - o.getZ();
            BlockPos at = o.offset(x, 0, z);
            if (level.getBlockState(at).isAir() && level.getBlockState(at.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(at, (k % 3 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState(), 2);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Schlangentempel bei {}, Altar {} ({}), {} ms", o, stage.altar, level.getBlockState(stage.altar),
                System.currentTimeMillis() - started);
        stage.ready = true;
    }

    private static AABB area(Stage stage) {
        return new AABB(stage.origin).inflate(40, 30, 40).expandTowards(0, 0, 40);
    }

    /**
     * Legt die Schlange hin ({@code serpent}: x, z, Blick, Krümmung; auf der Höhe der Insel) und den Ritter ({@code soldier}: x,
     * y, z) und beginnt den Angriff.
     */
    private static Step place(Stage stage, String attack, String serpent, String soldier) {
        return Step.server(level -> {
            if (level.getEntity(stage.serpent) instanceof QuetzaraEntity q && level.getEntity(stage.soldier) instanceof LivingEntity knight) {
                String[] s = serpent.split(",");
                String[] k = soldier.split(",");
                Vec3 kp = local(stage, Double.parseDouble(k[0]), Double.parseDouble(k[1]), Double.parseDouble(k[2]));
                knight.teleportTo(kp.x, kp.y, kp.z);
                knight.setDeltaMovement(Vec3.ZERO);
                knight.setHealth(knight.getMaxHealth());
                knight.removeAllEffects();
                Vec3 sp = local(stage, Double.parseDouble(s[0]), -12.0, Double.parseDouble(s[1]));
                q.layOut(sp, Float.parseFloat(s[2]), Float.parseFloat(s[3]));
                q.setTarget(knight);
                q.setMolted(attack.equals("MOLT") ? false : q.isMolted());
                q.forceAttack(QuetzaraEntity.Attack.valueOf(attack));
            }
        });
    }

    /** Beendet ihren Angriff einen Tick vor dem nächsten (sonst sähe der Client nicht, dass derselbe Angriff neu beginnt). */
    private static Step calm(Stage stage) {
        return Step.server(level -> {
            if (level.getEntity(stage.serpent) instanceof QuetzaraEntity q) {
                q.forceAttack(QuetzaraEntity.Attack.NONE);
            }
        });
    }

    /** Räumt Beute, Geschosse und Giftwolken fort (ein neuer Angriff beginnt ohne sie). */
    private static void clear(ServerLevel level, Stage stage) {
        for (var e : level.getEntitiesOfClass(Entity.class, area(stage), e -> e instanceof ItemEntity || e instanceof Projectile
                || e instanceof AreaEffectCloud)) {
            e.discard();
        }
    }

    /** Ein Punkt des Tempels (Schnauze bei 0, 0, 0; der Kopf liegt nach Süden, +z) in Weltkoordinaten. */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        return new Vec3(stage.origin.getX() + x + 0.5, stage.origin.getY() + y, stage.origin.getZ() + z + 0.5);
    }

    /** Kamera am Punkt (dx, dy, dz) des Tempels (Augenhöhe), Blick auf (tx, ty, tz). */
    private static Step at(Stage stage, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (stage.origin == null) {
                return;
            }
            Vec3 eye = local(stage, dx, dy, dz);
            Vec3 target = local(stage, tx, ty, tz);
            Vec3 look = target.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", eye.x, eye.y - 1.62, eye.z, yaw,
                    pitch));
        });
    }

    private static <T extends Mob> T creature(ServerLevel level, EntityType<T> type, Vec3 at, float yaw) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(at.x, at.y, at.z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        return mob;
    }
}
