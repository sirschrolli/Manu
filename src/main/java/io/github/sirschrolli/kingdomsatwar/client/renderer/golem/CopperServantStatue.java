package io.github.sirschrolli.kingdomsatwar.client.renderer.golem;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mojang.math.Transformation;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.Consumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.CopperGolemStatueRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.client.renderer.special.NoDataSpecialModelRenderer;
import net.minecraft.client.renderer.special.SpecialModelRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.util.Unit;
import net.minecraft.world.level.block.CopperGolemStatueBlock;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.entity.CopperGolemStatueBlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector3fc;
import org.jspecify.annotations.Nullable;

/**
 * Statuen des Kupferknechts: Ein ganz oxidierter Kupferknecht erstarrt irgendwann zur Statue (dem Vanilla-Block).
 * Gezeichnet wird sie – im Spiel wie im Inventar – mit dem Modell und den Bildern des Kupferknechts in den vier
 * Haltungen der Statue (stehend, sitzend, laufend, mit ausgebreiteten Armen).
 */
public final class CopperServantStatue {
    private CopperServantStatue() {}

    /** Das Modell des Kupferknechts in einer festen Haltung, aufrecht für Block und Gegenstand gedreht (wie Vanillas Statuenmodell). */
    static final class StatueModel extends Model<Unit> {
        private final CopperGolemStatueBlock.Pose pose;
        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart rightArm;
        private final ModelPart leftArm;
        private final ModelPart rightLeg;
        private final ModelPart leftLeg;

        StatueModel(ModelPart root, CopperGolemStatueBlock.Pose pose) {
            super(root, RenderTypes::entityCutout);
            this.pose = pose;
            this.body = root.getChild("body");
            this.head = this.body.getChild("head");
            this.rightArm = this.body.getChild("right_arm");
            this.leftArm = this.body.getChild("left_arm");
            this.rightLeg = root.getChild("right_leg");
            this.leftLeg = root.getChild("left_leg");
        }

        @Override
        public void setupAnim(Unit ignored) {
            this.resetPose();
            this.root.y = 24.0f;                           // gedreht um die Höhe der Füße: die Füße stehen auf dem Blockboden
            this.root.zRot = Mth.PI;                       // wie Vanillas Statue: aufrecht ohne die Spiegelung des Wesen-Renderers
            switch (this.pose) {
                case SITTING -> {
                    this.root.y -= 3.5f;                   // tiefer, damit er auf dem Boden sitzt
                    this.rightLeg.xRot = -Mth.HALF_PI;
                    this.leftLeg.xRot = -Mth.HALF_PI;
                    this.rightArm.xRot = -0.55f;
                    this.leftArm.xRot = -0.55f;
                    this.head.xRot = 0.12f;
                }
                case RUNNING -> {
                    this.body.xRot = 0.14f;
                    this.head.xRot = -0.1f;
                    this.rightArm.xRot = -1.0f;
                    this.leftArm.xRot = 0.87f;
                    this.rightLeg.xRot = -0.85f;
                    this.leftLeg.xRot = 0.75f;
                }
                case STAR -> {
                    this.rightArm.zRot = 1.92f;
                    this.leftArm.zRot = -1.92f;
                    this.rightLeg.zRot = 0.26f;
                    this.leftLeg.zRot = -0.26f;
                }
                default -> { }
            }
        }
    }

    static Map<CopperGolemStatueBlock.Pose, StatueModel> models(EntityModelSet modelSet) {
        Map<CopperGolemStatueBlock.Pose, StatueModel> models = new EnumMap<>(CopperGolemStatueBlock.Pose.class);
        for (CopperGolemStatueBlock.Pose pose : CopperGolemStatueBlock.Pose.values()) {
            models.put(pose, new StatueModel(modelSet.bakeLayer(GolemRenderers.COPPER_GOLEM), pose));
        }
        return models;
    }

    static Identifier texture(WeatheringCopper.WeatherState state) {
        return CopperServantRenderer.TEXTURES[Mth.clamp(state.ordinal(), 0, CopperServantRenderer.TEXTURES.length - 1)];
    }

    /** Ersetzt Vanillas Statuen-Renderer: dieselbe Lage und Drehung, aber der Kupferknecht. */
    public static final class BlockRenderer implements BlockEntityRenderer<CopperGolemStatueBlockEntity, CopperGolemStatueRenderState> {
        private final Map<CopperGolemStatueBlock.Pose, StatueModel> models;

        public BlockRenderer(BlockEntityRendererProvider.Context context) {
            this.models = models(context.entityModelSet());
        }

        @Override
        public CopperGolemStatueRenderState createRenderState() {
            return new CopperGolemStatueRenderState();
        }

        @Override
        public void extractRenderState(CopperGolemStatueBlockEntity blockEntity, CopperGolemStatueRenderState state, float partialTicks,
                Vec3 cameraPosition, ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress) {
            BlockEntityRenderer.super.extractRenderState(blockEntity, state, partialTicks, cameraPosition, breakProgress);
            var blockState = blockEntity.getBlockState();
            state.direction = blockState.getValue(CopperGolemStatueBlock.FACING);
            state.pose = blockState.getValue(CopperGolemStatueBlock.POSE);
            state.oxidationState = blockState.getBlock() instanceof CopperGolemStatueBlock statue ? statue.getWeatheringState()
                    : WeatheringCopper.WeatherState.UNAFFECTED;
        }

        @Override
        public void submit(CopperGolemStatueRenderState state, PoseStack poseStack, SubmitNodeCollector collector, CameraRenderState camera) {
            poseStack.pushPose();
            poseStack.mulPose(new Transformation(new Matrix4f().translation(0.5f, 0.0f, 0.5f)
                    .rotate(Axis.YP.rotationDegrees(-state.direction.getOpposite().toYRot()))));
            StatueModel model = this.models.get(state.pose);
            Identifier texture = texture(state.oxidationState);
            collector.submitModel(model, Unit.INSTANCE, poseStack, texture, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
            if (state.breakProgress != null) {
                collector.order(1).submitCrumblingOverlay(model, Unit.INSTANCE, poseStack, model.renderType(texture), state.lightCoords,
                        OverlayTexture.NO_OVERLAY, -1, state.breakProgress);
            }
            poseStack.popPose();
        }

        @Override
        public AABB getRenderBoundingBox(CopperGolemStatueBlockEntity blockEntity) {
            BlockPos pos = blockEntity.getBlockPos();
            return new AABB(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1.5, pos.getZ() + 1);
        }
    }

    /** Die Statue als Gegenstand (Inventar, Hand, Rahmen): Stufe der Verwitterung und Haltung wie beim Block. */
    public record ItemRenderer(StatueModel model, Identifier texture) implements NoDataSpecialModelRenderer {
        @Override
        public void submit(PoseStack poseStack, SubmitNodeCollector collector, int lightCoords, int overlayCoords, boolean hasFoil, int outlineColor) {
            collector.submitModel(this.model, Unit.INSTANCE, poseStack, this.texture, lightCoords, overlayCoords, outlineColor);
        }

        @Override
        public void getExtents(Consumer<Vector3fc> output) {
            PoseStack poseStack = new PoseStack();
            this.model.setupAnim(Unit.INSTANCE);
            this.model.root().getExtentsForGui(poseStack, output);
        }

        public record Unbaked(WeatheringCopper.WeatherState stage, CopperGolemStatueBlock.Pose pose) implements NoDataSpecialModelRenderer.Unbaked {
            public static final MapCodec<Unbaked> MAP_CODEC = RecordCodecBuilder.mapCodec(i -> i.group(
                    WeatheringCopper.WeatherState.CODEC.fieldOf("stage").forGetter(Unbaked::stage),
                    CopperGolemStatueBlock.Pose.CODEC.fieldOf("pose").forGetter(Unbaked::pose)).apply(i, Unbaked::new));

            @Override
            public MapCodec<Unbaked> type() {
                return MAP_CODEC;
            }

            @Override
            public ItemRenderer bake(SpecialModelRenderer.BakingContext context) {
                return new ItemRenderer(new StatueModel(context.entityModelSet().bakeLayer(GolemRenderers.COPPER_GOLEM), this.pose),
                        CopperServantStatue.texture(this.stage));
            }
        }
    }
}
