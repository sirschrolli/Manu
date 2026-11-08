package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.CrystalFormationBlock;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.hero.Crystals;
import io.github.sirschrolli.kingdomsatwar.hero.HeroLevel;
import io.github.sirschrolli.kingdomsatwar.magic.Mana;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.SectionPos;
import net.minecraft.world.level.block.Blocks;
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

    /**
     * Das eigene Levelsystem: das Charakterfenster (Heldenstufe, Talente, Werte), der Titel beim Aufstieg, die kurze
     * Anzeige der Heldenerfahrung nach einem Sieg (die Erfahrungskugeln von Minecraft fallen wie immer) und der Tooltip,
     * wofür es Heldenerfahrung gibt.
     */
    static void character(Deque<Step> steps) {
        int oz = -80;
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-12, ground - 1, oz - 16), new BlockPos(12, ground - 1, oz + 8), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-12, ground, oz - 16), new BlockPos(12, ground + 8, oz + 8), Blocks.AIR.defaultBlockState());
            Showcase.fill(level, new BlockPos(-3, ground - 1, oz - 6), new BlockPos(3, ground - 1, oz - 2), Blocks.COARSE_DIRT.defaultBlockState());
            for (int i = 0; i < 18; i++) {
                int x = -10 + (i * 7) % 21;
                int z = oz - 14 + (i * 5) % 20;
                if (Math.abs(x) > 3 || z < oz - 6 || z > oz - 2) {
                    level.setBlock(new BlockPos(x, ground, z), (i % 3 == 0 ? Blocks.FERN : Blocks.SHORT_GRASS).defaultBlockState(), 2);
                }
            }
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            Crystals.set(player, new Crystals.Count(6, 4));
            // Heldenstufe 47 (Ritter), zwei Punkte noch frei
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(47) + (HeroLevel.total(48) - HeroLevel.total(47)) * 3 / 5,
                    java.util.List.of(12, 10, 6, 8, 9, 3), 23));
            player.setHealth(player.getMaxHealth());
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.command("item replace entity @s weapon.mainhand with minecraft:diamond_sword"));
        steps.add(Step.command("item replace entity @s armor.head with minecraft:iron_helmet"));
        steps.add(Step.command("item replace entity @s armor.chest with minecraft:diamond_chestplate"));
        steps.add(Step.command("item replace entity @s armor.legs with minecraft:iron_leggings"));
        steps.add(Step.command("item replace entity @s armor.feet with minecraft:iron_boots"));
        steps.add(Step.command("item replace entity @s hotbar.1 with kingdomsatwar:command_baton"));
        steps.add(Step.command("item replace entity @s hotbar.2 with minecraft:bread 12"));
        steps.add(Showcase.tp(0.5, 0.0, oz + 0.5, 180, 12));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.waitTicks(40));

        // 1. das Charakterfenster (nach dem Öffnen: Leiste gefüllt, Karten hereingeglitten)
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.CharacterScreen()));
        steps.add(Step.waitTicks(6));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("hero_0_screen"));
        // 2. der Weg zur Legende: die Legende mit ihrer Belohnung
        steps.add(Step.once(minecraft -> hoverCharacter(minecraft, 116 + 185, 75)));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("hero_1_track"));
        // ein Talentpunkt geht in die Lebenskraft: das Kästchen blitzt auf
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.server(level -> HeroLevel.spend(level.getServer().getPlayerList().getPlayers().getFirst(), HeroLevel.Talent.VITALITY)));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("hero_2_spend"));
        // der Weg zur Legende: erst im Anflug, dann angekommen, dann das Ende des Weges
        steps.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.HeroPathScreen()));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(17));
        steps.add(Step.screenshot("hero_p0_flight"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("hero_p1_path"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.hero.HeroPathScreen path) {
                path.jumpTo(93.0f);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("hero_p2_legend"));
        steps.add(Step.screen(minecraft -> null));

        // 3. ein Sieg: die Anzeige über der Schnellleiste
        steps.add(Step.server(level -> {
            var zombie = io.github.sirschrolli.kingdomsatwar.registry.ModEntities.ZOMBIE.get().create(level,
                    net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (zombie != null) {
                zombie.snapTo(0.5, Showcase.ground, oz - 2.5, 0.0f, 0.0f);
                zombie.setNoAi(true);
                // mit Helm brennt er in der Sonne nicht
                zombie.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, new net.minecraft.world.item.ItemStack(
                        net.minecraft.world.item.Items.IRON_HELMET));
                level.addFreshEntity(zombie);
            }
        }));
        steps.add(Step.waitTicks(20));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            level.getEntitiesOfClass(net.minecraft.world.entity.monster.Monster.class, new net.minecraft.world.phys.AABB(new BlockPos(0, Showcase.ground,
                    oz - 3)).inflate(4)).forEach(zombie -> zombie.hurtServer(level, level.damageSources().playerAttack(player), 1000.0f));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("hero_3_gained"));
        steps.add(Step.waitTicks(70));

        // 4. Aufstieg mit neuem Rang: von 39 auf 40 – Ritter (die Szene mitten im Lauf)
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(40) - 30, java.util.List.of(), 23));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> HeroLevel.award(level.getServer().getPlayerList().getPlayers().getFirst(), 45)));
        steps.add(Step.waitTicks(9));
        steps.add(Step.screenshot("hero_4_burst"));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("hero_5_rank"));
        steps.add(Step.waitTicks(80));

        // 5. Legende: von 99 auf 100, mit der Belohnung
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(100) - 20, java.util.List.of(), 23));
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.server(level -> HeroLevel.award(level.getServer().getPlayerList().getPlayers().getFirst(), 40)));
        steps.add(Step.waitTicks(44));
        steps.add(Step.screenshot("hero_6_legend"));
        steps.add(Step.waitTicks(70));

        patScene(steps, oz);
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    /** Nur der Weg zur Legende (schnell zu prüfen). */
    static void path(Deque<Step> steps) {
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(47) + (HeroLevel.total(48) - HeroLevel.total(47)) * 3 / 5,
                    java.util.List.of(12, 10, 6, 8, 9, 3), 23));
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.hud(true));
        steps.add(Step.waitForChunks(200));
        steps.add(Step.screen(minecraft -> new io.github.sirschrolli.kingdomsatwar.client.hero.HeroPathScreen()));
        steps.add(Step.once(Showcase::parkMouse));
        steps.add(Step.waitTicks(17));
        steps.add(Step.screenshot("hero_p0_flight"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("hero_p1_path"));
        steps.add(Step.once(minecraft -> {
            if (minecraft.gui.screen() instanceof io.github.sirschrolli.kingdomsatwar.client.hero.HeroPathScreen path) {
                path.jumpTo(93.0f);
            }
        }));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("hero_p2_legend"));
        steps.add(Step.screen(minecraft -> null));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    /** Nur das Streicheln (schnell zu prüfen). */
    static void pat(Deque<Step> steps) {
        int oz = -80;
        steps.add(Step.server(level -> {
            int ground = Showcase.ground;
            Showcase.fill(level, new BlockPos(-12, ground - 1, oz - 16), new BlockPos(12, ground - 1, oz + 8), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(-12, ground, oz - 16), new BlockPos(12, ground + 8, oz + 8), Blocks.AIR.defaultBlockState());
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            HeroLevel.set(player, new HeroLevel.Hero(HeroLevel.total(100), java.util.List.of(), 23));
        }));
        steps.add(Step.command("gamemode survival"));
        steps.add(Step.command("clear @s"));
        steps.add(Step.hud(true));
        steps.add(Step.once(minecraft -> minecraft.options.chatVisibility().set(net.minecraft.world.entity.player.ChatVisiblity.HIDDEN)));
        steps.add(Step.waitForChunks(200));
        patScene(steps, oz);
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.hud(false));
    }

    private static void patScene(Deque<Step> steps, int oz) {
        // 6. Streicheln (wie in PatPat): ein eigener Wolf sitzt vor dem Helden, die Hand tätschelt ihm den Kopf, er duckt sich
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            var wolf = net.minecraft.world.entity.EntityTypes.WOLF.create(level, net.minecraft.world.entity.EntitySpawnReason.COMMAND);
            if (wolf != null) {
                wolf.snapTo(0.5, Showcase.ground, oz - 1.3, 0.0f, 0.0f);
                wolf.tame(player);
                wolf.setOrderedToSit(true);
                wolf.setInSittingPose(true);
                wolf.setNoAi(true);
                wolf.setYRot(0.0f);
                wolf.setYHeadRot(0.0f);
                wolf.setYBodyRot(0.0f);
                level.addFreshEntity(wolf);
            }
        }));
        steps.add(Step.command("item replace entity @s weapon.mainhand with minecraft:air"));
        steps.add(Showcase.tp(0.5, 0.0, oz + 0.5, 180, 34));
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(level -> {
            ServerPlayer player = level.getServer().getPlayerList().getPlayers().getFirst();
            level.getEntitiesOfClass(net.minecraft.world.entity.animal.wolf.Wolf.class, player.getBoundingBox().inflate(4)).forEach(wolf -> {
                io.github.sirschrolli.kingdomsatwar.hero.HeroPat.forget(player);
                io.github.sirschrolli.kingdomsatwar.hero.HeroPat.pat(player, wolf);
            });
        }));
        steps.add(Step.once(minecraft -> io.github.sirschrolli.kingdomsatwar.client.hero.PatClient.devProgress = 0.62f));
        steps.add(Step.once(minecraft -> minecraft.gui.toastManager().clear()));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("hero_7_pat"));
        steps.add(Step.once(minecraft -> {
            minecraft.options.setCameraType(net.minecraft.client.CameraType.FIRST_PERSON);
            io.github.sirschrolli.kingdomsatwar.client.hero.PatClient.devProgress = null;
        }));
        steps.add(Step.waitTicks(10));
        // wie im Spiel: schleichen und mit leerer Hand rechtsklicken (der Weg über die Eingabe, nicht über den Server)
        steps.add(Step.once(minecraft -> minecraft.options.keyShift.setDown(true)));
        steps.add(Step.waitTicks(3));
        steps.add(Step.once(minecraft -> {
            if (minecraft.player == null || minecraft.gameMode == null || minecraft.level == null) {
                return;
            }
            for (net.minecraft.world.entity.Entity entity : minecraft.level.entitiesForRendering()) {
                if (entity instanceof net.minecraft.world.entity.animal.wolf.Wolf wolf) {
                    InteractionResult result = minecraft.gameMode.interact(minecraft.player, wolf,
                            new net.minecraft.world.phys.EntityHitResult(wolf), InteractionHand.MAIN_HAND);
                    KingdomsAtWar.LOGGER.info("[Showcase] Schleichen + Rechtsklick auf den Wolf: {} (schleicht: {})", result,
                            minecraft.player.isShiftKeyDown());
                }
            }
        }));
        steps.add(Step.waitTicks(2));
        steps.add(Step.screenshot("hero_7b_gesture"));
        steps.add(Step.once(minecraft -> minecraft.options.keyShift.setDown(false)));
        steps.add(Step.waitTicks(10));
    }

    /** Mauszeiger über eine Stelle des Charakterfensters (Koordinaten im Fenster). */
    private static void hoverCharacter(net.minecraft.client.Minecraft minecraft, int guiX, int guiY) {
        int left = (minecraft.getWindow().getGuiScaledWidth() - 324) / 2;
        int top = (minecraft.getWindow().getGuiScaledHeight() - 246) / 2;
        double scale = minecraft.getWindow().getGuiScale();
        try {
            for (String name : new String[] {"xpos", "ypos"}) {
                java.lang.reflect.Field field = minecraft.mouseHandler.getClass().getDeclaredField(name);
                field.setAccessible(true);
                field.setDouble(minecraft.mouseHandler, name.equals("xpos") ? (left + guiX) * scale : (top + guiY) * scale);
            }
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
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
