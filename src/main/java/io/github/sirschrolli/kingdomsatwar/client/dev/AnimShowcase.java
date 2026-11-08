package io.github.sirschrolli.kingdomsatwar.client.dev;

import io.github.sirschrolli.kingdomsatwar.client.dev.ShowcaseRunner.Step;
import io.github.sirschrolli.kingdomsatwar.dragon.DragonType;
import io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierType;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import java.util.Deque;
import java.util.List;
import java.util.Locale;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

/**
 * Nur zur Prüfung der Bewegungen: Jedes Wesen läuft (ohne eigenen Willen, mit fester Geschwindigkeit) eine Strecke nach
 * Osten, die Kamera begleitet es von der Seite, und für jeden Spieltick entsteht ein Bild. So lassen sich Laufzyklen,
 * Kopf, Schwanz und Körper Bild für Bild ansehen.
 */
final class AnimShowcase {
    private AnimShowcase() {}

    /** Ein Wesen, wie schnell es läuft (Blöcke je Tick) und wie weit die Kamera weg ist. */
    private record Subject(String name, Supplier<? extends EntityType<? extends Mob>> type, double speed, double distance, double height) {}

    private static final List<Subject> SUBJECTS = List.of(
            new Subject("knight", () -> ModEntities.SOLDIERS.get(SoldierType.KNIGHT).get(), 0.13, 5.0, 1.0),
            new Subject("villager", ModEntities.VILLAGER::get, 0.1, 5.0, 1.0),
            new Subject("cattle", ModEntities.CATTLE::get, 0.1, 6.0, 0.8),
            new Subject("pig", ModEntities.FARM_PIG::get, 0.1, 5.0, 0.6),
            new Subject("sheep", ModEntities.FARM_SHEEP::get, 0.1, 5.0, 0.7),
            new Subject("chicken", ModEntities.FARM_CHICKEN::get, 0.08, 3.5, 0.4),
            new Subject("horse_walk", ModEntities.FARM_HORSE::get, 0.1, 7.0, 1.0),
            new Subject("horse_gallop", ModEntities.FARM_HORSE::get, 0.42, 7.0, 1.0),
            new Subject("wolf", ModEntities.WOLF::get, 0.16, 5.0, 0.5),
            new Subject("fox", ModEntities.FOX::get, 0.16, 4.0, 0.4),
            new Subject("bear", ModEntities.BEAR::get, 0.12, 7.0, 1.0),
            new Subject("deer", ModEntities.DEER::get, 0.14, 6.0, 1.0),
            new Subject("boar", ModEntities.WILD_BOAR::get, 0.13, 5.0, 0.6),
            new Subject("troll", ModEntities.CAVE_TROLL::get, 0.1, 8.0, 1.6),
            new Subject("zombie", ModEntities.ZOMBIE::get, 0.1, 5.0, 1.0),
            new Subject("skeleton", ModEntities.SKELETON::get, 0.1, 5.0, 1.0),
            new Subject("piglin", ModEntities.PIGLIN::get, 0.12, 5.0, 1.0),
            new Subject("hoglin", ModEntities.HOGLIN::get, 0.13, 7.0, 0.9),
            new Subject("enderman", ModEntities.ENDERMAN::get, 0.12, 7.0, 1.6),
            new Subject("dragon", () -> ModEntities.DRAGONS.get(DragonType.FIRE).get(), 0.16, 12.0, 1.8));

    static void review(Deque<Step> steps, String only) {
        steps.add(Step.hud(false));
        steps.add(Step.command("gamemode spectator"));
        steps.add(Step.command("time set 6000"));
        steps.add(Step.command("gamerule advance_time false"));
        steps.add(Step.command("weather clear"));
        int z0 = 12000;
        int row = 0;
        for (Subject subject : SUBJECTS) {
            if (!only.isEmpty() && !subject.name().equals(only)) {
                continue;
            }
            int x0 = 12000;
            int z = z0 + row * 40;
            row++;
            Mob[] mob = new Mob[1];
            steps.add(Showcase.tp(x0 + 6.5, 4, z + subject.distance(), 180, 8));
            steps.add(Step.waitTicks(10));
            steps.add(Step.waitForChunks(400));
            steps.add(Step.server(level -> {
                int g = Showcase.ground;
                Showcase.fill(level, new BlockPos(x0 - 10, g - 1, z - 12), new BlockPos(x0 + 40, g - 1, z + 14), Blocks.GRASS_BLOCK.defaultBlockState());
                Showcase.fill(level, new BlockPos(x0 - 10, g, z - 12), new BlockPos(x0 + 40, g + 14, z + 14), Blocks.AIR.defaultBlockState());
                Mob m = subject.type().get().create(level, EntitySpawnReason.COMMAND);
                if (m == null) {
                    return;
                }
                m.snapTo(x0 + 0.5, g, z + 0.5, -90.0f, 0.0f);
                m.setYBodyRot(-90.0f);
                m.setYHeadRot(-90.0f);
                m.setNoAi(true);
                m.setPersistenceRequired();
                if (m instanceof io.github.sirschrolli.kingdomsatwar.entity.soldier.SoldierEntity soldier) {
                    soldier.equip();
                }
                if (m instanceof io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonEntity dragon) {
                    dragon.applyStage(io.github.sirschrolli.kingdomsatwar.entity.dragon.DragonStage.byNumber(4), false);
                }
                level.addFreshEntity(m);
                mob[0] = m;
            }));
            steps.add(Step.waitTicks(20));
            // erst anlaufen lassen (damit der Laufzyklus voll im Gang ist), dann Bild für Bild
            steps.add(Step.server(level -> walk(mob[0], subject.speed())));
            for (int i = 0; i < 12; i++) {
                steps.add(Step.server(level -> walk(mob[0], subject.speed())));
                steps.add(Step.waitTicks(1));
            }
            steps.add(Step.record("anim_" + subject.name(), 16, minecraft -> {
                Mob m = mob[0];
                if (m == null) {
                    return;
                }
                minecraft.getSingleplayerServer().execute(() -> walk(m, subject.speed()));
                Vec3 cam = new Vec3(m.getX(), m.getY() + subject.height(), m.getZ() + subject.distance());
                minecraft.player.connection.sendCommand(String.format(Locale.ROOT, "tp @s %.2f %.2f %.2f 180 4", cam.x, cam.y - 1.62 + 0.4, cam.z));
            }));
            steps.add(Step.server(level -> {
                if (mob[0] != null) {
                    mob[0].discard();
                }
            }));
        }
    }

    /** Schiebt das Wesen gleichmäßig nach Osten (die Laufanimation folgt der echten Bewegung). */
    private static void walk(Mob m, double speed) {
        if (m == null || m.level().isClientSide()) {
            return;
        }
        m.setYRot(-90.0f);
        m.setYBodyRot(-90.0f);
        m.setYHeadRot(-90.0f);
        // ohne eigenen Willen bewegt sich ein Wesen nicht von selbst: Schritt für Schritt versetzen (die Laufanimation
        // ergibt sich aus der Strecke je Tick)
        m.setPos(m.getX() + speed, m.getY(), m.getZ());
        if (m instanceof LivingEntity living) {
            living.setSprinting(speed > 0.3);
        }
    }
}
