package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.army.WarTableScreen;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.Warhorse;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeWeaponEntity;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.AABB;

/**
 * Vorführung der neuen Truppen: alle Arten in einer Reihe (vorn, hinten, nah), zwei Lanzenreiter auf ihren Schlachtrossen,
 * ein Lanzenangriff mit gesenkter Lanze, die Schlachtreihe aus Schildträgern, Speerträgern und Armbrustschützen, ein
 * Belagerungsknecht am Katapult und die Wahl der Ausbildung am Feldherrentisch.
 */
final class TroopShowcase {
    private static final int X0 = 200;
    private static final int Z0 = -200;
    /** Die Reihe aller Arten (von links nach rechts, von vorn gesehen). */
    private static final SoldierType[] LINE = {SoldierType.SWORDSMAN, SoldierType.SPEARMAN, SoldierType.SHIELDBEARER, SoldierType.ENGINEER,
            SoldierType.KNIGHT, SoldierType.LANCER, SoldierType.MAN_AT_ARMS, SoldierType.CAPTAIN, SoldierType.ARCHER, SoldierType.CROSSBOWMAN};

    private TroopShowcase() {}

    static void troops(Deque<Step> steps) {
        SoldierEntity[] charger = {null};
        Mob[] dummy = {null};
        SiegeWeaponEntity[] catapult = {null};
        SoldierEntity[] crossbowmen = new SoldierEntity[4];
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int cx = (X0 - 40) >> 4; cx <= (X0 + 70) >> 4; cx++) {
                for (int cz = (Z0 - 70) >> 4; cz <= (Z0 + 30) >> 4; cz++) {
                    level.getChunk(cx, cz);
                    level.setChunkForced(cx, cz, true);
                }
            }
            Showcase.fill(level, new BlockPos(X0 - 36, g - 1, Z0 - 66), new BlockPos(X0 + 66, g - 1, Z0 + 26), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(X0 - 36, g, Z0 - 66), new BlockPos(X0 + 66, g + 10, Z0 + 26), Blocks.AIR.defaultBlockState());
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            // 1) die Reihe: jede Art einmal, die neuen dazwischen
            double x = X0;
            for (SoldierType type : LINE) {
                double step = type.mounted() ? 3.2 : 1.9;
                x += step / 2.0;
                pose(level, type, type == SoldierType.LANCER ? 0 : 1, x, Z0, 0.0f, null);
                x += step / 2.0;
            }
            // 2) zwei Lanzenreiter (Rappe und Schimmel), seitlich
            pose(level, SoldierType.LANCER, 0, X0 + 40.5, Z0 + 0.5, 90.0f, null);
            pose(level, SoldierType.LANCER, 1, X0 + 40.5, Z0 + 4.0, 90.0f, null);
            // 3) die Schlachtreihe: vorn Schildträger mit erhobenem Schild, dahinter Speerträger, ganz hinten Armbrustschützen
            for (int i = 0; i < 4; i++) {
                double fx = X0 + 0.5 + i * 1.3;
                SoldierEntity bearer = pose(level, SoldierType.SHIELDBEARER, i % 3, fx, Z0 - 24.0, 0.0f, owner);
                bearer.startUsingItem(InteractionHand.OFF_HAND);
                SoldierEntity spear = pose(level, SoldierType.SPEARMAN, i % 3, fx + 0.4, Z0 - 25.4, 0.0f, owner);
                spear.setAggressive(true);
                SoldierEntity xbow = pose(level, SoldierType.CROSSBOWMAN, i % 3, fx - 0.1, Z0 - 27.0, 0.0f, owner);
                xbow.setAggressive(true);
                if (i % 2 == 0) {
                    xbow.getMainHandItem().set(DataComponents.CHARGED_PROJECTILES, ChargedProjectiles.of(new ItemStackTemplate(Items.ARROW)));
                }
                crossbowmen[i] = xbow;
            }
            // 4) Belagerungsknecht am beschädigten Katapult (mit eigenem Willen: er geht hin und bessert aus)
            catapult[0] = ModEntities.siegeWeapon(SiegeType.CATAPULT).create(level, EntitySpawnReason.COMMAND);
            if (catapult[0] != null && owner != null) {
                catapult[0].snapTo(X0 + 22.5, g, Z0 - 24.5, 200.0f, 0.0f);
                catapult[0].setYHeadRot(200.0f);
                catapult[0].setOwner(owner.getUUID());
                catapult[0].setHealthFraction(0.35f);
                level.addFreshEntity(catapult[0]);
                SoldierEntity engineer = live(level, SoldierType.ENGINEER, 0, X0 + 24.5, Z0 - 20.5, 180.0f, owner);
                engineer.setMode(SoldierEntity.MODE_HOLD, engineer.blockPosition());
            }
            // 5) Feldherrentisch: ein Schwertkämpfer davor, im Feldlager auch die neuen Truppen
            level.setBlock(new BlockPos(X0 + 50, g, Z0 - 40), ModBlocks.RECRUITMENT_TABLE.get().defaultBlockState()
                    .setValue(HorizontalDirectionalBlock.FACING, Direction.SOUTH), Block.UPDATE_ALL);
            if (owner != null) {
                pose(level, SoldierType.SWORDSMAN, 2, X0 + 49.5, Z0 - 37.5, 180.0f, owner).setRank(1);
                pose(level, SoldierType.SPEARMAN, 0, X0 + 46.5, Z0 - 38.5, 90.0f, owner);
                SoldierEntity bearer = pose(level, SoldierType.SHIELDBEARER, 1, X0 + 53.5, Z0 - 38.5, -90.0f, owner);
                bearer.setRank(2);
                bearer.setHealth(bearer.getMaxHealth() * 0.55f);
                pose(level, SoldierType.CROSSBOWMAN, 2, X0 + 46.5, Z0 - 42.5, 90.0f, owner);
                pose(level, SoldierType.LANCER, 1, X0 + 55.5, Z0 - 44.5, -90.0f, owner).setRank(1);
            }
        }));
        steps.add(Step.until(() -> catapult[0] != null, 400));
        steps.add(Step.waitTicks(40));

        // Reihe von vorn, von hinten und nah
        double mid = X0 + lineWidth() / 2.0;
        steps.add(look(mid, 2.2, Z0 + 10.5, mid, 1.2, Z0));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("troops_lineup"));
        steps.add(look(mid, 2.2, Z0 - 10.5, mid, 1.2, Z0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("troops_lineup_back"));
        steps.add(look(X0 + 4.6, 1.75, Z0 + 3.6, X0 + 4.6, 1.15, Z0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("troops_close_left"));
        steps.add(look(X0 + 11.4, 2.1, Z0 + 4.4, X0 + 11.4, 1.35, Z0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("troops_close_mid"));
        steps.add(look(X0 + lineWidth() - 3.2, 1.75, Z0 + 3.6, X0 + lineWidth() - 3.2, 1.15, Z0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("troops_close_right"));

        // Lanzenreiter auf ihren Rossen
        steps.add(look(X0 + 36.2, 2.3, Z0 - 2.6, X0 + 40.2, 1.6, Z0 + 2.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("troops_lancers"));

        // Schlachtreihe – zwei der Armbrustschützen spannen gerade
        steps.add(look(X0 + 7.6, 3.3, Z0 - 20.2, X0 + 2.6, 1.0, Z0 - 25.6));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            for (int i = 1; i < crossbowmen.length; i += 2) {
                if (crossbowmen[i] != null) {
                    crossbowmen[i].startUsingItem(InteractionHand.MAIN_HAND);
                }
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("troops_formation"));

        // Belagerungsknecht bei der Arbeit: die Kamera seitlich, Knecht und Katapult im Bild
        steps.add(look(X0 + 27.0, 2.4, Z0 - 19.8, X0 + 22.5, 1.0, Z0 - 24.0));
        steps.add(Step.until(() -> engineer(catapult[0]) != null, 600));
        steps.add(Step.once(minecraft -> {
            SoldierEntity e = engineer(catapult[0]);
            if (e != null) {
                double cx = catapult[0].getX();
                double cz = catapult[0].getZ();
                double dx = e.getX() - cx;
                double dz = e.getZ() - cz;
                double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                // vom Katapult zum Knecht, um 65 Grad gedreht und etwas weiter weg
                double a = Math.toRadians(65.0);
                double rx = (dx * Math.cos(a) - dz * Math.sin(a)) / len;
                double rz = (dx * Math.sin(a) + dz * Math.cos(a)) / len;
                double mx = (cx + e.getX()) / 2.0;
                double mz = (cz + e.getZ()) / 2.0;
                lookAbs(minecraft, mx + rx * 5.0, Showcase.ground + 2.3, mz + rz * 5.0, mx, Showcase.ground + 1.0, mz);
            }
        }));
        steps.add(Step.until(() -> working(catapult[0]), 200));
        steps.add(Step.waitTicks(1));
        steps.add(Step.screenshot("troops_engineer"));

        // Lanzenangriff: ein Lanzenreiter mit eigenem Willen gegen eine Strohpuppe (Mumie ohne Willen)
        steps.add(Step.server(level -> {
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            int g = Showcase.ground;
            charger[0] = live(level, SoldierType.LANCER, 0, X0 + 10.5, Z0 - 44.5, 180.0f, owner);
            charger[0].setMode(SoldierEntity.MODE_HOLD, charger[0].blockPosition());
            var husk = ModEntities.HUSK.get().create(level, EntitySpawnReason.COMMAND);
            if (husk != null) {
                husk.snapTo(X0 + 10.5, g, Z0 - 62.5, 0.0f, 0.0f);
                husk.setNoAi(true);
                husk.getAttribute(Attributes.MAX_HEALTH).setBaseValue(400.0);
                husk.setHealth(400.0f);
                husk.setPersistenceRequired();
                level.addFreshEntity(husk);
                dummy[0] = husk;
            }
        }));
        steps.add(Step.until(() -> charger[0] != null && charger[0].getVehicle() instanceof Warhorse, 200));
        steps.add(Step.server(level -> {
            if (charger[0] != null && dummy[0] != null) {
                charger[0].setTarget(dummy[0]);
            }
        }));
        steps.add(look(X0 + 18.5, 2.0, Z0 - 50.0, X0 + 10.5, 1.4, Z0 - 56.0));
        // im Anritt: Lanze gesenkt, auf den Gegner zu, noch ein paar Schritte entfernt – dann die Kamera von der Seite
        steps.add(Step.until(() -> charging(charger[0], dummy[0]), 900));
        steps.add(Step.once(minecraft -> {
            if (charger[0] != null && dummy[0] != null) {
                double lx = charger[0].getX();
                double lz = charger[0].getZ();
                double tx = dummy[0].getX();
                double tz = dummy[0].getZ();
                double dx = tx - lx;
                double dz = tz - lz;
                double len = Math.max(0.1, Math.sqrt(dx * dx + dz * dz));
                double mx = lx + dx * 0.55;
                double mz = lz + dz * 0.55;
                lookAbs(minecraft, mx - dz / len * 6.5, Showcase.ground + 2.2, mz + dx / len * 6.5, mx, Showcase.ground + 1.2, mz);
            }
        }));
        steps.add(Step.screenshot("troops_charge"));

        // Ausbildung am Feldherrentisch: Schwertkämpfer, Wahl Speerträger
        steps.add(look(X0 + 50.5, 1.4, Z0 - 36.4, X0 + 50.5, 0.8, Z0 - 40.0));
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.hud(true));
        steps.add(Step.screen(minecraft -> new WarTableScreen(new BlockPos(X0 + 50, Showcase.ground, Z0 - 40), 32, 6, true)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof WarTableScreen screen) {
                screen.choose(SoldierType.SPEARMAN);
            }
        }));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(15));
        steps.add(Step.screenshot("troops_war_table"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof WarTableScreen screen) {
                screen.choose(SoldierType.ENGINEER);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("troops_war_table_2"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    /** Wie breit die Reihe aller Arten ist. */
    private static double lineWidth() {
        double w = 0.0;
        for (SoldierType type : LINE) {
            w += type.mounted() ? 3.2 : 1.9;
        }
        return w;
    }

    /** Der Belagerungsknecht am Katapult (wenn er schon dort ist). */
    private static SoldierEntity engineer(SiegeWeaponEntity weapon) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || weapon == null) {
            return null;
        }
        ServerLevel level = server.overworld();
        var list = level.getEntitiesOfClass(SoldierEntity.class, new AABB(weapon.blockPosition()).inflate(3.2),
                s -> s.getSoldierType() == SoldierType.ENGINEER && s.getNavigation().isDone());
        return list.isEmpty() ? null : list.getFirst();
    }

    /** Reitet der Lanzenreiter gerade mit gesenkter Lanze auf den Gegner zu (noch 4 bis 8 Blöcke entfernt)? */
    private static boolean charging(SoldierEntity lancer, Mob target) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || lancer == null || target == null || !(lancer.getVehicle() instanceof Warhorse horse)) {
            return false;
        }
        double d = lancer.distanceTo(target);
        double toward = (target.getX() - horse.getX()) * horse.getDeltaMovement().x + (target.getZ() - horse.getZ()) * horse.getDeltaMovement().z;
        return lancer.isUsingItem() && d > 4.0 && d < 8.0 && toward > 0.3;
    }

    /** Hämmert der Belagerungsknecht gerade am Katapult? */
    private static boolean working(SiegeWeaponEntity weapon) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null || weapon == null) {
            return false;
        }
        ServerLevel level = server.overworld();
        return level.getEntitiesOfClass(SoldierEntity.class, new AABB(weapon.blockPosition()).inflate(4.0),
                s -> s.getSoldierType() == SoldierType.ENGINEER && s.getCurrentSwing() != null).size() > 0;
    }

    /** Ein Soldat ohne eigenen Willen für die Bilder (Lanzenreiter samt Ross). */
    private static SoldierEntity pose(ServerLevel level, SoldierType type, int variant, double x, double z, float yaw, Player owner) {
        SoldierEntity soldier = spawn(level, type, variant, x, z, yaw, owner);
        soldier.setNoAi(true);
        if (type.mounted()) {
            Warhorse horse = Warhorse.saddleUp(level, soldier);
            if (horse != null) {
                horse.setNoAi(true);
                soldier.mountSteed(horse);
            }
        }
        return soldier;
    }

    /** Ein Soldat mit eigenem Willen. */
    private static SoldierEntity live(ServerLevel level, SoldierType type, int variant, double x, double z, float yaw, Player owner) {
        return spawn(level, type, variant, x, z, yaw, owner);
    }

    private static SoldierEntity spawn(ServerLevel level, SoldierType type, int variant, double x, double z, float yaw, Player owner) {
        SoldierEntity soldier = ModEntities.SOLDIERS.get(type).get().create(level, EntitySpawnReason.COMMAND);
        soldier.equip();
        soldier.setVariant(Math.min(variant, type.variants() - 1));
        soldier.giveRandomName();
        soldier.setHealth(soldier.getMaxHealth());
        soldier.snapTo(x, Showcase.ground, z, yaw, 0.0f);
        soldier.setYBodyRot(yaw);
        soldier.setYHeadRot(yaw);
        if (owner != null) {
            soldier.setOwner(owner);
            soldier.setMode(SoldierEntity.MODE_HOLD, soldier.blockPosition());
        }
        level.addFreshEntity(soldier);
        return soldier;
    }

    /** Kamera an eine Stelle (absolute Höhen), Blick auf einen Punkt. */
    private static void lookAbs(Minecraft minecraft, double ex, double ey, double ez, double tx, double ty, double tz) {
        double lx = tx - ex;
        double ly = ty - ey;
        double lz = tz - ez;
        float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
        float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
        minecraft.options.setCameraType(CameraType.FIRST_PERSON);
        minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", ex, ey - 1.62, ez, yaw, pitch));
    }

    private static Step look(double ex, double ey, double ez, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            int g = Showcase.ground;
            double lx = tx - ex;
            double ly = ty - ey;
            double lz = tz - ez;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.options.setCameraType(CameraType.FIRST_PERSON);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", ex, g + ey - 1.62, ez, yaw,
                    pitch));
        });
    }
}
