package io.github.sirschrolli.kingdomsatwar.client.renderer;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.client.renderer.end.EndRenderers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.MonsterRenderers;
import io.github.sirschrolli.kingdomsatwar.client.renderer.nether.NetherRenderers;
import java.util.List;
import java.util.Map;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.object.skull.SkullModelBase;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.SkullBlock;
import org.jspecify.annotations.Nullable;

/**
 * Köpfe und Schädel im Aussehen der eigenen Wesen: Der Skelettschädel ist der Schädel eines Gerippes, der
 * Witherskelettschädel der eines Brandgebeins (mit Flammen), der Zombiekopf der eines Wiedergängers, der Creeperkopf der
 * eines Kriechers, der Piglinkopf der eines Borstlings (mit wackelnden Ohren) und der Drachenkopf der des Sternenfressers
 * (mit klappendem Kiefer). Gebaut aus den Köpfen der Wesen selbst – derselben Geometrie, derselben Textur –, als Block, als
 * Gegenstand und auf dem Kopf getragen. Die Blöcke bleiben die von Vanilla (Witherbeschwörung, Masken, Notenblöcke).
 */
public final class OwnHeads {
    private OwnHeads() {}

    private record Look(ModelLayerLocation layer, Identifier texture) {}

    private static final Map<SkullBlock.Types, Look> LOOKS = Map.of(
            SkullBlock.Types.SKELETON, new Look(MonsterRenderers.BONES, KingdomsAtWar.id("textures/entity/skeleton/gerippe_knochen.png")),
            SkullBlock.Types.WITHER_SKELETON, new Look(MonsterRenderers.BONES, KingdomsAtWar.id("textures/entity/skeleton/brandgebein.png")),
            SkullBlock.Types.ZOMBIE, new Look(MonsterRenderers.UNDEAD, KingdomsAtWar.id("textures/entity/undead/zombie_bauer.png")),
            SkullBlock.Types.CREEPER, new Look(MonsterRenderers.CREEPER, KingdomsAtWar.id("textures/entity/creeper/kriecher_wald.png")),
            SkullBlock.Types.PIGLIN, new Look(NetherRenderers.BORSTLING, KingdomsAtWar.id("textures/entity/piglin/karmesin.png")),
            SkullBlock.Types.DRAGON, new Look(EndRenderers.STERNENFRESSER, KingdomsAtWar.id("textures/entity/ender_dragon/sternenfresser.png")));

    /** Die Texturen der Köpfe eintragen (bevor Blöcke und Gegenstände sie zum ersten Mal brauchen). */
    public static void install() {
        LOOKS.forEach((type, look) -> SkullBlockRenderer.SKIN_BY_TYPE.put(type, look.texture()));
    }

    /** Das Modell für einen Kopf von Vanilla (oder {@code null}: dann bleibt es beim Vanilla-Modell). */
    public static @Nullable SkullModelBase create(EntityModelSet models, SkullBlock.Type type) {
        if (!(type instanceof SkullBlock.Types vanilla) || !LOOKS.containsKey(vanilla)) {
            return null;
        }
        ModelPart head = models.bakeLayer(LOOKS.get(vanilla).layer()).getChild("head");
        ModelPart root = new ModelPart(List.of(), Map.of("head", head));
        return switch (vanilla) {
            case SKELETON -> new Head(root, "mushrooms", "flames");
            case WITHER_SKELETON -> new Head(root, "mushrooms");
            case PIGLIN -> new BristlingHead(root);
            case DRAGON -> new DevourerHead(root);
            default -> new Head(root);
        };
    }

    /** Ein Kopf, der wie ein Schädel auf dem Block sitzt (der Hals unten in der Mitte); Teile anderer Abarten aus. */
    private static class Head extends SkullModelBase {
        protected final ModelPart head;
        private final String[] hidden;

        Head(ModelPart root, String... hidden) {
            super(root);
            this.head = root.getChild("head");
            this.hidden = hidden;
        }

        @Override
        public void setupAnim(SkullModelBase.State state) {
            super.setupAnim(state);
            this.head.x = 0.0f;
            this.head.y = 0.0f;
            this.head.z = 0.0f;
            this.head.yRot = state.yRot * Mth.DEG_TO_RAD;
            this.head.xRot = state.xRot * Mth.DEG_TO_RAD;
            for (String part : this.hidden) {
                if (this.head.hasChild(part)) {
                    this.head.getChild(part).visible = false;
                }
            }
        }
    }

    /** Der Borstlingskopf: die Ohren wackeln wie bei Vanilla, wenn der Kopf Strom bekommt. */
    private static final class BristlingHead extends Head {
        BristlingHead(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(SkullModelBase.State state) {
            super.setupAnim(state);
            if (this.head.hasChild("left_ear") && this.head.hasChild("right_ear")) {
                this.head.getChild("left_ear").zRot = (float) (-(Math.cos(state.animationPos * Math.PI * 0.2 * 1.2) + 2.5)) * 0.2f;
                this.head.getChild("right_ear").zRot = (float) (Math.cos(state.animationPos * Math.PI * 0.2) + 2.5) * 0.2f;
            }
        }
    }

    /** Der Kopf des Sternenfressers, verkleinert wie der Drachenkopf; der Kiefer klappt, wenn der Kopf Strom bekommt. */
    private static final class DevourerHead extends Head {
        DevourerHead(ModelPart root) {
            super(root);
        }

        @Override
        public void setupAnim(SkullModelBase.State state) {
            super.setupAnim(state);
            this.head.y = -7.986666f;
            this.head.xScale = 0.75f;
            this.head.yScale = 0.75f;
            this.head.zScale = 0.75f;
            if (this.head.hasChild("jaw")) {
                this.head.getChild("jaw").xRot = (float) (Math.sin(state.animationPos * Math.PI * 0.2) + 1.0) * 0.2f;
            }
        }
    }
}
