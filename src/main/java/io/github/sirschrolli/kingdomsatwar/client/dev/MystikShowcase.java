package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.fae.StageHatBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.entity.fae.LightDoveEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicCardEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MagicSwordEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MissMystikEntity;
import io.github.sirschrolli.kingdomsatwar.entity.fae.MystikImageEntity;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.fae.FaeGate;
import io.github.sirschrolli.kingdomsatwar.fae.MystikStage;
import io.github.sirschrolli.kingdomsatwar.fae.feature.MystikStageFeature;
import io.github.sirschrolli.kingdomsatwar.magic.fx.SpellFxEntity;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import io.github.sirschrolli.kingdomsatwar.registry.MystikBlocks;
import java.util.Deque;
import java.util.Locale;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Vorführung ({@code mystik_boss}): MissMystik auf einer echten Zauberbühne im Feenreich – die Bühne mit Vorhängen,
 * Rampenlichtern und Bänken, der große Zylinder, ihr Auftritt (Konfetti, Verbeugung), Bildnisse, die Bossleiste, alle
 * Tricks gegen einen (zähen) Ritter, die Zugabe, das Finale, ihre letzte Verbeugung, der leere Zylinder und Kartendeck und
 * Zylinder am Rüstungsständer.
 */
final class MystikShowcase {
    private static final String STAGE_TAG = "kaw_mystik_stage";

    private MystikShowcase() {}

    /** Mitte der Bühne (Boden der Lichtung + 1), der Zylinder und die Ids von MissMystik und Ritter. */
    private static final class Stage {
        final double[] c = new double[3];
        final double[] cam = new double[3];
        BlockPos hat;
        int mystik = -1;
        int knight = -1;
        boolean ready;
    }

    private static ServerLevel fae(ServerLevel any) {
        return any.getServer().getLevel(FaeGate.FAE_REALM);
    }

    static void boss(Deque<Step> steps) {
        Stage s = new Stage();
        steps.add(Step.hud(false));
        steps.add(Step.once(minecraft -> {
            minecraft.options.renderDistance().set(12);
            minecraft.options.gamma().set(1.0);
        }));
        steps.add(Step.command("gamerule spawn_monsters false"));
        steps.add(Step.command("gamerule spawn_mobs false"));
        steps.add(Step.command("difficulty normal"));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.server(any -> locate(fae(any), s)));
        steps.add(Step.until(() -> s.ready, 9000));
        // Übersicht vom Rand des Zuschauerrunds
        steps.add(at(s, 0.0, 17.0, 32.0, 0.0, 4.0, -4.0));
        steps.add(FaeShowcase.arrived(s.cam));
        steps.add(Step.waitTicks(60));
        steps.add(Step.waitForChunks(1500));
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            for (Entity e : level.getEntitiesOfClass(Mob.class, new AABB(s.hat).inflate(40, 20, 40))) {
                e.discard();
            }
        }));
        steps.add(Step.waitTicks(40));
        steps.add(Step.screenshot("mystik_stage_overview"));
        steps.add(at(s, 7.0, 7.5, 10.5, 0.0, 4.5, -5.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("mystik_stage"));
        steps.add(at(s, 2.4, 2.6, -2.2, 0.0, 2.9, -6.0));
        steps.add(Step.waitTicks(20));
        steps.add(Step.screenshot("mystik_hat"));
        // ein Ritter tritt an die Bühne: die Vorstellung beginnt
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            Mob knight = creature(level, ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), local(s, 0, 2, 1), 180.0f);
            knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
            knight.setSilent(true);
            s.knight = knight.getId();
            if (level.getBlockEntity(s.hat) instanceof StageHatBlockEntity hat) {
                MissMystikEntity mystik = hat.summon(level);
                if (mystik != null) {
                    s.mystik = mystik.getId();
                    mystik.setTarget(knight);
                }
            }
        }));
        steps.add(at(s, 1.0, 3.6, 4.5, 0.0, 3.8, -6.0));
        steps.add(Step.waitTicks(MissMystikEntity.APPEAR_BURST + 3));
        steps.add(Step.screenshot("mystik_appear"));
        // die Verbeugung (zum Publikum hin)
        steps.add(Step.waitTicks(8));
        steps.add(at(s, 2.2, 5.6, 0.2, 0.0, 6.6, -6.0));
        steps.add(faceCamera(s));
        steps.add(Step.waitTicks(10));
        steps.add(faceCamera(s));
        steps.add(watch(s, 2.2, 5.6, 0.2));
        steps.add(Step.waitTicks(3));
        steps.add(Step.screenshot("mystik_bow"));
        steps.add(Step.waitTicks(20));
        // Bildnisse: sie hält still über der Bühne, der Kamera zugewandt
        steps.add(pose(s, 0.0, 3.0, -3.0, 0.0, 3.0, 6.0));
        steps.add(at(s, 1.6, 4.6, 1.2, 0.0, 4.9, -3.0));
        steps.add(Step.waitTicks(12));
        steps.add(Step.screenshot("mystik_portrait"));
        steps.add(at(s, -3.4, 3.4, 0.6, 0.0, 4.4, -3.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("mystik_portrait_side"));
        steps.add(at(s, 0.6, 5.6, -0.2, 0.0, 5.7, -3.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("mystik_portrait_face"));
        steps.add(at(s, 1.8, 4.6, -7.4, 0.0, 4.6, -3.0));
        steps.add(Step.waitTicks(8));
        steps.add(Step.screenshot("mystik_portrait_back"));
        steps.add(Step.server(any -> {
            if (fae(any).getEntity(s.mystik) instanceof MissMystikEntity mystik) {
                mystik.setNoAi(false);
            }
        }));
        // mit Bossleiste
        steps.add(Step.hud(true));
        steps.add(follow(s, -35.0, 6.0, 1.0, 0.25));
        steps.add(Step.waitTicks(4));
        steps.add(Step.screenshot("mystik_bossbar"));
        steps.add(Step.hud(false));
        java.util.Set<String> started = new java.util.HashSet<>();
        // die Tricks: {Trick, Tick des Bildes, Name, Blickwinkel zur Linie MissMystik → Ritter (Grad), Abstand, Höhe, Blick zum Ritter 0..1}
        for (String[] shot : new String[][] {{"CARDS", "17", "mystik_cards", "-25", "4.6", "2.2", "0.15"},
                {"DOVES", "20", "mystik_doves", "-40", "6.5", "1.0", "0.0"},
                {"VANISH", "13", "mystik_vanish", "-60", "5.5", "0.8", "0.2"},
                {"VANISH", "31", "mystik_reappear", "-90", "4.6", "0.8", "0.5"},
                {"MIRRORS", "26", "mystik_mirrors", "", "", "", ""},
                {"HAT", "19", "mystik_hat_throw", "-70", "5.0", "0.8", "0.4"},
                {"HAT", "50", "mystik_vortex", "", "", "", ""},
                {"HAT", "77", "mystik_hat_burst", "", "", "", ""},
                {"SWORDS", "27", "mystik_swords", "20", "5.2", "1.4", "1.0"},
                {"SWORDS", "34", "mystik_stab", "20", "5.2", "1.4", "1.0"},
                {"CARD_TRICK", "40", "mystik_card_trick", "-95", "6.5", "1.2", "0.55"},
                {"SPOTLIGHT", "20", "mystik_spotlight", "", "", "", ""},
                {"FINALE", "64", "mystik_finale", "", "", "", ""},
                {"FINALE", "113", "mystik_finale_blast", "", "", "", ""}}) {
            String attack = shot[0];
            MissMystikEntity.Attack trick = MissMystikEntity.Attack.valueOf(attack);
            int tick = Integer.parseInt(shot[1]);
            if (!started.contains(attack)) {
                started.add(attack);
                steps.add(Step.server(any -> {
                    ServerLevel level = fae(any);
                    clearTricks(level, s);
                    if (level.getEntity(s.mystik) instanceof MissMystikEntity mystik && level.getEntity(s.knight) instanceof LivingEntity knight) {
                        Vec3 k = local(s, 0, 2, 1);
                        knight.teleportTo(k.x, k.y, k.z);
                        knight.setDeltaMovement(Vec3.ZERO);
                        knight.removeAllEffects();
                        knight.addEffect(new MobEffectInstance(MobEffects.RESISTANCE, 1_000_000, 4, false, false));
                        knight.setHealth(knight.getMaxHealth());
                        Vec3 a = local(s, -1.5, 4.2, -3.5);
                        mystik.teleportTo(a.x, a.y, a.z);
                        mystik.setDeltaMovement(Vec3.ZERO);
                        mystik.setTarget(knight);
                        mystik.lookAt(knight, 360.0f, 360.0f);
                        mystik.setYBodyRot(mystik.getYRot());
                        mystik.setYHeadRot(mystik.getYRot());
                        mystik.forceAttack(MissMystikEntity.Attack.NONE);
                    }
                }));
                steps.add(Step.waitTicks(2));
                steps.add(Step.server(any -> {
                    if (fae(any).getEntity(s.mystik) instanceof MissMystikEntity mystik) {
                        if (trick == MissMystikEntity.Attack.SPOTLIGHT || trick == MissMystikEntity.Attack.FINALE) {
                            mystik.forceEncore();
                        }
                        if (trick == MissMystikEntity.Attack.CARD_TRICK) {
                            mystik.forceSuit(MissMystikEntity.Suit.HEART);
                        }
                        mystik.forceAttack(trick);
                    }
                }));
            }
            steps.add(attackAt(s, trick, tick - 5));
            if (shot[2].equals("mystik_vortex")) {
                // der Ritter steht ein Stück neben dem Hut (sonst stünde er mitten darin)
                steps.add(Step.server(any -> {
                    if (fae(any).getEntity(s.knight) instanceof Entity knight) {
                        Vec3 k = local(s, 3.2, 2, 2.4);
                        knight.teleportTo(k.x, k.y, k.z);
                    }
                }));
            }
            switch (shot[2]) {
                case "mystik_mirrors" -> steps.add(at(s, 0.0, 7.5, 10.5, 0.0, 3.0, 0.5));
                case "mystik_spotlight" -> steps.add(at(s, 0.0, 8.0, 13.0, 0.0, 4.5, -1.0));
                case "mystik_vortex" -> steps.add(above(s, 10.0, 7.5, 0.25));
                case "mystik_hat_burst" -> steps.add(above(s, 7.0, 3.0, 0.4));
                case "mystik_finale" -> steps.add(at(s, 3.5, 5.5, 6.5, 0.0, 8.6, -1.5));
                case "mystik_finale_blast" -> steps.add(at(s, -3.0, 6.0, 6.0, 0.0, 9.2, -1.5));
                default -> steps.add(follow(s, Double.parseDouble(shot[3]), Double.parseDouble(shot[4]), Double.parseDouble(shot[5]),
                        Double.parseDouble(shot[6])));
            }
            steps.add(Step.waitTicks(1));
            steps.add(Step.screenshot(shot[2]));
        }
        // in der Zugabe, aus der Nähe
        steps.add(Step.waitTicks(30));
        steps.add(Step.server(any -> clearTricks(fae(any), s)));
        steps.add(pose(s, 0.0, 3.2, -3.0, 4.0, 3.0, 6.0));
        steps.add(at(s, 2.6, 4.4, 0.8, 0.0, 4.9, -3.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("mystik_encore"));
        // ihre letzte Verbeugung (der Ritter tritt beiseite)
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getEntity(s.knight) instanceof LivingEntity knight) {
                Vec3 k = local(s, 6, 1, 8);
                knight.teleportTo(k.x, k.y, k.z);
            }
            if (level.getEntity(s.mystik) instanceof MissMystikEntity mystik) {
                mystik.setNoAi(false);
                Vec3 a = local(s, 0, 3.2, -3);
                mystik.snapTo(a.x, a.y, a.z, 0.0f, 0.0f);
                mystik.setYBodyRot(0.0f);
                mystik.setYHeadRot(0.0f);
                mystik.setHealth(1.0f);
                mystik.hurtServer(level, level.damageSources().genericKill(), 1000.0f);
            }
        }));
        steps.add(at(s, 1.2, 4.4, 1.4, 0.0, 4.6, -3.0));
        steps.add(Step.waitTicks(28));
        steps.add(Step.screenshot("mystik_last_bow"));
        steps.add(Step.waitTicks(MissMystikEntity.DEATH_TICKS - 31));
        steps.add(Step.screenshot("mystik_confetti"));
        steps.add(Step.waitTicks(40));
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getEntity(s.knight) instanceof Entity knight) {
                knight.discard();
            }
            for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, new AABB(s.hat).inflate(30, 30, 30))) {
                item.discard();
            }
        }));
        steps.add(at(s, 2.4, 2.6, -2.2, 0.0, 2.9, -6.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("mystik_hat_empty"));
        // Kartendeck und Zylinder am Rüstungsständer (vorn auf der Bühne)
        steps.add(Step.server(any -> {
            ServerLevel level = fae(any);
            Vec3 p = local(s, 2, 2, -1);
            ArmorStand stand = new ArmorStand(level, p.x, p.y, p.z);
            stand.setShowArms(true);
            stand.setYRot(-24.0f);
            stand.setYBodyRot(-24.0f);
            stand.setYHeadRot(-24.0f);
            stand.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(MystikBlocks.MYSTIC_DECK.get()));
            stand.setItemSlot(EquipmentSlot.HEAD, new ItemStack(MystikBlocks.MYSTIC_TOP_HAT.get()));
            stand.addTag(STAGE_TAG);
            level.addFreshEntity(stand);
        }));
        steps.add(at(s, 3.3, 3.6, 1.9, 2.0, 3.3, -1.0));
        steps.add(Step.waitTicks(30));
        steps.add(Step.screenshot("mystik_gear_stand"));
        steps.add(at(s, 2.9, 4.3, 0.4, 2.0, 4.0, -1.0));
        steps.add(Step.waitTicks(10));
        steps.add(Step.screenshot("mystik_top_hat"));
    }

    /** Räumt auf, was die Tricks zurückließen: Spiegelbilder, Karten, Tauben, Schwerter, Lichtsäulen und Ringe. */
    private static void clearTricks(ServerLevel level, Stage s) {
        AABB area = new AABB(s.hat).inflate(40, 30, 40);
        for (Entity e : level.getEntitiesOfClass(Entity.class, area, e -> e instanceof MystikImageEntity || e instanceof MagicCardEntity
                || e instanceof LightDoveEntity || e instanceof MagicSwordEntity || e instanceof SpellFxEntity)) {
            e.discard();
        }
    }

    /**
     * Wartet, bis MissMystik (im Client) den Trick {@code attack} seit mindestens {@code tick} Ticks zeigt – oder ihn schon
     * beendet hat.
     */
    private static Step attackAt(Stage s, MissMystikEntity.Attack attack, int tick) {
        boolean[] seen = {false};
        return Step.until(() -> {
            var minecraft = net.minecraft.client.Minecraft.getInstance();
            if (minecraft.level == null || !(minecraft.level.getEntity(s.mystik) instanceof MissMystikEntity mystik)) {
                return false;
            }
            if (mystik.currentAttack() == attack) {
                seen[0] = true;
                return mystik.attackTime(0.0f) >= tick;
            }
            return seen[0];
        }, 400);
    }

    /** Dreht MissMystik zur Kamera (zum Spieler). */
    private static Step faceCamera(Stage s) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level == null || !(level.getEntity(s.mystik) instanceof MissMystikEntity mystik) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            double dx = player.getX() - mystik.getX();
            double dz = player.getZ() - mystik.getZ();
            float yaw = (float) (Math.atan2(dz, dx) * 180.0 / Math.PI) - 90.0f;
            mystik.setYRot(yaw);
            mystik.setYBodyRot(yaw);
            mystik.setYHeadRot(yaw);
        });
    }

    /** Sie hält still an (x, y, z), zugewandt dem Punkt (fx, fy, fz). */
    private static Step pose(Stage s, double x, double y, double z, double fx, double fy, double fz) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level.getEntity(s.mystik) instanceof MissMystikEntity mystik) {
                mystik.forceAttack(MissMystikEntity.Attack.NONE);
                mystik.setNoAi(true);
                Vec3 a = local(s, x, y, z);
                Vec3 face = local(s, fx, fy, fz).subtract(a);
                float yaw = (float) (Math.atan2(face.z, face.x) * 180.0 / Math.PI) - 90.0f;
                mystik.snapTo(a.x, a.y, a.z, yaw, 0.0f);
                mystik.setYBodyRot(yaw);
                mystik.setYHeadRot(yaw);
                mystik.setDeltaMovement(Vec3.ZERO);
            }
        });
    }

    /**
     * Kamera bei MissMystik: im Abstand {@code distance}, um {@code angle} Grad aus der Linie MissMystik → Ritter gedreht,
     * {@code height} über ihr; Blick auf sie ({@code toward} 0) oder auf den Ritter ({@code toward} 1).
     */
    private static Step follow(Stage s, double angle, double distance, double height, double toward) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level == null || !(level.getEntity(s.mystik) instanceof Entity mystik) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 c = mystik.position().add(mystik.getDeltaMovement().scale(4.0)).add(0.0, 1.6, 0.0);
            Vec3 k = level.getEntity(s.knight) instanceof Entity knight ? knight.position().add(0.0, 1.0, 0.0) : c.add(0.0, 0.0, -1.0);
            Vec3 line = k.subtract(c).multiply(1.0, 0.0, 1.0);
            if (line.lengthSqr() < 1.0e-3) {
                line = new Vec3(0.0, 0.0, -1.0);
            }
            Vec3 focus = c.add(k.subtract(c).scale(toward));
            Vec3 dir = line.normalize().yRot((float) Math.toRadians(angle));
            Vec3 eye = focus.add(dir.scale(distance)).add(0.0, height, 0.0);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /**
     * Kamera schräg über dem Ritter: {@code back} Blöcke vor ihm (zum Publikum hin), {@code up} über ihm; der Blick geht auf
     * den Ritter, um {@code lift} angehoben (so ist MissMystik dahinter im Bild).
     */
    private static Step above(Stage s, double back, double up, double lift) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level == null || !(level.getEntity(s.knight) instanceof Entity knight) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 focus = knight.position().add(0.0, 0.5 + lift * 3.0, 0.0);
            Vec3 eye = knight.position().add(2.5, up + 1.0, back);
            Vec3 look = focus.subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Kamera am festen Punkt (dx, dy, dz) der Bühne (Augenhöhe), Blick auf MissMystik (dort, wo sie gerade schwebt). */
    private static Step watch(Stage s, double dx, double dy, double dz) {
        return Step.server(any -> {
            ServerLevel level = fae(any);
            if (level == null || !(level.getEntity(s.mystik) instanceof Entity mystik) || any.getServer().getPlayerList().getPlayers().isEmpty()) {
                return;
            }
            Vec3 eye = local(s, dx, dy, dz);
            Vec3 look = mystik.position().add(0.0, 1.6, 0.0).subtract(eye);
            float yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(look.y, Math.sqrt(look.x * look.x + look.z * look.z)) * 180.0 / Math.PI);
            var player = any.getServer().getPlayerList().getPlayers().getFirst();
            player.teleportTo(level, eye.x, eye.y - 1.62, eye.z, java.util.Set.of(), yaw, pitch, true);
        });
    }

    /** Sucht die nächste Zauberbühne, lässt ihre Chunks entstehen und merkt sich Mitte und Zylinder. */
    private static void locate(ServerLevel level, Stage s) {
        if (level == null) {
            s.ready = true;
            return;
        }
        BlockPos center = MystikStage.nearest(level.getSeed(), 600, 600);
        if (center == null) {
            KingdomsAtWar.LOGGER.info("[Showcase] keine Zauberbühne gefunden");
            s.ready = true;
            return;
        }
        int cx = center.getX() >> 4;
        int cz = center.getZ() >> 4;
        for (int dx = -3; dx <= 3; dx++) {
            for (int dz = -3; dz <= 3; dz++) {
                level.setChunkForced(cx + dx, cz + dz, true);
                level.getChunk(cx + dx, cz + dz);
            }
        }
        int x = center.getX();
        int z = center.getZ() + MystikStageFeature.HAT_DZ;
        int top = level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z);
        BlockPos found = null;
        for (int y = top + 2; y > top - 16 && found == null; y--) {
            BlockPos p = new BlockPos(x, y, z);
            if (level.getBlockState(p).is(MystikBlocks.STAGE_HAT.get())) {
                found = p;
            }
        }
        s.hat = found != null ? found : new BlockPos(x, top, z);
        s.c[0] = center.getX() + 0.5;
        s.c[1] = s.hat.getY() - MystikStageFeature.HAT_HEIGHT + 1;
        s.c[2] = center.getZ() + 0.5;
        KingdomsAtWar.LOGGER.info("[Showcase] Zauberbühne bei {}, Zylinder bei {} ({})", center, s.hat, level.getBlockState(s.hat));
        s.ready = true;
    }

    /** Ein Punkt der Bühne (relativ zur Mitte, y über dem Boden der Lichtung) in Weltkoordinaten. */
    private static Vec3 local(Stage s, double x, double y, double z) {
        return new Vec3(s.c[0] + x, s.c[1] + y, s.c[2] + z);
    }

    /** Kamera an Mitte + (dx, dy, dz) (Augenhöhe), Blick auf Mitte + (tx, ty, tz) – im Feenreich. */
    private static Step at(Stage s, double dx, double dy, double dz, double tx, double ty, double tz) {
        return Step.once(minecraft -> {
            s.cam[0] = s.c[0] + dx;
            s.cam[1] = s.c[1] + dy - 1.62;
            s.cam[2] = s.c[2] + dz;
            double lx = tx - dx;
            double ly = ty - dy;
            double lz = tz - dz;
            float yaw = (float) (-Math.atan2(lx, lz) * 180.0 / Math.PI);
            float pitch = (float) (-Math.atan2(ly, Math.sqrt(lx * lx + lz * lz)) * 180.0 / Math.PI);
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT,
                    "execute in kingdomsatwar:fae_realm run tp @s %.2f %.2f %.2f %.1f %.1f", s.cam[0], s.cam[1], s.cam[2], yaw, pitch));
        });
    }

    private static <T extends Mob> T creature(ServerLevel level, net.minecraft.world.entity.EntityType<T> type, Vec3 at, float yaw) {
        T mob = type.create(level, EntitySpawnReason.COMMAND);
        if (mob == null) {
            throw new IllegalStateException("Wesen lässt sich nicht erzeugen: " + type);
        }
        mob.setNoAi(true);
        mob.setPersistenceRequired();
        mob.addTag(STAGE_TAG);
        mob.snapTo(at.x, at.y, at.z, yaw, 0);
        mob.setYBodyRot(yaw);
        mob.setYHeadRot(yaw);
        level.addFreshEntity(mob);
        return mob;
    }
}
