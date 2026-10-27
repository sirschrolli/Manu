package io.github.sirschrolli.kingdomsatwar.client.magic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.entity.MagicTableBlockEntity;
import io.github.sirschrolli.kingdomsatwar.magic.KnownSpells;
import io.github.sirschrolli.kingdomsatwar.magic.Spell;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

/**
 * Über dem Magietisch: sieben Runen kreisen um die Platte – je eine für jeden Zauber; die, die der eingelegte Stab
 * kennt, leuchten in ihrer Farbe. Liegt ein Stab im Tisch, schwebt er langsam drehend darüber, eine Zaubertafel liegt
 * schwebend auf der Platte. Beim Einschreiben ziehen sich die Runen zum Stab zusammen, eine Lichtsäule in der Farbe des
 * Zaubers steigt auf, und der Stab wirbelt hoch.
 */
public class MagicTableRenderer implements BlockEntityRenderer<MagicTableBlockEntity, MagicTableRenderer.State> {
    private static final Identifier GLYPHS = KingdomsAtWar.id("textures/block/magic_table_glyphs.png");
    private static final Identifier BEAM = KingdomsAtWar.id("textures/block/magic_table_beam.png");
    private static final RenderType GLYPH_TYPE = RenderTypes.entityTranslucentEmissive(GLYPHS);
    private static final RenderType BEAM_TYPE = RenderTypes.entityTranslucentEmissive(BEAM);
    private static final int FULL_BRIGHT = 0xF000F0;
    private static final int RUNES = 7;
    private static final int GLYPH_COUNT = 8;

    private final ItemModelResolver items;

    public static class State extends BlockEntityRenderState {
        final ItemStackRenderState staff = new ItemStackRenderState();
        final ItemStackRenderState tablet = new ItemStackRenderState();
        boolean hasStaff;
        boolean hasTablet;
        float time;
        float spin;
        float presence;
        /** Fortschritt des Rituals 0..1 (0 = kein Ritual). */
        float ritual;
        int ritualColor;
        final int[] runeColors = new int[RUNES];
        final boolean[] runeLit = new boolean[RUNES];
    }

    public MagicTableRenderer(BlockEntityRendererProvider.Context context) {
        this.items = context.itemModelResolver();
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(MagicTableBlockEntity table, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(table, state, partialTicks, cameraPosition, breakProgress);
        state.time = table.time + partialTicks;
        state.spin = Mth.lerp(partialTicks, table.spinO, table.spin);
        state.presence = Mth.lerp(partialTicks, table.presenceO, table.presence);
        state.ritual = table.ritual > 0 ? 1.0f - (table.ritual - partialTicks) / MagicTableBlockEntity.RITUAL_TICKS : 0.0f;
        state.ritualColor = table.ritualSpell.color();
        int seed = (int) table.getBlockPos().asLong();
        ItemStack staff = table.staff();
        state.hasStaff = !staff.isEmpty();
        this.items.updateForTopItem(state.staff, staff, ItemDisplayContext.FIXED, table.getLevel(), null, seed);
        ItemStack tablet = table.tablet();
        state.hasTablet = !tablet.isEmpty();
        this.items.updateForTopItem(state.tablet, tablet, ItemDisplayContext.FIXED, table.getLevel(), null, seed + 1);
        KnownSpells spells = KnownSpells.of(staff);
        Spell[] all = Spell.values();
        for (int i = 0; i < RUNES; i++) {
            state.runeLit[i] = state.hasStaff && spells.knows(all[i]);
            state.runeColors[i] = all[i].color();
        }
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        float ritual = state.ritual;
        float swell = ritual > 0.0f ? Mth.sin(ritual * Mth.PI) : 0.0f;

        // Runenkreis
        float radius = 0.66f * (1.0f - 0.55f * swell);
        for (int i = 0; i < RUNES; i++) {
            float angle = state.spin + i * Mth.TWO_PI / RUNES;
            float bob = Mth.sin(state.time * 0.07f + i * 1.9f) * 0.035f;
            float height = 1.12f + bob + 0.25f * swell;
            float brightness = state.runeLit[i] ? 0.75f + 0.25f * state.presence : 0.28f + 0.22f * state.presence;
            brightness = Math.min(1.0f, brightness + swell * 0.6f);
            int rgb = state.runeLit[i] ? state.runeColors[i] : 0x9A7ACC;
            if (swell > 0.0f) {
                rgb = ARGB.srgbLerp(swell * 0.7f, rgb, state.ritualColor);
            }
            int color = ARGB.color(Math.round(255 * brightness), rgb);
            int glyph = i % GLYPH_COUNT;
            pose.pushPose();
            pose.translate(0.5f + Mth.cos(angle) * radius, height, 0.5f + Mth.sin(angle) * radius);
            pose.rotate(Axis.YP, -angle - Mth.HALF_PI);
            float size = 0.27f + 0.06f * swell;
            pose.scale(size, size, size);
            collector.submitCustomGeometry(pose, GLYPH_TYPE, (p, buffer) -> glyphQuad(p, buffer, glyph, color));
            pose.popPose();
        }

        // Lichtsäule beim Ritual
        if (swell > 0.01f) {
            int beam = ARGB.color(Math.round(230 * swell), state.ritualColor);
            for (int k = 0; k < 2; k++) {
                pose.pushPose();
                pose.translate(0.5f, 0.82f, 0.5f);
                pose.rotateDegrees(Axis.YP, state.time * 4.0f + k * 90.0f);
                pose.scale(0.55f + 0.25f * swell, 1.4f * swell + 0.2f, 1.0f);
                collector.submitCustomGeometry(pose, BEAM_TYPE, (p, buffer) -> beamQuad(p, buffer, beam));
                pose.popPose();
            }
        }

        // Zaubertafel: schwebt flach über der Platte und dreht sich langsam
        if (state.hasTablet) {
            pose.pushPose();
            pose.translate(0.5f, 0.86f + Mth.sin(state.time * 0.05f) * 0.012f, 0.5f);
            pose.rotateDegrees(Axis.YP, state.time * 0.8f);
            pose.rotateDegrees(Axis.XP, 90.0f);
            pose.scale(0.42f, 0.42f, 0.42f);
            state.tablet.submit(pose, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }

        // Stab: schwebt und dreht sich; beim Ritual wirbelt er hoch
        if (state.hasStaff) {
            pose.pushPose();
            float lift = 0.3f * swell;
            pose.translate(0.5f, 1.42f + Mth.sin(state.time * 0.08f) * 0.05f + lift, 0.5f);
            pose.rotateDegrees(Axis.YP, state.time * (1.6f + 18.0f * swell));
            pose.rotateDegrees(Axis.ZP, -45.0f);
            pose.scale(0.56f, 0.56f, 0.56f);
            state.staff.submit(pose, collector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
            pose.popPose();
        }
    }

    /** Eine Rune als senkrechtes Bild (beide Seiten sichtbar). */
    private static void glyphQuad(PoseStack.Pose pose, VertexConsumer buffer, int glyph, int color) {
        float u0 = glyph / (float) GLYPH_COUNT;
        float u1 = (glyph + 1) / (float) GLYPH_COUNT;
        vertex(buffer, pose, -0.5f, -0.5f, u0, 1.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, u1, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, u1, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, u0, 0.0f, color);
        vertex(buffer, pose, -0.5f, 0.5f, u0, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.5f, u1, 0.0f, color);
        vertex(buffer, pose, 0.5f, -0.5f, u1, 1.0f, color);
        vertex(buffer, pose, -0.5f, -0.5f, u0, 1.0f, color);
    }

    /** Senkrechter Lichtstreifen (unten breit und hell, oben ausgeblendet), beidseitig. */
    private static void beamQuad(PoseStack.Pose pose, VertexConsumer buffer, int color) {
        vertex(buffer, pose, -0.5f, 0.0f, 0.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 1.0f, 1.0f, color);
        vertex(buffer, pose, 0.5f, 1.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 1.0f, 0.0f, 0.0f, color);
        vertex(buffer, pose, -0.5f, 1.0f, 0.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 1.0f, 1.0f, 0.0f, color);
        vertex(buffer, pose, 0.5f, 0.0f, 1.0f, 1.0f, color);
        vertex(buffer, pose, -0.5f, 0.0f, 0.0f, 1.0f, color);
    }

    private static void vertex(VertexConsumer buffer, PoseStack.Pose pose, float x, float y, float u, float v, int color) {
        buffer.addVertex(pose, x, y, 0.0f).setColor(color).setUv(u, v).setOverlay(OverlayTexture.NO_OVERLAY).setLight(FULL_BRIGHT)
                .setNormal(pose, 0.0f, 1.0f, 0.0f);
    }

    @Override
    public AABB getRenderBoundingBox(MagicTableBlockEntity table) {
        BlockPos pos = table.getBlockPos();
        return new AABB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 2.6, pos.getZ() + 1.5);
    }
}
