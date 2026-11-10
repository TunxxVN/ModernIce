package com.tunxxvn.modernice.config;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.GuiTextField;
import net.minecraftforge.fml.client.config.GuiSlider;
import org.lwjgl.input.Keyboard;

import java.io.IOException;
import java.util.Locale;

public final class IceConfigScreen extends GuiScreen {
    private final IceConfig config;
    private GuiSlider slider;
    private GuiTextField valueField;

    public IceConfigScreen(IceConfig config) { this.config = config; }

    @Override
    public void initGui() {
        if (valueField != null && valueField.isFocused()) applyValueField();
        buttonList.clear();
        Keyboard.enableRepeatEvents(true);
        int rowWidth = Math.min(200, width - 32);
        int left = (width - rowWidth) / 2;
        valueField = new GuiTextField(5, fontRendererObj, left + rowWidth - 64, height / 2 - 8, 64, 20);
        valueField.setMaxStringLength(16);
        valueField.setValidator(text -> text.matches("[0-9]*(\\.[0-9]*)?"));
        valueField.setVisible(config.customSlipperiness());
        valueField.setEnabled(config.customSlipperiness());
        buttonList.add(new GuiButton(3, left, height / 2 - 58, rowWidth, 20,
                "Modern Ice: " + (config.enabled() ? "ON" : "OFF")));
        buttonList.add(new GuiButton(4, left, height / 2 - 34, rowWidth, 20,
                "Custom Slipperiness: " + (config.customSlipperiness() ? "ON" : "OFF")));
        slider = new GuiSlider(0, left, height / 2 - 8, rowWidth - 72, 20,
                "Slipperiness: ", "", IceConfig.MIN, IceConfig.MAX, config.value(), true, true,
                changed -> {
                    config.set(changed.getValue());
                    String value = String.format(Locale.ROOT, "%.3f", config.value());
                    changed.displayString = "Slipperiness: " + value;
                    valueField.setText(value);
                });
        slider.precision = 3;
        slider.updateSlider();
        slider.visible = config.customSlipperiness();
        slider.enabled = config.customSlipperiness();
        buttonList.add(slider);
        GuiButton reset = new GuiButton(1, width / 2 - 88, height / 2 + 48, 84, 20, "Reset");
        reset.enabled = config.customSlipperiness();
        buttonList.add(reset);
        buttonList.add(new GuiButton(2, width / 2 + 4, height / 2 + 48, 84, 20, "Done"));
    }

    @Override
    protected void actionPerformed(GuiButton button) {
        if (valueField.isFocused()) applyValueField();
        if (button.id == 1) {
            config.set(IceConfig.MIN);
            slider.setValue(config.value());
            slider.updateSlider();
        } else if (button.id == 2) {
            mc.displayGuiScreen(null);
        } else if (button.id == 3) {
            config.setEnabled(!config.enabled());
            initGui();
        } else if (button.id == 4) {
            config.setCustomSlipperiness(!config.customSlipperiness());
            initGui();
        }
    }

    @Override
    protected void keyTyped(char typed, int key) throws IOException {
        if (valueField.getVisible()) {
            if (key == Keyboard.KEY_TAB) {
                if (valueField.isFocused()) applyValueField();
                else valueField.setFocused(true);
                return;
            }
            if (valueField.isFocused()) {
                if (key == Keyboard.KEY_RETURN || key == Keyboard.KEY_NUMPADENTER) {
                    applyValueField();
                    return;
                }
                if (valueField.textboxKeyTyped(typed, key)) return;
            }
        }
        if (slider.visible && (key == Keyboard.KEY_LEFT || key == Keyboard.KEY_RIGHT)) {
            config.set(config.value() + (key == Keyboard.KEY_LEFT ? -1 : 1) * 0.001D);
            slider.setValue(config.value());
            slider.updateSlider();
        } else {
            super.keyTyped(typed, key);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (valueField.getVisible()) {
            boolean focused = valueField.isFocused();
            valueField.mouseClicked(mouseX, mouseY, mouseButton);
            if (focused && !valueField.isFocused()) applyValueField();
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void applyValueField() {
        double value;
        try {
            value = Double.parseDouble(valueField.getText());
        } catch (NumberFormatException ignored) {
            value = config.value();
        }
        config.set(value);
        valueField.setFocused(false);
        slider.setValue(config.value());
        slider.updateSlider();
    }

    @Override public void updateScreen() { valueField.updateCursorCounter(); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        drawCenteredString(fontRendererObj, "Modern Ice", width / 2, height / 2 - 80, 0xFFFFFF);
        if (slider.visible) {
            int y = slider.yPosition + 27;
            for (String line : fontRendererObj.listFormattedStringToWidth(IceConfig.NOTE, Math.min(240, width - 32))) {
                drawCenteredString(fontRendererObj, line, width / 2, y, 0xBBBBBB);
                y += fontRendererObj.FONT_HEIGHT + 1;
            }
        }
        super.drawScreen(mouseX, mouseY, partialTicks);
        valueField.drawTextBox();
    }

    @Override
    public void onGuiClosed() {
        if (valueField.isFocused()) applyValueField();
        Keyboard.enableRepeatEvents(false);
        config.save();
    }
    @Override public boolean doesGuiPauseGame() { return false; }
}
