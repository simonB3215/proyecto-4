package com.example.client;

import com.example.client.gui.TranslatorOptionsScreen;
import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExampleModClient implements ClientModInitializer {

    // Regex to match "Party > [MVP+] Name: Message" or "Party > Name: Message"
    private static final Pattern PARTY_CHAT_PATTERN = Pattern.compile("^(Party > .*?: )(.*)$");
    
    // Variables de estado
    public static boolean isEnabled = true;
    public static String targetLanguage = "es";

    @Override
    public void onInitializeClient() {
        // Cargar Configuración almacenada localmente
        ConfigManager.loadConfig();

        // Registrar Atajo de Teclado (Keybind 'V' por defecto)
        KeyMapping openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
            "key.translator.open_menu", 
            InputConstants.Type.KEYSYM, 
            GLFW.GLFW_KEY_V, 
            "category.translator"
        ));

        // Evento de Tick para abrir el menú visual
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.consumeClick()) {
                client.setScreen(new TranslatorOptionsScreen(client.screen));
            }
        });

        // Comando /translator
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            dispatcher.register(ClientCommandManager.literal("translator")
                .then(ClientCommandManager.literal("toggle")
                    .executes(context -> {
                        isEnabled = !isEnabled;
                        context.getSource().sendFeedback(Component.literal("§aTraductor " + (isEnabled ? "Activado" : "Desactivado")));
                        return 1;
                    })
                )
                .then(ClientCommandManager.literal("lang")
                    .then(ClientCommandManager.argument("language", StringArgumentType.word())
                        .executes(context -> {
                            targetLanguage = StringArgumentType.getString(context, "language");
                            context.getSource().sendFeedback(Component.literal("§aIdioma de traducción cambiado a: §e" + targetLanguage.toUpperCase()));
                            return 1;
                        })
                    )
                )
            );
        });

        // Evento de Intercepción de Chat de Jugadores (Con firma criptográfica)
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!isEnabled) return true;
            return processMessage(message.getString());
        });

        // Evento de Intercepción del Sistema (Usado por /tellraw, y por servidores grandes como Hypixel)
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!isEnabled || overlay) return true;
            return processMessage(message.getString());
        });
    }

    private static boolean processMessage(String rawText) {
        if (rawText.startsWith("Party >")) {
            Matcher matcher = PARTY_CHAT_PATTERN.matcher(rawText);
            if (matcher.matches()) {
                String prefix = matcher.group(1);
                String textToTranslate = matcher.group(2);

                TranslatorHelper.translateAsync(textToTranslate, targetLanguage).thenAccept(translatedText -> {
                    Minecraft client = Minecraft.getInstance();
                    if (client.gui != null) {
                        client.execute(() -> {
                            // Agregamos \u200B (espacio invisible) para evitar bucles infinitos
                            client.gui.getChat().addMessage(Component.literal("§9\u200BParty > §f" + rawText.substring(8, prefix.length()) + "§e" + translatedText));
                        });
                    }
                });
                return false; // Cancela el mensaje original
            }
        }
        return true; // Permite el paso de mensajes normales
    }
}