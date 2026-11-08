package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.CrystalFormationBlock;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.LevelChunkSection;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;

/**
 * Vorführung des eigenen Helden: Herz- und Manakristalle, wie sie die Welt in ihren Höhlen wachsen lässt (die Szene
 * sucht sie in den Chunks um den Startpunkt und zählt, wie häufig sie sind), und wie man sie aufnimmt – zwanzig Herzen,
 * 300 Mana.
 */
final class HeroShowcase {
    /** So weit (in Chunks) um den Startpunkt wird gesucht. */
    private static final int RADIUS = 7;

    private HeroShowcase() {}

    /** Ein Blick auf einen Kristall: wo man steht (Füße), von wo die Kamera schaut (Augen) und wohin. */
    private static final class View {
        volatile boolean found;
        double feetX, feetY, feetZ;
        double camX, camY, camZ;
        double targetX, targetY, targetZ;

        /** Kamera frei vor dem Kristall (Zuschauer) bzw. stehend auf dem Höhlenboden. */
        Step go(boolean camera) {
            return Step.once(minecraft -> {
                if (!this.found || minecraft.player == null) {
                    return;
                }
                double x = camera ? this.camX : this.feetX;
                double eyeY = camera ? this.camY : this.feetY + 1.62;
                double z = camera ? this.camZ : this.feetZ;
                double lx = this.targetX - x;
                double ly = this.targetY - eyeY;
                double lz = this.targetZ - z;
                float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.3f %.3f %.3f %.1f %.1f",
                        x, eyeY - 1.62, z, yaw, pitch));
            });
        }
    }

    static void crystals(Deque<Step> steps) {
        View heart = new View();
        View mana = new View();
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.once(minecraft -> minecraft.options.renderDistance().set(6)));
        boolean[] surveyed = new boolean[1];
        steps.add(Step.server(level -> {
            survey(level, heart, mana);
            surveyed[0] = true;
        }));
        steps.add(Step.until(() -> surveyed[0], 6000));
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> minecraft.options.gamma().set(1.0)));

        // 1./2. die Kristalle in ihren Höhlen
        steps.add(heart.go(true));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("crystals_0_heart_cave"));
        steps.add(mana.go(true));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(80));
        steps.add(Step.screenshot("crystals_1_mana_cave"));

        // 3. vor dem Herzkristall: den zehnten aufnehmen – zwanzig Herzen
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            Crystals.set(player, new Crystals.Count(Crystals.MAX - 1, Crystals.MAX - 1));
            player.setHealth(player.getMaxHealth());
            player.getFoodData().setFoodLevel(20);
        }));
        steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:heart_crystal 3"));
        steps.add(Step.command("item replace entity @s hotbar.1 with minecraft:stone_pickaxe"));
        steps.add(Step.command("item replace entity @s hotbar.2 with minecraft:torch 24"));
        steps.add(Step.command("item replace entity @s hotbar.3 with minecraft:bread 9"));
        steps.add(heart.go(false));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(40));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(use());
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("crystals_2_heart_absorb"));

        // 4. vor dem Manakristall, den Stab in der anderen Hand: den zehnten aufnehmen – 300 Mana
        steps.add(Step.command("item replace entity @s weapon.mainhand with kingdomsatwar:mana_crystal 2"));
        steps.add(Step.command("item replace entity @s weapon.offhand with kingdomsatwar:aetherit_staff"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            Mana.set(player, Mana.maxFor(player));
        }));
        steps.add(mana.go(false));
        steps.add(Step.waitTicks(20));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.waitTicks(40));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(use());
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("crystals_3_mana_absorb"));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            KingdomsAtWar.LOGGER.info("[Showcase] Kristalle aufgenommen: {}, Lebenspunkte {}/{}, Mana {}/{}", Crystals.of(player),
                    player.getHealth(), player.getMaxHealth(), Mana.get(player), Mana.maxFor(player));
        }));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    /** Rechtsklick mit dem Gegenstand in der Haupthand – wie im Spiel. */
    private static Step use() {
        return Step.once(minecraft -> {
            if (minecraft.gameMode != null && minecraft.player != null) {
                InteractionResult result = minecraft.gameMode.useItem(minecraft.player, InteractionHand.MAIN_HAND);
                KingdomsAtWar.LOGGER.info("[Showcase] {} benutzt: {}", minecraft.player.getMainHandItem(), result);
            }
        });
    }

    /**
     * Erzeugt die Chunks um den Startpunkt, zählt die Kristalle darin (wie häufig sie sind) und sucht für jede Sorte den
     * schönsten Blick: in einer möglichst weiten Höhle, freie Sicht, ein Stück Boden zum Stehen (beim Herz von vorn).
     */
    private static void survey(ServerLevel level, View heart, View mana) {
        long started = System.currentTimeMillis();
        ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
        ChunkPos center = player.chunkPosition();
        List<BlockPos> hearts = new ArrayList<>();
        List<BlockPos> manas = new ArrayList<>();
        int[] belowZero = new int[2];
        for (int cx = -RADIUS; cx <= RADIUS; cx++) {
            for (int cz = -RADIUS; cz <= RADIUS; cz++) {
                LevelChunk chunk = (LevelChunk) level.getChunk(center.x() + cx, center.z() + cz, ChunkStatus.FULL, true);
                LevelChunkSection[] sections = chunk.getSections();
                for (int i = 0; i < sections.length; i++) {
                    LevelChunkSection section = sections[i];
                    if (section.hasOnlyAir() || !section.maybeHas(state -> state.getBlock() instanceof CrystalFormationBlock)) {
                        continue;
                    }
                    int baseY = SectionPos.sectionToBlockCoord(chunk.getSectionYFromSectionIndex(i));
                    for (int y = 0; y < 16; y++) {
                        for (int z = 0; z < 16; z++) {
                            for (int x = 0; x < 16; x++) {
                                if (section.getBlockState(x, y, z).getBlock() instanceof CrystalFormationBlock crystal) {
                                    BlockPos pos = new BlockPos(chunk.getPos().getMinBlockX() + x, baseY + y, chunk.getPos().getMinBlockZ() + z);
                                    boolean isHeart = crystal.kind() == Crystals.Kind.HEART;
                                    (isHeart ? hearts : manas).add(pos);
                                    if (pos.getY() < 0) {
                                        belowZero[isHeart ? 0 : 1]++;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        int chunks = (2 * RADIUS + 1) * (2 * RADIUS + 1);
        KingdomsAtWar.LOGGER.info("[Showcase] Kristalle in {} Chunks: {} Herzkristalle (1 je {} Chunks, {} unter Y 0), {} Manakristalle "
                        + "(1 je {} Chunks, {} unter Y 0), {} ms", chunks, hearts.size(),
                String.format(Locale.ROOT, "%.1f", chunks / (double) Math.max(1, hearts.size())), belowZero[0], manas.size(),
                String.format(Locale.ROOT, "%.1f", chunks / (double) Math.max(1, manas.size())), belowZero[1],
                System.currentTimeMillis() - started);
        pick(level, hearts, heart, true);
        pick(level, manas, mana, false);
    }

    private static void pick(ServerLevel level, List<BlockPos> crystals, View view, boolean heart) {
        double best = -1.0;
        for (BlockPos pos : crystals) {
            BlockState state = level.getBlockState(pos);
            if (!(state.getBlock() instanceof CrystalFormationBlock)) {
                continue;
            }
            Direction facing = state.getValue(HorizontalDirectionalBlock.FACING);
            Vec3 target = new Vec3(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            int open = openness(level, pos);
            for (int a = 0; a < 16; a++) {
                double angle = a * Math.PI / 8.0;
                double dx = Math.sin(angle);
                double dz = Math.cos(angle);
                // das Herz ist flach: von vorn (oder hinten) sieht man es, von der Seite kaum
                double align = Math.abs(dx * facing.getStepX() + dz * facing.getStepZ());
                if (heart && align < 0.75) {
                    continue;
                }
                for (double distance : new double[] {2.0, 2.5, 3.0}) {
                    double fx = target.x + dx * distance;
                    double fz = target.z + dz * distance;
                    Double floor = floor(level, fx, pos.getY(), fz);
                    if (floor == null) {
                        continue;
                    }
                    Vec3 standing = new Vec3(fx, floor + 1.62, fz);
                    if (!clearAround(level, standing, 0.3) || !sees(level, standing, target, pos)) {
                        continue;
                    }
                    // die Kamera: etwas näher und tiefer, wenn dort Platz und freie Sicht ist
                    Vec3 camera = new Vec3(target.x + dx * distance * 0.62, pos.getY() + 0.8, target.z + dz * distance * 0.62);
                    if (!clearAround(level, camera, 0.3) || !sees(level, camera, target, pos)) {
                        camera = standing;
                    }
                    double score = open + (heart ? align * 20.0 : 0.0) - Math.abs(distance - 2.5) * 4.0 - Math.abs(floor - pos.getY()) * 6.0;
                    if (score > best) {
                        best = score;
                        view.feetX = fx;
                        view.feetY = floor;
                        view.feetZ = fz;
                        view.camX = camera.x;
                        view.camY = camera.y;
                        view.camZ = camera.z;
                        view.targetX = target.x;
                        view.targetY = pos.getY() + 0.45;
                        view.targetZ = target.z;
                        view.found = true;
                    }
                }
            }
        }
        KingdomsAtWar.LOGGER.info("[Showcase] Blick auf {}: {} (Wertung {}), stehend bei {} {} {}, Kamera {} {} {}", heart ? "Herzkristall" : "Manakristall",
                view.found ? "gefunden" : "keiner", Math.round(best), Math.round(view.feetX), Math.round(view.feetY), Math.round(view.feetZ),
                Math.round(view.camX), Math.round(view.camY), Math.round(view.camZ));
    }

    /** Der Boden (Höhe der Füße) nahe dieser Höhe: fester Block, darüber zwei Blöcke Luft ohne Wasser. */
    private static Double floor(ServerLevel level, double x, int y, double z) {
        for (int dy : new int[] {0, 1, -1, 2, -2}) {
            BlockPos feet = BlockPos.containing(x, y + dy, z);
            BlockPos below = feet.below();
            if (level.getBlockState(below).isFaceSturdy(level, below, Direction.UP) && level.getBlockState(feet).isAir()
                    && level.getBlockState(feet.above()).isAir() && level.getFluidState(feet).isEmpty()) {
                return (double) feet.getY();
            }
        }
        return null;
    }

    /** Um diesen Punkt herum (in jeder Richtung so weit) nur Luft. */
    private static boolean clearAround(ServerLevel level, Vec3 point, double margin) {
        for (int i = 0; i < 8; i++) {
            BlockPos pos = BlockPos.containing(point.x + ((i & 1) == 0 ? -margin : margin), point.y + ((i & 2) == 0 ? -margin : margin),
                    point.z + ((i & 4) == 0 ? -margin : margin));
            if (!level.getBlockState(pos).isAir() || !level.getFluidState(pos).isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /** Freie Sicht auf den Kristall (kein Block, kein Wasser dazwischen). */
    private static boolean sees(ServerLevel level, Vec3 from, Vec3 target, BlockPos crystal) {
        BlockHitResult hit = level.clip(new ClipContext(from, target, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, CollisionContext.empty()));
        return hit.getType() == HitResult.Type.MISS || hit.getBlockPos().equals(crystal);
    }

    /** Wie weit die Höhle um den Kristall ist: Luft in einem Kasten von 11 × 6 × 11 Blöcken. */
    private static int openness(ServerLevel level, BlockPos crystal) {
        int air = 0;
        for (BlockPos pos : BlockPos.betweenClosed(crystal.offset(-5, -1, -5), crystal.offset(5, 4, 5))) {
            if (level.getBlockState(pos).isAir()) {
                air++;
            }
        }
        return air;
    }
}
