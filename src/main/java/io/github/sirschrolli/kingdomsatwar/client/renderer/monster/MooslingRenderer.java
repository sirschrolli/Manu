package io.github.sirschrolli.kingdomsatwar.client.renderer.monster;

import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import net.minecraft.client.model.monster.spider.SpiderModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.monster.spider.Spider;

/**
 * Arachnophobie-Modus: Statt einer Spinne zeichnet dieser Renderer einen Moosling – einen runden, freundlichen Moosball
 * mit Kulleraugen und Blattschopf auf vier Stummelfüßen (in Höhlen blaugrauer Stein mit leuchtendem Moos). Er nutzt
 * Minecrafts Spinnenmodell auf Spinnenknochen ohne Beine: Der Ball dreht sich, wohin der Moosling schaut.
 */
public class MooslingRenderer<T extends Spider> extends MobRenderer<T, LivingEntityRenderState, SpiderModel> {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/moosling/moosling.png");
    private static final Identifier CAVE_TEXTURE = KingdomsAtWar.id("textures/entity/moosling/moosling_hoehle.png");
    private static final Identifier CAVE_GLOW = KingdomsAtWar.id("textures/entity/moosling/moosling_hoehle_glow.png");
    private final boolean cave;

    public MooslingRenderer(EntityRendererProvider.Context context, boolean cave) {
        super(context, new SpiderModel(context.bakeLayer(cave ? MonsterRenderers.CAVE_MOOSLING : MonsterRenderers.MOOSLING)), cave ? 0.4f : 0.55f);
        this.cave = cave;
        if (cave) {
            RenderType glow = RenderTypes.eyes(CAVE_GLOW);
            this.addLayer(new GlowLayer<>(this, state -> glow));
        }
    }

    @Override
    protected float getFlipDegrees() {
        return 180.0f;
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return this.cave ? CAVE_TEXTURE : TEXTURE;
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }
}
