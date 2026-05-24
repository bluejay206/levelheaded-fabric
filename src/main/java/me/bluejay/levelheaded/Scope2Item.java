package me.bluejay.levelheaded;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.SpyglassItem;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

public class Scope2Item extends SpyglassItem {

    public Scope2Item(Settings settings) {
        super(settings.maxCount(1));
    }

    @Override
    public TypedActionResult<ItemStack> use(World world, PlayerEntity user, Hand hand) {
        return super.use(world, user, hand);  // Let Zoomify + vanilla handle zoom
    }

    @Override
    public String getTranslationKey() {
        return "item.levelheaded.scope2";
    }

    public int getMaxUseTime(ItemStack stack) {
        return 72000;
    }
}