package net.fgtank123.minecraft.itemaccessrestrictor.core;

import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModBlockEntities;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModBlockStatesProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class ItemAccessRestrictorBlock extends Block implements EntityBlock {
    public static final String ID = "item_access_restrictor";
    public static final DirectionProperty FACING = BlockStateProperties.FACING;
    public static final BooleanProperty BLOCKING = ModBlockStatesProperties.BLOCKING;

    public ItemAccessRestrictorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
            this.stateDefinition.any()
                .setValue(FACING, Direction.DOWN)
                .setValue(BLOCKING, false)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, BLOCKING);
    }

    public ItemAccessRestrictorBlock() {
        this(
            Properties.of()
                .strength(1f)
                .isRedstoneConductor((state, blockGetter, pos) -> false)
                .isSuffocating((state, level, pos) -> false)
                .isViewBlocking((state, level, pos) -> false)
                .noOcclusion()
                .sound(SoundType.COPPER)
        );
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Direction direction = context.getClickedFace().getOpposite();
        return this.defaultBlockState()
            .setValue(FACING, direction);
    }

    @Override
    public BlockEntity newBlockEntity(@Nonnull BlockPos pos, @Nonnull BlockState state) {
        return ModBlockEntities.ITEM_ACCESS_RESTRICTOR.get().create(pos, state);
    }

    @Override
    protected boolean hasAnalogOutputSignal(@Nonnull BlockState state) {
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@Nonnull BlockState blockState, @Nonnull Level level, @Nonnull BlockPos pos) {
        if (level.getBlockEntity(pos) instanceof ItemAccessRestrictorBlockEntity limiterBlockEntity) {
            return limiterBlockEntity.getAnalogRedstoneSignal();
        } else {
            return 0;
        }
    }

    @Nonnull
    @Override
    protected InteractionResult useWithoutItem(@Nonnull BlockState state, @Nonnull Level level, @Nonnull BlockPos pos, @Nonnull Player player, @Nonnull BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) instanceof ItemAccessRestrictorBlockEntity blockEntity) {
                player.openMenu(blockEntity, pos);
            }
            return InteractionResult.CONSUME;
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(@Nonnull Level level, @Nonnull BlockState state, @Nonnull BlockEntityType<T> blockEntityType) {
        return (level1, pos, state1, blockEntity) -> {
            if (blockEntity instanceof ItemAccessRestrictorBlockEntity itemAccessRestrictorBlockEntity) {
                itemAccessRestrictorBlockEntity.tick(level1, pos, state1);
            }
        };
    }
}
