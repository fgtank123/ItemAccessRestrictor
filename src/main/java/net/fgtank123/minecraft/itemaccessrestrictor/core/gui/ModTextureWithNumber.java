package net.fgtank123.minecraft.itemaccessrestrictor.core.gui;

import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.ModTexture;
import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.Rectangle;
import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.RenderUtils;
import net.minecraft.client.gui.GuiGraphics;

import java.util.HashMap;
import java.util.Map;

import static net.fgtank123.minecraft.itemaccessrestrictor.core.gui.ModTextureEnum.*;


public record ModTextureWithNumber(
    ModTexture modTexture,
    Rectangle relativeNumberAreaRectangle,
    int number
) implements ModTexture {

    @Override
    public int getWidth() {
        return modTexture.getWidth();
    }

    @Override
    public int getHeight() {
        return modTexture.getHeight();
    }

    @Override
    public void blitTo(GuiGraphics guiGraphics, int x, int y, int z) {
        blitToWithNumber(guiGraphics, number, x, y, z);
    }

    @Override
    public void blitTo(GuiGraphics guiGraphics, int x, int y) {
        blitToWithNumber(guiGraphics, number, x, y, 0);
    }

    private static final Map<Character, ModTexture> numberTextureMap;

    static {
        numberTextureMap = new HashMap<>();
        numberTextureMap.put('0', N_0);
        numberTextureMap.put('1', N_1);
        numberTextureMap.put('2', N_2);
        numberTextureMap.put('3', N_3);
        numberTextureMap.put('4', N_4);
        numberTextureMap.put('5', N_5);
        numberTextureMap.put('6', N_6);
        numberTextureMap.put('7', N_7);
        numberTextureMap.put('8', N_8);
        numberTextureMap.put('9', N_9);
    }

    private void blitToWithNumber(GuiGraphics guiGraphics, int number, int x, int y, int z) {
        RenderUtils.blitToWithNumber(
            modTexture,
            relativeNumberAreaRectangle,
            numberTextureMap::get,
            guiGraphics,
            number,
            x,
            y,
            z
        );
    }
}
