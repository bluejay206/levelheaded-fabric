package me.bluejay.levelheaded.client.mixin;

import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;

/**
 * FOV zoom mixin — currently DISABLED for Minecraft 26.2.
 *
 * Runtime failure on 26.2: @ModifyVariable on getFov does not match.
 * Client mixins list is empty so the game launches.
 * Scope is the vanilla spyglass, so zoom comes from vanilla isScoping().
 */
@Mixin(GameRenderer.class)
public class GameRendererMixin {
}
