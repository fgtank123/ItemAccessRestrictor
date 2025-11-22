package net.fgtank123.minecraft.itemaccessrestrictor.definitions;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import net.fgtank123.minecraft.itemaccessrestrictor.ModMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.ArrayList;
import java.util.List;

import static net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModBlocks.ITEM_ACCESS_RESTRICTOR;

public class ModCreativeModTabs {
    // Create a Deferred Register to hold CreativeModeTabs which will all be registered under the "item_access_restrictor" namespace
    public static final DeferredRegister<CreativeModeTab> DR = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, ModMain.MOD_ID);

    // Creates a creative tab with the id "item_access_restrictor:item_access_restrictor_tab" for the example item, that is placed after the combat tab
    @SuppressWarnings("unused")
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> CREATIVE_MODE_TAB = DR.register(
        "creative_tab",
        () -> CreativeModeTab.builder()
            .title(Component.translatable("gui.item_access_restrictor.creative_tab"))
            .icon(() -> ITEM_ACCESS_RESTRICTOR.get().asItem().getDefaultInstance())
            .displayItems(ModCreativeModTabs::buildDisplayItems)
            .build()
    );

    private static final Multimap<ResourceKey<CreativeModeTab>, DeferredItem<?>> EXTERNAL_DEFERRED_ITEMS = HashMultimap
        .create();
    private static final List<DeferredItem<?>> DEFERRED_ITEMS = new ArrayList<>();

    public static void initExternal(BuildCreativeModeTabContentsEvent contents) {
        for (var itemDefinition : EXTERNAL_DEFERRED_ITEMS.get(contents.getTabKey())) {
            if (itemDefinition != null) {
                contents.accept(itemDefinition);
            }
        }
    }

    public static void add(DeferredItem<?> itemDef) {
        DEFERRED_ITEMS.add(itemDef);
    }

    public static void addExternal(ResourceKey<CreativeModeTab> tab, DeferredItem<?> itemDef) {
        EXTERNAL_DEFERRED_ITEMS.put(tab, itemDef);
    }

    private static void buildDisplayItems(
        @SuppressWarnings("unused") CreativeModeTab.ItemDisplayParameters itemDisplayParameters,
        CreativeModeTab.Output output
    ) {
        for (var itemDef : DEFERRED_ITEMS) {
            output.accept(itemDef);
        }
    }
}
