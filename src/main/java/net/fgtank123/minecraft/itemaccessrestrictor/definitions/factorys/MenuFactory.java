package net.fgtank123.minecraft.itemaccessrestrictor.definitions.factorys;

import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;

@FunctionalInterface
public interface MenuFactory<M extends AbstractContainerMenu, B extends BlockEntity> {
    M create(MenuType<M> menuType, int containerId, Inventory playInventory, B blockEntity);
}
