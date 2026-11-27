package me.bluejay.levelheaded.client;

import me.bluejay.levelheaded.PointLists;
import me.bluejay.levelheaded.core.data.ControlPoint;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.world.phys.Vec3;

public final class PointMarkers {

    private static boolean visible;

    private PointMarkers() {}

    public static boolean visible() { return visible; }

    public static void toggle() {
        visible = !visible;
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.gui == null) return;
        int count = PointLists.STORE.size();
        String msg = visible ? "LevelHeaded markers on (" + count + " points)" : "LevelHeaded markers off";
        client.gui.setOverlayMessage(Component.literal(msg), false);
    }

    public static void tick(Minecraft client) {
        if (!visible || client.level == null || client.player == null) return;
        Vec3 eye = client.player.getEyePosition();
        for (ControlPoint p : PointLists.STORE.list()) {
            double x = p.easting();
            double y = p.elevation();
            double z = -p.northing();
            if (eye.distanceToSqr(x, y, z) > 256 * 256) continue;
            for (int i = 0; i < 5; i++) {
                client.level.addParticle(
                        new DustParticleOptions(0xFFFF00FF, 1.2f),
                        true, true,
                        x, y + 0.1 * i, z, 0.0, 0.0, 0.0);
            }
        }
    }
}
