package net.fgtank123.itemaccessrestrictor.definitions;

import net.fgtank123.itemaccessrestrictor.core.ItemAccessRestrictorBlock;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

import static net.fgtank123.itemaccessrestrictor.ModMain.MOD_ID;

public class ModBlocks {
    // Create a Deferred Register to hold Blocks which will all be registered under the "item_access_restrictor" namespace
    public static final DeferredRegister.Blocks DR = DeferredRegister.createBlocks(MOD_ID);
    // Creates a new Block with the id "item_access_restrictor:item_access_restrictor", combining the namespace and path
    public static final DeferredBlock<Block> ITEM_ACCESS_RESTRICTOR = registerBlock(
        ItemAccessRestrictorBlock.ID,
        ItemAccessRestrictorBlock::new,
        CreativeModeTabs.FUNCTIONAL_BLOCKS
    );

    static <B extends Block> DeferredBlock<B> registerBlock(String name, Supplier<B> blockSupplier, ResourceKey<CreativeModeTab> tab) {
        DeferredBlock<B> deferredBlock = DR.register(name, blockSupplier);
        ModItems.registerBlockItem(name, deferredBlock, tab);
        return deferredBlock;
    }

    @SuppressWarnings("unused")
    static <B extends Block> DeferredBlock<B> registerBlock(String name, Supplier<B> blockSupplier) {
        return registerBlock(name, blockSupplier, null);
    }
}
