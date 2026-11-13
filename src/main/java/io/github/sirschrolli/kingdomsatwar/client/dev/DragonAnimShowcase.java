package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity;
import io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Nur zur Prüfung der Drachen-Bewegungen: Schritt, Galopp, Jungtier, Hinsetzen, Abheben, Flug, Kurvenflug, Landen, Odem,
 * Brüllen und Biss – jeweils als Bildfolge (ein Bild je Spieltick), die Kamera begleitet den Drachen von der Seite.
 */
final class DragonAnimShowcase {
    private DragonAnimShowcase() {}

    /**
     * Eine Szene: Stufe, Bilder, Vorlauf in Ticks, Start in der Luft (Höhe), was zu Beginn der Aufnahme geschieht und
     * wie sich der Drache je Tick bewegt (Tick seit Beginn des Vorlaufs).
     */
    private record Clip(String name, DragonStage stage, int frames, int preroll, double height, Consumer<DragonEntity> start,
            BiConsumer<DragonEntity, Integer> move, double camDistance, double camHeight, boolean behind) {}

    /**
     * Ein Schaubild: eine Szene mit einer anderen Drachenart, aufgenommen bis Bild {@code frames} aus einer schönen
     * Perspektive – Kamera relativ zum Drachen (vorn, oben, rechts in Blöcken) und Blickziel (Höhe über den Füßen,
     * vorn).
     */
    private record Hero(String name, String clip, DragonType type, int frames, double forward, double up, double right,
            double lookUp, double lookForward) {}

    private static final List<Hero> HEROES = List.of(
            new Hero("sit", "sit", DragonType.ICE, 34, 3.6, 1.2, 3.9, 1.9, 0.5),
            new Hero("roar", "roar", DragonType.LIGHTNING, 30, 4.0, 0.8, 3.5, 2.1, 0.6),
            new Hero("land", "land", DragonType.FIRE, 18, 2.8, 0.8, 5.0, 1.5, 0.0),
            new Hero("bank", "bank", DragonType.NATURE, 24, -5.0, 2.6, 4.2, 0.9, 1.0));

    private static final Consumer<DragonEntity> NOTHING = d -> { };

    private static final List<Clip> CLIPS = List.of(
            new Clip("walk", DragonStage.ADULT, 24, 20, 0, NOTHING, (d, t) -> forward(d, 0.1, 0.0), 10.0, 1.6, false),
            new Clip("run", DragonStage.ADULT, 24, 20, 0, NOTHING, (d, t) -> forward(d, 0.34, 0.0), 11.0, 1.6, false),
            new Clip("hatchling", DragonStage.HATCHLING, 24, 20, 0, NOTHING, (d, t) -> forward(d, 0.06, 0.0), 3.5, 0.5, false),
            new Clip("sit", DragonStage.ADULT, 36, 10, 0, d -> {
                d.setOrderedToSit(true);
                d.setInSittingPose(true);
            }, (d, t) -> { }, 9.0, 1.4, false),
            new Clip("takeoff", DragonStage.ADULT, 36, 10, 0, d -> d.setFlying(true),
                    (d, t) -> forward(d, t > 10 ? 0.16 : 0.0, t > 10 ? Math.min(0.3, (t - 10) * 0.03) : 0.0), 12.0, 3.0, false),
            new Clip("fly", DragonStage.ADULT, 30, 30, 8, d -> { }, (d, t) -> forward(d, 0.4, 0.0), 13.0, 0.0, false),
            new Clip("glide", DragonStage.ADULT, 24, 30, 14, d -> { }, (d, t) -> forward(d, 0.45, -0.22), 13.0, 0.0, false),
            new Clip("bank", DragonStage.ADULT, 30, 30, 8, d -> { }, (d, t) -> turn(d, 0.4, 5.0f), 12.0, 2.5, true),
            new Clip("land", DragonStage.ADULT, 40, 20, 5, d -> { }, DragonAnimShowcase::land, 12.0, 2.0, false),
            new Clip("breath", DragonStage.ADULT, 40, 10, 0, d -> d.startBreath(null), (d, t) -> { }, 10.0, 1.6, false),
            new Clip("roar", DragonStage.ADULT, 44, 10, 0, DragonEntity::roar, (d, t) -> { }, 10.0, 1.6, false),
            new Clip("bite", DragonStage.ADULT, 16, 10, 0, d -> d.level().broadcastEntityEvent(d, (byte) 111), (d, t) -> { }, 9.0, 1.6,
                    false));

    static void review(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        if (only.equals("hero")) {
            int row = 0;
            for (Hero hero : HEROES) {
                Clip clip = CLIPS.stream().filter(c -> c.name().equals(hero.clip())).findFirst().orElseThrow();
                clip(steps, clip, hero, 14000 + row * 60);
                row++;
            }
            return;
        }
        int row = 0;
        for (Clip clip : CLIPS) {
            if (!only.isEmpty() && !clip.name().equals(only)) {
                continue;
            }
            clip(steps, clip, null, 14000 + row * 60);
            row++;
        }
    }

    private static void clip(Deque<Step> steps, Clip clip, @Nullable Hero hero, int z) {
        int x0 = 14000;
        DragonType type = hero != null ? hero.type() : DragonType.FIRE;
        String name = hero != null ? "dhero_" + hero.name() : "danim_" + clip.name();
        int frames = hero != null ? hero.frames() : clip.frames();
        DragonEntity[] dragon = new DragonEntity[1];
        int[] tick = new int[1];
        steps.add(Showcase.tp(x0 + 10.5, 6, z + 14, 180, 8));
        steps.add(Step.waitTicks(10));
        steps.add(Step.waitForChunks(400));
        steps.add(Step.server(level -> {
            int g = Showcase.ground;
            Showcase.fill(level, new BlockPos(x0 - 20, g - 1, z - 20), new BlockPos(x0 + 60, g - 1, z + 20), Blocks.GRASS_BLOCK.defaultBlockState());
            Showcase.fill(level, new BlockPos(x0 - 20, g, z - 20), new BlockPos(x0 + 60, g + 24, z + 20), Blocks.AIR.defaultBlockState());
            DragonEntity d = ModEntities.DRAGONS.get(type).get().create(level, EntitySpawnReason.COMMAND);
            if (d == null) {
                return;
            }
            d.snapTo(x0 + 0.5, g + clip.height(), z + 0.5, -90.0f, 0.0f);
            d.setYBodyRot(-90.0f);
            d.setYHeadRot(-90.0f);
            d.setNoAi(true);
            d.setPersistenceRequired();
            d.applyStage(clip.stage(), false);
            level.addFreshEntity(d);
            if (clip.height() > 0) {
                d.setFlying(true);
            }
            dragon[0] = d;
        }));
        steps.add(Step.waitTicks(10));
        for (int i = 0; i < clip.preroll(); i++) {
            steps.add(Step.server(level -> clip.move().accept(dragon[0], tick[0]++)));
            steps.add(Step.waitTicks(1));
        }
        steps.add(Step.server(level -> clip.start().accept(dragon[0])));
        steps.add(Step.record(name, frames, minecraft -> {
            DragonEntity d = dragon[0];
            if (d == null) {
                return;
            }
            minecraft.getSingleplayerServer().execute(() -> clip.move().accept(d, tick[0]++));
            Vec3 cam;
            float yaw;
            if (hero != null) {
                // Schaubild: Kamera schräg vor bzw. neben dem Drachen, Blick auf Brust und Kopf
                Vec3 fwd = Vec3.directionFromRotation(0, d.getYRot());
                Vec3 right = Vec3.directionFromRotation(0, d.getYRot() + 90.0f);
                cam = d.position().add(fwd.scale(hero.forward())).add(right.scale(hero.right())).add(0, hero.up(), 0);
                Vec3 target = d.position().add(fwd.scale(hero.lookForward())).add(0, hero.lookUp(), 0);
                Vec3 look = target.subtract(cam);
                yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
                float pitch = (float) (-Math.atan2(look.y, look.horizontalDistance()) * 180.0 / Math.PI);
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x,
                        cam.y - 1.62, cam.z, yaw, pitch));
                return;
            }
            if (clip.behind()) {
                Vec3 back = Vec3.directionFromRotation(0, d.getYRot()).scale(-clip.camDistance());
                Vec3 side = Vec3.directionFromRotation(0, d.getYRot() + 90.0f).scale(clip.camDistance() * 0.35);
                cam = d.position().add(back).add(side).add(0, clip.camHeight(), 0);
                Vec3 look = d.position().add(0, 1.5, 0).subtract(cam);
                yaw = (float) (-Math.atan2(look.x, look.z) * 180.0 / Math.PI);
            } else {
                cam = new Vec3(d.getX(), d.getY() + clip.camHeight(), d.getZ() + clip.camDistance());
                yaw = 180.0f;
            }
            minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f %.1f %.1f", cam.x, cam.y,
                    cam.z, yaw, clip.behind() ? 10.0f : 4.0f));
        }));
        steps.add(Step.server(level -> {
            if (dragon[0] != null) {
                dragon[0].discard();
            }
        }));
    }

    /** Geradeaus nach Osten (vorwärts) und auf/ab. */
    private static void forward(DragonEntity d, double speed, double climb) {
        if (d == null) {
            return;
        }
        d.setYRot(-90.0f);
        d.setYBodyRot(-90.0f);
        d.setYHeadRot(-90.0f);
        d.setPos(d.getX() + speed, d.getY() + climb, d.getZ());
    }

    /** Kurvenflug: dreht sich je Tick um {@code degrees} und fliegt dabei vorwärts. */
    private static void turn(DragonEntity d, double speed, float degrees) {
        if (d == null) {
            return;
        }
        float yaw = d.getYRot() + degrees;
        d.setYRot(yaw);
        d.setYBodyRot(yaw);
        d.setYHeadRot(yaw);
        Vec3 dir = Vec3.directionFromRotation(0, yaw);
        d.setPos(d.getX() + dir.x * speed, d.getY(), d.getZ() + dir.z * speed);
    }

    /** Landeanflug: sinkt langsamer werdend bis zum Boden, setzt auf und läuft aus. */
    private static void land(DragonEntity d, int t) {
        if (d == null) {
            return;
        }
        double above = d.getY() - Showcase.ground;
        if (d.isFlying()) {
            double sink = Math.min(above, Math.max(0.05, above * 0.09));
            forward(d, Math.max(0.06, 0.24 - t * 0.003), -sink);
            if (d.getY() - Showcase.ground < 0.02) {
                d.setPos(d.getX(), Showcase.ground, d.getZ());
                d.setFlying(false);
            }
        } else {
            forward(d, Math.max(0.0, 0.08 - (t - 30) * 0.01), 0.0);
        }
    }
}
