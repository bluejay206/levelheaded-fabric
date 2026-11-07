package me.bluejay.levelheaded.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * FOV zoom mixin — currently DISABLED for Minecraft 26.2.
 *
 * Runtime failure on 26.2: @ModifyVariable on getFov does not match.
 * 1.21.1 used MixinExtras @ModifyReturnValue with descriptor
 * getFov(Lnet/minecraft/client/Camera;FZ)D (or F in later versions).
 *
 * 26.2 is unobfuscated; method signature / existence needs re-verification
 * against current GameRenderer (possible rename, package, or return type change).
 *
 * Client mixins list is empty so the game launches. Re-enable after fixing
 * the injection target (prefer @ModifyReturnValue if MixinExtras is available,
 * or a correct @ModifyVariable / @Inject on the real getFov / calculateFov).
 *
 * ScopeZoom still runs (keybind + hold Scope) but has no effect until this
 * mixin is restored.
 *
 * Previous implementation:
 *   @ModifyVariable(method = "getFov", at = @At("RETURN"), ordinal = 0)
 *   private double levelheaded$zoom(double fov) {
 *       return fov * ScopeZoom.getFovMultiplier();
 *   }
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
}