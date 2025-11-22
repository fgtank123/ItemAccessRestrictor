package net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.GuiGraphics;

import java.util.Arrays;
import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

public class RenderUtils {
    public static void windowBlitTo(
        ModTexture cornerLeftTop,
        ModTexture cornerRightTop,
        ModTexture cornerRightBottom,
        ModTexture cornerLeftBottom,
        ModTexture edgeLeft,
        ModTexture edgeTop,
        ModTexture edgeRight,
        ModTexture edgeBottom,
        ModTexture fill,
        GuiGraphics guiGraphics,
        int windowX,
        int windowY,
        int windowWidth,
        int windowHeight
    ) {
        // corners
        cornerLeftTop.blitTo(
            guiGraphics,
            windowX,
            windowY
        );
        cornerRightTop.blitTo(
            guiGraphics,
            windowX + windowWidth - cornerRightTop.getWidth(),
            windowY
        );
        cornerRightBottom.blitTo(
            guiGraphics,
            windowX + windowWidth - cornerRightBottom.getWidth(),
            windowY + windowHeight - cornerRightBottom.getHeight()
        );
        cornerLeftBottom.blitTo(
            guiGraphics,
            windowX,
            windowY + windowHeight - cornerLeftBottom.getHeight()
        );
        // edges
        edgeLeft.blitToStretch(
            guiGraphics,
            windowX,
            windowY + cornerLeftTop.getHeight(),
            edgeLeft.getWidth(),
            windowHeight - cornerLeftTop.getHeight() - cornerLeftBottom.getHeight()
        );
        edgeTop.blitToStretch(
            guiGraphics,
            windowX + cornerLeftTop.getWidth(),
            windowY,
            windowWidth - cornerLeftTop.getWidth() - cornerRightTop.getWidth(),
            edgeTop.getHeight()
        );
        edgeRight.blitToStretch(
            guiGraphics,
            windowX + windowWidth - edgeRight.getWidth(),
            windowY + cornerRightTop.getHeight(),
            edgeRight.getWidth(),
            windowHeight - cornerRightTop.getHeight() - cornerRightBottom.getHeight()
        );
        edgeBottom.blitToStretch(
            guiGraphics,
            windowX + cornerLeftBottom.getWidth(),
            windowY + windowHeight - edgeBottom.getHeight(),
            windowWidth - cornerLeftBottom.getWidth() - cornerRightBottom.getWidth(),
            edgeBottom.getHeight()
        );
        // center
        fill.blitToStretch(
            guiGraphics,
            windowX + cornerLeftTop.getWidth(),
            windowY + cornerLeftTop.getHeight(),
            windowWidth - cornerLeftTop.getWidth() - cornerRightBottom.getWidth(),
            windowHeight - cornerLeftTop.getHeight() - cornerRightBottom.getHeight()
        );
    }

    public static void blitToWithNumber(
        ModTexture mainModTexture,
        Rectangle relativeNumberAreaRectangle,
        Function<Character, ModTexture> charModTextureGetter,
        GuiGraphics guiGraphics,
        int number,
        int x,
        int y,
        int z
    ) {
        if (number < 0) {
            throw new IllegalArgumentException();
        }
        String numberString = ((Integer) number).toString();
        List<ModTexture> numberCharTextures = numberString.chars().mapToObj(v -> charModTextureGetter.apply((char) v)).toList();
        int maxHeight = numberCharTextures.stream().reduce(
            0,
            (i, modTexture) -> Math.max(i, modTexture.getHeight()),
            Math::max
        );
        BinaryOperator<Integer> widthAdder = (a, b) -> {
            if (a == 0 || b == 0) {
                return a + b;
            } else {
                return a + 1 + b;
            }
        };
        int numberWidth = numberCharTextures.stream().reduce(
            0,
            (i, modTexture) -> widthAdder.apply(i, modTexture.getWidth()),
            widthAdder
        );
        if (relativeNumberAreaRectangle.width() < numberWidth || relativeNumberAreaRectangle.height() < maxHeight) {
            throw new IllegalArgumentException();
        }
        mainModTexture.blitTo(guiGraphics, x, y, z);
        int poseX = x + relativeNumberAreaRectangle.x() + (relativeNumberAreaRectangle.width() - numberWidth) / 2;
        int poseY = y + relativeNumberAreaRectangle.y() + (relativeNumberAreaRectangle.height() - maxHeight) / 2;
        for (ModTexture numberCharTexture : numberCharTextures) {
            numberCharTexture.blitTo(guiGraphics, poseX, poseY + maxHeight - numberCharTexture.getHeight(), z);
            poseX += numberCharTexture.getWidth() + 1;
        }
    }

    @SuppressWarnings("unused")
    public static void renderWithTransparency(Runnable renderTask, float alpha) {
        // 保存当前GL状态
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        float[] preShaderColor = RenderSystem.getShaderColor();
        preShaderColor = Arrays.copyOf(preShaderColor, preShaderColor.length);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, alpha);

        // 执行渲染任务
        renderTask.run();

        // 恢复GL状态
        RenderSystem.setShaderColor(preShaderColor[0], preShaderColor[1], preShaderColor[2], preShaderColor[3]);
        RenderSystem.disableBlend();
    }

}
