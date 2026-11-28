package io.github.sirschrolli.kingdomsatwar.entity.creature.nether;

import io.github.sirschrolli.kingdomsatwar.entity.ReplacesVanilla;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.boss.wither.WitherBoss;
import net.minecraft.world.level.Level;

/**
 * Aschenkönig (statt Wither): ein schwebender König aus verkohlten Knochen mit drei Köpfen – der mittlere unter einer
 * Krone aus angelaufenem Gold, in der Seelenfeuer lodert, die seitlichen mit geschwungenen Hörnern –, einem Joch mit
 * Knochendornen, einem Brustkorb, in dem ein Herz aus Seelenfeuer schlägt, langen Krallenarmen, einem Schwanz mit
 * Knochenklinge und einem zerschlissenen Königsmantel in Netherrot. Er erwacht wie der Wither aus vier Seelensand und drei
 * Schädeln der Brandgebeine, lädt sich auf, sprengt sich frei und tut dann alles, was der Wither tut: feuert Seelenschädel
 * aus allen drei Köpfen, verdorrt, heilt sich, zerbricht Blöcke, hüllt sich bei halber Kraft in einen Seelenschild, gegen
 * den Pfeile nichts ausrichten, und lässt einen Netherstern zurück.
 */
public class Aschenkoenig extends WitherBoss implements ReplacesVanilla, io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyWarden {
    public Aschenkoenig(EntityType<? extends WitherBoss> type, Level level) {
        super(type, level);
    }

    // ------------------------------------------------------------------ Himmelswacht (0.82)

    @Override
    public io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme skyTheme() {
        return io.github.sirschrolli.kingdomsatwar.entity.boss.sky.SkyTheme.ASH;
    }

    @Override
    public boolean guardsSky() {
        return this.getInvulnerableTicks() <= 0 && this.getTarget() != null && this.getTarget().isAlive();
    }
}
