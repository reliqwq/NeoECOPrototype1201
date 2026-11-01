package cn.dancingsnow.neoecoprototype1201.items;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.ForgeMod;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * 猪咪存储矩阵外壳：带攻击伤害、剑速与 +2 实体交互距离的彩蛋物品。
 *
 * <p>1.20.1 has no {@code Item.Properties#attributes}, so the modifiers go through
 * {@code getDefaultAttributeModifiers(EquipmentSlot)} - the hook vanilla's own sword uses. Mainhand only,
 * which is what that slot argument is for; the 1.21.1 line says the same with {@code EquipmentSlotGroup.MAINHAND}.
 *
 * <p>Two things do not come across unchanged. The reach attribute is Forge's here: vanilla 1.20.1 has no
 * {@code Attributes.ENTITY_INTERACTION_RANGE}, and the binary patch adds {@code ForgeMod.ENTITY_REACH} to
 * {@code Player.createAttributes()}, so the same +2 lands on the same player attribute. Sweep does not:
 * 1.21.1 had to grant {@code ItemAbilities.SWORD_SWEEP} because NeoForge moved the test off
 * {@code instanceof SwordItem}, while Forge 1.20.1 still tests the class and has no ability query. A real
 * SwordItem would sweep, and would also carry durability and a tool tier the 1.21.1 item does not have.
 */
public class PigmeeStorageMatrixHousingItem extends Item {

    public PigmeeStorageMatrixHousingItem(Properties properties) {
        super(properties);
    }

    @Override
    public Multimap<Attribute, AttributeModifier> getDefaultAttributeModifiers(EquipmentSlot slot) {
        return slot == EquipmentSlot.MAINHAND ? Modifiers.MAINHAND : super.getDefaultAttributeModifiers(slot);
    }

    /** Built on first use: {@code ForgeMod.ENTITY_REACH} is another mod's registry object, and item
     * registration is not ordered after the attribute registry closes. */
    private static final class Modifiers {
        private static final Multimap<Attribute, AttributeModifier> MAINHAND =
                ImmutableMultimap.<Attribute, AttributeModifier>builder()
                        .put(Attributes.ATTACK_DAMAGE, new AttributeModifier(
                                Item.BASE_ATTACK_DAMAGE_UUID, "base_attack_damage",
                                5.6D, AttributeModifier.Operation.ADDITION))
                        .put(Attributes.ATTACK_SPEED, new AttributeModifier(
                                Item.BASE_ATTACK_SPEED_UUID, "base_attack_speed",
                                -2.4D, AttributeModifier.Operation.ADDITION))
                        .put(ForgeMod.ENTITY_REACH.get(), new AttributeModifier(
                                UUID.nameUUIDFromBytes("pigmee_reach".getBytes(StandardCharsets.UTF_8)),
                                "pigmee_reach", 2.0D, AttributeModifier.Operation.ADDITION))
                        .build();
    }
}
