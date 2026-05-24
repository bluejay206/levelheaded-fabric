package me.bluejay.client.hud;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;

public class DescriptionInputScreen extends Screen {

    private final TextFieldWidget textField;

    public DescriptionInputScreen() {
        super(Text.literal("Sticky Description"));
        this.textField = new TextFieldWidget(
                MinecraftClient.getInstance().textRenderer,
                0, 0, 300, 20,
                Text.literal("Description")
        );
    }

    @Override
    protected void init() {
        this.textField.setText(SurveyorHud.getStickyDescription());
        this.textField.setMaxLength(64);

        // Center the text field
        this.textField.setPosition(this.width / 2 - 150, this.height / 2 - 10);

        this.addDrawableChild(this.textField);
        this.setFocused(this.textField);   // Still requires click to type (as requested)
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 257) { // Enter
            SurveyorHud.setStickyDescription(this.textField.getText());
            this.close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void close() {
        MinecraftClient.getInstance().setScreen(null);
    }

    @Override
    public void render(net.minecraft.client.gui.DrawContext context, int mouseX, int mouseY, float delta) {
        this.renderBackground(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer,
                "Sticky Description (click to edit, Enter to save)",
                this.width / 2, this.height / 2 - 40, 0xAAAAAA);
        super.render(context, mouseX, mouseY, delta);
    }
}