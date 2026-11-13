package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.deep.OrganDeskBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.item.deep.TuningForkItem;
import io.github.sirschrolli.kingdomsatwar.registry.CathedralGear;
import io.github.sirschrolli.kingdomsatwar.registry.EchoGear;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code sordin_boss}, seit 1.7): Sordin, der Kantor der Stille, in einer Stillen Kathedrale – in der flachen
 * Testwelt in einen Block aus Tiefenschiefer gebaut (wie unter den Bergen), das Biom die Tiefe Dunkelheit. Das Mittelschiff,
 * der Kantor an der Orgel (er spielt, ohne dass ein Ton erklingt), das leuchtende Orgelpult; sein Erwachen und die
 * Bossleiste; seine Angriffe gegen einen (zähen) Ritter: Gabelhieb, Schallwelle, Akkord, Notenschwarm, die Stille; ein
 * Notenblock lässt ihn taumeln; das Fortissimo und der dreifache Schall; sein Tod und das verstummte Pult; zuletzt die
 * Stimmgabel – am Rüstungsständer, in der Hand (dritte Person vorn und hinten, erste Person) und die Resonanz.
 */
final class SordinShowcase {
    private static final String STAGE_TAG = "kaw_sordin_stage";
    private static final int OX = 0;
    private static final int OZ = 900;

    private SordinShowcase() {}

    private static final class Stage {
        BlockPos origin;
        BlockPos desk;
        int cantor = -1;
        int knight = -1;
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
                "gamerule advance_time false", "time set 18000", "weather clear", "gamemode spectator"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.server(level -> build(level, stage)));
        steps.add(Step.until(() -> stage.ready, 6000));
        // die Kathedrale in ihrer Höhle: das Portal mit den Türmen und der Fensterrose
        steps.add(at(stage, 0.0, 6.0, 37.0, 0.0, 12.5, 18.0));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(level -> {
            for (Entity e : level.getEntitiesOfClass(Mob.class, area(stage), m -> !(m instanceof SordinEntity))) {
                e.discard();
            }
            if (level.getBlockEntity(stage.desk) instanceof OrganDeskBlockEntity desk) {
                SordinEntity cantor = desk.cantor(level);
                if (cantor == null) {
                    cantor = desk.spawn(level, false);
                }
                if (cantor != null) {
                    stage.cantor = cantor.getId();
                }
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.screenshot("sordin_kathedrale_aussen"));
        // das Mittelschiff, hinten die Orgel
        steps.add(at(stage, 0.0, 8.5, 14.5, 0.0, 4.5, -18.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.screenshot("sordin_kathedrale"));
        // er spielt (von der Seite, von vorn über das Pult, ganz nah)
        steps.add(at(stage, 4.2, 5.0, -15.2, 0.0, 4.0, -18.4));
        steps.add(Step.waitTicks(25));
        steps.add(Step.screenshot("sordin_orgel"));
        steps.add(at(stage, 2.4, 5.7, -19.9, 0.0, 4.9, -17.9));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("sordin_wache"));
        steps.add(at(stage, -1.7, 4.3, -17.3, 0.2, 3.2, -18.7));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("sordin_orgelpult"));
        // er erwacht
        steps.add(Step.server(level -> {
            if (level.getBlockEntity(stage.desk) instanceof OrganDeskBlockEntity desk) {
                desk.awaken(level);
            }
            LivingEntity knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(stage, 0.0, 1.0, -2.0), 180.0f);
            knight.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1000.0);
            knight.setHealth(1000.0f);
            stage.knight = knight.getId();
        }));
        steps.add(at(stage, 2.0, 6.4, -12.6, 0.0, 4.6, -18.0));
        // er wendet sich um (zu dem, der ihn weckte – hier niemand: die Kamera ist ein Zuschauer)
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.cantor) instanceof SordinEntity cantor) {
                cantor.setYRot(0.0f);
                cantor.setYBodyRot(0.0f);
                cantor.setYHeadRot(0.0f);
            }
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("sordin_awaken"));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(22));
        steps.add(Step.screenshot("sordin_bossbar"));
        steps.add(Step.hud(false));
        steps.add(Step.waitTicks(20));
        // die Angriffe: beide an ihre Plätze, dann der Angriff
        // {Angriff, Ticks bis zum Bild, Name, Blickwinkel zur Linie Kantor–Ritter (Grad), Abstand, Höhe, Abstand des Ritters}
        // (das Bild entsteht etwa sechs Ticks nach der angegebenen Zahl: die Schritte dazwischen brauchen je einen Tick)
        // (die Kamera steht schräg hinter dem Ritter und sieht dem Kantor ins Gesicht – im Schiff, über den Bänken)
        for (String[] shot : new String[][] {{"SWING", "9", "sordin_swing", "30", "6.5", "0.6", "3.4"},
                {"SONIC", "16", "sordin_sonic_aim", "28", "9.0", "1.0", "7.5"}, {"SONIC", "23", "sordin_sonic", "28", "9.0", "1.0", "7.5"},
                {"CHORD", "30", "sordin_chord", "30", "9.5", "5.0", "4.5"}, {"NOTES", "30", "sordin_notes", "30", "9.0", "1.4", "7.0"},
                {"SILENCE", "4", "sordin_silence", "25", "6.0", "0.6", "5.0"}}) {
            if (!Step.wanted(shot[2])) {
                continue;
            }
            steps.add(place(stage, shot[0], Double.parseDouble(shot[6])));
            steps.add(Step.waitTicks(Integer.parseInt(shot[1]) - 4));
            steps.add(follow(stage, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5])));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot(shot[2]));
            if (shot[0].equals("SILENCE")) {
                // er taucht hinter dem Ritter wieder auf, schon ausholend
                steps.add(Step.waitTicks(SordinEntity.SILENCE_APPEAR - 18));
                steps.add(follow(stage, 25.0, 6.5, 0.8));
                steps.add(Step.waitTicks(4));
                steps.add(Step.screenshot("sordin_silence_reveal"));
            }
            steps.add(Step.waitTicks(60));
            steps.add(Step.server(level -> clear(level, stage)));
        }
        // jemand spielt auf der Orgel: er hält sich die Ohren zu und taumelt
        steps.add(place(stage, "NONE", 5.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.server(level -> level.blockEvent(BlockPos.containing(local(stage, 1, 2, -19)), Blocks.NOTE_BLOCK, 0, 0)));
        steps.add(Step.waitTicks(10));
        steps.add(follow(stage, 28.0, 7.0, 0.8));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("sordin_stagger"));
        steps.add(Step.waitTicks(SordinEntity.STAGGER_TICKS));
        // unter halber Kraft: das Fortissimo (er holt Luft, dann der Schlag)
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.cantor) instanceof SordinEntity cantor) {
                cantor.setHealth(cantor.getMaxHealth() * 0.46f);
            }
        }));
        steps.add(place(stage, "FORTISSIMO", 6.0));
        steps.add(Step.waitTicks(20));
        steps.add(follow(stage, 22.0, 8.5, 1.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("sordin_fortissimo_luft"));
        steps.add(Step.waitTicks(SordinEntity.FORTISSIMO_BLAST - 34));
        steps.add(follow(stage, 32.0, 11.0, 3.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("sordin_fortissimo"));
        steps.add(Step.waitTicks(60));
        // im Fortissimo: drei Schallwellen auf einmal
        steps.add(place(stage, "SONIC", 7.5));
        steps.add(Step.waitTicks(SordinEntity.SONIC_FIRE - 4));
        steps.add(follow(stage, 16.0, 11.0, 4.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("sordin_sonic_fan"));
        steps.add(Step.waitTicks(60));
        // sein Tod
        steps.add(Step.server(level -> {
            if (level.getEntity(stage.knight) instanceof Entity knight) {
                knight.discard();
            }
            clear(level, stage);
            if (level.getEntity(stage.cantor) instanceof SordinEntity cantor) {
                Vec3 p = local(stage, 0.0, 1.0, -8.0);
                cantor.teleportTo(p.x, p.y, p.z);
                cantor.forceAttack(SordinEntity.Attack.NONE);
                cantor.setInvulnerableTime(0);
                cantor.setHealth(1.0f);
                cantor.hurtServer(level, level.damageSources().generic(), 50.0f);
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(at(stage, 2.4, 3.8, -4.2, 0.0, 2.2, -8.0));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("sordin_death"));
        steps.add(Step.waitTicks(SordinEntity.DEATH_TICKS));
        steps.add(at(stage, -1.7, 4.3, -17.3, 0.2, 3.2, -18.7));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("sordin_orgelpult_verstummt"));
        // die Stimmgabel am Rüstungsständer (die Beute fort, sie läge sonst im Bild)
        steps.add(Step.server(level -> {
            for (var e : level.getEntitiesOfClass(ItemEntity.class, area(stage))) {
                e.discard();
            }
            Vec3 at = local(stage, 0.0, 1.0, -8.5);
            ArmorStand stand = new ArmorStand(level, at.x, at.y, at.z);
            stand.setShowArms(true);
            stand.setYRot(0.0f);
            stand.setYBodyRot(0.0f);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(CathedralGear.TUNING_FORK.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(stage, 0.9, 2.6, -6.4, -0.3, 1.9, -8.5));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("stimmgabel_staender"));
        // die Stimmgabel in der Hand: dritte Person (vorn, hinten), erste Person
        steps.add(Step.server(level -> {
            for (var e : level.getEntitiesOfClass(ArmorStand.class, area(stage))) {
                e.discard();
            }
        }));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            player.getInventory().clearContent();
            player.getInventory().setItem(0, new ItemStack(CathedralGear.TUNING_FORK.get()));
            player.getInventory().setItem(1, new ItemStack(EchoGear.ECHO_CRYSTAL.get(), 4));
            player.getInventory().setItem(2, new ItemStack(CathedralGear.ORGAN_DESK.get()));
            player.getInventory().setItem(3, new ItemStack(CathedralGear.SORDIN_SPAWN_EGG.get()));
        }));
        steps.add(Step.once(minecraft -> minecraft.player.getInventory().setSelectedSlot(0)));
        steps.add(tp(stage, 0.0, 1.0, -7.0, 20.0f, 6.0f));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT);
            minecraft.options.fov().set(60);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("stimmgabel_spieler_vorn"));
        steps.add(tp(stage, 0.0, 1.0, -6.0, 200.0f, 6.0f));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("stimmgabel_spieler_hinten"));
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
        steps.add(tp(stage, -1.0, 1.0, -3.0, 165.0f, 10.0f));
        steps.add(Step.waitTicks(30));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("stimmgabel_hand"));
        steps.add(Step.hud(false));
        // die Resonanz: durch eine Mauer aus Tiefenschiefer hindurch trifft sie den Gegner dahinter (auf der Stufe zum Chor)
        steps.add(Step.once(minecraft -> {
            ShowcaseRunner.hideHand = true;
            minecraft.options.setCameraType(CameraType.THIRD_PERSON_BACK);
        }));
        steps.add(tp(stage, 0.0, 1.0, 3.0, 180.0f, 0.0f));
        steps.add(Step.server(level -> {
            for (int x = -1; x <= 1; x++) {
                for (int y = 1; y <= 2; y++) {
                    level.setBlock(BlockPos.containing(local(stage, x, y, -2)), Blocks.DEEPSLATE_BRICKS.defaultBlockState(), Block.UPDATE_CLIENTS);
                }
            }
            LivingEntity dummy = creature(level, ModEntities.SOLDIERS.get(SoldierType.SWORDSMAN).get(), local(stage, 0.0, 2.0, -11.0), 0.0f);
            dummy.getAttribute(Attributes.MAX_HEALTH).setBaseValue(200.0);
            dummy.setHealth(200.0f);
        }));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            TuningForkItem.resonate(level, player, TuningForkItem.DAMAGE);
        }));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("stimmgabel_resonanz"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.quit());
    }

    /**
     * Baut die Kathedrale: ein Block aus Tiefenschiefer (die Höhle wird hineingehauen), das Biom die Tiefe Dunkelheit; dazu
     * unsichtbare Lichtblöcke im Schiff, im Chor und in der Apsis (sonst sähe man auf den Bildern wenig).
     */
    private static void build(ServerLevel level, Stage stage) {
        long started = System.currentTimeMillis();
        stage.origin = new BlockPos(OX, Showcase.ground + 8, OZ);
        BlockPos o = stage.origin;
        int x0 = o.getX() - 33;
        int x1 = o.getX() + 33;
        int y0 = o.getY() - 6;
        int y1 = o.getY() + 49;
        int z0 = o.getZ() - 43;
        int z1 = o.getZ() + 47;
        for (int cx = x0 >> 4; cx <= x1 >> 4; cx++) {
            for (int cz = z0 >> 4; cz <= z1 >> 4; cz++) {
                level.setChunkForced(cx, cz, true);
                level.getChunk(cx, cz);
            }
        }
        BlockPos.MutableBlockPos p = new BlockPos.MutableBlockPos();
        var deepslate = Blocks.DEEPSLATE.defaultBlockState();
        for (int x = x0; x <= x1; x++) {
            for (int z = z0; z <= z1; z++) {
                for (int y = y0; y <= y1; y++) {
                    level.setBlock(p.set(x, y, z), deepslate, 2 | 16);
                }
            }
        }
        var biome = level.registryAccess().lookupOrThrow(Registries.BIOME).getOrThrow(net.minecraft.world.level.biome.Biomes.DEEP_DARK);
        for (int bx = x0; bx <= x1; bx += 16) {
            for (int bz = z0; bz <= z1; bz += 16) {
                net.minecraft.server.commands.FillBiomeCommand.fill(level, new BlockPos(bx, y0, bz),
                        new BlockPos(Math.min(x1, bx + 15), y1, Math.min(z1, bz + 15)), biome)
                        .ifRight(error -> KingdomsAtWar.LOGGER.warn("[Showcase] Biom nicht gesetzt: {}", error.getMessage()));
            }
        }
        SitePiece piece = SiteSamples.silentCathedral(o);
        BoundingBox all = new BoundingBox(x0, y0, z0, x1, y1, z1);
        piece.postProcess(level, level.structureManager(), level.getChunkSource().getGenerator(), level.getRandom(), all, ChunkPos.containing(o), o);
        stage.desk = o.offset(0, 2, -19);
        // Licht (unsichtbar): im Schiff, im Chor, an der Orgel
        int[][] lights = {{0, 5, 14}, {0, 5, 10}, {0, 5, 6}, {0, 5, 2}, {0, 5, -2}, {0, 6, -6}, {0, 6, -10}, {0, 7, -14}, {0, 9, -17},
                {0, 13, 10}, {0, 13, 2}, {0, 13, -6}, {-8, 4, 8}, {8, 4, 8}, {-8, 4, 0}, {8, 4, 0}, {-8, 4, -8}, {8, 4, -8}, {-3, 4, -16},
                {3, 4, -16}, {0, 6, 22}, {0, 6, 27}, {-5, 5, 30}, {5, 5, 30}, {0, 16, 26}, {-10, 10, 28}, {10, 10, 28}};
        for (int[] l : lights) {
            BlockPos at = o.offset(l[0], l[1], l[2]);
            if (level.getBlockState(at).isAir()) {
                level.setBlock(at, Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, 12), Block.UPDATE_CLIENTS);
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Stille Kathedrale bei {}, Pult {} ({}), {} ms", o, stage.desk, level.getBlockState(stage.desk),
                System.currentTimeMillis() - started);
        stage.ready = true;
    }

    private static AABB area(Stage stage) {
        return new AABB(stage.origin).inflate(30, 20, 40);
    }

    /** Stellt Kantor und Ritter auf ihre Plätze (der Ritter {@code distance} vor ihm, im Mittelgang) und beginnt den Angriff. */
    private static Step place(Stage stage, String attack, double distance) {
        return Step.server(level -> {
            if (level.getEntity(stage.cantor) instanceof SordinEntity cantor && level.getEntity(stage.knight) instanceof LivingEntity knight) {
                Vec3 p = local(stage, 0.0, 1.0, -9.0);
                Vec3 k = local(stage, 0.0, 1.0, -9.0 + distance);
                cantor.teleportTo(p.x, p.y, p.z);
                cantor.setInvisible(false);
                knight.teleportTo(k.x, k.y, k.z);
                knight.setDeltaMovement(Vec3.ZERO);
                knight.resetFallDistance();
                knight.setHealth(knight.getMaxHealth());
                cantor.setTarget(knight);
                cantor.lookAt(knight, 360.0f, 360.0f);
                cantor.setYBodyRot(cantor.getYRot());
                cantor.setYHeadRot(cantor.getYRot());
                cantor.forceAttack(SordinEntity.Attack.valueOf(attack));
            }
        });
    }

    /** Räumt Noten und Ringe fort (sie gehören zum Kantor; ein neuer Angriff beginnt ohne sie). */
    private static void clear(ServerLevel level, Stage stage) {
        for (var e : level.getEntitiesOfClass(Entity.class, area(stage), e -> e instanceof ItemEntity
                || e.getType() == io.github.sirschrolli.kingdomsatwar.registry.ModEntities.SPELL_FX.get())) {
            e.discard();
        }
    }

    /** Kamera beim Kantor: Abstand, Drehung aus der Linie Kantor → Ritter (Grad), Höhe über seiner Brust. */
    private static Step follow(Stage stage, double angle, double distance, double height) {
        return Step.server(level -> {
            if (!(level.getEntity(stage.cantor) instanceof Entity cantor) || level.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = cantor.position().add(0.0, 2.6, 0.0);
            Vec3 toward = level.getEntity(stage.knight) instanceof Entity knight ? knight.position().subtract(cantor.position())
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

    /** Ein Punkt im Rahmen der Kathedrale (Mitte des Mittelschiffs bei 0, 0, 0; das Portal liegt bei +z) in Weltkoordinaten. */
    private static Vec3 local(Stage stage, double x, double y, double z) {
        return new Vec3(stage.origin.getX() + x + 0.5, stage.origin.getY() + y, stage.origin.getZ() + z + 0.5);
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

    /** Kamera am Punkt (dx, dy, dz) der Kathedrale (Augenhöhe), Blick auf (tx, ty, tz). */
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
