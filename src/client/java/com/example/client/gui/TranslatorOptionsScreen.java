package com.example.client.gui;

import com.example.client.ConfigManager;
import com.example.client.ExampleModClient;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.Arrays;
import java.util.List;

public class TranslatorOptionsScreen extends Screen {

    private final Screen parent;
    private EditBox apiKeyField;
    private Button toggleButton;
    private Button langButton;
    
    // Lista de idiomas requeridos de ejemplo por el usuario
    private static final List<String> LANGUAGES = Arrays.asList("es", "en", "pl", "fr", "de", "ru", "pt", "it");

    public TranslatorOptionsScreen(Screen parent) {
        super(Component.literal("Configuración del Traductor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 50;

        // Botón Activar/Desactivar
        this.toggleButton = Button.builder(Component.literal("Traductor: " + (ExampleModClient.isEnabled ? "ON" : "OFF")), button -> {
            ExampleModClient.isEnabled = !ExampleModClient.isEnabled;
            button.setMessage(Component.literal("Traductor: " + (ExampleModClient.isEnabled ? "ON" : "OFF")));
            ConfigManager.saveConfig();
        }).bounds(centerX - 100, startY, 200, 20).build();
        this.addRenderableWidget(toggleButton);

        // Botón Ciclo de Idioma
        this.langButton = Button.builder(Component.literal("Idioma: " + ExampleModClient.targetLanguage.toUpperCase()), button -> {
            int index = LANGUAGES.indexOf(ExampleModClient.targetLanguage);
            if (index == -1 || index == LANGUAGES.size() - 1) {
                ExampleModClient.targetLanguage = LANGUAGES.get(0);
            } else {
                ExampleModClient.targetLanguage = LANGUAGES.get(index + 1);
            }
            button.setMessage(Component.literal("Idioma: " + ExampleModClient.targetLanguage.toUpperCase()));
            ConfigManager.saveConfig();
        }).bounds(centerX - 100, startY + 30, 200, 20).build();
        this.addRenderableWidget(langButton);

        // EditBox para API Key
        this.apiKeyField = new EditBox(this.font, centerX - 100, startY + 80, 200, 20, Component.literal("API Key"));
        this.apiKeyField.setMaxLength(100);
        this.apiKeyField.setValue(ConfigManager.apiKey);
        this.addRenderableWidget(this.apiKeyField);

        // Botón Sincronizar Hypixel
        Button syncButton = Button.builder(Component.literal("Sincronizar desde Hypixel"), button -> {
            ConfigManager.apiKey = this.apiKeyField.getValue();
            ConfigManager.saveConfig();
            
            button.setMessage(Component.literal("Sincronizando..."));
            HypixelApiHelper.syncLanguageFromHypixel(ConfigManager.apiKey).thenAccept(lang -> {
                if (lang != null && this.minecraft != null) {
                    ExampleModClient.targetLanguage = lang;
                    ConfigManager.saveConfig();
                    this.minecraft.execute(() -> {
                        langButton.setMessage(Component.literal("Idioma: " + ExampleModClient.targetLanguage.toUpperCase()));
                        button.setMessage(Component.literal("¡Sincronizado con Hypixel!"));
                    });
                } else if (this.minecraft != null) {
                    this.minecraft.execute(() -> {
                        button.setMessage(Component.literal("Error: ¡Llave inválida u oculta!"));
                    });
                }
            });
        }).bounds(centerX - 100, startY + 110, 200, 20).build();
        this.addRenderableWidget(syncButton);

        // Botón Guardar y Salir
        Button closeButton = Button.builder(Component.literal("Cerrar"), button -> {
            ConfigManager.apiKey = this.apiKeyField.getValue();
            ConfigManager.saveConfig();
            if(this.minecraft != null) this.minecraft.setScreen(this.parent);
        }).bounds(centerX - 100, this.height - 40, 200, 20).build();
        this.addRenderableWidget(closeButton);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, 20, 0xFFFFFF);
        guiGraphics.drawString(this.font, "Hypixel API Key:", this.width / 2 - 100, 65, 0xA0A0A0);
        super.render(guiGraphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void removed() {
        ConfigManager.apiKey = this.apiKeyField.getValue();
        ConfigManager.saveConfig();
        super.removed();
    }
}
