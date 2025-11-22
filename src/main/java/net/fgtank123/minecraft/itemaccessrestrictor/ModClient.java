package net.fgtank123.minecraft.itemaccessrestrictor;

import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.ItemAccessRestrictorScreen;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.ModMenus;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

// This class will not load on dedicated servers. Accessing client side code from here is safe.
@Mod(value = ModMain.MOD_ID, dist = Dist.CLIENT)
// You can use EventBusSubscriber to automatically register all static methods in the class annotated with @SubscribeEvent
@EventBusSubscriber(modid = ModMain.MOD_ID, value = Dist.CLIENT)
public class ModClient {

    public ModClient(IEventBus modEventBus, @SuppressWarnings("unused") ModContainer container) {
        // Allows NeoForge to create a config screen for this mod's configs.
        // The config screen is accessed by going to the Mods screen > clicking on your mod > clicking on config.
        // Do not forget to add translations for your config options to the en_us.json file.
        // container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        modEventBus.addListener(this::onRegisterMenuScreens);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        // Some client setup code
    }

    public void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.ITEM_ACCESS_RESTRICTOR_MENU.get(), ItemAccessRestrictorScreen::new);
    }
}
