package cn.dancingsnow.neoecoprototype1201.block.crafting;

import appeng.block.crafting.AbstractCraftingUnitBlock;
import appeng.blockentity.crafting.CraftingBlockEntity;
import cn.dancingsnow.neoecoprototype1201.blockentity.decoration.NamedDollBlockEntity;
import cn.dancingsnow.neoecoprototype1201.event.PlushieScare;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/**
 * A plushie that is also a whole crafting CPU on its own: it carries the bytes and the threads itself,
 * answers to the CPU list under its own name, and hands its channels out of its bottom face only.
 *
 * <p>Its block entity is AE2's own {@link CraftingBlockEntity}, and it has to be exactly that class: the
 * grid keys its machine table by the runtime class of the block entity, so a subclass of ours would never
 * be found by the service that collects CPUs. Everything that makes this block a plushie rather than a
 * crafting unit - the one-way face, the refuse to join an AE2 storage wall, the name - is therefore paid
 * for next to that class, in the two mixins over it and in the ticker below.</p>
 *
 * <p>Unlike its 1.21.1 twin this block needs no capability registration to reach the network. That line
 * posts {@code AECapabilities.IN_WORLD_GRID_NODE_HOST} for the block entity type, because on NeoForge 21
 * that is the only way {@code GridHelper} finds a host; on AE2 15.4.10 the lookup tries
 * {@code blockEntity instanceof IInWorldGridNodeHost} first (measured on that jar: {@code getNodeHost}
 * offsets 0-19, and only its fallback at offset 25 reads the Forge capability), and
 * {@code CraftingBlockEntity} already is one through {@code AENetworkBlockEntity ->
 * IGridConnectedBlockEntity}. The node itself comes from AE2's own constructor, which sets
 * {@code setInWorldNode(true)} and then asks the sides question our mixin answers.</p>
 *
 * <p>Neither {@code newBlockEntity} nor {@code getTicker} is written here: on this line 1.20.1's
 * {@code BlockBehaviour.Properties} has no {@code blockEntity()}, so
 * {@link cn.dancingsnow.neoecoprototype1201.registration.ModBlockEntities} hands the finished type and the
 * ticker to {@code AEBaseEntityBlock#setBlockEntity} after construction, which is what those two methods
 * then read - the same way AE2 wires its own crafting unit.</p>
 */
public class DollCraftingUnitBlock extends AbstractCraftingUnitBlock<CraftingBlockEntity> {
    /** The plushie's face: the same property the other dolls turn on. */
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public DollCraftingUnitBlock(BlockBehaviour.Properties properties, DollCraftingUnitType type) {
        super(properties, type);
        registerDefaultState(defaultBlockState().setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    /** Set down looking at whoever set it down, like the other dolls. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    /**
     * The plushie turns on its face. Public, not protected: 1.20.1 declares {@code rotate} and
     * {@code mirror} public on {@code BlockBehaviour} while 1.21.1 declares them protected, so the parent
     * line's shape would not compile here - the same narrowing trap
     * {@link cn.dancingsnow.neoecoprototype1201.block.decoration.NamedDollBlock} already steps around.
     */
    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    /**
     * The plushie half of this block's work, on the CPU's own ticks: it keeps repelling creepers from the
     * spot it sits in, and it keeps the name it shows in the CPU list. Both are re-asserted rather than
     * written once because the block entity is not ours to give a constructor hook - a doll placed by a
     * piston, a structure or a test would otherwise sit unnamed, and the picker would fall back to the
     * pile-of-units name.
     *
     * <p>AE2 names a cluster only from a member that answers a custom name, and {@code setName} both stores
     * that name and asks the cluster to recompose it. It takes a {@link String}, so the CPU name cannot be a
     * translation key - the same letters show in every language, while the block's own name in the tooltip
     * and the HUD stays translated.</p>
     */
    public static void tick(Level level, BlockPos pos, BlockState state, CraftingBlockEntity cpu) {
        if (cpu.getCustomName() == null) {
            cpu.setName(((DollCraftingUnitBlock) state.getBlock()).cpuName());
        }
        if (PlushieScare.sweepDue(level.getGameTime())) {
            NamedDollBlockEntity.scareAround(level, pos);
        }
    }

    /** The name this plushie takes in the CPU list. */
    public String cpuName() {
        return ((DollCraftingUnitType) this.type).cpuName();
    }
}
