package com.example.client.gui;

import com.example.client.ConfigManager;
import com.example.client.ExampleModClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class TranslatorOptionsScreen extends Screen {

    private final Screen parent;
    private TextFieldWidget apiKeyField;
    private EssentialButtonWidget toggleButton;
    private EssentialButtonWidget langButton;
    
    private static final List<String> LANGUAGES = Arrays.asList("es", "en", "pl", "fr", "de", "ru", "pt", "it");

    public TranslatorOptionsScreen(Screen parent) {
        super(Text.literal("Configuración del Traductor"));
        this.parent = parent;
    }

    private void sendAestheticMessage(boolean state) {
        if (this.client != null && this.client.player != null) {
            String color = state ? "§a§lACTIVADO" : "§c§lDESACTIVADO";
            String m = "§8[§bPartyTranslator§8] §7» §fEl Traductor Inteligente ha sido " + color + "§f.";
            this.client.player.sendMessage(Text.literal(m), false);
        }
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;

        // Botón Activar/Desactivar
        this.toggleButton = new EssentialButtonWidget(centerX - 102, startY, 100, 20, () -> Text.literal("Traductor: " + (ExampleModClient.isEnabled ? "§aON" : "§cOFF")), () -> {
            ExampleModClient.isEnabled = !ExampleModClient.isEnabled;
            sendAestheticMessage(ExampleModClient.isEnabled);
            saveSettings();
        });
        this.addDrawableChild(toggleButton);

        // Botón Modo Traducción
        this.addDrawableChild(new EssentialButtonWidget(centerX + 2, startY, 100, 20, () -> Text.literal(ExampleModClient.translateMode.equals("PARTY") ? "Modo: PARTY" : "Modo: GLOBAL"), () -> {
            ExampleModClient.translateMode = ExampleModClient.translateMode.equals("PARTY") ? "ALL" : "PARTY";
            saveSettings();
        }));

        // Menú Desplegable de Idiomas (Banderas/Nombres estilizados)
        List<String> langOptions = Arrays.asList("[ES] Español", "[EN] English", "[PL] Polski", "[FR] Français", "[DE] Deutsch", "[RU] Русский", "[PT] Português", "[IT] Italiano");
        String currentLangStr = "[ES] Español";
        for (String opt : langOptions) {
            if (opt.toLowerCase().contains("[" + ExampleModClient.targetLanguage + "]")) {
                currentLangStr = opt; break;
            }
        }
        
        EssentialDropdownWidget langDropdown = new EssentialDropdownWidget(centerX - 100, startY + 25, 200, 20, Text.empty(), langOptions, currentLangStr, selected -> {
            ExampleModClient.targetLanguage = selected.substring(1, 3).toLowerCase();
            saveSettings();
        });

        // Dropdown Color de Texto
        List<String> colorOptions = Arrays.asList("§fBlanco", "§eAmarillo", "§aVerde", "§cRojo", "§bCeleste", "§dRosa", "§6Naranja", "§7Gris");
        String currentColorStr = "§eAmarillo";
        for (String c : colorOptions) {
            if (c.startsWith(ConfigManager.textColor)) { currentColorStr = c; break; }
        }
        
        EssentialDropdownWidget colorDropdown = new EssentialDropdownWidget(centerX - 100, startY + 50, 200, 20, Text.empty(), colorOptions, currentColorStr, selected -> {
            ConfigManager.textColor = selected.substring(0, 2);
            saveSettings();
        });

        // TextFieldWidget para API Key
        this.apiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, startY + 90, 200, 20, Text.literal("API Key"));
        this.apiKeyField.setMaxLength(100);
        this.apiKeyField.setText(ConfigManager.apiKey);
        this.addDrawableChild(this.apiKeyField);

        // Botines de Acción Extendida
        this.addDrawableChild(new EssentialButtonWidget(centerX - 100, startY + 115, 200, 20, Text.literal("Sincronizar desde Hypixel"), () -> {
            saveSettings();
            HypixelApiHelper.syncLanguageFromHypixel(ConfigManager.apiKey).thenAccept(lang -> {
                if (lang != null && this.client != null) {
                    ExampleModClient.targetLanguage = lang;
                    saveSettings();
                    this.client.execute(() -> this.client.setScreen(new TranslatorOptionsScreen(this.parent))); // Reload screen
                }
            });
        }));

        this.addDrawableChild(new EssentialButtonWidget(centerX - 100, startY + 140, 200, 20, Text.literal("Reposicionar Chat Traducido"), () -> {
            saveSettings();
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }));

        this.addDrawableChild(new EssentialButtonWidget(centerX - 100, this.height - 30, 200, 20, Text.literal("Guardar y Cerrar"), () -> {
            saveSettings();
            if(this.client != null) this.client.setScreen(this.parent);
        }));
        
        // Añadir dropdowns al final para que queden on top (últimos dibujados = renderizados encima de otros widgets normales, aunque EssentialDropdownWidget maneja Z-index 300)
        this.addDrawableChild(colorDropdown);
        this.addDrawableChild(langDropdown);
    }

    private void saveSettings() {
        if (this.apiKeyField != null) ConfigManager.apiKey = this.apiKeyField.getText();
        ConfigManager.saveConfig();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Hypixel API Key:", this.width / 2 - 100, 60, 0xFFA0A0A0);
    }

    @Override
    public void removed() {
        saveSettings();
        super.removed();
    }
}
