package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardLocator;
import io.github.sirschrolli.kingdomsatwar.abyss.hoard.HoardPiece;
import io.github.sirschrolli.kingdomsatwar.arena.BossArenaKind;
import io.github.sirschrolli.kingdomsatwar.arena.BossFights;
import io.github.sirschrolli.kingdomsatwar.arena.BossLair;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.fae.MystikStage;
import io.github.sirschrolli.kingdomsatwar.fae.feature.MystikStageFeature;
import io.github.sirschrolli.kingdomsatwar.registry.AnniBlocks;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import io.github.sirschrolli.kingdomsatwar.worldgen.lair.ModStructures;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteKind;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SitePiece;
import io.github.sirschrolli.kingdomsatwar.worldgen.site.SiteSamples;
import io.github.sirschrolli.kingdomsatwar.worldgen.throne.ThroneLocator;
import java.util.Deque;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderSet;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.structure.StructureStart;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Vorführung ({@code boss_arenas} oder {@code boss_arena_<id>}): Für jeden der sechs großen Bosse tritt ein Spieler an den
 * echten Hort, wird in die Kampfdimension gezogen (Bilder: weit über der Insel, der Blick vom Ankunftsplatz, der Boss
 * vor dem fremden Himmel), dann fällt der Boss, und alle kehren an den Hort zurück.
 */
final class BossArenaShowcase {
    private BossArenaShowcase() {}

    /** Der gefundene Hort. */
    private static final class Spot {
        @Nullable BlockPos lair;
        @Nullable Vec3 approach;
        int state;
    }

    static void arenas(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamerule spawn_monsters false"));
        for (BossArenaKind kind : BossArenaKind.values()) {
            if (!only.isEmpty() && !only.equals(kind.id())) {
                continue;
            }
            arena(steps, kind);
        }
    }

    private static ResourceKey<Level> home(BossArenaKind kind) {
        return kind.home();
    }

    private static void arena(Deque<Step> steps, BossArenaKind kind) {
        Spot spot = new Spot();
        String name = "arena_" + kind.id();
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(any.getServer().getLevel(home(kind)), kind, spot)));
        steps.add(Step.until(() -> spot.state != 0, 12000));
        // erst die Chunks um den Hort fertig erzeugen lassen (dann steht der Hort samt Blockentität)
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().getLevel(home(kind));
            if (level != null && spot.lair != null) {
                ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
                Vec3 view = Vec3.atCenterOf(spot.lair).add(0.0, 30.0, 30.0);
                player.teleportTo(level, view.x, view.y, view.z, java.util.Set.of(), 180.0f, 40.0f, true);
            }
        }));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1200));
        // ein Spieler (unverwundbar, er darf fliegen) tritt an den Hort – und wird hineingezogen
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.server(any -> {
            ServerLevel level = any.getServer().getLevel(home(kind));
            if (level == null || spot.lair == null || !(level.getBlockEntity(spot.lair) instanceof BossLair lair)) {
                KingdomsAtWar.LOGGER.info("[Showcase] {}: kein Hort bei {}", kind.id(), spot.lair);
                spot.state = -1;
                return;
            }
            lair.arenaReset(level);
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            hover(player);
            Vec3 a = spot.approach;
            player.teleportTo(level, a.x, a.y, a.z, java.util.Set.of(), 0.0f, 10.0f, true);
            KingdomsAtWar.LOGGER.info("[Showcase] {}: Spieler am Hort bei {}", kind.id(), a);
        }));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return spot.state < 0 || minecraft.level != null && minecraft.level.dimension() == kind.level();
        }, 600));
        steps.add(Step.waitTicks(30));
        steps.add(Step.waitForChunks(800));
        // der Blick vom Ankunftsplatz zur Mitte des Horts, weit über der Insel (der fremde Himmel), der Boss vor dem Himmel
        // und von unten: jedes Mal erst die Chunks um die Kamera laden lassen (Himmel und Dunst kommen vom Biom dort)
        for (String mode : new String[] {"view", "wide", "boss", "low"}) {
            steps.add(camera(kind, mode));
            steps.add(Step.waitTicks(mode.equals("view") ? 50 : 16));
            steps.add(Step.waitForChunks(300));
            steps.add(camera(kind, mode));
            steps.add(Step.waitTicks(3));
            steps.add(Step.screenshot(name + "_" + mode));
        }
        // er fällt: alle kehren zurück
        steps.add(Step.server(any -> {
            ServerLevel arena = any.getServer().getLevel(kind.level());
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            BossFights.Fight fight = BossFights.of(any.getServer()).byFighter(player.getUUID());
            if (arena != null && fight != null && arena.getBlockEntity(fight.trigger()) instanceof BossLair replica
                    && replica.arenaBoss(arena) instanceof LivingEntity boss) {
                boss.setLastHurtByPlayer(player, 100);
                boss.kill(arena);
            }
        }));
        steps.add(Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            return minecraft.level != null && minecraft.level.dimension() == home(kind);
        }, 900));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(any -> {
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.getAbilities().invulnerable = false;
            player.getAbilities().flying = false;
            player.getAbilities().mayfly = false;
            player.onUpdateAbilities();
            ServerLevel level = any.getServer().getLevel(home(kind));
            if (level != null && spot.lair != null && level.getBlockEntity(spot.lair) instanceof BossLair lair) {
                KingdomsAtWar.LOGGER.info("[Showcase] {}: zurück am Hort, besiegt: {}", kind.id(), lair.arenaDefeated());
            }
        }));
    }

    /**
     * Kamera in der Arena: {@code view} vom Ankunftsplatz zur Mitte, {@code wide} weit draußen und hoch über der Insel,
     * {@code boss} schräg vor dem Boss, {@code low} tief unter dem Rand der Insel hinauf.
     */
    private static Step camera(BossArenaKind kind, String mode) {
        return Step.server(any -> {
            ServerLevel arena = any.getServer().getLevel(kind.level());
            ServerPlayer player = any.getServer().getPlayerList().getPlayers().getFirst();
            BossFights.Fight fight = BossFights.of(any.getServer()).byFighter(player.getUUID());
            if (arena == null || fight == null || player.level() != arena) {
                return;
            }
            Vec3 c = Vec3.atBottomCenterOf(fight.center());
            LivingEntity boss = arena.getBlockEntity(fight.trigger()) instanceof BossLair replica ? replica.arenaBoss(arena) : null;
            Vec3 eye;
            Vec3 target;
            double r = kind.reach();
            // die Kämpfer erscheinen zuerst im Süden der Mitte (+z): von dort aus blickt man auf den Hort
            switch (mode) {
                case "wide" -> {
                    // weit draußen, aber noch in der Arena (sonst gälte der Kampf als aufgegeben)
                    double far = Math.min(r * 1.42, r + 18.0);
                    eye = c.add(far * 0.67, Math.min(r * 0.55, 46.0), far * 0.74);
                    target = c.add(0.0, -4.0, 0.0);
                }
                case "boss" -> {
                    // vor dem Boss, etwas tiefer als er, der Blick hinauf (der fremde Himmel hinter ihm)
                    Vec3 b = boss != null ? boss.position().add(0.0, boss.getBbHeight() * 0.55, 0.0) : c.add(0.0, 6.0, 0.0);
                    Vec3 away = b.subtract(c).multiply(1.0, 0.0, 1.0);
                    if (away.lengthSqr() < 4.0) {
                        away = new Vec3(0.0, 0.0, 1.0);
                    }
                    double d = boss != null ? Math.max(8.0, boss.getBbWidth() * 2.6 + 6.0) : 12.0;
                    eye = b.add(away.normalize().yRot(0.6f).scale(d)).add(0.0, -1.2, 0.0);
                    target = b.add(0.0, 0.6, 0.0);
                }
                case "low" -> {
                    double far = Math.min(r * 1.63, r + 18.0);
                    eye = c.add(-far * 0.77, -Math.min(r * 0.3, 26.0), far * 0.64);
                    target = c.add(0.0, 4.0, 0.0);
                }
                default -> {
                    // auf dem Dach der Esse ist wenig Platz: dort dicht über dem Amboss
                    double back = kind == BossArenaKind.MALLEUS ? 5.0 : kind.spawnMax() + 7.0;
                    eye = c.add(0.0, kind == BossArenaKind.MALLEUS ? 4.0 : 5.5, back);
                    target = c.add(0.0, 2.5, 0.0);
                }
            }
            // nicht in einer Wand, nicht im Laub: so weit zum Ziel hin, bis die Kamera frei steht
            for (int i = 0; i < 40 && !free(arena, eye); i++) {
                eye = eye.add(target.subtract(eye).normalize().scale(0.75));
            }
            Vec3 look = target.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            // der Wechsel der Welt setzt die Fähigkeiten des Überlebensmodus zurück: wieder unverwundbar und schwebend
            hover(player);
            player.setDeltaMovement(Vec3.ZERO);
            player.teleportTo(arena, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Unverwundbar und schwebend (die Kamera), dazu Widerstand, falls die Welt die Fähigkeiten zurücksetzt. */
    private static void hover(ServerPlayer player) {
        player.getAbilities().invulnerable = true;
        player.getAbilities().mayfly = true;
        player.getAbilities().flying = true;
        player.onUpdateAbilities();
        player.resetFallDistance();
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.RESISTANCE, 1200, 4, false, false));
    }

    /** Steht die Kamera (Augenhöhe) frei – kein fester Block, kein Laub um sie herum? */
    private static boolean free(ServerLevel level, Vec3 eye) {
        BlockPos p = BlockPos.containing(eye);
        for (BlockPos q : new BlockPos[] {p, p.above(), p.below(), p.north(), p.south(), p.east(), p.west()}) {
            var state = level.getBlockState(q);
            if (!state.isAir() && !state.getCollisionShape(level, q).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    // =====================================================================================
    // Die Horte finden
    // =====================================================================================

    private static void locate(@Nullable ServerLevel level, BossArenaKind kind, Spot spot) {
        if (level == null) {
            spot.state = -1;
            return;
        }
        try {
            switch (kind) {
                case MYSTIK -> {
                    BlockPos center = MystikStage.nearest(level.getSeed(), 600, 600);
                    if (center != null) {
                        hold(level, center);
                        int x = center.getX();
                        int z = center.getZ() + MystikStageFeature.HAT_DZ;
                        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                        for (int y = top + 2; y > top - 16 && spot.lair == null; y--) {
                            if (level.getBlockState(new BlockPos(x, y, z)).is(MystikBlocks.STAGE_HAT.get())) {
                                spot.lair = new BlockPos(x, y, z);
                            }
                        }
                    }
                    if (spot.lair != null) {
                        spot.approach = Vec3.atBottomCenterOf(spot.lair).add(0.0, 0.5, 5.0);
                    }
                }
                case MALLEUS -> {
                    SitePiece tower = site(level, SiteKind.ASH_FORGE, new BlockPos(0, 64, 0), true);
                    if (tower != null) {
                        spot.lair = SiteSamples.ashForgeAnvil(tower);
                        hold(level, spot.lair);
                        spot.approach = Vec3.atBottomCenterOf(spot.lair).add(2.5, 0.5, 2.5);
                    }
                }
                case ASTERIA -> {
                    SitePiece watch = site(level, SiteKind.STARWATCH, new BlockPos(1200, 64, 0), false);
                    if (watch != null) {
                        spot.lair = SiteSamples.starwatchStone(watch);
                        hold(level, spot.lair);
                        spot.approach = Vec3.atBottomCenterOf(spot.lair).add(4.0, 0.0, 0.0);
                    }
                }
                case ANNI -> {
                    var structure = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).get(ModStructures.site(SiteKind.ERROR_ZONE));
                    var found = structure.isEmpty() ? null : level.getChunkSource().getGenerator().findNearestMapStructure(level,
                            HolderSet.direct(structure.get()), BlockPos.ZERO, 160, false);
                    if (found != null) {
                        int x = (found.getFirst().getX() & ~15) + 8;
                        int z = (found.getFirst().getZ() & ~15) + 8;
                        hold(level, new BlockPos(x, 64, z));
                        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
                        for (int y = top + 4; y > top - 24 && spot.lair == null; y--) {
                            if (level.getBlockState(new BlockPos(x, y, z)).is(AnniBlocks.ERROR_CORE.get())) {
                                spot.lair = new BlockPos(x, y, z);
                            }
                        }
                    }
                    if (spot.lair != null) {
                        spot.approach = Vec3.atBottomCenterOf(spot.lair).add(5.0, 0.0, 0.0);
                    }
                }
                case KING -> {
                    BlockPos center = ThroneLocator.center(level);
                    if (center != null) {
                        hold(level, center);
                        spot.lair = ThroneLocator.seal(center);
                        spot.approach = Vec3.atBottomCenterOf(center).add(0.0, 0.5, 10.0);
                    }
                }
                case WORLD_EATER -> {
                    BlockPos center = HoardLocator.center(level);
                    if (center != null) {
                        hold(level, center);
                        spot.lair = HoardPiece.sealPosition(center);
                        spot.approach = Vec3.atBottomCenterOf(center).add(0.0, 0.5, 20.0);
                    }
                }
            }
        } catch (RuntimeException e) {
            KingdomsAtWar.LOGGER.error("[Showcase] {}: Hort nicht gefunden", kind.id(), e);
        }
        KingdomsAtWar.LOGGER.info("[Showcase] {}: Hort bei {} ({})", kind.id(), spot.lair, spot.lair == null ? "-" : level.getBlockState(spot.lair));
        spot.state = spot.lair != null ? 1 : -1;
    }

    private static @Nullable SitePiece site(ServerLevel level, SiteKind kind, BlockPos from, boolean tower) {
        var holder = level.registryAccess().lookupOrThrow(Registries.STRUCTURE).getOrThrow(ModStructures.site(kind));
        var found = level.getChunkSource().getGenerator().findNearestMapStructure(level, HolderSet.direct(holder), from, 300, false);
        if (found == null) {
            return null;
        }
        ChunkPos chunk = ChunkPos.containing(found.getFirst());
        StructureStart start = level.getChunk(chunk.x(), chunk.z(), ChunkStatus.STRUCTURE_STARTS, true).getStartForStructure(holder.value());
        if (start == null || !start.isValid()) {
            return null;
        }
        for (var piece : start.getPieces()) {
            if (piece instanceof SitePiece site && site.kind() == kind && (!tower || SiteSamples.isAshForgeTower(site))) {
                return site;
            }
        }
        return null;
    }

    /** Hält die Chunks um den Hort geladen (dann entsteht er samt Blockentität). */
    private static void hold(ServerLevel level, BlockPos at) {
        int cx = at.getX() >> 4;
        int cz = at.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
    }

}
