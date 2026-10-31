package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import cn.dancingsnow.neoecoprototype1201.integration.ae2.OversizeInterfaceLogic;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Energized superconductive interface: AE2's interface with one marker able to stock 8192 instead of a
 * stack, and a passive 4000 AE/t feed into the grid.
 *
 * <p>The marker count stays at AE2's nine because this line opens AE2's own interface GUI; widening the
 * count without widening the panel would hide the extra slots.</p>
 */
public class SimplifySuperconductiveInterfaceBlockEntity extends InterfaceBlockEntity
        implements IPassiveEnergyGenerator {
    public static final double GENERATION_RATE = 4000.0D;

    private boolean suppressed;

    public SimplifySuperconductiveInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        var logic = super.createLogic();
        OversizeInterfaceLogic.install(logic);
        return logic;
    }

    @Override
    public double getRate() {
        return suppressed ? 0.0D : GENERATION_RATE;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }
}
