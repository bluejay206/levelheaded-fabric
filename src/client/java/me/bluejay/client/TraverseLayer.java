package me.bluejay.client;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/**
 * TraverseLayer temporarily disabled to restore a working build.
 * The live red polyline feature will be re-added once the build is stable.
 */
@Environment(EnvType.CLIENT)
public class TraverseLayer {

    private static final List<Vec3d> traversePoints = new ArrayList<>();
    private static boolean enabled = true;

    public static void register() {
        // WorldRenderEvents.AFTER_TRANSLUCENT.register(TraverseLayer::render);  // Commented out
    }

    public static void addPoint(Vec3d point) {
        // if (point != null) traversePoints.add(point);
    }

    public static void clear() {
        traversePoints.clear();
    }

    public static void toggle() {
        enabled = !enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }

    // private static void render(WorldRenderContext context) { ... }  // Fully disabled
}