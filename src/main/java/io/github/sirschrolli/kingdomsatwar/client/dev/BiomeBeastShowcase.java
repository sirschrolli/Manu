package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.GriffinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.PrimevalElkEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.SnowLeopardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creature.biome.StormGullEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.BiomeBeasts;
import io.github.sirschrolli.kingdomsatwar.registry.ModBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.worldgen.overworld.OverworldBiomes;
import java.util.Deque;
import java.util.Locale;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SnowLayerBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;

/**
 * Vorführung der Tiere der eigenen Biome (0.85) auf kleinen Bühnen hoch über dem Gelände, jede im Biom ihres Tiers (Himmel,
 * Nebel und Licht stimmen): der Schneeleopard auf einem verschneiten Gipfelgrat, der Urelch auf einer Lichtung im Urhain, die
 * Sturmmöwen an einem Kiesstrand der Sturmküste mit Steg und Brandungsfelsen, der Greif in einer Felsschlucht mit
 * gebänderten Felsnadeln.
 */
final class BiomeBeastShowcase {
    private static final String STAGE_TAG = "kaw_beast_stage";
    private static final String OVERWORLD = "minecraft:overworld";
    /** Bühne des Schneeleoparden: Mitte (x, Boden-y, z). */
    private static final int LX = 6000;
    private static final int LZ = 6000;
    /** Bühne des Urelchs. */
    private static final int EX = 6300;
    private static final int EZ = 6000;
    /** Bühne der Sturmmöwen. */
    private static final int GX = 6600;
    private static final int GZ = 6000;
    /** Bühne des Greifen. */
    private static final int KX = 6900;
    private static final int KZ = 6000;
    private static final int SY = 200;

    private BiomeBeastShowcase() {}

    /** {@code only}: nur diese Bühne (leopard) – oder alle. */
    static void beasts(Deque<Step> steps, String only) {
        Mob[] m = new Mob[8];
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("time set 1000"));
        steps.add(Step.command("weather clear"));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.fov().set(55)));

        // ------------------------------------------------------------------ Schneeleopard
        if (only.isEmpty() || only.equals("leopard")) {
            build(steps, level -> frost(level, LX, SY, LZ), OverworldBiomes.FROST_PEAKS, LX, SY, LZ);
            // zwei Leoparden: der helle oben auf dem Felsen, der rauchgraue im Schnee davor
            arrive(steps, LX + 0.6, SY + 0.9, LZ - 1.2, LX - 1.0, SY + 1.0, LZ + 4.2);
            steps.add(Step.server(o -> {
                m[0] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX - 3.4, SY + 2, LZ + 6.0, 205.0f, l -> l.setVariant(0));
                m[1] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX + 0.8, SY, LZ + 2.6, 118.0f, l -> l.setVariant(1));
            }));
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_leopard_pair"));
            // von der Seite: das Anschleichen (geduckt, Schwanz flach) an eine Ziege heran
            steps.add(Step.server(o -> {
                clear(o, LX, SY, LZ);
                m[0] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX + 1.4, SY, LZ + 3.2, 90.0f, l -> {
                    l.setVariant(0);
                    l.setStalking(true);
                });
                m[1] = creature(o, ModEntities.FARM_GOAT.get(), LX - 3.0, SY, LZ + 3.4, 285.0f, g -> { });
            }));
            look(steps, LX - 0.6, SY + 0.5, LZ - 1.8, LX - 0.6, SY + 0.6, LZ + 3.2);
            steps.add(Step.waitTicks(45));
            steps.add(Step.screenshot("beast_leopard_stalk"));
            // der Sprung: mitten in der Luft, voll gestreckt
            steps.add(Step.server(o -> {
                clear(o, LX, SY, LZ);
                m[1] = creature(o, ModEntities.FARM_GOAT.get(), LX - 3.3, SY, LZ + 3.4, 75.0f, g -> { });
                m[0] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX + 0.2, SY + 1.0, LZ + 3.2, 90.0f, l -> l.setVariant(0));
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], SnowLeopardEntity.EVENT_POUNCE)));
            steps.add(Step.waitTicks(7));
            steps.add(Step.screenshot("beast_leopard_pounce"));
            // gezähmt: liegt neben seinem Jungen
            steps.add(Step.server(o -> {
                clear(o, LX, SY, LZ);
                m[0] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX - 0.4, SY, LZ + 3.0, 205.0f, l -> {
                    l.setVariant(1);
                    l.setTame(true, true);
                    l.setOrderedToSit(true);
                    l.setInSittingPose(true);
                });
                m[1] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX + 1.7, SY, LZ + 2.2, 150.0f, l -> {
                    l.setVariant(1);
                    l.setBaby(true);
                });
            }));
            look(steps, LX + 3.4, SY + 0.5, LZ + 1.4, LX + 0.2, SY + 0.3, LZ + 2.8);
            steps.add(Step.waitTicks(50));
            steps.add(Step.screenshot("beast_leopard_rest"));
            // nah: das Gesicht
            steps.add(Step.server(o -> {
                clear(o, LX, SY, LZ);
                m[0] = creature(o, BiomeBeasts.SNOW_LEOPARD.get(), LX + 0.5, SY, LZ + 2.6, 192.0f, l -> l.setVariant(0));
            }));
            look(steps, LX + 1.1, SY - 0.1, LZ + 0.2, LX + 0.5, SY + 0.75, LZ + 2.6);
            steps.add(Step.waitTicks(30));
            steps.add(Step.screenshot("beast_leopard_close"));
            steps.add(Step.server(o -> clear(o, LX, SY, LZ)));
        }

        // ------------------------------------------------------------------ Urelch
        if (only.isEmpty() || only.equals("elk")) {
            build(steps, level -> grove(level, EX, SY, EZ), OverworldBiomes.PRIMEVAL_FOREST, EX, SY, EZ);
            // die Herde: der Bulle vorn, eine grasende Kuh und ein Kalb dahinter
            arrive(steps, EX + 1.8, SY + 0.6, EZ - 3.0, EX - 1.2, SY + 1.6, EZ + 4.2);
            steps.add(Step.server(o -> {
                m[0] = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX - 0.5, SY, EZ + 2.5, 205.0f, e -> e.setBull(true));
                m[1] = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX - 5.0, SY, EZ + 7.0, 140.0f, e -> {
                    e.setBull(false);
                    e.setEating(true);
                });
                m[2] = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX - 2.6, SY, EZ + 7.6, 160.0f, e -> {
                    e.setBull(false);
                    e.setBaby(true);
                });
            }));
            steps.add(Step.waitTicks(50));
            steps.add(Step.screenshot("beast_elk_herd"));
            // der Brunftruf, von der Seite
            steps.add(Step.server(o -> {
                clear(o, EX, SY, EZ);
                m[0] = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX + 0.5, SY, EZ + 3.0, 90.0f, e -> e.setBull(true));
            }));
            look(steps, EX + 0.6, SY + 0.3, EZ - 2.4, EX + 0.2, SY + 1.9, EZ + 3.0);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], PrimevalElkEntity.EVENT_BUGLE)));
            steps.add(Step.waitTicks(24));
            steps.add(Step.screenshot("beast_elk_bugle"));
            // gesattelt und mit Truhen, ein Ritter reitet ihn
            steps.add(Step.server(o -> {
                clear(o, EX, SY, EZ);
                PrimevalElkEntity elk = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX + 0.0, SY, EZ + 3.0, 150.0f, e -> {
                    e.setBull(true);
                    e.setTamed(true);
                    e.setChest(true);
                    e.setItemSlot(net.minecraft.world.entity.EquipmentSlot.SADDLE, new net.minecraft.world.item.ItemStack(
                            net.minecraft.world.item.Items.SADDLE));
                });
                m[0] = elk;
                Mob knight = creature(o, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), EX, SY + 2, EZ + 3.0, 150.0f, k -> { });
                knight.finalizeSpawn(o, o.getCurrentDifficultyAt(knight.blockPosition()), EntitySpawnReason.COMMAND, null);
                knight.setNoAi(true);
                knight.startRiding(elk, true, false);
                m[1] = knight;
            }));
            look(steps, EX + 3.0, SY + 0.8, EZ - 1.4, EX + 0.0, SY + 1.9, EZ + 3.0);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_elk_ride"));
            // der Ansturm: Schaufeln gesenkt
            steps.add(Step.server(o -> {
                clear(o, EX, SY, EZ);
                m[0] = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX + 1.0, SY, EZ + 2.5, 200.0f, e -> {
                    e.setBull(true);
                    e.setCharging(true);
                });
            }));
            look(steps, EX - 0.2, SY + 0.1, EZ - 1.6, EX + 0.8, SY + 1.4, EZ + 2.5);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_elk_charge"));
            // in der Dämmerung: die Beeren an den Schaufeln glühen
            steps.add(Step.command("time set 12200"));
            steps.add(Step.server(o -> {
                clear(o, EX, SY, EZ);
                m[0] = creature(o, BiomeBeasts.PRIMEVAL_ELK.get(), EX + 0.5, SY, EZ + 3.2, 215.0f, e -> e.setBull(true));
            }));
            look(steps, EX + 1.8, SY + 1.4, EZ + 0.4, EX + 0.5, SY + 2.3, EZ + 3.2);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_elk_dusk"));
            steps.add(Step.command("time set 1000"));
            steps.add(Step.server(o -> clear(o, EX, SY, EZ)));
        }

        // ------------------------------------------------------------------ Sturmmöwe
        if (only.isEmpty() || only.equals("gull")) {
            build(steps, level -> coast(level, GX, SY, GZ), OverworldBiomes.STORM_COAST, GX, SY, GZ, 120, 170);
            // der Schwarm über dem Steg: drei in der Luft, eine auf dem Pfahl, eine dunkle auf dem Wasser
            arrive(steps, GX - 0.5, SY + 0.3, GZ - 5.0, GX + 1.2, SY + 3.0, GZ + 8.0);
            steps.add(Step.server(o -> {
                m[0] = gull(o, GX - 0.8, SY + 2.7, GZ + 2.8, 150.0f, 0, true);
                m[1] = gull(o, GX + 3.2, SY + 5.0, GZ + 9.5, 95.0f, 1, true);
                m[2] = gull(o, GX - 3.6, SY + 4.3, GZ + 12.5, 290.0f, 0, true);
                m[3] = gull(o, GX + 3.5, SY + 2.0, GZ + 6.5, 200.0f, 0, false);
                m[4] = gull(o, GX - 2.5, SY - 0.32, GZ + 8.5, 160.0f, 1, false);
            }));
            steps.add(Step.waitTicks(45));
            steps.add(Step.screenshot("beast_gull_flock"));
            // auf dem Pfahl: sie lacht, Hals gereckt, Schnabel offen
            steps.add(Step.server(o -> {
                clear(o, GX, SY, GZ);
                m[0] = gull(o, GX + 3.5, SY + 2.0, GZ + 6.5, 70.0f, 0, false);
            }));
            look(steps, GX + 1.1, SY + 0.75, GZ + 4.7, GX + 3.5, SY + 2.45, GZ + 6.5);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], StormGullEntity.EVENT_CALL)));
            steps.add(Step.waitTicks(9));
            steps.add(Step.screenshot("beast_gull_call"));
            // der Dieb: mit dem gestohlenen Kabeljau davon, eine Mantelmöwe hinterher
            steps.add(Step.server(o -> {
                clear(o, GX, SY, GZ);
                m[0] = gull(o, GX + 1.0, SY + 2.6, GZ + 4.6, 128.0f, 0, true);
                ((StormGullEntity) m[0]).setCarried(new ItemStack(Items.COD));
                m[1] = gull(o, GX + 3.4, SY + 3.5, GZ + 8.8, 140.0f, 1, true);
            }));
            look(steps, GX + 3.4, SY + 0.9, GZ + 0.4, GX + 1.6, SY + 2.9, GZ + 5.4);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_gull_thief"));
            // am Felsen: eine helle pickt, die dunkle Mantelmöwe schaut zu
            steps.add(Step.server(o -> {
                clear(o, GX, SY, GZ);
                m[0] = gull(o, GX - 5.0, SY + 1.0, GZ + 3.1, 105.0f, 0, false);
                m[1] = gull(o, GX - 6.4, SY + 1.0, GZ + 3.7, 235.0f, 1, false);
            }));
            look(steps, GX - 3.2, SY + 0.35, GZ + 0.1, GX - 5.6, SY + 1.5, GZ + 3.4);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], StormGullEntity.EVENT_PECK)));
            steps.add(Step.waitTicks(3));
            steps.add(Step.screenshot("beast_gull_rock"));
            steps.add(Step.server(o -> clear(o, GX, SY, GZ)));
        }

        // ------------------------------------------------------------------ Greif
        if (only.isEmpty() || only.equals("griffin")) {
            build(steps, level -> gorge(level, KX, SY, KZ), OverworldBiomes.ROCK_GORGES, KX, SY, KZ);
            // auf seiner Felsnadel sitzend, von unten; der Rostgreif auf einem Felsen daneben
            arrive(steps, KX - 3.6, SY + 0.2, KZ - 4.6, KX + 0.6, SY + 6.6, KZ + 7.0);
            steps.add(Step.server(o -> {
                m[0] = griffin(o, KX + 2.5, SY + 9.0, KZ + 6.5, 150.0f, 0, false, g -> g.setResting(true));
                m[1] = griffin(o, KX - 4.5, SY + 3.0, KZ + 9.5, 205.0f, 1, false, g -> { });
            }));
            steps.add(Step.waitTicks(50));
            steps.add(Step.screenshot("beast_griffin_perch"));
            // hoch über der Schlucht: der goldene schlägt, der Rostgreif segelt weiter hinten
            steps.add(Step.server(o -> {
                clear(o, KX, SY, KZ);
                m[0] = griffin(o, KX + 0.5, SY + 8.0, KZ + 8.0, 250.0f, 0, true, g -> { });
                m[1] = griffin(o, KX - 7.0, SY + 13.0, KZ + 22.0, 75.0f, 1, true, g -> { });
            }));
            look(steps, KX - 2.0, SY + 1.5, KZ - 3.0, KX - 0.6, SY + 9.6, KZ + 10.0);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_griffin_soar"));
            // der Sturzflug auf einen Hasen: Schwingen zurück, Nase hinab, die Fänge voran
            steps.add(Step.server(o -> {
                clear(o, KX, SY, KZ);
                m[1] = creature(o, net.minecraft.world.entity.EntityTypes.RABBIT, KX + 0.4, SY, KZ + 2.8, 90.0f, r -> { });
                m[0] = griffin(o, KX + 0.6, SY + 4.6, KZ + 5.6, 195.0f, 0, true, g -> {
                    g.setDiving(true);
                    g.setXRot(52.0f);
                });
            }));
            look(steps, KX + 6.6, SY + 1.0, KZ + 3.4, KX + 0.4, SY + 3.4, KZ + 4.4);
            steps.add(Step.waitTicks(40));
            steps.add(Step.screenshot("beast_griffin_dive"));
            // der Zugriff: Fänge vor, die Schwingen bremsen
            steps.add(Step.server(o -> {
                clear(o, KX, SY, KZ);
                m[1] = creature(o, net.minecraft.world.entity.EntityTypes.RABBIT, KX + 0.2, SY, KZ + 3.2, 40.0f, r -> { });
                m[0] = griffin(o, KX + 0.3, SY + 1.7, KZ + 4.6, 168.0f, 0, true, g -> g.setXRot(-8.0f));
            }));
            look(steps, KX - 3.6, SY + 0.3, KZ + 0.2, KX + 0.2, SY + 2.3, KZ + 4.2);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], GriffinEntity.EVENT_STRIKE)));
            steps.add(Step.waitTicks(4));
            steps.add(Step.screenshot("beast_griffin_strike"));
            // der Schrei: Kopf hoch, Schnabel auf, die Schwingen gelüpft
            steps.add(Step.server(o -> {
                clear(o, KX, SY, KZ);
                m[0] = griffin(o, KX + 0.5, SY, KZ + 4.2, 140.0f, 0, false, g -> { });
            }));
            look(steps, KX - 2.4, SY + 0.5, KZ + 0.4, KX + 0.4, SY + 2.0, KZ + 4.2);
            steps.add(Step.waitTicks(30));
            steps.add(Step.server(o -> event(m[0], GriffinEntity.EVENT_SCREECH)));
            steps.add(Step.waitTicks(10));
            steps.add(Step.screenshot("beast_griffin_screech"));
            // der Rostgreif, nah: sitzend auf einem Felsblock
            steps.add(Step.server(o -> {
                clear(o, KX, SY, KZ);
                m[0] = griffin(o, KX - 4.5, SY + 1.0, KZ + 3.5, 120.0f, 1, false, g -> g.setResting(true));
            }));
            look(steps, KX - 1.0, SY + 0.6, KZ + 0.0, KX - 4.4, SY + 2.5, KZ + 3.6);
            steps.add(Step.waitTicks(50));
            steps.add(Step.screenshot("beast_griffin_rust"));
            steps.add(Step.server(o -> clear(o, KX, SY, KZ)));
        }
    }

    // =====================================================================================
    // Bühnen
    // =====================================================================================

    private static void build(Deque<Step> steps, Consumer<ServerLevel> stage, ResourceKey<Biome> biome, int cx, int y, int cz) {
        build(steps, stage, biome, cx, y, cz, 48, 60);
    }

    /** Baut die Bühne und setzt das Biom (in Scheiben, damit keine das Befehlslimit überschreitet). */
    private static void build(Deque<Step> steps, Consumer<ServerLevel> stage, ResourceKey<Biome> biome, int cx, int y, int cz, int rx, int zMax) {
        boolean[] ready = new boolean[1];
        steps.add(Step.server(o -> {
            stage.accept(o);
            var holder = o.registryAccess().lookupOrThrow(net.minecraft.core.registries.Registries.BIOME).getOrThrow(biome);
            for (int x = cx - rx; x <= cx + rx; x += 8) {
                for (int z = cz - 32; z <= cz + zMax; z += 90) {
                    net.minecraft.server.commands.FillBiomeCommand.fill(o, new BlockPos(x, y - 12, z), new BlockPos(x + 7, y + 30,
                            Math.min(z + 89, cz + zMax)), holder);
                }
            }
            ready[0] = true;
        }));
        steps.add(Step.until(() -> ready[0], 6000));
    }

    private static void clearAbove(ServerLevel level, int cx, int y, int cz) {
        Showcase.fill(level, new BlockPos(cx - 46, y, cz - 30), new BlockPos(cx + 46, y + 30, cz + 58), Blocks.AIR.defaultBlockState());
    }

    /** Ein verschneiter Gipfelgrat: Schnee und Packeis, Felsen aus Raureifstein, hinten ein Kamm, verschneite Fichten. */
    private static void frost(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(85);
        BlockState rime = ModBlocks.RIME_STONE.base().get().defaultBlockState();
        Showcase.fill(level, new BlockPos(cx - 46, y - 4, cz - 30), new BlockPos(cx + 46, y - 2, cz + 58), rime);
        Showcase.fill(level, new BlockPos(cx - 46, y - 1, cz - 30), new BlockPos(cx + 46, y - 1, cz + 58), Blocks.SNOW_BLOCK.defaultBlockState());
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 58; z++) {
                int r = random.nextInt(100);
                if (r < 6) {
                    set(level, x, y - 1, z, Blocks.PACKED_ICE.defaultBlockState());
                } else if (r < 9) {
                    set(level, x, y - 1, z, ModBlocks.FROZEN_SOIL.get().defaultBlockState());
                } else if (r < 40) {
                    set(level, x, y, z, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + random.nextInt(2)));
                } else if (r < 41 && !front(x, z, cx, cz)) {
                    set(level, x, y, z, ModBlocks.FROST_FLOWER.get().defaultBlockState());
                }
            }
        }
        // ein Felsen hinten links, zwei Blöcke hoch, oben verschneit (darauf steht der helle Leopard)
        for (int dx = -5; dx <= -2; dx++) {
            for (int dz = 5; dz <= 8; dz++) {
                int h = (dx == -5 || dz == 8) ? 3 : 2;
                for (int dy = 0; dy < h; dy++) {
                    set(level, cx + dx, y + dy, cz + dz, rime);
                }
                set(level, cx + dx, y + h, cz + dz, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1));
            }
        }
        // vorn: nur Schnee (keine Blumen, keine Verwehungen, in denen die Pfoten verschwänden)
        Showcase.fill(level, new BlockPos(cx - 4, y, cz - 4), new BlockPos(cx + 4, y, cz + 4), Blocks.AIR.defaultBlockState());
        // Felsen und ein Kamm, hinten höher
        for (int i = 0; i < 80; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz - 24 + random.nextInt(80);
            if (front(x, z, cx, cz) || Math.abs(x - cx) < 9 && z < cz + 14) {
                continue;
            }
            int h = 2 + random.nextInt(z > cz + 20 ? 16 : 5);
            int w = 1 + random.nextInt(3);
            for (int dx = -w; dx <= w; dx++) {
                for (int dz = -w; dz <= w; dz++) {
                    int hh = h - (Math.abs(dx) + Math.abs(dz)) * 2;
                    for (int dy = 0; dy < hh; dy++) {
                        set(level, x + dx, y + dy, z + dz, random.nextInt(9) == 0 ? Blocks.PACKED_ICE.defaultBlockState() : rime);
                    }
                    if (hh > 0) {
                        set(level, x + dx, y + hh, z + dz, Blocks.SNOW.defaultBlockState().setValue(SnowLayerBlock.LAYERS, 1 + random.nextInt(3)));
                    }
                }
            }
        }
        // verschneite Fichten an den Seiten
        for (int i = 0; i < 26; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz - 20 + random.nextInt(70);
            if (front(x, z, cx, cz) || Math.abs(x - cx) < 12 && z < cz + 16) {
                continue;
            }
            spruce(level, x, y, z, 6 + random.nextInt(5));
        }
    }

    /** Eine Lichtung im Urhain: Gras, Podsol und Moos, Farne, Leuchtkappen; ringsum Riesenstämme aus Urholz mit dichter Krone. */
    private static void grove(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(86);
        Showcase.fill(level, new BlockPos(cx - 46, y - 4, cz - 30), new BlockPos(cx + 46, y - 2, cz + 58), Blocks.DIRT.defaultBlockState());
        Showcase.fill(level, new BlockPos(cx - 46, y - 1, cz - 30), new BlockPos(cx + 46, y - 1, cz + 58),
                Blocks.GRASS_BLOCK.defaultBlockState());
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 58; z++) {
                int r = random.nextInt(100);
                boolean clearing = Math.abs(x - cx) < 6 && z > cz - 8 && z < cz + 9;
                if (r < 12) {
                    set(level, x, y - 1, z, Blocks.PODZOL.defaultBlockState());
                } else if (r < 18) {
                    set(level, x, y - 1, z, Blocks.MOSS_BLOCK.defaultBlockState());
                } else if (r < 19 && !clearing) {
                    set(level, x, y - 1, z, ModBlocks.GLOWING_MOSS.get().defaultBlockState());
                }
                int p = random.nextInt(100);
                if (clearing) {
                    if (p < 18) {
                        set(level, x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                    }
                } else if (p < 22) {
                    set(level, x, y, z, Blocks.FERN.defaultBlockState());
                } else if (p < 30) {
                    set(level, x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                } else if (p < 32) {
                    set(level, x, y, z, ModBlocks.GLOWCAP.get().defaultBlockState());
                } else if (p < 33) {
                    set(level, x, y, z, ModBlocks.GROVE_BELL.get().defaultBlockState());
                }
            }
        }
        // Riesenstämme (2×2) mit breiter Krone, vorn frei
        for (int i = 0; i < 18; i++) {
            int x = cx - 42 + random.nextInt(85);
            int z = cz - 18 + random.nextInt(72);
            if (Math.abs(x - cx) < 13 && z < cz + 18) {
                continue;
            }
            giant(level, x, y, z, 14 + random.nextInt(9), random);
        }
        // ein umgestürzter, bemooster Stamm links
        for (int dx = -12; dx <= -6; dx++) {
            set(level, cx + dx, y, cz + 9, ModBlocks.ANCIENT_LOG.get().defaultBlockState().setValue(BlockStateProperties.AXIS,
                    net.minecraft.core.Direction.Axis.X));
            set(level, cx + dx, y + 1, cz + 9, Blocks.MOSS_CARPET.defaultBlockState());
        }
    }

    private static void giant(ServerLevel level, int x, int y, int z, int height, RandomSource random) {
        BlockState log = ModBlocks.ANCIENT_LOG.get().defaultBlockState();
        for (int dy = -1; dy < height; dy++) {
            for (int dx = 0; dx <= 1; dx++) {
                for (int dz = 0; dz <= 1; dz++) {
                    set(level, x + dx, y + dy, z + dz, log);
                }
            }
        }
        // Wurzelfüße
        for (int[] d : new int[][] {{-1, 0}, {2, 1}, {0, -1}, {1, 2}}) {
            set(level, x + d[0], y, z + d[1], log);
        }
        BlockState leaves = ModBlocks.ANCIENT_LEAVES.get().defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
        int r = 4 + random.nextInt(2);
        for (int dx = -r; dx <= r + 1; dx++) {
            for (int dz = -r; dz <= r + 1; dz++) {
                for (int dy = -2; dy <= 2; dy++) {
                    double d = Math.pow(dx - 0.5, 2) + Math.pow(dz - 0.5, 2) + dy * dy * 3.0;
                    if (d <= r * r && level.getBlockState(new BlockPos(x + dx, y + height + dy, z + dz)).isAir()) {
                        set(level, x + dx, y + height + dy, z + dz, leaves);
                    }
                }
            }
        }
    }

    private static void spruce(ServerLevel level, int x, int y, int z, int height) {
        for (int dy = 0; dy < height; dy++) {
            set(level, x, y + dy, z, Blocks.SPRUCE_LOG.defaultBlockState());
        }
        BlockState leaves = Blocks.SPRUCE_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
        for (int dy = 2; dy <= height; dy++) {
            int r = Math.max(0, (height - dy) / 2);
            if ((height - dy) % 2 == 1) {
                r = Math.max(0, r - 1);
            }
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.abs(dx) + Math.abs(dz) <= r + 1 && level.getBlockState(new BlockPos(x + dx, y + dy, z + dz)).isAir()) {
                        set(level, x + dx, y + dy, z + dz, leaves);
                        if (level.getBlockState(new BlockPos(x + dx, y + dy + 1, z + dz)).isAir()) {
                            set(level, x + dx, y + dy + 1, z + dz, Blocks.SNOW.defaultBlockState());
                        }
                    }
                }
            }
        }
        set(level, x, y + height + 1, z, leaves);
    }

    /**
     * Die Sturmküste: ein Strand aus Kies und Sturmstein, ein Steg aus Fichtenholz mit Pfählen, die graue See bis zum Horizont,
     * draußen Brandungstürme aus Sturmstein und Schiefer, am Ufer umspülte Felsen.
     */
    private static void coast(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(87);
        BlockState storm = ModBlocks.STORMSTONE.base().get().defaultBlockState();
        BlockState cobbled = ModBlocks.COBBLED_STORMSTONE.base().get().defaultBlockState();
        BlockState shale = ModBlocks.SHALE.base().get().defaultBlockState();
        BlockState gravel = Blocks.GRAVEL.defaultBlockState();
        BlockState water = Blocks.WATER.defaultBlockState();
        // die See bis zum Horizont: drei Blöcke tief über Sturmstein
        Showcase.fill(level, new BlockPos(cx - 120, y - 4, cz - 30), new BlockPos(cx + 120, y - 4, cz + 170), storm);
        Showcase.fill(level, new BlockPos(cx - 120, y - 3, cz - 30), new BlockPos(cx + 120, y - 1, cz + 170), water);
        // der Strand: geschwungene Uferlinie, davor ein Streifen Flachwasser über Kies
        for (int x = cx - 46; x <= cx + 46; x++) {
            int dx = x - cx;
            int shore = cz + 4 + (int) Math.round(1.5 * Math.sin(dx * 0.25) + 2.0 * Math.sin(dx * 0.09 + 1.0));
            for (int z = cz - 30; z <= shore + 2; z++) {
                set(level, x, y - 3, z, storm);
                if (z > shore) {
                    set(level, x, y - 2, z, gravel);
                    continue;
                }
                set(level, x, y - 2, z, storm);
                int r = random.nextInt(100);
                BlockState top = r < 46 ? gravel : r < 70 ? storm : r < 86 ? cobbled : r < 92 ? Blocks.ANDESITE.defaultBlockState() : gravel;
                if (z < cz - 9 && random.nextInt(3) > 0) {
                    top = Blocks.GRASS_BLOCK.defaultBlockState();
                }
                set(level, x, y - 1, z, top);
                if (top.is(Blocks.GRASS_BLOCK) && random.nextInt(3) == 0) {
                    set(level, x, y, z, Blocks.SHORT_GRASS.defaultBlockState());
                } else if (!front(x, z, cx, cz) && z < shore - 1 && random.nextInt(40) == 0) {
                    set(level, x, y, z, ModBlocks.STORM_CRYSTAL.get().defaultBlockState());
                }
            }
        }
        // umspülte Steine im Flachwasser
        for (int i = 0; i < 26; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = cz + 3 + random.nextInt(10);
            if (Math.abs(x - cx - 4) < 3 || Math.abs(x - cx) < 6 && z < cz + 10) {
                continue;
            }
            set(level, x, y - 1, z, random.nextBoolean() ? storm : cobbled);
            if (random.nextInt(3) == 0) {
                set(level, x, y, z, storm);
            }
        }
        // ein flacher Felsen links am Ufer (darauf stehen zwei Möwen)
        for (int dx = -7; dx <= -4; dx++) {
            for (int dz = 2; dz <= 4; dz++) {
                if ((dx == -7 || dx == -4) && (dz == 2 || dz == 4)) {
                    continue;
                }
                set(level, cx + dx, y - 1, cz + dz, storm);
                set(level, cx + dx, y, cz + dz, (dx + dz) % 3 == 0 ? cobbled : storm);
            }
        }
        set(level, cx - 8, y - 1, cz + 3, storm);
        set(level, cx - 8, y, cz + 3, Blocks.ANDESITE.defaultBlockState());
        // Felsbrocken am Strand, seitlich
        for (int i = 0; i < 14; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz - 20 + random.nextInt(22);
            if (Math.abs(x - cx) < 11) {
                continue;
            }
            int w = 1 + random.nextInt(2);
            for (int ox = -w; ox <= w; ox++) {
                for (int oz = -w; oz <= w; oz++) {
                    int h = 1 + w - Math.max(Math.abs(ox), Math.abs(oz)) + random.nextInt(2);
                    for (int oy = 0; oy < h; oy++) {
                        set(level, x + ox, y + oy, z + oz, random.nextInt(7) == 0 ? ModBlocks.CHARGED_STORMSTONE.get().defaultBlockState() : storm);
                    }
                }
            }
        }
        // draußen die Brandungstürme
        int[][] stacks = {{-30, 34, 18, 4}, {-12, 52, 26, 5}, {22, 40, 14, 3}, {42, 62, 30, 6}, {-52, 70, 22, 5}, {8, 88, 34, 6}, {-26, 18, 9, 2},
                {34, 22, 7, 2}};
        for (int[] st : stacks) {
            seaStack(level, cx + st[0], y - 4, cz + st[1], st[2], st[3], storm, shale, random);
        }
        // der Steg: Fichtenstufen auf Pfählen, an den Rändern Poller; vorn ein Fass, am Ende eine Laterne
        BlockState deck = Blocks.SPRUCE_SLAB.defaultBlockState();
        BlockState post = Blocks.SPRUCE_LOG.defaultBlockState();
        for (int z = cz + 1; z <= cz + 17; z++) {
            for (int x = cx + 3; x <= cx + 5; x++) {
                set(level, x, y, z, deck);
                if ((x == cx + 3 || x == cx + 5) && (z - cz) % 3 == 0) {
                    for (int dy = -3; dy < 0; dy++) {
                        set(level, x, y + dy, z, post);
                    }
                }
            }
        }
        for (int z : new int[] {cz + 6, cz + 10, cz + 14, cz + 17}) {
            for (int x : new int[] {cx + 3, cx + 5}) {
                for (int dy = -3; dy <= 1; dy++) {
                    set(level, x, y + dy, z, post);
                }
            }
        }
        set(level, cx + 5, y, cz + 2, Blocks.BARREL.defaultBlockState());
        set(level, cx + 5, y + 2, cz + 17, Blocks.LANTERN.defaultBlockState());
        set(level, cx + 4, y + 1, cz + 3, Blocks.SPRUCE_TRAPDOOR.defaultBlockState());
    }

    /** Ein Brandungsturm: unten breit, oben schmal, mit Schieferbändern; manchmal ein Grasschopf obenauf. */
    private static void seaStack(ServerLevel level, int x, int y, int z, int height, int radius, BlockState storm, BlockState shale,
            RandomSource random) {
        double lean = (random.nextDouble() - 0.5) * 0.12;
        int top = y;
        for (int dy = 0; dy < height; dy++) {
            double r = radius * (1.0 - dy / (height * 1.35)) + 0.4;
            int ox = (int) Math.round(lean * dy);
            BlockState band = (dy / 3) % 3 == 1 ? shale : storm;
            for (int dx = -radius - 1; dx <= radius + 1; dx++) {
                for (int dz = -radius - 1; dz <= radius + 1; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz) + random.nextDouble() * 0.6;
                    if (d <= r) {
                        set(level, x + dx + ox, y + dy, z + dz, random.nextInt(14) == 0 ? shale : band);
                        top = y + dy;
                    }
                }
            }
        }
        if (radius >= 4) {
            int ox = (int) Math.round(lean * (height - 1));
            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    if (level.getBlockState(new BlockPos(x + dx + ox, top, z + dz)).is(Blocks.AIR)) {
                        continue;
                    }
                    set(level, x + dx + ox, top, z + dz, Blocks.GRASS_BLOCK.defaultBlockState());
                    if (random.nextBoolean()) {
                        set(level, x + dx + ox, top + 1, z + dz, Blocks.SHORT_GRASS.defaultBlockState());
                    }
                }
            }
        }
    }

    /** Gebänderter Fels wie in den Wänden der Felsschluchten (Tuff, Schiefer, Stein, Kalzit, Andesit, Terrakotta, Tropfstein, Basalt). */
    private static BlockState strata(int dy) {
        Block[] bands = {Blocks.TUFF, ModBlocks.SHALE.base().get(), Blocks.STONE, Blocks.CALCITE, Blocks.ANDESITE, ModBlocks.SHALE.base().get(),
                Blocks.TERRACOTTA, Blocks.TUFF, Blocks.DRIPSTONE_BLOCK, Blocks.STONE, ModBlocks.SHALE.base().get(), Blocks.SMOOTH_BASALT};
        int[] thickness = {3, 2, 4, 1, 2, 3, 2, 3, 1, 3, 2, 1};
        int k = Math.floorMod(dy + 2, 27);
        for (int i = 0; i < bands.length; i++) {
            if (k < thickness[i]) {
                return bands[i].defaultBlockState();
            }
            k -= thickness[i];
        }
        return Blocks.TUFF.defaultBlockState();
    }

    /**
     * Eine Felsschlucht: trockenes Gras, grober Boden und Geröll, gebänderte Felsnadeln, hinten die Schluchtwand, Akazien. Vorn
     * rechts eine hohe Nadel mit flacher Kuppe (darauf sitzt der Greif), links ein niedriger Felsen und ein Felsblock.
     */
    private static void gorge(ServerLevel level, int cx, int y, int cz) {
        clearAbove(level, cx, y, cz);
        RandomSource random = RandomSource.create(88);
        Showcase.fill(level, new BlockPos(cx - 46, y - 4, cz - 30), new BlockPos(cx + 46, y - 2, cz + 58), Blocks.TUFF.defaultBlockState());
        Showcase.fill(level, new BlockPos(cx - 46, y - 1, cz - 30), new BlockPos(cx + 46, y - 1, cz + 58), Blocks.GRASS_BLOCK.defaultBlockState());
        for (int x = cx - 46; x <= cx + 46; x++) {
            for (int z = cz - 30; z <= cz + 58; z++) {
                int r = random.nextInt(100);
                if (r < 28) {
                    set(level, x, y - 1, z, Blocks.COARSE_DIRT.defaultBlockState());
                } else if (r < 36) {
                    set(level, x, y - 1, z, ModBlocks.RUBBLE.get().defaultBlockState());
                }
                int p = random.nextInt(100);
                boolean clearing = Math.abs(x - cx) < 5 && z > cz - 6 && z < cz + 6;
                if (p < (clearing ? 6 : 18) && r >= 28) {
                    set(level, x, y, z, Blocks.SHORT_DRY_GRASS.defaultBlockState());
                } else if (p < 22 && !clearing && r >= 28) {
                    set(level, x, y, z, Blocks.TALL_DRY_GRASS.defaultBlockState());
                } else if (p < 23 && !clearing) {
                    set(level, x, y, z, Blocks.DEAD_BUSH.defaultBlockState());
                }
            }
        }
        // die Felsnadel des Greifen: rund, nach oben schmaler, oben eine flache Kuppe (3×3)
        spire(level, cx + 2, y, cz + 6, 9, 2.6, 1.6, random);
        Showcase.fill(level, new BlockPos(cx + 1, y + 9, cz + 5), new BlockPos(cx + 3, y + 9, cz + 7), Blocks.AIR.defaultBlockState());
        Showcase.fill(level, new BlockPos(cx + 1, y + 8, cz + 5), new BlockPos(cx + 3, y + 8, cz + 7), ModBlocks.SHALE.base().get().defaultBlockState());
        // links ein niedriger Felsen (3 hoch) und ein flacher Block (1 hoch)
        for (int dx = -6; dx <= -3; dx++) {
            for (int dz = 8; dz <= 11; dz++) {
                int h = (dx == -6 || dz == 11) ? 4 : 3;
                for (int dy = 0; dy < h; dy++) {
                    set(level, cx + dx, y + dy, cz + dz, strata(dy));
                }
            }
        }
        for (int dx = -6; dx <= -3; dx++) {
            for (int dz = 2; dz <= 5; dz++) {
                if ((dx == -6 || dx == -3) && (dz == 2 || dz == 5)) {
                    continue;
                }
                set(level, cx + dx, y, cz + dz, ModBlocks.SHALE.base().get().defaultBlockState());
            }
        }
        // weitere Nadeln, hinten höher, und die Schluchtwand
        for (int i = 0; i < 26; i++) {
            int x = cx - 44 + random.nextInt(89);
            int z = cz + 12 + random.nextInt(40);
            if (Math.abs(x - cx) < 8 && z < cz + 20) {
                continue;
            }
            spire(level, x, y, z, 8 + random.nextInt(z > cz + 30 ? 22 : 12), 1.6 + random.nextDouble() * 2.4, 0.8 + random.nextDouble(), random);
        }
        for (int x = cx - 46; x <= cx + 46; x++) {
            int h = 18 + (int) Math.round(6.0 * Math.sin(x * 0.11) + 4.0 * Math.sin(x * 0.29 + 1.3));
            int depth = 4 + random.nextInt(3);
            for (int z = cz + 58 - depth; z <= cz + 58; z++) {
                for (int dy = 0; dy < h; dy++) {
                    set(level, x, y + dy, z, strata(dy));
                }
                set(level, x, y + h, z, Blocks.GRASS_BLOCK.defaultBlockState());
            }
        }
        // Akazien an den Seiten
        for (int i = 0; i < 9; i++) {
            int x = cx - 40 + random.nextInt(81);
            int z = cz - 14 + random.nextInt(44);
            if (Math.abs(x - cx) < 12 && z < cz + 16) {
                continue;
            }
            acacia(level, x, y, z, random);
        }
    }

    /** Eine Felsnadel aus gebändertem Fels: unten {@code r0}, oben {@code r1} breit, mit etwas Unruhe im Umriss. */
    private static void spire(ServerLevel level, int x, int y, int z, int height, double r0, double r1, RandomSource random) {
        int reach = (int) Math.ceil(r0) + 1;
        for (int dy = -1; dy < height; dy++) {
            double t = Math.max(0, dy) / (double) Math.max(1, height - 1);
            double r = r0 + (r1 - r0) * t;
            for (int dx = -reach; dx <= reach; dx++) {
                for (int dz = -reach; dz <= reach; dz++) {
                    double d = Math.sqrt(dx * dx + dz * dz) + (random.nextDouble() - 0.5) * 0.6;
                    if (d <= r) {
                        set(level, x + dx, y + dy, z + dz, strata(dy));
                    }
                }
            }
        }
    }

    /** Eine Akazie: schräger Stamm, flache Schirmkrone. */
    private static void acacia(ServerLevel level, int x, int y, int z, RandomSource random) {
        BlockState log = Blocks.ACACIA_LOG.defaultBlockState();
        BlockState leaves = Blocks.ACACIA_LEAVES.defaultBlockState().setValue(BlockStateProperties.PERSISTENT, true);
        int h = 4 + random.nextInt(3);
        int ox = 0;
        int oz = 0;
        int lean = random.nextInt(4);
        for (int dy = 0; dy < h; dy++) {
            if (dy >= 2) {
                ox += lean == 0 ? 1 : lean == 1 ? -1 : 0;
                oz += lean == 2 ? 1 : lean == 3 ? -1 : 0;
            }
            set(level, x + ox, y + dy, z + oz, log);
        }
        int top = y + h;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                if (Math.abs(dx) + Math.abs(dz) <= 4) {
                    set(level, x + ox + dx, top, z + oz + dz, leaves);
                }
                if (Math.abs(dx) + Math.abs(dz) <= 2) {
                    set(level, x + ox + dx, top + 1, z + oz + dz, leaves);
                }
            }
        }
    }

    private static boolean front(int x, int z, int cx, int cz) {
        return Math.abs(x - cx) < 10 && z > cz - 14 && z < cz + 10;
    }

    private static void set(ServerLevel level, int x, int y, int z, BlockState state) {
        level.setBlock(new BlockPos(x, y, z), state, Block.UPDATE_CLIENTS);
    }

    // =====================================================================================
    // Hilfen
    // =====================================================================================

    private static <T extends Mob> T creature(ServerLevel level, EntityType<T> type, double x, double y, double z, float yaw, Consumer<T> setup) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(x, y, z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        setup.accept(mob);
        return mob;
    }

    private static Mob griffin(ServerLevel level, double x, double y, double z, float yaw, int variant, boolean flying,
            Consumer<GriffinEntity> setup) {
        return creature(level, BiomeBeasts.GRIFFIN.get(), x, y, z, yaw, g -> {
            g.setVariant(variant);
            g.setFlying(flying);
            setup.accept(g);
        });
    }

    private static Mob gull(ServerLevel level, double x, double y, double z, float yaw, int variant, boolean flying) {
        return creature(level, BiomeBeasts.STORM_GULL.get(), x, y, z, yaw, g -> {
            g.setVariant(variant);
            g.setFlying(flying);
        });
    }

    private static void event(Entity entity, byte id) {
        if (entity.level() instanceof ServerLevel level) {
            level.broadcastEntityEvent(entity, id);
        }
    }

    private static void clear(ServerLevel level, int cx, int y, int cz) {
        AABB all = new AABB(cx - 50, y - 10, cz - 34, cx + 50, y + 34, cz + 62);
        for (Entity e : level.getEntitiesOfClass(Entity.class, all, e -> e.entityTags().contains(STAGE_TAG))) {
            e.discard();
        }
    }

    private static void arrive(Deque<Step> steps, double x, double y, double z, double tx, double ty, double tz) {
        look(steps, x, y, z, tx, ty, tz);
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(600));
        steps.add(Step.waitTicks(30));
    }

    /** Kamera mit den Füßen bei (x, y, z), Blick auf (tx, ty, tz). */
    private static void look(Deque<Step> steps, double x, double y, double z, double tx, double ty, double tz) {
        steps.add(Step.once(minecraft -> {
            double lx = tx - x;
            double ly = ty - (y + 1.62);
            double lz = tz - z;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "execute in %s run tp @s %.2f %.2f %.2f %.1f %.1f", OVERWORLD, x,
                    y, z, yaw, pitch));
        }));
    }
}
