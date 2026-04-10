package com.example.client;

import com.example.client.gui.TranslatorOptionsScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ExampleModClient implements ClientModInitializer {

    // Regex to match "Party > [MVP+] Name: Message" or "Party > Name: Message"
    private static final Pattern PARTY_CHAT_PATTERN = Pattern.compile(".*?(Party > )(.*?: )(.*)$");
    
    // Variables de estado
    public static boolean isEnabled = true;
    public static String targetLanguage = "es";

    @Override
    public void onInitializeClient() {
        // Cargar Configuración almacenada localmente
        ConfigManager.loadConfig();

        // Registrar Atajo de Teclado usando Yarn Mappings 1.21.11 (KeyBinding.Category)
        KeyBinding.Category CATEGORY = new KeyBinding.Category(
            net.minecraft.util.Identifier.of("translator", "main")
        );

        KeyBinding openMenuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
            "key.translator.open_menu", 
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V, 
            CATEGORY
        ));

        // Evento de Tick para abrir el menú visual
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            while (openMenuKey.wasPressed()) {
                client.setScreen(new TranslatorOptionsScreen(client.currentScreen));
            }
        });

        // Registrar Evento del Custom HUD Overlay
        net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback.EVENT.register(com.example.client.gui.PartyChatHud::render);

        // Evento de Intercepción de Chat de Jugadores (Con firma criptográfica)
        ClientReceiveMessageEvents.ALLOW_CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            if (!isEnabled) return true;
            processMessage(message.getString());
            return true; // Siempre permitimos que el chat de vanilla reciba el original
        });

        // Evento de Intercepción del Sistema (Usado por /tellraw, y por servidores grandes como Hypixel)
        ClientReceiveMessageEvents.ALLOW_GAME.register((message, overlay) -> {
            if (!isEnabled || overlay) return true;
            processMessage(message.getString());
            return true; // Siempre permitimos que el chat de vanilla reciba el original
        });

        // Mensaje de bienvenida al unirse a un servidor o mundo
        net.fabricmc.fabric.api.client.networking.v1.ClientPlayConnectionEvents.JOIN.register((handler, sender, client) -> {
            String version = net.fabricmc.loader.api.FabricLoader.getInstance().getModContainer("modid")
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("1.0.0");
            
            client.execute(() -> {
                if (client.inGameHud != null && isEnabled) {
                    client.inGameHud.getChatHud().addMessage(
                        Text.literal("§8[§bPartyTranslator§8] §7» §aFuncionando correctamente. §7Versión: §e" + version)
                    );
                }
            });
        });
    }

    private static void processMessage(String rawText) {
        if (rawText.contains("Party >") && !rawText.contains("\u200B")) {
            Matcher matcher = PARTY_CHAT_PATTERN.matcher(rawText);
            if (matcher.matches()) {
                String userPart = matcher.group(2);
                String textToTranslate = matcher.group(3);

                TranslatorHelper.translateAsync(textToTranslate, targetLanguage).thenAccept(translatedText -> {
                    MinecraftClient client = MinecraftClient.getInstance();
                    if (client.inGameHud != null) {
                        client.execute(() -> {
                            Text vanilatxt = Text.literal("§9\u200BParty > §f" + userPart + "§e" + translatedText);
                            // Enviar a Vanilla Chat
                            client.inGameHud.getChatHud().addMessage(vanilatxt);
                            // Enviar a PartyChatHud exclusivo
                            com.example.client.gui.PartyChatHud.addMessage(vanilatxt);
                        });
                    }
                });
            }
        }
    }
}