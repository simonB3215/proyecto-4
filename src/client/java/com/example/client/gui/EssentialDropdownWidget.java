package com.example.client.gui;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ClickableWidget;
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder;
import net.minecraft.text.Text;

import java.util.List;
import java.util.function.Consumer;

public class EssentialDropdownWidget extends ClickableWidget {

    private boolean expanded = false;
    private final List<String> options;
    private int selectedIndex = 0;
    private final Consumer<String> onSelect;
    
    private final int OPTION_HEIGHT = 20;

    public EssentialDropdownWidget(int x, int y, int width, int height, Text message, List<String> options, String currentSelection, Consumer<String> onSelect) {
        super(x, y, width, height, message);
        this.options = options;
        this.onSelect = onSelect;
        
        if (options.contains(currentSelection)) {
            this.selectedIndex = options.indexOf(currentSelection);
        }
    }

     public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!this.active || !this.visible) return false;
        
        boolean mainHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;

        if (expanded) {
            int menuHeight = options.size() * OPTION_HEIGHT;
            if (mouseX >= getX() && mouseX <= getX() + width && mouseY > getY() + height && mouseY <= getY() + height + menuHeight) {
                int clickedIndex = (int) ((mouseY - (getY() + height)) / OPTION_HEIGHT);
                if (clickedIndex >= 0 && clickedIndex < options.size()) {
                    selectedIndex = clickedIndex;
                    onSelect.accept(options.get(selectedIndex));
                    expanded = false;
                    return true;
                }
            } else if (mainHovered) {
                expanded = !expanded;
                return true;
            } else {
                expanded = false;
            }
        } else {
            if (mainHovered) {
                expanded = true;
                return true;
            }
        }
        return false;
    }

    @Override
    public void renderWidget(DrawContext context, int mouseX, int mouseY, float delta) {
        boolean mainHovered = mouseX >= this.getX() && mouseY >= this.getY() && mouseX < this.getX() + this.width && mouseY < this.getY() + this.height;
        
        int bgColor = mainHovered || expanded ? 0xAA444444 : 0x90000000;
        int borderColor = mainHovered || expanded ? 0xFFFFFFFF : 0x44FFFFFF;
        
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + this.height, bgColor);
        
        context.fill(this.getX(), this.getY(), this.getX() + this.width, this.getY() + 1, borderColor);
        context.fill(this.getX(), this.getY() + this.height - 1, this.getX() + this.width, this.getY() + this.height, borderColor);
        context.fill(this.getX(), this.getY(), this.getX() + 1, this.getY() + this.height, borderColor);
        context.fill(this.getX() + this.width - 1, this.getY(), this.getX() + this.width, this.getY() + this.height, borderColor);
        
        String displayText = "Idioma: " + options.get(selectedIndex);
        int textX = this.getX() + 5;
        int textY = this.getY() + (this.height - MinecraftClient.getInstance().textRenderer.fontHeight) / 2 + 1;
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, displayText, textX, textY, 0xFFFFFFFF);
        
        context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, expanded ? "▲" : "▼", this.getX() + this.width - 12, textY, 0xFFAAAAAA);
        
        if (expanded) {
            int menuY = this.getY() + this.height;
            int menuHeight = options.size() * OPTION_HEIGHT;
            
            // Dropdown background
            context.fill(this.getX(), menuY, this.getX() + this.width, menuY + menuHeight, 0xD0000000);
            
            // Dropdown border
            context.fill(this.getX(), menuY, this.getX() + this.width, menuY + menuHeight, 0xD0000000); // Clear middle
            
            context.fill(this.getX(), menuY + menuHeight - 1, this.getX() + this.width, menuY + menuHeight, borderColor); // Bottom
            context.fill(this.getX(), menuY, this.getX() + 1, menuY + menuHeight, borderColor); // Left
            context.fill(this.getX() + this.width - 1, menuY, this.getX() + this.width, menuY + menuHeight, borderColor); // Right
            
            for (int i = 0; i < options.size(); i++) {
                int optY = menuY + (i * OPTION_HEIGHT);
                boolean optHovered = mouseX >= this.getX() && mouseX <= this.getX() + this.width && mouseY >= optY && mouseY < optY + OPTION_HEIGHT;
                
                if (optHovered) {
                    context.fill(this.getX() + 1, optY, this.getX() + this.width - 1, optY + OPTION_HEIGHT, 0x884444FF);
                }
                
                context.drawTextWithShadow(MinecraftClient.getInstance().textRenderer, options.get(i), this.getX() + 5, optY + 6, optHovered ? 0xFFFFFFFF : 0xFFAAAAAA);
            }
        }
    }

    @Override
    protected void appendClickableNarrations(NarrationMessageBuilder builder) {}
}
