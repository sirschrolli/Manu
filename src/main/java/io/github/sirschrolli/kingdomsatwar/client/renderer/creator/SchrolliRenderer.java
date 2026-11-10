package io.github.sirschrolli.kingdomsatwar.client.renderer.creator;

import com.mojang.blaze3d.vertex.PoseStack;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity;
import io.github.sirschrolli.kingdomsatwar.entity.creator.SchrolliEntity.Attack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import org.jspecify.annotations.Nullable;

/**
 * Schrolli der Schöpfer, doppelt so groß wie sein Modell (in der Wahren Gestalt noch ein Drittel größer): Augen, Krone,
 * Heiligenschein, Sonne auf der Brust, Zepterstern und Federspitzen leuchten; das Gewand der Wahren Gestalt ist ein
 * leuchtendes Weltall. Beim Erscheinen, Aufsteigen, im Lichtsprung, bei der Neuschöpfung und im Vergehen leuchtet seine
 * ganze Gestalt in weißem (oder violettem) Licht auf.
 */
public class SchrolliRenderer extends MobRenderer<SchrolliEntity, SchrolliRenderState, SchrolliModel> {
    public static final float SCALE = 2.0f;
    private static final Identifier TEXTURE = tex("schrolli");
    private static final Identifier TEXTURE_TRUE = tex("schrolli_true");
    private static final RenderType GLOW = RenderTypes.eyes(tex("schrolli_glow"));
    private static final RenderType GLOW_TRUE = RenderTypes.eyes(tex("schrolli_true_glow"));
    private static final RenderType FLASH = RenderTypes.eyes(tex("schrolli_flash"));
    private static final RenderType FLASH_TRUE = RenderTypes.eyes(tex("schrolli_true_flash"));

    private static Identifier tex(String name) {
        return KingdomsAtWar.id("textures/entity/schrolli/" + name + ".png");
    }

    public SchrolliRenderer(EntityRendererProvider.Context context) {
        super(context, new SchrolliModel(context.bakeLayer(CreatorRenderers.SCHROLLI)), 2.6f);
        this.addLayer(new RenderLayer<>(this) {
            @Override
            public void submit(PoseStack poseStack, SubmitNodeCollector collector, int light, SchrolliRenderState state, float yRot, float xRot) {
                if (state.isInvisible) {
                    return;
                }
                float fade = fade(state);
                float t = state.ageInTicks;
                float glow = (0.88f + Mth.sin(t * 0.09f) * 0.08f + Mth.sin(t * 0.41f) * 0.04f) * fade;
                if ((state.flags & SchrolliEntity.FLAG_GLARE) != 0) {
                    glow = Math.min(1.0f, glow + 0.3f);
                }
                collector.order(1).submitModel(this.getParentModel(), state, poseStack, state.trueForm ? GLOW_TRUE : GLOW, light,
                        OverlayTexture.NO_OVERLAY, ARGB.color(Math.round(Mth.clamp(glow, 0.0f, 1.0f) * 255.0f), 0xFFFFFF), null, state.outlineColor);
                if (state.trueForm && state.deathTime <= 0.0f) {
                    // das Weltall seines Gewands schimmert ein zweites Mal, violett und langsam pulsierend
                    float shine = 0.35f + Mth.sin(t * 0.07f) * 0.15f;
                    collector.order(2).submitModel(this.getParentModel(), state, poseStack, GLOW_TRUE, light, OverlayTexture.NO_OVERLAY,
                            ARGB.color(Math.round(shine * 255.0f), 0xD8B0FF), null, state.outlineColor);
                }
                if (state.flash > 0.01f) {
                    int tint = (state.flags & SchrolliEntity.FLAG_CHANNEL) != 0 ? 0xC890FF : 0xFFF6E0;
                    collector.order(3).submitModel(this.getParentModel(), state, poseStack, state.trueForm ? FLASH_TRUE : FLASH, light,
                            OverlayTexture.NO_OVERLAY, ARGB.color(Math.round(Mth.clamp(state.flash, 0.0f, 1.0f) * 255.0f), tint), null,
                            state.outlineColor);
                }
            }
        });
    }

    /** Im Vergehen wird er durchsichtig (das letzte Fünftel). */
    private static float fade(SchrolliRenderState state) {
        if (state.deathTime <= 0.0f) {
            return 1.0f;
        }
        float d = Mth.clamp(state.deathTime / SchrolliEntity.DEATH_TICKS, 0.0f, 1.0f);
        return d < 0.82f ? 1.0f : Math.max(0.0f, 1.0f - (d - 0.82f) / 0.12f);
    }

    /** Wie hell seine ganze Gestalt gerade aufleuchtet. */
    private static float flash(SchrolliEntity entity, Attack attack, float a, float age) {
        if (entity.deathTime > 0) {
            float d = entity.deathTime / (float) SchrolliEntity.DEATH_TICKS;
            return Mth.clamp(0.15f + (d - 0.5f) * 1.8f, 0.0f, 1.0f) + Mth.sin(age * 0.8f) * 0.08f * d;
        }
        return switch (attack) {
            case AWAKEN -> Math.max(Mth.clamp(1.0f - a / 40.0f, 0.0f, 1.0f), Mth.clamp(1.0f - Math.abs(a - SchrolliEntity.AWAKEN_LAND) / 10.0f, 0.0f, 1.0f));
            case ASCEND -> a < SchrolliEntity.ASCEND_SHATTER ? Mth.clamp(a / SchrolliEntity.ASCEND_SHATTER, 0.0f, 1.0f)
                    : Mth.clamp(1.0f - (a - SchrolliEntity.ASCEND_REVEAL) / 40.0f, 0.0f, 1.0f);
            case BLINK -> Math.max(Mth.clamp(1.0f - Math.abs(a - 4.0f) / 4.0f, 0.0f, 1.0f), Mth.clamp(1.0f - Math.abs(a - 16.0f) / 5.0f, 0.0f, 1.0f));
            case RECREATION -> 0.3f + Mth.sin(age * 0.25f) * 0.18f;
            case WORD -> Mth.clamp((a - 20.0f) / 36.0f, 0.0f, 1.0f) * (a < 58.0f ? 0.8f : 0.0f);
            case SUPERNOVA -> a > 120.0f && a < 145.0f ? Mth.clamp((a - 120.0f) / 20.0f, 0.0f, 0.7f) : 0.0f;
            default -> 0.0f;
        };
    }

    @Override
    public Identifier getTextureLocation(SchrolliRenderState state) {
        return state.trueForm ? TEXTURE_TRUE : TEXTURE;
    }

    @Override
    protected @Nullable RenderType getRenderType(SchrolliRenderState state, boolean isBodyVisible, boolean forceTransparent, boolean appearGlowing) {
        Identifier texture = this.getTextureLocation(state);
        if (!isBodyVisible && !forceTransparent && !appearGlowing) {
            return null;
        }
        return appearGlowing && !isBodyVisible ? RenderTypes.outline(texture) : RenderTypes.entityTranslucent(texture);
    }

    @Override
    protected int getModelTint(SchrolliRenderState state) {
        return ARGB.color(Math.round(fade(state) * 255.0f), 0xFFFFFF);
    }

    @Override
    public SchrolliRenderState createRenderState() {
        return new SchrolliRenderState();
    }

    @Override
    public void extractRenderState(SchrolliEntity entity, SchrolliRenderState state, float partialTicks) {
        super.extractRenderState(entity, state, partialTicks);
        Attack attack = entity.currentAttack();
        state.attack = attack.ordinal();
        state.attackTime = entity.attackTime(partialTicks);
        state.trueForm = entity.isTrueForm();
        state.flags = (entity.hasFlag(SchrolliEntity.FLAG_HALO) ? SchrolliEntity.FLAG_HALO : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_ORB) ? SchrolliEntity.FLAG_ORB : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_HIDDEN) ? SchrolliEntity.FLAG_HIDDEN : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_CHANNEL) ? SchrolliEntity.FLAG_CHANNEL : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_STUN) ? SchrolliEntity.FLAG_STUN : 0)
                | (entity.hasFlag(SchrolliEntity.FLAG_GLARE) ? SchrolliEntity.FLAG_GLARE : 0);
        state.wing = Mth.lerp(partialTicks, entity.wingO, entity.wing);
        state.ascension = entity.ascension(partialTicks);
        state.flash = flash(entity, attack, state.attackTime, state.ageInTicks);
        if ((state.flags & SchrolliEntity.FLAG_HIDDEN) != 0) {
            state.isInvisible = true;
        }
        // im Vergehen nicht rot getönt (er vergeht in Licht)
        if (entity.deathTime > 0) {
            state.hasRedOverlay = false;
        }
    }

    @Override
    protected void scale(SchrolliRenderState state, PoseStack poseStack) {
        poseStack.scale(SCALE, SCALE, SCALE);
    }

    /** Er kippt im Tod nicht zur Seite: er vergeht in Licht. */
    @Override
    protected float getFlipDegrees() {
        return 0.0f;
    }

    @Override
    protected int getBlockLightLevel(SchrolliEntity entity, BlockPos pos) {
        return 15;
    }

    @Override
    protected float getShadowStrength(SchrolliRenderState state) {
        return 0.4f;
    }
}
