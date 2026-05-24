package me.bluejay.client.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import me.bluejay.client.LevelHeadedClient;

@Mixin(GameRenderer.class)
public abstract class Scope2FovMixin {

    @ModifyReturnValue(method = "getFov(Lnet/minecraft/client/render/Camera;FZ)D", at = @At("RETURN"))
    private double applyScope2Zoom(double originalFov) {
        MinecraftClient client = MinecraftClient.getInstance();
        PlayerEntity player = client.player;

        if (player == null || client.world == null) {
            return originalFov;
        }

        // Direct Scope2 detection
        boolean isHoldingScope2 = false;
        try {
            var stack = player.getMainHandStack();
            if (!stack.isEmpty()) {
                String name = stack.getItem().getClass().getSimpleName();
                if (name.contains("Scope2Item") || "Scope2Item".equals(name)) {
                    isHoldingScope2 = true;
                }
            }
        } catch (Exception ignored) {}

        if (isHoldingScope2 && client.options.useKey.isPressed()) {
            double factor = LevelHeadedClient.getScope2ZoomFactor();
            double zoomedFov = originalFov / factor;

            // Debug every 40 ticks
            if (client.world.getTime() % 40 == 0) {
                System.out.println("[LevelHeaded] === SCOPE2 ZOOM ACTIVE === Factor=" + factor + "x | Original=" + originalFov + " → Zoomed=" + zoomedFov);
            }

            return zoomedFov;
        }

        return originalFov;
    }
}