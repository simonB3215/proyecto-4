package com.example.client;

import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

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

        // Evento de Intercepción de Chat
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!isEnabled) return true; // Si está desactivado, saltar inyección

            String rawText = message.getString();

            if (rawText.startsWith("Party >")) {
                Matcher matcher = PARTY_CHAT_PATTERN.matcher(rawText);
                if (matcher.matches()) {
                    String prefix = matcher.group(1);
                    String textToTranslate = matcher.group(2);

                    TranslatorHelper.translateAsync(textToTranslate, targetLanguage).thenAccept(translatedText -> {
                        Minecraft client = Minecraft.getInstance();
                        if (client.gui != null) {
                            client.execute(() -> {
                                client.gui.getChat().addMessage(Component.literal("§9Party > §f" + rawText.substring(8, prefix.length()) + "§e" + translatedText));
                            });
                        }
                    });
                    return false; // Cancela el mensaje original para que no se muestre
                }
            }
            return true; // Permite que se muestren los demás mensajes
        });

        // Renderizado del HUD en pantalla
        HudRenderCallback.EVENT.register((guiGraphics, tickCounter) -> {
            if (!isEnabled) return; // Si está desactivado, ocultar HUD

            Minecraft client = Minecraft.getInstance();
            if (client.font != null) {
                // Color blanco (0xFFFFFF), con sombra (true) en la esquina superior izquierda
                guiGraphics.drawString(client.font, "Traducción: " + targetLanguage.toUpperCase(), 5, 5, 0xFFFFFF, true);
            }
        });
    }
}