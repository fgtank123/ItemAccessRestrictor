package net.fgtank123.minecraft.itemaccessrestrictor.core.gui;

import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import static net.fgtank123.minecraft.itemaccessrestrictor.core.gui.ModTextureEnum.*;

public interface ModTextures {
    static void windowBlitTo(GuiGraphics guiGraphics, int windowX, int windowY, int windowWidth, int windowHeight) {
        RenderUtils.windowBlitTo(
            WINDOW_BACKGROUND_CORNER_LEFT_TOP,
            WINDOW_BACKGROUND_CORNER_RIGHT_TOP,
            WINDOW_BACKGROUND_CORNER_RIGHT_BOTTOM,
            WINDOW_BACKGROUND_CORNER_LEFT_BOTTOM,
            WINDOW_BACKGROUND_EDGE_LEFT,
            WINDOW_BACKGROUND_EDGE_TOP,
            WINDOW_BACKGROUND_EDGE_RIGHT,
            WINDOW_BACKGROUND_EDGE_BOTTOM,
            WINDOW_BACKGROUND_FILL,
            guiGraphics,
            windowX,
            windowY,
            windowWidth,
            windowHeight
        );
    }
}
