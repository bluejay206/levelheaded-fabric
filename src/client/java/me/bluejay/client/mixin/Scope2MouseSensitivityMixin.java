package me.bluejay.client.mixin;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.client.LevelHeadedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public abstract class Scope2MouseSensitivityMixin {

    @Shadow private double cursorDeltaX;
    @Shadow private double cursorDeltaY;

    @Inject(method = "updateMouse(D)V", at = @At("HEAD"))
    private void onUpdateMouse(double timeDelta, CallbackInfo ci) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        if (!mc.player.getMainHandStack().isOf(ModItems.SCOPE2)) return;
        if (!mc.options.useKey.isPressed()) return;

        double zoomFactor = LevelHeadedClient.getScope2ZoomFactor();
        if (zoomFactor <= 1.5) return;

        // Dynamic ramp: stronger reduction at higher zoom (good for 200x surveying)
        // Base 8.0 gives nice feel; quadratic-like drop for precision
        double multiplier = 8.0 / (zoomFactor * zoomFactor * 0.85);
        multiplier = Math.max(multiplier, 0.012); // floor prevents total lock at 200x

        double originalX = this.cursorDeltaX;
        double originalY = this.cursorDeltaY;

        this.cursorDeltaX *= multiplier;
        this.cursorDeltaY *= multiplier;

        // Throttled debug (every 4 ticks)
        if (mc.world.getTime() % 4 == 0) {
            System.out.printf("[LevelHeaded SENSITIVITY] Zoom=%.1fx | Multi=%.4f | RawX=%.5f → %.5f | RawY=%.5f → %.5f%n",
                    zoomFactor, multiplier, originalX, this.cursorDeltaX, originalY, this.cursorDeltaY);
        }
    }
}