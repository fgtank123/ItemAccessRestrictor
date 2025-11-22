package net.fgtank123.minecraft.itemaccessrestrictor.core;

import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class SimulatePassedItems {
    private ArrayList<ItemStack> itemStacks = new ArrayList<>();
    private long gameTime = -1;

    public void clear() {
        gameTime = -1;
        itemStacks = new ArrayList<>();
    }

    public void add(ItemStack itemStack, long gameTime) {
        if (this.gameTime != gameTime) {
            this.gameTime = gameTime;
            itemStacks.clear();
        }
        ItemStack sameTypeOne = findSameTypeOne(itemStack);
        if (sameTypeOne != null) {
            sameTypeOne.setCount(sameTypeOne.getCount() + itemStack.getCount());
        } else {
            itemStacks.add(itemStack);
        }
    }

    public void remove(ItemStack itemStack, long gameTime) {
        if (this.gameTime != gameTime) {
            this.clear();
        } else {
            ItemStack sameTypeOne = findSameTypeOne(itemStack);
            if (sameTypeOne != null) {
                if (sameTypeOne.getCount() > itemStack.getCount()) {
                    sameTypeOne.setCount(sameTypeOne.getCount() - itemStack.getCount());
                } else {
                    itemStacks.remove(sameTypeOne);
                }
            }
        }
    }

    public boolean contains(ItemStack itemStack, long gameTime) {
        if (this.gameTime != gameTime) {
            return false;
        }
        ItemStack sameTypeOne = findSameTypeOne(itemStack);
        return sameTypeOne != null && sameTypeOne.getCount() >= itemStack.getCount();
    }

    public boolean isEmpty() {
        for (ItemStack itemStack : this.itemStacks) {
            if (itemStack.getCount() > 0) {
                return false;
            }
        }
        return true;
    }

    private ItemStack findSameTypeOne(ItemStack itemStack) {
        for (ItemStack stack : itemStacks) {
            if (ItemStack.isSameItemSameComponents(stack, itemStack)) {
                return stack;
            }
        }
        return null;
    }


    @SuppressWarnings("unused")
    public long getGameTime() {
        return gameTime;
    }


}
