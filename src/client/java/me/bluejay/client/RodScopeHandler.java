package me.bluejay.client;

import me.bluejay.levelheaded.ModItems;
import me.bluejay.client.hud.SurveyorHud;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.util.TypedActionResult;
import net.minecraft.util.Hand;
import net.minecraft.world.World;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;

public class RodScopeHandler {

    public static void register() {
        UseItemCallback.EVENT.register((player, world, hand) -> {
            ItemStack stack = player.getStackInHand(hand);

            boolean isSurveyTool = stack.isOf(ModItems.ROD) || stack.isOf(ModItems.SCOPE2);

            if (isSurveyTool && hand == Hand.MAIN_HAND) {
                SurveyorHud.toggleMode();
                return TypedActionResult.success(stack);
            }

            return TypedActionResult.pass(stack);
        });

        System.out.println("[LevelHeaded] RodScopeHandler registered (ROD + SCOPE2)");
    }
}