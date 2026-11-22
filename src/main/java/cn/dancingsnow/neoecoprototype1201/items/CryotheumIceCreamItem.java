package cn.dancingsnow.neoecoprototype1201.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

/** Drinkable food. 1.20.1 has no FoodProperties.usingConvertsTo, so return the bottle here. */
public final class CryotheumIceCreamItem extends Item {
    public static final int FREEZE_TICKS = 80;

    public CryotheumIceCreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        // Item's food path awards the criterion/stat, applies nutrition/effects and consumes one item.
        boolean returnBottle = !(entity instanceof Player player) || !player.getAbilities().instabuild;
        ItemStack remainder = super.finishUsingItem(stack, level, entity);
        if (!level.isClientSide) {
            entity.setTicksFrozen(Math.max(entity.getTicksFrozen(), FREEZE_TICKS));
        }
        if (returnBottle) {
            ItemStack bottle = new ItemStack(Items.GLASS_BOTTLE);
            if (remainder.isEmpty()) {
                return bottle;
            }
            if (entity instanceof Player player && !player.getInventory().add(bottle)) {
                player.drop(bottle, false);
            }
        }
        return remainder;
    }
}
