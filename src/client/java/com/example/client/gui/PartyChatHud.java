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
        for (PartyMessage msg : MESSAGES) {
            // Un fondo semitransparente detrás de cada línea (x, y, ancho, alto, color)
            int width = client.textRenderer.getWidth(msg.message) + 4;
            context.fill(-2, currentY - 1, width, currentY + client.textRenderer.fontHeight + 1, 0x80000000);
            
            // Renderizar texto con sombra
            context.drawTextWithShadow(client.textRenderer, msg.message, 0, currentY, 0xFFFFFF);
            currentY += client.textRenderer.fontHeight + 3;
        }
        
        context.getMatrices().popMatrix();
    }
}
