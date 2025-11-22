package net.fgtank123.itemaccessrestrictor.definitions;

import com.google.common.base.Preconditions;
import net.fgtank123.itemaccessrestrictor.ModMain;
import net.fgtank123.itemaccessrestrictor.core.ItemAccessRestrictorBlock;
import net.fgtank123.itemaccessrestrictor.core.ItemAccessRestrictorBlockEntity;
import net.fgtank123.itemaccessrestrictor.definitions.factorys.BlockEntityFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;

public class ModBlockEntities {

    public static final DeferredRegister<BlockEntityType<?>> DR = DeferredRegister.create(
        Registries.BLOCK_ENTITY_TYPE,
        ModMain.MOD_ID
    );

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<ItemAccessRestrictorBlockEntity>> ITEM_ACCESS_RESTRICTOR = registerBlockEntityType(
        ItemAccessRestrictorBlock.ID,
        ItemAccessRestrictorBlockEntity::new,
        ModBlocks.ITEM_ACCESS_RESTRICTOR
    );

    @SafeVarargs
    public static <T extends BlockEntity> DeferredHolder<BlockEntityType<?>, BlockEntityType<T>> registerBlockEntityType(
        String shortId,
        BlockEntityFactory<T> factory,
        DeferredBlock<Block>... blockDefinitions
    ) {
        Preconditions.checkArgument(blockDefinitions.length > 0);
        return DR.register(
            shortId,
            () -> {
                AtomicReference<BlockEntityType<T>> typeHolder = new AtomicReference<>();
                var blocks = Arrays.stream(blockDefinitions)
                    .map(DeferredHolder::get)
                    .toArray(Block[]::new);
                // noinspection DataFlowIssue
                var type = BlockEntityType.Builder.of(
                    (blockPos, blockState) -> factory.create(
                        typeHolder.get(),
                        blockPos,
                        blockState
                    ),
                    blocks
                ).build(null);
                typeHolder.setPlain(type);
                return type;
            }
        );
    }

}

