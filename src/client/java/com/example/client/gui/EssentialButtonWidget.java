package com.example.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.function.Supplier;

public class EssentialButtonWidget extends ClickableWidget {
    
    private final Supplier<Text> textSupplier;
    private final Runnable onPress;

    public EssentialButtonWidget(int x, int y, int width, int height, Text message, Runnable onPress) {
        super(x, y, width, height, message);
        this.textSupplier = () -> this.getMessage();
        this.onPress = onPress;
    }
    
    public EssentialButtonWidget(int x, int y, int width, int height, Supplier<Text> textSupplier, Runnable onPress) {
        super(x, y, width, height, textSupplier.get());
        this.textSupplier = textSupplier;
        this.onPress = onPress;
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height) {
            this.onPress.run();
            return true;
        }
        return false;
    }

    @Override
    protected void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        this.hovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        
        int bgColor = this.hovered ? 0xAA444444 : 0x90000000;
        int borderColor = this.hovered ? 0xFFFFFFFF : 0x44FFFFFF;
        
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
        
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor); 
        context.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);
        context.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
        context.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
        
        Text currentText = textSupplier != null ? textSupplier.get() : this.getMessage();
        TextRenderer textRenderer = MinecraftClient.getInstance().textRenderer;
        int textWidth = textRenderer.getWidth(currentText);
        
        int textX = this.getX() + (this.width / 2) - (textWidth / 2);
        int textY = this.getY() + (this.height - textRenderer.fontHeight) / 2 + 1;
        
        context.drawTextWithShadow(textRenderer, currentText, textX, textY, this.active ? 0xFFFFFFFF : 0xFFAAAAAA);
    }
    
    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
