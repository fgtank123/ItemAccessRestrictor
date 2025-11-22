package net.fgtank123.minecraft.itemaccessrestrictor;

import com.mojang.logging.LogUtils;
import net.fgtank123.minecraft.itemaccessrestrictor.core.ItemAccessRestrictorBlockEntity;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.*;
import net.fgtank123.minecraft.guidatasync.GuiDataSynchronizationPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(ModMain.MOD_ID)
@SuppressWarnings("unused")
public class ModMain {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "item_access_restrictor";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    public static ResourceLocation makeId(String id) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, id);
    }

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public ModMain(IEventBus modEventBus, ModContainer modContainer) {
        ModBlocks.DR.register(modEventBus);
        ModItems.DR.register(modEventBus);
        ModBlockEntities.DR.register(modEventBus);
        ModCreativeModTabs.DR.register(modEventBus);
        ModMenus.DR.register(modEventBus);

        // Register the commonSetup method for mod loading
        modEventBus.addListener(this::commonSetup);

        modEventBus.addListener(this::onRegisterCapabilities);
        modEventBus.addListener(this::onRegisterPayloadHandlers);


        // Register items to external creative tabs
        modEventBus.addListener(ModCreativeModTabs::initExternal);

        NeoForge.EVENT_BUS.addListener(this::onServerStarting);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        // modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        // Some common setup code
        // LOGGER.info("HELLO FROM COMMON SETUP");
    }

    public void onServerStarting(ServerStartingEvent event) {
        // Do something when the server starts
        // LOGGER.info("HELLO from server starting");
    }

    public void onRegisterCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(
            Capabilities.ItemHandler.BLOCK,
            ModBlockEntities.ITEM_ACCESS_RESTRICTOR.get(),
            ItemAccessRestrictorBlockEntity::getItemHandler
        );
    }

    public void onRegisterPayloadHandlers(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar(MOD_ID);
        GuiDataSynchronizationPacketPayload.register(registrar, MOD_ID);
    }

}
