package cn.dancingsnow.neoecoprototype1201.integration.jade.provider;

import cn.dancingsnow.neoecoprototype1201.api.MachineMood;
import cn.dancingsnow.neoecoprototype1201.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype1201.blockentity.crafting.SimplifyCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype1201.integration.jade.SimplifyJadePlugin;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

/**
 * The F1 crafting host's mind line: which face it is wearing, and what the grid charges it while idle.
 * The line is absent on every host without a face, so the fifteen out of sixteen that never grew one keep
 * their tooltip exactly as it was.
 *
 * <p>The tier itself needs no server data - {@code mind_tier} is a block state property and the client
 * already has it. Only the wattage has to come from the server, because it is a live figure on the grid
 * node and the client's copy of that node is not what answers the question.</p>
 */
public enum SimplifyCraftingHostProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof SimplifyCraftingSystemBlockEntity host
                && accessor.getBlockState().getValue(SimplifyCraftingSystemBlock.HAS_MIND)) {
            tag.putDouble("mindIdlePower", host.idlePowerDraw());
        }
    }

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        var state = accessor.getBlockState();
        CompoundTag data = accessor.getServerData();
        if (!state.hasProperty(SimplifyCraftingSystemBlock.HAS_MIND)
                || !state.getValue(SimplifyCraftingSystemBlock.HAS_MIND)
                || !data.contains("mindIdlePower")) {
            return;
        }
        // Root locale, one decimal: the figure is a rate, and a locale-dependent separator would make the
        // same machine read differently to two players on the same server.
        String watts = String.format(java.util.Locale.ROOT, "%.1f", data.getDouble("mindIdlePower"));
        // The word and the figure come out of the same ladder on purpose: a face saying it is fine while
        // the number says it is not is the one contradiction a player can see without opening anything.
        tooltip.add(Component.translatable("jade.neoecoprototype1201.machine_mind",
                Component.translatable(MachineMood.wordKey(
                        state.getValue(SimplifyCraftingSystemBlock.MIND_TIER))), watts)
                .withStyle(ChatFormatting.GRAY));
    }

    @Override
    public ResourceLocation getUid() {
        return SimplifyJadePlugin.id("simplify_crafting_system");
    }
}
