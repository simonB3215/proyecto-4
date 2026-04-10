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
    private TextFieldWidget xField;
    private TextFieldWidget yField;
    private TextFieldWidget scaleField;
    private ButtonWidget toggleButton;
    private ButtonWidget langButton;
    
    // Lista de idiomas requeridos de ejemplo por el usuario
    private static final List<String> LANGUAGES = Arrays.asList("es", "en", "pl", "fr", "de", "ru", "pt", "it");

    public TranslatorOptionsScreen(Screen parent) {
        super(Text.literal("Configuración del Traductor"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        int centerX = this.width / 2;
        int startY = 30;

        // Botón Activar/Desactivar
        this.toggleButton = ButtonWidget.builder(Text.literal("Traductor: " + (ExampleModClient.isEnabled ? "ON" : "OFF")), button -> {
            ExampleModClient.isEnabled = !ExampleModClient.isEnabled;
            button.setMessage(Text.literal("Traductor: " + (ExampleModClient.isEnabled ? "ON" : "OFF")));
            saveSettings();
        }).dimensions(centerX - 100, startY, 200, 20).build();
        this.addDrawableChild(toggleButton);

        // Botón Ciclo de Idioma
        this.langButton = ButtonWidget.builder(Text.literal("Idioma: " + ExampleModClient.targetLanguage.toUpperCase()), button -> {
            int index = LANGUAGES.indexOf(ExampleModClient.targetLanguage);
            if (index == -1 || index == LANGUAGES.size() - 1) {
                ExampleModClient.targetLanguage = LANGUAGES.get(0);
            } else {
                ExampleModClient.targetLanguage = LANGUAGES.get(index + 1);
            }
            button.setMessage(Text.literal("Idioma: " + ExampleModClient.targetLanguage.toUpperCase()));
            saveSettings();
        }).dimensions(centerX - 100, startY + 30, 200, 20).build();
        this.addDrawableChild(langButton);

        // TextFieldWidget para API Key
        this.apiKeyField = new TextFieldWidget(this.textRenderer, centerX - 100, startY + 75, 200, 20, Text.literal("API Key"));
        this.apiKeyField.setMaxLength(100);
        this.apiKeyField.setText(ConfigManager.apiKey); // In Yarn it's setText()
        this.addDrawableChild(this.apiKeyField);

        // Botón Sincronizar Hypixel
        ButtonWidget syncButton = ButtonWidget.builder(Text.literal("Sincronizar desde Hypixel"), button -> {
            saveSettings();
            
            button.setMessage(Text.literal("Sincronizando..."));
            HypixelApiHelper.syncLanguageFromHypixel(ConfigManager.apiKey).thenAccept(lang -> {
                if (lang != null && this.client != null) {
                    ExampleModClient.targetLanguage = lang;
                    saveSettings();
                    this.client.execute(() -> {
                        langButton.setMessage(Text.literal("Idioma: " + ExampleModClient.targetLanguage.toUpperCase()));
                        button.setMessage(Text.literal("¡Sincronizado con Hypixel!"));
                    });
                } else if (this.client != null) {
                    this.client.execute(() -> {
                        button.setMessage(Text.literal("Error: ¡Llave inválida u oculta!"));
                    });
                }
            });
        }).dimensions(centerX - 100, startY + 100, 200, 20).build();
        this.addDrawableChild(syncButton);

        // GUI Options
        this.xField = new TextFieldWidget(this.textRenderer, centerX - 100, startY + 145, 60, 20, Text.literal("X"));
        this.xField.setText(String.valueOf(ConfigManager.hudX));
        this.addDrawableChild(this.xField);

        this.yField = new TextFieldWidget(this.textRenderer, centerX - 30, startY + 145, 60, 20, Text.literal("Y"));
        this.yField.setText(String.valueOf(ConfigManager.hudY));
        this.addDrawableChild(this.yField);

        this.scaleField = new TextFieldWidget(this.textRenderer, centerX + 40, startY + 145, 60, 20, Text.literal("Scale"));
        this.scaleField.setText(String.valueOf(ConfigManager.hudScale));
        this.addDrawableChild(this.scaleField);

        // Botón Guardar y Salir
        ButtonWidget closeButton = ButtonWidget.builder(Text.literal("Cerrar"), button -> {
            saveSettings();
            if(this.client != null) this.client.setScreen(this.parent);
        }).dimensions(centerX - 100, this.height - 30, 200, 20).build();
        this.addDrawableChild(closeButton);
    }

    private void saveSettings() {
        if (this.apiKeyField != null) ConfigManager.apiKey = this.apiKeyField.getText();
        if (this.xField != null) {
            try { ConfigManager.hudX = Integer.parseInt(this.xField.getText()); } catch(Exception ignored) {}
        }
        if (this.yField != null) {
            try { ConfigManager.hudY = Integer.parseInt(this.yField.getText()); } catch(Exception ignored) {}
        }
        if (this.scaleField != null) {
            try { ConfigManager.hudScale = Float.parseFloat(this.scaleField.getText()); } catch(Exception ignored) {}
        }
        ConfigManager.saveConfig();
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, 10, 0xFFFFFF);
        context.drawTextWithShadow(this.textRenderer, "Hypixel API Key:", this.width / 2 - 100, 60, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, "X:", this.width / 2 - 100, 133, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, "Y:", this.width / 2 - 30, 133, 0xA0A0A0);
        context.drawTextWithShadow(this.textRenderer, "Escala:", this.width / 2 + 40, 133, 0xA0A0A0);
    }

    @Override
    public void removed() {
        saveSettings();
        super.removed();
    }
}
