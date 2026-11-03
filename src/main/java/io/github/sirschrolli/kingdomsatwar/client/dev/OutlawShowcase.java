package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Butcher;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Conjurer;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Devastator;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Highwayman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Tormentor;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.raid.Raid;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Weltvorführung der übrigen eigenen Monster (E4b): Wegelagerer (einer als Hauptmann mit Banner), Schlächter mit
 * verschränkten Armen und mit Axt, Beschwörer beim Zaubern mit ihren Plagegeistern, Gaukler, Verwüster (einer mit
 * Reiter), Riffwächter, Windgeister, Knorz und Tiefenhüter – bei Tag und bei Nacht.
 */
final class OutlawShowcase {
    /**
     * Haltungen, die das Spiel nicht speichert (Angriff, Zauber, wacher Knorz): Merkmale an den Wesen, die Haltung selbst
     * wird erst kurz vor jedem Bild gesetzt – die weit entfernten Gruppen werden zwischendurch entladen und neu geladen.
     */
    private static final String ATTACK = "showcase_attack";
    private static final String CAST = "showcase_cast";

    private OutlawShowcase() {}

    private static void applyPoses(ServerLevel level, double x, int row) {
        var area = new net.minecraft.world.phys.AABB(x - 24, Showcase.ground - 2, row - 12, x + 24, Showcase.ground + 12, row + 12);
        var player = level.players().isEmpty() ? null : level.players().getFirst();
        for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
            if (mob.entityTags().contains(ATTACK)) {
                mob.setAggressive(true);
                if (mob instanceof net.minecraft.world.entity.monster.Vex vex) {
                    vex.setIsCharging(true);
                }
            }
            if (mob.entityTags().contains(CAST)) {
                if (mob instanceof Conjurer conjurer) {
                    conjurer.showCasting();
                } else if (mob instanceof io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Trickster trickster) {
                    trickster.showCasting();
                }
            }
            if (mob instanceof net.minecraft.world.entity.monster.creaking.Creaking creaking && player != null && !creaking.isActive()) {
                creaking.activate(player);                       // wach: die Augen glühen
            }
        }
    }

    private static <T extends Mob> void spawn(ServerLevel level, EntityType<T> type, double x, double z, float yaw, Consumer<T> setup) {
        Showcase.spawnCreature(level, type, (int) Math.floor(x), (int) Math.floor(z), yaw, setup);
    }

    private static <T extends Mob> T rider(ServerLevel level, EntityType<T> type, Mob mount, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob != null) {
            mob.setNoAi(true);
            mob.setPersistenceRequired();
            mob.snapTo(mount.getX(), mount.getY(), mount.getZ(), mount.getYRot(), 0);
            setup.accept(mob);
            level.addFreshEntity(mob);
            mob.startRiding(mount, true, false);
        }
        return mob;
    }

    /** Schwebt (ohne Schwerkraft) in der angegebenen Höhe über dem Boden. */
    private static void hover(Mob mob, double height) {
        mob.setNoGravity(true);
        mob.setPos(mob.getX(), mob.getY() + height, mob.getZ());
    }

    private static int group(int index) {
        return -320 + index * 64;
    }

    static void outlaws(Deque<Step> steps) {
        int oz = 760;
        steps.add(Step.command("difficulty hard"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            for (int g = 0; g < 8; g++) {
                int base = group(g);
                Showcase.fill(level, new BlockPos(base - 10, ground - 1, oz - 16), new BlockPos(base + 40, ground - 1, oz + 16),
                        Blocks.GRASS_BLOCK.defaultBlockState());
                Showcase.fill(level, new BlockPos(base - 10, ground, oz - 16), new BlockPos(base + 40, ground + 18, oz + 16),
                        Blocks.AIR.defaultBlockState());
            }
            var banner = Raid.getOminousBannerInstance(level.registryAccess().lookupOrThrow(Registries.BANNER_PATTERN));

            // Wegelagerer: Wald, Steppe, Nord – mit Armbrust; der Hauptmann trägt das Banner
            int b = group(0);
            int x = b;
            for (var look : Highwayman.Look.values()) {
                spawn(level, ModEntities.PILLAGER.get(), x, oz, 15, m -> {
                    m.setLook(look);
                    m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                });
                x += 2;
            }
            spawn(level, ModEntities.PILLAGER.get(), b + 6, oz, 15, m -> {
                m.setLook(Highwayman.Look.WALD);
                m.setPatrolLeader(true);
                m.setItemSlot(EquipmentSlot.HEAD, banner.copy());
            });
            x = b + 11;
            for (var look : Highwayman.Look.values()) {
                spawn(level, ModEntities.PILLAGER.get(), x, oz, 200, m -> m.setLook(look));    // von hinten: Köcher, Pelz, Schärpe
                x += 2;
            }

            // Schlächter: verschränkte Arme und mit erhobener Axt
            b = group(1);
            x = b;
            for (var look : Butcher.Look.values()) {
                spawn(level, ModEntities.VINDICATOR.get(), x, oz, 15, m -> m.setLook(look));
                spawn(level, ModEntities.VINDICATOR.get(), x + 2, oz, 15, m -> {
                    m.setLook(look);
                    m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_AXE));
                    m.addTag(ATTACK);
                });
                x += 4;
            }

            // Beschwörer: einer mit verschränkten Armen, einer beim Zaubern; ihre Plagegeister schweben daneben
            b = group(2);
            x = b;
            for (var look : Conjurer.Look.values()) {
                spawn(level, ModEntities.EVOKER.get(), x, oz, 15, m -> m.setLook(look));
                spawn(level, ModEntities.EVOKER.get(), x + 2, oz, 15, m -> {
                    m.setLook(look);
                    m.addTag(CAST);
                });
                spawn(level, ModEntities.VEX.get(), x + 4, oz, 15, m -> {
                    m.setLook(look == Conjurer.Look.RUNEN ? Tormentor.Look.TUERKIS : Tormentor.Look.VIOLETT);
                    m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
                    if (look == Conjurer.Look.RUNEN) {
                        m.addTag(ATTACK);
                    }
                    hover(m, 1.1);
                });
                x += 7;
            }

            // Gaukler: verschränkte Arme, mit dem Bogen und beim Zaubern
            b = group(3);
            spawn(level, ModEntities.ILLUSIONER.get(), b, oz, 15, m -> { });
            spawn(level, ModEntities.ILLUSIONER.get(), b + 2, oz, 15, m -> {
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
                m.addTag(ATTACK);
            });
            spawn(level, ModEntities.ILLUSIONER.get(), b + 4, oz, 15, m -> m.addTag(CAST));

            // Verwüster: Kriegsauerochse und Steppenungetüm, einer mit Wegelagerer als Reiter
            b = group(4);
            x = b;
            for (var look : Devastator.Look.values()) {
                spawn(level, ModEntities.RAVAGER.get(), x, oz, 25, m -> m.setLook(look));
                x += 5;
            }
            spawn(level, ModEntities.RAVAGER.get(), b + 12, oz, 335, m -> {
                m.setLook(Devastator.Look.AUEROCHSE);
                rider(level, ModEntities.PILLAGER.get(), m, p -> {
                    p.setLook(Highwayman.Look.STEPPE);
                    p.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.CROSSBOW));
                });
            });

            // Riffwächter und uralter Riffwächter (schwebend, damit sie nicht zappeln)
            b = group(5);
            spawn(level, ModEntities.GUARDIAN.get(), b, oz, 20, m -> hover(m, 1.0));
            spawn(level, ModEntities.GUARDIAN.get(), b + 2, oz, 320, m -> hover(m, 1.3));
            spawn(level, ModEntities.ELDER_GUARDIAN.get(), b + 7, oz, 25, m -> hover(m, 0.6));

            // Windgeister und Knorz (wach: die Augen glühen)
            b = group(6);
            spawn(level, ModEntities.BREEZE.get(), b, oz, 15, m -> { });
            spawn(level, ModEntities.BREEZE.get(), b + 2, oz, 330, m -> { });
            spawn(level, ModEntities.CREAKING.get(), b + 7, oz, 15, m -> { });
            spawn(level, ModEntities.CREAKING.get(), b + 10, oz, 345, m -> { });

            // Tiefenhüter
            b = group(7);
            spawn(level, ModEntities.WARDEN.get(), b, oz, 15, m -> { });
            spawn(level, ModEntities.WARDEN.get(), b + 4, oz, 330, m -> { });
        }));
        steps.add(Step.waitTicks(100));
        steps.add(Step.waitTicks(20));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(45)));
        shot(steps, "outlaw_highwaymen_a", group(0) + 3.5, oz, 1.6, 1.0, 6.4, true);
        shot(steps, "outlaw_highwaymen_b", group(0) + 13.5, oz, 1.6, 1.0, 5.2, false);
        shot(steps, "outlaw_butchers", group(1) + 3.5, oz, 1.6, 1.0, 6.4, true);
        shot(steps, "outlaw_conjurers", group(2) + 6.5, oz, 1.7, 1.1, 8.2, true);
        shot(steps, "outlaw_tricksters", group(3) + 2.5, oz, 1.6, 1.0, 5.4, true);
        shot(steps, "outlaw_devastators", group(4) + 6.5, oz, 2.4, 1.4, 11.0, true);
        shot(steps, "outlaw_guardians", group(5) + 4.5, oz, 2.0, 1.4, 7.4, true);
        shot(steps, "outlaw_breeze_gnarl", group(6) + 5.5, oz, 2.0, 1.3, 8.6, true);
        shot(steps, "outlaw_wardens", group(7) + 2.5, oz, 2.2, 1.5, 8.0, true);
        steps.add(Step.command("time set 18000"));
        steps.add(Step.waitTicks(20));
        shot(steps, "outlaw_night_conjurers", group(2) + 6.5, oz, 1.7, 1.1, 8.2, true);
        shot(steps, "outlaw_night_gnarl", group(6) + 5.5, oz, 2.0, 1.3, 8.6, true);
        shot(steps, "outlaw_night_wardens", group(7) + 2.5, oz, 2.2, 1.5, 8.0, true);
        steps.add(Step.command("time set 6000"));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        steps.add(Step.hud(true));
    }

    /**
     * Bild einer Gruppe: die Kamera steht {@code distance} Blöcke südlich der Reihe {@code row} in Augenhöhe {@code eye}
     * und blickt nach Norden auf die Höhe {@code target} (beides über dem Boden).
     */
    private static void shot(Deque<Step> steps, String name, double x, int row, double eye, double target, double distance,
            boolean first) {
        float pitch = (float) Math.toDegrees(Math.atan2(eye - target, distance));
        steps.add(Showcase.tp(x, eye - 1.62, row + 0.5 + distance, 180, pitch));
        if (first) {
            steps.add(Step.waitTicks(60));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.waitTicks(100));
            steps.add(Step.waitForChunks(200));
        } else {
            steps.add(Step.waitTicks(20));
        }
        steps.add(Step.server(level -> applyPoses(level, x, row)));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot(name));
    }
}
