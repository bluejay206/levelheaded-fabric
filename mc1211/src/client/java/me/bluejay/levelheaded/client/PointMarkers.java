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
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Draws a magenta box at each stored point. Toggle with Z.
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
                        x - cam.x + 0.05, y - cam.y + 0.5, z - cam.z + 0.05,
                        1.0f, 0.0f, 1.0f, 1.0f);
            }
        });
    }

    public static void tick(MinecraftClient client) {
        // Particles removed. Overlay only.
    }
}
