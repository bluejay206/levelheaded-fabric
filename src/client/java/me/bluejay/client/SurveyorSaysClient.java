package me.bluejay.client;

import me.bluejay.ModItems;
import me.bluejay.ServerTransitManager;
import me.bluejay.client.hud.RodHud;
import me.bluejay.client.hud.ScopeHud;
import me.bluejay.network.OccupyPosPayload;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.math.BlockPos;

public class SurveyorSaysClient implements ClientModInitializer {

	public static final RodHud rodHud = new RodHud();
	public static final ScopeHud scopeHud = new ScopeHud();

	@Override
	public void onInitializeClient() {
		System.out.println("[SurveyorSays] Client initialization started");

		SurveyorKeybinds.register();

		// Register receiver for occupy position updates from server
		ClientPlayNetworking.registerGlobalReceiver(OccupyPosPayload.ID, (payload, context) -> {
			context.client().execute(() -> ClientTransitCache.setOccupyPos(payload.pos()));
		});

		// Sync occupy position on join (singleplayer)
		ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
			if (client.getServer() != null) {
				BlockPos savedPos = ServerTransitManager.getOccupyPos(client.getServer());
				if (!savedPos.equals(BlockPos.ORIGIN)) {
					ClientTransitCache.setOccupyPos(savedPos);
				}
			}
		});

		HudRenderCallback.EVENT.register((context, tickCounter) -> {
			rodHud.render(context, tickCounter);
			scopeHud.render(context, tickCounter);
		});

		// Right-click tool handling
		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (!world.isClient || hand != Hand.MAIN_HAND) {
				return TypedActionResult.pass(player.getStackInHand(hand));
			}

			if (player.getStackInHand(hand).isOf(ModItems.ROD)) {
				rodHud.requestUpdate();
				return TypedActionResult.success(player.getStackInHand(hand));
			}
			if (player.getStackInHand(hand).isOf(ModItems.SCOPE)) {
				scopeHud.requestUpdate();
				return TypedActionResult.success(player.getStackInHand(hand));
			}

			return TypedActionResult.pass(player.getStackInHand(hand));
		});

		System.out.println("[SurveyorSays] HUD + Keybinds ready (H = toggle HUD | V = save shot)");
		System.out.println("[SurveyorSays] Custom splashes loaded via assets/minecraft/texts/splashes.txt");
	}

	public static void showShotMessage(String type) {
		MinecraftClient client = MinecraftClient.getInstance();
		if (client.player != null) {
			client.player.sendMessage(
					Text.literal("§a✓ " + type + " saved to CSV"),
					true
			);
		}
	}
}