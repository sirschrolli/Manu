package io.github.sirschrolli.kingdomsatwar.client.renderer.nether;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.client.model.nether.AschenkoenigModel;
import io.github.sirschrolli.kingdomsatwar.client.renderer.monster.GlowLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.state.WitherRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.boss.wither.WitherBoss;

/**
 * Aschenkönig: eigenes Modell (im Spiel doppelt so groß, beim Erwachen wächst er wie der Wither von 1,5 auf 2), eigene
 * Bilder – während er sich auflädt, das gebleichte, ganz im Seelenfeuer glühende Bild, am Ende im Wechsel –, Augen,
 * Risse, Herz und Krone leuchten, bei halber Kraft hüllt ihn der Seelenschild ein. Er leuchtet selbst (wie der Wither).
 */
public class AschenkoenigRenderer extends MobRenderer<WitherBoss, WitherRenderState, AschenkoenigModel> {
    private static final Identifier TEXTURE = NetherRenderers.texture("wither", "aschenkoenig");
    private static final Identifier AWAKENING = NetherRenderers.texture("wither", "aschenkoenig_erwachen");
    private static final RenderType GLOW = RenderTypes.eyes(NetherRenderers.texture("wither", "aschenkoenig_glow"));
    private static final RenderType AWAKENING_GLOW = RenderTypes.eyes(NetherRenderers.texture("wither", "aschenkoenig_erwachen_glow"));
    private static final Identifier SHIELD = NetherRenderers.texture("wither", "seelenschild");

    public AschenkoenigRenderer(EntityRendererProvider.Context context) {
        super(context, new AschenkoenigModel(context.bakeLayer(NetherRenderers.ASCHENKOENIG)), 1.0f);
        this.addLayer(new GlowLayer<>(this, state -> awakening(state) ? AWAKENING_GLOW : GLOW));
        this.addLayer(new ShieldLayer(this, new AschenkoenigModel(context.bakeLayer(NetherRenderers.ASCHENKOENIG_SHIELD)).shell()));
    }

    /** Beim Aufladen das glühende Bild, in den letzten vier Sekunden im Wechsel (wie beim Wither). */
    static boolean awakening(WitherRenderState state) {
        int ticks = Mth.floor(state.invulnerableTicks);
        return ticks > 0 && (ticks > 80 || ticks / 5 % 2 != 1);
    }

    @Override
    protected int getBlockLightLevel(WitherBoss entity, BlockPos pos) {
        return 15;
    }

    @Override
    public Identifier getTextureLocation(WitherRenderState state) {
        return awakening(state) ? AWAKENING : TEXTURE;
    }

    @Override
    public WitherRenderState createRenderState() {
        return new WitherRenderState();
    }

    @Override
    protected void scale(WitherRenderState state, PoseStack poseStack) {
        float scale = 2.0f;
        if (state.invulnerableTicks > 0.0f) {
            scale -= state.invulnerableTicks / 220.0f * 0.5f;
        }
        poseStack.scale(scale, scale, scale);
    }

    @Override
    public void extractRenderState(WitherBoss entity, WitherRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        int ticks = entity.getInvulnerableTicks();
        state.invulnerableTicks = ticks > 0 ? ticks - partialTicks : 0.0f;
        System.arraycopy(entity.getHeadXRots(), 0, state.xHeadRots, 0, state.xHeadRots.length);
        System.arraycopy(entity.getHeadYRots(), 0, state.yHeadRots, 0, state.yHeadRots.length);
        state.isPowered = entity.isPowered();
    }

    /** Seelenschild: Schlieren aus Seelenfeuer wandern über eine knapp größere Hülle des Modells. */
    private static final class ShieldLayer extends EnergySwirlLayer<WitherRenderState, AschenkoenigModel> {
        private final AschenkoenigModel shell;

        ShieldLayer(RenderLayerParent<WitherRenderState, AschenkoenigModel> parent, AschenkoenigModel shell) {
            super(parent);
            this.shell = shell;
        }

        @Override
        protected boolean isPowered(WitherRenderState state) {
            return state.isPowered;
        }

        @Override
        protected float xOffset(float t) {
            return Mth.cos(t * 0.02f) * 3.0f;
        }

        @Override
        protected Identifier getTextureLocation() {
            return SHIELD;
        }

        @Override
        protected AschenkoenigModel model() {
            return this.shell;
        }
    }
}
