package com.example.client.gui;

import com.example.client.ConfigManager;
import com.example.client.ExampleModClient;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.text.Text;
import net.minecraft.util.Util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class PartyChatHud {
    
    public static class PartyMessage {
        public Text message;
        public long addedTime;
        
        public PartyMessage(Text message) {
            this.message = message;
            this.addedTime = System.currentTimeMillis();
        }
    }
    
    private static final List<PartyMessage> MESSAGES = new ArrayList<>();
    private static final long DISPLAY_DURATION = 10000; // 10 segundos
    
    public static void addMessage(Text text) {
        MESSAGES.add(new PartyMessage(text));
        if (MESSAGES.size() > 50) {
            MESSAGES.remove(0);
        }
    }
    
    public static void render(DrawContext context, net.minecraft.client.render.RenderTickCounter tickCounter) {
        if (!ExampleModClient.isEnabled || MESSAGES.isEmpty()) return;
        
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.options.hudHidden) return;
        
        long currentTime = System.currentTimeMillis();
        
        // Remove expired messages gracefully
        Iterator<PartyMessage> iterator = MESSAGES.iterator();
        while (iterator.hasNext()) {
            PartyMessage msg = iterator.next();
            if (currentTime - msg.addedTime > DISPLAY_DURATION) {
                iterator.remove();
            }
        }
        
        if (MESSAGES.isEmpty()) return;
        
        int x = ConfigManager.hudX;
        int y = ConfigManager.hudY;
        
        context.getMatrices().pushMatrix();
        context.getMatrices().translate((float)x, (float)y);
        context.getMatrices().scale(ConfigManager.hudScale, ConfigManager.hudScale);
        
        int currentY = 0;
        int maxWidth = 320;
        
        for (PartyMessage msg : MESSAGES) {
            long age = currentTime - msg.addedTime;
            float alpha = 1.0f;
            
            // Fade in (300ms)
            if (age < 300) {
                alpha = age / 300.0f;
            } 
            // Fade out (500ms)
            else if (DISPLAY_DURATION - age < 500) {
                alpha = (DISPLAY_DURATION - age) / 500.0f;
            }
            
            alpha = Math.max(0.0f, Math.min(1.0f, alpha));
            
            int textAlphaInt = (int) (alpha * 255.0f);
            int textColor = (textAlphaInt << 24) | 0x00FFFFFF;
            
            int bgAlphaInt = (int) (alpha * 128.0f); // Máximo 50% opacidad del fondo negro
            int bgColor = (bgAlphaInt << 24) | 0x000000;

            List<net.minecraft.text.OrderedText> lines = client.textRenderer.wrapLines(msg.message, maxWidth);
            
            // Determinar ancho unificado para estabilizar estéticamente el texto de todo el bloque
            int blockWidth = 0;
            for (net.minecraft.text.OrderedText line : lines) {
                int w = client.textRenderer.getWidth(line);
                if (w > blockWidth) blockWidth = w;
            }
            
            int blockHeight = lines.size() * (client.textRenderer.fontHeight + 3);
            
            // Dibujar recuadro de fondo centralizado una sola vez por mensaje para evitar franjas
            context.fill(-2, currentY - 2, blockWidth + 4, currentY + blockHeight - 1, bgColor);
            
            for (net.minecraft.text.OrderedText line : lines) {
                context.drawTextWithShadow(client.textRenderer, line, 0, currentY, textColor);
                currentY += client.textRenderer.fontHeight + 3;
            }
            
            // Margen entre diferentes mensajes
            currentY += 4;
        }
        
        context.getMatrices().popMatrix();
    }
}
