package io.github.sirschrolli.kingdomsatwar.client.creator;

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
 * Der Himmel der Schöpfung: Über dem violetten Himmel mit seinen Sternen hängt hoch oben ein <b>Sonnenrad</b> – ein goldener
 * Kranz aus Strahlen und Zeichen, der sich langsam dreht. Ringsum leuchten <b>Nebel</b> in Violett, Rosa und Türkis, eine
 * <b>Galaxie</b>, ferne <b>Welten</b> (eine mit Ring) und Bündel aus <b>Lichtstrahlen</b> am Horizont; tief unten, unter dem
 * Meer aus Wolken, schimmern weitere Sterne. Erhebt sich der Schöpfer in seiner Wahren Gestalt, öffnet sich der Himmel: Ein
 * gewaltiges <b>Auge</b> aus Sternen erscheint im Zenit, Risse aus weißem Licht klaffen, und alles leuchtet heller; beim
 * Jüngsten Gericht und der Supernova flammt der ganze Himmel auf. Auf dem Weg zum Allmächtigen (seit 0.78) verschlingt ein
 * Schwarzes Loch das Licht; dann brennt eine gewaltige <b>Sonne</b> im Zenit, und der ganze Himmel glüht.
 *
 * <p>Gezeichnet wird nach dem Himmel (vor dem Gelände) mit der Pipeline der Himmelskörper: Das Bild
 * ({@code textures/environment/creation_sky.png}, 4×4 Felder) wird additiv aufgetragen.
 */
@EventBusSubscriber(modid = KingdomsAtWar.MODID, value = Dist.CLIENT)
public final class CreationSkyRenderer {
    private static final Identifier TEXTURE = KingdomsAtWar.id("textures/environment/creation_sky.png");
    private static final float DISTANCE = 100.0f;

    /**
     * Ein Bild am Himmel: Richtung (Gier, Abstand vom Zenit in Grad), Drehung in der Fläche, halbe Breite/Höhe (bei Abstand
     * 100), Feld im Atlas (0–15), Helligkeit, Drehgeschwindigkeit (Grad je Sekunde), Takt des Atmens, und ob es erst mit der
     * Wahren Gestalt erscheint (dann wächst es mit ihr).
     */
    record Element(float yaw, float zenith, float roll, float halfWidth, float halfHeight, int cell, float brightness, float spin, float breathe,
            boolean revealed, boolean sun) {}

    static final Element[] ELEMENTS = {
            // das Sonnenrad, hoch oben, und sein Lichthof
            new Element(30, 22, 0, 46, 46, 12, 0.75f, 0.0f, 0.3f, false, false),
            new Element(30, 22, 0, 30, 30, 0, 1.0f, 2.2f, 0.4f, false, false),
            // Nebel
            new Element(150, 52, 20, 70, 48, 1, 0.75f, 0.0f, 0.15f, false, false),
            new Element(260, 40, -30, 64, 46, 2, 0.7f, 0.0f, 0.2f, false, false),
            new Element(340, 64, 10, 58, 38, 1, 0.55f, 0.0f, 0.18f, false, false),
            new Element(80, 70, 40, 50, 34, 2, 0.5f, 0.0f, 0.22f, false, false),
            // eine Galaxie
            new Element(205, 30, 35, 26, 26, 3, 0.85f, 0.6f, 0.1f, false, false),
            // ferne Welten
            new Element(110, 38, 0, 9, 9, 4, 0.95f, 0.0f, 0.05f, false, false),
            new Element(300, 30, 0, 6, 6, 5, 0.9f, 0.0f, 0.05f, false, false),
            new Element(20, 60, 0, 5, 5, 6, 0.9f, 0.0f, 0.05f, false, false),
            new Element(235, 66, 0, 4, 4, 5, 0.7f, 0.0f, 0.05f, false, false),
            // Strahlenbündel am Horizont
            new Element(0, 84, 0, 60, 26, 7, 0.55f, 0.0f, 0.25f, false, false),
            new Element(120, 86, 0, 52, 24, 7, 0.5f, 0.0f, 0.3f, false, false),
            new Element(240, 84, 0, 56, 26, 7, 0.5f, 0.0f, 0.28f, false, false),
            // Sternhaufen
            new Element(60, 48, 0, 30, 30, 11, 0.8f, 0.0f, 0.2f, false, false),
            new Element(190, 74, 0, 34, 34, 11, 0.7f, 0.0f, 0.2f, false, false),
            new Element(290, 56, 0, 28, 28, 11, 0.75f, 0.0f, 0.2f, false, false),
            // unter den Wolken
            new Element(90, 140, 0, 60, 60, 11, 0.55f, 0.0f, 0.15f, false, false),
            new Element(270, 150, 30, 70, 50, 2, 0.4f, 0.0f, 0.15f, false, false),
            // die Wahre Gestalt: das Auge der Schöpfung und Risse aus Licht
            new Element(0, 0, 0, 58, 58, 9, 1.0f, -1.2f, 0.35f, true, false),
            new Element(70, 34, 25, 44, 44, 10, 0.9f, 0.0f, 0.5f, true, false),
            new Element(200, 42, -40, 40, 40, 10, 0.85f, 0.0f, 0.55f, true, false),
            new Element(315, 38, 70, 36, 36, 10, 0.8f, 0.0f, 0.6f, true, false),
            new Element(140, 70, 0, 50, 36, 8, 0.7f, 0.0f, 0.4f, true, false),
            // der Allmächtige (seit 0.78): eine gewaltige Sonne im Zenit, ihr Kranz, und Glut über dem Horizont
            new Element(30, 22, 0, 96, 96, 12, 1.0f, 0.0f, 0.5f, false, true),
            new Element(30, 22, 0, 60, 60, 0, 1.0f, -4.0f, 0.6f, false, true),
            new Element(30, 22, 15, 44, 44, 0, 0.8f, 6.5f, 0.7f, false, true),
            new Element(60, 84, 0, 70, 30, 7, 0.8f, 0.0f, 0.35f, false, true),
            new Element(180, 85, 0, 66, 28, 7, 0.75f, 0.0f, 0.4f, false, true),
            new Element(300, 84, 0, 68, 30, 7, 0.8f, 0.0f, 0.38f, false, true),
            // der ganze Himmel glüht: weite, weiche Lichthöfe rundum
            new Element(0, 58, 0, 120, 100, 12, 0.5f, 0.0f, 0.2f, false, true),
            new Element(72, 60, 0, 120, 100, 12, 0.45f, 0.0f, 0.22f, false, true),
            new Element(144, 58, 0, 120, 100, 12, 0.5f, 0.0f, 0.18f, false, true),
            new Element(216, 60, 0, 120, 100, 12, 0.45f, 0.0f, 0.24f, false, true),
            new Element(288, 58, 0, 120, 100, 12, 0.5f, 0.0f, 0.2f, false, true),
    };

    private static @Nullable GpuBuffer vertices;

    private CreationSkyRenderer() {}

    @SubscribeEvent
    static void afterSky(RenderLevelStageEvent.AfterSky event) {
        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || !CreatorClient.inCreation(level)) {
            return;
        }
        if (vertices == null) {
            vertices = build();
        }
        float partial = minecraft.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        float seconds = (level.getGameTime() % 1_728_000L + partial) / 20.0f;
        float reveal = CreatorClient.reveal(partial);
        float flare = CreatorClient.flare();
        float sunrise = CreatorClient.sunrise(partial);
        float eclipse = CreatorClient.eclipse(partial);
        AbstractTexture texture = minecraft.getTextureManager().getTexture(TEXTURE);
        RenderSystem.AutoStorageIndexBuffer indices = RenderSystem.getSequentialBuffer(PrimitiveTopology.QUADS);
        GpuBuffer indexBuffer = indices.getBuffer(6);
        RenderTarget target = minecraft.gameRenderer.mainRenderTarget();
        Matrix4f view = new Matrix4f(event.getModelViewMatrix());
        try (RenderPass pass = RenderSystem.getDevice().createCommandEncoder()
                .createRenderPass(() -> "Creation sky", target.getColorTextureView(), Optional.empty(), target.getDepthTextureView(),
                        OptionalDouble.empty())) {
            pass.setPipeline(RenderSystem.getCompiledPipeline(RenderPipelines.CELESTIAL));
            RenderSystem.bindDefaultUniforms(pass);
            pass.setUniform("Sampler0", texture.getTextureView(), texture.getSampler());
            pass.setVertexBuffer(0, vertices.slice());
            pass.setIndexBuffer(indexBuffer, indices.type());
            for (int i = 0; i < ELEMENTS.length; i++) {
                Element e = ELEMENTS[i];
                float glow = e.brightness() * (0.88f + 0.12f * Mth.sin(seconds * e.breathe() * 2.0f + i * 1.7f));
                if (e.sun()) {
                    glow *= sunrise;
                } else if (e.revealed()) {
                    // der Allmächtige verbrennt das Auge der Wahren Gestalt
                    glow *= reveal * (1.0f - sunrise);
                } else {
                    // in der Wahren Gestalt leuchtet alles heller (das Sonnenrad flammt violett-weiß)
                    glow *= 1.0f + 0.35f * reveal;
                }
                // das Schwarze Loch verschlingt das Licht des Himmels
                glow *= 1.0f - 0.85f * eclipse;
                glow = Mth.clamp(glow + flare * 0.35f * (1.0f - eclipse), 0.0f, 1.0f);
                if (glow < 0.01f) {
                    continue;
                }
                Matrix4f m = new Matrix4f(view);
                float spin = e.spin() * (e.cell() == 0 ? 1.0f + 1.5f * reveal : 1.0f);
                if (spin != 0.0f) {
                    // um die eigene Mitte drehen: um die Achse vom Betrachter zum Bild
                    Vector3f axis = direction(e);
                    m.rotate(seconds * spin * Mth.DEG_TO_RAD, axis.x, axis.y, axis.z);
                }
                float r = 1.0f;
                float g = 1.0f;
                float b = 1.0f;
                if (e.sun()) {
                    // die Sonne des Allmächtigen: weißgolden innen, glühend orange außen
                    g = e.cell() == 12 ? 0.62f : 0.86f;
                    b = e.cell() == 12 ? 0.3f : 0.55f;
                } else if (e.cell() == 0 || e.cell() == 12) {
                    // das Sonnenrad wird in der Wahren Gestalt bleich-violett (beim Allmächtigen glühend)
                    r = 1.0f - 0.15f * reveal * (1.0f - sunrise);
                    g = 1.0f - 0.25f * reveal * (1.0f - sunrise) - 0.2f * sunrise;
                    b = 1.0f - 0.5f * sunrise;
                } else {
                    // alles wird warm
                    g = 1.0f - 0.18f * sunrise;
                    b = 1.0f - 0.45f * sunrise;
                }
                GpuBufferSlice transforms = RenderSystem.getDynamicUniforms().writeTransform(m, new Vector4f(r, g, b, glow));
                pass.setUniform("DynamicTransforms", transforms);
                pass.drawIndexed(6, 1, 0, i * 4, 0);
            }
        }
    }

    private static Vector3f direction(Element e) {
        Matrix4f pose = new Matrix4f().rotateY(-e.yaw() * Mth.DEG_TO_RAD).rotateX(e.zenith() * Mth.DEG_TO_RAD);
        return pose.transformDirection(new Vector3f(0.0f, 1.0f, 0.0f)).normalize();
    }

    private static GpuBuffer build() {
        try (ByteBufferBuilder bytes = ByteBufferBuilder.exactlySized(ELEMENTS.length * 4 * DefaultVertexFormat.POSITION_TEX.getVertexSize())) {
            BufferBuilder builder = new BufferBuilder(bytes, PrimitiveTopology.QUADS, DefaultVertexFormat.POSITION_TEX);
            for (Element e : ELEMENTS) {
                Matrix4f pose = new Matrix4f()
                        .rotateY(-e.yaw() * Mth.DEG_TO_RAD)
                        .rotateX(e.zenith() * Mth.DEG_TO_RAD)
                        .rotateY(e.roll() * Mth.DEG_TO_RAD);
                float u0 = (e.cell() % 4) * 0.25f;
                float v0 = (e.cell() / 4) * 0.25f;
                float w = e.halfWidth();
                float h = e.halfHeight();
                vertex(builder, pose, -w, -h, u0, v0);
                vertex(builder, pose, w, -h, u0 + 0.25f, v0);
                vertex(builder, pose, w, h, u0 + 0.25f, v0 + 0.25f);
                vertex(builder, pose, -w, h, u0, v0 + 0.25f);
            }
            try (MeshData mesh = builder.buildOrThrow()) {
                return RenderSystem.getDevice().createBuffer(() -> "Creation sky", 32, mesh.vertexBuffer());
            }
        }
    }

    private static void vertex(BufferBuilder builder, Matrix4f pose, float x, float z, float u, float v) {
        Vector3f p = pose.transformPosition(new Vector3f(x, DISTANCE, z));
        builder.addVertex(p.x, p.y, p.z).setUv(u, v);
    }
}
