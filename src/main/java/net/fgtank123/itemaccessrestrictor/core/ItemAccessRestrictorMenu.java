package net.fgtank123.itemaccessrestrictor.core;

import net.fgtank123.itemaccessrestrictor.definitions.ComparatorOutputMode;
import net.fgtank123.neoforgegui.GuiDataSynchronizationManager;
import net.fgtank123.neoforgegui.ValueRef;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class ItemAccessRestrictorMenu extends AbstractContainerMenu {

    public static final String ID = "item_access_restrictor_menu";

    private final GuiDataSynchronizationManager dataSynchronizationManager;

    private final ValueRef<Boolean> blockingInputIfNotEmpty;
    private final ValueRef<Boolean> blockingInputIfReceivingRedstoneSignal;
    private final ValueRef<Integer> inputStackingLimit;
    private final ValueRef<ComparatorOutputMode> comparatorOutputMode;
    private final ValueRef<Integer> quantityOfRetainedItems;
    private final ValueRef<boolean[]> slotDisables;

    private final ValueRef<List<ItemStack>> facingBlockItemStacks;

    public ItemAccessRestrictorMenu(@Nullable MenuType<?> menuType, int containerId, Inventory playerInventory, ItemAccessRestrictorBlockEntity host) {
        super(menuType, containerId);
        dataSynchronizationManager = new GuiDataSynchronizationManager(
            this,
            playerInventory.player
        );
        blockingInputIfNotEmpty = dataSynchronizationManager.register(
            host.getBlockingInputIfNotEmpty()::getValue,
            host.getBlockingInputIfNotEmpty()::setValue
        );
        blockingInputIfReceivingRedstoneSignal = dataSynchronizationManager.register(
            host.getBlockingInputIfReceivingRedstoneSignal()::getValue,
            host.getBlockingInputIfReceivingRedstoneSignal()::setValue
        );
        inputStackingLimit = dataSynchronizationManager.register(
            host.getInputStackingLimit()::getValue,
            host.getInputStackingLimit()::setValue
        );
        comparatorOutputMode = dataSynchronizationManager.register(
            host.getComparatorOutputMode()::getValue,
            host.getComparatorOutputMode()::setValue
        );
        quantityOfRetainedItems = dataSynchronizationManager.register(
            host.getQuantityOfRetainedItems()::getValue,
            host.getQuantityOfRetainedItems()::setValue
        );
        slotDisables = dataSynchronizationManager.register(
            host.getSlotDisables()::getValue,
            host.getSlotDisables()::setValue
        );
        AtomicReference<List<ItemStack>> clientFacingBlockItemStacksRef = new AtomicReference<>(new ArrayList<>());
        boolean isServerSide = playerInventory.player instanceof ServerPlayer;
        facingBlockItemStacks = dataSynchronizationManager.registerList(
            () -> {
                if (isServerSide) {
                    IItemHandler facingBlockItemHandler = host.getFacingBlockItemHandler();
                    return IntStream.range(0, facingBlockItemHandler.getSlots())
                        .mapToObj(facingBlockItemHandler::getStackInSlot)
                        .map(ItemStack::copy)
                        .collect(Collectors.toList());
                } else {
                    return clientFacingBlockItemStacksRef.get();
                }
            },
            v -> {
                if (!isServerSide) {
                    clientFacingBlockItemStacksRef.set(v);
                }
            },
            ItemStack.OPTIONAL_STREAM_CODEC::encode,
            ItemStack.OPTIONAL_STREAM_CODEC::decode,
            ItemAccessRestrictorMenu::itemStackEquals
        );
    }

    private static boolean itemStackEquals(ItemStack itemStack1, ItemStack itemStack2) {
        return ItemStack.isSameItemSameComponents(itemStack1, itemStack2) && itemStack1.getCount() == itemStack2.getCount();
    }

    @Nonnull
    @Override
    public ItemStack quickMoveStack(@Nonnull Player player, int index) {
        return ItemStack.EMPTY;
    }

    @Override
    public boolean stillValid(@Nonnull Player player) {
        return true;
    }

    @Override
    public void broadcastChanges() {
        super.broadcastChanges();
        dataSynchronizationManager.broadcastChanges();
    }

    public ValueRef<Boolean> getBlockingInputIfNotEmpty() {
        return blockingInputIfNotEmpty;
    }

    public ValueRef<Boolean> getBlockingInputIfReceivingRedstoneSignal() {
        return blockingInputIfReceivingRedstoneSignal;
    }

    public ValueRef<Integer> getInputStackingLimit() {
        return inputStackingLimit;
    }

    public ValueRef<ComparatorOutputMode> getComparatorOutputMode() {
        return comparatorOutputMode;
    }

    public ValueRef<Integer> getQuantityOfRetainedItems() {
        return quantityOfRetainedItems;
    }

    public ValueRef<boolean[]> getSlotDisables() {
        return slotDisables;
    }

    public ValueRef<List<ItemStack>> getFacingBlockItemStacks() {
        return facingBlockItemStacks;
    }
}
