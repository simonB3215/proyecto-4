package com.example.client.gui;

import com.example.client.ConfigManager;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;

public class HudEditorScreen extends Screen {

    private final Screen parent;
    private boolean isDragging = false;
    private int dragOffsetX = 0;
    private int dragOffsetY = 0;
    
    private final int MOCK_WIDTH = 180;
    private final int MOCK_HEIGHT = 38;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("Editor del Chat de Party"));
        this.parent = parent;
    }

    private boolean wasMouseDown = false;

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (verticalAmount > 0) {
            ConfigManager.hudScale += 0.05f;
            if (ConfigManager.hudScale > 3.0f) ConfigManager.hudScale = 3.0f;
        } else if (verticalAmount < 0) {
            ConfigManager.hudScale -= 0.05f;
            if (ConfigManager.hudScale < 0.3f) ConfigManager.hudScale = 0.3f;
        }
        ConfigManager.saveConfig();
        return super.mouseScrolled(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // En 1.21.11, usar el método genérico que dibuja el marco oscuro por omisión.
        super.render(context, mouseX, mouseY, delta);
        
        int x = ConfigManager.hudX;
        int y = ConfigManager.hudY;
        float scale = ConfigManager.hudScale;

        int scaledWidth = (int)(MOCK_WIDTH * scale);
        int scaledHeight = (int)(MOCK_HEIGHT * scale);
        boolean hovering = (mouseX >= x && mouseX <= x + scaledWidth && mouseY >= y && mouseY <= y + scaledHeight);

        boolean isMouseDown = org.lwjgl.glfw.GLFW.glfwGetMouseButton(net.minecraft.client.MinecraftClient.getInstance().getWindow().getHandle(), org.lwjgl.glfw.GLFW.GLFW_MOUSE_BUTTON_1) == org.lwjgl.glfw.GLFW.GLFW_PRESS;
        
        if (isMouseDown && !wasMouseDown && hovering) {
            isDragging = true;
            dragOffsetX = mouseX - x;
            dragOffsetY = mouseY - y;
        } else if (!isMouseDown && wasMouseDown) {
            if (isDragging) ConfigManager.saveConfig();
            isDragging = false;
        }

        if (isDragging) {
            ConfigManager.hudX = mouseX - dragOffsetX;
            ConfigManager.hudY = mouseY - dragOffsetY;
            x = ConfigManager.hudX;
            y = ConfigManager.hudY;
        }
        
        wasMouseDown = isMouseDown;
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)x, (float)y);
        context.getMatrices().scale(scale, scale);
        
        // Mock background box
        context.fill(0, 0, MOCK_WIDTH, MOCK_HEIGHT, 0x80000000);
        
        // Outline si estamos interactuando
        
        // drawBorder draw outer lines 1px
        if (hovering || isDragging) {
            context.fill(-1, -1, MOCK_WIDTH + 1, 0, 0xFF00FF00); // Top
            context.fill(-1, MOCK_HEIGHT, MOCK_WIDTH + 1, MOCK_HEIGHT + 1, 0xFF00FF00); // Bottom
            context.fill(-1, 0, 0, MOCK_HEIGHT, 0xFF00FF00); // Left
            context.fill(MOCK_WIDTH, 0, MOCK_WIDTH + 1, MOCK_HEIGHT, 0xFF00FF00); // Right
        }
        
        context.drawTextWithShadow(this.textRenderer, "§9Party > §fNotch: §eHola Mundo!", 4, 5, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "§9Party > §fSimon: §eEscala al " + (int)(scale*100) + "%", 4, 20, 0xFFFFFF);
        
        context.getMatrices().popMatrix();
        
        context.drawCenteredTextWithShadow(this.textRenderer, "Arrastra la caja para mover el Chat de Party", this.width / 2, this.height - 40, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Gira la Rueda del Ratón para aumentar/reducir el tamaño", this.width / 2, this.height - 25, 0xFFFFFF);
        context.drawCenteredTextWithShadow(this.textRenderer, "Pulsa §e[ESC]§f para volver", this.width / 2, this.height - 10, 0xAAAAAA);
    }

    @Override
    public void close() {
        ConfigManager.saveConfig();
        if (this.client != null) {
            this.client.setScreen(this.parent);
        }
    }
}
