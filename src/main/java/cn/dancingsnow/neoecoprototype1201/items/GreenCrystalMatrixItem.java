package cn.dancingsnow.neoecoprototype1201.items;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * The L1 core material, whose name wears the family's green - the same colour the 1.21.1 line gives it.
 *
 * <p>On 1.20.1 that has to be said here rather than at registration: {@code Item.Properties} has no
 * {@code name} entry yet (the data-component form is 1.21.1's), and a lang string cannot carry a colour
 * because formatting marks in lang values are not parsed.</p>
 */
public class GreenCrystalMatrixItem extends Item {
    /** Sampled off the artist's own green band, the number the 1.21.1 line uses for this family. */
    private static final int NAME_THEME_GREEN = 0xFF94EDAB;

    public GreenCrystalMatrixItem(Properties properties) {
        super(properties);
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.translatable(getDescriptionId())
                .withStyle(style -> style.withColor(NAME_THEME_GREEN));
    }
}
