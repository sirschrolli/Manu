package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.castle.RavenBannerBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.castle.RobberKnightEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.castle.MorningStarItem;
import io.github.sirschrolli.kingdomsatwar.registry.CastleGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code raubritter_boss}, seit 1.8): Kuno von Rabenfels, der Raubritter, in seiner Raubritterburg – in der
 * flachen Testwelt auf ihrem Hügel im dunklen Wald, an einem klaren Morgen. Die Burg von außen, der Hof mit dem Ritter zu Ross
 * unter dem Rabenbanner, das Banner nah; sein Erwachen und die Bossleiste; dann auf der Wiese vor der Burg seine Angriffe gegen
 * einen (zähen) Ritter: Lanzengang, Lanzenstich, Steigen, Hornruf, Bolzenhagel; der Abwurf unter halber Kraft; zu Fuß
 * Morgensternschlag, Wirbel, Schildstoß und Schildwall; sein Tod und das zerrissene Banner; zuletzt der Morgenstern – am
 * Rüstungsständer, in der Hand (dritte Person vorn und hinten, erste Person) und der Schmetterschlag.
 */
final class RobberKnightShowcase {
    private static final String STAGE_TAG = "kaw_robber_stage";
    private static final int OX = 0;
    private static final int OZ = 1200;
    /** Die Wiese vor der Burg, auf der er vorführt: Standhöhe und Linie (eigene Koordinaten der Burg). */
    private static final double FIELD_Y = -4.0;
    private static final double FIELD_Z = 38.0;

    private RobberKnightShowcase() {}

    private static final class Stage {
        BlockPos origin;
        BlockPos banner;
        int knight = -1;
        int soldier = -1;
        boolean ready;
    }

    static void boss(Deque<Step> steps) {
        Stage stage = new Stage();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(10);
            minecraft.options.gamma().set(1.0);
            ShowcaseRunner.hideHand = true;
        }));
        for (String command : new String[] {"gamerule spawn_monsters false", "gamerule spawn_wardens false", "difficulty normal",
                "gamerule advance_time false", "time set 2600", "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(level -> build(level, stage)));
        steps.add(Step.until(() -> stage.ready, 6000));
        // die Burg auf ihrem Hügel im dunklen Wald, von Süden (über dem Graben das Torhaus, die Türme, der Bergfried)
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(at(stage, 5.0, 8.0, 42.0, 0.0, 7.0, 0.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(level -> {
            // seine Knechte gehen schlafen (sie stünden sonst im Bild oder gingen auf den Ritter los, der hier vorführt)
            for (Entity e : level.getEntitiesOfClass(Mob.class, area(stage), m -> !(m instanceof RobberKnightEntity))) {
                e.discard();
            }
            if (level.getBlockEntity(stage.banner) instanceof RavenBannerBlockEntity banner) {
                RobberKnightEntity knight = banner.knight(level);
                if (knight == null) {
                    knight = banner.spawn(level, false);
                }
                if (knight != null) {
                    stage.knight = knight.getId();
                }
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("raubritterburg"));
        // im Hof: er hält zu Ross Hof unter dem Rabenbanner (vom Tor her, dann das Banner, dann er selbst)
        steps.add(at(stage, 0.0, 3.3, 2.6, -0.8, 2.9, -5.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.screenshot("raubritter_hof"));
        steps.add(at(stage, -3.4, 2.9, -1.6, -2.0, 2.9, -6.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("rabenbanner"));
        steps.add(at(stage, -2.8, 3.8, -0.2, -0.3, 3.1, -4.2));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("raubritter_nah"));
        // er erwacht: das Ross steigt, er reckt die Lanze
        steps.add(Step.server(level -> {
            LivingEntity soldier = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(stage, 0.0, 1.0, 9.0), 180.0f);
            soldier.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            soldier.setHealth(1000.0f);
            stage.soldier = soldier.getId();
            if (level.getBlockEntity(stage.banner) instanceof RavenBannerBlockEntity banner) {
                banner.awaken(level);
            }
        }));
        steps.add(at(stage, 3.2, 2.5, -2.4, -0.2, 3.5, -4.2));
        steps.add(Step.waitTicks(28));
        steps.add(Step.screenshot("raubritter_erwacht"));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(16));
        steps.add(Step.screenshot("raubritter_bossleiste"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(30));
        // hinaus vor die Burg: er kämpft auf der Wiese vor dem Graben (hier ist sein Platz für die Vorführung)
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.knight) instanceof RobberKnightEntity knight) {
                knight.bindTo(BlockPos.containing(local(stage, 0.0, FIELD_Y, FIELD_Z)), 270.0f, stage.banner);
            }
        }));
        // der Lanzengang (von der Seite, wo er gerade vorbeisprengt; hinten die Burg)
        steps.add(calm(stage));
        steps.add(place(stage, "CHARGE", true, -11.0, 4.0));
        steps.add(at(stage, -3.0, FIELD_Y + 1.6, FIELD_Z + 7.5, -4.0, FIELD_Y + 2.0, FIELD_Z));
        steps.add(Step.waitTicks(RobberKnightEntity.CHARGE_AIM + 5));
        steps.add(Step.screenshot("raubritter_lanzengang"));
        steps.add(Step.waitTicks(50));
        // die Angriffe zu Ross: beide an ihre Plätze (er im Westen, der Gegner östlich von ihm), dann der Angriff
        // {Angriff, Tick des Bildes, Name, Blickwinkel zur Linie Ritter–Gegner (Grad), Abstand, Höhe, Abstand des Gegners}
        // (das Bild entsteht etwa sechs Ticks nach der angegebenen Zahl: die Schritte dazwischen brauchen je einen Tick)
        // (negative Winkel: die Kamera steht südlich, die Burg im Hintergrund)
        for (String[] shot : new String[][] {{"THRUST", "9", "raubritter_lanzenstich", "-62", "7.5", "0.4", "4.6"},
                {"TRAMPLE", "10", "raubritter_steigen", "-58", "8.0", "0.2", "4.5"},
                {"HORN", "30", "raubritter_hornruf", "-30", "11.0", "2.0", "7.0"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(calm(stage));
            steps.add(place(stage, shot[0], true, -4.0, -4.0 + Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(56));
            steps.add(Step.server(level -> clear(level, stage)));
        }
        // der Bolzenhagel: von oben, die roten Zeichen um den Gegner, dann die Bolzen
        steps.add(calm(stage));
        steps.add(place(stage, "VOLLEY", true, -6.0, 3.0));
        steps.add(at(stage, -1.0, FIELD_Y + 6.5, FIELD_Z + 10.5, -0.5, FIELD_Y, FIELD_Z));
        steps.add(Step.waitTicks(RobberKnightEntity.VOLLEY_MARK + 10));
        steps.add(Step.screenshot("raubritter_bolzenhagel_zeichen"));
        steps.add(Step.waitTicks(RobberKnightEntity.VOLLEY_FIRE - RobberKnightEntity.VOLLEY_MARK - 10));
        steps.add(Step.screenshot("raubritter_bolzenhagel"));
        steps.add(Step.waitTicks(50));
        steps.add(Step.server(level -> clear(level, stage)));
        // unter halber Kraft wirft ihn das Ross ab
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.knight) instanceof RobberKnightEntity knight) {
                Vec3 p = local(stage, -2.0, FIELD_Y, FIELD_Z);
                knight.forceAttack(RobberKnightEntity.Attack.NONE);
                knight.setDeltaMovement(Vec3.ZERO);
                knight.teleportTo(p.x, p.y, p.z);
                knight.setYRot(270.0f);
                knight.setYBodyRot(270.0f);
                knight.setYHeadRot(270.0f);
                knight.setHealth(knight.getMaxHealth() * 0.46f);
            }
            if (level.getEntity(stage.soldier) instanceof Entity soldier) {
                Vec3 k = local(stage, 6.0, FIELD_Y, FIELD_Z);
                soldier.teleportTo(k.x, k.y, k.z);
            }
        }));
        steps.add(at(stage, -1.0, FIELD_Y + 1.8, FIELD_Z + 7.5, -2.0, FIELD_Y + 1.8, FIELD_Z));
        steps.add(Step.waitTicks(13));
        steps.add(Step.screenshot("raubritter_abwurf"));
        steps.add(Step.waitTicks(10));
        steps.add(at(stage, 3.0, FIELD_Y + 1.7, FIELD_Z + 2.6, -2.2, FIELD_Y + 0.5, FIELD_Z));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("raubritter_gestuerzt"));
        steps.add(Step.waitTicks(RobberKnightEntity.Attack.UNHORSE.duration));
        steps.add(Step.server(level -> {
            for (Entity e : level.getEntitiesOfClass(Entity.class, area(stage), e -> e.entityTags().contains("kaw_robber_horse"))) {
                e.discard();
            }
        }));
        // zu Fuß: der Morgenstern
        for (String[] shot : new String[][] {{"SMASH", "8", "raubritter_ausholen", "-70", "5.5", "0.2", "3.2"},
                {"SMASH", "15", "raubritter_morgensternschlag", "-70", "5.5", "0.2", "3.2"},
                {"WHIRL", "16", "raubritter_wirbel", "-60", "6.0", "0.6", "3.2"},
                {"BASH", "7", "raubritter_schildstoss", "-75", "5.5", "0.0", "2.6"},
                {"WALL", "16", "raubritter_schildwall", "-40", "5.5", "0.2", "4.0"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(calm(stage));
            steps.add(place(stage, shot[0], false, -3.0, -3.0 + Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            steps.add(Step.waitTicks(60));
            steps.add(Step.server(level -> clear(level, stage)));
        }
        // sein Tod; das Banner reißt
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.soldier) instanceof Entity soldier) {
                soldier.discard();
            }
            clear(level, stage);
            if (level.getEntity(stage.knight) instanceof RobberKnightEntity knight) {
                Vec3 p = local(stage, -1.0, FIELD_Y, FIELD_Z);
                knight.teleportTo(p.x, p.y, p.z);
                knight.setYRot(300.0f);
                knight.forceAttack(RobberKnightEntity.Attack.NONE);
                knight.setInvulnerableTime(0);
                knight.setHealth(1.0f);
                knight.hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(at(stage, 1.2, FIELD_Y + 1.8, FIELD_Z + 3.8, -1.0, FIELD_Y + 0.4, FIELD_Z));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("raubritter_tod"));
        steps.add(Step.waitTicks(RobberKnightEntity.DEATH_TICKS));
        steps.add(at(stage, -2.9, 2.9, -2.6, -2.0, 3.0, -6.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("rabenbanner_zerrissen"));
        // der Morgenstern am Rüstungsständer (die Beute fort, sie läge sonst im Bild)
        steps.add(Step.server(level -> {
            for (var e : level.getEntitiesOfClass(ItemEntity.class, area(stage))) {
                e.discard();
            }
            Vec3 at = local(stage, 2.0, FIELD_Y, FIELD_Z + 2.0);
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            stand.setShowArms(true);
            stand.setYRot(0.0f);
            stand.setYBodyRot(0.0f);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CastleGear.MORNING_STAR.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 2.9, FIELD_Y + 1.6, FIELD_Z + 4.1, 2.3, FIELD_Y + 0.9, FIELD_Z + 2.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("morgenstern_staender"));
        // der Morgenstern in der Hand: dritte Person (vorn, hinten), erste Person
        steps.add(Step.server(level -> {
            for (var e : level.getEntitiesOfClass(ArmorStand.class, area(stage))) {
                e.discard();
            }
        }));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(CastleGear.MORNING_STAR.get()));
            player.getInventory().setItem(1, new ItemStack(ModItems.STEEL_INGOT.get(), 4));
            player.getInventory().setItem(2, new ItemStack(CastleGear.RAVEN_BANNER.get()));
            player.getInventory().setItem(3, new ItemStack(CastleGear.ROBBER_KNIGHT_SPAWN_EGG.get()));
            player.getInventory().setItem(4, new ItemStack(net.minecraft.world.item.Items.BANNER.pick(net.minecraft.world.item.DyeColor.BLACK)));
        }));
        steps.add(Step.once(minecraft -> minecraft.player.getInventory().setSelectedSlot(0)));
        steps.add(tp(stage, 2.0, FIELD_Y, FIELD_Z + 2.0, 20.0f, 6.0f));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.options.fov().set(60);
        }));
        steps.add(Step.waitTicks(10));
        steps.add(body(35.0f));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("morgenstern_spieler_vorn"));
        steps.add(tp(stage, 2.0, FIELD_Y, FIELD_Z + 2.0, 200.0f, 6.0f));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(10));
        steps.add(body(35.0f));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("morgenstern_spieler_hinten"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.fov().set(70);
            ShowcaseRunner.hideHand = false;
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.getAbilities().invulnerable = true;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }));
        steps.add(tp(stage, 2.0, FIELD_Y, FIELD_Z + 6.0, 168.0f, 4.0f));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("morgenstern_hand"));
        steps.add(Step.hud(false));
        // der Schmetterschlag (von hinten über die Schulter: der Boden reißt vor ihm auf, zwei Gegner fliegen)
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = true;
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
            minecraft.options.fov().set(80);
        }));
        steps.add(tp(stage, 0.0, FIELD_Y, FIELD_Z + 6.0, 180.0f, 22.0f));
        steps.add(Step.server(level -> {
            for (double dx : new double[] {-1.2, 1.4}) {
                LivingEntity dummy = creature(level, ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), local(stage, dx, FIELD_Y, FIELD_Z + 3.0), 0.0f);
                dummy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
                dummy.setHealth(200.0f);
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            MorningStarItem.smash(level, player, MorningStarItem.DAMAGE);
        }));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("morgenstern_schmetterschlag"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.quit());
    }

    /**
     * Baut die Raubritterburg auf ihren Hügel (der Hof liegt vier Blöcke über der Ebene): ringsum Gras statt des Sands der
     * flachen Testwelt, dunkle Eichen im Kreis um die Burg (nicht auf der Wiese im Süden, wo er vorführt), etwas Gras und Farn.
     */
    private static void build(ServerLevel level, Stage stage) {
        long started = System.currentTimeMillis();
        stage.origin = new BlockPos(OX, Showcase.ground + 4, OZ);
        BlockPos o = stage.origin;
        int x0 = o.getX() - 70;
        int x1 = o.getX() + 70;
        int z0 = o.getZ() - 60;
        int z1 = o.getZ() + 90;
        for (int cx = x0 >> 4; cx <= x1 >> 4; cx++) {
            for (int cz = z0 >> 4; cz <= z1 >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
            }
        }
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        int top = o.getY() - 5;
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                level.setBlock(p.set(x, top, z), Blocks.GRASS_BLOCK.defaultBlockState(), 2 | 16);
                level.setBlock(p.set(x, top - 1, z), Blocks.DIRT.defaultBlockState(), 2 | 16);
                level.setBlock(p.set(x, top - 2, z), Blocks.DIRT.defaultBlockState(), 2 | 16);
            }
        }
        SitePiece piece = SiteSamples.robberCastle(o);
        BoundingBox all = new BoundingBox(o.getX() - 40, o.getY() - 20, o.getZ() - 40, o.getX() + 40, o.getY() + 40, o.getZ() + 40);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), all, ChunkPos.containing(o), o);
        stage.banner = o.offset(-2, 1, -6);
        // der dunkle Wald ringsum (nicht auf der Wiese im Süden)
        var features = level.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.FEATURE);
        var random = net.minecraft.util.RandomSource.create(1808L);
        for (int k = 0; k < 90; k++) {
            double a = k * 2.39996;
            double r = 31.0 + (k * 7 % 31);
            int x = (int) Math.round(Math.cos(a) * r);
            int z = (int) Math.round(Math.sin(a) * r);
            if (Math.abs(x) < 26 && z > 18 && z < 75) {
                continue;
            }
            BlockPos at = o.offset(x, -4, z);
            features.getOrThrow(net.minecraft.data.worldgen.features.TreeFeatures.DARK_OAK).value().place(level,
                    level.getChunkSource().getGenerator(), random, at);
        }
        for (int k = 0; k < 900; k++) {
            int x = random.nextInt(x1 - x0) + x0 - o.getX();
            int z = random.nextInt(z1 - z0) + z0 - o.getZ();
            if (x * x + z * z < 28 * 28 || Math.abs(x) < 16 && z > 22 && z < 60) {
                continue;
            }
            BlockPos at = o.offset(x, -4, z);
            if (level.getBlockState(at).isAir() && level.getBlockState(at.below()).is(Blocks.GRASS_BLOCK)) {
                level.setBlock(at, (k % 3 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState(), 2);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Raubritterburg bei {}, Banner {} ({}), {} ms", o, stage.banner, level.getBlockState(stage.banner),
                System.currentTimeMillis() - started);
        stage.ready = true;
    }

    private static AABB area(Stage stage) {
        return new AABB(stage.origin).inflate(40, 24, 40).expandTowards(0, 0, 20);
    }

    /**
     * Stellt Ritter und Gegner auf ihre Plätze auf der Wiese vor der Burg (der Ritter bei {@code kx}, der Gegner bei {@code sx},
     * beide auf der Linie {@link #FIELD_Z}) und beginnt den Angriff ({@code mounted}: zu Ross).
     */
    private static Step place(Stage stage, String attack, boolean mounted, double kx, double sx) {
        return Step.server(level -> {
            if (level.getEntity(stage.knight) instanceof RobberKnightEntity knight && level.getEntity(stage.soldier) instanceof LivingEntity soldier) {
                Vec3 p = local(stage, kx, FIELD_Y, FIELD_Z);
                Vec3 k = local(stage, sx, FIELD_Y, FIELD_Z);
                if (!mounted && knight.isMounted()) {
                    knight.forceOnFoot();
                }
                knight.teleportTo(p.x, p.y, p.z);
                knight.setDeltaMovement(Vec3.ZERO);
                soldier.teleportTo(k.x, k.y, k.z);
                soldier.setDeltaMovement(Vec3.ZERO);
                soldier.resetFallDistance();
                soldier.setHealth(soldier.getMaxHealth());
                knight.setTarget(soldier);
                knight.lookAt(soldier, 360.0f, 360.0f);
                knight.setYBodyRot(knight.getYRot());
                knight.setYHeadRot(knight.getYRot());
                knight.forceAttack(RobberKnightEntity.Attack.valueOf(attack));
            }
        });
    }

    /**
     * Beendet seinen Angriff einen Tick vor dem nächsten (sonst sähe der Client nicht, dass ein Angriff von vorn beginnt, wenn
     * es derselbe ist wie zuvor).
     */
    private static Step calm(Stage stage) {
        return Step.server(level -> {
            if (level.getEntity(stage.knight) instanceof RobberKnightEntity knight) {
                knight.forceAttack(RobberKnightEntity.Attack.NONE);
            }
        });
    }

    /** Räumt Beute, Bolzen, Wirkungen und seine Leute fort (ein neuer Angriff beginnt ohne sie). */
    private static void clear(ServerLevel level, Stage stage) {
        for (var e : level.getEntitiesOfClass(Entity.class, area(stage), e -> e instanceof ItemEntity || e instanceof AbstractArrow
                || e.getType() == ModEntities.SPELL_FX.get() || e.entityTags().contains("kaw_robber_men"))) {
            e.discard();
        }
    }

    /** Kamera beim Ritter: Abstand, Drehung aus der Linie Ritter → Gegner (Grad), Höhe über seiner Brust. */
    private static Step follow(Stage stage, double angle, double distance, double height) {
        return Step.server(level -> {
            if (!(level.getEntity(stage.knight) instanceof RobberKnightEntity knight) || level.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = knight.position().add(0.0, knight.isMounted() ? 2.4 : 1.4, 0.0);
            Vec3 toward = level.getEntity(stage.soldier) instanceof Entity soldier ? soldier.position().subtract(knight.position())
                    .multiply(1.0, 0.0, 1.0) : new Vec3(0.0, 0.0, 1.0);
            if (toward.lengthSqr() < 1.0e-3) {
                toward = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 dir = toward.normalize().yRot((float) Math.toRadians(angle));
            Vec3 focus = c.add(toward.normalize().scale(Math.min(2.5, toward.length() * 0.4)));
            Vec3 eye = c.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Ein Punkt im Rahmen der Burg (Mitte des Hofs bei 0, 0, 0; man steht auf y = 1; das Tor liegt bei +z) in Weltkoordinaten. */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        return new Vec3(stage.origin.getX() + x + 0.5, stage.origin.getY() + y, stage.origin.getZ() + z + 0.5);
    }

    /** Dreht den Körper des Spielers gegen den Kopf (wie wenn er nur den Kopf wendet): Die Waffe in der Hand zeigt ihre Länge. */
    private static Step body(float offset) {
        return Step.once(minecraft -> {
            float yaw = minecraft.player.getYRot() + offset;
            minecraft.player.setYBodyRot(yaw);
            minecraft.player.yBodyRotO = yaw;
        });
    }

    /** Der Spieler selbst an den Punkt (x, y, z), mit Blick (Grad). */
    private static Step tp(Stage stage, double x, double y, double z, float yaw, float pitch) {
        return Step.once(minecraft -> {
            if (stage.origin == null) {
                return;
            }
            Vec3 p = local(stage, x, y, z);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", p.x, p.y, p.z, yaw, pitch));
        });
    }

    /** Kamera am Punkt (dx, dy, dz) der Burg (Augenhöhe), Blick auf (tx, ty, tz). */
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

    private static <T extends Mob> T creature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, Vec3 at, float yaw) {
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
