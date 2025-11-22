package net.fgtank123.itemaccessrestrictor.core.gui.widgets;

import net.minecraft.client.Options;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;

import javax.annotation.Nonnull;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class SettingSlider extends ExtendedSlider {
    private final Consumer<Integer> valueApplier;
    private final Supplier<Integer> valueSupplier;
    private final Supplier<Boolean> enableSupplier;

    public SettingSlider(
        int width,
        Component prefix,
        int minValue,
        int maxValue,
        Supplier<Integer> valueSupplier,
        Consumer<Integer> valueApplier,
        Supplier<Boolean> enableSupplier
    ) {
        super(0, 0, width, 20, prefix, Component.empty(), minValue, maxValue, valueSupplier.get(), 1D, 0, true);
        this.valueApplier = valueApplier;
        this.valueSupplier = valueSupplier;
        this.enableSupplier = enableSupplier;
        this.active = enableSupplier.get();
    }

    @Override
    protected void updateMessage() {
        this.setMessage(Options.genericValueLabel(prefix, Component.literal(this.getValueString())));
    }

    @Nonnull
    @Override
    public Component getMessage() {
        if (disabled()) {
            return prefix;
        } else {
            return super.getMessage();
        }
    }

    private boolean disabled() {
        boolean enable = enableSupplier.get();
        if (enable != this.active) {
            this.active = enable;
        }
        return !enable;
    }

    private boolean isDragging = false;

    @Override
    protected void onDrag(double mouseX, double mouseY, double dragX, double dragY) {
        if (disabled()) {
            return;
        }
        if (!isDragging) {
            isDragging = true;
        }
        super.onDrag(mouseX, mouseY, dragX, dragY);
    }

    private boolean isClicking = false;

    @Override
    public void onClick(double mouseX, double mouseY) {
        if (disabled()) {
            return;
        }
        if (!isClicking) {
            isClicking = true;
        }
        super.onClick(mouseX, mouseY);
    }

    @Override
    public void onRelease(double mouseX, double mouseY) {
        if (disabled()) {
            return;
        }
        isDragging = false;
        isClicking = false;
        super.onRelease(mouseX, mouseY);
    }

    @Override
    public void onClick(double mouseX, double mouseY, int button) {
        if (disabled()) {
            return;
        }
        if (!isClicking) {
            isClicking = true;
        }
        super.onClick(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (disabled()) {
            return false;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (disabled()) {
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public void renderWidget(@Nonnull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (disabled()) {
            if (getValueInt() != minValue) {
                this.setValue(minValue);
            }
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        } else {
            if (!isDragging && !isClicking) {
                int value = valueSupplier.get();
                if (getValueInt() != value) {
                    this.setValue(value);
                }
            }
            super.renderWidget(guiGraphics, mouseX, mouseY, partialTick);
        }
    }

    private int preValue;

    @Override
    protected void applyValue() {
        if (!disabled() && valueApplier != null) {
            int value = this.getValueInt();
            if (preValue != value) {
                valueApplier.accept(value);
                preValue = value;
            }
        }
    }
}
