package io.github.sirschrolli.kingdomsatwar.client.boss;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Thalassas Modell: eine Seeschlange – fünf Halsglieder, sechzehn Glieder Schlangenleib mit Schwanzflosse, zwei
 * Brustflossen (in den Kanälen der Vorderbeine), keine Hinterbeine, keine Flügel.
 *
 * <p>Dazu der Kragen aus sieben Flossenstrahlen: Er liegt in Ruhe nach hinten angelegt und stellt sich bei Drohung
 * und Angriff fächerförmig auf (gesteuert über den sonst ungenutzten Spreiz-Kanal der Flügel). Die Barteln an der
 * Schnauze treiben träge im Wasser.
 */
public class ThalassaModel extends DragonBossModel {
    private final List<ModelPart> frill = new ArrayList<>();
    private final ModelPart[] barbels = new ModelPart[4];

    public ThalassaModel(ModelPart root) {
        super(root, 5, 16, true, false, 0, 10.0f);
        Function<String, @Nullable ModelPart> part = root.createPartLookup();
        for (String name : new String[] {"frill_c0", "frill_l1", "frill_r1", "frill_l2", "frill_r2", "frill_l3", "frill_r3"}) {
            ModelPart p = part.apply(name);
            if (p != null) {
                this.frill.add(p);
            }
        }
        String[] b = {"barbel_l1", "barbel_r1", "barbel_l2", "barbel_r2"};
        for (int i = 0; i < 4; i++) {
            this.barbels[i] = part.apply(b[i]);
        }
    }

    @Override
    public DragonMotion createMotion() {
        return new ThalassaMotion(this.neck.length, this.tail.length, true, this.bodyHeight);
    }

    @Override
    public void setupAnim(BossRenderState s) {
        super.setupAnim(s);
        // Kragen: angelegt (0) bis voll aufgestellt (1)
        float flare = Mth.clamp(s.spread, 0.0f, 1.2f);
        float t = s.ageInTicks / 20.0f;
        for (int i = 0; i < this.frill.size(); i++) {
            ModelPart p = this.frill.get(i);
            float tremble = Mth.sin(t * 9.0f + i * 1.3f) * 0.03f * flare;
            p.xRot += -0.75f * (1.0f - Math.min(1.0f, flare)) + tremble;
            p.zRot *= 0.45f + 0.55f * Math.min(1.0f, flare) + 0.1f * Math.max(0.0f, flare - 1.0f);
        }
        // Barteln: treiben träge, schwingen dem Kopf nach
        for (int i = 0; i < 4; i++) {
            ModelPart p = this.barbels[i];
            if (p == null) {
                continue;
            }
            float side = i % 2 == 0 ? 1.0f : -1.0f;
            float lag = i < 2 ? 0.0f : 0.8f;
            p.xRot += Mth.sin(t * 1.3f - lag + side) * 0.12f - s.headPitch * 0.4f;
            p.zRot += Mth.sin(t * 0.9f - lag) * 0.1f * side - s.headYaw * 0.3f;
        }
    }
}
