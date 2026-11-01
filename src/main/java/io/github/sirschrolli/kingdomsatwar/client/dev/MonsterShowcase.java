package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.BoneHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Boneman;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DrownedCorpse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.DryCamel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Hag;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Mummy;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Nightmare;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Ooze;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.PlantCreeper;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Revenant;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.Silverling;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.UndeadHorse;
import io.github.sirschrolli.kingdomsatwar.entity.creature.monster.WildSpider;
import io.github.sirschrolli.kingdomsatwar.entity.villager.VillageProfession;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;

/**
 * Weltvorführung der eigenen Monster (E4a): alle Arten nebeneinander, dazu Ausrüstung, Jungtiere, Reiter und Größen.
 * Ein einzelner getönter Glasblock hoch über jedem Monster hält die Sonne ab (so brennen die Untoten nicht): das
 * Himmelslicht fällt seitlich ein und bleibt fast voll hell, nur "freier Himmel" ist es dort nicht mehr.
 */
final class MonsterShowcase {
    private MonsterShowcase() {}

    private static <T extends Mob> void spawn(ServerLevel level, EntityType<T> type, double x, double z, float yaw, Consumer<T> setup) {
        Showcase.spawnCreature(level, type, (int) Math.floor(x), (int) Math.floor(z), yaw, setup);
        level.setBlock(new BlockPos((int) Math.floor(x), Showcase.ground + 40, (int) Math.floor(z)), Blocks.TINTED_GLASS.defaultBlockState(),
                net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
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

    /** Ein Wert, den Vanilla nur aus dem Spielstand liest (z. B. "powered" beim Kriecher). */
    private static void withTag(Mob mob, Consumer<net.minecraft.nbt.CompoundTag> edit) {
        var out = net.minecraft.world.level.storage.TagValueOutput.createWithContext(net.minecraft.util.ProblemReporter.DISCARDING,
                mob.registryAccess());
        mob.saveWithoutId(out);
        var tag = out.buildResult();
        edit.accept(tag);
        mob.load(net.minecraft.world.level.storage.TagValueInput.create(net.minecraft.util.ProblemReporter.DISCARDING, mob.registryAccess(),
                tag));
    }

    /** Jede Gruppe steht auf einer eigenen Fläche (64 Blöcke auseinander), damit hinter ihr nichts anderes im Bild ist. */
    private static int group(int index) {
        return -320 + index * 64;
    }

    static void monsters(Deque<Step> steps) {
        int oz = 560;
        steps.add(Step.command("difficulty hard"));                // in friedlicher Welt verschwinden Monster sofort
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            for (int g = 0; g < 10; g++) {
                int base = group(g);
                Showcase.fill(level, new BlockPos(base - 10, ground - 1, oz - 16), new BlockPos(base + 40, ground - 1, oz + 16),
                        Blocks.GRASS_BLOCK.defaultBlockState());
                Showcase.fill(level, new BlockPos(base - 10, ground, oz - 16), new BlockPos(base + 40, ground + 18, oz + 16),
                        Blocks.AIR.defaultBlockState());
            }

            // Wiedergänger: drei Arten, einer mit Eisenhelm und Schwert, ein Kind; Mumien; Wasserleichen (eine mit Dreizack)
            int b = group(0);
            int x = b;
            for (var look : Revenant.Look.values()) {
                spawn(level, ModEntities.ZOMBIE.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }
            spawn(level, ModEntities.ZOMBIE.get(), b + 6, oz, 20, m -> {
                m.setLook(Revenant.Look.BAUER);
                m.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET));
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
            });
            spawn(level, ModEntities.ZOMBIE.get(), b + 8, oz, 20, m -> {
                m.setLook(Revenant.Look.MOENCH);
                m.setBaby(true);
            });
            x = b + 12;
            for (var look : Mummy.Look.values()) {
                spawn(level, ModEntities.HUSK.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }
            x = b + 17;
            for (var look : DrownedCorpse.Look.values()) {
                spawn(level, ModEntities.DROWNED.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }
            spawn(level, ModEntities.DROWNED.get(), b + 21, oz, 20, m -> {
                m.setLook(DrownedCorpse.Look.MATROSE);
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.TRIDENT));
            });

            // untote Dorfbewohner: acht Berufe in ihrer Tracht
            x = group(1);
            int v = 0;
            for (var profession : new VillageProfession[] {VillageProfession.FARMER, VillageProfession.BAKER, VillageProfession.FISHER,
                    VillageProfession.ARMORER, VillageProfession.HERBALIST, VillageProfession.SHEPHERD, VillageProfession.SCRIBE,
                    VillageProfession.MERCHANT}) {
                int variant = v++ % 3;
                spawn(level, ModEntities.ZOMBIE_VILLAGER.get(), x, oz, 20, m -> {
                    m.setProfession(profession);
                    m.setVariant(variant);
                });
                x += 2;
            }

            // Gerippe (eines mit Bogen), Frostgebein, Moorgebein (eines geschoren), Dörrgebein
            b = group(2);
            x = b;
            for (var look : Boneman.Look.values()) {
                spawn(level, ModEntities.SKELETON.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }
            spawn(level, ModEntities.SKELETON.get(), b + 6, oz, 20, m -> {
                m.setLook(Boneman.Look.JAEGER);
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW));
            });
            spawn(level, ModEntities.STRAY.get(), b + 9, oz, 20, m -> { });
            spawn(level, ModEntities.BOGGED.get(), b + 11, oz, 20, m -> { });
            spawn(level, ModEntities.BOGGED.get(), b + 13, oz, 20, m -> m.setSheared(true));
            spawn(level, ModEntities.PARCHED.get(), b + 15, oz, 20, m -> { });

            // Kriecher: fünf Arten und ein aufgeladener
            b = group(3);
            x = b;
            for (var look : PlantCreeper.Look.values()) {
                spawn(level, ModEntities.CREEPER.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }
            spawn(level, ModEntities.CREEPER.get(), b + 11, oz, 20, m -> {
                m.setLook(PlantCreeper.Look.WALD);
                withTag(m, tag -> tag.putBoolean("powered", true));
            });

            // Spinnen: vier Arten, Höhlenspinne, Spinnenreiter
            b = group(4);
            x = b;
            for (var look : WildSpider.Look.values()) {
                spawn(level, ModEntities.SPIDER.get(), x, oz, 20, m -> m.setLook(look));
                x += 3;
            }
            spawn(level, ModEntities.CAVE_SPIDER.get(), b + 12, oz, 20, m -> { });
            spawn(level, ModEntities.SPIDER.get(), b + 16, oz, 20, m -> {
                m.setLook(WildSpider.Look.WALD);
                rider(level, ModEntities.SKELETON.get(), m, s -> s.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BOW)));
            });

            // Schleime in drei Größen je Art, Schwefelwürfel (groß, klein, mit geschlucktem Block)
            b = group(5);
            x = b;
            for (var look : Ooze.Look.values()) {
                for (int size : new int[] {1, 2, 4}) {
                    spawn(level, ModEntities.SLIME.get(), x, oz, 20, m -> {
                        m.setLook(look);
                        m.setSize(size, true);
                    });
                    x += size + 1;
                }
                x += 1;
            }
            spawn(level, ModEntities.SULFUR_CUBE.get(), b + 24, oz, 20, m -> m.setSize(2, true));
            spawn(level, ModEntities.SULFUR_CUBE.get(), b + 27, oz, 20, m -> m.setSize(1, true));
            spawn(level, ModEntities.SULFUR_CUBE.get(), b + 30, oz, 20, m -> {
                m.setSize(2, true);
                m.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.MOSSY_COBBLESTONE));
            });

            // Hexen: drei Arten, eine mit Trank in der Hand; Nachtmahre schweben daneben über dem Boden
            b = group(6);
            x = b;
            for (var look : Hag.Look.values()) {
                spawn(level, ModEntities.WITCH.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }
            spawn(level, ModEntities.WITCH.get(), b + 6, oz, 20, m -> {
                m.setLook(Hag.Look.KRAUT);
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.POTION));
            });
            x = b + 12;
            for (var look : Nightmare.Look.values()) {
                spawn(level, ModEntities.PHANTOM.get(), x, oz, 200, m -> {
                    m.setLook(look);
                    m.setNoGravity(true);
                    m.setPos(m.getX(), m.getY() + 1.6, m.getZ());
                });
                x += 4;
            }

            // Silberfischchen
            x = group(7);
            for (var look : Silverling.Look.values()) {
                spawn(level, ModEntities.SILVERFISH.get(), x, oz, 20, m -> m.setLook(look));
                x += 2;
            }

            // Zombie- und Skelettpferde (eines gesattelt, mit Gerippe-Reiter)
            b = group(8);
            x = b;
            for (var look : UndeadHorse.Look.values()) {
                spawn(level, ModEntities.ZOMBIE_HORSE.get(), x, oz, 20, m -> m.setLook(look));
                x += 4;
            }
            for (var look : BoneHorse.Look.values()) {
                spawn(level, ModEntities.SKELETON_HORSE.get(), x, oz, 20, m -> m.setLook(look));
                x += 4;
            }
            spawn(level, ModEntities.SKELETON_HORSE.get(), b + 16, oz, 20, m -> {
                m.setLook(BoneHorse.Look.BLEICH);
                m.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                rider(level, ModEntities.SKELETON.get(), m, s -> s.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.IRON_HELMET)));
            });

            // Dörrkamele (eines gesattelt mit Mumie und Dörrgebein als Reitern)
            b = group(9);
            x = b;
            for (var look : DryCamel.Look.values()) {
                spawn(level, ModEntities.CAMEL_HUSK.get(), x, oz, 20, m -> m.setLook(look));
                x += 5;
            }
            spawn(level, ModEntities.CAMEL_HUSK.get(), b + 10, oz, 20, m -> {
                m.setLook(DryCamel.Look.MUMIE);
                m.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                rider(level, ModEntities.HUSK.get(), m, h -> h.setLook(Mummy.Look.WUESTLING));
                rider(level, ModEntities.PARCHED.get(), m, p -> { });
            });
        }));
        steps.add(Step.waitTicks(100));
        // Sonnenschirme erneuern, sobald die Chunks Licht haben (vorher geht die Licht-Änderung verloren), und löschen
        steps.add(Step.server(level -> {
            var area = new net.minecraft.world.phys.AABB(group(0) - 12, Showcase.ground - 2, oz - 18, group(9) + 42, Showcase.ground + 20,
                    oz + 18);
            for (Mob mob : level.getEntitiesOfClass(Mob.class, area)) {
                BlockPos top = BlockPos.containing(mob.getX(), Showcase.ground + 40, mob.getZ());
                level.setBlock(top, Blocks.AIR.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                level.setBlock(top, Blocks.TINTED_GLASS.defaultBlockState(), net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
                mob.clearFire();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(45)));
        shot(steps, "monster_zombies_a", group(0) + 4.0, oz, 1.6, 1.0, 6.6, true);
        shot(steps, "monster_zombies_b", group(0) + 17.5, oz, 1.6, 1.0, 7.2, true);
        shot(steps, "monster_villagers_a", group(1) + 3.0, oz, 1.6, 1.0, 6.4, true);
        shot(steps, "monster_villagers_b", group(1) + 11.0, oz, 1.6, 1.0, 6.4, false);
        shot(steps, "monster_skeletons_a", group(2) + 3.5, oz, 1.6, 1.0, 6.4, true);
        shot(steps, "monster_skeletons_b", group(2) + 12.5, oz, 1.6, 1.0, 6.4, false);
        shot(steps, "monster_creepers", group(3) + 6.0, oz, 1.5, 0.9, 8.6, true);
        shot(steps, "monster_spiders_a", group(4) + 5.0, oz, 1.2, 0.4, 7.2, true);
        shot(steps, "monster_spiders_b", group(4) + 14.5, oz, 1.4, 0.6, 6.0, false);
        shot(steps, "monster_slimes", group(5) + 10.5, oz, 1.4, 0.7, 12.0, true);
        shot(steps, "monster_sulfur_cubes", group(5) + 27.5, oz, 1.2, 0.5, 5.6, false);
        shot(steps, "monster_witches", group(6) + 3.5, oz, 1.7, 1.2, 6.4, true);
        shot(steps, "monster_phantoms", group(6) + 14.5, oz, 2.0, 1.8, 6.6, false);
        shot(steps, "monster_silverfish", group(7) + 1.5, oz, 0.6, 0.15, 2.8, true);
        shot(steps, "monster_horses_a", group(8) + 6.0, oz, 1.8, 1.2, 9.4, true);
        shot(steps, "monster_horses_b", group(8) + 15.0, oz, 2.2, 1.6, 6.6, false);
        shot(steps, "monster_camels", group(9) + 6.0, oz, 2.6, 1.6, 12.0, true);
        // bei Nacht: die leuchtenden Augen
        steps.add(Step.command("time set 18000"));
        steps.add(Step.waitTicks(20));
        shot(steps, "monster_night_zombies", group(0) + 4.0, oz, 1.6, 1.0, 6.6, true);
        shot(steps, "monster_night_skeletons", group(2) + 7.5, oz, 1.6, 1.0, 9.0, true);
        shot(steps, "monster_night_spiders", group(4) + 7.5, oz, 1.2, 0.4, 9.0, true);
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
        steps.add(Step.screenshot(name));
    }
}
