package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonWar;
import io.github.sirschrolli.kingdomsatwar.entity.realm.RealmWarriorEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.ModItems;
import io.github.sirschrolli.kingdomsatwar.war.Realm;
import io.github.sirschrolli.kingdomsatwar.war.RealmState;
import io.github.sirschrolli.kingdomsatwar.war.WarriorRole;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung: Drachen im Krieg. Ein Heerlager von Rabenstein (Krieger im Krieg mit dem Spieler), ein Feuerdrache und ein
 * Eisdrache des Spielers fliegen auf das Horn hin einen Luftangriff – Bilder vom Anflug hoch über dem Lager, vom
 * Sturzflug mit dem Odem und vom Horn in der Hand.
 */
final class DragonWarShowcase {
    private static final int X0 = -260;
    private static final int Z0 = -120;

    private DragonWarShowcase() {}

    static void dragonWar(Deque<Step> steps) {
        List<DragonEntity> dragons = new ArrayList<>();
        List<RealmWarriorEntity> camp = new ArrayList<>();
        steps.add(Step.hud(false));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            for (int cx = (X0 - 60) >> 4; cx <= (X0 + 60) >> 4; cx++) {
                for (int cz = (Z0 - 60) >> 4; cz <= (Z0 + 60) >> 4; cz++) {
                    level.getChunk(cx, cz);
                    level.setChunkForced(cx, cz, true);
                }
            }
            Showcase.fill(level, new BlockPos(X0 - 56, g - 1, Z0 - 56), new BlockPos(X0 + 56, g - 1, Z0 + 56), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(X0 - 56, g, Z0 - 56), new BlockPos(X0 + 56, g + 30, Z0 + 56), Blocks.AIR.defaultBlockState());
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            if (owner == null) {
                return;
            }
            // das Heerlager: Zelte (Wollblöcke) und eine Schar Krieger
            for (int i = 0; i < 3; i++) {
                BlockPos tent = new BlockPos(X0 - 8 + i * 8, g, Z0 - 10);
                Showcase.fill(level, tent.offset(-1, 0, -1), tent.offset(1, 1, 1), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.RED).defaultBlockState());
                level.setBlock(tent.offset(0, 2, 0), Blocks.WOOL.pick(net.minecraft.world.item.DyeColor.RED).defaultBlockState(), 3);
                level.setBlock(tent.offset(0, 0, 1), Blocks.AIR.defaultBlockState(), 3);
                level.setBlock(tent.offset(0, 1, 1), Blocks.AIR.defaultBlockState(), 3);
            }
            WarriorRole[] roles = {WarriorRole.FOOTMAN, WarriorRole.FOOTMAN, WarriorRole.ARCHER, WarriorRole.CROSSBOWMAN, WarriorRole.KNIGHT,
                    WarriorRole.FOOTMAN, WarriorRole.ARCHER, WarriorRole.CAPTAIN};
            for (int i = 0; i < roles.length; i++) {
                RealmWarriorEntity w = ModEntities.REALM_WARRIORS.get(roles[i]).get().create(level, EntitySpawnReason.COMMAND);
                if (w == null) {
                    continue;
                }
                w.setRealm(Realm.RABENSTEIN);
                double a = i * Math.PI * 2 / roles.length;
                w.snapTo(X0 + Math.cos(a) * 5.0, g, Z0 + Math.sin(a) * 5.0, (float) Math.toDegrees(a), 0.0f);
                w.finalizeSpawn(level, level.getCurrentDifficultyAt(w.blockPosition()), EntitySpawnReason.COMMAND, null);
                w.setPost(w.blockPosition(), new BlockPos(X0, g, Z0));
                w.setPersistenceRequired();
                // sie sollen das Bild nicht verlassen: ein paar Leben mehr, damit der Odem sie nicht gleich umwirft
                var health = w.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH);
                if (health != null) {
                    health.setBaseValue(health.getBaseValue() * 6.0);
                    w.setHealth(w.getMaxHealth());
                }
                level.addFreshEntity(w);
                camp.add(w);
            }
            // zwei Drachen des Spielers am anderen Ende des Felds
            DragonType[] types = {DragonType.FIRE, DragonType.ICE};
            for (int i = 0; i < types.length; i++) {
                DragonEntity d = ModEntities.DRAGONS.get(types[i]).get().create(level, EntitySpawnReason.COMMAND);
                if (d == null) {
                    continue;
                }
                d.applyStage(i == 0 ? DragonStage.ELDER : DragonStage.ADULT, false);
                d.snapTo(X0 - 6 + i * 12, g, Z0 + 40, 180.0f, 0.0f);
                d.setPersistenceRequired();
                level.addFreshEntity(d);
                d.bondWith(owner);
                d.setMode(DragonEntity.MODE_SIT);   // sie warten, bis das Horn sie ruft
                d.setHealth(d.getMaxHealth());
                dragons.add(d);
            }
        }));
        steps.add(Step.until(() -> dragons.size() == 2, 400));
        steps.add(look(X0 + 4.0, 3.0, Z0 + 52.0, X0, 6.0, Z0 + 20.0));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(30));

        // das Horn in der Hand (mit Anzeige)
        steps.add(Step.command("gamemode creative"));
        steps.add(Step.waitTicks(10));
        steps.add(Step.once(minecraft -> {
            var server = minecraft.getSingleplayerServer();
            if (server != null) {
                server.execute(() -> {
                    var player = server.getPlayerList().getPlayers().getFirst();
                    player.getInventory().setSelectedSlot(0);
                    player.getInventory().setItem(0, new ItemStack(ModItems.DRAGON_HORN.get()));
                });
            }
        }));
        // hinter den Drachen, Blick über sie hinweg zum Lager (noch ist kein Krieg erklärt)
        steps.add(look(X0 + 3.0, 1.7, Z0 + 56.0, X0 + 1.0, 2.2, Z0 + 30.0));
        steps.add(Step.hud(true));
        steps.add(Step.waitTicks(30));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(5));
        steps.add(Step.screenshot("dragonwar_horn"));
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));

        // Krieg mit Rabenstein – und der Luftangriff auf das Lager
        steps.add(Step.server(level -> {
            Player owner = level.players().isEmpty() ? null : level.players().getFirst();
            if (owner != null) {
                RealmState state = RealmState.of(level);
                state.declareWar(Realm.RABENSTEIN, owner.getUUID());
                state.save(level);
            }
            DragonWar.strike(level, null, List.copyOf(dragons), new BlockPos(X0, Showcase.ground, Z0));
        }));
        // Anflug: hoch über dem Lager
        steps.add(Step.until(() -> phase(dragons, 0) != null, 900));
        steps.add(Step.once(minecraft -> {
            DragonEntity d = phase(dragons, 0);
            if (d != null) {
                lookAbs(minecraft, X0 + 14.0, Showcase.ground + 3.0, Z0 + 14.0, d.getX(), d.getY() + 1.5, d.getZ());
            }
        }));
        steps.add(Step.screenshot("dragonwar_approach"));
        // Sturzflug mit Odem, von der Seite: das Spiel hält für das Bild kurz an (die Kamera steht dann genau richtig)
        for (int shot = 0; shot < 2; shot++) {
            String name = shot == 0 ? "dragonwar_dive" : "dragonwar_dive_ice";
            int index = shot;
            steps.add(Step.until(() -> phase(dragons, 1) != null && dragons.get(index) == phase(dragons, 1), 1500));
            steps.add(Step.command("tick freeze"));
            steps.add(Step.waitTicks(2));
            steps.add(Step.once(minecraft -> {
                DragonEntity d = dragons.get(index);
                LivingEntity t = d.getTarget();
                Vec3 at = t != null ? t.position() : d.position();
                Vec3 mid = d.position().add(at).scale(0.5);
                Vec3 dir = at.subtract(d.position());
                Vec3 side = new Vec3(-dir.z, 0.0, dir.x).normalize();
                // auf der Seite, auf der kein anderer Drache im Bild hängt
                DragonEntity other = dragons.get(1 - index);
                if (other.position().subtract(mid).dot(side) > 0.0) {
                    side = side.scale(-1.0);
                }
                Vec3 eye = mid.add(side.scale(15.0)).add(0.0, 1.0, 0.0);
                lookAbs(minecraft, eye.x, Math.max(eye.y, Showcase.ground + 2.2), eye.z, mid.x, mid.y + 1.8, mid.z);
            }));
            steps.add(Step.waitTicks(6));
            steps.add(Step.screenshot(name));
            steps.add(Step.command("tick unfreeze"));
        }
        steps.add(Step.server(level -> {
            for (DragonEntity d : dragons) {
                d.recall();
            }
        }));
        steps.add(Step.waitTicks(10));
    }

    /**
     * Ein Drache im Anflug hoch über dem Lager ({@code phase} 0) bzw. im Sturzflug mit Odem knapp über dem Ziel
     * ({@code phase} 1) – oder {@code null}.
     */
    private static DragonEntity phase(List<DragonEntity> dragons, int phase) {
        var server = Minecraft.getInstance().getSingleplayerServer();
        if (server == null) {
            return null;
        }
        for (DragonEntity d : dragons) {
            LivingEntity t = d.getTarget();
            if (t == null || !d.isFlying()) {
                continue;
            }
            double height = d.getY() - t.getY();
            double distance = d.distanceTo(t);
            if (phase == 0 && height > 10.0 && distance < 26.0 && distance > 12.0) {
                return d;
            }
            if (phase == 1 && d.isBreathing() && distance < 11.0 && height < 8.0) {
                return d;
            }
        }
        return null;
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
        return Step.once(minecraft -> lookAbs(minecraft, ex, Showcase.ground + ey, ez, tx, Showcase.ground + ty, tz));
    }
}
