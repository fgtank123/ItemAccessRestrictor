package net.fgtank123.minecraft.itemaccessrestrictor.core.gui;

import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.ModTexture;
import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.Rectangle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import static net.fgtank123.minecraft.itemaccessrestrictor.core.gui.ModTextureFiles.*;

public enum ModTextureEnum implements ModTexture {
    BLOCKING_INPUT_IF_NOT_EMPTY(SETTING_STATES, new Rectangle(0, 0, 16, 16)),
    BLOCKING_INPUT_IF_NOT_EMPTY_NOT(SETTING_STATES, new Rectangle(16, 0, 16, 16)),
    BLOCKING_INPUT_IF_RECEIVING_REDSTONE_SIGNAL(SETTING_STATES, new Rectangle(32, 0, 16, 16)),
    BLOCKING_INPUT_IF_RECEIVING_REDSTONE_SIGNAL_NOT(SETTING_STATES, new Rectangle(48, 0, 16, 16)),
    INPUT_STACKING_LIMIT_UNSET(SETTING_STATES, new Rectangle(0, 16, 16, 16)),
    INPUT_STACKING_LIMIT_SET(
        SETTING_STATES,
        new Rectangle(16, 16, 16, 16),
        new Rectangle(8, 0, 7, 6)
    ),
    QUANTITY_OF_RETAINED_ITEMS_UNSET(SETTING_STATES, new Rectangle(32, 16, 16, 16)),
    QUANTITY_OF_RETAINED_ITEMS_SET(
        SETTING_STATES,
        new Rectangle(48, 16, 16, 16),
        new Rectangle(8, 8, 7, 6)
    ),
    COMPARATOR_OUTPUT_MODE_ONLY_COUNT_EFFECTIVE_ITEMS_AND_SLOTS(SETTING_STATES, new Rectangle(0, 32, 16, 16)),
    COMPARATOR_OUTPUT_MODE_SAME_WITH_FACING_BLOCK(SETTING_STATES, new Rectangle(16, 32, 16, 16)),
    CROSS_MARK(SETTING_STATES, new Rectangle(32, 32, 16, 16)),
    CHECK_MARK(SETTING_STATES, new Rectangle(48, 32, 16, 16)),
    N_0(SETTING_STATES, new Rectangle(0, 58, 3, 6)),
    N_1(SETTING_STATES, new Rectangle(3, 58, 3, 6)),
    N_2(SETTING_STATES, new Rectangle(6, 58, 3, 6)),
    N_3(SETTING_STATES, new Rectangle(9, 58, 3, 6)),
    N_4(SETTING_STATES, new Rectangle(12, 58, 3, 6)),
    N_5(SETTING_STATES, new Rectangle(15, 58, 3, 6)),
    N_6(SETTING_STATES, new Rectangle(18, 58, 3, 6)),
    N_7(SETTING_STATES, new Rectangle(21, 58, 3, 6)),
    N_8(SETTING_STATES, new Rectangle(24, 58, 3, 6)),
    N_9(SETTING_STATES, new Rectangle(27, 58, 3, 6)),
    SLOT_BACKGROUND(MAIN, new Rectangle(0, 0, 18, 18)),
    DISABLED_SLOT_BACKGROUND(MAIN, new Rectangle(18, 0, 18, 18)),
    WINDOW_BACKGROUND_CORNER_LEFT_TOP(MAIN, new Rectangle(0, 18, 7, 7)),
    WINDOW_BACKGROUND_CORNER_RIGHT_TOP(MAIN, new Rectangle(7, 18, 7, 7)),
    WINDOW_BACKGROUND_CORNER_LEFT_BOTTOM(MAIN, new Rectangle(0, 25, 7, 7)),
    WINDOW_BACKGROUND_CORNER_RIGHT_BOTTOM(MAIN, new Rectangle(7, 25, 7, 7)),
    WINDOW_BACKGROUND_EDGE_LEFT(MAIN, new Rectangle(0, 24, 7, 1)),
    WINDOW_BACKGROUND_EDGE_TOP(MAIN, new Rectangle(7, 18, 1, 7)),
    WINDOW_BACKGROUND_EDGE_RIGHT(MAIN, new Rectangle(7, 25, 7, 1)),
    WINDOW_BACKGROUND_EDGE_BOTTOM(MAIN, new Rectangle(6, 25, 1, 7)),
    WINDOW_BACKGROUND_FILL(MAIN, new Rectangle(6, 24, 1, 1)),
    SETTING_BUTTON(MAIN, new Rectangle(0, 44, 20, 20)),
    SETTING_BUTTON_HIGHLIGHT(MAIN, new Rectangle(20, 44, 20, 20)),
    ;
    private final ResourceLocation fileId;
    private final Rectangle textureRectangle;
    private final Rectangle relativeNumberAreaRectangle;

    ModTextureEnum(ResourceLocation fileId, Rectangle textureRectangle) {
        this(fileId, textureRectangle, null);
    }

    ModTextureEnum(ResourceLocation fileId, Rectangle textureRectangle, Rectangle relativeNumberAreaRectangle) {
        if (!WH_MAP.containsKey(fileId)) {
            throw new IllegalArgumentException();
        }
        this.fileId = fileId;
        this.textureRectangle = textureRectangle;
        this.relativeNumberAreaRectangle = relativeNumberAreaRectangle;
    }

    ModTexture withNumber(int number) {
        if (relativeNumberAreaRectangle == null) {
            throw new UnsupportedOperationException();
        }
        return new ModTextureWithNumber(this, relativeNumberAreaRectangle, number);
    }

    @SuppressWarnings("unused")
    public ResourceLocation getFileId() {
        return fileId;
    }

    public int getX() {
        return textureRectangle.x();
    }

    public int getY() {
        return textureRectangle.y();
    }

    @Override
    public int getWidth() {
        return textureRectangle.width();
    }

    @Override
    public int getHeight() {
        return textureRectangle.height();
    }

    public int getTextureWidth() {
        return WH_MAP.get(fileId).getLeft();
    }

    public int getTextureHeight() {
        return WH_MAP.get(fileId).getRight();
    }

    @SuppressWarnings("unused")
    public Rectangle getRelativeNumberAreaRectangle() {
        return relativeNumberAreaRectangle;
    }

    @Override
    public void blitToStretch(GuiGraphics guiGraphics, int x, int y, int stretchedWidth, int stretchedHeight) {
        guiGraphics.blit(fileId, x, y, stretchedWidth, stretchedHeight, getX(), getY(), getWidth(), getHeight(), getTextureWidth(), getTextureHeight());
    }

    @Override
    public void blitTo(GuiGraphics guiGraphics, int x, int y, int z) {
        guiGraphics.blit(fileId, x, y, z, getX(), getY(), getWidth(), getHeight(), getTextureWidth(), getTextureHeight());
    }
}
