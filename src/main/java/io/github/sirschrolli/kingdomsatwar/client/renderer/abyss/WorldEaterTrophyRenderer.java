package io.github.sirschrolli.kingdomsatwar.client.renderer.abyss;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.serialization.MapCodec;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.block.abyss.WorldEaterTrophyBlock;
import io.github.sirschrolli.kingdomsatwar.block.abyss.WorldEaterTrophyBlockEntity;
import io.github.sirschrolli.kingdomsatwar.client.model.ModModelLayers;
import java.util.function.Consumer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;
import org.joml.Vector3fc;

/**
 * Der Kopf auf der Trophäe des Weltenretters: das Modell des Weltenfressers auf ein Viertel verkleinert, auf dem Sockel,
 * dem zugewandt, der ihn aufgestellt hat. Die Nähte, Augen und Kronensplitter glühen und pulsieren langsam, der Rachen
 * atmet. Als Gegenstand (Inventar, Hand, Rahmen) sitzt derselbe Kopf auf dem Sockelmodell.
 */
public class WorldEaterTrophyRenderer implements BlockEntityRenderer<WorldEaterTrophyBlockEntity, WorldEaterTrophyRenderer.State> {
    static final Identifier TEXTURE = KingdomsAtWar.id("textures/entity/world_eater/world_eater.png");
    static final RenderType GLOW = RenderTypes.eyes(KingdomsAtWar.id("textures/entity/world_eater/world_eater_glow.png"));
    /** Verkleinerung gegenüber dem Modell (1 Modellpixel = 1/16 Block). */
    static final float SCALE = 0.25f;
    /** Höhe des Drehpunkts des Kopfes über dem Boden des Blocks. */
    static final float HEIGHT = 0.68f;
    /** Der Kopf ist vorn länger als hinten: so weit wird er zurückgeschoben, damit er mittig auf dem Sockel sitzt. */
    static final float BACK = 0.12f;

    private final WorldEaterModel model;

    public static class State extends BlockEntityRenderState {
        final WorldEaterRenderState head = new WorldEaterRenderState();
        float yaw;
        float time;
    }

    public WorldEaterTrophyRenderer(BlockEntityRendererProvider.Context context) {
        this.model = new WorldEaterModel(context.entityModelSet().bakeLayer(ModModelLayers.WORLD_EATER));
    }

    @Override
    public State createRenderState() {
        return new State();
    }

    @Override
    public void extractRenderState(WorldEaterTrophyBlockEntity trophy, State state, float partialTicks, Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
        BlockEntityRenderer.super.extractRenderState(trophy, state, partialTicks, cameraPosition, breakProgress);
        var blockState = trophy.getBlockState();
        Direction facing = blockState.hasProperty(WorldEaterTrophyBlock.FACING) ? blockState.getValue(WorldEaterTrophyBlock.FACING) : Direction.SOUTH;
        state.yaw = facing.toYRot();
        BlockPos pos = trophy.getBlockPos();
        long seed = pos.asLong() * 31L;
        state.time = (trophy.getLevel() == null ? 0L : trophy.getLevel().getGameTime()) % 240_000L + partialTicks + (seed & 255);
        state.head.ageInTicks = state.time;
        state.head.maw = 0.16f + 0.1f * (0.5f + 0.5f * Mth.sin(state.time * 0.045f));
        state.head.lightCoords = state.lightCoords;
    }

    @Override
    public void submit(State state, PoseStack pose, SubmitNodeCollector collector, CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5f, 0.0f, 0.5f);
        submitHead(this.model, state.head, pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, state.yaw, glow(state.time), 0);
        pose.popPose();
    }

    /** Helligkeit des Leuchtens: ein langsames Pulsieren. */
    static int glow(float time) {
        return ARGB.color(Math.round((0.78f + 0.22f * Mth.sin(time * 0.06f)) * 255.0f), 0xFFFFFF);
    }

    /** Zeichnet den Kopf über dem Sockel; {@code pose} steht in der Mitte des Blocks auf dem Boden. */
    static void submitHead(WorldEaterModel model, WorldEaterRenderState head, PoseStack pose, SubmitNodeCollector collector, int light, int overlay,
            float yaw, int glow, int outline) {
        pose.pushPose();
        pose.translate(0.0f, HEIGHT, 0.0f);
        pose.rotate(Axis.YP.rotationDegrees(180.0f - yaw));
        pose.translate(0.0f, 0.0f, BACK);
        pose.scale(-SCALE, -SCALE, SCALE);
        collector.submitModel(model, head, pose, TEXTURE, light, overlay, outline);
        collector.order(1).submitModel(model, head, pose, GLOW, light, OverlayTexture.NO_OVERLAY, glow, null, outline);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(WorldEaterTrophyBlockEntity blockEntity) {
        BlockPos pos = blockEntity.getBlockPos();
        return new AABB(pos.getX() - 0.5, pos.getY(), pos.getZ() - 0.5, pos.getX() + 1.5, pos.getY() + 1.6, pos.getZ() + 1.5);
    }

    /** Die Trophäe als Gegenstand: der Kopf (der Sockel kommt aus dem Blockmodell), dem Betrachter zugewandt. */
    public record ItemRenderer(WorldEaterModel model) implements NoDataSpecialModelRenderer {
        private static final WorldEaterRenderState STILL = new WorldEaterRenderState();

        static {
            STILL.maw = 0.22f;
            STILL.ageInTicks = 0.0f;
        }

        @Override
        public void submit(PoseStack pose, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            pose.pushPose();
            pose.translate(0.5f, 0.0f, 0.5f);
            submitHead(this.model, STILL, pose, collector, lightCoords, overlayCoords, 0.0f, 0xFFFFFFFF, outlineColor);
            pose.popPose();
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            PoseStack pose = new PoseStack();
            pose.translate(0.5f, HEIGHT, 0.5f);
            pose.rotate(Axis.YP.rotationDegrees(180.0f));
            pose.translate(0.0f, 0.0f, BACK);
            pose.scale(-SCALE, -SCALE, SCALE);
            this.model.setupAnim(STILL);
            this.model.root().getExtentsForGui(pose, output);
        }

        public record Unbaked() implements NoDataSpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = MapCodec.unit(new Unbaked());

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public ItemRenderer bake(SpecialModelRenderer.BakingContext context) {
                return new ItemRenderer(new WorldEaterModel(context.entityModelSet().bakeLayer(ModModelLayers.WORLD_EATER)));
            }
        }
    }
}
