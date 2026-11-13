package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.end.VoidThroneBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorIllusion;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.end.PhaseBladeItem;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseGear;
import io.github.sirschrolli.kingdomsatwar.registry.PhaseSanctuary;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code phasmor_boss}, seit 1.5): Phasmor, der Leerwandlerkönig, in einem echten Phasenheiligtum auf den
 * äußeren Inseln des Ends – das schwebende Heiligtum von vorn und von unten, der kniende König, der Leerenthron; das
 * Erwachen und die Bossleiste; seine Angriffe gegen einen (zähen) Ritter, die Krönung, der Phasensturm und das Wasser; sein
 * Tod und der erloschene Thron; zuletzt die Phasenklinge – am Rüstungsständer, in der Hand (erste und dritte Person) und
 * der Phasenhieb.
 */
final class PhasmorShowcase {
    private static final String STAGE_TAG = "kaw_phasmor_stage";

    private PhasmorShowcase() {}

    private static final class Stage {
        SitePiece sanctum;
        BlockPos throne;
        int king = -1;
        int knight = -1;
        int state;
    }

    private static ServerLevel end(ServerLevel any) {
        return any.getServer().getLevel(Level.END);
    }

    static void boss(Deque<Step> steps) {
        Stage stage = new Stage();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(end(any), stage)));
        steps.add(Step.until(() -> stage.state != 0, 12000));
        // das Heiligtum von vorn und von unten
        steps.add(at(stage, 6.0, 18.0, 40.0, 0.0, -2.0, -2.0));
        steps.add(Step.waitTicks(80));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(stage.throne).inflate(48, 30, 48), m -> !(m instanceof PhasmorEntity))) {
                e.discard();
            }
            // für freie Sicht: die Chorusgewächse ringsum fort (nur in der Vorführung)
            BlockPos o = stage.sanctum.origin();
            BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
            for (int x = -44; x <= 44; x++) {
                for (int z = -44; z <= 44; z++) {
                    for (int y = -34; y <= 24; y++) {
                        p.set(o.getX() + x, o.getY() + y, o.getZ() + z);
                        var state = level.getBlockState(p);
                        if (state.is(net.minecraft.world.level.block.Blocks.CHORUS_PLANT) || state.is(net.minecraft.world.level.block.Blocks.CHORUS_FLOWER)) {
                            level.setBlock(p, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                        }
                    }
                }
            }
            if (level.getBlockEntity(stage.throne) instanceof VoidThroneBlockEntity throne) {
                PhasmorEntity king = throne.king(level);
                if (king == null) {
                    king = throne.spawn(level, false);
                }
                if (king != null) {
                    stage.king = king.getId();
                }
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("phasmor_heiligtum"));
        steps.add(at(stage, -34.0, 1.0, 12.0, 0.0, -5.0, -1.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("phasmor_heiligtum_unten"));
        steps.add(at(stage, 9.0, 4.0, 7.0, 0.0, 2.5, -9.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("phasmor_heiligtum_innen"));
        // er kniet und hält Wache
        steps.add(at(stage, -3.6, 2.4, -1.2, 0.0, 1.9, -6.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("phasmor_wache"));
        steps.add(at(stage, -3.4, 4.6, -6.6, 0.0, 3.6, -11.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("phasmor_thron"));
        // er erwacht
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getBlockEntity(stage.throne) instanceof VoidThroneBlockEntity throne) {
                throne.awaken(level);
            }
            Vec3 k = local(stage, 0.0, 0.0, 1.0);
            LivingEntity knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), k, 180.0f);
            knight.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000.0);
            knight.setHealth(1000.0f);
            stage.knight = knight.getId();
        }));
        steps.add(at(stage, 2.6, 3.0, -1.2, 0.0, 3.0, -6.0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("phasmor_awaken"));
        steps.add(Step.waitTicks(12));
        steps.add(Step.hud(true));
        // die Leiste senkt sich, entrollt ihr Band und lädt sich auf (gut anderthalb Sekunden)
        steps.add(Step.waitTicks(36));
        steps.add(Step.screenshot("phasmor_bossbar"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(30));
        // die Angriffe: beide an ihre Plätze, dann der Angriff
        // {Angriff, Ticks bis zum Bild, Name, Blickwinkel zur Linie König–Ritter (Grad), Abstand, Höhe, Abstand des Ritters}
        for (String[] shot : new String[][] {{"SLASH", "15", "phasmor_slash", "-80", "8.0", "1.0", "4.0"},
                {"BLINK", "20", "phasmor_blink", "-60", "9.0", "1.4", "6.0"}, {"ILLUSIONS", "24", "phasmor_illusions", "-30", "12.0", "4.0", "6.0"},
                {"HURL", "30", "phasmor_hurl", "-75", "10.0", "1.6", "7.0"}, {"RIFTS", "30", "phasmor_rifts", "-60", "10.0", "5.0", "6.0"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(place(stage, shot[0], Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            if (shot[0].equals("RIFTS")) {
                // die Risse brechen auf
                steps.add(Step.waitTicks(PhasmorEntity.RIFT_ERUPT - Integer.parseInt(shot[1]) + 2));
                steps.add(Step.screenshot("phasmor_rifts_erupt"));
            }
            steps.add(Step.waitTicks(50));
            steps.add(Step.server(any -> clear(end(any), stage)));
        }
        // die Krönung
        steps.add(place(stage, "CROWN", 7.0));
        steps.add(Step.waitTicks(PhasmorEntity.CROWN_FLARE));
        steps.add(follow(stage, -25.0, 7.5, 1.2));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("phasmor_crown"));
        steps.add(Step.waitTicks(50));
        // gekrönt: der Phasensturm
        steps.add(place(stage, "STORM", 6.0));
        steps.add(Step.waitTicks(PhasmorEntity.STORM_BLINKS[1] + 1));
        steps.add(follow(stage, -70.0, 9.0, 2.0));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("phasmor_storm"));
        steps.add(Step.waitTicks(80));
        // Wasser: er krümmt sich
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.king) instanceof PhasmorEntity king) {
                Vec3 p = local(stage, 0.0, 0.0, -3.0);
                king.teleportTo(p.x, p.y, p.z);
                king.setInvulnerableTime(0);
                king.hurtServer(level, level.damageSources().drown(), 1.0f);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(follow(stage, -40.0, 7.5, 1.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("phasmor_water"));
        steps.add(Step.waitTicks(PhasmorEntity.STAGGER_TICKS));
        // sein Tod
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.knight) instanceof Entity knight) {
                knight.discard();
            }
            clear(level, stage);
            if (level.getEntity(stage.king) instanceof PhasmorEntity king) {
                Vec3 p = local(stage, 0.0, 0.0, -3.0);
                king.teleportTo(p.x, p.y, p.z);
                king.forceAttack(PhasmorEntity.Attack.NONE);
                king.setInvulnerableTime(0);
                king.setHealth(1.0f);
                king.hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(at(stage, 4.0, 2.6, 2.0, 0.0, 2.2, -3.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("phasmor_death"));
        steps.add(Step.waitTicks(PhasmorEntity.DEATH_TICKS));
        steps.add(at(stage, -3.4, 4.6, -6.6, 0.0, 3.6, -11.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("phasmor_thron_erloschen"));
        // die Phasenklinge am Rüstungsständer (die Beute fort, sie läge sonst im Bild)
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            for (var e : level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, new AABB(stage.throne).inflate(30, 16, 30))) {
                e.discard();
            }
            Vec3 at = local(stage, 2.0, 0.0, -3.0);
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            float yaw = stage.sanctum.rotation().rotate(net.minecraft.core.Direction.SOUTH).toYRot();
            stand.setShowArms(true);
            stand.setYRot(yaw);
            stand.setYBodyRot(yaw);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(PhaseSanctuary.PHASE_BLADE.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 2.6, 1.9, 0.2, 2.0, 1.2, -3.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("phasenklinge_staender"));
        // die Phasenklinge in der Hand: dritte Person (vorn, hinten), erste Person
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(PhaseSanctuary.PHASE_BLADE.get()));
            player.getInventory().setItem(1, new ItemStack(PhaseGear.PHASIT.get(), 9));
            player.getInventory().setItem(2, new ItemStack(PhaseSanctuary.VOID_THRONE.get()));
            player.getInventory().setItem(3, new ItemStack(PhaseSanctuary.PHASMOR_SPAWN_EGG.get()));
        }));
        steps.add(Step.once(minecraft -> minecraft.player.getInventory().setSelectedSlot(0)));
        steps.add(tp(stage, 0.0, 0.0, 2.0, 180.0f, 8.0f));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.options.fov().set(60);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("phasenklinge_spieler_vorn"));
        steps.add(tp(stage, 0.0, 0.0, 2.0, 140.0f, 6.0f));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("phasenklinge_spieler_hinten"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.options.fov().set(70);
            ShowcaseRunner.hideHand = false;
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.getAbilities().invulnerable = true;
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();
        }));
        steps.add(tp(stage, -1.0, 0.0, 4.0, 160.0f, 12.0f));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("phasenklinge_hand"));
        steps.add(Step.hud(false));
        // der Phasenhieb: durch einen Gegner hindurch (von vorn: hinter dem Spieler die Spur und der Getroffene)
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = true;
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
        }));
        steps.add(tp(stage, 0.0, 0.0, 7.0, 180.0f, 4.0f));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            Vec3 d = local(stage, 0.0, 0.0, 3.0);
            float facing = stage.sanctum.rotation().rotate(net.minecraft.core.Direction.SOUTH).toYRot();
            LivingEntity dummy = creature(level, ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), d, facing);
            dummy.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(200.0);
            dummy.setHealth(200.0f);
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            PhaseBladeItem.dash(level, player, PhaseBladeItem.DASH_DAMAGE);
        }));
        // er blickt zur Seite: die Kamera (vor ihm) zeigt schräg die Spur und den Getroffenen
        steps.add(Step.waitTicks(1));
        steps.add(Step.once(minecraft -> {
            if (stage.sanctum != null) {
                minecraft.player.setYRot(yaw(stage, 138.0f));
                minecraft.player.setXRot(14.0f);
            }
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("phasenhieb"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.quit());
    }

    /** Nur zur Prüfung ({@code phasmor_pose}): der kniende König auf einer kleinen Plattform im End, von allen Seiten. */
    static void pose(Deque<Step> steps) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        int[] id = {-1};
        steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand("execute in minecraft:the_end run tp @s 40.5 104 8 180 20")));
        steps.add(Step.waitTicks(60));
        steps.add(Step.server(any -> {
            ServerLevel level = end(any);
            for (int x = -4; x <= 4; x++) {
                for (int z = -4; z <= 4; z++) {
                    level.setBlockAndUpdate(new BlockPos(40 + x, 100, z), net.minecraft.world.level.block.Blocks.END_STONE_BRICKS.defaultBlockState());
                }
            }
            PhasmorEntity king = ModEntities.PHASMOR.get().create(level, EntitySpawnReason.COMMAND);
            king.snapTo(40.5, 101, 0.5, 0.0f, 0.0f);
            king.yBodyRot = 0.0f;
            king.yHeadRot = 0.0f;
            king.bindTo(new BlockPos(40, 101, 0), null);
            level.addFreshEntity(king);
            id[0] = king.getId();
        }));
        steps.add(Step.waitTicks(40));
        String[] names = {"pose_vorn", "pose_rechts", "pose_hinten", "pose_links", "pose_schraeg"};
        double[][] eyes = {{40.5, 103.2, 7.0}, {47.0, 103.2, 0.5}, {40.5, 103.2, -6.0}, {34.0, 103.2, 0.5}, {45.5, 103.2, 5.5}};
        for (int i = 0; i < names.length; i++) {
            double[] e = eyes[i];
            Vec3 look = new Vec3(40.5, 102.2, 0.5).subtract(e[0], e[1], e[2]);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            steps.add(Step.once(minecraft -> minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in minecraft:the_end run tp @s %.2f %.2f %.2f %.1f %.1f", e[0], e[1] - 1.62, e[2], yaw, pitch))));
            steps.add(Step.waitTicks(20));
            steps.add(Step.screenshot(names[i]));
        }
        steps.add(Step.quit());
    }

    /** Stellt König und Ritter auf ihre Plätze (der Ritter {@code distance} vor ihm) und beginnt den Angriff. */
    private static Step place(Stage stage, String attack, double distance) {
        return Step.server(any -> {
            ServerLevel level = end(any);
            if (level.getEntity(stage.king) instanceof PhasmorEntity king && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                Vec3 p = local(stage, 0.0, 0.0, -4.0);
                Vec3 k = local(stage, 0.0, 0.0, -4.0 + distance);
                king.teleportTo(p.x, p.y, p.z);
                king.setInvisible(false);
                knight.teleportTo(k.x, k.y, k.z);
                knight.setDeltaMovement(Vec3.ZERO);
                knight.setHealth(knight.getMaxHealth());
                king.setTarget(knight);
                king.lookAt(knight, 360.0f, 360.0f);
                king.setYBodyRot(king.getYRot());
                king.setYHeadRot(king.getYRot());
                if (attack.equals("STORM")) {
                    king.forceCrowned();
                }
                king.forceAttack(PhasmorEntity.Attack.valueOf(attack));
            }
        });
    }

    /** Räumt Trugbilder und Brocken fort. */
    private static void clear(ServerLevel level, Stage stage) {
        AABB area = new AABB(stage.throne).inflate(30, 16, 30);
        for (var e : level.getEntitiesOfClass(PhasmorIllusion.class, area)) {
            e.discard();
        }
        for (var e : level.getEntitiesOfClass(Projectile.class, area)) {
            e.discard();
        }
    }

    /** Kamera beim König: Abstand, Drehung aus der Linie König → Ritter (Grad), Höhe über seiner Brust. */
    private static Step follow(Stage stage, double angle, double distance, double height) {
        return Step.server(any -> {
            ServerLevel level = end(any);
            if (level == null || !(level.getEntity(stage.king) instanceof Entity king) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = king.position().add(0.0, 2.6, 0.0);
            Vec3 toward = level.getEntity(stage.knight) instanceof Entity knight ? knight.position().subtract(king.position())
                    .multiply(1.0, 0.0, 1.0) : new Vec3(0.0, 0.0, 1.0);
            if (toward.lengthSqr() < 1.0e-3) {
                toward = new Vec3(0.0, 0.0, 1.0);
            }
            Vec3 dir = toward.normalize().yRot((float) Math.toRadians(angle));
            Vec3 focus = c.add(toward.normalize().scale(Math.min(2.5, toward.length() * 0.35)));
            Vec3 eye = c.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Sucht das nächste Phasenheiligtum, lässt seine Chunks entstehen und merkt sich Heiligtum und Thron. */
    private static void locate(ServerLevel level, Stage stage) {
        if (level == null) {
            stage.state = -1;
            return;
        }
        var holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(SiteKind.PHASE_SANCTUARY));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), new BlockPos(1200, 64, 0), 400,
                false);
        if (found == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] kein Phasenheiligtum gefunden");
            stage.state = -1;
            return;
        }
        ChunkPos chunk = ChunkPos.containing(found.getFirst());
        StructureStart start = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
        if (start == null || !start.isValid()) {
            stage.state = -1;
            return;
        }
        for (var piece : start.getPieces()) {
            if (piece instanceof SitePiece site && site.kind() == SiteKind.PHASE_SANCTUARY && site.part() == 0) {
                stage.sanctum = site;
            }
        }
        if (stage.sanctum == null) {
            stage.state = -1;
            return;
        }
        stage.throne = SiteSamples.phaseSanctumThrone(stage.sanctum);
        int cx = stage.throne.getX() >> 4;
        int cz = stage.throne.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Phasenheiligtum bei {}, Thron bei {} ({})", stage.sanctum.origin(), stage.throne,
                level.getBlockState(stage.throne));
        stage.state = 1;
    }

    /** Ein Punkt im Rahmen des Heiligtums (Mitte der Plattform bei 0, 0, 0; vorn ist +z) in Weltkoordinaten (Blockmitte). */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        BlockPos base = stage.sanctum.origin();
        double[] r = turn(x, z, stage.sanctum.rotation());
        return new Vec3(base.getX() + r[0] + 0.5, base.getY() + y, base.getZ() + r[1] + 0.5);
    }

    private static double[] turn(double x, double z, net.minecraft.world.level.block.Rotation rotation) {
        return switch (rotation) {
            case CLOCKWISE_90 -> new double[] {-z, x};
            case CLOCKWISE_180 -> new double[] {-x, -z};
            case COUNTERCLOCKWISE_90 -> new double[] {z, -x};
            default -> new double[] {x, z};
        };
    }

    /** Der Blickwinkel (Grad) im Rahmen des Heiligtums, in Weltkoordinaten. */
    private static float yaw(Stage stage, float yaw) {
        return switch (stage.sanctum.rotation()) {
            case CLOCKWISE_90 -> yaw + 90.0f;
            case CLOCKWISE_180 -> yaw + 180.0f;
            case COUNTERCLOCKWISE_90 -> yaw - 90.0f;
            default -> yaw;
        };
    }

    /** Der Spieler selbst an den Punkt (x, y, z) des Heiligtums, mit Blick (Grad im Rahmen des Heiligtums). */
    private static Step tp(Stage stage, double x, double y, double z, float yaw, float pitch) {
        return Step.once(minecraft -> {
            if (stage.sanctum == null) {
                return;
            }
            Vec3 p = local(stage, x, y, z);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in minecraft:the_end run tp @s %.2f %.2f %.2f %.1f %.1f",
                    p.x, p.y, p.z, yaw(stage, yaw), pitch));
        });
    }

    /** Kamera am Punkt (dx, dy, dz) des Heiligtums (Augenhöhe), Blick auf (tx, ty, tz) – im End. */
    private static Step at(Stage stage, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            if (stage.sanctum == null) {
                return;
            }
            Vec3 eye = local(stage, dx, dy, dz);
            Vec3 target = local(stage, tx, ty, tz);
            Vec3 look = target.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in minecraft:the_end run tp @s %.2f %.2f %.2f %.1f %.1f",
                    eye.x, eye.y - 1.62, eye.z, yaw, pitch));
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
