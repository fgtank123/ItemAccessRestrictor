package net.fgtank123.minecraft.itemaccessrestrictor.core.gui.widgets;

import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.ModTextureEnum;
import net.fgtank123.minecraft.itemaccessrestrictor.core.gui.utils.ModTexture;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Supplier;

public class TextureButton extends Button {
    private final ModTexture bgTexture;
    private final ModTexture bgTextureHighlight;
    private final Supplier<ModTexture> fgTextureSupplier;
    private final Supplier<String> titleSupplier;
    private final Supplier<String> valueSupplier;
    private final Supplier<String> nextValueSupplier;
    private final Supplier<Boolean> enableGetter;

    public TextureButton(
        ModTexture bgTexture,
        ModTexture bgTextureHighlight,
        Supplier<ModTexture> fgTextureSupplier,
        Runnable onPress,
        Supplier<String> titleSupplier,
        Supplier<String> valueSupplier,
        Supplier<String> nextValueSupplier,
        Supplier<Boolean> enableGetter
    ) {
        super(0, 0, bgTexture.getWidth(), bgTexture.getHeight(), CommonComponents.EMPTY, button -> onPress.run(), DEFAULT_NARRATION);
        this.bgTexture = bgTexture;
        this.bgTextureHighlight = bgTextureHighlight;
        this.fgTextureSupplier = fgTextureSupplier;
        this.titleSupplier = titleSupplier;
        this.valueSupplier = valueSupplier;
        this.nextValueSupplier = nextValueSupplier;
        this.enableGetter = enableGetter;
    }

    private boolean disabled() {
        boolean enable = enableGetter.get();
        if (enable != this.active) {
            this.active = enable;
        }
        return !enable;
    }

    @Override
    public int getWidth() {
        if (disabled()) {
            return 0;
        }
        return super.getWidth();
    }

    @Override
    public int getHeight() {
        if (disabled()) {
            return 0;
        }
        return super.getHeight();
    }

    @Override
    public void updateWidgetNarration(@Nonnull NarrationElementOutput narrationElementOutput) {
        String value = this.valueSupplier.get();
        MutableComponent titleComponent;
        if (StringUtils.isNotBlank(value)) {
            titleComponent = CommonComponents.joinForNarration(
                Component.literal(this.titleSupplier.get()),
                Component.literal(value)
            );
        } else {
            titleComponent = Component.literal(value);
        }
        narrationElementOutput.add(NarratedElementType.TITLE, titleComponent);
        String nextValue = this.nextValueSupplier.get();
        if (this.active && StringUtils.isNotBlank(nextValue)) {
            Component component = Component.literal(nextValue);
            if (this.isFocused()) {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.focused", component));
            } else {
                narrationElementOutput.add(NarratedElementType.USAGE, Component.translatable("narration.cycle_button.usage.hovered", component));
            }
        }
    }

    @Override
    protected void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (disabled()) {
            return;
        }
        ModTexture bgModTexture;
        if (this.isHoveredOrFocused()) {
            bgModTexture = bgTextureHighlight;
        } else {
            bgModTexture = bgTexture;
        }
        bgModTexture.blitTo(guiGraphics, getX(), getY());
        ModTexture fgTexture = fgTextureSupplier.get();
        fgTexture.blitTo(
            guiGraphics,
            getX() + (bgModTexture.getWidth() - fgTexture.getWidth()) / 2,
            getY() + (bgModTexture.getHeight() - fgTexture.getHeight()) / 2
        );
        updateTooltip();
    }

    private String preValue;
    private String preTitle;

    private void updateTooltip() {
        String title = this.titleSupplier.get();
        String value = this.valueSupplier.get();
        if (!Objects.equals(this.preTitle, title) || !Objects.equals(this.preValue, value)) {
            this.preTitle = title;
            this.preValue = value;
            MutableComponent titleComponent = Component.literal(title).setStyle(Style.EMPTY.applyFormat(ChatFormatting.WHITE));
            setTooltip(Tooltip.create(
                StringUtils.isBlank(value) ? titleComponent : CommonComponents.joinLines(
                    titleComponent,
                    Component.literal(value).setStyle(Style.EMPTY.applyFormat(ChatFormatting.GRAY))
                ),
                null
            ));
        }
    }


    public static TextureButton createButton(
        Supplier<ModTexture> fgTextureSupplier,
        Runnable onPress,
        Supplier<String> titleSupplier,
        Supplier<String> valueSupplier,
        Supplier<String> nextValueSupplier
    ) {
        return new TextureButton(
            ModTextureEnum.SETTING_BUTTON,
            ModTextureEnum.SETTING_BUTTON_HIGHLIGHT,
            fgTextureSupplier,
            onPress,
            titleSupplier,
            valueSupplier,
            nextValueSupplier,
            () -> true
        );
    }

    private final static ModTexture VANILLA_SETTING_BUTTON_TEXTURE = new ModTexture() {
        private final ResourceLocation BUTTON = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button.png");

        @Override
        public int getWidth() {
            return 20;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public void blitTo(GuiGraphics guiGraphics, int x, int y, int z) {
            int textureWidth = 200;
            int textureHeight = 20;
            int halfWidth = getWidth() / 2;
            guiGraphics.blit(
                BUTTON,
                x, y, z,
                0, 0, halfWidth, getHeight(),
                textureWidth, textureHeight
            );
            guiGraphics.blit(
                BUTTON,
                x + halfWidth, y, z,
                textureWidth - halfWidth, 0, halfWidth, getHeight(),
                textureWidth, textureHeight
            );
        }
    };
    private final static ModTexture VANILLA_SETTING_BUTTON_HIGHLIGHT_TEXTURE = new ModTexture() {
        private final ResourceLocation BUTTON_HIGHLIGHT = ResourceLocation.withDefaultNamespace("textures/gui/sprites/widget/button_highlighted.png");

        @Override
        public int getWidth() {
            return 20;
        }

        @Override
        public int getHeight() {
            return 20;
        }

        @Override
        public void blitTo(GuiGraphics guiGraphics, int x, int y, int z) {
            int textureWidth = 200;
            int textureHeight = 20;
            int halfWidth = getWidth() / 2;
            guiGraphics.blit(
                BUTTON_HIGHLIGHT,
                x, y, z,
                0, 0, halfWidth, getHeight(),
                textureWidth, textureHeight
            );
            guiGraphics.blit(
                BUTTON_HIGHLIGHT,
                x + halfWidth, y, z,
                textureWidth - halfWidth, 0, halfWidth, getHeight(),
                textureWidth, textureHeight
            );
        }
    };

    public static TextureButton createVanillaButton(
        Supplier<ModTexture> fgTextureSupplier,
        Runnable onPress,
        Supplier<String> titleSupplier,
        Supplier<Boolean> enableGetter
    ) {
        return new TextureButton(
            VANILLA_SETTING_BUTTON_TEXTURE,
            VANILLA_SETTING_BUTTON_HIGHLIGHT_TEXTURE,
            fgTextureSupplier,
            onPress,
            titleSupplier,
            () -> null,
            () -> null,
            enableGetter
        );
    }
}
