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
    private ButtonWidget toggleButton;
    private ButtonWidget modeButton;
    private ButtonWidget langDropdownBtn;
    private ButtonWidget colorDropdownBtn;
    private ButtonWidget syncButton;
    private ButtonWidget posButton;
    private ButtonWidget bgButton;
    private ButtonWidget viewModeButton;
    private ButtonWidget closeButton;

    private boolean langExpanded = false;
    private boolean colorExpanded = false;
    private List<ButtonWidget> langOptionButtons = new java.util.ArrayList<>();
    private List<ButtonWidget> colorOptionButtons = new java.util.ArrayList<>();
    
    private List<String> langOptions = Arrays.asList("[ES] Español", "[EN] English", "[PL] Polski", "[FR] Français", "[DE] Deutsch", "[RU] Русский", "[PT] Português", "[IT] Italiano");
    private List<String> colorOptions = Arrays.asList("§fBlanco", "§eAmarillo", "§aVerde", "§cRojo", "§bCeleste", "§dRosa", "§6Naranja", "§7Gris");

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

        // Limpiar opciones
        langOptionButtons.clear();
        colorOptionButtons.clear();

        this.toggleButton = ButtonWidget.builder(Text.empty(), btn -> {
            ExampleModClient.isEnabled = !ExampleModClient.isEnabled;
            sendAestheticMessage(ExampleModClient.isEnabled);
            saveSettings();
        }).dimensions(centerX - 102, startY, 100, 20).build();
        this.toggleButton.setAlpha(0.0f);
        this.addDrawableChild(toggleButton);

        this.modeButton = ButtonWidget.builder(Text.empty(), btn -> {
            ExampleModClient.translateMode = ExampleModClient.translateMode.equals("PARTY") ? "ALL" : "PARTY";
            saveSettings();
        }).dimensions(centerX + 2, startY, 100, 20).build();
        this.modeButton.setAlpha(0.0f);
        this.addDrawableChild(modeButton);

        this.langDropdownBtn = ButtonWidget.builder(Text.empty(), btn -> {
            langExpanded = !langExpanded;
            if(langExpanded) colorExpanded = false;
            updateDropdowns();
        }).dimensions(centerX - 100, startY + 25, 200, 20).build();
        this.langDropdownBtn.setAlpha(0.0f);
        this.addDrawableChild(langDropdownBtn);

        this.colorDropdownBtn = ButtonWidget.builder(Text.empty(), btn -> {
            colorExpanded = !colorExpanded;
            if(colorExpanded) langExpanded = false;
            updateDropdowns();
        }).dimensions(centerX - 100, startY + 50, 200, 20).build();
        this.colorDropdownBtn.setAlpha(0.0f);
        this.addDrawableChild(colorDropdownBtn);

        this.apiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, startY + 90, 200, 20, Text.literal("API Key"));
        this.apiKeyField.setMaxLength(100);
        this.apiKeyField.setText(ConfigManager.apiKey);
        this.addDrawableChild(this.apiKeyField);

        this.syncButton = ButtonWidget.builder(Text.empty(), btn -> {
            saveSettings();
            HypixelApiHelper.syncLanguageFromHypixel(ConfigManager.apiKey).thenAccept(lang -> {
                if (lang != null && this.client != null) {
                    ExampleModClient.targetLanguage = lang;
                    saveSettings();
                }
            });
        }).dimensions(centerX - 100, startY + 115, 200, 20).build();
        this.syncButton.setAlpha(0.0f);
        this.addDrawableChild(syncButton);

        this.posButton = ButtonWidget.builder(Text.empty(), btn -> {
            saveSettings();
            if (this.client != null) this.client.setScreen(new HudEditorScreen(this));
        }).dimensions(centerX - 100, startY + 140, 200, 20).build();
        this.posButton.setAlpha(0.0f);
        this.addDrawableChild(posButton);

        this.bgButton = ButtonWidget.builder(Text.empty(), btn -> {
            ConfigManager.showBackground = !ConfigManager.showBackground;
            saveSettings();
        }).dimensions(centerX - 102, startY + 165, 100, 20).build();
        this.bgButton.setAlpha(0.0f);
        this.addDrawableChild(bgButton);

        this.viewModeButton = ButtonWidget.builder(Text.empty(), btn -> {
            ConfigManager.combinedMode = !ConfigManager.combinedMode;
            saveSettings();
        }).dimensions(centerX + 2, startY + 165, 100, 20).build();
        this.viewModeButton.setAlpha(0.0f);
        this.addDrawableChild(viewModeButton);

        this.closeButton = ButtonWidget.builder(Text.empty(), btn -> {
            saveSettings();
            if(this.client != null) this.client.setScreen(this.parent);
        }).dimensions(centerX - 100, this.height - 30, 200, 20).build();
        this.closeButton.setAlpha(0.0f);
        this.addDrawableChild(closeButton);

        updateDropdowns();
    }

    private void updateDropdowns() {
        for(ButtonWidget opt : langOptionButtons) this.remove(opt);
        for(ButtonWidget opt : colorOptionButtons) this.remove(opt);
        langOptionButtons.clear();
        colorOptionButtons.clear();

        boolean anyExpanded = langExpanded || colorExpanded;
        if (this.toggleButton != null) this.toggleButton.active = !anyExpanded;
        if (this.modeButton != null) this.modeButton.active = !anyExpanded;
        if (this.langDropdownBtn != null) this.langDropdownBtn.active = !colorExpanded;
        if (this.colorDropdownBtn != null) this.colorDropdownBtn.active = !langExpanded;
        if (this.apiKeyField != null) this.apiKeyField.setEditable(!anyExpanded);
        if (this.syncButton != null) this.syncButton.active = !anyExpanded;
        if (this.posButton != null) this.posButton.active = !anyExpanded;
        if (this.bgButton != null) this.bgButton.active = !anyExpanded;
        if (this.viewModeButton != null) this.viewModeButton.active = !anyExpanded;
        if (this.closeButton != null) this.closeButton.active = !anyExpanded;

        if (langExpanded) {
            int my = langDropdownBtn.getY() + 20;
            for (int i = 0; i < langOptions.size(); i++) {
                String optionStr = langOptions.get(i);
                ButtonWidget optBtn = ButtonWidget.builder(Text.empty(), btn -> {
                    ExampleModClient.targetLanguage = optionStr.substring(1, 3).toLowerCase();
                    langExpanded = false;
                    saveSettings();
                    updateDropdowns();
                }).dimensions(langDropdownBtn.getX(), my + (i * 20), 200, 20).build();
                optBtn.setAlpha(0.0f);
                langOptionButtons.add(optBtn);
                this.addDrawableChild(optBtn);
            }
        }

        if (colorExpanded) {
            int my = colorDropdownBtn.getY() + 20;
            for (int i = 0; i < colorOptions.size(); i++) {
                String optionStr = colorOptions.get(i);
                ButtonWidget optBtn = ButtonWidget.builder(Text.empty(), btn -> {
                    ConfigManager.textColor = optionStr.substring(0, 2);
                    colorExpanded = false;
                    saveSettings();
                    updateDropdowns();
                }).dimensions(colorDropdownBtn.getX(), my + (i * 20), 200, 20).build();
                optBtn.setAlpha(0.0f);
                colorOptionButtons.add(optBtn);
                this.addDrawableChild(optBtn);
            }
        }
    }

    private void saveSettings() {
        if (this.apiKeyField != null) ConfigManager.apiKey = this.apiKeyField.getText();
        ConfigManager.saveConfig();
    }

    private void drawEssentialAesthetic(DrawContext context, ButtonWidget btn, String text, int mouseX, int mouseY, boolean isDropdown, boolean expanded) {
        if (!btn.visible) return;
        boolean hovered = mouseX >= btn.getX() && mouseY >= btn.getY() && mouseX < btn.getX() + btn.getWidth() && mouseY < btn.getY() + btn.getHeight();
        int bgColor = hovered ? 0xAA444444 : 0x90000000;
        int borderColor = hovered ? 0xFFFFFFFF : 0x44FFFFFF;

        context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), bgColor);
        context.fill(btn.getX(), btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + 1, borderColor);
        context.fill(btn.getX(), btn.getY() + btn.getHeight() - 1, btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), borderColor);
        context.fill(btn.getX(), btn.getY(), btn.getX() + 1, btn.getY() + btn.getHeight(), borderColor);
        context.fill(btn.getX() + btn.getWidth() - 1, btn.getY(), btn.getX() + btn.getWidth(), btn.getY() + btn.getHeight(), borderColor);

        int textX = isDropdown ? btn.getX() + 5 : btn.getX() + (btn.getWidth() / 2) - (this.textRenderer.getWidth(text) / 2);
        int textY = btn.getY() + (btn.getHeight() - this.textRenderer.fontHeight) / 2 + 1;
        context.drawTextWithShadow(this.textRenderer, text, textX, textY, btn.active ? 0xFFFFFFFF : 0xFFAAAAAA);

        if (isDropdown) {
            context.drawTextWithShadow(this.textRenderer, expanded ? "▲" : "▼", btn.getX() + btn.getWidth() - 12, textY, 0xFFAAAAAA);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Hypixel API Key:", this.width / 2 - 100, 105, 0xFFA0A0A0);

        drawEssentialAesthetic(context, toggleButton, "Traductor: " + (ExampleModClient.isEnabled ? "§aON" : "§cOFF"), mouseX, mouseY, false, false);
        drawEssentialAesthetic(context, modeButton, ExampleModClient.translateMode.equals("PARTY") ? "Modo: PARTY" : "Modo: GLOBAL", mouseX, mouseY, false, false);
        
        String currentLangStr = "[ES] Español";
        for (String opt : langOptions) { if (opt.toLowerCase().contains("[" + ExampleModClient.targetLanguage + "]")) { currentLangStr = opt; break; } }
        drawEssentialAesthetic(context, langDropdownBtn, "Idioma: " + currentLangStr, mouseX, mouseY, true, langExpanded);

        String currentColorStr = "§eAmarillo";
        for (String c : colorOptions) { if (c.startsWith(ConfigManager.textColor)) { currentColorStr = c; break; } }
        drawEssentialAesthetic(context, colorDropdownBtn, "Color F.: " + currentColorStr, mouseX, mouseY, true, colorExpanded);

        drawEssentialAesthetic(context, syncButton, "Sincronizar desde Hypixel", mouseX, mouseY, false, false);
        drawEssentialAesthetic(context, posButton, "Reposicionar Chat Traducido", mouseX, mouseY, false, false);
        drawEssentialAesthetic(context, bgButton, ConfigManager.showBackground ? "Fondo: ON" : "Fondo: OFF", mouseX, mouseY, false, false);
        drawEssentialAesthetic(context, viewModeButton, ConfigManager.combinedMode ? "Chat: General" : "Chat: Dividido", mouseX, mouseY, false, false);
        drawEssentialAesthetic(context, closeButton, "Guardar y Cerrar", mouseX, mouseY, false, false);

        if (langExpanded) {
            context.fill(langDropdownBtn.getX(), langDropdownBtn.getY() + 20, langDropdownBtn.getX() + langDropdownBtn.getWidth(), langDropdownBtn.getY() + 20 + (langOptions.size() * 20), 0xD0000000);
            for (int i = 0; i < langOptionButtons.size(); i++) {
                ButtonWidget optBtn = langOptionButtons.get(i);
                boolean hov = mouseX >= optBtn.getX() && mouseY >= optBtn.getY() && mouseX < optBtn.getX() + optBtn.getWidth() && mouseY < optBtn.getY() + optBtn.getHeight();
                if (hov) context.fill(optBtn.getX() + 1, optBtn.getY(), optBtn.getX() + optBtn.getWidth() - 1, optBtn.getY() + 20, 0x884444FF);
                context.drawTextWithShadow(this.textRenderer, langOptions.get(i), optBtn.getX() + 5, optBtn.getY() + 6, hov ? 0xFFFFFFFF : 0xFFAAAAAA);
            }
        }

        if (colorExpanded) {
            context.fill(colorDropdownBtn.getX(), colorDropdownBtn.getY() + 20, colorDropdownBtn.getX() + colorDropdownBtn.getWidth(), colorDropdownBtn.getY() + 20 + (colorOptions.size() * 20), 0xD0000000);
            for (int i = 0; i < colorOptionButtons.size(); i++) {
                ButtonWidget optBtn = colorOptionButtons.get(i);
                boolean hov = mouseX >= optBtn.getX() && mouseY >= optBtn.getY() && mouseX < optBtn.getX() + optBtn.getWidth() && mouseY < optBtn.getY() + optBtn.getHeight();
                if (hov) context.fill(optBtn.getX() + 1, optBtn.getY(), optBtn.getX() + optBtn.getWidth() - 1, optBtn.getY() + 20, 0x884444FF);
                context.drawTextWithShadow(this.textRenderer, colorOptions.get(i), optBtn.getX() + 5, optBtn.getY() + 6, hov ? 0xFFFFFFFF : 0xFFAAAAAA);
            }
        }
    }

    @Override
    public void removed() {
        saveSettings();
        super.removed();
    }
}
