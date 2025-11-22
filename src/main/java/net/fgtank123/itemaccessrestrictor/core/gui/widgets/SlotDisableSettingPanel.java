package net.fgtank123.itemaccessrestrictor.core.gui.widgets;

import net.fgtank123.itemaccessrestrictor.core.gui.ModTextureEnum;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Supplier;


public class SlotDisableSettingPanel extends AbstractWidget {
    private static final int SLOT_HIGHLIGHT_COLOR = 0x80FFFFFF;

    private final Supplier<List<ItemStack>> itemStacksGetter;
    private final Supplier<boolean[]> slotDisablesGetter;
    private final Consumer<boolean[]> slotDisablesSetter;
    private SlotAreaInfo slotAreaInfo;
    private int preSlots;

    private SlotPosition hoverSlotPosition;

    public SlotDisableSettingPanel(
        Supplier<List<ItemStack>> itemStacksGetter,
        Supplier<boolean[]> slotDisablesGetter,
        Consumer<boolean[]> slotDisablesSetter
    ) {
        super(0, 0, 0, 0, Component.empty());
        this.itemStacksGetter = itemStacksGetter;
        this.slotDisablesGetter = slotDisablesGetter;
        this.slotDisablesSetter = slotDisablesSetter;
        arrangeSlots();
    }

    public SlotAreaInfo getSlotAreaInfo() {
        return slotAreaInfo;
    }

    private int getSlotX(int slotColumnIndex) {
        return getX() + slotColumnIndex * getSlotWidth();
    }

    private int getSlotY(int slotRowIndex) {
        return getY() + slotRowIndex * getSlotHeight();
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (hoverSlotPosition != null) {
            int slotIndex = toSlotIndex(hoverSlotPosition.rowIndex(), hoverSlotPosition.columnIndex());
            int size = itemStacksGetter.get().size();
            if (slotIndex < size) {
                boolean[] slotDisables = slotDisablesGetter.get();
                if (size != slotDisables.length) {
                    boolean[] newSlotDisables = new boolean[size];
                    newSlotDisables[slotIndex] = true;
                    slotDisablesSetter.accept(newSlotDisables);
                } else {
                    slotDisables[slotIndex] = !slotDisables[slotIndex];
                    slotDisablesSetter.accept(slotDisables);
                }
            }
        }
    }

    @Override
    protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        refreshHoverSlotPosition(mouseX, mouseY);
        boolean[] slotDisables = slotDisablesGetter.get();
        List<ItemStack> itemStacks = itemStacksGetter.get();

        out:
        for (int rowIndex = 0; rowIndex < slotAreaInfo.rowCount(); rowIndex++) {
            for (int columnIndex = 0; columnIndex < slotAreaInfo.columnCount(); columnIndex++) {
                int slotIndex = toSlotIndex(rowIndex, columnIndex);
                if (slotIndex >= slotAreaInfo.allowSlots()) {
                    break out;
                }
                int slotX = getSlotX(columnIndex);
                int slotY = getSlotY(rowIndex);
                if (slotDisabled(slotDisables, slotIndex)) {
                    ModTextureEnum.DISABLED_SLOT_BACKGROUND.blitTo(guiGraphics, slotX, slotY);
                    if (hoverSlotPosition != null && hoverSlotPosition.rowIndex() == rowIndex && hoverSlotPosition.columnIndex() == columnIndex) {
                        ItemStack itemStack = itemStacks.get(slotIndex);
                        guiGraphics.renderItem(itemStack, slotX + 1, slotY + 1);
                        guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, slotX + 1, slotY + 1);
                    }
                } else {
                    ModTextureEnum.SLOT_BACKGROUND.blitTo(guiGraphics, slotX, slotY);
                    ItemStack itemStack = itemStacks.get(slotIndex);
                    guiGraphics.renderItem(itemStack, slotX + 1, slotY + 1);
                    guiGraphics.renderItemDecorations(Minecraft.getInstance().font, itemStack, slotX + 1, slotY + 1);
                }
            }
        }

        if (hoverSlotPosition != null) {
            int rowIndex = hoverSlotPosition.rowIndex();
            int columnIndex = hoverSlotPosition.columnIndex();
            int slotX = getSlotX(columnIndex);
            int slotY = getSlotY(rowIndex);
            guiGraphics.fillGradient(
                RenderType.guiOverlay(),
                slotX, slotY,
                slotX + getSlotWidth(), slotY + getSlotHeight(),
                SLOT_HIGHLIGHT_COLOR, SLOT_HIGHLIGHT_COLOR,
                0
            );
            int slotIndex = toSlotIndex(rowIndex, columnIndex);
            setTooltip(Tooltip.create(
                CommonComponents.joinLines(
                    Component.translatable("gui.item_access_restrictor.slot_disables_tooltip").setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE)),
                    slotDisabled(slotDisables, slotIndex) ?
                        Component.translatable("gui.item_access_restrictor.setting_disabled").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY)) :
                        Component.translatable("gui.item_access_restrictor.setting_enabled").setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY))
                )
            ));
        } else {
            setTooltip(null);
        }
    }

    private static boolean slotDisabled(boolean[] slotDisables, int slotIndex) {
        if (slotIndex < slotDisables.length) {
            return slotDisables[slotIndex];
        } else {
            return false;
        }
    }


    private void refreshHoverSlotPosition(int mouseX, int mouseY) {
        int mouseHoverRowIndex = Math.floorDiv(mouseY - getY(), getSlotHeight());
        if (mouseHoverRowIndex < 0 || mouseHoverRowIndex >= slotAreaInfo.rowCount()) {
            hoverSlotPosition = null;
            return;
        }
        int mouseHoverColumnIndex = Math.floorDiv(mouseX - getX(), getSlotWidth());
        if (mouseHoverColumnIndex < 0 || mouseHoverColumnIndex >= slotAreaInfo.columnCount()) {
            hoverSlotPosition = null;
            return;
        }
        int slotIndex = toSlotIndex(mouseHoverRowIndex, mouseHoverColumnIndex);
        if (slotIndex >= slotAreaInfo.allowSlots()) {
            hoverSlotPosition = null;
            return;
        }
        hoverSlotPosition = new SlotPosition(mouseHoverRowIndex, mouseHoverColumnIndex);
    }

    private int toSlotIndex(int rowIndex, int columnIndex) {
        return slotAreaInfo.columnCount() * rowIndex + columnIndex;
    }

    @Override
    protected void updateWidgetNarration(@Nonnull NarrationElementOutput narrationElementOutput) {
        if (hoverSlotPosition != null) {
            narrationElementOutput.add(
                NarratedElementType.POSITION,
                Component.translatable(
                    "gui.item_access_restrictor.slot_disables_position.narrate",
                    hoverSlotPosition.rowIndex() + 1,
                    hoverSlotPosition.columnIndex() + 1
                )
            );
        }

    }

    private static int getSlotWidth() {
        return ModTextureEnum.SLOT_BACKGROUND.getWidth();
    }

    private static int getSlotHeight() {
        return ModTextureEnum.SLOT_BACKGROUND.getHeight();
    }


    public void arrangeSlots() {
        List<ItemStack> itemStacks = itemStacksGetter.get();
        if (slotAreaInfo == null || preSlots != itemStacks.size()) {
            slotAreaInfo = resolveSlotAreaInfo(itemStacks.size());
            this.setWidth(slotAreaInfo.slotAreaWidth());
            this.setHeight(slotAreaInfo.slotAreaHeight());
            preSlots = itemStacks.size();
        }
    }

    private record SlotPosition(int rowIndex, int columnIndex) {
    }

    public record SlotAreaInfo(
        int columnCount,
        int rowCount,
        int allowSlots
    ) {
        public int slotAreaWidth() {
            return columnCount * getSlotWidth();
        }

        public int slotAreaHeight() {
            return rowCount * getSlotHeight();
        }
    }

    private SlotAreaInfo resolveSlotAreaInfo(int slots) {
        int columnCount;
        int rowCount;
        if (slots > 108) {
            slots = 108;
        }
        if (slots == 0) {
            columnCount = 0;
            rowCount = 0;
        } else if (slots <= 9) {
            columnCount = slots;
            rowCount = 1;
        } else if (slots <= 54) {
            columnCount = 9;
            rowCount = Math.ceilDiv(slots, 9);
        } else {
            columnCount = 12;
            rowCount = Math.ceilDiv(slots, 12);
        }
        return new SlotAreaInfo(columnCount, rowCount, slots);
    }
}
