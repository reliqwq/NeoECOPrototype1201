package cn.dancingsnow.neoecoprototype1201.items;

import cn.dancingsnow.neoecoprototype1201.Prototype1201;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Block item for the four dolls of honoured players, plus the giant plushie. Every one of them can be worn
 * on the head: night vision and the creepers keeping their distance come from
 * {@link cn.dancingsnow.neoecoprototype1201.event.FumoEquipmentEffects}, and each doll carries its own
 * armour and its own list of worn effects as a {@link DollStats} handed over where it is registered.
 *
 * <p>The face is in the block's own model, so nothing here resolves a player skin and no custom renderer is
 * involved except the giant plushie's, which scales that same model up rather than drawing a new one.</p>
 *
 * <p>Three things the 1.21.1 line carries as stack data have to live on the item here, because 1.20.1 has
 * neither {@code DataComponents.ITEM_NAME} nor a glint override: the coloured name goes through
 * {@link #getName(ItemStack)}, the shine through {@link #isFoil(ItemStack)}, and the armour through
 * {@code IForgeItem#getAttributeModifiers(EquipmentSlot, ItemStack)} - which is the hook the equipment scan
 * actually reaches on this generation ({@code ItemStack#getAttributeModifiers} ends in it, read from the
 * patched bytecode). The modifier instances are built once per item, never per call: the scan removes the
 * old ones by id before adding, and a fresh id every sweep would let armour stack without limit.</p>
 */
public class FumoItem extends BlockItem implements Equipable {
    /**
     * One effect a worn doll hands out, at the level it is handed out at.
     *
     * <p>The level lives here rather than at the site that applies it because the tooltip and the
     * hand-out have to agree: {@code FumoEquipmentEffects} builds its {@code MobEffectInstance} straight
     * from this record, and the line below quotes the same number.</p>
     */
    public record DollEffect(MobEffect effect, int amplifier) {
        private static final String[] NUMERALS = {"", "II", "III", "IV", "V"};

        /** The effect's own name with vanilla's roman numeral on it - none for the first level, as vanilla. */
        public Component describe() {
            var name = effect.getDisplayName().copy();
            if (amplifier <= 0) {
                return name;
            }
            return name.append(" ")
                    .append(amplifier < NUMERALS.length ? NUMERALS[amplifier] : Integer.toString(amplifier + 1));
        }
    }

    /**
     * What the doll grants while worn: its own armour, the effects on top of the shared night vision, and
     * the AE/t it is authored to push into chargeable things in the wearer's pockets - zero for the dolls
     * that do not charge anything, which is most of them. On this line that rate is a constant, not a
     * server key: the 1.20.1 tree ships no config system at all.
     */
    public record DollStats(float armor, float toughness, List<DollEffect> extraEffects, double chargeAEPerTick) {
    }

    private static final String ARMOR_NAME = Prototype1201.MOD_ID + ":fumo_armor";
    private static final String TOUGHNESS_NAME = Prototype1201.MOD_ID + ":fumo_armor_toughness";

    /** What the doll carries on whoever wears it, given at registration and never null. */
    private final DollStats wornStats;
    private final Multimap<Attribute, AttributeModifier> wornModifiers;
    @Nullable
    private final Integer nameColor;
    private final boolean shiny;

    public FumoItem(Block block, Properties properties, DollStats wornStats) {
        this(block, properties, wornStats, null, false);
    }

    public FumoItem(Block block, Properties properties, DollStats wornStats,
                    @Nullable Integer nameColor, boolean shiny) {
        super(block, properties);
        this.wornStats = java.util.Objects.requireNonNull(wornStats, "a doll needs its worn stats");
        this.nameColor = nameColor;
        this.shiny = shiny;
        // Built once, and immutable: the equipment scan is handed this map to read, and the modifier ids
        // have to be the same ones it removed last time or armour would stack on every rescan.
        this.wornModifiers = ImmutableMultimap.<Attribute, AttributeModifier>builder()
                .put(Attributes.ARMOR,
                        new AttributeModifier(ARMOR_NAME, wornStats.armor(), AttributeModifier.Operation.ADDITION))
                .put(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(TOUGHNESS_NAME, wornStats.toughness(),
                                AttributeModifier.Operation.ADDITION))
                .build();
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /** What a particular doll grants on top of the night vision every doll gives, empty for most of them. */
    public static List<DollEffect> extraEffects(ItemStack stack) {
        DollStats stats = statsOf(stack);
        return stats == null ? List.of() : stats.extraEffects();
    }

    /** Null for anything that is not one of our dolls. */
    @Nullable
    public static DollStats statsOf(ItemStack stack) {
        return stack.getItem() instanceof FumoItem item ? item.wornStats : null;
    }

    /** The dolls double as light armour, each with its own values, and only on the head. */
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        return slot == EquipmentSlot.HEAD ? wornModifiers : ImmutableMultimap.of();
    }

    /**
     * A coloured default name, which on 1.21.1 is an {@code ITEM_NAME} component. Overriding the name here
     * keeps the same property that component has: it is not an anvil rename, so the tooltip stays upright
     * and the stack is not counted as renamed.
     */
    @Override
    public Component getName(ItemStack stack) {
        if (nameColor == null) {
            return super.getName(stack);
        }
        return Component.translatable(this.getBlock().getDescriptionId())
                .withStyle(Style.EMPTY.withColor(nameColor));
    }

    @Override
    public boolean isFoil(ItemStack stack) {
        return shiny || super.isFoil(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        DollStats stats = statsOf(stack);
        if (stats == null) return;
        // What these lines promise is exactly what FumoEquipmentEffects#onPlayerTick grants.
        tooltip.add(Component.translatable("block." + Prototype1201.MOD_ID + ".fumo_reliqwq.worn_hint")
                .withStyle(ChatFormatting.GRAY));
        for (DollEffect extra : stats.extraEffects()) {
            // Named after the effect itself, so a future DollStats entry needs no new lang key and the
            // line cannot drift from what FumoEquipmentEffects actually applies - the level comes from the
            // same number that is handed to the MobEffectInstance.
            tooltip.add(Component.translatable("block." + Prototype1201.MOD_ID + ".fumo_reliqwq.worn_extra",
                            extra.describe())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stats.chargeAEPerTick() > 0.0D) {
            // No number on this line either: the rate is authored, and quoting it would be quoting a
            // constant the player can do nothing about - the line says what happens, not what it costs.
            tooltip.add(Component.translatable("block." + Prototype1201.MOD_ID + ".fumo_reliqwq.worn_charge")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}
