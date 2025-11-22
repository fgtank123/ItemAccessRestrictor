package net.fgtank123.minecraft.itemaccessrestrictor.definitions;

import net.fgtank123.minecraft.itemaccessrestrictor.ModMain;
import net.fgtank123.minecraft.itemaccessrestrictor.core.ItemAccessRestrictorMenu;
import net.fgtank123.minecraft.itemaccessrestrictor.definitions.factorys.MenuFactory;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.concurrent.atomic.AtomicReference;

public class ModMenus {
    public static final DeferredRegister<MenuType<?>> DR = DeferredRegister.create(
        Registries.MENU,
        ModMain.MOD_ID
    );

    public static final DeferredHolder<MenuType<?>, MenuType<ItemAccessRestrictorMenu>> ITEM_ACCESS_RESTRICTOR_MENU = registerMenuType(
        ItemAccessRestrictorMenu.ID,
        ItemAccessRestrictorMenu::new
    );

    @SuppressWarnings({"SameParameterValue", "unchecked"})
    private static <M extends AbstractContainerMenu, B extends BlockEntity> DeferredHolder<MenuType<?>, MenuType<M>> registerMenuType(String name, MenuFactory<M, B> factory) {
        return DR.register(name, () -> {
            AtomicReference<MenuType<M>> typeHolder = new AtomicReference<>();
            //noinspection resource
            MenuType<M> menuType = IMenuTypeExtension.create(
                (windowId, inv, data) -> factory.create(
                    typeHolder.get(),
                    windowId,
                    inv,
                    (B) inv.player.level().getBlockEntity(data.readBlockPos())
                )
            );
            typeHolder.setPlain(menuType);
            return menuType;
        });
    }
}
