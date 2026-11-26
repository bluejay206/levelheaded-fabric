package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.core.data.ControlPoint;
import net.minecraft.client.MinecraftClient;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec3d;

/**
 * Spawns end-rod particles at each stored point. Toggle with Z.
 * Northing is stored as -z, so world z is -northing.
 */
public final class PointMarkers {

    private static boolean visible;

    private PointMarkers() {}

    public static boolean visible() { return visible; }

    public static void toggle() {
        visible = !visible;
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null && client.inGameHud != null) {
            int count = PointLists.STORE.size();
            String msg = visible
                    ? "Markers on (" + count + " points)"
                    : "Markers off";
            client.inGameHud.setOverlayMessage(Text.literal(msg), false);
        }
    }

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
