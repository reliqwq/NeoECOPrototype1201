package cn.dancingsnow.neoecoprototype1201.blockentity.crafting;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.blockentity.misc.InterfaceBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * L1 powered ME interface: AE2's own interface, its nine markers and its own GUI, plus a passive
 * 200 AE/t feed into the grid.
 *
 * <p>Same two node calls the 1.21.1 line makes: register the generator as a grid service and declare no
 * idle draw. AE2's {@code InterfaceBlockEntity.createMainNode()} builds the node through
 * {@code GridHelper.createManagedNode} and never sets an idle figure itself, so the zero is a statement of
 * intent - a machine that pays the network should not also be billed by it - rather than an override of a
 * measured default.</p>
 */
public class SimplifyPoweredMEInterfaceBlockEntity extends InterfaceBlockEntity
        implements IPassiveEnergyGenerator {
    public static final double GENERATION_RATE = 200.0D;

    private boolean suppressed;

    public SimplifyPoweredMEInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
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
