package net.fgtank123.itemaccessrestrictor.core.gui;

import com.mojang.blaze3d.platform.InputConstants;
import net.fgtank123.itemaccessrestrictor.core.ItemAccessRestrictorMenu;
import net.fgtank123.itemaccessrestrictor.core.gui.widgets.SettingSlider;
import net.fgtank123.itemaccessrestrictor.core.gui.widgets.SlotDisableSettingPanel;
import net.fgtank123.itemaccessrestrictor.core.gui.widgets.TextureButton;
import net.fgtank123.itemaccessrestrictor.definitions.ComparatorOutputMode;
import net.fgtank123.itemaccessrestrictor.definitions.ModNBTSettingDefinitions;
import net.fgtank123.neoforgegui.ValueRef;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Renderable;
import net.minecraft.client.gui.layouts.LayoutElement;
import net.minecraft.client.gui.layouts.LinearLayout;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.MenuAccess;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;

import static net.fgtank123.itemaccessrestrictor.core.gui.ModTextureEnum.*;

@OnlyIn(Dist.CLIENT)
public class ItemAccessRestrictorScreen extends Screen implements MenuAccess<ItemAccessRestrictorMenu> {

    private static final int LABEL_COLOR = 0x404040;

    private final ItemAccessRestrictorMenu menu;
    private final int titleLabelX;
    private final int titleLabelY;

    private LinearLayout mainLayout;
    private SlotDisableSettingPanel slotDisableSettingPanel;
    private boolean preFacingBlockIsEmpty;

    public ItemAccessRestrictorScreen(ItemAccessRestrictorMenu menu, @SuppressWarnings("unused") Inventory playerInventory, Component title) {
        super(title);
        this.menu = menu;
        this.titleLabelX = 8;
        this.titleLabelY = 6;
    }

    private TextureButton blockingInputIfNotEmptyButton() {
        return TextureButton.createButton(
            () -> {
                boolean blockingInputIfNotEmpty = menu.getBlockingInputIfNotEmpty().get();
                if (blockingInputIfNotEmpty) {
                    return BLOCKING_INPUT_IF_NOT_EMPTY;
                } else {
                    return BLOCKING_INPUT_IF_NOT_EMPTY_NOT;
                }
            },
            () -> {
                ValueRef<Boolean> blockingInputIfNotEmptyRef = menu.getBlockingInputIfNotEmpty();
                if (blockingInputIfNotEmptyRef.get()) {
                    blockingInputIfNotEmptyRef.set(false);
                } else {
                    blockingInputIfNotEmptyRef.set(true);
                }
            },
            () -> Component.translatable("gui.item_access_restrictor.blocking_input_if_not_empty_tooltip").getString(),
            () -> {
                boolean blockingInputIfNotEmpty = menu.getBlockingInputIfNotEmpty().get();
                if (blockingInputIfNotEmpty) {
                    return Component.translatable("gui.item_access_restrictor.setting_enabled").getString();
                } else {
                    return Component.translatable("gui.item_access_restrictor.setting_disabled").getString();
                }
            }, () -> {
                boolean blockingInputIfNotEmpty = menu.getBlockingInputIfNotEmpty().get();
                if (blockingInputIfNotEmpty) {
                    return Component.translatable("gui.item_access_restrictor.setting_disabled").getString();
                } else {
                    return Component.translatable("gui.item_access_restrictor.setting_enabled").getString();
                }
            });
    }

    private TextureButton blockingInputIfReceivingRedstoneSignalButton() {
        return TextureButton.createButton(
            () -> {
                boolean blockingInputIfReceivingRedstoneSignal = menu.getBlockingInputIfReceivingRedstoneSignal().get();
                if (blockingInputIfReceivingRedstoneSignal) {
                    return BLOCKING_INPUT_IF_RECEIVING_REDSTONE_SIGNAL;
                } else {
                    return BLOCKING_INPUT_IF_RECEIVING_REDSTONE_SIGNAL_NOT;
                }
            },
            () -> {
                ValueRef<Boolean> blockingInputIfReceivingRedstoneSignalRef = menu.getBlockingInputIfReceivingRedstoneSignal();
                if (blockingInputIfReceivingRedstoneSignalRef.get()) {
                    blockingInputIfReceivingRedstoneSignalRef.set(false);
                } else {
                    blockingInputIfReceivingRedstoneSignalRef.set(true);
                }
            },
            () -> Component.translatable("gui.item_access_restrictor.blocking_input_if_receiving_redstone_signal_tooltip").getString(),
            () -> {
                boolean blockingInputIfReceivingRedstoneSignal = menu.getBlockingInputIfReceivingRedstoneSignal().get();
                if (blockingInputIfReceivingRedstoneSignal) {
                    return Component.translatable("gui.item_access_restrictor.setting_enabled").getString();
                } else {
                    return Component.translatable("gui.item_access_restrictor.setting_disabled").getString();
                }
            }, () -> {
                boolean blockingInputIfReceivingRedstoneSignal = menu.getBlockingInputIfReceivingRedstoneSignal().get();
                if (blockingInputIfReceivingRedstoneSignal) {
                    return Component.translatable("gui.item_access_restrictor.setting_disabled").getString();
                } else {
                    return Component.translatable("gui.item_access_restrictor.setting_enabled").getString();
                }
            });
    }

    private TextureButton inputStackingLimitButton() {
        return TextureButton.createButton(
            () -> {
                int inputStackingLimit = menu.getInputStackingLimit().get();
                if (inputStackingLimit == -1) {
                    return INPUT_STACKING_LIMIT_UNSET;
                } else {
                    return INPUT_STACKING_LIMIT_SET.withNumber(inputStackingLimit);
                }
            },
            () -> {
                ValueRef<Integer> inputStackingLimitRef = menu.getInputStackingLimit();
                int inputStackingLimit = inputStackingLimitRef.get();
                if (inputStackingLimit == -1) {
                    inputStackingLimitRef.set(1);
                } else {
                    inputStackingLimitRef.set(-1);
                }
            },
            () -> Component.translatable("gui.item_access_restrictor.input_stacking_limit_tooltip").getString(),
            () -> {
                int inputStackingLimit = menu.getInputStackingLimit().get();
                if (inputStackingLimit == -1) {
                    return Component.translatable("gui.item_access_restrictor.input_stacking_limit_disabled").getString();
                } else {
                    return inputStackingLimit + "";
                }
            }, () -> {
                int inputStackingLimit = menu.getInputStackingLimit().get();
                if (inputStackingLimit == -1) {
                    return "1";
                } else {
                    return Component.translatable("gui.item_access_restrictor.input_stacking_limit_disabled").getString();
                }
            });
    }

    private SettingSlider inputStackingLimitSlider() {
        Set<Integer> values = ModNBTSettingDefinitions.INPUT_STACKING_LIMIT.getValues();
        int min = values.stream().filter(
            v -> v > 0
        ).min(Integer::compare).orElseThrow();
        int max = values.stream().filter(
            v -> v > 0
        ).max(Integer::compare).orElseThrow();
        return new SettingSlider(
            150,
            Component.translatable("gui.item_access_restrictor.input_stacking_limit_tooltip"),
            min,
            max,
            () -> menu.getInputStackingLimit().get(),
            v -> {
                menu.getInputStackingLimit().set(v);
                System.out.println(v);
            },
            () -> !Objects.equals(menu.getInputStackingLimit().get(), values.iterator().next())
        );
    }

    private TextureButton comparatorOutputModeButton() {
        return TextureButton.createButton(
            () -> {
                ComparatorOutputMode comparatorOutputMode = menu.getComparatorOutputMode().get();
                switch (comparatorOutputMode) {
                    case ONLY_COUNT_EFFECTIVE_ITEMS_AND_SLOTS -> {
                        return COMPARATOR_OUTPUT_MODE_ONLY_COUNT_EFFECTIVE_ITEMS_AND_SLOTS;
                    }
                    case SAME_WITH_FACING_BLOCK -> {
                        return COMPARATOR_OUTPUT_MODE_SAME_WITH_FACING_BLOCK;
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + comparatorOutputMode);
                }
            },
            () -> {
                ValueRef<ComparatorOutputMode> comparatorOutputModeRef = menu.getComparatorOutputMode();
                int ordinal = comparatorOutputModeRef.get().ordinal();
                ComparatorOutputMode nextValue = ordinal >= ComparatorOutputMode.values().length - 1 ? ComparatorOutputMode.values()[0] : ComparatorOutputMode.values()[ordinal + 1];
                comparatorOutputModeRef.set(nextValue);
            },
            () -> Component.translatable("gui.item_access_restrictor.comparator_output_mode_tooltip").getString(),
            () -> {
                ComparatorOutputMode comparatorOutputMode = menu.getComparatorOutputMode().get();
                switch (comparatorOutputMode) {
                    case ONLY_COUNT_EFFECTIVE_ITEMS_AND_SLOTS -> {
                        return Component.translatable("gui.item_access_restrictor.comparator_output_mode_only_count_effective_items_and_slots").getString();
                    }
                    case SAME_WITH_FACING_BLOCK -> {
                        return Component.translatable("gui.item_access_restrictor.comparator_output_mode_same_with_facing_block").getString();
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + comparatorOutputMode);
                }
            }, () -> {
                ComparatorOutputMode comparatorOutputMode = menu.getComparatorOutputMode().get();
                switch (comparatorOutputMode) {
                    case ONLY_COUNT_EFFECTIVE_ITEMS_AND_SLOTS -> {
                        return Component.translatable("gui.item_access_restrictor.comparator_output_mode_same_with_facing_block").getString();
                    }
                    case SAME_WITH_FACING_BLOCK -> {
                        return Component.translatable("gui.item_access_restrictor.comparator_output_mode_only_count_effective_items_and_slots").getString();
                    }
                    default -> throw new IllegalStateException("Unexpected value: " + comparatorOutputMode);
                }
            });
    }

    private TextureButton quantityOfRetainedItemsButton() {
        return TextureButton.createButton(
            () -> {
                int quantityOfRetainedItems = menu.getQuantityOfRetainedItems().get();
                if (quantityOfRetainedItems == 0) {
                    return QUANTITY_OF_RETAINED_ITEMS_UNSET;
                } else {
                    return QUANTITY_OF_RETAINED_ITEMS_SET.withNumber(quantityOfRetainedItems);
                }
            },
            () -> {
                ValueRef<Integer> quantityOfRetainedItemsRef = menu.getQuantityOfRetainedItems();
                int quantityOfRetainedItems = quantityOfRetainedItemsRef.get();
                if (quantityOfRetainedItems == 0) {
                    quantityOfRetainedItemsRef.set(1);
                } else {
                    quantityOfRetainedItemsRef.set(0);
                }
            },
            () -> Component.translatable("gui.item_access_restrictor.quantity_of_retained_items_tooltip").getString(),
            () -> {
                int quantityOfRetainedItems = menu.getQuantityOfRetainedItems().get();
                if (quantityOfRetainedItems == 0) {
                    return Component.translatable("gui.item_access_restrictor.quantity_of_retained_items_disabled").getString();
                } else {
                    return quantityOfRetainedItems + "";
                }
            }, () -> {
                int quantityOfRetainedItems = menu.getQuantityOfRetainedItems().get();
                if (quantityOfRetainedItems == 0) {
                    return "1";
                } else {
                    return Component.translatable("gui.item_access_restrictor.quantity_of_retained_items_disabled").getString();
                }
            });
    }

    private SettingSlider quantityOfRetainedItemsSlider() {
        Set<Integer> values = ModNBTSettingDefinitions.QUANTITY_OF_RETAINED_ITEMS.getValues();
        int min = values.stream().filter(
            v -> v > 0
        ).min(Integer::compare).orElseThrow();
        int max = values.stream().filter(
            v -> v > 0
        ).max(Integer::compare).orElseThrow();
        return new SettingSlider(
            150,
            Component.translatable("gui.item_access_restrictor.quantity_of_retained_items_tooltip"),
            min,
            max,
            () -> menu.getQuantityOfRetainedItems().get(),
            v -> menu.getQuantityOfRetainedItems().set(v),
            () -> !Objects.equals(menu.getQuantityOfRetainedItems().get(), values.iterator().next())
        );
    }

    private TextureButton enableAllSlotsButton() {
        return TextureButton.createVanillaButton(
            () -> CHECK_MARK,
            () -> menu.getSlotDisables().set(new boolean[0]),
            () -> Component.translatable("gui.item_access_restrictor.enable_all_slots_button").getString(),
            () -> !menu.getFacingBlockItemStacks().get().isEmpty()
        );
    }

    private TextureButton disableAllSlotsButton() {
        return TextureButton.createVanillaButton(
            () -> CROSS_MARK,
            () -> {
                boolean[] booleans = new boolean[menu.getFacingBlockItemStacks().get().size()];
                Arrays.fill(booleans, true);
                menu.getSlotDisables().set(booleans);
            },
            () -> Component.translatable("gui.item_access_restrictor.disable_all_slots_button").getString(),
            () -> !menu.getFacingBlockItemStacks().get().isEmpty()
        );
    }

    @SuppressWarnings("SameParameterValue")
    private LinearLayout vertical(int spacing, LayoutElement... layoutElements) {
        LinearLayout layout = LinearLayout.vertical();
        layout.spacing(spacing);
        for (LayoutElement layoutElement : layoutElements) {
            layout.addChild(layoutElement);
        }
        return layout;
    }

    private LinearLayout horizontal(int spacing, LayoutElement... layoutElements) {
        LinearLayout layout = LinearLayout.horizontal();
        layout.spacing(spacing);
        for (LayoutElement layoutElement : layoutElements) {
            layout.addChild(layoutElement);
        }
        return layout;
    }

    @Override
    protected void init() {
        int columnSpacing = 4;
        slotDisableSettingPanel = addRenderableWidget(new SlotDisableSettingPanel(
            menu.getFacingBlockItemStacks()::get,
            menu.getSlotDisables()::get,
            menu.getSlotDisables()::set
        ));
        LinearLayout firstRow = horizontal(
            columnSpacing,
            addRenderableWidget(blockingInputIfNotEmptyButton()),
            addRenderableWidget(blockingInputIfReceivingRedstoneSignalButton()),
            addRenderableWidget(comparatorOutputModeButton())
        );
        LinearLayout secondRow = horizontal(
            columnSpacing,
            addRenderableWidget(inputStackingLimitButton()),
            addRenderableWidget(inputStackingLimitSlider())
        );
        LinearLayout thirdRow = horizontal(
            columnSpacing,
            addRenderableWidget(quantityOfRetainedItemsButton()),
            addRenderableWidget(quantityOfRetainedItemsSlider())
        );

        if (slotDisableSettingPanel.getSlotAreaInfo().allowSlots() > 0) {
            preFacingBlockIsEmpty = false;
            mainLayout = vertical(
                3,
                firstRow,
                secondRow,
                thirdRow,
                horizontal(
                    columnSpacing,
                    addRenderableWidget(disableAllSlotsButton()),
                    addRenderableWidget(enableAllSlotsButton())
                ),
                slotDisableSettingPanel
            );
        } else {
            preFacingBlockIsEmpty = true;
            mainLayout = vertical(
                3,
                firstRow,
                secondRow,
                thirdRow
            );
        }
    }

    @Override
    public void render(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (preFacingBlockIsEmpty != menu.getFacingBlockItemStacks().get().isEmpty()) {
            rebuildWidgets();
        }
        slotDisableSettingPanel.arrangeSlots();
        mainLayout.arrangeElements();

        int layoutYOnWindow = this.titleLabelY + font.lineHeight + 2;
        int windowWidth = WINDOW_BACKGROUND_CORNER_LEFT_TOP.getWidth() + mainLayout.getWidth() + WINDOW_BACKGROUND_CORNER_RIGHT_BOTTOM.getWidth();
        int windowHeight = layoutYOnWindow + mainLayout.getHeight() + WINDOW_BACKGROUND_CORNER_RIGHT_BOTTOM.getHeight();

        int leftPos = (this.width - windowWidth) / 2;
        int topPos = (this.height - windowHeight) / 2;
        // draw transparent background
        renderTransparentBackground(guiGraphics);
        // draw window
        ModTextures.windowBlitTo(guiGraphics, leftPos, topPos, windowWidth, windowHeight);

        // draw title
        guiGraphics.drawString(this.font, this.title, this.titleLabelX + leftPos, this.titleLabelY + topPos, LABEL_COLOR, false);

        // set widgets' position
        mainLayout.setPosition(leftPos + WINDOW_BACKGROUND_CORNER_LEFT_TOP.getWidth(), topPos + layoutYOnWindow);
        // draw widgets
        for (Renderable renderable : this.renderables) {
            renderable.render(guiGraphics, mouseX, mouseY, partialTick);
        }

    }


    @Nonnull
    @Override
    public ItemAccessRestrictorMenu getMenu() {
        return menu;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        InputConstants.Key mouseKey = InputConstants.getKey(keyCode, scanCode);
        if (super.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        } else if (this.minecraft != null && this.minecraft.options.keyInventory.isActiveAndMatches(mouseKey)) {
            // 支持e键关闭
            this.onClose();
            return true;
        } else {
            return false;
        }
    }

    @Override
    public final void tick() {
        if (this.minecraft != null && this.minecraft.player != null && (!this.minecraft.player.isAlive() || this.minecraft.player.isRemoved())) {
            this.minecraft.player.closeContainer();
        }
    }

    @Override
    public void onClose() {
        if (this.minecraft != null && this.minecraft.player != null) {
            this.minecraft.player.closeContainer();
        }
        super.onClose();
    }

}
