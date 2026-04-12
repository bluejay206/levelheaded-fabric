package me.bluejay.client;

import me.bluejay.ModItems;
import me.bluejay.client.hud.RodHud;
import me.bluejay.client.hud.ScopeHud;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.item.Item;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class RodScopeHandler {

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (world.isClient && hand == Hand.MAIN_HAND) {
                Item held = player.getStackInHand(hand).getItem();

                if (held == ModItems.ROD || held == ModItems.SCOPE) {
                    // Toggle between LIVE and SHOT mode
                    if (held == ModItems.ROD) {
                        RodHud rodHud = new RodHud(); // We need a better way - see note below
                        rodHud.requestUpdate();
                    } else {
                        ScopeHud scopeHud = new ScopeHud();
                        scopeHud.requestUpdate();
                    }
                    return TypedActionResult.success(player.getStackInHand(hand));
                }
            }
            return TypedActionResult.pass(player.getStackInHand(hand));
        });
    }
}