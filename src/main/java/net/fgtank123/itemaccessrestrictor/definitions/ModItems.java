package net.fgtank123.itemaccessrestrictor.definitions;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import static net.fgtank123.itemaccessrestrictor.ModMain.MOD_ID;

public class ModItems {
    // Create a Deferred Register to hold Items which will all be registered under the "item_access_restrictor" namespace
    public static final DeferredRegister.Items DR = DeferredRegister.createItems(MOD_ID);

    static void registerBlockItem(String name, DeferredBlock<? extends Block> deferredBlock, ResourceKey<CreativeModeTab> tab) {
        DeferredItem<BlockItem> blockItemDeferredItem = ModItems.DR.register(name, () -> new BlockItem(deferredBlock.get(), new Item.Properties()));
        if (tab == null) {
            ModCreativeModTabs.add(blockItemDeferredItem);
        } else {
            ModCreativeModTabs.add(blockItemDeferredItem);
            ModCreativeModTabs.addExternal(tab, blockItemDeferredItem);
        }
    }
}
