package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import io.github.sirschrolli.kingdomsatwar.registry.ModEntities;
import net.minecraft.world.entity.ConversionParams;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EntityTypes;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.piglin.PiglinBrute;
import net.minecraft.world.level.Level;
import org.jspecify.annotations.Nullable;

/**
 * Borstenhüne (statt Piglin-Barbar): ein Wächter des Keilervolks mit gehörntem Eisenhelm, Brustpanzer mit
 * Keilerkopf-Wappen, Schulterstücken und Kettenärmeln. Er bewacht wie der Barbar die Schätze der Bastionen, lässt
 * sich nicht mit Gold besänftigen und greift jeden an, der ihm zu nahe kommt.
 */
public class BristleGiant extends PiglinBrute implements ReplacesVanilla {
    public BristleGiant(EntityType<? extends PiglinBrute> type, Level level) {
        super(type, level);
    }

    /** In der Oberwelt verwest er wie der Barbar – zu einem Moderborstling in seiner dunkelroten Tracht. */
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Mob> @Nullable T convertTo(EntityType<T> type, ConversionParams params, EntitySpawnReason reason,
            ConversionParams.AfterConversion<T> after) {
        if (type != EntityTypes.ZOMBIFIED_PIGLIN) {
            return super.convertTo(type, params, reason, after);
        }
        return (T) super.convertTo(ModEntities.ZOMBIFIED_PIGLIN.get(), params, reason, rotten -> {
            rotten.setLook(RottenBristling.Look.MODER_KARMESIN);
            after.finalizeConversion((T) rotten);
        });
    }
}
