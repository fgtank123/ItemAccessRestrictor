package net.fgtank123.minecraft.itemaccessrestrictor.core;

import net.fgtank123.minecraft.itemaccessrestrictor.ModMain;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ComparatorOutputMode;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModBlockStatesProperties;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModMenus;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModNBTSettingDefinitions;
import net.fgtank123.minecraft.nbtsetting.NBTSetting;
import net.fgtank123.minecraft.nbtsetting.NBTSettingsManager;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.Nameable;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.capabilities.BlockCapabilityCache;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.wrapper.EmptyItemHandler;
import org.apache.commons.lang3.ArrayUtils;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;


public class ItemAccessRestrictorBlockEntity extends BlockEntity implements MenuProvider, Nameable {
    private final AtomicReference<BlockCapabilityCache<IItemHandler, Direction>> facingBlockCapabilityCache = new AtomicReference<>(null);

    @Nullable
    private Component name;

    private final NBTSettingsManager nbtSettingsManager = new NBTSettingsManager();

    private final NBTSetting<Boolean> blockingInputIfNotEmpty;
    private final NBTSetting<Boolean> blockingInputIfReceivingRedstoneSignal;
    private final NBTSetting<Integer> inputStackingLimit;
    private final NBTSetting<ComparatorOutputMode> comparatorOutputMode;
    private final NBTSetting<Integer> numberOfItemsRetained;
    private final NBTSetting<boolean[]> slotDisables;
    int lastAnalogRedstoneSignal;

    /**
     * 用于支持多物品同时输入，基于ae2在输入多种物品前，会提前将每种物品都模拟输入一遍。同一游戏刻内，凡是模拟输入并通过的物品在后续实际输入时可豁免阻挡
     */
    private final SimulatePassedItems simulatePassedItemsUsedByBlocking = new SimulatePassedItems();

    public ItemAccessRestrictorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        blockingInputIfNotEmpty = nbtSettingsManager.register(ModNBTSettingDefinitions.BLOCKING_INPUT_IF_NOT_EMPTY);
        blockingInputIfReceivingRedstoneSignal = nbtSettingsManager.register(ModNBTSettingDefinitions.BLOCKING_INPUT_IF_RECEIVING_REDSTONE_SIGNAL);
        inputStackingLimit = nbtSettingsManager.register(ModNBTSettingDefinitions.INPUT_STACKING_LIMIT);
        comparatorOutputMode = nbtSettingsManager.register(ModNBTSettingDefinitions.COMPARATOR_OUTPUT_MODE);
        numberOfItemsRetained = nbtSettingsManager.register(ModNBTSettingDefinitions.NUMBER_OF_ITEMS_RETAINED);
        slotDisables = nbtSettingsManager.register(ModNBTSettingDefinitions.SLOT_DISABLES);
        nbtSettingsManager.onValueChanged(
            (definition, newValue, oldValue) -> setChanged()
        );
    }

    public void tick(Level level, BlockPos pos, BlockState state) {
        if (level != null && !level.isClientSide()) {
            boolean[] oldSlotDisables = slotDisables.getValue();
            int oldSlotDisableLength = oldSlotDisables.length;
            if (oldSlotDisableLength > 0 && innerGetFacingBlockItemHandler().getSlots() != oldSlotDisableLength) {
                simulatePassedItemsUsedByBlocking.clear();
                slotDisables.setValue(new boolean[0]);
            }
            checkUpdateBlockingState();
            int lastAnalogRedstoneSignal = this.lastAnalogRedstoneSignal;
            if (lastAnalogRedstoneSignal != getAnalogRedstoneSignal()) {
                level.updateNeighbourForOutputSignal(pos, state.getBlock());
            }
        }
    }

    @Override
    protected void loadAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ModMain.LOGGER.warn(tag.getString("test_asd"));
        if (tag.contains("CustomName", 8)) {
            this.name = parseCustomNameSafe(tag.getString("CustomName"), registries);
        }
        nbtSettingsManager.loadFromNBT(tag);
    }

    @Override
    protected void saveAdditional(@Nonnull CompoundTag tag, @Nonnull HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (this.name != null) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name, registries));
        }
        nbtSettingsManager.saveToNBT(tag);
    }

    @Nonnull
    @Override
    public Component getName() {
        return this.name != null ? this.name : this.getDefaultName();
    }

    protected Component getDefaultName() {
        return Component.translatable("block." + ModMain.MOD_ID + "." + ItemAccessRestrictorBlock.ID);
    }

    private static final IItemHandler INNER_EMPTY_ITEM_HANDLER = new EmptyItemHandler();

    public Direction getFacing() {
        return getFacing(getBlockState());
    }

    public static Direction getFacing(BlockState blockState) {
        return blockState.getValue(BlockStateProperties.FACING);
    }

    private IItemHandler innerGetFacingBlockItemHandler() {
        if (level instanceof ServerLevel serverLevel) {
            if (facingBlockCapabilityCache.get() == null) {
                Direction facing = getFacing();
                BlockPos facingBlockPos = getBlockPos().relative(facing);
                Direction facingBlockFacing = facing.getOpposite();
                facingBlockCapabilityCache.compareAndSet(
                    null,
                    BlockCapabilityCache.create(
                        Capabilities.ItemHandler.BLOCK,
                        serverLevel,
                        facingBlockPos,
                        facingBlockFacing
                    )
                );
            }
            BlockCapabilityCache<IItemHandler, Direction> capabilityCache = facingBlockCapabilityCache.get();
            return Optional.ofNullable(
                capabilityCache.getCapability()
            ).orElseGet(EmptyItemHandler::new);
        } else {
            return INNER_EMPTY_ITEM_HANDLER;
        }
    }

    public IItemHandler getFacingBlockItemHandler() {
        boolean[] slotDisablesValue = slotDisables.getValue();
        if (slotDisablesValue.length > 0 && !ArrayUtils.contains(slotDisablesValue, false)) {
            return innerGetFacingBlockItemHandler();
        } else {
            if (getItemHandler().getSlots() == 0) {
                return INNER_EMPTY_ITEM_HANDLER;
            } else {
                return innerGetFacingBlockItemHandler();
            }
        }
    }

    private final static ThreadLocal<Map<String, Boolean>> isInnerRunning = ThreadLocal.withInitial(HashMap::new);

    /**
     * 通过封装调用，禁用递归操作，从而禁止嵌套使用
     */
    private <R> R wrapExecuting(String runningKey, Supplier<R> onFirst, Supplier<R> onNotFirstOrNotServer) {
        Map<String, Boolean> isInnerRunningMap = isInnerRunning.get();
        if (level == null || level instanceof ClientLevel || isInnerRunningMap.getOrDefault(runningKey, false)) {
            return onNotFirstOrNotServer.get();
        }
        isInnerRunningMap.put(runningKey, true);
        R r = onFirst.get();
        isInnerRunningMap.put(runningKey, false);
        return r;
    }

    private int getRealSlotIndex(int realSlots, int slot) {
        boolean[] slotDisables = this.slotDisables.getValue();
        if (slotDisables.length == 0) {
            return slot;
        } else {
            int newSlotIndex = -1;
            for (int i = 0; i < realSlots; i++) {
                boolean slotDisable = i < slotDisables.length && slotDisables[i];
                if (!slotDisable) {
                    newSlotIndex++;
                    if (newSlotIndex == slot) {
                        return i;
                    }
                }
            }
            return -1;
        }
    }


    @Nonnull
    private ItemStack innerGetStackInSlot(int slot) {
        int numberOfItemsRetained = this.numberOfItemsRetained.getValue();
        IItemHandler itemHandler = innerGetFacingBlockItemHandler();
        int realSlotIndex = getRealSlotIndex(itemHandler.getSlots(), slot);
        ItemStack stackInSlot = itemHandler.getStackInSlot(realSlotIndex);
        if (stackInSlot.getCount() <= numberOfItemsRetained) {
            return ItemStack.EMPTY;
        } else {
            ItemStack copy = stackInSlot.copy();
            copy.split(numberOfItemsRetained);
            return copy;
        }
    }

    private boolean isAllAllowSlotsEmpty(int slots) {
        boolean isAllAllowSlotsEmpty = true;
        for (int i = 0; i < slots; i++) {
            if (!innerGetStackInSlot(i).isEmpty()) {
                isAllAllowSlotsEmpty = false;
                break;
            }
        }
        return isAllAllowSlotsEmpty;
    }

    public IItemHandler getItemHandler(Direction fromSide) {
        if (getFacing() == fromSide) {
            return INNER_EMPTY_ITEM_HANDLER;
        } else {
            return getItemHandler();
        }
    }

    public IItemHandler getItemHandler() {
        return new IItemHandler() {
            @Override
            public int getSlots() {
                return wrapExecuting(
                    "getSlots",
                    this::innerGetSlots,
                    INNER_EMPTY_ITEM_HANDLER::getSlots
                );
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {
                return wrapExecuting(
                    "getStackInSlot",
                    () -> innerGetStackInSlot(slot),
                    () -> INNER_EMPTY_ITEM_HANDLER.getStackInSlot(slot)
                );
            }

            private boolean isAllAllowSlotsEmpty() {
                return ItemAccessRestrictorBlockEntity.this.isAllAllowSlotsEmpty(this.getSlots());
            }

            private int innerGetSlots() {
                boolean[] slotDisables = ItemAccessRestrictorBlockEntity.this.slotDisables.getValue();
                int slots = innerGetFacingBlockItemHandler().getSlots();
                if (slotDisables.length == 0) {
                    return slots;
                } else {
                    int disableSlotCount = 0;
                    for (boolean slotDisable : slotDisables) {
                        if (slotDisable) {
                            disableSlotCount++;
                        }
                    }
                    return slots - disableSlotCount;
                }
            }

            @Nonnull
            private ItemStack innerInsertItem(int realSlotIndex, @Nonnull ItemStack stack, boolean simulate, IItemHandler itemHandler) {
                int inputStackingLimit = ItemAccessRestrictorBlockEntity.this.inputStackingLimit.getValue();
                if (inputStackingLimit > 0) {
                    ItemStack stackInSlot = itemHandler.getStackInSlot(realSlotIndex);
                    if (ItemStack.isSameItemSameComponents(stack, stackInSlot)) {
                        int slotLimit = itemHandler.getSlotLimit(realSlotIndex);
                        if (inputStackingLimit < slotLimit) {
                            int oldCount = stackInSlot.getCount();
                            if (oldCount >= inputStackingLimit) {
                                return stack;
                            } else if (oldCount + stack.getCount() > inputStackingLimit) {
                                int allowInsertCount = inputStackingLimit - oldCount;
                                int preRemainingCount = stack.getCount() - allowInsertCount;
                                ItemStack remainingItemStack = itemHandler.insertItem(
                                    realSlotIndex,
                                    stack.copyWithCount(allowInsertCount),
                                    simulate
                                );
                                return stack.copyWithCount(preRemainingCount + remainingItemStack.getCount());
                            }
                        }
                    }
                }
                return itemHandler.insertItem(realSlotIndex, stack, simulate);
            }

            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                return wrapExecuting(
                    "insertItem",
                    () -> {
                        Objects.requireNonNull(level);
                        if (blockingInputIfReceivingRedstoneSignal.getValue() && level.hasNeighborSignal(getBlockPos())) {
                            return stack;
                        }
                        IItemHandler itemHandler = innerGetFacingBlockItemHandler();
                        int realSlotIndex = getRealSlotIndex(itemHandler.getSlots(), slot);
                        boolean blockingInputIfNotEmpty = ItemAccessRestrictorBlockEntity.this.blockingInputIfNotEmpty.getValue();
                        boolean simulateBlockingPassed = false;
                        boolean blockingPassedCauseBeforeSimulate = false;
                        if (blockingInputIfNotEmpty) {
                            boolean allAllowSlotsEmpty = this.isAllAllowSlotsEmpty();
                            if (simulate) {
                                if (!allAllowSlotsEmpty) {
                                    return stack;
                                } else {
                                    simulateBlockingPassed = true;
                                }
                            } else {
                                if (simulatePassedItemsUsedByBlocking.contains(stack, level.getGameTime())) {
                                    blockingPassedCauseBeforeSimulate = true;
                                }
                                if (!allAllowSlotsEmpty && !blockingPassedCauseBeforeSimulate) {
                                    if (!simulatePassedItemsUsedByBlocking.isEmpty()) {
                                        simulatePassedItemsUsedByBlocking.clear();
                                    }
                                    return stack;
                                }
                            }
                        }

                        ItemStack remainedItem = innerInsertItem(realSlotIndex, stack, simulate, itemHandler);
                        if (blockingInputIfNotEmpty) {
                            if (simulateBlockingPassed) {
                                if (remainedItem.getCount() < stack.getCount()) {
                                    simulatePassedItemsUsedByBlocking.add(
                                        stack.copyWithCount(stack.getCount() - remainedItem.getCount()),
                                        level.getGameTime()
                                    );
                                }
                            } else if (blockingPassedCauseBeforeSimulate) {
                                if (remainedItem.getCount() != stack.getCount()) {
                                    simulatePassedItemsUsedByBlocking.remove(stack.copyWithCount(stack.getCount() - remainedItem.getCount()), level.getGameTime());
                                    if (simulatePassedItemsUsedByBlocking.isEmpty()) {
                                        simulatePassedItemsUsedByBlocking.clear();
                                    } else if (slot >= this.innerGetSlots() - 1) {
                                        simulatePassedItemsUsedByBlocking.clear();
                                    }
                                } else {
                                    if (slot >= this.innerGetSlots() - 1) {
                                        simulatePassedItemsUsedByBlocking.clear();
                                    }
                                }
                            }
                        } else {
                            if (!simulatePassedItemsUsedByBlocking.isEmpty()) {
                                simulatePassedItemsUsedByBlocking.clear();
                            }
                        }
                        return remainedItem;
                    },
                    () -> INNER_EMPTY_ITEM_HANDLER.insertItem(slot, stack, simulate)
                );
            }

            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return wrapExecuting(
                    "extractItem",
                    () -> {
                        IItemHandler itemHandler = innerGetFacingBlockItemHandler();
                        int numberOfItemsRetained = ItemAccessRestrictorBlockEntity.this.numberOfItemsRetained.getValue();
                        int realSlotIndex = getRealSlotIndex(itemHandler.getSlots(), slot);
                        int count = itemHandler.getStackInSlot(realSlotIndex).getCount();
                        if (count <= numberOfItemsRetained) {
                            return ItemStack.EMPTY;
                        } else {
                            int newAmount = count - numberOfItemsRetained;
                            return itemHandler.extractItem(realSlotIndex, Math.min(newAmount, amount), simulate);
                        }
                    },
                    () -> INNER_EMPTY_ITEM_HANDLER.extractItem(slot, amount, simulate)
                );
            }

            @Override
            public int getSlotLimit(int slot) {
                return wrapExecuting(
                    "getSlotLimit",
                    () -> {
                        IItemHandler itemHandler = innerGetFacingBlockItemHandler();
                        int inputStackingLimit = ItemAccessRestrictorBlockEntity.this.inputStackingLimit.getValue();
                        int numberOfItemsRetained = ItemAccessRestrictorBlockEntity.this.numberOfItemsRetained.getValue();
                        int realSlotIndex = getRealSlotIndex(itemHandler.getSlots(), slot);
                        ItemStack stackInSlot = itemHandler.getStackInSlot(realSlotIndex);
                        int slotLimit = Math.min(itemHandler.getSlotLimit(realSlotIndex), stackInSlot.getMaxStackSize());
                        if (inputStackingLimit > 0 && inputStackingLimit < slotLimit) {
                            slotLimit = inputStackingLimit;
                        }
                        int itemCount = stackInSlot.getCount();
                        if (itemCount <= numberOfItemsRetained) {
                            return Math.max(slotLimit - itemCount, 0);
                        } else {
                            return Math.max(slotLimit - numberOfItemsRetained, 0);
                        }
                    },
                    () -> INNER_EMPTY_ITEM_HANDLER.getSlotLimit(slot)
                );
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return wrapExecuting(
                    "isItemValid",
                    () -> {
                        IItemHandler itemHandler = innerGetFacingBlockItemHandler();
                        return itemHandler.isItemValid(slot, stack);
                    },
                    () -> INNER_EMPTY_ITEM_HANDLER.isItemValid(slot, stack)
                );
            }
        };
    }


    public boolean isBlocking() {
        if (level instanceof ServerLevel serverLevel) {
            int slots = this.getItemHandler().getSlots();
            if (slots > 0) {
                // 如果配置了接收到红石信号时阻挡，且当前正在接收红石信号时，则阻挡
                if (blockingInputIfReceivingRedstoneSignal.getValue() && serverLevel.hasNeighborSignal(getBlockPos())) {
                    return true;
                }
                // 如果配置了不为空时阻挡，且当前不为空，则阻挡
                // noinspection RedundantIfStatement
                if (this.blockingInputIfNotEmpty.getValue() && !isAllAllowSlotsEmpty(slots)) {
                    return true;
                }
            }

        }
        return false;
    }

    public int getAnalogRedstoneSignal() {
        lastAnalogRedstoneSignal = getAnalogRedstoneSignalInner();
        return lastAnalogRedstoneSignal;
    }

    public int getAnalogRedstoneSignalInner() {
        if (level instanceof ServerLevel) {
            switch (comparatorOutputMode.getValue()) {
                case SAME_WITH_FACING_BLOCK -> {
                    // 设置与朝向方块相同时返回朝向方块的模拟信号
                    BlockState blockState = getBlockState();
                    BlockPos blockPos = getBlockPos();
                    Direction facingDirection = ItemAccessRestrictorBlockEntity.getFacing(blockState);
                    BlockPos facingBlockPos = blockPos.relative(facingDirection);
                    BlockState facingBlockState = level.getBlockState(facingBlockPos);
                    if (facingBlockState.hasAnalogOutputSignal()) {
                        return facingBlockState.getAnalogOutputSignal(level, facingBlockPos);
                    } else {
                        return 0;
                    }
                }
                case ONLY_COUNT_EFFECTIVE_ITEMS_AND_SLOTS -> {
                    return getRedstoneSignalFromItemHandler(this.getItemHandler());
                }
                default -> throw new IllegalStateException("Unexpected value: " + comparatorOutputMode.getValue());
            }
        } else {
            return 0;
        }
    }

    public static int getRedstoneSignalFromItemHandler(@Nonnull IItemHandler itemHandler) {
        float f = 0.0F;
        int disabled = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack itemstack = itemHandler.getStackInSlot(i);
            if (!itemstack.isEmpty()) {
                f += (float) itemstack.getCount() / (float) itemHandler.getSlotLimit(i);
            }/* else if (!itemHandler.isItemValid(i, ItemStack.EMPTY)) {
                disabled++;
            }*/
        }
        f /= (float) (itemHandler.getSlots() - disabled);
        return Mth.lerpDiscrete(Math.min(f, 1F), 0, 15);
    }

    protected void checkUpdateBlockingState() {
        BlockPos pos = getBlockPos();
        if (getLevel() instanceof ServerLevel serverLevel && serverLevel.getBlockEntity(pos) instanceof ItemAccessRestrictorBlockEntity blockEntity) {
            BlockState state = getBlockState();
            if (blockEntity.isBlocking() != state.getValue(ModBlockStatesProperties.BLOCKING)) {
                serverLevel.setBlock(pos, state.cycle(ModBlockStatesProperties.BLOCKING), 2);
            }
        }
    }

    @Nonnull
    @Override
    public Component getDisplayName() {
        return this.getName();
    }

    public NBTSetting<Boolean> getBlockingInputIfNotEmpty() {
        return blockingInputIfNotEmpty;
    }

    public NBTSetting<Boolean> getBlockingInputIfReceivingRedstoneSignal() {
        return blockingInputIfReceivingRedstoneSignal;
    }

    public NBTSetting<Integer> getInputStackingLimit() {
        return inputStackingLimit;
    }

    public NBTSetting<ComparatorOutputMode> getComparatorOutputMode() {
        return comparatorOutputMode;
    }

    public NBTSetting<Integer> getNumberOfItemsRetained() {
        return numberOfItemsRetained;
    }

    public NBTSetting<boolean[]> getSlotDisables() {
        return slotDisables;
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int containerId, @Nonnull Inventory playerInventory, @Nonnull Player player) {
        return new ItemAccessRestrictorMenu(
            ModMenus.ITEM_ACCESS_RESTRICTOR_MENU.get(),
            containerId,
            playerInventory,
            this
        );
    }
}
