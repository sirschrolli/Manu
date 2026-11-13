package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Bear;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.HouseCat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBat;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildBee;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildCamel;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildFox;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildArmadillo;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.WildWolf;
import io.github.sirschrolli.kingdomsatwar.entity.creature.wild.Wildcat;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.UUID;
import java.util.function.Consumer;
import net.minecraft.client.CameraType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.EntityReference;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.armadillo.Armadillo;
import net.minecraft.world.entity.animal.equine.Llama;
import net.minecraft.world.entity.animal.panda.Panda;
import net.minecraft.world.entity.animal.parrot.Parrot;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;

/** Weltvorführung der eigenen Wildtiere (E3b): alle Arten nebeneinander, dazu Posen, Ausrüstung und Jungtiere. */
final class WildShowcase {
    private WildShowcase() {}

    /** Posen, die Vanilla nicht speichert (Stehen, Liegen, Sitzen) – nach dem Nachladen des Chunks neu setzen. */
    private static final String POSE = "kingdomsatwar_showcase_pose";
    private static final UUID OWNER = UUID.fromString("6f1a3c2e-9a43-4b1e-8d55-2a4c1f0e7b11");

    private static <T extends Mob> void spawn(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, double x, double z,
            float yaw, Consumer<T> setup) {
        Showcase.spawnCreature(level, type, (int) Math.floor(x), (int) Math.floor(z), yaw, setup);
    }

    /** Ein Wert, den Vanilla nur aus dem Spielstand liest (z. B. "Sleeping" beim Fuchs). */
    private static void withTag(Mob mob, Consumer<CompoundTag> edit) {
        TagValueOutput out = TagValueOutput.createWithContext(ProblemReporter.DISCARDING, mob.registryAccess());
        mob.saveWithoutId(out);
        CompoundTag tag = out.buildResult();
        edit.accept(tag);
        mob.load(TagValueInput.create(ProblemReporter.DISCARDING, mob.registryAccess(), tag));
    }

    private static void tame(net.minecraft.world.entity.TamableAnimal animal) {
        animal.setOwnerReference(EntityReference.of(OWNER));
        animal.setTame(true, true);
    }

    static void wildAnimals(Deque<Step> steps) {
        int oz = 420;
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-10, ground - 1, oz - 8), new BlockPos(50, ground - 1, oz + 92), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-10, ground, oz - 8), new BlockPos(50, ground + 12, oz + 92), Blocks.AIR.defaultBlockState());

            // Wölfe: sechs Arten, ein wütender, ein zahmer mit Halsband (sitzend), einer im Wolfspanzer, ein Welpe
            int x = 0;
            for (var breed : WildWolf.Breed.values()) {
                spawn(level, ModEntities.WOLF.get(), x, oz, 20, m -> m.setBreed(breed));
                x += 2;
            }
            spawn(level, ModEntities.WOLF.get(), 12, oz, 20, m -> {
                m.setBreed(WildWolf.Breed.GRAU);
                m.setPersistentAngerEndTime(level.getGameTime() + 1_000_000L);
            });
            spawn(level, ModEntities.WOLF.get(), 14, oz, 20, m -> {
                m.setBreed(WildWolf.Breed.TIMBER);
                tame(m);
                m.setComponent(DataComponents.WOLF_COLLAR, DyeColor.RED);
                m.setOrderedToSit(true);
                m.setInSittingPose(true);
            });
            spawn(level, ModEntities.WOLF.get(), 16, oz, 20, m -> {
                m.setBreed(WildWolf.Breed.GRAU);
                tame(m);
                m.setComponent(DataComponents.WOLF_COLLAR, DyeColor.BLUE);
                m.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.WOLF_ARMOR));
            });
            spawn(level, ModEntities.WOLF.get(), 18, oz, 20, m -> {
                m.setBreed(WildWolf.Breed.POLAR);
                m.setBaby(true);
            });

            // Füchse: vier Arten, einer schläft, einer trägt Beeren im Maul, ein Welpe
            x = 0;
            for (var breed : WildFox.Breed.values()) {
                spawn(level, ModEntities.FOX.get(), x, oz + 8, 20, m -> m.setBreed(breed));
                x += 2;
            }
            spawn(level, ModEntities.FOX.get(), 8, oz + 8, 20, m -> {
                m.setBreed(WildFox.Breed.ROT);
                withTag(m, tag -> tag.putBoolean("Sleeping", true));
            });
            spawn(level, ModEntities.FOX.get(), 10, oz + 8, 20, m -> {
                m.setBreed(WildFox.Breed.SILBER);
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.SWEET_BERRIES));
            });
            spawn(level, ModEntities.FOX.get(), 12, oz + 8, 20, m -> {
                m.setBreed(WildFox.Breed.POLAR);
                m.setBaby(true);
            });

            // Bären: drei Arten, ein aufgerichteter Braunbär, Junge
            x = 0;
            for (var breed : Bear.Breed.values()) {
                spawn(level, ModEntities.BEAR.get(), x, oz + 16, 20, m -> m.setBreed(breed));
                x += 4;
            }
            spawn(level, ModEntities.BEAR.get(), 12, oz + 16, 20, m -> {
                m.setBreed(Bear.Breed.BRAUN);
                m.setStanding(true);
                m.addTag(POSE);
            });
            spawn(level, ModEntities.BEAR.get(), 15, oz + 16, 20, m -> {
                m.setBreed(Bear.Breed.EIS);
                m.setBaby(true);
            });
            spawn(level, ModEntities.BEAR.get(), 17, oz + 16, 20, m -> {
                m.setBreed(Bear.Breed.SCHWARZ);
                m.setBaby(true);
            });

            // Pandas: ein Gemüt je Gen, einer sitzt und isst Bambus, ein Junges
            x = 0;
            for (var gene : Panda.Gene.values()) {
                spawn(level, ModEntities.PANDA.get(), x, oz + 26, 20, m -> {
                    m.setMainGene(gene);
                    m.setHiddenGene(gene);
                });
                x += 3;
            }
            spawn(level, ModEntities.PANDA.get(), 21, oz + 26, 20, m -> {
                m.setMainGene(Panda.Gene.NORMAL);
                m.setHiddenGene(Panda.Gene.NORMAL);
                m.sit(true);
                m.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.BAMBOO));
                m.addTag(POSE);
            });
            spawn(level, ModEntities.PANDA.get(), 24, oz + 26, 20, m -> {
                m.setMainGene(Panda.Gene.PLAYFUL);
                m.setHiddenGene(Panda.Gene.PLAYFUL);
                m.setBaby(true);
            });

            // Katzen: acht Hauskatzen, eine zahme mit Halsband, eine liegende, ein Kätzchen; drei Wildkatzen
            x = 0;
            for (var breed : HouseCat.Breed.values()) {
                spawn(level, ModEntities.CAT.get(), x, oz + 36, 20, m -> m.setBreed(breed));
                x += 2;
            }
            spawn(level, ModEntities.CAT.get(), 16, oz + 36, 20, m -> {
                m.setBreed(HouseCat.Breed.GETIGERT);
                tame(m);
                m.setComponent(DataComponents.CAT_COLLAR, DyeColor.LIGHT_BLUE);
                m.setOrderedToSit(true);
                m.setInSittingPose(true);
            });
            spawn(level, ModEntities.CAT.get(), 18, oz + 36, 20, m -> {
                m.setBreed(HouseCat.Breed.PERSER);
                tame(m);
                m.setComponent(DataComponents.CAT_COLLAR, DyeColor.PINK);
                m.setLying(true);
                m.addTag(POSE);
            });
            spawn(level, ModEntities.CAT.get(), 20, oz + 36, 20, m -> {
                m.setBreed(HouseCat.Breed.ROT);
                m.setBaby(true);
            });
            x = 23;
            for (var breed : Wildcat.Breed.values()) {
                spawn(level, ModEntities.WILDCAT.get(), x, oz + 36, 20, m -> m.setBreed(breed));
                x += 3;
            }

            // Papageien auf Zaunpfählen, Bienen in der Luft, Fledermäuse unter einem Steinbalken
            for (var variant : Parrot.Variant.values()) {
                int px = variant.getId() * 2;
                level.setBlock(new BlockPos(px, ground, oz + 46), Blocks.OAK_FENCE.defaultBlockState(), 2);
                spawn(level, ModEntities.PARROT.get(), px, oz + 46, 20, m -> {
                    m.setComponent(DataComponents.PARROT_VARIANT, variant);
                    tame(m);
                    m.setOrderedToSit(true);
                    m.setInSittingPose(true);
                    m.setNoGravity(true);
                    m.setPos(px + 0.5, ground + 1.0, oz + 46.5);
                });
            }
            x = 11;
            for (var breed : WildBee.Breed.values()) {
                for (int state = 0; state < 3; state++) {
                    int s = state;
                    double bx = x;
                    spawn(level, ModEntities.BEE.get(), bx, oz + 46, 20, m -> {
                        m.setBreed(breed);
                        m.setNoGravity(true);
                        m.setPos(bx + 0.5, ground + 1.0, oz + 46.5);
                        if (s == 1) {
                            withTag(m, tag -> tag.putBoolean("HasNectar", true));
                        } else if (s == 2) {
                            m.setPersistentAngerEndTime(level.getGameTime() + 1_000_000L);
                        }
                    });
                    x += 1;
                }
                x += 1;
            }
            Showcase.fill(level, new BlockPos(20, ground + 3, oz + 45), new BlockPos(29, ground + 3, oz + 47), Blocks.STONE.defaultBlockState());
            x = 21;
            for (var breed : WildBat.Breed.values()) {
                double bx = x;
                spawn(level, ModEntities.BAT.get(), bx, oz + 46, 0, m -> {
                    m.setBreed(breed);
                    m.setNoGravity(true);
                    m.setResting(true);
                    m.setPos(bx + 0.5, ground + 2.1, oz + 46.5);
                });
                x += 2;
            }
            spawn(level, ModEntities.BAT.get(), 27, oz + 46, 20, m -> {
                m.setBreed(WildBat.Breed.VAMPIR);
                m.setNoGravity(true);
                m.setPos(27.5, ground + 1.4, oz + 47.5);
            });

            // Gürteltiere, Kamele
            spawn(level, ModEntities.ARMADILLO.get(), 0, oz + 56, 20, m -> m.setBreed(WildArmadillo.Breed.NEUNBINDEN));
            spawn(level, ModEntities.ARMADILLO.get(), 2, oz + 56, 20, m -> m.setBreed(WildArmadillo.Breed.KUGEL));
            spawn(level, ModEntities.ARMADILLO.get(), 4, oz + 56, 20, m -> {
                m.setBreed(WildArmadillo.Breed.NEUNBINDEN);
                m.switchToState(Armadillo.ArmadilloState.SCARED);
            });
            spawn(level, ModEntities.ARMADILLO.get(), 6, oz + 56, 20, m -> {
                m.setBreed(WildArmadillo.Breed.KUGEL);
                m.setBaby(true);
            });
            spawn(level, ModEntities.CAMEL.get(), 10, oz + 56, 20, m -> m.setBreed(WildCamel.Breed.DROMEDAR));
            spawn(level, ModEntities.CAMEL.get(), 15, oz + 56, 20, m -> m.setBreed(WildCamel.Breed.TRAMPELTIER));
            spawn(level, ModEntities.CAMEL.get(), 20, oz + 56, 20, m -> {
                m.setBreed(WildCamel.Breed.TRAMPELTIER);
                m.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
            });
            spawn(level, ModEntities.CAMEL.get(), 25, oz + 56, 20, m -> {
                m.setBreed(WildCamel.Breed.DROMEDAR);
                m.setItemSlot(EquipmentSlot.SADDLE, new ItemStack(Items.SADDLE));
                m.sitDown();
            });
            spawn(level, ModEntities.CAMEL.get(), 29, oz + 56, 20, m -> {
                m.setBreed(WildCamel.Breed.DROMEDAR);
                m.setBaby(true);
            });

            // Lamas: vier Farben, mit Teppich, mit Packtaschen, das Händlerlama, ein Fohlen
            x = 0;
            for (var variant : Llama.Variant.values()) {
                spawn(level, ModEntities.LLAMA.get(), x, oz + 70, 20, m -> m.setComponent(DataComponents.LLAMA_VARIANT, variant));
                x += 3;
            }
            spawn(level, ModEntities.LLAMA.get(), 12, oz + 70, 20, m -> {
                m.setComponent(DataComponents.LLAMA_VARIANT, Llama.Variant.WHITE);
                m.setItemSlot(EquipmentSlot.BODY, new ItemStack(Items.CARPET.pick(DyeColor.RED)));
            });
            spawn(level, ModEntities.LLAMA.get(), 15, oz + 70, 20, m -> {
                m.setComponent(DataComponents.LLAMA_VARIANT, Llama.Variant.BROWN);
                m.setTamed(true);
                m.setChest(true);
            });
            spawn(level, ModEntities.TRADER_LLAMA.get(), 18, oz + 70, 20, m -> m.setComponent(DataComponents.LLAMA_VARIANT, Llama.Variant.CREAMY));
            spawn(level, ModEntities.LLAMA.get(), 21, oz + 70, 20, m -> {
                m.setComponent(DataComponents.LLAMA_VARIANT, Llama.Variant.GRAY);
                m.setBaby(true);
            });
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(45)));
        // Kamera in Augenhöhe der Tiere (Angabe = Augenhöhe über dem Boden, Abstand in Blöcken)
        // Kamera in Augenhöhe der Tiere: Mitte der Gruppe (x), Reihe (z), Augen- und Zielhöhe über dem Boden, Abstand
        shot(steps, "wild_wolves_a", 3.5, oz, 1.0, 0.45, 5.4, true);
        steps.add(Step.server(level -> {
            for (var entity : level.getAllEntities()) {
                if (!entity.entityTags().contains(POSE)) {
                    continue;
                }
                switch (entity) {
                    case Bear bear -> bear.setStanding(true);
                    case HouseCat cat -> cat.setLying(true);
                    case Panda panda -> panda.sit(true);
                    default -> { }
                }
            }
        }));
        steps.add(Step.waitTicks(20));
        shot(steps, "wild_wolves_b", 10.5, oz, 1.0, 0.45, 4.4, false);
        shot(steps, "wild_wolves_c", 16.5, oz, 1.0, 0.45, 4.4, false);
        shot(steps, "wild_foxes_a", 3.5, oz + 8, 0.9, 0.35, 5.2, false);
        shot(steps, "wild_foxes_b", 10.5, oz + 8, 0.9, 0.35, 4.4, false);
        shot(steps, "wild_bears_a", 4.5, oz + 16, 1.7, 0.8, 7.5, false);
        shot(steps, "wild_bears_b", 15.0, oz + 16, 1.7, 0.9, 6.0, false);
        shot(steps, "wild_pandas_a", 5.0, oz + 26, 1.5, 0.7, 8.4, false);
        shot(steps, "wild_pandas_b", 18.5, oz + 26, 1.5, 0.7, 9.0, false);
        shot(steps, "wild_cats_a", 3.5, oz + 36, 0.8, 0.35, 5.2, false);
        shot(steps, "wild_cats_b", 11.5, oz + 36, 0.8, 0.35, 5.2, false);
        shot(steps, "wild_cats_c", 18.5, oz + 36, 0.8, 0.3, 4.2, false);
        shot(steps, "wild_wildcats", 26.5, oz + 36, 0.9, 0.4, 5.4, false);
        shot(steps, "wild_parrots", 4.5, oz + 46, 1.5, 1.3, 6.2, false);
        shot(steps, "wild_bees", 14.5, oz + 46, 1.2, 1.15, 6.2, false);
        shot(steps, "wild_bats", 24.5, oz + 46, 1.6, 2.3, 4.6, false);
        shot(steps, "wild_armadillos", 3.5, oz + 56, 0.75, 0.3, 5.2, false);
        shot(steps, "wild_camels_a", 12.5, oz + 56, 2.0, 1.3, 6.0, false);
        shot(steps, "wild_camels_b", 24.5, oz + 56, 2.0, 1.1, 7.8, false);
        shot(steps, "wild_llamas_a", 4.5, oz + 70, 1.6, 1.0, 7.6, false);
        shot(steps, "wild_llamas_b", 16.5, oz + 70, 1.6, 1.0, 7.6, false);

        steps.add(Step.once(minecraft -> minecraft.options.fov().set(70)));
        // Papagei auf der Schulter (Ansicht von vorn)
        steps.add(Step.server(level -> {
            for (ServerPlayer player : level.players()) {
                player.setGameMode(net.minecraft.world.level.GameType.CREATIVE);
                player.getAbilities().flying = false;
                player.onUpdateAbilities();
            }
        }));
        steps.add(Showcase.tp(40.5, 0.0, oz + 10.5, 0, 0));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(level -> {
            for (ServerPlayer player : level.players()) {
                var parrot = ModEntities.PARROT.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (parrot != null) {
                    parrot.setComponent(DataComponents.PARROT_VARIANT, Parrot.Variant.RED_BLUE);
                    parrot.snapTo(player.getX(), player.getY(), player.getZ(), 0, 0);
                    level.addFreshEntity(parrot);
                    parrot.setEntityOnShoulder(player);
                }
                var grey = ModEntities.PARROT.get().create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
                if (grey != null) {
                    grey.setComponent(DataComponents.PARROT_VARIANT, Parrot.Variant.GRAY);
                    grey.snapTo(player.getX(), player.getY(), player.getZ(), 0, 0);
                    level.addFreshEntity(grey);
                    grey.setEntityOnShoulder(player);
                }
            }
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.THIRD_PERSON_FRONT)));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("wild_shoulder"));
        steps.add(Step.once(minecraft -> minecraft.options.setCameraType(CameraType.FIRST_PERSON)));
        steps.add(Step.server(level -> {
            for (ServerPlayer player : level.players()) {
                player.setGameMode(net.minecraft.world.level.GameType.SPECTATOR);
            }
        }));
        steps.add(Step.hud(true));
    }

    /**
     * Bild einer Tiergruppe: die Kamera steht {@code distance} Blöcke südlich der Reihe {@code row} in Augenhöhe {@code eye}
     * und blickt nach Norden auf die Höhe {@code target} (beides über dem Boden).
     */
    private static void shot(Deque<Step> steps, String name, double x, int row, double eye, double target, double distance,
            boolean first) {
        float pitch = (float) Math.toDegrees(Math.atan2(eye - target, distance));
        steps.add(Showcase.tp(x, eye - 1.62, row + 0.5 + distance, 180, pitch));
        if (first) {
            // nach dem weiten Sprung erst die Chunks ankommen lassen, dann warten, bis alles gebaut ist
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
