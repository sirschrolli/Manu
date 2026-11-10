package io.github.sirschrolli.kingdomsatwar.client.abyss;

import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.ByteBufferBuilder;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.MeshData;
import com.mojang.renderpearl.api.buffers.GpuBuffer;
import com.mojang.renderpearl.api.buffers.GpuBufferSlice;
import com.mojang.renderpearl.api.commands.RenderPass;
import com.mojang.renderpearl.api.pipeline.PrimitiveTopology;
import io.github.sirschrolli.kingdomsatwar.KingdomsAtWar;
import io.github.sirschrolli.kingdomsatwar.abyss.AbyssGate;
import java.util.Optional;
import java.util.OptionalDouble;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;
import org.jspecify.annotations.Nullable;

/**
 * Der Himmel des Abgrunds: Über dem tiefen Karmesin klaffen <b>Risse</b> – weißglühende Sprünge mit orangem Glühen und
 * rotem Schein, sternförmige Einschläge, ein klaffendes „Auge“, hinter dem fremde Sterne funkeln. Sie hängen überall, auch
 * unter dem Horizont (unten ist hier ja nur wieder Himmel), atmen langsam und flammen ab und zu auf.
 *
 * <p>Gezeichnet wird nach dem Himmel (vor dem Gelände) mit der Pipeline der Himmelskörper: Das Bild
 * ({@code textures/environment/abyss_rifts.png}, vier Varianten) wird additiv aufgetragen und vom Dunst nicht verschluckt.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class AbyssSkyRenderer {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/environment/abyss_rifts.png");
    private static final float DISTANCE = 100.0f;

    /**
     * Ein Riss am Himmel: Richtung (Gierwinkel, Abstand vom Zenit – 0 senkrecht oben, 90 am Horizont, 180 senkrecht unten),
     * Drehung in der Fläche, halbe Breite/Höhe (bei Abstand 100), Bildvariante, Grundhelligkeit, Phase und Takt des Atmens,
     * Abstand der Flammenstöße in Sekunden.
     */
    record Rift(float yaw, float zenith, float roll, float halfWidth, float halfHeight, int variant, float brightness, float phase,
            float speed, float flarePeriod) {}

    static final Rift[] RIFTS = {
            // der große Riss quer über den Zenit
            new Rift(20, 6, 35, 92, 92, 0, 1.0f, 0.0f, 0.55f, 31),
            // die großen in halber Höhe
            new Rift(140, 48, 8, 56, 56, 2, 0.95f, 1.3f, 0.45f, 43),
            new Rift(250, 36, 70, 50, 50, 1, 0.85f, 2.1f, 0.62f, 37),
            new Rift(318, 58, -24, 50, 50, 3, 0.8f, 4.0f, 0.5f, 29),
            new Rift(172, 24, -60, 34, 34, 3, 0.7f, 5.2f, 0.7f, 53),
            // am Horizont
            new Rift(76, 79, 4, 62, 40, 0, 0.72f, 0.7f, 0.48f, 47),
            new Rift(196, 83, 0, 30, 30, 1, 0.6f, 3.3f, 0.66f, 59),
            new Rift(284, 86, 90, 36, 36, 3, 0.62f, 2.7f, 0.58f, 41),
            new Rift(14, 70, -16, 30, 30, 2, 0.66f, 1.9f, 0.52f, 61),
            // unter dem Horizont – unten ist nur wieder Himmel
            new Rift(108, 118, 30, 52, 52, 0, 0.55f, 4.6f, 0.44f, 67),
            new Rift(228, 134, -40, 44, 44, 1, 0.5f, 0.4f, 0.6f, 71),
            new Rift(350, 152, 0, 60, 60, 2, 0.5f, 3.8f, 0.4f, 79),
    };

    private static @Nullable GpuBuffer vertices;

    private AbyssSkyRenderer() {}

    @SubscribeEvent
    static void afterSky(RenderLevelStageEvent.AfterSky event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || level.dimension() != AbyssGate.THE_ABYSS) {
            return;
        }
        if (vertices == null) {
            vertices = build();
        }
        AbstractTexture texture = minecraft.getTextureManager().getTexture(TEXTURE);
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6);
        float seconds = (level.getGameTime() % 1_728_000L + minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false)) / 20.0f;
        RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Abyss sky rifts", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(),
                        OptionalDouble.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.CELESTIAL));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("Sampler0", texture.getTextureView(), texture.getSampler());
            pass.setVertexBuffer(0, vertices.slice());
            pass.setIndexBuffer(indexBuffer, indices.type());
            for (int i = 0; i < RIFTS.length; i++) {
                float glow = glow(RIFTS[i], seconds);
                GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(view, new Vector4f(1.0f, 1.0f, 1.0f, glow));
                pass.setUniform("DynamicTransforms", transforms);
                pass.drawIndexed(6, 1, 0, i * 4, 0);
            }
        }
    }

    /** Helligkeit eines Risses: langsames Atmen, ein feines Flackern und ab und zu ein Aufflammen, das langsam abklingt. */
    static float glow(Rift rift, float seconds) {
        float breathe = 0.84f + 0.11f * Mth.sin(seconds * rift.speed() + rift.phase()) + 0.05f * Mth.sin(seconds * 2.3f + rift.phase() * 1.7f);
        float local = (seconds + rift.phase() * 7.0f) % rift.flarePeriod();
        float flare = local < 0.25f ? local / 0.25f : (float) Math.exp(-(local - 0.25f) * 1.6f);
        return Mth.clamp(rift.brightness() * (breathe + 0.35f * flare), 0.0f, 1.0f);
    }

    private static GpuBuffer build() {
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(RIFTS.length * 4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
            for (Rift rift : RIFTS) {
                Matrix4f pose = new Matrix4f()
                        .rotateY(-rift.yaw() * Mth.DEG_TO_RAD)
                        .rotateX(rift.zenith() * Mth.DEG_TO_RAD)
                        .rotateY(rift.roll() * Mth.DEG_TO_RAD);
                float u0 = (rift.variant() % 2) * 0.5f;
                float v0 = (rift.variant() / 2) * 0.5f;
                float w = rift.halfWidth();
                float h = rift.halfHeight();
                vertex(builder, pose, -w, -h, u0, v0);
                vertex(builder, pose, w, -h, u0 + 0.5f, v0);
                vertex(builder, pose, w, h, u0 + 0.5f, v0 + 0.5f);
                vertex(builder, pose, -w, h, u0, v0 + 0.5f);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "Abyss sky rifts", 32, mesh.vertexBuffer());
            }
        }
    }

    private static void vertex(BufferBuilder builder, Matrix4f pose, float x, float z, float u, float v) {
        Vector3f p = pose.transformPosition(new Vector3f(x, DISTANCE, z));
        builder.addVertex(p.x, p.y, p.z).setUv(u, v);
    }
}
