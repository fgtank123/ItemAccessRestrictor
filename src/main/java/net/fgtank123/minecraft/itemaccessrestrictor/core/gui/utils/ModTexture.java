package net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils;

import net.minecraft.client.gui.GuiGraphics;

public interface ModTexture {
    int getWidth();

    int getHeight();

    default void blitToStretch(GuiGraphics guiGraphics, int x, int y, int stretchedWidth, int stretchedHeight) {
        throw new UnsupportedOperationException();
    }

    void blitTo(GuiGraphics guiGraphics, int x, int y, int z);

    default void blitTo(GuiGraphics guiGraphics, int x, int y) {
        blitTo(guiGraphics, x, y, 0);
    }
}
