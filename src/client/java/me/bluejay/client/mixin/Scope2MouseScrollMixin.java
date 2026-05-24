package me.bluejay.client.mixin;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.client.LevelHeadedClient;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.Mouse;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Mouse.class)
public class Scope2MouseScrollMixin {

    @Inject(method = "onMouseScroll", at = @At("HEAD"), cancellable = true)
    private void onMouseScroll(long window, double horizontal, double vertical, CallbackInfo ci) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        // Only intercept when Scope2 is held + right-click active
        if (!client.player.getMainHandStack().isOf(ModItems.SCOPE2)) return;
        if (!client.options.useKey.isPressed()) return;

        if (vertical != 0) {
            // REVERSED direction: scroll up now zooms IN (as you had before)
            double newTarget = LevelHeadedClient.scope2ZoomTarget + (vertical * LevelHeadedClient.ZOOM_STEP);

            LevelHeadedClient.setScope2ZoomTarget(newTarget);

            System.out.printf("[LevelHeaded] Wheel zoom → target %.1fx (current %.1fx)%n",
                    LevelHeadedClient.scope2ZoomTarget, LevelHeadedClient.scope2ZoomCurrent);

            // Cancel vanilla behavior (prevents hotbar switching)
            ci.cancel();
        }
    }
}