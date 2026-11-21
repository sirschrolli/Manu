package io.github.sirschrolli.kingdomsatwar.entity.boss;

import java.util.List;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Gemeinsames der Bosse (Drachen und Magier): wen sie als Gegner sehen und wie sie Schaden austeilen. So können
 * Geschosse ({@link BossProjectile}), Flächentreffer ({@link BossFx}) und Zauber mit jedem Boss arbeiten.
 */
public interface ElementalBoss {
    /** Ist das ein Gegner dieses Bosses? (Spieler, Soldaten, gezähmte Drachen und alle, die ihn angegriffen haben) */
    boolean isBossEnemy(@Nullable LivingEntity entity);

    /** Alle Gegner in einem Bereich. */
    List<LivingEntity> enemiesIn(AABB box);

    /** Alle Gegner im Umkreis (waagerecht) um einen Punkt. */
    List<LivingEntity> enemiesAround(Vec3 center, double radius, double height);

    /** Fügt einem Gegner Schaden zu (zählt als Angriff des Bosses). */
    boolean hurtTarget(ServerLevel level, LivingEntity target, float damage);

    /** Wie stark die Angriffe gerade sind (Wut, Schwierigkeit). */
    float damageMultiplier();
}
