package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.core.data.ControlPoint;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Draws a small box at each stored point. Toggle with Z.
 * Particles remain as a fallback for platforms without the render event.
 * Northing is stored as -z, so world z is -northing.
 */
public final class PointMarkers {

    private static boolean visible;
    private static boolean renderRegistered;

    private PointMarkers() {}

    public static boolean visible() { return visible; }

    public static void toggle() {
        visible = !visible;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.inGameHud != null) {
            int count = PointLists.STORE.size();
            String msg = visible ? "Markers on (" + count + " points)" : "Markers off";
            client.inGameHud.setOverlayMessage(Text.literal(msg), false);
        }
    }

    public static void registerRender() {
        if (renderRegistered) return;
        renderRegistered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(context -> {
            if (!visible) return;
            MinecraftClient client = MinecraftClient.getInstance();
            if (client.player == null || context.matrixStack() == null || context.consumers() == null) return;
            Vec3d cam = context.camera().getPos();
            MatrixStack matrices = context.matrixStack();
            VertexConsumerProvider consumers = context.consumers();
            VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());
            for (ControlPoint p : PointLists.STORE.list()) {
                double x = p.easting();
                double y = p.elevation();
                double z = -p.northing();
                if (cam.squaredDistanceTo(x, y, z) > 256 * 256) continue;
                WorldRenderer.drawBox(
                        matrices, lines,
                        x - cam.x - 0.05, y - cam.y, z - cam.z - 0.05,
                        x - cam.x + 0.05, y - cam.y + 1.0, z - cam.z + 0.05,
                        1.0f, 1.0f, 1.0f, 1.0f);
            }
        });
    }

    /** Particle fallback. Used when the render event is unavailable. */
    public static void tick(MinecraftClient client) {
        if (!visible || client.world == null || client.player == null) return;
        Vec3d eye = client.player.getEyePos();
        for (ControlPoint p : PointLists.STORE.list()) {
            double x = p.easting();
            double y = p.elevation();
            double z = -p.northing();
            if (eye.squaredDistanceTo(x, y, z) > 256 * 256) continue;
            for (int i = 0; i < 4; i++) {
                client.world.addParticle(
                        ParticleTypes.END_ROD,
                        true,
                        x, y + 0.2 + i * 0.4, z,
                        0.0, 0.02, 0.0);
            }
        }
    }
}
