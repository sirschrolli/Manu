package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossProjectile;
import io.github.sirschrolli.kingdomsatwar.entity.creator.DivineShotEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.CometLanceEntity;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellProjectile;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeProjectile;
import io.github.sirschrolli.kingdomsatwar.siege.SiegeType;
import java.util.Deque;
import java.util.Set;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.arrow.AbstractArrow;
import net.minecraft.world.entity.projectile.arrow.ThrownTrident;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Flugbahn-Prüfung ({@code wurf_flug}): Dreizack, Lichtspeer, goldene Feder, Sternenlanze, Eislanze, Ballistenbolzen und
 * Glaciaras Eisspeer fliegen nebeneinander – schräg nach oben, schräg nach unten, von oben gesehen in einer Richtung
 * schräg zu den Weltachsen –, dazu ein Dreizack im echten Bogen (mit Schwerkraft) und fallende Eiszapfen. Jedes
 * Geschoss muss mit der Spitze genau in Flugrichtung zeigen.
 */
final class FlightShowcase {
    private static final int BASE_X = 9000;
    private static final int BASE_Z = 9000;

    private FlightShowcase() {}

    private static ServerPlayer player(ServerLevel any) {
        return any.getServer().getPlayerList().getPlayers().getFirst();
    }

    static void flight(Deque<Step> steps) {
        for (String command : new String[] {"time set 6000", "weather clear", "kill @e[type=!minecraft:player]"}) {
            steps.add(Step.command(command));
        }
        steps.add(Step.hud(false));
        int[] y = new int[1];
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            ServerPlayer player = player(any);
            player.teleportTo(level, BASE_X + 0.5, 200, BASE_Z - 9.5, Set.of(), 0.0f, 0.0f, true);
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            y[0] = 170;
            // eine helle Wand hinter der Flugebene und ein Boden darunter (für die Ansicht von oben)
            for (int x = -16; x <= 16; x++) {
                for (int dy = -9; dy <= 9; dy++) {
                    level.setBlock(new BlockPos(BASE_X + x, y[0] + dy, BASE_Z + 6), Blocks.POLISHED_ANDESITE.defaultBlockState(), 2);
                }
                for (int z = -16; z <= 16; z++) {
                    level.setBlock(new BlockPos(BASE_X + x, y[0] - 10, BASE_Z + z), Blocks.SMOOTH_STONE.defaultBlockState(), 2);
                }
            }
        }));
        steps.add(Step.waitTicks(10));
        // seitlich: schräg nach oben (nach +X), schräg nach unten (nach -X)
        side(steps, y, "wurf_flug_hoch", 90.0, 30.0);
        side(steps, y, "wurf_flug_tief", 270.0, -30.0);
        // von oben: waagrecht, schräg zu den Achsen
        top(steps, y, "wurf_flug_oben", 30.0);
        top(steps, y, "wurf_flug_oben2", 215.0);
        // ein Dreizack im echten Bogen, drei Augenblicke
        arc(steps, y);
        // Eiszapfen fallen (gerade und schräg)
        icicles(steps, y);
        // Würfe aus dem Seitwärtslaufen, von oben gesehen
        strafe(steps, y);
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
    }

    /** Richtung aus Gier (wie bei Pfeilen: x = sin, z = cos) und Neigung (nach oben positiv), in Grad. */
    private static Vec3 dir(double yaw, double pitch) {
        double a = Math.toRadians(yaw);
        double p = Math.toRadians(pitch);
        return new Vec3(Math.sin(a) * Math.cos(p), Math.sin(p), Math.cos(a) * Math.cos(p));
    }

    private static void side(Deque<Step> steps, int[] y, String name, double yaw, double pitch) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            ServerPlayer player = player(any);
            // die Kamera schaut nach +Z auf die Wand; die Geschosse fliegen in der Ebene z = BASE_Z
            player.teleportTo(level, BASE_X + 0.5, y[0] - 1.62, BASE_Z - 10.0, Set.of(), 0.0f, 0.0f, true);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            Vec3 d = dir(yaw, pitch);
            Vec3 center = new Vec3(BASE_X + 0.5, y[0] - 0.5, BASE_Z + 0.5);
            volley(level, player(any), center, d, new Vec3(0.0, 1.0, 0.0), 0.16);
        }));
        steps.add(Step.waitTicks(14));
        steps.add(Step.screenshot(name));
    }

    private static void top(Deque<Step> steps, int[] y, String name, double yaw) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            ServerPlayer player = player(any);
            player.teleportTo(level, BASE_X + 0.5, y[0] + 4.0, BASE_Z + 0.5, Set.of(), 0.0f, 90.0f, true);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            Vec3 d = dir(yaw, 0.0);
            Vec3 across = new Vec3(-d.z, 0.0, d.x);
            Vec3 center = new Vec3(BASE_X + 0.5, y[0] - 4.0, BASE_Z + 0.5);
            volley(level, player(any), center, d, across, 0.16);
        }));
        steps.add(Step.waitTicks(14));
        steps.add(Step.screenshot(name));
    }

    /** Sieben Geschosse nebeneinander (versetzt um {@code across}), alle in Richtung {@code d}, ohne Schwerkraft. */
    private static void volley(ServerLevel level, ServerPlayer player, Vec3 center, Vec3 d, Vec3 across, double speed) {
        for (int k = 0; k < 7; k++) {
            Vec3 p = center.add(across.scale((3 - k) * 1.15)).subtract(d.scale(1.2));
            switch (k) {
                case 0 -> {
                    ThrownTrident trident = new ThrownTrident(level, player, new ItemStack(Items.TRIDENT));
                    trident.snapTo(p.x, p.y, p.z, 0.0f, 0.0f);
                    trident.setNoGravity(true);
                    trident.shoot(d.x, d.y, d.z, (float) speed, 0.0f);
                    trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                    level.addFreshEntity(trident);
                }
                case 1 -> DivineShotEntity.thrownSpear(level, player, p, d, speed, 0.0f);
                case 2 -> DivineShotEntity.feather(level, player, p, d, speed, 0.0f);
                case 3 -> {
                    CometLanceEntity lance = new CometLanceEntity(level, player);
                    lance.launch(p, p.add(d.scale(20.0)));
                    lance.setNoGravity(true);
                    lance.setDeltaMovement(d.scale(speed));
                    aim(lance, d);
                    level.addFreshEntity(lance);
                }
                case 4 -> SpellProjectile.shoot(level, player, SpellProjectile.Kind.ICE_LANCE, p, d, speed, 0.0f, null, 1);
                case 5 -> {
                    SiegeProjectile bolt = SiegeProjectile.devCreate(level, SiegeType.BALLISTA, player, p);
                    bolt.setNoGravity(true);
                    bolt.setDeltaMovement(d.scale(speed));
                    aim(bolt, d);
                    level.addFreshEntity(bolt);
                }
                default -> {
                    BossProjectile ice = new BossProjectile(ModEntities.BOSS_PROJECTILE.get(), level);
                    ice.setKind(BossProjectile.Kind.ICE_SPEAR);
                    ice.setPos(p.x, p.y, p.z);
                    ice.setDeltaMovement(d.scale(speed));
                    level.addFreshEntity(ice);
                }
            }
        }
    }

    private static void aim(Entity e, Vec3 d) {
        float yaw = (float) (Mth.atan2(d.x, d.z) * Mth.RAD_TO_DEG);
        float pitch = (float) (Mth.atan2(d.y, d.horizontalDistance()) * Mth.RAD_TO_DEG);
        e.setYRot(yaw);
        e.setXRot(pitch);
        e.yRotO = yaw;
        e.xRotO = pitch;
    }

    /** Ein Dreizack mit Schwerkraft im Bogen nach +X, von der Seite: kurz nach dem Wurf, oben, beim Fallen. */
    private static void arc(Deque<Step> steps, int[] y) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            player(any).teleportTo(level, BASE_X - 2.5, y[0] - 1.62, BASE_Z - 9.0, Set.of(), 0.0f, 0.0f, true);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            ServerPlayer player = player(any);
            for (int i = 0; i < 2; i++) {
                ThrownTrident trident = new ThrownTrident(level, player, new ItemStack(Items.TRIDENT));
                trident.snapTo(BASE_X - 8.0, y[0] - 4.0 + i * 0.01, BASE_Z + 0.5 + i * 2.0, 0.0f, 0.0f);
                Vec3 d = dir(90.0, 52.0);
                trident.shoot(d.x, d.y, d.z, i == 0 ? 0.95f : 0.8f, 0.0f);
                trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                level.addFreshEntity(trident);
            }
        }));
        for (int[] shot : new int[][] {{3, 1}, {7, 2}, {7, 3}}) {
            steps.add(Step.waitTicks(shot[0]));
            steps.add(Step.screenshot("wurf_bogen_" + shot[1]));
        }
    }

    /**
     * Drei Dreizacke, geworfen wie aus dem Seitwärtslaufen: Der Wurf zeigt nach +Z, die Eigenbewegung des Werfers (nach
     * +X) kommt erst danach dazu – so fliegen sie schräg; die Zinken müssen trotzdem genau in die Flugrichtung zeigen.
     */
    private static void strafe(Deque<Step> steps, int[] y) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            player(any).teleportTo(level, BASE_X + 0.5, y[0] + 4.0, BASE_Z + 0.5, Set.of(), 0.0f, 90.0f, true);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            ServerPlayer player = player(any);
            for (int i = 0; i < 3; i++) {
                ThrownTrident trident = new ThrownTrident(level, player, new ItemStack(Items.TRIDENT));
                trident.snapTo(BASE_X + 2.0 - i * 2.0, y[0] - 4.0, BASE_Z - 2.5, 0.0f, 0.0f);
                trident.setNoGravity(true);
                trident.shoot(0.0, 0.0, 1.0, 0.14f, 0.0f);
                trident.setDeltaMovement(trident.getDeltaMovement().add(0.05 + i * 0.05, 0.0, 0.0));
                trident.pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                level.addFreshEntity(trident);
            }
        }));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("wurf_seitwaerts"));
    }

    private static void icicles(Deque<Step> steps, int[] y) {
        steps.add(Step.command("kill @e[type=!minecraft:player]"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            player(any).teleportTo(level, BASE_X + 0.5, y[0] - 1.62, BASE_Z - 10.0, Set.of(), 0.0f, -8.0f, true);
        }));
        steps.add(Step.waitTicks(6));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().overworld();
            for (int i = 0; i < 3; i++) {
                BossProjectile ice = new BossProjectile(ModEntities.BOSS_PROJECTILE.get(), level);
                ice.setKind(BossProjectile.Kind.ICICLE);
                ice.setPos(BASE_X - 3.0 + i * 3.0, y[0] + 5.0, BASE_Z + 0.5);
                ice.setDeltaMovement(new Vec3((i - 1) * 0.08, -0.05, 0.0));
                level.addFreshEntity(ice);
            }
        }));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("wurf_eiszapfen"));
    }
}
