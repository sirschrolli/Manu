package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.boss.BossAttack;
import io.github.sirschrolli.kingdomsatwar.entity.boss.DragonBossEntity;
import io.github.sirschrolli.kingdomsatwar.entity.deep.SordinEntity;
import io.github.sirschrolli.kingdomsatwar.entity.end.PhasmorEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MothQueenEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.StoneKeeperEntity;
import io.github.sirschrolli.kingdomsatwar.entity.nether.GlutschmiedEntity;
import io.github.sirschrolli.kingdomsatwar.entity.nether.IgnaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.serpent.QuetzaraEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.entity.voidrealm.ShatteredKingEntity;
import io.github.sirschrolli.kingdomsatwar.registry.FaeBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.VoidBlocks;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung der neuen Angriffe aus 1.13: je Boss eine eigene Bühne (Boden, freie Luft darüber), davor zähe Ritter ohne
 * eigenen Willen (tausend Lebenspunkte – unverwundbare taugen nicht: Wer unverwundbar ist, den kann kein Wesen zum Ziel
 * nehmen). Jeder neue Angriff wird ausgelöst und im besten Moment fotografiert ({@code attacks113_<name>_<n>}).
 * Aufruf: {@code -Dkingdomsatwar.showcase=attacks113} (alle) oder {@code attacks113_<name>} (einer).
 */
final class AttackShowcase {
    private AttackShowcase() {}

    /** Was eine Bühne zum Auslösen braucht: die Welt, ihre Mitte am Boden, die Ritter (der erste ist das Ziel). */
    private record Stage(ServerLevel level, Vec3 origin, List<LivingEntity> knights, List<Entity> cast) {
        LivingEntity target() {
            return this.knights.getFirst();
        }

        Vec3 at(double dx, double dy, double dz) {
            return this.origin.add(dx, dy, dz);
        }
    }

    /**
     * Eine Bühne: Name, Halbmesser und Höhe des freien Raums, Boden, Ritter (je {dx, dz, Blickrichtung}), Kamera und Blickpunkt
     * (relativ zur Mitte), die Ticks nach dem Auslösen, zu denen fotografiert wird, und was der Boss tut.
     */
    private record Scene(String name, int size, int height, BlockState floor, double[][] knights, double[] cam, double[] focus, int[] at,
            BiConsumer<Stage, Mob[]> spawn, BiConsumer<Stage, Mob[]> start) {}

    private static final List<Scene> SCENES = new ArrayList<>();

    static {
        // ---------------------------------------------------------------- die Drachen
        dragon("flame_crescents", DragonType.FIRE, Blocks.BASALT.defaultBlockState(), new int[] {26});
        dragon("frost_spin", DragonType.ICE, Blocks.PACKED_ICE.defaultBlockState(), new int[] {34});
        dragon("lightning_grid", DragonType.LIGHTNING, Blocks.STONE.defaultBlockState(), new int[] {37});
        dragon("vine_lash", DragonType.NATURE, Blocks.MOSS_BLOCK.defaultBlockState(), new int[] {26});
        dragon("stone_fists", DragonType.EARTH, Blocks.COARSE_DIRT.defaultBlockState(), new int[] {33});
        dragon("hydro_blade", DragonType.WATER, Blocks.SAND.defaultBlockState(), new int[] {32});
        dragon("soul_chains", null, Blocks.BLACKSTONE.defaultBlockState(), new int[] {40});
        // ---------------------------------------------------------------- die übrigen Bosse
        SCENES.add(new Scene("phasmor_gaze", 14, 14, Blocks.END_STONE_BRICKS.defaultBlockState(),
                new double[][] {{0.0, 3.5, 180.0}, {-3.0, 4.0, 200.0}, {3.0, 4.0, 160.0}}, new double[] {7.5, 3.4, 6.5},
                new double[] {0.0, 2.6, 0.0}, new int[] {22},
                (s, boss) -> boss[0] = mob(s, ModEntities.PHASMOR.get(), s.at(0.5, 0.0, -2.5), 0.0f),
                (s, boss) -> {
                    PhasmorEntity king = (PhasmorEntity) boss[0];
                    king.setTarget(s.target());
                    king.forceAttack(PhasmorEntity.Attack.GAZE);
                }));
        SCENES.add(new Scene("quetzara_rain", 16, 14, Blocks.MOSSY_STONE_BRICKS.defaultBlockState(),
                new double[][] {{6.0, 0.0, 90.0}, {7.0, 3.5, 90.0}, {7.0, -3.5, 90.0}}, new double[] {0.0, 11.0, 15.0},
                new double[] {4.0, 1.0, 0.0}, new int[] {36},
                (s, boss) -> {
                    QuetzaraEntity q = mob(s, ModEntities.QUETZARA.get(), s.at(-3.0, 0.0, 0.0), -90.0f);
                    q.layOut(s.at(-3.0, 0.0, 0.0), -90.0f, 0.0f);
                    boss[0] = q;
                },
                (s, boss) -> {
                    QuetzaraEntity q = (QuetzaraEntity) boss[0];
                    q.setTarget(s.target());
                    q.forceAttack(QuetzaraEntity.Attack.RAIN);
                }));
        SCENES.add(new Scene("keeper_cage", 14, 12, FaeBlocks.FAESTONE_BRICKS.base().get().defaultBlockState(),
                new double[][] {{0.0, 4.0, 180.0}}, new double[] {8.0, 6.5, 12.0}, new double[] {0.0, 1.5, 1.5}, new int[] {28},
                (s, boss) -> boss[0] = mob(s, ModEntities.STONE_KEEPER.get(), s.at(0.5, 0.0, -3.5), 0.0f),
                (s, boss) -> {
                    StoneKeeperEntity keeper = (StoneKeeperEntity) boss[0];
                    keeper.setTarget(s.target());
                    keeper.forceAttack(StoneKeeperEntity.Attack.CAGE);
                }));
        SCENES.add(new Scene("sordin_echo", 14, 12, Blocks.DEEPSLATE_TILES.defaultBlockState(),
                new double[][] {{0.0, 5.0, 180.0}, {-3.5, 4.0, 200.0}, {3.5, 4.0, 160.0}}, new double[] {9.5, 5.0, 9.0},
                new double[] {0.0, 1.0, 2.5}, new int[] {27},
                (s, boss) -> boss[0] = mob(s, ModEntities.SORDIN.get(), s.at(0.5, 0.0, -3.5), 0.0f),
                (s, boss) -> {
                    SordinEntity cantor = (SordinEntity) boss[0];
                    cantor.setTarget(s.target());
                    cantor.forceAttack(SordinEntity.Attack.ECHO);
                }));
        SCENES.add(new Scene("glutschmied_grate", 14, 12, Blocks.POLISHED_BLACKSTONE_BRICKS.defaultBlockState(),
                new double[][] {{0.0, 6.0, 180.0}}, new double[] {7.0, 13.5, 15.0}, new double[] {0.0, 0.0, 1.5}, new int[] {34},
                (s, boss) -> boss[0] = mob(s, ModEntities.GLUTSCHMIED.get(), s.at(0.5, 0.0, 0.5), 0.0f),
                (s, boss) -> {
                    GlutschmiedEntity smith = (GlutschmiedEntity) boss[0];
                    smith.setTarget(s.target());
                    smith.forceAttack(GlutschmiedEntity.Attack.GRATE);
                }));
        SCENES.add(new Scene("ignara_wreath", 14, 12, Blocks.POLISHED_BLACKSTONE.defaultBlockState(),
                new double[][] {{0.0, 4.0, 180.0}}, new double[] {8.0, 6.0, 13.0}, new double[] {0.0, 1.0, 2.5}, new int[] {22, 32, 49},
                (s, boss) -> boss[0] = mob(s, ModEntities.IGNARA.get(), s.at(0.5, 0.0, -3.5), 0.0f),
                (s, boss) -> {
                    IgnaraEntity ignara = (IgnaraEntity) boss[0];
                    ignara.setTarget(s.target());
                    ignara.forceAttack(IgnaraEntity.Attack.WREATH);
                }));
        SCENES.add(new Scene("moth_spiral", 18, 18, Blocks.MOSS_BLOCK.defaultBlockState(),
                new double[][] {{0.0, 3.0, 180.0}}, new double[] {2.0, 15.0, 15.0}, new double[] {0.0, 0.0, 2.5}, new int[] {44, 58},
                (s, boss) -> boss[0] = mob(s, ModEntities.MOTH_QUEEN.get(), s.at(8.0, 6.5, 3.0), 0.0f),
                (s, boss) -> {
                    MothQueenEntity queen = (MothQueenEntity) boss[0];
                    queen.setTarget(s.target());
                    queen.forceAttack(MothQueenEntity.Attack.SPIRAL);
                }));
        SCENES.add(new Scene("king_crash", 16, 20, VoidBlocks.ASTRAL_MARBLE.base().get().defaultBlockState(),
                new double[][] {{0.0, 3.0, 180.0}}, new double[] {12.0, 6.5, 15.0}, new double[] {0.0, 6.0, 1.0}, new int[] {28, 35},
                (s, boss) -> boss[0] = mob(s, ModEntities.SHATTERED_KING.get(), s.at(0.5, 2.5, -5.5), 0.0f),
                (s, boss) -> ((ShatteredKingEntity) boss[0]).forceAttack(s.level(), "crash", s.target(), true)));
    }

    /** Ein Drache (oder Nyxar, ohne Element) auf einer großen Bühne, drei Ritter zwölf Blöcke vor ihm. */
    private static void dragon(String attack, DragonType type, BlockState floor, int[] at) {
        boolean water = type == DragonType.WATER;
        SCENES.add(new Scene(attack, 38, 26, floor, new double[][] {{0.0, 12.0, 180.0}, {-5.0, 13.0, 180.0}, {5.0, 13.0, 180.0}},
                new double[] {19.0, 7.5, 11.0}, new double[] {0.0, 3.0, 5.0}, at,
                (s, boss) -> {
                    if (water) {
                        // ein See um Thalassa, die Ritter stehen am Ufer
                        BlockPos o = BlockPos.containing(s.origin());
                        for (BlockPos pos : BlockPos.betweenClosed(o.offset(-9, -4, -9), o.offset(9, -1, 9))) {
                            if (pos.distSqr(new BlockPos(o.getX(), pos.getY(), o.getZ())) <= 9 * 9) {
                                s.level().setBlock(pos, Blocks.WATER.defaultBlockState(), Block.UPDATE_CLIENTS);
                            }
                        }
                    }
                    EntityType<? extends DragonBossEntity> kind = type == null ? ModEntities.NYXAR.get() : ModEntities.bossType(type);
                    DragonBossEntity b = kind.create(s.level(), EntitySpawnReason.COMMAND);
                    if (b == null) {
                        throw new IllegalStateException("Kein Drache: " + attack);
                    }
                    Vec3 p = s.at(0.5, water ? -2.0 : 0.0, 0.5);
                    b.snapTo(p.x, p.y, p.z, 0.0f, 0.0f);
                    b.setYBodyRot(0.0f);
                    b.setYHeadRot(0.0f);
                    b.setHome(BlockPos.containing(s.origin()));
                    s.level().addFreshEntity(b);
                    b.cinematic(water ? DragonBossEntity.MoveMode.WATER : DragonBossEntity.MoveMode.GROUND, 3);
                    s.cast().add(b);
                    boss[0] = b;
                },
                (s, boss) -> {
                    DragonBossEntity b = (DragonBossEntity) boss[0];
                    b.snapTo(s.origin().x + 0.5, b.getY(), s.origin().z + 0.5, 0.0f, 0.0f);
                    b.setYBodyRot(0.0f);
                    BossAttack found = b.findAttack(attack);
                    if (found != null) {
                        b.startAttack(s.level(), found, s.target());
                    }
                }));
    }

    static void attacks(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        int index = 0;
        boolean first = true;
        for (Scene scene : SCENES) {
            int ox = 21000 + index * 200;
            int oz = 21000;
            index++;
            if (!only.isEmpty() && !scene.name().equals(only)) {
                continue;
            }
            Mob[] boss = new Mob[1];
            Stage[] stage = new Stage[1];
            steps.add(Showcase.tp(ox + 0.5, 30, oz - 30.5, 0, 30));
            steps.add(Step.waitTicks(first ? 100 : 20));
            steps.add(Step.waitForChunks(600));
            first = false;
            steps.add(Step.server(level -> {
                int g = Showcase.ground;
                int r = scene.size();
                Showcase.fill(level, new BlockPos(ox - r, g - 4, oz - r), new BlockPos(ox + r, g - 1, oz + r), scene.floor());
                Showcase.fill(level, new BlockPos(ox - r, g, oz - r), new BlockPos(ox + r, g + scene.height(), oz + r), Blocks.AIR.defaultBlockState());
                Stage s = new Stage(level, new Vec3(ox, g, oz), new ArrayList<>(), new ArrayList<>());
                for (double[] k : scene.knights()) {
                    SoldierEntity knight = ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get().create(level, EntitySpawnReason.COMMAND);
                    if (knight == null) {
                        continue;
                    }
                    knight.equip();
                    knight.snapTo(ox + 0.5 + k[0], g, oz + 0.5 + k[1], (float) k[2], 0.0f);
                    knight.setYBodyRot((float) k[2]);
                    knight.setYHeadRot((float) k[2]);
                    knight.setNoAi(true);
                    knight.getAttribute(net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH).setBaseValue(1000.0);
                    knight.setHealth(1000.0f);
                    knight.setPersistenceRequired();
                    level.addFreshEntity(knight);
                    s.knights().add(knight);
                    s.cast().add(knight);
                }
                scene.spawn().accept(s, boss);
                if (boss[0] != null && !s.cast().contains(boss[0])) {
                    s.cast().add(boss[0]);
                }
                stage[0] = s;
            }));
            steps.add(Step.waitTicks(30));
            steps.add(Step.once(minecraft -> {
                Vec3 o = new Vec3(ox + 0.5, Showcase.ground, oz + 0.5);
                Vec3 cam = o.add(scene.cam()[0], scene.cam()[1], scene.cam()[2]);
                Vec3 look = o.add(scene.focus()[0], scene.focus()[1], scene.focus()[2]).subtract(cam);
                float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x, cam.y - 1.62, cam.z,
                        yaw, pitch));
            }));
            steps.add(Step.waitTicks(10));
            steps.add(Step.server(level -> {
                if (stage[0] != null && boss[0] != null) {
                    scene.start().accept(stage[0], boss);
                }
            }));
            // zur Kontrolle im Protokoll: läuft der Angriff?
            steps.add(Step.waitTicks(3));
            steps.add(Step.server(level -> {
                Mob b = boss[0];
                if (b != null) {
                    LOGGER.info("[Showcase] {}: lebt={} Ziel={} Angriff={}", scene.name(), b.isAlive(),
                            b.getTarget() == null ? "-" : b.getTarget().getType().getDescriptionId(), attackOf(b));
                }
            }));
            int waited = 3;
            for (int i = 0; i < scene.at().length; i++) {
                steps.add(Step.waitTicks(scene.at()[i] - waited));
                waited = scene.at()[i];
                steps.add(Step.screenshot("attacks113_" + scene.name() + "_" + i));
            }
            steps.add(Step.waitTicks(20));
            steps.add(Step.server(level -> {
                if (stage[0] != null) {
                    stage[0].cast().forEach(Entity::discard);
                    // was der Angriff hinterließ (Geschosse, Wolken, Wirkungen)
                    level.getEntitiesOfClass(Entity.class, new AABB(BlockPos.containing(stage[0].origin())).inflate(scene.size() + 8, 30, scene.size() + 8),
                            e -> !(e instanceof net.minecraft.world.entity.player.Player)).forEach(Entity::discard);
                }
            }));
        }
    }

    private static final org.slf4j.Logger LOGGER = com.mojang.logging.LogUtils.getLogger();

    /** Für die Fehlersuche: welcher Angriff gerade läuft. */
    private static String attackOf(Mob b) {
        return switch (b) {
            case PhasmorEntity p -> p.currentAttack().name();
            case SordinEntity p -> p.currentAttack().name();
            case StoneKeeperEntity p -> p.currentAttack().name();
            case GlutschmiedEntity p -> p.currentAttack().name();
            case IgnaraEntity p -> p.currentAttack().name();
            case MothQueenEntity p -> p.currentAttack().name();
            case QuetzaraEntity p -> p.currentAttack().name();
            case ShatteredKingEntity p -> p.currentAttack();
            default -> "?";
        };
    }

    private static <T extends Mob> T mob(Stage s, EntityType<T> type, Vec3 at, float yaw) {
        T mob = type.create(s.level(), EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setPersistenceRequired();
        mob.snapTo(at.x, at.y, at.z, yaw, 0.0f);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        s.level().addFreshEntity(mob);
        s.cast().add(mob);
        return mob;
    }
}
