package me.bluejay.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.client.hud.RodHud;
import me.bluejay.client.hud.ScopeHud;
import me.bluejay.client.SurveyorKeybinds;
import me.bluejay.client.ClientTransitCache;
import me.bluejay.client.hud.SurveyorHud;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.MathHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import me.bluejay.levelheaded.network.OccupyPosPayload;

public class LevelHeadedClient implements ClientModInitializer {

    public static final double ZOOM_MIN   = 4.0;
    public static final double ZOOM_MAX   = 100.0;
    public static final double ZOOM_STEP  = 4.0;
    public static final double ZOOM_SPEED = 0.35;

    public static double scope2ZoomTarget = 8.0;
    public static double scope2ZoomCurrent = 8.0;

    // Made public so SurveyorKeybinds can reliably access the singletons (as in b002)
    public static final RodHud rodHud = new RodHud();
    public static final ScopeHud scopeHud = new ScopeHud();

    @Override

    public void onInitializeClient() {
        // === NEW: Register payload receiver ===
        ClientPlayNetworking.registerGlobalReceiver(OccupyPosPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientTransitCache.setOccupyPos(payload.pos());
                System.out.println("[LevelHeaded] Payload received → Occupy set to " + payload.pos());
            });
        });

        // Register keybinds
        SurveyorKeybinds.register();

        HudRenderCallback.EVENT.register((context, tickCounter) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;

            if (rodHud.isHoldingToolPublic(mc.player)) {
                rodHud.render(context, tickCounter);
            } else if (scopeHud.isHoldingToolPublic(mc.player)) {
                scopeHud.render(context, tickCounter);
            }

            // Scope2 overlay when right-click held
            if (mc.player.getMainHandStack().isOf(ModItems.SCOPE2)
                    && mc.options.useKey.isPressed()) {
                renderScope2Overlay(context, mc);
            }
        });

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            // Smooth lerp for zoom
            if (Math.abs(scope2ZoomCurrent - scope2ZoomTarget) > 0.01) {
                scope2ZoomCurrent = MathHelper.lerp(ZOOM_SPEED, scope2ZoomCurrent, scope2ZoomTarget);
            } else {
                scope2ZoomCurrent = scope2ZoomTarget;
            }

            // Force HUD update every tick while Scope2 is actively zooming (for finer angle steps)
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player != null
                    && mc.player.getMainHandStack().isOf(ModItems.SCOPE2)
                    && mc.options.useKey.isPressed()) {
                rodHud.requestUpdate();
                scopeHud.requestUpdate();
            }
        });

        ClientPlayNetworking.registerGlobalReceiver(OccupyPosPayload.ID, (payload, context) -> {
            context.client().execute(() -> {
                ClientTransitCache.setOccupyPos(payload.pos());
            });
        });

        System.out.println("[LevelHeaded] Client initialized – Scope2 200x zoom + per-frame HUD math during zoom");
    }

    public static double getScope2ZoomFactor() {
        return scope2ZoomCurrent;
    }

    public static void setScope2ZoomTarget(double target) {
        scope2ZoomTarget = MathHelper.clamp(target, ZOOM_MIN, ZOOM_MAX);
    }

    private static void renderScope2Overlay(DrawContext context, MinecraftClient mc) {
        int w = mc.getWindow().getScaledWidth();
        int h = mc.getWindow().getScaledHeight();
        context.fill(0, 0, w, h, 0x20000000);  // 25% dark overlay
    }
}