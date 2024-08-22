package smartin.cubiccarts.block;

import com.mojang.serialization.MapCodec;
import net.minecraft.block.*;
import net.minecraft.block.enums.RailShape;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.*;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class CopperRail extends AbstractRailBlock {
    public static final MapCodec<PoweredRailBlock> CODEC = createCodec(PoweredRailBlock::new);
    public static final EnumProperty<RailShape> SHAPE = Properties.STRAIGHT_RAIL_SHAPE;
    public static final IntProperty POWERED = Properties.POWER;

    public MapCodec<PoweredRailBlock> getCodec() {
        return CODEC;
    }

    public CopperRail(Settings settings) {
        super(true, settings);
        this.setDefaultState(this.stateManager.getDefaultState().with(SHAPE, RailShape.NORTH_SOUTH).with(POWERED, 0).with(WATERLOGGED, false));
    }

    protected int isPoweredByOtherRails(World world, BlockPos pos, BlockState state, boolean bl, int distance) {
        if (distance >= 8) {
            return 0;
        } else {
            int i = pos.getX();
            int j = pos.getY();
            int k = pos.getZ();
            boolean bl2 = true;
            RailShape railShape = state.get(SHAPE);
            switch (railShape) {
                case NORTH_SOUTH:
                    if (bl) {
                        ++k;
                    } else {
                        --k;
                    }
                    break;
                case EAST_WEST:
                    if (bl) {
                        --i;
                    } else {
                        ++i;
                    }
                    break;
                case ASCENDING_EAST:
                    if (bl) {
                        --i;
                    } else {
                        ++i;
                        ++j;
                        bl2 = false;
                    }

                    railShape = RailShape.EAST_WEST;
                    break;
                case ASCENDING_WEST:
                    if (bl) {
                        --i;
                        ++j;
                        bl2 = false;
                    } else {
                        ++i;
                    }

                    railShape = RailShape.EAST_WEST;
                    break;
                case ASCENDING_NORTH:
                    if (bl) {
                        ++k;
                    } else {
                        --k;
                        ++j;
                        bl2 = false;
                    }

                    railShape = RailShape.NORTH_SOUTH;
                    break;
                case ASCENDING_SOUTH:
                    if (bl) {
                        ++k;
                        ++j;
                        bl2 = false;
                    } else {
                        --k;
                    }

                    railShape = RailShape.NORTH_SOUTH;
            }

            int level = this.isPoweredByOtherRails(world, new BlockPos(i, j, k), bl, distance, railShape);
            if (level > 0) {
                return level;
            } else {
                if (bl2) {
                    return this.isPoweredByOtherRails(world, new BlockPos(i, j - 1, k), bl, distance, railShape);
                }
            }
        }
        return 0;
    }

    protected int isPoweredByOtherRails(World world, BlockPos pos, boolean bl, int distance, RailShape shape) {
        BlockState blockState = world.getBlockState(pos);
        if (!blockState.isOf(this)) {
            return 0;
        } else {
            RailShape railShape = blockState.get(SHAPE);
            if (shape == RailShape.EAST_WEST && (railShape == RailShape.NORTH_SOUTH || railShape == RailShape.ASCENDING_NORTH || railShape == RailShape.ASCENDING_SOUTH)) {
                return 0;
            } else if (shape == RailShape.NORTH_SOUTH && (railShape == RailShape.EAST_WEST || railShape == RailShape.ASCENDING_EAST || railShape == RailShape.ASCENDING_WEST)) {
                return 0;
            } else if (blockState.get(POWERED) > 0) {
                return Math.max(world.getReceivedRedstonePower(pos), this.isPoweredByOtherRails(world, pos, blockState, bl, distance + 1));
            } else {
                return 0;
            }
        }
    }

    protected void updateBlockState(BlockState state, World world, BlockPos pos, Block neighbor) {
        int currentLevel = state.get(POWERED);
        int updateLevel = Math.max(
                world.getReceivedRedstonePower(pos), Math.max(
                        this.isPoweredByOtherRails(world, pos, state, true, 0),
                        this.isPoweredByOtherRails(world, pos, state, false, 0)));
        if (currentLevel != updateLevel) {
            world.setBlockState(pos, state.with(POWERED, updateLevel), 3);
            world.updateNeighborsAlways(pos.down(), this);
            if (state.get(SHAPE).isAscending()) {
                world.updateNeighborsAlways(pos.up(), this);
            }
        }

    }

    public Property<RailShape> getShapeProperty() {
        return SHAPE;
    }

    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        switch (rotation) {
            case CLOCKWISE_180:
                switch (state.get(SHAPE)) {
                    case ASCENDING_EAST -> {
                        return state.with(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case ASCENDING_WEST -> {
                        return state.with(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case ASCENDING_NORTH -> {
                        return state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_SOUTH -> {
                        return state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case SOUTH_EAST -> {
                        return state.with(SHAPE, RailShape.NORTH_WEST);
                    }
                    case SOUTH_WEST -> {
                        return state.with(SHAPE, RailShape.NORTH_EAST);
                    }
                    case NORTH_WEST -> {
                        return state.with(SHAPE, RailShape.SOUTH_EAST);
                    }
                    case NORTH_EAST -> {
                        return state.with(SHAPE, RailShape.SOUTH_WEST);
                    }
                }
                ;
            case COUNTERCLOCKWISE_90:
                switch ((RailShape) state.get(SHAPE)) {
                    case NORTH_SOUTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.EAST_WEST);
                    }
                    case EAST_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_SOUTH);
                    }
                    case ASCENDING_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case ASCENDING_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_NORTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case ASCENDING_SOUTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case SOUTH_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    }
                    case SOUTH_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    }
                    case NORTH_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case NORTH_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                    }
                }
            case CLOCKWISE_90:
                switch ((RailShape) state.get(SHAPE)) {
                    case NORTH_SOUTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.EAST_WEST);
                    }
                    case EAST_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_SOUTH);
                    }
                    case ASCENDING_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case ASCENDING_NORTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    }
                    case ASCENDING_SOUTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    }
                    case SOUTH_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case SOUTH_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                    }
                    case NORTH_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    }
                    case NORTH_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    }
                }
            default:
                return state;
        }
    }

    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        RailShape railShape = (RailShape) state.get(SHAPE);
        switch (mirror) {
            case LEFT_RIGHT:
                switch (railShape) {
                    case ASCENDING_NORTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_SOUTH);
                    }
                    case ASCENDING_SOUTH -> {
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_NORTH);
                    }
                    case SOUTH_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    }
                    case SOUTH_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                    }
                    case NORTH_WEST -> {
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    }
                    case NORTH_EAST -> {
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    }
                    default -> {
                        return super.mirror(state, mirror);
                    }
                }
            case FRONT_BACK:
                switch (railShape) {
                    case ASCENDING_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_WEST);
                    case ASCENDING_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.ASCENDING_EAST);
                    case ASCENDING_NORTH:
                    case ASCENDING_SOUTH:
                    default:
                        break;
                    case SOUTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_WEST);
                    case SOUTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.SOUTH_EAST);
                    case NORTH_WEST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_EAST);
                    case NORTH_EAST:
                        return (BlockState) state.with(SHAPE, RailShape.NORTH_WEST);
                }
        }

        return super.mirror(state, mirror);
    }

    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(new Property[]{SHAPE, POWERED, WATERLOGGED});
    }
}
